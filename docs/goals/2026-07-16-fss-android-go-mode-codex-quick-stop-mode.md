# Codex Goal — `/go` continuation: Quick Stop Mode

Date: 2026-07-16 22:51 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: Claude Fable/Sonnet/Opus/Haiku are session-limited until 23:10 Asia/Seoul, so Hermes is continuing `/go` with the verified Codex fallback.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Family Alert Message Pack slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
# => BUILD SUCCESSFUL in 18s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 1002K, 1025299 bytes
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
FamilyAlertMessagePackBuilderTest: debug/release tests=10 failures=0 errors=0
PrivacyPostureRegressionTest: debug/release tests=6 failures=0 errors=0
AccountLockdownChecklistBuilderTest: debug/release tests=11 failures=0 errors=0
PrivacyCenterBuilderTest: debug/release tests=9 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 15` of `origin/main`
- latest local commits:
  - `07fd9cb feat: add family alert message pack`
  - `ab9ffd7 feat: add account lockdown checklist`
  - `6f576bb feat: add incident timeline briefing`
  - `2a6b396 feat: add safety flow router`
  - `b194ad2 feat: add privacy center`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now has many safety flows, but a scared user still needs one ultra-simple entry point: **stop, do not pay/share/install, call a trusted route, then choose the right flow**. The next cohesive slice is a stateless **Quick Stop Mode** screen with large, calm first-response guidance and direct navigation to existing app modules.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -10`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyReadiness.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyCallbackCard.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyFlowRouter.kt`
- `app/src/main/java/com/mongmong/familyshield/AccountLockdownChecklist.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyAlertMessagePack.kt`
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

## `/go` task: Quick Stop Mode slice

Implement a focused local-only emergency entry screen for overwhelmed users. It should be simple, stateless, and route to existing flows.

1. **New deterministic Quick Stop model**
   - Add a small pure Kotlin builder/model, e.g. `QuickStopModeBuilder`.
   - It must be unit-testable and not depend on Android UI classes.
   - Suggested inputs:
     - current `ScamAnalysis?`;
     - saved `SafetyPlan` or `FamilyReadiness`;
     - optional trusted contact count / has trusted contact info derived from `SafetyPlan`.
   - Output should include:
     - Korean title and intro;
     - a “3-step stop script” in stable order: stop conversation, do not pay/share/install, verify through existing trusted route;
     - a small set of large action cards in stable order;
     - each card has id, urgency, label, short explanation, primary action, optional secondary actions, and safety notes;
     - recommended action card ids based on analysis risk/signals and readiness;
     - privacy/safety notes;
     - safe family share summary.
   - Action cards should route to existing flows, not duplicate them:
     - “지금 대화 멈추기” / stop script — stay on screen but show script/no action mutation;
     - “가족 콜백 확인” — existing family callback card / safety plan;
     - “위험 분석하기” — checker or current result;
     - “이미 돈·정보·앱을 했어요” — response center + account lockdown;
     - “공식 도움·신고 번호” — official help directory;
     - “증거 보존” — evidence kit;
     - “가족에게 경고 공유” — family alert message pack.
   - Recommendation logic should be deterministic:
     - no analysis: recommend stop script, checker, family callback / safety plan;
     - high risk or urgent money/credential/remote/safe-account signals: recommend stop script, response/account lockdown, evidence, official help;
     - family/deepfake/phone-number-change signals: recommend family callback before other routes;
     - link/smishing signals: recommend checker/current result, evidence, family alert, official help;
     - if readiness is low, include safety plan/family callback as a recommended card.
   - Safe share summary must include only generic stop guidance and route labels. It must exclude raw pasted scam text, redacted clues, safety word/callback/contact values, full phone numbers, 6+ digit account/OTP/PIN-like numbers, URLs/domains, resident-registration-like patterns.

2. **Visible UI integration**
   - Add a prominent Home CTA near the top, e.g. “긴급 멈춤 모드”.
   - Add a screen that displays:
     - title/intro;
     - 3-step stop script;
     - recommended action cards;
     - all action cards;
     - privacy/safety notes.
   - Use simple large native buttons / section copy. Do not require fancy UI.
   - Add buttons to existing flows where appropriate:
     - Family Callback Card;
     - Safety Plan;
     - Checker / current Result;
     - Safety Flow Router;
     - Response Center;
     - Account Lockdown Checklist;
     - Evidence Preservation Kit;
     - Official Help Directory;
     - Family Alert Message Pack.
   - Add “가족에게 멈춤 안내 공유” using text-only `ACTION_SEND`; pass only the builder’s safe share summary.
   - Do not store button selections, share history, or checklist state.
   - Do not mutate incident logs automatically.
   - Do not auto-dial, auto-send, read contacts, or request permissions.
   - Add Home/back CTA.

3. **Interoperability / safety**
   - Reuse existing navigation helpers and flows; do not duplicate all official help/evidence/account/family-alert copy.
   - Do not introduce any new Android permissions.
   - Do not add network calls.
   - Do not claim the app blocks calls/messages, removes malware, freezes accounts, recovers money, or guarantees safety/legal outcomes.
   - If a phone action is added, it must use existing `ACTION_DIAL` patterns, not direct call. Prefer routing to existing official/help/callback screens rather than adding a new dial implementation.

4. **Tests**
   - Unit tests covering:
     - no-analysis/default script, card order, and conservative recommended cards;
     - high-risk money/credential/remote/safe-account analysis recommends stop + response/account/evidence/help;
     - family/deepfake/number-change analysis recommends family callback early;
     - link/smishing analysis recommends analysis/current result, evidence, family alert/help without implying automatic scanning;
     - low readiness recommends safety plan/family callback; ready readiness copy differs calmly;
     - every action card has label, explanation, primary action label, safety notes, and stable id/order;
     - share summary includes stop guidance and route labels/actions but excludes raw text, redacted clues, safety plan values, phone/account/OTP/PIN/URL/domain/resident-number patterns;
     - output deterministic for same inputs;
     - copy states manual action/no contact reading/no auto-send/no storage/no monitoring;
     - copy does not imply automatic monitoring, call/SMS blocking, contact reading, network upload, malware removal, account freeze, financial recovery, legal guarantee, or automatic deletion.
   - Add/adjust privacy regression tests if needed to ensure the screen is stateless and share helper sends only builder summary.

5. **Docs**
   - Update `README.md` to document Quick Stop Mode as local-only, stateless, manual first-response guidance that adds no permissions, reads no contacts, auto-sends nothing, and stores no selections.

## Verification gates

Run fresh gates before claiming completion:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
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
feat: add quick stop mode
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the Quick Stop Mode slice, or a truthful blocker handoff exists with exact evidence.
