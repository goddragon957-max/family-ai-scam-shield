package com.mongmong.familyshield

data class OfficialReportPrepCard(
    val title: String,
    val riskSummary: String,
    val redactedClue: String,
    val detectedSignalLabels: List<String>,
    val recommendedEntries: List<OfficialHelpEntry>,
    val scriptLines: List<String>,
    val beforeCallChecklist: List<String>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedEntryIds: List<OfficialHelpEntryId>
        get() = recommendedEntries.map { it.id }
}

object OfficialReportPrepBuilder {
    private val urgentHelpSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.APP_INSTALL,
        ScamSignal.LIVE_CALL_CONTROL,
        ScamSignal.CREDENTIALS,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.LOAN_REFINANCE_FEE
    )

    private val plausibleFinancialRiskSignals = setOf(
        ScamSignal.BAIT,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    fun build(analysis: ScamAnalysis): OfficialReportPrepCard {
        val signalSet = analysis.matchedSignals.toSet()
        val signalLabels = analysis.matchedSignals
            .map { it.label }
            .ifEmpty { listOf("강한 위험 신호 없음") }
        val recommendedEntries = recommendEntries(analysis, signalSet)
        val riskSummary = "위험도 ${analysis.riskLevel.label} · ${analysis.score}점 — ${analysis.summary}"
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)
        val scriptLines = buildScriptLines(analysis, signalLabels, recommendedEntries)
        val beforeCallChecklist = buildBeforeCallChecklist(analysis, signalSet)
        val privacySafetyNotes = listOf(
            "개인정보를 가린 짧은 단서만 화면에 표시하며, 붙여넣은 전체 원문과 가림 단서는 가족 공유 요약에 넣지 않습니다.",
            "이 카드는 따로 저장하지 않으며 사건 기록을 자동으로 추가하거나 바꾸지 않습니다.",
            "현재 분석은 기기 안에서만 정리합니다. 통화·문자를 자동으로 확인하거나 네트워크로 보내지 않습니다.",
            "상담 준비용 참고 안내이며 법률·금융 자문이나 피해금 회수·환급 약속이 아닙니다. 실제 조치와 결과는 공식 기관과 금융회사에 확인하세요."
        )
        val familyShareSummary = buildFamilyShareSummary(
            riskSummary = riskSummary,
            signalLabels = signalLabels,
            recommendedEntries = recommendedEntries,
            scriptLines = scriptLines,
            beforeCallChecklist = beforeCallChecklist
        )

        return OfficialReportPrepCard(
            title = "신고용 상담 준비 카드",
            riskSummary = riskSummary,
            redactedClue = redactedClue,
            detectedSignalLabels = signalLabels,
            recommendedEntries = recommendedEntries,
            scriptLines = scriptLines,
            beforeCallChecklist = beforeCallChecklist,
            privacySafetyNotes = privacySafetyNotes,
            familyShareSummary = familyShareSummary
        )
    }

    private fun recommendEntries(
        analysis: ScamAnalysis,
        signalSet: Set<ScamSignal>
    ): List<OfficialHelpEntry> {
        val recommendedIds = when {
            analysis.riskLevel == RiskLevel.HIGH || signalSet.any { it in urgentHelpSignals } ->
                OfficialHelpEntryId.entries.toSet()
            else -> buildSet {
                add(OfficialHelpEntryId.VOICE_PHISHING_1394)
                if (signalSet.any { it in plausibleFinancialRiskSignals }) {
                    add(OfficialHelpEntryId.BANK_CARD_OFFICIAL)
                }
            }
        }

        return OfficialHelpDirectoryBuilder.build().entries.filter { it.id in recommendedIds }
    }

    private fun buildScriptLines(
        analysis: ScamAnalysis,
        signalLabels: List<String>,
        recommendedEntries: List<OfficialHelpEntry>
    ): List<String> = buildList {
        if (analysis.matchedSignals.isEmpty()) {
            add("강한 위험 신호는 찾지 못했지만, 가족 콜백과 공식 상담으로 요청 내용을 다시 확인하고 싶습니다.")
        } else {
            add("보이스피싱 또는 스미싱이 의심되어 상담 준비 카드를 만들었습니다.")
        }
        add("기기 내 분석 결과 위험도는 ${analysis.riskLevel.label}, ${analysis.score}점입니다.")
        if (analysis.matchedSignals.isEmpty()) {
            add("기기 내 분석에서 확인된 항목은 ‘강한 위험 신호 없음’입니다.")
        } else {
            add("탐지된 위험 신호는 ${signalLabels.joinToString(", ")}입니다.")
        }
        add("송금·상품권 전달, 링크 클릭, 앱 설치·화면공유, 계좌·인증정보 제공 여부는 실제로 한 행동만 구분해 말씀드리겠습니다.")
        recommendedEntries.forEach { entry ->
            add("${contactLabel(entry)}에 문의: ${entry.whatToSay.first()}")
        }
        add("지금 먼저 할 조치와 준비할 자료, 상담 또는 신고 접수번호를 알려 주세요.")
    }

    private fun buildBeforeCallChecklist(
        analysis: ScamAnalysis,
        signalSet: Set<ScamSignal>
    ): List<String> = buildList {
        if (analysis.riskLevel == RiskLevel.HIGH || signalSet.any { it in urgentHelpSignals }) {
            add("상대와의 대화를 멈추고 송금·앱 설치·화면공유·인증정보 제공을 중단하세요.")
            add("기존에 저장된 가족 번호로 직접 콜백해 사실을 확인하세요.")
        } else {
            add("기존에 저장된 가족 번호로 직접 콜백해 사실을 먼저 확인하세요.")
        }
        add("연락받은 시각과 실제로 한 행동을 시간 순서대로 적고, 하지 않은 행동을 피해 사실처럼 말하지 마세요.")
        add("송금이나 카드 거래가 있었다면 금융회사, 시각, 금액, 이미 받은 접수번호를 본인 메모에 준비하세요.")
        if (ScamSignal.LINK in signalSet || ScamSignal.PARCEL_INVITATION_SMISHING in signalSet) {
            add("의심 링크를 다시 열지 말고 클릭·정보 입력·다운로드·앱 설치 여부만 확인하세요.")
        }
        if (ScamSignal.APP_INSTALL in signalSet || ScamSignal.LIVE_CALL_CONTROL in signalSet) {
            add("원격 연결이 계속되면 먼저 연결을 끊고, 계정 보호는 다른 안전한 기기에서 진행하세요.")
        }
        add("문자나 메신저에 적힌 번호 대신 공식 앱·카드 뒷면·직접 입력한 공식 웹사이트의 번호를 사용하세요.")
        add("비밀번호, OTP, 보안카드 번호, 카드 PIN, 상품권 PIN은 통화 중에도 말하지 마세요.")
        add("통화가 끝나면 기관명, 연락 시각, 안내받은 조치와 접수번호를 기록하세요.")
    }

    private fun buildFamilyShareSummary(
        riskSummary: String,
        signalLabels: List<String>,
        recommendedEntries: List<OfficialHelpEntry>,
        scriptLines: List<String>,
        beforeCallChecklist: List<String>
    ): String = buildString {
        appendLine("[가족 AI 보이스피싱 방패] 신고용 상담 준비 카드")
        appendLine()
        appendLine(riskSummary)
        appendLine()
        appendLine("탐지 신호")
        signalLabels.forEach { appendLine("- $it") }
        appendLine()
        appendLine("추천 공식 연락처")
        recommendedEntries.forEach { appendLine("- ${contactLabel(it)}") }
        appendLine()
        appendLine("상담원에게 말할 내용")
        scriptLines.forEachIndexed { index, line -> appendLine("${index + 1}. $line") }
        appendLine()
        appendLine("전화 전 확인")
        beforeCallChecklist.forEach { appendLine("- $it") }
        appendLine()
        appendLine("가족에게는 기존에 저장된 번호로 먼저 콜백해 주세요.")
        appendLine("붙여넣은 원문과 가림 단서, 상대 발신번호·개인 전화번호·계좌번호·비밀번호·인증번호(OTP)·링크·신분정보는 이 공유문에 포함하지 않았습니다.")
        append("이 내용은 상담 준비용 참고이며 실제 조치와 결과는 공식 기관과 금융회사에 확인해 주세요.")
    }.trim()

    private fun contactLabel(entry: OfficialHelpEntry): String =
        entry.dialNumber?.let { number -> "$number · ${entry.label}" }
            ?: "${entry.label} · 공식 앱·카드 뒷면에서 번호 확인"
}
