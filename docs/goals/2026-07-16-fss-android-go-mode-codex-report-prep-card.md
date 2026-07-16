# Codex Goal — `/go` continuation: Official Report Prep Card

Date: 2026-07-16 19:45 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Worker: Codex CLI `gpt-5.6-sol` with `model_reasoning_effort="ultra"`.
Reason: Claude Fable/Sonnet/Opus/Haiku are session-limited until 23:10 Asia/Seoul, so Hermes is continuing `/go` with the verified Codex fallback.

You are an autonomous coding agent being delegated a real Android Kotlin MVP. Continue in `/go` mode: implement the next cohesive product slice, run verification, and create a local commit only if verified. Do not ask follow-up questions.

## Current verified state before this handoff

Hermes independently verified the previous Official Help Directory slice:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
# => BUILD SUCCESSFUL in 1s
ls -lh app/build/outputs/apk/debug/app-debug.apk
# => 903K
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
OfficialHelpDirectoryBuilderTest: debug/release tests=6 failures=0 errors=0
IncidentResponseBuilderTest: debug/release tests=5 failures=0 errors=0
```

Git state before this goal file:

- branch `main` is clean in tracked files and `ahead 8` of `origin/main`
- latest local commits:
  - `9743ac4 feat: add official help directory`
  - `b557ad9 feat: harden incident log privacy`
  - `2759086 feat: add evidence preservation kit`
  - `1181b99 feat: add scam sample library`
  - `a386649 feat: add family readiness scorecard`
- ignored local state only: `.gradle/`, `.tools/`, `app/build/`, `local.properties`
- do not push

The app now has analysis, family callbacks, incident response, evidence preservation, privacy-safe logs, and official contact directory. The next obvious MVP slice is to help a user who just got a risky analysis prepare exactly what to say to official support **without exposing raw pasted scam text**.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- latest commit context (`git log --oneline -10`)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentPreviewRedactor.kt`
- `app/src/main/java/com/mongmong/familyshield/OfficialHelpDirectory.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentResponse.kt`
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

## `/go` task: Official Report Prep Card slice

Implement a focused local-only report/counseling prep card that turns a completed `ScamAnalysis` into a privacy-safe script for 112/1394/1332/bank-card calls.

1. **New deterministic report-prep model**
   - Add a small Kotlin builder/model, e.g. `OfficialReportPrepBuilder`.
   - Input: `ScamAnalysis`.
   - Output should include:
     - Korean title;
     - risk summary using risk label/score;
     - redacted clue/preview using `IncidentPreviewRedactor` — never raw pasted full text;
     - detected signal labels or a safe “강한 위험 신호 없음” fallback;
     - recommended official help entry IDs or labels from `OfficialHelpDirectoryBuilder`;
     - “상담원에게 말할 내용” script lines;
     - “전화 전 확인” checklist;
     - privacy/safety notes;
     - family/share summary string.
   - Recommendation logic should be deterministic and simple:
     - high risk or transfer/OTP/remote-control/account/safe-account signals should include 112 + 1394 + 1332 and bank/card guidance;
     - link/smishing without money/info should at least include 1394 and bank/card guidance if account/card risk is plausible;
     - lower risk should still include 1394 guidance and family callback first.
   - Keep copy clear that this is prep guidance, not legal/financial recovery guarantee.

2. **Visible UI integration**
   - Add a screen reachable from the Result screen, e.g. “신고용 상담 준비 카드”.
   - If opened without a current analysis, show a safe empty-state CTA to run analysis first.
   - The screen should display:
     - risk summary;
     - redacted clue;
     - detected signal labels;
     - recommended official contacts with dial buttons only where the official help entry has a number;
     - what-to-say script lines;
     - before-call checklist and privacy notes.
   - Add a share button, e.g. “가족에게 상담 준비 카드 공유”, using text-only `ACTION_SEND`.
   - Dial buttons must use `ACTION_DIAL`, with no new permissions.
   - Add a back CTA to the Result screen and Home.

3. **Interoperability**
   - Reuse `OfficialHelpDirectoryBuilder` rather than duplicating official numbers/copy where reasonable.
   - Reuse `IncidentPreviewRedactor` for all raw-text previews.
   - Do not store the report prep card in `SharedPreferences`.
   - Do not mutate incident logs automatically.

4. **Tests**
   - Unit tests covering:
     - high-risk transfer/OTP/remote-control/safe-account analysis recommends 112, 1394, 1332, and bank/card guidance;
     - link-only or smishing-like analysis recommends at least 1394 and avoids overclaiming guaranteed recovery;
     - redacted clue masks phone numbers, account-like long digits, OTP/auth codes, links/domains, and identity-like numbers;
     - family/share summary includes risk/signals/recommended contacts/scripts but excludes raw scam secrets;
     - output is deterministic for the same analysis;
     - copy does not imply automatic monitoring, network upload, legal guarantee, or guaranteed refund.
   - Keep or update existing tests accordingly.

5. **Docs**
   - Update `README.md` to document the report prep card as local-only, analysis-derived, privacy-redacted, not stored, and using `ACTION_DIAL`/`ACTION_SEND` only.

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
feat: add official report prep card
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the official report prep card slice, or a truthful blocker handoff exists with exact evidence.
