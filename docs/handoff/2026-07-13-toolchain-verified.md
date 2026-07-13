# Toolchain Verified — 2026-07-13 (Next Product Slice)

## Summary

This note supersedes the toolchain-recovery narrative in
`docs/handoff/2026-07-13-toolchain-blocker.md` for day-to-day use. That file
documents *how* Hermes originally recovered a no-sudo local JDK/Android SDK.
This file documents the *current* verified state after Claude Code added the
family safety plan slice in this same session, using that same toolchain.

The earlier "Java is missing" blocker no longer applies. A repo-local JDK 17
and Android SDK already exist under the git-ignored `.tools/` directory and
work when the environment is exported explicitly.

## One addition required this session

`local.properties` (already covered by `.gitignore`, never committed) did not
exist in this checkout, so Gradle could not find the Android SDK even with
`JAVA_HOME` exported. It was created once with:

```properties
sdk.dir=/home/sy/.openclaw/workspace/family-scam-shield-android/.tools/android-sdk
```

This is a normal local machine file, not a build artifact, and does not need
to be recreated unless the checkout is fresh or moved.

## Verified command (from repo root)

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
chmod +x gradlew
./gradlew test assembleDebug
ls -la app/build/outputs/apk/debug/app-debug.apk
```

## Fresh result (this session)

```txt
BUILD SUCCESSFUL in 7s
62 actionable tasks: 13 executed, 49 up-to-date
```

Unit tests, `ScamAnalyzerTest` (7 tests, 0 failures, 0 errors — 4 new tests
added for the expanded analyzer coverage in this slice):

```txt
app/build/test-results/testDebugUnitTest/TEST-com.mongmong.familyshield.ScamAnalyzerTest.xml
tests="7" skipped="0" failures="0" errors="0"
```

Debug APK:

```txt
app/build/outputs/apk/debug/app-debug.apk
875746 bytes
```

## Product change verified in this build

- Local "family verification plan" screen (safe word, manually typed trusted
  contacts, callback script) stored via `SafetyPlanStore`
  (`SharedPreferences`, on-device only).
- Result screen now shows the saved plan (or a CTA to create one) with
  safe-word question, dial buttons (`ACTION_DIAL`, no `CALL_PHONE`
  permission), and a refuse-until-verified checklist.
- `AndroidManifest.xml` inspected after the change: still declares no
  permissions at all.

Do not push unless explicitly requested.
