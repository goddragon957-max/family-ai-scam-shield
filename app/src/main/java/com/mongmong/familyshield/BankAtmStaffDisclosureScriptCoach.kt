package com.mongmong.familyshield

enum class BankAtmStaffDisclosureScriptCardId(val displayOrder: Int) {
    DISCLOSE_CASH_WITHDRAWAL_PRESSURE_TO_BANK_ATM_STAFF(1),
    REQUEST_VALUE_FREE_PAUSE_BEFORE_DETAILS(2),
    END_CONTACT_AND_ASK_STAFF_FOR_SPACE(3),
    PAUSE_COURIER_MAIL_LOCKER_HANDOFF_AT_COUNTER(4),
    SEPARATE_OFFICIAL_TITLE_FROM_PROOF(5),
    CHOOSE_FAMILY_HELP_REPORT_NEXT_SENTENCE(6)
}

enum class BankAtmStaffDisclosureScriptActionId {
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

data class BankAtmStaffDisclosureScriptAction(
    val id: BankAtmStaffDisclosureScriptActionId,
    val label: String
)

data class BankAtmStaffDisclosureScriptCard(
    val id: BankAtmStaffDisclosureScriptCardId,
    val displayOrder: Int,
    val title: String,
    val staffDisclosureSituation: String,
    val valueFreeScript: String,
    val whyItMatters: String,
    val doNotSayShareAttachOrRecordBoundaries: List<String>,
    val primaryAction: BankAtmStaffDisclosureScriptAction,
    val secondaryActions: List<BankAtmStaffDisclosureScriptAction>
) {
    val routeActions: List<BankAtmStaffDisclosureScriptAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class BankAtmStaffDisclosureScriptCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<BankAtmStaffDisclosureScriptCardId>,
    val recommendedActions: List<BankAtmStaffDisclosureScriptAction>,
    val cards: List<BankAtmStaffDisclosureScriptCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<BankAtmStaffDisclosureScriptCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<BankAtmStaffDisclosureScriptActionId>
        get() = recommendedActions.map { action -> action.id }
}

object BankAtmStaffDisclosureScriptCoachBuilder {
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
    ): BankAtmStaffDisclosureScriptCoach {
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
            BankAtmStaffDisclosureScriptCardId
                .DISCLOSE_CASH_WITHDRAWAL_PRESSURE_TO_BANK_ATM_STAFF,
            BankAtmStaffDisclosureScriptCardId.REQUEST_VALUE_FREE_PAUSE_BEFORE_DETAILS,
            BankAtmStaffDisclosureScriptCardId.END_CONTACT_AND_ASK_STAFF_FOR_SPACE
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += BankAtmStaffDisclosureScriptCardId.entries
        }
        if (hasFinancialSignal || hasRecoveryPromiseSignal) {
            recommendedCardSet += BankAtmStaffDisclosureScriptCardId
                .DISCLOSE_CASH_WITHDRAWAL_PRESSURE_TO_BANK_ATM_STAFF
            recommendedCardSet +=
                BankAtmStaffDisclosureScriptCardId.REQUEST_VALUE_FREE_PAUSE_BEFORE_DETAILS
            recommendedCardSet += BankAtmStaffDisclosureScriptCardId
                .PAUSE_COURIER_MAIL_LOCKER_HANDOFF_AT_COUNTER
            recommendedCardSet +=
                BankAtmStaffDisclosureScriptCardId.SEPARATE_OFFICIAL_TITLE_FROM_PROOF
            recommendedCardSet +=
                BankAtmStaffDisclosureScriptCardId.CHOOSE_FAMILY_HELP_REPORT_NEXT_SENTENCE
        }
        if (hasOfficialPressureSignal) {
            recommendedCardSet +=
                BankAtmStaffDisclosureScriptCardId.REQUEST_VALUE_FREE_PAUSE_BEFORE_DETAILS
            recommendedCardSet +=
                BankAtmStaffDisclosureScriptCardId.END_CONTACT_AND_ASK_STAFF_FOR_SPACE
            recommendedCardSet +=
                BankAtmStaffDisclosureScriptCardId.SEPARATE_OFFICIAL_TITLE_FROM_PROOF
            recommendedCardSet +=
                BankAtmStaffDisclosureScriptCardId.CHOOSE_FAMILY_HELP_REPORT_NEXT_SENTENCE
        }
        if (hasLiveLinkAppCourierSignal) {
            recommendedCardSet +=
                BankAtmStaffDisclosureScriptCardId.END_CONTACT_AND_ASK_STAFF_FOR_SPACE
            recommendedCardSet += BankAtmStaffDisclosureScriptCardId
                .PAUSE_COURIER_MAIL_LOCKER_HANDOFF_AT_COUNTER
            recommendedCardSet +=
                BankAtmStaffDisclosureScriptCardId.CHOOSE_FAMILY_HELP_REPORT_NEXT_SENTENCE
        }
        if (hasCredentialSignal) {
            recommendedCardSet +=
                BankAtmStaffDisclosureScriptCardId.REQUEST_VALUE_FREE_PAUSE_BEFORE_DETAILS
            recommendedCardSet +=
                BankAtmStaffDisclosureScriptCardId.CHOOSE_FAMILY_HELP_REPORT_NEXT_SENTENCE
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet +=
                BankAtmStaffDisclosureScriptCardId.END_CONTACT_AND_ASK_STAFF_FOR_SPACE
            recommendedCardSet +=
                BankAtmStaffDisclosureScriptCardId.SEPARATE_OFFICIAL_TITLE_FROM_PROOF
            recommendedCardSet +=
                BankAtmStaffDisclosureScriptCardId.CHOOSE_FAMILY_HELP_REPORT_NEXT_SENTENCE
        }
        if (hasSavedIncident) {
            recommendedCardSet +=
                BankAtmStaffDisclosureScriptCardId.CHOOSE_FAMILY_HELP_REPORT_NEXT_SENTENCE
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedCardSet +=
                    BankAtmStaffDisclosureScriptCardId.END_CONTACT_AND_ASK_STAFF_FOR_SPACE
                recommendedCardSet +=
                    BankAtmStaffDisclosureScriptCardId.CHOOSE_FAMILY_HELP_REPORT_NEXT_SENTENCE
            }
            ReadinessLevel.READY -> {
                recommendedCardSet += BankAtmStaffDisclosureScriptCardId
                    .DISCLOSE_CASH_WITHDRAWAL_PRESSURE_TO_BANK_ATM_STAFF
                recommendedCardSet +=
                    BankAtmStaffDisclosureScriptCardId.REQUEST_VALUE_FREE_PAUSE_BEFORE_DETAILS
            }
        }
        val recommendedCardIds = BankAtmStaffDisclosureScriptCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            BankAtmStaffDisclosureScriptActionId.CHECKER
        } else {
            BankAtmStaffDisclosureScriptActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            currentResultActionId,
            BankAtmStaffDisclosureScriptActionId.QUICK_STOP,
            BankAtmStaffDisclosureScriptActionId
                .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH,
            BankAtmStaffDisclosureScriptActionId.MONEY_TRANSFER_PAUSE_GATE,
            BankAtmStaffDisclosureScriptActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
            BankAtmStaffDisclosureScriptActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
            BankAtmStaffDisclosureScriptActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
            BankAtmStaffDisclosureScriptActionId.TRUSTED_HELPER_REVIEW,
            BankAtmStaffDisclosureScriptActionId.PRIVACY_CENTER,
            BankAtmStaffDisclosureScriptActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            BankAtmStaffDisclosureScriptActionId.entries
                .filterNot { actionId ->
                    actionId == BankAtmStaffDisclosureScriptActionId.ANALYSIS_RESULT
                }
                .forEach { actionId -> recommendedActionSet += actionId }
        }
        if (hasHighRisk) {
            BankAtmStaffDisclosureScriptActionId.entries.forEach { actionId ->
                recommendedActionSet += actionId
            }
        }
        if (hasFinancialSignal) {
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId
                .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId.REPORT_PREP
        }
        if (hasOfficialPressureSignal) {
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId.REPORT_PREP
        }
        if (hasLiveLinkAppCourierSignal) {
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.LINK_APP_INSTALL_QUARANTINE_COACH
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId.PRIVACY_CENTER
        }
        if (hasCredentialSignal) {
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.GIFT_CARD_CODE_PAUSE_COACH
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.EXPOSURE_IMPACT_TRIAGE
        }
        if (hasRecoveryPromiseSignal) {
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId
                .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId.REPORT_PREP
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId
                .VOICE_IMPERSONATION_REALITY_CHECK_COACH
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.NEW_NUMBER_QUARANTINE_COACH
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.FAMILY_SAFE_WORD_RESET_COACH
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasSavedIncident) {
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += BankAtmStaffDisclosureScriptActionId.EVIDENCE_KIT
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.INCIDENT_TIMELINE_BRIEFING
            recommendedActionSet +=
                BankAtmStaffDisclosureScriptActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedActionSet += BankAtmStaffDisclosureScriptActionId.SAFETY_PLAN
                recommendedActionSet +=
                    BankAtmStaffDisclosureScriptActionId.FAMILY_SAFE_WORD_RESET_COACH
                recommendedActionSet +=
                    BankAtmStaffDisclosureScriptActionId.FAMILY_READINESS_ROADMAP
                recommendedActionSet +=
                    BankAtmStaffDisclosureScriptActionId.FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += BankAtmStaffDisclosureScriptActionId.SAFE_CALL_SCRIPTS
            }
            ReadinessLevel.READY -> {
                recommendedActionSet += BankAtmStaffDisclosureScriptActionId.FAMILY_CALLBACK_CARD
                recommendedActionSet +=
                    BankAtmStaffDisclosureScriptActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                recommendedActionSet += BankAtmStaffDisclosureScriptActionId.SAFE_CALL_SCRIPTS
            }
        }
        val recommendedActions = BankAtmStaffDisclosureScriptActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null -> "검사 결과가 없어도 모든 직원 공개 문구를 보수적으로 읽기"
            hasFinancialSignal -> "돈 행동을 멈추도록 값 없는 직원 도움 문장부터 고르기"
            hasOfficialPressureSignal -> "직함·사건 압박을 분리하고 직원 앞에서 숨기지 않기"
            hasLiveLinkAppCourierSignal -> "같은 연락을 벗어나 전달·설치 지시를 직원 앞에서 멈추기"
            hasCredentialSignal -> "비밀값을 말하지 않고 값 없는 멈춤 문장 고르기"
            hasRecoveryPromiseSignal -> "회복 약속과 돈·물품 전달을 함께 멈추기"
            hasFamilyVerificationSignal -> "가족 위급 주장도 값 없이 알리고 별도 경로로 확인하기"
            hasHighRisk -> "높은 위험 범주에서 직원 도움 문장과 기존 대응 경로를 넓게 고르기"
            hasSavedIncident -> "사건 내용 없이 증거·가족·도움·후속 경로만 고르기"
            else -> "민감한 값을 말하기 전에 짧은 멈춤 문장 고르기"
        }

        return BankAtmStaffDisclosureScriptCoach(
            title = "은행·ATM 직원 공개 문구 코치",
            intro =
                "은행·ATM·공공 장소·서비스 카운터 가까이에서 현금 인출·연락 유지·비밀 " +
                    "유지·물품 전달 압박을 받았을 때 금액·계좌·사건·위치·인물 값을 받지 " +
                    "않고 주변 직원이나 도우미에게 직접 읽을 고정 문장과 기존 대응 화면만 " +
                    "고르는 로컬 전용·무상태 수동 안내입니다.",
            modeLabel = "값 입력 없음 · 직원 조회 없음 · 금융 행동 없음",
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

    private fun buildCards(): List<BankAtmStaffDisclosureScriptCard> {
        fun action(
            id: BankAtmStaffDisclosureScriptActionId
        ): BankAtmStaffDisclosureScriptAction = actionFor(id)

        return listOf(
            BankAtmStaffDisclosureScriptCard(
                id = BankAtmStaffDisclosureScriptCardId
                    .DISCLOSE_CASH_WITHDRAWAL_PRESSURE_TO_BANK_ATM_STAFF,
                displayOrder = BankAtmStaffDisclosureScriptCardId
                    .DISCLOSE_CASH_WITHDRAWAL_PRESSURE_TO_BANK_ATM_STAFF.displayOrder,
                title = "은행·ATM 직원에게 현금 인출 압박을 짧게 알리기",
                staffDisclosureSituation =
                    "누군가 돈 행동을 재촉하며 금융 장소에서 연락을 계속하라고 해 가까운 직원에게 도움을 청해야 하는 때",
                valueFreeScript =
                    "누군가 돈 행동을 재촉하고 있습니다. 금액이나 계좌 내용은 말하지 않고 이 행동을 잠시 멈출 수 있게 도와주세요.",
                whyItMatters =
                    "짧은 공개 문장은 상대의 비밀 지시에서 벗어나 생각할 시간을 만들고 민감한 금융값의 재노출을 줄입니다.",
                doNotSayShareAttachOrRecordBoundaries = listOf(
                    "금액·권종·현금 일련값·계좌·카드·은행·지점·ATM·위치·주소·경로 값을 말하거나 적지 마세요.",
                    "앱은 인출·입금·송금·결제·카드·계정 동작을 하지 않고 지점·ATM·직원을 찾거나 확인하지 않습니다."
                ),
                primaryAction = action(
                    BankAtmStaffDisclosureScriptActionId
                        .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH
                ),
                secondaryActions = listOf(
                    action(BankAtmStaffDisclosureScriptActionId.QUICK_STOP),
                    action(BankAtmStaffDisclosureScriptActionId.MONEY_TRANSFER_PAUSE_GATE),
                    action(BankAtmStaffDisclosureScriptActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(
                        BankAtmStaffDisclosureScriptActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(
                        BankAtmStaffDisclosureScriptActionId
                            .OFFICIAL_PRESSURE_REALITY_CHECK_COACH
                    )
                )
            ),
            BankAtmStaffDisclosureScriptCard(
                id = BankAtmStaffDisclosureScriptCardId
                    .REQUEST_VALUE_FREE_PAUSE_BEFORE_DETAILS,
                displayOrder = BankAtmStaffDisclosureScriptCardId
                    .REQUEST_VALUE_FREE_PAUSE_BEFORE_DETAILS.displayOrder,
                title = "자세한 내용을 묻기 전에 값 없이 멈춤 요청하기",
                staffDisclosureSituation =
                    "직원이나 도우미가 금액·계좌·사건 설명을 묻기 전에 먼저 행동 중단과 안전한 대기만 요청하려는 때",
                valueFreeScript =
                    "민감한 값은 여기서 말하거나 적지 않겠습니다. 먼저 아무 행동도 이어지지 않게 잠시 기다릴 공간을 부탁드립니다.",
                whyItMatters =
                    "도움 요청과 상세 정보 공개를 분리하면 필요한 멈춤을 먼저 만들면서 금융·신원·사건값 노출을 줄일 수 있습니다.",
                doNotSayShareAttachOrRecordBoundaries = listOf(
                    "원문·가린 단서·사건 내용·신고값·계좌·카드·거래·OTP·PIN·비밀번호·보안 답을 말하거나 적지 마세요.",
                    "문서·사진·파일·화면·오디오를 촬영·첨부·업로드·URI 공유하거나 직원·사건·문서를 검증하지 않습니다."
                ),
                primaryAction = action(
                    BankAtmStaffDisclosureScriptActionId.MONEY_TRANSFER_PAUSE_GATE
                ),
                secondaryActions = listOf(
                    action(BankAtmStaffDisclosureScriptActionId.CHECKER),
                    action(BankAtmStaffDisclosureScriptActionId.ANALYSIS_RESULT),
                    action(BankAtmStaffDisclosureScriptActionId.SAFE_PASTE_PRIVACY_COACH),
                    action(
                        BankAtmStaffDisclosureScriptActionId
                            .CREDENTIAL_SECRET_FREEZE_COACH
                    ),
                    action(BankAtmStaffDisclosureScriptActionId.GIFT_CARD_CODE_PAUSE_COACH),
                    action(BankAtmStaffDisclosureScriptActionId.ACCOUNT_LOCKDOWN),
                    action(BankAtmStaffDisclosureScriptActionId.EXPOSURE_IMPACT_TRIAGE)
                )
            ),
            BankAtmStaffDisclosureScriptCard(
                id = BankAtmStaffDisclosureScriptCardId.END_CONTACT_AND_ASK_STAFF_FOR_SPACE,
                displayOrder = BankAtmStaffDisclosureScriptCardId
                    .END_CONTACT_AND_ASK_STAFF_FOR_SPACE.displayOrder,
                title = "통화·채팅을 끝내고 직원에게 혼자 지시를 따르지 않겠다고 말하기",
                staffDisclosureSituation =
                    "상대가 연락을 끊지 말고 가족이나 직원에게 숨긴 채 행동하라고 하는 때",
                valueFreeScript =
                    "상대와의 연락을 제가 직접 끝내겠습니다. 혼자 지시를 따르지 않도록 잠시 곁에 있어 주세요.",
                whyItMatters =
                    "연락 유지와 비밀 지시를 끊으면 상대가 다음 행동을 계속 통제하기 어려워지고 별도 확인을 시작할 수 있습니다.",
                doNotSayShareAttachOrRecordBoundaries = listOf(
                    "상대·직원·도우미 이름, 전화번호, 채널, 프로필, 발신 표시, 현재 위치나 목적지를 말하거나 공유하지 마세요.",
                    "통화 상태·통화 기록·문자·연락처·알림을 읽지 않고 통화 종료·차단·발신·문자 전송을 자동 수행하지 않습니다."
                ),
                primaryAction = action(
                    BankAtmStaffDisclosureScriptActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
                ),
                secondaryActions = listOf(
                    action(BankAtmStaffDisclosureScriptActionId.REMOTE_ACCESS_CUTOFF_COACH),
                    action(BankAtmStaffDisclosureScriptActionId.RECONTACT_BOUNDARY_COACH),
                    action(BankAtmStaffDisclosureScriptActionId.TRUSTED_HELPER_REVIEW),
                    action(BankAtmStaffDisclosureScriptActionId.FAMILY_ALERT_MESSAGES),
                    action(BankAtmStaffDisclosureScriptActionId.FAMILY_RESPONSE_ROLE_BOARD),
                    action(
                        BankAtmStaffDisclosureScriptActionId.FAMILY_CALM_DEBRIEF_AGENDA
                    )
                )
            ),
            BankAtmStaffDisclosureScriptCard(
                id = BankAtmStaffDisclosureScriptCardId
                    .PAUSE_COURIER_MAIL_LOCKER_HANDOFF_AT_COUNTER,
                displayOrder = BankAtmStaffDisclosureScriptCardId
                    .PAUSE_COURIER_MAIL_LOCKER_HANDOFF_AT_COUNTER.displayOrder,
                title = "택배·퀵·우편·보관함 지시를 카운터에서 값 없이 멈춤 요청하기",
                staffDisclosureSituation =
                    "현금·카드·통장·기기·문서를 카운터·관리실·택배·퀵·우편·보관함 경로로 넘기라고 하는 때",
                valueFreeScript =
                    "전달·보관·발송 지시를 지금 따르지 않겠습니다. 물품이나 위치 정보를 말하지 않고 이 행동을 멈추겠습니다.",
                whyItMatters =
                    "상대가 정한 전달 경로는 직접 만남이 아니어도 회수 흐름이 될 수 있으므로 물품과 위치값을 더 주기 전에 멈춰야 합니다.",
                doNotSayShareAttachOrRecordBoundaries = listOf(
                    "주소·경로·만남 장소·보관함·기사·송장·물품·수거·배달·인계 상태를 말하거나 기록하지 마세요.",
                    "택배·퀵·우편·보관함을 조회·접수·추적·발송·수거·예약하지 않고 지도·위치·일정을 사용하지 않습니다."
                ),
                primaryAction = action(
                    BankAtmStaffDisclosureScriptActionId
                        .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH
                ),
                secondaryActions = listOf(
                    action(
                        BankAtmStaffDisclosureScriptActionId
                            .LINK_APP_INSTALL_QUARANTINE_COACH
                    ),
                    action(BankAtmStaffDisclosureScriptActionId.GIFT_CARD_CODE_PAUSE_COACH),
                    action(
                        BankAtmStaffDisclosureScriptActionId
                            .RECOVERY_PROMISE_REALITY_CHECK_COACH
                    )
                )
            ),
            BankAtmStaffDisclosureScriptCard(
                id = BankAtmStaffDisclosureScriptCardId.SEPARATE_OFFICIAL_TITLE_FROM_PROOF,
                displayOrder = BankAtmStaffDisclosureScriptCardId
                    .SEPARATE_OFFICIAL_TITLE_FROM_PROOF.displayOrder,
                title = "기관·은행·플랫폼 직함은 확인 전 증거가 아니라고 말하기",
                staffDisclosureSituation =
                    "기관·은행·플랫폼 직함이나 사건·문서를 내세워 돈·물품·기기 전달이 의무라고 하는 때",
                valueFreeScript =
                    "상대가 말한 직함은 확인 근거로 쓰지 않겠습니다. 상대가 준 경로와 분리해 다음 도움 순서만 고르겠습니다.",
                whyItMatters =
                    "직함·문서·발신 표시는 사람이나 요청, 전달 방식이 공식이거나 안전하다는 증명이 아닙니다.",
                doNotSayShareAttachOrRecordBoundaries = listOf(
                    "기관·은행·플랫폼·사람·직함·번호·문서·사건·접수·신고 값을 말하거나 공유하지 마세요.",
                    "공식 번호·기관 목록·발신자·전화번호·문서·사건·법률 상태를 조회·검증하지 않고 전화·브라우저·문자·신고 제출을 하지 않습니다."
                ),
                primaryAction = action(
                    BankAtmStaffDisclosureScriptActionId
                        .OFFICIAL_PRESSURE_REALITY_CHECK_COACH
                ),
                secondaryActions = listOf(
                    action(
                        BankAtmStaffDisclosureScriptActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(
                        BankAtmStaffDisclosureScriptActionId
                            .VOICE_IMPERSONATION_REALITY_CHECK_COACH
                    ),
                    action(BankAtmStaffDisclosureScriptActionId.NEW_NUMBER_QUARANTINE_COACH),
                    action(
                        BankAtmStaffDisclosureScriptActionId
                            .VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    ),
                    action(BankAtmStaffDisclosureScriptActionId.OFFICIAL_HELP_DIRECTORY),
                    action(BankAtmStaffDisclosureScriptActionId.REPORT_PREP)
                )
            ),
            BankAtmStaffDisclosureScriptCard(
                id = BankAtmStaffDisclosureScriptCardId
                    .CHOOSE_FAMILY_HELP_REPORT_NEXT_SENTENCE,
                displayOrder = BankAtmStaffDisclosureScriptCardId
                    .CHOOSE_FAMILY_HELP_REPORT_NEXT_SENTENCE.displayOrder,
                title = "가족·보호자·공식 도움·신고 준비로 이어갈 다음 한 문장 고르기",
                staffDisclosureSituation =
                    "값 없는 도움 요청 뒤 가족 검토·증거 개인정보·공식 도움·신고 준비·후속 점검 순서를 고르려는 때",
                valueFreeScript =
                    "민감한 내용을 말하지 않고 가족이나 보호자와 상의한 뒤 기존 도움·신고 준비 안내를 직접 확인하겠습니다.",
                whyItMatters =
                    "값 없는 다음 문장은 사건·금융·신원값을 반복 공개하지 않으면서 이미 있는 안전 흐름으로 이어 줍니다.",
                doNotSayShareAttachOrRecordBoundaries = listOf(
                    "원문·가린 단서·사건 내용·가족 안전단어·콜백 문장·연락처·사람·금액·문서·결과 상태를 말하거나 공유하지 마세요.",
                    "증거를 촬영·녹음·스캔·첨부·업로드하지 않고 공식 도움 조회·신고 제출·법률·금융 결과를 만들거나 저장하지 않습니다."
                ),
                primaryAction = action(
                    BankAtmStaffDisclosureScriptActionId.TRUSTED_HELPER_REVIEW
                ),
                secondaryActions = listOf(
                    action(BankAtmStaffDisclosureScriptActionId.SAFETY_PLAN),
                    action(
                        BankAtmStaffDisclosureScriptActionId.FAMILY_READINESS_ROADMAP
                    ),
                    action(
                        BankAtmStaffDisclosureScriptActionId.FAMILY_SAFETY_DRILL_COACH
                    ),
                    action(BankAtmStaffDisclosureScriptActionId.FAMILY_CALLBACK_CARD),
                    action(
                        BankAtmStaffDisclosureScriptActionId.FAMILY_SAFE_WORD_RESET_COACH
                    ),
                    action(BankAtmStaffDisclosureScriptActionId.SAFE_CALL_SCRIPTS),
                    action(BankAtmStaffDisclosureScriptActionId.EVIDENCE_HANDOFF_PRIVACY_COACH),
                    action(BankAtmStaffDisclosureScriptActionId.EVIDENCE_KIT),
                    action(
                        BankAtmStaffDisclosureScriptActionId.INCIDENT_TIMELINE_BRIEFING
                    ),
                    action(BankAtmStaffDisclosureScriptActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                    action(BankAtmStaffDisclosureScriptActionId.SAFETY_FLOW_ROUTER),
                    action(BankAtmStaffDisclosureScriptActionId.PRIVACY_CENTER)
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
                    "검사 결과나 일치 신호가 없어도 은행·ATM·직원·카운터·보안요원·" +
                        "도우미·기사·기관·사람·발신자·문서·사건·결제·요청·전달 상태를 " +
                        "공식·검증·안전·유효·접수·해결·완료라고 단정하지 않고 모든 값 없는 " +
                        "직원 공개 문구와 기존 대응 경로를 보수적으로 보여 줍니다."
                )
            }
            if (hasHighRisk) {
                add(
                    "높은 위험 범주에서는 돈·대면 전달 멈춤, 연락 종료, 별도 확인, 가족, " +
                        "계정·기기·노출·증거·공식 도움·후속 대응 경로를 값 없이 넓게 보여 줍니다."
                )
            }
            if (hasFinancialSignal) {
                add(
                    "돈 관련 범주에서는 현금·대면 전달 멈춤, 송금 멈춤, 연락 종료, 기관 " +
                        "압박, 별도 확인, 계정·노출·증거 개인정보·보호자·공식 도움·신고 준비 " +
                        "화면을 고르며 은행·ATM·현금·카드·결제 동작이나 지도·위치·지점 조회를 하지 않습니다."
                )
            }
            if (hasOfficialPressureSignal) {
                add(
                    "사칭·비밀 유지 범주에서는 기관 압박, 연락 종료, 별도 확인, 가족 알림·" +
                        "차분 대화, 증거 개인정보, 보호자, 공식 도움·신고 준비 화면을 고르며 " +
                        "공식 번호·문서·사건을 검증하거나 전화·브라우저·문자·신고 제출을 하지 않습니다."
                )
            }
            if (hasLiveLinkAppCourierSignal) {
                add(
                    "통화 유지·앱·링크·택배 문자 범주에서는 연락 종료, 링크·앱 격리, " +
                        "원격지시 중단, 노출·계정·개인정보 화면을 고르며 통화 상태, 앱·패키지·" +
                        "기기·설정을 읽거나 지도·위치·택배 추적·원격 연결 종료·기기 정리를 하지 않습니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증·상품권·결제 범주에서는 비밀값 멈춤, 상품권 코드 멈춤, 계정 잠금, " +
                        "안전 붙여넣기 개인정보, 원격지시 중단, 노출 분류 화면을 고르며 OTP·" +
                        "PIN·비밀번호·계좌·카드·은행·지갑·상품권·현금 값을 사용하지 않습니다."
                )
            }
            if (hasRecoveryPromiseSignal) {
                add(
                    "회복 약속 범주에서는 회복 현실 점검, 돈·대면 전달 멈춤, 증거 개인정보, " +
                        "보호자, 공식 도움·신고 준비 화면을 고르며 환급·차지백·취소·동결·" +
                        "추적·회복 결과를 수행하거나 보장하지 않습니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 긴급·목소리 사칭·번호 변경 범주에서는 별도 확인, 가족 콜백, 목소리 " +
                        "현실 점검, 새 번호 격리, 안전단어 재정비, 값 없는 전화 문구, 가족 " +
                        "알림 화면을 고르며 연락처·안전단어·콜백 값을 사용하거나 통화·문자를 읽지 않습니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만으로 증거 개인정보·증거 체크·사건 흐름·" +
                        "후속 점검 화면을 더 보여 주며 사건 내용이나 미리보기를 읽지 않습니다."
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
                "가족 준비가 되어 있다는 범주는 직원·도우미·은행·지점·ATM·현금·인출·" +
                    "전달·인계·요청이 안전하거나 신뢰할 수 있거나 공식이라는 뜻이 아닙니다. " +
                    "준비 상태와 관계없이 값 없는 도움 문장과 별도 확인을 직접 고르세요."
        }
        return listOf(situationCopy, readinessCopy).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 고정 문장을 사용자가 직접 읽고 기존 앱 화면을 직접 고르는 로컬 전용·무상태 수동 안내이며 직원·도우미·은행·ATM·지점·카운터·위치·금액·계좌·카드·사건 값을 입력하라고 요구하지 않습니다.",
        "추천은 현재 검사 결과의 위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기·가족 안전 계획의 실제 값을 사용하지 않습니다.",
        "현금 인출·입금·송금·결제·계좌·카드·통장·은행·ATM·NFC·지갑·블록체인·상품권 기능 또는 API를 사용하지 않고 취소·동결·차단·환급·차지백·추적·회복을 수행·조회·제출·보장하지 않습니다.",
        "지도·위치·주소·경로·길 안내·내비게이션·지오코딩·주소 검증을 사용하지 않고 은행·ATM·지점·상점·카운터·만남 장소를 찾거나 기록·검증·추천하지 않습니다.",
        "직원·보안요원·도우미·기사·수사관·기관·사람의 신원이나 직함을 조회·확인·검증하지 않고 택배·퀵·우편·보관함·기사를 추적하거나 이동·만남·전달·수거·배차·예약·일정을 만들지 않습니다.",
        "네트워크·웹 탐색·브라우저·웹뷰를 사용하지 않고 기관 정보·공식 번호·디렉터리·발신자 표시·전화번호·문서·사건·신고·접수·영수증·법률·금융 상태를 조회·검증하거나 신고를 제출하지 않습니다.",
        "전화·다이얼·발신·통화·종료·선별·차단·통화 상태·통화 기록·문자·SMS·받는 사람 선택·자동 전송을 사용하지 않고 연락처·주소록·알림을 읽기·쓰기·편집·선택·감시·필터링하지 않습니다.",
        "QR·바코드·OCR·카메라·이미지 분석·화면 캡처를 사용하지 않고 파일·문서·사진·스크린샷·오디오·미디어·저장소를 읽거나 선택·촬영·녹음·전사·첨부·스트림·URI 공유·업로드·동기화하지 않습니다.",
        "클립보드·텍스트 감시기·자동완성·자격 증명·계정·로그인·인증·생체 인증·키가드·키 저장소·암호화 API를 사용하지 않고 계정·인증 상태를 읽거나 바꾸지 않습니다.",
        "OTP·PIN·비밀번호·보안 답·주민 식별값·안전단어·콜백 문장·연락처 이름·번호·계좌·카드·은행·지갑·거래·금액·현금 일련값·코드 값을 읽기·표시·저장·복사·붙여넣기·자동 입력·공유·제출하지 않습니다.",
        "앱·패키지·설치 앱 목록·앱 이름·권한·기기 상태를 검사하거나 읽지 않고 설치·제거·비활성화·강제 종료·권한 허용·철회·설정 변경·기기 정리·원격 연결 종료를 수행하지 않습니다.",
        "접근성 서비스·기기 관리자·VPN·사용 통계·알림 수신 서비스·권한 관리자·패키지 설치 도구·기기 관리 기능을 사용하지 않습니다.",
        "직원·도우미·기사·수사관·기관·은행·지점·ATM·상점·카운터·주소·경로·택배·현금·문서·사건·신고·계좌·카드·기기·결제·전달·환급·회복 상태가 공식·실제·안전·보안·검증·유효·접수·제출·수신·보호·적법·완료·해결·차단·취소·역전·회복·환급·동결·추적·배달·수거·입금·예약·보장·문제없음 상태라고 판정하거나 증명하지 않습니다.",
        "수동 문자 공유는 고정 카드 제목과 기존 앱 경로·행동 종류만 담고 원문·가린 단서·계획·사건·기관 목록·연락처·채널·프로필·계정·결제·지갑·링크·앱·위치·직원·기사·현금·기기·받는 사람·확인·제출·전달·회복 결과를 담거나 자동 발송하지 않습니다.",
        "새 Android 권한을 요청하지 않고 알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러·브로드캐스트 수신기를 만들거나 예약하지 않습니다.",
        "새 저장소를 만들지 않고 카드 선택·추천·읽음·공유·공개·확인·직원·도우미·금액·위치·경로·만남·전달·계좌·결제·증거·신고·회복·결과·진행·상태·이력을 저장하지 않으며 가족 계획·사건·디렉터리·연락처·알림·통화·문자·위치·계정·결제·기기 상태를 수정하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<BankAtmStaffDisclosureScriptCardId>,
        recommendedActions: List<BankAtmStaffDisclosureScriptAction>,
        cards: List<BankAtmStaffDisclosureScriptCard>
    ): String {
        val cardById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[은행·ATM 직원 공개 문구 코치]")
            appendLine("민감한 값을 말하지 않고 주변 직원에게 직접 읽을 문장을 고르는 로컬 수동 안내입니다.")
            appendLine()
            appendLine("추천 값 없는 직원 공개 문구 카드")
            recommendedCardIds.forEach { cardId ->
                val card = cardById.getValue(cardId)
                appendLine("- ${card.title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 실제 금액·위치·사람·연락처·계정·카드·문서·사건·현금·기기 값은 보내지 않습니다.")
            append("- 이 안내는 금융 행동·직원 확인·위치 조회·전달·추적·예약·검증·신고·환급·회복을 수행하거나 보장하지 않습니다.")
        }
    }

    private fun actionFor(
        id: BankAtmStaffDisclosureScriptActionId
    ): BankAtmStaffDisclosureScriptAction = BankAtmStaffDisclosureScriptAction(
        id = id,
        label = when (id) {
            BankAtmStaffDisclosureScriptActionId.QUICK_STOP -> "긴급 멈춤 모드"
            BankAtmStaffDisclosureScriptActionId.CHECKER -> "의심 문구 검사"
            BankAtmStaffDisclosureScriptActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            BankAtmStaffDisclosureScriptActionId.SAFETY_PLAN -> "가족 안전 계획"
            BankAtmStaffDisclosureScriptActionId.FAMILY_READINESS_ROADMAP ->
                "가족 준비 로드맵"
            BankAtmStaffDisclosureScriptActionId.FAMILY_SAFETY_DRILL_COACH ->
                "가족 안전 연습 코치"
            BankAtmStaffDisclosureScriptActionId.SAFE_PASTE_PRIVACY_COACH ->
                "안전 붙여넣기 개인정보 코치"
            BankAtmStaffDisclosureScriptActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                "현재 연락을 끝내는 문장"
            BankAtmStaffDisclosureScriptActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 카드"
            BankAtmStaffDisclosureScriptActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                "가족 안전단어 재정비 코치"
            BankAtmStaffDisclosureScriptActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                "별도 경로 확인 코치"
            BankAtmStaffDisclosureScriptActionId.SAFE_CALL_SCRIPTS ->
                "값 없는 안전 확인 전화 문구"
            BankAtmStaffDisclosureScriptActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                "목소리 사칭 현실 점검"
            BankAtmStaffDisclosureScriptActionId.NEW_NUMBER_QUARANTINE_COACH ->
                "새 연락수단 격리 코치"
            BankAtmStaffDisclosureScriptActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                "자격 비밀값 멈춤 코치"
            BankAtmStaffDisclosureScriptActionId.FAMILY_ALERT_MESSAGES -> "가족 알림 문구"
            BankAtmStaffDisclosureScriptActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                "확인 후속 행동 보드"
            BankAtmStaffDisclosureScriptActionId.RECONTACT_BOUNDARY_COACH ->
                "재접촉 경계 코치"
            BankAtmStaffDisclosureScriptActionId.TRUSTED_HELPER_REVIEW -> "보호자 검토 모드"
            BankAtmStaffDisclosureScriptActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                "가족 대응 역할 보드"
            BankAtmStaffDisclosureScriptActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                "가족 차분 대화 안건"
            BankAtmStaffDisclosureScriptActionId
                .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH ->
                "현금 인출·택배/퀵 대면 전달 멈춤 코치"
            BankAtmStaffDisclosureScriptActionId.MONEY_TRANSFER_PAUSE_GATE ->
                "송금 전 멈춤 확인"
            BankAtmStaffDisclosureScriptActionId.GIFT_CARD_CODE_PAUSE_COACH ->
                "상품권·코드 멈춤 코치"
            BankAtmStaffDisclosureScriptActionId.LINK_APP_INSTALL_QUARANTINE_COACH ->
                "링크·앱 설치 격리 코치"
            BankAtmStaffDisclosureScriptActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                "기관 사칭·압박 현실 점검"
            BankAtmStaffDisclosureScriptActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH ->
                "회복 약속 현실 점검"
            BankAtmStaffDisclosureScriptActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크"
            BankAtmStaffDisclosureScriptActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                "원격 접근 중단 코치"
            BankAtmStaffDisclosureScriptActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
            BankAtmStaffDisclosureScriptActionId.PRIVACY_CENTER -> "개인정보 센터"
            BankAtmStaffDisclosureScriptActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                "증거 전달 개인정보 코치"
            BankAtmStaffDisclosureScriptActionId.EVIDENCE_KIT -> "증거 보존 체크"
            BankAtmStaffDisclosureScriptActionId.INCIDENT_TIMELINE_BRIEFING ->
                "사건 흐름 요약"
            BankAtmStaffDisclosureScriptActionId.OFFICIAL_HELP_DIRECTORY ->
                "공식 도움 연락처"
            BankAtmStaffDisclosureScriptActionId.REPORT_PREP -> "공식 신고 준비"
            BankAtmStaffDisclosureScriptActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                "후속 점검 계획"
            BankAtmStaffDisclosureScriptActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
