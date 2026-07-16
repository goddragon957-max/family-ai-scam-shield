# Codex Goal — `/go` continuation: Trusted Helper Review Mode

Date: 2026-07-17 02:14 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: continue `/go` while Claude Sonnet is still session-limited until 04:10 Asia/Seoul. The previous Codex slice was independently verified by Hermes.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Exposure Impact Triage slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
# => BUILD SUCCESSFUL in 1s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 1.2M, 1159396 bytes
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
ExposureImpactTriageBuilderTest: debug/release tests=14 failures=0 errors=0
PrivacyPostureRegressionTest: debug/release tests=14 failures=0 errors=0
PrivacyCenterBuilderTest: debug/release tests=9 failures=0 errors=0
FamilyReadinessRoadmapBuilderTest: debug/release tests=14 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 23` of `origin/main`
- latest local commits:
  - `5860ce5 feat: add exposure impact triage`
  - `19d9a9b feat: add family readiness roadmap`
  - `f4297de feat: add family safety drill coach`
  - `419bb4f feat: add risk signal explainers`
  - `06a1bef feat: add safe verification call scripts`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now covers analysis, family callback, incident response, evidence, official help, quick stop, recovery, safety flow routing, drills, readiness, and exposure triage. The next cohesive slice is **Trusted Helper Review Mode**: a stateless guide for a trusted family/helper who is physically helping the user review what happened without asking for passwords, OTPs, safe words, contact values, or raw scam text.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -12`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/TrustedHelperHandoffCard.kt`
- `app/src/main/java/com/mongmong/familyshield/ExposureImpactTriage.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyCallbackCard.kt`
- `app/src/main/java/com/mongmong/familyshield/SafeVerificationCallScripts.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyReadinessRoadmap.kt`
- `app/src/main/java/com/mongmong/familyshield/QuickStopMode.kt`
- `app/src/main/java/com/mongmong/familyshield/RecoveryPlan24h.kt`
- `app/src/main/java/com/mongmong/familyshield/AccountLockdownChecklist.kt`
- `app/src/main/java/com/mongmong/familyshield/EvidencePreservationKit.kt`
- `app/src/main/java/com/mongmong/familyshield/OfficialHelpDirectory.kt`
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

## `/go` task: Trusted Helper Review Mode slice

Implement a focused local-only screen for a trusted helper. The screen should help a helper sit beside the user and review the situation safely, without collecting or storing helper notes.

1. **New deterministic trusted-helper-review model**
   - Add a pure Kotlin builder/model, e.g. `TrustedHelperReviewModeBuilder`.
   - It must be unit-testable and not depend on Android UI classes.
   - Suggested inputs:
     - current `ScamAnalysis?`;
     - value-less `FamilyReadiness`;
     - optionally whether there are saved incident records as a count only, if easy to pass without exposing records.
   - Output should include:
     - Korean title and intro;
     - stable helper review cards in order;
     - each card has id, stage/category, helper role, safe question to ask, what not to ask, first safe move, route actions, and priority;
     - recommended card ids/actions based on current analysis signals/readiness;
     - privacy/safety notes;
     - safe family share summary.
   - Cards should cover at least:
     - **먼저 멈추고 안심시키기**: pause pressure, no blame, no additional payment/info/app action;
     - **비밀값 묻지 않기**: do not ask for passwords, OTPs, card secrets, safe word answers, exact contact numbers, resident numbers;
     - **무엇을 했는지만 분류하기**: route to Exposure Impact Triage, not raw notes;
     - **가족 본인 확인 돕기**: route to Family Callback Card and Safe Call Scripts;
     - **이미 한 행동 보호하기**: route to Recovery Plan, Account Lockdown, Evidence Kit;
     - **공식 도움 준비하기**: route to Official Help Directory and Official Report Prep;
     - **가족에게 일반 경고 공유하기**: route to Family Alert Message Pack, without raw scam text;
     - **다음 가족 준비로 연결하기**: route to Family Readiness Roadmap and Drill Coach.
   - Recommendation logic should be deterministic:
     - no analysis/no signals: recommend pause, secret-boundary, classify exposure, callback;
     - high risk or money/credential/payment/safe-account/gift/crypto signals: recommend protect actions + official help + evidence;
     - app/remote/screen-share signals: recommend protect actions + helper handoff + evidence;
     - link/smishing signals: recommend classify exposure + evidence + family alert;
     - family/deepfake/number-change signals: recommend family verification + family alert + safe-call;
     - secrecy/impersonation pressure: recommend pause, secret-boundary, classify exposure, official help;
     - low readiness: recommend readiness roadmap and drill coach.
   - Safe family share summary must include only stage/category/action labels and generic helper-safe moves. It must exclude raw pasted scam text, redacted clues, safe word/callback/contact values, full phone numbers, 6+ digit account/OTP/PIN-like numbers, URLs/domains, resident-registration-like patterns.

2. **Visible UI integration**
   - Add Home CTA near helper/family/recovery items, e.g. “보호자와 함께 안전 검토”.
   - Add result CTA if appropriate, e.g. “이 신호로 보호자 검토 시작”.
   - Add a CTA from `TrustedHelperHandoffCard` or related helper screen if straightforward, but do not break existing behavior.
   - Screen should display:
     - title/intro;
     - recommended helper cards first;
     - all remaining helper cards;
     - privacy/safety notes.
   - Add buttons to existing flows where appropriate:
     - Quick Stop Mode;
     - Exposure Impact Triage;
     - Checker / current Result;
     - Risk Signal Explainers;
     - Family Callback Card;
     - Safe Verification Call Scripts;
     - Family Alert Message Pack;
     - Trusted Helper Handoff Card;
     - Family Readiness Roadmap;
     - Family Safety Drill Coach;
     - Recovery Plan 24h;
     - Account Lockdown Checklist;
     - Evidence Preservation Kit;
     - Official Help Directory;
     - Official Report Prep;
     - Safety Flow Router;
     - Privacy Center.
   - Add “가족에게 보호자 검토 요약 공유” using text-only `ACTION_SEND`; pass only the builder’s safe family share summary.
   - Do not store selected card, helper answer, helper notes, completion state, checklist state, score, read progress, share history, or helper-review history.
   - Do not mutate incident logs automatically.
   - Do not auto-dial, auto-send, read contacts, scrape messages, or request permissions.
   - Add Home/back CTA.

3. **Interoperability / safety**
   - Reuse existing navigation helpers and flows; do not duplicate full official-help/account-lockdown/evidence/recovery content.
   - The screen must never tell a helper to ask for, collect, or write down secrets. It should tell helpers to ask categories only: “어떤 종류였나요?” not “값이 뭐였나요?”
   - Never assert that the helper has verified truth or that exposure definitely happened based only on analysis signals.
   - Do not introduce new Android permissions.
   - Do not add network calls.
   - Use calm Korean copy and avoid shame/blame.
   - Do not claim the review guarantees safety, proves whether exposure happened, monitors family, blocks calls/messages, removes malware, freezes accounts/cards, recovers money, or provides legal advice.

4. **Tests**
   - Unit tests covering:
     - no-analysis/default recommendation includes pause, secret-boundary, exposure triage, callback, and avoids safe verdict;
     - high-risk/financial/credential/safe-account/gift/crypto/payment signals recommend protect/official/evidence actions;
     - remote-control/app/screen-share recommends protect/evidence/helper without malware-removal guarantee;
     - link/smishing recommends exposure triage/evidence/family alert without auto-scan claims;
     - family/deepfake/number-change recommends family verification/family alert/safe-call/helper;
     - secrecy/impersonation includes pause/secret-boundary/exposure triage/official help;
     - low readiness includes readiness roadmap and drill coach; ready copy differs calmly;
     - every helper card has stage/category, helper role, safe question, what-not-to-ask, first safe move, primary action, stable id/order;
     - safe questions and copy never ask for secret values (password, OTP value, PIN, card secret, safe-word answer, exact contact number, resident number, raw link);
     - share summary includes card/action labels and generic safe moves but excludes raw text, redacted clues, safety plan values, phone/account/OTP/PIN/URL/domain/resident-number patterns;
     - output deterministic for same inputs;
     - copy states manual/local/no storage/no contact reading/no auto-send/no monitoring/no network/no incident mutation;
     - copy does not imply guaranteed safety/proof, automatic monitoring, call/SMS blocking, malware removal, account/card freeze, money recovery, legal guarantee, automatic deletion, or access to secrets.
   - Add/adjust privacy regression tests to ensure the helper review screen is stateless and share helper sends only builder summary.

5. **Docs**
   - Update `README.md` to document Trusted Helper Review Mode as local-only, stateless, helper-safe guidance; it adds no permissions, reads no contacts/messages, auto-sends nothing, stores no helper notes/selections/history, and mutates no incidents.

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
feat: add trusted helper review mode
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the Trusted Helper Review Mode slice, or a truthful blocker handoff exists with exact evidence.
