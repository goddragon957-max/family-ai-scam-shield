package com.mongmong.familyshield

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyReportBuilderTest {
    @Test
    fun highRiskReportSummarizesSignalsButDoesNotExposeOriginalMessage() {
        val original = "엄마 나 휴대폰 고장났어. 지금 바로 이 계좌로 300만원 송금해줘. 누구에게도 말하지 마."
        val analysis = ScamAnalyzer.analyze(original)

        val report = SafetyReportBuilder.build(analysis)

        assertTrue(report.contains("가족 AI 보이스피싱 방패"))
        assertTrue(report.contains("위험도: 높음"))
        assertTrue(report.contains("긴급 송금 압박"))
        assertTrue(report.contains("가족 긴급상황/목소리 사칭"))
        assertTrue(report.contains("원문은 개인정보 보호를 위해 포함하지 않았습니다"))
        assertFalse(report.contains(original))
        assertFalse(report.contains("300만원"))
    }

    @Test
    fun safeReportStillGivesCallbackGuidance() {
        val analysis = ScamAnalyzer.analyze("오늘 저녁 7시에 집에서 밥 먹자. 오는 길에 우유 하나만 사와.")

        val report = SafetyReportBuilder.build(analysis)

        assertTrue(report.contains("위험도: 낮음"))
        assertTrue(report.contains("강한 위험 신호 없음"))
        assertTrue(report.contains("직접 확인"))
    }
}
