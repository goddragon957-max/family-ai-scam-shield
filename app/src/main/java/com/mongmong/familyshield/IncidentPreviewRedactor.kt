package com.mongmong.familyshield

object IncidentPreviewRedactor {
    const val MAX_PREVIEW_LENGTH = 80
    const val FALLBACK_PREVIEW = "가림 처리된 의심 내용"

    private const val MASKED_PHONE = "[전화번호 숨김]"
    private const val MASKED_LONG_NUMBER = "[긴 숫자 숨김]"
    private const val MASKED_AUTH_CODE = "[인증번호 숨김]"
    private const val MASKED_LINK = "[링크 숨김]"
    private const val MASKED_IDENTITY = "[신분정보 숨김]"
    private const val AUTH_KEYWORD_PATTERN =
        "(?:(?:인증|확인|보안|승인)\\s*(?:번호|코드)|(?:일회용\\s*)?비밀번호|(?:otp|pin)(?:\\s*(?:번호|코드))?|authentication\\s*code|verification\\s*code)"
    private const val AUTH_TOKEN_PATTERN =
        "(?:[a-z0-9](?:[a-z0-9-]{2,10}[a-z0-9])|[a-z0-9]{1,4}(?:[ -][a-z0-9]{1,4}){1,5})"
    private const val MOBILE_PREFIX_PATTERN = "(?:0?1[016789])"
    private const val AREA_PREFIX_PATTERN = "(?:(?:0?(?:2|3[1-3]|4[1-4]|5[1-5]|6[1-4]|70|80))|(?:0?50\\d))"

    private val maskLabels = listOf(
        MASKED_PHONE,
        MASKED_LONG_NUMBER,
        MASKED_AUTH_CODE,
        MASKED_LINK,
        MASKED_IDENTITY
    )

    private val lineBreakPattern = Regex("[\\r\\n\\u2028\\u2029]+")
    private val whitespacePattern = Regex("[\\s\\p{Z}]+")
    private val emailPattern = Regex(
        """(?<![\p{L}\p{N}._%+-])[\p{L}\p{N}._%+-]+@(?:[\p{L}\p{N}](?:[\p{L}\p{N}-]{0,61}[\p{L}\p{N}])?\.)+(?:[\p{L}]{2,63}|xn--[a-z0-9-]{2,59})(?![\p{L}\p{N}-])""",
        RegexOption.IGNORE_CASE
    )
    private val urlPattern = Regex(
        """(?:[a-z][a-z0-9+.-]*://|www\.)[^\s<>()\[\]{}]+""",
        RegexOption.IGNORE_CASE
    )
    private val ipv4Pattern = Regex(
        """(?<!\d)(?:(?:25[0-5]|2[0-4]\d|1?\d?\d)\.){3}(?:25[0-5]|2[0-4]\d|1?\d?\d)(?::\d{1,5})?(?:[/?:#][^\s<>()\[\]{}]*)?(?!\d)"""
    )
    private val bareDomainPattern = Regex(
        """(?<![\p{L}\p{N}_@.-])(?:[\p{L}\p{N}](?:[\p{L}\p{N}-]{0,61}[\p{L}\p{N}])?\.)+(?:[\p{L}]{2,63}|xn--[a-z0-9-]{2,59})(?:[/?:#][^\s<>()\[\]{}]*)?""",
        RegexOption.IGNORE_CASE
    )
    private val labeledIdentityPattern = Regex(
        """(?:주민(?:등록)?번호|외국인(?:등록)?번호|운전면허(?:번호)?|신분증(?:번호)?|여권(?:번호)?|passport(?:\s*(?:no\.?|number))?)\s*(?:[:=]|은|는|이|가|을|를)?\s*(?:[a-z0-9][a-z0-9-]{5,19}|\d(?:[\d -]{4,18}\d))""",
        RegexOption.IGNORE_CASE
    )
    private val residentNumberPattern = Regex("""(?<!\d)\d{6}\s*-?\s*[1-8]\d{6}(?!\d)""")
    private val driverLicensePattern = Regex("""(?<!\d)\d{2}\s*-\s*\d{2}\s*-\s*\d{6}\s*-\s*\d{2}(?!\d)""")
    private val authenticationCodePattern = Regex(
        "$AUTH_KEYWORD_PATTERN\\s*(?:[:=]|은|는|이|가|을|를)?\\s*$AUTH_TOKEN_PATTERN(?![a-z0-9])",
        RegexOption.IGNORE_CASE
    )
    private val trailingAuthenticationCodePattern = Regex(
        "(?<![a-z0-9])$AUTH_TOKEN_PATTERN\\s*(?:은|는|이|가|을|를)?\\s*$AUTH_KEYWORD_PATTERN(?![a-z0-9])",
        RegexOption.IGNORE_CASE
    )
    private val partialAuthenticationCodePattern = Regex(
        "$AUTH_KEYWORD_PATTERN\\s*(?:[:=]|은|는|이|가|을|를)?\\s*[a-z0-9](?:[a-z0-9 -]{0,2})$",
        RegexOption.IGNORE_CASE
    )
    private val passportPattern = Regex(
        """(?<![a-z0-9])[a-z]{1,2}\d{7,8}(?![a-z0-9])""",
        RegexOption.IGNORE_CASE
    )
    private val phonePattern = Regex(
        "(?<!\\d)(?:(?:(?:\\+?82[\\s.-]?)?$MOBILE_PREFIX_PATTERN|\\(\\s*$MOBILE_PREFIX_PATTERN\\s*\\))[\\s.-]?\\d{3,4}[\\s.-]?\\d{4}|(?:(?:\\+?82[\\s.-]?)?$AREA_PREFIX_PATTERN|\\(\\s*$AREA_PREFIX_PATTERN\\s*\\))[\\s.-]?\\d{3,4}[\\s.-]?\\d{4}|(?:15|16|18)\\d{2}[\\s.-]?\\d{4})(?!\\d)"
    )
    private val partialPhonePattern = Regex(
        """(?:전화(?:번호)?|연락처|휴대폰(?:번호)?|핸드폰(?:번호)?)\s*(?:[:=]|은|는|이|가|을|를)?\s*\+?\d[\d()\s.-]{2,}$"""
    )
    private val longNumberPattern = Regex("""(?<!\d)\d(?:[\s./-]?\d){6,}(?!\d)""")
    private val isolatedCodePattern = Regex("""(?<!\d)\d{4,6}(?!\d)""")

    fun build(analysis: ScamAnalysis): String {
        val incidentHint = analysis.matchedSignals.firstOrNull()?.label ?: "일반 확인"
        return limit("$incidentHint · ${redactWithoutLimit(analysis.input)}")
    }

    fun buildPrivacySafeClue(analysis: ScamAnalysis): String {
        val incidentHint = analysis.matchedSignals.firstOrNull()?.label ?: "일반 확인"
        val normalizedInput = normalize(analysis.input)
        val redactedInput = redactWithoutLimit(analysis.input)
        val candidate = "$incidentHint · $redactedInput"
        val maskedSensitiveValue = redactedInput != normalizedInput && maskLabels.any(redactedInput::contains)

        return if (maskedSensitiveValue || candidate.length > MAX_PREVIEW_LENGTH) {
            limit(candidate)
        } else {
            "$incidentHint · 붙여넣은 원문 비공개"
        }
    }

    fun redact(rawText: String): String = limit(redactWithoutLimit(rawText))

    private fun redactWithoutLimit(rawText: String): String {
        var redacted = normalize(rawText)
        if (redacted.isBlank()) return FALLBACK_PREVIEW

        redacted = emailPattern.replace(redacted, MASKED_LINK)
        redacted = urlPattern.replace(redacted, MASKED_LINK)
        redacted = ipv4Pattern.replace(redacted, MASKED_LINK)
        redacted = bareDomainPattern.replace(redacted, MASKED_LINK)
        redacted = labeledIdentityPattern.replace(redacted, MASKED_IDENTITY)
        redacted = driverLicensePattern.replace(redacted, MASKED_IDENTITY)
        redacted = residentNumberPattern.replace(redacted, MASKED_IDENTITY)
        redacted = authenticationCodePattern.replace(redacted, MASKED_AUTH_CODE)
        redacted = trailingAuthenticationCodePattern.replace(redacted, MASKED_AUTH_CODE)
        redacted = partialAuthenticationCodePattern.replace(redacted, MASKED_AUTH_CODE)
        redacted = passportPattern.replace(redacted, MASKED_IDENTITY)
        redacted = phonePattern.replace(redacted, MASKED_PHONE)
        redacted = partialPhonePattern.replace(redacted, MASKED_PHONE)
        redacted = longNumberPattern.replace(redacted, MASKED_LONG_NUMBER)
        redacted = isolatedCodePattern.replace(redacted, MASKED_AUTH_CODE)

        return collapseWhitespace(redacted).ifBlank { FALLBACK_PREVIEW }
    }

    private fun normalize(rawText: String): String {
        val normalizedCharacters = buildString(rawText.length) {
            var index = 0
            while (index < rawText.length) {
                val codePoint = rawText.codePointAt(index)
                val decimalDigit = Character.digit(codePoint, 10)
                when {
                    codePoint in 0xFF01..0xFF5E -> append((codePoint - 0xFEE0).toChar())
                    decimalDigit >= 0 -> append(('0'.code + decimalDigit).toChar())
                    codePoint in unicodeDashCodePoints -> append('-')
                    codePoint in 0x200B..0x200D ||
                        codePoint == 0x2060 ||
                        codePoint == 0xFEFF ||
                        codePoint in 0x202A..0x202E ||
                        codePoint in 0x2066..0x2069 -> Unit
                    else -> appendCodePoint(codePoint)
                }
                index += Character.charCount(codePoint)
            }
        }
        return collapseWhitespace(lineBreakPattern.replace(normalizedCharacters, " "))
    }

    private fun collapseWhitespace(text: String): String = whitespacePattern.replace(text, " ").trim()

    private fun limit(text: String): String {
        val normalized = collapseWhitespace(text).ifBlank { FALLBACK_PREVIEW }
        if (normalized.length <= MAX_PREVIEW_LENGTH) return normalized

        var safeEnd = MAX_PREVIEW_LENGTH - 1
        maskLabels.forEach { label ->
            var maskStart = normalized.indexOf(label)
            while (maskStart >= 0) {
                if (maskStart < safeEnd && maskStart + label.length > safeEnd) {
                    safeEnd = maskStart
                }
                maskStart = normalized.indexOf(label, maskStart + label.length)
            }
        }

        var shortened = normalized.take(safeEnd).trimEnd()
        if (shortened.lastOrNull()?.let(Character::isHighSurrogate) == true) {
            shortened = shortened.dropLast(1)
        }
        return "$shortened…"
    }

    private val unicodeDashCodePoints = setOf(0x2010, 0x2011, 0x2012, 0x2013, 0x2014, 0x2015, 0x2212)
}
