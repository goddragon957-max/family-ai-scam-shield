# FSS Android /go continuation — New Number Quarantine Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `6dea45b5a7cb5c022ba67a01179ca17c8628b80a` — `feat: add voice impersonation reality check coach`

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

Result: `BUILD SUCCESSFUL`; APK exists with `1401566` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 37 commits.

Focused JUnit XML evidence before this slice:

- `VoiceImpersonationRealityCheckCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 18/18, release 18/18, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 28/28, release 28/28, failures 0, errors 0, skipped 0.
- `FamilySafeWordResetCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.

Manifest/source invariant scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `RECORD_AUDIO`, `CAMERA`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device-admin, notification-listener, VPN, or package-install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Repo-wide source scan has no audio/voice APIs (`MediaRecorder`, `AudioRecord`, `SpeechRecognizer`, `RecognizerIntent`, `TextToSpeech`, `AudioManager`, `MediaPlayer`, `SoundPool`, `android.media`, `android.speech`), no scheduler/notification/contact/call-log/SMS/stream/media/file/camera/clipboard/text-watcher/call/payment/NFC/account-manager/device-admin/accessibility/VPN/package/network/browser/map APIs, except the unchanged legacy `Intent.ACTION_DIAL` and `Uri.parse` in `MainActivity.kt`; OCR/audio mentions are explanatory privacy copy only.
- Bounded Voice Reality Check slice scan found no forbidden audio, call-state, media, network, persistence, mutation, contact/call/SMS/notification, stream/URI share, plan/incident/official-directory, raw value, voice clip/transcript, or unsafe status APIs. It uses one manual `ACTION_SEND` + `text/plain` share helper with only `coach.familyShareSummary`.

Parent source inspection found no Voice Reality Check privacy/routing issue: it uses `VoiceImpersonationRealityCheckCoachBuilder.build(analysis=currentAnalysis, readiness=FamilyReadinessBuilder.build(safetyPlan.load()), incidentCount=incidents.count())`, shares only the builder summary, routes only to existing flows, and has privacy regression coverage for model purity, 21 entry points, route-only actions, incident count helper, safe share helper, manifest permissions, and forbidden audio/call/media/contact/network/persistence APIs.

## Product intent for next cohesive slice

Add a **New Number Quarantine Coach** screen/card. A common family voice-phishing path is: “I lost/broke my phone, this is my new number / new Kakao profile / new account; don’t call the old number; send money/OTP/install app now.” The app already has separate-channel verification and voice reality checks, but users need one concrete quarantine step: **do not save, replace, trust, or act through the new channel until it has been verified outside that channel**.

This is **not** a contact manager, dialer, SMS app, number validator, official lookup, profile verifier, spam blocker, call blocker, or address-book updater. It must not read, write, edit, insert, delete, pick, validate, normalize, store, block, mute, label, or auto-message phone numbers/contacts/profiles/channels. It should only show deterministic value-free cards and route to existing flows.

Good user story:

> “Someone says this is my family member’s new number. Before I save it or send money, show me what to do without touching my contacts or calling/texting automatically.”

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `NewNumberQuarantineCoach.kt`:
   - `NewNumberQuarantineCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): NewNumberQuarantineCoach`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction or literal plan values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, score, action plan, summary, raw pasted text, redacted clues, incident bodies/previews, `IncidentPreviewRedactor`, persisted incident contents, contact labels/numbers, `SafetyPlan.safeWord`, callback script values, account/card/OTP/PIN/password values, resident IDs, exact URLs/domains, official directory entries/numbers, app/package names, device IDs, call/SMS/contact/notification state, verification/recontact/handoff/reset/voice result values, or anything from Android contacts/telephony APIs.

2. New-number quarantine cards:
   - Fixed manual cards, for example:
     - “새 번호·새 프로필을 바로 저장하지 않기”
     - “새 채팅·통화 안에서 돈·인증·앱 이야기를 이어가지 않기”
     - “예전부터 알던 가족 경로로 직접 확인하기”
     - “가족에게는 새 번호값 대신 행동 종류만 알리기”
     - “확인 전 가족 계획·안전단어를 바꾸지 않기”
     - “확인 뒤에도 기존 계획 화면에서만 직접 정리하기”
   - Each card should include:
     - id/display order/title
     - quarantine scenario sentence to read manually
     - short value-free action sentence
     - why it matters
     - do-not-save/contact/value boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, `PHONE_NUMBER_CHANGE`, `FAMILY_EMERGENCY`, `DEEPFAKE_VOICE`, secrecy/impersonation, urgent transfer/financial/payment, credentials/OTP, link/smishing, app-install/live-call-control, saved incident, and low/in-progress/ready readiness.
   - Default/no-analysis state should be conservative: show all/broad quarantine cards and never call the new number/profile/channel trusted, verified, official, safe, secure, blocked, saved, updated, replaced, confirmed, resolved, clean, spam-free, or problem-free.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `newNumberQuarantineCoachScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard / readiness section.
     - Analysis result screen.
     - Quick Stop.
     - Active Contact Exit Scripts.
     - Safe Verification Call Scripts.
     - Family Callback card.
     - Voice Impersonation Reality Check Coach.
     - Family Safe-Word Reset Coach.
     - Separate Channel Verification Coach.
     - Verification Outcome Next Steps Board.
     - Recontact Boundary Coach.
     - Family Alert / Trusted Helper Review / Family Response Role Board / Calm Debrief / Follow-up Planner / Safety Flow Router / Privacy Center.
     - Money Transfer Pause Gate / Account Lockdown / Remote Access Cutoff / Evidence Handoff if appropriate.
   - Action buttons must delegate to existing screens/flows only; do not duplicate contact editing, dialing, SMS, verification, call scripts, safety plan editing, evidence/report/helper/account/recovery/follow-up implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** add permissions or use `ContactsContract`, contact insert/edit/delete/update/pick APIs, `Intent.ACTION_INSERT`, `Intent.ACTION_EDIT`, `ACTION_PICK`, `ACTION_GET_CONTENT`, `ACTION_OPEN_DOCUMENT`, `ACTION_SENDTO`, `smsto:`, `tel:`, `Intent.ACTION_DIAL`, `Intent.ACTION_CALL`, `TelephonyManager`, `PhoneStateListener`, `CallScreeningService`, `SmsManager`, call log, notification, clipboard, media/file/photo/camera/OCR, URI grants, attachments, `Intent.EXTRA_STREAM`, network/web/map/geocoder, `PhoneNumberUtils`, `Patterns.PHONE`, official lookup, package manager, payment/banking, device-control, alarm/scheduler/background-worker, or persistence mutation APIs.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new coach is named as local/manual/no-permission/no-network/no-contact-read/write/no address-book update/no dial/SMS/no number validation/no profile verification/no call blocking/no notification monitoring/no clipboard/no media/file/camera/OCR/no attachment/no URI share/no auto-send/no scheduler/no storage/no mutation.
   - Update `README.md` flow and add a section for the new feature.
   - Keep copy explicit that the feature routes to existing verification/family/safe-word/voice/money/account/remote/evidence screens but never proves whether a new number/profile/channel belongs to family or an official institution.

5. Tests:
   - Add `NewNumberQuarantineCoachTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default coach is conservative, recommends all quarantine cards, and does not claim the number/channel/profile is trusted/verified/official/safe/secure/blocked/saved/updated/replaced/confirmed/resolved/clean/spam-free/problem-free.
     - `PHONE_NUMBER_CHANGE`, family emergency, and deepfake voice recommend not saving/replacing the number, separate-channel verification, family callback, safe call scripts, voice reality check, family alert, safe-word reset, verification outcome next steps without exposing contact/safe-word/callback values.
     - impersonation/secrecy recommends not keeping the new channel secret, family alert/debrief, trusted helper, evidence handoff/privacy, official help/report prep as routes if useful, without official numbers or directory values.
     - financial/payment/credential/OTP signals recommend money pause/account lockdown/remote cutoff as supporting routes without claiming payment cancellation, contact blocking, account/card freeze, refund, recovery, or secret cleanup.
     - link/smishing/app-install/live-call-control recommends not continuing in the same new channel/link/app/call/screen-share session, remote cutoff/exposure/privacy routes, and no URL validation, link scan, app inventory, malware removal, remote disconnect, or device-cleaning claim.
     - saved incident changes recommendations only by count category and does not read incident contents/previews.
     - low/in-progress readiness recommends safety plan, safe-word reset, readiness roadmap, safety drill, safe call scripts; ready readiness says readiness does not prove a new channel is family.
     - every card has complete fields, stable order, unique route actions, and all action IDs map to existing flows.
     - share summary contains only generic quarantine/card/route/action labels and no raw input, redacted clue, safety plan literal, safe-word/callback/contact values, phone numbers, URLs/domains, long numbers, package/app names, account/card/OTP/PIN/resident-like values, device identifiers, official numbers, directory values, file/media names, URI/path values, screenshot/image/OCR/audio/transcription values, call/SMS/notification state, recipient names, verification/result/status values, or contact-save/update status.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no contacts/address-book read/write/edit/insert/delete/pick/no number validation/no profile verification/no dial/call/SMS/no call-log/no notifications/no clipboard/no file/photo/media/camera/OCR/no attachment/no URI share/no auto-send/no network/no official lookup/no permissions/no scheduler/no payment/banking/device control.
     - copy avoids unsafe guarantees: no “100%”, no “verified new number”, no “safe/secure”, no “official”, no “trusted”, no “blocked”, no “saved”, no “updated”, no “replaced”, no “confirmed”, no contact auto-save/update/delete/block, no payment/refund/recovery guarantee, no legal/financial result guarantee, no automatic monitoring/scanning/OCR/deletion/redaction/blocking/calling/messaging/lookup/verification/storage/upload/attachment.
   - Add/extend source-reading tests that prove the model does not import Android or touch contact/phone/SMS/call/media/network APIs, `SafetyPlanStore`, `SafetyPlan`, `.safeWord`, `.callbackScript`, `.contacts`, `IncidentStore`, incident contents, `IncidentPreviewRedactor`, official directory entries, package manager results, clipboard, notification, payment/device APIs, or persistence mutation.
   - Add/extend UI source tests proving the new screen uses only `FamilyReadinessBuilder.build(safetyPlan.load())` to compute value-free readiness and passes only readiness category into the builder; it must not read/display/share plan literal values, incident bodies/previews, call state, audio state, contact labels/numbers, new number values, or official numbers. The route to existing screens is allowed; the new slice must not call `safetyPlan.save(`, `safetyPlan.clear()`, `incidents.add/clear/load`, `prefs.edit`, contact APIs, dial/SMS APIs, or recorder/listener APIs.
   - Add/extend source scan for contact/phone/channel APIs and existing privacy APIs. Explicitly forbid in the new slice and repo-wide if practical: `ContactsContract`, `Intent.ACTION_INSERT`, `Intent.ACTION_EDIT`, `ACTION_PICK`, `ACTION_GET_CONTENT`, `ACTION_OPEN_DOCUMENT`, `ACTION_SENDTO`, `smsto:`, `tel:`, `PhoneNumberUtils`, `Patterns.PHONE`, `Intent.ACTION_DIAL` in the new slice, `Intent.ACTION_CALL`, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, `PhoneStateListener`, `CallLog`, `SmsManager`, notification listener/manager APIs, `ClipboardManager`, media/file/photo/camera/OCR APIs, `Intent.EXTRA_STREAM`, `ClipData`, URI grants, `Uri.parse`, `FileProvider`, `MediaStore`, network/browser/map APIs, `OfficialHelpDirectoryBuilder`, `PackageManager` app inventory APIs, payment/banking/NFC/account manager APIs, device-admin/accessibility/VPN/settings/package install APIs, scheduler APIs, `prefs.edit`, `putString`, `putBoolean`, `.save(` in the new slice, and any new `<uses-permission>`.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No contact/address-book reading, writing, editing, inserting, deleting, picking, labeling, merging, replacing, saving, blocking, muting, or syncing.
- No dialing/calling/hang-up/call-screening/call-blocking/call-state/call-log/SMS/send-to/recipient APIs.
- No phone-number, URL/domain, profile, official-account, identity, spam, carrier, or institution validation/lookup.
- No network calls, web browsing, maps/geocoding, official-number lookup, report submission, upload, cloud backup, or evidence sync.
- No microphone/audio/media/file/photo/storage/camera/screenshot/OCR/image analysis APIs, attachments, URI sharing, streams, file/document/photo/audio pickers, MediaStore, FileProvider, or image/audio capture.
- No display/share/use of literal `SafetyPlan.safeWord`, callback script text, contact labels/numbers, raw analysis input, redacted clues, incident previews/bodies, official directory values, package/app names, device IDs, recipient values, or exact new/old number/channel/profile values.
- No mutation of safety plan, incident log, contacts, notification state, call/SMS state, account/card/bank state, device/app/settings state, or verification status from the new coach.
- No notification listening/monitoring/filtering, clipboard reading/writing, alarms, reminders, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No payment APIs, banking APIs, NFC, account-manager integration, payment cancellation, refund, account/card freeze, or recovery automation.
- No accessibility service, device admin, VPN, usage stats, package/app inventory scanning, app install/uninstall, settings mutation, app ops, or MDM behavior.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only. It must not attach files/streams or include any phone/contact/channel/profile/secret-like value.
- Do not ask users to type exact names, contacts, new/old phone numbers, account/card numbers, OTP/PIN, passwords, safe-word answers, callback script literals, URLs/domains, resident IDs, device IDs, installed app/package names, official numbers, audio clip names, transcripts, screenshots with credentials, file/media names, URI/path values, call-log details, notification contents, recipient names, verification/result values, or raw scam text into this coach.
- Do not claim the new number/profile/channel is family, official, trusted, verified, confirmed, safe, secure, clean, spam-free, blocked, saved, updated, replaced, resolved, accepted, or automatically protected.

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
    'ContactsContract','Intent.ACTION_INSERT','Intent.ACTION_EDIT','ACTION_PICK','ACTION_GET_CONTENT','ACTION_OPEN_DOCUMENT','ACTION_SENDTO','smsto:','tel:','PhoneNumberUtils','Patterns.PHONE',
    'Intent.ACTION_CALL','Intent.ACTION_DIAL','TelecomManager','TelephonyManager','CallScreeningService','PhoneStateListener','CallLog','SmsManager',
    'MediaRecorder','AudioRecord','SpeechRecognizer','RecognizerIntent','TextToSpeech','AudioManager','MediaPlayer','SoundPool','android.media','android.speech','RECORD_AUDIO',
    'AlarmManager','NotificationManager','NotificationCompat','WorkManager','JobScheduler','CalendarContract','ForegroundService','BroadcastReceiver','POST_NOTIFICATIONS','SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED',
    'Intent.EXTRA_STREAM','ClipData','FLAG_GRANT_READ_URI_PERMISSION','FLAG_GRANT_WRITE_URI_PERMISSION','Uri.parse','FileProvider','MediaStore','ACTION_IMAGE_CAPTURE','READ_MEDIA_IMAGES','READ_EXTERNAL_STORAGE','WRITE_EXTERNAL_STORAGE','CAMERA',
    'MlKit','TextRecognition','OCR','ImageAnalysis','BitmapFactory','ExifInterface','ClipboardManager','getPrimaryClip','setPrimaryClip','TextWatcher','addTextChangedListener',
    'BillingClient','NfcAdapter','AccountManager','DevicePolicyManager','AccessibilityService','VpnService','NotificationListenerService','UsageStatsManager','PackageInstaller','deletePackage','ACTION_MANAGE_UNKNOWN_APP_SOURCES','ACTION_APPLICATION_DETAILS_SETTINGS','Settings.',
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

Note: repo-wide scan may continue to show unchanged legacy `Intent.ACTION_DIAL`, `Uri.parse`, and explanatory privacy-copy mentions of OCR/audio. If so, add a bounded slice-specific source scan/test proving the new New Number Quarantine slice itself does not contain `ACTION_DIAL`, `Uri.parse`, contact/address-book, call/SMS, notification, clipboard, media/file/photo/camera/OCR, stream/URI share, network/browser/map, official lookup, persistence mutation, plan mutation, raw/saved secret values, exact phone/channel values, official numbers, app/package values, attachments, or incident bodies/previews.

Also inspect focused JUnit XML for `NewNumberQuarantineCoachTest`, `PrivacyCenterBuilderTest`, and `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-new-number-quarantine-coach.md
git commit -m "feat: add new number quarantine coach"
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
