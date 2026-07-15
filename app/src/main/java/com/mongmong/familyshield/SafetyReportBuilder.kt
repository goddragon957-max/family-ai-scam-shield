package com.mongmong.familyshield

object SafetyReportBuilder {
    fun build(analysis: ScamAnalysis): String {
        val advice = analysis.guardianAdvice
        val signals = if (analysis.matchedSignals.isEmpty()) {
            "강한 위험 신호 없음"
        } else {
            analysis.matchedSignals.joinToString("\n") { "- ${it.label}" }
        }
        val actions = advice.immediateActions.joinToString("\n") { "- $it" }

        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 확인 요청")
            appendLine()
            appendLine("위험도: ${analysis.riskLevel.label} (${analysis.score}점)")
            appendLine(advice.verdict)
            appendLine()
            appendLine("감지 신호")
            appendLine(signals)
            appendLine()
            appendLine("지금 할 일")
            appendLine(actions)
            appendLine()
            appendLine(advice.callbackPrompt)
            appendLine()
            appendLine("원문은 개인정보 보호를 위해 포함하지 않았습니다. 이 메시지를 받은 가족은 기존에 저장된 번호로 직접 다시 연락해 주세요.")
        }.trim()
    }
}
