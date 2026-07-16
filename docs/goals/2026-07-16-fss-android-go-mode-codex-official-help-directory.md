# Codex Goal — `/go` continuation: Official Help Directory

Date: 2026-07-16 19:26 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: Claude Fable/Sonnet/Opus/Haiku are session-limited until 23:10 Asia/Seoul, so Hermes is continuing `/go` with the verified Codex fallback.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Incident Log Privacy Hardening slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
# => BUILD SUCCESSFUL in 2s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 896K
git diff --check
# => clean
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 7` of `origin/main`
- latest local commits:
  - `b557ad9 feat: harden incident log privacy`
  - `2759086 feat: add evidence preservation kit`
  - `1181b99 feat: add scam sample library`
  - `a386649 feat: add family readiness scorecard`
  - `f53dd6d feat: add family safety drill`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now has many response paths mentioning 112, 1394, and 1332. The next obvious MVP slice is to centralize those official/help contacts and scripts so families can reach the right place without hunting through multiple screens.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -10`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentResponse.kt`
- `app/src/main/java/com/mongmong/familyshield/EvidencePreservationKit.kt`
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
- phone actions must use `ACTION_DIAL`, not `CALL_PHONE`
- no automatic surveillance; user-pasted/shared text only
- calm, practical Korean copy
- `android:allowBackup="false"`
- no build artifacts or local toolchain files in commits

## `/go` task: Official Help Directory slice

Implement a focused local-only help directory that centralizes official/emergency contacts already used in the app.

1. **New deterministic help-directory model**
   - Add a small Kotlin model/builder for official help entries.
   - Include at least:
     - `112` 경찰 긴급 신고 — when immediate danger or urgent fraud damage has happened;
     - `1394` 보이스피싱 통합신고/상담 — when voice phishing/smishing is suspected or a phishing number/site should be reported;
     - `1332` 금융감독원 — when financial damage, payment stop, dispute, or damage relief process guidance is needed;
     - A generic “은행/카드사 공식 대표번호” entry with no hardcoded phone number, telling users to use the number printed in the app/card/official website rather than a message link.
   - For each entry include:
     - Korean label;
     - optional dial number;
     - when-to-use guidance;
     - one or more “what to say” script lines;
     - privacy/safety notes (do not share OTP/PIN/raw text in family chat, etc.).
   - Keep deterministic and unit-testable.
   - Do not add unverified third-party numbers.

2. **Visible UI integration**
   - Add a screen reachable from Home, e.g. “신고·상담 연락처”.
   - Add CTAs from the Result screen, Response Center, and Evidence Preservation Kit to open the help directory.
   - Show each entry with when-to-use text, what-to-say script, safety note, and a dial button only when a dial number exists.
   - For the bank/card entry, show guidance only and no dial button.
   - Use `ACTION_DIAL` for dial buttons and request no permissions.

3. **Share support**
   - Add a safe share summary for the directory, e.g. “가족에게 신고 연락처 공유”.
   - It should include the official numbers and what-to-say checklist but not any raw pasted scam text.
   - The share action should use `ACTION_SEND` text only.

4. **Tests**
   - Unit tests covering:
     - directory contains the required entries and stable order;
     - only number-backed entries expose dial numbers;
     - every number-backed entry has numeric dial text and no non-dial entry has one;
     - every entry has what-to-say script lines;
     - share summary includes 112, 1394, 1332 and excludes raw scam placeholder fragments;
     - copy does not imply automatic monitoring/network upload/legal guarantee.

5. **Docs**
   - Update `README.md` to document the Official Help Directory as local-only guidance using `ACTION_DIAL`, no `CALL_PHONE` permission, and no network lookup.

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
feat: add official help directory
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the official help directory slice, or a truthful blocker handoff exists with exact evidence.
