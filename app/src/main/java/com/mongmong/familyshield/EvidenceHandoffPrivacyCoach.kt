package com.mongmong.familyshield

enum class EvidenceHandoffPrivacyCardId(val displayOrder: Int) {
    BEFORE_FORWARD_REMOVE_VALUES(1),
    FAMILY_HELPER_ACTION_CATEGORIES(2),
    OFFICIAL_CONSULTATION_PREP(3),
    MONEY_ACCOUNT_CREDENTIALS(4),
    LINK_APP_REMOTE_DETAILS(5),
    FOLLOW_UP_WITHOUT_HANDOFF_LOG(6)
}

enum class EvidenceHandoffPrivacyActionId {
    QUICK_STOP,
    CHECKER,
    ANALYSIS_RESULT,
    SAFE_PASTE_PRIVACY_COACH,
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
    INCIDENT_TIMELINE_BRIEFING,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    RECOVERY_PLAN,
    FOLLOW_UP_CHECK_IN_PLANNER,
    FAMILY_CALM_DEBRIEF_AGENDA,
    SAFETY_FLOW_ROUTER
}

data class EvidenceHandoffPrivacyAction(
    val id: EvidenceHandoffPrivacyActionId,
    val label: String
)

data class EvidenceHandoffPrivacyCard(
    val id: EvidenceHandoffPrivacyCardId,
    val displayOrder: Int,
    val title: String,
    val handoffScenario: String,
    val privacyActionSentence: String,
    val whyItMatters: String,
    val doNotAttachRecordOrShareBoundaries: List<String>,
    val primaryAction: EvidenceHandoffPrivacyAction,
    val secondaryActions: List<EvidenceHandoffPrivacyAction>
) {
    val routeActions: List<EvidenceHandoffPrivacyAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class EvidenceHandoffPrivacyCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<EvidenceHandoffPrivacyCardId>,
    val recommendedActions: List<EvidenceHandoffPrivacyAction>,
    val cards: List<EvidenceHandoffPrivacyCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<EvidenceHandoffPrivacyCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<EvidenceHandoffPrivacyActionId>
        get() = recommendedActions.map { action -> action.id }
}

object EvidenceHandoffPrivacyCoachBuilder {
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
    ): EvidenceHandoffPrivacyCoach {
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
            EvidenceHandoffPrivacyCardId.BEFORE_FORWARD_REMOVE_VALUES,
            EvidenceHandoffPrivacyCardId.FAMILY_HELPER_ACTION_CATEGORIES,
            EvidenceHandoffPrivacyCardId.FOLLOW_UP_WITHOUT_HANDOFF_LOG
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += EvidenceHandoffPrivacyCardId.entries
        }
        if (hasFamilyVerificationSignal || hasSecrecyImpersonationSignal) {
            recommendedCardSet += EvidenceHandoffPrivacyCardId.FAMILY_HELPER_ACTION_CATEGORIES
        }
        if (hasHighRisk || hasSecrecyImpersonationSignal || hasFinancialSignal || hasSavedIncident) {
            recommendedCardSet += EvidenceHandoffPrivacyCardId.OFFICIAL_CONSULTATION_PREP
        }
        if (hasHighRisk || hasFinancialSignal || hasCredentialSignal) {
            recommendedCardSet += EvidenceHandoffPrivacyCardId.MONEY_ACCOUNT_CREDENTIALS
        }
        if (hasHighRisk || hasLinkSignal || hasRemoteAccessSignal) {
            recommendedCardSet += EvidenceHandoffPrivacyCardId.LINK_APP_REMOTE_DETAILS
        }
        if (hasSavedIncident || needsReadinessSupport) {
            recommendedCardSet += EvidenceHandoffPrivacyCardId.FOLLOW_UP_WITHOUT_HANDOFF_LOG
        }
        val recommendedCardIds = EvidenceHandoffPrivacyCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            EvidenceHandoffPrivacyActionId.CHECKER
        } else {
            EvidenceHandoffPrivacyActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            EvidenceHandoffPrivacyActionId.SAFE_PASTE_PRIVACY_COACH,
            EvidenceHandoffPrivacyActionId.PRIVACY_CENTER,
            EvidenceHandoffPrivacyActionId.TRUSTED_HELPER_REVIEW,
            EvidenceHandoffPrivacyActionId.EVIDENCE_KIT,
            EvidenceHandoffPrivacyActionId.FOLLOW_UP_CHECK_IN_PLANNER,
            EvidenceHandoffPrivacyActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            recommendedActionSet += EvidenceHandoffPrivacyActionId.QUICK_STOP
            recommendedActionSet += currentResultActionId
            recommendedActionSet += EvidenceHandoffPrivacyActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += EvidenceHandoffPrivacyActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet +=
                EvidenceHandoffPrivacyActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet +=
                EvidenceHandoffPrivacyActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
            recommendedActionSet += EvidenceHandoffPrivacyActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += EvidenceHandoffPrivacyActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += EvidenceHandoffPrivacyActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += EvidenceHandoffPrivacyActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += EvidenceHandoffPrivacyActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += EvidenceHandoffPrivacyActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += EvidenceHandoffPrivacyActionId.RECOVERY_PLAN
            recommendedActionSet += EvidenceHandoffPrivacyActionId.FAMILY_CALM_DEBRIEF_AGENDA
        }
        if (hasHighRisk) {
            recommendedActionSet += EvidenceHandoffPrivacyActionId.QUICK_STOP
            recommendedActionSet += EvidenceHandoffPrivacyActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += EvidenceHandoffPrivacyActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += EvidenceHandoffPrivacyActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += EvidenceHandoffPrivacyActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += EvidenceHandoffPrivacyActionId.REPORT_PREP
            recommendedActionSet += EvidenceHandoffPrivacyActionId.RECOVERY_PLAN
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += EvidenceHandoffPrivacyActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += EvidenceHandoffPrivacyActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += EvidenceHandoffPrivacyActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += EvidenceHandoffPrivacyActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                EvidenceHandoffPrivacyActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet +=
                EvidenceHandoffPrivacyActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedActionSet += EvidenceHandoffPrivacyActionId.QUICK_STOP
            recommendedActionSet += EvidenceHandoffPrivacyActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += EvidenceHandoffPrivacyActionId.FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet += EvidenceHandoffPrivacyActionId.EVIDENCE_KIT
            recommendedActionSet += EvidenceHandoffPrivacyActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += EvidenceHandoffPrivacyActionId.REPORT_PREP
            recommendedActionSet +=
                EvidenceHandoffPrivacyActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
        }
        if (hasFinancialSignal) {
            recommendedActionSet += EvidenceHandoffPrivacyActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += EvidenceHandoffPrivacyActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += EvidenceHandoffPrivacyActionId.EVIDENCE_KIT
            recommendedActionSet += EvidenceHandoffPrivacyActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += EvidenceHandoffPrivacyActionId.REPORT_PREP
            recommendedActionSet += EvidenceHandoffPrivacyActionId.RECOVERY_PLAN
            recommendedActionSet += EvidenceHandoffPrivacyActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (hasCredentialSignal) {
            recommendedActionSet += EvidenceHandoffPrivacyActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += EvidenceHandoffPrivacyActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += EvidenceHandoffPrivacyActionId.EVIDENCE_KIT
        }
        if (hasLinkSignal) {
            recommendedActionSet += EvidenceHandoffPrivacyActionId.CHECKER
            recommendedActionSet += EvidenceHandoffPrivacyActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += EvidenceHandoffPrivacyActionId.PRIVACY_CENTER
            recommendedActionSet += EvidenceHandoffPrivacyActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet += EvidenceHandoffPrivacyActionId.EVIDENCE_KIT
        }
        if (hasRemoteAccessSignal) {
            recommendedActionSet += EvidenceHandoffPrivacyActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += EvidenceHandoffPrivacyActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += EvidenceHandoffPrivacyActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += EvidenceHandoffPrivacyActionId.EVIDENCE_KIT
        }
        if (hasSavedIncident) {
            recommendedActionSet += EvidenceHandoffPrivacyActionId.EVIDENCE_KIT
            recommendedActionSet += EvidenceHandoffPrivacyActionId.INCIDENT_TIMELINE_BRIEFING
            if (analysis != null) {
                recommendedActionSet += EvidenceHandoffPrivacyActionId.REPORT_PREP
            }
            recommendedActionSet += EvidenceHandoffPrivacyActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (needsReadinessSupport) {
            recommendedActionSet += EvidenceHandoffPrivacyActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += EvidenceHandoffPrivacyActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += EvidenceHandoffPrivacyActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += EvidenceHandoffPrivacyActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += EvidenceHandoffPrivacyActionId.FAMILY_READINESS_ROADMAP
        }
        val recommendedActions = EvidenceHandoffPrivacyActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null -> "분석 전에도 모든 증거 전달 경계를 보수적으로 읽기"
            hasRemoteAccessSignal -> "앱·화면·기기 세부값을 전달하지 않고 보호 경로 고르기"
            hasCredentialSignal -> "인증·계정 값을 전달하지 않고 계정 보호 경로 고르기"
            hasFinancialSignal -> "돈 관련 값을 전달하지 않고 멈춤·증거·도움 경로 고르기"
            hasHighRisk -> "높은 위험 신호에서 원본 보관과 값 없는 전달을 분리하기"
            hasFamilyVerificationSignal -> "가족 확인 비밀 없이 행동 종류만 함께 말하기"
            hasSecrecyImpersonationSignal -> "전달을 혼자 숨기지 않고 가족·도움 경로 고르기"
            hasLinkSignal -> "정확한 링크 대신 노출 종류와 다음 경로만 말하기"
            hasSavedIncident -> "저장 사건 내용 없이 증거·후속 경로 고르기"
            else -> "보내기 전에 필요한 행동 종류와 기존 경로만 고르기"
        }

        return EvidenceHandoffPrivacyCoach(
            title = "증거 전달 개인정보 코치",
            intro =
                "가족·보호자·상담 창구에 증거를 건네기 전에 원문과 민감값의 전달을 멈추고, " +
                    "값 없는 문장과 기존 앱 경로를 사용자가 직접 고르는 로컬 전용·무상태 수동 " +
                    "안내입니다. 증거 파일이나 화면을 받거나 판정하지 않습니다.",
            modeLabel = "보내기 전 · 원본 보관과 값 없는 설명 분리",
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

    private fun buildCards(): List<EvidenceHandoffPrivacyCard> {
        fun action(id: EvidenceHandoffPrivacyActionId): EvidenceHandoffPrivacyAction =
            actionFor(id)

        return listOf(
            EvidenceHandoffPrivacyCard(
                id = EvidenceHandoffPrivacyCardId.BEFORE_FORWARD_REMOVE_VALUES,
                displayOrder =
                    EvidenceHandoffPrivacyCardId.BEFORE_FORWARD_REMOVE_VALUES.displayOrder,
                title = "원문·스크린샷 보내기 전: 민감값 먼저 빼기",
                handoffScenario =
                    "원문·스크린샷·사진·파일을 가족이나 도움 창구에 그대로 보내려는 때",
                privacyActionSentence =
                    "원본은 제가 따로 보관하고, 지금은 필요한 행동 종류만 말하겠습니다.",
                whyItMatters =
                    "원본 화면에는 대화와 무관한 인증값·계정값·연락값·기기값이 함께 보일 수 있어 보관과 전달을 분리해야 합니다.",
                doNotAttachRecordOrShareBoundaries = listOf(
                    "원문·스크린샷·사진·파일·미디어를 이 코치에 붙이거나 올리거나 첨부하지 마세요.",
                    "이름·연락처·정확한 번호·계정·카드·OTP·PIN·비밀번호·신분값·기기 식별값·파일 이름·경로를 적거나 공유하지 마세요."
                ),
                primaryAction = action(EvidenceHandoffPrivacyActionId.SAFE_PASTE_PRIVACY_COACH),
                secondaryActions = listOf(
                    action(EvidenceHandoffPrivacyActionId.EVIDENCE_KIT),
                    action(EvidenceHandoffPrivacyActionId.PRIVACY_CENTER),
                    action(EvidenceHandoffPrivacyActionId.CHECKER),
                    action(EvidenceHandoffPrivacyActionId.ANALYSIS_RESULT)
                )
            ),
            EvidenceHandoffPrivacyCard(
                id = EvidenceHandoffPrivacyCardId.FAMILY_HELPER_ACTION_CATEGORIES,
                displayOrder =
                    EvidenceHandoffPrivacyCardId.FAMILY_HELPER_ACTION_CATEGORIES.displayOrder,
                title = "가족·보호자에게 줄 때: 행동 종류만 공유하기",
                handoffScenario =
                    "가족이나 곁의 보호자에게 지금 상황과 필요한 도움을 알리려는 때",
                privacyActionSentence =
                    "사람이나 비밀값 대신 돈·계정·링크·기기 중 어떤 행동을 멈췄는지만 말하겠습니다.",
                whyItMatters =
                    "행동 범주와 다음 역할만 공유해도 함께 대응할 수 있고 가족 확인 비밀과 연락값의 재노출을 줄일 수 있습니다.",
                doNotAttachRecordOrShareBoundaries = listOf(
                    "가족 이름·안전단어 답·콜백 문장·연락처·번호·받는 사람 이름을 기록하거나 공유하지 마세요.",
                    "가족에게 보냈는지, 누가 읽었는지, 무엇을 맡았는지 체크하거나 저장하지 마세요."
                ),
                primaryAction = action(EvidenceHandoffPrivacyActionId.FAMILY_ALERT_MESSAGES),
                secondaryActions = listOf(
                    action(EvidenceHandoffPrivacyActionId.TRUSTED_HELPER_REVIEW),
                    action(EvidenceHandoffPrivacyActionId.FAMILY_CALLBACK_CARD),
                    action(EvidenceHandoffPrivacyActionId.SAFE_CALL_SCRIPTS),
                    action(
                        EvidenceHandoffPrivacyActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(
                        EvidenceHandoffPrivacyActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    ),
                    action(EvidenceHandoffPrivacyActionId.FAMILY_READINESS_ROADMAP)
                )
            ),
            EvidenceHandoffPrivacyCard(
                id = EvidenceHandoffPrivacyCardId.OFFICIAL_CONSULTATION_PREP,
                displayOrder =
                    EvidenceHandoffPrivacyCardId.OFFICIAL_CONSULTATION_PREP.displayOrder,
                title = "기관·상담 준비: 원본은 내가 보관하고 말할 내용만 정리하기",
                handoffScenario =
                    "사용자가 직접 고른 신고·상담 창구에 어떤 일이 있었는지 설명하려는 때",
                privacyActionSentence =
                    "원본은 제가 보관하고, 행동 종류와 지금 필요한 도움만 말하겠습니다.",
                whyItMatters =
                    "상담 준비와 실제 제출은 다르므로 먼저 값 없는 설명을 만들고 필요한 원본 범위는 해당 창구에서 직접 확인해야 합니다.",
                doNotAttachRecordOrShareBoundaries = listOf(
                    "기관 이름·담당자 이름·정확한 번호·접수값·사건 원문·통화 기록·메시지 내용을 이 카드에 적지 마세요.",
                    "이 코치가 기관을 조회하거나 증거를 제출·접수·전송했다는 상태를 기록하지 마세요."
                ),
                primaryAction = action(EvidenceHandoffPrivacyActionId.OFFICIAL_HELP_DIRECTORY),
                secondaryActions = listOf(
                    action(EvidenceHandoffPrivacyActionId.REPORT_PREP),
                    action(EvidenceHandoffPrivacyActionId.EVIDENCE_KIT),
                    action(EvidenceHandoffPrivacyActionId.INCIDENT_TIMELINE_BRIEFING),
                    action(EvidenceHandoffPrivacyActionId.FAMILY_CALM_DEBRIEF_AGENDA)
                )
            ),
            EvidenceHandoffPrivacyCard(
                id = EvidenceHandoffPrivacyCardId.MONEY_ACCOUNT_CREDENTIALS,
                displayOrder =
                    EvidenceHandoffPrivacyCardId.MONEY_ACCOUNT_CREDENTIALS.displayOrder,
                title = "돈·계정·OTP 노출 증거: 값은 말하지 않고 보호 경로로 이동",
                handoffScenario =
                    "송금·결제·계정·카드·인증 비밀이 보이는 증거를 전달하려는 때",
                privacyActionSentence =
                    "금액과 비밀값은 말하지 않고 돈 행동을 멈춘 뒤 계정 보호와 증거 경로를 고르겠습니다.",
                whyItMatters =
                    "도움을 구하는 전달 과정에서도 금융·인증 비밀이 다시 노출될 수 있어 값 설명보다 멈춤과 별도 보호가 먼저입니다.",
                doNotAttachRecordOrShareBoundaries = listOf(
                    "금액·계좌·카드·OTP·PIN·비밀번호·거래값·금융 화면·인증 화면을 첨부·입력·공유하지 마세요.",
                    "이 카드가 송금·결제를 취소하거나 계정·카드를 정지·동결하거나 환급·회수를 처리한 것으로 기록하지 마세요."
                ),
                primaryAction = action(EvidenceHandoffPrivacyActionId.MONEY_TRANSFER_PAUSE_GATE),
                secondaryActions = listOf(
                    action(EvidenceHandoffPrivacyActionId.ACCOUNT_LOCKDOWN),
                    action(EvidenceHandoffPrivacyActionId.REMOTE_ACCESS_CUTOFF_COACH),
                    action(EvidenceHandoffPrivacyActionId.EVIDENCE_KIT),
                    action(EvidenceHandoffPrivacyActionId.RECOVERY_PLAN),
                    action(EvidenceHandoffPrivacyActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                    action(EvidenceHandoffPrivacyActionId.QUICK_STOP)
                )
            ),
            EvidenceHandoffPrivacyCard(
                id = EvidenceHandoffPrivacyCardId.LINK_APP_REMOTE_DETAILS,
                displayOrder = EvidenceHandoffPrivacyCardId.LINK_APP_REMOTE_DETAILS.displayOrder,
                title = "링크·앱·원격지시 증거: URL·앱·기기값 퍼뜨리지 않기",
                handoffScenario =
                    "링크·설치 지시·화면공유·원격 조작이 보이는 증거를 설명하려는 때",
                privacyActionSentence =
                    "정확한 링크와 앱·기기값은 보내지 않고 노출 행동 종류만 말하겠습니다.",
                whyItMatters =
                    "정확한 주소나 기기 세부정보를 다시 열거나 전달하면 다른 가족에게도 노출 경로가 이어질 수 있습니다.",
                doNotAttachRecordOrShareBoundaries = listOf(
                    "URL·도메인·앱 이름·패키지 이름·기기 식별값·설치 목록·파일 이름·화면공유 상태를 적거나 공유하지 마세요.",
                    "이 코치가 링크를 검사하거나 앱을 판정하거나 원격 연결을 끊거나 화면을 검토한 것으로 기록하지 마세요."
                ),
                primaryAction = action(EvidenceHandoffPrivacyActionId.REMOTE_ACCESS_CUTOFF_COACH),
                secondaryActions = listOf(
                    action(EvidenceHandoffPrivacyActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(EvidenceHandoffPrivacyActionId.SAFE_PASTE_PRIVACY_COACH),
                    action(EvidenceHandoffPrivacyActionId.TRUSTED_HELPER_REVIEW),
                    action(EvidenceHandoffPrivacyActionId.ACCOUNT_LOCKDOWN),
                    action(EvidenceHandoffPrivacyActionId.EVIDENCE_KIT),
                    action(EvidenceHandoffPrivacyActionId.PRIVACY_CENTER)
                )
            ),
            EvidenceHandoffPrivacyCard(
                id = EvidenceHandoffPrivacyCardId.FOLLOW_UP_WITHOUT_HANDOFF_LOG,
                displayOrder =
                    EvidenceHandoffPrivacyCardId.FOLLOW_UP_WITHOUT_HANDOFF_LOG.displayOrder,
                title = "후속 점검: 전달 기록을 남기지 않고 가족과 다시 보기",
                handoffScenario =
                    "가족과 이후 행동을 다시 살피거나 다른 도움 경로를 고르려는 때",
                privacyActionSentence =
                    "누구에게 무엇을 보냈는지는 적지 않고, 다음에 볼 행동 종류만 가족과 정하겠습니다.",
                whyItMatters =
                    "받는 사람·전달물·전달 결과를 새로 저장하지 않아도 기존 후속 화면에서 필요한 행동 범주를 다시 볼 수 있습니다.",
                doNotAttachRecordOrShareBoundaries = listOf(
                    "받는 사람 이름·연락처·전달한 파일·미디어 이름·경로·날짜·읽음·전송·접수 결과를 기록하지 마세요.",
                    "카드 선택·체크 표시·완료·담당자·메모·전달 상태·이력을 저장하지 마세요."
                ),
                primaryAction = action(
                    EvidenceHandoffPrivacyActionId.FOLLOW_UP_CHECK_IN_PLANNER
                ),
                secondaryActions = listOf(
                    action(EvidenceHandoffPrivacyActionId.FAMILY_CALM_DEBRIEF_AGENDA),
                    action(EvidenceHandoffPrivacyActionId.SAFETY_FLOW_ROUTER),
                    action(EvidenceHandoffPrivacyActionId.INCIDENT_TIMELINE_BRIEFING)
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
                    "현재 분석이 없거나 일치 신호가 없습니다. 이를 증거에 문제가 없거나 전달 준비가 " +
                        "끝났다는 뜻으로 보지 말고 모든 카드를 보수적으로 직접 읽으세요."
                )
            }
            if (hasHighRisk) {
                add(
                    "위험도가 높아 원본 보관과 값 없는 가족·계정·증거·도움 경로를 함께 권합니다. " +
                        "증거 내용이나 전달 결과를 판정하는 뜻은 아닙니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 긴급상황·목소리 합성·번호 변경 신호가 있어 가족 확인 비밀 없이 행동 " +
                        "종류만 공유하고 값 없는 확인 전화·가족 경고·보호자 검토·별도 경로·결과별 " +
                        "다음 단계를 권합니다."
                )
            }
            if (hasSecrecyImpersonationSignal) {
                add(
                    "비밀 유지·기관 사칭 신호가 있어 증거 전달을 혼자 숨기지 않고 가족 경고·차분 " +
                        "대화·증거 보존·사용자가 직접 고를 도움·신고 준비·별도 경로를 권합니다."
                )
            }
            if (hasFinancialSignal) {
                add(
                    "송금·결제·금융 압박 신호가 있어 돈 행동을 멈추고 계정 보호·증거 보존·사용자가 " +
                        "직접 고를 도움·신고 준비·수습·후속 점검 경로를 권합니다. 결제 취소·계정이나 " +
                        "카드 정지·동결·환급·회수·피해 복구를 대신하거나 보장하지 않습니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증 비밀 노출 가능성이 있어 계정 보호와 원격지시 중단 경로를 권합니다. " +
                        "비밀값을 지우거나 계정·카드를 자동 정지·동결하는 기능은 아닙니다."
                )
            }
            if (hasLinkSignal) {
                add(
                    "링크·스미싱 신호가 있어 검사기·노출 분류·개인정보·붙여넣기 전 가리기·증거 " +
                        "경로를 권합니다. 링크를 스캔하거나 URL·도메인을 검증하지 않습니다."
                )
            }
            if (hasRemoteAccessSignal) {
                add(
                    "앱 설치·화면공유·원격지시 신호가 있어 원격지시 중단·보호자·계정·증거 경로를 " +
                        "권합니다. 화면이나 설치 목록을 검토하지 않고 원격 연결 해제·악성 프로그램 " +
                        "제거·기기 정리를 수행하거나 보장하지 않습니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만 반영하며 사건 내용은 읽지 않습니다. 증거 보존·" +
                        "사건 타임라인·신고 준비·후속 점검의 기존 경로만 함께 권합니다."
                )
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW ->
                "가족 준비가 부족한 상태라 값 없는 확인 전화·가족 경고·보호자 검토·준비 로드맵을 함께 권합니다. 실제 가족 값은 입력하지 마세요."
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중이라 값 없는 확인 전화·가족 경고·보호자 검토·준비 로드맵으로 빠진 부분을 직접 살피도록 권합니다. 저장된 실제 값은 추천에 사용하지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있어도 이번 증거 내용·받는 사람·전달 경로·전달 결과를 확인했다는 뜻은 아닙니다. 원본 보관과 값 없는 설명을 계속 분리하세요."
        }
        return listOf(situationCopy, readinessCopy).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 사용자가 화면을 직접 읽고 직접 선택하는 로컬 전용·무상태 수동 안내이며 증거·받는 사람·전달 결과 입력을 요구하지 않습니다.",
        "추천은 위험도·일치 신호, 값 없는 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·사건 내용·가족 안전 계획 값을 사용하지 않습니다.",
        "이름·연락처·정확한 전화번호·계정·계좌·카드·OTP·PIN·비밀번호·가족 확인 답·주민번호·기기 식별자·정확한 URL·도메인·공식 번호·앱 이름·패키지 이름·파일·미디어 이름·경로·받는 사람·전달 결과값을 묻거나 저장하지 않습니다.",
        "카드 선택·추천·읽음·공유·체크 표시·메모·담당자·받는 사람·첨부 상태·보낸 상태·전달 상태·접수 상태·진행·이력을 위한 새 저장소나 증거 전달 저장소를 만들거나 기록하지 않습니다.",
        "파일·사진·이미지·미디어·저장소를 읽지 않고 카메라를 사용하거나 화면을 캡처하지 않으며 스크린샷을 가져오거나 OCR·이미지 분석을 하지 않습니다.",
        "파일·사진·이미지·미디어를 첨부하거나 업로드하지 않고 스트림·URI로 공유하지 않으며 파일·문서·사진 선택기를 열지 않습니다.",
        "연락처·통화 기록·문자·메시지·통화 상태·알림 내용을 읽거나 감시하지 않고 연락처를 읽거나 쓰거나 고르지 않습니다.",
        "전화 앱을 열거나 자동 다이얼·발신·통화·종료를 하지 않고 통화를 선별·차단하지 않으며 문자·SMS를 보내거나 읽지 않습니다.",
        "받는 사람을 고르거나 공유를 자동 전송하지 않고 알림 리스너·알림 모니터링·알림 필터링을 사용하지 않습니다.",
        "클립보드를 읽거나 쓰지 않고 원문·번호·링크·파일 경로·전달 결과를 복사하거나 붙여넣지 않습니다.",
        "네트워크·웹 탐색·지도·위치·기관 번호 조회·공식 경로 조회·전화번호 검증·링크·URL·도메인 검증·링크 스캔을 사용하지 않습니다.",
        "새 Android 권한을 요청하지 않습니다.",
        "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커를 만들거나 예약하지 않으며 스케줄러를 사용하지 않습니다.",
        "결제·은행 기능이나 API, NFC나 Android 계정 관리자에 접근하지 않고 송금·결제·취소·계정 또는 카드 정지·동결·환급·피해 복구를 자동 수행하지 않습니다.",
        "기기 관리자·접근성 서비스·VPN·사용 기록·앱 작업 권한이나 기기 제어 기능을 사용하지 않고 설치 앱·패키지 목록을 조회·설치·제거하거나 설정을 바꾸지 않습니다.",
        "붙여넣은 글, 화면·파일·사진·미디어, 통화 상태·기록, 연락처, 문자, 알림, 사건 기록, 가족 안전 계획, 기관 목록, 계정·카드·은행·기기·앱·패키지·네트워크·VPN·알림 설정, 돈의 움직임, 확인 결과·재연락 상태·증거 전달 상태를 추가·수정·삭제하지 않습니다.",
        "원문·화면·파일·사진·미디어·연락·번호·링크·기기·계정·증거를 자동으로 가리거나 모니터링·스캔·OCR·삭제·차단·호출·메시지 전송·조회·확인·검증·저장·업로드·제출·첨부하지 않습니다.",
        "이 안내는 증거의 상태나 내용, 사람·번호·링크·기관, 제출·접수 결과, 악성 프로그램 제거, 원격 연결 종료, 기기 정리, 계정·카드 조치, 결제 취소·환급·회복 또는 법률·금융 결과를 증명하거나 보장하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<EvidenceHandoffPrivacyCardId>,
        recommendedActions: List<EvidenceHandoffPrivacyAction>,
        cards: List<EvidenceHandoffPrivacyCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 증거 전달 개인정보 코치")
            appendLine()
            appendLine("증거를 보내기 전에 값 없는 전달 경계와 기존 앱 경로를 함께 보고 있습니다.")
            appendLine()
            appendLine("추천 전달 경계 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("가족에게 말할 값 없는 문장")
            appendLine("- 원본은 내가 따로 보관하고, 필요한 행동 종류와 다음 앱 경로만 함께 고르겠습니다.")
            append(
                "원문·민감값·사람·연락처·번호·링크·계정·카드·인증·기기·가족 계획·" +
                    "파일·경로·받는 사람·전달 결과 없이 고정된 카드와 기존 앱 경로 이름만 담았습니다."
            )
        }.trim()
    }

    private fun actionFor(
        id: EvidenceHandoffPrivacyActionId
    ): EvidenceHandoffPrivacyAction = EvidenceHandoffPrivacyAction(
        id = id,
        label = when (id) {
            EvidenceHandoffPrivacyActionId.QUICK_STOP -> "긴급 멈춤 모드"
            EvidenceHandoffPrivacyActionId.CHECKER -> "의심 내용 검사"
            EvidenceHandoffPrivacyActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            EvidenceHandoffPrivacyActionId.SAFE_PASTE_PRIVACY_COACH ->
                "붙여넣기 전 개인정보 가리기"
            EvidenceHandoffPrivacyActionId.PRIVACY_CENTER -> "개인정보 센터"
            EvidenceHandoffPrivacyActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
            EvidenceHandoffPrivacyActionId.FAMILY_CALLBACK_CARD -> "평소 가족 경로 콜백 카드"
            EvidenceHandoffPrivacyActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                "별도 경로 확인 코치"
            EvidenceHandoffPrivacyActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                "확인 결과별 다음 단계 보드"
            EvidenceHandoffPrivacyActionId.SAFE_CALL_SCRIPTS ->
                "값 없는 가족 콜백·확인 전화 스크립트"
            EvidenceHandoffPrivacyActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
            EvidenceHandoffPrivacyActionId.TRUSTED_HELPER_REVIEW ->
                "보호자와 함께 안전 검토"
            EvidenceHandoffPrivacyActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
            EvidenceHandoffPrivacyActionId.MONEY_TRANSFER_PAUSE_GATE -> "송금 전 멈춤 확인"
            EvidenceHandoffPrivacyActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
            EvidenceHandoffPrivacyActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                "화면공유·원격지시 끊기 코치"
            EvidenceHandoffPrivacyActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            EvidenceHandoffPrivacyActionId.INCIDENT_TIMELINE_BRIEFING -> "사건 타임라인 요약"
            EvidenceHandoffPrivacyActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처"
            EvidenceHandoffPrivacyActionId.REPORT_PREP -> "신고용 상담 준비 카드"
            EvidenceHandoffPrivacyActionId.RECOVERY_PLAN -> "24시간 수습 계획"
            EvidenceHandoffPrivacyActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                "며칠 뒤 다시 확인할 것"
            EvidenceHandoffPrivacyActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                "가족 차분 대화 안건"
            EvidenceHandoffPrivacyActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
