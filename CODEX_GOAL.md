/goal
Build a real Android Kotlin MVP in this repository for "가족 사기 방패".

Context:
- The user asked for `/go` continuous execution mode and specifically wants a real Android Kotlin project.
- This repo is `/home/sy/.openclaw/workspace/family-scam-shield-android`.
- Start from the existing `AGENT.md` requirements.

Implement:
1. Android Gradle project with root `settings.gradle.kts`, root `build.gradle.kts`, `gradle.properties`, app module, package `com.mongmong.familyshield`.
2. Kotlin source using simple native Android Views, not Compose:
   - `MainActivity` with a small single-activity navigation/state machine.
   - First-run explicit consent onboarding.
   - Home dashboard.
   - Scam text checker where the user pastes suspicious SMS/Kakao/call transcript.
   - Local rule-based analyzer that scores and labels risks: urgent money transfer, prosecutor/police/bank impersonation, app install/remote control, links, loan/support/refund bait, account/password/OTP requests, voice/deepfake family emergency, gift card/crypto, secrecy pressure.
   - Result screen with risk level, matched signals, recommended family callback/safe-word routine, emergency actions.
   - Incident log stored locally via SharedPreferences/JSON or simple serialized text.
3. Korean copy, privacy-first wording, no server/network.
4. Unit tests for the analyzer with at least high-risk, medium-risk, and safe-message examples.
5. `README.md` with project overview, flow, build/test commands, and privacy limitations.
6. Any helpful scripts for local bootstrap if Java/Android SDK is absent, but do not hide build blockers.

Quality:
- Keep code compact and understandable.
- Avoid Android permissions unless strictly needed; this MVP should not request call/SMS permissions.
- Make UI visibly app-like enough for MVP: clear sections, action buttons, risk chips, Korean text.
- Run available verification commands. If Android SDK/JDK is missing, still add wrapper/bootstrap instructions and note exact missing tool.
