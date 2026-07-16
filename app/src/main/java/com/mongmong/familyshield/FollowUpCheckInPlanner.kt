package com.mongmong.familyshield

enum class FollowUpCheckpointId(val displayOrder: Int) {
    TONIGHT_STABILIZE(1),
    NEXT_MORNING_FINANCE_ACCOUNT(2),
    TWO_TO_THREE_DAYS_DEVICE_MESSENGER(3),
    THIS_WEEK_FAMILY_CALLBACK(4),
    THIS_WEEK_OFFICIAL_EVIDENCE(5),
    ONE_TO_TWO_WEEKS_STATEMENTS_ALERTS(6),
    THIS_MONTH_FAMILY_READINESS(7),
    WHEN_ANXIETY_RETURNS_STOP(8)
}

enum class FollowUpCheckpointCategory(val label: String) {
    STABILIZATION("연락 중단·안정"),
    FINANCE_ACCOUNT("금융·계정"),
    DEVICE_MESSENGER("기기·메신저"),
    FAMILY_VERIFICATION("가족 경고·콜백"),
    OFFICIAL_EVIDENCE("공식 접수·증거"),
    STATEMENT_PRIVACY("명세·로그인 알림"),
    FAMILY_READINESS("가족 준비"),
    UNCERTAINTY_STOP("불안 재발·다시 멈춤")
}

enum class FollowUpCheckpointPriority(val rank: Int, val label: String) {
    STOP_FIRST(0, "먼저 수동 확인"),
    NEXT_CHECK(1, "가까운 시점 직접 확인"),
    FOLLOW_UP(2, "며칠 뒤 직접 확인"),
    PREPARE(3, "가족과 준비")
}

enum class FollowUpCheckInActionId {
    QUICK_STOP,
    EXPOSURE_IMPACT_TRIAGE,
    TRUSTED_HELPER_REVIEW,
    CHECKER,
    ANALYSIS_RESULT,
    FAMILY_CALLBACK_CARD,
    SAFE_CALL_SCRIPTS,
    FAMILY_ALERT_MESSAGES,
    FAMILY_READINESS_ROADMAP,
    FAMILY_SAFETY_DRILL_COACH,
    SAFETY_PLAN,
    RECOVERY_PLAN,
    ACCOUNT_LOCKDOWN,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    INCIDENT_TIMELINE_BRIEFING,
    SAFETY_FLOW_ROUTER,
    PRIVACY_CENTER
}

data class FollowUpCheckInAction(
    val id: FollowUpCheckInActionId,
    val label: String
)

data class FollowUpCheckpoint(
    val id: FollowUpCheckpointId,
    val displayOrder: Int,
    val timeLabel: String,
    val category: FollowUpCheckpointCategory,
    val title: String,
    val purpose: String,
    val manualCheckSteps: List<String>,
    val whatNotToDo: List<String>,
    val priority: FollowUpCheckpointPriority,
    val primaryAction: FollowUpCheckInAction,
    val secondaryActions: List<FollowUpCheckInAction>
) {
    val routeActions: List<FollowUpCheckInAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class FollowUpCheckInPlan(
    val title: String,
    val intro: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedCheckpointIds: List<FollowUpCheckpointId>,
    val recommendedActions: List<FollowUpCheckInAction>,
    val checkpoints: List<FollowUpCheckpoint>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedCheckpoints: List<FollowUpCheckpoint>
        get() = recommendedCheckpointIds.mapNotNull { checkpointId ->
            checkpoints.find { checkpoint -> checkpoint.id == checkpointId }
        }

    val recommendedActionIds: List<FollowUpCheckInActionId>
        get() = recommendedActions.map { action -> action.id }
}

object FollowUpCheckInPlannerBuilder {
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

    private val deviceSignals = setOf(
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

    private val pressureSignals = setOf(
        ScamSignal.SECRECY,
        ScamSignal.IMPERSONATION
    )

    fun build(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness,
        incidentCount: Int = 0
    ): FollowUpCheckInPlan {
        val signals = analysis?.matchedSignals.orEmpty().toSet()
        val hasDefaultState = analysis == null || signals.isEmpty()
        val hasFinancialSignal = analysis?.riskLevel == RiskLevel.HIGH ||
            signals.any { signal -> signal in financialSignals }
        val hasDeviceSignal = signals.any { signal -> signal in deviceSignals }
        val hasLinkSignal = signals.any { signal -> signal in linkSignals }
        val hasFamilySignal = signals.any { signal -> signal in familySignals }
        val hasPressureSignal = signals.any { signal -> signal in pressureSignals }
        val hasSavedIncident = incidentCount > 0

        val recommendedCheckpointSet = linkedSetOf<FollowUpCheckpointId>()
        if (hasDefaultState) {
            recommendedCheckpointSet += FollowUpCheckpointId.TONIGHT_STABILIZE
            recommendedCheckpointSet += FollowUpCheckpointId.THIS_WEEK_FAMILY_CALLBACK
            recommendedCheckpointSet += FollowUpCheckpointId.THIS_MONTH_FAMILY_READINESS
            recommendedCheckpointSet += FollowUpCheckpointId.WHEN_ANXIETY_RETURNS_STOP
        }
        if (hasFinancialSignal) {
            recommendedCheckpointSet += FollowUpCheckpointId.NEXT_MORNING_FINANCE_ACCOUNT
            recommendedCheckpointSet += FollowUpCheckpointId.THIS_WEEK_OFFICIAL_EVIDENCE
            recommendedCheckpointSet += FollowUpCheckpointId.ONE_TO_TWO_WEEKS_STATEMENTS_ALERTS
        }
        if (hasDeviceSignal) {
            recommendedCheckpointSet += FollowUpCheckpointId.TWO_TO_THREE_DAYS_DEVICE_MESSENGER
        }
        if (hasLinkSignal) {
            recommendedCheckpointSet += FollowUpCheckpointId.TWO_TO_THREE_DAYS_DEVICE_MESSENGER
            recommendedCheckpointSet += FollowUpCheckpointId.THIS_WEEK_FAMILY_CALLBACK
            recommendedCheckpointSet += FollowUpCheckpointId.ONE_TO_TWO_WEEKS_STATEMENTS_ALERTS
        }
        if (hasFamilySignal) {
            recommendedCheckpointSet += FollowUpCheckpointId.THIS_WEEK_FAMILY_CALLBACK
        }
        if (hasPressureSignal) {
            recommendedCheckpointSet += FollowUpCheckpointId.TONIGHT_STABILIZE
            recommendedCheckpointSet += FollowUpCheckpointId.THIS_WEEK_OFFICIAL_EVIDENCE
        }
        if (hasSavedIncident) {
            recommendedCheckpointSet += FollowUpCheckpointId.THIS_WEEK_OFFICIAL_EVIDENCE
        }
        if (readiness.level == ReadinessLevel.LOW) {
            recommendedCheckpointSet += FollowUpCheckpointId.THIS_MONTH_FAMILY_READINESS
        }
        if (recommendedCheckpointSet.isEmpty()) {
            recommendedCheckpointSet += FollowUpCheckpointId.TONIGHT_STABILIZE
            recommendedCheckpointSet += FollowUpCheckpointId.WHEN_ANXIETY_RETURNS_STOP
        }
        val recommendedCheckpointIds = FollowUpCheckpointId.entries.filter { checkpointId ->
            checkpointId in recommendedCheckpointSet
        }

        val recommendedActionSet = linkedSetOf<FollowUpCheckInActionId>()
        if (hasDefaultState) {
            recommendedActionSet += FollowUpCheckInActionId.QUICK_STOP
            recommendedActionSet += FollowUpCheckInActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += FollowUpCheckInActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += if (analysis == null) {
                FollowUpCheckInActionId.CHECKER
            } else {
                FollowUpCheckInActionId.ANALYSIS_RESULT
            }
            recommendedActionSet += FollowUpCheckInActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += FollowUpCheckInActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += FollowUpCheckInActionId.FAMILY_READINESS_ROADMAP
        }
        if (hasFinancialSignal) {
            recommendedActionSet += FollowUpCheckInActionId.RECOVERY_PLAN
            recommendedActionSet += FollowUpCheckInActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += FollowUpCheckInActionId.EVIDENCE_KIT
            recommendedActionSet += FollowUpCheckInActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += FollowUpCheckInActionId.REPORT_PREP
            recommendedActionSet += FollowUpCheckInActionId.PRIVACY_CENTER
        }
        if (hasDeviceSignal) {
            recommendedActionSet += FollowUpCheckInActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += FollowUpCheckInActionId.RECOVERY_PLAN
            recommendedActionSet += FollowUpCheckInActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += FollowUpCheckInActionId.EVIDENCE_KIT
            recommendedActionSet += FollowUpCheckInActionId.PRIVACY_CENTER
        }
        if (hasLinkSignal) {
            recommendedActionSet += FollowUpCheckInActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += FollowUpCheckInActionId.FAMILY_ALERT_MESSAGES
            recommendedActionSet += FollowUpCheckInActionId.ACCOUNT_LOCKDOWN
            recommendedActionSet += FollowUpCheckInActionId.EVIDENCE_KIT
            recommendedActionSet += FollowUpCheckInActionId.PRIVACY_CENTER
        }
        if (hasFamilySignal) {
            recommendedActionSet += FollowUpCheckInActionId.FAMILY_CALLBACK_CARD
            recommendedActionSet += FollowUpCheckInActionId.SAFE_CALL_SCRIPTS
            recommendedActionSet += FollowUpCheckInActionId.FAMILY_ALERT_MESSAGES
        }
        if (hasPressureSignal) {
            recommendedActionSet += FollowUpCheckInActionId.QUICK_STOP
            recommendedActionSet += FollowUpCheckInActionId.TRUSTED_HELPER_REVIEW
            recommendedActionSet += FollowUpCheckInActionId.OFFICIAL_HELP_DIRECTORY
        }
        if (hasSavedIncident) {
            recommendedActionSet += FollowUpCheckInActionId.EVIDENCE_KIT
            recommendedActionSet += FollowUpCheckInActionId.OFFICIAL_HELP_DIRECTORY
            recommendedActionSet += FollowUpCheckInActionId.REPORT_PREP
            recommendedActionSet += FollowUpCheckInActionId.INCIDENT_TIMELINE_BRIEFING
        }
        if (readiness.level == ReadinessLevel.LOW) {
            recommendedActionSet += FollowUpCheckInActionId.FAMILY_READINESS_ROADMAP
            recommendedActionSet += FollowUpCheckInActionId.FAMILY_SAFETY_DRILL_COACH
            recommendedActionSet += FollowUpCheckInActionId.SAFETY_PLAN
        }
        if (recommendedActionSet.isEmpty()) {
            recommendedActionSet += FollowUpCheckInActionId.QUICK_STOP
            recommendedActionSet += FollowUpCheckInActionId.EXPOSURE_IMPACT_TRIAGE
            recommendedActionSet += FollowUpCheckInActionId.TRUSTED_HELPER_REVIEW
        }
        val recommendedActions = FollowUpCheckInActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val checkpoints = buildCheckpoints(hasAnalysis = analysis != null)
        val recommendationTitle = when {
            analysis == null -> "분석 전 기본 후속 점검"
            hasFinancialSignal || hasDeviceSignal -> "현재 신호에서 먼저 볼 후속 점검"
            signals.isEmpty() -> "일치 신호가 없어도 이어갈 후속 점검"
            else -> "현재 신호에 맞춘 수동 후속 점검"
        }

        return FollowUpCheckInPlan(
            title = "며칠 뒤 다시 확인할 것",
            intro =
                "의심 연락이나 가능한 노출 뒤 가족이 정한 시점에 직접 다시 살펴보는 로컬·무상태 안내입니다. " +
                    "앱이 계정·기기·메시지를 지켜보거나 후속 일정을 예약하지 않으며, 체크 결과도 저장하지 않습니다.",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                analysis = analysis,
                readinessLevel = readiness.level,
                signals = signals,
                hasFinancialSignal = hasFinancialSignal,
                hasDeviceSignal = hasDeviceSignal,
                hasLinkSignal = hasLinkSignal,
                hasFamilySignal = hasFamilySignal,
                hasPressureSignal = hasPressureSignal,
                hasSavedIncident = hasSavedIncident
            ),
            recommendedCheckpointIds = recommendedCheckpointIds,
            recommendedActions = recommendedActions,
            checkpoints = checkpoints,
            privacySafetyNotes = privacySafetyNotes(),
            familyShareSummary = buildFamilyShareSummary(
                recommendedCheckpointIds = recommendedCheckpointIds,
                recommendedActions = recommendedActions,
                checkpoints = checkpoints
            )
        )
    }

    private fun buildCheckpoints(hasAnalysis: Boolean): List<FollowUpCheckpoint> {
        fun action(id: FollowUpCheckInActionId): FollowUpCheckInAction = actionFor(id)
        fun currentResultAction(): FollowUpCheckInAction = action(
            if (hasAnalysis) FollowUpCheckInActionId.ANALYSIS_RESULT else FollowUpCheckInActionId.CHECKER
        )

        return listOf(
            FollowUpCheckpoint(
                id = FollowUpCheckpointId.TONIGHT_STABILIZE,
                displayOrder = FollowUpCheckpointId.TONIGHT_STABILIZE.displayOrder,
                timeLabel = "오늘 밤",
                category = FollowUpCheckpointCategory.STABILIZATION,
                title = "오늘 밤 다시 안정시키기",
                purpose = "추가 연락과 행동을 멈춘 상태를 가족이 직접 다시 확인하고 마음을 가라앉힙니다.",
                manualCheckSteps = listOf(
                    "의심 연락에 더 답하지 않았는지 가족과 직접 확인합니다.",
                    "추가 송금·정보 제공·앱 행동을 보류한 채 긴급 멈춤, 노출 영향, 보호자 검토 순서를 함께 읽습니다."
                ),
                whatNotToDo = listOf(
                    "상대가 다시 재촉해도 같은 연락 수단으로 해명하거나 협상하지 마세요.",
                    "잠잠해졌다는 이유만으로 상황이 끝났다고 단정하지 마세요."
                ),
                priority = FollowUpCheckpointPriority.STOP_FIRST,
                primaryAction = action(FollowUpCheckInActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(FollowUpCheckInActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(FollowUpCheckInActionId.TRUSTED_HELPER_REVIEW),
                    currentResultAction(),
                    action(FollowUpCheckInActionId.RECOVERY_PLAN)
                )
            ),
            FollowUpCheckpoint(
                id = FollowUpCheckpointId.NEXT_MORNING_FINANCE_ACCOUNT,
                displayOrder = FollowUpCheckpointId.NEXT_MORNING_FINANCE_ACCOUNT.displayOrder,
                timeLabel = "내일 오전",
                category = FollowUpCheckpointCategory.FINANCE_ACCOUNT,
                title = "내일 오전 금융·계정 확인",
                purpose = "은행·카드·주요 계정의 활동을 공식 경로에서 가족이 직접 다시 확인합니다.",
                manualCheckSteps = listOf(
                    "평소 쓰던 공식 앱이나 직접 찾은 대표 경로에서 거래·결제 활동과 로그인 알림을 확인합니다.",
                    "모르는 변경이나 정보 제공 가능성이 있으면 계정 보호 목록과 공식 도움 준비를 함께 봅니다."
                ),
                whatNotToDo = listOf(
                    "의심 연락이 준 링크·번호·앱으로 금융기관이나 계정에 접속하지 마세요.",
                    "가족에게도 비밀번호·인증번호·PIN 같은 실제 비밀값을 보여주지 마세요."
                ),
                priority = FollowUpCheckpointPriority.NEXT_CHECK,
                primaryAction = action(FollowUpCheckInActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(FollowUpCheckInActionId.OFFICIAL_HELP_DIRECTORY),
                    action(FollowUpCheckInActionId.RECOVERY_PLAN),
                    action(FollowUpCheckInActionId.PRIVACY_CENTER)
                )
            ),
            FollowUpCheckpoint(
                id = FollowUpCheckpointId.TWO_TO_THREE_DAYS_DEVICE_MESSENGER,
                displayOrder = FollowUpCheckpointId.TWO_TO_THREE_DAYS_DEVICE_MESSENGER.displayOrder,
                timeLabel = "2~3일 뒤",
                category = FollowUpCheckpointCategory.DEVICE_MESSENGER,
                title = "2~3일 기기·메신저 관찰",
                purpose = "설치 앱·원격 접근 설정·메신저 세션의 낯선 변화를 가족이 기기에서 직접 살펴봅니다.",
                manualCheckSteps = listOf(
                    "기기 설정에서 최근 설치 앱과 원격·화면공유 관련 접근 권한을 직접 확인합니다.",
                    "메신저와 주요 계정의 로그인 기기·세션 목록에서 모르는 변화를 직접 확인합니다."
                ),
                whatNotToDo = listOf(
                    "이 화면이 기기나 링크를 검사했다고 여기거나 변화가 없다고 단정하지 마세요.",
                    "확인이 필요할 수 있는 기록을 서둘러 지우거나 출처 불명 정리 앱을 추가 설치하지 마세요."
                ),
                priority = FollowUpCheckpointPriority.FOLLOW_UP,
                primaryAction = action(FollowUpCheckInActionId.ACCOUNT_LOCKDOWN),
                secondaryActions = listOf(
                    action(FollowUpCheckInActionId.EVIDENCE_KIT),
                    action(FollowUpCheckInActionId.TRUSTED_HELPER_REVIEW),
                    action(FollowUpCheckInActionId.PRIVACY_CENTER)
                )
            ),
            FollowUpCheckpoint(
                id = FollowUpCheckpointId.THIS_WEEK_FAMILY_CALLBACK,
                displayOrder = FollowUpCheckpointId.THIS_WEEK_FAMILY_CALLBACK.displayOrder,
                timeLabel = "이번 주",
                category = FollowUpCheckpointCategory.FAMILY_VERIFICATION,
                title = "이번 주 가족 경고·콜백 점검",
                purpose = "가족이 평소 알고 있던 별도 경로로 다시 확인하고 일반 경고와 콜백 습관을 맞춥니다.",
                manualCheckSteps = listOf(
                    "새로 받은 번호가 아니라 평소 알고 있던 가족 경로로 직접 콜백하는 순서를 확인합니다.",
                    "사기 원문이나 비밀값 없이 일반 경고 문구와 확인 통화 순서를 가족과 연습합니다."
                ),
                whatNotToDo = listOf(
                    "목소리나 프로필만으로 가족 본인이라고 단정하지 마세요.",
                    "안전 질문의 답이나 정확한 연락처 값을 가족 공유 요약에 넣지 마세요."
                ),
                priority = FollowUpCheckpointPriority.FOLLOW_UP,
                primaryAction = action(FollowUpCheckInActionId.FAMILY_CALLBACK_CARD),
                secondaryActions = listOf(
                    action(FollowUpCheckInActionId.SAFE_CALL_SCRIPTS),
                    action(FollowUpCheckInActionId.FAMILY_ALERT_MESSAGES),
                    action(FollowUpCheckInActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            FollowUpCheckpoint(
                id = FollowUpCheckpointId.THIS_WEEK_OFFICIAL_EVIDENCE,
                displayOrder = FollowUpCheckpointId.THIS_WEEK_OFFICIAL_EVIDENCE.displayOrder,
                timeLabel = "이번 주",
                category = FollowUpCheckpointCategory.OFFICIAL_EVIDENCE,
                title = "이번 주 공식 접수·증거 정리",
                purpose = "필요한 공식 문의와 접수에 대비해 시간·행동·증거 종류를 가족이 직접 정리합니다.",
                manualCheckSteps = listOf(
                    "공식 도움 경로를 직접 확인하고, 어떤 행동과 자료를 준비할지 종류만 정리합니다.",
                    "증거 보존, 접수 준비, 사건 타임라인 화면을 열어 필요한 항목을 수동으로 대조합니다."
                ),
                whatNotToDo = listOf(
                    "사칭 상대에게 접수 여부를 알리거나 증거 원본을 다시 보내지 마세요.",
                    "접수만으로 피해 회복이나 법적 결과가 정해진다고 여기지 마세요."
                ),
                priority = FollowUpCheckpointPriority.FOLLOW_UP,
                primaryAction = action(FollowUpCheckInActionId.OFFICIAL_HELP_DIRECTORY),
                secondaryActions = listOf(
                    action(FollowUpCheckInActionId.REPORT_PREP),
                    action(FollowUpCheckInActionId.EVIDENCE_KIT),
                    action(FollowUpCheckInActionId.INCIDENT_TIMELINE_BRIEFING)
                )
            ),
            FollowUpCheckpoint(
                id = FollowUpCheckpointId.ONE_TO_TWO_WEEKS_STATEMENTS_ALERTS,
                displayOrder = FollowUpCheckpointId.ONE_TO_TWO_WEEKS_STATEMENTS_ALERTS.displayOrder,
                timeLabel = "1~2주 뒤",
                category = FollowUpCheckpointCategory.STATEMENT_PRIVACY,
                title = "1~2주 명세·알림 재확인",
                purpose = "뒤늦게 보일 수 있는 명세·로그인 알림·모르는 변경을 공식 화면에서 직접 확인합니다.",
                manualCheckSteps = listOf(
                    "은행·카드·주요 계정의 공식 화면에서 명세와 로그인 알림을 직접 다시 확인합니다.",
                    "모르는 결제·로그인·연락처·복구 설정 변경이 보이면 공식 지원 경로와 계정 보호 목록을 다시 엽니다."
                ),
                whatNotToDo = listOf(
                    "알림에 포함된 링크를 바로 누르거나 발신 표시만 믿지 마세요.",
                    "이 앱이 계정 활동을 대신 확인했다고 여기지 마세요."
                ),
                priority = FollowUpCheckpointPriority.FOLLOW_UP,
                primaryAction = action(FollowUpCheckInActionId.PRIVACY_CENTER),
                secondaryActions = listOf(
                    action(FollowUpCheckInActionId.ACCOUNT_LOCKDOWN),
                    action(FollowUpCheckInActionId.OFFICIAL_HELP_DIRECTORY),
                    action(FollowUpCheckInActionId.EVIDENCE_KIT)
                )
            ),
            FollowUpCheckpoint(
                id = FollowUpCheckpointId.THIS_MONTH_FAMILY_READINESS,
                displayOrder = FollowUpCheckpointId.THIS_MONTH_FAMILY_READINESS.displayOrder,
                timeLabel = "이번 달",
                category = FollowUpCheckpointCategory.FAMILY_READINESS,
                title = "이번 달 가족 준비 보강",
                purpose = "이번 일을 탓하지 않고 가족의 멈춤·콜백·도움 요청 준비를 조금씩 보완합니다.",
                manualCheckSteps = listOf(
                    "가족 준비 로드맵에서 비어 있는 항목을 한 가지 골라 직접 보완합니다.",
                    "가족 안전 연습 코치로 멈춤 문구와 별도 콜백 순서를 함께 연습합니다."
                ),
                whatNotToDo = listOf(
                    "한 번에 모든 준비를 끝내려 하거나 가족을 탓하지 마세요.",
                    "안전 질문 답·콜백 연락처·비밀값을 공유 요약이나 연습 메모에 적지 마세요."
                ),
                priority = FollowUpCheckpointPriority.PREPARE,
                primaryAction = action(FollowUpCheckInActionId.FAMILY_READINESS_ROADMAP),
                secondaryActions = listOf(
                    action(FollowUpCheckInActionId.FAMILY_SAFETY_DRILL_COACH),
                    action(FollowUpCheckInActionId.SAFETY_PLAN),
                    action(FollowUpCheckInActionId.FAMILY_CALLBACK_CARD),
                    action(FollowUpCheckInActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            FollowUpCheckpoint(
                id = FollowUpCheckpointId.WHEN_ANXIETY_RETURNS_STOP,
                displayOrder = FollowUpCheckpointId.WHEN_ANXIETY_RETURNS_STOP.displayOrder,
                timeLabel = "불안이 다시 들 때",
                category = FollowUpCheckpointCategory.UNCERTAINTY_STOP,
                title = "불안 재발 시 다시 멈춤",
                purpose = "새 연락이나 뒤늦은 걱정이 생기면 혼자 결론내리지 않고 멈춤과 노출 분류로 돌아갑니다.",
                manualCheckSteps = listOf(
                    "새 요구에 답하거나 행동하기 전에 긴급 멈춤 순서를 다시 읽습니다.",
                    "실제로 한 행동의 종류만 노출 영향 화면에서 고르고 믿을 만한 보호자와 함께 다음 경로를 정합니다."
                ),
                whatNotToDo = listOf(
                    "불안하다는 이유로 사칭 상대에게 다시 연락하거나 비밀값을 확인해 주지 마세요.",
                    "혼자 감당하거나 이미 늦었다고 자책하지 마세요."
                ),
                priority = FollowUpCheckpointPriority.STOP_FIRST,
                primaryAction = action(FollowUpCheckInActionId.QUICK_STOP),
                secondaryActions = listOf(
                    action(FollowUpCheckInActionId.EXPOSURE_IMPACT_TRIAGE),
                    action(FollowUpCheckInActionId.TRUSTED_HELPER_REVIEW),
                    currentResultAction()
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readinessLevel: ReadinessLevel,
        signals: Set<ScamSignal>,
        hasFinancialSignal: Boolean,
        hasDeviceSignal: Boolean,
        hasLinkSignal: Boolean,
        hasFamilySignal: Boolean,
        hasPressureSignal: Boolean,
        hasSavedIncident: Boolean
    ): String {
        val situationCopy = buildList {
            if (analysis == null || signals.isEmpty()) {
                add(
                    "현재 분석이 없거나 일치 신호가 없습니다. 이것만으로 상황이 끝났다고 단정하지 않고, " +
                        "오늘 밤 멈춤·이번 주 가족 콜백·이번 달 준비·불안할 때 다시 멈춤을 수동으로 확인하세요."
                )
            }
            if (hasFinancialSignal) {
                add("금융·계정 관련 신호가 있어 내일 오전 활동, 이번 주 공식 도움·증거, 이후 명세를 공식 경로에서 직접 확인하세요.")
            }
            if (hasDeviceSignal) {
                add("앱·원격·화면공유 관련 신호가 있어 며칠 뒤 기기 설정과 계정 세션을 직접 살펴보고 보호자와 함께 확인하세요.")
            }
            if (hasLinkSignal) {
                add("링크·스미싱 관련 신호가 있어 자동 검사에 기대지 말고 기기·계정 변화, 가족 경고, 개인정보 경로를 직접 확인하세요.")
            }
            if (hasFamilySignal) {
                add("가족·딥페이크·번호 변경 관련 신호가 있어 평소 알던 별도 경로의 콜백과 확인 통화를 이번 주에 다시 맞춰보세요.")
            }
            if (hasPressureSignal) {
                add("비밀 유지·기관 사칭 압박 신호가 있어 오늘 다시 멈추고 보호자 검토와 공식 도움 경로를 함께 확인하세요.")
            }
            if (hasSavedIncident) {
                add("증거·접수 준비·사건 타임라인 경로도 이번 주 수동 점검에 포함하세요.")
            }
            if (isEmpty()) {
                add("현재 신호만으로 사실을 확정하지 않고 멈춤과 직접 확인을 이어가세요.")
            }
        }.joinToString(" ")

        val readinessCopy = when (readinessLevel) {
            ReadinessLevel.LOW ->
                "가족 준비가 아직 부족해도 괜찮습니다. 이번 달 로드맵과 연습 코치부터 차분히 시작하세요."
            ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중입니다. 마련된 콜백 경로를 활용하고 빈 항목은 차분히 보완하세요."
            ReadinessLevel.READY ->
                "가족 준비가 되어 있어도 이번 상황의 사실이 확인된 것은 아닙니다. 저장된 루틴을 차분히 직접 다시 점검하세요."
        }
        return "$situationCopy $readinessCopy"
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 화면은 사용자가 직접 여는 수동 후속 안내이며 현재 위험도·일치 신호, 값 없는 가족 준비 수준, 저장 사건의 유무만 기기 안의 로컬 규칙으로 사용합니다.",
        "점검 시각은 참고용 문구입니다. 알림·알람·캘린더 일정·리마인더를 만들거나 예약하지 않고 백그라운드 작업을 실행하지 않습니다.",
        "선택한 점검, 답변·메모, 완료·체크 상태, 점수, 읽음·진행 상태, 공유 이력, 후속 점검 이력을 저장하지 않습니다.",
        "화면을 열거나 경로 버튼을 눌러도 사건 기록을 자동으로 추가·수정·삭제하지 않습니다.",
        "문자·메시지·통화·연락처를 읽거나 감시하지 않고 계정·기기·가족을 자동 모니터링하지 않습니다.",
        "공유는 연락처를 읽거나 대상을 고르지 않으며 사용자가 공유 앱과 대상을 직접 선택하기 전에는 아무것도 자동 전송하지 않습니다.",
        "네트워크를 사용하거나 새 Android 권한을 요청하지 않으며 전화·문자를 자동 실행하거나 차단하지 않습니다.",
        "이 안내는 링크·기기·계정을 검사하지 않으며 사기·노출·안전 여부를 확정하거나 증명하지 않습니다.",
        "악성 앱 제거, 계정·카드 조치, 피해금 회복, 법률 자문이나 결과를 수행·보장하지 않으며 데이터를 자동 삭제하지 않습니다.",
        "사기 원문, 가린 단서, 안전 질문 답, 콜백·연락처 값, 전화·계좌·인증·PIN 값, 링크·도메인은 이 결과와 가족 공유 요약에 사용하지 않습니다. 실제 조치는 공식 경로에서 사용자가 직접 진행합니다."
    )

    private fun buildFamilyShareSummary(
        recommendedCheckpointIds: List<FollowUpCheckpointId>,
        recommendedActions: List<FollowUpCheckInAction>,
        checkpoints: List<FollowUpCheckpoint>
    ): String {
        val checkpointsById = checkpoints.associateBy { checkpoint -> checkpoint.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 수동 후속 점검 요약")
            appendLine()
            appendLine("먼저 직접 확인할 시점")
            recommendedCheckpointIds.forEach { checkpointId ->
                val checkpoint = checkpointsById.getValue(checkpointId)
                appendLine("- [${checkpoint.timeLabel} · ${checkpoint.category.label}] ${checkpoint.title}")
            }
            appendLine()
            appendLine("먼저 열어볼 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("전체 수동 점검 순서")
            checkpoints.forEach { checkpoint ->
                appendLine("- [${checkpoint.timeLabel} · ${checkpoint.category.label}] ${checkpoint.title}")
                checkpoint.manualCheckSteps.forEach { step -> appendLine("  · $step") }
                appendLine("  · 앱 경로: ${checkpoint.routeActions.joinToString(" / ") { action -> action.label }}")
            }
            appendLine()
            append("가족이 각 시점에 직접 확인하는 일반 안내이며, 사기 원문·비밀값·연락처 값·링크는 포함하지 않았습니다.")
        }
    }

    private fun actionFor(id: FollowUpCheckInActionId): FollowUpCheckInAction =
        FollowUpCheckInAction(
            id = id,
            label = when (id) {
                FollowUpCheckInActionId.QUICK_STOP -> "긴급 멈춤 모드"
                FollowUpCheckInActionId.EXPOSURE_IMPACT_TRIAGE -> "무엇을 했는지 종류만 분류하기"
                FollowUpCheckInActionId.TRUSTED_HELPER_REVIEW -> "보호자와 함께 안전 검토"
                FollowUpCheckInActionId.CHECKER -> "의심 내용 확인기"
                FollowUpCheckInActionId.ANALYSIS_RESULT -> "현재 분석 결과"
                FollowUpCheckInActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 확인 카드"
                FollowUpCheckInActionId.SAFE_CALL_SCRIPTS -> "안전 확인 전화 스크립트"
                FollowUpCheckInActionId.FAMILY_ALERT_MESSAGES -> "가족 경고 메시지 팩"
                FollowUpCheckInActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
                FollowUpCheckInActionId.FAMILY_SAFETY_DRILL_COACH -> "가족 안전 연습 코치"
                FollowUpCheckInActionId.SAFETY_PLAN -> "가족 안전 계획"
                FollowUpCheckInActionId.RECOVERY_PLAN -> "24시간 수습 계획"
                FollowUpCheckInActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크리스트"
                FollowUpCheckInActionId.EVIDENCE_KIT -> "증거 보존 체크리스트"
                FollowUpCheckInActionId.OFFICIAL_HELP_DIRECTORY -> "신고·상담 연락처 보기"
                FollowUpCheckInActionId.REPORT_PREP -> "신고용 상담 준비 카드"
                FollowUpCheckInActionId.INCIDENT_TIMELINE_BRIEFING -> "사건 타임라인 요약"
                FollowUpCheckInActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
                FollowUpCheckInActionId.PRIVACY_CENTER -> "개인정보 센터"
            }
        )
}
