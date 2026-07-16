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

    @Test
    fun liveCallScreenSharePressureIsFlagged() {
        val result = ScamAnalyzer.analyze(
            "은행 보안팀입니다. 통화 끊지 말고 화면공유 켠 뒤 보안앱 APK를 설치하고 인증번호를 불러주세요."
        )

        assertTrue(ScamSignal.LIVE_CALL_CONTROL in result.matchedSignals)
        assertTrue(ScamSignal.APP_INSTALL in result.matchedSignals)
        assertTrue(ScamSignal.CREDENTIALS in result.matchedSignals)
        assertEquals(RiskLevel.HIGH, result.riskLevel)
    }

    @Test
    fun safeAccountAuthorityFraudIsFlagged() {
        val result = ScamAnalyzer.analyze(
            "검찰청입니다. 명의도용으로 대포통장이 개설되어 자금세탁 연루 조사 중이니 안전계좌로 이체하세요."
        )

        assertTrue(ScamSignal.SAFE_ACCOUNT_FRAUD in result.matchedSignals)
        assertTrue(ScamSignal.IMPERSONATION in result.matchedSignals)
        assertTrue(result.score >= 30)
    }

    @Test
    fun parcelInvitationSmishingIsFlagged() {
        val result = ScamAnalyzer.analyze(
            "택배 배송이 지연되어 주소 확인이 필요합니다. 아래 링크에서 확인해주세요. https://short.example/box"
        )

        assertTrue(ScamSignal.PARCEL_INVITATION_SMISHING in result.matchedSignals)
        assertTrue(ScamSignal.LINK in result.matchedSignals)
    }

    @Test
    fun overseasPaymentAlertPanicBaitIsFlagged() {
        val result = ScamAnalyzer.analyze(
            "해외 결제 승인 완료 안내입니다. 본인이 아니라면 아래 인증번호를 알려주세요."
        )

        assertTrue(ScamSignal.OVERSEAS_PAYMENT_ALERT in result.matchedSignals)
        assertTrue(ScamSignal.CREDENTIALS in result.matchedSignals)
        assertTrue(result.score >= 30)
    }

    @Test
    fun guardianAdviceTellsFamilyToStopAndCallBack() {
        val result = ScamAnalyzer.analyze(
            "엄마 나 번호 바뀌었어. 이 번호로 저장해. 급히 계좌로 돈 보내줘. 아무한테도 말하지 마."
        )

        assertTrue(result.guardianAdvice.verdict.contains("지금 멈춤"))
        assertTrue(result.guardianAdvice.immediateActions.any { it.contains("기존에 저장된 가족 번호") })
        assertTrue(result.guardianAdvice.immediateActions.any { it.contains("송금") })
    }
}
