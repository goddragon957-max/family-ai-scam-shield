package com.mongmong.familyshield

enum class NewNumberQuarantineCardId(val displayOrder: Int) {
    DO_NOT_SAVE_OR_REPLACE_NEW_CHANNEL(1),
    STOP_SAME_CHANNEL_EXCHANGE(2),
    VERIFY_THROUGH_ESTABLISHED_FAMILY_ROUTE(3),
    SHARE_ACTION_CATEGORIES_ONLY(4),
    KEEP_PLAN_AND_SAFE_WORD_UNCHANGED(5),
    MANUAL_REVIEW_AFTER_VERIFICATION(6)
}

enum class NewNumberQuarantineActionId {
    QUICK_STOP,
    CHECKER,
    ANALYSIS_RESULT,
    SAFETY_PLAN,
    FAMILY_READINESS_ROADMAP,
    FAMILY_SAFETY_DRILL_COACH,
    ACTIVE_CONTACT_EXIT_SCRIPTS,
    FAMILY_CALLBACK_CARD,
    FAMILY_SAFE_WORD_RESET_COACH,
    SEPARATE_CHANNEL_VERIFICATION_COACH,
    SAFE_CALL_SCRIPTS,
    VOICE_IMPERSONATION_REALITY_CHECK_COACH,
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

data class NewNumberQuarantineAction(
    val id: NewNumberQuarantineActionId,
    val label: String
)

data class NewNumberQuarantineCard(
    val id: NewNumberQuarantineCardId,
    val displayOrder: Int,
    val title: String,
    val quarantineScenario: String,
    val valueFreeActionSentence: String,
    val whyItMatters: String,
    val doNotSaveContactOrValueBoundaries: List<String>,
    val primaryAction: NewNumberQuarantineAction,
    val secondaryActions: List<NewNumberQuarantineAction>
) {
    val routeActions: List<NewNumberQuarantineAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class NewNumberQuarantineCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<NewNumberQuarantineCardId>,
    val recommendedActions: List<NewNumberQuarantineAction>,
    val cards: List<NewNumberQuarantineCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<NewNumberQuarantineCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<NewNumberQuarantineActionId>
        get() = recommendedActions.map { action -> action.id }
}

object NewNumberQuarantineCoachBuilder {
    private val familyVerificationSignals = setOf(
        ScamSignal.PHONE_NUMBER_CHANGE,
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE
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
    ): NewNumberQuarantineCoach {
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
            NewNumberQuarantineCardId.DO_NOT_SAVE_OR_REPLACE_NEW_CHANNEL,
            NewNumberQuarantineCardId.STOP_SAME_CHANNEL_EXCHANGE
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += NewNumberQuarantineCardId.entries
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet +=
                NewNumberQuarantineCardId.DO_NOT_SAVE_OR_REPLACE_NEW_CHANNEL
            recommendedCardSet += NewNumberQuarantineCardId.STOP_SAME_CHANNEL_EXCHANGE
            recommendedCardSet +=
                NewNumberQuarantineCardId.VERIFY_THROUGH_ESTABLISHED_FAMILY_ROUTE
            recommendedCardSet += NewNumberQuarantineCardId.SHARE_ACTION_CATEGORIES_ONLY
            recommendedCardSet +=
                NewNumberQuarantineCardId.KEEP_PLAN_AND_SAFE_WORD_UNCHANGED
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedCardSet += NewNumberQuarantineCardId.STOP_SAME_CHANNEL_EXCHANGE
            recommendedCardSet += NewNumberQuarantineCardId.SHARE_ACTION_CATEGORIES_ONLY
        }
        if (hasFinancialSignal || hasCredentialSignal) {
            recommendedCardSet += NewNumberQuarantineCardId.STOP_SAME_CHANNEL_EXCHANGE
            recommendedCardSet +=
                NewNumberQuarantineCardId.DO_NOT_SAVE_OR_REPLACE_NEW_CHANNEL
        }
        if (hasLinkSignal || hasRemoteAccessSignal) {
            recommendedCardSet += NewNumberQuarantineCardId.STOP_SAME_CHANNEL_EXCHANGE
            recommendedCardSet +=
                NewNumberQuarantineCardId.DO_NOT_SAVE_OR_REPLACE_NEW_CHANNEL
            recommendedCardSet += NewNumberQuarantineCardId.SHARE_ACTION_CATEGORIES_ONLY
        }
        if (hasSavedIncident) {
            recommendedCardSet += NewNumberQuarantineCardId.SHARE_ACTION_CATEGORIES_ONLY
            recommendedCardSet += NewNumberQuarantineCardId.MANUAL_REVIEW_AFTER_VERIFICATION
        }
        when (readiness.level) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedCardSet +=
                    NewNumberQuarantineCardId.VERIFY_THROUGH_ESTABLISHED_FAMILY_ROUTE
                recommendedCardSet +=
                    NewNumberQuarantineCardId.KEEP_PLAN_AND_SAFE_WORD_UNCHANGED
                recommendedCardSet +=
                    NewNumberQuarantineCardId.MANUAL_REVIEW_AFTER_VERIFICATION
            }
            ReadinessLevel.READY -> {
                recommendedCardSet +=
                    NewNumberQuarantineCardId.DO_NOT_SAVE_OR_REPLACE_NEW_CHANNEL
                recommendedCardSet +=
                    NewNumberQuarantineCardId.VERIFY_THROUGH_ESTABLISHED_FAMILY_ROUTE
                recommendedCardSet +=
                    NewNumberQuarantineCardId.MANUAL_REVIEW_AFTER_VERIFICATION
            }
        }
        val recommendedCardIds = NewNumberQuarantineCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            NewNumberQuarantineActionId.CHECKER
        } else {
            NewNumberQuarantineActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            currentResultActionId,
            NewNumberQuarantineActionId.QUICK_STOP,
            NewNumberQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
            NewNumberQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
            NewNumberQuarantineActionId.PRIVACY_CENTER,
            NewNumberQuarantineActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            NewNumberQuarantineActionId.entries
                .filterNot { actionId ->
                    actionId == NewNumberQuarantineActionId.ANALYSIS_RESULT
                }
                .forEach { actionId -> recommendedActionSet += actionId }
        }
        if (hasHighRisk) {
            NewNumberQuarantineActionId.entries.forEach { actionId ->
                recommendedActionSet += actionId
            }
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += NewNumberQuarantineActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet +=
                NewNumberQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += NewNumberQuarantineActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet +=
                NewNumberQuarantineActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH
            recommendedActionSet += NewNumberQuarantineActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet +=
                NewNumberQuarantineActionId.FAMILY_SAFE_WORD_RESET_COACH
            recommendedActionSet +=
                NewNumberQuarantineActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedActionSet += NewNumberQuarantineActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet +=
                NewNumberQuarantineActionId.FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet += NewNumberQuarantineActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                NewNumberQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += NewNumberQuarantineActionId.PRIVACY_CENTER
            recommendedActionSet += NewNumberQuarantineActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += NewNumberQuarantineActionId.REPORT_PREP
        }
        if (hasFinancialSignal || hasCredentialSignal) {
            recommendedActionSet +=
                NewNumberQuarantineActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += NewNumberQuarantineActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                NewNumberQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += NewNumberQuarantineActionId.EXPOSURE_IMPACT_TRIAGE
        }
        if (hasLinkSignal) {
            recommendedActionSet += NewNumberQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                NewNumberQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet +=
                NewNumberQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += NewNumberQuarantineActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += NewNumberQuarantineActionId.PRIVACY_CENTER
        }
        if (hasRemoteAccessSignal) {
            recommendedActionSet += NewNumberQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                NewNumberQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += NewNumberQuarantineActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += NewNumberQuarantineActionId.PRIVACY_CENTER
            recommendedActionSet += NewNumberQuarantineActionId.TRUSTED_HELPER_REVIEW
        }
        if (hasSavedIncident) {
            recommendedActionSet +=
                NewNumberQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += NewNumberQuarantineActionId.EVIDENCE_KIT
            recommendedActionSet +=
                NewNumberQuarantineActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        when (readiness.level) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedActionSet += NewNumberQuarantineActionId.SAFETY_PLAN
                recommendedActionSet +=
                    NewNumberQuarantineActionId.FAMILY_SAFE_WORD_RESET_COACH
                recommendedActionSet +=
                    NewNumberQuarantineActionId.FAMILY_READINESS_ROADMAP
                recommendedActionSet +=
                    NewNumberQuarantineActionId.FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += NewNumberQuarantineActionId.SAFE_CALL_SCRIPTS
            }
            ReadinessLevel.READY -> {
                recommendedActionSet += NewNumberQuarantineActionId.FAMILY_CALLBACK_CARD
                recommendedActionSet +=
                    NewNumberQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                recommendedActionSet += NewNumberQuarantineActionId.SAFE_CALL_SCRIPTS
                recommendedActionSet +=
                    NewNumberQuarantineActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
            }
        }
        val recommendedActions = NewNumberQuarantineActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null -> "검사 결과가 없어도 모든 새 연락 경로 격리 카드를 보수적으로 읽기"
            hasFamilyVerificationSignal ->
                "새 번호·새 프로필을 저장하지 않고 평소 가족 경로로 직접 확인하기"
            hasRemoteAccessSignal ->
                "같은 새 연락·앱·통화·화면공유 흐름을 벗어나 다음 행동 고르기"
            hasLinkSignal -> "같은 새 연락·링크 흐름에서 확인을 이어가지 않기"
            hasCredentialSignal -> "새 연락 안의 인증·계정 행동부터 멈추기"
            hasFinancialSignal -> "새 연락 안의 돈 행동부터 멈추기"
            hasSecrecyImpersonationSignal ->
                "새 연락을 비밀로 두지 않고 가족·보호자에게 행동만 알리기"
            hasHighRisk -> "높은 위험 신호에서 새 연락 밖의 대응 경로를 넓게 고르기"
            hasSavedIncident -> "저장 사건 내용 없이 새 연락 격리와 후속 경로 고르기"
            else -> "새 연락을 바로 믿거나 바꾸지 않고 별도 확인 경로 고르기"
        }

        return NewNumberQuarantineCoach(
            title = "새 번호 격리 코치",
            intro =
                "새 번호·새 프로필·새 계정이라고 알려 온 연락을 바로 저장하거나 기존 연락처와 " +
                    "바꾸지 않고, 그 연락 밖에서 확인할 때까지 멈춤·가족 확인·수동 정리 순서를 " +
                    "직접 읽고 기존 앱 경로만 고르는 로컬 전용·무상태 수동 안내입니다.",
            modeLabel = "연락처를 건드리지 않고 · 새 연락 밖에서 확인",
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

    private fun buildCards(): List<NewNumberQuarantineCard> {
        fun action(id: NewNumberQuarantineActionId): NewNumberQuarantineAction =
            actionFor(id)

        return listOf(
            NewNumberQuarantineCard(
                id = NewNumberQuarantineCardId.DO_NOT_SAVE_OR_REPLACE_NEW_CHANNEL,
                displayOrder =
                    NewNumberQuarantineCardId.DO_NOT_SAVE_OR_REPLACE_NEW_CHANNEL.displayOrder,
                title = "새 번호·새 프로필을 바로 저장하거나 바꾸지 않기",
                quarantineScenario =
                    "가족이 휴대폰을 잃어버렸거나 고장 났다며 새 번호·새 프로필만 쓰라고 하는 때",
                valueFreeActionSentence =
                    "확인 전에는 새 연락을 주소록에 넣거나 기존 가족 연락 경로와 바꾸지 않겠습니다.",
                whyItMatters =
                    "새 연락을 먼저 등록하면 이후 확인도 상대가 제시한 경로 안에서 반복하기 쉽습니다.",
                doNotSaveContactOrValueBoundaries = listOf(
                    "새 번호·예전 번호·프로필 이름·계정값·가족 이름을 이 코치에 적지 마세요.",
                    "연락처를 읽기·쓰기·편집·삽입·삭제·선택·병합·교체·저장·차단·음소거·동기화하지 않습니다."
                ),
                primaryAction = action(
                    NewNumberQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                ),
                secondaryActions = listOf(
                    action(NewNumberQuarantineActionId.FAMILY_CALLBACK_CARD),
                    action(NewNumberQuarantineActionId.SAFE_CALL_SCRIPTS),
                    action(
                        NewNumberQuarantineActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH
                    ),
                    action(
                        NewNumberQuarantineActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    )
                )
            ),
            NewNumberQuarantineCard(
                id = NewNumberQuarantineCardId.STOP_SAME_CHANNEL_EXCHANGE,
                displayOrder =
                    NewNumberQuarantineCardId.STOP_SAME_CHANNEL_EXCHANGE.displayOrder,
                title = "새 채팅·통화 안에서 돈·인증·앱 이야기를 이어가지 않기",
                quarantineScenario =
                    "새 연락이 송금·결제·인증값·링크·앱 설치·통화 유지·화면공유를 재촉하는 때",
                valueFreeActionSentence =
                    "현재 연락을 벗어나고 같은 새 채팅·링크·앱·통화 안에서는 추가 행동을 하지 않겠습니다.",
                whyItMatters =
                    "같은 연락 안에서 확인을 이어가면 상대가 확인 경로와 다음 행동을 계속 통제할 수 있습니다.",
                doNotSaveContactOrValueBoundaries = listOf(
                    "계정·계좌·카드·OTP·PIN·비밀번호·주민번호·링크·앱 이름·기기값을 말하거나 적지 마세요.",
                    "전화·문자·자동 답장·통화 종료·차단·결제·계정·기기 조치를 이 코치가 대신한다고 기대하지 마세요."
                ),
                primaryAction = action(
                    NewNumberQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
                ),
                secondaryActions = listOf(
                    action(NewNumberQuarantineActionId.QUICK_STOP),
                    action(NewNumberQuarantineActionId.MONEY_TRANSFER_PAUSE_GATE),
                    action(NewNumberQuarantineActionId.ACCOUNT_LOCKDOWN),
                    action(NewNumberQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH),
                    action(NewNumberQuarantineActionId.RECONTACT_BOUNDARY_COACH)
                )
            ),
            NewNumberQuarantineCard(
                id =
                    NewNumberQuarantineCardId.VERIFY_THROUGH_ESTABLISHED_FAMILY_ROUTE,
                displayOrder =
                    NewNumberQuarantineCardId
                        .VERIFY_THROUGH_ESTABLISHED_FAMILY_ROUTE.displayOrder,
                title = "예전부터 알던 가족 경로로 직접 확인하기",
                quarantineScenario =
                    "번호 변경·가족 위급 상황·익숙한 목소리라는 말 때문에 마음이 급해지는 때",
                valueFreeActionSentence =
                    "현재 상대가 알려 준 수단이 아닌 예전부터 알던 가족 확인 순서를 직접 고르겠습니다.",
                whyItMatters =
                    "현재 상대가 제시하지 않은 별도 경로를 쓰면 새 연락 밖에서 행동을 다시 확인할 수 있습니다.",
                doNotSaveContactOrValueBoundaries = listOf(
                    "가족 이름·연락처 라벨·새 번호·예전 번호·안전단어 답·콜백 문장을 이 카드에 넣지 마세요.",
                    "이 코치는 전화번호·프로필·기관·신원·통화 상대를 판정하거나 증명하지 않습니다."
                ),
                primaryAction = action(NewNumberQuarantineActionId.FAMILY_CALLBACK_CARD),
                secondaryActions = listOf(
                    action(
                        NewNumberQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(NewNumberQuarantineActionId.SAFE_CALL_SCRIPTS),
                    action(
                        NewNumberQuarantineActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH
                    ),
                    action(NewNumberQuarantineActionId.FAMILY_ALERT_MESSAGES),
                    action(
                        NewNumberQuarantineActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    )
                )
            ),
            NewNumberQuarantineCard(
                id = NewNumberQuarantineCardId.SHARE_ACTION_CATEGORIES_ONLY,
                displayOrder =
                    NewNumberQuarantineCardId.SHARE_ACTION_CATEGORIES_ONLY.displayOrder,
                title = "가족에게는 새 번호값 대신 행동 종류만 알리기",
                quarantineScenario =
                    "새 연락을 비밀로 하라는 압박을 받거나 가족·보호자에게 상황을 알리고 싶은 때",
                valueFreeActionSentence =
                    "사람·번호·프로필·결과값 대신 격리·별도 확인·돈 멈춤 같은 행동 종류만 나누겠습니다.",
                whyItMatters =
                    "가족은 대응 순서를 함께 정할 수 있지만 민감한 연락값을 다시 퍼뜨릴 필요는 없습니다.",
                doNotSaveContactOrValueBoundaries = listOf(
                    "가족 이름·받는 사람·전화번호·프로필·채팅 내용·통화 내용·확인 질문과 답을 공유하지 마세요.",
                    "읽음·보냄·확인·재연락·전달·담당자·상대 상태·진행 이력을 기록하지 마세요."
                ),
                primaryAction = action(NewNumberQuarantineActionId.FAMILY_ALERT_MESSAGES),
                secondaryActions = listOf(
                    action(NewNumberQuarantineActionId.TRUSTED_HELPER_REVIEW),
                    action(NewNumberQuarantineActionId.FAMILY_RESPONSE_ROLE_BOARD),
                    action(NewNumberQuarantineActionId.FAMILY_CALM_DEBRIEF_AGENDA),
                    action(
                        NewNumberQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
                    ),
                    action(NewNumberQuarantineActionId.FOLLOW_UP_CHECK_IN_PLANNER)
                )
            ),
            NewNumberQuarantineCard(
                id = NewNumberQuarantineCardId.KEEP_PLAN_AND_SAFE_WORD_UNCHANGED,
                displayOrder =
                    NewNumberQuarantineCardId.KEEP_PLAN_AND_SAFE_WORD_UNCHANGED.displayOrder,
                title = "확인 전 가족 계획·안전단어를 바꾸지 않기",
                quarantineScenario =
                    "새 연락이 가족 확인 규칙·안전단어·콜백 순서까지 바꾸자고 요구하는 때",
                valueFreeActionSentence =
                    "새 연락 안에서는 가족 규칙을 바꾸지 않고 별도 확인 뒤 기존 계획 화면을 직접 열겠습니다.",
                whyItMatters =
                    "확인 규칙을 현재 상대와 함께 바꾸면 다음 확인 단계도 그 상대에게 노출될 수 있습니다.",
                doNotSaveContactOrValueBoundaries = listOf(
                    "안전단어·확인 답·콜백 문장·연락처 이름·번호·가족 계획의 실제 값을 적거나 말하지 마세요.",
                    "이 코치는 가족 계획·안전단어·콜백 문장·연락처를 추가·수정·초기화·삭제·저장하지 않습니다."
                ),
                primaryAction = action(
                    NewNumberQuarantineActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                ),
                secondaryActions = listOf(
                    action(NewNumberQuarantineActionId.FAMILY_SAFE_WORD_RESET_COACH),
                    action(NewNumberQuarantineActionId.SAFETY_PLAN),
                    action(NewNumberQuarantineActionId.FAMILY_READINESS_ROADMAP),
                    action(NewNumberQuarantineActionId.FAMILY_SAFETY_DRILL_COACH),
                    action(NewNumberQuarantineActionId.PRIVACY_CENTER)
                )
            ),
            NewNumberQuarantineCard(
                id = NewNumberQuarantineCardId.MANUAL_REVIEW_AFTER_VERIFICATION,
                displayOrder =
                    NewNumberQuarantineCardId.MANUAL_REVIEW_AFTER_VERIFICATION.displayOrder,
                title = "확인 뒤에도 기존 계획 화면에서만 직접 정리하기",
                quarantineScenario =
                    "별도 가족 경로를 시도한 뒤 연락 규칙과 후속 행동을 다시 살펴보려는 때",
                valueFreeActionSentence =
                    "결과값을 이 코치에 넣지 않고 기존 계획·가족 대화·후속 점검 화면을 직접 고르겠습니다.",
                whyItMatters =
                    "수동 정리는 새 연락값이나 판정 상태를 이 코치에 남기지 않고도 기존 흐름에서 할 수 있습니다.",
                doNotSaveContactOrValueBoundaries = listOf(
                    "확인 결과·상대 상태·새 번호·프로필·연락처 변경 내용·담당자·받는 사람을 입력하지 마세요.",
                    "이 코치는 주소록 갱신·연락처 교체·번호 저장·프로필 확인·상태 기록을 수행하지 않습니다."
                ),
                primaryAction = action(NewNumberQuarantineActionId.SAFETY_PLAN),
                secondaryActions = listOf(
                    action(NewNumberQuarantineActionId.FAMILY_READINESS_ROADMAP),
                    action(NewNumberQuarantineActionId.FAMILY_SAFETY_DRILL_COACH),
                    action(NewNumberQuarantineActionId.FAMILY_CALM_DEBRIEF_AGENDA),
                    action(NewNumberQuarantineActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                    action(NewNumberQuarantineActionId.SAFETY_FLOW_ROUTER)
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
                    "현재 검사 결과가 없거나 일치 신호가 없습니다. 새 번호·새 프로필·새 연락을 " +
                        "가족이나 기관의 것으로 단정하지 말고 모든 격리 카드를 보수적으로 직접 읽으세요."
                )
            }
            if (hasHighRisk) {
                add(
                    "위험도가 높아 연락 격리·별도 확인·가족 공유·돈·계정·기기·증거·도움 " +
                        "경로를 넓게 권합니다. 번호·프로필·사람·기관·연락 상태나 조치 결과를 " +
                        "판정하는 뜻은 아닙니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "번호 변경·가족 위급 상황·합성 음성 의심 신호가 있어 새 연락을 저장하거나 " +
                        "기존 연락과 바꾸지 않고 별도 경로·가족 콜백·값 없는 확인 전화·목소리 " +
                        "현실 점검·가족 경고·안전단어 재정비·확인 뒤 다음 단계 경로를 권합니다."
                )
            }
            if (hasSecrecyImpersonationSignal) {
                add(
                    "비밀 유지·사칭 신호가 있어 새 연락을 숨긴 채 이어가지 말고 가족 경고·차분 " +
                        "대화·보호자 검토·증거 전달 경계·개인정보·사용자가 직접 고를 도움·신고 " +
                        "준비 경로를 살펴보세요. 기관 번호나 도움 목록 값은 사용하지 않습니다."
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
                    "링크·스미싱 신호가 있어 같은 새 연락·링크·앱·통화 흐름에서 계속하지 말고 " +
                        "연락 종료 문구·별도 경로·원격지시 중단·노출 분류·개인정보 경로를 " +
                        "권합니다. URL·도메인을 판정하거나 링크를 스캔하지 않습니다."
                )
            }
            if (hasRemoteAccessSignal) {
                add(
                    "앱 설치·통화 유지·화면공유 신호가 있어 같은 새 연락·앱·통화·화면공유 " +
                        "세션을 벗어나는 연락 종료 문구·원격지시 중단·노출 분류·개인정보·보호자 " +
                        "경로를 권합니다. 설치 앱 목록이나 화면을 읽지 않고 원격 연결 종료·악성 " +
                        "프로그램 제거·기기 정리를 수행하지 않습니다."
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
                "가족 준비가 되어 있다는 범주는 기존 준비 항목이 채워졌다는 뜻일 뿐 새 번호·새 프로필·새 연락이 가족이라는 점을 증명하지 않습니다. 준비 상태와 관계없이 새 연락 밖의 가족 경로를 직접 고르세요."
        }
        return listOf(situationCopy, readinessCopy).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 고정 문장을 직접 읽고 기존 앱 경로를 직접 고르는 로컬 전용·무상태 수동 안내이며 이름·연락처·번호·프로필·채널·확인 결과값 입력을 요구하지 않습니다.",
        "추천은 현재 검사 결과의 위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기·가족 안전 계획의 실제 값을 사용하지 않습니다.",
        "가족 안전 계획·안전단어 재정비·콜백 화면으로 이동하는 버튼은 기존 화면을 열 뿐이며 안전단어·콜백 문장·연락처 이름·번호를 읽거나 표시·공유·추가·수정·초기화·삭제·저장하지 않습니다.",
        "주소록·연락처를 읽기·쓰기·편집·삽입·삭제·선택·라벨링·병합·교체·저장·차단·음소거·동기화·갱신하지 않으며 연락처 관리 기능이 아닙니다.",
        "전화번호를 정규화·검사·판정하지 않고 프로필·채널·공식 계정·신원·스팸·통신사·기관을 조회·검증·증명하지 않으며 전화·다이얼·통화·통화 종료·통화 선별·차단·상태·기록·SMS·받는 사람 선택·자동 전송을 사용하지 않습니다.",
        "통화 기록·문자·메시지·통화 상태·알림을 읽거나 감시·필터링하지 않고 클립보드를 읽거나 쓰지 않습니다.",
        "파일·문서·사진·이미지·오디오·미디어·저장소를 읽지 않고 카메라·화면 캡처·스크린샷 가져오기·OCR·이미지 분석·음성 녹음·전사·파일 선택기·첨부·스트림·URI 공유를 사용하지 않습니다.",
        "네트워크·웹 탐색·브라우저·지도·위치·지오코딩·기관 정보·공식 번호 조회·보고 제출·업로드·클라우드 백업·증거 동기화를 사용하지 않습니다.",
        "새 Android 권한을 요청하지 않고 알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러를 만들거나 예약하지 않습니다.",
        "새 저장소를 만들지 않고 카드 선택·추천·읽음·공유·체크·메모·담당자·받는 사람·확인·재연락·전달·진행·상태·이력을 저장하지 않습니다.",
        "결제·은행 기능이나 API, NFC, Android 계정 관리자에 접근하지 않고 송금·결제·취소·계정 또는 카드 정지·동결·환급·회수·복구를 자동 수행하지 않습니다.",
        "기기 관리자·접근성 서비스·VPN·사용 기록·앱 작업 권한을 쓰지 않고 설치 앱·패키지 목록 조회, 앱 설치·제거, 설정 변경, 원격 연결 종료, 악성 프로그램 검사·제거, 기기 정리를 하지 않습니다.",
        "화면·경로·공유 버튼을 눌러도 원문, 연락처·번호·프로필·통화·문자·알림, 사건 기록, 가족 계획, 기관 목록, 계정·카드·은행·기기·앱·파일·미디어 상태나 확인 상태를 추가·수정·삭제하지 않습니다.",
        "수동 문자 공유는 고정된 격리 카드·기존 앱 경로·행동 종류만 담고 연락처·번호·프로필·채널·비밀값·받는 사람·확인 결과·파일·첨부·스트림·URI를 담거나 자동 발송하지 않습니다.",
        "새 번호·새 프로필·새 채널이 가족·기관의 것인지 또는 신뢰·확인·보호·정리 상태인지 판정하거나 보장하지 않으며 법률·금융 결과를 증명하거나 보장하지 않습니다. 자동 모니터링·검사·스캔·OCR·삭제·가림·차단·통화·메시지 전송·조회·검증·저장·업로드·첨부·제출을 수행하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<NewNumberQuarantineCardId>,
        recommendedActions: List<NewNumberQuarantineAction>,
        cards: List<NewNumberQuarantineCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 새 번호 격리 코치")
            appendLine()
            appendLine("새 연락을 바로 저장하거나 바꾸지 않고 값 없는 수동 격리 순서를 함께 보고 있습니다.")
            appendLine()
            appendLine("추천 격리 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("가족에게 말할 값 없는 문장")
            appendLine("- 격리·별도 확인·돈 멈춤·도움 요청 같은 행동 종류만 함께 살펴보세요.")
            append(
                "사람·연락처·번호·프로필·채널·안전단어·콜백 문장·원문·사건 내용·링크·" +
                    "계정·인증·기기·앱·파일·받는 사람·결과값 없이 고정된 카드와 기존 앱 " +
                    "경로 이름만 담았습니다."
            )
        }.trim()
    }

    private fun actionFor(
        id: NewNumberQuarantineActionId
    ): NewNumberQuarantineAction = NewNumberQuarantineAction(
        id = id,
        label = when (id) {
            NewNumberQuarantineActionId.QUICK_STOP -> "긴급 멈춤 모드"
            NewNumberQuarantineActionId.CHECKER -> "의심 내용 검사"
            NewNumberQuarantineActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            NewNumberQuarantineActionId.SAFETY_PLAN -> "가족 안전 계획"
            NewNumberQuarantineActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
            NewNumberQuarantineActionId.FAMILY_SAFETY_DRILL_COACH ->
                "가족 안전 연습 코치"
            NewNumberQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                "진행 중 연락 종료 문구"
            NewNumberQuarantineActionId.FAMILY_CALLBACK_CARD ->
                "평소 가족 경로 콜백 카드"
            NewNumberQuarantineActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                "가족 안전단어 재설정 코치"
            NewNumberQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                "별도 경로 확인 코치"
            NewNumberQuarantineActionId.SAFE_CALL_SCRIPTS ->
                "값 없는 가족 콜백·확인 전화 스크립트"
            NewNumberQuarantineActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                "목소리 사칭 현실 점검 코치"
            NewNumberQuarantineActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
            NewNumberQuarantineActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                "확인 결과별 다음 단계 보드"
            NewNumberQuarantineActionId.RECONTACT_BOUNDARY_COACH -> "재연락 경계 코치"
            NewNumberQuarantineActionId.TRUSTED_HELPER_REVIEW ->
                "보호자와 함께 안전 검토"
            NewNumberQuarantineActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                "가족 대응 역할 보드"
            NewNumberQuarantineActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                "가족 차분 대화 안건"
            NewNumberQuarantineActionId.MONEY_TRANSFER_PAUSE_GATE ->
                "송금 전 멈춤 확인"
            NewNumberQuarantineActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
            NewNumberQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                "화면공유·원격지시 끊기 코치"
            NewNumberQuarantineActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
            NewNumberQuarantineActionId.PRIVACY_CENTER -> "개인정보 센터"
            NewNumberQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                "증거 전달 개인정보 코치"
            NewNumberQuarantineActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            NewNumberQuarantineActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 도움 경로"
            NewNumberQuarantineActionId.REPORT_PREP -> "신고용 상담 준비 카드"
            NewNumberQuarantineActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                "며칠 뒤 다시 확인할 것"
            NewNumberQuarantineActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
