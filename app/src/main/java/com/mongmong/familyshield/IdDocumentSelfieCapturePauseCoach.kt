package com.mongmong.familyshield

enum class IdDocumentSelfieCapturePauseCardId(val displayOrder: Int) {
    STOP_ID_DOCUMENT_PHOTOGRAPH_SCAN_UPLOAD_OR_READ(1),
    REFUSE_SELFIE_VIDEO_LIVENESS_OR_ID_HOLDING_CAPTURE(2),
    SEPARATE_LOAN_REFUND_RECOVERY_ACCOUNT_SIM_PROTECTION_PROMISES(3),
    LEAVE_LINK_APP_REMOTE_ID_FACE_ACCOUNT_FLOW(4),
    CHOOSE_FAMILY_HELPER_ACCOUNT_EXPOSURE_AND_EVIDENCE_PRIVACY_ROUTES(5),
    CHOOSE_OFFICIAL_HELP_REPORT_AND_FOLLOW_UP_ROUTES(6)
}

enum class IdDocumentSelfieCapturePauseActionId {
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
    SIM_NUMBER_PORT_OUT_PAUSE_COACH,
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

data class IdDocumentSelfieCapturePauseAction(
    val id: IdDocumentSelfieCapturePauseActionId,
    val label: String
)

data class IdDocumentSelfieCapturePauseCard(
    val id: IdDocumentSelfieCapturePauseCardId,
    val displayOrder: Int,
    val title: String,
    val idDocumentSelfiePressureSituation: String,
    val valueFreePauseScript: String,
    val whyItMatters: String,
    val doNotPhotographScanUploadReadIdFaceAccountSayShareAttachOrRecordBoundaries:
        List<String>,
    val primaryAction: IdDocumentSelfieCapturePauseAction,
    val secondaryActions: List<IdDocumentSelfieCapturePauseAction>
) {
    val routeActions: List<IdDocumentSelfieCapturePauseAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class IdDocumentSelfieCapturePauseCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<IdDocumentSelfieCapturePauseCardId>,
    val recommendedActions: List<IdDocumentSelfieCapturePauseAction>,
    val cards: List<IdDocumentSelfieCapturePauseCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<IdDocumentSelfieCapturePauseCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<IdDocumentSelfieCapturePauseActionId>
        get() = recommendedActions.map { action -> action.id }
}

object IdDocumentSelfieCapturePauseCoachBuilder {
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
    ): IdDocumentSelfieCapturePauseCoach {
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
            IdDocumentSelfieCapturePauseCardId.STOP_ID_DOCUMENT_PHOTOGRAPH_SCAN_UPLOAD_OR_READ,
            IdDocumentSelfieCapturePauseCardId.REFUSE_SELFIE_VIDEO_LIVENESS_OR_ID_HOLDING_CAPTURE,
            IdDocumentSelfieCapturePauseCardId.LEAVE_LINK_APP_REMOTE_ID_FACE_ACCOUNT_FLOW
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += IdDocumentSelfieCapturePauseCardId.entries
        }
        if (hasCredentialSignal) {
            recommendedCardSet += IdDocumentSelfieCapturePauseCardId.STOP_ID_DOCUMENT_PHOTOGRAPH_SCAN_UPLOAD_OR_READ
            recommendedCardSet += IdDocumentSelfieCapturePauseCardId.REFUSE_SELFIE_VIDEO_LIVENESS_OR_ID_HOLDING_CAPTURE
            recommendedCardSet +=
                IdDocumentSelfieCapturePauseCardId.LEAVE_LINK_APP_REMOTE_ID_FACE_ACCOUNT_FLOW
            recommendedCardSet +=
                IdDocumentSelfieCapturePauseCardId.CHOOSE_FAMILY_HELPER_ACCOUNT_EXPOSURE_AND_EVIDENCE_PRIVACY_ROUTES
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet +=
                IdDocumentSelfieCapturePauseCardId.SEPARATE_LOAN_REFUND_RECOVERY_ACCOUNT_SIM_PROTECTION_PROMISES
            recommendedCardSet +=
                IdDocumentSelfieCapturePauseCardId.CHOOSE_FAMILY_HELPER_ACCOUNT_EXPOSURE_AND_EVIDENCE_PRIVACY_ROUTES
        }
        if (hasOfficialPressureSignal) {
            recommendedCardSet += IdDocumentSelfieCapturePauseCardId.REFUSE_SELFIE_VIDEO_LIVENESS_OR_ID_HOLDING_CAPTURE
            recommendedCardSet +=
                IdDocumentSelfieCapturePauseCardId.SEPARATE_LOAN_REFUND_RECOVERY_ACCOUNT_SIM_PROTECTION_PROMISES
            recommendedCardSet +=
                IdDocumentSelfieCapturePauseCardId.CHOOSE_FAMILY_HELPER_ACCOUNT_EXPOSURE_AND_EVIDENCE_PRIVACY_ROUTES
            recommendedCardSet +=
                IdDocumentSelfieCapturePauseCardId.CHOOSE_OFFICIAL_HELP_REPORT_AND_FOLLOW_UP_ROUTES
        }
        if (hasLiveLinkAppSignal) {
            recommendedCardSet += IdDocumentSelfieCapturePauseCardId.STOP_ID_DOCUMENT_PHOTOGRAPH_SCAN_UPLOAD_OR_READ
            recommendedCardSet +=
                IdDocumentSelfieCapturePauseCardId.LEAVE_LINK_APP_REMOTE_ID_FACE_ACCOUNT_FLOW
            recommendedCardSet +=
                IdDocumentSelfieCapturePauseCardId.CHOOSE_FAMILY_HELPER_ACCOUNT_EXPOSURE_AND_EVIDENCE_PRIVACY_ROUTES
        }
        if (hasFinancialRecoverySignal) {
            recommendedCardSet += IdDocumentSelfieCapturePauseCardId.STOP_ID_DOCUMENT_PHOTOGRAPH_SCAN_UPLOAD_OR_READ
            recommendedCardSet += IdDocumentSelfieCapturePauseCardId.REFUSE_SELFIE_VIDEO_LIVENESS_OR_ID_HOLDING_CAPTURE
            recommendedCardSet +=
                IdDocumentSelfieCapturePauseCardId.SEPARATE_LOAN_REFUND_RECOVERY_ACCOUNT_SIM_PROTECTION_PROMISES
            recommendedCardSet +=
                IdDocumentSelfieCapturePauseCardId.CHOOSE_FAMILY_HELPER_ACCOUNT_EXPOSURE_AND_EVIDENCE_PRIVACY_ROUTES
            recommendedCardSet +=
                IdDocumentSelfieCapturePauseCardId.CHOOSE_OFFICIAL_HELP_REPORT_AND_FOLLOW_UP_ROUTES
        }
        if (hasSavedIncident) {
            recommendedCardSet +=
                IdDocumentSelfieCapturePauseCardId.CHOOSE_OFFICIAL_HELP_REPORT_AND_FOLLOW_UP_ROUTES
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedCardSet +=
                    IdDocumentSelfieCapturePauseCardId.CHOOSE_FAMILY_HELPER_ACCOUNT_EXPOSURE_AND_EVIDENCE_PRIVACY_ROUTES
                recommendedCardSet +=
                    IdDocumentSelfieCapturePauseCardId.CHOOSE_OFFICIAL_HELP_REPORT_AND_FOLLOW_UP_ROUTES
            }
            ReadinessLevel.READY -> {
                recommendedCardSet +=
                    IdDocumentSelfieCapturePauseCardId.STOP_ID_DOCUMENT_PHOTOGRAPH_SCAN_UPLOAD_OR_READ
                recommendedCardSet +=
                    IdDocumentSelfieCapturePauseCardId.SEPARATE_LOAN_REFUND_RECOVERY_ACCOUNT_SIM_PROTECTION_PROMISES
            }
        }
        val recommendedCardIds = IdDocumentSelfieCapturePauseCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            IdDocumentSelfieCapturePauseActionId.CHECKER
        } else {
            IdDocumentSelfieCapturePauseActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            currentResultActionId,
            IdDocumentSelfieCapturePauseActionId.QUICK_STOP,
            IdDocumentSelfieCapturePauseActionId.SAFE_PASTE_PRIVACY_COACH,
            IdDocumentSelfieCapturePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH,
            IdDocumentSelfieCapturePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
            IdDocumentSelfieCapturePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
            IdDocumentSelfieCapturePauseActionId.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH,
            IdDocumentSelfieCapturePauseActionId.SIM_NUMBER_PORT_OUT_PAUSE_COACH,
            IdDocumentSelfieCapturePauseActionId.ACCOUNT_LOCKDOWN,
            IdDocumentSelfieCapturePauseActionId.EXPOSURE_IMPACT_TRIAGE,
            IdDocumentSelfieCapturePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
            IdDocumentSelfieCapturePauseActionId.PRIVACY_CENTER,
            IdDocumentSelfieCapturePauseActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            IdDocumentSelfieCapturePauseActionId.entries
                .filterNot { actionId ->
                    actionId == IdDocumentSelfieCapturePauseActionId.ANALYSIS_RESULT
                }
                .forEach { actionId -> recommendedActionSet += actionId }
        }
        if (hasHighRisk) {
            IdDocumentSelfieCapturePauseActionId.entries.forEach { actionId ->
                recommendedActionSet += actionId
            }
        }
        if (hasCredentialSignal) {
            recommendedActionSet +=
                IdDocumentSelfieCapturePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet +=
                IdDocumentSelfieCapturePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.NEW_NUMBER_QUARANTINE_COACH
            recommendedActionSet +=
                IdDocumentSelfieCapturePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet +=
                IdDocumentSelfieCapturePauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH
            recommendedActionSet +=
                IdDocumentSelfieCapturePauseActionId.FAMILY_SAFE_WORD_RESET_COACH
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasOfficialPressureSignal) {
            recommendedActionSet +=
                IdDocumentSelfieCapturePauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                IdDocumentSelfieCapturePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet +=
                IdDocumentSelfieCapturePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.REPORT_PREP
        }
        if (hasLiveLinkAppSignal) {
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                IdDocumentSelfieCapturePauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.PRIVACY_CENTER
        }
        if (hasFinancialRecoverySignal) {
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.GIFT_CARD_CODE_PAUSE_COACH
            recommendedActionSet +=
                IdDocumentSelfieCapturePauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH
            recommendedActionSet +=
                IdDocumentSelfieCapturePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                IdDocumentSelfieCapturePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.REPORT_PREP
        }
        if (hasSavedIncident) {
            recommendedActionSet +=
                IdDocumentSelfieCapturePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.EVIDENCE_KIT
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.INCIDENT_TIMELINE_BRIEFING
            recommendedActionSet += IdDocumentSelfieCapturePauseActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedActionSet += IdDocumentSelfieCapturePauseActionId.SAFETY_PLAN
                recommendedActionSet +=
                    IdDocumentSelfieCapturePauseActionId.FAMILY_SAFE_WORD_RESET_COACH
                recommendedActionSet +=
                    IdDocumentSelfieCapturePauseActionId.FAMILY_READINESS_ROADMAP
                recommendedActionSet +=
                    IdDocumentSelfieCapturePauseActionId.FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += IdDocumentSelfieCapturePauseActionId.SAFE_CALL_SCRIPTS
            }
            ReadinessLevel.READY -> {
                recommendedActionSet += IdDocumentSelfieCapturePauseActionId.FAMILY_CALLBACK_CARD
                recommendedActionSet +=
                    IdDocumentSelfieCapturePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                recommendedActionSet += IdDocumentSelfieCapturePauseActionId.SAFE_CALL_SCRIPTS
                recommendedActionSet +=
                    IdDocumentSelfieCapturePauseActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
            }
        }
        val recommendedActions = IdDocumentSelfieCapturePauseActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null ->
                "검사 결과가 없어도 모든 신분증·셀피 촬영 멈춤 문구를 보수적으로 읽기"
            hasCredentialSignal -> "신분·얼굴·계정 비밀값을 촬영하거나 말하기 전에 멈추기"
            hasFamilyVerificationSignal ->
                "가족·새 번호·목소리 주장과 신분·얼굴 확인 요청을 분리하기"
            hasOfficialPressureSignal ->
                "기관 직함과 확인·보호·복구 약속을 신분·얼굴 요청에서 분리하기"
            hasLiveLinkAppSignal ->
                "통화·링크·앱·원격지시 흐름에서 문서·얼굴·계정 입력을 멈추기"
            hasFinancialRecoverySignal ->
                "대출·환급·회복 약속과 신분·얼굴·계정 요청을 함께 멈추기"
            hasHighRisk -> "높은 위험 범주에서 모든 멈춤 문구와 기존 대응 경로 보기"
            hasSavedIncident -> "사건 내용 없이 증거·도움·후속 경로만 고르기"
            else -> "신분증·문서·셀피·얼굴 확인 요청에 답하기 전 짧게 멈추기"
        }

        return IdDocumentSelfieCapturePauseCoach(
            title = "신분증·셀피 촬영 멈춤 코치",
            intro =
                "누군가 신분증·주민등록증·운전면허증·여권·건강보험증·통장 사본을 촬영·" +
                    "스캔·업로드·화면공유·전달하거나 내용을 읽고, 셀피·영상·신분증을 든 " +
                    "얼굴 확인을 하라고 재촉할 때 실제 값을 받지 않고 고정 멈춤 문장과 기존 " +
                    "대응 화면만 직접 고르는 로컬 전용·무상태 수동 안내입니다.",
            modeLabel = "값 입력 없음 · 카메라/파일 읽기 없음 · 촬영/검증 행동 없음",
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

    private fun buildCards(): List<IdDocumentSelfieCapturePauseCard> {
        fun action(id: IdDocumentSelfieCapturePauseActionId): IdDocumentSelfieCapturePauseAction =
            actionFor(id)

        return listOf(
            IdDocumentSelfieCapturePauseCard(
                id = IdDocumentSelfieCapturePauseCardId.STOP_ID_DOCUMENT_PHOTOGRAPH_SCAN_UPLOAD_OR_READ,
                displayOrder =
                    IdDocumentSelfieCapturePauseCardId.STOP_ID_DOCUMENT_PHOTOGRAPH_SCAN_UPLOAD_OR_READ.displayOrder,
                title = "신분증·주민번호·여권·면허증을 촬영·전송하기 전 멈추기",
                idDocumentSelfiePressureSituation =
                    "상대가 확인·대출·환급·계정 회복을 내세워 신분 문서를 지금 촬영·스캔·전송하거나 내용을 읽어 달라고 재촉하는 때",
                valueFreePauseScript =
                    "신분증이나 문서를 촬영·스캔·업로드하거나 내용을 읽지 않겠습니다. 이 신원 확인 흐름을 여기서 멈추겠습니다.",
                whyItMatters =
                    "값 없는 짧은 거절은 신분·계정 정보가 사진이나 말로 다시 노출되는 일을 줄이고 상대가 정한 속도를 끊습니다.",
                doNotPhotographScanUploadReadIdFaceAccountSayShareAttachOrRecordBoundaries =
                    listOf(
                        "실제 신분증·주민 식별값·여권·면허증·건강보험증·통장·계정·카드·은행 값을 말하거나 보여 주거나 적지 마세요.",
                        "문서를 촬영·스캔·복사·붙여넣기·첨부·업로드·화면공유하지 않고 카메라·갤러리·파일·OCR·QR·바코드를 읽지 않습니다."
                    ),
                primaryAction = action(
                    IdDocumentSelfieCapturePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH
                ),
                secondaryActions = listOf(
                    action(IdDocumentSelfieCapturePauseActionId.QUICK_STOP),
                    action(IdDocumentSelfieCapturePauseActionId.SAFE_PASTE_PRIVACY_COACH),
                    action(IdDocumentSelfieCapturePauseActionId.ACCOUNT_LOCKDOWN),
                    action(IdDocumentSelfieCapturePauseActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(
                        IdDocumentSelfieCapturePauseActionId
                            .EVIDENCE_HANDOFF_PRIVACY_COACH
                    )
                )
            ),
            IdDocumentSelfieCapturePauseCard(
                id = IdDocumentSelfieCapturePauseCardId.REFUSE_SELFIE_VIDEO_LIVENESS_OR_ID_HOLDING_CAPTURE,
                displayOrder =
                    IdDocumentSelfieCapturePauseCardId.REFUSE_SELFIE_VIDEO_LIVENESS_OR_ID_HOLDING_CAPTURE.displayOrder,
                title = "셀피·영상통화·얼굴 인증·신분증 들고 찍기 요구 거절하기",
                idDocumentSelfiePressureSituation =
                    "상대가 본인 확인이나 실시간 확인을 이유로 얼굴·셀피·영상·신분증을 함께 찍거나 보여 달라고 하는 때",
                valueFreePauseScript =
                    "셀피·영상·얼굴 확인이나 신분증을 들고 찍는 요구를 따르지 않겠습니다. 현재 연락과 촬영 흐름을 끝내겠습니다.",
                whyItMatters =
                    "실시간이라는 말이나 얼굴 확인 화면은 상대의 신원·기관·절차가 공식이거나 필요한지 증명하지 않습니다.",
                doNotPhotographScanUploadReadIdFaceAccountSayShareAttachOrRecordBoundaries =
                    listOf(
                        "실제 셀피·얼굴·음성·영상·신분 문서·계정·연락처·화면 값을 촬영·녹화·말하기·전달하지 마세요.",
                        "카메라·마이크·영상통화·얼굴 인식·얼굴 감지·생존 확인·생체 인증을 사용하거나 촬영·녹음·분석·판정하지 않습니다."
                    ),
                primaryAction = action(
                    IdDocumentSelfieCapturePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
                ),
                secondaryActions = listOf(
                    action(IdDocumentSelfieCapturePauseActionId.REMOTE_ACCESS_CUTOFF_COACH),
                    action(
                        IdDocumentSelfieCapturePauseActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(
                        IdDocumentSelfieCapturePauseActionId
                            .VOICE_IMPERSONATION_REALITY_CHECK_COACH
                    ),
                    action(IdDocumentSelfieCapturePauseActionId.RECONTACT_BOUNDARY_COACH),
                    action(IdDocumentSelfieCapturePauseActionId.PRIVACY_CENTER)
                )
            ),
            IdDocumentSelfieCapturePauseCard(
                id =
                    IdDocumentSelfieCapturePauseCardId
                        .SEPARATE_LOAN_REFUND_RECOVERY_ACCOUNT_SIM_PROTECTION_PROMISES,
                displayOrder =
                    IdDocumentSelfieCapturePauseCardId
                        .SEPARATE_LOAN_REFUND_RECOVERY_ACCOUNT_SIM_PROTECTION_PROMISES.displayOrder,
                title = "대출·환급·회복·계정/SIM 보호 약속과 신분 확인 요청 분리하기",
                idDocumentSelfiePressureSituation =
                    "대출·환급·계정 회복·번호 보호나 기관 절차를 약속하며 신분 문서·얼굴·계정 확인을 재촉하는 때",
                valueFreePauseScript =
                    "대출·환급·회복·보호 약속을 신분 확인의 근거로 쓰지 않겠습니다. 돈·계정·SIM 행동도 함께 멈추겠습니다.",
                whyItMatters =
                    "약속과 신분 확인 요구를 분리하면 상대가 제공한 설명만으로 기관·계정·번호·결과 상태를 확정하지 않게 됩니다.",
                doNotPhotographScanUploadReadIdFaceAccountSayShareAttachOrRecordBoundaries =
                    listOf(
                        "신분·주민·여권·면허·문서·셀피·얼굴·계정·전화번호·SIM·기기·결제·환급·사건 값을 말하거나 공유하지 마세요.",
                        "기관·직원·신원·문서·계정·SIM·대출·환급·회복·보호 상태를 조회·검증하거나 전화·문자·웹·은행 행동을 하지 않습니다."
                    ),
                primaryAction = action(
                    IdDocumentSelfieCapturePauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH
                ),
                secondaryActions = listOf(
                    action(IdDocumentSelfieCapturePauseActionId.MONEY_TRANSFER_PAUSE_GATE),
                    action(
                        IdDocumentSelfieCapturePauseActionId
                            .OFFICIAL_PRESSURE_REALITY_CHECK_COACH
                    ),
                    action(IdDocumentSelfieCapturePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH),
                    action(IdDocumentSelfieCapturePauseActionId.ACCOUNT_LOCKDOWN),
                    action(IdDocumentSelfieCapturePauseActionId.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
                )
            ),
            IdDocumentSelfieCapturePauseCard(
                id = IdDocumentSelfieCapturePauseCardId.LEAVE_LINK_APP_REMOTE_ID_FACE_ACCOUNT_FLOW,
                displayOrder =
                    IdDocumentSelfieCapturePauseCardId
                        .LEAVE_LINK_APP_REMOTE_ID_FACE_ACCOUNT_FLOW.displayOrder,
                title = "링크·앱·원격지시 안에서 문서/얼굴/계정 정보를 입력하지 않기",
                idDocumentSelfiePressureSituation =
                    "상대가 통화나 화면공유를 유지한 채 링크·앱·원격지시 안에서 문서·얼굴·계정 정보를 입력하라고 하는 때",
                valueFreePauseScript =
                    "현재 연락과 화면 흐름을 벗어나고 링크·앱·원격지시 안에서는 문서·얼굴·계정 정보를 입력하지 않겠습니다.",
                whyItMatters =
                    "상대가 만든 흐름 밖으로 나오면 입력 위치와 다음 행동까지 계속 통제되는 일을 줄일 수 있습니다.",
                doNotPhotographScanUploadReadIdFaceAccountSayShareAttachOrRecordBoundaries =
                    listOf(
                        "링크·도메인·앱·패키지·파일·문서·사진·영상·얼굴·계정·로그인·기기·화면 값을 말하거나 공유하지 마세요.",
                        "네트워크·브라우저·앱 목록·파일·카메라·기기·설정을 읽거나 링크 열기·설치/제거·원격 종료·기기 정리를 하지 않습니다."
                    ),
                primaryAction = action(
                    IdDocumentSelfieCapturePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
                ),
                secondaryActions = listOf(
                    action(IdDocumentSelfieCapturePauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH),
                    action(IdDocumentSelfieCapturePauseActionId.REMOTE_ACCESS_CUTOFF_COACH),
                    action(IdDocumentSelfieCapturePauseActionId.ACCOUNT_LOCKDOWN),
                    action(IdDocumentSelfieCapturePauseActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(IdDocumentSelfieCapturePauseActionId.PRIVACY_CENTER)
                )
            ),
            IdDocumentSelfieCapturePauseCard(
                id =
                    IdDocumentSelfieCapturePauseCardId
                        .CHOOSE_FAMILY_HELPER_ACCOUNT_EXPOSURE_AND_EVIDENCE_PRIVACY_ROUTES,
                displayOrder =
                    IdDocumentSelfieCapturePauseCardId
                        .CHOOSE_FAMILY_HELPER_ACCOUNT_EXPOSURE_AND_EVIDENCE_PRIVACY_ROUTES.displayOrder,
                title = "민감값 없는 가족·보호자·계정/노출·증거 개인정보 경로 고르기",
                idDocumentSelfiePressureSituation =
                    "신분·얼굴 요청을 멈춘 뒤 가족 확인·보호자 검토·계정 안내·노출 분류·증거 개인정보 중 다음 화면을 고르려는 때",
                valueFreePauseScript =
                    "민감한 값을 다시 말하지 않고 가족의 기존 확인 경로와 값 없는 보호자·계정·노출·증거 개인정보 안내를 직접 고르겠습니다.",
                whyItMatters =
                    "기존 화면으로만 이동하면 신분·얼굴·계정·사건 값을 새로 수집하지 않고 필요한 대응 종류를 나눌 수 있습니다.",
                doNotPhotographScanUploadReadIdFaceAccountSayShareAttachOrRecordBoundaries =
                    listOf(
                        "안전단어·콜백 문장·연락처·신분증·주민 식별값·얼굴·계정·기기·사건 내용을 말하거나 공유하지 마세요.",
                        "연락처·통화·문자·알림·계정·로그인·문서·카메라·기기 상태를 읽거나 가족·계정·증거·확인 상태를 바꾸지 않습니다."
                    ),
                primaryAction = action(
                    IdDocumentSelfieCapturePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                ),
                secondaryActions = listOf(
                    action(IdDocumentSelfieCapturePauseActionId.FAMILY_CALLBACK_CARD),
                    action(IdDocumentSelfieCapturePauseActionId.FAMILY_ALERT_MESSAGES),
                    action(IdDocumentSelfieCapturePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH),
                    action(IdDocumentSelfieCapturePauseActionId.ACCOUNT_LOCKDOWN),
                    action(IdDocumentSelfieCapturePauseActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(
                        IdDocumentSelfieCapturePauseActionId
                            .EVIDENCE_HANDOFF_PRIVACY_COACH
                    ),
                    action(
                        IdDocumentSelfieCapturePauseActionId
                            .VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    ),
                    action(IdDocumentSelfieCapturePauseActionId.TRUSTED_HELPER_REVIEW)
                )
            ),
            IdDocumentSelfieCapturePauseCard(
                id = IdDocumentSelfieCapturePauseCardId.CHOOSE_OFFICIAL_HELP_REPORT_AND_FOLLOW_UP_ROUTES,
                displayOrder =
                    IdDocumentSelfieCapturePauseCardId
                        .CHOOSE_OFFICIAL_HELP_REPORT_AND_FOLLOW_UP_ROUTES.displayOrder,
                title = "공식 도움·신고 준비·후속 점검 경로 고르기",
                idDocumentSelfiePressureSituation =
                    "신분·얼굴·계정 요청을 멈춘 뒤 증거 개인정보·보호자·공식 도움·신고 준비·가족 역할·후속 점검 순서를 고르려는 때",
                valueFreePauseScript =
                    "값이나 결과 상태를 말하지 않고 기존 도움·신고 준비·가족·후속 점검 화면을 직접 고르겠습니다.",
                whyItMatters =
                    "고정 경로 이름만 고르면 신분·얼굴·사건·기관·사람·계정 값을 다시 공개하지 않고 다음 수동 단계를 살펴볼 수 있습니다.",
                doNotPhotographScanUploadReadIdFaceAccountSayShareAttachOrRecordBoundaries =
                    listOf(
                        "원문·가린 단서·신분·얼굴·사건·기관·직원·도우미·문서·접수·결과·계정·기기 값을 말하거나 공유하지 마세요.",
                        "증거를 촬영·녹음·스캔·첨부·업로드하지 않고 공식 조회·전화·문자·신고 제출·법률·금융 결과를 만들지 않습니다."
                    ),
                primaryAction = action(
                    IdDocumentSelfieCapturePauseActionId.TRUSTED_HELPER_REVIEW
                ),
                secondaryActions = listOf(
                    action(IdDocumentSelfieCapturePauseActionId.FAMILY_RESPONSE_ROLE_BOARD),
                    action(IdDocumentSelfieCapturePauseActionId.FAMILY_CALM_DEBRIEF_AGENDA),
                    action(
                        IdDocumentSelfieCapturePauseActionId
                            .EVIDENCE_HANDOFF_PRIVACY_COACH
                    ),
                    action(IdDocumentSelfieCapturePauseActionId.EVIDENCE_KIT),
                    action(IdDocumentSelfieCapturePauseActionId.INCIDENT_TIMELINE_BRIEFING),
                    action(IdDocumentSelfieCapturePauseActionId.OFFICIAL_HELP_DIRECTORY),
                    action(IdDocumentSelfieCapturePauseActionId.REPORT_PREP),
                    action(IdDocumentSelfieCapturePauseActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                    action(IdDocumentSelfieCapturePauseActionId.SAFETY_FLOW_ROUTER)
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
                    "검사 결과나 일치 신호가 없어도 신분증·주민등록증·여권·면허증·문서·" +
                        "파일·사진·영상·셀피·얼굴·라이브니스·생존 확인·QR·바코드·계정·전화·SIM·링크·" +
                        "앱·기관·직원·사건·신고·결제·환급·회복·촬영·스캔·업로드·제출·" +
                        "검증·보호 상태를 공식·검증·안전·유효·접수·완료라고 단정하지 않고 " +
                        "모든 값 없는 멈춤 카드와 기존 대응 경로를 보수적으로 보여 줍니다."
                )
            }
            if (hasHighRisk) {
                add(
                    "높은 위험 범주에서는 신분 문서·셀피·얼굴·계정 정보 공유와 촬영·스캔·" +
                        "업로드 지시를 멈추고 가족·계정·노출·증거·도움·후속 경로를 값 없이 넓게 보여 줍니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증·상품권·결제 범주에서는 자격 비밀값 멈춤, 계정 보호, 안전 붙여넣기 " +
                        "개인정보, 노출 분류, 증거 개인정보 화면을 고르며 OTP·PIN·비밀번호·" +
                        "주민 식별값·신분증·문서·계정·카드·은행·지갑 값을 사용하지 않습니다."
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
                        "기관·직원 신원·공식 번호·신분증·문서·사건을 조회·확인·검증하거나 " +
                        "전화·브라우저·문자·신고 제출을 하지 않습니다."
                )
            }
            if (hasLiveLinkAppSignal) {
                add(
                    "통화 유지·앱·링크·택배 문자 범주에서는 연락 종료, 링크·앱 격리, 원격지시 " +
                        "중단, 계정 보호, 노출·개인정보 화면을 고르며 통화 상태·앱·패키지·기기·" +
                        "설정·카메라·파일 상태를 읽거나 링크 열기·원격 종료·기기 정리를 하지 않습니다."
                )
            }
            if (hasFinancialRecoverySignal) {
                add(
                    "돈·회복 약속 범주에서는 송금 멈춤, 회복 약속 현실 점검, 비밀값 멈춤, 계정 " +
                        "보호, 증거 개인정보, 보호자, 공식 도움·신고 준비 화면을 고르며 결제·은행·" +
                        "환급·차지백·취소·동결·차단·추적·회복이나 신분·얼굴 검증 행동을 수행하지 않습니다."
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
                "가족 준비가 되어 있다는 범주는 신분증·문서·셀피·얼굴·라이브니스·생존 확인·계정·요청이 " +
                    "안전하거나 신뢰할 수 있거나 공식이라는 뜻이 아닙니다. 준비 상태와 관계없이 " +
                    "촬영·스캔·업로드·읽기 지시를 멈추고 별도 확인 경로를 고르세요."
        }
        return listOf(situationCopy, readinessCopy)
            .filter { copy -> copy.isNotBlank() }
            .joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 고정 멈춤 문장을 사용자가 직접 읽고 기존 앱 화면을 직접 고르는 로컬 전용·무상태 수동 안내이며 민감한 값을 입력하라고 요구하지 않고 새 저장소·자동 처리·자동 전송·스케줄러를 만들지 않습니다.",
        "추천은 현재 검사 결과의 위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기·가족 계획의 실제 값을 사용하지 않습니다.",
        "신분증·주민등록증·주민 식별값·여권·면허증·건강보험증·통장·문서·파일·사진·영상·셀피·얼굴·라이브니스·생존 확인·생체·갤러리·카메라·스크린샷·OCR·QR·바코드·미디어·URI·경로 값을 입력·표시·저장·복사·공유·제출하지 않습니다.",
        "카메라·CameraX·Camera2·마이크·MediaRecorder·AudioRecord·OCR·텍스트 인식·QR·바코드 스캔·이미지 분석·얼굴 인식·얼굴 감지·라이브니스·생존 확인·생체 인증·셀피/영상 촬영·문서 스캔을 사용하지 않습니다.",
        "파일·문서·사진·영상·미디어·갤러리·스크린샷·저장소를 읽거나 고르지 않고 파일 선택기·촬영·녹음·전사·첨부·업로드·동기화·스트림·URI 공유를 사용하지 않습니다.",
        "신분증·주민등록증·여권·면허증·건강보험증·통장·문서·얼굴·라이브니스·생존 확인을 검사·분석·일치·확인·검증·유효성 판정·가림·정제하지 않습니다.",
        "전화·다이얼·발신·통화·종료·선별·차단·통화 상태·통화 기록·발신자 표시·문자·SMS·받는 사람 선택을 사용하지 않고 연락처·주소록·알림을 읽기·쓰기·편집·선택·감시·필터링하지 않습니다.",
        "네트워크·웹 탐색·브라우저·웹뷰·외부 링크 열기를 사용하지 않고 공식 정보·번호·디렉터리·기관·직원·신원·문서·사건·신고·접수·법률·금융 상태를 조회·검증하거나 전화·문자·신고·출동을 요청·제출하지 않습니다.",
        "클립보드·텍스트 감시기·자동완성·자격 증명 API를 사용하지 않고 계정·로그인·인증·생체 인증·키가드·키 저장소·암호화 API나 계정·인증 상태를 읽거나 바꾸지 않습니다.",
        "계좌·카드·은행·지갑·거래·금액·현금·주민 식별값·상품권·코드 값을 읽기·표시·저장·공유하지 않고 결제·은행·ATM·NFC·지갑·블록체인·상품권 동작이나 구매·잔액·교환·환급·차지백·취소·동결·차단·추적·회복을 수행하지 않습니다.",
        "전화 통신·SIM·구독·통신사 API를 사용하지 않고 전화번호·SIM/eSIM/USIM·IMEI·ICCID·IMSI·기기 ID·일련값·계정·고객 값을 읽거나 조회·검증·변경하지 않습니다.",
        "앱·패키지·설치 앱 목록·앱 이름·권한·기기·설정 상태를 읽거나 검사하지 않고 설치·제거·비활성화·강제 종료·권한 허용·철회·설정 변경·원격 연결 종료·기기 정리를 수행하지 않습니다.",
        "IMEI·ICCID·IMSI·기기 ID·일련값을 읽지 않고 기기 관리자·접근성 서비스·VPN·사용 통계·알림 수신 서비스·권한 관리자·패키지 설치 도구·기기 제어 기능을 사용하지 않습니다.",
        "신분증·문서·셀피·얼굴·라이브니스·생존 확인·QR·바코드·파일·사진·영상·계정·전화·SIM·링크·앱·기관·직원·사건·신고·결제·환급·회복·차단·동결·업로드·제출·검증·가림·정제·촬영·스캔·분석·일치·보호 상태가 공식·실제·안전·보안·검증·유효·접수·제출·수신·해결·완료·회복·환급·동결·보호·적법·신뢰·보장·문제없음이라고 판정하거나 증명하지 않습니다.",
        "수동 문자 공유는 고정 카드 제목과 기존 앱 경로·행동 종류만 담고 원문·가린 단서·계획·사건·기관 목록·연락처·전화번호·SIM·계정·결제·링크·앱·기기·신분증·문서·얼굴·미디어·받는 사람·확인·제출·검증·촬영·스캔·업로드·회복 결과를 담거나 파일·스트림·URI를 붙이거나 자동 발송하지 않습니다.",
        "새 Android 권한을 요청하지 않고 알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러·브로드캐스트 수신기를 만들거나 예약하지 않습니다.",
        "가족 계획·사건·공식 디렉터리·연락처·알림·통화·문자·카메라·파일·미디어·문서·신분증·얼굴·생체·OCR·QR·바코드·통신·SIM·계정·결제·앱·패키지·권한·기기·설정·자격·신고·확인·결과 상태를 수정하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<IdDocumentSelfieCapturePauseCardId>,
        recommendedActions: List<IdDocumentSelfieCapturePauseAction>,
        cards: List<IdDocumentSelfieCapturePauseCard>
    ): String {
        val cardById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[신분증·셀피 촬영 멈춤 코치]")
            appendLine("민감한 신분·얼굴 값을 받지 않고 신원 확인 압박을 멈춘 채 직접 읽을 문장을 고르는 로컬 수동 안내입니다.")
            appendLine()
            appendLine("추천 값 없는 신분증·셀피 촬영 멈춤 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 실제 신분증·주민 식별값·여권·면허증·문서·사진·영상·셀피·얼굴·계정 값은 보내지 않습니다.")
            append("- 이 안내는 촬영·스캔·업로드·확인·검증·제출·계정·결제·신고 행동을 수행하거나 결과를 보장하지 않습니다.")
        }
    }

    private fun actionFor(
        id: IdDocumentSelfieCapturePauseActionId
    ): IdDocumentSelfieCapturePauseAction = IdDocumentSelfieCapturePauseAction(
        id = id,
        label = when (id) {
            IdDocumentSelfieCapturePauseActionId.QUICK_STOP -> "긴급 멈춤 모드"
            IdDocumentSelfieCapturePauseActionId.CHECKER -> "의심 문구 검사"
            IdDocumentSelfieCapturePauseActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            IdDocumentSelfieCapturePauseActionId.SAFETY_PLAN -> "가족 안전 계획"
            IdDocumentSelfieCapturePauseActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
            IdDocumentSelfieCapturePauseActionId.FAMILY_SAFETY_DRILL_COACH -> "가족 안전 연습 코치"
            IdDocumentSelfieCapturePauseActionId.SAFE_PASTE_PRIVACY_COACH ->
                "안전 붙여넣기 개인정보 코치"
            IdDocumentSelfieCapturePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                "현재 연락을 끝내는 문장"
            IdDocumentSelfieCapturePauseActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 카드"
            IdDocumentSelfieCapturePauseActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                "가족 안전단어 재정비 코치"
            IdDocumentSelfieCapturePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                "별도 경로 확인 코치"
            IdDocumentSelfieCapturePauseActionId.SAFE_CALL_SCRIPTS -> "값 없는 안전 확인 전화 문구"
            IdDocumentSelfieCapturePauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                "목소리 사칭 현실 점검"
            IdDocumentSelfieCapturePauseActionId.NEW_NUMBER_QUARANTINE_COACH ->
                "새 연락수단 격리 코치"
            IdDocumentSelfieCapturePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                "자격 비밀값 멈춤 코치"
            IdDocumentSelfieCapturePauseActionId.FAMILY_ALERT_MESSAGES -> "가족 알림 문구"
            IdDocumentSelfieCapturePauseActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                "확인 후속 행동 보드"
            IdDocumentSelfieCapturePauseActionId.RECONTACT_BOUNDARY_COACH -> "재접촉 경계 코치"
            IdDocumentSelfieCapturePauseActionId.TRUSTED_HELPER_REVIEW -> "보호자 검토 모드"
            IdDocumentSelfieCapturePauseActionId.FAMILY_RESPONSE_ROLE_BOARD -> "가족 대응 역할 보드"
            IdDocumentSelfieCapturePauseActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                "가족 차분 대화 안건"
            IdDocumentSelfieCapturePauseActionId.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH ->
                "이동 경로·만남 장소 멈춤 코치"
            IdDocumentSelfieCapturePauseActionId.SIM_NUMBER_PORT_OUT_PAUSE_COACH ->
                "SIM·번호이동 멈춤 코치"
            IdDocumentSelfieCapturePauseActionId.MONEY_TRANSFER_PAUSE_GATE -> "송금 전 멈춤 확인"
            IdDocumentSelfieCapturePauseActionId.GIFT_CARD_CODE_PAUSE_COACH ->
                "상품권·코드 멈춤 코치"
            IdDocumentSelfieCapturePauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH ->
                "링크·앱 설치 격리 코치"
            IdDocumentSelfieCapturePauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                "기관 사칭·압박 현실 점검"
            IdDocumentSelfieCapturePauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH ->
                "회복 약속 현실 점검"
            IdDocumentSelfieCapturePauseActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크"
            IdDocumentSelfieCapturePauseActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                "원격 접근 중단 코치"
            IdDocumentSelfieCapturePauseActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
            IdDocumentSelfieCapturePauseActionId.PRIVACY_CENTER -> "개인정보 센터"
            IdDocumentSelfieCapturePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                "증거 전달 개인정보 코치"
            IdDocumentSelfieCapturePauseActionId.EVIDENCE_KIT -> "증거 보존 체크"
            IdDocumentSelfieCapturePauseActionId.INCIDENT_TIMELINE_BRIEFING -> "사건 흐름 요약"
            IdDocumentSelfieCapturePauseActionId.OFFICIAL_HELP_DIRECTORY -> "공식 도움 연락처"
            IdDocumentSelfieCapturePauseActionId.REPORT_PREP -> "공식 신고 준비"
            IdDocumentSelfieCapturePauseActionId.FOLLOW_UP_CHECK_IN_PLANNER -> "후속 점검 계획"
            IdDocumentSelfieCapturePauseActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
