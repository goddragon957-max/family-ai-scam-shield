package com.mongmong.familyshield

enum class OfficialPressureRealityCheckCardId(val displayOrder: Int) {
    TITLE_CHANNEL_AND_DOCUMENT_ARE_NOT_PROOF(1),
    BREAK_SECRECY_AND_PRESSURE(2),
    PAUSE_MONEY_BEFORE_ANY_CHECK(3),
    SEPARATE_CREDENTIAL_APP_AND_SCREEN_REQUESTS(4),
    FIND_A_PUBLIC_CHANNEL_LATER(5),
    PREPARE_VALUE_FREE_EVIDENCE_AND_REPORT(6)
}

enum class OfficialPressureRealityCheckActionId {
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

data class OfficialPressureRealityCheckAction(
    val id: OfficialPressureRealityCheckActionId,
    val label: String
)

data class OfficialPressureRealityCheckCard(
    val id: OfficialPressureRealityCheckCardId,
    val displayOrder: Int,
    val title: String,
    val pressureScenario: String,
    val valueFreeActionSentence: String,
    val whyItMatters: String,
    val doNotCallLookupShareOrSubmitBoundaries: List<String>,
    val primaryAction: OfficialPressureRealityCheckAction,
    val secondaryActions: List<OfficialPressureRealityCheckAction>
) {
    val routeActions: List<OfficialPressureRealityCheckAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class OfficialPressureRealityCheckCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<OfficialPressureRealityCheckCardId>,
    val recommendedActions: List<OfficialPressureRealityCheckAction>,
    val cards: List<OfficialPressureRealityCheckCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<OfficialPressureRealityCheckCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<OfficialPressureRealityCheckActionId>
        get() = recommendedActions.map { action -> action.id }
}

object OfficialPressureRealityCheckCoachBuilder {
    private val officialPressureSignals = setOf(
        ScamSignal.IMPERSONATION,
        ScamSignal.SECRECY
    )

    private val financialSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.BAIT,
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
    ): OfficialPressureRealityCheckCoach {
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val hasNoAnalysisOrSignals = analysis == null || signals.isEmpty()
        val hasHighRisk = analysis?.riskLevel == RiskLevel.HIGH
        val hasOfficialPressureSignal = signals.any { signal ->
            signal in officialPressureSignals
        }
        val hasFinancialSignal = signals.any { signal -> signal in financialSignals }
        val hasCredentialSignal = signals.any { signal -> signal in credentialSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasRemoteAccessSignal = signals.any { signal -> signal in remoteAccessSignals }
        val hasFamilyVerificationSignal = signals.any { signal ->
            signal in familyVerificationSignals
        }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0

        val recommendedCardSet = linkedSetOf(
            OfficialPressureRealityCheckCardId.TITLE_CHANNEL_AND_DOCUMENT_ARE_NOT_PROOF,
            OfficialPressureRealityCheckCardId.BREAK_SECRECY_AND_PRESSURE
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += OfficialPressureRealityCheckCardId.entries
        }
        if (hasOfficialPressureSignal) {
            recommendedCardSet +=
                OfficialPressureRealityCheckCardId.TITLE_CHANNEL_AND_DOCUMENT_ARE_NOT_PROOF
            recommendedCardSet +=
                OfficialPressureRealityCheckCardId.BREAK_SECRECY_AND_PRESSURE
            recommendedCardSet +=
                OfficialPressureRealityCheckCardId.FIND_A_PUBLIC_CHANNEL_LATER
            recommendedCardSet +=
                OfficialPressureRealityCheckCardId.PREPARE_VALUE_FREE_EVIDENCE_AND_REPORT
        }
        if (hasFinancialSignal) {
            recommendedCardSet += OfficialPressureRealityCheckCardId.PAUSE_MONEY_BEFORE_ANY_CHECK
            recommendedCardSet +=
                OfficialPressureRealityCheckCardId.SEPARATE_CREDENTIAL_APP_AND_SCREEN_REQUESTS
            recommendedCardSet +=
                OfficialPressureRealityCheckCardId.FIND_A_PUBLIC_CHANNEL_LATER
        }
        if (hasCredentialSignal) {
            recommendedCardSet +=
                OfficialPressureRealityCheckCardId.SEPARATE_CREDENTIAL_APP_AND_SCREEN_REQUESTS
            recommendedCardSet += OfficialPressureRealityCheckCardId.PAUSE_MONEY_BEFORE_ANY_CHECK
        }
        if (hasLinkSignal || hasRemoteAccessSignal) {
            recommendedCardSet +=
                OfficialPressureRealityCheckCardId.TITLE_CHANNEL_AND_DOCUMENT_ARE_NOT_PROOF
            recommendedCardSet +=
                OfficialPressureRealityCheckCardId.SEPARATE_CREDENTIAL_APP_AND_SCREEN_REQUESTS
            recommendedCardSet +=
                OfficialPressureRealityCheckCardId.FIND_A_PUBLIC_CHANNEL_LATER
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet +=
                OfficialPressureRealityCheckCardId.TITLE_CHANNEL_AND_DOCUMENT_ARE_NOT_PROOF
            recommendedCardSet +=
                OfficialPressureRealityCheckCardId.BREAK_SECRECY_AND_PRESSURE
            recommendedCardSet +=
                OfficialPressureRealityCheckCardId.FIND_A_PUBLIC_CHANNEL_LATER
        }
        if (hasSavedIncident) {
            recommendedCardSet +=
                OfficialPressureRealityCheckCardId.PREPARE_VALUE_FREE_EVIDENCE_AND_REPORT
        }
        when (readiness.level) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedCardSet +=
                    OfficialPressureRealityCheckCardId.BREAK_SECRECY_AND_PRESSURE
                recommendedCardSet +=
                    OfficialPressureRealityCheckCardId.FIND_A_PUBLIC_CHANNEL_LATER
            }
            ReadinessLevel.READY -> {
                recommendedCardSet +=
                    OfficialPressureRealityCheckCardId.TITLE_CHANNEL_AND_DOCUMENT_ARE_NOT_PROOF
                recommendedCardSet +=
                    OfficialPressureRealityCheckCardId.FIND_A_PUBLIC_CHANNEL_LATER
            }
        }
        val recommendedCardIds = OfficialPressureRealityCheckCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            OfficialPressureRealityCheckActionId.CHECKER
        } else {
            OfficialPressureRealityCheckActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            currentResultActionId,
            OfficialPressureRealityCheckActionId.QUICK_STOP,
            OfficialPressureRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
            OfficialPressureRealityCheckActionId.OFFICIAL_HELP_DIRECTORY,
            OfficialPressureRealityCheckActionId.REPORT_PREP,
            OfficialPressureRealityCheckActionId.PRIVACY_CENTER,
            OfficialPressureRealityCheckActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            OfficialPressureRealityCheckActionId.entries
                .filterNot { actionId ->
                    actionId == OfficialPressureRealityCheckActionId.ANALYSIS_RESULT
                }
                .forEach { actionId -> recommendedActionSet += actionId }
        }
        if (hasHighRisk) {
            OfficialPressureRealityCheckActionId.entries.forEach { actionId ->
                recommendedActionSet += actionId
            }
        }
        if (hasOfficialPressureSignal) {
            recommendedActionSet += OfficialPressureRealityCheckActionId.QUICK_STOP
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet += OfficialPressureRealityCheckActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet += OfficialPressureRealityCheckActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += OfficialPressureRealityCheckActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += OfficialPressureRealityCheckActionId.REPORT_PREP
        }
        if (hasFinancialSignal) {
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet += OfficialPressureRealityCheckActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += OfficialPressureRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE
        }
        if (hasCredentialSignal) {
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet += OfficialPressureRealityCheckActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet += OfficialPressureRealityCheckActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += OfficialPressureRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE
        }
        if (hasLinkSignal || hasRemoteAccessSignal) {
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += OfficialPressureRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += OfficialPressureRealityCheckActionId.PRIVACY_CENTER
            recommendedActionSet += OfficialPressureRealityCheckActionId.SAFE_PASTE_PRIVACY_COACH
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += OfficialPressureRealityCheckActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.NEW_NUMBER_QUARANTINE_COACH
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH
            recommendedActionSet += OfficialPressureRealityCheckActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += OfficialPressureRealityCheckActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasSavedIncident) {
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += OfficialPressureRealityCheckActionId.EVIDENCE_KIT
            recommendedActionSet +=
                OfficialPressureRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        when (readiness.level) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedActionSet += OfficialPressureRealityCheckActionId.SAFETY_PLAN
                recommendedActionSet +=
                    OfficialPressureRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH
                recommendedActionSet +=
                    OfficialPressureRealityCheckActionId.FAMILY_READINESS_ROADMAP
                recommendedActionSet +=
                    OfficialPressureRealityCheckActionId.FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += OfficialPressureRealityCheckActionId.SAFE_CALL_SCRIPTS
            }
            ReadinessLevel.READY -> {
                recommendedActionSet += OfficialPressureRealityCheckActionId.FAMILY_CALLBACK_CARD
                recommendedActionSet +=
                    OfficialPressureRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                recommendedActionSet += OfficialPressureRealityCheckActionId.SAFE_CALL_SCRIPTS
            }
        }
        val recommendedActions = OfficialPressureRealityCheckActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null -> "검사 결과가 없어도 모든 기관 사칭 압박 카드를 보수적으로 읽기"
            hasOfficialPressureSignal -> "직함·기관명·문서·발신 표시를 근거로 복종하지 않기"
            hasFinancialSignal -> "안전계좌·환급·비용·결제 말보다 돈 행동 먼저 멈추기"
            hasCredentialSignal -> "인증 비밀값·계정·기기 요구를 별도 멈춤 경로로 분리하기"
            hasRemoteAccessSignal -> "같은 앱·통화·화면공유 흐름을 벗어나기"
            hasLinkSignal -> "상대가 준 링크 밖에서 나중에 공개 경로를 직접 찾기"
            hasFamilyVerificationSignal -> "익숙한 관계나 목소리도 별도 가족 경로로 다시 보기"
            hasHighRisk -> "높은 위험 범주에서 값 없는 대응 경로를 넓게 고르기"
            hasSavedIncident -> "사건 내용 없이 증거·신고 준비 경로만 고르기"
            else -> "압박에 응답하기 전에 멈추고 별도 경로를 고르기"
        }

        return OfficialPressureRealityCheckCoach(
            title = "기관 사칭 압박 현실 점검 코치",
            intro =
                "경찰·수사·금융·배송·플랫폼 같은 직함이나 기관명, 발신 표시, 링크, 문서 " +
                    "화면, 위협 문구만으로 상대 요청을 사실로 단정하지 않고 값 없는 고정 " +
                    "카드에서 기존 대응 화면만 직접 고르는 로컬 전용·무상태 수동 안내입니다.",
            modeLabel = "직함은 증거가 아님 · 압박보다 멈춤",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                readinessLevel = readiness.level,
                hasNoAnalysisOrSignals = hasNoAnalysisOrSignals,
                hasHighRisk = hasHighRisk,
                hasOfficialPressureSignal = hasOfficialPressureSignal,
                hasFinancialSignal = hasFinancialSignal,
                hasCredentialSignal = hasCredentialSignal,
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

    private fun buildCards(): List<OfficialPressureRealityCheckCard> {
        fun action(
            id: OfficialPressureRealityCheckActionId
        ): OfficialPressureRealityCheckAction = actionFor(id)

        return listOf(
            OfficialPressureRealityCheckCard(
                id =
                    OfficialPressureRealityCheckCardId
                        .TITLE_CHANNEL_AND_DOCUMENT_ARE_NOT_PROOF,
                displayOrder =
                    OfficialPressureRealityCheckCardId
                        .TITLE_CHANNEL_AND_DOCUMENT_ARE_NOT_PROOF.displayOrder,
                title = "직함·기관명·문서·발신번호를 증거로 단정하지 않기",
                pressureScenario =
                    "수사·보호·본인 확인을 말하며 직함, 배지, 발신 표시, 문서 화면을 믿으라고 재촉하는 때",
                valueFreeActionSentence =
                    "보이는 명칭과 화면만으로 판단하지 않고 지금은 요청 행동을 멈추겠습니다.",
                whyItMatters =
                    "말과 화면은 압박을 그럴듯하게 만들 수 있지만 요청 주체나 내용의 사실 여부를 증명하지 않습니다.",
                doNotCallLookupShareOrSubmitBoundaries = listOf(
                    "사람·기관·직함·발신번호·문서·사건 식별값을 이 코치에 적거나 공유하지 마세요.",
                    "발신자·전화번호·기관·사건·문서를 조회·검증하지 않고 전화·문자·브라우저·지도를 열거나 제출하지 않습니다."
                ),
                primaryAction = action(OfficialPressureRealityCheckActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(OfficialPressureRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(
                        OfficialPressureRealityCheckActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(OfficialPressureRealityCheckActionId.TRUSTED_HELPER_REVIEW)
                )
            ),
            OfficialPressureRealityCheckCard(
                id = OfficialPressureRealityCheckCardId.BREAK_SECRECY_AND_PRESSURE,
                displayOrder =
                    OfficialPressureRealityCheckCardId.BREAK_SECRECY_AND_PRESSURE.displayOrder,
                title = "비밀 유지·조사 협조 압박을 가족·보호자에게 숨기지 않기",
                pressureScenario =
                    "누구에게도 말하지 말거나 통화·채팅을 끊지 말고 즉시 협조하라고 위협하는 때",
                valueFreeActionSentence =
                    "요청 세부값 대신 압박 행동 종류만 가족·보호자에게 알리고 혼자 대응하지 않겠습니다.",
                whyItMatters =
                    "비밀 유지 요구를 끊으면 상대가 만든 시간 압박에서 벗어나 다른 사람이 함께 순서를 볼 수 있습니다.",
                doNotCallLookupShareOrSubmitBoundaries = listOf(
                    "사람 이름·연락처·받는 사람·원문·안전단어·콜백 문장·확인 결과를 적거나 공유하지 마세요.",
                    "수동 공유에는 고정 카드·기존 앱 경로·행동 종류만 넣고 상대에게 자동 전화·문자·회신·전송하지 않습니다."
                ),
                primaryAction = action(
                    OfficialPressureRealityCheckActionId.FAMILY_ALERT_MESSAGES
                ),
                secondaryActions = listOf(
                    action(OfficialPressureRealityCheckActionId.TRUSTED_HELPER_REVIEW),
                    action(
                        OfficialPressureRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA
                    ),
                    action(OfficialPressureRealityCheckActionId.FAMILY_RESPONSE_ROLE_BOARD),
                    action(
                        OfficialPressureRealityCheckActionId
                            .EVIDENCE_HANDOFF_PRIVACY_COACH
                    )
                )
            ),
            OfficialPressureRealityCheckCard(
                id = OfficialPressureRealityCheckCardId.PAUSE_MONEY_BEFORE_ANY_CHECK,
                displayOrder =
                    OfficialPressureRealityCheckCardId.PAUSE_MONEY_BEFORE_ANY_CHECK.displayOrder,
                title = "안전계좌·환급·대출비용·해외결제 이야기는 돈 행동 전에 멈추기",
                pressureScenario =
                    "보호 계좌, 환급, 보증 비용, 승인되지 않은 결제를 말하며 이체·구매·결제를 재촉하는 때",
                valueFreeActionSentence =
                    "송금·결제·상품권·계정 행동을 멈추고 값 없는 돈 멈춤 경로부터 읽겠습니다.",
                whyItMatters =
                    "긴급한 돈 요구를 먼저 멈추면 기관이나 결제 상태를 이 코치가 판정하지 않아도 다음 행동을 분리할 수 있습니다.",
                doNotCallLookupShareOrSubmitBoundaries = listOf(
                    "은행·계좌·카드·결제·상품권·받는 사람·금액·거래·환급 값을 입력하거나 공유하지 마세요.",
                    "은행·결제 상태를 조회·검증하지 않고 송금·취소·정지·동결·환급·회수·복구를 수행하거나 보장하지 않습니다."
                ),
                primaryAction = action(
                    OfficialPressureRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE
                ),
                secondaryActions = listOf(
                    action(
                        OfficialPressureRealityCheckActionId
                            .CREDENTIAL_SECRET_FREEZE_COACH
                    ),
                    action(OfficialPressureRealityCheckActionId.ACCOUNT_LOCKDOWN),
                    action(OfficialPressureRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE)
                )
            ),
            OfficialPressureRealityCheckCard(
                id =
                    OfficialPressureRealityCheckCardId
                        .SEPARATE_CREDENTIAL_APP_AND_SCREEN_REQUESTS,
                displayOrder =
                    OfficialPressureRealityCheckCardId
                        .SEPARATE_CREDENTIAL_APP_AND_SCREEN_REQUESTS.displayOrder,
                title = "인증 비밀값·앱 설치·화면공유 요구는 별도 멈춤 경로로 분리하기",
                pressureScenario =
                    "조사·환급·보호를 이유로 인증 비밀값, 링크, 앱 설치, 통화 유지, 화면공유를 요구하는 때",
                valueFreeActionSentence =
                    "같은 링크·앱·통화·화면공유 흐름에서는 입력이나 설치를 이어가지 않겠습니다.",
                whyItMatters =
                    "상대가 제시한 흐름 안에서 계속 행동하면 확인 과정과 입력 위치까지 상대가 통제할 수 있습니다.",
                doNotCallLookupShareOrSubmitBoundaries = listOf(
                    "인증번호·PIN·비밀번호·보안 답·계정·카드·링크·앱 이름·기기 식별값을 적거나 공유하지 마세요.",
                    "URL·도메인·앱·기기를 검사하지 않고 설치 앱 조회·악성 프로그램 제거·원격 종료·기기 정리·로그인을 수행하지 않습니다."
                ),
                primaryAction = action(
                    OfficialPressureRealityCheckActionId.CREDENTIAL_SECRET_FREEZE_COACH
                ),
                secondaryActions = listOf(
                    action(OfficialPressureRealityCheckActionId.SAFE_PASTE_PRIVACY_COACH),
                    action(
                        OfficialPressureRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH
                    ),
                    action(OfficialPressureRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE)
                )
            ),
            OfficialPressureRealityCheckCard(
                id = OfficialPressureRealityCheckCardId.FIND_A_PUBLIC_CHANNEL_LATER,
                displayOrder =
                    OfficialPressureRealityCheckCardId.FIND_A_PUBLIC_CHANNEL_LATER.displayOrder,
                title = "기관 연락은 사용자가 직접 찾은 공개 경로에서 나중에 확인하기",
                pressureScenario =
                    "상대가 지금 이 번호·링크·계정·문서만 사용해야 한다며 바로 확인하라고 재촉하는 때",
                valueFreeActionSentence =
                    "현재 흐름과 충분히 분리된 뒤 사용자가 직접 찾은 공개 경로를 수동으로 선택하겠습니다.",
                whyItMatters =
                    "상대가 제공한 경로를 그대로 쓰지 않으면 같은 압박 흐름에 확인 행동이 묶이는 일을 줄일 수 있습니다.",
                doNotCallLookupShareOrSubmitBoundaries = listOf(
                    "기관명·기관 번호·디렉터리 값·정확한 URL·도메인·프로필·사건 번호를 이 코치에 적지 마세요.",
                    "이 코치는 공개 경로를 찾거나 기관·번호·링크를 확인하지 않으며 자동 다이얼·통화·문자·브라우저·지도 탐색을 하지 않습니다."
                ),
                primaryAction = action(
                    OfficialPressureRealityCheckActionId.OFFICIAL_HELP_DIRECTORY
                ),
                secondaryActions = listOf(
                    action(
                        OfficialPressureRealityCheckActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(OfficialPressureRealityCheckActionId.REPORT_PREP),
                    action(OfficialPressureRealityCheckActionId.PRIVACY_CENTER)
                )
            ),
            OfficialPressureRealityCheckCard(
                id =
                    OfficialPressureRealityCheckCardId
                        .PREPARE_VALUE_FREE_EVIDENCE_AND_REPORT,
                displayOrder =
                    OfficialPressureRealityCheckCardId
                        .PREPARE_VALUE_FREE_EVIDENCE_AND_REPORT.displayOrder,
                title = "증거·신고 준비는 값 없이 기존 화면에서만 정리하기",
                pressureScenario =
                    "상황을 가족과 검토하거나 증거 보존·도움·신고 준비·후속 점검을 이어가려는 때",
                valueFreeActionSentence =
                    "사건 원문이나 식별값 없이 기존 증거·신고 준비 화면에서 행동 순서만 살펴보겠습니다.",
                whyItMatters =
                    "값 없는 라우팅은 민감한 사건 정보가 카드나 공유 문장에 반복 노출되는 일을 줄입니다.",
                doNotCallLookupShareOrSubmitBoundaries = listOf(
                    "원문·가린 단서·사건 내용·미리보기·문서·파일·사진·녹음·받는 사람·제출 결과를 적거나 공유하지 마세요.",
                    "증거를 수집·촬영·첨부·업로드·동기화·가림·삭제하지 않고 신고를 작성·제출하거나 접수 결과를 확인하지 않습니다."
                ),
                primaryAction = action(
                    OfficialPressureRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
                ),
                secondaryActions = listOf(
                    action(OfficialPressureRealityCheckActionId.EVIDENCE_KIT),
                    action(OfficialPressureRealityCheckActionId.REPORT_PREP),
                    action(
                        OfficialPressureRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER
                    ),
                    action(OfficialPressureRealityCheckActionId.SAFETY_FLOW_ROUTER)
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        readinessLevel: ReadinessLevel,
        hasNoAnalysisOrSignals: Boolean,
        hasHighRisk: Boolean,
        hasOfficialPressureSignal: Boolean,
        hasFinancialSignal: Boolean,
        hasCredentialSignal: Boolean,
        hasLinkSignal: Boolean,
        hasRemoteAccessSignal: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (hasNoAnalysisOrSignals) {
                add(
                    "검사 결과나 일치 신호가 없어도 사람·기관·직함·발신 표시·번호·링크·" +
                        "문서·사건·요청 상태를 단정하지 말고 모든 압박 카드를 보수적으로 " +
                        "읽도록 권합니다."
                )
            }
            if (hasHighRisk) {
                add(
                    "높은 위험 범주에서는 돈·인증 비밀값·계정·기기·가족·증거·도움·" +
                        "후속 대응 경로를 값 없이 넓게 함께 보여 줍니다."
                )
            }
            if (hasOfficialPressureSignal) {
                add(
                    "사칭·비밀 유지 압박 범주에서는 요청을 숨긴 채 이어가지 말고 긴급 " +
                        "멈춤·진행 중 연락 종료·가족 알림·차분 대화·보호자 검토·증거 전달 " +
                        "개인정보·도움·신고 준비 경로를 직접 고르도록 권합니다. 기관 번호나 " +
                        "디렉터리 값은 사용하지 않습니다."
                )
            }
            if (hasFinancialSignal) {
                add(
                    "안전계좌·대환 비용·환급·해외 결제·긴급 송금 같은 돈 압박 범주에서는 " +
                        "송금 멈춤·비밀값 멈춤·계정 잠금·원격지시 중단·노출 분류 경로를 " +
                        "보여 주며 취소·정지·동결·환급·회수·복구 결과를 만들지 않습니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증 비밀값 범주에서는 비밀값 멈춤·붙여넣기 개인정보·계정 잠금·" +
                        "원격지시 중단·노출 분류 경로를 권하며 인증번호·PIN·비밀번호·계정·" +
                        "카드 값을 읽거나 표시하지 않습니다."
                )
            }
            if (hasLinkSignal || hasRemoteAccessSignal) {
                add(
                    "링크·문자 미끼·앱 설치·통화 유지 신호에서는 같은 링크·앱·통화·" +
                        "화면공유 흐름에서 계속하지 말고 종료 문구·원격지시 중단·노출 분류·" +
                        "개인정보 경로를 직접 고르도록 권합니다. URL 검증·링크 검사·앱 목록 " +
                        "조회·악성 프로그램 제거·원격 종료·기기 정리를 하지 않습니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 긴급·목소리 사칭·번호 변경 범주에서는 별도 경로 확인·가족 콜백·" +
                        "목소리 현실 점검·새 번호 격리·안전단어 재정비·값 없는 전화 문구·" +
                        "가족 알림 경로를 직접 고르며 연락처·안전단어·콜백 값을 사용하지 않습니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만으로 증거 전달 개인정보·증거 체크·후속 " +
                        "점검 경로를 더 보여 주며 사건 내용이나 미리보기는 읽지 않습니다."
                )
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 아직 진행 중인 범주라 안전 계획·안전단어 재정비·준비 로드맵·" +
                    "가족 연습·값 없는 전화 문구 경로를 직접 살펴보도록 권합니다. 저장된 " +
                    "계획의 실제 값은 추천에 사용하지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있다는 범주는 기존 준비 항목이 채워졌다는 뜻일 뿐 어떤 " +
                    "기관·연락·요청이 실재하거나 믿을 만하다는 뜻이 아닙니다. 준비 상태와 " +
                    "관계없이 압박 행동을 멈추고 별도 경로를 직접 고르세요."
        }
        return listOf(situationCopy, readinessCopy).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 고정 문장을 직접 읽고 기존 앱 경로를 직접 고르는 로컬 전용·무상태 수동 안내이며 사람·기관·연락처·번호·링크·문서·사건·계정·결제·기기·결과값 입력을 요구하지 않습니다.",
        "추천은 현재 검사 결과의 위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기·가족 안전 계획의 실제 값을 사용하지 않습니다.",
        "사람·기관·직함·발신자 표시·전화번호·URL·도메인·프로필·공식 계정·신원·문서·법률 사건·신고·결제·계정·로그인·기기·은행 행동·복구 상태를 조회·검증·확인·판정하지 않습니다.",
        "기관 정보·공식 번호·디렉터리 값·사건 기록을 가져오지 않고 공식 번호 조회·전화번호 검증·URL 검증·도메인 스캔·발신자 표시 확인·문서 검증·신고 제출을 하지 않습니다.",
        "네트워크·웹 탐색·브라우저·지도·위치·지오코딩을 사용하지 않고 전화·다이얼·발신·통화·종료·선별·차단·문자·SMS·받는 사람 선택·자동 전송을 하지 않습니다.",
        "연락처·주소록·통화 기록·통화 상태·문자·알림을 읽거나 감시하지 않고 연락처 읽기·쓰기·편집·삽입·삭제·선택과 알림 수신·필터링을 하지 않습니다.",
        "클립보드·텍스트 감시기·자동완성·Credential Manager·Account Manager·생체 인증·키가드·키 저장소·암호화 API를 사용하지 않고 자격 증명·계정·인증·로그인 상태를 읽거나 바꾸지 않습니다.",
        "인증번호·PIN·비밀번호·보안 답·안전단어·콜백 문장·연락처 이름·번호·계정·카드·은행·받는 사람 값을 읽기·표시·저장·복사·붙여넣기·자동 입력·공유·제출하지 않습니다.",
        "파일·문서·사진·이미지·스크린샷·오디오·미디어·저장소를 읽지 않고 카메라·화면 캡처·OCR·이미지 분석·녹음·전사·파일 선택기·첨부·스트림·URI 공유를 사용하지 않습니다.",
        "수동 문자 공유는 고정 압박 카드·기존 앱 경로·행동 종류만 담고 원문·가린 단서·사건·계획·기관 목록·연락처·채널·계정·결제·기기·앱·파일·받는 사람·확인·제출·복구 결과를 담거나 자동 발송하지 않습니다.",
        "새 Android 권한을 요청하지 않고 알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러를 만들거나 예약하지 않습니다.",
        "새 저장소를 만들지 않고 카드 선택·추천·읽음·공유·체크·메모·담당자·받는 사람·확인·신고·계정·결제·복구·진행·상태·이력을 저장하지 않으며 가족 계획·사건·디렉터리·연락처·알림·통화·문자 상태를 수정하지 않습니다.",
        "결제·은행 기능이나 API·NFC를 사용하지 않고 송금·결제 취소·계정 또는 카드 정지·동결·환급·회수·복구를 자동 수행하지 않습니다.",
        "기기 관리자·접근성 서비스·VPN·사용 기록·앱 작업 권한을 쓰지 않고 설치 앱·패키지 목록 조회, 앱 설치·제거, 설정 변경, 원격 연결 종료, 악성 프로그램 검사·제거, 기기 정리를 하지 않습니다.",
        "기존 도움·신고 준비·돈·비밀값·계정·원격지시·노출·증거·가족·후속 화면으로 이동하는 버튼은 화면만 열 뿐 해당 기능을 이 코치에서 복제하거나 값을 넘기지 않습니다.",
        "사람·기관·직함·발신 표시·번호·링크·문서·사건·신고·결제·계정·로그인·기기·은행 행동·복구 상태가 공식·실재·안전·보안됨·검증됨·유효·신뢰됨·접수됨·제출됨·수신됨·차단됨·해결됨·완료됨·복구됨·환급됨·동결됨·보호됨·합법·문제없음이라고 증명하거나 보장하지 않습니다.",
        "법률 자문이나 법률·금융 결과를 제공·보장하지 않으며 자동 모니터링·검사·스캔·OCR·삭제·가림·차단·통화·메시지 전송·조회·검증·저장·업로드·첨부·제출을 수행하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<OfficialPressureRealityCheckCardId>,
        recommendedActions: List<OfficialPressureRealityCheckAction>,
        cards: List<OfficialPressureRealityCheckCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 기관 사칭 압박 현실 점검 코치")
            appendLine()
            appendLine("직함과 압박을 증거로 단정하지 않는 수동 멈춤 순서를 함께 보고 있습니다.")
            appendLine()
            appendLine("추천 현실 점검 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("가족에게 말할 값 없는 문장")
            appendLine("- 직함·기관 압박·비밀 유지·돈·인증·앱 요구 같은 행동 종류만 함께 살펴보세요.")
            append(
                "사람·연락처·번호·링크·문서·사건·계정·결제·기기·앱·파일·받는 사람·" +
                    "확인·신고·복구 결과 없이 고정 카드와 기존 앱 경로 이름만 담았습니다."
            )
        }.trim()
    }

    private fun actionFor(
        id: OfficialPressureRealityCheckActionId
    ): OfficialPressureRealityCheckAction = OfficialPressureRealityCheckAction(
        id = id,
        label = when (id) {
            OfficialPressureRealityCheckActionId.QUICK_STOP -> "긴급 멈춤 모드"
            OfficialPressureRealityCheckActionId.CHECKER -> "의심 내용 검사"
            OfficialPressureRealityCheckActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            OfficialPressureRealityCheckActionId.SAFETY_PLAN -> "가족 안전 계획"
            OfficialPressureRealityCheckActionId.FAMILY_READINESS_ROADMAP ->
                "가족 준비 로드맵"
            OfficialPressureRealityCheckActionId.FAMILY_SAFETY_DRILL_COACH ->
                "가족 안전 연습 코치"
            OfficialPressureRealityCheckActionId.SAFE_PASTE_PRIVACY_COACH ->
                "붙여넣기 전 개인정보 가리기"
            OfficialPressureRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                "진행 중 연락 종료 문구"
            OfficialPressureRealityCheckActionId.FAMILY_CALLBACK_CARD ->
                "평소 가족 경로 콜백 카드"
            OfficialPressureRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                "가족 안전단어 재설정 코치"
            OfficialPressureRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                "별도 경로 확인 코치"
            OfficialPressureRealityCheckActionId.SAFE_CALL_SCRIPTS ->
                "값 없는 가족 콜백·확인 전화 스크립트"
            OfficialPressureRealityCheckActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                "목소리 사칭 현실 점검 코치"
            OfficialPressureRealityCheckActionId.NEW_NUMBER_QUARANTINE_COACH ->
                "새 번호 격리 코치"
            OfficialPressureRealityCheckActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                "자격 비밀값 멈춤 코치"
            OfficialPressureRealityCheckActionId.FAMILY_ALERT_MESSAGES ->
                "가족 경고 메시지 팩"
            OfficialPressureRealityCheckActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                "확인 결과별 다음 단계 보드"
            OfficialPressureRealityCheckActionId.RECONTACT_BOUNDARY_COACH ->
                "재연락 경계 코치"
            OfficialPressureRealityCheckActionId.TRUSTED_HELPER_REVIEW ->
                "보호자와 함께 안전 검토"
            OfficialPressureRealityCheckActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                "가족 대응 역할 보드"
            OfficialPressureRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                "가족 차분 대화 안건"
            OfficialPressureRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE ->
                "송금 전 멈춤 확인"
            OfficialPressureRealityCheckActionId.ACCOUNT_LOCKDOWN ->
                "계정·기기 잠금 체크리스트"
            OfficialPressureRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                "화면공유·원격지시 끊기 코치"
            OfficialPressureRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE ->
                "노출 영향 분류"
            OfficialPressureRealityCheckActionId.PRIVACY_CENTER -> "개인정보 센터"
            OfficialPressureRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                "증거 전달 개인정보 코치"
            OfficialPressureRealityCheckActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            OfficialPressureRealityCheckActionId.OFFICIAL_HELP_DIRECTORY ->
                "신고·상담 도움 경로"
            OfficialPressureRealityCheckActionId.REPORT_PREP -> "신고용 상담 준비 카드"
            OfficialPressureRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                "며칠 뒤 다시 확인할 것"
            OfficialPressureRealityCheckActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
