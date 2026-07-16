package com.mongmong.familyshield

enum class IncidentSituation(val label: String) {
    MONEY_SENT("이미 돈을 보냈거나 상품권/코인을 전달했어요"),
    APP_INSTALLED("앱을 설치했거나 원격제어·화면공유를 허용했어요"),
    CREDENTIALS_SHARED("계좌·비밀번호·인증번호(OTP)·신분증 정보를 알려줬어요"),
    LINK_OPENED("링크만 눌렀고 돈이나 정보는 아직 주지 않았어요")
}

data class EmergencyContact(val label: String, val number: String)

data class IncidentResponsePlan(
    val situation: IncidentSituation,
    val title: String,
    val steps: List<String>,
    val emergencyContacts: List<EmergencyContact>
)

object IncidentResponseBuilder {
    private val commonContacts = OfficialHelpDirectoryBuilder.build().entries.mapNotNull { entry ->
        entry.dialNumber?.let { number -> EmergencyContact(entry.label, number) }
    }

    fun build(situation: IncidentSituation): IncidentResponsePlan {
        val steps = mutableListOf<String>()
        val title: String

        when (situation) {
            IncidentSituation.MONEY_SENT -> {
                title = "긴급 대응: 송금/상품권 전달 이후"
                steps += "송금한 은행 고객센터나 앱에서 지급정지를 바로 신청하세요."
                steps += "체크·신용카드로 결제했다면 카드사에 분실·도난 신고로 카드를 잠그세요."
                steps += "112(경찰) 또는 1394(보이스피싱 통합신고)에 전화해 피해 사실을 신고하세요."
                steps += "금융감독원 1332에 전화해 피해구제 절차를 안내받으세요."
                steps += "송금 내역, 계좌번호, 영수증 등 증거는 스크린샷과 원본 자료로 보관하고, 원문 대화는 가족 단톡방에 그대로 공유하지 마세요."
            }
            IncidentSituation.APP_INSTALLED -> {
                title = "긴급 대응: 앱 설치/원격제어 이후"
                steps += "지금 바로 비행기 모드로 전환하거나 와이파이·데이터를 꺼서 원격 연결을 끊으세요."
                steps += "연결이 끊긴 상태에서 앱 이름·설치 시각·권한 화면을 안전하게 캡처한 뒤 원격제어 앱이나 출처가 불분명한 앱을 삭제하세요."
                steps += "앱 삭제가 바로 되지 않더라도 다른 안전한 기기(가족 폰, PC)에서 은행 앱과 주요 계정 비밀번호를 변경하세요."
                steps += "112 또는 1394에 전화해 원격제어 피해 사실을 신고하세요."
                steps += "은행에도 연락해 계좌 모니터링 강화나 지급정지를 요청하세요."
            }
            IncidentSituation.CREDENTIALS_SHARED -> {
                title = "긴급 대응: 계좌/비밀번호/인증정보 공유 이후"
                steps += "알려준 계좌의 비밀번호와 이체 비밀번호를 다른 안전한 기기에서 즉시 변경하세요."
                steps += "은행에 전화해 계좌 지급정지 또는 이체 한도 축소를 요청하세요."
                steps += "OTP·보안카드를 재발급받고, 알려준 신분증 정보는 명의도용 방지 서비스에 등록하세요."
                steps += "112 또는 1394에 전화해 개인정보 유출 및 사기 시도를 신고하세요."
                steps += "금융감독원 1332에 전화해 계좌·카드 추가 보호 조치를 문의하세요."
            }
            IncidentSituation.LINK_OPENED -> {
                title = "긴급 대응: 링크만 열어본 경우"
                steps += "추가로 정보를 입력하거나 설치한 것이 없다면 지금 창을 닫고 앱을 종료하세요."
                steps += "본인도 모르게 설치된 앱이 있다면 먼저 비행기 모드로 연결을 끊고, 앱 이름·설치 시각·권한 화면을 안전하게 캡처한 뒤 삭제하세요. 의심 파일은 열지 마세요."
                steps += "앱 삭제가 바로 되지 않더라도 다른 안전한 기기에서 은행 앱, 이메일 등 주요 계정 비밀번호를 변경하세요."
                steps += "이후 송금이나 정보 제공 요구가 이어지면 지금 멈추고 가족 확인 루틴을 진행하세요."
                steps += "불안하면 1394(보이스피싱 통합신고)에 전화해 상담받으세요."
            }
        }

        return IncidentResponsePlan(situation, title, steps, commonContacts)
    }
}
