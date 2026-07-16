package com.mongmong.familyshield

enum class FamilyReadinessRoadmapCardId(val displayOrder: Int) {
    SAFETY_PLAN_FOUNDATION(1),
    CALLBACK_ROUTINE_PRACTICE(2),
    RISK_SIGNAL_LEARNING(3),
    QUICK_STOP_PREPARATION(4),
    INCIDENT_RESPONSE_PREPARATION(5),
    EVIDENCE_ACCOUNT_PROTECTION(6),
    FAMILY_HELPER_NETWORK(7),
    PRIVACY_LOCAL_DATA_REVIEW(8)
}

enum class FamilyReadinessRoadmapStage(val label: String) {
    FOUNDATION("1단계 · 가족 기반"),
    VERIFY("2단계 · 직접 확인"),
    LEARN("3단계 · 위험 학습"),
    STOP("4단계 · 긴급 멈춤"),
    RESPOND("5단계 · 피해 대응"),
    PROTECT("6단계 · 증거·계정 보호"),
    CONNECT("7단계 · 가족 협력"),
    REVIEW("8단계 · 개인정보 점검")
}

enum class FamilyReadinessRoadmapStatus(val label: String) {
    START_NOW("지금 시작"),
    FOLLOW_UP("빠진 준비 보완"),
    PRACTICE("함께 연습"),
    CURRENT_CONTEXT("현재 신호 우선"),
    PREPARE_AHEAD("미리 준비"),
    ROUTINE_REVIEW("정기 점검")
}

enum class FamilyReadinessRoadmapPriority(val rank: Int, val label: String) {
    IMMEDIATE(0, "현재 상황 우선"),
    NEXT(1, "다음 준비"),
    MAINTENANCE(2, "꾸준히 점검")
}

enum class FamilyReadinessRoadmapActionId {
    QUICK_STOP,
    CHECKER,
    ANALYSIS_RESULT,
    RISK_SIGNAL_EXPLAINERS,
    SAMPLE_LIBRARY,
    FAMILY_CALLBACK_CARD,
    SAFETY_PLAN,
    SAFETY_FLOW_ROUTER,
    ACCOUNT_LOCKDOWN,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    RESPONSE_CENTER,
    FAMILY_ALERT_MESSAGES,
    TRUSTED_HELPER_HANDOFF,
    SAFE_CALL_SCRIPTS,
    RECOVERY_PLAN,
    FAMILY_SAFETY_DRILL_COACH,
    PRIVACY_CENTER
}

data class FamilyReadinessRoadmapAction(
    val id: FamilyReadinessRoadmapActionId,
    val label: String
)

data class FamilyReadinessRoadmapSummary(
    val level: ReadinessLevel,
    val levelLabel: String,
    val completedCount: Int,
    val totalCount: Int,
    val remainingCount: Int
)

data class FamilyReadinessRoadmapCard(
    val id: FamilyReadinessRoadmapCardId,
    val displayOrder: Int,
    val stage: FamilyReadinessRoadmapStage,
    val title: String,
    val readinessStatus: FamilyReadinessRoadmapStatus,
    val whyItMatters: String,
    val nextSmallSteps: List<String>,
    val avoidNotes: List<String>,
    val priority: FamilyReadinessRoadmapPriority,
    val primaryAction: FamilyReadinessRoadmapAction,
    val secondaryActions: List<FamilyReadinessRoadmapAction>
) {
    val primaryActionLabel: String
        get() = primaryAction.label

    val secondaryActionLabels: List<String>
        get() = secondaryActions.map { action -> action.label }
}

data class FamilyReadinessRoadmap(
    val title: String,
    val intro: String,
    val readinessSummary: FamilyReadinessRoadmapSummary,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<FamilyReadinessRoadmapCardId>,
    val recommendedActions: List<FamilyReadinessRoadmapAction>,
    val cards: List<FamilyReadinessRoadmapCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<FamilyReadinessRoadmapCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<FamilyReadinessRoadmapActionId>
        get() = recommendedActions.map { action -> action.id }
}

object FamilyReadinessRoadmapBuilder {
    private val familySignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.PHONE_NUMBER_CHANGE
    )

    private val financialSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.IMPERSONATION,
        ScamSignal.BAIT,
        ScamSignal.CREDENTIALS,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val remoteSignals = setOf(
        ScamSignal.APP_INSTALL,
        ScamSignal.LIVE_CALL_CONTROL
    )

    private val linkSignals = setOf(
        ScamSignal.LINK,
        ScamSignal.PARCEL_INVITATION_SMISHING
    )

    private val dangerousSignals = financialSignals + remoteSignals + ScamSignal.SECRECY

    fun build(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness
    ): FamilyReadinessRoadmap {
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val needsImmediateResponse = analysis?.riskLevel == RiskLevel.HIGH ||
            signals.any { signal -> signal in dangerousSignals }
        val situationCardIds = situationCardIds(
            signals = signals,
            needsImmediateResponse = needsImmediateResponse
        )
        val recommendedCardIds = recommendCardIds(
            analysis = analysis,
            readinessLevel = readiness.level,
            situationCardIds = situationCardIds
        )
        val recommendedActionIds = recommendActionIds(
            analysis = analysis,
            readinessLevel = readiness.level,
            signals = signals,
            needsImmediateResponse = needsImmediateResponse
        )
        val cards = buildCards(
            hasAnalysis = analysis != null,
            readinessLevel = readiness.level,
            recommendedCardIds = recommendedCardIds.toSet(),
            situationCardIds = situationCardIds.toSet()
        )
        val recommendedActions = recommendedActionIds.map { actionId ->
            actionFor(
                id = actionId,
                hasAnalysis = analysis != null,
                readinessLevel = readiness.level
            )
        }
        val safeTotalCount = readiness.totalCount.coerceAtLeast(0)
        val safeCompletedCount = readiness.completedCount.coerceIn(0, safeTotalCount)
        val readinessSummary = FamilyReadinessRoadmapSummary(
            level = readiness.level,
            levelLabel = readiness.level.label,
            completedCount = safeCompletedCount,
            totalCount = safeTotalCount,
            remainingCount = safeTotalCount - safeCompletedCount
        )

        return FamilyReadinessRoadmap(
            title = "가족 준비 로드맵",
            intro = "우리 가족의 현재 준비 수준과 로컬 분석 신호만 값 없이 살펴보고, 다음에 준비할 작은 행동을 기존 안전 화면으로 연결합니다.",
            readinessSummary = readinessSummary,
            recommendationTitle = when {
                needsImmediateResponse -> "현재 상황은 멈춤·대응 준비부터"
                analysis == null -> "분석 전 가족 준비 순서"
                signals.isEmpty() -> "일치 신호가 없어도 이어갈 준비"
                else -> "현재 신호에 맞춘 준비 순서"
            },
            recommendationExplanation = buildRecommendationExplanation(
                analysis = analysis,
                readinessLevel = readiness.level,
                signals = signals,
                needsImmediateResponse = needsImmediateResponse
            ),
            recommendedCardIds = recommendedCardIds,
            recommendedActions = recommendedActions,
            cards = cards,
            privacySafetyNotes = privacySafetyNotes(),
            familyShareSummary = buildFamilyShareSummary(
                recommendedCardIds = recommendedCardIds,
                cards = cards
            )
        )
    }

    private fun situationCardIds(
        signals: Set<ScamSignal>,
        needsImmediateResponse: Boolean
    ): List<FamilyReadinessRoadmapCardId> = buildList {
        if (needsImmediateResponse) {
            add(FamilyReadinessRoadmapCardId.QUICK_STOP_PREPARATION)
            add(FamilyReadinessRoadmapCardId.INCIDENT_RESPONSE_PREPARATION)
        }
        if (signals.any { signal -> signal in familySignals }) {
            add(FamilyReadinessRoadmapCardId.CALLBACK_ROUTINE_PRACTICE)
            add(FamilyReadinessRoadmapCardId.FAMILY_HELPER_NETWORK)
        }
        if (signals.any { signal -> signal in financialSignals }) {
            add(FamilyReadinessRoadmapCardId.INCIDENT_RESPONSE_PREPARATION)
            add(FamilyReadinessRoadmapCardId.EVIDENCE_ACCOUNT_PROTECTION)
        }
        if (signals.any { signal -> signal in remoteSignals }) {
            add(FamilyReadinessRoadmapCardId.EVIDENCE_ACCOUNT_PROTECTION)
            add(FamilyReadinessRoadmapCardId.FAMILY_HELPER_NETWORK)
        }
        if (signals.any { signal -> signal in linkSignals }) {
            add(FamilyReadinessRoadmapCardId.RISK_SIGNAL_LEARNING)
            add(FamilyReadinessRoadmapCardId.EVIDENCE_ACCOUNT_PROTECTION)
            add(FamilyReadinessRoadmapCardId.FAMILY_HELPER_NETWORK)
        }
        if (ScamSignal.SECRECY in signals) {
            add(FamilyReadinessRoadmapCardId.QUICK_STOP_PREPARATION)
            add(FamilyReadinessRoadmapCardId.FAMILY_HELPER_NETWORK)
        }
    }.distinct()

    private fun recommendCardIds(
        analysis: ScamAnalysis?,
        readinessLevel: ReadinessLevel,
        situationCardIds: List<FamilyReadinessRoadmapCardId>
    ): List<FamilyReadinessRoadmapCardId> {
        val recommended = linkedSetOf<FamilyReadinessRoadmapCardId>()
        if (analysis != null) {
            recommended += situationCardIds
        }
        recommended += readinessCardIds(readinessLevel)
        if (analysis == null || analysis.matchedSignals.isEmpty()) {
            recommended += FamilyReadinessRoadmapCardId.RISK_SIGNAL_LEARNING
        }
        return recommended.toList()
    }

    private fun readinessCardIds(
        level: ReadinessLevel
    ): List<FamilyReadinessRoadmapCardId> = when (level) {
        ReadinessLevel.LOW -> listOf(
            FamilyReadinessRoadmapCardId.SAFETY_PLAN_FOUNDATION,
            FamilyReadinessRoadmapCardId.CALLBACK_ROUTINE_PRACTICE,
            FamilyReadinessRoadmapCardId.QUICK_STOP_PREPARATION
        )
        ReadinessLevel.IN_PROGRESS -> listOf(
            FamilyReadinessRoadmapCardId.SAFETY_PLAN_FOUNDATION,
            FamilyReadinessRoadmapCardId.CALLBACK_ROUTINE_PRACTICE,
            FamilyReadinessRoadmapCardId.QUICK_STOP_PREPARATION,
            FamilyReadinessRoadmapCardId.PRIVACY_LOCAL_DATA_REVIEW
        )
        ReadinessLevel.READY -> listOf(
            FamilyReadinessRoadmapCardId.CALLBACK_ROUTINE_PRACTICE,
            FamilyReadinessRoadmapCardId.PRIVACY_LOCAL_DATA_REVIEW,
            FamilyReadinessRoadmapCardId.RISK_SIGNAL_LEARNING
        )
    }

    private fun recommendActionIds(
        analysis: ScamAnalysis?,
        readinessLevel: ReadinessLevel,
        signals: Set<ScamSignal>,
        needsImmediateResponse: Boolean
    ): List<FamilyReadinessRoadmapActionId> {
        val recommended = linkedSetOf<FamilyReadinessRoadmapActionId>()
        if (needsImmediateResponse) {
            recommended += FamilyReadinessRoadmapActionId.QUICK_STOP
            recommended += FamilyReadinessRoadmapActionId.RECOVERY_PLAN
        }
        if (signals.any { signal -> signal in familySignals }) {
            recommended += FamilyReadinessRoadmapActionId.FAMILY_CALLBACK_CARD
            recommended += FamilyReadinessRoadmapActionId.FAMILY_ALERT_MESSAGES
            recommended += FamilyReadinessRoadmapActionId.TRUSTED_HELPER_HANDOFF
        }
        if (signals.any { signal -> signal in financialSignals }) {
            recommended += FamilyReadinessRoadmapActionId.SAFE_CALL_SCRIPTS
            recommended += FamilyReadinessRoadmapActionId.ACCOUNT_LOCKDOWN
            recommended += FamilyReadinessRoadmapActionId.OFFICIAL_HELP_DIRECTORY
            recommended += FamilyReadinessRoadmapActionId.RECOVERY_PLAN
        }
        if (signals.any { signal -> signal in remoteSignals }) {
            recommended += FamilyReadinessRoadmapActionId.ACCOUNT_LOCKDOWN
            recommended += FamilyReadinessRoadmapActionId.EVIDENCE_KIT
            recommended += FamilyReadinessRoadmapActionId.TRUSTED_HELPER_HANDOFF
        }
        if (signals.any { signal -> signal in linkSignals }) {
            recommended += FamilyReadinessRoadmapActionId.RISK_SIGNAL_EXPLAINERS
            recommended += FamilyReadinessRoadmapActionId.EVIDENCE_KIT
            recommended += FamilyReadinessRoadmapActionId.FAMILY_ALERT_MESSAGES
        }
        if (ScamSignal.SECRECY in signals) {
            recommended += FamilyReadinessRoadmapActionId.QUICK_STOP
            recommended += FamilyReadinessRoadmapActionId.SAFE_CALL_SCRIPTS
            recommended += FamilyReadinessRoadmapActionId.FAMILY_ALERT_MESSAGES
        }
        recommended += readinessActionIds(readinessLevel)
        if (analysis == null) {
            recommended += FamilyReadinessRoadmapActionId.CHECKER
            recommended += FamilyReadinessRoadmapActionId.SAMPLE_LIBRARY
            recommended += FamilyReadinessRoadmapActionId.RISK_SIGNAL_EXPLAINERS
        } else if (signals.isEmpty()) {
            recommended += FamilyReadinessRoadmapActionId.ANALYSIS_RESULT
            recommended += FamilyReadinessRoadmapActionId.RISK_SIGNAL_EXPLAINERS
            recommended += FamilyReadinessRoadmapActionId.CHECKER
        }
        return recommended.toList()
    }

    private fun readinessActionIds(
        level: ReadinessLevel
    ): List<FamilyReadinessRoadmapActionId> = when (level) {
        ReadinessLevel.LOW -> listOf(
            FamilyReadinessRoadmapActionId.SAFETY_PLAN,
            FamilyReadinessRoadmapActionId.FAMILY_CALLBACK_CARD,
            FamilyReadinessRoadmapActionId.FAMILY_SAFETY_DRILL_COACH,
            FamilyReadinessRoadmapActionId.QUICK_STOP
        )
        ReadinessLevel.IN_PROGRESS -> listOf(
            FamilyReadinessRoadmapActionId.SAFETY_PLAN,
            FamilyReadinessRoadmapActionId.FAMILY_SAFETY_DRILL_COACH,
            FamilyReadinessRoadmapActionId.SAFE_CALL_SCRIPTS,
            FamilyReadinessRoadmapActionId.PRIVACY_CENTER
        )
        ReadinessLevel.READY -> listOf(
            FamilyReadinessRoadmapActionId.FAMILY_SAFETY_DRILL_COACH,
            FamilyReadinessRoadmapActionId.PRIVACY_CENTER,
            FamilyReadinessRoadmapActionId.RISK_SIGNAL_EXPLAINERS
        )
    }

    private fun buildCards(
        hasAnalysis: Boolean,
        readinessLevel: ReadinessLevel,
        recommendedCardIds: Set<FamilyReadinessRoadmapCardId>,
        situationCardIds: Set<FamilyReadinessRoadmapCardId>
    ): List<FamilyReadinessRoadmapCard> {
        fun action(id: FamilyReadinessRoadmapActionId): FamilyReadinessRoadmapAction =
            actionFor(id, hasAnalysis, readinessLevel)

        fun priority(id: FamilyReadinessRoadmapCardId): FamilyReadinessRoadmapPriority = when {
            id in situationCardIds -> FamilyReadinessRoadmapPriority.IMMEDIATE
            id in recommendedCardIds -> FamilyReadinessRoadmapPriority.NEXT
            else -> FamilyReadinessRoadmapPriority.MAINTENANCE
        }

        fun status(
            id: FamilyReadinessRoadmapCardId,
            default: FamilyReadinessRoadmapStatus
        ): FamilyReadinessRoadmapStatus = if (id in situationCardIds) {
            FamilyReadinessRoadmapStatus.CURRENT_CONTEXT
        } else {
            default
        }

        val planStatus = when (readinessLevel) {
            ReadinessLevel.LOW -> FamilyReadinessRoadmapStatus.START_NOW
            ReadinessLevel.IN_PROGRESS -> FamilyReadinessRoadmapStatus.FOLLOW_UP
            ReadinessLevel.READY -> FamilyReadinessRoadmapStatus.ROUTINE_REVIEW
        }

        return listOf(
            FamilyReadinessRoadmapCard(
                id = FamilyReadinessRoadmapCardId.SAFETY_PLAN_FOUNDATION,
                displayOrder = FamilyReadinessRoadmapCardId.SAFETY_PLAN_FOUNDATION.displayOrder,
                stage = FamilyReadinessRoadmapStage.FOUNDATION,
                title = "가족 안전 계획 시작",
                readinessStatus = status(
                    FamilyReadinessRoadmapCardId.SAFETY_PLAN_FOUNDATION,
                    planStatus
                ),
                whyItMatters = "급한 연락을 받기 전에 가족 확인 질문, 평소 콜백 경로, 읽을 문장을 함께 정해 두면 새 번호의 압박에서 벗어나 직접 확인하기 쉽습니다.",
                nextSmallSteps = listOf(
                    "가족만 아는 확인 질문을 함께 정합니다.",
                    "평소 번호로 확인할 신뢰 연락처를 사용자가 직접 입력합니다.",
                    "급할 때 그대로 읽을 콜백 문장을 함께 정합니다."
                ),
                avoidNotes = listOf(
                    "실제 확인 질문·답, 연락처 값, 콜백 문장을 로드맵이나 공유문에 적지 마세요.",
                    "상대가 새로 알려준 번호를 확인 없이 신뢰 연락처로 저장하지 마세요."
                ),
                priority = priority(FamilyReadinessRoadmapCardId.SAFETY_PLAN_FOUNDATION),
                primaryAction = action(FamilyReadinessRoadmapActionId.SAFETY_PLAN),
                secondaryActions = listOf(
                    action(FamilyReadinessRoadmapActionId.FAMILY_CALLBACK_CARD)
                )
            ),
            FamilyReadinessRoadmapCard(
                id = FamilyReadinessRoadmapCardId.CALLBACK_ROUTINE_PRACTICE,
                displayOrder = FamilyReadinessRoadmapCardId.CALLBACK_ROUTINE_PRACTICE.displayOrder,
                stage = FamilyReadinessRoadmapStage.VERIFY,
                title = "콜백 루틴 연습",
                readinessStatus = status(
                    FamilyReadinessRoadmapCardId.CALLBACK_ROUTINE_PRACTICE,
                    FamilyReadinessRoadmapStatus.PRACTICE
                ),
                whyItMatters = "익숙한 목소리나 가족 이름만 믿지 않고 현재 연락을 끊은 뒤 평소 경로로 다시 확인하는 순서는 짧게라도 반복해 볼수록 실행하기 쉽습니다.",
                nextSmallSteps = listOf(
                    "가족 콜백 확인 카드로 직접 확인 순서를 읽습니다.",
                    "가상 연습에서 멈춤 문구를 소리 내어 말합니다.",
                    "전화 전에 말할 내용과 말하지 않을 비밀정보를 함께 확인합니다."
                ),
                avoidNotes = listOf(
                    "새 번호나 걸려온 번호로만 본인 확인을 끝내지 마세요.",
                    "연습 중에도 실제 확인 질문의 답이나 비밀정보를 메시지로 보내지 마세요."
                ),
                priority = priority(FamilyReadinessRoadmapCardId.CALLBACK_ROUTINE_PRACTICE),
                primaryAction = action(FamilyReadinessRoadmapActionId.FAMILY_CALLBACK_CARD),
                secondaryActions = listOf(
                    action(FamilyReadinessRoadmapActionId.FAMILY_SAFETY_DRILL_COACH),
                    action(FamilyReadinessRoadmapActionId.SAFE_CALL_SCRIPTS)
                )
            ),
            FamilyReadinessRoadmapCard(
                id = FamilyReadinessRoadmapCardId.RISK_SIGNAL_LEARNING,
                displayOrder = FamilyReadinessRoadmapCardId.RISK_SIGNAL_LEARNING.displayOrder,
                stage = FamilyReadinessRoadmapStage.LEARN,
                title = "위험 신호 이해",
                readinessStatus = status(
                    FamilyReadinessRoadmapCardId.RISK_SIGNAL_LEARNING,
                    if (hasAnalysis) {
                        FamilyReadinessRoadmapStatus.PRACTICE
                    } else {
                        FamilyReadinessRoadmapStatus.PREPARE_AHEAD
                    }
                ),
                whyItMatters = "송금 재촉, 비밀정보 요청, 앱 설치, 링크, 가족 사칭 같은 규칙의 이유를 알면 다음 의심 연락을 직접 멈추고 확인할 기준을 세우기 쉽습니다.",
                nextSmallSteps = listOf(
                    "위험 신호 설명 카드에서 대표 압박 방식을 살펴봅니다.",
                    "의심 내용이 있으면 사용자가 직접 붙여넣어 로컬 분석합니다.",
                    "실제 원문 없이 내장된 가상 샘플로 확인 순서를 연습합니다."
                ),
                avoidNotes = listOf(
                    "분석 결과가 없거나 일치 신호가 적다는 이유로 괜찮다고 단정하지 마세요.",
                    "앱이 문자·메시지·링크를 자동으로 찾아 검사한다고 기대하지 마세요."
                ),
                priority = priority(FamilyReadinessRoadmapCardId.RISK_SIGNAL_LEARNING),
                primaryAction = if (hasAnalysis) {
                    action(FamilyReadinessRoadmapActionId.RISK_SIGNAL_EXPLAINERS)
                } else {
                    action(FamilyReadinessRoadmapActionId.CHECKER)
                },
                secondaryActions = buildList {
                    add(action(FamilyReadinessRoadmapActionId.SAMPLE_LIBRARY))
                    if (hasAnalysis) {
                        add(action(FamilyReadinessRoadmapActionId.ANALYSIS_RESULT))
                    } else {
                        add(action(FamilyReadinessRoadmapActionId.RISK_SIGNAL_EXPLAINERS))
                    }
                }
            ),
            FamilyReadinessRoadmapCard(
                id = FamilyReadinessRoadmapCardId.QUICK_STOP_PREPARATION,
                displayOrder = FamilyReadinessRoadmapCardId.QUICK_STOP_PREPARATION.displayOrder,
                stage = FamilyReadinessRoadmapStage.STOP,
                title = "긴급 멈춤 준비",
                readinessStatus = status(
                    FamilyReadinessRoadmapCardId.QUICK_STOP_PREPARATION,
                    FamilyReadinessRoadmapStatus.PREPARE_AHEAD
                ),
                whyItMatters = "압박이 강할 때는 설명보다 대화를 멈추고 돈·비밀정보·앱 행동을 보류한 뒤 평소 경로로 확인하는 짧은 순서가 먼저입니다.",
                nextSmallSteps = listOf(
                    "긴급 멈춤 모드의 세 문장을 천천히 읽습니다.",
                    "상황별 안전 확인 전화 문장을 미리 살펴봅니다.",
                    "판단하기 어렵다면 안전 흐름 길잡이에서 기존 대응 화면을 고릅니다."
                ),
                avoidNotes = listOf(
                    "상대에게 이해시키려다 통화나 화면공유를 계속 유지하지 마세요.",
                    "확인이 끝나기 전에 돈·인증정보·앱 설치 행동을 다시 시작하지 마세요."
                ),
                priority = priority(FamilyReadinessRoadmapCardId.QUICK_STOP_PREPARATION),
                primaryAction = action(FamilyReadinessRoadmapActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(FamilyReadinessRoadmapActionId.SAFE_CALL_SCRIPTS),
                    action(FamilyReadinessRoadmapActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            FamilyReadinessRoadmapCard(
                id = FamilyReadinessRoadmapCardId.INCIDENT_RESPONSE_PREPARATION,
                displayOrder = FamilyReadinessRoadmapCardId.INCIDENT_RESPONSE_PREPARATION.displayOrder,
                stage = FamilyReadinessRoadmapStage.RESPOND,
                title = "피해 의심 시 대응 준비",
                readinessStatus = status(
                    FamilyReadinessRoadmapCardId.INCIDENT_RESPONSE_PREPARATION,
                    FamilyReadinessRoadmapStatus.PREPARE_AHEAD
                ),
                whyItMatters = "돈·정보·앱 행동을 이미 했을 가능성이 있으면 추가 행동을 멈추고 시간대별 수습, 공식 도움, 실제 상황별 대응 순서를 직접 고를 수 있어야 합니다.",
                nextSmallSteps = listOf(
                    "24시간 수습 계획에서 지금 할 일과 다음 점검을 구분합니다.",
                    "앱에 내장된 공식 도움 경로와 상담 준비 항목을 확인합니다.",
                    "실제로 한 행동과 가까운 긴급 대응 항목만 직접 고릅니다."
                ),
                avoidNotes = listOf(
                    "분석 신호만 보고 실제 송금·정보 제공·앱 설치가 있었다고 단정하지 마세요.",
                    "이 앱이 지급정지나 피해금 회수를 대신한다고 기대하지 마세요."
                ),
                priority = priority(FamilyReadinessRoadmapCardId.INCIDENT_RESPONSE_PREPARATION),
                primaryAction = action(FamilyReadinessRoadmapActionId.RECOVERY_PLAN),
                secondaryActions = listOf(
                    action(FamilyReadinessRoadmapActionId.OFFICIAL_HELP_DIRECTORY),
                    action(FamilyReadinessRoadmapActionId.RESPONSE_CENTER),
                    action(FamilyReadinessRoadmapActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            FamilyReadinessRoadmapCard(
                id = FamilyReadinessRoadmapCardId.EVIDENCE_ACCOUNT_PROTECTION,
                displayOrder = FamilyReadinessRoadmapCardId.EVIDENCE_ACCOUNT_PROTECTION.displayOrder,
                stage = FamilyReadinessRoadmapStage.PROTECT,
                title = "증거와 계정 보호 준비",
                readinessStatus = status(
                    FamilyReadinessRoadmapCardId.EVIDENCE_ACCOUNT_PROTECTION,
                    FamilyReadinessRoadmapStatus.PREPARE_AHEAD
                ),
                whyItMatters = "링크·비밀정보·앱·원격제어 노출이 의심되면 다른 안전한 기기에서 계정을 보호하고 필요한 화면과 시각을 수동으로 보존하는 순서를 알아두는 편이 좋습니다.",
                nextSmallSteps = listOf(
                    "증거 보존 체크리스트에서 지우기 전에 남길 항목을 확인합니다.",
                    "계정·기기 잠금 체크리스트에서 노출 가능성에 맞는 수동 순서를 봅니다.",
                    "필요하면 공식 도움 화면에서 다음 절차를 직접 확인합니다."
                ),
                avoidNotes = listOf(
                    "의심 링크나 앱을 증거 확인 목적으로 다시 열지 마세요.",
                    "로드맵이 증거를 자동 수집하거나 악성 앱을 제거한다고 기대하지 마세요."
                ),
                priority = priority(FamilyReadinessRoadmapCardId.EVIDENCE_ACCOUNT_PROTECTION),
                primaryAction = action(FamilyReadinessRoadmapActionId.EVIDENCE_KIT),
                secondaryActions = listOf(
                    action(FamilyReadinessRoadmapActionId.ACCOUNT_LOCKDOWN),
                    action(FamilyReadinessRoadmapActionId.OFFICIAL_HELP_DIRECTORY)
                )
            ),
            FamilyReadinessRoadmapCard(
                id = FamilyReadinessRoadmapCardId.FAMILY_HELPER_NETWORK,
                displayOrder = FamilyReadinessRoadmapCardId.FAMILY_HELPER_NETWORK.displayOrder,
                stage = FamilyReadinessRoadmapStage.CONNECT,
                title = "가족 경고/도우미 네트워크",
                readinessStatus = status(
                    FamilyReadinessRoadmapCardId.FAMILY_HELPER_NETWORK,
                    FamilyReadinessRoadmapStatus.PREPARE_AHEAD
                ),
                whyItMatters = "혼자 판단하지 않도록 가족에게 일반 경고를 알리고, 비밀정보를 받지 않은 채 옆에서 절차를 함께 볼 믿을 만한 도우미를 정해 두면 좋습니다.",
                nextSmallSteps = listOf(
                    "원문과 비밀 값을 뺀 가족 경고 문구를 살펴봅니다.",
                    "상황을 함께 확인할 믿을 만한 도우미 역할을 정합니다.",
                    "가족 사칭이 의심되면 평소 콜백 경로로 다시 확인합니다."
                ),
                avoidNotes = listOf(
                    "도우미에게 비밀번호·인증정보·안전 질문의 답을 맡기지 마세요.",
                    "앱이 연락처를 읽거나 가족에게 경고를 자동 전송한다고 기대하지 마세요."
                ),
                priority = priority(FamilyReadinessRoadmapCardId.FAMILY_HELPER_NETWORK),
                primaryAction = action(FamilyReadinessRoadmapActionId.FAMILY_ALERT_MESSAGES),
                secondaryActions = listOf(
                    action(FamilyReadinessRoadmapActionId.TRUSTED_HELPER_HANDOFF),
                    action(FamilyReadinessRoadmapActionId.FAMILY_CALLBACK_CARD)
                )
            ),
            FamilyReadinessRoadmapCard(
                id = FamilyReadinessRoadmapCardId.PRIVACY_LOCAL_DATA_REVIEW,
                displayOrder = FamilyReadinessRoadmapCardId.PRIVACY_LOCAL_DATA_REVIEW.displayOrder,
                stage = FamilyReadinessRoadmapStage.REVIEW,
                title = "개인정보와 로컬 데이터 점검",
                readinessStatus = status(
                    FamilyReadinessRoadmapCardId.PRIVACY_LOCAL_DATA_REVIEW,
                    FamilyReadinessRoadmapStatus.ROUTINE_REVIEW
                ),
                whyItMatters = "앱에 저장되는 항목과 사용하지 않는 권한, 항목별 삭제 방법을 정기적으로 확인하면 가족이 로컬 데이터 범위를 함께 이해할 수 있습니다.",
                nextSmallSteps = listOf(
                    "개인정보 센터에서 값이 아닌 저장 유무와 개수만 확인합니다.",
                    "사용하지 않는 문자·통화·연락처·마이크·인터넷 권한을 확인합니다.",
                    "정리가 필요하면 개인정보 센터의 항목별 삭제 버튼을 사용자가 직접 고릅니다."
                ),
                avoidNotes = listOf(
                    "로드맵을 보는 것만으로 데이터가 지워진다고 생각하지 마세요.",
                    "안전 계획의 실제 값이나 사건 단서를 가족 공유 요약에 옮기지 마세요."
                ),
                priority = priority(FamilyReadinessRoadmapCardId.PRIVACY_LOCAL_DATA_REVIEW),
                primaryAction = action(FamilyReadinessRoadmapActionId.PRIVACY_CENTER),
                secondaryActions = listOf(
                    action(FamilyReadinessRoadmapActionId.SAFETY_PLAN),
                    action(FamilyReadinessRoadmapActionId.RISK_SIGNAL_EXPLAINERS)
                )
            )
        )
    }

    private fun actionFor(
        id: FamilyReadinessRoadmapActionId,
        hasAnalysis: Boolean,
        readinessLevel: ReadinessLevel
    ): FamilyReadinessRoadmapAction = FamilyReadinessRoadmapAction(
        id = id,
        label = when (id) {
            FamilyReadinessRoadmapActionId.QUICK_STOP -> "긴급 멈춤 모드"
            FamilyReadinessRoadmapActionId.CHECKER -> if (hasAnalysis) {
                "다른 의심 내용 직접 분석하기"
            } else {
                "의심 내용 직접 분석하기"
            }
            FamilyReadinessRoadmapActionId.ANALYSIS_RESULT -> "현재 분석 결과 보기"
            FamilyReadinessRoadmapActionId.RISK_SIGNAL_EXPLAINERS -> "위험 신호 설명 카드"
            FamilyReadinessRoadmapActionId.SAMPLE_LIBRARY -> "가상 샘플로 분석 연습"
            FamilyReadinessRoadmapActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 확인 카드"
            FamilyReadinessRoadmapActionId.SAFETY_PLAN -> when (readinessLevel) {
                ReadinessLevel.LOW -> "가족 안전 계획 만들기"
                ReadinessLevel.IN_PROGRESS -> "가족 안전 계획 보완하기"
                ReadinessLevel.READY -> "가족 안전 계획 점검하기"
            }
            FamilyReadinessRoadmapActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
            FamilyReadinessRoadmapActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
            FamilyReadinessRoadmapActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            FamilyReadinessRoadmapActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처 보기"
            FamilyReadinessRoadmapActionId.RESPONSE_CENTER -> "긴급 대응 센터"
            FamilyReadinessRoadmapActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
            FamilyReadinessRoadmapActionId.TRUSTED_HELPER_HANDOFF -> "보호자 도움 요청 카드"
            FamilyReadinessRoadmapActionId.SAFE_CALL_SCRIPTS -> "안전 확인 전화 스크립트"
            FamilyReadinessRoadmapActionId.RECOVERY_PLAN -> "24시간 수습 계획"
            FamilyReadinessRoadmapActionId.FAMILY_SAFETY_DRILL_COACH -> "가족 안전 연습 코치"
            FamilyReadinessRoadmapActionId.PRIVACY_CENTER -> "개인정보 센터"
        }
    )

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readinessLevel: ReadinessLevel,
        signals: Set<ScamSignal>,
        needsImmediateResponse: Boolean
    ): String {
        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW -> "가족 준비가 비어 있어도 괜찮습니다. 안전 계획, 콜백 연습, 멈춤 순서부터 차분히 시작하세요."
            ReadinessLevel.IN_PROGRESS -> "가족 준비가 진행 중입니다. 빠진 준비를 보완하고 콜백 연습, 확인 전화, 개인정보 점검을 이어가세요."
            ReadinessLevel.READY -> "가족 확인 기반이 준비되어 있습니다. 가상 연습, 주간 개인정보 점검, 위험 신호 학습으로 유지하세요."
        }
        if (analysis == null) {
            return "현재 분석 결과가 없어 괜찮다고 단정하지 않습니다. 의심 내용 직접 검사, 가상 샘플, 위험 신호 학습도 함께 확인하세요. $readinessCopy"
        }
        if (needsImmediateResponse) {
            return "현재 위험도 또는 즉시 멈춰 직접 확인할 신호가 있어 긴급 멈춤과 피해 의심 대응 준비를 예방 카드보다 먼저 보여드립니다. 실제로 한 행동만 기존 화면에서 직접 확인하세요. $readinessCopy"
        }
        if (signals.isEmpty()) {
            return "뚜렷하게 일치한 신호가 없어도 괜찮다고 단정하지 않습니다. 현재 결과와 위험 신호 설명을 다시 보고 다른 의심 내용은 직접 검사하세요. $readinessCopy"
        }

        val situationCopy = buildList {
            if (signals.any { signal -> signal in familySignals }) {
                add("가족·목소리·번호 변경 신호는 콜백 연습과 가족 경고·도우미 준비를 먼저 권합니다.")
            }
            if (signals.any { signal -> signal in financialSignals }) {
                add("돈·비밀정보·금융 미끼 신호는 확인 전화, 계정 보호, 공식 도움, 수습 계획을 권합니다.")
            }
            if (signals.any { signal -> signal in remoteSignals }) {
                add("앱·화면공유 신호는 계정 보호, 증거 보존, 믿을 만한 도우미 준비를 권합니다.")
            }
            if (signals.any { signal -> signal in linkSignals }) {
                add("링크·스미싱 신호는 위험 신호 학습, 증거 보존, 가족 경고 준비를 권합니다.")
            }
            if (ScamSignal.SECRECY in signals) {
                add("비밀 유지 압박은 대화를 멈추고 믿을 만한 가족과 확인할 준비를 권합니다.")
            }
            if (isEmpty()) {
                add("현재 일치 신호와 가까운 기존 안전 화면을 직접 골라 확인하세요.")
            }
        }.joinToString(" ")
        return "$situationCopy $readinessCopy"
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 로드맵은 사용자가 직접 여는 수동 안내이며, 현재 분석의 위험도·일치 신호와 값 없는 가족 준비 수준·개수만 기기 안의 로컬 규칙으로 사용합니다.",
        "로드맵 빌더에는 안전 질문·콜백 문장·연락처의 실제 값을 전달하지 않으며 카드와 공유문에 표시하지 않습니다.",
        "문자·메시지·통화·연락처를 자동으로 읽거나 감시하지 않고 가족을 자동 모니터링하지 않습니다.",
        "로드맵 선택, 완료 상태, 체크 상태, 점수, 읽음·진행 상태, 공유 이력, 로드맵 이력을 저장하지 않습니다.",
        "화면을 열거나 버튼을 누르는 것만으로 사건 기록을 추가·수정·삭제하지 않습니다.",
        "공유 버튼은 연락처를 읽거나 받는 사람을 고르지 않습니다. 사용자가 공유 앱과 대상을 직접 선택하며 앱이 자동으로 보내지 않습니다.",
        "네트워크를 사용하지 않고 새 Android 권한을 요청하지 않으며 전화나 메시지를 자동 실행하지 않습니다.",
        "이 로드맵은 통화·문자 차단, 악성 앱 제거, 계정·카드 정지, 피해금 회수, 법률 자문 또는 안전 결과를 보장하지 않습니다. 필요한 조치는 공식 경로에서 직접 진행하세요."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<FamilyReadinessRoadmapCardId>,
        cards: List<FamilyReadinessRoadmapCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 가족 준비 로드맵")
            appendLine()
            appendLine("먼저 준비할 단계")
            recommendedCardIds.forEachIndexed { index, cardId ->
                val card = cardsById.getValue(cardId)
                appendLine("${index + 1}. [${card.stage.label}] ${card.title}")
                appendLine("   다음 작은 단계: ${card.nextSmallSteps.first()}")
                appendLine("   앱 행동: ${actionLabels(card)}")
            }
            appendLine()
            appendLine("전체 가족 준비 지도")
            cards.forEach { card ->
                appendLine("- [${card.stage.label}] ${card.title}")
                appendLine("  다음 작은 단계: ${card.nextSmallSteps.first()}")
                appendLine("  앱 행동: ${actionLabels(card)}")
            }
            appendLine()
            appendLine("각 단계는 가족이 직접 선택하는 일반 준비 안내이며 선택·완료·공유 이력을 저장하지 않습니다.")
            append(
                "붙여넣은 원문·개인정보 가림 단서·안전 질문·콜백 내용·연락처 값과 전화·계좌·비밀번호·" +
                    "인증번호·PIN·링크·도메인·신분정보 값은 이 공유문에 포함하지 않았습니다."
            )
        }.trim()
    }

    private fun actionLabels(card: FamilyReadinessRoadmapCard): String =
        (listOf(card.primaryAction) + card.secondaryActions)
            .joinToString(" / ") { action -> action.label }
}
