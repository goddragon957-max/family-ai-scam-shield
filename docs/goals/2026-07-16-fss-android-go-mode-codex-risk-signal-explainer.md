# Codex Goal — `/go` continuation: Risk Signal Explainer Cards

Date: 2026-07-16 23:55 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: Claude Code hit session limit (`resets 4:10am Asia/Seoul`) on the previous run, although Hermes recovered and verified/committed Safe Verification Call Scripts. Continue `/go` with the verified Codex fallback.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified and committed the previous Safe Verification Call Scripts slice after Claude hit its session limit:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
# => BUILD SUCCESSFUL in 1s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 1.1M, 1079532 bytes
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
SafeVerificationCallScriptsBuilderTest: debug/release tests=11 failures=0 errors=0
PrivacyPostureRegressionTest: debug/release tests=10 failures=0 errors=0
TrustedHelperHandoffCardBuilderTest: debug/release tests=11 failures=0 errors=0
PrivacyCenterBuilderTest: debug/release tests=9 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 19` of `origin/main`
- latest local commits:
  - `06a1bef feat: add safe verification call scripts`
  - `ff22a67 feat: add trusted helper handoff card`
  - `bf22afe feat: add 24-hour recovery plan`
  - `4ddf0b6 feat: add quick stop mode`
  - `07fd9cb feat: add family alert message pack`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now has many action flows. The next cohesive slice is **Risk Signal Explainer Cards**: explain *why* a specific analysis signal is suspicious in calm Korean, what to do next, and which existing flow to open. This helps families understand matched `ScamSignal`s without exposing pasted raw text.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -12`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
- `app/src/main/java/com/mongmong/familyshield/QuickStopMode.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyFlowRouter.kt`
- `app/src/main/java/com/mongmong/familyshield/AccountLockdownChecklist.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyAlertMessagePack.kt`
- `app/src/main/java/com/mongmong/familyshield/TrustedHelperHandoffCard.kt`
- `app/src/main/java/com/mongmong/familyshield/SafeVerificationCallScripts.kt`
- `app/src/main/java/com/mongmong/familyshield/RecoveryPlan24h.kt`
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

## `/go` task: Risk Signal Explainer Cards slice

Implement a focused local-only screen explaining matched scam signals and safe next actions. This screen should teach the user/family how to interpret the app's local analysis without replaying raw scam text.

1. **New deterministic signal-explainer model**
   - Add a pure Kotlin builder/model, e.g. `RiskSignalExplainerBuilder`.
   - It must be unit-testable and not depend on Android UI classes.
   - Suggested inputs:
     - current `ScamAnalysis?`;
     - `FamilyReadiness` if useful.
   - Output should include:
     - Korean title and intro;
     - stable signal cards in order;
     - each card has id/signal key, label, category, why suspicious, what it often tries to make the user do, immediate safe move, family discussion prompt, primary/secondary actions routed to existing app flows, and urgency;
     - recommended signal card ids/actions based on current analysis signals and readiness;
     - if no analysis or no matched signals: a conservative default explainer set and clear CTA to analyze text;
     - privacy/safety notes;
     - safe family share summary.
   - Cards should cover every current `ScamSignal` enum value, grouped into meaningful categories:
     - urgency/money pressure;
     - links/smishing/parcels/invitations;
     - credentials/OTP/PIN/passwords;
     - app install/remote control/screen sharing;
     - family emergency/new number/deepfake voice;
     - safe account/fraud refund/loan refinance/overseas payment;
     - gift cards/gifticons/crypto or other stored scam signals.
   - Recommendation logic should be deterministic:
     - no analysis/no signals: recommend general money/OTP/link/family-callback explainers + checker/quick stop;
     - family/deepfake/phone-number-change: recommend family/new-number/deepfake cards first + family callback/family alert;
     - money/credential/safe-account/gift/crypto: recommend money/credential cards + safe verification call/account lockdown/official help;
     - app-install/remote/screen-share: recommend remote/app cards + account lockdown/evidence/helper;
     - link/smishing: recommend link/smishing cards + evidence/family alert/current result;
     - high risk should include Quick Stop / Recovery Plan among actions.
   - Safe share summary must include only signal labels/categories/action labels and generic safety moves. It must exclude raw pasted scam text, redacted clues, safety word/callback/contact values, full phone numbers, 6+ digit account/OTP/PIN-like numbers, URLs/domains, resident-registration-like patterns.

2. **Visible UI integration**
   - Add Home CTA near analysis/education actions, e.g. “위험 신호 설명 보기”.
   - Add current-result CTA if appropriate, e.g. “탐지 신호 설명 보기”.
   - Add a screen that displays:
     - title/intro;
     - recommended signal cards;
     - all signal cards or categorized cards;
     - privacy/safety notes.
   - Add buttons to existing flows where appropriate:
     - Quick Stop Mode;
     - Checker / current Result;
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
   - Add “가족에게 위험 신호 설명 공유” using text-only `ACTION_SEND`; pass only the builder's safe share summary.
   - Do not store selected signal, read progress, share history, or checklist/progress state.
   - Do not mutate incident logs automatically.
   - Do not auto-dial, auto-send, read contacts, scrape messages, or request permissions.
   - Add Home/back CTA.

3. **Interoperability / safety**
   - Reuse existing navigation helpers and flows; do not duplicate entire existing flow copy.
   - Do not introduce new Android permissions.
   - Do not add network calls.
   - Use calm Korean copy and avoid shame/blame.
   - Do not claim the app conclusively identifies all scams, blocks calls/messages, removes malware, freezes accounts/cards, recovers money, gives legal advice, or guarantees safety/legal outcomes.
   - Explain that rules are local guidance from matched signals, not remote AI or official judgment.

4. **Tests**
   - Unit tests covering:
     - no-analysis/default signal card order and conservative recommendations/actions;
     - every `ScamSignal` enum value has an explainer card and stable label/category;
     - family/deepfake/number-change recommends relevant family cards and callback/family alert actions early;
     - money/credential/safe-account/gift/crypto recommends money/credential cards and safe-call/account/help actions;
     - remote-control/app/screen-share recommends remote/app cards and account/evidence/helper actions without malware-removal guarantee;
     - link/smishing recommends link/smishing cards and evidence/family alert/current-result actions without auto-scan claims;
     - high-risk analysis includes Quick Stop/Recovery Plan actions;
     - every card has category, why suspicious, scam pressure goal, safe move, family prompt, primary action, stable id/order;
     - share summary includes signal/action labels but excludes raw text, redacted clues, safety plan values, phone/account/OTP/PIN/URL/domain/resident-number patterns;
     - output deterministic for same inputs;
     - copy states manual/local/no storage/no contact reading/no auto-send/no monitoring/no network;
     - copy does not imply automatic monitoring, conclusive detection, call/SMS blocking, malware removal, account/card freeze, money recovery, legal guarantee, automatic deletion, or access to secrets.
   - Add/adjust privacy regression tests if needed to ensure the screen is stateless and share helper sends only builder summary.

5. **Docs**
   - Update `README.md` to document Risk Signal Explainer Cards as local-only, stateless, educational guidance based on matched local signals; it adds no permissions, reads no contacts/messages, auto-sends nothing, stores no signal selections/read history, and mutates no incidents.

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
feat: add risk signal explainers
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the Risk Signal Explainer Cards slice, or a truthful blocker handoff exists with exact evidence.
