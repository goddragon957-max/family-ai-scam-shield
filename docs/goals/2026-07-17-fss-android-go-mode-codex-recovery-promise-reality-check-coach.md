# FSS Android /go continuation — Recovery Promise Reality Check Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `f841e60315db1842e68fb7f929c0a82449de6203` — `feat: add official pressure reality check coach`

Parent evaluator reconciled the Hermes background notification, confirmed the worker log had `CODEX_RC=0`, and independently reran verification.

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

Result: `BUILD SUCCESSFUL`; APK exists with `1459650` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 40 commits.

Focused JUnit XML evidence before this slice:

- `OfficialPressureRealityCheckCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 21/21, release 21/21, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 31/31, release 31/31, failures 0, errors 0, skipped 0.
- `CredentialSecretFreezeCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.

Parent manifest/source scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `RECORD_AUDIO`, `CAMERA`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device-admin, notification-listener, VPN, or package-install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Repo-wide scan has no network/browser/geocoder/webview APIs, no phone/SMS/contact/call-state APIs except existing legacy official-help dial route (`Intent.ACTION_DIAL`, `tel:`, `Uri.parse`, `dialNumber` in established official-help/report code), no clipboard/text-watcher/autofill/CredentialManager/AccountManager/biometric/keyguard/keystore/crypto APIs, no scheduler/notification service APIs, no stream/media/file/camera APIs, no payment/NFC/device-control APIs. Repo-wide expected hits: existing `OfficialHelpDirectoryBuilder`/`directory.entries`/`dialNumber` in official-help/report files, existing legacy `Intent.ACTION_DIAL`/`tel:`/`Uri.parse` in `MainActivity.kt`, and explanatory privacy-copy `OCR`/`RECORD_AUDIO` strings.
- Bounded Official Pressure slice scan found no forbidden implementation APIs, no official directory values in builder/share text, no dial/SMS/network/browser/map/contact/notification/clipboard/autofill/credential/account/auth/crypto/media/payment/device APIs, no incident/plan mutation, no raw incident/safety/official values, no unsafe official/verified/legal/complete claims. It has exactly one manual text share (`ACTION_SEND`, `text/plain`) and one allowed `openHelpDirectory(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)` route.

Parent source inspection found no Official Pressure privacy/routing issue: the slice uses only `currentAnalysis`, `FamilyReadinessBuilder.build(safetyPlan.load())`, and `incidents.count()`, shares only the builder summary, routes to existing flows, and keeps official lookup/dial/report submission out of the builder and share text.

## Product intent for next cohesive slice

Add a **Recovery Promise Reality Check Coach** screen/card for 2차 피해 / recovery-scam prevention.

After a phishing scare or loss, scammers often return as “refund team”, “bank recovery”, “crypto recovery”, “lawyer”, “police investigator”, “chargeback helper”, “platform support”, or “case handler” and promise to recover money, cancel payment, freeze accounts, trace wallets, submit reports, or unlock compensation — but only if the user pays a fee, shares OTP/PIN/password/account/card/wallet details, installs an app, screen-shares, or keeps it secret. The app already has money pause, credential freeze, official pressure, evidence handoff, official help/report prep, and follow-up flows. This slice should give users a dedicated **do not pay/share/install for promised recovery** gate that routes to existing safe flows without performing any recovery, refund, chargeback, crypto tracing, legal filing, official lookup, or device/account action.

Good user story:

> “Someone says they can get my money back or cancel/freeze something if I pay a fee or give codes. Before I respond, show me a safe local checklist that doesn’t read my bank/wallet/case details.”

This is **not** a refund/chargeback/case submission tool, legal service, investigator, crypto tracing wallet, bank/payment integration, account freeze tool, debt/loan service, proof-of-loss validator, official lookup, browser, dialer, SMS sender, network service, or document/evidence uploader. It must not collect, fetch, validate, submit, upload, charge, refund, reverse, recover, freeze, unlock, trace, cancel, block, guarantee, or verify anything. It should only show deterministic value-free cards and route to existing flows.

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `RecoveryPromiseRealityCheckCoach.kt`:
   - `RecoveryPromiseRealityCheckCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): RecoveryPromiseRealityCheckCoach`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction or literal plan values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, score, action plan, summary, raw pasted text, redacted clues, incident bodies/previews, `IncidentPreviewRedactor`, persisted incident contents, contact labels/numbers, `SafetyPlan.safeWord`, callback script values, official directory entries/numbers, exact agency names/numbers from directory, phone numbers, URLs/domains, case IDs, report/submission/receipt status, account/card/bank/payment/wallet/transaction/refund/chargeback values, OTP/PIN/password/security answer values, resident IDs, device IDs, app/package names, clipboard contents, login/account/authentication state, call/SMS/contact/notification state, recipient values, helper/lawyer/investigator values, or verification/recontact/handoff/reset/recovery result values.

2. Recovery-promise cards:
   - Fixed manual cards, for example:
     - “회복·환급·취소 약속을 결과로 믿지 않기”
     - “수수료·세금·보증금·상품권을 먼저 내지 않기”
     - “OTP·PIN·비밀번호·계정·카드·지갑 정보를 회복 조건으로 주지 않기”
     - “앱 설치·원격조작·화면공유로 회복을 돕겠다는 흐름 끊기”
     - “변호사·수사관·은행·플랫폼 직함은 기존 공식 압박 점검으로 분리하기”
     - “증거·신고·후속 점검은 값 없이 기존 화면에서만 정리하기”
   - Each card should include:
     - id/display order/title
     - recovery-promise scenario sentence to read manually
     - short value-free action sentence
     - why it matters
     - do-not-pay/share/submit/install/call boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, urgent transfer/financial/payment signals (`URGENT_TRANSFER`, `BAIT`, `GIFT_CRYPTO`, `SAFE_ACCOUNT_FRAUD`, `LOAN_REFINANCE_FEE`, `GIFTICON_PIN`, `OVERSEAS_PAYMENT_ALERT`), credential/OTP signals, impersonation/secrecy/official-pressure signals, link/smishing, app-install/live-call-control, family emergency/deepfake/phone-number-change, saved incident, and low/in-progress/ready readiness.
   - Default/no-analysis state should be conservative: show all/broad recovery-promise cards and never claim a payment, account, card, bank, wallet, transaction, case, report, recovery, chargeback, refund, cancellation, freeze, block, investigation, agency, helper, lawyer, institution, caller, link, document, device, or credential state is official, verified, safe, secure, valid, accepted, submitted, received, blocked, cancelled, reversed, recovered, refunded, frozen, protected, legal, complete, resolved, traceable, guaranteed, or problem-free.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `recoveryPromiseRealityCheckCoachScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard / readiness section.
     - Analysis result screen.
     - Quick Stop.
     - Money Transfer Pause Gate.
     - Credential Secret Freeze Coach.
     - Official Pressure Reality Check Coach.
     - Account & Device Lockdown Checklist.
     - Remote Access Cutoff Coach.
     - Exposure Impact Triage.
     - Evidence Handoff Privacy Coach.
     - Evidence Kit / Incident Timeline if appropriate.
     - Official Help Directory and Official Report Prep Card.
     - Active Contact Exit Scripts.
     - Safe Paste Privacy Coach.
     - Separate Channel Verification Coach.
     - Verification Outcome Next Steps Board.
     - Recontact Boundary Coach.
     - New Number Quarantine Coach.
     - Voice Impersonation Reality Check Coach.
     - Family Alert / Trusted Helper Review / Family Response Role Board / Calm Debrief / Follow-up Planner / Safety Flow Router / Privacy Center.
   - Action buttons must delegate to existing screens/flows only; do not duplicate official lookup, dialing, browsing, report submission, legal advice, refund/chargeback, bank/payment/account/device/evidence/family implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** add permissions or use network/web/browser/map/geocoder APIs, official directory values in the builder/share text, phone-number/URL validation, wallet/blockchain/exchange APIs, payment/banking/card APIs, `Intent.ACTION_VIEW`, `Intent.ACTION_DIAL`, `Intent.ACTION_CALL`, `ACTION_SENDTO`, `tel:`, `smsto:`, contacts/call/SMS/notification APIs, clipboard/text watcher/autofill/credential APIs, account/auth/crypto APIs, file/media/camera/OCR, URI grants, attachments, `Intent.EXTRA_STREAM`, NFC, device-control, alarm/scheduler/background-worker, or persistence mutation APIs.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new coach is named as local/manual/no-permission/no-network/no-official-lookup/no-recovery/refund/chargeback/case/legal/payment/bank/wallet/blockchain/crypto tracing/no-number-validation/no-URL-validation/no-caller-ID-check/no-document-validation/no-report-submission/no-dial/no-SMS/no-browser/no-map/no contacts/calls/notifications/no clipboard/autofill/credential APIs/no media/file/camera/OCR/no attachment/no URI share/no auto-send/no scheduler/no payment/banking/NFC/device control/no storage/no mutation.
   - Update `README.md` flow and add a section for the new feature.
   - Keep copy explicit that the feature routes to existing money, credential, official pressure, evidence, official help/report prep, account/remote/exposure, family/helper, and follow-up screens but never proves or performs recovery/refund/chargeback/cancellation/freeze/tracing/report/case/legal/financial results.

5. Tests:
   - Add `RecoveryPromiseRealityCheckCoachTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default coach is conservative, recommends all recovery-promise cards, and does not claim payment/account/card/bank/wallet/transaction/case/report/recovery/chargeback/refund/cancellation/freeze/block/investigation/agency/helper/lawyer/institution/caller/link/document/device/credential state is official/verified/safe/secure/valid/accepted/submitted/received/blocked/cancelled/reversed/recovered/refunded/frozen/protected/legal/complete/resolved/traceable/guaranteed/problem-free.
     - financial/recovery-bait signals (`URGENT_TRANSFER`, `BAIT`, `GIFT_CRYPTO`, `SAFE_ACCOUNT_FRAUD`, `LOAN_REFINANCE_FEE`, `GIFTICON_PIN`, `OVERSEAS_PAYMENT_ALERT`) recommend money pause, credential freeze, official pressure check, account lockdown, exposure triage, evidence handoff, trusted helper, official help/report prep without claiming payment cancellation, account/card freeze, refund, chargeback, crypto tracing, or recovery.
     - credential/OTP signals recommend credential secret freeze, safe paste/privacy, account lockdown, remote cutoff, exposure triage without exposing OTP/PIN/password/account/card/bank/wallet values.
     - impersonation/secrecy/official-pressure style signals recommend official pressure reality check, active contact exit, family alert/debrief, trusted helper, evidence handoff/privacy, official help/report prep without official numbers/directory values or legal/case validation.
     - link/smishing/app-install/live-call-control recommends not continuing in the same link/app/call/screen-share session, remote cutoff/exposure/privacy routes, and no URL validation, link scan, app inventory, malware removal, remote disconnect, device cleaning, wallet tracing, or browser/network behavior.
     - family emergency/deepfake/phone-number-change recommends separate-channel verification, family callback, voice reality check, new-number quarantine, safe-word reset, safe call scripts, family alert without exposing contact/safe-word/callback values.
     - saved incident changes recommendations only by count category and does not read incident contents/previews.
     - low/in-progress readiness recommends safety plan, safe-word reset, readiness roadmap, safety drill, safe call scripts; ready readiness says readiness does not make a recovery/helper/institution/request real or safe.
     - every card has complete fields, stable order, unique route actions, and all action IDs map to existing flows.
     - share summary contains only generic recovery-promise/card/route/action labels and no raw input, redacted clue, safety plan literal, safe-word/callback/contact values, phone numbers, URLs/domains, long numbers, package/app names, account/card/bank/wallet/transaction/OTP/PIN/password/security answer/resident-like values, device identifiers, official numbers, directory values, agency/helper/lawyer names, case/report/submission/receipt IDs, file/media names, URI/path values, screenshot/image/OCR/audio/transcription values, clipboard/autofill/account/login state, call/SMS/notification state, recipient names, verification/result/status values, refund/chargeback/recovery/freeze/cancel/block/trace status, or legal/financial outcome values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no recovery/refund/chargeback/legal/case/wallet/crypto-tracing/payment/bank action/no official lookup/no phone/URL validation/no caller-ID/document validation/no report submission/no dial/call/SMS/browser/map/network/no contacts/call-log/notifications/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no file/photo/media/camera/OCR/no attachment/no URI share/no auto-send/no permissions/no scheduler/no payment/banking/NFC/device control.
     - copy avoids unsafe guarantees: no “100%”, no “verified recovery”, no “refund guaranteed”, no “chargeback accepted”, no “case accepted”, no “wallet traced”, no “payment cancelled”, no “account frozen/protected”, no “recovered”, no “refunded”, no “cancelled”, no “blocked”, no “resolved”, no “complete”, no legal/financial result guarantee, no automatic monitoring/scanning/OCR/deletion/redaction/blocking/calling/messaging/lookup/verification/storage/upload/attachment/submission.
   - Add/extend source-reading tests that prove the model does not import Android or touch network/browser/map/geocoder/official lookup/contact/phone/SMS/call/notification/clipboard/autofill/credential/account/auth/crypto/media/payment/device APIs, `SafetyPlanStore`, `SafetyPlan`, `.safeWord`, `.callbackScript`, `.contacts`, `IncidentStore`, incident contents, `IncidentPreviewRedactor`, official directory entries, package manager results, wallet/blockchain/payment/refund/chargeback/case APIs, or persistence mutation.
   - Add/extend UI source tests proving the new screen uses only `FamilyReadinessBuilder.build(safetyPlan.load())` to compute value-free readiness and passes only readiness category into the builder; it must not read/display/share plan literal values, incident bodies/previews, official numbers/directory entries, caller ID/call state, URLs/domains, clipboard text, autofill/account/login state, contact labels/numbers, account/card/bank/wallet/transaction values, or package/app values. The route to existing screens is allowed; the new slice must not call `safetyPlan.save(`, `safetyPlan.clear()`, `incidents.add/clear/load`, `prefs.edit`, `OfficialHelpDirectoryBuilder` in builder/share text, `dial`, `openHelpDirectory` is allowed only as a route action from the action router, and no network/browser/dial/SMS APIs.
   - Add/extend source scan for recovery/payment/wallet/official/network/phone APIs and existing privacy APIs. Explicitly forbid in the new slice and repo-wide if practical: `HttpURLConnection`, `HttpsURLConnection`, `java.net.URL`, `OkHttp`, `Retrofit`, `ktor.client`, `WebView`, `Intent.ACTION_VIEW`, `Geocoder`, `Patterns.WEB_URL`, `URLUtil`, `InetAddress`, `PhoneNumberUtils`, `Patterns.PHONE`, `OfficialHelpDirectoryBuilder` in the model/share text, `directory.entries`, `dialNumber`, `Intent.ACTION_DIAL` in the new slice, `Intent.ACTION_CALL`, `ACTION_SENDTO`, `tel:`, `smsto:`, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, `PhoneStateListener`, `CallLog`, `SmsManager`, `ContactsContract`, notification listener/manager APIs, `ClipboardManager`, `TextWatcher`, `AutofillManager`, `CredentialManager`, `AccountManager`, `BiometricPrompt`, `KeyguardManager`, `KeyStore`, `javax.crypto`, `MessageDigest`, media/file/photo/camera/OCR APIs, `Intent.EXTRA_STREAM`, `ClipData`, URI grants, `Uri.parse`, `FileProvider`, `MediaStore`, payment/banking/card/wallet/blockchain/exchange/refund/chargeback APIs (if any names appear, they must be explanatory copy only and bounded tests should prove no implementation API), `BillingClient`, `NfcAdapter`, device-admin/accessibility/VPN/settings/package install APIs, scheduler APIs, `prefs.edit`, `putString`, `putBoolean`, `.save(` in the new slice, and any new `<uses-permission>`.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No refund, chargeback, wallet, crypto tracing, recovery, official-number, legal-case, bank/account/card/payment, caller-ID, phone-number, URL/domain, profile, institution, document, report, identity, credential, code, device, or recovery-status validation/lookup.
- No network calls, web browsing, browser intents, maps/geocoding, official-number lookup, report submission, upload, cloud backup, evidence sync, blockchain/exchange/wallet lookup, or payment processing.
- No dialing/calling/hang-up/call-screening/call-blocking/call-state/call-log/SMS/send-to/recipient APIs.
- No contact/address-book reading/writing/editing/picking, notification listening/monitoring/filtering, clipboard/text watcher/autofill/credential APIs, account/auth/crypto APIs, media/file/photo/storage/camera/screenshot/OCR/image analysis APIs, attachments, URI sharing, streams, file/document/photo/audio pickers, MediaStore, FileProvider, or image/audio capture.
- No display/share/use of official directory phone numbers, agency entry values, exact links/domains, case/report/receipt IDs, literal OTP/PIN/password/security answers, account/card/bank/wallet/transaction/payment/refund/chargeback values, `SafetyPlan.safeWord`, callback script text, contact labels/numbers, raw analysis input, redacted clues, incident previews/bodies, package/app names, device IDs, recipient values, helper/lawyer/investigator names, or exact phone/channel/profile values.
- No mutation of safety plan, incident log, official directory, contacts, notification state, call/SMS state, account/card/bank/payment/wallet state, device/app/settings state, credential status, report/submission/receipt status, chargeback/refund/recovery/freeze/cancel/block/trace status, or verification status from the new coach.
- No alarms, reminders, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No payment APIs, banking APIs, NFC, account-manager integration, payment cancellation, chargeback filing, refund claim, account/card freeze, wallet tracing, blockchain lookup, debt/loan service, or recovery automation.
- No accessibility service, device admin, VPN, usage stats, package/app inventory scanning, app install/uninstall, settings mutation, app ops, or MDM behavior.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only. It must not attach files/streams or include any official/contact/channel/profile/account/payment/wallet/result value.
- Do not ask users to type exact names, contacts, phone numbers, account/card/bank/wallet/transaction values, OTP/PIN, passwords, security answers, safe-word answers, callback script literals, URLs/domains, case/report/receipt IDs, resident IDs, device IDs, installed app/package names, official numbers, lawyer/helper/investigator names, audio clip names, transcripts, screenshots with credentials, file/media names, URI/path values, call-log details, notification contents, recipient names, refund/chargeback/recovery/freeze/cancel/block/trace status, or raw scam text into this coach.
- Do not claim a person, institution, helper, lawyer, investigator, title, caller ID, number, link, document, case, report, payment, account, card, bank action, wallet, transaction, credential, login, device, chargeback, refund, freeze, trace, cancellation, or recovery state is official, real, safe, secure, verified, valid, accepted, submitted, received, blocked, cancelled, reversed, resolved, complete, recovered, refunded, frozen, protected, legal, traceable, guaranteed, problem-free, or automatically protected.

## Verification gate before commit

Run from repo root:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
python3 - <<'PY'
from pathlib import Path
manifest = Path('app/src/main/AndroidManifest.xml').read_text()
for token in ['CALL_PHONE','READ_SMS','SEND_SMS','RECEIVE_SMS','READ_CALL_LOG','READ_CONTACTS','WRITE_CONTACTS','READ_PHONE_STATE','ANSWER_PHONE_CALLS','RECORD_AUDIO','CAMERA','READ_MEDIA_IMAGES','READ_MEDIA_VIDEO','READ_EXTERNAL_STORAGE','WRITE_EXTERNAL_STORAGE','INTERNET','ACCESS_FINE_LOCATION','ACCESS_COARSE_LOCATION','POST_NOTIFICATIONS','SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED','PACKAGE_USAGE_STATS','QUERY_ALL_PACKAGES','BIND_ACCESSIBILITY_SERVICE','BIND_DEVICE_ADMIN','BIND_NOTIFICATION_LISTENER_SERVICE','BIND_VPN_SERVICE','REQUEST_INSTALL_PACKAGES']:
    print(f'{token}={token in manifest}')
needle='android:allowBackup="false"'
print(f'allowBackup=false={needle in manifest}')
print(f'uses-permission-tag={"<uses-permission" in manifest}')
needles = [
    'HttpURLConnection','HttpsURLConnection','java.net.URL','OkHttp','Retrofit','ktor.client','WebView','Intent.ACTION_VIEW','Geocoder','Patterns.WEB_URL','URLUtil','InetAddress','PhoneNumberUtils','Patterns.PHONE','OfficialHelpDirectoryBuilder','directory.entries','dialNumber',
    'Intent.ACTION_CALL','Intent.ACTION_DIAL','ACTION_SENDTO','tel:','smsto:','TelecomManager','TelephonyManager','CallScreeningService','PhoneStateListener','CallLog','SmsManager','ContactsContract',
    'ClipboardManager','getPrimaryClip','setPrimaryClip','TextWatcher','addTextChangedListener','AutofillManager','CredentialManager','android.credentials','AccountManager','BiometricPrompt','KeyguardManager','KeyStore','javax.crypto','MessageDigest',
    'MediaRecorder','AudioRecord','SpeechRecognizer','RecognizerIntent','TextToSpeech','AudioManager','MediaPlayer','SoundPool','android.media','android.speech','RECORD_AUDIO',
    'NotificationManager','NotificationCompat','NotificationListenerService','AlarmManager','WorkManager','JobScheduler','CalendarContract','ForegroundService','BroadcastReceiver','POST_NOTIFICATIONS','SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED',
    'Intent.EXTRA_STREAM','ClipData','FLAG_GRANT_READ_URI_PERMISSION','FLAG_GRANT_WRITE_URI_PERMISSION','Uri.parse','FileProvider','MediaStore','ACTION_IMAGE_CAPTURE','ACTION_PICK','ACTION_GET_CONTENT','ACTION_OPEN_DOCUMENT','READ_MEDIA_IMAGES','READ_EXTERNAL_STORAGE','WRITE_EXTERNAL_STORAGE','CAMERA','MlKit','TextRecognition','OCR','ImageAnalysis','BitmapFactory','ExifInterface',
    'BillingClient','NfcAdapter','DevicePolicyManager','AccessibilityService','VpnService','UsageStatsManager','PackageInstaller','deletePackage','ACTION_MANAGE_UNKNOWN_APP_SOURCES','ACTION_APPLICATION_DETAILS_SETTINGS','Settings.','getInstalledPackages','getInstalledApplications'
]
paths = list(Path('app/src/main').rglob('*.kt')) + [Path('app/src/main/AndroidManifest.xml')]
for needle in needles:
    hits = [str(p) for p in paths if needle in p.read_text(errors='ignore')]
    print(f'{needle}={hits}')
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

Note: repo-wide scan may continue to show unchanged legacy official-help/report helper APIs (`OfficialHelpDirectoryBuilder`, `directory.entries`, `dialNumber`, `Intent.ACTION_DIAL`, `tel:`, `Uri.parse`) and explanatory privacy-copy mentions of OCR/audio/permissions. If so, add a bounded slice-specific source scan/test proving the new Recovery Promise Reality Check slice itself does not contain network/browser/map/official lookup, refund/chargeback/wallet/payment/bank APIs, dial/SMS/contact/notification, clipboard/autofill/credential/account/auth/crypto, media/file/photo/camera/OCR implementation APIs, stream/URI share, payment/device, persistence mutation, plan mutation, raw/saved secret values, exact phone/channel/profile/account/payment/wallet values, official numbers, directory values, app/package values, attachments, report/submission/receipt/recovery status, or incident bodies/previews. `openHelpDirectory(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)` is allowed only in the UI action router as a route to an existing screen.

Also inspect focused JUnit XML for `RecoveryPromiseRealityCheckCoachTest`, `PrivacyCenterBuilderTest`, and `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-recovery-promise-reality-check-coach.md
git commit -m "feat: add recovery promise reality check coach"
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
