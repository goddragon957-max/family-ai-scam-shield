# FSS Android /go continuation — SIM/Number Port-Out Pause Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `1830db94a60775aa9c6b1ab114810994ff239151` — `feat: add travel route pause coach`

Parent evaluator reconciled the Hermes background notification: wrapper showed exit code 1/trailing `logout`, but the process log/final output had `CODEX_RC=0`, a local commit, and a DONE summary.

Parent-side evidence collected after the commit:

```bash
git status --short --branch --ignored=matching
# ## main...origin/main [ahead 47]
# !! .gradle/
# !! .tools/
# !! app/build/
# !! local.properties

git show --stat --oneline --summary HEAD
# 1830db9 feat: add travel route pause coach
# 8 files changed, 3749 insertions(+), 42 deletions(-)
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

Result: `BUILD SUCCESSFUL`; APK exists with `1618089` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 47 commits.

Focused JUnit XML after the travel-route slice:

- `TravelRouteMeetingPointPauseCoachTest`: debug 18/18, release 18/18, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 28/28, release 28/28, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 38/38, release 38/38, failures 0, errors 0, skipped 0.

Manifest/source evidence:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `RECORD_AUDIO`, `CAMERA`, media/storage, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device-admin, notification-listener, VPN, or package-install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>`, no `<service>`, no `<receiver>`.
- Bounded travel-route slice implementation scan after stripping string literals: `SLICE_CODE_FORBIDDEN_HITS=[]`.
- `TravelRouteMeetingPointPauseCoach.kt` has no Android imports, no `SafetyPlan` reference, and no raw analysis field access (`input`, `score`, `actionPlan`, `summary`, redacted clue).
- UI uses `incidentCount = incidents.count()` and `FamilyReadinessBuilder.build(safetyPlan.load())`; no `incidents.load`, no persistence mutations.
- Share helper uses exactly manual `Intent.ACTION_SEND`, `text/plain`, `putExtra(Intent.EXTRA_TEXT, summary)`, and no `Intent.EXTRA_STREAM`.
- Expected repo-wide legacy hits remain only in established official-help/report paths (`Intent.ACTION_DIAL`, `Uri.parse`, `tel:`, `OfficialHelpDirectoryBuilder`, `directory.entries`, `dialNumber`) plus explanatory privacy-copy tokens (`OCR`, `RECORD_AUDIO`) and feature class/copy hits.

Parent source inspection found no obvious travel-route privacy/routing issue: `TravelRouteMeetingPointPauseCoachBuilder` uses only `analysis?.riskLevel`, `analysis?.matchedSignals`, `readiness.level`, and `incidentCount.coerceAtLeast(0)`; cards and UI show fixed value-free movement/meeting pause scripts and route to existing flows; README/Privacy Center document no permissions/network/location/map/route/navigation/address/traffic/transit/taxi/ride-hailing/courier tracking/meeting scheduling/official lookup/dial/SMS/contact/media/file/clipboard/credential/account/device/scheduler/storage/mutation behavior.

## Product intent for next cohesive slice

Add a **SIM / Number Port-Out Pause Coach** for the moment a scammer pressures the user to share telecom verification codes, carrier account details, USIM/eSIM/number-port information, device serial/IMEI-like data, or to visit/open a carrier route to “protect,” “recover,” “verify,” “transfer,” “cancel,” “reissue,” “unlock,” or “block” a phone number.

This slice should be a local, stateless, value-free coach that gives short “do not share carrier/SIM/number-port secrets; stop the port/reissue conversation; use existing safe routes” scripts. It must **not** use telephony/SIM/subscription APIs, carrier APIs, phone-number validation, SMS/call logs/call state, contacts, network/browser lookups, official/carrier lookup, account/auth/credential APIs, device identifiers, package/settings/device-control APIs, payment/banking/card actions, evidence/file/photo/media attachment, or persistent state.

Good user story:

> “Someone says my phone number, SIM, eSIM, carrier account, or verification code must be moved, reissued, protected, unlocked, cancelled, or recovered right now. Before I share a code, device number, carrier account detail, or visit a link/store/call, show me a short stop script and existing safe routes without reading my phone number, SMS, SIM, IMEI, account, code, carrier, contact, or incident details.”

This complements new-number quarantine, credential secret freeze, account lockdown, official pressure, link/app quarantine, remote cutoff, and separate-channel verification. It is about **telecom takeover / SIM swap / number port-out pressure** while staying purely educational and manual.

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `SimNumberPortOutPauseCoach.kt`:
   - `SimNumberPortOutPauseCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): SimNumberPortOutPauseCoach`
   - Pure/stateless; no Android imports.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction or literal plan values
     - `incidentCount.coerceAtLeast(0)` as a boolean/count category only
   - Must not use/display/share raw analysis input, score, action plan, summary, redacted clue, incident bodies/previews, `IncidentPreviewRedactor`, saved incident contents, `SafetyPlan.safeWord`, callback script literals, contact labels/numbers, official directory entries/numbers, exact phone number/new number/old number/channel/profile/carrier/telecom store/SIM/eSIM/USIM/IMSI/ICCID/IMEI/device serial/account ID/customer number/porting PIN/password/OTP/security answer/link/domain/app/package/file/media/URI values, SMS/call/caller-ID/contact/notification state, carrier plan/billing/payment/card/bank/wallet/transaction values, resident IDs, device IDs, helper/lawyer/investigator/staff/courier/driver names, case/report/submission/receipt/status values, ported/reissued/activated/deactivated/blocked/unblocked/cancelled/recovered/frozen/verified/changed/updated/saved/replaced/transferred/payment/refund/chargeback outcomes, or legal/financial result values.

2. Pause/script cards:
   - Fixed manual cards, for example:
     - “통신사·유심·번호이동 인증코드 말하기 멈추기”
     - “SIM/eSIM/USIM 재발급·번호이동·명의변경 지시를 값 없이 거절하기”
     - “새 번호·분실폰·통신사 직함·보호/복구 약속을 확인 근거에서 분리하기”
     - “링크·앱·원격지시로 통신/계정 정보를 입력하지 않기”
     - “가족 기존 경로·별도 확인·계정 보호·노출 분류로 이동하기”
     - “공식 도움·신고 준비·후속 점검 경로 고르기”
   - Each card should include:
     - id/display order/title
     - SIM/number-port pressure situation sentence
     - short value-free pause/refusal script
     - why it matters
     - do-not-share-code/port/reissue/call/SMS/contact/account/device/say/share/attach/record boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, credential/OTP signals (`CREDENTIALS`, `GIFTICON_PIN`, `OVERSEAS_PAYMENT_ALERT`), new-number/family verification signals (`PHONE_NUMBER_CHANGE`, `FAMILY_EMERGENCY`, `DEEPFAKE_VOICE`), official-pressure signals (`IMPERSONATION`, `SECRECY`, `SAFE_ACCOUNT_FRAUD`), live-call/app/link pressure (`LIVE_CALL_CONTROL`, `APP_INSTALL`, `LINK`, `PARCEL_INVITATION_SMISHING`), financial/recovery contexts (`URGENT_TRANSFER`, `BAIT`, `GIFT_CRYPTO`, `SAFE_ACCOUNT_FRAUD`, `LOAN_REFINANCE_FEE`, `OVERSEAS_PAYMENT_ALERT`, `GIFTICON_PIN`), saved incident count, and low/in-progress/ready readiness.
   - No-analysis/default should be conservative: show all pause cards and never claim a phone number, SIM/eSIM/USIM, carrier, store, app, link, device, account, credential, code, contact, caller, SMS, case, report, payment, refund, recovery, block, freeze, port-out, reissue, activation, deactivation, cancellation, replacement, transfer, update, verification, or protection state is official, verified, safe, valid, accepted, submitted, received, protected, legal, resolved, complete, blocked, unblocked, cancelled, reversed, recovered, refunded, frozen, traced, ported, reissued, activated, deactivated, changed, updated, saved, replaced, transferred, guaranteed, or problem-free.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `simNumberPortOutPauseCoachScreen()`.
   - Add route buttons from relevant screens:
     - Home dashboard/readiness section.
     - Analysis result.
     - Quick Stop.
     - Credential Secret Freeze Coach.
     - New Number Quarantine Coach.
     - Separate Channel Verification Coach.
     - Family Safe Word Reset Coach / Family Callback / Safe Call Scripts.
     - Account & Device Lockdown Checklist.
     - Remote Access Cutoff Coach.
     - Link & App Install Quarantine Coach.
     - Official Pressure Reality Check Coach.
     - Recovery Promise Reality Check Coach.
     - Money Transfer Pause Gate / Gift Card Code Pause Coach where appropriate.
     - Exposure Impact Triage.
     - Evidence Handoff Privacy Coach.
     - Verification Outcome Next Steps Board / Recontact Boundary Coach / Voice Impersonation Reality Check Coach.
     - Official Help Directory and Official Report Prep Card.
     - Family Alert / Trusted Helper Review / Family Response Role Board / Calm Debrief / Follow-up Planner / Safety Flow Router / Privacy Center.
   - Action buttons must delegate to existing screens/flows only; do not duplicate SIM status checks, carrier account checks, number validation, official lookup, dialing, browsing, report submission, legal advice, device settings, package/app inspection, credential/account reset, payment/banking, contact/SMS/call behavior, or family/contact implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.

4. Privacy Center / README:
   - Update `PrivacyCenter.kt` inventory/copy so this new coach is named as local/manual/no-permission/no-network/no-telephony/no-SIM/subscription/carrier APIs/no phone-number validation/no SMS/call/call-log/caller-ID/contact/notification access/no official/carrier lookup/no report submission/no dial/no browser/webview/no QR/barcode/OCR/camera/no file/media/no evidence attachment/no URI share/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no payment/banking/NFC/wallet/blockchain/refund/chargeback/gift-card action/no app/package/device/settings mutation/no IMEI/ICCID/IMSI/device-ID/serial reads/no device-admin/accessibility/VPN/usage-stats/no auto-send/no scheduler/no storage/no mutation.
   - Update `README.md` flow and add a section for the new feature.

5. Tests:
   - Add `SimNumberPortOutPauseCoachTest.kt`.
   - Update privacy/source regression tests and Privacy Center tests.
   - Include tests proving:
     - default/no-analysis is conservative, recommends all pause cards/actions, and avoids unsafe official/verified/safe/completed/status claims.
     - credential/OTP signals route to credential freeze, account lockdown, safe paste/privacy, remote cutoff, exposure triage without exposing OTP/PIN/password/porting PIN/account/card/bank/wallet/gift-card/cash/SIM/carrier values.
     - new-number/family/deepfake signals route to new-number quarantine, separate-channel verification, family callback, voice reality, safe-word reset, safe call scripts, family alert without phone number/contact/safe-word/callback values or contacts/calls/SMS.
     - official pressure/secrecy routes to official pressure, active contact exit, separate-channel verification, family alert/debrief, evidence/helper/help/report without official-number lookup, carrier/staff identity check, document/case validation, dial/browser/SMS/report submission.
     - live-call/app/link pressure routes to active contact exit, link/app quarantine, remote cutoff, account lockdown, exposure/privacy without call-state APIs, app/device/settings/package APIs, telephony/SIM APIs, phone validation, or remote disconnect/device cleaning claims.
     - financial/recovery contexts route to money pause, recovery-promise, credential freeze, account lockdown, evidence/helper/help/report without payment/banking/recovery/freeze/cancel/chargeback actions.
     - saved incident changes recommendations only by count category and does not read incident contents/previews.
     - low/in-progress readiness routes to safety plan, safe-word reset, readiness roadmap, safety drill, safe call scripts; ready readiness says readiness does not make a number/SIM/carrier/account/request/code safe/trusted/official.
     - every card has complete fields, stable order, unique route actions, and all action IDs map to existing flows.
     - share summary contains only generic pause/card/route/action labels and no raw input, redacted clue, plan literals, safe-word/callback/contact values, phone numbers, SMS/call/notification state, carrier/store/SIM/eSIM/USIM/IMSI/ICCID/IMEI/device serial/account/customer/porting PIN values, URLs/domains, long numbers, QR/barcode/code/package/app/file/media/URI/path values, account/card/bank/wallet/transaction/amount/cash serial/OTP/PIN/password/security answer/resident-like values, official numbers, directory values, agency/helper/lawyer/investigator/staff names, case/report/submission/receipt IDs, screenshot/image/OCR/audio/transcription values, clipboard/autofill/account/login state, recipient names, verification/result/status values, ported/reissued/activated/deactivated/blocked/unblocked/cancelled/recovered/frozen/changed/updated/saved/replaced/transferred/payment/refund/chargeback status, or legal/financial outcome values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no telephony/SIM/subscription/carrier APIs/no phone-number validation/no SMS/call/call-log/caller-ID/contacts/notifications/no official/carrier lookup/no report submission/no dial/call/SMS/no QR/barcode/OCR/camera/no file/photo/media/no attachment/no URI share/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no payment/banking/NFC/wallet/blockchain/refund/chargeback/no app/package/device/settings mutation/no IMEI/ICCID/IMSI/device-ID/serial reads/no device-admin/accessibility/VPN/usage-stats/no auto-send/no permissions/no scheduler.
   - Add/extend source-reading tests proving model and bounded UI slice avoid all forbidden APIs/values. Allow `openHelpDirectory(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)` only in the action router as a route to the existing official-help screen.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No TelephonyManager, SubscriptionManager, CarrierConfigManager, SmsManager, CallLog, ContactsContract, phone-number validation, SIM/eSIM/USIM/IMSI/ICCID/IMEI/device-serial/device-ID reads, carrier APIs, number-port/activation/deactivation/reissue checks, official/carrier lookup, emergency dispatch, report submission, legal/case lookup, caller-ID checking, document validation, or carrier-store lookup.
- No bank/ATM/cash/card/account/payment operation, refund/chargeback/recovery/freeze/cancel/trace, app/device/settings mutation, or financial/legal automation.
- No network/browser/webview/map/geocoder/navigation APIs and no `Intent.ACTION_VIEW`.
- No dialing/calling/hang-up/call-screening/call-blocking/call-state/call-log/SMS/send-to/recipient APIs.
- No contact/address-book reading/writing/editing/picking, notification listening/monitoring/filtering, clipboard/text watcher/autofill/credential APIs, account/auth/crypto APIs.
- No QR/barcode scan, OCR, camera, image analysis, screenshot, file/media picker, attachment, stream, URI share, upload, or evidence sync.
- No PackageManager/app inventory/app label/package APIs, package install/uninstall/settings/accessibility/device-admin/VPN/usage-stats/notification-listener APIs, app permission/grant/revoke APIs, malware scanning/antivirus APIs, or device-control behavior.
- No display/share/use of official directory phone numbers, carrier/store/agency/bank/branch/brand/staff/helper/lawyer/investigator names, exact phone/new-number/old-number/channel/profile/SMS/caller/SIM/eSIM/USIM/IMSI/ICCID/IMEI/serial/device/account/customer/porting PIN/carrier/link/domain/app/package/file/media/URI values, cash amounts/serials/banknotes, QR/barcode/code values, case/report/receipt IDs, literal OTP/PIN/password/security answers, account/card/bank/wallet/transaction/payment/refund/chargeback values, `SafetyPlan.safeWord`, callback script text, contact labels/numbers, raw analysis input, redacted clues, incident previews/bodies, device IDs, or any ported/reissued/activated/deactivated/blocked/unblocked/cancelled/recovered/frozen/changed/updated/saved/replaced/transferred/payment/refund/chargeback/recovery/freeze/cancel/block/trace status values.
- No mutation of safety plan, incident log, official directory, contacts, notification state, call/SMS state, telecom/SIM/subscription/carrier/number/account/device/app/package/permission/settings state, credential status, report/submission/receipt status, purchase/balance/redeem/refund/chargeback/recovery/freeze/cancel/block/trace status, or verification status.
- No alarms, reminders, CalendarContract, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only. It must not attach files/streams or include any official/contact/channel/profile/phone/SIM/carrier/account/payment/wallet/link/app/device/result value.
- Do not claim a phone number, SIM/eSIM/USIM, carrier, store, app, link, device, account, credential, code, contact, caller, SMS, case, report, payment, refund, recovery, block, freeze, port-out, reissue, activation, deactivation, cancellation, replacement, transfer, update, verification, or protection state is official, real, safe, secure, verified, valid, accepted, submitted, received, installed, uninstalled, disabled, cleaned, blocked, unblocked, removed, revoked, granted, scanned, inspected, checked, resolved, complete, recovered, refunded, frozen, protected, legal, trusted, ported, reissued, activated, deactivated, changed, updated, saved, replaced, transferred, malware-free, guaranteed, or problem-free.

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

Also inspect focused JUnit XML for `SimNumberPortOutPauseCoachTest`, `PrivacyCenterBuilderTest`, and `PrivacyPostureRegressionTest`.

Note: repo-wide scan may continue to show unchanged legacy official-help/report helper APIs (`OfficialHelpDirectoryBuilder`, `directory.entries`, `dialNumber`, `Intent.ACTION_DIAL`, `tel:`, `Uri.parse`) and explanatory privacy-copy mentions of OCR/audio/permissions. If so, add a bounded slice-specific source scan/test proving the new SIM/Number Port slice itself does not contain forbidden implementation APIs or values. `openHelpDirectory(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)` is allowed only in the UI action router.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-sim-number-port-out-pause-coach.md
git commit -m "feat: add sim port-out pause coach"
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
