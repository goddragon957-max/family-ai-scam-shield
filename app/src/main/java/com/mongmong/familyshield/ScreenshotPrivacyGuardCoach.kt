package com.mongmong.familyshield

enum class ScreenshotPrivacyGuardCardId(val displayOrder: Int) {
    BEFORE_SCREENSHOT_REDACT_REMINDER(1),
    AFTER_SCREENSHOT_CLEANUP_REMINDER(2),
    GALLERY_CLOUD_BACKUP_SYNC_AWARENESS(3),
    PREFER_APP_SHARE_BUTTON_OVER_RAW_SCREENSHOT(4),
    FAMILY_SHARING_CHANNEL_BOUNDARY_REMINDER(5)
}

enum class ScreenshotPrivacyGuardActionId {
    QUICK_STOP,
    CHECKER,
    ANALYSIS_RESULT,
    SAFETY_PLAN,
    FAMILY_READINESS_ROADMAP,
    FAMILY_SAFETY_DRILL_COACH,
    SAFE_PASTE_PRIVACY_COACH,
    CREDENTIAL_SECRET_FREEZE_COACH,
    ACCOUNT_LOCKDOWN,
    EXPOSURE_IMPACT_TRIAGE,
    EVIDENCE_HANDOFF_PRIVACY_COACH,
    EVIDENCE_KIT,
    INCIDENT_TIMELINE_BRIEFING,
    ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH,
    FAMILY_ALERT_MESSAGES,
    FAMILY_CALLBACK_CARD,
    TRUSTED_HELPER_REVIEW,
    SAFE_CALL_SCRIPTS,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    FOLLOW_UP_CHECK_IN_PLANNER,
    PRIVACY_CENTER,
    SAFETY_FLOW_ROUTER
}

data class ScreenshotPrivacyGuardAction(
    val id: ScreenshotPrivacyGuardActionId,
    val label: String
)

data class ScreenshotPrivacyGuardCard(
    val id: ScreenshotPrivacyGuardCardId,
    val displayOrder: Int,
    val title: String,
    val screenshotPressureSituation: String,
    val valueFreePauseScript: String,
    val whyItMatters: String,
    val doNotCaptureDetectStoreUploadOrAutoShareBoundaries: List<String>,
    val primaryAction: ScreenshotPrivacyGuardAction,
    val secondaryActions: List<ScreenshotPrivacyGuardAction>
) {
    val routeActions: List<ScreenshotPrivacyGuardAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class ScreenshotPrivacyGuardCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<ScreenshotPrivacyGuardCardId>,
    val recommendedActions: List<ScreenshotPrivacyGuardAction>,
    val cards: List<ScreenshotPrivacyGuardCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<ScreenshotPrivacyGuardCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<ScreenshotPrivacyGuardActionId>
        get() = recommendedActions.map { action -> action.id }
}

object ScreenshotPrivacyGuardCoachBuilder {
    private val credentialSignals = setOf(
        ScamSignal.CREDENTIALS,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val familyVerificationSignals = setOf(
        ScamSignal.PHONE_NUMBER_CHANGE,
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE
    )

    private val officialPressureSignals = setOf(
        ScamSignal.IMPERSONATION,
        ScamSignal.SECRECY,
        ScamSignal.SAFE_ACCOUNT_FRAUD
    )

    private val liveLinkAppSignals = setOf(
        ScamSignal.LIVE_CALL_CONTROL,
        ScamSignal.APP_INSTALL,
        ScamSignal.LINK,
        ScamSignal.PARCEL_INVITATION_SMISHING
    )

    private val financialRecoverySignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.BAIT,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.OVERSEAS_PAYMENT_ALERT,
        ScamSignal.GIFTICON_PIN
    )

    fun build(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness,
        incidentCount: Int = 0
    ): ScreenshotPrivacyGuardCoach {
        val riskLevel = analysis?.riskLevel
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val readinessLevel = readiness.level
        val hasNoAnalysisOrSignals = analysis == null || signals.isEmpty()
        val hasHighRisk = riskLevel == RiskLevel.HIGH
        val hasCredentialSignal = signals.any { signal -> signal in credentialSignals }
        val hasFamilyVerificationSignal = signals.any { signal ->
            signal in familyVerificationSignals
        }
        val hasOfficialPressureSignal = signals.any { signal ->
            signal in officialPressureSignals
        }
        val hasLiveLinkAppSignal = signals.any { signal -> signal in liveLinkAppSignals }
        val hasFinancialRecoverySignal = signals.any { signal ->
            signal in financialRecoverySignals
        }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0

        val recommendedCardSet = linkedSetOf(
            ScreenshotPrivacyGuardCardId.BEFORE_SCREENSHOT_REDACT_REMINDER,
            ScreenshotPrivacyGuardCardId.PREFER_APP_SHARE_BUTTON_OVER_RAW_SCREENSHOT
        )
        if (hasNoAnalysisOrSignals || hasHighRisk) {
            recommendedCardSet += ScreenshotPrivacyGuardCardId.entries
        }
        if (hasCredentialSignal) {
            recommendedCardSet += ScreenshotPrivacyGuardCardId.BEFORE_SCREENSHOT_REDACT_REMINDER
            recommendedCardSet +=
                ScreenshotPrivacyGuardCardId.PREFER_APP_SHARE_BUTTON_OVER_RAW_SCREENSHOT
            recommendedCardSet += ScreenshotPrivacyGuardCardId.AFTER_SCREENSHOT_CLEANUP_REMINDER
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet +=
                ScreenshotPrivacyGuardCardId.FAMILY_SHARING_CHANNEL_BOUNDARY_REMINDER
            recommendedCardSet +=
                ScreenshotPrivacyGuardCardId.PREFER_APP_SHARE_BUTTON_OVER_RAW_SCREENSHOT
        }
        if (hasOfficialPressureSignal) {
            recommendedCardSet +=
                ScreenshotPrivacyGuardCardId.PREFER_APP_SHARE_BUTTON_OVER_RAW_SCREENSHOT
            recommendedCardSet += ScreenshotPrivacyGuardCardId.AFTER_SCREENSHOT_CLEANUP_REMINDER
            recommendedCardSet +=
                ScreenshotPrivacyGuardCardId.FAMILY_SHARING_CHANNEL_BOUNDARY_REMINDER
        }
        if (hasLiveLinkAppSignal) {
            recommendedCardSet += ScreenshotPrivacyGuardCardId.GALLERY_CLOUD_BACKUP_SYNC_AWARENESS
            recommendedCardSet += ScreenshotPrivacyGuardCardId.AFTER_SCREENSHOT_CLEANUP_REMINDER
        }
        if (hasFinancialRecoverySignal) {
            recommendedCardSet += ScreenshotPrivacyGuardCardId.BEFORE_SCREENSHOT_REDACT_REMINDER
            recommendedCardSet += ScreenshotPrivacyGuardCardId.AFTER_SCREENSHOT_CLEANUP_REMINDER
            recommendedCardSet += ScreenshotPrivacyGuardCardId.GALLERY_CLOUD_BACKUP_SYNC_AWARENESS
        }
        if (hasSavedIncident) {
            recommendedCardSet += ScreenshotPrivacyGuardCardId.AFTER_SCREENSHOT_CLEANUP_REMINDER
            recommendedCardSet += ScreenshotPrivacyGuardCardId.GALLERY_CLOUD_BACKUP_SYNC_AWARENESS
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedCardSet +=
                    ScreenshotPrivacyGuardCardId.FAMILY_SHARING_CHANNEL_BOUNDARY_REMINDER
            }
            ReadinessLevel.READY -> {
                recommendedCardSet += ScreenshotPrivacyGuardCardId.BEFORE_SCREENSHOT_REDACT_REMINDER
                recommendedCardSet +=
                    ScreenshotPrivacyGuardCardId.PREFER_APP_SHARE_BUTTON_OVER_RAW_SCREENSHOT
            }
        }
        val recommendedCardIds = ScreenshotPrivacyGuardCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val currentResultActionId = if (analysis == null) {
            ScreenshotPrivacyGuardActionId.CHECKER
        } else {
            ScreenshotPrivacyGuardActionId.ANALYSIS_RESULT
        }
        val recommendedActionSet = linkedSetOf(
            currentResultActionId,
            ScreenshotPrivacyGuardActionId.QUICK_STOP,
            ScreenshotPrivacyGuardActionId.SAFE_PASTE_PRIVACY_COACH,
            ScreenshotPrivacyGuardActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
            ScreenshotPrivacyGuardActionId.PRIVACY_CENTER,
            ScreenshotPrivacyGuardActionId.EXPOSURE_IMPACT_TRIAGE,
            ScreenshotPrivacyGuardActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            ScreenshotPrivacyGuardActionId.entries
                .filterNot { actionId ->
                    actionId == ScreenshotPrivacyGuardActionId.ANALYSIS_RESULT
                }
                .forEach { actionId -> recommendedActionSet += actionId }
        }
        if (hasHighRisk) {
            ScreenshotPrivacyGuardActionId.entries.forEach { actionId ->
                recommendedActionSet += actionId
            }
        }
        if (hasCredentialSignal) {
            recommendedActionSet += ScreenshotPrivacyGuardActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet += ScreenshotPrivacyGuardActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet +=
                ScreenshotPrivacyGuardActionId.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += ScreenshotPrivacyGuardActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += ScreenshotPrivacyGuardActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += ScreenshotPrivacyGuardActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += ScreenshotPrivacyGuardActionId.SAFE_CALL_SCRIPTS
        }
        if (hasOfficialPressureSignal) {
            recommendedActionSet += ScreenshotPrivacyGuardActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += ScreenshotPrivacyGuardActionId.REPORT_PREP
            recommendedActionSet += ScreenshotPrivacyGuardActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += ScreenshotPrivacyGuardActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += ScreenshotPrivacyGuardActionId.ACCOUNT_LOCKDOWN
        }
        if (hasLiveLinkAppSignal) {
            recommendedActionSet += ScreenshotPrivacyGuardActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += ScreenshotPrivacyGuardActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += ScreenshotPrivacyGuardActionId.PRIVACY_CENTER
        }
        if (hasFinancialRecoverySignal) {
            recommendedActionSet += ScreenshotPrivacyGuardActionId.CREDENTIAL_SECRET_FREEZE_COACH
            recommendedActionSet += ScreenshotPrivacyGuardActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += ScreenshotPrivacyGuardActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += ScreenshotPrivacyGuardActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += ScreenshotPrivacyGuardActionId.REPORT_PREP
        }
        if (hasSavedIncident) {
            recommendedActionSet += ScreenshotPrivacyGuardActionId.EVIDENCE_KIT
            recommendedActionSet += ScreenshotPrivacyGuardActionId.INCIDENT_TIMELINE_BRIEFING
            recommendedActionSet += ScreenshotPrivacyGuardActionId.FOLLOW_UP_CHECK_IN_PLANNER
        }
        when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS -> {
                recommendedActionSet += ScreenshotPrivacyGuardActionId.SAFETY_PLAN
                recommendedActionSet += ScreenshotPrivacyGuardActionId.FAMILY_READINESS_ROADMAP
                recommendedActionSet += ScreenshotPrivacyGuardActionId.FAMILY_SAFETY_DRILL_COACH
            }
            ReadinessLevel.READY -> {
                recommendedActionSet += ScreenshotPrivacyGuardActionId.FAMILY_CALLBACK_CARD
                recommendedActionSet += ScreenshotPrivacyGuardActionId.SAFE_CALL_SCRIPTS
            }
        }
        val recommendedActions = ScreenshotPrivacyGuardActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards()
        val recommendationTitle = when {
            analysis == null ->
                "검사 결과가 없어도 모든 화면캡처 개인정보 안내 문구를 보수적으로 읽기"
            hasCredentialSignal -> "인증·상품권·결제 값이 보이는 화면을 캡처하기 전에 가리기"
            hasFamilyVerificationSignal ->
                "가족·새 번호·목소리 사칭 화면을 캡처해 나눌 때 안전한 경로 고르기"
            hasOfficialPressureSignal ->
                "기관 사칭·압박 화면을 캡처해 신고 준비할 때 정리 방법 확인"
            hasLiveLinkAppSignal ->
                "통화·링크·앱 관련 화면을 캡처하기 전 갤러리·클라우드 동기화 인지"
            hasFinancialRecoverySignal ->
                "송금·회복 약속 화면을 캡처하기 전후 가리기·정리를 함께 챙기기"
            hasHighRisk -> "높은 위험 범주에서 모든 화면캡처 개인정보 카드와 기존 경로 보기"
            hasSavedIncident -> "저장된 사건이 있다는 개수 범주만으로 정리·공유 경로 고르기"
            else -> "이 화면을 캡처하기 전이나 후에 짧게 개인정보 안내를 읽기"
        }

        return ScreenshotPrivacyGuardCoach(
            title = "스크린샷 개인정보 가드 코치",
            intro =
                "사건 분석 결과, 위험 신호 설명, 증거 보존 체크리스트 같은 화면을 캡처해 " +
                    "가족에게 보내기 전이나 보낸 뒤에, 실제 화면캡처를 감지하거나 막지 않고 " +
                    "고정된 문장만 직접 읽는 로컬 전용·무상태 수동 안내입니다.",
            modeLabel = "화면캡처 감지 없음 · 캡처 차단 없음 · 갤러리/파일 읽기 없음",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                readinessLevel = readinessLevel,
                hasNoAnalysisOrSignals = hasNoAnalysisOrSignals,
                hasHighRisk = hasHighRisk,
                hasCredentialSignal = hasCredentialSignal,
                hasFamilyVerificationSignal = hasFamilyVerificationSignal,
                hasOfficialPressureSignal = hasOfficialPressureSignal,
                hasLiveLinkAppSignal = hasLiveLinkAppSignal,
                hasFinancialRecoverySignal = hasFinancialRecoverySignal,
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

    private fun buildCards(): List<ScreenshotPrivacyGuardCard> {
        fun action(id: ScreenshotPrivacyGuardActionId): ScreenshotPrivacyGuardAction =
            actionFor(id)

        return listOf(
            ScreenshotPrivacyGuardCard(
                id = ScreenshotPrivacyGuardCardId.BEFORE_SCREENSHOT_REDACT_REMINDER,
                displayOrder =
                    ScreenshotPrivacyGuardCardId.BEFORE_SCREENSHOT_REDACT_REMINDER.displayOrder,
                title = "캡처하기 전에 화면에 보이는 값 다시 확인하기",
                screenshotPressureSituation =
                    "분석 결과·위험 신호 설명·증거 체크리스트 화면을 가족에게 보여 주려고 지금 화면을 캡처하려는 때",
                valueFreePauseScript =
                    "캡처하기 전에 화면에 계좌·전화번호·신분 값이나 원문이 그대로 보이는지 먼저 확인하겠습니다.",
                whyItMatters =
                    "이 앱은 사건 기록에 원문 대신 가린 미리보기만 남기지만, 캡처한 사진에는 화면에 보이는 값이 그대로 담깁니다.",
                doNotCaptureDetectStoreUploadOrAutoShareBoundaries = listOf(
                    "이 안내는 실제 화면캡처를 만들거나 막지 않고 캡처 여부를 감지·기록하지 않습니다.",
                    "이 안내는 갤러리·파일·미디어스토어를 읽지 않고 캡처된 이미지의 값을 확인·판정하지 않습니다."
                ),
                primaryAction = action(ScreenshotPrivacyGuardActionId.SAFE_PASTE_PRIVACY_COACH),
                secondaryActions = listOf(
                    action(ScreenshotPrivacyGuardActionId.CREDENTIAL_SECRET_FREEZE_COACH),
                    action(
                        ScreenshotPrivacyGuardActionId.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH
                    ),
                    action(ScreenshotPrivacyGuardActionId.EVIDENCE_HANDOFF_PRIVACY_COACH),
                    action(ScreenshotPrivacyGuardActionId.PRIVACY_CENTER)
                )
            ),
            ScreenshotPrivacyGuardCard(
                id = ScreenshotPrivacyGuardCardId.AFTER_SCREENSHOT_CLEANUP_REMINDER,
                displayOrder =
                    ScreenshotPrivacyGuardCardId.AFTER_SCREENSHOT_CLEANUP_REMINDER.displayOrder,
                title = "캡처한 화면을 보낸 뒤 직접 정리하기",
                screenshotPressureSituation =
                    "이미 화면을 캡처해 가족에게 보냈거나 보내고 나서 캡처본을 그대로 갤러리에 남겨 두려는 때",
                valueFreePauseScript =
                    "필요한 곳에 전달했다면 갤러리에 남은 캡처본을 직접 확인하고 필요 없는 사본은 직접 지우겠습니다.",
                whyItMatters =
                    "캡처본을 지우지 않고 두면 원래 의도한 곳 밖에서도 계속 남아 다시 열리거나 옮겨질 수 있습니다.",
                doNotCaptureDetectStoreUploadOrAutoShareBoundaries = listOf(
                    "이 안내는 갤러리·사진·파일을 자동으로 찾아 삭제·이동·정리하지 않고 사용자가 직접 여는 갤러리 앱에서만 지웁니다.",
                    "이 안내는 캡처본이 실제로 지워졌는지 확인·기록하지 않습니다."
                ),
                primaryAction = action(ScreenshotPrivacyGuardActionId.EVIDENCE_KIT),
                secondaryActions = listOf(
                    action(ScreenshotPrivacyGuardActionId.ACCOUNT_LOCKDOWN),
                    action(ScreenshotPrivacyGuardActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(ScreenshotPrivacyGuardActionId.PRIVACY_CENTER)
                )
            ),
            ScreenshotPrivacyGuardCard(
                id = ScreenshotPrivacyGuardCardId.GALLERY_CLOUD_BACKUP_SYNC_AWARENESS,
                displayOrder =
                    ScreenshotPrivacyGuardCardId.GALLERY_CLOUD_BACKUP_SYNC_AWARENESS.displayOrder,
                title = "캡처본이 클라우드 사진 백업으로도 이동할 수 있음을 기억하기",
                screenshotPressureSituation =
                    "기기의 사진 자동 백업이 켜져 있는 상태에서 민감한 화면을 캡처하려는 때",
                valueFreePauseScript =
                    "이 캡처본이 이 앱 밖의 클라우드 사진 백업이나 다른 기기로도 동기화될 수 있음을 기억하겠습니다.",
                whyItMatters =
                    "클라우드 사진 백업은 이 앱이 켜거나 끄지 않는 별도 설정이라 캡처본이 가족이 모르는 곳까지 남을 수 있습니다.",
                doNotCaptureDetectStoreUploadOrAutoShareBoundaries = listOf(
                    "이 안내는 기기의 클라우드 백업·동기화 설정을 읽거나 켜고 끄지 않습니다.",
                    "이 안내는 다른 앱의 갤러리·클라우드 저장 상태를 조회·판정하지 않습니다."
                ),
                primaryAction = action(ScreenshotPrivacyGuardActionId.EXPOSURE_IMPACT_TRIAGE),
                secondaryActions = listOf(
                    action(ScreenshotPrivacyGuardActionId.ACCOUNT_LOCKDOWN),
                    action(ScreenshotPrivacyGuardActionId.EVIDENCE_HANDOFF_PRIVACY_COACH),
                    action(ScreenshotPrivacyGuardActionId.PRIVACY_CENTER)
                )
            ),
            ScreenshotPrivacyGuardCard(
                id = ScreenshotPrivacyGuardCardId.PREFER_APP_SHARE_BUTTON_OVER_RAW_SCREENSHOT,
                displayOrder =
                    ScreenshotPrivacyGuardCardId
                        .PREFER_APP_SHARE_BUTTON_OVER_RAW_SCREENSHOT.displayOrder,
                title = "캡처본 대신 이 앱의 고정 문구 공유 버튼을 먼저 고려하기",
                screenshotPressureSituation =
                    "가족에게 상황을 알리려고 화면을 캡처해 보내려는 때",
                valueFreePauseScript =
                    "가능하면 캡처본 대신 이 화면에 있는 가족 공유 버튼으로 고정된 값 없는 문구를 먼저 보내겠습니다.",
                whyItMatters =
                    "이 앱의 공유 버튼은 값 없는 고정 요약 텍스트만 전달하지만 캡처본은 화면에 보이는 모든 내용을 그대로 옮깁니다.",
                doNotCaptureDetectStoreUploadOrAutoShareBoundaries = listOf(
                    "이 안내는 캡처본을 대신 만들거나 자동으로 전송하지 않고 값 없는 고정 텍스트 공유만 안내합니다.",
                    "이 안내는 받는 사람을 자동으로 고르거나 캡처본·파일을 첨부하지 않습니다."
                ),
                primaryAction = action(ScreenshotPrivacyGuardActionId.EVIDENCE_HANDOFF_PRIVACY_COACH),
                secondaryActions = listOf(
                    action(ScreenshotPrivacyGuardActionId.SAFE_PASTE_PRIVACY_COACH),
                    action(ScreenshotPrivacyGuardActionId.INCIDENT_TIMELINE_BRIEFING),
                    action(ScreenshotPrivacyGuardActionId.FAMILY_ALERT_MESSAGES)
                )
            ),
            ScreenshotPrivacyGuardCard(
                id = ScreenshotPrivacyGuardCardId.FAMILY_SHARING_CHANNEL_BOUNDARY_REMINDER,
                displayOrder =
                    ScreenshotPrivacyGuardCardId
                        .FAMILY_SHARING_CHANNEL_BOUNDARY_REMINDER.displayOrder,
                title = "캡처본을 나눌 가족 채널의 경계를 정하기",
                screenshotPressureSituation =
                    "캡처본을 어느 가족 대화방이나 사람에게 보낼지 정하지 못한 때",
                valueFreePauseScript =
                    "미리 정한 가족·보호자에게만 전달하고, 다른 단체 대화방이나 낯선 채널로는 옮기지 않겠습니다.",
                whyItMatters =
                    "정한 사람 밖으로 캡처본이 퍼지면 원래 의도한 도움 요청 목적을 벗어나 값이 계속 남을 수 있습니다.",
                doNotCaptureDetectStoreUploadOrAutoShareBoundaries = listOf(
                    "이 안내는 대화방·채널·받는 사람을 대신 고르거나 자동으로 전달·전송하지 않습니다.",
                    "이 안내는 가족·보호자 이름이나 연락처 값을 입력받거나 표시·저장하지 않습니다."
                ),
                primaryAction = action(ScreenshotPrivacyGuardActionId.FAMILY_ALERT_MESSAGES),
                secondaryActions = listOf(
                    action(ScreenshotPrivacyGuardActionId.TRUSTED_HELPER_REVIEW),
                    action(ScreenshotPrivacyGuardActionId.FAMILY_CALLBACK_CARD),
                    action(ScreenshotPrivacyGuardActionId.SAFETY_FLOW_ROUTER)
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        readinessLevel: ReadinessLevel,
        hasNoAnalysisOrSignals: Boolean,
        hasHighRisk: Boolean,
        hasCredentialSignal: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasOfficialPressureSignal: Boolean,
        hasLiveLinkAppSignal: Boolean,
        hasFinancialRecoverySignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (hasNoAnalysisOrSignals) {
                add(
                    "검사 결과나 일치 신호가 없어도 화면캡처·갤러리·클라우드 백업·공유 상태를 " +
                        "안전·차단·감지·완료라고 단정하지 않고 모든 값 없는 화면캡처 개인정보 " +
                        "카드와 기존 대응 경로를 보수적으로 보여 줍니다."
                )
            }
            if (hasHighRisk) {
                add(
                    "높은 위험 범주에서는 캡처 전 가리기, 캡처 후 정리, 클라우드 동기화 인지, " +
                        "앱 공유 버튼 우선, 가족 채널 경계 카드를 값 없이 넓게 보여 줍니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증·상품권·결제 범주에서는 캡처 전 가리기, 앱 공유 버튼 우선, 캡처 후 정리 " +
                        "카드를 고르며 OTP·PIN·비밀번호·계좌·카드·상품권 값을 사용하지 않습니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "번호 변경·가족 긴급·목소리 사칭 범주에서는 가족 채널 경계, 앱 공유 버튼 우선 " +
                        "카드를 고르며 전화번호·연락처·안전단어·콜백 값이나 통화·문자를 사용하지 않습니다."
                )
            }
            if (hasOfficialPressureSignal) {
                add(
                    "사칭·비밀 유지·안전계좌 범주에서는 앱 공유 버튼 우선, 캡처 후 정리, 가족 채널 " +
                        "경계 카드를 고르며 기관·직원 신원·공식 번호나 사건 접수 상태를 조회·검증하지 않습니다."
                )
            }
            if (hasLiveLinkAppSignal) {
                add(
                    "통화 유지·앱·링크·택배 문자 범주에서는 클라우드 동기화 인지, 캡처 후 정리 " +
                        "카드를 고르며 앱·패키지·기기·설정·클라우드 백업 상태를 읽거나 바꾸지 않습니다."
                )
            }
            if (hasFinancialRecoverySignal) {
                add(
                    "돈·회복 약속 범주에서는 캡처 전 가리기, 캡처 후 정리, 클라우드 동기화 인지 " +
                        "카드를 고르며 결제·은행·환급·차지백·회복 행동을 수행하지 않습니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만으로 캡처 후 정리, 클라우드 동기화 인지 카드를 " +
                        "더 보여 주며 사건 내용이나 미리보기를 읽지 않습니다."
                )
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW,
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 아직 진행 중인 범주라 가족 채널 경계 카드를 먼저 살펴보도록 " +
                    "권합니다. 저장된 계획의 실제 값은 추천에 사용하지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있다는 범주는 화면캡처·갤러리·클라우드 백업·공유 상태가 " +
                    "안전하거나 완료됐다는 뜻이 아닙니다. 준비 상태와 관계없이 캡처 전 가리기와 " +
                    "앱 공유 버튼 우선을 계속 확인하세요."
        }
        return listOf(situationCopy, readinessCopy)
            .filter { copy -> copy.isNotBlank() }
            .joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 고정 문장을 사용자가 직접 읽고 기존 앱 화면을 직접 고르는 로컬 전용·무상태 수동 안내이며 화면캡처를 감지·차단·기록하지 않고 새 저장소·자동 처리·자동 전송·스케줄러를 만들지 않습니다.",
        "추천은 현재 검사 결과의 위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기·가족 계획의 실제 값을 사용하지 않습니다.",
        "이 앱은 WindowManager.LayoutParams.FLAG_SECURE를 사용하지 않으므로 이 앱의 어떤 화면도 캡처를 강제로 막지 않습니다.",
        "이 앱은 미디어스토어 콘텐츠 감시자·접근성 서비스·기타 방법으로 실제 화면캡처 발생을 감지하지 않고 감지했다고 표시하지도 않습니다.",
        "캡처된 이미지는 이 앱이 아니라 기기의 갤러리·카메라 롤에 보관되며, 기기의 클라우드 사진 백업 설정에 따라 이 앱이 통제하지 않는 곳으로도 동기화될 수 있습니다.",
        "갤러리·카메라·CameraX·Camera2·미디어스토어·콘텐츠 감시자·OCR·이미지 분석·파일 선택기·클립보드를 사용하지 않고 캡처된 이미지의 값을 읽거나 표시·저장·분석·삭제하지 않습니다.",
        "카메라·마이크·MediaRecorder·AudioRecord·QR·바코드 스캔·얼굴 인식·생체 인증을 사용하지 않습니다.",
        "파일·문서·사진·영상·미디어·갤러리·저장소를 읽거나 고르지 않고 파일 선택기·촬영·녹음·전사·자동 첨부·자동 업로드·동기화·스트림·URI 공유를 사용하지 않습니다.",
        "화면캡처·갤러리·클라우드 백업·공유 상태를 검사·분석·일치·확인·검증·유효성 판정하지 않습니다.",
        "전화·다이얼·발신·통화·종료·선별·차단·통화 상태·통화 기록·발신자 표시·문자·SMS·받는 사람 선택을 사용하지 않고 연락처·주소록·알림을 읽기·쓰기·편집·선택·감시·필터링하지 않습니다.",
        "네트워크·웹 탐색·브라우저·웹뷰·외부 링크 열기를 사용하지 않고 공식 정보·번호·디렉터리·기관·직원·신원·문서·사건·신고·접수·법률·금융 상태를 조회·검증하거나 전화·문자·신고·출동을 요청·제출하지 않습니다.",
        "클립보드·텍스트 감시기·자동완성·자격 증명 API를 사용하지 않고 계정·로그인·인증·생체 인증·키가드·키 저장소·암호화 API나 계정·인증 상태를 읽거나 바꾸지 않습니다.",
        "계좌·카드·은행·지갑·거래·금액·현금·주민 식별값·상품권·코드 값을 읽기·표시·저장·공유하지 않고 결제·은행·ATM·NFC·지갑·블록체인·상품권 동작이나 구매·잔액·교환·환급·차지백·취소·동결·차단·추적·회복을 수행하지 않습니다.",
        "전화 통신·SIM·구독·통신사 API를 사용하지 않고 전화번호·SIM/eSIM/USIM·IMEI·ICCID·IMSI·기기 ID·일련값·계정·고객 값을 읽거나 조회·검증·변경하지 않습니다.",
        "앱·패키지·설치 앱 목록·앱 이름·권한·기기·설정 상태를 읽거나 검사하지 않고 설치·제거·비활성화·강제 종료·권한 허용·철회·설정 변경·원격 연결 종료·기기 정리를 수행하지 않습니다.",
        "기기 관리자·접근성 서비스·VPN·사용 통계·알림 수신 서비스·권한 관리자·패키지 설치 도구·기기 제어 기능을 사용하지 않습니다.",
        "화면캡처·갤러리·클라우드 백업·공유·삭제 상태가 공식·실제·안전·보안·검증·유효·접수·제출·수신·해결·완료·차단·감지·삭제됨·보호·적법·신뢰·보장·문제없음이라고 판정하거나 증명하지 않습니다.",
        "수동 문자 공유는 고정 카드 제목과 기존 앱 경로·행동 종류만 담고 원문·가린 단서·계획·사건·기관 목록·연락처·전화번호·SIM·계정·결제·링크·앱·기기·캡처본·이미지·미디어·받는 사람·확인·제출·검증·삭제 결과를 담거나 파일·스트림·URI를 붙이거나 자동 발송하지 않습니다.",
        "새 Android 권한을 요청하지 않고 알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러·브로드캐스트 수신기를 만들거나 예약하지 않습니다.",
        "가족 계획·사건·공식 디렉터리·연락처·알림·통화·문자·카메라·파일·미디어·갤러리·캡처본·계정·결제·앱·패키지·권한·기기·설정·확인·결과 상태를 수정하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<ScreenshotPrivacyGuardCardId>,
        recommendedActions: List<ScreenshotPrivacyGuardAction>,
        cards: List<ScreenshotPrivacyGuardCard>
    ): String {
        val cardById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[스크린샷 개인정보 가드 코치]")
            appendLine("실제 화면캡처를 감지·차단하지 않고 캡처 전후에 직접 읽을 값 없는 문장을 고르는 로컬 수동 안내입니다.")
            appendLine()
            appendLine("추천 값 없는 화면캡처 개인정보 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 실제 캡처본·이미지·원문·가린 단서·계좌·전화번호·신분 값은 보내지 않습니다.")
            append("- 이 안내는 화면캡처를 감지·차단·삭제하거나 계정·결제·신고 행동을 수행하거나 결과를 보장하지 않습니다.")
        }
    }

    private fun actionFor(
        id: ScreenshotPrivacyGuardActionId
    ): ScreenshotPrivacyGuardAction = ScreenshotPrivacyGuardAction(
        id = id,
        label = when (id) {
            ScreenshotPrivacyGuardActionId.QUICK_STOP -> "긴급 멈춤 모드"
            ScreenshotPrivacyGuardActionId.CHECKER -> "의심 문구 검사"
            ScreenshotPrivacyGuardActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            ScreenshotPrivacyGuardActionId.SAFETY_PLAN -> "가족 안전 계획"
            ScreenshotPrivacyGuardActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
            ScreenshotPrivacyGuardActionId.FAMILY_SAFETY_DRILL_COACH -> "가족 안전 연습 코치"
            ScreenshotPrivacyGuardActionId.SAFE_PASTE_PRIVACY_COACH ->
                "안전 붙여넣기 개인정보 코치"
            ScreenshotPrivacyGuardActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                "자격 비밀값 멈춤 코치"
            ScreenshotPrivacyGuardActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크"
            ScreenshotPrivacyGuardActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
            ScreenshotPrivacyGuardActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                "증거 전달 개인정보 코치"
            ScreenshotPrivacyGuardActionId.EVIDENCE_KIT -> "증거 보존 체크"
            ScreenshotPrivacyGuardActionId.INCIDENT_TIMELINE_BRIEFING -> "사건 흐름 요약"
            ScreenshotPrivacyGuardActionId.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH ->
                "신분증·셀피 촬영 멈춤 코치"
            ScreenshotPrivacyGuardActionId.FAMILY_ALERT_MESSAGES -> "가족 알림 문구"
            ScreenshotPrivacyGuardActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 카드"
            ScreenshotPrivacyGuardActionId.TRUSTED_HELPER_REVIEW -> "보호자 검토 모드"
            ScreenshotPrivacyGuardActionId.SAFE_CALL_SCRIPTS -> "값 없는 안전 확인 전화 문구"
            ScreenshotPrivacyGuardActionId.OFFICIAL_HELP_DIRECTORY -> "공식 도움 연락처"
            ScreenshotPrivacyGuardActionId.REPORT_PREP -> "공식 신고 준비"
            ScreenshotPrivacyGuardActionId.FOLLOW_UP_CHECK_IN_PLANNER -> "후속 점검 계획"
            ScreenshotPrivacyGuardActionId.PRIVACY_CENTER -> "개인정보 센터"
            ScreenshotPrivacyGuardActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
