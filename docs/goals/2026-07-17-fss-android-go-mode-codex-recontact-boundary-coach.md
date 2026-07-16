# FSS Android /go continuation — Recontact Boundary Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `05cb0057a944517575c042bf86d5a744096169c8` — `feat: add verification outcome next steps board`

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

Result: `BUILD SUCCESSFUL`; APK exists with `1325836` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main` by 33 commits.

Focused JUnit XML evidence before this slice:

- `VerificationOutcomeNextStepsBoardTest`: debug 15/15, release 15/15, failures 0, errors 0, skipped 0.
- `PrivacyCenterBuilderTest`: debug 14/14, release 14/14, failures 0, errors 0, skipped 0.
- `PrivacyPostureRegressionTest`: debug 24/24, release 24/24, failures 0, errors 0, skipped 0.

Manifest/source invariant scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `WRITE_CONTACTS`, `RECORD_AUDIO`, `INTERNET`, location, notification, alarm, boot, usage stats, query-all-packages, accessibility, device admin, notification listener, VPN, or package install permissions.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Repo-wide source scan has no `AlarmManager`, `NotificationManager`, `NotificationCompat`, `WorkManager`, `JobScheduler`, `CalendarContract`, `ForegroundService`, `BroadcastReceiver`, `ContactsContract`, `SmsManager`, stream/URI share APIs, clipboard APIs, text watchers, `Intent.ACTION_CALL`, telecom/telephony/call-screening APIs, payment/NFC/account-manager APIs, device-admin/accessibility/VPN/notification-listener/usage-stats/package-installer/settings APIs, installed-package scans, network/browser/map APIs, or `ACTION_VIEW`.
- Repo-wide source scan has one unchanged legacy `Intent.ACTION_DIAL` reference in `MainActivity.kt`; parent slice scan proved the Verification Outcome slice itself does not contain `ACTION_DIAL`, `ACTION_CALL`, contact APIs, network/browser/map APIs, official lookup, persistence mutation, outcome storage, raw/saved values, or official directory phone values.

Parent source inspection found the Verification Outcome slice uses `VerificationOutcomeNextStepsBoardBuilder.build(analysis=currentAnalysis, readiness=FamilyReadinessBuilder.build(safetyPlan.load()), incidentCount=incidents.count())`, shares only `board.familyShareSummary` through manual `ACTION_SEND`/`text/plain`, and has bounded privacy regression tests covering the new model, UI routes, share helper, incident count helper, manifest permissions, and forbidden APIs.

## Product intent for next cohesive slice

Add a **Recontact Boundary Coach** screen/card: after the user stops or reports a suspicious contact, scammers often call back, switch numbers, threaten secrecy, ask for “just one more code”, or send a new link/profile. Families need a value-free boundary script board that keeps the person from re-engaging through the same attacker-controlled channel.

This is not a call blocker, spam filter, dialer, SMS tool, contact manager, notification monitor, or automatic protection engine. It is a deterministic local manual coach that helps the user and family agree: “if they contact again, we do not negotiate there; we keep the current channel closed and use existing app routes.”

The slice should compose with existing flows:

- Quick Stop Mode
- Active Contact Exit Scripts
- Separate Channel Verification Coach
- Verification Outcome Next Steps Board
- Family Alert Message Pack
- Safe Verification Call Scripts
- Trusted Helper Review / Handoff
- Money Transfer Pause Gate
- Remote Access Cutoff Coach
- Account Lockdown Checklist
- Evidence Preservation Kit
- Official Help / Report Prep
- Follow-up Check-in Planner
- Family Calm Debrief Agenda
- Privacy Center
- Safety Flow Router

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `RecontactBoundaryCoach.kt`:
   - `RecontactBoundaryCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): RecontactBoundaryCoach`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level` only — not title/count/checklist/nextAction values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, score, raw pasted text, redacted clues, incident bodies/previews, `IncidentPreviewRedactor`, persisted incident contents, phone/contact values, URLs/domains, package/app names, installed app lists, account/card values, OTP/PIN, resident IDs, device IDs, official numbers, safety plan literal values, call/SMS/contact state, verification result values, or anything from official directory entries.

2. Boundary cards:
   - Fixed manual cards, for example:
     - “다시 전화·문자 오면: 같은 경로에서 응답하지 않기”
     - “새 번호·새 프로필·새 링크로 오면: 같은 사람인지 판단하지 말고 별도 경로”
     - “협박·비밀 유지·수사/기관 압박 재시도: 가족에게 값 없이 알리기”
     - “돈·OTP·계정·카드 재요구: 돈/계정 행동 계속 멈춤”
     - “앱 설치·화면공유·원격지시 재개 요구: 기기 화면 숨기고 보호자 점검”
     - “가족 후속 약속: 오늘/내일/이번 주 다시 확인”
   - Each card should include:
     - title/label
     - recontact scenario sentence to read manually
     - boundary script sentence (short, sayable, value-free)
     - why it matters
     - do-not-record / do-not-share boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high risk, family/deepfake/number-change, impersonation/secrecy, urgent transfer/financial/payment, credentials/OTP, link/smishing, app-install/remote-control, saved incident, and low/in-progress readiness.
   - Default/no-analysis state should be conservative: show all or broad boundary cards and never call the contact safe, blocked, verified, or resolved.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `recontactBoundaryCoachScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard near Quick Stop / Active Contact Exit / Separate Channel / Verification Outcome.
     - Analysis result screen.
     - Quick Stop screen.
     - Active Contact Exit Scripts.
     - Separate Channel Verification Coach.
     - Verification Outcome Next Steps Board.
     - Money Transfer Pause Gate.
     - Remote Access Cutoff Coach.
     - Family Alert / Family Calm Debrief / Follow-up Planner / Safety Flow Router / Privacy Center if appropriate.
   - Action buttons must delegate to existing screens/flows only; do not duplicate call blocking, contact management, official directory, callback, account, evidence, report, recovery, or follow-up implementations.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** add contact APIs, call log APIs, dial/call/SMS automation, call-screening/blocking, notification listener, web search/browser/network calls, maps, official-number lookup, phone/link verification, clipboard APIs, payment/banking/device-control APIs, alarms, scheduler/background work, or any storage of boundary status/read/checkmarks/recontact state.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new coach is named as local/manual/no-permission/no-network/no-contact-reading/no-call-log/no-dial/no-call/no-SMS/no-call-screening/no-blocking/no-notification-monitor/no-official-lookup/no-number-validation/no-link-validation/no-recontact-storage/no checkmarks/no auto-send/no scheduler/no mutation.
   - Update `README.md` flow and add a section for the new feature.

5. Tests:
   - Add `RecontactBoundaryCoachTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default board is conservative and does not call anything safe, verified, official, resolved, blocked, silenced, or problem-free.
     - family/deepfake/number-change recommends same-channel non-response, known-family callback, family alert, separate-channel and verification-outcome routes without exposing safe-word/callback/contact values.
     - impersonation/secrecy recommends not keeping recontact secret, family alert/debrief, active-contact exit, official help, and separate-channel routes.
     - urgent transfer/financial/payment recommends money pause, account lockdown, evidence, official help, report prep/recovery/follow-up routes without payment/freeze/refund/recovery guarantees.
     - credentials/OTP recommends account lockdown and remote-access cutoff without claiming account freeze.
     - app-install/live-call-control recommends remote-access cutoff and trusted-helper/account/evidence routes without device-cleaning, malware-removal, or remote-disconnect guarantees.
     - link/smishing recommends checker/exposure/privacy/separate-channel routes and no link scan or URL validation claim.
     - saved incident changes recommendations without reading incident contents.
     - low/in-progress readiness adds safe-call/family support/trusted helper/roadmap while ready copy remains cautious.
     - every card has complete fields, stable order, unique routes, and all action IDs map to existing flows.
     - share summary contains only generic boundary/card/route/action labels and no raw input, redacted clue, safety plan literal, contact, phone number, URL/domain, long numbers, package/app names, account/card/OTP/PIN/resident-like values, device identifiers, official numbers, directory values, call/SMS state, notification state, or recontact result values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no recontact storage/no call log/no contacts/no contact reading/no auto-send/no network/no official lookup/no number validation/no link validation/no permissions/no scheduler/no mutation/no dial/call/SMS automation/no call-screening/no blocking/no notification monitoring/no payment/banking/device control.
     - copy avoids unsafe guarantees: no “100%”, no “safe”, no “verified”, no “officially confirmed”, no “resolved”, no “blocked”, no “silenced”, no “call blocked”, no “SMS blocked”, no malware removal, no remote access disconnected claim, no device cleaned claim, no account/card auto-freeze, no payment cancellation/refund/recovery guarantee, no legal/financial result guarantee, no automatic monitoring/scanning/deletion/redaction/blocking/calling/messaging/lookup/verification/storage.
   - Add/extend source-reading tests that prove the UI share helper uses only `ACTION_SEND` + `text/plain` + builder summary and does not use `Intent.EXTRA_STREAM`, `ClipData`, URI grants, `ContactsContract`, `CallLog`, `SmsManager`, `Intent.ACTION_CALL`, `Intent.ACTION_DIAL` in the new slice, raw `ScamAnalysis`, `analysis.input`, `SafetyPlan` literal values, `IncidentPreviewRedactor`, incident store contents, official directory phone values, package manager scan results, notification APIs, call-screening APIs, or persistence mutation.
   - Add/extend source scan for scheduler APIs, manifest permission invariants, clipboard APIs, call/SMS/contact/call-log APIs, notification listener APIs, payment/banking APIs, network/browser/map APIs, official lookup APIs, recontact storage/persistence APIs, and device-control APIs. Explicitly forbid `CallLog`, `Intent.ACTION_CALL`, `Intent.ACTION_DIAL`, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, `PhoneStateListener`, `SmsManager`, `ContactsContract`, `NotificationListenerService`, `NotificationManager`, `ClipboardManager`, `BillingClient`, `NfcAdapter`, `AccountManager`, `DevicePolicyManager`, `AccessibilityService`, `VpnService`, `UsageStatsManager`, `PackageInstaller`, `ACTION_VIEW` for web URLs in the new slice, `HttpURLConnection`, `OkHttp`, `WebView`, `Geocoder`, `PackageManager.getInstalledPackages`, `prefs.edit`, `putString`, `putBoolean`, `.save(` in the new slice, and any new `<uses-permission>`.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No network calls, web browsing, maps/geocoding, official-number lookup, phone-number validation, URL/domain validation, link scanning, identity verification, official confirmation, spam scoring, or blocklist lookup.
- No contact reading, contact writing, contact picking, call-log reading, auto dial, auto call, auto hang-up, call screening, call blocking, call state, SMS send/read, recipient choosing, notification listening, or notification monitoring.
- No clipboard reading/writing.
- No accessibility service, device admin, VPN, usage stats, package/app inventory scanning, app install/uninstall, settings mutation, app ops, or MDM behavior.
- No payment APIs, banking APIs, NFC, account-manager integration, payment cancellation, refund, account/card freeze, or recovery automation.
- No automatic redaction, scanning, monitoring, blocking, muting, notification filtering, lookup, verification, removal, cleaning, disconnecting, freezing, transfer, refund, recovery, or recontact storage.
- No mutation of pasted text, call state, call logs, contacts, SMS, notifications, incident log, safety plan, official directory, account/card/bank/device settings, installed apps, package state, accessibility settings, network/VPN state, notification settings, money movement, verification result, or recontact/boundary status.
- No alarms, notifications, calendar events, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No new persistence for card choices, progress, reads, shares, names, assignments, checkmarks, notes, clipboard contents, recontact status, boundary status, blocked/muted status, contact/channel values, official numbers, package names, device status, or history.
- Opening the screen or pressing route/share buttons must not add/modify/delete incident records.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only.
- Do not ask users to paste names, contacts, exact phone numbers, account/card numbers, OTP/PIN, passwords, safe-word answers, URLs/domains, resident IDs, device IDs, installed app/package names, official numbers, screenshots with credentials, call-log details, notification contents, recontact result values, or raw scam text.
- Do not claim calls/messages are blocked, muted, filtered, reported, silenced, safe, verified, officially confirmed, resolved, removed, cleaned, disconnected, frozen, cancelled, refunded, recovered, or automatically protected.

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
for token in ['CALL_PHONE','READ_SMS','READ_CALL_LOG','READ_CONTACTS','WRITE_CONTACTS','READ_PHONE_STATE','ANSWER_PHONE_CALLS','RECORD_AUDIO','INTERNET','ACCESS_FINE_LOCATION','ACCESS_COARSE_LOCATION','POST_NOTIFICATIONS','SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED','PACKAGE_USAGE_STATS','QUERY_ALL_PACKAGES','BIND_ACCESSIBILITY_SERVICE','BIND_DEVICE_ADMIN','BIND_NOTIFICATION_LISTENER_SERVICE','BIND_VPN_SERVICE','REQUEST_INSTALL_PACKAGES']:
    print(f'{token}={token in manifest}')
needle='android:allowBackup="false"'
print(f'allowBackup=false={needle in manifest}')
print(f'uses-permission-tag={"<uses-permission" in manifest}')
needles = [
    'AlarmManager','NotificationManager','NotificationCompat','WorkManager','JobScheduler',
    'CalendarContract','ForegroundService','BroadcastReceiver','POST_NOTIFICATIONS',
    'SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED','ContactsContract','CallLog','SmsManager',
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

Note: repo-wide scan may continue to show the unchanged legacy `Intent.ACTION_DIAL` in `MainActivity.kt`. If so, add a bounded slice-specific source scan/test proving the new Recontact Boundary slice itself does not contain `ACTION_DIAL`, `ACTION_CALL`, `CallLog`, contact APIs, call-screening APIs, notification APIs, network/browser/map APIs, official lookup, persistence mutation, recontact storage, or raw/saved values.

Also inspect focused JUnit XML for the new test and key privacy tests, e.g. `RecontactBoundaryCoachTest`, `PrivacyCenterBuilderTest`, `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-recontact-boundary-coach.md
git commit -m "feat: add recontact boundary coach"
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
