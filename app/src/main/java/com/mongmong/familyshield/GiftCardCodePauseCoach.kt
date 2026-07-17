package com.mongmong.familyshield

enum class GiftCardCodePauseCardId(val displayOrder: Int) {
    TREAT_GIFT_CARDS_LIKE_MONEY(1),
    NEVER_REVEAL_CODE_PIN_BARCODE_OR_QR(2),
    STOP_SPLIT_PURCHASE_INSTRUCTIONS(3),
    NO_FEE_REFUND_FINE_OR_EMERGENCY_PAYMENT(4),
    LEAVE_LINK_APP_CALL_AND_SCREEN_SHARE(5),
    VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP(6)
}

enum class GiftCardCodePauseActionId {
    QUICK_STOP,
    CHECKER,
    ANALYSIS_RESULT,
    SAFETY_PLAN,
    FAMILY_READINESS_ROADMAP,
    FAMILY_SAFETY_DRILL_COACH,
    SAFE_PASTE_PRIVACY_COACH,
    ACTIVE_CONTACT_EXIT_SCRIPTS,
    FAMILY_CALLBACK_CARD,
    FAMILY_SAFE_WORD_RESET_COACH,
    SEPARATE_CHANNEL_VERIFICATION_COACH,
    SAFE_CALL_SCRIPTS,
    VOICE_IMPERSONATION_REALITY_CHECK_COACH,
    NEW_NUMBER_QUARANTINE_COACH,
    CREDENTIAL_SECRET_FREEZE_COACH,
    FAMILY_ALERT_MESSAGES,
    VERIFICATION_OUTCOME_NEXT_STEPS_BOARD,
    RECONTACT_BOUNDARY_COACH,
    TRUSTED_HELPER_REVIEW,
    FAMILY_RESPONSE_ROLE_BOARD,
    FAMILY_CALM_DEBRIEF_AGENDA,
    MONEY_TRANSFER_PAUSE_GATE,
    OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
    RECOVERY_PROMISE_REALITY_CHECK_COACH,
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

data class GiftCardCodePauseAction(
    val id: GiftCardCodePauseActionId,
    val label: String
)

data class GiftCardCodePauseCard(
    val id: GiftCardCodePauseCardId,
    val displayOrder: Int,
    val title: String,
    val codePressureScenario: String,
    val valueFreeActionSentence: String,
    val whyItMatters: String,
    val doNotBuyShareScanRedeemOrAttachBoundaries: List<String>,
    val primaryAction: GiftCardCodePauseAction,
    val secondaryActions: List<GiftCardCodePauseAction>
) {
    val routeActions: List<GiftCardCodePauseAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class GiftCardCodePauseCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<GiftCardCodePauseCardId>,
    val recommendedActions: List<GiftCardCodePauseAction>,
    val cards: List<GiftCardCodePauseCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<GiftCardCodePauseCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<GiftCardCodePauseActionId>
        get() = recommendedActions.map { action -> action.id }
}

object GiftCardCodePauseCoachBuilder {
    private val financialSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.BAIT,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val giftCardCodeSignals = setOf(
        ScamSignal.GIFTICON_PIN,
        ScamSignal.GIFT_CRYPTO
    )

    private val credentialSignals = setOf(
        ScamSignal.CREDENTIALS,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val officialPressureSignals = setOf(
        ScamSignal.IMPERSONATION,
        ScamSignal.SECRECY
    )

    private val recoveryPromiseSignals = setOf(
        ScamSignal.BAIT,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
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

    private val familyVerificationSignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.PHONE_NUMBER_CHANGE
    )

    fun build(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness,
        incidentCount: Int = 0
    ): GiftCardCodePauseCoach {
        val riskLevel = analysis?.riskLevel
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val readinessLevel = readiness.level
        val hasNoAnalysisOrSignals = analysis == null || signals.isEmpty()
        val hasHighRisk = riskLevel == RiskLevel.HIGH
        val hasGiftCardCodeSignal = signals.any { signal -> signal in giftCardCodeSignals }
        val hasFinancialSignal = signals.any { signal -> signal in financialSignals }
        val hasCredentialSignal = signals.any { signal -> signal in credentialSignals }
        val hasOfficialPressureSignal = signals.any { signal ->
            signal in officialPressureSignals
        }
        val hasRecoveryPromiseSignal = signals.any { signal ->
            signal in recoveryPromiseSignals
        }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasRemoteAccessSignal = signals.any { signal -> signal in remoteAccessSignals }
        val hasFamilyVerificationSignal = signals.any { signal ->
            signal in familyVerificationSignals
        }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0

        val recommendedCardSet = linkedSetOf(
            GiftCardCodePauseCardId.TREAT_GIFT_CARDS_LIKE_MONEY,
            GiftCardCodePauseCardId.NEVER_REVEAL_CODE_PIN_BARCODE_OR_QR
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += GiftCardCodePauseCardId.entries
        }
        if (hasGiftCardCodeSignal || hasFinancialSignal) {
            recommendedCardSet += GiftCardCodePauseCardId.TREAT_GIFT_CARDS_LIKE_MONEY
            recommendedCardSet += GiftCardCodePauseCardId.NEVER_REVEAL_CODE_PIN_BARCODE_OR_QR
            recommendedCardSet += GiftCardCodePauseCardId.STOP_SPLIT_PURCHASE_INSTRUCTIONS
            recommendedCardSet +=
                GiftCardCodePauseCardId.NO_FEE_REFUND_FINE_OR_EMERGENCY_PAYMENT
            recommendedCardSet +=
                GiftCardCodePauseCardId.VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP
        }
        if (hasCredentialSignal) {
            recommendedCardSet += GiftCardCodePauseCardId.NEVER_REVEAL_CODE_PIN_BARCODE_OR_QR
            recommendedCardSet += GiftCardCodePauseCardId.LEAVE_LINK_APP_CALL_AND_SCREEN_SHARE
        }
        if (hasOfficialPressureSignal || hasRecoveryPromiseSignal) {
            recommendedCardSet +=
                GiftCardCodePauseCardId.NO_FEE_REFUND_FINE_OR_EMERGENCY_PAYMENT
            recommendedCardSet += GiftCardCodePauseCardId.STOP_SPLIT_PURCHASE_INSTRUCTIONS
            recommendedCardSet +=
                GiftCardCodePauseCardId.VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP
        }
        if (hasLinkSignal || hasRemoteAccessSignal) {
            recommendedCardSet += GiftCardCodePauseCardId.NEVER_REVEAL_CODE_PIN_BARCODE_OR_QR
            recommendedCardSet += GiftCardCodePauseCardId.LEAVE_LINK_APP_CALL_AND_SCREEN_SHARE
            recommendedCardSet += GiftCardCodePauseCardId.TREAT_GIFT_CARDS_LIKE_MONEY
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet += GiftCardCodePauseCardId.TREAT_GIFT_CARDS_LIKE_MONEY
            recommendedCardSet +=
                GiftCardCodePauseCardId.NO_FEE_REFUND_FINE_OR_EMERGENCY_PAYMENT
            recommendedCardSet += GiftCardCodePauseCardId.STOP_SPLIT_PURCHASE_INSTRUCTIONS
        }
        if (hasSavedIncident) {
            recommendedCardSet +=
                GiftCardCodePauseCardId.VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedCardSet += GiftCardCodePauseCardId.TREAT_GIFT_CARDS_LIKE_MONEY
                recommendedCardSet +=
                    GiftCardCodePauseCardId.NO_FEE_REFUND_FINE_OR_EMERGENCY_PAYMENT
            }
            ReadinessLevel.READY -> {
                recommendedCardSet += GiftCardCodePauseCardId.TREAT_GIFT_CARDS_LIKE_MONEY
                recommendedCardSet +=
                    GiftCardCodePauseCardId.NEVER_REVEAL_CODE_PIN_BARCODE_OR_QR
            }
        }
        val recommendedCardIds = GiftCardCodePauseCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            GiftCardCodePauseActionId.CHECKER
        } else {
            GiftCardCodePauseActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            currentResultActionId,
            GiftCardCodePauseActionId.QUICK_STOP,
            GiftCardCodePauseActionId.MONEY_TRANSFER_PAUSE_GATE,
            GiftCardCodePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH,
            GiftCardCodePauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH,
            GiftCardCodePauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
            GiftCardCodePauseActionId.TRUSTED_HELPER_REVIEW,
            GiftCardCodePauseActionId.OFFICIAL_HELP_DIRECTORY,
            GiftCardCodePauseActionId.REPORT_PREP,
            GiftCardCodePauseActionId.PRIVACY_CENTER,
            GiftCardCodePauseActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            GiftCardCodePauseActionId.entries
                .filterNot { actionId ->
                    actionId == GiftCardCodePauseActionId.ANALYSIS_RESULT
                }
                .forEach { actionId -> recommendedActionSet += actionId }
        }
        if (hasHighRisk) {
            GiftCardCodePauseActionId.entries.forEach { actionId ->
                recommendedActionSet += actionId
            }
        }
        if (hasGiftCardCodeSignal || hasFinancialSignal) {
            recommendedActionSet += GiftCardCodePauseActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += GiftCardCodePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet +=
                GiftCardCodePauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH
            recommendedActionSet +=
                GiftCardCodePauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet += GiftCardCodePauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += GiftCardCodePauseActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += GiftCardCodePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += GiftCardCodePauseActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += GiftCardCodePauseActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += GiftCardCodePauseActionId.REPORT_PREP
        }
        if (hasCredentialSignal) {
            recommendedActionSet += GiftCardCodePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet += GiftCardCodePauseActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet += GiftCardCodePauseActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += GiftCardCodePauseActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += GiftCardCodePauseActionId.EXPOSURE_IMPACT_TRIAGE
        }
        if (hasOfficialPressureSignal) {
            recommendedActionSet +=
                GiftCardCodePauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet += GiftCardCodePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet += GiftCardCodePauseActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += GiftCardCodePauseActionId.FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet += GiftCardCodePauseActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += GiftCardCodePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += GiftCardCodePauseActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += GiftCardCodePauseActionId.REPORT_PREP
        }
        if (hasRecoveryPromiseSignal) {
            recommendedActionSet +=
                GiftCardCodePauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH
            recommendedActionSet += GiftCardCodePauseActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += GiftCardCodePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet += GiftCardCodePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += GiftCardCodePauseActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += GiftCardCodePauseActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += GiftCardCodePauseActionId.REPORT_PREP
        }
        if (hasLinkSignal || hasRemoteAccessSignal) {
            recommendedActionSet += GiftCardCodePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet += GiftCardCodePauseActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += GiftCardCodePauseActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += GiftCardCodePauseActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet += GiftCardCodePauseActionId.PRIVACY_CENTER
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet +=
                GiftCardCodePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += GiftCardCodePauseActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet +=
                GiftCardCodePauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH
            recommendedActionSet += GiftCardCodePauseActionId.NEW_NUMBER_QUARANTINE_COACH
            recommendedActionSet += GiftCardCodePauseActionId.FAMILY_SAFE_WORD_RESET_COACH
            recommendedActionSet += GiftCardCodePauseActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += GiftCardCodePauseActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasSavedIncident) {
            recommendedActionSet += GiftCardCodePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += GiftCardCodePauseActionId.EVIDENCE_KIT
            recommendedActionSet += GiftCardCodePauseActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedActionSet += GiftCardCodePauseActionId.SAFETY_PLAN
                recommendedActionSet += GiftCardCodePauseActionId.FAMILY_SAFE_WORD_RESET_COACH
                recommendedActionSet += GiftCardCodePauseActionId.FAMILY_READINESS_ROADMAP
                recommendedActionSet += GiftCardCodePauseActionId.FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += GiftCardCodePauseActionId.SAFE_CALL_SCRIPTS
            }
            ReadinessLevel.READY -> {
                recommendedActionSet += GiftCardCodePauseActionId.FAMILY_CALLBACK_CARD
                recommendedActionSet +=
                    GiftCardCodePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                recommendedActionSet += GiftCardCodePauseActionId.SAFE_CALL_SCRIPTS
            }
        }
        val recommendedActions = GiftCardCodePauseActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null -> "검사 결과가 없어도 모든 상품권·코드 멈춤 카드를 보수적으로 읽기"
            hasGiftCardCodeSignal -> "상품권 구매와 코드·PIN·바코드·QR 전달부터 멈추기"
            hasCredentialSignal -> "상품권 코드와 인증 비밀값을 말하거나 붙여넣지 않기"
            hasOfficialPressureSignal -> "직함·비밀 압박과 상품권 요구를 따로 점검하기"
            hasRecoveryPromiseSignal -> "환급·회복 약속에 상품권을 쓰지 않기"
            hasFinancialSignal -> "상품권을 송금과 같은 돈 행동으로 보고 멈추기"
            hasRemoteAccessSignal -> "같은 앱·통화·화면공유 코드 확인 흐름을 벗어나기"
            hasLinkSignal -> "상대가 준 링크 안에서 코드 확인을 이어가지 않기"
            hasFamilyVerificationSignal -> "가족 위급 주장도 상품권 없이 별도 경로로 확인하기"
            hasHighRisk -> "높은 위험 범주에서 값 없는 상품권·코드 대응을 넓게 고르기"
            hasSavedIncident -> "사건 내용 없이 증거·도움·후속 준비 경로만 고르기"
            else -> "상품권·코드 요청에 답하기 전 구매·공유·촬영 행동을 멈추기"
        }

        return GiftCardCodePauseCoach(
            title = "상품권·코드 멈춤 코치",
            intro =
                "상품권·기프티콘·선불카드·문화상품권을 사거나 코드·PIN·바코드·QR을 " +
                    "말하고 찍고 보내라는 압박 앞에서, 실제 값을 읽거나 받지 않고 고정 " +
                    "카드와 기존 대응 화면만 직접 고르는 로컬 전용·무상태 수동 안내입니다.",
            modeLabel = "구매 멈춤 · 코드값 비공유 · 스캔·사용 없음",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                readinessLevel = readinessLevel,
                hasNoAnalysisOrSignals = hasNoAnalysisOrSignals,
                hasHighRisk = hasHighRisk,
                hasGiftCardCodeSignal = hasGiftCardCodeSignal,
                hasFinancialSignal = hasFinancialSignal,
                hasCredentialSignal = hasCredentialSignal,
                hasOfficialPressureSignal = hasOfficialPressureSignal,
                hasRecoveryPromiseSignal = hasRecoveryPromiseSignal,
                hasLinkSignal = hasLinkSignal,
                hasRemoteAccessSignal = hasRemoteAccessSignal,
                hasFamilyVerificationSignal = hasFamilyVerificationSignal,
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

    private fun buildCards(): List<GiftCardCodePauseCard> {
        fun action(id: GiftCardCodePauseActionId): GiftCardCodePauseAction = actionFor(id)

        return listOf(
            GiftCardCodePauseCard(
                id = GiftCardCodePauseCardId.TREAT_GIFT_CARDS_LIKE_MONEY,
                displayOrder = GiftCardCodePauseCardId.TREAT_GIFT_CARDS_LIKE_MONEY.displayOrder,
                title = "상품권·기프티콘 구매를 송금보다 가볍게 보지 않기",
                codePressureScenario =
                    "현금 이체가 아니라 상품권·기프티콘·선불카드를 사는 것뿐이라고 재촉하는 때",
                valueFreeActionSentence =
                    "어떤 상품권 구매도 이어가지 않고 값 없는 송금 전 멈춤 순서를 읽겠습니다.",
                whyItMatters =
                    "상품권 구매는 돈을 쓰는 행동이며 코드가 넘어가면 되돌리기 어렵다는 압박에 이용될 수 있습니다.",
                doNotBuyShareScanRedeemOrAttachBoundaries = listOf(
                    "금액·수량·구매처·받는 사람·결제·계정·카드·은행·지갑·거래 값을 입력하거나 공유하지 마세요.",
                    "상품권·기프티콘·선불카드의 구매·결제·취소·환급·차지백·동결·차단·회수를 수행하거나 보장하지 않습니다."
                ),
                primaryAction = action(GiftCardCodePauseActionId.MONEY_TRANSFER_PAUSE_GATE),
                secondaryActions = listOf(
                    action(GiftCardCodePauseActionId.QUICK_STOP),
                    action(GiftCardCodePauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH),
                    action(GiftCardCodePauseActionId.EXPOSURE_IMPACT_TRIAGE)
                )
            ),
            GiftCardCodePauseCard(
                id = GiftCardCodePauseCardId.NEVER_REVEAL_CODE_PIN_BARCODE_OR_QR,
                displayOrder =
                    GiftCardCodePauseCardId.NEVER_REVEAL_CODE_PIN_BARCODE_OR_QR.displayOrder,
                title = "코드·PIN·바코드·QR을 말하거나 사진·캡처·전달하지 않기",
                codePressureScenario =
                    "확인·인증·정산을 이유로 교환권 코드나 PIN을 읽거나 바코드·QR 화면을 보내라고 하는 때",
                valueFreeActionSentence =
                    "실제 코드값을 말하거나 입력·촬영·캡처·복사·전달하지 않고 비밀값 멈춤 경로를 고르겠습니다.",
                whyItMatters =
                    "코드·PIN·바코드·QR은 이미지나 문장으로 넘기는 순간 다른 사람이 사용할 수 있는 민감값일 수 있습니다.",
                doNotBuyShareScanRedeemOrAttachBoundaries = listOf(
                    "상품권·교환권·기프티콘의 코드·PIN·바코드·QR·일련번호·사용 번호·잔액 값을 적거나 보여 주지 마세요.",
                    "코드를 읽기·스캔·해독·검증·사용·잔액 조회·복사·저장·업로드·첨부·전달하지 않습니다."
                ),
                primaryAction = action(
                    GiftCardCodePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH
                ),
                secondaryActions = listOf(
                    action(GiftCardCodePauseActionId.SAFE_PASTE_PRIVACY_COACH),
                    action(GiftCardCodePauseActionId.ACCOUNT_LOCKDOWN),
                    action(GiftCardCodePauseActionId.EXPOSURE_IMPACT_TRIAGE)
                )
            ),
            GiftCardCodePauseCard(
                id = GiftCardCodePauseCardId.STOP_SPLIT_PURCHASE_INSTRUCTIONS,
                displayOrder =
                    GiftCardCodePauseCardId.STOP_SPLIT_PURCHASE_INSTRUCTIONS.displayOrder,
                title = "쪼개서 여러 장 사라는 지시를 멈추기",
                codePressureScenario =
                    "한도나 의심을 피한다며 여러 곳에서 여러 장으로 나누어 사라고 하는 때",
                valueFreeActionSentence =
                    "구매 장소나 장수를 바꾸지 않고 모든 분할 구매 지시를 지금 멈추겠습니다.",
                whyItMatters =
                    "구매를 잘게 나누라는 지시는 생각할 시간을 줄이고 반복 지출을 이어가게 만들 수 있습니다.",
                doNotBuyShareScanRedeemOrAttachBoundaries = listOf(
                    "구매처·상표·장수·금액·영수증·결제수단·이동 경로를 이 코치에 적거나 공유하지 마세요.",
                    "매장·상품·재고·구매 한도·결제·영수증을 조회하지 않고 구매·예약·주문·취소를 하지 않습니다."
                ),
                primaryAction = action(GiftCardCodePauseActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(GiftCardCodePauseActionId.MONEY_TRANSFER_PAUSE_GATE),
                    action(GiftCardCodePauseActionId.RECONTACT_BOUNDARY_COACH),
                    action(GiftCardCodePauseActionId.TRUSTED_HELPER_REVIEW)
                )
            ),
            GiftCardCodePauseCard(
                id = GiftCardCodePauseCardId.NO_FEE_REFUND_FINE_OR_EMERGENCY_PAYMENT,
                displayOrder =
                    GiftCardCodePauseCardId
                        .NO_FEE_REFUND_FINE_OR_EMERGENCY_PAYMENT.displayOrder,
                title = "환급·수수료·보증금·벌금·가족 위급을 상품권으로 해결하지 않기",
                codePressureScenario =
                    "환급·회복·수수료·보증금·벌금·가족 위급 처리를 위해 상품권이 필요하다고 하는 때",
                valueFreeActionSentence =
                    "직함이나 사연을 결과로 믿지 않고 상품권 없이 기존 현실 점검 경로를 따로 읽겠습니다.",
                whyItMatters =
                    "공식처럼 들리는 이유나 긴급한 사연도 상품권 구매·코드 공유 요구의 사실 여부를 증명하지 않습니다.",
                doNotBuyShareScanRedeemOrAttachBoundaries = listOf(
                    "기관·사람·가족·도우미·변호사·수사관·사건·신고·공식 번호·환급·회복 값을 적거나 공유하지 마세요.",
                    "사람·기관·직함·번호·사건·법률 상태를 조회·검증하지 않고 환급·회복·차지백·신고·법률 절차를 수행하지 않습니다."
                ),
                primaryAction = action(
                    GiftCardCodePauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH
                ),
                secondaryActions = listOf(
                    action(GiftCardCodePauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH),
                    action(GiftCardCodePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH),
                    action(GiftCardCodePauseActionId.FAMILY_ALERT_MESSAGES)
                )
            ),
            GiftCardCodePauseCard(
                id = GiftCardCodePauseCardId.LEAVE_LINK_APP_CALL_AND_SCREEN_SHARE,
                displayOrder =
                    GiftCardCodePauseCardId.LEAVE_LINK_APP_CALL_AND_SCREEN_SHARE.displayOrder,
                title = "앱 설치·링크·화면공유로 코드 확인을 돕겠다는 흐름 끊기",
                codePressureScenario =
                    "링크나 앱을 열고 통화를 유지하거나 화면공유로 코드·바코드·QR 확인을 돕겠다고 하는 때",
                valueFreeActionSentence =
                    "같은 링크·앱·통화·화면공유 세션에서는 입력·설치·촬영·스캔을 이어가지 않겠습니다.",
                whyItMatters =
                    "상대가 고른 세션 안에서 행동을 이어가면 민감한 코드와 화면이 그대로 노출될 수 있습니다.",
                doNotBuyShareScanRedeemOrAttachBoundaries = listOf(
                    "URL·도메인·앱·패키지·기기 식별값·화면·파일·사진·스크린샷 이름을 적거나 공유하지 마세요.",
                    "링크·앱·기기·바코드·QR을 검사하지 않고 앱 목록 조회·악성 프로그램 제거·원격 종료·기기 정리를 수행하지 않습니다."
                ),
                primaryAction = action(GiftCardCodePauseActionId.REMOTE_ACCESS_CUTOFF_COACH),
                secondaryActions = listOf(
                    action(GiftCardCodePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(GiftCardCodePauseActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(GiftCardCodePauseActionId.PRIVACY_CENTER)
                )
            ),
            GiftCardCodePauseCard(
                id = GiftCardCodePauseCardId.VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP,
                displayOrder =
                    GiftCardCodePauseCardId.VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP.displayOrder,
                title = "증거·가족·공식 도움은 코드값 없이 기존 화면에서만 정리하기",
                codePressureScenario =
                    "가족과 검토하거나 증거·공식 도움·신고 준비·며칠 뒤 점검 순서를 이어가려는 때",
                valueFreeActionSentence =
                    "코드·사건 원문·식별값 없이 기존 증거·도움·신고 준비·후속 화면에서 순서만 살펴보겠습니다.",
                whyItMatters =
                    "값 없는 라우팅은 민감한 코드와 피해 정보가 카드나 공유 문장에 반복 노출되는 일을 줄입니다.",
                doNotBuyShareScanRedeemOrAttachBoundaries = listOf(
                    "코드·원문·가린 단서·사건 내용·미리보기·문서·영수증·파일·사진·받는 사람·제출 결과를 적거나 공유하지 마세요.",
                    "증거를 수집·촬영·스캔·첨부·업로드·동기화·가림·삭제하지 않고 신고·사건·법률 절차를 작성·제출·확인하지 않습니다."
                ),
                primaryAction = action(
                    GiftCardCodePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
                ),
                secondaryActions = listOf(
                    action(GiftCardCodePauseActionId.EVIDENCE_KIT),
                    action(GiftCardCodePauseActionId.TRUSTED_HELPER_REVIEW),
                    action(GiftCardCodePauseActionId.OFFICIAL_HELP_DIRECTORY),
                    action(GiftCardCodePauseActionId.REPORT_PREP),
                    action(GiftCardCodePauseActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                    action(GiftCardCodePauseActionId.SAFETY_FLOW_ROUTER)
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        readinessLevel: ReadinessLevel,
        hasNoAnalysisOrSignals: Boolean,
        hasHighRisk: Boolean,
        hasGiftCardCodeSignal: Boolean,
        hasFinancialSignal: Boolean,
        hasCredentialSignal: Boolean,
        hasOfficialPressureSignal: Boolean,
        hasRecoveryPromiseSignal: Boolean,
        hasLinkSignal: Boolean,
        hasRemoteAccessSignal: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (hasNoAnalysisOrSignals) {
                add(
                    "검사 결과나 일치 신호가 없어도 상품권·교환권·코드·PIN·바코드·QR·" +
                        "일련번호·결제·구매·잔액·사용·계정·카드·은행·지갑·거래·환급·" +
                        "차지백·취소·회복·가족 위급·사건·신고·도우미·매장·상표·기관·" +
                        "발신자·링크·문서·기기·자격 상태를 단정하지 말고 모든 상품권·" +
                        "코드 멈춤 카드를 보수적으로 읽도록 권합니다."
                )
            }
            if (hasHighRisk) {
                add(
                    "높은 위험 범주에서는 돈·코드·비밀값·기관 압박·계정·기기·노출·" +
                        "증거·가족·도움·후속 대응 경로를 값 없이 넓게 함께 보여 줍니다."
                )
            }
            if (hasGiftCardCodeSignal || hasFinancialSignal) {
                add(
                    "상품권 PIN·긴급 송금·미끼·가상자산·안전계좌·대환 비용·해외 결제 " +
                        "같은 돈 범주에서는 송금 멈춤·비밀값 멈춤·회복 약속 점검·기관 " +
                        "압박 점검·계정 잠금·노출 분류·증거 전달 개인정보·보호자 검토·" +
                        "도움·신고 준비 경로를 보여 주며 구매·결제 취소·환급·차지백·" +
                        "잔액 조회·코드 차단·회복 결과를 만들지 않습니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증 비밀값 범주에서는 비밀값 멈춤·붙여넣기 개인정보·계정 잠금·" +
                        "원격지시 중단·노출 분류 경로를 권하며 실제 상품권 코드·인증번호·" +
                        "PIN·비밀번호·계정·카드·은행·지갑 값을 읽거나 표시하지 않습니다."
                )
            }
            if (hasOfficialPressureSignal) {
                add(
                    "사칭·비밀 유지 압박 범주에서는 기관 압박 점검·진행 중 연락 종료·" +
                        "가족 알림·차분 대화·보호자 검토·증거 전달 개인정보·도움·신고 " +
                        "준비 경로를 직접 고르며 공식 번호·디렉터리 값·법률 사건 값을 사용하지 않습니다."
                )
            }
            if (hasRecoveryPromiseSignal) {
                add(
                    "환급·보상·회복을 떠올리게 하는 기존 신호 범주에서는 회복 약속 현실 " +
                        "점검·송금 멈춤·비밀값 멈춤·증거 전달 개인정보·보호자 검토·도움·" +
                        "신고 준비 경로를 고르며 회복·환급·차지백 결과를 단정하지 않습니다."
                )
            }
            if (hasLinkSignal || hasRemoteAccessSignal) {
                add(
                    "링크·문자 미끼·앱 설치·통화 유지 신호에서는 같은 링크·앱·통화·" +
                        "화면공유 세션을 계속하지 말고 종료 문구·원격지시 중단·노출 분류·" +
                        "개인정보 경로를 직접 고르세요. URL 검증·링크 스캔·앱 목록 조회·" +
                        "악성 프로그램 제거·원격 종료·기기 정리·상품권 잔액 조회·코드 사용을 하지 않습니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 긴급·목소리 사칭·번호 변경 범주에서는 별도 경로 확인·가족 " +
                        "콜백·목소리 현실 점검·새 번호 격리·안전단어 재정비·값 없는 전화 " +
                        "문구·가족 알림 경로를 고르며 연락처·안전단어·콜백 값을 사용하지 않습니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만으로 증거 전달 개인정보·증거 체크·" +
                        "후속 점검 경로를 더 보여 주며 사건 내용이나 미리보기는 읽지 않습니다."
                )
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 아직 진행 중인 범주라 안전 계획·안전단어 재정비·준비 " +
                    "로드맵·가족 연습·값 없는 전화 문구 경로를 직접 살펴보도록 권합니다. " +
                    "저장된 계획의 실제 값은 추천에 사용하지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있다는 범주는 기존 준비 항목이 채워졌다는 뜻일 뿐 " +
                    "상품권·교환권·코드·PIN·바코드·QR 요청이나 사람·기관·가족 위급 " +
                    "주장이 실제이거나 안전하다는 뜻이 아닙니다. 준비 상태와 관계없이 " +
                    "구매·공유·촬영·스캔·사용 행동을 멈추세요."
        }
        return listOf(situationCopy, readinessCopy).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 고정 문장을 직접 읽고 기존 앱 경로를 직접 고르는 로컬 전용·무상태 수동 안내이며 상품권·교환권·기프티콘·선불카드·코드·PIN·바코드·QR·일련번호·결제·계정·사건·사람 값을 입력하라고 요구하지 않습니다.",
        "추천은 현재 검사 결과의 위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기·가족 안전 계획의 실제 값을 사용하지 않습니다.",
        "상품권·교환권·기프티콘·선불카드·코드·PIN·바코드·QR·일련번호를 읽기·촬영·캡처·스캔·해독·검증·사용·잔액 조회·구매·저장·복사·업로드·첨부·전달하지 않습니다.",
        "상품권·결제·구매·취소·환급·차지백·동결·차단·회수·추적·회복을 수행·조회·제출·보장하지 않고 법률 서비스·자문·사건 판정이나 금융 결과를 제공하지 않습니다.",
        "지갑·블록체인·가상자산·거래소 추적, 결제·은행·계좌·카드 기능 또는 API·NFC를 사용하지 않고 송금·결제 처리·환급 청구·차지백 접수·코드 또는 계정 동결을 하지 않습니다.",
        "사람·기관·매장·상표·도우미·변호사·수사관·발신자 표시·전화번호·URL·도메인·프로필·신원·문서·사건·신고·계정·결제·지갑·기기·자격·회복 상태를 조회·검증·확인·판정하지 않습니다.",
        "기관 정보·공식 번호·디렉터리 값을 가져오지 않고 매장·상품 조회·공식 번호 조회·전화번호 검증·URL 검증·도메인 스캔·발신자 표시 확인·문서 검증·신고 제출을 하지 않습니다.",
        "네트워크·웹 탐색·브라우저·지도·위치·지오코딩을 사용하지 않고 전화·다이얼·발신·통화·종료·선별·차단·문자·SMS·받는 사람 선택·자동 전송을 하지 않습니다.",
        "연락처·주소록·통화 기록·통화 상태·문자·알림을 읽거나 감시하지 않고 연락처 읽기·쓰기·편집·선택과 알림 수신·필터링을 하지 않습니다.",
        "클립보드·텍스트 감시기·자동완성·자격 증명·계정·인증·생체 인증·키가드·키 저장소·암호화 API를 사용하지 않고 계정·로그인·인증 상태를 읽거나 바꾸지 않습니다.",
        "인증번호·PIN·비밀번호·보안 답·안전단어·콜백 문장·연락처 이름·번호·계정·카드·은행·지갑·거래·받는 사람 값을 읽기·표시·저장·복사·붙여넣기·자동 입력·공유·제출하지 않습니다.",
        "파일·문서·영수증·사진·이미지·스크린샷·오디오·미디어·저장소를 읽지 않고 카메라·화면 캡처·바코드·QR·OCR·이미지 분석·녹음·전사·파일 선택기·첨부·스트림·URI 공유를 사용하지 않습니다.",
        "수동 문자 공유는 고정 상품권·코드 카드와 기존 앱 경로·행동 종류만 담고 원문·코드·사건·계획·기관 목록·연락처·채널·계정·결제·지갑·기기·앱·파일·받는 사람·확인·제출·회복 결과를 담거나 자동 발송하지 않습니다.",
        "새 Android 권한을 요청하지 않고 알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러를 만들거나 예약하지 않습니다.",
        "새 저장소를 만들지 않고 카드 선택·추천·읽음·공유·체크·메모·담당자·받는 사람·구매·잔액·사용·환급·차지백·동결·취소·차단·회복·확인·신고·진행·상태·이력을 저장하지 않으며 가족 계획·사건·디렉터리·연락처·알림·통화·문자·계정·결제·기기 상태를 수정하지 않습니다.",
        "접근성 서비스·기기 관리자·VPN·사용 통계·앱 설치·제거·설정 변경·설치 앱·패키지 목록 조회·기기 제어를 사용하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<GiftCardCodePauseCardId>,
        recommendedActions: List<GiftCardCodePauseAction>,
        cards: List<GiftCardCodePauseCard>
    ): String {
        val cardById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[상품권·코드 멈춤 코치]")
            appendLine("상품권 구매와 코드·PIN·바코드·QR 공유를 멈추는 로컬 수동 점검입니다.")
            appendLine()
            appendLine("추천 상품권·코드 멈춤 카드")
            recommendedCardIds.forEach { cardId ->
                val card = cardById.getValue(cardId)
                appendLine("- ${card.title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 실제 상품권·교환권·코드·PIN·바코드·QR·일련번호·금액·계정·연락처·사건 값은 보내지 않습니다.")
            append("- 이 안내는 구매·스캔·사용·잔액 조회·취소·환급·차지백·동결·차단·회복·신고를 수행하거나 보장하지 않습니다.")
        }
    }

    private fun actionFor(id: GiftCardCodePauseActionId): GiftCardCodePauseAction =
        GiftCardCodePauseAction(id = id, label = actionLabels.getValue(id))

    private val actionLabels = mapOf(
        GiftCardCodePauseActionId.QUICK_STOP to "긴급 멈춤 모드",
        GiftCardCodePauseActionId.CHECKER to "사기 신호 검사 시작",
        GiftCardCodePauseActionId.ANALYSIS_RESULT to "현재 분석 결과",
        GiftCardCodePauseActionId.SAFETY_PLAN to "가족 안전 계획",
        GiftCardCodePauseActionId.FAMILY_READINESS_ROADMAP to "가족 준비 로드맵",
        GiftCardCodePauseActionId.FAMILY_SAFETY_DRILL_COACH to "가족 안전 연습 코치",
        GiftCardCodePauseActionId.SAFE_PASTE_PRIVACY_COACH to "안전 붙여넣기 개인정보 코치",
        GiftCardCodePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS to "진행 중 연락 종료 문구",
        GiftCardCodePauseActionId.FAMILY_CALLBACK_CARD to "가족 콜백 카드",
        GiftCardCodePauseActionId.FAMILY_SAFE_WORD_RESET_COACH to "가족 안전단어 재정비 코치",
        GiftCardCodePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH to "별도 경로 확인 코치",
        GiftCardCodePauseActionId.SAFE_CALL_SCRIPTS to "안전 확인 전화 문구",
        GiftCardCodePauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH to
            "목소리 사칭 현실 점검",
        GiftCardCodePauseActionId.NEW_NUMBER_QUARANTINE_COACH to "새 번호 격리 코치",
        GiftCardCodePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH to "인증 비밀값 멈춤 코치",
        GiftCardCodePauseActionId.FAMILY_ALERT_MESSAGES to "가족 알림 문구",
        GiftCardCodePauseActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD to
            "확인 결과 다음 단계 보드",
        GiftCardCodePauseActionId.RECONTACT_BOUNDARY_COACH to "재접촉 경계 코치",
        GiftCardCodePauseActionId.TRUSTED_HELPER_REVIEW to "신뢰 보호자 검토",
        GiftCardCodePauseActionId.FAMILY_RESPONSE_ROLE_BOARD to "가족 대응 역할 보드",
        GiftCardCodePauseActionId.FAMILY_CALM_DEBRIEF_AGENDA to "가족 차분 대화 순서",
        GiftCardCodePauseActionId.MONEY_TRANSFER_PAUSE_GATE to "송금 전 멈춤 게이트",
        GiftCardCodePauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH to
            "기관 사칭 압박 현실 점검",
        GiftCardCodePauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH to
            "회복 약속 현실 점검",
        GiftCardCodePauseActionId.ACCOUNT_LOCKDOWN to "계정·기기 잠금 체크리스트",
        GiftCardCodePauseActionId.REMOTE_ACCESS_CUTOFF_COACH to "원격지시 중단 코치",
        GiftCardCodePauseActionId.EXPOSURE_IMPACT_TRIAGE to "노출 영향 분류",
        GiftCardCodePauseActionId.PRIVACY_CENTER to "개인정보 센터",
        GiftCardCodePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH to
            "증거 전달 개인정보 코치",
        GiftCardCodePauseActionId.EVIDENCE_KIT to "증거 체크리스트",
        GiftCardCodePauseActionId.OFFICIAL_HELP_DIRECTORY to "공식 도움 경로",
        GiftCardCodePauseActionId.REPORT_PREP to "신고 준비 카드",
        GiftCardCodePauseActionId.FOLLOW_UP_CHECK_IN_PLANNER to "후속 점검 플래너",
        GiftCardCodePauseActionId.SAFETY_FLOW_ROUTER to "안전 대응 길잡이"
    )
}
