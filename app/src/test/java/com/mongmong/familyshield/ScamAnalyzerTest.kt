package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScamAnalyzerTest {
    @Test
    fun highRiskFamilyEmergencyTransferIsFlagged() {
        val result = ScamAnalyzer.analyze(
            "엄마 나 휴대폰 고장났어. 지금 바로 이 계좌로 300만원 송금해줘. 누구에게도 말하지 마."
        )

        assertEquals(RiskLevel.HIGH, result.riskLevel)
        assertTrue(ScamSignal.FAMILY_EMERGENCY in result.matchedSignals)
        assertTrue(ScamSignal.URGENT_TRANSFER in result.matchedSignals)
        assertTrue(ScamSignal.SECRECY in result.matchedSignals)
    }

    @Test
    fun mediumRiskRefundLinkIsFlagged() {
        val result = ScamAnalyzer.analyze(
            "정부 지원금 환급 대상입니다. 아래 링크를 클릭해 신청 정보를 확인하세요. https://short.example/app"
        )

        assertEquals(RiskLevel.MEDIUM, result.riskLevel)
        assertTrue(ScamSignal.BAIT in result.matchedSignals)
        assertTrue(ScamSignal.LINK in result.matchedSignals)
    }

    @Test
    fun ordinaryFamilyMessageStaysSafe() {
        val result = ScamAnalyzer.analyze("오늘 저녁 7시에 집에서 밥 먹자. 오는 길에 우유 하나만 사와.")

        assertEquals(RiskLevel.SAFE, result.riskLevel)
        assertTrue(result.matchedSignals.isEmpty())
        assertTrue(result.score < 30)
    }
}
