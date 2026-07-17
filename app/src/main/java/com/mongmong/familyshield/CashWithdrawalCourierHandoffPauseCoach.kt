package com.mongmong.familyshield

enum class CashWithdrawalCourierHandoffPauseCardId(val displayOrder: Int) {
    STOP_CASH_WITHDRAWAL_AND_ATM_BANK_VISIT(1),
    DO_NOT_HAND_OVER_CASH_CARD_BANKBOOK_DEVICE_OR_DOCUMENTS(2),
    DO_NOT_USE_LOCKER_PARCEL_MAIL_QUICK_OR_ENVELOPE_HANDOFF(3),
    LEAVE_CONTACT_AND_DO_NOT_TRAVEL_OR_HIDE_MOVEMENT(4),
    SEPARATE_OFFICIAL_TITLE_CASE_AND_DOCUMENT_PRESSURE(5),
    VALUE_FREE_EXPOSURE_EVIDENCE_FAMILY_HELP_AND_FOLLOW_UP(6)
}

enum class CashWithdrawalCourierHandoffPauseActionId {
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

data class CashWithdrawalCourierHandoffPauseAction(
    val id: CashWithdrawalCourierHandoffPauseActionId,
    val label: String
)

data class CashWithdrawalCourierHandoffPauseCard(
    val id: CashWithdrawalCourierHandoffPauseCardId,
    val displayOrder: Int,
    val title: String,
    val cashCourierPressureScenario: String,
    val valueFreeActionSentence: String,
    val whyItMatters: String,
    val doNotWithdrawTravelMeetHandoffShareOrAttachBoundaries: List<String>,
    val primaryAction: CashWithdrawalCourierHandoffPauseAction,
    val secondaryActions: List<CashWithdrawalCourierHandoffPauseAction>
) {
    val routeActions: List<CashWithdrawalCourierHandoffPauseAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class CashWithdrawalCourierHandoffPauseCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<CashWithdrawalCourierHandoffPauseCardId>,
    val recommendedActions: List<CashWithdrawalCourierHandoffPauseAction>,
    val cards: List<CashWithdrawalCourierHandoffPauseCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<CashWithdrawalCourierHandoffPauseCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<CashWithdrawalCourierHandoffPauseActionId>
        get() = recommendedActions.map { action -> action.id }
}

object CashWithdrawalCourierHandoffPauseCoachBuilder {
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

    private val liveLinkAppSignals = setOf(
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
    ): CashWithdrawalCourierHandoffPauseCoach {
        val riskLevel = analysis?.riskLevel
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val readinessLevel = readiness.level
        val hasNoAnalysisOrSignals = analysis == null || signals.isEmpty()
        val hasHighRisk = riskLevel == RiskLevel.HIGH
        val hasFinancialSignal = signals.any { signal -> signal in financialSignals }
        val hasOfficialPressureSignal = signals.any { signal ->
            signal in officialPressureSignals
        }
        val hasLiveLinkAppSignal = signals.any { signal -> signal in liveLinkAppSignals }
        val hasCredentialSignal = signals.any { signal -> signal in credentialSignals }
        val hasRecoveryPromiseSignal = signals.any { signal ->
            signal in recoveryPromiseSignals
        }
        val hasFamilyVerificationSignal = signals.any { signal ->
            signal in familyVerificationSignals
        }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0

        val recommendedCardSet = linkedSetOf(
            CashWithdrawalCourierHandoffPauseCardId.STOP_CASH_WITHDRAWAL_AND_ATM_BANK_VISIT,
            CashWithdrawalCourierHandoffPauseCardId
                .DO_NOT_HAND_OVER_CASH_CARD_BANKBOOK_DEVICE_OR_DOCUMENTS,
            CashWithdrawalCourierHandoffPauseCardId
                .LEAVE_CONTACT_AND_DO_NOT_TRAVEL_OR_HIDE_MOVEMENT
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += CashWithdrawalCourierHandoffPauseCardId.entries
        }
        if (hasFinancialSignal || hasRecoveryPromiseSignal) {
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId.STOP_CASH_WITHDRAWAL_AND_ATM_BANK_VISIT
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .DO_NOT_HAND_OVER_CASH_CARD_BANKBOOK_DEVICE_OR_DOCUMENTS
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .DO_NOT_USE_LOCKER_PARCEL_MAIL_QUICK_OR_ENVELOPE_HANDOFF
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .SEPARATE_OFFICIAL_TITLE_CASE_AND_DOCUMENT_PRESSURE
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .VALUE_FREE_EXPOSURE_EVIDENCE_FAMILY_HELP_AND_FOLLOW_UP
        }
        if (hasOfficialPressureSignal) {
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .DO_NOT_HAND_OVER_CASH_CARD_BANKBOOK_DEVICE_OR_DOCUMENTS
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .DO_NOT_USE_LOCKER_PARCEL_MAIL_QUICK_OR_ENVELOPE_HANDOFF
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .LEAVE_CONTACT_AND_DO_NOT_TRAVEL_OR_HIDE_MOVEMENT
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .SEPARATE_OFFICIAL_TITLE_CASE_AND_DOCUMENT_PRESSURE
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .VALUE_FREE_EXPOSURE_EVIDENCE_FAMILY_HELP_AND_FOLLOW_UP
        }
        if (hasLiveLinkAppSignal) {
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .DO_NOT_USE_LOCKER_PARCEL_MAIL_QUICK_OR_ENVELOPE_HANDOFF
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .LEAVE_CONTACT_AND_DO_NOT_TRAVEL_OR_HIDE_MOVEMENT
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .VALUE_FREE_EXPOSURE_EVIDENCE_FAMILY_HELP_AND_FOLLOW_UP
        }
        if (hasCredentialSignal) {
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .DO_NOT_HAND_OVER_CASH_CARD_BANKBOOK_DEVICE_OR_DOCUMENTS
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .VALUE_FREE_EXPOSURE_EVIDENCE_FAMILY_HELP_AND_FOLLOW_UP
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .LEAVE_CONTACT_AND_DO_NOT_TRAVEL_OR_HIDE_MOVEMENT
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .SEPARATE_OFFICIAL_TITLE_CASE_AND_DOCUMENT_PRESSURE
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .VALUE_FREE_EXPOSURE_EVIDENCE_FAMILY_HELP_AND_FOLLOW_UP
        }
        if (hasSavedIncident) {
            recommendedCardSet +=
                CashWithdrawalCourierHandoffPauseCardId
                    .VALUE_FREE_EXPOSURE_EVIDENCE_FAMILY_HELP_AND_FOLLOW_UP
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedCardSet +=
                    CashWithdrawalCourierHandoffPauseCardId
                        .LEAVE_CONTACT_AND_DO_NOT_TRAVEL_OR_HIDE_MOVEMENT
                recommendedCardSet +=
                    CashWithdrawalCourierHandoffPauseCardId
                        .VALUE_FREE_EXPOSURE_EVIDENCE_FAMILY_HELP_AND_FOLLOW_UP
            }
            ReadinessLevel.READY -> {
                recommendedCardSet +=
                    CashWithdrawalCourierHandoffPauseCardId
                        .STOP_CASH_WITHDRAWAL_AND_ATM_BANK_VISIT
                recommendedCardSet +=
                    CashWithdrawalCourierHandoffPauseCardId
                        .DO_NOT_HAND_OVER_CASH_CARD_BANKBOOK_DEVICE_OR_DOCUMENTS
            }
        }
        val recommendedCardIds = CashWithdrawalCourierHandoffPauseCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            CashWithdrawalCourierHandoffPauseActionId.CHECKER
        } else {
            CashWithdrawalCourierHandoffPauseActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            currentResultActionId,
            CashWithdrawalCourierHandoffPauseActionId.QUICK_STOP,
            CashWithdrawalCourierHandoffPauseActionId.MONEY_TRANSFER_PAUSE_GATE,
            CashWithdrawalCourierHandoffPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
            CashWithdrawalCourierHandoffPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
            CashWithdrawalCourierHandoffPauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
            CashWithdrawalCourierHandoffPauseActionId.TRUSTED_HELPER_REVIEW,
            CashWithdrawalCourierHandoffPauseActionId.PRIVACY_CENTER,
            CashWithdrawalCourierHandoffPauseActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            CashWithdrawalCourierHandoffPauseActionId.entries
                .filterNot { actionId ->
                    actionId == CashWithdrawalCourierHandoffPauseActionId.ANALYSIS_RESULT
                }
                .forEach { actionId -> recommendedActionSet += actionId }
        }
        if (hasHighRisk) {
            CashWithdrawalCourierHandoffPauseActionId.entries.forEach { actionId ->
                recommendedActionSet += actionId
            }
        }
        if (hasFinancialSignal) {
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += CashWithdrawalCourierHandoffPauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += CashWithdrawalCourierHandoffPauseActionId.REPORT_PREP
        }
        if (hasOfficialPressureSignal) {
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += CashWithdrawalCourierHandoffPauseActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += CashWithdrawalCourierHandoffPauseActionId.REPORT_PREP
        }
        if (hasLiveLinkAppSignal) {
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += CashWithdrawalCourierHandoffPauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += CashWithdrawalCourierHandoffPauseActionId.PRIVACY_CENTER
        }
        if (hasCredentialSignal) {
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.GIFT_CARD_CODE_PAUSE_COACH
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.EXPOSURE_IMPACT_TRIAGE
        }
        if (hasRecoveryPromiseSignal) {
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += CashWithdrawalCourierHandoffPauseActionId.REPORT_PREP
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += CashWithdrawalCourierHandoffPauseActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.NEW_NUMBER_QUARANTINE_COACH
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.FAMILY_SAFE_WORD_RESET_COACH
            recommendedActionSet += CashWithdrawalCourierHandoffPauseActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += CashWithdrawalCourierHandoffPauseActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasSavedIncident) {
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_KIT
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.INCIDENT_TIMELINE_BRIEFING
            recommendedActionSet +=
                CashWithdrawalCourierHandoffPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedActionSet += CashWithdrawalCourierHandoffPauseActionId.SAFETY_PLAN
                recommendedActionSet +=
                    CashWithdrawalCourierHandoffPauseActionId.FAMILY_SAFE_WORD_RESET_COACH
                recommendedActionSet +=
                    CashWithdrawalCourierHandoffPauseActionId.FAMILY_READINESS_ROADMAP
                recommendedActionSet +=
                    CashWithdrawalCourierHandoffPauseActionId.FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += CashWithdrawalCourierHandoffPauseActionId.SAFE_CALL_SCRIPTS
            }
            ReadinessLevel.READY -> {
                recommendedActionSet += CashWithdrawalCourierHandoffPauseActionId.FAMILY_CALLBACK_CARD
                recommendedActionSet +=
                    CashWithdrawalCourierHandoffPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                recommendedActionSet += CashWithdrawalCourierHandoffPauseActionId.SAFE_CALL_SCRIPTS
            }
        }
        val recommendedActions = CashWithdrawalCourierHandoffPauseActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null -> "검사 결과가 없어도 모든 현금·대면 전달 멈춤 카드를 보수적으로 읽기"
            hasFinancialSignal -> "현금 인출·이동·전달을 멈추고 기존 돈·노출·도움 경로 고르기"
            hasOfficialPressureSignal -> "기관·직함·사건 압박과 대면 전달 지시를 분리하기"
            hasLiveLinkAppSignal -> "같은 연락 세션을 벗어나 이동·설치·전달 지시를 멈추기"
            hasCredentialSignal -> "카드·통장·기기·비밀값을 건네거나 입력하지 않기"
            hasRecoveryPromiseSignal -> "환급·회복 약속과 현금·물품 전달을 함께 멈추기"
            hasFamilyVerificationSignal -> "가족 위급 주장도 이동 없이 별도 경로로 확인하기"
            hasHighRisk -> "높은 위험 범주에서 값 없는 현금·대면 전달 멈춤 순서를 넓게 고르기"
            hasSavedIncident -> "사건 내용 없이 노출·증거·가족·도움·후속 경로만 고르기"
            else -> "현금이나 물품을 준비하거나 이동하기 전에 멈추기"
        }

        return CashWithdrawalCourierHandoffPauseCoach(
            title = "현금 인출·택배/퀵 대면 전달 멈춤 코치",
            intro =
                "현금 인출, ATM·은행 방문, 사람·택배/퀵·보관함·우편을 통한 현금·카드·" +
                    "통장·기기·서류 전달 압박 앞에서 장소·금액·경로·신원을 받지 않고 고정 " +
                    "카드와 기존 대응 화면만 직접 고르는 로컬 전용·무상태 수동 안내입니다.",
            modeLabel = "현금 인출 없음 · 이동 안내 없음 · 대면 전달 없음",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                readinessLevel = readinessLevel,
                hasNoAnalysisOrSignals = hasNoAnalysisOrSignals,
                hasHighRisk = hasHighRisk,
                hasFinancialSignal = hasFinancialSignal,
                hasOfficialPressureSignal = hasOfficialPressureSignal,
                hasLiveLinkAppSignal = hasLiveLinkAppSignal,
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

    private fun buildCards(): List<CashWithdrawalCourierHandoffPauseCard> {
        fun action(
            id: CashWithdrawalCourierHandoffPauseActionId
        ): CashWithdrawalCourierHandoffPauseAction = actionFor(id)

        return listOf(
            CashWithdrawalCourierHandoffPauseCard(
                id = CashWithdrawalCourierHandoffPauseCardId
                    .STOP_CASH_WITHDRAWAL_AND_ATM_BANK_VISIT,
                displayOrder = CashWithdrawalCourierHandoffPauseCardId
                    .STOP_CASH_WITHDRAWAL_AND_ATM_BANK_VISIT.displayOrder,
                title = "현금 인출·ATM/은행 방문을 지금 멈추기",
                cashCourierPressureScenario =
                    "상대가 현금으로 바꾸거나 ATM·은행에 가서 계속 통화하며 준비하라고 하는 때",
                valueFreeActionSentence =
                    "현금을 인출하거나 금융 장소로 이동하지 않고 기존 송금 멈춤·연락 종료·별도 경로 화면만 고르겠습니다.",
                whyItMatters =
                    "현금으로 바꾸면 송금보다 되돌리기 어렵고 이동 중에는 가족과 별도로 생각할 시간이 줄 수 있습니다.",
                doNotWithdrawTravelMeetHandoffShareOrAttachBoundaries = listOf(
                    "금액·권종·일련값·계좌·카드·은행·ATM·지점·주소·위치·경로 값을 적거나 공유하지 마세요.",
                    "현금 인출·송금·결제·은행·ATM·카드·계정 동작을 수행하지 않고 지점 찾기·지도·위치·길 안내·경로 검증을 하지 않습니다."
                ),
                primaryAction = action(
                    CashWithdrawalCourierHandoffPauseActionId.MONEY_TRANSFER_PAUSE_GATE
                ),
                secondaryActions = listOf(
                    action(CashWithdrawalCourierHandoffPauseActionId.QUICK_STOP),
                    action(CashWithdrawalCourierHandoffPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(
                        CashWithdrawalCourierHandoffPauseActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(
                        CashWithdrawalCourierHandoffPauseActionId
                            .OFFICIAL_PRESSURE_REALITY_CHECK_COACH
                    )
                )
            ),
            CashWithdrawalCourierHandoffPauseCard(
                id = CashWithdrawalCourierHandoffPauseCardId
                    .DO_NOT_HAND_OVER_CASH_CARD_BANKBOOK_DEVICE_OR_DOCUMENTS,
                displayOrder = CashWithdrawalCourierHandoffPauseCardId
                    .DO_NOT_HAND_OVER_CASH_CARD_BANKBOOK_DEVICE_OR_DOCUMENTS.displayOrder,
                title = "직원·수사관·택배/퀵 기사에게 현금·카드·통장·기기를 건네지 않기",
                cashCourierPressureScenario =
                    "직함이나 기관을 내세운 사람이 현금·카드·통장·기기·서류를 직접 받겠다고 하는 때",
                valueFreeActionSentence =
                    "사람을 만나거나 물품을 건네지 않고 기존 연락 종료·계정·비밀값·보호자 검토 화면만 고르겠습니다.",
                whyItMatters =
                    "직함·복장·문서·사건 설명은 현금이나 물품을 건네도 된다는 증거가 아닙니다.",
                doNotWithdrawTravelMeetHandoffShareOrAttachBoundaries = listOf(
                    "사람·기사·수사관·도우미·기관·직함·연락처·계좌·카드·통장·기기·문서 값을 적거나 공유하지 마세요.",
                    "신원·문서·사건·공식성·안전성을 조회·검증하지 않고 만남·인계·수거·보관·동결·차단·회수·신고 제출을 수행하지 않습니다."
                ),
                primaryAction = action(
                    CashWithdrawalCourierHandoffPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
                ),
                secondaryActions = listOf(
                    action(CashWithdrawalCourierHandoffPauseActionId.ACCOUNT_LOCKDOWN),
                    action(
                        CashWithdrawalCourierHandoffPauseActionId
                            .CREDENTIAL_SECRET_FREEZE_COACH
                    ),
                    action(CashWithdrawalCourierHandoffPauseActionId.TRUSTED_HELPER_REVIEW),
                    action(CashWithdrawalCourierHandoffPauseActionId.FAMILY_ALERT_MESSAGES)
                )
            ),
            CashWithdrawalCourierHandoffPauseCard(
                id = CashWithdrawalCourierHandoffPauseCardId
                    .DO_NOT_USE_LOCKER_PARCEL_MAIL_QUICK_OR_ENVELOPE_HANDOFF,
                displayOrder = CashWithdrawalCourierHandoffPauseCardId
                    .DO_NOT_USE_LOCKER_PARCEL_MAIL_QUICK_OR_ENVELOPE_HANDOFF.displayOrder,
                title = "물품보관함·택배·우편·퀵·봉투 지시를 따르지 않기",
                cashCourierPressureScenario =
                    "현금이나 물품을 봉투에 넣어 보관함·우편·택배·퀵 방식으로 맡기라고 하는 때",
                valueFreeActionSentence =
                    "포장·발송·보관·인계하지 않고 기존 긴급 멈춤·링크/앱·상품권·회복·원격지시 화면만 고르겠습니다.",
                whyItMatters =
                    "비대면 보관이나 전달 방식도 상대가 정한 회수 흐름이면 현금·물품을 되찾기 어려워질 수 있습니다.",
                doNotWithdrawTravelMeetHandoffShareOrAttachBoundaries = listOf(
                    "주소·경로·보관함·택배·우편·퀵·봉투·기사·송장·물품·수거 상태 값을 적거나 공유하지 마세요.",
                    "배송·택배·퀵·보관함을 조회·예약·추적·접수·발송·수거하거나 만남을 예약하지 않고 지도·위치·캘린더를 사용하지 않습니다."
                ),
                primaryAction = action(CashWithdrawalCourierHandoffPauseActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(
                        CashWithdrawalCourierHandoffPauseActionId
                            .LINK_APP_INSTALL_QUARANTINE_COACH
                    ),
                    action(
                        CashWithdrawalCourierHandoffPauseActionId.GIFT_CARD_CODE_PAUSE_COACH
                    ),
                    action(
                        CashWithdrawalCourierHandoffPauseActionId
                            .RECOVERY_PROMISE_REALITY_CHECK_COACH
                    ),
                    action(
                        CashWithdrawalCourierHandoffPauseActionId.REMOTE_ACCESS_CUTOFF_COACH
                    )
                )
            ),
            CashWithdrawalCourierHandoffPauseCard(
                id = CashWithdrawalCourierHandoffPauseCardId
                    .LEAVE_CONTACT_AND_DO_NOT_TRAVEL_OR_HIDE_MOVEMENT,
                displayOrder = CashWithdrawalCourierHandoffPauseCardId
                    .LEAVE_CONTACT_AND_DO_NOT_TRAVEL_OR_HIDE_MOVEMENT.displayOrder,
                title = "통화·채팅을 유지한 채 이동하거나 가족에게 숨기지 않기",
                cashCourierPressureScenario =
                    "통화나 채팅을 끊지 말고 혼자 이동하며 가족·은행 직원에게 알리지 말라고 하는 때",
                valueFreeActionSentence =
                    "같은 연락 세션에서 이동·만남·전달을 이어가지 않고 종료 문구와 가족·별도 확인 경로만 고르겠습니다.",
                whyItMatters =
                    "연락 유지와 비밀 지시는 외부 확인을 막고 다음 행동을 상대가 계속 통제하게 만들 수 있습니다.",
                doNotWithdrawTravelMeetHandoffShareOrAttachBoundaries = listOf(
                    "통화·채팅·채널·프로필·연락처·전화번호·현재 위치·목적지·이동 경로 값을 적거나 공유하지 마세요.",
                    "통화 상태·통화 기록·문자·연락처·알림·위치를 읽지 않고 전화 종료·차단·발신·문자 발송·이동·만남 예약을 자동 수행하지 않습니다."
                ),
                primaryAction = action(
                    CashWithdrawalCourierHandoffPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
                ),
                secondaryActions = listOf(
                    action(
                        CashWithdrawalCourierHandoffPauseActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(CashWithdrawalCourierHandoffPauseActionId.RECONTACT_BOUNDARY_COACH),
                    action(CashWithdrawalCourierHandoffPauseActionId.FAMILY_CALLBACK_CARD),
                    action(CashWithdrawalCourierHandoffPauseActionId.SAFE_CALL_SCRIPTS)
                )
            ),
            CashWithdrawalCourierHandoffPauseCard(
                id = CashWithdrawalCourierHandoffPauseCardId
                    .SEPARATE_OFFICIAL_TITLE_CASE_AND_DOCUMENT_PRESSURE,
                displayOrder = CashWithdrawalCourierHandoffPauseCardId
                    .SEPARATE_OFFICIAL_TITLE_CASE_AND_DOCUMENT_PRESSURE.displayOrder,
                title = "기관·은행·플랫폼 직함과 사건/문서 압박을 기존 현실 점검으로 분리하기",
                cashCourierPressureScenario =
                    "기관·은행·플랫폼의 직함·사건·문서를 내세워 현금이나 물품 전달이 의무라고 하는 때",
                valueFreeActionSentence =
                    "상대가 준 번호·문서·경로를 쓰지 않고 기존 기관 압박·별도 확인·공식 도움·신고 준비 원칙만 읽겠습니다.",
                whyItMatters =
                    "직함·사건·문서·발신 표시는 사람·요청·전달 방식이 공식이거나 안전하다는 증명이 아닙니다.",
                doNotWithdrawTravelMeetHandoffShareOrAttachBoundaries = listOf(
                    "기관·은행·플랫폼·사람·직함·사건·문서·번호·주소·경로·신고·접수 값을 적거나 공유하지 마세요.",
                    "공식 번호·기관 목록·발신자·전화번호·문서·사건·법률 상태를 조회·검증하지 않고 전화·문자·브라우저·신고 제출·법률·금융 절차를 수행하지 않습니다."
                ),
                primaryAction = action(
                    CashWithdrawalCourierHandoffPauseActionId
                        .OFFICIAL_PRESSURE_REALITY_CHECK_COACH
                ),
                secondaryActions = listOf(
                    action(
                        CashWithdrawalCourierHandoffPauseActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(
                        CashWithdrawalCourierHandoffPauseActionId
                            .VOICE_IMPERSONATION_REALITY_CHECK_COACH
                    ),
                    action(
                        CashWithdrawalCourierHandoffPauseActionId.NEW_NUMBER_QUARANTINE_COACH
                    ),
                    action(
                        CashWithdrawalCourierHandoffPauseActionId.OFFICIAL_HELP_DIRECTORY
                    ),
                    action(CashWithdrawalCourierHandoffPauseActionId.REPORT_PREP)
                )
            ),
            CashWithdrawalCourierHandoffPauseCard(
                id = CashWithdrawalCourierHandoffPauseCardId
                    .VALUE_FREE_EXPOSURE_EVIDENCE_FAMILY_HELP_AND_FOLLOW_UP,
                displayOrder = CashWithdrawalCourierHandoffPauseCardId
                    .VALUE_FREE_EXPOSURE_EVIDENCE_FAMILY_HELP_AND_FOLLOW_UP.displayOrder,
                title = "노출·증거·가족·공식 도움은 장소·금액·인물값 없이 기존 화면에서 정리하기",
                cashCourierPressureScenario =
                    "이미 준비나 이동 압박을 접한 뒤 노출·증거·가족 검토·공식 도움·후속 점검 순서를 살펴보려는 때",
                valueFreeActionSentence =
                    "장소·금액·사람·사건·물품 값을 이 카드에 적지 않고 기존 노출·증거·가족·도움·후속 화면에서 행동 종류만 고르겠습니다.",
                whyItMatters =
                    "값 없는 라우팅은 민감한 신원·금융·이동 정보가 카드와 공유 문장에 반복 노출되는 일을 줄입니다.",
                doNotWithdrawTravelMeetHandoffShareOrAttachBoundaries = listOf(
                    "원문·가린 단서·사건 내용·미리보기·장소·금액·사람·물품·문서·파일·사진·오디오·결과 상태를 적거나 공유하지 마세요.",
                    "증거를 수집·촬영·스캔·첨부·업로드·동기화·가림·삭제하지 않고 현금·전달·위치·사람·계정·기기·신고·회복 결과를 만들거나 저장하지 않습니다."
                ),
                primaryAction = action(
                    CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
                ),
                secondaryActions = listOf(
                    action(CashWithdrawalCourierHandoffPauseActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_KIT),
                    action(
                        CashWithdrawalCourierHandoffPauseActionId.INCIDENT_TIMELINE_BRIEFING
                    ),
                    action(CashWithdrawalCourierHandoffPauseActionId.TRUSTED_HELPER_REVIEW),
                    action(CashWithdrawalCourierHandoffPauseActionId.FAMILY_RESPONSE_ROLE_BOARD),
                    action(
                        CashWithdrawalCourierHandoffPauseActionId.FAMILY_CALM_DEBRIEF_AGENDA
                    ),
                    action(
                        CashWithdrawalCourierHandoffPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER
                    ),
                    action(CashWithdrawalCourierHandoffPauseActionId.SAFETY_FLOW_ROUTER),
                    action(CashWithdrawalCourierHandoffPauseActionId.PRIVACY_CENTER)
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
        hasLiveLinkAppSignal: Boolean,
        hasCredentialSignal: Boolean,
        hasRecoveryPromiseSignal: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (hasNoAnalysisOrSignals) {
                add(
                    "검사 결과나 일치 신호가 없어도 사람·기사·수사관·도우미·기관·은행·" +
                        "지점·ATM·주소·위치·경로·보관함·택배·우편·현금·권종·문서·사건·" +
                        "신고·계정·카드·기기·연락·가족·결제·환급·회복·전달 상태를 " +
                        "단정하지 말고 모든 현금·대면 전달 멈춤 카드를 보수적으로 읽도록 권합니다."
                )
            }
            if (hasHighRisk) {
                add(
                    "높은 위험 범주에서는 현금·물품·이동·연락 세션·기관 압박·가족 확인·" +
                        "노출·증거·도움·후속 대응 경로를 값 없이 넓게 함께 보여 줍니다."
                )
            }
            if (hasFinancialSignal) {
                add(
                    "송금·미끼·가상자산·안전계좌·대환 비용·결제 범주에서는 돈 멈춤·기관 " +
                        "압박·진행 중 연락 종료·별도 경로 확인·계정 잠금·노출 분류·증거 " +
                        "전달 개인정보·보호자 검토·공식 도움·신고 준비 화면을 고르며 현금 " +
                        "인출, ATM·은행·카드·결제 동작, 환급·차지백·회복, 지도·위치·택배 " +
                        "기능이나 공식 번호 조회를 수행하지 않습니다."
                )
            }
            if (hasOfficialPressureSignal) {
                add(
                    "사칭·비밀 유지·안전계좌 압박 범주에서는 기관 압박·연락 종료·별도 확인·" +
                        "가족 알림·차분 대화·보호자 검토·증거 전달 개인정보·공식 도움·신고 " +
                        "준비 화면을 고르며 공식 번호·디렉터리 값·사건·법률 상태를 사용하거나 " +
                        "전화·브라우저·문자·신고 제출을 하지 않습니다."
                )
            }
            if (hasLiveLinkAppSignal) {
                add(
                    "통화 유지·앱·링크·택배 문자 범주에서는 같은 연락을 수동으로 벗어나고 " +
                        "링크·앱 격리·원격지시 중단·계정 잠금·노출 분류·개인정보 화면을 " +
                        "고르며 통화 상태, 앱·패키지·권한·설정·기기 상태를 읽거나 지도·위치·" +
                        "택배 추적·원격 연결 종료·기기 정리를 수행하지 않습니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증·상품권·결제 범주에서는 비밀값 멈춤·상품권 코드 멈춤·계정 잠금·" +
                        "안전 붙여넣기 개인정보·원격지시 중단·노출 분류 화면을 고르며 OTP·" +
                        "PIN·비밀번호·계정·카드·은행·지갑·상품권·현금 값을 읽거나 표시하지 않습니다."
                )
            }
            if (hasRecoveryPromiseSignal) {
                add(
                    "환급·회복을 떠올리게 하는 범주에서는 회복 약속·돈 멈춤·증거 전달 " +
                        "개인정보·보호자 검토·공식 도움·신고 준비 화면을 고르며 환급·차지백·" +
                        "취소·동결·추적·회복 또는 현금·물품 회수를 수행하거나 보장하지 않습니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 긴급·목소리 사칭·번호 변경 범주에서는 별도 경로 확인·가족 콜백·" +
                        "목소리 현실 점검·새 번호 격리·안전단어 재정비·값 없는 전화 문구·" +
                        "가족 알림 화면을 고르며 연락처·안전단어·콜백 값을 사용하거나 연락처·" +
                        "통화 기록·문자를 읽지 않습니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만으로 증거 전달 개인정보·증거 체크·사건 " +
                        "흐름·후속 점검 화면을 더 보여 주며 사건 내용이나 미리보기는 읽지 않습니다."
                )
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 아직 진행 중인 범주라 안전 계획·안전단어 재정비·준비 로드맵·" +
                    "가족 연습·값 없는 전화 문구 화면을 직접 살펴보도록 권합니다. 저장된 " +
                    "계획의 실제 값은 추천에 사용하지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있다는 범주는 기존 준비 항목이 채워졌다는 뜻일 뿐 현금 " +
                    "인출·ATM·은행 방문·이동·만남·택배/퀵·보관함·현금·카드·통장·기기·" +
                    "문서 전달 요청이 실제이거나 안전하거나 신뢰할 수 있거나 공식이라는 뜻이 " +
                    "아닙니다. 준비 상태와 관계없이 인출·이동·만남·전달 행동을 멈추세요."
        }
        return listOf(situationCopy, readinessCopy).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 고정 문장을 직접 읽고 기존 앱 화면을 직접 고르는 로컬 전용·무상태 수동 안내이며 이름·연락처·위치·주소·경로·만남·보관함·기사·현금·금액·계정·카드·통장·기기·문서·사건 값을 입력하라고 요구하지 않습니다.",
        "추천은 현재 검사 결과의 위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기·가족 안전 계획의 실제 값을 사용하지 않습니다.",
        "현금 인출·ATM·은행 방문·송금·결제·계좌·카드·통장·은행·NFC·지갑·블록체인·상품권 기능 또는 API를 사용하지 않고 구매·잔액 조회·사용·동결·차단·취소·환급·차지백·추적·회복·역전을 수행·조회·제출·보장하지 않습니다.",
        "지도·위치·경로·길 안내·내비게이션·지오코딩·주소 검증을 사용하지 않고 은행·ATM·지점·만남 장소·목적지·보관함을 찾거나 기록·검증·추천하지 않습니다.",
        "택배·퀵·우편·배송·보관함·봉투를 조회·예약·접수·추적·발송·배차·수거·보관·인계하지 않고 만남·방문·이동·경로·일정을 만들거나 예약하지 않습니다.",
        "네트워크·웹 탐색·브라우저·웹뷰를 사용하지 않고 기관 정보·공식 번호·디렉터리·발신자 표시·전화번호·사람·신원·문서·사건·법률·금융·전달 상태를 조회·검증·판정하지 않습니다.",
        "전화·다이얼·발신·통화·종료·선별·차단·통화 상태·통화 기록·문자·SMS·받는 사람 선택·자동 전송을 사용하지 않고 연락처·주소록·알림을 읽기·쓰기·편집·선택·감시·필터링하지 않습니다.",
        "QR·바코드·OCR·카메라·이미지 분석·화면 캡처를 사용하지 않고 파일·문서·사진·스크린샷·오디오·미디어·저장소를 읽거나 선택·촬영·녹음·전사·첨부·스트림·URI 공유·업로드·동기화하지 않습니다.",
        "클립보드·텍스트 감시기·자동완성·자격 증명·계정·인증·생체 인증·키가드·키 저장소·암호화 API를 사용하지 않고 로그인·계정·인증 상태를 읽거나 바꾸지 않습니다.",
        "OTP·PIN·비밀번호·보안 답·주민 식별값·안전단어·콜백 문장·연락처 이름·번호·계정·카드·은행·지갑·거래·금액·현금 일련값·상품권·코드 값을 읽기·표시·저장·복사·붙여넣기·자동 입력·공유·제출하지 않습니다.",
        "앱·패키지·설치 앱 목록·앱 이름·권한·기기 상태를 검사하거나 읽지 않고 설치·제거·비활성화·강제 종료·권한 허용·철회·설정 변경·기기 정리·원격 연결 종료를 수행하지 않습니다.",
        "접근성 서비스·기기 관리자·VPN·사용 통계·알림 수신 서비스·권한 관리자·패키지 설치 도구·기기 관리 기능을 사용하지 않습니다.",
        "사람·기사·수사관·도우미·기관·은행·지점·ATM·주소·경로·보관함·택배·현금·문서·사건·신고·계정·카드·기기·결제·환급·회복·전달 상태가 공식·실제·안전·보안·검증·유효·신뢰·접수·제출·수신·보호·적법·완료·해결·차단·취소·역전·회복·환급·동결·추적·배달·수거·입금·예약·보장·문제없음 상태라고 판정하거나 증명하지 않습니다.",
        "수동 문자 공유는 고정 현금·대면 전달 멈춤 카드와 기존 앱 화면·행동 종류만 담고 원문·가린 단서·계획·사건·기관 목록·연락처·채널·프로필·계정·결제·지갑·링크·앱·위치·기사·현금·기기·받는 사람·확인·제출·전달·회복 결과를 담거나 자동 발송하지 않습니다.",
        "새 Android 권한을 요청하지 않고 알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러·브로드캐스트 수신기를 만들거나 예약하지 않습니다.",
        "새 저장소를 만들지 않고 카드 선택·추천·읽음·공유·체크·메모·담당자·받는 사람·현금·인출·이동·위치·경로·만남·기사·택배·보관함·전달·계정·결제·증거·확인·신고·회복·결과·진행·상태·이력을 저장하지 않으며 가족 계획·사건·디렉터리·연락처·알림·통화·문자·계정·결제·위치·기기 상태를 수정하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<CashWithdrawalCourierHandoffPauseCardId>,
        recommendedActions: List<CashWithdrawalCourierHandoffPauseAction>,
        cards: List<CashWithdrawalCourierHandoffPauseCard>
    ): String {
        val cardById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[현금 인출·택배/퀵 대면 전달 멈춤 코치]")
            appendLine("현금 인출·이동·만남·물품 전달을 멈추는 로컬 수동 점검입니다.")
            appendLine()
            appendLine("추천 현금·대면 전달 멈춤 카드")
            recommendedCardIds.forEach { cardId ->
                val card = cardById.getValue(cardId)
                appendLine("- ${card.title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 실제 장소·주소·경로·사람·기사·연락처·금액·현금·계정·카드·통장·기기·문서·사건 값은 보내지 않습니다.")
            append("- 이 안내는 인출·이동·만남·전달·추적·예약·검증·신고·동결·취소·환급·회복을 수행하거나 보장하지 않습니다.")
        }
    }

    private fun actionFor(
        id: CashWithdrawalCourierHandoffPauseActionId
    ): CashWithdrawalCourierHandoffPauseAction =
        CashWithdrawalCourierHandoffPauseAction(id = id, label = actionLabels.getValue(id))

    private val actionLabels = mapOf(
        CashWithdrawalCourierHandoffPauseActionId.QUICK_STOP to "긴급 멈춤 모드",
        CashWithdrawalCourierHandoffPauseActionId.CHECKER to "사기 신호 검사 시작",
        CashWithdrawalCourierHandoffPauseActionId.ANALYSIS_RESULT to "현재 분석 결과",
        CashWithdrawalCourierHandoffPauseActionId.SAFETY_PLAN to "가족 안전 계획",
        CashWithdrawalCourierHandoffPauseActionId.FAMILY_READINESS_ROADMAP to "가족 준비 로드맵",
        CashWithdrawalCourierHandoffPauseActionId.FAMILY_SAFETY_DRILL_COACH to "가족 안전 연습 코치",
        CashWithdrawalCourierHandoffPauseActionId.SAFE_PASTE_PRIVACY_COACH to "안전 붙여넣기 개인정보 코치",
        CashWithdrawalCourierHandoffPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS to "진행 중 연락 종료 문구",
        CashWithdrawalCourierHandoffPauseActionId.FAMILY_CALLBACK_CARD to "가족 콜백 카드",
        CashWithdrawalCourierHandoffPauseActionId.FAMILY_SAFE_WORD_RESET_COACH to "가족 안전단어 재정비 코치",
        CashWithdrawalCourierHandoffPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH to "별도 경로 확인 코치",
        CashWithdrawalCourierHandoffPauseActionId.SAFE_CALL_SCRIPTS to "안전 확인 전화 문구",
        CashWithdrawalCourierHandoffPauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH to "목소리 사칭 현실 점검",
        CashWithdrawalCourierHandoffPauseActionId.NEW_NUMBER_QUARANTINE_COACH to "새 번호 격리 코치",
        CashWithdrawalCourierHandoffPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH to "인증 비밀값 멈춤 코치",
        CashWithdrawalCourierHandoffPauseActionId.FAMILY_ALERT_MESSAGES to "가족 알림 문구",
        CashWithdrawalCourierHandoffPauseActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD to "확인 결과 다음 단계 보드",
        CashWithdrawalCourierHandoffPauseActionId.RECONTACT_BOUNDARY_COACH to "재접촉 경계 코치",
        CashWithdrawalCourierHandoffPauseActionId.TRUSTED_HELPER_REVIEW to "신뢰 보호자 검토",
        CashWithdrawalCourierHandoffPauseActionId.FAMILY_RESPONSE_ROLE_BOARD to "가족 대응 역할 보드",
        CashWithdrawalCourierHandoffPauseActionId.FAMILY_CALM_DEBRIEF_AGENDA to "가족 차분 대화 순서",
        CashWithdrawalCourierHandoffPauseActionId.MONEY_TRANSFER_PAUSE_GATE to "송금 전 멈춤 게이트",
        CashWithdrawalCourierHandoffPauseActionId.GIFT_CARD_CODE_PAUSE_COACH to "상품권·코드 멈춤 코치",
        CashWithdrawalCourierHandoffPauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH to "링크·앱 설치 격리 코치",
        CashWithdrawalCourierHandoffPauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH to "기관 사칭 압박 현실 점검",
        CashWithdrawalCourierHandoffPauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH to "회복 약속 현실 점검",
        CashWithdrawalCourierHandoffPauseActionId.ACCOUNT_LOCKDOWN to "계정·기기 잠금 체크리스트",
        CashWithdrawalCourierHandoffPauseActionId.REMOTE_ACCESS_CUTOFF_COACH to "원격지시 중단 코치",
        CashWithdrawalCourierHandoffPauseActionId.EXPOSURE_IMPACT_TRIAGE to "노출 영향 분류",
        CashWithdrawalCourierHandoffPauseActionId.PRIVACY_CENTER to "개인정보 센터",
        CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH to "증거 전달 개인정보 코치",
        CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_KIT to "증거 체크리스트",
        CashWithdrawalCourierHandoffPauseActionId.INCIDENT_TIMELINE_BRIEFING to "사건 흐름 브리핑",
        CashWithdrawalCourierHandoffPauseActionId.OFFICIAL_HELP_DIRECTORY to "공식 도움 경로",
        CashWithdrawalCourierHandoffPauseActionId.REPORT_PREP to "신고 준비 카드",
        CashWithdrawalCourierHandoffPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER to "후속 점검 플래너",
        CashWithdrawalCourierHandoffPauseActionId.SAFETY_FLOW_ROUTER to "안전 대응 길잡이"
    )
}
