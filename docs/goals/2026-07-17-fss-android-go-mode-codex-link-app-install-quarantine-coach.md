# FSS Android /go continuation — Link & App Install Quarantine Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `f0859ecb48b00141a9ee9b964a4e483c215b6492` — `feat: add gift card code pause coach`

Parent evaluator reconciled the Hermes background notification, confirmed the worker log had `CODEX_RC=0`, inspected git/log/source/test/docs, and independently reran verification.

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

Result: `BUILD SUCCESSFUL`; APK exists with `1502832` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 42 commits.

Focused JUnit XML evidence before this slice:

- `GiftCardCodePauseCoachTest`: debug 16/16, release 16/16, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 23/23, release 23/23, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 33/33, release 33/33, failures 0, errors 0, skipped 0.
- `RecoveryPromiseRealityCheckCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.

Parent manifest/source scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `RECORD_AUDIO`, `CAMERA`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device-admin, notification-listener, VPN, or package-install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Repo-wide scan has no network/browser/geocoder/webview APIs, no phone/SMS/contact/call-state APIs except existing legacy official-help dial route (`Intent.ACTION_DIAL`, `tel:`, `Uri.parse`, `dialNumber` in established official-help/report code), no clipboard/text-watcher/autofill/CredentialManager/AccountManager/biometric/keyguard/keystore/crypto APIs, no scheduler/notification service APIs, no stream/media/file/camera implementation APIs, no payment/NFC/device-control APIs, no blockchain/wallet/chargeback/refund implementation APIs. Repo-wide expected hits: existing `OfficialHelpDirectoryBuilder`/`directory.entries`/`dialNumber` in official-help/report files, existing legacy `Intent.ACTION_DIAL`/`tel:`/`Uri.parse` in `MainActivity.kt`, explanatory privacy-copy `OCR`/`RECORD_AUDIO` strings.
- Bounded Gift Card Code slice scan found no forbidden implementation APIs, no official/store/brand/directory values in model/share, no gift-card balance/redeem/purchase implementation, no scanner/camera/media/file APIs, no payment/bank/wallet/refund/chargeback/recovery APIs, no dial/SMS/network/browser/map/contact/notification/clipboard/autofill/credential/account/auth/crypto/device APIs, no incident/plan mutation, no raw incident/safety/gift-card/payment/status values, no unsafe claims. It has exactly one manual text share (`ACTION_SEND`, `text/plain`) and one allowed `openHelpDirectory(Screen.GIFT_CARD_CODE_PAUSE_COACH)` route.

Parent source inspection found no Gift Card privacy/routing issue: the slice uses only `currentAnalysis`, `FamilyReadinessBuilder.build(safetyPlan.load())`, and `incidents.count()`, shares only the builder summary, routes to existing flows, and keeps gift-card purchase/redeem/balance, barcode/QR/OCR/camera, official lookup/dial/report submission, payment/refund/chargeback, and app/device operations out of the builder and share text.

## Product intent for next cohesive slice

Add a **Link & App Install Quarantine Coach** screen/card for 스미싱 링크, 택배/청첩장/부고/벌금/환급 링크, APK/app-install pressure, and “keep the call/chat/screen-share open while you install or log in” flows.

Current app has safe-paste, remote-access cutoff, account lockdown, and separate-channel verification, but Korean phishing often begins with “just tap this link / install this security app / allow permission / enter OTP or card info in this page / keep the call open while I guide you.” The user needs a dedicated quarantine gate: **do not tap, open, preview, validate, scan, copy, share, install, grant permission, log in, enter OTP/PIN/password/card/account/gift-card values, or continue in the same call/chat/screen-share session under pressure; route to existing safe-paste, credential, remote, account, money, official-pressure, evidence, family/helper, and follow-up flows.**

Good user story:

> “A message or caller gives me a delivery/invitation/refund/fine/security link or tells me to install an app before helping me. Before I touch it, show me a local checklist that never opens the link or inspects the app.”

This is **not** a browser, URL/domain checker, QR/barcode scanner, APK analyzer, antivirus, malware removal, app inventory, permission manager, settings shortcut, package installer/uninstaller, device cleaner, VPN/accessibility/device-admin service, app-store lookup, official lookup, dialer, SMS sender, network service, report-submission tool, or credential/payment/gift-card checker. It must not open, fetch, preview, resolve, normalize, validate, reputation-check, scan, sandbox, install, uninstall, disable, force-stop, grant/revoke permissions, clean, block, or verify any link, URL, domain, package, APK, app, permission, device, login, account, payment, gift-card, report, or official state. It should only show deterministic value-free cards and route to existing flows.

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `LinkAppInstallQuarantineCoach.kt`:
   - `LinkAppInstallQuarantineCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): LinkAppInstallQuarantineCoach`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction or literal plan values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, score, action plan, summary, raw pasted text, redacted clues, incident bodies/previews, `IncidentPreviewRedactor`, persisted incident contents, contact labels/numbers, `SafetyPlan.safeWord`, callback script values, official directory entries/numbers, exact agency/store/brand/app names/numbers from directory, URLs/domains, shortened links, QR/barcode values, package names, APK/file/media names, app labels, permissions, installed-app inventory, device IDs, caller IDs, case IDs, report/submission/receipt status, account/card/bank/payment/wallet/transaction/refund/chargeback values, actual gift card/voucher/PIN/barcode/QR/serial/redeem code values, OTP/PIN/password/security answer values, resident IDs, clipboard contents, login/account/authentication state, call/SMS/contact/notification state, recipient values, helper/lawyer/investigator values, or verification/recontact/handoff/reset/recovery/result values.

2. Link/app quarantine cards:
   - Fixed manual cards, for example:
     - “상대가 준 링크를 열어 확인하지 않기”
     - “APK·보안앱·원격앱 설치와 권한 허용 멈추기”
     - “같은 통화·채팅·화면공유 세션에서 입력하지 않기”
     - “로그인·OTP·PIN·비밀번호·카드·상품권 코드를 링크 안에 넣지 않기”
     - “택배·청첩장·부고·벌금·환급 안내는 별도 경로로 분리하기”
     - “노출 가능성은 기존 계정·기기·증거·가족 경로로 값 없이 정리하기”
   - Each card should include:
     - id/display order/title
     - link/app-pressure scenario sentence to read manually
     - short value-free action sentence
     - why it matters
     - do-not-open/install/enter/scan/grant/share boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, `LINK`, `PARCEL_INVITATION_SMISHING`, app-install/live-call-control (`APP_INSTALL`, `LIVE_CALL_CONTROL`), credential/OTP signals (`CREDENTIALS`, `GIFTICON_PIN`, `OVERSEAS_PAYMENT_ALERT`), urgent transfer/financial/payment signals (`URGENT_TRANSFER`, `BAIT`, `GIFT_CRYPTO`, `SAFE_ACCOUNT_FRAUD`, `LOAN_REFINANCE_FEE`, `OVERSEAS_PAYMENT_ALERT`), impersonation/secrecy/official-pressure signals, recovery-promise contexts, family emergency/deepfake/phone-number-change, saved incident, and low/in-progress/ready readiness.
   - Default/no-analysis state should be conservative: show all/broad link/app quarantine cards and never claim a link, URL, domain, QR, barcode, APK, app, package, permission, device, login, account, card, bank action, wallet, transaction, payment, gift-card/code, caller, contact, document, case, report, official channel, family emergency, helper, or recovery state is official, verified, safe, secure, valid, accepted, submitted, received, installed, uninstalled, disabled, cleaned, blocked, removed, revoked, granted, scanned, inspected, checked, resolved, complete, protected, legal, problem-free, malware-free, trusted, or guaranteed.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `linkAppInstallQuarantineCoachScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard / readiness section.
     - Analysis result screen.
     - Quick Stop.
     - Safe Paste Privacy Coach.
     - Active Contact Exit Scripts.
     - Remote Access Cutoff Coach.
     - Credential Secret Freeze Coach.
     - Account & Device Lockdown Checklist.
     - Exposure Impact Triage.
     - Money Transfer Pause Gate.
     - Gift Card Code Pause Coach.
     - Recovery Promise Reality Check Coach.
     - Official Pressure Reality Check Coach.
     - Separate Channel Verification Coach.
     - Verification Outcome Next Steps Board.
     - Recontact Boundary Coach.
     - New Number Quarantine Coach.
     - Voice Impersonation Reality Check Coach.
     - Evidence Handoff Privacy Coach.
     - Evidence Kit / Incident Timeline if appropriate.
     - Official Help Directory and Official Report Prep Card.
     - Family Alert / Trusted Helper Review / Family Response Role Board / Calm Debrief / Follow-up Planner / Safety Flow Router / Privacy Center.
   - Action buttons must delegate to existing screens/flows only; do not duplicate URL checking, official lookup, dialing, browsing, report submission, legal advice, APK/app inspection, permission management, settings, app install/uninstall, malware removal, remote disconnect, device cleaning, credential reset, payment/banking, gift-card, evidence/media/file, or family/contact implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** add permissions or use network/web/browser/map/geocoder APIs, `Intent.ACTION_VIEW`, URL/domain/phone validation APIs, official directory values in the builder/share text, QR/barcode/OCR/image/camera/media/file APIs, PackageManager/app inventory/app-label/package APIs, package install/uninstall/settings intents, device-admin/accessibility/VPN/usage-stats/notification-listener APIs, app permission/grant/revoke APIs, malware scanning/antivirus APIs, dial/call/SMS/contact/notification APIs, clipboard/text watcher/autofill/credential APIs, account/auth/crypto APIs, URI grants, attachments, `Intent.EXTRA_STREAM`, NFC/payment/banking/card/wallet/blockchain/refund/chargeback APIs, alarms/scheduler/background workers, or persistence mutation APIs.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new coach is named as local/manual/no-permission/no-network/no-browser/no-url-domain-validation/no-QR-barcode-OCR-camera/no-file-media/no-app-install-uninstall/no-package-inventory/no-permission-manager/no-settings mutation/no malware scan/no app-store lookup/no official lookup/no caller-ID/phone validation/no report submission/no dial/no SMS/no contacts/calls/notifications/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no payment/banking/NFC/wallet/blockchain/refund/chargeback/gift-card action/no evidence attachment/no URI share/no auto-send/no scheduler/no device-admin/accessibility/VPN/usage-stats/no storage/no mutation.
   - Update `README.md` flow and add a section for the new feature.
   - Keep copy explicit that the feature routes to existing safe-paste, credential freeze, remote cutoff, account lockdown, exposure triage, money/gift-card/recovery/official-pressure, evidence, official help/report prep, family/helper, and follow-up screens but never opens, checks, installs, removes, cleans, blocks, validates, scans, submits, or proves link/app/device/account/payment states.

5. Tests:
   - Add `LinkAppInstallQuarantineCoachTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default coach is conservative, recommends all link/app quarantine cards, and does not claim link/URL/domain/QR/barcode/APK/app/package/permission/device/login/account/payment/gift-card/contact/caller/document/case/report/official/family/helper/recovery state is official/verified/safe/secure/valid/accepted/submitted/received/installed/uninstalled/disabled/cleaned/blocked/removed/revoked/granted/scanned/inspected/checked/resolved/complete/protected/legal/problem-free/malware-free/trusted/guaranteed.
     - `LINK` and `PARCEL_INVITATION_SMISHING` recommend safe paste/privacy, separate-channel verification, active contact exit, remote cutoff, exposure triage, evidence handoff, official pressure/help/report prep without URL/domain validation, browser/network behavior, QR/barcode scan, official-number lookup, or report submission.
     - `APP_INSTALL`/`LIVE_CALL_CONTROL` recommend active contact exit, remote cutoff, account lockdown, exposure triage, credential freeze, safe paste/privacy, trusted helper, evidence handoff without app inventory, package manager, permission grant/revoke, settings mutation, installation/uninstallation, malware removal, remote disconnect, device cleaning, accessibility/device-admin/VPN/usage-stats behavior.
     - credential/OTP and gift-card signals recommend credential secret freeze, gift-card pause, safe paste/privacy, account lockdown, remote cutoff, exposure triage without exposing OTP/PIN/password/account/card/bank/wallet/gift-card/link/app values.
     - money/recovery/payment signals recommend money pause, recovery promise reality check, official pressure check, evidence handoff, trusted helper, official help/report prep without payment cancellation/refund/chargeback/recovery, browser/network, wallet/blockchain, bank/payment APIs.
     - impersonation/secrecy/official-pressure style signals recommend official pressure reality check, separate-channel verification, active contact exit, family alert/debrief, trusted helper, evidence handoff, official help/report prep without official numbers/directory values, legal/case validation, dial/browser/SMS/report submission.
     - family emergency/deepfake/phone-number-change recommends separate-channel verification, family callback, voice reality check, new-number quarantine, safe-word reset, safe call scripts, family alert without exposing contact/safe-word/callback values or reading contacts/calls/SMS.
     - saved incident changes recommendations only by count category and does not read incident contents/previews.
     - low/in-progress readiness recommends safety plan, safe-word reset, readiness roadmap, safety drill, safe call scripts; ready readiness says readiness does not make a link/app/request safe/trusted.
     - every card has complete fields, stable order, unique route actions, and all action IDs map to existing flows.
     - share summary contains only generic link/app/card/route/action labels and no raw input, redacted clue, safety plan literal, safe-word/callback/contact values, phone numbers, URLs/domains, long numbers, QR/barcode/code/package/app/file/media/URI/path values, account/card/bank/wallet/transaction/OTP/PIN/password/security answer/resident-like values, device identifiers, official numbers, directory values, store/brand/agency/helper/lawyer names, case/report/submission/receipt IDs, screenshot/image/OCR/audio/transcription values, clipboard/autofill/account/login state, call/SMS/notification state, recipient names, verification/result/status values, installed/removed/permission/malware/link/app/device/payment/refund/chargeback/recovery/freeze/cancel/block/trace status, or legal/financial outcome values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no link opening/no URL/domain validation/no browser/network/no QR/barcode/OCR/camera/no file/media/no app install/uninstall/no package inventory/no permission manager/no settings mutation/no malware scan/no official lookup/no phone/caller/document validation/no report submission/no dial/call/SMS/no contacts/call-log/notifications/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no file/photo/media/camera/OCR/no attachment/no URI share/no auto-send/no permissions/no scheduler/no payment/banking/NFC/device control/no device-admin/accessibility/VPN/usage-stats.
     - copy avoids unsafe guarantees: no “100%”, no “safe link”, no “trusted app”, no “URL verified”, no “domain checked”, no “malware-free”, no “installed/removed/cleaned/blocked”, no “permission revoked/granted”, no “device protected”, no “app disabled”, no “report submitted/accepted”, no legal/financial result guarantee, no automatic monitoring/scanning/deletion/redaction/blocking/calling/messaging/lookup/verification/storage/upload/attachment/submission.
   - Add/extend source-reading tests proving the model does not import Android or touch network/browser/map/geocoder/URL validation/official lookup/contact/phone/SMS/call/notification/clipboard/autofill/credential/account/auth/crypto/media/payment/device APIs, QR/barcode/OCR/camera APIs, PackageManager/app inventory/app-label/package APIs, package install/uninstall/settings/accessibility/device-admin/VPN/usage-stats APIs, payment/refund/chargeback/wallet/gift-card APIs, `SafetyPlanStore`, `SafetyPlan`, `.safeWord`, `.callbackScript`, `.contacts`, `IncidentStore`, incident contents, `IncidentPreviewRedactor`, official directory entries, package/app results, or persistence mutation.
   - Add/extend UI source tests proving the new screen uses only `FamilyReadinessBuilder.build(safetyPlan.load())` to compute value-free readiness and passes only readiness category into the builder; it must not read/display/share plan literal values, incident bodies/previews, official numbers/directory entries, caller ID/call state, URLs/domains, clipboard text, autofill/account/login state, contact labels/numbers, account/card/bank/wallet/transaction/gift-card/code values, QR/barcode values, package/app values, file/media/URI values, permission/app install state, malware/cleaning status, or report/submission/receipt status. The route to existing screens is allowed; the new slice must not call `safetyPlan.save(`, `safetyPlan.clear()`, `incidents.add/clear/load`, `prefs.edit`, `OfficialHelpDirectoryBuilder` in builder/share text, `dial`, `openHelpDirectory` is allowed only as a route action from the action router, and no network/browser/dial/SMS/app/device/settings APIs.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No link/URL/domain opening, previewing, resolving, fetching, following, shortening/expanding, reputation checking, validation, sandboxing, scanning, or classification.
- No browser/web/network/map/geocoding APIs and no `Intent.ACTION_VIEW`.
- No QR/barcode scan, OCR, camera, image analysis, screenshot, file/media picker, attachment, stream, URI share, upload, or evidence sync.
- No APK/app/package inspection, app-store lookup, package inventory, app label/package-name reading, install/uninstall/disable/force-stop, permission grant/revoke, settings mutation, app-ops, malware scan/removal, device cleaning, remote disconnect, accessibility service, device admin, VPN, usage stats, notification listener, package installer, or MDM behavior.
- No official-number lookup, legal/case lookup, caller-ID checking, number/URL/domain/document validation, report submission, upload, cloud backup, or receipt checking.
- No dialing/calling/hang-up/call-screening/call-blocking/call-state/call-log/SMS/send-to/recipient APIs.
- No contact/address-book reading/writing/editing/picking, notification listening/monitoring/filtering, clipboard/text watcher/autofill/credential APIs, account/auth/crypto APIs.
- No payment/banking/card/wallet/blockchain/gift-card/refund/chargeback/recovery APIs, payment cancellation, refund/chargeback filing, wallet tracing, purchase/redeem/balance check, code/account/card freeze, or financial/legal automation.
- No display/share/use of official directory phone numbers, agency/store/brand/app entry values, exact links/domains, QR/barcode/code values, package/app/file/media/URI values, case/report/receipt IDs, literal OTP/PIN/password/security answers, account/card/bank/wallet/transaction/payment/refund/chargeback values, `SafetyPlan.safeWord`, callback script text, contact labels/numbers, raw analysis input, redacted clues, incident previews/bodies, device IDs, recipient values, helper/lawyer/investigator names, exact phone/channel/profile values, installed/removed/permission/malware/device/report/submission/status values.
- No mutation of safety plan, incident log, official directory, contacts, notification state, call/SMS state, link/app/package/permission/device state, account/card/bank/payment/wallet/gift-card state, credential status, report/submission/receipt status, purchase/balance/redeem/refund/chargeback/recovery/freeze/cancel/block/trace status, or verification status from the new coach.
- No alarms, reminders, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only. It must not attach files/streams or include any official/contact/channel/profile/account/payment/wallet/link/app/device/result value.
- Do not ask users to type exact names, contacts, phone numbers, account/card/bank/wallet/transaction values, gift-card/voucher/PIN/barcode/QR/serial/redeem codes, OTP/PIN, passwords, security answers, safe-word answers, callback script literals, URLs/domains, package/app names, file/media names, URI/path values, case/report/receipt IDs, resident IDs, device IDs, official numbers, store/brand/helper/lawyer/investigator names, audio clip names, transcripts, screenshots with credentials, call-log details, notification contents, recipient names, installed/removed/permission/malware/link/app/device/payment/refund/chargeback/recovery/freeze/cancel/block/trace status, or raw scam text into this coach.
- Do not claim a link, URL, domain, QR, barcode, APK, app, package, permission, device, person, institution, store, brand, helper, lawyer, investigator, title, caller ID, number, document, case, report, payment, account, card, bank action, wallet, transaction, credential, login, purchase, redemption, balance, chargeback, refund, freeze, trace, cancellation, recovery, app install/removal/permission, malware/device state is official, real, safe, secure, verified, valid, accepted, submitted, received, installed, uninstalled, disabled, cleaned, blocked, removed, revoked, granted, scanned, inspected, checked, resolved, complete, recovered, refunded, frozen, protected, legal, trusted, malware-free, guaranteed, or problem-free.

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
    'Intent.EXTRA_STREAM','ClipData','FLAG_GRANT_READ_URI_PERMISSION','FLAG_GRANT_WRITE_URI_PERMISSION','Uri.parse','FileProvider','MediaStore','ACTION_IMAGE_CAPTURE','ACTION_PICK','ACTION_GET_CONTENT','ACTION_OPEN_DOCUMENT','READ_MEDIA_IMAGES','READ_EXTERNAL_STORAGE','WRITE_EXTERNAL_STORAGE','CAMERA','MlKit','TextRecognition','OCR','ImageAnalysis','BitmapFactory','ExifInterface','Barcode','QRCode','BarcodeScanner','QRCodeReader','MLKit','ImageCapture',
    'PackageManager','getInstalledPackages','getInstalledApplications','PackageInstaller','deletePackage','ACTION_MANAGE_UNKNOWN_APP_SOURCES','ACTION_APPLICATION_DETAILS_SETTINGS','Settings.','DevicePolicyManager','AccessibilityService','VpnService','UsageStatsManager','NotificationListenerService','AccessibilityManager','AppOpsManager','ACTION_REQUEST_INSTALL_PACKAGE','ACTION_DELETE','ACTION_UNINSTALL_PACKAGE',
    'BillingClient','NfcAdapter','Blockchain','WalletConnect','web3','Bitcoin','Ethereum','Chargeback','RefundManager','GiftCardManager','BalanceChecker','RedemptionService','PaymentClient','BankingClient','WalletClient','Malware','Antivirus','VirusTotal'
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

Note: repo-wide scan may continue to show unchanged legacy official-help/report helper APIs (`OfficialHelpDirectoryBuilder`, `directory.entries`, `dialNumber`, `Intent.ACTION_DIAL`, `tel:`, `Uri.parse`) and explanatory privacy-copy mentions of OCR/audio/permissions. If so, add a bounded slice-specific source scan/test proving the new Link & App Install Quarantine slice itself does not contain network/browser/map/official lookup, URL/domain validation, QR/barcode/OCR/camera/media/file implementation, PackageManager/app inventory/package/install/uninstall/settings/accessibility/device-admin/VPN/usage-stats implementation, malware/antivirus/device-cleaning/remote-disconnect implementation, payment/banking/wallet/refund/chargeback/gift-card APIs, dial/SMS/contact/notification, clipboard/autofill/credential/account/auth/crypto, stream/URI share, scheduler/device, persistence mutation, plan mutation, raw/saved secret values, exact phone/channel/profile/account/payment/wallet/link/app/package/device values, official numbers, directory values, app/package values, attachments, report/submission/receipt/recovery/device/status values, or incident bodies/previews. `openHelpDirectory(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)` is allowed only in the UI action router as a route to an existing screen.

Also inspect focused JUnit XML for `LinkAppInstallQuarantineCoachTest`, `PrivacyCenterBuilderTest`, and `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-link-app-install-quarantine-coach.md
git commit -m "feat: add link app install quarantine coach"
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
