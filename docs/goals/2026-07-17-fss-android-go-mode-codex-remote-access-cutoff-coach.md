# FSS Android /go continuation — Remote Access Cutoff Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `c7ac74aa68bcf7e615328791dcc5869d315f6eec` — `feat: add money transfer pause gate`

Parent evaluator reconciled the previous Hermes background notification, confirmed worker log had `### DONE`, `CODEX_RC=0`, and independently reran verification.

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

Result: `BUILD SUCCESSFUL`; APK exists with `1275846` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 30 commits.

Focused JUnit XML evidence before this slice:

- `MoneyTransferPauseGateBuilderTest`: debug 15/15, release 15/15, failures 0, errors 0.
- `PrivacyCenterBuilderTest`: debug 11/11, release 11/11, failures 0, errors 0.
- `PrivacyPostureRegressionTest`: debug 21/21, release 21/21, failures 0, errors 0.

Manifest/source invariant scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `RECORD_AUDIO`, `INTERNET`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Repo-wide source scan has no `AlarmManager`, `NotificationManager`, `NotificationCompat`, `WorkManager`, `JobScheduler`, `CalendarContract`, `ForegroundService`, `BroadcastReceiver`, `ContactsContract`, `SmsManager`, `Intent.EXTRA_STREAM`, `ClipData`, URI grant flag, `ClipboardManager`, `getPrimaryClip`, `setPrimaryClip`, `TextWatcher`, `addTextChangedListener`, `Intent.ACTION_CALL`, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, `PhoneStateListener`, `BillingClient`, `NfcAdapter`, or `AccountManager`.
- Repo-wide source scan has one existing legacy `Intent.ACTION_DIAL` reference in `MainActivity.kt`; parent slice scan proved the Money Transfer slice does **not** contain `ACTION_DIAL` or `ACTION_CALL`.

## Product intent for next cohesive slice

Add a **Remote Access Cutoff Coach** screen/card: when a scammer pushed app install, screen sharing, remote control, speakerphone/video-call coaching, or device-setting changes, the family needs a clear manual way to stop interaction, stop showing the screen, and move to trusted helper/account/evidence flows without the app pretending to remove malware or control the device.

This should feel like a private family AI guardian, not a device cleaner, antivirus, MDM, accessibility service, VPN, or app uninstaller. It should help the family say and do manually:

1. End the current call/chat/screen-share and stop following instructions.
2. Stop installing apps/files/profiles and stop revealing codes/screens.
3. Move sensitive account work to a trusted, separate device/helper where possible.
4. Preserve only safe evidence categories; do not spread secrets/screenshots with credentials.
5. Use existing account lockdown, trusted helper review, evidence, official help, money pause, and active-contact scripts.

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `RemoteAccessCutoffCoach.kt`:
   - `RemoteAccessCutoffCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): RemoteAccessCutoffCoach`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level`, counts/labels only — **not** safe word/callback/contact values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, raw/pasted text, incident bodies, `IncidentPreviewRedactor`, persisted incident contents, phone/contact values, URLs/domains, package names, installed app lists, account/card values, OTP/PIN, resident IDs, device IDs, or safety plan literal values.

2. Cutoff cards:
   - Fixed manual cards, for example:
     - “화면공유·통화 지시 끊기”
     - “앱 설치·파일·프로필 요구 중단”
     - “인증번호·금융화면 숨기기”
     - “민감 계정은 별도 기기/보호자와 점검”
     - “증거는 비밀값 없이 보존”
     - “공식 도움·후속 점검으로 이어가기”
   - Each card should include:
     - title/label
     - short manual cutoff sentence
     - why it matters
     - do-not-do boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for app install, live-call-control, credentials/OTP, urgent transfer/financial/money-pause signals, link/smishing, family/deepfake/number-change, secrecy/impersonation, saved incident, and low/in-progress readiness.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `remoteAccessCutoffCoachScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard near Active Contact Exit Scripts / Money Transfer Pause Gate.
     - Analysis result screen.
     - Quick Stop screen.
     - Active Contact Exit Scripts.
     - Money Transfer Pause Gate.
     - Account Lockdown Checklist.
     - Trusted Helper Review.
     - Safety Flow Router / Recovery Plan if appropriate.
   - Action buttons must delegate to existing screens/flows only; do not duplicate device-cleaning/account/evidence/official implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** add accessibility APIs, device-admin APIs, package manager install/uninstall scans, VPN, notification listener, usage stats, app ops, settings mutation, network calls, dial/call/SMS automation, or anything that claims to disconnect remote access, remove malware, clean a device, freeze accounts/cards, recover money, or block calls/apps.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new cutoff coach is named as local/manual/no-permission/no-network/no-device-admin/no-accessibility/no-installed-app-scan/no-uninstall/no-settings-mutation/no-storage/no auto-send/no automatic blocking/cleaning/recovery.
   - Update `README.md` flow and add a section for the new feature.

5. Tests:
   - Add `RemoteAccessCutoffCoachBuilderTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default coach is conservative and does not call anything safe.
     - app-install/live-call-control recommend screen-share cutoff, app/file install stop, trusted-helper/account/evidence routes, without claiming remote disconnect or malware removal.
     - credentials/OTP recommend hiding codes/screens and account protection without claiming account freeze.
     - urgent transfer/financial signals recommend money pause + account/evidence/official routes without payment cancellation/recovery guarantees.
     - link/smishing recommends no link/file opening and privacy/exposure/checker routes without link scan claims.
     - family/deepfake/number-change recommends ending current contact and family callback/safe call scripts.
     - secrecy/impersonation recommends not keeping the screen-share/contact secret and official route.
     - saved incident changes recommendations without reading incident contents.
     - low/in-progress readiness adds callback/readiness/family/helper support while ready copy remains cautious.
     - every card has complete fields, stable order, unique routes, and all action IDs map to existing flows.
     - share summary contains only generic cutoff/card/route/action labels and no raw input, redacted clue, safety plan literal, contact, phone number, URL/domain, long numbers, package/app names, account/card/OTP/PIN/resident-like values, or device identifiers.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no contacts/no auto-send/no network/no permissions/no scheduler/no mutation/no device-admin/no accessibility/no installed-app scan/no uninstall/no settings mutation/no remote-disconnect/no malware cleaning/no VPN/no notification listener.
     - copy avoids unsafe guarantees: no “100%”, no “safe”, no “blocked”, no malware removal, no remote access disconnected claim, no device cleaned claim, no account/card auto-freeze, no payment cancellation guarantee, no refund/recovery guarantee, no legal/financial result guarantee, no automatic monitoring/scanning/deletion/redaction/blocking/calling/payment/device action.
   - Add/extend source-reading tests that prove the UI share helper uses only `ACTION_SEND` + `text/plain` + builder summary and does not use `Intent.EXTRA_STREAM`, `ClipData`, URI grants, `ContactsContract`, `SmsManager`, raw `ScamAnalysis`, `analysis.input`, `SafetyPlan` literal values, `IncidentPreviewRedactor`, incident store contents, package manager scan results, or persistence mutation.
   - Add/extend source scan for scheduler APIs, manifest permission invariants, clipboard APIs, call/SMS automation APIs, payment/banking APIs, and device-control APIs. Explicitly forbid `Intent.ACTION_CALL`, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, `SmsManager`, `ClipboardManager`, `BillingClient`, `NfcAdapter`, `AccountManager`, `DevicePolicyManager`, `AccessibilityService`, `VpnService`, `NotificationListenerService`, `UsageStatsManager`, `PackageInstaller`, `deletePackage`, `ACTION_MANAGE_UNKNOWN_APP_SOURCES`, `ACTION_APPLICATION_DETAILS_SETTINGS`, `Settings.`, `PackageManager.getInstalledPackages`, and any new `<uses-permission>`.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No network calls.
- No automatic SMS/call/contact/clipboard reading.
- No accessibility service, device admin, VPN, notification listener, usage stats, package/app inventory scanning, app install/uninstall, settings mutation, app ops, or MDM behavior.
- No payment APIs, banking APIs, NFC, account-manager integration, payment cancellation, refund, account/card freeze, or recovery automation.
- No call blocking, call screening, auto dial, auto hang-up, auto SMS, or recipient choosing.
- No automatic redaction, scanning, monitoring, blocking, removal, cleaning, disconnecting, freezing, transfer, refund, or recovery.
- No mutation of pasted text, call state, contacts, incident log, safety plan, account/card/bank/device settings, installed apps, package state, accessibility settings, network/VPN state, notification settings, or money movement.
- No alarms, notifications, calendar events, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No new persistence for card choices, progress, reads, shares, names, assignments, checkmarks, notes, clipboard contents, remote-access status, app lists, package names, device status, or history.
- Opening the screen or pressing route/share buttons must not add/modify/delete incident records.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only.
- Do not ask users to paste names, contacts, exact phone numbers, account/card numbers, OTP/PIN, passwords, safe-word answers, URLs/domains, resident IDs, device IDs, installed app/package names, screenshots with credentials, or raw scam text.
- Do not claim official/legal/financial outcome, safety, scam certainty, malware removal, remote disconnection, device cleaning, account/card freeze, payment cancellation, refund, recovery, blocking, or automatic protection.

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
for token in ['CALL_PHONE','READ_SMS','READ_CALL_LOG','READ_CONTACTS','RECORD_AUDIO','INTERNET','POST_NOTIFICATIONS','SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED','PACKAGE_USAGE_STATS','QUERY_ALL_PACKAGES','BIND_ACCESSIBILITY_SERVICE','BIND_DEVICE_ADMIN','BIND_NOTIFICATION_LISTENER_SERVICE','BIND_VPN_SERVICE']:
    print(f'{token}={token in manifest}')
needle='android:allowBackup="false"'
print(f'allowBackup=false={needle in manifest}')
print(f'uses-permission-tag={"<uses-permission" in manifest}')
needles = [
    'AlarmManager','NotificationManager','NotificationCompat','WorkManager','JobScheduler',
    'CalendarContract','ForegroundService','BroadcastReceiver','POST_NOTIFICATIONS',
    'SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED','ContactsContract','SmsManager',
    'Intent.EXTRA_STREAM','ClipData','FLAG_GRANT_READ_URI_PERMISSION',
    'ClipboardManager','getPrimaryClip','setPrimaryClip','TextWatcher','addTextChangedListener',
    'Intent.ACTION_CALL','TelecomManager','TelephonyManager','CallScreeningService','PhoneStateListener',
    'BillingClient','NfcAdapter','AccountManager','DevicePolicyManager','AccessibilityService','VpnService',
    'NotificationListenerService','UsageStatsManager','PackageInstaller','deletePackage',
    'ACTION_MANAGE_UNKNOWN_APP_SOURCES','ACTION_APPLICATION_DETAILS_SETTINGS','Settings.',
    'getInstalledPackages','getInstalledApplications'
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

Also inspect focused JUnit XML for the new test and key privacy tests, e.g. `RemoteAccessCutoffCoachBuilderTest`, `PrivacyCenterBuilderTest`, `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-remote-access-cutoff-coach.md
git commit -m "feat: add remote access cutoff coach"
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
