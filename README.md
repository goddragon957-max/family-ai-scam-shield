# 가족 AI 보이스피싱 방패 Android MVP

Korean Android Kotlin MVP for a family AI voice-phishing prevention app. Suspicious SMS/Kakao/call transcript text is pasted manually, analyzed locally on-device, and turned into a practical "AI 보호자" stop/callback routine. The app is consent-first, uses simple native Android Views, and does not request SMS, call, contact, microphone, internet, or network permissions.

## Flow

1. 명시적 동의 온보딩
2. 홈 대시보드
3. 긴급 멈춤 모드에서 대화 중단 → 돈·정보·앱 금지 → 평소 신뢰 경로 확인의 3단계부터 실행
4. 안전 흐름 길잡이에서 현재 분석과 가족 준비 상태에 맞는 기존 대응 화면 선택
5. 개인정보 센터에서 로컬 데이터 목록, 사용하지 않는 권한, 항목별 삭제 제어 확인
6. 의심 SMS/Kakao/통화 녹취 붙여넣기 또는 다른 앱에서 텍스트 공유로 바로 불러오기
7. 로컬 규칙 기반 AI 보호자 분석
8. 위험도, 탐지 신호, 즉시 중단 행동, 가족 콜백/안전단어 루틴, 긴급 대응 안내, 원문 없는 가족 공유 리포트
9. 현재 분석과 저장된 안전 계획으로 가족 콜백 확인 카드 실행
10. 현재 분석에서 개인정보를 가린 신고용 상담 준비 카드 만들기
11. 상황별 신고·상담 연락처와 말할 내용 확인
12. 기기 로컬 사건 기록 저장(전체 원문 대신 개인정보 가림 미리보기)
13. 저장된 사건의 위험도·탐지 신호·시간 순서를 사건 타임라인 요약에서 확인하고 집계만 안전하게 가족 공유
14. 계정·기기 잠금 체크리스트에서 노출 가능성에 맞는 수동 보호 순서와 기존 대응 화면 확인
15. 가족 경고 메시지 팩에서 상황별 개인정보 보호 경고문을 골라 Android 공유 화면으로 직접 공유
16. 24시간 수습 계획에서 지금~15분·1시간 안·오늘 안·이번 주 시간대별로 무엇을 할지 확인하고 기존 대응 화면으로 이동
17. 보호자 도움 요청 카드에서 역할별 도우미에게 무엇을 부탁하고 무엇을 부탁하지 말지 확인한 뒤 비밀 없는 요청 문구를 공유

## Share Entry

The launcher activity also accepts Android `ACTION_SEND` text shares (`text/plain`). A user can share a suspicious Kakao/SMS/memo text into the app; after consent, the checker opens with the shared text pre-filled. This does not add SMS, contact, call, microphone, or internet permissions.

## Quick Stop Mode (긴급 멈춤 모드)

홈 맨 위의 "긴급 멈춤 모드"는 겁이 나거나 판단하기 어려운 순간을 위한 수동 첫 대응 화면입니다.
먼저 대화를 멈추고, 확인 전에는 돈·개인정보·인증번호를 주거나 앱을 설치하지 않으며, 상대가
알려준 새 번호나 링크가 아닌 평소 가족 번호 또는 공식 경로로 직접 확인하는 3단계를 크게
보여줍니다. `QuickStopModeBuilder`는 현재 `ScamAnalysis?`의 위험도·탐지 신호와 값 없는
`FamilyReadiness`만 사용해 같은 입력에 항상 같은 추천 카드 순서를 만듭니다.

추천과 모든 행동 카드는 기존 가족 콜백 확인 카드·가족 안전 계획·의심 내용 검사/현재 결과·안전
흐름 길잡이·긴급 대응 센터·계정/기기 잠금 체크리스트·증거 보존 체크리스트·신고/상담 연락처·
가족 경고 메시지 팩으로 이동합니다. 공식 도움, 증거 보존, 계정 보호 문구를 새 화면에 다시
구현하지 않습니다. 분석이 없으면 멈춤, 직접 검사, 가족 콜백을 먼저 안내하고, 송금·인증정보·
원격제어·안전계좌 같은 고위험 신호에는 긴급 대응·증거·공식 도움을, 가족/딥페이크/번호 변경
신호에는 가족 콜백을 앞에, 링크/스미싱 신호에는 현재 분석·증거·가족 경고·공식 도움을
결정적으로 추천합니다. 준비도가 낮으면 가족 콜백 카드에서 안전 계획을 시작하거나 보완하도록
차분히 안내합니다.

이 화면은 로컬 전용·무상태 수동 안내입니다. 버튼 선택, 공유 이력, 체크 상태를 저장하지 않고
화면을 열거나 버튼을 누르는 것만으로 사건 기록을 추가·수정·삭제하지 않습니다. 문자·통화·연락처를
읽거나 감시하지 않고, 받는 사람을 고르거나 메시지를 자동 전송하지 않으며, 네트워크 호출이나 새
Android 권한을 추가하지 않습니다. "가족에게 멈춤 안내 공유"는 사용자가 직접 눌렀을 때만
`ACTION_SEND`의 `text/plain` 공유 화면을 열고 빌더가 만든 `QuickStopMode.familyShareSummary`만
전달합니다. 공유문에는 일반 멈춤 안내와 앱 경로/행동 이름만 들어가며 붙여넣은 원문, 화면 단서,
안전 질문·콜백 내용·연락처 값, 전화·계좌·비밀번호·OTP/PIN·URL/도메인·신분정보 값을 넣지
않습니다.

## 24-Hour Recovery Plan (24시간 수습 계획)

홈의 "24시간 수습 계획"에서 긴급 멈춤 이후 다음 한 시간, 오늘, 이번 주에 무엇을 할지 시간대별로
차분히 확인할 수 있습니다. `RecoveryPlan24hBuilder`는 현재 `ScamAnalysis?`의 위험도·탐지 신호와
값 없는 `FamilyReadiness`만 사용해 같은 입력에 항상 같은 시간대 순서와 추천을 만듭니다. 시간대는
"지금~15분", "1시간 안", "오늘 안", "이번 주" 네 가지로 고정되어 있으며 각 시간대에는 목적, 할 일
체크리스트, 하지 말아야 할 일, 기존 화면으로 이동하는 주요·보조 버튼이 함께 표시됩니다.

분석이 없으면 지금~15분과 이번 주 시간대를 먼저 추천해 멈춤·직접 검사·가족 콜백과 예방·점검을
안내합니다. 고위험이거나 송금·인증정보·안전계좌·상품권·가상자산 신호가 있으면 네 시간대를 모두
추천해 긴급 대응·계정 잠금·증거 보존·공식 도움까지 이어가도록 안내합니다. 원격제어·앱 설치 신호도
네 시간대를 모두 추천해 연결 차단과 계정·기기 잠금·증거 보존을 안내합니다. 링크·스미싱 신호는
지금~15분·오늘 안·이번 주를 추천해 링크를 다시 열지 않는 확인과 증거 보존·가족 경고·공식 도움을
안내합니다. 가족·딥페이크·번호 변경 신호는 지금~15분·1시간 안·이번 주를 추천해 가족 콜백과 가족
경고를 이르게 안내합니다. 이번 주 시간대는 항상 추천 목록에 남아 예방·준비도 점검을 계속 안내
합니다.

각 시간대의 버튼은 기존 긴급 멈춤 모드, 의심 내용 검사/현재 결과, 가족 콜백 확인 카드, 안전 흐름
길잡이, 긴급 대응 센터, 계정·기기 잠금 체크리스트, 증거 보존 체크리스트, 신고·상담 연락처, 가족
경고 메시지 팩, 가족 안전 계획, 사건 타임라인 요약, 개인정보 센터로 이동할 뿐이며, 공식 도움·증거
보존·계정 보호 문구를 이 화면에 다시 구현하지 않습니다.

이 화면은 로컬 전용·무상태 수동 안내입니다. 시간대별 체크 상태나 선택을 저장하지 않고, 화면을
열거나 버튼을 누르는 것만으로 사건 기록을 추가·수정·삭제하지 않습니다. 문자·통화·연락처를 읽거나
감시하지 않고, 받는 사람을 고르거나 메시지를 자동 전송하지 않으며, 계좌·카드를 자동으로
지급정지하거나 악성 앱을 자동으로 제거하지 않고, 계정을 자동으로 잠그거나 피해금 회수·법적 결과를
보장하지 않습니다. "가족에게 수습 계획 공유"는 사용자가 직접 눌렀을 때만 `ACTION_SEND`의
`text/plain` 공유 화면을 열고 빌더가 만든 `RecoveryPlan24h.familyShareSummary`만 전달합니다.
공유문에는 시간대 이름과 앱 경로/행동 이름만 들어가며 붙여넣은 원문, 화면 단서, 안전 질문·콜백
내용·연락처 값, 전화·계좌·비밀번호·OTP/PIN·URL/도메인·신분정보 값은 넣지 않습니다.

## Trusted Helper Handoff Card (보호자 도움 요청 카드)

홈의 "보호자 도움 요청 카드"는 사용자가 원문이나 안전 계획 값을 그대로 보내지 않고도 한 명의
믿을 만한 사람에게 구체적으로 도움을 요청할 수 있게 돕는 로컬 전용·무상태 안내입니다.
`TrustedHelperHandoffBuilder`는 현재 `ScamAnalysis?`의 위험도·탐지 신호와 값 없는
`FamilyReadiness`만 사용해 같은 입력에 항상 같은 도우미 카드 순서와 추천을 만듭니다. 카드는
차분한 보호자/동행자, 가족 확인 도우미, 금융·계정 정리 도우미, 기기·앱 점검 도우미, 증거·신고
정리 도우미, 가족 경고 전달 도우미, 이번 주 점검 도우미 7종이며 각 카드에는 역할(대상), 왜
필요한지, 부탁할 일, 부탁하지 말아야 할 일, 기존 화면으로 이동하는 주요·보조 버튼이 함께
표시됩니다.

분석이 없으면 차분한 보호자와 가족 확인 도우미, 이번 주 점검 도우미를 기본으로 추천합니다.
가족·딥페이크·번호 변경 신호는 가족 확인 도우미와 가족 경고 전달 도우미를 이르게 추천하고,
송금·인증정보·안전계좌·상품권·가상자산 신호는 금융·계정 정리 도우미와 증거·신고 정리 도우미를
추천합니다. 원격제어·앱 설치 신호는 기기·앱 점검 도우미와 증거·신고 정리 도우미, 금융·계정 정리
도우미를 추천하고, 링크·스미싱 신호는 증거·신고 정리 도우미와 가족 경고 전달 도우미, 차분한
보호자를 추천합니다. 가족 준비도가 낮거나 진행 중이면 가족 확인 도우미를 항상 함께 추천합니다.
각 카드의 버튼은 기존 긴급 멈춤 모드, 의심 내용 검사/현재 결과, 가족 콜백 확인 카드, 가족 안전
계획, 안전 흐름 길잡이, 긴급 대응 센터, 계정·기기 잠금 체크리스트, 증거 보존 체크리스트,
신고·상담 연락처, 가족 경고 메시지 팩, 24시간 수습 계획, 사건 타임라인 요약, 개인정보 센터로
이동할 뿐이며 이 화면에 도움 관련 문구를 새로 구현하지 않습니다.

이 화면은 선택한 도우미나 공유 여부, 진행 상황을 저장하지 않으며 화면을 열거나 버튼을 누르는
것만으로 사건 기록을 추가·수정·삭제하지 않습니다. 문자·통화·연락처를 자동으로 읽거나 감시하지
않고, 새 Android 권한이나 네트워크 호출을 사용하지 않습니다. "보호자에게 도움 요청 공유"는
사용자가 직접 눌렀을 때만 `ACTION_SEND`의 `text/plain` 공유 화면을 열고 빌더가 만든
`TrustedHelperHandoffCard.handoffShareMessage`만 전달합니다. 공유문에는 도우미 역할·부탁할 일
요약과 앱 경로/행동 이름만 들어가며 붙여넣은 원문, 화면 단서, 안전 질문·콜백 내용·연락처 값,
전화·계좌·비밀번호·OTP/PIN·URL/도메인·신분정보 값은 넣지 않고, 도우미가 이런 비밀 정보에
접근하도록 하지 않습니다.

## Safety Flow Router (안전 흐름 길잡이)

홈 상단의 "지금 뭐부터 할지 고르기"에서 현재 분석의 위험도·탐지 신호와 가족 준비 상태를
바탕으로 다음 행동을 차분히 고를 수 있습니다. `SafetyFlowRouterBuilder`는 같은 입력에 항상 같은
순서와 추천을 만드는 로컬 규칙 기반 길잡이이며, 새로운 분석이나 신고 기능을 복제하지 않고 기존
의심 내용 검사, 가족 콜백·안전 계획, 긴급 대응 센터, 신고용 상담 준비 카드, 신고·상담 연락처,
증거 보존 체크리스트, 개인정보 센터 화면으로 연결합니다. 분석 결과가 없을 때는 검사와 가족
콜백 준비를 먼저 안내하고, 분석 결과가 있으면 탐지된 위험 신호에 맞는 기존 흐름을 추천합니다.

어떤 경로를 골랐는지와 추천 경로는 `SharedPreferences`나 다른 영구 저장소에 저장하지 않으며,
라우터를 열거나 버튼을 누르는 것만으로 사건 기록을 자동 추가·수정하지 않습니다. "가족에게 안전
흐름 요약 공유"는 사용자가 직접 누를 때 Android `ACTION_SEND`의 `text/plain`으로 경로 이름과
일반 행동만 전달합니다. 붙여넣은 원문, 화면의 개인정보 가림 단서, 안전단어·콜백 스크립트,
연락처 이름·전체 번호, 계좌·비밀번호·OTP/PIN·링크 값은 공유문에 포함하지 않습니다. 모든 추천은
기기 안에서 결정되며 자동 문자·통화·연락처 감시, 네트워크 호출, 새 Android 권한을 사용하지
않습니다.

## Account & Device Lockdown Checklist (계정·기기 잠금 체크리스트)

홈과 현재 AI 분석 결과의 "계정·기기 잠금 체크리스트"에서 사기 노출 뒤 직접 확인할 보호 순서를
볼 수 있습니다. `AccountLockdownChecklistBuilder`는 현재 `ScamAnalysis?`의 탐지 신호만 입력으로
사용해 은행·카드·결제 보호, 메신저·SNS 계정 복구, 기기·원격제어 정리, 비밀번호·OTP·보안수단
재설정, 링크·스미싱 후속 확인, 유심·전화번호 보호, 증거 보존·공식 도움 항목을 항상 같은 순서로
만듭니다. 원격제어·앱·화면공유 신호는 연결 차단과 안전한 다른 기기에서의 계정 보호를, 금융·
인증정보 신호는 은행·카드 확인과 비밀번호·보안수단 재설정을, 링크 신호는 재접속 금지와 증거·
공식 도움을, 가족 사칭·번호 변경 신호는 메신저 경고와 가족 콜백을 우선 안내합니다.

분석 결과가 없을 때도 일반 계정 점검과 증거·공식 도움을 보여주며 안전 흐름 길잡이, 새 분석,
긴급 대응 센터, 증거 보존 체크리스트, 신고·상담 연락처로 연결합니다. 분석 결과가 있으면 새 분석
대신 신고용 상담 준비 카드로 이어집니다. 이 화면은 기존 기능을 다시 구현하지 않고 해당 화면으로
이동할 뿐이며, 체크 완료 상태나 선택을 저장하지 않고 사건 기록을 자동 추가·수정·삭제하지
않습니다. 앱이 설정을 바꾸거나 앱·악성코드를 제거하고, 계정·카드·거래를 잠그거나 피해 회복을
보장하는 기능도 아닙니다. 모든 조치는 사용자가 신뢰할 수 있는 기기와 공식 경로에서 직접
진행합니다.

"가족에게 잠금 체크리스트 공유"는 사용자가 직접 누를 때 Android `ACTION_SEND`의 `text/plain`만
사용합니다. 공유문은 정적으로 정의된 항목과 기존 앱 행동 이름만 포함하며 붙여넣은 원문,
개인정보 가림 단서, 안전단어·콜백 내용·연락처 값, 전화·계좌·비밀번호·OTP/PIN·링크·도메인·
신분정보 값을 넣지 않습니다. 체크리스트는 기기 안에서 결정되며 네트워크 호출이나 새 Android
권한을 추가하지 않습니다.

## Family Alert Message Pack (가족 경고 메시지 팩)

홈과 현재 AI 분석 결과의 "가족 경고 메시지 만들기"에서 가족 전체 경고, 새 번호 사칭 주의,
계정 탈취 의심 알림, 원격제어·앱 설치·링크 주의, 송금·상품권·OTP 금지, 증거·신고 협조 요청,
일상 예방 공유 메시지를 볼 수 있습니다. `FamilyAlertMessagePackBuilder`는 `ScamAnalysis.input`을
읽거나 복사하지 않고 현재 분석의 탐지 신호만 사용하며, 가족 안전 계획 자체 대신 값 없는
`FamilyReadiness` 상태만 사용합니다. 따라서 붙여넣은 원문, 안전단어·콜백 스크립트·연락처 값을
메시지에 넣지 않습니다. 같은 신호와 준비 상태에는 항상 같은 템플릿 순서와 추천 결과를 만들고,
분석 결과가 없을 때도 일상 예방과 가족 전체 경고, 안전 계획·
콜백 점검을 기본으로 안내합니다. 예방과 증거·공식 도움 템플릿은 분석 유형과 관계없이 항상
확인할 수 있습니다.

각 카드의 기존 기능 버튼은 가족 콜백 확인 카드, 가족 안전 계획, 안전 흐름 길잡이, 계정·기기
잠금 체크리스트, 증거 보존 체크리스트, 신고·상담 연락처, 새 분석 또는 신고용 상담 준비 카드로
이동합니다. 공식 연락처·증거 보존·계정 보호 안내를 이 화면에 복제하지 않으며, 메시지 팩을 열거나
버튼을 누르는 것만으로 사건 기록을 추가·수정·삭제하지 않습니다. 템플릿 선택, 공유 이력,
체크 상태도 저장하지 않는 임시 화면입니다.

템플릿별 공유와 추천 메시지 묶음 공유는 사용자가 버튼을 눌렀을 때만 Android `ACTION_SEND`의
`text/plain` 공유 화면을 엽니다. 앱은 연락처를 읽거나 받는 사람을 고르지 않고 메시지를 자동으로
보내지 않으며, 사용자가 공유 앱과 대상을 직접 선택합니다. 공유문에는 붙여넣은 원문,
개인정보 가림 단서, 안전 질문·콜백 대사·연락처 이름과 번호, 전화·계좌·비밀번호·OTP/PIN·링크·
도메인·신분정보 값을 넣지 않습니다. 모든 추천은 기기 안에서 수행되며 네트워크 호출이나 새
Android 권한을 사용하지 않습니다. 계정 복구, 사기 차단, 피해금 회수·환급, 법적 결과나 안전을
보장하지 않습니다.

## Privacy Center (개인정보 센터)

홈의 "개인정보 센터"에서 현재 기기에 어떤 앱 데이터가 있는지 값 자체를 노출하지 않고 확인할
수 있습니다. `PrivacyCenterBuilder`는 저장된 `SafetyPlan`, 사건 기록 개수, 시작 동의 여부와
고정된 매니페스트 사실만 입력으로 사용하며 Android UI 객체나 현재 분석 원문을 받지 않습니다.
화면의 로컬 데이터 목록은 다음 범위를 구분해 보여줍니다:

- `family_shield` `SharedPreferences`에 저장된 시작 동의 여부 한 항목
- `safety_plan` `SharedPreferences`에 저장된 안전단어, 콜백 스크립트, 사용자가 직접 입력한 신뢰 연락처와 값 없는 가족 준비도·연락처 개수
- `incident_log` `SharedPreferences`에 현재 버전이 저장하는 사건 개수와 위험도·탐지 신호·점수·저장 시각·개인정보를 가린 짧은 미리보기 필드 (이전 버전 미리보기는 사건 기록 화면에서 불러올 때 다시 가림)
- 사건 기록에 붙여넣은 전체 원문을 보관하지 않는다는 상태
- 붙여넣은 입력의 현재 분석, 긴급 멈춤 모드 버튼 선택·공유 이력·체크 상태, 안전 흐름 선택, 가족 콜백/신고용 상담 준비 카드, 체크리스트, 가족 경고 메시지 템플릿 선택·공유 이력, 가족 안전 연습·3분 훈련 결과와 드릴·샘플 선택을 앱의 영구 저장소에 저장하지 않는다는 상태

권한 목록에는 `READ_SMS`, `CALL_PHONE`, `READ_CALL_LOG`, `READ_CONTACTS`, `RECORD_AUDIO`,
`INTERNET` 권한이 없음을 각각 표시합니다. 전화 버튼은 기존과 같이 `ACTION_DIAL`로 전화 앱만
열고 사용자가 직접 통화를 시작합니다. `android:allowBackup="false"`도 함께 표시하며, 이 기능을
위해 매니페스트 권한을 추가하지 않습니다. 이 백업 설정은 클라우드 백업·복원을 허용하지 않지만,
Android 12 이상에서는 제조사에 따라 기기 간 데이터 전송을 끌 수 없는 경우가 있으므로 새 기기의
전송 설정도 함께 확인하도록 안내합니다.

데이터 정리는 하나의 전체 삭제가 아니라 명시적으로 분리된 버튼으로만 진행됩니다. "사건 기록만
삭제"는 안전 계획과 동의 기록을 남기고, "가족 안전 계획만 삭제"는 사건 기록과 동의 기록을
남깁니다. "시작 동의 기록 초기화 (온보딩 다시 보기)"는 동의 플래그만 지운 뒤 즉시 온보딩을
다시 표시하며 사건 기록과 안전 계획은 그대로 둡니다. 개인정보 센터에 들어오는 것만으로 어떤
데이터도 자동 삭제하지 않습니다.

"가족에게 개인정보 요약 공유"는 Android `ACTION_SEND`의 `text/plain`만 사용합니다. 공유문에는
동의/안전 계획 유무, 준비도, 번호가 있는 연락처 수, 사건 개수, 권한 상태 같은 집계 정보만 들어가며
안전단어, 콜백 스크립트, 연락처 이름·전체 번호, 사건 미리보기, 붙여넣은 원문, 계좌·비밀번호·
OTP/PIN·링크·신분정보 값은 포함하지 않습니다. 사용자가 공유 앱을 골라 실제로 보낸 뒤에는 해당
앱의 개인정보 처리 방침이 적용됩니다.

## Family Safety Plan

홈 화면의 "가족 안전 계획 설정"에서 아래 내용을 미리 저장해둘 수 있습니다:

- 안전 확인 질문/단어 (예: 우리 가족만 아는 어릴 적 별명은?)
- 가족이 직접 입력한 신뢰 연락처 (연락처 앱 접근 없이 "이름 - 번호" 형식으로 수동 입력)
- 위급 상황에서 읽을 콜백 스크립트

저장된 계획은 분석 결과 화면에 자동으로 표시되어, 위험 신호가 있을 때 안전단어를 묻고
저장된 번호로 바로 전화(`ACTION_DIAL`, 통화 권한 요청 없음)할 수 있도록 안내합니다.
계획이 없으면 결과 화면에 계획 생성 CTA가 표시됩니다. 모든 데이터는
`SharedPreferences`에만 저장되며 기기 밖으로 전송되지 않습니다.

## Family Callback Verification Card (가족 콜백 확인 카드)

홈의 "가족 콜백 루틴 확인" 또는 AI 분석 결과의 "가족 콜백 확인 카드"에서 여는
임시 실행 카드입니다. `FamilyCallbackCardBuilder`가 현재 `ScamAnalysis?`와 사용자가 직접
저장한 `SafetyPlan`만 입력으로 받아 위험 요약, 가족 준비도, 안전 확인 질문, 순서가 있는 콜백
단계, 확인 전 금지 행동을 결정적으로 만듭니다. 홈에서 열면 이전 분석을 재사용하지 않는 안전한
빈 상태와 분석 시작 CTA를 보여주며, 계획이 부족하면 가족 안전 계획 설정을 먼저 안내합니다.

분석이 있는 카드의 화면 단서는 반드시 `IncidentPreviewRedactor.buildPrivacySafeClue`를 거쳐
전화번호, 계좌형 긴 숫자, 인증번호(OTP/PIN), 링크·도메인, 신분정보를 가립니다. 이 카드는
`SharedPreferences`나 다른 저장소에 저장하지 않고 사건 기록을 자동으로 추가하거나 수정하지도
않습니다. 가족 준비 상태는 별도 계산을 만들지 않고 기존 `FamilyReadinessBuilder` 결과를 그대로
사용합니다.

전화 버튼은 연락처 앱에서 읽어온 번호가 아니라 가족 안전 계획에 사용자가 수동으로 입력한
번호가 있는 신뢰 연락처만 표시합니다. Android `ACTION_DIAL`로 전화 앱만 열고 통화를 자동으로
시작하지 않으며 연락처나 통화 권한을 요청하지 않습니다. 공유 버튼은 Android `ACTION_SEND`의
`text/plain`만 사용합니다. 공유문은 붙여넣은 원문과 화면의 가림 단서, 저장된 안전 질문·콜백
스크립트·연락처 라벨, 신뢰 연락처 전체 번호, 계좌·비밀번호·OTP/PIN 값, 링크·신분정보를
포함하지 않고 "앱에 저장된 가족 안전 질문과 연락처를 사용하세요"라는 일반 안내만 전달합니다.
모든 카드 생성은 기기 안에서 수행되며 네트워크 업로드나 새 Android 권한을 사용하지 않습니다.

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
원격 연결 차단 후 앱 정보 보존·제거와 안전한 기기에서 비밀번호 변경, 증거 보관 방법 등)와
112(경찰)/1394(보이스피싱 통합신고)/1332(금융감독원) 연락처를 보여줍니다. 연락처는 기존
안전 계획과 같은 방식으로
`ACTION_DIAL`만 사용하며, `CALL_PHONE` 권한을 요청하지 않습니다.

"가족에게 대응 요약 공유" 버튼은 `IncidentResponseReportBuilder`가 만든 요약을 공유합니다. 이
요약에는 선택한 상황과 대응 단계, 연락처만 포함되며, 사용자가 붙여넣은 원문 텍스트는 애초에 이
화면에 입력되지 않으므로 요약에도 포함되지 않습니다.

## Official Help Directory (신고·상담 연락처)

홈, AI 분석 결과, 긴급 대응 센터, 증거 보존 체크리스트에서 열 수 있는 로컬 안내 화면입니다.
`OfficialHelpDirectoryBuilder`가 다음 항목을 고정된 순서로 결정적으로 제공합니다:

- 112 경찰 긴급 신고
- 1394 보이스피싱 통합신고·상담
- 1332 금융감독원
- 은행·카드사 공식 대표번호 확인 안내

각 항목에는 언제 사용할지, 상담원에게 말할 내용, 원문·OTP·PIN 등을 가족 채팅에 공유하지 않기
위한 안전 메모가 들어 있습니다. 112, 1394, 1332 버튼은 Android `ACTION_DIAL`로 전화 앱만
열고 사용자가 직접 통화를 시작합니다. `CALL_PHONE` 권한은 요청하지 않습니다. 은행·카드사
항목에는 임의 번호나 전화 버튼을 넣지 않고, 공식 앱·카드 뒷면·주소를 직접 입력한 공식
웹사이트에서 대표번호를 확인하도록 안내합니다.

연락처와 스크립트는 앱에 정적으로 내장된 로컬 안내이므로 인터넷이나 네트워크 조회를 사용하지
않습니다. "가족에게 신고 연락처 공유"는 Android `ACTION_SEND`의 `text/plain`만 사용하며,
공식 번호와 말할 내용 체크리스트만 포함하고 사용자가 붙여넣은 원문 텍스트나 실제 계좌 전체
번호, 비밀번호, OTP, 상품권 PIN은 포함하지 않습니다.

## Official Report Prep Card (신고용 상담 준비 카드)

홈이나 AI 분석 결과의 "신고용 상담 준비 카드"에서 열 수 있는 현재 분석 전용 임시 화면입니다.
분석 결과가 없으면 먼저 의심 내용을 직접 붙여넣어 분석하라는 안전한 빈 상태를 보여줍니다.
`OfficialReportPrepBuilder`는 `ScamAnalysis`만 입력으로 받아 위험도와 점수, 탐지 신호, 전화 전
확인 항목, 상담원에게 말할 내용을 결정적으로 정리합니다.

화면의 짧은 단서는 반드시 `IncidentPreviewRedactor`를 거쳐 전화번호, 계좌형 긴 숫자,
인증번호(OTP), 링크·도메인, 신분정보를 가립니다. 가족 공유 요약에는 이 가림 단서조차 넣지
않고 위험도, 신호 이름, 추천 공식 연락처, 상담 대사와 전화 전 확인 항목만 포함합니다. 가림 뒤에도
짧은 입력 전체가 남는 경우에는 단서 대신 원문 비공개 문구를 표시하므로, 붙여넣은 전체 원문을
화면의 카드나 공유문에 그대로 넣지 않습니다.

추천 연락처는 `OfficialHelpDirectoryBuilder`의 기존 항목을 그대로 재사용합니다. 고위험이거나
송금·계좌/OTP·원격제어·안전계좌 위험 신호가 있으면 112, 1394, 1332와 은행·카드사 공식
대표번호 확인 안내를 함께 보여줍니다. 링크·스미싱이나 더 낮은 위험에서도 1394 상담과 가족
콜백을 먼저 안내하며, 금융·카드 위험이 가능한 경우에는 은행·카드사 공식 번호 확인 안내를
덧붙입니다. 번호가 있는 공식 항목만 `ACTION_DIAL`로 전화 앱을 열고, 사용자가 직접 통화를
시작합니다.

"가족에게 상담 준비 카드 공유"는 Android `ACTION_SEND`의 `text/plain`만 사용합니다. 이
카드와 선택 상태는 `SharedPreferences`에 저장하지 않으며 사건 기록을 자동으로 추가하거나
수정하지도 않습니다. 모든 정리는 기기 안에서 수행되며, 상담 준비를 위한 참고 안내일 뿐
법률·금융 자문이나 피해금 회수·환급 약속이 아닙니다.

## Evidence Preservation Kit (증거 보존 체크리스트)

홈 화면의 "증거 보존 체크리스트" 또는 긴급 대응 센터의 상황별 CTA로 들어가는 수동 안내
화면입니다. 아직 상황을 잘 모르겠는 일반 모드와 긴급 대응 센터의 네 가지 상황(송금·상품권·코인
전달, 원격제어·화면공유 허용, 계정·인증정보 공유, 의심 링크 클릭)을 모두 제공합니다. 긴급 대응
센터에서 상황을 고른 뒤 열면 같은 상황의 체크리스트가 먼저 표시됩니다.

`EvidencePreservationKitBuilder`는 선택한 상황만으로 다음 내용을 결정적으로 만듭니다:

- 거래·수취 정보, 발신번호, 메시지·통화 시각 등 직접 남길 화면의 순서
- 휴대전화 전원과 원문 기록을 유지하고 성급한 삭제·초기화를 피하기 위한 주의사항
- 원격 연결을 먼저 끊고 다른 안전한 기기에서 계정을 보호하는 순서
- 은행·경찰 상담 시각과 지급정지·신고 접수번호를 정리하는 메모
- 붙여넣은 원문과 실제 계좌·비밀번호·OTP·상품권 PIN을 제외한 가족 공유 요약

이 키트는 앱이 증거를 자동으로 캡처, 수집 또는 업로드하는 기능이 아니라 기기 안에 내장된
체크리스트 안내입니다. 상황 선택이나 체크 완료 상태를 저장하지 않으며, 법적 효력을 보장하지
않습니다. 가족 공유는 Android `ACTION_SEND`의 `text/plain`을 사용하고 새 권한을 요청하지
않습니다.

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

## Scam Sample Library (샘플로 AI 분석 연습)

홈 화면의 "샘플로 AI 분석 연습" 버튼으로 들어가는 연습 화면입니다. 실제 사기 문자가 없어도
분석 흐름을 안전하게 연습할 수 있도록, `ScamSampleLibrary`가 결정적으로 생성하는 다섯 가지
연습용 가상 메시지를 제공합니다:

- 가족 번호 변경 + 급한 송금 요청
- 검찰/경찰 안전계좌 압박
- 원격제어 앱·화면공유 압박
- 택배/청첩장 스미싱 링크 (실제 링크는 포함하지 않음)
- 해외/소액 결제 승인 미끼

모든 샘플 텍스트는 `[연습용 가상 메시지]`로 시작하는 허구의 문장이며, 각 샘플에는 왜
의심스러운지 설명하는 학습 노트가 함께 표시됩니다. "이 샘플로 AI 보호자 분석하기" 버튼을
누르면 기존 검사/결과 화면과 동일한 로컬 분석 흐름으로 위험도와 신호, 대응 루틴을 보여줍니다.

샘플은 앱에 내장된 로컬 데이터만 사용하며 네트워크나 새 권한을 쓰지 않습니다. 어떤 샘플을
골랐는지 저장하지 않고, 결과 화면에서 사용자가 "사건 기록에 저장"을 직접 누르기 전에는
사건 기록에도 남지 않습니다. 단위 테스트(`ScamSampleLibraryTest`)가 모든 샘플이 허구로
표시되어 있고 분석기가 기대한 위험도/신호를 내는지 검증합니다.

## Incident Log (사건 기록)

사용자가 분석 결과에서 직접 저장을 선택한 경우에만 최근 30개 사건을 기기의
`SharedPreferences`에 보관합니다. 각 기록에는 위험도, 탐지 신호, 점수, 저장 시각과 짧은
개인정보 가림 미리보기만 들어갑니다. 전화번호, 긴 계좌형 숫자, 인증번호(OTP), 링크·도메인,
주민등록번호·여권번호 같은 신분정보 패턴은 저장 전에 가림 처리하며, 붙여넣은 전체 원문은 사건
기록에 보관하지 않습니다. 이전 버전에서 저장된 미리보기도 불러올 때 다시 가림 처리합니다.

## Incident Timeline Briefing (사건 타임라인 요약)

홈의 "사건 타임라인 요약" 또는 사건 기록 화면의 같은 이름 버튼에서, 사용자가 직접 저장한 최근
사건 기록을 최신순 타임라인과 집계로 확인할 수 있습니다. `IncidentTimelineBriefingBuilder`는
`IncidentStore.load()`가 돌려준 개인정보 가림 기록만 입력으로 받아 전체 건수, 위험도별 건수,
가장 높은 위험도, 최근 사건 시각, 여러 기록에서 많이 나타난 탐지 신호와 사건별 다음 행동을
결정적으로 정리합니다. 음수 점수와 비어 있거나 잘못된 위험도·신호·미리보기는 안전한 표시값으로
정규화하며, 사건 순서가 섞여 있어도 최신 기록부터 보여줍니다.

요약 화면은 저장된 사건을 읽을 뿐 새 분석이나 사건 기록을 만들지 않으며, 화면 선택과 요약을
새 저장소에 남기지 않습니다. 사건 기록을 지우거나 바꾸는 버튼도 두지 않고 안전 흐름 길잡이,
긴급 대응 센터, 증거 보존 체크리스트, 신고·상담 연락처와 현재 분석의 상담 준비 카드(현재 분석이
없으면 새 검사)로 연결합니다. 저장된 기록은 실제 피해 행동을 증명하지 않으므로 사용자가 본인
상황에 해당하는 경로를 직접 고릅니다.

"가족에게 사건 요약 공유"는 사용자가 직접 누를 때 Android `ACTION_SEND`의 `text/plain`만
사용합니다. 공유문은 위험도별 건수, 가장 높은 위험도, 허용된 탐지 신호 분류와 앱 안의 다음 행동
이름처럼 집계에 필요한 안전한 값만 새로 구성합니다. 사건별 개인정보 가림 미리보기와 표시 시각,
붙여넣은 원문, 전화번호·계좌번호·비밀번호·OTP/PIN·링크·도메인·신분정보, 가족 안전단어·콜백
질문·연락처 이름과 번호는 넣지 않습니다. 이 기능은 로컬에서만 동작하고 네트워크 호출, 자동
문자·통화·연락처 읽기, 새 Android 권한을 추가하지 않으며 공식 도움은 결과 보장이 아닌 경로
안내로만 제공합니다.

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
- Quick Stop Mode is deterministic, local-only, stateless manual first-response guidance; it reads no contacts, auto-sends nothing, stores no button/check/share state, never mutates incident logs automatically, shares only its builder-produced generic route summary, and adds no Android permissions.
- The 24-Hour Recovery Plan is deterministic, local-only, stateless manual time-window guidance (지금~15분/1시간 안/오늘 안/이번 주); it adds no permissions, reads no contacts, auto-sends nothing, stores no checklist/progress state, never mutates incident logs unless the user explicitly uses existing save/delete screens, and its user-triggered text share contains only window/action labels excluding pasted text, redacted clues, safety-plan values, and phone/account/OTP/PIN/URL/domain/resident-number-like values.
- The Safety Flow Router deterministically maps the current local analysis and family readiness to existing app flows; route and recommendation selections are not stored, opening it never mutates incident logs automatically, and its text share excludes pasted text and safety-plan secrets without adding network use or Android permissions.
- The Account & Device Lockdown Checklist is deterministic, local-only, stateless manual guidance; it does not change device or account settings, mutate incident logs, or add permissions, and its user-triggered text share contains only static section/action labels rather than analysis or safety-plan values.
- The Family Alert Message Pack is deterministic, local-only, and stateless; it uses analysis signals and secret-free readiness only, reads no contacts, never auto-sends, stores no template selection or share history, never mutates incident logs, and shares only builder-produced privacy-safe text without adding network use or Android permissions.
- The Incident Timeline Briefing deterministically summarizes saved privacy-redacted incident fields without creating new stored data; its user-triggered text share contains aggregate risk/signal/action labels only and excludes every per-incident clue, display time, pasted text, and safety-plan value without adding network use or Android permissions.
- The visible Privacy Center inventories consent, safety-plan readiness/contact count, incident count/retained fields, transient non-stored screens, and every intentionally absent permission without exposing saved values.
- Privacy Center clearing is explicit and scoped: incident logs, the family safety plan, and onboarding consent are controlled independently; opening the screen never deletes data automatically.
- The Privacy Center family explanation uses user-triggered `ACTION_SEND` `text/plain` with aggregate status only and excludes safety-plan values, incident previews, and pasted raw text.
- The Official Help Directory uses static on-device guidance and performs no network lookup.
- The Official Report Prep Card is derived only from the current local analysis, redacts its on-screen clue, omits all pasted-text excerpts from sharing, is not stored, and never mutates incident logs automatically.
- The Family Callback Verification Card is derived transiently from the current local analysis and manually saved safety plan, uses a privacy-redacted on-screen clue, is not stored, never mutates incident logs, and shares no raw/clue/safe-question/callback-script/contact-number secrets.
- No automatic SMS/call monitoring.
- No automatic evidence capture or upload; the evidence kit is static local guidance and stores no selection or completion state.
- No Android permissions are requested for calls, SMS, contacts, microphone, or internet.
- Android backup is disabled (`android:allowBackup="false"`) so local incident/safety-plan data is not included in platform cloud backup.
- The user must paste suspicious text manually.
- Incident logs keep only risk level, detected signals, score, timestamp, and a redacted preview in local `SharedPreferences`; the full pasted text is not retained in the incident log.
- Rule-based scoring is a safety checklist, not a guarantee. Always call family through an already-saved trusted number before sending money, installing apps, or sharing authentication codes.

## Package

Android package/application id:

```text
com.mongmong.familyshield
```
