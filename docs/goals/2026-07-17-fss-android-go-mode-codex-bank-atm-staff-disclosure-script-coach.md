# FSS Android /go continuation — Bank/ATM Staff Disclosure Script Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `8118f019396e817360a9e549d65f7c2baccda8a8` — `feat: add cash courier handoff pause coach`

Parent evaluator reconciled the Hermes background notification: wrapper showed exit code 1/trailing `logout`, but the worker log had `CODEX_RC=0`, a local commit, and a DONE summary.

Parent-side state and evidence collected after the commit:

```bash
git status --short --branch --ignored=matching
# ## main...origin/main [ahead 44]
# !! .gradle/
# !! .tools/
# !! app/build/
# !! local.properties

git show --stat --oneline --summary HEAD
# 8118f01 feat: add cash courier handoff pause coach
# 8 files changed, 3556 insertions(+), 37 deletions(-)
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

Result: `BUILD SUCCESSFUL`; APK exists with `1550515` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 44 commits.

Focused JUnit XML evidence after the cash/courier slice:

- `CashWithdrawalCourierHandoffPauseCoachTest`: debug 17/17, release 17/17, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 25/25, release 25/25, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 35/35, release 35/35, failures 0, errors 0, skipped 0.

Manifest/source evidence:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `RECORD_AUDIO`, `CAMERA`, media/storage, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device-admin, notification-listener, VPN, or package-install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>`, no `<service>`, no `<receiver>`.
- Bounded cash/courier slice scan found `SLICE_FORBIDDEN_HITS=[]` over the new model plus screen/card/router/share helper.
- Cash/courier share helper uses exactly manual `Intent.ACTION_SEND`, `text/plain`, `putExtra(Intent.EXTRA_TEXT, summary)` and no `Intent.EXTRA_STREAM`.
- Expected repo-wide legacy hits remain only in established official help/report paths (`Intent.ACTION_DIAL`, `Uri.parse`, `tel:`, `OfficialHelpDirectoryBuilder`, `directory.entries`, `dialNumber`) plus explanatory privacy-copy tokens (`OCR`, `RECORD_AUDIO`) and the new `Courier` class-name/copy hits.

Parent source inspection found no obvious cash/courier privacy/routing issue: `CashWithdrawalCourierHandoffPauseCoachBuilder` uses only `analysis?.riskLevel`, `analysis?.matchedSignals`, `readiness.level`, and `incidentCount.coerceAtLeast(0)`; UI passes `FamilyReadinessBuilder.build(safetyPlan.load())` and `incidents.count()` only; action buttons route to existing flows; share uses only builder summary; README documents no permissions/network/location/map/courier/cash/bank/payment/official lookup/dial/SMS/contact/media/file/clipboard/credential/account/device/scheduler/storage/mutation behavior.

## Product intent for next cohesive slice

Add a **Bank/ATM Staff Disclosure Script Coach** screen/card for the moment a victim is already near a bank, ATM, convenience store, post office, courier counter, apartment security desk, police/help desk, or other public/service counter after being told to withdraw cash, keep a call open, hide the situation, or hand over cash/card/bankbook/device/documents.

The new slice is not a map, branch finder, official lookup, dialer, report submitter, bank/cash/card/account action, staff identity checker, legal/financial advisor, courier tracker, emergency dispatch, contact/SMS tool, evidence/file/photo tool, scheduler, or monitoring system. It should give **short value-free scripts the user can manually read to a nearby trusted staff member/helper** without exposing amounts, locations, account/card numbers, case IDs, official numbers, caller IDs, route details, courier IDs, family safe words, incident text, or device/payment status.

Good user story:

> “I’m at/near a bank, ATM, counter, lobby, or pickup/drop-off point because someone pressured me. I need a short sentence to ask an employee/helper to pause the transaction or handoff without giving the app my location, amount, account, or case details.”

This slice should complement, not replace, the existing Cash Withdrawal & Courier Handoff Pause Coach. It should route back to cash/courier pause, money transfer pause, official-pressure reality check, active-contact exit scripts, separate-channel verification, trusted helper review, family alert/roles/debrief, evidence privacy, official help/report prep, exposure/account/remote/credential/link/gift/recovery follow-ups, and safety/follow-up flows.

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `BankAtmStaffDisclosureScriptCoach.kt`:
   - `BankAtmStaffDisclosureScriptCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): BankAtmStaffDisclosureScriptCoach`
   - Pure/stateless; no Android imports.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction or literal plan values
     - `incidentCount.coerceAtLeast(0)` as a boolean/count category only
   - Must not use or display/share raw analysis input, score, action plan, summary, redacted clue, incident bodies/previews, `IncidentPreviewRedactor`, saved incident contents, `SafetyPlan.safeWord`, callback script literals, contact labels/numbers, official directory entries/numbers, exact bank/branch/store/counter/agency/person names, locations/addresses/routes/meeting points, cash amounts/serials/banknotes, account/card/bank/payment/wallet/transaction values, OTP/PIN/password/security answers, resident IDs, device IDs, URLs/domains, QR/barcode/code values, package/app/file/media/URI values, caller ID/call state/SMS/contact/notification state, recipient/staff/helper/lawyer/investigator/courier names, case/report/submission/receipt/status values, delivered/collected/deposited/scheduled/handoff/cash/payment/refund/chargeback/recovery/freeze/cancel/block/trace outcomes, or legal/financial result values.

2. Script cards:
   - Fixed manual cards, for example:
     - “은행·ATM 직원에게 현금 인출 압박을 짧게 알리기”
     - “직원이 금액·계좌·사건 내용을 묻기 전에 ‘값 없이 멈춤’ 요청하기”
     - “통화·채팅을 끊고 창구/보안요원에게 혼자 있지 않다고 말하기”
     - “택배·퀵·우편·보관함 지시를 카운터/관리실에 값 없이 멈춤 요청하기”
     - “기관·은행·플랫폼 직함은 공식 확인 전 증거가 아니라고 말하기”
     - “가족·보호자·공식 도움·신고 준비로 이어갈 다음 한 문장 고르기”
   - Each card should include:
     - id/display order/title
     - staff-disclosure situation sentence to read manually
     - short script text safe to say aloud without values
     - why it matters
     - do-not-say/share/attach/record boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, financial/cash/payment signals (`URGENT_TRANSFER`, `BAIT`, `GIFT_CRYPTO`, `SAFE_ACCOUNT_FRAUD`, `LOAN_REFINANCE_FEE`, `OVERSEAS_PAYMENT_ALERT`), official-pressure signals (`IMPERSONATION`, `SECRECY`, `SAFE_ACCOUNT_FRAUD`), live-call/app/link/courier-control signals (`LIVE_CALL_CONTROL`, `APP_INSTALL`, `LINK`, `PARCEL_INVITATION_SMISHING`), credential/OTP/gift-card signals (`CREDENTIALS`, `GIFTICON_PIN`, `OVERSEAS_PAYMENT_ALERT`), recovery-promise contexts (`BAIT`, `GIFT_CRYPTO`, `SAFE_ACCOUNT_FRAUD`, `LOAN_REFINANCE_FEE`, `GIFTICON_PIN`, `OVERSEAS_PAYMENT_ALERT`), family emergency/deepfake/phone-number-change, saved incident count, and low/in-progress/ready readiness.
   - No-analysis/default should be conservative: show all script cards and never claim the bank/ATM/staff/counter/security/helper/courier/agency/person/caller/document/case/payment/request/handoff is official, verified, safe, valid, accepted, submitted, received, protected, legal, resolved, complete, blocked, cancelled, reversed, recovered, refunded, frozen, traced, delivered, collected, deposited, scheduled, guaranteed, or problem-free.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `bankAtmStaffDisclosureScriptCoachScreen()`.
   - Add route buttons from relevant screens:
     - Home dashboard/readiness section.
     - Analysis result.
     - Quick Stop.
     - Cash Withdrawal & Courier Handoff Pause Coach.
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
   - Action buttons must delegate to existing screens/flows only; do not duplicate bank/cash/card/account/payment, official lookup, dialing, browsing, report submission, legal advice, map/location/navigation, courier tracking, meeting scheduling, device settings/freeze, evidence/media/file/photo, contact/SMS, or family/contact implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.

4. Privacy Center / README:
   - Update `PrivacyCenter.kt` inventory/copy so this new coach is named as local/manual/no-permission/no-network/no-map/no-location/no-route/no-navigation/no-geocoder/no-address validation/no branch/ATM lookup/no staff identity check/no official lookup/no caller-ID/phone validation/no document/case validation/no report submission/no dial/no SMS/no contacts/calls/notifications/no QR/barcode/OCR/camera/no file/media/no evidence attachment/no URI share/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no payment/banking/NFC/wallet/blockchain/refund/chargeback/gift-card action/no courier tracking/no meeting scheduling/no app/package/device/settings mutation/no device-admin/accessibility/VPN/usage-stats/no auto-send/no scheduler/no storage/no mutation.
   - Update `README.md` flow and add a section for the new feature.

5. Tests:
   - Add `BankAtmStaffDisclosureScriptCoachTest.kt`.
   - Update privacy/source regression tests and Privacy Center tests.
   - Include tests proving:
     - default/no-analysis is conservative, recommends all script cards/actions, and avoids unsafe status claims.
     - financial/cash/payment signals route to cash/courier pause, money pause, active contact exit, official pressure, separate-channel verification, account/exposure/evidence/helper/help/report without ATM/bank/cash/card/payment actions or map/location/branch lookup.
     - official pressure/secrecy routes to official pressure, active contact exit, family alert/debrief, evidence/helper/help/report without official-number lookup, document/case validation, dial/browser/SMS/report submission.
     - live-call/app/link/courier-like pressure routes to active contact exit, link/app quarantine, remote cutoff, exposure/account/privacy without call-state APIs, app/device/settings/package APIs, maps/location/courier tracking, or remote disconnect/device cleaning claims.
     - credential/OTP/gift-card signals route to credential freeze, gift-card pause, account lockdown, safe paste/privacy, remote cutoff, exposure triage without exposing OTP/PIN/password/account/card/bank/wallet/gift-card/cash values.
     - family emergency/deepfake/phone-number-change routes to separate-channel verification, family callback, voice reality, new-number quarantine, safe-word reset, safe call scripts, family alert without contact/safe-word/callback values or contacts/calls/SMS.
     - saved incident changes recommendations only by count category and does not read incident contents/previews.
     - low/in-progress readiness routes to safety plan, safe-word reset, readiness roadmap, safety drill, safe call scripts; ready readiness says readiness does not make staff/bank/ATM/cash/handoff/request safe/trusted/official.
     - every card has complete fields, stable order, unique route actions, and all action IDs map to existing flows.
     - share summary contains only generic script/card/route/action labels and no raw input, redacted clue, plan literals, safe-word/callback/contact values, phone numbers, addresses/routes/locations/branch/staff/courier IDs, URLs/domains, long numbers, QR/barcode/code/package/app/file/media/URI/path values, account/card/bank/wallet/transaction/amount/cash serial/OTP/PIN/password/security answer/resident-like values, device identifiers, official numbers, directory values, agency/helper/lawyer/investigator/staff/courier names, case/report/submission/receipt IDs, screenshot/image/OCR/audio/transcription values, clipboard/autofill/account/login state, call/SMS/notification state, recipient names, verification/result/status values, delivered/collected/deposited/scheduled/handoff/cash/payment/refund/chargeback/recovery/freeze/cancel/block/trace status, or legal/financial outcome values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no bank/cash/card/payment action/no map/location/route/navigation/geocoder/address/branch validation/no staff/courier identity check/no courier tracking/no meeting scheduling/no official lookup/no phone/caller/document/case validation/no report submission/no dial/call/SMS/no contacts/call-log/notifications/no QR/barcode/OCR/camera/no file/photo/media/no attachment/no URI share/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no payment/banking/NFC/wallet/blockchain/refund/chargeback/no app/package/device/settings mutation/no device-admin/accessibility/VPN/usage-stats/no auto-send/no permissions/no scheduler.
   - Add/extend source-reading tests proving model and bounded UI slice avoid all forbidden APIs/values. Allow `openHelpDirectory(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)` only in the action router as a route to the existing official-help screen.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No bank/ATM/cash/card/account/payment operation, staff/courier/person identity check, branch finder, map/location/navigation/geocoder/address/route validation, courier/parcel/locker tracking, meet-up scheduling, official lookup, report submission, legal/case lookup, caller-ID checking, phone-number validation, or document validation.
- No network/browser/webview/map/geocoder/navigation APIs and no `Intent.ACTION_VIEW`.
- No dialing/calling/hang-up/call-screening/call-blocking/call-state/call-log/SMS/send-to/recipient APIs.
- No contact/address-book reading/writing/editing/picking, notification listening/monitoring/filtering, clipboard/text watcher/autofill/credential APIs, account/auth/crypto APIs.
- No QR/barcode scan, OCR, camera, image analysis, screenshot, file/media picker, attachment, stream, URI share, upload, or evidence sync.
- No PackageManager/app inventory/app label/package APIs, package install/uninstall/settings/accessibility/device-admin/VPN/usage-stats/notification-listener APIs, app permission/grant/revoke APIs, malware scanning/antivirus APIs, or device-control behavior.
- No payment/banking/card/wallet/blockchain/gift-card/refund/chargeback/recovery APIs, payment cancellation, refund/chargeback filing, wallet tracing, purchase/redeem/balance check, code/account/card freeze, cash serial tracking, or financial/legal automation.
- No display/share/use of official directory phone numbers, agency/bank/branch/store/brand/staff/helper/lawyer/investigator/courier names, exact locations/addresses/routes/meeting points/locker IDs/courier IDs, cash amounts/serials/banknotes, exact links/domains, QR/barcode/code values, package/app/file/media/URI values, case/report/receipt IDs, literal OTP/PIN/password/security answers, account/card/bank/wallet/transaction/payment/refund/chargeback values, `SafetyPlan.safeWord`, callback script text, contact labels/numbers, raw analysis input, redacted clues, incident previews/bodies, device IDs, exact phone/channel/profile values, or any delivered/collected/deposited/scheduled/handoff/payment/refund/chargeback/recovery/freeze/cancel/block/trace status values.
- No mutation of safety plan, incident log, official directory, contacts, notification state, call/SMS state, location/map/route/courier/parcel/locker/meeting state, bank/ATM/cash/payment/card/account state, link/app/package/permission/device state, credential status, report/submission/receipt status, purchase/balance/redeem/refund/chargeback/recovery/freeze/cancel/block/trace status, or verification status.
- No alarms, reminders, CalendarContract, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only. It must not attach files/streams or include any official/contact/channel/profile/account/payment/wallet/link/app/location/staff/courier/cash/device/result value.
- Do not claim a staff member, helper, courier, investigator, institution, bank, branch, ATM, route, address, locker, parcel, cash, document, case, report, payment, account, card, bank action, wallet, transaction, credential, login, device, location, handoff, purchase, redemption, balance, chargeback, refund, freeze, trace, cancellation, recovery, app install/removal/permission, or malware/device state is official, real, safe, secure, verified, valid, accepted, submitted, received, installed, uninstalled, disabled, cleaned, blocked, removed, revoked, granted, scanned, inspected, checked, resolved, complete, recovered, refunded, frozen, protected, legal, trusted, delivered, collected, deposited, scheduled, malware-free, guaranteed, or problem-free.

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

Also inspect focused JUnit XML for `BankAtmStaffDisclosureScriptCoachTest`, `PrivacyCenterBuilderTest`, and `PrivacyPostureRegressionTest`.

Note: repo-wide scan may continue to show unchanged legacy official-help/report helper APIs (`OfficialHelpDirectoryBuilder`, `directory.entries`, `dialNumber`, `Intent.ACTION_DIAL`, `tel:`, `Uri.parse`) and explanatory privacy-copy mentions of OCR/audio/permissions. If so, add a bounded slice-specific source scan/test proving the new Bank/ATM Staff Disclosure slice itself does not contain forbidden implementation APIs or values. `openHelpDirectory(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)` is allowed only in the UI action router.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-bank-atm-staff-disclosure-script-coach.md
git commit -m "feat: add bank staff disclosure scripts"
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
