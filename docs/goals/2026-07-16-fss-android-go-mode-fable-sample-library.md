# Claude Code Goal — Fable `/go` continuation: Scam Sample Library

Date: 2026-07-16 13:34 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Model requested by user/orchestrator: Claude Code `fable`.

You are Claude Code Fable being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous readiness slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
# => BUILD SUCCESSFUL in 2s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 878K
git diff --check
# => clean
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 4` of `origin/main`
- latest local commits:
  - `a386649 feat: add family readiness scorecard`
  - `f53dd6d feat: add family safety drill`
  - `7413c93 feat: add incident response center`
  - `e0dfc47 feat: add guardian action plan flow`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

This launch may use `--dangerously-skip-permissions` because previous non-interactive Claude `acceptEdits` sessions could edit files but sometimes could not run Gradle verification commands. The repo/scope is bounded and safe.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -8`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
- `app/src/main/java/com/mongmong/familyshield/ActionPlan.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyDrill.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyReadiness.kt`
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

## `/go` task: Scam Sample Library slice

The app now has prevention analysis, action-plan results, safety plan, incident response center, family safety drill, readiness scorecard, share entry, logs, and safe reports. The next obvious MVP slice is a **fictional scam sample library** so users can try the analyzer safely without needing a real scam message.

Implement a focused slice that includes all of the following:

1. **New deterministic sample model**
   - Add a small Kotlin model/builder for sample scam texts.
   - Include at least five sample categories relevant to this app:
     - family phone-number-change + urgent transfer;
     - prosecutor/police safe-account pressure;
     - remote-control app / screen-share pressure;
     - parcel/invitation smishing link;
     - overseas/small payment approval panic bait.
   - For each sample, provide:
     - Korean title;
     - sample text explicitly marked as fictional/practice, e.g. `[연습용 가상 메시지]`;
     - expected risk level;
     - expected key signals for unit tests;
     - short learning note in Korean.
   - Keep deterministic and unit-testable.
   - No network, no permissions, no persistence.

2. **Visible UI integration**
   - Add a screen reachable from Home, e.g. “샘플로 AI 분석 연습”.
   - Let the user choose a sample and see:
     - fictional sample text;
     - why it is suspicious / expected learning note;
     - button to analyze this sample with the existing AI 보호자 checker/result flow.
   - The analyze button should set the sample text into the existing checker/result flow without storing it as a real incident unless the user explicitly taps the existing save button on the result screen.
   - Keep UI simple native Views and Korean copy.

3. **Analyzer integration tests**
   - Unit tests should prove every sample is marked fictional/practice.
   - Unit tests should run each sample through `ScamAnalyzer.analyze` and assert the expected risk level and expected key signals are present.
   - Include at least one test that confirms sample text/learning note does not include monitoring/network/surveillance claims.

4. **Privacy/log consistency**
   - Do not add permissions.
   - Do not persist sample selections.
   - Do not alter incident log storage beyond using the existing explicit save behavior after analysis.

5. **Docs**
   - Update `README.md` to document the sample library as fictional/local-only and useful for practicing the analyzer safely.

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
feat: add scam sample library
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the sample-library slice, or a truthful blocker handoff exists with exact evidence.
