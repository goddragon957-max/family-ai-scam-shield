package com.mongmong.familyshield

enum class SafeCallScriptId {
    FAMILY_EXISTING_NUMBER,
    BANK_CARD_VERIFICATION,
    OFFICIAL_HOTLINE,
    CARRIER_ACCOUNT_SUPPORT,
    DEVICE_REMOTE_CONTROL_HELP,
    EVIDENCE_REPORT_PREP,
    PREVENTION_CHECKIN
}

enum class SafeCallScriptActionId {
    QUICK_STOP,
    CHECKER,
    ANALYSIS_RESULT,
    FAMILY_CALLBACK_CARD,
    SAFETY_PLAN,
    TRUSTED_HELPER_HANDOFF,
    RECOVERY_PLAN,
    SAFETY_FLOW_ROUTER,
    RESPONSE_CENTER,
    ACCOUNT_LOCKDOWN,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    FAMILY_ALERT_MESSAGES,
    NEW_ANALYSIS
}

enum class SafeCallScriptUrgency(val rank: Int, val label: String) {
    VERIFY_FIRST(0, "먼저 확인"),
    OFFICIAL_ROUTE(1, "공식 경로"),
    CLEANUP(2, "정리·신고 도움"),
    PREVENT(3, "예방·점검")
}

data class SafeCallScriptAction(
    val id: SafeCallScriptActionId,
    val label: String
)

data class SafeCallScriptCard(
    val id: SafeCallScriptId,
    val displayOrder: Int,
    val urgency: SafeCallScriptUrgency,
    val label: String,
    val whoToCall: String,
    val whenToUse: String,
    val openingScript: String,
    val questionsToAsk: List<String>,
    val neverSayNotes: List<String>,
    val primaryAction: SafeCallScriptAction,
    val secondaryActions: List<SafeCallScriptAction> = emptyList()
) {
    val primaryActionLabel: String
        get() = primaryAction.label

    val secondaryActionLabels: List<String>
        get() = secondaryActions.map { action -> action.label }
}

data class SafeVerificationCallScripts(
    val title: String,
    val intro: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedScriptIds: List<SafeCallScriptId>,
    val cards: List<SafeCallScriptCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<SafeCallScriptCard>
        get() = recommendedScriptIds.mapNotNull { scriptId ->
            cards.find { card -> card.id == scriptId }
        }

    val recommendedActionIds: List<SafeCallScriptActionId>
        get() = recommendedCards
            .flatMap { card -> listOf(card.primaryAction) + card.secondaryActions }
            .map { action -> action.id }
            .distinct()
}

object SafeVerificationCallScriptsBuilder {
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

    fun build(analysis: ScamAnalysis?, readiness: FamilyReadiness): SafeVerificationCallScripts {
        val hasAnalysis = analysis != null
        val cards = buildCards(hasAnalysis)
        val recommendedScriptIds = recommendScriptIds(analysis, readiness)
        val recommendationTitle = if (analysis == null) {
            "분석 전 기본 추천"
        } else {
            "현재 상황 기준 추천 전화"
        }

        return SafeVerificationCallScripts(
            title = "안전 확인 전화 스크립트",
            intro = "직접 전화로 확인할 때 무엇을 물어보고 무엇을 말하지 말아야 할지 정리했습니다. " +
                "이 앱은 대신 전화를 걸지 않으며, 통화는 항상 사용자가 직접 겁니다.",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(analysis, readiness),
            recommendedScriptIds = recommendedScriptIds,
            cards = cards,
            privacySafetyNotes = privacySafetyNotes(),
            familyShareSummary = buildFamilyShareSummary(
                recommendationTitle = recommendationTitle,
                recommendedScriptIds = recommendedScriptIds,
                cards = cards
            )
        )
    }

    private fun buildCards(hasAnalysis: Boolean): List<SafeCallScriptCard> = listOf(
        SafeCallScriptCard(
            id = SafeCallScriptId.FAMILY_EXISTING_NUMBER,
            displayOrder = 1,
            urgency = SafeCallScriptUrgency.VERIFY_FIRST,
            label = "가족 기존 번호 확인 전화",
            whoToCall = "가족 안전 계획에 미리 저장해 둔 기존 번호",
            whenToUse = "가족을 사칭하거나 번호가 바뀌었다는 연락, 목소리가 이상한 통화를 받았을 때",
            openingScript = "저장된 번호로 다시 걸었어. 방금 온 연락이 진짜 너 맞는지 확인하고 싶어서 전화했어.",
            questionsToAsk = listOf(
                "가족 안전 계획에 저장한 안전 질문에 답해줄 수 있어?",
                "방금 그 연락을 네가 진짜 보낸 게 맞아?",
                "혹시 최근에 번호가 바뀌었거나 새 번호를 알려준 적 있어?"
            ),
            neverSayNotes = listOf(
                "계좌 비밀번호나 인증번호(OTP)는 이 통화에서도 말하지 않아도 됩니다.",
                "안전 질문의 답이나 콜백 스크립트 내용을 새로 온 번호에는 절대 알려주지 마세요."
            ),
            primaryAction = SafeCallScriptAction(SafeCallScriptActionId.FAMILY_CALLBACK_CARD, "가족 콜백 확인 카드"),
            secondaryActions = listOf(
                SafeCallScriptAction(SafeCallScriptActionId.SAFETY_PLAN, "가족 안전 계획 확인"),
                SafeCallScriptAction(SafeCallScriptActionId.FAMILY_ALERT_MESSAGES, "가족 경고 메시지 팩"),
                if (hasAnalysis) {
                    SafeCallScriptAction(SafeCallScriptActionId.ANALYSIS_RESULT, "현재 분석 결과 보기")
                } else {
                    SafeCallScriptAction(SafeCallScriptActionId.CHECKER, "의심 내용 직접 분석하기")
                }
            )
        ),
        SafeCallScriptCard(
            id = SafeCallScriptId.BANK_CARD_VERIFICATION,
            displayOrder = 2,
            urgency = SafeCallScriptUrgency.OFFICIAL_ROUTE,
            label = "은행·카드사 확인 전화",
            whoToCall = "카드 뒷면이나 은행 공식 앱·공식 웹사이트에 적힌 대표번호",
            whenToUse = "의심스러운 이체, 카드 결제, 대출 안내 문자를 받았을 때",
            openingScript = "방금 이런 문자를 받았는데, 제 계좌·카드에 실제로 이런 거래나 요청이 있었는지 확인하고 싶어서 전화했습니다.",
            questionsToAsk = listOf(
                "방금 받은 이체/결제/대출 안내가 실제로 은행·카드사에서 보낸 것이 맞나요?",
                "의심스러운 거래가 있다면 다음에 제가 공식적으로 밟아야 할 절차가 무엇인가요?",
                "이 통화를 은행 공식 상담 기록으로 남겨주실 수 있나요?"
            ),
            neverSayNotes = listOf(
                "계좌 비밀번호나 보안카드 번호, 인증번호(OTP)는 상담원에게도 전화로 부르지 마세요.",
                "문자 속 링크나 상대가 알려준 번호가 아니라, 카드 뒷면·공식 앱의 번호로만 거세요."
            ),
            primaryAction = SafeCallScriptAction(SafeCallScriptActionId.OFFICIAL_HELP_DIRECTORY, "신고·상담 연락처"),
            secondaryActions = listOf(
                SafeCallScriptAction(SafeCallScriptActionId.ACCOUNT_LOCKDOWN, "계정·기기 잠금 체크리스트"),
                SafeCallScriptAction(SafeCallScriptActionId.REPORT_PREP, "신고용 상담 준비 카드")
            )
        ),
        SafeCallScriptCard(
            id = SafeCallScriptId.OFFICIAL_HOTLINE,
            displayOrder = 3,
            urgency = SafeCallScriptUrgency.OFFICIAL_ROUTE,
            label = "1394/112/1332 상담 전화",
            whoToCall = "112 경찰, 1394 보이스피싱 통합신고·상담, 1332 금융감독원",
            whenToUse = "상황이 사기인지 헷갈리거나, 어느 기관에 연락해야 할지 모를 때",
            openingScript = "보이스피싱이 의심되는 연락을 받았는데, 지금 제 상황에 어떤 절차를 밟아야 하는지 여쭤보려고 전화했습니다.",
            questionsToAsk = listOf(
                "지금 상황이 보이스피싱이 맞다면 제가 바로 해야 할 조치가 무엇인가요?",
                "이 상황은 112, 1394, 1332 중 어디에서 계속 상담해야 하나요?",
                "신고 접수 번호나 상담 시각을 남겨주실 수 있나요?"
            ),
            neverSayNotes = listOf(
                "상담원이 계좌 비밀번호나 인증번호를 요구하면 그 통화는 즉시 끊고 대표번호로 다시 거세요.",
                "실제 피해 여부와 상관없이, 상담 중 계좌 전체 번호나 안전단어를 먼저 말하지 마세요."
            ),
            primaryAction = SafeCallScriptAction(SafeCallScriptActionId.OFFICIAL_HELP_DIRECTORY, "신고·상담 연락처"),
            secondaryActions = listOf(
                SafeCallScriptAction(SafeCallScriptActionId.REPORT_PREP, "신고용 상담 준비 카드"),
                SafeCallScriptAction(SafeCallScriptActionId.RESPONSE_CENTER, "긴급 대응 센터")
            )
        ),
        SafeCallScriptCard(
            id = SafeCallScriptId.CARRIER_ACCOUNT_SUPPORT,
            displayOrder = 4,
            urgency = SafeCallScriptUrgency.OFFICIAL_ROUTE,
            label = "통신사·계정 지원 확인 전화",
            whoToCall = "통신사 공식 고객센터 또는 메신저·이메일 계정의 공식 지원 채널",
            whenToUse = "유심(SIM), 전화번호, 계정이 탈취되거나 도용된 것 같을 때",
            openingScript = "제 번호나 계정에 이상한 로그인이나 유심 변경 시도가 있었는지 확인하고 싶어서 전화했습니다.",
            questionsToAsk = listOf(
                "최근에 제 명의로 유심 재발급이나 번호 변경 요청이 접수된 적이 있나요?",
                "계정에 낯선 기기 로그인이나 비밀번호 변경 요청이 있었나요?",
                "지금 제가 공식적으로 확인하거나 잠글 수 있는 절차가 무엇인가요?"
            ),
            neverSayNotes = listOf(
                "상담원에게도 기존 비밀번호나 인증번호를 그대로 불러주지 마세요. 공식 절차의 본인 확인 방법만 따르세요.",
                "문자로 온 인증 링크가 아니라 공식 앱·웹사이트에서 직접 로그인해 확인하세요."
            ),
            primaryAction = SafeCallScriptAction(SafeCallScriptActionId.ACCOUNT_LOCKDOWN, "계정·기기 잠금 체크리스트"),
            secondaryActions = listOf(
                SafeCallScriptAction(SafeCallScriptActionId.OFFICIAL_HELP_DIRECTORY, "신고·상담 연락처"),
                SafeCallScriptAction(SafeCallScriptActionId.EVIDENCE_KIT, "증거 보존 체크리스트")
            )
        ),
        SafeCallScriptCard(
            id = SafeCallScriptId.DEVICE_REMOTE_CONTROL_HELP,
            displayOrder = 5,
            urgency = SafeCallScriptUrgency.CLEANUP,
            label = "기기·원격제어 점검 도움 전화",
            whoToCall = "함께 있어줄 가족이나 기기에 익숙한 믿을 만한 지인",
            whenToUse = "원격제어 앱, 화면공유, 출처 불명 앱 설치를 요구받았거나 이미 허용했을 때",
            openingScript = "지금 원격제어·화면공유 관련 요청을 받아서 그런데, 옆에서 같이 기기 상태를 봐줄 수 있어?",
            questionsToAsk = listOf(
                "지금 화면공유나 원격제어 연결이 남아 있는지 같이 확인해 줄 수 있어?",
                "의심되는 앱이 설치되어 있으면 같이 찾아봐 줄 수 있어?",
                "계정 비밀번호는 내가 직접 바꿀 테니 옆에서 절차만 같이 봐줄 수 있어?"
            ),
            neverSayNotes = listOf(
                "도와주는 사람에게도 비밀번호나 인증번호를 대신 입력해 달라고 하지 마세요.",
                "이 통화나 앱이 원격 연결을 대신 끊거나 악성 앱을 자동으로 제거해주지 않습니다. 연결 차단은 기기 설정에서 직접 하세요."
            ),
            primaryAction = SafeCallScriptAction(SafeCallScriptActionId.TRUSTED_HELPER_HANDOFF, "보호자 도움 요청 카드"),
            secondaryActions = listOf(
                SafeCallScriptAction(SafeCallScriptActionId.ACCOUNT_LOCKDOWN, "계정·기기 잠금 체크리스트"),
                SafeCallScriptAction(SafeCallScriptActionId.EVIDENCE_KIT, "증거 보존 체크리스트")
            )
        ),
        SafeCallScriptCard(
            id = SafeCallScriptId.EVIDENCE_REPORT_PREP,
            displayOrder = 6,
            urgency = SafeCallScriptUrgency.CLEANUP,
            label = "증거·신고 준비 전화",
            whoToCall = "같이 화면과 시간을 정리해 줄 가족·지인, 또는 신고 전 상담원",
            whenToUse = "메시지나 통화 내용을 지우기 전에, 신고·상담을 준비하고 싶을 때",
            openingScript = "신고하기 전에 화면이랑 시간을 같이 정리해줄 수 있어? 아직 아무것도 지우지 않았어.",
            questionsToAsk = listOf(
                "지금 남아있는 메시지·통화·거래 화면을 지우지 말고 같이 정리해 줄 수 있어?",
                "신고 전 상담에서 어떤 내용을 준비해서 말하면 좋을지 같이 봐줄 수 있어?",
                "신고 접수 후 받은 접수번호나 상담 시각을 같이 기록해 줄 수 있어?"
            ),
            neverSayNotes = listOf(
                "정리를 돕는 사람에게 실제 계좌·비밀번호·인증번호 값을 그대로 불러주지 마세요.",
                "증거를 확인한다는 이유로 의심 링크나 앱을 다시 열어보라고 하지 마세요."
            ),
            primaryAction = SafeCallScriptAction(SafeCallScriptActionId.EVIDENCE_KIT, "증거 보존 체크리스트"),
            secondaryActions = listOf(
                SafeCallScriptAction(SafeCallScriptActionId.REPORT_PREP, "신고용 상담 준비 카드"),
                SafeCallScriptAction(SafeCallScriptActionId.FAMILY_ALERT_MESSAGES, "가족 경고 메시지 팩"),
                if (hasAnalysis) {
                    SafeCallScriptAction(SafeCallScriptActionId.ANALYSIS_RESULT, "현재 분석 결과 보기")
                } else {
                    SafeCallScriptAction(SafeCallScriptActionId.CHECKER, "의심 내용 직접 분석하기")
                }
            )
        ),
        SafeCallScriptCard(
            id = SafeCallScriptId.PREVENTION_CHECKIN,
            displayOrder = 7,
            urgency = SafeCallScriptUrgency.PREVENT,
            label = "예방 점검 전화",
            whoToCall = "이번 주 안전 계획을 같이 점검할 가족",
            whenToUse = "특별한 사건이 없어도, 매주 한 번 가족 콜백 루틴과 안전 계획을 점검하고 싶을 때",
            openingScript = "특별한 일은 아니고, 이번 주 우리 가족 안전 계획이랑 콜백 번호 한번 같이 확인해보려고 전화했어.",
            questionsToAsk = listOf(
                "우리 가족 안전 질문이랑 콜백 번호가 지금도 맞는지 같이 확인해볼까?",
                "최근에 이상한 문자나 전화 받은 적 있어?",
                "다음 주에도 이 시간에 한 번 더 점검해볼까?"
            ),
            neverSayNotes = listOf(
                "점검 통화라도 안전 질문의 답이나 콜백 스크립트 내용을 문자로 미리 보내지 마세요.",
                "점검을 이유로 서로의 비밀번호나 인증번호를 공유하지 마세요."
            ),
            primaryAction = SafeCallScriptAction(SafeCallScriptActionId.RECOVERY_PLAN, "24시간 수습 계획"),
            secondaryActions = listOf(
                SafeCallScriptAction(SafeCallScriptActionId.SAFETY_FLOW_ROUTER, "안전 흐름 길잡이"),
                SafeCallScriptAction(SafeCallScriptActionId.FAMILY_ALERT_MESSAGES, "가족 경고 메시지 팩")
            )
        )
    )

    private fun recommendScriptIds(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness
    ): List<SafeCallScriptId> {
        if (analysis == null) {
            return listOf(
                SafeCallScriptId.FAMILY_EXISTING_NUMBER,
                SafeCallScriptId.PREVENTION_CHECKIN
            )
        }

        val signals = analysis.matchedSignals.toSet()
        val hasFamilySignal = signals.any { signal -> signal in familySignals }
        val hasFinancialSignal = signals.any { signal -> signal in financialCredentialSignals }
        val hasRemoteSignal = signals.any { signal -> signal in remoteSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val recommended = linkedSetOf<SafeCallScriptId>()

        if (hasFamilySignal) {
            recommended += SafeCallScriptId.FAMILY_EXISTING_NUMBER
        }
        if (hasFinancialSignal) {
            recommended += SafeCallScriptId.BANK_CARD_VERIFICATION
            recommended += SafeCallScriptId.OFFICIAL_HOTLINE
        }
        if (hasRemoteSignal) {
            recommended += SafeCallScriptId.DEVICE_REMOTE_CONTROL_HELP
            recommended += SafeCallScriptId.EVIDENCE_REPORT_PREP
        }
        if (hasLinkSignal) {
            recommended += SafeCallScriptId.EVIDENCE_REPORT_PREP
            recommended += SafeCallScriptId.OFFICIAL_HOTLINE
        }
        if (readiness.level != ReadinessLevel.READY) {
            recommended += SafeCallScriptId.FAMILY_EXISTING_NUMBER
        }
        if (recommended.isEmpty()) {
            recommended += SafeCallScriptId.FAMILY_EXISTING_NUMBER
            recommended += SafeCallScriptId.PREVENTION_CHECKIN
        }

        return recommended.toList()
    }

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness
    ): String {
        val readinessSuffix = " ${readinessMessage(readiness.level)}"
        if (analysis == null) {
            return "먼저 가족 기존 번호로 확인 전화를 걸고, 의심 내용을 새로 분석하거나 예방 점검 전화로 안전 계획을 " +
                "확인하세요.$readinessSuffix"
        }

        val signals = analysis.matchedSignals.toSet()
        val hasFamilySignal = signals.any { signal -> signal in familySignals }
        val hasFinancialSignal = signals.any { signal -> signal in financialCredentialSignals }
        val hasRemoteSignal = signals.any { signal -> signal in remoteSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val situationCopy = when {
            hasFamilySignal && (hasFinancialSignal || hasRemoteSignal) ->
                "가족·목소리·번호 변경 신호가 있어 가족 기존 번호 확인 전화를 가장 먼저 걸고, " +
                    "실제로 돈이나 정보를 요구받았다면 은행·카드사 확인 전화와 1394/112/1332 상담 전화도 이어서 진행하세요."
            hasFamilySignal ->
                "가족·목소리·번호 변경 신호가 있어 가족 기존 번호 확인 전화를 가장 먼저 거세요. 새로 온 번호로는 절대 확인하지 마세요."
            hasFinancialSignal ->
                "송금·인증정보 관련 신호가 있어 은행·카드사 확인 전화와 1394/112/1332 상담 전화를 함께 진행하세요."
            hasRemoteSignal ->
                "원격제어·앱 설치 신호가 있어 기기·원격제어 점검 도움 전화와 증거·신고 준비 전화를 함께 진행하세요."
            hasLinkSignal ->
                "링크·스미싱 신호가 있어 증거·신고 준비 전화와 1394/112/1332 상담 전화를 진행하고, " +
                    "가족에게도 같은 링크를 조심하라고 알리세요."
            else ->
                "뚜렷한 위험 신호는 적지만, 가족 기존 번호 확인 전화로 상황을 알리고 함께 확인하세요."
        }
        return situationCopy + readinessSuffix
    }

    private fun readinessMessage(level: ReadinessLevel): String = when (level) {
        ReadinessLevel.LOW ->
            "가족 콜백 준비가 부족하니 가족 기존 번호 확인 전화를 걸며 안전 계획부터 채우세요."
        ReadinessLevel.IN_PROGRESS ->
            "가족 콜백 준비가 진행 중이니 가족 기존 번호 확인 전화와 함께 남은 항목을 보완하세요."
        ReadinessLevel.READY ->
            "저장된 가족 콜백 루틴이 준비되어 있으니 예방 점검 전화로 그대로 점검만 하세요."
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 화면은 사용자가 직접 여는 수동 안내이며 문자·통화·연락처를 자동으로 읽거나 감시하지 않습니다.",
        "현재 분석은 사용자가 직접 붙여넣거나 텍스트로 공유한 내용만 기기 안에서 판단하며 네트워크로 업로드하지 않습니다.",
        "어떤 전화 스크립트를 봤는지, 통화 여부나 진행 상황을 저장하지 않으며, 이 화면을 여는 것만으로 사건 기록을 " +
            "추가·수정·삭제하지 않습니다.",
        "이 화면은 전화를 자동으로 걸지 않고 메시지를 자동으로 보내지 않습니다. 전화번호가 있는 버튼은 각 화면의 " +
            "기존 ACTION_DIAL 연결만 열며 통화는 사용자가 직접 시작합니다.",
        "공유 버튼은 연락처를 읽거나 받는 사람을 고르지 않고 자동으로 보내지 않습니다. 사용자가 공유 앱과 대상을 직접 선택합니다.",
        "전화로는 인증번호·비밀번호·계좌 전체 번호·안전단어·콜백 내용을 말하지 마세요. 확인된 공식 절차에서 꼭 필요한 " +
            "경우가 아니라면 알려주지 않아도 되며, 이 앱은 이런 값을 수집하거나 화면에 표시하지 않습니다.",
        "이 화면은 전화·문자를 자동으로 차단하거나 악성 앱을 자동으로 제거하지 않으며, 계좌를 자동으로 지급정지하거나 " +
            "피해금 회수·법률 자문을 보장하지 않습니다.",
        "이 화면은 기존 화면으로 연결할 뿐이며 새 Android 권한이나 네트워크 호출을 사용하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendationTitle: String,
        recommendedScriptIds: List<SafeCallScriptId>,
        cards: List<SafeCallScriptCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }

        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 안전 확인 전화 스크립트")
            appendLine()
            appendLine(recommendationTitle)
            recommendedScriptIds.forEach { scriptId ->
                val card = cardsById.getValue(scriptId)
                appendLine("- ${card.label}: ${actionLabels(card)}")
            }
            appendLine()
            appendLine("전체 전화 스크립트 목록")
            cards.forEach { card ->
                appendLine("- ${card.label}: ${actionLabels(card)}")
            }
            appendLine()
            appendLine("이 요약은 제가 직접 눌러 공유하며 앱은 전화를 대신 걸거나 메시지를 자동으로 보내지 않습니다.")
            append(
                "붙여넣은 원문·화면 단서·안전 질문·콜백 내용·연락처 값과 전화·계좌·비밀번호·" +
                    "인증번호·PIN·링크·신분정보 값은 이 요약에 포함하지 않았습니다."
            )
        }.trim()
    }

    private fun actionLabels(card: SafeCallScriptCard): String =
        (listOf(card.primaryAction) + card.secondaryActions)
            .joinToString(" / ") { action -> action.label }
}
