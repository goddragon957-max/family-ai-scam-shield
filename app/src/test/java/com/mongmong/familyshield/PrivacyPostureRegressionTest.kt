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
            "android.permission.INTERNET",
            "android.permission.POST_NOTIFICATIONS",
            "android.permission.SCHEDULE_EXACT_ALARM",
            "android.permission.RECEIVE_BOOT_COMPLETED"
        ).forEach { permission ->
            assertFalse("manifest declared forbidden permission: $permission", manifest.contains(permission))
        }
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
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

    @Test
    fun recoveryPlanIsStatelessDelegatesToExistingFlowsAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/RecoveryPlan24h.kt"
        )
        val homeScreen = activitySource
            .substringAfter("private fun LinearLayout.home()")
            .substringBefore("private fun LinearLayout.quickStopModeScreen")
        val recoveryPlanScreen = activitySource
            .substringAfter("private fun LinearLayout.recoveryPlan24hScreen")
            .substringBefore("private fun openRecoveryPlanAction")
        val recoveryPlanActionRouter = activitySource
            .substringAfter("private fun openRecoveryPlanAction")
            .substringBefore("private fun LinearLayout.checker")
        val recoveryPlanShareHelper = activitySource
            .substringAfter("private fun shareRecoveryPlanSummary")
            .substringBefore("private fun LinearLayout.riskCard")
        val normalizedActionRouter = recoveryPlanActionRouter.replace(Regex("\\s+"), " ")

        assertFalse(Regex("(?m)^\\s*import\\s+android\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))

        assertTrue(activitySource.contains("RECOVERY_PLAN"))
        assertTrue(
            activitySource.contains(
                "Screen.RECOVERY_PLAN -> recoveryPlan24hScreen()"
            )
        )
        assertTrue(
            homeScreen.contains(
                "secondaryButton(\"24시간 수습 계획\") { show(Screen.RECOVERY_PLAN) }"
            )
        )

        assertTrue(recoveryPlanScreen.contains("RecoveryPlan24hBuilder.build("))
        assertTrue(recoveryPlanScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            recoveryPlanScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(recoveryPlanScreen.contains("plan.recommendedWindows"))
        assertTrue(recoveryPlanScreen.contains("plan.windows"))
        assertTrue(
            recoveryPlanScreen.contains(
                "shareRecoveryPlanSummary(plan.familyShareSummary)"
            )
        )
        assertTrue(recoveryPlanScreen.contains("primaryButton(\"가족에게 수습 계획 공유\")"))
        assertTrue(recoveryPlanScreen.contains("secondaryButton(\"홈으로\")"))
        assertFalse(recoveryPlanScreen.contains("analysis.input"))
        assertFalse(recoveryPlanScreen.contains("IncidentPreviewRedactor"))
        assertFalse(recoveryPlanScreen.contains("incidents.add("))
        assertFalse(recoveryPlanScreen.contains("incidents.clear("))
        assertFalse(recoveryPlanScreen.contains("prefs.edit("))
        assertFalse(recoveryPlanScreen.contains("safetyPlan.save("))
        assertFalse(recoveryPlanScreen.contains("CheckBox("))
        assertFalse(recoveryPlanScreen.contains("EditText("))
        assertFalse(recoveryPlanScreen.contains("currentAnalysis ="))
        assertFalse(recoveryPlanScreen.contains(".save("))
        assertFalse(recoveryPlanScreen.contains("putString("))
        assertFalse(recoveryPlanScreen.contains("putBoolean("))

        assertTrue(
            normalizedActionRouter.contains(
                "RecoveryActionId.QUICK_STOP -> show(Screen.QUICK_STOP)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "RecoveryActionId.FAMILY_CALLBACK_CARD -> " +
                    "openFamilyCallbackCard(currentAnalysis)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "RecoveryActionId.CHECKER -> show(Screen.CHECKER)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "RecoveryActionId.ANALYSIS_RESULT -> " +
                    "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "RecoveryActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "RecoveryActionId.RESPONSE_CENTER -> openResponseCenter()"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "RecoveryActionId.FAMILY_ALERT_MESSAGES -> show(Screen.FAMILY_ALERT_MESSAGES)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "RecoveryActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "RecoveryActionId.EVIDENCE_KIT -> openEvidenceKit(null)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "RecoveryActionId.OFFICIAL_HELP_DIRECTORY -> " +
                    "openHelpDirectory(Screen.RECOVERY_PLAN)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "RecoveryActionId.REPORT_PREP -> " +
                    "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "RecoveryActionId.NEW_ANALYSIS -> show(Screen.CHECKER)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "RecoveryActionId.SAFETY_PLAN -> show(Screen.PLAN)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "RecoveryActionId.TIMELINE_BRIEFING -> show(Screen.TIMELINE_BRIEFING)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "RecoveryActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)"
            )
        )
        assertFalse(recoveryPlanActionRouter.contains("dial("))
        assertFalse(recoveryPlanActionRouter.contains("Intent.ACTION_DIAL"))
        assertFalse(recoveryPlanActionRouter.contains("Intent.ACTION_CALL"))

        val recoveryPlanShareArguments = Regex("shareRecoveryPlanSummary\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("plan.familyShareSummary"), recoveryPlanShareArguments)
        assertTrue(recoveryPlanShareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(recoveryPlanShareHelper.contains("type = \"text/plain\""))
        assertTrue(recoveryPlanShareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(recoveryPlanShareHelper.contains("Intent.createChooser"))
        assertEquals(
            1,
            Regex("putExtra\\(Intent\\.EXTRA_TEXT, summary\\)")
                .findAll(recoveryPlanShareHelper)
                .count()
        )
        assertFalse(recoveryPlanShareHelper.contains("Intent.EXTRA_STREAM"))
        assertFalse(recoveryPlanShareHelper.contains("ClipData"))
        assertFalse(recoveryPlanShareHelper.contains("FLAG_GRANT_READ_URI_PERMISSION"))
        assertFalse(recoveryPlanShareHelper.contains("FLAG_GRANT_WRITE_URI_PERMISSION"))
        assertFalse(recoveryPlanShareHelper.contains("ScamAnalysis"))
        assertFalse(recoveryPlanShareHelper.contains("currentAnalysis"))
        assertFalse(recoveryPlanShareHelper.contains("analysis.input"))
        assertFalse(recoveryPlanShareHelper.contains("IncidentPreviewRedactor"))
        assertFalse(recoveryPlanShareHelper.contains("SafetyPlan"))
        assertFalse(recoveryPlanShareHelper.contains("incidents"))
        assertFalse(recoveryPlanShareHelper.contains("ContactsContract"))
        assertFalse(recoveryPlanShareHelper.contains("SmsManager"))
    }

    @Test
    fun trustedHelperHandoffIsStatelessDelegatesToExistingFlowsAndSharesOnlyBuilderMessage() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/TrustedHelperHandoffCard.kt"
        )
        val homeScreen = activitySource
            .substringAfter("private fun LinearLayout.home()")
            .substringBefore("private fun LinearLayout.quickStopModeScreen")
        val handoffScreen = activitySource
            .substringAfter("private fun LinearLayout.trustedHelperHandoffScreen")
            .substringBefore("private fun openTrustedHelperAction")
        val handoffActionRouter = activitySource
            .substringAfter("private fun openTrustedHelperAction")
            .substringBefore("private fun shareTrustedHelperHandoff")
        val handoffShareHelper = activitySource
            .substringAfter("private fun shareTrustedHelperHandoff")
            .substringBefore("private fun LinearLayout.title")
        val normalizedActionRouter = handoffActionRouter.replace(Regex("\\s+"), " ")

        assertFalse(Regex("(?m)^\\s*import\\s+android\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))

        assertTrue(activitySource.contains("TRUSTED_HELPER_HANDOFF"))
        assertTrue(
            activitySource.contains(
                "Screen.TRUSTED_HELPER_HANDOFF -> trustedHelperHandoffScreen()"
            )
        )
        assertTrue(
            homeScreen.contains(
                "secondaryButton(\"보호자 도움 요청 카드\") { show(Screen.TRUSTED_HELPER_HANDOFF) }"
            )
        )

        assertTrue(handoffScreen.contains("TrustedHelperHandoffBuilder.build("))
        assertTrue(handoffScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            handoffScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(handoffScreen.contains("card.recommendedCards"))
        assertTrue(handoffScreen.contains("card.cards"))
        assertTrue(
            handoffScreen.contains(
                "shareTrustedHelperHandoff(card.handoffShareMessage)"
            )
        )
        assertTrue(handoffScreen.contains("primaryButton(\"보호자에게 도움 요청 공유\")"))
        assertTrue(handoffScreen.contains("secondaryButton(\"홈으로\")"))
        assertFalse(handoffScreen.contains("analysis.input"))
        assertFalse(handoffScreen.contains("IncidentPreviewRedactor"))
        assertFalse(handoffScreen.contains("incidents.add("))
        assertFalse(handoffScreen.contains("incidents.clear("))
        assertFalse(handoffScreen.contains("prefs.edit("))
        assertFalse(handoffScreen.contains("safetyPlan.save("))
        assertFalse(handoffScreen.contains("CheckBox("))
        assertFalse(handoffScreen.contains("EditText("))
        assertFalse(handoffScreen.contains("currentAnalysis ="))
        assertFalse(handoffScreen.contains(".save("))
        assertFalse(handoffScreen.contains("putString("))
        assertFalse(handoffScreen.contains("putBoolean("))

        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.QUICK_STOP -> show(Screen.QUICK_STOP)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.FAMILY_CALLBACK_CARD -> " +
                    "openFamilyCallbackCard(currentAnalysis)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.CHECKER -> show(Screen.CHECKER)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.ANALYSIS_RESULT -> " +
                    "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.SAFETY_PLAN -> show(Screen.PLAN)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.RESPONSE_CENTER -> openResponseCenter()"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.EVIDENCE_KIT -> openEvidenceKit(null)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.OFFICIAL_HELP_DIRECTORY -> " +
                    "openHelpDirectory(Screen.TRUSTED_HELPER_HANDOFF)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.REPORT_PREP -> " +
                    "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.NEW_ANALYSIS -> show(Screen.CHECKER)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.FAMILY_ALERT_MESSAGES -> show(Screen.FAMILY_ALERT_MESSAGES)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.TIMELINE_BRIEFING -> show(Screen.TIMELINE_BRIEFING)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "TrustedHelperActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)"
            )
        )
        assertFalse(handoffActionRouter.contains("dial("))
        assertFalse(handoffActionRouter.contains("Intent.ACTION_DIAL"))
        assertFalse(handoffActionRouter.contains("Intent.ACTION_CALL"))

        val handoffShareArguments = Regex("shareTrustedHelperHandoff\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("card.handoffShareMessage"), handoffShareArguments)
        assertTrue(handoffShareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(handoffShareHelper.contains("type = \"text/plain\""))
        assertTrue(handoffShareHelper.contains("putExtra(Intent.EXTRA_TEXT, message)"))
        assertTrue(handoffShareHelper.contains("Intent.createChooser"))
        assertEquals(
            1,
            Regex("putExtra\\(Intent\\.EXTRA_TEXT, message\\)")
                .findAll(handoffShareHelper)
                .count()
        )
        assertFalse(handoffShareHelper.contains("Intent.EXTRA_SUBJECT"))
        assertFalse(handoffShareHelper.contains("Intent.EXTRA_STREAM"))
        assertFalse(handoffShareHelper.contains("ClipData"))
        assertFalse(handoffShareHelper.contains("FLAG_GRANT_READ_URI_PERMISSION"))
        assertFalse(handoffShareHelper.contains("FLAG_GRANT_WRITE_URI_PERMISSION"))
        assertFalse(handoffShareHelper.contains("ScamAnalysis"))
        assertFalse(handoffShareHelper.contains("currentAnalysis"))
        assertFalse(handoffShareHelper.contains("analysis.input"))
        assertFalse(handoffShareHelper.contains("IncidentPreviewRedactor"))
        assertFalse(handoffShareHelper.contains("SafetyPlan"))
        assertFalse(handoffShareHelper.contains("incidents"))
        assertFalse(handoffShareHelper.contains("ContactsContract"))
        assertFalse(handoffShareHelper.contains("SmsManager"))
    }

    @Test
    fun safeCallScriptsIsStatelessDelegatesToExistingFlowsAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafeVerificationCallScripts.kt"
        )
        val homeScreen = activitySource
            .substringAfter("private fun LinearLayout.home()")
            .substringBefore("private fun LinearLayout.quickStopModeScreen")
        val safeScriptsScreen = activitySource
            .substringAfter("private fun LinearLayout.safeVerificationCallScriptsScreen")
            .substringBefore("private fun openSafeCallScriptAction")
        val safeScriptsActionRouter = activitySource
            .substringAfter("private fun openSafeCallScriptAction")
            .substringBefore("private fun shareSafeCallScriptsSummary")
        val safeScriptsShareHelper = activitySource
            .substringAfter("private fun shareSafeCallScriptsSummary")
            .substringBefore("private fun LinearLayout.exposureImpactTriageScreen")
        val normalizedActionRouter = safeScriptsActionRouter.replace(Regex("\\s+"), " ")

        assertFalse(Regex("(?m)^\\s*import\\s+android\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))

        assertTrue(activitySource.contains("SAFE_CALL_SCRIPTS"))
        assertTrue(
            activitySource.contains(
                "Screen.SAFE_CALL_SCRIPTS -> safeVerificationCallScriptsScreen()"
            )
        )
        assertTrue(
            homeScreen.contains(
                "secondaryButton(\"안전 확인 전화 스크립트\") { show(Screen.SAFE_CALL_SCRIPTS) }"
            )
        )

        assertTrue(safeScriptsScreen.contains("SafeVerificationCallScriptsBuilder.build("))
        assertTrue(safeScriptsScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            safeScriptsScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(safeScriptsScreen.contains("scripts.recommendedCards"))
        assertTrue(safeScriptsScreen.contains("scripts.cards"))
        assertTrue(
            safeScriptsScreen.contains(
                "shareSafeCallScriptsSummary(scripts.familyShareSummary)"
            )
        )
        assertTrue(safeScriptsScreen.contains("primaryButton(\"가족에게 전화 스크립트 공유\")"))
        assertTrue(safeScriptsScreen.contains("secondaryButton(\"홈으로\")"))
        assertFalse(safeScriptsScreen.contains("analysis.input"))
        assertFalse(safeScriptsScreen.contains("IncidentPreviewRedactor"))
        assertFalse(safeScriptsScreen.contains("incidents.add("))
        assertFalse(safeScriptsScreen.contains("incidents.clear("))
        assertFalse(safeScriptsScreen.contains("prefs.edit("))
        assertFalse(safeScriptsScreen.contains("safetyPlan.save("))
        assertFalse(safeScriptsScreen.contains("CheckBox("))
        assertFalse(safeScriptsScreen.contains("EditText("))
        assertFalse(safeScriptsScreen.contains("currentAnalysis ="))
        assertFalse(safeScriptsScreen.contains(".save("))
        assertFalse(safeScriptsScreen.contains("putString("))
        assertFalse(safeScriptsScreen.contains("putBoolean("))

        assertTrue(
            normalizedActionRouter.contains(
                "SafeCallScriptActionId.QUICK_STOP -> show(Screen.QUICK_STOP)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "SafeCallScriptActionId.FAMILY_CALLBACK_CARD -> " +
                    "openFamilyCallbackCard(currentAnalysis)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "SafeCallScriptActionId.CHECKER -> show(Screen.CHECKER)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "SafeCallScriptActionId.ANALYSIS_RESULT -> " +
                    "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "SafeCallScriptActionId.SAFETY_PLAN -> show(Screen.PLAN)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "SafeCallScriptActionId.TRUSTED_HELPER_HANDOFF -> show(Screen.TRUSTED_HELPER_HANDOFF)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "SafeCallScriptActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "SafeCallScriptActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "SafeCallScriptActionId.RESPONSE_CENTER -> openResponseCenter()"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "SafeCallScriptActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "SafeCallScriptActionId.EVIDENCE_KIT -> openEvidenceKit(null)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "SafeCallScriptActionId.OFFICIAL_HELP_DIRECTORY -> " +
                    "openHelpDirectory(Screen.SAFE_CALL_SCRIPTS)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "SafeCallScriptActionId.REPORT_PREP -> " +
                    "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "SafeCallScriptActionId.FAMILY_ALERT_MESSAGES -> show(Screen.FAMILY_ALERT_MESSAGES)"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "SafeCallScriptActionId.NEW_ANALYSIS -> show(Screen.CHECKER)"
            )
        )
        assertFalse(safeScriptsActionRouter.contains("dial("))
        assertFalse(safeScriptsActionRouter.contains("Intent.ACTION_DIAL"))
        assertFalse(safeScriptsActionRouter.contains("Intent.ACTION_CALL"))

        val safeScriptsShareArguments = Regex("shareSafeCallScriptsSummary\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("scripts.familyShareSummary"), safeScriptsShareArguments)
        assertTrue(safeScriptsShareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(safeScriptsShareHelper.contains("type = \"text/plain\""))
        assertTrue(safeScriptsShareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(safeScriptsShareHelper.contains("Intent.createChooser"))
        assertEquals(
            1,
            Regex("putExtra\\(Intent\\.EXTRA_TEXT, summary\\)")
                .findAll(safeScriptsShareHelper)
                .count()
        )
        assertFalse(safeScriptsShareHelper.contains("Intent.EXTRA_SUBJECT"))
        assertFalse(safeScriptsShareHelper.contains("Intent.EXTRA_STREAM"))
        assertFalse(safeScriptsShareHelper.contains("ClipData"))
        assertFalse(safeScriptsShareHelper.contains("FLAG_GRANT_READ_URI_PERMISSION"))
        assertFalse(safeScriptsShareHelper.contains("FLAG_GRANT_WRITE_URI_PERMISSION"))
        assertFalse(safeScriptsShareHelper.contains("ScamAnalysis"))
        assertFalse(safeScriptsShareHelper.contains("currentAnalysis"))
        assertFalse(safeScriptsShareHelper.contains("analysis.input"))
        assertFalse(safeScriptsShareHelper.contains("IncidentPreviewRedactor"))
        assertFalse(safeScriptsShareHelper.contains("SafetyPlan"))
        assertFalse(safeScriptsShareHelper.contains("incidents"))
        assertFalse(safeScriptsShareHelper.contains("ContactsContract"))
        assertFalse(safeScriptsShareHelper.contains("SmsManager"))
    }

    @Test
    fun riskSignalExplainersArePureStatelessDelegateToExistingFlowsAndShareOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/RiskSignalExplainer.kt"
        )
        val homeScreen = activitySource
            .substringAfter("private fun LinearLayout.home()")
            .substringBefore("private fun LinearLayout.quickStopModeScreen")
        val resultScreen = activitySource
            .substringAfter("private fun LinearLayout.result()")
            .substringBefore("private fun LinearLayout.incidentLog")
        val explainerScreen = activitySource
            .substringAfter("private fun LinearLayout.riskSignalExplainerScreen")
            .substringBefore("private fun openRiskSignalExplainerAction")
        val explainerActionRouter = activitySource
            .substringAfter("private fun openRiskSignalExplainerAction")
            .substringBefore("private fun shareRiskSignalExplainerSummary")
        val explainerShareHelper = activitySource
            .substringAfter("private fun shareRiskSignalExplainerSummary")
            .substringBefore("private fun LinearLayout.safeVerificationCallScriptsScreen")
        val normalizedActionRouter = explainerActionRouter.replace(Regex("\\s+"), " ")

        assertFalse(Regex("(?m)^\\s*import\\s+android\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))
        assertFalse(modelSource.contains("analysis.input"))
        assertFalse(modelSource.contains("IncidentPreviewRedactor"))

        assertTrue(activitySource.contains("RISK_SIGNAL_EXPLAINERS"))
        assertTrue(
            activitySource.contains(
                "Screen.RISK_SIGNAL_EXPLAINERS -> riskSignalExplainerScreen()"
            )
        )
        assertTrue(
            homeScreen.contains(
                "secondaryButton(\"위험 신호 설명 보기\") { show(Screen.RISK_SIGNAL_EXPLAINERS) }"
            )
        )
        assertTrue(
            resultScreen.contains(
                "secondaryButton(\"탐지 신호 설명 보기\") { show(Screen.RISK_SIGNAL_EXPLAINERS) }"
            )
        )

        assertTrue(explainerScreen.contains("RiskSignalExplainerBuilder.build("))
        assertTrue(explainerScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            explainerScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(explainerScreen.contains("explainer.recommendedCards"))
        assertTrue(explainerScreen.contains("explainer.cards"))
        assertTrue(explainerScreen.contains("explainer.privacySafetyNotes"))
        assertTrue(
            explainerScreen.contains(
                "shareRiskSignalExplainerSummary(explainer.familyShareSummary)"
            )
        )
        assertTrue(explainerScreen.contains("primaryButton(\"가족에게 위험 신호 설명 공유\")"))
        assertTrue(explainerScreen.contains("secondaryButton(\"분석 결과로 돌아가기\")"))
        assertTrue(explainerScreen.contains("secondaryButton(\"홈으로\")"))
        assertFalse(explainerScreen.contains("analysis.input"))
        assertFalse(explainerScreen.contains("IncidentPreviewRedactor"))
        assertFalse(explainerScreen.contains("incidents.add("))
        assertFalse(explainerScreen.contains("incidents.clear("))
        assertFalse(explainerScreen.contains("prefs.edit("))
        assertFalse(explainerScreen.contains("safetyPlan.save("))
        assertFalse(explainerScreen.contains("CheckBox("))
        assertFalse(explainerScreen.contains("EditText("))
        assertFalse(explainerScreen.contains("currentAnalysis ="))
        assertFalse(explainerScreen.contains(".save("))
        assertFalse(explainerScreen.contains("putString("))
        assertFalse(explainerScreen.contains("putBoolean("))

        listOf(
            "RiskSignalExplainerActionId.QUICK_STOP -> show(Screen.QUICK_STOP)",
            "RiskSignalExplainerActionId.CHECKER -> show(Screen.CHECKER)",
            "RiskSignalExplainerActionId.ANALYSIS_RESULT -> " +
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "RiskSignalExplainerActionId.FAMILY_CALLBACK_CARD -> " +
                "openFamilyCallbackCard(currentAnalysis)",
            "RiskSignalExplainerActionId.SAFETY_PLAN -> show(Screen.PLAN)",
            "RiskSignalExplainerActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)",
            "RiskSignalExplainerActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)",
            "RiskSignalExplainerActionId.EVIDENCE_KIT -> openEvidenceKit(null)",
            "RiskSignalExplainerActionId.OFFICIAL_HELP_DIRECTORY -> " +
                "openHelpDirectory(Screen.RISK_SIGNAL_EXPLAINERS)",
            "RiskSignalExplainerActionId.FAMILY_ALERT_MESSAGES -> show(Screen.FAMILY_ALERT_MESSAGES)",
            "RiskSignalExplainerActionId.TRUSTED_HELPER_HANDOFF -> " +
                "show(Screen.TRUSTED_HELPER_HANDOFF)",
            "RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)",
            "RiskSignalExplainerActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)"
        ).forEach { route ->
            assertTrue("risk signal explainer route omitted: $route", normalizedActionRouter.contains(route))
        }
        assertFalse(explainerActionRouter.contains("dial("))
        assertFalse(explainerActionRouter.contains("Intent.ACTION_DIAL"))
        assertFalse(explainerActionRouter.contains("Intent.ACTION_CALL"))

        val explainerShareArguments = Regex("shareRiskSignalExplainerSummary\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("explainer.familyShareSummary"), explainerShareArguments)
        assertTrue(explainerShareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(explainerShareHelper.contains("type = \"text/plain\""))
        assertTrue(explainerShareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(explainerShareHelper.contains("Intent.createChooser"))
        assertEquals(
            1,
            Regex("putExtra\\(Intent\\.EXTRA_TEXT, summary\\)")
                .findAll(explainerShareHelper)
                .count()
        )
        assertFalse(explainerShareHelper.contains("Intent.EXTRA_SUBJECT"))
        assertFalse(explainerShareHelper.contains("Intent.EXTRA_STREAM"))
        assertFalse(explainerShareHelper.contains("ClipData"))
        assertFalse(explainerShareHelper.contains("FLAG_GRANT_READ_URI_PERMISSION"))
        assertFalse(explainerShareHelper.contains("FLAG_GRANT_WRITE_URI_PERMISSION"))
        assertFalse(explainerShareHelper.contains("ScamAnalysis"))
        assertFalse(explainerShareHelper.contains("currentAnalysis"))
        assertFalse(explainerShareHelper.contains("analysis.input"))
        assertFalse(explainerShareHelper.contains("IncidentPreviewRedactor"))
        assertFalse(explainerShareHelper.contains("SafetyPlan"))
        assertFalse(explainerShareHelper.contains("incidents"))
        assertFalse(explainerShareHelper.contains("ContactsContract"))
        assertFalse(explainerShareHelper.contains("SmsManager"))
    }

    @Test
    fun familySafetyDrillCoachIsPureStatelessDelegatesToExistingFlowsAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/FamilySafetyDrillCoach.kt"
        )
        val readinessSection = activitySource
            .substringAfter("private fun LinearLayout.readinessSection")
            .substringBefore("private fun openDrill")
        val resultScreen = activitySource
            .substringAfter("private fun LinearLayout.result()")
            .substringBefore("private fun LinearLayout.incidentLog")
        val drillScreen = activitySource
            .substringAfter("private fun LinearLayout.drillScreen")
            .substringBefore("private fun LinearLayout.familySafetyDrillCard")
        val drillCardRenderer = activitySource
            .substringAfter("private fun LinearLayout.familySafetyDrillCard")
            .substringBefore("private fun openFamilySafetyDrillAction")
        val drillActionRouter = activitySource
            .substringAfter("private fun openFamilySafetyDrillAction")
            .substringBefore("private fun shareFamilySafetyDrillSummary")
        val drillShareHelper = activitySource
            .substringAfter("private fun shareFamilySafetyDrillSummary")
            .substringBefore("private fun openSampleLibrary")
        val normalizedActionRouter = drillActionRouter.replace(Regex("\\s+"), " ")

        assertFalse(Regex("(?m)^\\s*import\\s+android\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))
        assertFalse(modelSource.contains("analysis.input"))
        assertFalse(modelSource.contains("IncidentPreviewRedactor"))
        assertFalse(modelSource.contains("SafetyPlan"))
        assertFalse(modelSource.contains("SafetyPlanStore"))

        assertTrue(activitySource.contains("Screen.DRILL -> drillScreen()"))
        assertFalse(activitySource.contains("selectedDrillScenario"))
        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:drill|practice|pass|progress|history|score)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "KEY_\\w*(?:DRILL|PRACTICE|PASS|PROGRESS|HISTORY|SCORE)\\w*"
            ).containsMatchIn(activitySource)
        )
        assertTrue(
            readinessSection.contains(
                "secondaryButton(\"가족 안전 연습 코치\") { openDrill() }"
            )
        )
        assertTrue(
            resultScreen.contains(
                "secondaryButton(\"이 신호로 가족 연습하기\") { openDrill() }"
            )
        )

        assertTrue(drillScreen.contains("FamilySafetyDrillCoachBuilder.build("))
        assertTrue(drillScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            drillScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(drillScreen.contains("coach.recommendedCards"))
        assertTrue(drillScreen.contains("coach.cards"))
        assertTrue(drillScreen.contains("title(coach.title)"))
        assertTrue(drillScreen.contains("subtitle(coach.intro)"))
        assertTrue(drillScreen.contains("coach.fictionNotice"))
        assertTrue(drillScreen.contains("coach.privacySafetyNotes"))
        assertTrue(
            drillScreen.contains(
                "shareFamilySafetyDrillSummary(coach.familyShareSummary)"
            )
        )
        assertTrue(drillScreen.contains("primaryButton(\"가족에게 연습 과제 공유\")"))
        assertTrue(drillScreen.contains("secondaryButton(\"분석 결과로 돌아가기\")"))
        assertTrue(drillScreen.contains("secondaryButton(\"홈으로\")"))
        val recommendedRenderIndex = drillScreen.indexOf("coach.recommendedCards.forEach")
        val remainingRenderIndex = drillScreen.indexOf("val remainingCards = coach.cards.filterNot")
        assertTrue(recommendedRenderIndex >= 0)
        assertTrue(remainingRenderIndex > recommendedRenderIndex)
        assertEquals(
            1,
            Regex("coach\\.recommendedCards\\.forEach")
                .findAll(drillScreen)
                .count()
        )

        listOf(
            "card.title",
            "card.scenario",
            "card.fictionalSetup",
            "card.rolePlayLines",
            "card.expectedStopPhrase",
            "card.responseSteps",
            "card.passChecklist",
            "card.familyReflectionPrompt",
            "card.difficulty",
            "card.urgency",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { field ->
            assertTrue("drill UI omitted $field", drillCardRenderer.contains(field))
        }

        val statelessUiSource = drillScreen + drillCardRenderer
        assertFalse(statelessUiSource.contains("analysis.input"))
        assertFalse(statelessUiSource.contains("IncidentPreviewRedactor"))
        assertFalse(statelessUiSource.contains("plan.safeWord"))
        assertFalse(statelessUiSource.contains("plan.callbackScript"))
        assertFalse(statelessUiSource.contains("plan.contacts"))
        assertFalse(statelessUiSource.contains("incidents.add("))
        assertFalse(statelessUiSource.contains("incidents.clear("))
        assertFalse(statelessUiSource.contains("prefs.edit("))
        assertFalse(statelessUiSource.contains("safetyPlan.save("))
        assertFalse(statelessUiSource.contains("CheckBox("))
        assertFalse(statelessUiSource.contains("EditText("))
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))
        assertFalse(statelessUiSource.contains(".save("))
        assertFalse(statelessUiSource.contains("putString("))
        assertFalse(statelessUiSource.contains("putBoolean("))

        listOf(
            "FamilySafetyDrillActionId.QUICK_STOP -> show(Screen.QUICK_STOP)",
            "FamilySafetyDrillActionId.CHECKER -> show(Screen.CHECKER)",
            "FamilySafetyDrillActionId.ANALYSIS_RESULT -> " +
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "FamilySafetyDrillActionId.RISK_SIGNAL_EXPLAINERS -> " +
                "show(Screen.RISK_SIGNAL_EXPLAINERS)",
            "FamilySafetyDrillActionId.FAMILY_CALLBACK_CARD -> " +
                "openFamilyCallbackCard(currentAnalysis)",
            "FamilySafetyDrillActionId.SAFETY_PLAN -> show(Screen.PLAN)",
            "FamilySafetyDrillActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)",
            "FamilySafetyDrillActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)",
            "FamilySafetyDrillActionId.EVIDENCE_KIT -> openEvidenceKit(null)",
            "FamilySafetyDrillActionId.OFFICIAL_HELP_DIRECTORY -> " +
                "openHelpDirectory(Screen.DRILL)",
            "FamilySafetyDrillActionId.FAMILY_ALERT_MESSAGES -> " +
                "show(Screen.FAMILY_ALERT_MESSAGES)",
            "FamilySafetyDrillActionId.TRUSTED_HELPER_HANDOFF -> " +
                "show(Screen.TRUSTED_HELPER_HANDOFF)",
            "FamilySafetyDrillActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)",
            "FamilySafetyDrillActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)"
        ).forEach { route ->
            assertTrue("family drill route omitted: $route", normalizedActionRouter.contains(route))
        }
        assertFalse(drillActionRouter.contains("dial("))
        assertFalse(drillActionRouter.contains("Intent.ACTION_DIAL"))
        assertFalse(drillActionRouter.contains("Intent.ACTION_CALL"))

        val shareArguments = Regex("shareFamilySafetyDrillSummary\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertTrue(drillShareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(drillShareHelper.contains("type = \"text/plain\""))
        assertTrue(drillShareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(drillShareHelper.contains("Intent.createChooser"))
        assertEquals(
            1,
            Regex("putExtra\\(Intent\\.EXTRA_TEXT, summary\\)")
                .findAll(drillShareHelper)
                .count()
        )
        assertFalse(drillShareHelper.contains("Intent.EXTRA_SUBJECT"))
        assertFalse(drillShareHelper.contains("Intent.EXTRA_STREAM"))
        assertFalse(drillShareHelper.contains("ClipData"))
        assertFalse(drillShareHelper.contains("FLAG_GRANT_READ_URI_PERMISSION"))
        assertFalse(drillShareHelper.contains("FLAG_GRANT_WRITE_URI_PERMISSION"))
        assertFalse(drillShareHelper.contains("ScamAnalysis"))
        assertFalse(drillShareHelper.contains("currentAnalysis"))
        assertFalse(drillShareHelper.contains("analysis.input"))
        assertFalse(drillShareHelper.contains("IncidentPreviewRedactor"))
        assertFalse(drillShareHelper.contains("SafetyPlan"))
        assertFalse(drillShareHelper.contains("incidents"))
        assertFalse(drillShareHelper.contains("ContactsContract"))
        assertFalse(drillShareHelper.contains("SmsManager"))
    }

    @Test
    fun familyReadinessRoadmapIsPureStatelessDelegatesToExistingFlowsAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/FamilyReadinessRoadmap.kt"
        )
        val readinessSection = activitySource
            .substringAfter("private fun LinearLayout.readinessSection")
            .substringBefore("private fun openDrill")
        val resultScreen = activitySource
            .substringAfter("private fun LinearLayout.result()")
            .substringBefore("private fun LinearLayout.incidentLog")
        val roadmapScreen = activitySource
            .substringAfter("private fun LinearLayout.familyReadinessRoadmapScreen")
            .substringBefore("private fun LinearLayout.familyReadinessRoadmapCard")
        val roadmapCardRenderer = activitySource
            .substringAfter("private fun LinearLayout.familyReadinessRoadmapCard")
            .substringBefore("private fun openFamilyReadinessRoadmapAction")
        val roadmapActionRouter = activitySource
            .substringAfter("private fun openFamilyReadinessRoadmapAction")
            .substringBefore("private fun shareFamilyReadinessRoadmap")
        val roadmapShareHelper = activitySource
            .substringAfter("private fun shareFamilyReadinessRoadmap")
            .substringBefore("private fun LinearLayout.riskSignalExplainerScreen")
        val normalizedActionRouter = roadmapActionRouter.replace(Regex("\\s+"), " ")

        listOf(
            "private fun LinearLayout.readinessSection",
            "private fun openDrill",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.incidentLog",
            "private fun LinearLayout.familyReadinessRoadmapScreen",
            "private fun LinearLayout.familyReadinessRoadmapCard",
            "private fun openFamilyReadinessRoadmapAction",
            "private fun shareFamilyReadinessRoadmap",
            "private fun LinearLayout.riskSignalExplainerScreen"
        ).forEach { marker ->
            assertTrue("roadmap source marker missing: $marker", activitySource.contains(marker))
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))
        assertFalse(modelSource.contains("analysis.input"))
        assertFalse(modelSource.contains("IncidentPreviewRedactor"))
        assertFalse(modelSource.contains("SafetyPlan"))
        assertFalse(modelSource.contains("SafetyPlanStore"))

        assertTrue(activitySource.contains("FAMILY_READINESS_ROADMAP"))
        assertTrue(
            activitySource.contains(
                "Screen.FAMILY_READINESS_ROADMAP -> familyReadinessRoadmapScreen()"
            )
        )
        assertTrue(
            readinessSection.contains(
                "secondaryButton(\"가족 준비 로드맵\") { " +
                    "show(Screen.FAMILY_READINESS_ROADMAP) }"
            )
        )
        assertTrue(
            resultScreen.contains(
                "secondaryButton(\"이 상황 기준 준비 로드맵\") { " +
                    "show(Screen.FAMILY_READINESS_ROADMAP) }"
            )
        )
        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:roadmap|readinessRoadmap)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "(?m)^\\s*private\\s+var[^\\n]*FamilyReadinessRoadmap"
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex("KEY_\\w*(?:ROADMAP|READINESS_ROADMAP)\\w*")
                .containsMatchIn(activitySource)
        )

        assertTrue(roadmapScreen.contains("FamilyReadinessRoadmapBuilder.build("))
        assertTrue(roadmapScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            roadmapScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(roadmapScreen.contains("title(roadmap.title)"))
        assertTrue(roadmapScreen.contains("subtitle(roadmap.intro)"))
        assertTrue(roadmapScreen.contains("roadmap.readinessSummary.levelLabel"))
        assertTrue(roadmapScreen.contains("roadmap.readinessSummary.completedCount"))
        assertTrue(roadmapScreen.contains("roadmap.readinessSummary.totalCount"))
        assertTrue(roadmapScreen.contains("roadmap.readinessSummary.remainingCount"))
        assertTrue(roadmapScreen.contains("roadmap.privacySafetyNotes"))
        assertTrue(
            roadmapScreen.contains(
                "shareFamilyReadinessRoadmap(roadmap.familyShareSummary)"
            )
        )
        assertTrue(roadmapScreen.contains("primaryButton(\"가족에게 준비 로드맵 공유\")"))
        assertTrue(roadmapScreen.contains("secondaryButton(\"분석 결과로 돌아가기\")"))
        assertTrue(roadmapScreen.contains("secondaryButton(\"홈으로\")"))
        val recommendedRenderIndex = roadmapScreen.indexOf("roadmap.recommendedCards.forEach")
        val remainingRenderIndex = roadmapScreen.indexOf("val remainingCards = roadmap.cards.filterNot")
        assertTrue(recommendedRenderIndex >= 0)
        assertTrue(remainingRenderIndex > recommendedRenderIndex)
        assertEquals(
            1,
            Regex("roadmap\\.recommendedCards\\.forEach")
                .findAll(roadmapScreen)
                .count()
        )

        listOf(
            "card.stage",
            "card.title",
            "card.readinessStatus",
            "card.priority",
            "card.whyItMatters",
            "card.nextSmallSteps",
            "card.avoidNotes",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { field ->
            assertTrue("roadmap UI omitted $field", roadmapCardRenderer.contains(field))
        }

        val statelessUiSource = roadmapScreen + roadmapCardRenderer + roadmapActionRouter
        assertFalse(statelessUiSource.contains("analysis.input"))
        assertFalse(statelessUiSource.contains("IncidentPreviewRedactor"))
        assertFalse(statelessUiSource.contains("plan.safeWord"))
        assertFalse(statelessUiSource.contains("plan.callbackScript"))
        assertFalse(statelessUiSource.contains("plan.contacts"))
        assertFalse(statelessUiSource.contains("incidents.add("))
        assertFalse(statelessUiSource.contains("incidents.clear("))
        assertFalse(statelessUiSource.contains("prefs.edit("))
        assertFalse(statelessUiSource.contains("safetyPlan.save("))
        assertFalse(statelessUiSource.contains("CheckBox("))
        assertFalse(statelessUiSource.contains("EditText("))
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))
        assertFalse(statelessUiSource.contains(".save("))
        assertFalse(statelessUiSource.contains("putString("))
        assertFalse(statelessUiSource.contains("putBoolean("))

        listOf(
            "FamilyReadinessRoadmapActionId.QUICK_STOP -> show(Screen.QUICK_STOP)",
            "FamilyReadinessRoadmapActionId.CHECKER -> show(Screen.CHECKER)",
            "FamilyReadinessRoadmapActionId.ANALYSIS_RESULT -> " +
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "FamilyReadinessRoadmapActionId.RISK_SIGNAL_EXPLAINERS -> " +
                "show(Screen.RISK_SIGNAL_EXPLAINERS)",
            "FamilyReadinessRoadmapActionId.SAMPLE_LIBRARY -> openSampleLibrary()",
            "FamilyReadinessRoadmapActionId.FAMILY_CALLBACK_CARD -> " +
                "openFamilyCallbackCard(currentAnalysis)",
            "FamilyReadinessRoadmapActionId.SAFETY_PLAN -> show(Screen.PLAN)",
            "FamilyReadinessRoadmapActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)",
            "FamilyReadinessRoadmapActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)",
            "FamilyReadinessRoadmapActionId.EVIDENCE_KIT -> openEvidenceKit(null)",
            "FamilyReadinessRoadmapActionId.OFFICIAL_HELP_DIRECTORY -> " +
                "openHelpDirectory(Screen.FAMILY_READINESS_ROADMAP)",
            "FamilyReadinessRoadmapActionId.RESPONSE_CENTER -> openResponseCenter()",
            "FamilyReadinessRoadmapActionId.FAMILY_ALERT_MESSAGES -> " +
                "show(Screen.FAMILY_ALERT_MESSAGES)",
            "FamilyReadinessRoadmapActionId.TRUSTED_HELPER_HANDOFF -> " +
                "show(Screen.TRUSTED_HELPER_HANDOFF)",
            "FamilyReadinessRoadmapActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)",
            "FamilyReadinessRoadmapActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)",
            "FamilyReadinessRoadmapActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()",
            "FamilyReadinessRoadmapActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)"
        ).forEach { route ->
            assertTrue("family readiness roadmap route omitted: $route", normalizedActionRouter.contains(route))
        }
        assertFalse(roadmapActionRouter.contains("dial("))
        assertFalse(roadmapActionRouter.contains("Intent.ACTION_DIAL"))
        assertFalse(roadmapActionRouter.contains("Intent.ACTION_CALL"))

        val shareArguments = Regex("shareFamilyReadinessRoadmap\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("roadmap.familyShareSummary"), shareArguments)
        assertTrue(roadmapShareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(roadmapShareHelper.contains("type = \"text/plain\""))
        assertTrue(roadmapShareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(roadmapShareHelper.contains("Intent.createChooser"))
        assertEquals(
            1,
            Regex("putExtra\\(Intent\\.EXTRA_TEXT, summary\\)")
                .findAll(roadmapShareHelper)
                .count()
        )
        assertFalse(roadmapShareHelper.contains("Intent.EXTRA_SUBJECT"))
        assertFalse(roadmapShareHelper.contains("Intent.EXTRA_STREAM"))
        assertFalse(roadmapShareHelper.contains("ClipData"))
        assertFalse(roadmapShareHelper.contains("FLAG_GRANT_READ_URI_PERMISSION"))
        assertFalse(roadmapShareHelper.contains("FLAG_GRANT_WRITE_URI_PERMISSION"))
        assertFalse(roadmapShareHelper.contains("ScamAnalysis"))
        assertFalse(roadmapShareHelper.contains("currentAnalysis"))
        assertFalse(roadmapShareHelper.contains("analysis.input"))
        assertFalse(roadmapShareHelper.contains("IncidentPreviewRedactor"))
        assertFalse(roadmapShareHelper.contains("SafetyPlan"))
        assertFalse(roadmapShareHelper.contains("incidents"))
        assertFalse(roadmapShareHelper.contains("ContactsContract"))
        assertFalse(roadmapShareHelper.contains("SmsManager"))
    }

    @Test
    fun exposureImpactTriageIsPureStatelessDelegatesToExistingFlowsAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/ExposureImpactTriage.kt"
        )
        val homeScreen = activitySource
            .substringAfter("private fun LinearLayout.home()")
            .substringBefore("private fun LinearLayout.quickStopModeScreen")
        val resultScreen = activitySource
            .substringAfter("private fun LinearLayout.result()")
            .substringBefore("private fun LinearLayout.incidentLog")
        val triageScreen = activitySource
            .substringAfter("private fun LinearLayout.exposureImpactTriageScreen")
            .substringBefore("private fun LinearLayout.exposureImpactTriageCard")
        val triageCardRenderer = activitySource
            .substringAfter("private fun LinearLayout.exposureImpactTriageCard")
            .substringBefore("private fun openExposureImpactTriageAction")
        val triageActionRouter = activitySource
            .substringAfter("private fun openExposureImpactTriageAction")
            .substringBefore("private fun shareExposureImpactTriage")
        val triageShareHelper = activitySource
            .substringAfter("private fun shareExposureImpactTriage")
            .substringBefore("private fun LinearLayout.followUpCheckInPlannerScreen")
        val normalizedHomeScreen = homeScreen.replace(Regex("\\s+"), " ")
        val normalizedResultScreen = resultScreen.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = triageActionRouter.replace(Regex("\\s+"), " ")

        listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.incidentLog",
            "private fun shareSafeCallScriptsSummary",
            "private fun LinearLayout.exposureImpactTriageScreen",
            "private fun LinearLayout.exposureImpactTriageCard",
            "private fun openExposureImpactTriageAction",
            "private fun shareExposureImpactTriage",
            "private val match"
        ).forEach { marker ->
            assertTrue("exposure triage source marker missing: $marker", activitySource.contains(marker))
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))
        assertFalse(modelSource.contains("analysis.input"))
        assertFalse(modelSource.contains("IncidentPreviewRedactor"))
        assertFalse(modelSource.contains("SafetyPlan"))
        assertFalse(modelSource.contains("SafetyPlanStore"))

        assertTrue(activitySource.contains("EXPOSURE_IMPACT_TRIAGE"))
        assertTrue(
            activitySource.contains(
                "Screen.EXPOSURE_IMPACT_TRIAGE -> exposureImpactTriageScreen()"
            )
        )
        assertTrue(
            normalizedHomeScreen.contains(
                "secondaryButton(\"무엇이 노출됐는지 고르기\") { " +
                    "show(Screen.EXPOSURE_IMPACT_TRIAGE) }"
            )
        )
        assertTrue(
            normalizedResultScreen.contains(
                "secondaryButton(\"이 신호로 노출 가능성 확인\") { " +
                    "show(Screen.EXPOSURE_IMPACT_TRIAGE) }"
            )
        )
        assertTrue(homeScreen.indexOf("지금 뭐부터 할지 고르기") >= 0)
        assertTrue(
            homeScreen.indexOf("무엇이 노출됐는지 고르기") >
                homeScreen.indexOf("지금 뭐부터 할지 고르기")
        )
        assertTrue(
            homeScreen.indexOf("24시간 수습 계획") >
                homeScreen.indexOf("무엇이 노출됐는지 고르기")
        )
        assertTrue(resultScreen.indexOf("탐지 신호 설명 보기") >= 0)
        assertTrue(
            resultScreen.indexOf("이 신호로 노출 가능성 확인") >
                resultScreen.indexOf("탐지 신호 설명 보기")
        )
        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:exposure|impact|triage)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "(?m)^\\s*private\\s+var[^\\n]*ExposureImpact"
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex("KEY_\\w*(?:EXPOSURE|IMPACT|TRIAGE)\\w*")
                .containsMatchIn(activitySource)
        )

        assertTrue(triageScreen.contains("ExposureImpactTriageBuilder.build("))
        assertTrue(triageScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            triageScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(triageScreen.contains("title(triage.title)"))
        assertTrue(triageScreen.contains("subtitle(triage.intro)"))
        assertTrue(triageScreen.contains("triage.recommendationTitle"))
        assertTrue(triageScreen.contains("triage.recommendationExplanation"))
        assertTrue(triageScreen.contains("triage.recommendedActions"))
        assertTrue(triageScreen.contains("triage.privacySafetyNotes"))
        assertTrue(
            triageScreen.contains(
                "shareExposureImpactTriage(triage.familyShareSummary)"
            )
        )
        assertTrue(triageScreen.contains("primaryButton(\"가족에게 노출 분류 공유\")"))
        assertTrue(triageScreen.contains("secondaryButton(\"분석 결과로 돌아가기\")"))
        assertTrue(triageScreen.contains("secondaryButton(\"홈으로\")"))
        val recommendedRenderIndex = triageScreen.indexOf("triage.recommendedCards.forEach")
        val remainingRenderIndex =
            triageScreen.indexOf("val remainingCards = triage.cards.filterNot")
        assertTrue(recommendedRenderIndex >= 0)
        assertTrue(remainingRenderIndex > recommendedRenderIndex)
        assertEquals(
            1,
            Regex("triage\\.recommendedCards\\.forEach")
                .findAll(triageScreen)
                .count()
        )

        listOf(
            "card.category",
            "card.label",
            "card.whenThisMightApply",
            "card.firstSafeMove",
            "card.whatToAvoid",
            "card.officialHelpPrepNotes",
            "card.priority",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { field ->
            assertTrue("exposure triage UI omitted $field", triageCardRenderer.contains(field))
        }

        val statelessUiSource = triageScreen + triageCardRenderer + triageActionRouter
        assertFalse(statelessUiSource.contains("analysis.input"))
        assertFalse(statelessUiSource.contains("IncidentPreviewRedactor"))
        assertFalse(statelessUiSource.contains("plan.safeWord"))
        assertFalse(statelessUiSource.contains("plan.callbackScript"))
        assertFalse(statelessUiSource.contains("plan.contacts"))
        assertFalse(statelessUiSource.contains("incidents.add("))
        assertFalse(statelessUiSource.contains("incidents.clear("))
        assertFalse(statelessUiSource.contains("prefs.edit("))
        assertFalse(statelessUiSource.contains("safetyPlan.save("))
        assertFalse(statelessUiSource.contains("CheckBox("))
        assertFalse(statelessUiSource.contains("EditText("))
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))
        assertFalse(statelessUiSource.contains("selectedExposure"))
        assertFalse(statelessUiSource.contains("selectedTriage"))
        assertFalse(statelessUiSource.contains(".save("))
        assertFalse(statelessUiSource.contains("putString("))
        assertFalse(statelessUiSource.contains("putBoolean("))

        listOf(
            "ExposureImpactActionId.QUICK_STOP -> show(Screen.QUICK_STOP)",
            "ExposureImpactActionId.CHECKER -> show(Screen.CHECKER)",
            "ExposureImpactActionId.ANALYSIS_RESULT -> " +
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "ExposureImpactActionId.RISK_SIGNAL_EXPLAINERS -> " +
                "show(Screen.RISK_SIGNAL_EXPLAINERS)",
            "ExposureImpactActionId.FAMILY_CALLBACK_CARD -> " +
                "openFamilyCallbackCard(currentAnalysis)",
            "ExposureImpactActionId.FAMILY_READINESS_ROADMAP -> " +
                "show(Screen.FAMILY_READINESS_ROADMAP)",
            "ExposureImpactActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)",
            "ExposureImpactActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)",
            "ExposureImpactActionId.EVIDENCE_KIT -> " +
                "openEvidenceKit(action.evidenceSituation)",
            "ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY -> " +
                "openHelpDirectory(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "ExposureImpactActionId.REPORT_PREP -> " +
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "ExposureImpactActionId.RESPONSE_CENTER -> " +
                "openResponseCenter(action.responseSituation)",
            "ExposureImpactActionId.FAMILY_ALERT_MESSAGES -> " +
                "show(Screen.FAMILY_ALERT_MESSAGES)",
            "ExposureImpactActionId.TRUSTED_HELPER_HANDOFF -> " +
                "show(Screen.TRUSTED_HELPER_HANDOFF)",
            "ExposureImpactActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)",
            "ExposureImpactActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)",
            "ExposureImpactActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)"
        ).forEach { route ->
            assertTrue("exposure impact route omitted: $route", normalizedActionRouter.contains(route))
        }
        assertFalse(triageActionRouter.contains("dial("))
        assertFalse(triageActionRouter.contains("Intent.ACTION_DIAL"))
        assertFalse(triageActionRouter.contains("Intent.ACTION_CALL"))
        assertFalse(triageActionRouter.contains("Intent.ACTION_SEND"))

        val shareArguments = Regex("shareExposureImpactTriage\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("triage.familyShareSummary"), shareArguments)
        assertTrue(triageShareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(triageShareHelper.contains("type = \"text/plain\""))
        assertTrue(triageShareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(triageShareHelper.contains("Intent.createChooser"))
        assertTrue(triageShareHelper.contains("가족에게 노출 분류 공유"))
        assertEquals(
            1,
            Regex("putExtra\\(Intent\\.EXTRA_TEXT, summary\\)")
                .findAll(triageShareHelper)
                .count()
        )
        assertEquals(1, Regex("putExtra\\(").findAll(triageShareHelper).count())
        assertFalse(triageShareHelper.contains("Intent.EXTRA_SUBJECT"))
        assertFalse(triageShareHelper.contains("Intent.EXTRA_STREAM"))
        assertFalse(triageShareHelper.contains("ClipData"))
        assertFalse(triageShareHelper.contains("FLAG_GRANT_READ_URI_PERMISSION"))
        assertFalse(triageShareHelper.contains("FLAG_GRANT_WRITE_URI_PERMISSION"))
        assertFalse(triageShareHelper.contains("ScamAnalysis"))
        assertFalse(triageShareHelper.contains("currentAnalysis"))
        assertFalse(triageShareHelper.contains("analysis.input"))
        assertFalse(triageShareHelper.contains("IncidentPreviewRedactor"))
        assertFalse(triageShareHelper.contains("SafetyPlan"))
        assertFalse(triageShareHelper.contains("incidents"))
        assertFalse(triageShareHelper.contains("ContactsContract"))
        assertFalse(triageShareHelper.contains("SmsManager"))
    }

    @Test
    fun trustedHelperReviewIsPureStatelessDelegatesToExistingFlowsAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/TrustedHelperReviewMode.kt"
        )
        val homeScreen = activitySource
            .substringAfter("private fun LinearLayout.home()")
            .substringBefore("private fun LinearLayout.quickStopModeScreen")
        val resultScreen = activitySource
            .substringAfter("private fun LinearLayout.result()")
            .substringBefore("private fun LinearLayout.incidentLog")
        val handoffScreen = activitySource
            .substringAfter("private fun LinearLayout.trustedHelperHandoffScreen")
            .substringBefore("private fun openTrustedHelperAction")
        val reviewScreen = activitySource
            .substringAfter("private fun LinearLayout.trustedHelperReviewModeScreen")
            .substringBefore("private fun LinearLayout.trustedHelperReviewCard")
        val reviewCardRenderer = activitySource
            .substringAfter("private fun LinearLayout.trustedHelperReviewCard")
            .substringBefore("private fun openTrustedHelperReviewAction")
        val reviewActionRouter = activitySource
            .substringAfter("private fun openTrustedHelperReviewAction")
            .substringBefore("private fun shareTrustedHelperReview")
        val reviewShareHelper = activitySource
            .substringAfter("private fun shareTrustedHelperReview")
            .substringBefore("private fun LinearLayout.trustedHelperHandoffScreen")
        val normalizedHomeScreen = homeScreen.replace(Regex("\\s+"), " ")
        val normalizedResultScreen = resultScreen.replace(Regex("\\s+"), " ")
        val normalizedHandoffScreen = handoffScreen.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = reviewActionRouter.replace(Regex("\\s+"), " ")

        listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.trustedHelperReviewCard",
            "private fun openTrustedHelperReviewAction",
            "private fun shareTrustedHelperReview",
            "private fun LinearLayout.trustedHelperHandoffScreen"
        ).forEach { marker ->
            assertTrue("trusted helper review source marker missing: $marker", activitySource.contains(marker))
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))
        assertFalse(modelSource.contains("analysis.input"))
        assertFalse(modelSource.contains("IncidentPreviewRedactor"))
        assertFalse(modelSource.contains("SafetyPlan"))
        assertFalse(modelSource.contains("SafetyPlanStore"))

        assertTrue(activitySource.contains("TRUSTED_HELPER_REVIEW"))
        assertTrue(
            activitySource.contains(
                "Screen.TRUSTED_HELPER_REVIEW -> trustedHelperReviewModeScreen()"
            )
        )
        assertTrue(
            normalizedHomeScreen.contains(
                "secondaryButton(\"보호자와 함께 안전 검토\") { " +
                    "show(Screen.TRUSTED_HELPER_REVIEW) }"
            )
        )
        assertTrue(
            normalizedResultScreen.contains(
                "secondaryButton(\"이 신호로 보호자 검토 시작\") { " +
                    "show(Screen.TRUSTED_HELPER_REVIEW) }"
            )
        )
        assertTrue(
            normalizedHandoffScreen.contains(
                "secondaryButton(\"보호자와 함께 안전 검토\") { " +
                    "show(Screen.TRUSTED_HELPER_REVIEW) }"
            )
        )
        val homeHandoffIndex = homeScreen.indexOf("보호자 도움 요청 카드")
        val homeReviewIndex = homeScreen.indexOf("보호자와 함께 안전 검토")
        assertTrue(homeHandoffIndex >= 0)
        assertTrue(homeReviewIndex > homeHandoffIndex)
        val resultExposureIndex = resultScreen.indexOf("이 신호로 노출 가능성 확인")
        val resultReviewIndex = resultScreen.indexOf("이 신호로 보호자 검토 시작")
        assertTrue(resultExposureIndex >= 0)
        assertTrue(resultReviewIndex > resultExposureIndex)
        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:trustedHelperReview|helperReview|reviewMode)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex("KEY_\\w*(?:TRUSTED_HELPER_REVIEW|HELPER_REVIEW)\\w*")
                .containsMatchIn(activitySource)
        )

        assertTrue(reviewScreen.contains("TrustedHelperReviewModeBuilder.build("))
        assertTrue(reviewScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            reviewScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(reviewScreen.contains("title(review.title)"))
        assertTrue(reviewScreen.contains("subtitle(review.intro)"))
        assertTrue(reviewScreen.contains("review.recommendationTitle"))
        assertTrue(reviewScreen.contains("review.recommendationExplanation"))
        assertTrue(reviewScreen.contains("review.recommendedActions"))
        assertTrue(reviewScreen.contains("review.privacySafetyNotes"))
        assertTrue(
            reviewScreen.contains(
                "shareTrustedHelperReview(review.familyShareSummary)"
            )
        )
        assertTrue(
            reviewScreen.contains(
                "primaryButton(\"가족에게 보호자 검토 요약 공유\")"
            )
        )
        assertTrue(reviewScreen.contains("secondaryButton(\"분석 결과로 돌아가기\")"))
        assertTrue(reviewScreen.contains("secondaryButton(\"홈으로\")"))
        val recommendedRenderIndex = reviewScreen.indexOf("review.recommendedCards.forEach")
        val remainingRenderIndex =
            reviewScreen.indexOf("val remainingCards = review.cards.filterNot")
        val remainingRenderLoopIndex = reviewScreen.indexOf("remainingCards.forEach")
        assertTrue(recommendedRenderIndex >= 0)
        assertTrue(remainingRenderIndex > recommendedRenderIndex)
        assertTrue(remainingRenderLoopIndex > remainingRenderIndex)
        assertEquals(
            1,
            Regex("review\\.recommendedCards\\.forEach")
                .findAll(reviewScreen)
                .count()
        )
        assertEquals(
            1,
            Regex("remainingCards\\.forEach")
                .findAll(reviewScreen)
                .count()
        )

        listOf(
            "card.stage",
            "card.category",
            "card.title",
            "card.helperRole",
            "card.safeQuestion",
            "card.whatNotToAsk",
            "card.firstSafeMove",
            "card.priority",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { field ->
            assertTrue("trusted helper review UI omitted $field", reviewCardRenderer.contains(field))
        }

        val statelessUiSource = reviewScreen + reviewCardRenderer + reviewActionRouter
        assertFalse(statelessUiSource.contains("analysis.input"))
        assertFalse(statelessUiSource.contains("IncidentPreviewRedactor"))
        assertFalse(statelessUiSource.contains("plan.safeWord"))
        assertFalse(statelessUiSource.contains("plan.callbackScript"))
        assertFalse(statelessUiSource.contains("plan.contacts"))
        assertFalse(statelessUiSource.contains("incidents.add("))
        assertFalse(statelessUiSource.contains("incidents.clear("))
        assertFalse(statelessUiSource.contains("prefs.edit("))
        assertFalse(statelessUiSource.contains("safetyPlan.save("))
        assertFalse(statelessUiSource.contains("CheckBox("))
        assertFalse(statelessUiSource.contains("EditText("))
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))
        assertFalse(statelessUiSource.contains("selectedHelperReview"))
        assertFalse(statelessUiSource.contains("selectedReviewCard"))
        assertFalse(statelessUiSource.contains(".save("))
        assertFalse(statelessUiSource.contains("putString("))
        assertFalse(statelessUiSource.contains("putBoolean("))

        listOf(
            "TrustedHelperReviewActionId.QUICK_STOP -> show(Screen.QUICK_STOP)",
            "TrustedHelperReviewActionId.EXPOSURE_IMPACT_TRIAGE -> " +
                "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "TrustedHelperReviewActionId.CHECKER -> show(Screen.CHECKER)",
            "TrustedHelperReviewActionId.ANALYSIS_RESULT -> " +
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "TrustedHelperReviewActionId.RISK_SIGNAL_EXPLAINERS -> " +
                "show(Screen.RISK_SIGNAL_EXPLAINERS)",
            "TrustedHelperReviewActionId.FAMILY_CALLBACK_CARD -> " +
                "openFamilyCallbackCard(currentAnalysis)",
            "TrustedHelperReviewActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)",
            "TrustedHelperReviewActionId.FAMILY_ALERT_MESSAGES -> " +
                "show(Screen.FAMILY_ALERT_MESSAGES)",
            "TrustedHelperReviewActionId.TRUSTED_HELPER_HANDOFF -> " +
                "show(Screen.TRUSTED_HELPER_HANDOFF)",
            "TrustedHelperReviewActionId.FAMILY_READINESS_ROADMAP -> " +
                "show(Screen.FAMILY_READINESS_ROADMAP)",
            "TrustedHelperReviewActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()",
            "TrustedHelperReviewActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)",
            "TrustedHelperReviewActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)",
            "TrustedHelperReviewActionId.EVIDENCE_KIT -> openEvidenceKit(null)",
            "TrustedHelperReviewActionId.OFFICIAL_HELP_DIRECTORY -> " +
                "openHelpDirectory(Screen.TRUSTED_HELPER_REVIEW)",
            "TrustedHelperReviewActionId.REPORT_PREP -> " +
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "TrustedHelperReviewActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)",
            "TrustedHelperReviewActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)"
        ).forEach { route ->
            assertTrue("trusted helper review route omitted: $route", normalizedActionRouter.contains(route))
        }
        assertFalse(reviewActionRouter.contains("dial("))
        assertFalse(reviewActionRouter.contains("Intent.ACTION_DIAL"))
        assertFalse(reviewActionRouter.contains("Intent.ACTION_CALL"))
        assertFalse(reviewActionRouter.contains("Intent.ACTION_SEND"))

        val shareArguments = Regex("shareTrustedHelperReview\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("review.familyShareSummary"), shareArguments)
        assertTrue(reviewShareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(reviewShareHelper.contains("type = \"text/plain\""))
        assertTrue(reviewShareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(reviewShareHelper.contains("Intent.createChooser"))
        assertTrue(reviewShareHelper.contains("가족에게 보호자 검토 요약 공유"))
        assertEquals(
            1,
            Regex("putExtra\\(Intent\\.EXTRA_TEXT, summary\\)")
                .findAll(reviewShareHelper)
                .count()
        )
        assertEquals(1, Regex("putExtra\\(").findAll(reviewShareHelper).count())
        assertFalse(reviewShareHelper.contains("Intent.EXTRA_SUBJECT"))
        assertFalse(reviewShareHelper.contains("Intent.EXTRA_STREAM"))
        assertFalse(reviewShareHelper.contains("ClipData"))
        assertFalse(reviewShareHelper.contains("FLAG_GRANT_READ_URI_PERMISSION"))
        assertFalse(reviewShareHelper.contains("FLAG_GRANT_WRITE_URI_PERMISSION"))
        assertFalse(reviewShareHelper.contains("ScamAnalysis"))
        assertFalse(reviewShareHelper.contains("currentAnalysis"))
        assertFalse(reviewShareHelper.contains("analysis.input"))
        assertFalse(reviewShareHelper.contains("IncidentPreviewRedactor"))
        assertFalse(reviewShareHelper.contains("SafetyPlan"))
        assertFalse(reviewShareHelper.contains("incidents"))
        assertFalse(reviewShareHelper.contains("ContactsContract"))
        assertFalse(reviewShareHelper.contains("SmsManager"))
    }

    @Test
    fun followUpCheckInPlannerIsPureStatelessManualAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/FollowUpCheckInPlanner.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.recoveryPlan24hScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.followUpCheckInCard",
            "private fun openFollowUpCheckInAction",
            "private fun shareFollowUpCheckInPlanner"
        ).forEach { marker ->
            assertTrue("follow-up planner source marker missing: $marker", activitySource.contains(marker))
        }

        val homeScreen = activitySource
            .substringAfter("private fun LinearLayout.home()")
            .substringBefore("private fun LinearLayout.quickStopModeScreen")
        val resultScreen = activitySource
            .substringAfter("private fun LinearLayout.result()")
            .substringBefore("private fun LinearLayout.incidentLog")
        val recoveryScreen = activitySource
            .substringAfter("private fun LinearLayout.recoveryPlan24hScreen")
            .substringBefore("private fun openRecoveryPlanAction")
        val plannerScreen = activitySource
            .substringAfter("private fun LinearLayout.followUpCheckInPlannerScreen")
            .substringBefore("private fun LinearLayout.followUpCheckInCard")
        val checkpointRenderer = activitySource
            .substringAfter("private fun LinearLayout.followUpCheckInCard")
            .substringBefore("private fun openFollowUpCheckInAction")
        val actionRouter = activitySource
            .substringAfter("private fun openFollowUpCheckInAction")
            .substringBefore("private fun shareFollowUpCheckInPlanner")
        val shareHelper = activitySource
            .substringAfter("private fun shareFollowUpCheckInPlanner")
            .substringBefore("\n    private val ")
        val normalizedHomeScreen = homeScreen.replace(Regex("\\s+"), " ")
        val normalizedResultScreen = resultScreen.replace(Regex("\\s+"), " ")
        val normalizedRecoveryScreen = recoveryScreen.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))
        assertFalse(modelSource.contains("analysis.input"))
        assertFalse(modelSource.contains("IncidentPreviewRedactor"))
        assertFalse(modelSource.contains("SafetyPlan"))
        assertFalse(modelSource.contains("SafetyPlanStore"))

        assertTrue(activitySource.contains("FOLLOW_UP_CHECK_IN_PLANNER"))
        assertTrue(
            activitySource.contains(
                "Screen.FOLLOW_UP_CHECK_IN_PLANNER -> followUpCheckInPlannerScreen()"
            )
        )
        assertTrue(
            normalizedHomeScreen.contains(
                "secondaryButton(\"며칠 뒤 다시 확인할 것\") { " +
                    "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER) }"
            )
        )
        assertTrue(
            normalizedResultScreen.contains(
                "secondaryButton(\"이 신호로 후속 점검 보기\") { " +
                    "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER) }"
            )
        )
        assertTrue(
            normalizedRecoveryScreen.contains(
                "secondaryButton(\"며칠 뒤 후속 점검 보기\") { " +
                    "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER) }"
            )
        )
        assertTrue(homeScreen.indexOf("24시간 수습 계획") >= 0)
        assertTrue(
            homeScreen.indexOf("며칠 뒤 다시 확인할 것") >
                homeScreen.indexOf("24시간 수습 계획")
        )
        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:followUp|checkIn|checkpoint|reminder)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex("KEY_\\w*(?:FOLLOW_UP|CHECK_IN|CHECKPOINT|REMINDER)\\w*")
                .containsMatchIn(activitySource)
        )

        assertTrue(plannerScreen.contains("FollowUpCheckInPlannerBuilder.build("))
        assertTrue(plannerScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            plannerScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(
            plannerScreen.contains("incidentCount = incidents.load().size") ||
                plannerScreen.contains("incidentCount = incidents.load().count()") ||
                plannerScreen.contains("incidentCount = incidents.count()")
        )
        assertTrue(plannerScreen.contains("title(planner.title)"))
        assertTrue(plannerScreen.contains("subtitle(planner.intro)"))
        assertTrue(plannerScreen.contains("planner.recommendationTitle"))
        assertTrue(plannerScreen.contains("planner.recommendationExplanation"))
        assertTrue(plannerScreen.contains("planner.recommendedActions"))
        assertTrue(plannerScreen.contains("planner.privacySafetyNotes"))
        assertTrue(
            plannerScreen.contains(
                "shareFollowUpCheckInPlanner(planner.familyShareSummary)"
            )
        )
        assertTrue(plannerScreen.contains("primaryButton(\"가족에게 후속 점검 공유\")"))
        assertTrue(plannerScreen.contains("secondaryButton(\"분석 결과로 돌아가기\")"))
        assertTrue(plannerScreen.contains("secondaryButton(\"홈으로\")"))
        val recommendedRenderIndex =
            plannerScreen.indexOf("planner.recommendedCheckpoints.forEach")
        val remainingFilterIndex =
            plannerScreen.indexOf("val remainingCheckpoints = planner.checkpoints.filterNot")
        val remainingRenderIndex = plannerScreen.indexOf("remainingCheckpoints.forEach")
        assertTrue(recommendedRenderIndex >= 0)
        assertTrue(remainingFilterIndex > recommendedRenderIndex)
        assertTrue(remainingRenderIndex > remainingFilterIndex)
        assertEquals(
            1,
            Regex("planner\\.recommendedCheckpoints\\.forEach")
                .findAll(plannerScreen)
                .count()
        )
        assertEquals(
            1,
            Regex("remainingCheckpoints\\.forEach")
                .findAll(plannerScreen)
                .count()
        )

        listOf(
            "checkpoint.timeLabel",
            "checkpoint.category",
            "checkpoint.title",
            "checkpoint.purpose",
            "checkpoint.manualCheckSteps",
            "checkpoint.whatNotToDo",
            "checkpoint.priority",
            "checkpoint.primaryAction",
            "checkpoint.secondaryActions"
        ).forEach { field ->
            assertTrue("follow-up planner UI omitted $field", checkpointRenderer.contains(field))
        }

        val statelessUiSource = plannerScreen + checkpointRenderer + actionRouter
        assertFalse(statelessUiSource.contains("analysis.input"))
        assertFalse(statelessUiSource.contains("IncidentPreviewRedactor"))
        assertFalse(statelessUiSource.contains("incident.preview"))
        assertFalse(statelessUiSource.contains("plan.safeWord"))
        assertFalse(statelessUiSource.contains("plan.callbackScript"))
        assertFalse(statelessUiSource.contains("plan.contacts"))
        assertFalse(statelessUiSource.contains("incidents.add("))
        assertFalse(statelessUiSource.contains("incidents.clear("))
        assertFalse(statelessUiSource.contains("prefs.edit("))
        assertFalse(statelessUiSource.contains("safetyPlan.save("))
        assertFalse(statelessUiSource.contains("CheckBox("))
        assertFalse(statelessUiSource.contains("EditText("))
        assertFalse(statelessUiSource.contains("RadioButton("))
        assertFalse(statelessUiSource.contains("Switch("))
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))
        assertFalse(statelessUiSource.contains("selectedCheckpoint"))
        assertFalse(statelessUiSource.contains("selectedCheckIn"))
        assertFalse(statelessUiSource.contains("reminderSchedule"))
        assertFalse(statelessUiSource.contains("onSaveInstanceState"))
        assertFalse(statelessUiSource.contains(".save("))
        assertFalse(statelessUiSource.contains("putString("))
        assertFalse(statelessUiSource.contains("putBoolean("))
        assertFalse(statelessUiSource.contains("putInt("))
        assertFalse(statelessUiSource.contains("Intent.ACTION_SEND"))

        listOf(
            "QUICK_STOP -> show(Screen.QUICK_STOP)",
            "EXPOSURE_IMPACT_TRIAGE -> show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "TRUSTED_HELPER_REVIEW -> show(Screen.TRUSTED_HELPER_REVIEW)",
            "CHECKER -> show(Screen.CHECKER)",
            "ANALYSIS_RESULT -> " +
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "FAMILY_CALLBACK_CARD -> openFamilyCallbackCard(currentAnalysis)",
            "SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)",
            "FAMILY_ALERT_MESSAGES -> show(Screen.FAMILY_ALERT_MESSAGES)",
            "FAMILY_READINESS_ROADMAP -> show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH -> openDrill()",
            "SAFETY_PLAN -> show(Screen.PLAN)",
            "RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)",
            "ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)",
            "TIMELINE_BRIEFING -> show(Screen.TIMELINE_BRIEFING)",
            "SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)",
            "PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)"
        ).forEach { route ->
            assertTrue("follow-up planner route omitted: $route", normalizedActionRouter.contains(route))
        }
        assertTrue(normalizedActionRouter.contains("EVIDENCE_KIT ->"))
        assertTrue(normalizedActionRouter.contains("openEvidenceKit("))
        assertTrue(normalizedActionRouter.contains("OFFICIAL_HELP_DIRECTORY ->"))
        assertTrue(
            normalizedActionRouter.contains(
                "openHelpDirectory(Screen.FOLLOW_UP_CHECK_IN_PLANNER)"
            )
        )
        assertTrue(normalizedActionRouter.contains("REPORT_PREP ->"))
        assertTrue(
            normalizedActionRouter.contains(
                "FollowUpCheckInActionId.REPORT_PREP -> show(Screen.REPORT_PREP)"
            )
        )
        assertFalse(actionRouter.contains("dial("))
        assertFalse(actionRouter.contains("Intent.ACTION_DIAL"))
        assertFalse(actionRouter.contains("Intent.ACTION_CALL"))

        val shareArguments = Regex("shareFollowUpCheckInPlanner\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("planner.familyShareSummary"), shareArguments)
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertTrue(shareHelper.contains("가족에게 후속 점검 공유"))
        assertEquals(
            1,
            Regex("putExtra\\(Intent\\.EXTRA_TEXT, summary\\)")
                .findAll(shareHelper)
                .count()
        )
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        assertFalse(shareHelper.contains("Intent.EXTRA_SUBJECT"))
        assertFalse(shareHelper.contains("Intent.EXTRA_STREAM"))
        assertFalse(shareHelper.contains("ClipData"))
        assertFalse(shareHelper.contains("FLAG_GRANT_READ_URI_PERMISSION"))
        assertFalse(shareHelper.contains("FLAG_GRANT_WRITE_URI_PERMISSION"))
        assertFalse(shareHelper.contains("ScamAnalysis"))
        assertFalse(shareHelper.contains("currentAnalysis"))
        assertFalse(shareHelper.contains("analysis.input"))
        assertFalse(shareHelper.contains("IncidentPreviewRedactor"))
        assertFalse(shareHelper.contains("SafetyPlan"))
        assertFalse(shareHelper.contains("incidents"))
        assertFalse(shareHelper.contains("ContactsContract"))
        assertFalse(shareHelper.contains("SmsManager"))

        listOf(
            "AlarmManager",
            "NotificationManager",
            "NotificationCompat",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "ForegroundService",
            "BroadcastReceiver"
        ).forEach { forbiddenApi ->
            assertFalse(
                "follow-up planner used forbidden scheduler API: $forbiddenApi",
                activitySource.contains(forbiddenApi) || modelSource.contains(forbiddenApi)
            )
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun familyResponseRoleBoardIsPureStatelessManualAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/FamilyResponseRoleBoard.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.familyResponseRoleCard",
            "private fun openFamilyResponseRoleAction",
            "private fun shareFamilyResponseRoleBoard"
        ).forEach { marker ->
            assertTrue("family role-board source marker missing: $marker", activitySource.contains(marker))
        }

        val homeScreen = functionBlock(activitySource, "private fun LinearLayout.home()")
        val resultScreen = functionBlock(activitySource, "private fun LinearLayout.result()")
        val boardScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.familyResponseRoleBoardScreen"
        )
        val roleRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.familyResponseRoleCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openFamilyResponseRoleAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareFamilyResponseRoleBoard"
        )
        val normalizedHomeScreen = homeScreen.replace(Regex("\\s+"), " ")
        val normalizedResultScreen = resultScreen.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))
        assertFalse(modelSource.contains("analysis.input"))
        assertFalse(modelSource.contains("IncidentPreviewRedactor"))
        assertFalse(modelSource.contains("SafetyPlan"))
        assertFalse(modelSource.contains("SafetyPlanStore"))

        assertTrue(activitySource.contains("FAMILY_RESPONSE_ROLE_BOARD"))
        assertTrue(
            activitySource.contains(
                "Screen.FAMILY_RESPONSE_ROLE_BOARD -> familyResponseRoleBoardScreen()"
            )
        )
        assertTrue(
            normalizedHomeScreen.contains(
                "secondaryButton(\"가족 역할 나눠 대응하기\") { " +
                    "show(Screen.FAMILY_RESPONSE_ROLE_BOARD) }"
            ) ||
                normalizedHomeScreen.contains(
                    "primaryButton(\"가족 역할 나눠 대응하기\") { " +
                        "show(Screen.FAMILY_RESPONSE_ROLE_BOARD) }"
                )
        )
        assertTrue(
            normalizedResultScreen.contains(
                "secondaryButton(\"이 신호로 가족 역할 나누기\") { " +
                    "show(Screen.FAMILY_RESPONSE_ROLE_BOARD) }"
            ) ||
                normalizedResultScreen.contains(
                    "primaryButton(\"이 신호로 가족 역할 나누기\") { " +
                        "show(Screen.FAMILY_RESPONSE_ROLE_BOARD) }"
                )
        )

        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:roleBoard|selectedRole|assignedRole|" +
                    "roleAssignment|checkmark)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "KEY_\\w*(?:ROLE_BOARD|SELECTED_ROLE|ASSIGNED_ROLE|ROLE_ASSIGNMENT|CHECKMARK)\\w*"
            ).containsMatchIn(activitySource)
        )

        assertTrue(boardScreen.contains("FamilyResponseRoleBoardBuilder.build("))
        assertTrue(boardScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            boardScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(
            boardScreen.contains("incidentCount = incidents.load().size") ||
                boardScreen.contains("incidentCount = incidents.load().count()") ||
                boardScreen.contains("incidentCount = incidents.count()")
        )
        assertTrue(boardScreen.contains("title(board.title)"))
        assertTrue(boardScreen.contains("subtitle(board.intro)"))
        assertTrue(boardScreen.contains("board.recommendationTitle"))
        assertTrue(boardScreen.contains("board.recommendationExplanation"))
        assertTrue(boardScreen.contains("board.recommendedActions"))
        assertTrue(boardScreen.contains("board.privacySafetyNotes"))
        assertTrue(
            boardScreen.contains(
                "shareFamilyResponseRoleBoard(board.familyShareSummary)"
            )
        )
        assertTrue(boardScreen.contains("primaryButton(\"가족에게 역할 보드 공유\")"))
        assertTrue(
            boardScreen.contains("secondaryButton(\"분석 결과로 돌아가기\")") ||
                boardScreen.contains("secondaryButton(\"홈으로\")")
        )

        val recommendedRenderIndex = boardScreen.indexOf("board.recommendedRoles")
        val remainingRolesIndex = boardScreen.indexOf("remainingRoles")
        assertTrue(recommendedRenderIndex >= 0)
        assertTrue(remainingRolesIndex > recommendedRenderIndex)
        assertEquals(
            1,
            Regex("board\\.recommendedRoles\\.forEach")
                .findAll(boardScreen)
                .count()
        )
        assertEquals(
            1,
            Regex("remainingRoles\\.forEach")
                .findAll(boardScreen)
                .count()
        )

        listOf(
            "card.roleLabel",
            "card.mission",
            "card.safeHandoffSentence",
            "card.category",
            "card.whenToUse",
            "card.doNotDoBoundaries",
            "card.priority",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { field ->
            assertTrue("family role-board UI omitted $field", roleRenderer.contains(field))
        }

        val statelessUiSource = boardScreen + roleRenderer + actionRouter
        listOf(
            "analysis.input",
            "IncidentPreviewRedactor",
            "incident.preview",
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            "incidents.add(",
            "incidents.clear(",
            "prefs.edit(",
            "safetyPlan.save(",
            "CheckBox(",
            "EditText(",
            "RadioButton(",
            "Switch(",
            "selectedRole",
            "assignedRole",
            "roleAssignment",
            "checkmark",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "Intent.ACTION_SEND"
        ).forEach { forbiddenState ->
            assertFalse(
                "family role-board UI used forbidden state/value: $forbiddenState",
                statelessUiSource.contains(forbiddenState, ignoreCase = true)
            )
        }
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))

        listOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "TRUSTED_HELPER_HANDOFF" to "show(Screen.TRUSTED_HELPER_HANDOFF)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "RECOVERY_PLAN" to "show(Screen.RECOVERY_PLAN)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REPORT_PREP" to "show(Screen.REPORT_PREP)",
            "INCIDENT_TIMELINE_BRIEFING" to "show(Screen.TIMELINE_BRIEFING)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)"
        ).forEach { (actionId, destination) ->
            assertTrue(
                "family role-board route omitted action: $actionId",
                normalizedActionRouter.contains("FamilyResponseRoleActionId.$actionId ->")
            )
            assertTrue(
                "family role-board route omitted destination for $actionId: $destination",
                normalizedActionRouter.contains(destination)
            )
        }
        assertTrue(
            normalizedActionRouter.contains("FamilyResponseRoleActionId.EVIDENCE_KIT ->")
        )
        assertTrue(normalizedActionRouter.contains("openEvidenceKit("))
        assertTrue(
            normalizedActionRouter.contains(
                "FamilyResponseRoleActionId.OFFICIAL_HELP_DIRECTORY ->"
            )
        )
        assertTrue(
            normalizedActionRouter.contains(
                "openHelpDirectory(Screen.FAMILY_RESPONSE_ROLE_BOARD)"
            )
        )
        assertFalse(actionRouter.contains("dial("))
        assertFalse(actionRouter.contains("Intent.ACTION_DIAL"))
        assertFalse(actionRouter.contains("Intent.ACTION_CALL"))

        val shareArguments = Regex("shareFamilyResponseRoleBoard\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("board.familyShareSummary"), shareArguments)
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertTrue(shareHelper.contains("가족에게 역할 보드 공유"))
        assertEquals(
            1,
            Regex("putExtra\\(Intent\\.EXTRA_TEXT, summary\\)")
                .findAll(shareHelper)
                .count()
        )
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "ScamAnalysis",
            "currentAnalysis",
            "analysis.input",
            "IncidentPreviewRedactor",
            "SafetyPlan",
            "incidents",
            "ContactsContract",
            "SmsManager"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "family role-board share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource)
            )
        }

        listOf(
            "ContactsContract",
            "SmsManager",
            "AlarmManager",
            "NotificationManager",
            "NotificationCompat",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "ForegroundService",
            "BroadcastReceiver"
        ).forEach { forbiddenApi ->
            assertFalse(
                "family role-board used forbidden API: $forbiddenApi",
                activitySource.contains(forbiddenApi) || modelSource.contains(forbiddenApi)
            )
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun familyCalmDebriefAgendaIsPureStatelessManualAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/FamilyCalmDebriefAgenda.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.familyCalmDebriefCard",
            "private fun openFamilyCalmDebriefAction",
            "private fun shareFamilyCalmDebriefAgenda"
        ).forEach { marker ->
            assertTrue("family debrief source marker missing: $marker", activitySource.contains(marker))
        }

        val homeScreen = functionBlock(activitySource, "private fun LinearLayout.home()")
        val resultScreen = functionBlock(activitySource, "private fun LinearLayout.result()")
        val quickStopScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.quickStopModeScreen"
        )
        val helperReviewScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.trustedHelperReviewModeScreen"
        )
        val followUpScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.followUpCheckInPlannerScreen"
        )
        val roleBoardScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.familyResponseRoleBoardScreen"
        )
        val agendaScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.familyCalmDebriefAgendaScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.familyCalmDebriefCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openFamilyCalmDebriefAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareFamilyCalmDebriefAgenda"
        )
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))
        listOf(
            "analysis.input",
            "IncidentPreviewRedactor",
            "SafetyPlan",
            "SafetyPlanStore",
            "IncidentStore",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction"
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "family debrief model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }

        assertTrue(activitySource.contains("FAMILY_CALM_DEBRIEF_AGENDA"))
        assertTrue(
            activitySource.contains(
                "Screen.FAMILY_CALM_DEBRIEF_AGENDA -> familyCalmDebriefAgendaScreen()"
            )
        )
        listOf(
            homeScreen,
            resultScreen,
            quickStopScreen,
            helperReviewScreen,
            followUpScreen,
            roleBoardScreen
        ).forEachIndexed { index, entryScreen ->
            assertTrue(
                "family debrief entry route missing at index $index",
                entryScreen.contains("show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)")
            )
        }

        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:calmDebrief|debriefAgenda|selectedAgenda|" +
                    "selectedDebrief|debriefProgress|debriefHistory|checkmark)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "KEY_\\w*(?:CALM_DEBRIEF|DEBRIEF_AGENDA|SELECTED_AGENDA|" +
                    "DEBRIEF_PROGRESS|DEBRIEF_HISTORY|CHECKMARK)\\w*"
            ).containsMatchIn(activitySource)
        )

        assertTrue(agendaScreen.contains("FamilyCalmDebriefAgendaBuilder.build("))
        assertTrue(agendaScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            agendaScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(agendaScreen.contains("incidentCount = incidents.count()"))
        assertFalse(agendaScreen.contains("incidents.load("))
        assertTrue(agendaScreen.contains("title(agenda.title)"))
        assertTrue(agendaScreen.contains("subtitle(agenda.intro)"))
        assertTrue(agendaScreen.contains("agenda.durationLabel"))
        assertTrue(agendaScreen.contains("agenda.recommendationTitle"))
        assertTrue(agendaScreen.contains("agenda.recommendationExplanation"))
        assertTrue(agendaScreen.contains("agenda.recommendedActions"))
        assertTrue(agendaScreen.contains("agenda.privacySafetyNotes"))
        assertTrue(
            agendaScreen.contains(
                "shareFamilyCalmDebriefAgenda(agenda.familyShareSummary)"
            )
        )
        assertTrue(agendaScreen.contains("primaryButton(\"가족에게 차분 대화 순서 공유\")"))
        assertTrue(agendaScreen.contains("secondaryButton(\"홈으로\")"))

        assertEquals(
            1,
            Regex("agenda\\.agendaCards\\.forEach")
                .findAll(agendaScreen)
                .count()
        )
        assertTrue(agendaScreen.contains("recommended = card.id in recommendedIds"))
        assertFalse(agendaScreen.contains("remainingCards"))

        listOf(
            "card.displayOrder",
            "card.title",
            "card.category",
            "card.whatToSayOrDo",
            "card.whatNotToAskStoreOrShare",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { field ->
            assertTrue("family debrief UI omitted $field", cardRenderer.contains(field))
        }

        val statelessUiSource = agendaScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            "incidents.add(",
            "incidents.clear(",
            "prefs.edit(",
            "safetyPlan.save(",
            "CheckBox(",
            "EditText(",
            "RadioButton(",
            "Switch(",
            "selectedAgenda",
            "selectedDebrief",
            "debriefProgress",
            "debriefHistory",
            "checkmark",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "Intent.ACTION_SEND"
        ).forEach { forbiddenState ->
            assertFalse(
                "family debrief UI used forbidden state/value: $forbiddenState",
                statelessUiSource.contains(forbiddenState, ignoreCase = true)
            )
        }
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))

        mapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "RECOVERY_PLAN" to "show(Screen.RECOVERY_PLAN)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "INCIDENT_TIMELINE_BRIEFING" to "show(Screen.TIMELINE_BRIEFING)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)"
        ).forEach { (actionId, destination) ->
            assertTrue(
                "family debrief route omitted action: $actionId",
                normalizedActionRouter.contains("FamilyCalmDebriefActionId.$actionId ->")
            )
            assertTrue(
                "family debrief route omitted destination for $actionId: $destination",
                normalizedActionRouter.contains(destination)
            )
        }
        assertFalse(actionRouter.contains("dial("))
        assertFalse(actionRouter.contains("Intent.ACTION_DIAL"))
        assertFalse(actionRouter.contains("Intent.ACTION_CALL"))

        val shareArguments = Regex("shareFamilyCalmDebriefAgenda\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("agenda.familyShareSummary"), shareArguments)
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertTrue(shareHelper.contains("가족에게 차분 대화 순서 공유"))
        assertEquals(
            1,
            Regex("putExtra\\(Intent\\.EXTRA_TEXT, summary\\)")
                .findAll(shareHelper)
                .count()
        )
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "ScamAnalysis",
            "currentAnalysis",
            "analysis.input",
            "IncidentPreviewRedactor",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "ContactsContract",
            "SmsManager"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "family debrief share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource)
            )
        }

        listOf(
            "ContactsContract",
            "SmsManager",
            "AlarmManager",
            "NotificationManager",
            "NotificationCompat",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "ForegroundService",
            "BroadcastReceiver"
        ).forEach { forbiddenApi ->
            assertFalse(
                "family debrief used forbidden API: $forbiddenApi",
                activitySource.contains(forbiddenApi) || modelSource.contains(forbiddenApi)
            )
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun safePastePrivacyCoachIsPureStatelessManualDoesNotTouchClipboardAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafePastePrivacyCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.checker()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.privacyCenterScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.safePastePrivacyCoachScreen",
            "private fun LinearLayout.safePastePrivacyCoachCard",
            "private fun openSafePastePrivacyCoachAction",
            "private fun shareSafePastePrivacyCoach"
        ).forEach { marker ->
            assertTrue("safe-paste source marker missing: $marker", activitySource.contains(marker))
        }

        val homeScreen = functionBlock(activitySource, "private fun LinearLayout.home()")
        val checkerScreen = functionBlock(activitySource, "private fun LinearLayout.checker()")
        val resultScreen = functionBlock(activitySource, "private fun LinearLayout.result()")
        val privacyCenterScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.privacyCenterScreen"
        )
        val flowRouterScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.safetyFlowRouterScreen"
        )
        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.safePastePrivacyCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.safePastePrivacyCoachCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openSafePastePrivacyCoachAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareSafePastePrivacyCoach"
        )
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))
        listOf(
            "analysis.input",
            "analysis.score",
            "IncidentPreviewRedactor",
            "SafetyPlan",
            "SafetyPlanStore",
            "IncidentStore",
            "readiness.title",
            "readiness.checklist",
            "readiness.nextAction",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "doAfterTextChanged",
            "EditText",
            "setText(",
            "input.text",
            "ScamAnalyzer",
            "Regex(",
            "replace("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "safe-paste model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }
        assertFalse(
            Regex("analysis\\s*\\??\\s*\\.\\s*(?:input|score)")
                .containsMatchIn(modelSource)
        )

        assertTrue(activitySource.contains("SAFE_PASTE_PRIVACY_COACH"))
        assertTrue(
            activitySource.contains(
                "Screen.SAFE_PASTE_PRIVACY_COACH -> safePastePrivacyCoachScreen()"
            )
        )
        listOf(
            homeScreen,
            checkerScreen,
            resultScreen,
            privacyCenterScreen,
            flowRouterScreen
        ).forEachIndexed { index, entryScreen ->
            assertTrue(
                "safe-paste entry route missing at index $index",
                entryScreen.contains("show(Screen.SAFE_PASTE_PRIVACY_COACH)")
            )
        }

        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:safePaste|pasteCoach|masking|redaction|clipboard|" +
                    "selectedCoach|coachProgress|coachHistory|checkmark)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "KEY_\\w*(?:SAFE_PASTE|PASTE_COACH|MASKING|REDACTION|CLIPBOARD|" +
                    "SELECTED_COACH|COACH_PROGRESS|COACH_HISTORY|CHECKMARK)\\w*"
            ).containsMatchIn(activitySource)
        )

        assertTrue(coachScreen.contains("SafePastePrivacyCoachBuilder.build("))
        assertTrue(coachScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            coachScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(
            coachScreen.contains(
                "incidentCount = if (incidents.hasSavedIncidents()) 1 else 0"
            )
        )
        assertFalse(coachScreen.contains("incidents.load("))
        val incidentPresenceHelper = functionBlock(
            incidentStoreSource,
            "fun hasSavedIncidents()"
        )
        assertTrue(incidentPresenceHelper.contains("prefs.contains(\"items\")"))
        listOf(
            "getString",
            "decode",
            "Base64",
            "IncidentPreviewRedactor",
            "preview",
            "riskLabel",
            "signals",
            "score",
            ".map",
            "split("
        ).forEach { forbiddenIncidentRead ->
            assertFalse(
                "incident presence read stored content: $forbiddenIncidentRead",
                incidentPresenceHelper.contains(forbiddenIncidentRead)
            )
        }
        val incidentCountHelper = functionBlock(incidentStoreSource, "fun count()")
        assertTrue(incidentCountHelper.contains("lineSequence()"))
        assertTrue(incidentCountHelper.contains(".count"))
        listOf(
            "decode",
            "Base64",
            "IncidentPreviewRedactor",
            "preview",
            "riskLabel",
            "signals",
            "score",
            ".map",
            "split("
        ).forEach { forbiddenIncidentRead ->
            assertFalse(
                "incident count decoded stored content: $forbiddenIncidentRead",
                incidentCountHelper.contains(forbiddenIncidentRead)
            )
        }
        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes"
        ).forEach { renderedField ->
            assertTrue("safe-paste UI omitted $renderedField", coachScreen.contains(renderedField))
        }
        assertTrue(
            coachScreen.contains(
                "shareSafePastePrivacyCoach(coach.familyShareSummary)"
            )
        )
        assertTrue(coachScreen.contains("primaryButton(\"가족에게 개인정보 가리기 안내 공유\")"))
        assertTrue(coachScreen.contains("secondaryButton(\"홈으로\")"))

        assertEquals(
            1,
            Regex("coach\\.cards\\.forEach")
                .findAll(coachScreen)
                .count()
        )
        assertTrue(coachScreen.contains("recommended = card.id in recommendedIds"))
        listOf(
            "card.displayOrder",
            "card.title",
            "card.whyItMatters",
            "card.howToMaskManually",
            "card.whatNotToPasteOrShare",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { field ->
            assertTrue("safe-paste card UI omitted $field", cardRenderer.contains(field))
        }

        val statelessUiSource = coachScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            "incidents.add(",
            "incidents.clear(",
            "prefs.edit(",
            "safetyPlan.save(",
            "EditText(",
            "setText(",
            "text.toString(",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "doAfterTextChanged",
            "ScamAnalyzer.analyze(",
            "pendingSharedText",
            "CheckBox(",
            "RadioButton(",
            "Switch(",
            "selectedCoach",
            "coachProgress",
            "coachHistory",
            "checkmark",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "replace(",
            "Intent.ACTION_SEND"
        ).forEach { forbiddenStateOrTransformation ->
            assertFalse(
                "safe-paste UI used forbidden state/value/transformation: $forbiddenStateOrTransformation",
                statelessUiSource.contains(forbiddenStateOrTransformation, ignoreCase = true)
            )
        }
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))
        assertFalse(Regex("\\.text\\s*=").containsMatchIn(statelessUiSource))

        mapOf(
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAMPLE_LIBRARY" to "openSampleLibrary()",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.SAFE_PASTE_PRIVACY_COACH)",
            "INCIDENT_TIMELINE_BRIEFING" to "show(Screen.TIMELINE_BRIEFING)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        ).forEach { (actionId, destination) ->
            assertTrue(
                "safe-paste route omitted action: $actionId",
                normalizedActionRouter.contains("SafePastePrivacyCoachActionId.$actionId ->")
            )
            assertTrue(
                "safe-paste route omitted destination for $actionId: $destination",
                normalizedActionRouter.contains(destination)
            )
        }
        assertFalse(actionRouter.contains("dial("))
        assertFalse(actionRouter.contains("Intent.ACTION_DIAL"))
        assertFalse(actionRouter.contains("Intent.ACTION_CALL"))

        val shareArguments = Regex("shareSafePastePrivacyCoach\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertTrue(shareHelper.contains("개인정보 가리기 안내 공유"))
        assertEquals(
            1,
            Regex("putExtra\\(Intent\\.EXTRA_TEXT, summary\\)")
                .findAll(shareHelper)
                .count()
        )
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "ScamAnalysis",
            "currentAnalysis",
            "analysis.input",
            "IncidentPreviewRedactor",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "ContactsContract",
            "SmsManager",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "safe-paste share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource)
            )
        }

        listOf(
            "ContactsContract",
            "SmsManager",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "doAfterTextChanged",
            "AlarmManager",
            "NotificationManager",
            "NotificationCompat",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "ForegroundService",
            "BroadcastReceiver"
        ).forEach { forbiddenApi ->
            assertFalse(
                "safe-paste used forbidden API: $forbiddenApi",
                activitySource.contains(forbiddenApi) || modelSource.contains(forbiddenApi)
            )
        }
        val productionSources = findProjectPath("app/src/main")
            .walkTopDown()
            .filter { file ->
                file.isFile && (file.extension == "kt" || file.name == "AndroidManifest.xml")
            }
            .toList()
        listOf(
            "AlarmManager",
            "NotificationManager",
            "NotificationCompat",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "ForegroundService",
            "BroadcastReceiver",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "ContactsContract",
            "SmsManager",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "doAfterTextChanged",
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "java.net.Socket",
            "OkHttpClient",
            "Retrofit",
            "ktor.client"
        ).forEach { forbiddenProductionToken ->
            val hits = productionSources.filter { file ->
                file.readText().contains(forbiddenProductionToken)
            }
            assertTrue(
                "production source used forbidden token $forbiddenProductionToken in " +
                    hits.joinToString { it.path },
                hits.isEmpty()
            )
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

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

    private fun findProjectPath(relativePath: String): File {
        val userDirectory = requireNotNull(System.getProperty("user.dir"))
        var directory = File(userDirectory).absoluteFile
        while (true) {
            val candidate = File(directory, relativePath)
            if (candidate.exists()) return candidate
            directory = directory.parentFile
                ?: error("Could not find project path from $userDirectory: $relativePath")
        }
    }

    private fun readProjectFile(relativePath: String): String {
        val file = findProjectPath(relativePath)
        require(file.isFile) { "Project path is not a file: ${file.path}" }
        return file.readText()
    }
}
