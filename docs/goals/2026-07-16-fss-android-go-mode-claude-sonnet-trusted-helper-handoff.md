# Claude Goal — `/go` continuation: Trusted Helper Handoff Card

Date: 2026-07-16 23:29 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Claude Code CLI, model `sonnet`, effort `high`.
Reason: continue `/go` on the Android MVP with Claude Sonnet after the verified 24-Hour Recovery Plan slice. If this Claude model is blocked by usage/session limits, return a truthful `### BLOCKED` handoff with exact CLI output and do not fake completion.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous 24-Hour Recovery Plan slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
# => BUILD SUCCESSFUL in 2s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 1.1M, 1051454 bytes
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
RecoveryPlan24hBuilderTest: debug/release tests=10 failures=0 errors=0
PrivacyPostureRegressionTest: debug/release tests=8 failures=0 errors=0
QuickStopModeBuilderTest: debug/release tests=9 failures=0 errors=0
PrivacyCenterBuilderTest: debug/release tests=9 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 17` of `origin/main`
- latest local commits:
  - `bf22afe feat: add 24-hour recovery plan`
  - `4ddf0b6 feat: add quick stop mode`
  - `07fd9cb feat: add family alert message pack`
  - `ab9ffd7 feat: add account lockdown checklist`
  - `6f576bb feat: add incident timeline briefing`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now covers immediate stop, flow routing, family alerts, account lockdown, official help, evidence, incident timelines, and 24-hour recovery. The next cohesive slice is a **Trusted Helper Handoff Card**: a privacy-safe, role-specific way for a scared user to ask one trusted person for help without dumping raw scam text, secret safety-plan values, or sensitive identifiers into chat.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -12`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
- `app/src/main/java/com/mongmong/familyshield/QuickStopMode.kt`
- `app/src/main/java/com/mongmong/familyshield/RecoveryPlan24h.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyCallbackCard.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyAlertMessagePack.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyFlowRouter.kt`
- `app/src/main/java/com/mongmong/familyshield/AccountLockdownChecklist.kt`
- `app/src/main/java/com/mongmong/familyshield/EvidencePreservationKit.kt`
- `app/src/main/java/com/mongmong/familyshield/OfficialHelpDirectory.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentTimelineBriefing.kt`
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

## `/go` task: Trusted Helper Handoff Card slice

Implement a focused local-only handoff screen for asking a trusted helper/caregiver/family member to assist. This is not the same as a broad family warning blast; it should say “please help me do these steps” in a privacy-safe way.

1. **New deterministic helper-handoff model**
   - Add a pure Kotlin builder/model, e.g. `TrustedHelperHandoffBuilder`.
   - It must be unit-testable and not depend on Android UI classes.
   - Suggested inputs:
     - current `ScamAnalysis?`;
     - `FamilyReadiness` or value-less safety-plan readiness;
     - optional saved incident count if useful, but do not include incident previews/raw text in output.
   - Output should include:
     - Korean title and intro;
     - helper roles/cards in stable order;
     - each card has id, label, audience/role, why this helper matters, what to ask them to do, what not to ask them to do, primary/secondary actions routed to existing app flows, and urgency;
     - recommended helper card ids/actions based on current analysis signals and readiness;
     - privacy/safety notes;
     - safe handoff share message.
   - Helper cards should cover at least:
     - **차분한 보호자/동행자**: stay with me, stop pressure, help call existing family number;
     - **가족 확인 도우미**: run family callback/safety plan and prevent new-number trust;
     - **금융·계정 정리 도우미**: help with response center/account lockdown/official help when money/credentials exposed;
     - **기기·앱 점검 도우미**: help stop remote control/app exposure and use account lockdown/evidence paths;
     - **증거·신고 정리 도우미**: preserve screenshots/times and prepare official report/help;
     - **가족 경고 전달 도우미**: help share the family alert message pack without secrets;
     - **이번 주 점검 도우미**: help review recovery plan/timeline/privacy center.
   - Recommendation logic should be deterministic:
     - no analysis: recommend calm companion + checker/new analysis + family callback/safety plan + weekly review;
     - family/deepfake/phone-number-change: recommend family confirmation and family warning early;
     - money/credential/safe-account/gift/crypto: recommend finance/account helper, evidence/report helper, official help;
     - app-install/remote/screen-share: recommend device/app helper, evidence/report helper, account lockdown;
     - link/smishing: recommend evidence/report helper, family warning helper, checker/current result;
     - low readiness: recommend family confirmation/safety plan helper.
   - Safe share message must include only generic help requests, role/action labels, and existing app route labels. It must exclude raw pasted scam text, redacted clues, safety word/callback/contact values, full phone numbers, 6+ digit account/OTP/PIN-like numbers, URLs/domains, resident-registration-like patterns.

2. **Visible UI integration**
   - Add Home CTA near recovery/family actions, e.g. “보호자 도움 요청 카드”.
   - Add a screen that displays:
     - title/intro;
     - recommended helper cards;
     - all helper cards;
     - privacy/safety notes.
   - Add buttons to existing flows where appropriate:
     - Quick Stop Mode;
     - Checker / current Result;
     - Family Callback Card;
     - Safety Plan;
     - Safety Flow Router;
     - Response Center;
     - Account Lockdown Checklist;
     - Evidence Preservation Kit;
     - Official Help Directory;
     - Family Alert Message Pack;
     - Recovery Plan 24h;
     - Incident Timeline Briefing / Privacy Center if already present as screens.
   - Add “보호자에게 도움 요청 공유” using text-only `ACTION_SEND`; pass only the builder’s safe handoff share message.
   - Do not store selected helper, share history, or checklist/progress state.
   - Do not mutate incident logs automatically.
   - Do not auto-dial, auto-send, read contacts, or request permissions.
   - Add Home/back CTA.

3. **Interoperability / safety**
   - Reuse existing navigation helpers and flows; do not duplicate official help/evidence/account/recovery copy.
   - Do not introduce new Android permissions.
   - Do not add network calls.
   - Use calm Korean copy that reduces shame/blame: helper should not interrogate, blame, or demand secrets.
   - Do not claim the app blocks calls/messages, removes malware, freezes accounts/cards, recovers money, gives legal advice, or guarantees safety/legal outcomes.

4. **Tests**
   - Unit tests covering:
     - no-analysis/default helper order and conservative recommended helpers/actions;
     - family/deepfake/number-change recommends family confirmation and family warning early;
     - money/credential/safe-account/gift/crypto recommends finance/account + evidence/report/help;
     - remote-control/app/screen-share recommends device/app + evidence/account/help without malware-removal guarantee;
     - link/smishing recommends evidence/report + family warning + analysis/current result without auto-scan claims;
     - low readiness recommends family confirmation/safety plan helper; ready readiness copy differs calmly;
     - every helper card has role/audience, why, ask steps, do-not-ask notes, primary action, stable id/order;
     - share message includes helper/action labels but excludes raw text, redacted clues, safety plan values, phone/account/OTP/PIN/URL/domain/resident-number patterns;
     - output deterministic for same inputs;
     - copy states manual/no storage/no contact reading/no auto-send/no monitoring/no network;
     - copy does not imply automatic monitoring, call/SMS blocking, malware removal, account/card freeze, money recovery, legal guarantee, automatic deletion, or helper access to secrets.
   - Add/adjust privacy regression tests if needed to ensure the screen is stateless and share helper sends only builder summary.

5. **Docs**
   - Update `README.md` to document Trusted Helper Handoff Card as local-only, stateless, manual help-request guidance that adds no permissions, reads no contacts, auto-sends nothing, stores no helper selections/progress, and mutates no incidents.

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
feat: add trusted helper handoff card
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the Trusted Helper Handoff Card slice, or a truthful blocker handoff exists with exact evidence.
