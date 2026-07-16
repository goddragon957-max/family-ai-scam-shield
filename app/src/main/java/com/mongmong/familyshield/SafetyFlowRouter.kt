package com.mongmong.familyshield

enum class SafetyFlowRouteId {
    SUSPICIOUS_REQUEST,
    FAMILY_OR_NUMBER_CHANGE,
    MONEY_OR_INFORMATION_SENT,
    REMOTE_ACCESS_GRANTED,
    OFFICIAL_HELP,
    EVIDENCE_PRESERVATION,
    PRIVACY
}

enum class SafetyFlowActionId {
    CHECKER,
    ANALYSIS_RESULT,
    FAMILY_CALLBACK_CARD,
    SAFETY_PLAN,
    RESPONSE_CENTER,
    REPORT_PREP,
    OFFICIAL_HELP_DIRECTORY,
    EVIDENCE_KIT,
    PRIVACY_CENTER
}

enum class SafetyFlowUrgency(val rank: Int, val label: String) {
    IMMEDIATE(0, "지금 먼저"),
    VERIFY(1, "직접 확인"),
    PRESERVE(2, "보존·상담"),
    INFORMATION(3, "정보 확인")
}

data class SafetyFlowAction(
    val id: SafetyFlowActionId,
    val label: String,
    val evidenceSituation: IncidentSituation? = null
)

data class SafetyFlowRouteCard(
    val id: SafetyFlowRouteId,
    val displayOrder: Int,
    val urgency: SafetyFlowUrgency,
    val label: String,
    val explanation: String,
    val primaryAction: SafetyFlowAction,
    val secondaryActions: List<SafetyFlowAction> = emptyList(),
    val safetyNotes: List<String>
) {
    val primaryActionLabel: String
        get() = primaryAction.label

    val secondaryActionLabels: List<String>
        get() = secondaryActions.map { action -> action.label }

    val urgencyOrder: Int
        get() = urgency.rank
}

data class SafetyFlowRouter(
    val title: String,
    val intro: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedRouteIds: List<SafetyFlowRouteId>,
    val routes: List<SafetyFlowRouteCard>,
    val readiness: FamilyReadiness,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedRoutes: List<SafetyFlowRouteCard>
        get() = recommendedRouteIds.mapNotNull { routeId ->
            routes.find { route -> route.id == routeId }
        }

    val recommendedActionIds: List<SafetyFlowActionId>
        get() = recommendedRoutes
            .flatMap { route -> listOf(route.primaryAction) + route.secondaryActions }
            .map { action -> action.id }
            .distinct()
}

object SafetyFlowRouterBuilder {
    private val familySignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.PHONE_NUMBER_CHANGE
    )

    private val urgentSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.CREDENTIALS,
        ScamSignal.APP_INSTALL,
        ScamSignal.LIVE_CALL_CONTROL,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE
    )

    private val remoteSignals = setOf(
        ScamSignal.APP_INSTALL,
        ScamSignal.LIVE_CALL_CONTROL
    )

    private val linkSignals = setOf(
        ScamSignal.LINK,
        ScamSignal.PARCEL_INVITATION_SMISHING
    )

    fun build(analysis: ScamAnalysis?, readiness: FamilyReadiness): SafetyFlowRouter {
        val routes = buildRoutes(
            hasAnalysis = analysis != null,
            readiness = readiness
        )
        val recommendedRouteIds = recommendRouteIds(analysis)
        val recommendationTitle = if (analysis == null) {
            "분석 전 기본 안내"
        } else {
            "현재 분석 기준 추천"
        }
        val recommendationExplanation = buildRecommendationExplanation(analysis, readiness)
        val privacySafetyNotes = listOf(
            "문자·통화·연락처를 자동으로 읽거나 감시하지 않고, 사용자가 직접 붙여넣거나 텍스트로 공유한 내용만 기기 안에서 분석합니다.",
            "분석 신호는 실제 피해 행동을 뜻하지 않습니다. 본인에게 해당하는 경로만 직접 고르세요.",
            "안전 흐름 선택과 추천은 저장하지 않으며 사건 기록을 추가하거나 수정하지 않습니다.",
            "분석 내용이나 경로 선택을 앱 자체 서버로 보내지 않으며 네트워크 분석이나 새 권한을 사용하지 않습니다.",
            "이 안내는 상황에 맞는 기존 기능을 찾기 위한 참고입니다. 실제 조치와 결과는 공식 기관과 금융회사에 확인하세요."
        )

        return SafetyFlowRouter(
            title = "지금 뭐부터 해야 할까요?",
            intro = "현재 상황과 가장 가까운 항목을 직접 고르세요. 이 화면은 기존 로컬 안전 기능으로 안내하며 선택을 저장하지 않습니다.",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = recommendationExplanation,
            recommendedRouteIds = recommendedRouteIds,
            routes = routes,
            readiness = readiness,
            privacySafetyNotes = privacySafetyNotes,
            familyShareSummary = buildFamilyShareSummary(
                recommendationTitle = recommendationTitle,
                recommendedRouteIds = recommendedRouteIds,
                routes = routes
            )
        )
    }

    private fun buildRoutes(
        hasAnalysis: Boolean,
        readiness: FamilyReadiness
    ): List<SafetyFlowRouteCard> = listOf(
        SafetyFlowRouteCard(
            id = SafetyFlowRouteId.SUSPICIOUS_REQUEST,
            displayOrder = 1,
            urgency = SafetyFlowUrgency.VERIFY,
            label = "수상한 문자·메신저·통화를 받았어요",
            explanation = "받은 내용을 직접 붙여넣어 기기 안에서 위험 신호와 멈춤 행동을 확인합니다.",
            primaryAction = SafetyFlowAction(
                id = SafetyFlowActionId.CHECKER,
                label = "의심 내용 검사"
            ),
            secondaryActions = if (hasAnalysis) {
                listOf(
                    SafetyFlowAction(
                        id = SafetyFlowActionId.ANALYSIS_RESULT,
                        label = "현재 분석 결과 보기"
                    )
                )
            } else {
                emptyList()
            },
            safetyNotes = listOf(
                "의심 내용을 앱이 자동으로 찾는 기능이 아니라 사용자가 직접 붙여넣어 확인하는 흐름입니다.",
                "민감한 값은 지우거나 가린 뒤 검사해도 됩니다."
            )
        ),
        SafetyFlowRouteCard(
            id = SafetyFlowRouteId.FAMILY_OR_NUMBER_CHANGE,
            displayOrder = 2,
            urgency = SafetyFlowUrgency.VERIFY,
            label = "가족 요청·번호 변경 같아요",
            explanation =
                "평소 쓰던 가족 번호와 안전 질문으로 직접 확인합니다. " +
                    "현재 가족 준비도는 ${readiness.level.label} (${readiness.completedCount}/${readiness.totalCount})입니다.",
            primaryAction = SafetyFlowAction(
                id = SafetyFlowActionId.FAMILY_CALLBACK_CARD,
                label = "가족 콜백 확인"
            ),
            secondaryActions = listOf(
                SafetyFlowAction(
                    id = SafetyFlowActionId.SAFETY_PLAN,
                    label = safetyPlanActionLabel(readiness.level)
                )
            ),
            safetyNotes = listOf(
                "새로 받은 번호나 걸려온 번호만 믿지 말고 평소 알고 있던 번호로 직접 확인하세요.",
                "가족 안전 질문과 신뢰 연락처의 실제 값은 이 라우터 공유문에 넣지 않습니다."
            )
        ),
        SafetyFlowRouteCard(
            id = SafetyFlowRouteId.MONEY_OR_INFORMATION_SENT,
            displayOrder = 3,
            urgency = SafetyFlowUrgency.IMMEDIATE,
            label = "이미 돈·상품권·코인/정보를 보냈어요",
            explanation =
                "돈·상품권·코인 또는 인증정보를 실제로 보냈다면 긴급 대응 순서를 고르고, " +
                    "현재 분석이 있으면 상담 준비 카드로 이어갑니다.",
            primaryAction = SafetyFlowAction(
                id = SafetyFlowActionId.RESPONSE_CENTER,
                label = "긴급 대응 열기"
            ),
            secondaryActions = listOf(
                SafetyFlowAction(
                    id = SafetyFlowActionId.REPORT_PREP,
                    label = "상담 준비 카드"
                ),
                SafetyFlowAction(
                    id = SafetyFlowActionId.EVIDENCE_KIT,
                    label = "증거 보존 보기"
                )
            ),
            safetyNotes = listOf(
                "추가 송금·정보 제공을 멈추고 실제로 한 행동만 선택하세요.",
                "지급정지와 계정 보호가 급하면 자료 정리보다 긴급 조치를 먼저 진행하세요."
            )
        ),
        SafetyFlowRouteCard(
            id = SafetyFlowRouteId.REMOTE_ACCESS_GRANTED,
            displayOrder = 4,
            urgency = SafetyFlowUrgency.IMMEDIATE,
            label = "앱 설치·화면공유·원격제어를 했어요",
            explanation = "원격 연결을 먼저 끊고 안전한 기기에서 계정을 보호한 뒤 앱·권한 정보를 보존합니다.",
            primaryAction = SafetyFlowAction(
                id = SafetyFlowActionId.RESPONSE_CENTER,
                label = "원격 대응 열기"
            ),
            secondaryActions = listOf(
                SafetyFlowAction(
                    id = SafetyFlowActionId.EVIDENCE_KIT,
                    label = "원격 증거 보존",
                    evidenceSituation = IncidentSituation.APP_INSTALLED
                )
            ),
            safetyNotes = listOf(
                "원격 연결이 이어지면 화면 보존보다 연결 차단을 먼저 하세요.",
                "비밀번호 변경과 계정 보호는 의심 기기가 아닌 다른 안전한 기기에서 진행하세요."
            )
        ),
        SafetyFlowRouteCard(
            id = SafetyFlowRouteId.OFFICIAL_HELP,
            displayOrder = 5,
            urgency = SafetyFlowUrgency.PRESERVE,
            label = "신고·상담 번호가 필요해요",
            explanation = "상황에 맞는 공식 신고·상담 창구와 전화 전에 말할 내용을 확인합니다.",
            primaryAction = SafetyFlowAction(
                id = SafetyFlowActionId.OFFICIAL_HELP_DIRECTORY,
                label = "공식 연락처 보기"
            ),
            secondaryActions = if (hasAnalysis) {
                listOf(
                    SafetyFlowAction(
                        id = SafetyFlowActionId.REPORT_PREP,
                        label = "상담 준비 카드"
                    )
                )
            } else {
                emptyList()
            },
            safetyNotes = listOf(
                "전화 버튼은 전화 앱만 열며 사용자가 번호를 확인한 뒤 직접 통화를 시작합니다.",
                "문자·메신저 안의 번호 대신 공식 앱·카드 뒷면 등 평소 확인 가능한 공식 경로를 사용하세요."
            )
        ),
        SafetyFlowRouteCard(
            id = SafetyFlowRouteId.EVIDENCE_PRESERVATION,
            displayOrder = 6,
            urgency = SafetyFlowUrgency.PRESERVE,
            label = "증거를 지워도 될지 모르겠어요",
            explanation = "메시지·통화 시각·거래·앱 정보를 지우기 전에 수동 보존 순서를 확인합니다.",
            primaryAction = SafetyFlowAction(
                id = SafetyFlowActionId.EVIDENCE_KIT,
                label = "증거 보존 확인"
            ),
            secondaryActions = listOf(
                SafetyFlowAction(
                    id = SafetyFlowActionId.OFFICIAL_HELP_DIRECTORY,
                    label = "신고 연락처 보기"
                )
            ),
            safetyNotes = listOf(
                "필요한 화면을 남기기 전에 메시지·앱·기록을 급히 삭제하거나 기기를 초기화하지 마세요.",
                "자금·계정 보호가 급하면 지급정지나 원격 연결 차단을 먼저 하세요."
            )
        ),
        SafetyFlowRouteCard(
            id = SafetyFlowRouteId.PRIVACY,
            displayOrder = 7,
            urgency = SafetyFlowUrgency.INFORMATION,
            label = "내 데이터·권한이 걱정돼요",
            explanation = "이 기기에 저장된 앱 데이터와 사용하지 않는 권한, 항목별 삭제 방법을 확인합니다.",
            primaryAction = SafetyFlowAction(
                id = SafetyFlowActionId.PRIVACY_CENTER,
                label = "개인정보 센터 열기"
            ),
            safetyNotes = listOf(
                "개인정보 센터에 들어가는 것만으로 데이터가 삭제되지는 않습니다.",
                "라우터 경로 선택은 저장하지 않으며 개인정보 센터의 삭제 버튼도 사용자가 직접 눌러야 동작합니다."
            )
        )
    )

    private fun safetyPlanActionLabel(level: ReadinessLevel): String = when (level) {
        ReadinessLevel.LOW -> "안전 계획 만들기"
        ReadinessLevel.IN_PROGRESS -> "안전 계획 보완하기"
        ReadinessLevel.READY -> "안전 계획 확인하기"
    }

    private fun recommendRouteIds(analysis: ScamAnalysis?): List<SafetyFlowRouteId> {
        if (analysis == null) {
            return listOf(
                SafetyFlowRouteId.SUSPICIOUS_REQUEST,
                SafetyFlowRouteId.FAMILY_OR_NUMBER_CHANGE
            )
        }

        val signals = analysis.matchedSignals.toSet()
        val hasFamilySignal = signals.any { it in familySignals }
        val hasRemoteSignal = signals.any { it in remoteSignals }
        val hasLinkSignal = signals.any { it in linkSignals }
        val needsUrgentRoutes = analysis.riskLevel == RiskLevel.HIGH || signals.any { it in urgentSignals }
        val recommended = linkedSetOf<SafetyFlowRouteId>()

        if (hasFamilySignal) {
            recommended += SafetyFlowRouteId.FAMILY_OR_NUMBER_CHANGE
        }
        if (needsUrgentRoutes) {
            recommended += SafetyFlowRouteId.MONEY_OR_INFORMATION_SENT
            if (hasRemoteSignal) {
                recommended += SafetyFlowRouteId.REMOTE_ACCESS_GRANTED
            }
            recommended += SafetyFlowRouteId.EVIDENCE_PRESERVATION
            recommended += SafetyFlowRouteId.OFFICIAL_HELP
        }
        if (hasLinkSignal) {
            recommended += SafetyFlowRouteId.SUSPICIOUS_REQUEST
            recommended += SafetyFlowRouteId.EVIDENCE_PRESERVATION
            recommended += SafetyFlowRouteId.OFFICIAL_HELP
        }
        if (hasFamilySignal && !needsUrgentRoutes && !hasLinkSignal) {
            recommended += SafetyFlowRouteId.SUSPICIOUS_REQUEST
        }
        if (recommended.isEmpty()) {
            recommended += SafetyFlowRouteId.SUSPICIOUS_REQUEST
            recommended += SafetyFlowRouteId.FAMILY_OR_NUMBER_CHANGE
        }

        return recommended.toList()
    }

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness
    ): String {
        if (analysis == null) {
            return if (readiness.level == ReadinessLevel.READY) {
                "먼저 수상한 내용을 직접 붙여넣어 확인하고, 준비된 가족 콜백 루틴도 함께 점검하세요."
            } else {
                "먼저 수상한 내용을 직접 붙여넣어 확인하고, 가족 콜백 카드에서 부족한 안전 계획을 준비하세요."
            }
        }

        val signals = analysis.matchedSignals.toSet()
        val hasFamilySignal = signals.any { it in familySignals }
        val needsUrgentRoutes = analysis.riskLevel == RiskLevel.HIGH || signals.any { it in urgentSignals }
        val hasLinkSignal = signals.any { it in linkSignals }

        return when {
            hasFamilySignal && needsUrgentRoutes ->
                "가족 사칭 가능성을 먼저 기존 번호로 확인하세요. 이미 돈·정보를 보냈거나 원격 접근을 허용했다면 긴급 대응과 증거·공식 도움 흐름을 이어서 확인하세요."
            hasFamilySignal ->
                "가족·번호 변경 신호가 있어 기존에 알고 있던 번호로 콜백 확인을 먼저 권합니다."
            needsUrgentRoutes ->
                "강한 위험 신호가 있습니다. 이미 실행한 행동이 있다면 긴급 대응, 상담 준비, 증거 보존, 공식 도움 흐름을 순서대로 확인하세요."
            hasLinkSignal ->
                "링크·스미싱 신호가 있어 현재 분석 행동을 확인하고, 링크를 다시 열지 않은 채 증거 보존과 공식 상담 흐름을 확인하세요."
            else ->
                "현재 분석의 행동 안내를 다시 확인하고, 송금·인증·앱 설치 요청이 이어지면 가족 콜백으로 검증하세요."
        }
    }

    private fun buildFamilyShareSummary(
        recommendationTitle: String,
        recommendedRouteIds: List<SafetyFlowRouteId>,
        routes: List<SafetyFlowRouteCard>
    ): String {
        val routesById = routes.associateBy { route -> route.id }

        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 지금 할 일 안내")
            appendLine()
            appendLine(recommendationTitle)
            recommendedRouteIds.forEachIndexed { index, routeId ->
                val route = routesById.getValue(routeId)
                appendLine("${index + 1}. ${route.label}: ${actionLabels(route)}")
            }
            appendLine()
            appendLine("선택 가능한 경로")
            routes.forEach { route ->
                appendLine("- ${route.label}: ${actionLabels(route)}")
            }
            appendLine()
            appendLine("경로 선택은 저장하지 않으며 사건 기록을 자동으로 추가하거나 수정하지 않습니다.")
            appendLine("각 버튼은 앱에 이미 있는 로컬 분석·가족 콜백·긴급 대응·증거 보존·공식 도움·개인정보 화면으로 이동합니다.")
            append(
                "붙여넣은 원문과 개인정보 가림 단서, 가족 안전단어·콜백 스크립트, 연락처 이름·번호, " +
                    "계좌·비밀번호·인증번호(OTP)·PIN·링크 값은 이 공유문에 포함하지 않았습니다."
            )
        }.trim()
    }

    private fun actionLabels(route: SafetyFlowRouteCard): String =
        (listOf(route.primaryAction) + route.secondaryActions)
            .joinToString(" / ") { action -> action.label }
}
