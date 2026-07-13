# Claude Code Goal — Finish `가족 사기 방패` Android MVP

Date: 2026-07-13
Working directory: `/home/sy/.openclaw/workspace/family-scam-shield-android`

You are Claude Code being handed a real Android Kotlin MVP. Continue from the current repository state; do **not** replace it with a web app, landing page, or fake mock.

## Product

`가족 사기 방패` is a Korean Android app that helps families pause before scams: suspicious SMS/Kakao/call transcript text is pasted manually, analyzed locally on-device, and turned into a clear family callback / safe-word / emergency response routine.

## Current state to inspect first

Read these files before editing:

- `AGENT.md`
- `README.md`
- `CODEX_GOAL.md`
- `.gitignore`
- `app/src/main/java/com/mongmong/familyshield/MainActivity.kt`
- `app/src/main/java/com/mongmong/familyshield/ScamAnalyzer.kt`
- `app/src/main/java/com/mongmong/familyshield/IncidentStore.kt`
- `app/src/test/java/com/mongmong/familyshield/ScamAnalyzerTest.kt`

Known current blocker from Hermes verification just before handoff:

```txt
./gradlew test assembleDebug
→ ERROR: Java is required to run Gradle. Install JDK 17.
command -v java → missing
ANDROID_HOME → unset
```

A previous test result file exists showing `ScamAnalyzerTest` had 3/3 passing, but you must perform fresh verification before claiming done.

## Hard requirements

1. Keep this as a **real Android Kotlin project** using Gradle Kotlin DSL.
2. Package/application id stays `com.mongmong.familyshield`.
3. Use simple native Android Views; do not migrate to Compose unless absolutely necessary.
4. Local-only, privacy-first MVP:
   - no backend/server dependency,
   - no automatic SMS/call/contact monitoring,
   - no microphone/contact/SMS/call/internet permissions,
   - user manually pastes suspicious text/transcripts,
   - incident logs remain on-device.
5. Korean UI copy must be practical, calm, and non-alarmist.
6. The app path must remain:
   - onboarding consent → home → paste suspicious text → local analysis result → family callback/safe-word routine → emergency checklist → local incident log.
7. Do not commit build artifacts, `.gradle/`, `app/build/`, `.tools/`, or machine-local files.

## Implementation goals

Finish this MVP to a handoffable debug build.

Minimum work:

1. Clean project hygiene:
   - Ensure `.gitignore` covers `.gradle/`, `build/`, `app/build/`, `.tools/`, `local.properties`, IDE noise.
   - Ensure `README.md` has truthful build/test instructions and privacy limitations.
   - Ensure `AGENT.md` still matches the actual app.
2. Improve source if needed:
   - Keep `MainActivity` compact but make the UI feel like an MVP app, not a raw sketch.
   - Make analyzer signals reliable for Korean scam phrases: urgent transfer, authority impersonation, app install/remote control, links, loan/support/refund bait, credentials/OTP, family emergency/voice-deepfake, gift cards/crypto, secrecy pressure.
   - Keep risk scoring deterministic and easy to test.
   - Keep incident log local and bounded.
3. Add or improve tests:
   - At least high-risk family emergency transfer.
   - At least medium-risk refund/link bait.
   - At least safe ordinary family message.
   - Add extra regression tests if you change scoring.
4. Android manifest safety:
   - Verify there are no sensitive permissions.
   - Confirm the launcher activity builds.
5. Verification:
   - Run `./gradlew test`.
   - Run `./gradlew assembleDebug`.
   - Verify debug APK exists at `app/build/outputs/apk/debug/app-debug.apk`.

## Toolchain recovery policy

If Java/Android SDK is missing:

1. First inspect `scripts/bootstrap-android.sh` and use it if appropriate.
2. Do not rely on interactive sudo. If `sudo -n true` fails, do not loop on apt.
3. Prefer a no-sudo local JDK 17 under `.tools/jdk-17` or `$HOME/.local/opt/jdk-17` only if network download is straightforward and safe; keep `.tools/` ignored.
4. Export `JAVA_HOME` and `ANDROID_HOME` only for the verification shell session.
5. If the Android SDK is missing but Java works, use the bootstrap script to install SDK platform 35 / build-tools 35.0.0.
6. If verification remains blocked, write a truthful blocker note to `docs/handoff/2026-07-13-toolchain-blocker.md` with exact commands and outputs. Do **not** say the APK is built if it is not.

## Git / completion policy

- If tests and `assembleDebug` pass, create a local git commit with a concise message such as:
  `feat: build family scam shield android mvp`
- If verification is blocked by toolchain, do not create a misleading completion commit. You may leave a handoff note and a coherent diff, but final output must say `BLOCKED` and name the exact missing tool/command.
- Never push unless explicitly asked.

## Final response format

End with one of these exact statuses:

### `DONE`
Include:
- Files changed summary
- Test command output summary
- APK path and file size
- Commit SHA

### `BLOCKED`
Include:
- Exact blocker
- Commands attempted
- Files changed
- Next command Hermes/user should run

Completion condition: this repo has a verified local Android debug APK and a local commit, or a truthful blocker handoff exists with exact evidence.
