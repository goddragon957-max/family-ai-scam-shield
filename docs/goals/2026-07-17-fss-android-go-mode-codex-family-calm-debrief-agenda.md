# FSS Android /go continuation — Family Calm Debrief Agenda

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `894bb4aff443efffb4b4cc71445218b22b4364b8` — `feat: add family response role board`

Parent evaluator independently ran after the previous worker notification:

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

Result: `BUILD SUCCESSFUL`, APK exists with `1211671` bytes, tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main`.

Focused JUnit XML evidence before this slice:

- `FamilyResponseRoleBoardBuilderTest`: debug 14/14, release 14/14, failures 0, errors 0.
- `PrivacyCenterBuilderTest`: debug 9/9, release 9/9, failures 0, errors 0.
- `PrivacyPostureRegressionTest`: debug 17/17, release 17/17, failures 0, errors 0.

Manifest/source invariant scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `RECORD_AUDIO`, `INTERNET`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Source scan has no `AlarmManager`, `NotificationManager`, `NotificationCompat`, `WorkManager`, `JobScheduler`, `CalendarContract`, `ForegroundService`, `BroadcastReceiver`, `ContactsContract`, `SmsManager`, `Intent.EXTRA_STREAM`, `ClipData`, URI grant flag.

## Product intent for next cohesive slice

Add a **Family Calm Debrief Agenda** screen/card: after a scary call/message or after the role board, the family needs a short, blame-free, privacy-safe way to talk for 5 minutes and decide the next manual route.

This is not a task tracker, not chat monitoring, not contact reading, and not a diary. It is a deterministic local agenda that helps the family say:

1. We stop blaming and calm down.
2. We confirm no more money/info/apps until separate verification.
3. We describe only action categories, not raw scam text or secrets.
4. We choose existing safety routes: callback, role board, exposure triage, account/device protection, evidence, official help, follow-up, privacy center.
5. We share a short no-secret family debrief message manually if needed.

The slice should strengthen the “family 보호자 / AI guardian” positioning: less like a telecom blocking app, more like a private family response coach.

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `FamilyCalmDebriefAgenda.kt`:
   - `FamilyCalmDebriefAgendaBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): FamilyCalmDebriefAgenda`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level`, counts/labels only — **not** safe word/callback/contact values
     - `incidentCount.coerceAtLeast(0)` as a boolean/count category only
   - Must not use `analysis.input`, incident bodies, `IncidentPreviewRedactor`, persisted incident contents, phone/contact values, URLs/domains, accounts, OTP/PIN, resident IDs, or safety plan literal values.

2. Agenda content:
   - Fixed agenda cards/steps, for example:
     - “비난 없이 안정시키기”
     - “돈·정보·앱 행동 멈춤 확인”
     - “행동 종류만 말하기”
     - “가족 본인확인/별도 콜백 정하기”
     - “계정·기기·증거·공식 도움 경로 고르기”
     - “며칠 뒤 다시 확인할 약속 말로 정하기”
   - Each card should include:
     - short title/label
     - what to say/do
     - what not to ask/store/share
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high-risk/financial, remote app, link/smishing, family/deepfake/number-change, secrecy/impersonation, saved incident, and low/in-progress readiness.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `familyCalmDebriefAgendaScreen()`.
   - Add route buttons from home and from relevant existing screens:
     - Home dashboard.
     - Analysis result screen.
     - Family Response Role Board.
     - Follow-up Check-in Planner if appropriate.
     - Trusted Helper Review or Quick Stop if appropriate.
   - Action buttons must delegate to existing screens/flows only; do not duplicate official help/account/evidence implementation.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new debrief agenda is named as local/manual/no-permission/no-network/no-monitoring/no-storage.
   - Update `README.md` flow and add a section for the new feature.

5. Tests:
   - Add `FamilyCalmDebriefAgendaBuilderTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default agenda is conservative and does not call it safe.
     - high risk / financial / credentials / urgent transfer recommend protection/evidence/official/follow-up routes.
     - app-install/live-call-control recommend account/device and evidence/trusted helper routes without removal guarantees.
     - link/smishing recommend exposure/evidence/family warning/privacy routes without link-scan claims.
     - family/deepfake/number-change recommend callback and family warning routes.
     - secrecy/impersonation recommend stop/exposure/official route.
     - saved incident changes recommendations without reading incident contents.
     - low readiness adds callback/readiness/drill/follow-up support while ready copy remains cautious.
     - every card has complete fields, stable order, unique routes, and all action IDs map to existing flows.
     - share summary contains only generic agenda/route/action labels and no raw input, redacted clue, safety plan literal, contact, phone number, URL/domain, long numbers, account/OTP/PIN/resident-like values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no contacts/no auto-send/no network/no permissions/no scheduler/no mutation.
     - copy avoids unsafe guarantees: no “100%”, no “safe”, no “blocked”, no malware removal, no account/card auto-freeze, no legal/financial result guarantee, no automatic monitoring/scanning/deletion.
   - Add/extend source-reading tests that prove the UI share helper uses only `ACTION_SEND` + `text/plain` + builder summary and does not use `Intent.EXTRA_STREAM`, `ClipData`, URI grants, `ContactsContract`, `SmsManager`, raw `ScamAnalysis`, `analysis.input`, `SafetyPlan` literal values, `IncidentPreviewRedactor`, incident store contents, or persistence mutation.
   - Add/extend source scan for scheduler APIs and manifest permission invariants.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No network calls.
- No automatic SMS/call/contact reading.
- No message auto-send and no recipient choosing.
- No automatic monitoring/scanning/blocking/removal/freezing/recovery.
- No alarms, notifications, calendar events, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No new persistence for agenda choices, cards, progress, reads, shares, names, assignments, checkmarks, notes, or history.
- Opening the screen or pressing route/share buttons must not add/modify/delete incident records.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only.
- Do not ask users for names, contacts, exact phone numbers, account numbers, OTP/PIN, passwords, safe-word answers, URLs/domains, resident IDs, or raw scam text.
- Do not claim official/legal/financial outcome, safety, scam certainty, malware removal, account/card freeze, recovery, or blocking.

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
    'Intent.EXTRA_STREAM','ClipData','FLAG_GRANT_READ_URI_PERMISSION'
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

Also inspect focused JUnit XML for the new test and key privacy tests, e.g. `FamilyCalmDebriefAgendaBuilderTest`, `PrivacyCenterBuilderTest`, `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-family-calm-debrief-agenda.md
git commit -m "feat: add family calm debrief agenda"
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
