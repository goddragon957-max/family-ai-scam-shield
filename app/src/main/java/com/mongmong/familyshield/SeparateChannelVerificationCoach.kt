package com.mongmong.familyshield

enum class SeparateChannelVerificationCardId(val displayOrder: Int) {
    ABANDON_CURRENT_CHANNEL(1),
    FAMILY_KNOWN_PATH_CALLBACK(2),
    USER_FOUND_OFFICIAL_CHANNEL(3),
    DIRECT_OFFICIAL_APP_OR_SITE(4),
    SEPARATE_DEVICE_ACCOUNT_REVIEW(5),
    VALUE_FREE_FAMILY_UPDATE(6)
}

enum class SeparateChannelVerificationActionId {
    QUICK_STOP,
    ACTIVE_CONTACT_EXIT_SCRIPTS,
    CHECKER,
    ANALYSIS_RESULT,
    PRIVACY_CENTER,
    EXPOSURE_IMPACT_TRIAGE,
    SAFE_CALL_SCRIPTS,
    FAMILY_ALERT_MESSAGES,
    TRUSTED_HELPER_REVIEW,
    FAMILY_READINESS_ROADMAP,
    MONEY_TRANSFER_PAUSE_GATE,
    ACCOUNT_LOCKDOWN,
    REMOTE_ACCESS_CUTOFF_COACH,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    SAFETY_FLOW_ROUTER
}

data class SeparateChannelVerificationAction(
    val id: SeparateChannelVerificationActionId,
    val label: String
)

data class SeparateChannelVerificationCard(
    val id: SeparateChannelVerificationCardId,
    val displayOrder: Int,
    val title: String,
    val verificationSentence: String,
    val whenToUse: String,
    val whyItMatters: String,
    val doNotUseOrShareBoundaries: List<String>,
    val primaryAction: SeparateChannelVerificationAction,
    val secondaryActions: List<SeparateChannelVerificationAction>
) {
    val routeActions: List<SeparateChannelVerificationAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class SeparateChannelVerificationCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<SeparateChannelVerificationCardId>,
    val recommendedActions: List<SeparateChannelVerificationAction>,
    val cards: List<SeparateChannelVerificationCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<SeparateChannelVerificationCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<SeparateChannelVerificationActionId>
        get() = recommendedActions.map { action -> action.id }
}

object SeparateChannelVerificationCoachBuilder {
    private val familyVerificationSignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.PHONE_NUMBER_CHANGE
    )

    private val secrecyImpersonationSignals = setOf(
        ScamSignal.SECRECY,
        ScamSignal.IMPERSONATION
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

    fun build(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness,
        incidentCount: Int = 0
    ): SeparateChannelVerificationCoach {
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val hasNoAnalysisOrSignals = analysis == null || signals.isEmpty()
        val hasHighRisk = analysis?.riskLevel == RiskLevel.HIGH
        val hasFamilyVerificationSignal = signals.any { signal ->
            signal in familyVerificationSignals
        }
        val hasSecrecyImpersonationSignal = signals.any { signal ->
            signal in secrecyImpersonationSignals
        }
        val hasFinancialSignal = signals.any { signal -> signal in financialSignals }
        val hasCredentialSignal = signals.any { signal -> signal in credentialSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasRemoteAccessSignal = signals.any { signal -> signal in remoteAccessSignals }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0
        val needsReadinessSupport = readiness.level != ReadinessLevel.READY

        val recommendedCardSet = linkedSetOf(
            SeparateChannelVerificationCardId.ABANDON_CURRENT_CHANNEL,
            SeparateChannelVerificationCardId.VALUE_FREE_FAMILY_UPDATE
        )
        if (hasNoAnalysisOrSignals) {
            recommendedCardSet += SeparateChannelVerificationCardId.entries
        }
        if (hasHighRisk) {
            recommendedCardSet += SeparateChannelVerificationCardId.USER_FOUND_OFFICIAL_CHANNEL
            recommendedCardSet += SeparateChannelVerificationCardId.SEPARATE_DEVICE_ACCOUNT_REVIEW
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet += SeparateChannelVerificationCardId.FAMILY_KNOWN_PATH_CALLBACK
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedCardSet += SeparateChannelVerificationCardId.USER_FOUND_OFFICIAL_CHANNEL
        }
        if (hasFinancialSignal) {
            recommendedCardSet += SeparateChannelVerificationCardId.USER_FOUND_OFFICIAL_CHANNEL
            recommendedCardSet += SeparateChannelVerificationCardId.SEPARATE_DEVICE_ACCOUNT_REVIEW
        }
        if (hasCredentialSignal || hasRemoteAccessSignal) {
            recommendedCardSet += SeparateChannelVerificationCardId.SEPARATE_DEVICE_ACCOUNT_REVIEW
        }
        if (hasLinkSignal) {
            recommendedCardSet += SeparateChannelVerificationCardId.DIRECT_OFFICIAL_APP_OR_SITE
        }
        if (hasSavedIncident) {
            recommendedCardSet += SeparateChannelVerificationCardId.USER_FOUND_OFFICIAL_CHANNEL
        }
        if (needsReadinessSupport) {
            recommendedCardSet += SeparateChannelVerificationCardId.FAMILY_KNOWN_PATH_CALLBACK
            recommendedCardSet += SeparateChannelVerificationCardId.SEPARATE_DEVICE_ACCOUNT_REVIEW
        }
        val recommendedCardIds = SeparateChannelVerificationCardId.entries
            .filter { cardId -> cardId in recommendedCardSet }

        val recommendedActionSet = linkedSetOf(
            SeparateChannelVerificationActionId.QUICK_STOP,
            SeparateChannelVerificationActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
            if (analysis == null) {
                SeparateChannelVerificationActionId.CHECKER
            } else {
                SeparateChannelVerificationActionId.ANALYSIS_RESULT
            },
            SeparateChannelVerificationActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            recommendedActionSet += SeparateChannelVerificationActionId.PRIVACY_CENTER
            recommendedActionSet += SeparateChannelVerificationActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += SeparateChannelVerificationActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += SeparateChannelVerificationActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (hasHighRisk) {
            recommendedActionSet += SeparateChannelVerificationActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += SeparateChannelVerificationActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += SeparateChannelVerificationActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += SeparateChannelVerificationActionId.EVIDENCE_KIT
            recommendedActionSet += SeparateChannelVerificationActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += SeparateChannelVerificationActionId.REPORT_PREP
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += SeparateChannelVerificationActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += SeparateChannelVerificationActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedActionSet += SeparateChannelVerificationActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet += SeparateChannelVerificationActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += SeparateChannelVerificationActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (hasFinancialSignal) {
            recommendedActionSet += SeparateChannelVerificationActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += SeparateChannelVerificationActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += SeparateChannelVerificationActionId.EVIDENCE_KIT
            recommendedActionSet += SeparateChannelVerificationActionId.OFFICIAL_HELP_DIRECTORY
            if (analysis != null) {
                recommendedActionSet += SeparateChannelVerificationActionId.REPORT_PREP
            }
        }
        if (hasCredentialSignal) {
            recommendedActionSet += SeparateChannelVerificationActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += SeparateChannelVerificationActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += SeparateChannelVerificationActionId.EVIDENCE_KIT
        }
        if (hasRemoteAccessSignal) {
            recommendedActionSet += SeparateChannelVerificationActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += SeparateChannelVerificationActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += SeparateChannelVerificationActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += SeparateChannelVerificationActionId.EVIDENCE_KIT
        }
        if (hasLinkSignal) {
            recommendedActionSet += SeparateChannelVerificationActionId.CHECKER
            recommendedActionSet += SeparateChannelVerificationActionId.PRIVACY_CENTER
            recommendedActionSet += SeparateChannelVerificationActionId.EXPOSURE_IMPACT_TRIAGE
        }
        if (hasSavedIncident) {
            recommendedActionSet += SeparateChannelVerificationActionId.EVIDENCE_KIT
            if (analysis != null) {
                recommendedActionSet += SeparateChannelVerificationActionId.REPORT_PREP
            }
        }
        if (needsReadinessSupport) {
            recommendedActionSet += SeparateChannelVerificationActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += SeparateChannelVerificationActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += SeparateChannelVerificationActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += SeparateChannelVerificationActionId.FAMILY_READINESS_ROADMAP
        }
        val recommendedActions = SeparateChannelVerificationActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards(hasAnalysis = analysis != null)
        val recommendationTitle = when {
            analysis == null -> "분석 전에도 현재 경로를 버리고 별도 경로를 고르는 안내"
            hasRemoteAccessSignal -> "현재 연락·설치 지시와 분리된 기기·보호자로 점검하기"
            hasCredentialSignal -> "인증 비밀을 더 공유하지 않고 별도 계정 경로로 점검하기"
            hasFinancialSignal || hasHighRisk -> "돈 행동을 멈추고 직접 찾은 기관 경로로 점검하기"
            hasFamilyVerificationSignal -> "새 연락 경로를 쓰지 않고 평소 가족 경로로 콜백하기"
            hasSecrecyImpersonationSignal -> "현재 연락을 비밀로 하지 않고 별도 기관 경로 찾기"
            hasLinkSignal -> "현재 링크·번호를 다시 쓰지 않고 앱 안에서 직접 확인하기"
            else -> "현재 연락과 분리된 경로를 사용자가 직접 고르기"
        }

        return SeparateChannelVerificationCoach(
            title = "별도 경로 확인 코치",
            intro =
                "현재 상대가 준 연락처·번호·링크·계정·프로필을 확인 수단으로 다시 쓰지 않고, " +
                    "가족이 평소 알던 경로나 사용자가 직접 찾은 기관 경로의 종류를 고르는 로컬 전용·무상태 수동 안내입니다. " +
                    "이 앱은 사람·번호·링크·기관을 확인하거나 대신 연락하지 않습니다.",
            modeLabel = "현재 경로 중단 · 별도 경로를 직접 선택",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                analysis = analysis,
                readinessLevel = readiness.level,
                signals = signals,
                hasHighRisk = hasHighRisk,
                hasFamilyVerificationSignal = hasFamilyVerificationSignal,
                hasSecrecyImpersonationSignal = hasSecrecyImpersonationSignal,
                hasFinancialSignal = hasFinancialSignal,
                hasCredentialSignal = hasCredentialSignal,
                hasLinkSignal = hasLinkSignal,
                hasRemoteAccessSignal = hasRemoteAccessSignal,
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

    private fun buildCards(hasAnalysis: Boolean): List<SeparateChannelVerificationCard> {
        fun action(
            id: SeparateChannelVerificationActionId
        ): SeparateChannelVerificationAction = actionFor(id)

        fun currentResultAction(): SeparateChannelVerificationAction = action(
            if (hasAnalysis) {
                SeparateChannelVerificationActionId.ANALYSIS_RESULT
            } else {
                SeparateChannelVerificationActionId.CHECKER
            }
        )

        return listOf(
            SeparateChannelVerificationCard(
                id = SeparateChannelVerificationCardId.ABANDON_CURRENT_CHANNEL,
                displayOrder = SeparateChannelVerificationCardId.ABANDON_CURRENT_CHANNEL.displayOrder,
                title = "현재 연락 경로 버리기",
                verificationSentence =
                    "현재 통화·채팅·번호·링크·계정·프로필을 더 쓰지 않고 연락을 직접 끝내겠습니다.",
                whenToUse =
                    "상대가 계속 답장·통화 유지·링크 열기·번호 저장·앱 이동을 재촉할 때 먼저 사용합니다.",
                whyItMatters =
                    "같은 경로 안에서는 상대가 보여 주는 새 정보도 상대가 통제할 수 있어 독립된 확인이 되기 어렵습니다.",
                doNotUseOrShareBoundaries = listOf(
                    "상대가 새로 준 번호·링크·계정·프로필이나 전달받은 연락처를 확인 경로로 다시 쓰지 마세요.",
                    "이 버튼이 통화·메시지·연락처를 자동으로 끊거나 차단했다고 여기지 마세요."
                ),
                primaryAction = action(SeparateChannelVerificationActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                secondaryActions = listOf(
                    action(SeparateChannelVerificationActionId.QUICK_STOP),
                    action(SeparateChannelVerificationActionId.REMOTE_ACCESS_CUTOFF_COACH),
                    action(SeparateChannelVerificationActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            SeparateChannelVerificationCard(
                id = SeparateChannelVerificationCardId.FAMILY_KNOWN_PATH_CALLBACK,
                displayOrder =
                    SeparateChannelVerificationCardId.FAMILY_KNOWN_PATH_CALLBACK.displayOrder,
                title = "가족은 평소 경로로 콜백",
                verificationSentence =
                    "새 연락처가 아니라 가족이 원래 쓰던 별도 경로로 직접 콜백하고 값 없는 확인 문장을 읽겠습니다.",
                whenToUse =
                    "가족 사고·목소리 사칭·영상 합성·번호 변경·급한 부탁처럼 가족 확인이 필요할 때 사용합니다.",
                whyItMatters =
                    "현재 연락과 분리된 평소 가족 경로는 새 번호나 합성된 목소리만 믿고 행동하는 일을 줄이는 데 도움이 됩니다.",
                doNotUseOrShareBoundaries = listOf(
                    "새 번호를 저장하거나 현재 상대에게 가족 확인 답·안전 질문 답·연락처 값을 보내지 마세요.",
                    "이 코치에 가족 이름·전화번호·콜백 문장 원문·안전 질문 답을 입력하지 마세요."
                ),
                primaryAction = action(SeparateChannelVerificationActionId.SAFE_CALL_SCRIPTS),
                secondaryActions = listOf(
                    action(SeparateChannelVerificationActionId.FAMILY_ALERT_MESSAGES),
                    action(SeparateChannelVerificationActionId.TRUSTED_HELPER_REVIEW),
                    action(SeparateChannelVerificationActionId.FAMILY_READINESS_ROADMAP)
                )
            ),
            SeparateChannelVerificationCard(
                id = SeparateChannelVerificationCardId.USER_FOUND_OFFICIAL_CHANNEL,
                displayOrder =
                    SeparateChannelVerificationCardId.USER_FOUND_OFFICIAL_CHANNEL.displayOrder,
                title = "기관·은행·수사기관은 직접 찾은 공식 경로",
                verificationSentence =
                    "현재 상대가 준 정보는 쓰지 않고 기관·은행·수사기관의 경로를 제가 별도로 직접 찾아 문의하겠습니다.",
                whenToUse =
                    "은행·카드사·경찰·검찰·통신사·학교·직장·공공기관을 자칭하거나 송금·신고를 재촉할 때 사용합니다.",
                whyItMatters =
                    "현재 상대가 제공한 연락 수단과 분리해야 사칭한 사람이 만든 안내를 다시 따라가는 일을 줄일 수 있습니다.",
                doNotUseOrShareBoundaries = listOf(
                    "상대가 준 대표번호·검색 결과·링크·QR·연결 전화를 기관 경로라고 가정하지 마세요.",
                    "문의할 때도 비밀번호·OTP·PIN·계정·카드 전체 값·신분정보를 말하거나 보내지 마세요."
                ),
                primaryAction = action(SeparateChannelVerificationActionId.OFFICIAL_HELP_DIRECTORY),
                secondaryActions = listOf(
                    action(SeparateChannelVerificationActionId.MONEY_TRANSFER_PAUSE_GATE),
                    action(SeparateChannelVerificationActionId.EVIDENCE_KIT),
                    action(SeparateChannelVerificationActionId.REPORT_PREP)
                )
            ),
            SeparateChannelVerificationCard(
                id = SeparateChannelVerificationCardId.DIRECT_OFFICIAL_APP_OR_SITE,
                displayOrder =
                    SeparateChannelVerificationCardId.DIRECT_OFFICIAL_APP_OR_SITE.displayOrder,
                title = "택배·청첩장·플랫폼 알림은 앱/공식 사이트에서 직접 확인",
                verificationSentence =
                    "현재 메시지의 링크를 누르지 않고 제가 평소 쓰던 앱이나 별도로 찾은 사이트 안에서 알림을 직접 확인하겠습니다.",
                whenToUse =
                    "택배·청첩장·부고·결제 승인·플랫폼 계정·학교나 직장 알림처럼 링크가 붙은 요청에 사용합니다.",
                whyItMatters =
                    "메시지 안의 링크와 번호를 재사용하지 않으면 가짜 화면이나 가짜 상담 경로로 이어지는 위험을 줄일 수 있습니다.",
                doNotUseOrShareBoundaries = listOf(
                    "현재 링크·단축 주소·QR·발신 번호를 다시 열거나 복사해 이 코치에 넣지 마세요.",
                    "이 앱이 링크·URL·도메인·발신 번호를 검사하거나 기관 소유라고 판정한다고 여기지 마세요."
                ),
                primaryAction = action(SeparateChannelVerificationActionId.CHECKER),
                secondaryActions = listOf(
                    action(SeparateChannelVerificationActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(SeparateChannelVerificationActionId.PRIVACY_CENTER),
                    currentResultAction()
                ).filterNot { route ->
                    route.id == SeparateChannelVerificationActionId.CHECKER
                }.distinctBy { route -> route.id }
            ),
            SeparateChannelVerificationCard(
                id = SeparateChannelVerificationCardId.SEPARATE_DEVICE_ACCOUNT_REVIEW,
                displayOrder =
                    SeparateChannelVerificationCardId.SEPARATE_DEVICE_ACCOUNT_REVIEW.displayOrder,
                title = "계정·기기 노출은 별도 기기와 보호자 점검",
                verificationSentence =
                    "현재 연락과 의심 기기에서 민감한 작업을 멈추고 가능하면 별도 기기와 보호자에게서 기존 보호 경로를 직접 고르겠습니다.",
                whenToUse =
                    "비밀번호·인증번호·계정 화면·앱 설치·화면공유·원격제어·기기 설정 요구가 있었을 때 사용합니다.",
                whyItMatters =
                    "현재 연락과 분리된 환경에서 함께 보면 추가 인증 비밀이나 민감 화면을 더 노출할 가능성을 줄일 수 있습니다.",
                doNotUseOrShareBoundaries = listOf(
                    "현재 상대가 지정한 앱·파일·프로필·설정·지원 도구를 설치하거나 다시 열지 마세요.",
                    "보호자에게도 비밀번호·OTP·PIN·계정·카드 값·기기 식별자·앱 목록을 보내지 마세요."
                ),
                primaryAction = action(SeparateChannelVerificationActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(SeparateChannelVerificationActionId.REMOTE_ACCESS_CUTOFF_COACH),
                    action(SeparateChannelVerificationActionId.TRUSTED_HELPER_REVIEW),
                    action(SeparateChannelVerificationActionId.EVIDENCE_KIT)
                )
            ),
            SeparateChannelVerificationCard(
                id = SeparateChannelVerificationCardId.VALUE_FREE_FAMILY_UPDATE,
                displayOrder =
                    SeparateChannelVerificationCardId.VALUE_FREE_FAMILY_UPDATE.displayOrder,
                title = "확인 결과는 값 없이 가족에게 알리기",
                verificationSentence =
                    "별도 경로로 직접 확인한 뒤 실제 값은 빼고 중단·확인·다음 수동 행동의 종류만 가족에게 알리겠습니다.",
                whenToUse =
                    "가족에게 현재 진행 상황을 알려 비밀 유지 압박을 끊고 다음 역할을 함께 고를 때 사용합니다.",
                whyItMatters =
                    "행동 종류만 공유하면 가족 협력은 이어 가면서 원문과 인증·계정·연락 정보를 다시 퍼뜨리지 않을 수 있습니다.",
                doNotUseOrShareBoundaries = listOf(
                    "원문 메시지·이름·연락처·번호·링크·OTP·PIN·비밀번호·계정·카드·안전 계획 값을 넣지 마세요.",
                    "이 공유 버튼은 받는 사람을 고르거나 자동 전송하지 않으며 확인 결과를 증명하지 않습니다."
                ),
                primaryAction = action(SeparateChannelVerificationActionId.FAMILY_ALERT_MESSAGES),
                secondaryActions = listOf(
                    action(SeparateChannelVerificationActionId.TRUSTED_HELPER_REVIEW),
                    action(SeparateChannelVerificationActionId.SAFETY_FLOW_ROUTER),
                    action(SeparateChannelVerificationActionId.PRIVACY_CENTER)
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readinessLevel: ReadinessLevel,
        signals: Set<ScamSignal>,
        hasHighRisk: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasSecrecyImpersonationSignal: Boolean,
        hasFinancialSignal: Boolean,
        hasCredentialSignal: Boolean,
        hasLinkSignal: Boolean,
        hasRemoteAccessSignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (analysis == null || signals.isEmpty()) {
                add(
                    "현재 분석이 없거나 일치 신호가 없습니다. 이를 현재 사람·번호·링크·계정·" +
                        "요청에 문제가 없다는 판정으로 보지 말고 현재 경로 사용을 멈춘 뒤 별도 경로를 직접 고르세요."
                )
            }
            if (hasHighRisk) {
                add(
                    "위험도가 높아 현재 연락과 돈·계정 행동을 멈추고 가족·기관·계정·증거 경로를 " +
                        "서로 분리해 직접 확인하도록 권합니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 긴급상황·목소리 합성·번호 변경 신호가 있어 새 연락처가 아닌 평소 가족 " +
                        "경로의 콜백과 값 없는 확인 전화 문장을 권합니다."
                )
            }
            if (hasSecrecyImpersonationSignal) {
                add(
                    "비밀 유지·기관 사칭 신호가 있어 현재 연락 경로를 버리고 가족에게 알린 뒤 " +
                        "사용자가 직접 찾은 기관 경로를 권합니다. 확인 과정을 비밀로 두지 마세요."
                )
            }
            if (hasFinancialSignal) {
                add(
                    "송금·결제·금융 압박 신호가 있어 돈 행동을 멈추고 계정·증거·기관 도움 경로를 " +
                        "권합니다. 결제 취소·계정 또는 카드 정지·환급·회수를 대신하거나 보장하지 않습니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증 비밀 요구 가능성이 있어 계정 보호와 원격지시 중단 경로를 권합니다. " +
                        "계정이나 카드를 자동 정지·동결하는 기능은 아닙니다."
                )
            }
            if (hasLinkSignal) {
                add(
                    "링크·스미싱 신호가 있어 현재 링크와 번호를 재사용하지 않고 검사기·노출 분류·" +
                        "개인정보 경로를 권합니다. 이 코치는 링크를 스캔하거나 URL·도메인을 판정하지 않습니다."
                )
            }
            if (hasRemoteAccessSignal) {
                add(
                    "앱 설치·화면공유·원격지시 신호가 있어 현재 연락과 기기 작업을 멈추고 별도 " +
                        "기기·보호자·계정·증거 경로를 권합니다. 원격 연결 해제·악성 프로그램 제거·" +
                        "기기 정리를 수행하거나 보장하지 않습니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만 반영하며 사건 내용은 읽지 않습니다. " +
                        "비밀값 없는 증거와 상담 준비 경로만 함께 권합니다."
                )
            }
            if (isEmpty()) {
                add(
                    "현재 신호만으로 사람·번호·링크·기관·계정 상태를 확정하지 않고 현재 연락과 " +
                        "분리된 경로를 사용자가 직접 고르도록 안내합니다."
                )
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW ->
                "가족 준비가 부족한 상태라 값 없는 콜백 문장·가족 경고·보호자 검토·준비 로드맵을 함께 권합니다. 실제 가족 값은 이 화면에 입력하지 마세요."
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중이라 값 없는 콜백 문장·가족 경고·보호자 검토·준비 로드맵으로 빠진 부분을 직접 확인하도록 권합니다. 저장된 실제 값은 추천에 들어오지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있어도 이번 사람·번호·링크·기관·계정 상태를 점검했다는 뜻은 아닙니다. 현재 경로와 분리해 다시 확인하세요."
        }
        return "$situationCopy $readinessCopy"
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 사용자가 화면을 직접 읽고 직접 선택하는 로컬 전용·무상태 수동 안내입니다.",
        "추천은 위험도·일치 신호, 값 없는 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·사건 내용·가족 안전 계획 값을 사용하지 않습니다.",
        "이름·연락처·정확한 전화번호·계정·계좌·카드·OTP·PIN·비밀번호·가족 확인 답·주민번호·기기 식별자·정확한 URL·도메인·공식 번호·앱 이름·패키지 이름을 묻거나 저장하지 않습니다.",
        "카드 선택·추천·읽음·공유·확인 상태·채널 값·공식 번호·앱 이름·기기 상태·체크·메모·진행·이력을 위한 새 저장소를 만들거나 기록하지 않습니다.",
        "문자·메시지·통화·연락처·클립보드·계정·기기 화면을 읽거나 감시하지 않고 연락처를 읽거나 쓰거나 고르지 않습니다.",
        "네트워크·웹 탐색·지도·위치·기관 번호 조회·전화번호 검증·링크·URL·도메인 검증·링크 스캔을 사용하지 않습니다.",
        "새 Android 권한을 요청하지 않습니다.",
        "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커를 만들거나 예약하지 않으며 스케줄러를 사용하지 않습니다.",
        "전화 앱을 열거나 전화를 자동으로 걸거나 끊거나 선별·차단하지 않고 문자·SMS를 보내거나 읽지 않으며 받는 사람을 고르거나 공유를 자동 전송하지 않습니다.",
        "기기 관리자·접근성 서비스·VPN·알림 접근·사용 기록·앱 작업 권한이나 기기 제어 기능을 사용하지 않고 설치 앱·패키지 목록을 조회·설치·제거하거나 기기 설정을 바꾸지 않습니다.",
        "결제·은행 기능이나 API, NFC나 Android 계정 관리자에 접근하지 않고 송금·결제·취소·계정 또는 카드 정지·동결·환급·피해 복구를 자동 수행하지 않습니다.",
        "붙여넣은 글, 통화 상태, 연락처, 사건 기록, 가족 안전 계획, 기관 목록, 계정·카드·은행·기기 설정, 설치 앱·패키지 상태나 돈의 움직임을 추가·수정·삭제하지 않습니다.",
        "원문·화면·연락·번호·링크·기기·계정을 자동으로 가리거나 모니터링·스캔·삭제·차단·호출·메시지 전송·조회·확인하지 않습니다.",
        "이 안내는 본인·번호·링크·기관 확인, 사기 여부, 조치 완료, 악성 프로그램 제거, 원격 연결 종료, 기기 정리, 계정·카드 조치, 결제 취소·환급·회복 또는 법률·금융 결과를 증명하거나 보장하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<SeparateChannelVerificationCardId>,
        recommendedActions: List<SeparateChannelVerificationAction>,
        cards: List<SeparateChannelVerificationCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 별도 경로 확인 코치")
            appendLine()
            appendLine("현재 경로와 분리한 수동 확인 중입니다.")
            appendLine()
            appendLine("추천 확인 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("가족에게 알릴 값 없는 문장")
            appendLine("- 현재 연락 경로를 쓰지 않고 별도 경로를 직접 확인 중입니다.")
            append(
                "원문·사람·연락처·번호·링크·인증·계정·카드·기기·가족 계획 값 없이 " +
                    "일반 카드와 기존 앱 경로 이름만 담았습니다."
            )
        }.trim()
    }

    private fun actionFor(
        id: SeparateChannelVerificationActionId
    ): SeparateChannelVerificationAction = SeparateChannelVerificationAction(
        id = id,
        label = when (id) {
            SeparateChannelVerificationActionId.QUICK_STOP -> "긴급 멈춤 모드"
            SeparateChannelVerificationActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                "진행 중 연락 종료 문장"
            SeparateChannelVerificationActionId.CHECKER -> "의심 내용 검사"
            SeparateChannelVerificationActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            SeparateChannelVerificationActionId.PRIVACY_CENTER -> "개인정보 센터"
            SeparateChannelVerificationActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
            SeparateChannelVerificationActionId.SAFE_CALL_SCRIPTS ->
                "값 없는 가족 콜백·확인 전화 스크립트"
            SeparateChannelVerificationActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
            SeparateChannelVerificationActionId.TRUSTED_HELPER_REVIEW ->
                "보호자와 함께 안전 검토"
            SeparateChannelVerificationActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
            SeparateChannelVerificationActionId.MONEY_TRANSFER_PAUSE_GATE -> "송금 전 멈춤 확인"
            SeparateChannelVerificationActionId.ACCOUNT_LOCKDOWN ->
                "계정·기기 잠금 체크리스트"
            SeparateChannelVerificationActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                "화면공유·원격지시 끊기 코치"
            SeparateChannelVerificationActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            SeparateChannelVerificationActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처"
            SeparateChannelVerificationActionId.REPORT_PREP -> "신고용 상담 준비 카드"
            SeparateChannelVerificationActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
