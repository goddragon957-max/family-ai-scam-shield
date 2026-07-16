package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyDrillBuilderTest {
    @Test
    fun familyNumberChangeDrillPracticesSafeWordAndCallback() {
        val drill = SafetyDrillBuilder.build(DrillScenario.FAMILY_NUMBER_CHANGE)

        assertTrue(drill.fictionalScript.contains("계좌"))
        assertTrue(drill.responseSteps.any { it.contains("안전단어") })
        assertTrue(drill.responseSteps.any { it.contains("저장된") && it.contains("전화") })
        assertTrue(drill.passChecklist.any { it.contains("콜백") })
        assertTrue(drill.passChecklist.any { it.contains("송금을 멈췄다") })
    }

    @Test
    fun prosecutorSafeAccountDrillPracticesRefusalAndOfficialNumbers() {
        val drill = SafetyDrillBuilder.build(DrillScenario.PROSECUTOR_SAFE_ACCOUNT)

        assertTrue(drill.fictionalScript.contains("안전계좌"))
        assertTrue(drill.responseSteps.any { it.contains("끊습니다") })
        assertTrue(drill.responseSteps.any { it.contains("112") || it.contains("1332") })
        assertTrue(drill.passChecklist.any { it.contains("안전계좌") && it.contains("거절") })
        assertTrue(drill.passChecklist.any { it.contains("가족에게") })
    }

    @Test
    fun remoteControlDrillPracticesRefusingInstallAndScreenShare() {
        val drill = SafetyDrillBuilder.build(DrillScenario.REMOTE_CONTROL)

        assertTrue(drill.fictionalScript.contains("화면공유"))
        assertTrue(drill.responseSteps.any { it.contains("거절") })
        assertTrue(drill.responseSteps.any { it.contains("끊습니다") })
        assertTrue(drill.responseSteps.any { it.contains("앱 정보") && it.contains("안전하게 남긴") })
        assertTrue(drill.passChecklist.any { it.contains("화면공유") && it.contains("거절") })
    }

    @Test
    fun parcelSmishingDrillPracticesNotTappingLinksAndContainsNoRealLink() {
        val drill = SafetyDrillBuilder.build(DrillScenario.PARCEL_SMISHING)

        assertTrue(drill.fictionalScript.contains("택배"))
        assertFalse(drill.fictionalScript.contains("http"))
        assertTrue(drill.responseSteps.any { it.contains("링크는 누르지 않습니다") })
        assertTrue(drill.responseSteps.any { it.contains("공식") })
        assertTrue(drill.passChecklist.any { it.contains("링크를 누르지 않았다") })
    }

    @Test
    fun everyScenarioIsDeterministicAndComplete() {
        DrillScenario.entries.forEach { scenario ->
            val drill = SafetyDrillBuilder.build(scenario)

            assertEquals(scenario, drill.scenario)
            assertTrue(drill.title.isNotBlank())
            assertTrue(drill.fictionalScript.isNotBlank())
            assertTrue(drill.responseSteps.size >= 3)
            assertTrue(drill.passChecklist.size >= 3)
            assertEquals(drill, SafetyDrillBuilder.build(scenario))
        }
    }

    @Test
    fun everyScriptIsMarkedAsFictionalPractice() {
        DrillScenario.entries.forEach { scenario ->
            val drill = SafetyDrillBuilder.build(scenario)

            assertTrue(drill.fictionalScript.contains("연습용 가상"))
            assertTrue(drill.title.contains("연습"))
        }
        assertTrue(SafetyDrillBuilder.FICTION_NOTICE.contains("가상 시나리오"))
    }

    @Test
    fun drillTextNeverImpliesRealMonitoringOrSurveillance() {
        DrillScenario.entries.forEach { scenario ->
            val drill = SafetyDrillBuilder.build(scenario)
            val allText = buildString {
                append(drill.title)
                append(drill.fictionalScript)
                drill.responseSteps.forEach(::append)
                drill.passChecklist.forEach(::append)
            }

            assertFalse(allText.contains("감시"))
            assertFalse(allText.contains("몰래"))
            assertFalse(allText.contains("자동으로 읽"))
        }
    }
}
