package com.mongmong.familyshield

enum class ActiveContactExitScriptCardId(val displayOrder: Int) {
    HANG_UP_CALL(1),
    STOP_MESSAGE_REPLY(2),
    REFUSE_CREDENTIALS(3),
    REFUSE_APP_SCREEN_SHARE(4),
    VERIFY_FAMILY_EXISTING_NUMBER(5),
    END_IMPERSONATION(6),
    STOP_LINK(7),
    TELL_FAMILY_AFTER_EXIT(8)
}

enum class ActiveContactExitScriptActionId {
    QUICK_STOP,
    CHECKER,
    ANALYSIS_RESULT,
    PRIVACY_CENTER,
    EXPOSURE_IMPACT_TRIAGE,
    FAMILY_CALLBACK_CARD,
    SAFE_CALL_SCRIPTS,
    FAMILY_ALERT_MESSAGES,
    TRUSTED_HELPER_REVIEW,
    FAMILY_READINESS_ROADMAP,
    ACCOUNT_LOCKDOWN,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    INCIDENT_TIMELINE_BRIEFING,
    SAFETY_FLOW_ROUTER,
    FAMILY_CALM_DEBRIEF_AGENDA
}

data class ActiveContactExitScriptAction(
    val id: ActiveContactExitScriptActionId,
    val label: String
)

data class ActiveContactExitScriptCard(
    val id: ActiveContactExitScriptCardId,
    val displayOrder: Int,
    val title: String,
    val scriptText: String,
    val whenToUse: String,
    val neverSayOrDo: List<String>,
    val primaryAction: ActiveContactExitScriptAction,
    val secondaryActions: List<ActiveContactExitScriptAction>
) {
    val routeActions: List<ActiveContactExitScriptAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class ActiveContactExitScripts(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<ActiveContactExitScriptCardId>,
    val recommendedActions: List<ActiveContactExitScriptAction>,
    val cards: List<ActiveContactExitScriptCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<ActiveContactExitScriptCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<ActiveContactExitScriptActionId>
        get() = recommendedActions.map { action -> action.id }
}

object ActiveContactExitScriptsBuilder {
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

    private val remoteControlSignals = setOf(
        ScamSignal.APP_INSTALL,
        ScamSignal.LIVE_CALL_CONTROL
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
    ): ActiveContactExitScripts {
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val hasNoAnalysisOrSignals = analysis == null || signals.isEmpty()
        val hasHighRisk = analysis?.riskLevel == RiskLevel.HIGH
        val hasFinancialSignal = signals.any { signal -> signal in financialSignals }
        val hasCredentialSignal = signals.any { signal -> signal in credentialSignals }
        val hasRemoteControlSignal = signals.any { signal -> signal in remoteControlSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasFamilyVerificationSignal =
            signals.any { signal -> signal in familyVerificationSignals }
        val hasSecrecyImpersonationSignal =
            signals.any { signal -> signal in secrecyImpersonationSignals }
        val hasSavedIncident = incidentCount.coerceAtLeast(0) > 0
        val needsReadinessSupport = readiness.level != ReadinessLevel.READY

        val recommendedCardSet = linkedSetOf(
            ActiveContactExitScriptCardId.HANG_UP_CALL,
            ActiveContactExitScriptCardId.TELL_FAMILY_AFTER_EXIT
        )
        if (hasNoAnalysisOrSignals) {
            recommendedCardSet += ActiveContactExitScriptCardId.STOP_MESSAGE_REPLY
            recommendedCardSet += ActiveContactExitScriptCardId.REFUSE_CREDENTIALS
            recommendedCardSet += ActiveContactExitScriptCardId.REFUSE_APP_SCREEN_SHARE
            recommendedCardSet += ActiveContactExitScriptCardId.VERIFY_FAMILY_EXISTING_NUMBER
            recommendedCardSet += ActiveContactExitScriptCardId.END_IMPERSONATION
            recommendedCardSet += ActiveContactExitScriptCardId.STOP_LINK
        }
        if (hasHighRisk || hasFinancialSignal) {
            recommendedCardSet += ActiveContactExitScriptCardId.REFUSE_CREDENTIALS
            recommendedCardSet += ActiveContactExitScriptCardId.END_IMPERSONATION
        }
        if (hasCredentialSignal) {
            recommendedCardSet += ActiveContactExitScriptCardId.REFUSE_CREDENTIALS
        }
        if (hasRemoteControlSignal) {
            recommendedCardSet += ActiveContactExitScriptCardId.REFUSE_APP_SCREEN_SHARE
        }
        if (hasLinkSignal) {
            recommendedCardSet += ActiveContactExitScriptCardId.STOP_MESSAGE_REPLY
            recommendedCardSet += ActiveContactExitScriptCardId.STOP_LINK
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet += ActiveContactExitScriptCardId.VERIFY_FAMILY_EXISTING_NUMBER
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedCardSet += ActiveContactExitScriptCardId.END_IMPERSONATION
        }
        if (hasSavedIncident || needsReadinessSupport) {
            recommendedCardSet += ActiveContactExitScriptCardId.TELL_FAMILY_AFTER_EXIT
        }
        val recommendedCardIds = ActiveContactExitScriptCardId.entries
            .filter { cardId -> cardId in recommendedCardSet }

        val recommendedActionSet = linkedSetOf(
            ActiveContactExitScriptActionId.QUICK_STOP,
            if (analysis == null) {
                ActiveContactExitScriptActionId.CHECKER
            } else {
                ActiveContactExitScriptActionId.ANALYSIS_RESULT
            },
            ActiveContactExitScriptActionId.SAFETY_FLOW_ROUTER
        )
        if (hasNoAnalysisOrSignals) {
            recommendedActionSet += ActiveContactExitScriptActionId.PRIVACY_CENTER
            recommendedActionSet += ActiveContactExitScriptActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += ActiveContactExitScriptActionId.SAFE_CALL_SCRIPTS
        }
        if (hasHighRisk || hasFinancialSignal || hasCredentialSignal) {
            recommendedActionSet += ActiveContactExitScriptActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += ActiveContactExitScriptActionId.EVIDENCE_KIT
            recommendedActionSet += ActiveContactExitScriptActionId.OFFICIAL_HELP_DIRECTORY
            if (analysis != null) {
                recommendedActionSet += ActiveContactExitScriptActionId.REPORT_PREP
            }
        }
        if (hasRemoteControlSignal) {
            recommendedActionSet += ActiveContactExitScriptActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += ActiveContactExitScriptActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += ActiveContactExitScriptActionId.EVIDENCE_KIT
        }
        if (hasLinkSignal) {
            recommendedActionSet += ActiveContactExitScriptActionId.CHECKER
            recommendedActionSet += ActiveContactExitScriptActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += ActiveContactExitScriptActionId.PRIVACY_CENTER
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += ActiveContactExitScriptActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += ActiveContactExitScriptActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedActionSet += ActiveContactExitScriptActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasSavedIncident) {
            recommendedActionSet += ActiveContactExitScriptActionId.EVIDENCE_KIT
            recommendedActionSet += ActiveContactExitScriptActionId.INCIDENT_TIMELINE_BRIEFING
        }
        if (needsReadinessSupport) {
            recommendedActionSet += ActiveContactExitScriptActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += ActiveContactExitScriptActionId.FAMILY_READINESS_ROADMAP
        }
        recommendedActionSet += ActiveContactExitScriptActionId.FAMILY_CALM_DEBRIEF_AGENDA
        val recommendedActions = ActiveContactExitScriptActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards(hasAnalysis = analysis != null)
        val recommendationTitle = when {
            analysis == null -> "분석 전 보수적인 연락 종료 문장"
            hasHighRisk || hasFinancialSignal || hasCredentialSignal ->
                "돈·인증 비밀을 주지 않고 지금 끝내기"
            hasRemoteControlSignal -> "앱·화면공유를 거절하고 지금 끝내기"
            hasLinkSignal -> "링크를 열거나 답장하지 않고 끝내기"
            hasFamilyVerificationSignal -> "새 번호를 믿지 않고 별도 확인하기"
            hasSecrecyImpersonationSignal -> "비밀 압박·기관 사칭 연락 끝내기"
            else -> "확인 전 연락을 보수적으로 끝내기"
        }

        return ActiveContactExitScripts(
            title = "진행 중 연락 종료 문장",
            intro =
                "수상한 통화·문자·카톡·영상통화·화면공유·앱 설치 압박이 이어질 때 " +
                    "짧게 읽고 직접 연락을 끝내는 로컬 전용·무상태 수동 문장입니다. " +
                    "이 앱은 전화를 걸거나 끊거나 차단하지 않고 문자를 보내지 않습니다.",
            modeLabel = "지금 읽기 · 직접 말하고 직접 연락 끝내기",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                analysis = analysis,
                readinessLevel = readiness.level,
                signals = signals,
                hasHighRisk = hasHighRisk,
                hasFinancialSignal = hasFinancialSignal,
                hasCredentialSignal = hasCredentialSignal,
                hasRemoteControlSignal = hasRemoteControlSignal,
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

    private fun buildCards(hasAnalysis: Boolean): List<ActiveContactExitScriptCard> {
        fun action(
            id: ActiveContactExitScriptActionId
        ): ActiveContactExitScriptAction = actionFor(id)

        fun currentResultAction(): ActiveContactExitScriptAction = action(
            if (hasAnalysis) {
                ActiveContactExitScriptActionId.ANALYSIS_RESULT
            } else {
                ActiveContactExitScriptActionId.CHECKER
            }
        )

        return listOf(
            ActiveContactExitScriptCard(
                id = ActiveContactExitScriptCardId.HANG_UP_CALL,
                displayOrder = ActiveContactExitScriptCardId.HANG_UP_CALL.displayOrder,
                title = "통화 바로 끊기 문장",
                scriptText =
                    "지금은 확인 전이라 통화를 끊겠습니다. 평소 알던 번호나 공식 경로로 다시 확인하겠습니다.",
                whenToUse =
                    "상대가 통화·영상통화를 계속 유지시키거나 지금 결정하라고 재촉할 때",
                neverSayOrDo = listOf(
                    "논쟁하거나 상대가 시키는 확인 절차를 통화 안에서 계속하지 마세요.",
                    "송금·결제·인증정보 제공·앱 설치를 약속하지 마세요."
                ),
                primaryAction = action(ActiveContactExitScriptActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(ActiveContactExitScriptActionId.FAMILY_CALLBACK_CARD),
                    action(ActiveContactExitScriptActionId.SAFE_CALL_SCRIPTS),
                    action(ActiveContactExitScriptActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            ActiveContactExitScriptCard(
                id = ActiveContactExitScriptCardId.STOP_MESSAGE_REPLY,
                displayOrder = ActiveContactExitScriptCardId.STOP_MESSAGE_REPLY.displayOrder,
                title = "문자·카톡 답장 멈춤 문장",
                scriptText =
                    "지금은 답장을 멈추겠습니다. 링크는 열지 않고 가족과 별도로 확인하겠습니다.",
                whenToUse =
                    "문자·카톡·메신저에서 답장, 개인정보, 송금이나 빠른 확인을 재촉할 때",
                neverSayOrDo = listOf(
                    "이름·연락처·계좌·인증정보 같은 실제 값을 답장으로 보내지 마세요.",
                    "상대가 보낸 링크·파일·새 연락처를 열거나 저장하지 마세요."
                ),
                primaryAction = currentResultAction(),
                secondaryActions = listOf(
                    action(ActiveContactExitScriptActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES),
                    action(ActiveContactExitScriptActionId.PRIVACY_CENTER)
                )
            ),
            ActiveContactExitScriptCard(
                id = ActiveContactExitScriptCardId.REFUSE_CREDENTIALS,
                displayOrder = ActiveContactExitScriptCardId.REFUSE_CREDENTIALS.displayOrder,
                title = "인증번호·비밀번호 거절 문장",
                scriptText =
                    "인증번호나 비밀번호는 말하거나 보내지 않겠습니다. 지금 연락을 끝내겠습니다.",
                whenToUse =
                    "인증번호, 비밀번호, 계정·카드·신분정보 또는 상품권 비밀값을 요구할 때",
                neverSayOrDo = listOf(
                    "인증번호·비밀번호·보안카드·상품권 비밀값을 읽어주거나 화면으로 보여주지 마세요.",
                    "계좌·카드·신분정보 전체 값이나 가족 확인 답을 알려주지 마세요."
                ),
                primaryAction = action(ActiveContactExitScriptActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(ActiveContactExitScriptActionId.OFFICIAL_HELP_DIRECTORY),
                    action(ActiveContactExitScriptActionId.EVIDENCE_KIT),
                    action(ActiveContactExitScriptActionId.QUICK_STOP)
                )
            ),
            ActiveContactExitScriptCard(
                id = ActiveContactExitScriptCardId.REFUSE_APP_SCREEN_SHARE,
                displayOrder = ActiveContactExitScriptCardId.REFUSE_APP_SCREEN_SHARE.displayOrder,
                title = "앱 설치·화면공유 거절 문장",
                scriptText =
                    "앱 설치·화면공유·원격제어는 하지 않겠습니다. 지금 연락을 끝내겠습니다.",
                whenToUse =
                    "앱 설치, 화면공유, 원격제어, 스피커폰 유지나 기기 설정 변경을 요구할 때",
                neverSayOrDo = listOf(
                    "상대가 보낸 앱·파일을 설치하거나 화면·알림·인증정보를 보여주지 마세요.",
                    "이 문장이 연결 해제나 앱 제거를 대신한다고 여기지 마세요."
                ),
                primaryAction = action(ActiveContactExitScriptActionId.TRUSTED_HELPER_REVIEW),
                secondaryActions = listOf(
                    action(ActiveContactExitScriptActionId.ACCOUNT_LOCKDOWN),
                    action(ActiveContactExitScriptActionId.EVIDENCE_KIT),
                    action(ActiveContactExitScriptActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            ActiveContactExitScriptCard(
                id = ActiveContactExitScriptCardId.VERIFY_FAMILY_EXISTING_NUMBER,
                displayOrder = ActiveContactExitScriptCardId.VERIFY_FAMILY_EXISTING_NUMBER.displayOrder,
                title = "가족 새 번호·목소리 사칭 확인 문장",
                scriptText =
                    "새 번호나 목소리만으로 확인하지 않겠습니다. 지금 끊고 평소 알던 가족 경로로 다시 확인하겠습니다.",
                whenToUse =
                    "가족이 새 번호라고 하거나 익숙한 목소리·영상으로 긴급한 부탁을 할 때",
                neverSayOrDo = listOf(
                    "새 번호에 가족 확인 답, 연락처, 돈이나 인증정보를 알려주지 마세요.",
                    "목소리·영상·프로필만으로 본인이라고 단정하지 마세요."
                ),
                primaryAction = action(ActiveContactExitScriptActionId.FAMILY_CALLBACK_CARD),
                secondaryActions = listOf(
                    action(ActiveContactExitScriptActionId.SAFE_CALL_SCRIPTS),
                    action(ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES),
                    action(ActiveContactExitScriptActionId.QUICK_STOP)
                )
            ),
            ActiveContactExitScriptCard(
                id = ActiveContactExitScriptCardId.END_IMPERSONATION,
                displayOrder = ActiveContactExitScriptCardId.END_IMPERSONATION.displayOrder,
                title = "기관·은행·수사기관 사칭 종료 문장",
                scriptText =
                    "이 연락은 여기서 끝내겠습니다. 비밀로 하지 않고 제가 직접 찾은 공식 경로로 확인하겠습니다.",
                whenToUse =
                    "기관·은행·수사기관을 자칭하거나 가족에게 알리지 말고 따르라고 압박할 때",
                neverSayOrDo = listOf(
                    "비밀 유지 요구를 따르거나 상대가 알려준 번호·링크로 다시 확인하지 마세요.",
                    "법적·금융 불이익이라는 말에 밀려 돈·정보·앱 행동을 계속하지 마세요."
                ),
                primaryAction = action(ActiveContactExitScriptActionId.OFFICIAL_HELP_DIRECTORY),
                secondaryActions = listOf(
                    action(ActiveContactExitScriptActionId.REPORT_PREP),
                    action(ActiveContactExitScriptActionId.EVIDENCE_KIT),
                    action(ActiveContactExitScriptActionId.QUICK_STOP)
                )
            ),
            ActiveContactExitScriptCard(
                id = ActiveContactExitScriptCardId.STOP_LINK,
                displayOrder = ActiveContactExitScriptCardId.STOP_LINK.displayOrder,
                title = "링크·청첩장·택배 문자 멈춤 문장",
                scriptText =
                    "링크는 열지 않고 답장도 하지 않겠습니다. 가족과 별도 경로로 확인하겠습니다.",
                whenToUse =
                    "택배·청첩장·부고·결제 알림이나 혜택을 내세워 링크 클릭을 재촉할 때",
                neverSayOrDo = listOf(
                    "링크를 다시 열거나 주소를 복사해 다른 사람에게 보내지 마세요.",
                    "답장으로 개인정보를 주거나 링크가 검사·차단되었다고 단정하지 마세요."
                ),
                primaryAction = currentResultAction(),
                secondaryActions = listOf(
                    action(ActiveContactExitScriptActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES),
                    action(ActiveContactExitScriptActionId.PRIVACY_CENTER)
                )
            ),
            ActiveContactExitScriptCard(
                id = ActiveContactExitScriptCardId.TELL_FAMILY_AFTER_EXIT,
                displayOrder = ActiveContactExitScriptCardId.TELL_FAMILY_AFTER_EXIT.displayOrder,
                title = "끊은 뒤 가족에게 말할 문장",
                scriptText =
                    "방금 수상한 연락을 끝냈습니다. 추가 행동은 멈추겠습니다. 실제 비밀값 없이 어떤 행동을 요구받았는지만 같이 확인해 주세요.",
                whenToUse =
                    "연락을 끝낸 직후 혼자 판단하지 않고 가족이나 믿을 만한 보호자에게 도움을 청할 때",
                neverSayOrDo = listOf(
                    "가족 대화에도 원문·정확한 전화·계좌·인증·신분정보 값을 올리지 마세요.",
                    "누구 탓인지 따지기보다 추가 행동을 멈추고 필요한 기존 경로를 직접 고르세요."
                ),
                primaryAction = action(ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES),
                secondaryActions = listOf(
                    action(ActiveContactExitScriptActionId.FAMILY_CALLBACK_CARD),
                    action(ActiveContactExitScriptActionId.FAMILY_READINESS_ROADMAP),
                    action(ActiveContactExitScriptActionId.FAMILY_CALM_DEBRIEF_AGENDA)
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readinessLevel: ReadinessLevel,
        signals: Set<ScamSignal>,
        hasHighRisk: Boolean,
        hasFinancialSignal: Boolean,
        hasCredentialSignal: Boolean,
        hasRemoteControlSignal: Boolean,
        hasLinkSignal: Boolean,
        hasFamilyVerificationSignal: Boolean,
        hasSecrecyImpersonationSignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (analysis == null || signals.isEmpty()) {
                add(
                    "현재 분석이 없거나 일치 신호가 없습니다. 이를 문제없다는 판정으로 보지 말고 " +
                        "통화·답장·링크·정보·앱 행동을 멈추는 문장부터 보수적으로 고르세요."
                )
            }
            if (hasHighRisk || hasFinancialSignal) {
                add(
                    "위험도가 높거나 금융·송금·결제 압박 신호가 있어 통화를 끝내고 인증 비밀을 " +
                        "거절한 뒤 계정 보호·증거·공식 도움 경로를 권합니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증정보 요구 신호가 있어 인증번호·비밀번호를 말하거나 보내지 않는 문장을 " +
                        "먼저 권합니다."
                )
            }
            if (hasRemoteControlSignal) {
                add(
                    "앱 설치·원격제어·통화 유지 신호가 있어 앱·화면공유를 거절하고 곁의 보호자와 " +
                        "계정·기기·증거 경로를 직접 확인하도록 권합니다."
                )
            }
            if (hasLinkSignal) {
                add(
                    "링크·스미싱 신호가 있어 열지 않고 답장하지 않는 문장과 검사·노출 분류·가족 " +
                        "경고·개인정보 경로를 권합니다. 이 안내는 링크를 검사하지 않습니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족·목소리 합성·번호 변경 신호가 있어 새 번호와 현재 목소리만 믿지 않고 " +
                        "평소 알던 별도 가족 경로로 확인하도록 권합니다."
                )
            }
            if (hasSecrecyImpersonationSignal) {
                add(
                    "비밀 유지·기관 사칭 압박 신호가 있어 ‘비밀로 하지 않고 연락을 끝내겠다’는 " +
                        "문장과 사용자가 직접 찾는 공식 경로를 권합니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 개수 범주만 반영하며 사건 내용은 읽지 않습니다. 연락을 " +
                        "끝낸 뒤 증거·타임라인 경로만 함께 권합니다."
                )
            }
            if (isEmpty()) {
                add("현재 신호만으로 사실을 확정하지 않고 연락을 끝낸 뒤 별도 경로로 직접 확인하세요.")
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW ->
                "가족 준비가 부족한 상태라 콜백·가족 경고·준비 로드맵을 함께 권합니다. 실제 가족 정보는 이 화면에 입력하지 마세요."
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중이라 콜백·가족 경고·준비 로드맵으로 빠진 부분을 직접 확인하도록 권합니다. 실제 저장 값은 추천에 들어오지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있어도 이번 연락의 사실이나 조치 완료가 확인된 것은 아닙니다. 연락을 끝내고 별도 경로로 다시 확인하세요."
        }
        return "$situationCopy $readinessCopy"
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 문장은 사용자가 화면을 직접 읽고 직접 말하는 로컬 전용·무상태 수동 안내입니다.",
        "추천은 위험도·일치 신호, 값 없는 가족 준비 수준, 저장 사건 개수 범주만 사용하며 원문·가린 단서·사건 내용·가족 안전 계획 값을 사용하지 않습니다.",
        "이름·연락처·정확한 전화번호·계좌·카드·인증번호·비밀번호·상품권 비밀값·가족 확인 답·신분정보·정확한 링크나 도메인을 묻거나 저장하지 않습니다.",
        "카드 선택·추천·읽음·공유·말하기·답장·통화 종료·체크 상태·메모·이력을 위한 새 저장소를 만들거나 기록하지 않습니다.",
        "문자·메시지·통화·연락처·클립보드를 자동으로 읽거나 감시하지 않습니다.",
        "네트워크를 사용하거나 새 Android 권한을 요청하지 않습니다.",
        "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커를 만들거나 예약하지 않으며 스케줄러도 사용하지 않습니다.",
        "전화를 걸거나 끊거나 선별·차단하지 않고, 문자를 보내거나 답장하거나 받는 사람을 고르지 않습니다. 통화 자동 발신·자동 종료나 SMS 자동 전송 기능이 아닙니다.",
        "붙여넣은 글, 통화 상태, 연락처, 사건 기록, 가족 안전 계획이나 기기 설정을 추가·수정·삭제하지 않습니다.",
        "원문·링크·앱·기기를 자동 검사·감시·가림·삭제·차단하지 않고 악성 앱 제거, 계정·카드 조치, 피해 회복을 수행하지 않습니다.",
        "이 안내는 사기·피해·본인확인·조치 완료·통화 차단·악성 앱 제거·계정 또는 카드 정지·피해 회복·법률·금융 결과를 증명하거나 보장하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<ActiveContactExitScriptCardId>,
        recommendedActions: List<ActiveContactExitScriptAction>,
        cards: List<ActiveContactExitScriptCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 진행 중 연락 종료 문장")
            appendLine()
            appendLine("지금 직접 읽을 추천 문장")
            recommendedCardIds.forEach { cardId ->
                val card = cardsById.getValue(cardId)
                appendLine("- ${card.title}: ${card.scriptText}")
            }
            appendLine()
            appendLine("연락을 끝낸 뒤 직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("전체 수동 종료 문장")
            cards.forEach { card -> appendLine("- ${card.title}") }
            appendLine()
            append(
                "원문·사람·연락처·비밀값을 넣지 않은 일반 수동 문장입니다. 가족이 직접 읽고 " +
                    "필요한 기존 앱 경로를 고릅니다."
            )
        }.trim()
    }

    private fun actionFor(
        id: ActiveContactExitScriptActionId
    ): ActiveContactExitScriptAction = ActiveContactExitScriptAction(
        id = id,
        label = when (id) {
            ActiveContactExitScriptActionId.QUICK_STOP -> "긴급 멈춤 모드"
            ActiveContactExitScriptActionId.CHECKER -> "의심 내용 검사"
            ActiveContactExitScriptActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            ActiveContactExitScriptActionId.PRIVACY_CENTER -> "개인정보 센터"
            ActiveContactExitScriptActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
            ActiveContactExitScriptActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 확인 카드"
            ActiveContactExitScriptActionId.SAFE_CALL_SCRIPTS -> "안전 확인 전화 스크립트"
            ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
            ActiveContactExitScriptActionId.TRUSTED_HELPER_REVIEW -> "보호자와 함께 안전 검토"
            ActiveContactExitScriptActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
            ActiveContactExitScriptActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
            ActiveContactExitScriptActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            ActiveContactExitScriptActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처"
            ActiveContactExitScriptActionId.REPORT_PREP -> "신고용 상담 준비 카드"
            ActiveContactExitScriptActionId.INCIDENT_TIMELINE_BRIEFING -> "사건 타임라인 요약"
            ActiveContactExitScriptActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
            ActiveContactExitScriptActionId.FAMILY_CALM_DEBRIEF_AGENDA -> "5분 가족 차분 대화"
        }
    )
}
