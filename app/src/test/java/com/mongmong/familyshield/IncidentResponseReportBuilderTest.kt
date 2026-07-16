package com.mongmong.familyshield

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IncidentResponseReportBuilderTest {
    @Test
    fun reportSummarizesSituationAndStepsWithoutRawScamText() {
        val rawScamText = "엄마 나 지금 계좌로 300만원 급히 보내줘. 이건 비밀이야."
        val plan = IncidentResponseBuilder.build(IncidentSituation.MONEY_SENT)

        val report = IncidentResponseReportBuilder.build(plan)

        assertTrue(report.contains("가족 AI 보이스피싱 방패"))
        assertTrue(report.contains(plan.situation.label))
        assertTrue(report.contains("긴급 대응"))
        assertTrue(report.contains("112"))
        assertTrue(report.contains("182"))
        assertTrue(report.contains("1332"))
        assertTrue(report.contains("원문 메시지나 대화 내용은 개인정보 보호를 위해 포함하지 않았습니다"))
        assertFalse(report.contains(rawScamText))
        assertFalse(report.contains("300만원"))
    }

    @Test
    fun reportForEverySituationExcludesArbitraryPastedText() {
        val arbitraryPastedText = "이 문장은 사용자가 붙여넣은 임의의 원문 텍스트입니다 12345"

        IncidentSituation.entries.forEach { situation ->
            val plan = IncidentResponseBuilder.build(situation)
            val report = IncidentResponseReportBuilder.build(plan)

            assertTrue(report.contains(situation.label))
            assertFalse(report.contains(arbitraryPastedText))
        }
    }
}
