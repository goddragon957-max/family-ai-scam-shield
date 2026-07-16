# FSS Android /go continuation — Active Contact Exit Scripts

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `2712f9d0a38d19191d901ee7257de0d474d2c2aa` — `feat: add safe paste privacy coach`

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

Result: `BUILD SUCCESSFUL`; APK exists with `1243784` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main`.

Focused JUnit XML evidence before this slice:

- `SafePastePrivacyCoachBuilderTest`: debug 16/16, release 16/16, failures 0, errors 0.
- `PrivacyCenterBuilderTest`: debug 9/9, release 9/9, failures 0, errors 0.
- `PrivacyPostureRegressionTest`: debug 19/19, release 19/19, failures 0, errors 0.

Manifest/source invariant scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `RECORD_AUDIO`, `INTERNET`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Source scan has no `AlarmManager`, `NotificationManager`, `NotificationCompat`, `WorkManager`, `JobScheduler`, `CalendarContract`, `ForegroundService`, `BroadcastReceiver`, `ContactsContract`, `SmsManager`, `Intent.EXTRA_STREAM`, `ClipData`, URI grant flag, `ClipboardManager`, `getPrimaryClip`, `setPrimaryClip`, `TextWatcher`, or `addTextChangedListener`.

## Product intent for next cohesive slice

Add an **Active Contact Exit Scripts** screen/card: when a suspicious call, Kakao/SMS conversation, video call, screen-share, or app-install pressure is happening right now, the user needs short sentences they can read to safely end the contact without arguing, revealing secrets, clicking links, or accepting remote control.

This should feel like a family AI guardian: not a call blocker and not a telecom tool, but a private local “what to say now” coach that helps the user escape the pressure and then route to existing family/safety flows.

Examples of script intent:

- “지금은 확인 전이라 통화를 끊겠습니다. 평소 알던 번호로 다시 확인하겠습니다.”
- “문자 링크는 열지 않고 가족과 확인하겠습니다.”
- “앱 설치·화면공유는 하지 않겠습니다.”
- “인증번호나 비밀번호는 말하지 않겠습니다.”
- “새 번호가 아니라 기존 연락처로 다시 확인하겠습니다.”

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `ActiveContactExitScripts.kt`:
   - `ActiveContactExitScriptsBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): ActiveContactExitScripts`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level`, counts/labels only — **not** safe word/callback/contact values
     - `incidentCount.coerceAtLeast(0)` as boolean/count category only
   - Must not use `analysis.input`, raw/pasted text, incident bodies, `IncidentPreviewRedactor`, persisted incident contents, phone/contact values, URLs/domains, accounts, OTP/PIN, resident IDs, or safety plan literal values.

2. Script cards:
   - Fixed manual script cards, for example:
     - “통화 바로 끊기 문장”
     - “문자·카톡 답장 멈춤 문장”
     - “인증번호·비밀번호 거절 문장”
     - “앱 설치·화면공유 거절 문장”
     - “가족 새 번호·목소리 사칭 확인 문장”
     - “기관·은행·수사기관 사칭 종료 문장”
     - “링크·청첩장·택배 문자 멈춤 문장”
     - “끊은 뒤 가족에게 말할 문장”
   - Each card should include:
     - title/label
     - short script text to read manually
     - when to use
     - never say / never do boundaries
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high-risk/financial, credentials/OTP, link/smishing, app-install/remote-control/live-call-control, family/deepfake/number-change, secrecy/impersonation, saved incident, and low/in-progress readiness.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `activeContactExitScriptsScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard near Quick Stop / Safe Call Scripts.
     - Analysis result screen.
     - Quick Stop screen.
     - Safe Flow Router.
     - Safe Paste Privacy Coach.
     - Family Calm Debrief Agenda if appropriate.
   - Action buttons must delegate to existing screens/flows only; do not duplicate official help/account/evidence/callback implementation.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.
   - Do **not** use `Intent.ACTION_CALL`, `Intent.ACTION_DIAL`, SMS APIs, notifications, alarms, or call-state APIs. Scripts are text on screen only; user manually speaks/copies if they choose.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new exit scripts are named as local/manual/no-permission/no-network/no-call/no-SMS/no-contact/no-storage/no auto-send/no call blocking.
   - Update `README.md` flow and add a section for the new feature.

5. Tests:
   - Add `ActiveContactExitScriptsBuilderTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default scripts are conservative and do not call anything safe.
     - high risk / financial / urgent transfer / credentials recommend hang-up, credential refusal, official/account/evidence routes.
     - app-install/live-call-control recommend app/screen-share refusal and trusted-helper/account routes without malware-removal guarantees.
     - link/smishing recommend no-click/no-reply script plus checker/exposure/family-warning/privacy routes without link-scan claims.
     - family/deepfake/number-change recommend new-number refusal, family callback, safe call scripts, and family warning routes.
     - secrecy/impersonation recommend “I will not keep this secret / I am ending contact” and official route.
     - saved incident changes recommendations without reading incident contents.
     - low/in-progress readiness adds callback/readiness/family support while ready copy remains cautious.
     - every script card has complete fields, stable order, unique routes, and all action IDs map to existing flows.
     - share summary contains only generic script/route/action labels and no raw input, redacted clue, safety plan literal, contact, phone number, URL/domain, long numbers, account/OTP/PIN/resident-like values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no contacts/no auto-send/no network/no permissions/no scheduler/no mutation/no call/SMS automation/no call blocking.
     - copy avoids unsafe guarantees: no “100%”, no “safe”, no “blocked”, no malware removal, no account/card auto-freeze, no legal/financial result guarantee, no automatic monitoring/scanning/deletion/redaction/blocking/calling.
   - Add/extend source-reading tests that prove the UI share helper uses only `ACTION_SEND` + `text/plain` + builder summary and does not use `Intent.EXTRA_STREAM`, `ClipData`, URI grants, `ContactsContract`, `SmsManager`, raw `ScamAnalysis`, `analysis.input`, `SafetyPlan` literal values, `IncidentPreviewRedactor`, incident store contents, or persistence mutation.
   - Add/extend source scan for scheduler APIs, manifest permission invariants, clipboard APIs, and call/SMS automation APIs. Explicitly forbid `Intent.ACTION_CALL`, `Intent.ACTION_DIAL`, `TelecomManager`, `TelephonyManager`, `CallScreeningService`, `PhoneStateListener`, `SMS`, `SmsManager`, `ClipboardManager`, `getPrimaryClip`, and `setPrimaryClip` for this slice.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No network calls.
- No automatic SMS/call/contact/clipboard reading.
- No call blocking, call screening, auto dial, auto hang-up, auto SMS, or recipient choosing.
- No automatic redaction, scanning, monitoring, blocking, removal, freezing, or recovery.
- No mutation of the user's pasted text, call state, contacts, incident log, safety plan, or device settings.
- No alarms, notifications, calendar events, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No new persistence for script choices, cards, progress, reads, shares, names, assignments, checkmarks, notes, clipboard contents, call status, or history.
- Opening the screen or pressing route/share buttons must not add/modify/delete incident records.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only.
- Do not ask users to paste names, contacts, exact phone numbers, account numbers, OTP/PIN, passwords, safe-word answers, URLs/domains, resident IDs, or raw scam text.
- Do not claim official/legal/financial outcome, safety, scam certainty, malware removal, account/card freeze, recovery, blocking, or automatic privacy/call protection.

## Implementation record

This slice is implemented as one deterministic Kotlin builder plus native `MainActivity` rendering and routing.
The screen shows fixed manual wording cards and delegates every next action to an existing app flow. It does not
add telecom behavior, permissions, background work, or feature-specific persistence. The only outward action is a
user-triggered `ACTION_SEND` `text/plain` chooser that receives the builder-produced `familyShareSummary`.

Privacy Center and README coverage explicitly inventory the feature as local-only, stateless, manual, no-network,
no-permission, no-contact/message/call/clipboard reading, no recipient selection or automatic send, no call
screening/blocking/dial/hang-up or SMS automation, no scheduler, and no mutation of pasted text, call state,
contacts, incidents, safety plans, or device settings. Source-reading regressions scope the `ACTION_DIAL`
prohibition to this new slice because established official-help screens intentionally retain user-confirmed
`ACTION_DIAL` behavior.

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
for token in ['CALL_PHONE','READ_SMS','READ_CALL_LOG','READ_CONTACTS','RECORD_AUDIO','INTERNET','POST_NOTIFICATIONS','SCHEDULE_EXACT_ALARM','RECEIVE_BOOT_COMPLETED']:
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
    'Intent.ACTION_CALL','Intent.ACTION_DIAL','TelecomManager','TelephonyManager','CallScreeningService','PhoneStateListener'
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

Also inspect focused JUnit XML for the new test and key privacy tests, e.g. `ActiveContactExitScriptsBuilderTest`, `PrivacyCenterBuilderTest`, `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-active-contact-exit-scripts.md
git commit -m "feat: add active contact exit scripts"
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
