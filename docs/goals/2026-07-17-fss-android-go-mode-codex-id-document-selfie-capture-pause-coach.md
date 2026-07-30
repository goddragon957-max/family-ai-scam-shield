# FSS Android /go continuation — ID Document & Selfie Capture Pause Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `808f589b6634fa0703d81a62bbf82f205eae835f` — `feat: add sim port-out pause coach`

Parent evaluator reconciled the Hermes background notification: wrapper showed exit code 1/trailing `logout`, but the process log/final output had `CODEX_RC=0`, a local commit, and a DONE summary.

Parent-side evidence collected after the commit:

```bash
git status --short --branch --ignored=matching
# ## main...origin/main [ahead 48]
# !! .gradle/
# !! .tools/
# !! app/build/
# !! local.properties

git show --stat --oneline --summary HEAD
# 808f589 feat: add sim port-out pause coach
# 8 files changed, 3507 insertions(+), 40 deletions(-)
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

Result: `BUILD SUCCESSFUL`; APK exists with `1640725` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 48 commits.

Focused JUnit XML after the SIM/port-out slice:

- `SimNumberPortOutPauseCoachTest`: debug 18/18, release 18/18, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 29/29, release 29/29, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 39/39, release 39/39, failures 0, errors 0, skipped 0.

Manifest/source evidence:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `RECORD_AUDIO`, `CAMERA`, media/storage, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device-admin, notification-listener, VPN, or package-install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>`, no `<service>`, no `<receiver>`.
- Bounded SIM/port-out slice implementation scan after stripping string literals: `SLICE_CODE_FORBIDDEN_HITS=[]`.
- `SimNumberPortOutPauseCoach.kt` has no Android imports, no `SafetyPlan` reference, and no raw analysis field access (`input`, `score`, `actionPlan`, `summary`, redacted clue).
- UI uses `incidentCount = incidents.count()` and `FamilyReadinessBuilder.build(safetyPlan.load())`; no `incidents.load`, no persistence mutations.
- Share helper uses exactly manual `Intent.ACTION_SEND`, `text/plain`, `putExtra(Intent.EXTRA_TEXT, summary)`, and no `Intent.EXTRA_STREAM`.
- Expected repo-wide legacy hits remain only in established official-help/report paths (`Intent.ACTION_DIAL`, `Uri.parse`, `tel:`, `OfficialHelpDirectoryBuilder`, `directory.entries`, `dialNumber`) plus explanatory privacy-copy tokens (`OCR`, `RECORD_AUDIO`) and feature class/copy hits.

Parent source inspection found no obvious SIM/port-out privacy/routing issue: `SimNumberPortOutPauseCoachBuilder` uses only `analysis?.riskLevel`, `analysis?.matchedSignals`, `readiness.level`, and `incidentCount.coerceAtLeast(0)`; cards and UI show fixed value-free telecom/SIM/number-port pause scripts and route to existing flows; README/Privacy Center document no permissions/network/telephony/SIM/subscription/carrier/SMS/call/contact/notification/device-ID/official lookup/dial/browser/file/media/clipboard/credential/account/payment/device/scheduler/storage/mutation behavior.

## Product intent for next cohesive slice

Add an **ID Document & Selfie Capture Pause Coach** for the moment a scammer asks the user to photograph, scan, upload, screen-share, forward, or read details from an ID card/resident card/driver license/passport/health-insurance card/bankbook copy/document, or to take a selfie/video/selfie-with-ID/liveness/face-verification shot as part of “verification,” “loan,” “refund,” “account recovery,” “SIM/number protection,” “police/prosecutor/FSS/bank/platform procedure,” or “remote help.”

This slice should be a local, stateless, value-free coach that gives short “do not photograph/scan/upload/read ID or face data; stop the identity-capture flow; use existing safe routes” scripts. It must **not** use camera, OCR, barcode/QR scan, image analysis, face recognition/liveness, biometric APIs, file/media pickers, URI/stream shares, document validation, official lookup, network/browser, contact/SMS/call, account/auth/credential APIs, device identifiers, package/settings/device-control APIs, payment/banking/card actions, evidence attachment/upload, or persistent state.

Good user story:

> “Someone says I need to send my ID card, resident number, passport, license, bankbook copy, selfie, or video to verify, protect, recover, get a loan/refund, unlock an account, or stop a case. Before I take a photo or read a number, show me a short stop script and existing safe routes without reading my camera, gallery, files, ID number, face data, account, contact, link, or incident details.”

This complements credential secret freeze, safe paste privacy, account lockdown, official pressure, recovery promise, link/app quarantine, remote cutoff, SIM/port-out, exposure triage, evidence handoff privacy, and report prep. It is about **identity document / selfie capture pressure** while staying purely educational and manual.

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `IdDocumentSelfieCapturePauseCoach.kt`:
   - `IdDocumentSelfieCapturePauseCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): IdDocumentSelfieCapturePauseCoach`
   - Pure/stateless; no Android imports.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction or literal plan values
     - `incidentCount.coerceAtLeast(0)` as a boolean/count category only
   - Must not use/display/share raw analysis input, score, action plan, summary, redacted clue, incident bodies/previews, `IncidentPreviewRedactor`, saved incident contents, `SafetyPlan.safeWord`, callback script literals, contact labels/numbers, official directory entries/numbers, exact ID/resident/passport/license/document/card/bankbook/file/photo/video/selfie/face/liveness/biometric/gallery/camera/screenshot/OCR/barcode/QR/media/URI/path values, phone/SIM/eSIM/USIM/IMEI/ICCID/IMSI/device serial/account/customer values, phone numbers, URLs/domains, package/app values, account/card/bank/payment/wallet/transaction values, OTP/PIN/password/security answers, resident IDs, device IDs, SMS/call/caller-ID/contact/notification state, recipient/helper/lawyer/investigator/staff/courier/driver names, case/report/submission/receipt/status values, uploaded/submitted/accepted/verified/validated/sanitized/redacted/captured/scanned/analyzed/matched/liveness-passed/recovered/frozen/cancelled/refunded/chargeback outcomes, or legal/financial result values.

2. Pause/script cards:
   - Fixed manual cards, for example:
     - “신분증·주민번호·여권·면허증을 촬영/전송하기 전 멈추기”
     - “셀피·영상통화·얼굴 인증·신분증 들고 찍기 요구 거절하기”
     - “대출·환급·회복·계정/SIM 보호 약속과 신분 확인 요청 분리하기”
     - “링크·앱·원격지시 안에서 문서/얼굴/계정 정보를 입력하지 않기”
     - “민감값 없는 가족·보호자·계정/노출·증거 개인정보 경로 고르기”
     - “공식 도움·신고 준비·후속 점검 경로 고르기”
   - Each card should include:
     - id/display order/title
     - ID/selfie pressure situation sentence
     - short value-free pause/refusal script
     - why it matters
     - do-not-photograph/scan/upload/read-ID/face/account/say/share/attach/record boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, credential/OTP signals (`CREDENTIALS`, `GIFTICON_PIN`, `OVERSEAS_PAYMENT_ALERT`), official-pressure signals (`IMPERSONATION`, `SECRECY`, `SAFE_ACCOUNT_FRAUD`), link/app/remote/live-call pressure (`LIVE_CALL_CONTROL`, `APP_INSTALL`, `LINK`, `PARCEL_INVITATION_SMISHING`), financial/recovery contexts (`URGENT_TRANSFER`, `BAIT`, `GIFT_CRYPTO`, `SAFE_ACCOUNT_FRAUD`, `LOAN_REFINANCE_FEE`, `OVERSEAS_PAYMENT_ALERT`, `GIFTICON_PIN`), family/new-number/voice contexts (`PHONE_NUMBER_CHANGE`, `FAMILY_EMERGENCY`, `DEEPFAKE_VOICE`), saved incident count, and low/in-progress/ready readiness.
   - No-analysis/default should be conservative: show all pause cards and never claim an ID, document, selfie, face, liveness, barcode, QR, file, photo, video, account, phone, SIM, link, app, institution, staff, case, report, payment, refund, recovery, block, freeze, upload, submission, verification, validation, sanitization, redaction, capture, scan, analysis, match, protection, or legal/financial result is official, verified, safe, valid, accepted, submitted, received, protected, legal, resolved, complete, blocked, unblocked, cancelled, reversed, recovered, refunded, frozen, traced, uploaded, captured, scanned, analyzed, matched, sanitized, redacted, guaranteed, or problem-free.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `idDocumentSelfieCapturePauseCoachScreen()`.
   - Add route buttons from relevant screens:
     - Home dashboard/readiness section.
     - Analysis result.
     - Quick Stop.
     - Safe Paste Privacy Coach.
     - Credential Secret Freeze Coach.
     - Account & Device Lockdown Checklist.
     - Exposure Impact Triage.
     - Evidence Handoff Privacy Coach.
     - Official Pressure Reality Check Coach.
     - Recovery Promise Reality Check Coach.
     - Link & App Install Quarantine Coach.
     - Remote Access Cutoff Coach.
     - SIM / Number Port-Out Pause Coach.
     - Money Transfer Pause Gate / Gift Card Code Pause Coach where appropriate.
     - Separate Channel Verification Coach / New Number Quarantine / Voice Reality / Recontact Boundary / Verification Outcome.
     - Travel Route / Doorstep / Cash-Courier / Bank-ATM staff where appropriate.
     - Official Help Directory and Official Report Prep Card.
     - Family Alert / Trusted Helper Review / Family Response Role Board / Calm Debrief / Follow-up Planner / Safety Flow Router / Privacy Center.
   - Action buttons must delegate to existing screens/flows only; do not duplicate camera/OCR/file/media/evidence/photo/ID validation, liveness/face verification, document scanning, official lookup, dialing, browsing, report submission, legal advice, account reset, device settings, package/app inspection, payment/banking, contact/SMS/call behavior, or family/contact implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.

4. Privacy Center / README:
   - Update `PrivacyCenter.kt` inventory/copy so this new coach is named as local/manual/no-permission/no-network/no-camera/no-microphone/no-OCR/no-QR/barcode scan/no image analysis/no face recognition/no liveness/no biometric/no file/photo/video/media/gallery/screenshot/storage read/no file picker/no attachment/upload/no URI/stream share/no document/ID/passport/license/resident/bankbook validation/no official lookup/no report submission/no dial/no browser/webview/no SMS/call/call-log/caller-ID/contact/notification access/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no payment/banking/NFC/wallet/blockchain/refund/chargeback/gift-card action/no telephony/SIM/device-ID/IMEI/ICCID/IMSI reads/no app/package/device/settings mutation/no device-admin/accessibility/VPN/usage-stats/no auto-send/no scheduler/no storage/no mutation.
   - Update `README.md` flow and add a section for the new feature.

5. Tests:
   - Add `IdDocumentSelfieCapturePauseCoachTest.kt`.
   - Update privacy/source regression tests and Privacy Center tests.
   - Include tests proving:
     - default/no-analysis is conservative, recommends all pause cards/actions, and avoids unsafe official/verified/safe/completed/status claims.
     - credential/OTP signals route to credential freeze, safe paste/privacy, account lockdown, exposure triage, evidence privacy without exposing OTP/PIN/password/resident/account/card/bank/wallet/ID/document values.
     - official pressure/secrecy routes to official pressure, active contact exit, separate-channel verification, family alert/debrief, evidence/helper/help/report without official-number lookup, institution/staff identity check, document/case validation, dial/browser/SMS/report submission.
     - link/app/remote/live-call pressure routes to active contact exit, link/app quarantine, remote cutoff, account lockdown, exposure/privacy without call-state APIs, app/device/settings/package APIs, camera/OCR/file/media APIs, or remote disconnect/device cleaning claims.
     - financial/recovery contexts route to money pause, recovery-promise, credential freeze, account lockdown, evidence/helper/help/report without payment/banking/recovery/freeze/cancel/chargeback actions.
     - family/new-number/voice contexts route to new-number quarantine, separate-channel verification, family callback, voice reality, safe-word reset, safe call scripts, family alert without phone number/contact/safe-word/callback values or contacts/calls/SMS.
     - saved incident changes recommendations only by count category and does not read incident contents/previews.
     - low/in-progress readiness routes to safety plan, safe-word reset, readiness roadmap, safety drill, safe call scripts; ready readiness says readiness does not make an ID/selfie/document/account/request safe/trusted/official.
     - every card has complete fields, stable order, unique route actions, and all action IDs map to existing flows.
     - share summary contains only generic pause/card/route/action labels and no raw input, redacted clue, plan literals, safe-word/callback/contact values, phone numbers, SMS/call/notification state, ID/resident/passport/license/document/card/bankbook/photo/video/selfie/face/liveness/biometric/gallery/camera/screenshot/OCR/barcode/QR/media/URI/path values, carrier/SIM/IMEI/ICCID/IMSI/device serial/account/customer values, URLs/domains, long numbers, package/app/file values, account/card/bank/wallet/transaction/amount/cash serial/OTP/PIN/password/security answer/resident-like values, official numbers, directory values, agency/helper/lawyer/investigator/staff names, case/report/submission/receipt IDs, clipboard/autofill/account/login state, recipient names, verification/result/status values, uploaded/submitted/accepted/verified/validated/sanitized/redacted/captured/scanned/analyzed/matched/liveness-passed/recovered/frozen/cancelled/refunded/chargeback status, or legal/financial outcome values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no camera/microphone/OCR/QR/barcode/image-analysis/face-recognition/liveness/biometric/no file/photo/video/media/gallery/screenshot/storage reads/no file picker/no attachment/upload/no URI/stream share/no document/ID/passport/license/resident/bankbook validation/no official lookup/no report submission/no dial/call/SMS/no contacts/call-log/notifications/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no payment/banking/NFC/wallet/blockchain/refund/chargeback/no telephony/SIM/device-ID/IMEI/ICCID/IMSI reads/no app/package/device/settings mutation/no device-admin/accessibility/VPN/usage-stats/no auto-send/no permissions/no scheduler.
   - Add/extend source-reading tests proving model and bounded UI slice avoid all forbidden APIs/values. Allow `openHelpDirectory(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)` only in the action router as a route to the existing official-help screen.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No Camera/CameraX/Camera2, MediaRecorder, AudioRecord, microphone/audio, OCR/ML Kit/TextRecognition, barcode/QR scanning, image analysis, face recognition, face detection, liveness, biometric APIs, selfie/video capture, document scanning, ID/passport/license/resident/bankbook validation, file/media/gallery/screenshot/storage reads, file picker, URI/stream share, upload, evidence sync, or document redaction/sanitization claims.
- No TelephonyManager, SubscriptionManager, CarrierConfigManager, SmsManager, CallLog, ContactsContract, phone-number validation, SIM/eSIM/USIM/IMSI/ICCID/IMEI/device-serial/device-ID reads, carrier APIs, official lookup, emergency dispatch, report submission, legal/case lookup, caller-ID checking, document validation, or carrier-store lookup.
- No bank/ATM/cash/card/account/payment operation, refund/chargeback/recovery/freeze/cancel/trace, app/device/settings mutation, or financial/legal automation.
- No network/browser/webview/map/geocoder/navigation APIs and no `Intent.ACTION_VIEW`.
- No dialing/calling/hang-up/call-screening/call-blocking/call-state/call-log/SMS/send-to/recipient APIs.
- No contact/address-book reading/writing/editing/picking, notification listening/monitoring/filtering, clipboard/text watcher/autofill/credential APIs, account/auth/crypto APIs.
- No PackageManager/app inventory/app label/package APIs, package install/uninstall/settings/accessibility/device-admin/VPN/usage-stats/notification-listener APIs, app permission/grant/revoke APIs, malware scanning/antivirus APIs, or device-control behavior.
- No display/share/use of ID/resident/passport/license/document/card/bankbook/photo/video/selfie/face/liveness/biometric/gallery/camera/screenshot/OCR/barcode/QR/media/URI/path values, official directory phone numbers, carrier/store/agency/bank/branch/brand/staff/helper/lawyer/investigator names, exact phone/SIM/eSIM/USIM/IMSI/ICCID/IMEI/serial/device/account/customer/porting PIN/carrier/link/domain/app/package/file values, cash amounts/serials/banknotes, case/report/receipt IDs, literal OTP/PIN/password/security answers, account/card/bank/wallet/transaction/payment/refund/chargeback values, `SafetyPlan.safeWord`, callback script text, contact labels/numbers, raw analysis input, redacted clues, incident previews/bodies, device IDs, or any uploaded/submitted/accepted/verified/validated/sanitized/redacted/captured/scanned/analyzed/matched/liveness-passed/recovered/frozen/cancelled/refunded/chargeback/protection status values.
- No mutation of safety plan, incident log, official directory, contacts, notification state, call/SMS state, camera/media/gallery/file/evidence/document/ID/biometric/face/liveness/OCR/barcode state, telecom/SIM/subscription/carrier/number/account/device/app/package/permission/settings state, credential status, report/submission/receipt status, purchase/balance/redeem/refund/chargeback/recovery/freeze/cancel/block/trace status, or verification status.
- No alarms, reminders, CalendarContract, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only. It must not attach files/streams or include any official/contact/channel/profile/phone/SIM/carrier/account/payment/wallet/link/app/device/ID/document/face/media/result value.
- Do not claim an ID, document, selfie, face, liveness, barcode, QR, file, photo, video, account, phone, SIM, link, app, institution, staff, case, report, payment, refund, recovery, block, freeze, upload, submission, verification, validation, sanitization, redaction, capture, scan, analysis, match, protection, or legal/financial result is official, real, safe, secure, verified, valid, accepted, submitted, received, installed, uninstalled, disabled, cleaned, blocked, unblocked, removed, revoked, granted, scanned, inspected, checked, resolved, complete, recovered, refunded, frozen, protected, legal, trusted, uploaded, captured, scanned, analyzed, matched, sanitized, redacted, malware-free, guaranteed, or problem-free.

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

Also inspect focused JUnit XML for `IdDocumentSelfieCapturePauseCoachTest`, `PrivacyCenterBuilderTest`, and `PrivacyPostureRegressionTest`.

Note: repo-wide scan may continue to show unchanged legacy official-help/report helper APIs (`OfficialHelpDirectoryBuilder`, `directory.entries`, `dialNumber`, `Intent.ACTION_DIAL`, `tel:`, `Uri.parse`) and explanatory privacy-copy mentions of OCR/audio/permissions. If so, add a bounded slice-specific source scan/test proving the new ID/Selfie slice itself does not contain the forbidden implementation APIs or values. `openHelpDirectory(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)` is allowed only in the UI action router.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-id-document-selfie-capture-pause-coach.md
git commit -m "feat: add ID document selfie pause coach"
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
