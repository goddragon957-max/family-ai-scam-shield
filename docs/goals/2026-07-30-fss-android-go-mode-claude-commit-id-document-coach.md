# FSS Android /go continuation — Commit IdDocumentSelfieCapturePauseCoach + next slice

Repo: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Claude Code
Model target: `fable` (or any available Claude model, fallback to `sonnet`/`opus` if `fable` is session-limited; do NOT retry on limit — pick the best available and proceed)
Mode: `/go` continuous execution
Git policy: **local commit only, no push** (push stays blocked until the user explicitly asks)

## Verified starting state (Hermes pre-flight, 2026-07-30)

Hermes independently verified the current state minutes before this handoff:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew clean test assembleDebug
# => BUILD SUCCESSFUL in 1m 31s
# => testDebugUnitTest total: tests=699 failures=0 errors=0
# => IdDocumentSelfieCapturePauseCoachTest: 18/18 passing
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 1,640,638 bytes
git diff --check
# => clean (tracked files only)
```

Working tree currently has uncommitted changes from the previous (interrupted) Codex session — these compile and pass tests, but were never committed:

- Modified: `README.md`, `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`,
  `app/src/main/java/com/mongmong/familyshield/PrivacyCenter.kt`,
  `app/src/test/java/com/mongmong/familyshield/PrivacyCenterBuilderTest.kt`,
  `app/src/test/java/com/mongmong/familyshield/PrivacyPostureRegressionTest.kt`
- Untracked:
  - `app/src/main/java/com/mongmong/familyshield/IdDocumentSelfieCapturePauseCoach.kt`
  - `app/src/test/java/com/mongmong/familyshield/IdDocumentSelfieCapturePauseCoachTest.kt`
  - `docs/goals/2026-07-17-fss-android-go-mode-codex-id-document-selfie-capture-pause-coach.md`

Git state:

- HEAD: `808f589 feat: add sim port-out pause coach`
- `main...origin/main: [ahead 48]`
- Remote: `https://github.com/goddragon957-max/family-ai-scam-shield.git`
- `gh` auth was just re-established this session for both `SoSyn2ne` and `goddragon957-max` (keyring), but **do not push** unless explicitly asked.

Permission/manifest invariants (all must remain true):

- No `CALL_PHONE`, `READ_SMS`, `READ_CALL_LOG`, `READ_CONTACTS`, `RECORD_AUDIO`, `INTERNET`,
  `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`.
- No `<uses-permission>` tag at all.
- `android:allowBackup="false"` stays in the manifest.
- No `AlarmManager`, `NotificationManager`, `NotificationCompat`, `WorkManager`, `JobScheduler`,
  `CalendarContract`, `ForegroundService`, `BroadcastReceiver`, `ContactsContract`, `SmsManager`,
  `Intent.EXTRA_STREAM`, `ClipData`, `FLAG_GRANT_READ_URI_PERMISSION`,
  `ClipboardManager`, `getPrimaryClip`, `setPrimaryClip`.

## Task 1 — Commit the uncommitted IdDocumentSelfieCapturePauseCoach slice

The IdDocument Selfie / ID Document Capture Pause Coach was implemented and tested, but the
previous session timed out before the commit landed. The first job of this `/go` round is to
land that work as a single coherent commit.

Steps:

1. Inspect the diff:
   - `git status --short --branch --ignored=matching`
   - `git diff --stat`
   - `git diff -- app/src/main/java/com/mongmong/familyshield/MainActivity.kt app/src/main/java/com/mongmong/familyshield/PrivacyCenter.kt`
   - `cat app/src/main/java/com/mongmong/familyshield/IdDocumentSelfieCapturePauseCoach.kt` (read full)
   - `cat app/src/test/java/com/mongmong/familyshield/IdDocumentSelfieCapturePauseCoachTest.kt` (read full)
   - `cat docs/goals/2026-07-17-fss-android-go-mode-codex-id-document-selfie-capture-pause-coach.md`
2. Make sure the slice is internally consistent:
   - The untracked Coach + Test are wired into the existing MainActivity navigation.
   - `PrivacyCenter.kt` change adds the new feature to the privacy inventory copy.
   - `PrivacyCenterBuilderTest.kt` and `PrivacyPostureRegressionTest.kt` cover the new feature.
   - `README.md` documents the new flow.
   - The test file imports the Coach class — if not, fix it minimally.
3. Do not introduce any new permissions, network access, scheduler/notification APIs,
   clipboard access, or auto-redaction. The Coach is a deterministic local guide only.
4. Re-run verification:
   ```bash
   export JAVA_HOME="$PWD/.tools/jdk-17"
   export PATH="$JAVA_HOME/bin:$PATH"
   export ANDROID_HOME="$PWD/.tools/android-sdk"
   ./gradlew test assembleDebug lintDebug
   git diff --check
   ```
5. Confirm APK still builds:
   `app/build/outputs/apk/debug/app-debug.apk` should exist and be ≥ 1.6M.
6. Stage the right files and commit:
   ```bash
   git add README.md \
           app/src/main/java/com/mongmong/familyshield/MainActivity.kt \
           app/src/main/java/com/mongmong/familyshield/PrivacyCenter.kt \
           app/src/main/java/com/mongmong/familyshield/IdDocumentSelfieCapturePauseCoach.kt \
           app/src/test/java/com/mongmong/familyshield/PrivacyCenterBuilderTest.kt \
           app/src/test/java/com/mongmong/familyshield/PrivacyPostureRegressionTest.kt \
           app/src/test/java/com/mongmong/familyshield/IdDocumentSelfieCapturePauseCoachTest.kt \
           docs/goals/2026-07-17-fss-android-go-mode-codex-id-document-selfie-capture-pause-coach.md
   git commit -m "feat: add id document selfie capture pause coach"
   ```
7. Do **not** push.

## Task 2 — Propose the next cohesive /go slice

After Task 1 lands, propose (and, if the user-equivalent intent is to keep `/go` going,
also implement) the next cohesive slice. The repo is already saturated with pause-coach
features; the next logical user need is something a normal family would actually hit early.

Pick **one** of these candidates based on what the existing app still lacks (read first):

A. **"지금 뭐부터 할지" First-Time User Onboarding Overlay**
   - For users who finished the consent screen but don't know where to start.
   - Static overlay/card on Home with three entry choices: paste suspicious text, run a
     family safety drill, open Privacy Center. No persistence, no auto-routing.
B. **"증거 화면캡처 가드" Screenshot Privacy Guard**
   - When the user is about to take a screenshot on a screen containing redacted
     incident data, a non-blocking reminder that the screenshot is not protected by the
     app. Pure copy, no FLAG_SECURE (we explicitly avoid forcing FLAG_SECURE on the
     whole app), no auto-mutation, no API hooks.
C. **"신고 후 24시간 체크인" Post-Report 24h Family Check-in**
   - Manual checklist the family uses the day after filing a report. No notifications,
     no alarms, no scheduler. Static, local, shareable.
D. **"보호자 동의 워크플로우" Family Protector Handoff Note**
   - If the user is helping an older relative, a static note template explaining what
     the app does and does not do, and what the family should still verify themselves
     (e.g., call the bank on a known number, never a number from the suspicious text).

Read the existing code, then choose the slice that is most absent from the current flow
and that a real family would notice missing. If you are confident in one, implement it
with the same standards as the prior slices:

- deterministic local builder/model
- one screen reachable from Home and from at least two other existing screens
- ACTION_SEND text-only manual share
- Privacy Center inventory updated
- README updated
- regression tests covering all the privacy/source invariants above
- local commit, no push

If you are not confident, write the goal file for the next slice, mark Task 2 as
`PROPOSED` (commit not required), and stop cleanly.

## Hard rules

- Real Android Kotlin project only — no web/mock rewrite, no Compose migration.
- Package stays `com.mongmong.familyshield`. Application id unchanged.
- Local-only, no server, no internet, no monitoring.
- No new permissions. No scheduler/notification/clipboard APIs.
- `android:allowBackup="false"` stays. No `<uses-permission>` ever.
- Korean copy is calm, practical, non-alarmist. Never claim "100% safe", "blocked",
  "removed", "recovered", "legal guarantee", or "guaranteed refund".
- Never push. Never delete a remote branch. Never force-push.
- If verification fails, do not commit — write a truthful `BLOCKED` handoff instead.

## Toolchain

Use the repo-local JDK/Android SDK:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
```

`scripts/bootstrap-android.sh` may be inspected but the local toolchain is already in
place; do not try to re-bootstrap unless verification is blocked by a missing tool.

## GitHub auth note

`gh` is now authenticated for both `SoSyn2ne` and `goddragon957-max`. **Do not push.**
Push is blocked until the user explicitly asks for it. The user is currently
`SoSyn2ne` (active). The remote (`goddragon957-max/family-ai-scam-shield`) is reachable
but not the active account — `git push` would prompt for credentials. Stay local.

## Final response schema

End with exactly one heading. Use `DONE` only when both Task 1 commit and (if attempted)
Task 2 commit are verified. Use `BLOCKED` if anything is genuinely stuck.

### DONE
- Files changed summary
- Exact verification commands + pass/fail summary
- APK path + byte size
- Local commit SHA(s) and message(s)
- Confirm: no push performed

### PROPOSED
- Task 1 commit summary (must already be `DONE`)
- Chosen next-slice name and one-paragraph rationale
- Goal file path written for the next worker (do not implement it this round)

### BLOCKED
- Exact blocker
- Commands attempted + failing output
- Current git status (clean/dirty, ahead count)
- Safest next step for Hermes to take
