package com.mongmong.familyshield

import org.junit.Assert.assertTrue
import org.junit.Test

class IncidentResponseBuilderTest {
    @Test
    fun moneySentPlanCoversPaymentStopAndReporting() {
        val plan = IncidentResponseBuilder.build(IncidentSituation.MONEY_SENT)

        assertTrue(plan.steps.any { it.contains("지급정지") })
        assertTrue(plan.steps.any { it.contains("카드") })
        assertTrue(plan.emergencyContacts.any { it.number == "112" })
        assertTrue(plan.emergencyContacts.any { it.number == "182" })
        assertTrue(plan.emergencyContacts.any { it.number == "1332" })
    }

    @Test
    fun appInstalledPlanCoversUninstallAndPasswordChangeFromSafeDevice() {
        val plan = IncidentResponseBuilder.build(IncidentSituation.APP_INSTALLED)

        assertTrue(plan.steps.any { it.contains("삭제") })
        assertTrue(plan.steps.any { it.contains("비밀번호") && it.contains("안전한 기기") })
        assertTrue(plan.emergencyContacts.any { it.number == "112" })
    }

    @Test
    fun credentialsSharedPlanCoversPasswordAndOtpReset() {
        val plan = IncidentResponseBuilder.build(IncidentSituation.CREDENTIALS_SHARED)

        assertTrue(plan.steps.any { it.contains("비밀번호") })
        assertTrue(plan.steps.any { it.contains("OTP") || it.contains("보안카드") })
        assertTrue(plan.emergencyContacts.any { it.number == "1332" })
    }

    @Test
    fun linkOpenedPlanStaysCalmWhenNoPaymentOrInfoSharedYet() {
        val plan = IncidentResponseBuilder.build(IncidentSituation.LINK_OPENED)

        assertTrue(plan.steps.any { it.contains("삭제") })
        assertTrue(plan.steps.any { it.contains("비밀번호") })
        assertTrue(plan.steps.any { it.contains("182") })
    }

    @Test
    fun everySituationProducesTheSameCommonEmergencyContacts() {
        IncidentSituation.entries.forEach { situation ->
            val plan = IncidentResponseBuilder.build(situation)
            assertTrue(plan.emergencyContacts.size == 3)
            assertTrue(plan.emergencyContacts.any { it.number == "112" })
            assertTrue(plan.emergencyContacts.any { it.number == "182" })
            assertTrue(plan.emergencyContacts.any { it.number == "1332" })
        }
    }
}
