package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OfficialHelpDirectoryBuilderTest {
    @Test
    fun containsRequiredEntriesInStableOrder() {
        val directory = OfficialHelpDirectoryBuilder.build()

        assertEquals(directory, OfficialHelpDirectoryBuilder.build())
        assertEquals(
            listOf(
                OfficialHelpEntryId.POLICE_112,
                OfficialHelpEntryId.VOICE_PHISHING_1394,
                OfficialHelpEntryId.FINANCIAL_SUPERVISORY_SERVICE_1332,
                OfficialHelpEntryId.BANK_CARD_OFFICIAL
            ),
            directory.entries.map { it.id }
        )
        assertEquals(listOf("112", "1394", "1332", null), directory.entries.map { it.dialNumber })
        assertEquals(
            listOf(
                "경찰 긴급 신고",
                "보이스피싱 통합신고·상담",
                "금융감독원",
                "은행·카드사 공식 대표번호"
            ),
            directory.entries.map { it.label }
        )
    }

    @Test
    fun onlyOfficialNumberEntriesAreDialable() {
        val entries = OfficialHelpDirectoryBuilder.build().entries

        assertEquals(
            listOf(
                OfficialHelpEntryId.POLICE_112,
                OfficialHelpEntryId.VOICE_PHISHING_1394,
                OfficialHelpEntryId.FINANCIAL_SUPERVISORY_SERVICE_1332
            ),
            entries.filter { it.dialNumber != null }.map { it.id }
        )
        val bankOrCard = entries.single { it.id == OfficialHelpEntryId.BANK_CARD_OFFICIAL }
        assertNull(bankOrCard.dialNumber)
        assertTrue(bankOrCard.whenToUse.contains("공식 앱"))
        assertTrue(bankOrCard.whenToUse.contains("카드 뒷면"))
        assertTrue(bankOrCard.whenToUse.contains("공식 웹사이트"))
        assertTrue(bankOrCard.safetyNotes.any { it.contains("메시지") || it.contains("문자") })
    }

    @Test
    fun dialNumbersContainAsciiDigitsOnlyAndGuidanceEntryHasNone() {
        val entries = OfficialHelpDirectoryBuilder.build().entries

        entries.filter { it.dialNumber != null }.forEach { entry ->
            val number = requireNotNull(entry.dialNumber)
            assertTrue(number.isNotBlank())
            assertTrue(number.all { it in '0'..'9' })
        }
        entries.filter { it.id == OfficialHelpEntryId.BANK_CARD_OFFICIAL }.forEach { entry ->
            assertNull(entry.dialNumber)
        }
    }

    @Test
    fun everyEntryHasCompleteWhatToSayScriptsAndSafetyNotes() {
        OfficialHelpDirectoryBuilder.build().entries.forEach { entry ->
            assertTrue(entry.label.isNotBlank())
            assertTrue(entry.whenToUse.isNotBlank())
            assertTrue(entry.whatToSay.isNotEmpty())
            assertTrue(entry.whatToSay.all { it.isNotBlank() })
            assertTrue(entry.safetyNotes.isNotEmpty())
            assertTrue(entry.safetyNotes.all { it.isNotBlank() })
        }
    }

    @Test
    fun familyShareSummaryContainsOfficialNumbersWithoutRawScamText() {
        val summary = OfficialHelpDirectoryBuilder.build().familyShareSummary
        val rawScamPlaceholder = "엄마, 지금 300만원을 보내고 OTP 123456을 알려줘"
        val rawFragments = listOf("엄마,", "300만원", "123456", "short.example")

        assertTrue(summary.contains("112"))
        assertTrue(summary.contains("1394"))
        assertTrue(summary.contains("1332"))
        assertTrue(summary.contains("말할 내용"))
        assertTrue(summary.contains("원문") && summary.contains("포함하지 않았습니다"))
        OfficialHelpDirectoryBuilder.build().entries.flatMap { it.whatToSay }.forEach { script ->
            assertTrue("summary omitted script: $script", summary.contains(script))
        }
        assertFalse(summary.contains(rawScamPlaceholder))
        rawFragments.forEach { fragment ->
            assertFalse("summary contained raw-text fragment: $fragment", summary.contains(fragment))
        }
    }

    @Test
    fun directoryCopyNeverClaimsMonitoringUploadOrLegalGuarantee() {
        val directory = OfficialHelpDirectoryBuilder.build()
        val allCopy = buildString {
            append(directory.title)
            append(directory.intro)
            directory.entries.forEach { entry ->
                append(entry.label)
                append(entry.dialNumber)
                append(entry.whenToUse)
                entry.whatToSay.forEach(::append)
                entry.safetyNotes.forEach(::append)
            }
            append(directory.familyShareSummary)
        }
        val forbiddenClaims = listOf(
            "자동으로 감시",
            "자동 모니터링",
            "몰래 확인",
            "자동으로 읽",
            "자동 수집",
            "서버로 업로드",
            "네트워크 업로드",
            "네트워크로 전송",
            "법적 효력을 보장",
            "법적 결과를 보장",
            "피해 회복을 보장",
            "반드시 환급",
            "100%"
        )

        forbiddenClaims.forEach { phrase ->
            assertFalse("directory contained unsafe claim: $phrase", allCopy.contains(phrase))
        }
    }
}
