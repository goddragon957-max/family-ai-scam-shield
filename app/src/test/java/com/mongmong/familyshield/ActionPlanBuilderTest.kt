package com.mongmong.familyshield

import org.junit.Assert.assertTrue
import org.junit.Test

class ActionPlanBuilderTest {
    @Test
    fun highRiskPlanTellsUserToStopTransferAndCallBack() {
        val analysis = ScamAnalyzer.analyze(
            "엄마 나 휴대폰 고장났어. 지금 바로 이 계좌로 300만원 송금해줘. 누구에게도 말하지 마."
        )

        val plan = analysis.actionPlan

        assertTrue(plan.steps.any { it.contains("송금") && it.contains("멈추") })
        assertTrue(plan.steps.any { it.contains("앱 설치") })
        assertTrue(plan.steps.any { it.contains("인증번호") })
        assertTrue(plan.steps.any { it.contains("가족 번호") })
        assertTrue(plan.steps.any { it.contains("지급정지") })
    }

    @Test
    fun mediumRiskPlanFocusesOnOfficialVerification() {
        val analysis = ScamAnalyzer.analyze(
            "정부 지원금 환급 대상입니다. 아래 링크를 클릭해 신청 정보를 확인하세요. https://short.example/app"
        )

        val plan = analysis.actionPlan

        assertTrue(plan.steps.any { it.contains("링크") && it.contains("누르지") })
        assertTrue(plan.steps.any { it.contains("공식") })
    }

    @Test
    fun safeMessagePlanStillWarnsAboutMoneyAndOtpRequests() {
        val analysis = ScamAnalyzer.analyze("오늘 저녁 7시에 집에서 밥 먹자. 오는 길에 우유 하나만 사와.")

        val plan = analysis.actionPlan

        assertTrue(plan.steps.any { it.contains("인증번호") && it.contains("앱 설치") })
    }
}
