# FSS Android /go continuation — Credential Secret Freeze Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `c6f96e195723017c8340212ee2204796d8b5c395` — `feat: add new number quarantine coach`

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

Result: `BUILD SUCCESSFUL`; APK exists with `1420230` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 38 commits.

Focused JUnit XML evidence before this slice:

- `NewNumberQuarantineCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 19/19, release 19/19, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 29/29, release 29/29, failures 0, errors 0, skipped 0.
- `VoiceImpersonationRealityCheckCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.

Manifest/source invariant scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `RECORD_AUDIO`, `CAMERA`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device-admin, notification-listener, VPN, or package-install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Repo-wide source scan has no contact/address-book insertion/edit/pick/send-to APIs, no SMS/call-state/call-log APIs, no audio/voice APIs, no scheduler/notification/stream/media/file/camera/clipboard/text-watcher/payment/NFC/account-manager/device-admin/accessibility/VPN/package/network/browser/map APIs. Repo-wide hits are only unchanged legacy `tel:`, `Intent.ACTION_DIAL`, and `Uri.parse` in `MainActivity.kt`, plus explanatory privacy-copy mentions of `RECORD_AUDIO`/`OCR`.
- Bounded New Number Quarantine slice scan found no forbidden contact/address-book, phone/SMS/call, notification, clipboard, media/file/camera/OCR APIs, stream/URI share, network/browser/map, official lookup, persistence mutation, plan/incident raw-value, exact phone/profile/channel, or verification-status APIs. It uses one manual `ACTION_SEND` + `text/plain` share helper with only `coach.familyShareSummary`. A literal `OCR` match in the model is explanatory privacy copy only.

Parent source inspection found no New Number Quarantine privacy/routing issue: it uses `NewNumberQuarantineCoachBuilder.build(analysis=currentAnalysis, readiness=FamilyReadinessBuilder.build(safetyPlan.load()), incidentCount=incidents.count())`, shares only the builder summary, routes only to existing flows, and has privacy regression coverage for model purity, 23 entry points, route-only actions, incident count helper, safe share helper, manifest permissions, and forbidden contact/phone/SMS/media/network/persistence APIs.

## Product intent for next cohesive slice

Add a **Credential Secret Freeze Coach** screen/card. A high-risk scam often turns into: “Tell me the OTP / password / PIN / security answer / gifticon PIN / verification code so I can protect you, confirm identity, process a refund, or move money.” The app already has account lockdown and money pause flows, but users need one concrete no-secrets gate: **stop before saying, typing, pasting, screenshotting, forwarding, or reusing any credential-like secret**.

This is **not** a password manager, OTP reader, autofill tool, clipboard scanner, account authenticator, credential checker, password-strength checker, code validator, authenticator app, biometric flow, keychain/keystore integration, or account-recovery service. It must not read, store, validate, copy, paste, autofill, submit, generate, rotate, reset, clean up, revoke, delete, or sync secrets. It should only show deterministic value-free cards and route to existing flows.

Good user story:

> “Someone is asking for an OTP/PIN/password/security answer. Before I say or paste it, show me what to do without the app seeing the secret.”

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `CredentialSecretFreezeCoach.kt`:
   - `CredentialSecretFreezeCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): CredentialSecretFreezeCoach`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction or literal plan values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, score, action plan, summary, raw pasted text, redacted clues, incident bodies/previews, `IncidentPreviewRedactor`, persisted incident contents, contact labels/numbers, `SafetyPlan.safeWord`, callback script values, account/card/OTP/PIN/password/security answer values, resident IDs, exact URLs/domains, official directory entries/numbers, app/package names, device IDs, clipboard contents, autofill state, account/authentication state, call/SMS/contact/notification state, or verification/recontact/handoff/reset/voice/new-number result values.

2. Credential freeze cards:
   - Fixed manual cards, for example:
     - “OTP·PIN·비밀번호를 말하거나 붙여넣지 않기”
     - “상대가 제시한 화면·링크·앱 안에서 인증을 이어가지 않기”
     - “돈·계정·기기 요구를 먼저 멈추기”
     - “가족·보호자에게는 비밀값 대신 행동 종류만 알리기”
     - “노출됐을 수 있으면 별도 기기·공식 경로에서 직접 조치 준비하기”
     - “확인 뒤에도 비밀값을 이 앱에 적지 않고 기존 경로로 후속 점검하기”
   - Each card should include:
     - id/display order/title
     - freeze scenario sentence to read manually
     - short value-free action sentence
     - why it matters
     - do-not-enter/share/clipboard/autofill boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, `CREDENTIALS`, `GIFTICON_PIN`, `OVERSEAS_PAYMENT_ALERT`, urgent transfer/financial/payment, safe account/refinance bait, link/smishing, app-install/live-call-control, family emergency/deepfake/phone-number-change, secrecy/impersonation, saved incident, and low/in-progress/ready readiness.
   - Default/no-analysis state should be conservative: show all/broad secret-freeze cards and never call a credential, code, account, login, device, bank, payment, or recovery state safe, secure, verified, valid, invalid, correct, accepted, blocked, revoked, reset, rotated, changed, cleaned, recovered, refunded, frozen, protected, complete, resolved, or problem-free.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `credentialSecretFreezeCoachScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard / readiness section.
     - Analysis result screen.
     - Quick Stop.
     - Safe Paste Privacy Coach.
     - Active Contact Exit Scripts.
     - Money Transfer Pause Gate.
     - Account & Device Lockdown Checklist.
     - Remote Access Cutoff Coach.
     - Exposure Impact Triage.
     - Evidence Handoff Privacy Coach.
     - New Number Quarantine Coach.
     - Voice Impersonation Reality Check Coach.
     - Separate Channel Verification Coach.
     - Verification Outcome Next Steps Board.
     - Recontact Boundary Coach.
     - Family Alert / Trusted Helper Review / Family Response Role Board / Calm Debrief / Follow-up Planner / Safety Flow Router / Privacy Center.
   - Action buttons must delegate to existing screens/flows only; do not duplicate credential editing, password reset, account login/recovery, bank/payment, contact, dialing/SMS, evidence/report/helper/account/recovery/follow-up implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** add permissions or use clipboard APIs, text watchers, autofill APIs, Credential Manager, AccountManager, BiometricPrompt, KeyguardManager, KeyStore/crypto secret storage, password managers, authenticators, WebView/browser/network, URL/phone validation, file/media/camera/OCR, contacts/call/SMS/notification APIs, URI grants, attachments, `Intent.EXTRA_STREAM`, payment/banking/NFC, device-control, alarm/scheduler/background-worker, or persistence mutation APIs.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new coach is named as local/manual/no-permission/no-network/no-secret-input/no-clipboard/no-autofill/no-text-watcher/no-password-manager/no-CredentialManager/no-AccountManager/no-biometric/no-keystore/no-account-login/no-password-reset/no-code-validation/no-secret-generation/no secret storage/no contact/call/SMS/no notification monitoring/no media/file/camera/OCR/no attachment/no URI share/no auto-send/no scheduler/no payment/banking/device control/no mutation.
   - Update `README.md` flow and add a section for the new feature.
   - Keep copy explicit that the feature routes to existing account/money/remote/exposure/privacy/family/evidence/follow-up screens but never proves whether a code, account, device, bank action, payment, or recovery state is safe or complete.

5. Tests:
   - Add `CredentialSecretFreezeCoachTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default coach is conservative, recommends all secret-freeze cards, and does not claim a credential/code/account/login/device/bank/payment/recovery state is safe/secure/verified/valid/invalid/correct/accepted/blocked/revoked/reset/rotated/changed/cleaned/recovered/refunded/frozen/protected/complete/resolved/problem-free.
     - `CREDENTIALS`, `GIFTICON_PIN`, and `OVERSEAS_PAYMENT_ALERT` recommend not saying/pasting/forwarding codes, account lockdown, remote cutoff, exposure triage, evidence handoff/privacy, trusted helper, family alert, safe paste privacy, without exposing OTP/PIN/password/account/card values.
     - urgent transfer/financial/safe-account/refinance bait recommends money pause/account lockdown/remote cutoff without claiming payment cancellation, account/card freeze, refund, recovery, revocation, reset, or secret cleanup.
     - link/smishing/app-install/live-call-control recommends not continuing in the same link/app/call/screen-share session, remote cutoff/exposure/privacy routes, and no URL validation, link scan, app inventory, malware removal, remote disconnect, or device-cleaning claim.
     - family emergency/deepfake/phone-number-change recommends separate-channel verification, family callback, voice reality check, new-number quarantine, safe-word reset, safe call scripts, family alert without exposing contact/safe-word/callback values.
     - secrecy/impersonation recommends not keeping the request secret, family alert/debrief, trusted helper, evidence handoff/privacy, official help/report prep if useful, without official numbers or directory values.
     - saved incident changes recommendations only by count category and does not read incident contents/previews.
     - low/in-progress readiness recommends safety plan, safe-word reset, readiness roadmap, safety drill, safe call scripts; ready readiness says readiness does not make a secret request safe.
     - every card has complete fields, stable order, unique route actions, and all action IDs map to existing flows.
     - share summary contains only generic freeze/card/route/action labels and no raw input, redacted clue, safety plan literal, safe-word/callback/contact values, phone numbers, URLs/domains, long numbers, package/app names, account/card/OTP/PIN/password/security answer/resident-like values, device identifiers, official numbers, directory values, file/media names, URI/path values, screenshot/image/OCR/audio/transcription values, clipboard/autofill/account/login state, call/SMS/notification state, recipient names, verification/result/status values, or reset/recovery status.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no secret input/no clipboard/no text watcher/no autofill/no password manager/no CredentialManager/no AccountManager/no biometric/no keystore/no account login/no password reset/no code validation/no secret generation/no contacts/call-log/SMS/notifications/no file/photo/media/camera/OCR/no attachment/no URI share/no auto-send/no network/no official lookup/no permissions/no scheduler/no payment/banking/device control.
     - copy avoids unsafe guarantees: no “100%”, no “verified code”, no “valid OTP”, no “safe/secure”, no “accepted”, no “blocked”, no “revoked”, no “reset”, no “rotated”, no “changed”, no “cleaned”, no “recovered”, no “refunded”, no “frozen”, no “protected”, no account/card auto-freeze, no payment/refund/recovery guarantee, no legal/financial result guarantee, no automatic monitoring/scanning/OCR/deletion/redaction/blocking/calling/messaging/lookup/verification/storage/upload/attachment.
   - Add/extend source-reading tests that prove the model does not import Android or touch clipboard/autofill/credential/account/auth/crypto/contact/phone/SMS/call/media/network APIs, `SafetyPlanStore`, `SafetyPlan`, `.safeWord`, `.callbackScript`, `.contacts`, `IncidentStore`, incident contents, `IncidentPreviewRedactor`, official directory entries, package manager results, payment/device APIs, or persistence mutation.
   - Add/extend UI source tests proving the new screen uses only `FamilyReadinessBuilder.build(safetyPlan.load())` to compute value-free readiness and passes only readiness category into the builder; it must not read/display/share plan literal values, incident bodies/previews, clipboard text, EditText secret values, autofill/account/login state, call state, audio state, contact labels/numbers, official numbers, or package/app values. The route to existing screens is allowed; the new slice must not call `safetyPlan.save(`, `safetyPlan.clear()`, `incidents.add/clear/load`, `prefs.edit`, `ClipboardManager`, `TextWatcher`, `AutofillManager`, `CredentialManager`, `AccountManager`, `BiometricPrompt`, `KeyStore`, payment, contact, dial/SMS, or recorder/listener APIs.
   - Add/extend source scan for secret/credential APIs and existing privacy APIs. Explicitly forbid in the new slice and repo-wide if practical: `ClipboardManager`, `getPrimaryClip`, `setPrimaryClip`, `TextWatcher`, `addTextChangedListener`, `AutofillManager`, `AutofillValue`, `android.view.autofill`, `CredentialManager`, `android.credentials`, `AccountManager`, `BiometricPrompt`, `KeyguardManager`, `KeyStore`, `javax.crypto`, `MessageDigest`, password manager/authenticator APIs, `ContactsContract`, `Intent.ACTION_INSERT`, `Intent.ACTION_EDIT`, `ACTION_PICK`, `ACTION_GET_CONTENT`, `ACTION_OPEN_DOCUMENT`, `ACTION_SENDTO`, `smsto:`, `tel:`, `PhoneNumberUtils`, `Patterns.PHONE`, `Intent.ACTION_DIAL` in the new slice, `Intent.ACTION_CALL`, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, `PhoneStateListener`, `CallLog`, `SmsManager`, notification listener/manager APIs, media/file/photo/camera/OCR APIs, `Intent.EXTRA_STREAM`, `ClipData`, URI grants, `Uri.parse`, `FileProvider`, `MediaStore`, network/browser/map APIs, `OfficialHelpDirectoryBuilder`, `PackageManager` app inventory APIs, payment/banking/NFC APIs, device-admin/accessibility/VPN/settings/package install APIs, scheduler APIs, `prefs.edit`, `putString`, `putBoolean`, `.save(` in the new slice, and any new `<uses-permission>`.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No credential/secret input fields, reading, storing, copying, pasting, validating, generating, rotating, resetting, revoking, deleting, syncing, cleaning, recovering, or submitting.
- No clipboard, text watcher, autofill, Credential Manager, AccountManager, biometric, keyguard, keystore, crypto secret storage, password manager, authenticator, account login, or password-reset APIs.
- No display/share/use of literal OTP/PIN/password/security answers, account/card/bank values, `SafetyPlan.safeWord`, callback script text, contact labels/numbers, raw analysis input, redacted clues, incident previews/bodies, official directory values, package/app names, device IDs, recipient values, exact URLs/domains, or exact phone/channel/profile values.
- No contact/address-book reading/writing/editing/picking, dialing/calling/hang-up/call-screening/call-blocking/call-state/call-log/SMS/send-to/recipient APIs.
- No phone-number, URL/domain, profile, official-account, identity, credential, code, payment, account, or institution validation/lookup.
- No network calls, web browsing, maps/geocoding, official-number lookup, report submission, upload, cloud backup, or evidence sync.
- No microphone/audio/media/file/photo/storage/camera/screenshot/OCR/image analysis APIs, attachments, URI sharing, streams, file/document/photo/audio pickers, MediaStore, FileProvider, or image/audio capture.
- No mutation of safety plan, incident log, contacts, notification state, call/SMS state, account/card/bank state, payment state, device/app/settings state, credential status, or verification status from the new coach.
- No notification listening/monitoring/filtering, alarms, reminders, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No payment APIs, banking APIs, NFC, account-manager integration, payment cancellation, refund, account/card freeze, or recovery automation.
- No accessibility service, device admin, VPN, usage stats, package/app inventory scanning, app install/uninstall, settings mutation, app ops, or MDM behavior.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only. It must not attach files/streams or include any credential/secret/contact/channel/profile/account/payment/result value.
- Do not ask users to type exact names, contacts, phone numbers, account/card numbers, OTP/PIN, passwords, security answers, safe-word answers, callback script literals, URLs/domains, resident IDs, device IDs, installed app/package names, official numbers, audio clip names, transcripts, screenshots with credentials, file/media names, URI/path values, call-log details, notification contents, recipient names, verification/result values, reset/recovery state, or raw scam text into this coach.
- Do not claim a credential, code, account, login, device, bank action, payment, or recovery state is safe, secure, verified, valid, invalid, correct, accepted, blocked, revoked, reset, rotated, changed, cleaned, recovered, refunded, frozen, protected, complete, resolved, problem-free, or automatically protected.

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
    'ClipboardManager','getPrimaryClip','setPrimaryClip','TextWatcher','addTextChangedListener','AutofillManager','AutofillValue','android.view.autofill','CredentialManager','android.credentials','AccountManager','BiometricPrompt','KeyguardManager','KeyStore','javax.crypto','MessageDigest',
    'ContactsContract','Intent.ACTION_INSERT','Intent.ACTION_EDIT','ACTION_PICK','ACTION_GET_CONTENT','ACTION_OPEN_DOCUMENT','ACTION_SENDTO','smsto:','tel:','PhoneNumberUtils','Patterns.PHONE',
    'Intent.ACTION_CALL','Intent.ACTION_DIAL','TelecomManager','TelephonyManager','CallScreeningService','PhoneStateListener','CallLog','SmsManager',
    'MediaRecorder','AudioRecord','SpeechRecognizer','RecognizerIntent','TextToSpeech','AudioManager','MediaPlayer','SoundPool','android.media','android.speech','RECORD_AUDIO',
    'AlarmManager','NotificationManager','NotificationCompat','WorkManager','JobScheduler','CalendarContract','ForegroundService','BroadcastReceiver','POST_NOTIFICATIONS','SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED',
    'Intent.EXTRA_STREAM','ClipData','FLAG_GRANT_READ_URI_PERMISSION','FLAG_GRANT_WRITE_URI_PERMISSION','Uri.parse','FileProvider','MediaStore','ACTION_IMAGE_CAPTURE','READ_MEDIA_IMAGES','READ_EXTERNAL_STORAGE','WRITE_EXTERNAL_STORAGE','CAMERA',
    'MlKit','TextRecognition','OCR','ImageAnalysis','BitmapFactory','ExifInterface','BillingClient','NfcAdapter','DevicePolicyManager','AccessibilityService','VpnService','NotificationListenerService','UsageStatsManager','PackageInstaller','deletePackage','ACTION_MANAGE_UNKNOWN_APP_SOURCES','ACTION_APPLICATION_DETAILS_SETTINGS','Settings.',
    'getInstalledPackages','getInstalledApplications','HttpURLConnection','OkHttp','WebView','Geocoder','ACTION_VIEW'
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

Note: repo-wide scan may continue to show unchanged legacy `tel:`, `Intent.ACTION_DIAL`, `Uri.parse`, and explanatory privacy-copy mentions of OCR/audio/permissions. If so, add a bounded slice-specific source scan/test proving the new Credential Secret Freeze slice itself does not contain clipboard/autofill/credential/account/auth/crypto, contact/address-book, call/SMS, notification, media/file/photo/camera/OCR, stream/URI share, network/browser/map, official lookup, payment/device, persistence mutation, plan mutation, raw/saved secret values, exact phone/channel/profile values, official numbers, app/package values, attachments, or incident bodies/previews.

Also inspect focused JUnit XML for `CredentialSecretFreezeCoachTest`, `PrivacyCenterBuilderTest`, and `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-credential-secret-freeze-coach.md
git commit -m "feat: add credential secret freeze coach"
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
