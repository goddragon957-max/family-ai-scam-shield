# Claude Code Goal — `/go` continuation: Incident Response Center

Date: 2026-07-16 10:28 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Model requested by orchestrator: Claude Code `sonnet` because `fable` is blocked. This launch may use `--dangerously-skip-permissions` because the previous `acceptEdits` run could edit files but could not run Gradle verification commands.

You are Claude Code being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes just confirmed:

```bash
claude --model sonnet --permission-mode acceptEdits --effort low -p 'Reply exactly: CLAUDE_READY'
# => CLAUDE_READY

export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
# => BUILD SUCCESSFUL in 1s
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 1` of `origin/main`
- last local commit: `e0dfc47 feat: add guardian action plan flow`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit/diff context (`git log --oneline -5`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
- `app/src/main/java/com/mongmong/familyshield/ActionPlan.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentStore.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyReportBuilder.kt`
- tests under `app/src/test/java/com/mongmong/familyshield/`

## Product identity / non-negotiables

This is a Korean Android app for a **family AI voice-phishing guardian**.

Keep:

- real Android Kotlin project, no web/mock rewrite
- package/application id: `com.mongmong.familyshield`
- simple native Android Views, no Compose migration
- local-only MVP: no server, no internet/network analysis
- no SMS/call/contact/microphone/internet permissions
- no automatic surveillance; user-pasted/shared text only
- calm, practical Korean copy
- `android:allowBackup="false"`
- no build artifacts or local toolchain files in commits

## `/go` task: implement the next cohesive product slice

The app now has onboarding, checker, analyzer, action-plan result screen, family safety plan, incident log, share entry, and safe family report. The next obvious MVP slice is an **Incident Response Center** for users who already sent money, installed an app, or shared credentials.

Implement a focused slice that includes all of the following:

1. **New deterministic response model**
   - Add a small Kotlin model/builder for incident-response guidance.
   - It should produce grouped, ordered Korean steps for at least:
     - money sent / transfer or gift card already completed;
     - app installed / remote control or screen sharing already happened;
     - OTP/password/account/ID information already shared;
     - suspicious link opened but no payment/info yet.
   - Keep it deterministic and unit-testable.
   - It must not require Android permissions or network calls.

2. **Visible UI integration**
   - Add a screen reachable from Home and Result, e.g. “이미 피해가 의심돼요 / 긴급 대응 센터”.
   - The screen should let the user choose one of the above situations and show the matching response steps.
   - Include practical Korean guidance for:
     - bank/card payment stop or card lock;
     - call 112 / 182 / 1332 as appropriate;
     - uninstall remote-control apps and change passwords from a safe device;
     - preserve evidence without sharing original sensitive text.
   - Add `ACTION_DIAL` buttons for emergency/support numbers using existing no-`CALL_PHONE` pattern. Do not request `CALL_PHONE` permission.

3. **Report/log/privacy consistency**
   - Do not store raw sensitive text for this response-center flow.
   - If you add a shareable response summary, ensure it contains only the selected situation and steps, not pasted scam content.
   - Keep existing incident log behavior bounded/local.

4. **Tests**
   - Unit tests for the response model covering at least the four situations above.
   - If you add a share/report builder, test that it excludes arbitrary raw scam text.

5. **Docs**
   - Update `README.md` to document the emergency response center and no-permission dial behavior.

## Verification gates

Run fresh gates before claiming completion:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
ls -lh app/build/outputs/apk/debug/app-debug.apk
git diff --check
```

## Git policy

- Create one local commit only if verification passes.
- Do not push.
- Do not commit `.hermes/`, `.gradle/`, `.tools/`, `app/build/`, `local.properties`, or other machine-local files.
- It is okay to commit this goal file with the implementation if useful, but do not include transient logs.

Suggested commit message:

```text
feat: add incident response center
```

## Final response schema

End with exactly one status heading.

### DONE
Include:
- files changed summary
- exact verification commands and pass summary
- APK path and size
- local commit SHA

### BLOCKED
Include:
- exact blocker
- commands attempted
- files changed / whether git tree is dirty
- next command Hermes should run

Completion condition: the repo has a verified local Android debug APK and a local commit for the incident-response slice, or a truthful blocker handoff exists with exact evidence.
