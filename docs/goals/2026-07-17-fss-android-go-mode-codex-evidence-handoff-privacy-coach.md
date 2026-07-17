# FSS Android /go continuation — Evidence Handoff Privacy Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `fba5f3ca7a067e5a36ec8235e7ea31a0b47c5a07` — `feat: add recontact boundary coach`

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

Result: `BUILD SUCCESSFUL`; APK exists with `1344357` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 34 commits.

Focused JUnit XML evidence before this slice:

- `RecontactBoundaryCoachTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 25/25, release 25/25, failures 0, errors 0, skipped 0.

Manifest/source invariant scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `READ_PHONE_STATE`, `ANSWER_PHONE_CALLS`, `RECORD_AUDIO`, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device admin, notification listener, VPN, or package install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Repo-wide source scan has no `AlarmManager`, `NotificationManager`, `NotificationCompat`, `WorkManager`, `JobScheduler`, `CalendarContract`, `ForegroundService`, `BroadcastReceiver`, `ContactsContract`, `CallLog`, `SmsManager`, stream/URI share APIs, clipboard APIs, text watchers, `Intent.ACTION_CALL`, telecom/telephony/call-screening APIs, payment/NFC/account-manager APIs, device-admin/accessibility/VPN/notification-listener/usage-stats/package-installer/settings APIs, installed-package scans, network/browser/map APIs, or `ACTION_VIEW`.
- Repo-wide source scan has one unchanged legacy `Intent.ACTION_DIAL` reference in `MainActivity.kt`; parent slice scan proved the Recontact Boundary slice itself does not contain `ACTION_DIAL`, `ACTION_CALL`, `CallLog`, contact APIs, call-screening APIs, notification APIs, network/browser/map APIs, official lookup, persistence mutation, recontact/boundary storage, raw/saved values, or official directory phone values.

Parent source inspection found the Recontact Boundary slice uses `RecontactBoundaryCoachBuilder.build(analysis=currentAnalysis, readiness=FamilyReadinessBuilder.build(safetyPlan.load()), incidentCount=incidents.count())`, shares only `coach.familyShareSummary` through manual `ACTION_SEND`/`text/plain`, and has bounded privacy regression tests covering the new model, UI routes, share helper, incident count helper, manifest permissions, call/SMS/contact/call-log/notification/device-control forbidden APIs, and persistence invariants.

## Product intent for next cohesive slice

Add an **Evidence Handoff Privacy Coach** screen/card: families often need to send evidence to a helper or official channel, but forwarding raw screenshots/messages can leak OTP, account/card values, URLs, phone numbers, family safe-word answers, device IDs, app/package names, or exact scammer contact details. The app already has evidence preservation and report-prep flows; this slice should sit between “save/prepare evidence” and “share/talk to helper” and teach a value-free, manual handoff checklist.

This is not a screenshot tool, image editor, file picker, upload flow, OCR, redaction engine, cloud backup, report submission, or evidence collector. It should not read files, images, media, photos, clipboard, screenshots, notifications, contacts, call logs, SMS, or incident bodies. It should not attach evidence files. It should not mutate saved incident records. It should only show deterministic cards and route to existing flows.

Good user story:

> “Before I send anything to family/helper/official support, show me what not to forward and what app screen to use next, without making me paste or upload the evidence.”

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `EvidenceHandoffPrivacyCoach.kt`:
   - `EvidenceHandoffPrivacyCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): EvidenceHandoffPrivacyCoach`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, score, action plan, summary, raw pasted text, redacted clues, incident bodies/previews, `IncidentPreviewRedactor`, persisted incident contents, phone/contact values, URLs/domains, package/app names, installed app lists, account/card values, OTP/PIN, resident IDs, device IDs, exact official numbers, safety plan literal values, call/SMS/contact/notification/media/file state, verification/recontact result values, or anything from official directory entries.

2. Evidence handoff cards:
   - Fixed manual cards, for example:
     - “원문·스크린샷 보내기 전: 민감값 먼저 빼기”
     - “가족/보호자에게 줄 때: 행동 종류만 공유하기”
     - “기관/상담 준비: 원본은 내가 보관하고 말할 내용만 정리하기”
     - “돈·계정·OTP 노출 증거: 값은 말하지 않고 계정/증거 경로로 이동”
     - “링크·앱·원격지시 증거: URL·앱/패키지/기기값을 퍼뜨리지 않기”
     - “후속 점검: 누구에게 무엇을 보냈는지 저장하지 않고 가족과 다시 보기”
   - Each card should include:
     - title/label
     - handoff scenario sentence to read manually
     - privacy action sentence (short, sayable, value-free)
     - why it matters
     - do-not-attach / do-not-record / do-not-share boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, family/deepfake/number-change, impersonation/secrecy, urgent transfer/financial/payment, credentials/OTP, link/smishing, app-install/remote-control, saved incident, and low/in-progress readiness.
   - Default/no-analysis state should be conservative: show all/broad handoff cards and never call evidence complete, sanitized, safe, verified, official, submitted, resolved, recovered, blocked, or redacted.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `evidenceHandoffPrivacyCoachScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard near Evidence / Report Prep / Privacy Center / Quick Stop.
     - Analysis result screen.
     - Evidence Preservation Kit screen or entry points.
     - Official Report Prep screen.
     - Privacy Center.
     - Safe Paste Privacy Coach.
     - Verification Outcome Next Steps Board.
     - Recontact Boundary Coach.
     - Family Alert / Trusted Helper Review / Follow-up Planner / Safety Flow Router if appropriate.
     - Money Transfer Pause Gate / Remote Access Cutoff Coach / Account Lockdown where evidence handoff is relevant.
   - Action buttons must delegate to existing screens/flows only; do not duplicate evidence storage, redaction, official directory, report prep, helper review, account, recovery, or follow-up implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** add camera/photo/media/file APIs, file pickers, URI grants, `Intent.EXTRA_STREAM`, `ClipData`, `MediaStore`, `ACTION_IMAGE_CAPTURE`, screenshot APIs, OCR, ML/image APIs, contact APIs, call log APIs, dial/call/SMS automation, notification listener, web search/browser/network calls, maps, official-number lookup, phone/link verification, clipboard APIs, payment/banking/device-control APIs, alarms, scheduler/background work, or any storage of handoff status/read/checkmarks/share recipients/attachment state.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new coach is named as local/manual/no-permission/no-network/no-file-read/no-photo-read/no-media-read/no-camera/no-screenshot/no-OCR/no-attachment/no-URI-share/no-contact-reading/no-call-log/no-dial/no-call/no-SMS/no-notification-monitor/no-official-lookup/no-number-validation/no-link-validation/no-handoff-storage/no checkmarks/no auto-send/no scheduler/no mutation.
   - Update `README.md` flow and add a section for the new feature.

5. Tests:
   - Add `EvidenceHandoffPrivacyCoachTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default coach is conservative and does not call evidence safe, sanitized, redacted, uploaded, submitted, verified, official, resolved, recovered, blocked, complete, or problem-free.
     - family/deepfake/number-change recommends family-safe value-free sharing, safe call scripts, family alert, trusted helper, separate-channel/verification outcome routes without exposing safe-word/callback/contact values.
     - impersonation/secrecy recommends not keeping the handoff secret, family alert/debrief, evidence kit, official help, report prep, and separate-channel routes.
     - urgent transfer/financial/payment recommends money pause, account lockdown, evidence kit, official help, report prep/recovery/follow-up routes without payment/freeze/refund/recovery guarantees.
     - credentials/OTP recommends account lockdown and remote-access cutoff without claiming account freeze or secret cleanup.
     - app-install/live-call-control recommends remote-access cutoff, trusted-helper/account/evidence routes without device-cleaning, malware-removal, screenshot review, or remote-disconnect guarantees.
     - link/smishing recommends checker/exposure/privacy/safe-paste/evidence routes and no link scan, URL validation, OCR, or automatic redaction claim.
     - saved incident changes recommendations without reading incident contents.
     - low/in-progress readiness adds safe-call/family support/trusted helper/roadmap while ready copy remains cautious.
     - every card has complete fields, stable order, unique routes, and all action IDs map to existing flows.
     - share summary contains only generic handoff/card/route/action labels and no raw input, redacted clue, safety plan literal, contact, phone number, URL/domain, long numbers, package/app names, account/card/OTP/PIN/resident-like values, device identifiers, official numbers, directory values, file/media names, URI/path values, screenshot/image/OCR values, call/SMS/notification state, recipient names, or handoff/result values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no handoff storage/no file/photo/media read/no camera/no screenshot capture/no OCR/no attachment/no URI share/no contacts/no call log/no auto-send/no network/no official lookup/no number validation/no link validation/no permissions/no scheduler/no mutation/no dial/call/SMS automation/no notification monitoring/no payment/banking/device control.
     - copy avoids unsafe guarantees: no “100%”, no “safe”, no “sanitized”, no “redacted”, no “uploaded”, no “submitted”, no “verified”, no “officially confirmed”, no “resolved”, no “blocked”, no “complete”, no malware removal, no remote access disconnected claim, no device cleaned claim, no account/card auto-freeze, no payment cancellation/refund/recovery guarantee, no legal/financial result guarantee, no automatic monitoring/scanning/OCR/deletion/redaction/blocking/calling/messaging/lookup/verification/storage/upload/attachment.
   - Add/extend source-reading tests that prove the UI share helper uses only `ACTION_SEND` + `text/plain` + builder summary and does not use `Intent.EXTRA_STREAM`, `ClipData`, URI grants, `Uri.parse`, `FileProvider`, `MediaStore`, `ACTION_IMAGE_CAPTURE`, `ACTION_GET_CONTENT`, `ACTION_OPEN_DOCUMENT`, `ACTION_PICK`, camera/photo/media/storage APIs, OCR/ML APIs, `ContactsContract`, `CallLog`, `SmsManager`, `Intent.ACTION_CALL`, `Intent.ACTION_DIAL` in the new slice, raw `ScamAnalysis`, `analysis.input`, `SafetyPlan` literal values, `IncidentPreviewRedactor`, incident store contents, official directory phone values, package manager scan results, notification APIs, call-screening APIs, or persistence mutation.
   - Add/extend source scan for scheduler APIs, manifest permission invariants, clipboard APIs, call/SMS/contact/call-log APIs, media/file/photo/camera/OCR APIs, notification listener APIs, payment/banking APIs, network/browser/map APIs, official lookup APIs, handoff storage/persistence APIs, and device-control APIs. Explicitly forbid `READ_MEDIA_IMAGES`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `CAMERA`, `MediaStore`, `ACTION_IMAGE_CAPTURE`, `ACTION_GET_CONTENT`, `ACTION_OPEN_DOCUMENT`, `ACTION_PICK`, `FileProvider`, `Uri.parse`, `Intent.EXTRA_STREAM`, `ClipData`, URI grants, OCR/ML/image APIs, `CallLog`, `Intent.ACTION_CALL`, `Intent.ACTION_DIAL`, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, `PhoneStateListener`, `SmsManager`, `ContactsContract`, `NotificationListenerService`, `NotificationManager`, `ClipboardManager`, `BillingClient`, `NfcAdapter`, `AccountManager`, `DevicePolicyManager`, `AccessibilityService`, `VpnService`, `UsageStatsManager`, `PackageInstaller`, `ACTION_VIEW` for web URLs in the new slice, `HttpURLConnection`, `OkHttp`, `WebView`, `Geocoder`, `PackageManager.getInstalledPackages`, `prefs.edit`, `putString`, `putBoolean`, `.save(` in the new slice, and any new `<uses-permission>`.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No file/photo/media/storage/camera/screenshot/OCR/image analysis APIs.
- No attachments, URI sharing, streams, file picker, document picker, photo picker, MediaStore, FileProvider, or image capture.
- No network calls, web browsing, maps/geocoding, official-number lookup, phone-number validation, URL/domain validation, link scanning, identity verification, official confirmation, report submission, upload, cloud backup, or evidence sync.
- No contact reading, contact writing, contact picking, call-log reading, auto dial, auto call, auto hang-up, call screening/blocking, call state, SMS send/read, recipient choosing, notification listening, or notification monitoring.
- No clipboard reading/writing.
- No accessibility service, device admin, VPN, usage stats, package/app inventory scanning, app install/uninstall, settings mutation, app ops, or MDM behavior.
- No payment APIs, banking APIs, NFC, account-manager integration, payment cancellation, refund, account/card freeze, or recovery automation.
- No automatic redaction, OCR, scanning, monitoring, blocking, muting, notification filtering, lookup, verification, removal, cleaning, disconnecting, freezing, transfer, refund, recovery, upload, submission, or handoff storage.
- No mutation of pasted text, screenshots, files, media, call state, call logs, contacts, SMS, notifications, incident log, safety plan, official directory, account/card/bank/device settings, installed apps, package state, accessibility settings, network/VPN state, notification settings, money movement, verification result, recontact/boundary status, or evidence handoff status.
- No alarms, notifications, calendar events, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No new persistence for card choices, progress, reads, shares, names, assignments, checkmarks, notes, clipboard contents, file paths, media names, attachment state, recipients, sent status, handoff status, blocked/muted status, contact/channel values, official numbers, package names, device status, or history.
- Opening the screen or pressing route/share buttons must not add/modify/delete incident records.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only. It must not attach files or streams.
- Do not ask users to paste names, contacts, exact phone numbers, account/card numbers, OTP/PIN, passwords, safe-word answers, URLs/domains, resident IDs, device IDs, installed app/package names, official numbers, screenshots with credentials, file/media names, URI/path values, call-log details, notification contents, recipient names, handoff result values, or raw scam text.
- Do not claim evidence is sanitized, redacted, uploaded, submitted, complete, accepted, safe, verified, officially confirmed, resolved, removed, cleaned, disconnected, frozen, cancelled, refunded, recovered, or automatically protected.

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

Note: repo-wide scan may continue to show the unchanged legacy `Intent.ACTION_DIAL` in `MainActivity.kt`. If so, add a bounded slice-specific source scan/test proving the new Evidence Handoff slice itself does not contain `ACTION_DIAL`, `ACTION_CALL`, media/file/photo/camera/OCR APIs, stream/URI share APIs, `CallLog`, contact APIs, call-screening APIs, notification APIs, network/browser/map APIs, official lookup, persistence mutation, handoff storage, attachments, or raw/saved values.

Also inspect focused JUnit XML for the new test and key privacy tests, e.g. `EvidenceHandoffPrivacyCoachTest`, `PrivacyCenterBuilderTest`, `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-evidence-handoff-privacy-coach.md
git commit -m "feat: add evidence handoff privacy coach"
```

Do **not** push.

## Final response schema

End with either:

### DONE

- What changed.
- Files updated count.
- Verification commands and results.
- Focused JUnit counts for the new test and privacy regressions.
- APK path and byte size.
- Local commit SHA and message.
- Confirm no push performed.

or:

### BLOCKED

- Exact blocker.
- What changed, if anything.
- Commands attempted and failing output.
- Current git status.
- Safest next step.
