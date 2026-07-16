package com.mongmong.familyshield

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPostureRegressionTest {
    @Test
    fun manifestDeclaresNoPermissionsAndDisablesCloudBackup() {
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        listOf(
            "android.permission.READ_SMS",
            "android.permission.CALL_PHONE",
            "android.permission.READ_CALL_LOG",
            "android.permission.READ_CONTACTS",
            "android.permission.RECORD_AUDIO",
            "android.permission.INTERNET"
        ).forEach { permission ->
            assertFalse("manifest declared forbidden permission: $permission", manifest.contains(permission))
        }
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun phoneActionsUseDialerIntentAndNeverDirectCallIntent() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )

        assertTrue(activitySource.contains("Intent(Intent.ACTION_DIAL"))
        assertFalse(activitySource.contains("Intent.ACTION_CALL"))
        assertFalse(activitySource.contains("android.permission.CALL_PHONE"))
    }

    @Test
    fun quickStopIsPureStatelessDelegatesToExistingFlowsAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/QuickStopMode.kt"
        )
        val homeScreen = activitySource
            .substringAfter("private fun LinearLayout.home()")
            .substringBefore("private fun LinearLayout.quickStopModeScreen")
        val quickStopScreen = activitySource
            .substringAfter("private fun LinearLayout.quickStopModeScreen")
            .substringBefore("private fun openQuickStopAction")
        val quickStopActionRouter = activitySource
            .substringAfter("private fun openQuickStopAction")
            .substringBefore("private fun LinearLayout.safetyFlowRouterScreen")
        val quickStopShareHelper = activitySource
            .substringAfter("private fun shareQuickStopSummary")
            .substringBefore("private fun shareSafetyReport")
        val normalizedActionRouter = quickStopActionRouter.replace(Regex("\\s+"), " ")

        assertFalse(Regex("(?m)^\\s*import\\s+android\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))

        assertTrue(activitySource.contains("QUICK_STOP"))
        assertTrue(
            activitySource.contains(
                "Screen.QUICK_STOP -> quickStopModeScreen()"
            )
        )
        assertTrue(
            homeScreen.contains(
                "primaryButton(\"긴급 멈춤 모드\") { show(Screen.QUICK_STOP) }"
            )
        )
        assertTrue(
            homeScreen.indexOf("긴급 멈춤 모드") in 0 until
                homeScreen.indexOf("지금 뭐부터 할지 고르기")
        )

        assertTrue(quickStopScreen.contains("QuickStopModeBuilder.build("))
        assertTrue(quickStopScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            quickStopScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(quickStopScreen.contains("mode.recommendedCards"))
        assertTrue(quickStopScreen.contains("mode.cards"))
        assertTrue(
            quickStopScreen.contains(
                "shareQuickStopSummary(mode.familyShareSummary)"
            )
        )
        assertTrue(quickStopScreen.contains("primaryButton(\"가족에게 멈춤 안내 공유\")"))
        assertTrue(quickStopScreen.contains("secondaryButton(\"홈으로\")"))
        assertFalse(quickStopScreen.contains("analysis.input"))
        assertFalse(quickStopScreen.contains("IncidentPreviewRedactor"))
        assertFalse(quickStopScreen.contains("incidents.add("))
        assertFalse(quickStopScreen.contains("incidents.clear("))
        assertFalse(quickStopScreen.contains("prefs.edit("))
        assertFalse(quickStopScreen.contains("safetyPlan.save("))
        assertFalse(quickStopScreen.contains("CheckBox("))
        assertFalse(quickStopScreen.contains("EditText("))
        assertFalse(quickStopScreen.contains("currentAnalysis ="))
        assertFalse(quickStopScreen.contains(".save("))
        assertFalse(quickStopScreen.contains("putString("))
        assertFalse(quickStopScreen.contains("putBoolean("))

        assertTrue(
            normalizedActionRouter.contains(
                "QuickStopActionId.STOP_SCRIPT -> show(Screen.QUICK_STOP)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "QuickStopActionId.FAMILY_CALLBACK_CARD -> " +
                    "openFamilyCallbackCard(currentAnalysis)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "QuickStopActionId.SAFETY_PLAN -> show(Screen.PLAN)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "QuickStopActionId.CHECKER -> show(Screen.CHECKER)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "QuickStopActionId.ANALYSIS_RESULT -> " +
                    "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "QuickStopActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "QuickStopActionId.RESPONSE_CENTER -> openResponseCenter()"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "QuickStopActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "QuickStopActionId.OFFICIAL_HELP_DIRECTORY -> " +
                    "openHelpDirectory(Screen.QUICK_STOP)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "QuickStopActionId.EVIDENCE_KIT -> openEvidenceKit(null)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "QuickStopActionId.FAMILY_ALERT_MESSAGES -> show(Screen.FAMILY_ALERT_MESSAGES)"
            )
        )
        assertFalse(quickStopActionRouter.contains("dial("))
        assertFalse(quickStopActionRouter.contains("Intent.ACTION_DIAL"))
        assertFalse(quickStopActionRouter.contains("Intent.ACTION_CALL"))

        val quickStopShareArguments = Regex("shareQuickStopSummary\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("mode.familyShareSummary"), quickStopShareArguments)
        assertTrue(quickStopShareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(quickStopShareHelper.contains("type = \"text/plain\""))
        assertTrue(quickStopShareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(quickStopShareHelper.contains("Intent.createChooser"))
        assertEquals(
            1,
            Regex("putExtra\\(Intent\\.EXTRA_TEXT, summary\\)")
                .findAll(quickStopShareHelper)
                .count()
        )
        assertFalse(quickStopShareHelper.contains("Intent.EXTRA_SUBJECT"))
        assertFalse(quickStopShareHelper.contains("Intent.EXTRA_STREAM"))
        assertFalse(quickStopShareHelper.contains("ClipData"))
        assertFalse(quickStopShareHelper.contains("FLAG_GRANT_READ_URI_PERMISSION"))
        assertFalse(quickStopShareHelper.contains("FLAG_GRANT_WRITE_URI_PERMISSION"))
        assertFalse(quickStopShareHelper.contains("ScamAnalysis"))
        assertFalse(quickStopShareHelper.contains("currentAnalysis"))
        assertFalse(quickStopShareHelper.contains("analysis.input"))
        assertFalse(quickStopShareHelper.contains("IncidentPreviewRedactor"))
        assertFalse(quickStopShareHelper.contains("SafetyPlan"))
        assertFalse(quickStopShareHelper.contains("incidents"))
        assertFalse(quickStopShareHelper.contains("ContactsContract"))
        assertFalse(quickStopShareHelper.contains("SmsManager"))
    }

    @Test
    fun privacySharePassesOnlyBuilderSummaryThroughTextSend() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val privacyShareHelper = activitySource
            .substringAfter("private fun sharePrivacySummary")
            .substringBefore("private fun LinearLayout.riskCard")

        assertTrue(activitySource.contains("sharePrivacySummary(privacyCenter.familyShareSummary)"))
        assertTrue(privacyShareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(privacyShareHelper.contains("type = \"text/plain\""))
        assertTrue(privacyShareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertFalse(privacyShareHelper.contains("SafetyPlan"))
        assertFalse(privacyShareHelper.contains("ScamAnalysis"))
        assertFalse(privacyShareHelper.contains("incident.preview"))
    }

    @Test
    fun incidentBriefingReadsSavedLogsWithoutMutatingAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val briefingScreen = activitySource
            .substringAfter("private fun LinearLayout.incidentTimelineBriefingScreen")
            .substringBefore("private fun openIncidentBriefingAction")
        val briefingShareHelper = activitySource
            .substringAfter("private fun shareIncidentTimelineBriefing")
            .substringBefore("private fun sharePrivacySummary")

        assertTrue(briefingScreen.contains("incidents.load()"))
        assertTrue(
            briefingScreen.contains(
                "shareIncidentTimelineBriefing(briefing.familyShareSummary)"
            )
        )
        assertFalse(briefingScreen.contains("incidents.add("))
        assertFalse(briefingScreen.contains("incidents.clear("))
        assertFalse(briefingScreen.contains("prefs.edit("))

        assertTrue(briefingShareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(briefingShareHelper.contains("type = \"text/plain\""))
        assertTrue(briefingShareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertFalse(briefingShareHelper.contains("Incident"))
        assertFalse(briefingShareHelper.contains("ScamAnalysis"))
        assertFalse(briefingShareHelper.contains("preview"))
    }

    @Test
    fun accountLockdownIsStatelessDelegatesToExistingFlowsAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val lockdownScreen = activitySource
            .substringAfter("private fun LinearLayout.accountLockdownChecklistScreen")
            .substringBefore("private fun openAccountLockdownAction")
        val lockdownActionRouter = activitySource
            .substringAfter("private fun openAccountLockdownAction")
            .substringBefore("private fun LinearLayout.checker")
        val lockdownShareHelper = activitySource
            .substringAfter("private fun shareAccountLockdownChecklist")
            .substringBefore("private fun shareIncidentTimelineBriefing")
        val normalizedActionRouter = lockdownActionRouter.replace(Regex("\\s+"), " ")

        assertTrue(activitySource.contains("ACCOUNT_LOCKDOWN"))
        assertTrue(
            activitySource.contains(
                "secondaryButton(\"계정·기기 잠금 체크리스트\") { " +
                    "show(Screen.ACCOUNT_LOCKDOWN) }"
            )
        )
        assertTrue(
            activitySource.contains(
                "Screen.ACCOUNT_LOCKDOWN -> accountLockdownChecklistScreen()"
            )
        )
        assertTrue(
            lockdownScreen.contains(
                "AccountLockdownChecklistBuilder.build(currentAnalysis)"
            )
        )
        assertTrue(
            lockdownScreen.contains(
                "shareAccountLockdownChecklist(checklist.familyShareSummary)"
            )
        )
        assertTrue(lockdownScreen.contains("secondaryButton(\"홈으로\")"))
        assertFalse(lockdownScreen.contains("incidents.add("))
        assertFalse(lockdownScreen.contains("incidents.clear("))
        assertFalse(lockdownScreen.contains("prefs.edit("))
        assertFalse(lockdownScreen.contains("safetyPlan.save("))
        assertFalse(lockdownScreen.contains("CheckBox("))
        assertFalse(lockdownScreen.contains("currentAnalysis ="))
        assertFalse(lockdownScreen.contains(".save("))
        assertFalse(lockdownScreen.contains("putString("))
        assertFalse(lockdownScreen.contains("putBoolean("))

        assertTrue(
            normalizedActionRouter.contains(
                "AccountLockdownActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "AccountLockdownActionId.FAMILY_CALLBACK_CARD -> " +
                    "openFamilyCallbackCard(currentAnalysis)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "AccountLockdownActionId.RESPONSE_CENTER -> " +
                    "openResponseCenter(action.responseSituation)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "AccountLockdownActionId.EVIDENCE_KIT -> " +
                    "openEvidenceKit(action.evidenceSituation)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "AccountLockdownActionId.OFFICIAL_HELP_DIRECTORY -> " +
                    "openHelpDirectory(Screen.ACCOUNT_LOCKDOWN)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "AccountLockdownActionId.REPORT_PREP -> " +
                    "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "AccountLockdownActionId.NEW_ANALYSIS -> show(Screen.CHECKER)"
            )
        )

        assertTrue(lockdownShareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(lockdownShareHelper.contains("type = \"text/plain\""))
        assertTrue(lockdownShareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertEquals(
            1,
            Regex("putExtra\\(Intent\\.EXTRA_TEXT, summary\\)")
                .findAll(lockdownShareHelper)
                .count()
        )
        assertFalse(lockdownShareHelper.contains("Intent.EXTRA_STREAM"))
        assertFalse(lockdownShareHelper.contains("ClipData"))
        assertFalse(lockdownShareHelper.contains("FLAG_GRANT_READ_URI_PERMISSION"))
        assertFalse(lockdownShareHelper.contains("FLAG_GRANT_WRITE_URI_PERMISSION"))
        assertFalse(lockdownShareHelper.contains("ScamAnalysis"))
        assertFalse(lockdownShareHelper.contains("currentAnalysis"))
        assertFalse(lockdownShareHelper.contains("analysis.input"))
        assertFalse(lockdownShareHelper.contains("IncidentPreviewRedactor"))
        assertFalse(lockdownShareHelper.contains("SafetyPlan"))
        assertFalse(lockdownShareHelper.contains("incidents"))
    }

    @Test
    fun familyAlertMessagesAreStatelessUseExistingFlowsAndShareOnlyBuilderText() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val alertScreen = activitySource
            .substringAfter("private fun LinearLayout.familyAlertMessagePackScreen")
            .substringBefore("private fun openFamilyAlertAction")
        val alertActionRouter = activitySource
            .substringAfter("private fun openFamilyAlertAction")
            .substringBefore("private fun LinearLayout.accountLockdownChecklistScreen")
        val alertShareHelper = activitySource
            .substringAfter("private fun shareFamilyAlertText")
            .substringBefore("private fun shareAccountLockdownChecklist")
        val normalizedActionRouter = alertActionRouter.replace(Regex("\\s+"), " ")

        assertTrue(activitySource.contains("FAMILY_ALERT_MESSAGES"))
        assertTrue(
            activitySource.contains(
                "secondaryButton(\"가족 경고 메시지 만들기\") { " +
                    "show(Screen.FAMILY_ALERT_MESSAGES) }"
            )
        )
        assertTrue(
            activitySource.contains(
                "Screen.FAMILY_ALERT_MESSAGES -> familyAlertMessagePackScreen()"
            )
        )
        assertTrue(alertScreen.contains("FamilyAlertMessagePackBuilder.build("))
        assertTrue(alertScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            alertScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(alertScreen.contains("shareFamilyAlertText(template.messageBody)"))
        assertTrue(alertScreen.contains("shareFamilyAlertText(pack.combinedShareSummary)"))
        val alertShareArguments = Regex("shareFamilyAlertText\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(
            listOf(
                "template.messageBody",
                "template.messageBody",
                "pack.combinedShareSummary"
            ),
            alertShareArguments
        )
        assertTrue(alertScreen.contains("secondaryButton(\"홈으로\")"))
        assertFalse(alertScreen.contains("analysis.input"))
        assertFalse(alertScreen.contains("IncidentPreviewRedactor"))
        assertFalse(alertScreen.contains("incidents.add("))
        assertFalse(alertScreen.contains("incidents.clear("))
        assertFalse(alertScreen.contains("prefs.edit("))
        assertFalse(alertScreen.contains("safetyPlan.save("))
        assertFalse(alertScreen.contains("CheckBox("))
        assertFalse(alertScreen.contains("currentAnalysis ="))
        assertFalse(alertScreen.contains(".save("))
        assertFalse(alertScreen.contains("putString("))
        assertFalse(alertScreen.contains("putBoolean("))

        assertTrue(
            normalizedActionRouter.contains(
                "FamilyAlertActionId.FAMILY_CALLBACK_CARD -> " +
                    "openFamilyCallbackCard(currentAnalysis)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "FamilyAlertActionId.SAFETY_PLAN -> show(Screen.PLAN)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "FamilyAlertActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "FamilyAlertActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "FamilyAlertActionId.EVIDENCE_KIT -> openEvidenceKit(null)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "FamilyAlertActionId.OFFICIAL_HELP_DIRECTORY -> " +
                    "openHelpDirectory(Screen.FAMILY_ALERT_MESSAGES)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "FamilyAlertActionId.REPORT_PREP -> " +
                    "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "FamilyAlertActionId.NEW_ANALYSIS -> show(Screen.CHECKER)"
            )
        )

        assertTrue(alertShareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(alertShareHelper.contains("type = \"text/plain\""))
        assertTrue(alertShareHelper.contains("putExtra(Intent.EXTRA_TEXT, text)"))
        assertTrue(alertShareHelper.contains("Intent.createChooser"))
        assertEquals(
            1,
            Regex("putExtra\\(Intent\\.EXTRA_TEXT, text\\)")
                .findAll(alertShareHelper)
                .count()
        )
        assertFalse(alertShareHelper.contains("Intent.EXTRA_SUBJECT"))
        assertFalse(alertShareHelper.contains("Intent.EXTRA_STREAM"))
        assertFalse(alertShareHelper.contains("ClipData"))
        assertFalse(alertShareHelper.contains("FLAG_GRANT_READ_URI_PERMISSION"))
        assertFalse(alertShareHelper.contains("FLAG_GRANT_WRITE_URI_PERMISSION"))
        assertFalse(alertShareHelper.contains("ScamAnalysis"))
        assertFalse(alertShareHelper.contains("currentAnalysis"))
        assertFalse(alertShareHelper.contains("analysis.input"))
        assertFalse(alertShareHelper.contains("IncidentPreviewRedactor"))
        assertFalse(alertShareHelper.contains("SafetyPlan"))
        assertFalse(alertShareHelper.contains("incidents"))
        assertFalse(alertShareHelper.contains("ContactsContract"))
        assertFalse(alertShareHelper.contains("SmsManager"))
    }

    private fun readProjectFile(relativePath: String): String {
        val userDirectory = requireNotNull(System.getProperty("user.dir"))
        var directory = File(userDirectory).absoluteFile
        while (true) {
            val candidate = File(directory, relativePath)
            if (candidate.isFile) return candidate.readText()
            directory = directory.parentFile
                ?: error("Could not find project file from $userDirectory: $relativePath")
        }
    }
}
