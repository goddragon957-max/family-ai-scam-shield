package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EvidencePreservationKitBuilderTest {
    @Test
    fun genericKitHasStablePreservationFirstOrder() {
        val kit = EvidencePreservationKitBuilder.build()

        assertNull(kit.situation)
        assertEquals(kit, EvidencePreservationKitBuilder.build(null))
        assertEquals(
            listOf(
                "휴대전화 전원을 유지하고 상대와의 대화는 멈춘 뒤, 메시지·통화 기록을 지우지 마세요.",
                "발신번호, 메시지·통화 시각, 요구 내용이 보이도록 관련 화면을 직접 캡처하세요.",
                "언제 무엇을 눌렀고, 돈·정보·앱 접근을 제공했는지 시간 순서대로 따로 적으세요.",
                "거래가 있었다면 금액·수취 계좌나 지갑·상품권 전달 여부와 거래 시각을 보관하세요.",
                "은행·카드사·경찰에 연락한 시각과 상담자, 접수번호를 이어서 기록하세요."
            ),
            kit.checklistItems
        )
        assertTrue(kit.title.contains("상황이 아직 확실하지 않을 때"))
        assertTrue(kit.cautions.any { it.contains("삭제") || it.contains("초기화") })
    }

    @Test
    fun moneySentKitPreservesTransactionsBeforeReportNumbers() {
        val kit = EvidencePreservationKitBuilder.build(IncidentSituation.MONEY_SENT)

        assertEquals(IncidentSituation.MONEY_SENT, kit.situation)
        assertTrue(kit.checklistItems[0].contains("거래 시각") && kit.checklistItems[0].contains("금액"))
        assertTrue(kit.checklistItems[1].contains("PIN") && kit.checklistItems[1].contains("전달 시각"))
        assertTrue(kit.checklistItems[2].contains("발신번호") && kit.checklistItems[2].contains("통화"))
        assertTrue(kit.checklistItems.last().contains("지급정지") && kit.checklistItems.last().contains("접수번호"))
    }

    @Test
    fun remoteControlKitDisconnectsBeforeCaptureAndSafeDeviceChanges() {
        val kit = EvidencePreservationKitBuilder.build(IncidentSituation.APP_INSTALLED)

        assertEquals(IncidentSituation.APP_INSTALLED, kit.situation)
        assertTrue(kit.checklistItems[0].contains("먼저") && kit.checklistItems[0].contains("연결을 끊"))
        assertTrue(kit.checklistItems[1].contains("앱 이름") && kit.checklistItems[1].contains("권한"))
        assertTrue(kit.checklistItems[3].contains("다른 안전한 기기") && kit.checklistItems[3].contains("비밀번호"))
        assertTrue(kit.cautions.any { it.contains("다시 켜") || it.contains("다시 실행") })
    }

    @Test
    fun credentialsSharedKitRecordsKindsWithoutRepeatingSecrets() {
        val kit = EvidencePreservationKitBuilder.build(IncidentSituation.CREDENTIALS_SHARED)

        assertEquals(IncidentSituation.CREDENTIALS_SHARED, kit.situation)
        assertTrue(kit.checklistItems[0].contains("정보의 종류") && kit.checklistItems[0].contains("실제 비밀값"))
        assertTrue(kit.checklistItems[1].contains("보안 경고") && kit.checklistItems[1].contains("캡처"))
        assertTrue(kit.checklistItems[2].contains("다른 안전한 기기"))
        assertTrue(kit.cautions.any { it.contains("신분증 사진") && it.contains("보내지") })
    }

    @Test
    fun linkOpenedKitCapturesWithoutReopening() {
        val kit = EvidencePreservationKitBuilder.build(IncidentSituation.LINK_OPENED)

        assertEquals(IncidentSituation.LINK_OPENED, kit.situation)
        assertTrue(kit.checklistItems[0].contains("다시 열지") && kit.checklistItems[0].contains("캡처"))
        assertTrue(kit.checklistItems[1].contains("누른 시각") && kit.checklistItems[1].contains("다운로드"))
        assertTrue(kit.checklistItems[2].contains("의심 파일을 열지 않은 채"))
        assertTrue(kit.cautions.any { it.contains("링크를 다시 누르") })
    }

    @Test
    fun everyKitIsDeterministicAndComplete() {
        allSituationsIncludingGeneric().forEach { situation ->
            val kit = EvidencePreservationKitBuilder.build(situation)

            assertEquals(kit, EvidencePreservationKitBuilder.build(situation))
            assertEquals(situation, kit.situation)
            assertTrue(kit.title.isNotBlank())
            assertTrue(kit.checklistItems.size >= 3)
            assertTrue(kit.checklistItems.all { it.isNotBlank() })
            assertTrue(kit.cautions.isNotEmpty())
            assertTrue(kit.cautions.all { it.isNotBlank() })
            assertTrue(kit.officialReportPrepNotes.all { it.isNotBlank() })
        }
    }

    @Test
    fun everyFamilySummaryNamesSituationButExcludesRawTextAndUnsafeClaims() {
        val placeholderRawScamText = "엄마, 300만원을 이 계좌로 보내고 OTP 123456도 말해줘"
        val rawTextFragments = listOf("엄마,", "300만원", "123456")
        val forbiddenClaims = listOf(
            "감시",
            "모니터링",
            "몰래",
            "자동 수집",
            "자동 캡처",
            "서버",
            "업로드",
            "네트워크로 전송",
            "네트워크에 전송",
            "monitoring",
            "server upload"
        )

        allSituationsIncludingGeneric().forEach { situation ->
            val kit = EvidencePreservationKitBuilder.build(situation)
            val summary = kit.familyShareSummary
            val expectedSituation = situation?.label ?: EvidencePreservationKitBuilder.GENERIC_SITUATION_LABEL

            assertTrue(summary.contains("증거 보존"))
            assertTrue(summary.contains(expectedSituation))
            assertTrue(summary.contains("접수번호"))
            assertTrue(summary.contains("원문") && summary.contains("포함하지 않았습니다"))
            assertFalse(summary.contains(placeholderRawScamText))
            rawTextFragments.forEach { fragment ->
                assertFalse("summary contained raw-text fragment: $fragment", summary.contains(fragment))
            }
            forbiddenClaims.forEach { phrase ->
                assertFalse("summary contained unsafe claim: $phrase", summary.lowercase().contains(phrase.lowercase()))
            }
        }
    }

    private fun allSituationsIncludingGeneric(): List<IncidentSituation?> =
        listOf<IncidentSituation?>(null) + IncidentSituation.entries
}
