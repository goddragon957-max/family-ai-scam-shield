package com.mongmong.familyshield

data class ActionPlan(val title: String, val steps: List<String>)

object ActionPlanBuilder {
    fun build(analysis: ScamAnalysis): ActionPlan {
        val signals = analysis.matchedSignals.toSet()
        val steps = mutableListOf<String>()

        when (analysis.riskLevel) {
            RiskLevel.HIGH -> {
                steps += "송금·이체·환전을 지금 즉시 멈추세요."
                steps += "앱 설치, 화면공유, 원격제어 요청을 거절하세요."
                steps += "계좌번호, 비밀번호, 인증번호(OTP), 신분증 정보를 알려주지 마세요."
                steps += "압박이나 협박이 느껴지면 지금 통화를 끊으세요."
                steps += "끊은 뒤 기존에 저장된 가족 번호로 다시 걸어 확인하세요."
                if (ScamSignal.URGENT_TRANSFER in signals ||
                    ScamSignal.GIFT_CRYPTO in signals ||
                    ScamSignal.GIFTICON_PIN in signals ||
                    ScamSignal.SAFE_ACCOUNT_FRAUD in signals
                ) {
                    steps += "이미 송금·상품권 전달을 했다면 은행 지급정지와 112 또는 1394 신고를 바로 진행하세요."
                }
            }
            RiskLevel.MEDIUM -> {
                steps += "문자나 카카오톡 속 링크는 누르지 마세요."
                steps += "은행·기관 공식 앱이나 대표번호로 직접 확인하세요."
                steps += "계좌, 비밀번호, 개인정보 입력은 확인 전까지 보류하세요."
                steps += "여전히 의심되면 가족에게 알리고 다시 검사하세요."
            }
            RiskLevel.SAFE -> {
                steps += "지금은 강한 사기 신호가 적습니다."
                steps += "그래도 돈, 인증번호(OTP), 앱 설치를 요구하면 직접 확인 후 진행하세요."
                steps += "의심스러운 부분이 있으면 이 내용을 다시 검사해보세요."
            }
        }

        val title = when (analysis.riskLevel) {
            RiskLevel.HIGH -> "지금부터 3분: 즉시 실행 루틴"
            RiskLevel.MEDIUM -> "지금부터 3분: 확인 루틴"
            RiskLevel.SAFE -> "지금부터 3분: 안전 확인"
        }

        return ActionPlan(title, steps)
    }
}
