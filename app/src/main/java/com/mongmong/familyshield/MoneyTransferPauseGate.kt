package com.mongmong.familyshield

enum class MoneyTransferPauseCardId(val displayOrder: Int) {
    PAYMENT_PAUSE(1),
    SAFE_ACCOUNT_REFUND_LOAN_FEE(2),
    GIFT_CARD_CRYPTO_PAUSE(3),
    FINANCIAL_CREDENTIALS_REFUSAL(4),
    SEPARATE_FAMILY_OFFICIAL_VERIFICATION(5),
    EVIDENCE_AND_OFFICIAL_HELP(6)
}

enum class MoneyTransferPauseActionId {
    QUICK_STOP,
    ACTIVE_CONTACT_EXIT_SCRIPTS,
    CHECKER,
    ANALYSIS_RESULT,
    FAMILY_CALLBACK_CARD,
    SAFE_CALL_SCRIPTS,
    FAMILY_ALERT_MESSAGES,
    TRUSTED_HELPER_REVIEW,
    FAMILY_READINESS_ROADMAP,
    ACCOUNT_LOCKDOWN,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    SAFETY_FLOW_ROUTER,
    RECOVERY_PLAN,
    FOLLOW_UP_CHECK_IN_PLANNER
}

data class MoneyTransferPauseAction(
    val id: MoneyTransferPauseActionId,
    val label: String
)

data class MoneyTransferPauseCard(
    val id: MoneyTransferPauseCardId,
    val displayOrder: Int,
    val title: String,
    val pauseSentence: String,
    val whyItMatters: String,
    val doNotDoBoundaries: List<String>,
    val primaryAction: MoneyTransferPauseAction,
    val secondaryActions: List<MoneyTransferPauseAction>
) {
    val routeActions: List<MoneyTransferPauseAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class MoneyTransferPauseGate(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<MoneyTransferPauseCardId>,
    val recommendedActions: List<MoneyTransferPauseAction>,
    val cards: List<MoneyTransferPauseCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<MoneyTransferPauseCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<MoneyTransferPauseActionId>
        get() = recommendedActions.map { action -> action.id }
}

object MoneyTransferPauseGateBuilder {
    private val financialSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.BAIT,
        ScamSignal.CREDENTIALS,
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

    private val safeAccountFeeSignals = setOf(
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.BAIT,
        ScamSignal.LOAN_REFINANCE_FEE
    )

    private val giftCardCryptoSignals = setOf(
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.GIFTICON_PIN
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
    ): MoneyTransferPauseGate {
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val hasNoAnalysisOrSignals = analysis == null || signals.isEmpty()
        val hasHighRisk = analysis?.riskLevel == RiskLevel.HIGH
        val hasUrgentTransfer = ScamSignal.URGENT_TRANSFER in signals
        val hasFinancialSignal = signals.any { signal -> signal in financialSignals }
        val hasCredentialSignal = signals.any { signal -> signal in credentialSignals }
        val hasSafeAccountFeeSignal = signals.any { signal -> signal in safeAccountFeeSignals }
        val hasGiftCardCryptoSignal = signals.any { signal -> signal in giftCardCryptoSignals }
        val hasOverseasAlert = ScamSignal.OVERSEAS_PAYMENT_ALERT in signals
        val hasFamilyVerificationSignal =
            signals.any { signal -> signal in familyVerificationSignals }
        val hasSecrecyImpersonationSignal =
            signals.any { signal -> signal in secrecyImpersonationSignals }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0
        val needsReadinessSupport = readiness.level != ReadinessLevel.READY

        val recommendedCardSet = linkedSetOf(
            MoneyTransferPauseCardId.PAYMENT_PAUSE,
            MoneyTransferPauseCardId.SEPARATE_FAMILY_OFFICIAL_VERIFICATION
        )
        if (hasNoAnalysisOrSignals) {
            recommendedCardSet += MoneyTransferPauseCardId.entries
        }
        if (hasHighRisk || hasUrgentTransfer) {
            recommendedCardSet += MoneyTransferPauseCardId.FINANCIAL_CREDENTIALS_REFUSAL
            recommendedCardSet += MoneyTransferPauseCardId.EVIDENCE_AND_OFFICIAL_HELP
        }
        if (hasFinancialSignal) {
            recommendedCardSet += MoneyTransferPauseCardId.PAYMENT_PAUSE
            recommendedCardSet += MoneyTransferPauseCardId.EVIDENCE_AND_OFFICIAL_HELP
        }
        if (hasCredentialSignal) {
            recommendedCardSet += MoneyTransferPauseCardId.FINANCIAL_CREDENTIALS_REFUSAL
        }
        if (hasSafeAccountFeeSignal) {
            recommendedCardSet += MoneyTransferPauseCardId.SAFE_ACCOUNT_REFUND_LOAN_FEE
        }
        if (hasGiftCardCryptoSignal) {
            recommendedCardSet += MoneyTransferPauseCardId.GIFT_CARD_CRYPTO_PAUSE
        }
        if (hasOverseasAlert) {
            recommendedCardSet += MoneyTransferPauseCardId.FINANCIAL_CREDENTIALS_REFUSAL
            recommendedCardSet += MoneyTransferPauseCardId.SEPARATE_FAMILY_OFFICIAL_VERIFICATION
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet += MoneyTransferPauseCardId.SEPARATE_FAMILY_OFFICIAL_VERIFICATION
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedCardSet += MoneyTransferPauseCardId.PAYMENT_PAUSE
            recommendedCardSet += MoneyTransferPauseCardId.SEPARATE_FAMILY_OFFICIAL_VERIFICATION
            recommendedCardSet += MoneyTransferPauseCardId.EVIDENCE_AND_OFFICIAL_HELP
        }
        if (hasSavedIncident) {
            recommendedCardSet += MoneyTransferPauseCardId.EVIDENCE_AND_OFFICIAL_HELP
        }
        if (needsReadinessSupport) {
            recommendedCardSet += MoneyTransferPauseCardId.SEPARATE_FAMILY_OFFICIAL_VERIFICATION
        }
        val recommendedCardIds = MoneyTransferPauseCardId.entries
            .filter { cardId -> cardId in recommendedCardSet }

        val recommendedActionSet = linkedSetOf(
            MoneyTransferPauseActionId.QUICK_STOP,
            if (analysis == null) {
                MoneyTransferPauseActionId.CHECKER
            } else {
                MoneyTransferPauseActionId.ANALYSIS_RESULT
            },
            MoneyTransferPauseActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            recommendedActionSet += MoneyTransferPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet += MoneyTransferPauseActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += MoneyTransferPauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += MoneyTransferPauseActionId.EVIDENCE_KIT
            recommendedActionSet += MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (hasHighRisk || hasUrgentTransfer || hasFinancialSignal || hasCredentialSignal) {
            recommendedActionSet += MoneyTransferPauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += MoneyTransferPauseActionId.EVIDENCE_KIT
            recommendedActionSet += MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY
            if (analysis != null) {
                recommendedActionSet += MoneyTransferPauseActionId.REPORT_PREP
            }
            recommendedActionSet += MoneyTransferPauseActionId.RECOVERY_PLAN
            recommendedActionSet += MoneyTransferPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += MoneyTransferPauseActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += MoneyTransferPauseActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += MoneyTransferPauseActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedActionSet += MoneyTransferPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet += MoneyTransferPauseActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (hasSavedIncident) {
            recommendedActionSet += MoneyTransferPauseActionId.EVIDENCE_KIT
            recommendedActionSet += MoneyTransferPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (needsReadinessSupport) {
            recommendedActionSet += MoneyTransferPauseActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += MoneyTransferPauseActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += MoneyTransferPauseActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += MoneyTransferPauseActionId.FAMILY_READINESS_ROADMAP
        }
        val recommendedActions = MoneyTransferPauseActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards(hasAnalysis = analysis != null)
        val recommendationTitle = when {
            analysis == null -> "분석 전에도 결제하지 않는 보수적인 멈춤선"
            hasHighRisk || hasUrgentTransfer -> "지금 돈·인증 비밀을 주지 않고 별도 확인"
            hasSafeAccountFeeSignal -> "안전계좌·환급·대출 비용 말만 믿지 않기"
            hasGiftCardCryptoSignal -> "상품권·기프티콘·가상자산 전달 멈추기"
            hasOverseasAlert -> "결제 알림에서 받은 경로 말고 별도 확인"
            hasCredentialSignal -> "인증 비밀과 금융정보를 주지 않기"
            hasFamilyVerificationSignal -> "가족 별도 콜백 전에는 돈을 움직이지 않기"
            hasSecrecyImpersonationSignal -> "결제를 비밀로 하지 않고 공식 경로 확인"
            else -> "확인 전 돈과 금융정보를 보수적으로 멈추기"
        }

        return MoneyTransferPauseGate(
            title = "송금 전 멈춤 확인",
            intro =
                "송금·결제·상품권·기프티콘·가상자산·계좌 이동이나 금융 비밀 제공을 재촉받을 때 " +
                    "사용자가 직접 읽고 직접 고르는 로컬 전용·무상태 수동 확인문입니다. " +
                    "이 앱은 돈을 막거나 옮기지 않고 은행이나 결제수단에 연결되지 않습니다.",
            modeLabel = "돈 보내기 전 · 지금 직접 멈추고 별도 확인",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                analysis = analysis,
                readinessLevel = readiness.level,
                signals = signals,
                hasHighRisk = hasHighRisk,
                hasUrgentTransfer = hasUrgentTransfer,
                hasFinancialSignal = hasFinancialSignal,
                hasCredentialSignal = hasCredentialSignal,
                hasSafeAccountFeeSignal = hasSafeAccountFeeSignal,
                hasGiftCardCryptoSignal = hasGiftCardCryptoSignal,
                hasOverseasAlert = hasOverseasAlert,
                hasFamilyVerificationSignal = hasFamilyVerificationSignal,
                hasSecrecyImpersonationSignal = hasSecrecyImpersonationSignal,
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

    private fun buildCards(hasAnalysis: Boolean): List<MoneyTransferPauseCard> {
        fun action(id: MoneyTransferPauseActionId): MoneyTransferPauseAction = actionFor(id)

        fun currentResultAction(): MoneyTransferPauseAction = action(
            if (hasAnalysis) {
                MoneyTransferPauseActionId.ANALYSIS_RESULT
            } else {
                MoneyTransferPauseActionId.CHECKER
            }
        )

        return listOf(
            MoneyTransferPauseCard(
                id = MoneyTransferPauseCardId.PAYMENT_PAUSE,
                displayOrder = MoneyTransferPauseCardId.PAYMENT_PAUSE.displayOrder,
                title = "송금·결제 멈춤 선언",
                pauseSentence =
                    "별도 확인이 끝날 때까지 송금·결제·계좌 이동·상품권 구매·가상자산 전송을 하지 않겠습니다.",
                whyItMatters =
                    "급하다는 말에 돈을 먼저 움직이면 현재 연락과 분리된 확인 기회를 잃기 쉽습니다.",
                doNotDoBoundaries = listOf(
                    "현재 연락을 유지한 채 송금·결제 화면을 열거나 소액 시험 결제를 하지 마세요.",
                    "상대가 정한 시간·순서·받는 곳에 맞춰 돈이나 자산을 움직이지 마세요."
                ),
                primaryAction = action(MoneyTransferPauseActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(MoneyTransferPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(MoneyTransferPauseActionId.FAMILY_CALLBACK_CARD),
                    action(MoneyTransferPauseActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            MoneyTransferPauseCard(
                id = MoneyTransferPauseCardId.SAFE_ACCOUNT_REFUND_LOAN_FEE,
                displayOrder = MoneyTransferPauseCardId.SAFE_ACCOUNT_REFUND_LOAN_FEE.displayOrder,
                title = "안전계좌·환급·대출 수수료 의심",
                pauseSentence =
                    "안전계좌·환급·대출 전환·보증료라는 말만으로 돈을 보내지 않겠습니다. " +
                        "현재 연락은 끝내고 제가 직접 찾은 공식 경로로 확인하겠습니다.",
                whyItMatters =
                    "자금을 보호하거나 돌려받으려면 먼저 돈을 보내야 한다는 이야기는 추가 송금을 재촉하는 수단이 될 수 있습니다.",
                doNotDoBoundaries = listOf(
                    "현재 상대가 준 계좌·번호·링크·앱을 공식 확인 경로로 여기지 마세요.",
                    "환급·대출·명의 확인을 이유로 선입금·보증료·예치금을 보내지 마세요."
                ),
                primaryAction = action(MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY),
                secondaryActions = listOf(
                    action(MoneyTransferPauseActionId.EVIDENCE_KIT),
                    action(MoneyTransferPauseActionId.REPORT_PREP),
                    action(MoneyTransferPauseActionId.ACCOUNT_LOCKDOWN)
                )
            ),
            MoneyTransferPauseCard(
                id = MoneyTransferPauseCardId.GIFT_CARD_CRYPTO_PAUSE,
                displayOrder = MoneyTransferPauseCardId.GIFT_CARD_CRYPTO_PAUSE.displayOrder,
                title = "상품권·기프티콘·가상자산 멈춤",
                pauseSentence =
                    "상품권·기프티콘을 사거나 비밀값을 보내지 않고 가상자산도 전송하지 않겠습니다.",
                whyItMatters =
                    "구매 뒤 번호나 이미지를 보내라는 요구와 가상자산 전송은 일반 송금처럼 먼저 멈춰 별도 확인해야 합니다.",
                doNotDoBoundaries = listOf(
                    "상품권·기프티콘 번호, PIN, 바코드나 구매 영수증을 보내거나 보여주지 마세요.",
                    "상대가 지정한 지갑·거래소·구매처로 가상자산이나 결제값을 보내지 마세요."
                ),
                primaryAction = action(MoneyTransferPauseActionId.EVIDENCE_KIT),
                secondaryActions = listOf(
                    action(MoneyTransferPauseActionId.ACCOUNT_LOCKDOWN),
                    action(MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY),
                    action(MoneyTransferPauseActionId.FAMILY_ALERT_MESSAGES)
                )
            ),
            MoneyTransferPauseCard(
                id = MoneyTransferPauseCardId.FINANCIAL_CREDENTIALS_REFUSAL,
                displayOrder = MoneyTransferPauseCardId.FINANCIAL_CREDENTIALS_REFUSAL.displayOrder,
                title = "OTP/PIN/비밀번호·카드정보 거절",
                pauseSentence =
                    "인증번호나 PIN·비밀번호를 말하거나 보내지 않고 계좌·카드 정보도 입력하거나 보여주지 않겠습니다.",
                whyItMatters =
                    "인증 비밀과 금융정보는 확인이나 취소를 돕는다는 말로 요구되더라도 현재 연락에 제공하지 않아야 합니다.",
                doNotDoBoundaries = listOf(
                    "인증번호·PIN·비밀번호·보안카드 값·계좌·카드 전체 값을 읽거나 입력하지 마세요.",
                    "화면공유·사진·메시지로 금융 비밀이나 신분정보를 보여주지 마세요."
                ),
                primaryAction = action(MoneyTransferPauseActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(MoneyTransferPauseActionId.QUICK_STOP),
                    action(MoneyTransferPauseActionId.EVIDENCE_KIT),
                    action(MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY)
                )
            ),
            MoneyTransferPauseCard(
                id = MoneyTransferPauseCardId.SEPARATE_FAMILY_OFFICIAL_VERIFICATION,
                displayOrder =
                    MoneyTransferPauseCardId.SEPARATE_FAMILY_OFFICIAL_VERIFICATION.displayOrder,
                title = "가족/기관 별도 확인",
                pauseSentence =
                    "현재 연락에서 받은 번호·목소리·링크는 사용하지 않고 평소 알던 가족 경로와 제가 직접 찾은 기관 경로로 별도 확인하겠습니다.",
                whyItMatters =
                    "가족 목소리·새 번호·기관 이름이 익숙해도 현재 연락 안에서만 확인하면 같은 압박에 머물 수 있습니다.",
                doNotDoBoundaries = listOf(
                    "새 번호·현재 목소리·프로필만으로 가족이나 기관이라고 단정하지 마세요.",
                    "현재 상대에게 가족 확인 답, 연락처, 금융정보나 확인 결과를 알려주지 마세요."
                ),
                primaryAction = action(MoneyTransferPauseActionId.FAMILY_CALLBACK_CARD),
                secondaryActions = listOf(
                    action(MoneyTransferPauseActionId.SAFE_CALL_SCRIPTS),
                    action(MoneyTransferPauseActionId.TRUSTED_HELPER_REVIEW),
                    action(MoneyTransferPauseActionId.FAMILY_READINESS_ROADMAP),
                    action(MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY)
                )
            ),
            MoneyTransferPauseCard(
                id = MoneyTransferPauseCardId.EVIDENCE_AND_OFFICIAL_HELP,
                displayOrder = MoneyTransferPauseCardId.EVIDENCE_AND_OFFICIAL_HELP.displayOrder,
                title = "증거·공식 도움으로 이어가기",
                pauseSentence =
                    "추가 결제는 멈추고 요구받은 행동 종류만 직접 보존한 뒤 기존 공식 도움 경로를 고르겠습니다.",
                whyItMatters =
                    "비밀값을 다시 퍼뜨리지 않는 범위에서 요구 종류와 상황을 남기면 가족·공식 창구와 차분히 다음 행동을 고를 수 있습니다.",
                doNotDoBoundaries = listOf(
                    "증거를 남긴다며 상대에게 다시 답장하거나 원문·비밀값을 다른 곳에 퍼뜨리지 마세요.",
                    "이 카드가 결제 취소·환급·자금 회수나 공식 결과를 대신한다고 여기지 마세요."
                ),
                primaryAction = action(MoneyTransferPauseActionId.EVIDENCE_KIT),
                secondaryActions = listOf(
                    action(MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY),
                    action(MoneyTransferPauseActionId.RECOVERY_PLAN),
                    action(MoneyTransferPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                    currentResultAction()
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readinessLevel: ReadinessLevel,
        signals: Set<ScamSignal>,
        hasHighRisk: Boolean,
        hasUrgentTransfer: Boolean,
        hasFinancialSignal: Boolean,
        hasCredentialSignal: Boolean,
        hasSafeAccountFeeSignal: Boolean,
        hasGiftCardCryptoSignal: Boolean,
        hasOverseasAlert: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasSecrecyImpersonationSignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (analysis == null || signals.isEmpty()) {
                add(
                    "현재 분석이 없거나 일치 신호가 없습니다. 이를 결제해도 된다는 판정으로 보지 말고 " +
                        "돈·상품권·가상자산·금융 비밀을 먼저 멈춘 뒤 별도 확인하세요."
                )
            }
            if (hasHighRisk || hasUrgentTransfer) {
                add(
                    "위험도가 높거나 긴급 송금 압박 신호가 있어 결제 멈춤·인증 비밀 거절을 먼저 두고 " +
                        "계정 보호·증거·공식 도움·후속 점검 경로를 권합니다."
                )
            } else if (hasFinancialSignal) {
                add(
                    "금융·송금·결제 관련 신호가 있어 어떤 금액도 먼저 움직이지 않고 계정 보호·증거·" +
                        "공식 도움 경로를 직접 고르도록 권합니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증정보 요구 가능성이 있어 인증번호·PIN·비밀번호·계좌·카드 정보를 말하거나 " +
                        "보내지 않는 카드를 권합니다."
                )
            }
            if (hasSafeAccountFeeSignal) {
                add(
                    "안전계좌·환급·대출·보증료 이야기와 관련된 신호가 있어 현재 연락을 끝내고 " +
                        "사용자가 직접 찾은 공식 경로로 확인하도록 권합니다."
                )
            }
            if (hasGiftCardCryptoSignal) {
                add(
                    "상품권·기프티콘·가상자산 관련 신호가 있어 구매·비밀값 전달·전송을 모두 멈추는 " +
                        "카드를 권합니다."
                )
            }
            if (hasOverseasAlert) {
                add(
                    "해외·소액 결제 알림 신호가 있어 알림에 포함된 경로를 쓰지 않고 금융 비밀을 " +
                        "주지 않은 채 별도 공식 경로로 확인하도록 권합니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 긴급상황·목소리 합성·번호 변경 신호가 있어 평소 알던 가족 경로로 콜백하기 " +
                        "전에는 돈을 움직이지 않도록 권합니다."
                )
            }
            if (hasSecrecyImpersonationSignal) {
                add(
                    "비밀 유지·기관 사칭 압박 신호가 있어 결제를 비밀로 하지 않고 현재 연락을 끝낸 뒤 " +
                        "가족과 사용자가 직접 찾은 공식 경로에 알리도록 권합니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만 반영하며 사건 내용은 읽지 않습니다. 증거·" +
                        "후속 점검 경로만 함께 권합니다."
                )
            }
            if (isEmpty()) {
                add("현재 신호만으로 사실을 확정하지 않고 결제 전 별도 가족·공식 확인을 직접 진행하세요.")
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW ->
                "가족 준비가 부족한 상태라 콜백·가족 경고·보호자 검토·준비 로드맵을 함께 권합니다. 실제 가족 정보는 이 화면에 입력하지 마세요."
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중이라 콜백·가족 경고·보호자 검토·준비 로드맵으로 빠진 부분을 직접 확인하도록 권합니다. 저장된 실제 값은 추천에 들어오지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있어도 이번 결제나 연락의 사실·조치 완료가 확인된 것은 아닙니다. 돈을 움직이지 말고 별도 경로로 다시 확인하세요."
        }
        return "$situationCopy $readinessCopy"
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 게이트는 사용자가 화면을 직접 읽고 직접 선택하는 로컬 전용·무상태 수동 안내입니다.",
        "추천은 위험도·일치 신호, 값 없는 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·사건 내용·가족 안전 계획 값을 사용하지 않습니다.",
        "이름·연락처·정확한 전화번호·계좌·카드·인증번호·PIN·비밀번호·상품권 비밀값·가족 확인 답·신분정보·정확한 링크나 도메인을 묻거나 저장하지 않습니다.",
        "카드 선택·추천·읽음·공유·체크·메모·결제 상태·진행·이력을 위한 새 저장소를 만들거나 기록하지 않습니다.",
        "문자·메시지·통화·연락처·클립보드·금융 앱을 자동으로 읽거나 감시하지 않습니다.",
        "네트워크를 사용하거나 새 Android 권한을 요청하지 않습니다.",
        "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커를 만들거나 예약하지 않으며 스케줄러를 사용하지 않습니다.",
        "결제·은행 API, NFC나 Android 계정 관리자에 접근하지 않으며 송금·결제 접근 권한과 계좌·카드·은행 연동이 없습니다.",
        "전화를 걸거나 끊거나 선별·차단하지 않고 문자를 보내거나 받는 사람을 고르지 않으며 공유도 자동 전송하지 않습니다.",
        "붙여넣은 글, 통화 상태, 연락처, 사건 기록, 가족 안전 계획, 계정·카드·은행·기기 설정이나 돈의 움직임을 추가·수정·삭제하지 않습니다.",
        "결제·송금·이체를 자동 차단·취소·실행하지 않고 계정·카드를 자동 정지·동결하지 않으며 환급·자금 회수·피해 복구를 자동 수행하지 않습니다.",
        "이 안내는 사기·본인확인·조치 완료·결제 취소·환급·자금 회수·계정 또는 카드 조치·법률·금융 결과를 증명하거나 보장하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<MoneyTransferPauseCardId>,
        recommendedActions: List<MoneyTransferPauseAction>,
        cards: List<MoneyTransferPauseCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 송금 전 멈춤 확인")
            appendLine()
            appendLine("지금 직접 읽을 추천 멈춤 문장")
            recommendedCardIds.forEach { cardId ->
                val card = cardsById.getValue(cardId)
                appendLine("- ${card.title}: ${card.pauseSentence}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("전체 수동 카드")
            cards.forEach { card -> appendLine("- ${card.title}") }
            appendLine()
            append(
                "원문·사람·연락처·비밀값을 넣지 않은 일반 멈춤 문장과 기존 경로 이름입니다. " +
                    "가족이 직접 읽고 필요한 경로를 직접 고릅니다."
            )
        }.trim()
    }

    private fun actionFor(
        id: MoneyTransferPauseActionId
    ): MoneyTransferPauseAction = MoneyTransferPauseAction(
        id = id,
        label = when (id) {
            MoneyTransferPauseActionId.QUICK_STOP -> "긴급 멈춤 모드"
            MoneyTransferPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS -> "진행 중 연락 종료 문장"
            MoneyTransferPauseActionId.CHECKER -> "의심 내용 검사"
            MoneyTransferPauseActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            MoneyTransferPauseActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 확인 카드"
            MoneyTransferPauseActionId.SAFE_CALL_SCRIPTS -> "안전 확인 전화 스크립트"
            MoneyTransferPauseActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
            MoneyTransferPauseActionId.TRUSTED_HELPER_REVIEW -> "보호자와 함께 안전 검토"
            MoneyTransferPauseActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
            MoneyTransferPauseActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
            MoneyTransferPauseActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처"
            MoneyTransferPauseActionId.REPORT_PREP -> "신고용 상담 준비 카드"
            MoneyTransferPauseActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
            MoneyTransferPauseActionId.RECOVERY_PLAN -> "24시간 수습 계획"
            MoneyTransferPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER -> "며칠 뒤 후속 점검"
        }
    )
}
