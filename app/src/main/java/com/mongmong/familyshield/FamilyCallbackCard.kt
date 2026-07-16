package com.mongmong.familyshield

data class FamilyCallbackCard(
    val title: String,
    val riskSummary: String,
    val redactedClue: String?,
    val readiness: FamilyReadiness,
    val safeWordPrompt: String,
    val callbackSteps: List<String>,
    val dialableContacts: List<TrustedContact>,
    val guardrails: List<String>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val needsSafetyPlanSetup: Boolean
        get() = readiness.level != ReadinessLevel.READY
}

object FamilyCallbackCardBuilder {
    private val stopFirstSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.APP_INSTALL,
        ScamSignal.BAIT,
        ScamSignal.CREDENTIALS,
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.PHONE_NUMBER_CHANGE,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.LIVE_CALL_CONTROL,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    fun build(analysis: ScamAnalysis?, plan: SafetyPlan): FamilyCallbackCard {
        val readiness = FamilyReadinessBuilder.build(plan)
        val safeWord = plan.safeWord.trim()
        val dialableContacts = plan.contacts
            .map { contact ->
                contact.copy(
                    label = contact.label.trim(),
                    number = contact.number.trim()
                )
            }
            .filter { it.number.isNotBlank() }
        val shouldStopFirst = analysis?.let { result ->
            result.riskLevel == RiskLevel.HIGH ||
                result.matchedSignals.any { it in stopFirstSignals }
        } == true
        val riskSummary = analysis?.let { result ->
            "위험도 ${result.riskLevel.label} · ${result.score}점 — ${result.summary}"
        } ?: "아직 분석 결과가 없습니다. 저장된 가족 안전 계획을 먼저 점검하고, " +
            "의심 요청이 생기면 내용을 직접 붙여넣어 분석하세요."
        val safeWordPrompt = if (safeWord.isNotBlank()) {
            "저장된 가족 번호로 콜백이 연결되면 가족 안전단어(확인 질문)를 그대로 물어보세요: " +
                "“$safeWord”\n대답만 믿지 말고 평소 알고 있던 가족 정보와 함께 확인하세요."
        } else {
            "저장된 가족 안전단어(확인 질문)가 없습니다. 목소리나 이름만으로 본인이라고 판단하지 말고, " +
                "가족 안전 계획에 가족만 아는 질문을 먼저 설정하세요."
        }
        val callbackSteps = buildCallbackSteps(
            analysis = analysis,
            shouldStopFirst = shouldStopFirst,
            hasDialableContacts = dialableContacts.isNotEmpty(),
            hasSafeWord = safeWord.isNotBlank()
        )
        val guardrails = listOf(
            "본인 확인이 끝날 때까지 송금·이체·상품권·코인 전달을 하지 마세요.",
            "앱 설치·화면공유·원격제어를 허용하지 마세요.",
            "계좌번호·비밀번호·인증번호(OTP)·보안카드 번호·카드/상품권 PIN을 말하거나 보내지 마세요.",
            "문자·메신저의 새 번호, 걸려온 번호, 링크를 가족 확인 수단으로 사용하지 마세요."
        )
        val privacySafetyNotes = listOf(
            "이 카드는 현재 기기 안의 분석 결과와 사용자가 직접 입력한 가족 안전 계획만 사용합니다. " +
                "기기 연락처 목록을 읽지 않으며 연락처 권한을 요청하지 않습니다.",
            "분석 내용과 콜백 카드는 네트워크로 보내지 않으며 통화·문자 내용을 자동으로 가져오지 않습니다.",
            "개인정보를 가린 짧은 단서만 화면에 표시하며, 가족 공유 요약에는 붙여넣은 원문과 가림 단서를 넣지 않습니다.",
            "이 카드는 따로 저장하지 않으며 사건 기록을 자동으로 추가하거나 수정하지 않습니다.",
            "전화 버튼은 ACTION_DIAL로 전화 앱만 열고 사용자가 직접 통화를 시작합니다. " +
                "공유 버튼은 사용자가 누를 때 ACTION_SEND로 텍스트 요약만 엽니다.",
            "이 안내는 가족 확인을 돕는 참고 절차이며 법률·금융 자문이나 피해금 회수·환급, " +
                "정확한 본인 확인을 약속하는 기능이 아닙니다."
        )

        return FamilyCallbackCard(
            title = "가족 콜백 확인 카드",
            riskSummary = riskSummary,
            redactedClue = analysis?.let(IncidentPreviewRedactor::buildPrivacySafeClue),
            readiness = readiness,
            safeWordPrompt = safeWordPrompt,
            callbackSteps = callbackSteps,
            dialableContacts = dialableContacts,
            guardrails = guardrails,
            privacySafetyNotes = privacySafetyNotes,
            familyShareSummary = buildFamilyShareSummary(
                riskSummary = riskSummary,
                readiness = readiness,
                shouldStopFirst = shouldStopFirst,
                hasDialableContacts = dialableContacts.isNotEmpty(),
                hasSafeWord = safeWord.isNotBlank()
            )
        )
    }

    private fun buildCallbackSteps(
        analysis: ScamAnalysis?,
        shouldStopFirst: Boolean,
        hasDialableContacts: Boolean,
        hasSafeWord: Boolean
    ): List<String> = buildList {
        if (shouldStopFirst) {
            add("현재 상대와의 통화·메시지 대화를 먼저 멈추고 답장이나 재통화를 보류하세요.")
        }

        if (hasDialableContacts) {
            if (shouldStopFirst) {
                add(
                    "의심 메시지의 새 번호나 걸려온 번호가 아니라, 가족 안전 계획에 미리 저장된 " +
                        "신뢰 번호로 직접 콜백해 확인하세요."
                )
            } else {
                add(
                    "송금·앱 설치·인증번호(OTP) 제공 전에, 의심 요청 속 번호가 아닌 가족 안전 계획에 " +
                        "저장된 신뢰 번호로 먼저 직접 콜백해 확인하세요."
                )
            }
        } else {
            add(
                if (shouldStopFirst) {
                    "저장된 콜백 번호가 없습니다. 의심 메시지의 새 번호·링크·발신번호로 확인하지 말고, " +
                        "가족 안전 계획에 평소 쓰던 신뢰 번호를 먼저 설정하세요."
                } else {
                    "저장된 콜백 번호가 없습니다. 송금·앱 설치·인증번호(OTP) 제공을 진행하지 말고, " +
                        "의심 메시지의 새 번호·링크·발신번호를 쓰지 않은 채 가족 안전 계획에 " +
                        "평소 쓰던 신뢰 번호를 먼저 설정하세요."
                }
            )
        }

        if (hasSafeWord) {
            add("저장된 번호로 연결된 뒤 가족 안전단어(확인 질문)를 직접 묻고, 대답이 평소와 맞는지 확인하세요.")
        } else {
            add(
                "목소리만 믿지 말고 가족 안전 계획에 안전단어(확인 질문)를 설정한 뒤, " +
                    "저장된 번호 콜백과 함께 확인하세요."
            )
        }

        if (analysis == null) {
            add("의심 요청이 있다면 내용을 직접 붙여넣어 기기 안에서 분석하고 위험 신호를 확인하세요.")
        } else if (
            analysis.matchedSignals.any {
                it == ScamSignal.FAMILY_EMERGENCY ||
                    it == ScamSignal.DEEPFAKE_VOICE ||
                    it == ScamSignal.PHONE_NUMBER_CHANGE
            }
        ) {
            add("목소리나 ‘번호가 바뀌었다’는 말만으로 본인 확인을 끝내지 마세요.")
        } else {
            add("요청한 사람의 이름이나 말투만으로 본인 확인을 끝내지 마세요.")
        }

        add("가족 본인 확인이 끝난 뒤에도 돈·앱·인증정보 요청은 필요성과 공식 경로를 다시 확인하세요.")
    }

    private fun buildFamilyShareSummary(
        riskSummary: String,
        readiness: FamilyReadiness,
        shouldStopFirst: Boolean,
        hasDialableContacts: Boolean,
        hasSafeWord: Boolean
    ): String = buildString {
        appendLine("[가족 AI 보이스피싱 방패] 가족 콜백 확인 카드")
        appendLine()
        appendLine(riskSummary)
        appendLine("가족 준비도: ${readiness.level.label} (${readiness.completedCount}/${readiness.totalCount})")
        appendLine()
        appendLine("가족 확인 순서")
        var stepNumber = 1
        if (shouldStopFirst) {
            appendLine("$stepNumber. 현재 상대와의 대화를 멈추고 송금·앱 설치·인증정보 제공을 보류하세요.")
            stepNumber += 1
        }
        if (hasDialableContacts) {
            appendLine("$stepNumber. 의심 요청 속 번호가 아니라 앱에 저장된 가족 신뢰 연락처로 직접 콜백하세요.")
        } else {
            appendLine("$stepNumber. 의심 요청의 새 번호를 사용하지 말고 가족 안전 계획에 평소 쓰던 번호를 먼저 설정하세요.")
        }
        stepNumber += 1
        if (hasSafeWord) {
            appendLine("$stepNumber. 콜백이 연결되면 앱에 저장된 가족 안전 질문으로 확인하세요.")
        } else {
            appendLine("$stepNumber. 가족 안전 계획에 가족만 아는 안전 질문을 설정하세요.")
        }
        stepNumber += 1
        appendLine("${stepNumber}. 확인 전에는 돈·앱·인증번호(OTP)를 보내거나 허용하지 마세요.")
        appendLine()
        appendLine("앱에 저장된 가족 안전 질문과 신뢰 연락처를 사용해 직접 확인해 주세요.")
        append(
            "붙여넣은 원문, 개인정보 가림 단서, 가족 안전단어(확인 질문) 내용, 저장된 연락처 이름·전체 전화번호, " +
                "콜백 스크립트, 계좌번호·비밀번호·인증번호(OTP)·카드/상품권 PIN·링크·신분정보는 " +
                "이 공유문에 포함하지 않았습니다."
        )
    }.trim()
}
