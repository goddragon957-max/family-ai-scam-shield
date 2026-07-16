# Claude Goal — `/go` continuation: Safe Verification Call Scripts

Date: 2026-07-16 23:42 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Claude Code CLI, model `sonnet`, effort `high`.
Reason: continue `/go` on the Android MVP with Claude Sonnet after the verified Trusted Helper Handoff Card slice. If this Claude model is blocked by usage/session limits, return a truthful `### BLOCKED` handoff with exact CLI output and do not fake completion.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Trusted Helper Handoff Card slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug lintDebug
# => BUILD SUCCESSFUL in 3s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 1.1M, 1066087 bytes
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
TrustedHelperHandoffCardBuilderTest: debug/release tests=11 failures=0 errors=0
PrivacyPostureRegressionTest: debug/release tests=9 failures=0 errors=0
RecoveryPlan24hBuilderTest: debug/release tests=10 failures=0 errors=0
PrivacyCenterBuilderTest: debug/release tests=9 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 18` of `origin/main`
- latest local commits:
  - `ff22a67 feat: add trusted helper handoff card`
  - `bf22afe feat: add 24-hour recovery plan`
  - `4ddf0b6 feat: add quick stop mode`
  - `07fd9cb feat: add family alert message pack`
  - `ab9ffd7 feat: add account lockdown checklist`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now helps users stop, plan recovery, ask trusted helpers, warn family, and reach official help. The next cohesive slice is **Safe Verification Call Scripts**: short, privacy-safe scripts for what to say when manually calling a trusted family number, bank/card company, 1394/112/1332, carrier/account support, or a trusted helper. This complements Official Help and Report Prep without duplicating their details.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -14`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
- `app/src/main/java/com/mongmong/familyshield/QuickStopMode.kt`
- `app/src/main/java/com/mongmong/familyshield/RecoveryPlan24h.kt`
- `app/src/main/java/com/mongmong/familyshield/TrustedHelperHandoffCard.kt`
- `app/src/main/java/com/mongmong/familyshield/FamilyCallbackCard.kt`
- `app/src/main/java/com/mongmong/familyshield/OfficialReportPrep.kt`
- `app/src/main/java/com/mongmong/familyshield/OfficialHelpDirectory.kt`
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

## `/go` task: Safe Verification Call Scripts slice

Implement a focused local-only screen that gives the user safe words to say on manual calls. It should not call for the user or expose secrets.

1. **New deterministic call-script model**
   - Add a pure Kotlin builder/model, e.g. `SafeVerificationCallScriptsBuilder`.
   - It must be unit-testable and not depend on Android UI classes.
   - Suggested inputs:
     - current `ScamAnalysis?`;
     - `FamilyReadiness` or value-less safety-plan readiness.
   - Output should include:
     - Korean title and intro;
     - script cards in stable order;
     - each card has id, label, who to call, when to use, opening script, questions to ask, what never to say, primary/secondary actions routed to existing app flows, and urgency;
     - recommended script ids/actions based on current analysis signals and readiness;
     - privacy/safety notes;
     - safe share summary containing generic scripts/action labels only.
   - Script cards should cover at least:
     - **가족 기존 번호 확인 전화**: use saved/known number, do not trust new number;
     - **은행·카드사 확인 전화**: ask about suspicious transfer/card/payment and next steps through official channel;
     - **1394/112/1332 상담 전화**: summarize situation calmly and ask which official route applies;
     - **통신사·계정 지원 확인 전화**: SIM/number/account takeover concerns, ask official process only;
     - **기기·원격제어 점검 도움 전화**: ask trusted helper to sit together, not take over secrets;
     - **증거·신고 준비 전화**: ask helper to help preserve screenshots/times before deleting;
     - **예방 점검 전화**: weekly family safety-plan follow-up.
   - Recommendation logic should be deterministic:
     - no analysis: recommend family existing-number call, checker/new analysis, prevention call;
     - family/deepfake/phone-number-change: recommend family existing-number call first, then family alert/helper if useful;
     - money/credential/safe-account/gift/crypto: recommend bank/card call, official 1394/112/1332 call, account lockdown/report prep;
     - app-install/remote/screen-share: recommend device/helper call, account lockdown, evidence/help;
     - link/smishing: recommend evidence/report call, official help, family alert, current analysis;
     - low readiness: recommend family existing-number call and safety plan.
   - Safe share summary must include only generic call scripts, route/action labels, and manual-call reminders. It must exclude raw pasted scam text, redacted clues, safety word/callback/contact values, full phone numbers, 6+ digit account/OTP/PIN-like numbers, URLs/domains, resident-registration-like patterns.

2. **Visible UI integration**
   - Add Home CTA near official/recovery/helper actions, e.g. “안전 확인 전화 스크립트”.
   - Add a screen that displays:
     - title/intro;
     - recommended script cards;
     - all script cards;
     - privacy/safety notes.
   - Add buttons to existing flows where appropriate:
     - Quick Stop Mode;
     - Checker / current Result;
     - Family Callback Card;
     - Safety Plan;
     - Trusted Helper Handoff Card;
     - Recovery Plan 24h;
     - Safety Flow Router;
     - Response Center;
     - Account Lockdown Checklist;
     - Evidence Preservation Kit;
     - Official Help Directory;
     - Official Report Prep Card;
     - Family Alert Message Pack.
   - Add “가족에게 전화 스크립트 공유” using text-only `ACTION_SEND`; pass only the builder’s safe share summary.
   - Do not store selected script, call history, share history, or checklist/progress state.
   - Do not mutate incident logs automatically.
   - Do not auto-dial, auto-send, read contacts, scrape numbers, or request permissions.
   - If adding a phone-related button, prefer routing to existing Official Help / Family Callback screens; any direct dial must use `ACTION_DIAL` only.
   - Add Home/back CTA.

3. **Interoperability / safety**
   - Reuse existing navigation helpers and flows; do not duplicate the entire official help directory or report-prep content.
   - Do not introduce new Android permissions.
   - Do not add network calls.
   - Use calm Korean copy that reduces shame/blame.
   - Do not claim the app blocks calls/messages, removes malware, freezes accounts/cards, recovers money, gives legal advice, or guarantees safety/legal outcomes.
   - Scripts must explicitly tell users not to say OTP/PIN/password/account full numbers/safety word/callback details aloud unless they are in a verified official process that truly requires it; even then, this app should not collect or display those values.

4. **Tests**
   - Unit tests covering:
     - no-analysis/default script order and conservative recommended scripts/actions;
     - family/deepfake/number-change recommends family existing-number call early;
     - money/credential/safe-account/gift/crypto recommends bank/card + official help/report/account actions;
     - remote-control/app/screen-share recommends device/helper + account/evidence/help without malware-removal guarantee;
     - link/smishing recommends evidence/report + official help + family alert/current analysis without auto-scan claims;
     - low readiness recommends family call/safety plan; ready readiness copy differs calmly;
     - every script card has who-to-call, when-to-use, opening script, questions, never-say notes, primary action, stable id/order;
     - share summary includes script/action labels but excludes raw text, redacted clues, safety plan values, phone/account/OTP/PIN/URL/domain/resident-number patterns;
     - output deterministic for same inputs;
     - copy states manual/no storage/no contact reading/no auto-send/no auto-dial/no monitoring/no network;
     - copy does not imply automatic monitoring, call/SMS blocking, malware removal, account/card freeze, money recovery, legal guarantee, automatic deletion, or helper/agent access to secrets.
   - Add/adjust privacy regression tests if needed to ensure the screen is stateless and share helper sends only builder summary.

5. **Docs**
   - Update `README.md` to document Safe Verification Call Scripts as local-only, stateless, manual call guidance that adds no permissions, reads no contacts, auto-dials/sends nothing, stores no script selections/call history/progress, and mutates no incidents.

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
feat: add safe verification call scripts
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the Safe Verification Call Scripts slice, or a truthful blocker handoff exists with exact evidence.
