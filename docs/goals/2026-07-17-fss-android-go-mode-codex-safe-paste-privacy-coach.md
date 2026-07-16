# FSS Android /go continuation — Safe Paste Privacy Coach

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI
Model target: `gpt-5.6-sol`
Reasoning effort: `ultra`
Mode: go-continuation
Git policy: **local commit only, no push**

## Verified starting state

Latest verified commit before this slice:

- `9efe6e155b8da3e152e9535424b4edc282a3f2b6` — `feat: add family calm debrief agenda`

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

Result: `BUILD SUCCESSFUL`; APK exists with `1226436` bytes; `git diff --check` clean; tracked worktree clean except ignored `.gradle/`, `.tools/`, `app/build/`, `local.properties`; branch `main` is ahead of `origin/main`.

Focused JUnit XML evidence before this slice:

- `FamilyCalmDebriefAgendaBuilderTest`: debug 14/14, release 14/14, failures 0, errors 0.
- `PrivacyCenterBuilderTest`: debug 9/9, release 9/9, failures 0, errors 0.
- `PrivacyPostureRegressionTest`: debug 18/18, release 18/18, failures 0, errors 0.

Manifest/source invariant scan before this slice:

- Manifest has no `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `RECORD_AUDIO`, `INTERNET`, `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`.
- `android:allowBackup="false"` is present.
- No `<uses-permission>` tag.
- Source scan has no `AlarmManager`, `NotificationManager`, `NotificationCompat`, `WorkManager`, `JobScheduler`, `CalendarContract`, `ForegroundService`, `BroadcastReceiver`, `ContactsContract`, `SmsManager`, `Intent.EXTRA_STREAM`, `ClipData`, URI grant flag.

## Product intent for next cohesive slice

Add a **Safe Paste Privacy Coach** screen/card: before a user pastes suspicious SMS/Kakao/call text into the checker, the app should help them manually hide unnecessary secrets while still preserving enough context for the local rules-based analysis.

This strengthens the family-protector positioning: the app is not a telecom surveillance app and not a cloud AI; it is a private local coach that tells families what **not** to expose before they paste or share.

The coach must not parse, redact, store, monitor, or auto-edit the user's text. It is a deterministic local guide that says things like:

1. Leave the suspicious wording and pressure pattern if possible.
2. Mask phone/account/OTP/PIN/password/resident-like IDs/safety-plan values.
3. Replace exact links/domains with a generic category like `[링크]` instead of reopening them.
4. Keep only action categories: “송금 요구”, “앱 설치 유도”, “가족 사칭”, etc.
5. Then proceed to existing checker / sample / privacy center / family safety routes.

## Required implementation

Add a narrow, testable, user-facing Android/Kotlin slice:

1. New deterministic model/builder, likely `SafePastePrivacyCoach.kt`:
   - `SafePastePrivacyCoachBuilder.build(analysis: ScamAnalysis?, readiness: FamilyReadiness, incidentCount: Int = 0): SafePastePrivacyCoach`
   - Pure/stateless; no Android dependencies.
   - Uses only:
     - `analysis?.riskLevel`
     - `analysis?.matchedSignals`
     - `readiness.level`, counts/labels only — **not** safe word/callback/contact values
     - `incidentCount.coerceAtLeast(0)` as a boolean/count category only
   - Must not use `analysis.input`, pasted text, incident bodies, `IncidentPreviewRedactor`, persisted incident contents, phone/contact values, URLs/domains, accounts, OTP/PIN, resident IDs, or safety plan literal values.

2. Coach cards/checklist:
   - Fixed manual masking cards/steps, for example:
     - “원문 전체를 그대로 붙이지 않기”
     - “전화·계좌·신분정보 가리기”
     - “OTP/PIN/비밀번호/안전질문 답 가리기”
     - “링크·도메인은 `[링크]`로 바꾸기”
     - “행동 종류는 남기기”
     - “붙여넣은 뒤 기존 검사로 이동하기”
   - Each card should include:
     - title/label
     - why it matters
     - how to mask manually
     - what not to paste/share
     - primary existing route action
     - optional secondary existing route actions
   - Recommended cards/actions should change deterministically for high-risk/financial, credentials/OTP, link/smishing, app-install/remote-control, family/deepfake/number-change, secrecy/impersonation, saved incident, and low/in-progress readiness.

3. Native UI integration in `MainActivity.kt`:
   - Add a new `Screen` enum value and render method, e.g. `safePastePrivacyCoachScreen()`.
   - Add route buttons from relevant places:
     - Home dashboard near checker / privacy center.
     - Onboarding or checker screen before/near paste area if practical.
     - Analysis result screen for “다음부터 붙여넣기 전 가리기”.
     - Privacy Center.
     - Safe Flow Router or Family Calm Debrief if appropriate.
   - Action buttons must delegate to existing screens/flows only; do not duplicate checker, privacy center, sample library, official help, or account/evidence implementation.
   - Add a manual share button that opens `Intent.ACTION_SEND`, `type = "text/plain"`, with only the builder-produced safe `familyShareSummary`.

4. Privacy Center / documentation coverage:
   - Update `PrivacyCenter.kt` if it has explicit feature inventory/copy so the new safe-paste coach is named as local/manual/no-permission/no-network/no-monitoring/no-storage/no auto-redaction.
   - Update `README.md` flow and add a section for the new feature.

5. Tests:
   - Add `SafePastePrivacyCoachBuilderTest.kt` with meaningful JUnit coverage.
   - Update existing privacy/source regression tests as needed.
   - Include tests proving:
     - no-analysis/default coach is conservative and does not call pasted text safe.
     - high risk / financial / credentials / urgent transfer recommend account/OTP/financial masking plus checker/account/evidence/official routes.
     - app-install/live-call-control recommend app/remote-control masking and trusted-helper/account routes without malware-removal guarantees.
     - link/smishing recommend link/domain masking plus checker/exposure/family-warning/privacy routes without link-scan claims.
     - family/deepfake/number-change recommend callback/safety-question masking and family warning routes.
     - secrecy/impersonation recommend hiding raw messages, stopping pressure, and official route.
     - saved incident changes recommendations without reading incident contents.
     - low/in-progress readiness adds callback/readiness/privacy support while ready copy remains cautious.
     - every card has complete fields, stable order, unique routes, and all action IDs map to existing flows.
     - share summary contains only generic masking/checklist/route/action labels and no raw input, redacted clue, safety plan literal, contact, phone number, URL/domain, long numbers, account/OTP/PIN/resident-like values.
     - builder is deterministic and clamps negative incident counts.
     - copy says manual/local/stateless/no storage/no contacts/no auto-send/no network/no permissions/no scheduler/no mutation/no automatic redaction.
     - copy avoids unsafe guarantees: no “100%”, no “safe”, no “blocked”, no malware removal, no account/card auto-freeze, no legal/financial result guarantee, no automatic monitoring/scanning/deletion/redaction.
   - Add/extend source-reading tests that prove the UI share helper uses only `ACTION_SEND` + `text/plain` + builder summary and does not use `Intent.EXTRA_STREAM`, `ClipData`, URI grants, `ContactsContract`, `SmsManager`, raw `ScamAnalysis`, `analysis.input`, `SafetyPlan` literal values, `IncidentPreviewRedactor`, incident store contents, or persistence mutation.
   - Add/extend source scan for scheduler APIs, manifest permission invariants, and no automatic clipboard reading/editing. Explicitly forbid `ClipboardManager`, `getPrimaryClip`, `setPrimaryClip`, text watchers for auto-redaction, and any silent paste transformation.

## Non-negotiable privacy/product invariants

- No new Android permissions.
- No network calls.
- No automatic SMS/call/contact/clipboard reading.
- No automatic redaction, scanning, monitoring, blocking, removal, freezing, or recovery.
- No mutation of the user's pasted text unless the user manually edits it in the existing checker.
- No message auto-send and no recipient choosing.
- No alarms, notifications, calendar events, WorkManager, JobScheduler, foreground service, broadcast receivers, or background workers.
- No new persistence for coach choices, cards, progress, reads, shares, names, assignments, checkmarks, notes, clipboard contents, or history.
- Opening the screen or pressing route/share buttons must not add/modify/delete incident records.
- Share must be manual `ACTION_SEND` text only and must use builder-produced safe summary only.
- Do not ask users to paste names, contacts, exact phone numbers, account numbers, OTP/PIN, passwords, safe-word answers, URLs/domains, resident IDs, or raw scam text.
- Do not claim official/legal/financial outcome, safety, scam certainty, malware removal, account/card freeze, recovery, blocking, or automatic privacy protection.

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
    'ClipboardManager','getPrimaryClip','setPrimaryClip'
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

Also inspect focused JUnit XML for the new test and key privacy tests, e.g. `SafePastePrivacyCoachBuilderTest`, `PrivacyCenterBuilderTest`, `PrivacyPostureRegressionTest`.

## Commit policy

After green verification and quick source review, create exactly one local commit, likely:

```bash
git add README.md app/src/main/java/com/mongmong/familyshield app/src/test/java/com/mongmong/familyshield docs/goals/2026-07-17-fss-android-go-mode-codex-safe-paste-privacy-coach.md
git commit -m "feat: add safe paste privacy coach"
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
