package com.mongmong.familyshield

enum class VoiceImpersonationRealityCheckCardId(val displayOrder: Int) {
    VOICE_IS_NOT_PROOF(1),
    STOP_SAME_CALL_EXCHANGE(2),
    VERIFY_THROUGH_FAMILY_ROUTE(3),
    KEEP_VOICE_ARTIFACTS_OUT(4),
    PAUSE_MONEY_CREDENTIAL_OR_APP(5),
    SHARE_ACTION_CATEGORIES_ONLY(6)
}

enum class VoiceImpersonationRealityCheckActionId {
    QUICK_STOP,
    CHECKER,
    ANALYSIS_RESULT,
    SAFETY_PLAN,
    FAMILY_READINESS_ROADMAP,
    FAMILY_SAFETY_DRILL_COACH,
    FAMILY_CALLBACK_CARD,
    FAMILY_SAFE_WORD_RESET_COACH,
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

data class VoiceImpersonationRealityCheckAction(
    val id: VoiceImpersonationRealityCheckActionId,
    val label: String
)

data class VoiceImpersonationRealityCheckCard(
    val id: VoiceImpersonationRealityCheckCardId,
    val displayOrder: Int,
    val title: String,
    val realityCheckScenario: String,
    val valueFreeActionSentence: String,
    val whyItMatters: String,
    val doNotRecordShareOrAskBoundaries: List<String>,
    val primaryAction: VoiceImpersonationRealityCheckAction,
    val secondaryActions: List<VoiceImpersonationRealityCheckAction>
) {
    val routeActions: List<VoiceImpersonationRealityCheckAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class VoiceImpersonationRealityCheckCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<VoiceImpersonationRealityCheckCardId>,
    val recommendedActions: List<VoiceImpersonationRealityCheckAction>,
    val cards: List<VoiceImpersonationRealityCheckCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<VoiceImpersonationRealityCheckCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<VoiceImpersonationRealityCheckActionId>
        get() = recommendedActions.map { action -> action.id }
}

object VoiceImpersonationRealityCheckCoachBuilder {
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
    ): VoiceImpersonationRealityCheckCoach {
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
            VoiceImpersonationRealityCheckCardId.VOICE_IS_NOT_PROOF,
            VoiceImpersonationRealityCheckCardId.KEEP_VOICE_ARTIFACTS_OUT
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += VoiceImpersonationRealityCheckCardId.entries
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet += VoiceImpersonationRealityCheckCardId.VOICE_IS_NOT_PROOF
            recommendedCardSet += VoiceImpersonationRealityCheckCardId.STOP_SAME_CALL_EXCHANGE
            recommendedCardSet +=
                VoiceImpersonationRealityCheckCardId.VERIFY_THROUGH_FAMILY_ROUTE
            recommendedCardSet += VoiceImpersonationRealityCheckCardId.SHARE_ACTION_CATEGORIES_ONLY
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedCardSet += VoiceImpersonationRealityCheckCardId.STOP_SAME_CALL_EXCHANGE
            recommendedCardSet += VoiceImpersonationRealityCheckCardId.SHARE_ACTION_CATEGORIES_ONLY
        }
        if (hasFinancialSignal || hasCredentialSignal) {
            recommendedCardSet += VoiceImpersonationRealityCheckCardId.STOP_SAME_CALL_EXCHANGE
            recommendedCardSet +=
                VoiceImpersonationRealityCheckCardId.PAUSE_MONEY_CREDENTIAL_OR_APP
        }
        if (hasLinkSignal || hasRemoteAccessSignal) {
            recommendedCardSet += VoiceImpersonationRealityCheckCardId.STOP_SAME_CALL_EXCHANGE
            recommendedCardSet += VoiceImpersonationRealityCheckCardId.KEEP_VOICE_ARTIFACTS_OUT
            recommendedCardSet +=
                VoiceImpersonationRealityCheckCardId.PAUSE_MONEY_CREDENTIAL_OR_APP
        }
        if (hasSavedIncident) {
            recommendedCardSet += VoiceImpersonationRealityCheckCardId.KEEP_VOICE_ARTIFACTS_OUT
            recommendedCardSet += VoiceImpersonationRealityCheckCardId.SHARE_ACTION_CATEGORIES_ONLY
        }
        when (readiness.level) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedCardSet +=
                    VoiceImpersonationRealityCheckCardId.VERIFY_THROUGH_FAMILY_ROUTE
                recommendedCardSet += VoiceImpersonationRealityCheckCardId.STOP_SAME_CALL_EXCHANGE
            }
            ReadinessLevel.READY -> {
                recommendedCardSet += VoiceImpersonationRealityCheckCardId.VOICE_IS_NOT_PROOF
                recommendedCardSet +=
                    VoiceImpersonationRealityCheckCardId.VERIFY_THROUGH_FAMILY_ROUTE
            }
        }
        val recommendedCardIds = VoiceImpersonationRealityCheckCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            VoiceImpersonationRealityCheckActionId.CHECKER
        } else {
            VoiceImpersonationRealityCheckActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            currentResultActionId,
            VoiceImpersonationRealityCheckActionId.QUICK_STOP,
            VoiceImpersonationRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
            VoiceImpersonationRealityCheckActionId.SAFE_CALL_SCRIPTS,
            VoiceImpersonationRealityCheckActionId.PRIVACY_CENTER,
            VoiceImpersonationRealityCheckActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            VoiceImpersonationRealityCheckActionId.entries
                .filterNot { actionId ->
                    actionId == VoiceImpersonationRealityCheckActionId.ANALYSIS_RESULT
                }
                .forEach { actionId -> recommendedActionSet += actionId }
        }
        if (hasHighRisk) {
            VoiceImpersonationRealityCheckActionId.entries.forEach { actionId ->
                recommendedActionSet += actionId
            }
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet +=
                VoiceImpersonationRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet +=
                VoiceImpersonationRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH
            recommendedActionSet +=
                VoiceImpersonationRealityCheckActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet +=
                VoiceImpersonationRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                VoiceImpersonationRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.PRIVACY_CENTER
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.REPORT_PREP
        }
        if (hasFinancialSignal || hasCredentialSignal) {
            recommendedActionSet +=
                VoiceImpersonationRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                VoiceImpersonationRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE
        }
        if (hasLinkSignal) {
            recommendedActionSet +=
                VoiceImpersonationRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet +=
                VoiceImpersonationRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.PRIVACY_CENTER
        }
        if (hasRemoteAccessSignal) {
            recommendedActionSet +=
                VoiceImpersonationRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.PRIVACY_CENTER
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.TRUSTED_HELPER_REVIEW
        }
        if (hasSavedIncident) {
            recommendedActionSet +=
                VoiceImpersonationRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += VoiceImpersonationRealityCheckActionId.EVIDENCE_KIT
            recommendedActionSet +=
                VoiceImpersonationRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        when (readiness.level) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedActionSet += VoiceImpersonationRealityCheckActionId.SAFETY_PLAN
                recommendedActionSet +=
                    VoiceImpersonationRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH
                recommendedActionSet +=
                    VoiceImpersonationRealityCheckActionId.FAMILY_READINESS_ROADMAP
                recommendedActionSet +=
                    VoiceImpersonationRealityCheckActionId.FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += VoiceImpersonationRealityCheckActionId.SAFE_CALL_SCRIPTS
            }
            ReadinessLevel.READY -> {
                recommendedActionSet += VoiceImpersonationRealityCheckActionId.FAMILY_CALLBACK_CARD
                recommendedActionSet +=
                    VoiceImpersonationRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                recommendedActionSet += VoiceImpersonationRealityCheckActionId.SAFE_CALL_SCRIPTS
            }
        }
        val recommendedActions = VoiceImpersonationRealityCheckActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null -> "검사 결과가 없어도 모든 목소리 현실 점검 카드를 보수적으로 읽기"
            hasFamilyVerificationSignal -> "익숙한 소리도 단독 근거로 삼지 않고 가족 경로로 다시 확인하기"
            hasRemoteAccessSignal -> "같은 통화·앱·화면공유 흐름을 벗어나 다음 행동 고르기"
            hasLinkSignal -> "같은 링크·메시지·통화 흐름에서 확인을 이어가지 않기"
            hasCredentialSignal -> "목소리에 기대지 않고 인증·계정 행동부터 멈추기"
            hasFinancialSignal -> "목소리에 기대지 않고 돈 행동부터 멈추기"
            hasSecrecyImpersonationSignal -> "통화를 비밀로 두지 않고 가족·보호자에게 행동만 알리기"
            hasHighRisk -> "높은 위험 신호에서 목소리 외의 확인 경로를 넓게 고르기"
            hasSavedIncident -> "저장 사건 내용 없이 목소리 현실 점검과 후속 경로 고르기"
            else -> "목소리를 증거로 단정하지 않고 별도 확인 경로 고르기"
        }

        return VoiceImpersonationRealityCheckCoach(
            title = "목소리 사칭 현실 점검 코치",
            intro =
                "들리는 목소리가 아주 익숙해도 그 소리만 근거로 삼지 않고, 녹음하거나 " +
                    "파일을 넣지 않은 채 멈춤·별도 확인·가족 공유 순서를 직접 읽고 기존 앱 " +
                    "경로만 고르는 로컬 전용·무상태 수동 안내입니다.",
            modeLabel = "녹음 없이 · 목소리 대신 행동 확인",
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

    private fun buildCards(): List<VoiceImpersonationRealityCheckCard> {
        fun action(
            id: VoiceImpersonationRealityCheckActionId
        ): VoiceImpersonationRealityCheckAction = actionFor(id)

        return listOf(
            VoiceImpersonationRealityCheckCard(
                id = VoiceImpersonationRealityCheckCardId.VOICE_IS_NOT_PROOF,
                displayOrder =
                    VoiceImpersonationRealityCheckCardId.VOICE_IS_NOT_PROOF.displayOrder,
                title = "익숙한 목소리도 증거로 단정하지 않기",
                realityCheckScenario =
                    "자녀·부모·가족·기관 담당자처럼 아주 익숙한 목소리가 급한 부탁을 하는 때",
                valueFreeActionSentence =
                    "목소리만으로 상대를 정하지 않고 현재 연락 밖의 평소 확인 순서로 이동하겠습니다.",
                whyItMatters =
                    "말투와 감정이 익숙하게 들리는 것만으로 통화 상대의 신원을 증명할 수는 없습니다.",
                doNotRecordShareOrAskBoundaries = listOf(
                    "상대에게 목소리를 더 들려 달라거나 가족만 아는 새 답을 말해 보라고 요구하지 마세요.",
                    "목소리를 녹음·재생·비교하거나 소리 특징과 사람 상태를 이 코치에 적지 마세요."
                ),
                primaryAction = action(
                    VoiceImpersonationRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                ),
                secondaryActions = listOf(
                    action(VoiceImpersonationRealityCheckActionId.FAMILY_CALLBACK_CARD),
                    action(VoiceImpersonationRealityCheckActionId.SAFE_CALL_SCRIPTS),
                    action(
                        VoiceImpersonationRealityCheckActionId
                            .VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    ),
                    action(VoiceImpersonationRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH)
                )
            ),
            VoiceImpersonationRealityCheckCard(
                id = VoiceImpersonationRealityCheckCardId.STOP_SAME_CALL_EXCHANGE,
                displayOrder =
                    VoiceImpersonationRealityCheckCardId.STOP_SAME_CALL_EXCHANGE.displayOrder,
                title = "통화 안에서 새 질문·답·돈 이야기를 이어가지 않기",
                realityCheckScenario =
                    "상대가 통화를 유지한 채 새 확인 질문·돈·인증·비밀 이야기를 재촉하는 때",
                valueFreeActionSentence =
                    "현재 대화를 끝내고 같은 상대가 제시한 경로에서는 추가 대답이나 행동을 하지 않겠습니다.",
                whyItMatters =
                    "같은 통화에서 정보를 더 주고받으면 상대가 다음 압박에 쓸 단서와 소리를 더 얻을 수 있습니다.",
                doNotRecordShareOrAskBoundaries = listOf(
                    "이름·관계·별명·안전단어·콜백 문장·계정·카드·OTP·PIN·비밀번호를 묻거나 말하지 마세요.",
                    "통화를 시험하려고 더 오래 유지하거나 따라 말하기·반복 발화·음성 샘플 제공을 요청하지 마세요."
                ),
                primaryAction = action(VoiceImpersonationRealityCheckActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(VoiceImpersonationRealityCheckActionId.RECONTACT_BOUNDARY_COACH),
                    action(
                        VoiceImpersonationRealityCheckActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(VoiceImpersonationRealityCheckActionId.SAFE_CALL_SCRIPTS),
                    action(VoiceImpersonationRealityCheckActionId.PRIVACY_CENTER)
                )
            ),
            VoiceImpersonationRealityCheckCard(
                id = VoiceImpersonationRealityCheckCardId.VERIFY_THROUGH_FAMILY_ROUTE,
                displayOrder =
                    VoiceImpersonationRealityCheckCardId.VERIFY_THROUGH_FAMILY_ROUTE.displayOrder,
                title = "목소리 대신 평소 가족 경로로 다시 확인하기",
                realityCheckScenario =
                    "가족 위급 상황·번호 변경·익숙한 기관 목소리처럼 들려 마음이 급해지는 때",
                valueFreeActionSentence =
                    "현재 상대가 알려 준 번호나 링크가 아닌 평소 가족 확인 경로를 직접 선택하겠습니다.",
                whyItMatters =
                    "현재 상대가 통제하지 않는 별도 경로를 쓰면 목소리 이외의 행동 확인 단계를 더할 수 있습니다.",
                doNotRecordShareOrAskBoundaries = listOf(
                    "정확한 가족 이름·연락처·전화번호·안전단어 답·콜백 문장을 이 카드에 넣지 마세요.",
                    "새 번호·기관 번호·URL·도메인을 이 코치에 적거나 번호·링크 판정을 요청하지 마세요."
                ),
                primaryAction = action(
                    VoiceImpersonationRealityCheckActionId.FAMILY_CALLBACK_CARD
                ),
                secondaryActions = listOf(
                    action(
                        VoiceImpersonationRealityCheckActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(VoiceImpersonationRealityCheckActionId.SAFE_CALL_SCRIPTS),
                    action(VoiceImpersonationRealityCheckActionId.FAMILY_ALERT_MESSAGES),
                    action(VoiceImpersonationRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH),
                    action(
                        VoiceImpersonationRealityCheckActionId
                            .VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    )
                )
            ),
            VoiceImpersonationRealityCheckCard(
                id = VoiceImpersonationRealityCheckCardId.KEEP_VOICE_ARTIFACTS_OUT,
                displayOrder =
                    VoiceImpersonationRealityCheckCardId.KEEP_VOICE_ARTIFACTS_OUT.displayOrder,
                title = "목소리 샘플·녹음·스크린샷을 앱에 넣지 않기",
                realityCheckScenario =
                    "통화 소리나 관련 화면을 저장해 이 앱에서 판별받고 싶어지는 때",
                valueFreeActionSentence =
                    "소리·파일·이미지는 넣지 않고 고정된 현실 점검 문장과 기존 대응 경로만 읽겠습니다.",
                whyItMatters =
                    "이 코치는 소리나 파일을 처리하지 않으며 민감한 통화 내용을 더 복제할 필요가 없습니다.",
                doNotRecordShareOrAskBoundaries = listOf(
                    "녹음 파일·음성 메시지·통화 녹취·자막·전사문·스크린샷·사진·영상을 만들거나 넣지 마세요.",
                    "파일 이름·미디어 경로·URI·첨부물·소리 특징·화자 추정값을 적거나 공유하지 마세요."
                ),
                primaryAction = action(VoiceImpersonationRealityCheckActionId.PRIVACY_CENTER),
                secondaryActions = listOf(
                    action(
                        VoiceImpersonationRealityCheckActionId
                            .EVIDENCE_HANDOFF_PRIVACY_COACH
                    ),
                    action(VoiceImpersonationRealityCheckActionId.EVIDENCE_KIT),
                    action(VoiceImpersonationRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(VoiceImpersonationRealityCheckActionId.TRUSTED_HELPER_REVIEW)
                )
            ),
            VoiceImpersonationRealityCheckCard(
                id = VoiceImpersonationRealityCheckCardId.PAUSE_MONEY_CREDENTIAL_OR_APP,
                displayOrder =
                    VoiceImpersonationRealityCheckCardId.PAUSE_MONEY_CREDENTIAL_OR_APP.displayOrder,
                title = "돈·인증·앱 설치 요구가 붙으면 먼저 멈추기",
                realityCheckScenario =
                    "익숙한 목소리에 송금·결제·인증번호·링크·앱 설치·화면공유 요구가 붙는 때",
                valueFreeActionSentence =
                    "요구를 진행하지 않고 돈 멈춤·계정 보호·원격지시 중단 경로를 직접 고르겠습니다.",
                whyItMatters =
                    "목소리의 익숙함과 돈·계정·기기 행동을 허용해도 되는지는 별개의 문제입니다.",
                doNotRecordShareOrAskBoundaries = listOf(
                    "계좌·카드·OTP·PIN·비밀번호·주민번호·기기 식별자·앱 이름·패키지 이름을 적지 마세요.",
                    "송금·결제·계정·카드·원격 연결·기기 정리 결과를 입력하거나 자동 처리를 기대하지 마세요."
                ),
                primaryAction = action(
                    VoiceImpersonationRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE
                ),
                secondaryActions = listOf(
                    action(VoiceImpersonationRealityCheckActionId.ACCOUNT_LOCKDOWN),
                    action(
                        VoiceImpersonationRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH
                    ),
                    action(VoiceImpersonationRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(VoiceImpersonationRealityCheckActionId.QUICK_STOP)
                )
            ),
            VoiceImpersonationRealityCheckCard(
                id = VoiceImpersonationRealityCheckCardId.SHARE_ACTION_CATEGORIES_ONLY,
                displayOrder =
                    VoiceImpersonationRealityCheckCardId.SHARE_ACTION_CATEGORIES_ONLY.displayOrder,
                title = "가족에게는 목소리값 대신 행동 종류만 알리기",
                realityCheckScenario =
                    "가족·보호자에게 통화 상황과 다음 대응을 알리고 싶어지는 때",
                valueFreeActionSentence =
                    "사람·목소리·연락처·결과값 대신 멈춤·별도 확인·도움 요청 같은 행동 종류만 나누겠습니다.",
                whyItMatters =
                    "가족이 대응 순서는 함께 볼 수 있지만 통화 소리나 민감한 값을 다시 퍼뜨릴 필요는 없습니다.",
                doNotRecordShareOrAskBoundaries = listOf(
                    "가족 이름·받는 사람·연락처·통화 내용·녹취·목소리 특징·확인 질문·답을 적거나 공유하지 마세요.",
                    "읽음·보낸 상태·담당자·상대 상태·확인 결과·메모·체크·진행 이력을 저장하지 마세요."
                ),
                primaryAction = action(
                    VoiceImpersonationRealityCheckActionId.FAMILY_ALERT_MESSAGES
                ),
                secondaryActions = listOf(
                    action(VoiceImpersonationRealityCheckActionId.TRUSTED_HELPER_REVIEW),
                    action(
                        VoiceImpersonationRealityCheckActionId.FAMILY_RESPONSE_ROLE_BOARD
                    ),
                    action(
                        VoiceImpersonationRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA
                    ),
                    action(
                        VoiceImpersonationRealityCheckActionId
                            .EVIDENCE_HANDOFF_PRIVACY_COACH
                    ),
                    action(VoiceImpersonationRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER)
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
                    "현재 검사 결과가 없거나 일치 신호가 없습니다. 목소리만으로 사람 여부나 " +
                        "합성 여부를 단정하지 말고 모든 카드를 보수적으로 직접 읽으세요."
                )
            }
            if (hasHighRisk) {
                add(
                    "위험도가 높아 멈춤·별도 확인·가족 공유·돈·계정·기기·증거·도움 경로를 " +
                        "넓게 권합니다. 통화 상대나 소리의 상태 또는 조치 결과를 판정하는 뜻은 " +
                        "아닙니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 위급 상황·합성 음성 의심·번호 변경 신호가 있어 목소리를 증거로 " +
                        "삼지 않고 가족 콜백·별도 경로·값 없는 확인 전화·가족 경고·안전단어 " +
                        "재정비·확인 뒤 다음 단계 경로를 권합니다."
                )
            }
            if (hasSecrecyImpersonationSignal) {
                add(
                    "비밀 유지·사칭 신호가 있어 같은 통화 안에서 숨기지 말고 가족 경고·차분 " +
                        "대화·보호자 검토·증거 전달 경계·개인정보·사용자가 직접 고를 도움·신고 " +
                        "준비 경로를 살펴보세요."
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
                        "권합니다. 계정·카드·인증값을 읽거나 비밀 정리를 대신하지 않습니다."
                )
            }
            if (hasLinkSignal) {
                add(
                    "링크·스미싱 신호가 있어 같은 링크·메시지·통화 흐름에서 계속하지 말고 " +
                        "별도 경로·원격지시 중단·노출 분류·개인정보 경로를 권합니다. URL·도메인을 " +
                        "판정하거나 링크를 스캔하지 않습니다."
                )
            }
            if (hasRemoteAccessSignal) {
                add(
                    "앱 설치·통화 유지·화면공유 신호가 있어 같은 앱·통화·화면 세션을 벗어나는 " +
                        "원격지시 중단·노출 분류·개인정보·보호자 경로를 권합니다. 설치 앱 목록이나 " +
                        "화면을 읽지 않고 원격 연결 종료·악성 프로그램 제거·기기 정리를 수행하지 " +
                        "않습니다."
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
                "가족 준비가 부족한 범주라 가족 안전 계획·안전단어 재정비·준비 로드맵·가상 연습·값 없는 확인 전화 경로를 권합니다. 실제 가족 값은 이 코치에 입력하지 마세요."
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중인 범주라 가족 안전 계획·안전단어 재정비·준비 로드맵·가상 연습·값 없는 확인 전화 경로로 빠진 순서를 직접 살펴보도록 권합니다. 저장된 실제 값은 추천에 사용하지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있다는 범주는 기존 준비 항목이 채워졌다는 뜻일 뿐 통화 상대의 신원을 증명하지 않습니다. 익숙한 목소리도 단독 근거로 삼지 말고 별도 가족 경로를 직접 고르세요."
        }
        return listOf(situationCopy, readinessCopy).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 사용자가 고정 문장을 직접 읽고 기존 앱 경로를 직접 고르는 로컬 전용·무상태 수동 안내이며 이름·목소리·통화 내용·결과값 입력을 요구하지 않습니다.",
        "추천은 현재 검사 결과의 위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기·가족 안전 계획의 실제 값을 사용하지 않습니다.",
        "가족 안전 계획과 안전단어 재정비로 이동하는 버튼은 기존 화면을 열 뿐이며 안전단어·콜백 문장·연락처 이름·번호를 읽거나 표시·공유·수정·저장·삭제하지 않습니다.",
        "마이크·음성 녹음·통화 녹음·소리 재생·통화 청취·통화 상태 감시·음성 인식·전사·자막 생성·화자 검증·목소리 대조·딥페이크 탐지·분류·사람 식별을 사용하거나 수행하지 않습니다.",
        "목소리의 사람 여부·합성 여부·일치 여부를 판정하거나 신원·기관·통화 상대를 증명하지 않으며 음성 샘플·녹음 파일·전사문·소리 특징을 묻거나 저장·공유하지 않습니다.",
        "카드 선택·추천·읽음·공유·체크·메모·이름·담당자·받는 사람·목소리 상태·통화 상태·확인·재연락·전달 결과·진행·이력을 위한 새 저장소를 만들거나 기록하지 않습니다.",
        "이름·연락처·정확한 전화번호·계정·계좌·카드·OTP·PIN·비밀번호·안전단어 답·콜백 문장·주민번호·기기 식별자·정확한 URL·도메인·기관 번호·앱·패키지·파일·미디어·오디오 이름·경로·받는 사람·결과값을 묻거나 저장하지 않습니다.",
        "연락처·통화 기록·문자·메시지·통화 상태·알림·클립보드를 읽거나 감시하지 않고 연락처를 읽거나 쓰거나 고르지 않으며 전화·다이얼·통화 종료·통화 차단·SMS·받는 사람 선택·자동 전송을 하지 않습니다.",
        "파일·사진·이미지·오디오·미디어·저장소를 읽지 않고 카메라·화면 캡처·스크린샷 가져오기·OCR·이미지 분석·파일·문서·사진·오디오 선택기·첨부·스트림·URI 공유를 사용하지 않습니다.",
        "네트워크·웹 탐색·지도·위치·기관 정보 조회·전화번호 판정·링크·URL·도메인 판정·링크 스캔·업로드·클라우드 백업·증거 동기화를 사용하지 않습니다.",
        "새 Android 권한을 요청하지 않고 알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러를 만들거나 예약하지 않습니다.",
        "결제·은행 기능이나 API, NFC, Android 계정 관리자에 접근하지 않고 송금·결제·취소·계정 또는 카드 정지·동결·환급·회수·복구를 자동 수행하지 않습니다.",
        "기기 관리자·접근성 서비스·VPN·사용 기록·앱 작업 권한을 쓰지 않고 설치 앱·패키지 목록 조회, 앱 설치·제거, 설정 변경, 원격 연결 종료, 악성 프로그램 검사·제거, 기기 정리를 하지 않습니다.",
        "화면이나 경로·공유 버튼을 눌러도 원문, 통화·연락처·문자·알림, 사건 기록, 가족 안전 계획, 기관 목록, 계정·카드·은행·기기·앱·패키지·파일·미디어·오디오·네트워크 설정, 돈의 움직임, 확인·재연락·전달 상태를 추가·수정·삭제하지 않습니다.",
        "사람·목소리·번호·링크·기관·계정·기기 상태나 대응 결과를 확인·보증하지 않으며 법률·금융 결과를 증명하거나 보장하지 않습니다. 자동 모니터링·녹음·청취·인식·전사·대조·탐지·분류·스캔·OCR·삭제·가림·차단·호출·메시지 전송·조회·판정·저장·업로드·첨부·제출을 수행하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<VoiceImpersonationRealityCheckCardId>,
        recommendedActions: List<VoiceImpersonationRealityCheckAction>,
        cards: List<VoiceImpersonationRealityCheckCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 목소리 사칭 현실 점검 코치")
            appendLine()
            appendLine("목소리를 증거로 단정하지 않고 값 없는 수동 점검 순서를 함께 보고 있습니다.")
            appendLine()
            appendLine("추천 현실 점검 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("가족에게 말할 값 없는 문장")
            appendLine("- 목소리 대신 멈춤·별도 확인·가족 공유 같은 행동 종류만 함께 살펴보세요.")
            append(
                "사람·연락처·번호·안전단어·콜백 문장·원문·사건 내용·링크·계정·인증·" +
                    "기기·앱·파일·오디오·받는 사람·결과값 없이 고정된 카드와 기존 앱 경로 " +
                    "이름만 담았습니다."
            )
        }.trim()
    }

    private fun actionFor(
        id: VoiceImpersonationRealityCheckActionId
    ): VoiceImpersonationRealityCheckAction = VoiceImpersonationRealityCheckAction(
        id = id,
        label = when (id) {
            VoiceImpersonationRealityCheckActionId.QUICK_STOP -> "긴급 멈춤 모드"
            VoiceImpersonationRealityCheckActionId.CHECKER -> "의심 내용 검사"
            VoiceImpersonationRealityCheckActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            VoiceImpersonationRealityCheckActionId.SAFETY_PLAN -> "가족 안전 계획"
            VoiceImpersonationRealityCheckActionId.FAMILY_READINESS_ROADMAP ->
                "가족 준비 로드맵"
            VoiceImpersonationRealityCheckActionId.FAMILY_SAFETY_DRILL_COACH ->
                "가족 안전 연습 코치"
            VoiceImpersonationRealityCheckActionId.FAMILY_CALLBACK_CARD ->
                "평소 가족 경로 콜백 카드"
            VoiceImpersonationRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                "가족 안전단어 재설정 코치"
            VoiceImpersonationRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                "별도 경로 확인 코치"
            VoiceImpersonationRealityCheckActionId.SAFE_CALL_SCRIPTS ->
                "값 없는 가족 콜백·확인 전화 스크립트"
            VoiceImpersonationRealityCheckActionId.FAMILY_ALERT_MESSAGES ->
                "가족 경고 메시지 팩"
            VoiceImpersonationRealityCheckActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                "확인 결과별 다음 단계 보드"
            VoiceImpersonationRealityCheckActionId.RECONTACT_BOUNDARY_COACH ->
                "재연락 경계 코치"
            VoiceImpersonationRealityCheckActionId.TRUSTED_HELPER_REVIEW ->
                "보호자와 함께 안전 검토"
            VoiceImpersonationRealityCheckActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                "가족 대응 역할 보드"
            VoiceImpersonationRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                "가족 차분 대화 안건"
            VoiceImpersonationRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE ->
                "송금 전 멈춤 확인"
            VoiceImpersonationRealityCheckActionId.ACCOUNT_LOCKDOWN ->
                "계정·기기 잠금 체크리스트"
            VoiceImpersonationRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                "화면공유·원격지시 끊기 코치"
            VoiceImpersonationRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE ->
                "노출 영향 분류"
            VoiceImpersonationRealityCheckActionId.PRIVACY_CENTER -> "개인정보 센터"
            VoiceImpersonationRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                "증거 전달 개인정보 코치"
            VoiceImpersonationRealityCheckActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            VoiceImpersonationRealityCheckActionId.OFFICIAL_HELP_DIRECTORY ->
                "신고·상담 연락처"
            VoiceImpersonationRealityCheckActionId.REPORT_PREP -> "신고용 상담 준비 카드"
            VoiceImpersonationRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                "며칠 뒤 다시 확인할 것"
            VoiceImpersonationRealityCheckActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
