# FSS Android /go continuation — Verification Outcome Next Steps Board

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `6d1a7ed0dd37c246993bf89320c2e6b3601dddeb` — `feat: add separate channel verification coach`

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

Result: `BUILD SUCCESSFUL`; APK exists with `1309461` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 32 commits.

Focused JUnit XML evidence before this slice:

- `SeparateChannelVerificationCoachBuilderTest`: debug 15/15, release 15/15, failures 0, errors 0.
- `PrivacyCenterBuilderTest`: debug 13/13, release 13/13, failures 0, errors 0.
- `PrivacyPostureRegressionTest`: debug 23/23, release 23/23, failures 0, errors 0.

Manifest/source invariant scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `RECORD_AUDIO`, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device admin, notification listener, VPN, or package install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Repo-wide source scan has no `AlarmManager`, `NotificationManager`, `NotificationCompat`, `WorkManager`, `JobScheduler`, `CalendarContract`, `ForegroundService`, `BroadcastReceiver`, `ContactsContract`, `SmsManager`, stream/URI share APIs, clipboard APIs, text watchers, `Intent.ACTION_CALL`, telecom/telephony/call-screening APIs, payment/NFC/account-manager APIs, device-admin/accessibility/VPN/notification-listener/usage-stats/package-installer/settings APIs, installed-package scans, network/browser/map APIs, or `ACTION_VIEW`.
- Repo-wide source scan has one existing legacy `Intent.ACTION_DIAL` reference in `MainActivity.kt`; parent slice scan proved the Separate Channel slice does **not** contain `ACTION_DIAL`, `ACTION_CALL`, contact APIs, network/browser/map APIs, official lookup, persistence mutation, raw `analysis.input`, or official directory phone values.

## Product intent for next cohesive slice

Add a **Verification Outcome Next Steps Board** screen/card: after the user has stopped the current contact and attempted a separate-channel check, the family still needs a value-free “what now?” board that maps *possible verification outcomes* to existing next actions without storing the result, validating a channel, or claiming anyone is safe.

This should feel like a private family AI guardian, not a case-management system or verification authority. It helps the family manually decide what to do if:

1. The family member or institution cannot be reached yet.
2. The known family path says the current request was not from them.
3. The user found an official channel and still needs to preserve evidence / report / review accounts.
4. Money, credentials, links, remote access, or device/account screens may already have been exposed.
5. The family needs a value-free update and a follow-up check without putting raw messages, contacts, numbers, links, OTP/PIN, passwords, account/card values, device identifiers, or official numbers into the app.

Do **not** ask the user to enter an outcome. This slice is a deterministic static board with recommended cards chosen from analysis/readiness/incident-count only. It must not store “verified”, “safe”, “called”, “confirmed”, or checkbox state.

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `VerificationOutcomeNextStepsBoard.kt`:
   - `VerificationOutcomeNextStepsBoardBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): VerificationOutcomeNextStepsBoard`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level`, counts/labels only — **not** safe word/callback/contact values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, raw/pasted text, incident bodies, `IncidentPreviewRedactor`, persisted incident contents, phone/contact values, URLs/domains, package names, installed app lists, account/card values, OTP/PIN, resident IDs, device IDs, exact official numbers, verification result values, or safety plan literal values.

2. Outcome cards:
   - Fixed manual cards, for example:
     - “아직 확인 안 됨: 계속 멈춤 유지”
     - “가족 경로가 아니라고 확인되면: 가족 경고와 증거”
     - “기관·은행 경로 확인 뒤: 공식 도움·신고 준비”
     - “돈·인증·계정 노출 가능성: 계정·송금 수습”
     - “링크·앱·화면공유 노출 가능성: 원격/노출 점검”
     - “가족에게 값 없는 결과 공유와 후속 점검”
   - Each card should include:
     - title/label
     - value-free outcome condition (e.g. “가족이 아니라고 확인되면”, “아직 연락이 안 되면”) without asking the user to enter it
     - next-step sentence to read manually
     - why it matters
     - do-not-record / do-not-share boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, family/deepfake/number-change, impersonation/secrecy, urgent transfer/financial/payment, credentials/OTP, link/smishing, app-install/remote-control, saved incident, and low/in-progress readiness.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `verificationOutcomeNextStepsBoardScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard near Separate Channel Verification / Quick Stop.
     - Analysis result screen.
     - Quick Stop screen.
     - Separate Channel Verification Coach.
     - Active Contact Exit Scripts.
     - Money Transfer Pause Gate.
     - Remote Access Cutoff Coach.
     - Family Alert / Family Calm Debrief / Follow-up Planner if appropriate.
     - Official Help / Report Prep / Safety Flow Router if appropriate.
   - Action buttons must delegate to existing screens/flows only; do not duplicate contacts, official directory, callback, account, evidence, report, recovery, or follow-up implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** add contact APIs, dial/call/SMS automation, web search/browser/network calls, maps, official-number lookup, phone/link verification, payment/banking/device-control APIs, notifications, alarms, scheduler/background work, or any storage of outcome/status/checkmarks.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new board is named as local/manual/no-permission/no-network/no-contact-reading/no-dial/no-SMS/no-official-lookup/no-number-validation/no-link-validation/no-outcome-storage/no checkmarks/no auto-send/no scheduler/no mutation.
   - Update `README.md` flow and add a section for the new feature.

5. Tests:
   - Add `VerificationOutcomeNextStepsBoardTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default board is conservative and does not call anything safe, verified, official, resolved, recovered, or problem-free.
     - family/deepfake/number-change recommends “not from them / cannot reach them yet” style cards, family alert, safe call scripts, and separate-channel routes without exposing safe-word/callback/contact values.
     - impersonation/secrecy recommends not keeping the outcome secret, active-contact exit, official help, and family update routes.
     - urgent transfer/financial/payment recommends money pause, account lockdown, evidence, official help, report prep, and recovery/follow-up routes without payment/freeze/refund guarantees.
     - credentials/OTP recommends account lockdown and remote-access cutoff without claiming account freeze.
     - app-install/live-call-control recommends remote-access cutoff and trusted-helper/account/evidence routes without device-cleaning guarantees.
     - link/smishing recommends checker/exposure/privacy routes and no link scan or URL validation claim.
     - saved incident changes recommendations without reading incident contents.
     - low/in-progress readiness adds readiness/trusted helper/family support while ready copy remains cautious.
     - every card has complete fields, stable order, unique routes, and all action IDs map to existing flows.
     - share summary contains only generic outcome/card/route/action labels and no raw input, redacted clue, safety plan literal, contact, phone number, URL/domain, long numbers, package/app names, account/card/OTP/PIN/resident-like values, device identifiers, official numbers, directory values, or verification result values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no outcome storage/no contacts/no contact reading/no auto-send/no network/no official lookup/no number validation/no link validation/no permissions/no scheduler/no mutation/no dial/call/SMS automation/no payment/banking/device control.
     - copy avoids unsafe guarantees: no “100%”, no “safe”, no “verified”, no “officially confirmed”, no “resolved”, no “recovered”, no “blocked”, no malware removal, no remote access disconnected claim, no device cleaned claim, no account/card auto-freeze, no payment cancellation/refund/recovery guarantee, no legal/financial result guarantee, no automatic monitoring/scanning/deletion/redaction/blocking/calling/messaging/lookup/verification/storage.
   - Add/extend source-reading tests that prove the UI share helper uses only `ACTION_SEND` + `text/plain` + builder summary and does not use `Intent.EXTRA_STREAM`, `ClipData`, URI grants, `ContactsContract`, `SmsManager`, `Intent.ACTION_CALL`, `Intent.ACTION_DIAL` in the new slice, raw `ScamAnalysis`, `analysis.input`, `SafetyPlan` literal values, `IncidentPreviewRedactor`, incident store contents, official directory phone values, package manager scan results, or persistence mutation.
   - Add/extend source scan for scheduler APIs, manifest permission invariants, clipboard APIs, call/SMS/contact APIs, payment/banking APIs, network/browser/map APIs, official lookup APIs, outcome storage/persistence APIs, and device-control APIs. Explicitly forbid `Intent.ACTION_CALL`, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, `SmsManager`, `ContactsContract`, `ClipboardManager`, `BillingClient`, `NfcAdapter`, `AccountManager`, `DevicePolicyManager`, `AccessibilityService`, `VpnService`, `NotificationListenerService`, `UsageStatsManager`, `PackageInstaller`, `ACTION_VIEW` for web URLs in the new slice, `HttpURLConnection`, `OkHttp`, `WebView`, `Geocoder`, `PackageManager.getInstalledPackages`, `prefs.edit`, `putString`, `putBoolean`, `.save(` in the new slice, and any new `<uses-permission>`.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No network calls, web browsing, maps/geocoding, official-number lookup, phone-number validation, URL/domain validation, link scanning, identity verification, or official confirmation.
- No contact reading, contact writing, contact picking, auto dial, auto call, auto hang-up, call screening, call state, SMS send/read, or recipient choosing.
- No clipboard reading/writing.
- No accessibility service, device admin, VPN, notification listener, usage stats, package/app inventory scanning, app install/uninstall, settings mutation, app ops, or MDM behavior.
- No payment APIs, banking APIs, NFC, account-manager integration, payment cancellation, refund, account/card freeze, or recovery automation.
- No automatic redaction, scanning, monitoring, blocking, lookup, verification, removal, cleaning, disconnecting, freezing, transfer, refund, recovery, or outcome storage.
- No mutation of pasted text, call state, contacts, incident log, safety plan, official directory, account/card/bank/device settings, installed apps, package state, accessibility settings, network/VPN state, notification settings, money movement, or verification result/status.
- No alarms, notifications, calendar events, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No new persistence for card choices, progress, reads, shares, names, assignments, checkmarks, notes, clipboard contents, verification status, outcome status, contact/channel values, official numbers, package names, device status, or history.
- Opening the screen or pressing route/share buttons must not add/modify/delete incident records.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only.
- Do not ask users to paste names, contacts, exact phone numbers, account/card numbers, OTP/PIN, passwords, safe-word answers, URLs/domains, resident IDs, device IDs, installed app/package names, official numbers, screenshots with credentials, verification result values, or raw scam text.
- Do not claim identity is verified, a number/link is official, a channel is safe, official/legal/financial outcome, safety, scam certainty, malware removal, remote disconnection, device cleaning, account/card freeze, payment cancellation, refund, recovery, blocking, completion, resolution, or automatic protection.

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

Note: repo-wide scan may continue to show the unchanged legacy `Intent.ACTION_DIAL` in `MainActivity.kt`. If so, add a bounded slice-specific source scan/test proving the new Verification Outcome slice itself does not contain `ACTION_DIAL`, `ACTION_CALL`, contact APIs, network/browser/map APIs, official lookup, persistence mutation, outcome storage, or raw/saved values.

Also inspect focused JUnit XML for the new test and key privacy tests, e.g. `VerificationOutcomeNextStepsBoardTest`, `PrivacyCenterBuilderTest`, `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-verification-outcome-next-steps-board.md
git commit -m "feat: add verification outcome next steps board"
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
