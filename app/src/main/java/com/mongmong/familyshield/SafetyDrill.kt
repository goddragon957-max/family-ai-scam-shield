package com.mongmong.familyshield

enum class DrillScenario(val label: String) {
    FAMILY_NUMBER_CHANGE("자녀/부모 번호 변경 송금 요청 연습"),
    PROSECUTOR_SAFE_ACCOUNT("검찰/경찰 안전계좌 압박 연습"),
    REMOTE_CONTROL("원격제어 앱·화면공유 압박 연습"),
    PARCEL_SMISHING("택배/청첩장 스미싱 링크 연습")
}

data class SafetyDrill(
    val scenario: DrillScenario,
    val title: String,
    val fictionalScript: String,
    val responseSteps: List<String>,
    val passChecklist: List<String>
)

object SafetyDrillBuilder {
    const val FICTION_NOTICE =
        "아래는 실제 사건이 아닌, 가족이 함께 연습하기 위한 가상 시나리오입니다."

    fun build(scenario: DrillScenario): SafetyDrill = when (scenario) {
        DrillScenario.FAMILY_NUMBER_CHANGE -> SafetyDrill(
            scenario = scenario,
            title = "연습 1 · 자녀/부모 번호 변경 송금 요청",
            fictionalScript = "[연습용 가상 문자] “엄마, 나 폰이 고장 나서 임시 번호로 연락해. " +
                "지금 급하게 보험료를 내야 하는데 이 계좌로 30만 원만 먼저 보내줘.” " +
                "— 실제 문자가 아닌 연습용 가상 문장입니다.",
            responseSteps = listOf(
                "답장하지 말고 일단 멈춥니다. 급하다는 말이 나올수록 더 천천히 확인합니다.",
                "가족만 아는 안전단어(질문)를 먼저 물어봅니다.",
                "새 번호가 아니라 기존에 저장된 자녀/부모 번호로 직접 전화해 확인합니다.",
                "본인 확인이 끝나기 전에는 송금하지 않습니다."
            ),
            passChecklist = listOf(
                "새 번호로 답장하거나 전화하지 않았다",
                "안전단어(질문)를 먼저 물어봤다",
                "기존에 저장된 번호로 콜백해서 확인했다",
                "확인 전에는 송금을 멈췄다"
            )
        )

        DrillScenario.PROSECUTOR_SAFE_ACCOUNT -> SafetyDrill(
            scenario = scenario,
            title = "연습 2 · 검찰/경찰 안전계좌 압박",
            fictionalScript = "[연습용 가상 통화] “서울중앙지검 수사관입니다. 고객님 계좌가 범죄에 " +
                "연루되어 예금을 안전계좌로 옮겨야 합니다. 수사 중이니 가족에게도 말하지 마세요.” " +
                "— 실제 통화가 아닌 연습용 가상 대사입니다.",
            responseSteps = listOf(
                "전화를 먼저 끊습니다. 진짜 수사기관은 전화로 이체를 요구하지 않습니다.",
                "‘안전계좌’로 돈을 옮기라는 요구는 어떤 경우에도 거절합니다.",
                "궁금하면 걸려온 번호가 아니라 112나 금융감독원 1332 공식 번호로 직접 확인합니다.",
                "비밀 유지 요구는 따르지 않고, 바로 가족에게 상황을 알립니다."
            ),
            passChecklist = listOf(
                "전화를 먼저 끊었다",
                "안전계좌 이체 요구를 거절했다",
                "112 또는 1332 공식 번호로 직접 확인했다",
                "가족에게 숨기지 않고 바로 알렸다"
            )
        )

        DrillScenario.REMOTE_CONTROL -> SafetyDrill(
            scenario = scenario,
            title = "연습 3 · 원격제어 앱·화면공유 압박",
            fictionalScript = "[연습용 가상 통화] “고객센터입니다. 보안 점검을 위해 지금 알려드리는 " +
                "앱을 설치하고 화면공유를 켜 주세요. 확인이 끝날 때까지 통화를 끊으면 안 됩니다.” " +
                "— 실제 통화가 아닌 연습용 가상 대사입니다.",
            responseSteps = listOf(
                "앱 설치와 화면공유 요청은 어떤 기관이든 거절합니다.",
                "‘통화를 끊지 말라’는 압박은 무시하고 전화를 끊습니다.",
                "저장된 가족 번호로 전화해 방금 받은 요구를 공유합니다.",
                "실제 상황에서 이미 설치했다면 먼저 비행기 모드로 연결을 끊고, 앱 정보 화면을 안전하게 남긴 뒤 긴급 대응 센터 절차에 따라 제거와 계정 보호를 진행합니다."
            ),
            passChecklist = listOf(
                "앱 설치와 화면공유를 거절했다",
                "통화 유지 압박을 무시하고 전화를 끊었다",
                "저장된 가족 번호로 상황을 공유했다",
                "앱의 긴급 대응 센터 위치를 확인해뒀다"
            )
        )

        DrillScenario.PARCEL_SMISHING -> SafetyDrill(
            scenario = scenario,
            title = "연습 4 · 택배/청첩장 스미싱 링크",
            fictionalScript = "[연습용 가상 문자] “[국제발신] 택배 주소지 오류로 배송이 보류되었습니다. " +
                "아래 링크에서 주소를 다시 확인해 주세요.” " +
                "— 실제 문자가 아닌 연습용 가상 문장이며, 링크는 포함하지 않았습니다.",
            responseSteps = listOf(
                "문자 속 링크는 누르지 않습니다.",
                "택배 조회가 필요하면 공식 택배사 앱이나 공식 사이트에서 직접 확인합니다.",
                "이름, 주소, 카드번호 등 개인정보를 링크 화면에 입력하지 않습니다.",
                "의심 문자는 가족과 공유해 함께 확인하고, 이미 링크를 눌렀다면 긴급 대응 센터 절차를 따릅니다."
            ),
            passChecklist = listOf(
                "문자 속 링크를 누르지 않았다",
                "공식 앱/사이트에서 직접 조회했다",
                "개인정보를 입력하지 않았다",
                "의심 문자를 가족과 공유해 함께 확인했다"
            )
        )
    }
}
