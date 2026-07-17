package com.mongmong.familyshield

enum class SimNumberPortOutPauseCardId(val displayOrder: Int) {
    STOP_CARRIER_SIM_PORT_CODE_SHARING(1),
    REFUSE_SIM_REISSUE_PORT_OR_NAME_TRANSFER(2),
    SEPARATE_NUMBER_DEVICE_TITLE_AND_PROTECTION_PROMISES(3),
    LEAVE_LINK_APP_AND_REMOTE_TELECOM_ACCOUNT_FLOW(4),
    CHOOSE_FAMILY_VERIFICATION_ACCOUNT_AND_EXPOSURE_ROUTES(5),
    CHOOSE_HELP_REPORT_AND_FOLLOW_UP_ROUTES(6)
}

enum class SimNumberPortOutPauseActionId {
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
    TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH,
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

data class SimNumberPortOutPauseAction(
    val id: SimNumberPortOutPauseActionId,
    val label: String
)

data class SimNumberPortOutPauseCard(
    val id: SimNumberPortOutPauseCardId,
    val displayOrder: Int,
    val title: String,
    val simNumberPortPressureSituation: String,
    val valueFreePauseScript: String,
    val whyItMatters: String,
    val doNotShareCodePortReissueCallSmsContactAccountDeviceSayShareAttachOrRecordBoundaries:
        List<String>,
    val primaryAction: SimNumberPortOutPauseAction,
    val secondaryActions: List<SimNumberPortOutPauseAction>
) {
    val routeActions: List<SimNumberPortOutPauseAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class SimNumberPortOutPauseCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<SimNumberPortOutPauseCardId>,
    val recommendedActions: List<SimNumberPortOutPauseAction>,
    val cards: List<SimNumberPortOutPauseCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<SimNumberPortOutPauseCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<SimNumberPortOutPauseActionId>
        get() = recommendedActions.map { action -> action.id }
}

object SimNumberPortOutPauseCoachBuilder {
    private val credentialSignals = setOf(
        ScamSignal.CREDENTIALS,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val familyVerificationSignals = setOf(
        ScamSignal.PHONE_NUMBER_CHANGE,
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE
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

    private val financialRecoverySignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.BAIT,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.OVERSEAS_PAYMENT_ALERT,
        ScamSignal.GIFTICON_PIN
    )

    fun build(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness,
        incidentCount: Int = 0
    ): SimNumberPortOutPauseCoach {
        val riskLevel = analysis?.riskLevel
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val readinessLevel = readiness.level
        val hasNoAnalysisOrSignals = analysis == null || signals.isEmpty()
        val hasHighRisk = riskLevel == RiskLevel.HIGH
        val hasCredentialSignal = signals.any { signal -> signal in credentialSignals }
        val hasFamilyVerificationSignal = signals.any { signal ->
            signal in familyVerificationSignals
        }
        val hasOfficialPressureSignal = signals.any { signal ->
            signal in officialPressureSignals
        }
        val hasLiveLinkAppSignal = signals.any { signal -> signal in liveLinkAppSignals }
        val hasFinancialRecoverySignal = signals.any { signal ->
            signal in financialRecoverySignals
        }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0

        val recommendedCardSet = linkedSetOf(
            SimNumberPortOutPauseCardId.STOP_CARRIER_SIM_PORT_CODE_SHARING,
            SimNumberPortOutPauseCardId.REFUSE_SIM_REISSUE_PORT_OR_NAME_TRANSFER,
            SimNumberPortOutPauseCardId.LEAVE_LINK_APP_AND_REMOTE_TELECOM_ACCOUNT_FLOW
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += SimNumberPortOutPauseCardId.entries
        }
        if (hasCredentialSignal) {
            recommendedCardSet += SimNumberPortOutPauseCardId.STOP_CARRIER_SIM_PORT_CODE_SHARING
            recommendedCardSet += SimNumberPortOutPauseCardId.REFUSE_SIM_REISSUE_PORT_OR_NAME_TRANSFER
            recommendedCardSet +=
                SimNumberPortOutPauseCardId.LEAVE_LINK_APP_AND_REMOTE_TELECOM_ACCOUNT_FLOW
            recommendedCardSet +=
                SimNumberPortOutPauseCardId.CHOOSE_FAMILY_VERIFICATION_ACCOUNT_AND_EXPOSURE_ROUTES
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet +=
                SimNumberPortOutPauseCardId.SEPARATE_NUMBER_DEVICE_TITLE_AND_PROTECTION_PROMISES
            recommendedCardSet +=
                SimNumberPortOutPauseCardId.CHOOSE_FAMILY_VERIFICATION_ACCOUNT_AND_EXPOSURE_ROUTES
        }
        if (hasOfficialPressureSignal) {
            recommendedCardSet += SimNumberPortOutPauseCardId.REFUSE_SIM_REISSUE_PORT_OR_NAME_TRANSFER
            recommendedCardSet +=
                SimNumberPortOutPauseCardId.SEPARATE_NUMBER_DEVICE_TITLE_AND_PROTECTION_PROMISES
            recommendedCardSet +=
                SimNumberPortOutPauseCardId.CHOOSE_FAMILY_VERIFICATION_ACCOUNT_AND_EXPOSURE_ROUTES
            recommendedCardSet +=
                SimNumberPortOutPauseCardId.CHOOSE_HELP_REPORT_AND_FOLLOW_UP_ROUTES
        }
        if (hasLiveLinkAppSignal) {
            recommendedCardSet += SimNumberPortOutPauseCardId.STOP_CARRIER_SIM_PORT_CODE_SHARING
            recommendedCardSet +=
                SimNumberPortOutPauseCardId.LEAVE_LINK_APP_AND_REMOTE_TELECOM_ACCOUNT_FLOW
            recommendedCardSet +=
                SimNumberPortOutPauseCardId.CHOOSE_FAMILY_VERIFICATION_ACCOUNT_AND_EXPOSURE_ROUTES
        }
        if (hasFinancialRecoverySignal) {
            recommendedCardSet += SimNumberPortOutPauseCardId.STOP_CARRIER_SIM_PORT_CODE_SHARING
            recommendedCardSet += SimNumberPortOutPauseCardId.REFUSE_SIM_REISSUE_PORT_OR_NAME_TRANSFER
            recommendedCardSet +=
                SimNumberPortOutPauseCardId.SEPARATE_NUMBER_DEVICE_TITLE_AND_PROTECTION_PROMISES
            recommendedCardSet +=
                SimNumberPortOutPauseCardId.CHOOSE_FAMILY_VERIFICATION_ACCOUNT_AND_EXPOSURE_ROUTES
            recommendedCardSet +=
                SimNumberPortOutPauseCardId.CHOOSE_HELP_REPORT_AND_FOLLOW_UP_ROUTES
        }
        if (hasSavedIncident) {
            recommendedCardSet +=
                SimNumberPortOutPauseCardId.CHOOSE_HELP_REPORT_AND_FOLLOW_UP_ROUTES
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedCardSet +=
                    SimNumberPortOutPauseCardId.CHOOSE_FAMILY_VERIFICATION_ACCOUNT_AND_EXPOSURE_ROUTES
                recommendedCardSet +=
                    SimNumberPortOutPauseCardId.CHOOSE_HELP_REPORT_AND_FOLLOW_UP_ROUTES
            }
            ReadinessLevel.READY -> {
                recommendedCardSet +=
                    SimNumberPortOutPauseCardId.STOP_CARRIER_SIM_PORT_CODE_SHARING
                recommendedCardSet +=
                    SimNumberPortOutPauseCardId.SEPARATE_NUMBER_DEVICE_TITLE_AND_PROTECTION_PROMISES
            }
        }
        val recommendedCardIds = SimNumberPortOutPauseCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            SimNumberPortOutPauseActionId.CHECKER
        } else {
            SimNumberPortOutPauseActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            currentResultActionId,
            SimNumberPortOutPauseActionId.QUICK_STOP,
            SimNumberPortOutPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH,
            SimNumberPortOutPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
            SimNumberPortOutPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
            SimNumberPortOutPauseActionId.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH,
            SimNumberPortOutPauseActionId.ACCOUNT_LOCKDOWN,
            SimNumberPortOutPauseActionId.EXPOSURE_IMPACT_TRIAGE,
            SimNumberPortOutPauseActionId.PRIVACY_CENTER,
            SimNumberPortOutPauseActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            SimNumberPortOutPauseActionId.entries
                .filterNot { actionId ->
                    actionId == SimNumberPortOutPauseActionId.ANALYSIS_RESULT
                }
                .forEach { actionId -> recommendedActionSet += actionId }
        }
        if (hasHighRisk) {
            SimNumberPortOutPauseActionId.entries.forEach { actionId ->
                recommendedActionSet += actionId
            }
        }
        if (hasCredentialSignal) {
            recommendedActionSet +=
                SimNumberPortOutPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet += SimNumberPortOutPauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += SimNumberPortOutPauseActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet += SimNumberPortOutPauseActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += SimNumberPortOutPauseActionId.EXPOSURE_IMPACT_TRIAGE
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += SimNumberPortOutPauseActionId.NEW_NUMBER_QUARANTINE_COACH
            recommendedActionSet +=
                SimNumberPortOutPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += SimNumberPortOutPauseActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet +=
                SimNumberPortOutPauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH
            recommendedActionSet +=
                SimNumberPortOutPauseActionId.FAMILY_SAFE_WORD_RESET_COACH
            recommendedActionSet += SimNumberPortOutPauseActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += SimNumberPortOutPauseActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasOfficialPressureSignal) {
            recommendedActionSet +=
                SimNumberPortOutPauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet += SimNumberPortOutPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                SimNumberPortOutPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += SimNumberPortOutPauseActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += SimNumberPortOutPauseActionId.FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet +=
                SimNumberPortOutPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += SimNumberPortOutPauseActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += SimNumberPortOutPauseActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += SimNumberPortOutPauseActionId.REPORT_PREP
        }
        if (hasLiveLinkAppSignal) {
            recommendedActionSet += SimNumberPortOutPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                SimNumberPortOutPauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH
            recommendedActionSet += SimNumberPortOutPauseActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += SimNumberPortOutPauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += SimNumberPortOutPauseActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += SimNumberPortOutPauseActionId.PRIVACY_CENTER
        }
        if (hasFinancialRecoverySignal) {
            recommendedActionSet += SimNumberPortOutPauseActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += SimNumberPortOutPauseActionId.GIFT_CARD_CODE_PAUSE_COACH
            recommendedActionSet +=
                SimNumberPortOutPauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH
            recommendedActionSet +=
                SimNumberPortOutPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet += SimNumberPortOutPauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                SimNumberPortOutPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += SimNumberPortOutPauseActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += SimNumberPortOutPauseActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += SimNumberPortOutPauseActionId.REPORT_PREP
        }
        if (hasSavedIncident) {
            recommendedActionSet +=
                SimNumberPortOutPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += SimNumberPortOutPauseActionId.EVIDENCE_KIT
            recommendedActionSet += SimNumberPortOutPauseActionId.INCIDENT_TIMELINE_BRIEFING
            recommendedActionSet += SimNumberPortOutPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedActionSet += SimNumberPortOutPauseActionId.SAFETY_PLAN
                recommendedActionSet +=
                    SimNumberPortOutPauseActionId.FAMILY_SAFE_WORD_RESET_COACH
                recommendedActionSet +=
                    SimNumberPortOutPauseActionId.FAMILY_READINESS_ROADMAP
                recommendedActionSet +=
                    SimNumberPortOutPauseActionId.FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += SimNumberPortOutPauseActionId.SAFE_CALL_SCRIPTS
            }
            ReadinessLevel.READY -> {
                recommendedActionSet += SimNumberPortOutPauseActionId.FAMILY_CALLBACK_CARD
                recommendedActionSet +=
                    SimNumberPortOutPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                recommendedActionSet += SimNumberPortOutPauseActionId.SAFE_CALL_SCRIPTS
                recommendedActionSet +=
                    SimNumberPortOutPauseActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
            }
        }
        val recommendedActions = SimNumberPortOutPauseActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null ->
                "검사 결과가 없어도 모든 SIM·번호이동 멈춤 문구를 보수적으로 읽기"
            hasCredentialSignal -> "통신·계정 인증 비밀값을 말하거나 입력하기 전에 멈추기"
            hasFamilyVerificationSignal ->
                "새 번호·분실 기기 주장과 번호이동 요청을 기존 가족 경로에서 분리하기"
            hasOfficialPressureSignal ->
                "통신사·기관 직함과 보호·복구 약속을 요청의 근거에서 분리하기"
            hasLiveLinkAppSignal ->
                "통화·링크·앱·원격지시 흐름에서 통신·계정 정보 입력을 멈추기"
            hasFinancialRecoverySignal ->
                "돈·회복 약속과 번호이동·재발급·계정 지시를 함께 멈추기"
            hasHighRisk -> "높은 위험 범주에서 모든 멈춤 문구와 기존 대응 경로 보기"
            hasSavedIncident -> "사건 내용 없이 증거·도움·후속 경로만 고르기"
            else -> "번호이동·재발급·통신 계정 요청에 답하기 전 짧게 멈추기"
        }

        return SimNumberPortOutPauseCoach(
            title = "SIM·번호이동 멈춤 코치",
            intro =
                "누군가 번호·SIM·eSIM·USIM·통신 계정·인증코드·기기 식별 정보를 지금 " +
                    "말하거나 입력하고 번호이동·재발급·명의변경·보호·복구·해제·취소를 " +
                    "진행하라고 재촉할 때 실제 값을 받지 않고 고정 멈춤 문장과 기존 대응 " +
                    "화면만 직접 고르는 로컬 전용·무상태 수동 안내입니다.",
            modeLabel = "값 입력 없음 · 통신/SIM 조회 없음 · 번호이동/재발급 행동 없음",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                readinessLevel = readinessLevel,
                hasNoAnalysisOrSignals = hasNoAnalysisOrSignals,
                hasHighRisk = hasHighRisk,
                hasCredentialSignal = hasCredentialSignal,
                hasFamilyVerificationSignal = hasFamilyVerificationSignal,
                hasOfficialPressureSignal = hasOfficialPressureSignal,
                hasLiveLinkAppSignal = hasLiveLinkAppSignal,
                hasFinancialRecoverySignal = hasFinancialRecoverySignal,
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

    private fun buildCards(): List<SimNumberPortOutPauseCard> {
        fun action(id: SimNumberPortOutPauseActionId): SimNumberPortOutPauseAction =
            actionFor(id)

        return listOf(
            SimNumberPortOutPauseCard(
                id = SimNumberPortOutPauseCardId.STOP_CARRIER_SIM_PORT_CODE_SHARING,
                displayOrder =
                    SimNumberPortOutPauseCardId.STOP_CARRIER_SIM_PORT_CODE_SHARING.displayOrder,
                title = "통신사·유심·번호이동 인증코드 말하기 멈추기",
                simNumberPortPressureSituation =
                    "상대가 번호를 보호·복구·이전·해제하려면 지금 통신 인증값이나 계정 비밀값을 읽어 달라고 재촉하는 때",
                valueFreePauseScript =
                    "통신·계정 인증값을 말하거나 입력하지 않겠습니다. 이 요청을 여기서 멈추겠습니다.",
                whyItMatters =
                    "값 없는 짧은 거절은 번호이동·재발급·계정 변경에 쓰일 수 있는 비밀을 다시 드러내지 않고 상대가 정한 속도를 끊습니다.",
                doNotShareCodePortReissueCallSmsContactAccountDeviceSayShareAttachOrRecordBoundaries =
                    listOf(
                        "실제 인증코드·OTP·PIN·비밀번호·보안 답·번호이동용 비밀값·통신 계정·고객 식별값을 말하거나 적지 마세요.",
                        "코드를 읽기·복사·붙여넣기·자동 입력·전달·재사용하지 않고 통화·문자·알림·클립보드·계정 상태를 읽지 않습니다."
                    ),
                primaryAction = action(
                    SimNumberPortOutPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH
                ),
                secondaryActions = listOf(
                    action(SimNumberPortOutPauseActionId.QUICK_STOP),
                    action(SimNumberPortOutPauseActionId.SAFE_PASTE_PRIVACY_COACH),
                    action(SimNumberPortOutPauseActionId.ACCOUNT_LOCKDOWN),
                    action(SimNumberPortOutPauseActionId.EXPOSURE_IMPACT_TRIAGE)
                )
            ),
            SimNumberPortOutPauseCard(
                id = SimNumberPortOutPauseCardId.REFUSE_SIM_REISSUE_PORT_OR_NAME_TRANSFER,
                displayOrder =
                    SimNumberPortOutPauseCardId.REFUSE_SIM_REISSUE_PORT_OR_NAME_TRANSFER.displayOrder,
                title = "SIM/eSIM/USIM 재발급·번호이동·명의변경 지시를 값 없이 거절하기",
                simNumberPortPressureSituation =
                    "상대가 번호를 옮기거나 유심을 다시 내고 명의를 바꾸거나 잠금·해제·취소 절차를 즉시 따라 하라고 하는 때",
                valueFreePauseScript =
                    "번호이동·재발급·명의변경 지시를 따르지 않겠습니다. 통신·계정 행동을 직접 시작하지 않겠습니다.",
                whyItMatters =
                    "절차 이름이나 긴급성은 상대가 제시한 이동·재발급·변경 요청이 공식이거나 필요한지 증명하지 않습니다.",
                doNotShareCodePortReissueCallSmsContactAccountDeviceSayShareAttachOrRecordBoundaries =
                    listOf(
                        "전화번호·새 번호·이전 번호·SIM/eSIM/USIM·가입·명의·통신사·매장·계정·고객 값을 말하거나 공유하지 마세요.",
                        "번호이동·개통·해지·재발급·교체·활성화·비활성화·차단·해제를 조회하거나 신청·변경·취소하지 않습니다."
                    ),
                primaryAction = action(
                    SimNumberPortOutPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                ),
                secondaryActions = listOf(
                    action(SimNumberPortOutPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(SimNumberPortOutPauseActionId.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH),
                    action(SimNumberPortOutPauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH),
                    action(SimNumberPortOutPauseActionId.RECONTACT_BOUNDARY_COACH)
                )
            ),
            SimNumberPortOutPauseCard(
                id =
                    SimNumberPortOutPauseCardId
                        .SEPARATE_NUMBER_DEVICE_TITLE_AND_PROTECTION_PROMISES,
                displayOrder =
                    SimNumberPortOutPauseCardId
                        .SEPARATE_NUMBER_DEVICE_TITLE_AND_PROTECTION_PROMISES.displayOrder,
                title = "새 번호·분실폰·통신사 직함·보호/복구 약속을 확인 근거에서 분리하기",
                simNumberPortPressureSituation =
                    "새 번호나 분실 기기를 내세우거나 통신사·기관 직함과 보호·복구 약속으로 번호·계정 행동을 재촉하는 때",
                valueFreePauseScript =
                    "새 번호·기기 주장이나 직함·약속을 확인 근거로 쓰지 않겠습니다. 기존 가족 경로에서만 다음 순서를 고르겠습니다.",
                whyItMatters =
                    "번호·기기 이야기와 직함·약속을 분리하면 상대가 제공한 정보만으로 신원·통신사·요청 상태를 확정하지 않게 됩니다.",
                doNotShareCodePortReissueCallSmsContactAccountDeviceSayShareAttachOrRecordBoundaries =
                    listOf(
                        "전화번호·연락처·채널·프로필·통신사·매장·직원·기기 이름·일련값·IMEI·ICCID·IMSI 같은 식별값을 말하지 마세요.",
                        "신원·직함·발신자·전화번호·통신사·매장·문서·사건·보호·복구 상태를 조회·검증하거나 전화·문자·웹 탐색을 하지 않습니다."
                    ),
                primaryAction = action(
                    SimNumberPortOutPauseActionId.NEW_NUMBER_QUARANTINE_COACH
                ),
                secondaryActions = listOf(
                    action(SimNumberPortOutPauseActionId.FAMILY_CALLBACK_CARD),
                    action(
                        SimNumberPortOutPauseActionId
                            .VOICE_IMPERSONATION_REALITY_CHECK_COACH
                    ),
                    action(SimNumberPortOutPauseActionId.FAMILY_SAFE_WORD_RESET_COACH),
                    action(SimNumberPortOutPauseActionId.SAFE_CALL_SCRIPTS),
                    action(SimNumberPortOutPauseActionId.FAMILY_ALERT_MESSAGES)
                )
            ),
            SimNumberPortOutPauseCard(
                id = SimNumberPortOutPauseCardId.LEAVE_LINK_APP_AND_REMOTE_TELECOM_ACCOUNT_FLOW,
                displayOrder =
                    SimNumberPortOutPauseCardId
                        .LEAVE_LINK_APP_AND_REMOTE_TELECOM_ACCOUNT_FLOW.displayOrder,
                title = "링크·앱·원격지시로 통신/계정 정보를 입력하지 않기",
                simNumberPortPressureSituation =
                    "상대가 통화를 유지한 채 링크·앱·화면공유·원격지시 안에서 통신·계정·기기 정보를 입력하라고 하는 때",
                valueFreePauseScript =
                    "현재 연락과 화면 흐름을 벗어나고 링크·앱·원격지시 안에서는 통신·계정 정보를 입력하지 않겠습니다.",
                whyItMatters =
                    "상대가 만든 흐름 밖으로 나오면 입력 위치와 다음 행동까지 계속 통제되는 일을 줄일 수 있습니다.",
                doNotShareCodePortReissueCallSmsContactAccountDeviceSayShareAttachOrRecordBoundaries =
                    listOf(
                        "링크·도메인·앱·패키지·화면·계정·로그인·기기·전화·문자·알림 값을 말하거나 공유하지 마세요.",
                        "네트워크·브라우저·앱 목록·패키지·기기·설정·권한을 읽거나 링크 열기·앱 설치/제거·설정 변경·원격 종료·기기 정리를 하지 않습니다."
                    ),
                primaryAction = action(
                    SimNumberPortOutPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
                ),
                secondaryActions = listOf(
                    action(SimNumberPortOutPauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH),
                    action(SimNumberPortOutPauseActionId.REMOTE_ACCESS_CUTOFF_COACH),
                    action(SimNumberPortOutPauseActionId.ACCOUNT_LOCKDOWN),
                    action(SimNumberPortOutPauseActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(SimNumberPortOutPauseActionId.PRIVACY_CENTER)
                )
            ),
            SimNumberPortOutPauseCard(
                id =
                    SimNumberPortOutPauseCardId
                        .CHOOSE_FAMILY_VERIFICATION_ACCOUNT_AND_EXPOSURE_ROUTES,
                displayOrder =
                    SimNumberPortOutPauseCardId
                        .CHOOSE_FAMILY_VERIFICATION_ACCOUNT_AND_EXPOSURE_ROUTES.displayOrder,
                title = "가족 기존 경로·별도 확인·계정 보호·노출 분류로 이동하기",
                simNumberPortPressureSituation =
                    "요청을 멈춘 뒤 가족 확인·비밀값 멈춤·계정 보호 안내·원격지시 중단·노출 분류 중 다음 화면을 고르려는 때",
                valueFreePauseScript =
                    "민감한 값을 다시 말하지 않고 가족의 기존 확인 경로와 값 없는 계정·노출 안내를 직접 고르겠습니다.",
                whyItMatters =
                    "기존 화면으로만 이동하면 전화번호·코드·계정·기기 값을 새로 수집하지 않고 필요한 대응 종류를 나눌 수 있습니다.",
                doNotShareCodePortReissueCallSmsContactAccountDeviceSayShareAttachOrRecordBoundaries =
                    listOf(
                        "안전단어·콜백 문장·연락처 이름·번호·통신 계정·코드·기기·사건 내용을 말하거나 공유하지 마세요.",
                        "연락처·통화·문자·알림·계정·로그인·통신·SIM·기기 상태를 읽거나 가족·계정·기기·확인 상태를 바꾸지 않습니다."
                    ),
                primaryAction = action(
                    SimNumberPortOutPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                ),
                secondaryActions = listOf(
                    action(SimNumberPortOutPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH),
                    action(SimNumberPortOutPauseActionId.ACCOUNT_LOCKDOWN),
                    action(SimNumberPortOutPauseActionId.REMOTE_ACCESS_CUTOFF_COACH),
                    action(SimNumberPortOutPauseActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(
                        SimNumberPortOutPauseActionId
                            .VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    ),
                    action(SimNumberPortOutPauseActionId.TRUSTED_HELPER_REVIEW)
                )
            ),
            SimNumberPortOutPauseCard(
                id = SimNumberPortOutPauseCardId.CHOOSE_HELP_REPORT_AND_FOLLOW_UP_ROUTES,
                displayOrder =
                    SimNumberPortOutPauseCardId
                        .CHOOSE_HELP_REPORT_AND_FOLLOW_UP_ROUTES.displayOrder,
                title = "공식 도움·신고 준비·후속 점검 경로 고르기",
                simNumberPortPressureSituation =
                    "통신·계정 요청을 멈춘 뒤 증거 개인정보·보호자·공식 도움·신고 준비·가족 역할·후속 점검 순서를 고르려는 때",
                valueFreePauseScript =
                    "값이나 결과 상태를 말하지 않고 기존 도움·신고 준비·가족·후속 점검 화면을 직접 고르겠습니다.",
                whyItMatters =
                    "고정 경로 이름만 고르면 사건·기관·사람·계정·통신 값을 다시 공개하지 않고 다음 수동 단계를 살펴볼 수 있습니다.",
                doNotShareCodePortReissueCallSmsContactAccountDeviceSayShareAttachOrRecordBoundaries =
                    listOf(
                        "원문·가린 단서·사건 내용·기관·직원·도우미·문서·접수·결과·통신·계정·기기 값을 말하거나 공유하지 마세요.",
                        "증거를 촬영·녹음·스캔·첨부·업로드하지 않고 공식/통신사 조회·전화·문자·신고 제출·법률·금융 결과를 만들지 않습니다."
                    ),
                primaryAction = action(
                    SimNumberPortOutPauseActionId.TRUSTED_HELPER_REVIEW
                ),
                secondaryActions = listOf(
                    action(SimNumberPortOutPauseActionId.FAMILY_RESPONSE_ROLE_BOARD),
                    action(SimNumberPortOutPauseActionId.FAMILY_CALM_DEBRIEF_AGENDA),
                    action(
                        SimNumberPortOutPauseActionId
                            .EVIDENCE_HANDOFF_PRIVACY_COACH
                    ),
                    action(SimNumberPortOutPauseActionId.EVIDENCE_KIT),
                    action(SimNumberPortOutPauseActionId.INCIDENT_TIMELINE_BRIEFING),
                    action(SimNumberPortOutPauseActionId.OFFICIAL_HELP_DIRECTORY),
                    action(SimNumberPortOutPauseActionId.REPORT_PREP),
                    action(SimNumberPortOutPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                    action(SimNumberPortOutPauseActionId.SAFETY_FLOW_ROUTER)
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        readinessLevel: ReadinessLevel,
        hasNoAnalysisOrSignals: Boolean,
        hasHighRisk: Boolean,
        hasCredentialSignal: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasOfficialPressureSignal: Boolean,
        hasLiveLinkAppSignal: Boolean,
        hasFinancialRecoverySignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (hasNoAnalysisOrSignals) {
                add(
                    "검사 결과나 일치 신호가 없어도 전화번호·SIM/eSIM/USIM·통신사·매장·" +
                        "앱·링크·기기·계정·자격·코드·연락처·발신자·문자·사건·신고·결제·" +
                        "환급·회복·차단·동결·번호이동·재발급·개통·해지·교체·변경·보호 " +
                        "상태를 공식·검증·안전·유효·접수·완료라고 단정하지 않고 모든 값 없는 " +
                        "멈춤 카드와 기존 대응 경로를 보수적으로 보여 줍니다."
                )
            }
            if (hasHighRisk) {
                add(
                    "높은 위험 범주에서는 코드·통신·계정·기기 정보 공유와 번호이동·재발급 " +
                        "지시를 멈추고 가족·계정·노출·증거·도움·후속 경로를 값 없이 넓게 보여 줍니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증·상품권·결제 범주에서는 자격 비밀값 멈춤, 계정 보호, 안전 붙여넣기 " +
                        "개인정보, 원격지시 중단, 노출 분류 화면을 고르며 OTP·PIN·비밀번호·" +
                        "번호이동용 비밀값·통신 계정·카드·은행·지갑·상품권 값을 사용하지 않습니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "번호 변경·가족 긴급·목소리 사칭 범주에서는 새 연락 격리, 별도 확인, 가족 " +
                        "콜백, 목소리 현실 점검, 안전단어 재정비, 값 없는 전화 문구, 가족 알림 " +
                        "화면을 고르며 전화번호·연락처·안전단어·콜백 값이나 통화·문자를 사용하지 않습니다."
                )
            }
            if (hasOfficialPressureSignal) {
                add(
                    "사칭·비밀 유지·안전계좌 범주에서는 기관 압박, 연락 종료, 별도 확인, 가족 " +
                        "알림·차분 대화, 증거 개인정보, 보호자, 공식 도움·신고 준비 화면을 고르며 " +
                        "통신사·매장·직원·기관 신원·공식 번호·문서·사건을 조회하거나 전화·브라우저·" +
                        "문자·신고 제출을 하지 않습니다."
                )
            }
            if (hasLiveLinkAppSignal) {
                add(
                    "통화 유지·앱·링크·택배 문자 범주에서는 연락 종료, 링크·앱 격리, 원격지시 " +
                        "중단, 계정 보호, 노출·개인정보 화면을 고르며 통화 상태·앱·패키지·기기·" +
                        "설정·통신·SIM 상태를 읽거나 링크 열기·원격 종료·기기 정리를 하지 않습니다."
                )
            }
            if (hasFinancialRecoverySignal) {
                add(
                    "돈·회복 약속 범주에서는 송금 멈춤, 회복 약속 현실 점검, 비밀값 멈춤, 계정 " +
                        "보호, 증거 개인정보, 보호자, 공식 도움·신고 준비 화면을 고르며 결제·은행·" +
                        "환급·차지백·취소·동결·차단·추적·회복이나 번호이동·재발급 행동을 수행하지 않습니다."
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
                    "가족 연습·값 없는 전화 문구 화면을 직접 살펴보도록 권합니다. 저장된 계획의 " +
                    "실제 값은 추천에 사용하지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있다는 범주는 전화번호·SIM/eSIM/USIM·통신사·매장·계정·" +
                    "요청·코드가 안전하거나 신뢰할 수 있거나 공식이라는 뜻이 아닙니다. 준비 " +
                    "상태와 관계없이 값 공유와 번호이동·재발급 지시를 멈추고 별도 확인 경로를 고르세요."
        }
        return listOf(situationCopy, readinessCopy)
            .filter { copy -> copy.isNotBlank() }
            .joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 고정 멈춤 문장을 사용자가 직접 읽고 기존 앱 화면을 직접 고르는 로컬 전용·무상태 수동 안내이며 민감한 값을 입력하라고 요구하지 않고 새 저장소·자동 처리·자동 전송·스케줄러를 만들지 않습니다.",
        "추천은 현재 검사 결과의 위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기·가족 계획의 실제 값을 사용하지 않습니다.",
        "전화번호·새 번호·이전 번호·채널·프로필·통신사·매장·SIM/eSIM/USIM·IMSI·ICCID·IMEI·기기 일련값·기기 ID·통신 계정·고객 번호·번호이동용 PIN·OTP·PIN·비밀번호·보안 답을 입력·표시·저장·복사·공유·제출하지 않습니다.",
        "전화 통신·SIM·구독·통신사 API를 사용하지 않고 전화번호를 검증하거나 번호이동·개통·해지·재발급·명의변경·교체·활성화·비활성화·차단·해제 상태를 조회·변경하지 않습니다.",
        "전화·다이얼·발신·통화·종료·선별·차단·통화 상태·통화 기록·발신자 표시·문자·SMS·받는 사람 선택을 사용하지 않고 연락처·주소록·알림을 읽기·쓰기·편집·선택·감시·필터링하지 않습니다.",
        "네트워크·웹 탐색·브라우저·웹뷰를 사용하지 않고 공식/통신사 정보·번호·디렉터리·매장·직원·신원·문서·사건·신고·접수·법률·금융 상태를 조회·검증하거나 전화·문자·신고·출동을 요청·제출하지 않습니다.",
        "QR·바코드·OCR·카메라·이미지 분석·화면 캡처를 사용하지 않고 파일·문서·사진·스크린샷·오디오·미디어·저장소를 읽거나 선택·촬영·녹음·전사·첨부·스트림·URI 공유·업로드·동기화하지 않습니다.",
        "클립보드·텍스트 감시기·자동완성·자격 증명 API를 사용하지 않고 계정·로그인·인증·생체 인증·키가드·키 저장소·암호화 API나 계정·인증 상태를 읽거나 바꾸지 않습니다.",
        "계좌·카드·은행·지갑·거래·금액·현금·주민 식별값·상품권·코드 값을 읽기·표시·저장·공유하지 않고 결제·은행·ATM·NFC·지갑·블록체인·상품권 동작이나 구매·잔액·교환·환급·차지백·취소·동결·차단·추적·회복을 수행하지 않습니다.",
        "앱·패키지·설치 앱 목록·앱 이름·권한·기기·설정 상태를 읽거나 검사하지 않고 설치·제거·비활성화·강제 종료·권한 허용·철회·설정 변경·원격 연결 종료·기기 정리를 수행하지 않습니다.",
        "IMEI·ICCID·IMSI·기기 ID·일련값을 읽지 않고 기기 관리자·접근성 서비스·VPN·사용 통계·알림 수신 서비스·권한 관리자·패키지 설치 도구·기기 제어 기능을 사용하지 않습니다.",
        "전화번호·SIM/eSIM/USIM·통신사·매장·앱·링크·기기·계정·자격·코드·연락처·발신자·문자·사건·신고·결제·환급·회복·차단·동결·번호이동·재발급·개통·해지·취소·교체·변경·확인·보호 상태가 공식·실제·안전·보안·검증·유효·접수·제출·수신·설치·제거·정리·해결·완료·회복·환급·동결·보호·적법·신뢰·보장·문제없음이라고 판정하거나 증명하지 않습니다.",
        "수동 문자 공유는 고정 카드 제목과 기존 앱 경로·행동 종류만 담고 원문·가린 단서·계획·사건·기관 목록·연락처·채널·프로필·전화번호·SIM·통신사·계정·결제·지갑·링크·앱·기기·받는 사람·확인·제출·번호이동·재발급·회복 결과를 담거나 파일·스트림·URI를 붙이거나 자동 발송하지 않습니다.",
        "새 Android 권한을 요청하지 않고 알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러·브로드캐스트 수신기를 만들거나 예약하지 않습니다.",
        "가족 계획·사건·공식 디렉터리·연락처·알림·통화·문자·통신·SIM·구독·통신사·번호·계정·결제·앱·패키지·권한·기기·설정·자격·신고·확인·결과 상태를 수정하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<SimNumberPortOutPauseCardId>,
        recommendedActions: List<SimNumberPortOutPauseAction>,
        cards: List<SimNumberPortOutPauseCard>
    ): String {
        val cardById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[SIM·번호이동 멈춤 코치]")
            appendLine("민감한 값을 말하지 않고 통신·번호이동 압박을 멈춘 채 직접 읽을 문장을 고르는 로컬 수동 안내입니다.")
            appendLine()
            appendLine("추천 값 없는 통신·번호이동 멈춤 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 실제 전화번호·SIM/eSIM/USIM·통신사·계정·인증코드·기기·연락처·링크·결제 값은 보내지 않습니다.")
            append("- 이 안내는 번호이동·재발급·보호·복구·차단·확인·신고·계정·기기 행동을 수행하거나 결과를 보장하지 않습니다.")
        }
    }

    private fun actionFor(
        id: SimNumberPortOutPauseActionId
    ): SimNumberPortOutPauseAction = SimNumberPortOutPauseAction(
        id = id,
        label = when (id) {
            SimNumberPortOutPauseActionId.QUICK_STOP -> "긴급 멈춤 모드"
            SimNumberPortOutPauseActionId.CHECKER -> "의심 문구 검사"
            SimNumberPortOutPauseActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            SimNumberPortOutPauseActionId.SAFETY_PLAN -> "가족 안전 계획"
            SimNumberPortOutPauseActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
            SimNumberPortOutPauseActionId.FAMILY_SAFETY_DRILL_COACH -> "가족 안전 연습 코치"
            SimNumberPortOutPauseActionId.SAFE_PASTE_PRIVACY_COACH ->
                "안전 붙여넣기 개인정보 코치"
            SimNumberPortOutPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                "현재 연락을 끝내는 문장"
            SimNumberPortOutPauseActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 카드"
            SimNumberPortOutPauseActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                "가족 안전단어 재정비 코치"
            SimNumberPortOutPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                "별도 경로 확인 코치"
            SimNumberPortOutPauseActionId.SAFE_CALL_SCRIPTS -> "값 없는 안전 확인 전화 문구"
            SimNumberPortOutPauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                "목소리 사칭 현실 점검"
            SimNumberPortOutPauseActionId.NEW_NUMBER_QUARANTINE_COACH ->
                "새 연락수단 격리 코치"
            SimNumberPortOutPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                "자격 비밀값 멈춤 코치"
            SimNumberPortOutPauseActionId.FAMILY_ALERT_MESSAGES -> "가족 알림 문구"
            SimNumberPortOutPauseActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                "확인 후속 행동 보드"
            SimNumberPortOutPauseActionId.RECONTACT_BOUNDARY_COACH -> "재접촉 경계 코치"
            SimNumberPortOutPauseActionId.TRUSTED_HELPER_REVIEW -> "보호자 검토 모드"
            SimNumberPortOutPauseActionId.FAMILY_RESPONSE_ROLE_BOARD -> "가족 대응 역할 보드"
            SimNumberPortOutPauseActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                "가족 차분 대화 안건"
            SimNumberPortOutPauseActionId.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH ->
                "이동 경로·만남 장소 멈춤 코치"
            SimNumberPortOutPauseActionId.MONEY_TRANSFER_PAUSE_GATE -> "송금 전 멈춤 확인"
            SimNumberPortOutPauseActionId.GIFT_CARD_CODE_PAUSE_COACH ->
                "상품권·코드 멈춤 코치"
            SimNumberPortOutPauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH ->
                "링크·앱 설치 격리 코치"
            SimNumberPortOutPauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                "기관 사칭·압박 현실 점검"
            SimNumberPortOutPauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH ->
                "회복 약속 현실 점검"
            SimNumberPortOutPauseActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크"
            SimNumberPortOutPauseActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                "원격 접근 중단 코치"
            SimNumberPortOutPauseActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
            SimNumberPortOutPauseActionId.PRIVACY_CENTER -> "개인정보 센터"
            SimNumberPortOutPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                "증거 전달 개인정보 코치"
            SimNumberPortOutPauseActionId.EVIDENCE_KIT -> "증거 보존 체크"
            SimNumberPortOutPauseActionId.INCIDENT_TIMELINE_BRIEFING -> "사건 흐름 요약"
            SimNumberPortOutPauseActionId.OFFICIAL_HELP_DIRECTORY -> "공식 도움 연락처"
            SimNumberPortOutPauseActionId.REPORT_PREP -> "공식 신고 준비"
            SimNumberPortOutPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER -> "후속 점검 계획"
            SimNumberPortOutPauseActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
