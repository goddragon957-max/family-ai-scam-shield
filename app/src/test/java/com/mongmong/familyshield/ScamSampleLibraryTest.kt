package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScamSampleLibraryTest {
    @Test
    fun coversAllRequiredCategories() {
        val samples = ScamSampleLibrary.all()

        assertEquals(ScamSampleCategory.entries.size, samples.size)
        assertTrue(samples.size >= 5)
        assertEquals(ScamSampleCategory.entries, samples.map { it.category })
    }

    @Test
    fun everySampleIsMarkedFictionalPractice() {
        ScamSampleLibrary.all().forEach { sample ->
            assertTrue(sample.sampleText.startsWith(ScamSampleLibrary.FICTION_MARKER))
            assertTrue(sample.sampleText.contains("연습용 가상"))
            assertTrue(sample.title.contains("샘플"))
        }
        assertTrue(ScamSampleLibrary.FICTION_NOTICE.contains("연습용 가상 메시지"))
    }

    @Test
    fun analyzerMatchesExpectedRiskLevelForEverySample() {
        ScamSampleLibrary.all().forEach { sample ->
            val analysis = ScamAnalyzer.analyze(sample.sampleText)

            assertEquals(
                "risk level mismatch for ${sample.category}",
                sample.expectedRiskLevel,
                analysis.riskLevel
            )
        }
    }

    @Test
    fun analyzerMatchesExpectedKeySignalsForEverySample() {
        ScamSampleLibrary.all().forEach { sample ->
            val analysis = ScamAnalyzer.analyze(sample.sampleText)

            assertTrue(sample.expectedSignals.isNotEmpty())
            sample.expectedSignals.forEach { signal ->
                assertTrue(
                    "expected ${signal.name} for ${sample.category}",
                    signal in analysis.matchedSignals
                )
            }
        }
    }

    @Test
    fun everySampleIsDeterministicAndComplete() {
        ScamSampleCategory.entries.forEach { category ->
            val sample = ScamSampleLibrary.build(category)

            assertEquals(category, sample.category)
            assertTrue(sample.title.isNotBlank())
            assertTrue(sample.sampleText.isNotBlank())
            assertTrue(sample.learningNote.isNotBlank())
            assertEquals(sample, ScamSampleLibrary.build(category))
        }
    }

    @Test
    fun sampleTextContainsNoRealLink() {
        ScamSampleLibrary.all().forEach { sample ->
            val lowered = sample.sampleText.lowercase()

            assertFalse(lowered.contains("http"))
            assertFalse(lowered.contains("www."))
        }
    }

    @Test
    fun sampleCopyNeverClaimsMonitoringNetworkOrSurveillance() {
        ScamSampleLibrary.all().forEach { sample ->
            val allText = sample.title + sample.sampleText + sample.learningNote

            assertFalse(allText.contains("감시"))
            assertFalse(allText.contains("몰래"))
            assertFalse(allText.contains("자동으로 읽"))
            assertFalse(allText.contains("서버"))
            assertFalse(allText.contains("네트워크"))
        }
    }
}
