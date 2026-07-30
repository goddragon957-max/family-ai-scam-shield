package com.mongmong.familyshield

enum class PostIncidentReviewAnchorId(val displayOrder: Int, val dayOffset: Int) {
    DAY_0_WRAP_UP_WHAT_JUST_HAPPENED(1, 0),
    DAY_3_EMOTION_AND_IMMEDIATE_RECHECK(2, 3),
    DAY_7_WORD_ACCOUNT_NUMBER_CHANGES(3, 7),
    DAY_14_RECONTACT_OR_NEW_ATTEMPT(4, 14),
    DAY_30_FAMILY_SAFETY_PLAN_REFRESH(5, 30),
    DAY_90_NEW_FAMILY_LONG_TERM_CARE_CHANGE(6, 90),
    DAY_180_ID_ACCOUNT_SECURITY_SWEEP(7, 180),
    DAY_365_FULL_FAMILY_SAFETY_PLAN_REVIEW(8, 365)
}

enum class PostIncidentReviewEmphasis(val label: String) {
    EMPHASIZED("먼저 표시할 시점"),
    NORMAL("차례로 표시할 시점"),
    DIMMED("나중에 표시할 시점")
}

enum class PostIncidentReviewActionId {
    ONBOARDING,
    FOLLOW_UP_CHECK_IN_PLANNER,
    FAMILY_CALM_DEBRIEF_AGENDA,
    SAFETY_PLAN,
    RECONTACT_BOUNDARY_COACH,
    ACTIVE_CONTACT_EXIT_SCRIPTS,
    FAMILY_READINESS_ROADMAP,
    FAMILY_CALLBACK_CARD,
    FAMILY_SAFETY_DRILL_COACH,
    ACCOUNT_LOCKDOWN,
    EVIDENCE_KIT,
    INCIDENT_TIMELINE_BRIEFING,
    TRUSTED_HELPER_REVIEW,
    OFFICIAL_HELP_DIRECTORY,
    PRIVACY_CENTER,
    SAFETY_FLOW_ROUTER,
    HOME
}

data class PostIncidentReviewAction(
    val id: PostIncidentReviewActionId,
    val label: String
)

data class PostIncidentReviewAnchor(
    val id: PostIncidentReviewAnchorId,
    val displayOrder: Int,
    val dayOffset: Int,
    val dayLabel: String,
    val title: String,
    val whyItMatters: String,
    val manualRecheckItems: List<String>,
    val familyDiscussionQuestions: List<String>,
    val markInOwnCalendarInstruction: String,
    val emphasis: PostIncidentReviewEmphasis,
    val inAppViewLabel: String,
    val primaryAction: PostIncidentReviewAction,
    val secondaryActions: List<PostIncidentReviewAction>
) {
    val routeActions: List<PostIncidentReviewAction>
        get() = listOf(primaryAction) + secondaryActions
}

data class PostIncidentReviewCalendar(
    val title: String,
    val intro: String,
    val modeLabel: String,
    val manualCalendarNote: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val calendarSkipped: Boolean,
    val firstTimeMessage: String?,
    val emphasizedAnchorId: PostIncidentReviewAnchorId?,
    val recommendedAnchorIds: List<PostIncidentReviewAnchorId>,
    val recommendedActions: List<PostIncidentReviewAction>,
    val anchors: List<PostIncidentReviewAnchor>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedAnchors: List<PostIncidentReviewAnchor>
        get() = recommendedAnchorIds.mapNotNull { anchorId ->
            anchors.find { anchor -> anchor.id == anchorId }
        }

    val recommendedActionIds: List<PostIncidentReviewActionId>
        get() = recommendedActions.map { action -> action.id }
}

object PostIncidentReviewCalendarBuilder {
    private val earlyAnchorIds = listOf(
        PostIncidentReviewAnchorId.DAY_0_WRAP_UP_WHAT_JUST_HAPPENED,
        PostIncidentReviewAnchorId.DAY_3_EMOTION_AND_IMMEDIATE_RECHECK,
        PostIncidentReviewAnchorId.DAY_7_WORD_ACCOUNT_NUMBER_CHANGES
    )

    private val midAnchorIds = earlyAnchorIds + listOf(
        PostIncidentReviewAnchorId.DAY_14_RECONTACT_OR_NEW_ATTEMPT,
        PostIncidentReviewAnchorId.DAY_30_FAMILY_SAFETY_PLAN_REFRESH
    )

    fun build(
        consent: Boolean,
        readiness: FamilyReadiness,
        incidentCount: Int = 0,
        lastIncidentDaysAgo: Int? = null
    ): PostIncidentReviewCalendar {
        val readinessLevel = readiness.level
        val safeIncidentCount = incidentCount.coerceAtLeast(0)
        val safeDaysAgo = lastIncidentDaysAgo?.coerceAtLeast(0)
        val calendarSkipped = safeIncidentCount == 0
        val nearAnchorId = if (calendarSkipped) null else nearestAnchorId(safeDaysAgo)
        val nextAnchorId = nearAnchorId?.let(::anchorAfter)

        val emphasisByAnchorId = buildEmphasis(
            calendarSkipped = calendarSkipped,
            nearAnchorId = nearAnchorId,
            nextAnchorId = nextAnchorId,
            readinessLevel = readinessLevel
        )

        val recommendedAnchorSet = linkedSetOf<PostIncidentReviewAnchorId>()
        if (!calendarSkipped) {
            if (nearAnchorId != null) {
                recommendedAnchorSet += nearAnchorId
                if (nextAnchorId != null) recommendedAnchorSet += nextAnchorId
            }
            when (readinessLevel) {
                ReadinessLevel.LOW -> recommendedAnchorSet += earlyAnchorIds
                ReadinessLevel.IN_PROGRESS -> recommendedAnchorSet += midAnchorIds
                ReadinessLevel.READY ->
                    recommendedAnchorSet += PostIncidentReviewAnchorId.entries
            }
        }
        val recommendedAnchorIds = PostIncidentReviewAnchorId.entries.filter { anchorId ->
            anchorId in recommendedAnchorSet
        }

        val recommendedActionSet = linkedSetOf<PostIncidentReviewActionId>()
        if (!consent) {
            recommendedActionSet += PostIncidentReviewActionId.ONBOARDING
        }
        if (calendarSkipped) {
            recommendedActionSet += PostIncidentReviewActionId.SAFETY_PLAN
            recommendedActionSet += PostIncidentReviewActionId.FAMILY_READINESS_ROADMAP
            recommendedActionSet += PostIncidentReviewActionId.FOLLOW_UP_CHECK_IN_PLANNER
        } else {
            recommendedAnchorIds.forEach { anchorId ->
                recommendedActionSet += primaryActionIdFor(anchorId)
            }
            when (readinessLevel) {
                ReadinessLevel.LOW -> {
                    recommendedActionSet += PostIncidentReviewActionId.SAFETY_PLAN
                    recommendedActionSet += PostIncidentReviewActionId.FAMILY_READINESS_ROADMAP
                    recommendedActionSet += PostIncidentReviewActionId.FAMILY_CALLBACK_CARD
                }
                ReadinessLevel.IN_PROGRESS -> {
                    recommendedActionSet += PostIncidentReviewActionId.FAMILY_READINESS_ROADMAP
                    recommendedActionSet += PostIncidentReviewActionId.FAMILY_SAFETY_DRILL_COACH
                }
                ReadinessLevel.READY -> {
                    recommendedActionSet += PostIncidentReviewActionId.FAMILY_SAFETY_DRILL_COACH
                    recommendedActionSet += PostIncidentReviewActionId.TRUSTED_HELPER_REVIEW
                }
            }
        }
        recommendedActionSet += PostIncidentReviewActionId.PRIVACY_CENTER
        recommendedActionSet += PostIncidentReviewActionId.SAFETY_FLOW_ROUTER
        val recommendedActions = PostIncidentReviewActionId.entries
            .filter { actionId -> actionId in recommendedActionSet }
            .map(::actionFor)

        val anchors = buildAnchors(emphasisByAnchorId)
        val anchorById = anchors.associateBy { anchor -> anchor.id }

        val recommendationTitle = when {
            !consent -> "먼저 시작 동의를 마치고 사건 뒤 가족 점검 시점을 이어서 보기"
            calendarSkipped -> "아직 기록된 사건이 없어 점검 달력 대신 가족 준비부터 살펴보기"
            nearAnchorId != null ->
                "지금은 ${anchorById.getValue(nearAnchorId).dayLabel} 점검 시점에 가까워 그 시점부터 직접 표시하기"
            readinessLevel == ReadinessLevel.LOW ->
                "가족 준비가 아직 부족한 범주라 0일·3일·7일 점검부터 직접 표시하기"
            readinessLevel == ReadinessLevel.IN_PROGRESS ->
                "가족 준비가 진행 중인 범주라 0일부터 1개월 점검까지 나눠 직접 표시하기"
            else -> "가족 준비가 되어 있는 범주라 0일부터 1년까지 전체 점검 시점을 넓게 표시하기"
        }

        val firstTimeMessage = if (calendarSkipped) {
            "아직 사건이 기록되지 않았어요. 기준으로 삼을 사건 날짜가 없으므로 이 달력은 " +
                "건너뛰고, 먼저 가족 안전 계획과 가족 준비 로드맵을 채워 두세요. 나중에 " +
                "사건을 기록하면 그 날짜를 0일로 삼아 직접 표시할 점검 시점이 나타납니다."
        } else {
            null
        }

        return PostIncidentReviewCalendar(
            title = "사건 뒤 가족 점검 달력",
            intro =
                "의심 연락이나 실제 피해가 있었던 뒤 하루로 끝내지 않고 3일·1주·2주·1개월·" +
                    "3개월·6개월·1년마다 가족이 직접 다시 살펴볼 시점을 고정 문장으로 보여 주는 " +
                    "로컬 전용·무상태 수동 안내입니다. 이 화면은 진짜 달력이 아니라 사용자가 " +
                    "직접 자기 달력에 옮겨 적을 목록입니다.",
            modeLabel = "자동 예약 없음 · 알림 보내지 않음 · 달력 앱에 쓰지 않음",
            manualCalendarNote =
                "이 화면은 자동으로 알림을 보내지 않습니다. 가족과 직접 정한 일정에 가족 안전 " +
                    "계획 점검을 표시해 두세요.",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(
                consent = consent,
                readinessLevel = readinessLevel,
                calendarSkipped = calendarSkipped,
                nearAnchorLabel = nearAnchorId?.let { anchorId ->
                    anchorById.getValue(anchorId).dayLabel
                },
                hasDaysAgoInput = safeDaysAgo != null
            ),
            calendarSkipped = calendarSkipped,
            firstTimeMessage = firstTimeMessage,
            emphasizedAnchorId = nearAnchorId,
            recommendedAnchorIds = recommendedAnchorIds,
            recommendedActions = recommendedActions,
            anchors = anchors,
            privacySafetyNotes = privacySafetyNotes(),
            familyShareSummary = buildFamilyShareSummary(
                calendarSkipped = calendarSkipped,
                firstTimeMessage = firstTimeMessage,
                recommendedAnchorIds = recommendedAnchorIds,
                recommendedActions = recommendedActions,
                anchors = anchors
            )
        )
    }

    private fun nearestAnchorId(safeDaysAgo: Int?): PostIncidentReviewAnchorId? {
        if (safeDaysAgo == null) return null
        return PostIncidentReviewAnchorId.entries
            .filter { anchorId ->
                distance(safeDaysAgo, anchorId.dayOffset) <= nearWindowFor(anchorId.dayOffset)
            }
            .minByOrNull { anchorId -> distance(safeDaysAgo, anchorId.dayOffset) }
    }

    private fun nearWindowFor(dayOffset: Int): Int = when {
        dayOffset <= 7 -> 2
        dayOffset <= 30 -> 5
        dayOffset <= 90 -> 10
        else -> 20
    }

    private fun distance(first: Int, second: Int): Int =
        if (first >= second) first - second else second - first

    private fun anchorAfter(
        anchorId: PostIncidentReviewAnchorId
    ): PostIncidentReviewAnchorId? = PostIncidentReviewAnchorId.entries
        .getOrNull(PostIncidentReviewAnchorId.entries.indexOf(anchorId) + 1)

    private fun buildEmphasis(
        calendarSkipped: Boolean,
        nearAnchorId: PostIncidentReviewAnchorId?,
        nextAnchorId: PostIncidentReviewAnchorId?,
        readinessLevel: ReadinessLevel
    ): Map<PostIncidentReviewAnchorId, PostIncidentReviewEmphasis> =
        PostIncidentReviewAnchorId.entries.associateWith { anchorId ->
            when {
                calendarSkipped -> PostIncidentReviewEmphasis.DIMMED
                nearAnchorId != null -> when (anchorId) {
                    nearAnchorId -> PostIncidentReviewEmphasis.EMPHASIZED
                    nextAnchorId -> PostIncidentReviewEmphasis.NORMAL
                    else -> PostIncidentReviewEmphasis.DIMMED
                }
                readinessLevel == ReadinessLevel.LOW ->
                    if (anchorId in earlyAnchorIds) {
                        PostIncidentReviewEmphasis.EMPHASIZED
                    } else {
                        PostIncidentReviewEmphasis.DIMMED
                    }
                readinessLevel == ReadinessLevel.IN_PROGRESS ->
                    if (anchorId in midAnchorIds) {
                        PostIncidentReviewEmphasis.EMPHASIZED
                    } else {
                        PostIncidentReviewEmphasis.NORMAL
                    }
                else -> PostIncidentReviewEmphasis.NORMAL
            }
        }

    private fun primaryActionIdFor(
        anchorId: PostIncidentReviewAnchorId
    ): PostIncidentReviewActionId = when (anchorId) {
        PostIncidentReviewAnchorId.DAY_0_WRAP_UP_WHAT_JUST_HAPPENED ->
            PostIncidentReviewActionId.FOLLOW_UP_CHECK_IN_PLANNER
        PostIncidentReviewAnchorId.DAY_3_EMOTION_AND_IMMEDIATE_RECHECK ->
            PostIncidentReviewActionId.FAMILY_CALM_DEBRIEF_AGENDA
        PostIncidentReviewAnchorId.DAY_7_WORD_ACCOUNT_NUMBER_CHANGES ->
            PostIncidentReviewActionId.SAFETY_PLAN
        PostIncidentReviewAnchorId.DAY_14_RECONTACT_OR_NEW_ATTEMPT ->
            PostIncidentReviewActionId.RECONTACT_BOUNDARY_COACH
        PostIncidentReviewAnchorId.DAY_30_FAMILY_SAFETY_PLAN_REFRESH ->
            PostIncidentReviewActionId.FAMILY_READINESS_ROADMAP
        PostIncidentReviewAnchorId.DAY_90_NEW_FAMILY_LONG_TERM_CARE_CHANGE ->
            PostIncidentReviewActionId.FAMILY_CALLBACK_CARD
        PostIncidentReviewAnchorId.DAY_180_ID_ACCOUNT_SECURITY_SWEEP ->
            PostIncidentReviewActionId.ACCOUNT_LOCKDOWN
        PostIncidentReviewAnchorId.DAY_365_FULL_FAMILY_SAFETY_PLAN_REVIEW ->
            PostIncidentReviewActionId.FAMILY_SAFETY_DRILL_COACH
    }

    private fun buildAnchors(
        emphasisByAnchorId: Map<PostIncidentReviewAnchorId, PostIncidentReviewEmphasis>
    ): List<PostIncidentReviewAnchor> {
        fun action(id: PostIncidentReviewActionId): PostIncidentReviewAction = actionFor(id)

        fun anchor(
            id: PostIncidentReviewAnchorId,
            dayLabel: String,
            title: String,
            whyItMatters: String,
            manualRecheckItems: List<String>,
            familyDiscussionQuestions: List<String>,
            markInOwnCalendarInstruction: String,
            secondaryActions: List<PostIncidentReviewAction>
        ): PostIncidentReviewAnchor {
            val primaryAction = action(primaryActionIdFor(id))
            return PostIncidentReviewAnchor(
                id = id,
                displayOrder = id.displayOrder,
                dayOffset = id.dayOffset,
                dayLabel = dayLabel,
                title = title,
                whyItMatters = whyItMatters,
                manualRecheckItems = manualRecheckItems,
                familyDiscussionQuestions = familyDiscussionQuestions,
                markInOwnCalendarInstruction = markInOwnCalendarInstruction,
                emphasis = emphasisByAnchorId.getValue(id),
                inAppViewLabel = "이 앱에서 보기 · ${primaryAction.label}",
                primaryAction = primaryAction,
                secondaryActions = secondaryActions
            )
        }

        return listOf(
            anchor(
                id = PostIncidentReviewAnchorId.DAY_0_WRAP_UP_WHAT_JUST_HAPPENED,
                dayLabel = "0일차 (사건 당일)",
                title = "방금 끝난 일 정리",
                whyItMatters =
                    "가장 흐릿해지기 쉬운 것은 방금 무슨 요구를 받았고 어디까지 응했는지입니다. " +
                        "당일에 종류만 적어 두면 이후 모든 점검의 기준이 됩니다.",
                manualRecheckItems = listOf(
                    "어떤 요구를 받았고 어디까지 응했는지 종류만 가족과 함께 말로 정리합니다.",
                    "이미 이 앱에 있는 후속 점검 목록을 열어 오늘·내일 할 일을 직접 확인합니다.",
                    "남겨 둘 자료의 종류만 정하고 실제 값은 화면에도 공유문에도 적지 않습니다."
                ),
                familyDiscussionQuestions = listOf(
                    "오늘 우리가 실제로 응한 요구의 종류는 무엇이었나요?",
                    "다음에 같은 요구를 받으면 누가 먼저 멈추고 누구에게 알릴까요?",
                    "오늘 남겨 둬야 할 자료의 종류는 무엇인가요?"
                ),
                markInOwnCalendarInstruction =
                    "종이 달력이나 늘 쓰는 달력 앱에 오늘 날짜를 '가족 안전 점검 0일'로 직접 " +
                        "적고, 그 아래에 3일·7일·14일·30일·90일·180일·365일 칸도 함께 적어 두세요.",
                secondaryActions = listOf(
                    action(PostIncidentReviewActionId.EVIDENCE_KIT),
                    action(PostIncidentReviewActionId.INCIDENT_TIMELINE_BRIEFING),
                    action(PostIncidentReviewActionId.ACTIVE_CONTACT_EXIT_SCRIPTS)
                )
            ),
            anchor(
                id = PostIncidentReviewAnchorId.DAY_3_EMOTION_AND_IMMEDIATE_RECHECK,
                dayLabel = "3일차",
                title = "감정 정리 + 즉시 점검",
                whyItMatters =
                    "사흘쯤 지나면 놀람이 조금 가라앉아 당일에는 말하지 못한 부분이 나옵니다. " +
                        "탓하지 않는 대화로 빠진 부분을 채울 수 있는 시점입니다.",
                manualRecheckItems = listOf(
                    "탓하지 않는 5분 대화 순서를 열어 서로가 놀랐던 지점을 말로 나눕니다.",
                    "당일에 미뤄 둔 확인 항목이 있는지 후속 점검 목록에서 직접 대조합니다.",
                    "혼자 결론 내리지 않도록 보호자와 함께 볼 화면을 정합니다."
                ),
                familyDiscussionQuestions = listOf(
                    "그날 가장 무섭거나 급하게 느껴졌던 순간은 언제였나요?",
                    "지금 다시 보니 미뤄 둔 확인 항목이 남아 있나요?",
                    "다음 점검은 누구와 함께 볼까요?"
                ),
                markInOwnCalendarInstruction =
                    "사건 날짜에서 사흘 뒤 칸에 '가족 안전 점검 3일'이라고 직접 적어 두세요. " +
                        "이 앱은 그 날짜를 대신 적거나 저장하지 않습니다.",
                secondaryActions = listOf(
                    action(PostIncidentReviewActionId.FOLLOW_UP_CHECK_IN_PLANNER),
                    action(PostIncidentReviewActionId.TRUSTED_HELPER_REVIEW),
                    action(PostIncidentReviewActionId.PRIVACY_CENTER)
                )
            ),
            anchor(
                id = PostIncidentReviewAnchorId.DAY_7_WORD_ACCOUNT_NUMBER_CHANGES,
                dayLabel = "7일차 (1주)",
                title = "1주일 점검: 단어·계좌·번호 바뀐 것",
                whyItMatters =
                    "일주일 사이에 가족 확인 문구를 새로 정했거나 계좌·연락 수단을 바꿨을 수 " +
                        "있습니다. 바뀐 것을 계획에 반영하지 않으면 다음에 또 흔들립니다.",
                manualRecheckItems = listOf(
                    "가족 안전 계획 화면을 열어 이번 주에 바뀐 항목이 있는지 직접 확인합니다.",
                    "새로 쓰기 시작한 연락 수단이 있으면 그 종류만 가족과 맞춰 둡니다.",
                    "실제 확인 문구나 번호는 말로만 나누고 이 화면이나 공유문에는 적지 않습니다."
                ),
                familyDiscussionQuestions = listOf(
                    "이번 주에 새로 바꾼 항목의 종류는 무엇인가요?",
                    "바뀐 내용을 아직 모르는 가족이 있나요?",
                    "다음 확인은 어떤 경로로 할지 정했나요?"
                ),
                markInOwnCalendarInstruction =
                    "사건 날짜에서 일주일 뒤 칸에 '가족 안전 점검 7일'이라고 직접 적어 두세요.",
                secondaryActions = listOf(
                    action(PostIncidentReviewActionId.FAMILY_CALLBACK_CARD),
                    action(PostIncidentReviewActionId.FAMILY_READINESS_ROADMAP),
                    action(PostIncidentReviewActionId.ACCOUNT_LOCKDOWN)
                )
            ),
            anchor(
                id = PostIncidentReviewAnchorId.DAY_14_RECONTACT_OR_NEW_ATTEMPT,
                dayLabel = "14일차 (2주)",
                title = "2주 점검: 재연락·새 시도 여부",
                whyItMatters =
                    "한 번 응답한 집에는 다른 이름과 다른 번호로 다시 시도가 오는 경우가 " +
                        "있습니다. 같은 집에 온 시도인지 가족끼리 맞춰 보는 시점입니다.",
                manualRecheckItems = listOf(
                    "지난 2주 동안 비슷한 요구를 받은 사람이 가족 중에 있는지 서로 물어봅니다.",
                    "다시 연락이 왔을 때 쓸 경계 문장을 함께 소리 내어 읽어 봅니다.",
                    "이 앱이 문자·통화를 감시한 결과가 아니라 가족이 직접 확인하는 대화입니다."
                ),
                familyDiscussionQuestions = listOf(
                    "지난 2주 동안 비슷한 연락을 받은 사람이 있었나요?",
                    "다시 연락이 오면 어떤 문장으로 끝낼까요?",
                    "끝낸 뒤에는 누구에게 바로 알릴까요?"
                ),
                markInOwnCalendarInstruction =
                    "사건 날짜에서 2주 뒤 칸에 '가족 안전 점검 14일'이라고 직접 적어 두세요.",
                secondaryActions = listOf(
                    action(PostIncidentReviewActionId.ACTIVE_CONTACT_EXIT_SCRIPTS),
                    action(PostIncidentReviewActionId.FAMILY_CALM_DEBRIEF_AGENDA),
                    action(PostIncidentReviewActionId.SAFETY_FLOW_ROUTER)
                )
            ),
            anchor(
                id = PostIncidentReviewAnchorId.DAY_30_FAMILY_SAFETY_PLAN_REFRESH,
                dayLabel = "30일차 (1개월)",
                title = "1개월 점검: 가족 안전 계획 갱신",
                whyItMatters =
                    "한 달이면 급한 마음이 가라앉아 계획을 차분히 손볼 수 있습니다. 이때 " +
                        "비어 있던 준비 항목을 하나씩 채워 두면 다음 시도에 덜 흔들립니다.",
                manualRecheckItems = listOf(
                    "가족 준비 로드맵에서 아직 비어 있는 항목을 한 가지 골라 직접 채웁니다.",
                    "한 달 전과 비교해 연락 담당이나 함께 볼 보호자가 바뀌었는지 확인합니다.",
                    "한 달이 지났다는 사실만으로 상황이 끝났다고 단정하지 않습니다."
                ),
                familyDiscussionQuestions = listOf(
                    "지난 한 달 사이에 우리 가족 상황에서 달라진 것은 무엇인가요?",
                    "아직 비어 있는 준비 항목은 무엇인가요?",
                    "다음 달에 누가 이 점검을 챙길까요?"
                ),
                markInOwnCalendarInstruction =
                    "사건 날짜에서 한 달 뒤 칸에 '가족 안전 점검 30일'이라고 직접 적어 두세요.",
                secondaryActions = listOf(
                    action(PostIncidentReviewActionId.SAFETY_PLAN),
                    action(PostIncidentReviewActionId.FAMILY_SAFETY_DRILL_COACH),
                    action(PostIncidentReviewActionId.PRIVACY_CENTER)
                )
            ),
            anchor(
                id = PostIncidentReviewAnchorId.DAY_90_NEW_FAMILY_LONG_TERM_CARE_CHANGE,
                dayLabel = "90일차 (3개월)",
                title = "3개월 점검: 새 가족·장기 부양 변화",
                whyItMatters =
                    "석 달이면 결혼·출산·이사·간병처럼 가족 구성이나 돌봄 상황이 바뀌기도 " +
                        "합니다. 바뀐 구성에 맞춰 연락 순서를 다시 맞춰야 합니다.",
                manualRecheckItems = listOf(
                    "가족 구성이나 함께 사는 사람이 바뀌었는지 종류만 확인합니다.",
                    "새로 도움이 필요한 어르신이나 아이가 생겼다면 연락 순서를 다시 정합니다.",
                    "바뀐 연락 순서를 가족 콜백 화면에서 함께 읽어 봅니다."
                ),
                familyDiscussionQuestions = listOf(
                    "지난 석 달 사이에 함께 사는 사람이나 돌봄 상황이 바뀌었나요?",
                    "새로 이 안내를 알아야 할 가족이 있나요?",
                    "연락 순서에서 첫 번째로 확인할 사람은 누구인가요?"
                ),
                markInOwnCalendarInstruction =
                    "사건 날짜에서 석 달 뒤 칸에 '가족 안전 점검 90일'이라고 직접 적어 두세요.",
                secondaryActions = listOf(
                    action(PostIncidentReviewActionId.SAFETY_PLAN),
                    action(PostIncidentReviewActionId.FAMILY_READINESS_ROADMAP),
                    action(PostIncidentReviewActionId.TRUSTED_HELPER_REVIEW)
                )
            ),
            anchor(
                id = PostIncidentReviewAnchorId.DAY_180_ID_ACCOUNT_SECURITY_SWEEP,
                dayLabel = "180일차 (6개월)",
                title = "6개월 점검: 신분·계좌·보안 종합",
                whyItMatters =
                    "노출 가능성이 있었던 정보는 반년쯤 뒤에 다른 형태로 쓰이기도 합니다. " +
                        "공식 경로에서 계정과 명세를 한 번 더 살펴볼 시점입니다.",
                manualRecheckItems = listOf(
                    "평소 쓰던 공식 앱이나 직접 찾은 대표 경로에서 계정·명세를 직접 살펴봅니다.",
                    "계정·기기 잠금 목록에서 아직 하지 않은 항목이 있는지 대조합니다.",
                    "모르는 변경이 보이면 공식 도움 연락처를 직접 확인해 상담합니다."
                ),
                familyDiscussionQuestions = listOf(
                    "반년 사이에 모르는 변경이 보인 계정 종류가 있었나요?",
                    "아직 하지 않은 잠금·확인 항목은 무엇인가요?",
                    "상담이 필요하면 누가 공식 경로로 연락할까요?"
                ),
                markInOwnCalendarInstruction =
                    "사건 날짜에서 반년 뒤 칸에 '가족 안전 점검 180일'이라고 직접 적어 두세요.",
                secondaryActions = listOf(
                    action(PostIncidentReviewActionId.OFFICIAL_HELP_DIRECTORY),
                    action(PostIncidentReviewActionId.EVIDENCE_KIT),
                    action(PostIncidentReviewActionId.PRIVACY_CENTER)
                )
            ),
            anchor(
                id = PostIncidentReviewAnchorId.DAY_365_FULL_FAMILY_SAFETY_PLAN_REVIEW,
                dayLabel = "365일차 (1년)",
                title = "1년 점검: 가족 안전 계획 전면 재검토",
                whyItMatters =
                    "일 년이면 가족이 쓰는 연락 수단과 생활 방식이 꽤 달라집니다. 계획 전체를 " +
                        "처음부터 다시 읽고 낡은 부분을 정리할 시점입니다.",
                manualRecheckItems = listOf(
                    "가족 안전 연습 코치로 멈춤 문구와 확인 순서를 처음부터 다시 연습합니다.",
                    "일 년 전에 정한 항목 중 지금 쓰지 않는 것이 있으면 가족과 함께 정리합니다.",
                    "내년에도 같은 점검을 이어갈지 다음 1년 일정을 직접 적어 둡니다."
                ),
                familyDiscussionQuestions = listOf(
                    "일 년 전에 정한 것 중 지금은 쓰지 않는 항목이 있나요?",
                    "올해 새로 배운 수법 중 가족이 함께 알아야 할 종류가 있나요?",
                    "내년 점검은 언제로 정할까요?"
                ),
                markInOwnCalendarInstruction =
                    "사건 날짜에서 일 년 뒤 칸에 '가족 안전 점검 365일'이라고 직접 적고, " +
                        "그 자리에서 다음 해 일정도 이어서 적어 두세요.",
                secondaryActions = listOf(
                    action(PostIncidentReviewActionId.SAFETY_PLAN),
                    action(PostIncidentReviewActionId.FAMILY_READINESS_ROADMAP),
                    action(PostIncidentReviewActionId.HOME)
                )
            )
        )
    }

    private fun buildRecommendationExplanation(
        consent: Boolean,
        readinessLevel: ReadinessLevel,
        calendarSkipped: Boolean,
        nearAnchorLabel: String?,
        hasDaysAgoInput: Boolean
    ): String {
        val situationCopy = buildList {
            if (!consent) {
                add(
                    "시작 동의 기록이 없다는 범주에서는 다른 시점보다 온보딩 동의 화면 경로를 " +
                        "먼저 보여 주며, 동의 여부가 점검을 대신 진행했다는 뜻은 아닙니다."
                )
            }
            if (calendarSkipped) {
                add(
                    "저장된 사건이 없다는 개수 범주에서는 기준 날짜가 없으므로 점검 시점 전체를 " +
                        "건너뛰고 가족 안전 계획·가족 준비 로드맵·후속 점검 경로만 보여 줍니다."
                )
            } else if (nearAnchorLabel != null) {
                add(
                    "마지막 사건에서 지난 날수 범주가 $nearAnchorLabel 점검에 가까워 그 시점과 " +
                        "바로 다음 시점만 먼저 보여 주고 멀리 있는 시점은 흐리게 둡니다."
                )
            } else if (hasDaysAgoInput) {
                add(
                    "마지막 사건에서 지난 날수 범주가 어떤 고정 시점에도 가깝지 않아 특정 시점을 " +
                        "앞세우지 않고 가족 준비 범주에 맞춰 순서대로 보여 줍니다."
                )
            }
        }.joinToString(" ")

        val readinessCopy = if (calendarSkipped) {
            "가족 준비 범주와 관계없이 사건 기록이 생기기 전까지는 점검 시점을 권하지 않습니다."
        } else {
            when (readinessLevel) {
                ReadinessLevel.LOW ->
                    "가족 준비가 아직 부족한 범주라 0일·3일·7일 시점을 먼저 권하고 뒤쪽 시점은 " +
                        "흐리게 둡니다. 저장된 계획의 실제 값은 추천에 사용하지 않습니다."
                ReadinessLevel.IN_PROGRESS ->
                    "가족 준비가 진행 중인 범주라 0일부터 1개월 시점까지 먼저 권하고 나머지 " +
                        "시점도 순서대로 함께 보여 줍니다. 저장된 계획의 실제 값은 추천에 " +
                        "사용하지 않습니다."
                ReadinessLevel.READY ->
                    "가족 준비가 되어 있다는 범주는 이번 일이 끝났다는 뜻이 아닙니다. 0일부터 " +
                        "1년까지 전체 시점을 넓게 보여 주니 가족과 나눠 직접 표시하세요."
            }
        }

        val boundaryCopy =
            "어떤 경우에도 이 화면은 날짜를 저장하거나 기기 달력 앱에 일정을 쓰지 않고 알림·" +
                "알람·반복 예약을 만들지 않습니다. 표시는 사용자가 자기 달력에 직접 합니다."

        return listOf(situationCopy, readinessCopy, boundaryCopy)
            .filter { copy -> copy.isNotBlank() }
            .joinToString(" ")
    }

    private fun privacySafetyNotes(): List<String> = listOf(
        "이 코치는 고정 문장을 사용자가 직접 읽고 기존 앱 화면을 직접 고르는 로컬 전용·무상태 " +
            "수동 안내이며 새 저장소·자동 처리·자동 전송·백그라운드 작업을 만들지 않습니다.",
        "추천은 시작 동의 여부, 가족 준비 수준, 저장 사건 개수 범주, 마지막 사건에서 지난 날수 " +
            "범주만 사용하며 안전단어·콜백 값·연락처·사건 내용·미리보기·저장된 계획의 실제 값을 " +
            "사용하지 않습니다.",
        "이 화면은 진짜 달력이 아닙니다. 기기 달력 앱이나 일정 제공자를 읽지 않고 일정을 " +
            "만들거나 바꾸지 않으며 어떤 달력 API도 사용하지 않습니다.",
        "점검 날짜·시각·반복 주기·완료 여부를 저장하지 않으며 화면을 닫으면 아무 표시도 남지 않습니다.",
        "알림을 보내지 않고 알람·반복 예약·리마인더·작업·서비스·백그라운드 워커·브로드캐스트 " +
            "수신기를 만들거나 예약하지 않습니다.",
        "연락처·주소록을 읽거나 쓰지 않고 가족 일정이나 연락처를 다른 기기·계정과 맞추지 않습니다.",
        "문자·SMS·통화·통화 기록·발신자 표시·클립보드·알림을 읽거나 감시하지 않고 자동 발신·" +
            "자동 전송을 하지 않습니다.",
        "네트워크·웹 탐색·브라우저·외부 링크 열기를 사용하지 않고 계정·기관·사건·신고 상태를 " +
            "조회·검증하지 않습니다.",
        "카메라·마이크·파일·사진·미디어·저장소를 읽지 않고 화면 캡처·OCR·이미지 분석·녹음을 " +
            "하지 않습니다.",
        "새 Android 권한을 요청하지 않으며 이 화면을 열거나 시점·경로·공유 버튼을 누르는 것만으로 " +
            "사건 기록·가족 안전 계획·계정·기기·설정을 추가·수정·삭제하지 않습니다.",
        "이 코치는 재연락 여부, 계정 상태, 피해 회복 여부를 확인·판정·증명하지 않으며 점검을 " +
            "마쳤다는 이유만으로 상황이 끝났다고 단정하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        calendarSkipped: Boolean,
        firstTimeMessage: String?,
        recommendedAnchorIds: List<PostIncidentReviewAnchorId>,
        recommendedActions: List<PostIncidentReviewAction>,
        anchors: List<PostIncidentReviewAnchor>
    ): String {
        val anchorById = anchors.associateBy { anchor -> anchor.id }
        return buildString {
            appendLine("[사건 뒤 가족 점검 달력]")
            appendLine(
                "이 앱이 일정을 예약하거나 알림을 보내지 않고, 가족이 각자 쓰는 달력에 직접 " +
                    "적어 두는 점검 시점 목록입니다."
            )
            appendLine()
            if (calendarSkipped) {
                appendLine("먼저 볼 안내")
                appendLine("- ${firstTimeMessage.orEmpty()}")
            } else {
                appendLine("먼저 직접 표시할 점검 시점")
                recommendedAnchorIds.forEach { anchorId ->
                    val anchor = anchorById.getValue(anchorId)
                    appendLine("- [${anchor.dayLabel}] ${anchor.title}")
                }
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("전체 점검 시점")
            anchors.forEach { anchor ->
                appendLine("- [${anchor.dayLabel}] ${anchor.title}")
            }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 안전단어·콜백 값·연락처·전화번호·계좌·사건 내용·날짜 값은 보내지 않습니다.")
            append(
                "- 이 안내는 일정을 예약하거나 알림을 보내지 않고 달력 앱에 쓰지 않으며 결과를 " +
                    "보장하지 않습니다."
            )
        }
    }

    private fun actionFor(id: PostIncidentReviewActionId): PostIncidentReviewAction =
        PostIncidentReviewAction(
            id = id,
            label = when (id) {
                PostIncidentReviewActionId.ONBOARDING -> "시작 동의 화면"
                PostIncidentReviewActionId.FOLLOW_UP_CHECK_IN_PLANNER -> "며칠 뒤 다시 확인할 것"
                PostIncidentReviewActionId.FAMILY_CALM_DEBRIEF_AGENDA -> "가족 차분 대화 안건"
                PostIncidentReviewActionId.SAFETY_PLAN -> "가족 안전 계획"
                PostIncidentReviewActionId.RECONTACT_BOUNDARY_COACH -> "다시 연락할 때 경계 정하기"
                PostIncidentReviewActionId.ACTIVE_CONTACT_EXIT_SCRIPTS -> "지금 연락 끝내는 문장"
                PostIncidentReviewActionId.FAMILY_READINESS_ROADMAP -> "가족 준비 로드맵"
                PostIncidentReviewActionId.FAMILY_CALLBACK_CARD -> "가족 콜백 카드"
                PostIncidentReviewActionId.FAMILY_SAFETY_DRILL_COACH -> "가족 안전 연습 코치"
                PostIncidentReviewActionId.ACCOUNT_LOCKDOWN -> "계정·기기 잠금 체크"
                PostIncidentReviewActionId.EVIDENCE_KIT -> "증거 보존 체크"
                PostIncidentReviewActionId.INCIDENT_TIMELINE_BRIEFING -> "사건 흐름 요약"
                PostIncidentReviewActionId.TRUSTED_HELPER_REVIEW -> "보호자 검토 모드"
                PostIncidentReviewActionId.OFFICIAL_HELP_DIRECTORY -> "공식 도움 연락처"
                PostIncidentReviewActionId.PRIVACY_CENTER -> "개인정보 센터"
                PostIncidentReviewActionId.SAFETY_FLOW_ROUTER -> "안전 흐름 길잡이"
                PostIncidentReviewActionId.HOME -> "홈으로"
            }
        )
}
