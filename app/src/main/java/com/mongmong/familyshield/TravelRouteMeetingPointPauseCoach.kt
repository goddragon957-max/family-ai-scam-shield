package com.mongmong.familyshield

enum class TravelRouteMeetingPointPauseCardId(val displayOrder: Int) {
    STOP_MOVEMENT_BEFORE_DEPARTURE(1),
    DO_NOT_SHARE_DESTINATION_LOCATION_TRANSPORT_OR_ARRIVAL(2),
    SEPARATE_BANK_ATM_AND_OFFICIAL_DESTINATION_PRESSURE(3),
    REFUSE_COURIER_LOCKER_PARKING_LOBBY_OR_STORE_MEETING(4),
    END_CONTACT_AND_WAIT_WITHOUT_TRAVEL_OR_MEETING(5),
    CHOOSE_FAMILY_HELP_REPORT_AND_FOLLOW_UP_ROUTE(6)
}

enum class TravelRouteMeetingPointPauseActionId {
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
    DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH,
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

data class TravelRouteMeetingPointPauseAction(
    val id: TravelRouteMeetingPointPauseActionId,
    val label: String
)

data class TravelRouteMeetingPointPauseCard(
    val id: TravelRouteMeetingPointPauseCardId,
    val displayOrder: Int,
    val title: String,
    val movementMeetingPressureSituation: String,
    val valueFreePauseScript: String,
    val whyItMatters: String,
    val doNotTravelNavigateShareLocationMeetHandoffSayShareAttachOrRecordBoundaries: List<String>,
    val primaryAction: TravelRouteMeetingPointPauseAction,
    val secondaryActions: List<TravelRouteMeetingPointPauseAction>
) {
    val routeActions: List<TravelRouteMeetingPointPauseAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class TravelRouteMeetingPointPauseCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<TravelRouteMeetingPointPauseCardId>,
    val recommendedActions: List<TravelRouteMeetingPointPauseAction>,
    val cards: List<TravelRouteMeetingPointPauseCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<TravelRouteMeetingPointPauseCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<TravelRouteMeetingPointPauseActionId>
        get() = recommendedActions.map { action -> action.id }
}

object TravelRouteMeetingPointPauseCoachBuilder {
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
    ): TravelRouteMeetingPointPauseCoach {
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
            TravelRouteMeetingPointPauseCardId.STOP_MOVEMENT_BEFORE_DEPARTURE,
            TravelRouteMeetingPointPauseCardId
                .DO_NOT_SHARE_DESTINATION_LOCATION_TRANSPORT_OR_ARRIVAL,
            TravelRouteMeetingPointPauseCardId.END_CONTACT_AND_WAIT_WITHOUT_TRAVEL_OR_MEETING
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += TravelRouteMeetingPointPauseCardId.entries
        }
        if (hasFinancialSignal || hasRecoveryPromiseSignal) {
            recommendedCardSet += TravelRouteMeetingPointPauseCardId.STOP_MOVEMENT_BEFORE_DEPARTURE
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .SEPARATE_BANK_ATM_AND_OFFICIAL_DESTINATION_PRESSURE
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .REFUSE_COURIER_LOCKER_PARKING_LOBBY_OR_STORE_MEETING
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .END_CONTACT_AND_WAIT_WITHOUT_TRAVEL_OR_MEETING
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .CHOOSE_FAMILY_HELP_REPORT_AND_FOLLOW_UP_ROUTE
        }
        if (hasOfficialPressureSignal) {
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .DO_NOT_SHARE_DESTINATION_LOCATION_TRANSPORT_OR_ARRIVAL
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .SEPARATE_BANK_ATM_AND_OFFICIAL_DESTINATION_PRESSURE
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .END_CONTACT_AND_WAIT_WITHOUT_TRAVEL_OR_MEETING
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .CHOOSE_FAMILY_HELP_REPORT_AND_FOLLOW_UP_ROUTE
        }
        if (hasLiveLinkAppCourierSignal) {
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .DO_NOT_SHARE_DESTINATION_LOCATION_TRANSPORT_OR_ARRIVAL
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .REFUSE_COURIER_LOCKER_PARKING_LOBBY_OR_STORE_MEETING
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .END_CONTACT_AND_WAIT_WITHOUT_TRAVEL_OR_MEETING
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .CHOOSE_FAMILY_HELP_REPORT_AND_FOLLOW_UP_ROUTE
        }
        if (hasCredentialSignal) {
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .DO_NOT_SHARE_DESTINATION_LOCATION_TRANSPORT_OR_ARRIVAL
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .CHOOSE_FAMILY_HELP_REPORT_AND_FOLLOW_UP_ROUTE
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .DO_NOT_SHARE_DESTINATION_LOCATION_TRANSPORT_OR_ARRIVAL
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .END_CONTACT_AND_WAIT_WITHOUT_TRAVEL_OR_MEETING
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .CHOOSE_FAMILY_HELP_REPORT_AND_FOLLOW_UP_ROUTE
        }
        if (hasSavedIncident) {
            recommendedCardSet += TravelRouteMeetingPointPauseCardId
                .CHOOSE_FAMILY_HELP_REPORT_AND_FOLLOW_UP_ROUTE
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedCardSet += TravelRouteMeetingPointPauseCardId
                    .END_CONTACT_AND_WAIT_WITHOUT_TRAVEL_OR_MEETING
                recommendedCardSet += TravelRouteMeetingPointPauseCardId
                    .CHOOSE_FAMILY_HELP_REPORT_AND_FOLLOW_UP_ROUTE
            }
            ReadinessLevel.READY -> {
                recommendedCardSet += TravelRouteMeetingPointPauseCardId
                    .STOP_MOVEMENT_BEFORE_DEPARTURE
                recommendedCardSet += TravelRouteMeetingPointPauseCardId
                    .DO_NOT_SHARE_DESTINATION_LOCATION_TRANSPORT_OR_ARRIVAL
            }
        }
        val recommendedCardIds = TravelRouteMeetingPointPauseCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            TravelRouteMeetingPointPauseActionId.CHECKER
        } else {
            TravelRouteMeetingPointPauseActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            currentResultActionId,
            TravelRouteMeetingPointPauseActionId.QUICK_STOP,
            TravelRouteMeetingPointPauseActionId
                .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH,
            TravelRouteMeetingPointPauseActionId.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH,
            TravelRouteMeetingPointPauseActionId.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH,
            TravelRouteMeetingPointPauseActionId.MONEY_TRANSFER_PAUSE_GATE,
            TravelRouteMeetingPointPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
            TravelRouteMeetingPointPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
            TravelRouteMeetingPointPauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
            TravelRouteMeetingPointPauseActionId.TRUSTED_HELPER_REVIEW,
            TravelRouteMeetingPointPauseActionId.PRIVACY_CENTER,
            TravelRouteMeetingPointPauseActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            TravelRouteMeetingPointPauseActionId.entries
                .filterNot { actionId ->
                    actionId == TravelRouteMeetingPointPauseActionId.ANALYSIS_RESULT
                }
                .forEach { actionId -> recommendedActionSet += actionId }
        }
        if (hasHighRisk) {
            TravelRouteMeetingPointPauseActionId.entries.forEach { actionId ->
                recommendedActionSet += actionId
            }
        }
        if (hasFinancialSignal) {
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.REPORT_PREP
        }
        if (hasOfficialPressureSignal) {
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.REPORT_PREP
        }
        if (hasLiveLinkAppCourierSignal) {
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .LINK_APP_INSTALL_QUARANTINE_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.PRIVACY_CENTER
        }
        if (hasCredentialSignal) {
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.GIFT_CARD_CODE_PAUSE_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.EXPOSURE_IMPACT_TRIAGE
        }
        if (hasRecoveryPromiseSignal) {
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .RECOVERY_PROMISE_REALITY_CHECK_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.REPORT_PREP
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .VOICE_IMPERSONATION_REALITY_CHECK_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.NEW_NUMBER_QUARANTINE_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .FAMILY_SAFE_WORD_RESET_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasSavedIncident) {
            recommendedActionSet += TravelRouteMeetingPointPauseActionId
                .EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.EVIDENCE_KIT
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.INCIDENT_TIMELINE_BRIEFING
            recommendedActionSet += TravelRouteMeetingPointPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedActionSet += TravelRouteMeetingPointPauseActionId.SAFETY_PLAN
                recommendedActionSet += TravelRouteMeetingPointPauseActionId
                    .FAMILY_SAFE_WORD_RESET_COACH
                recommendedActionSet += TravelRouteMeetingPointPauseActionId
                    .FAMILY_READINESS_ROADMAP
                recommendedActionSet += TravelRouteMeetingPointPauseActionId
                    .FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += TravelRouteMeetingPointPauseActionId.SAFE_CALL_SCRIPTS
            }
            ReadinessLevel.READY -> {
                recommendedActionSet += TravelRouteMeetingPointPauseActionId.FAMILY_CALLBACK_CARD
                recommendedActionSet += TravelRouteMeetingPointPauseActionId
                    .SEPARATE_CHANNEL_VERIFICATION_COACH
                recommendedActionSet += TravelRouteMeetingPointPauseActionId.SAFE_CALL_SCRIPTS
            }
        }
        val recommendedActions = TravelRouteMeetingPointPauseActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null -> "검사 결과가 없어도 모든 이동·만남 멈춤 문구를 보수적으로 읽기"
            hasRecoveryPromiseSignal -> "회복 약속과 이동·만남 지시를 함께 멈추기"
            hasFinancialSignal -> "은행·ATM·전달 장소로 움직이기 전에 이동을 멈추기"
            hasOfficialPressureSignal -> "기관 직함과 목적지 압박을 이동 근거에서 분리하기"
            hasLiveLinkAppCourierSignal -> "연락을 끝내고 위치·경로·도착 정보를 말하지 않기"
            hasCredentialSignal -> "비밀값을 말하지 않고 이동·만남 요청도 멈추기"
            hasFamilyVerificationSignal -> "가족 위급 주장도 이동 없이 별도 경로로 확인하기"
            hasHighRisk -> "높은 위험 범주에서 모든 이동·만남 멈춤 문구와 기존 경로 보기"
            hasSavedIncident -> "사건 내용 없이 증거·가족·도움·후속 경로만 고르기"
            else -> "출발하거나 위치를 말하기 전에 짧은 멈춤 문구 고르기"
        }

        return TravelRouteMeetingPointPauseCoach(
            title = "이동 경로·만남 장소 멈춤 코치",
            intro =
                "누군가 지금 집이나 직장을 떠나 걷거나 차량·대중교통으로 은행·ATM·기관·" +
                    "카운터·주차장·로비·보관함·상점 같은 장소로 가서 사람을 만나라고 할 때 " +
                    "현재 위치·주소·경로·목적지·교통수단·도착 시간·사람 값을 받지 않고 " +
                    "고정 멈춤 문장과 기존 대응 화면만 고르는 로컬 전용·무상태 수동 안내입니다.",
            modeLabel = "값 입력 없음 · 위치/지도 접근 없음 · 이동/만남 행동 없음",
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

    private fun buildCards(): List<TravelRouteMeetingPointPauseCard> {
        fun action(
            id: TravelRouteMeetingPointPauseActionId
        ): TravelRouteMeetingPointPauseAction = actionFor(id)

        return listOf(
            TravelRouteMeetingPointPauseCard(
                id = TravelRouteMeetingPointPauseCardId.STOP_MOVEMENT_BEFORE_DEPARTURE,
                displayOrder = TravelRouteMeetingPointPauseCardId
                    .STOP_MOVEMENT_BEFORE_DEPARTURE.displayOrder,
                title = "출발하기 전 통화가 정한 이동 지시 멈추기",
                movementMeetingPressureSituation =
                    "상대가 전화를 유지하거나 비밀로 하라며 지금 집·직장·현재 장소를 떠나 정해 준 곳으로 움직이라고 재촉하는 때",
                valueFreePauseScript =
                    "지금 출발하거나 이동하지 않겠습니다. 이 연락과 분리해 다음 행동을 직접 고르겠습니다.",
                whyItMatters =
                    "출발 전에 멈추면 상대가 정한 속도와 이동 통제에서 벗어나 위치나 목적지 단서를 더 주지 않고 기존 안전 경로를 고를 시간이 생깁니다.",
                doNotTravelNavigateShareLocationMeetHandoffSayShareAttachOrRecordBoundaries =
                    listOf(
                        "걷거나 운전하거나 차량·대중교통에 타지 말고 현재 위치·출발지·목적지·경로·교통수단·도착 시간을 말하거나 적지 마세요.",
                        "앱은 위치·지도·경로·길 안내·교통 정보를 읽거나 출발·이동·탑승·도착 상태를 확인하지 않습니다."
                    ),
                primaryAction = action(TravelRouteMeetingPointPauseActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(TravelRouteMeetingPointPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(
                        TravelRouteMeetingPointPauseActionId
                            .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH
                    ),
                    action(
                        TravelRouteMeetingPointPauseActionId
                            .BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH
                    ),
                    action(
                        TravelRouteMeetingPointPauseActionId
                            .DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH
                    ),
                    action(TravelRouteMeetingPointPauseActionId.MONEY_TRANSFER_PAUSE_GATE)
                )
            ),
            TravelRouteMeetingPointPauseCard(
                id = TravelRouteMeetingPointPauseCardId
                    .DO_NOT_SHARE_DESTINATION_LOCATION_TRANSPORT_OR_ARRIVAL,
                displayOrder = TravelRouteMeetingPointPauseCardId
                    .DO_NOT_SHARE_DESTINATION_LOCATION_TRANSPORT_OR_ARRIVAL.displayOrder,
                title = "목적지·현재 위치·교통수단·도착 시간을 말하지 않기",
                movementMeetingPressureSituation =
                    "상대가 어디에 있는지, 어느 길로 가는지, 무엇을 타는지, 언제 닿는지 계속 보고하라고 하는 때",
                valueFreePauseScript =
                    "현재 위치나 이동 정보를 말하지 않겠습니다. 이동과 만남 요청을 여기서 멈추겠습니다.",
                whyItMatters =
                    "값 없는 짧은 거절은 주소·경로·교통수단·시간 단서를 다시 드러내지 않으면서 실시간 이동 통제를 끊습니다.",
                doNotTravelNavigateShareLocationMeetHandoffSayShareAttachOrRecordBoundaries =
                    listOf(
                        "집·직장·건물·로비·호수·주소·현재 위치·경로·목적지·랜드마크·주차·교통수단·도착 시간을 말하거나 공유하지 마세요.",
                        "위치 공유·지도·내비게이션·지오코딩·주소 검증·교통·대중교통·택시·배차 기능을 사용하지 않습니다."
                    ),
                primaryAction = action(
                    TravelRouteMeetingPointPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
                ),
                secondaryActions = listOf(
                    action(
                        TravelRouteMeetingPointPauseActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(TravelRouteMeetingPointPauseActionId.RECONTACT_BOUNDARY_COACH),
                    action(TravelRouteMeetingPointPauseActionId.FAMILY_ALERT_MESSAGES),
                    action(TravelRouteMeetingPointPauseActionId.TRUSTED_HELPER_REVIEW),
                    action(TravelRouteMeetingPointPauseActionId.PRIVACY_CENTER)
                )
            ),
            TravelRouteMeetingPointPauseCard(
                id = TravelRouteMeetingPointPauseCardId
                    .SEPARATE_BANK_ATM_AND_OFFICIAL_DESTINATION_PRESSURE,
                displayOrder = TravelRouteMeetingPointPauseCardId
                    .SEPARATE_BANK_ATM_AND_OFFICIAL_DESTINATION_PRESSURE.displayOrder,
                title = "은행·ATM·기관·법원·경찰서 이동 압박을 현실 점검으로 분리하기",
                movementMeetingPressureSituation =
                    "상대가 기관·수사·금융 직함이나 문서·사건을 내세우며 특정 장소로 가는 일이 의무라고 하는 때",
                valueFreePauseScript =
                    "상대가 말한 직함과 장소를 이동 근거로 쓰지 않겠습니다. 이동하지 않고 별도 확인 순서만 직접 고르겠습니다.",
                whyItMatters =
                    "직함·문서·발신 표시는 상대가 정한 목적지나 만남 요청이 공식이거나 안전하다는 증명이 아닙니다.",
                doNotTravelNavigateShareLocationMeetHandoffSayShareAttachOrRecordBoundaries =
                    listOf(
                        "기관·은행·지점·직원·수사관·사람 이름, 번호, 주소, 문서, 사건, 접수 값을 말하거나 공유하지 마세요.",
                        "공식 번호·기관 목록·목적지·직원·수사관·문서·사건을 조회·검증하지 않고 전화·브라우저·문자·신고 제출을 하지 않습니다."
                    ),
                primaryAction = action(
                    TravelRouteMeetingPointPauseActionId
                        .OFFICIAL_PRESSURE_REALITY_CHECK_COACH
                ),
                secondaryActions = listOf(
                    action(
                        TravelRouteMeetingPointPauseActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(
                        TravelRouteMeetingPointPauseActionId
                            .BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH
                    ),
                    action(TravelRouteMeetingPointPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(
                        TravelRouteMeetingPointPauseActionId
                            .VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    ),
                    action(TravelRouteMeetingPointPauseActionId.OFFICIAL_HELP_DIRECTORY),
                    action(TravelRouteMeetingPointPauseActionId.REPORT_PREP)
                )
            ),
            TravelRouteMeetingPointPauseCard(
                id = TravelRouteMeetingPointPauseCardId
                    .REFUSE_COURIER_LOCKER_PARKING_LOBBY_OR_STORE_MEETING,
                displayOrder = TravelRouteMeetingPointPauseCardId
                    .REFUSE_COURIER_LOCKER_PARKING_LOBBY_OR_STORE_MEETING.displayOrder,
                title = "택배/퀵·보관함·주차장·로비 만남 지시를 값 없이 멈추기",
                movementMeetingPressureSituation =
                    "상대가 기사나 사람을 만나거나 카운터·보관함·주차장·로비·상점에서 물품을 주고받으라고 하는 때",
                valueFreePauseScript =
                    "사람을 만나거나 물품을 건네지 않겠습니다. 장소로 이동하지 않고 이 지시를 멈추겠습니다.",
                whyItMatters =
                    "이동과 대면 인계를 함께 거절하면 상대가 정한 사람·장소·시간 흐름으로 돈이나 물품을 가져가는 일을 막을 여지가 생깁니다.",
                doNotTravelNavigateShareLocationMeetHandoffSayShareAttachOrRecordBoundaries =
                    listOf(
                        "사람을 만나거나 현금·카드·통장·기기·서류·봉투·물품을 건네지 말고 장소·기사·차량·보관함·도착 값을 말하지 마세요.",
                        "택배·퀵·보관함·배차·수거·배송을 조회·예약·접수·추적하지 않고 만남·이동·전달 일정을 만들지 않습니다."
                    ),
                primaryAction = action(
                    TravelRouteMeetingPointPauseActionId
                        .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH
                ),
                secondaryActions = listOf(
                    action(
                        TravelRouteMeetingPointPauseActionId
                            .DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH
                    ),
                    action(
                        TravelRouteMeetingPointPauseActionId
                            .BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH
                    ),
                    action(TravelRouteMeetingPointPauseActionId.MONEY_TRANSFER_PAUSE_GATE),
                    action(TravelRouteMeetingPointPauseActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(
                        TravelRouteMeetingPointPauseActionId
                            .EVIDENCE_HANDOFF_PRIVACY_COACH
                    )
                )
            ),
            TravelRouteMeetingPointPauseCard(
                id = TravelRouteMeetingPointPauseCardId
                    .END_CONTACT_AND_WAIT_WITHOUT_TRAVEL_OR_MEETING,
                displayOrder = TravelRouteMeetingPointPauseCardId
                    .END_CONTACT_AND_WAIT_WITHOUT_TRAVEL_OR_MEETING.displayOrder,
                title = "통화·채팅을 끊고 이동 대신 가까운 안전한 사람·장소에서 대기하기",
                movementMeetingPressureSituation =
                    "상대가 통화나 채팅을 유지한 채 앱·링크를 열고 가족에게 숨기며 계속 이동하라고 하는 때",
                valueFreePauseScript =
                    "이 연락을 직접 끝내고 더 이동하지 않겠습니다. 민감한 내용을 말하지 않고 가까운 도움을 직접 고르겠습니다.",
                whyItMatters =
                    "실시간 연락과 이동을 함께 멈추면 상대가 화면·경로·만남 행동을 계속 통제하기 어려워집니다.",
                doNotTravelNavigateShareLocationMeetHandoffSayShareAttachOrRecordBoundaries =
                    listOf(
                        "전화번호·채널·프로필·링크·앱·화면·현재 위치·경로·목적지·차량·도착 정보를 말하거나 공유하지 마세요.",
                        "통화 상태·문자·연락처·알림·앱·패키지·기기·설정을 읽거나 통화 종료·차단·삭제·설정 변경·원격 연결 종료를 자동 수행하지 않습니다."
                    ),
                primaryAction = action(
                    TravelRouteMeetingPointPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
                ),
                secondaryActions = listOf(
                    action(
                        TravelRouteMeetingPointPauseActionId
                            .LINK_APP_INSTALL_QUARANTINE_COACH
                    ),
                    action(TravelRouteMeetingPointPauseActionId.REMOTE_ACCESS_CUTOFF_COACH),
                    action(TravelRouteMeetingPointPauseActionId.RECONTACT_BOUNDARY_COACH),
                    action(TravelRouteMeetingPointPauseActionId.FAMILY_ALERT_MESSAGES),
                    action(TravelRouteMeetingPointPauseActionId.PRIVACY_CENTER)
                )
            ),
            TravelRouteMeetingPointPauseCard(
                id = TravelRouteMeetingPointPauseCardId
                    .CHOOSE_FAMILY_HELP_REPORT_AND_FOLLOW_UP_ROUTE,
                displayOrder = TravelRouteMeetingPointPauseCardId
                    .CHOOSE_FAMILY_HELP_REPORT_AND_FOLLOW_UP_ROUTE.displayOrder,
                title = "가족·보호자·공식 도움·신고 준비·후속 점검 경로 고르기",
                movementMeetingPressureSituation =
                    "이동과 만남을 멈춘 뒤 가족 확인·보호자 검토·증거 개인정보·공식 도움·신고 준비·후속 점검 순서를 고르려는 때",
                valueFreePauseScript =
                    "민감한 내용을 말하지 않고 이동하지 않은 채 가족이나 보호자와 상의한 뒤 기존 도움·신고 준비 안내를 직접 확인하겠습니다.",
                whyItMatters =
                    "값 없는 다음 문장은 주소·경로·사람·사건·금융값을 반복 공개하지 않으면서 이미 있는 안전 흐름으로 이어 줍니다.",
                doNotTravelNavigateShareLocationMeetHandoffSayShareAttachOrRecordBoundaries =
                    listOf(
                        "원문·가린 단서·사건 내용·안전단어·콜백 문장·연락처·주소·경로·사람·금액·문서·결과 상태를 말하거나 공유하지 마세요.",
                        "증거를 촬영·녹음·스캔·첨부·업로드하지 않고 공식 도움 조회·신고 제출·출동·법률·금융 결과를 만들거나 저장하지 않습니다."
                    ),
                primaryAction = action(
                    TravelRouteMeetingPointPauseActionId.TRUSTED_HELPER_REVIEW
                ),
                secondaryActions = listOf(
                    action(TravelRouteMeetingPointPauseActionId.SAFETY_PLAN),
                    action(
                        TravelRouteMeetingPointPauseActionId.FAMILY_READINESS_ROADMAP
                    ),
                    action(
                        TravelRouteMeetingPointPauseActionId.FAMILY_SAFETY_DRILL_COACH
                    ),
                    action(TravelRouteMeetingPointPauseActionId.FAMILY_CALLBACK_CARD),
                    action(
                        TravelRouteMeetingPointPauseActionId
                            .FAMILY_SAFE_WORD_RESET_COACH
                    ),
                    action(TravelRouteMeetingPointPauseActionId.SAFE_CALL_SCRIPTS),
                    action(TravelRouteMeetingPointPauseActionId.FAMILY_ALERT_MESSAGES),
                    action(
                        TravelRouteMeetingPointPauseActionId.FAMILY_RESPONSE_ROLE_BOARD
                    ),
                    action(
                        TravelRouteMeetingPointPauseActionId.FAMILY_CALM_DEBRIEF_AGENDA
                    ),
                    action(
                        TravelRouteMeetingPointPauseActionId
                            .EVIDENCE_HANDOFF_PRIVACY_COACH
                    ),
                    action(TravelRouteMeetingPointPauseActionId.EVIDENCE_KIT),
                    action(TravelRouteMeetingPointPauseActionId.INCIDENT_TIMELINE_BRIEFING),
                    action(TravelRouteMeetingPointPauseActionId.OFFICIAL_HELP_DIRECTORY),
                    action(TravelRouteMeetingPointPauseActionId.REPORT_PREP),
                    action(TravelRouteMeetingPointPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                    action(TravelRouteMeetingPointPauseActionId.SAFETY_FLOW_ROUTER),
                    action(TravelRouteMeetingPointPauseActionId.PRIVACY_CENTER)
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
                    "검사 결과나 일치 신호가 없어도 경로·목적지·만남·여행·차량·기사·" +
                        "방문자·직원·기관·은행·플랫폼·문서·사건·택배·주소·요청·인계·" +
                        "결제·신고 상태를 공식·검증·안전·유효·접수·해결·완료라고 단정하지 " +
                        "않고 모든 값 없는 이동·만남 멈춤 카드와 기존 대응 경로를 보수적으로 보여 줍니다."
                )
            }
            if (hasHighRisk) {
                add(
                    "높은 위험 범주에서는 이동·만남·위치 공유·돈·물품 전달 멈춤, 연락 " +
                        "종료, 별도 확인, 가족, 계정·기기·노출·증거·공식 도움·후속 경로를 값 없이 넓게 보여 줍니다."
                )
            }
            if (hasFinancialSignal) {
                add(
                    "돈 관련 범주에서는 현금·대면 전달 멈춤, 은행 직원 공개 문구, 문 앞 " +
                        "거절, 송금 멈춤, 연락 종료, 기관 압박, 별도 확인, 계정·노출·증거 " +
                        "개인정보·보호자·공식 도움·신고 준비 화면을 고르며 현금·카드·결제·" +
                        "계정 동작이나 지도·위치·주소·경로·목적지·차량 행동을 하지 않습니다."
                )
            }
            if (hasOfficialPressureSignal) {
                add(
                    "사칭·비밀 유지 범주에서는 기관 압박, 연락 종료, 별도 확인, 가족 " +
                        "알림·차분 대화, 증거 개인정보, 보호자, 공식 도움·신고 준비 화면을 " +
                        "고르며 공식 번호·목적지·직원·방문자·기사 신원·문서·사건을 검증하거나 " +
                        "전화·브라우저·문자·신고 제출을 하지 않습니다."
                )
            }
            if (hasLiveLinkAppCourierSignal) {
                add(
                    "통화 유지·앱·링크·택배 문자 범주에서는 연락 종료, 링크·앱 격리, " +
                        "원격지시 중단, 현금·물품 전달과 문 앞 응대 멈춤, 노출·계정·개인정보 " +
                        "화면을 고르며 통화 상태, 앱·패키지·기기·설정을 읽거나 지도·위치·" +
                        "경로·내비게이션·택배 추적·여행 예약·원격 연결 종료·기기 정리를 하지 않습니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증·상품권·결제 범주에서는 비밀값 멈춤, 상품권 코드 멈춤, 계정 " +
                        "잠금, 안전 붙여넣기 개인정보, 원격지시 중단, 노출 분류 화면을 고르며 " +
                        "OTP·PIN·비밀번호·계좌·카드·은행·지갑·상품권·현금 값을 사용하지 않습니다."
                )
            }
            if (hasRecoveryPromiseSignal) {
                add(
                    "회복 약속 범주에서는 회복 현실 점검, 돈·물품·이동·만남 멈춤, 증거 " +
                        "개인정보, 보호자, 공식 도움·신고 준비 화면을 고르며 환급·차지백·" +
                        "취소·동결·추적·회복 결과를 수행하거나 보장하지 않습니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 긴급·목소리 사칭·번호 변경 범주에서는 별도 확인, 가족 콜백, " +
                        "목소리 현실 점검, 새 번호 격리, 안전단어 재정비, 값 없는 전화 문구, " +
                        "가족 알림 화면을 고르며 연락처·안전단어·콜백 값을 사용하거나 통화·문자를 읽지 않습니다."
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
                "가족 준비가 되어 있다는 범주는 경로·목적지·만남·요청·여행·이동·인계가 " +
                    "안전하거나 신뢰할 수 있거나 공식이라는 뜻이 아닙니다. 준비 상태와 " +
                    "관계없이 이동을 멈추고 값 없는 거절 문장과 별도 확인 경로를 직접 고르세요."
        }
        return listOf(situationCopy, readinessCopy)
            .filter { copy -> copy.isNotBlank() }
            .joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 고정 멈춤 문장을 사용자가 직접 읽고 기존 앱 화면을 직접 고르는 로컬 전용·무상태 수동 안내이며 민감한 값을 입력하라고 요구하지 않고 새 저장소·자동 처리·자동 전송·스케줄러를 만들지 않습니다.",
        "추천은 현재 검사 결과의 위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기·가족 안전 계획의 실제 값을 사용하지 않습니다.",
        "현재 위치·집·직장·건물·로비·호수·엘리베이터·주차장·인터폰·문·보관함·상점·역·주소·경로·목적지·랜드마크·만남·차량·교통수단·운임·출발·도착·예상 시간 값을 입력·표시·저장하지 않습니다.",
        "지도·위치·경로·길 안내·내비게이션·지오코딩·주소 검증을 사용하지 않고 현재 위치를 감지하거나 집·직장·건물·로비·주차장·보관함·상점·역·목적지·만남 장소를 찾거나 기록·검증·추천하지 않습니다.",
        "경로 계획·교통·대중교통·택시·배차·승차 호출 기능이나 API를 사용하지 않고 걷기·운전·차량·택시·버스·지하철·승차·운임·출발·도착·예상 시간·이동 상태를 조회·계산·안내하지 않습니다.",
        "택배·퀵·우편·배송·보관함·기사·물품을 조회·예약·접수·추적·발송·배차·수거·보관·인계하지 않고 만남·방문·여행·이동·경로·캘린더 일정·약속을 만들거나 예약하지 않습니다.",
        "네트워크·웹 탐색·브라우저·웹뷰를 사용하지 않고 기관 정보·공식 번호·디렉터리·발신자 표시·전화번호·사람·신원·주소·목적지·문서·사건·신고·접수·영수증·법률·금융 상태를 조회·검증하거나 신고·출동을 요청·제출하지 않습니다.",
        "전화·다이얼·발신·통화·종료·선별·차단·통화 상태·통화 기록·문자·SMS·받는 사람 선택·자동 전송을 사용하지 않고 연락처·주소록·알림을 읽기·쓰기·편집·선택·감시·필터링하지 않습니다.",
        "QR·바코드·OCR·카메라·이미지 분석·화면 캡처를 사용하지 않고 파일·문서·사진·스크린샷·오디오·미디어·저장소를 읽거나 선택·촬영·녹음·전사·첨부·스트림·URI 공유·업로드·동기화하지 않습니다.",
        "클립보드·텍스트 감시기·자동완성·자격 증명·계정·로그인·인증·생체 인증·키가드·키 저장소·암호화 API를 사용하지 않고 계정·인증 상태를 읽거나 바꾸지 않습니다.",
        "OTP·PIN·비밀번호·보안 답·주민 식별값·안전단어·콜백 문장·연락처 이름·번호·주소·계좌·카드·은행·지갑·거래·금액·현금 일련값·코드 값을 읽기·표시·저장·복사·붙여넣기·자동 입력·공유·제출하지 않습니다.",
        "현금 인출·입금·송금·결제·계좌·카드·통장·은행·ATM·NFC·지갑·블록체인·가상자산·상품권 기능 또는 API를 사용하지 않고 구매·잔액·교환·취소·동결·차단·환급·차지백·추적·회복을 수행·조회·제출·보장하지 않습니다.",
        "앱·패키지·설치 앱 목록·앱 이름·권한·기기 상태를 검사하거나 읽지 않고 설치·제거·비활성화·강제 종료·권한 허용·철회·설정 변경·기기 정리·원격 연결 종료를 수행하지 않습니다.",
        "접근성 서비스·기기 관리자·VPN·사용 통계·알림 수신 서비스·권한 관리자·패키지 설치 도구·기기 관리 기능을 사용하지 않습니다.",
        "경로·목적지·만남·차량·기사·방문자·직원·도우미·수사관·기관·은행·지점·주소·집·문·로비·보관함·택배·현금·문서·사건·신고·계좌·카드·기기·결제·출입·인계·환급·회복 상태가 공식·실제·안전·보안·검증·유효·접수·제출·수신·설치·제거·비활성화·정리·차단·보호·적법·신뢰·완료·해결·취소·역전·회복·환급·동결·추적·배달·수거·입금·예약·도착·출발·탑승·보장·문제없음 상태라고 판정하거나 증명하지 않습니다.",
        "수동 문자 공유는 고정 카드 제목과 기존 앱 경로·행동 종류만 담고 원문·가린 단서·계획·사건·기관 목록·연락처·채널·프로필·계정·결제·지갑·링크·앱·주소·위치·경로·목적지·차량·직원·방문자·기사·현금·기기·받는 사람·확인·제출·이동·만남·인계·회복 결과를 담거나 파일·스트림·URI를 붙이거나 자동 발송하지 않습니다.",
        "새 Android 권한을 요청하지 않고 알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러·브로드캐스트 수신기를 만들거나 예약하지 않습니다.",
        "가족 계획·사건·공식 디렉터리·연락처·알림·통화·문자·위치·지도·경로·교통·택시·배차·택배·보관함·만남·방문·문·계정·결제·앱·권한·기기·자격·신고·확인·결과 상태를 수정하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<TravelRouteMeetingPointPauseCardId>,
        recommendedActions: List<TravelRouteMeetingPointPauseAction>,
        cards: List<TravelRouteMeetingPointPauseCard>
    ): String {
        val cardById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[이동 경로·만남 장소 멈춤 코치]")
            appendLine("민감한 값을 말하지 않고 이동과 만남을 멈춘 채 직접 읽을 문장을 고르는 로컬 수동 안내입니다.")
            appendLine()
            appendLine("추천 값 없는 이동·만남 멈춤 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 실제 현재 위치·주소·경로·목적지·교통수단·도착 시간·사람·연락처·금액·계정·기기 값은 보내지 않습니다.")
            append("- 이 안내는 이동·길 안내·위치 조회·만남·인계·추적·예약·검증·신고·금융·기기 행동을 수행하거나 보장하지 않습니다.")
        }
    }

    private fun actionFor(
        id: TravelRouteMeetingPointPauseActionId
    ): TravelRouteMeetingPointPauseAction = TravelRouteMeetingPointPauseAction(
        id = id,
        label = when (id) {
            TravelRouteMeetingPointPauseActionId.QUICK_STOP -> "긴급 멈춤 모드"
            TravelRouteMeetingPointPauseActionId.CHECKER -> "의심 문구 검사"
            TravelRouteMeetingPointPauseActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            TravelRouteMeetingPointPauseActionId.SAFETY_PLAN -> "가족 안전 계획"
            TravelRouteMeetingPointPauseActionId.FAMILY_READINESS_ROADMAP ->
                "가족 준비 로드맵"
            TravelRouteMeetingPointPauseActionId.FAMILY_SAFETY_DRILL_COACH ->
                "가족 안전 연습 코치"
            TravelRouteMeetingPointPauseActionId.SAFE_PASTE_PRIVACY_COACH ->
                "안전 붙여넣기 개인정보 코치"
            TravelRouteMeetingPointPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                "현재 연락을 끝내는 문장"
            TravelRouteMeetingPointPauseActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 카드"
            TravelRouteMeetingPointPauseActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                "가족 안전단어 재정비 코치"
            TravelRouteMeetingPointPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                "별도 경로 확인 코치"
            TravelRouteMeetingPointPauseActionId.SAFE_CALL_SCRIPTS ->
                "값 없는 안전 확인 전화 문구"
            TravelRouteMeetingPointPauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                "목소리 사칭 현실 점검"
            TravelRouteMeetingPointPauseActionId.NEW_NUMBER_QUARANTINE_COACH ->
                "새 연락수단 격리 코치"
            TravelRouteMeetingPointPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                "자격 비밀값 멈춤 코치"
            TravelRouteMeetingPointPauseActionId.FAMILY_ALERT_MESSAGES -> "가족 알림 문구"
            TravelRouteMeetingPointPauseActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                "확인 후속 행동 보드"
            TravelRouteMeetingPointPauseActionId.RECONTACT_BOUNDARY_COACH ->
                "재접촉 경계 코치"
            TravelRouteMeetingPointPauseActionId.TRUSTED_HELPER_REVIEW -> "보호자 검토 모드"
            TravelRouteMeetingPointPauseActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                "가족 대응 역할 보드"
            TravelRouteMeetingPointPauseActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                "가족 차분 대화 안건"
            TravelRouteMeetingPointPauseActionId
                .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH ->
                "현금 인출·택배/퀵 대면 전달 멈춤 코치"
            TravelRouteMeetingPointPauseActionId.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH ->
                "은행·ATM 직원 공개 문구 코치"
            TravelRouteMeetingPointPauseActionId.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH ->
                "문 앞 방문자·자택 인계 거절 코치"
            TravelRouteMeetingPointPauseActionId.MONEY_TRANSFER_PAUSE_GATE ->
                "송금 전 멈춤 확인"
            TravelRouteMeetingPointPauseActionId.GIFT_CARD_CODE_PAUSE_COACH ->
                "상품권·코드 멈춤 코치"
            TravelRouteMeetingPointPauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH ->
                "링크·앱 설치 격리 코치"
            TravelRouteMeetingPointPauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                "기관 사칭·압박 현실 점검"
            TravelRouteMeetingPointPauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH ->
                "회복 약속 현실 점검"
            TravelRouteMeetingPointPauseActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크"
            TravelRouteMeetingPointPauseActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                "원격 접근 중단 코치"
            TravelRouteMeetingPointPauseActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
            TravelRouteMeetingPointPauseActionId.PRIVACY_CENTER -> "개인정보 센터"
            TravelRouteMeetingPointPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                "증거 전달 개인정보 코치"
            TravelRouteMeetingPointPauseActionId.EVIDENCE_KIT -> "증거 보존 체크"
            TravelRouteMeetingPointPauseActionId.INCIDENT_TIMELINE_BRIEFING ->
                "사건 흐름 요약"
            TravelRouteMeetingPointPauseActionId.OFFICIAL_HELP_DIRECTORY ->
                "공식 도움 연락처"
            TravelRouteMeetingPointPauseActionId.REPORT_PREP -> "공식 신고 준비"
            TravelRouteMeetingPointPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                "후속 점검 계획"
            TravelRouteMeetingPointPauseActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
