package com.mongmong.familyshield

enum class RemoteAccessCutoffCardId(val displayOrder: Int) {
    END_CALL_AND_SCREEN_SHARE(1),
    STOP_APP_FILE_PROFILE_INSTALL(2),
    HIDE_CODES_AND_FINANCIAL_SCREENS(3),
    USE_SEPARATE_DEVICE_AND_HELPER(4),
    PRESERVE_SAFE_EVIDENCE(5),
    CONTINUE_OFFICIAL_HELP_AND_FOLLOW_UP(6)
}

enum class RemoteAccessCutoffActionId {
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
    ACCOUNT_LOCKDOWN,
    MONEY_TRANSFER_PAUSE_GATE,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    SAFETY_FLOW_ROUTER,
    RECOVERY_PLAN,
    FOLLOW_UP_CHECK_IN_PLANNER
}

data class RemoteAccessCutoffAction(
    val id: RemoteAccessCutoffActionId,
    val label: String
)

data class RemoteAccessCutoffCard(
    val id: RemoteAccessCutoffCardId,
    val displayOrder: Int,
    val title: String,
    val cutoffSentence: String,
    val whyItMatters: String,
    val doNotDoBoundaries: List<String>,
    val primaryAction: RemoteAccessCutoffAction,
    val secondaryActions: List<RemoteAccessCutoffAction>
) {
    val routeActions: List<RemoteAccessCutoffAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class RemoteAccessCutoffCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<RemoteAccessCutoffCardId>,
    val recommendedActions: List<RemoteAccessCutoffAction>,
    val cards: List<RemoteAccessCutoffCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<RemoteAccessCutoffCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<RemoteAccessCutoffActionId>
        get() = recommendedActions.map { action -> action.id }
}

object RemoteAccessCutoffCoachBuilder {
    private val remoteAccessSignals = setOf(
        ScamSignal.APP_INSTALL,
        ScamSignal.LIVE_CALL_CONTROL
    )

    private val credentialSignals = setOf(
        ScamSignal.CREDENTIALS,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

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

    private val linkSignals = setOf(
        ScamSignal.LINK,
        ScamSignal.PARCEL_INVITATION_SMISHING
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
    ): RemoteAccessCutoffCoach {
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val hasNoAnalysisOrSignals = analysis == null || signals.isEmpty()
        val hasHighRisk = analysis?.riskLevel == RiskLevel.HIGH
        val hasRemoteAccessSignal = signals.any { signal -> signal in remoteAccessSignals }
        val hasCredentialSignal = signals.any { signal -> signal in credentialSignals }
        val hasFinancialSignal = signals.any { signal -> signal in financialSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasFamilyVerificationSignal =
            signals.any { signal -> signal in familyVerificationSignals }
        val hasSecrecyImpersonationSignal =
            signals.any { signal -> signal in secrecyImpersonationSignals }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0
        val needsReadinessSupport = readiness.level != ReadinessLevel.READY

        val recommendedCardSet = linkedSetOf(
            RemoteAccessCutoffCardId.END_CALL_AND_SCREEN_SHARE,
            RemoteAccessCutoffCardId.USE_SEPARATE_DEVICE_AND_HELPER
        )
        if (hasNoAnalysisOrSignals) {
            recommendedCardSet += RemoteAccessCutoffCardId.entries
        }
        if (hasHighRisk) {
            recommendedCardSet += RemoteAccessCutoffCardId.HIDE_CODES_AND_FINANCIAL_SCREENS
            recommendedCardSet += RemoteAccessCutoffCardId.PRESERVE_SAFE_EVIDENCE
            recommendedCardSet += RemoteAccessCutoffCardId.CONTINUE_OFFICIAL_HELP_AND_FOLLOW_UP
        }
        if (hasRemoteAccessSignal) {
            recommendedCardSet += RemoteAccessCutoffCardId.END_CALL_AND_SCREEN_SHARE
            recommendedCardSet += RemoteAccessCutoffCardId.STOP_APP_FILE_PROFILE_INSTALL
            recommendedCardSet += RemoteAccessCutoffCardId.HIDE_CODES_AND_FINANCIAL_SCREENS
            recommendedCardSet += RemoteAccessCutoffCardId.USE_SEPARATE_DEVICE_AND_HELPER
            recommendedCardSet += RemoteAccessCutoffCardId.PRESERVE_SAFE_EVIDENCE
        }
        if (hasCredentialSignal) {
            recommendedCardSet += RemoteAccessCutoffCardId.HIDE_CODES_AND_FINANCIAL_SCREENS
            recommendedCardSet += RemoteAccessCutoffCardId.USE_SEPARATE_DEVICE_AND_HELPER
        }
        if (hasFinancialSignal) {
            recommendedCardSet += RemoteAccessCutoffCardId.HIDE_CODES_AND_FINANCIAL_SCREENS
            recommendedCardSet += RemoteAccessCutoffCardId.PRESERVE_SAFE_EVIDENCE
            recommendedCardSet += RemoteAccessCutoffCardId.CONTINUE_OFFICIAL_HELP_AND_FOLLOW_UP
        }
        if (hasLinkSignal) {
            recommendedCardSet += RemoteAccessCutoffCardId.STOP_APP_FILE_PROFILE_INSTALL
            recommendedCardSet += RemoteAccessCutoffCardId.PRESERVE_SAFE_EVIDENCE
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet += RemoteAccessCutoffCardId.END_CALL_AND_SCREEN_SHARE
            recommendedCardSet += RemoteAccessCutoffCardId.USE_SEPARATE_DEVICE_AND_HELPER
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedCardSet += RemoteAccessCutoffCardId.END_CALL_AND_SCREEN_SHARE
            recommendedCardSet += RemoteAccessCutoffCardId.PRESERVE_SAFE_EVIDENCE
            recommendedCardSet += RemoteAccessCutoffCardId.CONTINUE_OFFICIAL_HELP_AND_FOLLOW_UP
        }
        if (hasSavedIncident) {
            recommendedCardSet += RemoteAccessCutoffCardId.PRESERVE_SAFE_EVIDENCE
            recommendedCardSet += RemoteAccessCutoffCardId.CONTINUE_OFFICIAL_HELP_AND_FOLLOW_UP
        }
        if (needsReadinessSupport) {
            recommendedCardSet += RemoteAccessCutoffCardId.USE_SEPARATE_DEVICE_AND_HELPER
        }
        val recommendedCardIds = RemoteAccessCutoffCardId.entries
            .filter { cardId -> cardId in recommendedCardSet }

        val recommendedActionSet = linkedSetOf(
            RemoteAccessCutoffActionId.QUICK_STOP,
            RemoteAccessCutoffActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
            if (analysis == null) {
                RemoteAccessCutoffActionId.CHECKER
            } else {
                RemoteAccessCutoffActionId.ANALYSIS_RESULT
            },
            RemoteAccessCutoffActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            recommendedActionSet += RemoteAccessCutoffActionId.PRIVACY_CENTER
            recommendedActionSet += RemoteAccessCutoffActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += RemoteAccessCutoffActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += RemoteAccessCutoffActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += RemoteAccessCutoffActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += RemoteAccessCutoffActionId.EVIDENCE_KIT
            recommendedActionSet += RemoteAccessCutoffActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (hasHighRisk || hasFinancialSignal) {
            recommendedActionSet += RemoteAccessCutoffActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += RemoteAccessCutoffActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet += RemoteAccessCutoffActionId.EVIDENCE_KIT
            recommendedActionSet += RemoteAccessCutoffActionId.OFFICIAL_HELP_DIRECTORY
            if (analysis != null) {
                recommendedActionSet += RemoteAccessCutoffActionId.REPORT_PREP
            }
            recommendedActionSet += RemoteAccessCutoffActionId.RECOVERY_PLAN
            recommendedActionSet += RemoteAccessCutoffActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (hasRemoteAccessSignal) {
            recommendedActionSet += RemoteAccessCutoffActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += RemoteAccessCutoffActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += RemoteAccessCutoffActionId.EVIDENCE_KIT
        }
        if (hasCredentialSignal) {
            recommendedActionSet += RemoteAccessCutoffActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += RemoteAccessCutoffActionId.EVIDENCE_KIT
            recommendedActionSet += RemoteAccessCutoffActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (hasLinkSignal) {
            recommendedActionSet += RemoteAccessCutoffActionId.CHECKER
            recommendedActionSet += RemoteAccessCutoffActionId.PRIVACY_CENTER
            recommendedActionSet += RemoteAccessCutoffActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += RemoteAccessCutoffActionId.EVIDENCE_KIT
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += RemoteAccessCutoffActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += RemoteAccessCutoffActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += RemoteAccessCutoffActionId.TRUSTED_HELPER_REVIEW
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedActionSet += RemoteAccessCutoffActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += RemoteAccessCutoffActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (hasSavedIncident) {
            recommendedActionSet += RemoteAccessCutoffActionId.EVIDENCE_KIT
            recommendedActionSet += RemoteAccessCutoffActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        if (needsReadinessSupport) {
            recommendedActionSet += RemoteAccessCutoffActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += RemoteAccessCutoffActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += RemoteAccessCutoffActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += RemoteAccessCutoffActionId.FAMILY_READINESS_ROADMAP
        }
        val recommendedActions = RemoteAccessCutoffActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards(hasAnalysis = analysis != null)
        val recommendationTitle = when {
            analysis == null -> "분석 전에도 연락·화면공유부터 멈추는 보수적 안내"
            hasRemoteAccessSignal -> "현재 연락·화면공유·설치 지시를 직접 끝내기"
            hasCredentialSignal -> "인증 비밀과 민감 화면을 숨기고 별도 점검하기"
            hasHighRisk || hasFinancialSignal -> "돈·계정 행동을 멈추고 별도 경로로 점검하기"
            hasLinkSignal -> "링크·파일을 열지 않고 노출 여부를 직접 점검하기"
            hasFamilyVerificationSignal -> "현재 연락을 끝내고 평소 가족 경로로 확인하기"
            hasSecrecyImpersonationSignal -> "화면공유·연락을 비밀로 하지 않고 공식 경로 찾기"
            else -> "현재 연락을 끝내고 별도 기기·보호자와 점검하기"
        }

        return RemoteAccessCutoffCoach(
            title = "원격 접근 끊기 코치",
            intro =
                "앱 설치·화면공유·원격제어·스피커폰·영상통화·기기 설정 변경을 재촉받을 때 " +
                    "사용자가 직접 연락과 지시를 끝내고 기존 보호 경로를 고르는 로컬 전용·무상태 수동 안내입니다. " +
                    "이 앱은 원격 연결을 해제하거나 앱을 검사·제거하거나 기기 설정을 바꾸지 않습니다.",
            modeLabel = "지금 직접 끊기 · 화면을 숨기고 별도 도움으로 이동",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                analysis = analysis,
                readinessLevel = readiness.level,
                signals = signals,
                hasHighRisk = hasHighRisk,
                hasRemoteAccessSignal = hasRemoteAccessSignal,
                hasCredentialSignal = hasCredentialSignal,
                hasFinancialSignal = hasFinancialSignal,
                hasLinkSignal = hasLinkSignal,
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

    private fun buildCards(hasAnalysis: Boolean): List<RemoteAccessCutoffCard> {
        fun action(id: RemoteAccessCutoffActionId): RemoteAccessCutoffAction = actionFor(id)

        fun currentResultAction(): RemoteAccessCutoffAction = action(
            if (hasAnalysis) {
                RemoteAccessCutoffActionId.ANALYSIS_RESULT
            } else {
                RemoteAccessCutoffActionId.CHECKER
            }
        )

        return listOf(
            RemoteAccessCutoffCard(
                id = RemoteAccessCutoffCardId.END_CALL_AND_SCREEN_SHARE,
                displayOrder = RemoteAccessCutoffCardId.END_CALL_AND_SCREEN_SHARE.displayOrder,
                title = "화면공유·통화 지시 끊기",
                cutoffSentence =
                    "현재 통화·채팅·영상통화·화면공유를 제가 직접 끝내고 더는 지시를 따르지 않겠습니다.",
                whyItMatters =
                    "현재 연락과 화면 노출이 이어지면 상대의 압박 속에서 설치·인증·금융 행동을 계속하기 쉽습니다.",
                doNotDoBoundaries = listOf(
                    "이 버튼이 통화·화면공유·원격 연결을 자동으로 끝냈다고 여기지 마세요.",
                    "상대와 논쟁하거나 스피커폰·영상통화·화면공유를 유지한 채 확인하지 마세요."
                ),
                primaryAction = action(RemoteAccessCutoffActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(RemoteAccessCutoffActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(RemoteAccessCutoffActionId.SAFE_CALL_SCRIPTS),
                    action(RemoteAccessCutoffActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            RemoteAccessCutoffCard(
                id = RemoteAccessCutoffCardId.STOP_APP_FILE_PROFILE_INSTALL,
                displayOrder = RemoteAccessCutoffCardId.STOP_APP_FILE_PROFILE_INSTALL.displayOrder,
                title = "앱 설치·파일·프로필 요구 중단",
                cutoffSentence =
                    "상대가 요구한 링크·앱·파일·프로필을 열거나 설치하지 않고 기기 설정도 바꾸지 않겠습니다.",
                whyItMatters =
                    "설치 파일·프로필·설정 변경 요구는 화면과 계정 정보를 더 노출시키는 다음 행동으로 이어질 수 있습니다.",
                doNotDoBoundaries = listOf(
                    "상대가 보낸 링크·파일·프로필을 다시 열거나 앱 이름·설치 목록을 이 화면에 적지 마세요.",
                    "이 코치가 앱·파일·기기를 검사하거나 악성 앱을 찾아 제거했다고 여기지 마세요."
                ),
                primaryAction = action(RemoteAccessCutoffActionId.TRUSTED_HELPER_REVIEW),
                secondaryActions = listOf(
                    action(RemoteAccessCutoffActionId.ACCOUNT_LOCKDOWN),
                    action(RemoteAccessCutoffActionId.EVIDENCE_KIT),
                    action(RemoteAccessCutoffActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(RemoteAccessCutoffActionId.PRIVACY_CENTER),
                    currentResultAction()
                )
            ),
            RemoteAccessCutoffCard(
                id = RemoteAccessCutoffCardId.HIDE_CODES_AND_FINANCIAL_SCREENS,
                displayOrder =
                    RemoteAccessCutoffCardId.HIDE_CODES_AND_FINANCIAL_SCREENS.displayOrder,
                title = "인증번호·금융화면 숨기기",
                cutoffSentence =
                    "인증번호·PIN·비밀번호를 말하거나 보내지 않고 계정·카드·송금 화면을 보여주지 않겠습니다.",
                whyItMatters =
                    "화면공유나 통화 지시가 이어지는 동안 새 인증 비밀과 금융 화면도 상대에게 보일 수 있습니다.",
                doNotDoBoundaries = listOf(
                    "인증번호·PIN·비밀번호·보안카드·계좌·카드 전체 값이나 신분정보를 입력하거나 보여주지 마세요.",
                    "자격정보가 보이는 화면·사진을 증거라며 공유하거나 계정·카드가 자동 정지됐다고 여기지 마세요."
                ),
                primaryAction = action(RemoteAccessCutoffActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(RemoteAccessCutoffActionId.MONEY_TRANSFER_PAUSE_GATE),
                    action(RemoteAccessCutoffActionId.EVIDENCE_KIT),
                    action(RemoteAccessCutoffActionId.OFFICIAL_HELP_DIRECTORY)
                )
            ),
            RemoteAccessCutoffCard(
                id = RemoteAccessCutoffCardId.USE_SEPARATE_DEVICE_AND_HELPER,
                displayOrder =
                    RemoteAccessCutoffCardId.USE_SEPARATE_DEVICE_AND_HELPER.displayOrder,
                title = "민감 계정은 별도 기기/보호자와 점검",
                cutoffSentence =
                    "현재 연락과 의심 기기에서 민감 계정 작업을 멈추고 가능하면 보호자와 분리된 기기에서 공식 경로를 직접 확인하겠습니다.",
                whyItMatters =
                    "현재 연락·화면공유와 분리된 환경에서 가족이나 보호자와 함께 보면 상대의 지시를 계속 따를 가능성을 줄일 수 있습니다.",
                doNotDoBoundaries = listOf(
                    "현재 상대가 준 번호·링크·앱을 공식 확인 경로로 사용하지 마세요.",
                    "보호자에게도 이름·연락처·계정값·인증 비밀·가족 확인 답을 이 코치에 입력하게 하지 마세요."
                ),
                primaryAction = action(RemoteAccessCutoffActionId.TRUSTED_HELPER_REVIEW),
                secondaryActions = listOf(
                    action(RemoteAccessCutoffActionId.SAFE_CALL_SCRIPTS),
                    action(RemoteAccessCutoffActionId.FAMILY_ALERT_MESSAGES),
                    action(RemoteAccessCutoffActionId.FAMILY_READINESS_ROADMAP),
                    action(RemoteAccessCutoffActionId.ACCOUNT_LOCKDOWN)
                )
            ),
            RemoteAccessCutoffCard(
                id = RemoteAccessCutoffCardId.PRESERVE_SAFE_EVIDENCE,
                displayOrder = RemoteAccessCutoffCardId.PRESERVE_SAFE_EVIDENCE.displayOrder,
                title = "증거는 비밀값 없이 보존",
                cutoffSentence =
                    "연락 방식·요구 행동·발생 순서 같은 일반 범주만 직접 남기고 비밀값과 자격정보 화면은 퍼뜨리지 않겠습니다.",
                whyItMatters =
                    "비밀값을 다시 노출하지 않는 범위에서 상황 종류를 남기면 가족과 공식 창구에서 다음 수동 조치를 고르기 쉽습니다.",
                doNotDoBoundaries = listOf(
                    "비밀번호·인증번호·계정·카드·신분정보가 보이는 화면이나 원문을 공유하지 마세요.",
                    "증거를 만든다며 상대에게 다시 답하거나 링크·파일·앱을 다시 열거나 성급히 삭제하지 마세요."
                ),
                primaryAction = action(RemoteAccessCutoffActionId.EVIDENCE_KIT),
                secondaryActions = listOf(
                    action(RemoteAccessCutoffActionId.OFFICIAL_HELP_DIRECTORY),
                    action(RemoteAccessCutoffActionId.REPORT_PREP),
                    currentResultAction()
                )
            ),
            RemoteAccessCutoffCard(
                id = RemoteAccessCutoffCardId.CONTINUE_OFFICIAL_HELP_AND_FOLLOW_UP,
                displayOrder =
                    RemoteAccessCutoffCardId.CONTINUE_OFFICIAL_HELP_AND_FOLLOW_UP.displayOrder,
                title = "공식 도움·후속 점검으로 이어가기",
                cutoffSentence =
                    "현재 연락을 끝낸 뒤 앱 안의 공식 도움·수습·후속 점검 경로를 제가 직접 골라 확인하겠습니다.",
                whyItMatters =
                    "연락을 끝낸 뒤에도 계정·금융·기기 노출 범주에 맞는 공식 확인과 며칠 뒤 점검을 이어갈 수 있습니다.",
                doNotDoBoundaries = listOf(
                    "이 코치가 신고·계정 조치·결제 취소·환급·복구를 대신하거나 결과를 보장한다고 여기지 마세요.",
                    "현재 상대가 알려준 기관·금융회사 연락처나 링크를 공식 도움 경로로 사용하지 마세요."
                ),
                primaryAction = action(RemoteAccessCutoffActionId.OFFICIAL_HELP_DIRECTORY),
                secondaryActions = listOf(
                    action(RemoteAccessCutoffActionId.RECOVERY_PLAN),
                    action(RemoteAccessCutoffActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                    action(RemoteAccessCutoffActionId.SAFETY_FLOW_ROUTER)
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readinessLevel: ReadinessLevel,
        signals: Set<ScamSignal>,
        hasHighRisk: Boolean,
        hasRemoteAccessSignal: Boolean,
        hasCredentialSignal: Boolean,
        hasFinancialSignal: Boolean,
        hasLinkSignal: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasSecrecyImpersonationSignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (analysis == null || signals.isEmpty()) {
                add(
                    "현재 분석이 없거나 일치 신호가 없습니다. 이를 현재 연락이나 기기가 문제없다는 " +
                        "판정으로 보지 말고 연락·화면공유·설치·정보·금융 행동을 보수적으로 멈추세요."
                )
            }
            if (hasHighRisk) {
                add(
                    "위험도가 높아 현재 연락을 끝내고 인증·금융 화면을 숨긴 뒤 계정·증거·공식 " +
                        "도움 경로를 직접 고르도록 권합니다."
                )
            }
            if (hasRemoteAccessSignal) {
                add(
                    "앱 설치·원격제어·통화 유지·화면공유 신호가 있어 연락과 화면 노출을 직접 " +
                        "끝내고 보호자·계정·증거 경로를 권합니다. 이 코치는 원격 연결을 해제하거나 " +
                        "악성 앱을 제거하지 않습니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증정보 요구 가능성이 있어 코드와 금융 화면을 숨기고 계정 보호 경로를 " +
                        "권합니다. 계정이나 카드를 자동 정지·동결하는 기능은 아닙니다."
                )
            }
            if (hasFinancialSignal) {
                add(
                    "송금·결제·금융 압박 신호가 있어 송금 전 멈춤과 계정·증거·공식 도움 경로를 " +
                        "권합니다. 결제 취소·환급·피해금 회수를 대신하거나 보장하지 않습니다."
                )
            }
            if (hasLinkSignal) {
                add(
                    "링크·스미싱 신호가 있어 링크·파일을 열지 않고 노출 영향·개인정보·검사 " +
                        "화면을 직접 고르도록 권합니다. 이 코치는 링크나 파일을 스캔하지 않습니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 긴급상황·목소리 합성·번호 변경 신호가 있어 현재 연락을 끝낸 뒤 평소 " +
                        "알던 가족 콜백·확인 전화 문장을 사용하도록 권합니다."
                )
            }
            if (hasSecrecyImpersonationSignal) {
                add(
                    "비밀 유지·기관 사칭 압박 신호가 있어 화면공유나 현재 연락을 비밀로 하지 " +
                        "않고 가족과 사용자가 직접 찾은 공식 경로에 알리도록 권합니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만 반영하며 사건 내용은 읽지 않습니다. " +
                        "비밀값 없는 증거와 후속 점검 경로만 함께 권합니다."
                )
            }
            if (isEmpty()) {
                add(
                    "현재 신호만으로 원격 접근이나 피해를 확정하지 않고 연락을 끝낸 뒤 별도 " +
                        "기기·보호자·공식 경로로 직접 확인하세요."
                )
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW ->
                "가족 준비가 부족한 상태라 콜백·가족 경고·보호자 검토·준비 로드맵을 함께 권합니다. 실제 가족 값은 이 화면에 입력하지 마세요."
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중이라 콜백·가족 경고·보호자 검토·준비 로드맵으로 빠진 부분을 직접 확인하도록 권합니다. 저장된 실제 값은 추천에 들어오지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있어도 이번 연락·화면공유·기기 상태나 조치 완료가 확인된 것은 아닙니다. 현재 연락을 끝내고 별도 경로로 다시 확인하세요."
        }
        return "$situationCopy $readinessCopy"
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 사용자가 화면을 직접 읽고 직접 선택하는 로컬 전용·무상태 수동 안내입니다.",
        "추천은 위험도·일치 신호, 값 없는 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·사건 내용·가족 안전 계획 값을 사용하지 않습니다.",
        "이름·연락처·정확한 전화번호·계정·계좌·카드·OTP·PIN·비밀번호·가족 확인 답·주민번호·기기 식별자·정확한 URL·도메인·앱 이름·패키지 이름을 묻거나 저장하지 않습니다.",
        "카드 선택·추천·읽음·공유·체크·메모·클립보드 내용·원격 접근 상태·설치 앱 목록·패키지 이름·기기 상태·진행·이력을 위한 새 저장소를 만들거나 기록하지 않습니다.",
        "문자·메시지·통화·연락처·클립보드·계정·기기 화면을 자동으로 읽거나 감시하지 않습니다.",
        "네트워크를 사용하거나 새 Android 권한을 요청하지 않습니다.",
        "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커를 만들거나 예약하지 않으며 스케줄러를 사용하지 않습니다.",
        "전화를 걸거나 끊거나 선별·차단하지 않고 문자를 보내거나 받는 사람을 고르지 않으며 공유도 자동 전송하지 않습니다.",
        "기기 관리자·접근성 서비스·VPN·알림 접근·알림 리스너·사용 기록·앱 작업 권한을 사용하지 않고 설치 앱·패키지 목록을 검사하지 않습니다.",
        "원문·화면·증거를 자동으로 가리거나 스캔·삭제하지 않고 자동 모니터링도 하지 않습니다.",
        "앱·파일·프로필을 설치하거나 제거하지 않고, 기기·계정·접근성·네트워크·VPN·알림 설정을 변경하지 않으며 원격 연결 해제·악성 앱 제거·기기 정리를 수행하지 않습니다.",
        "결제·은행 API, NFC나 Android 계정 관리자에 접근하지 않고 송금·결제 취소·계정 또는 카드 정지·동결·환급·피해 복구를 자동 수행하지 않습니다.",
        "붙여넣은 글, 통화 상태, 연락처, 사건 기록, 가족 안전 계획, 계정·카드·은행·기기 설정, 설치 앱·패키지 상태나 돈의 움직임을 추가·수정·삭제하지 않습니다.",
        "이 안내는 사기·본인확인·원격 연결 종료·악성 앱 제거·기기 정리·조치 완료·계정 또는 카드 조치·결제 취소·환급·피해 회복·법률·금융 결과를 증명하거나 보장하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<RemoteAccessCutoffCardId>,
        recommendedActions: List<RemoteAccessCutoffAction>,
        cards: List<RemoteAccessCutoffCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 원격 접근 끊기 코치")
            appendLine()
            appendLine("지금 직접 읽을 추천 끊기 문장")
            recommendedCardIds.forEach { cardId ->
                val card = cardsById.getValue(cardId)
                appendLine("- ${card.title}: ${card.cutoffSentence}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("전체 수동 끊기 카드")
            cards.forEach { card -> appendLine("- ${card.title}") }
            appendLine()
            append(
                "원문·사람·연락처·비밀값·기기값을 넣지 않은 일반 수동 문장과 기존 경로 " +
                    "이름입니다. 가족이 직접 읽고 필요한 경로를 직접 고릅니다."
            )
        }.trim()
    }

    private fun actionFor(
        id: RemoteAccessCutoffActionId
    ): RemoteAccessCutoffAction = RemoteAccessCutoffAction(
        id = id,
        label = when (id) {
            RemoteAccessCutoffActionId.QUICK_STOP -> "긴급 멈춤 모드"
            RemoteAccessCutoffActionId.ACTIVE_CONTACT_EXIT_SCRIPTS -> "진행 중 연락 종료 문장"
            RemoteAccessCutoffActionId.CHECKER -> "의심 내용 검사"
            RemoteAccessCutoffActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            RemoteAccessCutoffActionId.PRIVACY_CENTER -> "개인정보 센터"
            RemoteAccessCutoffActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
            RemoteAccessCutoffActionId.SAFE_CALL_SCRIPTS ->
                "값 없는 가족 콜백·안전 확인 전화 스크립트"
            RemoteAccessCutoffActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
            RemoteAccessCutoffActionId.TRUSTED_HELPER_REVIEW -> "보호자와 함께 안전 검토"
            RemoteAccessCutoffActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
            RemoteAccessCutoffActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
            RemoteAccessCutoffActionId.MONEY_TRANSFER_PAUSE_GATE -> "송금 전 멈춤 확인"
            RemoteAccessCutoffActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            RemoteAccessCutoffActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처"
            RemoteAccessCutoffActionId.REPORT_PREP -> "신고용 상담 준비 카드"
            RemoteAccessCutoffActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
            RemoteAccessCutoffActionId.RECOVERY_PLAN -> "24시간 수습 계획"
            RemoteAccessCutoffActionId.FOLLOW_UP_CHECK_IN_PLANNER -> "며칠 뒤 후속 점검"
        }
    )
}
