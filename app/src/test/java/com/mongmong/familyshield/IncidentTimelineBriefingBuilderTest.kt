package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IncidentTimelineBriefingBuilderTest {
    @Test
    fun emptyIncidentListHasZeroMetricsAndSafeNextStepGuidance() {
        val briefing = build(emptyList(), hasCurrentAnalysis = false)

        assertEquals(0, briefing.totalCount)
        assertEquals(IncidentBriefingRiskCounts(0, 0, 0, 0), briefing.riskCounts)
        assertEquals("기록 없음", briefing.highestRiskLabel)
        assertNotNull(briefing.emptyStateTitle)
        assertNotNull(briefing.emptyStateMessage)
        assertTrue(requireNotNull(briefing.emptyStateMessage).contains("새 검사"))
        assertTrue(requireNotNull(briefing.emptyStateMessage).contains("새 기록은 생기지 않습니다"))
        assertNull(briefing.latestIncidentDisplayTime)
        assertNull(briefing.latestIncidentRiskLabel)
        assertTrue(briefing.topSignals.isEmpty())
        assertTrue(briefing.timelineItems.isEmpty())

        val actionIds = briefing.nextSteps.map { it.id }
        assertTrue(IncidentBriefingActionId.NEW_ANALYSIS in actionIds)
        assertTrue(IncidentBriefingActionId.SAFETY_FLOW_ROUTER in actionIds)
        assertTrue(IncidentBriefingActionId.RESPONSE_CENTER in actionIds)
        assertTrue(IncidentBriefingActionId.EVIDENCE_KIT in actionIds)
        assertTrue(IncidentBriefingActionId.OFFICIAL_HELP_DIRECTORY in actionIds)
        assertFalse(IncidentBriefingActionId.REPORT_PREP in actionIds)
        assertTrue(
            briefing.nextSteps.single { it.id == IncidentBriefingActionId.NEW_ANALYSIS }.recommended
        )
        assertTrue(briefing.familyShareSummary.contains("저장 사건: 0건"))
    }

    @Test
    fun mixedUnsortedAndDuplicateIncidentsHaveDeterministicMetricsAndNewestFirstOrder() {
        val duplicateHigh = incident(
            timestamp = 2_000,
            riskLabel = RiskLevel.HIGH.label,
            score = 91,
            signals = ScamSignal.URGENT_TRANSFER.label,
            preview = "긴급 송금 압박 · 붙여넣은 원문 비공개"
        )
        val input = listOf(
            incident(1_000, RiskLevel.SAFE.label, 8),
            duplicateHigh,
            incident(3_000, RiskLevel.MEDIUM.label, -7),
            incident(1_500, "  ", 20),
            duplicateHigh.copy()
        )

        val briefing = build(input, hasCurrentAnalysis = true)

        assertEquals(5, briefing.totalCount)
        assertEquals(IncidentBriefingRiskCounts(2, 1, 1, 1), briefing.riskCounts)
        assertEquals(RiskLevel.HIGH.label, briefing.highestRiskLabel)
        assertEquals("시각-3000", briefing.latestIncidentDisplayTime)
        assertEquals(RiskLevel.MEDIUM.label, briefing.latestIncidentRiskLabel)
        assertEquals(
            listOf(3_000L, 2_000L, 2_000L, 1_500L, 1_000L),
            briefing.timelineItems.map { it.timestamp }
        )
        assertEquals(0, briefing.timelineItems.first().score)
        assertEquals(2, briefing.timelineItems.count { it.timestamp == 2_000L })
        assertEquals("확인 필요", briefing.timelineItems.single { it.timestamp == 1_500L }.riskLabel)
    }

    @Test
    fun unknownRiskStaysConservativeBesideLowRiskAndPoisonedLabelIsNotShared() {
        val poisonedRiskLabel = "가족암구호-푸른별 010-9999-8888 https://risk-leak.example"
        val briefing = build(
            listOf(
                incident(2_000, RiskLevel.SAFE.label, 4),
                incident(1_000, poisonedRiskLabel, 22)
            ),
            hasCurrentAnalysis = false
        )

        assertEquals(IncidentBriefingRiskCounts(0, 0, 1, 1), briefing.riskCounts)
        assertEquals("확인 필요", briefing.highestRiskLabel)
        assertFalse(briefing.familyShareSummary.contains(poisonedRiskLabel))
        assertFalse(briefing.familyShareSummary.contains("010-9999-8888"))
        assertFalse(briefing.familyShareSummary.contains("risk-leak.example"))
    }

    @Test
    fun topSignalsAggregateCommaSeparatedLabelsPerIncidentWithoutPreviewText() {
        val previewMarker = "PREVIEW-MARKER-NEVER-A-SIGNAL"
        val incidents = listOf(
            incident(
                3_000,
                RiskLevel.HIGH.label,
                88,
                " ${ScamSignal.URGENT_TRANSFER.label}, ${ScamSignal.CREDENTIALS.label}, " +
                    "${ScamSignal.URGENT_TRANSFER.label}, ",
                previewMarker
            ),
            incident(
                2_000,
                RiskLevel.MEDIUM.label,
                48,
                "${ScamSignal.URGENT_TRANSFER.label}, ${ScamSignal.LINK.label}",
                "$previewMarker-two"
            ),
            incident(
                1_000,
                RiskLevel.SAFE.label,
                18,
                ScamSignal.LINK.label,
                "$previewMarker-three"
            )
        )

        val briefing = build(incidents, hasCurrentAnalysis = false)

        assertEquals(
            listOf(
                IncidentBriefingSignal(ScamSignal.URGENT_TRANSFER.label, 2),
                IncidentBriefingSignal(ScamSignal.LINK.label, 2),
                IncidentBriefingSignal(ScamSignal.CREDENTIALS.label, 1)
            ),
            briefing.topSignals
        )
        assertTrue(briefing.topSignals.none { it.label.contains(previewMarker) })
        assertFalse(briefing.familyShareSummary.contains(previewMarker))
    }

    @Test
    fun timelineUsesOnlyStoredRedactedPreviewAndCalmBlankFallbacks() {
        val legacyPreview = "연락처 010-2222-3333 계좌 123-456-789012 인증번호 482913"
        val incidents = listOf(
            incident(2_000, "", -10, "", ""),
            incident(1_000, RiskLevel.MEDIUM.label, 45, ScamSignal.LINK.label, legacyPreview)
        )

        val briefing = build(incidents, hasCurrentAnalysis = false)
        val blankItem = briefing.timelineItems.first()
        val legacyItem = briefing.timelineItems.last()

        assertEquals("확인 필요", blankItem.riskLabel)
        assertEquals(0, blankItem.score)
        assertTrue(blankItem.signalLabels.isEmpty())
        assertEquals("강한 위험 신호 없음", blankItem.signalSummary)
        assertEquals(IncidentPreviewRedactor.FALLBACK_PREVIEW, blankItem.redactedPreview)
        assertEquals(IncidentPreviewRedactor.redact(legacyPreview), legacyItem.redactedPreview)
        assertFalse(legacyItem.redactedPreview.contains("010-2222-3333"))
        assertFalse(legacyItem.redactedPreview.contains("123-456-789012"))
        assertFalse(legacyItem.redactedPreview.contains("482913"))

        val blankTime = IncidentTimelineBriefingBuilder.build(
            incidents = listOf(incidents.first()),
            hasCurrentAnalysis = false,
            displayTime = { "  " }
        )
        val failedTime = IncidentTimelineBriefingBuilder.build(
            incidents = listOf(incidents.first()),
            hasCurrentAnalysis = false,
            displayTime = { error("formatter unavailable") }
        )
        assertEquals("시각 정보 없음", blankTime.timelineItems.single().displayTime)
        assertEquals("시각 정보 없음", failedTime.timelineItems.single().displayTime)
    }

    @Test
    fun moneyCredentialAndRemoteSignalsEachRecommendEveryResponseFlow() {
        val urgentSignals = listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.CREDENTIALS,
            ScamSignal.APP_INSTALL,
            ScamSignal.LIVE_CALL_CONTROL,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE
        )
        urgentSignals.forEachIndexed { index, signal ->
            val storedIncident = incident(
                timestamp = 5_000L + index,
                riskLabel = RiskLevel.MEDIUM.label,
                score = 45,
                signals = signal.label
            )
            val withAnalysis = build(listOf(storedIncident), hasCurrentAnalysis = true)
            val recommendedWithAnalysis = withAnalysis.nextSteps
                .filter { it.recommended }
                .map { it.id }

            listOf(
                IncidentBriefingActionId.RESPONSE_CENTER,
                IncidentBriefingActionId.EVIDENCE_KIT,
                IncidentBriefingActionId.OFFICIAL_HELP_DIRECTORY,
                IncidentBriefingActionId.REPORT_PREP
            ).forEach { actionId ->
                assertTrue(
                    "${signal.name} briefing omitted $actionId",
                    actionId in recommendedWithAnalysis
                )
            }
            assertEquals(
                "긴급 대응 센터 확인",
                withAnalysis.timelineItems.single().recommendedNextActionLabel
            )
        }

        val withoutAnalysis = build(
            listOf(
                incident(
                    6_000,
                    RiskLevel.HIGH.label,
                    100,
                    ScamSignal.URGENT_TRANSFER.label
                )
            ),
            hasCurrentAnalysis = false
        )
        val recommendedWithoutAnalysis = withoutAnalysis.nextSteps
            .filter { it.recommended }
            .map { it.id }
        assertTrue(IncidentBriefingActionId.NEW_ANALYSIS in recommendedWithoutAnalysis)
        assertFalse(IncidentBriefingActionId.REPORT_PREP in withoutAnalysis.nextSteps.map { it.id })
    }

    @Test
    fun familyDeepfakeAndNumberChangeSignalsEachRecommendFamilyVerificationFirst() {
        val familySignals = listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        )
        familySignals.forEachIndexed { index, signal ->
            val briefing = build(
                listOf(
                    incident(
                        timestamp = 4_000L + index,
                        riskLabel = RiskLevel.MEDIUM.label,
                        score = 55,
                        signals = signal.label
                    )
                ),
                hasCurrentAnalysis = false
            )

            assertEquals(
                "${signal.name} should recommend the router first",
                IncidentBriefingActionId.SAFETY_FLOW_ROUTER,
                briefing.nextSteps.first().id
            )
            assertTrue(briefing.nextSteps.first().recommended)
            assertTrue(briefing.nextSteps.first().explanation.contains("가족 콜백"))
            assertTrue(briefing.timelineItems.single().recommendedNextActionLabel.contains("가족 콜백"))
        }
    }

    @Test
    fun familyShareUsesOnlyAggregatesAndExcludesSensitivePatternsAndEveryIncidentClue() {
        val clueMarker = "LOCAL-ONLY-CLUE-BLUE-UMBRELLA"
        val safeWordValue = "파란 우산 아래 푸른별"
        val contactLabel = "엄마-비밀연락처"
        val poisonedPreview =
            "$clueMarker 전화 010-9876-5432 계좌 123-456-789012 OTP 654321 " +
                "https://evil.example/path secure-bank.co.kr 주민번호 900101-1234567 " +
                "안전단어 $safeWordValue 연락처 $contactLabel"
        val poisonedUnknownSignal = "$contactLabel 010-2468-1357 $safeWordValue"
        val storedIncident = incident(
            timestamp = 9_000,
            riskLabel = RiskLevel.HIGH.label,
            score = 99,
            signals = "${ScamSignal.URGENT_TRANSFER.label}, $poisonedUnknownSignal",
            preview = poisonedPreview
        )
        val briefing = IncidentTimelineBriefingBuilder.build(
            incidents = listOf(storedIncident),
            hasCurrentAnalysis = true,
            displayTime = { "https://time-leak.example/010-1111-2222/$it" }
        )
        val summary = briefing.familyShareSummary

        assertTrue(summary.contains("저장 사건: 1건"))
        assertTrue(summary.contains("가장 높은 위험도: 높음"))
        assertTrue(summary.contains(ScamSignal.URGENT_TRANSFER.label))
        briefing.nextSteps.forEach { action ->
            assertTrue("share omitted action label: ${action.label}", summary.contains(action.label))
        }
        briefing.timelineItems.forEach { item ->
            assertFalse("share included timeline clue: ${item.redactedPreview}", summary.contains(item.redactedPreview))
            assertFalse("share included display time: ${item.displayTime}", summary.contains(item.displayTime))
        }
        listOf(
            clueMarker,
            safeWordValue,
            contactLabel,
            poisonedUnknownSignal,
            "010-9876-5432",
            "010-2468-1357",
            "123-456-789012",
            "654321",
            "evil.example",
            "secure-bank.co.kr",
            "time-leak.example",
            "900101-1234567"
        ).forEach { secret ->
            assertFalse("share leaked secret: $secret", summary.contains(secret))
        }
        assertFalse(Regex("0\\d{1,2}[- .]?\\d{3,4}[- .]?\\d{4}").containsMatchIn(summary))
        assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(summary))
        assertFalse(
            Regex("(?i)(?:[a-z][a-z0-9+.-]*://|www\\.|[a-z0-9-]+\\.(?:com|net|org|kr|한국))")
                .containsMatchIn(summary)
        )
        assertFalse(Regex("(?<!\\d)\\d{6}\\s*-?\\s*[1-8]\\d{6}(?!\\d)").containsMatchIn(summary))
    }

    @Test
    fun outputIsDeterministicAndBuilderDoesNotMutateInput() {
        val input = mutableListOf(
            incident(1_000, RiskLevel.SAFE.label, 4, "", "일반 확인 · 붙여넣은 원문 비공개"),
            incident(3_000, RiskLevel.HIGH.label, 90, ScamSignal.CREDENTIALS.label),
            incident(2_000, RiskLevel.MEDIUM.label, 45, ScamSignal.LINK.label)
        )
        val snapshot = input.toList()

        val first = build(input, hasCurrentAnalysis = true)
        val second = build(input, hasCurrentAnalysis = true)

        assertEquals(first, second)
        assertEquals(first.timelineItems, second.timelineItems)
        assertEquals(first.topSignals, second.topSignals)
        assertEquals(first.nextSteps, second.nextSteps)
        assertEquals(first.familyShareSummary, second.familyShareSummary)
        assertEquals(snapshot, input)
    }

    @Test
    fun copyStatesLocalManualLimitsWithoutMonitoringUploadDeletionOrOutcomeGuarantees() {
        val briefings = listOf(
            build(emptyList(), hasCurrentAnalysis = false),
            build(
                listOf(
                    incident(
                        2_000,
                        RiskLevel.HIGH.label,
                        90,
                        "${ScamSignal.URGENT_TRANSFER.label}, ${ScamSignal.APP_INSTALL.label}",
                        "개인정보 가림 단서"
                    )
                ),
                hasCurrentAnalysis = true
            )
        )
        val forbiddenAffirmativeClaims = listOf(
            "자동으로 감시합니다",
            "자동 모니터링합니다",
            "몰래 확인합니다",
            "자동으로 검사합니다",
            "자동으로 읽습니다",
            "자동으로 수집합니다",
            "연락처를 자동으로 읽습니다",
            "서버로 업로드합니다",
            "네트워크로 업로드합니다",
            "네트워크로 전송합니다",
            "법적 효력을 보장합니다",
            "법적 결과를 보장합니다",
            "피해 회복을 보장합니다",
            "피해금 회수를 보장합니다",
            "반드시 환급됩니다",
            "반드시 회수됩니다",
            "자동으로 삭제합니다",
            "자동 삭제합니다",
            "100%",
            "automatic monitoring",
            "reads contacts automatically",
            "server upload",
            "legal guarantee",
            "guaranteed recovery"
        )

        briefings.forEach { briefing ->
            val allCopy = allCopy(briefing)
            val privacyCopy = briefing.privacySafetyNotes.joinToString(" ")

            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "briefing copy contained unsafe claim: $claim",
                    allCopy.lowercase().contains(claim.lowercase())
                )
            }
            assertTrue(privacyCopy.contains("기기 안"))
            assertTrue(privacyCopy.contains("문자·통화·연락처"))
            assertTrue(privacyCopy.contains("자동으로 읽거나 감시하지 않"))
            assertTrue(privacyCopy.contains("따로 저장하지 않"))
            assertTrue(privacyCopy.contains("새 사건") && privacyCopy.contains("추가하지 않"))
            assertTrue(privacyCopy.contains("삭제하지 않"))
            assertTrue(privacyCopy.contains("네트워크") && privacyCopy.contains("보내지 않"))
            assertTrue(privacyCopy.contains("실제 조치와 결과") && privacyCopy.contains("확인"))
            assertTrue(privacyCopy.contains("가림 단서") && privacyCopy.contains("공유 요약에는 넣지 않"))
        }
    }

    private fun build(
        incidents: List<Incident>,
        hasCurrentAnalysis: Boolean
    ): IncidentTimelineBriefing = IncidentTimelineBriefingBuilder.build(
        incidents = incidents,
        hasCurrentAnalysis = hasCurrentAnalysis,
        displayTime = { timestamp -> "시각-$timestamp" }
    )

    private fun incident(
        timestamp: Long,
        riskLabel: String,
        score: Int,
        signals: String = "",
        preview: String = "개인정보 가림 단서"
    ): Incident = Incident(timestamp, riskLabel, score, signals, preview)

    private fun allCopy(briefing: IncidentTimelineBriefing): String = buildString {
        append(briefing.title)
        append(briefing.intro)
        append(briefing.emptyStateTitle.orEmpty())
        append(briefing.emptyStateMessage.orEmpty())
        append(briefing.totalCount)
        append(briefing.riskCounts)
        append(briefing.highestRiskLabel)
        append(briefing.latestIncidentDisplayTime.orEmpty())
        append(briefing.latestIncidentRiskLabel.orEmpty())
        briefing.topSignals.forEach { signal ->
            append(signal.label)
            append(signal.incidentCount)
        }
        briefing.timelineItems.forEach { item ->
            append(item.displayTime)
            append(item.riskLabel)
            append(item.score)
            item.signalLabels.forEach(::append)
            append(item.signalSummary)
            append(item.redactedPreview)
            append(item.recommendedNextActionLabel)
        }
        briefing.nextSteps.forEach { action ->
            append(action.label)
            append(action.explanation)
        }
        briefing.privacySafetyNotes.forEach(::append)
        append(briefing.familyShareSummary)
    }
}
