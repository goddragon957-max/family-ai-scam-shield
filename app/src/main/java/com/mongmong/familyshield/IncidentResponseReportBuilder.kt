package com.mongmong.familyshield

object IncidentResponseReportBuilder {
    fun build(plan: IncidentResponsePlan): String {
        val steps = plan.steps.mapIndexed { index, step -> "${index + 1}. $step" }.joinToString("\n")
        val contacts = plan.emergencyContacts.joinToString("\n") { "- ${it.label}: ${it.number}" }

        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 긴급 대응 요약")
            appendLine()
            appendLine("상황: ${plan.situation.label}")
            appendLine(plan.title)
            appendLine(steps)
            appendLine()
            appendLine("도움 연락처")
            appendLine(contacts)
            appendLine()
            appendLine("원문 메시지나 대화 내용은 개인정보 보호를 위해 포함하지 않았습니다. 선택한 상황과 대응 단계만 공유됩니다.")
        }.trim()
    }
}
