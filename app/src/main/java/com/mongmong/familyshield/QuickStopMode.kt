package com.mongmong.familyshield

enum class QuickStopCardId {
    STOP_CONVERSATION,
    FAMILY_CALLBACK,
    RISK_ANALYSIS,
    EXPOSURE_RESPONSE,
    OFFICIAL_HELP,
    EVIDENCE_PRESERVATION,
    FAMILY_ALERT
}

enum class QuickStopActionId {
    STOP_SCRIPT,
    FAMILY_CALLBACK_CARD,
    SAFETY_PLAN,
    CHECKER,
    ANALYSIS_RESULT,
    SAFETY_FLOW_ROUTER,
    RESPONSE_CENTER,
    ACCOUNT_LOCKDOWN,
    OFFICIAL_HELP_DIRECTORY,
    EVIDENCE_KIT,
    FAMILY_ALERT_MESSAGES
}

enum class QuickStopUrgency(val rank: Int, val label: String) {
    STOP_NOW(0, "지금 먼저"),
    VERIFY(1, "직접 확인"),
    RESPOND(2, "이미 했다면"),
    PRESERVE_AND_HELP(3, "보존·도움"),
    WARN_FAMILY(4, "가족 알림")
}

data class QuickStopStep(
    val displayOrder: Int,
    val instruction: String
)

data class QuickStopAction(
    val id: QuickStopActionId,
    val label: String
)

data class QuickStopActionCard(
    val id: QuickStopCardId,
    val displayOrder: Int,
    val urgency: QuickStopUrgency,
    val label: String,
    val explanation: String,
    val primaryAction: QuickStopAction,
    val secondaryActions: List<QuickStopAction> = emptyList(),
    val safetyNotes: List<String>
) {
    val primaryActionLabel: String
        get() = primaryAction.label

    val secondaryActionLabels: List<String>
        get() = secondaryActions.map { action -> action.label }
}

data class QuickStopMode(
    val title: String,
    val intro: String,
    val stopScriptTitle: String,
    val stopScriptSteps: List<QuickStopStep>,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val readinessMessage: String,
    val recommendedCardIds: List<QuickStopCardId>,
    val cards: List<QuickStopActionCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<QuickStopActionCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<QuickStopActionId>
        get() = recommendedCards
            .flatMap { card -> listOf(card.primaryAction) + card.secondaryActions }
            .map { action -> action.id }
            .distinct()
}

object QuickStopModeBuilder {
    private val familySignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.PHONE_NUMBER_CHANGE
    )

    private val urgentExposureSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.BAIT,
        ScamSignal.CREDENTIALS,
        ScamSignal.APP_INSTALL,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.LIVE_CALL_CONTROL,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val linkSignals = setOf(
        ScamSignal.LINK,
        ScamSignal.PARCEL_INVITATION_SMISHING
    )

    fun build(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness
    ): QuickStopMode {
        val stopScriptSteps = listOf(
            QuickStopStep(
                displayOrder = 1,
                instruction = "지금 대화를 멈추고 전화를 끊거나 답장을 보류하세요."
            ),
            QuickStopStep(
                displayOrder = 2,
                instruction = "확인 전에는 돈을 보내거나 개인정보·인증번호를 공유하거나 앱을 설치하지 마세요."
            ),
            QuickStopStep(
                displayOrder = 3,
                instruction = "상대가 알려준 새 번호·링크가 아닌, 평소 저장한 가족 번호나 공식 경로로 직접 확인하세요."
            )
        )
        val cards = buildCards(
            hasAnalysis = analysis != null,
            readiness = readiness
        )
        val recommendedCardIds = recommendCardIds(analysis, readiness)
        val privacySafetyNotes = listOf(
            "이 화면은 사용자가 직접 여는 수동 안내이며 문자·통화·연락처를 자동으로 읽거나 감시하지 않습니다.",
            "현재 분석은 사용자가 직접 붙여넣거나 텍스트로 공유한 내용만 기기 안에서 판단하며 네트워크로 업로드하지 않습니다.",
            "버튼 선택, 공유 이력, 체크 상태를 저장하지 않으며 화면을 여는 것만으로 사건 기록을 추가·수정·삭제하지 않습니다.",
            "공유 버튼은 연락처를 읽거나 받는 사람을 고르지 않고 자동으로 보내지 않습니다. 사용자가 공유 앱과 대상을 직접 선택합니다.",
            "앱은 통화·문자를 차단하지 않고, 악성 앱을 제거하거나 계좌·카드를 정지하지 않으며, 피해금 회수를 보장하지 않습니다. 필요한 조치는 공식 경로에서 직접 진행하세요.",
            "법률·금융 자문이나 안전·법적 결과를 보장하는 기능이 아니며, 이 화면을 위해 새 Android 권한을 요청하지 않습니다."
        )
        val recommendationTitle = if (analysis == null) {
            "분석 전, 이 순서부터"
        } else {
            "현재 상황에서 먼저 볼 경로"
        }
        val readinessMessage = readinessMessage(readiness.level)

        return QuickStopMode(
            title = "긴급 멈춤 모드",
            intro = "겁이 나거나 판단이 어려우면 설명하려 애쓰지 않아도 됩니다. 먼저 멈추고, 주지 말고, 평소 신뢰 경로로 확인하세요.",
            stopScriptTitle = "그대로 읽는 3단계 멈춤 대사",
            stopScriptSteps = stopScriptSteps,
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                analysis = analysis,
                readiness = readiness
            ),
            readinessMessage = readinessMessage,
            recommendedCardIds = recommendedCardIds,
            cards = cards,
            privacySafetyNotes = privacySafetyNotes,
            familyShareSummary = buildFamilyShareSummary(
                stopScriptSteps = stopScriptSteps,
                recommendedCardIds = recommendedCardIds,
                cards = cards
            )
        )
    }

    private fun buildCards(
        hasAnalysis: Boolean,
        readiness: FamilyReadiness
    ): List<QuickStopActionCard> = listOf(
        QuickStopActionCard(
            id = QuickStopCardId.STOP_CONVERSATION,
            displayOrder = 1,
            urgency = QuickStopUrgency.STOP_NOW,
            label = "지금 대화 멈추기",
            explanation = "상대에게 해명하거나 설득할 필요 없이 통화를 끊고 답장을 보류한 뒤 위의 세 문장을 천천히 확인합니다.",
            primaryAction = QuickStopAction(
                id = QuickStopActionId.STOP_SCRIPT,
                label = "3단계 멈춤 대사 다시 읽기"
            ),
            secondaryActions = listOf(
                QuickStopAction(
                    id = QuickStopActionId.SAFETY_FLOW_ROUTER,
                    label = "상황별 안전 흐름 보기"
                )
            ),
            safetyNotes = listOf(
                "버튼을 눌러도 통화나 메시지를 자동으로 차단하지 않으며, 이 화면에 그대로 머뭅니다.",
                "상대가 재촉해도 확인이 끝날 때까지 송금·정보 공유·앱 설치를 다시 시작하지 마세요."
            )
        ),
        QuickStopActionCard(
            id = QuickStopCardId.FAMILY_CALLBACK,
            displayOrder = 2,
            urgency = QuickStopUrgency.VERIFY,
            label = "가족 콜백 확인",
            explanation = "새로 받은 번호가 아니라 가족 안전 계획에 직접 저장한 평소 번호와 안전 질문으로 확인합니다.",
            primaryAction = QuickStopAction(
                id = QuickStopActionId.FAMILY_CALLBACK_CARD,
                label = "가족 콜백 확인 카드"
            ),
            secondaryActions = listOf(
                QuickStopAction(
                    id = QuickStopActionId.SAFETY_PLAN,
                    label = safetyPlanActionLabel(readiness.level)
                )
            ),
            safetyNotes = listOf(
                "앱은 연락처를 읽지 않으며 안전 계획의 연락처는 사용자가 직접 입력합니다.",
                "전화 버튼이 있더라도 전화 앱만 열고 실제 통화는 사용자가 직접 시작합니다."
            )
        ),
        QuickStopActionCard(
            id = QuickStopCardId.RISK_ANALYSIS,
            displayOrder = 3,
            urgency = QuickStopUrgency.VERIFY,
            label = "위험 분석하기",
            explanation = if (hasAnalysis) {
                "현재 로컬 분석 결과와 멈춤 행동을 다시 확인하거나, 다른 의심 내용을 직접 붙여넣어 검사합니다."
            } else {
                "의심 문자·메신저·통화 내용을 사용자가 직접 붙여넣어 기기 안에서 위험 신호를 확인합니다."
            },
            primaryAction = if (hasAnalysis) {
                QuickStopAction(
                    id = QuickStopActionId.ANALYSIS_RESULT,
                    label = "현재 분석 결과 보기"
                )
            } else {
                QuickStopAction(
                    id = QuickStopActionId.CHECKER,
                    label = "의심 내용 직접 분석하기"
                )
            },
            secondaryActions = if (hasAnalysis) {
                listOf(
                    QuickStopAction(
                        id = QuickStopActionId.CHECKER,
                        label = "다른 내용 직접 분석하기"
                    )
                )
            } else {
                emptyList()
            },
            safetyNotes = listOf(
                "앱이 문자나 통화를 자동으로 찾거나 스캔하는 기능이 아니라 사용자가 넣은 텍스트만 확인합니다.",
                "분석 결과는 참고 안내이며 사기 여부나 안전을 보장하지 않습니다."
            )
        ),
        QuickStopActionCard(
            id = QuickStopCardId.EXPOSURE_RESPONSE,
            displayOrder = 4,
            urgency = QuickStopUrgency.RESPOND,
            label = "이미 돈·정보·앱을 했어요",
            explanation = "이미 송금·정보 제공·앱 설치·원격 허용을 했다면 실제로 한 행동에 맞는 수동 대응과 계정·기기 보호 순서를 엽니다.",
            primaryAction = QuickStopAction(
                id = QuickStopActionId.RESPONSE_CENTER,
                label = "긴급 대응 센터 열기"
            ),
            secondaryActions = listOf(
                QuickStopAction(
                    id = QuickStopActionId.ACCOUNT_LOCKDOWN,
                    label = "계정·기기 잠금 체크리스트"
                )
            ),
            safetyNotes = listOf(
                "이 앱이 지급정지·계정 잠금·앱 제거를 대신 실행하지 않습니다.",
                "추가 송금과 정보 제공을 멈추고 공식 금융회사·기관 경로에서 직접 조치하세요."
            )
        ),
        QuickStopActionCard(
            id = QuickStopCardId.OFFICIAL_HELP,
            displayOrder = 5,
            urgency = QuickStopUrgency.PRESERVE_AND_HELP,
            label = "공식 도움·신고 번호",
            explanation = "앱에 내장된 신고·상담 창구와 전화 전에 말할 내용을 확인합니다.",
            primaryAction = QuickStopAction(
                id = QuickStopActionId.OFFICIAL_HELP_DIRECTORY,
                label = "신고·상담 연락처 보기"
            ),
            safetyNotes = listOf(
                "상대가 문자·메신저로 알려준 번호가 아니라 공식 앱·카드 뒷면 등 평소 확인 가능한 경로를 사용하세요.",
                "전화 버튼은 통화를 자동으로 걸지 않고 전화 앱만 엽니다."
            )
        ),
        QuickStopActionCard(
            id = QuickStopCardId.EVIDENCE_PRESERVATION,
            displayOrder = 6,
            urgency = QuickStopUrgency.PRESERVE_AND_HELP,
            label = "증거 보존",
            explanation = "메시지·통화 시각·거래·앱 정보를 급히 지우기 전에 기존 수동 보존 체크리스트를 확인합니다.",
            primaryAction = QuickStopAction(
                id = QuickStopActionId.EVIDENCE_KIT,
                label = "증거 보존 체크리스트 열기"
            ),
            secondaryActions = listOf(
                QuickStopAction(
                    id = QuickStopActionId.OFFICIAL_HELP_DIRECTORY,
                    label = "공식 도움 경로 확인"
                )
            ),
            safetyNotes = listOf(
                "앱이 증거를 자동으로 캡처·수집·업로드하지 않습니다.",
                "자금·계정 보호나 원격 연결 차단이 급하면 증거 정리보다 그 조치를 먼저 하세요."
            )
        ),
        QuickStopActionCard(
            id = QuickStopCardId.FAMILY_ALERT,
            displayOrder = 7,
            urgency = QuickStopUrgency.WARN_FAMILY,
            label = "가족에게 경고 공유",
            explanation = "붙여넣은 원문과 가족 비밀 값을 넣지 않는 기존 가족 경고 메시지 팩으로 이동합니다.",
            primaryAction = QuickStopAction(
                id = QuickStopActionId.FAMILY_ALERT_MESSAGES,
                label = "가족 경고 메시지 만들기"
            ),
            secondaryActions = listOf(
                QuickStopAction(
                    id = QuickStopActionId.FAMILY_CALLBACK_CARD,
                    label = "가족 콜백 확인 카드"
                )
            ),
            safetyNotes = listOf(
                "앱은 연락처와 받는 사람을 읽거나 선택하지 않고 메시지를 자동으로 보내지 않습니다.",
                "공유 앱과 대상, 실제 전송 여부는 사용자가 직접 결정합니다."
            )
        )
    )

    private fun recommendCardIds(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness
    ): List<QuickStopCardId> {
        if (analysis == null) {
            return listOf(
                QuickStopCardId.STOP_CONVERSATION,
                QuickStopCardId.RISK_ANALYSIS,
                QuickStopCardId.FAMILY_CALLBACK
            )
        }

        val signals = analysis.matchedSignals.toSet()
        val hasFamilySignal = signals.any { signal -> signal in familySignals }
        val hasUrgentExposure = analysis.riskLevel == RiskLevel.HIGH ||
            signals.any { signal -> signal in urgentExposureSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val recommended = linkedSetOf(QuickStopCardId.STOP_CONVERSATION)

        if (hasFamilySignal || readiness.level != ReadinessLevel.READY) {
            recommended += QuickStopCardId.FAMILY_CALLBACK
        }
        if (hasLinkSignal) {
            recommended += QuickStopCardId.RISK_ANALYSIS
        }
        if (hasUrgentExposure) {
            recommended += QuickStopCardId.EXPOSURE_RESPONSE
        }
        if (hasUrgentExposure || hasLinkSignal) {
            recommended += QuickStopCardId.EVIDENCE_PRESERVATION
        }
        if (hasLinkSignal) {
            recommended += QuickStopCardId.FAMILY_ALERT
        }
        if (hasUrgentExposure || hasLinkSignal) {
            recommended += QuickStopCardId.OFFICIAL_HELP
        }
        if (!hasUrgentExposure && !hasLinkSignal) {
            recommended += QuickStopCardId.RISK_ANALYSIS
        }
        if (recommended.size == 2 && QuickStopCardId.RISK_ANALYSIS in recommended) {
            recommended += QuickStopCardId.FAMILY_CALLBACK
        }

        return recommended.toList()
    }

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness
    ): String {
        val readinessSuffix = " ${readinessMessage(readiness.level)}"
        if (analysis == null) {
            return "대화를 먼저 멈춘 뒤 의심 내용을 직접 분석하고, 평소 가족 번호로 확인하세요.$readinessSuffix"
        }

        val signals = analysis.matchedSignals.toSet()
        val hasFamilySignal = signals.any { signal -> signal in familySignals }
        val hasUrgentExposure = analysis.riskLevel == RiskLevel.HIGH ||
            signals.any { signal -> signal in urgentExposureSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val situationCopy = when {
            hasFamilySignal && hasUrgentExposure ->
                "가족·목소리·번호 변경 신호가 있어 평소 번호로 콜백을 먼저 확인하세요. 이미 실행한 행동이 있다면 긴급 대응과 증거·공식 도움을 이어서 확인하세요."
            hasFamilySignal && hasLinkSignal ->
                "가족·번호 변경 가능성을 평소 번호로 먼저 확인하고, 링크를 다시 열지 않은 채 분석·증거·가족 경고·공식 도움 경로를 확인하세요."
            hasFamilySignal ->
                "가족·목소리·번호 변경 신호가 있어 평소 번호로 콜백한 뒤 현재 분석 행동을 확인하세요."
            hasUrgentExposure ->
                "강한 송금·정보·앱 위험 신호가 있습니다. 이미 실행한 행동이 있다면 긴급 대응, 증거 보존, 공식 도움 순서로 직접 확인하세요."
            hasLinkSignal ->
                "링크·스미싱 신호가 있습니다. 링크를 다시 열지 말고 현재 분석, 증거 보존, 가족 경고, 공식 도움을 확인하세요."
            else ->
                "현재 분석의 행동 안내를 다시 보고, 돈·정보·앱 요구는 평소 가족 번호로 확인하세요."
        }
        return situationCopy + readinessSuffix
    }

    private fun readinessMessage(level: ReadinessLevel): String = when (level) {
        ReadinessLevel.LOW ->
            "가족 콜백 준비가 아직 부족해도 괜찮습니다. 콜백 카드에서 가족 안전 계획을 차분히 시작하세요."
        ReadinessLevel.IN_PROGRESS ->
            "가족 콜백 준비가 진행 중입니다. 콜백 카드에서 빠진 안전 계획을 차분히 보완하세요."
        ReadinessLevel.READY ->
            "저장된 가족 콜백 루틴이 준비되어 있습니다. 평소 번호와 안전 질문으로 차분히 확인하세요."
    }

    private fun safetyPlanActionLabel(level: ReadinessLevel): String = when (level) {
        ReadinessLevel.LOW -> "가족 안전 계획 만들기"
        ReadinessLevel.IN_PROGRESS -> "가족 안전 계획 보완하기"
        ReadinessLevel.READY -> "가족 안전 계획 확인하기"
    }

    private fun buildFamilyShareSummary(
        stopScriptSteps: List<QuickStopStep>,
        recommendedCardIds: List<QuickStopCardId>,
        cards: List<QuickStopActionCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }

        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 긴급 멈춤 안내")
            appendLine()
            appendLine("먼저 함께 지켜 주세요")
            stopScriptSteps.forEach { step ->
                appendLine("${step.displayOrder}. ${step.instruction}")
            }
            appendLine()
            appendLine("앱에서 먼저 볼 경로")
            recommendedCardIds.forEach { cardId ->
                val card = cardsById.getValue(cardId)
                appendLine("- ${card.label}: ${actionLabels(card)}")
            }
            appendLine()
            appendLine("선택 가능한 모든 경로")
            cards.forEach { card ->
                appendLine("- ${card.label}: ${actionLabels(card)}")
            }
            appendLine()
            appendLine("이 안내는 수동 확인용이며 버튼 선택·공유 이력·체크 상태를 저장하지 않습니다.")
            appendLine("앱은 연락처를 읽거나 받는 사람을 고르지 않고 자동으로 보내지 않습니다.")
            append(
                "붙여넣은 원문·화면 단서·가족 안전 질문·콜백 내용·연락처 값과 전화·계좌·비밀번호·" +
                    "인증번호·PIN·링크·신분정보 값은 이 공유문에 포함하지 않았습니다."
            )
        }.trim()
    }

    private fun actionLabels(card: QuickStopActionCard): String =
        (listOf(card.primaryAction) + card.secondaryActions)
            .joinToString(" / ") { action -> action.label }
}
