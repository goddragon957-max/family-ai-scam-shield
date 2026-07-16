package com.mongmong.familyshield

enum class VerificationOutcomeNextStepsCardId(val displayOrder: Int) {
    UNREACHED_KEEP_STOPPED(1),
    NOT_FROM_THEM_FAMILY_WARNING(2),
    USER_FOUND_INSTITUTION_PATH(3),
    MONEY_CREDENTIAL_ACCOUNT_EXPOSURE(4),
    LINK_APP_SCREEN_SHARE_EXPOSURE(5),
    VALUE_FREE_FAMILY_UPDATE(6)
}

enum class VerificationOutcomeNextStepsActionId {
    QUICK_STOP,
    ACTIVE_CONTACT_EXIT_SCRIPTS,
    CHECKER,
    ANALYSIS_RESULT,
    PRIVACY_CENTER,
    EXPOSURE_IMPACT_TRIAGE,
    SEPARATE_CHANNEL_VERIFICATION_COACH,
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

data class VerificationOutcomeNextStepsAction(
    val id: VerificationOutcomeNextStepsActionId,
    val label: String
)

data class VerificationOutcomeNextStepsCard(
    val id: VerificationOutcomeNextStepsCardId,
    val displayOrder: Int,
    val title: String,
    val outcomeCondition: String,
    val nextStepSentence: String,
    val whyItMatters: String,
    val doNotRecordOrShareBoundaries: List<String>,
    val primaryAction: VerificationOutcomeNextStepsAction,
    val secondaryActions: List<VerificationOutcomeNextStepsAction>
) {
    val routeActions: List<VerificationOutcomeNextStepsAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class VerificationOutcomeNextStepsBoard(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<VerificationOutcomeNextStepsCardId>,
    val recommendedActions: List<VerificationOutcomeNextStepsAction>,
    val cards: List<VerificationOutcomeNextStepsCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<VerificationOutcomeNextStepsCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<VerificationOutcomeNextStepsActionId>
        get() = recommendedActions.map { action -> action.id }
}

object VerificationOutcomeNextStepsBoardBuilder {
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
    ): VerificationOutcomeNextStepsBoard {
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
            VerificationOutcomeNextStepsCardId.UNREACHED_KEEP_STOPPED,
            VerificationOutcomeNextStepsCardId.VALUE_FREE_FAMILY_UPDATE
        )
        if (hasNoAnalysisOrSignals) {
            recommendedCardSet += VerificationOutcomeNextStepsCardId.entries
        }
        if (hasHighRisk) {
            recommendedCardSet += VerificationOutcomeNextStepsCardId.USER_FOUND_INSTITUTION_PATH
            recommendedCardSet +=
                VerificationOutcomeNextStepsCardId.MONEY_CREDENTIAL_ACCOUNT_EXPOSURE
            recommendedCardSet +=
                VerificationOutcomeNextStepsCardId.LINK_APP_SCREEN_SHARE_EXPOSURE
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet += VerificationOutcomeNextStepsCardId.NOT_FROM_THEM_FAMILY_WARNING
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedCardSet += VerificationOutcomeNextStepsCardId.NOT_FROM_THEM_FAMILY_WARNING
            recommendedCardSet += VerificationOutcomeNextStepsCardId.USER_FOUND_INSTITUTION_PATH
        }
        if (hasFinancialSignal) {
            recommendedCardSet += VerificationOutcomeNextStepsCardId.USER_FOUND_INSTITUTION_PATH
            recommendedCardSet +=
                VerificationOutcomeNextStepsCardId.MONEY_CREDENTIAL_ACCOUNT_EXPOSURE
        }
        if (hasCredentialSignal) {
            recommendedCardSet +=
                VerificationOutcomeNextStepsCardId.MONEY_CREDENTIAL_ACCOUNT_EXPOSURE
        }
        if (hasLinkSignal || hasRemoteAccessSignal) {
            recommendedCardSet +=
                VerificationOutcomeNextStepsCardId.LINK_APP_SCREEN_SHARE_EXPOSURE
        }
        if (hasSavedIncident) {
            recommendedCardSet += VerificationOutcomeNextStepsCardId.USER_FOUND_INSTITUTION_PATH
        }
        if (needsReadinessSupport) {
            recommendedCardSet += VerificationOutcomeNextStepsCardId.UNREACHED_KEEP_STOPPED
            recommendedCardSet += VerificationOutcomeNextStepsCardId.VALUE_FREE_FAMILY_UPDATE
        }
        val recommendedCardIds = VerificationOutcomeNextStepsCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            VerificationOutcomeNextStepsActionId.CHECKER
        } else {
            VerificationOutcomeNextStepsActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            VerificationOutcomeNextStepsActionId.QUICK_STOP,
            VerificationOutcomeNextStepsActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
            VerificationOutcomeNextStepsActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
            VerificationOutcomeNextStepsActionId.FOLLOW_UP_CHECK_IN_PLANNER,
            VerificationOutcomeNextStepsActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            recommendedActionSet += currentResultActionId
            recommendedActionSet += VerificationOutcomeNextStepsActionId.PRIVACY_CENTER
            recommendedActionSet += VerificationOutcomeNextStepsActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += VerificationOutcomeNextStepsActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += VerificationOutcomeNextStepsActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += VerificationOutcomeNextStepsActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += VerificationOutcomeNextStepsActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (hasHighRisk) {
            recommendedActionSet += VerificationOutcomeNextStepsActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += VerificationOutcomeNextStepsActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += VerificationOutcomeNextStepsActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += VerificationOutcomeNextStepsActionId.EVIDENCE_KIT
            recommendedActionSet += VerificationOutcomeNextStepsActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += VerificationOutcomeNextStepsActionId.REPORT_PREP
            recommendedActionSet += VerificationOutcomeNextStepsActionId.RECOVERY_PLAN
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += VerificationOutcomeNextStepsActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += VerificationOutcomeNextStepsActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet +=
                VerificationOutcomeNextStepsActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedActionSet += VerificationOutcomeNextStepsActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet += VerificationOutcomeNextStepsActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += VerificationOutcomeNextStepsActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet +=
                VerificationOutcomeNextStepsActionId.FAMILY_CALM_DEBRIEF_AGENDA
        }
        if (hasFinancialSignal) {
            recommendedActionSet += VerificationOutcomeNextStepsActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += VerificationOutcomeNextStepsActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += VerificationOutcomeNextStepsActionId.EVIDENCE_KIT
            recommendedActionSet += VerificationOutcomeNextStepsActionId.OFFICIAL_HELP_DIRECTORY
            if (analysis != null) {
                recommendedActionSet += VerificationOutcomeNextStepsActionId.REPORT_PREP
            }
            recommendedActionSet += VerificationOutcomeNextStepsActionId.RECOVERY_PLAN
            recommendedActionSet += VerificationOutcomeNextStepsActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (hasCredentialSignal) {
            recommendedActionSet += VerificationOutcomeNextStepsActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += VerificationOutcomeNextStepsActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += VerificationOutcomeNextStepsActionId.EVIDENCE_KIT
            recommendedActionSet += VerificationOutcomeNextStepsActionId.RECOVERY_PLAN
        }
        if (hasRemoteAccessSignal) {
            recommendedActionSet += VerificationOutcomeNextStepsActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += VerificationOutcomeNextStepsActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += VerificationOutcomeNextStepsActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += VerificationOutcomeNextStepsActionId.EVIDENCE_KIT
            recommendedActionSet += VerificationOutcomeNextStepsActionId.RECOVERY_PLAN
        }
        if (hasLinkSignal) {
            recommendedActionSet += VerificationOutcomeNextStepsActionId.CHECKER
            recommendedActionSet += VerificationOutcomeNextStepsActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += VerificationOutcomeNextStepsActionId.PRIVACY_CENTER
        }
        if (hasSavedIncident) {
            recommendedActionSet += VerificationOutcomeNextStepsActionId.EVIDENCE_KIT
            if (analysis != null) {
                recommendedActionSet += VerificationOutcomeNextStepsActionId.REPORT_PREP
            }
            recommendedActionSet += VerificationOutcomeNextStepsActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (needsReadinessSupport) {
            recommendedActionSet += VerificationOutcomeNextStepsActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += VerificationOutcomeNextStepsActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += VerificationOutcomeNextStepsActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += VerificationOutcomeNextStepsActionId.FAMILY_READINESS_ROADMAP
            recommendedActionSet +=
                VerificationOutcomeNextStepsActionId.FAMILY_CALM_DEBRIEF_AGENDA
        }
        val recommendedActions = VerificationOutcomeNextStepsActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards(hasAnalysis = analysis != null)
        val recommendationTitle = when {
            analysis == null -> "분석 전에도 가능한 결과별 다음 행동을 보수적으로 보기"
            hasRemoteAccessSignal -> "원격지시·화면 노출 가능성부터 수동 점검하기"
            hasCredentialSignal -> "인증·계정 노출 가능성부터 수동 수습하기"
            hasFinancialSignal || hasHighRisk -> "돈 행동을 멈춘 채 계정·증거·도움 경로 고르기"
            hasFamilyVerificationSignal -> "연락 불가와 가족 경로 불일치 가능성을 함께 보기"
            hasSecrecyImpersonationSignal -> "결과를 비밀로 두지 않고 가족·기관 경로 고르기"
            hasLinkSignal -> "링크·노출 가능성을 값 없이 분류하기"
            else -> "가능한 결과별 다음 수동 행동 고르기"
        }

        return VerificationOutcomeNextStepsBoard(
            title = "확인 결과별 다음 단계 보드",
            intro =
                "현재 연락을 멈추고 별도 경로를 시도한 뒤 생길 수 있는 결과별 다음 행동을 " +
                    "가족이 직접 읽는 로컬 전용·무상태 수동 안내입니다. 결과를 입력하거나 저장하지 않고, " +
                    "사람·번호·링크·기관·계정·기기 상태를 판정하지 않습니다.",
            modeLabel = "가능한 결과만 읽기 · 값 없이 다음 경로 선택",
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

    private fun buildCards(hasAnalysis: Boolean): List<VerificationOutcomeNextStepsCard> {
        fun action(
            id: VerificationOutcomeNextStepsActionId
        ): VerificationOutcomeNextStepsAction = actionFor(id)

        fun currentResultAction(): VerificationOutcomeNextStepsAction = action(
            if (hasAnalysis) {
                VerificationOutcomeNextStepsActionId.ANALYSIS_RESULT
            } else {
                VerificationOutcomeNextStepsActionId.CHECKER
            }
        )

        return listOf(
            VerificationOutcomeNextStepsCard(
                id = VerificationOutcomeNextStepsCardId.UNREACHED_KEEP_STOPPED,
                displayOrder = VerificationOutcomeNextStepsCardId.UNREACHED_KEEP_STOPPED.displayOrder,
                title = "아직 확인 안 됨: 계속 멈춤 유지",
                outcomeCondition =
                    "평소 가족 경로나 사용자가 직접 찾은 기관 경로로 아직 연락이 닿지 않으면",
                nextStepSentence =
                    "현재 연락에 다시 답하거나 돈·정보·링크·앱 행동을 시작하지 말고 멈춤을 유지한 채 별도 경로와 보호자 도움을 직접 고르세요.",
                whyItMatters =
                    "연락이 닿지 않는 상태는 현재 요청을 따라도 된다는 뜻이 아니므로 시간을 두고 독립된 경로를 유지하는 편이 추가 행동을 줄입니다.",
                doNotRecordOrShareBoundaries = listOf(
                    "연락을 기다리며 가족 이름·연락처·정확한 번호·안전 질문 답·콜백 문장 원문을 이 보드에 입력하거나 기록하지 마세요.",
                    "연락이 안 된다는 상태를 현재 상대에게 알려 주거나 상대가 새로 제시한 번호·링크·계정을 확인 경로로 쓰지 마세요."
                ),
                primaryAction = action(
                    VerificationOutcomeNextStepsActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                ),
                secondaryActions = listOf(
                    action(VerificationOutcomeNextStepsActionId.QUICK_STOP),
                    action(VerificationOutcomeNextStepsActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(VerificationOutcomeNextStepsActionId.SAFE_CALL_SCRIPTS),
                    action(VerificationOutcomeNextStepsActionId.TRUSTED_HELPER_REVIEW)
                )
            ),
            VerificationOutcomeNextStepsCard(
                id = VerificationOutcomeNextStepsCardId.NOT_FROM_THEM_FAMILY_WARNING,
                displayOrder =
                    VerificationOutcomeNextStepsCardId.NOT_FROM_THEM_FAMILY_WARNING.displayOrder,
                title = "가족 경로가 아니라고 확인되면: 가족 경고와 증거",
                outcomeCondition =
                    "평소 가족 경로에서 현재 요청이 그 가족에게서 온 것이 아니라고 들으면",
                nextStepSentence =
                    "현재 상대에게 다시 따지거나 결과를 비밀로 두지 말고 가족에게 값 없는 경고를 알린 뒤 증거 보존과 도움 경로를 직접 고르세요.",
                whyItMatters =
                    "가족에게 행동 범주만 알려 두면 같은 연락이 다른 가족에게 이어질 때 추가 노출을 줄이고 필요한 수동 대응을 함께 고를 수 있습니다.",
                doNotRecordOrShareBoundaries = listOf(
                    "누가 무엇이라고 답했는지, 가족 이름·번호·안전 질문 답·목소리·사진·메시지 원문을 이 보드에 적거나 공유하지 마세요.",
                    "이 카드가 신원·번호·채널을 판정하거나 사건을 자동 신고·차단했다고 여기지 마세요."
                ),
                primaryAction = action(VerificationOutcomeNextStepsActionId.FAMILY_ALERT_MESSAGES),
                secondaryActions = listOf(
                    action(VerificationOutcomeNextStepsActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(VerificationOutcomeNextStepsActionId.SAFE_CALL_SCRIPTS),
                    action(VerificationOutcomeNextStepsActionId.EVIDENCE_KIT),
                    action(VerificationOutcomeNextStepsActionId.FAMILY_CALM_DEBRIEF_AGENDA)
                )
            ),
            VerificationOutcomeNextStepsCard(
                id = VerificationOutcomeNextStepsCardId.USER_FOUND_INSTITUTION_PATH,
                displayOrder =
                    VerificationOutcomeNextStepsCardId.USER_FOUND_INSTITUTION_PATH.displayOrder,
                title = "기관·은행 경로 확인 뒤: 공식 도움·신고 준비",
                outcomeCondition =
                    "현재 상대와 무관하게 사용자가 직접 찾은 기관·은행 경로에 문의한 뒤에도 증거 보존·상담·신고 준비가 필요하면",
                nextStepSentence =
                    "현재 상대가 준 번호·링크는 다시 쓰지 말고 기존 도움·증거·신고 준비 화면에서 말할 내용과 보존 순서를 직접 확인하세요.",
                whyItMatters =
                    "연락 경로와 증거·상담 준비를 분리하면 민감한 값을 다시 퍼뜨리지 않고 필요한 기관 문의를 차분히 준비할 수 있습니다.",
                doNotRecordOrShareBoundaries = listOf(
                    "기관 이름·담당자·정확한 번호·검색 결과·URL·접수값이나 답변 내용을 결과값으로 이 보드에 기록하지 마세요.",
                    "이 보드가 기관 경로를 조회·검증하거나 신고 접수·법률·금융 결과를 보장한다고 여기지 마세요."
                ),
                primaryAction = action(
                    VerificationOutcomeNextStepsActionId.OFFICIAL_HELP_DIRECTORY
                ),
                secondaryActions = listOf(
                    action(VerificationOutcomeNextStepsActionId.EVIDENCE_KIT),
                    action(VerificationOutcomeNextStepsActionId.REPORT_PREP),
                    action(VerificationOutcomeNextStepsActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                    action(VerificationOutcomeNextStepsActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            VerificationOutcomeNextStepsCard(
                id = VerificationOutcomeNextStepsCardId.MONEY_CREDENTIAL_ACCOUNT_EXPOSURE,
                displayOrder =
                    VerificationOutcomeNextStepsCardId.MONEY_CREDENTIAL_ACCOUNT_EXPOSURE.displayOrder,
                title = "돈·인증·계정 노출 가능성: 계정·송금 수습",
                outcomeCondition =
                    "송금·결제, 계정·카드 값, 비밀번호·OTP·PIN 같은 인증 비밀이 이미 노출됐을 가능성이 있으면",
                nextStepSentence =
                    "추가 송금·결제·로그인을 멈추고 송금 멈춤, 계정 보호, 증거 보존, 24시간 수습과 후속 점검 경로를 직접 고르세요.",
                whyItMatters =
                    "노출 가능성만 있어도 추가 행동을 보류하고 기존 보호 경로를 나누어 확인하면 더 많은 값이나 돈이 이어지는 일을 줄일 수 있습니다.",
                doNotRecordOrShareBoundaries = listOf(
                    "금액·계좌·카드·비밀번호·OTP·PIN·접수번호·거래값이나 기관 답변을 이 보드에 입력·저장·공유하지 마세요.",
                    "이 카드가 송금·결제를 취소하거나 계정·카드를 정지·동결하거나 환급·회수를 끝냈다고 여기지 마세요."
                ),
                primaryAction = action(
                    VerificationOutcomeNextStepsActionId.MONEY_TRANSFER_PAUSE_GATE
                ),
                secondaryActions = listOf(
                    action(VerificationOutcomeNextStepsActionId.ACCOUNT_LOCKDOWN),
                    action(VerificationOutcomeNextStepsActionId.EVIDENCE_KIT),
                    action(VerificationOutcomeNextStepsActionId.RECOVERY_PLAN),
                    action(VerificationOutcomeNextStepsActionId.FOLLOW_UP_CHECK_IN_PLANNER)
                )
            ),
            VerificationOutcomeNextStepsCard(
                id = VerificationOutcomeNextStepsCardId.LINK_APP_SCREEN_SHARE_EXPOSURE,
                displayOrder =
                    VerificationOutcomeNextStepsCardId.LINK_APP_SCREEN_SHARE_EXPOSURE.displayOrder,
                title = "링크·앱·화면공유 노출 가능성: 원격·노출 점검",
                outcomeCondition =
                    "링크 열기·앱 설치·화면공유·원격지시·계정 화면 노출 가능성이 있으면",
                nextStepSentence =
                    "현재 연락·링크·앱 조작을 멈추고 원격지시 중단, 노출 종류, 보호자 검토와 개인정보 경로를 별도로 직접 확인하세요.",
                whyItMatters =
                    "가능한 노출을 종류별 수동 경로로 나누면 기기나 링크 상태를 성급히 단정하지 않고 계정·증거 행동을 이어 갈 수 있습니다.",
                doNotRecordOrShareBoundaries = listOf(
                    "정확한 URL·도메인·앱 이름·패키지 이름·기기 식별자·계정 화면·비밀번호·인증값을 이 보드에 넣거나 공유하지 마세요.",
                    "이 카드가 링크를 스캔하거나 앱 목록을 조회하거나 원격 연결 해제·악성 프로그램 제거·기기 정리를 수행했다고 여기지 마세요."
                ),
                primaryAction = action(
                    VerificationOutcomeNextStepsActionId.REMOTE_ACCESS_CUTOFF_COACH
                ),
                secondaryActions = listOf(
                    action(VerificationOutcomeNextStepsActionId.EXPOSURE_IMPACT_TRIAGE),
                    currentResultAction(),
                    action(VerificationOutcomeNextStepsActionId.PRIVACY_CENTER),
                    action(VerificationOutcomeNextStepsActionId.TRUSTED_HELPER_REVIEW)
                )
            ),
            VerificationOutcomeNextStepsCard(
                id = VerificationOutcomeNextStepsCardId.VALUE_FREE_FAMILY_UPDATE,
                displayOrder =
                    VerificationOutcomeNextStepsCardId.VALUE_FREE_FAMILY_UPDATE.displayOrder,
                title = "가족에게 값 없는 결과 공유와 후속 점검",
                outcomeCondition =
                    "가족이 현재 상태와 다음 수동 행동을 함께 정해야 하면",
                nextStepSentence =
                    "실제 확인 답이나 결과값 없이 ‘멈춤 유지·가족 경고·기관 도움·계정·기기 점검·후속 점검’ 같은 행동 범주만 가족에게 알리세요.",
                whyItMatters =
                    "행동 범주만 공유하면 비밀 유지 압박을 끊고 가족 도움을 받으면서 원문과 민감한 값을 다시 퍼뜨리지 않을 수 있습니다.",
                doNotRecordOrShareBoundaries = listOf(
                    "원문·이름·연락처·번호·링크·OTP·PIN·비밀번호·계정·카드·기기 식별자·가족 안전 계획 값을 넣지 마세요.",
                    "확인 결과·누가 답했는지·공식 번호·담당자·접수값을 저장하거나 체크하지 말고 받는 사람과 전송 여부는 사용자가 직접 고르세요."
                ),
                primaryAction = action(VerificationOutcomeNextStepsActionId.FAMILY_ALERT_MESSAGES),
                secondaryActions = listOf(
                    action(VerificationOutcomeNextStepsActionId.FAMILY_CALM_DEBRIEF_AGENDA),
                    action(VerificationOutcomeNextStepsActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                    action(VerificationOutcomeNextStepsActionId.FAMILY_READINESS_ROADMAP),
                    action(VerificationOutcomeNextStepsActionId.SAFETY_FLOW_ROUTER)
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
                    "현재 분석이 없거나 일치 신호가 없습니다. 이를 사람·번호·링크·기관·계정에 " +
                        "문제가 없다는 판정으로 보지 말고 가능한 결과 카드를 보수적으로 읽으세요."
                )
            }
            if (hasHighRisk) {
                add(
                    "위험도가 높아 돈·계정·기기·증거·기관 도움의 가능한 다음 경로를 함께 권합니다. " +
                        "현재 요청의 정체나 노출 여부를 확정하는 뜻은 아닙니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 긴급상황·목소리 합성·번호 변경 신호가 있어 아직 연락이 닿지 않는 경우와 " +
                        "가족 경로의 요청이 아닌 경우를 함께 읽고 값 없는 가족 경고·확인 전화·별도 경로를 권합니다."
                )
            }
            if (hasSecrecyImpersonationSignal) {
                add(
                    "비밀 유지·기관 사칭 신호가 있어 결과를 혼자 감추지 않고 연락 종료·가족 알림·" +
                        "사용자가 직접 찾을 기관 도움 경로를 권합니다."
                )
            }
            if (hasFinancialSignal) {
                add(
                    "송금·결제·금융 압박 신호가 있어 돈 행동을 멈추고 계정·증거·기관 도움·신고 준비·" +
                        "24시간 수습·후속 점검 경로를 권합니다. 취소·정지·동결·환급·회수를 대신하거나 보장하지 않습니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증 비밀 요구 가능성이 있어 계정 보호와 원격지시 중단 경로를 권합니다. " +
                        "계정이나 카드를 자동 정지·동결하는 기능은 아닙니다."
                )
            }
            if (hasLinkSignal) {
                add(
                    "링크·스미싱 신호가 있어 검사기·노출 분류·개인정보 경로를 권합니다. " +
                        "이 보드는 링크를 스캔하거나 URL·도메인을 검증하지 않습니다."
                )
            }
            if (hasRemoteAccessSignal) {
                add(
                    "앱 설치·화면공유·원격지시 신호가 있어 원격지시 중단·보호자·계정·증거 경로를 " +
                        "권합니다. 원격 연결 해제·악성 프로그램 제거·기기 정리를 수행하거나 보장하지 않습니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만 반영하며 사건 내용은 읽지 않습니다. " +
                        "값 없는 증거·신고 준비·후속 점검 경로만 함께 권합니다."
                )
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW ->
                "가족 준비가 부족한 상태라 값 없는 확인 전화·가족 알림·보호자 검토·준비 로드맵을 함께 권합니다. 실제 가족 값은 입력하지 마세요."
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중이라 값 없는 확인 전화·가족 알림·보호자 검토·준비 로드맵으로 빠진 부분을 직접 살피도록 권합니다. 저장된 실제 값은 추천에 사용하지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있어도 이번 사람·번호·링크·기관·계정·기기 상태나 결과를 확인했다는 뜻은 아닙니다. 가능한 결과를 가정해 수동 경로를 고르세요."
        }
        return listOf(situationCopy, readinessCopy).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 보드는 사용자가 화면을 직접 읽고 직접 선택하는 로컬 전용·무상태 수동 안내이며 결과 입력을 요구하지 않습니다.",
        "추천은 위험도·일치 신호, 값 없는 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·사건 내용·가족 안전 계획 값을 사용하지 않습니다.",
        "이름·연락처·정확한 전화번호·계정·계좌·카드·OTP·PIN·비밀번호·가족 확인 답·주민번호·기기 식별자·정확한 URL·도메인·공식 번호·앱 이름·패키지 이름·확인 결과값을 묻거나 저장하지 않습니다.",
        "카드 선택·추천·읽음·공유·확인 결과·결과 상태·채널 값·공식 번호·앱 이름·기기 상태·체크·메모·진행·이력을 위한 새 저장소를 만들거나 기록하지 않습니다.",
        "문자·메시지·통화·연락처·클립보드·계정·기기 화면을 읽거나 감시하지 않고 연락처를 읽거나 쓰거나 고르지 않습니다.",
        "네트워크·웹 탐색·지도·위치·기관 번호 조회·공식 경로 조회·전화번호 검증·링크·URL·도메인 검증·링크 스캔을 사용하지 않습니다.",
        "새 Android 권한을 요청하지 않습니다.",
        "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커를 만들거나 예약하지 않으며 스케줄러를 사용하지 않습니다.",
        "전화 앱을 열거나 전화를 자동으로 걸거나 끊거나 선별·차단하지 않고 문자·SMS를 보내거나 읽지 않으며 받는 사람을 고르거나 공유를 자동 전송하지 않습니다.",
        "클립보드를 읽거나 쓰지 않고 확인 답·결과·번호·링크를 복사하거나 붙여넣지 않습니다.",
        "기기 관리자·접근성 서비스·VPN·알림 접근·사용 기록·앱 작업 권한이나 기기 제어 기능을 사용하지 않고 설치 앱·패키지 목록을 조회·설치·제거하거나 기기 설정을 바꾸지 않습니다.",
        "결제·은행 기능이나 API, NFC나 Android 계정 관리자에 접근하지 않고 송금·결제·취소·계정 또는 카드 정지·동결·환급·피해 복구를 자동 수행하지 않습니다.",
        "붙여넣은 글, 통화 상태, 연락처, 사건 기록, 가족 안전 계획, 기관 목록, 계정·카드·은행·기기 설정, 설치 앱·패키지 상태, 네트워크·VPN·알림 상태, 돈의 움직임이나 확인 결과를 추가·수정·삭제하지 않습니다.",
        "원문·화면·연락·번호·링크·기기·계정·결과를 자동으로 가리거나 모니터링·스캔·삭제·차단·호출·메시지 전송·조회·확인·검증·저장하지 않습니다.",
        "이 안내는 본인·번호·링크·기관 확인, 사기 여부, 조치 완료, 악성 프로그램 제거, 원격 연결 종료, 기기 정리, 계정·카드 조치, 결제 취소·환급·회복 또는 법률·금융 결과를 증명하거나 보장하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<VerificationOutcomeNextStepsCardId>,
        recommendedActions: List<VerificationOutcomeNextStepsAction>,
        cards: List<VerificationOutcomeNextStepsCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 확인 결과별 다음 단계 보드")
            appendLine()
            appendLine("가능한 결과별 다음 수동 경로를 함께 보고 있습니다.")
            appendLine()
            appendLine("추천 결과 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("가족에게 알릴 값 없는 문장")
            appendLine("- 현재 연락에서 더 행동하지 않고 가능한 결과별 다음 수동 경로를 직접 고르고 있습니다.")
            append(
                "원문·사람·연락처·번호·링크·인증·계정·카드·기기·가족 계획·확인 답·결과값 없이 " +
                    "고정된 카드와 기존 앱 경로 이름만 담았습니다."
            )
        }.trim()
    }

    private fun actionFor(
        id: VerificationOutcomeNextStepsActionId
    ): VerificationOutcomeNextStepsAction = VerificationOutcomeNextStepsAction(
        id = id,
        label = when (id) {
            VerificationOutcomeNextStepsActionId.QUICK_STOP -> "긴급 멈춤 모드"
            VerificationOutcomeNextStepsActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                "진행 중 연락 종료 문장"
            VerificationOutcomeNextStepsActionId.CHECKER -> "의심 내용 검사"
            VerificationOutcomeNextStepsActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            VerificationOutcomeNextStepsActionId.PRIVACY_CENTER -> "개인정보 센터"
            VerificationOutcomeNextStepsActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
            VerificationOutcomeNextStepsActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                "별도 경로 확인 코치"
            VerificationOutcomeNextStepsActionId.SAFE_CALL_SCRIPTS ->
                "값 없는 가족 콜백·확인 전화 스크립트"
            VerificationOutcomeNextStepsActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
            VerificationOutcomeNextStepsActionId.TRUSTED_HELPER_REVIEW ->
                "보호자와 함께 안전 검토"
            VerificationOutcomeNextStepsActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
            VerificationOutcomeNextStepsActionId.MONEY_TRANSFER_PAUSE_GATE -> "송금 전 멈춤 확인"
            VerificationOutcomeNextStepsActionId.ACCOUNT_LOCKDOWN ->
                "계정·기기 잠금 체크리스트"
            VerificationOutcomeNextStepsActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                "화면공유·원격지시 끊기 코치"
            VerificationOutcomeNextStepsActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            VerificationOutcomeNextStepsActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처"
            VerificationOutcomeNextStepsActionId.REPORT_PREP -> "신고용 상담 준비 카드"
            VerificationOutcomeNextStepsActionId.RECOVERY_PLAN -> "24시간 수습 계획"
            VerificationOutcomeNextStepsActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                "며칠 뒤 다시 확인할 것"
            VerificationOutcomeNextStepsActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                "가족 차분 대화 안건"
            VerificationOutcomeNextStepsActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
