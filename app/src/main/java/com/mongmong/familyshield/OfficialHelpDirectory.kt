package com.mongmong.familyshield

enum class OfficialHelpEntryId {
    POLICE_112,
    VOICE_PHISHING_1394,
    FINANCIAL_SUPERVISORY_SERVICE_1332,
    BANK_CARD_OFFICIAL
}

data class OfficialHelpEntry(
    val id: OfficialHelpEntryId,
    val label: String,
    val dialNumber: String?,
    val whenToUse: String,
    val whatToSay: List<String>,
    val safetyNotes: List<String>
)

data class OfficialHelpDirectory(
    val title: String,
    val intro: String,
    val entries: List<OfficialHelpEntry>,
    val familyShareSummary: String
)

object OfficialHelpDirectoryBuilder {
    fun build(): OfficialHelpDirectory {
        val entries = listOf(
            OfficialHelpEntry(
                id = OfficialHelpEntryId.POLICE_112,
                label = "경찰 긴급 신고",
                dialNumber = "112",
                whenToUse =
                    "신변의 위험이 있거나 송금·상품권·가상자산 전달 등 긴급한 사기 피해가 이미 발생해 즉시 신고가 필요할 때 사용하세요.",
                whatToSay = listOf(
                    "보이스피싱 또는 스미싱 피해가 의심된다고 먼저 말하세요.",
                    "발생 시각, 피해 유형과 금액, 상대 발신번호, 현재 위험 여부를 차례로 말하세요.",
                    "지금 가장 먼저 할 조치와 신고 접수번호를 확인하세요."
                ),
                safetyNotes = listOf(
                    "위험한 장소라면 먼저 안전한 곳으로 이동하고 상대와의 통화를 끊으세요.",
                    "가족 채팅에는 원문 대화, 계좌 전체 번호, 비밀번호, OTP, 신분증 사진을 올리지 마세요."
                )
            ),
            OfficialHelpEntry(
                id = OfficialHelpEntryId.VOICE_PHISHING_1394,
                label = "보이스피싱 통합신고·상담",
                dialNumber = "1394",
                whenToUse =
                    "보이스피싱·스미싱이 의심되거나, 의심 전화번호·사이트를 제보하고 대응 방법을 상담받고 싶을 때 사용하세요.",
                whatToSay = listOf(
                    "보이스피싱 또는 스미싱이 의심되어 상담과 신고 방법을 안내받고 싶다고 말하세요.",
                    "연락받은 시각, 사칭한 기관, 상대가 요구한 내용, 의심 번호나 사이트를 말하세요.",
                    "링크 클릭, 앱 설치, 정보 제공, 송금 여부를 구분해 말하고 다음 조치를 확인하세요."
                ),
                safetyNotes = listOf(
                    "의심 링크는 다시 열지 말고 메시지 속 번호로 다시 전화하지 마세요.",
                    "상담 내용을 가족에게 공유할 때도 원문, OTP, 상품권 PIN, 신분증 사진은 빼세요."
                )
            ),
            OfficialHelpEntry(
                id = OfficialHelpEntryId.FINANCIAL_SUPERVISORY_SERVICE_1332,
                label = "금융감독원",
                dialNumber = "1332",
                whenToUse =
                    "금융 피해가 발생했거나 지급정지, 금융회사 분쟁, 피해구제 절차에 대한 안내가 필요할 때 사용하세요.",
                whatToSay = listOf(
                    "사기 의심 금융 피해로 지급정지 또는 피해구제 절차를 문의한다고 말하세요.",
                    "거래 금융회사, 거래 시각과 금액, 이미 요청한 조치와 받은 접수번호를 말하세요.",
                    "추가로 연락할 기관, 준비할 자료, 다음 절차를 확인하세요."
                ),
                safetyNotes = listOf(
                    "비밀번호, OTP, 보안카드 번호, 카드 PIN은 누구에게도 말하지 마세요.",
                    "피해금 회복을 약속하며 수수료나 선입금을 요구하는 연락은 다시 확인하세요."
                )
            ),
            OfficialHelpEntry(
                id = OfficialHelpEntryId.BANK_CARD_OFFICIAL,
                label = "은행·카드사 공식 대표번호",
                dialNumber = null,
                whenToUse =
                    "계좌 지급정지, 카드 잠금, 결제 취소 가능 여부, 계정 보호 조치를 금융회사에 바로 요청할 때 사용하세요. 공식 앱, 카드 뒷면, 주소를 직접 입력한 공식 웹사이트에서 대표번호를 확인하세요.",
                whatToSay = listOf(
                    "사기 피해가 의심되어 계좌 지급정지, 카드 잠금 또는 결제 확인이 필요하다고 말하세요.",
                    "거래 시각과 금액, 피해 유형을 말하고 즉시 가능한 보호 조치를 요청하세요.",
                    "처리 결과와 접수번호, 추가로 해야 할 일을 확인하세요."
                ),
                safetyNotes = listOf(
                    "문자·카카오톡·검색 광고의 링크나 그 안에 적힌 번호를 사용하지 마세요.",
                    "은행·카드사에도 비밀번호, OTP, 보안카드 번호, 카드 PIN을 말하지 마세요."
                )
            )
        )

        return OfficialHelpDirectory(
            title = "신고·상담 연락처",
            intro = "상황에 맞는 공식 창구를 고르세요. 번호가 있는 항목은 전화 앱만 열며 통화는 사용자가 직접 시작합니다.",
            entries = entries,
            familyShareSummary = buildFamilyShareSummary(entries)
        )
    }

    private fun buildFamilyShareSummary(entries: List<OfficialHelpEntry>): String =
        buildString {
            appendLine("[가족 AI 보이스피싱 방패] 신고·상담 연락처")
            entries.forEach { entry ->
                appendLine()
                val contactLabel = entry.dialNumber?.let { "$it · ${entry.label}" }
                    ?: "${entry.label} · 공식 번호 직접 확인"
                appendLine(contactLabel)
                appendLine("언제: ${entry.whenToUse}")
                appendLine("말할 내용:")
                entry.whatToSay.forEachIndexed { index, script ->
                    appendLine("${index + 1}. $script")
                }
            }
            appendLine()
            append("붙여넣은 원문 대화와 실제 계좌 전체 번호, 비밀번호, 인증번호(OTP), 상품권 PIN은 포함하지 않았습니다.")
        }.trim()
}
