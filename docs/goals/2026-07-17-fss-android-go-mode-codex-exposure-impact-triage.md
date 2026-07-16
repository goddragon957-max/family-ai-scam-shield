# Codex Goal — `/go` continuation: Exposure Impact Triage

Date: 2026-07-17 01:49 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: continue `/go` while Claude Sonnet is still session-limited until 04:10 Asia/Seoul. The previous Codex slice was independently verified by Hermes.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Family Readiness Roadmap slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
# => BUILD SUCCESSFUL in 1s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 1.1M, 1141092 bytes
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
FamilyReadinessRoadmapBuilderTest: debug/release tests=14 failures=0 errors=0
PrivacyPostureRegressionTest: debug/release tests=13 failures=0 errors=0
PrivacyCenterBuilderTest: debug/release tests=9 failures=0 errors=0
FamilySafetyDrillCoachBuilderTest: debug/release tests=14 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 22` of `origin/main`
- latest local commits:
  - `19d9a9b feat: add family readiness roadmap`
  - `f4297de feat: add family safety drill coach`
  - `419bb4f feat: add risk signal explainers`
  - `06a1bef feat: add safe verification call scripts`
  - `ff22a67 feat: add trusted helper handoff card`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now has many prevention, education, and recovery flows. The next cohesive slice is **Exposure Impact Triage**: a stateless screen that helps a user map “what might have been exposed?” to the right existing response flow without assuming the exposure actually happened and without storing the selection.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -12`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentResponse.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentResponseReportBuilder.kt`
- `app/src/main/java/com/mongmong/familyshield/RecoveryPlan24h.kt`
- `app/src/main/java/com/mongmong/familyshield/AccountLockdownChecklist.kt`
- `app/src/main/java/com/mongmong/familyshield/EvidencePreservationKit.kt`
- `app/src/main/java/com/mongmong/familyshield/OfficialReportPrep.kt`
- `app/src/main/java/com/mongmong/familyshield/OfficialHelpDirectory.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyReadinessRoadmap.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyFlowRouter.kt`
- `app/src/main/java/com/mongmong/familyshield/RiskSignalExplainer.kt`
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

## `/go` task: Exposure Impact Triage slice

Implement a focused local-only triage screen that helps users choose the right response when they are unsure what exposure happened. This is not an evidence recorder and not a questionnaire that stores answers.

1. **New deterministic exposure-triage model**
   - Add a pure Kotlin builder/model, e.g. `ExposureImpactTriageBuilder`.
   - It must be unit-testable and not depend on Android UI classes.
   - Suggested inputs:
     - current `ScamAnalysis?`;
     - value-less `FamilyReadiness` if useful.
   - Output should include:
     - Korean title and intro;
     - stable exposure cards in order;
     - each card has id, label, category, “when this might apply”, first safe move, what to avoid, what to prepare for official help, primary/secondary actions routed to existing app flows, and priority;
     - recommended exposure ids/actions based on current analysis signals/readiness;
     - privacy/safety notes;
     - safe family share summary.
   - Exposure cards should cover at least:
     - **아직 아무 행동도 안 함 / 불안함만 있음**: stop, analyze, callback, roadmap;
     - **돈·상품권·기프티콘·가상자산을 보냄**: recovery/official help/response center/evidence;
     - **계좌·카드·비밀번호·OTP·신분정보를 말함/입력함**: account lockdown/official help/recovery;
     - **앱 설치·화면공유·원격제어 허용**: account lockdown/evidence/helper/response center;
     - **링크 클릭만 함**: evidence/risk explainer/account check/family alert;
     - **링크에서 개인정보/결제정보 입력**: account lockdown/official help/recovery/evidence;
     - **가족 새 번호/딥페이크/가족 사칭에 답함**: family callback/family alert/safe-call/helper;
     - **확인 불가/복합 상황**: safety flow router/recovery/official help/trusted helper.
   - Recommendation logic should be deterministic:
     - no analysis/no signals: recommend no-action/analyze/callback and unclear-complex cards; avoid saying safe;
     - high risk or financial/credential/safe-account/gift/crypto/payment signals: recommend money/value exposure + credential/payment exposure + quick stop/recovery/official help/account/evidence;
     - app-install/remote/screen-share: recommend app/remote exposure + account/evidence/helper/response center;
     - link/smishing: recommend link-click-only and link-info-entered cards + evidence/risk explainer/family alert;
     - family/deepfake/phone-number-change: recommend family impersonation exposure + family callback/family alert/safe-call/helper;
     - secrecy/impersonation/unclear pressure: include unclear-complex + safety flow/router/official help;
     - low readiness: include callback/roadmap actions.
   - Safe family share summary must include only exposure labels/categories/action labels and generic safe moves. It must exclude raw pasted scam text, redacted clues, safety word/callback/contact values, full phone numbers, 6+ digit account/OTP/PIN-like numbers, URLs/domains, resident-registration-like patterns.

2. **Visible UI integration**
   - Add Home CTA near recovery/response items, e.g. “무엇이 노출됐는지 고르기”.
   - Add current-result CTA if appropriate, e.g. “이 신호로 노출 가능성 확인”.
   - Screen should display:
     - title/intro;
     - recommended exposure cards first;
     - all remaining cards;
     - privacy/safety notes.
   - Add buttons to existing flows where appropriate:
     - Quick Stop Mode;
     - Checker / current Result;
     - Risk Signal Explainer Cards;
     - Family Callback Card;
     - Family Readiness Roadmap;
     - Safety Flow Router;
     - Account Lockdown Checklist;
     - Evidence Preservation Kit;
     - Official Help Directory;
     - Official Report Prep Card;
     - Response Center / IncidentResponse situations;
     - Family Alert Message Pack;
     - Trusted Helper Handoff Card;
     - Safe Verification Call Scripts;
     - Recovery Plan 24h;
     - Privacy Center.
   - Add “가족에게 노출 분류 공유” using text-only `ACTION_SEND`; pass only the builder’s safe family share summary.
   - Do not store selected exposure, triage answer, completion state, checklist state, score, read progress, share history, or exposure history.
   - Do not mutate incident logs automatically.
   - Do not auto-dial, auto-send, read contacts, scrape messages, or request permissions.
   - Add Home/back CTA.

3. **Interoperability / safety**
   - Reuse existing navigation helpers and flows; do not duplicate full official-help/account-lockdown/evidence/recovery content.
   - The triage must never assert that a user definitely sent money/shared OTP/installed an app just because an analysis signal matched. Use “해당될 수 있음 / 직접 확인” wording.
   - Do not introduce new Android permissions.
   - Do not add network calls.
   - Use calm Korean copy and avoid shame/blame.
   - Do not claim the triage guarantees safety, proves whether exposure happened, monitors family, blocks calls/messages, removes malware, freezes accounts/cards, recovers money, or provides legal advice.

4. **Tests**
   - Unit tests covering:
     - no-analysis/default recommendation avoids saying safe and recommends no-action/callback/analyze/unclear actions;
     - high-risk/financial/credential/safe-account/gift/crypto/payment signals recommend money/credential exposures and quick stop/recovery/official help/account/evidence actions;
     - remote-control/app/screen-share recommends app/remote exposure and account/evidence/helper/response-center without malware-removal guarantee;
     - link/smishing recommends link-click-only + link-info-entered and evidence/risk/family-alert actions without auto-scan claims;
     - family/deepfake/number-change recommends family impersonation exposure and callback/alert/safe-call/helper actions;
     - secrecy/unclear pressure includes unclear-complex card and safety-flow/official-help actions;
     - low readiness includes callback/roadmap actions; ready copy differs calmly;
     - every exposure card has category, when-applies, first safe move, avoid notes, prep notes, primary action, stable id/order;
     - share summary includes exposure/action labels and generic safe moves but excludes raw text, redacted clues, safety plan values, phone/account/OTP/PIN/URL/domain/resident-number patterns;
     - output deterministic for same inputs;
     - copy states manual/local/no storage/no contact reading/no auto-send/no monitoring/no network/no incident mutation;
     - copy does not imply guaranteed safety/proof, automatic monitoring, call/SMS blocking, malware removal, account/card freeze, money recovery, legal guarantee, automatic deletion, or access to secrets.
   - Add/adjust privacy regression tests to ensure the triage screen is stateless and share helper sends only builder summary.

5. **Docs**
   - Update `README.md` to document Exposure Impact Triage as local-only, stateless, non-diagnostic exposure mapping guidance; it adds no permissions, reads no contacts/messages, auto-sends nothing, stores no exposure selections/history, and mutates no incidents.

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
feat: add exposure impact triage
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the Exposure Impact Triage slice, or a truthful blocker handoff exists with exact evidence.
