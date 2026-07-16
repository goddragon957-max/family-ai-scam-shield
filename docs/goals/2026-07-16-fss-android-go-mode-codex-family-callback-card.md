# Codex Goal — `/go` continuation: Family Callback Verification Card

Date: 2026-07-16 20:09 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: Claude Fable/Sonnet/Opus/Haiku are session-limited until 23:10 Asia/Seoul, so Hermes is continuing `/go` with the verified Codex fallback.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Official Report Prep Card slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
# => BUILD SUCCESSFUL in 1s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 913K
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
OfficialReportPrepBuilderTest: debug/release tests=9 failures=0 errors=0
OfficialHelpDirectoryBuilderTest: debug/release tests=6 failures=0 errors=0
IncidentPreviewRedactorTest: debug/release tests=12 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 9` of `origin/main`
- latest local commits:
  - `8a23aa3 feat: add official report prep card`
  - `9743ac4 feat: add official help directory`
  - `b557ad9 feat: harden incident log privacy`
  - `2759086 feat: add evidence preservation kit`
  - `1181b99 feat: add scam sample library`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now helps users analyze risk, prepare official reports, preserve evidence, and find official contacts. The next obvious MVP slice is the core “family-first” action: a privacy-safe callback verification card that turns a risk result plus the saved family safety plan into exact steps for calling a trusted family number before money/apps/OTP are shared.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -10`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentPreviewRedactor.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyReadiness.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyDrill.kt`
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

## `/go` task: Family Callback Verification Card slice

Implement a focused local-only callback verification card that helps the user safely call a trusted family contact after an analysis result.

1. **New deterministic callback-card model**
   - Add a small Kotlin builder/model, e.g. `FamilyCallbackCardBuilder`.
   - Input: `ScamAnalysis?` and `SafetyPlan`.
   - Output should include:
     - Korean title;
     - risk summary when analysis exists, or safe empty-state text when analysis is null;
     - privacy-safe clue using `IncidentPreviewRedactor` when analysis exists — never raw full pasted text;
     - readiness status derived from `FamilyReadinessBuilder`;
     - safe-word prompt text if a safe word/question exists, and fallback guidance if not;
     - ordered callback steps;
     - trusted contacts from the saved plan, with labels and dial numbers for screen use;
     - “do not do until verified” guardrails;
     - privacy/safety notes;
     - share summary string.
   - Deterministic behavior:
     - high-risk or money/app/OTP/remote-control/family-emergency/deepfake/number-change signals should tell the user to stop the current conversation first and call a saved trusted number;
     - if no trusted contact is saved, the card must prominently guide the user to create a safety plan before relying on a new number from the suspicious message;
     - lower-risk analysis still recommends callback through a saved number before money/apps/OTP.
   - The share summary must **not** include the raw pasted scam text, the redacted clue, the safe word/question itself, full trusted contact phone numbers, OTP/PIN, account numbers, or links. It may say “use the saved family safe question/contact in the app.”

2. **Visible UI integration**
   - Add a screen reachable from the Result screen, e.g. “가족 콜백 확인 카드”.
   - Add a Home CTA too, e.g. “가족 콜백 루틴 확인”, that opens the card with no current analysis and explains how to set up/run an analysis.
   - If opened without a current analysis, show the saved plan/readiness and CTA to run analysis.
   - If a safety plan is missing/incomplete, show CTA to `가족 안전 계획 설정`.
   - Display:
     - risk summary / empty state;
     - redacted clue only when analysis exists;
     - readiness status;
     - safe-word prompt or missing prompt guidance;
     - callback steps;
     - trusted contact dial buttons only for contacts that have a nonblank number;
     - guardrails and privacy notes.
   - Dial buttons must use existing `dial()` (`ACTION_DIAL`) and request no permission.
   - Add share button using text-only `ACTION_SEND`.
   - Add back buttons to Result (when analysis exists) and Home.

3. **Interoperability**
   - Reuse `SafetyPlanStore` / `SafetyPlan` / `TrustedContact`.
   - Reuse `FamilyReadinessBuilder` for readiness status rather than duplicating logic.
   - Reuse `IncidentPreviewRedactor` for all analysis clues.
   - Do not store the callback card in `SharedPreferences`.
   - Do not mutate incident logs automatically.
   - Do not read contacts from the device; only use user-entered plan contacts.

4. **Tests**
   - Unit tests covering:
     - high-risk family/number-change/voice/deepfake/money/OTP analysis produces stop-current-conversation + saved-number callback steps;
     - low-risk/no-analysis card still prioritizes saved-number callback before money/apps/OTP;
     - missing safety plan shows setup guidance and no dialable contacts;
     - saved safety plan exposes dialable screen contacts but share summary excludes full phone numbers and safe word text;
     - redacted clue masks phone/account/OTP/link/identity patterns and raw text is not included;
     - output is deterministic for the same analysis/plan;
     - copy does not imply automatic monitoring, contacts permission, network upload, legal guarantee, or guaranteed recovery.

5. **Docs**
   - Update `README.md` to document the Family Callback Verification Card as local-only, analysis/plan-derived, privacy-redacted, not stored, using `ACTION_DIAL`/`ACTION_SEND` only, and using only manually saved trusted contacts.

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
feat: add family callback verification card
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the family callback verification card slice, or a truthful blocker handoff exists with exact evidence.
