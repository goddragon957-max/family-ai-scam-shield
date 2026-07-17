package com.mongmong.familyshield

enum class CredentialSecretFreezeCardId(val displayOrder: Int) {
    DO_NOT_SAY_TYPE_PASTE_OR_FORWARD(1),
    LEAVE_REQUESTING_SESSION(2),
    PAUSE_MONEY_ACCOUNT_AND_DEVICE_ACTIONS(3),
    SHARE_ACTION_CATEGORIES_ONLY(4),
    PREPARE_MANUAL_OFFICIAL_ACTION(5),
    REVIEW_WITHOUT_SECRET_INPUT(6)
}

enum class CredentialSecretFreezeActionId {
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
    FAMILY_ALERT_MESSAGES,
    VERIFICATION_OUTCOME_NEXT_STEPS_BOARD,
    RECONTACT_BOUNDARY_COACH,
    TRUSTED_HELPER_REVIEW,
    FAMILY_RESPONSE_ROLE_BOARD,
    FAMILY_CALM_DEBRIEF_AGENDA,
    MONEY_TRANSFER_PAUSE_GATE,
    ACCOUNT_LOCKDOWN,
    REMOTE_ACCESS_CUTOFF_COACH,
    EXPOSURE_IMPACT_TRIAGE,
    PRIVACY_CENTER,
    EVIDENCE_HANDOFF_PRIVACY_COACH,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    FOLLOW_UP_CHECK_IN_PLANNER,
    SAFETY_FLOW_ROUTER
}

data class CredentialSecretFreezeAction(
    val id: CredentialSecretFreezeActionId,
    val label: String
)

data class CredentialSecretFreezeCard(
    val id: CredentialSecretFreezeCardId,
    val displayOrder: Int,
    val title: String,
    val freezeScenario: String,
    val valueFreeActionSentence: String,
    val whyItMatters: String,
    val doNotEnterShareClipboardOrAutofillBoundaries: List<String>,
    val primaryAction: CredentialSecretFreezeAction,
    val secondaryActions: List<CredentialSecretFreezeAction>
) {
    val routeActions: List<CredentialSecretFreezeAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class CredentialSecretFreezeCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<CredentialSecretFreezeCardId>,
    val recommendedActions: List<CredentialSecretFreezeAction>,
    val cards: List<CredentialSecretFreezeCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<CredentialSecretFreezeCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<CredentialSecretFreezeActionId>
        get() = recommendedActions.map { action -> action.id }
}

object CredentialSecretFreezeCoachBuilder {
    private val credentialSignals = setOf(
        ScamSignal.CREDENTIALS,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
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

    private val linkSignals = setOf(
        ScamSignal.LINK,
        ScamSignal.PARCEL_INVITATION_SMISHING
    )

    private val remoteAccessSignals = setOf(
        ScamSignal.APP_INSTALL,
        ScamSignal.LIVE_CALL_CONTROL
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
    ): CredentialSecretFreezeCoach {
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val hasNoAnalysisOrSignals = analysis == null || signals.isEmpty()
        val hasHighRisk = analysis?.riskLevel == RiskLevel.HIGH
        val hasCredentialSignal = signals.any { signal -> signal in credentialSignals }
        val hasFinancialSignal = signals.any { signal -> signal in financialSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasRemoteAccessSignal = signals.any { signal -> signal in remoteAccessSignals }
        val hasFamilyVerificationSignal = signals.any { signal ->
            signal in familyVerificationSignals
        }
        val hasSecrecyImpersonationSignal = signals.any { signal ->
            signal in secrecyImpersonationSignals
        }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0

        val recommendedCardSet = linkedSetOf(
            CredentialSecretFreezeCardId.DO_NOT_SAY_TYPE_PASTE_OR_FORWARD,
            CredentialSecretFreezeCardId.LEAVE_REQUESTING_SESSION
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += CredentialSecretFreezeCardId.entries
        }
        if (hasCredentialSignal) {
            recommendedCardSet +=
                CredentialSecretFreezeCardId.DO_NOT_SAY_TYPE_PASTE_OR_FORWARD
            recommendedCardSet += CredentialSecretFreezeCardId.LEAVE_REQUESTING_SESSION
            recommendedCardSet +=
                CredentialSecretFreezeCardId.PAUSE_MONEY_ACCOUNT_AND_DEVICE_ACTIONS
            recommendedCardSet += CredentialSecretFreezeCardId.SHARE_ACTION_CATEGORIES_ONLY
            recommendedCardSet += CredentialSecretFreezeCardId.PREPARE_MANUAL_OFFICIAL_ACTION
        }
        if (hasFinancialSignal) {
            recommendedCardSet += CredentialSecretFreezeCardId.LEAVE_REQUESTING_SESSION
            recommendedCardSet +=
                CredentialSecretFreezeCardId.PAUSE_MONEY_ACCOUNT_AND_DEVICE_ACTIONS
            recommendedCardSet += CredentialSecretFreezeCardId.PREPARE_MANUAL_OFFICIAL_ACTION
        }
        if (hasLinkSignal || hasRemoteAccessSignal) {
            recommendedCardSet +=
                CredentialSecretFreezeCardId.DO_NOT_SAY_TYPE_PASTE_OR_FORWARD
            recommendedCardSet += CredentialSecretFreezeCardId.LEAVE_REQUESTING_SESSION
            recommendedCardSet += CredentialSecretFreezeCardId.SHARE_ACTION_CATEGORIES_ONLY
        }
        if (hasFamilyVerificationSignal || hasSecrecyImpersonationSignal) {
            recommendedCardSet +=
                CredentialSecretFreezeCardId.DO_NOT_SAY_TYPE_PASTE_OR_FORWARD
            recommendedCardSet += CredentialSecretFreezeCardId.LEAVE_REQUESTING_SESSION
            recommendedCardSet += CredentialSecretFreezeCardId.SHARE_ACTION_CATEGORIES_ONLY
        }
        if (hasSavedIncident) {
            recommendedCardSet += CredentialSecretFreezeCardId.SHARE_ACTION_CATEGORIES_ONLY
            recommendedCardSet += CredentialSecretFreezeCardId.REVIEW_WITHOUT_SECRET_INPUT
        }
        when (readiness.level) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedCardSet += CredentialSecretFreezeCardId.SHARE_ACTION_CATEGORIES_ONLY
                recommendedCardSet += CredentialSecretFreezeCardId.REVIEW_WITHOUT_SECRET_INPUT
            }
            ReadinessLevel.READY -> {
                recommendedCardSet +=
                    CredentialSecretFreezeCardId.DO_NOT_SAY_TYPE_PASTE_OR_FORWARD
                recommendedCardSet += CredentialSecretFreezeCardId.REVIEW_WITHOUT_SECRET_INPUT
            }
        }
        val recommendedCardIds = CredentialSecretFreezeCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            CredentialSecretFreezeActionId.CHECKER
        } else {
            CredentialSecretFreezeActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            currentResultActionId,
            CredentialSecretFreezeActionId.QUICK_STOP,
            CredentialSecretFreezeActionId.SAFE_PASTE_PRIVACY_COACH,
            CredentialSecretFreezeActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
            CredentialSecretFreezeActionId.PRIVACY_CENTER,
            CredentialSecretFreezeActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            CredentialSecretFreezeActionId.entries
                .filterNot { actionId ->
                    actionId == CredentialSecretFreezeActionId.ANALYSIS_RESULT
                }
                .forEach { actionId -> recommendedActionSet += actionId }
        }
        if (hasHighRisk) {
            CredentialSecretFreezeActionId.entries.forEach { actionId ->
                recommendedActionSet += actionId
            }
        }
        if (hasCredentialSignal) {
            recommendedActionSet += CredentialSecretFreezeActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                CredentialSecretFreezeActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += CredentialSecretFreezeActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet +=
                CredentialSecretFreezeActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += CredentialSecretFreezeActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += CredentialSecretFreezeActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += CredentialSecretFreezeActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet += CredentialSecretFreezeActionId.MONEY_TRANSFER_PAUSE_GATE
        }
        if (hasFinancialSignal) {
            recommendedActionSet +=
                CredentialSecretFreezeActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += CredentialSecretFreezeActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                CredentialSecretFreezeActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += CredentialSecretFreezeActionId.EXPOSURE_IMPACT_TRIAGE
        }
        if (hasLinkSignal) {
            recommendedActionSet +=
                CredentialSecretFreezeActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                CredentialSecretFreezeActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += CredentialSecretFreezeActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += CredentialSecretFreezeActionId.PRIVACY_CENTER
            recommendedActionSet += CredentialSecretFreezeActionId.SAFE_PASTE_PRIVACY_COACH
        }
        if (hasRemoteAccessSignal) {
            recommendedActionSet +=
                CredentialSecretFreezeActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                CredentialSecretFreezeActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += CredentialSecretFreezeActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += CredentialSecretFreezeActionId.PRIVACY_CENTER
            recommendedActionSet += CredentialSecretFreezeActionId.TRUSTED_HELPER_REVIEW
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet +=
                CredentialSecretFreezeActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += CredentialSecretFreezeActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet +=
                CredentialSecretFreezeActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH
            recommendedActionSet +=
                CredentialSecretFreezeActionId.NEW_NUMBER_QUARANTINE_COACH
            recommendedActionSet +=
                CredentialSecretFreezeActionId.FAMILY_SAFE_WORD_RESET_COACH
            recommendedActionSet += CredentialSecretFreezeActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += CredentialSecretFreezeActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet +=
                CredentialSecretFreezeActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedActionSet += CredentialSecretFreezeActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet +=
                CredentialSecretFreezeActionId.FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet += CredentialSecretFreezeActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                CredentialSecretFreezeActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += CredentialSecretFreezeActionId.PRIVACY_CENTER
            recommendedActionSet += CredentialSecretFreezeActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += CredentialSecretFreezeActionId.REPORT_PREP
        }
        if (hasSavedIncident) {
            recommendedActionSet +=
                CredentialSecretFreezeActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += CredentialSecretFreezeActionId.EVIDENCE_KIT
            recommendedActionSet +=
                CredentialSecretFreezeActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        when (readiness.level) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedActionSet += CredentialSecretFreezeActionId.SAFETY_PLAN
                recommendedActionSet +=
                    CredentialSecretFreezeActionId.FAMILY_SAFE_WORD_RESET_COACH
                recommendedActionSet +=
                    CredentialSecretFreezeActionId.FAMILY_READINESS_ROADMAP
                recommendedActionSet +=
                    CredentialSecretFreezeActionId.FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += CredentialSecretFreezeActionId.SAFE_CALL_SCRIPTS
            }
            ReadinessLevel.READY -> {
                recommendedActionSet += CredentialSecretFreezeActionId.FAMILY_CALLBACK_CARD
                recommendedActionSet +=
                    CredentialSecretFreezeActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                recommendedActionSet += CredentialSecretFreezeActionId.SAFE_CALL_SCRIPTS
                recommendedActionSet +=
                    CredentialSecretFreezeActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
            }
        }
        val recommendedActions = CredentialSecretFreezeActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null -> "검사 결과가 없어도 모든 비밀값 멈춤 카드를 보수적으로 읽기"
            hasCredentialSignal -> "비밀값을 말하거나 입력하기 전에 모든 전달 행동 멈추기"
            hasRemoteAccessSignal ->
                "같은 앱·통화·화면공유 흐름에서 비밀값 행동을 이어가지 않기"
            hasLinkSignal -> "상대가 제시한 화면·링크 밖으로 나와 비밀값 행동 멈추기"
            hasFinancialSignal -> "돈·계정 행동보다 비밀값 멈춤을 먼저 두기"
            hasFamilyVerificationSignal ->
                "가족을 내세운 요청에도 비밀값 없이 별도 가족 경로 고르기"
            hasSecrecyImpersonationSignal ->
                "요청을 숨기지 말고 비밀값 대신 행동 종류만 알리기"
            hasHighRisk -> "높은 위험 신호에서 비밀값 없는 대응 경로를 넓게 고르기"
            hasSavedIncident -> "저장 사건 내용 없이 멈춤과 후속 경로 고르기"
            else -> "비밀값 없이 멈추고 기존 대응 경로 고르기"
        }

        return CredentialSecretFreezeCoach(
            title = "자격 비밀값 멈춤 코치",
            intro =
                "인증번호·PIN·비밀번호·보안 답·상품권 비밀값을 말하거나 입력·붙여넣기·" +
                    "캡처·전달·재사용하기 전에 멈추고, 값 없이 읽는 고정 카드에서 기존 대응 " +
                    "화면만 직접 고르는 로컬 전용·무상태 수동 안내입니다.",
            modeLabel = "비밀값을 보지 않고 · 전달 전에 멈춤",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                readinessLevel = readiness.level,
                hasNoAnalysisOrSignals = hasNoAnalysisOrSignals,
                hasHighRisk = hasHighRisk,
                hasCredentialSignal = hasCredentialSignal,
                hasFinancialSignal = hasFinancialSignal,
                hasLinkSignal = hasLinkSignal,
                hasRemoteAccessSignal = hasRemoteAccessSignal,
                hasFamilyVerificationSignal = hasFamilyVerificationSignal,
                hasSecrecyImpersonationSignal = hasSecrecyImpersonationSignal,
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

    private fun buildCards(): List<CredentialSecretFreezeCard> {
        fun action(id: CredentialSecretFreezeActionId): CredentialSecretFreezeAction =
            actionFor(id)

        return listOf(
            CredentialSecretFreezeCard(
                id = CredentialSecretFreezeCardId.DO_NOT_SAY_TYPE_PASTE_OR_FORWARD,
                displayOrder =
                    CredentialSecretFreezeCardId.DO_NOT_SAY_TYPE_PASTE_OR_FORWARD.displayOrder,
                title = "OTP·PIN·비밀번호를 말하거나 붙여넣지 않기",
                freezeScenario =
                    "보호·본인 확인·환급·송금 처리를 이유로 인증값이나 계정 비밀값을 요구하는 때",
                valueFreeActionSentence =
                    "어떤 비밀값도 말하거나 타이핑·붙여넣기·캡처·전달·재사용하지 않겠습니다.",
                whyItMatters =
                    "비밀값은 한 번 전달되면 상대가 다른 계정·돈·기기 행동으로 요구를 넓힐 수 있습니다.",
                doNotEnterShareClipboardOrAutofillBoundaries = listOf(
                    "실제 인증번호·PIN·비밀번호·보안 답·상품권 비밀값을 이 코치에 적지 마세요.",
                    "클립보드·자동완성·자격 증명·계정 로그인 상태를 읽거나 쓰지 않으며 복사·붙여넣기·자동 입력·제출하지 않습니다."
                ),
                primaryAction = action(CredentialSecretFreezeActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(CredentialSecretFreezeActionId.SAFE_PASTE_PRIVACY_COACH),
                    action(CredentialSecretFreezeActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(CredentialSecretFreezeActionId.FAMILY_ALERT_MESSAGES)
                )
            ),
            CredentialSecretFreezeCard(
                id = CredentialSecretFreezeCardId.LEAVE_REQUESTING_SESSION,
                displayOrder = CredentialSecretFreezeCardId.LEAVE_REQUESTING_SESSION.displayOrder,
                title = "상대가 제시한 화면·링크·앱 안에서 인증을 이어가지 않기",
                freezeScenario =
                    "같은 채팅·링크·앱·통화·화면공유에서 코드를 읽거나 입력하라고 재촉하는 때",
                valueFreeActionSentence =
                    "현재 흐름을 벗어나고 그 화면·링크·앱·통화 안에서는 추가 행동을 하지 않겠습니다.",
                whyItMatters =
                    "상대가 만든 흐름 안에서 계속 확인하면 입력 위치와 다음 행동까지 상대가 통제할 수 있습니다.",
                doNotEnterShareClipboardOrAutofillBoundaries = listOf(
                    "링크·도메인·앱·패키지·기기·통화·화면공유 값을 이 코치에 적지 마세요.",
                    "URL·앱·기기·연결 상태를 검사하지 않고 브라우저·네트워크·화면 캡처·알림·통화 상태를 사용하지 않습니다."
                ),
                primaryAction = action(
                    CredentialSecretFreezeActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
                ),
                secondaryActions = listOf(
                    action(CredentialSecretFreezeActionId.REMOTE_ACCESS_CUTOFF_COACH),
                    action(CredentialSecretFreezeActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(CredentialSecretFreezeActionId.PRIVACY_CENTER)
                )
            ),
            CredentialSecretFreezeCard(
                id = CredentialSecretFreezeCardId.PAUSE_MONEY_ACCOUNT_AND_DEVICE_ACTIONS,
                displayOrder =
                    CredentialSecretFreezeCardId.PAUSE_MONEY_ACCOUNT_AND_DEVICE_ACTIONS.displayOrder,
                title = "돈·계정·기기 요구를 먼저 멈추기",
                freezeScenario =
                    "비밀값 요구가 송금·결제·계정 보호·앱 설치·원격 조작과 함께 이어지는 때",
                valueFreeActionSentence =
                    "돈·계정·기기 행동을 멈추고 값 없는 기존 점검 경로만 직접 고르겠습니다.",
                whyItMatters =
                    "비밀값 요구와 돈·계정·기기 지시가 함께 나오면 피해 경로가 동시에 넓어질 수 있습니다.",
                doNotEnterShareClipboardOrAutofillBoundaries = listOf(
                    "계좌·카드·은행·로그인·인증·기기 식별값이나 결제·복구 상태를 입력하지 마세요.",
                    "송금·결제·계정·카드·은행·앱·기기 상태를 읽거나 바꾸지 않고 자동 취소·정지·환급·복구를 하지 않습니다."
                ),
                primaryAction = action(
                    CredentialSecretFreezeActionId.MONEY_TRANSFER_PAUSE_GATE
                ),
                secondaryActions = listOf(
                    action(CredentialSecretFreezeActionId.ACCOUNT_LOCKDOWN),
                    action(CredentialSecretFreezeActionId.REMOTE_ACCESS_CUTOFF_COACH),
                    action(CredentialSecretFreezeActionId.EXPOSURE_IMPACT_TRIAGE)
                )
            ),
            CredentialSecretFreezeCard(
                id = CredentialSecretFreezeCardId.SHARE_ACTION_CATEGORIES_ONLY,
                displayOrder =
                    CredentialSecretFreezeCardId.SHARE_ACTION_CATEGORIES_ONLY.displayOrder,
                title = "가족·보호자에게는 비밀값 대신 행동 종류만 알리기",
                freezeScenario =
                    "누구에게도 알리지 말라는 압박이나 가족·기관을 사칭한 비밀값 요청을 받은 때",
                valueFreeActionSentence =
                    "가족·보호자에게는 값 대신 비밀값 요구·돈 요구·앱 요구 같은 행동 종류만 알리겠습니다.",
                whyItMatters =
                    "비밀을 유지하라는 압박을 끊으면서도 민감한 값을 다시 노출하지 않고 도움을 구할 수 있습니다.",
                doNotEnterShareClipboardOrAutofillBoundaries = listOf(
                    "사람 이름·연락처·번호·받는 사람·안전단어·콜백 문장·비밀값을 적거나 공유하지 마세요.",
                    "수동 문자 공유에는 고정 카드·기존 앱 경로·행동 종류만 넣고 파일·첨부·스트림·URI를 넣지 않습니다."
                ),
                primaryAction = action(CredentialSecretFreezeActionId.FAMILY_ALERT_MESSAGES),
                secondaryActions = listOf(
                    action(CredentialSecretFreezeActionId.TRUSTED_HELPER_REVIEW),
                    action(CredentialSecretFreezeActionId.FAMILY_CALM_DEBRIEF_AGENDA),
                    action(CredentialSecretFreezeActionId.EVIDENCE_HANDOFF_PRIVACY_COACH)
                )
            ),
            CredentialSecretFreezeCard(
                id = CredentialSecretFreezeCardId.PREPARE_MANUAL_OFFICIAL_ACTION,
                displayOrder =
                    CredentialSecretFreezeCardId.PREPARE_MANUAL_OFFICIAL_ACTION.displayOrder,
                title = "노출됐을 수 있으면 별도 기기·공식 경로에서 직접 조치 준비하기",
                freezeScenario =
                    "비밀값을 이미 보였거나 말했을 가능성이 있어 후속 조치를 준비해야 하는 때",
                valueFreeActionSentence =
                    "현재 상대가 제시한 흐름 밖에서 공식 경로를 직접 찾고 필요한 행동 종류만 준비하겠습니다.",
                whyItMatters =
                    "이 코치가 상태를 판정하지 않아도 별도 경로에서 사람이 직접 다음 조치를 준비할 수 있습니다.",
                doNotEnterShareClipboardOrAutofillBoundaries = listOf(
                    "기관 번호·공식 목록 값·계정·카드·비밀값·노출 결과·조치 결과를 입력하지 마세요.",
                    "기관·번호·계정·코드·결제·복구 상태를 조회·검증하지 않고 로그인·비밀번호 변경·코드 검사를 수행하지 않습니다."
                ),
                primaryAction = action(CredentialSecretFreezeActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(CredentialSecretFreezeActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(CredentialSecretFreezeActionId.OFFICIAL_HELP_DIRECTORY),
                    action(CredentialSecretFreezeActionId.REPORT_PREP),
                    action(CredentialSecretFreezeActionId.EVIDENCE_HANDOFF_PRIVACY_COACH)
                )
            ),
            CredentialSecretFreezeCard(
                id = CredentialSecretFreezeCardId.REVIEW_WITHOUT_SECRET_INPUT,
                displayOrder =
                    CredentialSecretFreezeCardId.REVIEW_WITHOUT_SECRET_INPUT.displayOrder,
                title = "확인 뒤에도 비밀값을 이 앱에 적지 않고 기존 경로로 후속 점검하기",
                freezeScenario =
                    "가족 확인·공식 도움·계정·증거·후속 점검 화면을 이어서 살펴보려는 때",
                valueFreeActionSentence =
                    "비밀값과 결과값 없이 기존 화면을 직접 열어 행동 순서만 살펴보겠습니다.",
                whyItMatters =
                    "값 없는 라우팅은 같은 비밀을 여러 화면이나 공유 문장에 반복 노출하는 일을 줄입니다.",
                doNotEnterShareClipboardOrAutofillBoundaries = listOf(
                    "원문·가린 단서·사건 내용·계획 실제 값·확인 결과·복구 상태를 이 코치에 적지 마세요.",
                    "카드 선택·추천·읽음·공유·진행·결과를 저장하지 않고 계획·사건·계정·기기 상태를 바꾸지 않습니다."
                ),
                primaryAction = action(
                    CredentialSecretFreezeActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
                ),
                secondaryActions = listOf(
                    action(CredentialSecretFreezeActionId.EVIDENCE_KIT),
                    action(CredentialSecretFreezeActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                    action(CredentialSecretFreezeActionId.SAFETY_FLOW_ROUTER),
                    action(CredentialSecretFreezeActionId.PRIVACY_CENTER)
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        readinessLevel: ReadinessLevel,
        hasNoAnalysisOrSignals: Boolean,
        hasHighRisk: Boolean,
        hasCredentialSignal: Boolean,
        hasFinancialSignal: Boolean,
        hasLinkSignal: Boolean,
        hasRemoteAccessSignal: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasSecrecyImpersonationSignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (hasNoAnalysisOrSignals) {
                add(
                    "검사 결과나 일치 신호가 없어도 비밀값 요청의 상태를 단정하지 말고 모든 " +
                        "멈춤 카드를 보수적으로 읽도록 권합니다."
                )
            }
            if (hasHighRisk) {
                add(
                    "높은 위험 범주에서는 비밀값 없는 돈·계정·기기·가족·증거·개인정보 " +
                        "대응 경로를 넓게 함께 보여 줍니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증·계정·상품권 비밀값 요구 신호에서는 말하기·입력·붙여넣기·캡처·" +
                        "전달·재사용을 멈추고 계정 잠금·원격지시 중단·노출 분류·증거 전달 " +
                        "개인정보·보호자·가족 알림 경로를 권합니다."
                )
            }
            if (hasFinancialSignal) {
                add(
                    "송금·결제·안전계좌·대환 미끼 범주에서는 돈 멈춤·계정 잠금·원격지시 " +
                        "중단·노출 분류를 수동 지원 경로로 보여 주며 취소·동결·환급·회수나 " +
                        "복구 결과를 만들지 않습니다."
                )
            }
            if (hasLinkSignal || hasRemoteAccessSignal) {
                add(
                    "링크·문자 미끼·앱 설치·통화 유지 신호에서는 같은 링크·앱·통화·" +
                        "화면공유 흐름에서 인증을 이어가지 말고 종료 문구·원격지시 중단·노출 " +
                        "분류·개인정보 경로를 직접 고르도록 권합니다. URL 검사·앱 목록 조회·" +
                        "악성 프로그램 제거·원격 종료·기기 정리를 하지 않습니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 긴급·목소리 사칭·번호 변경 범주에서는 비밀값을 묻거나 공유하지 " +
                        "않고 별도 경로 확인·가족 콜백·목소리 현실 점검·새 번호 격리·안전단어 " +
                        "재정비·값 없는 전화 문구·가족 알림 경로를 직접 고르도록 권합니다."
                )
            }
            if (hasSecrecyImpersonationSignal) {
                add(
                    "비밀 유지·사칭 범주에서는 요청을 숨긴 채 이어가지 말고 행동 종류만 " +
                        "가족·보호자에게 알린 뒤 증거 전달 개인정보·차분 대화·도움·신고 준비 " +
                        "경로를 직접 고르도록 권합니다. 기관 번호나 도움 목록 값은 사용하지 않습니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만으로 증거 전달 개인정보·증거 체크·후속 " +
                        "점검 경로를 더 보여 주며 사건 내용이나 미리보기는 읽지 않습니다."
                )
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 아직 진행 중인 범주라 안전 계획·안전단어 재정비·준비 로드맵·" +
                    "가족 연습·값 없는 전화 문구 경로를 직접 살펴보도록 권합니다. 저장된 " +
                    "계획의 실제 값은 추천에 사용하지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있다는 범주는 기존 준비 항목이 채워졌다는 뜻일 뿐 어떤 " +
                    "비밀값 요청도 안전하다는 뜻이 아닙니다. 준비 상태와 관계없이 비밀값을 " +
                    "보내지 말고 별도 경로를 직접 고르세요."
        }
        return listOf(situationCopy, readinessCopy).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 고정 문장을 직접 읽고 기존 앱 경로를 직접 고르는 로컬 전용·무상태 수동 안내이며 비밀값·계정값·연락처·받는 사람·결과값 입력을 요구하지 않습니다.",
        "추천은 현재 검사 결과의 위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기·가족 안전 계획의 실제 값을 사용하지 않습니다.",
        "인증번호·PIN·비밀번호·보안 답·상품권 비밀값을 읽기·표시·저장·복사·붙여넣기·자동 입력·제출·생성·변경·재정비·취소·폐기·동기화·정리·복구하지 않습니다.",
        "클립보드·텍스트 감시기·자동완성, Credential Manager·Account Manager·Biometric Prompt·Keyguard Manager·Key Store·암호화 비밀 저장소, 비밀번호 관리자·인증 앱 기능을 사용하지 않습니다.",
        "계정 로그인·계정 복구·비밀번호 변경·코드 검증·비밀번호 강도 검사·비밀값 생성을 하지 않고 자격 증명·코드·계정·로그인·인증 상태를 판정하지 않습니다.",
        "가족 안전 계획·안전단어 재정비·콜백 화면으로 이동하는 버튼은 기존 화면을 열 뿐이며 안전단어·콜백 문장·연락처 이름·번호를 읽거나 표시·공유·수정·저장·삭제하지 않습니다.",
        "연락처·주소록·통화 기록·문자·SMS·통화 상태·알림을 읽거나 감시하지 않고 연락처 읽기·쓰기·편집·삽입·삭제·선택, 전화·다이얼·발신·통화·종료·선별·차단·SMS·받는 사람 선택·자동 전송을 하지 않습니다.",
        "전화번호·URL·도메인·프로필·공식 계정·신원·기관·자격 증명·코드·결제·계정 상태를 검사·조회·검증하지 않습니다.",
        "파일·문서·사진·이미지·오디오·미디어·저장소를 읽지 않고 카메라·화면 캡처·스크린샷 가져오기·OCR·이미지 분석·녹음·전사·파일 선택기·첨부·스트림·URI 공유를 사용하지 않습니다.",
        "네트워크·웹 탐색·브라우저·지도·위치·지오코딩·기관 정보·공식 번호 조회·보고 제출·업로드·클라우드 백업·증거 동기화를 사용하지 않습니다.",
        "새 Android 권한을 요청하지 않고 알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러를 만들거나 예약하지 않습니다.",
        "새 저장소를 만들지 않고 카드 선택·추천·읽음·공유·체크·메모·담당자·받는 사람·확인·노출·계정·결제·복구·진행·상태·이력을 저장하지 않습니다.",
        "결제·은행 기능이나 API·NFC를 사용하지 않고 송금·결제 취소·계정 또는 카드 정지·동결·환급·회수·복구를 자동 수행하지 않습니다.",
        "기기 관리자·접근성 서비스·VPN·사용 기록·앱 작업 권한을 쓰지 않고 설치 앱·패키지 목록 조회, 앱 설치·제거, 설정 변경, 원격 연결 종료, 악성 프로그램 검사·제거, 기기 정리를 하지 않습니다.",
        "화면·경로·공유 버튼을 눌러도 원문·비밀값·클립보드·자동완성·계정·로그인·연락처·통화·문자·알림·사건·가족 계획·기관 목록·결제·기기·앱·파일·확인 결과를 추가·수정·삭제하지 않습니다.",
        "수동 문자 공유는 고정된 멈춤 카드·기존 앱 경로·행동 종류만 담고 비밀값·사람·연락처·채널·계정·결제·받는 사람·결과값·파일·첨부·스트림·URI를 담거나 자동 발송하지 않습니다.",
        "자격 증명·코드·계정·로그인·기기·은행 행동·결제·복구 상태를 판정하거나 보장하지 않으며 법률·금융 결과를 증명하거나 보장하지 않습니다. 자동 모니터링·검사·스캔·OCR·삭제·가림·차단·통화·메시지 전송·조회·검증·저장·업로드·첨부·제출을 수행하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<CredentialSecretFreezeCardId>,
        recommendedActions: List<CredentialSecretFreezeAction>,
        cards: List<CredentialSecretFreezeCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 비밀값 멈춤 코치")
            appendLine()
            appendLine("비밀값을 보거나 옮기지 않는 수동 멈춤 순서를 함께 보고 있습니다.")
            appendLine()
            appendLine("추천 멈춤 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("가족에게 말할 값 없는 문장")
            appendLine("- 멈춤·별도 확인·돈 멈춤·계정 점검·도움 요청 같은 행동 종류만 함께 살펴보세요.")
            append(
                "사람·연락처·채널·비밀값·계정값·원문·사건 내용·링크·기기·앱·파일·" +
                    "받는 사람·확인·결제·복구 결과 없이 고정 카드와 기존 앱 경로 이름만 담았습니다."
            )
        }.trim()
    }

    private fun actionFor(
        id: CredentialSecretFreezeActionId
    ): CredentialSecretFreezeAction = CredentialSecretFreezeAction(
        id = id,
        label = when (id) {
            CredentialSecretFreezeActionId.QUICK_STOP -> "긴급 멈춤 모드"
            CredentialSecretFreezeActionId.CHECKER -> "의심 내용 검사"
            CredentialSecretFreezeActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            CredentialSecretFreezeActionId.SAFETY_PLAN -> "가족 안전 계획"
            CredentialSecretFreezeActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
            CredentialSecretFreezeActionId.FAMILY_SAFETY_DRILL_COACH ->
                "가족 안전 연습 코치"
            CredentialSecretFreezeActionId.SAFE_PASTE_PRIVACY_COACH ->
                "붙여넣기 전 개인정보 가리기"
            CredentialSecretFreezeActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                "진행 중 연락 종료 문구"
            CredentialSecretFreezeActionId.FAMILY_CALLBACK_CARD ->
                "평소 가족 경로 콜백 카드"
            CredentialSecretFreezeActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                "가족 안전단어 재설정 코치"
            CredentialSecretFreezeActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                "별도 경로 확인 코치"
            CredentialSecretFreezeActionId.SAFE_CALL_SCRIPTS ->
                "값 없는 가족 콜백·확인 전화 스크립트"
            CredentialSecretFreezeActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                "목소리 사칭 현실 점검 코치"
            CredentialSecretFreezeActionId.NEW_NUMBER_QUARANTINE_COACH ->
                "새 번호 격리 코치"
            CredentialSecretFreezeActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
            CredentialSecretFreezeActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                "확인 결과별 다음 단계 보드"
            CredentialSecretFreezeActionId.RECONTACT_BOUNDARY_COACH -> "재연락 경계 코치"
            CredentialSecretFreezeActionId.TRUSTED_HELPER_REVIEW ->
                "보호자와 함께 안전 검토"
            CredentialSecretFreezeActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                "가족 대응 역할 보드"
            CredentialSecretFreezeActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                "가족 차분 대화 안건"
            CredentialSecretFreezeActionId.MONEY_TRANSFER_PAUSE_GATE ->
                "송금 전 멈춤 확인"
            CredentialSecretFreezeActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
            CredentialSecretFreezeActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                "화면공유·원격지시 끊기 코치"
            CredentialSecretFreezeActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
            CredentialSecretFreezeActionId.PRIVACY_CENTER -> "개인정보 센터"
            CredentialSecretFreezeActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                "증거 전달 개인정보 코치"
            CredentialSecretFreezeActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            CredentialSecretFreezeActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 도움 경로"
            CredentialSecretFreezeActionId.REPORT_PREP -> "신고용 상담 준비 카드"
            CredentialSecretFreezeActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                "며칠 뒤 다시 확인할 것"
            CredentialSecretFreezeActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
