# Codex Goal — `/go` continuation: Family Readiness Roadmap

Date: 2026-07-17 01:03 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: continue `/go` while Claude Sonnet is still session-limited until 04:10 Asia/Seoul. The previous Codex slice was independently verified by Hermes.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Family Safety Drill Coach slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
# => BUILD SUCCESSFUL in 19s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 1.1M, 1120022 bytes
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
FamilySafetyDrillCoachBuilderTest: debug/release tests=14 failures=0 errors=0
PrivacyPostureRegressionTest: debug/release tests=12 failures=0 errors=0
PrivacyCenterBuilderTest: debug/release tests=9 failures=0 errors=0
RiskSignalExplainerBuilderTest: debug/release tests=12 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 21` of `origin/main`
- latest local commits:
  - `f4297de feat: add family safety drill coach`
  - `419bb4f feat: add risk signal explainers`
  - `06a1bef feat: add safe verification call scripts`
  - `ff22a67 feat: add trusted helper handoff card`
  - `bf22afe feat: add 24-hour recovery plan`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now has many response and education screens. The next cohesive slice is **Family Readiness Roadmap**: a stateless roadmap that turns the current family readiness state into a short “what to prepare next” path without exposing the actual safe word, callback script, or contact values.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -12`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyReadiness.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilySafetyDrillCoach.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyCallbackCard.kt`
- `app/src/main/java/com/mongmong/familyshield/QuickStopMode.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyFlowRouter.kt`
- `app/src/main/java/com/mongmong/familyshield/RiskSignalExplainer.kt`
- `app/src/main/java/com/mongmong/familyshield/SafeVerificationCallScripts.kt`
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

## `/go` task: Family Readiness Roadmap slice

Implement a focused local-only roadmap screen that answers “우리 가족은 다음에 무엇을 준비하면 되나?” It should use readiness status and current analysis signals only as a value-less guide, not as a storage/progress tracker.

1. **New deterministic readiness-roadmap model**
   - Add a pure Kotlin builder/model, e.g. `FamilyReadinessRoadmapBuilder`.
   - It must be unit-testable and not depend on Android UI classes.
   - Suggested inputs:
     - current `ScamAnalysis?`;
     - current `FamilyReadiness`;
     - optionally a boolean/count for whether local incident history exists, but do not read or expose incident contents.
   - Output should include:
     - Korean title and intro;
     - stable roadmap cards in order;
     - each card has id, stage, title, readiness status, why it matters, next small steps, “avoid” notes, primary/secondary actions routed to existing app flows, and priority;
     - recommended card ids/actions based on readiness and current analysis signals;
     - privacy/safety notes;
     - safe family share summary.
   - Roadmap cards should cover at least:
     - **가족 안전 계획 시작**: safe word/callback contact/script setup through existing Safety Plan;
     - **콜백 루틴 연습**: use Family Callback Card and Family Safety Drill Coach;
     - **위험 신호 이해**: Risk Signal Explainer + sample/checker;
     - **긴급 멈춤 준비**: Quick Stop + Safe Verification Call Scripts;
     - **피해 의심 시 대응 준비**: Recovery Plan + Official Help + Response Center;
     - **증거와 계정 보호 준비**: Evidence Kit + Account Lockdown;
     - **가족 경고/도우미 네트워크**: Family Alert + Trusted Helper Handoff;
     - **개인정보와 로컬 데이터 점검**: Privacy Center.
   - Recommendation logic should be deterministic:
     - low readiness: recommend safety-plan start, callback routine practice, quick stop;
     - in-progress readiness: recommend missing-readiness follow-up, drill coach, safe-call scripts, privacy center;
     - ready readiness: recommend drill coach, weekly maintenance/privacy review, risk signal learning;
     - high risk/current dangerous signals: include quick stop and recovery plan before prevention cards;
     - family/deepfake/phone-number-change: include callback routine + family alert/helper;
     - financial/credential/safe-account/gift/crypto/payment: include safe-call/account lockdown/official help/recovery;
     - remote/app/screen-share: include account/evidence/helper;
     - link/smishing: include risk explainer/evidence/family alert;
     - no analysis: include checker/sample/risk education but avoid saying “safe”.
   - Safe family share summary must include only roadmap titles/stages/action labels and generic next steps. It must exclude raw pasted scam text, redacted clues, safety word/callback/contact values, full phone numbers, 6+ digit account/OTP/PIN-like numbers, URLs/domains, resident-registration-like patterns.

2. **Visible UI integration**
   - Add Home/readiness CTA such as “가족 준비 로드맵”.
   - Add current-result CTA if appropriate, e.g. “이 상황 기준 준비 로드맵”.
   - Screen should display:
     - title/intro;
     - readiness summary using counts/levels only, not values;
     - recommended roadmap cards first;
     - all roadmap cards or remaining cards;
     - privacy/safety notes.
   - Add buttons to existing flows where appropriate:
     - Quick Stop Mode;
     - Checker / current Result;
     - Risk Signal Explainer Cards;
     - Family Callback Card;
     - Safety Plan;
     - Safety Flow Router;
     - Account Lockdown Checklist;
     - Evidence Preservation Kit;
     - Official Help Directory;
     - Response Center;
     - Family Alert Message Pack;
     - Trusted Helper Handoff Card;
     - Safe Verification Call Scripts;
     - Recovery Plan 24h;
     - Family Safety Drill Coach;
     - Privacy Center.
   - Add “가족에게 준비 로드맵 공유” using text-only `ACTION_SEND`; pass only the builder’s safe family share summary.
   - Do not store selected roadmap cards, completion state, checklist state, score, read progress, share history, or roadmap history.
   - Do not mutate incident logs automatically.
   - Do not auto-dial, auto-send, read contacts, scrape messages, or request permissions.
   - Add Home/back CTA.

3. **Interoperability / safety**
   - Reuse existing navigation helpers and flows; do not duplicate full official-help/account-lockdown/evidence/drill content.
   - The roadmap may mention readiness item labels/counts, but must not show stored safe word, callback script, contact names, or phone numbers.
   - Do not introduce new Android permissions.
   - Do not add network calls.
   - Use calm Korean copy and avoid shame/blame.
   - Do not claim the roadmap guarantees safety, prevents all scams, monitors family, blocks calls/messages, removes malware, freezes accounts/cards, recovers money, or provides legal advice.

4. **Tests**
   - Unit tests covering:
     - low/in-progress/ready readiness recommendation order and actions;
     - no-analysis/default roadmap avoids saying safe and recommends checker/risk education;
     - family/deepfake/number-change signals recommend callback/family alert/helper actions;
     - financial/credential/safe-account/gift/crypto/payment signals recommend safe-call/account/help/recovery actions;
     - remote-control/app/screen-share recommends account/evidence/helper without malware-removal guarantee;
     - link/smishing recommends risk explainer/evidence/family alert without auto-scan claims;
     - high-risk analysis includes Quick Stop and Recovery Plan first;
     - every roadmap card has stage, title, status, why, steps, avoid notes, primary action, stable id/order;
     - share summary includes roadmap/action labels but excludes raw text, redacted clues, safety plan values, phone/account/OTP/PIN/URL/domain/resident-number patterns;
     - output deterministic for same inputs;
     - copy states manual/local/no storage/no contact reading/no auto-send/no monitoring/no network/no incident mutation;
     - copy does not imply guaranteed safety/prevention, automatic monitoring, call/SMS blocking, malware removal, account/card freeze, money recovery, legal guarantee, automatic deletion, or access to secrets.
   - Add/adjust privacy regression tests to ensure the roadmap screen is stateless and share helper sends only builder summary.

5. **Docs**
   - Update `README.md` to document Family Readiness Roadmap as local-only, value-less, stateless preparation guidance; it adds no permissions, reads no contacts/messages, auto-sends nothing, stores no roadmap selection/completion/history, and mutates no incidents.

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
feat: add family readiness roadmap
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the Family Readiness Roadmap slice, or a truthful blocker handoff exists with exact evidence.
