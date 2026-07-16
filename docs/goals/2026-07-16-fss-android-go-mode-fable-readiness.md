# Claude Code Goal — Fable `/go` continuation: Family Readiness Scorecard

Date: 2026-07-16 13:27 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Model requested by user/orchestrator: Claude Code `fable`.

You are Claude Code Fable being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Fable family-drill slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
# => BUILD SUCCESSFUL in 1s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 874K
git diff --check
# => clean
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 3` of `origin/main`
- latest local commits:
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
- latest commit context (`git log --oneline -7`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyDrill.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentResponse.kt`
- `app/src/main/java/com/mongmong/familyshield/ActionPlan.kt`
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

## `/go` task: Family Readiness Scorecard slice

The app now has prevention analysis, action-plan results, safety plan, incident response center, family safety drill, share entry, logs, and safe reports. The next obvious MVP slice is a **family readiness scorecard** so users immediately know whether their household is prepared before a real scam.

Implement a focused slice that includes all of the following:

1. **New deterministic readiness model**
   - Add a small Kotlin model/builder that evaluates a `SafetyPlan`.
   - Inputs should use only the existing local safety plan fields: safe word/question, trusted contacts, callback script.
   - Output should include:
     - Korean status title/level, e.g. 준비 부족 / 준비 중 / 준비 좋음;
     - score or completed item count;
     - checklist items with done/missing state;
     - next recommended action in Korean.
   - Suggested checklist:
     - safe word/question saved;
     - at least one trusted callback contact saved;
     - callback script saved;
     - optional: two or more contacts for redundancy.
   - Keep deterministic and unit-testable.
   - No network, no permissions, no raw scam text.

2. **Visible UI integration**
   - Show the readiness scorecard on Home, near the top after the privacy/product summary.
   - If items are missing, provide CTA buttons to `가족 안전 계획 설정` and `가족 안전 연습 / 3분 훈련`.
   - If ready, still offer drill and edit-plan actions.
   - Keep simple native Views, Korean copy, and avoid clutter.

3. **Drill integration**
   - On the drill screen, show a short readiness hint based on the current safety plan: if missing safe word/contact, prompt setup; if ready, say the saved routine is ready for practice.
   - Do not persist drill state unless needed; prefer no new persistence.

4. **Tests**
   - Unit tests for the readiness model covering:
     - empty plan = low readiness and setup recommendations;
     - safe word only;
     - contact only;
     - complete plan with script and multiple contacts = high readiness;
     - deterministic checklist labels/actions.

5. **Docs**
   - Update `README.md` to document the readiness scorecard as local-only and based only on the saved safety plan.

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
feat: add family readiness scorecard
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the family readiness scorecard slice, or a truthful blocker handoff exists with exact evidence.
