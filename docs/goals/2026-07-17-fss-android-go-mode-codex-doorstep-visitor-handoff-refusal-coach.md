# FSS Android /go continuation — Doorstep Visitor & Home Handoff Refusal Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `97de8ca534f74bb448f8311282587d51be80785f` — `feat: add bank staff disclosure scripts`

Parent evaluator reconciled the Hermes background notification: wrapper showed exit code 1/trailing `logout`, but worker log had `CODEX_RC=0`, a local commit, and a DONE summary.

Parent-side state and evidence collected after the commit:

```bash
git status --short --branch --ignored=matching
# ## main...origin/main [ahead 45]
# !! .gradle/
# !! .tools/
# !! app/build/
# !! local.properties

git show --stat --oneline --summary HEAD
# 97de8ca feat: add bank staff disclosure scripts
# 8 files changed, 3720 insertions(+), 38 deletions(-)
```

Parent verification command:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
python3 - <<'PY'
from pathlib import Path
apk = Path('app/build/outputs/apk/debug/app-debug.apk')
print(f'APK_EXISTS={apk.exists()}')
if apk.exists(): print(f'APK_BYTES={apk.stat().st_size}')
PY
git diff --check
git status --short --branch --ignored=matching
```

Result: `BUILD SUCCESSFUL`; APK exists with `1572053` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 45 commits.

Focused JUnit XML evidence after the bank/ATM staff slice:

- `BankAtmStaffDisclosureScriptCoachTest`: debug 17/17, release 17/17, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 26/26, release 26/26, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 36/36, release 36/36, failures 0, errors 0, skipped 0.

Manifest/source evidence:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `RECORD_AUDIO`, `CAMERA`, media/storage, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device-admin, notification-listener, VPN, or package-install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>`, no `<service>`, no `<receiver>`.
- Bounded bank/ATM staff slice scan found `SLICE_FORBIDDEN_HITS=[]` over the new model plus screen/card/router/share helper.
- Bank/ATM staff share helper uses exactly manual `Intent.ACTION_SEND`, `text/plain`, `putExtra(Intent.EXTRA_TEXT, summary)` and no `Intent.EXTRA_STREAM`.
- Expected repo-wide legacy hits remain only in established official help/report paths (`Intent.ACTION_DIAL`, `Uri.parse`, `tel:`, `OfficialHelpDirectoryBuilder`, `directory.entries`, `dialNumber`) plus explanatory privacy-copy tokens (`OCR`, `RECORD_AUDIO`) and new `BankAtmStaff` class-name/copy hits.

Parent source inspection found no obvious bank/ATM staff privacy/routing issue: `BankAtmStaffDisclosureScriptCoachBuilder` uses only `analysis?.riskLevel`, `analysis?.matchedSignals`, `readiness.level`, and `incidentCount.coerceAtLeast(0)`; UI passes `FamilyReadinessBuilder.build(safetyPlan.load())` and `incidents.count()` only; action buttons route to existing flows; share uses only builder summary; README documents no permissions/network/location/map/staff identity/bank/cash/payment/official lookup/dial/SMS/contact/media/file/clipboard/credential/account/device/scheduler/storage/mutation behavior.

## Product intent for next cohesive slice

Add a **Doorstep Visitor & Home Handoff Refusal Coach** for the moment someone is coming to or already at the user’s home/apartment/office/building lobby/door, claiming to be police/prosecutor/FSS/bank/security/courier/quick-service/repair/platform staff, and asking to enter, receive cash/card/bankbook/device/documents, collect a package/envelope, check a phone/app, keep the phone call open, or keep the visit secret.

This slice should be a local, stateless, value-free coach that gives short refusal scripts and existing app routes. It must **not** use doorbell/camera/microphone, contacts/SMS/calls, location/map/address, courier tracking, staff identity checks, official lookup, emergency dispatch, report submission, device settings, bank/payment/card/account actions, evidence/file/photo attachments, or persistent state.

Good user story:

> “Someone says they are coming to my door or is already outside to collect cash, documents, cards, a bankbook, a phone, or a package. Before I open the door or hand anything over, show me a short script and safe next routes without recording my address, visitor identity, amount, or incident details.”

This complements the cash/courier pause and bank/ATM staff scripts. It should route to cash/courier pause, bank/ATM staff disclosure, money transfer pause, official-pressure check, active-contact exit, separate-channel verification, family callback/safe-call, trusted helper, family alert/roles/debrief, evidence privacy, official help/report prep, exposure/account/remote/credential/link/gift/recovery follow-ups, and safety/follow-up flows.

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `DoorstepVisitorHandoffRefusalCoach.kt`:
   - `DoorstepVisitorHandoffRefusalCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): DoorstepVisitorHandoffRefusalCoach`
   - Pure/stateless; no Android imports.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction or literal plan values
     - `incidentCount.coerceAtLeast(0)` as a boolean/count category only
   - Must not use/display/share raw analysis input, score, action plan, summary, redacted clue, incident bodies/previews, `IncidentPreviewRedactor`, saved incident contents, `SafetyPlan.safeWord`, callback script literals, contact labels/numbers, official directory entries/numbers, exact home/building/address/route/location/door/lobby/unit/elevator/intercom/CCTV/doorbell data, visitor/courier/security/repair/police/prosecutor/bank/platform names, phone numbers, URLs/domains, QR/barcode/code values, package/app/file/media/URI values, cash amounts/serials/banknotes, account/card/bank/payment/wallet/transaction values, OTP/PIN/password/security answers, resident IDs, device IDs, caller ID/call state/SMS/contact/notification state, recipient/helper/lawyer/investigator/courier names, case/report/submission/receipt/status values, delivered/collected/deposited/scheduled/handoff/payment/refund/chargeback/recovery/freeze/cancel/block/trace outcomes, or legal/financial result values.

2. Refusal/script cards:
   - Fixed manual cards, for example:
     - “문을 열기 전 상대가 준 방문·수거 지시를 멈추기”
     - “현금·카드·통장·휴대폰·서류·봉투를 문밖 사람에게 건네지 않기”
     - “인터폰·문 앞·로비에서 값 없는 거절 문장만 말하기”
     - “통화·채팅을 유지한 채 방문자에게 안내하지 않기”
     - “기관·은행·플랫폼 직함과 문서·사건 압박을 현실 점검으로 분리하기”
     - “가족·보호자·경비/관리실·공식 도움·신고 준비로 이어갈 다음 경로 고르기”
   - Each card should include:
     - id/display order/title
     - doorstep/handoff pressure situation sentence
     - short value-free refusal script
     - why it matters
     - do-not-open/let-in/handoff/say/share/attach/record boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, financial/cash/payment signals (`URGENT_TRANSFER`, `BAIT`, `GIFT_CRYPTO`, `SAFE_ACCOUNT_FRAUD`, `LOAN_REFINANCE_FEE`, `OVERSEAS_PAYMENT_ALERT`), official-pressure signals (`IMPERSONATION`, `SECRECY`, `SAFE_ACCOUNT_FRAUD`), live-call/app/link/courier-control signals (`LIVE_CALL_CONTROL`, `APP_INSTALL`, `LINK`, `PARCEL_INVITATION_SMISHING`), credential/OTP/gift-card signals (`CREDENTIALS`, `GIFTICON_PIN`, `OVERSEAS_PAYMENT_ALERT`), recovery-promise contexts (`BAIT`, `GIFT_CRYPTO`, `SAFE_ACCOUNT_FRAUD`, `LOAN_REFINANCE_FEE`, `GIFTICON_PIN`, `OVERSEAS_PAYMENT_ALERT`), family emergency/deepfake/phone-number-change, saved incident count, and low/in-progress/ready readiness.
   - No-analysis/default should be conservative: show all refusal cards and never claim a visitor, courier, guard, staff member, police/prosecutor/FSS/bank/platform person, document, case, parcel, address, route, home visit, payment, request, handoff, device, account, card, or report is official, verified, safe, valid, accepted, submitted, received, protected, legal, resolved, complete, blocked, cancelled, reversed, recovered, refunded, frozen, traced, delivered, collected, deposited, scheduled, guaranteed, or problem-free.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `doorstepVisitorHandoffRefusalCoachScreen()`.
   - Add route buttons from relevant screens:
     - Home dashboard/readiness section.
     - Analysis result.
     - Quick Stop.
     - Cash Withdrawal & Courier Handoff Pause Coach.
     - Bank/ATM Staff Disclosure Script Coach.
     - Money Transfer Pause Gate.
     - Official Pressure Reality Check Coach.
     - Active Contact Exit Scripts.
     - Recovery Promise Reality Check Coach.
     - Link & App Install Quarantine Coach.
     - Gift Card Code Pause Coach.
     - Credential Secret Freeze Coach.
     - Account & Device Lockdown Checklist.
     - Remote Access Cutoff Coach.
     - Exposure Impact Triage.
     - Separate Channel Verification Coach.
     - Verification Outcome Next Steps Board.
     - Recontact Boundary Coach.
     - New Number Quarantine Coach.
     - Voice Impersonation Reality Check Coach.
     - Evidence Handoff Privacy Coach.
     - Evidence Kit / Incident Timeline if appropriate.
     - Official Help Directory and Official Report Prep Card.
     - Family Alert / Trusted Helper Review / Family Response Role Board / Calm Debrief / Follow-up Planner / Safety Flow Router / Privacy Center.
   - Action buttons must delegate to existing screens/flows only; do not duplicate visitor identity checking, official lookup, dialing, browsing, report submission, legal advice, map/location/navigation, courier tracking, meeting scheduling, bank/cash/card/account/payment, device settings/freeze, evidence/media/file/photo, contact/SMS, or family/contact implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.

4. Privacy Center / README:
   - Update `PrivacyCenter.kt` inventory/copy so this new coach is named as local/manual/no-permission/no-network/no-map/no-location/no-route/no-navigation/no-geocoder/no-address validation/no home/building/door/intercom/camera/doorbell access/no staff/visitor/courier identity check/no courier tracking/no meeting scheduling/no official lookup/no caller-ID/phone validation/no document/case validation/no report submission/no dial/no SMS/no contacts/calls/notifications/no QR/barcode/OCR/camera/no file/media/no evidence attachment/no URI share/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no payment/banking/NFC/wallet/blockchain/refund/chargeback/gift-card action/no app/package/device/settings mutation/no device-admin/accessibility/VPN/usage-stats/no auto-send/no scheduler/no storage/no mutation.
   - Update `README.md` flow and add a section for the new feature.

5. Tests:
   - Add `DoorstepVisitorHandoffRefusalCoachTest.kt`.
   - Update privacy/source regression tests and Privacy Center tests.
   - Include tests proving:
     - default/no-analysis is conservative, recommends all refusal cards/actions, and avoids unsafe official/verified/safe/completed/status claims.
     - financial/cash/payment signals route to cash/courier pause, bank staff disclosure, money pause, active-contact exit, official pressure, separate-channel verification, account/exposure/evidence/helper/help/report without cash/card/payment/account actions or map/location/address/door/courier behavior.
     - official pressure/secrecy routes to official pressure, active contact exit, family alert/debrief, evidence/helper/help/report without official-number lookup, visitor/staff identity check, document/case validation, dial/browser/SMS/report submission.
     - live-call/app/link/courier-like pressure routes to active contact exit, link/app quarantine, remote cutoff, cash/courier pause, exposure/account/privacy without call-state APIs, app/device/settings/package APIs, maps/location/courier tracking, or remote disconnect/device cleaning claims.
     - credential/OTP/gift-card signals route to credential freeze, gift-card pause, account lockdown, safe paste/privacy, remote cutoff, exposure triage without exposing OTP/PIN/password/account/card/bank/wallet/gift-card/cash values.
     - family emergency/deepfake/phone-number-change routes to separate-channel verification, family callback, voice reality, new-number quarantine, safe-word reset, safe call scripts, family alert without contact/safe-word/callback values or contacts/calls/SMS.
     - saved incident changes recommendations only by count category and does not read incident contents/previews.
     - low/in-progress readiness routes to safety plan, safe-word reset, readiness roadmap, safety drill, safe call scripts; ready readiness says readiness does not make visitor/doorstep/request/handoff safe/trusted/official.
     - every card has complete fields, stable order, unique route actions, and all action IDs map to existing flows.
     - share summary contains only generic refusal/card/route/action labels and no raw input, redacted clue, plan literals, safe-word/callback/contact values, phone numbers, addresses/routes/locations/home/unit/lobby/intercom/doorbell/camera/staff/courier IDs, URLs/domains, long numbers, QR/barcode/code/package/app/file/media/URI/path values, account/card/bank/wallet/transaction/amount/cash serial/OTP/PIN/password/security answer/resident-like values, device identifiers, official numbers, directory values, agency/helper/lawyer/investigator/staff/courier/visitor names, case/report/submission/receipt IDs, screenshot/image/OCR/audio/transcription values, clipboard/autofill/account/login state, call/SMS/notification state, recipient names, verification/result/status values, delivered/collected/deposited/scheduled/handoff/payment/refund/chargeback/recovery/freeze/cancel/block/trace status, or legal/financial outcome values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no home/door/building/intercom/camera/doorbell access/no visitor identity check/no bank/cash/card/payment action/no map/location/route/navigation/geocoder/address validation/no courier tracking/no meeting scheduling/no official lookup/no phone/caller/document/case validation/no report submission/no dial/call/SMS/no contacts/call-log/notifications/no QR/barcode/OCR/camera/no file/photo/media/no attachment/no URI share/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no payment/banking/NFC/wallet/blockchain/refund/chargeback/no app/package/device/settings mutation/no device-admin/accessibility/VPN/usage-stats/no auto-send/no permissions/no scheduler.
   - Add/extend source-reading tests proving model and bounded UI slice avoid all forbidden APIs/values. Allow `openHelpDirectory(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)` only in the action router as a route to the existing official-help screen.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No doorbell/camera/mic/intercom/CCTV/home automation access, visitor identity check, courier tracking, branch/official lookup, map/location/navigation/geocoder/address/route validation, emergency dispatch, report submission, legal/case lookup, caller-ID checking, phone-number validation, or document validation.
- No bank/ATM/cash/card/account/payment operation, refund/chargeback/recovery/freeze/cancel/trace, app/device/settings mutation, or financial/legal automation.
- No network/browser/webview/map/geocoder/navigation APIs and no `Intent.ACTION_VIEW`.
- No dialing/calling/hang-up/call-screening/call-blocking/call-state/call-log/SMS/send-to/recipient APIs.
- No contact/address-book reading/writing/editing/picking, notification listening/monitoring/filtering, clipboard/text watcher/autofill/credential APIs, account/auth/crypto APIs.
- No QR/barcode scan, OCR, camera, image analysis, screenshot, file/media picker, attachment, stream, URI share, upload, or evidence sync.
- No PackageManager/app inventory/app label/package APIs, package install/uninstall/settings/accessibility/device-admin/VPN/usage-stats/notification-listener APIs, app permission/grant/revoke APIs, malware scanning/antivirus APIs, or device-control behavior.
- No display/share/use of official directory phone numbers, agency/bank/branch/store/brand/staff/helper/lawyer/investigator/courier/visitor names, exact home/building/lobby/unit/address/route/meeting/doorbell/intercom/camera values, cash amounts/serials/banknotes, exact links/domains, QR/barcode/code values, package/app/file/media/URI values, case/report/receipt IDs, literal OTP/PIN/password/security answers, account/card/bank/wallet/transaction/payment/refund/chargeback values, `SafetyPlan.safeWord`, callback script text, contact labels/numbers, raw analysis input, redacted clues, incident previews/bodies, device IDs, exact phone/channel/profile values, or any delivered/collected/deposited/scheduled/handoff/payment/refund/chargeback/recovery/freeze/cancel/block/trace status values.
- No mutation of safety plan, incident log, official directory, contacts, notification state, call/SMS state, location/map/route/courier/parcel/locker/meeting/home/door state, bank/ATM/cash/payment/card/account state, link/app/package/permission/device state, credential status, report/submission/receipt status, purchase/balance/redeem/refund/chargeback/recovery/freeze/cancel/block/trace status, or verification status.
- No alarms, reminders, CalendarContract, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only. It must not attach files/streams or include any official/contact/channel/profile/account/payment/wallet/link/app/location/staff/visitor/courier/cash/device/result value.
- Do not claim a visitor, staff member, helper, courier, investigator, institution, bank, branch, route, address, home, door, lobby, locker, parcel, cash, document, case, report, payment, account, card, bank action, wallet, transaction, credential, login, device, location, handoff, purchase, redemption, balance, chargeback, refund, freeze, trace, cancellation, recovery, app install/removal/permission, or malware/device state is official, real, safe, secure, verified, valid, accepted, submitted, received, installed, uninstalled, disabled, cleaned, blocked, removed, revoked, granted, scanned, inspected, checked, resolved, complete, recovered, refunded, frozen, protected, legal, trusted, delivered, collected, deposited, scheduled, malware-free, guaranteed, or problem-free.

## Verification gate before commit

Run from repo root:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
python3 - <<'PY'
from pathlib import Path
import re
manifest = Path('app/src/main/AndroidManifest.xml').read_text()
for token in ['CALL_PHONE','READ_SMS','SEND_SMS','RECEIVE_SMS','READ_CALL_LOG','READ_CONTACTS','WRITE_CONTACTS','READ_PHONE_STATE','ANSWER_PHONE_CALLS','RECORD_AUDIO','CAMERA','READ_MEDIA_IMAGES','READ_MEDIA_VIDEO','READ_EXTERNAL_STORAGE','WRITE_EXTERNAL_STORAGE','INTERNET','ACCESS_FINE_LOCATION','ACCESS_COARSE_LOCATION','POST_NOTIFICATIONS','SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED','PACKAGE_USAGE_STATS','QUERY_ALL_PACKAGES','BIND_ACCESSIBILITY_SERVICE','BIND_DEVICE_ADMIN','BIND_NOTIFICATION_LISTENER_SERVICE','BIND_VPN_SERVICE','REQUEST_INSTALL_PACKAGES']:
    print(f'{token}={token in manifest}')
needle='android:allowBackup="false"'
print(f'allowBackup=false={needle in manifest}')
print(f'uses-permission-tag={"<uses-permission" in manifest}')
service_or_receiver = bool(re.search(r'<(?:service|receiver)\\b', manifest))
print(f'service-or-receiver={service_or_receiver}')
PY
python3 - <<'PY'
from pathlib import Path
apk = Path('app/build/outputs/apk/debug/app-debug.apk')
print(f'APK_EXISTS={apk.exists()}')
if apk.exists(): print(f'APK_BYTES={apk.stat().st_size}')
PY
git diff --check
git status --short --branch --ignored=matching
```

Also inspect focused JUnit XML for `DoorstepVisitorHandoffRefusalCoachTest`, `PrivacyCenterBuilderTest`, and `PrivacyPostureRegressionTest`.

Note: repo-wide scan may continue to show unchanged legacy official-help/report helper APIs (`OfficialHelpDirectoryBuilder`, `directory.entries`, `dialNumber`, `Intent.ACTION_DIAL`, `tel:`, `Uri.parse`) and explanatory privacy-copy mentions of OCR/audio/permissions. If so, add a bounded slice-specific source scan/test proving the new Doorstep Visitor slice itself does not contain forbidden implementation APIs or values. `openHelpDirectory(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)` is allowed only in the UI action router.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-doorstep-visitor-handoff-refusal-coach.md
git commit -m "feat: add doorstep handoff refusal coach"
```

Do **not** push.

## Final response schema

Return a concise final summary with:

- Files changed count and key files.
- Verification command and result.
- Focused JUnit counts for the new tests plus Privacy Center and privacy regression tests.
- Manifest/API scan result.
- APK path and byte size.
- Commit SHA and message.
- Worktree status and ahead count.
- Explicit `No push performed`.
