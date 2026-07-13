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
            ScamSignal.SECRECY to ScamSignal.URGENT_TRANSFER
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
