package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IncidentPreviewRedactorTest {
    @Test
    fun phoneNumbersAreMaskedWithoutLeakingDigits() {
        val domestic = IncidentPreviewRedactor.redact("새 번호 010-1234-5678로 연락해")
        val international = IncidentPreviewRedactor.redact("콜백은 +82 10 9876 5432로 해주세요")
        val parenthesized = IncidentPreviewRedactor.redact("전화 (010) 2222-3333으로 확인")

        assertEquals("새 번호 [전화번호 숨김]로 연락해", domestic)
        assertEquals("콜백은 [전화번호 숨김]로 해주세요", international)
        assertEquals("전화 [전화번호 숨김]으로 확인", parenthesized)
        assertFalse(domestic.contains("010-1234-5678"))
        assertFalse(international.contains("9876 5432"))
        assertFalse(parenthesized.any(Char::isDigit))
    }

    @Test
    fun accountLikeLongDigitRunsAreMaskedButContextRemains() {
        val preview = IncidentPreviewRedactor.redact("국민 123-456-789012 계좌로 지금 송금")

        assertEquals("국민 [긴 숫자 숨김] 계좌로 지금 송금", preview)
        assertFalse(preview.contains("123-456-789012"))
    }

    @Test
    fun authenticationCodesAreMasked() {
        val numeric = IncidentPreviewRedactor.redact("인증번호 482913을 알려줘")
        val alphaNumeric = IncidentPreviewRedactor.redact("OTP: A1B2C3 입력 요청")
        val spaced = IncidentPreviewRedactor.redact("인증코드를 482 913 입력하세요")
        val groupedAlphaNumeric = IncidentPreviewRedactor.redact("OTP 번호 A1 B2 C3 입력")

        assertEquals("[인증번호 숨김]을 알려줘", numeric)
        assertEquals("[인증번호 숨김] 입력 요청", alphaNumeric)
        assertEquals("[인증번호 숨김] 입력하세요", spaced)
        assertEquals("[인증번호 숨김] 입력", groupedAlphaNumeric)
        assertFalse(numeric.contains("482913"))
        assertFalse(alphaNumeric.contains("A1B2C3"))
        assertFalse(spaced.any(Char::isDigit))
        assertFalse(groupedAlphaNumeric.contains("A1 B2 C3"))
    }

    @Test
    fun urlsBareDomainsAndEmailDomainsAreMasked() {
        val preview = IncidentPreviewRedactor.redact(
            "https://evil.example.com/pay 또는 secure-bank.co.kr/login, help@fake.example 확인"
        )
        val alternateLinks = IncidentPreviewRedactor.redact(
            "intent://fake.example/pay, 사기.한국/pay, 1.2.3.4/login"
        )

        assertEquals("[링크 숨김] 또는 [링크 숨김] [링크 숨김] 확인", preview)
        assertEquals("[링크 숨김] [링크 숨김] [링크 숨김]", alternateLinks)
        assertFalse(preview.contains("evil.example.com"))
        assertFalse(preview.contains("secure-bank.co.kr"))
        assertFalse(preview.contains("help@fake.example"))
        assertFalse(alternateLinks.contains("사기.한국"))
        assertFalse(alternateLinks.any(Char::isDigit))
    }

    @Test
    fun residentPassportAndDriverLicenseNumbersAreMasked() {
        val preview = IncidentPreviewRedactor.redact(
            "주민번호 900101-1234567, 여권번호 M123A4567, 면허 12-34-567890-12"
        )

        assertEquals("[신분정보 숨김], [신분정보 숨김], 면허 [신분정보 숨김]", preview)
        listOf("900101-1234567", "M123A4567", "12-34-567890-12").forEach { sensitive ->
            assertFalse("preview contained identity value: $sensitive", preview.contains(sensitive))
        }
        assertFalse(preview.any(Char::isDigit))
    }

    @Test
    fun newlineNormalizationAndTrimmingAreDeterministic() {
        val normalized = "첫 줄 둘째 줄 " + "가".repeat(100)
        val raw = "첫 줄\r\n둘째 줄\n" + "가".repeat(100)
        val expected = normalized.take(IncidentPreviewRedactor.MAX_PREVIEW_LENGTH - 1) + "…"

        val preview = IncidentPreviewRedactor.redact(raw)

        assertEquals(expected, preview)
        assertEquals(IncidentPreviewRedactor.MAX_PREVIEW_LENGTH, preview.length)
        assertEquals(preview, IncidentPreviewRedactor.redact(raw))
        assertFalse(preview.contains('\n'))
        assertFalse(preview.contains('\r'))
    }

    @Test
    fun redactionRunsBeforeTrimmingSoBoundarySecretsDoNotLeak() {
        val account = "1234567890123456"
        val preview = IncidentPreviewRedactor.redact("가".repeat(75) + " $account")

        assertTrue(preview.length <= IncidentPreviewRedactor.MAX_PREVIEW_LENGTH)
        assertFalse(preview.contains(account))
        assertFalse(preview.any(Char::isDigit))
        assertFalse(preview.contains("[긴 숫자 숨"))
    }

    @Test
    fun sensitiveOnlyAndInvisibleInputsAlwaysHaveNonblankFallbacks() {
        assertEquals("[긴 숫자 숨김]", IncidentPreviewRedactor.redact("123456789012"))
        assertEquals(
            IncidentPreviewRedactor.FALLBACK_PREVIEW,
            IncidentPreviewRedactor.redact("\u200B\u2060\uFEFF")
        )
        assertTrue(IncidentPreviewRedactor.redact("123456789012").isNotBlank())
    }

    @Test
    fun legacyPreviewSanitizationIsIdempotent() {
        val legacy = "연락처 010-2222-3333 계좌 123-456-789012 인증번호 482913"
        val sanitized = IncidentPreviewRedactor.redact(legacy)

        assertEquals(sanitized, IncidentPreviewRedactor.redact(sanitized))
        listOf("010-2222-3333", "123-456-789012", "482913").forEach { sensitive ->
            assertFalse("sanitized preview contained: $sensitive", sanitized.contains(sensitive))
        }
        assertFalse(sanitized.any(Char::isDigit))
    }

    @Test
    fun truncatedLegacyPhoneAndAuthenticationFragmentsAreMasked() {
        val partialPhone = IncidentPreviewRedactor.redact("이전 기록의 마지막 연락처 010-123")
        val partialAuthenticationCode = IncidentPreviewRedactor.redact("이전 기록의 마지막 인증번호 482")

        assertEquals("이전 기록의 마지막 [전화번호 숨김]", partialPhone)
        assertEquals("이전 기록의 마지막 [인증번호 숨김]", partialAuthenticationCode)
        assertFalse(partialPhone.any(Char::isDigit))
        assertFalse(partialAuthenticationCode.any(Char::isDigit))
    }

    @Test
    fun unicodeDecimalDigitsAreNormalizedBeforeMasking() {
        val arabicIndic = IncidentPreviewRedactor.redact("전화번호 ٠١٠-١٢٣٤-٥٦٧٨")
        val fullWidth = IncidentPreviewRedactor.redact("전화번호 ０１０-１２３４-５６７８")

        assertEquals("전화번호 [전화번호 숨김]", arabicIndic)
        assertEquals("전화번호 [전화번호 숨김]", fullWidth)
        assertFalse(arabicIndic.any(Char::isDigit))
        assertFalse(fullWidth.any(Char::isDigit))
    }

    @Test
    fun analysisBuilderAddsAStableIncidentHintWithoutRawSecrets() {
        val analysis = ScamAnalyzer.analyze("인증번호 482913을 알려줘")

        val preview = IncidentPreviewRedactor.build(analysis)

        assertEquals("계좌/비밀번호/OTP 요구 · [인증번호 숨김]을 알려줘", preview)
        assertEquals(preview, IncidentPreviewRedactor.build(analysis))
        assertFalse(preview.contains("482913"))
    }
}
