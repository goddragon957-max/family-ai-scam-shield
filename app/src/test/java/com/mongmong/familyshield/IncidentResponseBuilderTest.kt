package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IncidentResponseBuilderTest {
    @Test
    fun moneySentPlanCoversPaymentStopAndReporting() {
        val plan = IncidentResponseBuilder.build(IncidentSituation.MONEY_SENT)

        assertTrue(plan.steps.any { it.contains("지급정지") })
        assertTrue(plan.steps.any { it.contains("카드") })
        assertTrue(plan.emergencyContacts.any { it.number == "112" })
        assertTrue(plan.emergencyContacts.any { it.number == "1394" })
        assertTrue(plan.emergencyContacts.any { it.number == "1332" })
    }

    @Test
    fun appInstalledPlanCoversUninstallAndPasswordChangeFromSafeDevice() {
        val plan = IncidentResponseBuilder.build(IncidentSituation.APP_INSTALLED)
        val preserveAndDeleteStep = plan.steps.first { it.contains("삭제") }

        assertTrue(plan.steps.any { it.contains("삭제") })
        assertTrue(plan.steps.first().contains("연결을 끊"))
        assertTrue(preserveAndDeleteStep.contains("캡처"))
        assertTrue(preserveAndDeleteStep.indexOf("캡처") < preserveAndDeleteStep.indexOf("삭제"))
        assertTrue(plan.steps.any { it.contains("비밀번호") && it.contains("안전한 기기") })
        assertTrue(plan.emergencyContacts.any { it.number == "112" })
    }

    @Test
    fun credentialsSharedPlanCoversPasswordAndOtpReset() {
        val plan = IncidentResponseBuilder.build(IncidentSituation.CREDENTIALS_SHARED)

        assertTrue(plan.steps.any { it.contains("비밀번호") && it.contains("다른 안전한 기기") })
        assertTrue(plan.steps.any { it.contains("OTP") || it.contains("보안카드") })
        assertTrue(plan.emergencyContacts.any { it.number == "1332" })
    }

    @Test
    fun linkOpenedPlanStaysCalmWhenNoPaymentOrInfoSharedYet() {
        val plan = IncidentResponseBuilder.build(IncidentSituation.LINK_OPENED)
        val preserveAndDeleteStep = plan.steps.first { it.contains("삭제") }

        assertTrue(plan.steps.any { it.contains("삭제") })
        assertTrue(preserveAndDeleteStep.contains("먼저") && preserveAndDeleteStep.contains("연결을 끊"))
        assertTrue(preserveAndDeleteStep.contains("캡처"))
        assertTrue(preserveAndDeleteStep.indexOf("캡처") < preserveAndDeleteStep.indexOf("삭제"))
        assertTrue(plan.steps.any { it.contains("비밀번호") && it.contains("다른 안전한 기기") })
        assertTrue(plan.steps.any { it.contains("1394") })
    }

    @Test
    fun everySituationProducesTheSameCommonEmergencyContacts() {
        val expectedContacts = OfficialHelpDirectoryBuilder.build().entries.mapNotNull { entry ->
            entry.dialNumber?.let { number -> EmergencyContact(entry.label, number) }
        }

        IncidentSituation.entries.forEach { situation ->
            val plan = IncidentResponseBuilder.build(situation)
            assertEquals(expectedContacts, plan.emergencyContacts)
            assertTrue(plan.emergencyContacts.size == 3)
            assertTrue(plan.emergencyContacts.any { it.number == "112" })
            assertTrue(plan.emergencyContacts.any { it.number == "1394" })
            assertTrue(plan.emergencyContacts.any { it.number == "1332" })
        }
    }
}
