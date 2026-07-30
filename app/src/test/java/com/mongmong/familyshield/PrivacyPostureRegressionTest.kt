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
            "android.permission.SEND_SMS",
            "android.permission.RECEIVE_SMS",
            "android.permission.CALL_PHONE",
            "android.permission.READ_CALL_LOG",
            "android.permission.READ_PHONE_STATE",
            "android.permission.ANSWER_PHONE_CALLS",
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.RECORD_AUDIO",
            "android.permission.CAMERA",
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.INTERNET",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.POST_NOTIFICATIONS",
            "android.permission.SCHEDULE_EXACT_ALARM",
            "android.permission.RECEIVE_BOOT_COMPLETED",
            "android.permission.PACKAGE_USAGE_STATS",
            "android.permission.QUERY_ALL_PACKAGES",
            "android.permission.BIND_ACCESSIBILITY_SERVICE",
            "android.permission.BIND_DEVICE_ADMIN",
            "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
            "android.permission.BIND_VPN_SERVICE",
            "android.permission.REQUEST_INSTALL_PACKAGES"
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
            "ktor.client",
            "Intent.ACTION_CALL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "android.telecom",
            "android.telephony",
            "sendTextMessage"
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

    @Test
    fun activeContactExitScriptsArePureStatelessManualAndShareOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/ActiveContactExitScripts.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.safePastePrivacyCoachScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.activeContactExitScriptCard",
            "private fun openActiveContactExitScriptAction",
            "private fun shareActiveContactExitScripts"
        ).forEach { marker ->
            assertTrue("active-contact source marker missing: $marker", activitySource.contains(marker))
        }

        val homeScreen = functionBlock(activitySource, "private fun LinearLayout.home()")
        val resultScreen = functionBlock(activitySource, "private fun LinearLayout.result()")
        val quickStopScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.quickStopModeScreen"
        )
        val flowRouterScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.safetyFlowRouterScreen"
        )
        val safePasteScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.safePastePrivacyCoachScreen"
        )
        val debriefScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.familyCalmDebriefAgendaScreen"
        )
        val scriptsScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.activeContactExitScriptsScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.activeContactExitScriptCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openActiveContactExitScriptAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareActiveContactExitScripts"
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
            "ScamAnalyzer",
            "Regex(",
            "ContactsContract",
            "SmsManager",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener"
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "active-contact model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }
        assertFalse(
            Regex("analysis\\s*\\??\\s*\\.\\s*(?:input|score)")
                .containsMatchIn(modelSource)
        )

        assertTrue(activitySource.contains("ACTIVE_CONTACT_EXIT_SCRIPTS"))
        assertTrue(
            activitySource.contains(
                "Screen.ACTIVE_CONTACT_EXIT_SCRIPTS -> activeContactExitScriptsScreen()"
            )
        )
        listOf(
            homeScreen,
            resultScreen,
            quickStopScreen,
            flowRouterScreen,
            safePasteScreen,
            debriefScreen
        ).forEachIndexed { index, entryScreen ->
            assertTrue(
                "active-contact entry route missing at index $index",
                entryScreen.contains("show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)")
            )
        }

        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:activeContact|exitScript|selectedScript|scriptProgress|" +
                    "scriptHistory|callStatus|replyStatus|checkmark)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "KEY_\\w*(?:ACTIVE_CONTACT|EXIT_SCRIPT|SELECTED_SCRIPT|SCRIPT_PROGRESS|" +
                    "SCRIPT_HISTORY|CALL_STATUS|REPLY_STATUS|CHECKMARK)\\w*"
            ).containsMatchIn(activitySource)
        )

        assertTrue(scriptsScreen.contains("ActiveContactExitScriptsBuilder.build("))
        assertTrue(scriptsScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            scriptsScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(scriptsScreen.contains("incidentCount = incidents.count()"))
        assertFalse(scriptsScreen.contains("incidents.load("))

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
                "active-contact incident count decoded stored content: $forbiddenIncidentRead",
                incidentCountHelper.contains(forbiddenIncidentRead)
            )
        }

        listOf(
            "title(scripts.title)",
            "subtitle(scripts.intro)",
            "scripts.recommendationTitle",
            "scripts.recommendationExplanation",
            "scripts.recommendedCards",
            "scripts.recommendedActions",
            "scripts.cards",
            "scripts.privacySafetyNotes"
        ).forEach { renderedField ->
            assertTrue("active-contact UI omitted $renderedField", scriptsScreen.contains(renderedField))
        }
        assertTrue(
            scriptsScreen.contains(
                "shareActiveContactExitScripts(scripts.familyShareSummary)"
            )
        )
        assertTrue(scriptsScreen.contains("primaryButton(\"가족에게 종료 문장 안내 공유\")"))
        assertTrue(scriptsScreen.contains("secondaryButton(\"홈으로\")"))
        assertEquals(1, Regex("scripts\\.cards\\.forEach").findAll(scriptsScreen).count())
        assertTrue(scriptsScreen.contains("recommended = card.id in recommendedIds"))

        listOf(
            "card.displayOrder",
            "card.title",
            "card.scriptText",
            "card.whenToUse",
            "card.neverSayOrDo",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { field ->
            assertTrue("active-contact card UI omitted $field", cardRenderer.contains(field))
        }

        val statelessUiSource = scriptsScreen + cardRenderer + actionRouter
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
            "selectedScript",
            "scriptProgress",
            "scriptHistory",
            "callStatus",
            "replyStatus",
            "checkmark",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "Intent.ACTION_SEND"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "active-contact UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))

        mapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "INCIDENT_TIMELINE_BRIEFING" to "show(Screen.TIMELINE_BRIEFING)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)"
        ).forEach { (actionId, destination) ->
            assertTrue(
                "active-contact route omitted action: $actionId",
                normalizedActionRouter.contains("ActiveContactExitScriptActionId.$actionId ->")
            )
            assertTrue(
                "active-contact route omitted destination for $actionId: $destination",
                normalizedActionRouter.contains(destination)
            )
        }
        listOf(
            "dial(",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "SmsManager",
            "sendTextMessage",
            "ContactsContract",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip"
        ).forEach { forbiddenRouteApi ->
            assertFalse(
                "active-contact router used forbidden API: $forbiddenRouteApi",
                actionRouter.contains(forbiddenRouteApi)
            )
        }

        val shareArguments = Regex("shareActiveContactExitScripts\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("scripts.familyShareSummary"), shareArguments)
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertTrue(shareHelper.contains("가족에게 종료 문장 안내 공유"))
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
            "setPrimaryClip",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "active-contact share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource)
            )
        }

        val activeContactSliceSource = scriptsScreen + cardRenderer + actionRouter + shareHelper
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
            "sendTextMessage",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener"
        ).forEach { forbiddenSliceApi ->
            assertFalse(
                "active-contact slice used forbidden API: $forbiddenSliceApi",
                activeContactSliceSource.contains(forbiddenSliceApi) ||
                    modelSource.contains(forbiddenSliceApi)
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
            "Intent.ACTION_CALL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "android.telecom",
            "android.telephony",
            "sendTextMessage"
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

    @Test
    fun moneyTransferPauseGateIsPureStatelessManualAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MoneyTransferPauseGate.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.recoveryPlan24hScreen",
            "private fun LinearLayout.helpDirectoryScreen",
            "private fun LinearLayout.officialReportPrepScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.moneyTransferPauseCard",
            "private fun openMoneyTransferPauseAction",
            "private fun shareMoneyTransferPauseGate"
        ).forEach { marker ->
            assertTrue("money-pause source marker missing: $marker", activitySource.contains(marker))
        }

        val homeScreen = functionBlock(activitySource, "private fun LinearLayout.home()")
        val resultScreen = functionBlock(activitySource, "private fun LinearLayout.result()")
        val quickStopScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.quickStopModeScreen"
        )
        val activeContactScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.activeContactExitScriptsScreen"
        )
        val accountLockdownScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.accountLockdownChecklistScreen"
        )
        val flowRouterScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.safetyFlowRouterScreen"
        )
        val recoveryScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.recoveryPlan24hScreen"
        )
        val helpScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.helpDirectoryScreen"
        )
        val reportPrepScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.officialReportPrepScreen"
        )
        val gateScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.moneyTransferPauseGateScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.moneyTransferPauseCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openMoneyTransferPauseAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareMoneyTransferPauseGate"
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
            "ScamAnalyzer",
            "Regex(",
            "ContactsContract",
            "SmsManager",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "Payment",
            "BillingClient",
            "NfcAdapter",
            "AccountManager"
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "money-pause model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }
        assertFalse(
            Regex("analysis\\s*\\??\\s*\\.\\s*(?:input|score)")
                .containsMatchIn(modelSource)
        )

        assertTrue(activitySource.contains("MONEY_TRANSFER_PAUSE_GATE"))
        assertFalse(modelSource.contains("INCIDENT_TIMELINE_BRIEFING"))
        assertTrue(
            activitySource.contains(
                "Screen.MONEY_TRANSFER_PAUSE_GATE -> moneyTransferPauseGateScreen()"
            )
        )
        listOf(
            homeScreen,
            resultScreen,
            quickStopScreen,
            activeContactScreen,
            accountLockdownScreen,
            flowRouterScreen,
            recoveryScreen,
            helpScreen,
            reportPrepScreen
        ).forEachIndexed { index, entryScreen ->
            assertTrue(
                "money-pause entry route missing at index $index",
                entryScreen.contains("show(Screen.MONEY_TRANSFER_PAUSE_GATE)")
            )
        }

        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:moneyTransfer|pauseGate|selectedPayment|" +
                    "paymentStatus|paymentHistory|cardProgress|checkmark)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "KEY_\\w*(?:MONEY_TRANSFER|PAUSE_GATE|SELECTED_PAYMENT|PAYMENT_STATUS|" +
                    "PAYMENT_HISTORY|CARD_PROGRESS|CHECKMARK)\\w*"
            ).containsMatchIn(activitySource)
        )

        assertTrue(gateScreen.contains("MoneyTransferPauseGateBuilder.build("))
        assertTrue(gateScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            gateScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(gateScreen.contains("incidentCount = incidents.count()"))
        assertFalse(gateScreen.contains("incidents.load("))

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
                "money-pause incident count decoded stored content: $forbiddenIncidentRead",
                incidentCountHelper.contains(forbiddenIncidentRead)
            )
        }

        listOf(
            "title(gate.title)",
            "subtitle(gate.intro)",
            "gate.modeLabel",
            "gate.recommendationTitle",
            "gate.recommendationExplanation",
            "gate.recommendedCards",
            "gate.recommendedActions",
            "gate.cards",
            "gate.privacySafetyNotes"
        ).forEach { renderedField ->
            assertTrue("money-pause UI omitted $renderedField", gateScreen.contains(renderedField))
        }
        assertTrue(
            gateScreen.contains(
                "shareMoneyTransferPauseGate(gate.familyShareSummary)"
            )
        )
        assertTrue(gateScreen.contains("primaryButton(\"가족에게 송금 멈춤 안내 공유\")"))
        assertTrue(gateScreen.contains("secondaryButton(\"홈으로\")"))
        assertEquals(1, Regex("gate\\.cards\\.forEach").findAll(gateScreen).count())
        assertTrue(gateScreen.contains("recommended = card.id in recommendedIds"))

        listOf(
            "card.displayOrder",
            "card.title",
            "card.pauseSentence",
            "card.whyItMatters",
            "card.doNotDoBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { field ->
            assertTrue("money-pause card UI omitted $field", cardRenderer.contains(field))
        }

        val statelessUiSource = gateScreen + cardRenderer + actionRouter
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
            "selectedPayment",
            "paymentStatus",
            "paymentHistory",
            "cardProgress",
            "checkmark",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "Intent.ACTION_SEND"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "money-pause UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))

        mapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)",
            "RECOVERY_PLAN" to "show(Screen.RECOVERY_PLAN)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)"
        ).forEach { (actionId, destination) ->
            assertTrue(
                "money-pause route omitted exact branch for $actionId: $destination",
                normalizedActionRouter.contains(
                    "MoneyTransferPauseActionId.$actionId -> $destination"
                )
            )
        }
        assertFalse(actionRouter.contains("TIMELINE_BRIEFING"))
        listOf(
            "dial(",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "SmsManager",
            "sendTextMessage",
            "ContactsContract",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "Payment",
            "BillingClient",
            "NfcAdapter",
            "AccountManager"
        ).forEach { forbiddenRouteApi ->
            assertFalse(
                "money-pause router used forbidden API: $forbiddenRouteApi",
                actionRouter.contains(forbiddenRouteApi)
            )
        }

        val shareArguments = Regex("shareMoneyTransferPauseGate\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("gate.familyShareSummary"), shareArguments)
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertTrue(shareHelper.contains("가족에게 송금 멈춤 안내 공유"))
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
            "setPrimaryClip",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "Payment",
            "BillingClient",
            "NfcAdapter",
            "AccountManager"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "money-pause share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource)
            )
        }

        val gateSliceSource = gateScreen + cardRenderer + actionRouter + shareHelper
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
            "sendTextMessage",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "AutofillManager",
            "AutofillValue",
            "android.view.autofill",
            "CredentialManager",
            "android.credentials",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "MessageDigest",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "Payment",
            "BillingClient",
            "NfcAdapter",
            "AccountManager"
        ).forEach { forbiddenSliceApi ->
            assertFalse(
                "money-pause slice used forbidden API: $forbiddenSliceApi",
                gateSliceSource.contains(forbiddenSliceApi) ||
                    modelSource.contains(forbiddenSliceApi)
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
            "AutofillManager",
            "AutofillValue",
            "android.view.autofill",
            "CredentialManager",
            "android.credentials",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "MessageDigest",
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "java.net.Socket",
            "OkHttpClient",
            "Retrofit",
            "ktor.client",
            "Intent.ACTION_CALL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "android.telecom",
            "android.telephony",
            "sendTextMessage",
            "Payment",
            "BillingClient",
            "NfcAdapter",
            "AccountManager"
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

    @Test
    fun remoteAccessCutoffCoachIsPureStatelessManualAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/RemoteAccessCutoffCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.recoveryPlan24hScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.remoteAccessCutoffCard",
            "private fun openRemoteAccessCutoffAction",
            "private fun shareRemoteAccessCutoffCoach"
        ).forEach { marker ->
            assertTrue("remote-cutoff source marker missing: $marker", activitySource.contains(marker))
        }

        val entryScreens = listOf(
            functionBlock(activitySource, "private fun LinearLayout.home()"),
            functionBlock(activitySource, "private fun LinearLayout.result()"),
            functionBlock(activitySource, "private fun LinearLayout.quickStopModeScreen"),
            functionBlock(activitySource, "private fun LinearLayout.activeContactExitScriptsScreen"),
            functionBlock(activitySource, "private fun LinearLayout.moneyTransferPauseGateScreen"),
            functionBlock(activitySource, "private fun LinearLayout.accountLockdownChecklistScreen"),
            functionBlock(activitySource, "private fun LinearLayout.trustedHelperReviewModeScreen"),
            functionBlock(activitySource, "private fun LinearLayout.safetyFlowRouterScreen"),
            functionBlock(activitySource, "private fun LinearLayout.recoveryPlan24hScreen")
        )
        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.remoteAccessCutoffCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.remoteAccessCutoffCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openRemoteAccessCutoffAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareRemoteAccessCutoffCoach"
        )
        val valueFreeCallbackScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.safeVerificationCallScriptsScreen"
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
            "FAMILY_CALLBACK_CARD",
            "readiness.title",
            "readiness.checklist",
            "readiness.nextAction",
            "ScamAnalyzer",
            "ContactsContract",
            "SmsManager",
            "ClipboardManager",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "NotificationListenerService",
            "UsageStatsManager",
            "PackageInstaller",
            "Settings.",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "BillingClient",
            "NfcAdapter",
            "AccountManager"
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "remote-cutoff model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }
        assertFalse(
            Regex("analysis\\s*\\??\\s*\\.\\s*(?:input|score)")
                .containsMatchIn(modelSource)
        )

        assertTrue(activitySource.contains("REMOTE_ACCESS_CUTOFF_COACH"))
        assertTrue(
            activitySource.contains(
                "Screen.REMOTE_ACCESS_CUTOFF_COACH -> remoteAccessCutoffCoachScreen()"
            )
        )
        entryScreens.forEachIndexed { index, entryScreen ->
            assertTrue(
                "remote-cutoff entry route missing at index $index",
                entryScreen.contains("show(Screen.REMOTE_ACCESS_CUTOFF_COACH)")
            )
        }
        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:remoteAccess|cutoff|installedApps|packageList|" +
                    "deviceStatus|cardProgress|checkmark)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "KEY_\\w*(?:REMOTE_ACCESS|CUTOFF|INSTALLED_APPS|PACKAGE_LIST|" +
                    "DEVICE_STATUS|CARD_PROGRESS|CHECKMARK)\\w*"
            ).containsMatchIn(activitySource)
        )

        assertTrue(coachScreen.contains("RemoteAccessCutoffCoachBuilder.build("))
        assertTrue(coachScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            coachScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(coachScreen.contains("incidentCount = incidents.count()"))
        assertFalse(coachScreen.contains("incidents.load("))

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
                "remote-cutoff incident count decoded stored content: $forbiddenIncidentRead",
                incidentCountHelper.contains(forbiddenIncidentRead)
            )
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes"
        ).forEach { renderedField ->
            assertTrue("remote-cutoff UI omitted $renderedField", coachScreen.contains(renderedField))
        }
        assertTrue(
            coachScreen.contains(
                "shareRemoteAccessCutoffCoach(coach.familyShareSummary)"
            )
        )
        assertTrue(coachScreen.contains("primaryButton(\"가족에게 원격지시 중단 안내 공유\")"))
        assertTrue(coachScreen.contains("secondaryButton(\"홈으로\")"))
        assertEquals(1, Regex("coach\\.cards\\.forEach").findAll(coachScreen).count())
        assertTrue(coachScreen.contains("recommended = card.id in recommendedIds"))

        listOf(
            "card.displayOrder",
            "card.title",
            "card.cutoffSentence",
            "card.whyItMatters",
            "card.doNotDoBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { field ->
            assertTrue("remote-cutoff card UI omitted $field", cardRenderer.contains(field))
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
            "text.toString(",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "ScamAnalyzer.analyze(",
            "pendingSharedText",
            "CheckBox(",
            "RadioButton(",
            "Switch(",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "NotificationListenerService",
            "UsageStatsManager",
            "PackageInstaller",
            "deletePackage",
            "Settings.",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "Intent.ACTION_SEND"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "remote-cutoff UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))

        mapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)",
            "RECOVERY_PLAN" to "show(Screen.RECOVERY_PLAN)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)"
        ).forEach { (actionId, destination) ->
            assertTrue(
                "remote-cutoff route omitted exact branch for $actionId: $destination",
                normalizedActionRouter.contains(
                    "RemoteAccessCutoffActionId.$actionId -> $destination"
                )
            )
        }
        assertFalse(actionRouter.contains("openFamilyCallbackCard"))
        assertTrue(
            valueFreeCallbackScreen.contains("SafeVerificationCallScriptsBuilder.build(")
        )
        listOf(
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.number",
            "TrustedContact"
        ).forEach { forbiddenStoredLiteral ->
            assertFalse(
                "value-free callback destination rendered stored literal: $forbiddenStoredLiteral",
                valueFreeCallbackScreen.contains(forbiddenStoredLiteral)
            )
        }
        listOf(
            "dial(",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "SmsManager",
            "sendTextMessage",
            "ContactsContract",
            "ClipboardManager",
            "PackageManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "NotificationListenerService",
            "UsageStatsManager",
            "PackageInstaller",
            "BillingClient",
            "NfcAdapter",
            "AccountManager"
        ).forEach { forbiddenRouteApi ->
            assertFalse(
                "remote-cutoff router used forbidden API: $forbiddenRouteApi",
                actionRouter.contains(forbiddenRouteApi)
            )
        }

        val shareArguments = Regex("shareRemoteAccessCutoffCoach\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertTrue(shareHelper.contains("가족에게 원격지시 중단 안내 공유"))
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
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "NotificationListenerService",
            "UsageStatsManager",
            "PackageInstaller",
            "deletePackage",
            "Settings.",
            "prefs.edit(",
            "safetyPlan.save(",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "BillingClient",
            "NfcAdapter",
            "AccountManager"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "remote-cutoff share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource)
            )
        }

        val cutoffSliceSource = coachScreen + cardRenderer + actionRouter + shareHelper
        val forbiddenSliceApis = listOf(
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
            "sendTextMessage",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "NotificationListenerService",
            "UsageStatsManager",
            "PackageInstaller",
            "deletePackage",
            "Intent.ACTION_DELETE",
            "ACTION_UNINSTALL_PACKAGE",
            "ACTION_INSTALL_PACKAGE",
            "REQUEST_INSTALL_PACKAGES",
            "ACTION_MANAGE_UNKNOWN_APP_SOURCES",
            "ACTION_APPLICATION_DETAILS_SETTINGS",
            "Settings.",
            "getInstalledPackages",
            "getInstalledApplications",
            "queryIntentActivities",
            "getPackageInfo",
            "getApplicationInfo",
            "canRequestPackageInstalls",
            "installPackage",
            "setApplicationEnabledSetting",
            "clearApplicationUserData",
            "<queries",
            "AppOpsManager"
        )
        forbiddenSliceApis.forEach { forbiddenSliceApi ->
            assertFalse(
                "remote-cutoff slice used forbidden API: $forbiddenSliceApi",
                cutoffSliceSource.contains(forbiddenSliceApi) ||
                    modelSource.contains(forbiddenSliceApi)
            )
        }

        val productionSources = findProjectPath("app/src/main")
            .walkTopDown()
            .filter { file ->
                file.isFile && (file.extension == "kt" || file.name == "AndroidManifest.xml")
            }
            .toList()
        val forbiddenProductionTokens = forbiddenSliceApis
            .filterNot { token -> token == "Intent.ACTION_DIAL" } + listOf(
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "java.net.Socket",
            "OkHttpClient",
            "Retrofit",
            "ktor.client",
            "android.telecom",
            "android.telephony"
        )
        forbiddenProductionTokens.distinct().forEach { forbiddenProductionToken ->
            val hits = productionSources.filter { file ->
                file.readText().contains(forbiddenProductionToken)
            }
            assertTrue(
                "production source used forbidden token $forbiddenProductionToken in " +
                    hits.joinToString { it.path },
                hits.isEmpty()
            )
        }

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "RECORD_AUDIO",
            "INTERNET",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(
                "manifest declared forbidden permission token: $forbiddenPermission",
                manifest.contains(forbiddenPermission)
            )
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun separateChannelVerificationCoachIsPureStatelessAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SeparateChannelVerificationCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val entryMarkers = listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.familyCallbackCardScreen",
            "private fun LinearLayout.safeVerificationCallScriptsScreen",
            "private fun LinearLayout.helpDirectoryScreen",
            "private fun LinearLayout.officialReportPrepScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.privacyCenterScreen"
        )
        listOf(
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.separateChannelVerificationCard",
            "private fun openSeparateChannelVerificationAction",
            "private fun shareSeparateChannelVerificationCoach"
        ).forEach { marker ->
            assertTrue(
                "separate-channel source marker missing: $marker",
                activitySource.contains(marker)
            )
        }

        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.SEPARATE_CHANNEL_VERIFICATION_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.mapIndexed { index, marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "separate-channel entry must have one exact show-only button at index $index",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "dial(",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.ACTION_SEND",
            "openHelpDirectory",
            "openFamilyCallbackCard",
            "ContactsContract",
            "SmsManager",
            "ClipboardManager",
            "startActivity(",
            "setIntent("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(
                "separate-channel entry button used side effect: $forbiddenEntrySideEffect",
                entryRouteSource.contains(forbiddenEntrySideEffect)
            )
        }

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.separateChannelVerificationCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.separateChannelVerificationCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openSeparateChannelVerificationAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareSeparateChannelVerificationCoach"
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
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "ScamAnalyzer",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "dialNumber",
            "contact.number",
            "ContactsContract",
            "SmsManager",
            "ClipboardManager",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "NotificationListenerService",
            "UsageStatsManager",
            "PackageInstaller",
            "BillingClient",
            "NfcAdapter",
            "AccountManager"
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "separate-channel model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }
        assertFalse(
            Regex("analysis\\s*\\??\\s*\\.\\s*(?:input|score)")
                .containsMatchIn(modelSource)
        )
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(
                "separate-channel model embedded official number: $exactOfficialNumber",
                Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource)
            )
        }

        assertTrue(activitySource.contains("SEPARATE_CHANNEL_VERIFICATION_COACH"))
        assertTrue(
            activitySource.contains(
                "Screen.SEPARATE_CHANNEL_VERIFICATION_COACH ->"
            )
        )
        assertTrue(activitySource.contains("separateChannelVerificationCoachScreen()"))
        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:separateChannel|verificationStatus|officialNumber|" +
                    "channelValue|cardProgress|checkmark)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "KEY_\\w*(?:SEPARATE_CHANNEL|VERIFICATION_STATUS|OFFICIAL_NUMBER|" +
                    "CHANNEL_VALUE|CARD_PROGRESS|CHECKMARK)\\w*"
            ).containsMatchIn(activitySource)
        )

        assertTrue(coachScreen.contains("SeparateChannelVerificationCoachBuilder.build("))
        assertTrue(coachScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            coachScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(coachScreen.contains("incidentCount = incidents.count()"))
        assertFalse(coachScreen.contains("incidents.load("))

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
                "separate-channel incident count decoded content: $forbiddenIncidentRead",
                incidentCountHelper.contains(forbiddenIncidentRead)
            )
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareSeparateChannelVerificationCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue(
                "separate-channel UI omitted $renderedField",
                coachScreen.contains(renderedField)
            )
        }
        assertTrue(coachScreen.contains("primaryButton(\"가족에게 별도 경로 확인 안내 공유\")"))
        assertTrue(coachScreen.contains("secondaryButton(\"홈으로\")"))
        assertEquals(1, Regex("coach\\.cards\\.forEach").findAll(coachScreen).count())
        assertTrue(coachScreen.contains("recommended = card.id in recommendedIds"))

        listOf(
            "card.displayOrder",
            "card.title",
            "card.verificationSentence",
            "card.whenToUse",
            "card.whyItMatters",
            "card.doNotUseOrShareBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { field ->
            assertTrue("separate-channel card UI omitted $field", cardRenderer.contains(field))
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
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.number",
            "incidents.add(",
            "incidents.clear(",
            "prefs.edit(",
            "safetyPlan.save(",
            "EditText(",
            "text.toString(",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "ScamAnalyzer.analyze(",
            "pendingSharedText",
            "CheckBox(",
            "RadioButton(",
            "Switch(",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "openFamilyCallbackCard",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "Intent.ACTION_SEND"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "separate-channel UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))

        mapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        ).forEach { (actionId, destination) ->
            assertTrue(
                "separate-channel route omitted branch for $actionId: $destination",
                normalizedActionRouter.contains(
                    "SeparateChannelVerificationActionId.$actionId -> $destination"
                )
            )
        }
        listOf(
            "dial(",
            "openFamilyCallbackCard",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "ContactsContract",
            "SmsManager",
            "ClipboardManager",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "PackageManager",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService"
        ).forEach { forbiddenRouteApi ->
            assertFalse(
                "separate-channel router used forbidden API: $forbiddenRouteApi",
                actionRouter.contains(forbiddenRouteApi)
            )
        }

        val shareArguments = Regex("shareSeparateChannelVerificationCoach\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertTrue(shareHelper.contains("가족에게 별도 경로 확인 안내 공유"))
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
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "ContactsContract",
            "SmsManager",
            "ClipboardManager",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "prefs.edit(",
            "safetyPlan.save(",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "separate-channel share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource)
            )
        }

        val sliceSource = coachScreen + cardRenderer + actionRouter + shareHelper + modelSource
        val forbiddenSliceApis = listOf(
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
            "sendTextMessage",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "NotificationListenerService",
            "UsageStatsManager",
            "PackageInstaller",
            "deletePackage",
            "ACTION_MANAGE_UNKNOWN_APP_SOURCES",
            "ACTION_APPLICATION_DETAILS_SETTINGS",
            "Settings.",
            "getInstalledPackages",
            "getInstalledApplications",
            "queryIntentActivities",
            "getPackageInfo",
            "getApplicationInfo",
            "canRequestPackageInstalls",
            "setApplicationEnabledSetting",
            "clearApplicationUserData",
            "AppOpsManager",
            "HttpURLConnection",
            "HttpsURLConnection",
            "OkHttp",
            "Retrofit",
            "WebView",
            "Geocoder",
            "Patterns.WEB_URL",
            "Patterns.PHONE",
            "PhoneNumberUtils",
            "URLUtil",
            "InetAddress",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "incidents.add(",
            "incidents.clear(",
            "prefs.edit(",
            "safetyPlan.save("
        )
        forbiddenSliceApis.forEach { forbiddenSliceApi ->
            assertFalse(
                "separate-channel slice used forbidden API: $forbiddenSliceApi",
                sliceSource.contains(forbiddenSliceApi)
            )
        }

        val productionSources = findProjectPath("app/src/main")
            .walkTopDown()
            .filter { file ->
                file.isFile && (file.extension == "kt" || file.name == "AndroidManifest.xml")
            }
            .toList()
        val forbiddenProductionTokens = forbiddenSliceApis
            .filterNot { token ->
                token == "Intent.ACTION_DIAL" ||
                    token == "dialNumber" ||
                    token == "OfficialHelpDirectoryBuilder" ||
                    token == "directory.entries" ||
                    token == "incidents.add(" ||
                    token == "incidents.clear(" ||
                    token == "prefs.edit(" ||
                    token == "safetyPlan.save("
            } + listOf(
            "java.net.URL",
            "java.net.Socket",
            "OkHttpClient",
            "ktor.client",
            "android.telecom",
            "android.telephony"
        )
        forbiddenProductionTokens.distinct().forEach { forbiddenProductionToken ->
            val hits = productionSources.filter { file ->
                file.readText().contains(forbiddenProductionToken)
            }
            assertTrue(
                "production source used forbidden token $forbiddenProductionToken in " +
                    hits.joinToString { it.path },
                hits.isEmpty()
            )
        }

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "RECORD_AUDIO",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(
                "manifest declared forbidden permission token: $forbiddenPermission",
                manifest.contains(forbiddenPermission)
            )
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun verificationOutcomeNextStepsBoardIsPureStatelessAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/VerificationOutcomeNextStepsBoard.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val entryMarkers = listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.helpDirectoryScreen",
            "private fun LinearLayout.officialReportPrepScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.privacyCenterScreen"
        )
        listOf(
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsCard",
            "private fun openVerificationOutcomeNextStepsAction",
            "private fun shareVerificationOutcomeNextStepsBoard"
        ).forEach { marker ->
            assertTrue(
                "verification-outcome source marker missing: $marker",
                activitySource.contains(marker)
            )
        }

        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.mapIndexed { index, marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "verification-outcome entry must have one exact show-only button at index $index",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "dial(",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.ACTION_SEND",
            "openHelpDirectory",
            "openEvidenceKit",
            "openFamilyCallbackCard",
            "ContactsContract",
            "SmsManager",
            "ClipboardManager",
            "startActivity(",
            "setIntent("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(
                "verification-outcome entry button used side effect: $forbiddenEntrySideEffect",
                entryRouteSource.contains(forbiddenEntrySideEffect)
            )
        }

        val boardScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.verificationOutcomeNextStepsCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openVerificationOutcomeNextStepsAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareVerificationOutcomeNextStepsBoard"
        )
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))
        assertTrue(
            Regex("analysis\\s*\\?\\.\\s*riskLevel").containsMatchIn(modelSource)
        )
        assertTrue(
            Regex("analysis\\s*\\?\\.\\s*matchedSignals").containsMatchIn(modelSource)
        )
        assertTrue(Regex("readiness\\s*\\.\\s*level").containsMatchIn(modelSource))
        assertTrue(
            Regex("incidentCount\\s*\\.\\s*coerceAtLeast\\(0\\)")
                .containsMatchIn(modelSource)
        )
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "SafetyPlan",
            "SafetyPlanStore",
            "IncidentStore",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "ScamAnalyzer",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "dialNumber",
            "contact.number",
            "ContactsContract",
            "SmsManager",
            "ClipboardManager",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "NotificationListenerService",
            "UsageStatsManager",
            "PackageInstaller",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "ANDROID_ID",
            "Build.SERIAL",
            "AdvertisingIdClient",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "verification-outcome model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }
        assertFalse(
            Regex("analysis\\s*\\??\\s*\\.\\s*(?:input|score|actionPlan|summary)")
                .containsMatchIn(modelSource)
        )
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(
                "verification-outcome model embedded official number: $exactOfficialNumber",
                Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource)
            )
        }

        assertTrue(activitySource.contains("VERIFICATION_OUTCOME_NEXT_STEPS_BOARD"))
        assertTrue(
            activitySource.contains(
                "Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->"
            )
        )
        assertTrue(activitySource.contains("verificationOutcomeNextStepsBoardScreen()"))
        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:verificationOutcome|outcomeStatus|verified|" +
                    "officialNumber|channelValue|cardProgress|checkmark)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "KEY_\\w*(?:VERIFICATION_OUTCOME|OUTCOME_STATUS|VERIFIED|OFFICIAL_NUMBER|" +
                    "CHANNEL_VALUE|CARD_PROGRESS|CHECKMARK)\\w*"
            ).containsMatchIn(activitySource)
        )

        assertTrue(boardScreen.contains("VerificationOutcomeNextStepsBoardBuilder.build("))
        assertTrue(boardScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            boardScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(boardScreen.contains("incidentCount = incidents.count()"))
        assertFalse(boardScreen.contains("incidents.load("))

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
                "verification-outcome incident count decoded content: $forbiddenIncidentRead",
                incidentCountHelper.contains(forbiddenIncidentRead)
            )
        }

        listOf(
            "title(board.title)",
            "subtitle(board.intro)",
            "board.modeLabel",
            "board.recommendationTitle",
            "board.recommendationExplanation",
            "board.recommendedCards",
            "board.recommendedActions",
            "board.cards",
            "board.privacySafetyNotes",
            "shareVerificationOutcomeNextStepsBoard(board.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue(
                "verification-outcome UI omitted $renderedField",
                boardScreen.contains(renderedField)
            )
        }
        assertTrue(
            boardScreen.contains("primaryButton(\"가족에게 값 없는 결과별 다음 단계 공유\")")
        )
        assertTrue(boardScreen.contains("secondaryButton(\"홈으로\")"))
        assertEquals(1, Regex("board\\.cards\\.forEach").findAll(boardScreen).count())
        assertTrue(boardScreen.contains("recommended = card.id in recommendedIds"))

        listOf(
            "card.displayOrder",
            "card.title",
            "card.outcomeCondition",
            "card.nextStepSentence",
            "card.whyItMatters",
            "card.doNotRecordOrShareBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { field ->
            assertTrue(
                "verification-outcome card UI omitted $field",
                cardRenderer.contains(field)
            )
        }

        val statelessUiSource = boardScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.number",
            "incidents.add(",
            "incidents.clear(",
            "prefs.edit(",
            "safetyPlan.save(",
            "EditText(",
            "text.toString(",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "ScamAnalyzer.analyze(",
            "pendingSharedText",
            "CheckBox(",
            "RadioButton(",
            "Switch(",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "openFamilyCallbackCard",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "Intent.ACTION_SEND"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "verification-outcome UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))

        mapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)",
            "RECOVERY_PLAN" to "show(Screen.RECOVERY_PLAN)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)"
        ).forEach { (actionId, destination) ->
            assertTrue(
                "verification-outcome route omitted branch for $actionId: $destination",
                normalizedActionRouter.contains(
                    "VerificationOutcomeNextStepsActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            21,
            Regex("VerificationOutcomeNextStepsActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        listOf(
            "dial(",
            "openFamilyCallbackCard",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "ContactsContract",
            "SmsManager",
            "ClipboardManager",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PackageManager",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            ".save("
        ).forEach { forbiddenRouteApi ->
            assertFalse(
                "verification-outcome router used forbidden API: $forbiddenRouteApi",
                actionRouter.contains(forbiddenRouteApi)
            )
        }

        val shareArguments = Regex("shareVerificationOutcomeNextStepsBoard\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("board.familyShareSummary"), shareArguments)
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertTrue(shareHelper.contains("가족에게 결과별 다음 단계 공유"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "ScamAnalysis",
            "currentAnalysis",
            "analysis.input",
            "IncidentPreviewRedactor",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "ContactsContract",
            "SmsManager",
            "ClipboardManager",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "prefs.edit(",
            "safetyPlan.save(",
            "putString(",
            "putBoolean(",
            ".save(",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "verification-outcome share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource)
            )
        }

        val sliceSource = boardScreen + cardRenderer + actionRouter + shareHelper + modelSource
        val forbiddenSliceApis = listOf(
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
            "sendTextMessage",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "NotificationListenerService",
            "UsageStatsManager",
            "PackageInstaller",
            "deletePackage",
            "Intent.ACTION_DELETE",
            "ACTION_UNINSTALL_PACKAGE",
            "ACTION_INSTALL_PACKAGE",
            "REQUEST_INSTALL_PACKAGES",
            "ACTION_MANAGE_UNKNOWN_APP_SOURCES",
            "ACTION_APPLICATION_DETAILS_SETTINGS",
            "Settings.",
            "ANDROID_ID",
            "Build.SERIAL",
            "AdvertisingIdClient",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "queryIntentActivities",
            "getPackageInfo",
            "getApplicationInfo",
            "canRequestPackageInstalls",
            "installPackage",
            "setApplicationEnabledSetting",
            "clearApplicationUserData",
            "AppOpsManager",
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "java.net.Socket",
            "OkHttp",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Geocoder",
            "Patterns.WEB_URL",
            "Patterns.PHONE",
            "PhoneNumberUtils",
            "URLUtil",
            "InetAddress",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "analysis.input",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        )
        forbiddenSliceApis.forEach { forbiddenSliceApi ->
            assertFalse(
                "verification-outcome slice used forbidden API/source: $forbiddenSliceApi",
                sliceSource.contains(forbiddenSliceApi)
            )
        }

        val productionSources = findProjectPath("app/src/main")
            .walkTopDown()
            .filter { file ->
                file.isFile && (file.extension == "kt" || file.name == "AndroidManifest.xml")
            }
            .toList()
        val legacyProductionTokens = setOf(
            "Intent.ACTION_DIAL",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "analysis.input",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        )
        val forbiddenProductionTokens = forbiddenSliceApis
            .filterNot { token -> token in legacyProductionTokens } + listOf(
            "OkHttpClient",
            "android.telecom",
            "android.telephony"
        )
        forbiddenProductionTokens.distinct().forEach { forbiddenProductionToken ->
            val hits = productionSources.filter { file ->
                file.readText().contains(forbiddenProductionToken)
            }
            assertTrue(
                "production source used forbidden token $forbiddenProductionToken in " +
                    hits.joinToString { it.path },
                hits.isEmpty()
            )
        }

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "RECORD_AUDIO",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(
                "manifest declared forbidden permission token: $forbiddenPermission",
                manifest.contains(forbiddenPermission)
            )
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun recontactBoundaryCoachIsPureStatelessAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/RecontactBoundaryCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val entryMarkers = listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.privacyCenterScreen"
        )
        listOf(
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.recontactBoundaryCard",
            "private fun openRecontactBoundaryAction",
            "private fun shareRecontactBoundaryCoach"
        ).forEach { marker ->
            assertTrue(
                "recontact-boundary source marker missing: $marker",
                activitySource.contains(marker)
            )
        }

        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.RECONTACT_BOUNDARY_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.mapIndexed { index, marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "recontact-boundary entry must have one exact show-only button at index $index",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(13, entryRouteSources.size)
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "dial(",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.ACTION_SEND",
            "openHelpDirectory",
            "openEvidenceKit",
            "openFamilyCallbackCard",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "ClipboardManager",
            "startActivity(",
            "setIntent("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(
                "recontact-boundary entry button used side effect: $forbiddenEntrySideEffect",
                entryRouteSource.contains(forbiddenEntrySideEffect)
            )
        }

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.recontactBoundaryCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.recontactBoundaryCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openRecontactBoundaryAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareRecontactBoundaryCoach"
        )
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))
        assertTrue(
            Regex("analysis\\s*\\?\\.\\s*riskLevel").containsMatchIn(modelSource)
        )
        assertTrue(
            Regex("analysis\\s*\\?\\.\\s*matchedSignals").containsMatchIn(modelSource)
        )
        assertTrue(Regex("readiness\\s*\\.\\s*level").containsMatchIn(modelSource))
        assertTrue(
            Regex("incidentCount\\s*\\.\\s*coerceAtLeast\\(0\\)")
                .containsMatchIn(modelSource)
        )
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "SafetyPlanStore",
            "IncidentStore",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "ScamAnalyzer",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "dialNumber",
            "contact.number",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "NotificationManager",
            "NotificationListenerService",
            "CallScreeningService",
            "TelecomManager",
            "TelephonyManager",
            "PhoneStateListener",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "ANDROID_ID",
            "Build.SERIAL",
            "AdvertisingIdClient",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "recontact-boundary model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }
        assertFalse(
            Regex("analysis\\s*\\??\\s*\\.\\s*(?:input|score|actionPlan|summary)")
                .containsMatchIn(modelSource)
        )
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(
                "recontact-boundary model embedded official number: $exactOfficialNumber",
                Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource)
            )
        }

        assertTrue(activitySource.contains("RECONTACT_BOUNDARY_COACH"))
        assertTrue(activitySource.contains("Screen.RECONTACT_BOUNDARY_COACH ->"))
        assertTrue(activitySource.contains("recontactBoundaryCoachScreen()"))
        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:recontact|boundary|blocked|muted|silenced|" +
                    "cardProgress|checkmark|contactChannel|notificationState)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "KEY_\\w*(?:RECONTACT|BOUNDARY|BLOCKED|MUTED|SILENCED|CARD_PROGRESS|" +
                    "CHECKMARK|CONTACT_CHANNEL|NOTIFICATION_STATE)\\w*"
            ).containsMatchIn(activitySource)
        )

        assertTrue(coachScreen.contains("RecontactBoundaryCoachBuilder.build("))
        assertTrue(coachScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            coachScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(coachScreen.contains("incidentCount = incidents.count()"))
        assertFalse(coachScreen.contains("incidents.load("))

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
                "recontact-boundary incident count decoded content: $forbiddenIncidentRead",
                incidentCountHelper.contains(forbiddenIncidentRead)
            )
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareRecontactBoundaryCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue(
                "recontact-boundary UI omitted $renderedField",
                coachScreen.contains(renderedField)
            )
        }
        assertTrue(
            coachScreen.contains("primaryButton(\"가족에게 값 없는 재연락 경계 안내 공유\")")
        )
        assertTrue(coachScreen.contains("secondaryButton(\"홈으로\")"))
        assertEquals(1, Regex("coach\\.cards\\.forEach").findAll(coachScreen).count())
        assertTrue(coachScreen.contains("recommended = card.id in recommendedIds"))

        listOf(
            "card.displayOrder",
            "card.title",
            "card.recontactScenario",
            "card.boundaryScript",
            "card.whyItMatters",
            "card.doNotRecordOrShareBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { field ->
            assertTrue(
                "recontact-boundary card UI omitted $field",
                cardRenderer.contains(field)
            )
        }

        val statelessUiSource = coachScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "analysis.score",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.number",
            "incidents.add(",
            "incidents.clear(",
            "prefs.edit(",
            "safetyPlan.save(",
            "EditText(",
            "text.toString(",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "ScamAnalyzer.analyze(",
            "pendingSharedText",
            "CheckBox(",
            "RadioButton(",
            "Switch(",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationListenerService",
            "CallScreeningService",
            "recontactStatus",
            "boundaryStatus",
            "blockedStatus",
            "mutedStatus",
            "silencedStatus",
            "cardProgress",
            "checkmark"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "recontact-boundary UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))

        mapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.RECONTACT_BOUNDARY_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "RECOVERY_PLAN" to "show(Screen.RECOVERY_PLAN)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        ).forEach { (actionId, destination) ->
            assertTrue(
                "recontact-boundary route omitted branch for $actionId: $destination",
                normalizedActionRouter.contains(
                    "RecontactBoundaryActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            23,
            Regex("RecontactBoundaryActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        listOf(
            "dial(",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "NotificationManager",
            "NotificationListenerService",
            "CallScreeningService",
            "TelecomManager",
            "TelephonyManager",
            "PhoneStateListener",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PackageManager",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            ".save("
        ).forEach { forbiddenRouteApi ->
            assertFalse(
                "recontact-boundary router used forbidden API: $forbiddenRouteApi",
                actionRouter.contains(forbiddenRouteApi)
            )
        }

        val shareArguments = Regex("shareRecontactBoundaryCoach\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertTrue(shareHelper.contains("가족에게 재연락 경계 안내 공유"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "ScamAnalysis",
            "currentAnalysis",
            "analysis.input",
            "IncidentPreviewRedactor",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "NotificationManager",
            "NotificationListenerService",
            "CallScreeningService",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "prefs.edit(",
            "safetyPlan.save(",
            "putString(",
            "putBoolean(",
            ".save(",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "recontactStatus",
            "boundaryStatus",
            "blockedStatus",
            "mutedStatus",
            "silencedStatus"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "recontact-boundary share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource, ignoreCase = true)
            )
        }

        val sliceSource = coachScreen + cardRenderer + actionRouter + shareHelper + modelSource
        val forbiddenSliceApis = listOf(
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
            "CallLog",
            "SmsManager",
            "sendTextMessage",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "NotificationListenerService",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "deletePackage",
            "Intent.ACTION_DELETE",
            "ACTION_UNINSTALL_PACKAGE",
            "ACTION_INSTALL_PACKAGE",
            "REQUEST_INSTALL_PACKAGES",
            "ACTION_MANAGE_UNKNOWN_APP_SOURCES",
            "ACTION_APPLICATION_DETAILS_SETTINGS",
            "Settings.",
            "ANDROID_ID",
            "Build.SERIAL",
            "AdvertisingIdClient",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "queryIntentActivities",
            "getPackageInfo",
            "getApplicationInfo",
            "canRequestPackageInstalls",
            "installPackage",
            "setApplicationEnabledSetting",
            "clearApplicationUserData",
            "AppOpsManager",
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "java.net.Socket",
            "OkHttp",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Geocoder",
            "Patterns.WEB_URL",
            "Patterns.PHONE",
            "PhoneNumberUtils",
            "URLUtil",
            "InetAddress",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "analysis.input",
            "analysis.score",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save(",
            "recontactStatus",
            "boundaryStatus",
            "blockedStatus",
            "mutedStatus",
            "silencedStatus",
            "cardProgress",
            "checkmark"
        )
        forbiddenSliceApis.forEach { forbiddenSliceApi ->
            assertFalse(
                "recontact-boundary slice used forbidden API/source: $forbiddenSliceApi",
                sliceSource.contains(forbiddenSliceApi, ignoreCase = true)
            )
        }

        val productionSources = findProjectPath("app/src/main")
            .walkTopDown()
            .filter { file ->
                file.isFile && (file.extension == "kt" || file.name == "AndroidManifest.xml")
            }
            .toList()
        val legacyProductionTokens = setOf(
            "Intent.ACTION_DIAL",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "analysis.input",
            "analysis.score",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        )
        val forbiddenProductionTokens = forbiddenSliceApis
            .filterNot { token -> token in legacyProductionTokens } + listOf(
            "OkHttpClient",
            "android.telecom",
            "android.telephony"
        )
        forbiddenProductionTokens.distinct().forEach { forbiddenProductionToken ->
            val hits = productionSources.filter { file ->
                file.readText().contains(forbiddenProductionToken)
            }
            assertTrue(
                "production source used forbidden token $forbiddenProductionToken in " +
                    hits.joinToString { it.path },
                hits.isEmpty()
            )
        }

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(
                "manifest declared forbidden permission token: $forbiddenPermission",
                manifest.contains(forbiddenPermission)
            )
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun evidenceHandoffPrivacyCoachIsPureStatelessAndSharesOnlyBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/EvidenceHandoffPrivacyCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val safetyPlanStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val entryMarkers = listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.evidencePreservationScreen",
            "private fun LinearLayout.officialReportPrepScreen",
            "private fun LinearLayout.privacyCenterScreen",
            "private fun LinearLayout.safePastePrivacyCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen"
        )
        listOf(
            "private fun LinearLayout.evidenceHandoffPrivacyCoachScreen",
            "private fun LinearLayout.evidenceHandoffPrivacyCard",
            "private fun openEvidenceHandoffPrivacyAction",
            "private fun shareEvidenceHandoffPrivacyCoach"
        ).forEach { marker ->
            assertTrue(
                "evidence-handoff source marker missing: $marker",
                activitySource.contains(marker)
            )
        }

        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.EVIDENCE_HANDOFF_PRIVACY_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.mapIndexed { index, marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "evidence-handoff entry must have one exact show-only button at index $index",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(15, entryRouteSources.size)
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "ClipboardManager",
            "startActivity(",
            "setIntent("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(
                "evidence-handoff entry button used side effect: $forbiddenEntrySideEffect",
                entryRouteSource.contains(forbiddenEntrySideEffect)
            )
        }

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.evidenceHandoffPrivacyCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.evidenceHandoffPrivacyCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openEvidenceHandoffPrivacyAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareEvidenceHandoffPrivacyCoach"
        )
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))
        assertTrue(Regex("analysis\\s*\\?\\.\\s*riskLevel").containsMatchIn(modelSource))
        assertTrue(Regex("analysis\\s*\\?\\.\\s*matchedSignals").containsMatchIn(modelSource))
        assertTrue(Regex("readiness\\s*\\.\\s*level").containsMatchIn(modelSource))
        assertTrue(
            Regex("incidentCount\\s*\\.\\s*coerceAtLeast\\(0\\)")
                .containsMatchIn(modelSource)
        )
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "SafetyPlanStore",
            "IncidentStore",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "ScamAnalyzer",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "directory.entries",
            "dialNumber",
            "contact.number",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "NotificationManager",
            "NotificationListenerService",
            "CallScreeningService",
            "TelecomManager",
            "TelephonyManager",
            "PhoneStateListener",
            "MediaStore",
            "FileProvider",
            "Uri.parse",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "TextRecognition",
            "TextRecognizer",
            "InputImage",
            "ImageAnalysis",
            "BitmapFactory",
            "ExifInterface",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "ANDROID_ID",
            "Build.SERIAL",
            "AdvertisingIdClient",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "evidence-handoff model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }
        assertFalse(
            Regex("analysis\\s*\\??\\s*\\.\\s*(?:input|score|actionPlan|summary)")
                .containsMatchIn(modelSource)
        )
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(
                "evidence-handoff model embedded official number: $exactOfficialNumber",
                Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource)
            )
        }

        assertTrue(activitySource.contains("EVIDENCE_HANDOFF_PRIVACY_COACH"))
        assertTrue(activitySource.contains("Screen.EVIDENCE_HANDOFF_PRIVACY_COACH ->"))
        assertTrue(activitySource.contains("evidenceHandoffPrivacyCoachScreen()"))
        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:evidenceHandoff|handoffStatus|attachmentState|" +
                    "shareRecipient|recipientName|sentStatus|cardProgress|checkmark)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "KEY_\\w*(?:EVIDENCE_HANDOFF|HANDOFF_STATUS|ATTACHMENT_STATE|SHARE_RECIPIENT|" +
                    "RECIPIENT_NAME|SENT_STATUS|CARD_PROGRESS|CHECKMARK)\\w*"
            ).containsMatchIn(activitySource)
        )
        listOf("EvidenceHandoffPrivacy", "evidence_handoff", "handoff_status", "attachment_state")
            .forEach { forbiddenStoredState ->
                assertFalse(incidentStoreSource.contains(forbiddenStoredState, ignoreCase = true))
                assertFalse(safetyPlanStoreSource.contains(forbiddenStoredState, ignoreCase = true))
            }

        assertTrue(coachScreen.contains("EvidenceHandoffPrivacyCoachBuilder.build("))
        assertTrue(coachScreen.contains("analysis = currentAnalysis"))
        assertTrue(
            coachScreen.contains(
                "readiness = FamilyReadinessBuilder.build(safetyPlan.load())"
            )
        )
        assertTrue(coachScreen.contains("incidentCount = incidents.count()"))
        assertFalse(coachScreen.contains("incidents.load("))

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
                "evidence-handoff incident count decoded content: $forbiddenIncidentRead",
                incidentCountHelper.contains(forbiddenIncidentRead)
            )
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareEvidenceHandoffPrivacyCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue(
                "evidence-handoff UI omitted $renderedField",
                coachScreen.contains(renderedField)
            )
        }
        assertTrue(
            coachScreen.contains("primaryButton(\"가족에게 값 없는 증거 전달 경계 공유\")")
        )
        assertTrue(coachScreen.contains("secondaryButton(\"홈으로\")"))
        assertEquals(1, Regex("coach\\.cards\\.forEach").findAll(coachScreen).count())
        assertTrue(coachScreen.contains("recommended = card.id in recommendedIds"))

        listOf(
            "card.displayOrder",
            "card.title",
            "card.handoffScenario",
            "card.privacyActionSentence",
            "card.whyItMatters",
            "card.doNotAttachRecordOrShareBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { field ->
            assertTrue(
                "evidence-handoff card UI omitted $field",
                cardRenderer.contains(field)
            )
        }

        val statelessUiSource = coachScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "analysis.score",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.number",
            "incidents.add(",
            "incidents.clear(",
            "prefs.edit(",
            "safetyPlan.save(",
            "EditText(",
            "text.toString(",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "ScamAnalyzer.analyze(",
            "pendingSharedText",
            "CheckBox(",
            "RadioButton(",
            "Switch(",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "MediaStore",
            "FileProvider",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "TextRecognition",
            "ImageAnalysis",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationListenerService",
            "CallScreeningService",
            "handoffStatus",
            "attachmentState",
            "recipientName",
            "sentStatus",
            "cardProgress",
            "checkmark"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "evidence-handoff UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))

        mapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFE_PASTE_PRIVACY_COACH" to "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "INCIDENT_TIMELINE_BRIEFING" to "show(Screen.TIMELINE_BRIEFING)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "RECOVERY_PLAN" to "show(Screen.RECOVERY_PLAN)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        ).forEach { (actionId, destination) ->
            assertTrue(
                "evidence-handoff route omitted branch for $actionId: $destination",
                normalizedActionRouter.contains(
                    "EvidenceHandoffPrivacyActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            24,
            Regex("EvidenceHandoffPrivacyActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        listOf(
            "dial(",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.ACTION_SEND",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "MediaStore",
            "FileProvider",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "TextRecognition",
            "ImageAnalysis",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "NotificationManager",
            "NotificationListenerService",
            "CallScreeningService",
            "TelecomManager",
            "TelephonyManager",
            "PhoneStateListener",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PackageManager",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            ".save("
        ).forEach { forbiddenRouteApi ->
            assertFalse(
                "evidence-handoff router used forbidden API: $forbiddenRouteApi",
                actionRouter.contains(forbiddenRouteApi)
            )
        }

        val shareArguments = Regex("shareEvidenceHandoffPrivacyCoach\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertTrue(shareHelper.contains("가족에게 증거 전달 경계 공유"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "ScamAnalysis",
            "currentAnalysis",
            "analysis.input",
            "IncidentPreviewRedactor",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "NotificationManager",
            "NotificationListenerService",
            "CallScreeningService",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "prefs.edit(",
            "safetyPlan.save(",
            "putString(",
            "putBoolean(",
            ".save(",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "handoffStatus",
            "attachmentState",
            "recipientName",
            "sentStatus"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "evidence-handoff share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource, ignoreCase = true)
            )
        }

        val sliceSource = coachScreen + cardRenderer + actionRouter + shareHelper + modelSource
        val forbiddenSliceApis = listOf(
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
            "CallLog",
            "SmsManager",
            "sendTextMessage",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "android.permission.CAMERA",
            "android.hardware.camera",
            "androidx.camera",
            "CameraManager",
            "ImageCapture",
            "PhotoPicker",
            "PickVisualMedia",
            "DocumentsContract",
            "ContentResolver",
            "openInputStream",
            "java.io.File",
            "com.google.mlkit",
            "MlKit",
            "MLKit",
            "TextRecognition",
            "TextRecognizer",
            "InputImage",
            "ImageAnalysis",
            "BitmapFactory",
            "ExifInterface",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "NotificationListenerService",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "deletePackage",
            "Intent.ACTION_DELETE",
            "ACTION_UNINSTALL_PACKAGE",
            "ACTION_INSTALL_PACKAGE",
            "REQUEST_INSTALL_PACKAGES",
            "ACTION_MANAGE_UNKNOWN_APP_SOURCES",
            "ACTION_APPLICATION_DETAILS_SETTINGS",
            "Settings.",
            "ANDROID_ID",
            "Build.SERIAL",
            "AdvertisingIdClient",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "queryIntentActivities",
            "getPackageInfo",
            "getApplicationInfo",
            "canRequestPackageInstalls",
            "installPackage",
            "setApplicationEnabledSetting",
            "clearApplicationUserData",
            "AppOpsManager",
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "java.net.Socket",
            "OkHttp",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Geocoder",
            "Patterns.WEB_URL",
            "Patterns.PHONE",
            "PhoneNumberUtils",
            "URLUtil",
            "InetAddress",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "analysis.input",
            "analysis.score",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save(",
            "handoffStatus",
            "attachmentState",
            "recipientName",
            "sentStatus",
            "cardProgress",
            "checkmark"
        )
        forbiddenSliceApis.forEach { forbiddenSliceApi ->
            assertFalse(
                "evidence-handoff slice used forbidden API/source: $forbiddenSliceApi",
                sliceSource.contains(forbiddenSliceApi, ignoreCase = true)
            )
        }

        val productionSources = findProjectPath("app/src/main")
            .walkTopDown()
            .filter { file ->
                file.isFile && (file.extension == "kt" || file.name == "AndroidManifest.xml")
            }
            .toList()
        val repoWideForbiddenTokens = listOf(
            "AlarmManager",
            "NotificationManager",
            "NotificationCompat",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "ForegroundService",
            "BroadcastReceiver",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "android.permission.CAMERA",
            "androidx.camera",
            "CameraManager",
            "ImageCapture",
            "PhotoPicker",
            "PickVisualMedia",
            "DocumentsContract",
            "com.google.mlkit",
            "TextRecognition",
            "TextRecognizer",
            "InputImage",
            "ImageAnalysis",
            "BitmapFactory",
            "ExifInterface",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "Intent.ACTION_CALL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "NotificationListenerService",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "deletePackage",
            "ACTION_MANAGE_UNKNOWN_APP_SOURCES",
            "ACTION_APPLICATION_DETAILS_SETTINGS",
            "AppOpsManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "HttpURLConnection",
            "HttpsURLConnection",
            "OkHttpClient",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Geocoder",
            "Intent.ACTION_VIEW"
        )
        repoWideForbiddenTokens.forEach { forbiddenProductionToken ->
            val hits = productionSources.filter { file ->
                file.readText().contains(forbiddenProductionToken)
            }
            assertTrue(
                "production source used forbidden token $forbiddenProductionToken in " +
                    hits.joinToString { it.path },
                hits.isEmpty()
            )
        }

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "CAMERA",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(
                "manifest declared forbidden permission token: $forbiddenPermission",
                manifest.contains(forbiddenPermission)
            )
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun familySafeWordResetCoachIsPureStatelessRoutesOnlyAndSharesBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/FamilySafeWordResetCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val safetyPlanStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.familySafeWordResetCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.familySafeWordResetCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openFamilySafeWordResetAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareFamilySafeWordResetCoach"
        )
        val normalizedActivitySource = activitySource.replace(Regex("\\s+"), " ")
        val normalizedModelSource = modelSource.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        listOf(
            "private fun LinearLayout.familySafeWordResetCoachScreen",
            "private fun LinearLayout.familySafeWordResetCard",
            "private fun openFamilySafeWordResetAction",
            "private fun shareFamilySafeWordResetCoach"
        ).forEach { marker ->
            assertTrue(
                "safe-word reset source marker missing: $marker",
                activitySource.contains(marker)
            )
        }
        assertTrue(activitySource.contains("FAMILY_SAFE_WORD_RESET_COACH"))
        assertTrue(
            normalizedActivitySource.contains(
                "Screen.FAMILY_SAFE_WORD_RESET_COACH -> familySafeWordResetCoachScreen()"
            )
        )

        val entryMarkers = listOf(
            "private fun LinearLayout.readinessSection()",
            "private fun LinearLayout.safetyPlanScreen()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.safeVerificationCallScriptsScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.familyCallbackCardScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.privacyCenterScreen"
        )
        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.FAMILY_SAFE_WORD_RESET_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.map { marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "safe-word reset entry must have one show-only button in $marker",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(16, entryRouteSources.size)
        assertEquals(
            31,
            Regex("show\\(Screen\\.FAMILY_SAFE_WORD_RESET_COACH\\)")
                .findAll(activitySource)
                .count()
        )
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "ClipboardManager",
            "startActivity(",
            "setIntent("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(
                "safe-word reset entry button used side effect: $forbiddenEntrySideEffect",
                entryRouteSource.contains(forbiddenEntrySideEffect)
            )
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(
            Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource)
        )
        assertTrue(
            normalizedModelSource.contains(
                "fun build( analysis: ScamAnalysis?, readiness: FamilyReadiness, " +
                    "incidentCount: Int = 0 ): FamilySafeWordResetCoach"
            )
        )
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)
        assertEquals(2, Regex("\\bincidentCount\\b").findAll(modelSource).count())
        assertFalse(Regex("analysis\\s*\\.\\s*[A-Za-z]").containsMatchIn(modelSource))

        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "analysis.redactedClue",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "SafetyPlanStore",
            "SafetyPlan(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "directory.entries",
            "dialNumber",
            "ScamAnalyzer",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "NotificationManager",
            "MediaStore",
            "FileProvider",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "SecureRandom",
            "KeyGenerator",
            "MessageDigest",
            "Cipher",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "safe-word reset model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(
                "safe-word reset model embedded official number: $exactOfficialNumber",
                Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource)
            )
        }

        val builderCallPattern = Regex(
            "FamilySafeWordResetCoachBuilder\\.build\\(\\s*" +
                "analysis\\s*=\\s*currentAnalysis\\s*,\\s*" +
                "readiness\\s*=\\s*FamilyReadinessBuilder\\.build\\(" +
                "safetyPlan\\.load\\(\\)\\)\\s*,\\s*" +
                "incidentCount\\s*=\\s*incidents\\.count\\(\\)\\s*\\)"
        )
        assertEquals(1, builderCallPattern.findAll(coachScreen).count())
        assertEquals(
            1,
            Regex("FamilySafeWordResetCoachBuilder\\.build\\(")
                .findAll(activitySource)
                .count()
        )
        assertFalse(coachScreen.contains("incidents.load("))
        assertFalse(coachScreen.contains("val plan ="))

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
                "safe-word reset incident count decoded content: $forbiddenIncidentRead",
                incidentCountHelper.contains(forbiddenIncidentRead)
            )
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareFamilySafeWordResetCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue(
                "safe-word reset UI omitted $renderedField",
                coachScreen.contains(renderedField)
            )
        }
        assertTrue(
            coachScreen.contains("primaryButton(\"가족에게 값 없는 재정비 안내 공유\")")
        )
        assertTrue(coachScreen.contains("secondaryButton(\"가족 안전 계획 직접 편집\")"))
        assertTrue(coachScreen.contains("secondaryButton(\"홈으로\")"))
        assertEquals(1, Regex("coach\\.cards\\.forEach").findAll(coachScreen).count())
        assertTrue(coachScreen.contains("recommended = card.id in recommendedIds"))
        listOf(
            "card.displayOrder",
            "card.title",
            "card.resetScenario",
            "card.valueFreeFamilySentence",
            "card.whyItMatters",
            "card.doNotSayRecordOrShareBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedCardField ->
            assertTrue(
                "safe-word reset card UI omitted $renderedCardField",
                cardRenderer.contains(renderedCardField)
            )
        }

        val statelessUiSource = coachScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "EditText(",
            "text.toString(",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "ScamAnalyzer.analyze(",
            "pendingSharedText",
            "CheckBox(",
            "RadioButton(",
            "Switch(",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "MediaStore",
            "FileProvider",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationListenerService",
            "CallScreeningService",
            "resetStatus",
            "rotationStatus",
            "oldSecretStatus",
            "newSecretStatus",
            "resetProgress",
            "recipientName",
            "sentStatus",
            "cardProgress",
            "checkmark"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "safe-word reset UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))

        val expectedActionRoutes = linkedMapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        val actionIdBlock = functionBlock(
            modelSource,
            "enum class FamilySafeWordResetActionId"
        )
        val declaredActionIds = Regex("(?m)^\\s*([A-Z][A-Z_]+),?\\s*$")
            .findAll(actionIdBlock)
            .map { match -> match.groupValues[1] }
            .toSet()
        assertEquals(expectedActionRoutes.keys, declaredActionIds)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "safe-word reset route omitted branch for $actionId: $destination",
                normalizedActionRouter.contains(
                    "FamilySafeWordResetActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            expectedActionRoutes.size,
            Regex("FamilySafeWordResetActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        listOf(
            "dial(",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.ACTION_SEND",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "NotificationManager",
            "NotificationListenerService",
            "CallScreeningService",
            "TelecomManager",
            "TelephonyManager",
            "PhoneStateListener",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PackageManager",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "putString(",
            "putBoolean(",
            ".save("
        ).forEach { forbiddenRouteApi ->
            assertFalse(
                "safe-word reset router used forbidden API: $forbiddenRouteApi",
                actionRouter.contains(forbiddenRouteApi)
            )
        }

        val shareArguments = Regex("shareFamilySafeWordResetCoach\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertEquals(1, Regex("Intent\\.ACTION_SEND").findAll(shareHelper).count())
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "ScamAnalysis",
            "currentAnalysis",
            "analysis.input",
            "IncidentPreviewRedactor",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "NotificationManager",
            "PackageManager",
            "prefs.edit(",
            "safetyPlan.save(",
            "putString(",
            "putBoolean(",
            ".save(",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "resetStatus",
            "oldSecretStatus",
            "newSecretStatus",
            "recipientName",
            "sentStatus"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "safe-word reset share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource, ignoreCase = true)
            )
        }

        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:safeWordReset|oldSecret|newSecret|resetStatus|" +
                    "rotationStatus|resetProgress|resetRecipient)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "KEY_\\w*(?:SAFE_WORD_RESET|OLD_SECRET|NEW_SECRET|RESET_STATUS|" +
                    "ROTATION_STATUS|RESET_PROGRESS|RESET_RECIPIENT)\\w*"
            ).containsMatchIn(activitySource)
        )
        val persistenceSources = incidentStoreSource + safetyPlanStoreSource
        listOf(
            "FamilySafeWordReset",
            "family_safe_word_reset",
            "safe_word_reset",
            "reset_status",
            "rotation_status",
            "old_secret_status",
            "new_secret_status",
            "reset_progress",
            "reset_recipient"
        ).forEach { forbiddenStoredState ->
            assertFalse(
                "safe-word reset state was added to persistence: $forbiddenStoredState",
                persistenceSources.contains(forbiddenStoredState, ignoreCase = true)
            )
        }

        val sliceSource = coachScreen + cardRenderer + actionRouter + shareHelper + modelSource
        val forbiddenSliceApis = listOf(
            "AlarmManager",
            "NotificationManager",
            "NotificationCompat",
            "WorkManager",
            "CoroutineWorker",
            "JobScheduler",
            "CalendarContract",
            "ForegroundService",
            "BroadcastReceiver",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "sendTextMessage",
            "Intent.EXTRA_STREAM",
            "ACTION_SEND_MULTIPLE",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "android.permission.CAMERA",
            "android.hardware.camera",
            "androidx.camera",
            "CameraManager",
            "ImageCapture",
            "PhotoPicker",
            "PickVisualMedia",
            "DocumentsContract",
            "ContentResolver",
            "openInputStream",
            "java.io.File",
            "com.google.mlkit",
            "MlKit",
            "TextRecognition",
            "TextRecognizer",
            "InputImage",
            "ImageAnalysis",
            "BitmapFactory",
            "ExifInterface",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "NotificationListenerService",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "deletePackage",
            "Intent.ACTION_DELETE",
            "ACTION_UNINSTALL_PACKAGE",
            "ACTION_INSTALL_PACKAGE",
            "REQUEST_INSTALL_PACKAGES",
            "ACTION_MANAGE_UNKNOWN_APP_SOURCES",
            "ACTION_APPLICATION_DETAILS_SETTINGS",
            "Settings.",
            "ANDROID_ID",
            "Build.SERIAL",
            "AdvertisingIdClient",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "queryIntentActivities",
            "getPackageInfo",
            "getApplicationInfo",
            "canRequestPackageInstalls",
            "installPackage",
            "setApplicationEnabledSetting",
            "clearApplicationUserData",
            "AppOpsManager",
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "java.net.Socket",
            "OkHttp",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Geocoder",
            "Patterns.WEB_URL",
            "Patterns.PHONE",
            "PhoneNumberUtils",
            "URLUtil",
            "InetAddress",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "SecureRandom",
            "KeyGenerator",
            "MessageDigest",
            "Cipher",
            "PasswordGenerator",
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save(",
            "resetStatus",
            "rotationStatus",
            "oldSecretStatus",
            "newSecretStatus",
            "resetProgress",
            "recipientName",
            "sentStatus",
            "cardProgress",
            "checkmark"
        )
        forbiddenSliceApis.forEach { forbiddenSliceApi ->
            assertFalse(
                "safe-word reset slice used forbidden API/source: $forbiddenSliceApi",
                sliceSource.contains(forbiddenSliceApi, ignoreCase = true)
            )
        }

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "CAMERA",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(
                "manifest declared forbidden permission token: $forbiddenPermission",
                manifest.contains(forbiddenPermission)
            )
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun voiceImpersonationRealityCheckCoachIsPureStatelessRoutesOnlyAndSharesBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/VoiceImpersonationRealityCheckCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val safetyPlanStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.voiceImpersonationRealityCheckCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.voiceImpersonationRealityCheckCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openVoiceImpersonationRealityCheckAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareVoiceImpersonationRealityCheckCoach"
        )
        val normalizedActivitySource = activitySource.replace(Regex("\\s+"), " ")
        val normalizedModelSource = modelSource.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        listOf(
            "private fun LinearLayout.voiceImpersonationRealityCheckCoachScreen",
            "private fun LinearLayout.voiceImpersonationRealityCheckCard",
            "private fun openVoiceImpersonationRealityCheckAction",
            "private fun shareVoiceImpersonationRealityCheckCoach"
        ).forEach { marker ->
            assertTrue(
                "voice reality-check source marker missing: $marker",
                activitySource.contains(marker)
            )
        }
        assertTrue(activitySource.contains("VOICE_IMPERSONATION_REALITY_CHECK"))
        assertTrue(
            normalizedActivitySource.contains(
                "Screen.VOICE_IMPERSONATION_REALITY_CHECK -> " +
                    "voiceImpersonationRealityCheckCoachScreen()"
            )
        )

        val entryMarkers = listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.readinessSection()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.safeVerificationCallScriptsScreen",
            "private fun LinearLayout.familyCallbackCardScreen",
            "private fun LinearLayout.familySafeWordResetCoachScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.privacyCenterScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.evidenceHandoffPrivacyCoachScreen"
        )
        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.VOICE_IMPERSONATION_REALITY_CHECK\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.map { marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "voice reality-check entry must have one show-only button in $marker",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(21, entryRouteSources.size)
        assertEquals(
            34,
            Regex("show\\(Screen\\.VOICE_IMPERSONATION_REALITY_CHECK\\)")
                .findAll(activitySource)
                .count()
        )
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "ClipboardManager",
            "startActivity(",
            "setIntent("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(
                "voice reality-check entry button used side effect: $forbiddenEntrySideEffect",
                entryRouteSource.contains(forbiddenEntrySideEffect)
            )
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(
            Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource)
        )
        assertTrue(
            normalizedModelSource.contains(
                "fun build( analysis: ScamAnalysis?, readiness: FamilyReadiness, " +
                    "incidentCount: Int = 0 ): VoiceImpersonationRealityCheckCoach"
            )
        )
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)
        assertEquals(2, Regex("\\bincidentCount\\b").findAll(modelSource).count())
        assertFalse(Regex("analysis\\s*\\.\\s*[A-Za-z]").containsMatchIn(modelSource))

        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "analysis.redactedClue",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "SafetyPlanStore",
            "SafetyPlan",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "directory.entries",
            "dialNumber",
            "MediaRecorder",
            "AudioRecord",
            "AudioTrack",
            "SpeechRecognizer",
            "RecognizerIntent",
            "TextToSpeech",
            "AudioManager",
            "MediaPlayer",
            "SoundPool",
            "MicrophoneInfo",
            "android.media",
            "android.speech",
            "RECORD_AUDIO",
            "ScamAnalyzer",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "NotificationManager",
            "NotificationListenerService",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "TelephonyCallback",
            "MediaStore",
            "FileProvider",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "voice reality-check model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(
                "voice reality-check model embedded official number: $exactOfficialNumber",
                Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource)
            )
        }

        val builderCallPattern = Regex(
            "VoiceImpersonationRealityCheckCoachBuilder\\.build\\(\\s*" +
                "analysis\\s*=\\s*currentAnalysis\\s*,\\s*" +
                "readiness\\s*=\\s*FamilyReadinessBuilder\\.build\\(" +
                "safetyPlan\\.load\\(\\)\\)\\s*,\\s*" +
                "incidentCount\\s*=\\s*incidents\\.count\\(\\)\\s*\\)"
        )
        assertEquals(1, builderCallPattern.findAll(coachScreen).count())
        assertEquals(
            1,
            Regex("VoiceImpersonationRealityCheckCoachBuilder\\.build\\(")
                .findAll(activitySource)
                .count()
        )
        assertEquals(1, Regex("safetyPlan\\.load\\(\\)").findAll(coachScreen).count())
        assertEquals(1, Regex("incidents\\.count\\(\\)").findAll(coachScreen).count())
        assertFalse(coachScreen.contains("incidents.load("))
        assertFalse(coachScreen.contains("val plan ="))
        assertFalse(
            coachScreen
                .replace("FamilyReadinessBuilder.build(safetyPlan.load())", "")
                .contains("safetyPlan.")
        )
        assertFalse(coachScreen.replace("incidents.count()", "").contains("incidents."))

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
                "voice reality-check incident count decoded content: $forbiddenIncidentRead",
                incidentCountHelper.contains(forbiddenIncidentRead)
            )
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareVoiceImpersonationRealityCheckCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue(
                "voice reality-check UI omitted $renderedField",
                coachScreen.contains(renderedField)
            )
        }
        assertTrue(coachScreen.contains("secondaryButton(\"홈으로\")"))
        assertEquals(1, Regex("coach\\.cards\\.forEach").findAll(coachScreen).count())
        assertTrue(coachScreen.contains("recommended = card.id in recommendedIds"))
        listOf(
            "card.displayOrder",
            "card.title",
            "card.realityCheckScenario",
            "card.valueFreeActionSentence",
            "card.whyItMatters",
            "card.doNotRecordShareOrAskBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedCardField ->
            assertTrue(
                "voice reality-check card UI omitted $renderedCardField",
                cardRenderer.contains(renderedCardField)
            )
        }

        val statelessUiSource = coachScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "EditText(",
            "text.toString(",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "ScamAnalyzer.analyze(",
            "pendingSharedText",
            "CheckBox(",
            "RadioButton(",
            "Switch(",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "RecognizerIntent",
            "TextToSpeech",
            "MediaPlayer",
            "MediaStore",
            "FileProvider",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationListenerService",
            "CallScreeningService",
            "voiceStatus",
            "callerStatus",
            "recordingStatus",
            "audioName",
            "transcriptValue",
            "recipientName",
            "sentStatus",
            "cardProgress",
            "checkmark"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "voice reality-check UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))

        val expectedActionRoutes = linkedMapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "FAMILY_SAFE_WORD_RESET_COACH" to
                "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        val actionIdBlock = functionBlock(
            modelSource,
            "enum class VoiceImpersonationRealityCheckActionId"
        )
        val declaredActionIds = Regex("(?m)^\\s*([A-Z][A-Z_]+),?\\s*$")
            .findAll(actionIdBlock)
            .map { match -> match.groupValues[1] }
            .toSet()
        assertEquals(expectedActionRoutes.keys, declaredActionIds)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "voice reality-check route omitted branch for $actionId: $destination",
                normalizedActionRouter.contains(
                    "VoiceImpersonationRealityCheckActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            expectedActionRoutes.size,
            Regex("VoiceImpersonationRealityCheckActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        listOf(
            "dial(",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.ACTION_SEND",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "RecognizerIntent",
            "TextToSpeech",
            "AudioManager",
            "MediaPlayer",
            "SoundPool",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "NotificationManager",
            "NotificationListenerService",
            "CallScreeningService",
            "TelecomManager",
            "TelephonyManager",
            "PhoneStateListener",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PackageManager",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "putString(",
            "putBoolean(",
            ".save("
        ).forEach { forbiddenRouteApi ->
            assertFalse(
                "voice reality-check router used forbidden API: $forbiddenRouteApi",
                actionRouter.contains(forbiddenRouteApi)
            )
        }

        val shareArguments = Regex(
            "shareVoiceImpersonationRealityCheckCoach\\(([^)]*)\\)"
        )
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertEquals(1, Regex("Intent\\.ACTION_SEND").findAll(shareHelper).count())
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "RecognizerIntent",
            "MediaPlayer",
            "ScamAnalysis",
            "currentAnalysis",
            "analysis.input",
            "IncidentPreviewRedactor",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "NotificationManager",
            "PackageManager",
            "prefs.edit(",
            "safetyPlan.save(",
            "putString(",
            "putBoolean(",
            ".save(",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "voiceStatus",
            "callerStatus",
            "recordingStatus",
            "audioName",
            "transcriptValue",
            "recipientName",
            "sentStatus"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "voice reality-check share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource, ignoreCase = true)
            )
        }

        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:voiceImpersonation|voiceStatus|callerStatus|" +
                    "recordingStatus|voiceReal|voiceFake|audioName|transcript|recipient)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "KEY_\\w*(?:VOICE_IMPERSONATION|VOICE_STATUS|CALLER_STATUS|" +
                    "RECORDING_STATUS|VOICE_REAL|VOICE_FAKE|AUDIO_NAME|TRANSCRIPT|RECIPIENT)\\w*"
            ).containsMatchIn(activitySource)
        )
        val persistenceSources = incidentStoreSource + safetyPlanStoreSource
        listOf(
            "VoiceImpersonation",
            "voice_impersonation",
            "voice_status",
            "caller_status",
            "recording_status",
            "voice_real_status",
            "voice_fake_status",
            "voice_check_progress",
            "audio_name",
            "transcript_value",
            "voice_recipient",
            "voice_share_status"
        ).forEach { forbiddenStoredState ->
            assertFalse(
                "voice reality-check state was added to persistence: $forbiddenStoredState",
                persistenceSources.contains(forbiddenStoredState, ignoreCase = true)
            )
        }

        val nonShareSliceSource = coachScreen + cardRenderer + actionRouter + modelSource
        val fullSliceSource = nonShareSliceSource + shareHelper
        val forbiddenSliceApis = listOf(
            "MediaRecorder",
            "AudioRecord",
            "AudioTrack",
            "SpeechRecognizer",
            "RecognizerIntent",
            "TextToSpeech",
            "AudioManager",
            "MediaPlayer",
            "SoundPool",
            "MicrophoneInfo",
            "AudioDeviceInfo",
            "android.media",
            "android.speech",
            "RECORD_AUDIO",
            "AlarmManager",
            "NotificationManager",
            "NotificationCompat",
            "WorkManager",
            "CoroutineWorker",
            "JobScheduler",
            "CalendarContract",
            "ForegroundService",
            "BroadcastReceiver",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "sendTextMessage",
            "Intent.EXTRA_STREAM",
            "ACTION_SEND_MULTIPLE",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "android.permission.CAMERA",
            "android.hardware.camera",
            "androidx.camera",
            "CameraManager",
            "ImageCapture",
            "PhotoPicker",
            "PickVisualMedia",
            "DocumentsContract",
            "ContentResolver",
            "openInputStream",
            "java.io.File",
            "com.google.mlkit",
            "MlKit",
            "TextRecognition",
            "TextRecognizer",
            "InputImage",
            "ImageAnalysis",
            "BitmapFactory",
            "ExifInterface",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "TelephonyCallback",
            "NotificationListenerService",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "deletePackage",
            "Intent.ACTION_DELETE",
            "ACTION_UNINSTALL_PACKAGE",
            "ACTION_INSTALL_PACKAGE",
            "REQUEST_INSTALL_PACKAGES",
            "ACTION_MANAGE_UNKNOWN_APP_SOURCES",
            "ACTION_APPLICATION_DETAILS_SETTINGS",
            "Settings.",
            "ANDROID_ID",
            "Build.SERIAL",
            "AdvertisingIdClient",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "queryIntentActivities",
            "getPackageInfo",
            "getApplicationInfo",
            "canRequestPackageInstalls",
            "installPackage",
            "setApplicationEnabledSetting",
            "clearApplicationUserData",
            "AppOpsManager",
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "java.net.Socket",
            "OkHttp",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Geocoder",
            "Patterns.WEB_URL",
            "Patterns.PHONE",
            "PhoneNumberUtils",
            "URLUtil",
            "InetAddress",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            "plan.safeWord",
            "plan.callbackScript",
            "plan.contacts",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save(",
            "voiceStatus",
            "callerStatus",
            "recordingStatus",
            "audioName",
            "transcriptValue",
            "recipientName",
            "sentStatus",
            "cardProgress",
            "checkmark"
        )
        forbiddenSliceApis.forEach { forbiddenSliceApi ->
            assertFalse(
                "voice reality-check slice used forbidden API/source: $forbiddenSliceApi",
                fullSliceSource.contains(forbiddenSliceApi, ignoreCase = true)
            )
        }
        assertFalse(nonShareSliceSource.contains("Intent.ACTION_SEND"))

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "SEND_SMS",
            "RECEIVE_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "CAMERA",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(
                "manifest declared forbidden permission token: $forbiddenPermission",
                manifest.contains(forbiddenPermission)
            )
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun newNumberQuarantineCoachIsPureStatelessRoutesOnlyAndSharesBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/NewNumberQuarantineCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val safetyPlanStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.newNumberQuarantineCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.newNumberQuarantineCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openNewNumberQuarantineAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareNewNumberQuarantineCoach"
        )
        val normalizedActivitySource = activitySource.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        listOf(
            "private fun LinearLayout.newNumberQuarantineCoachScreen",
            "private fun LinearLayout.newNumberQuarantineCard",
            "private fun openNewNumberQuarantineAction",
            "private fun shareNewNumberQuarantineCoach"
        ).forEach { marker ->
            assertTrue(
                "new-number quarantine source marker missing: $marker",
                activitySource.contains(marker)
            )
        }
        assertTrue(activitySource.contains("NEW_NUMBER_QUARANTINE_COACH"))
        assertTrue(
            normalizedActivitySource.contains(
                "Screen.NEW_NUMBER_QUARANTINE_COACH -> newNumberQuarantineCoachScreen()"
            )
        )

        val entryMarkers = listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.readinessSection()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.safeVerificationCallScriptsScreen",
            "private fun LinearLayout.familyCallbackCardScreen",
            "private fun LinearLayout.voiceImpersonationRealityCheckCoachScreen",
            "private fun LinearLayout.familySafeWordResetCoachScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.privacyCenterScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.evidenceHandoffPrivacyCoachScreen"
        )
        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.NEW_NUMBER_QUARANTINE_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.map { marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "new-number quarantine entry must have one show-only button in $marker",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(23, entryRouteSources.size)
        assertEquals(
            35,
            Regex("show\\(Screen\\.NEW_NUMBER_QUARANTINE_COACH\\)")
                .findAll(activitySource)
                .count()
        )
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "ClipboardManager",
            "startActivity(",
            "setIntent("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(
                "new-number quarantine entry used side effect: $forbiddenEntrySideEffect",
                entryRouteSource.contains(forbiddenEntrySideEffect)
            )
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(
            Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource)
        )
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)
        assertEquals(2, Regex("\\bincidentCount\\b").findAll(modelSource).count())
        assertFalse(Regex("analysis\\s*\\.\\s*[A-Za-z]").containsMatchIn(modelSource))

        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "analysis.redactedClue",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "SafetyPlanStore",
            "SafetyPlan(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "directory.entries",
            "ContactsContract",
            "PhoneNumberUtils",
            "Patterns.PHONE",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationListenerService",
            "ClipboardManager",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "RecognizerIntent",
            "TextToSpeech",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "new-number quarantine model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(
                "new-number quarantine model embedded official number: $exactOfficialNumber",
                Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource)
            )
        }

        val builderCallPattern = Regex(
            "NewNumberQuarantineCoachBuilder\\.build\\(\\s*" +
                "analysis\\s*=\\s*currentAnalysis\\s*,\\s*" +
                "readiness\\s*=\\s*FamilyReadinessBuilder\\.build\\(" +
                "safetyPlan\\.load\\(\\)\\)\\s*,\\s*" +
                "incidentCount\\s*=\\s*incidents\\.count\\(\\)\\s*\\)"
        )
        assertEquals(1, builderCallPattern.findAll(coachScreen).count())
        assertEquals(
            1,
            Regex("NewNumberQuarantineCoachBuilder\\.build\\(")
                .findAll(activitySource)
                .count()
        )
        assertEquals(1, Regex("safetyPlan\\.load\\(\\)").findAll(coachScreen).count())
        assertEquals(1, Regex("incidents\\.count\\(\\)").findAll(coachScreen).count())
        assertFalse(coachScreen.contains("incidents.load("))
        assertFalse(coachScreen.contains("val plan ="))
        assertFalse(
            coachScreen
                .replace("FamilyReadinessBuilder.build(safetyPlan.load())", "")
                .contains("safetyPlan.")
        )
        assertFalse(coachScreen.replace("incidents.count()", "").contains("incidents."))

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
                "new-number incident count decoded content: $forbiddenIncidentRead",
                incidentCountHelper.contains(forbiddenIncidentRead)
            )
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareNewNumberQuarantineCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue(
                "new-number quarantine UI omitted $renderedField",
                coachScreen.contains(renderedField)
            )
        }
        assertTrue(coachScreen.contains("secondaryButton(\"홈으로\")"))
        assertEquals(1, Regex("coach\\.cards\\.forEach").findAll(coachScreen).count())
        assertTrue(coachScreen.contains("recommended = card.id in recommendedIds"))
        listOf(
            "card.displayOrder",
            "card.title",
            "card.quarantineScenario",
            "card.valueFreeActionSentence",
            "card.whyItMatters",
            "card.doNotSaveContactOrValueBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedCardField ->
            assertTrue(
                "new-number quarantine card UI omitted $renderedCardField",
                cardRenderer.contains(renderedCardField)
            )
        }

        val statelessUiSource = coachScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "EditText(",
            "text.toString(",
            "ClipboardManager",
            "ScamAnalyzer.analyze(",
            "pendingSharedText",
            "CheckBox(",
            "RadioButton(",
            "Switch(",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PackageManager",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationListenerService",
            "CallScreeningService",
            "newNumberValue",
            "oldNumberValue",
            "profileValue",
            "channelValue",
            "contactStatus",
            "verificationStatus",
            "recipientName"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "new-number quarantine UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))

        val expectedActionRoutes = linkedMapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "FAMILY_SAFE_WORD_RESET_COACH" to
                "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "VOICE_IMPERSONATION_REALITY_CHECK_COACH" to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        val actionIdBlock = functionBlock(
            modelSource,
            "enum class NewNumberQuarantineActionId"
        )
        val declaredActionIds = Regex("(?m)^\\s*([A-Z][A-Z_]+),?\\s*$")
            .findAll(actionIdBlock)
            .map { match -> match.groupValues[1] }
            .toSet()
        assertEquals(expectedActionRoutes.keys, declaredActionIds)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "new-number quarantine route omitted $actionId: $destination",
                normalizedActionRouter.contains(
                    "NewNumberQuarantineActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            expectedActionRoutes.size,
            Regex("NewNumberQuarantineActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        listOf(
            "dial(",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.ACTION_SEND",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "NotificationManager",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "PackageManager",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "putString(",
            "putBoolean(",
            ".save("
        ).forEach { forbiddenRouteApi ->
            assertFalse(
                "new-number quarantine router used forbidden API: $forbiddenRouteApi",
                actionRouter.contains(forbiddenRouteApi)
            )
        }

        val shareArguments = Regex("shareNewNumberQuarantineCoach\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertEquals(1, Regex("Intent\\.ACTION_SEND").findAll(shareHelper).count())
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "ScamAnalysis",
            "currentAnalysis",
            "analysis.input",
            "IncidentPreviewRedactor",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "NotificationManager",
            "PackageManager",
            "prefs.edit(",
            "safetyPlan.save(",
            "putString(",
            "putBoolean(",
            ".save(",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "newNumberValue",
            "oldNumberValue",
            "contactStatus",
            "verificationStatus",
            "recipientName"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "new-number quarantine share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource, ignoreCase = true)
            )
        }

        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:newNumber|oldNumber|profileValue|channelValue|" +
                    "contactStatus|quarantineStatus|verificationStatus|recipient)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "KEY_\\w*(?:NEW_NUMBER|OLD_NUMBER|PROFILE_VALUE|CHANNEL_VALUE|" +
                    "CONTACT_STATUS|QUARANTINE_STATUS|VERIFICATION_STATUS|RECIPIENT)\\w*"
            ).containsMatchIn(activitySource)
        )
        val persistenceSources = incidentStoreSource + safetyPlanStoreSource
        listOf(
            "NewNumberQuarantine",
            "new_number_quarantine",
            "new_number_value",
            "old_number_value",
            "profile_value",
            "channel_value",
            "contact_status",
            "quarantine_status",
            "verification_status",
            "quarantine_progress",
            "quarantine_recipient"
        ).forEach { forbiddenStoredState ->
            assertFalse(
                "new-number quarantine state was persisted: $forbiddenStoredState",
                persistenceSources.contains(forbiddenStoredState, ignoreCase = true)
            )
        }

        val nonShareSliceSource = coachScreen + cardRenderer + actionRouter + modelSource
        val fullSliceSource = nonShareSliceSource + shareHelper
        val forbiddenSliceApis = listOf(
            "ContactsContract",
            "Intent.ACTION_INSERT",
            "Intent.ACTION_EDIT",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_SENDTO",
            "smsto:",
            "tel:",
            "PhoneNumberUtils",
            "Patterns.PHONE",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationCompat",
            "NotificationListenerService",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "RecognizerIntent",
            "TextToSpeech",
            "android.media",
            "android.speech",
            "Intent.EXTRA_STREAM",
            "ACTION_SEND_MULTIPLE",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "DocumentsContract",
            "ContentResolver",
            "openInputStream",
            "java.io.File",
            "MlKit",
            "TextRecognition",
            "ImageAnalysis",
            "BitmapFactory",
            "ExifInterface",
            "AlarmManager",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "ForegroundService",
            "BroadcastReceiver",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "Settings.",
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save(",
            "newNumberValue",
            "oldNumberValue",
            "profileValue",
            "channelValue",
            "contactStatus",
            "verificationStatus",
            "recipientName"
        )
        forbiddenSliceApis.forEach { forbiddenSliceApi ->
            assertFalse(
                "new-number quarantine slice used forbidden API/source: $forbiddenSliceApi",
                fullSliceSource.contains(forbiddenSliceApi, ignoreCase = true)
            )
        }
        assertFalse(nonShareSliceSource.contains("Intent.ACTION_SEND"))

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "SEND_SMS",
            "RECEIVE_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "CAMERA",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(
                "manifest declared forbidden permission token: $forbiddenPermission",
                manifest.contains(forbiddenPermission)
            )
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun credentialSecretFreezeCoachIsPureStatelessRoutesOnlyAndSharesBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/CredentialSecretFreezeCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val safetyPlanStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.credentialSecretFreezeCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.credentialSecretFreezeCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openCredentialSecretFreezeAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareCredentialSecretFreezeCoach"
        )
        val normalizedActivitySource = activitySource.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        listOf(
            "private fun LinearLayout.credentialSecretFreezeCoachScreen",
            "private fun LinearLayout.credentialSecretFreezeCard",
            "private fun openCredentialSecretFreezeAction",
            "private fun shareCredentialSecretFreezeCoach"
        ).forEach { marker ->
            assertTrue(
                "credential-secret-freeze source marker missing: $marker",
                activitySource.contains(marker)
            )
        }
        assertTrue(activitySource.contains("CREDENTIAL_SECRET_FREEZE_COACH"))
        assertTrue(
            normalizedActivitySource.contains(
                "Screen.CREDENTIAL_SECRET_FREEZE_COACH -> credentialSecretFreezeCoachScreen()"
            )
        )

        val entryMarkers = listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.readinessSection()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.safePastePrivacyCoachScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.exposureImpactTriageScreen",
            "private fun LinearLayout.evidenceHandoffPrivacyCoachScreen",
            "private fun LinearLayout.newNumberQuarantineCoachScreen",
            "private fun LinearLayout.voiceImpersonationRealityCheckCoachScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.privacyCenterScreen"
        )
        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.CREDENTIAL_SECRET_FREEZE_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.map { marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "credential-secret-freeze entry must have one show-only button in $marker",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(23, entryRouteSources.size)
        assertEquals(
            37,
            Regex("show\\(Screen\\.CREDENTIAL_SECRET_FREEZE_COACH\\)")
                .findAll(activitySource)
                .count()
        )
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "BiometricPrompt",
            "startActivity(",
            "setIntent("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(
                "credential-secret-freeze entry used side effect: $forbiddenEntrySideEffect",
                entryRouteSource.contains(forbiddenEntrySideEffect)
            )
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(
            Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource)
        )
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)
        assertEquals(2, Regex("\\bincidentCount\\b").findAll(modelSource).count())
        assertFalse(Regex("analysis\\s*\\.\\s*[A-Za-z]").containsMatchIn(modelSource))

        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "analysis.redactedClue",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "SafetyPlanStore",
            "SafetyPlan(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "directory.entries",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "AutofillManager",
            "AutofillValue",
            "android.view.autofill",
            "CredentialManager",
            "android.credentials",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "MessageDigest",
            "ContactsContract",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationListenerService",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "FileProvider",
            "MediaStore",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "credential-secret-freeze model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(
                Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource)
            )
        }

        val builderCallPattern = Regex(
            "CredentialSecretFreezeCoachBuilder\\.build\\(\\s*" +
                "analysis\\s*=\\s*currentAnalysis\\s*,\\s*" +
                "readiness\\s*=\\s*FamilyReadinessBuilder\\.build\\(" +
                "safetyPlan\\.load\\(\\)\\)\\s*,\\s*" +
                "incidentCount\\s*=\\s*incidents\\.count\\(\\)\\s*\\)"
        )
        assertEquals(1, builderCallPattern.findAll(coachScreen).count())
        assertEquals(
            1,
            Regex("CredentialSecretFreezeCoachBuilder\\.build\\(")
                .findAll(activitySource)
                .count()
        )
        assertEquals(1, Regex("safetyPlan\\.load\\(\\)").findAll(coachScreen).count())
        assertEquals(1, Regex("incidents\\.count\\(\\)").findAll(coachScreen).count())
        assertFalse(coachScreen.contains("incidents.load("))
        assertFalse(coachScreen.contains("val plan ="))
        assertFalse(
            coachScreen
                .replace("FamilyReadinessBuilder.build(safetyPlan.load())", "")
                .contains("safetyPlan.")
        )
        assertFalse(coachScreen.replace("incidents.count()", "").contains("incidents."))

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
                "credential-secret incident count decoded content: $forbiddenIncidentRead",
                incidentCountHelper.contains(forbiddenIncidentRead)
            )
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareCredentialSecretFreezeCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue(
                "credential-secret-freeze UI omitted $renderedField",
                coachScreen.contains(renderedField)
            )
        }
        assertTrue(coachScreen.contains("secondaryButton(\"홈으로\")"))
        assertEquals(1, Regex("coach\\.cards\\.forEach").findAll(coachScreen).count())
        assertTrue(coachScreen.contains("recommended = card.id in recommendedIds"))
        listOf(
            "card.displayOrder",
            "card.title",
            "card.freezeScenario",
            "card.valueFreeActionSentence",
            "card.whyItMatters",
            "card.doNotEnterShareClipboardOrAutofillBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedCardField ->
            assertTrue(
                "credential-secret-freeze card UI omitted $renderedCardField",
                cardRenderer.contains(renderedCardField)
            )
        }

        val statelessUiSource = coachScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "EditText(",
            "text.toString(",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "AutofillManager",
            "AutofillValue",
            "CredentialManager",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "ScamAnalyzer.analyze(",
            "pendingSharedText",
            "CheckBox(",
            "RadioButton(",
            "Switch(",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PackageManager",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationListenerService",
            "credentialValue",
            "otpValue",
            "pinValue",
            "passwordValue",
            "securityAnswerValue",
            "accountValue",
            "cardValue",
            "clipboardValue",
            "autofillState",
            "loginState",
            "authenticationState",
            "recipientName",
            "verificationStatus",
            "recoveryStatus"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "credential-secret-freeze UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))

        val expectedActionRoutes = linkedMapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "SAFE_PASTE_PRIVACY_COACH" to "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "FAMILY_SAFE_WORD_RESET_COACH" to
                "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "VOICE_IMPERSONATION_REALITY_CHECK_COACH" to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            "NEW_NUMBER_QUARANTINE_COACH" to "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        val actionIdBlock = functionBlock(
            modelSource,
            "enum class CredentialSecretFreezeActionId"
        )
        val declaredActionIds = Regex("(?m)^\\s*([A-Z][A-Z_]+),?\\s*$")
            .findAll(actionIdBlock)
            .map { match -> match.groupValues[1] }
            .toSet()
        assertEquals(expectedActionRoutes.keys, declaredActionIds)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "credential-secret-freeze route omitted $actionId: $destination",
                normalizedActionRouter.contains(
                    "CredentialSecretFreezeActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            expectedActionRoutes.size,
            Regex("CredentialSecretFreezeActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        listOf(
            "dial(",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.ACTION_SEND",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "BiometricPrompt",
            "KeyStore",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "PackageManager",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "putString(",
            "putBoolean(",
            ".save("
        ).forEach { forbiddenRouteApi ->
            assertFalse(
                "credential-secret-freeze router used forbidden API: $forbiddenRouteApi",
                actionRouter.contains(forbiddenRouteApi)
            )
        }

        val shareArguments = Regex("shareCredentialSecretFreezeCoach\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertEquals(1, Regex("Intent\\.ACTION_SEND").findAll(shareHelper).count())
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "ScamAnalysis",
            "currentAnalysis",
            "analysis.input",
            "IncidentPreviewRedactor",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "BiometricPrompt",
            "NotificationManager",
            "PackageManager",
            "prefs.edit(",
            "safetyPlan.save(",
            "putString(",
            "putBoolean(",
            ".save(",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "credentialValue",
            "otpValue",
            "pinValue",
            "passwordValue",
            "accountValue",
            "cardValue",
            "recipientName",
            "verificationStatus",
            "recoveryStatus"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "credential-secret-freeze share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource, ignoreCase = true)
            )
        }

        assertFalse(
            Regex(
                "private\\s+var\\s+\\w*(?:credential|otp|pin|password|securityAnswer|" +
                    "accountValue|cardValue|clipboardValue|autofillState|loginState|recipient)\\w*",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(activitySource)
        )
        assertFalse(
            Regex(
                "KEY_\\w*(?:CREDENTIAL|OTP|PIN|PASSWORD|SECURITY_ANSWER|ACCOUNT_VALUE|" +
                    "CARD_VALUE|CLIPBOARD_VALUE|AUTOFILL_STATE|LOGIN_STATE|RECIPIENT)\\w*"
            ).containsMatchIn(activitySource)
        )
        val persistenceSources = incidentStoreSource + safetyPlanStoreSource
        listOf(
            "CredentialSecretFreeze",
            "credential_secret_freeze",
            "credential_value",
            "otp_value",
            "pin_value",
            "password_value",
            "security_answer_value",
            "account_value",
            "card_value",
            "clipboard_value",
            "autofill_state",
            "login_state",
            "authentication_state",
            "credential_status",
            "recovery_status",
            "freeze_progress",
            "freeze_recipient"
        ).forEach { forbiddenStoredState ->
            assertFalse(
                "credential-secret-freeze state was persisted: $forbiddenStoredState",
                persistenceSources.contains(forbiddenStoredState, ignoreCase = true)
            )
        }

        val nonShareSliceSource = coachScreen + cardRenderer + actionRouter + modelSource
        val fullSliceSource = nonShareSliceSource + shareHelper
        val forbiddenSliceApis = listOf(
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "AutofillManager",
            "AutofillValue",
            "android.view.autofill",
            "CredentialManager",
            "android.credentials",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "MessageDigest",
            "ContactsContract",
            "Intent.ACTION_INSERT",
            "Intent.ACTION_EDIT",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_SENDTO",
            "smsto:",
            "tel:",
            "PhoneNumberUtils",
            "Patterns.PHONE",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationCompat",
            "NotificationListenerService",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "RecognizerIntent",
            "TextToSpeech",
            "android.media",
            "android.speech",
            "Intent.EXTRA_STREAM",
            "ACTION_SEND_MULTIPLE",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "DocumentsContract",
            "ContentResolver",
            "openInputStream",
            "java.io.File",
            "MlKit",
            "TextRecognition",
            "ImageAnalysis",
            "BitmapFactory",
            "ExifInterface",
            "AlarmManager",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "ForegroundService",
            "BroadcastReceiver",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "Settings.",
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save(",
            "credentialValue",
            "otpValue",
            "pinValue",
            "passwordValue",
            "securityAnswerValue",
            "accountValue",
            "cardValue",
            "clipboardValue",
            "autofillState",
            "loginState",
            "authenticationState",
            "recipientName",
            "verificationStatus",
            "recoveryStatus"
        )
        forbiddenSliceApis.forEach { forbiddenSliceApi ->
            assertFalse(
                "credential-secret-freeze slice used forbidden API/source: $forbiddenSliceApi",
                fullSliceSource.contains(forbiddenSliceApi, ignoreCase = true)
            )
        }
        assertFalse(nonShareSliceSource.contains("Intent.ACTION_SEND"))

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "SEND_SMS",
            "RECEIVE_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "CAMERA",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(
                "manifest declared forbidden permission token: $forbiddenPermission",
                manifest.contains(forbiddenPermission)
            )
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun officialPressureRealityCheckCoachIsPureStatelessRoutesOnlyAndSharesBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/OfficialPressureRealityCheckCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val safetyPlanStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.officialPressureRealityCheckCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.officialPressureRealityCheckCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openOfficialPressureRealityCheckAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareOfficialPressureRealityCheckCoach"
        )
        val normalizedActivitySource = activitySource.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertTrue(activitySource.contains("OFFICIAL_PRESSURE_REALITY_CHECK_COACH"))
        assertTrue(
            normalizedActivitySource.contains(
                "Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH -> " +
                    "officialPressureRealityCheckCoachScreen()"
            )
        )

        val entryMarkers = listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.readinessSection()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.helpDirectoryScreen",
            "private fun LinearLayout.officialReportPrepScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.credentialSecretFreezeCoachScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.exposureImpactTriageScreen",
            "private fun LinearLayout.evidenceHandoffPrivacyCoachScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.safePastePrivacyCoachScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.newNumberQuarantineCoachScreen",
            "private fun LinearLayout.voiceImpersonationRealityCheckCoachScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.privacyCenterScreen"
        )
        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.OFFICIAL_PRESSURE_REALITY_CHECK_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.map { marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "official-pressure entry must have one show-only button in $marker",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(26, entryRouteSources.size)
        assertEquals(
            37,
            Regex("show\\(Screen\\.OFFICIAL_PRESSURE_REALITY_CHECK_COACH\\)")
                .findAll(activitySource)
                .count()
        )
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear()",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "startActivity(",
            "setIntent("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(
                "official-pressure entry used side effect: $forbiddenEntrySideEffect",
                entryRouteSource.contains(forbiddenEntrySideEffect)
            )
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(
            Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource)
        )
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)
        assertFalse(Regex("analysis\\s*\\.\\s*[A-Za-z]").containsMatchIn(modelSource))

        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "analysis.redactedClue",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "SafetyPlanStore",
            "SafetyPlan(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "directory.entries",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "AutofillManager",
            "CredentialManager",
            "android.credentials",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "ContactsContract",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationListenerService",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "FileProvider",
            "MediaStore",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Intent.ACTION_VIEW",
            "Geocoder",
            "Patterns.WEB_URL",
            "URLUtil",
            "InetAddress",
            "PhoneNumberUtils",
            "Patterns.PHONE",
            "BillingClient",
            "NfcAdapter",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "Settings.",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "official-pressure model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(
                Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource)
            )
        }

        val builderCallPattern = Regex(
            "OfficialPressureRealityCheckCoachBuilder\\.build\\(\\s*" +
                "analysis\\s*=\\s*currentAnalysis\\s*,\\s*" +
                "readiness\\s*=\\s*FamilyReadinessBuilder\\.build\\(" +
                "safetyPlan\\.load\\(\\)\\)\\s*,\\s*" +
                "incidentCount\\s*=\\s*incidents\\.count\\(\\)\\s*\\)"
        )
        assertEquals(1, builderCallPattern.findAll(coachScreen).count())
        assertEquals(
            1,
            Regex("OfficialPressureRealityCheckCoachBuilder\\.build\\(")
                .findAll(activitySource)
                .count()
        )
        assertEquals(1, Regex("safetyPlan\\.load\\(\\)").findAll(coachScreen).count())
        assertEquals(1, Regex("incidents\\.count\\(\\)").findAll(coachScreen).count())
        assertFalse(coachScreen.contains("incidents.load("))
        assertFalse(coachScreen.contains("val plan ="))
        assertFalse(
            coachScreen
                .replace("FamilyReadinessBuilder.build(safetyPlan.load())", "")
                .contains("safetyPlan.")
        )
        assertFalse(coachScreen.replace("incidents.count()", "").contains("incidents."))

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
                "official-pressure incident count decoded content: $forbiddenIncidentRead",
                incidentCountHelper.contains(forbiddenIncidentRead)
            )
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareOfficialPressureRealityCheckCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue(
                "official-pressure UI omitted $renderedField",
                coachScreen.contains(renderedField)
            )
        }
        assertTrue(coachScreen.contains("secondaryButton(\"홈으로\")"))
        assertEquals(1, Regex("coach\\.cards\\.forEach").findAll(coachScreen).count())
        assertTrue(coachScreen.contains("recommended = card.id in recommendedIds"))
        listOf(
            "card.displayOrder",
            "card.title",
            "card.pressureScenario",
            "card.valueFreeActionSentence",
            "card.whyItMatters",
            "card.doNotCallLookupShareOrSubmitBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedCardField ->
            assertTrue(
                "official-pressure card UI omitted $renderedCardField",
                cardRenderer.contains(renderedCardField)
            )
        }

        val statelessUiSource = coachScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "EditText(",
            "text.toString(",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "BiometricPrompt",
            "KeyStore",
            "ScamAnalyzer.analyze(",
            "pendingSharedText",
            "CheckBox(",
            "RadioButton(",
            "Switch(",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PackageManager",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "callerId",
            "callState",
            "clipboardText",
            "autofillState",
            "loginState",
            "recipientName",
            "verificationStatus",
            "reportStatus",
            "recoveryStatus"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "official-pressure UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }
        assertFalse(Regex("currentAnalysis\\s*=(?!=)").containsMatchIn(statelessUiSource))

        val expectedActionRoutes = linkedMapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "SAFE_PASTE_PRIVACY_COACH" to "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "FAMILY_SAFE_WORD_RESET_COACH" to
                "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "VOICE_IMPERSONATION_REALITY_CHECK_COACH" to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            "NEW_NUMBER_QUARANTINE_COACH" to "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            "CREDENTIAL_SECRET_FREEZE_COACH" to
                "show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        val actionIdBlock = functionBlock(
            modelSource,
            "enum class OfficialPressureRealityCheckActionId"
        )
        val declaredActionIds = Regex("(?m)^\\s*([A-Z][A-Z_]+),?\\s*$")
            .findAll(actionIdBlock)
            .map { match -> match.groupValues[1] }
            .toSet()
        assertEquals(expectedActionRoutes.keys, declaredActionIds)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "official-pressure route omitted $actionId: $destination",
                normalizedActionRouter.contains(
                    "OfficialPressureRealityCheckActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            expectedActionRoutes.size,
            Regex("OfficialPressureRealityCheckActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(actionRouter).count())
        assertFalse((coachScreen + cardRenderer + modelSource).contains("openHelpDirectory("))
        listOf(
            "dialNumber",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.ACTION_SEND",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "PackageManager",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "putString(",
            "putBoolean(",
            ".save("
        ).forEach { forbiddenRouteApi ->
            assertFalse(
                "official-pressure router used forbidden API: $forbiddenRouteApi",
                actionRouter.contains(forbiddenRouteApi)
            )
        }

        val shareArguments = Regex("shareOfficialPressureRealityCheckCoach\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertEquals(1, Regex("Intent\\.ACTION_SEND").findAll(shareHelper).count())
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "ScamAnalysis",
            "currentAnalysis",
            "analysis.input",
            "IncidentPreviewRedactor",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "NotificationManager",
            "PackageManager",
            "prefs.edit(",
            "safetyPlan.save(",
            "putString(",
            "putBoolean(",
            ".save(",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "callerId",
            "callState",
            "clipboardText",
            "autofillState",
            "loginState",
            "recipientName",
            "verificationStatus",
            "reportStatus",
            "recoveryStatus"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "official-pressure share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource, ignoreCase = true)
            )
        }

        val persistenceSources = incidentStoreSource + safetyPlanStoreSource
        listOf(
            "OfficialPressureRealityCheck",
            "official_pressure_reality_check",
            "official_pressure_status",
            "caller_id_status",
            "case_status",
            "report_status",
            "payment_status",
            "account_status",
            "device_status",
            "recovery_status"
        ).forEach { forbiddenStoredState ->
            assertFalse(
                "official-pressure state was persisted: $forbiddenStoredState",
                persistenceSources.contains(forbiddenStoredState, ignoreCase = true)
            )
        }

        val nonShareSliceSource = coachScreen + cardRenderer + actionRouter + modelSource
        val fullSliceSource = nonShareSliceSource + shareHelper
        listOf(
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "AutofillManager",
            "CredentialManager",
            "android.credentials",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "ContactsContract",
            "ACTION_SENDTO",
            "smsto:",
            "tel:",
            "PhoneNumberUtils",
            "Patterns.PHONE",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationCompat",
            "NotificationListenerService",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "RecognizerIntent",
            "TextToSpeech",
            "android.media",
            "android.speech",
            "Intent.EXTRA_STREAM",
            "ACTION_SEND_MULTIPLE",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "DocumentsContract",
            "ContentResolver",
            "openInputStream",
            "java.io.File",
            "MlKit",
            "TextRecognition",
            "ImageAnalysis",
            "BitmapFactory",
            "ExifInterface",
            "AlarmManager",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "ForegroundService",
            "BroadcastReceiver",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Geocoder",
            "Patterns.WEB_URL",
            "URLUtil",
            "InetAddress",
            "BillingClient",
            "NfcAdapter",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "Settings.",
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save(",
            "officialNumber",
            "directoryValue",
            "callerId",
            "callState",
            "urlValue",
            "domainValue",
            "caseId",
            "accountValue",
            "cardValue",
            "otpValue",
            "pinValue",
            "passwordValue",
            "securityAnswerValue",
            "clipboardValue",
            "autofillState",
            "loginState",
            "packageName",
            "appName",
            "deviceId",
            "recipientName",
            "verificationStatus",
            "reportStatus",
            "submissionStatus",
            "recoveryStatus"
        ).forEach { forbiddenSliceApi ->
            assertFalse(
                "official-pressure slice used forbidden API/source: $forbiddenSliceApi",
                fullSliceSource.contains(forbiddenSliceApi, ignoreCase = true)
            )
        }
        assertFalse(nonShareSliceSource.contains("Intent.ACTION_SEND"))

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "SEND_SMS",
            "RECEIVE_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "CAMERA",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(
                "manifest declared forbidden permission token: $forbiddenPermission",
                manifest.contains(forbiddenPermission)
            )
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun recoveryPromiseRealityCheckCoachIsPureStatelessRoutesOnlyAndSharesBuilderSummary() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/RecoveryPromiseRealityCheckCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val safetyPlanStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.recoveryPromiseRealityCheckCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.recoveryPromiseRealityCheckCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openRecoveryPromiseRealityCheckAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareRecoveryPromiseRealityCheckCoach"
        )
        val normalizedActivitySource = activitySource.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertTrue(activitySource.contains("RECOVERY_PROMISE_REALITY_CHECK_COACH"))
        assertTrue(
            normalizedActivitySource.contains(
                "Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH -> " +
                    "recoveryPromiseRealityCheckCoachScreen()"
            )
        )

        val entryMarkers = listOf(
            "private fun LinearLayout.officialPressureRealityCheckCoachScreen",
            "private fun LinearLayout.credentialSecretFreezeCoachScreen",
            "private fun LinearLayout.newNumberQuarantineCoachScreen",
            "private fun LinearLayout.voiceImpersonationRealityCheckCoachScreen",
            "private fun LinearLayout.evidenceHandoffPrivacyCoachScreen",
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.safePastePrivacyCoachScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.home()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.incidentTimelineBriefingScreen",
            "private fun LinearLayout.privacyCenterScreen",
            "private fun LinearLayout.evidencePreservationScreen",
            "private fun LinearLayout.helpDirectoryScreen",
            "private fun LinearLayout.officialReportPrepScreen",
            "private fun LinearLayout.readinessSection()",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.exposureImpactTriageScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen"
        )
        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.RECOVERY_PROMISE_REALITY_CHECK_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.map { marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "recovery-promise entry must have one show-only button in $marker",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(29, entryRouteSources.size)
        assertEquals(
            37,
            Regex("show\\(Screen\\.RECOVERY_PROMISE_REALITY_CHECK_COACH\\)")
                .findAll(activitySource)
                .count()
        )
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear()",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "startActivity(",
            "setIntent("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(
                "recovery-promise entry used side effect: $forbiddenEntrySideEffect",
                entryRouteSource.contains(forbiddenEntrySideEffect)
            )
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(
            Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource)
        )
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "analysis.redactedClue",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "SafetyPlanStore",
            "SafetyPlan(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "directory.entries",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "MessageDigest",
            "ContactsContract",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationListenerService",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "FileProvider",
            "MediaStore",
            "ContentResolver",
            "java.io.File",
            "Intent.EXTRA_STREAM",
            "Uri.parse",
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Intent.ACTION_VIEW",
            "Geocoder",
            "Patterns.WEB_URL",
            "URLUtil",
            "InetAddress",
            "PhoneNumberUtils",
            "Patterns.PHONE",
            "PaymentClient",
            "BankingClient",
            "CardClient",
            "WalletConnect",
            "BlockchainClient",
            "ExchangeClient",
            "RefundClient",
            "ChargebackClient",
            "BillingClient",
            "NfcAdapter",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "Settings.",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "recovery-promise model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource))
        }

        val builderCallPattern = Regex(
            "RecoveryPromiseRealityCheckCoachBuilder\\.build\\(\\s*" +
                "analysis\\s*=\\s*currentAnalysis\\s*,\\s*" +
                "readiness\\s*=\\s*FamilyReadinessBuilder\\.build\\(" +
                "safetyPlan\\.load\\(\\)\\)\\s*,\\s*" +
                "incidentCount\\s*=\\s*incidents\\.count\\(\\)\\s*\\)"
        )
        assertEquals(1, builderCallPattern.findAll(coachScreen).count())
        assertEquals(
            1,
            Regex("RecoveryPromiseRealityCheckCoachBuilder\\.build\\(")
                .findAll(activitySource)
                .count()
        )
        assertEquals(1, Regex("safetyPlan\\.load\\(\\)").findAll(coachScreen).count())
        assertEquals(1, Regex("incidents\\.count\\(\\)").findAll(coachScreen).count())
        assertFalse(coachScreen.contains("incidents.load("))
        assertFalse(coachScreen.contains("val plan ="))
        assertFalse(
            coachScreen
                .replace("FamilyReadinessBuilder.build(safetyPlan.load())", "")
                .contains("safetyPlan.")
        )
        assertFalse(coachScreen.replace("incidents.count()", "").contains("incidents."))

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
                "recovery-promise incident count decoded content: $forbiddenIncidentRead",
                incidentCountHelper.contains(forbiddenIncidentRead)
            )
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareRecoveryPromiseRealityCheckCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue(
                "recovery-promise UI omitted $renderedField",
                coachScreen.contains(renderedField)
            )
        }
        assertTrue(coachScreen.contains("secondaryButton(\"홈으로\")"))
        assertEquals(1, Regex("coach\\.cards\\.forEach").findAll(coachScreen).count())
        listOf(
            "card.displayOrder",
            "card.title",
            "card.recoveryPromiseScenario",
            "card.valueFreeActionSentence",
            "card.whyItMatters",
            "card.doNotPayShareSubmitInstallOrCallBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedCardField ->
            assertTrue(
                "recovery-promise card UI omitted $renderedCardField",
                cardRenderer.contains(renderedCardField)
            )
        }

        val statelessUiSource = coachScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "EditText(",
            "text.toString(",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "BiometricPrompt",
            "KeyStore",
            "ScamAnalyzer.analyze(",
            "pendingSharedText",
            "CheckBox(",
            "RadioButton(",
            "Switch(",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PackageManager",
            "onSaveInstanceState",
            ".save(",
            "putString(",
            "putBoolean(",
            "putInt(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "callerId",
            "callState",
            "clipboardText",
            "autofillState",
            "loginState",
            "recipientName",
            "caseId",
            "reportStatus",
            "submissionStatus",
            "paymentStatus",
            "walletStatus",
            "refundStatus",
            "chargebackStatus",
            "recoveryStatus"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "recovery-promise UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }

        val expectedActionRoutes = linkedMapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "SAFE_PASTE_PRIVACY_COACH" to "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "FAMILY_SAFE_WORD_RESET_COACH" to "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "VOICE_IMPERSONATION_REALITY_CHECK_COACH" to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            "NEW_NUMBER_QUARANTINE_COACH" to "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            "CREDENTIAL_SECRET_FREEZE_COACH" to
                "show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "OFFICIAL_PRESSURE_REALITY_CHECK_COACH" to
                "show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        val actionIdBlock = functionBlock(
            modelSource,
            "enum class RecoveryPromiseRealityCheckActionId"
        )
        val declaredActionIds = Regex("(?m)^\\s*([A-Z][A-Z_]+),?\\s*$")
            .findAll(actionIdBlock)
            .map { match -> match.groupValues[1] }
            .toSet()
        assertEquals(expectedActionRoutes.keys, declaredActionIds)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "recovery-promise route omitted $actionId: $destination",
                normalizedActionRouter.contains(
                    "RecoveryPromiseRealityCheckActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            expectedActionRoutes.size,
            Regex("RecoveryPromiseRealityCheckActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(actionRouter).count())
        assertFalse((coachScreen + cardRenderer + modelSource).contains("openHelpDirectory("))

        val shareArguments = Regex("shareRecoveryPromiseRealityCheckCoach\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertEquals(1, Regex("Intent\\.ACTION_SEND").findAll(shareHelper).count())
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "ScamAnalysis",
            "currentAnalysis",
            "analysis.input",
            "IncidentPreviewRedactor",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "NotificationManager",
            "PackageManager",
            "prefs.edit(",
            "safetyPlan.save(",
            "putString(",
            "putBoolean(",
            ".save(",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "callerId",
            "callState",
            "clipboardText",
            "autofillState",
            "loginState",
            "recipientName",
            "caseId",
            "reportStatus",
            "submissionStatus",
            "paymentStatus",
            "walletStatus",
            "refundStatus",
            "chargebackStatus",
            "recoveryStatus"
        ).forEach { forbiddenShareSource ->
            assertFalse(
                "recovery-promise share used forbidden source: $forbiddenShareSource",
                shareHelper.contains(forbiddenShareSource, ignoreCase = true)
            )
        }

        val persistenceSources = incidentStoreSource + safetyPlanStoreSource
        listOf(
            "RecoveryPromiseRealityCheck",
            "recovery_promise_reality_check",
            "recovery_promise_status",
            "refund_status",
            "chargeback_status",
            "wallet_status",
            "payment_status",
            "case_status",
            "report_status",
            "recovery_status"
        ).forEach { forbiddenStoredState ->
            assertFalse(
                "recovery-promise state was persisted: $forbiddenStoredState",
                persistenceSources.contains(forbiddenStoredState, ignoreCase = true)
            )
        }

        val nonShareSliceSource = coachScreen + cardRenderer + actionRouter + modelSource
        assertFalse(nonShareSliceSource.contains("Intent.ACTION_SEND"))
        listOf(
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "MessageDigest",
            "ContactsContract",
            "ACTION_SENDTO",
            "smsto:",
            "tel:",
            "PhoneNumberUtils",
            "Patterns.PHONE",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationCompat",
            "NotificationListenerService",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ContentResolver",
            "java.io.File",
            "AlarmManager",
            "WorkManager",
            "JobScheduler",
            "BroadcastReceiver",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Geocoder",
            "Patterns.WEB_URL",
            "URLUtil",
            "InetAddress",
            "PaymentClient",
            "BankingClient",
            "WalletConnect",
            "BlockchainClient",
            "ExchangeClient",
            "RefundClient",
            "ChargebackClient",
            "BillingClient",
            "NfcAdapter",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "Settings.",
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save(",
            "officialNumber",
            "directoryValue",
            "callerId",
            "callState",
            "urlValue",
            "domainValue",
            "caseId",
            "accountValue",
            "cardValue",
            "bankValue",
            "paymentValue",
            "transactionValue",
            "walletValue",
            "otpValue",
            "pinValue",
            "passwordValue",
            "securityAnswerValue",
            "clipboardValue",
            "autofillState",
            "loginState",
            "packageName",
            "appName",
            "deviceId",
            "recipientName",
            "reportStatus",
            "submissionStatus",
            "refundStatus",
            "chargebackStatus",
            "recoveryStatus"
        ).forEach { forbiddenSliceApi ->
            assertFalse(
                "recovery-promise slice used forbidden API/source: $forbiddenSliceApi",
                nonShareSliceSource.contains(forbiddenSliceApi, ignoreCase = true)
            )
        }

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "SEND_SMS",
            "RECEIVE_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "CAMERA",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(
                "manifest declared forbidden permission token: $forbiddenPermission",
                manifest.contains(forbiddenPermission)
            )
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun giftCardCodePauseCoachUsesValueFreeInputsRoutesOnlyAndTextOnlyShare() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/GiftCardCodePauseCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val safetyPlanStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.giftCardCodePauseCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.giftCardCodePauseCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openGiftCardCodePauseAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareGiftCardCodePauseCoach"
        )
        val normalizedActivitySource = activitySource.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertTrue(
            normalizedActivitySource.contains(
                "Screen.GIFT_CARD_CODE_PAUSE_COACH -> giftCardCodePauseCoachScreen()"
            )
        )
        val entryMarkers = listOf(
            "private fun LinearLayout.recoveryPromiseRealityCheckCoachScreen",
            "private fun LinearLayout.officialPressureRealityCheckCoachScreen",
            "private fun LinearLayout.credentialSecretFreezeCoachScreen",
            "private fun LinearLayout.newNumberQuarantineCoachScreen",
            "private fun LinearLayout.voiceImpersonationRealityCheckCoachScreen",
            "private fun LinearLayout.evidenceHandoffPrivacyCoachScreen",
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.safePastePrivacyCoachScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.home()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.incidentTimelineBriefingScreen",
            "private fun LinearLayout.privacyCenterScreen",
            "private fun LinearLayout.evidencePreservationScreen",
            "private fun LinearLayout.helpDirectoryScreen",
            "private fun LinearLayout.officialReportPrepScreen",
            "private fun LinearLayout.readinessSection()",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.exposureImpactTriageScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen"
        )
        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.GIFT_CARD_CODE_PAUSE_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.map { marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "gift-card entry must have one show-only button in $marker",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(30, entryRouteSources.size)
        assertEquals(
            37,
            Regex("show\\(Screen\\.GIFT_CARD_CODE_PAUSE_COACH\\)")
                .findAll(activitySource)
                .count()
        )
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear()",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "Uri.parse",
            "startActivity("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(entryRouteSource.contains(forbiddenEntrySideEffect))
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "analysis.redactedClue",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "SafetyPlanStore",
            "SafetyPlan(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "directory.entries",
            "ContactsContract",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "MessageDigest",
            "FileProvider",
            "MediaStore",
            "ContentResolver",
            "java.io.File",
            "Intent.EXTRA_STREAM",
            "Uri.parse",
            "BarcodeScanner",
            "BarcodeScanning",
            "QRCodeReader",
            "TextRecognition",
            "ImageAnalysis",
            "ImageCapture",
            "CameraManager",
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Intent.ACTION_VIEW",
            "Geocoder",
            "Patterns.WEB_URL",
            "URLUtil",
            "PhoneNumberUtils",
            "Patterns.PHONE",
            "GiftCardClient",
            "RedeemClient",
            "BalanceClient",
            "PaymentClient",
            "BankingClient",
            "CardClient",
            "WalletConnect",
            "BlockchainClient",
            "ExchangeClient",
            "RefundClient",
            "ChargebackClient",
            "BillingClient",
            "NfcAdapter",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "Settings.",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "gift-card model used forbidden source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource))
        }

        val builderCallPattern = Regex(
            "GiftCardCodePauseCoachBuilder\\.build\\(\\s*" +
                "analysis\\s*=\\s*currentAnalysis\\s*,\\s*" +
                "readiness\\s*=\\s*FamilyReadinessBuilder\\.build\\(" +
                "safetyPlan\\.load\\(\\)\\)\\s*,\\s*" +
                "incidentCount\\s*=\\s*incidents\\.count\\(\\)\\s*\\)"
        )
        assertEquals(1, builderCallPattern.findAll(coachScreen).count())
        assertEquals(
            1,
            Regex("GiftCardCodePauseCoachBuilder\\.build\\(")
                .findAll(activitySource)
                .count()
        )
        assertEquals(1, Regex("safetyPlan\\.load\\(\\)").findAll(coachScreen).count())
        assertEquals(1, Regex("incidents\\.count\\(\\)").findAll(coachScreen).count())
        assertFalse(coachScreen.contains("incidents.load("))
        assertFalse(
            coachScreen
                .replace("FamilyReadinessBuilder.build(safetyPlan.load())", "")
                .contains("safetyPlan.")
        )
        assertFalse(coachScreen.replace("incidents.count()", "").contains("incidents."))

        val incidentCountHelper = functionBlock(incidentStoreSource, "fun count()")
        assertTrue(incidentCountHelper.contains("lineSequence()"))
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
            assertFalse(incidentCountHelper.contains(forbiddenIncidentRead))
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareGiftCardCodePauseCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue("gift-card UI omitted $renderedField", coachScreen.contains(renderedField))
        }
        listOf(
            "card.displayOrder",
            "card.title",
            "card.codePressureScenario",
            "card.valueFreeActionSentence",
            "card.whyItMatters",
            "card.doNotBuyShareScanRedeemOrAttachBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedCardField ->
            assertTrue(cardRenderer.contains(renderedCardField))
        }

        val statelessUiSource = coachScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "EditText(",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "ScamAnalyzer.analyze(",
            "pendingSharedText",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PackageManager",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "callerId",
            "callState",
            "clipboardText",
            "autofillState",
            "loginState",
            "recipientName",
            "giftCardCodeValue",
            "barcodeValue",
            "qrValue",
            "paymentStatus",
            "walletStatus",
            "refundStatus",
            "chargebackStatus",
            "recoveryStatus"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "gift-card UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }

        val expectedActionRoutes = linkedMapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "SAFE_PASTE_PRIVACY_COACH" to "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "FAMILY_SAFE_WORD_RESET_COACH" to "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "VOICE_IMPERSONATION_REALITY_CHECK_COACH" to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            "NEW_NUMBER_QUARANTINE_COACH" to "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            "CREDENTIAL_SECRET_FREEZE_COACH" to
                "show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "OFFICIAL_PRESSURE_REALITY_CHECK_COACH" to
                "show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)",
            "RECOVERY_PROMISE_REALITY_CHECK_COACH" to
                "show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.GIFT_CARD_CODE_PAUSE_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        val actionIdBlock = functionBlock(modelSource, "enum class GiftCardCodePauseActionId")
        val declaredActionIds = Regex("(?m)^\\s*([A-Z][A-Z_]+),?\\s*$")
            .findAll(actionIdBlock)
            .map { match -> match.groupValues[1] }
            .toSet()
        assertEquals(expectedActionRoutes.keys, declaredActionIds)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                normalizedActionRouter.contains(
                    "GiftCardCodePauseActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            expectedActionRoutes.size,
            Regex("GiftCardCodePauseActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(actionRouter).count())
        assertFalse((coachScreen + cardRenderer + modelSource).contains("openHelpDirectory("))

        val shareArguments = Regex("shareGiftCardCodePauseCoach\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertEquals(1, Regex("Intent\\.ACTION_SEND").findAll(shareHelper).count())
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "ScamAnalysis",
            "currentAnalysis",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "NotificationManager",
            "PackageManager",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW"
        ).forEach { forbiddenShareSource ->
            assertFalse(shareHelper.contains(forbiddenShareSource, ignoreCase = true))
        }

        val persistenceSources = incidentStoreSource + safetyPlanStoreSource
        listOf(
            "GiftCardCodePause",
            "gift_card_code_pause",
            "gift_card_code_status",
            "voucher_code_status",
            "barcode_status",
            "redeem_status",
            "balance_status",
            "purchase_status"
        ).forEach { forbiddenStoredState ->
            assertFalse(
                persistenceSources.contains(forbiddenStoredState, ignoreCase = true)
            )
        }

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "SEND_SMS",
            "RECEIVE_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "CAMERA",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(manifest.contains(forbiddenPermission))
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun linkAppInstallQuarantineCoachUsesBoundedValueFreeInputsAndRoutesOnly() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/LinkAppInstallQuarantineCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val safetyPlanStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.linkAppInstallQuarantineCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.linkAppInstallQuarantineCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openLinkAppInstallQuarantineAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareLinkAppInstallQuarantineCoach"
        )
        val normalizedActivitySource = activitySource.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertTrue(
            normalizedActivitySource.contains(
                "Screen.LINK_APP_INSTALL_QUARANTINE_COACH -> " +
                    "linkAppInstallQuarantineCoachScreen()"
            )
        )

        val entryMarkers = listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.readinessSection()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.safePastePrivacyCoachScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.credentialSecretFreezeCoachScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.exposureImpactTriageScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.giftCardCodePauseCoachScreen",
            "private fun LinearLayout.recoveryPromiseRealityCheckCoachScreen",
            "private fun LinearLayout.officialPressureRealityCheckCoachScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.newNumberQuarantineCoachScreen",
            "private fun LinearLayout.voiceImpersonationRealityCheckCoachScreen",
            "private fun LinearLayout.evidenceHandoffPrivacyCoachScreen",
            "private fun LinearLayout.evidencePreservationScreen",
            "private fun LinearLayout.incidentTimelineBriefingScreen",
            "private fun LinearLayout.helpDirectoryScreen",
            "private fun LinearLayout.officialReportPrepScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.privacyCenterScreen"
        )
        assertEquals(31, entryMarkers.size)
        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.LINK_APP_INSTALL_QUARANTINE_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.map { marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "link/app entry must have one show-only button in $marker",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(
            38,
            Regex("show\\(Screen\\.LINK_APP_INSTALL_QUARANTINE_COACH\\)")
                .findAll(activitySource)
                .count()
        )
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear()",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "Uri.parse",
            "startActivity("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(entryRouteSource.contains(forbiddenEntrySideEffect))
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(
            Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource)
        )
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)

        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "analysis.redactedClue",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "SafetyPlanStore",
            "SafetyPlan(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "directory.entries",
            "dialNumber",
            "urlValue",
            "domainValue",
            "shortenedLinkValue",
            "qrValue",
            "barcodeValue",
            "apkValue",
            "packageName",
            "appLabel",
            "installedApps",
            "permissionState",
            "deviceId",
            "callerId",
            "caseId",
            "reportStatus",
            "submissionStatus",
            "receiptId",
            "accountNumber",
            "cardNumber",
            "bankValue",
            "walletValue",
            "transactionValue",
            "otpValue",
            "pinValue",
            "passwordValue",
            "securityAnswer",
            "residentId",
            "clipboardText",
            "loginState",
            "verificationResult",
            "recoveryResult",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "link/app model used forbidden value or source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource, ignoreCase = true)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource))
        }

        val builderCallPattern = Regex(
            "LinkAppInstallQuarantineCoachBuilder\\.build\\(\\s*" +
                "analysis\\s*=\\s*currentAnalysis\\s*,\\s*" +
                "readiness\\s*=\\s*FamilyReadinessBuilder\\.build\\(" +
                "safetyPlan\\.load\\(\\)\\)\\s*,\\s*" +
                "incidentCount\\s*=\\s*incidents\\.count\\(\\)\\s*\\)"
        )
        assertEquals(1, builderCallPattern.findAll(coachScreen).count())
        assertEquals(
            1,
            Regex("LinkAppInstallQuarantineCoachBuilder\\.build\\(")
                .findAll(activitySource)
                .count()
        )
        assertEquals(1, Regex("safetyPlan\\.load\\(\\)").findAll(coachScreen).count())
        assertEquals(1, Regex("incidents\\.count\\(\\)").findAll(coachScreen).count())
        assertFalse(coachScreen.contains("incidents.load("))
        assertFalse(
            coachScreen
                .replace("FamilyReadinessBuilder.build(safetyPlan.load())", "")
                .contains("safetyPlan.")
        )
        assertFalse(coachScreen.replace("incidents.count()", "").contains("incidents."))

        val incidentCountHelper = functionBlock(incidentStoreSource, "fun count()")
        assertTrue(incidentCountHelper.contains("lineSequence()"))
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
            assertFalse(incidentCountHelper.contains(forbiddenIncidentRead))
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareLinkAppInstallQuarantineCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue("link/app UI omitted $renderedField", coachScreen.contains(renderedField))
        }
        listOf(
            "card.displayOrder",
            "card.title",
            "card.linkAppPressureScenario",
            "card.valueFreeActionSentence",
            "card.whyItMatters",
            "card.doNotOpenInstallEnterScanGrantOrShareBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedCardField ->
            assertTrue(cardRenderer.contains(renderedCardField))
        }

        val statelessUiSource = coachScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "EditText(",
            "pendingSharedText",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "callerId",
            "callState",
            "clipboardText",
            "autofillState",
            "loginState",
            "recipientName",
            "urlValue",
            "domainValue",
            "qrValue",
            "barcodeValue",
            "apkValue",
            "packageName",
            "appLabel",
            "installedApps",
            "permissionState",
            "malwareStatus",
            "deviceId",
            "accountNumber",
            "cardNumber",
            "bankValue",
            "walletValue",
            "transactionValue",
            "giftCardCodeValue",
            "otpValue",
            "pinValue",
            "passwordValue",
            "caseId",
            "reportStatus",
            "submissionStatus",
            "receiptId",
            "verificationResult",
            "recoveryResult"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "link/app UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }

        val expectedActionRoutes = linkedMapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "SAFE_PASTE_PRIVACY_COACH" to "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "FAMILY_SAFE_WORD_RESET_COACH" to "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "VOICE_IMPERSONATION_REALITY_CHECK_COACH" to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            "NEW_NUMBER_QUARANTINE_COACH" to "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            "CREDENTIAL_SECRET_FREEZE_COACH" to
                "show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "GIFT_CARD_CODE_PAUSE_COACH" to "show(Screen.GIFT_CARD_CODE_PAUSE_COACH)",
            "OFFICIAL_PRESSURE_REALITY_CHECK_COACH" to
                "show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)",
            "RECOVERY_PROMISE_REALITY_CHECK_COACH" to
                "show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "INCIDENT_TIMELINE_BRIEFING" to "show(Screen.TIMELINE_BRIEFING)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(36, expectedActionRoutes.size)
        val actionIdBlock = functionBlock(
            modelSource,
            "enum class LinkAppInstallQuarantineActionId"
        )
        val declaredActionIds = Regex("(?m)^\\s*([A-Z][A-Z_]+),?\\s*$")
            .findAll(actionIdBlock)
            .map { match -> match.groupValues[1] }
            .toSet()
        assertEquals(expectedActionRoutes.keys, declaredActionIds)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                normalizedActionRouter.contains(
                    "LinkAppInstallQuarantineActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            36,
            Regex("LinkAppInstallQuarantineActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(actionRouter).count())
        assertFalse((coachScreen + cardRenderer + modelSource).contains("openHelpDirectory("))

        val shareArguments = Regex("shareLinkAppInstallQuarantineCoach\\(([^)]*)\\)")
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertEquals(1, Regex("Intent\\.ACTION_SEND").findAll(shareHelper).count())
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "ScamAnalysis",
            "currentAnalysis",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "NotificationManager",
            "PackageManager",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW"
        ).forEach { forbiddenShareSource ->
            assertFalse(shareHelper.contains(forbiddenShareSource, ignoreCase = true))
        }

        val boundedImplementationSource = coachScreen + cardRenderer + actionRouter + modelSource
        listOf(
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Intent.ACTION_VIEW",
            "Geocoder",
            "Patterns.WEB_URL",
            "URLUtil",
            "InetAddress",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PhoneNumberUtils",
            "Patterns.PHONE",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "ACTION_SENDTO",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "CallLog",
            "SmsManager",
            "ContactsContract",
            "NotificationManager",
            "NotificationCompat",
            "NotificationListenerService",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "android.credentials",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "MessageDigest",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "BarcodeScanner",
            "BarcodeScanning",
            "QRCodeReader",
            "TextRecognition",
            "ImageAnalysis",
            "ImageCapture",
            "CameraManager",
            "BitmapFactory",
            "ExifInterface",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "PackageInstaller",
            "deletePackage",
            "ACTION_MANAGE_UNKNOWN_APP_SOURCES",
            "ACTION_APPLICATION_DETAILS_SETTINGS",
            "ACTION_REQUEST_INSTALL_PACKAGE",
            "ACTION_UNINSTALL_PACKAGE",
            "DevicePolicyManager",
            "AccessibilityService",
            "AccessibilityManager",
            "VpnService",
            "UsageStatsManager",
            "AppOpsManager",
            "BillingClient",
            "NfcAdapter",
            "WalletConnect",
            "BlockchainClient",
            "PaymentClient",
            "BankingClient",
            "CardClient",
            "RefundClient",
            "ChargebackClient",
            "GiftCardClient",
            "BalanceClient",
            "RedeemClient",
            "MalwareScanner",
            "AntivirusClient",
            "VirusTotal",
            "AlarmManager",
            "WorkManager",
            "JobScheduler",
            "ForegroundService",
            "BroadcastReceiver",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear()",
            "incidents.add(",
            "incidents.clear(",
            "incidents.load("
        ).forEach { forbiddenSliceApi ->
            assertFalse(
                "link/app slice used forbidden API/source: $forbiddenSliceApi",
                boundedImplementationSource.contains(forbiddenSliceApi, ignoreCase = true)
            )
        }

        val persistenceSources = incidentStoreSource + safetyPlanStoreSource
        listOf(
            "LinkAppInstallQuarantine",
            "link_app_install_quarantine",
            "link_quarantine_status",
            "app_install_status",
            "permission_status",
            "malware_status",
            "package_status",
            "link_validation_status"
        ).forEach { forbiddenStoredState ->
            assertFalse(
                persistenceSources.contains(forbiddenStoredState, ignoreCase = true)
            )
        }

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "SEND_SMS",
            "RECEIVE_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "CAMERA",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(manifest.contains(forbiddenPermission))
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun cashWithdrawalCourierHandoffPauseCoachUsesBoundedValueFreeInputsAndRoutesOnly() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/" +
                "CashWithdrawalCourierHandoffPauseCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val safetyPlanStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.cashWithdrawalCourierHandoffPauseCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.cashWithdrawalCourierHandoffPauseCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openCashWithdrawalCourierHandoffPauseAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareCashWithdrawalCourierHandoffPauseCoach"
        )
        val normalizedActivitySource = activitySource.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertTrue(
            normalizedActivitySource.contains(
                "Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH -> " +
                    "cashWithdrawalCourierHandoffPauseCoachScreen()"
            )
        )

        val entryMarkers = listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.readinessSection()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.officialPressureRealityCheckCoachScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.recoveryPromiseRealityCheckCoachScreen",
            "private fun LinearLayout.linkAppInstallQuarantineCoachScreen",
            "private fun LinearLayout.giftCardCodePauseCoachScreen",
            "private fun LinearLayout.credentialSecretFreezeCoachScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.exposureImpactTriageScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.newNumberQuarantineCoachScreen",
            "private fun LinearLayout.voiceImpersonationRealityCheckCoachScreen",
            "private fun LinearLayout.evidenceHandoffPrivacyCoachScreen",
            "private fun LinearLayout.evidencePreservationScreen",
            "private fun LinearLayout.incidentTimelineBriefingScreen",
            "private fun LinearLayout.helpDirectoryScreen",
            "private fun LinearLayout.officialReportPrepScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.privacyCenterScreen"
        )
        assertEquals(31, entryMarkers.size)
        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.map { marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "cash/courier entry must have one show-only button in $marker",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(
            37,
            Regex("show\\(Screen\\.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH\\)")
                .findAll(activitySource)
                .count()
        )
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear()",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "Uri.parse",
            "startActivity("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(entryRouteSource.contains(forbiddenEntrySideEffect))
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(
            Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource)
        )
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)
        assertEquals(2, Regex("\\bincidentCount\\b").findAll(modelSource).count())
        assertFalse(Regex("analysis\\s*\\.\\s*[A-Za-z]").containsMatchIn(modelSource))

        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "analysis.redactedClue",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "SafetyPlanStore",
            "SafetyPlan(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "directory.entries",
            "dialNumber",
            "locationValue",
            "addressValue",
            "routeValue",
            "meetingPoint",
            "lockerId",
            "courierId",
            "cashAmount",
            "cashSerial",
            "banknoteValue",
            "accountNumber",
            "cardNumber",
            "bankValue",
            "walletValue",
            "transactionValue",
            "otpValue",
            "pinValue",
            "passwordValue",
            "securityAnswer",
            "residentId",
            "deviceId",
            "callerId",
            "caseId",
            "reportStatus",
            "submissionStatus",
            "receiptId",
            "clipboardText",
            "loginState",
            "handoffStatus",
            "deliveryStatus",
            "recoveryResult",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "cash/courier model used forbidden value/source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource, ignoreCase = true)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource))
        }

        val builderCallPattern = Regex(
            "CashWithdrawalCourierHandoffPauseCoachBuilder\\.build\\(\\s*" +
                "analysis\\s*=\\s*currentAnalysis\\s*,\\s*" +
                "readiness\\s*=\\s*FamilyReadinessBuilder\\.build\\(" +
                "safetyPlan\\.load\\(\\)\\)\\s*,\\s*" +
                "incidentCount\\s*=\\s*incidents\\.count\\(\\)\\s*\\)"
        )
        assertEquals(1, builderCallPattern.findAll(coachScreen).count())
        assertEquals(
            1,
            Regex("CashWithdrawalCourierHandoffPauseCoachBuilder\\.build\\(")
                .findAll(activitySource)
                .count()
        )
        assertEquals(1, Regex("safetyPlan\\.load\\(\\)").findAll(coachScreen).count())
        assertEquals(1, Regex("incidents\\.count\\(\\)").findAll(coachScreen).count())
        assertFalse(coachScreen.contains("incidents.load("))
        assertFalse(
            coachScreen
                .replace("FamilyReadinessBuilder.build(safetyPlan.load())", "")
                .contains("safetyPlan.")
        )
        assertFalse(coachScreen.replace("incidents.count()", "").contains("incidents."))

        val incidentCountHelper = functionBlock(incidentStoreSource, "fun count()")
        assertTrue(incidentCountHelper.contains("lineSequence()"))
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
            assertFalse(incidentCountHelper.contains(forbiddenIncidentRead))
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareCashWithdrawalCourierHandoffPauseCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue("cash/courier UI omitted $renderedField", coachScreen.contains(renderedField))
        }
        listOf(
            "card.displayOrder",
            "card.title",
            "card.cashCourierPressureScenario",
            "card.valueFreeActionSentence",
            "card.whyItMatters",
            "card.doNotWithdrawTravelMeetHandoffShareOrAttachBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedCardField ->
            assertTrue(cardRenderer.contains(renderedCardField))
        }

        val statelessUiSource = coachScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "EditText(",
            "pendingSharedText",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "callerId",
            "callState",
            "clipboardText",
            "autofillState",
            "loginState",
            "recipientName",
            "locationValue",
            "addressValue",
            "routeValue",
            "meetingPoint",
            "lockerId",
            "courierId",
            "cashAmount",
            "cashSerial",
            "accountNumber",
            "cardNumber",
            "bankValue",
            "walletValue",
            "transactionValue",
            "giftCardCodeValue",
            "otpValue",
            "pinValue",
            "passwordValue",
            "packageName",
            "appLabel",
            "installedApps",
            "permissionState",
            "deviceId",
            "caseId",
            "reportStatus",
            "submissionStatus",
            "receiptId",
            "handoffStatus",
            "deliveryStatus",
            "recoveryResult"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "cash/courier UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }

        val expectedActionRoutes = linkedMapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "SAFE_PASTE_PRIVACY_COACH" to "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "FAMILY_SAFE_WORD_RESET_COACH" to "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "VOICE_IMPERSONATION_REALITY_CHECK_COACH" to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            "NEW_NUMBER_QUARANTINE_COACH" to "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            "CREDENTIAL_SECRET_FREEZE_COACH" to
                "show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "GIFT_CARD_CODE_PAUSE_COACH" to "show(Screen.GIFT_CARD_CODE_PAUSE_COACH)",
            "LINK_APP_INSTALL_QUARANTINE_COACH" to
                "show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)",
            "OFFICIAL_PRESSURE_REALITY_CHECK_COACH" to
                "show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)",
            "RECOVERY_PROMISE_REALITY_CHECK_COACH" to
                "show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "INCIDENT_TIMELINE_BRIEFING" to "show(Screen.TIMELINE_BRIEFING)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(37, expectedActionRoutes.size)
        val actionIdBlock = functionBlock(
            modelSource,
            "enum class CashWithdrawalCourierHandoffPauseActionId"
        )
        val declaredActionIds = Regex("(?m)^\\s*([A-Z][A-Z_]+),?\\s*$")
            .findAll(actionIdBlock)
            .map { match -> match.groupValues[1] }
            .toSet()
        assertEquals(expectedActionRoutes.keys, declaredActionIds)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                normalizedActionRouter.contains(
                    "CashWithdrawalCourierHandoffPauseActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            37,
            Regex("CashWithdrawalCourierHandoffPauseActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(actionRouter).count())
        assertFalse((coachScreen + cardRenderer + modelSource).contains("openHelpDirectory("))

        val shareArguments = Regex(
            "shareCashWithdrawalCourierHandoffPauseCoach\\(([^)]*)\\)"
        )
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertEquals(1, Regex("Intent\\.ACTION_SEND").findAll(shareHelper).count())
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "ScamAnalysis",
            "currentAnalysis",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "NotificationManager",
            "PackageManager",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW"
        ).forEach { forbiddenShareSource ->
            assertFalse(shareHelper.contains(forbiddenShareSource, ignoreCase = true))
        }

        val boundedImplementationSource = coachScreen + cardRenderer + actionRouter + modelSource
        listOf(
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Intent.ACTION_VIEW",
            "Geocoder",
            "LocationManager",
            "FusedLocationProvider",
            "LocationServices",
            "Maps",
            "GoogleMap",
            "MapView",
            "NavController",
            "Patterns.WEB_URL",
            "URLUtil",
            "InetAddress",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PhoneNumberUtils",
            "Patterns.PHONE",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "ACTION_SENDTO",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "CallLog",
            "SmsManager",
            "ContactsContract",
            "NotificationManager",
            "NotificationCompat",
            "NotificationListenerService",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "android.credentials",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "MessageDigest",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "BarcodeScanner",
            "QRCodeReader",
            "TextRecognition",
            "ImageAnalysis",
            "ImageCapture",
            "CameraManager",
            "BitmapFactory",
            "ExifInterface",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "PackageInstaller",
            "deletePackage",
            "ACTION_MANAGE_UNKNOWN_APP_SOURCES",
            "ACTION_APPLICATION_DETAILS_SETTINGS",
            "ACTION_REQUEST_INSTALL_PACKAGE",
            "ACTION_UNINSTALL_PACKAGE",
            "DevicePolicyManager",
            "AccessibilityService",
            "AccessibilityManager",
            "VpnService",
            "UsageStatsManager",
            "AppOpsManager",
            "BillingClient",
            "NfcAdapter",
            "WalletConnect",
            "PaymentClient",
            "BankingClient",
            "CardClient",
            "AtmClient",
            "CashManager",
            "BankBranch",
            "CourierClient",
            "DeliveryTracking",
            "TrackingClient",
            "MeetingScheduler",
            "RefundClient",
            "ChargebackClient",
            "GiftCardClient",
            "AlarmManager",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "ForegroundService",
            "BroadcastReceiver",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear()",
            "incidents.add(",
            "incidents.clear(",
            "incidents.load("
        ).forEach { forbiddenSliceApi ->
            assertFalse(
                "cash/courier slice used forbidden API/source: $forbiddenSliceApi",
                boundedImplementationSource.contains(forbiddenSliceApi, ignoreCase = true)
            )
        }

        val persistenceSources = incidentStoreSource + safetyPlanStoreSource
        listOf(
            "CashWithdrawalCourierHandoff",
            "cash_withdrawal_courier_handoff",
            "cash_withdrawal_status",
            "handoff_status",
            "courier_status",
            "location_status",
            "meeting_status",
            "delivery_status"
        ).forEach { forbiddenStoredState ->
            assertFalse(persistenceSources.contains(forbiddenStoredState, ignoreCase = true))
        }

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "SEND_SMS",
            "RECEIVE_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "CAMERA",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(manifest.contains(forbiddenPermission))
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun bankAtmStaffDisclosureScriptCoachUsesBoundedValueFreeInputsAndRoutesOnly() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/" +
                "BankAtmStaffDisclosureScriptCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val safetyPlanStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.bankAtmStaffDisclosureScriptCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.bankAtmStaffDisclosureScriptCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openBankAtmStaffDisclosureScriptAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareBankAtmStaffDisclosureScriptCoach"
        )
        val normalizedActivitySource = activitySource.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertTrue(
            normalizedActivitySource.contains(
                "Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH -> " +
                    "bankAtmStaffDisclosureScriptCoachScreen()"
            )
        )

        val entryMarkers = listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.readinessSection()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.cashWithdrawalCourierHandoffPauseCoachScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.officialPressureRealityCheckCoachScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.recoveryPromiseRealityCheckCoachScreen",
            "private fun LinearLayout.linkAppInstallQuarantineCoachScreen",
            "private fun LinearLayout.giftCardCodePauseCoachScreen",
            "private fun LinearLayout.credentialSecretFreezeCoachScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.exposureImpactTriageScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.newNumberQuarantineCoachScreen",
            "private fun LinearLayout.voiceImpersonationRealityCheckCoachScreen",
            "private fun LinearLayout.evidenceHandoffPrivacyCoachScreen",
            "private fun LinearLayout.evidencePreservationScreen",
            "private fun LinearLayout.incidentTimelineBriefingScreen",
            "private fun LinearLayout.helpDirectoryScreen",
            "private fun LinearLayout.officialReportPrepScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.privacyCenterScreen"
        )
        assertEquals(32, entryMarkers.size)
        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.map { marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "bank/ATM staff entry must have one show-only button in $marker",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(
            36,
            Regex("show\\(Screen\\.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH\\)")
                .findAll(activitySource)
                .count()
        )
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear()",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "Intent.ACTION_VIEW",
            "Intent.EXTRA_STREAM",
            "Uri.parse",
            "startActivity("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(entryRouteSource.contains(forbiddenEntrySideEffect))
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(
            Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource)
        )
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)
        assertEquals(2, Regex("\\bincidentCount\\b").findAll(modelSource).count())
        assertFalse(Regex("analysis\\s*\\.\\s*[A-Za-z]").containsMatchIn(modelSource))

        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "analysis.redactedClue",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "SafetyPlanStore",
            "SafetyPlan(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "directory.entries",
            "dialNumber",
            "officialNumber",
            "agencyName",
            "bankName",
            "branchName",
            "storeName",
            "counterName",
            "staffName",
            "helperName",
            "lawyerName",
            "investigatorName",
            "courierName",
            "locationValue",
            "addressValue",
            "routeValue",
            "meetingPoint",
            "lockerId",
            "courierId",
            "cashAmount",
            "cashSerial",
            "banknoteValue",
            "accountNumber",
            "cardNumber",
            "bankValue",
            "walletValue",
            "transactionValue",
            "paymentValue",
            "refundValue",
            "chargebackValue",
            "otpValue",
            "pinValue",
            "passwordValue",
            "securityAnswer",
            "residentId",
            "deviceId",
            "callerId",
            "phoneNumber",
            "urlValue",
            "domainValue",
            "qrValue",
            "barcodeValue",
            "codeValue",
            "packageValue",
            "appValue",
            "fileValue",
            "mediaValue",
            "uriValue",
            "caseId",
            "reportStatus",
            "submissionStatus",
            "receiptId",
            "clipboardText",
            "loginState",
            "callState",
            "smsState",
            "notificationState",
            "verificationStatus",
            "handoffStatus",
            "deliveryStatus",
            "collectionStatus",
            "depositStatus",
            "scheduleStatus",
            "paymentStatus",
            "refundStatus",
            "chargebackStatus",
            "freezeStatus",
            "cancelStatus",
            "blockStatus",
            "traceStatus",
            "recoveryResult",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "bank/ATM staff model used forbidden value/source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource, ignoreCase = true)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource))
        }

        val builderCallPattern = Regex(
            "BankAtmStaffDisclosureScriptCoachBuilder\\.build\\(\\s*" +
                "analysis\\s*=\\s*currentAnalysis\\s*,\\s*" +
                "readiness\\s*=\\s*FamilyReadinessBuilder\\.build\\(" +
                "safetyPlan\\.load\\(\\)\\)\\s*,\\s*" +
                "incidentCount\\s*=\\s*incidents\\.count\\(\\)\\s*\\)"
        )
        assertEquals(1, builderCallPattern.findAll(coachScreen).count())
        assertEquals(
            1,
            Regex("BankAtmStaffDisclosureScriptCoachBuilder\\.build\\(")
                .findAll(activitySource)
                .count()
        )
        assertEquals(1, Regex("safetyPlan\\.load\\(\\)").findAll(coachScreen).count())
        assertEquals(1, Regex("incidents\\.count\\(\\)").findAll(coachScreen).count())
        assertFalse(coachScreen.contains("incidents.load("))
        assertFalse(
            coachScreen
                .replace("FamilyReadinessBuilder.build(safetyPlan.load())", "")
                .contains("safetyPlan.")
        )
        assertFalse(coachScreen.replace("incidents.count()", "").contains("incidents."))

        val incidentCountHelper = functionBlock(incidentStoreSource, "fun count()")
        assertTrue(incidentCountHelper.contains("lineSequence()"))
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
            assertFalse(incidentCountHelper.contains(forbiddenIncidentRead))
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareBankAtmStaffDisclosureScriptCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue("bank/ATM staff UI omitted $renderedField", coachScreen.contains(renderedField))
        }
        listOf(
            "card.displayOrder",
            "card.title",
            "card.staffDisclosureSituation",
            "card.valueFreeScript",
            "card.whyItMatters",
            "card.doNotSayShareAttachOrRecordBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedCardField ->
            assertTrue(cardRenderer.contains(renderedCardField))
        }

        val statelessUiSource = coachScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "contact.label",
            "contact.number",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "EditText(",
            "pendingSharedText",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "callerId",
            "callState",
            "clipboardText",
            "autofillState",
            "loginState",
            "recipientName",
            "staffName",
            "helperName",
            "courierName",
            "locationValue",
            "addressValue",
            "routeValue",
            "meetingPoint",
            "lockerId",
            "courierId",
            "cashAmount",
            "cashSerial",
            "accountNumber",
            "cardNumber",
            "bankValue",
            "walletValue",
            "transactionValue",
            "giftCardCodeValue",
            "otpValue",
            "pinValue",
            "passwordValue",
            "packageName",
            "appLabel",
            "installedApps",
            "permissionState",
            "deviceId",
            "caseId",
            "reportStatus",
            "submissionStatus",
            "receiptId",
            "handoffStatus",
            "deliveryStatus",
            "collectionStatus",
            "depositStatus",
            "scheduleStatus",
            "paymentStatus",
            "refundStatus",
            "chargebackStatus",
            "freezeStatus",
            "cancelStatus",
            "blockStatus",
            "traceStatus",
            "recoveryResult"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "bank/ATM staff UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }

        val expectedActionRoutes = linkedMapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "SAFE_PASTE_PRIVACY_COACH" to "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "FAMILY_SAFE_WORD_RESET_COACH" to "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "VOICE_IMPERSONATION_REALITY_CHECK_COACH" to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            "NEW_NUMBER_QUARANTINE_COACH" to "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            "CREDENTIAL_SECRET_FREEZE_COACH" to
                "show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH" to
                "show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "GIFT_CARD_CODE_PAUSE_COACH" to "show(Screen.GIFT_CARD_CODE_PAUSE_COACH)",
            "LINK_APP_INSTALL_QUARANTINE_COACH" to
                "show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)",
            "OFFICIAL_PRESSURE_REALITY_CHECK_COACH" to
                "show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)",
            "RECOVERY_PROMISE_REALITY_CHECK_COACH" to
                "show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "INCIDENT_TIMELINE_BRIEFING" to "show(Screen.TIMELINE_BRIEFING)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(38, expectedActionRoutes.size)
        val actionIdBlock = functionBlock(
            modelSource,
            "enum class BankAtmStaffDisclosureScriptActionId"
        )
        val declaredActionIds = Regex("(?m)^\\s*([A-Z][A-Z_]+),?\\s*$")
            .findAll(actionIdBlock)
            .map { match -> match.groupValues[1] }
            .toSet()
        assertEquals(expectedActionRoutes.keys, declaredActionIds)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                normalizedActionRouter.contains(
                    "BankAtmStaffDisclosureScriptActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            38,
            Regex("BankAtmStaffDisclosureScriptActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(actionRouter).count())
        assertFalse((coachScreen + cardRenderer + modelSource).contains("openHelpDirectory("))

        val shareArguments = Regex(
            "shareBankAtmStaffDisclosureScriptCoach\\(([^)]*)\\)"
        )
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertEquals(1, Regex("Intent\\.ACTION_SEND").findAll(shareHelper).count())
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_PICK",
            "ScamAnalysis",
            "currentAnalysis",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "NotificationManager",
            "PackageManager",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW"
        ).forEach { forbiddenShareSource ->
            assertFalse(shareHelper.contains(forbiddenShareSource, ignoreCase = true))
        }

        val boundedImplementationSource = coachScreen + cardRenderer + actionRouter + modelSource
        listOf(
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Intent.ACTION_VIEW",
            "Geocoder",
            "android.location.Address",
            "RoutePlanner",
            "NavigationManager",
            "LocationManager",
            "FusedLocationProvider",
            "LocationServices",
            "Maps",
            "GoogleMap",
            "MapView",
            "NavController",
            "Patterns.WEB_URL",
            "URLUtil",
            "InetAddress",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PhoneNumberUtils",
            "Patterns.PHONE",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "ACTION_SENDTO",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "CallLog",
            "SmsManager",
            "ContactsContract",
            "NotificationManager",
            "NotificationCompat",
            "NotificationListenerService",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "android.credentials",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "MessageDigest",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "BarcodeScanner",
            "QRCodeReader",
            "TextRecognition",
            "ImageAnalysis",
            "ImageCapture",
            "CameraManager",
            "BitmapFactory",
            "ExifInterface",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "PackageInstaller",
            "deletePackage",
            "ACTION_MANAGE_UNKNOWN_APP_SOURCES",
            "ACTION_APPLICATION_DETAILS_SETTINGS",
            "ACTION_REQUEST_INSTALL_PACKAGE",
            "ACTION_UNINSTALL_PACKAGE",
            "DevicePolicyManager",
            "AccessibilityService",
            "AccessibilityManager",
            "VpnService",
            "UsageStatsManager",
            "AppOpsManager",
            "BillingClient",
            "NfcAdapter",
            "WalletConnect",
            "PaymentClient",
            "BankingClient",
            "CardClient",
            "AtmClient",
            "CashManager",
            "BankBranch",
            "BranchFinder",
            "AtmLocator",
            "AtmFinder",
            "StaffDirectory",
            "StaffIdentity",
            "IdentityVerifier",
            "DocumentValidator",
            "CaseLookup",
            "ReportSubmission",
            "CourierClient",
            "CourierTracker",
            "ParcelTracker",
            "LockerTracker",
            "DeliveryTracking",
            "TrackingClient",
            "MeetingScheduler",
            "RefundClient",
            "ChargebackClient",
            "GiftCardClient",
            "AlarmManager",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "ForegroundService",
            "BroadcastReceiver",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear()",
            "incidents.add(",
            "incidents.clear(",
            "incidents.load("
        ).forEach { forbiddenSliceApi ->
            assertFalse(
                "bank/ATM staff slice used forbidden API/source: $forbiddenSliceApi",
                boundedImplementationSource.contains(forbiddenSliceApi, ignoreCase = true)
            )
        }

        val persistenceSources = incidentStoreSource + safetyPlanStoreSource
        listOf(
            "BankAtmStaffDisclosureScript",
            "bank_atm_staff_disclosure",
            "staff_disclosure_status",
            "staff_identity_status",
            "bank_atm_status",
            "branch_status",
            "counter_status",
            "handoff_status",
            "location_status",
            "meeting_status",
            "delivery_status"
        ).forEach { forbiddenStoredState ->
            assertFalse(persistenceSources.contains(forbiddenStoredState, ignoreCase = true))
        }

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "SEND_SMS",
            "RECEIVE_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "CAMERA",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(manifest.contains(forbiddenPermission))
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }


    @Test
    fun doorstepVisitorHandoffRefusalCoachUsesBoundedValueFreeInputsAndRoutesOnly() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/" +
                "DoorstepVisitorHandoffRefusalCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val safetyPlanStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.doorstepVisitorHandoffRefusalCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.doorstepVisitorHandoffRefusalCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openDoorstepVisitorHandoffRefusalAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareDoorstepVisitorHandoffRefusalCoach"
        )
        val normalizedActivitySource = activitySource.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertTrue(
            normalizedActivitySource.contains(
                "Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH -> " +
                    "doorstepVisitorHandoffRefusalCoachScreen()"
            )
        )

        val entryMarkers = listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.readinessSection()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.cashWithdrawalCourierHandoffPauseCoachScreen",
            "private fun LinearLayout.bankAtmStaffDisclosureScriptCoachScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.officialPressureRealityCheckCoachScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.recoveryPromiseRealityCheckCoachScreen",
            "private fun LinearLayout.linkAppInstallQuarantineCoachScreen",
            "private fun LinearLayout.giftCardCodePauseCoachScreen",
            "private fun LinearLayout.credentialSecretFreezeCoachScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.exposureImpactTriageScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.newNumberQuarantineCoachScreen",
            "private fun LinearLayout.voiceImpersonationRealityCheckCoachScreen",
            "private fun LinearLayout.evidenceHandoffPrivacyCoachScreen",
            "private fun LinearLayout.evidencePreservationScreen",
            "private fun LinearLayout.incidentTimelineBriefingScreen",
            "private fun LinearLayout.helpDirectoryScreen",
            "private fun LinearLayout.officialReportPrepScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.privacyCenterScreen"
        )
        assertEquals(33, entryMarkers.size)
        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.map { marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "doorstep entry must have one show-only button in $marker",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(
            35,
            Regex("show\\(Screen\\.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH\\)")
                .findAll(activitySource)
                .count()
        )
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "Uri.parse",
            "startActivity("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(entryRouteSource.contains(forbiddenEntrySideEffect))
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(
            Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource)
        )
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)
        assertEquals(2, Regex("\\bincidentCount\\b").findAll(modelSource).count())
        assertFalse(Regex("analysis\\s*\\.\\s*[A-Za-z]").containsMatchIn(modelSource))

        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "analysis.redactedClue",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "SafetyPlanStore",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "officialNumber",
            "visitorName",
            "staffName",
            "courierName",
            "helperName",
            "lawyerName",
            "investigatorName",
            "homeAddress",
            "buildingValue",
            "unitValue",
            "lobbyValue",
            "doorValue",
            "intercomValue",
            "doorbellValue",
            "locationValue",
            "addressValue",
            "routeValue",
            "meetingPoint",
            "courierId",
            "cashAmount",
            "cashSerial",
            "accountNumber",
            "cardNumber",
            "walletValue",
            "transactionValue",
            "paymentValue",
            "otpValue",
            "pinValue",
            "passwordValue",
            "securityAnswer",
            "residentId",
            "deviceId",
            "callerId",
            "phoneNumber",
            "urlValue",
            "domainValue",
            "qrValue",
            "barcodeValue",
            "codeValue",
            "packageValue",
            "appValue",
            "fileValue",
            "mediaValue",
            "uriValue",
            "caseId",
            "reportStatus",
            "submissionStatus",
            "receiptId",
            "callState",
            "smsState",
            "notificationState",
            "handoffStatus",
            "deliveryStatus",
            "collectionStatus",
            "depositStatus",
            "scheduleStatus",
            "paymentStatus",
            "refundStatus",
            "chargebackStatus",
            "freezeStatus",
            "cancelStatus",
            "blockStatus",
            "traceStatus",
            "recoveryResult",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "doorstep model used forbidden value/source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource, ignoreCase = true)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource))
        }

        val builderCallPattern = Regex(
            "DoorstepVisitorHandoffRefusalCoachBuilder\\.build\\(\\s*" +
                "analysis\\s*=\\s*currentAnalysis\\s*,\\s*" +
                "readiness\\s*=\\s*FamilyReadinessBuilder\\.build\\(" +
                "safetyPlan\\.load\\(\\)\\)\\s*,\\s*" +
                "incidentCount\\s*=\\s*incidents\\.count\\(\\)\\s*\\)"
        )
        assertEquals(1, builderCallPattern.findAll(coachScreen).count())
        assertEquals(
            1,
            Regex("DoorstepVisitorHandoffRefusalCoachBuilder\\.build\\(")
                .findAll(activitySource)
                .count()
        )
        assertEquals(1, Regex("safetyPlan\\.load\\(\\)").findAll(coachScreen).count())
        assertEquals(1, Regex("incidents\\.count\\(\\)").findAll(coachScreen).count())
        assertFalse(coachScreen.contains("incidents.load("))
        assertFalse(
            coachScreen
                .replace("FamilyReadinessBuilder.build(safetyPlan.load())", "")
                .contains("safetyPlan.")
        )
        assertFalse(coachScreen.replace("incidents.count()", "").contains("incidents."))

        val incidentCountHelper = functionBlock(incidentStoreSource, "fun count()")
        assertTrue(incidentCountHelper.contains("lineSequence()"))
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
            assertFalse(incidentCountHelper.contains(forbiddenIncidentRead))
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareDoorstepVisitorHandoffRefusalCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue("doorstep UI omitted $renderedField", coachScreen.contains(renderedField))
        }
        listOf(
            "card.displayOrder",
            "card.title",
            "card.doorstepHandoffPressureSituation",
            "card.valueFreeRefusalScript",
            "card.whyItMatters",
            "card.doNotOpenLetInHandoffSayShareAttachOrRecordBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedCardField ->
            assertTrue(cardRenderer.contains(renderedCardField))
        }

        val statelessUiSource = coachScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "EditText(",
            "pendingSharedText",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "visitorName",
            "staffName",
            "courierName",
            "homeAddress",
            "locationValue",
            "addressValue",
            "routeValue",
            "cashAmount",
            "accountNumber",
            "cardNumber",
            "otpValue",
            "pinValue",
            "passwordValue",
            "deviceId",
            "caseId",
            "reportStatus",
            "handoffStatus",
            "deliveryStatus",
            "collectionStatus",
            "paymentStatus",
            "recoveryResult"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "doorstep UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }

        val expectedActionRoutes = linkedMapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "SAFE_PASTE_PRIVACY_COACH" to "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "FAMILY_SAFE_WORD_RESET_COACH" to "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "VOICE_IMPERSONATION_REALITY_CHECK_COACH" to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            "NEW_NUMBER_QUARANTINE_COACH" to "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            "CREDENTIAL_SECRET_FREEZE_COACH" to
                "show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH" to
                "show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)",
            "BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH" to
                "show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "GIFT_CARD_CODE_PAUSE_COACH" to "show(Screen.GIFT_CARD_CODE_PAUSE_COACH)",
            "LINK_APP_INSTALL_QUARANTINE_COACH" to
                "show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)",
            "OFFICIAL_PRESSURE_REALITY_CHECK_COACH" to
                "show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)",
            "RECOVERY_PROMISE_REALITY_CHECK_COACH" to
                "show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "INCIDENT_TIMELINE_BRIEFING" to "show(Screen.TIMELINE_BRIEFING)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(39, expectedActionRoutes.size)
        val actionIdBlock = functionBlock(
            modelSource,
            "enum class DoorstepVisitorHandoffRefusalActionId"
        )
        val declaredActionIds = Regex("(?m)^\\s*([A-Z][A-Z_]+),?\\s*$")
            .findAll(actionIdBlock)
            .map { match -> match.groupValues[1] }
            .toSet()
        assertEquals(expectedActionRoutes.keys, declaredActionIds)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                normalizedActionRouter.contains(
                    "DoorstepVisitorHandoffRefusalActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            39,
            Regex("DoorstepVisitorHandoffRefusalActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(actionRouter).count())
        assertFalse((coachScreen + cardRenderer + modelSource).contains("openHelpDirectory("))

        val shareArguments = Regex(
            "shareDoorstepVisitorHandoffRefusalCoach\\(([^)]*)\\)"
        )
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertEquals(1, Regex("Intent\\.ACTION_SEND").findAll(shareHelper).count())
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ScamAnalysis",
            "currentAnalysis",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "CredentialManager",
            "AccountManager",
            "NotificationManager",
            "PackageManager",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW"
        ).forEach { forbiddenShareSource ->
            assertFalse(shareHelper.contains(forbiddenShareSource, ignoreCase = true))
        }

        val boundedImplementationSource = coachScreen + cardRenderer + actionRouter + modelSource
        listOf(
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "WebView",
            "Intent.ACTION_VIEW",
            "Geocoder",
            "android.location.Address",
            "LocationManager",
            "FusedLocationProvider",
            "GoogleMap",
            "MapView",
            "NavigationManager",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PhoneNumberUtils",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "ACTION_SENDTO",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "CallLog",
            "SmsManager",
            "ContactsContract",
            "NotificationManager",
            "NotificationListenerService",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "BarcodeScanner",
            "QRCodeReader",
            "TextRecognition",
            "ImageAnalysis",
            "ImageCapture",
            "CameraManager",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "PackageInstaller",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "BillingClient",
            "NfcAdapter",
            "PaymentClient",
            "BankingClient",
            "CardClient",
            "AtmClient",
            "CashManager",
            "StaffIdentity",
            "VisitorIdentity",
            "IdentityVerifier",
            "DocumentValidator",
            "CaseLookup",
            "ReportSubmission",
            "CourierClient",
            "CourierTracker",
            "ParcelTracker",
            "DoorbellManager",
            "IntercomManager",
            "HomeAutomation",
            "MeetingScheduler",
            "EmergencyDispatcher",
            "RefundClient",
            "ChargebackClient",
            "GiftCardClient",
            "AlarmManager",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "ForegroundService",
            "BroadcastReceiver",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear()",
            "incidents.add(",
            "incidents.clear(",
            "incidents.load("
        ).forEach { forbiddenSliceApi ->
            assertFalse(
                "doorstep slice used forbidden API/source: $forbiddenSliceApi",
                boundedImplementationSource.contains(forbiddenSliceApi, ignoreCase = true)
            )
        }

        val persistenceSources = incidentStoreSource + safetyPlanStoreSource
        listOf(
            "DoorstepVisitorHandoffRefusal",
            "doorstep_visitor",
            "visitor_identity_status",
            "door_status",
            "intercom_status",
            "doorbell_status",
            "building_status",
            "home_status",
            "handoff_status",
            "delivery_status",
            "collection_status",
            "meeting_status"
        ).forEach { forbiddenStoredState ->
            assertFalse(persistenceSources.contains(forbiddenStoredState, ignoreCase = true))
        }

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "SEND_SMS",
            "RECEIVE_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "CAMERA",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(manifest.contains(forbiddenPermission))
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun travelRouteMeetingPointPauseCoachUsesBoundedValueFreeInputsAndRoutesOnly() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/" +
                "TravelRouteMeetingPointPauseCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val safetyPlanStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.travelRouteMeetingPointPauseCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.travelRouteMeetingPointPauseCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openTravelRouteMeetingPointPauseAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareTravelRouteMeetingPointPauseCoach"
        )
        val normalizedActivitySource = activitySource.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertTrue(
            normalizedActivitySource.contains(
                "Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH -> " +
                    "travelRouteMeetingPointPauseCoachScreen()"
            )
        )

        val entryMarkers = listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.readinessSection()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.cashWithdrawalCourierHandoffPauseCoachScreen",
            "private fun LinearLayout.bankAtmStaffDisclosureScriptCoachScreen",
            "private fun LinearLayout.doorstepVisitorHandoffRefusalCoachScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.officialPressureRealityCheckCoachScreen",
            "private fun LinearLayout.activeContactExitScriptsScreen",
            "private fun LinearLayout.recoveryPromiseRealityCheckCoachScreen",
            "private fun LinearLayout.linkAppInstallQuarantineCoachScreen",
            "private fun LinearLayout.giftCardCodePauseCoachScreen",
            "private fun LinearLayout.credentialSecretFreezeCoachScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.exposureImpactTriageScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.newNumberQuarantineCoachScreen",
            "private fun LinearLayout.voiceImpersonationRealityCheckCoachScreen",
            "private fun LinearLayout.evidenceHandoffPrivacyCoachScreen",
            "private fun LinearLayout.evidencePreservationScreen",
            "private fun LinearLayout.incidentTimelineBriefingScreen",
            "private fun LinearLayout.helpDirectoryScreen",
            "private fun LinearLayout.officialReportPrepScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.privacyCenterScreen"
        )
        assertEquals(34, entryMarkers.size)
        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.map { marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "travel-route entry must have one show-only button in $marker",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(
            37,
            Regex("show\\(Screen\\.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH\\)")
                .findAll(activitySource)
                .count()
        )
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "Uri.parse",
            "startActivity("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(entryRouteSource.contains(forbiddenEntrySideEffect))
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(
            Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource)
        )
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)
        assertEquals(2, Regex("\\bincidentCount\\b").findAll(modelSource).count())
        assertFalse(Regex("analysis\\s*\\.\\s*[A-Za-z]").containsMatchIn(modelSource))

        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "analysis.redactedClue",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "SafetyPlanStore",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "officialNumber",
            "homeAddress",
            "workAddress",
            "buildingValue",
            "unitValue",
            "lobbyValue",
            "parkingValue",
            "elevatorValue",
            "intercomValue",
            "doorValue",
            "lockerValue",
            "storeValue",
            "stationValue",
            "currentLocation",
            "locationValue",
            "addressValue",
            "routeValue",
            "destinationValue",
            "landmarkValue",
            "vehicleValue",
            "taxiValue",
            "busValue",
            "subwayValue",
            "rideValue",
            "fareValue",
            "arrivalValue",
            "departureValue",
            "etaValue",
            "visitorName",
            "staffName",
            "courierName",
            "driverName",
            "helperName",
            "lawyerName",
            "investigatorName",
            "courierId",
            "cashAmount",
            "cashSerial",
            "accountNumber",
            "cardNumber",
            "walletValue",
            "transactionValue",
            "paymentValue",
            "otpValue",
            "pinValue",
            "passwordValue",
            "securityAnswer",
            "residentId",
            "deviceId",
            "callerId",
            "phoneNumber",
            "urlValue",
            "domainValue",
            "qrValue",
            "barcodeValue",
            "codeValue",
            "packageValue",
            "appValue",
            "fileValue",
            "mediaValue",
            "uriValue",
            "caseId",
            "reportStatus",
            "submissionStatus",
            "receiptId",
            "callState",
            "smsState",
            "contactState",
            "notificationState",
            "arrivedStatus",
            "departedStatus",
            "boardedStatus",
            "routedStatus",
            "scheduledStatus",
            "meetingStatus",
            "handoffStatus",
            "deliveryStatus",
            "collectionStatus",
            "depositStatus",
            "paymentStatus",
            "refundStatus",
            "chargebackStatus",
            "freezeStatus",
            "cancelStatus",
            "blockStatus",
            "traceStatus",
            "recoveryResult",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "travel-route model used forbidden value/source: $forbiddenModelSource",
                modelSource.contains(forbiddenModelSource, ignoreCase = true)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource))
        }

        val builderCallPattern = Regex(
            "TravelRouteMeetingPointPauseCoachBuilder\\.build\\(\\s*" +
                "analysis\\s*=\\s*currentAnalysis\\s*,\\s*" +
                "readiness\\s*=\\s*FamilyReadinessBuilder\\.build\\(" +
                "safetyPlan\\.load\\(\\)\\)\\s*,\\s*" +
                "incidentCount\\s*=\\s*incidents\\.count\\(\\)\\s*\\)"
        )
        assertEquals(1, builderCallPattern.findAll(coachScreen).count())
        assertEquals(
            1,
            Regex("TravelRouteMeetingPointPauseCoachBuilder\\.build\\(")
                .findAll(activitySource)
                .count()
        )
        assertEquals(1, Regex("safetyPlan\\.load\\(\\)").findAll(coachScreen).count())
        assertEquals(1, Regex("incidents\\.count\\(\\)").findAll(coachScreen).count())
        assertFalse(coachScreen.contains("incidents.load("))
        assertFalse(
            coachScreen
                .replace("FamilyReadinessBuilder.build(safetyPlan.load())", "")
                .contains("safetyPlan.")
        )
        assertFalse(coachScreen.replace("incidents.count()", "").contains("incidents."))

        val incidentCountHelper = functionBlock(incidentStoreSource, "fun count()")
        assertTrue(incidentCountHelper.contains("lineSequence()"))
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
            assertFalse(incidentCountHelper.contains(forbiddenIncidentRead))
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareTravelRouteMeetingPointPauseCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue("travel-route UI omitted $renderedField", coachScreen.contains(renderedField))
        }
        listOf(
            "card.displayOrder",
            "card.title",
            "card.movementMeetingPressureSituation",
            "card.valueFreePauseScript",
            "card.whyItMatters",
            "card.doNotTravelNavigateShareLocationMeetHandoffSayShareAttachOrRecordBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedCardField ->
            assertTrue(cardRenderer.contains(renderedCardField))
        }

        val statelessUiSource = coachScreen + cardRenderer + actionRouter
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "EditText(",
            "pendingSharedText",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "currentLocation",
            "homeAddress",
            "workAddress",
            "locationValue",
            "addressValue",
            "routeValue",
            "destinationValue",
            "meetingPointValue",
            "vehicleValue",
            "fareValue",
            "arrivalValue",
            "departureValue",
            "visitorName",
            "staffName",
            "courierName",
            "driverName",
            "cashAmount",
            "accountNumber",
            "cardNumber",
            "otpValue",
            "pinValue",
            "passwordValue",
            "deviceId",
            "caseId",
            "reportStatus",
            "routedStatus",
            "scheduledStatus",
            "arrivalStatus",
            "handoffStatus",
            "deliveryStatus",
            "collectionStatus",
            "paymentStatus",
            "recoveryResult"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "travel-route UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }

        val expectedActionRoutes = linkedMapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "SAFE_PASTE_PRIVACY_COACH" to "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "FAMILY_SAFE_WORD_RESET_COACH" to "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "VOICE_IMPERSONATION_REALITY_CHECK_COACH" to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            "NEW_NUMBER_QUARANTINE_COACH" to "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            "CREDENTIAL_SECRET_FREEZE_COACH" to
                "show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH" to
                "show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)",
            "BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH" to
                "show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)",
            "DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH" to
                "show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "GIFT_CARD_CODE_PAUSE_COACH" to "show(Screen.GIFT_CARD_CODE_PAUSE_COACH)",
            "LINK_APP_INSTALL_QUARANTINE_COACH" to
                "show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)",
            "OFFICIAL_PRESSURE_REALITY_CHECK_COACH" to
                "show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)",
            "RECOVERY_PROMISE_REALITY_CHECK_COACH" to
                "show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "INCIDENT_TIMELINE_BRIEFING" to "show(Screen.TIMELINE_BRIEFING)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(40, expectedActionRoutes.size)
        val actionIdBlock = functionBlock(
            modelSource,
            "enum class TravelRouteMeetingPointPauseActionId"
        )
        val declaredActionIds = Regex("(?m)^\\s*([A-Z][A-Z_]+),?\\s*$")
            .findAll(actionIdBlock)
            .map { match -> match.groupValues[1] }
            .toSet()
        assertEquals(expectedActionRoutes.keys, declaredActionIds)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                normalizedActionRouter.contains(
                    "TravelRouteMeetingPointPauseActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            40,
            Regex("TravelRouteMeetingPointPauseActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(actionRouter).count())
        assertFalse((coachScreen + cardRenderer + modelSource).contains("openHelpDirectory("))

        val shareArguments = Regex(
            "shareTravelRouteMeetingPointPauseCoach\\(([^)]*)\\)"
        )
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertEquals(1, Regex("Intent\\.ACTION_SEND").findAll(shareHelper).count())
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ScamAnalysis",
            "currentAnalysis",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "CredentialManager",
            "AccountManager",
            "NotificationManager",
            "PackageManager",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW"
        ).forEach { forbiddenShareSource ->
            assertFalse(shareHelper.contains(forbiddenShareSource, ignoreCase = true))
        }

        val boundedImplementationSource = coachScreen + cardRenderer + actionRouter + modelSource
        listOf(
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "WebView",
            "Intent.ACTION_VIEW",
            "Geocoder",
            "android.location.Address",
            "LocationManager",
            "LocationServices",
            "FusedLocationProvider",
            "PlacesClient",
            "GoogleMap",
            "MapView",
            "NavigationManager",
            "DirectionsClient",
            "DirectionsApi",
            "RoutePlanner",
            "TrafficClient",
            "TransitClient",
            "TaxiClient",
            "RideHailingClient",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "PhoneNumberUtils",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "ACTION_SENDTO",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "CallLog",
            "SmsManager",
            "ContactsContract",
            "NotificationManager",
            "NotificationListenerService",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "BarcodeScanner",
            "QRCodeReader",
            "TextRecognition",
            "ImageAnalysis",
            "ImageCapture",
            "CameraManager",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "PackageInstaller",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "BillingClient",
            "NfcAdapter",
            "PaymentClient",
            "BankingClient",
            "CardClient",
            "AtmClient",
            "CashManager",
            "StaffIdentity",
            "VisitorIdentity",
            "DriverIdentity",
            "IdentityVerifier",
            "DocumentValidator",
            "CaseLookup",
            "ReportSubmission",
            "CourierClient",
            "CourierTracker",
            "ParcelTracker",
            "LockerTracker",
            "MeetingScheduler",
            "EmergencyDispatcher",
            "RefundClient",
            "ChargebackClient",
            "GiftCardClient",
            "AlarmManager",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "ForegroundService",
            "BroadcastReceiver",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear()",
            "incidents.add(",
            "incidents.clear(",
            "incidents.load("
        ).forEach { forbiddenSliceApi ->
            assertFalse(
                "travel-route slice used forbidden API/source: $forbiddenSliceApi",
                boundedImplementationSource.contains(forbiddenSliceApi, ignoreCase = true)
            )
        }

        val persistenceSources = incidentStoreSource + safetyPlanStoreSource
        listOf(
            "TravelRouteMeetingPointPause",
            "travel_route",
            "current_location",
            "destination_status",
            "route_status",
            "meeting_status",
            "arrival_status",
            "departure_status",
            "boarded_status",
            "routed_status",
            "scheduled_status",
            "vehicle_status",
            "fare_status",
            "courier_status",
            "handoff_status"
        ).forEach { forbiddenStoredState ->
            assertFalse(persistenceSources.contains(forbiddenStoredState, ignoreCase = true))
        }

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "SEND_SMS",
            "RECEIVE_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "CAMERA",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(manifest.contains(forbiddenPermission))
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun simNumberPortOutPauseCoachUsesBoundedValueFreeInputsAndRoutesOnly() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SimNumberPortOutPauseCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val safetyPlanStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.simNumberPortOutPauseCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.simNumberPortOutPauseCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openSimNumberPortOutPauseAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareSimNumberPortOutPauseCoach"
        )
        val normalizedActivitySource = activitySource.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertTrue(
            normalizedActivitySource.contains(
                "Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH -> " +
                    "simNumberPortOutPauseCoachScreen()"
            )
        )

        val entryMarkers = listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.readinessSection()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.credentialSecretFreezeCoachScreen",
            "private fun LinearLayout.newNumberQuarantineCoachScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.familySafeWordResetCoachScreen",
            "private fun LinearLayout.familyCallbackCardScreen",
            "private fun LinearLayout.safeVerificationCallScriptsScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.linkAppInstallQuarantineCoachScreen",
            "private fun LinearLayout.officialPressureRealityCheckCoachScreen",
            "private fun LinearLayout.recoveryPromiseRealityCheckCoachScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.giftCardCodePauseCoachScreen",
            "private fun LinearLayout.exposureImpactTriageScreen",
            "private fun LinearLayout.evidenceHandoffPrivacyCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.voiceImpersonationRealityCheckCoachScreen",
            "private fun LinearLayout.helpDirectoryScreen",
            "private fun LinearLayout.officialReportPrepScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.privacyCenterScreen",
            "private fun LinearLayout.travelRouteMeetingPointPauseCoachScreen"
        )
        assertEquals(32, entryMarkers.size)
        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.SIM_NUMBER_PORT_OUT_PAUSE_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.map { marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "SIM number-port entry must have one show-only button in $marker",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(
            34,
            Regex("show\\(Screen\\.SIM_NUMBER_PORT_OUT_PAUSE_COACH\\)")
                .findAll(activitySource)
                .count()
        )
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "Uri.parse",
            "startActivity("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(entryRouteSource.contains(forbiddenEntrySideEffect))
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(
            Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource)
        )
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)
        assertEquals(2, Regex("\\bincidentCount\\b").findAll(modelSource).count())
        assertFalse(Regex("analysis\\s*\\.\\s*[A-Za-z]").containsMatchIn(modelSource))

        val modelImplementationSource = stripKotlinStringLiterals(modelSource)
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "analysis.redactedClue",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "SafetyPlanStore",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "officialNumber",
            "phoneNumber",
            "newNumber",
            "oldNumber",
            "channelValue",
            "profileValue",
            "carrierName",
            "carrierStore",
            "simValue",
            "esimValue",
            "usimValue",
            "imsiValue",
            "iccidValue",
            "imeiValue",
            "serialValue",
            "deviceId",
            "accountId",
            "customerNumber",
            "portingPin",
            "otpValue",
            "pinValue",
            "passwordValue",
            "securityAnswer",
            "callerId",
            "callState",
            "smsState",
            "contactState",
            "notificationState",
            "urlValue",
            "domainValue",
            "packageValue",
            "appValue",
            "fileValue",
            "mediaValue",
            "uriValue",
            "accountNumber",
            "cardNumber",
            "bankValue",
            "walletValue",
            "transactionValue",
            "paymentValue",
            "residentId",
            "helperName",
            "lawyerName",
            "investigatorName",
            "staffName",
            "caseId",
            "reportStatus",
            "submissionStatus",
            "receiptId",
            "portedStatus",
            "reissuedStatus",
            "activatedStatus",
            "deactivatedStatus",
            "blockedStatus",
            "unblockedStatus",
            "cancelledStatus",
            "recoveredStatus",
            "frozenStatus",
            "verifiedStatus",
            "changedStatus",
            "updatedStatus",
            "savedStatus",
            "replacedStatus",
            "transferredStatus",
            "paymentStatus",
            "refundStatus",
            "chargebackStatus",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "SIM number-port model used forbidden value/source: $forbiddenModelSource",
                modelImplementationSource.contains(forbiddenModelSource, ignoreCase = true)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource))
        }

        val builderCallPattern = Regex(
            "SimNumberPortOutPauseCoachBuilder\\.build\\(\\s*" +
                "analysis\\s*=\\s*currentAnalysis\\s*,\\s*" +
                "readiness\\s*=\\s*FamilyReadinessBuilder\\.build\\(" +
                "safetyPlan\\.load\\(\\)\\)\\s*,\\s*" +
                "incidentCount\\s*=\\s*incidents\\.count\\(\\)\\s*\\)"
        )
        assertEquals(1, builderCallPattern.findAll(coachScreen).count())
        assertEquals(
            1,
            Regex("SimNumberPortOutPauseCoachBuilder\\.build\\(")
                .findAll(activitySource)
                .count()
        )
        assertEquals(1, Regex("safetyPlan\\.load\\(\\)").findAll(coachScreen).count())
        assertEquals(1, Regex("incidents\\.count\\(\\)").findAll(coachScreen).count())
        assertFalse(coachScreen.contains("incidents.load("))
        assertFalse(
            coachScreen
                .replace("FamilyReadinessBuilder.build(safetyPlan.load())", "")
                .contains("safetyPlan.")
        )
        assertFalse(coachScreen.replace("incidents.count()", "").contains("incidents."))

        val incidentCountHelper = functionBlock(incidentStoreSource, "fun count()")
        assertTrue(incidentCountHelper.contains("lineSequence()"))
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
            assertFalse(incidentCountHelper.contains(forbiddenIncidentRead))
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareSimNumberPortOutPauseCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue("SIM number-port UI omitted $renderedField", coachScreen.contains(renderedField))
        }
        listOf(
            "card.displayOrder",
            "card.title",
            "card.simNumberPortPressureSituation",
            "card.valueFreePauseScript",
            "card.whyItMatters",
            "card.doNotShareCodePortReissueCallSmsContactAccountDeviceSayShareAttachOrRecordBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedCardField ->
            assertTrue(cardRenderer.contains(renderedCardField))
        }

        val statelessUiImplementationSource = stripKotlinStringLiterals(
            coachScreen + cardRenderer + actionRouter
        )
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "EditText(",
            "pendingSharedText",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "phoneNumber",
            "carrierName",
            "carrierStore",
            "simValue",
            "subscriptionValue",
            "imeiValue",
            "iccidValue",
            "imsiValue",
            "serialValue",
            "deviceId",
            "accountId",
            "customerNumber",
            "portingPin",
            "otpValue",
            "pinValue",
            "passwordValue",
            "callState",
            "smsState",
            "contactState",
            "notificationState",
            "caseId",
            "reportStatus",
            "portedStatus",
            "reissuedStatus",
            "activatedStatus",
            "deactivatedStatus",
            "blockedStatus",
            "recoveredStatus",
            "frozenStatus",
            "paymentStatus",
            "refundStatus",
            "chargebackStatus"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "SIM number-port UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiImplementationSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }

        val expectedActionRoutes = linkedMapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "SAFE_PASTE_PRIVACY_COACH" to "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "FAMILY_SAFE_WORD_RESET_COACH" to "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "VOICE_IMPERSONATION_REALITY_CHECK_COACH" to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            "NEW_NUMBER_QUARANTINE_COACH" to "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            "CREDENTIAL_SECRET_FREEZE_COACH" to
                "show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH" to
                "show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "GIFT_CARD_CODE_PAUSE_COACH" to "show(Screen.GIFT_CARD_CODE_PAUSE_COACH)",
            "LINK_APP_INSTALL_QUARANTINE_COACH" to
                "show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)",
            "OFFICIAL_PRESSURE_REALITY_CHECK_COACH" to
                "show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)",
            "RECOVERY_PROMISE_REALITY_CHECK_COACH" to
                "show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "INCIDENT_TIMELINE_BRIEFING" to "show(Screen.TIMELINE_BRIEFING)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(38, expectedActionRoutes.size)
        val actionIdBlock = functionBlock(
            modelSource,
            "enum class SimNumberPortOutPauseActionId"
        )
        val declaredActionIds = Regex("(?m)^\\s*([A-Z][A-Z_]+),?\\s*$")
            .findAll(actionIdBlock)
            .map { match -> match.groupValues[1] }
            .toSet()
        assertEquals(expectedActionRoutes.keys, declaredActionIds)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                normalizedActionRouter.contains(
                    "SimNumberPortOutPauseActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            38,
            Regex("SimNumberPortOutPauseActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(actionRouter).count())
        assertFalse((coachScreen + cardRenderer + modelSource).contains("openHelpDirectory("))

        val shareArguments = Regex(
            "shareSimNumberPortOutPauseCoach\\(([^)]*)\\)"
        )
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertEquals(1, Regex("Intent\\.ACTION_SEND").findAll(shareHelper).count())
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ScamAnalysis",
            "currentAnalysis",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "TelephonyManager",
            "SubscriptionManager",
            "CarrierConfigManager",
            "EuiccManager",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "CredentialManager",
            "AccountManager",
            "NotificationManager",
            "PackageManager",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW"
        ).forEach { forbiddenShareSource ->
            assertFalse(shareHelper.contains(forbiddenShareSource, ignoreCase = true))
        }

        val boundedImplementationSource = stripKotlinStringLiterals(
            coachScreen + cardRenderer + actionRouter + modelSource
        )
        listOf(
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "WebView",
            "Intent.ACTION_VIEW",
            "Geocoder",
            "LocationManager",
            "FusedLocationProvider",
            "LocationServices",
            "GoogleMap",
            "MapView",
            "NavController",
            "DirectionsApi",
            "RoutePlanner",
            "NavigationClient",
            "TrafficClient",
            "TransitClient",
            "TaxiClient",
            "RideHailingClient",
            "AddressValidator",
            "CourierTrackingClient",
            "MeetingScheduler",
            "EmergencyDispatcher",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "CarrierLookup",
            "CarrierClient",
            "CarrierApi",
            "CarrierConfigManager",
            "TelephonyManager",
            "SubscriptionManager",
            "SubscriptionInfo",
            "EuiccManager",
            "TelecomManager",
            "PhoneNumberUtils",
            "PhoneStateListener",
            "CallScreeningService",
            "CallLog",
            "SmsManager",
            "ContactsContract",
            "NotificationManager",
            "NotificationListenerService",
            "getLine1Number",
            "getPhoneNumber",
            "getSimSerialNumber",
            "getSubscriberId",
            "getImei",
            "getMeid",
            "Build.getSerial",
            "Settings.Secure",
            "ANDROID_ID",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "ACTION_SENDTO",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "BarcodeScanner",
            "QRCodeReader",
            "TextRecognition",
            "ImageAnalysis",
            "ImageCapture",
            "CameraManager",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "PackageInstaller",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "BillingClient",
            "NfcAdapter",
            "PaymentClient",
            "BankingClient",
            "CardClient",
            "AtmClient",
            "CashManager",
            "WalletConnect",
            "BlockchainClient",
            "RefundClient",
            "ChargebackClient",
            "GiftCardClient",
            "IdentityVerifier",
            "DocumentValidator",
            "CaseLookup",
            "ReportSubmission",
            "AlarmManager",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "ForegroundService",
            "BroadcastReceiver",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear()",
            "incidents.add(",
            "incidents.clear(",
            "incidents.load("
        ).forEach { forbiddenSliceApi ->
            assertFalse(
                "SIM number-port slice used forbidden API/source: $forbiddenSliceApi",
                boundedImplementationSource.contains(forbiddenSliceApi, ignoreCase = true)
            )
        }

        val persistenceSources = incidentStoreSource + safetyPlanStoreSource
        listOf(
            "SimNumberPortOutPause",
            "sim_number_port",
            "sim_status",
            "esim_status",
            "usim_status",
            "subscription_status",
            "carrier_status",
            "phone_number_status",
            "port_out_status",
            "ported_status",
            "reissue_status",
            "activation_status",
            "deactivation_status",
            "imei_status",
            "iccid_status",
            "imsi_status"
        ).forEach { forbiddenStoredState ->
            assertFalse(persistenceSources.contains(forbiddenStoredState, ignoreCase = true))
        }

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "SEND_SMS",
            "RECEIVE_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "CAMERA",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(manifest.contains(forbiddenPermission))
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    @Test
    fun idDocumentSelfieCapturePauseCoachUsesBoundedValueFreeInputsAndRoutesOnly() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IdDocumentSelfieCapturePauseCoach.kt"
        )
        val incidentStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/IncidentStore.kt"
        )
        val safetyPlanStoreSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/SafetyPlanStore.kt"
        )
        val manifest = readProjectFile("app/src/main/AndroidManifest.xml")

        val coachScreen = functionBlock(
            activitySource,
            "private fun LinearLayout.idDocumentSelfieCapturePauseCoachScreen"
        )
        val cardRenderer = functionBlock(
            activitySource,
            "private fun LinearLayout.idDocumentSelfieCapturePauseCard"
        )
        val actionRouter = functionBlock(
            activitySource,
            "private fun openIdDocumentSelfieCapturePauseAction"
        )
        val shareHelper = functionBlock(
            activitySource,
            "private fun shareIdDocumentSelfieCapturePauseCoach"
        )
        val normalizedActivitySource = activitySource.replace(Regex("\\s+"), " ")
        val normalizedActionRouter = actionRouter.replace(Regex("\\s+"), " ")

        assertTrue(
            normalizedActivitySource.contains(
                "Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH -> " +
                    "idDocumentSelfieCapturePauseCoachScreen()"
            )
        )

        val entryMarkers = listOf(
            "private fun LinearLayout.home()",
            "private fun LinearLayout.readinessSection()",
            "private fun LinearLayout.result()",
            "private fun LinearLayout.quickStopModeScreen",
            "private fun LinearLayout.safePastePrivacyCoachScreen",
            "private fun LinearLayout.credentialSecretFreezeCoachScreen",
            "private fun LinearLayout.accountLockdownChecklistScreen",
            "private fun LinearLayout.exposureImpactTriageScreen",
            "private fun LinearLayout.evidenceHandoffPrivacyCoachScreen",
            "private fun LinearLayout.officialPressureRealityCheckCoachScreen",
            "private fun LinearLayout.recoveryPromiseRealityCheckCoachScreen",
            "private fun LinearLayout.linkAppInstallQuarantineCoachScreen",
            "private fun LinearLayout.remoteAccessCutoffCoachScreen",
            "private fun LinearLayout.simNumberPortOutPauseCoachScreen",
            "private fun LinearLayout.moneyTransferPauseGateScreen",
            "private fun LinearLayout.giftCardCodePauseCoachScreen",
            "private fun LinearLayout.separateChannelVerificationCoachScreen",
            "private fun LinearLayout.newNumberQuarantineCoachScreen",
            "private fun LinearLayout.voiceImpersonationRealityCheckCoachScreen",
            "private fun LinearLayout.recontactBoundaryCoachScreen",
            "private fun LinearLayout.verificationOutcomeNextStepsBoardScreen",
            "private fun LinearLayout.travelRouteMeetingPointPauseCoachScreen",
            "private fun LinearLayout.doorstepVisitorHandoffRefusalCoachScreen",
            "private fun LinearLayout.cashWithdrawalCourierHandoffPauseCoachScreen",
            "private fun LinearLayout.bankAtmStaffDisclosureScriptCoachScreen",
            "private fun LinearLayout.helpDirectoryScreen",
            "private fun LinearLayout.officialReportPrepScreen",
            "private fun LinearLayout.familyAlertMessagePackScreen",
            "private fun LinearLayout.trustedHelperReviewModeScreen",
            "private fun LinearLayout.familyResponseRoleBoardScreen",
            "private fun LinearLayout.familyCalmDebriefAgendaScreen",
            "private fun LinearLayout.followUpCheckInPlannerScreen",
            "private fun LinearLayout.safetyFlowRouterScreen",
            "private fun LinearLayout.privacyCenterScreen"
        )
        assertEquals(34, entryMarkers.size)
        val entryRoutePattern = Regex(
            "(?:primary|secondary)Button\\(\"[^\"]+\"\\)\\s*\\{\\s*" +
                "show\\(Screen\\.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH\\)\\s*}"
        )
        val entryRouteSources = entryMarkers.map { marker ->
            val entryScreen = functionBlock(activitySource, marker)
            val exactRoutes = entryRoutePattern.findAll(entryScreen).toList()
            assertEquals(
                "ID/selfie entry must have one show-only button in $marker",
                1,
                exactRoutes.size
            )
            exactRoutes.single().value
        }
        assertEquals(
            34,
            Regex("show\\(Screen\\.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH\\)")
                .findAll(activitySource)
                .count()
        )
        val entryRouteSource = entryRouteSources.joinToString("\n")
        listOf(
            "incidents.add(",
            "incidents.clear(",
            "incidents.load(",
            "prefs.edit(",
            "safetyPlan.save(",
            "Intent.ACTION_SEND",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
            "Uri.parse",
            "startActivity("
        ).forEach { forbiddenEntrySideEffect ->
            assertFalse(entryRouteSource.contains(forbiddenEntrySideEffect))
        }

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(
            Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource)
        )
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)
        assertEquals(2, Regex("\\bincidentCount\\b").findAll(modelSource).count())
        assertFalse(Regex("analysis\\s*\\.\\s*[A-Za-z]").containsMatchIn(modelSource))

        val modelImplementationSource = stripKotlinStringLiterals(modelSource)
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "analysis.redactedClue",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "SafetyPlanStore",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "idValue",
            "residentId",
            "passportValue",
            "licenseValue",
            "documentValue",
            "cardValue",
            "bankbookValue",
            "fileValue",
            "photoValue",
            "videoValue",
            "selfieValue",
            "faceValue",
            "livenessValue",
            "biometricValue",
            "galleryValue",
            "cameraValue",
            "screenshotValue",
            "ocrValue",
            "barcodeValue",
            "qrValue",
            "mediaValue",
            "uriValue",
            "pathValue",
            "phoneNumber",
            "simValue",
            "imeiValue",
            "iccidValue",
            "imsiValue",
            "serialValue",
            "deviceId",
            "accountId",
            "customerNumber",
            "urlValue",
            "domainValue",
            "packageValue",
            "appValue",
            "accountNumber",
            "bankValue",
            "walletValue",
            "transactionValue",
            "paymentValue",
            "otpValue",
            "pinValue",
            "passwordValue",
            "securityAnswer",
            "callerId",
            "callState",
            "smsState",
            "contactState",
            "notificationState",
            "recipientName",
            "helperName",
            "lawyerName",
            "investigatorName",
            "staffName",
            "caseId",
            "reportStatus",
            "submissionStatus",
            "receiptId",
            "uploadedStatus",
            "acceptedStatus",
            "verifiedStatus",
            "validatedStatus",
            "sanitizedStatus",
            "redactedStatus",
            "capturedStatus",
            "scannedStatus",
            "analyzedStatus",
            "matchedStatus",
            "livenessPassedStatus",
            "recoveredStatus",
            "frozenStatus",
            "cancelledStatus",
            "refundedStatus",
            "chargebackStatus",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            ".save("
        ).forEach { forbiddenModelSource ->
            assertFalse(
                "ID/selfie model used forbidden value/source: $forbiddenModelSource",
                modelImplementationSource.contains(forbiddenModelSource, ignoreCase = true)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource))
        }

        val builderCallPattern = Regex(
            "IdDocumentSelfieCapturePauseCoachBuilder\\.build\\(\\s*" +
                "analysis\\s*=\\s*currentAnalysis\\s*,\\s*" +
                "readiness\\s*=\\s*FamilyReadinessBuilder\\.build\\(" +
                "safetyPlan\\.load\\(\\)\\)\\s*,\\s*" +
                "incidentCount\\s*=\\s*incidents\\.count\\(\\)\\s*\\)"
        )
        assertEquals(1, builderCallPattern.findAll(coachScreen).count())
        assertEquals(
            1,
            Regex("IdDocumentSelfieCapturePauseCoachBuilder\\.build\\(")
                .findAll(activitySource)
                .count()
        )
        assertEquals(1, Regex("safetyPlan\\.load\\(\\)").findAll(coachScreen).count())
        assertEquals(1, Regex("incidents\\.count\\(\\)").findAll(coachScreen).count())
        assertFalse(coachScreen.contains("incidents.load("))
        assertFalse(
            coachScreen
                .replace("FamilyReadinessBuilder.build(safetyPlan.load())", "")
                .contains("safetyPlan.")
        )
        assertFalse(coachScreen.replace("incidents.count()", "").contains("incidents."))

        val incidentCountHelper = functionBlock(incidentStoreSource, "fun count()")
        assertTrue(incidentCountHelper.contains("lineSequence()"))
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
            assertFalse(incidentCountHelper.contains(forbiddenIncidentRead))
        }

        listOf(
            "title(coach.title)",
            "subtitle(coach.intro)",
            "coach.modeLabel",
            "coach.recommendationTitle",
            "coach.recommendationExplanation",
            "coach.recommendedCards",
            "coach.recommendedActions",
            "coach.cards",
            "coach.privacySafetyNotes",
            "shareIdDocumentSelfieCapturePauseCoach(coach.familyShareSummary)"
        ).forEach { renderedField ->
            assertTrue("ID/selfie UI omitted $renderedField", coachScreen.contains(renderedField))
        }
        listOf(
            "card.displayOrder",
            "card.title",
            "card.idDocumentSelfiePressureSituation",
            "card.valueFreePauseScript",
            "card.whyItMatters",
            "card.doNotPhotographScanUploadReadIdFaceAccountSayShareAttachOrRecordBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedCardField ->
            assertTrue(cardRenderer.contains(renderedCardField))
        }

        val statelessUiImplementationSource = stripKotlinStringLiterals(
            coachScreen + cardRenderer + actionRouter
        )
        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "IncidentPreviewRedactor",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear(",
            "EditText(",
            "pendingSharedText",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "idValue",
            "residentId",
            "passportValue",
            "licenseValue",
            "documentValue",
            "photoValue",
            "videoValue",
            "selfieValue",
            "faceValue",
            "livenessValue",
            "biometricValue",
            "galleryValue",
            "cameraValue",
            "screenshotValue",
            "ocrValue",
            "barcodeValue",
            "qrValue",
            "fileValue",
            "mediaValue",
            "uriValue",
            "pathValue",
            "phoneNumber",
            "simValue",
            "imeiValue",
            "iccidValue",
            "imsiValue",
            "deviceId",
            "accountId",
            "customerNumber",
            "callState",
            "smsState",
            "contactState",
            "notificationState",
            "caseId",
            "reportStatus",
            "uploadedStatus",
            "verifiedStatus",
            "capturedStatus",
            "scannedStatus",
            "analyzedStatus",
            "matchedStatus",
            "livenessPassedStatus",
            "recoveredStatus",
            "frozenStatus",
            "cancelledStatus",
            "refundStatus",
            "chargebackStatus"
        ).forEach { forbiddenStateOrValue ->
            assertFalse(
                "ID/selfie UI used forbidden state/value: $forbiddenStateOrValue",
                statelessUiImplementationSource.contains(forbiddenStateOrValue, ignoreCase = true)
            )
        }

        val expectedActionRoutes = linkedMapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "SAFE_PASTE_PRIVACY_COACH" to "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "FAMILY_SAFE_WORD_RESET_COACH" to "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "VOICE_IMPERSONATION_REALITY_CHECK_COACH" to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            "NEW_NUMBER_QUARANTINE_COACH" to "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            "CREDENTIAL_SECRET_FREEZE_COACH" to
                "show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH" to
                "show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)",
            "SIM_NUMBER_PORT_OUT_PAUSE_COACH" to
                "show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "GIFT_CARD_CODE_PAUSE_COACH" to "show(Screen.GIFT_CARD_CODE_PAUSE_COACH)",
            "LINK_APP_INSTALL_QUARANTINE_COACH" to
                "show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)",
            "OFFICIAL_PRESSURE_REALITY_CHECK_COACH" to
                "show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)",
            "RECOVERY_PROMISE_REALITY_CHECK_COACH" to
                "show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "INCIDENT_TIMELINE_BRIEFING" to "show(Screen.TIMELINE_BRIEFING)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(39, expectedActionRoutes.size)
        val actionIdBlock = functionBlock(
            modelSource,
            "enum class IdDocumentSelfieCapturePauseActionId"
        )
        val declaredActionIds = Regex("(?m)^\\s*([A-Z][A-Z_]+),?\\s*$")
            .findAll(actionIdBlock)
            .map { match -> match.groupValues[1] }
            .toSet()
        assertEquals(expectedActionRoutes.keys, declaredActionIds)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                normalizedActionRouter.contains(
                    "IdDocumentSelfieCapturePauseActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            39,
            Regex("IdDocumentSelfieCapturePauseActionId\\.[A-Z_]+\\s*->")
                .findAll(actionRouter)
                .count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(actionRouter).count())
        assertFalse((coachScreen + cardRenderer + modelSource).contains("openHelpDirectory("))

        val shareArguments = Regex(
            "shareIdDocumentSelfieCapturePauseCoach\\(([^)]*)\\)"
        )
            .findAll(activitySource)
            .map { match -> match.groupValues[1].trim() }
            .filterNot { argument -> argument.contains(": String") }
            .toList()
        assertEquals(listOf("coach.familyShareSummary"), shareArguments)
        assertEquals(1, Regex("Intent\\.ACTION_SEND").findAll(shareHelper).count())
        assertTrue(shareHelper.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareHelper.contains("type = \"text/plain\""))
        assertTrue(shareHelper.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertTrue(shareHelper.contains("Intent.createChooser"))
        assertEquals(1, Regex("putExtra\\(").findAll(shareHelper).count())
        listOf(
            "Intent.EXTRA_SUBJECT",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "FLAG_GRANT_READ_URI_PERMISSION",
            "FLAG_GRANT_WRITE_URI_PERMISSION",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "ScamAnalysis",
            "currentAnalysis",
            "SafetyPlan",
            "IncidentStore",
            "incidents",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "CameraManager",
            "ImageCapture",
            "ImageAnalysis",
            "TextRecognition",
            "BarcodeScanner",
            "BiometricPrompt",
            "FaceDetector",
            "TelephonyManager",
            "SubscriptionManager",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "ClipboardManager",
            "CredentialManager",
            "AccountManager",
            "NotificationManager",
            "PackageManager",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW"
        ).forEach { forbiddenShareSource ->
            assertFalse(shareHelper.contains(forbiddenShareSource, ignoreCase = true))
        }

        val boundedImplementationSource = stripKotlinStringLiterals(
            coachScreen + cardRenderer + actionRouter + modelSource
        )
        listOf(
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "WebView",
            "Intent.ACTION_VIEW",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "IdentityVerifier",
            "DocumentValidator",
            "PassportValidator",
            "LicenseValidator",
            "ResidentIdValidator",
            "LivenessDetector",
            "LivenessCheck",
            "FaceDetector",
            "FaceRecognition",
            "FaceMatcher",
            "BiometricPrompt",
            "BiometricManager",
            "FingerprintManager",
            "CameraX",
            "CameraManager",
            "CameraDevice",
            "CameraCaptureSession",
            "ImageAnalysis",
            "ImageCapture",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "TextRecognition",
            "TextRecognizer",
            "MlKit",
            "BarcodeScanner",
            "QRCodeReader",
            "ACTION_IMAGE_CAPTURE",
            "ACTION_VIDEO_CAPTURE",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "DocumentsContract",
            "ContentResolver",
            "openInputStream",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "BitmapFactory",
            "ExifInterface",
            "TelephonyManager",
            "SubscriptionManager",
            "CarrierConfigManager",
            "EuiccManager",
            "TelecomManager",
            "PhoneNumberUtils",
            "PhoneStateListener",
            "CallScreeningService",
            "CallLog",
            "SmsManager",
            "ContactsContract",
            "NotificationManager",
            "NotificationListenerService",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "ACTION_SENDTO",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "PackageInstaller",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "BillingClient",
            "NfcAdapter",
            "PaymentClient",
            "BankingClient",
            "CardClient",
            "WalletConnect",
            "BlockchainClient",
            "RefundClient",
            "ChargebackClient",
            "GiftCardClient",
            "CaseLookup",
            "ReportSubmission",
            "AlarmManager",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "ForegroundService",
            "BroadcastReceiver",
            "prefs.edit(",
            "safetyPlan.save(",
            "safetyPlan.clear()",
            "incidents.add(",
            "incidents.clear(",
            "incidents.load("
        ).forEach { forbiddenSliceApi ->
            assertFalse(
                "ID/selfie slice used forbidden API/source: $forbiddenSliceApi",
                boundedImplementationSource.contains(forbiddenSliceApi, ignoreCase = true)
            )
        }

        val persistenceSources = incidentStoreSource + safetyPlanStoreSource
        listOf(
            "IdDocumentSelfieCapturePause",
            "id_document_selfie",
            "document_status",
            "id_status",
            "passport_status",
            "license_status",
            "resident_status",
            "bankbook_status",
            "photo_status",
            "video_status",
            "selfie_status",
            "face_status",
            "liveness_status",
            "biometric_status",
            "capture_status",
            "scan_status",
            "upload_status",
            "validation_status"
        ).forEach { forbiddenStoredState ->
            assertFalse(persistenceSources.contains(forbiddenStoredState, ignoreCase = true))
        }

        listOf(
            "CALL_PHONE",
            "READ_SMS",
            "SEND_SMS",
            "RECEIVE_SMS",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "WRITE_CONTACTS",
            "READ_PHONE_STATE",
            "ANSWER_PHONE_CALLS",
            "RECORD_AUDIO",
            "CAMERA",
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "INTERNET",
            "ACCESS_FINE_LOCATION",
            "ACCESS_COARSE_LOCATION",
            "POST_NOTIFICATIONS",
            "SCHEDULE_EXACT_ALARM",
            "RECEIVE_BOOT_COMPLETED",
            "PACKAGE_USAGE_STATS",
            "QUERY_ALL_PACKAGES",
            "BIND_ACCESSIBILITY_SERVICE",
            "BIND_DEVICE_ADMIN",
            "BIND_NOTIFICATION_LISTENER_SERVICE",
            "BIND_VPN_SERVICE",
            "REQUEST_INSTALL_PACKAGES"
        ).forEach { forbiddenPermission ->
            assertFalse(manifest.contains(forbiddenPermission))
        }
        assertFalse(Regex("<uses-permission(?:-sdk-\\d+)?\\b").containsMatchIn(manifest))
        assertFalse(Regex("<(?:service|receiver)\\b").containsMatchIn(manifest))
        assertTrue(Regex("android:allowBackup\\s*=\\s*\"false\"").containsMatchIn(manifest))
    }

    private fun stripKotlinStringLiterals(source: String): String {
        val withoutTripleQuoted = Regex("\"\"\"[\\s\\S]*?\"\"\"")
            .replace(source, "\"\"")
        return Regex("\"(?:\\\\.|[^\"\\\\])*\"")
            .replace(withoutTripleQuoted, "\"\"")
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
