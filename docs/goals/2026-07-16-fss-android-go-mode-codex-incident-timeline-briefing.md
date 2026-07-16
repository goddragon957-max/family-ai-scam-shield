# Codex Goal — `/go` continuation: Incident Timeline Briefing

Date: 2026-07-16 21:30 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: Claude Fable/Sonnet/Opus/Haiku are session-limited until 23:10 Asia/Seoul, so Hermes is continuing `/go` with the verified Codex fallback.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Safety Flow Router slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
# => BUILD SUCCESSFUL in 1s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 950K
git diff --check
# => clean
```

Additional Hermes checks:

```text
AndroidManifest permissions:
CALL_PHONE=False
READ_SMS=False
READ_CALL_LOG=False
READ_CONTACTS=False
RECORD_AUDIO=False
INTERNET=False
allowBackup=false=True
uses-permission-tag=False
SafetyFlowRouterBuilderTest: debug/release tests=8 failures=0 errors=0
PrivacyCenterBuilderTest: debug/release tests=9 failures=0 errors=0
PrivacyPostureRegressionTest: debug/release tests=3 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 12` of `origin/main`
- latest local commits:
  - `2a6b396 feat: add safety flow router`
  - `b194ad2 feat: add privacy center`
  - `bf55ce0 feat: add family callback verification card`
  - `8a23aa3 feat: add official report prep card`
  - `9743ac4 feat: add official help directory`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now has saved privacy-safe incident logs, response/help/evidence flows, and a safety router. The next cohesive slice is an **Incident Timeline Briefing**: a local-only summary that turns saved incident logs into a calm family/police-prep overview without leaking raw scam text or sensitive values.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -10`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentStore.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentPreviewRedactor.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyFlowRouter.kt`
- `app/src/main/java/com/mongmong/familyshield/EvidencePreservationKit.kt`
- `app/src/main/java/com/mongmong/familyshield/OfficialHelpDirectory.kt`
- tests under `app/src/test/java/com/mongmong/familyshield/`

## Product identity / non-negotiables

This is a Korean Android app for a **family AI voice-phishing guardian**.

Keep:

- real Android Kotlin project, no web/mock rewrite
- package/application id: `com.mongmong.familyshield`
- simple native Android Views, no Compose migration
- local-only MVP: no server, no internet/network analysis
- no SMS/call/contact/microphone/internet permissions
- phone actions must use `ACTION_DIAL`, not `CALL_PHONE`
- no automatic surveillance; user-pasted/shared text only
- calm, practical Korean copy
- `android:allowBackup="false"`
- no build artifacts or local toolchain files in commits

## `/go` task: Incident Timeline Briefing slice

Implement a focused local-only incident briefing screen that summarizes saved incident logs and helps the user decide what to do next.

1. **New deterministic briefing model**
   - Add a small pure Kotlin builder/model, e.g. `IncidentTimelineBriefingBuilder`.
   - It must be unit-testable and not depend on Android UI classes.
   - Suggested input can be `List<Incident>` plus optional stable display-time mapper/data class if needed. Keep deterministic in tests.
   - Output should include:
     - Korean title and intro;
     - empty-state copy;
     - summary metrics: total count, risk counts, highest risk, latest incident time/label if present;
     - top signal labels/categories across saved incidents;
     - timeline items in stable newest-first order;
     - per-item fields: display time, risk label, score, signal summary, redacted preview/short clue, recommended next action label;
     - next-step cards/actions mapping to existing flows: safety router, response center, evidence kit, official help directory, report prep/new analysis as appropriate;
     - privacy notes;
     - safe family briefing share summary.
   - Do not include raw `ScamAnalysis.input` (not available in incident logs anyway). Treat `Incident.preview` as already privacy-redacted but still avoid putting per-incident preview clues into the share summary by default.
   - Normalize bad input safely:
     - negative scores clamp to 0 in display metrics;
     - empty risk/signal/preview strings produce calm fallback labels;
     - duplicate or unsorted input should still output sorted newest-first timeline.
   - The builder should not mutate the store.

2. **Visible UI integration**
   - Add Home CTA, e.g. “사건 타임라인 요약”.
   - Add a button from the existing “사건 기록” screen to the new briefing screen.
   - Add a screen that displays:
     - summary metrics;
     - top signals;
     - recommended next steps;
     - timeline cards using redacted previews only;
     - privacy notes.
   - Add buttons to existing flows:
     - Safety Flow Router if available;
     - Response Center;
     - Evidence Preservation Kit;
     - Official Help Directory;
     - Report Prep if a current analysis exists, otherwise “새 검사”.
   - Add “가족에게 사건 요약 공유” using text-only `ACTION_SEND`; use the briefing builder’s safe share summary only.
   - Do not store anything when the user opens the briefing.
   - Do not clear or mutate incident logs from the briefing screen.
   - Add Home/back CTA.

3. **Interoperability / privacy safety**
   - Reuse existing `IncidentStore.load()` and `IncidentStore.displayTime()`; do not change the stored format unless absolutely necessary.
   - Reuse existing navigation helpers where possible.
   - Do not introduce new permissions.
   - Do not add network calls.
   - Do not imply the app continuously monitors messages/calls or automatically collects evidence.
   - If the share summary mentions official help, keep it as route guidance only, not legal/financial outcome guarantees.

4. **Tests**
   - Unit tests covering:
     - empty incident list shows empty state, zero counts, no timeline items, safe next-step guidance;
     - mixed incidents are sorted newest-first and risk counts/highest risk/latest incident are deterministic;
     - top signal labels aggregate from comma-separated `Incident.signals` without leaking preview text;
     - timeline item display uses only the stored redacted preview/clue and handles blank values;
     - high-risk money/credential/remote-like signals recommend response center, evidence kit, official help, and report prep/new analysis;
     - family/deepfake/number-change-like signals recommend safety router/family verification path;
     - safe share summary includes counts, highest risk, top signals, and action labels, but excludes raw-looking sensitive patterns: full phone numbers, 6+ digit account/OTP/PIN-like numbers, URLs/domains, resident-registration-like patterns, safe-word/contact-like labels, and per-incident preview clues;
     - output is deterministic for the same inputs;
     - copy does not imply automatic monitoring, contact reading, network upload, legal guarantee, guaranteed recovery, or automatic deletion.
   - Add/adjust regression tests only if needed.

5. **Docs**
   - Update `README.md` to document the Incident Timeline Briefing as a local-only summary of saved privacy-redacted incident logs that stores no new data, shares only aggregate-safe text, and adds no permissions.

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
feat: add incident timeline briefing
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the Incident Timeline Briefing slice, or a truthful blocker handoff exists with exact evidence.
