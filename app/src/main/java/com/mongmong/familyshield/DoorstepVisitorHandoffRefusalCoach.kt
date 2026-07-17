package com.mongmong.familyshield

enum class DoorstepVisitorHandoffRefusalCardId(val displayOrder: Int) {
    PAUSE_VISIT_OR_COLLECTION_INSTRUCTION_BEFORE_OPENING(1),
    REFUSE_CASH_CARD_BANKBOOK_DEVICE_DOCUMENT_OR_ENVELOPE_HANDOFF(2),
    USE_VALUE_FREE_REFUSAL_AT_INTERCOM_DOOR_OR_LOBBY(3),
    END_LIVE_CONTACT_WITHOUT_GUIDING_VISITOR(4),
    SEPARATE_OFFICIAL_TITLE_DOCUMENT_AND_CASE_PRESSURE(5),
    CHOOSE_FAMILY_BUILDING_HELP_AND_FOLLOW_UP_ROUTE(6)
}

enum class DoorstepVisitorHandoffRefusalActionId {
    QUICK_STOP,
    CHECKER,
    ANALYSIS_RESULT,
    SAFETY_PLAN,
    FAMILY_READINESS_ROADMAP,
    FAMILY_SAFETY_DRILL_COACH,
    SAFE_PASTE_PRIVACY_COACH,
    ACTIVE_CONTACT_EXIT_SCRIPTS,
    FAMILY_CALLBACK_CARD,
    FAMILY_SAFE_WORD_RESET_COACH,
    SEPARATE_CHANNEL_VERIFICATION_COACH,
    SAFE_CALL_SCRIPTS,
    VOICE_IMPERSONATION_REALITY_CHECK_COACH,
    NEW_NUMBER_QUARANTINE_COACH,
    CREDENTIAL_SECRET_FREEZE_COACH,
    FAMILY_ALERT_MESSAGES,
    VERIFICATION_OUTCOME_NEXT_STEPS_BOARD,
    RECONTACT_BOUNDARY_COACH,
    TRUSTED_HELPER_REVIEW,
    FAMILY_RESPONSE_ROLE_BOARD,
    FAMILY_CALM_DEBRIEF_AGENDA,
    CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH,
    BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH,
    MONEY_TRANSFER_PAUSE_GATE,
    GIFT_CARD_CODE_PAUSE_COACH,
    LINK_APP_INSTALL_QUARANTINE_COACH,
    OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
    RECOVERY_PROMISE_REALITY_CHECK_COACH,
    ACCOUNT_LOCKDOWN,
    REMOTE_ACCESS_CUTOFF_COACH,
    EXPOSURE_IMPACT_TRIAGE,
    PRIVACY_CENTER,
    EVIDENCE_HANDOFF_PRIVACY_COACH,
    EVIDENCE_KIT,
    INCIDENT_TIMELINE_BRIEFING,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    FOLLOW_UP_CHECK_IN_PLANNER,
    SAFETY_FLOW_ROUTER
}

data class DoorstepVisitorHandoffRefusalAction(
    val id: DoorstepVisitorHandoffRefusalActionId,
    val label: String
)

data class DoorstepVisitorHandoffRefusalCard(
    val id: DoorstepVisitorHandoffRefusalCardId,
    val displayOrder: Int,
    val title: String,
    val doorstepHandoffPressureSituation: String,
    val valueFreeRefusalScript: String,
    val whyItMatters: String,
    val doNotOpenLetInHandoffSayShareAttachOrRecordBoundaries: List<String>,
    val primaryAction: DoorstepVisitorHandoffRefusalAction,
    val secondaryActions: List<DoorstepVisitorHandoffRefusalAction>
) {
    val routeActions: List<DoorstepVisitorHandoffRefusalAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class DoorstepVisitorHandoffRefusalCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<DoorstepVisitorHandoffRefusalCardId>,
    val recommendedActions: List<DoorstepVisitorHandoffRefusalAction>,
    val cards: List<DoorstepVisitorHandoffRefusalCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<DoorstepVisitorHandoffRefusalCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<DoorstepVisitorHandoffRefusalActionId>
        get() = recommendedActions.map { action -> action.id }
}

object DoorstepVisitorHandoffRefusalCoachBuilder {
    private val financialSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.BAIT,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val officialPressureSignals = setOf(
        ScamSignal.IMPERSONATION,
        ScamSignal.SECRECY,
        ScamSignal.SAFE_ACCOUNT_FRAUD
    )

    private val liveLinkAppCourierSignals = setOf(
        ScamSignal.LIVE_CALL_CONTROL,
        ScamSignal.APP_INSTALL,
        ScamSignal.LINK,
        ScamSignal.PARCEL_INVITATION_SMISHING
    )

    private val credentialSignals = setOf(
        ScamSignal.CREDENTIALS,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val recoveryPromiseSignals = setOf(
        ScamSignal.BAIT,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val familyVerificationSignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.PHONE_NUMBER_CHANGE
    )

    fun build(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness,
        incidentCount: Int = 0
    ): DoorstepVisitorHandoffRefusalCoach {
        val riskLevel = analysis?.riskLevel
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val readinessLevel = readiness.level
        val hasNoAnalysisOrSignals = analysis == null || signals.isEmpty()
        val hasHighRisk = riskLevel == RiskLevel.HIGH
        val hasFinancialSignal = signals.any { signal -> signal in financialSignals }
        val hasOfficialPressureSignal = signals.any { signal ->
            signal in officialPressureSignals
        }
        val hasLiveLinkAppCourierSignal = signals.any { signal ->
            signal in liveLinkAppCourierSignals
        }
        val hasCredentialSignal = signals.any { signal -> signal in credentialSignals }
        val hasRecoveryPromiseSignal = signals.any { signal ->
            signal in recoveryPromiseSignals
        }
        val hasFamilyVerificationSignal = signals.any { signal ->
            signal in familyVerificationSignals
        }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0

        val recommendedCardSet = linkedSetOf(
            DoorstepVisitorHandoffRefusalCardId
                .PAUSE_VISIT_OR_COLLECTION_INSTRUCTION_BEFORE_OPENING,
            DoorstepVisitorHandoffRefusalCardId
                .REFUSE_CASH_CARD_BANKBOOK_DEVICE_DOCUMENT_OR_ENVELOPE_HANDOFF,
            DoorstepVisitorHandoffRefusalCardId
                .USE_VALUE_FREE_REFUSAL_AT_INTERCOM_DOOR_OR_LOBBY
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += DoorstepVisitorHandoffRefusalCardId.entries
        }
        if (hasFinancialSignal || hasRecoveryPromiseSignal) {
            recommendedCardSet += DoorstepVisitorHandoffRefusalCardId
                .PAUSE_VISIT_OR_COLLECTION_INSTRUCTION_BEFORE_OPENING
            recommendedCardSet +=
                DoorstepVisitorHandoffRefusalCardId
                    .REFUSE_CASH_CARD_BANKBOOK_DEVICE_DOCUMENT_OR_ENVELOPE_HANDOFF
            recommendedCardSet += DoorstepVisitorHandoffRefusalCardId
                .END_LIVE_CONTACT_WITHOUT_GUIDING_VISITOR
            recommendedCardSet +=
                DoorstepVisitorHandoffRefusalCardId
                    .SEPARATE_OFFICIAL_TITLE_DOCUMENT_AND_CASE_PRESSURE
            recommendedCardSet +=
                DoorstepVisitorHandoffRefusalCardId
                    .CHOOSE_FAMILY_BUILDING_HELP_AND_FOLLOW_UP_ROUTE
        }
        if (hasOfficialPressureSignal) {
            recommendedCardSet +=
                DoorstepVisitorHandoffRefusalCardId
                    .REFUSE_CASH_CARD_BANKBOOK_DEVICE_DOCUMENT_OR_ENVELOPE_HANDOFF
            recommendedCardSet +=
                DoorstepVisitorHandoffRefusalCardId
                    .USE_VALUE_FREE_REFUSAL_AT_INTERCOM_DOOR_OR_LOBBY
            recommendedCardSet +=
                DoorstepVisitorHandoffRefusalCardId
                    .SEPARATE_OFFICIAL_TITLE_DOCUMENT_AND_CASE_PRESSURE
            recommendedCardSet +=
                DoorstepVisitorHandoffRefusalCardId
                    .CHOOSE_FAMILY_BUILDING_HELP_AND_FOLLOW_UP_ROUTE
        }
        if (hasLiveLinkAppCourierSignal) {
            recommendedCardSet +=
                DoorstepVisitorHandoffRefusalCardId
                    .USE_VALUE_FREE_REFUSAL_AT_INTERCOM_DOOR_OR_LOBBY
            recommendedCardSet += DoorstepVisitorHandoffRefusalCardId
                .END_LIVE_CONTACT_WITHOUT_GUIDING_VISITOR
            recommendedCardSet +=
                DoorstepVisitorHandoffRefusalCardId
                    .CHOOSE_FAMILY_BUILDING_HELP_AND_FOLLOW_UP_ROUTE
        }
        if (hasCredentialSignal) {
            recommendedCardSet +=
                DoorstepVisitorHandoffRefusalCardId
                    .REFUSE_CASH_CARD_BANKBOOK_DEVICE_DOCUMENT_OR_ENVELOPE_HANDOFF
            recommendedCardSet +=
                DoorstepVisitorHandoffRefusalCardId
                    .CHOOSE_FAMILY_BUILDING_HELP_AND_FOLLOW_UP_ROUTE
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet +=
                DoorstepVisitorHandoffRefusalCardId
                    .USE_VALUE_FREE_REFUSAL_AT_INTERCOM_DOOR_OR_LOBBY
            recommendedCardSet +=
                DoorstepVisitorHandoffRefusalCardId
                    .SEPARATE_OFFICIAL_TITLE_DOCUMENT_AND_CASE_PRESSURE
            recommendedCardSet +=
                DoorstepVisitorHandoffRefusalCardId
                    .CHOOSE_FAMILY_BUILDING_HELP_AND_FOLLOW_UP_ROUTE
        }
        if (hasSavedIncident) {
            recommendedCardSet +=
                DoorstepVisitorHandoffRefusalCardId
                    .CHOOSE_FAMILY_BUILDING_HELP_AND_FOLLOW_UP_ROUTE
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedCardSet +=
                    DoorstepVisitorHandoffRefusalCardId
                        .USE_VALUE_FREE_REFUSAL_AT_INTERCOM_DOOR_OR_LOBBY
                recommendedCardSet +=
                    DoorstepVisitorHandoffRefusalCardId
                        .CHOOSE_FAMILY_BUILDING_HELP_AND_FOLLOW_UP_ROUTE
            }
            ReadinessLevel.READY -> {
                recommendedCardSet += DoorstepVisitorHandoffRefusalCardId
                    .PAUSE_VISIT_OR_COLLECTION_INSTRUCTION_BEFORE_OPENING
                recommendedCardSet +=
                    DoorstepVisitorHandoffRefusalCardId
                        .REFUSE_CASH_CARD_BANKBOOK_DEVICE_DOCUMENT_OR_ENVELOPE_HANDOFF
            }
        }
        val recommendedCardIds = DoorstepVisitorHandoffRefusalCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            DoorstepVisitorHandoffRefusalActionId.CHECKER
        } else {
            DoorstepVisitorHandoffRefusalActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            currentResultActionId,
            DoorstepVisitorHandoffRefusalActionId.QUICK_STOP,
            DoorstepVisitorHandoffRefusalActionId
                .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH,
            DoorstepVisitorHandoffRefusalActionId
                .BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH,
            DoorstepVisitorHandoffRefusalActionId.MONEY_TRANSFER_PAUSE_GATE,
            DoorstepVisitorHandoffRefusalActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
            DoorstepVisitorHandoffRefusalActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
            DoorstepVisitorHandoffRefusalActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
            DoorstepVisitorHandoffRefusalActionId.TRUSTED_HELPER_REVIEW,
            DoorstepVisitorHandoffRefusalActionId.PRIVACY_CENTER,
            DoorstepVisitorHandoffRefusalActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            DoorstepVisitorHandoffRefusalActionId.entries
                .filterNot { actionId ->
                    actionId == DoorstepVisitorHandoffRefusalActionId.ANALYSIS_RESULT
                }
                .forEach { actionId -> recommendedActionSet += actionId }
        }
        if (hasHighRisk) {
            DoorstepVisitorHandoffRefusalActionId.entries.forEach { actionId ->
                recommendedActionSet += actionId
            }
        }
        if (hasFinancialSignal) {
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId
                .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId
                .BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.REPORT_PREP
        }
        if (hasOfficialPressureSignal) {
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.REPORT_PREP
        }
        if (hasLiveLinkAppCourierSignal) {
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.LINK_APP_INSTALL_QUARANTINE_COACH
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.PRIVACY_CENTER
        }
        if (hasCredentialSignal) {
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.GIFT_CARD_CODE_PAUSE_COACH
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.EXPOSURE_IMPACT_TRIAGE
        }
        if (hasRecoveryPromiseSignal) {
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId
                .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.REPORT_PREP
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId
                .VOICE_IMPERSONATION_REALITY_CHECK_COACH
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.NEW_NUMBER_QUARANTINE_COACH
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.FAMILY_SAFE_WORD_RESET_COACH
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasSavedIncident) {
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.EVIDENCE_KIT
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.INCIDENT_TIMELINE_BRIEFING
            recommendedActionSet +=
                DoorstepVisitorHandoffRefusalActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.SAFETY_PLAN
                recommendedActionSet +=
                    DoorstepVisitorHandoffRefusalActionId.FAMILY_SAFE_WORD_RESET_COACH
                recommendedActionSet +=
                    DoorstepVisitorHandoffRefusalActionId.FAMILY_READINESS_ROADMAP
                recommendedActionSet +=
                    DoorstepVisitorHandoffRefusalActionId.FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.SAFE_CALL_SCRIPTS
            }
            ReadinessLevel.READY -> {
                recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.FAMILY_CALLBACK_CARD
                recommendedActionSet +=
                    DoorstepVisitorHandoffRefusalActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                recommendedActionSet += DoorstepVisitorHandoffRefusalActionId.SAFE_CALL_SCRIPTS
            }
        }
        val recommendedActions = DoorstepVisitorHandoffRefusalActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null -> "검사 결과가 없어도 모든 문 앞 거절 문구를 보수적으로 읽기"
            hasRecoveryPromiseSignal -> "회복 약속과 방문·수거 지시를 함께 멈추기"
            hasFinancialSignal -> "문을 열거나 돈·물품을 건네기 전에 짧게 거절하기"
            hasOfficialPressureSignal -> "직함·문서·사건 압박과 문 앞 요청을 분리하기"
            hasLiveLinkAppCourierSignal -> "연락을 끝내고 방문자에게 위치나 행동을 안내하지 않기"
            hasCredentialSignal -> "비밀값과 기기를 보여 주지 않고 인계 요청 거절하기"
            hasFamilyVerificationSignal -> "가족 위급 주장도 문을 열지 않고 별도 경로로 확인하기"
            hasHighRisk -> "높은 위험 범주에서 모든 거절 문구와 기존 대응 경로 보기"
            hasSavedIncident -> "사건 내용 없이 증거·가족·도움·후속 경로만 고르기"
            else -> "문을 열거나 값을 말하기 전에 짧은 거절 문구 고르기"
        }

        return DoorstepVisitorHandoffRefusalCoach(
            title = "문 앞 방문자·자택 인계 거절 코치",
            intro =
                "누군가 집·건물·직장 문 앞이나 로비에 온다고 하거나 이미 기다리며 출입·" +
                    "현금·카드·통장·기기·서류·봉투 인계를 요구할 때 주소·사람·금액·사건 " +
                    "값을 받지 않고 직접 읽을 고정 거절 문장과 기존 대응 화면만 고르는 " +
                    "로컬 전용·무상태 수동 안내입니다.",
            modeLabel = "값 입력 없음 · 문/방문자 접근 없음 · 인계 행동 없음",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                readinessLevel = readinessLevel,
                hasNoAnalysisOrSignals = hasNoAnalysisOrSignals,
                hasHighRisk = hasHighRisk,
                hasFinancialSignal = hasFinancialSignal,
                hasOfficialPressureSignal = hasOfficialPressureSignal,
                hasLiveLinkAppCourierSignal = hasLiveLinkAppCourierSignal,
                hasCredentialSignal = hasCredentialSignal,
                hasRecoveryPromiseSignal = hasRecoveryPromiseSignal,
                hasFamilyVerificationSignal = hasFamilyVerificationSignal,
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

    private fun buildCards(): List<DoorstepVisitorHandoffRefusalCard> {
        fun action(
            id: DoorstepVisitorHandoffRefusalActionId
        ): DoorstepVisitorHandoffRefusalAction = actionFor(id)

        return listOf(
            DoorstepVisitorHandoffRefusalCard(
                id = DoorstepVisitorHandoffRefusalCardId
                    .PAUSE_VISIT_OR_COLLECTION_INSTRUCTION_BEFORE_OPENING,
                displayOrder = DoorstepVisitorHandoffRefusalCardId
                    .PAUSE_VISIT_OR_COLLECTION_INSTRUCTION_BEFORE_OPENING.displayOrder,
                title = "문을 열기 전 방문·수거 지시 멈추기",
                doorstepHandoffPressureSituation =
                    "누군가 곧 도착하거나 밖에서 기다린다며 문·공동 출입구를 열고 바로 응대하라고 재촉하는 때",
                valueFreeRefusalScript =
                    "지금 문을 열거나 방문·수거 지시를 따르지 않겠습니다. 다음 행동은 이 연락과 분리해 직접 고르겠습니다.",
                whyItMatters =
                    "출입보다 먼저 멈추면 상대가 정한 속도에서 벗어나 값이나 물품을 더 내주지 않고 기존 안전 경로를 고를 시간이 생깁니다.",
                doNotOpenLetInHandoffSayShareAttachOrRecordBoundaries = listOf(
                    "문·공동 출입구를 열거나 사람을 들이지 말고 집·건물·로비·호수·주소·경로·도착 위치 값을 말하거나 적지 마세요.",
                    "앱은 문·인터폰·CCTV·카메라·도어벨·홈 자동화에 접근하지 않고 방문자 신원·위치·경로를 확인하지 않습니다."
                ),
                primaryAction = action(DoorstepVisitorHandoffRefusalActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH
                    ),
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH
                    ),
                    action(DoorstepVisitorHandoffRefusalActionId.MONEY_TRANSFER_PAUSE_GATE),
                    action(DoorstepVisitorHandoffRefusalActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    )
                )
            ),
            DoorstepVisitorHandoffRefusalCard(
                id = DoorstepVisitorHandoffRefusalCardId
                    .REFUSE_CASH_CARD_BANKBOOK_DEVICE_DOCUMENT_OR_ENVELOPE_HANDOFF,
                displayOrder = DoorstepVisitorHandoffRefusalCardId
                    .REFUSE_CASH_CARD_BANKBOOK_DEVICE_DOCUMENT_OR_ENVELOPE_HANDOFF.displayOrder,
                title = "현금·카드·통장·휴대폰·서류·봉투 인계 거절하기",
                doorstepHandoffPressureSituation =
                    "문밖 사람이 보관·확인·보호·처리를 이유로 돈이나 물품을 직접 넘기라고 하는 때",
                valueFreeRefusalScript =
                    "어떤 돈이나 물품도 건네지 않겠습니다. 문을 열지 않고 이 요청을 여기서 멈추겠습니다.",
                whyItMatters =
                    "짧은 인계 거절은 금액·계정·기기·문서 내용을 다시 드러내지 않으면서 대면 회수 흐름을 끊습니다.",
                doNotOpenLetInHandoffSayShareAttachOrRecordBoundaries = listOf(
                    "현금·권종·일련값·카드·통장·계정·기기·서류·봉투·물품·코드 값을 문밖에 보여 주거나 건네지 마세요.",
                    "인출·입금·송금·결제·카드·계정·수거·배달 동작을 하지 않고 금액·은행·방문자·기사·물품 상태를 조회하지 않습니다."
                ),
                primaryAction = action(
                    DoorstepVisitorHandoffRefusalActionId
                        .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH
                ),
                secondaryActions = listOf(
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH
                    ),
                    action(DoorstepVisitorHandoffRefusalActionId.MONEY_TRANSFER_PAUSE_GATE),
                    action(DoorstepVisitorHandoffRefusalActionId.GIFT_CARD_CODE_PAUSE_COACH),
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .CREDENTIAL_SECRET_FREEZE_COACH
                    ),
                    action(DoorstepVisitorHandoffRefusalActionId.ACCOUNT_LOCKDOWN),
                    action(DoorstepVisitorHandoffRefusalActionId.EXPOSURE_IMPACT_TRIAGE)
                )
            ),
            DoorstepVisitorHandoffRefusalCard(
                id = DoorstepVisitorHandoffRefusalCardId
                    .USE_VALUE_FREE_REFUSAL_AT_INTERCOM_DOOR_OR_LOBBY,
                displayOrder = DoorstepVisitorHandoffRefusalCardId
                    .USE_VALUE_FREE_REFUSAL_AT_INTERCOM_DOOR_OR_LOBBY.displayOrder,
                title = "인터폰·문 앞·로비에서 값 없는 거절 문장만 말하기",
                doorstepHandoffPressureSituation =
                    "상대가 직함·사건·문서·택배를 내세우며 이름·주소·물품·금액 설명이나 출입을 요구하는 때",
                valueFreeRefusalScript =
                    "문을 열거나 값을 말하지 않겠습니다. 방문 요청은 받아들이지 않고 별도 도움 경로만 직접 고르겠습니다.",
                whyItMatters =
                    "고정된 값 없는 문장은 대화를 길게 이어 상대에게 신원·거주·물품 단서를 더 주는 일을 줄입니다.",
                doNotOpenLetInHandoffSayShareAttachOrRecordBoundaries = listOf(
                    "이름·연락처·주소·호수·현재 위치·가족·문서·사건·물품 값을 말하거나 화면·사진·파일로 보여 주지 마세요.",
                    "인터폰·마이크·카메라·도어벨·CCTV를 듣거나 촬영·녹음하지 않고 사람·직함·문서·사건을 검증하지 않습니다."
                ),
                primaryAction = action(
                    DoorstepVisitorHandoffRefusalActionId
                        .OFFICIAL_PRESSURE_REALITY_CHECK_COACH
                ),
                secondaryActions = listOf(
                    action(DoorstepVisitorHandoffRefusalActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(DoorstepVisitorHandoffRefusalActionId.FAMILY_ALERT_MESSAGES),
                    action(DoorstepVisitorHandoffRefusalActionId.TRUSTED_HELPER_REVIEW),
                    action(DoorstepVisitorHandoffRefusalActionId.SAFE_CALL_SCRIPTS)
                )
            ),
            DoorstepVisitorHandoffRefusalCard(
                id = DoorstepVisitorHandoffRefusalCardId
                    .END_LIVE_CONTACT_WITHOUT_GUIDING_VISITOR,
                displayOrder = DoorstepVisitorHandoffRefusalCardId
                    .END_LIVE_CONTACT_WITHOUT_GUIDING_VISITOR.displayOrder,
                title = "통화·채팅을 끝내고 방문자에게 안내하지 않기",
                doorstepHandoffPressureSituation =
                    "상대가 연락을 유지한 채 문으로 이동하거나 앱·링크를 열고 밖의 사람에게 안내하라고 하는 때",
                valueFreeRefusalScript =
                    "이 연락을 직접 끝내겠습니다. 문을 열거나 위치·이동·기기 안내를 하지 않겠습니다.",
                whyItMatters =
                    "실시간 연락과 방문 안내를 함께 멈추면 상대가 화면과 이동, 대면 행동을 계속 통제하기 어려워집니다.",
                doNotOpenLetInHandoffSayShareAttachOrRecordBoundaries = listOf(
                    "전화번호·채널·프로필·발신 표시·링크·앱·화면·현재 위치·이동 경로·출입 방법을 말하거나 공유하지 마세요.",
                    "통화 상태·문자·연락처·알림·앱·패키지·기기·설정을 읽거나 통화 종료·차단·삭제·설정 변경을 자동 수행하지 않습니다."
                ),
                primaryAction = action(
                    DoorstepVisitorHandoffRefusalActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
                ),
                secondaryActions = listOf(
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .LINK_APP_INSTALL_QUARANTINE_COACH
                    ),
                    action(DoorstepVisitorHandoffRefusalActionId.REMOTE_ACCESS_CUTOFF_COACH),
                    action(DoorstepVisitorHandoffRefusalActionId.RECONTACT_BOUNDARY_COACH),
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH
                    ),
                    action(DoorstepVisitorHandoffRefusalActionId.PRIVACY_CENTER)
                )
            ),
            DoorstepVisitorHandoffRefusalCard(
                id = DoorstepVisitorHandoffRefusalCardId
                    .SEPARATE_OFFICIAL_TITLE_DOCUMENT_AND_CASE_PRESSURE,
                displayOrder = DoorstepVisitorHandoffRefusalCardId
                    .SEPARATE_OFFICIAL_TITLE_DOCUMENT_AND_CASE_PRESSURE.displayOrder,
                title = "기관·은행·플랫폼 직함과 문서·사건 압박 분리하기",
                doorstepHandoffPressureSituation =
                    "기관·은행·보안·배송·수리·플랫폼 직함이나 문서·사건을 내세워 출입·확인·인계를 의무라고 하는 때",
                valueFreeRefusalScript =
                    "상대가 말한 직함과 문서는 출입이나 인계 근거로 쓰지 않겠습니다. 상대가 준 경로와 분리해 다음 순서만 고르겠습니다.",
                whyItMatters =
                    "직함·문서·발신 표시는 문 앞 사람이나 요청, 전달 방식이 공식이거나 안전하다는 증명이 아닙니다.",
                doNotOpenLetInHandoffSayShareAttachOrRecordBoundaries = listOf(
                    "기관·은행·플랫폼·직원·기사·사람 이름, 번호, 문서, 사건, 접수, 신고 값을 말하거나 공유하지 마세요.",
                    "공식 번호·기관 목록·방문자·발신자·전화번호·문서·사건·법률 상태를 조회·검증하지 않고 전화·브라우저·문자·신고 제출을 하지 않습니다."
                ),
                primaryAction = action(
                    DoorstepVisitorHandoffRefusalActionId
                        .OFFICIAL_PRESSURE_REALITY_CHECK_COACH
                ),
                secondaryActions = listOf(
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .VOICE_IMPERSONATION_REALITY_CHECK_COACH
                    ),
                    action(DoorstepVisitorHandoffRefusalActionId.NEW_NUMBER_QUARANTINE_COACH),
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH
                    ),
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    ),
                    action(DoorstepVisitorHandoffRefusalActionId.OFFICIAL_HELP_DIRECTORY),
                    action(DoorstepVisitorHandoffRefusalActionId.REPORT_PREP)
                )
            ),
            DoorstepVisitorHandoffRefusalCard(
                id = DoorstepVisitorHandoffRefusalCardId
                    .CHOOSE_FAMILY_BUILDING_HELP_AND_FOLLOW_UP_ROUTE,
                displayOrder = DoorstepVisitorHandoffRefusalCardId
                    .CHOOSE_FAMILY_BUILDING_HELP_AND_FOLLOW_UP_ROUTE.displayOrder,
                title = "가족·보호자·관리 지원·공식 도움·신고 준비 경로 고르기",
                doorstepHandoffPressureSituation =
                    "거절 뒤 가족 검토·건물 관리 지원·증거 개인정보·공식 도움·신고 준비·후속 점검 순서를 고르려는 때",
                valueFreeRefusalScript =
                    "민감한 내용을 말하지 않고 문을 닫은 채 가족이나 보호자와 상의한 뒤 기존 도움·신고 준비 안내를 직접 확인하겠습니다.",
                whyItMatters =
                    "값 없는 다음 문장은 주소·사람·사건·금융값을 반복 공개하지 않으면서 이미 있는 안전 흐름으로 이어 줍니다.",
                doNotOpenLetInHandoffSayShareAttachOrRecordBoundaries = listOf(
                    "원문·가린 단서·사건 내용·안전단어·콜백 문장·연락처·주소·사람·금액·문서·결과 상태를 말하거나 공유하지 마세요.",
                    "증거를 촬영·녹음·스캔·첨부·업로드하지 않고 관리실 연락·공식 도움 조회·신고 제출·출동·법률·금융 결과를 만들거나 저장하지 않습니다."
                ),
                primaryAction = action(
                    DoorstepVisitorHandoffRefusalActionId.TRUSTED_HELPER_REVIEW
                ),
                secondaryActions = listOf(
                    action(DoorstepVisitorHandoffRefusalActionId.SAFETY_PLAN),
                    action(
                        DoorstepVisitorHandoffRefusalActionId.FAMILY_READINESS_ROADMAP
                    ),
                    action(
                        DoorstepVisitorHandoffRefusalActionId.FAMILY_SAFETY_DRILL_COACH
                    ),
                    action(DoorstepVisitorHandoffRefusalActionId.FAMILY_CALLBACK_CARD),
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .FAMILY_SAFE_WORD_RESET_COACH
                    ),
                    action(DoorstepVisitorHandoffRefusalActionId.SAFE_CALL_SCRIPTS),
                    action(DoorstepVisitorHandoffRefusalActionId.FAMILY_ALERT_MESSAGES),
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .FAMILY_RESPONSE_ROLE_BOARD
                    ),
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .FAMILY_CALM_DEBRIEF_AGENDA
                    ),
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .EVIDENCE_HANDOFF_PRIVACY_COACH
                    ),
                    action(DoorstepVisitorHandoffRefusalActionId.EVIDENCE_KIT),
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .INCIDENT_TIMELINE_BRIEFING
                    ),
                    action(DoorstepVisitorHandoffRefusalActionId.OFFICIAL_HELP_DIRECTORY),
                    action(DoorstepVisitorHandoffRefusalActionId.REPORT_PREP),
                    action(
                        DoorstepVisitorHandoffRefusalActionId
                            .FOLLOW_UP_CHECK_IN_PLANNER
                    ),
                    action(DoorstepVisitorHandoffRefusalActionId.SAFETY_FLOW_ROUTER),
                    action(DoorstepVisitorHandoffRefusalActionId.PRIVACY_CENTER)
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        readinessLevel: ReadinessLevel,
        hasNoAnalysisOrSignals: Boolean,
        hasHighRisk: Boolean,
        hasFinancialSignal: Boolean,
        hasOfficialPressureSignal: Boolean,
        hasLiveLinkAppCourierSignal: Boolean,
        hasCredentialSignal: Boolean,
        hasRecoveryPromiseSignal: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (hasNoAnalysisOrSignals) {
                add(
                    "검사 결과나 일치 신호가 없어도 방문자·기사·경비·직원·기관·은행·" +
                        "플랫폼·문서·사건·택배·주소·경로·방문·요청·출입·인계·결제·신고 " +
                        "상태를 공식·검증·안전·유효·접수·해결·완료라고 단정하지 않고 모든 " +
                        "값 없는 거절 카드와 기존 대응 경로를 보수적으로 보여 줍니다."
                )
            }
            if (hasHighRisk) {
                add(
                    "높은 위험 범주에서는 문 열기·출입·대면 인계 멈춤, 연락 종료, 별도 " +
                        "확인, 가족, 계정·기기·노출·증거·공식 도움·후속 경로를 값 없이 넓게 보여 줍니다."
                )
            }
            if (hasFinancialSignal) {
                add(
                    "돈 관련 범주에서는 현금·물품 인계 멈춤, 은행 직원 공개 문구, 송금 " +
                        "멈춤, 연락 종료, 기관 압박, 별도 확인, 계정·노출·증거 개인정보·" +
                        "보호자·공식 도움·신고 준비 화면을 고르며 현금·카드·결제·계정 " +
                        "동작이나 지도·위치·주소·문·기사 행동을 하지 않습니다."
                )
            }
            if (hasOfficialPressureSignal) {
                add(
                    "사칭·비밀 유지 범주에서는 기관 압박, 연락 종료, 별도 확인, 가족 " +
                        "알림·차분 대화, 증거 개인정보, 보호자, 공식 도움·신고 준비 화면을 " +
                        "고르며 공식 번호·방문자·직원 신원·문서·사건을 검증하거나 전화·" +
                        "브라우저·문자·신고 제출을 하지 않습니다."
                )
            }
            if (hasLiveLinkAppCourierSignal) {
                add(
                    "통화 유지·앱·링크·택배 문자 범주에서는 연락 종료, 링크·앱 격리, " +
                        "원격지시 중단, 현금·물품 인계 멈춤, 노출·계정·개인정보 화면을 " +
                        "고르며 통화 상태, 앱·패키지·기기·설정을 읽거나 지도·위치·택배 " +
                        "추적·원격 연결 종료·기기 정리를 하지 않습니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증·상품권·결제 범주에서는 비밀값 멈춤, 상품권 코드 멈춤, 계정 " +
                        "잠금, 안전 붙여넣기 개인정보, 원격지시 중단, 노출 분류 화면을 " +
                        "고르며 OTP·PIN·비밀번호·계좌·카드·은행·지갑·상품권·현금 값을 사용하지 않습니다."
                )
            }
            if (hasRecoveryPromiseSignal) {
                add(
                    "회복 약속 범주에서는 회복 현실 점검, 돈·물품 인계 멈춤, 증거 " +
                        "개인정보, 보호자, 공식 도움·신고 준비 화면을 고르며 환급·차지백·" +
                        "취소·동결·추적·회복 결과를 수행하거나 보장하지 않습니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 긴급·목소리 사칭·번호 변경 범주에서는 별도 확인, 가족 콜백, " +
                        "목소리 현실 점검, 새 번호 격리, 안전단어 재정비, 값 없는 전화 " +
                        "문구, 가족 알림 화면을 고르며 연락처·안전단어·콜백 값을 사용하거나 통화·문자를 읽지 않습니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만으로 증거 개인정보·증거 체크·사건 " +
                        "흐름·후속 점검 화면을 더 보여 주며 사건 내용이나 미리보기를 읽지 않습니다."
                )
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 아직 진행 중인 범주라 안전 계획·안전단어 재정비·준비 " +
                    "로드맵·가족 연습·값 없는 전화 문구 화면을 직접 살펴보도록 권합니다. " +
                    "저장된 계획의 실제 값은 추천에 사용하지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있다는 범주는 방문자·문 앞 상황·요청·출입·인계가 " +
                    "안전하거나 신뢰할 수 있거나 공식이라는 뜻이 아닙니다. 준비 상태와 " +
                    "관계없이 문을 닫은 채 값 없는 거절 문장과 별도 확인 경로를 직접 고르세요."
        }
        return listOf(situationCopy, readinessCopy)
            .filter { copy -> copy.isNotBlank() }
            .joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 고정 거절 문장을 사용자가 직접 읽고 기존 앱 화면을 직접 고르는 로컬 전용·무상태 수동 안내이며 민감한 값을 입력하라고 요구하지 않고 새 저장소·자동 처리·자동 전송·스케줄러를 만들지 않습니다.",
        "추천은 현재 검사 결과의 위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기·가족 안전 계획의 실제 값을 사용하지 않습니다.",
        "집·건물·문·공동 출입구·로비·호수·주소·경로·위치·만남 값을 입력·표시·저장하지 않고 문·인터폰·CCTV·카메라·마이크·도어벨·홈 자동화에 접근하거나 출입·개방·잠금·호출을 제어하지 않습니다.",
        "지도·위치·주소·경로·길 안내·내비게이션·지오코딩·주소 검증을 사용하지 않고 집·건물·로비·문·만남 장소를 찾거나 기록·검증·추천하지 않습니다.",
        "방문자·직원·경비·관리 인력·기사·수리 인력·수사관·기관·은행·플랫폼 사람의 신원이나 직함을 조회·확인·검증하지 않고 택배·퀵·우편·물품·기사를 추적하거나 방문·이동·수거·배달·인계·배차·예약·일정을 만들지 않습니다.",
        "네트워크·웹 탐색·브라우저·웹뷰를 사용하지 않고 기관 정보·공식 번호·디렉터리·발신자 표시·전화번호·문서·사건·신고·접수·영수증·법률·금융 상태를 조회·검증하거나 신고·출동을 요청·제출하지 않습니다.",
        "전화·다이얼·발신·통화·종료·선별·차단·통화 상태·통화 기록·문자·SMS·받는 사람 선택·자동 전송을 사용하지 않고 연락처·주소록·알림을 읽기·쓰기·편집·선택·감시·필터링하지 않습니다.",
        "QR·바코드·OCR·카메라·이미지 분석·화면 캡처를 사용하지 않고 파일·문서·사진·스크린샷·오디오·미디어·저장소를 읽거나 선택·촬영·녹음·전사·첨부·스트림·URI 공유·업로드·동기화하지 않습니다.",
        "클립보드·텍스트 감시기·자동완성·자격 증명·계정·로그인·인증·생체 인증·키가드·키 저장소·암호화 API를 사용하지 않고 계정·인증 상태를 읽거나 바꾸지 않습니다.",
        "OTP·PIN·비밀번호·보안 답·주민 식별값·안전단어·콜백 문장·연락처 이름·번호·주소·계좌·카드·은행·지갑·거래·금액·현금 일련값·코드 값을 읽기·표시·저장·복사·붙여넣기·자동 입력·공유·제출하지 않습니다.",
        "현금 인출·입금·송금·결제·계좌·카드·통장·은행·ATM·NFC·지갑·블록체인·상품권 기능 또는 API를 사용하지 않고 구매·잔액·교환·취소·동결·차단·환급·차지백·추적·회복을 수행·조회·제출·보장하지 않습니다.",
        "앱·패키지·설치 앱 목록·앱 이름·권한·기기 상태를 검사하거나 읽지 않고 설치·제거·비활성화·강제 종료·권한 허용·철회·설정 변경·기기 정리·원격 연결 종료를 수행하지 않습니다.",
        "접근성 서비스·기기 관리자·VPN·사용 통계·알림 수신 서비스·권한 관리자·패키지 설치 도구·기기 관리 기능을 사용하지 않습니다.",
        "방문자·직원·경비·기사·수사관·기관·은행·플랫폼·주소·경로·택배·물품·현금·문서·사건·신고·계좌·카드·기기·결제·출입·인계·환급·회복 상태가 공식·실제·안전·보안·검증·유효·접수·제출·수신·보호·적법·완료·해결·차단·취소·역전·회복·환급·동결·추적·배달·수거·입금·예약·보장·문제없음 상태라고 판정하거나 증명하지 않습니다.",
        "수동 문자 공유는 고정 카드 제목과 기존 앱 경로·행동 종류만 담고 원문·가린 단서·계획·사건·기관 목록·연락처·채널·프로필·계정·결제·지갑·링크·앱·주소·위치·직원·방문자·기사·현금·기기·받는 사람·확인·제출·출입·인계·회복 결과를 담거나 파일·스트림·URI를 붙이거나 자동 발송하지 않습니다.",
        "새 Android 권한을 요청하지 않고 알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러·브로드캐스트 수신기를 만들거나 예약하지 않습니다.",
        "가족 계획·사건·공식 디렉터리·연락처·알림·통화·문자·위치·지도·경로·택배·물품·방문·문·계정·결제·앱·권한·기기·자격·신고·확인·결과 상태를 수정하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<DoorstepVisitorHandoffRefusalCardId>,
        recommendedActions: List<DoorstepVisitorHandoffRefusalAction>,
        cards: List<DoorstepVisitorHandoffRefusalCard>
    ): String {
        val cardById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[문 앞 방문자·자택 인계 거절 코치]")
            appendLine("민감한 값을 말하지 않고 문을 닫은 채 직접 읽을 거절 문장을 고르는 로컬 수동 안내입니다.")
            appendLine()
            appendLine("추천 값 없는 거절 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 실제 주소·위치·사람·연락처·금액·계정·카드·문서·사건·물품·기기 값은 보내지 않습니다.")
            append("- 이 안내는 출입·인계·금융 행동·신원 확인·위치 조회·추적·예약·검증·신고·환급·회복을 수행하거나 보장하지 않습니다.")
        }
    }

    private fun actionFor(
        id: DoorstepVisitorHandoffRefusalActionId
    ): DoorstepVisitorHandoffRefusalAction = DoorstepVisitorHandoffRefusalAction(
        id = id,
        label = when (id) {
            DoorstepVisitorHandoffRefusalActionId.QUICK_STOP -> "긴급 멈춤 모드"
            DoorstepVisitorHandoffRefusalActionId.CHECKER -> "의심 문구 검사"
            DoorstepVisitorHandoffRefusalActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            DoorstepVisitorHandoffRefusalActionId.SAFETY_PLAN -> "가족 안전 계획"
            DoorstepVisitorHandoffRefusalActionId.FAMILY_READINESS_ROADMAP ->
                "가족 준비 로드맵"
            DoorstepVisitorHandoffRefusalActionId.FAMILY_SAFETY_DRILL_COACH ->
                "가족 안전 연습 코치"
            DoorstepVisitorHandoffRefusalActionId.SAFE_PASTE_PRIVACY_COACH ->
                "안전 붙여넣기 개인정보 코치"
            DoorstepVisitorHandoffRefusalActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                "현재 연락을 끝내는 문장"
            DoorstepVisitorHandoffRefusalActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 카드"
            DoorstepVisitorHandoffRefusalActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                "가족 안전단어 재정비 코치"
            DoorstepVisitorHandoffRefusalActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                "별도 경로 확인 코치"
            DoorstepVisitorHandoffRefusalActionId.SAFE_CALL_SCRIPTS ->
                "값 없는 안전 확인 전화 문구"
            DoorstepVisitorHandoffRefusalActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                "목소리 사칭 현실 점검"
            DoorstepVisitorHandoffRefusalActionId.NEW_NUMBER_QUARANTINE_COACH ->
                "새 연락수단 격리 코치"
            DoorstepVisitorHandoffRefusalActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                "자격 비밀값 멈춤 코치"
            DoorstepVisitorHandoffRefusalActionId.FAMILY_ALERT_MESSAGES -> "가족 알림 문구"
            DoorstepVisitorHandoffRefusalActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                "확인 후속 행동 보드"
            DoorstepVisitorHandoffRefusalActionId.RECONTACT_BOUNDARY_COACH ->
                "재접촉 경계 코치"
            DoorstepVisitorHandoffRefusalActionId.TRUSTED_HELPER_REVIEW -> "보호자 검토 모드"
            DoorstepVisitorHandoffRefusalActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                "가족 대응 역할 보드"
            DoorstepVisitorHandoffRefusalActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                "가족 차분 대화 안건"
            DoorstepVisitorHandoffRefusalActionId
                .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH ->
                "현금 인출·택배/퀵 대면 전달 멈춤 코치"
            DoorstepVisitorHandoffRefusalActionId
                .BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH ->
                "은행·ATM 직원 공개 문구 코치"
            DoorstepVisitorHandoffRefusalActionId.MONEY_TRANSFER_PAUSE_GATE ->
                "송금 전 멈춤 확인"
            DoorstepVisitorHandoffRefusalActionId.GIFT_CARD_CODE_PAUSE_COACH ->
                "상품권·코드 멈춤 코치"
            DoorstepVisitorHandoffRefusalActionId.LINK_APP_INSTALL_QUARANTINE_COACH ->
                "링크·앱 설치 격리 코치"
            DoorstepVisitorHandoffRefusalActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                "기관 사칭·압박 현실 점검"
            DoorstepVisitorHandoffRefusalActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH ->
                "회복 약속 현실 점검"
            DoorstepVisitorHandoffRefusalActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크"
            DoorstepVisitorHandoffRefusalActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                "원격 접근 중단 코치"
            DoorstepVisitorHandoffRefusalActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
            DoorstepVisitorHandoffRefusalActionId.PRIVACY_CENTER -> "개인정보 센터"
            DoorstepVisitorHandoffRefusalActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                "증거 전달 개인정보 코치"
            DoorstepVisitorHandoffRefusalActionId.EVIDENCE_KIT -> "증거 보존 체크"
            DoorstepVisitorHandoffRefusalActionId.INCIDENT_TIMELINE_BRIEFING ->
                "사건 흐름 요약"
            DoorstepVisitorHandoffRefusalActionId.OFFICIAL_HELP_DIRECTORY ->
                "공식 도움 연락처"
            DoorstepVisitorHandoffRefusalActionId.REPORT_PREP -> "공식 신고 준비"
            DoorstepVisitorHandoffRefusalActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                "후속 점검 계획"
            DoorstepVisitorHandoffRefusalActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
