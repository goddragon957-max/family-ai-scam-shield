package com.mongmong.familyshield

enum class ExposureImpactId(val displayOrder: Int) {
    NO_ACTION_OR_UNCERTAIN(1),
    MONEY_OR_VALUE_SENT(2),
    CREDENTIAL_OR_PAYMENT_INFO_SHARED(3),
    APP_OR_REMOTE_ACCESS_GRANTED(4),
    LINK_CLICK_ONLY(5),
    LINK_INFO_OR_PAYMENT_ENTERED(6),
    FAMILY_IMPERSONATION_ANSWERED(7),
    UNCLEAR_OR_MULTIPLE(8)
}

enum class ExposureImpactCategory(val label: String) {
    BEFORE_ACTION("행동 전·불안 확인"),
    MONEY_VALUE("돈·상품권·가상자산"),
    ACCOUNT_IDENTITY("계정·결제·신분정보"),
    DEVICE_ACCESS("앱·화면공유·원격접근"),
    LINK_ACTIVITY("링크 클릭·정보 입력"),
    FAMILY_IMPERSONATION("가족 사칭·딥페이크"),
    UNCLEAR_COMPLEX("확인 불가·복합 상황")
}

enum class ExposureImpactPriority(val rank: Int, val label: String) {
    STOP_AND_VERIFY(0, "멈추고 직접 확인"),
    PROTECT_NOW(1, "빠르게 보호 확인"),
    PRESERVE_AND_CHECK(2, "보존하고 확인"),
    SORT_WITH_HELP(3, "함께 상황 정리")
}

enum class ExposureImpactActionId {
    QUICK_STOP,
    CHECKER,
    ANALYSIS_RESULT,
    RISK_SIGNAL_EXPLAINERS,
    FAMILY_CALLBACK_CARD,
    FAMILY_READINESS_ROADMAP,
    SAFETY_FLOW_ROUTER,
    ACCOUNT_LOCKDOWN,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    RESPONSE_CENTER,
    FAMILY_ALERT_MESSAGES,
    TRUSTED_HELPER_HANDOFF,
    SAFE_CALL_SCRIPTS,
    RECOVERY_PLAN,
    PRIVACY_CENTER
}

data class ExposureImpactAction(
    val id: ExposureImpactActionId,
    val label: String,
    val responseSituation: IncidentSituation? = null,
    val evidenceSituation: IncidentSituation? = null
)

data class ExposureImpactCard(
    val id: ExposureImpactId,
    val displayOrder: Int,
    val category: ExposureImpactCategory,
    val label: String,
    val whenThisMightApply: String,
    val firstSafeMove: String,
    val whatToAvoid: List<String>,
    val officialHelpPrepNotes: List<String>,
    val priority: ExposureImpactPriority,
    val primaryAction: ExposureImpactAction,
    val secondaryActions: List<ExposureImpactAction> = emptyList()
) {
    val primaryActionLabel: String
        get() = primaryAction.label

    val secondaryActionLabels: List<String>
        get() = secondaryActions.map { action -> action.label }
}

data class ExposureImpactTriage(
    val title: String,
    val intro: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedExposureIds: List<ExposureImpactId>,
    val recommendedActions: List<ExposureImpactAction>,
    val cards: List<ExposureImpactCard>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCards: List<ExposureImpactCard>
        get() = recommendedExposureIds.mapNotNull { exposureId ->
            cards.find { card -> card.id == exposureId }
        }

    val recommendedActionIds: List<ExposureImpactActionId>
        get() = recommendedActions.map { action -> action.id }
}

object ExposureImpactTriageBuilder {
    private val financialExposureSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.BAIT,
        ScamSignal.CREDENTIALS,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val remoteAccessSignals = setOf(
        ScamSignal.APP_INSTALL,
        ScamSignal.LIVE_CALL_CONTROL
    )

    private val linkSignals = setOf(
        ScamSignal.LINK,
        ScamSignal.PARCEL_INVITATION_SMISHING
    )

    private val familyImpersonationSignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.PHONE_NUMBER_CHANGE
    )

    private val unclearPressureSignals = setOf(
        ScamSignal.SECRECY,
        ScamSignal.IMPERSONATION
    )

    fun build(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness
    ): ExposureImpactTriage {
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val hasNoSignals = analysis == null || signals.isEmpty()
        val hasFinancialExposureSignal = analysis?.riskLevel == RiskLevel.HIGH ||
            signals.any { signal -> signal in financialExposureSignals }
        val hasRemoteAccessSignal = signals.any { signal -> signal in remoteAccessSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasFamilyImpersonationSignal =
            signals.any { signal -> signal in familyImpersonationSignals }
        val hasUnclearPressureSignal = signals.any { signal -> signal in unclearPressureSignals }

        val recommendedExposureSet = linkedSetOf<ExposureImpactId>()
        if (hasNoSignals) {
            recommendedExposureSet += ExposureImpactId.NO_ACTION_OR_UNCERTAIN
            recommendedExposureSet += ExposureImpactId.UNCLEAR_OR_MULTIPLE
        }
        if (hasFinancialExposureSignal) {
            recommendedExposureSet += ExposureImpactId.MONEY_OR_VALUE_SENT
            recommendedExposureSet += ExposureImpactId.CREDENTIAL_OR_PAYMENT_INFO_SHARED
        }
        if (hasRemoteAccessSignal) {
            recommendedExposureSet += ExposureImpactId.APP_OR_REMOTE_ACCESS_GRANTED
        }
        if (hasLinkSignal) {
            recommendedExposureSet += ExposureImpactId.LINK_CLICK_ONLY
            recommendedExposureSet += ExposureImpactId.LINK_INFO_OR_PAYMENT_ENTERED
        }
        if (hasFamilyImpersonationSignal) {
            recommendedExposureSet += ExposureImpactId.FAMILY_IMPERSONATION_ANSWERED
        }
        if (hasUnclearPressureSignal) {
            recommendedExposureSet += ExposureImpactId.UNCLEAR_OR_MULTIPLE
        }
        if (recommendedExposureSet.isEmpty()) {
            recommendedExposureSet += ExposureImpactId.NO_ACTION_OR_UNCERTAIN
            recommendedExposureSet += ExposureImpactId.UNCLEAR_OR_MULTIPLE
        }
        val recommendedExposureIds = ExposureImpactId.entries.filter { exposureId ->
            exposureId in recommendedExposureSet
        }

        val recommendedActionIds = linkedSetOf<ExposureImpactActionId>()
        if (hasNoSignals) {
            recommendedActionIds += ExposureImpactActionId.QUICK_STOP
            if (analysis == null) {
                recommendedActionIds += ExposureImpactActionId.CHECKER
            } else {
                recommendedActionIds += ExposureImpactActionId.ANALYSIS_RESULT
                recommendedActionIds += ExposureImpactActionId.CHECKER
            }
            recommendedActionIds += ExposureImpactActionId.FAMILY_CALLBACK_CARD
            recommendedActionIds += ExposureImpactActionId.SAFETY_FLOW_ROUTER
            recommendedActionIds += ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (hasFinancialExposureSignal) {
            recommendedActionIds += ExposureImpactActionId.QUICK_STOP
            recommendedActionIds += ExposureImpactActionId.RECOVERY_PLAN
            recommendedActionIds += ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionIds += ExposureImpactActionId.ACCOUNT_LOCKDOWN
            recommendedActionIds += ExposureImpactActionId.EVIDENCE_KIT
            recommendedActionIds += ExposureImpactActionId.RESPONSE_CENTER
            if (analysis != null) {
                recommendedActionIds += ExposureImpactActionId.REPORT_PREP
            }
        }
        if (hasRemoteAccessSignal) {
            recommendedActionIds += ExposureImpactActionId.ACCOUNT_LOCKDOWN
            recommendedActionIds += ExposureImpactActionId.EVIDENCE_KIT
            recommendedActionIds += ExposureImpactActionId.TRUSTED_HELPER_HANDOFF
            recommendedActionIds += ExposureImpactActionId.RESPONSE_CENTER
        }
        if (hasLinkSignal) {
            recommendedActionIds += ExposureImpactActionId.EVIDENCE_KIT
            recommendedActionIds += ExposureImpactActionId.RISK_SIGNAL_EXPLAINERS
            recommendedActionIds += ExposureImpactActionId.FAMILY_ALERT_MESSAGES
            recommendedActionIds += ExposureImpactActionId.ACCOUNT_LOCKDOWN
        }
        if (hasFamilyImpersonationSignal) {
            recommendedActionIds += ExposureImpactActionId.FAMILY_CALLBACK_CARD
            recommendedActionIds += ExposureImpactActionId.FAMILY_ALERT_MESSAGES
            recommendedActionIds += ExposureImpactActionId.SAFE_CALL_SCRIPTS
            recommendedActionIds += ExposureImpactActionId.TRUSTED_HELPER_HANDOFF
        }
        if (hasUnclearPressureSignal) {
            recommendedActionIds += ExposureImpactActionId.SAFETY_FLOW_ROUTER
            recommendedActionIds += ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (readiness.level == ReadinessLevel.LOW) {
            recommendedActionIds += ExposureImpactActionId.FAMILY_CALLBACK_CARD
            recommendedActionIds += ExposureImpactActionId.FAMILY_READINESS_ROADMAP
        }

        val cards = buildCards(
            hasAnalysis = analysis != null,
            readinessLevel = readiness.level
        )
        val recommendedActions = recommendedActionIds.map { actionId ->
            actionFor(
                id = actionId,
                hasAnalysis = analysis != null,
                readinessLevel = readiness.level
            )
        }
        val recommendationTitle = when {
            analysis == null -> "분석 전 기본 노출 확인"
            hasFinancialExposureSignal || hasRemoteAccessSignal -> "강한 신호에서 직접 확인할 노출 가능성"
            signals.isEmpty() -> "일치 신호가 없어도 직접 확인할 항목"
            else -> "현재 신호에서 직접 확인할 노출 가능성"
        }

        return ExposureImpactTriage(
            title = "무엇이 노출됐을 수 있나요?",
            intro =
                "실제로 한 행동을 사용자가 직접 확인해 알맞은 기존 대응 화면을 찾는 로컬·무상태 안내입니다. " +
                    "분석 신호만으로 돈·정보·앱 노출이 있었다고 단정하거나 안전을 판정하지 않습니다.",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                analysis = analysis,
                readinessLevel = readiness.level,
                signals = signals,
                hasFinancialExposureSignal = hasFinancialExposureSignal,
                hasRemoteAccessSignal = hasRemoteAccessSignal,
                hasLinkSignal = hasLinkSignal,
                hasFamilyImpersonationSignal = hasFamilyImpersonationSignal,
                hasUnclearPressureSignal = hasUnclearPressureSignal
            ),
            recommendedExposureIds = recommendedExposureIds,
            recommendedActions = recommendedActions,
            cards = cards,
            privacySafetyNotes = privacySafetyNotes(),
            familyShareSummary = buildFamilyShareSummary(
                recommendationTitle = recommendationTitle,
                recommendedExposureIds = recommendedExposureIds,
                recommendedActions = recommendedActions,
                cards = cards
            )
        )
    }

    private fun buildCards(
        hasAnalysis: Boolean,
        readinessLevel: ReadinessLevel
    ): List<ExposureImpactCard> {
        fun action(
            id: ExposureImpactActionId,
            responseSituation: IncidentSituation? = null,
            evidenceSituation: IncidentSituation? = null
        ): ExposureImpactAction = actionFor(
            id = id,
            hasAnalysis = hasAnalysis,
            readinessLevel = readinessLevel,
            responseSituation = responseSituation,
            evidenceSituation = evidenceSituation
        )

        fun currentAnalysisAction(): ExposureImpactAction = action(
            if (hasAnalysis) {
                ExposureImpactActionId.ANALYSIS_RESULT
            } else {
                ExposureImpactActionId.CHECKER
            }
        )

        fun reportPrepAction(): ExposureImpactAction? = if (hasAnalysis) {
            action(ExposureImpactActionId.REPORT_PREP)
        } else {
            null
        }

        return listOf(
            ExposureImpactCard(
                id = ExposureImpactId.NO_ACTION_OR_UNCERTAIN,
                displayOrder = ExposureImpactId.NO_ACTION_OR_UNCERTAIN.displayOrder,
                category = ExposureImpactCategory.BEFORE_ACTION,
                label = "아직 아무 행동도 안 함 / 불안함만 있음",
                whenThisMightApply =
                    "돈·정보·앱 행동을 하지 않았다고 생각되거나 불안함만 있을 때 해당될 수 있습니다. 실제로 한 행동을 직접 확인하세요.",
                firstSafeMove =
                    "대화를 멈추고 추가 행동을 보류한 뒤, 의심 내용을 직접 분석하고 평소 가족 번호로 확인하세요.",
                whatToAvoid = listOf(
                    "분석 신호가 적거나 행동이 기억나지 않는다는 이유로 안전하다고 단정하지 마세요.",
                    "상대가 준 새 번호·링크로 사실 확인을 이어가지 마세요."
                ),
                officialHelpPrepNotes = listOf(
                    "연락받은 시각과 상대가 요구한 행동의 종류만 시간 순서로 적으세요.",
                    "실제로 하지 않은 송금·정보 제공·앱 설치를 피해 사실처럼 말하지 마세요."
                ),
                priority = ExposureImpactPriority.STOP_AND_VERIFY,
                primaryAction = action(ExposureImpactActionId.QUICK_STOP),
                secondaryActions = listOf(
                    currentAnalysisAction(),
                    action(ExposureImpactActionId.FAMILY_CALLBACK_CARD),
                    action(ExposureImpactActionId.FAMILY_READINESS_ROADMAP)
                )
            ),
            ExposureImpactCard(
                id = ExposureImpactId.MONEY_OR_VALUE_SENT,
                displayOrder = ExposureImpactId.MONEY_OR_VALUE_SENT.displayOrder,
                category = ExposureImpactCategory.MONEY_VALUE,
                label = "돈·상품권·기프티콘·가상자산을 보냄",
                whenThisMightApply =
                    "이체·결제·상품권 번호 전달·가상자산 전송을 실제로 했을 때 해당될 수 있습니다. 분석 신호가 아닌 본인의 실제 행동을 직접 확인하세요.",
                firstSafeMove =
                    "추가 전달을 멈추고 24시간 수습 계획에서 금융회사와 공식 도움, 증거 보존 순서를 확인하세요.",
                whatToAvoid = listOf(
                    "환급·회수를 약속하는 상대에게 수수료나 추가 금액을 보내지 마세요.",
                    "지급정지·공식 도움을 늦추면서 모든 자료를 완벽히 모으려 하지 마세요."
                ),
                officialHelpPrepNotes = listOf(
                    "거래 수단, 시각, 금액, 금융회사나 구매처를 본인 메모에 정리하세요.",
                    "금융회사·경찰에 연락했다면 처리 결과와 접수번호를 보관하세요."
                ),
                priority = ExposureImpactPriority.PROTECT_NOW,
                primaryAction = action(ExposureImpactActionId.RECOVERY_PLAN),
                secondaryActions = buildList {
                    add(action(ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY))
                    add(
                        action(
                            ExposureImpactActionId.RESPONSE_CENTER,
                            responseSituation = IncidentSituation.MONEY_SENT
                        )
                    )
                    add(
                        action(
                            ExposureImpactActionId.EVIDENCE_KIT,
                            evidenceSituation = IncidentSituation.MONEY_SENT
                        )
                    )
                    reportPrepAction()?.let(::add)
                }
            ),
            ExposureImpactCard(
                id = ExposureImpactId.CREDENTIAL_OR_PAYMENT_INFO_SHARED,
                displayOrder = ExposureImpactId.CREDENTIAL_OR_PAYMENT_INFO_SHARED.displayOrder,
                category = ExposureImpactCategory.ACCOUNT_IDENTITY,
                label = "계좌·카드·비밀번호·OTP·신분정보를 말함/입력함",
                whenThisMightApply =
                    "계정·결제·인증·신분정보를 실제로 말하거나 입력했을 때 해당될 수 있습니다. 어떤 종류였는지만 직접 확인하고 비밀값은 다시 적지 마세요.",
                firstSafeMove =
                    "다른 안전한 기기와 공식 경로에서 계정·기기 잠금 체크리스트를 열어 보호 순서를 확인하세요.",
                whatToAvoid = listOf(
                    "비밀번호·인증번호·카드 비밀값을 가족 채팅이나 상담 준비 메모에 복사하지 마세요.",
                    "원격 접근이 의심되는 기기에서 새 비밀번호를 입력하지 마세요."
                ),
                officialHelpPrepNotes = listOf(
                    "노출 가능성이 있는 정보의 종류, 전달 시각, 관련 서비스만 값 없이 정리하세요.",
                    "비밀번호 변경·세션 종료·재발급을 했다면 완료 시각과 접수번호만 남기세요."
                ),
                priority = ExposureImpactPriority.PROTECT_NOW,
                primaryAction = action(ExposureImpactActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = buildList {
                    add(action(ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY))
                    add(action(ExposureImpactActionId.RECOVERY_PLAN))
                    add(
                        action(
                            ExposureImpactActionId.RESPONSE_CENTER,
                            responseSituation = IncidentSituation.CREDENTIALS_SHARED
                        )
                    )
                    add(
                        action(
                            ExposureImpactActionId.EVIDENCE_KIT,
                            evidenceSituation = IncidentSituation.CREDENTIALS_SHARED
                        )
                    )
                    reportPrepAction()?.let(::add)
                }
            ),
            ExposureImpactCard(
                id = ExposureImpactId.APP_OR_REMOTE_ACCESS_GRANTED,
                displayOrder = ExposureImpactId.APP_OR_REMOTE_ACCESS_GRANTED.displayOrder,
                category = ExposureImpactCategory.DEVICE_ACCESS,
                label = "앱 설치·화면공유·원격제어 허용",
                whenThisMightApply =
                    "앱을 설치했거나 화면공유·접근성·기기 관리자·원격조작을 실제로 허용했을 때 해당될 수 있습니다. 설치·허용 여부를 직접 확인하세요.",
                firstSafeMove =
                    "진행 중인 원격 연결을 먼저 끊고, 다른 안전한 기기에서 계정 보호와 증거 보존 순서를 확인하세요.",
                whatToAvoid = listOf(
                    "증거를 더 보려고 의심 앱이나 원격 연결을 다시 실행하지 마세요.",
                    "이 분류만 보고 기기가 안전해졌거나 앱이 제거됐다고 단정하지 마세요."
                ),
                officialHelpPrepNotes = listOf(
                    "앱 이름, 설치 시각, 허용한 권한, 연결 시작·종료 시각을 정리하세요.",
                    "기기·계정 지원과 공식 기관에서 받은 안내와 접수번호를 기록하세요."
                ),
                priority = ExposureImpactPriority.PROTECT_NOW,
                primaryAction = action(ExposureImpactActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(
                        ExposureImpactActionId.EVIDENCE_KIT,
                        evidenceSituation = IncidentSituation.APP_INSTALLED
                    ),
                    action(ExposureImpactActionId.TRUSTED_HELPER_HANDOFF),
                    action(
                        ExposureImpactActionId.RESPONSE_CENTER,
                        responseSituation = IncidentSituation.APP_INSTALLED
                    )
                )
            ),
            ExposureImpactCard(
                id = ExposureImpactId.LINK_CLICK_ONLY,
                displayOrder = ExposureImpactId.LINK_CLICK_ONLY.displayOrder,
                category = ExposureImpactCategory.LINK_ACTIVITY,
                label = "링크 클릭만 함",
                whenThisMightApply =
                    "의심 링크를 열었지만 정보 입력·파일 실행·앱 설치는 하지 않았다고 확인될 때 해당될 수 있습니다. 이후 행동을 직접 다시 확인하세요.",
                firstSafeMove =
                    "링크를 다시 열지 말고 메시지와 클릭 시각을 보존한 뒤 위험 신호와 계정 점검 경로를 확인하세요.",
                whatToAvoid = listOf(
                    "링크 상태를 확인하려고 다시 누르거나 내려받은 파일을 실행하지 마세요.",
                    "이 앱이 링크나 기기를 자동 검사했다고 생각하지 마세요."
                ),
                officialHelpPrepNotes = listOf(
                    "메시지 수신·클릭 시각과 다운로드·정보 입력·앱 설치 여부를 구분해 적으세요.",
                    "이후 로그인·결제·보안 알림이 있었다면 시각과 관련 서비스만 정리하세요."
                ),
                priority = ExposureImpactPriority.PRESERVE_AND_CHECK,
                primaryAction = action(
                    ExposureImpactActionId.EVIDENCE_KIT,
                    evidenceSituation = IncidentSituation.LINK_OPENED
                ),
                secondaryActions = listOf(
                    action(ExposureImpactActionId.RISK_SIGNAL_EXPLAINERS),
                    action(ExposureImpactActionId.ACCOUNT_LOCKDOWN),
                    action(ExposureImpactActionId.FAMILY_ALERT_MESSAGES)
                )
            ),
            ExposureImpactCard(
                id = ExposureImpactId.LINK_INFO_OR_PAYMENT_ENTERED,
                displayOrder = ExposureImpactId.LINK_INFO_OR_PAYMENT_ENTERED.displayOrder,
                category = ExposureImpactCategory.LINK_ACTIVITY,
                label = "링크에서 개인정보/결제정보 입력",
                whenThisMightApply =
                    "링크를 연 뒤 계정·개인·결제정보를 실제로 입력했을 때 해당될 수 있습니다. 입력한 정보의 종류만 직접 확인하세요.",
                firstSafeMove =
                    "링크를 닫고 다른 안전한 기기의 공식 경로에서 계정·결제 보호를 시작한 뒤 공식 도움과 증거 순서를 확인하세요.",
                whatToAvoid = listOf(
                    "입력 내용을 확인하려고 의심 페이지에 다시 로그인하지 마세요.",
                    "실제 계정·결제·신분정보 값을 가족 공유문이나 일반 메모에 옮기지 마세요."
                ),
                officialHelpPrepNotes = listOf(
                    "입력한 정보의 종류, 입력 시각, 관련 서비스만 실제 값 없이 정리하세요.",
                    "계정·카드 보호 조치와 상담 접수번호를 시간 순서로 기록하세요."
                ),
                priority = ExposureImpactPriority.PROTECT_NOW,
                primaryAction = action(ExposureImpactActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = buildList {
                    add(action(ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY))
                    add(action(ExposureImpactActionId.RECOVERY_PLAN))
                    add(
                        action(
                            ExposureImpactActionId.EVIDENCE_KIT,
                            evidenceSituation = IncidentSituation.CREDENTIALS_SHARED
                        )
                    )
                    reportPrepAction()?.let(::add)
                }
            ),
            ExposureImpactCard(
                id = ExposureImpactId.FAMILY_IMPERSONATION_ANSWERED,
                displayOrder = ExposureImpactId.FAMILY_IMPERSONATION_ANSWERED.displayOrder,
                category = ExposureImpactCategory.FAMILY_IMPERSONATION,
                label = "가족 새 번호/딥페이크/가족 사칭에 답함",
                whenThisMightApply =
                    "가족을 자칭한 새 번호·음성·영상·메신저에 답했을 때 해당될 수 있습니다. 답장만 했는지 다른 행동도 했는지 직접 확인하세요.",
                firstSafeMove =
                    "현재 연락을 멈추고 새 번호가 아닌 평소 가족 번호와 별도 가족 경로로 콜백 확인하세요.",
                whatToAvoid = listOf(
                    "익숙한 이름·사진·목소리만으로 본인이라고 단정하지 마세요.",
                    "새 번호로만 안전 질문을 이어가거나 추가 정보를 보내지 마세요."
                ),
                officialHelpPrepNotes = listOf(
                    "연락 수단과 시각, 상대가 요구한 행동, 실제로 답하거나 한 행동만 구분해 적으세요.",
                    "평소 가족 경로로 확인한 시각과 가족 경고 여부를 정리하세요."
                ),
                priority = ExposureImpactPriority.STOP_AND_VERIFY,
                primaryAction = action(ExposureImpactActionId.FAMILY_CALLBACK_CARD),
                secondaryActions = listOf(
                    action(ExposureImpactActionId.FAMILY_ALERT_MESSAGES),
                    action(ExposureImpactActionId.SAFE_CALL_SCRIPTS),
                    action(ExposureImpactActionId.TRUSTED_HELPER_HANDOFF)
                )
            ),
            ExposureImpactCard(
                id = ExposureImpactId.UNCLEAR_OR_MULTIPLE,
                displayOrder = ExposureImpactId.UNCLEAR_OR_MULTIPLE.displayOrder,
                category = ExposureImpactCategory.UNCLEAR_COMPLEX,
                label = "확인 불가/복합 상황",
                whenThisMightApply =
                    "무엇을 했는지 기억이 흐리거나 돈·정보·앱·링크 행동이 여러 개 겹쳤을 때 해당될 수 있습니다. 한 단계씩 직접 확인하세요.",
                firstSafeMove =
                    "추가 행동을 멈추고 믿을 만한 사람과 시간 순서를 정리한 뒤 안전 흐름 길잡이에서 가까운 기존 대응을 고르세요.",
                whatToAvoid = listOf(
                    "기억나지 않는 부분을 추측해 피해 사실로 확정하지 마세요.",
                    "혼자 해결하려고 상대와 다시 연락하거나 의심 링크·앱을 다시 열지 마세요."
                ),
                officialHelpPrepNotes = listOf(
                    "확실한 사실, 기억이 불분명한 부분, 실제로 한 보호 조치를 나눠 시간 순서로 적으세요.",
                    "공식 상담에서는 모르는 부분을 모른다고 말하고 다음 확인 순서를 물으세요."
                ),
                priority = ExposureImpactPriority.SORT_WITH_HELP,
                primaryAction = action(ExposureImpactActionId.SAFETY_FLOW_ROUTER),
                secondaryActions = buildList {
                    add(action(ExposureImpactActionId.RECOVERY_PLAN))
                    add(action(ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY))
                    add(action(ExposureImpactActionId.TRUSTED_HELPER_HANDOFF))
                    reportPrepAction()?.let(::add)
                    add(action(ExposureImpactActionId.PRIVACY_CENTER))
                }
            )
        )
    }

    private fun actionFor(
        id: ExposureImpactActionId,
        hasAnalysis: Boolean,
        readinessLevel: ReadinessLevel,
        responseSituation: IncidentSituation? = null,
        evidenceSituation: IncidentSituation? = null
    ): ExposureImpactAction = ExposureImpactAction(
        id = id,
        label = when (id) {
            ExposureImpactActionId.QUICK_STOP -> "긴급 멈춤 모드"
            ExposureImpactActionId.CHECKER -> if (hasAnalysis) {
                "다른 의심 내용 직접 분석하기"
            } else {
                "의심 내용 직접 분석하기"
            }
            ExposureImpactActionId.ANALYSIS_RESULT -> "현재 분석 결과 보기"
            ExposureImpactActionId.RISK_SIGNAL_EXPLAINERS -> "위험 신호 설명 카드"
            ExposureImpactActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 확인 카드"
            ExposureImpactActionId.FAMILY_READINESS_ROADMAP -> when (readinessLevel) {
                ReadinessLevel.LOW -> "가족 준비 로드맵 시작하기"
                ReadinessLevel.IN_PROGRESS -> "가족 준비 로드맵 보완하기"
                ReadinessLevel.READY -> "가족 준비 로드맵 점검하기"
            }
            ExposureImpactActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
            ExposureImpactActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
            ExposureImpactActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
            ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처 보기"
            ExposureImpactActionId.REPORT_PREP -> "신고용 상담 준비 카드"
            ExposureImpactActionId.RESPONSE_CENTER -> "긴급 대응 센터"
            ExposureImpactActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
            ExposureImpactActionId.TRUSTED_HELPER_HANDOFF -> "보호자 도움 요청 카드"
            ExposureImpactActionId.SAFE_CALL_SCRIPTS -> "안전 확인 전화 스크립트"
            ExposureImpactActionId.RECOVERY_PLAN -> "24시간 수습 계획"
            ExposureImpactActionId.PRIVACY_CENTER -> "개인정보 센터"
        },
        responseSituation = responseSituation,
        evidenceSituation = evidenceSituation
    )

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readinessLevel: ReadinessLevel,
        signals: Set<ScamSignal>,
        hasFinancialExposureSignal: Boolean,
        hasRemoteAccessSignal: Boolean,
        hasLinkSignal: Boolean,
        hasFamilyImpersonationSignal: Boolean,
        hasUnclearPressureSignal: Boolean
    ): String {
        val situationCopy = when {
            analysis == null ->
                "현재 분석이 없어도 괜찮다고 단정하지 않습니다. 아직 행동하지 않은 경우와 확인 불가·복합 상황을 먼저 보고 직접 분석과 가족 콜백을 이어가세요."
            signals.isEmpty() && analysis.riskLevel != RiskLevel.HIGH ->
                "뚜렷하게 일치한 신호가 없어도 안전을 증명하지 않습니다. 실제로 한 행동을 다시 확인하고 현재 결과·새 분석·가족 콜백 경로를 살펴보세요."
            else -> buildList {
                if (hasFinancialExposureSignal) {
                    add("강한 위험도나 돈·계정·결제 관련 신호가 있어 돈·가치 전달과 계정·결제정보 노출 가능성을 함께 직접 확인하세요.")
                }
                if (hasRemoteAccessSignal) {
                    add("앱·화면공유·원격제어 신호가 있어 실제 설치·허용 여부를 직접 확인하세요.")
                }
                if (hasLinkSignal) {
                    add("링크·스미싱 신호가 있어 클릭만 했는지 정보 입력까지 했는지 구분해 확인하세요.")
                }
                if (hasFamilyImpersonationSignal) {
                    add("가족·딥페이크·번호 변경 신호가 있어 답장 뒤 다른 행동이 있었는지 확인하고 평소 번호로 콜백하세요.")
                }
                if (hasUnclearPressureSignal) {
                    add("비밀 유지·기관 사칭 압박이 있어 확인 불가·복합 상황 경로도 함께 보세요.")
                }
                if (isEmpty()) {
                    add("현재 신호만으로 실제 노출을 알 수 없으므로 본인이 한 행동과 가까운 분류를 직접 고르세요.")
                }
            }.joinToString(" ")
        }

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW ->
                "가족 준비가 아직 부족해도 괜찮습니다. 가족 콜백과 준비 로드맵을 차분히 시작하세요."
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중입니다. 지금은 평소 콜백 경로를 확인하고 빠진 준비는 나중에 보완하세요."
            ReadinessLevel.READY ->
                "가족 콜백 기반이 준비되어 있어도 노출 여부는 별도로 직접 확인하고, 실제 상황과 맞는 항목만 고르세요."
        }
        return "$situationCopy $readinessCopy"
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 분류는 사용자가 직접 여는 수동 안내이며 현재 분석의 위험도·일치 신호와 값 없는 가족 준비 수준만 기기 안의 로컬 규칙으로 사용합니다.",
        "분석 신호는 실제 송금·정보 입력·앱 설치·링크 클릭을 증명하지 않습니다. 실제로 한 행동을 사용자가 직접 확인하세요.",
        "문자·메시지·통화·연락처를 읽거나 감시하지 않고 가족을 자동 모니터링하지 않습니다.",
        "노출 분류 선택·답변·완료·체크·점수·읽음·진행 상태, 공유 이력, 노출 이력을 저장하지 않습니다.",
        "화면을 열거나 버튼을 누르는 것만으로 사건 기록을 추가·수정·삭제하지 않습니다.",
        "공유 버튼은 연락처를 읽거나 받는 사람을 고르지 않으며, 사용자가 공유 앱과 대상을 직접 선택하기 전에는 자동으로 보내지 않습니다.",
        "네트워크를 사용하거나 새 Android 권한을 요청하지 않고 전화·문자를 자동 실행하거나 차단하지 않습니다.",
        "이 분류는 사기·안전을 확정하거나 악성 앱 제거, 계정·카드 정지, 피해금 회수, 법률 자문 또는 결과를 보장하지 않습니다. 필요한 조치는 공식 경로에서 직접 진행하세요.",
        "앱은 비밀번호·인증번호·안전 질문·연락처 같은 비밀 값에 접근하지 않으며 분류 결과로 데이터를 자동 삭제하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        recommendationTitle: String,
        recommendedExposureIds: List<ExposureImpactId>,
        recommendedActions: List<ExposureImpactAction>,
        cards: List<ExposureImpactCard>
    ): String {
        val cardsById = cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 노출 가능성 분류")
            appendLine()
            appendLine(recommendationTitle)
            recommendedExposureIds.forEach { exposureId ->
                val card = cardsById.getValue(exposureId)
                appendLine("- [${card.category.label}] ${card.label}")
            }
            appendLine()
            appendLine("먼저 이어갈 앱 행동")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("전체 노출 분류와 일반 안전 행동")
            cards.forEach { card ->
                appendLine("- [${card.category.label}] ${card.label}")
                appendLine("  먼저: ${card.firstSafeMove}")
                appendLine("  앱 행동: ${actionLabels(card)}")
            }
            appendLine()
            appendLine("함께 지킬 일반 안전 행동")
            appendLine("- 대화를 멈추고 추가 송금·정보 제공·앱 행동을 보류합니다.")
            appendLine("- 새 번호나 메시지 속 경로 대신 평소 가족 번호와 공식 경로로 직접 확인합니다.")
            appendLine("- 무엇을 했는지 불분명하면 믿을 만한 사람과 시간 순서를 정리하고 공식 도움을 확인합니다.")
            appendLine()
            appendLine("이 분류는 실제 노출을 증명하지 않는 로컬·무상태 수동 안내입니다.")
            appendLine("앱은 연락처를 읽거나 받는 사람을 고르지 않고 메시지를 자동으로 보내지 않습니다.")
            append(
                "붙여넣은 원문·개인정보 가림 단서·안전 질문·콜백 내용·연락처 값과 전화·계좌·" +
                    "비밀번호·인증번호·PIN·링크·도메인·신분정보 값은 이 공유문에 포함하지 않았습니다."
            )
        }.trim()
    }

    private fun actionLabels(card: ExposureImpactCard): String =
        (listOf(card.primaryAction) + card.secondaryActions)
            .joinToString(" / ") { action -> action.label }
}
