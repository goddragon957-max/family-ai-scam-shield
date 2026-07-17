package com.mongmong.familyshield

enum class RecoveryPromiseRealityCheckCardId(val displayOrder: Int) {
    PROMISE_IS_NOT_A_RESULT(1),
    NO_UPFRONT_FEE_OR_PAYMENT(2),
    NO_SECRET_OR_FINANCIAL_VALUES(3),
    NO_APP_REMOTE_OR_SCREEN_SHARE(4),
    SEPARATE_TITLE_AND_OFFICIAL_PRESSURE(5),
    VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP(6)
}

enum class RecoveryPromiseRealityCheckActionId {
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
    OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
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

data class RecoveryPromiseRealityCheckAction(
    val id: RecoveryPromiseRealityCheckActionId,
    val label: String
)

data class RecoveryPromiseRealityCheckCard(
    val id: RecoveryPromiseRealityCheckCardId,
    val displayOrder: Int,
    val title: String,
    val recoveryPromiseScenario: String,
    val valueFreeActionSentence: String,
    val whyItMatters: String,
    val doNotPayShareSubmitInstallOrCallBoundaries: List<String>,
    val primaryAction: RecoveryPromiseRealityCheckAction,
    val secondaryActions: List<RecoveryPromiseRealityCheckAction>
) {
    val routeActions: List<RecoveryPromiseRealityCheckAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class RecoveryPromiseRealityCheckCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<RecoveryPromiseRealityCheckCardId>,
    val recommendedActions: List<RecoveryPromiseRealityCheckAction>,
    val cards: List<RecoveryPromiseRealityCheckCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<RecoveryPromiseRealityCheckCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<RecoveryPromiseRealityCheckActionId>
        get() = recommendedActions.map { action -> action.id }
}

object RecoveryPromiseRealityCheckCoachBuilder {
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

    private val officialPressureSignals = setOf(
        ScamSignal.IMPERSONATION,
        ScamSignal.SECRECY
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

    fun build(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness,
        incidentCount: Int = 0
    ): RecoveryPromiseRealityCheckCoach {
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val riskLevel = analysis?.riskLevel
        val readinessLevel = readiness.level
        val hasNoAnalysisOrSignals = analysis == null || signals.isEmpty()
        val hasHighRisk = riskLevel == RiskLevel.HIGH
        val hasFinancialSignal = signals.any { signal -> signal in financialSignals }
        val hasCredentialSignal = signals.any { signal -> signal in credentialSignals }
        val hasOfficialPressureSignal = signals.any { signal ->
            signal in officialPressureSignals
        }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasRemoteAccessSignal = signals.any { signal -> signal in remoteAccessSignals }
        val hasFamilyVerificationSignal = signals.any { signal ->
            signal in familyVerificationSignals
        }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0

        val recommendedCardSet = linkedSetOf(
            RecoveryPromiseRealityCheckCardId.PROMISE_IS_NOT_A_RESULT,
            RecoveryPromiseRealityCheckCardId.NO_UPFRONT_FEE_OR_PAYMENT
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += RecoveryPromiseRealityCheckCardId.entries
        }
        if (hasFinancialSignal) {
            recommendedCardSet += RecoveryPromiseRealityCheckCardId.PROMISE_IS_NOT_A_RESULT
            recommendedCardSet += RecoveryPromiseRealityCheckCardId.NO_UPFRONT_FEE_OR_PAYMENT
            recommendedCardSet += RecoveryPromiseRealityCheckCardId.NO_SECRET_OR_FINANCIAL_VALUES
            recommendedCardSet += RecoveryPromiseRealityCheckCardId.SEPARATE_TITLE_AND_OFFICIAL_PRESSURE
            recommendedCardSet +=
                RecoveryPromiseRealityCheckCardId.VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP
        }
        if (hasCredentialSignal) {
            recommendedCardSet += RecoveryPromiseRealityCheckCardId.NO_SECRET_OR_FINANCIAL_VALUES
            recommendedCardSet += RecoveryPromiseRealityCheckCardId.NO_APP_REMOTE_OR_SCREEN_SHARE
        }
        if (hasOfficialPressureSignal) {
            recommendedCardSet += RecoveryPromiseRealityCheckCardId.PROMISE_IS_NOT_A_RESULT
            recommendedCardSet += RecoveryPromiseRealityCheckCardId.SEPARATE_TITLE_AND_OFFICIAL_PRESSURE
            recommendedCardSet +=
                RecoveryPromiseRealityCheckCardId.VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP
        }
        if (hasLinkSignal || hasRemoteAccessSignal) {
            recommendedCardSet += RecoveryPromiseRealityCheckCardId.NO_SECRET_OR_FINANCIAL_VALUES
            recommendedCardSet += RecoveryPromiseRealityCheckCardId.NO_APP_REMOTE_OR_SCREEN_SHARE
            recommendedCardSet += RecoveryPromiseRealityCheckCardId.PROMISE_IS_NOT_A_RESULT
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet += RecoveryPromiseRealityCheckCardId.PROMISE_IS_NOT_A_RESULT
            recommendedCardSet += RecoveryPromiseRealityCheckCardId.SEPARATE_TITLE_AND_OFFICIAL_PRESSURE
        }
        if (hasSavedIncident) {
            recommendedCardSet +=
                RecoveryPromiseRealityCheckCardId.VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedCardSet += RecoveryPromiseRealityCheckCardId.PROMISE_IS_NOT_A_RESULT
                recommendedCardSet +=
                    RecoveryPromiseRealityCheckCardId.SEPARATE_TITLE_AND_OFFICIAL_PRESSURE
            }
            ReadinessLevel.READY -> {
                recommendedCardSet += RecoveryPromiseRealityCheckCardId.PROMISE_IS_NOT_A_RESULT
            }
        }
        val recommendedCardIds = RecoveryPromiseRealityCheckCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            RecoveryPromiseRealityCheckActionId.CHECKER
        } else {
            RecoveryPromiseRealityCheckActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            currentResultActionId,
            RecoveryPromiseRealityCheckActionId.QUICK_STOP,
            RecoveryPromiseRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE,
            RecoveryPromiseRealityCheckActionId.CREDENTIAL_SECRET_FREEZE_COACH,
            RecoveryPromiseRealityCheckActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
            RecoveryPromiseRealityCheckActionId.TRUSTED_HELPER_REVIEW,
            RecoveryPromiseRealityCheckActionId.OFFICIAL_HELP_DIRECTORY,
            RecoveryPromiseRealityCheckActionId.REPORT_PREP,
            RecoveryPromiseRealityCheckActionId.PRIVACY_CENTER,
            RecoveryPromiseRealityCheckActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            RecoveryPromiseRealityCheckActionId.entries
                .filterNot { actionId ->
                    actionId == RecoveryPromiseRealityCheckActionId.ANALYSIS_RESULT
                }
                .forEach { actionId -> recommendedActionSet += actionId }
        }
        if (hasHighRisk) {
            RecoveryPromiseRealityCheckActionId.entries.forEach { actionId ->
                recommendedActionSet += actionId
            }
        }
        if (hasFinancialSignal) {
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.REPORT_PREP
        }
        if (hasCredentialSignal) {
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE
        }
        if (hasOfficialPressureSignal) {
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.REPORT_PREP
        }
        if (hasLinkSignal || hasRemoteAccessSignal) {
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.PRIVACY_CENTER
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.NEW_NUMBER_QUARANTINE_COACH
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasSavedIncident) {
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += RecoveryPromiseRealityCheckActionId.EVIDENCE_KIT
            recommendedActionSet +=
                RecoveryPromiseRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedActionSet += RecoveryPromiseRealityCheckActionId.SAFETY_PLAN
                recommendedActionSet +=
                    RecoveryPromiseRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH
                recommendedActionSet +=
                    RecoveryPromiseRealityCheckActionId.FAMILY_READINESS_ROADMAP
                recommendedActionSet +=
                    RecoveryPromiseRealityCheckActionId.FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += RecoveryPromiseRealityCheckActionId.SAFE_CALL_SCRIPTS
            }
            ReadinessLevel.READY -> {
                recommendedActionSet += RecoveryPromiseRealityCheckActionId.FAMILY_CALLBACK_CARD
                recommendedActionSet +=
                    RecoveryPromiseRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                recommendedActionSet += RecoveryPromiseRealityCheckActionId.SAFE_CALL_SCRIPTS
            }
        }
        val recommendedActions = RecoveryPromiseRealityCheckActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null -> "검사 결과가 없어도 모든 회복 약속 카드를 보수적으로 읽기"
            hasFinancialSignal -> "회복을 조건으로 한 선결제와 정보 요구부터 멈추기"
            hasCredentialSignal -> "회복 조건으로 비밀값·계정·기기 행동을 주지 않기"
            hasOfficialPressureSignal -> "직함과 사건 말보다 회복 약속 행동을 따로 점검하기"
            hasRemoteAccessSignal -> "같은 앱·통화·화면공유 회복 흐름을 벗어나기"
            hasLinkSignal -> "상대가 준 링크 안에서 회복 절차를 이어가지 않기"
            hasFamilyVerificationSignal -> "익숙한 관계를 내세운 회복 약속도 별도 가족 경로로 보기"
            hasHighRisk -> "높은 위험 범주에서 값 없는 회복 약속 대응을 넓게 고르기"
            hasSavedIncident -> "사건 내용 없이 증거·도움·후속 준비 경로만 고르기"
            else -> "회복 약속에 답하기 전에 결제·공유·설치 행동을 멈추기"
        }

        return RecoveryPromiseRealityCheckCoach(
            title = "회복 약속 현실 점검 코치",
            intro =
                "환급팀·회복 지원·사건 담당 같은 말로 돈을 되찾거나 결제·계정 문제를 " +
                    "처리해 준다고 해도 결과로 단정하지 않고, 수수료 결제·비밀값 공유·앱 " +
                    "설치 전에 값 없는 고정 카드에서 기존 대응 화면만 직접 고르는 로컬 " +
                    "전용·무상태 수동 안내입니다.",
            modeLabel = "회복 약속은 결과가 아님 · 선결제와 비밀값 멈춤",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                readinessLevel = readinessLevel,
                hasNoAnalysisOrSignals = hasNoAnalysisOrSignals,
                hasHighRisk = hasHighRisk,
                hasFinancialSignal = hasFinancialSignal,
                hasCredentialSignal = hasCredentialSignal,
                hasOfficialPressureSignal = hasOfficialPressureSignal,
                hasLinkSignal = hasLinkSignal,
                hasRemoteAccessSignal = hasRemoteAccessSignal,
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

    private fun buildCards(): List<RecoveryPromiseRealityCheckCard> {
        fun action(
            id: RecoveryPromiseRealityCheckActionId
        ): RecoveryPromiseRealityCheckAction = actionFor(id)

        return listOf(
            RecoveryPromiseRealityCheckCard(
                id = RecoveryPromiseRealityCheckCardId.PROMISE_IS_NOT_A_RESULT,
                displayOrder =
                    RecoveryPromiseRealityCheckCardId.PROMISE_IS_NOT_A_RESULT.displayOrder,
                title = "회복·환급·취소 약속을 결과로 믿지 않기",
                recoveryPromiseScenario =
                    "누군가 피해금 회복, 결제 취소, 계정 보호나 보상을 처리해 주겠다고 재촉하는 때",
                valueFreeActionSentence =
                    "약속을 결과로 단정하지 않고 지금은 결제·공유·설치·제출을 멈추겠습니다.",
                whyItMatters =
                    "피해 뒤에 다시 접근하는 사람은 되찾아 주겠다는 기대를 이용해 새로운 행동을 요구할 수 있습니다.",
                doNotPayShareSubmitInstallOrCallBoundaries = listOf(
                    "회복·환급·취소·동결·추적·보상 상태나 사건 식별값을 이 코치에 적거나 공유하지 마세요.",
                    "회복·환급·차지백·취소·동결·추적·사건 처리를 수행·조회·제출·보장하지 않습니다."
                ),
                primaryAction = action(RecoveryPromiseRealityCheckActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(RecoveryPromiseRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE),
                    action(RecoveryPromiseRealityCheckActionId.TRUSTED_HELPER_REVIEW),
                    action(RecoveryPromiseRealityCheckActionId.RECONTACT_BOUNDARY_COACH)
                )
            ),
            RecoveryPromiseRealityCheckCard(
                id = RecoveryPromiseRealityCheckCardId.NO_UPFRONT_FEE_OR_PAYMENT,
                displayOrder =
                    RecoveryPromiseRealityCheckCardId.NO_UPFRONT_FEE_OR_PAYMENT.displayOrder,
                title = "수수료·세금·보증금·상품권을 먼저 내지 않기",
                recoveryPromiseScenario =
                    "회복이나 보상 절차를 시작하려면 먼저 비용·세금·보증금·상품권이 필요하다고 하는 때",
                valueFreeActionSentence =
                    "어떤 선결제도 이어가지 않고 값 없는 송금 전 멈춤 순서를 읽겠습니다.",
                whyItMatters =
                    "먼저 돈을 요구하는 회복 약속은 기존 피해와 별개인 추가 손실로 이어질 수 있습니다.",
                doNotPayShareSubmitInstallOrCallBoundaries = listOf(
                    "금액·받는 사람·은행·계좌·카드·결제·거래·상품권·지갑 값을 입력하거나 공유하지 마세요.",
                    "송금·구매·결제·취소·환급·차지백·회수·계정 또는 카드 동결을 수행하거나 보장하지 않습니다."
                ),
                primaryAction = action(
                    RecoveryPromiseRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE
                ),
                secondaryActions = listOf(
                    action(
                        RecoveryPromiseRealityCheckActionId
                            .CREDENTIAL_SECRET_FREEZE_COACH
                    ),
                    action(RecoveryPromiseRealityCheckActionId.ACCOUNT_LOCKDOWN),
                    action(RecoveryPromiseRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE)
                )
            ),
            RecoveryPromiseRealityCheckCard(
                id = RecoveryPromiseRealityCheckCardId.NO_SECRET_OR_FINANCIAL_VALUES,
                displayOrder =
                    RecoveryPromiseRealityCheckCardId.NO_SECRET_OR_FINANCIAL_VALUES.displayOrder,
                title = "인증 비밀값·계정·카드·지갑 정보를 회복 조건으로 주지 않기",
                recoveryPromiseScenario =
                    "본인 확인이나 회복 처리를 이유로 인증 비밀값·로그인·금융·지갑 정보를 요구하는 때",
                valueFreeActionSentence =
                    "어떤 실제 값도 말하거나 붙여넣지 않고 비밀값 멈춤 경로를 직접 고르겠습니다.",
                whyItMatters =
                    "회복이라는 명목은 인증과 금융 정보를 다시 요구하는 구실로 쓰일 수 있습니다.",
                doNotPayShareSubmitInstallOrCallBoundaries = listOf(
                    "인증번호·PIN·비밀번호·보안 답·계정·카드·은행·지갑·거래 값을 적거나 공유하지 마세요.",
                    "자격 증명·로그인·계정·카드·은행·지갑·결제 상태를 읽거나 확인하거나 바꾸지 않습니다."
                ),
                primaryAction = action(
                    RecoveryPromiseRealityCheckActionId.CREDENTIAL_SECRET_FREEZE_COACH
                ),
                secondaryActions = listOf(
                    action(RecoveryPromiseRealityCheckActionId.SAFE_PASTE_PRIVACY_COACH),
                    action(RecoveryPromiseRealityCheckActionId.ACCOUNT_LOCKDOWN),
                    action(RecoveryPromiseRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE)
                )
            ),
            RecoveryPromiseRealityCheckCard(
                id = RecoveryPromiseRealityCheckCardId.NO_APP_REMOTE_OR_SCREEN_SHARE,
                displayOrder =
                    RecoveryPromiseRealityCheckCardId.NO_APP_REMOTE_OR_SCREEN_SHARE.displayOrder,
                title = "앱 설치·원격조작·화면공유로 회복을 돕겠다는 흐름 끊기",
                recoveryPromiseScenario =
                    "링크나 앱을 열고 통화를 유지하거나 원격조작·화면공유를 해야 회복을 돕는다고 하는 때",
                valueFreeActionSentence =
                    "같은 링크·앱·통화·화면공유 흐름에서는 입력이나 설치를 이어가지 않겠습니다.",
                whyItMatters =
                    "상대가 고른 세션 안에서 계속 행동하면 입력 위치와 화면 행동까지 통제받을 수 있습니다.",
                doNotPayShareSubmitInstallOrCallBoundaries = listOf(
                    "URL·도메인·앱·패키지·기기 식별값·화면·파일·사진·녹음 이름을 적거나 공유하지 마세요.",
                    "링크·앱·기기를 검사하지 않고 앱 목록 조회·설치·제거·원격 종료·기기 정리를 수행하지 않습니다."
                ),
                primaryAction = action(
                    RecoveryPromiseRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH
                ),
                secondaryActions = listOf(
                    action(RecoveryPromiseRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(RecoveryPromiseRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(RecoveryPromiseRealityCheckActionId.PRIVACY_CENTER)
                )
            ),
            RecoveryPromiseRealityCheckCard(
                id = RecoveryPromiseRealityCheckCardId.SEPARATE_TITLE_AND_OFFICIAL_PRESSURE,
                displayOrder =
                    RecoveryPromiseRealityCheckCardId
                        .SEPARATE_TITLE_AND_OFFICIAL_PRESSURE.displayOrder,
                title = "변호사·수사관·은행·플랫폼 직함은 기존 공식 압박 점검으로 분리하기",
                recoveryPromiseScenario =
                    "직함·기관명·사건 설명·문서 화면을 내세워 회복 절차가 공식이라고 믿으라고 하는 때",
                valueFreeActionSentence =
                    "직함과 화면을 증거로 단정하지 않고 기존 기관 사칭 압박 점검을 따로 읽겠습니다.",
                whyItMatters =
                    "그럴듯한 직함이나 문서는 사람·기관·요청·회복 약속의 사실 여부를 증명하지 않습니다.",
                doNotPayShareSubmitInstallOrCallBoundaries = listOf(
                    "사람·도우미·변호사·수사관·기관·발신번호·문서·사건·공식 번호 값을 적거나 공유하지 마세요.",
                    "사람·기관·직함·번호·발신 표시·문서·사건을 조회·검증하지 않고 전화·문자·브라우저·지도를 열지 않습니다."
                ),
                primaryAction = action(
                    RecoveryPromiseRealityCheckActionId
                        .OFFICIAL_PRESSURE_REALITY_CHECK_COACH
                ),
                secondaryActions = listOf(
                    action(RecoveryPromiseRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(
                        RecoveryPromiseRealityCheckActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(RecoveryPromiseRealityCheckActionId.TRUSTED_HELPER_REVIEW)
                )
            ),
            RecoveryPromiseRealityCheckCard(
                id =
                    RecoveryPromiseRealityCheckCardId
                        .VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP,
                displayOrder =
                    RecoveryPromiseRealityCheckCardId
                        .VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP.displayOrder,
                title = "증거·신고·후속 점검은 값 없이 기존 화면에서만 정리하기",
                recoveryPromiseScenario =
                    "가족과 검토하거나 증거 보존·도움·신고 준비·며칠 뒤 점검 순서를 이어가려는 때",
                valueFreeActionSentence =
                    "사건 원문이나 식별값 없이 기존 증거·신고 준비·후속 화면에서 순서만 살펴보겠습니다.",
                whyItMatters =
                    "값 없는 라우팅은 민감한 피해 정보가 카드나 공유 문장에 반복 노출되는 일을 줄입니다.",
                doNotPayShareSubmitInstallOrCallBoundaries = listOf(
                    "원문·가린 단서·사건 내용·미리보기·문서·파일·사진·녹음·받는 사람·제출 결과를 적거나 공유하지 마세요.",
                    "증거를 수집·촬영·첨부·업로드·동기화·가림·삭제하지 않고 신고·사건·법률 절차를 작성·제출·확인하지 않습니다."
                ),
                primaryAction = action(
                    RecoveryPromiseRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
                ),
                secondaryActions = listOf(
                    action(RecoveryPromiseRealityCheckActionId.EVIDENCE_KIT),
                    action(RecoveryPromiseRealityCheckActionId.OFFICIAL_HELP_DIRECTORY),
                    action(RecoveryPromiseRealityCheckActionId.REPORT_PREP),
                    action(
                        RecoveryPromiseRealityCheckActionId
                            .FOLLOW_UP_CHECK_IN_PLANNER
                    ),
                    action(RecoveryPromiseRealityCheckActionId.SAFETY_FLOW_ROUTER)
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        readinessLevel: ReadinessLevel,
        hasNoAnalysisOrSignals: Boolean,
        hasHighRisk: Boolean,
        hasFinancialSignal: Boolean,
        hasCredentialSignal: Boolean,
        hasOfficialPressureSignal: Boolean,
        hasLinkSignal: Boolean,
        hasRemoteAccessSignal: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (hasNoAnalysisOrSignals) {
                add(
                    "검사 결과나 일치 신호가 없어도 결제·계정·카드·은행·지갑·거래·" +
                        "사건·신고·회복·환급·취소·동결·추적·사람·기관·링크·문서·기기·" +
                        "자격 상태를 단정하지 말고 모든 회복 약속 카드를 보수적으로 읽도록 권합니다."
                )
            }
            if (hasHighRisk) {
                add(
                    "높은 위험 범주에서는 돈·비밀값·기관 압박·계정·기기·노출·증거·" +
                        "가족·도움·후속 대응 경로를 값 없이 넓게 함께 보여 줍니다."
                )
            }
            if (hasFinancialSignal) {
                add(
                    "긴급 송금·미끼·상품권·가상자산·안전계좌·대환 비용·해외 결제 같은 " +
                        "돈 범주에서는 송금 멈춤·비밀값 멈춤·기관 압박 점검·계정 잠금·" +
                        "노출 분류·증거 전달 개인정보·보호자 검토·도움·신고 준비 경로를 " +
                        "보여 주며 취소·동결·환급·차지백·회수·복구·지갑 추적 결과를 만들지 않습니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증 비밀값 범주에서는 비밀값 멈춤·붙여넣기 개인정보·계정 잠금·" +
                        "원격지시 중단·노출 분류 경로를 권하며 인증번호·PIN·비밀번호·" +
                        "계정·카드·은행·지갑 값을 읽거나 표시하지 않습니다."
                )
            }
            if (hasOfficialPressureSignal) {
                add(
                    "사칭·비밀 유지 압박 범주에서는 기관 압박 점검·진행 중 연락 종료·" +
                        "가족 알림·차분 대화·보호자 검토·증거 전달 개인정보·도움·신고 " +
                        "준비 경로를 직접 고르며 공식 번호·디렉터리 값·법률 사건 값을 사용하지 않습니다."
                )
            }
            if (hasLinkSignal || hasRemoteAccessSignal) {
                add(
                    "링크·문자 미끼·앱 설치·통화 유지 신호에서는 같은 링크·앱·통화·" +
                        "화면공유 세션을 계속하지 말고 종료 문구·원격지시 중단·노출 분류·" +
                        "개인정보 경로를 직접 고르세요. URL 검증·링크 검사·앱 목록 조회·" +
                        "악성 프로그램 제거·원격 종료·기기 정리·지갑 추적을 하지 않습니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 긴급·목소리 사칭·번호 변경 범주에서는 별도 경로 확인·가족 " +
                        "콜백·목소리 현실 점검·새 번호 격리·안전단어 재정비·값 없는 전화 " +
                        "문구·가족 알림 경로를 고르며 연락처·안전단어·콜백 값을 사용하지 않습니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만으로 증거 전달 개인정보·증거 체크·" +
                        "후속 점검 경로를 더 보여 주며 사건 내용이나 미리보기는 읽지 않습니다."
                )
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 아직 진행 중인 범주라 안전 계획·안전단어 재정비·준비 " +
                    "로드맵·가족 연습·값 없는 전화 문구 경로를 직접 살펴보도록 권합니다. " +
                    "저장된 계획의 실제 값은 추천에 사용하지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있다는 범주는 기존 준비 항목이 채워졌다는 뜻일 뿐 " +
                    "회복 제안·도우미·변호사·수사관·기관·요청이 실제이거나 안전하다는 " +
                    "뜻이 아닙니다. 준비 상태와 관계없이 선결제·공유·설치 행동을 멈추세요."
        }
        return listOf(situationCopy, readinessCopy).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 고정 문장을 직접 읽고 기존 앱 경로를 직접 고르는 로컬 전용·무상태 수동 안내이며 사람·기관·연락처·번호·링크·문서·사건·계정·결제·지갑·기기·회복 결과값 입력을 요구하지 않습니다.",
        "추천은 현재 검사 결과의 위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기·가족 안전 계획의 실제 값을 사용하지 않습니다.",
        "사람·기관·도우미·변호사·수사관·직함·발신자 표시·전화번호·URL·도메인·프로필·신원·문서·사건·신고·결제·계정·카드·은행·지갑·거래·로그인·기기·회복 상태를 조회·검증·확인·판정하지 않습니다.",
        "기관 정보·공식 번호·디렉터리 값을 가져오지 않고 공식 번호 조회·전화번호 검증·URL 검증·도메인 스캔·발신자 표시 확인·문서 검증·신고 제출을 하지 않습니다.",
        "네트워크·웹 탐색·브라우저·지도·위치·지오코딩을 사용하지 않고 전화·다이얼·발신·통화·종료·선별·차단·문자·SMS·받는 사람 선택·자동 전송을 하지 않습니다.",
        "연락처·주소록·통화 기록·통화 상태·문자·알림을 읽거나 감시하지 않고 연락처 읽기·쓰기·편집·선택과 알림 수신·필터링을 하지 않습니다.",
        "클립보드·텍스트 감시기·자동완성·자격 증명·계정·인증·생체 인증·키가드·키 저장소·암호화 API를 사용하지 않고 계정·인증·로그인 상태를 읽거나 바꾸지 않습니다.",
        "인증번호·PIN·비밀번호·보안 답·안전단어·콜백 문장·연락처 이름·번호·계정·카드·은행·지갑·거래·받는 사람 값을 읽기·표시·저장·복사·붙여넣기·자동 입력·공유·제출하지 않습니다.",
        "파일·문서·사진·이미지·스크린샷·오디오·미디어·저장소를 읽지 않고 카메라·화면 캡처·OCR·이미지 분석·녹음·전사·파일 선택기·첨부·스트림·URI 공유를 사용하지 않습니다.",
        "수동 문자 공유는 고정 회복 약속 카드·기존 앱 경로·행동 종류만 담고 원문·사건·계획·기관 목록·연락처·채널·계정·결제·지갑·기기·앱·파일·받는 사람·확인·제출·회복 결과를 담거나 자동 발송하지 않습니다.",
        "새 Android 권한을 요청하지 않고 알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러를 만들거나 예약하지 않습니다.",
        "새 저장소를 만들지 않고 카드 선택·추천·읽음·공유·체크·메모·담당자·받는 사람·확인·신고·계정·결제·지갑·회복·진행·상태·이력을 저장하지 않으며 가족 계획·사건·디렉터리·연락처·알림·통화·문자 상태를 수정하지 않습니다.",
        "회복·환급·차지백·취소·동결·차단·회수·추적·보상·사건·신고·법률 절차를 수행하지 않고 법률 서비스·자문이나 법률·금융 결과를 제공·보장하지 않습니다.",
        "지갑·블록체인·가상자산·거래소 추적이나 조회를 하지 않고 결제·은행·카드 기능 또는 API·NFC를 사용하지 않으며 송금·구매·결제 처리·계정 또는 카드 조작을 하지 않습니다.",
        "기기 관리자·접근성 서비스·VPN·사용 기록·앱 작업 권한을 쓰지 않고 설치 앱·패키지 목록 조회, 앱 설치·제거, 설정 변경, 원격 연결 종료, 악성 프로그램 검사·제거, 기기 정리를 하지 않습니다.",
        "기존 돈·비밀값·기관 압박·계정·원격지시·노출·증거·도움·신고 준비·가족·보호자·후속 화면으로 이동하는 버튼은 화면만 열 뿐 해당 기능을 복제하거나 값을 넘기지 않습니다.",
        "사람·기관·도우미·변호사·수사관·직함·발신 표시·번호·링크·문서·사건·신고·결제·계정·카드·은행·지갑·거래·자격 증명·로그인·기기·차지백·환급·동결·추적·취소·회복 상태가 공식·실재·안전·보안됨·검증됨·유효·신뢰됨·접수됨·제출됨·수신됨·차단됨·취소됨·반전됨·해결됨·완료됨·회수됨·환급됨·동결됨·보호됨·합법·추적 가능·보장됨·문제없음이라고 증명하거나 보장하지 않습니다.",
        "자동 모니터링·검사·스캔·OCR·삭제·가림·차단·통화·메시지 전송·조회·검증·저장·업로드·첨부·제출을 수행하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<RecoveryPromiseRealityCheckCardId>,
        recommendedActions: List<RecoveryPromiseRealityCheckAction>,
        cards: List<RecoveryPromiseRealityCheckCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 회복 약속 현실 점검 코치")
            appendLine()
            appendLine("회복을 약속한 뒤 요구하는 행동을 멈추는 수동 순서를 함께 보고 있습니다.")
            appendLine()
            appendLine("추천 회복 약속 점검 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("가족에게 말할 값 없는 문장")
            appendLine("- 회복 약속 뒤 선결제·비밀값·설치·화면공유 요구가 있는지만 함께 살펴보세요.")
            append(
                "사람·연락처·번호·링크·문서·사건·계정·결제·지갑·기기·앱·파일·" +
                    "받는 사람·확인·제출·회복 결과 없이 고정 카드와 기존 앱 경로 이름만 담았습니다."
            )
        }.trim()
    }

    private fun actionFor(
        id: RecoveryPromiseRealityCheckActionId
    ): RecoveryPromiseRealityCheckAction = RecoveryPromiseRealityCheckAction(
        id = id,
        label = when (id) {
            RecoveryPromiseRealityCheckActionId.QUICK_STOP -> "긴급 멈춤 모드"
            RecoveryPromiseRealityCheckActionId.CHECKER -> "의심 내용 검사"
            RecoveryPromiseRealityCheckActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            RecoveryPromiseRealityCheckActionId.SAFETY_PLAN -> "가족 안전 계획"
            RecoveryPromiseRealityCheckActionId.FAMILY_READINESS_ROADMAP ->
                "가족 준비 로드맵"
            RecoveryPromiseRealityCheckActionId.FAMILY_SAFETY_DRILL_COACH ->
                "가족 안전 연습 코치"
            RecoveryPromiseRealityCheckActionId.SAFE_PASTE_PRIVACY_COACH ->
                "붙여넣기 전 개인정보 가리기"
            RecoveryPromiseRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                "진행 중 연락 종료 문구"
            RecoveryPromiseRealityCheckActionId.FAMILY_CALLBACK_CARD ->
                "평소 가족 경로 콜백 카드"
            RecoveryPromiseRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                "가족 안전단어 재설정 코치"
            RecoveryPromiseRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                "별도 경로 확인 코치"
            RecoveryPromiseRealityCheckActionId.SAFE_CALL_SCRIPTS ->
                "값 없는 가족 콜백·확인 전화 스크립트"
            RecoveryPromiseRealityCheckActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                "목소리 사칭 현실 점검 코치"
            RecoveryPromiseRealityCheckActionId.NEW_NUMBER_QUARANTINE_COACH ->
                "새 번호 격리 코치"
            RecoveryPromiseRealityCheckActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                "자격 비밀값 멈춤 코치"
            RecoveryPromiseRealityCheckActionId.FAMILY_ALERT_MESSAGES ->
                "가족 경고 메시지 팩"
            RecoveryPromiseRealityCheckActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                "확인 결과별 다음 단계 보드"
            RecoveryPromiseRealityCheckActionId.RECONTACT_BOUNDARY_COACH ->
                "재연락 경계 코치"
            RecoveryPromiseRealityCheckActionId.TRUSTED_HELPER_REVIEW ->
                "보호자와 함께 안전 검토"
            RecoveryPromiseRealityCheckActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                "가족 대응 역할 보드"
            RecoveryPromiseRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                "가족 차분 대화 안건"
            RecoveryPromiseRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE ->
                "송금 전 멈춤 확인"
            RecoveryPromiseRealityCheckActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                "기관 사칭 압박 현실 점검 코치"
            RecoveryPromiseRealityCheckActionId.ACCOUNT_LOCKDOWN ->
                "계정·기기 잠금 체크리스트"
            RecoveryPromiseRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                "화면공유·원격지시 끊기 코치"
            RecoveryPromiseRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE ->
                "노출 영향 분류"
            RecoveryPromiseRealityCheckActionId.PRIVACY_CENTER -> "개인정보 센터"
            RecoveryPromiseRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                "증거 전달 개인정보 코치"
            RecoveryPromiseRealityCheckActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            RecoveryPromiseRealityCheckActionId.OFFICIAL_HELP_DIRECTORY ->
                "신고·상담 도움 경로"
            RecoveryPromiseRealityCheckActionId.REPORT_PREP -> "신고용 상담 준비 카드"
            RecoveryPromiseRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                "며칠 뒤 다시 확인할 것"
            RecoveryPromiseRealityCheckActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
