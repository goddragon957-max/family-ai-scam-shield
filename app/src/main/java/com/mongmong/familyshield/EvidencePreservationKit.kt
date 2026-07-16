package com.mongmong.familyshield

data class EvidencePreservationKit(
    val situation: IncidentSituation?,
    val title: String,
    val checklistItems: List<String>,
    val cautions: List<String>,
    val familyShareSummary: String,
    val officialReportPrepNotes: List<String> = emptyList()
)

object EvidencePreservationKitBuilder {
    const val GENERIC_SITUATION_LABEL = "아직 상황을 잘 모르겠어요"

    fun build(situation: IncidentSituation? = null): EvidencePreservationKit {
        val content = when (situation) {
            null -> genericContent()
            IncidentSituation.MONEY_SENT -> moneySentContent()
            IncidentSituation.APP_INSTALLED -> remoteControlContent()
            IncidentSituation.CREDENTIALS_SHARED -> credentialsSharedContent()
            IncidentSituation.LINK_OPENED -> linkOpenedContent()
        }
        val situationLabel = situation?.label ?: GENERIC_SITUATION_LABEL

        return EvidencePreservationKit(
            situation = situation,
            title = content.title,
            checklistItems = content.checklistItems,
            cautions = content.cautions,
            familyShareSummary = buildFamilyShareSummary(situationLabel, content.shareGuidance),
            officialReportPrepNotes = content.officialReportPrepNotes
        )
    }

    private fun genericContent() = EvidenceContent(
        title = "증거 보존 체크리스트: 상황이 아직 확실하지 않을 때",
        checklistItems = listOf(
            "휴대전화 전원을 유지하고 상대와의 대화는 멈춘 뒤, 메시지·통화 기록을 지우지 마세요.",
            "발신번호, 메시지·통화 시각, 요구 내용이 보이도록 관련 화면을 직접 캡처하세요.",
            "언제 무엇을 눌렀고, 돈·정보·앱 접근을 제공했는지 시간 순서대로 따로 적으세요.",
            "거래가 있었다면 금액·수취 계좌나 지갑·상품권 전달 여부와 거래 시각을 보관하세요.",
            "은행·카드사·경찰에 연락한 시각과 상담자, 접수번호를 이어서 기록하세요."
        ),
        cautions = listOf(
            "필요한 화면을 남기기 전에 메시지·통화 기록·앱을 급히 삭제하거나 휴대전화를 초기화하지 마세요.",
            "증거를 모으느라 송금 차단, 원격 연결 끊기, 계정 보호 같은 긴급 조치를 늦추지 마세요.",
            "가족 단톡방에는 원문, 계좌 전체 번호, 비밀번호, OTP, 신분증 사진을 올리지 마세요."
        ),
        shareGuidance = "메시지·통화 시각과 관련 화면을 먼저 남기고, 무슨 일이 있었는지 시간 순서대로 적어 주세요.",
        officialReportPrepNotes = listOf(
            "상담 전에 시간 순서와 금액·정보·앱 접근 여부를 한 장에 정리하세요.",
            "은행·카드사·112·1394·1332에서 받은 접수번호와 안내 내용을 이어서 보관하세요."
        )
    )

    private fun moneySentContent() = EvidenceContent(
        title = "증거 보존 체크리스트: 송금·상품권·코인 전달",
        checklistItems = listOf(
            "송금·카드 결제·상품권 구매·코인 전송 내역에서 거래 시각, 금액, 수취 계좌나 지갑이 보이도록 캡처하세요.",
            "상품권을 전달했다면 PIN과 전달 시각을 외부에 공유하지 말고 따로 기록하세요.",
            "상대 발신번호와 통화 목록, 메시지 대화의 날짜·시각이 보이도록 화면을 순서대로 캡처하세요.",
            "입금 확인증, 상품권 영수증, 코인 거래 식별번호처럼 이미 받은 자료를 한곳에 모으세요.",
            "은행·카드사·경찰에 연락한 시각과 상담자, 지급정지·신고 접수번호를 보관하세요."
        ),
        cautions = listOf(
            "화면을 남기기 전에 거래 내역이나 메시지를 삭제하지 마세요.",
            "확인 비용이나 환급 수수료라는 말에 추가 송금하거나 상대에게 다시 연락하지 마세요.",
            "계좌 전체 번호, 코인 지갑 복구문구, 상품권 PIN을 가족 채팅이나 공개 공간에 올리지 마세요."
        ),
        shareGuidance = "거래 시각·금액·수취 정보와 연락 기록을 삭제하지 말고 보관한 뒤, 은행 지급정지와 신고 접수번호를 남겨 주세요.",
        officialReportPrepNotes = listOf(
            "거래한 금융회사, 시각, 금액, 수취 계좌·지갑·거래 식별번호를 정리하세요.",
            "상품권이면 구매처·영수증·PIN 전달 시각을 외부 공유 없이 준비하세요.",
            "은행 지급정지 번호와 경찰 신고 접수번호를 같은 시간 순서표에 적으세요."
        )
    )

    private fun remoteControlContent() = EvidenceContent(
        title = "증거 보존 체크리스트: 원격제어·화면공유 허용",
        checklistItems = listOf(
            "원격제어·화면공유가 이어지고 있다면 먼저 비행기 모드로 전환하거나 와이파이·데이터를 꺼 연결을 끊으세요.",
            "연결이 끊긴 상태에서 안전하게 가능할 때 앱 이름, 설치 시각, 허용 권한, 접근성·기기 관리자 설정 화면을 캡처하세요.",
            "앱 설치나 화면공유를 요구한 메시지·발신번호·통화 시각과 다운로드 안내 화면을 캡처하세요.",
            "다른 안전한 기기에서 주요 계정 비밀번호를 변경하고, 변경·로그아웃 알림의 시각을 보관하세요.",
            "은행·경찰에 연락한 시각과 상담자, 계정 보호·신고 접수번호를 보관하세요."
        ),
        cautions = listOf(
            "증거를 더 모으려고 의심 휴대전화의 연결을 다시 켜거나 원격 앱을 다시 실행하지 마세요.",
            "원격 조작 가능성이 있는 휴대전화에서 은행·이메일 비밀번호를 변경하지 마세요.",
            "기본 정보를 남기기 전에 앱 삭제나 초기화를 서두르지 마세요. 다만 원격 연결이 계속되면 캡처보다 연결 차단을 우선하세요."
        ),
        shareGuidance = "먼저 원격 연결을 끊고, 안전한 범위에서 앱 이름과 허용 권한을 남긴 뒤 다른 안전한 기기로 계정을 보호해 주세요.",
        officialReportPrepNotes = listOf(
            "앱 이름, 설치 시각, 허용한 권한, 원격 연결이 시작·종료된 시각을 정리하세요.",
            "은행의 계정 보호 번호와 경찰 신고 접수번호, 안내받은 앱 제거 시점을 기록하세요."
        )
    )

    private fun credentialsSharedContent() = EvidenceContent(
        title = "증거 보존 체크리스트: 계정·인증정보 공유",
        checklistItems = listOf(
            "알려준 정보의 종류(계좌, 비밀번호, OTP, 신분증 등)와 전달 시각만 적고, 실제 비밀값은 다시 복사하지 마세요.",
            "정보를 요구한 메시지·발신번호·통화 시각과 로그인·보안 경고 화면을 캡처하세요.",
            "다른 안전한 기기에서 비밀번호 변경, 전체 로그아웃, OTP·카드 재발급을 진행하고 완료 시각을 보관하세요.",
            "은행·카드사에 잠금이나 한도 축소를 요청한 시각과 접수번호를 기록하세요.",
            "경찰·상담기관에 연락했다면 신고 접수번호와 추가로 안내받은 조치를 적으세요."
        ),
        cautions = listOf(
            "실제 비밀번호, OTP, 보안카드 번호, 신분증 사진을 가족 채팅에 보내지 마세요.",
            "필요한 화면을 남기기 전에 보안 알림이나 상대 메시지를 삭제하지 마세요.",
            "이미 알려준 비밀번호를 다른 계정에서 다시 사용하지 마세요."
        ),
        shareGuidance = "공유한 정보의 종류와 시각, 보안 경고 화면을 남기고 다른 안전한 기기에서 계정 보호 조치를 진행해 주세요.",
        officialReportPrepNotes = listOf(
            "노출된 정보의 종류와 전달 시각, 해당 계정·금융회사를 실제 비밀값 없이 정리하세요.",
            "비밀번호 변경·OTP 재발급·카드 잠금 완료 시각과 기관별 접수번호를 기록하세요."
        )
    )

    private fun linkOpenedContent() = EvidenceContent(
        title = "증거 보존 체크리스트: 의심 링크 클릭",
        checklistItems = listOf(
            "링크를 다시 열지 말고, 발신번호·메시지·링크 문구·수신 시각이 보이는 화면을 캡처하세요.",
            "링크를 누른 시각과 정보 입력, 파일 다운로드, 앱 설치 여부를 시간 순서대로 적으세요.",
            "의심 파일을 열지 않은 채 브라우저 다운로드 기록, 보안 경고, 새로 생긴 앱 이름을 캡처하세요.",
            "이후 도착한 로그인·결제·비밀번호 변경 알림이 있다면 시각이 보이도록 보관하세요.",
            "1394나 금융회사에 상담했다면 연락 시각과 상담·접수번호를 기록하세요."
        ),
        cautions = listOf(
            "증거 확인을 위해 링크를 다시 누르거나 파일·앱을 실행하지 마세요.",
            "기본 화면을 남기기 전에 메시지와 다운로드 기록을 지우거나 휴대전화를 초기화하지 마세요.",
            "앱 설치나 원격제어까지 허용했다면 링크 점검을 멈추고 원격 연결 차단 안내를 먼저 따르세요."
        ),
        shareGuidance = "링크를 다시 열지 말고, 메시지와 클릭 시각·다운로드 여부·보안 알림을 삭제하지 않은 채 남겨 주세요.",
        officialReportPrepNotes = listOf(
            "메시지 수신·링크 클릭 시각과 정보 입력·다운로드·앱 설치 여부를 정리하세요.",
            "상담했다면 1394 또는 금융회사의 접수번호와 이후 보안 알림 시각을 함께 기록하세요."
        )
    )

    private fun buildFamilyShareSummary(situationLabel: String, guidance: String): String =
        buildString {
            appendLine("[가족 AI 보이스피싱 방패] 증거 보존 확인")
            appendLine()
            appendLine("상황: $situationLabel")
            appendLine(guidance)
            appendLine("은행·경찰에 연락했다면 상담 시각과 접수번호도 함께 보관해 주세요.")
            appendLine()
            append("붙여넣은 원문 대화와 실제 계좌·비밀번호·인증번호(OTP)·상품권 PIN은 포함하지 않았습니다.")
        }.trim()

    private data class EvidenceContent(
        val title: String,
        val checklistItems: List<String>,
        val cautions: List<String>,
        val shareGuidance: String,
        val officialReportPrepNotes: List<String>
    )
}
