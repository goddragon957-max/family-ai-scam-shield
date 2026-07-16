package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OfficialReportPrepBuilderTest {
    @Test
    fun highRiskTransferOtpRemoteControlAndSafeAccountRecommendsAllOfficialHelp() {
        val analysis = ScamAnalyzer.analyze(
            "은행 보안팀입니다. 안전계좌로 지금 바로 송금하고 원격제어 앱 설치 후 OTP 482913을 알려주세요."
        )

        val prep = OfficialReportPrepBuilder.build(analysis)

        assertEquals(RiskLevel.HIGH, analysis.riskLevel)
        assertTrue(ScamSignal.URGENT_TRANSFER in analysis.matchedSignals)
        assertTrue(ScamSignal.CREDENTIALS in analysis.matchedSignals)
        assertTrue(ScamSignal.APP_INSTALL in analysis.matchedSignals)
        assertTrue(ScamSignal.SAFE_ACCOUNT_FRAUD in analysis.matchedSignals)
        assertEquals(OfficialHelpDirectoryBuilder.build().entries, prep.recommendedEntries)
        assertEquals(
            listOf(
                OfficialHelpEntryId.POLICE_112,
                OfficialHelpEntryId.VOICE_PHISHING_1394,
                OfficialHelpEntryId.FINANCIAL_SUPERVISORY_SERVICE_1332,
                OfficialHelpEntryId.BANK_CARD_OFFICIAL
            ),
            prep.recommendedEntryIds
        )
        assertEquals(listOf("112", "1394", "1332", null), prep.recommendedEntries.map { it.dialNumber })
        assertTrue(prep.beforeCallChecklist.any { it.contains("중단") })
        assertTrue(prep.beforeCallChecklist.any { it.contains("가족 번호") && it.contains("콜백") })
    }

    @Test
    fun linkOnlyAnalysisRecommends1394WithoutAssumingFinancialExposure() {
        val analysis = ScamAnalyzer.analyze("배송 상태 확인은 https://short.example/check 에서 해주세요.")

        val prep = OfficialReportPrepBuilder.build(analysis)

        assertEquals(RiskLevel.SAFE, analysis.riskLevel)
        assertEquals(listOf(ScamSignal.LINK), analysis.matchedSignals)
        assertEquals(
            listOf(OfficialHelpEntryId.VOICE_PHISHING_1394),
            prep.recommendedEntryIds
        )
        assertFalse(OfficialHelpEntryId.POLICE_112 in prep.recommendedEntryIds)
        assertFalse(OfficialHelpEntryId.FINANCIAL_SUPERVISORY_SERVICE_1332 in prep.recommendedEntryIds)
        assertFalse(OfficialHelpEntryId.BANK_CARD_OFFICIAL in prep.recommendedEntryIds)
        assertTrue(prep.beforeCallChecklist.first().contains("가족 번호"))
        assertTrue(prep.beforeCallChecklist.any { it.contains("링크를 다시 열지") })
        assertFalse(prep.familyShareSummary.contains("반드시 환급"))
        assertFalse(prep.familyShareSummary.contains("피해금 회수를 보장"))
    }

    @Test
    fun financialPaymentLinkAddsBankCardGuidanceWithoutEscalatingToEmergencyEntries() {
        val analysis = ScamAnalyzer.analyze(
            "해외 결제 승인 확인을 위해 링크 클릭: https://short.example/payment"
        )

        val prep = OfficialReportPrepBuilder.build(analysis)

        assertTrue(ScamSignal.LINK in analysis.matchedSignals)
        assertTrue(ScamSignal.OVERSEAS_PAYMENT_ALERT in analysis.matchedSignals)
        assertEquals(
            listOf(
                OfficialHelpEntryId.VOICE_PHISHING_1394,
                OfficialHelpEntryId.BANK_CARD_OFFICIAL
            ),
            prep.recommendedEntryIds
        )
        assertFalse(OfficialHelpEntryId.POLICE_112 in prep.recommendedEntryIds)
        assertFalse(OfficialHelpEntryId.FINANCIAL_SUPERVISORY_SERVICE_1332 in prep.recommendedEntryIds)
    }

    @Test
    fun lowRiskFallbackStillRecommends1394AndFamilyCallbackFirst() {
        val analysis = ScamAnalyzer.analyze("오늘 저녁 7시에 집에서 밥 먹자. 오는 길에 우유 하나만 사와.")

        val prep = OfficialReportPrepBuilder.build(analysis)

        assertEquals(listOf("강한 위험 신호 없음"), prep.detectedSignalLabels)
        assertEquals(
            listOf(OfficialHelpEntryId.VOICE_PHISHING_1394),
            prep.recommendedEntryIds
        )
        assertTrue(prep.beforeCallChecklist.first().contains("가족 번호"))
        assertTrue(prep.beforeCallChecklist.first().contains("먼저 확인"))
        assertTrue(prep.scriptLines.any { it.contains("공식 상담") && it.contains("다시 확인") })
    }

    @Test
    fun redactedClueMasksEverySensitivePattern() {
        val raw =
            "010-1234-5678 123456789012 OTP A1B2C3 https://evil.example 주민번호 900101-1234567"
        val analysis = ScamAnalyzer.analyze(raw)

        val clue = OfficialReportPrepBuilder.build(analysis).redactedClue

        listOf(
            "[전화번호 숨김]",
            "[긴 숫자 숨김]",
            "[인증번호 숨김]",
            "[링크 숨김]",
            "[신분정보 숨김]"
        ).forEach { mask -> assertTrue("redacted clue omitted $mask: $clue", clue.contains(mask)) }
        listOf(
            "010-1234-5678",
            "123456789012",
            "A1B2C3",
            "evil.example",
            "900101-1234567"
        ).forEach { secret -> assertFalse("redacted clue leaked $secret", clue.contains(secret)) }
        assertEquals(IncidentPreviewRedactor.buildPrivacySafeClue(analysis), clue)
    }

    @Test
    fun shortUnmaskedInputIsNeverRepeatedAsTheFullClue() {
        val raw = "김철수에게 300만원 보내줘"

        val clue = OfficialReportPrepBuilder.build(ScamAnalyzer.analyze(raw)).redactedClue

        assertFalse(clue.contains(raw))
        assertFalse(clue.contains("김철수"))
        assertFalse(clue.contains("300만원"))
        assertTrue(clue.contains("붙여넣은 원문 비공개"))
    }

    @Test
    fun familyShareIncludesRiskSignalsContactsAndScriptsButNoInputSecretsOrPreview() {
        val raw =
            "은행 보안팀 010-9876-5432입니다. 안전계좌 123456789012로 지금 바로 송금하고 " +
                "OTP 482913 입력 후 https://evil.example 접속, 주민번호 900101-1234567을 보내세요."
        val analysis = ScamAnalyzer.analyze(raw)

        val prep = OfficialReportPrepBuilder.build(analysis)
        val summary = prep.familyShareSummary

        assertTrue(summary.contains(prep.riskSummary))
        prep.detectedSignalLabels.forEach { label -> assertTrue(summary.contains(label)) }
        prep.recommendedEntries.forEach { entry ->
            assertTrue(summary.contains(entry.label))
            entry.dialNumber?.let { number -> assertTrue(summary.contains(number)) }
        }
        prep.scriptLines.forEach { line -> assertTrue("share omitted script: $line", summary.contains(line)) }
        prep.recommendedEntries.forEach { entry ->
            assertTrue(prep.scriptLines.any { it.contains(entry.whatToSay.first()) })
        }
        assertFalse(summary.contains(prep.redactedClue))
        assertFalse(summary.contains(raw))
        listOf(
            "010-9876-5432",
            "123456789012",
            "482913",
            "evil.example",
            "900101-1234567"
        ).forEach { secret -> assertFalse("family share leaked $secret", summary.contains(secret)) }
        assertTrue(summary.contains("포함하지 않았습니다"))
    }

    @Test
    fun outputIsDeterministicForTheSameAnalysis() {
        val analysis = ScamAnalyzer.analyze(
            "택배 배송 조회를 위해 링크 클릭: https://short.example/parcel"
        )

        val first = OfficialReportPrepBuilder.build(analysis)
        val second = OfficialReportPrepBuilder.build(analysis)

        assertEquals(first, second)
        assertEquals(first.familyShareSummary, second.familyShareSummary)
        assertEquals(first.recommendedEntryIds, second.recommendedEntryIds)
    }

    @Test
    fun copyStatesLocalManualLimitsAndMakesNoMonitoringUploadOrOutcomePromise() {
        val analyses = listOf(
            ScamAnalyzer.analyze("오늘 저녁에 통화하자."),
            ScamAnalyzer.analyze("택배 링크 클릭 https://short.example/parcel"),
            ScamAnalyzer.analyze("안전계좌로 지금 바로 송금하고 OTP 482913을 알려줘")
        )

        analyses.forEach { analysis ->
            val prep = OfficialReportPrepBuilder.build(analysis)
            val allCopy = buildString {
                append(prep.title)
                append(prep.riskSummary)
                append(prep.redactedClue)
                prep.detectedSignalLabels.forEach(::append)
                prep.recommendedEntries.forEach { entry ->
                    append(entry.label)
                    append(entry.whenToUse)
                    entry.whatToSay.forEach(::append)
                    entry.safetyNotes.forEach(::append)
                }
                prep.scriptLines.forEach(::append)
                prep.beforeCallChecklist.forEach(::append)
                prep.privacySafetyNotes.forEach(::append)
                append(prep.familyShareSummary)
            }
            val forbiddenClaims = listOf(
                "자동으로 감시",
                "자동 모니터링",
                "몰래 확인",
                "자동 수집",
                "서버로 업로드",
                "네트워크 업로드",
                "법적 효력을 보장",
                "법적 결과를 보장",
                "피해 회복을 보장",
                "피해금 회수를 보장",
                "반드시 환급",
                "환불 보장",
                "100%"
            )

            forbiddenClaims.forEach { phrase ->
                assertFalse("prep copy contained unsafe claim: $phrase", allCopy.contains(phrase))
            }
            assertTrue(prep.privacySafetyNotes.any { it.contains("기기 안") })
            assertTrue(prep.privacySafetyNotes.any { it.contains("자동으로 확인") && it.contains("않습니다") })
            assertTrue(prep.privacySafetyNotes.any { it.contains("네트워크로 보내지 않습니다") })
            assertTrue(prep.privacySafetyNotes.any { it.contains("약속이 아닙니다") })
            assertTrue(prep.privacySafetyNotes.any { it.contains("따로 저장하지 않") })
            assertTrue(prep.privacySafetyNotes.any { it.contains("사건 기록") && it.contains("자동으로") })
        }
    }
}
