package com.mongmong.familyshield

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PostIncidentReviewCalendarBuilderTest {
    @Test
    fun defaultCalendarIsConservativeAndNeverClaimsSchedulingOrCompletion() {
        val calendar = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 1,
            lastIncidentDaysAgo = null
        )

        assertEquals("사건 뒤 가족 점검 달력", calendar.title)
        assertTrue(calendar.recommendedAnchorIds.isNotEmpty())
        assertTrue(calendar.recommendedActions.isNotEmpty())
        assertFalse(calendar.calendarSkipped)
        assertNull(calendar.firstTimeMessage)

        listOf(
            "100%",
            "safe",
            "blocked",
            "removed",
            "recovered",
            "guaranteed",
            "automatically scheduled",
            "auto reminder",
            "자동 알림",
            "캘린더에 자동 저장",
            "스케줄러",
            "자동으로 예약",
            "자동으로 일정",
            "일정을 대신 등록",
            "안전합니다",
            "보장합니다",
            "차단했습니다",
            "삭제했습니다",
            "복구했습니다",
            "회복했습니다",
            "끝났습니다",
            "문제없습니다"
        ).forEach { unsafeClaim ->
            assertFalse(
                "default calendar made unsafe claim: $unsafeClaim",
                allCopy(calendar).contains(unsafeClaim, ignoreCase = true)
            )
        }
    }

    @Test
    fun anchorsAreCompleteStableAndUseUniqueDeclaredRouteActions() {
        val calendar = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 3
        )

        assertEquals(8, PostIncidentReviewAnchorId.entries.size)
        assertEquals(PostIncidentReviewAnchorId.entries.size, calendar.anchors.size)
        assertEquals(PostIncidentReviewAnchorId.entries, calendar.anchors.map { it.id })
        assertEquals(
            (1..PostIncidentReviewAnchorId.entries.size).toList(),
            calendar.anchors.map { anchor -> anchor.displayOrder }
        )
        assertEquals(
            listOf(0, 3, 7, 14, 30, 90, 180, 365),
            calendar.anchors.map { anchor -> anchor.dayOffset }
        )

        calendar.anchors.forEach { anchor ->
            assertEquals(anchor.id.displayOrder, anchor.displayOrder)
            assertEquals(anchor.id.dayOffset, anchor.dayOffset)
            assertTrue(anchor.dayLabel.isNotBlank())
            assertTrue(anchor.title.isNotBlank())
            assertTrue(anchor.whyItMatters.isNotBlank())
            assertTrue(anchor.manualRecheckItems.isNotEmpty())
            assertTrue(anchor.manualRecheckItems.all { item -> item.isNotBlank() })
            assertEquals(3, anchor.familyDiscussionQuestions.size)
            assertTrue(anchor.familyDiscussionQuestions.all { question -> question.isNotBlank() })
            assertTrue(anchor.markInOwnCalendarInstruction.contains("직접"))
            assertTrue(anchor.inAppViewLabel.startsWith("이 앱에서 보기 · "))
            assertTrue(anchor.inAppViewLabel.endsWith(anchor.primaryAction.label))
            assertTrue(anchor.secondaryActions.isNotEmpty())
            assertEquals(
                anchor.routeActions.size,
                anchor.routeActions.map { action -> action.id }.distinct().size
            )
            assertTrue(
                anchor.routeActions.all { action -> action.id in PostIncidentReviewActionId.entries }
            )
            assertTrue(anchor.routeActions.all { action -> action.label.isNotBlank() })
            assertFalse(
                PostIncidentReviewActionId.ONBOARDING in anchor.routeActions.map { it.id }
            )
        }

        val primaryActionIds = calendar.anchors.map { anchor -> anchor.primaryAction.id }
        assertEquals(primaryActionIds.size, primaryActionIds.distinct().size)
        assertEquals(
            calendar.recommendedActions.size,
            calendar.recommendedActions.map { action -> action.id }.distinct().size
        )
    }

    @Test
    fun noConsentRecommendsOnboardingLinkFirst() {
        val calendar = PostIncidentReviewCalendarBuilder.build(
            consent = false,
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2,
            lastIncidentDaysAgo = 40
        )

        assertEquals(
            PostIncidentReviewActionId.ONBOARDING,
            calendar.recommendedActions.first().id
        )
        assertTrue(calendar.recommendationTitle.contains("동의"))
        assertTrue(calendar.recommendationExplanation.contains("온보딩 동의 화면 경로"))
    }

    @Test
    fun consentGrantedNeverRecommendsOnboarding() {
        val calendar = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2
        )

        assertFalse(PostIncidentReviewActionId.ONBOARDING in calendar.recommendedActionIds)
    }

    @Test
    fun noIncidentHistorySkipsEntireCalendarAndShowsFirstTimeMessage() {
        val calendar = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 0,
            lastIncidentDaysAgo = 30
        )

        assertTrue(calendar.calendarSkipped)
        assertTrue(calendar.recommendedAnchorIds.isEmpty())
        assertNull(calendar.emphasizedAnchorId)
        assertTrue(
            calendar.anchors.all { anchor ->
                anchor.emphasis == PostIncidentReviewEmphasis.DIMMED
            }
        )
        val firstTimeMessage = requireNotNull(calendar.firstTimeMessage)
        assertTrue(firstTimeMessage.startsWith("아직 사건이 기록되지 않았어요."))
        assertTrue(calendar.recommendationTitle.contains("아직 기록된 사건이 없어"))
        assertEquals(
            listOf(
                PostIncidentReviewActionId.SAFETY_PLAN,
                PostIncidentReviewActionId.FAMILY_READINESS_ROADMAP,
                PostIncidentReviewActionId.FOLLOW_UP_CHECK_IN_PLANNER,
                PostIncidentReviewActionId.PRIVACY_CENTER,
                PostIncidentReviewActionId.SAFETY_FLOW_ROUTER
            ).sortedBy { actionId -> actionId.ordinal },
            calendar.recommendedActionIds
        )
        assertTrue(calendar.familyShareSummary.contains(firstTimeMessage))
        assertFalse(calendar.familyShareSummary.contains("먼저 직접 표시할 점검 시점"))
    }

    @Test
    fun negativeIncidentCountIsClampedToNoHistory() {
        val negativeIncidentCount = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = -9
        )
        val zeroIncidentCount = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 0
        )
        val oneIncident = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 1
        )
        val manyIncidents = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = Int.MAX_VALUE
        )

        assertEquals(zeroIncidentCount, negativeIncidentCount)
        assertEquals(oneIncident, manyIncidents)
        assertNotEquals(zeroIncidentCount.calendarSkipped, oneIncident.calendarSkipped)
    }

    @Test
    fun lastIncidentDaysAgoNearAnchorEmphasizesThatAnchorAndDimsFarOnes() {
        val calendar = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 1,
            lastIncidentDaysAgo = 31
        )

        assertEquals(
            PostIncidentReviewAnchorId.DAY_30_FAMILY_SAFETY_PLAN_REFRESH,
            calendar.emphasizedAnchorId
        )
        val emphasisById = calendar.anchors.associate { anchor -> anchor.id to anchor.emphasis }
        assertEquals(
            PostIncidentReviewEmphasis.EMPHASIZED,
            emphasisById.getValue(PostIncidentReviewAnchorId.DAY_30_FAMILY_SAFETY_PLAN_REFRESH)
        )
        assertEquals(
            PostIncidentReviewEmphasis.NORMAL,
            emphasisById.getValue(
                PostIncidentReviewAnchorId.DAY_90_NEW_FAMILY_LONG_TERM_CARE_CHANGE
            )
        )
        listOf(
            PostIncidentReviewAnchorId.DAY_0_WRAP_UP_WHAT_JUST_HAPPENED,
            PostIncidentReviewAnchorId.DAY_3_EMOTION_AND_IMMEDIATE_RECHECK,
            PostIncidentReviewAnchorId.DAY_7_WORD_ACCOUNT_NUMBER_CHANGES,
            PostIncidentReviewAnchorId.DAY_14_RECONTACT_OR_NEW_ATTEMPT,
            PostIncidentReviewAnchorId.DAY_180_ID_ACCOUNT_SECURITY_SWEEP,
            PostIncidentReviewAnchorId.DAY_365_FULL_FAMILY_SAFETY_PLAN_REVIEW
        ).forEach { farAnchorId ->
            assertEquals(
                "far anchor was not dimmed: $farAnchorId",
                PostIncidentReviewEmphasis.DIMMED,
                emphasisById.getValue(farAnchorId)
            )
        }
        assertTrue(calendar.recommendationTitle.contains("30일차 (1개월)"))
        assertTrue(
            PostIncidentReviewAnchorId.DAY_30_FAMILY_SAFETY_PLAN_REFRESH in
                calendar.recommendedAnchorIds
        )
        assertTrue(
            PostIncidentReviewAnchorId.DAY_90_NEW_FAMILY_LONG_TERM_CARE_CHANGE in
                calendar.recommendedAnchorIds
        )
    }

    @Test
    fun lastIncidentDaysAgoFarFromEveryAnchorFallsBackToReadinessOrder() {
        val calendar = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 1,
            lastIncidentDaysAgo = 250
        )

        assertNull(calendar.emphasizedAnchorId)
        assertTrue(
            calendar.anchors.all { anchor ->
                anchor.emphasis == PostIncidentReviewEmphasis.NORMAL
            }
        )
        assertTrue(calendar.recommendationExplanation.contains("어떤 고정 시점에도 가깝지 않아"))
    }

    @Test
    fun lastIncidentDaysAgoClampsNegativeAndTreatsUnknownAsNoEmphasis() {
        val negativeDaysAgo = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 1,
            lastIncidentDaysAgo = -400
        )
        val sameDayIncident = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 1,
            lastIncidentDaysAgo = 0
        )
        val unknownDaysAgo = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 1,
            lastIncidentDaysAgo = null
        )

        assertEquals(sameDayIncident, negativeDaysAgo)
        assertEquals(
            PostIncidentReviewAnchorId.DAY_0_WRAP_UP_WHAT_JUST_HAPPENED,
            negativeDaysAgo.emphasizedAnchorId
        )
        assertNull(unknownDaysAgo.emphasizedAnchorId)
        assertNotEquals(unknownDaysAgo, sameDayIncident)
    }

    @Test
    fun lowReadinessEmphasizesEarlyAnchorsAndHighReadinessSpreadsFullCalendar() {
        val lowReadiness = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 1
        )
        val inProgressReadiness = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.IN_PROGRESS),
            incidentCount = 1
        )
        val readyReadiness = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 1
        )

        val earlyAnchorIds = listOf(
            PostIncidentReviewAnchorId.DAY_0_WRAP_UP_WHAT_JUST_HAPPENED,
            PostIncidentReviewAnchorId.DAY_3_EMOTION_AND_IMMEDIATE_RECHECK,
            PostIncidentReviewAnchorId.DAY_7_WORD_ACCOUNT_NUMBER_CHANGES
        )
        assertEquals(earlyAnchorIds, lowReadiness.recommendedAnchorIds)
        lowReadiness.anchors.forEach { anchor ->
            val expected = if (anchor.id in earlyAnchorIds) {
                PostIncidentReviewEmphasis.EMPHASIZED
            } else {
                PostIncidentReviewEmphasis.DIMMED
            }
            assertEquals("low readiness emphasis wrong for ${anchor.id}", expected, anchor.emphasis)
        }
        assertTrue(lowReadiness.recommendationTitle.contains("0일·3일·7일 점검부터"))

        assertEquals(
            earlyAnchorIds + listOf(
                PostIncidentReviewAnchorId.DAY_14_RECONTACT_OR_NEW_ATTEMPT,
                PostIncidentReviewAnchorId.DAY_30_FAMILY_SAFETY_PLAN_REFRESH
            ),
            inProgressReadiness.recommendedAnchorIds
        )

        assertEquals(PostIncidentReviewAnchorId.entries, readyReadiness.recommendedAnchorIds)
        assertTrue(
            readyReadiness.anchors.all { anchor ->
                anchor.emphasis == PostIncidentReviewEmphasis.NORMAL
            }
        )
        assertTrue(readyReadiness.recommendationTitle.contains("0일부터 1년까지"))
        assertTrue(readyReadiness.recommendationExplanation.contains("이번 일이 끝났다는 뜻이 아닙니다"))
    }

    @Test
    fun familyShareSummaryIsRebuiltOnlyFromGenericFixedAnchorAndRouteLabels() {
        val calendar = PostIncidentReviewCalendarBuilder.build(
            consent = false,
            readiness = readiness(
                ReadinessLevel.IN_PROGRESS,
                "PLAN_LITERAL SAFE_WORD_LITERAL CALLBACK_LITERAL CONTACT_LITERAL"
            ),
            incidentCount = 4,
            lastIncidentDaysAgo = 8
        )
        val summary = calendar.familyShareSummary
        val anchorById = calendar.anchors.associateBy { anchor -> anchor.id }
        val expected = buildString {
            appendLine("[사건 뒤 가족 점검 달력]")
            appendLine(
                "이 앱이 일정을 예약하거나 알림을 보내지 않고, 가족이 각자 쓰는 달력에 직접 " +
                    "적어 두는 점검 시점 목록입니다."
            )
            appendLine()
            appendLine("먼저 직접 표시할 점검 시점")
            calendar.recommendedAnchorIds.forEach { anchorId ->
                val anchor = anchorById.getValue(anchorId)
                appendLine("- [${anchor.dayLabel}] ${anchor.title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            calendar.recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("전체 점검 시점")
            calendar.anchors.forEach { anchor ->
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
        assertEquals(expected, summary)

        listOf(
            "PLAN_LITERAL",
            "SAFE_WORD_LITERAL",
            "CALLBACK_LITERAL",
            "CONTACT_LITERAL"
        ).forEach { privateValue ->
            assertFalse("share leaked $privateValue", summary.contains(privateValue))
        }
        listOf(
            Regex("\\d{2,3}-\\d{3,4}-\\d{4}"),
            Regex("\\b\\d{6}-\\d{7}\\b"),
            Regex("https?://"),
            Regex("(?i)\\bOTP\\b"),
            Regex("(?i)\\bwww\\.")
        ).forEach { sensitivePattern ->
            assertFalse(
                "share summary contained sensitive pattern: $sensitivePattern",
                sensitivePattern.containsMatchIn(summary)
            )
        }
    }

    @Test
    fun builderIsDeterministicAndIgnoresReadinessLiteralFields() {
        val first = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "FIRST_PLAN_VALUE"),
            incidentCount = 2,
            lastIncidentDaysAgo = 15
        )
        val second = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "SECOND_PLAN_VALUE"),
            incidentCount = 77,
            lastIncidentDaysAgo = 15
        )
        val third = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "FIRST_PLAN_VALUE"),
            incidentCount = 2,
            lastIncidentDaysAgo = 15
        )

        assertEquals(first, second)
        assertEquals(first, third)
        val copy = allCopy(first)
        listOf("FIRST_PLAN_VALUE", "SECOND_PLAN_VALUE").forEach { privateValue ->
            assertFalse("calendar leaked $privateValue", copy.contains(privateValue))
        }
    }

    @Test
    fun privacyCopyStatesManualLocalStatelessAndEveryBoundary() {
        val calendar = PostIncidentReviewCalendarBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 1,
            lastIncidentDaysAgo = 7
        )
        val copy = allCopy(calendar)

        assertEquals(
            "이 화면은 자동으로 알림을 보내지 않습니다. 가족과 직접 정한 일정에 가족 안전 " +
                "계획 점검을 표시해 두세요.",
            calendar.manualCalendarNote
        )
        assertEquals("자동 예약 없음 · 알림 보내지 않음 · 달력 앱에 쓰지 않음", calendar.modeLabel)

        listOf(
            "로컬 전용",
            "무상태 수동 안내",
            "새 저장소·자동 처리·자동 전송·백그라운드 작업을 만들지 않습니다",
            "이 화면은 진짜 달력이 아닙니다",
            "기기 달력 앱이나 일정 제공자를 읽지 않고 일정을 만들거나 바꾸지 않으며 어떤 달력 " +
                "API도 사용하지 않습니다",
            "점검 날짜·시각·반복 주기·완료 여부를 저장하지 않으며",
            "알림을 보내지 않고 알람·반복 예약·리마인더·작업·서비스·백그라운드 워커·브로드캐스트 " +
                "수신기를 만들거나 예약하지 않습니다",
            "연락처·주소록을 읽거나 쓰지 않고 가족 일정이나 연락처를 다른 기기·계정과 맞추지 않습니다",
            "문자·SMS·통화·통화 기록·발신자 표시·클립보드·알림을 읽거나 감시하지 않고",
            "네트워크·웹 탐색·브라우저·외부 링크 열기를 사용하지 않고",
            "새 Android 권한을 요청하지 않으며",
            "표시는 사용자가 자기 달력에 직접 합니다"
        ).forEach { boundary ->
            assertTrue("privacy copy omitted $boundary", copy.contains(boundary))
        }
    }

    @Test
    fun modelSourceIsPureAndReadsOnlyApprovedCategoryInputs() {
        val source = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/PostIncidentReviewCalendar.kt"
        )

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(source))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(source))
        assertFalse(source.contains("SafetyPlanStore"))
        assertFalse(source.contains("IncidentStore"))
        assertFalse(source.contains("IncidentPreviewRedactor"))
        assertFalse(source.contains("java.util.Calendar"))
        assertFalse(source.contains("System.currentTimeMillis"))
        assertFalse(source.contains("LocalDate"))

        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(source).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(source).map { match -> match.groupValues[1] }.toSet()
        val daysAgoOperations = Regex(
            "lastIncidentDaysAgo\\s*\\??\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(source).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)
        assertEquals(setOf("coerceAtLeast"), daysAgoOperations)

        val implementationSource = stripStringLiterals(source)
        listOf(
            "CalendarContract",
            "AlarmManager",
            "NotificationManager",
            "NotificationCompat",
            "WorkManager",
            "JobScheduler",
            "ForegroundService",
            "BroadcastReceiver",
            "ContentProvider",
            "ContactsContract",
            "SmsManager",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "MediaStore",
            "ContentObserver",
            "FLAG_SECURE",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "USE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED"
        ).forEach { forbiddenApi ->
            assertFalse(
                "post-incident review calendar model used forbidden API: $forbiddenApi",
                implementationSource.contains(forbiddenApi)
            )
        }
    }

    @Test
    fun everyActionIdMapsToAnExistingMainActivityFlow() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val routerSource = functionBlock(
            activitySource,
            "private fun openPostIncidentReviewCalendarAction"
        )
        val normalizedRouter = routerSource.replace(Regex("\\s+"), " ")
        val expectedActionRoutes = linkedMapOf(
            "ONBOARDING" to "show(Screen.ONBOARDING)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "INCIDENT_TIMELINE_BRIEFING" to "show(Screen.TIMELINE_BRIEFING)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "OFFICIAL_HELP_DIRECTORY" to "openHelpDirectory(Screen.POST_INCIDENT_REVIEW_CALENDAR)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)",
            "HOME" to "show(Screen.HOME)"
        )
        assertEquals(PostIncidentReviewActionId.entries.size, expectedActionRoutes.size)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "action did not map to existing flow: $actionId",
                normalizedRouter.contains("PostIncidentReviewActionId.$actionId -> $destination")
            )
        }
        assertEquals(
            PostIncidentReviewActionId.entries.size,
            Regex("PostIncidentReviewActionId\\.[A-Z_]+\\s*->").findAll(routerSource).count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(routerSource).count())
        assertFalse(routerSource.contains("dial("))
        assertFalse(routerSource.contains("Intent.ACTION_DIAL"))
        assertFalse(routerSource.contains("Intent.ACTION_CALL"))
    }

    @Test
    fun boundedUiSliceUsesOnlyCountReadinessConsentTimestampRoutesAndManualTextShare() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val screenSource = functionBlock(
            activitySource,
            "private fun LinearLayout.postIncidentReviewCalendarScreen"
        )
        val anchorSource = functionBlock(
            activitySource,
            "private fun LinearLayout.postIncidentReviewAnchorCard"
        )
        val shareSource = functionBlock(
            activitySource,
            "private fun sharePostIncidentReviewCalendar"
        )
        val daysAgoSource = functionBlock(activitySource, "private fun lastIncidentDaysAgo")

        assertTrue(
            activitySource.contains(
                "Screen.POST_INCIDENT_REVIEW_CALENDAR -> postIncidentReviewCalendarScreen()"
            )
        )
        assertEquals(
            1,
            Regex("PostIncidentReviewCalendarBuilder\\.build\\(")
                .findAll(activitySource)
                .count()
        )
        assertTrue(screenSource.contains("prefs.getBoolean(KEY_CONSENTED, false)"))
        assertTrue(screenSource.contains("FamilyReadinessBuilder.build(safetyPlan.load())"))
        assertTrue(screenSource.contains("incidentCount = incidents.count()"))
        assertTrue(screenSource.contains("lastIncidentDaysAgo = lastIncidentDaysAgo()"))

        listOf(
            "title(calendar.title)",
            "subtitle(calendar.intro)",
            "calendar.manualCalendarNote",
            "calendar.modeLabel",
            "calendar.recommendationTitle",
            "calendar.recommendationExplanation",
            "calendar.recommendedActions",
            "calendar.anchors",
            "calendar.privacySafetyNotes",
            "sharePostIncidentReviewCalendar(calendar.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue("calendar UI omitted $renderedField", screenSource.contains(renderedField))
        }
        listOf(
            "anchor.dayLabel",
            "anchor.title",
            "anchor.whyItMatters",
            "anchor.manualRecheckItems",
            "anchor.familyDiscussionQuestions",
            "anchor.markInOwnCalendarInstruction",
            "anchor.inAppViewLabel",
            "anchor.primaryAction",
            "anchor.secondaryActions"
        ).forEach { renderedField ->
            assertTrue("anchor card omitted $renderedField", anchorSource.contains(renderedField))
        }

        assertTrue(daysAgoSource.contains("incidents.load()"))
        assertTrue(daysAgoSource.contains("incident.timestamp"))
        assertFalse(daysAgoSource.contains("incident.preview"))
        assertFalse(daysAgoSource.contains("incident.signals"))
        assertFalse(daysAgoSource.contains("incident.score"))

        assertTrue(shareSource.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareSource.contains("type = \"text/plain\""))
        assertTrue(shareSource.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareSource).count())
        assertFalse(shareSource.contains("Intent.EXTRA_STREAM"))
        assertFalse(shareSource.contains("Uri.parse"))

        val boundedImplementationSource = stripStringLiterals(
            screenSource + anchorSource + shareSource + daysAgoSource
        )
        listOf(
            "CalendarContract",
            "AlarmManager",
            "NotificationManager",
            "NotificationCompat",
            "WorkManager",
            "JobScheduler",
            "BroadcastReceiver",
            "ContentObserver",
            "MediaStore",
            "ClipboardManager",
            "Intent.ACTION_VIEW",
            "Intent.ACTION_CALL",
            "analysis.input",
            "IncidentPreviewRedactor",
            "prefs.edit(",
            "safetyPlan.save(",
            "incidents.add(",
            "incidents.clear(",
            "EditText(",
            "CheckBox(",
            "putString(",
            "putBoolean("
        ).forEach { forbiddenApi ->
            assertFalse(
                "bounded post-incident calendar UI used forbidden implementation: $forbiddenApi",
                boundedImplementationSource.contains(forbiddenApi, ignoreCase = true)
            )
        }
    }

    @Test
    fun everyDeclaredEntryPointRoutesToTheCalendarScreen() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.POST_INCIDENT_REVIEW_CALENDAR\\)\\s*}"
        )
        listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.privacyCenterScreen"
        ).forEach { marker ->
            val entryScreen = functionBlock(activitySource, marker)
            assertEquals(
                "calendar entry must have exactly one show-only button in $marker",
                1,
                entryRoutePattern.findAll(entryScreen).count()
            )
        }
    }

    @Test
    fun privacyCenterInventoriesTheCalendarAsManualNoSchedulerAndNonMutating() {
        val center = PrivacyCenterBuilder.build(SafetyPlan("", "", emptyList()), 0, false)

        val transientInventory = center.localDataInventoryItems.single { item ->
            item.title == "임시 입력·카드·선택 상태"
        }
        assertTrue(transientInventory.detail.contains("사건 뒤 가족 점검 달력의 시점 선택"))
        assertTrue(transientInventory.detail.contains("점검 날짜·기준 날짜·시각·"))

        val featureStatements = center.noCollectionStatements.filter { statement ->
            statement.contains("사건 뒤 가족 점검 달력")
        }
        assertEquals(2, featureStatements.size)
        val boundaryStatement = featureStatements.single { statement ->
            statement.startsWith("사건 뒤 가족 점검 달력은")
        }
        val mutationStatement = featureStatements.single { statement ->
            statement.startsWith("사건 뒤 가족 점검 달력 화면")
        }
        listOf(
            "새 Android 권한·네트워크·저장소 없는 로컬 전용·무상태 수동 안내",
            "이 화면은 자동으로 알림을 보내지 않습니다",
            "가족과 직접 정한 일정에 가족 안전 계획 점검을 표시해 두세요",
            "기기 달력 앱의 일정을 읽거나 만들거나 바꾸지 않으며",
            "알림·알람·반복 예약·리마인더를 만들거나 예약하지 않습니다",
            "마지막 사건에서 지난 날수 범주만 사용하고"
        ).forEach { boundary ->
            assertTrue("privacy center omitted $boundary", boundaryStatement.contains(boundary))
        }
        assertTrue(mutationStatement.contains("추가·수정·삭제하지 않습니다"))
    }

    @Test
    fun manifestAndMainSourceStayPermissionSchedulerAndComponentFree() {
        val root = projectRoot()
        val manifest = File(root, "app/src/main/AndroidManifest.xml").readText()

        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver|provider)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))

        val forbiddenApis = listOf(
            "CalendarContract",
            "AlarmManager",
            "NotificationManager",
            "NotificationCompat",
            "WorkManager",
            "JobScheduler",
            "ForegroundService",
            "BroadcastReceiver",
            "ContentProvider",
            "ContactsContract",
            "SmsManager",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "USE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED"
        )
        File(root, "app/src/main/java").walkTopDown()
            .filter { file -> file.isFile && file.extension == "kt" }
            .forEach { file ->
                val implementationText = stripStringLiterals(file.readText())
                forbiddenApis.forEach { forbiddenApi ->
                    assertFalse(
                        "${file.name} used forbidden API: $forbiddenApi",
                        implementationText.contains(forbiddenApi)
                    )
                }
            }
    }

    private fun readiness(
        level: ReadinessLevel,
        privateLiteral: String = "PRIVATE_READINESS_LITERAL"
    ): FamilyReadiness = FamilyReadiness(
        level = level,
        title = "TITLE_$privateLiteral",
        completedCount = 731,
        totalCount = 742,
        checklist = listOf(
            ReadinessItem(
                label = "LABEL_$privateLiteral",
                done = true,
                action = "ACTION_$privateLiteral"
            )
        ),
        nextAction = "NEXT_$privateLiteral"
    )

    private fun allCopy(calendar: PostIncidentReviewCalendar): String = buildString {
        appendLine(calendar.title)
        appendLine(calendar.intro)
        appendLine(calendar.modeLabel)
        appendLine(calendar.manualCalendarNote)
        appendLine(calendar.recommendationTitle)
        appendLine(calendar.recommendationExplanation)
        appendLine(calendar.firstTimeMessage.orEmpty())
        calendar.recommendedActions.forEach { action -> appendLine(action.label) }
        calendar.anchors.forEach { anchor ->
            appendLine(anchor.dayLabel)
            appendLine(anchor.title)
            appendLine(anchor.whyItMatters)
            anchor.manualRecheckItems.forEach(::appendLine)
            anchor.familyDiscussionQuestions.forEach(::appendLine)
            appendLine(anchor.markInOwnCalendarInstruction)
            appendLine(anchor.emphasis.label)
            appendLine(anchor.inAppViewLabel)
            anchor.routeActions.forEach { action -> appendLine(action.label) }
        }
        calendar.privacySafetyNotes.forEach(::appendLine)
        append(calendar.familyShareSummary)
    }

    private fun stripStringLiterals(source: String): String = source.replace(
        Regex("\"(?:\\\\.|[^\"\\\\])*\""),
        "\"\""
    )

    private fun functionBlock(source: String, marker: String): String {
        val start = source.indexOf(marker)
        require(start >= 0) { "Source marker missing: $marker" }
        val openingBrace = source.indexOf('{', start)
        require(openingBrace >= 0) { "Opening brace missing after: $marker" }
        var depth = 0
        for (index in openingBrace until source.length) {
            when (source[index]) {
                '{' -> depth += 1
                '}' -> {
                    depth -= 1
                    if (depth == 0) return source.substring(start, index + 1)
                }
            }
        }
        error("Closing brace missing after: $marker")
    }

    private fun projectRoot(): File {
        var directory = File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
        repeat(8) {
            val candidate = File(directory, "app/src/main/java")
            if (candidate.isDirectory) return directory
            val parent = directory.parentFile
            if (parent != null) directory = parent
        }
        error("Could not locate project root")
    }

    private fun readProjectFile(relativePath: String): String {
        var directory = File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
        repeat(8) {
            val candidate = File(directory, relativePath)
            if (candidate.isFile) return candidate.readText()
            val parent = directory.parentFile
            if (parent != null) directory = parent
        }
        error("Could not locate project file: $relativePath")
    }
}
