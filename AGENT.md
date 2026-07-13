# AGENT.md — Family Scam Shield Android MVP

## Product
Korean Android MVP for a "가족 사기 방패" app: a consent-first, local-only helper that lets families check suspicious calls/SMS/Kakao messages before sending money or installing apps.

## Hard requirements
- Real Android Kotlin project, not a web placeholder.
- Kotlin + Gradle Kotlin DSL.
- Prefer simple native Android Views (no Compose) to minimize dependency/toolchain risk.
- Local-only MVP: no server, no network analysis, no surveillance.
- First-run consent onboarding must be explicit.
- Core path: 동의 온보딩 → 로컬 사기 텍스트 분석 → 위험 신호 설명 → 가족 확인 루틴/대응 체크리스트 → 사건 기록.
- Korean UI copy. Clear, practical, non-alarmist.
- Include unit tests for the rule-based scam analyzer.
- Keep the project Android Studio/import friendly and CLI-buildable with `./gradlew`.

## Privacy/Safety posture
This app must not imply it can automatically monitor other people or intercept calls without permission. The MVP accepts user-pasted text/transcripts and gives checklist guidance. It should clearly say: 가족 동의가 필요하며, 민감정보는 기기 안에서만 처리됩니다.

## Verification target
- `./gradlew test` should run analyzer tests.
- `./gradlew assembleDebug` should produce a debug APK when Android SDK/JDK are available.
- If the local machine lacks Android SDK/JDK, provide a reproducible bootstrap script and document the blocker honestly.
