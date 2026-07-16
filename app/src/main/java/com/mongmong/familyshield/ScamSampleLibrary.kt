package com.mongmong.familyshield

enum class ScamSampleCategory(val label: String) {
    FAMILY_NUMBER_CHANGE_TRANSFER("가족 번호 변경 + 급한 송금 요청"),
    PROSECUTOR_SAFE_ACCOUNT("검찰/경찰 안전계좌 압박"),
    REMOTE_CONTROL_PRESSURE("원격제어 앱·화면공유 압박"),
    PARCEL_SMISHING_LINK("택배/청첩장 스미싱 링크"),
    OVERSEAS_PAYMENT_ALERT("해외/소액 결제 승인 미끼")
}

data class ScamSample(
    val category: ScamSampleCategory,
    val title: String,
    val sampleText: String,
    val expectedRiskLevel: RiskLevel,
    val expectedSignals: List<ScamSignal>,
    val learningNote: String
)

object ScamSampleLibrary {
    const val FICTION_MARKER = "[연습용 가상 메시지]"
    const val FICTION_NOTICE =
        "아래 샘플은 실제 문자가 아닌, 분석 연습을 위해 앱에 내장된 연습용 가상 메시지입니다."

    fun all(): List<ScamSample> = ScamSampleCategory.entries.map(::build)

    fun build(category: ScamSampleCategory): ScamSample = when (category) {
        ScamSampleCategory.FAMILY_NUMBER_CHANGE_TRANSFER -> ScamSample(
            category = category,
            title = "샘플 1 · 가족 번호 변경 + 급한 송금 요청",
            sampleText = "$FICTION_MARKER 엄마 나 폰 고장나서 번호 바뀌었어. 이 번호로 저장해줘. " +
                "지금 바로 이 계좌로 급히 돈 보내줘. 다른 사람한테는 말하지 마.",
            expectedRiskLevel = RiskLevel.HIGH,
            expectedSignals = listOf(
                ScamSignal.PHONE_NUMBER_CHANGE,
                ScamSignal.URGENT_TRANSFER,
                ScamSignal.FAMILY_EMERGENCY,
                ScamSignal.SECRECY
            ),
            learningNote = "가족을 사칭해 ‘번호가 바뀌었다’며 새 번호로만 연락하게 하고 급하게 송금을 " +
                "재촉하는 조합은 대표적인 가족 사칭 사기입니다. 기존에 저장된 번호로 직접 전화해 " +
                "확인하기 전에는 송금하지 마세요."
        )

        ScamSampleCategory.PROSECUTOR_SAFE_ACCOUNT -> ScamSample(
            category = category,
            title = "샘플 2 · 검찰/경찰 안전계좌 압박",
            sampleText = "$FICTION_MARKER 서울중앙지검 수사관입니다. 고객님 계좌가 범죄에 연루되어 " +
                "지금 바로 안전 계좌로 이체해야 합니다. 수사가 끝날 때까지 가족에게도 알리지 마세요.",
            expectedRiskLevel = RiskLevel.HIGH,
            expectedSignals = listOf(
                ScamSignal.IMPERSONATION,
                ScamSignal.SAFE_ACCOUNT_FRAUD,
                ScamSignal.URGENT_TRANSFER,
                ScamSignal.SECRECY
            ),
            learningNote = "진짜 검찰·경찰·금융감독원은 전화나 문자로 ‘안전계좌’ 이체를 요구하지 " +
                "않습니다. 수사 기밀이라며 가족에게 숨기라고 압박하는 것도 전형적인 사기 신호입니다."
        )

        ScamSampleCategory.REMOTE_CONTROL_PRESSURE -> ScamSample(
            category = category,
            title = "샘플 3 · 원격제어 앱·화면공유 압박",
            sampleText = "$FICTION_MARKER 은행 보안팀입니다. 고객님 계정 보호를 위해 지금 안내하는 " +
                "보안 앱 설치 후 화면 공유를 켜 주세요. 확인이 끝날 때까지 통화 끊지 말고 기다리세요.",
            expectedRiskLevel = RiskLevel.HIGH,
            expectedSignals = listOf(
                ScamSignal.IMPERSONATION,
                ScamSignal.APP_INSTALL,
                ScamSignal.LIVE_CALL_CONTROL
            ),
            learningNote = "어떤 기관도 보안 점검을 이유로 앱 설치나 화면 공유를 요구하지 않습니다. " +
                "통화를 끊지 못하게 하는 것은 주변에 확인할 시간을 주지 않으려는 수법입니다."
        )

        ScamSampleCategory.PARCEL_SMISHING_LINK -> ScamSample(
            category = category,
            title = "샘플 4 · 택배/청첩장 스미싱 링크",
            sampleText = "$FICTION_MARKER [국제발신] 택배 주소 오류로 배송이 보류되었습니다. " +
                "오늘 안에 링크 클릭 후 주소를 다시 입력해 주세요. (연습용이라 실제 링크는 넣지 않았습니다)",
            expectedRiskLevel = RiskLevel.MEDIUM,
            expectedSignals = listOf(
                ScamSignal.PARCEL_INVITATION_SMISHING,
                ScamSignal.LINK
            ),
            learningNote = "택배·청첩장·부고를 사칭한 문자는 링크 클릭을 유도해 가짜 페이지로 " +
                "보냅니다. 링크 대신 공식 택배사 앱이나 공식 사이트에서 직접 조회하세요."
        )

        ScamSampleCategory.OVERSEAS_PAYMENT_ALERT -> ScamSample(
            category = category,
            title = "샘플 5 · 해외/소액 결제 승인 미끼",
            sampleText = "$FICTION_MARKER [해외발신] 회원님 카드로 소액 결제 289,000원이 " +
                "승인되었습니다. 본인이 아닌 경우 아래 번호로 전화해 인증번호를 말씀해 주세요.",
            expectedRiskLevel = RiskLevel.MEDIUM,
            expectedSignals = listOf(
                ScamSignal.OVERSEAS_PAYMENT_ALERT,
                ScamSignal.CREDENTIALS
            ),
            learningNote = "하지도 않은 결제 승인 문자로 놀라게 한 뒤 확인 전화를 걸게 해 인증번호나 " +
                "개인정보를 빼내는 수법입니다. 문자 속 번호가 아니라 카드사 공식 대표번호로 확인하세요."
        )
    }
}
