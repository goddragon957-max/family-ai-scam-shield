# Claude Code Goal — Fable `/go` continuation: Evidence Preservation Kit

Date: 2026-07-16 18:25 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Original model requested by user/orchestrator: Claude Code `fable`.
Fallback update: Fable, Sonnet, Opus, and Haiku all returned the same Claude session limit (`resets 11:10pm Asia/Seoul`) before making code changes. Hermes verified Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"` and is using that as the active `/go` worker so the slice keeps moving.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous sample-library slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
# => BUILD SUCCESSFUL in 4s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 883K
git diff --check
# => clean
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 5` of `origin/main`
- latest local commits:
  - `1181b99 feat: add scam sample library`
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
- `app/src/main/java/com/mongmong/familyshield/IncidentResponse.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentResponseReportBuilder.kt`
- `app/src/main/java/com/mongmong/familyshield/ActionPlan.kt`
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

## `/go` task: Evidence Preservation Kit slice

The app now detects suspicious text, creates action plans/reports, keeps a local incident log, has a response center, family drill, readiness scorecard, and fictional sample library. The next obvious MVP slice is an **evidence preservation kit**: when a family suspects 피해, they need a calm checklist for what to save before deleting apps/messages or calling institutions.

Implement a focused slice that includes all of the following:

1. **New deterministic evidence model**
   - Add a small Kotlin model/builder for evidence preservation guidance.
   - It should be deterministic, unit-testable, and local-only.
   - Inputs may be an `IncidentSituation` from the existing response center, or a generic mode when no situation is selected.
   - Output should include:
     - Korean title;
     - ordered checklist items;
     - “do not do yet” cautions;
     - short family-share summary that does **not** include raw pasted scam text;
     - optional official-report prep notes.
   - Cover at least these situations:
     - money sent / gift card / crypto handed over;
     - remote-control app or screen-share allowed;
     - account/password/OTP/ID shared;
     - suspicious link clicked but no money/info yet;
     - generic “not sure yet”.
   - Suggested checklist examples:
     - screenshot transaction/recipient/call/message timestamps;
     - write down time, amount, account/wallet/gift-card PIN if relevant;
     - keep phone powered and avoid deleting messages before screenshots;
     - if remote-control was allowed, disconnect network or stop app, then use another safe device for password changes;
     - keep bank/police report numbers after contact.
   - Avoid implying automatic evidence capture, hidden monitoring, network upload, or legal guarantee.

2. **Visible UI integration**
   - Add a screen reachable from Home, e.g. “증거 보존 체크리스트”.
   - Also add a CTA from the existing Response Center so users can open evidence guidance after choosing/considering an incident situation.
   - If a situation is selected in the Response Center, preselect or show the matching evidence kit. If not, show the generic kit first and let the user switch between situations.
   - Show ordered 체크리스트, 주의할 점, and a share button for the safe family summary.
   - The share summary must use Android `ACTION_SEND` text and must not include pasted original scam text.
   - Do not store evidence kit selections or checklist completion state.

3. **Tests**
   - Unit tests covering generic kit and each `IncidentSituation` mapping.
   - Verify ordering/determinism.
   - Verify every kit includes at least 3 checklist items and at least one caution.
   - Verify share summaries contain situation/evidence guidance but not monitoring/server/network-upload claims and not placeholder raw scam text.

4. **Docs**
   - Update `README.md` to document the Evidence Preservation Kit as local-only guidance with no automatic capture/upload.

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
feat: add evidence preservation kit
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the evidence preservation kit slice, or a truthful blocker handoff exists with exact evidence.
