package com.mongmong.familyshield

enum class RiskSignalCategory(val displayOrder: Int, val label: String) {
    URGENCY_MONEY_PRESSURE(1, "긴급·송금·기관 압박"),
    LINKS_AND_SMISHING(2, "링크·스미싱"),
    CREDENTIALS_AND_SECRETS(3, "계정·인증 비밀정보"),
    APP_AND_REMOTE_CONTROL(4, "앱 설치·원격제어"),
    FAMILY_IMPERSONATION(5, "가족 사칭·목소리·번호 변경"),
    FINANCIAL_BAIT(6, "안전계좌·환급·대출·결제 미끼"),
    GIFT_AND_CRYPTO(7, "상품권·기프티콘·가상자산")
}

enum class RiskSignalExplainerUrgency(val rank: Int, val label: String) {
    STOP_NOW(0, "지금 멈춤"),
    VERIFY_FIRST(1, "직접 확인"),
    PROTECT_IF_EXPOSED(2, "노출됐다면 보호"),
    LEARN_AND_PREPARE(3, "가족과 미리 확인")
}

enum class RiskSignalExplainerActionId {
    QUICK_STOP,
    CHECKER,
    ANALYSIS_RESULT,
    FAMILY_CALLBACK_CARD,
    SAFETY_PLAN,
    SAFETY_FLOW_ROUTER,
    ACCOUNT_LOCKDOWN,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    FAMILY_ALERT_MESSAGES,
    TRUSTED_HELPER_HANDOFF,
    SAFE_CALL_SCRIPTS,
    RECOVERY_PLAN
}

data class RiskSignalExplainerAction(
    val id: RiskSignalExplainerActionId,
    val label: String
)

data class RiskSignalExplainerCard(
    val id: ScamSignal,
    val displayOrder: Int,
    val category: RiskSignalCategory,
    val urgency: RiskSignalExplainerUrgency,
    val label: String,
    val whySuspicious: String,
    val scamPressureGoal: String,
    val immediateSafeMove: String,
    val familyDiscussionPrompt: String,
    val primaryAction: RiskSignalExplainerAction,
    val secondaryActions: List<RiskSignalExplainerAction> = emptyList()
) {
    val signalKey: String
        get() = id.name

    val primaryActionLabel: String
        get() = primaryAction.label

    val secondaryActionLabels: List<String>
        get() = secondaryActions.map { action -> action.label }
}

data class RiskSignalExplainer(
    val title: String,
    val intro: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedSignalIds: List<ScamSignal>,
    val recommendedActions: List<RiskSignalExplainerAction>,
    val cards: List<RiskSignalExplainerCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<RiskSignalExplainerCard>
        get() = recommendedSignalIds.mapNotNull { signalId ->
            cards.find { card -> card.id == signalId }
        }

    val recommendedActionIds: List<RiskSignalExplainerActionId>
        get() = recommendedActions.map { action -> action.id }
}

object RiskSignalExplainerBuilder {
    private val familySignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.PHONE_NUMBER_CHANGE,
        ScamSignal.DEEPFAKE_VOICE
    )

    private val financialSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
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

    private val pressureSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.IMPERSONATION,
        ScamSignal.SECRECY
    )

    private val defaultSignalIds = listOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.CREDENTIALS,
        ScamSignal.LINK,
        ScamSignal.FAMILY_EMERGENCY
    )

    fun build(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness
    ): RiskSignalExplainer {
        val hasAnalysis = analysis != null
        val matchedSignals = analysis?.matchedSignals.orEmpty().toSet()
        val cards = buildCards(hasAnalysis, readiness)
        val recommendedSignalIds = recommendSignalIds(matchedSignals)
        val recommendedActionIds = recommendActionIds(
            analysis = analysis,
            matchedSignals = matchedSignals,
            readiness = readiness
        )
        val recommendedActions = recommendedActionIds.map { actionId ->
            actionFor(actionId, hasAnalysis, readiness.level)
        }
        val recommendationTitle = when {
            analysis == null -> "분석 전에도 알아둘 기본 신호"
            matchedSignals.isEmpty() -> "뚜렷한 일치 신호가 없을 때 기본 확인"
            else -> "현재 일치 신호부터 차분히 보기"
        }

        return RiskSignalExplainer(
            title = "위험 신호 설명 카드",
            intro = "사용자가 직접 넣은 텍스트에서 일치한 로컬 규칙이 왜 주의 신호인지 설명합니다. " +
                "원문을 다시 보여주지 않으며, 원격 AI나 공식 기관의 확정 판정이 아닌 교육용 안내입니다.",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                analysis = analysis,
                matchedSignals = matchedSignals,
                readiness = readiness
            ),
            recommendedSignalIds = recommendedSignalIds,
            recommendedActions = recommendedActions,
            cards = cards,
            privacySafetyNotes = privacySafetyNotes(),
            familyShareSummary = buildFamilyShareSummary(
                recommendationTitle = recommendationTitle,
                recommendedSignalIds = recommendedSignalIds,
                recommendedActions = recommendedActions,
                cards = cards
            )
        )
    }

    private fun buildCards(
        hasAnalysis: Boolean,
        readiness: FamilyReadiness
    ): List<RiskSignalExplainerCard> {
        fun action(id: RiskSignalExplainerActionId): RiskSignalExplainerAction =
            actionFor(id, hasAnalysis, readiness.level)

        fun resultAction(): RiskSignalExplainerAction = action(
            if (hasAnalysis) {
                RiskSignalExplainerActionId.ANALYSIS_RESULT
            } else {
                RiskSignalExplainerActionId.CHECKER
            }
        )

        return listOf(
            RiskSignalExplainerCard(
                id = ScamSignal.URGENT_TRANSFER,
                displayOrder = 1,
                category = RiskSignalCategory.URGENCY_MONEY_PRESSURE,
                urgency = RiskSignalExplainerUrgency.STOP_NOW,
                label = ScamSignal.URGENT_TRANSFER.label,
                whySuspicious = "생각하고 확인할 시간을 줄이면서 지금 돈을 보내야 한다고 재촉하는 방식은 안전한 판단을 어렵게 만듭니다.",
                scamPressureGoal = "확인이 끝나기 전에 송금·이체·입금을 실행하게 하려는 경우가 많습니다.",
                immediateSafeMove = "대화를 멈추고 송금을 보류한 뒤, 평소 저장한 가족 번호나 금융회사 공식 경로로 직접 확인하세요.",
                familyDiscussionPrompt = "누가 아무리 급하다고 해도 확인 전에는 돈을 보내지 않는다는 가족 규칙을 함께 정해 둘까요?",
                primaryAction = action(RiskSignalExplainerActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS),
                    action(RiskSignalExplainerActionId.RECOVERY_PLAN)
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.IMPERSONATION,
                displayOrder = 2,
                category = RiskSignalCategory.URGENCY_MONEY_PRESSURE,
                urgency = RiskSignalExplainerUrgency.VERIFY_FIRST,
                label = ScamSignal.IMPERSONATION.label,
                whySuspicious = "검찰·경찰·은행 같은 기관 이름은 신뢰와 두려움을 동시에 이용해 확인을 건너뛰게 할 수 있습니다.",
                scamPressureGoal = "기관 지시처럼 보이게 만들어 돈이나 인증정보를 내놓게 하려는 경우가 많습니다.",
                immediateSafeMove = "상대와의 연락을 끝내고, 그 연락에서 받은 번호가 아닌 공식 대표 경로를 직접 찾아 확인하세요.",
                familyDiscussionPrompt = "기관을 자칭하는 연락은 일단 끊고 어느 공식 경로로 다시 확인할지 가족과 정해 둘까요?",
                primaryAction = action(RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.OFFICIAL_HELP_DIRECTORY),
                    action(RiskSignalExplainerActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.APP_INSTALL,
                displayOrder = 3,
                category = RiskSignalCategory.APP_AND_REMOTE_CONTROL,
                urgency = RiskSignalExplainerUrgency.PROTECT_IF_EXPOSED,
                label = ScamSignal.APP_INSTALL.label,
                whySuspicious = "출처를 확인하지 않은 앱이나 원격제어 도구는 상대가 화면과 계정 행동에 개입할 통로가 될 수 있습니다.",
                scamPressureGoal = "기기 화면을 보거나 조작하게 만들고, 계정·금융 행동을 대신 진행하게 하려는 경우가 많습니다.",
                immediateSafeMove = "설치를 멈추고, 이미 허용했다면 원격 연결을 끊은 뒤 다른 안전한 기기에서 보호 순서를 확인하세요.",
                familyDiscussionPrompt = "앱 설치를 요구받으면 혼자 진행하지 않고 누구에게 먼저 도움을 요청할지 정해 둘까요?",
                primaryAction = action(RiskSignalExplainerActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.EVIDENCE_KIT),
                    action(RiskSignalExplainerActionId.TRUSTED_HELPER_HANDOFF)
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.LINK,
                displayOrder = 4,
                category = RiskSignalCategory.LINKS_AND_SMISHING,
                urgency = RiskSignalExplainerUrgency.VERIFY_FIRST,
                label = ScamSignal.LINK.label,
                whySuspicious = "메시지 속 링크는 주소와 운영 주체를 충분히 확인하기 전에 가짜 로그인·결제 화면으로 이끌 수 있습니다.",
                scamPressureGoal = "링크를 열어 계정정보나 결제정보를 입력하거나 파일을 받게 하려는 경우가 많습니다.",
                immediateSafeMove = "링크를 다시 열지 말고, 필요한 확인은 평소 쓰던 공식 앱이나 직접 찾은 공식 경로에서 하세요.",
                familyDiscussionPrompt = "가족 채팅에 링크가 와도 발신자에게 다른 경로로 먼저 확인하는 규칙을 만들어 둘까요?",
                primaryAction = action(RiskSignalExplainerActionId.EVIDENCE_KIT),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.FAMILY_ALERT_MESSAGES),
                    resultAction()
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.BAIT,
                displayOrder = 5,
                category = RiskSignalCategory.FINANCIAL_BAIT,
                urgency = RiskSignalExplainerUrgency.VERIFY_FIRST,
                label = ScamSignal.BAIT.label,
                whySuspicious = "대출·지원금·환급처럼 반가운 제안은 조건과 기관을 확인하기 전에 수수료나 정보를 내게 만들 수 있습니다.",
                scamPressureGoal = "혜택을 놓칠 것처럼 느끼게 해 선입금·개인정보·인증 절차를 서두르게 하는 경우가 많습니다.",
                immediateSafeMove = "제안을 보류하고 금융회사나 기관의 공식 앱·대표 경로에서 대상 여부와 절차를 직접 확인하세요.",
                familyDiscussionPrompt = "예상하지 못한 혜택 연락을 받으면 어떤 공식 창구에서 먼저 확인할지 함께 정해 둘까요?",
                primaryAction = action(RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.OFFICIAL_HELP_DIRECTORY),
                    resultAction()
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.CREDENTIALS,
                displayOrder = 6,
                category = RiskSignalCategory.CREDENTIALS_AND_SECRETS,
                urgency = RiskSignalExplainerUrgency.STOP_NOW,
                label = ScamSignal.CREDENTIALS.label,
                whySuspicious = "비밀번호·인증번호·보안수단은 본인 확인과 거래 승인을 넘길 수 있는 비밀값이라 전화나 메시지로 요구하면 특히 주의해야 합니다.",
                scamPressureGoal = "계정에 들어가거나 거래를 승인하고 본인처럼 행동할 수 있는 정보를 얻으려는 경우가 많습니다.",
                immediateSafeMove = "어떤 값도 말하거나 보내지 말고, 이미 공유했다면 다른 안전한 기기에서 계정 보호 순서를 확인하세요.",
                familyDiscussionPrompt = "가족끼리도 비밀번호와 인증번호는 묻거나 대신 입력하지 않는 원칙을 확인할까요?",
                primaryAction = action(RiskSignalExplainerActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS),
                    action(RiskSignalExplainerActionId.OFFICIAL_HELP_DIRECTORY)
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.FAMILY_EMERGENCY,
                displayOrder = 7,
                category = RiskSignalCategory.FAMILY_IMPERSONATION,
                urgency = RiskSignalExplainerUrgency.VERIFY_FIRST,
                label = ScamSignal.FAMILY_EMERGENCY.label,
                whySuspicious = "가족의 사고나 위급함을 들으면 목소리와 상황을 차분히 확인하기 전에 도와주려는 마음이 앞설 수 있습니다.",
                scamPressureGoal = "가족을 돕는다는 마음을 이용해 콜백 확인 전에 송금이나 정보 제공을 하게 만드는 경우가 많습니다.",
                immediateSafeMove = "현재 연락을 끊고 새로 받은 번호가 아닌 평소 가족 번호로 직접 다시 전화해 확인하세요.",
                familyDiscussionPrompt = "위급한 연락이 와도 사용할 가족 안전 질문과 콜백 순서를 지금 함께 연습해 볼까요?",
                primaryAction = action(RiskSignalExplainerActionId.FAMILY_CALLBACK_CARD),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.FAMILY_ALERT_MESSAGES),
                    action(RiskSignalExplainerActionId.SAFETY_PLAN)
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.GIFT_CRYPTO,
                displayOrder = 8,
                category = RiskSignalCategory.GIFT_AND_CRYPTO,
                urgency = RiskSignalExplainerUrgency.STOP_NOW,
                label = ScamSignal.GIFT_CRYPTO.label,
                whySuspicious = "상품권 번호나 가상자산 전송은 전달 뒤 되돌리기 어렵고 일반적인 계좌 이체보다 확인 경로가 낯설 수 있습니다.",
                scamPressureGoal = "현금 대신 추적과 취소가 어려운 방식으로 빠르게 가치를 넘기게 하려는 경우가 많습니다.",
                immediateSafeMove = "구매·번호 전달·전송을 멈추고, 평소 가족 번호와 공식 상담 경로로 요청 사실을 확인하세요.",
                familyDiscussionPrompt = "상품권이나 가상자산을 요구받으면 금액과 관계없이 먼저 가족에게 확인하는 규칙을 정할까요?",
                primaryAction = action(RiskSignalExplainerActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS),
                    action(RiskSignalExplainerActionId.ACCOUNT_LOCKDOWN)
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.SECRECY,
                displayOrder = 9,
                category = RiskSignalCategory.URGENCY_MONEY_PRESSURE,
                urgency = RiskSignalExplainerUrgency.STOP_NOW,
                label = ScamSignal.SECRECY.label,
                whySuspicious = "가족이나 은행에 알리지 말라는 말은 다른 사람이 사실을 확인하고 개입할 기회를 줄이려는 압박일 수 있습니다.",
                scamPressureGoal = "사용자를 혼자 판단하게 만들어 송금·정보 제공·앱 설치를 중단 없이 이어가게 하려는 경우가 많습니다.",
                immediateSafeMove = "상대와의 대화를 멈추고 믿을 만한 가족이나 보호자 한 명에게 일반 상황부터 알리세요.",
                familyDiscussionPrompt = "‘아무에게도 말하지 말라’는 요청이 나오면 바로 가족에게 알리는 신호로 삼을까요?",
                primaryAction = action(RiskSignalExplainerActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.FAMILY_ALERT_MESSAGES),
                    action(RiskSignalExplainerActionId.TRUSTED_HELPER_HANDOFF)
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.PHONE_NUMBER_CHANGE,
                displayOrder = 10,
                category = RiskSignalCategory.FAMILY_IMPERSONATION,
                urgency = RiskSignalExplainerUrgency.VERIFY_FIRST,
                label = ScamSignal.PHONE_NUMBER_CHANGE.label,
                whySuspicious = "새 번호를 바로 저장하게 하면 평소 번호로 본인 여부를 확인하는 안전한 연결고리가 끊길 수 있습니다.",
                scamPressureGoal = "가족인 것처럼 대화를 이어가며 송금이나 인증정보 요청을 자연스럽게 받아들이게 하려는 경우가 많습니다.",
                immediateSafeMove = "새 번호를 저장하거나 답하기 전에 기존에 알고 있던 가족 번호로 직접 전화해 변경 사실을 확인하세요.",
                familyDiscussionPrompt = "번호가 바뀌면 예전 번호나 다른 가족을 통해 확인한다는 규칙을 정해 둘까요?",
                primaryAction = action(RiskSignalExplainerActionId.FAMILY_CALLBACK_CARD),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.FAMILY_ALERT_MESSAGES),
                    action(RiskSignalExplainerActionId.SAFETY_PLAN)
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.LOAN_REFINANCE_FEE,
                displayOrder = 11,
                category = RiskSignalCategory.FINANCIAL_BAIT,
                urgency = RiskSignalExplainerUrgency.VERIFY_FIRST,
                label = ScamSignal.LOAN_REFINANCE_FEE.label,
                whySuspicious = "낮은 금리로 바꿔준다는 제안 뒤에 보증료나 선입금을 먼저 요구하면 정상 절차인지 별도 확인이 필요합니다.",
                scamPressureGoal = "좋은 조건을 놓치기 전에 비용을 먼저 내야 한다며 송금을 재촉하는 경우가 많습니다.",
                immediateSafeMove = "선입금을 멈추고 기존 거래 금융회사와 공식 상담 경로에서 상품과 비용을 직접 확인하세요.",
                familyDiscussionPrompt = "대출 조건 변경 연락은 누구와 어떤 공식 경로로 함께 확인할지 정해 둘까요?",
                primaryAction = action(RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.OFFICIAL_HELP_DIRECTORY),
                    action(RiskSignalExplainerActionId.ACCOUNT_LOCKDOWN)
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.GIFTICON_PIN,
                displayOrder = 12,
                category = RiskSignalCategory.GIFT_AND_CRYPTO,
                urgency = RiskSignalExplainerUrgency.STOP_NOW,
                label = ScamSignal.GIFTICON_PIN.label,
                whySuspicious = "기프티콘이나 상품권의 번호·PIN은 사진이나 문자로 전달하는 순간 다른 사람이 사용할 수 있는 가치입니다.",
                scamPressureGoal = "구매한 상품권의 비밀 번호를 보내게 해 현금처럼 사용하려는 경우가 많습니다.",
                immediateSafeMove = "구매와 번호 전송을 멈추고, 이미 보냈다면 공식 판매처와 상담 경로에서 가능한 조치를 직접 확인하세요.",
                familyDiscussionPrompt = "가족이라도 상품권 번호나 PIN을 메시지로 요구하지 않는다는 약속을 해 둘까요?",
                primaryAction = action(RiskSignalExplainerActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS),
                    action(RiskSignalExplainerActionId.OFFICIAL_HELP_DIRECTORY)
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.LIVE_CALL_CONTROL,
                displayOrder = 13,
                category = RiskSignalCategory.APP_AND_REMOTE_CONTROL,
                urgency = RiskSignalExplainerUrgency.STOP_NOW,
                label = ScamSignal.LIVE_CALL_CONTROL.label,
                whySuspicious = "통화를 끊지 못하게 하거나 화면을 계속 공유하게 하면 다른 사람에게 확인할 시간과 기기 통제권을 잃기 쉽습니다.",
                scamPressureGoal = "상대의 지시에 따라 앱·계정·금융 행동을 계속 실행하고 확인 연락을 못 하게 하려는 경우가 많습니다.",
                immediateSafeMove = "통화와 화면공유를 끝내고 원격 연결을 끊은 뒤, 다른 안전한 기기에서 계정 보호와 증거 순서를 확인하세요.",
                familyDiscussionPrompt = "통화를 끊지 말라는 말을 들으면 즉시 종료하고 누구에게 도움을 요청할지 정해 둘까요?",
                primaryAction = action(RiskSignalExplainerActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.EVIDENCE_KIT),
                    action(RiskSignalExplainerActionId.TRUSTED_HELPER_HANDOFF)
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.DEEPFAKE_VOICE,
                displayOrder = 14,
                category = RiskSignalCategory.FAMILY_IMPERSONATION,
                urgency = RiskSignalExplainerUrgency.VERIFY_FIRST,
                label = ScamSignal.DEEPFAKE_VOICE.label,
                whySuspicious = "익숙한 목소리처럼 들려도 짧은 음성만으로 본인을 확정하기 어렵고, 합성이나 흉내 가능성을 별도로 확인해야 합니다.",
                scamPressureGoal = "목소리에 대한 신뢰를 이용해 안전 질문과 기존 번호 콜백을 건너뛰게 하려는 경우가 많습니다.",
                immediateSafeMove = "현재 통화를 끊고 기존 가족 번호로 다시 걸어 안전 질문과 다른 가족 경로로 함께 확인하세요.",
                familyDiscussionPrompt = "목소리만 믿지 않고 사용할 안전 질문과 두 번째 확인자를 함께 정해 둘까요?",
                primaryAction = action(RiskSignalExplainerActionId.FAMILY_CALLBACK_CARD),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS),
                    action(RiskSignalExplainerActionId.FAMILY_ALERT_MESSAGES)
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.SAFE_ACCOUNT_FRAUD,
                displayOrder = 15,
                category = RiskSignalCategory.FINANCIAL_BAIT,
                urgency = RiskSignalExplainerUrgency.STOP_NOW,
                label = ScamSignal.SAFE_ACCOUNT_FRAUD.label,
                whySuspicious = "명의도용이나 자금세탁 연루를 이유로 다른 계좌로 옮기라는 요청은 두려움을 이용해 이체를 정당화할 수 있습니다.",
                scamPressureGoal = "돈을 보호하거나 조사한다는 명목으로 상대가 지정한 계좌에 자금을 보내게 하려는 경우가 많습니다.",
                immediateSafeMove = "이체를 멈추고, 연락에서 받은 번호가 아닌 금융회사와 공식 상담 경로로 사실을 직접 확인하세요.",
                familyDiscussionPrompt = "‘안전한 계좌로 옮기라’는 말을 들으면 즉시 끊고 가족과 공식 기관에 확인하기로 할까요?",
                primaryAction = action(RiskSignalExplainerActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS),
                    action(RiskSignalExplainerActionId.OFFICIAL_HELP_DIRECTORY),
                    action(RiskSignalExplainerActionId.ACCOUNT_LOCKDOWN)
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.PARCEL_INVITATION_SMISHING,
                displayOrder = 16,
                category = RiskSignalCategory.LINKS_AND_SMISHING,
                urgency = RiskSignalExplainerUrgency.VERIFY_FIRST,
                label = ScamSignal.PARCEL_INVITATION_SMISHING.label,
                whySuspicious = "택배·청첩장·부고처럼 일상적인 알림은 내용을 빨리 확인하려는 마음을 이용해 링크 클릭을 유도할 수 있습니다.",
                scamPressureGoal = "익숙한 알림처럼 보이게 해 링크를 열고 정보 입력이나 파일 실행을 하게 만드는 경우가 많습니다.",
                immediateSafeMove = "문자 링크를 다시 열지 말고 배송사 공식 앱이나 알고 있던 발신자에게 다른 경로로 확인하세요.",
                familyDiscussionPrompt = "예상하지 못한 택배·초대·부고 링크는 가족 채팅에서도 바로 열지 않는 규칙을 만들까요?",
                primaryAction = action(RiskSignalExplainerActionId.EVIDENCE_KIT),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.FAMILY_ALERT_MESSAGES),
                    resultAction()
                )
            ),
            RiskSignalExplainerCard(
                id = ScamSignal.OVERSEAS_PAYMENT_ALERT,
                displayOrder = 17,
                category = RiskSignalCategory.FINANCIAL_BAIT,
                urgency = RiskSignalExplainerUrgency.VERIFY_FIRST,
                label = ScamSignal.OVERSEAS_PAYMENT_ALERT.label,
                whySuspicious = "낯선 결제 알림은 놀란 마음으로 문자 속 번호나 링크에 즉시 반응하게 만들 수 있습니다.",
                scamPressureGoal = "취소나 확인을 돕는 척하며 인증번호·카드정보를 받거나 다른 결제를 승인하게 하는 경우가 많습니다.",
                immediateSafeMove = "알림 속 링크와 번호를 쓰지 말고 카드 뒷면이나 평소 공식 앱에서 실제 거래를 직접 확인하세요.",
                familyDiscussionPrompt = "낯선 결제 알림이 오면 어떤 공식 경로에서 먼저 거래를 확인할지 함께 정할까요?",
                primaryAction = action(RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS),
                secondaryActions = listOf(
                    action(RiskSignalExplainerActionId.ACCOUNT_LOCKDOWN),
                    action(RiskSignalExplainerActionId.OFFICIAL_HELP_DIRECTORY)
                )
            )
        )
    }

    private fun recommendSignalIds(matchedSignals: Set<ScamSignal>): List<ScamSignal> {
        if (matchedSignals.isEmpty()) return defaultSignalIds

        val recommended = linkedSetOf<ScamSignal>()
        if (matchedSignals.any { signal -> signal in familySignals }) {
            recommended += ScamSignal.FAMILY_EMERGENCY
            recommended += ScamSignal.PHONE_NUMBER_CHANGE
            recommended += ScamSignal.DEEPFAKE_VOICE
        }
        if (matchedSignals.any { signal -> signal in financialSignals }) {
            recommended += ScamSignal.URGENT_TRANSFER
            recommended += ScamSignal.CREDENTIALS
            ScamSignal.entries
                .filter { signal -> signal in matchedSignals && signal in financialSignals }
                .forEach { signal -> recommended += signal }
        }
        if (matchedSignals.any { signal -> signal in remoteSignals }) {
            recommended += ScamSignal.APP_INSTALL
            recommended += ScamSignal.LIVE_CALL_CONTROL
        }
        if (matchedSignals.any { signal -> signal in linkSignals }) {
            recommended += ScamSignal.LINK
            recommended += ScamSignal.PARCEL_INVITATION_SMISHING
        }
        ScamSignal.entries
            .filter { signal -> signal in matchedSignals }
            .forEach { signal -> recommended += signal }

        return recommended.toList()
    }

    private fun recommendActionIds(
        analysis: ScamAnalysis?,
        matchedSignals: Set<ScamSignal>,
        readiness: FamilyReadiness
    ): List<RiskSignalExplainerActionId> {
        if (matchedSignals.isEmpty()) {
            return buildList {
                if (analysis?.riskLevel == RiskLevel.HIGH) {
                    add(RiskSignalExplainerActionId.QUICK_STOP)
                    add(RiskSignalExplainerActionId.RECOVERY_PLAN)
                }
                add(RiskSignalExplainerActionId.CHECKER)
                if (analysis?.riskLevel != RiskLevel.HIGH) {
                    add(RiskSignalExplainerActionId.QUICK_STOP)
                }
                add(RiskSignalExplainerActionId.FAMILY_CALLBACK_CARD)
                if (readiness.level != ReadinessLevel.READY) {
                    add(RiskSignalExplainerActionId.SAFETY_PLAN)
                }
            }
        }

        val recommended = linkedSetOf<RiskSignalExplainerActionId>()
        if (analysis?.riskLevel == RiskLevel.HIGH) {
            recommended += RiskSignalExplainerActionId.QUICK_STOP
            recommended += RiskSignalExplainerActionId.RECOVERY_PLAN
        }
        if (matchedSignals.any { signal -> signal in familySignals }) {
            recommended += RiskSignalExplainerActionId.FAMILY_CALLBACK_CARD
            recommended += RiskSignalExplainerActionId.FAMILY_ALERT_MESSAGES
            if (readiness.level != ReadinessLevel.READY) {
                recommended += RiskSignalExplainerActionId.SAFETY_PLAN
            }
        }
        if (matchedSignals.any { signal -> signal in financialSignals }) {
            recommended += RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS
            recommended += RiskSignalExplainerActionId.ACCOUNT_LOCKDOWN
            recommended += RiskSignalExplainerActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (matchedSignals.any { signal -> signal in remoteSignals }) {
            recommended += RiskSignalExplainerActionId.ACCOUNT_LOCKDOWN
            recommended += RiskSignalExplainerActionId.EVIDENCE_KIT
            recommended += RiskSignalExplainerActionId.TRUSTED_HELPER_HANDOFF
        }
        if (matchedSignals.any { signal -> signal in linkSignals }) {
            recommended += RiskSignalExplainerActionId.EVIDENCE_KIT
            recommended += RiskSignalExplainerActionId.FAMILY_ALERT_MESSAGES
            recommended += RiskSignalExplainerActionId.ANALYSIS_RESULT
        }
        if (matchedSignals.any { signal -> signal in pressureSignals }) {
            recommended += RiskSignalExplainerActionId.QUICK_STOP
            recommended += RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS
        }
        if (recommended.isEmpty()) {
            recommended += RiskSignalExplainerActionId.ANALYSIS_RESULT
            recommended += RiskSignalExplainerActionId.SAFETY_FLOW_ROUTER
        }

        return recommended.toList()
    }

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        matchedSignals: Set<ScamSignal>,
        readiness: FamilyReadiness
    ): String {
        val readinessCopy = when (readiness.level) {
            ReadinessLevel.LOW -> "가족 콜백 준비가 아직 부족해도 괜찮습니다. 안전 계획을 차분히 시작할 수 있습니다."
            ReadinessLevel.IN_PROGRESS -> "가족 콜백 준비가 진행 중입니다. 빠진 안전 계획을 차분히 보완할 수 있습니다."
            ReadinessLevel.READY -> "저장된 가족 콜백 루틴이 준비되어 있으니 평소 번호와 안전 질문으로 확인하세요."
        }
        if (matchedSignals.isEmpty()) {
            val resultCopy = if (analysis == null) {
                "현재 분석이 없어 송금·인증정보·링크·가족 사칭의 기본 신호를 먼저 보여드립니다."
            } else {
                "강하게 일치한 신호가 없어도 안전을 확정할 수는 없습니다. 기본 신호를 살펴보고 다른 의심 내용이 있으면 직접 다시 분석하세요."
            }
            return "$resultCopy 대화를 멈추고 직접 분석하는 경로부터 확인하세요. $readinessCopy"
        }

        val family = matchedSignals.any { signal -> signal in familySignals }
        val financial = matchedSignals.any { signal -> signal in financialSignals }
        val remote = matchedSignals.any { signal -> signal in remoteSignals }
        val link = matchedSignals.any { signal -> signal in linkSignals }
        val situationCopy = buildList {
            if (family) add("가족·목소리·번호 변경 신호는 기존 번호 콜백과 가족 알림을 먼저 확인하세요.")
            if (financial) add("송금·인증·금융 미끼 신호는 안전 확인 전화, 계정 보호, 공식 도움 경로를 확인하세요.")
            if (remote) add("앱·원격제어 신호는 연결 중단 뒤 계정 보호, 증거 보존, 믿을 만한 도우미 경로를 확인하세요.")
            if (link) add("링크·스미싱 신호는 다시 열지 말고 증거 보존, 가족 알림, 현재 결과를 확인하세요.")
            if (isEmpty()) add("현재 일치 신호와 가까운 설명을 읽고 기존 안전 흐름에서 다음 행동을 직접 고르세요.")
        }.joinToString(" ")

        return "$situationCopy $readinessCopy"
    }

    private fun actionFor(
        id: RiskSignalExplainerActionId,
        hasAnalysis: Boolean,
        readinessLevel: ReadinessLevel
    ): RiskSignalExplainerAction = RiskSignalExplainerAction(
        id = id,
        label = when (id) {
            RiskSignalExplainerActionId.QUICK_STOP -> "긴급 멈춤 모드"
            RiskSignalExplainerActionId.CHECKER -> if (hasAnalysis) {
                "다른 의심 내용 직접 분석하기"
            } else {
                "의심 내용 직접 분석하기"
            }
            RiskSignalExplainerActionId.ANALYSIS_RESULT -> "현재 분석 결과 보기"
            RiskSignalExplainerActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 확인 카드"
            RiskSignalExplainerActionId.SAFETY_PLAN -> when (readinessLevel) {
                ReadinessLevel.LOW -> "가족 안전 계획 만들기"
                ReadinessLevel.IN_PROGRESS -> "가족 안전 계획 보완하기"
                ReadinessLevel.READY -> "가족 안전 계획 확인하기"
            }
            RiskSignalExplainerActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
            RiskSignalExplainerActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
            RiskSignalExplainerActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            RiskSignalExplainerActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처 보기"
            RiskSignalExplainerActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
            RiskSignalExplainerActionId.TRUSTED_HELPER_HANDOFF -> "보호자 도움 요청 카드"
            RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS -> "안전 확인 전화 스크립트"
            RiskSignalExplainerActionId.RECOVERY_PLAN -> "24시간 수습 계획"
        }
    )

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 화면은 사용자가 직접 여는 수동 교육 안내이며 문자·통화·연락처를 자동으로 읽거나 감시하지 않습니다.",
        "사용자가 직접 붙여넣거나 텍스트로 공유한 내용의 일치 신호만 기기 안에서 로컬 규칙으로 정리하며 네트워크 분석이나 업로드를 하지 않습니다.",
        "카드 선택, 읽은 상태, 공유 이력, 진행 상태를 저장하지 않으며 화면을 여는 것만으로 사건 기록을 추가·수정·삭제하지 않습니다.",
        "공유 버튼은 연락처를 읽거나 받는 사람을 고르지 않습니다. 사용자가 직접 공유 앱과 대상을 고르며 앱이 자동으로 보내지 않고 이 화면은 전화를 자동으로 걸지 않습니다.",
        "일치 신호는 참고용 교육 안내이며 사기 여부를 확정하거나 공식 판단을 대신하지 않습니다.",
        "앱은 통화·문자를 차단하거나 악성 앱을 제거하지 않고, 계좌·카드를 정지하거나 피해금 회수와 법률·안전 결과를 보장하지 않습니다.",
        "이 화면은 새 Android 권한이나 네트워크 호출을 추가하지 않습니다. 저장된 가족 안전 계획에서는 값 없는 준비 상태만 계산하며 비밀번호·인증번호·안전 질문·연락처 값을 카드나 공유문에 표시하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendationTitle: String,
        recommendedSignalIds: List<ScamSignal>,
        recommendedActions: List<RiskSignalExplainerAction>,
        cards: List<RiskSignalExplainerCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        val allActions = cards
            .flatMap { card -> listOf(card.primaryAction) + card.secondaryActions }
            .distinctBy { action -> action.label }

        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 위험 신호 설명")
            appendLine()
            appendLine(recommendationTitle)
            recommendedSignalIds.forEach { signalId ->
                val card = cardsById.getValue(signalId)
                appendLine("- [${card.category.label}] ${card.label}")
            }
            appendLine()
            appendLine("먼저 이어갈 앱 행동")
            recommendedActions.forEach { action ->
                appendLine("- ${action.label}")
            }
            appendLine()
            appendLine("모든 신호 분류")
            cards.forEach { card ->
                appendLine("- [${card.category.label}] ${card.label}")
            }
            appendLine()
            appendLine("선택 가능한 앱 행동")
            allActions.forEach { action ->
                appendLine("- ${action.label}")
            }
            appendLine()
            appendLine("함께 지킬 일반 안전 행동")
            appendLine("- 대화를 멈추고 돈·비밀정보·앱 요청을 보류합니다.")
            appendLine("- 새 번호나 메시지 속 경로 대신 평소 가족 번호와 공식 경로로 직접 확인합니다.")
            appendLine("- 링크는 다시 열지 않고, 이미 한 행동이 있다면 기존 보호·증거·공식 도움 화면을 확인합니다.")
            appendLine()
            appendLine("이 설명은 로컬 규칙의 교육용 안내이며 카드 선택·읽음·공유 이력을 저장하지 않습니다.")
            appendLine("앱은 연락처를 읽거나 받는 사람을 고르지 않고 자동으로 보내지 않습니다.")
            append(
                "붙여넣은 원문·화면 단서·안전 질문·콜백 내용·연락처 값과 전화·계좌·비밀번호·" +
                    "인증번호·PIN·링크·도메인·신분정보 값은 이 공유문에 포함하지 않았습니다."
            )
        }.trim()
    }
}
