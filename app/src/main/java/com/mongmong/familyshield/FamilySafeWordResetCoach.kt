package com.mongmong.familyshield

enum class FamilySafeWordResetCardId(val displayOrder: Int) {
    PAUSE_OLD_SECRET(1),
    CHOOSE_SEPARATE_OFFLINE_CHECK(2),
    MANUAL_SAFETY_PLAN_EDIT(3),
    SHARE_RULE_NOT_VALUE(4),
    VERIFY_AFTER_FAMILY_PRESSURE(5),
    FICTIONAL_REHEARSAL_ONLY(6)
}

enum class FamilySafeWordResetActionId {
    QUICK_STOP,
    CHECKER,
    ANALYSIS_RESULT,
    SAFETY_PLAN,
    FAMILY_READINESS_ROADMAP,
    FAMILY_SAFETY_DRILL_COACH,
    FAMILY_CALLBACK_CARD,
    SEPARATE_CHANNEL_VERIFICATION_COACH,
    SAFE_CALL_SCRIPTS,
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

data class FamilySafeWordResetAction(
    val id: FamilySafeWordResetActionId,
    val label: String
)

data class FamilySafeWordResetCard(
    val id: FamilySafeWordResetCardId,
    val displayOrder: Int,
    val title: String,
    val resetScenario: String,
    val valueFreeFamilySentence: String,
    val whyItMatters: String,
    val doNotSayRecordOrShareBoundaries: List<String>,
    val primaryAction: FamilySafeWordResetAction,
    val secondaryActions: List<FamilySafeWordResetAction>
) {
    val routeActions: List<FamilySafeWordResetAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class FamilySafeWordResetCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<FamilySafeWordResetCardId>,
    val recommendedActions: List<FamilySafeWordResetAction>,
    val cards: List<FamilySafeWordResetCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<FamilySafeWordResetCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<FamilySafeWordResetActionId>
        get() = recommendedActions.map { action -> action.id }
}

object FamilySafeWordResetCoachBuilder {
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
    ): FamilySafeWordResetCoach {
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

        val recommendedCardSet = linkedSetOf(
            FamilySafeWordResetCardId.PAUSE_OLD_SECRET,
            FamilySafeWordResetCardId.SHARE_RULE_NOT_VALUE,
            FamilySafeWordResetCardId.FICTIONAL_REHEARSAL_ONLY
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += FamilySafeWordResetCardId.entries
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet += FamilySafeWordResetCardId.PAUSE_OLD_SECRET
            recommendedCardSet += FamilySafeWordResetCardId.CHOOSE_SEPARATE_OFFLINE_CHECK
            recommendedCardSet += FamilySafeWordResetCardId.MANUAL_SAFETY_PLAN_EDIT
            recommendedCardSet += FamilySafeWordResetCardId.VERIFY_AFTER_FAMILY_PRESSURE
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedCardSet += FamilySafeWordResetCardId.SHARE_RULE_NOT_VALUE
            recommendedCardSet += FamilySafeWordResetCardId.VERIFY_AFTER_FAMILY_PRESSURE
        }
        if (hasFinancialSignal || hasCredentialSignal) {
            recommendedCardSet += FamilySafeWordResetCardId.PAUSE_OLD_SECRET
            recommendedCardSet += FamilySafeWordResetCardId.SHARE_RULE_NOT_VALUE
        }
        if (hasLinkSignal || hasRemoteAccessSignal) {
            recommendedCardSet += FamilySafeWordResetCardId.CHOOSE_SEPARATE_OFFLINE_CHECK
            recommendedCardSet += FamilySafeWordResetCardId.VERIFY_AFTER_FAMILY_PRESSURE
        }
        if (hasSavedIncident) {
            recommendedCardSet += FamilySafeWordResetCardId.SHARE_RULE_NOT_VALUE
            recommendedCardSet += FamilySafeWordResetCardId.FICTIONAL_REHEARSAL_ONLY
        }
        when (readiness.level) {
            ReadinessLevel.LOW -> {
                recommendedCardSet += FamilySafeWordResetCardId.CHOOSE_SEPARATE_OFFLINE_CHECK
                recommendedCardSet += FamilySafeWordResetCardId.MANUAL_SAFETY_PLAN_EDIT
            }
            ReadinessLevel.IN_PROGRESS -> {
                recommendedCardSet += FamilySafeWordResetCardId.MANUAL_SAFETY_PLAN_EDIT
                recommendedCardSet += FamilySafeWordResetCardId.SHARE_RULE_NOT_VALUE
            }
            ReadinessLevel.READY -> {
                recommendedCardSet += FamilySafeWordResetCardId.PAUSE_OLD_SECRET
                recommendedCardSet += FamilySafeWordResetCardId.FICTIONAL_REHEARSAL_ONLY
            }
        }
        val recommendedCardIds = FamilySafeWordResetCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            FamilySafeWordResetActionId.CHECKER
        } else {
            FamilySafeWordResetActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            currentResultActionId,
            FamilySafeWordResetActionId.SAFETY_PLAN,
            FamilySafeWordResetActionId.FAMILY_READINESS_ROADMAP,
            FamilySafeWordResetActionId.FAMILY_SAFETY_DRILL_COACH,
            FamilySafeWordResetActionId.SAFE_CALL_SCRIPTS,
            FamilySafeWordResetActionId.PRIVACY_CENTER,
            FamilySafeWordResetActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            FamilySafeWordResetActionId.entries
                .filterNot { actionId -> actionId == FamilySafeWordResetActionId.ANALYSIS_RESULT }
                .forEach { actionId -> recommendedActionSet += actionId }
        }
        if (hasHighRisk) {
            recommendedActionSet += FamilySafeWordResetActionId.QUICK_STOP
            recommendedActionSet += FamilySafeWordResetActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet +=
                FamilySafeWordResetActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
            recommendedActionSet += FamilySafeWordResetActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += FamilySafeWordResetActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += FamilySafeWordResetActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += FamilySafeWordResetActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += FamilySafeWordResetActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += FamilySafeWordResetActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += FamilySafeWordResetActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += FamilySafeWordResetActionId.EVIDENCE_KIT
            recommendedActionSet += FamilySafeWordResetActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += FamilySafeWordResetActionId.REPORT_PREP
            recommendedActionSet += FamilySafeWordResetActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += FamilySafeWordResetActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += FamilySafeWordResetActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += FamilySafeWordResetActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += FamilySafeWordResetActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet +=
                FamilySafeWordResetActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
            recommendedActionSet += FamilySafeWordResetActionId.SAFETY_PLAN
            recommendedActionSet += FamilySafeWordResetActionId.FAMILY_SAFETY_DRILL_COACH
            recommendedActionSet += FamilySafeWordResetActionId.FAMILY_READINESS_ROADMAP
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedActionSet += FamilySafeWordResetActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += FamilySafeWordResetActionId.FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet += FamilySafeWordResetActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += FamilySafeWordResetActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += FamilySafeWordResetActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += FamilySafeWordResetActionId.REPORT_PREP
        }
        if (hasFinancialSignal) {
            recommendedActionSet += FamilySafeWordResetActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += FamilySafeWordResetActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += FamilySafeWordResetActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += FamilySafeWordResetActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += FamilySafeWordResetActionId.EVIDENCE_KIT
        }
        if (hasCredentialSignal) {
            recommendedActionSet += FamilySafeWordResetActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += FamilySafeWordResetActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += FamilySafeWordResetActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += FamilySafeWordResetActionId.EXPOSURE_IMPACT_TRIAGE
        }
        if (hasLinkSignal) {
            recommendedActionSet += FamilySafeWordResetActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += FamilySafeWordResetActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += FamilySafeWordResetActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += FamilySafeWordResetActionId.PRIVACY_CENTER
        }
        if (hasRemoteAccessSignal) {
            recommendedActionSet += FamilySafeWordResetActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += FamilySafeWordResetActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += FamilySafeWordResetActionId.PRIVACY_CENTER
            recommendedActionSet += FamilySafeWordResetActionId.TRUSTED_HELPER_REVIEW
        }
        if (hasSavedIncident) {
            recommendedActionSet += FamilySafeWordResetActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += FamilySafeWordResetActionId.EVIDENCE_KIT
            recommendedActionSet += FamilySafeWordResetActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        when (readiness.level) {
            ReadinessLevel.LOW -> {
                recommendedActionSet += FamilySafeWordResetActionId.SAFETY_PLAN
                recommendedActionSet += FamilySafeWordResetActionId.FAMILY_READINESS_ROADMAP
                recommendedActionSet += FamilySafeWordResetActionId.FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += FamilySafeWordResetActionId.SAFE_CALL_SCRIPTS
            }
            ReadinessLevel.IN_PROGRESS -> {
                recommendedActionSet += FamilySafeWordResetActionId.SAFETY_PLAN
                recommendedActionSet += FamilySafeWordResetActionId.FAMILY_READINESS_ROADMAP
                recommendedActionSet += FamilySafeWordResetActionId.FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += FamilySafeWordResetActionId.SAFE_CALL_SCRIPTS
                recommendedActionSet += FamilySafeWordResetActionId.FAMILY_CALLBACK_CARD
            }
            ReadinessLevel.READY -> {
                recommendedActionSet += FamilySafeWordResetActionId.FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += FamilySafeWordResetActionId.SAFE_CALL_SCRIPTS
                recommendedActionSet += FamilySafeWordResetActionId.FAMILY_CALLBACK_CARD
                recommendedActionSet += FamilySafeWordResetActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            }
        }
        val recommendedActions = FamilySafeWordResetActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null -> "분석 전에도 모든 재정비 카드를 보수적으로 읽기"
            hasFamilyVerificationSignal -> "이전 문구 하나에 기대지 않고 별도 가족 경로로 확인하기"
            hasRemoteAccessSignal -> "같은 연락·앱·화면 세션을 벗어나 재정비 순서 고르기"
            hasLinkSignal -> "같은 링크·메시지 경로로 새 비밀을 보내지 않기"
            hasCredentialSignal -> "인증 비밀과 가족 확인 비밀을 분리해 보호 경로 고르기"
            hasFinancialSignal -> "돈 행동을 멈추고 값 없는 가족 확인 규칙 다시 보기"
            hasSecrecyImpersonationSignal -> "재정비를 숨기지 않고 가족·보호자와 규칙만 나누기"
            hasHighRisk -> "높은 위험 신호에서 이전 문구만 믿지 않고 넓게 재정비하기"
            hasSavedIncident -> "저장 사건 내용 없이 가족 확인 루틴 다시 보기"
            else -> "실제 답을 말하지 않고 가족 확인 루틴 다시 보기"
        }

        return FamilySafeWordResetCoach(
            title = "가족 안전단어 재설정 코치",
            intro =
                "가족 확인 문구가 말해졌거나 보였을 수 있다고 느낄 때 실제 답을 입력하지 않고, " +
                    "이전 문구 하나에 기대지 않는 수동 재정비 순서와 기존 앱 경로만 직접 고르는 " +
                    "로컬 전용·무상태 안내입니다.",
            modeLabel = "실제 답 없이 · 가족이 직접 다시 정하기",
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

    private fun buildCards(): List<FamilySafeWordResetCard> {
        fun action(id: FamilySafeWordResetActionId): FamilySafeWordResetAction = actionFor(id)

        return listOf(
            FamilySafeWordResetCard(
                id = FamilySafeWordResetCardId.PAUSE_OLD_SECRET,
                displayOrder = FamilySafeWordResetCardId.PAUSE_OLD_SECRET.displayOrder,
                title = "이전 안전단어는 잠시 단독 근거로 믿지 않기",
                resetScenario =
                    "가족 확인 문구가 통화·메시지·화면에 드러났거나 누군가 들었을 수 있다고 느끼는 때",
                valueFreeFamilySentence =
                    "이전 문구 하나만으로 상대를 믿지 않고 평소 합의한 다른 가족 경로로 다시 확인하겠습니다.",
                whyItMatters =
                    "확인 문구를 아는 것과 실제 가족인지 확인하는 것은 같지 않으므로 별도 경로와 함께 봐야 합니다.",
                doNotSayRecordOrShareBoundaries = listOf(
                    "이전 답이나 새 답, 힌트·철자·발음·비슷한 표현을 이 코치에 말하거나 적지 마세요.",
                    "누가 문구를 알았는지, 어떤 연락에서 나왔는지, 확인 결과가 어땠는지 저장하거나 공유하지 마세요."
                ),
                primaryAction = action(
                    FamilySafeWordResetActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                ),
                secondaryActions = listOf(
                    action(FamilySafeWordResetActionId.QUICK_STOP),
                    action(FamilySafeWordResetActionId.SAFE_CALL_SCRIPTS),
                    action(FamilySafeWordResetActionId.FAMILY_CALLBACK_CARD),
                    action(FamilySafeWordResetActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
                )
            ),
            FamilySafeWordResetCard(
                id = FamilySafeWordResetCardId.CHOOSE_SEPARATE_OFFLINE_CHECK,
                displayOrder =
                    FamilySafeWordResetCardId.CHOOSE_SEPARATE_OFFLINE_CHECK.displayOrder,
                title = "새 확인 질문은 같은 채팅·통화 안에서 정하지 않기",
                resetScenario =
                    "현재 상대가 보고 듣는 연락 경로에서 새 가족 확인 방법을 정하자고 하는 때",
                valueFreeFamilySentence =
                    "현재 연락을 끝낸 뒤 가족이 평소 정한 별도 방식에서 확인 규칙만 직접 정하겠습니다.",
                whyItMatters =
                    "의심되는 같은 경로에서 새 값을 정하면 그 값도 바로 노출될 수 있습니다.",
                doNotSayRecordOrShareBoundaries = listOf(
                    "같은 채팅·통화·링크·앱·화면공유 세션으로 새 질문이나 답을 보내지 마세요.",
                    "정확한 번호·주소·계정·프로필·앱 이름·기기 정보나 만날 장소를 이 코치에 적지 마세요."
                ),
                primaryAction = action(
                    FamilySafeWordResetActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                ),
                secondaryActions = listOf(
                    action(FamilySafeWordResetActionId.SAFE_CALL_SCRIPTS),
                    action(FamilySafeWordResetActionId.SAFETY_PLAN),
                    action(FamilySafeWordResetActionId.PRIVACY_CENTER),
                    action(FamilySafeWordResetActionId.RECONTACT_BOUNDARY_COACH)
                )
            ),
            FamilySafeWordResetCard(
                id = FamilySafeWordResetCardId.MANUAL_SAFETY_PLAN_EDIT,
                displayOrder = FamilySafeWordResetCardId.MANUAL_SAFETY_PLAN_EDIT.displayOrder,
                title = "가족 안전 계획은 직접 열어 새 값으로 바꾸기",
                resetScenario =
                    "가족이 별도 방식으로 확인 규칙을 정한 뒤 이 기기의 계획을 손으로 고치려는 때",
                valueFreeFamilySentence =
                    "이 코치에는 값을 말하지 않고 가족 안전 계획을 직접 열어 필요한 칸만 손으로 고치겠습니다.",
                whyItMatters =
                    "코치와 계획 편집을 분리하면 추천·공유 문장에 실제 가족 값이 섞이지 않습니다.",
                doNotSayRecordOrShareBoundaries = listOf(
                    "이 코치는 계획의 안전단어·콜백 문장·연락처 이름·번호를 열어 보거나 대신 고치지 않습니다.",
                    "이 코치에서 계획 값을 복사·생성·검사·변환·삭제하거나 자동 교체했다고 표시하지 마세요."
                ),
                primaryAction = action(FamilySafeWordResetActionId.SAFETY_PLAN),
                secondaryActions = listOf(
                    action(FamilySafeWordResetActionId.FAMILY_READINESS_ROADMAP),
                    action(FamilySafeWordResetActionId.FAMILY_SAFETY_DRILL_COACH),
                    action(FamilySafeWordResetActionId.PRIVACY_CENTER),
                    action(FamilySafeWordResetActionId.SAFE_CALL_SCRIPTS)
                )
            ),
            FamilySafeWordResetCard(
                id = FamilySafeWordResetCardId.SHARE_RULE_NOT_VALUE,
                displayOrder = FamilySafeWordResetCardId.SHARE_RULE_NOT_VALUE.displayOrder,
                title = "가족에게는 새 값 대신 바뀔 규칙만 말하기",
                resetScenario =
                    "가족·보호자에게 확인 루틴을 다시 살펴본다는 사실을 알리려는 때",
                valueFreeFamilySentence =
                    "실제 질문과 답은 보내지 않고 이전 문구만 믿지 않는다는 규칙과 다음 행동 종류만 말하겠습니다.",
                whyItMatters =
                    "가족이 행동 변화는 함께 알아야 하지만 새 비밀 자체를 메시지로 돌려 보낼 필요는 없습니다.",
                doNotSayRecordOrShareBoundaries = listOf(
                    "가족 이름·받는 사람·연락처·새 질문·답·힌트·콜백 문장·보낸 상태를 적거나 공유하지 마세요.",
                    "누가 맡았는지, 읽었는지, 동의했는지, 어떤 값으로 정했는지 체크하거나 저장하지 마세요."
                ),
                primaryAction = action(FamilySafeWordResetActionId.FAMILY_ALERT_MESSAGES),
                secondaryActions = listOf(
                    action(FamilySafeWordResetActionId.FAMILY_CALM_DEBRIEF_AGENDA),
                    action(FamilySafeWordResetActionId.TRUSTED_HELPER_REVIEW),
                    action(FamilySafeWordResetActionId.FAMILY_RESPONSE_ROLE_BOARD),
                    action(FamilySafeWordResetActionId.EVIDENCE_HANDOFF_PRIVACY_COACH)
                )
            ),
            FamilySafeWordResetCard(
                id = FamilySafeWordResetCardId.VERIFY_AFTER_FAMILY_PRESSURE,
                displayOrder =
                    FamilySafeWordResetCardId.VERIFY_AFTER_FAMILY_PRESSURE.displayOrder,
                title = "새 번호·딥페이크·비밀 압박 뒤에는 별도 경로로 다시 확인하기",
                resetScenario =
                    "가족 위급 상황·낯선 번호·합성 음성·비밀 유지·돈 또는 인증 압박이 이어지는 때",
                valueFreeFamilySentence =
                    "현재 상대가 준 번호나 경로를 쓰지 않고 평소 가족 확인 경로에서 행동 종류만 다시 확인하겠습니다.",
                whyItMatters =
                    "목소리나 문구가 그럴듯해도 현재 상대가 통제하지 않는 경로가 추가로 필요합니다.",
                doNotSayRecordOrShareBoundaries = listOf(
                    "새 번호·정확한 연락처·기관 번호·URL·도메인·계정·카드·OTP·PIN·비밀번호를 이 카드에 적지 마세요.",
                    "사람·번호·기관·링크가 확인됐다는 상태나 돈·계정 조치 결과를 기록하지 마세요."
                ),
                primaryAction = action(
                    FamilySafeWordResetActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                ),
                secondaryActions = listOf(
                    action(FamilySafeWordResetActionId.FAMILY_CALLBACK_CARD),
                    action(FamilySafeWordResetActionId.SAFE_CALL_SCRIPTS),
                    action(FamilySafeWordResetActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD),
                    action(FamilySafeWordResetActionId.RECONTACT_BOUNDARY_COACH),
                    action(FamilySafeWordResetActionId.QUICK_STOP)
                )
            ),
            FamilySafeWordResetCard(
                id = FamilySafeWordResetCardId.FICTIONAL_REHEARSAL_ONLY,
                displayOrder =
                    FamilySafeWordResetCardId.FICTIONAL_REHEARSAL_ONLY.displayOrder,
                title = "연습은 가상 문장으로만 하고 실제 답은 말하지 않기",
                resetScenario =
                    "가족이 재정비한 확인 루틴을 차분하게 연습해 보려는 때",
                valueFreeFamilySentence =
                    "가상의 가족·문장·답으로 멈춤과 별도 확인 순서만 연습하겠습니다.",
                whyItMatters =
                    "실제 비밀을 반복해서 말하지 않아도 멈춤·콜백·가족 알림 순서를 익힐 수 있습니다.",
                doNotSayRecordOrShareBoundaries = listOf(
                    "실제 안전단어 답·콜백 문장·가족 이름·연락처·번호를 연습 예시로 쓰지 마세요.",
                    "연습 통과 여부·점수·체크·읽음·진행·메모·새 값 상태를 저장하지 마세요."
                ),
                primaryAction = action(
                    FamilySafeWordResetActionId.FAMILY_SAFETY_DRILL_COACH
                ),
                secondaryActions = listOf(
                    action(FamilySafeWordResetActionId.FAMILY_READINESS_ROADMAP),
                    action(FamilySafeWordResetActionId.SAFE_CALL_SCRIPTS),
                    action(FamilySafeWordResetActionId.SAFETY_FLOW_ROUTER),
                    action(FamilySafeWordResetActionId.FOLLOW_UP_CHECK_IN_PLANNER)
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
                    "현재 분석이 없거나 일치 신호가 없습니다. 어떤 비밀 상태도 단정하지 말고 " +
                        "모든 카드를 보수적으로 직접 읽으세요."
                )
            }
            if (hasHighRisk) {
                add(
                    "위험도가 높아 이전 문구 하나에 기대지 않고 별도 확인·가족 공유·돈 멈춤·" +
                        "계정·기기·증거·도움 경로를 함께 권합니다. 실제 노출이나 조치 결과를 " +
                        "판정하는 뜻은 아닙니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 위급 상황·목소리 합성·번호 변경 신호가 있어 이전 문구만 믿지 않고 " +
                        "별도 경로 확인·값 없는 확인 전화·가족 경고·콜백 카드·결과별 다음 단계·" +
                        "수동 계획 편집·가상 연습·준비 로드맵을 권합니다."
                )
            }
            if (hasSecrecyImpersonationSignal) {
                add(
                    "비밀 유지·사칭 신호가 있어 재정비 사실을 혼자 숨기지 말고 실제 값 대신 " +
                        "바뀔 규칙 종류만 가족·보호자와 나누며 증거 전달 경계·사용자가 직접 고를 " +
                        "도움·신고 준비 경로를 살펴보세요."
                )
            }
            if (hasFinancialSignal) {
                add(
                    "송금·결제 압박 신호가 있어 돈 행동 멈춤·계정 보호·원격지시 중단을 보조 " +
                        "경로로 권합니다. 송금·결제 취소, 계정·카드 정지·동결, 환급·회수·복구를 " +
                        "수행하거나 보장하지 않습니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증 비밀 요구 신호가 있어 계정 보호·원격지시 중단·노출 분류를 함께 " +
                        "권합니다. 계정값이나 인증값을 읽거나 비밀 정리를 대신하지 않습니다."
                )
            }
            if (hasLinkSignal) {
                add(
                    "링크·스미싱 신호가 있어 같은 링크·메시지 경로로 새 비밀을 보내지 말고 " +
                        "별도 경로·노출 분류·개인정보 경로를 권합니다. URL·도메인을 검증하거나 " +
                        "링크를 스캔하지 않습니다."
                )
            }
            if (hasRemoteAccessSignal) {
                add(
                    "앱 설치·화면공유·통화 유지 신호가 있어 같은 앱·통화·화면 세션을 벗어나는 " +
                        "원격지시 중단·노출 분류·개인정보·보호자 경로를 권합니다. 설치 앱 목록이나 " +
                        "화면을 읽지 않고 기기 검사·정리를 하지 않습니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만 반영하며 사건 내용이나 미리보기는 읽지 " +
                        "않습니다. 증거 전달 경계·증거 보존·후속 점검 경로만 함께 권합니다."
                )
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW ->
                "가족 준비가 부족한 범주라 계획을 직접 만들거나 고칠 순서, 준비 로드맵, 가상 연습, 값 없는 확인 전화 경로를 권합니다. 실제 가족 값은 이 코치에 입력하지 마세요."
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중인 범주라 계획을 직접 살피고 준비 로드맵·가상 연습·값 없는 확인 전화·콜백 경로로 빠진 순서를 점검하도록 권합니다. 저장된 실제 값은 추천에 사용하지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있다는 범주는 기존 항목이 채워졌다는 뜻일 뿐 확인 비밀이 노출되지 않았다는 증거가 아닙니다. 실제 답 없이 재정비 상황과 별도 확인 순서를 가상으로 연습하세요."
        }
        return listOf(situationCopy, readinessCopy).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 사용자가 고정 문장을 직접 읽고 기존 앱 경로를 직접 고르는 로컬 전용·무상태 수동 안내이며 실제 답이나 재정비 결과 입력을 요구하지 않습니다.",
        "추천은 현재 분석의 위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·사건 내용·미리보기·가족 안전 계획의 실제 값을 사용하지 않습니다.",
        "가족 안전 계획으로 이동하는 버튼은 사용자가 원할 때 기존 편집 화면을 열 뿐입니다. 이 코치는 계획의 안전단어·콜백 문장·연락처 이름·번호를 열어 보거나 표시·공유·수정·저장·삭제하지 않습니다.",
        "안전단어·질문·답·콜백 문장을 생성·추천·추론·변환·검사·검증·복사·동기화하거나 자동 교체하지 않고 이전 값이나 새 값의 상태를 판정하지 않습니다.",
        "카드 선택·추천·읽음·공유·체크·메모·이름·담당자·받는 사람·재정비·이전 값·새 값·연락처·채널·보낸 상태·진행·이력을 위한 새 저장소를 만들거나 기록하지 않습니다.",
        "이름·연락처·정확한 전화번호·계정·계좌·카드·OTP·PIN·비밀번호·가족 확인 답·주민번호·기기 식별자·정확한 URL·도메인·공식 번호·앱 이름·패키지 이름·파일·미디어 이름·경로·받는 사람·결과값을 묻거나 저장하지 않습니다.",
        "연락처·통화 기록·문자·메시지·통화 상태·알림·클립보드를 읽거나 감시하지 않고 연락처를 읽거나 쓰거나 고르지 않으며 전화·다이얼·통화 종료·SMS·받는 사람 선택·자동 전송을 하지 않습니다.",
        "파일·사진·이미지·미디어·저장소를 읽지 않고 카메라·화면 캡처·스크린샷 가져오기·OCR·이미지 분석·파일·문서·사진 선택기·첨부·스트림·URI 공유를 사용하지 않습니다.",
        "네트워크·웹 탐색·지도·위치·기관 번호 조회·공식 경로 조회·전화번호 검증·링크·URL·도메인 검증·링크 스캔을 사용하지 않습니다.",
        "새 Android 권한을 요청하지 않고 알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러를 만들거나 예약하지 않습니다.",
        "결제·은행 기능이나 API, NFC, Android 계정 관리자에 접근하지 않고 송금·결제·취소·계정 또는 카드 정지·동결·환급·회수·복구를 자동 수행하지 않습니다.",
        "기기 관리자·접근성 서비스·VPN·사용 기록·앱 작업 권한을 쓰지 않고 설치 앱·패키지 목록 조회, 앱 설치·제거, 설정 변경, 원격 연결 종료, 악성 프로그램 검사·제거, 기기 정리를 하지 않습니다.",
        "화면이나 경로·공유 버튼을 눌러도 붙여넣은 글, 통화·연락처·문자·알림, 사건 기록, 가족 안전 계획, 기관 목록, 계정·카드·은행·기기·앱·패키지·파일·미디어·네트워크·VPN 설정, 돈의 움직임, 확인·재연락·전달·재정비 상태를 추가·수정·삭제하지 않습니다.",
        "사람·번호·링크·기관·확인 비밀·기기 상태나 재정비 결과를 확인·보증하지 않으며 법률·금융 결과를 증명하거나 보장하지 않습니다. 자동 모니터링·스캔·OCR·삭제·가림·차단·호출·메시지 전송·조회·검증·저장·업로드·첨부·제출을 수행하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<FamilySafeWordResetCardId>,
        recommendedActions: List<FamilySafeWordResetAction>,
        cards: List<FamilySafeWordResetCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 가족 안전단어 재설정 코치")
            appendLine()
            appendLine("실제 질문과 답 없이 가족 확인 루틴의 수동 재정비 순서를 함께 보고 있습니다.")
            appendLine()
            appendLine("추천 재정비 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("가족에게 말할 값 없는 문장")
            appendLine("- 실제 답 대신 이전 문구 하나에 기대지 않는 규칙과 별도 확인 순서만 함께 살펴보세요.")
            append(
                "사람·연락처·번호·질문·답·힌트·콜백 문장·원문·사건 내용·링크·계정·" +
                    "인증·기기·파일·받는 사람·결과값 없이 고정된 카드와 기존 앱 경로 이름만 담았습니다."
            )
        }.trim()
    }

    private fun actionFor(
        id: FamilySafeWordResetActionId
    ): FamilySafeWordResetAction = FamilySafeWordResetAction(
        id = id,
        label = when (id) {
            FamilySafeWordResetActionId.QUICK_STOP -> "긴급 멈춤 모드"
            FamilySafeWordResetActionId.CHECKER -> "의심 내용 검사"
            FamilySafeWordResetActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            FamilySafeWordResetActionId.SAFETY_PLAN -> "가족 안전 계획 직접 편집"
            FamilySafeWordResetActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
            FamilySafeWordResetActionId.FAMILY_SAFETY_DRILL_COACH -> "가족 안전 연습 코치"
            FamilySafeWordResetActionId.FAMILY_CALLBACK_CARD -> "평소 가족 경로 콜백 카드"
            FamilySafeWordResetActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                "별도 경로 확인 코치"
            FamilySafeWordResetActionId.SAFE_CALL_SCRIPTS ->
                "값 없는 가족 콜백·확인 전화 스크립트"
            FamilySafeWordResetActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
            FamilySafeWordResetActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                "확인 결과별 다음 단계 보드"
            FamilySafeWordResetActionId.RECONTACT_BOUNDARY_COACH -> "재연락 경계 코치"
            FamilySafeWordResetActionId.TRUSTED_HELPER_REVIEW -> "보호자와 함께 안전 검토"
            FamilySafeWordResetActionId.FAMILY_RESPONSE_ROLE_BOARD -> "가족 대응 역할 보드"
            FamilySafeWordResetActionId.FAMILY_CALM_DEBRIEF_AGENDA -> "가족 차분 대화 안건"
            FamilySafeWordResetActionId.MONEY_TRANSFER_PAUSE_GATE -> "송금 전 멈춤 확인"
            FamilySafeWordResetActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
            FamilySafeWordResetActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                "화면공유·원격지시 끊기 코치"
            FamilySafeWordResetActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
            FamilySafeWordResetActionId.PRIVACY_CENTER -> "개인정보 센터"
            FamilySafeWordResetActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                "증거 전달 개인정보 코치"
            FamilySafeWordResetActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            FamilySafeWordResetActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처"
            FamilySafeWordResetActionId.REPORT_PREP -> "신고용 상담 준비 카드"
            FamilySafeWordResetActionId.FOLLOW_UP_CHECK_IN_PLANNER -> "며칠 뒤 다시 확인할 것"
            FamilySafeWordResetActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
