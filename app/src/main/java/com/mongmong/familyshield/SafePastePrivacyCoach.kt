package com.mongmong.familyshield

enum class SafePastePrivacyCoachCardId(val displayOrder: Int) {
    MINIMUM_NEEDED_CONTEXT(1),
    PHONE_ACCOUNT_IDENTITY(2),
    CREDENTIALS_AND_SAFETY_ANSWERS(3),
    LINK_AND_DOMAIN(4),
    APP_AND_REMOTE_CONTROL(5),
    FAMILY_CALLBACK_DETAILS(6),
    ACTION_CATEGORY_CONTEXT(7),
    OPEN_EXISTING_CHECKER(8)
}

enum class SafePastePrivacyCoachActionId {
    CHECKER,
    ANALYSIS_RESULT,
    SAMPLE_LIBRARY,
    PRIVACY_CENTER,
    EXPOSURE_IMPACT_TRIAGE,
    QUICK_STOP,
    FAMILY_CALLBACK_CARD,
    SAFE_CALL_SCRIPTS,
    FAMILY_ALERT_MESSAGES,
    TRUSTED_HELPER_REVIEW,
    FAMILY_READINESS_ROADMAP,
    ACCOUNT_LOCKDOWN,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    INCIDENT_TIMELINE_BRIEFING,
    SAFETY_FLOW_ROUTER
}

data class SafePastePrivacyCoachAction(
    val id: SafePastePrivacyCoachActionId,
    val label: String
)

data class SafePastePrivacyCoachCard(
    val id: SafePastePrivacyCoachCardId,
    val displayOrder: Int,
    val title: String,
    val whyItMatters: String,
    val howToMaskManually: String,
    val whatNotToPasteOrShare: List<String>,
    val primaryAction: SafePastePrivacyCoachAction,
    val secondaryActions: List<SafePastePrivacyCoachAction>
) {
    val routeActions: List<SafePastePrivacyCoachAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class SafePastePrivacyCoach(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCardIds: List<SafePastePrivacyCoachCardId>,
    val recommendedActions: List<SafePastePrivacyCoachAction>,
    val cards: List<SafePastePrivacyCoachCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<SafePastePrivacyCoachCard>
        get() = recommendedCardIds.mapNotNull { cardId ->
            cards.find { card -> card.id == cardId }
        }

    val recommendedActionIds: List<SafePastePrivacyCoachActionId>
        get() = recommendedActions.map { action -> action.id }
}

object SafePastePrivacyCoachBuilder {
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
    ): SafePastePrivacyCoach {
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
        val safeIncidentCount = incidentCount.coerceAtLeast(0)
        val hasSavedIncident = safeIncidentCount > 0
        val needsReadinessSupport = readiness.level != ReadinessLevel.READY

        val recommendedCardSet = linkedSetOf(
            SafePastePrivacyCoachCardId.MINIMUM_NEEDED_CONTEXT,
            SafePastePrivacyCoachCardId.ACTION_CATEGORY_CONTEXT
        )
        if (hasNoAnalysisOrSignals) {
            recommendedCardSet += SafePastePrivacyCoachCardId.PHONE_ACCOUNT_IDENTITY
            recommendedCardSet += SafePastePrivacyCoachCardId.CREDENTIALS_AND_SAFETY_ANSWERS
            recommendedCardSet += SafePastePrivacyCoachCardId.LINK_AND_DOMAIN
            recommendedCardSet += SafePastePrivacyCoachCardId.APP_AND_REMOTE_CONTROL
            recommendedCardSet += SafePastePrivacyCoachCardId.FAMILY_CALLBACK_DETAILS
            recommendedCardSet += SafePastePrivacyCoachCardId.OPEN_EXISTING_CHECKER
        }
        if (hasHighRisk || hasFinancialSignal) {
            recommendedCardSet += SafePastePrivacyCoachCardId.PHONE_ACCOUNT_IDENTITY
            recommendedCardSet += SafePastePrivacyCoachCardId.CREDENTIALS_AND_SAFETY_ANSWERS
            recommendedCardSet += SafePastePrivacyCoachCardId.OPEN_EXISTING_CHECKER
        }
        if (hasHighRisk || hasCredentialSignal) {
            recommendedCardSet += SafePastePrivacyCoachCardId.PHONE_ACCOUNT_IDENTITY
            recommendedCardSet += SafePastePrivacyCoachCardId.CREDENTIALS_AND_SAFETY_ANSWERS
            recommendedCardSet += SafePastePrivacyCoachCardId.OPEN_EXISTING_CHECKER
        }
        if (hasRemoteControlSignal) {
            recommendedCardSet += SafePastePrivacyCoachCardId.APP_AND_REMOTE_CONTROL
            recommendedCardSet += SafePastePrivacyCoachCardId.CREDENTIALS_AND_SAFETY_ANSWERS
            recommendedCardSet += SafePastePrivacyCoachCardId.OPEN_EXISTING_CHECKER
        }
        if (hasLinkSignal) {
            recommendedCardSet += SafePastePrivacyCoachCardId.LINK_AND_DOMAIN
            recommendedCardSet += SafePastePrivacyCoachCardId.OPEN_EXISTING_CHECKER
        }
        if (hasFamilyVerificationSignal) {
            recommendedCardSet += SafePastePrivacyCoachCardId.FAMILY_CALLBACK_DETAILS
            recommendedCardSet += SafePastePrivacyCoachCardId.CREDENTIALS_AND_SAFETY_ANSWERS
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedCardSet += SafePastePrivacyCoachCardId.OPEN_EXISTING_CHECKER
        }
        if (hasSavedIncident) {
            recommendedCardSet += SafePastePrivacyCoachCardId.PHONE_ACCOUNT_IDENTITY
        }
        if (needsReadinessSupport) {
            recommendedCardSet += SafePastePrivacyCoachCardId.FAMILY_CALLBACK_DETAILS
            recommendedCardSet += SafePastePrivacyCoachCardId.CREDENTIALS_AND_SAFETY_ANSWERS
        }
        val recommendedCardIds = SafePastePrivacyCoachCardId.entries.filter { cardId ->
            cardId in recommendedCardSet
        }

        val recommendedActionSet = linkedSetOf(
            SafePastePrivacyCoachActionId.CHECKER,
            SafePastePrivacyCoachActionId.PRIVACY_CENTER
        )
        if (analysis != null) {
            recommendedActionSet += SafePastePrivacyCoachActionId.ANALYSIS_RESULT
        }
        if (hasNoAnalysisOrSignals) {
            recommendedActionSet += SafePastePrivacyCoachActionId.SAMPLE_LIBRARY
            recommendedActionSet += SafePastePrivacyCoachActionId.FAMILY_CALLBACK_CARD
        }
        if (hasHighRisk || hasFinancialSignal || hasCredentialSignal) {
            recommendedActionSet += SafePastePrivacyCoachActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += SafePastePrivacyCoachActionId.EVIDENCE_KIT
            recommendedActionSet += SafePastePrivacyCoachActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (hasRemoteControlSignal) {
            recommendedActionSet += SafePastePrivacyCoachActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += SafePastePrivacyCoachActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += SafePastePrivacyCoachActionId.EVIDENCE_KIT
            recommendedActionSet += SafePastePrivacyCoachActionId.SAFETY_FLOW_ROUTER
        }
        if (hasLinkSignal) {
            recommendedActionSet += SafePastePrivacyCoachActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += SafePastePrivacyCoachActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasFamilyVerificationSignal) {
            recommendedActionSet += SafePastePrivacyCoachActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += SafePastePrivacyCoachActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += SafePastePrivacyCoachActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasSecrecyImpersonationSignal) {
            recommendedActionSet += SafePastePrivacyCoachActionId.QUICK_STOP
            recommendedActionSet += SafePastePrivacyCoachActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (hasSavedIncident) {
            recommendedActionSet += SafePastePrivacyCoachActionId.EVIDENCE_KIT
            recommendedActionSet += SafePastePrivacyCoachActionId.INCIDENT_TIMELINE_BRIEFING
        }
        if (needsReadinessSupport) {
            recommendedActionSet += SafePastePrivacyCoachActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += SafePastePrivacyCoachActionId.FAMILY_READINESS_ROADMAP
        }
        val recommendedActions = SafePastePrivacyCoachActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val cards = buildCards(hasAnalysis = analysis != null)
        val recommendationTitle = when {
            analysis == null -> "분석 전 보수적인 수동 가리기"
            hasHighRisk || hasFinancialSignal || hasCredentialSignal ->
                "금융·인증 비밀부터 직접 가리기"
            hasRemoteControlSignal -> "앱·원격제어 세부정보를 직접 가리기"
            hasLinkSignal -> "정확한 링크 대신 행동 범주 남기기"
            hasFamilyVerificationSignal -> "가족 확인 비밀을 제외하고 범주 남기기"
            hasSecrecyImpersonationSignal -> "원문 압박을 멈추고 범주만 남기기"
            else -> "필요한 맥락만 보수적으로 남기기"
        }

        return SafePastePrivacyCoach(
            title = "붙여넣기 전 개인정보 가리기",
            intro =
                "의심 문자·메신저·통화 문구에서 분석에 불필요한 비밀을 사용자가 먼저 직접 " +
                    "가리는 로컬 전용·무상태 수동 안내입니다. 이 코치는 원문을 받거나 자동으로 " +
                    "고치지 않으며 선택이나 진행 상태를 기록하지 않습니다.",
            modeLabel = "붙여넣기 전 · 직접 읽고 직접 가리기",
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

    private fun buildCards(hasAnalysis: Boolean): List<SafePastePrivacyCoachCard> {
        fun action(id: SafePastePrivacyCoachActionId): SafePastePrivacyCoachAction =
            actionFor(id)

        fun currentResultAction(): SafePastePrivacyCoachAction = action(
            if (hasAnalysis) {
                SafePastePrivacyCoachActionId.ANALYSIS_RESULT
            } else {
                SafePastePrivacyCoachActionId.CHECKER
            }
        )

        return listOf(
            SafePastePrivacyCoachCard(
                id = SafePastePrivacyCoachCardId.MINIMUM_NEEDED_CONTEXT,
                displayOrder = SafePastePrivacyCoachCardId.MINIMUM_NEEDED_CONTEXT.displayOrder,
                title = "원문 전체를 그대로 붙이지 않기",
                whyItMatters =
                    "위험 신호를 살피는 데 필요하지 않은 대화 이력과 개인 정보는 가족을 식별할 단서가 될 수 있습니다.",
                howToMaskManually =
                    "압박 표현과 요청 행동이 드러나는 짧은 부분만 사용자가 직접 남기고, 나머지 원문은 입력하기 전에 빼세요.",
                whatNotToPasteOrShare = listOf(
                    "사기 원문 전체, 앞뒤 대화 이력, 사람 이름이나 프로필 설명",
                    "개인 메모, 음성 기록 전체, 가족만 아는 사정"
                ),
                primaryAction = action(SafePastePrivacyCoachActionId.PRIVACY_CENTER),
                secondaryActions = listOf(
                    action(SafePastePrivacyCoachActionId.SAMPLE_LIBRARY),
                    action(SafePastePrivacyCoachActionId.CHECKER)
                )
            ),
            SafePastePrivacyCoachCard(
                id = SafePastePrivacyCoachCardId.PHONE_ACCOUNT_IDENTITY,
                displayOrder = SafePastePrivacyCoachCardId.PHONE_ACCOUNT_IDENTITY.displayOrder,
                title = "전화·계좌·신분정보 가리기",
                whyItMatters =
                    "정확한 식별값 없이도 송금·결제·신분정보 요구라는 행동 종류를 남길 수 있습니다.",
                howToMaskManually =
                    "정확한 값은 직접 지우고 [전화], [계좌], [카드], [신분정보]로 바꾸되 " +
                        "‘계좌번호 요구’ 같은 행동 범주는 남기세요.",
                whatNotToPasteOrShare = listOf(
                    "전화번호, 계좌·카드 번호, 정확한 금액이나 거래 식별값",
                    "주민·여권·면허 정보, 신분증 사진 내용, 생년월일과 주소"
                ),
                primaryAction = action(SafePastePrivacyCoachActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(SafePastePrivacyCoachActionId.EVIDENCE_KIT),
                    action(SafePastePrivacyCoachActionId.OFFICIAL_HELP_DIRECTORY)
                )
            ),
            SafePastePrivacyCoachCard(
                id = SafePastePrivacyCoachCardId.CREDENTIALS_AND_SAFETY_ANSWERS,
                displayOrder =
                    SafePastePrivacyCoachCardId.CREDENTIALS_AND_SAFETY_ANSWERS.displayOrder,
                title = "OTP/PIN/비밀번호/안전질문 답 가리기",
                whyItMatters =
                    "인증값과 가족 확인 답은 요청 유형만으로 충분하며 실제 값은 다시 노출할 필요가 없습니다.",
                howToMaskManually =
                    "실제 값은 직접 지우고 [인증정보] 또는 [가족확인답]으로 바꾼 뒤 " +
                        "‘인증번호(OTP) 요구’라는 범주만 남기세요.",
                whatNotToPasteOrShare = listOf(
                    "OTP·PIN·비밀번호·보안카드 값·복구 코드",
                    "가족 안전단어, 안전질문 답, 실제 콜백 문장"
                ),
                primaryAction = action(SafePastePrivacyCoachActionId.CHECKER),
                secondaryActions = listOf(
                    action(SafePastePrivacyCoachActionId.ACCOUNT_LOCKDOWN),
                    action(SafePastePrivacyCoachActionId.PRIVACY_CENTER)
                )
            ),
            SafePastePrivacyCoachCard(
                id = SafePastePrivacyCoachCardId.LINK_AND_DOMAIN,
                displayOrder = SafePastePrivacyCoachCardId.LINK_AND_DOMAIN.displayOrder,
                title = "링크·도메인은 [링크]로 바꾸기",
                whyItMatters =
                    "정확한 주소를 다시 열지 않아도 링크 클릭 유도나 택배·청첩장 사칭이라는 맥락을 남길 수 있습니다.",
                howToMaskManually =
                    "주소 전체를 사용자가 직접 [링크]로 바꾸고 ‘택배 조회 링크 클릭 요구’처럼 행동 범주만 적으세요.",
                whatNotToPasteOrShare = listOf(
                    "정확한 URL·도메인·단축 주소·검색어·접속 매개값",
                    "링크를 열어 확인한 개인정보나 로그인 화면 값"
                ),
                primaryAction = action(SafePastePrivacyCoachActionId.CHECKER),
                secondaryActions = listOf(
                    action(SafePastePrivacyCoachActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(SafePastePrivacyCoachActionId.FAMILY_ALERT_MESSAGES),
                    action(SafePastePrivacyCoachActionId.PRIVACY_CENTER)
                )
            ),
            SafePastePrivacyCoachCard(
                id = SafePastePrivacyCoachCardId.APP_AND_REMOTE_CONTROL,
                displayOrder = SafePastePrivacyCoachCardId.APP_AND_REMOTE_CONTROL.displayOrder,
                title = "앱·원격제어 세부정보 가리기",
                whyItMatters =
                    "설치·화면공유·통화 유지 요구의 종류만으로 대응 경로를 고를 수 있습니다.",
                howToMaskManually =
                    "앱 이름·설치 주소·접속 값은 빼고 ‘앱 설치 유도’, ‘원격제어 요구’, " +
                        "‘통화 끊지 말라는 압박’만 남기세요.",
                whatNotToPasteOrShare = listOf(
                    "설치 파일·앱 주소, 원격 접속 코드, 기기 식별값",
                    "화면공유 장면, 계정 화면, 상대가 알려 준 설정값"
                ),
                primaryAction = action(SafePastePrivacyCoachActionId.TRUSTED_HELPER_REVIEW),
                secondaryActions = listOf(
                    action(SafePastePrivacyCoachActionId.ACCOUNT_LOCKDOWN),
                    action(SafePastePrivacyCoachActionId.EVIDENCE_KIT),
                    action(SafePastePrivacyCoachActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            SafePastePrivacyCoachCard(
                id = SafePastePrivacyCoachCardId.FAMILY_CALLBACK_DETAILS,
                displayOrder = SafePastePrivacyCoachCardId.FAMILY_CALLBACK_DETAILS.displayOrder,
                title = "가족 정보·콜백 답 가리기",
                whyItMatters =
                    "가족 사칭 여부를 살필 때도 이름·새 번호·가족 확인 답 자체는 필요하지 않습니다.",
                howToMaskManually =
                    "값은 직접 빼고 ‘가족 휴대폰 고장 사칭’, ‘새 번호로 연락 요구’, " +
                        "‘목소리 합성 의심’, ‘안전질문 답 요구’만 남기세요.",
                whatNotToPasteOrShare = listOf(
                    "가족 이름·별명·사진 설명, 정확한 기존 번호와 새 번호",
                    "안전단어·안전질문 답, 콜백 스크립트, 신뢰 연락처 값"
                ),
                primaryAction = action(SafePastePrivacyCoachActionId.FAMILY_CALLBACK_CARD),
                secondaryActions = listOf(
                    action(SafePastePrivacyCoachActionId.SAFE_CALL_SCRIPTS),
                    action(SafePastePrivacyCoachActionId.FAMILY_ALERT_MESSAGES),
                    action(SafePastePrivacyCoachActionId.FAMILY_READINESS_ROADMAP)
                )
            ),
            SafePastePrivacyCoachCard(
                id = SafePastePrivacyCoachCardId.ACTION_CATEGORY_CONTEXT,
                displayOrder = SafePastePrivacyCoachCardId.ACTION_CATEGORY_CONTEXT.displayOrder,
                title = "행동 종류는 남기기",
                whyItMatters =
                    "로컬 규칙은 돈·정보·링크·앱·가족 사칭 같은 요청 종류와 압박 표현으로 참고 신호를 정리합니다.",
                howToMaskManually =
                    "‘송금 요구’, ‘인증번호 요구’, ‘앱 설치 유도’, ‘가족 휴대폰 고장 사칭’, " +
                        "‘경찰·은행 사칭’과 ‘지금 바로’, ‘비밀’ 같은 압박 범주만 남기세요.",
                whatNotToPasteOrShare = listOf(
                    "정확한 금액·시간·장소·거래 정보와 실제 상대 식별값",
                    "탐지 결과가 피해나 사기를 확정한다는 문장"
                ),
                primaryAction = action(SafePastePrivacyCoachActionId.EXPOSURE_IMPACT_TRIAGE),
                secondaryActions = listOf(
                    action(SafePastePrivacyCoachActionId.QUICK_STOP),
                    action(SafePastePrivacyCoachActionId.OFFICIAL_HELP_DIRECTORY),
                    currentResultAction()
                )
            ),
            SafePastePrivacyCoachCard(
                id = SafePastePrivacyCoachCardId.OPEN_EXISTING_CHECKER,
                displayOrder = SafePastePrivacyCoachCardId.OPEN_EXISTING_CHECKER.displayOrder,
                title = "붙여넣은 뒤 기존 검사로 이동하기",
                whyItMatters =
                    "수동으로 비밀을 뺀 뒤 기존 로컬 규칙 검사와 이미 마련된 가족 대응 화면을 이어서 사용할 수 있습니다.",
                howToMaskManually =
                    "가리기 항목을 한 번 더 직접 확인한 뒤 검사 입력칸에 필요한 범주와 압박 문구만 붙여넣으세요.",
                whatNotToPasteOrShare = listOf(
                    "이 코치가 원문을 자동으로 바꾸거나 확인했다고 여기지 마세요.",
                    "정확한 링크를 다시 열거나 비밀값을 복원해 넣지 마세요."
                ),
                primaryAction = action(SafePastePrivacyCoachActionId.CHECKER),
                secondaryActions = listOf(
                    action(SafePastePrivacyCoachActionId.SAMPLE_LIBRARY),
                    action(SafePastePrivacyCoachActionId.PRIVACY_CENTER),
                    action(SafePastePrivacyCoachActionId.SAFETY_FLOW_ROUTER)
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
                    "현재 분석이 없거나 일치 신호가 없습니다. 이를 괜찮음이나 문제없음 판정으로 " +
                        "보지 말고 원문·식별값·인증값·링크를 먼저 직접 가리세요."
                )
            }
            if (hasHighRisk || hasFinancialSignal) {
                add(
                    "위험도가 높거나 금융·결제·긴급 송금 관련 신호가 있어 전화·계좌·신분정보와 " +
                        "인증 비밀을 가린 뒤 검사·계정 보호·증거·공식 도움 경로를 권합니다."
                )
            }
            if (hasCredentialSignal) {
                add(
                    "인증정보 관련 신호가 있어 OTP·PIN·비밀번호·가족 확인 답의 실제 값은 빼고 " +
                        "‘인증번호(OTP) 요구’라는 범주만 남기세요."
                )
            } else if (hasHighRisk) {
                add(
                    "위험도가 높아 인증정보 신호가 따로 없어도 OTP·PIN·비밀번호·가족 확인 답은 " +
                        "보수적으로 가리고 실제 값 대신 일반 범주만 남기세요."
                )
            }
            if (hasRemoteControlSignal) {
                add(
                    "앱 설치·원격제어·통화 유지 관련 신호가 있어 앱·접속 세부값은 넣지 않고 " +
                        "보호자 검토와 계정·기기 점검 경로를 권합니다."
                )
            }
            if (hasLinkSignal) {
                add(
                    "링크·스미싱 관련 신호가 있어 정확한 주소를 [링크]로 직접 바꾸고 검사·노출 분류·" +
                        "가족 경고·개인정보 경로를 권합니다. 이 안내는 링크를 열거나 검사하지 않습니다."
                )
            }
            if (hasFamilyVerificationSignal) {
                add(
                    "가족 사칭·목소리 합성·번호 변경 관련 신호가 있어 이름·번호·안전질문 답은 빼고 " +
                        "별도 콜백과 일반 가족 경고 경로를 권합니다."
                )
            }
            if (hasSecrecyImpersonationSignal) {
                add(
                    "비밀 유지·기관 사칭 압박 신호가 있어 원문 전체를 숨기고 추가 행동을 멈춘 뒤 " +
                        "공식 경로를 사용자가 직접 찾도록 권합니다."
                )
            }
            if (hasSavedIncident) {
                add(
                    "저장 사건이 있다는 범주만 반영했으며 사건 내용은 읽지 않습니다. 비밀값을 다시 " +
                        "꺼내지 않고 증거·타임라인 경로만 함께 권합니다."
                )
            }
            if (isEmpty()) {
                add(
                    "현재 신호만으로 사실을 확정하지 않고 불필요한 비밀을 직접 가린 뒤 기존 검사와 " +
                        "가족 확인 경로를 이용하세요."
                )
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW ->
                "가족 준비가 부족한 상태라 별도 콜백·준비 로드맵·개인정보 센터를 함께 권합니다. 실제 가족 정보나 답은 이 코치에 입력하지 마세요."
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중이라 콜백·준비 로드맵·개인정보 센터로 빠진 부분을 직접 확인하도록 권합니다. 실제 저장 값은 추천 문구에 들어오지 않습니다."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있어도 이번 문구가 문제없거나 본인확인이 끝났다는 뜻은 아닙니다. 비밀을 가리고 별도 경로로 다시 확인하세요."
        }
        return "$situationCopy $readinessCopy"
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 사용자가 화면을 직접 읽고 직접 가리는 로컬 전용·무상태 수동 안내입니다.",
        "붙여넣을 문장을 받거나 파싱·스캔하지 않으며 원문, 가린 단서나 저장 사건 내용을 읽지 않습니다. 가족 준비 추천에는 실제 계획 값이 아닌 준비 수준만 사용합니다.",
        "이름·연락처·전화번호·계좌·카드·OTP/PIN·비밀번호·안전질문 답·신분정보·정확한 링크나 도메인을 묻거나 저장하지 않습니다.",
        "카드 선택, 읽음·진행·체크 상태, 가림·공유 이력, 이름·담당자·메모·클립보드 내용이나 붙여넣기 이력을 저장하지 않습니다.",
        "문자·메시지·통화·연락처·클립보드를 자동으로 읽거나 감시하지 않습니다.",
        "네트워크를 사용하거나 새 Android 권한을 요청하지 않습니다.",
        "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커를 만들거나 예약하지 않습니다.",
        "사용자의 글을 자동 가림·교정·검사·수정·삭제하지 않고 메시지를 자동 전송하거나 받는 사람을 고르지 않습니다.",
        "화면을 열거나 카드·경로·공유 버튼을 눌러도 붙여넣은 글이나 사건 기록을 추가·수정·삭제하지 않습니다.",
        "추천은 위험도·일치 신호, 값 없는 가족 준비 수준, 저장 사건 유무만 사용하며 실제 비밀값이나 사건별 내용을 사용하지 않습니다.",
        "이 안내는 사기·피해·본인확인·조치 완료를 증명하지 않고 앱 제거·계정 또는 금융 조치·피해 회수·법률·금융 결과를 수행하거나 보장하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCardIds: List<SafePastePrivacyCoachCardId>,
        recommendedActions: List<SafePastePrivacyCoachAction>,
        cards: List<SafePastePrivacyCoachCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 붙여넣기 전 개인정보 가리기")
            appendLine()
            appendLine("추천 수동 가리기 카드")
            recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("전체 수동 가리기 순서")
            cards.forEach { card -> appendLine("- ${card.title}") }
            appendLine()
            append(
                "원문이나 실제 비밀값 없이 만든 일반 체크리스트입니다. 가족이 직접 가린 뒤 필요한 " +
                    "기존 앱 경로를 선택합니다."
            )
        }.trim()
    }

    private fun actionFor(
        id: SafePastePrivacyCoachActionId
    ): SafePastePrivacyCoachAction = SafePastePrivacyCoachAction(
        id = id,
        label = when (id) {
            SafePastePrivacyCoachActionId.CHECKER -> "의심 내용 검사"
            SafePastePrivacyCoachActionId.ANALYSIS_RESULT -> "현재 분석 결과"
            SafePastePrivacyCoachActionId.SAMPLE_LIBRARY -> "사기 패턴 샘플 연습"
            SafePastePrivacyCoachActionId.PRIVACY_CENTER -> "개인정보 센터"
            SafePastePrivacyCoachActionId.EXPOSURE_IMPACT_TRIAGE -> "노출 영향 분류"
            SafePastePrivacyCoachActionId.QUICK_STOP -> "긴급 멈춤 모드"
            SafePastePrivacyCoachActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 확인 카드"
            SafePastePrivacyCoachActionId.SAFE_CALL_SCRIPTS -> "안전 확인 전화 스크립트"
            SafePastePrivacyCoachActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
            SafePastePrivacyCoachActionId.TRUSTED_HELPER_REVIEW -> "보호자와 함께 안전 검토"
            SafePastePrivacyCoachActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
            SafePastePrivacyCoachActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
            SafePastePrivacyCoachActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            SafePastePrivacyCoachActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처"
            SafePastePrivacyCoachActionId.INCIDENT_TIMELINE_BRIEFING -> "사건 타임라인 요약"
            SafePastePrivacyCoachActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
        }
    )
}
