# 가족 사기 방패 Android MVP

Korean Android Kotlin MVP for checking suspicious family scam messages locally on-device. The app is consent-first, uses simple native Android Views, and does not request SMS, call, contact, or network permissions.

## Flow

1. 명시적 동의 온보딩
2. 홈 대시보드
3. 의심 SMS/Kakao/통화 녹취 붙여넣기
4. 로컬 규칙 기반 사기 신호 분석
5. 위험도, 탐지 신호, 가족 콜백/안전단어 루틴, 긴급 대응 안내
6. 기기 로컬 사건 기록 저장

## Analyzer Signals

The local analyzer scores these signals:

- 긴급 송금 압박
- 검찰/경찰/은행/금감원 등 기관 사칭
- 앱 설치 또는 원격제어 요구
- 링크 클릭 유도
- 대출/지원금/환급 미끼
- 계좌/비밀번호/OTP/신분증 요구
- 가족 긴급상황, 목소리/딥페이크 사칭
- 상품권/가상자산 요구
- 비밀 유지 압박

## Build And Test

Requirements:

- JDK 17
- Android SDK with platform 35 and build-tools 35.0.0

Commands:

```bash
./gradlew test
./gradlew assembleDebug
```

If Java or the Android SDK is missing, run:

```bash
chmod +x scripts/bootstrap-android.sh
./scripts/bootstrap-android.sh
```

The bootstrap script installs Android command-line tools and SDK packages when Java is already available. It does not hide blockers: if JDK 17 is missing, install it first.

## Privacy And Limitations

- No server and no network analysis.
- No automatic SMS/call monitoring.
- No Android permissions are requested for calls, SMS, contacts, microphone, or internet.
- The user must paste suspicious text manually.
- Incident logs are stored only in local `SharedPreferences` on the device.
- Rule-based scoring is a safety checklist, not a guarantee. Always call family through an already-saved trusted number before sending money, installing apps, or sharing authentication codes.

## Package

Android package/application id:

```text
com.mongmong.familyshield
```
