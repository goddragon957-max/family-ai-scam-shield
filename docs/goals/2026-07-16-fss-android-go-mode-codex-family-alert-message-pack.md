# Codex Goal — `/go` continuation: Family Alert Message Pack

Date: 2026-07-16 22:22 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: Claude Fable/Sonnet/Opus/Haiku are session-limited until 23:10 Asia/Seoul, so Hermes is continuing `/go` with the verified Codex fallback.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Account & Device Lockdown Checklist slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
# => BUILD SUCCESSFUL in 27s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 989K, 1012073 bytes
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
AccountLockdownChecklistBuilderTest: debug/release tests=11 failures=0 errors=0
PrivacyPostureRegressionTest: debug/release tests=5 failures=0 errors=0
IncidentTimelineBriefingBuilderTest: debug/release tests=10 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 14` of `origin/main`
- latest local commits:
  - `ab9ffd7 feat: add account lockdown checklist`
  - `6f576bb feat: add incident timeline briefing`
  - `2a6b396 feat: add safety flow router`
  - `b194ad2 feat: add privacy center`
  - `bf55ce0 feat: add family callback verification card`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now helps a user analyze, route, report, preserve evidence, lock down accounts/devices, and summarize incident records. The next cohesive slice is a **Family Alert Message Pack**: privacy-safe, ready-to-share warning messages for family members after suspected account takeover, new-number fraud, remote-control exposure, or urgent transfer requests. This should help a protector warn relatives quickly without leaking private scam text or secret safety-plan values.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -10`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyReadiness.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyCallbackCard.kt`
- `app/src/main/java/com/mongmong/familyshield/AccountLockdownChecklist.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyFlowRouter.kt`
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

## `/go` task: Family Alert Message Pack slice

Implement a focused local-only message pack that drafts safe family warning messages. It should provide copy that the user can share manually through Android’s share sheet; it must never auto-send or read contacts.

1. **New deterministic message-pack model**
   - Add a small pure Kotlin builder/model, e.g. `FamilyAlertMessagePackBuilder`.
   - It must be unit-testable and not depend on Android UI classes.
   - Suggested inputs:
     - current `ScamAnalysis?`;
     - saved `SafetyPlan` or `FamilyReadiness`;
     - optional incident count if useful.
   - Output should include:
     - Korean title and intro;
     - stable message templates/cards in order;
     - each card has id, title, audience, urgency/priority, when to use, message body, follow-up checklist, and action labels mapping to existing app flows;
     - recommended template ids based on current analysis signals and readiness;
     - privacy/safety notes;
     - a safe combined share summary.
   - Templates should cover at least:
     - **가족 전체 경고**: “내 계정/번호 사칭 가능성, 송금·인증 요청 무시, 기존 번호로 확인”;
     - **새 번호 사칭 주의**: family/new-number/deepfake signals, ask relatives not to trust new number requests;
     - **계정 탈취 의심 알림**: messaging/social account recovery, warn group not to answer requests;
     - **원격제어·앱 설치 주의**: do not open links/install apps/share screens if someone contacts them;
     - **송금·상품권·OTP 금지**: no money/gift cards/OTP/PIN/passwords;
     - **증거·신고 협조 요청**: preserve screenshots/times and use official help/report prep;
     - **일상 예방 공유**: calm prevention reminder for low/no analysis.
   - Recommendation logic should be deterministic and simple:
     - no analysis: recommend prevention + family-wide warning + safety-plan/callback check;
     - family/deepfake/phone-number-change signals: recommend new-number warning first, then family-wide warning;
     - credentials/money/gift/crypto/safe-account signals: recommend no-transfer/OTP warning and account-takeover alert;
     - app-install/remote/screen-share signals: recommend remote-control caution and account-takeover alert;
     - link/smishing signals: recommend remote/link caution and evidence/help cooperation;
     - always make prevention and evidence/help templates available.
   - Message bodies and combined share summary must exclude:
     - raw pasted scam text;
     - redacted clue;
     - safety word/callback script values;
     - contact labels/numbers;
     - full phone numbers;
     - 6+ digit account/OTP/PIN-like numbers;
     - URLs/domains;
     - resident-registration-like patterns;
     - exact secret-looking tokens from the input.
   - Mention that the user should send/share manually; the app does not auto-send and does not read contacts.

2. **Visible UI integration**
   - Add Home CTA, e.g. “가족 경고 메시지 만들기”.
   - Add a screen that displays:
     - title/intro;
     - current-analysis recommendations or default prevention guidance;
     - message template cards with message body and follow-up checklist;
     - privacy/safety notes.
   - Add buttons to existing flows where appropriate:
     - Family Callback Card;
     - Safety Flow Router;
     - Account Lockdown Checklist;
     - Evidence Preservation Kit;
     - Official Help Directory;
     - New Analysis/Report Prep as appropriate.
   - Add per-template share button and combined share button using text-only `ACTION_SEND`; pass only the builder-produced message body / safe summary.
   - Do not auto-send messages.
   - Do not read contacts.
   - Do not store selected template, share history, or checkbox state.
   - Do not mutate incident logs automatically.
   - Add Home/back CTA.

3. **Interoperability / safety**
   - Reuse existing navigation helpers and flows; do not duplicate all official help/evidence/account-lockdown copy.
   - Do not introduce any new Android permissions.
   - Do not add network calls.
   - Avoid telling the user to broadcast private details. Keep messages high-signal and privacy-preserving.
   - Do not promise account recovery, scam blocking, financial recovery, legal outcomes, or guaranteed safety.

4. **Tests**
   - Unit tests covering:
     - no-analysis/default message order and conservative recommended templates;
     - family/deepfake/number-change analysis recommends new-number/family warning first;
     - credential/money/gift/crypto/safe-account analysis recommends no-transfer/OTP and account-takeover messages;
     - remote-control/app/screen-share analysis recommends remote caution and account-takeover messages;
     - link/smishing analysis recommends link/remote caution plus evidence/help cooperation without implying automatic scanning;
     - every template has title, audience, urgency, when-to-use, body, follow-up checklist, and action labels;
     - per-template message bodies and combined summary exclude raw text, redacted clues, safe word/callback/contact values, full phone/account/OTP/PIN/URL/domain/resident-number patterns;
     - output deterministic for same inputs;
     - copy states manual sharing/no contact reading/no auto-send/no storage;
     - copy does not imply automatic monitoring, contact reading, network upload, automatic sending, account recovery guarantee, financial recovery guarantee, legal guarantee, or automatic deletion.
   - Add/adjust privacy regression tests if needed to ensure the screen does not mutate incident logs and share helpers send only builder strings.

5. **Docs**
   - Update `README.md` to document Family Alert Message Pack as local-only, stateless, manually shared templates that read no contacts, auto-send nothing, store no template selections, and add no permissions.

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
feat: add family alert message pack
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the Family Alert Message Pack slice, or a truthful blocker handoff exists with exact evidence.
