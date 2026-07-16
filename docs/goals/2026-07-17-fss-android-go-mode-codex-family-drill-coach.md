# Codex Goal — `/go` continuation: Family Safety Drill Coach

Date: 2026-07-17 00:27 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: continue `/go` while Claude Sonnet is still session-limited until 04:10 Asia/Seoul. The previous Codex slice was independently verified by Hermes.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Risk Signal Explainer Cards slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
# => BUILD SUCCESSFUL in 16s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 1.1M, 1101002 bytes
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
RiskSignalExplainerBuilderTest: debug/release tests=12 failures=0 errors=0
PrivacyPostureRegressionTest: debug/release tests=11 failures=0 errors=0
SafeVerificationCallScriptsBuilderTest: debug/release tests=11 failures=0 errors=0
TrustedHelperHandoffCardBuilderTest: debug/release tests=11 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 20` of `origin/main`
- latest local commits:
  - `419bb4f feat: add risk signal explainers`
  - `06a1bef feat: add safe verification call scripts`
  - `ff22a67 feat: add trusted helper handoff card`
  - `bf22afe feat: add 24-hour recovery plan`
  - `4ddf0b6 feat: add quick stop mode`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app already has a small `SafetyDrill.kt` with four fictional practice scenarios. The next cohesive slice is **Family Safety Drill Coach**: turn drills into a richer stateless family rehearsal coach that recommends fictional practice scenarios from the current analysis/readiness and routes to existing safety flows. This should strengthen the “family guardian” identity without storing drill progress.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -12`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyDrill.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
- `app/src/main/java/com/mongmong/familyshield/RiskSignalExplainer.kt`
- `app/src/main/java/com/mongmong/familyshield/SafeVerificationCallScripts.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyCallbackCard.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyReadiness.kt`
- `app/src/main/java/com/mongmong/familyshield/QuickStopMode.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyFlowRouter.kt`
- `app/src/main/java/com/mongmong/familyshield/AccountLockdownChecklist.kt`
- `app/src/main/java/com/mongmong/familyshield/EvidencePreservationKit.kt`
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

## `/go` task: Family Safety Drill Coach slice

Implement a focused local-only drill coach that helps families rehearse safe responses before or after a suspicious contact. It should remain fictional, stateless, and privacy-safe.

1. **New deterministic drill-coach model**
   - Add or evolve a pure Kotlin builder/model, e.g. `FamilySafetyDrillCoachBuilder`.
   - It must be unit-testable and not depend on Android UI classes.
   - It may reuse/extend existing `SafetyDrill`/`DrillScenario`, but avoid breaking current tests.
   - Suggested inputs:
     - current `ScamAnalysis?`;
     - value-less `FamilyReadiness`.
   - Output should include:
     - Korean title and intro;
     - stable drill cards in order;
     - each card has id/scenario, title, fictional setup, role-play lines, expected stop phrase, response steps, pass checklist, family reflection prompt, primary/secondary actions routed to existing app flows, and difficulty/urgency;
     - recommended drill ids/actions based on current analysis signals and readiness;
     - privacy/safety notes;
     - safe family share summary.
   - Drill cards should cover at least:
     - family new-number money request;
     - prosecutor/police safe-account pressure;
     - remote-control app/screen-share pressure;
     - parcel/invitation smishing link;
     - OTP/password/account-verification request;
     - gift card/gifticon/crypto transfer pressure;
     - overseas-card/payment-cancel bait;
     - weekly prevention check-in.
   - Recommendation logic should be deterministic:
     - no analysis/no signals: recommend family new-number, OTP/password, weekly check-in + checker/safety plan;
     - family/deepfake/phone-number-change: recommend family new-number drill first + family callback/family alert/safe-call;
     - money/safe-account/loan/gift/crypto/payment signals: recommend safe-account or payment/gift drills + quick stop/safe-call/account lockdown/official help;
     - app-install/remote/screen-share: recommend remote-control drill + account lockdown/evidence/helper;
     - link/smishing: recommend smishing drill + evidence/family alert/risk signal explainer;
     - low readiness: include weekly check-in and safety plan action;
     - high risk: include Quick Stop and 24h Recovery actions.
   - Safe family share summary must include only fictional scenario titles, generic stop phrases, pass checklist labels, and existing action labels. It must exclude raw pasted scam text, redacted clues, safety word/callback/contact values, full phone numbers, 6+ digit account/OTP/PIN-like numbers, URLs/domains, resident-registration-like patterns.

2. **Visible UI integration**
   - Enhance the existing drill screen (`Screen.DRILL`) or add a new Home CTA such as “가족 안전 연습 코치”. Prefer reusing the existing drill entry point if it keeps navigation simple.
   - Add current-result CTA if appropriate, e.g. “이 신호로 가족 연습하기”.
   - Screen should display:
     - title/intro;
     - recommended drill cards first;
     - all drill cards or categorized sections;
     - fictional notice;
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
     - Family Alert Message Pack;
     - Trusted Helper Handoff Card;
     - Safe Verification Call Scripts;
     - Recovery Plan 24h.
   - Add “가족에게 연습 과제 공유” using text-only `ACTION_SEND`; pass only the builder’s safe family share summary.
   - Do not store selected drill, pass/fail state, checklist state, score, read progress, share history, or practice history.
   - Do not mutate incident logs automatically.
   - Do not auto-dial, auto-send, read contacts, scrape messages, or request permissions.
   - Add Home/back CTA.

3. **Interoperability / safety**
   - Keep all drill scripts clearly fictional; no real links, real phone numbers, real bank/account numbers, or real personal details.
   - Reuse existing navigation helpers and flows; do not duplicate full official-help/account-lockdown/evidence content.
   - Do not introduce new Android permissions.
   - Do not add network calls.
   - Use calm Korean copy and avoid shame/blame.
   - Do not claim the drills guarantee safety, prevent all scams, train an AI model, monitor family, block calls/messages, remove malware, freeze accounts/cards, recover money, or provide legal advice.

4. **Tests**
   - Unit tests covering:
     - no-analysis/default drill order and conservative recommended drills/actions;
     - every drill card has fictional setup, role-play lines, stop phrase, response steps, pass checklist, reflection prompt, primary action, stable id/order;
     - no drill contains real URLs/domains, full phone numbers, 6+ digit account/OTP/PIN-like numbers, or resident-number-like patterns;
     - family/deepfake/number-change recommends family drill and callback/family alert/safe-call actions;
     - money/safe-account/loan/gift/crypto/payment recommends financial drills and quick stop/safe-call/account/help actions;
     - remote-control/app/screen-share recommends remote drill and account/evidence/helper actions without malware-removal guarantee;
     - link/smishing recommends smishing drill and evidence/family alert/risk-explainer actions without auto-scan claims;
     - low readiness recommends weekly check-in/safety plan; ready readiness copy differs calmly;
     - high risk includes Quick Stop and Recovery Plan actions;
     - share summary includes scenario/action/checklist labels but excludes raw text, redacted clues, safety plan values, phone/account/OTP/PIN/URL/domain/resident-number patterns;
     - output deterministic for same inputs;
     - copy states fictional/manual/local/no storage/no contact reading/no auto-send/no monitoring/no network;
     - copy does not imply training, automatic monitoring, conclusive prevention, call/SMS blocking, malware removal, account/card freeze, money recovery, legal guarantee, automatic deletion, or access to secrets.
   - Add/adjust privacy regression tests if needed to ensure the drill screen is stateless and share helper sends only builder summary.

5. **Docs**
   - Update `README.md` to document Family Safety Drill Coach as local-only, fictional, stateless family rehearsal guidance; it adds no permissions, reads no contacts/messages, auto-sends nothing, stores no drill selection/pass-fail/practice history, and mutates no incidents.

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
feat: add family safety drill coach
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the Family Safety Drill Coach slice, or a truthful blocker handoff exists with exact evidence.
