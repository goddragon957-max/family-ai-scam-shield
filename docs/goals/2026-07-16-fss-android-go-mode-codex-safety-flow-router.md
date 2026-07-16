# Codex Goal — `/go` continuation: Safety Flow Router

Date: 2026-07-16 20:59 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: Claude Fable/Sonnet/Opus/Haiku are session-limited until 23:10 Asia/Seoul, so Hermes is continuing `/go` with the verified Codex fallback.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Privacy Center slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
# => BUILD SUCCESSFUL in 1s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 936K
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
PrivacyCenterBuilderTest: debug/release tests=9 failures=0 errors=0
PrivacyPostureRegressionTest: debug/release tests=3 failures=0 errors=0
FamilyCallbackCardBuilderTest: debug/release tests=7 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 11` of `origin/main`
- latest local commits:
  - `b194ad2 feat: add privacy center`
  - `bf55ce0 feat: add family callback verification card`
  - `8a23aa3 feat: add official report prep card`
  - `9743ac4 feat: add official help directory`
  - `b557ad9 feat: harden incident log privacy`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now has many strong safety modules. The next obvious MVP slice is a first-responder style **Safety Flow Router** so a stressed user can answer “지금 뭐부터 해야 하지?” and jump to the right existing flow without reading every Home button.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -10`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentResponse.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyCallbackCard.kt`
- `app/src/main/java/com/mongmong/familyshield/OfficialReportPrep.kt`
- `app/src/main/java/com/mongmong/familyshield/PrivacyCenter.kt`
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

## `/go` task: Safety Flow Router slice

Implement a focused local-only routing screen that helps users choose the right next action.

1. **New deterministic router model**
   - Add a small Kotlin model/builder, e.g. `SafetyFlowRouterBuilder`.
   - It should be pure/unit-testable and not depend on Android UI classes.
   - Suggested inputs:
     - current `ScamAnalysis?`;
     - current `SafetyPlan` or readiness;
     - incident count if useful.
   - Output should include:
     - Korean title and intro;
     - a small number of route cards in stable order;
     - each route card has an id, label, urgency level/order, explanation, primary action label, optional secondary action labels, safety notes;
     - optional recommended route id(s) based on analysis risk/signals/readiness;
     - safe share summary string for family explanation.
   - Route cards should map to existing flows, not duplicate their content:
     - “수상한 문자를 받았어요” → analysis checker;
     - “가족/번호 변경 요청 같아요” → family callback card and safety plan;
     - “이미 돈·상품권·코인/정보를 보냈어요” → response center + official report prep;
     - “앱 설치·화면공유·원격제어를 했어요” → response center/evidence kit;
     - “신고·상담 번호가 필요해요” → official help directory;
     - “증거를 지워도 될지 모르겠어요” → evidence kit;
     - “내 데이터/권한이 걱정돼요” → privacy center.
   - Recommendation logic should be deterministic and simple:
     - no analysis: prioritize checker + family safety plan/callback readiness;
     - high risk or urgent money/credentials/remote/safe-account signals: prioritize response center, report prep, evidence, official help;
     - family emergency/deepfake/phone-number-change signals: prioritize family callback card;
     - link/smishing signals: prioritize checker result actions, evidence kit, official help;
     - privacy concern route should always be available but not usually the top urgent recommendation.
   - Share summary must not include raw pasted scam text, redacted clue, safe word, contact names/numbers, OTP/PIN/account/link values.

2. **Visible UI integration**
   - Add a Home CTA near the top, e.g. “지금 뭐부터 할지 고르기”.
   - Add a screen that displays the router title/intro and route cards.
   - If current analysis exists, show “현재 분석 기준 추천” with recommended route labels; otherwise show “분석 전 기본 안내”.
   - Each route card should have a button that navigates to the existing screen:
     - checker → `Screen.CHECKER`
     - callback → existing `openFamilyCallbackCard(currentAnalysis)`
     - response center → `openResponseCenter()`
     - report prep → `Screen.REPORT_PREP`
     - official help → `openHelpDirectory(Screen.<router>)` or safe equivalent
     - evidence → `openEvidenceKit(...)` where sensible
     - privacy → `Screen.PRIVACY_CENTER`
   - Add text-only share button for the router summary.
   - Do not store the selected route.
   - Do not mutate incident logs automatically.
   - Add Home/back CTA.

3. **Interoperability / navigation safety**
   - Reuse existing screens and builders; do not duplicate their full copy.
   - Do not introduce new permissions.
   - Do not add network calls.
   - Keep button labels content-aware and short enough for native View buttons.
   - If adding a new `Screen` enum value, ensure all `when` branches are covered.

4. **Tests**
   - Unit tests covering:
     - default/no-analysis route order and available routes;
     - high-risk money/OTP/remote/safe-account analysis recommends response center/report prep/evidence/official help;
     - family emergency/deepfake/phone-number-change analysis recommends family callback first;
     - link/smishing analysis recommends evidence/help without implying automatic scanning;
     - all route cards have labels, explanations, primary action labels, and safety notes;
     - share summary includes route labels/actions but excludes raw text, redacted clue, safe word, contact names/numbers, OTP/PIN/account/link patterns;
     - deterministic output for same inputs;
     - copy does not imply automatic monitoring, contact reading, network upload, legal guarantee, guaranteed recovery, or automatic deletion.
   - Add/adjust regression tests only if needed.

5. **Docs**
   - Update `README.md` to document the Safety Flow Router as a local-only navigation aid that stores no selections, uses existing flows, and adds no permissions.

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
feat: add safety flow router
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the Safety Flow Router slice, or a truthful blocker handoff exists with exact evidence.
