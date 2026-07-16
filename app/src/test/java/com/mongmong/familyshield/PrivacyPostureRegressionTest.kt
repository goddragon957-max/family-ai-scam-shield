package com.mongmong.familyshield

import java.io.File
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
