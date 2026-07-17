# FSS Android /go continuation — Gift Card Code Pause Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `623327e041319d756826959b92d0dbc2a1bb2966` — `feat: add recovery promise reality check coach`

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

Result: `BUILD SUCCESSFUL`; APK exists with `1482625` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 41 commits.

Focused JUnit XML evidence before this slice:

- `RecoveryPromiseRealityCheckCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 22/22, release 22/22, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 32/32, release 32/32, failures 0, errors 0, skipped 0.
- `OfficialPressureRealityCheckCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.

Parent manifest/source scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `RECORD_AUDIO`, `CAMERA`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device-admin, notification-listener, VPN, or package-install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Repo-wide scan has no network/browser/geocoder/webview APIs, no phone/SMS/contact/call-state APIs except existing legacy official-help dial route (`Intent.ACTION_DIAL`, `tel:`, `Uri.parse`, `dialNumber` in established official-help/report code), no clipboard/text-watcher/autofill/CredentialManager/AccountManager/biometric/keyguard/keystore/crypto APIs, no scheduler/notification service APIs, no stream/media/file/camera implementation APIs, no payment/NFC/device-control APIs, no blockchain/wallet/chargeback/refund implementation APIs. Repo-wide expected hits: existing `OfficialHelpDirectoryBuilder`/`directory.entries`/`dialNumber` in official-help/report files, existing legacy `Intent.ACTION_DIAL`/`tel:`/`Uri.parse` in `MainActivity.kt`, and explanatory privacy-copy `OCR`/`RECORD_AUDIO` strings.
- Bounded Recovery Promise slice scan found no forbidden implementation APIs, no official directory values in builder/share text, no payment/bank/wallet/refund/chargeback/recovery APIs, no dial/SMS/network/browser/map/contact/notification/clipboard/autofill/credential/account/auth/crypto/media/device APIs, no incident/plan mutation, no raw incident/safety/official/payment/wallet/recovery values, no unsafe refund/recovery/legal/guarantee claims. It has exactly one manual text share (`ACTION_SEND`, `text/plain`) and one allowed `openHelpDirectory(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)` route.

Parent source inspection found no Recovery Promise privacy/routing issue: the slice uses only `currentAnalysis`, `FamilyReadinessBuilder.build(safetyPlan.load())`, and `incidents.count()`, shares only the builder summary, routes to existing flows, and keeps recovery/refund/chargeback/wallet/official lookup/dial/report submission out of the builder and share text.

## Product intent for next cohesive slice

Add a **Gift Card Code Pause Coach** screen/card for 상품권·기프티콘·선불카드·문화상품권·Google Play/App Store/Steam-style code scams.

Current app has broad money pause and credential freeze flows, but Korean phishing often shifts the victim into “buy gift cards / send the PIN / photograph the barcode / read the voucher code / forward the e-gifticon / split purchases across stores” because these are irreversible and feel less like a bank transfer. The user needs a dedicated gate: **do not buy, reveal, photograph, scan, forward, redeem, validate, or check balance for any gift card/voucher code under pressure; route to existing money, credential, official-pressure, recovery, evidence, family/helper, and follow-up flows.**

Good user story:

> “Someone tells me to buy a gift card or send the voucher/PIN/barcode/code as a fee, emergency, refund, or verification step. Before I do anything, show me a safe local checklist that never reads or scans the code.”

This is **not** a gift-card balance checker, redemption tool, barcode/QR scanner, camera/OCR feature, purchase flow, wallet/payment/banking integration, store lookup, refund/recovery service, official lookup, browser, dialer, SMS sender, or network service. It must not read, scan, photograph, decode, validate, redeem, check balance, purchase, refund, reverse, block, freeze, recover, upload, attach, forward, or verify any voucher/gift-card/code/barcode/QR/payment state. It should only show deterministic value-free cards and route to existing flows.

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `GiftCardCodePauseCoach.kt`:
   - `GiftCardCodePauseCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): GiftCardCodePauseCoach`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction or literal plan values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, score, action plan, summary, raw pasted text, redacted clues, incident bodies/previews, `IncidentPreviewRedactor`, persisted incident contents, contact labels/numbers, `SafetyPlan.safeWord`, callback script values, official directory entries/numbers, exact agency/store/brand names/numbers from directory, phone numbers, URLs/domains, case IDs, report/submission/receipt status, account/card/bank/payment/wallet/transaction/refund/chargeback values, actual gift card/voucher/PIN/barcode/QR/serial/redeem code values, OTP/PIN/password/security answer values, resident IDs, device IDs, app/package names, clipboard contents, login/account/authentication state, call/SMS/contact/notification state, recipient values, helper/lawyer/investigator values, or verification/recontact/handoff/reset/recovery/result values.

2. Gift-card/code pause cards:
   - Fixed manual cards, for example:
     - “상품권·기프티콘 구매를 송금보다 가볍게 보지 않기”
     - “코드·PIN·바코드·QR을 말하거나 사진·캡처·전달하지 않기”
     - “쪼개서 여러 장 사라는 지시를 멈추기”
     - “환급·수수료·보증금·벌금·가족 위급을 상품권으로 해결하지 않기”
     - “앱 설치·링크·화면공유로 코드 확인을 돕겠다는 흐름 끊기”
     - “증거·가족·공식 도움은 코드값 없이 기존 화면에서만 정리하기”
   - Each card should include:
     - id/display order/title
     - code-pressure scenario sentence to read manually
     - short value-free action sentence
     - why it matters
     - do-not-buy/share/scan/redeem/attach boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, `GIFTICON_PIN`, urgent transfer/financial/payment signals (`URGENT_TRANSFER`, `BAIT`, `GIFT_CRYPTO`, `SAFE_ACCOUNT_FRAUD`, `LOAN_REFINANCE_FEE`, `OVERSEAS_PAYMENT_ALERT`), credential/OTP signals, impersonation/secrecy/official-pressure signals, recovery-promise contexts (use existing signals only, not raw text), link/smishing, app-install/live-call-control, family emergency/deepfake/phone-number-change, saved incident, and low/in-progress/ready readiness.
   - Default/no-analysis state should be conservative: show all/broad gift-card/code cards and never claim a gift card, voucher, code, PIN, barcode, QR, serial, payment, purchase, balance, redemption, account, wallet, transaction, refund, chargeback, cancellation, recovery, family emergency, case, report, helper, store, brand, institution, caller, link, document, device, or credential state is official, verified, safe, secure, valid, accepted, submitted, received, blocked, cancelled, reversed, recovered, refunded, frozen, protected, legal, complete, resolved, redeemed, unused, traceable, guaranteed, or problem-free.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `giftCardCodePauseCoachScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard / readiness section.
     - Analysis result screen.
     - Quick Stop.
     - Money Transfer Pause Gate.
     - Credential Secret Freeze Coach.
     - Recovery Promise Reality Check Coach.
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
   - Action buttons must delegate to existing screens/flows only; do not duplicate official lookup, dialing, browsing, report submission, legal advice, gift-card redemption, balance check, purchase, refund/chargeback, bank/payment/account/device/evidence/family implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** add permissions or use network/web/browser/map/geocoder APIs, official directory values in the builder/share text, phone-number/URL validation, barcode/QR/image/OCR scanning, camera, media/file pickers, wallet/blockchain/exchange APIs, payment/banking/card APIs, gift-card redemption/balance/purchase APIs, `Intent.ACTION_VIEW`, `Intent.ACTION_DIAL`, `Intent.ACTION_CALL`, `ACTION_SENDTO`, `tel:`, `smsto:`, contacts/call/SMS/notification APIs, clipboard/text watcher/autofill/credential APIs, account/auth/crypto APIs, URI grants, attachments, `Intent.EXTRA_STREAM`, NFC, device-control, alarm/scheduler/background-worker, or persistence mutation APIs.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new coach is named as local/manual/no-permission/no-network/no-gift-card-balance/redeem/purchase/refund/no-barcode/QR/OCR/camera/no official lookup/no recovery/refund/chargeback/case/legal/payment/bank/wallet/blockchain/crypto tracing/no-number-validation/no-URL-validation/no-caller-ID-check/no-document-validation/no-report-submission/no-dial/no-SMS/no-browser/no-map/no contacts/calls/notifications/no clipboard/autofill/credential APIs/no media/file/camera/OCR/no attachment/no URI share/no auto-send/no scheduler/no payment/banking/NFC/device control/no storage/no mutation.
   - Update `README.md` flow and add a section for the new feature.
   - Keep copy explicit that the feature routes to existing money, credential, recovery, official pressure, evidence, official help/report prep, account/remote/exposure, family/helper, and follow-up screens but never proves or performs purchase, redemption, balance check, refund, chargeback, cancellation, freeze, tracing, report, legal, or financial results.

5. Tests:
   - Add `GiftCardCodePauseCoachTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default coach is conservative, recommends all gift-card/code cards, and does not claim gift card/voucher/code/PIN/barcode/QR/serial/payment/purchase/balance/redemption/account/wallet/transaction/refund/chargeback/cancellation/recovery/family emergency/case/report/helper/store/brand/institution/caller/link/document/device/credential state is official/verified/safe/secure/valid/accepted/submitted/received/blocked/cancelled/reversed/recovered/refunded/frozen/protected/legal/complete/resolved/redeemed/unused/traceable/guaranteed/problem-free.
     - `GIFTICON_PIN` and money signals recommend money pause, credential freeze, recovery promise check, official pressure check, account lockdown, exposure triage, evidence handoff, trusted helper, official help/report prep without claiming gift-card redemption, payment cancellation, refund, chargeback, balance check, code blocking, or recovery.
     - credential/OTP signals recommend credential secret freeze, safe paste/privacy, account lockdown, remote cutoff, exposure triage without exposing OTP/PIN/password/account/card/bank/wallet/gift-card values.
     - impersonation/secrecy/official-pressure style signals recommend official pressure reality check, active contact exit, family alert/debrief, trusted helper, evidence handoff/privacy, official help/report prep without official numbers/directory values or legal/case validation.
     - recovery-promise contexts route to Recovery Promise Reality Check Coach, money pause, credential freeze, evidence handoff, trusted helper, official help/report prep without recovery/refund/chargeback claims.
     - link/smishing/app-install/live-call-control recommends not continuing in the same link/app/call/screen-share session, remote cutoff/exposure/privacy routes, and no URL validation, link scan, app inventory, malware removal, remote disconnect, device cleaning, gift-card balance/redeem/check, or browser/network behavior.
     - family emergency/deepfake/phone-number-change recommends separate-channel verification, family callback, voice reality check, new-number quarantine, safe-word reset, safe call scripts, family alert without exposing contact/safe-word/callback values.
     - saved incident changes recommendations only by count category and does not read incident contents/previews.
     - low/in-progress readiness recommends safety plan, safe-word reset, readiness roadmap, safety drill, safe call scripts; ready readiness says readiness does not make a gift-card/code request real or safe.
     - every card has complete fields, stable order, unique route actions, and all action IDs map to existing flows.
     - share summary contains only generic gift-card/code/card/route/action labels and no raw input, redacted clue, safety plan literal, safe-word/callback/contact values, phone numbers, URLs/domains, long numbers, barcode/QR/serial/redeem values, package/app names, account/card/bank/wallet/transaction/OTP/PIN/password/security answer/resident-like values, device identifiers, official numbers, directory values, store/brand/agency/helper/lawyer names, case/report/submission/receipt IDs, file/media names, URI/path values, screenshot/image/OCR/audio/transcription values, clipboard/autofill/account/login state, call/SMS/notification state, recipient names, verification/result/status values, purchase/balance/redeem/refund/chargeback/recovery/freeze/cancel/block/trace status, or legal/financial outcome values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no gift-card purchase/redeem/balance/refund/chargeback/legal/case/wallet/crypto-tracing/payment/bank action/no barcode/QR/OCR/camera/no official lookup/no phone/URL validation/no caller-ID/document validation/no report submission/no dial/call/SMS/browser/map/network/no contacts/call-log/notifications/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no file/photo/media/camera/OCR/no attachment/no URI share/no auto-send/no permissions/no scheduler/no payment/banking/NFC/device control.
     - copy avoids unsafe guarantees: no “100%”, no “valid code”, no “unused”, no “redeemed”, no “balance checked”, no “payment cancelled”, no “refund guaranteed”, no “chargeback accepted”, no “recovered”, no “refunded”, no “cancelled”, no “blocked”, no “resolved”, no “complete”, no legal/financial result guarantee, no automatic monitoring/scanning/OCR/deletion/redaction/blocking/calling/messaging/lookup/verification/storage/upload/attachment/submission.
   - Add/extend source-reading tests that prove the model does not import Android or touch network/browser/map/geocoder/official lookup/contact/phone/SMS/call/notification/clipboard/autofill/credential/account/auth/crypto/media/payment/device APIs, barcode/QR/OCR/camera APIs, gift-card redemption/balance/purchase APIs, `SafetyPlanStore`, `SafetyPlan`, `.safeWord`, `.callbackScript`, `.contacts`, `IncidentStore`, incident contents, `IncidentPreviewRedactor`, official directory entries, package manager results, wallet/blockchain/payment/refund/chargeback/case APIs, or persistence mutation.
   - Add/extend UI source tests proving the new screen uses only `FamilyReadinessBuilder.build(safetyPlan.load())` to compute value-free readiness and passes only readiness category into the builder; it must not read/display/share plan literal values, incident bodies/previews, official numbers/directory entries, caller ID/call state, URLs/domains, clipboard text, autofill/account/login state, contact labels/numbers, account/card/bank/wallet/transaction/gift-card code values, barcode/QR values, or package/app values. The route to existing screens is allowed; the new slice must not call `safetyPlan.save(`, `safetyPlan.clear()`, `incidents.add/clear/load`, `prefs.edit`, `OfficialHelpDirectoryBuilder` in builder/share text, `dial`, `openHelpDirectory` is allowed only as a route action from the action router, and no network/browser/dial/SMS APIs.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No gift-card/voucher/code/PIN/barcode/QR/serial balance check, redemption, purchase, refund, cancellation, blocking, tracing, validation, capture, scan, OCR, photo, upload, attachment, or forwarding.
- No refund, chargeback, wallet, crypto tracing, recovery, official-number, legal-case, bank/account/card/payment, caller-ID, phone-number, URL/domain, profile, institution, document, report, identity, credential, code, device, or recovery-status validation/lookup.
- No network calls, web browsing, browser intents, maps/geocoding, official-number lookup, report submission, upload, cloud backup, evidence sync, blockchain/exchange/wallet lookup, payment processing, app-store/store lookup, or barcode/QR services.
- No dialing/calling/hang-up/call-screening/call-blocking/call-state/call-log/SMS/send-to/recipient APIs.
- No contact/address-book reading/writing/editing/picking, notification listening/monitoring/filtering, clipboard/text watcher/autofill/credential APIs, account/auth/crypto APIs, media/file/photo/storage/camera/screenshot/OCR/image analysis APIs, attachments, URI sharing, streams, file/document/photo/audio pickers, MediaStore, FileProvider, or image/audio capture.
- No display/share/use of official directory phone numbers, agency/store/brand entry values, exact links/domains, case/report/receipt IDs, literal gift-card/voucher/PIN/barcode/QR/serial/redeem values, literal OTP/PIN/password/security answers, account/card/bank/wallet/transaction/payment/refund/chargeback values, `SafetyPlan.safeWord`, callback script text, contact labels/numbers, raw analysis input, redacted clues, incident previews/bodies, package/app names, device IDs, recipient values, helper/lawyer/investigator names, or exact phone/channel/profile values.
- No mutation of safety plan, incident log, official directory, contacts, notification state, call/SMS state, account/card/bank/payment/wallet/gift-card state, device/app/settings state, credential status, report/submission/receipt status, purchase/balance/redeem/refund/chargeback/recovery/freeze/cancel/block/trace status, or verification status from the new coach.
- No alarms, reminders, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No payment APIs, banking APIs, gift-card APIs, NFC, account-manager integration, payment cancellation, chargeback filing, refund claim, account/card/code freeze, wallet tracing, blockchain lookup, purchase/redeem/balance check, debt/loan service, or recovery automation.
- No accessibility service, device admin, VPN, usage stats, package/app inventory scanning, app install/uninstall, settings mutation, app ops, or MDM behavior.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only. It must not attach files/streams or include any official/contact/channel/profile/account/payment/wallet/gift-card/result value.
- Do not ask users to type exact names, contacts, phone numbers, account/card/bank/wallet/transaction values, gift-card/voucher/PIN/barcode/QR/serial/redeem codes, OTP/PIN, passwords, security answers, safe-word answers, callback script literals, URLs/domains, case/report/receipt IDs, resident IDs, device IDs, installed app/package names, official numbers, store/brand/helper/lawyer/investigator names, audio clip names, transcripts, screenshots with credentials, file/media names, URI/path values, call-log details, notification contents, recipient names, purchase/balance/redeem/refund/chargeback/recovery/freeze/cancel/block/trace status, or raw scam text into this coach.
- Do not claim a gift card, voucher, code, PIN, barcode, QR, serial, person, institution, store, brand, helper, lawyer, investigator, title, caller ID, number, link, document, case, report, payment, account, card, bank action, wallet, transaction, credential, login, device, purchase, redemption, balance, chargeback, refund, freeze, trace, cancellation, or recovery state is official, real, safe, secure, verified, valid, accepted, submitted, received, blocked, cancelled, reversed, resolved, complete, recovered, refunded, frozen, protected, legal, redeemed, unused, traceable, guaranteed, problem-free, or automatically protected.

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
    'Intent.EXTRA_STREAM','ClipData','FLAG_GRANT_READ_URI_PERMISSION','FLAG_GRANT_WRITE_URI_PERMISSION','Uri.parse','FileProvider','MediaStore','ACTION_IMAGE_CAPTURE','ACTION_PICK','ACTION_GET_CONTENT','ACTION_OPEN_DOCUMENT','READ_MEDIA_IMAGES','READ_EXTERNAL_STORAGE','WRITE_EXTERNAL_STORAGE','CAMERA','MlKit','TextRecognition','OCR','ImageAnalysis','BitmapFactory','ExifInterface','Barcode','QRCode','MLKit','ImageCapture',
    'BillingClient','NfcAdapter','DevicePolicyManager','AccessibilityService','VpnService','UsageStatsManager','PackageInstaller','deletePackage','ACTION_MANAGE_UNKNOWN_APP_SOURCES','ACTION_APPLICATION_DETAILS_SETTINGS','Settings.','getInstalledPackages','getInstalledApplications','Blockchain','WalletConnect','web3','Bitcoin','Ethereum','Chargeback','RefundManager'
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

Note: repo-wide scan may continue to show unchanged legacy official-help/report helper APIs (`OfficialHelpDirectoryBuilder`, `directory.entries`, `dialNumber`, `Intent.ACTION_DIAL`, `tel:`, `Uri.parse`) and explanatory privacy-copy mentions of OCR/audio/permissions. If so, add a bounded slice-specific source scan/test proving the new Gift Card Code Pause slice itself does not contain network/browser/map/official lookup, gift-card balance/redeem/purchase APIs, barcode/QR/OCR/camera/media implementation APIs, refund/chargeback/wallet/payment/bank APIs, dial/SMS/contact/notification, clipboard/autofill/credential/account/auth/crypto, stream/URI share, payment/device, persistence mutation, plan mutation, raw/saved secret values, exact phone/channel/profile/account/payment/wallet/gift-card values, official numbers, directory values, app/package values, attachments, report/submission/receipt/recovery status, or incident bodies/previews. `openHelpDirectory(Screen.GIFT_CARD_CODE_PAUSE_COACH)` is allowed only in the UI action router as a route to an existing screen.

Also inspect focused JUnit XML for `GiftCardCodePauseCoachTest`, `PrivacyCenterBuilderTest`, and `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-gift-card-code-pause-coach.md
git commit -m "feat: add gift card code pause coach"
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
