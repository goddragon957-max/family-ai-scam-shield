# 가족 AI 보이스피싱 방패 Android MVP

Korean Android Kotlin MVP for a family AI voice-phishing prevention app. Suspicious SMS/Kakao/call transcript text is pasted manually, analyzed locally on-device, and turned into a practical "AI 보호자" stop/callback routine. The app is consent-first, uses simple native Android Views, and does not request SMS, call, contact, microphone, internet, or network permissions.

## Flow

1. 명시적 동의 온보딩
2. 홈 대시보드
3. 의심 SMS/Kakao/통화 녹취 붙여넣기 또는 다른 앱에서 텍스트 공유로 바로 불러오기
4. 로컬 규칙 기반 AI 보호자 분석
5. 위험도, 탐지 신호, 즉시 중단 행동, 가족 콜백/안전단어 루틴, 긴급 대응 안내, 원문 없는 가족 공유 리포트
6. 기기 로컬 사건 기록 저장

## Share Entry

The launcher activity also accepts Android `ACTION_SEND` text shares (`text/plain`). A user can share a suspicious Kakao/SMS/memo text into the app; after consent, the checker opens with the shared text pre-filled. This does not add SMS, contact, call, microphone, or internet permissions.

## Family Safety Plan

홈 화면의 "가족 안전 계획 설정"에서 아래 내용을 미리 저장해둘 수 있습니다:

- 안전 확인 질문/단어 (예: 우리 가족만 아는 어릴 적 별명은?)
- 가족이 직접 입력한 신뢰 연락처 (연락처 앱 접근 없이 "이름 - 번호" 형식으로 수동 입력)
- 위급 상황에서 읽을 콜백 스크립트

저장된 계획은 분석 결과 화면에 자동으로 표시되어, 위험 신호가 있을 때 안전단어를 묻고
저장된 번호로 바로 전화(`ACTION_DIAL`, 통화 권한 요청 없음)할 수 있도록 안내합니다.
계획이 없으면 결과 화면에 계획 생성 CTA가 표시됩니다. 모든 데이터는
`SharedPreferences`에만 저장되며 기기 밖으로 전송되지 않습니다.

## AI Guardian Advice

The app does not call a remote model in this MVP. "AI 보호자" means a deterministic on-device prevention engine that converts matched scam signals into concrete stop/callback actions:

- verdict: 지금 멈춤 / 확인 전 보류 / 강한 위험 신호 적음
- immediate actions: stop transfer, refuse APK/remote-control/screen-share, do not share OTP/ID, use official numbers
- callback prompt: ask a family safe question and call back through an already-saved trusted number
- safe share report: summarizes risk/signals/actions for family chat without including the pasted original text

## 3분 실행 루틴 (Action Plan)

The result screen also shows a short, ordered "지금부터 3분" checklist tailored to the risk level:

- 높음: stop transfer/app install/OTP now, hang up if pressured, call back through the saved family number, and stop payment/report if money was already sent
- 주의: don't click links, verify through official bank/agency channels, hold off on entering personal info
- 낮음: still warns that money, OTP, or app-install requests need direct family verification

The important checklist lines are included in the safe family report (`SafetyReportBuilder`) without ever including the original pasted text.

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
- 가족 번호 변경 사칭 ("번호 바뀌었어", "이 번호로 저장해")
- 대환대출/보증료 편취 (대환대출, 전환대출, 보증료, 선입금)
- 기프티콘/상품권 PIN 요구
- 통화 유지/화면공유/원격 확인 압박
- 딥페이크/음성 복제 의심 (목소리 복제, 음성 합성, 보이스피싱)
- 명의도용/안전계좌 편취 (명의 도용, 계좌 보호, 자금세탁 연루, 안전계좌, 대포통장)
- 택배/청첩장/부고 링크 스미싱 (택배 배송/조회, 모바일 청첩장, 부고장)
- 해외/소액 결제 승인 알림 (해외 결제, 소액 결제, 결제 승인)

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

### Verified local toolchain (no sudo)

This repo also works fully offline of any system-wide install using a
repo-local JDK/Android SDK under the git-ignored `.tools/` directory. From
the repo root:

```bash
export JAVA_HOME="$PWD/.tools/jdk-17"
export PATH="$JAVA_HOME/bin:$PATH"
chmod +x gradlew
./gradlew test assembleDebug
```

If Gradle reports `SDK location not found`, create `local.properties`
(git-ignored, not committed) once with:

```properties
sdk.dir=/absolute/path/to/repo/.tools/android-sdk
```

See `docs/handoff/2026-07-13-toolchain-verified.md` for the latest verified
run and result.

## Privacy And Limitations

- No server and no network analysis.
- No automatic SMS/call monitoring.
- No Android permissions are requested for calls, SMS, contacts, microphone, or internet.
- Android backup is disabled (`android:allowBackup="false"`) so local incident/safety-plan data is not included in platform cloud backup.
- The user must paste suspicious text manually.
- Incident logs are stored only in local `SharedPreferences` on the device.
- Rule-based scoring is a safety checklist, not a guarantee. Always call family through an already-saved trusted number before sending money, installing apps, or sharing authentication codes.

## Package

Android package/application id:

```text
com.mongmong.familyshield
```
