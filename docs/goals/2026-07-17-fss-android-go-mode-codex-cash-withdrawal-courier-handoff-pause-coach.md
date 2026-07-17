# FSS Android /go continuation — Cash Withdrawal & Courier Handoff Pause Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `0d97a0f243c20ae7d195a5337beffe67ed1d3ff9` — `feat: add link app install quarantine coach`

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

Result: `BUILD SUCCESSFUL`; APK exists with `1527946` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 43 commits.

Focused JUnit XML evidence before this slice:

- `LinkAppInstallQuarantineCoachTest`: debug 17/17, release 17/17, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 24/24, release 24/24, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 34/34, release 34/34, failures 0, errors 0, skipped 0.
- `GiftCardCodePauseCoachTest`: debug 16/16, release 16/16, failures 0, errors 0, skipped 0.

Parent manifest/source scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `RECORD_AUDIO`, `CAMERA`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device-admin, notification-listener, VPN, or package-install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Repo-wide scan has no network/browser/geocoder/webview APIs, no phone/SMS/contact/call-state APIs except existing legacy official-help dial route (`Intent.ACTION_DIAL`, `tel:`, `Uri.parse`, `dialNumber` in established official-help/report code), no clipboard/text-watcher/autofill/CredentialManager/AccountManager/biometric/keyguard/keystore/crypto APIs, no scheduler/notification service APIs, no stream/media/file/camera implementation APIs, no PackageManager/app inventory/install/uninstall/settings/accessibility/device-admin/VPN/usage-stats APIs, no payment/NFC/device-control APIs, no blockchain/wallet/chargeback/refund implementation APIs. Repo-wide expected hits: existing `OfficialHelpDirectoryBuilder`/`directory.entries`/`dialNumber` in official-help/report files, existing legacy `Intent.ACTION_DIAL`/`tel:`/`Uri.parse` in `MainActivity.kt`, explanatory privacy-copy `OCR`/`RECORD_AUDIO` strings.
- Bounded Link/App slice scan found no forbidden implementation APIs, no URL/domain/package/app/permission/device/official values in model/share, no network/browser/map URL validation, no QR/barcode/OCR/camera/media/file implementation, no PackageManager/app inventory/install/uninstall/settings/accessibility/device-admin/VPN/usage-stats implementation, no malware/antivirus/device-cleaning/remote-disconnect implementation, no payment/bank/wallet/refund/chargeback/gift-card APIs, no dial/SMS/contact/notification/clipboard/autofill/credential/account/auth/crypto APIs, no incident/plan mutation, no raw incident/safety/link/app/package/device/status values, no unsafe claims. It has exactly one manual text share (`ACTION_SEND`, `text/plain`) and one allowed `openHelpDirectory(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)` route.

Parent source inspection found no Link/App privacy/routing issue: the slice uses only `currentAnalysis`, `FamilyReadinessBuilder.build(safetyPlan.load())`, and `incidents.count()`, shares only the builder summary, routes to existing flows, and keeps URL/app/package/permission/device checking or mutation, browser/network/dial/report submission, official lookup values, evidence attachments, payment/refund/chargeback/gift-card actions, and settings/device operations out of the builder and share text.

## Product intent for next cohesive slice

Add a **Cash Withdrawal & Courier Handoff Pause Coach** screen/card for 현금 인출, ATM/은행 방문, 직원·수사관·금감원·택배/퀵/보안요원에게 현금·카드·통장·기기·서류를 전달, 물품보관함/우편/택배/퀵으로 맡기기, 가족에게 숨기고 이동하라는 보이스피싱 흐름.

Current app has money transfer, official-pressure, active-contact, recovery, link/app, and gift-card gates, but Korean phishing often moves the victim into physical-world handoff: “은행에 가서 현금으로 빼라”, “수사관/금감원 직원/택배 기사가 받을 것이다”, “통화 끊지 말고 카페/역/ATM/은행 앞에서 만나라”, “봉투/서류/카드/통장을 맡겨라”, “가족에게 말하지 말라”. The user needs a dedicated gate: **do not withdraw cash, travel to a meeting point, hand over cash/card/bankbook/device/documents, put anything in a locker/parcel/courier, keep the call open, or hide the movement; route to existing money, official-pressure, active-contact, family/helper, evidence, official help/report prep, and follow-up flows.**

Good user story:

> “Someone claiming to be a bank/police/prosecutor/FSS/platform/courier tells me to withdraw cash or meet/hand off something. Before I leave or hand anything over, show me a local checklist that never records locations, amounts, routes, or identities.”

This is **not** a cash/ATM/banking tool, courier tracker, map/navigation, location recorder, meet-up scheduler, police dispatch, bank branch finder, official lookup, dialer, SMS sender, report-submission tool, evidence/photo scanner, document validator, identity checker, cash serial tracker, account/card/device freeze tool, recovery/refund service, or legal/financial decision maker. It must not read, store, validate, geocode, map, track, route, call, message, schedule, report, scan, photograph, attach, upload, submit, block, freeze, recover, reverse, or verify any location, route, cash amount, ATM/branch, courier/person, institution, document, account/card/bank/payment, device, case/report, or handoff state. It should only show deterministic value-free cards and route to existing flows.

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `CashWithdrawalCourierHandoffPauseCoach.kt`:
   - `CashWithdrawalCourierHandoffPauseCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): CashWithdrawalCourierHandoffPauseCoach`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction or literal plan values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, score, action plan, summary, raw pasted text, redacted clues, incident bodies/previews, `IncidentPreviewRedactor`, persisted incident contents, contact labels/numbers, `SafetyPlan.safeWord`, callback script values, official directory entries/numbers, exact agency/store/brand/app/bank/branch names/numbers from directory, locations/addresses/routes/meeting points/locker IDs/courier IDs, phone numbers, URLs/domains, package/app/file/media names, case IDs, report/submission/receipt status, account/card/bank/payment/wallet/transaction/refund/chargeback values, cash amounts/serials/banknotes, OTP/PIN/password/security answer values, resident IDs, device IDs, clipboard contents, login/account/authentication state, call/SMS/contact/notification state, recipient/courier/helper/lawyer/investigator values, or verification/recontact/handoff/reset/recovery/result values.

2. Cash/courier pause cards:
   - Fixed manual cards, for example:
     - “현금 인출·ATM/은행 방문을 지금 멈추기”
     - “직원·수사관·택배/퀵 기사에게 현금·카드·통장·기기를 건네지 않기”
     - “물품보관함·택배·우편·퀵·봉투 지시를 따르지 않기”
     - “통화·채팅을 유지한 채 이동하거나 가족에게 숨기지 않기”
     - “기관·은행·플랫폼 직함과 사건/문서 압박을 기존 현실 점검으로 분리하기”
     - “노출·증거·가족·공식 도움은 장소·금액·인물값 없이 기존 화면에서 정리하기”
   - Each card should include:
     - id/display order/title
     - cash/courier-pressure scenario sentence to read manually
     - short value-free action sentence
     - why it matters
     - do-not-withdraw/travel/handoff/meet/share/attach boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, urgent transfer/financial/payment signals (`URGENT_TRANSFER`, `BAIT`, `GIFT_CRYPTO`, `SAFE_ACCOUNT_FRAUD`, `LOAN_REFINANCE_FEE`, `OVERSEAS_PAYMENT_ALERT`), official-pressure signals (`IMPERSONATION`, `SECRECY`, `SAFE_ACCOUNT_FRAUD`), live-call/app/link control (`LIVE_CALL_CONTROL`, `APP_INSTALL`, `LINK`, `PARCEL_INVITATION_SMISHING`), credential/OTP signals (`CREDENTIALS`, `GIFTICON_PIN`, `OVERSEAS_PAYMENT_ALERT`), recovery-promise contexts (`BAIT`, `GIFT_CRYPTO`, `SAFE_ACCOUNT_FRAUD`, `LOAN_REFINANCE_FEE`, `GIFTICON_PIN`, `OVERSEAS_PAYMENT_ALERT`), family emergency/deepfake/phone-number-change, saved incident, and low/in-progress/ready readiness.
   - Default/no-analysis state should be conservative: show all/broad cash/courier pause cards and never claim a person, courier, investigator, helper, institution, bank, branch, ATM, route, address, locker, parcel, cash amount, banknote, document, case, report, account, card, device, phone/channel, family emergency, payment, refund, recovery, or handoff state is official, verified, safe, secure, valid, accepted, submitted, received, protected, legal, resolved, complete, blocked, cancelled, reversed, recovered, refunded, frozen, traced, guaranteed, problem-free, delivered, collected, deposited, or scheduled.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `cashWithdrawalCourierHandoffPauseCoachScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard / readiness section.
     - Analysis result screen.
     - Quick Stop.
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
   - Action buttons must delegate to existing screens/flows only; do not duplicate official lookup, dialing, browsing, report submission, legal advice, map/location/navigation, courier tracking, meeting scheduling, cash/ATM/bank/payment/card/account operations, device settings/freeze, evidence/media/file/photo, or family/contact implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** add permissions or use location/map/geocoder/navigation APIs, network/web/browser APIs, `Intent.ACTION_VIEW`, phone/URL/address validation APIs, official directory values in builder/share text, dial/call/SMS/contact/notification APIs, QR/barcode/OCR/image/camera/media/file APIs, PackageManager/app inventory/app-label/package APIs, package install/uninstall/settings/accessibility/device-admin/VPN/usage-stats APIs, payment/banking/card/wallet/blockchain/refund/chargeback APIs, ATM/cash/courier/tracking APIs, clipboard/text watcher/autofill/credential APIs, account/auth/crypto APIs, URI grants, attachments, `Intent.EXTRA_STREAM`, NFC/device-control APIs, alarms/scheduler/background workers, or persistence mutation APIs.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new coach is named as local/manual/no-permission/no-network/no-map/no-location/no-route/no-navigation/no-geocoder/no-address validation/no courier tracking/no meet-up scheduling/no cash/ATM/bank/payment/card/account operation/no official lookup/no caller-ID/phone validation/no document/case validation/no report submission/no dial/no SMS/no contacts/calls/notifications/no QR/barcode/OCR/camera/no file/media/no evidence attachment/no URI share/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no payment/banking/NFC/wallet/blockchain/refund/chargeback/gift-card action/no app/package/device/settings mutation/no device-admin/accessibility/VPN/usage-stats/no auto-send/no scheduler/no storage/no mutation.
   - Update `README.md` flow and add a section for the new feature.
   - Keep copy explicit that the feature routes to existing money pause, official pressure, active-contact exit, separate-channel verification, family callback/safe call, trusted helper, recovery/gift-card/link-app/credential/account/remote/exposure/evidence, official help/report prep, family/helper, and follow-up screens but never withdraws, transfers, guides, maps, tracks, schedules, validates, dispatches, submits, freezes, recovers, or proves cash/handoff/location/person/case/payment states.

5. Tests:
   - Add `CashWithdrawalCourierHandoffPauseCoachTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default coach is conservative, recommends all cash/courier cards, and does not claim person/courier/investigator/helper/institution/bank/branch/ATM/route/address/locker/parcel/cash amount/banknote/document/case/report/account/card/device/phone/channel/family/payment/refund/recovery/handoff state is official/verified/safe/secure/valid/accepted/submitted/received/protected/legal/resolved/complete/blocked/cancelled/reversed/recovered/refunded/frozen/traced/guaranteed/problem-free/delivered/collected/deposited/scheduled.
     - financial/safe-account/loan/payment signals recommend money pause, official pressure check, active contact exit, separate-channel verification, account lockdown, exposure triage, evidence handoff, trusted helper, official help/report prep without cash withdrawal, ATM/bank/card/payment actions, refund/chargeback/recovery, map/location/courier behavior, or official-number lookup.
     - official pressure/secrecy signals recommend official pressure check, active contact exit, separate-channel verification, family alert/debrief, trusted helper, evidence handoff, official help/report prep without official numbers/directory values, legal/case validation, dial/browser/SMS/report submission.
     - live-call/app/link control recommends ending same contact/session manually, link-app quarantine, remote cutoff, exposure/privacy/account routes without call-state APIs, app/device/settings/package APIs, maps/location, or remote disconnect/device cleaning claims.
     - credential/OTP/gift-card signals recommend credential secret freeze, gift-card pause, account lockdown, safe paste/privacy, remote cutoff, exposure triage without exposing OTP/PIN/password/account/card/bank/wallet/gift-card/cash values.
     - family emergency/deepfake/phone-number-change recommends separate-channel verification, family callback, voice reality check, new-number quarantine, safe-word reset, safe call scripts, family alert without exposing contact/safe-word/callback values or reading contacts/calls/SMS.
     - saved incident changes recommendations only by count category and does not read incident contents/previews.
     - low/in-progress readiness recommends safety plan, safe-word reset, readiness roadmap, safety drill, safe call scripts; ready readiness says readiness does not make a cash/handoff/request safe/trusted/official.
     - every card has complete fields, stable order, unique route actions, and all action IDs map to existing flows.
     - share summary contains only generic cash/courier/card/route/action labels and no raw input, redacted clue, safety plan literal, safe-word/callback/contact values, phone numbers, addresses/routes/locations/locker IDs/courier IDs, URLs/domains, long numbers, QR/barcode/code/package/app/file/media/URI/path values, account/card/bank/wallet/transaction/amount/cash serial/OTP/PIN/password/security answer/resident-like values, device identifiers, official numbers, directory values, agency/helper/lawyer/investigator/courier names, case/report/submission/receipt IDs, screenshot/image/OCR/audio/transcription values, clipboard/autofill/account/login state, call/SMS/notification state, recipient names, verification/result/status values, delivered/collected/deposited/scheduled/handoff/cash/payment/refund/chargeback/recovery/freeze/cancel/block/trace status, or legal/financial outcome values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no cash withdrawal/no ATM/bank/payment action/no map/location/route/navigation/geocoder/address validation/no courier tracking/no meeting scheduling/no official lookup/no phone/caller/document/case validation/no report submission/no dial/call/SMS/no contacts/call-log/notifications/no QR/barcode/OCR/camera/no file/photo/media/no attachment/no URI share/no clipboard/text watcher/autofill/credential APIs/no account/auth/crypto APIs/no payment/banking/NFC/wallet/blockchain/refund/chargeback/no app/package/device/settings mutation/no device-admin/accessibility/VPN/usage-stats/no auto-send/no permissions/no scheduler.
     - copy avoids unsafe guarantees: no “100%”, no “official courier”, no “verified investigator”, no “safe handoff”, no “bank verified”, no “cash protected”, no “case accepted”, no “report submitted/accepted/received”, no “delivered/collected/deposited/scheduled”, no “payment cancelled”, no “refund guaranteed”, no “chargeback accepted”, no “recovered/refunded/frozen/traced”, no legal/financial result guarantee, no automatic monitoring/scanning/deletion/redaction/blocking/calling/messaging/lookup/verification/storage/upload/attachment/submission.
   - Add/extend source-reading tests proving the model does not import Android or touch network/browser/map/geocoder/navigation/location/address validation/official lookup/contact/phone/SMS/call/notification/clipboard/autofill/credential/account/auth/crypto/media/payment/device APIs, QR/barcode/OCR/camera APIs, PackageManager/app inventory/app-label/package APIs, package install/uninstall/settings/accessibility/device-admin/VPN/usage-stats APIs, payment/refund/chargeback/wallet/gift-card APIs, map/location/courier/tracking/meeting/calendar APIs, `SafetyPlanStore`, `SafetyPlan`, `.safeWord`, `.callbackScript`, `.contacts`, `IncidentStore`, incident contents, `IncidentPreviewRedactor`, official directory entries, package/app/location/courier results, or persistence mutation.
   - Add/extend UI source tests proving the new screen uses only `FamilyReadinessBuilder.build(safetyPlan.load())` to compute value-free readiness and passes only readiness category into the builder; it must not read/display/share plan literal values, incident bodies/previews, official numbers/directory entries, caller ID/call state, URLs/domains, locations/addresses/routes, clipboard text, autofill/account/login state, contact labels/numbers, account/card/bank/wallet/transaction/cash/cash-serial/gift-card/code values, QR/barcode values, package/app values, file/media/URI values, map/navigation state, courier/handoff/meeting state, permission/app install state, or report/submission/receipt status. The route to existing screens is allowed; the new slice must not call `safetyPlan.save(`, `safetyPlan.clear()`, `incidents.add/clear/load`, `prefs.edit`, `OfficialHelpDirectoryBuilder` in builder/share text, `dial`, `openHelpDirectory` is allowed only as a route action from the action router, and no network/browser/dial/SMS/map/location/app/device/settings APIs.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No cash withdrawal, ATM/bank branch action, card/account/payment operation, courier/parcel/locker tracking, meet-up scheduling, map/location/navigation/geocoding, address/route validation, official lookup, report submission, legal/case lookup, caller-ID checking, phone-number validation, or document validation.
- No network/browser/webview/map/geocoder/navigation APIs and no `Intent.ACTION_VIEW`.
- No dialing/calling/hang-up/call-screening/call-blocking/call-state/call-log/SMS/send-to/recipient APIs.
- No contact/address-book reading/writing/editing/picking, notification listening/monitoring/filtering, clipboard/text watcher/autofill/credential APIs, account/auth/crypto APIs.
- No QR/barcode scan, OCR, camera, image analysis, screenshot, file/media picker, attachment, stream, URI share, upload, or evidence sync.
- No PackageManager/app inventory/app label/package APIs, package install/uninstall/settings/accessibility/device-admin/VPN/usage-stats/notification-listener APIs, app permission/grant/revoke APIs, malware scanning/antivirus APIs, or device-control behavior.
- No payment/banking/card/wallet/blockchain/gift-card/refund/chargeback/recovery APIs, payment cancellation, refund/chargeback filing, wallet tracing, purchase/redeem/balance check, code/account/card freeze, or financial/legal automation.
- No display/share/use of official directory phone numbers, agency/bank/branch/store/brand/app entry values, exact locations/addresses/routes/meeting points/locker IDs/courier IDs, cash amounts/serials/banknotes, exact links/domains, QR/barcode/code values, package/app/file/media/URI values, case/report/receipt IDs, literal OTP/PIN/password/security answers, account/card/bank/wallet/transaction/payment/refund/chargeback values, `SafetyPlan.safeWord`, callback script text, contact labels/numbers, raw analysis input, redacted clues, incident previews/bodies, device IDs, recipient/courier/helper/lawyer/investigator names, exact phone/channel/profile values, delivered/collected/deposited/scheduled/handoff/cash/payment/refund/chargeback/recovery/freeze/cancel/block/trace status values.
- No mutation of safety plan, incident log, official directory, contacts, notification state, call/SMS state, location/map/route/courier/parcel/locker/meeting state, link/app/package/permission/device state, account/card/bank/payment/wallet/gift-card state, credential status, report/submission/receipt status, purchase/balance/redeem/refund/chargeback/recovery/freeze/cancel/block/trace status, or verification status from the new coach.
- No alarms, reminders, CalendarContract, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only. It must not attach files/streams or include any official/contact/channel/profile/account/payment/wallet/link/app/location/courier/cash/device/result value.
- Do not ask users to type exact names, contacts, phone numbers, addresses/routes/meeting places/locker IDs/courier IDs, cash amounts/serials, account/card/bank/wallet/transaction values, gift-card/voucher/PIN/barcode/QR/serial/redeem codes, OTP/PIN, passwords, security answers, safe-word answers, callback script literals, URLs/domains, package/app names, file/media names, URI/path values, case/report/receipt IDs, resident IDs, device IDs, official numbers, agency/bank/branch/store/brand/helper/lawyer/investigator/courier names, audio clip names, transcripts, screenshots with credentials, call-log details, notification contents, recipient names, delivered/collected/deposited/scheduled/handoff/cash/payment/refund/chargeback/recovery/freeze/cancel/block/trace status, or raw scam text into this coach.
- Do not claim a person, courier, investigator, helper, institution, bank, branch, ATM, route, address, locker, parcel, cash, document, case, report, payment, account, card, bank action, wallet, transaction, credential, login, device, location, handoff, purchase, redemption, balance, chargeback, refund, freeze, trace, cancellation, recovery, app install/removal/permission, or malware/device state is official, real, safe, secure, verified, valid, accepted, submitted, received, installed, uninstalled, disabled, cleaned, blocked, removed, revoked, granted, scanned, inspected, checked, resolved, complete, recovered, refunded, frozen, protected, legal, trusted, delivered, collected, deposited, scheduled, malware-free, guaranteed, or problem-free.

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
    'HttpURLConnection','HttpsURLConnection','java.net.URL','OkHttp','Retrofit','ktor.client','WebView','Intent.ACTION_VIEW','Geocoder','LocationManager','FusedLocationProvider','LocationServices','Intent.ACTION_VIEW','ACTION_VIEW','ACTION_SENDTO','Maps','GoogleMap','MapView','NavController','Address','Patterns.WEB_URL','URLUtil','InetAddress','PhoneNumberUtils','Patterns.PHONE','OfficialHelpDirectoryBuilder','directory.entries','dialNumber',
    'Intent.ACTION_CALL','Intent.ACTION_DIAL','tel:','smsto:','TelecomManager','TelephonyManager','CallScreeningService','PhoneStateListener','CallLog','SmsManager','ContactsContract',
    'ClipboardManager','getPrimaryClip','setPrimaryClip','TextWatcher','addTextChangedListener','AutofillManager','CredentialManager','android.credentials','AccountManager','BiometricPrompt','KeyguardManager','KeyStore','javax.crypto','MessageDigest',
    'MediaRecorder','AudioRecord','SpeechRecognizer','RecognizerIntent','TextToSpeech','AudioManager','MediaPlayer','SoundPool','android.media','android.speech','RECORD_AUDIO',
    'NotificationManager','NotificationCompat','NotificationListenerService','AlarmManager','WorkManager','JobScheduler','CalendarContract','ForegroundService','BroadcastReceiver','POST_NOTIFICATIONS','SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED',
    'Intent.EXTRA_STREAM','ClipData','FLAG_GRANT_READ_URI_PERMISSION','FLAG_GRANT_WRITE_URI_PERMISSION','Uri.parse','FileProvider','MediaStore','ACTION_IMAGE_CAPTURE','ACTION_PICK','ACTION_GET_CONTENT','ACTION_OPEN_DOCUMENT','READ_MEDIA_IMAGES','READ_EXTERNAL_STORAGE','WRITE_EXTERNAL_STORAGE','CAMERA','MlKit','TextRecognition','OCR','ImageAnalysis','BitmapFactory','ExifInterface','Barcode','QRCode','BarcodeScanner','QRCodeReader','MLKit','ImageCapture',
    'PackageManager','getInstalledPackages','getInstalledApplications','PackageInstaller','deletePackage','ACTION_MANAGE_UNKNOWN_APP_SOURCES','ACTION_APPLICATION_DETAILS_SETTINGS','Settings.','DevicePolicyManager','AccessibilityService','VpnService','UsageStatsManager','AccessibilityManager','AppOpsManager','ACTION_REQUEST_INSTALL_PACKAGE','ACTION_DELETE','ACTION_UNINSTALL_PACKAGE',
    'BillingClient','NfcAdapter','Blockchain','WalletConnect','web3','Bitcoin','Ethereum','Chargeback','RefundManager','GiftCardManager','BalanceChecker','RedemptionService','PaymentClient','BankingClient','WalletClient','Courier','DeliveryTracking','TrackingClient','AtmClient','CashManager','BankBranch','MeetingScheduler'
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

Note: repo-wide scan may continue to show unchanged legacy official-help/report helper APIs (`OfficialHelpDirectoryBuilder`, `directory.entries`, `dialNumber`, `Intent.ACTION_DIAL`, `tel:`, `Uri.parse`) and explanatory privacy-copy mentions of OCR/audio/permissions. If so, add a bounded slice-specific source scan/test proving the new Cash Withdrawal & Courier Handoff Pause slice itself does not contain network/browser/map/location/geocoder/navigation/address validation/official lookup, URL/domain validation, QR/barcode/OCR/camera/media/file implementation, PackageManager/app inventory/package/install/uninstall/settings/accessibility/device-admin/VPN/usage-stats implementation, payment/banking/wallet/refund/chargeback/gift-card/cash/ATM/courier/tracking APIs, dial/SMS/contact/notification, clipboard/autofill/credential/account/auth/crypto, stream/URI share, scheduler/device, persistence mutation, plan mutation, raw/saved secret values, exact phone/channel/profile/account/payment/wallet/link/app/package/location/courier/cash/device values, official numbers, directory values, app/package/location/courier values, attachments, report/submission/receipt/recovery/handoff/status values, or incident bodies/previews. `openHelpDirectory(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)` is allowed only in the UI action router as a route to an existing screen.

Also inspect focused JUnit XML for `CashWithdrawalCourierHandoffPauseCoachTest`, `PrivacyCenterBuilderTest`, and `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-cash-withdrawal-courier-handoff-pause-coach.md
git commit -m "feat: add cash courier handoff pause coach"
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
