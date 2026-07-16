package com.mongmong.familyshield

enum class RecontactBoundaryCardId(val displayOrder: Int) {
    SAME_CHANNEL_NON_RESPONSE(1),
    NEW_NUMBER_PROFILE_LINK_SEPARATE_PATH(2),
    SECRECY_PRESSURE_FAMILY_ALERT(3),
    MONEY_CREDENTIALS_STAY_PAUSED(4),
    REMOTE_ACCESS_STAY_CLOSED(5),
    FAMILY_FOLLOW_UP_PROMISE(6)
}

enum class RecontactBoundaryActionId {
    QUICK_STOP,
    ACTIVE_CONTACT_EXIT_SCRIPTS,
    CHECKER,
    ANALYSIS_RESULT,
    PRIVACY_CENTER,
    EXPOSURE_IMPACT_TRIAGE,
    FAMILY_CALLBACK_CARD,
    SEPARATE_CHANNEL_VERIFICATION_COACH,
    VERIFICATION_OUTCOME_NEXT_STEPS_BOARD,
    SAFE_CALL_SCRIPTS,
    FAMILY_ALERT_MESSAGES,
    TRUSTED_HELPER_REVIEW,
    FAMILY_READINESS_ROADMAP,
    MONEY_TRANSFER_PAUSE_GATE,
    ACCOUNT_LOCKDOWN,
    REMOTE_ACCESS_CUTOFF_COACH,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    RECOVERY_PLAN,
    FOLLOW_UP_CHECK_IN_PLANNER,
    FAMILY_CALM_DEBRIEF_AGENDA,
    SAFETY_FLOW_ROUTER
}

data class RecontactBoundaryAction(
    val id: RecontactBoundaryActionId,
    val label: String
)

data class RecontactBoundaryCard(
    val id: RecontactBoundaryCardId,
    val displayOrder: Int,
    val title: String,
    val recontactScenario: String,
    val boundaryScript: String,
    val whyItMatters: String,
    val doNotRecordOrShareBoundaries: List<String>,
    val primaryAction: RecontactBoundaryAction,
    val secondaryActions: List<RecontactBoundaryAction>
) {
    val routeActions: List<RecontactBoundaryAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class RecontactBoundaryCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<RecontactBoundaryCardId>,
    val recommendedActions: List<RecontactBoundaryAction>,
    val cards: List<RecontactBoundaryCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<RecontactBoundaryCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<RecontactBoundaryActionId>
        get() = recommendedActions.map { action -> action.id }
}

object RecontactBoundaryCoachBuilder {
    private val familyVerificationSignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.PHONE_NUMBER_CHANGE
    )

    private val secrecyImpersonationSignals = setOf(
        ScamSignal.SECRECY,
        ScamSignal.IMPERSONATION
    )

    private val financialSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.BAIT,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val credentialSignals = setOf(
        ScamSignal.CREDENTIALS,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val linkSignals = setOf(
        ScamSignal.LINK,
        ScamSignal.PARCEL_INVITATION_SMISHING
    )

    private val remoteAccessSignals = setOf(
        ScamSignal.APP_INSTALL,
        ScamSignal.LIVE_CALL_CONTROL
    )

    fun build(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness,
        incidentCount: Int = 0
    ): RecontactBoundaryCoach {
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val hasNoAnalysisOrSignals = analysis == null || signals.isEmpty()
        val hasHighRisk = analysis?.riskLevel == RiskLevel.HIGH
        val hasFamilyVerificationSignal = signals.any { signal ->
            signal in familyVerificationSignals
        }
        val hasSecrecyImpersonationSignal = signals.any { signal ->
            signal in secrecyImpersonationSignals
        }
        val hasFinancialSignal = signals.any { signal -> signal in financialSignals }
        val hasCredentialSignal = signals.any { signal -> signal in credentialSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasRemoteAccessSignal = signals.any { signal -> signal in remoteAccessSignals }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0
        val needsReadinessSupport = readiness.level != ReadinessLevel.READY

        val recommendedCardSet = linkedSetOf(
            RecontactBoundaryCardId.SAME_CHANNEL_NON_RESPONSE,
            RecontactBoundaryCardId.FAMILY_FOLLOW_UP_PROMISE
        )
        if (hasNoAnalysisOrSignals) {
            recommendedCardSet += RecontactBoundaryCardId.entries
        }
        if (hasHighRisk) {
            recommendedCardSet += RecontactBoundaryCardId.NEW_NUMBER_PROFILE_LINK_SEPARATE_PATH
            recommendedCardSet += RecontactBoundaryCardId.SECRECY_PRESSURE_FAMILY_ALERT
            recommendedCardSet += RecontactBoundaryCardId.MONEY_CREDENTIALS_STAY_PAUSED
            recommendedCardSet += RecontactBoundaryCardId.REMOTE_ACCESS_STAY_CLOSED
        }
        if (hasFamilyVerificationSignal || hasLinkSignal) {
            recommendedCardSet += RecontactBoundaryCardId.NEW_NUMBER_PROFILE_LINK_SEPARATE_PATH
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedCardSet += RecontactBoundaryCardId.SECRECY_PRESSURE_FAMILY_ALERT
        }
        if (hasFinancialSignal || hasCredentialSignal) {
            recommendedCardSet += RecontactBoundaryCardId.MONEY_CREDENTIALS_STAY_PAUSED
        }
        if (hasRemoteAccessSignal) {
            recommendedCardSet += RecontactBoundaryCardId.REMOTE_ACCESS_STAY_CLOSED
        }
        if (hasSavedIncident || needsReadinessSupport) {
            recommendedCardSet += RecontactBoundaryCardId.FAMILY_FOLLOW_UP_PROMISE
        }
        val recommendedCardIds = RecontactBoundaryCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            RecontactBoundaryActionId.CHECKER
        } else {
            RecontactBoundaryActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            RecontactBoundaryActionId.QUICK_STOP,
            RecontactBoundaryActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
            RecontactBoundaryActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
            RecontactBoundaryActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD,
            RecontactBoundaryActionId.FOLLOW_UP_CHECK_IN_PLANNER,
            RecontactBoundaryActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            recommendedActionSet += currentResultActionId
            recommendedActionSet += RecontactBoundaryActionId.PRIVACY_CENTER
            recommendedActionSet += RecontactBoundaryActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += RecontactBoundaryActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += RecontactBoundaryActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += RecontactBoundaryActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += RecontactBoundaryActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += RecontactBoundaryActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += RecontactBoundaryActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += RecontactBoundaryActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += RecontactBoundaryActionId.EVIDENCE_KIT
            recommendedActionSet += RecontactBoundaryActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += RecontactBoundaryActionId.RECOVERY_PLAN
            recommendedActionSet += RecontactBoundaryActionId.FAMILY_CALM_DEBRIEF_AGENDA
        }
        if (hasHighRisk) {
            recommendedActionSet += RecontactBoundaryActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += RecontactBoundaryActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += RecontactBoundaryActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += RecontactBoundaryActionId.EVIDENCE_KIT
            recommendedActionSet += RecontactBoundaryActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += RecontactBoundaryActionId.REPORT_PREP
            recommendedActionSet += RecontactBoundaryActionId.RECOVERY_PLAN
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += RecontactBoundaryActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += RecontactBoundaryActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += RecontactBoundaryActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += RecontactBoundaryActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet +=
                RecontactBoundaryActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedActionSet += RecontactBoundaryActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet += RecontactBoundaryActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += RecontactBoundaryActionId.FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet += RecontactBoundaryActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += RecontactBoundaryActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
        }
        if (hasFinancialSignal) {
            recommendedActionSet += RecontactBoundaryActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += RecontactBoundaryActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += RecontactBoundaryActionId.EVIDENCE_KIT
            recommendedActionSet += RecontactBoundaryActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += RecontactBoundaryActionId.REPORT_PREP
            recommendedActionSet += RecontactBoundaryActionId.RECOVERY_PLAN
            recommendedActionSet += RecontactBoundaryActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (hasCredentialSignal) {
            recommendedActionSet += RecontactBoundaryActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += RecontactBoundaryActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += RecontactBoundaryActionId.EVIDENCE_KIT
            recommendedActionSet += RecontactBoundaryActionId.RECOVERY_PLAN
        }
        if (hasLinkSignal) {
            recommendedActionSet += RecontactBoundaryActionId.CHECKER
            recommendedActionSet += RecontactBoundaryActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += RecontactBoundaryActionId.PRIVACY_CENTER
            recommendedActionSet += RecontactBoundaryActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet +=
                RecontactBoundaryActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
        }
        if (hasRemoteAccessSignal) {
            recommendedActionSet += RecontactBoundaryActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += RecontactBoundaryActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += RecontactBoundaryActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += RecontactBoundaryActionId.EVIDENCE_KIT
            recommendedActionSet += RecontactBoundaryActionId.RECOVERY_PLAN
        }
        if (hasSavedIncident) {
            recommendedActionSet += RecontactBoundaryActionId.EVIDENCE_KIT
            if (analysis != null) {
                recommendedActionSet += RecontactBoundaryActionId.REPORT_PREP
            }
            recommendedActionSet += RecontactBoundaryActionId.FOLLOW_UP_CHECK_IN_PLANNER
            recommendedActionSet += RecontactBoundaryActionId.FAMILY_CALM_DEBRIEF_AGENDA
        }
        if (needsReadinessSupport) {
            recommendedActionSet += RecontactBoundaryActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += RecontactBoundaryActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += RecontactBoundaryActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += RecontactBoundaryActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += RecontactBoundaryActionId.FAMILY_READINESS_ROADMAP
            recommendedActionSet += RecontactBoundaryActionId.FAMILY_CALM_DEBRIEF_AGENDA
        }
        if (analysis != null && currentResultActionId in recommendedActionSet) {
            recommendedActionSet -= RecontactBoundaryActionId.CHECKER
        }
        val recommendedActions = RecontactBoundaryActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards(hasAnalysis = analysis != null)
        val recommendationTitle = when {
            analysis == null -> "분석 전에도 모든 재연락 경계를 보수적으로 읽기"
            hasRemoteAccessSignal -> "원격지시 재개 요구에 답하지 않고 보호 경로 고르기"
            hasCredentialSignal -> "인증·계정 재요구에 답하지 않고 계정 행동 멈추기"
            hasFinancialSignal -> "돈 요구가 되풀이돼도 멈춤을 유지하기"
            hasHighRisk -> "높은 위험 신호에도 같은 경로를 닫고 보호 경로 고르기"
            hasFamilyVerificationSignal -> "새 번호·목소리·가족 압박도 평소 경로로 분리하기"
            hasSecrecyImpersonationSignal -> "재연락을 비밀로 두지 않고 가족과 경계 세우기"
            hasLinkSignal -> "새 링크·프로필에 반응하지 않고 별도 경로 고르기"
            else -> "같은 경로를 닫아 둔 채 가족 후속 약속 정하기"
        }

        return RecontactBoundaryCoach(
            title = "재연락 경계 코치",
            intro =
                "의심 연락을 멈춘 뒤 다시 전화·문자하거나 새 번호·프로필·링크로 접근해도 " +
                    "그 경로에서 협상하지 않도록 가족이 짧은 문장을 직접 읽는 로컬 전용·무상태 " +
                    "수동 안내입니다. 상대·채널·번호·링크·기기 상태를 판정하지 않습니다.",
            modeLabel = "같은 경로는 닫기 · 값 없는 경계 문장 읽기",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                readinessLevel = readiness.level,
                hasNoAnalysisOrSignals = hasNoAnalysisOrSignals,
                hasHighRisk = hasHighRisk,
                hasFamilyVerificationSignal = hasFamilyVerificationSignal,
                hasSecrecyImpersonationSignal = hasSecrecyImpersonationSignal,
                hasFinancialSignal = hasFinancialSignal,
                hasCredentialSignal = hasCredentialSignal,
                hasLinkSignal = hasLinkSignal,
                hasRemoteAccessSignal = hasRemoteAccessSignal,
                hasSavedIncident = hasSavedIncident
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

    private fun buildCards(hasAnalysis: Boolean): List<RecontactBoundaryCard> {
        fun action(id: RecontactBoundaryActionId): RecontactBoundaryAction = actionFor(id)

        fun currentResultAction(): RecontactBoundaryAction = action(
            if (hasAnalysis) {
                RecontactBoundaryActionId.ANALYSIS_RESULT
            } else {
                RecontactBoundaryActionId.CHECKER
            }
        )

        return listOf(
            RecontactBoundaryCard(
                id = RecontactBoundaryCardId.SAME_CHANNEL_NON_RESPONSE,
                displayOrder = RecontactBoundaryCardId.SAME_CHANNEL_NON_RESPONSE.displayOrder,
                title = "다시 전화·문자 오면: 같은 경로에서 응답하지 않기",
                recontactScenario =
                    "방금 멈춘 번호·대화방·프로필과 같은 경로에서 다시 연락이 오면",
                boundaryScript =
                    "이 경로에서는 더 답하지 않겠습니다. 별도 경로로 확인하겠습니다.",
                whyItMatters =
                    "같은 경로에서 설명을 듣거나 따지기 시작하면 새 압박과 요구로 다시 이어질 수 있어 먼저 대화를 닫아 두는 것이 중요합니다.",
                doNotRecordOrShareBoundaries = listOf(
                    "정확한 전화번호·연락처·대화방·프로필·통화 기록·메시지 원문이나 다시 연락한 결과를 이 카드에 적지 마세요.",
                    "답하지 않았다는 상태를 저장·체크하거나 상대에게 가족 경로·확인 답·다음 행동을 알려 주지 마세요."
                ),
                primaryAction = action(RecontactBoundaryActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                secondaryActions = listOf(
                    action(RecontactBoundaryActionId.QUICK_STOP),
                    action(RecontactBoundaryActionId.SEPARATE_CHANNEL_VERIFICATION_COACH),
                    action(RecontactBoundaryActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
                )
            ),
            RecontactBoundaryCard(
                id = RecontactBoundaryCardId.NEW_NUMBER_PROFILE_LINK_SEPARATE_PATH,
                displayOrder =
                    RecontactBoundaryCardId.NEW_NUMBER_PROFILE_LINK_SEPARATE_PATH.displayOrder,
                title = "새 번호·새 프로필·새 링크로 오면: 별도 경로로 분리하기",
                recontactScenario =
                    "번호·계정·프로필·대화방·링크를 바꿔 같은 사람이라고 주장하며 다시 접근하면",
                boundaryScript =
                    "새 경로에서는 확인하지 않겠습니다. 제가 평소 쓰던 경로로 따로 확인하겠습니다.",
                whyItMatters =
                    "상대가 새로 제시한 경로끼리 비교하면 모두 상대가 통제할 수 있으므로 평소 가족 경로나 사용자가 직접 찾은 경로로 분리해야 합니다.",
                doNotRecordOrShareBoundaries = listOf(
                    "새 번호·연락처·URL·도메인·프로필 이름·가족 이름·안전 질문 답·콜백 문장 원문을 입력하거나 공유하지 마세요.",
                    "이 카드가 새 번호·링크·프로필의 주인이나 신뢰 여부를 판정한다고 여기지 마세요."
                ),
                primaryAction = action(
                    RecontactBoundaryActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                ),
                secondaryActions = listOf(
                    action(RecontactBoundaryActionId.FAMILY_CALLBACK_CARD),
                    action(RecontactBoundaryActionId.SAFE_CALL_SCRIPTS),
                    currentResultAction(),
                    action(RecontactBoundaryActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(RecontactBoundaryActionId.PRIVACY_CENTER),
                    action(RecontactBoundaryActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
                )
            ),
            RecontactBoundaryCard(
                id = RecontactBoundaryCardId.SECRECY_PRESSURE_FAMILY_ALERT,
                displayOrder = RecontactBoundaryCardId.SECRECY_PRESSURE_FAMILY_ALERT.displayOrder,
                title = "협박·비밀·기관 압박이 다시 오면: 가족에게 값 없이 알리기",
                recontactScenario =
                    "불이익·수사·처벌을 말하거나 가족에게 숨기고 통화를 유지하라고 다시 압박하면",
                boundaryScript =
                    "이 연락은 숨기지 않겠습니다. 지금 가족과 상의하고 이 경로에는 답하지 않겠습니다.",
                whyItMatters =
                    "비밀 유지 압박을 끊고 행동 범주만 가족에게 알리면 혼자 판단하거나 같은 상대와 협상하는 상황을 줄일 수 있습니다.",
                doNotRecordOrShareBoundaries = listOf(
                    "협박 원문·담당자 이름·기관 이름·정확한 번호·접수값·가족 연락처·통화 내용을 이 카드에 기록하지 마세요.",
                    "가족 알림에는 원문이나 개인정보 대신 ‘재연락·비밀 압박·현재 경로 중단’ 같은 행동 범주만 쓰세요."
                ),
                primaryAction = action(RecontactBoundaryActionId.FAMILY_ALERT_MESSAGES),
                secondaryActions = listOf(
                    action(RecontactBoundaryActionId.FAMILY_CALM_DEBRIEF_AGENDA),
                    action(RecontactBoundaryActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(RecontactBoundaryActionId.OFFICIAL_HELP_DIRECTORY),
                    action(RecontactBoundaryActionId.SEPARATE_CHANNEL_VERIFICATION_COACH)
                )
            ),
            RecontactBoundaryCard(
                id = RecontactBoundaryCardId.MONEY_CREDENTIALS_STAY_PAUSED,
                displayOrder =
                    RecontactBoundaryCardId.MONEY_CREDENTIALS_STAY_PAUSED.displayOrder,
                title = "돈·OTP·계정·카드를 다시 요구하면: 돈·계정 행동 계속 멈추기",
                recontactScenario =
                    "송금·결제·상품권이나 계정·카드·비밀번호·OTP·PIN을 한 번만 더 달라고 하면",
                boundaryScript =
                    "돈과 인증 정보는 더 주지 않겠습니다. 가족과 별도 도움 경로를 먼저 확인하겠습니다.",
                whyItMatters =
                    "‘마지막 한 번’이라는 말도 추가 돈 이동이나 계정 노출로 이어질 수 있어 현재 경로의 요구를 계속 보류해야 합니다.",
                doNotRecordOrShareBoundaries = listOf(
                    "금액·계좌·카드·비밀번호·OTP·PIN·거래값·접수값이나 금융 화면을 이 카드에 입력·저장·공유하지 마세요.",
                    "이 카드가 송금·결제를 취소하거나 계정·카드를 정지·동결하거나 환급·회수를 끝냈다고 여기지 마세요."
                ),
                primaryAction = action(RecontactBoundaryActionId.MONEY_TRANSFER_PAUSE_GATE),
                secondaryActions = listOf(
                    action(RecontactBoundaryActionId.ACCOUNT_LOCKDOWN),
                    action(RecontactBoundaryActionId.EVIDENCE_KIT),
                    action(RecontactBoundaryActionId.OFFICIAL_HELP_DIRECTORY),
                    action(RecontactBoundaryActionId.REPORT_PREP),
                    action(RecontactBoundaryActionId.RECOVERY_PLAN),
                    action(RecontactBoundaryActionId.FOLLOW_UP_CHECK_IN_PLANNER)
                )
            ),
            RecontactBoundaryCard(
                id = RecontactBoundaryCardId.REMOTE_ACCESS_STAY_CLOSED,
                displayOrder = RecontactBoundaryCardId.REMOTE_ACCESS_STAY_CLOSED.displayOrder,
                title = "앱 설치·화면공유·원격지시를 다시 요구하면: 기기 화면 닫기",
                recontactScenario =
                    "앱·파일·프로필 설치, 화면공유, 스피커폰, 원격 조작이나 설정 변경을 다시 요구하면",
                boundaryScript =
                    "화면과 기기 조작을 더 보여 주지 않겠습니다. 보호자와 별도로 점검하겠습니다.",
                whyItMatters =
                    "현재 연락을 보면서 기기를 계속 조작하면 인증 화면이나 추가 설정이 노출될 수 있어 화면을 숨기고 기존 보호 경로로 옮겨야 합니다.",
                doNotRecordOrShareBoundaries = listOf(
                    "앱·패키지 이름·기기 식별자·계정 화면·비밀번호·인증값·설치 목록·화면공유 상태를 이 카드에 넣거나 공유하지 마세요.",
                    "이 카드가 원격 연결을 해제하거나 악성 프로그램을 제거하거나 기기를 정리했다고 여기지 마세요."
                ),
                primaryAction = action(
                    RecontactBoundaryActionId.REMOTE_ACCESS_CUTOFF_COACH
                ),
                secondaryActions = listOf(
                    action(RecontactBoundaryActionId.TRUSTED_HELPER_REVIEW),
                    action(RecontactBoundaryActionId.ACCOUNT_LOCKDOWN),
                    action(RecontactBoundaryActionId.EVIDENCE_KIT),
                    action(RecontactBoundaryActionId.EXPOSURE_IMPACT_TRIAGE)
                )
            ),
            RecontactBoundaryCard(
                id = RecontactBoundaryCardId.FAMILY_FOLLOW_UP_PROMISE,
                displayOrder = RecontactBoundaryCardId.FAMILY_FOLLOW_UP_PROMISE.displayOrder,
                title = "가족 후속 약속: 오늘·내일·이번 주 다시 확인하기",
                recontactScenario =
                    "연락을 멈춘 뒤 가족이 추가 접근이나 필요한 보호 행동을 함께 다시 살펴봐야 하면",
                boundaryScript =
                    "오늘은 더 대응하지 않고, 가족과 정한 때에 기존 앱 경로를 다시 살펴보겠습니다.",
                whyItMatters =
                    "한 번 멈춘 뒤에도 다른 가족이나 경로로 압박이 이어질 수 있어 값 없이 다시 볼 시점과 도움 역할만 정해 두는 것이 좋습니다.",
                doNotRecordOrShareBoundaries = listOf(
                    "이름·담당자·날짜·연락처·재연락 결과·차단·음소거 상태·메모·체크 표시를 이 카드에 저장하지 마세요.",
                    "가족 공유에는 실제 확인 답·번호·링크·계정·카드·기기·가족 안전 계획 값을 넣지 마세요."
                ),
                primaryAction = action(
                    RecontactBoundaryActionId.FOLLOW_UP_CHECK_IN_PLANNER
                ),
                secondaryActions = listOf(
                    action(RecontactBoundaryActionId.FAMILY_CALM_DEBRIEF_AGENDA),
                    action(RecontactBoundaryActionId.FAMILY_ALERT_MESSAGES),
                    action(RecontactBoundaryActionId.TRUSTED_HELPER_REVIEW),
                    action(RecontactBoundaryActionId.FAMILY_READINESS_ROADMAP),
                    action(RecontactBoundaryActionId.SAFETY_FLOW_ROUTER)
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        readinessLevel: ReadinessLevel,
        hasNoAnalysisOrSignals: Boolean,
        hasHighRisk: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasSecrecyImpersonationSignal: Boolean,
        hasFinancialSignal: Boolean,
        hasCredentialSignal: Boolean,
        hasLinkSignal: Boolean,
        hasRemoteAccessSignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (hasNoAnalysisOrSignals) {
                add(
                    "현재 분석이 없거나 일치 신호가 없습니다. 이를 사람·번호·프로필·링크·기관·" +
                        "계정·기기에 문제가 없다는 판정으로 보지 말고 모든 경계를 보수적으로 읽으세요."
                )
            }
            if (hasHighRisk) {
                add(
                    "위험도가 높아 같은 경로를 닫은 채 돈·계정·기기·증거·기관 도움 경로를 함께 " +
                        "권합니다. 상대의 정체나 재연락 상태를 확정하는 뜻은 아닙니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 긴급상황·목소리 합성·번호 변경 신호가 있어 같은 경로에 답하지 않고 평소 " +
                        "가족 콜백·값 없는 확인 전화·가족 경고·별도 경로·결과별 다음 단계를 권합니다."
                )
            }
            if (hasSecrecyImpersonationSignal) {
                add(
                    "비밀 유지·기관 사칭 신호가 있어 재연락을 혼자 숨기지 않고 종료 문장·가족 알림·" +
                        "차분 대화·사용자가 직접 고를 도움·별도 경로를 권합니다."
                )
            }
            if (hasFinancialSignal) {
                add(
                    "송금·결제·금융 압박 신호가 있어 돈 행동을 계속 멈추고 계정·증거·기관 도움·" +
                        "신고 준비·24시간 수습·후속 점검 경로를 권합니다. 취소·정지·동결·환급·" +
                        "회수를 대신하거나 보장하지 않습니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증 비밀 재요구 가능성이 있어 계정 보호와 원격지시 중단 경로를 권합니다. " +
                        "계정이나 카드를 자동 정지·동결하는 기능은 아닙니다."
                )
            }
            if (hasLinkSignal) {
                add(
                    "링크·스미싱 신호가 있어 새 링크에 반응하지 않고 검사기·노출 분류·개인정보·" +
                        "별도 경로를 권합니다. 링크를 스캔하거나 URL·도메인을 검증하지 않습니다."
                )
            }
            if (hasRemoteAccessSignal) {
                add(
                    "앱 설치·화면공유·원격지시 신호가 있어 원격지시 중단·보호자·계정·증거 경로를 " +
                        "권합니다. 원격 연결 해제·악성 프로그램 제거·기기 정리를 수행하거나 " +
                        "보장하지 않습니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만 반영하며 사건 내용은 읽지 않습니다. 값 없는 " +
                        "증거·신고 준비·후속 점검·가족 대화 경로만 함께 권합니다."
                )
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW ->
                "가족 준비가 부족한 상태라 평소 가족 콜백·값 없는 확인 전화·가족 알림·보호자 검토·준비 로드맵을 함께 권합니다. 실제 가족 값은 입력하지 마세요."
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중이라 평소 가족 콜백·값 없는 확인 전화·가족 알림·보호자 검토·준비 로드맵으로 빠진 부분을 직접 살피도록 권합니다. 저장된 실제 값은 추천에 사용하지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있어도 이번 사람·번호·프로필·링크·기관·계정·기기나 재연락 결과를 확인했다는 뜻은 아닙니다. 같은 경로를 닫은 채 수동 경계를 고르세요."
        }
        return listOf(situationCopy, readinessCopy).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 사용자가 화면을 직접 읽고 직접 선택하는 로컬 전용·무상태 수동 안내이며 재연락 결과 입력을 요구하지 않습니다.",
        "추천은 위험도·일치 신호, 값 없는 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·사건 내용·가족 안전 계획 값을 사용하지 않습니다.",
        "이름·연락처·정확한 전화번호·계정·계좌·카드·OTP·PIN·비밀번호·가족 확인 답·주민번호·기기 식별자·정확한 URL·도메인·공식 번호·앱 이름·패키지 이름·재연락 결과값을 묻거나 저장하지 않습니다.",
        "카드 선택·추천·읽음·공유·재연락 상태·경계 상태·차단·음소거 상태·채널 값·체크 표시·메모·진행·이력을 위한 새 저장소를 만들거나 기록하지 않습니다.",
        "통화 기록·문자·메시지·통화 상태·연락처·알림을 읽거나 감시하지 않고 연락처를 읽거나 쓰거나 고르지 않습니다.",
        "네트워크·웹 탐색·지도·위치·기관 번호 조회·공식 경로 조회·전화번호 검증·링크·URL·도메인 검증·링크 스캔을 사용하지 않습니다.",
        "새 Android 권한을 요청하지 않습니다.",
        "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커를 만들거나 예약하지 않으며 스케줄러를 사용하지 않습니다.",
        "전화 앱을 열거나 자동 다이얼·발신·통화·종료를 하지 않고 통화를 선별·차단·음소거하지 않으며 문자·SMS를 보내거나 읽지 않습니다.",
        "받는 사람을 고르거나 공유를 자동 전송하지 않고 알림을 필터링하거나 재연락을 모니터링하지 않습니다.",
        "클립보드를 읽거나 쓰지 않고 답·번호·링크·결과를 복사하거나 붙여넣지 않습니다.",
        "결제·은행 기능이나 API, NFC나 Android 계정 관리자에 접근하지 않고 송금·결제·취소·계정 또는 카드 정지·동결·환급·피해 복구를 자동 수행하지 않습니다.",
        "기기 관리자·접근성 서비스·VPN·알림 접근·사용 기록·앱 작업 권한이나 기기 제어 기능을 사용하지 않고 설치 앱·패키지 목록을 조회·설치·제거하거나 설정을 바꾸지 않습니다.",
        "붙여넣은 글, 통화 상태·기록, 연락처, 문자, 알림, 사건 기록, 가족 안전 계획, 기관 목록, 계정·카드·은행·기기·앱·패키지·네트워크·VPN·알림 설정, 돈의 움직임, 확인 결과나 재연락·경계 상태를 추가·수정·삭제하지 않습니다.",
        "원문·화면·연락·번호·링크·기기·계정·재연락을 자동으로 가리거나 모니터링·스캔·삭제·차단·호출·메시지 전송·조회·확인·검증·저장하지 않습니다.",
        "이 안내는 본인·번호·링크·기관 확인, 사기 여부, 통화·문자 차단, 악성 프로그램 제거, 원격 연결 종료, 기기 정리, 계정·카드 조치, 결제 취소·환급·회복 또는 법률·금융 결과를 증명하거나 보장하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<RecontactBoundaryCardId>,
        recommendedActions: List<RecontactBoundaryAction>,
        cards: List<RecontactBoundaryCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 재연락 경계 코치")
            appendLine()
            appendLine("다시 연락해도 같은 경로에서 협상하지 않을 경계를 함께 보고 있습니다.")
            appendLine()
            appendLine("추천 경계 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("가족에게 알릴 값 없는 문장")
            appendLine("- 같은 경로에는 답하지 않고 가족과 별도 앱 경로를 직접 고르고 있습니다.")
            append(
                "원문·사람·연락처·번호·링크·인증·계정·카드·기기·가족 계획·확인 답·" +
                    "재연락 결과값 없이 고정된 카드와 기존 앱 경로 이름만 담았습니다."
            )
        }.trim()
    }

    private fun actionFor(id: RecontactBoundaryActionId): RecontactBoundaryAction =
        RecontactBoundaryAction(
            id = id,
            label = when (id) {
                RecontactBoundaryActionId.QUICK_STOP -> "긴급 멈춤 모드"
                RecontactBoundaryActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                    "진행 중 연락 종료 문장"
                RecontactBoundaryActionId.CHECKER -> "의심 내용 검사"
                RecontactBoundaryActionId.ANALYSIS_RESULT -> "현재 분석 결과"
                RecontactBoundaryActionId.PRIVACY_CENTER -> "개인정보 센터"
                RecontactBoundaryActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
                RecontactBoundaryActionId.FAMILY_CALLBACK_CARD -> "평소 가족 경로 콜백 카드"
                RecontactBoundaryActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                    "별도 경로 확인 코치"
                RecontactBoundaryActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                    "확인 결과별 다음 단계 보드"
                RecontactBoundaryActionId.SAFE_CALL_SCRIPTS ->
                    "값 없는 가족 콜백·확인 전화 스크립트"
                RecontactBoundaryActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
                RecontactBoundaryActionId.TRUSTED_HELPER_REVIEW ->
                    "보호자와 함께 안전 검토"
                RecontactBoundaryActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
                RecontactBoundaryActionId.MONEY_TRANSFER_PAUSE_GATE -> "송금 전 멈춤 확인"
                RecontactBoundaryActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
                RecontactBoundaryActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                    "화면공유·원격지시 끊기 코치"
                RecontactBoundaryActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
                RecontactBoundaryActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처"
                RecontactBoundaryActionId.REPORT_PREP -> "신고용 상담 준비 카드"
                RecontactBoundaryActionId.RECOVERY_PLAN -> "24시간 수습 계획"
                RecontactBoundaryActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                    "며칠 뒤 다시 확인할 것"
                RecontactBoundaryActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                    "가족 차분 대화 안건"
                RecontactBoundaryActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
            }
        )
}
