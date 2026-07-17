# FSS Android /go continuation — Official Pressure Reality Check Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `1cb1c7d457d2aa712b625d7b43d36ea6fd74c6ee` — `feat: add credential secret freeze coach`

Parent evaluator reconciled the Hermes background notification, confirmed the worker log had `### DONE`, `CODEX_RC=0`, and independently reran verification.

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

Result: `BUILD SUCCESSFUL`; APK exists with `1440454` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 39 commits.

Focused JUnit XML evidence before this slice:

- `CredentialSecretFreezeCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 20/20, release 20/20, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 30/30, release 30/30, failures 0, errors 0, skipped 0.
- `NewNumberQuarantineCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.

Manifest/source invariant scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `RECORD_AUDIO`, `CAMERA`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device-admin, notification-listener, VPN, or package-install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Repo-wide source scan has no clipboard/text-watcher/autofill/CredentialManager/AccountManager/biometric/keyguard/keystore/crypto APIs, no contact/address-book insertion/edit/pick/send-to APIs, no SMS/call-state/call-log APIs, no audio/voice APIs, no scheduler/notification/stream/media/file/camera/payment/NFC/device-admin/accessibility/VPN/package/network/browser/map APIs. Repo-wide hits are only unchanged legacy `tel:`, `Intent.ACTION_DIAL`, and `Uri.parse` in `MainActivity.kt`, plus explanatory privacy-copy mentions of `RECORD_AUDIO`/`OCR`.
- Bounded Credential Secret Freeze slice scan found no forbidden clipboard/autofill/credential/account/auth/crypto, contact/address-book, phone/SMS/call, notification, media/file/camera/OCR implementation APIs, stream/URI share, network/browser/map, official lookup, payment/device, persistence mutation, plan/incident raw-value, secret/account/card/clipboard/autofill/login/recovery status APIs. It uses one manual `ACTION_SEND` + `text/plain` share helper with only `coach.familyShareSummary`. A literal `OCR` match in the model is explanatory privacy copy only.

Parent source inspection found no Credential Secret Freeze privacy/routing issue: it uses `CredentialSecretFreezeCoachBuilder.build(analysis=currentAnalysis, readiness=FamilyReadinessBuilder.build(safetyPlan.load()), incidentCount=incidents.count())`, shares only the builder summary, routes only to existing flows, and has privacy regression coverage for model purity, 23 entry points, route-only actions, incident count helper, safe share helper, manifest permissions, and forbidden credential/clipboard/autofill/contact/phone/media/network/persistence APIs.

## Product intent for next cohesive slice

Add an **Official Pressure Reality Check Coach** screen/card. Many Korean phishing flows impersonate police/prosecutors/FSS/banks/couriers/platforms and pressure the user with “investigation”, “safe account”, “refund”, “loan fee”, “overseas payment”, “do not tell anyone”, “install this app”, “confirm with this number/link”, or “send OTP/money now.” The app has official help/report prep and several money/credential/device flows, but users need a dedicated official-pressure gate: **do not treat titles, badges, caller IDs, links, document screenshots, or threatening words as proof; pause and route to value-free existing help/stop flows without calling, browsing, or validating official numbers automatically.**

This is **not** an official-number lookup, legal advice, case lookup, police/prosecutor/bank verifier, caller-ID checker, domain checker, document validator, report submission tool, dialer, browser, map/geocoder, payment/banking integration, or network service. It must not fetch, validate, dial, open, store, submit, upload, or confirm official channels. It should only show deterministic value-free cards and route to existing flows.

Good user story:

> “Someone says they are police/prosecutor/bank/courier and threatens me. Before I obey, show me what to do without the app checking numbers or calling anyone.”

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `OfficialPressureRealityCheckCoach.kt`:
   - `OfficialPressureRealityCheckCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): OfficialPressureRealityCheckCoach`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction or literal plan values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, score, action plan, summary, raw pasted text, redacted clues, incident bodies/previews, `IncidentPreviewRedactor`, persisted incident contents, contact labels/numbers, `SafetyPlan.safeWord`, callback script values, account/card/OTP/PIN/password/security answer values, resident IDs, exact URLs/domains, official directory entries/numbers, agency names/numbers from directory, app/package names, device IDs, clipboard contents, account/authentication state, caller ID/call state, call/SMS/contact/notification state, or verification/recontact/handoff/reset/voice/new-number/credential result values.

2. Official-pressure cards:
   - Fixed manual cards, for example:
     - “직함·기관명·문서·발신번호를 증거로 단정하지 않기”
     - “비밀 유지·조사 협조 압박을 가족/보호자에게 숨기지 않기”
     - “안전계좌·환급·대출비용·해외결제 이야기는 돈 행동 전에 멈추기”
     - “OTP·PIN·앱 설치·화면공유 요구는 별도 멈춤 경로로 분리하기”
     - “기관 연락은 사용자가 직접 찾은 공개 경로에서 나중에 확인하기”
     - “증거·신고 준비는 값 없이 기존 화면에서만 정리하기”
   - Each card should include:
     - id/display order/title
     - pressure scenario sentence to read manually
     - short value-free action sentence
     - why it matters
     - do-not-call/lookup/share/submit boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, `IMPERSONATION`, `SECRECY`, `SAFE_ACCOUNT_FRAUD`, `LOAN_REFINANCE_FEE`, `OVERSEAS_PAYMENT_ALERT`, urgent transfer/financial/payment, credentials/OTP, link/smishing, app-install/live-call-control, family emergency/deepfake/phone-number-change, saved incident, and low/in-progress/ready readiness.
   - Default/no-analysis state should be conservative: show all/broad official-pressure cards and never call a person, institution, title, caller ID, number, link, document, case, report, payment, account, device, or recovery state official, verified, confirmed, safe, secure, valid, trusted, real, accepted, submitted, received, blocked, resolved, complete, recovered, refunded, frozen, protected, legal, or problem-free.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `officialPressureRealityCheckCoachScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard / readiness section.
     - Analysis result screen.
     - Quick Stop.
     - Official Help Directory.
     - Official Report Prep Card.
     - Money Transfer Pause Gate.
     - Credential Secret Freeze Coach.
     - Account & Device Lockdown Checklist.
     - Remote Access Cutoff Coach.
     - Exposure Impact Triage.
     - Evidence Handoff Privacy Coach.
     - Active Contact Exit Scripts.
     - Safe Paste Privacy Coach.
     - Separate Channel Verification Coach.
     - Verification Outcome Next Steps Board.
     - Recontact Boundary Coach.
     - New Number Quarantine Coach.
     - Voice Impersonation Reality Check Coach.
     - Family Alert / Trusted Helper Review / Family Response Role Board / Calm Debrief / Follow-up Planner / Safety Flow Router / Privacy Center.
   - Action buttons must delegate to existing screens/flows only; do not duplicate official lookup, dialing, browsing, reporting, legal advice, money/account/payment/device/evidence/family implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** add permissions or use network/web/browser/map/geocoder APIs, official directory values in the builder/share text, phone-number/URL validation, `Intent.ACTION_VIEW`, `Intent.ACTION_DIAL`, `Intent.ACTION_CALL`, `ACTION_SENDTO`, `tel:`, `smsto:`, contacts/call/SMS/notification APIs, clipboard/text watcher/autofill/credential APIs, file/media/camera/OCR, URI grants, attachments, `Intent.EXTRA_STREAM`, payment/banking/NFC, device-control, alarm/scheduler/background-worker, or persistence mutation APIs.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new coach is named as local/manual/no-permission/no-network/no-official-lookup/no-number-validation/no-URL-validation/no-domain-scan/no-caller-ID-check/no-document-validation/no-report-submission/no-dial/no-SMS/no-browser/no-map/no contacts/calls/notifications/no clipboard/autofill/credential APIs/no media/file/camera/OCR/no attachment/no URI share/no auto-send/no scheduler/no payment/banking/device control/no storage/no mutation.
   - Update `README.md` flow and add a section for the new feature.
   - Keep copy explicit that the feature routes to existing official help/report prep, money, credential, account, remote, evidence, family, and follow-up screens but never proves whether an institution, number, link, case, document, payment, account, or recovery state is real/safe/complete.

5. Tests:
   - Add `OfficialPressureRealityCheckCoachTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default coach is conservative, recommends all official-pressure cards, and does not claim person/institution/title/caller ID/number/link/document/case/report/payment/account/device/recovery state is official/verified/confirmed/safe/secure/valid/trusted/real/accepted/submitted/received/blocked/resolved/complete/recovered/refunded/frozen/protected/legal/problem-free.
     - impersonation/secrecy recommends not keeping the request secret, quick stop, active contact exit, family alert/debrief, trusted helper, evidence handoff/privacy, official help/report prep routes without official numbers or directory values.
     - safe account/refinance/overseas payment/urgent transfer/financial signals recommend money pause, credential freeze, account lockdown, remote cutoff, exposure triage without claiming payment cancellation, account/card freeze, refund, recovery, or official confirmation.
     - credential/OTP signals recommend credential secret freeze, safe paste/privacy, account lockdown, remote cutoff, exposure triage without exposing OTP/PIN/password/account/card values.
     - link/smishing/app-install/live-call-control recommends not continuing in the same link/app/call/screen-share session, remote cutoff/exposure/privacy routes, and no URL validation, link scan, app inventory, malware removal, remote disconnect, or device-cleaning claim.
     - family emergency/deepfake/phone-number-change recommends separate-channel verification, family callback, voice reality check, new-number quarantine, safe-word reset, safe call scripts, family alert without exposing contact/safe-word/callback values.
     - saved incident changes recommendations only by count category and does not read incident contents/previews.
     - low/in-progress readiness recommends safety plan, safe-word reset, readiness roadmap, safety drill, safe call scripts; ready readiness says readiness does not make an institution/contact/request real.
     - every card has complete fields, stable order, unique route actions, and all action IDs map to existing flows.
     - share summary contains only generic official-pressure/card/route/action labels and no raw input, redacted clue, safety plan literal, safe-word/callback/contact values, phone numbers, URLs/domains, long numbers, package/app names, account/card/OTP/PIN/password/security answer/resident-like values, device identifiers, official numbers, directory values, case IDs, file/media names, URI/path values, screenshot/image/OCR/audio/transcription values, clipboard/autofill/account/login state, call/SMS/notification state, recipient names, verification/result/status values, report/submission/receipt values, or reset/recovery status.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no official lookup/no phone/URL validation/no domain scan/no caller-ID check/no document validation/no report submission/no dial/call/SMS/browser/map/network/no contacts/call-log/notifications/no clipboard/text watcher/autofill/credential APIs/no file/photo/media/camera/OCR/no attachment/no URI share/no auto-send/no permissions/no scheduler/no payment/banking/device control.
     - copy avoids unsafe guarantees: no “100%”, no “verified official”, no “official confirmed”, no “valid case”, no “safe/secure”, no “trusted”, no “accepted”, no “submitted”, no “received”, no “blocked”, no “resolved”, no “complete”, no “recovered”, no “refunded”, no “frozen”, no “protected”, no legal/financial result guarantee, no automatic monitoring/scanning/OCR/deletion/redaction/blocking/calling/messaging/lookup/verification/storage/upload/attachment/submission.
   - Add/extend source-reading tests that prove the model does not import Android or touch network/browser/map/geocoder/official lookup/contact/phone/SMS/call/notification/clipboard/autofill/credential/account/auth/crypto/media/payment/device APIs, `SafetyPlanStore`, `SafetyPlan`, `.safeWord`, `.callbackScript`, `.contacts`, `IncidentStore`, incident contents, `IncidentPreviewRedactor`, official directory entries, package manager results, or persistence mutation.
   - Add/extend UI source tests proving the new screen uses only `FamilyReadinessBuilder.build(safetyPlan.load())` to compute value-free readiness and passes only readiness category into the builder; it must not read/display/share plan literal values, incident bodies/previews, official numbers/directory entries, caller ID/call state, URLs/domains, clipboard text, autofill/account/login state, contact labels/numbers, or package/app values. The route to existing screens is allowed; the new slice must not call `safetyPlan.save(`, `safetyPlan.clear()`, `incidents.add/clear/load`, `prefs.edit`, `OfficialHelpDirectoryBuilder`, `dial`, `openHelpDirectory` is allowed only as a route action from the action router, not in the builder/share text, and no network/browser/dial/SMS APIs.
   - Add/extend source scan for official/network/phone APIs and existing privacy APIs. Explicitly forbid in the new slice and repo-wide if practical: `HttpURLConnection`, `HttpsURLConnection`, `java.net.URL`, `OkHttp`, `Retrofit`, `ktor.client`, `WebView`, `Intent.ACTION_VIEW`, `Geocoder`, `Patterns.WEB_URL`, `URLUtil`, `InetAddress`, `PhoneNumberUtils`, `Patterns.PHONE`, `OfficialHelpDirectoryBuilder` in the model/share text, `directory.entries`, `dialNumber`, `Intent.ACTION_DIAL` in the new slice, `Intent.ACTION_CALL`, `ACTION_SENDTO`, `tel:`, `smsto:`, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, `PhoneStateListener`, `CallLog`, `SmsManager`, `ContactsContract`, notification listener/manager APIs, `ClipboardManager`, `TextWatcher`, `AutofillManager`, `CredentialManager`, `AccountManager`, media/file/photo/camera/OCR APIs, `Intent.EXTRA_STREAM`, `ClipData`, URI grants, `Uri.parse`, `FileProvider`, `MediaStore`, payment/banking/NFC APIs, device-admin/accessibility/VPN/settings/package install APIs, scheduler APIs, `prefs.edit`, `putString`, `putBoolean`, `.save(` in the new slice, and any new `<uses-permission>`.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No official-number, legal-case, bank/account, caller-ID, phone-number, URL/domain, profile, institution, document, report, identity, credential, code, payment, account, device, or recovery validation/lookup.
- No network calls, web browsing, browser intents, maps/geocoding, official-number lookup, report submission, upload, cloud backup, or evidence sync.
- No dialing/calling/hang-up/call-screening/call-blocking/call-state/call-log/SMS/send-to/recipient APIs.
- No contact/address-book reading/writing/editing/picking, notification listening/monitoring/filtering, clipboard/text watcher/autofill/credential APIs, account/auth/crypto APIs, media/file/photo/storage/camera/screenshot/OCR/image analysis APIs, attachments, URI sharing, streams, file/document/photo/audio pickers, MediaStore, FileProvider, or image/audio capture.
- No display/share/use of official directory phone numbers, agency entry values, exact links/domains, case IDs, literal OTP/PIN/password/security answers, account/card/bank values, `SafetyPlan.safeWord`, callback script text, contact labels/numbers, raw analysis input, redacted clues, incident previews/bodies, package/app names, device IDs, recipient values, or exact phone/channel/profile values.
- No mutation of safety plan, incident log, official directory, contacts, notification state, call/SMS state, account/card/bank/payment state, device/app/settings state, credential status, report/submission status, or verification status from the new coach.
- No alarms, reminders, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No payment APIs, banking APIs, NFC, account-manager integration, payment cancellation, refund, account/card freeze, or recovery automation.
- No accessibility service, device admin, VPN, usage stats, package/app inventory scanning, app install/uninstall, settings mutation, app ops, or MDM behavior.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only. It must not attach files/streams or include any official/contact/channel/profile/account/payment/result value.
- Do not ask users to type exact names, contacts, phone numbers, account/card numbers, OTP/PIN, passwords, security answers, safe-word answers, callback script literals, URLs/domains, case IDs, resident IDs, device IDs, installed app/package names, official numbers, audio clip names, transcripts, screenshots with credentials, file/media names, URI/path values, call-log details, notification contents, recipient names, report/submission/result values, reset/recovery state, or raw scam text into this coach.
- Do not claim a person, institution, title, caller ID, number, link, document, case, report, payment, account, login, device, bank action, or recovery state is official, real, safe, secure, verified, valid, accepted, submitted, received, blocked, resolved, complete, recovered, refunded, frozen, protected, legal, problem-free, or automatically protected.

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
    'ClipboardManager','getPrimaryClip','setPrimaryClip','TextWatcher','addTextChangedListener','AutofillManager','CredentialManager','android.credentials','AccountManager','BiometricPrompt','KeyguardManager','KeyStore','javax.crypto',
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

Note: repo-wide scan may continue to show unchanged legacy `tel:`, `Intent.ACTION_DIAL`, `Uri.parse`, and explanatory privacy-copy mentions of OCR/audio/permissions. If so, add a bounded slice-specific source scan/test proving the new Official Pressure Reality Check slice itself does not contain network/browser/map/official lookup, dial/SMS/contact/notification, clipboard/autofill/credential/account/auth/crypto, media/file/photo/camera/OCR implementation APIs, stream/URI share, payment/device, persistence mutation, plan mutation, raw/saved secret values, exact phone/channel/profile values, official numbers, directory values, app/package values, attachments, report/submission values, or incident bodies/previews. `openHelpDirectory(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)` is allowed only in the UI action router as a route to an existing screen.

Also inspect focused JUnit XML for `OfficialPressureRealityCheckCoachTest`, `PrivacyCenterBuilderTest`, and `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-official-pressure-reality-check-coach.md
git commit -m "feat: add official pressure reality check coach"
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
