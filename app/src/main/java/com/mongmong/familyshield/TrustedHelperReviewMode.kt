package com.mongmong.familyshield

enum class TrustedHelperReviewCardId(val displayOrder: Int) {
    PAUSE_AND_REASSURE(1),
    SECRET_VALUE_BOUNDARY(2),
    CLASSIFY_ACTIONS_ONLY(3),
    FAMILY_IDENTITY_VERIFICATION(4),
    PROTECT_ACTIONS_ALREADY_TAKEN(5),
    PREPARE_OFFICIAL_HELP(6),
    SHARE_GENERAL_FAMILY_ALERT(7),
    CONNECT_NEXT_FAMILY_PREPARATION(8)
}

enum class TrustedHelperReviewStage(val label: String) {
    STOP_AND_REASSURE("먼저 멈춤"),
    KEEP_SECRET_BOUNDARY("비밀 경계"),
    SORT_ACTION_CATEGORIES("행동 분류"),
    VERIFY_SEPARATELY("가족 확인"),
    PROTECT_AND_PRESERVE("보호·보존"),
    USE_OFFICIAL_ROUTES("공식 도움"),
    WARN_WITHOUT_RAW_TEXT("가족 경고"),
    PREPARE_TOGETHER("다음 준비")
}

enum class TrustedHelperReviewCategory(val label: String) {
    CALM_SUPPORT("안심·압박 중단"),
    SECRET_PROTECTION("비밀값 보호"),
    EXPOSURE_CLASSIFICATION("행동 종류 분류"),
    FAMILY_VERIFICATION("가족 본인 확인"),
    RECOVERY_PROTECTION("이미 한 행동 보호"),
    OFFICIAL_ASSISTANCE("공식 도움 준비"),
    GENERAL_WARNING("일반 경고 공유"),
    READINESS_PRACTICE("가족 준비·연습")
}

enum class TrustedHelperReviewPriority(val rank: Int, val label: String) {
    STOP_NOW(0, "지금 먼저"),
    KEEP_BOUNDARY(1, "비밀 먼저 보호"),
    VERIFY_CATEGORIES(2, "종류만 확인"),
    PROTECT_NOW(3, "보호·보존 확인"),
    PREPARE_NEXT(4, "다음 준비")
}

enum class TrustedHelperReviewActionId {
    QUICK_STOP,
    EXPOSURE_IMPACT_TRIAGE,
    CHECKER,
    ANALYSIS_RESULT,
    RISK_SIGNAL_EXPLAINERS,
    FAMILY_CALLBACK_CARD,
    SAFE_CALL_SCRIPTS,
    FAMILY_ALERT_MESSAGES,
    TRUSTED_HELPER_HANDOFF,
    FAMILY_READINESS_ROADMAP,
    FAMILY_SAFETY_DRILL_COACH,
    RECOVERY_PLAN,
    ACCOUNT_LOCKDOWN,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    SAFETY_FLOW_ROUTER,
    PRIVACY_CENTER
}

data class TrustedHelperReviewAction(
    val id: TrustedHelperReviewActionId,
    val label: String
)

data class TrustedHelperReviewCard(
    val id: TrustedHelperReviewCardId,
    val displayOrder: Int,
    val stage: TrustedHelperReviewStage,
    val category: TrustedHelperReviewCategory,
    val title: String,
    val helperRole: String,
    val safeQuestion: String,
    val whatNotToAsk: List<String>,
    val firstSafeMove: String,
    val priority: TrustedHelperReviewPriority,
    val primaryAction: TrustedHelperReviewAction,
    val secondaryActions: List<TrustedHelperReviewAction>
) {
    val routeActions: List<TrustedHelperReviewAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class TrustedHelperReviewMode(
    val title: String,
    val intro: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<TrustedHelperReviewCardId>,
    val recommendedActions: List<TrustedHelperReviewAction>,
    val cards: List<TrustedHelperReviewCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<TrustedHelperReviewCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<TrustedHelperReviewActionId>
        get() = recommendedActions.map { action -> action.id }
}

object TrustedHelperReviewModeBuilder {
    private val financialProtectionSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.BAIT,
        ScamSignal.CREDENTIALS,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val remoteAccessSignals = setOf(
        ScamSignal.APP_INSTALL,
        ScamSignal.LIVE_CALL_CONTROL
    )

    private val linkSignals = setOf(
        ScamSignal.LINK,
        ScamSignal.PARCEL_INVITATION_SMISHING
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
        readiness: FamilyReadiness
    ): TrustedHelperReviewMode {
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val hasDefaultState = analysis == null ||
            (signals.isEmpty() && analysis.riskLevel != RiskLevel.HIGH)
        val hasFinancialProtectionSignal = analysis?.riskLevel == RiskLevel.HIGH ||
            signals.any { signal -> signal in financialProtectionSignals }
        val hasRemoteAccessSignal = signals.any { signal -> signal in remoteAccessSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasFamilyVerificationSignal =
            signals.any { signal -> signal in familyVerificationSignals }
        val hasSecrecyImpersonationSignal =
            signals.any { signal -> signal in secrecyImpersonationSignals }

        val recommendedCardSet = linkedSetOf<TrustedHelperReviewCardId>()
        if (hasDefaultState) {
            recommendedCardSet += TrustedHelperReviewCardId.PAUSE_AND_REASSURE
            recommendedCardSet += TrustedHelperReviewCardId.SECRET_VALUE_BOUNDARY
            recommendedCardSet += TrustedHelperReviewCardId.CLASSIFY_ACTIONS_ONLY
            recommendedCardSet += TrustedHelperReviewCardId.FAMILY_IDENTITY_VERIFICATION
        }
        if (hasFinancialProtectionSignal || hasRemoteAccessSignal) {
            recommendedCardSet += TrustedHelperReviewCardId.PROTECT_ACTIONS_ALREADY_TAKEN
        }
        if (hasFinancialProtectionSignal) {
            recommendedCardSet += TrustedHelperReviewCardId.PREPARE_OFFICIAL_HELP
        }
        if (hasLinkSignal) {
            recommendedCardSet += TrustedHelperReviewCardId.CLASSIFY_ACTIONS_ONLY
            recommendedCardSet += TrustedHelperReviewCardId.SHARE_GENERAL_FAMILY_ALERT
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet += TrustedHelperReviewCardId.FAMILY_IDENTITY_VERIFICATION
            recommendedCardSet += TrustedHelperReviewCardId.SHARE_GENERAL_FAMILY_ALERT
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedCardSet += TrustedHelperReviewCardId.PAUSE_AND_REASSURE
            recommendedCardSet += TrustedHelperReviewCardId.SECRET_VALUE_BOUNDARY
            recommendedCardSet += TrustedHelperReviewCardId.CLASSIFY_ACTIONS_ONLY
            recommendedCardSet += TrustedHelperReviewCardId.PREPARE_OFFICIAL_HELP
        }
        if (readiness.level == ReadinessLevel.LOW) {
            recommendedCardSet += TrustedHelperReviewCardId.CONNECT_NEXT_FAMILY_PREPARATION
        }
        if (recommendedCardSet.isEmpty()) {
            recommendedCardSet += TrustedHelperReviewCardId.PAUSE_AND_REASSURE
            recommendedCardSet += TrustedHelperReviewCardId.SECRET_VALUE_BOUNDARY
            recommendedCardSet += TrustedHelperReviewCardId.CLASSIFY_ACTIONS_ONLY
            recommendedCardSet += TrustedHelperReviewCardId.FAMILY_IDENTITY_VERIFICATION
        }
        val recommendedCardIds = TrustedHelperReviewCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val recommendedActionSet = linkedSetOf<TrustedHelperReviewActionId>()
        if (hasDefaultState) {
            recommendedActionSet += TrustedHelperReviewActionId.QUICK_STOP
            recommendedActionSet += TrustedHelperReviewActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += TrustedHelperReviewActionId.CHECKER
            if (analysis != null) {
                recommendedActionSet += TrustedHelperReviewActionId.ANALYSIS_RESULT
            }
            recommendedActionSet += TrustedHelperReviewActionId.FAMILY_CALLBACK_CARD
        }
        if (hasFinancialProtectionSignal) {
            recommendedActionSet += TrustedHelperReviewActionId.RECOVERY_PLAN
            recommendedActionSet += TrustedHelperReviewActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += TrustedHelperReviewActionId.EVIDENCE_KIT
            recommendedActionSet += TrustedHelperReviewActionId.OFFICIAL_HELP_DIRECTORY
            if (analysis != null) {
                recommendedActionSet += TrustedHelperReviewActionId.REPORT_PREP
            }
        }
        if (hasRemoteAccessSignal) {
            recommendedActionSet += TrustedHelperReviewActionId.RECOVERY_PLAN
            recommendedActionSet += TrustedHelperReviewActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += TrustedHelperReviewActionId.EVIDENCE_KIT
            recommendedActionSet += TrustedHelperReviewActionId.TRUSTED_HELPER_HANDOFF
        }
        if (hasLinkSignal) {
            recommendedActionSet += TrustedHelperReviewActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += TrustedHelperReviewActionId.EVIDENCE_KIT
            recommendedActionSet += TrustedHelperReviewActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += TrustedHelperReviewActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += TrustedHelperReviewActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += TrustedHelperReviewActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += TrustedHelperReviewActionId.TRUSTED_HELPER_HANDOFF
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedActionSet += TrustedHelperReviewActionId.QUICK_STOP
            recommendedActionSet += TrustedHelperReviewActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += TrustedHelperReviewActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (readiness.level == ReadinessLevel.LOW) {
            recommendedActionSet += TrustedHelperReviewActionId.FAMILY_READINESS_ROADMAP
            recommendedActionSet += TrustedHelperReviewActionId.FAMILY_SAFETY_DRILL_COACH
        }
        if (recommendedActionSet.isEmpty()) {
            recommendedActionSet += TrustedHelperReviewActionId.QUICK_STOP
            recommendedActionSet += TrustedHelperReviewActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += TrustedHelperReviewActionId.CHECKER
            recommendedActionSet += TrustedHelperReviewActionId.FAMILY_CALLBACK_CARD
        }
        val recommendedActions = TrustedHelperReviewActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map { actionId ->
                actionFor(
                    id = actionId,
                    hasAnalysis = analysis != null,
                    readinessLevel = readiness.level
                )
            }

        val cards = buildCards(
            hasAnalysis = analysis != null,
            readinessLevel = readiness.level
        )
        val recommendationTitle = when {
            analysis == null -> "분석 전 보호자 기본 검토"
            hasFinancialProtectionSignal || hasRemoteAccessSignal ->
                "강한 신호에서 먼저 함께 볼 검토"
            signals.isEmpty() -> "일치 신호가 없어도 함께 볼 기본 검토"
            else -> "현재 신호에서 먼저 함께 볼 검토"
        }

        return TrustedHelperReviewMode(
            title = "보호자와 함께 안전 검토",
            intro =
                "믿을 만한 보호자가 곁에서 비난 없이 상황을 종류만 확인하고 기존 안전 화면을 함께 고르는 " +
                    "로컬·무상태 안내입니다. 비밀번호·인증번호·안전 질문 답·연락처 값이나 사기 원문을 묻거나 적지 않습니다.",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                analysis = analysis,
                readinessLevel = readiness.level,
                signals = signals,
                hasFinancialProtectionSignal = hasFinancialProtectionSignal,
                hasRemoteAccessSignal = hasRemoteAccessSignal,
                hasLinkSignal = hasLinkSignal,
                hasFamilyVerificationSignal = hasFamilyVerificationSignal,
                hasSecrecyImpersonationSignal = hasSecrecyImpersonationSignal
            ),
            recommendedCardIds = recommendedCardIds,
            recommendedActions = recommendedActions,
            cards = cards,
            privacySafetyNotes = privacySafetyNotes(),
            familyShareSummary = buildFamilyShareSummary(
                recommendationTitle = recommendationTitle,
                recommendedCardIds = recommendedCardIds,
                recommendedActions = recommendedActions,
                cards = cards
            )
        )
    }

    private fun buildCards(
        hasAnalysis: Boolean,
        readinessLevel: ReadinessLevel
    ): List<TrustedHelperReviewCard> {
        fun action(id: TrustedHelperReviewActionId): TrustedHelperReviewAction = actionFor(
            id = id,
            hasAnalysis = hasAnalysis,
            readinessLevel = readinessLevel
        )

        fun currentAnalysisAction(): TrustedHelperReviewAction = action(
            if (hasAnalysis) {
                TrustedHelperReviewActionId.ANALYSIS_RESULT
            } else {
                TrustedHelperReviewActionId.CHECKER
            }
        )

        return listOf(
            TrustedHelperReviewCard(
                id = TrustedHelperReviewCardId.PAUSE_AND_REASSURE,
                displayOrder = TrustedHelperReviewCardId.PAUSE_AND_REASSURE.displayOrder,
                stage = TrustedHelperReviewStage.STOP_AND_REASSURE,
                category = TrustedHelperReviewCategory.CALM_SUPPORT,
                title = "먼저 멈추고 안심시키기",
                helperRole = "옆에서 대화와 추가 행동을 멈추고, 탓하지 않은 채 함께 있어주는 보호자",
                safeQuestion = "지금은 설명을 서두르지 말고 함께 대화를 멈춘 뒤 잠깐 숨을 고를까요?",
                whatNotToAsk = listOf(
                    "왜 믿었는지, 왜 바로 알아차리지 못했는지 다그치지 마세요.",
                    "사기 대화 원문이나 모든 화면을 처음부터 다시 보여 달라고 하지 마세요."
                ),
                firstSafeMove =
                    "현재 연락과 추가 송금·정보 제공·앱 행동을 보류하고, 옆에서 긴급 멈춤 순서를 함께 읽습니다.",
                priority = TrustedHelperReviewPriority.STOP_NOW,
                primaryAction = action(TrustedHelperReviewActionId.QUICK_STOP),
                secondaryActions = listOf(
                    currentAnalysisAction(),
                    action(TrustedHelperReviewActionId.RISK_SIGNAL_EXPLAINERS)
                )
            ),
            TrustedHelperReviewCard(
                id = TrustedHelperReviewCardId.SECRET_VALUE_BOUNDARY,
                displayOrder = TrustedHelperReviewCardId.SECRET_VALUE_BOUNDARY.displayOrder,
                stage = TrustedHelperReviewStage.KEEP_SECRET_BOUNDARY,
                category = TrustedHelperReviewCategory.SECRET_PROTECTION,
                title = "비밀값 묻지 않기",
                helperRole = "실제 값을 보거나 대신 입력하지 않고 정보 종류만 구분해주는 보호자",
                safeQuestion =
                    "말하거나 입력한 것이 있다면 실제 값은 말하지 말고, 계정·인증·결제·신분정보 중 어떤 종류였나요?",
                whatNotToAsk = listOf(
                    "비밀번호, 인증번호·OTP, PIN, 카드 비밀값, 보안카드 값, 안전 질문의 답을 묻거나 적지 마세요.",
                    "정확한 연락처·전화·계좌·주민등록번호나 링크 원문을 말해 달라거나 복사해 달라고 하지 마세요."
                ),
                firstSafeMove =
                    "비밀값은 말하거나 옮기지 말고, 필요한 정보 종류만 분류해 다음 앱 경로를 고릅니다.",
                priority = TrustedHelperReviewPriority.KEEP_BOUNDARY,
                primaryAction = action(TrustedHelperReviewActionId.PRIVACY_CENTER),
                secondaryActions = listOf(
                    action(TrustedHelperReviewActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(TrustedHelperReviewActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            TrustedHelperReviewCard(
                id = TrustedHelperReviewCardId.CLASSIFY_ACTIONS_ONLY,
                displayOrder = TrustedHelperReviewCardId.CLASSIFY_ACTIONS_ONLY.displayOrder,
                stage = TrustedHelperReviewStage.SORT_ACTION_CATEGORIES,
                category = TrustedHelperReviewCategory.EXPOSURE_CLASSIFICATION,
                title = "무엇을 했는지만 분류하기",
                helperRole = "상대의 원문을 받아 적지 않고 사용자가 실제로 한 행동 종류만 차분히 구분하는 보호자",
                safeQuestion =
                    "실제로 한 행동은 돈 전달·정보 입력·링크 클릭·앱 설치·화면공유 중 어떤 종류였나요?",
                whatNotToAsk = listOf(
                    "정확한 금액·계좌·인증값·개인정보 값을 다시 말해 달라고 하지 마세요.",
                    "분석 신호만 보고 실제 노출이나 피해가 있었다고 단정하지 마세요."
                ),
                firstSafeMove =
                    "사용자가 실제로 한 행동과 가까운 범주만 노출 영향 분류에서 직접 확인합니다.",
                priority = TrustedHelperReviewPriority.VERIFY_CATEGORIES,
                primaryAction = action(TrustedHelperReviewActionId.EXPOSURE_IMPACT_TRIAGE),
                secondaryActions = listOf(
                    currentAnalysisAction(),
                    action(TrustedHelperReviewActionId.RISK_SIGNAL_EXPLAINERS),
                    action(TrustedHelperReviewActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            TrustedHelperReviewCard(
                id = TrustedHelperReviewCardId.FAMILY_IDENTITY_VERIFICATION,
                displayOrder = TrustedHelperReviewCardId.FAMILY_IDENTITY_VERIFICATION.displayOrder,
                stage = TrustedHelperReviewStage.VERIFY_SEPARATELY,
                category = TrustedHelperReviewCategory.FAMILY_VERIFICATION,
                title = "가족 본인 확인 돕기",
                helperRole = "새 연락 수단을 믿지 않고 평소 가족 경로로 별도 확인을 함께 진행하는 보호자",
                safeQuestion =
                    "가족을 자칭한 연락이 있었나요? 있었다면 새 연락 수단이 아닌 평소 별도 경로로 함께 확인할까요?",
                whatNotToAsk = listOf(
                    "안전단어 답, 콜백 문장, 정확한 가족 번호를 말하거나 화면에 적어 달라고 하지 마세요.",
                    "익숙한 이름·사진·목소리만으로 본인 확인이 끝났다고 말하지 마세요."
                ),
                firstSafeMove =
                    "새 번호나 메시지 속 경로를 쓰지 않고 가족 콜백 카드와 안전 전화 스크립트로 별도 확인합니다.",
                priority = TrustedHelperReviewPriority.VERIFY_CATEGORIES,
                primaryAction = action(TrustedHelperReviewActionId.FAMILY_CALLBACK_CARD),
                secondaryActions = listOf(
                    action(TrustedHelperReviewActionId.SAFE_CALL_SCRIPTS),
                    action(TrustedHelperReviewActionId.TRUSTED_HELPER_HANDOFF)
                )
            ),
            TrustedHelperReviewCard(
                id = TrustedHelperReviewCardId.PROTECT_ACTIONS_ALREADY_TAKEN,
                displayOrder = TrustedHelperReviewCardId.PROTECT_ACTIONS_ALREADY_TAKEN.displayOrder,
                stage = TrustedHelperReviewStage.PROTECT_AND_PRESERVE,
                category = TrustedHelperReviewCategory.RECOVERY_PROTECTION,
                title = "이미 한 행동 보호하기",
                helperRole = "대신 로그인하거나 조작하지 않고 수습·계정 보호·증거 순서를 옆에서 챙기는 보호자",
                safeQuestion =
                    "이미 한 행동이 있다면 송금·정보 입력·앱 허용·링크 클릭 중 어떤 종류였나요?",
                whatNotToAsk = listOf(
                    "비밀번호·인증번호·카드 비밀값을 알려 달라거나 대신 로그인해 달라고 하지 마세요.",
                    "증거를 더 보겠다며 의심 앱·링크·원격 연결을 다시 열어 달라고 하지 마세요."
                ),
                firstSafeMove =
                    "추가 행동을 멈춘 채 수습 계획, 계정·기기 보호, 증거 보존 순서를 기존 화면에서 확인합니다.",
                priority = TrustedHelperReviewPriority.PROTECT_NOW,
                primaryAction = action(TrustedHelperReviewActionId.RECOVERY_PLAN),
                secondaryActions = listOf(
                    action(TrustedHelperReviewActionId.ACCOUNT_LOCKDOWN),
                    action(TrustedHelperReviewActionId.EVIDENCE_KIT),
                    action(TrustedHelperReviewActionId.TRUSTED_HELPER_HANDOFF)
                )
            ),
            TrustedHelperReviewCard(
                id = TrustedHelperReviewCardId.PREPARE_OFFICIAL_HELP,
                displayOrder = TrustedHelperReviewCardId.PREPARE_OFFICIAL_HELP.displayOrder,
                stage = TrustedHelperReviewStage.USE_OFFICIAL_ROUTES,
                category = TrustedHelperReviewCategory.OFFICIAL_ASSISTANCE,
                title = "공식 도움 준비하기",
                helperRole = "공식 창구와 말할 범주를 함께 찾되 비밀값이나 원문을 대신 수집하지 않는 보호자",
                safeQuestion =
                    "공식 도움에 설명할 범주는 돈·계정·기기·링크·가족 사칭 중 어느 쪽에 가깝나요?",
                whatNotToAsk = listOf(
                    "상담 준비를 이유로 계좌 전체 번호·인증값·신분정보·연락처 값을 일반 메모에 적지 마세요.",
                    "사실을 확인한다며 의심 링크를 다시 열거나 상대가 알려준 번호로 전화하지 마세요."
                ),
                firstSafeMove =
                    "신고·상담 연락처와 상담 준비 카드에서 공식 경로와 말할 범주만 함께 확인합니다.",
                priority = TrustedHelperReviewPriority.PROTECT_NOW,
                primaryAction = action(TrustedHelperReviewActionId.OFFICIAL_HELP_DIRECTORY),
                secondaryActions = listOf(
                    action(
                        if (hasAnalysis) {
                            TrustedHelperReviewActionId.REPORT_PREP
                        } else {
                            TrustedHelperReviewActionId.CHECKER
                        }
                    ),
                    action(TrustedHelperReviewActionId.EVIDENCE_KIT)
                )
            ),
            TrustedHelperReviewCard(
                id = TrustedHelperReviewCardId.SHARE_GENERAL_FAMILY_ALERT,
                displayOrder = TrustedHelperReviewCardId.SHARE_GENERAL_FAMILY_ALERT.displayOrder,
                stage = TrustedHelperReviewStage.WARN_WITHOUT_RAW_TEXT,
                category = TrustedHelperReviewCategory.GENERAL_WARNING,
                title = "가족에게 일반 경고 공유하기",
                helperRole = "사기 원문과 가족 비밀 없이 위험 범주와 멈춤 행동만 다른 가족에게 알리는 보호자",
                safeQuestion = "다른 가족에게는 원문 없이 어떤 위험 범주와 멈춤 행동만 알리면 될까요?",
                whatNotToAsk = listOf(
                    "사기 원문·캡처·링크를 그대로 전달해 달라고 하지 마세요.",
                    "안전 질문 답·콜백 내용·정확한 가족 연락처 목록을 경고문에 넣지 마세요."
                ),
                firstSafeMove =
                    "가족 경고 메시지 팩에서 원문 없는 일반 경고를 고르고, 전송 대상은 사용자가 직접 선택합니다.",
                priority = TrustedHelperReviewPriority.VERIFY_CATEGORIES,
                primaryAction = action(TrustedHelperReviewActionId.FAMILY_ALERT_MESSAGES),
                secondaryActions = listOf(
                    action(TrustedHelperReviewActionId.TRUSTED_HELPER_HANDOFF),
                    action(TrustedHelperReviewActionId.SAFE_CALL_SCRIPTS)
                )
            ),
            TrustedHelperReviewCard(
                id = TrustedHelperReviewCardId.CONNECT_NEXT_FAMILY_PREPARATION,
                displayOrder = TrustedHelperReviewCardId.CONNECT_NEXT_FAMILY_PREPARATION.displayOrder,
                stage = TrustedHelperReviewStage.PREPARE_TOGETHER,
                category = TrustedHelperReviewCategory.READINESS_PRACTICE,
                title = "다음 가족 준비로 연결하기",
                helperRole = "이번 검토 뒤 콜백 경로와 멈춤 연습을 비밀값 없이 함께 준비하는 보호자",
                safeQuestion = "콜백 경로 확인과 멈춤 연습 중 어떤 준비부터 함께 할까요?",
                whatNotToAsk = listOf(
                    "연습을 이유로 안전 질문 답·비밀번호·인증값을 서로 공개하지 마세요.",
                    "준비 점검을 감시나 기기 접근 권한 공유로 바꾸지 마세요."
                ),
                firstSafeMove =
                    "가족 준비 로드맵에서 다음 작은 단계를 고르고 안전 연습 코치로 멈춤과 콜백 순서를 연습합니다.",
                priority = TrustedHelperReviewPriority.PREPARE_NEXT,
                primaryAction = action(TrustedHelperReviewActionId.FAMILY_READINESS_ROADMAP),
                secondaryActions = listOf(
                    action(TrustedHelperReviewActionId.FAMILY_SAFETY_DRILL_COACH),
                    action(TrustedHelperReviewActionId.FAMILY_CALLBACK_CARD),
                    action(TrustedHelperReviewActionId.PRIVACY_CENTER)
                )
            )
        )
    }

    private fun actionFor(
        id: TrustedHelperReviewActionId,
        hasAnalysis: Boolean,
        readinessLevel: ReadinessLevel
    ): TrustedHelperReviewAction = TrustedHelperReviewAction(
        id = id,
        label = when (id) {
            TrustedHelperReviewActionId.QUICK_STOP -> "긴급 멈춤 모드"
            TrustedHelperReviewActionId.EXPOSURE_IMPACT_TRIAGE -> "무엇을 했는지 종류만 분류하기"
            TrustedHelperReviewActionId.CHECKER -> if (hasAnalysis) {
                "다른 의심 내용 직접 분석하기"
            } else {
                "의심 내용 직접 분석하기"
            }
            TrustedHelperReviewActionId.ANALYSIS_RESULT -> "현재 분석 결과 보기"
            TrustedHelperReviewActionId.RISK_SIGNAL_EXPLAINERS -> "위험 신호 설명 카드"
            TrustedHelperReviewActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 확인 카드"
            TrustedHelperReviewActionId.SAFE_CALL_SCRIPTS -> "안전 확인 전화 스크립트"
            TrustedHelperReviewActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
            TrustedHelperReviewActionId.TRUSTED_HELPER_HANDOFF -> "보호자 도움 요청 카드"
            TrustedHelperReviewActionId.FAMILY_READINESS_ROADMAP -> when (readinessLevel) {
                ReadinessLevel.LOW -> "가족 준비 로드맵 시작하기"
                ReadinessLevel.IN_PROGRESS -> "가족 준비 로드맵 보완하기"
                ReadinessLevel.READY -> "가족 준비 로드맵 점검하기"
            }
            TrustedHelperReviewActionId.FAMILY_SAFETY_DRILL_COACH -> "가족 안전 연습 코치"
            TrustedHelperReviewActionId.RECOVERY_PLAN -> "24시간 수습 계획"
            TrustedHelperReviewActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
            TrustedHelperReviewActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            TrustedHelperReviewActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처 보기"
            TrustedHelperReviewActionId.REPORT_PREP -> "신고용 상담 준비 카드"
            TrustedHelperReviewActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
            TrustedHelperReviewActionId.PRIVACY_CENTER -> "개인정보 센터"
        }
    )

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readinessLevel: ReadinessLevel,
        signals: Set<ScamSignal>,
        hasFinancialProtectionSignal: Boolean,
        hasRemoteAccessSignal: Boolean,
        hasLinkSignal: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasSecrecyImpersonationSignal: Boolean
    ): String {
        val situationCopy = when {
            analysis == null ->
                "현재 분석 결과가 없어도 안전하다고 단정하지 않습니다. 먼저 압박을 멈추고 비밀값 경계를 세운 뒤 실제로 한 행동의 종류와 평소 가족 확인 경로만 함께 확인하세요."
            signals.isEmpty() && analysis.riskLevel != RiskLevel.HIGH ->
                "일치한 신호가 없어도 안전을 증명하지 않습니다. 보호자는 비밀값을 묻지 않고 실제 행동 종류와 별도 가족 확인 경로를 차분히 살펴보세요."
            else -> buildList {
                if (hasFinancialProtectionSignal) {
                    add("강한 위험도나 돈·계정·결제 관련 신호가 있어 실제 행동 여부를 종류만 확인하고 수습·계정 보호·증거·공식 도움을 함께 살펴보세요.")
                }
                if (hasRemoteAccessSignal) {
                    add("앱·화면공유·원격제어 신호가 있어 실제 허용 여부를 직접 확인하고 보호·증거·도우미 경로를 함께 살펴보세요.")
                }
                if (hasLinkSignal) {
                    add("링크·스미싱 신호가 있어 클릭과 정보 입력을 구분하고 증거·가족 경고 경로를 함께 확인하세요.")
                }
                if (hasFamilyVerificationSignal) {
                    add("가족·딥페이크·번호 변경 신호가 있어 새 연락 수단이 아닌 평소 별도 경로로 가족 확인을 도와주세요.")
                }
                if (hasSecrecyImpersonationSignal) {
                    add("비밀 유지·기관 사칭 압박이 있어 먼저 멈추고 비밀값을 보호한 뒤 행동 종류와 공식 도움 경로를 확인하세요.")
                }
                if (isEmpty()) {
                    add("현재 신호만으로 실제 상황을 증명할 수 없으므로 비밀값 없이 행동 종류와 기존 안전 경로를 함께 확인하세요.")
                }
            }.joinToString(" ")
        }

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW ->
                "가족 준비가 아직 부족해도 괜찮습니다. 검토 뒤 준비 로드맵과 연습 코치를 차분히 시작하세요."
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중입니다. 지금은 있는 콜백 경로만 활용하고 빠진 준비는 검토 뒤 차분히 보완하세요."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있어도 이번 상황의 사실이 확인된 것은 아닙니다. 저장된 루틴을 비밀값 공유 없이 차분히 활용하세요."
        }
        return "$situationCopy $readinessCopy"
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 검토는 사용자가 직접 여는 수동 안내이며 현재 분석의 위험도·일치 신호와 값 없는 가족 준비 수준만 기기 안의 로컬 규칙으로 사용합니다.",
        "분석 신호는 실제 송금·정보 입력·앱 설치·링크 클릭이나 사기 여부를 증명하지 않습니다. 보호자는 사용자가 실제로 한 행동의 종류만 확인하세요.",
        "비밀번호·인증번호·PIN·카드 비밀값·안전 질문 답·콜백 내용·정확한 연락처·주민번호·링크 원문을 묻거나 수집하거나 기록하지 않습니다.",
        "선택한 카드, 보호자 질문·답변·메모, 완료·체크 상태, 점수, 읽음·진행 상태, 공유 이력, 보호자 검토 이력을 저장하지 않습니다.",
        "화면을 열거나 버튼을 누르는 것만으로 사건 기록을 추가·수정·삭제하지 않습니다.",
        "문자·메시지·통화·연락처를 읽거나 감시하지 않고 가족을 자동 모니터링하지 않습니다.",
        "공유 버튼은 연락처를 읽거나 받는 사람을 고르지 않으며, 사용자가 공유 앱과 대상을 직접 선택하기 전에는 메시지를 자동으로 보내지 않습니다.",
        "네트워크를 사용하거나 새 Android 권한을 요청하지 않고 전화·문자를 자동 실행하거나 차단하지 않습니다.",
        "이 검토는 안전·사기·노출 여부를 확정하지 않으며 악성 앱 제거, 계정·카드 정지, 피해금 회수, 법률 자문이나 결과를 보장하지 않습니다. 데이터를 자동 삭제하지도 않습니다.",
        "이 검토 빌더와 공유 요약은 보호자나 가족의 비밀값에 접근하지 않으며, 검토 화면은 실제 값을 표시하거나 공유하지 않습니다. 모든 실제 조치는 사용자가 공식 경로에서 직접 진행합니다."
    )

    private fun buildFamilyShareSummary(
        recommendationTitle: String,
        recommendedCardIds: List<TrustedHelperReviewCardId>,
        recommendedActions: List<TrustedHelperReviewAction>,
        cards: List<TrustedHelperReviewCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }

        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 보호자 안전 검토 요약")
            appendLine()
            appendLine(recommendationTitle)
            recommendedCardIds.forEach { cardId ->
                val card = cardsById.getValue(cardId)
                appendLine("- [${card.stage.label} · ${card.category.label}] ${card.title}")
            }
            appendLine()
            appendLine("먼저 이어갈 앱 행동")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("전체 보호자 검토 순서와 일반 안전 행동")
            cards.forEach { card ->
                appendLine("- [${card.stage.label} · ${card.category.label}] ${card.title}")
                appendLine("  먼저: ${card.firstSafeMove}")
                appendLine("  앱 행동: ${card.routeActions.joinToString(" / ") { action -> action.label }}")
            }
            appendLine()
            appendLine("함께 지킬 일반 안전 행동")
            appendLine("- 비난하지 않고 현재 압박과 추가 행동부터 멈춥니다.")
            appendLine("- 비밀값은 묻거나 옮기지 않고 행동 종류만 확인합니다.")
            appendLine("- 새 연락 수단 대신 평소 가족 경로와 공식 경로를 사용합니다.")
            appendLine()
            appendLine("이 요약은 로컬·무상태 수동 안내이며 실제 노출이나 안전을 증명하지 않습니다.")
            appendLine("앱은 연락처를 읽거나 받는 사람을 고르지 않고 메시지를 자동으로 보내지 않습니다.")
            append(
                "붙여넣은 원문·개인정보 가림 단서·안전 질문·콜백 내용·연락처 값과 전화·계좌·" +
                    "비밀번호·인증번호·PIN·링크·도메인·신분정보 값은 이 요약에 포함하지 않았습니다."
            )
        }.trim()
    }
}
