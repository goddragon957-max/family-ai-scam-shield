package com.mongmong.familyshield

enum class RiskLevel(val label: String, val color: Int) {
    SAFE("낮음", 0xFF15803D.toInt()),
    MEDIUM("주의", 0xFFB45309.toInt()),
    HIGH("높음", 0xFFB91C1C.toInt())
}

enum class ScamSignal(
    val label: String,
    val description: String,
    val score: Int,
    val patterns: List<Regex>
) {
    URGENT_TRANSFER(
        "긴급 송금 압박",
        "지금 바로 돈을 보내라거나 이체를 재촉합니다.",
        25,
        listOf("(?=.*(급히|긴급|지금\\s*바로|당장))(?=.*(송금|이체|입금|돈\\s*보내|계좌로)).*").map { Regex(it) }
    ),
    IMPERSONATION(
        "기관 사칭",
        "검찰, 경찰, 은행, 금감원 등 기관을 사칭할 수 있습니다.",
        25,
        listOf("검찰|검사|경찰|수사관|금융감독원|금감원|은행|카드사|보안팀").map { Regex(it) }
    ),
    APP_INSTALL(
        "앱 설치/원격제어 유도",
        "원격제어 앱이나 출처 불명 앱 설치를 요구합니다.",
        30,
        listOf("앱\\s*설치|어플\\s*설치|원격\\s*제어|팀뷰어|애니데스크|anydesk|teamviewer|apk").map { Regex(it, RegexOption.IGNORE_CASE) }
    ),
    LINK(
        "링크 클릭 유도",
        "문자 안의 링크를 누르게 하여 피싱 페이지로 보낼 수 있습니다.",
        18,
        listOf("https?://\\S+|www\\.\\S+|bit\\.ly|tinyurl|카카오\\s*링크|링크\\s*클릭").map { Regex(it, RegexOption.IGNORE_CASE) }
    ),
    BAIT(
        "대출/지원금/환급 미끼",
        "저금리 대출, 정부지원금, 환급을 미끼로 개인정보나 돈을 요구할 수 있습니다.",
        18,
        listOf("저금리|대출|정부\\s*지원|지원금|환급|보상금|당첨|수수료").map { Regex(it) }
    ),
    CREDENTIALS(
        "계좌/비밀번호/OTP 요구",
        "계좌, 비밀번호, 인증번호, OTP 같은 민감정보를 요구합니다.",
        30,
        listOf("계좌번호|비밀번호|패스워드|인증번호|otp|보안카드|주민등록번호|신분증").map { Regex(it, RegexOption.IGNORE_CASE) }
    ),
    FAMILY_EMERGENCY(
        "가족 긴급상황/목소리 사칭",
        "가족 사고, 납치, 휴대폰 고장, 목소리 사칭으로 확인 전 송금을 유도합니다.",
        28,
        listOf("(?=.*(엄마|아빠|아들|딸|손자|손녀|가족))(?=.*(사고|납치|응급실|휴대폰\\s*고장|폰\\s*고장|목소리|딥페이크)).*").map { Regex(it) }
    ),
    GIFT_CRYPTO(
        "상품권/가상자산 요구",
        "상품권 번호나 코인 송금을 요구합니다.",
        25,
        listOf("상품권|기프트카드|문화상품권|구글\\s*플레이|비트코인|코인|가상자산|crypto").map { Regex(it, RegexOption.IGNORE_CASE) }
    ),
    SECRECY(
        "비밀 유지 압박",
        "가족이나 은행 직원에게 알리지 말라고 압박합니다.",
        25,
        listOf("비밀|말하지\\s*마|알리지\\s*마|혼자만|통화\\s*끊지\\s*마|누구에게도").map { Regex(it) }
    ),
    PHONE_NUMBER_CHANGE(
        "가족 번호 변경 사칭",
        "가족이 휴대폰 번호가 바뀌었다며 새 번호로만 연락하라고 할 수 있습니다.",
        22,
        listOf("번호\\s*바뀌었|번호\\s*바꿨|이\\s*번호로\\s*(저장|연락)|새\\s*번호로\\s*연락").map { Regex(it) }
    ),
    LOAN_REFINANCE_FEE(
        "대환대출/보증료 편취",
        "대환대출, 저금리 전환을 미끼로 보증료나 선입금을 요구할 수 있습니다.",
        22,
        listOf("대환\\s*대출|전환\\s*대출|보증료|선\\s*입금").map { Regex(it) }
    ),
    GIFTICON_PIN(
        "기프티콘/상품권 PIN 요구",
        "기프티콘이나 상품권의 PIN 번호를 사진이나 문자로 요구할 수 있습니다.",
        25,
        listOf("기프티콘|핀\\s*번호|pin\\s*번호|상품권\\s*번호").map { Regex(it, RegexOption.IGNORE_CASE) }
    ),
    LIVE_CALL_CONTROL(
        "통화 유지/화면공유 압박",
        "통화를 끊지 못하게 하거나 화면공유·원격 확인을 요구할 수 있습니다.",
        24,
        listOf(
            "통화\\s*(끊지|종료하지)\\s*(마|말)",
            "끊지\\s*말고",
            "화면\\s*공유",
            "스피커폰",
            "원격\\s*확인",
            "보안\\s*앱"
        ).map { Regex(it, RegexOption.IGNORE_CASE) }
    ),
    DEEPFAKE_VOICE(
        "딥페이크/음성 복제 의심",
        "목소리를 복제하거나 AI로 합성한 음성으로 가족을 사칭할 수 있습니다.",
        22,
        listOf(
            "목소리\\s*복제", "음성\\s*복제", "목소리\\s*합성", "음성\\s*합성",
            "ai\\s*음성", "ai\\s*목소리", "보이스\\s*피싱", "딥페이크\\s*(목소리|영상|통화)?"
        ).map { Regex(it, RegexOption.IGNORE_CASE) }
    ),
    SAFE_ACCOUNT_FRAUD(
        "명의도용/안전계좌 편취",
        "명의 도용, 자금세탁 연루, 안전계좌 확인을 명목으로 계좌 이체를 요구할 수 있습니다.",
        26,
        listOf("명의\\s*도용|계좌\\s*보호|자금\\s*세탁\\s*연루|안전\\s*계좌|대포\\s*통장").map { Regex(it) }
    ),
    PARCEL_INVITATION_SMISHING(
        "택배/청첩장/부고 링크",
        "택배 배송, 모바일 청첩장, 부고 알림을 사칭한 문자로 링크 클릭을 유도할 수 있습니다.",
        18,
        listOf("택배\\s*(배송|조회|주소|도착)|모바일\\s*청첩장|청첩장|부고장|부고\\s*(알림|안내)").map { Regex(it) }
    ),
    OVERSEAS_PAYMENT_ALERT(
        "해외/소액 결제 승인 알림",
        "소액 결제나 해외 결제 승인 문자로 놀라게 한 뒤 인증번호나 확인 전화를 유도할 수 있습니다.",
        20,
        listOf("해외\\s*결제|소액\\s*결제|결제\\s*승인|해외\\s*(발급|사용)\\s*승인").map { Regex(it) }
    )
}

data class ScamAnalysis(
    val input: String,
    val score: Int,
    val riskLevel: RiskLevel,
    val matchedSignals: List<ScamSignal>
) {
    val summary: String
        get() = when (riskLevel) {
            RiskLevel.HIGH -> "고위험 신호가 겹쳤습니다. 송금, 앱 설치, 정보 제공을 멈추고 가족에게 직접 확인하세요."
            RiskLevel.MEDIUM -> "주의 신호가 있습니다. 링크를 누르거나 돈을 보내기 전에 가족 확인 루틴을 진행하세요."
            RiskLevel.SAFE -> "강한 사기 신호는 적습니다. 그래도 민감정보와 송금 요청은 직접 확인하세요."
        }

    val guardianAdvice: GuardianAdvice
        get() = GuardianAdvisor.advise(this)

    val actionPlan: ActionPlan
        get() = ActionPlanBuilder.build(this)
}

data class GuardianAdvice(
    val verdict: String,
    val immediateActions: List<String>,
    val callbackPrompt: String
)

object GuardianAdvisor {
    fun advise(analysis: ScamAnalysis): GuardianAdvice {
        val signals = analysis.matchedSignals.toSet()
        val actions = linkedSetOf<String>()

        val verdict = when (analysis.riskLevel) {
            RiskLevel.HIGH -> "AI 보호자 판단: 지금 멈춤. 송금·앱설치·인증번호 제공 금지."
            RiskLevel.MEDIUM -> "AI 보호자 판단: 확인 전 보류. 링크·송금·개인정보 입력 금지."
            RiskLevel.SAFE -> "AI 보호자 판단: 강한 위험 신호는 적지만, 돈이나 인증번호 요청은 직접 확인."
        }

        if (ScamSignal.FAMILY_EMERGENCY in signals || ScamSignal.DEEPFAKE_VOICE in signals || ScamSignal.PHONE_NUMBER_CHANGE in signals) {
            actions += "전화를 끊고 기존에 저장된 가족 번호로 직접 다시 전화하세요. 새 번호로 확인하지 마세요."
        }
        if (ScamSignal.URGENT_TRANSFER in signals || ScamSignal.GIFT_CRYPTO in signals || ScamSignal.GIFTICON_PIN in signals) {
            actions += "송금, 상품권 구매, 기프티콘/PIN 전달을 즉시 멈추세요."
        }
        if (ScamSignal.APP_INSTALL in signals || ScamSignal.LIVE_CALL_CONTROL in signals) {
            actions += "통화 유지·화면공유·원격제어·APK/보안앱 설치 요구는 거절하세요."
        }
        if (ScamSignal.CREDENTIALS in signals) {
            actions += "계좌번호, 비밀번호, OTP, 신분증 사진을 말하거나 보내지 마세요."
        }
        if (ScamSignal.LINK in signals || ScamSignal.BAIT in signals || ScamSignal.LOAN_REFINANCE_FEE in signals) {
            actions += "문자 링크 대신 은행·정부기관 공식 앱이나 대표번호로 직접 확인하세요."
        }
        if (ScamSignal.IMPERSONATION in signals) {
            actions += "검찰·경찰·은행을 자칭하면 기관 대표번호로 끊고 다시 거세요."
        }
        if (ScamSignal.SECRECY in signals) {
            actions += "‘비밀로 해라’는 말이 나오면 가족 단톡방이나 보호자에게 바로 공유하세요."
        }
        if (ScamSignal.SAFE_ACCOUNT_FRAUD in signals) {
            actions += "검찰·경찰·금감원은 전화로 ‘안전계좌’, ‘명의도용 확인’을 이유로 계좌 이체를 요구하지 않습니다. 이체하지 마세요."
        }
        if (ScamSignal.PARCEL_INVITATION_SMISHING in signals) {
            actions += "택배·청첩장·부고 문자 속 링크는 누르지 말고 발신자에게 직접 확인하세요."
        }
        if (ScamSignal.OVERSEAS_PAYMENT_ALERT in signals) {
            actions += "해외/소액 결제 승인 알림에 놀라 인증번호를 부르지 말고 카드사 공식 앱·대표번호로 확인하세요."
        }
        if (actions.isEmpty()) {
            actions += "대화가 돈·인증번호·앱설치로 이어지면 다시 검사하고 가족 확인 루틴을 실행하세요."
        }

        val callbackPrompt = when {
            ScamSignal.DEEPFAKE_VOICE in signals || ScamSignal.FAMILY_EMERGENCY in signals ->
                "안전 질문: 우리 가족만 아는 별명/장소를 물어보고, 대답이 애매하면 통화를 종료하세요."
            ScamSignal.PHONE_NUMBER_CHANGE in signals ->
                "확인 질문: 예전 번호로 직접 전화해 ‘번호 변경’ 사실을 확인하기 전에는 새 번호를 저장하지 마세요."
            else ->
                "확인 질문: 지금 요청을 기존 가족 번호나 공식 대표번호로 다시 확인하세요."
        }

        return GuardianAdvice(verdict, actions.toList(), callbackPrompt)
    }
}

object ScamAnalyzer {
    fun analyze(rawText: String): ScamAnalysis {
        val normalized = rawText.trim().lowercase()
        if (normalized.isBlank()) {
            return ScamAnalysis(rawText, 0, RiskLevel.SAFE, emptyList())
        }

        val matches = ScamSignal.entries.filter { signal ->
            signal.patterns.any { it.containsMatchIn(normalized) }
        }

        val synergy = listOf(
            ScamSignal.URGENT_TRANSFER to ScamSignal.IMPERSONATION,
            ScamSignal.URGENT_TRANSFER to ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.APP_INSTALL to ScamSignal.CREDENTIALS,
            ScamSignal.LINK to ScamSignal.CREDENTIALS,
            ScamSignal.SECRECY to ScamSignal.URGENT_TRANSFER,
            ScamSignal.PHONE_NUMBER_CHANGE to ScamSignal.URGENT_TRANSFER,
            ScamSignal.DEEPFAKE_VOICE to ScamSignal.URGENT_TRANSFER,
            ScamSignal.DEEPFAKE_VOICE to ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.LIVE_CALL_CONTROL to ScamSignal.APP_INSTALL,
            ScamSignal.LIVE_CALL_CONTROL to ScamSignal.CREDENTIALS,
            ScamSignal.GIFTICON_PIN to ScamSignal.SECRECY,
            ScamSignal.SAFE_ACCOUNT_FRAUD to ScamSignal.IMPERSONATION,
            ScamSignal.SAFE_ACCOUNT_FRAUD to ScamSignal.URGENT_TRANSFER,
            ScamSignal.PARCEL_INVITATION_SMISHING to ScamSignal.LINK,
            ScamSignal.OVERSEAS_PAYMENT_ALERT to ScamSignal.CREDENTIALS
        ).count { (a, b) -> a in matches && b in matches } * 10

        val score = (matches.sumOf { it.score } + synergy).coerceAtMost(100)
        val level = when {
            score >= 70 -> RiskLevel.HIGH
            score >= 30 -> RiskLevel.MEDIUM
            else -> RiskLevel.SAFE
        }

        return ScamAnalysis(rawText, score, level, matches)
    }
}
