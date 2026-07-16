# Claude Goal — `/go` continuation: 24-Hour Recovery Plan

Date: 2026-07-16 23:14 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Claude Code CLI, model `sonnet`, effort `high`.
Reason: prior Claude Fable run was session-limited; continue `/go` by delegating to Claude with a different model now that the reset window has passed. If this Claude model is also blocked, return a truthful `### BLOCKED` handoff with exact CLI output and do not fake completion.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Quick Stop Mode slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
# => BUILD SUCCESSFUL in 1s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 1016K, 1040217 bytes
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
QuickStopModeBuilderTest: debug/release tests=9 failures=0 errors=0
PrivacyPostureRegressionTest: debug/release tests=7 failures=0 errors=0
FamilyAlertMessagePackBuilderTest: debug/release tests=10 failures=0 errors=0
PrivacyCenterBuilderTest: debug/release tests=9 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 16` of `origin/main`
- latest local commits:
  - `4ddf0b6 feat: add quick stop mode`
  - `07fd9cb feat: add family alert message pack`
  - `ab9ffd7 feat: add account lockdown checklist`
  - `6f576bb feat: add incident timeline briefing`
  - `2a6b396 feat: add safety flow router`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now has Quick Stop, routing, family alerts, account lockdown, official help, evidence preservation, and incident timeline. The next cohesive slice is a **24-Hour Recovery Plan**: after the user stops the live pressure, show a calm manual sequence for the next hour/day/week, based on what might have been exposed, without storing checklist state or promising recovery.

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
- `app/src/main/java/com/mongmong/familyshield/EvidencePreservationKit.kt`
- `app/src/main/java/com/mongmong/familyshield/OfficialHelpDirectory.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentTimelineBriefing.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyAlertMessagePack.kt`
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

## `/go` task: 24-Hour Recovery Plan slice

Implement a focused local-only recovery plan screen for after the immediate stop moment. It should help a user and family see “what to do next” over time, while routing to existing app modules.

1. **New deterministic recovery-plan model**
   - Add a pure Kotlin builder/model, e.g. `RecoveryPlan24hBuilder`.
   - It must be unit-testable and not depend on Android UI classes.
   - Suggested inputs:
     - current `ScamAnalysis?`;
     - saved incident count or a small incident-history summary if useful, but do not include incident previews/raw text in output;
     - `FamilyReadiness` if useful.
   - Output should include:
     - Korean title and intro;
     - time windows in stable order, e.g. `지금~15분`, `1시간 안`, `오늘 안`, `이번 주`;
     - each window has id, label, purpose, checklist steps, “do not do” notes, primary/secondary actions routed to existing app flows, and urgency;
     - recommended window ids/actions based on current analysis signals;
     - privacy/safety notes;
     - safe family share summary.
   - Recommended logic should be deterministic:
     - no analysis: recommend Quick Stop, new analysis/checker, family callback, prevention/review;
     - high risk or money/credential/safe-account/gift/crypto signals: emphasize immediate stop, official help, response center, account lockdown, evidence preservation;
     - remote-control/app/screen-share signals: emphasize disconnect/unsafe app review through existing account lockdown + evidence + official help;
     - link/smishing signals: emphasize do not reopen, evidence, family alert, official help, checker/current result;
     - family/deepfake/phone-number-change signals: emphasize family callback and family alert early;
     - always keep weekly prevention/readiness review available.
   - The plan must be manual guidance only:
     - no automatic recovery, no account freezing, no malware removal, no money recovery guarantee;
     - no stored checkboxes/progress state;
     - no mutation of incident logs.
   - Safe share summary must include only generic windows, checklist/action labels, and route labels. It must exclude raw pasted scam text, redacted clues, safety word/callback/contact values, full phone numbers, 6+ digit account/OTP/PIN-like numbers, URLs/domains, resident-registration-like patterns.

2. **Visible UI integration**
   - Add Home CTA near safety/response actions, e.g. “24시간 수습 계획”.
   - Add a screen that displays:
     - title/intro;
     - recommendations;
     - time-window cards with checklist steps and do-not-do notes;
     - privacy/safety notes.
   - Add buttons to existing flows where appropriate:
     - Quick Stop Mode;
     - Checker / current Result;
     - Safety Flow Router;
     - Response Center;
     - Account Lockdown Checklist;
     - Evidence Preservation Kit;
     - Official Help Directory;
     - Family Callback Card;
     - Family Alert Message Pack;
     - Incident Timeline Briefing / Privacy Center if already present as screens.
   - Add “가족에게 수습 계획 공유” using text-only `ACTION_SEND`; pass only the builder’s safe share summary.
   - Do not store checkbox/progress state or share history.
   - Do not mutate incident logs automatically.
   - Do not auto-dial, auto-send, read contacts, or request permissions.
   - Add Home/back CTA.

3. **Interoperability / safety**
   - Reuse existing navigation helpers and flows; do not duplicate all official help/evidence/account/family-alert copy.
   - Do not introduce new Android permissions.
   - Do not add network calls.
   - Avoid scary absolute claims. Use calm Korean copy.
   - Do not claim the app blocks calls/messages, removes malware, freezes accounts/cards, recovers money, gives legal advice, or guarantees safety/legal outcomes.

4. **Tests**
   - Unit tests covering:
     - no-analysis/default window order and conservative recommended actions;
     - high-risk money/credential/safe-account/gift/crypto analysis recommends immediate help, response/account lockdown, evidence;
     - remote-control/app/screen-share recommends account lockdown/evidence/help without malware-removal guarantee;
     - link/smishing recommends do-not-reopen, evidence, family alert/help without implying automatic scanning;
     - family/deepfake/number-change recommends family callback and family alert early;
     - every window has label, purpose, checklist, do-not-do notes, primary action, stable id/order;
     - share summary includes window/action labels but excludes raw text, redacted clues, safety plan values, phone/account/OTP/PIN/URL/domain/resident-number patterns;
     - output deterministic for same inputs;
     - copy states manual/no storage/no contact reading/no auto-send/no monitoring/no network;
     - copy does not imply automatic monitoring, call/SMS blocking, malware removal, account/card freeze, money recovery, legal guarantee, or automatic deletion.
   - Add/adjust privacy regression tests if needed to ensure the screen is stateless and share helper sends only builder summary.

5. **Docs**
   - Update `README.md` to document 24-Hour Recovery Plan as local-only, stateless, manual time-window guidance that adds no permissions, reads no contacts, auto-sends nothing, stores no progress, and mutates no incidents unless the user explicitly uses existing save/delete screens.

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
feat: add 24-hour recovery plan
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the 24-Hour Recovery Plan slice, or a truthful blocker handoff exists with exact evidence.
