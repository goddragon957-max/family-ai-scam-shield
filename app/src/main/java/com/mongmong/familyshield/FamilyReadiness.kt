package com.mongmong.familyshield

enum class ReadinessLevel(val label: String) {
    LOW("준비 부족"),
    IN_PROGRESS("준비 중"),
    READY("준비 좋음")
}

data class ReadinessItem(
    val label: String,
    val done: Boolean,
    val action: String,
    val optional: Boolean = false
)

data class FamilyReadiness(
    val level: ReadinessLevel,
    val title: String,
    val completedCount: Int,
    val totalCount: Int,
    val checklist: List<ReadinessItem>,
    val nextAction: String
)

object FamilyReadinessBuilder {
    const val READY_ACTION =
        "저장된 루틴이 준비됐어요. 가족 안전 연습(3분 훈련)으로 안전단어 질문과 콜백을 함께 연습해 보세요."

    fun build(plan: SafetyPlan): FamilyReadiness {
        val contactCount = plan.contacts.count { it.number.isNotBlank() }
        val checklist = listOf(
            ReadinessItem(
                label = "가족만 아는 안전단어(확인 질문) 저장",
                done = plan.safeWord.isNotBlank(),
                action = "가족 안전 계획에서 우리 가족만 아는 안전단어를 저장하세요."
            ),
            ReadinessItem(
                label = "콜백용 신뢰 연락처 1명 이상 저장",
                done = contactCount >= 1,
                action = "가족 안전 계획에 번호가 있는 신뢰 연락처를 한 명 이상 저장하세요."
            ),
            ReadinessItem(
                label = "위급할 때 읽을 콜백 스크립트 저장",
                done = plan.callbackScript.isNotBlank(),
                action = "가족 안전 계획에 위급할 때 그대로 읽을 콜백 스크립트를 저장하세요."
            ),
            ReadinessItem(
                label = "예비 연락처까지 2명 이상 저장 (선택)",
                done = contactCount >= 2,
                action = "한 명이 전화를 못 받을 때를 대비해 신뢰 연락처를 한 명 더 저장하세요.",
                optional = true
            )
        )

        val coreItems = checklist.filterNot { it.optional }
        val coreDone = coreItems.count { it.done }
        val level = when {
            coreDone == coreItems.size -> ReadinessLevel.READY
            coreDone == 0 -> ReadinessLevel.LOW
            else -> ReadinessLevel.IN_PROGRESS
        }

        return FamilyReadiness(
            level = level,
            title = "가족 준비도 · ${level.label}",
            completedCount = checklist.count { it.done },
            totalCount = checklist.size,
            checklist = checklist,
            nextAction = checklist.firstOrNull { !it.done }?.action ?: READY_ACTION
        )
    }
}
