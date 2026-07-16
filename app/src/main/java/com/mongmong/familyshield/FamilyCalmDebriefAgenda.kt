package com.mongmong.familyshield

enum class FamilyCalmDebriefCardId(val displayOrder: Int) {
    CALM_WITHOUT_BLAME(1),
    CONFIRM_ACTION_PAUSE(2),
    DESCRIBE_ACTION_CATEGORIES(3),
    CHOOSE_SEPARATE_CALLBACK(4),
    CHOOSE_RESPONSE_ROUTES(5),
    SET_MANUAL_FOLLOW_UP(6)
}

enum class FamilyCalmDebriefCategory(val label: String) {
    EMOTIONAL_CALM("비난 없는 안정"),
    ACTION_PAUSE("추가 행동 멈춤"),
    CATEGORY_ONLY_REVIEW("행동 종류 확인"),
    SEPARATE_VERIFICATION("별도 본인확인"),
    MANUAL_RESPONSE("수동 보호 경로"),
    FAMILY_FOLLOW_UP("가족 후속 약속")
}

enum class FamilyCalmDebriefActionId {
    QUICK_STOP,
    EXPOSURE_IMPACT_TRIAGE,
    FAMILY_CALLBACK_CARD,
    ACCOUNT_LOCKDOWN,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    FOLLOW_UP_CHECK_IN_PLANNER,
    TRUSTED_HELPER_REVIEW,
    FAMILY_ALERT_MESSAGES,
    FAMILY_READINESS_ROADMAP,
    FAMILY_SAFETY_DRILL_COACH,
    FAMILY_RESPONSE_ROLE_BOARD,
    SAFE_CALL_SCRIPTS,
    RECOVERY_PLAN,
    REPORT_PREP,
    INCIDENT_TIMELINE_BRIEFING,
    SAFETY_FLOW_ROUTER,
    PRIVACY_CENTER,
    CHECKER,
    ANALYSIS_RESULT
}

data class FamilyCalmDebriefAction(
    val id: FamilyCalmDebriefActionId,
    val label: String
)

data class FamilyCalmDebriefCard(
    val id: FamilyCalmDebriefCardId,
    val displayOrder: Int,
    val title: String,
    val category: FamilyCalmDebriefCategory,
    val whatToSayOrDo: String,
    val whatNotToAskStoreOrShare: List<String>,
    val primaryAction: FamilyCalmDebriefAction,
    val secondaryActions: List<FamilyCalmDebriefAction>
) {
    val routeActions: List<FamilyCalmDebriefAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class FamilyCalmDebriefAgenda(
    val title: String,
    val intro: String,
    val durationLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<FamilyCalmDebriefCardId>,
    val recommendedActions: List<FamilyCalmDebriefAction>,
    val agendaCards: List<FamilyCalmDebriefCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<FamilyCalmDebriefCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            agendaCards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<FamilyCalmDebriefActionId>
        get() = recommendedActions.map { action -> action.id }

    val cards: List<FamilyCalmDebriefCard>
        get() = agendaCards
}

object FamilyCalmDebriefAgendaBuilder {
    private val financialProtectionSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.BAIT,
        ScamSignal.CREDENTIALS,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val remoteAccessSignals = setOf(
        ScamSignal.APP_INSTALL,
        ScamSignal.LIVE_CALL_CONTROL
    )

    private val linkSignals = setOf(
        ScamSignal.LINK,
        ScamSignal.PARCEL_INVITATION_SMISHING
    )

    private val familyVerificationSignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.PHONE_NUMBER_CHANGE
    )

    private val secrecyImpersonationSignals = setOf(
        ScamSignal.SECRECY,
        ScamSignal.IMPERSONATION
    )

    fun build(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness,
        incidentCount: Int = 0
    ): FamilyCalmDebriefAgenda {
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val hasDefaultState = analysis == null || signals.isEmpty()
        val hasFinancialProtectionSignal = analysis?.riskLevel == RiskLevel.HIGH ||
            signals.any { signal -> signal in financialProtectionSignals }
        val hasRemoteAccessSignal = signals.any { signal -> signal in remoteAccessSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasFamilyVerificationSignal =
            signals.any { signal -> signal in familyVerificationSignals }
        val hasSecrecyImpersonationSignal =
            signals.any { signal -> signal in secrecyImpersonationSignals }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0
        val needsReadinessSupport = readiness.level != ReadinessLevel.READY

        val recommendedCardSet = linkedSetOf(FamilyCalmDebriefCardId.CALM_WITHOUT_BLAME)
        if (hasDefaultState) {
            recommendedCardSet += FamilyCalmDebriefCardId.CONFIRM_ACTION_PAUSE
            recommendedCardSet += FamilyCalmDebriefCardId.DESCRIBE_ACTION_CATEGORIES
            recommendedCardSet += FamilyCalmDebriefCardId.CHOOSE_SEPARATE_CALLBACK
            recommendedCardSet += FamilyCalmDebriefCardId.SET_MANUAL_FOLLOW_UP
        }
        if (hasFinancialProtectionSignal) {
            recommendedCardSet += FamilyCalmDebriefCardId.CONFIRM_ACTION_PAUSE
            recommendedCardSet += FamilyCalmDebriefCardId.DESCRIBE_ACTION_CATEGORIES
            recommendedCardSet += FamilyCalmDebriefCardId.CHOOSE_RESPONSE_ROUTES
            recommendedCardSet += FamilyCalmDebriefCardId.SET_MANUAL_FOLLOW_UP
        }
        if (hasRemoteAccessSignal) {
            recommendedCardSet += FamilyCalmDebriefCardId.CONFIRM_ACTION_PAUSE
            recommendedCardSet += FamilyCalmDebriefCardId.DESCRIBE_ACTION_CATEGORIES
            recommendedCardSet += FamilyCalmDebriefCardId.CHOOSE_RESPONSE_ROUTES
        }
        if (hasLinkSignal) {
            recommendedCardSet += FamilyCalmDebriefCardId.CONFIRM_ACTION_PAUSE
            recommendedCardSet += FamilyCalmDebriefCardId.DESCRIBE_ACTION_CATEGORIES
            recommendedCardSet += FamilyCalmDebriefCardId.CHOOSE_RESPONSE_ROUTES
            recommendedCardSet += FamilyCalmDebriefCardId.SET_MANUAL_FOLLOW_UP
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet += FamilyCalmDebriefCardId.CHOOSE_SEPARATE_CALLBACK
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedCardSet += FamilyCalmDebriefCardId.CONFIRM_ACTION_PAUSE
            recommendedCardSet += FamilyCalmDebriefCardId.DESCRIBE_ACTION_CATEGORIES
            recommendedCardSet += FamilyCalmDebriefCardId.CHOOSE_RESPONSE_ROUTES
        }
        if (hasSavedIncident) {
            recommendedCardSet += FamilyCalmDebriefCardId.CHOOSE_RESPONSE_ROUTES
            recommendedCardSet += FamilyCalmDebriefCardId.SET_MANUAL_FOLLOW_UP
        }
        if (needsReadinessSupport) {
            recommendedCardSet += FamilyCalmDebriefCardId.CHOOSE_SEPARATE_CALLBACK
            recommendedCardSet += FamilyCalmDebriefCardId.SET_MANUAL_FOLLOW_UP
        }
        if (recommendedCardSet.size == 1) {
            recommendedCardSet += FamilyCalmDebriefCardId.CONFIRM_ACTION_PAUSE
            recommendedCardSet += FamilyCalmDebriefCardId.DESCRIBE_ACTION_CATEGORIES
            recommendedCardSet += FamilyCalmDebriefCardId.SET_MANUAL_FOLLOW_UP
        }
        val recommendedCardIds = FamilyCalmDebriefCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            FamilyCalmDebriefActionId.CHECKER
        } else {
            FamilyCalmDebriefActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf<FamilyCalmDebriefActionId>()
        if (hasDefaultState) {
            recommendedActionSet += FamilyCalmDebriefActionId.QUICK_STOP
            recommendedActionSet += FamilyCalmDebriefActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += currentResultActionId
            recommendedActionSet += FamilyCalmDebriefActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += FamilyCalmDebriefActionId.FAMILY_RESPONSE_ROLE_BOARD
            recommendedActionSet += FamilyCalmDebriefActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (hasFinancialProtectionSignal) {
            recommendedActionSet += FamilyCalmDebriefActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += FamilyCalmDebriefActionId.EVIDENCE_KIT
            recommendedActionSet += FamilyCalmDebriefActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += FamilyCalmDebriefActionId.FOLLOW_UP_CHECK_IN_PLANNER
            recommendedActionSet += FamilyCalmDebriefActionId.RECOVERY_PLAN
            if (analysis != null) recommendedActionSet += FamilyCalmDebriefActionId.REPORT_PREP
        }
        if (hasRemoteAccessSignal) {
            recommendedActionSet += FamilyCalmDebriefActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += FamilyCalmDebriefActionId.EVIDENCE_KIT
            recommendedActionSet += FamilyCalmDebriefActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += FamilyCalmDebriefActionId.RECOVERY_PLAN
        }
        if (hasLinkSignal) {
            recommendedActionSet += FamilyCalmDebriefActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += FamilyCalmDebriefActionId.EVIDENCE_KIT
            recommendedActionSet += FamilyCalmDebriefActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += FamilyCalmDebriefActionId.FOLLOW_UP_CHECK_IN_PLANNER
            recommendedActionSet += FamilyCalmDebriefActionId.PRIVACY_CENTER
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += FamilyCalmDebriefActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += FamilyCalmDebriefActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += FamilyCalmDebriefActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += FamilyCalmDebriefActionId.FAMILY_RESPONSE_ROLE_BOARD
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedActionSet += FamilyCalmDebriefActionId.QUICK_STOP
            recommendedActionSet += FamilyCalmDebriefActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += FamilyCalmDebriefActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (hasSavedIncident) {
            recommendedActionSet += FamilyCalmDebriefActionId.EVIDENCE_KIT
            recommendedActionSet += FamilyCalmDebriefActionId.INCIDENT_TIMELINE_BRIEFING
            recommendedActionSet += FamilyCalmDebriefActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (needsReadinessSupport) {
            recommendedActionSet += FamilyCalmDebriefActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += FamilyCalmDebriefActionId.FAMILY_READINESS_ROADMAP
            recommendedActionSet += FamilyCalmDebriefActionId.FAMILY_SAFETY_DRILL_COACH
            recommendedActionSet += FamilyCalmDebriefActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (recommendedActionSet.isEmpty()) {
            recommendedActionSet += FamilyCalmDebriefActionId.QUICK_STOP
            recommendedActionSet += FamilyCalmDebriefActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += FamilyCalmDebriefActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += FamilyCalmDebriefActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        val recommendedActions = FamilyCalmDebriefActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val agendaCards = buildAgendaCards(hasAnalysis = analysis != null)
        val recommendationTitle = when {
            analysis == null -> "분석 전 기본 5분 대화"
            hasFinancialProtectionSignal || hasRemoteAccessSignal ->
                "먼저 멈추고 보호 경로 고르기"
            signals.isEmpty() -> "일치 신호가 없어도 보수적으로 대화하기"
            else -> "현재 신호로 차분히 대화하기"
        }

        return FamilyCalmDebriefAgenda(
            title = "가족 차분 대화 안건",
            intro =
                "무서운 연락 뒤 가족이 5분 동안 누구를 탓하지 않고 다음 수동 경로를 고르는 " +
                    "로컬 전용·무상태 대화 순서입니다. 채팅 감시·업무 배정·일기 기능이 아니며 " +
                    "대화 내용이나 선택을 기록하지 않습니다.",
            durationLabel = "약 5분 · 가족이 옆에서 직접 읽는 수동 안건",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                analysis = analysis,
                readinessLevel = readiness.level,
                signals = signals,
                hasFinancialProtectionSignal = hasFinancialProtectionSignal,
                hasRemoteAccessSignal = hasRemoteAccessSignal,
                hasLinkSignal = hasLinkSignal,
                hasFamilyVerificationSignal = hasFamilyVerificationSignal,
                hasSecrecyImpersonationSignal = hasSecrecyImpersonationSignal,
                hasSavedIncident = hasSavedIncident
            ),
            recommendedCardIds = recommendedCardIds,
            recommendedActions = recommendedActions,
            agendaCards = agendaCards,
            privacySafetyNotes = privacySafetyNotes(),
            familyShareSummary = buildFamilyShareSummary(
                recommendedCardIds = recommendedCardIds,
                recommendedActions = recommendedActions,
                agendaCards = agendaCards
            )
        )
    }

    private fun buildAgendaCards(hasAnalysis: Boolean): List<FamilyCalmDebriefCard> {
        fun action(id: FamilyCalmDebriefActionId): FamilyCalmDebriefAction = actionFor(id)
        fun currentResultAction(): FamilyCalmDebriefAction = action(
            if (hasAnalysis) {
                FamilyCalmDebriefActionId.ANALYSIS_RESULT
            } else {
                FamilyCalmDebriefActionId.CHECKER
            }
        )

        return listOf(
            FamilyCalmDebriefCard(
                id = FamilyCalmDebriefCardId.CALM_WITHOUT_BLAME,
                displayOrder = FamilyCalmDebriefCardId.CALM_WITHOUT_BLAME.displayOrder,
                title = "비난 없이 안정시키기",
                category = FamilyCalmDebriefCategory.EMOTIONAL_CALM,
                whatToSayOrDo =
                    "“누구 탓인지보다 지금 함께 멈추는 일이 먼저예요. 한 사람씩 천천히 말해요.”라고 말하고 잠시 숨을 고릅니다.",
                whatNotToAskStoreOrShare = listOf(
                    "누가 속았는지 추궁하거나 서둘러 잘잘못을 정하지 마세요.",
                    "불안을 확인하려고 사기 원문이나 개인 비밀을 다시 보여 달라고 하지 마세요."
                ),
                primaryAction = action(FamilyCalmDebriefActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(FamilyCalmDebriefActionId.TRUSTED_HELPER_REVIEW),
                    action(FamilyCalmDebriefActionId.FAMILY_RESPONSE_ROLE_BOARD)
                )
            ),
            FamilyCalmDebriefCard(
                id = FamilyCalmDebriefCardId.CONFIRM_ACTION_PAUSE,
                displayOrder = FamilyCalmDebriefCardId.CONFIRM_ACTION_PAUSE.displayOrder,
                title = "돈·정보·앱 행동 멈춤 확인",
                category = FamilyCalmDebriefCategory.ACTION_PAUSE,
                whatToSayOrDo =
                    "“별도 확인 전에는 더 보내거나 알려 주거나 설치하지 않기로 해요.”라고 말하고 추가 행동을 함께 멈춥니다.",
                whatNotToAskStoreOrShare = listOf(
                    "정확한 송금·계정·인증·신분정보 값을 말하거나 적게 하지 마세요.",
                    "앱 버튼이 실제 지급정지·계정 잠금·기기 조치를 끝냈다고 여기지 마세요."
                ),
                primaryAction = action(FamilyCalmDebriefActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(FamilyCalmDebriefActionId.RECOVERY_PLAN),
                    currentResultAction(),
                    action(FamilyCalmDebriefActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            FamilyCalmDebriefCard(
                id = FamilyCalmDebriefCardId.DESCRIBE_ACTION_CATEGORIES,
                displayOrder = FamilyCalmDebriefCardId.DESCRIBE_ACTION_CATEGORIES.displayOrder,
                title = "행동 종류만 말하기",
                category = FamilyCalmDebriefCategory.CATEGORY_ONLY_REVIEW,
                whatToSayOrDo =
                    "“송금·결제, 정보 제공, 링크, 앱 설치, 통화 유지 중 어떤 종류가 있었는지만 말해요.”라고 묻고 범주만 고릅니다.",
                whatNotToAskStoreOrShare = listOf(
                    "사기 원문, 정확한 링크, 전화·계좌·인증·신분정보 값을 다시 묻거나 공유하지 마세요.",
                    "분석 신호를 실제 노출이나 피해가 확인됐다는 증거로 말하지 마세요."
                ),
                primaryAction = action(FamilyCalmDebriefActionId.EXPOSURE_IMPACT_TRIAGE),
                secondaryActions = listOf(
                    currentResultAction(),
                    action(FamilyCalmDebriefActionId.PRIVACY_CENTER)
                )
            ),
            FamilyCalmDebriefCard(
                id = FamilyCalmDebriefCardId.CHOOSE_SEPARATE_CALLBACK,
                displayOrder = FamilyCalmDebriefCardId.CHOOSE_SEPARATE_CALLBACK.displayOrder,
                title = "가족 본인확인·별도 콜백 정하기",
                category = FamilyCalmDebriefCategory.SEPARATE_VERIFICATION,
                whatToSayOrDo =
                    "“지금 받은 연락 말고 평소 알던 별도 경로로 가족이나 공식 기관을 직접 확인해요.”라고 말하고 콜백 순서를 고릅니다.",
                whatNotToAskStoreOrShare = listOf(
                    "가족 이름, 정확한 전화번호, 안전 질문 답이나 실제 콜백 문장을 이 안건에 입력하지 마세요.",
                    "새 번호·프로필·사진·목소리만으로 본인확인이 끝났다고 단정하지 마세요."
                ),
                primaryAction = action(FamilyCalmDebriefActionId.FAMILY_CALLBACK_CARD),
                secondaryActions = listOf(
                    action(FamilyCalmDebriefActionId.SAFE_CALL_SCRIPTS),
                    action(FamilyCalmDebriefActionId.FAMILY_ALERT_MESSAGES),
                    action(FamilyCalmDebriefActionId.FAMILY_RESPONSE_ROLE_BOARD)
                )
            ),
            FamilyCalmDebriefCard(
                id = FamilyCalmDebriefCardId.CHOOSE_RESPONSE_ROUTES,
                displayOrder = FamilyCalmDebriefCardId.CHOOSE_RESPONSE_ROUTES.displayOrder,
                title = "계정·기기·증거·공식 도움 경로 고르기",
                category = FamilyCalmDebriefCategory.MANUAL_RESPONSE,
                whatToSayOrDo =
                    "“실제 행동 종류에 맞춰 계정·기기 보호, 증거 보존, 공식 도움 중 지금 직접 할 한 경로부터 골라요.”라고 말합니다.",
                whatNotToAskStoreOrShare = listOf(
                    "비밀번호·인증값·계좌·카드·신분정보를 가족에게 받거나 대신 입력하지 마세요.",
                    "악성 프로그램 제거, 계정·카드 조치, 피해금 회수 또는 법률·금융 결과를 약속하지 마세요."
                ),
                primaryAction = action(FamilyCalmDebriefActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(FamilyCalmDebriefActionId.EVIDENCE_KIT),
                    action(FamilyCalmDebriefActionId.OFFICIAL_HELP_DIRECTORY),
                    action(FamilyCalmDebriefActionId.REPORT_PREP),
                    action(FamilyCalmDebriefActionId.RECOVERY_PLAN)
                )
            ),
            FamilyCalmDebriefCard(
                id = FamilyCalmDebriefCardId.SET_MANUAL_FOLLOW_UP,
                displayOrder = FamilyCalmDebriefCardId.SET_MANUAL_FOLLOW_UP.displayOrder,
                title = "며칠 뒤 다시 확인할 약속 말로 정하기",
                category = FamilyCalmDebriefCategory.FAMILY_FOLLOW_UP,
                whatToSayOrDo =
                    "“오늘로 끝났다고 단정하지 말고 며칠 뒤 가족이 직접 다시 볼 범주를 말로 정해요.”라고 합의합니다.",
                whatNotToAskStoreOrShare = listOf(
                    "담당자 이름, 날짜·답변·메모, 체크·완료·읽음 상태를 이 안건에 입력하거나 저장하지 마세요.",
                    "알림·알람·캘린더 일정이나 백그라운드 확인이 설정됐다고 여기지 마세요."
                ),
                primaryAction = action(FamilyCalmDebriefActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                secondaryActions = listOf(
                    action(FamilyCalmDebriefActionId.FAMILY_READINESS_ROADMAP),
                    action(FamilyCalmDebriefActionId.FAMILY_SAFETY_DRILL_COACH),
                    action(FamilyCalmDebriefActionId.INCIDENT_TIMELINE_BRIEFING),
                    action(FamilyCalmDebriefActionId.PRIVACY_CENTER)
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readinessLevel: ReadinessLevel,
        signals: Set<ScamSignal>,
        hasFinancialProtectionSignal: Boolean,
        hasRemoteAccessSignal: Boolean,
        hasLinkSignal: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasSecrecyImpersonationSignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (analysis == null || signals.isEmpty()) {
                add(
                    "현재 분석이 없거나 일치 신호가 없습니다. 이를 괜찮다는 판정으로 보지 않고 " +
                        "멈춤·행동 종류·별도 콜백·후속 약속부터 보수적으로 대화하세요."
                )
            }
            if (hasFinancialProtectionSignal) {
                add(
                    "위험도가 높거나 금융·결제·계정 관련 신호가 있어 추가 행동을 멈추고 " +
                        "계정·기기 보호, 증거, 공식 도움, 후속 점검 경로를 함께 권합니다."
                )
            }
            if (hasRemoteAccessSignal) {
                add(
                    "앱 설치·원격제어·통화 유지 관련 신호가 있어 계정·기기 보호와 증거 보존을 " +
                        "먼저 보고 곁의 보호자 검토를 함께 권합니다."
                )
            }
            if (hasLinkSignal) {
                add(
                    "링크·스미싱 관련 신호가 있어 행동 종류, 증거, 비밀 없는 가족 경고, " +
                        "개인정보·후속 점검 경로를 권합니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족·목소리 합성·번호 변경 관련 신호가 있어 평소 알던 별도 경로의 콜백과 " +
                        "일반 가족 경고를 권합니다."
                )
            }
            if (hasSecrecyImpersonationSignal) {
                add(
                    "비밀 유지·기관 사칭 압박 신호가 있어 먼저 멈추고 행동 종류만 확인한 뒤 " +
                        "공식 경로를 직접 찾도록 권합니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장된 사건이 있어도 사건 내용은 읽지 않으며 증거·타임라인·후속 점검 " +
                        "경로만 함께 권합니다."
                )
            }
            if (isEmpty()) {
                add("현재 신호만으로 사실을 확정하지 않고 멈춤과 직접 확인 경로를 차분히 고르세요.")
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW ->
                "가족 준비가 아직 부족해도 괜찮습니다. 별도 콜백·준비 로드맵·연습·후속 점검부터 작게 시작하세요."
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중입니다. 마련한 습관을 다시 확인하고 콜백·연습·후속 점검으로 빈 부분을 보완하세요."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있어도 이번 연락의 사실이나 조치 완료가 확인된 것은 아닙니다. 별도 콜백과 필요한 수동 경로를 다시 확인하세요."
        }
        return "$situationCopy $readinessCopy"
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 안건은 가족이 옆에서 직접 읽고 말하는 로컬 전용·무상태 수동 대화 안내입니다.",
        "가족 이름·연락처, 정확한 전화번호, 비밀번호·OTP/PIN, 안전 질문 답, 계좌·카드·신분정보, 원문·링크·메모를 묻거나 저장하지 않습니다.",
        "안건·카드 선택, 대화 답변·메모, 담당자·날짜, 체크·완료·점수·읽음·진행 상태, 공유 이력·대화 이력을 저장하지 않습니다.",
        "문자·메시지·통화·연락처·계정·기기를 읽거나 감시하지 않고 받는 사람을 고르거나 메시지를 자동으로 보내지 않습니다.",
        "네트워크를 사용하거나 새 Android 권한을 요청하지 않으며 전화·문자를 자동 실행하거나 차단하지 않습니다.",
        "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커를 만들거나 예약하지 않습니다.",
        "화면을 열거나 안건·경로·공유 버튼을 눌러도 사건 기록을 자동으로 추가·수정·삭제하지 않습니다.",
        "추천은 위험도·일치 신호, 값 없는 준비 수준, 저장 사건 유무만 사용하며 사기 원문·가린 단서·안전 계획 값·사건별 내용을 사용하지 않습니다.",
        "이 안내는 사기·피해·본인확인·조치 완료를 증명하지 않으며 링크 검사, 악성 프로그램 제거, 계정·카드 조치, 피해금 회수, 법률·금융 결과를 수행하거나 보장하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<FamilyCalmDebriefCardId>,
        recommendedActions: List<FamilyCalmDebriefAction>,
        agendaCards: List<FamilyCalmDebriefCard>
    ): String {
        val cardsById = agendaCards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 5분 가족 차분 대화")
            appendLine()
            appendLine("먼저 이야기할 안건")
            recommendedCardIds.forEach { cardId ->
                val card = cardsById.getValue(cardId)
                appendLine("- [${card.category.label}] ${card.title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("전체 5분 대화 안건")
            agendaCards.forEach { card ->
                appendLine("- [${card.category.label}] ${card.title}")
            }
            appendLine()
            append(
                "사람 이름·연락처·원문·비밀값을 넣지 않은 수동 대화 안내이며, " +
                    "가족이 직접 확인하고 필요한 기존 앱 경로를 고릅니다."
            )
        }.trim()
    }

    private fun actionFor(id: FamilyCalmDebriefActionId): FamilyCalmDebriefAction =
        FamilyCalmDebriefAction(
            id = id,
            label = when (id) {
                FamilyCalmDebriefActionId.QUICK_STOP -> "긴급 멈춤 모드"
                FamilyCalmDebriefActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
                FamilyCalmDebriefActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 확인 카드"
                FamilyCalmDebriefActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
                FamilyCalmDebriefActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
                FamilyCalmDebriefActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처"
                FamilyCalmDebriefActionId.FOLLOW_UP_CHECK_IN_PLANNER -> "며칠 뒤 후속 점검"
                FamilyCalmDebriefActionId.TRUSTED_HELPER_REVIEW -> "보호자와 함께 안전 검토"
                FamilyCalmDebriefActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
                FamilyCalmDebriefActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
                FamilyCalmDebriefActionId.FAMILY_SAFETY_DRILL_COACH -> "가족 안전 연습 코치"
                FamilyCalmDebriefActionId.FAMILY_RESPONSE_ROLE_BOARD -> "가족 대응 역할 보드"
                FamilyCalmDebriefActionId.SAFE_CALL_SCRIPTS -> "안전 확인 전화 스크립트"
                FamilyCalmDebriefActionId.RECOVERY_PLAN -> "24시간 수습 계획"
                FamilyCalmDebriefActionId.REPORT_PREP -> "신고용 상담 준비 카드"
                FamilyCalmDebriefActionId.INCIDENT_TIMELINE_BRIEFING -> "사건 타임라인 요약"
                FamilyCalmDebriefActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
                FamilyCalmDebriefActionId.PRIVACY_CENTER -> "개인정보 센터"
                FamilyCalmDebriefActionId.CHECKER -> "의심 내용 검사"
                FamilyCalmDebriefActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            }
        )
}
