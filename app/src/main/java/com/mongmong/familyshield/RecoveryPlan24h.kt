package com.mongmong.familyshield

enum class RecoveryWindowId {
    IMMEDIATE_15MIN,
    WITHIN_1_HOUR,
    WITHIN_TODAY,
    WITHIN_WEEK
}

enum class RecoveryActionId {
    QUICK_STOP,
    FAMILY_CALLBACK_CARD,
    CHECKER,
    ANALYSIS_RESULT,
    SAFETY_FLOW_ROUTER,
    RESPONSE_CENTER,
    FAMILY_ALERT_MESSAGES,
    ACCOUNT_LOCKDOWN,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    NEW_ANALYSIS,
    SAFETY_PLAN,
    TIMELINE_BRIEFING,
    PRIVACY_CENTER
}

enum class RecoveryUrgency(val rank: Int, val label: String) {
    STOP_NOW(0, "지금 먼저"),
    STABILIZE(1, "안정화"),
    CLEANUP(2, "정리·보존"),
    PREVENT(3, "예방·점검")
}

data class RecoveryAction(
    val id: RecoveryActionId,
    val label: String
)

data class RecoveryWindow(
    val id: RecoveryWindowId,
    val displayOrder: Int,
    val urgency: RecoveryUrgency,
    val label: String,
    val purpose: String,
    val checklistSteps: List<String>,
    val doNotDoNotes: List<String>,
    val primaryAction: RecoveryAction,
    val secondaryActions: List<RecoveryAction> = emptyList()
) {
    val primaryActionLabel: String
        get() = primaryAction.label

    val secondaryActionLabels: List<String>
        get() = secondaryActions.map { action -> action.label }
}

data class RecoveryPlan24h(
    val title: String,
    val intro: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedWindowIds: List<RecoveryWindowId>,
    val windows: List<RecoveryWindow>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedWindows: List<RecoveryWindow>
        get() = recommendedWindowIds.mapNotNull { windowId ->
            windows.find { window -> window.id == windowId }
        }

    val recommendedActionIds: List<RecoveryActionId>
        get() = recommendedWindows
            .flatMap { window -> listOf(window.primaryAction) + window.secondaryActions }
            .map { action -> action.id }
            .distinct()
}

object RecoveryPlan24hBuilder {
    private val urgentExposureSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.BAIT,
        ScamSignal.CREDENTIALS,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
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

    private val familySignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.PHONE_NUMBER_CHANGE
    )

    fun build(analysis: ScamAnalysis?, readiness: FamilyReadiness): RecoveryPlan24h {
        val hasAnalysis = analysis != null
        val windows = buildWindows(hasAnalysis)
        val recommendedWindowIds = recommendWindowIds(analysis)
        val recommendationTitle = if (analysis == null) {
            "분석 전 기본 순서"
        } else {
            "현재 상황 기준 추천 순서"
        }
        val privacySafetyNotes = listOf(
            "이 계획은 사용자가 직접 여는 수동 안내이며 문자·통화·연락처를 자동으로 읽거나 감시하지 않습니다.",
            "현재 분석은 사용자가 직접 붙여넣거나 텍스트로 공유한 내용만 기기 안에서 판단하며 네트워크로 업로드하지 않습니다.",
            "시간대별 체크 상태나 선택은 저장하지 않으며, 이 화면을 여는 것만으로 사건 기록을 추가·수정·삭제하지 않습니다.",
            "공유 버튼은 연락처를 읽거나 받는 사람을 고르지 않고 자동으로 보내지 않습니다. 사용자가 공유 앱과 대상을 직접 선택합니다.",
            "이 앱은 계좌·카드를 자동으로 지급정지하거나 악성 앱을 자동으로 제거하지 않으며, 계정을 자동으로 잠그거나 피해금 회수를 보장하지 않습니다.",
            "법률·금융 자문이나 안전·회복 결과를 보장하는 기능이 아니며, 이 계획을 위해 네트워크 호출이나 새 Android 권한을 사용하지 않습니다."
        )

        return RecoveryPlan24h(
            title = "24시간 수습 계획",
            intro = "지금의 압박을 멈춘 뒤 다음 한 시간, 오늘, 이번 주에 무엇을 할지 차분한 순서로 안내합니다. 자동 복구나 결과를 보장하지 않는 수동 안내입니다.",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(analysis, readiness),
            recommendedWindowIds = recommendedWindowIds,
            windows = windows,
            privacySafetyNotes = privacySafetyNotes,
            familyShareSummary = buildFamilyShareSummary(
                recommendationTitle = recommendationTitle,
                recommendedWindowIds = recommendedWindowIds,
                windows = windows
            )
        )
    }

    private fun buildWindows(hasAnalysis: Boolean): List<RecoveryWindow> = listOf(
        RecoveryWindow(
            id = RecoveryWindowId.IMMEDIATE_15MIN,
            displayOrder = 1,
            urgency = RecoveryUrgency.STOP_NOW,
            label = "지금~15분",
            purpose = "대화를 멈추고 추가 노출을 막는 즉시 행동입니다.",
            checklistSteps = listOf(
                "지금 통화나 대화를 멈추고 답장을 보류하세요.",
                "확인 전에는 송금, 개인정보·인증번호 제공, 앱 설치를 하지 마세요.",
                "상대가 알려준 새 번호나 링크가 아니라 평소 저장한 가족 번호나 공식 경로로 확인을 시작하세요."
            ),
            doNotDoNotes = listOf(
                "상대를 설득하거나 상황을 해명하려 하지 마세요.",
                "무섭거나 급하게 느껴져도 추가 정보를 보내며 대응하지 마세요."
            ),
            primaryAction = RecoveryAction(RecoveryActionId.QUICK_STOP, "긴급 멈춤 모드 다시 보기"),
            secondaryActions = listOf(
                RecoveryAction(RecoveryActionId.FAMILY_CALLBACK_CARD, "가족 콜백 확인 카드"),
                if (hasAnalysis) {
                    RecoveryAction(RecoveryActionId.ANALYSIS_RESULT, "현재 분석 결과 보기")
                } else {
                    RecoveryAction(RecoveryActionId.CHECKER, "의심 내용 직접 분석하기")
                }
            )
        ),
        RecoveryWindow(
            id = RecoveryWindowId.WITHIN_1_HOUR,
            displayOrder = 2,
            urgency = RecoveryUrgency.STABILIZE,
            label = "1시간 안",
            purpose = "무엇이 노출됐는지 정리하고 계정·자금 보호를 시작하는 시간입니다.",
            checklistSteps = listOf(
                "실제로 송금·정보 제공·앱 설치·원격 허용이 있었는지 침착하게 정리하세요.",
                "안전 흐름 길잡이에서 현재 상황과 가까운 항목을 직접 고르세요.",
                "이미 행동했다면 긴급 대응 센터에서 지급정지·계정 보호 절차를 시작하세요.",
                "새 번호나 링크 대신 평소 연락 방법으로 가족에게 상황을 알리세요."
            ),
            doNotDoNotes = listOf(
                "아직 확실하지 않은데 성급하게 기기를 초기화하거나 앱을 삭제하지 마세요.",
                "혼자 판단하지 말고 가족에게 상황을 먼저 공유하세요."
            ),
            primaryAction = RecoveryAction(RecoveryActionId.SAFETY_FLOW_ROUTER, "안전 흐름 길잡이에서 상황 고르기"),
            secondaryActions = listOf(
                RecoveryAction(RecoveryActionId.RESPONSE_CENTER, "이미 했다면 긴급 대응 센터"),
                RecoveryAction(RecoveryActionId.FAMILY_ALERT_MESSAGES, "가족에게 경고 메시지 보내기")
            )
        ),
        RecoveryWindow(
            id = RecoveryWindowId.WITHIN_TODAY,
            displayOrder = 3,
            urgency = RecoveryUrgency.CLEANUP,
            label = "오늘 안",
            purpose = "계정·기기를 정리하고 증거를 남기며 공식 도움을 확인하는 하루입니다.",
            checklistSteps = listOf(
                "계정·기기 잠금 체크리스트에서 은행·메신저·기기 보호 순서를 확인하세요.",
                "메시지·통화 기록과 거래 화면을 지우지 말고 증거 보존 체크리스트대로 남기세요.",
                "신고·상담 연락처에서 상황에 맞는 공식 창구와 말할 내용을 확인하세요.",
                "가족과 지인에게 사칭 위험을 알리는 경고 메시지를 공유하세요."
            ),
            doNotDoNotes = listOf(
                "확인한다는 이유로 의심 링크나 파일을 다시 열지 마세요.",
                "필요한 화면을 남기기 전에 메시지·앱·기기를 초기화하지 마세요."
            ),
            primaryAction = RecoveryAction(RecoveryActionId.ACCOUNT_LOCKDOWN, "계정·기기 잠금 체크리스트 열기"),
            secondaryActions = listOf(
                RecoveryAction(RecoveryActionId.EVIDENCE_KIT, "증거 보존 체크리스트"),
                RecoveryAction(RecoveryActionId.OFFICIAL_HELP_DIRECTORY, "신고·상담 연락처"),
                RecoveryAction(RecoveryActionId.FAMILY_ALERT_MESSAGES, "가족 경고 메시지 만들기"),
                if (hasAnalysis) {
                    RecoveryAction(RecoveryActionId.REPORT_PREP, "신고용 상담 준비 카드")
                } else {
                    RecoveryAction(RecoveryActionId.NEW_ANALYSIS, "새 의심 내용 분석")
                }
            )
        ),
        RecoveryWindow(
            id = RecoveryWindowId.WITHIN_WEEK,
            displayOrder = 4,
            urgency = RecoveryUrgency.PREVENT,
            label = "이번 주",
            purpose = "예방 습관을 다지고 가족 준비 상태를 점검하는 한 주입니다.",
            checklistSteps = listOf(
                "은행·카드·통신사 명세를 다시 확인해 낯선 거래나 변경이 없는지 점검하세요.",
                "가족 안전 계획과 준비도를 다시 확인하고 부족한 항목을 보완하세요.",
                "사건 타임라인 요약에서 최근 기록과 반복되는 신호를 함께 살펴보세요.",
                "개인정보 센터에서 남아 있는 로컬 데이터와 필요 없는 항목을 정리하세요."
            ),
            doNotDoNotes = listOf(
                "한 번 정리했다고 안심하지 말고 이후에도 낯선 요청은 다시 확인하세요.",
                "가족과 상의 없이 혼자 큰 결정을 내리지 마세요."
            ),
            primaryAction = RecoveryAction(RecoveryActionId.SAFETY_PLAN, "가족 안전 계획 점검"),
            secondaryActions = listOf(
                RecoveryAction(RecoveryActionId.TIMELINE_BRIEFING, "사건 타임라인 요약"),
                RecoveryAction(RecoveryActionId.PRIVACY_CENTER, "개인정보 센터")
            )
        )
    )

    private fun recommendWindowIds(analysis: ScamAnalysis?): List<RecoveryWindowId> {
        if (analysis == null) {
            return listOf(RecoveryWindowId.IMMEDIATE_15MIN, RecoveryWindowId.WITHIN_WEEK)
        }

        val signals = analysis.matchedSignals.toSet()
        val hasUrgentExposure = analysis.riskLevel == RiskLevel.HIGH ||
            signals.any { signal -> signal in urgentExposureSignals }
        val hasRemoteSignal = signals.any { signal -> signal in remoteSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasFamilySignal = signals.any { signal -> signal in familySignals }
        val includeHour = hasUrgentExposure || hasRemoteSignal || hasFamilySignal
        val includeToday = hasUrgentExposure || hasRemoteSignal || hasLinkSignal

        return listOfNotNull(
            RecoveryWindowId.IMMEDIATE_15MIN,
            RecoveryWindowId.WITHIN_1_HOUR.takeIf { includeHour },
            RecoveryWindowId.WITHIN_TODAY.takeIf { includeToday },
            RecoveryWindowId.WITHIN_WEEK
        )
    }

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness
    ): String {
        val readinessSuffix = " ${readinessMessage(readiness.level)}"
        if (analysis == null) {
            return "먼저 긴급 멈춤 모드로 지금을 안정시키고, 의심 내용을 새로 분석하거나 가족 콜백으로 확인한 뒤 이번 주 예방·점검을 이어가세요.$readinessSuffix"
        }

        val signals = analysis.matchedSignals.toSet()
        val hasUrgentExposure = analysis.riskLevel == RiskLevel.HIGH ||
            signals.any { signal -> signal in urgentExposureSignals }
        val hasRemoteSignal = signals.any { signal -> signal in remoteSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasFamilySignal = signals.any { signal -> signal in familySignals }
        val situationCopy = when {
            hasFamilySignal && (hasUrgentExposure || hasRemoteSignal) ->
                "가족·목소리·번호 변경 신호가 있어 1시간 안에 가족 콜백과 가족 경고를 먼저 확인하고, 이미 실행한 행동이 있다면 오늘 안에 계정 잠금·증거·공식 도움까지 이어가세요."
            hasFamilySignal ->
                "가족·목소리·번호 변경 신호가 있어 1시간 안에 가족 콜백 확인과 가족 경고 공유를 먼저 진행하세요."
            hasUrgentExposure ->
                "강한 송금·정보·앱 위험 신호가 있습니다. 1시간 안에 안전 흐름과 긴급 대응을 확인하고, 오늘 안에 계정·기기 잠금과 증거 보존, 공식 도움까지 진행하세요."
            hasRemoteSignal ->
                "원격제어·앱 설치 신호가 있어 1시간 안에 연결 차단과 긴급 대응을 확인하고, 오늘 안에 계정·기기 잠금과 증거 보존을 이어가세요."
            hasLinkSignal ->
                "링크·스미싱 신호가 있습니다. 링크를 다시 열지 말고 오늘 안에 증거 보존, 가족 경고, 공식 도움을 확인하세요."
            else ->
                "뚜렷한 위험 신호는 적지만, 지금 멈춤 대사를 다시 확인하고 이번 주 예방·점검을 이어가세요."
        }
        return situationCopy + readinessSuffix
    }

    private fun readinessMessage(level: ReadinessLevel): String = when (level) {
        ReadinessLevel.LOW ->
            "가족 콜백 준비가 아직 부족해도 괜찮습니다. 이번 주 안에 가족 안전 계획을 차분히 시작하세요."
        ReadinessLevel.IN_PROGRESS ->
            "가족 콜백 준비가 진행 중입니다. 이번 주 안에 빠진 안전 계획을 보완하세요."
        ReadinessLevel.READY ->
            "저장된 가족 콜백 루틴이 준비되어 있습니다. 이번 주 안에 안전 계획을 그대로 점검만 하세요."
    }

    private fun buildFamilyShareSummary(
        recommendationTitle: String,
        recommendedWindowIds: List<RecoveryWindowId>,
        windows: List<RecoveryWindow>
    ): String {
        val windowsById = windows.associateBy { window -> window.id }

        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 24시간 수습 계획")
            appendLine()
            appendLine(recommendationTitle)
            recommendedWindowIds.forEach { windowId ->
                val window = windowsById.getValue(windowId)
                appendLine("- ${window.label}: ${actionLabels(window)}")
            }
            appendLine()
            appendLine("전체 시간대")
            windows.forEach { window ->
                appendLine("- ${window.label}: ${actionLabels(window)}")
            }
            appendLine()
            appendLine("이 계획은 수동 안내이며 시간대별 체크 상태나 진행 상황을 저장하지 않습니다.")
            appendLine("앱은 연락처를 읽거나 받는 사람을 고르지 않고 메시지를 자동으로 보내지 않습니다.")
            append(
                "붙여넣은 원문·화면 단서·가족 안전 질문·콜백 내용·연락처 값과 전화·계좌·비밀번호·" +
                    "인증번호·PIN·링크·신분정보 값은 이 공유문에 포함하지 않았습니다."
            )
        }.trim()
    }

    private fun actionLabels(window: RecoveryWindow): String =
        (listOf(window.primaryAction) + window.secondaryActions)
            .joinToString(" / ") { action -> action.label }
}
