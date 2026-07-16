package com.mongmong.familyshield

enum class FamilySafetyDrillId(val displayOrder: Int) {
    FAMILY_NEW_NUMBER_MONEY(1),
    PROSECUTOR_SAFE_ACCOUNT(2),
    REMOTE_CONTROL_SCREEN_SHARE(3),
    PARCEL_INVITATION_SMISHING(4),
    OTP_PASSWORD_VERIFICATION(5),
    GIFT_CARD_CRYPTO_TRANSFER(6),
    OVERSEAS_PAYMENT_CANCEL(7),
    WEEKLY_PREVENTION_CHECKIN(8)
}

enum class FamilySafetyDrillDifficulty(val rank: Int, val label: String) {
    STARTER(0, "첫 연습"),
    INTERMEDIATE(1, "이어 연습"),
    ADVANCED(2, "압박 대응")
}

enum class FamilySafetyDrillUrgency(val rank: Int, val label: String) {
    STOP_NOW(0, "멈춤 우선"),
    VERIFY_FIRST(1, "직접 확인"),
    PREVENTION(2, "예방 점검")
}

enum class FamilySafetyDrillActionId {
    QUICK_STOP,
    CHECKER,
    ANALYSIS_RESULT,
    RISK_SIGNAL_EXPLAINERS,
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

data class FamilySafetyDrillAction(
    val id: FamilySafetyDrillActionId,
    val label: String
)

data class FamilySafetyDrillCard(
    val id: FamilySafetyDrillId,
    val displayOrder: Int,
    val scenario: String,
    val title: String,
    val fictionalSetup: String,
    val rolePlayLines: List<String>,
    val expectedStopPhrase: String,
    val responseSteps: List<String>,
    val passChecklist: List<String>,
    val familyReflectionPrompt: String,
    val difficulty: FamilySafetyDrillDifficulty,
    val urgency: FamilySafetyDrillUrgency,
    val primaryAction: FamilySafetyDrillAction,
    val secondaryActions: List<FamilySafetyDrillAction> = emptyList()
) {
    val primaryActionLabel: String
        get() = primaryAction.label

    val secondaryActionLabels: List<String>
        get() = secondaryActions.map { action -> action.label }
}

data class FamilySafetyDrillCoach(
    val title: String,
    val intro: String,
    val fictionNotice: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedDrillIds: List<FamilySafetyDrillId>,
    val recommendedActions: List<FamilySafetyDrillAction>,
    val cards: List<FamilySafetyDrillCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<FamilySafetyDrillCard>
        get() = recommendedDrillIds.mapNotNull { drillId ->
            cards.find { card -> card.id == drillId }
        }

    val recommendedActionIds: List<FamilySafetyDrillActionId>
        get() = recommendedActions.map { action -> action.id }
}

object FamilySafetyDrillCoachBuilder {
    const val FICTION_NOTICE =
        "모든 대사와 상황은 실제 사건이 아닌 가족 연습용 가상 시나리오입니다."

    private val familySignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.PHONE_NUMBER_CHANGE
    )

    private val safeAccountSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.IMPERSONATION,
        ScamSignal.BAIT,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.SAFE_ACCOUNT_FRAUD
    )

    private val financialSignals = safeAccountSignals + setOf(
        ScamSignal.CREDENTIALS,
        ScamSignal.GIFT_CRYPTO,
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

    private val pressureSignals = setOf(ScamSignal.SECRECY)

    private val defaultDrillIds = listOf(
        FamilySafetyDrillId.FAMILY_NEW_NUMBER_MONEY,
        FamilySafetyDrillId.OTP_PASSWORD_VERIFICATION,
        FamilySafetyDrillId.WEEKLY_PREVENTION_CHECKIN
    )

    fun build(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness
    ): FamilySafetyDrillCoach {
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val cards = buildCards(
            hasAnalysis = analysis != null,
            readinessLevel = readiness.level
        )
        val recommendedDrillIds = recommendDrillIds(signals, readiness.level)
        val recommendedActionIds = recommendActionIds(analysis, signals, readiness.level)
        val recommendedActions = recommendedActionIds.map { actionId ->
            actionFor(actionId, analysis != null, readiness.level)
        }
        val recommendationTitle = when {
            analysis == null -> "분석 전 가족 기본 연습"
            signals.isEmpty() -> "뚜렷한 신호가 없어도 해둘 기본 연습"
            else -> "현재 신호에 맞춘 가족 연습"
        }

        return FamilySafetyDrillCoach(
            title = "가족 안전 연습 코치",
            intro = "의심 연락을 받기 전이나 받은 뒤, 가족이 함께 멈춤 문구와 직접 확인 순서를 연습하도록 돕는 로컬 전용 코치입니다.",
            fictionNotice = FICTION_NOTICE,
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                analysis = analysis,
                signals = signals,
                readinessLevel = readiness.level
            ),
            recommendedDrillIds = recommendedDrillIds,
            recommendedActions = recommendedActions,
            cards = cards,
            privacySafetyNotes = privacySafetyNotes(),
            familyShareSummary = buildFamilyShareSummary(
                recommendedDrillIds = recommendedDrillIds,
                recommendedActions = recommendedActions,
                cards = cards
            )
        )
    }

    private fun buildCards(
        hasAnalysis: Boolean,
        readinessLevel: ReadinessLevel
    ): List<FamilySafetyDrillCard> {
        fun action(id: FamilySafetyDrillActionId): FamilySafetyDrillAction =
            actionFor(id, hasAnalysis, readinessLevel)

        fun resultAction(): FamilySafetyDrillAction = action(
            if (hasAnalysis) {
                FamilySafetyDrillActionId.ANALYSIS_RESULT
            } else {
                FamilySafetyDrillActionId.CHECKER
            }
        )

        return listOf(
            FamilySafetyDrillCard(
                id = FamilySafetyDrillId.FAMILY_NEW_NUMBER_MONEY,
                displayOrder = FamilySafetyDrillId.FAMILY_NEW_NUMBER_MONEY.displayOrder,
                scenario = "가족을 사칭한 새 번호의 급한 송금 요청",
                title = "가상 연습 · 가족 새 번호 송금 요청",
                fictionalSetup = "연습용 가상 상황: 가족을 사칭한 상대가 휴대전화가 고장 났다며 새 번호로 급한 비용을 대신 보내 달라고 재촉합니다. 실제 연락이나 개인정보가 아닙니다.",
                rolePlayLines = listOf(
                    "상대 역할(가상): 휴대전화가 고장 났어. 새 번호로만 답해 주고 급한 비용을 대신 보내 줘.",
                    "연습자 역할: 잠깐 멈출게요. 기존 가족 번호로 직접 확인하겠습니다."
                ),
                expectedStopPhrase = "잠깐 멈출게요. 기존 가족 번호로 직접 확인하겠습니다.",
                responseSteps = listOf(
                    "새 번호에는 답하거나 송금하지 않고 대화를 멈춥니다.",
                    "목소리나 말투만 믿지 않고 가족 안전 질문을 떠올립니다.",
                    "새 연락에서 받은 번호가 아닌 평소 알던 가족 번호로 직접 확인합니다.",
                    "확인이 끝날 때까지 돈과 비밀정보를 보내지 않습니다."
                ),
                passChecklist = listOf(
                    "새 번호와의 대화를 멈췄다",
                    "가족 안전 질문을 떠올렸다",
                    "기존 가족 번호로 직접 확인하기로 했다",
                    "확인 전 송금과 정보 제공을 보류했다"
                ),
                familyReflectionPrompt = "가족 번호가 바뀌었다는 연락이 오면 누구에게 어떤 순서로 다시 확인할지 함께 말해 보세요.",
                difficulty = FamilySafetyDrillDifficulty.STARTER,
                urgency = FamilySafetyDrillUrgency.VERIFY_FIRST,
                primaryAction = action(FamilySafetyDrillActionId.FAMILY_CALLBACK_CARD),
                secondaryActions = listOf(
                    action(FamilySafetyDrillActionId.FAMILY_ALERT_MESSAGES),
                    action(FamilySafetyDrillActionId.SAFE_CALL_SCRIPTS)
                )
            ),
            FamilySafetyDrillCard(
                id = FamilySafetyDrillId.PROSECUTOR_SAFE_ACCOUNT,
                displayOrder = FamilySafetyDrillId.PROSECUTOR_SAFE_ACCOUNT.displayOrder,
                scenario = "검찰·경찰을 사칭한 안전계좌 이체 압박",
                title = "가상 연습 · 검찰·경찰 안전계좌 압박",
                fictionalSetup = "연습용 가상 상황: 검찰·경찰을 사칭한 상대가 계좌가 사건에 연루됐다며 자금을 안전한 곳으로 옮기고 가족에게 알리지 말라고 압박합니다.",
                rolePlayLines = listOf(
                    "상대 역할(가상): 조사 중이니 통화를 유지하고 지금 자금을 안전계좌로 옮기세요.",
                    "연습자 역할: 이체하지 않겠습니다. 통화를 끝내고 공식 경로로 직접 확인하겠습니다."
                ),
                expectedStopPhrase = "이체하지 않겠습니다. 통화를 끝내고 공식 경로로 직접 확인하겠습니다.",
                responseSteps = listOf(
                    "통화 유지와 비밀 유지 압박을 거절하고 대화를 끝냅니다.",
                    "안전계좌라는 이름과 관계없이 상대가 지정한 곳으로 돈을 옮기지 않습니다.",
                    "그 연락에서 받은 경로가 아닌 평소 확인 가능한 공식 경로를 직접 찾습니다.",
                    "혼자 판단하지 않고 믿을 만한 가족이나 보호자에게 상황을 알립니다."
                ),
                passChecklist = listOf(
                    "통화 유지 압박을 거절했다",
                    "안전계좌 이체를 보류했다",
                    "공식 경로로 직접 확인하기로 했다",
                    "가족이나 보호자에게 알리기로 했다"
                ),
                familyReflectionPrompt = "기관을 자칭하는 연락을 받았을 때 사용할 공식 확인 경로와 도움받을 가족을 정해 보세요.",
                difficulty = FamilySafetyDrillDifficulty.ADVANCED,
                urgency = FamilySafetyDrillUrgency.STOP_NOW,
                primaryAction = action(FamilySafetyDrillActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(FamilySafetyDrillActionId.SAFE_CALL_SCRIPTS),
                    action(FamilySafetyDrillActionId.ACCOUNT_LOCKDOWN),
                    action(FamilySafetyDrillActionId.OFFICIAL_HELP_DIRECTORY),
                    action(FamilySafetyDrillActionId.RECOVERY_PLAN)
                )
            ),
            FamilySafetyDrillCard(
                id = FamilySafetyDrillId.REMOTE_CONTROL_SCREEN_SHARE,
                displayOrder = FamilySafetyDrillId.REMOTE_CONTROL_SCREEN_SHARE.displayOrder,
                scenario = "보안 점검을 내세운 앱 설치와 화면공유 압박",
                title = "가상 연습 · 원격제어 앱과 화면공유 압박",
                fictionalSetup = "연습용 가상 상황: 고객 지원을 사칭한 상대가 보안 점검에 필요하다며 앱 설치와 화면공유를 켜고 통화를 유지하라고 요구합니다.",
                rolePlayLines = listOf(
                    "상대 역할(가상): 확인이 끝날 때까지 통화를 끊지 말고 안내하는 앱과 화면공유를 켜 주세요.",
                    "연습자 역할: 설치와 화면공유를 하지 않겠습니다. 지금 연결을 끝내겠습니다."
                ),
                expectedStopPhrase = "설치와 화면공유를 하지 않겠습니다. 지금 연결을 끝내겠습니다.",
                responseSteps = listOf(
                    "앱 설치와 화면공유 요청을 거절합니다.",
                    "통화를 유지하라는 말과 관계없이 연결을 끝냅니다.",
                    "이미 허용했다면 다른 안전한 기기에서 계정 보호와 증거 보존 순서를 확인합니다.",
                    "혼자 제거를 단정하지 않고 믿을 만한 보호자와 공식 지원 경로에 도움을 요청합니다."
                ),
                passChecklist = listOf(
                    "앱 설치를 거절했다",
                    "화면공유와 통화를 끝내기로 했다",
                    "안전한 기기에서 보호 순서를 확인하기로 했다",
                    "믿을 만한 도움 경로를 골랐다"
                ),
                familyReflectionPrompt = "앱이나 화면공유를 요구받으면 가장 먼저 연결할 보호자와 안전한 기기를 정해 보세요.",
                difficulty = FamilySafetyDrillDifficulty.ADVANCED,
                urgency = FamilySafetyDrillUrgency.STOP_NOW,
                primaryAction = action(FamilySafetyDrillActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(FamilySafetyDrillActionId.EVIDENCE_KIT),
                    action(FamilySafetyDrillActionId.TRUSTED_HELPER_HANDOFF)
                )
            ),
            FamilySafetyDrillCard(
                id = FamilySafetyDrillId.PARCEL_INVITATION_SMISHING,
                displayOrder = FamilySafetyDrillId.PARCEL_INVITATION_SMISHING.displayOrder,
                scenario = "택배·초대 알림을 가장한 스미싱 링크",
                title = "가상 연습 · 택배와 초대 스미싱 링크",
                fictionalSetup = "연습용 가상 상황: 예상하지 못한 택배나 초대 알림이 도착해 주소 또는 참석 여부를 확인하라며 메시지 속 링크를 누르게 합니다. 실제 링크는 없습니다.",
                rolePlayLines = listOf(
                    "상대 역할(가상): 배송이나 초대 확인이 늦어지기 전에 메시지 속 링크를 지금 열어 주세요.",
                    "연습자 역할: 링크는 열지 않겠습니다. 평소 쓰던 공식 경로에서 직접 확인하겠습니다."
                ),
                expectedStopPhrase = "링크는 열지 않겠습니다. 평소 쓰던 공식 경로에서 직접 확인하겠습니다.",
                responseSteps = listOf(
                    "메시지 속 링크를 누르거나 다시 열지 않습니다.",
                    "택배나 초대 사실은 평소 쓰던 공식 앱 또는 알고 있던 사람에게 다른 경로로 확인합니다.",
                    "정보 입력이나 파일 실행을 하지 않습니다.",
                    "이미 누른 행동이 있다면 증거 보존과 가족 알림 경로를 확인합니다."
                ),
                passChecklist = listOf(
                    "메시지 속 링크를 열지 않았다",
                    "평소 쓰던 공식 경로를 선택했다",
                    "정보 입력과 파일 실행을 보류했다",
                    "증거 보존과 가족 알림 위치를 확인했다"
                ),
                familyReflectionPrompt = "가족 채팅에서 링크를 받아도 다른 경로로 먼저 확인하는 규칙을 함께 정해 보세요.",
                difficulty = FamilySafetyDrillDifficulty.INTERMEDIATE,
                urgency = FamilySafetyDrillUrgency.VERIFY_FIRST,
                primaryAction = action(FamilySafetyDrillActionId.EVIDENCE_KIT),
                secondaryActions = listOf(
                    action(FamilySafetyDrillActionId.FAMILY_ALERT_MESSAGES),
                    action(FamilySafetyDrillActionId.RISK_SIGNAL_EXPLAINERS)
                )
            ),
            FamilySafetyDrillCard(
                id = FamilySafetyDrillId.OTP_PASSWORD_VERIFICATION,
                displayOrder = FamilySafetyDrillId.OTP_PASSWORD_VERIFICATION.displayOrder,
                scenario = "계정 확인을 가장한 OTP·비밀번호 요청",
                title = "가상 연습 · OTP·비밀번호·계정 확인 요청",
                fictionalSetup = "연습용 가상 상황: 계정 확인을 돕는다고 주장하는 상대가 비밀번호, 일회용 인증정보(OTP) 또는 보안수단 내용을 말해 달라고 요구합니다.",
                rolePlayLines = listOf(
                    "상대 역할(가상): 본인 확인을 마치려면 방금 받은 인증정보와 비밀번호를 불러 주세요.",
                    "연습자 역할: 비밀정보는 말하지 않겠습니다. 공식 앱에서 직접 확인하겠습니다."
                ),
                expectedStopPhrase = "비밀정보는 말하지 않겠습니다. 공식 앱에서 직접 확인하겠습니다.",
                responseSteps = listOf(
                    "비밀번호와 인증정보를 말하거나 보내지 않습니다.",
                    "상대가 재촉해도 통화나 답장을 끝냅니다.",
                    "평소 쓰던 공식 앱에서 계정 알림과 상태를 직접 확인합니다.",
                    "이미 공유했다면 다른 안전한 기기에서 계정 보호 순서를 확인합니다."
                ),
                passChecklist = listOf(
                    "비밀번호와 인증정보 제공을 거절했다",
                    "재촉하는 대화를 끝냈다",
                    "공식 앱에서 직접 확인하기로 했다",
                    "노출됐다면 안전한 기기에서 보호하기로 했다"
                ),
                familyReflectionPrompt = "가족끼리도 비밀번호와 인증정보를 묻거나 대신 전달하지 않는 원칙을 말해 보세요.",
                difficulty = FamilySafetyDrillDifficulty.STARTER,
                urgency = FamilySafetyDrillUrgency.STOP_NOW,
                primaryAction = action(FamilySafetyDrillActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(FamilySafetyDrillActionId.SAFE_CALL_SCRIPTS),
                    action(FamilySafetyDrillActionId.QUICK_STOP)
                )
            ),
            FamilySafetyDrillCard(
                id = FamilySafetyDrillId.GIFT_CARD_CRYPTO_TRANSFER,
                displayOrder = FamilySafetyDrillId.GIFT_CARD_CRYPTO_TRANSFER.displayOrder,
                scenario = "상품권·기프티콘·가상자산 전달 압박",
                title = "가상 연습 · 상품권·기프티콘·가상자산 압박",
                fictionalSetup = "연습용 가상 상황: 상대가 수수료나 긴급 비용을 대신 내야 한다며 상품권, 기프티콘 또는 가상자산으로 가치를 보내 달라고 재촉합니다.",
                rolePlayLines = listOf(
                    "상대 역할(가상): 계좌 대신 상품권이나 가상자산으로 바로 보내면 처리할 수 있어요.",
                    "연습자 역할: 구매하거나 전달하지 않겠습니다. 먼저 가족과 공식 경로에 확인하겠습니다."
                ),
                expectedStopPhrase = "구매하거나 전달하지 않겠습니다. 먼저 가족과 공식 경로에 확인하겠습니다.",
                responseSteps = listOf(
                    "상품권 구매와 기프티콘·가상자산 전달을 멈춥니다.",
                    "번호나 화면을 촬영해 보내라는 요구도 거절합니다.",
                    "평소 가족 번호나 공식 상담 경로로 요청 사실을 확인합니다.",
                    "이미 전달했다면 추가 요구를 따르지 않고 기존 도움 화면을 확인합니다."
                ),
                passChecklist = listOf(
                    "구매와 전달을 멈췄다",
                    "비밀 번호나 화면 공유를 거절했다",
                    "가족과 공식 경로에 확인하기로 했다",
                    "추가 요구를 따르지 않기로 했다"
                ),
                familyReflectionPrompt = "가족을 자칭해도 상품권이나 가상자산 요청은 먼저 확인한다는 규칙을 정해 보세요.",
                difficulty = FamilySafetyDrillDifficulty.INTERMEDIATE,
                urgency = FamilySafetyDrillUrgency.STOP_NOW,
                primaryAction = action(FamilySafetyDrillActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(FamilySafetyDrillActionId.SAFE_CALL_SCRIPTS),
                    action(FamilySafetyDrillActionId.OFFICIAL_HELP_DIRECTORY)
                )
            ),
            FamilySafetyDrillCard(
                id = FamilySafetyDrillId.OVERSEAS_PAYMENT_CANCEL,
                displayOrder = FamilySafetyDrillId.OVERSEAS_PAYMENT_CANCEL.displayOrder,
                scenario = "낯선 해외 결제 알림과 취소 상담 미끼",
                title = "가상 연습 · 해외 카드 결제 취소 미끼",
                fictionalSetup = "연습용 가상 상황: 낯선 해외 결제가 승인됐다는 알림 뒤에 즉시 취소를 도와준다며 카드정보나 인증 절차를 요구합니다.",
                rolePlayLines = listOf(
                    "상대 역할(가상): 결제를 취소하려면 지금 카드정보와 인증 절차를 진행해야 합니다.",
                    "연습자 역할: 이 연락에서는 진행하지 않겠습니다. 평소 카드사 공식 경로로 확인하겠습니다."
                ),
                expectedStopPhrase = "이 연락에서는 진행하지 않겠습니다. 평소 카드사 공식 경로로 확인하겠습니다.",
                responseSteps = listOf(
                    "알림 속 링크나 상담 경로를 사용하지 않습니다.",
                    "카드정보와 인증정보를 말하거나 입력하지 않습니다.",
                    "평소 쓰던 카드사 공식 앱이나 카드에 적힌 경로에서 거래를 직접 확인합니다.",
                    "실제 모르는 거래가 보이면 공식 상담 절차를 사용자가 직접 진행합니다."
                ),
                passChecklist = listOf(
                    "알림 속 경로를 사용하지 않았다",
                    "카드정보와 인증정보 제공을 거절했다",
                    "카드사 공식 경로에서 직접 확인하기로 했다",
                    "공식 상담 절차를 직접 선택하기로 했다"
                ),
                familyReflectionPrompt = "낯선 결제 알림을 받았을 때 확인할 평소 카드사 경로를 가족과 함께 찾아보세요.",
                difficulty = FamilySafetyDrillDifficulty.INTERMEDIATE,
                urgency = FamilySafetyDrillUrgency.VERIFY_FIRST,
                primaryAction = action(FamilySafetyDrillActionId.SAFE_CALL_SCRIPTS),
                secondaryActions = listOf(
                    action(FamilySafetyDrillActionId.ACCOUNT_LOCKDOWN),
                    action(FamilySafetyDrillActionId.OFFICIAL_HELP_DIRECTORY)
                )
            ),
            FamilySafetyDrillCard(
                id = FamilySafetyDrillId.WEEKLY_PREVENTION_CHECKIN,
                displayOrder = FamilySafetyDrillId.WEEKLY_PREVENTION_CHECKIN.displayOrder,
                scenario = "가족이 함께하는 주간 예방 점검",
                title = "가상 연습 · 이번 주 가족 예방 점검",
                fictionalSetup = "연습용 가상 상황: 가족 모임에서 예상하지 못한 돈·비밀정보·앱·링크 요청이 왔다고 가정하고 서로의 멈춤 문구와 확인 순서를 점검합니다.",
                rolePlayLines = listOf(
                    "진행자 역할(가상): 급한 요청이 왔다고 가정하고 우리 가족의 첫 문장을 말해 볼까요?",
                    "연습자 역할: 지금은 멈추고, 평소 알던 가족 번호와 공식 경로로 확인하겠습니다."
                ),
                expectedStopPhrase = "지금은 멈추고, 평소 알던 가족 번호와 공식 경로로 확인하겠습니다.",
                responseSteps = listOf(
                    "가족 모두가 같은 멈춤 문구를 한 번씩 말합니다.",
                    "기존 가족 번호와 공식 확인 경로를 어디에서 찾을지 말합니다.",
                    "돈·비밀정보·앱·링크 요청을 확인 전 보류한다는 원칙을 확인합니다.",
                    "부족한 준비 항목은 가족 안전 계획에서 사용자가 직접 보완합니다."
                ),
                passChecklist = listOf(
                    "가족 멈춤 문구를 함께 말했다",
                    "기존 가족 번호 확인 순서를 설명했다",
                    "확인 전 보류 원칙을 확인했다",
                    "다음 예방 점검 행동을 골랐다"
                ),
                familyReflectionPrompt = "이번 주에 한 번 더 연습할 상황과 서로 도와줄 역할을 정해 보세요.",
                difficulty = FamilySafetyDrillDifficulty.STARTER,
                urgency = FamilySafetyDrillUrgency.PREVENTION,
                primaryAction = action(FamilySafetyDrillActionId.SAFETY_PLAN),
                secondaryActions = listOf(
                    resultAction(),
                    action(FamilySafetyDrillActionId.SAFETY_FLOW_ROUTER),
                    action(FamilySafetyDrillActionId.RISK_SIGNAL_EXPLAINERS)
                )
            )
        )
    }

    private fun recommendDrillIds(
        signals: Set<ScamSignal>,
        readinessLevel: ReadinessLevel
    ): List<FamilySafetyDrillId> {
        if (signals.isEmpty()) return defaultDrillIds

        val recommended = linkedSetOf<FamilySafetyDrillId>()
        if (signals.any { signal -> signal in familySignals }) {
            recommended += FamilySafetyDrillId.FAMILY_NEW_NUMBER_MONEY
        }
        if (signals.any { signal -> signal in safeAccountSignals }) {
            recommended += FamilySafetyDrillId.PROSECUTOR_SAFE_ACCOUNT
        }
        if (ScamSignal.CREDENTIALS in signals) {
            recommended += FamilySafetyDrillId.OTP_PASSWORD_VERIFICATION
        }
        if (signals.any { signal -> signal == ScamSignal.GIFT_CRYPTO || signal == ScamSignal.GIFTICON_PIN }) {
            recommended += FamilySafetyDrillId.GIFT_CARD_CRYPTO_TRANSFER
        }
        if (ScamSignal.OVERSEAS_PAYMENT_ALERT in signals) {
            recommended += FamilySafetyDrillId.OVERSEAS_PAYMENT_CANCEL
        }
        if (signals.any { signal -> signal in remoteSignals }) {
            recommended += FamilySafetyDrillId.REMOTE_CONTROL_SCREEN_SHARE
        }
        if (signals.any { signal -> signal in linkSignals }) {
            recommended += FamilySafetyDrillId.PARCEL_INVITATION_SMISHING
        }
        if (recommended.isEmpty()) {
            recommended += defaultDrillIds
        }
        if (readinessLevel != ReadinessLevel.READY) {
            recommended += FamilySafetyDrillId.WEEKLY_PREVENTION_CHECKIN
        }
        return recommended.toList()
    }

    private fun recommendActionIds(
        analysis: ScamAnalysis?,
        signals: Set<ScamSignal>,
        readinessLevel: ReadinessLevel
    ): List<FamilySafetyDrillActionId> {
        val recommended = linkedSetOf<FamilySafetyDrillActionId>()
        if (analysis?.riskLevel == RiskLevel.HIGH) {
            recommended += FamilySafetyDrillActionId.QUICK_STOP
            recommended += FamilySafetyDrillActionId.RECOVERY_PLAN
        }
        if (signals.isEmpty()) {
            recommended += FamilySafetyDrillActionId.CHECKER
            recommended += FamilySafetyDrillActionId.SAFETY_PLAN
            return recommended.toList()
        }
        if (signals.any { signal -> signal in familySignals }) {
            recommended += FamilySafetyDrillActionId.FAMILY_CALLBACK_CARD
            recommended += FamilySafetyDrillActionId.FAMILY_ALERT_MESSAGES
            recommended += FamilySafetyDrillActionId.SAFE_CALL_SCRIPTS
        }
        if (signals.any { signal -> signal in financialSignals }) {
            recommended += FamilySafetyDrillActionId.QUICK_STOP
            recommended += FamilySafetyDrillActionId.SAFE_CALL_SCRIPTS
            recommended += FamilySafetyDrillActionId.ACCOUNT_LOCKDOWN
            recommended += FamilySafetyDrillActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (signals.any { signal -> signal in remoteSignals }) {
            recommended += FamilySafetyDrillActionId.ACCOUNT_LOCKDOWN
            recommended += FamilySafetyDrillActionId.EVIDENCE_KIT
            recommended += FamilySafetyDrillActionId.TRUSTED_HELPER_HANDOFF
        }
        if (signals.any { signal -> signal in linkSignals }) {
            recommended += FamilySafetyDrillActionId.EVIDENCE_KIT
            recommended += FamilySafetyDrillActionId.FAMILY_ALERT_MESSAGES
            recommended += FamilySafetyDrillActionId.RISK_SIGNAL_EXPLAINERS
        }
        if (signals.any { signal -> signal in pressureSignals }) {
            recommended += FamilySafetyDrillActionId.QUICK_STOP
            recommended += FamilySafetyDrillActionId.SAFE_CALL_SCRIPTS
            recommended += FamilySafetyDrillActionId.FAMILY_ALERT_MESSAGES
        }
        if (readinessLevel != ReadinessLevel.READY) {
            recommended += FamilySafetyDrillActionId.SAFETY_PLAN
        }
        if (recommended.isEmpty()) {
            recommended += FamilySafetyDrillActionId.ANALYSIS_RESULT
            recommended += FamilySafetyDrillActionId.SAFETY_FLOW_ROUTER
        }
        return recommended.toList()
    }

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        signals: Set<ScamSignal>,
        readinessLevel: ReadinessLevel
    ): String {
        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW -> "가족 준비가 아직 부족해도 괜찮습니다. 주간 점검과 안전 계획부터 차분히 연습하세요."
            ReadinessLevel.IN_PROGRESS -> "가족 준비가 진행 중입니다. 주간 점검에서 빠진 확인 순서를 차분히 보완하세요."
            ReadinessLevel.READY -> "가족 확인 루틴이 준비되어 있습니다. 저장된 비밀값을 드러내지 않고 멈춤과 직접 확인 순서를 연습하세요."
        }
        if (signals.isEmpty()) {
            val context = if (analysis == null) {
                "현재 분석이 없어 새 번호 가족 사칭, 비밀정보 요청, 주간 예방 점검을 기본 과제로 추천합니다."
            } else {
                "뚜렷하게 일치한 신호가 없어도 안전을 단정하지 않고 같은 기본 과제를 추천합니다."
            }
            return "$context $readinessCopy"
        }

        val situationCopy = buildList {
            if (signals.any { signal -> signal in familySignals }) {
                add("가족·목소리·번호 변경 신호는 새 번호 송금 요청 연습을 먼저 권합니다.")
            }
            if (signals.any { signal -> signal in financialSignals }) {
                add("송금·안전계좌·인증·상품권·결제 신호는 해당 금융 압박 연습과 기존 보호 경로를 권합니다.")
            }
            if (signals.any { signal -> signal in remoteSignals }) {
                add("앱·화면공유 신호는 원격제어 압박을 끊는 연습을 권합니다.")
            }
            if (signals.any { signal -> signal in linkSignals }) {
                add("링크·스미싱 신호는 링크를 열지 않는 연습을 권합니다.")
            }
            if (signals.any { signal -> signal in pressureSignals }) {
                add("비밀 유지 압박 신호는 대화를 멈추고 믿을 만한 가족과 확인하는 기본 연습을 권합니다.")
            }
            if (isEmpty()) {
                add("현재 신호와 가까운 기본 연습을 추천합니다.")
            }
        }.joinToString(" ")
        return "$situationCopy $readinessCopy"
    }

    private fun actionFor(
        id: FamilySafetyDrillActionId,
        hasAnalysis: Boolean,
        readinessLevel: ReadinessLevel
    ): FamilySafetyDrillAction = FamilySafetyDrillAction(
        id = id,
        label = when (id) {
            FamilySafetyDrillActionId.QUICK_STOP -> "긴급 멈춤 모드"
            FamilySafetyDrillActionId.CHECKER -> if (hasAnalysis) {
                "다른 의심 내용 직접 분석하기"
            } else {
                "의심 내용 직접 분석하기"
            }
            FamilySafetyDrillActionId.ANALYSIS_RESULT -> "현재 분석 결과 보기"
            FamilySafetyDrillActionId.RISK_SIGNAL_EXPLAINERS -> "위험 신호 설명 보기"
            FamilySafetyDrillActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 확인 카드"
            FamilySafetyDrillActionId.SAFETY_PLAN -> when (readinessLevel) {
                ReadinessLevel.LOW -> "가족 안전 계획 만들기"
                ReadinessLevel.IN_PROGRESS -> "가족 안전 계획 보완하기"
                ReadinessLevel.READY -> "가족 안전 계획 확인하기"
            }
            FamilySafetyDrillActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
            FamilySafetyDrillActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
            FamilySafetyDrillActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            FamilySafetyDrillActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처 보기"
            FamilySafetyDrillActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
            FamilySafetyDrillActionId.TRUSTED_HELPER_HANDOFF -> "보호자 도움 요청 카드"
            FamilySafetyDrillActionId.SAFE_CALL_SCRIPTS -> "안전 확인 전화 스크립트"
            FamilySafetyDrillActionId.RECOVERY_PLAN -> "24시간 수습 계획"
        }
    )

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 사용자가 직접 여는 수동 연습 안내이며, 모든 대사는 실제 사건이 아닌 앱에 내장된 가상 시나리오입니다.",
        "현재 분석의 신호와 값 없는 가족 준비도만 기기 안의 로컬 규칙으로 사용하며, 붙여넣은 원문이나 안전 계획 값을 표시하지 않고 네트워크를 사용하지 않습니다.",
        "문자·통화·연락처를 자동으로 읽거나 감시하지 않고 가족을 자동 모니터링하지 않습니다.",
        "선택한 드릴, 통과 여부, 체크 상태, 점수, 읽음·진행 상태, 공유 이력, 연습 기록을 저장하지 않습니다.",
        "화면을 열거나 버튼을 누르는 것만으로 사건 기록을 추가·수정·삭제하지 않습니다.",
        "공유 버튼은 연락처를 읽거나 받는 사람을 고르지 않습니다. 사용자가 공유 앱과 대상을 직접 선택하며 앱이 자동으로 보내지 않습니다.",
        "새 Android 권한을 요청하지 않고, 전화를 자동으로 걸거나 메시지를 자동 전송하지 않습니다.",
        "이 연습은 AI 모델을 학습시키지 않으며 사기 예방을 보장하지 않습니다. 통화·문자 차단, 악성 앱 제거, 계좌·카드 정지, 피해금 회수 또는 법률 결과를 보장하는 기능이 아닙니다."
    )

    private fun buildFamilyShareSummary(
        recommendedDrillIds: List<FamilySafetyDrillId>,
        recommendedActions: List<FamilySafetyDrillAction>,
        cards: List<FamilySafetyDrillCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        val allActions = cards
            .flatMap { card -> listOf(card.primaryAction) + card.secondaryActions }
            .plus(recommendedActions)
            .distinctBy { action -> action.label }

        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 가상 연습 과제")
            appendLine()
            appendLine("추천 가상 시나리오 제목")
            recommendedDrillIds.forEach { drillId ->
                appendLine("- ${cardsById.getValue(drillId).title}")
            }
            appendLine()
            appendLine("가상 시나리오별 멈춤 문구와 통과 체크")
            cards.forEach { card ->
                appendLine("- ${card.title}")
                appendLine("  멈춤 문구: ${card.expectedStopPhrase}")
                card.passChecklist.forEach { checklistLabel ->
                    appendLine("  통과 체크: $checklistLabel")
                }
            }
            appendLine()
            appendLine("기존 앱 행동")
            allActions.forEach { action ->
                appendLine("- ${action.label}")
            }
        }.trim()
    }
}
