# Build Verification Handoff — 2026-07-13

> **Note:** For the most recent fresh verification (including the family
> safety plan slice added later in this same day), see
> `docs/handoff/2026-07-13-toolchain-verified.md`. This file remains accurate
> for the history of how the toolchain blocker was originally resolved.

## Summary

Claude Code initially reported `BLOCKED` because its non-interactive sandbox could not run binaries and the repo had no Java in PATH. Hermes then independently recovered the toolchain without sudo by installing local tooling under ignored `.tools/`, ran fresh Gradle verification, and produced a debug APK.

## Toolchain used

- Local JDK: `.tools/jdk-17`
- JDK version: Temurin OpenJDK `17.0.19+10`
- Local Android SDK: `.tools/android-sdk`
- Android platform: `android-35`
- Android build tools installed by Gradle/SDK manager: `35.0.0` and `34.0.0`

`.tools/`, `.gradle/`, and `app/build/` are ignored and should not be committed.

## Fresh verification

Command run from repo root:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export ANDROID_HOME="$PWD/.tools/android-sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
./gradlew clean test assembleDebug
```

Result:

```txt
BUILD SUCCESSFUL in 17s
63 actionable tasks: 63 executed
```

Fresh unit test evidence:

```txt
app/build/test-results/testDebugUnitTest/TEST-com.mongmong.familyshield.ScamAnalyzerTest.xml
- timestamp: 2026-07-13T01:00:32
- tests: 3
- failures: 0
- errors: 0
```

Debug APK:

```txt
app/build/outputs/apk/debug/app-debug.apk
842387 bytes
2026-07-13 10:00:31.594002734 +0900
```

## App verification notes

The inspected app remains aligned with the product requirements:

- Android Kotlin project, Gradle Kotlin DSL.
- Package/application id: `com.mongmong.familyshield`.
- Native Android Views; no Compose migration.
- No sensitive Android permissions declared in `AndroidManifest.xml`.
- Manual paste-only suspicious text analysis; no SMS/call/contact/microphone monitoring.
- Local deterministic scam analyzer covers all required risk-signal categories.
- Incident log is local `SharedPreferences`, bounded to 30 entries.

## Next operator notes

To rebuild later on this machine, use:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export ANDROID_HOME="$PWD/.tools/android-sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
./gradlew clean test assembleDebug
```

Do not push unless explicitly requested.
