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

## Incident Response Center (긴급 대응 센터)

홈 화면과 분석 결과 화면의 "이미 피해가 의심돼요 / 긴급 대응 센터" 버튼으로 들어갈 수 있는
화면입니다. 이미 아래 상황 중 하나가 벌어진 사용자를 위한 것으로, 원문 문자/통화 내용을
입력받거나 저장하지 않고 상황 선택만으로 동작합니다:

- 이미 돈을 보냈거나 상품권/코인을 전달함
- 앱을 설치했거나 원격제어·화면공유를 허용함
- 계좌·비밀번호·인증번호(OTP)·신분증 정보를 알려줌
- 링크만 눌렀고 돈이나 정보는 아직 주지 않음

상황을 고르면 `IncidentResponseBuilder`가 결정적으로 생성한 대응 단계(은행 지급정지/카드 잠금,
원격제어 앱 삭제 후 안전한 기기에서 비밀번호 변경, 증거 보관 방법 등)와 112(경찰)/182(보이스피싱
통합신고)/1332(금융감독원) 연락처를 보여줍니다. 연락처는 기존 안전 계획과 같은 방식으로
`ACTION_DIAL`만 사용하며, `CALL_PHONE` 권한을 요청하지 않습니다.

"가족에게 대응 요약 공유" 버튼은 `IncidentResponseReportBuilder`가 만든 요약을 공유합니다. 이
요약에는 선택한 상황과 대응 단계, 연락처만 포함되며, 사용자가 붙여넣은 원문 텍스트는 애초에 이
화면에 입력되지 않으므로 요약에도 포함되지 않습니다.

## Family Safety Drill (가족 안전 연습 / 3분 훈련)

홈 화면의 "가족 안전 연습 / 3분 훈련" 버튼으로 들어가는 연습 화면입니다. 실제 사건이 아닌,
앱에 내장된 가상 시나리오만 사용해 가족이 안전단어 질문과 콜백 루틴을 미리 연습할 수 있습니다.
시나리오는 `SafetyDrillBuilder`가 결정적으로 생성하며, 다음 네 가지를 포함합니다:

- 자녀/부모 번호 변경 송금 요청
- 검찰/경찰 안전계좌 압박
- 원격제어 앱·화면공유 압박
- 택배/청첩장 스미싱 링크

시나리오를 고르면 "연습용 가상"으로 명시된 가짜 문자/통화 대사, 이렇게 말하고 행동하라는
대응 단계, 통과 체크리스트가 표시됩니다. 가족 한 명이 가상 대사를 읽어주고 다른 가족이
대응을 연습하는 방식입니다.

가족 안전 계획에 안전단어가 저장되어 있으면 드릴 화면 상단에 "우리 가족 연습 질문"으로
표시되어 그 질문으로 연습하고, 없으면 안전 계획 생성 CTA가 표시됩니다. 훈련은 사용자
입력을 받지 않고 아무것도 저장·전송하지 않으며, 사건 기록에도 남기지 않습니다. 새 권한도
요청하지 않습니다.

## Family Readiness Scorecard (가족 준비도)

홈 화면 상단(개인정보 안내 바로 아래)에 표시되는 준비도 카드입니다. `FamilyReadinessBuilder`가
저장된 가족 안전 계획만 입력으로 사용해 결정적으로 계산하며, 네트워크나 새 권한, 붙여넣은
원문 텍스트를 전혀 사용하지 않습니다. 체크리스트는 네 항목입니다:

- 가족만 아는 안전단어(확인 질문) 저장
- 콜백용 신뢰 연락처 1명 이상 저장
- 위급할 때 읽을 콜백 스크립트 저장
- 예비 연락처까지 2명 이상 저장 (선택, 중복 대비)

핵심 세 항목의 완료 수에 따라 준비 부족 / 준비 중 / 준비 좋음 단계와 완료 개수(예: 2/4),
다음 할 일 한 가지를 한국어로 보여줍니다. 항목이 비어 있으면 "가족 안전 계획 설정"과
"가족 안전 연습 / 3분 훈련" 버튼이 카드와 함께 표시되고, 모두 준비된 상태에서도 연습과
계획 수정 버튼이 유지됩니다. 드릴 화면에도 같은 계산 결과로 짧은 준비 상태 안내가 표시되어,
계획이 부족하면 설정을 권하고 준비가 끝났으면 저장된 루틴 그대로 연습하라고 알려줍니다.
새로운 저장소나 상태는 추가하지 않습니다.

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
