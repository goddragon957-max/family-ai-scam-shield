# Codex Goal — `/go` continuation: Incident Log Privacy Hardening

Date: 2026-07-16 18:57 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: Claude Fable/Sonnet/Opus/Haiku are all session-limited until 23:10 Asia/Seoul, so Hermes is continuing `/go` with the verified Codex fallback.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Evidence Preservation Kit slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
# => BUILD SUCCESSFUL in 1s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 892K
git diff --check
# => clean
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 6` of `origin/main`
- latest local commits:
  - `2759086 feat: add evidence preservation kit`
  - `1181b99 feat: add scam sample library`
  - `a386649 feat: add family readiness scorecard`
  - `f53dd6d feat: add family safety drill`
  - `7413c93 feat: add incident response center`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The current app is privacy-first, but `IncidentStore.add()` still stores an 80-character preview of the user-pasted suspicious text. The next obvious MVP slice is to harden the local incident log so it preserves useful context without storing raw sensitive text.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -9`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentStore.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
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

## `/go` task: Incident Log Privacy Hardening slice

Implement a focused slice that makes the incident log safer by storing and displaying a redacted local preview instead of raw pasted text.

1. **New deterministic redaction model**
   - Add a small Kotlin redactor/builder for incident-log previews.
   - Input: raw pasted suspicious text and/or `ScamAnalysis`.
   - Output: a short Korean-safe preview string that:
     - keeps enough context for the user to recognize the incident class;
     - masks phone numbers, account-like long digit runs, OTP/authentication codes, URLs/domains, and obvious ID/passport/resident-registration number patterns;
     - replaces line breaks with spaces;
     - trims to a deterministic length;
     - never returns a blank preview when the input contains text.
   - Suggested replacement labels: `[전화번호 숨김]`, `[긴 숫자 숨김]`, `[인증번호 숨김]`, `[링크 숨김]`, `[신분정보 숨김]`.
   - Keep deterministic and unit-testable.

2. **IncidentStore integration**
   - Change `IncidentStore.add()` so newly saved incidents store the redacted preview only.
   - Do not store the full pasted original text anywhere.
   - For previously saved old previews, sanitize again on `load()` or before display so old local data is also safer after app update.
   - Keep the 30-item cap and existing encoding format compatible if practical.

3. **UI copy**
   - Update the incident log screen copy so users understand:
     - records are local only;
     - only a privacy-masked preview is shown;
     - full pasted text is not kept in the app log.
   - Rename the displayed field from `내용:` to something like `가림 처리된 단서:`.

4. **Tests**
   - Unit tests for the redactor covering:
     - phone-number masking;
     - account/long digit masking;
     - OTP/authentication code masking;
     - URL/domain masking;
     - ID/resident-number-like masking;
     - deterministic trimming and newline normalization;
     - nonblank fallback.
   - Unit/integration test for `IncidentStore.add()` if feasible with existing Android test setup; if direct Context tests are awkward, test the pure preview function used by `IncidentStore` and update existing tests accordingly.
   - Ensure no test expects raw sensitive fragments to remain visible.

5. **Docs**
   - Update `README.md` Incident Log / Privacy section to state that saved incidents keep only risk level, signals, score, timestamp, and a redacted preview — not full pasted text.

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
feat: harden incident log privacy
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the incident-log privacy slice, or a truthful blocker handoff exists with exact evidence.
