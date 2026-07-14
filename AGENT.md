# AGENT.md — Family AI Voice-Phishing Shield Android MVP

## Product
Korean Android MVP for a "가족 AI 보이스피싱 방패" app: a consent-first, local-only helper that lets families check suspicious calls/SMS/Kakao messages before sending money or installing apps. The visible product promise is an "AI 보호자" prevention loop, implemented in this MVP as deterministic on-device risk scoring plus concrete stop/callback actions.

## Hard requirements
- Real Android Kotlin project, not a web placeholder.
- Kotlin + Gradle Kotlin DSL.
- Prefer simple native Android Views (no Compose) to minimize dependency/toolchain risk.
- Local-only MVP: no server, no network analysis, no surveillance.
- First-run consent onboarding must be explicit.
- Core path: 동의 온보딩 → AI 보호자 로컬 분석 → 위험 신호 설명 → 즉시 중단 행동 → 가족 확인 루틴/대응 체크리스트 → 사건 기록.
- Family safety plan: users can save a safe word, manually typed trusted callback contacts (no contact picker), and a callback script locally (`SafetyPlanStore`, `SharedPreferences`). The result screen surfaces this plan (or a CTA to create one) and offers `ACTION_DIAL` shortcuts — no `CALL_PHONE` permission requested.
- Korean UI copy. Clear, practical, non-alarmist.
- Include unit tests for the rule-based scam analyzer.
- Keep the project Android Studio/import friendly and CLI-buildable with `./gradlew`.

## Privacy/Safety posture
This app must not imply it can automatically monitor other people or intercept calls without permission. The MVP accepts user-pasted text/transcripts and gives checklist guidance. It should clearly say: 가족 동의가 필요하며, 민감정보는 기기 안에서만 처리됩니다. Keep Android backup disabled (`android:allowBackup="false"`) unless a future encrypted/export flow is deliberately designed.

## Verification target
- `./gradlew test` should run analyzer tests.
- `./gradlew assembleDebug` should produce a debug APK when Android SDK/JDK are available.
- If the local machine lacks Android SDK/JDK, provide a reproducible bootstrap script and document the blocker honestly.
- This machine has a repo-local JDK/Android SDK under the git-ignored `.tools/` directory that is known to work. From repo root:
  ```bash
  export JAVA_HOME="$PWD/.tools/jdk-17"
  export PATH="$JAVA_HOME/bin:$PATH"
  ./gradlew test assembleDebug
  ```
  See `docs/handoff/2026-07-13-toolchain-verified.md` for the latest verified run. Do not claim the toolchain is missing without first trying this.
