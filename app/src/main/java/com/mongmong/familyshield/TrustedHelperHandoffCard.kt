package com.mongmong.familyshield

enum class TrustedHelperCardId {
    CALM_COMPANION,
    FAMILY_CONFIRMATION_HELPER,
    FINANCE_ACCOUNT_HELPER,
    DEVICE_APP_HELPER,
    EVIDENCE_REPORT_HELPER,
    FAMILY_WARNING_HELPER,
    WEEKLY_REVIEW_HELPER
}

enum class TrustedHelperActionId {
    QUICK_STOP,
    FAMILY_CALLBACK_CARD,
    CHECKER,
    ANALYSIS_RESULT,
    SAFETY_PLAN,
    SAFETY_FLOW_ROUTER,
    RESPONSE_CENTER,
    ACCOUNT_LOCKDOWN,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    NEW_ANALYSIS,
    FAMILY_ALERT_MESSAGES,
    RECOVERY_PLAN,
    TIMELINE_BRIEFING,
    PRIVACY_CENTER
}

enum class TrustedHelperUrgency(val rank: Int, val label: String) {
    STOP_NOW(0, "지금 먼저"),
    VERIFY(1, "확인 동행"),
    CLEANUP(2, "정리·신고 도움"),
    PREVENT(3, "예방·점검")
}

data class TrustedHelperAction(
    val id: TrustedHelperActionId,
    val label: String
)

data class TrustedHelperCard(
    val id: TrustedHelperCardId,
    val displayOrder: Int,
    val urgency: TrustedHelperUrgency,
    val label: String,
    val audience: String,
    val whyItMatters: String,
    val askSteps: List<String>,
    val doNotAskNotes: List<String>,
    val primaryAction: TrustedHelperAction,
    val secondaryActions: List<TrustedHelperAction> = emptyList()
) {
    val primaryActionLabel: String
        get() = primaryAction.label

    val secondaryActionLabels: List<String>
        get() = secondaryActions.map { action -> action.label }
}

data class TrustedHelperHandoffCard(
    val title: String,
    val intro: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<TrustedHelperCardId>,
    val cards: List<TrustedHelperCard>,
    val privacySafetyNotes: List<String>,
    val handoffShareMessage: String
) {
    val recommendedCards: List<TrustedHelperCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<TrustedHelperActionId>
        get() = recommendedCards
            .flatMap { card -> listOf(card.primaryAction) + card.secondaryActions }
            .map { action -> action.id }
            .distinct()
}

object TrustedHelperHandoffBuilder {
    private val familySignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.PHONE_NUMBER_CHANGE
    )

    private val financialCredentialSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.BAIT,
        ScamSignal.CREDENTIALS,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.GIFTICON_PIN,
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

    fun build(analysis: ScamAnalysis?, readiness: FamilyReadiness): TrustedHelperHandoffCard {
        val cards = buildCards(hasAnalysis = analysis != null)
        val recommendedCardIds = recommendCardIds(analysis, readiness)
        val recommendationTitle = if (analysis == null) {
            "분석 전 기본 추천"
        } else {
            "현재 상황 기준 추천 도우미"
        }

        return TrustedHelperHandoffCard(
            title = "보호자 도움 요청 카드",
            intro = "지금 상황을 혼자 감당하지 말고, 역할에 맞는 도우미에게 무엇을 도와달라고 할지 정리했습니다. " +
                "원문이나 비밀 정보를 그대로 보내지 않아도 됩니다.",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(analysis, readiness),
            recommendedCardIds = recommendedCardIds,
            cards = cards,
            privacySafetyNotes = privacySafetyNotes(),
            handoffShareMessage = buildHandoffShareMessage(
                recommendationTitle = recommendationTitle,
                recommendedCardIds = recommendedCardIds,
                cards = cards
            )
        )
    }

    private fun buildCards(hasAnalysis: Boolean): List<TrustedHelperCard> = listOf(
        TrustedHelperCard(
            id = TrustedHelperCardId.CALM_COMPANION,
            displayOrder = 1,
            urgency = TrustedHelperUrgency.STOP_NOW,
            label = "차분한 보호자/동행자",
            audience = "지금 옆에서 함께 있어줄 가족이나 가까운 지인",
            whyItMatters = "지금은 상황을 설명하기보다 압박을 멈추고 곁에서 함께 있어줄 사람이 먼저 필요합니다.",
            askSteps = listOf(
                "지금 통화나 대화를 잠시 멈추도록 옆에서 함께 도와달라고 하세요.",
                "혼자 판단하지 않도록 몇 분만 함께 있어달라고 하세요.",
                "평소 가족 번호로 다시 전화하는 걸 옆에서 지켜봐 달라고 하세요."
            ),
            doNotAskNotes = listOf(
                "무슨 일이 있었는지 캐묻거나 다그치지 말아 달라고 하세요.",
                "비밀번호나 인증번호 같은 값을 알려달라고 하지 마세요."
            ),
            primaryAction = TrustedHelperAction(TrustedHelperActionId.QUICK_STOP, "긴급 멈춤 모드"),
            secondaryActions = listOf(
                TrustedHelperAction(TrustedHelperActionId.FAMILY_CALLBACK_CARD, "가족 콜백 확인 카드"),
                if (hasAnalysis) {
                    TrustedHelperAction(TrustedHelperActionId.ANALYSIS_RESULT, "현재 분석 결과 보기")
                } else {
                    TrustedHelperAction(TrustedHelperActionId.CHECKER, "의심 내용 직접 분석하기")
                }
            )
        ),
        TrustedHelperCard(
            id = TrustedHelperCardId.FAMILY_CONFIRMATION_HELPER,
            displayOrder = 2,
            urgency = TrustedHelperUrgency.VERIFY,
            label = "가족 확인 도우미",
            audience = "가족 안전 질문과 콜백 번호를 함께 확인해줄 가족",
            whyItMatters = "새 번호나 목소리만으로 가족을 확인하면 위험하므로 기존 번호와 안전 질문으로 함께 확인할 사람이 필요합니다.",
            askSteps = listOf(
                "의심되는 연락 속 새 번호가 아니라 평소 알던 번호로 함께 전화해 달라고 하세요.",
                "가족 안전 계획에 안전 질문과 신뢰 번호를 함께 채워달라고 하세요.",
                "확인이 끝날 때까지 송금이나 정보 제공을 함께 멈춰달라고 하세요."
            ),
            doNotAskNotes = listOf(
                "안전 질문의 답이나 콜백 스크립트 내용을 문자로 미리 보내달라고 하지 마세요.",
                "새로 온 번호를 대신 저장하거나 답장해달라고 하지 마세요."
            ),
            primaryAction = TrustedHelperAction(TrustedHelperActionId.FAMILY_CALLBACK_CARD, "가족 콜백 확인 카드"),
            secondaryActions = listOf(
                TrustedHelperAction(TrustedHelperActionId.SAFETY_PLAN, "가족 안전 계획 확인"),
                TrustedHelperAction(TrustedHelperActionId.SAFETY_FLOW_ROUTER, "안전 흐름 길잡이")
            )
        ),
        TrustedHelperCard(
            id = TrustedHelperCardId.FINANCE_ACCOUNT_HELPER,
            displayOrder = 3,
            urgency = TrustedHelperUrgency.CLEANUP,
            label = "금융·계정 정리 도우미",
            audience = "은행·카드·계정 보호를 함께 확인해줄 가족이나 지인",
            whyItMatters = "돈이나 로그인 정보가 노출됐을 수 있어 지급정지·계정 보호 절차를 함께 확인할 사람이 필요합니다.",
            askSteps = listOf(
                "긴급 대응 센터에서 지급정지·신고 절차를 같이 확인해 달라고 하세요.",
                "계정·기기 잠금 체크리스트를 함께 보며 순서를 챙겨달라고 하세요.",
                "공식 신고·상담 연락처로 전화할 때 옆에서 함께 있어달라고 하세요."
            ),
            doNotAskNotes = listOf(
                "실제 계좌번호, 비밀번호, 인증번호를 알려달라고 하지 마세요.",
                "대신 송금하거나 대신 로그인해달라고 하지 마세요."
            ),
            primaryAction = TrustedHelperAction(TrustedHelperActionId.RESPONSE_CENTER, "긴급 대응 센터"),
            secondaryActions = listOf(
                TrustedHelperAction(TrustedHelperActionId.ACCOUNT_LOCKDOWN, "계정·기기 잠금 체크리스트"),
                TrustedHelperAction(TrustedHelperActionId.OFFICIAL_HELP_DIRECTORY, "신고·상담 연락처")
            )
        ),
        TrustedHelperCard(
            id = TrustedHelperCardId.DEVICE_APP_HELPER,
            displayOrder = 4,
            urgency = TrustedHelperUrgency.CLEANUP,
            label = "기기·앱 점검 도우미",
            audience = "기기와 앱 상태를 같이 점검해줄 가족이나 지인",
            whyItMatters = "원격제어나 출처 불명 앱이 설치됐을 수 있어 연결을 끊고 점검을 도와줄 사람이 필요합니다.",
            askSteps = listOf(
                "화면공유·원격제어 연결이 남아 있는지 함께 확인하고 끊어달라고 하세요.",
                "계정·기기 잠금 체크리스트 순서를 같이 짚어달라고 하세요.",
                "의심되는 앱이나 권한이 남아있는지 같이 살펴봐 달라고 하세요."
            ),
            doNotAskNotes = listOf(
                "기기를 대신 초기화하거나 앱을 마음대로 삭제해달라고 하지 마세요.",
                "내 비밀번호나 인증번호를 대신 입력해달라고 하지 마세요."
            ),
            primaryAction = TrustedHelperAction(TrustedHelperActionId.ACCOUNT_LOCKDOWN, "계정·기기 잠금 체크리스트"),
            secondaryActions = listOf(
                TrustedHelperAction(TrustedHelperActionId.EVIDENCE_KIT, "증거 보존 체크리스트"),
                TrustedHelperAction(TrustedHelperActionId.RESPONSE_CENTER, "긴급 대응 센터")
            )
        ),
        TrustedHelperCard(
            id = TrustedHelperCardId.EVIDENCE_REPORT_HELPER,
            displayOrder = 5,
            urgency = TrustedHelperUrgency.CLEANUP,
            label = "증거·신고 정리 도우미",
            audience = "화면과 시간을 정리하고 신고를 준비해줄 가족이나 지인",
            whyItMatters = "필요한 증거를 지우기 전에 함께 정리하고 신고·상담을 준비할 사람이 필요합니다.",
            askSteps = listOf(
                "메시지·통화·거래 화면과 시각을 지우지 않고 같이 정리해달라고 하세요.",
                "신고·상담 연락처와 준비 카드 내용을 같이 확인해달라고 하세요.",
                "필요하면 전화할 때 옆에서 같이 있어달라고 하세요."
            ),
            doNotAskNotes = listOf(
                "증거를 확인한다며 의심 링크나 앱을 대신 열어달라고 하지 마세요.",
                "민감한 계좌·비밀번호·신분정보를 대신 정리해서 보내달라고 하지 마세요."
            ),
            primaryAction = TrustedHelperAction(TrustedHelperActionId.EVIDENCE_KIT, "증거 보존 체크리스트"),
            secondaryActions = listOf(
                TrustedHelperAction(TrustedHelperActionId.OFFICIAL_HELP_DIRECTORY, "신고·상담 연락처"),
                if (hasAnalysis) {
                    TrustedHelperAction(TrustedHelperActionId.REPORT_PREP, "신고용 상담 준비 카드")
                } else {
                    TrustedHelperAction(TrustedHelperActionId.NEW_ANALYSIS, "새 의심 내용 분석")
                }
            )
        ),
        TrustedHelperCard(
            id = TrustedHelperCardId.FAMILY_WARNING_HELPER,
            displayOrder = 6,
            urgency = TrustedHelperUrgency.VERIFY,
            label = "가족 경고 전달 도우미",
            audience = "다른 가족과 지인에게 위험을 함께 알려줄 사람",
            whyItMatters = "같은 수법이 다른 가족에게도 갈 수 있어 비밀 없이 위험만 알려줄 사람이 필요합니다.",
            askSteps = listOf(
                "가족 경고 메시지 팩의 문구를 그대로 다른 가족에게 전달해달라고 하세요.",
                "새 번호나 링크로 확인하지 말고 기존 연락 방법을 쓰라고 함께 알려달라고 하세요.",
                "경고를 보낸 뒤 가족들이 이상한 요청에 답하지 않았는지 같이 확인해달라고 하세요."
            ),
            doNotAskNotes = listOf(
                "안전 질문 답이나 콜백 번호 같은 비밀 정보를 함께 퍼뜨려달라고 하지 마세요.",
                "확인되지 않은 원문 캡처를 그대로 다른 사람에게 퍼뜨려달라고 하지 마세요."
            ),
            primaryAction = TrustedHelperAction(TrustedHelperActionId.FAMILY_ALERT_MESSAGES, "가족 경고 메시지 팩"),
            secondaryActions = listOf(
                TrustedHelperAction(TrustedHelperActionId.SAFETY_FLOW_ROUTER, "안전 흐름 길잡이")
            )
        ),
        TrustedHelperCard(
            id = TrustedHelperCardId.WEEKLY_REVIEW_HELPER,
            displayOrder = 7,
            urgency = TrustedHelperUrgency.PREVENT,
            label = "이번 주 점검 도우미",
            audience = "이번 주 예방·점검을 같이 챙겨줄 가족",
            whyItMatters = "한 번의 대응으로 끝내지 않고 이번 주 안에 예방 습관과 준비 상태를 같이 점검할 사람이 필요합니다.",
            askSteps = listOf(
                "24시간 수습 계획의 이번 주 항목을 같이 확인해달라고 하세요.",
                "사건 타임라인과 반복되는 신호를 같이 살펴봐달라고 하세요.",
                "개인정보 센터에서 남은 데이터를 정리할 때 같이 봐달라고 하세요."
            ),
            doNotAskNotes = listOf(
                "지난 대화 원문이나 캡처를 전부 보여달라고 요구하지 마세요.",
                "점검을 이유로 매번 비밀번호나 인증번호를 공유해달라고 하지 마세요."
            ),
            primaryAction = TrustedHelperAction(TrustedHelperActionId.RECOVERY_PLAN, "24시간 수습 계획"),
            secondaryActions = listOf(
                TrustedHelperAction(TrustedHelperActionId.TIMELINE_BRIEFING, "사건 타임라인 요약"),
                TrustedHelperAction(TrustedHelperActionId.PRIVACY_CENTER, "개인정보 센터")
            )
        )
    )

    private fun recommendCardIds(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness
    ): List<TrustedHelperCardId> {
        if (analysis == null) {
            return listOf(
                TrustedHelperCardId.CALM_COMPANION,
                TrustedHelperCardId.FAMILY_CONFIRMATION_HELPER,
                TrustedHelperCardId.WEEKLY_REVIEW_HELPER
            )
        }

        val signals = analysis.matchedSignals.toSet()
        val hasFamilySignal = signals.any { signal -> signal in familySignals }
        val hasFinancialSignal = signals.any { signal -> signal in financialCredentialSignals }
        val hasRemoteSignal = signals.any { signal -> signal in remoteSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val recommended = linkedSetOf<TrustedHelperCardId>()

        if (hasFamilySignal) {
            recommended += TrustedHelperCardId.FAMILY_CONFIRMATION_HELPER
            recommended += TrustedHelperCardId.FAMILY_WARNING_HELPER
        }
        if (hasFinancialSignal) {
            recommended += TrustedHelperCardId.FINANCE_ACCOUNT_HELPER
            recommended += TrustedHelperCardId.EVIDENCE_REPORT_HELPER
        }
        if (hasRemoteSignal) {
            recommended += TrustedHelperCardId.DEVICE_APP_HELPER
            recommended += TrustedHelperCardId.EVIDENCE_REPORT_HELPER
            recommended += TrustedHelperCardId.FINANCE_ACCOUNT_HELPER
        }
        if (hasLinkSignal) {
            recommended += TrustedHelperCardId.EVIDENCE_REPORT_HELPER
            recommended += TrustedHelperCardId.FAMILY_WARNING_HELPER
            recommended += TrustedHelperCardId.CALM_COMPANION
        }
        if (readiness.level != ReadinessLevel.READY) {
            recommended += TrustedHelperCardId.FAMILY_CONFIRMATION_HELPER
        }
        if (recommended.isEmpty()) {
            recommended += TrustedHelperCardId.CALM_COMPANION
            recommended += TrustedHelperCardId.FAMILY_CONFIRMATION_HELPER
            recommended += TrustedHelperCardId.WEEKLY_REVIEW_HELPER
        }

        return recommended.toList()
    }

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness
    ): String {
        val readinessSuffix = " ${readinessMessage(readiness.level)}"
        if (analysis == null) {
            return "먼저 차분한 보호자에게 곁에 있어달라고 부탁하고, 의심 내용을 새로 분석하거나 가족 확인 도우미와 " +
                "기존 번호로 확인한 뒤 이번 주 점검까지 함께 부탁하세요.$readinessSuffix"
        }

        val signals = analysis.matchedSignals.toSet()
        val hasFamilySignal = signals.any { signal -> signal in familySignals }
        val hasFinancialSignal = signals.any { signal -> signal in financialCredentialSignals }
        val hasRemoteSignal = signals.any { signal -> signal in remoteSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val situationCopy = when {
            hasFamilySignal && (hasFinancialSignal || hasRemoteSignal) ->
                "가족·목소리·번호 변경 신호가 있어 가족 확인 도우미와 가족 경고 전달 도우미에게 먼저 부탁하고, " +
                    "실제로 돈이나 정보를 보냈다면 금융·계정 정리 도우미와 증거·신고 정리 도우미에게도 함께 부탁하세요."
            hasFamilySignal ->
                "가족·목소리·번호 변경 신호가 있어 가족 확인 도우미와 가족 경고 전달 도우미에게 먼저 도움을 요청하세요."
            hasFinancialSignal ->
                "송금·인증정보 관련 신호가 있어 금융·계정 정리 도우미와 증거·신고 정리 도우미에게 함께 도움을 요청하세요."
            hasRemoteSignal ->
                "원격제어·앱 설치 신호가 있어 기기·앱 점검 도우미와 증거·신고 정리 도우미, 금융·계정 정리 도우미에게 함께 도움을 요청하세요."
            hasLinkSignal ->
                "링크·스미싱 신호가 있어 증거·신고 정리 도우미와 가족 경고 전달 도우미에게 도움을 요청하고, " +
                    "차분한 보호자에게 현재 분석 결과도 함께 봐달라고 하세요."
            else ->
                "뚜렷한 위험 신호는 적지만, 차분한 보호자와 가족 확인 도우미에게 상황을 알리고 함께 확인하세요."
        }
        return situationCopy + readinessSuffix
    }

    private fun readinessMessage(level: ReadinessLevel): String = when (level) {
        ReadinessLevel.LOW ->
            "가족 콜백 준비가 부족하니 가족 확인 도우미와 함께 안전 계획부터 채워달라고 부탁하세요."
        ReadinessLevel.IN_PROGRESS ->
            "가족 콜백 준비가 진행 중이니 가족 확인 도우미와 함께 남은 항목을 보완하세요."
        ReadinessLevel.READY ->
            "저장된 가족 콜백 루틴이 준비되어 있으니 가족 확인 도우미와 함께 그대로 점검만 하세요."
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 카드는 사용자가 직접 여는 수동 안내이며 문자·통화·연락처를 자동으로 읽거나 감시하지 않습니다.",
        "현재 분석은 사용자가 직접 붙여넣거나 텍스트로 공유한 내용만 기기 안에서 판단하며 네트워크로 업로드하지 않습니다.",
        "어떤 도우미를 골랐는지, 공유 여부나 진행 상황을 저장하지 않으며, 이 화면을 여는 것만으로 사건 기록을 추가·수정·삭제하지 않습니다.",
        "공유 버튼은 연락처를 읽거나 받는 사람을 고르지 않고 자동으로 보내지 않습니다. 사용자가 공유 앱과 대상을 직접 선택합니다.",
        "도움 요청 공유문에는 붙여넣은 원문, 개인정보 가림 단서, 안전 질문·콜백 내용·연락처 값과 전화·계좌·비밀번호·" +
            "인증번호·PIN·링크·신분정보 값을 넣지 않으며, 도우미가 이런 비밀 정보에 접근하게 하지 않습니다.",
        "이 카드는 계좌·카드를 자동으로 지급정지하거나 악성 앱을 자동으로 제거하지 않으며, 계정을 자동으로 잠그거나 피해금 회수를 보장하지 않습니다.",
        "법률·금융 자문이나 안전·회복 결과를 보장하는 기능이 아니며, 이 카드를 위해 네트워크 호출이나 새 Android 권한을 사용하지 않습니다."
    )

    private fun buildHandoffShareMessage(
        recommendationTitle: String,
        recommendedCardIds: List<TrustedHelperCardId>,
        cards: List<TrustedHelperCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }

        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 보호자 도움 요청")
            appendLine()
            appendLine(recommendationTitle)
            recommendedCardIds.forEach { cardId ->
                val card = cardsById.getValue(cardId)
                appendLine("- ${card.label} (${card.audience}): ${actionLabels(card)}")
            }
            appendLine()
            appendLine("도와줄 수 있는 전체 역할")
            cards.forEach { card ->
                appendLine("- ${card.label}: ${actionLabels(card)}")
            }
            appendLine()
            appendLine("이 요청은 제가 직접 눌러 공유하며 앱은 연락처를 읽거나 메시지를 자동으로 보내지 않습니다.")
            appendLine("도우미 선택이나 진행 상황은 저장하지 않으며 사건 기록을 자동으로 바꾸지 않습니다.")
            append(
                "붙여넣은 원문·화면 단서·안전 질문·콜백 내용·연락처 값과 전화·계좌·비밀번호·" +
                    "인증번호·PIN·링크·신분정보 값은 이 요청에 포함하지 않았습니다."
            )
        }.trim()
    }

    private fun actionLabels(card: TrustedHelperCard): String =
        (listOf(card.primaryAction) + card.secondaryActions)
            .joinToString(" / ") { action -> action.label }
}
