package com.mongmong.familyshield

enum class FamilyResponseRoleId(val displayOrder: Int) {
    PAUSE_LEAD(1),
    CALLBACK_VERIFIER(2),
    EXPOSURE_SORTER(3),
    ACCOUNT_DEVICE_PROTECTOR(4),
    EVIDENCE_KEEPER(5),
    OFFICIAL_HELP_CONNECTOR(6),
    FAMILY_ALERT_RELAY(7),
    FOLLOW_UP_COORDINATOR(8)
}

enum class FamilyResponseRoleCategory(val label: String) {
    STOP_COORDINATION("멈춤·안심 진행"),
    FAMILY_VERIFICATION("가족 본인확인"),
    EXPOSURE_CLASSIFICATION("노출 종류 분류"),
    ACCOUNT_DEVICE_PROTECTION("계정·기기 보호"),
    EVIDENCE_PRESERVATION("증거·시간 정리"),
    OFFICIAL_ASSISTANCE("공식 도움 연결"),
    FAMILY_WARNING("비밀 없는 가족 경고"),
    FOLLOW_UP_READINESS("후속 점검·준비")
}

enum class FamilyResponseRolePriority(val rank: Int, val label: String) {
    STOP_FIRST(0, "지금 먼저"),
    VERIFY_SEPARATELY(1, "별도 확인"),
    PROTECT_NOW(2, "보호·보존"),
    CONNECT_OFFICIAL_HELP(3, "공식 경로 연결"),
    SHARE_WARNING(4, "일반 경고 전달"),
    FOLLOW_UP(5, "차분히 이어가기")
}

enum class FamilyResponseRoleActionId {
    QUICK_STOP,
    EXPOSURE_IMPACT_TRIAGE,
    TRUSTED_HELPER_REVIEW,
    TRUSTED_HELPER_HANDOFF,
    CHECKER,
    ANALYSIS_RESULT,
    FAMILY_CALLBACK_CARD,
    SAFE_CALL_SCRIPTS,
    FAMILY_ALERT_MESSAGES,
    FAMILY_READINESS_ROADMAP,
    FAMILY_SAFETY_DRILL_COACH,
    FOLLOW_UP_CHECK_IN_PLANNER,
    RECOVERY_PLAN,
    ACCOUNT_LOCKDOWN,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    INCIDENT_TIMELINE_BRIEFING,
    SAFETY_FLOW_ROUTER,
    PRIVACY_CENTER
}

data class FamilyResponseRoleAction(
    val id: FamilyResponseRoleActionId,
    val label: String
)

data class FamilyResponseRoleCard(
    val id: FamilyResponseRoleId,
    val displayOrder: Int,
    val roleLabel: String,
    val mission: String,
    val safeHandoffSentence: String,
    val category: FamilyResponseRoleCategory,
    val whenToUse: String,
    val doNotDoBoundaries: List<String>,
    val priority: FamilyResponseRolePriority,
    val primaryAction: FamilyResponseRoleAction,
    val secondaryActions: List<FamilyResponseRoleAction>
) {
    val routeActions: List<FamilyResponseRoleAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class FamilyResponseRoleBoard(
    val title: String,
    val intro: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedRoleIds: List<FamilyResponseRoleId>,
    val recommendedActions: List<FamilyResponseRoleAction>,
    val roleCards: List<FamilyResponseRoleCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedRoles: List<FamilyResponseRoleCard>
        get() = recommendedRoleIds.mapNotNull { roleId ->
            roleCards.find { card -> card.id == roleId }
        }

    val recommendedActionIds: List<FamilyResponseRoleActionId>
        get() = recommendedActions.map { action -> action.id }

    val cards: List<FamilyResponseRoleCard>
        get() = roleCards

    val roles: List<FamilyResponseRoleCard>
        get() = roleCards
}

object FamilyResponseRoleBoardBuilder {
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
        readiness: FamilyReadiness,
        incidentCount: Int = 0
    ): FamilyResponseRoleBoard {
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val hasDefaultState = analysis == null || signals.isEmpty()
        val hasFinancialProtectionSignal = analysis?.riskLevel == RiskLevel.HIGH ||
            signals.any { signal -> signal in financialProtectionSignals }
        val hasRemoteAccessSignal = signals.any { signal -> signal in remoteAccessSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasFamilyVerificationSignal =
            signals.any { signal -> signal in familyVerificationSignals }
        val hasSecrecyImpersonationSignal =
            signals.any { signal -> signal in secrecyImpersonationSignals }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0
        val needsReadinessSupport = readiness.level != ReadinessLevel.READY

        val recommendedRoleSet = linkedSetOf<FamilyResponseRoleId>()
        if (hasDefaultState) {
            recommendedRoleSet += FamilyResponseRoleId.PAUSE_LEAD
            recommendedRoleSet += FamilyResponseRoleId.CALLBACK_VERIFIER
            recommendedRoleSet += FamilyResponseRoleId.EXPOSURE_SORTER
            recommendedRoleSet += FamilyResponseRoleId.FOLLOW_UP_COORDINATOR
        }
        if (hasFinancialProtectionSignal) {
            recommendedRoleSet += FamilyResponseRoleId.ACCOUNT_DEVICE_PROTECTOR
            recommendedRoleSet += FamilyResponseRoleId.EVIDENCE_KEEPER
            recommendedRoleSet += FamilyResponseRoleId.OFFICIAL_HELP_CONNECTOR
            recommendedRoleSet += FamilyResponseRoleId.FOLLOW_UP_COORDINATOR
        }
        if (hasRemoteAccessSignal) {
            recommendedRoleSet += FamilyResponseRoleId.ACCOUNT_DEVICE_PROTECTOR
            recommendedRoleSet += FamilyResponseRoleId.EVIDENCE_KEEPER
        }
        if (hasLinkSignal) {
            recommendedRoleSet += FamilyResponseRoleId.EXPOSURE_SORTER
            recommendedRoleSet += FamilyResponseRoleId.EVIDENCE_KEEPER
            recommendedRoleSet += FamilyResponseRoleId.FAMILY_ALERT_RELAY
            recommendedRoleSet += FamilyResponseRoleId.FOLLOW_UP_COORDINATOR
        }
        if (hasFamilyVerificationSignal) {
            recommendedRoleSet += FamilyResponseRoleId.CALLBACK_VERIFIER
            recommendedRoleSet += FamilyResponseRoleId.FAMILY_ALERT_RELAY
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedRoleSet += FamilyResponseRoleId.PAUSE_LEAD
            recommendedRoleSet += FamilyResponseRoleId.EXPOSURE_SORTER
            recommendedRoleSet += FamilyResponseRoleId.OFFICIAL_HELP_CONNECTOR
        }
        if (hasSavedIncident) {
            recommendedRoleSet += FamilyResponseRoleId.EVIDENCE_KEEPER
            recommendedRoleSet += FamilyResponseRoleId.FOLLOW_UP_COORDINATOR
        }
        if (needsReadinessSupport) {
            recommendedRoleSet += FamilyResponseRoleId.CALLBACK_VERIFIER
            recommendedRoleSet += FamilyResponseRoleId.FOLLOW_UP_COORDINATOR
        }
        if (recommendedRoleSet.isEmpty()) {
            recommendedRoleSet += FamilyResponseRoleId.PAUSE_LEAD
            recommendedRoleSet += FamilyResponseRoleId.CALLBACK_VERIFIER
            recommendedRoleSet += FamilyResponseRoleId.EXPOSURE_SORTER
            recommendedRoleSet += FamilyResponseRoleId.FOLLOW_UP_COORDINATOR
        }
        val recommendedRoleIds = FamilyResponseRoleId.entries.filter { roleId ->
            roleId in recommendedRoleSet
        }

        val recommendedActionSet = linkedSetOf<FamilyResponseRoleActionId>()
        if (hasDefaultState) {
            recommendedActionSet += FamilyResponseRoleActionId.QUICK_STOP
            recommendedActionSet += FamilyResponseRoleActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += if (analysis == null) {
                FamilyResponseRoleActionId.CHECKER
            } else {
                FamilyResponseRoleActionId.ANALYSIS_RESULT
            }
            recommendedActionSet += FamilyResponseRoleActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += FamilyResponseRoleActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (hasFinancialProtectionSignal) {
            recommendedActionSet += FamilyResponseRoleActionId.RECOVERY_PLAN
            recommendedActionSet += FamilyResponseRoleActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += FamilyResponseRoleActionId.EVIDENCE_KIT
            recommendedActionSet += FamilyResponseRoleActionId.OFFICIAL_HELP_DIRECTORY
            if (analysis != null) recommendedActionSet += FamilyResponseRoleActionId.REPORT_PREP
            recommendedActionSet += FamilyResponseRoleActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (hasRemoteAccessSignal) {
            recommendedActionSet += FamilyResponseRoleActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += FamilyResponseRoleActionId.EVIDENCE_KIT
            recommendedActionSet += FamilyResponseRoleActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += FamilyResponseRoleActionId.RECOVERY_PLAN
        }
        if (hasLinkSignal) {
            recommendedActionSet += FamilyResponseRoleActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += FamilyResponseRoleActionId.EVIDENCE_KIT
            recommendedActionSet += FamilyResponseRoleActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += FamilyResponseRoleActionId.FOLLOW_UP_CHECK_IN_PLANNER
            recommendedActionSet += FamilyResponseRoleActionId.PRIVACY_CENTER
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += FamilyResponseRoleActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += FamilyResponseRoleActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += FamilyResponseRoleActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedActionSet += FamilyResponseRoleActionId.QUICK_STOP
            recommendedActionSet += FamilyResponseRoleActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += FamilyResponseRoleActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (hasSavedIncident) {
            recommendedActionSet += FamilyResponseRoleActionId.EVIDENCE_KIT
            recommendedActionSet += FamilyResponseRoleActionId.INCIDENT_TIMELINE_BRIEFING
            recommendedActionSet += FamilyResponseRoleActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (needsReadinessSupport) {
            recommendedActionSet += FamilyResponseRoleActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += FamilyResponseRoleActionId.FAMILY_READINESS_ROADMAP
            recommendedActionSet += FamilyResponseRoleActionId.FAMILY_SAFETY_DRILL_COACH
        }
        if (recommendedActionSet.isEmpty()) {
            recommendedActionSet += FamilyResponseRoleActionId.QUICK_STOP
            recommendedActionSet += FamilyResponseRoleActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += FamilyResponseRoleActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += FamilyResponseRoleActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        val recommendedActions = recommendedActionSet.map(::actionFor)

        val roleCards = buildRoleCards(hasAnalysis = analysis != null)
        val recommendationTitle = when {
            analysis == null -> "분석 전 기본 가족 역할"
            hasFinancialProtectionSignal || hasRemoteAccessSignal -> "먼저 나눌 보호·보존 역할"
            signals.isEmpty() -> "일치 신호가 없어도 나눌 기본 역할"
            else -> "현재 신호에서 먼저 나눌 가족 역할"
        }

        return FamilyResponseRoleBoard(
            title = "가족 대응 역할 보드",
            intro =
                "가족이 사람 이름이나 비밀값을 입력하지 않고 일반 역할을 말로 나눠 맡는 로컬 전용·" +
                    "무상태 수동 안내입니다. 앱은 담당자를 배정하거나 역할 진행을 추적하지 않습니다.",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                analysis = analysis,
                readinessLevel = readiness.level,
                signals = signals,
                hasFinancialProtectionSignal = hasFinancialProtectionSignal,
                hasRemoteAccessSignal = hasRemoteAccessSignal,
                hasLinkSignal = hasLinkSignal,
                hasFamilyVerificationSignal = hasFamilyVerificationSignal,
                hasSecrecyImpersonationSignal = hasSecrecyImpersonationSignal,
                hasSavedIncident = hasSavedIncident
            ),
            recommendedRoleIds = recommendedRoleIds,
            recommendedActions = recommendedActions,
            roleCards = roleCards,
            privacySafetyNotes = privacySafetyNotes(),
            familyShareSummary = buildFamilyShareSummary(
                recommendedRoleIds = recommendedRoleIds,
                recommendedActions = recommendedActions,
                roleCards = roleCards
            )
        )
    }

    private fun buildRoleCards(hasAnalysis: Boolean): List<FamilyResponseRoleCard> {
        fun action(id: FamilyResponseRoleActionId): FamilyResponseRoleAction = actionFor(id)
        fun currentResultAction(): FamilyResponseRoleAction = action(
            if (hasAnalysis) {
                FamilyResponseRoleActionId.ANALYSIS_RESULT
            } else {
                FamilyResponseRoleActionId.CHECKER
            }
        )

        return listOf(
            FamilyResponseRoleCard(
                id = FamilyResponseRoleId.PAUSE_LEAD,
                displayOrder = FamilyResponseRoleId.PAUSE_LEAD.displayOrder,
                roleLabel = "멈춤 진행자",
                mission = "추가 연락·송금·정보 제공·앱 행동을 멈추고 가족이 차분히 다음 경로를 고르도록 이끕니다.",
                safeHandoffSentence =
                    "지금은 모두 멈추고, 한 사람은 긴급 멈춤 순서를 읽고 다른 가족은 비밀값 없이 다음 역할을 골라 주세요.",
                category = FamilyResponseRoleCategory.STOP_COORDINATION,
                whenToUse = "상대가 재촉하거나 가족이 불안해 무엇부터 할지 정하기 어려울 때 먼저 맡습니다.",
                doNotDoBoundaries = listOf(
                    "누가 속았는지 따지거나 서둘러 결론내리지 마세요.",
                    "분석 신호만으로 사기 여부나 안전 여부가 확인됐다고 말하지 마세요."
                ),
                priority = FamilyResponseRolePriority.STOP_FIRST,
                primaryAction = action(FamilyResponseRoleActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(FamilyResponseRoleActionId.TRUSTED_HELPER_REVIEW),
                    action(FamilyResponseRoleActionId.TRUSTED_HELPER_HANDOFF),
                    currentResultAction(),
                    action(FamilyResponseRoleActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            FamilyResponseRoleCard(
                id = FamilyResponseRoleId.CALLBACK_VERIFIER,
                displayOrder = FamilyResponseRoleId.CALLBACK_VERIFIER.displayOrder,
                roleLabel = "가족 본인확인 담당",
                mission = "새 연락 수단이 아닌 평소 알던 별도 경로에서 가족 본인 여부를 직접 확인하도록 돕습니다.",
                safeHandoffSentence =
                    "가족 확인 역할은 평소 별도 경로로 콜백만 진행하고, 안전 질문의 답이나 연락처 값은 공유하지 말아 주세요.",
                category = FamilyResponseRoleCategory.FAMILY_VERIFICATION,
                whenToUse = "가족 사칭, 목소리 합성 의심, 번호 변경 또는 긴급상황 주장이 있을 때 맡습니다.",
                doNotDoBoundaries = listOf(
                    "안전단어 답, 정확한 전화번호, 콜백 문장 같은 실제 값을 묻거나 적지 마세요.",
                    "프로필·이름·사진·목소리만으로 본인 확인이 끝났다고 단정하지 마세요."
                ),
                priority = FamilyResponseRolePriority.VERIFY_SEPARATELY,
                primaryAction = action(FamilyResponseRoleActionId.FAMILY_CALLBACK_CARD),
                secondaryActions = listOf(
                    action(FamilyResponseRoleActionId.SAFE_CALL_SCRIPTS),
                    action(FamilyResponseRoleActionId.FAMILY_ALERT_MESSAGES),
                    action(FamilyResponseRoleActionId.TRUSTED_HELPER_HANDOFF)
                )
            ),
            FamilyResponseRoleCard(
                id = FamilyResponseRoleId.EXPOSURE_SORTER,
                displayOrder = FamilyResponseRoleId.EXPOSURE_SORTER.displayOrder,
                roleLabel = "노출 분류 담당",
                mission = "사기 원문이나 실제 값을 받아 적지 않고 사용자가 한 행동을 송금·정보·링크·앱 같은 종류로만 구분합니다.",
                safeHandoffSentence =
                    "실제 내용은 말하지 말고 어떤 행동 종류가 있었는지만 노출 분류 화면에서 함께 확인해 주세요.",
                category = FamilyResponseRoleCategory.EXPOSURE_CLASSIFICATION,
                whenToUse = "무엇을 했는지 불분명하거나 다음 보호 경로를 고르기 전에 행동 범주를 확인할 때 맡습니다.",
                doNotDoBoundaries = listOf(
                    "사기 원문, 링크, 계좌·인증·신분정보의 실제 값을 다시 보여 달라고 하지 마세요.",
                    "분석 신호를 실제 노출이나 피해의 증거로 여기지 마세요."
                ),
                priority = FamilyResponseRolePriority.VERIFY_SEPARATELY,
                primaryAction = action(FamilyResponseRoleActionId.EXPOSURE_IMPACT_TRIAGE),
                secondaryActions = listOf(
                    currentResultAction(),
                    action(FamilyResponseRoleActionId.TRUSTED_HELPER_REVIEW),
                    action(FamilyResponseRoleActionId.SAFETY_FLOW_ROUTER),
                    action(FamilyResponseRoleActionId.PRIVACY_CENTER)
                )
            ),
            FamilyResponseRoleCard(
                id = FamilyResponseRoleId.ACCOUNT_DEVICE_PROTECTOR,
                displayOrder = FamilyResponseRoleId.ACCOUNT_DEVICE_PROTECTOR.displayOrder,
                roleLabel = "계정·기기 보호 담당",
                mission = "가족이 공식 경로와 안전한 기기에서 계정·결제·기기 보호 순서를 직접 실행하도록 안내합니다.",
                safeHandoffSentence =
                    "계정·기기 역할은 비밀번호나 인증값을 받지 않고, 보호 목록과 수습 순서만 함께 확인해 주세요.",
                category = FamilyResponseRoleCategory.ACCOUNT_DEVICE_PROTECTION,
                whenToUse = "송금·결제·계정정보 제공, 앱 설치, 원격제어 또는 화면공유 가능성을 직접 확인해야 할 때 맡습니다.",
                doNotDoBoundaries = listOf(
                    "비밀번호·OTP·PIN·보안카드 값이나 정확한 계좌번호를 수집하거나 대신 입력하지 마세요.",
                    "앱이 악성 프로그램을 제거했거나 계정·카드를 정지했다고 말하지 마세요."
                ),
                priority = FamilyResponseRolePriority.PROTECT_NOW,
                primaryAction = action(FamilyResponseRoleActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(FamilyResponseRoleActionId.RECOVERY_PLAN),
                    action(FamilyResponseRoleActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(FamilyResponseRoleActionId.OFFICIAL_HELP_DIRECTORY)
                )
            ),
            FamilyResponseRoleCard(
                id = FamilyResponseRoleId.EVIDENCE_KEEPER,
                displayOrder = FamilyResponseRoleId.EVIDENCE_KEEPER.displayOrder,
                roleLabel = "증거 보존 담당",
                mission = "필요한 자료와 시간·행동 종류를 수동으로 정리하고 기존 증거·타임라인 경로를 안내합니다.",
                safeHandoffSentence =
                    "증거 역할은 원문을 널리 보내지 말고, 필요한 자료 종류와 시간 순서만 보존 화면에서 확인해 주세요.",
                category = FamilyResponseRoleCategory.EVIDENCE_PRESERVATION,
                whenToUse = "링크·앱·송금·정보 제공 가능성이 있거나 공식 상담 전에 자료 종류를 정리할 때 맡습니다.",
                doNotDoBoundaries = listOf(
                    "의심 링크를 다시 열거나 사칭 상대에게 증거를 다시 보내지 마세요.",
                    "사기 원문·비밀값·신분정보를 가족 전체나 불필요한 사람에게 넓게 전달하지 마세요."
                ),
                priority = FamilyResponseRolePriority.PROTECT_NOW,
                primaryAction = action(FamilyResponseRoleActionId.EVIDENCE_KIT),
                secondaryActions = listOf(
                    action(FamilyResponseRoleActionId.INCIDENT_TIMELINE_BRIEFING),
                    action(FamilyResponseRoleActionId.REPORT_PREP),
                    action(FamilyResponseRoleActionId.FOLLOW_UP_CHECK_IN_PLANNER)
                )
            ),
            FamilyResponseRoleCard(
                id = FamilyResponseRoleId.OFFICIAL_HELP_CONNECTOR,
                displayOrder = FamilyResponseRoleId.OFFICIAL_HELP_CONNECTOR.displayOrder,
                roleLabel = "공식 도움 연결 담당",
                mission = "평소 공식 대표 경로와 앱 안의 신고·상담 준비 화면을 찾아 가족이 직접 문의하도록 돕습니다.",
                safeHandoffSentence =
                    "공식 도움 역할은 결과를 약속하지 말고, 필요한 창구와 일반 상담 준비 항목만 함께 확인해 주세요.",
                category = FamilyResponseRoleCategory.OFFICIAL_ASSISTANCE,
                whenToUse = "금융·계정·기관 사칭 신호나 실제 행동 가능성이 있어 공식 확인과 접수를 준비할 때 맡습니다.",
                doNotDoBoundaries = listOf(
                    "상대가 알려준 번호·링크·앱을 공식 경로라고 믿지 마세요.",
                    "법률·금융 판단, 지급정지, 피해금 회수 또는 접수 결과를 보장하지 마세요."
                ),
                priority = FamilyResponseRolePriority.CONNECT_OFFICIAL_HELP,
                primaryAction = action(FamilyResponseRoleActionId.OFFICIAL_HELP_DIRECTORY),
                secondaryActions = listOf(
                    action(FamilyResponseRoleActionId.REPORT_PREP),
                    action(FamilyResponseRoleActionId.RECOVERY_PLAN),
                    action(FamilyResponseRoleActionId.EVIDENCE_KIT)
                )
            ),
            FamilyResponseRoleCard(
                id = FamilyResponseRoleId.FAMILY_ALERT_RELAY,
                displayOrder = FamilyResponseRoleId.FAMILY_ALERT_RELAY.displayOrder,
                roleLabel = "가족 경고 전달 담당",
                mission = "가족이 같은 수법에 급히 반응하지 않도록 원문과 비밀값 없는 일반 경고를 직접 공유합니다.",
                safeHandoffSentence =
                    "가족 경고 역할은 사기 원문이나 비밀을 붙이지 말고, 멈춤과 별도 확인 안내만 전달해 주세요.",
                category = FamilyResponseRoleCategory.FAMILY_WARNING,
                whenToUse = "가족 사칭·번호 변경·링크·기관 압박 같은 수법을 다른 가족도 조심해야 할 때 맡습니다.",
                doNotDoBoundaries = listOf(
                    "사기 원문, 링크, 전화·계좌·인증정보, 안전 질문 답을 경고문에 넣지 마세요.",
                    "앱이 받는 사람을 골랐거나 메시지를 자동으로 보냈다고 말하지 마세요."
                ),
                priority = FamilyResponseRolePriority.SHARE_WARNING,
                primaryAction = action(FamilyResponseRoleActionId.FAMILY_ALERT_MESSAGES),
                secondaryActions = listOf(
                    action(FamilyResponseRoleActionId.FAMILY_CALLBACK_CARD),
                    action(FamilyResponseRoleActionId.SAFE_CALL_SCRIPTS),
                    action(FamilyResponseRoleActionId.TRUSTED_HELPER_HANDOFF)
                )
            ),
            FamilyResponseRoleCard(
                id = FamilyResponseRoleId.FOLLOW_UP_COORDINATOR,
                displayOrder = FamilyResponseRoleId.FOLLOW_UP_COORDINATOR.displayOrder,
                roleLabel = "후속 점검 담당",
                mission = "가족이 직접 다시 확인할 시점과 준비·연습·개인정보 점검 경로를 차분히 이어갑니다.",
                safeHandoffSentence =
                    "후속 역할은 알림을 예약하거나 감시하지 않고, 가족이 직접 볼 점검·준비 화면만 함께 골라 주세요.",
                category = FamilyResponseRoleCategory.FOLLOW_UP_READINESS,
                whenToUse = "즉시 멈춤 뒤 며칠 동안 다시 확인하거나 가족의 콜백·연습 준비를 보완할 때 맡습니다.",
                doNotDoBoundaries = listOf(
                    "알림·알람·캘린더 일정이나 백그라운드 감시가 설정됐다고 말하지 마세요.",
                    "담당자 이름, 점검 답변·메모, 완료·체크 상태나 진행 이력을 앱에 기록하려 하지 마세요."
                ),
                priority = FamilyResponseRolePriority.FOLLOW_UP,
                primaryAction = action(FamilyResponseRoleActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                secondaryActions = listOf(
                    action(FamilyResponseRoleActionId.FAMILY_READINESS_ROADMAP),
                    action(FamilyResponseRoleActionId.FAMILY_SAFETY_DRILL_COACH),
                    action(FamilyResponseRoleActionId.PRIVACY_CENTER),
                    action(FamilyResponseRoleActionId.SAFETY_FLOW_ROUTER)
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readinessLevel: ReadinessLevel,
        signals: Set<ScamSignal>,
        hasFinancialProtectionSignal: Boolean,
        hasRemoteAccessSignal: Boolean,
        hasLinkSignal: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasSecrecyImpersonationSignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (analysis == null || signals.isEmpty()) {
                add(
                    "현재 분석이 없거나 일치 신호가 없습니다. 이를 안전 판정으로 보지 않고 멈춤·가족 콜백·" +
                        "노출 종류 분류·후속 점검 역할부터 수동으로 나누세요."
                )
            }
            if (hasFinancialProtectionSignal) {
                add(
                    "위험도가 높거나 금융·결제·계정 관련 신호가 있어 계정·기기 보호, 증거 보존, " +
                        "공식 도움, 후속 점검 역할을 함께 권합니다."
                )
            }
            if (hasRemoteAccessSignal) {
                add("앱·원격제어·화면공유 관련 신호가 있어 계정·기기 보호와 증거 보존을 먼저 보고 보호자 검토를 함께 권합니다.")
            }
            if (hasLinkSignal) {
                add("링크·스미싱 관련 신호가 있어 노출 종류, 증거, 비밀 없는 가족 경고, 개인정보·후속 점검 경로를 권합니다.")
            }
            if (hasFamilyVerificationSignal) {
                add("가족·목소리 합성·번호 변경 관련 신호가 있어 평소 별도 경로의 본인확인과 일반 가족 경고 역할을 권합니다.")
            }
            if (hasSecrecyImpersonationSignal) {
                add("비밀 유지·기관 사칭 압박 신호가 있어 먼저 멈추고 행동 종류를 분류한 뒤 공식 경로를 직접 확인하도록 권합니다.")
            }
            if (hasSavedIncident) {
                add("저장된 사건이 있어도 사건 내용은 읽지 않으며, 증거 보존·사건 타임라인·후속 점검 경로만 함께 권합니다.")
            }
            if (isEmpty()) {
                add("현재 신호만으로 사실을 확정하지 않고 기본 역할과 직접 확인 경로를 차분히 나누세요.")
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW ->
                "가족 준비가 아직 부족해도 괜찮습니다. 본인확인 담당과 준비 로드맵·연습 코치부터 작게 시작하세요."
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중입니다. 마련된 콜백 습관을 활용하고 로드맵과 연습으로 빈 부분을 차분히 보완하세요."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있어도 이번 상황의 사실이 확인된 것은 아닙니다. 준비한 콜백과 역할을 직접 다시 확인하세요."
        }
        return "$situationCopy $readinessCopy"
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 보드는 가족이 옆에서 직접 상의해 일반 역할을 나누는 로컬 전용·무상태 수동 안내입니다.",
        "가족 이름·연락처, 역할 담당자·선택, 비밀번호·OTP/PIN, 안전 질문 답, 정확한 계좌번호, 원문·링크·메모를 묻거나 저장하지 않습니다.",
        "체크 표시·완료 상태·점수·읽음·진행 상태·공유 이력·역할 보드 이력을 저장하거나 이름 있는 배정을 추적하지 않습니다.",
        "문자·메시지·통화·연락처·계정·기기를 읽거나 감시하지 않고 받는 사람을 고르거나 메시지를 자동으로 보내지 않습니다.",
        "네트워크를 사용하거나 새 Android 권한을 요청하지 않으며 전화·문자를 자동 실행하거나 차단하지 않습니다.",
        "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커를 만들거나 예약하지 않습니다.",
        "화면을 열거나 역할·경로 버튼을 눌러도 사건 기록을 자동으로 추가·수정·삭제하지 않습니다.",
        "추천은 위험도·일치 신호, 값 없는 준비 수준, 저장 사건 유무만 사용하며 사기 원문·가린 단서·안전 계획 값·사건별 내용을 사용하지 않습니다.",
        "이 안내는 안전·피해·본인확인 완료를 증명하지 않으며 링크 검사, 악성 앱 제거, 계정·카드 정지, 피해금 회수, 법률·금융 결과를 수행하거나 보장하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedRoleIds: List<FamilyResponseRoleId>,
        recommendedActions: List<FamilyResponseRoleAction>,
        roleCards: List<FamilyResponseRoleCard>
    ): String {
        val cardsById = roleCards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 수동 가족 대응 역할 보드")
            appendLine()
            appendLine("먼저 나눌 일반 역할")
            recommendedRoleIds.forEach { roleId ->
                val card = cardsById.getValue(roleId)
                appendLine("- [${card.category.label}] ${card.roleLabel}")
                appendLine("  · 인계: ${card.safeHandoffSentence}")
            }
            appendLine()
            appendLine("먼저 열어볼 앱 행동")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("전체 일반 역할과 앱 경로")
            roleCards.forEach { card ->
                appendLine("- [${card.category.label}] ${card.roleLabel}")
                appendLine("  · 인계: ${card.safeHandoffSentence}")
                appendLine("  · 앱 경로: ${card.routeActions.joinToString(" / ") { action -> action.label }}")
            }
            appendLine()
            append("사람 이름이나 비밀값을 넣지 않은 수동 역할 안내이며, 가족이 직접 상의해 필요한 경로를 고릅니다.")
        }.trim()
    }

    private fun actionFor(id: FamilyResponseRoleActionId): FamilyResponseRoleAction =
        FamilyResponseRoleAction(
            id = id,
            label = when (id) {
                FamilyResponseRoleActionId.QUICK_STOP -> "긴급 멈춤 모드"
                FamilyResponseRoleActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
                FamilyResponseRoleActionId.TRUSTED_HELPER_REVIEW -> "보호자와 함께 안전 검토"
                FamilyResponseRoleActionId.TRUSTED_HELPER_HANDOFF -> "보호자 도움 요청 카드"
                FamilyResponseRoleActionId.CHECKER -> "의심 내용 검사"
                FamilyResponseRoleActionId.ANALYSIS_RESULT -> "현재 분석 결과"
                FamilyResponseRoleActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 확인 카드"
                FamilyResponseRoleActionId.SAFE_CALL_SCRIPTS -> "안전 확인 전화 스크립트"
                FamilyResponseRoleActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
                FamilyResponseRoleActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
                FamilyResponseRoleActionId.FAMILY_SAFETY_DRILL_COACH -> "가족 안전 연습 코치"
                FamilyResponseRoleActionId.FOLLOW_UP_CHECK_IN_PLANNER -> "며칠 뒤 후속 점검"
                FamilyResponseRoleActionId.RECOVERY_PLAN -> "24시간 수습 계획"
                FamilyResponseRoleActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
                FamilyResponseRoleActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
                FamilyResponseRoleActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처"
                FamilyResponseRoleActionId.REPORT_PREP -> "신고용 상담 준비 카드"
                FamilyResponseRoleActionId.INCIDENT_TIMELINE_BRIEFING -> "사건 타임라인 요약"
                FamilyResponseRoleActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
                FamilyResponseRoleActionId.PRIVACY_CENTER -> "개인정보 센터"
            }
        )
}
