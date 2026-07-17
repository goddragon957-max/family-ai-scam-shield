# FSS Android /go continuation — Voice Impersonation Reality Check Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `b8d654852d430abc87733859a64a46d5006ab7ea` — `feat: add family safe-word reset coach`

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

Result: `BUILD SUCCESSFUL`; APK exists with `1380520` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 36 commits.

Focused JUnit XML evidence before this slice:

- `FamilySafeWordResetCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 17/17, release 17/17, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 27/27, release 27/27, failures 0, errors 0, skipped 0.
- `EvidenceHandoffPrivacyCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.

Manifest/source invariant scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `RECORD_AUDIO`, `CAMERA`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device admin, notification listener, VPN, or package install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Repo-wide source scan has no scheduler, notification, contact, call-log, SMS, stream/URI share, media/photo/file picker, camera, clipboard, text watcher, `Intent.ACTION_CALL`, telecom/telephony/call-screening, payment/NFC/account-manager, device-admin/accessibility/VPN/usage-stats/package-installer/settings, installed-package, network/browser/map/geocoder, or `ACTION_VIEW` APIs.
- Repo-wide source scan still has one unchanged legacy `Intent.ACTION_DIAL` and `Uri.parse` reference in `MainActivity.kt`; bounded scans proved the new Safe-Word Reset slice itself does not contain `ACTION_DIAL`, `Uri.parse`, contact/call/SMS/notification/media/file/photo/camera/OCR APIs, stream/URI share APIs, network/browser/map APIs, official lookup, persistence mutation, safe-word reset storage, plan mutation, raw/saved secret values, attachments, or raw/saved incident values. OCR mentions in the repo are explanatory privacy copy.

Parent source inspection found no Safe-Word Reset privacy/routing issue: it uses `FamilySafeWordResetCoachBuilder.build(analysis=currentAnalysis, readiness=FamilyReadinessBuilder.build(safetyPlan.load()), incidentCount=incidents.count())`, shares only `coach.familyShareSummary` through manual `ACTION_SEND`/`text/plain`, routes to existing plan/drill/verification/family/privacy flows, does not read or mutate actual plan values, and has bounded privacy regression tests covering model, UI routes, share helper, incident count helper, manifest permissions, forbidden APIs, and persistence invariants.

## Product intent for next cohesive slice

Add a **Voice Impersonation Reality Check Coach** screen/card. The app is for family AI voice-phishing, but users still need a voice-specific moment of doubt: when a caller sounds exactly like family or a familiar official, the app should help them stop treating the voice as proof, avoid feeding the caller more voice samples or secrets, and move to existing verification/family/safe-word/money/account/evidence flows.

This is **not** a voice detector, deepfake classifier, speaker verifier, recorder, speech recognizer, call listener, audio player, transcription tool, microphone tool, media picker, or upload flow. It must not request or use microphone/audio/recording/media permissions or APIs. It must not ask the user to record, upload, replay, transcribe, compare, store, or share voice clips. It should only show deterministic value-free cards and route to existing flows.

Good user story:

> “The voice sounded like my child/parent. Before I trust it, show me what to do without recording the call or sending voice clips.”

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `VoiceImpersonationRealityCheckCoach.kt`:
   - `VoiceImpersonationRealityCheckCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): VoiceImpersonationRealityCheckCoach`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction or literal plan values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, score, action plan, summary, raw pasted text, redacted clues, incident bodies/previews, `IncidentPreviewRedactor`, persisted incident contents, phone/contact values, URLs/domains, official numbers, safety-plan literal values, account/card values, OTP/PIN/passwords, resident IDs, device IDs, app/package names, installed app lists, call/SMS/contact/notification/media/file/audio state, verification/recontact/handoff/reset result values, or anything from official directory entries.

2. Voice reality-check cards:
   - Fixed manual cards, for example:
     - “익숙한 목소리도 증거로 단정하지 않기”
     - “통화 안에서 새 질문·답·돈 이야기를 이어가지 않기”
     - “목소리 대신 평소 가족 경로로 다시 확인하기”
     - “목소리 샘플·녹음·스크린샷을 앱에 넣지 않기”
     - “돈·인증·앱 설치 요구가 붙으면 먼저 멈추기”
     - “가족에게는 목소리값 대신 행동 종류만 알리기”
   - Each card should include:
     - id/display order/title
     - reality-check scenario sentence to read manually
     - short value-free action sentence
     - why it matters
     - do-not-record / do-not-share / do-not-ask boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, `FAMILY_EMERGENCY`, `DEEPFAKE_VOICE`, `PHONE_NUMBER_CHANGE`, secrecy/impersonation, urgent transfer/financial/payment, credentials/OTP, link/smishing, app-install/live-call-control, saved incident, and low/in-progress/ready readiness.
   - Default/no-analysis state should be conservative: show all/broad voice cards and never call the voice real, fake, cloned, verified, matched, recognized, identified, safe, secure, official, resolved, blocked, recorded, analyzed, classified, complete, or problem-free.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `voiceImpersonationRealityCheckCoachScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard near family callback / quick stop / safe-word reset / safety plan / evidence.
     - Analysis result screen.
     - Quick Stop.
     - Safe verification call scripts.
     - Family callback card.
     - Family safe-word reset coach.
     - Separate channel verification coach.
     - Verification outcome next steps board.
     - Recontact boundary coach.
     - Family alert / trusted helper review / family response role board / calm debrief / follow-up planner / safety flow router / privacy center if appropriate.
     - Money transfer pause gate, account lockdown, remote access cutoff, evidence handoff if appropriate.
   - Action buttons must delegate to existing screens/flows only; do not duplicate voice analysis, call handling, safety plan editing, contact storage, safe-call scripts, evidence/report/helper/account/recovery/follow-up implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** add `RECORD_AUDIO`, microphone, audio recording/playback/transcription/speech recognition, call-state/listener, media/file/photo picker, URI grants, attachments, `Intent.EXTRA_STREAM`, clipboard, contact, call-log, SMS, notification, network, web, official lookup, payment/banking, device-control, alarm/scheduler/background-worker APIs.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new coach is named as local/manual/no-permission/no-network/no-microphone/no-audio-recording/no-call-listening/no-voice-analysis/no-speaker-verification/no-transcription/no-media/file read/no attachment/no URI share/no contacts/calls/SMS/clipboard/notification/no auto-send/no scheduler/no storage/no mutation.
   - Update `README.md` flow and add a section for the new feature.
   - Keep copy explicit that the feature routes to existing verification/family/safe-word/money/account/evidence screens but never proves whether a voice is real or fake.

5. Tests:
   - Add `VoiceImpersonationRealityCheckCoachTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default coach is conservative, recommends all voice cards, and does not claim the voice is real/fake/cloned/verified/matched/recognized/identified/analyzed/classified/safe/secure/official/resolved/blocked/complete.
     - family emergency/deepfake/phone-number-change recommends voice-is-not-proof, family callback, separate-channel verification, safe call scripts, family alert, safe-word reset, verification outcome next steps without exposing safe-word/callback/contact values.
     - impersonation/secrecy recommends not keeping the call secret, family alert/debrief, trusted helper, evidence handoff/privacy, official help/report prep as routes if useful, without official numbers or directory values.
     - financial/payment/credential/OTP signals recommend money pause/account lockdown/remote cutoff as supporting routes without claiming payment cancellation, account/card freeze, refund, recovery, or secret cleanup.
     - link/smishing/app-install/live-call-control recommends not continuing in the same link/app/call/screen-share session, remote cutoff/exposure/privacy routes, and no URL validation, link scan, app inventory, malware removal, remote disconnect, or device-cleaning claim.
     - saved incident changes recommendations only by count category and does not read incident contents/previews.
     - low/in-progress readiness recommends safety plan, safe-word reset, readiness roadmap, safety drill, safe call scripts; ready readiness says readiness does not prove the caller is real.
     - every card has complete fields, stable order, unique route actions, and all action IDs map to existing flows.
     - share summary contains only generic voice-check/card/route/action labels and no raw input, redacted clue, safety plan literal, safe-word/callback/contact values, phone numbers, URLs/domains, long numbers, package/app names, account/card/OTP/PIN/resident-like values, device identifiers, official numbers, directory values, file/media/audio names, URI/path values, screenshot/image/OCR/voice-analysis/transcription values, call/SMS/notification state, recipient names, or result/status values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no microphone/no voice/audio recording/no call listening/no speech recognition/no transcription/no speaker verification/no voice matching/no contacts/call-log/SMS/clipboard/notification/file/photo/media/camera/OCR/no attachment/no URI share/no auto-send/no network/no official lookup/no number validation/no link validation/no permissions/no scheduler/no payment/banking/device control.
     - copy avoids unsafe guarantees: no “100%”, no “safe/secure”, no “real voice”, no “fake voice”, no “deepfake detected”, no “speaker matched”, no “voice verified”, no “recognized”, no “classified”, no “analyzed”, no account/card auto-freeze, no payment/refund/recovery guarantee, no legal/financial result guarantee, no automatic monitoring/recording/scanning/OCR/deletion/redaction/blocking/calling/messaging/lookup/verification/storage/upload/attachment.
   - Add/extend source-reading tests that prove the model does not import Android or touch audio/call/media APIs, `SafetyPlanStore`, `SafetyPlan`, `.safeWord`, `.callbackScript`, `.contacts`, `IncidentStore`, incident contents, `IncidentPreviewRedactor`, official directory entries, contact numbers, package manager results, clipboard, contact/call/SMS/notification/media/file/camera/OCR/network/payment/device APIs, or persistence mutation.
   - Add/extend UI source tests proving the new screen uses only `FamilyReadinessBuilder.build(safetyPlan.load())` to compute value-free readiness and passes only readiness category into the builder; it must not read/display/share plan literal values, incident bodies/previews, call state, audio state, or contact values. The route to existing screens is allowed; the new slice must not call `safetyPlan.save(`, `safetyPlan.clear()`, `incidents.add/clear/load`, `prefs.edit`, or any recorder/listener APIs.
   - Add/extend source scan for audio/voice APIs and existing privacy APIs. Explicitly forbid in the new slice and repo-wide if practical: `RECORD_AUDIO`, `MediaRecorder`, `AudioRecord`, `SpeechRecognizer`, `RecognizerIntent`, `TextToSpeech`, `AudioManager`, `MediaPlayer`, `SoundPool`, `android.media`, `android.speech`, microphone APIs, call-state APIs, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `Intent.ACTION_CALL`, `Intent.ACTION_DIAL` in the new slice, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, `PhoneStateListener`, `CallLog`, `ContactsContract`, `SmsManager`, notification listener/manager APIs, `ClipboardManager`, media/file/photo/camera/OCR APIs, `Intent.EXTRA_STREAM`, `ClipData`, URI grants, `Uri.parse`, `FileProvider`, `MediaStore`, network/browser/map APIs, `OfficialHelpDirectoryBuilder`, `PackageManager` app inventory APIs, payment/banking/NFC/account manager APIs, device-admin/accessibility/VPN/settings/package install APIs, scheduler APIs, `prefs.edit`, `putString`, `putBoolean`, `.save(` in the new slice, and any new `<uses-permission>`.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No microphone, audio recording, audio playback, speech recognition, transcription, speaker verification, voice matching, deepfake detection/classification, call listening, call-state monitoring, or call recording APIs.
- No audio/media/file/photo/storage/camera/screenshot/OCR/image analysis APIs, attachments, URI sharing, streams, file/document/photo/audio pickers, MediaStore, FileProvider, or image/audio capture.
- No display/share/use of literal `SafetyPlan.safeWord`, callback script text, contact labels/numbers, raw analysis input, redacted clues, incident previews/bodies, official directory values, package/app names, device IDs, audio file names, transcripts, or recipient values.
- No mutation of safety plan or incident log from the new coach.
- No contact reading/writing/picking, call-log reading, auto dial/call/hang-up/call screening/blocking/call state, SMS send/read, recipient choosing, notification listening/monitoring/filtering, or clipboard reading/writing.
- No network calls, web browsing, maps/geocoding, official-number lookup, phone-number validation, URL/domain validation, link scanning, identity verification, official confirmation, report submission, upload, cloud backup, or evidence sync.
- No accessibility service, device admin, VPN, usage stats, package/app inventory scanning, app install/uninstall, settings mutation, app ops, or MDM behavior.
- No payment APIs, banking APIs, NFC, account-manager integration, payment cancellation, refund, account/card freeze, or recovery automation.
- No alarms, notifications, calendar events, reminders, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No new persistence for voice-check choices, progress, reads, shares, names, assignments, checkmarks, notes, clipboard contents, audio names, transcripts, recipients, sent status, caller status, voice-real/fake status, contact/channel values, official numbers, package names, device status, reminders, or history.
- Opening the screen or pressing route/share buttons must not add/modify/delete incident records or safety-plan values.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only. It must not attach files/streams/audio or include any voice clip/transcript/secret-like value.
- Do not ask users to type names, contacts, exact phone numbers, account/card numbers, OTP/PIN, passwords, safe-word answers, callback script literals, URLs/domains, resident IDs, device IDs, installed app/package names, official numbers, audio clip names, transcripts, screenshots with credentials, file/media names, URI/path values, call-log details, notification contents, recipient names, result values, or raw scam text into this coach.
- Do not claim the voice is real, fake, cloned, detected, analyzed, classified, matched, recognized, identified, safe, secure, verified, officially confirmed, complete, accepted, blocked, or automatically protected.

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
for token in ['CALL_PHONE','READ_SMS','READ_CALL_LOG','READ_CONTACTS','WRITE_CONTACTS','READ_PHONE_STATE','ANSWER_PHONE_CALLS','RECORD_AUDIO','CAMERA','READ_MEDIA_IMAGES','READ_MEDIA_VIDEO','READ_EXTERNAL_STORAGE','WRITE_EXTERNAL_STORAGE','INTERNET','ACCESS_FINE_LOCATION','ACCESS_COARSE_LOCATION','POST_NOTIFICATIONS','SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED','PACKAGE_USAGE_STATS','QUERY_ALL_PACKAGES','BIND_ACCESSIBILITY_SERVICE','BIND_DEVICE_ADMIN','BIND_NOTIFICATION_LISTENER_SERVICE','BIND_VPN_SERVICE','REQUEST_INSTALL_PACKAGES']:
    print(f'{token}={token in manifest}')
needle='android:allowBackup="false"'
print(f'allowBackup=false={needle in manifest}')
print(f'uses-permission-tag={"<uses-permission" in manifest}')
needles = [
    'MediaRecorder','AudioRecord','SpeechRecognizer','RecognizerIntent','TextToSpeech','AudioManager','MediaPlayer','SoundPool','android.media','android.speech','RECORD_AUDIO',
    'AlarmManager','NotificationManager','NotificationCompat','WorkManager','JobScheduler',
    'CalendarContract','ForegroundService','BroadcastReceiver','POST_NOTIFICATIONS',
    'SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED','ContactsContract','CallLog','SmsManager',
    'Intent.EXTRA_STREAM','ClipData','FLAG_GRANT_READ_URI_PERMISSION','FLAG_GRANT_WRITE_URI_PERMISSION',
    'Uri.parse','FileProvider','MediaStore','ACTION_IMAGE_CAPTURE','ACTION_GET_CONTENT','ACTION_OPEN_DOCUMENT','ACTION_PICK',
    'READ_MEDIA_IMAGES','READ_EXTERNAL_STORAGE','WRITE_EXTERNAL_STORAGE','CAMERA',
    'MlKit','TextRecognition','OCR','ImageAnalysis','BitmapFactory','ExifInterface',
    'ClipboardManager','getPrimaryClip','setPrimaryClip','TextWatcher','addTextChangedListener',
    'Intent.ACTION_CALL','Intent.ACTION_DIAL','TelecomManager','TelephonyManager','CallScreeningService','PhoneStateListener',
    'BillingClient','NfcAdapter','AccountManager','DevicePolicyManager','AccessibilityService','VpnService',
    'NotificationListenerService','UsageStatsManager','PackageInstaller','deletePackage',
    'ACTION_MANAGE_UNKNOWN_APP_SOURCES','ACTION_APPLICATION_DETAILS_SETTINGS','Settings.',
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

Note: repo-wide scan may continue to show unchanged legacy `Intent.ACTION_DIAL`, `Uri.parse`, and explanatory privacy-copy mentions of OCR/audio. If so, add a bounded slice-specific source scan/test proving the new Voice Impersonation slice itself does not contain `ACTION_DIAL`, `Uri.parse`, microphone/audio/call-listener/recorder/speech/media/file/photo/camera/OCR/contact/call/SMS/notification APIs, stream/URI share APIs, network/browser/map APIs, official lookup, persistence mutation, voice status storage, plan mutation, raw/saved secret values, attachments, audio names/transcripts, or raw/saved incident values.

Also inspect focused JUnit XML for the new test and key privacy tests, e.g. `VoiceImpersonationRealityCheckCoachTest`, `PrivacyCenterBuilderTest`, `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-voice-impersonation-reality-check-coach.md
git commit -m "feat: add voice impersonation reality check coach"
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

## Documentation convention for this slice

- The user-facing Korean feature name is **목소리 사칭 현실 점검 코치**; documentation and Privacy Center inventory use this name consistently with `VoiceImpersonationRealityCheckCoachBuilder`.
- README and Privacy Center copy describe this as local, manual, stateless, value-free routing to existing verification/family/safe-word/money/account/evidence flows. They must not be read as evidence that a voice is real or synthetic, and final build/test/scan/commit evidence is reported only after the verification gate finishes.
