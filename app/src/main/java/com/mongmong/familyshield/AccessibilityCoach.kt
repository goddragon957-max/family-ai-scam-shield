package com.mongmong.familyshield

enum class AccessibilityCoachCardId(val displayOrder: Int) {
    SYSTEM_FONT_SIZE(1),
    DISPLAY_SIZE(2),
    BOLD_HIGH_CONTRAST_TEXT(3),
    MAGNIFICATION(4),
    SELECT_TO_SPEAK_READ_ALOUD(5),
    READ_ALOUD_SHORTCUT(6),
    FAMILY_SAFE_WORD_CALLBACK_BIG_TEXT(7),
    EMERGENCY_REPORT_NUMBERS(8),
    CURRENT_ANALYSIS_BIG_TEXT(9),
    SIMPLE_MODE_TOGGLE_HINT(10)
}

enum class AccessibilityCoachActionId {
    ONBOARDING,
    PRIVACY_CENTER,
    FAMILY_READINESS_ROADMAP,
    ANALYSIS_RESULT,
    EVIDENCE_KIT,
    SAFETY_FLOW_ROUTER,
    SAFETY_PLAN,
    FAMILY_CALLBACK_CARD,
    OFFICIAL_HELP_DIRECTORY,
    CHECKER,
    HOME
}

data class AccessibilityCoachAction(
    val id: AccessibilityCoachActionId,
    val label: String
)

data class AccessibilityCoachCard(
    val id: AccessibilityCoachCardId,
    val displayOrder: Int,
    val title: String,
    val whyItMatters: String,
    val inAppSteps: List<String>,
    val systemSettingSteps: List<String>,
    val primaryAction: AccessibilityCoachAction,
    val secondaryActions: List<AccessibilityCoachAction>
) {
    val routeActions: List<AccessibilityCoachAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class AccessibilityCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<AccessibilityCoachCardId>,
    val recommendedActions: List<AccessibilityCoachAction>,
    val cards: List<AccessibilityCoachCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<AccessibilityCoachCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<AccessibilityCoachActionId>
        get() = recommendedActions.map { action -> action.id }
}

object AccessibilityCoachBuilder {
    fun build(
        consent: Boolean,
        readiness: FamilyReadiness,
        incidentCount: Int = 0
    ): AccessibilityCoach {
        val readinessLevel = readiness.level
        val hasIncident = incidentCount.coerceAtLeast(0) > 0

        val recommendedCardSet = linkedSetOf(
            AccessibilityCoachCardId.SYSTEM_FONT_SIZE,
            AccessibilityCoachCardId.BOLD_HIGH_CONTRAST_TEXT
        )
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS ->
                recommendedCardSet += AccessibilityCoachCardId.FAMILY_SAFE_WORD_CALLBACK_BIG_TEXT
            ReadinessLevel.READY -> {
                if (consent) {
                    recommendedCardSet += AccessibilityCoachCardId.SIMPLE_MODE_TOGGLE_HINT
                }
            }
        }
        if (hasIncident) {
            recommendedCardSet += AccessibilityCoachCardId.CURRENT_ANALYSIS_BIG_TEXT
            recommendedCardSet += AccessibilityCoachCardId.EMERGENCY_REPORT_NUMBERS
        }
        val recommendedCardIds = AccessibilityCoachCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val recommendedActionSet = linkedSetOf<AccessibilityCoachActionId>()
        if (!consent) {
            recommendedActionSet += AccessibilityCoachActionId.ONBOARDING
        }
        recommendedActionSet += AccessibilityCoachActionId.PRIVACY_CENTER
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS ->
                recommendedActionSet += AccessibilityCoachActionId.FAMILY_READINESS_ROADMAP
            ReadinessLevel.READY -> {}
        }
        if (hasIncident) {
            recommendedActionSet += AccessibilityCoachActionId.ANALYSIS_RESULT
            recommendedActionSet += AccessibilityCoachActionId.EVIDENCE_KIT
        }
        recommendedActionSet += AccessibilityCoachActionId.HOME
        val recommendedActions = AccessibilityCoachActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val recommendationTitle = when {
            !consent -> "먼저 시작 동의부터 마치고 큰 글씨·읽어주기 안내를 이어서 보기"
            hasIncident ->
                "저장된 사건이 있다는 개수 범주만으로 분석 결과와 긴급 신고 번호를 큰 글씨로 다시 보기"
            readinessLevel == ReadinessLevel.LOW || readinessLevel == ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 아직 진행 중인 범주라 안전단어·콜백을 다시 볼 화면 위치부터 안내"
            readinessLevel == ReadinessLevel.READY && consent ->
                "동의와 가족 준비가 모두 되어 있다는 범주에서 단순 모드와 개인정보 센터를 함께 안내"
            else -> "시스템 글자 크기와 고대비 문구부터 순서대로 안내"
        }

        val cards = buildCards()

        return AccessibilityCoach(
            title = "큰글씨·읽어주기 접근성 코치",
            intro =
                "글자를 키우거나 화면을 소리 내어 읽어 주는 기능은 이 앱이 대신 켜지 않고, " +
                    "안드로이드 시스템 설정에서 직접 켜는 방법과 이 앱 안에서 바로 할 수 있는 " +
                    "일을 구분해 안내하는 로컬 전용·무상태 수동 코치입니다.",
            modeLabel = "시스템 글자 크기 자동 변경 없음 · 읽어주기 자동 실행 없음 · 접근성 서비스 자동 켜기 없음",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                consent = consent,
                readinessLevel = readinessLevel,
                hasIncident = hasIncident
            ),
            recommendedCardIds = recommendedCardIds,
            recommendedActions = recommendedActions,
            cards = cards,
            privacySafetyNotes = privacySafetyNotes(),
            familyShareSummary = buildFamilyShareSummary(
                recommendedCardIds = recommendedCardIds,
                recommendedActions = recommendedActions,
                cards = cards
            )
        )
    }

    private fun buildCards(): List<AccessibilityCoachCard> {
        fun action(id: AccessibilityCoachActionId): AccessibilityCoachAction = actionFor(id)

        return listOf(
            AccessibilityCoachCard(
                id = AccessibilityCoachCardId.SYSTEM_FONT_SIZE,
                displayOrder = AccessibilityCoachCardId.SYSTEM_FONT_SIZE.displayOrder,
                title = "시스템 글자 크기 키우기",
                whyItMatters =
                    "작은 글자는 사기 문자와 실제 안내를 구분하기 더 어렵게 만듭니다. 안드로이드 " +
                        "시스템 글자 크기를 키우면 이 앱을 포함한 모든 앱의 글자가 함께 커집니다.",
                inAppSteps = listOf(
                    "이 앱의 모든 화면은 시스템 글자 크기 설정을 그대로 따르므로 시스템에서 키우면 " +
                        "이 앱의 글자도 함께 커집니다.",
                    "단순 모드를 켜면 홈 화면 버튼 글자를 22sp 이상 고정 크기로 보여 줍니다."
                ),
                systemSettingSteps = listOf(
                    "설정 앱을 엽니다.",
                    "디스플레이(화면) 메뉴로 들어갑니다.",
                    "글자 크기(글꼴 크기) 메뉴에서 슬라이더를 오른쪽으로 밀어 크게 만듭니다.",
                    "설정 앱을 나와 이 화면으로 돌아와 글자가 커졌는지 직접 확인합니다."
                ),
                primaryAction = action(AccessibilityCoachActionId.PRIVACY_CENTER),
                secondaryActions = listOf(action(AccessibilityCoachActionId.HOME))
            ),
            AccessibilityCoachCard(
                id = AccessibilityCoachCardId.DISPLAY_SIZE,
                displayOrder = AccessibilityCoachCardId.DISPLAY_SIZE.displayOrder,
                title = "화면 크기도 키우기",
                whyItMatters =
                    "글자 크기와 별개로 화면 표시 크기(디스플레이 크기)를 키우면 버튼과 아이콘도 " +
                        "함께 커져 손가락으로 누르기 쉬워집니다.",
                inAppSteps = listOf(
                    "이 앱의 버튼과 카드 크기는 시스템 디스플레이 크기 설정을 그대로 따릅니다."
                ),
                systemSettingSteps = listOf(
                    "설정 앱을 엽니다.",
                    "디스플레이(화면) 메뉴로 들어갑니다.",
                    "디스플레이 크기 메뉴에서 슬라이더를 오른쪽으로 밀어 항목과 버튼을 크게 만듭니다."
                ),
                primaryAction = action(AccessibilityCoachActionId.PRIVACY_CENTER),
                secondaryActions = listOf(action(AccessibilityCoachActionId.HOME))
            ),
            AccessibilityCoachCard(
                id = AccessibilityCoachCardId.BOLD_HIGH_CONTRAST_TEXT,
                displayOrder = AccessibilityCoachCardId.BOLD_HIGH_CONTRAST_TEXT.displayOrder,
                title = "굵은 글자 / 고대비 텍스트 켜기",
                whyItMatters =
                    "굵은 글자와 고대비 텍스트는 흐릿한 조명이나 눈이 쉽게 피로해지는 상황에서도 " +
                        "글자와 배경을 뚜렷하게 구분해 줍니다.",
                inAppSteps = listOf(
                    "이 앱의 모든 화면은 시스템 굵은 텍스트·고대비 텍스트 설정을 그대로 반영합니다."
                ),
                systemSettingSteps = listOf(
                    "설정 앱을 엽니다.",
                    "접근성 메뉴로 들어갑니다.",
                    "표시 크기 및 텍스트 메뉴에서 굵은 텍스트를 켭니다.",
                    "같은 메뉴에서 고대비 텍스트 옵션이 있으면 함께 켭니다."
                ),
                primaryAction = action(AccessibilityCoachActionId.PRIVACY_CENTER),
                secondaryActions = listOf(action(AccessibilityCoachActionId.HOME))
            ),
            AccessibilityCoachCard(
                id = AccessibilityCoachCardId.MAGNIFICATION,
                displayOrder = AccessibilityCoachCardId.MAGNIFICATION.displayOrder,
                title = "확대 / 돋보기",
                whyItMatters =
                    "화면 일부를 순간적으로 확대해서 보고 싶을 때, 시스템 돋보기 기능을 손가락으로 " +
                        "직접 켜고 끌 수 있습니다.",
                inAppSteps = listOf(
                    "이 앱은 자체 확대 기능을 만들지 않고 시스템 화면 확대 기능만 안내합니다."
                ),
                systemSettingSteps = listOf(
                    "설정 앱을 엽니다.",
                    "접근성 메뉴로 들어가 확대 항목을 찾습니다.",
                    "화면 세 번 두드리기 또는 접근성 버튼 중 원하는 확대 방법을 선택해 켭니다.",
                    "확대한 뒤 손가락으로 화면을 밀어 보고 싶은 부분으로 이동합니다."
                ),
                primaryAction = action(AccessibilityCoachActionId.PRIVACY_CENTER),
                secondaryActions = listOf(action(AccessibilityCoachActionId.HOME))
            ),
            AccessibilityCoachCard(
                id = AccessibilityCoachCardId.SELECT_TO_SPEAK_READ_ALOUD,
                displayOrder = AccessibilityCoachCardId.SELECT_TO_SPEAK_READ_ALOUD.displayOrder,
                title = "글자 읽어주기",
                whyItMatters =
                    "화면의 글자를 직접 읽기 어려울 때, 시스템이 짚은 부분을 소리 내어 읽어 주는 " +
                        "기능을 쓰면 중요한 내용을 놓치지 않을 수 있습니다.",
                inAppSteps = listOf(
                    "이 앱은 자체 음성 엔진이나 TTS를 실행하지 않고 시스템의 읽어주기 기능만 안내합니다."
                ),
                systemSettingSteps = listOf(
                    "설정 앱을 엽니다.",
                    "접근성 메뉴에서 텍스트 음성 변환 출력 항목을 열어 음성 엔진이 설치돼 있는지 확인합니다.",
                    "접근성 메뉴에서 선택하여 말하기(Select to Speak)를 켭니다.",
                    "화면에서 읽고 싶은 글자를 손가락으로 짚으면 시스템이 소리 내어 읽어 줍니다.",
                    "필요하면 같은 메뉴에서 TalkBack 전체 읽기 기능도 함께 살펴봅니다."
                ),
                primaryAction = action(AccessibilityCoachActionId.PRIVACY_CENTER),
                secondaryActions = listOf(action(AccessibilityCoachActionId.HOME))
            ),
            AccessibilityCoachCard(
                id = AccessibilityCoachCardId.READ_ALOUD_SHORTCUT,
                displayOrder = AccessibilityCoachCardId.READ_ALOUD_SHORTCUT.displayOrder,
                title = "흔들어서 읽기",
                whyItMatters =
                    "매번 설정 메뉴를 찾아 들어가지 않아도 되도록, 기기에 있는 접근성 단축키(볼륨 키 " +
                        "길게 누르기, 흔들기 등 제조사별 방법)를 미리 정해 두면 급할 때 더 빠르게 " +
                        "읽어주기 기능을 켤 수 있습니다.",
                inAppSteps = listOf(
                    "이 앱은 흔들기·단축키 동작을 감지하지 않고 시스템 접근성 단축키 설정 방법만 안내합니다."
                ),
                systemSettingSteps = listOf(
                    "설정 앱을 엽니다.",
                    "접근성 메뉴에서 접근성 단축키 항목을 찾습니다.",
                    "선택하여 말하기 또는 TalkBack을 단축키 기능으로 지정합니다.",
                    "기기에 따라 볼륨 키 길게 누르기, 흔들기 등 제조사가 제공하는 방법 중 하나를 고릅니다."
                ),
                primaryAction = action(AccessibilityCoachActionId.PRIVACY_CENTER),
                secondaryActions = listOf(action(AccessibilityCoachActionId.HOME))
            ),
            AccessibilityCoachCard(
                id = AccessibilityCoachCardId.FAMILY_SAFE_WORD_CALLBACK_BIG_TEXT,
                displayOrder =
                    AccessibilityCoachCardId.FAMILY_SAFE_WORD_CALLBACK_BIG_TEXT.displayOrder,
                title = "가족 안전단어 / 콜백 스크립트",
                whyItMatters =
                    "급하고 당황스러운 순간에는 저장해 둔 안전단어와 콜백 순서를 큰 글씨로 다시 " +
                        "확인할 화면이 어디에 있는지 아는 것이 중요합니다.",
                inAppSteps = listOf(
                    "가족 안전 계획 화면과 가족 콜백 카드 화면에서 저장한 안전단어·콜백 문장을 직접 " +
                        "열어 큰 글씨로 확인할 수 있습니다.",
                    "이 카드는 저장된 안전단어나 연락처 값을 읽거나 표시하지 않고 화면 이름으로만 안내합니다."
                ),
                systemSettingSteps = emptyList(),
                primaryAction = action(AccessibilityCoachActionId.SAFETY_PLAN),
                secondaryActions = listOf(
                    action(AccessibilityCoachActionId.FAMILY_CALLBACK_CARD),
                    action(AccessibilityCoachActionId.FAMILY_READINESS_ROADMAP)
                )
            ),
            AccessibilityCoachCard(
                id = AccessibilityCoachCardId.EMERGENCY_REPORT_NUMBERS,
                displayOrder = AccessibilityCoachCardId.EMERGENCY_REPORT_NUMBERS.displayOrder,
                title = "긴급 신고 번호",
                whyItMatters =
                    "긴급 상황에서는 번호를 찾아 헤매지 않고 바로 볼 수 있는 곳이 필요합니다. 이 " +
                        "카드는 공식 신고·상담 번호를 큰 글씨로 보여 주기만 합니다.",
                inAppSteps = listOf(
                    "공식 도움 연락처 화면에서 112(경찰), 1394(보이스피싱 상담), 182(경찰 민원) 같은 " +
                        "번호를 직접 눌러 전화 앱으로 열 수 있습니다.",
                    "이 카드는 번호로 자동 발신하지 않고 전화 앱을 여는 기존 화면으로만 안내합니다."
                ),
                systemSettingSteps = emptyList(),
                primaryAction = action(AccessibilityCoachActionId.OFFICIAL_HELP_DIRECTORY),
                secondaryActions = listOf(action(AccessibilityCoachActionId.SAFETY_FLOW_ROUTER))
            ),
            AccessibilityCoachCard(
                id = AccessibilityCoachCardId.CURRENT_ANALYSIS_BIG_TEXT,
                displayOrder = AccessibilityCoachCardId.CURRENT_ANALYSIS_BIG_TEXT.displayOrder,
                title = "지금 분석 결과",
                whyItMatters =
                    "방금 검사한 위험 신호를 다시 큰 글씨로 확인하고 싶을 때 분석 결과 화면으로 바로 " +
                        "이동할 수 있습니다.",
                inAppSteps = listOf(
                    "분석 결과 화면도 시스템 글자 크기 설정을 그대로 따르므로 시스템에서 키우면 결과 " +
                        "화면 글자도 함께 커집니다.",
                    "저장된 사건이 있다면 증거 보존 체크리스트에서 다음에 할 일을 다시 확인할 수 있습니다."
                ),
                systemSettingSteps = emptyList(),
                primaryAction = action(AccessibilityCoachActionId.ANALYSIS_RESULT),
                secondaryActions = listOf(
                    action(AccessibilityCoachActionId.CHECKER),
                    action(AccessibilityCoachActionId.EVIDENCE_KIT)
                )
            ),
            AccessibilityCoachCard(
                id = AccessibilityCoachCardId.SIMPLE_MODE_TOGGLE_HINT,
                displayOrder = AccessibilityCoachCardId.SIMPLE_MODE_TOGGLE_HINT.displayOrder,
                title = "단순 모드",
                whyItMatters =
                    "메뉴가 많아 복잡하게 느껴진다면 단순 모드로 바꿔 큰 버튼 세 개만 보이는 홈 " +
                        "화면으로 줄일 수 있습니다.",
                inAppSteps = listOf(
                    "개인정보 센터에서 단순 모드 켜기/끄기 버튼을 눌러 바로 전환할 수 있습니다.",
                    "단순 모드는 이 기기의 로컬 저장소에 켜짐/꺼짐 값 하나만 저장하며 다른 코치나 " +
                        "저장된 데이터를 바꾸지 않습니다."
                ),
                systemSettingSteps = emptyList(),
                primaryAction = action(AccessibilityCoachActionId.PRIVACY_CENTER),
                secondaryActions = listOf(action(AccessibilityCoachActionId.HOME))
            )
        )
    }

    private fun buildRecommendationExplanation(
        consent: Boolean,
        readinessLevel: ReadinessLevel,
        hasIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (!consent) {
                add(
                    "시작 동의 기록이 없다는 범주에서는 다른 카드보다 온보딩 동의 화면 경로를 " +
                        "먼저 보여 주며, 동의 여부가 글자 크기나 읽어주기 설정을 대신 켰다는 뜻은 " +
                        "아닙니다."
                )
            }
            if (readinessLevel == ReadinessLevel.LOW || readinessLevel == ReadinessLevel.IN_PROGRESS) {
                add(
                    "가족 준비가 아직 진행 중인 범주에서는 안전단어·콜백 스크립트를 큰 글씨로 " +
                        "다시 볼 화면 경로를 더 보여 주며, 저장된 계획의 실제 값은 추천에 사용하지 " +
                        "않습니다."
                )
            }
            if (hasIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만으로 분석 결과와 긴급 신고 번호 카드를 더 " +
                        "보여 주며 사건 내용이나 미리보기를 읽지 않습니다."
                )
            }
            if (readinessLevel == ReadinessLevel.READY && consent) {
                add(
                    "동의와 가족 준비가 모두 되어 있다는 범주에서는 단순 모드 카드와 개인정보 " +
                        "센터 경로를 함께 보여 줍니다."
                )
            }
        }.joinToString(" ")

        val boundaryCopy =
            "이 코치는 어떤 경우에도 시스템 글자 크기·디스플레이 크기·고대비·굵은 텍스트·확대·" +
                "TalkBack·선택하여 말하기 설정을 자동으로 켜거나 바꾸지 않고, 안드로이드 시스템 " +
                "설정 화면으로 이동해 사용자가 직접 켜는 방법만 안내합니다."

        return listOf(situationCopy, boundaryCopy)
            .filter { copy -> copy.isNotBlank() }
            .joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 고정 문장을 사용자가 직접 읽고 기존 앱 화면을 직접 고르는 로컬 전용·무상태 " +
            "수동 안내이며, 새 저장소·자동 처리·자동 전송·스케줄러를 만들지 않습니다.",
        "추천은 시작 동의 여부, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 안전단어·콜백 " +
            "값·연락처·사건 내용·미리보기·저장된 계획의 실제 값을 사용하지 않습니다.",
        "이 코치는 Configuration.fontScale, AccessibilityManager, WindowManager, " +
            "TextToSpeech, TtsSpan 같은 시스템 API를 사용하지 않으므로 글자 크기·읽어주기·" +
            "고대비·확대·TalkBack 설정을 자동으로 켜거나 바꾸지 않습니다.",
        "이 코치는 어떤 화면 글자나 버튼 크기도 자동으로 조정하지 않으며 모든 시스템 설정 " +
            "변경은 사용자가 안드로이드 설정 앱에서 직접 하는 수동 행동입니다.",
        "단순 모드는 기존 family_shield SharedPreferences에 pref_simple_mode 불리언 값 " +
            "하나만 저장하며, 다른 코치의 선택·진행·읽음·완료 상태나 가족 안전 계획·사건 기록을 " +
            "덮어쓰거나 읽지 않습니다.",
        "연락처·통화 기록·문자·SMS·클립보드·알림을 읽거나 감시하지 않고 연락처를 읽거나 쓰거나 " +
            "고르지 않습니다.",
        "전화 앱 열기 이외의 자동 발신·자동 통화·자동 종료·통화 선별·차단을 하지 않으며 " +
            "전화 연결은 항상 Intent.ACTION_DIAL로 전화 앱만 열고 통화는 사용자가 직접 " +
            "시작합니다.",
        "카메라·마이크·파일·사진·미디어·저장소를 읽지 않고 화면 캡처·OCR·이미지 분석·음성 " +
            "인식·녹음을 하지 않습니다.",
        "네트워크·웹 탐색·브라우저·외부 링크 열기를 사용하지 않고 공식 번호·기관·문서 상태를 " +
            "조회·검증하지 않습니다.",
        "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러·브로드캐스트 " +
            "수신기를 만들거나 예약하지 않습니다.",
        "새 Android 권한을 요청하지 않으며 접근성 서비스·TalkBack·Select to Speak를 이 앱이 " +
            "대신 켜거나 끄지 않습니다.",
        "이 코치의 카드 선택·읽음·추천·공유 상태는 저장하지 않고, 시스템 글자 크기·고대비·" +
            "확대·읽어주기 설정이 실제로 켜졌는지 확인·판정·증명하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<AccessibilityCoachCardId>,
        recommendedActions: List<AccessibilityCoachAction>,
        cards: List<AccessibilityCoachCard>
    ): String {
        val cardById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[큰글씨·읽어주기 접근성 코치]")
            appendLine(
                "시스템 글자 크기·읽어주기 설정을 자동으로 켜지 않고 직접 켜는 방법과 이 앱 " +
                    "안에서 바로 되는 일을 나눠 안내하는 로컬 수동 코치입니다."
            )
            appendLine()
            appendLine("추천 접근성 안내 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 안전단어·콜백 값·연락처·사건 내용은 보내지 않습니다.")
            append(
                "- 이 안내는 시스템 글자 크기·읽어주기·고대비·확대 설정을 자동으로 켜거나 " +
                    "바꾸지 않고 결과를 보장하지 않습니다."
            )
        }
    }

    private fun actionFor(id: AccessibilityCoachActionId): AccessibilityCoachAction =
        AccessibilityCoachAction(
            id = id,
            label = when (id) {
                AccessibilityCoachActionId.ONBOARDING -> "시작 동의 화면"
                AccessibilityCoachActionId.PRIVACY_CENTER -> "개인정보 센터"
                AccessibilityCoachActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
                AccessibilityCoachActionId.ANALYSIS_RESULT -> "현재 분석 결과"
                AccessibilityCoachActionId.EVIDENCE_KIT -> "증거 보존 체크"
                AccessibilityCoachActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
                AccessibilityCoachActionId.SAFETY_PLAN -> "가족 안전 계획"
                AccessibilityCoachActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 카드"
                AccessibilityCoachActionId.OFFICIAL_HELP_DIRECTORY -> "공식 도움 연락처"
                AccessibilityCoachActionId.CHECKER -> "의심 문구 검사"
                AccessibilityCoachActionId.HOME -> "홈으로"
            }
        )
}
