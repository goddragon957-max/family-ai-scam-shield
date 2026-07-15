package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SharedTextParserTest {
    @Test
    fun textOnlyShareIsTrimmedAndReturned() {
        val result = SharedTextParser.composeSharedText(
            text = "  엄마 나 폰 고장났어. 지금 바로 계좌로 돈 보내줘.  ",
            subject = null
        )

        assertEquals("엄마 나 폰 고장났어. 지금 바로 계좌로 돈 보내줘.", result)
    }

    @Test
    fun subjectAndBodyAreCombinedForAnalysis() {
        val result = SharedTextParser.composeSharedText(
            text = "통화 끊지 말고 보안앱 설치해 주세요.",
            subject = "은행 보안팀"
        )

        assertEquals("은행 보안팀\n\n통화 끊지 말고 보안앱 설치해 주세요.", result)
    }

    @Test
    fun blankShareReturnsNull() {
        val result = SharedTextParser.composeSharedText(text = "   ", subject = "  ")

        assertNull(result)
    }
}
