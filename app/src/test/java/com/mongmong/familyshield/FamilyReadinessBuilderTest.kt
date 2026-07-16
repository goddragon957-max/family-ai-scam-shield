package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyReadinessBuilderTest {
    private val emptyPlan = SafetyPlan(safeWord = "", callbackScript = "", contacts = emptyList())

    @Test
    fun emptyPlanIsLowReadinessWithSetupRecommendation() {
        val readiness = FamilyReadinessBuilder.build(emptyPlan)

        assertEquals(ReadinessLevel.LOW, readiness.level)
        assertEquals("준비 부족", readiness.level.label)
        assertEquals(0, readiness.completedCount)
        assertEquals(4, readiness.totalCount)
        assertTrue(readiness.checklist.none { it.done })
        assertTrue(readiness.nextAction.contains("안전단어"))
    }

    @Test
    fun safeWordOnlyIsInProgressAndRecommendsContact() {
        val readiness = FamilyReadinessBuilder.build(
            emptyPlan.copy(safeWord = "어릴 적 별명은?")
        )

        assertEquals(ReadinessLevel.IN_PROGRESS, readiness.level)
        assertEquals(1, readiness.completedCount)
        assertTrue(readiness.checklist.first { it.label.contains("안전단어") }.done)
        assertTrue(readiness.nextAction.contains("연락처"))
    }

    @Test
    fun contactOnlyIsInProgressAndRecommendsSafeWordFirst() {
        val readiness = FamilyReadinessBuilder.build(
            emptyPlan.copy(contacts = listOf(TrustedContact("엄마", "010-1234-5678")))
        )

        assertEquals(ReadinessLevel.IN_PROGRESS, readiness.level)
        assertEquals(1, readiness.completedCount)
        assertTrue(readiness.checklist.first { it.label.contains("연락처 1명") }.done)
        assertTrue(readiness.nextAction.contains("안전단어"))
    }

    @Test
    fun contactWithoutNumberDoesNotCount() {
        val readiness = FamilyReadinessBuilder.build(
            emptyPlan.copy(contacts = listOf(TrustedContact("엄마", "")))
        )

        assertEquals(ReadinessLevel.LOW, readiness.level)
        assertFalse(readiness.checklist.first { it.label.contains("연락처 1명") }.done)
    }

    @Test
    fun completePlanWithScriptAndTwoContactsIsHighReadiness() {
        val readiness = FamilyReadinessBuilder.build(
            SafetyPlan(
                safeWord = "어릴 적 별명은?",
                callbackScript = "전화를 끊고 저장된 번호로 다시 겁니다.",
                contacts = listOf(
                    TrustedContact("엄마", "010-1234-5678"),
                    TrustedContact("아빠", "010-2345-6789")
                )
            )
        )

        assertEquals(ReadinessLevel.READY, readiness.level)
        assertEquals("준비 좋음", readiness.level.label)
        assertEquals(4, readiness.completedCount)
        assertEquals(4, readiness.totalCount)
        assertTrue(readiness.checklist.all { it.done })
        assertEquals(FamilyReadinessBuilder.READY_ACTION, readiness.nextAction)
        assertTrue(readiness.nextAction.contains("연습"))
    }

    @Test
    fun readyWithSingleContactRecommendsBackupContact() {
        val readiness = FamilyReadinessBuilder.build(
            SafetyPlan(
                safeWord = "어릴 적 별명은?",
                callbackScript = "전화를 끊고 저장된 번호로 다시 겁니다.",
                contacts = listOf(TrustedContact("엄마", "010-1234-5678"))
            )
        )

        assertEquals(ReadinessLevel.READY, readiness.level)
        assertEquals(3, readiness.completedCount)
        assertTrue(readiness.nextAction.contains("한 명 더"))
    }

    @Test
    fun checklistLabelsAndActionsAreDeterministic() {
        val readiness = FamilyReadinessBuilder.build(emptyPlan)

        assertEquals(readiness, FamilyReadinessBuilder.build(emptyPlan))
        assertEquals(
            listOf(
                "가족만 아는 안전단어(확인 질문) 저장",
                "콜백용 신뢰 연락처 1명 이상 저장",
                "위급할 때 읽을 콜백 스크립트 저장",
                "예비 연락처까지 2명 이상 저장 (선택)"
            ),
            readiness.checklist.map { it.label }
        )
        assertTrue(readiness.checklist.last().optional)
        assertTrue(readiness.checklist.all { it.action.isNotBlank() })
        assertTrue(readiness.title.contains("가족 준비도"))
    }

    @Test
    fun readinessTextNeverImpliesMonitoringOrNetwork() {
        val plans = listOf(
            emptyPlan,
            emptyPlan.copy(safeWord = "별명"),
            SafetyPlan(
                safeWord = "별명",
                callbackScript = "콜백",
                contacts = listOf(
                    TrustedContact("엄마", "010-1234-5678"),
                    TrustedContact("아빠", "010-2345-6789")
                )
            )
        )
        plans.forEach { plan ->
            val readiness = FamilyReadinessBuilder.build(plan)
            val allText = buildString {
                append(readiness.title)
                append(readiness.nextAction)
                readiness.checklist.forEach {
                    append(it.label)
                    append(it.action)
                }
            }

            assertFalse(allText.contains("감시"))
            assertFalse(allText.contains("몰래"))
            assertFalse(allText.contains("서버"))
        }
    }
}
