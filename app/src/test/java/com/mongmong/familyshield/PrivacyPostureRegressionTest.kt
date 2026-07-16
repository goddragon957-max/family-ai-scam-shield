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
