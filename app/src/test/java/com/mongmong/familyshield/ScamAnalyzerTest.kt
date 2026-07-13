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

    @Test
    fun phoneNumberChangeImpersonationIsFlagged() {
        val result = ScamAnalyzer.analyze(
            "엄마 나 번호 바뀌었어. 이 번호로 저장하고 지금 바로 50만원만 입금해줘."
        )

        assertTrue(ScamSignal.PHONE_NUMBER_CHANGE in result.matchedSignals)
        assertTrue(ScamSignal.URGENT_TRANSFER in result.matchedSignals)
        assertTrue(result.score >= 30)
    }

    @Test
    fun loanRefinanceFeeBaitIsFlagged() {
        val result = ScamAnalyzer.analyze(
            "대환대출로 저금리 전환 가능합니다. 진행을 위해 보증료 30만원을 먼저 입금해주세요."
        )

        assertTrue(ScamSignal.LOAN_REFINANCE_FEE in result.matchedSignals)
        assertTrue(result.score >= 30)
    }

    @Test
    fun gifticonPinRequestWithSecrecyIsFlagged() {
        val result = ScamAnalyzer.analyze(
            "급하게 기프티콘 3장 사서 뒷면 핀번호 사진 찍어 보내줘. 이건 비밀로 해줘."
        )

        assertTrue(ScamSignal.GIFTICON_PIN in result.matchedSignals)
        assertTrue(ScamSignal.SECRECY in result.matchedSignals)
        assertTrue(result.score >= 30)
    }

    @Test
    fun deepfakeVoiceFamilyEmergencyIsHighRisk() {
        val result = ScamAnalyzer.analyze(
            "이 전화는 AI 목소리 복제로 만든 가짜일 수 있어 조심해. 근데 진짜 딸이야, 지금 바로 계좌로 돈 좀 보내줘."
        )

        assertTrue(ScamSignal.DEEPFAKE_VOICE in result.matchedSignals)
        assertEquals(RiskLevel.HIGH, result.riskLevel)
    }
}
