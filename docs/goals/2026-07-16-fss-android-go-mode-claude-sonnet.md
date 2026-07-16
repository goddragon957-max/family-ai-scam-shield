# Claude Code Goal — `/go` continuation for Family AI Voice-Phishing Shield

Date: 2026-07-16 10:08 KST
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`
Model requested by orchestrator: Claude Code `sonnet` because `fable` is blocked. Fallback if `sonnet` cannot run: `opus`.

You are Claude Code being delegated a real Android Kotlin MVP. Continue in `/go` mode: do the obvious next valuable implementation slice without asking follow-up questions, but stay inside the safe project scope below.

## Current verified state before handoff

Hermes preflight just ran from repo root with the repo-local toolchain:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
```

Result: `BUILD SUCCESSFUL in 10s`, debug APK already exists. Git status before this goal file was clean except ignored `.gradle/`, `.tools/`, `app/build/`, and `local.properties`.

## Read first

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- `.gitignore`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentStore.kt`
- `app/src/main/java/com/mongmong/familyshield/SafetyReportBuilder.kt`
- existing tests under `app/src/test/java/com/mongmong/familyshield/`

## Product identity / non-negotiables

This is a Korean Android app for a **family AI voice-phishing guardian**. It must stay a real Android Kotlin project, not a web page or mock.

Keep:

- package/application id: `com.mongmong.familyshield`
- simple native Android Views, no Compose migration
- local-only MVP: no server, no internet/network analysis
- no SMS/call/contact/microphone/internet permissions
- no automatic surveillance; user-pasted/shared text only
- calm, practical Korean copy
- `android:allowBackup="false"`
- no build artifacts or local toolchain files in commits

## `/go` task: implement one cohesive next product slice

The app already has onboarding, checker, analyzer, family safety plan, incident log, share entry, and safe family report. The next obvious MVP slice is to make the result screen feel more like an **actionable AI 보호자 실행 루틴**, not just a risk label.

Implement a cohesive, user-facing improvement that includes all of the following:

1. **Risk-specific action plan**
   - For each analysis result, generate a short ordered checklist for what to do in the next 3 minutes.
   - High risk should clearly say to stop transfer/app install/OTP, hang up if pressured, use saved family callback/contact, and report/payment-stop if money was sent.
   - Medium risk should focus on official-channel verification and not clicking links.
   - Safe should still warn about money/OTP/app-install requests.
   - Keep the logic deterministic and unit-testable; prefer a small new Kotlin object/data model if that keeps `MainActivity` compact.

2. **Visible UI integration**
   - Show the action plan prominently on the result screen in Korean.
   - Keep native View style consistent with the current app.
   - Do not remove current result elements: risk card, AI guardian advice, family plan section, emergency section, safe report share, incident save.

3. **Report/log consistency**
   - If you add an action plan model, include the important action plan lines in the safe family report without copying the original pasted scam text.
   - Incident logs must remain bounded/local and must not become a raw-text leak beyond the current safe preview behavior.

4. **Analyzer coverage improvement**
   - Add or improve at least one real Korean scam pattern relevant to family voice phishing / elderly-target scams, such as:
     - “명의 도용”, “계좌 보호”, “자금 세탁 연루”, “안전 계좌”, “대포통장” authority-pressure scams;
     - “카드 배송/택배/청첩장/부고장 링크” smishing;
     - “소액 결제/해외 결제 승인” panic bait.
   - Add regression tests for the new coverage.

5. **Docs**
   - Update `README.md` if user-visible flow/feature wording changed.

## Verification gates

Run fresh gates before claiming completion:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$PWD/.tools/android-sdk"
./gradlew test assembleDebug
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

Also run:

```bash
git diff --check
```

## Git policy

- Create one local commit only if verification passes.
- Do not push.
- Do not commit `.hermes/`, `.gradle/`, `.tools/`, `app/build/`, `local.properties`, or other machine-local files.
- It is okay to commit this goal file with the implementation if useful, but do not include transient logs.

Suggested commit message:

```text
feat: add guardian action plan flow
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

Completion condition: the repo has a verified local Android debug APK and a local commit for the action-plan slice, or a truthful blocker handoff exists with exact evidence.
