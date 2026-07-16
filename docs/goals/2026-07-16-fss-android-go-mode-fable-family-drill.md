# Claude Code Goal — Fable `/go` continuation: Family Safety Drill

Date: 2026-07-16 10:37 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Model requested by user/orchestrator: Claude Code `fable`.

You are Claude Code Fable being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes just confirmed Fable is available:

```bash
claude --model fable --permission-mode acceptEdits --effort low -p 'Reply exactly: CLAUDE_READY'
# => CLAUDE_READY
```

Hermes also preflighted the repo:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
# => BUILD SUCCESSFUL in 2s
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 2` of `origin/main`
- latest local commits:
  - `7413c93 feat: add incident response center`
  - `e0dfc47 feat: add guardian action plan flow`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

This launch may use `--dangerously-skip-permissions` because previous non-interactive Claude `acceptEdits` sessions could edit files but sometimes could not run Gradle verification commands. The repo/scope is bounded and safe.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -6`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
- `app/src/main/java/com/mongmong/familyshield/ActionPlan.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentResponse.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentStore.kt`
- report/share builders
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

## `/go` task: Family Safety Drill / rehearsal slice

The app now has prevention analysis, action-plan results, safety plan, incident response center, share entry, logs, and safe reports. The next obvious MVP slice is a **family safety drill** so families can practice the safe-word/callback routine before a real scam.

Implement a focused slice that includes all of the following:

1. **New deterministic drill model**
   - Add a small Kotlin model/builder for safety drill scenarios.
   - Include at least four scenario types relevant to Korean family voice phishing:
     - child/parent phone-number-change money request;
     - prosecutor/police safe-account pressure;
     - remote-control app / screen-share pressure;
     - parcel/invitation smishing link.
   - For each scenario, provide:
     - Korean title;
     - short fake incoming message/call script that is clearly fictional;
     - expected family response steps;
     - pass checklist items.
   - Keep deterministic and unit-testable.
   - No network, no permissions.

2. **Visible UI integration**
   - Add a screen reachable from Home, e.g. “가족 안전 연습 / 3분 훈련”.
   - Let the user choose a scenario and see:
     - fictional prompt;
     - what to say/do;
     - pass checklist.
   - If a family safety plan safe word exists, surface it in the drill screen as the practice question. If not, show a CTA to create the safety plan.
   - Keep UI simple native Views and Korean copy.

3. **Privacy/log consistency**
   - The drill must not store raw user messages; it uses built-in fictional scenarios only.
   - Do not alter incident logs except if you add a clearly separate, local-only drill completion count. Prefer no persistence unless genuinely useful.

4. **Tests**
   - Unit tests for the drill model covering all four scenarios.
   - Test that the scenario text is marked fictional/practice and does not imply real monitoring.

5. **Docs**
   - Update `README.md` to document the family safety drill and how it uses saved safe word/contact plan locally.

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
feat: add family safety drill
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the family safety drill slice, or a truthful blocker handoff exists with exact evidence.
