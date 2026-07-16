# Codex Goal — `/go` continuation: Privacy Center & Local Data Control

Date: 2026-07-16 20:29 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: Claude Fable/Sonnet/Opus/Haiku are session-limited until 23:10 Asia/Seoul, so Hermes is continuing `/go` with the verified Codex fallback.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Family Callback Verification Card slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
# => BUILD SUCCESSFUL in 1s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 921K
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
FamilyCallbackCardBuilderTest: debug/release tests=7 failures=0 errors=0
OfficialReportPrepBuilderTest: debug/release tests=9 failures=0 errors=0
FamilyReadinessBuilderTest: debug/release tests=8 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 10` of `origin/main`
- latest local commits:
  - `bf55ce0 feat: add family callback verification card`
  - `8a23aa3 feat: add official report prep card`
  - `9743ac4 feat: add official help directory`
  - `b557ad9 feat: harden incident log privacy`
  - `2759086 feat: add evidence preservation kit`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app has grown into a serious privacy-first safety MVP. The next obvious slice is a trust-building Privacy Center where the user can see exactly what local data exists, what permissions are not used, and clear local safety-plan/incident data from one place.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -10`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentStore.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt`
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
- phone actions must use `ACTION_DIAL`, not `CALL_PHONE`
- no automatic surveillance; user-pasted/shared text only
- calm, practical Korean copy
- `android:allowBackup="false"`
- no build artifacts or local toolchain files in commits

## `/go` task: Privacy Center & Local Data Control slice

Implement a focused local-only Privacy Center that makes the MVP’s privacy posture visible and controllable.

1. **New deterministic privacy-center model**
   - Add a small Kotlin builder/model, e.g. `PrivacyCenterBuilder`.
   - Inputs should be plain data, not Android UI objects, so it is unit-testable. Suggested inputs:
     - saved `SafetyPlan`;
     - saved incident count;
     - consent/onboarding accepted boolean;
     - manifest/permission facts supplied as constants or model fields.
   - Output should include:
     - Korean title and intro;
     - local data inventory items;
     - permission status items;
     - what the app does not collect/do;
     - recommended cleanup actions;
     - privacy notes;
     - safe share summary string for family explanation.
   - Inventory should clearly distinguish:
     - consent flag stored locally;
     - family safety plan safe word/script/manually entered trusted contacts stored in `SharedPreferences`;
     - incident logs stored locally with only risk/signals/score/timestamp/redacted preview;
     - transient cards/checklists/drills/sample selections not stored;
     - no pasted full text retained in incident logs.
   - Permission status should state no SMS/call log/contact/microphone/internet permissions and calls use dialer intent only.
   - Keep deterministic and unit-testable.

2. **Store support**
   - Add minimal helper methods if needed:
     - `IncidentStore.count()` or equivalent;
     - safety-plan summary can use existing `SafetyPlanStore.load()` and `FamilyReadinessBuilder`.
   - Do not change encoding formats unless necessary.
   - Do not migrate or delete anything automatically.

3. **Visible UI integration**
   - Add a Home CTA, e.g. “개인정보 센터”.
   - Add a screen that displays:
     - local data inventory;
     - permission status;
     - no-collection statements;
     - cleanup actions.
   - Cleanup buttons should be explicit and scoped:
     - clear incident logs;
     - clear family safety plan;
     - optionally reset onboarding/consent only if the copy is clear that the onboarding will appear again.
   - Clearing actions should call existing stores (`incidents.clear()`, `safetyPlan.clear()`, prefs consent flag) and refresh the screen with a Toast. Do not silently clear all data on entry.
   - Add “가족에게 개인정보 요약 공유” using text-only `ACTION_SEND` and no raw user text.
   - Add Home/back CTA.

4. **Tests**
   - Unit tests covering:
     - empty/no-data state shows no incidents, no safety plan, consent missing/false;
     - populated state shows readiness/contact count and incident count without exposing safe word/script/contact phone values;
     - permission copy states no SMS/call/contact/microphone/internet permissions and dialer-only behavior;
     - transient cards/checklists/drills are described as not stored;
     - share summary includes privacy posture but excludes safe word, callback script, full contact numbers, pasted raw text, OTP/PIN/account/link patterns;
     - output deterministic for same input;
     - copy does not imply monitoring, contact reading, network upload, legal guarantee, guaranteed recovery, or automatic deletion.
   - If you add `IncidentStore.count()`, test the pure builder and any easy store helpers where feasible with current test style.

5. **Docs**
   - Update `README.md` to document the Privacy Center, local data inventory, scoped clearing actions, text-only sharing, and no new permissions.

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
feat: add privacy center
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the Privacy Center slice, or a truthful blocker handoff exists with exact evidence.
