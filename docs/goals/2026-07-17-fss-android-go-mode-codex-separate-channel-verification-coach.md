# FSS Android /go continuation — Separate Channel Verification Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `3c86d00463a5e9894d3c429650a1acff00861219` — `feat: add remote access cutoff coach`

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

Result: `BUILD SUCCESSFUL`; APK exists with `1291293` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 31 commits.

Focused JUnit XML evidence before this slice:

- `RemoteAccessCutoffCoachBuilderTest`: debug 15/15, release 15/15, failures 0, errors 0.
- `PrivacyCenterBuilderTest`: debug 12/12, release 12/12, failures 0, errors 0.
- `PrivacyPostureRegressionTest`: debug 22/22, release 22/22, failures 0, errors 0.

Manifest/source invariant scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `RECORD_AUDIO`, `INTERNET`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `PACKAGE_USAGE_STATS`, `QUERY_ALL_PACKAGES`, `BIND_ACCESSIBILITY_SERVICE`, `BIND_DEVICE_ADMIN`, `BIND_NOTIFICATION_LISTENER_SERVICE`, `BIND_VPN_SERVICE`, or `REQUEST_INSTALL_PACKAGES`.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Repo-wide source scan has no `AlarmManager`, `NotificationManager`, `NotificationCompat`, `WorkManager`, `JobScheduler`, `CalendarContract`, `ForegroundService`, `BroadcastReceiver`, `ContactsContract`, `SmsManager`, `Intent.EXTRA_STREAM`, `ClipData`, URI grant flag, `ClipboardManager`, `getPrimaryClip`, `setPrimaryClip`, `TextWatcher`, `addTextChangedListener`, `Intent.ACTION_CALL`, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, `PhoneStateListener`, `BillingClient`, `NfcAdapter`, `AccountManager`, `DevicePolicyManager`, `AccessibilityService`, `VpnService`, `NotificationListenerService`, `UsageStatsManager`, `PackageInstaller`, `deletePackage`, app-source settings actions, `Settings.`, `getInstalledPackages`, or `getInstalledApplications`.
- Repo-wide source scan has one existing legacy `Intent.ACTION_DIAL` reference in `MainActivity.kt`; parent slice scan proved the Remote Access slice does **not** contain `ACTION_DIAL` or `ACTION_CALL`.
- Parent slice scan also proved the Remote Access slice has no persistence mutation (`incidents.add/clear`, `prefs.edit`, `safetyPlan.save`), no raw `analysis.input`, and no device-control/payment APIs.

## Skill maintenance note

Parent fixed a malformed dangling code fragment in `ai-coding-agent-clis` reference while supervising this run. No repository source was changed by that skill maintenance.

## Product intent for next cohesive slice

Add a **Separate Channel Verification Coach** screen/card: every high-risk scam flow now tells users to verify through “평소 알던 경로 / 직접 찾은 공식 경로”, but a stressed family member still needs one concrete, privacy-safe place to choose what *kind* of independent channel to use without the app reading contacts, dialing, searching the web, opening links, or storing real phone numbers.

This should feel like a private family AI guardian, not a contact manager, phone dialer, directory lookup, browser, or official verifier. It helps the family manually decide:

1. Stop using the current contact/link/number/account/profile as a verification channel.
2. Verify family requests through known family paths and value-free callback scripts.
3. Verify banks, police, carriers, delivery, platform, school, workplace, or public agencies only through user-found official channels.
4. Verify account/session concerns through official apps or separate trusted devices, without sharing credentials.
5. Share a value-free family update after verification, not raw messages, contacts, numbers, links, OTP/PIN, passwords, account/card values, or safety-plan secrets.

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `SeparateChannelVerificationCoach.kt`:
   - `SeparateChannelVerificationCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): SeparateChannelVerificationCoach`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level`, counts/labels only — **not** safe word/callback/contact values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, raw/pasted text, incident bodies, `IncidentPreviewRedactor`, persisted incident contents, phone/contact values, URLs/domains, package names, installed app lists, account/card values, OTP/PIN, resident IDs, device IDs, exact official numbers, or safety plan literal values.

2. Verification cards:
   - Fixed manual cards, for example:
     - “현재 연락 경로 버리기”
     - “가족은 평소 경로로 콜백”
     - “기관·은행·수사기관은 직접 찾은 공식 경로”
     - “택배·청첩장·플랫폼 알림은 앱/공식 사이트에서 직접 확인”
     - “계정·기기 노출은 별도 기기와 보호자 점검”
     - “확인 결과는 값 없이 가족에게 알리기”
   - Each card should include:
     - title/label
     - short manual verification sentence
     - when to use
     - why it matters
     - do-not-use / do-not-share boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, family/deepfake/number-change, impersonation/secrecy, urgent transfer/financial/payment, credentials/OTP, link/smishing, app-install/remote-control, saved incident, and low/in-progress readiness.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `separateChannelVerificationCoachScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard near Quick Stop / Active Contact / Money Transfer / Remote Access.
     - Analysis result screen.
     - Quick Stop screen.
     - Active Contact Exit Scripts.
     - Money Transfer Pause Gate.
     - Remote Access Cutoff Coach.
     - Family Callback Card / Safe Call Scripts if appropriate.
     - Official Help / Report Prep if appropriate.
     - Safety Flow Router / Account Lockdown / Privacy Center if appropriate.
   - Action buttons must delegate to existing screens/flows only; do not duplicate contacts, official directory, callback, account, evidence, or report implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** add contact APIs, dial/call/SMS automation, web search/browser/network calls, maps, official-number lookup, payment/banking/device-control APIs, notifications, alarms, scheduler/background work, or anything that claims to verify identity, validate a number, validate a URL, place a call, send a message, block a contact, or contact an institution.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new coach is named as local/manual/no-permission/no-network/no-contact-reading/no-dial/no-SMS/no-official-lookup/no-number-validation/no-link-validation/no-storage/no auto-send/no scheduler/no mutation.
   - Update `README.md` flow and add a section for the new feature.

5. Tests:
   - Add `SeparateChannelVerificationCoachBuilderTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default coach is conservative and does not call anything safe, verified, official, or problem-free.
     - family/deepfake/number-change recommends family known-channel callback, safe call scripts, and family alert routes without exposing safe-word/callback/contact values.
     - impersonation/secrecy recommends abandoning the current contact path, official help, active-contact exit, and not keeping verification secret.
     - urgent transfer/financial/payment recommends money pause, official help, account/evidence routes, and no payment/freeze/refund guarantee.
     - credentials/OTP recommends account lockdown and remote-access cutoff without claiming account freeze.
     - app-install/live-call-control recommends remote-access cutoff and trusted-helper/account/evidence routes without device-cleaning guarantees.
     - link/smishing recommends no current link/number reuse, checker/exposure/privacy routes, and no link scan or URL validation claim.
     - saved incident changes recommendations without reading incident contents.
     - low/in-progress readiness adds family readiness/trusted helper/family support while ready copy remains cautious.
     - every card has complete fields, stable order, unique routes, and all action IDs map to existing flows.
     - share summary contains only generic verification/card/route/action labels and no raw input, redacted clue, safety plan literal, contact, phone number, URL/domain, long numbers, package/app names, account/card/OTP/PIN/resident-like values, device identifiers, official numbers, or directory values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no contacts/no contact reading/no auto-send/no network/no official lookup/no number validation/no link validation/no permissions/no scheduler/no mutation/no dial/call/SMS automation/no payment/banking/device control.
     - copy avoids unsafe guarantees: no “100%”, no “safe”, no “verified”, no “officially confirmed”, no “blocked”, no malware removal, no remote access disconnected claim, no device cleaned claim, no account/card auto-freeze, no payment cancellation/refund/recovery guarantee, no legal/financial result guarantee, no automatic monitoring/scanning/deletion/redaction/blocking/calling/messaging/lookup/verification.
   - Add/extend source-reading tests that prove the UI share helper uses only `ACTION_SEND` + `text/plain` + builder summary and does not use `Intent.EXTRA_STREAM`, `ClipData`, URI grants, `ContactsContract`, `SmsManager`, `Intent.ACTION_CALL`, `Intent.ACTION_DIAL` in the new slice, raw `ScamAnalysis`, `analysis.input`, `SafetyPlan` literal values, `IncidentPreviewRedactor`, incident store contents, official directory phone values, package manager scan results, or persistence mutation.
   - Add/extend source scan for scheduler APIs, manifest permission invariants, clipboard APIs, call/SMS/contact APIs, payment/banking APIs, network/browser/map APIs, official lookup APIs, and device-control APIs. Explicitly forbid `Intent.ACTION_CALL`, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, `SmsManager`, `ContactsContract`, `ClipboardManager`, `BillingClient`, `NfcAdapter`, `AccountManager`, `DevicePolicyManager`, `AccessibilityService`, `VpnService`, `NotificationListenerService`, `UsageStatsManager`, `PackageInstaller`, `ACTION_VIEW` for web URLs in the new slice, `HttpURLConnection`, `OkHttp`, `WebView`, `Geocoder`, `PackageManager.getInstalledPackages`, and any new `<uses-permission>`.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No network calls, web browsing, maps/geocoding, official-number lookup, phone-number validation, URL/domain validation, or link scanning.
- No contact reading, contact writing, contact picking, auto dial, auto call, auto hang-up, call screening, call state, SMS send/read, or recipient choosing.
- No clipboard reading/writing.
- No accessibility service, device admin, VPN, notification listener, usage stats, package/app inventory scanning, app install/uninstall, settings mutation, app ops, or MDM behavior.
- No payment APIs, banking APIs, NFC, account-manager integration, payment cancellation, refund, account/card freeze, or recovery automation.
- No automatic redaction, scanning, monitoring, blocking, lookup, verification, removal, cleaning, disconnecting, freezing, transfer, refund, or recovery.
- No mutation of pasted text, call state, contacts, incident log, safety plan, official directory, account/card/bank/device settings, installed apps, package state, accessibility settings, network/VPN state, notification settings, or money movement.
- No alarms, notifications, calendar events, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No new persistence for card choices, progress, reads, shares, names, assignments, checkmarks, notes, clipboard contents, verification status, contact/channel values, official numbers, package names, device status, or history.
- Opening the screen or pressing route/share buttons must not add/modify/delete incident records.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only.
- Do not ask users to paste names, contacts, exact phone numbers, account/card numbers, OTP/PIN, passwords, safe-word answers, URLs/domains, resident IDs, device IDs, installed app/package names, official numbers, screenshots with credentials, or raw scam text.
- Do not claim identity is verified, a number/link is official, a channel is safe, official/legal/financial outcome, safety, scam certainty, malware removal, remote disconnection, device cleaning, account/card freeze, payment cancellation, refund, recovery, blocking, or automatic protection.

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
for token in ['CALL_PHONE','READ_SMS','READ_CALL_LOG','READ_CONTACTS','WRITE_CONTACTS','RECORD_AUDIO','INTERNET','ACCESS_FINE_LOCATION','ACCESS_COARSE_LOCATION','POST_NOTIFICATIONS','SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED','PACKAGE_USAGE_STATS','QUERY_ALL_PACKAGES','BIND_ACCESSIBILITY_SERVICE','BIND_DEVICE_ADMIN','BIND_NOTIFICATION_LISTENER_SERVICE','BIND_VPN_SERVICE','REQUEST_INSTALL_PACKAGES']:
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

Note: repo-wide scan may continue to show the unchanged legacy `Intent.ACTION_DIAL` in `MainActivity.kt`. If so, add a bounded slice-specific source scan/test proving the new Separate Channel Verification slice itself does not contain `ACTION_DIAL`, `ACTION_CALL`, contact APIs, network/browser/map APIs, official lookup, persistence mutation, or raw/saved values.

Also inspect focused JUnit XML for the new test and key privacy tests, e.g. `SeparateChannelVerificationCoachBuilderTest`, `PrivacyCenterBuilderTest`, `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-separate-channel-verification-coach.md
git commit -m "feat: add separate channel verification coach"
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
