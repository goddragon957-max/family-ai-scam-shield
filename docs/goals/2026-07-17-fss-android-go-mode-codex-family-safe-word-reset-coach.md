# FSS Android /go continuation — Family Safe-Word Reset Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `45fddb5670869874e5b6be817264496cc5c52cc1` — `feat: add evidence handoff privacy coach`

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

Result: `BUILD SUCCESSFUL`; APK exists with `1362089` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 35 commits.

Focused JUnit XML evidence before this slice:

- `EvidenceHandoffPrivacyCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 16/16, release 16/16, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 26/26, release 26/26, failures 0, errors 0, skipped 0.
- `RecontactBoundaryCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.

Manifest/source invariant scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `RECORD_AUDIO`, `CAMERA`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device admin, notification listener, VPN, or package install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Repo-wide source scan has no scheduler, notification, contact, call-log, SMS, stream/URI share, media/photo/file picker, camera, clipboard, text watcher, `Intent.ACTION_CALL`, telecom/telephony/call-screening, payment/NFC/account-manager, device-admin/accessibility/VPN/usage-stats/package-installer/settings, installed-package, network/browser/map/geocoder, or `ACTION_VIEW` APIs.
- Repo-wide source scan still has one unchanged legacy `Intent.ACTION_DIAL` reference in `MainActivity.kt`; the parent slice scan proved the Evidence Handoff slice itself does not contain `ACTION_DIAL`, `ACTION_CALL`, media/file/photo/camera/OCR APIs, stream/URI share APIs, `CallLog`, contact APIs, call-screening APIs, notification APIs, network/browser/map APIs, official lookup, persistence mutation, handoff storage, attachments, or raw/saved values.

Parent source inspection found no Evidence Handoff privacy/routing issue: it uses `EvidenceHandoffPrivacyCoachBuilder.build(analysis=currentAnalysis, readiness=FamilyReadinessBuilder.build(safetyPlan.load()), incidentCount=incidents.count())`, shares only `coach.familyShareSummary` through manual `ACTION_SEND`/`text/plain`, documents the feature in README and Privacy Center, and has bounded privacy regression tests covering model, UI routes, share helper, incident count helper, manifest permissions, forbidden APIs, and persistence invariants.

## Product intent for next cohesive slice

Add a **Family Safe-Word Reset Coach** screen/card. In family/deepfake/number-change scams, the existing family safe word or callback phrase may have been spoken, screenshotted, overheard, typed into chat, or simply made suspect. Families then need a calm, value-free rotation plan: treat the old secret as not enough, pick a new offline family check, update the safety plan manually, rehearse without typing the real answer into the coach, and tell helpers what category changed without exposing the new value.

This is **not** a password manager, secret generator, contact manager, notification/reminder, scheduler, SMS/call tool, clipboard tool, device security scanner, identity verification service, or automatic safety-plan migration. It must not display, copy, transform, store, share, validate, generate, rotate, delete, or infer real safe-word answers, callback scripts, contact names, phone numbers, account values, OTP/PIN/passwords, URLs/domains, official numbers, or incident content. It should only show deterministic cards and route to existing flows, especially `Screen.PLAN`, readiness roadmap, safety drill, callback/verification, family alert, privacy center, and other existing response screens.

Good user story:

> “If I think our family safe word or callback phrase might be exposed, help us reset the routine without making us type the actual secret here or sending it in chat.”

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `FamilySafeWordResetCoach.kt`:
   - `FamilySafeWordResetCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): FamilySafeWordResetCoach`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction or literal plan values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, score, action plan, summary, raw pasted text, redacted clues, incident bodies/previews, `IncidentPreviewRedactor`, persisted incident contents, phone/contact values, URLs/domains, official numbers, safety-plan literal values (`safeWord`, callback script text, contact labels/numbers), account/card values, OTP/PIN/passwords, resident IDs, device IDs, app/package names, installed app lists, call/SMS/contact/notification/media/file state, verification/recontact/handoff result values, or anything from official directory entries.

2. Safe-word reset cards:
   - Fixed manual cards, for example:
     - “이전 안전단어는 잠시 믿지 않기”
     - “새 확인 질문은 같은 채팅·통화 안에서 정하지 않기”
     - “가족 안전 계획은 직접 열어 새 값으로 바꾸기”
     - “가족에게는 새 값 대신 바뀐 규칙만 말하기”
     - “새 번호·딥페이크·비밀 압박 뒤에는 별도 경로로 다시 확인하기”
     - “연습은 가상 문장으로만 하고 실제 답은 말하지 않기”
   - Each card should include:
     - id/display order/title
     - reset scenario sentence to read manually
     - value-free family sentence
     - why it matters
     - do-not-say / do-not-record / do-not-share boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, family emergency/deepfake/phone-number-change, secrecy/impersonation, urgent transfer/financial/payment, credentials/OTP, link/smishing, app-install/remote-control/live-call-control, saved incident, and low/in-progress/ready readiness.
   - Default/no-analysis state should be conservative: show all/broad reset cards and never call the old word invalidated, new word created, rotated, secure, safe, verified, confirmed, updated, synced, delivered, complete, or problem-free.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `familySafeWordResetCoachScreen()`.
   - Add route buttons from relevant places:
     - Home readiness/dashboard area near “가족 안전 계획”, “가족 준비 로드맵”, “가족 안전 연습 코치”.
     - Safety plan screen (`Screen.PLAN`) as a helper before/after manual edits, without auto-mutating plan values.
     - Analysis result screen.
     - Quick Stop.
     - Safe verification call scripts.
     - Family alert message pack.
     - Family callback card area if appropriate.
     - Separate channel verification coach.
     - Verification outcome next steps board.
     - Recontact boundary coach.
     - Trusted helper review / family response role board / calm debrief / follow-up planner / safety flow router / privacy center if appropriate.
   - Action buttons must delegate to existing screens/flows only; do not duplicate the safety plan editor, contact storage, safe-call scripts, official directory, evidence, report, helper review, account, recovery, or follow-up implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** add any generator or validator for safe words; do not read or write clipboard; do not prefill the safety plan; do not create reminders; do not add notifications/alarms/schedulers/background workers; do not read contacts, call log, SMS, notifications, files, media, screenshots, or incident bodies; do not auto-send or auto-select recipients.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new coach is named as local/manual/no-permission/no-network/no-storage/no-new-safety-plan-storage/no-secret-generation/no-secret-validation/no-secret-copy/no-auto-rotation/no-plan-mutation/no contacts/calls/SMS/clipboard/notification/file/media/camera/OCR/no auto-send/no scheduler/no mutation.
   - Update `README.md` flow and add a section for the new feature.
   - Keep copy explicit that the feature routes to `Screen.PLAN` for the user to manually edit if they choose, but the coach itself does not open with, display, share, or mutate literal plan values.

5. Tests:
   - Add `FamilySafeWordResetCoachTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default coach is conservative, recommends all reset cards, and does not claim any safe word was changed, invalidated, verified, generated, delivered, synced, complete, safe, or secure.
     - family emergency/deepfake/phone-number-change recommends “do not trust the old phrase alone”, separate-channel verification, safe call scripts, family alert, callback card, verification outcome next steps, safety plan, drill/roadmap routes without exposing safe-word/callback/contact values.
     - impersonation/secrecy recommends not keeping the reset secret, family alert/debrief, trusted helper, evidence handoff/privacy, official help/report prep as routes if useful, without official numbers or directory values.
     - financial/payment/credential/OTP signals recommend money pause/account lockdown/remote cutoff as supporting routes without claiming account/card freeze, payment cancellation, or secret cleanup.
     - link/smishing/app-install/live-call-control recommends not sending the new secret through the same link/app/session, remote cutoff/exposure/privacy routes, and no URL validation, link scan, OCR, app inventory, or device-cleaning claim.
     - saved incident changes recommendations only by count category and does not read incident contents/previews.
     - low/in-progress readiness recommends creating/updating plan, readiness roadmap, safety drill, safe call scripts; ready readiness recommends rotation rehearsal while stating readiness does not prove the secret is uncompromised.
     - every card has complete fields, stable order, unique route actions, and all action IDs map to existing flows.
     - share summary contains only generic reset/card/route/action labels and no raw input, redacted clue, safety plan literal, safe-word/callback/contact values, phone numbers, URLs/domains, long numbers, package/app names, account/card/OTP/PIN/resident-like values, device identifiers, official numbers, directory values, file/media names, URI/path values, screenshot/image/OCR values, call/SMS/notification state, recipient names, handoff/result/reset status values, or generated secret-like output.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no secret generation/no secret validation/no secret copying/no auto rotation/no plan mutation/no contact/call/SMS/clipboard/notification/file/photo/media/camera/OCR/no attachment/no URI share/no auto-send/no network/no official lookup/no number validation/no link validation/no permissions/no scheduler/no payment/banking/device control.
     - copy avoids unsafe guarantees: no “100%”, no “safe/secure”, no “generated”, no “rotated”, no “updated”, no “synced”, no “verified”, no “officially confirmed”, no “complete”, no “old secret invalidated”, no “new secret active”, no account/card auto-freeze, no payment/refund/recovery guarantee, no legal/financial result guarantee, no automatic monitoring/scanning/OCR/deletion/redaction/blocking/calling/messaging/lookup/verification/storage/upload/attachment.
   - Add/extend source-reading tests that prove the model does not import Android or touch `SafetyPlanStore`, `SafetyPlan`, `.safeWord`, `.callbackScript`, `.contacts`, `IncidentStore`, incident contents, `IncidentPreviewRedactor`, official directory entries, contact numbers, package manager results, clipboard, contact/call/SMS/notification/media/file/camera/OCR/network/payment/device APIs, or persistence mutation.
   - Add/extend UI source tests proving the coach screen uses only `FamilyReadinessBuilder.build(safetyPlan.load())` to compute value-free readiness and passes only readiness level/category into the builder; it must not read/display/share `plan.safeWord`, `plan.callbackScript`, `plan.contacts`, contact labels/numbers, or any literal values. The route to `Screen.PLAN` is allowed, but the new slice must not call `safetyPlan.save(` or `safetyPlan.clear()`.
   - Add/extend source scan for scheduler APIs, manifest permission invariants, clipboard APIs, call/SMS/contact/call-log APIs, media/file/photo/camera/OCR APIs, notification listener APIs, payment/banking APIs, network/browser/map APIs, official lookup APIs, safe-word reset storage/persistence APIs, and device-control APIs. Explicitly forbid `READ_MEDIA_IMAGES`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `CAMERA`, `MediaStore`, `ACTION_IMAGE_CAPTURE`, `ACTION_GET_CONTENT`, `ACTION_OPEN_DOCUMENT`, `ACTION_PICK`, `FileProvider`, `Uri.parse`, `Intent.EXTRA_STREAM`, `ClipData`, URI grants, OCR/ML/image APIs, `CallLog`, `Intent.ACTION_CALL`, `Intent.ACTION_DIAL` in the new slice, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, `PhoneStateListener`, `SmsManager`, `ContactsContract`, `NotificationListenerService`, `NotificationManager`, `ClipboardManager`, `BillingClient`, `NfcAdapter`, `AccountManager`, `DevicePolicyManager`, `AccessibilityService`, `VpnService`, `UsageStatsManager`, `PackageInstaller`, `ACTION_VIEW` for web URLs in the new slice, `HttpURLConnection`, `OkHttp`, `WebView`, `Geocoder`, `PackageManager.getInstalledPackages`, `prefs.edit`, `putString`, `putBoolean`, `.save(` in the new slice, and any new `<uses-permission>`.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No safe-word/password/OTP/PIN/account/card/contact/phone/URL/domain/official-number generator, validator, copier, scanner, synchronizer, automatic rotator, or reset completion state.
- No display/share/use of literal `SafetyPlan.safeWord`, callback script text, contact labels/numbers, raw analysis input, redacted clues, incident previews/bodies, official directory values, package/app names, device IDs, or recipient values.
- No mutation of safety plan from the new coach. It may route to `Screen.PLAN`; only the existing plan editor may save/clear values when the user explicitly uses it.
- No contact reading/writing/picking, call-log reading, auto dial/call/hang-up/call screening/blocking/call state, SMS send/read, recipient choosing, notification listening/monitoring/filtering, or clipboard reading/writing.
- No file/photo/media/storage/camera/screenshot/OCR/image analysis APIs, attachments, URI sharing, streams, file/document/photo pickers, MediaStore, FileProvider, or image capture.
- No network calls, web browsing, maps/geocoding, official-number lookup, phone-number validation, URL/domain validation, link scanning, identity verification, official confirmation, report submission, upload, cloud backup, or evidence sync.
- No accessibility service, device admin, VPN, usage stats, package/app inventory scanning, app install/uninstall, settings mutation, app ops, or MDM behavior.
- No payment APIs, banking APIs, NFC, account-manager integration, payment cancellation, refund, account/card freeze, or recovery automation.
- No alarms, notifications, calendar events, reminders, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No new persistence for reset choices, progress, reads, shares, names, assignments, checkmarks, notes, clipboard contents, recipients, sent status, reset status, old/new secret status, contact/channel values, official numbers, package names, device status, reminders, or history.
- Opening the screen or pressing route/share buttons must not add/modify/delete incident records or safety-plan values.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only. It must not attach files or streams and must not include any secret-like value.
- Do not ask users to type names, contacts, exact phone numbers, account/card numbers, OTP/PIN, passwords, safe-word answers, callback script literals, URLs/domains, resident IDs, device IDs, installed app/package names, official numbers, screenshots with credentials, file/media names, URI/path values, call-log details, notification contents, recipient names, reset result values, or raw scam text into this coach.
- Do not claim the safe word was generated, reset, rotated, invalidated, updated, synced, delivered, active, secure, safe, verified, officially confirmed, complete, accepted, or automatically protected.

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

Note: repo-wide scan may continue to show the unchanged legacy `Intent.ACTION_DIAL` in `MainActivity.kt`. If so, add a bounded slice-specific source scan/test proving the new Family Safe-Word Reset slice itself does not contain `ACTION_DIAL`, `ACTION_CALL`, contact/call/SMS/notification/media/file/photo/camera/OCR APIs, stream/URI share APIs, network/browser/map APIs, official lookup, persistence mutation, safe-word reset storage, plan mutation, raw/saved secret values, attachments, or raw/saved incident values.

Also inspect focused JUnit XML for the new test and key privacy tests, e.g. `FamilySafeWordResetCoachTest`, `PrivacyCenterBuilderTest`, `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-family-safe-word-reset-coach.md
git commit -m "feat: add family safe-word reset coach"
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
