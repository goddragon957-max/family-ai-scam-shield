# Codex Goal — `/go` continuation: Account & Device Lockdown Checklist

Date: 2026-07-16 21:56 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: Claude Fable/Sonnet/Opus/Haiku are session-limited until 23:10 Asia/Seoul, so Hermes is continuing `/go` with the verified Codex fallback.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Incident Timeline Briefing slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
# => BUILD SUCCESSFUL in 1s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 973K, 995396 bytes
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
IncidentTimelineBriefingBuilderTest: debug/release tests=10 failures=0 errors=0
PrivacyPostureRegressionTest: debug/release tests=4 failures=0 errors=0
SafetyFlowRouterBuilderTest: debug/release tests=8 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 13` of `origin/main`
- latest local commits:
  - `6f576bb feat: add incident timeline briefing`
  - `2a6b396 feat: add safety flow router`
  - `b194ad2 feat: add privacy center`
  - `bf55ce0 feat: add family callback verification card`
  - `8a23aa3 feat: add official report prep card`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now has incident routing, evidence, report prep, official help, and timeline summaries. The next cohesive slice is an **Account & Device Lockdown Checklist** for users who may have shared OTP/passwords, installed remote-control apps, screen-shared, clicked phishing links, or exposed banking/messaging accounts.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -10`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentResponse.kt`
- `app/src/main/java/com/mongmong/familyshield/EvidencePreservationKit.kt`
- `app/src/main/java/com/mongmong/familyshield/OfficialHelpDirectory.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyFlowRouter.kt`
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

## `/go` task: Account & Device Lockdown Checklist slice

Implement a focused local-only checklist that tells the user what to lock down after a scam exposure. It should be practical guidance only; it must not pretend to change settings automatically.

1. **New deterministic lockdown model**
   - Add a small pure Kotlin builder/model, e.g. `AccountLockdownChecklistBuilder`.
   - It must be unit-testable and not depend on Android UI classes.
   - Suggested inputs:
     - current `ScamAnalysis?`;
     - optional saved incident count or latest briefing context if useful;
     - optional boolean flags for “has current analysis” can be derived.
   - Output should include:
     - Korean title and intro;
     - stable checklist sections/cards in order;
     - each section has id, title, priority/urgency, why it matters, manual steps, do-not-do notes, and action labels mapping to existing app flows;
     - recommended section ids based on current analysis signals;
     - privacy/safety notes;
     - safe family share summary.
   - Checklist sections should cover at least:
     - **Bank/card/payment freeze**: bank/card app or official phone, payment stop, transaction review;
     - **Messaging/social account recovery**: Kakao/Telegram/SNS logout sessions, password reset, 2FA, warn family;
     - **Device/remote-control cleanup**: disconnect network if actively controlled, uninstall suspicious remote/control apps, revoke accessibility/device-admin only via device settings, use a safe second device for password changes;
     - **Credential/OTP exposure**: change passwords, revoke sessions, reissue OTP/security cards where appropriate;
     - **Link/smishing exposure**: do not reopen links, preserve message, check official app/site manually;
     - **SIM/phone-number risk**: carrier/customer center check for USIM/eSIM/number changes if phone takeover signs appear;
     - **Evidence & official help**: preserve evidence, then use official help/report prep.
   - Recommendation logic should be deterministic and simple:
     - remote/app/screen-share signals prioritize device cleanup and credential/session reset;
     - credential/OTP/safe-account/money/gift/crypto signals prioritize bank/card/payment and credential/OTP sections;
     - link/smishing signals prioritize link/smishing + evidence/help;
     - family/deepfake/number-change signals include messaging/social/family warning and safety router/callback route;
     - no analysis: provide a conservative default: safety router, new analysis, evidence/help, and general account check.
   - Safe share summary must include section/action labels but exclude raw pasted scam text, redacted clue, safe word/callback/contact details, phone numbers, 6+ digit account/OTP/PIN-like numbers, URLs/domains, resident-registration-like patterns.

2. **Visible UI integration**
   - Add Home CTA, e.g. “계정·기기 잠금 체크리스트”.
   - Add a screen that displays:
     - title/intro;
     - recommended sections if current analysis exists, otherwise default guidance;
     - checklist sections with manual steps and do-not-do notes;
     - privacy/safety notes.
   - Add navigation buttons to existing flows where appropriate:
     - Safety Flow Router;
     - Response Center;
     - Evidence Preservation Kit;
     - Official Help Directory;
     - Report Prep if current analysis exists, otherwise New Analysis.
   - Add “가족에게 잠금 체크리스트 공유” using text-only `ACTION_SEND`; use only the builder’s safe share summary.
   - Do not store checklist selections or checkbox state. This slice should be stateless unless a small deterministic non-persistent UI expansion is unavoidable.
   - Do not mutate incident logs automatically.
   - Add Home/back CTA.

3. **Interoperability / safety**
   - Reuse existing navigation helpers and flows; do not duplicate all official help/evidence/report copy.
   - Do not introduce any new Android permissions.
   - Do not add network calls.
   - Avoid instructions that could be harmful if a device is actively controlled; say to use a trusted/safe second device for password changes when remote access is suspected.
   - Do not claim the app can remove malware, block attackers, freeze accounts, recover money, or legally guarantee outcomes.

4. **Tests**
   - Unit tests covering:
     - no-analysis/default checklist order and conservative recommended sections;
     - remote-control/app-install/screen-share analysis prioritizes device cleanup and credential/session reset;
     - OTP/password/safe-account/money/gift/crypto analysis prioritizes bank/card/payment and credential/OTP sections;
     - link/smishing analysis prioritizes link caution, evidence, and official help without implying automatic scanning;
     - family/deepfake/number-change analysis recommends messaging/social warning and safety router/callback route;
     - every checklist section has title, urgency, why, manual steps, do-not-do notes, and action labels;
     - share summary includes section labels/actions but excludes raw text, redacted clues, safe word/callback/contact values, phone/account/OTP/PIN/URL/domain/resident-number patterns;
     - output deterministic for same inputs;
     - copy does not imply automatic monitoring, contact reading, network upload, malware removal guarantee, account freeze guarantee, legal guarantee, guaranteed recovery, or automatic deletion.
   - Add/adjust privacy regression tests if needed to ensure the screen does not mutate incident logs and share helper sends only builder summary.

5. **Docs**
   - Update `README.md` to document Account & Device Lockdown Checklist as local-only, stateless, manual guidance that uses no new permissions and shares only privacy-safe summary text.

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
feat: add account lockdown checklist
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the Account & Device Lockdown Checklist slice, or a truthful blocker handoff exists with exact evidence.
