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
            .substringBefore("private val match")
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
        assertFalse(statelessUiSource.contains("currentAnalysis ="))
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
