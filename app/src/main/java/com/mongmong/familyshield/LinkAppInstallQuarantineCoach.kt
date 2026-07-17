package com.mongmong.familyshield

enum class LinkAppInstallQuarantineCardId(val displayOrder: Int) {
    DO_NOT_OPEN_PREVIEW_COPY_SCAN_OR_SHARE_LINK(1),
    STOP_APP_INSTALL_AND_PERMISSION_PRESSURE(2),
    LEAVE_CALL_CHAT_OR_SCREEN_SHARE_SESSION(3),
    KEEP_CREDENTIAL_PAYMENT_AND_CODE_VALUES_OUT(4),
    SEPARATE_DELIVERY_INVITATION_FINE_AND_REFUND(5),
    VALUE_FREE_EXPOSURE_EVIDENCE_HELP_AND_FOLLOW_UP(6)
}

enum class LinkAppInstallQuarantineActionId {
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
    GIFT_CARD_CODE_PAUSE_COACH,
    OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
    RECOVERY_PROMISE_REALITY_CHECK_COACH,
    ACCOUNT_LOCKDOWN,
    REMOTE_ACCESS_CUTOFF_COACH,
    EXPOSURE_IMPACT_TRIAGE,
    PRIVACY_CENTER,
    EVIDENCE_HANDOFF_PRIVACY_COACH,
    EVIDENCE_KIT,
    INCIDENT_TIMELINE_BRIEFING,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    FOLLOW_UP_CHECK_IN_PLANNER,
    SAFETY_FLOW_ROUTER
}

data class LinkAppInstallQuarantineAction(
    val id: LinkAppInstallQuarantineActionId,
    val label: String
)

data class LinkAppInstallQuarantineCard(
    val id: LinkAppInstallQuarantineCardId,
    val displayOrder: Int,
    val title: String,
    val linkAppPressureScenario: String,
    val valueFreeActionSentence: String,
    val whyItMatters: String,
    val doNotOpenInstallEnterScanGrantOrShareBoundaries: List<String>,
    val primaryAction: LinkAppInstallQuarantineAction,
    val secondaryActions: List<LinkAppInstallQuarantineAction>
) {
    val routeActions: List<LinkAppInstallQuarantineAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class LinkAppInstallQuarantineCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<LinkAppInstallQuarantineCardId>,
    val recommendedActions: List<LinkAppInstallQuarantineAction>,
    val cards: List<LinkAppInstallQuarantineCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<LinkAppInstallQuarantineCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<LinkAppInstallQuarantineActionId>
        get() = recommendedActions.map { action -> action.id }
}

object LinkAppInstallQuarantineCoachBuilder {
    private val linkSignals = setOf(
        ScamSignal.LINK,
        ScamSignal.PARCEL_INVITATION_SMISHING
    )

    private val appInstallAndLiveControlSignals = setOf(
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
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val recoveryPromiseSignals = setOf(
        ScamSignal.BAIT,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val officialPressureSignals = setOf(
        ScamSignal.IMPERSONATION,
        ScamSignal.SECRECY
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
    ): LinkAppInstallQuarantineCoach {
        val riskLevel = analysis?.riskLevel
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val readinessLevel = readiness.level
        val hasNoAnalysisOrSignals = analysis == null || signals.isEmpty()
        val hasHighRisk = riskLevel == RiskLevel.HIGH
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasAppInstallOrLiveControlSignal = signals.any { signal ->
            signal in appInstallAndLiveControlSignals
        }
        val hasCredentialSignal = signals.any { signal -> signal in credentialSignals }
        val hasFinancialSignal = signals.any { signal -> signal in financialSignals }
        val hasRecoveryPromiseSignal = signals.any { signal ->
            signal in recoveryPromiseSignals
        }
        val hasOfficialPressureSignal = signals.any { signal ->
            signal in officialPressureSignals
        }
        val hasFamilyVerificationSignal = signals.any { signal ->
            signal in familyVerificationSignals
        }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0

        val recommendedCardSet = linkedSetOf(
            LinkAppInstallQuarantineCardId.DO_NOT_OPEN_PREVIEW_COPY_SCAN_OR_SHARE_LINK,
            LinkAppInstallQuarantineCardId.LEAVE_CALL_CHAT_OR_SCREEN_SHARE_SESSION
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += LinkAppInstallQuarantineCardId.entries
        }
        if (hasLinkSignal) {
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.DO_NOT_OPEN_PREVIEW_COPY_SCAN_OR_SHARE_LINK
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.LEAVE_CALL_CHAT_OR_SCREEN_SHARE_SESSION
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.SEPARATE_DELIVERY_INVITATION_FINE_AND_REFUND
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.VALUE_FREE_EXPOSURE_EVIDENCE_HELP_AND_FOLLOW_UP
        }
        if (hasAppInstallOrLiveControlSignal) {
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.STOP_APP_INSTALL_AND_PERMISSION_PRESSURE
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.LEAVE_CALL_CHAT_OR_SCREEN_SHARE_SESSION
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.KEEP_CREDENTIAL_PAYMENT_AND_CODE_VALUES_OUT
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.VALUE_FREE_EXPOSURE_EVIDENCE_HELP_AND_FOLLOW_UP
        }
        if (hasCredentialSignal) {
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.LEAVE_CALL_CHAT_OR_SCREEN_SHARE_SESSION
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.KEEP_CREDENTIAL_PAYMENT_AND_CODE_VALUES_OUT
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.STOP_APP_INSTALL_AND_PERMISSION_PRESSURE
        }
        if (hasFinancialSignal || hasRecoveryPromiseSignal) {
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.KEEP_CREDENTIAL_PAYMENT_AND_CODE_VALUES_OUT
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.SEPARATE_DELIVERY_INVITATION_FINE_AND_REFUND
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.VALUE_FREE_EXPOSURE_EVIDENCE_HELP_AND_FOLLOW_UP
        }
        if (hasOfficialPressureSignal) {
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.LEAVE_CALL_CHAT_OR_SCREEN_SHARE_SESSION
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.SEPARATE_DELIVERY_INVITATION_FINE_AND_REFUND
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.VALUE_FREE_EXPOSURE_EVIDENCE_HELP_AND_FOLLOW_UP
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.LEAVE_CALL_CHAT_OR_SCREEN_SHARE_SESSION
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.SEPARATE_DELIVERY_INVITATION_FINE_AND_REFUND
        }
        if (hasSavedIncident) {
            recommendedCardSet +=
                LinkAppInstallQuarantineCardId.VALUE_FREE_EXPOSURE_EVIDENCE_HELP_AND_FOLLOW_UP
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedCardSet +=
                    LinkAppInstallQuarantineCardId.LEAVE_CALL_CHAT_OR_SCREEN_SHARE_SESSION
                recommendedCardSet +=
                    LinkAppInstallQuarantineCardId.SEPARATE_DELIVERY_INVITATION_FINE_AND_REFUND
            }
            ReadinessLevel.READY -> {
                recommendedCardSet +=
                    LinkAppInstallQuarantineCardId.DO_NOT_OPEN_PREVIEW_COPY_SCAN_OR_SHARE_LINK
                recommendedCardSet +=
                    LinkAppInstallQuarantineCardId.STOP_APP_INSTALL_AND_PERMISSION_PRESSURE
            }
        }
        val recommendedCardIds = LinkAppInstallQuarantineCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            LinkAppInstallQuarantineActionId.CHECKER
        } else {
            LinkAppInstallQuarantineActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            currentResultActionId,
            LinkAppInstallQuarantineActionId.QUICK_STOP,
            LinkAppInstallQuarantineActionId.SAFE_PASTE_PRIVACY_COACH,
            LinkAppInstallQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
            LinkAppInstallQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH,
            LinkAppInstallQuarantineActionId.EXPOSURE_IMPACT_TRIAGE,
            LinkAppInstallQuarantineActionId.PRIVACY_CENTER,
            LinkAppInstallQuarantineActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            LinkAppInstallQuarantineActionId.entries
                .filterNot { actionId ->
                    actionId == LinkAppInstallQuarantineActionId.ANALYSIS_RESULT
                }
                .forEach { actionId -> recommendedActionSet += actionId }
        }
        if (hasHighRisk) {
            LinkAppInstallQuarantineActionId.entries.forEach { actionId ->
                recommendedActionSet += actionId
            }
        }
        if (hasLinkSignal) {
            recommendedActionSet += LinkAppInstallQuarantineActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += LinkAppInstallQuarantineActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet += LinkAppInstallQuarantineActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += LinkAppInstallQuarantineActionId.REPORT_PREP
        }
        if (hasAppInstallOrLiveControlSignal) {
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += LinkAppInstallQuarantineActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += LinkAppInstallQuarantineActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet += LinkAppInstallQuarantineActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet += LinkAppInstallQuarantineActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
        }
        if (hasCredentialSignal) {
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet += LinkAppInstallQuarantineActionId.GIFT_CARD_CODE_PAUSE_COACH
            recommendedActionSet += LinkAppInstallQuarantineActionId.SAFE_PASTE_PRIVACY_COACH
            recommendedActionSet += LinkAppInstallQuarantineActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH
            recommendedActionSet += LinkAppInstallQuarantineActionId.EXPOSURE_IMPACT_TRIAGE
        }
        if (hasFinancialSignal || hasRecoveryPromiseSignal) {
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.MONEY_TRANSFER_PAUSE_GATE
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += LinkAppInstallQuarantineActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += LinkAppInstallQuarantineActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += LinkAppInstallQuarantineActionId.REPORT_PREP
        }
        if (hasOfficialPressureSignal) {
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
            recommendedActionSet += LinkAppInstallQuarantineActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.FAMILY_CALM_DEBRIEF_AGENDA
            recommendedActionSet += LinkAppInstallQuarantineActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += LinkAppInstallQuarantineActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += LinkAppInstallQuarantineActionId.REPORT_PREP
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
            recommendedActionSet += LinkAppInstallQuarantineActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.NEW_NUMBER_QUARANTINE_COACH
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.FAMILY_SAFE_WORD_RESET_COACH
            recommendedActionSet += LinkAppInstallQuarantineActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += LinkAppInstallQuarantineActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasSavedIncident) {
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            recommendedActionSet += LinkAppInstallQuarantineActionId.EVIDENCE_KIT
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.INCIDENT_TIMELINE_BRIEFING
            recommendedActionSet +=
                LinkAppInstallQuarantineActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedActionSet += LinkAppInstallQuarantineActionId.SAFETY_PLAN
                recommendedActionSet +=
                    LinkAppInstallQuarantineActionId.FAMILY_SAFE_WORD_RESET_COACH
                recommendedActionSet +=
                    LinkAppInstallQuarantineActionId.FAMILY_READINESS_ROADMAP
                recommendedActionSet +=
                    LinkAppInstallQuarantineActionId.FAMILY_SAFETY_DRILL_COACH
                recommendedActionSet += LinkAppInstallQuarantineActionId.SAFE_CALL_SCRIPTS
            }
            ReadinessLevel.READY -> {
                recommendedActionSet += LinkAppInstallQuarantineActionId.FAMILY_CALLBACK_CARD
                recommendedActionSet +=
                    LinkAppInstallQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                recommendedActionSet += LinkAppInstallQuarantineActionId.SAFE_CALL_SCRIPTS
            }
        }
        val recommendedActions = LinkAppInstallQuarantineActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null -> "검사 결과가 없어도 모든 링크·앱 설치 격리 카드를 보수적으로 읽기"
            hasLinkSignal -> "상대가 준 링크를 열거나 미리 보지 않고 별도 경로로 분리하기"
            hasAppInstallOrLiveControlSignal -> "앱 설치·권한 허용·통화 유지 지시를 함께 멈추기"
            hasCredentialSignal -> "링크·앱 안에 로그인·인증·결제 비밀값을 넣지 않기"
            hasFinancialSignal || hasRecoveryPromiseSignal ->
                "송금·환급·회복 압박과 링크·앱 행동을 함께 멈추기"
            hasOfficialPressureSignal -> "기관·직함 압박을 같은 링크·연락 세션에서 분리하기"
            hasFamilyVerificationSignal -> "가족 위급 주장도 링크·앱 없이 별도 경로로 확인하기"
            hasHighRisk -> "높은 위험 범주에서 값 없는 링크·앱 격리 순서를 넓게 고르기"
            hasSavedIncident -> "사건 내용 없이 노출·증거·도움·후속 경로만 고르기"
            else -> "링크를 누르거나 앱을 설치하기 전에 같은 세션을 멈추기"
        }

        return LinkAppInstallQuarantineCoach(
            title = "링크·앱 설치 격리 코치",
            intro =
                "택배·청첩장·부고·벌금·환급 링크나 APK·보안앱·원격앱 설치 압박 " +
                    "앞에서 링크와 앱을 읽거나 검사하지 않고, 고정 카드와 기존 대응 " +
                    "화면만 직접 고르는 로컬 전용·무상태 수동 안내입니다.",
            modeLabel = "링크 열기 없음 · 앱 설치 없음 · 값 입력 없음",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                readinessLevel = readinessLevel,
                hasNoAnalysisOrSignals = hasNoAnalysisOrSignals,
                hasHighRisk = hasHighRisk,
                hasLinkSignal = hasLinkSignal,
                hasAppInstallOrLiveControlSignal = hasAppInstallOrLiveControlSignal,
                hasCredentialSignal = hasCredentialSignal,
                hasFinancialSignal = hasFinancialSignal,
                hasRecoveryPromiseSignal = hasRecoveryPromiseSignal,
                hasOfficialPressureSignal = hasOfficialPressureSignal,
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

    private fun buildCards(): List<LinkAppInstallQuarantineCard> {
        fun action(
            id: LinkAppInstallQuarantineActionId
        ): LinkAppInstallQuarantineAction = actionFor(id)

        return listOf(
            LinkAppInstallQuarantineCard(
                id =
                    LinkAppInstallQuarantineCardId
                        .DO_NOT_OPEN_PREVIEW_COPY_SCAN_OR_SHARE_LINK,
                displayOrder =
                    LinkAppInstallQuarantineCardId
                        .DO_NOT_OPEN_PREVIEW_COPY_SCAN_OR_SHARE_LINK.displayOrder,
                title = "상대가 준 링크를 열어 확인하지 않기",
                linkAppPressureScenario =
                    "문자나 통화 상대가 배송·초대·부고·벌금·환급·보안 안내라며 링크를 바로 누르라고 하는 때",
                valueFreeActionSentence =
                    "링크를 열거나 미리 보기·복사·공유·스캔하지 않고 기존 개인정보·별도 경로 화면만 직접 고르겠습니다.",
                whyItMatters =
                    "상대가 고른 링크 안에서 확인을 이어가면 로그인·인증·결제 입력 압박이 같은 흐름으로 이어질 수 있습니다.",
                doNotOpenInstallEnterScanGrantOrShareBoundaries = listOf(
                    "URL·도메인·단축 주소·QR·바코드·발신자·문서 값을 적거나 붙여넣거나 공유하지 마세요.",
                    "링크를 열기·미리 보기·가져오기·해결·정규화·확장·분류·평판 조회·검증·스캔·샌드박스 실행하지 않습니다."
                ),
                primaryAction = action(
                    LinkAppInstallQuarantineActionId.SAFE_PASTE_PRIVACY_COACH
                ),
                secondaryActions = listOf(
                    action(
                        LinkAppInstallQuarantineActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH
                    ),
                    action(LinkAppInstallQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(LinkAppInstallQuarantineActionId.PRIVACY_CENTER)
                )
            ),
            LinkAppInstallQuarantineCard(
                id =
                    LinkAppInstallQuarantineCardId.STOP_APP_INSTALL_AND_PERMISSION_PRESSURE,
                displayOrder =
                    LinkAppInstallQuarantineCardId
                        .STOP_APP_INSTALL_AND_PERMISSION_PRESSURE.displayOrder,
                title = "APK·보안앱·원격앱 설치와 권한 허용 멈추기",
                linkAppPressureScenario =
                    "도움을 받으려면 앱을 설치하고 접근 권한이나 기기 설정을 바꿔야 한다고 재촉하는 때",
                valueFreeActionSentence =
                    "앱 설치·제거·실행과 권한·설정 변경을 하지 않고 기존 원격지시 중단·계정·노출 경로만 살펴보겠습니다.",
                whyItMatters =
                    "앱 설치와 권한 허용을 서두르게 하는 흐름은 화면·입력·기기 행동을 상대 지시에 묶을 수 있습니다.",
                doNotOpenInstallEnterScanGrantOrShareBoundaries = listOf(
                    "APK·패키지·앱 이름·파일·경로·기기 식별값·설치 목록·권한 상태를 적거나 공유하지 마세요.",
                    "앱이나 패키지를 검사·조회·설치·제거·비활성화·강제 종료하지 않고 권한 허용·철회·설정 변경·악성 프로그램 검사·제거·기기 정리를 수행하지 않습니다."
                ),
                primaryAction = action(
                    LinkAppInstallQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH
                ),
                secondaryActions = listOf(
                    action(LinkAppInstallQuarantineActionId.ACCOUNT_LOCKDOWN),
                    action(LinkAppInstallQuarantineActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(LinkAppInstallQuarantineActionId.TRUSTED_HELPER_REVIEW)
                )
            ),
            LinkAppInstallQuarantineCard(
                id =
                    LinkAppInstallQuarantineCardId.LEAVE_CALL_CHAT_OR_SCREEN_SHARE_SESSION,
                displayOrder =
                    LinkAppInstallQuarantineCardId
                        .LEAVE_CALL_CHAT_OR_SCREEN_SHARE_SESSION.displayOrder,
                title = "같은 통화·채팅·화면공유 세션에서 입력하지 않기",
                linkAppPressureScenario =
                    "통화나 채팅을 유지하고 화면을 보여 주면서 링크·앱·로그인 순서를 따라 하라고 하는 때",
                valueFreeActionSentence =
                    "같은 연락·화면공유 세션에서 누르기·설치·허용·로그인·입력을 이어가지 않고 종료 문구를 직접 고르겠습니다.",
                whyItMatters =
                    "연락을 계속 유지하면 생각할 시간을 잃고 상대가 화면과 다음 행동을 계속 통제하기 쉽습니다.",
                doNotOpenInstallEnterScanGrantOrShareBoundaries = listOf(
                    "통화·채팅·프로필·채널·화면·알림·받는 사람·연락처 값을 적거나 공유하지 마세요.",
                    "전화 종료·차단·선별, 문자 발송, 화면 녹화, 원격 연결 종료, 앱 조작이나 기기 제어를 자동 수행하지 않습니다."
                ),
                primaryAction = action(
                    LinkAppInstallQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS
                ),
                secondaryActions = listOf(
                    action(LinkAppInstallQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH),
                    action(
                        LinkAppInstallQuarantineActionId.CREDENTIAL_SECRET_FREEZE_COACH
                    ),
                    action(LinkAppInstallQuarantineActionId.RECONTACT_BOUNDARY_COACH)
                )
            ),
            LinkAppInstallQuarantineCard(
                id =
                    LinkAppInstallQuarantineCardId
                        .KEEP_CREDENTIAL_PAYMENT_AND_CODE_VALUES_OUT,
                displayOrder =
                    LinkAppInstallQuarantineCardId
                        .KEEP_CREDENTIAL_PAYMENT_AND_CODE_VALUES_OUT.displayOrder,
                title = "로그인·OTP·PIN·비밀번호·카드·상품권 코드를 링크 안에 넣지 않기",
                linkAppPressureScenario =
                    "링크나 앱 화면에서 로그인하거나 인증번호·비밀값·결제·상품권 정보를 넣어야 확인된다고 하는 때",
                valueFreeActionSentence =
                    "인증·계정·결제·상품권 값을 말하거나 입력하지 않고 기존 비밀값·계정·돈 멈춤 경로만 고르겠습니다.",
                whyItMatters =
                    "링크나 앱 화면의 요구만으로는 비밀값과 돈 정보를 건네도 되는지 판단할 수 없습니다.",
                doNotOpenInstallEnterScanGrantOrShareBoundaries = listOf(
                    "OTP·PIN·비밀번호·보안 답·주민 식별값·계정·카드·은행·지갑·거래·상품권·코드 값을 입력·복사·공유하지 마세요.",
                    "로그인·인증·결제·송금·구매·잔액 조회·사용·환급·차지백·동결·취소·추적·회복을 수행하거나 확인하지 않습니다."
                ),
                primaryAction = action(
                    LinkAppInstallQuarantineActionId.CREDENTIAL_SECRET_FREEZE_COACH
                ),
                secondaryActions = listOf(
                    action(LinkAppInstallQuarantineActionId.ACCOUNT_LOCKDOWN),
                    action(LinkAppInstallQuarantineActionId.MONEY_TRANSFER_PAUSE_GATE),
                    action(LinkAppInstallQuarantineActionId.GIFT_CARD_CODE_PAUSE_COACH)
                )
            ),
            LinkAppInstallQuarantineCard(
                id =
                    LinkAppInstallQuarantineCardId
                        .SEPARATE_DELIVERY_INVITATION_FINE_AND_REFUND,
                displayOrder =
                    LinkAppInstallQuarantineCardId
                        .SEPARATE_DELIVERY_INVITATION_FINE_AND_REFUND.displayOrder,
                title = "택배·청첩장·부고·벌금·환급 안내는 별도 경로로 분리하기",
                linkAppPressureScenario =
                    "배송 변경·초대 확인·부고 열람·벌금 납부·환급 신청·기관 안내를 링크 안에서 끝내라고 하는 때",
                valueFreeActionSentence =
                    "상대가 준 링크·번호·프로필을 쓰지 않고 기존 별도 경로·기관 압박·회복 약속 화면에서 원칙만 읽겠습니다.",
                whyItMatters =
                    "그럴듯한 제목과 긴급한 사연은 링크·앱·기관·문서·결제 상태를 증명하지 않습니다.",
                doNotOpenInstallEnterScanGrantOrShareBoundaries = listOf(
                    "기관·사람·가족·매장·상표·공식 번호·문서·사건·신고·환급·회복 값을 적거나 공유하지 마세요.",
                    "공식 번호·발신자·전화번호·문서·사건·법률 상태를 조회·검증하지 않고 전화·문자·신고 제출·법률·금융 절차를 수행하지 않습니다."
                ),
                primaryAction = action(
                    LinkAppInstallQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                ),
                secondaryActions = listOf(
                    action(
                        LinkAppInstallQuarantineActionId
                            .OFFICIAL_PRESSURE_REALITY_CHECK_COACH
                    ),
                    action(
                        LinkAppInstallQuarantineActionId
                            .RECOVERY_PROMISE_REALITY_CHECK_COACH
                    ),
                    action(LinkAppInstallQuarantineActionId.OFFICIAL_HELP_DIRECTORY),
                    action(LinkAppInstallQuarantineActionId.REPORT_PREP)
                )
            ),
            LinkAppInstallQuarantineCard(
                id =
                    LinkAppInstallQuarantineCardId
                        .VALUE_FREE_EXPOSURE_EVIDENCE_HELP_AND_FOLLOW_UP,
                displayOrder =
                    LinkAppInstallQuarantineCardId
                        .VALUE_FREE_EXPOSURE_EVIDENCE_HELP_AND_FOLLOW_UP.displayOrder,
                title = "노출 가능성은 기존 계정·기기·증거·가족 경로로 값 없이 정리하기",
                linkAppPressureScenario =
                    "이미 링크나 앱 흐름을 접한 뒤 노출·증거·공식 도움·가족 검토·며칠 뒤 점검 순서를 살펴보려는 때",
                valueFreeActionSentence =
                    "링크·앱·사건·계정 값을 이 카드에 적지 않고 기존 노출·증거·도움·가족·후속 화면에서 행동 종류만 고르겠습니다.",
                whyItMatters =
                    "값 없는 라우팅은 민감한 원문과 식별값이 카드나 공유 문장에 반복 노출되는 일을 줄입니다.",
                doNotOpenInstallEnterScanGrantOrShareBoundaries = listOf(
                    "원문·가린 단서·사건 내용·미리보기·문서·파일·사진·화면·오디오·식별값·결과 상태를 적거나 공유하지 마세요.",
                    "증거를 수집·촬영·스캔·첨부·업로드·동기화·가림·삭제하지 않고 링크·앱·기기·계정·신고·회복 결과를 만들거나 저장하지 않습니다."
                ),
                primaryAction = action(
                    LinkAppInstallQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
                ),
                secondaryActions = listOf(
                    action(LinkAppInstallQuarantineActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(LinkAppInstallQuarantineActionId.EVIDENCE_KIT),
                    action(LinkAppInstallQuarantineActionId.INCIDENT_TIMELINE_BRIEFING),
                    action(LinkAppInstallQuarantineActionId.TRUSTED_HELPER_REVIEW),
                    action(LinkAppInstallQuarantineActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                    action(LinkAppInstallQuarantineActionId.SAFETY_FLOW_ROUTER)
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        readinessLevel: ReadinessLevel,
        hasNoAnalysisOrSignals: Boolean,
        hasHighRisk: Boolean,
        hasLinkSignal: Boolean,
        hasAppInstallOrLiveControlSignal: Boolean,
        hasCredentialSignal: Boolean,
        hasFinancialSignal: Boolean,
        hasRecoveryPromiseSignal: Boolean,
        hasOfficialPressureSignal: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (hasNoAnalysisOrSignals) {
                add(
                    "검사 결과나 일치 신호가 없어도 링크·URL·도메인·단축 주소·QR·" +
                        "바코드·APK·앱·패키지·권한·기기·로그인·계정·카드·은행·지갑·" +
                        "거래·결제·상품권·코드·발신자·연락처·문서·사건·신고·기관·" +
                        "가족 위급·도우미·회복 상태를 단정하지 말고 모든 링크·앱 설치 " +
                        "격리 카드를 보수적으로 읽도록 권합니다."
                )
            }
            if (hasHighRisk) {
                add(
                    "높은 위험 범주에서는 링크·앱·연락 세션·인증·돈·기관 압박·" +
                        "가족 확인·노출·증거·도움·후속 대응 경로를 값 없이 넓게 함께 보여 줍니다."
                )
            }
            if (hasLinkSignal) {
                add(
                    "링크·택배·초대·부고 문자 범주에서는 안전 붙여넣기 개인정보·" +
                        "별도 경로 확인·진행 중 연락 종료·원격지시 중단·노출 분류·" +
                        "증거 전달 개인정보·기관 압박·도움·신고 준비 화면을 직접 " +
                        "고르며 URL·도메인 검증, 브라우저·네트워크 동작, QR·바코드 " +
                        "스캔, 공식 번호 조회나 신고 제출을 하지 않습니다."
                )
            }
            if (hasAppInstallOrLiveControlSignal) {
                add(
                    "앱 설치·통화 유지 범주에서는 진행 중 연락 종료·원격지시 중단·" +
                        "계정 잠금·노출 분류·비밀값 멈춤·안전 붙여넣기 개인정보·보호자 " +
                        "검토·증거 전달 개인정보 화면을 고르며 설치 앱·패키지·권한 " +
                        "목록 조회, 설정 변경, 앱 설치·제거, 악성 프로그램 제거, 원격 " +
                        "연결 종료, 기기 정리나 기기 제어를 수행하지 않습니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증 비밀값 범주에서는 비밀값 멈춤·상품권 코드 멈춤·안전 " +
                        "붙여넣기 개인정보·계정 잠금·원격지시 중단·노출 분류 화면을 " +
                        "고르며 OTP·PIN·비밀번호·계정·카드·은행·지갑·상품권·링크·" +
                        "앱 값을 읽거나 표시하지 않습니다."
                )
            }
            if (hasFinancialSignal || hasRecoveryPromiseSignal) {
                add(
                    "송금·미끼·가상자산·안전계좌·대환 비용·결제·환급·회복을 " +
                        "떠올리게 하는 범주에서는 송금 멈춤·회복 약속·기관 압박·증거 " +
                        "전달 개인정보·보호자 검토·도움·신고 준비 화면을 고르며 결제 " +
                        "취소·환급·차지백·회복·지갑 추적이나 은행·결제 기능을 수행하지 않습니다."
                )
            }
            if (hasOfficialPressureSignal) {
                add(
                    "사칭·비밀 유지 압박 범주에서는 기관 압박·별도 경로 확인·진행 " +
                        "중 연락 종료·가족 알림·차분 대화·보호자 검토·증거 전달 " +
                        "개인정보·도움·신고 준비 화면을 고르며 공식 번호·디렉터리 값·" +
                        "사건·법률 상태를 사용하거나 전화·문자·신고 제출을 하지 않습니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 긴급·목소리 사칭·번호 변경 범주에서는 별도 경로 확인·가족 " +
                        "콜백·목소리 현실 점검·새 번호 격리·안전단어 재정비·값 없는 " +
                        "전화 문구·가족 알림 화면을 고르며 연락처·안전단어·콜백 값을 " +
                        "사용하거나 연락처·통화 기록·문자를 읽지 않습니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만으로 증거 전달 개인정보·증거 " +
                        "체크·사건 흐름·후속 점검 화면을 더 보여 주며 사건 내용이나 " +
                        "미리보기는 읽지 않습니다."
                )
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 아직 진행 중인 범주라 안전 계획·안전단어 재정비·준비 " +
                    "로드맵·가족 연습·값 없는 전화 문구 화면을 직접 살펴보도록 " +
                    "권합니다. 저장된 계획의 실제 값은 추천에 사용하지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있다는 범주는 기존 준비 항목이 채워졌다는 뜻일 뿐 " +
                    "링크·URL·도메인·QR·바코드·APK·앱·패키지·권한·기기·로그인·" +
                    "계정·결제·상품권 요청이 실제이거나 안전하거나 신뢰할 수 있다는 " +
                    "뜻이 아닙니다. 준비 상태와 관계없이 열기·설치·허용·입력 행동을 멈추세요."
        }
        return listOf(situationCopy, readinessCopy).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 고정 문장을 직접 읽고 기존 앱 화면을 직접 고르는 로컬 전용·무상태 수동 안내이며 링크·URL·도메인·QR·바코드·APK·앱·패키지·권한·기기·로그인·계정·결제·상품권·사건·사람 값을 입력하라고 요구하지 않습니다.",
        "추천은 현재 검사 결과의 위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기·가족 안전 계획의 실제 값을 사용하지 않습니다.",
        "링크·URL·도메인·단축 주소를 열기·미리 보기·가져오기·따라가기·해결·정규화·단축·확장·평판 조회·검증·분류·스캔·샌드박스 실행하지 않습니다.",
        "네트워크·웹 탐색·브라우저·웹뷰·지도·위치·지오코딩을 사용하지 않고 앱스토어·기관 정보·공식 번호·디렉터리·발신자 표시·전화번호·문서·사건·법률 상태를 조회·검증하지 않습니다.",
        "QR·바코드·OCR·카메라·이미지 분석·화면 캡처를 사용하지 않고 파일·문서·사진·스크린샷·오디오·미디어·저장소를 읽거나 선택·촬영·녹음·전사·첨부·스트림·URI 공유·업로드·동기화하지 않습니다.",
        "APK·앱·패키지·설치 앱 목록·앱 이름·권한·기기 상태를 검사하거나 읽지 않고 설치·제거·비활성화·강제 종료·권한 허용·철회·설정 변경·앱 작업·악성 프로그램 검사·제거·기기 정리·원격 연결 종료를 수행하지 않습니다.",
        "접근성 서비스·기기 관리자·VPN·사용 통계·알림 수신 서비스·권한 관리자·패키지 설치 도구·기기 관리 기능을 사용하지 않습니다.",
        "전화·다이얼·발신·통화·종료·선별·차단·통화 상태·통화 기록·문자·SMS·받는 사람 선택·자동 전송을 사용하지 않습니다.",
        "연락처·주소록·통화 기록·문자·알림을 읽거나 감시하지 않고 연락처 읽기·쓰기·편집·선택과 알림 수신·필터링을 하지 않습니다.",
        "클립보드·텍스트 감시기·자동완성·자격 증명·계정·인증·생체 인증·키가드·키 저장소·암호화 API를 사용하지 않고 로그인·계정·인증 상태를 읽거나 바꾸지 않습니다.",
        "OTP·PIN·비밀번호·보안 답·주민 식별값·안전단어·콜백 문장·연락처 이름·번호·계정·카드·은행·지갑·거래·받는 사람·상품권·코드 값을 읽기·표시·저장·복사·붙여넣기·자동 입력·공유·제출하지 않습니다.",
        "결제·은행·카드·NFC·지갑·블록체인·가상자산·상품권 기능 또는 API를 사용하지 않고 송금·결제·구매·잔액 조회·사용·동결·취소·환급·차지백·추적·회복을 수행·조회·제출·보장하지 않습니다.",
        "기관·사람·가족·매장·상표·도우미·변호사·수사관·발신자·번호·프로필·문서·사건·신고·공식 경로·법률·금융 결과를 확인·판정·증명하지 않고 공식 조회·전화·문자·신고 제출·영수증 확인·법률 자문을 하지 않습니다.",
        "수동 문자 공유는 고정 링크·앱 격리 카드와 기존 앱 화면·행동 종류만 담고 원문·가린 단서·사건·계획·기관 목록·연락처·채널·프로필·계정·결제·지갑·상품권·링크·앱·패키지·권한·기기·파일·받는 사람·확인·제출·회복 결과를 담거나 자동 발송하지 않습니다.",
        "새 Android 권한을 요청하지 않고 알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러·브로드캐스트 수신기를 만들거나 예약하지 않습니다.",
        "새 저장소를 만들지 않고 카드 선택·추천·읽음·공유·체크·메모·담당자·받는 사람·링크·앱·패키지·권한·설치·제거·정리·차단·계정·결제·상품권·증거·확인·신고·회복·진행·상태·이력을 저장하지 않으며 가족 계획·사건·디렉터리·연락처·알림·통화·문자·계정·결제·기기 상태를 수정하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<LinkAppInstallQuarantineCardId>,
        recommendedActions: List<LinkAppInstallQuarantineAction>,
        cards: List<LinkAppInstallQuarantineCard>
    ): String {
        val cardById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[링크·앱 설치 격리 코치]")
            appendLine("링크 열기와 앱 설치·권한 허용·같은 세션 입력을 멈추는 로컬 수동 점검입니다.")
            appendLine()
            appendLine("추천 링크·앱 격리 카드")
            recommendedCardIds.forEach { cardId ->
                val card = cardById.getValue(cardId)
                appendLine("- ${card.title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 실제 링크·주소·QR·바코드·앱·패키지·권한·기기·로그인·계정·결제·상품권·연락처·사건 값은 보내지 않습니다.")
            append("- 이 안내는 열기·검사·스캔·설치·제거·권한 변경·기기 정리·결제·환급·회복·신고를 수행하거나 보장하지 않습니다.")
        }
    }

    private fun actionFor(
        id: LinkAppInstallQuarantineActionId
    ): LinkAppInstallQuarantineAction =
        LinkAppInstallQuarantineAction(id = id, label = actionLabels.getValue(id))

    private val actionLabels = mapOf(
        LinkAppInstallQuarantineActionId.QUICK_STOP to "긴급 멈춤 모드",
        LinkAppInstallQuarantineActionId.CHECKER to "사기 신호 검사 시작",
        LinkAppInstallQuarantineActionId.ANALYSIS_RESULT to "현재 분석 결과",
        LinkAppInstallQuarantineActionId.SAFETY_PLAN to "가족 안전 계획",
        LinkAppInstallQuarantineActionId.FAMILY_READINESS_ROADMAP to "가족 준비 로드맵",
        LinkAppInstallQuarantineActionId.FAMILY_SAFETY_DRILL_COACH to "가족 안전 연습 코치",
        LinkAppInstallQuarantineActionId.SAFE_PASTE_PRIVACY_COACH to
            "안전 붙여넣기 개인정보 코치",
        LinkAppInstallQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS to
            "진행 중 연락 종료 문구",
        LinkAppInstallQuarantineActionId.FAMILY_CALLBACK_CARD to "가족 콜백 카드",
        LinkAppInstallQuarantineActionId.FAMILY_SAFE_WORD_RESET_COACH to
            "가족 안전단어 재정비 코치",
        LinkAppInstallQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH to
            "별도 경로 확인 코치",
        LinkAppInstallQuarantineActionId.SAFE_CALL_SCRIPTS to "안전 확인 전화 문구",
        LinkAppInstallQuarantineActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH to
            "목소리 사칭 현실 점검",
        LinkAppInstallQuarantineActionId.NEW_NUMBER_QUARANTINE_COACH to
            "새 번호 격리 코치",
        LinkAppInstallQuarantineActionId.CREDENTIAL_SECRET_FREEZE_COACH to
            "인증 비밀값 멈춤 코치",
        LinkAppInstallQuarantineActionId.FAMILY_ALERT_MESSAGES to "가족 알림 문구",
        LinkAppInstallQuarantineActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD to
            "확인 결과 다음 단계 보드",
        LinkAppInstallQuarantineActionId.RECONTACT_BOUNDARY_COACH to "재접촉 경계 코치",
        LinkAppInstallQuarantineActionId.TRUSTED_HELPER_REVIEW to "신뢰 보호자 검토",
        LinkAppInstallQuarantineActionId.FAMILY_RESPONSE_ROLE_BOARD to "가족 대응 역할 보드",
        LinkAppInstallQuarantineActionId.FAMILY_CALM_DEBRIEF_AGENDA to
            "가족 차분 대화 순서",
        LinkAppInstallQuarantineActionId.MONEY_TRANSFER_PAUSE_GATE to "송금 전 멈춤 게이트",
        LinkAppInstallQuarantineActionId.GIFT_CARD_CODE_PAUSE_COACH to
            "상품권·코드 멈춤 코치",
        LinkAppInstallQuarantineActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH to
            "기관 사칭 압박 현실 점검",
        LinkAppInstallQuarantineActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH to
            "회복 약속 현실 점검",
        LinkAppInstallQuarantineActionId.ACCOUNT_LOCKDOWN to "계정·기기 잠금 체크리스트",
        LinkAppInstallQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH to "원격지시 중단 코치",
        LinkAppInstallQuarantineActionId.EXPOSURE_IMPACT_TRIAGE to "노출 영향 분류",
        LinkAppInstallQuarantineActionId.PRIVACY_CENTER to "개인정보 센터",
        LinkAppInstallQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH to
            "증거 전달 개인정보 코치",
        LinkAppInstallQuarantineActionId.EVIDENCE_KIT to "증거 체크리스트",
        LinkAppInstallQuarantineActionId.INCIDENT_TIMELINE_BRIEFING to "사건 흐름 브리핑",
        LinkAppInstallQuarantineActionId.OFFICIAL_HELP_DIRECTORY to "공식 도움 경로",
        LinkAppInstallQuarantineActionId.REPORT_PREP to "신고 준비 카드",
        LinkAppInstallQuarantineActionId.FOLLOW_UP_CHECK_IN_PLANNER to "후속 점검 플래너",
        LinkAppInstallQuarantineActionId.SAFETY_FLOW_ROUTER to "안전 대응 길잡이"
    )
}
