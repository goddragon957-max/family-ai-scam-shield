package com.mongmong.familyshield

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityCoachBuilderTest {
    @Test
    fun defaultCoachIsConservativeAndNeverClaimsAutoFontScaleOrDetection() {
        val coach = AccessibilityCoachBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals("큰글씨·읽어주기 접근성 코치", coach.title)
        assertTrue(coach.recommendedCardIds.isNotEmpty())
        assertTrue(coach.recommendedActions.isNotEmpty())

        listOf(
            "screenshot detected",
            "font resized",
            "auto scaled",
            "talkback enabled",
            "safe",
            "guaranteed",
            "problem-free",
            "글자가 커졌습니다",
            "자동으로 글자 크게",
            "읽어주기가 켜졌습니다",
            "고대비가 켜졌습니다",
            "확대가 켜졌습니다",
            "설정이 완료됐습니다",
            "안전합니다",
            "보장합니다",
            "문제없습니다",
            "100%"
        ).forEach { unsafeClaim ->
            assertFalse(
                "default coach made unsafe claim: $unsafeClaim",
                allCopy(coach).contains(unsafeClaim, ignoreCase = true)
            )
        }
    }

    @Test
    fun noConsentRecommendsOnboardingLinkFirst() {
        val coach = AccessibilityCoachBuilder.build(
            consent = false,
            readiness = readiness(ReadinessLevel.LOW)
        )

        assertEquals(AccessibilityCoachActionId.ONBOARDING, coach.recommendedActions.first().id)
        assertTrue(coach.recommendationTitle.contains("동의"))
    }

    @Test
    fun consentGrantedNeverRecommendsOnboarding() {
        val coach = AccessibilityCoachBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.LOW)
        )

        assertFalse(AccessibilityCoachActionId.ONBOARDING in coach.recommendedActionIds)
    }

    @Test
    fun lowOrInProgressReadinessRecommendsFamilySafeWordCallbackRoadmap() {
        listOf(ReadinessLevel.LOW, ReadinessLevel.IN_PROGRESS).forEach { level ->
            val coach = AccessibilityCoachBuilder.build(
                consent = true,
                readiness = readiness(level, "READINESS_PRIVATE_LITERAL")
            )

            assertTrue(
                AccessibilityCoachCardId.FAMILY_SAFE_WORD_CALLBACK_BIG_TEXT in
                    coach.recommendedCardIds
            )
            assertTrue(
                AccessibilityCoachActionId.FAMILY_READINESS_ROADMAP in coach.recommendedActionIds
            )
            assertFalse(allCopy(coach).contains("READINESS_PRIVATE_LITERAL"))
        }
    }

    @Test
    fun incidentPresentRecommendsAnalysisResultAndEvidenceKitUsingOnlyClampedCount() {
        val noIncident = AccessibilityCoachBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 0
        )
        val negativeIncident = AccessibilityCoachBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = -12
        )
        val oneIncident = AccessibilityCoachBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 1
        )
        val manyIncidents = AccessibilityCoachBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = Int.MAX_VALUE
        )

        assertEquals(noIncident, negativeIncident)
        assertEquals(oneIncident, manyIncidents)
        assertNotEquals(noIncident.recommendedCardIds, oneIncident.recommendedCardIds)
        assertTrue(
            AccessibilityCoachCardId.CURRENT_ANALYSIS_BIG_TEXT in oneIncident.recommendedCardIds
        )
        assertTrue(
            AccessibilityCoachCardId.EMERGENCY_REPORT_NUMBERS in oneIncident.recommendedCardIds
        )
        assertTrue(AccessibilityCoachActionId.ANALYSIS_RESULT in oneIncident.recommendedActionIds)
        assertTrue(AccessibilityCoachActionId.EVIDENCE_KIT in oneIncident.recommendedActionIds)
        assertTrue(oneIncident.recommendationExplanation.contains("개수 범주만"))
    }

    @Test
    fun highReadinessAndConsentRecommendsSimpleModeHintAndPrivacyCenter() {
        val coach = AccessibilityCoachBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 0
        )

        assertTrue(
            AccessibilityCoachCardId.SIMPLE_MODE_TOGGLE_HINT in coach.recommendedCardIds
        )
        assertTrue(AccessibilityCoachActionId.PRIVACY_CENTER in coach.recommendedActionIds)
    }

    @Test
    fun readyWithoutConsentDoesNotRecommendSimpleModeHint() {
        val coach = AccessibilityCoachBuilder.build(
            consent = false,
            readiness = readiness(ReadinessLevel.READY),
            incidentCount = 0
        )

        assertFalse(
            AccessibilityCoachCardId.SIMPLE_MODE_TOGGLE_HINT in coach.recommendedCardIds
        )
    }

    @Test
    fun cardsAreCompleteStableAndUseUniqueDeclaredRouteActions() {
        val coach = AccessibilityCoachBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(AccessibilityCoachCardId.entries.size, coach.cards.size)
        assertEquals(AccessibilityCoachCardId.entries, coach.cards.map { card -> card.id })
        assertEquals(
            (1..AccessibilityCoachCardId.entries.size).toList(),
            coach.cards.map { card -> card.displayOrder }
        )
        coach.cards.forEach { card ->
            assertEquals(card.id.displayOrder, card.displayOrder)
            assertTrue(card.title.isNotBlank())
            assertTrue(card.whyItMatters.isNotBlank())
            assertTrue(card.inAppSteps.isNotEmpty() || card.systemSettingSteps.isNotEmpty())
            assertTrue(card.primaryAction.label.isNotBlank())
            assertEquals(
                card.routeActions.size,
                card.routeActions.map { action -> action.id }.distinct().size
            )
            assertTrue(
                card.routeActions.all { action -> action.id in AccessibilityCoachActionId.entries }
            )
        }
        assertEquals(
            coach.recommendedActions.size,
            coach.recommendedActions.map { action -> action.id }.distinct().size
        )
    }

    @Test
    fun everyActionIdMapsToAnExistingMainActivityFlow() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val routerSource = functionBlock(
            activitySource,
            "private fun openAccessibilityCoachAction"
        )
        val normalizedRouter = routerSource.replace(Regex("\\s+"), " ")
        val expectedActionRoutes = linkedMapOf(
            "ONBOARDING" to "show(Screen.ONBOARDING)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "OFFICIAL_HELP_DIRECTORY" to "openHelpDirectory(Screen.ACCESSIBILITY_COACH)",
            "CHECKER" to "show(Screen.CHECKER)",
            "HOME" to "show(Screen.HOME)"
        )
        assertEquals(AccessibilityCoachActionId.entries.size, expectedActionRoutes.size)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "action did not map to existing flow: $actionId",
                normalizedRouter.contains("AccessibilityCoachActionId.$actionId -> $destination")
            )
        }
        assertEquals(
            AccessibilityCoachActionId.entries.size,
            Regex("AccessibilityCoachActionId\\.[A-Z_]+\\s*->").findAll(routerSource).count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(routerSource).count())
    }

    @Test
    fun familyShareSummaryIsRebuiltOnlyFromGenericFixedCardAndRouteLabels() {
        val coach = AccessibilityCoachBuilder.build(
            consent = false,
            readiness = readiness(
                ReadinessLevel.IN_PROGRESS,
                "PLAN_LITERAL SAFE_WORD_LITERAL CALLBACK_LITERAL CONTACT_LITERAL"
            ),
            incidentCount = 5
        )
        val summary = coach.familyShareSummary
        val cardsById = coach.cards.associateBy { card -> card.id }
        val expected = buildString {
            appendLine("[큰글씨·읽어주기 접근성 코치]")
            appendLine(
                "시스템 글자 크기·읽어주기 설정을 자동으로 켜지 않고 직접 켜는 방법과 이 앱 " +
                    "안에서 바로 되는 일을 나눠 안내하는 로컬 수동 코치입니다."
            )
            appendLine()
            appendLine("추천 접근성 안내 카드")
            coach.recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            coach.recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 안전단어·콜백 값·연락처·사건 내용은 보내지 않습니다.")
            append(
                "- 이 안내는 시스템 글자 크기·읽어주기·고대비·확대 설정을 자동으로 켜거나 " +
                    "바꾸지 않고 결과를 보장하지 않습니다."
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
    }

    @Test
    fun builderIsDeterministicAndIgnoresReadinessLiteralFields() {
        val first = AccessibilityCoachBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "FIRST_PLAN_VALUE"),
            incidentCount = 2
        )
        val second = AccessibilityCoachBuilder.build(
            consent = true,
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "SECOND_PLAN_VALUE"),
            incidentCount = 82
        )

        assertEquals(first, second)
        assertNoPrivateValues(first, "FIRST_PLAN_VALUE", "SECOND_PLAN_VALUE")
    }

    @Test
    fun privacyCopyStatesManualLocalStatelessAndEveryBoundary() {
        val copy = allCopy(
            AccessibilityCoachBuilder.build(true, readiness(ReadinessLevel.READY))
        )

        listOf(
            "로컬 전용",
            "무상태 수동 안내",
            "새 저장소·자동 처리·자동 전송·스케줄러를 만들지 않습니다",
            "pref_simple_mode 불리언 값",
            "연락처·통화 기록·문자·SMS·클립보드·알림을 읽거나 감시하지 않고",
            "네트워크·웹 탐색·브라우저·외부 링크 열기를 사용하지 않고",
            "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러·브로드캐스트 " +
                "수신기를 만들거나 예약하지 않습니다",
            "새 Android 권한을 요청하지 않으며",
            "전화 연결은 항상 Intent.ACTION_DIAL로 전화 앱만 열고 통화는 사용자가 직접 " +
                "시작합니다",
            "Configuration.fontScale, AccessibilityManager, WindowManager, " +
                "TextToSpeech, TtsSpan"
        ).forEach { boundary ->
            assertTrue("privacy copy omitted $boundary", copy.contains(boundary))
        }
        assertFalse(copy.contains("자동으로 글자 크게"))
    }

    @Test
    fun modelSourceIsPureAndReadsOnlyApprovedCategoryInputs() {
        val source = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/AccessibilityCoach.kt"
        )

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(source))
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(source).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(source).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)

        assertFalse(source.contains("MediaStore"))
        assertFalse(source.contains("ContentObserver"))

        val implementationSource = stripStringLiterals(source)
        listOf(
            "Configuration",
            "TextToSpeech",
            "TtsSpan",
            "AccessibilityManager",
            "AlarmManager",
            "NotificationManager",
            "NotificationCompat",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "ForegroundService",
            "BroadcastReceiver",
            "ContactsContract",
            "SmsManager",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "MediaStore",
            "ContentObserver",
            "FLAG_SECURE"
        ).forEach { forbiddenApi ->
            assertFalse(
                "accessibility coach model used forbidden API: $forbiddenApi",
                implementationSource.contains(forbiddenApi)
            )
        }
    }

    @Test
    fun boundedUiSliceUsesOnlyCountReadinessConsentRoutesAndManualTextShare() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val screenSource = functionBlock(
            activitySource,
            "private fun LinearLayout.accessibilityCoachScreen"
        )
        val cardSource = functionBlock(
            activitySource,
            "private fun LinearLayout.accessibilityCoachCard"
        )
        val shareSource = functionBlock(
            activitySource,
            "private fun shareAccessibilityCoach"
        )

        assertTrue(screenSource.contains("incidents.count()"))
        assertTrue(screenSource.contains("FamilyReadinessBuilder.build(safetyPlan.load())"))
        assertTrue(screenSource.contains("prefs.getBoolean(KEY_CONSENTED, false)"))
        assertFalse(screenSource.contains("currentAnalysis?.input"))
        assertFalse(screenSource.contains("currentAnalysis?.score"))

        listOf(
            "card.title",
            "card.whyItMatters",
            "card.inAppSteps",
            "card.systemSettingSteps",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedField -> assertTrue(cardSource.contains(renderedField)) }

        assertTrue(
            screenSource.contains("shareAccessibilityCoach(coach.familyShareSummary)")
        )
        assertTrue(shareSource.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareSource.contains("type = \"text/plain\""))
        assertTrue(shareSource.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertFalse(shareSource.contains("Intent.EXTRA_STREAM"))
        assertFalse(shareSource.contains("Uri.parse"))

        val boundedImplementationSource = stripStringLiterals(screenSource + cardSource + shareSource)
        listOf(
            "Intent.ACTION_VIEW",
            "Intent.ACTION_CALL",
            "ClipboardManager",
            "MediaStore",
            "ContentObserver",
            "prefs.edit(",
            "safetyPlan.save(",
            "incidents.add(",
            "incidents.clear("
        ).forEach { forbiddenApi ->
            assertFalse(
                "bounded accessibility coach UI used forbidden implementation: $forbiddenApi",
                boundedImplementationSource.contains(forbiddenApi, ignoreCase = true)
            )
        }
    }

    @Test
    fun simpleModeHomePathUsesOnlyExistingActionDialNeverActionCall() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val simpleModeHomeSource = functionBlock(
            activitySource,
            "private fun LinearLayout.simpleModeHome"
        )
        val dialSource = functionBlock(activitySource, "private fun dial(")

        assertFalse(simpleModeHomeSource.contains("ACTION_CALL"))
        assertTrue(dialSource.contains("Intent.ACTION_DIAL"))
        assertFalse(dialSource.contains("Intent.ACTION_CALL"))
        assertTrue(simpleModeHomeSource.contains("openFamilyCallbackCard(currentAnalysis)"))
        assertTrue(simpleModeHomeSource.contains("show(Screen.CHECKER)"))
        assertTrue(simpleModeHomeSource.contains("openHelpDirectory(Screen.HOME)"))
    }

    @Test
    fun simpleModePreferenceWritesOnlyExistingSharedPreferencesSingleBooleanKey() {
        val source = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SimpleModePreference.kt"
        )

        assertTrue(source.contains("getSharedPreferences(\"family_shield\", Context.MODE_PRIVATE)"))
        assertTrue(source.contains("KEY_SIMPLE_MODE = \"pref_simple_mode\""))
        assertEquals(1, Regex("putBoolean\\(").findAll(source).count())
        assertEquals(1, Regex("getBoolean\\(").findAll(source).count())
        assertFalse(source.contains("putString("))
        assertFalse(source.contains("putInt("))
        assertFalse(source.contains(".clear("))
        assertFalse(source.contains(".remove("))
    }

    @Test
    fun privacyPostureRegressionForbidsInvasiveApisAcrossMainSource() {
        val root = projectRoot()
        val mainSourceDir = File(root, "app/src/main/java")
        val manifest = File(root, "app/src/main/AndroidManifest.xml").readText()

        assertFalse(manifest.contains("<uses-permission"))
        assertTrue(manifest.contains("android:allowBackup=\"false\""))

        val forbiddenApis = listOf(
            "CALL_PHONE",
            "READ_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "RECORD_AUDIO",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "AlarmManager",
            "NotificationManager",
            "NotificationCompat",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "ForegroundService",
            "BroadcastReceiver",
            "ContactsContract",
            "SmsManager",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "MediaStore",
            "ContentObserver",
            "FLAG_SECURE"
        )
        mainSourceDir.walkTopDown()
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

    private fun assertNoPrivateValues(coach: AccessibilityCoach, vararg privateValues: String) {
        val copy = allCopy(coach)
        privateValues.forEach { privateValue ->
            assertFalse("coach leaked $privateValue", copy.contains(privateValue))
        }
    }

    private fun readiness(
        level: ReadinessLevel,
        privateLiteral: String = "PRIVATE_READINESS_LITERAL"
    ): FamilyReadiness = FamilyReadiness(
        level = level,
        title = "TITLE_$privateLiteral",
        completedCount = 919,
        totalCount = 937,
        checklist = listOf(
            ReadinessItem(
                label = "LABEL_$privateLiteral",
                done = true,
                action = "ACTION_$privateLiteral"
            )
        ),
        nextAction = "NEXT_$privateLiteral"
    )

    private fun allCopy(coach: AccessibilityCoach): String = buildString {
        appendLine(coach.title)
        appendLine(coach.intro)
        appendLine(coach.modeLabel)
        appendLine(coach.recommendationTitle)
        appendLine(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> appendLine(action.label) }
        coach.cards.forEach { card ->
            appendLine(card.title)
            appendLine(card.whyItMatters)
            card.inAppSteps.forEach(::appendLine)
            card.systemSettingSteps.forEach(::appendLine)
            card.routeActions.forEach { action -> appendLine(action.label) }
        }
        coach.privacySafetyNotes.forEach(::appendLine)
        append(coach.familyShareSummary)
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
