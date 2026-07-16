package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryPlan24hBuilderTest {
    @Test
    fun noAnalysisHasStableWindowOrderAndConservativeDefaultRecommendations() {
        val plan = RecoveryPlan24hBuilder.build(analysis = null, readiness = readiness())

        assertEquals("분석 전 기본 순서", plan.recommendationTitle)
        assertEquals(
            listOf(
                RecoveryWindowId.IMMEDIATE_15MIN,
                RecoveryWindowId.WITHIN_1_HOUR,
                RecoveryWindowId.WITHIN_TODAY,
                RecoveryWindowId.WITHIN_WEEK
            ),
            plan.windows.map { window -> window.id }
        )
        assertEquals((1..4).toList(), plan.windows.map { window -> window.displayOrder })
        assertEquals(
            listOf(RecoveryWindowId.IMMEDIATE_15MIN, RecoveryWindowId.WITHIN_WEEK),
            plan.recommendedWindowIds
        )
        assertEquals(
            plan.recommendedWindowIds,
            plan.recommendedWindows.map { window -> window.id }
        )
        listOf(
            RecoveryActionId.QUICK_STOP,
            RecoveryActionId.CHECKER,
            RecoveryActionId.FAMILY_CALLBACK_CARD,
            RecoveryActionId.SAFETY_PLAN,
            RecoveryActionId.TIMELINE_BRIEFING,
            RecoveryActionId.PRIVACY_CENTER
        ).forEach { actionId ->
            assertTrue("default guidance omitted $actionId", actionId in plan.recommendedActionIds)
        }
        listOf(
            RecoveryActionId.ACCOUNT_LOCKDOWN,
            RecoveryActionId.RESPONSE_CENTER,
            RecoveryActionId.EVIDENCE_KIT,
            RecoveryActionId.OFFICIAL_HELP_DIRECTORY,
            RecoveryActionId.REPORT_PREP,
            RecoveryActionId.ANALYSIS_RESULT
        ).forEach { actionId ->
            assertFalse(
                "default guidance should not include not-yet-relevant $actionId",
                actionId in plan.recommendedActionIds
            )
        }
        assertEquals(RecoveryWindowId.entries.size, plan.windows.size)
        assertEquals(plan.windows.size, plan.windows.map { window -> window.id }.distinct().size)
    }

    @Test
    fun highRiskMoneyCredentialSafeAccountGiftAndCryptoSignalsRecommendImmediateHelpResponseLockdownAndEvidence() {
        listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.CREDENTIALS,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.OVERSEAS_PAYMENT_ALERT,
            ScamSignal.BAIT
        ).forEach { signal ->
            val plan = RecoveryPlan24hBuilder.build(analysis = analysisWith(signal), readiness = readiness())

            assertEquals(
                "$signal recommended windows",
                listOf(
                    RecoveryWindowId.IMMEDIATE_15MIN,
                    RecoveryWindowId.WITHIN_1_HOUR,
                    RecoveryWindowId.WITHIN_TODAY,
                    RecoveryWindowId.WITHIN_WEEK
                ),
                plan.recommendedWindowIds
            )
            listOf(
                RecoveryActionId.QUICK_STOP,
                RecoveryActionId.RESPONSE_CENTER,
                RecoveryActionId.ACCOUNT_LOCKDOWN,
                RecoveryActionId.EVIDENCE_KIT,
                RecoveryActionId.OFFICIAL_HELP_DIRECTORY
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in plan.recommendedActionIds)
            }
        }
    }

    @Test
    fun remoteControlAppInstallAndScreenShareRecommendLockdownEvidenceHelpWithoutMalwareRemovalGuarantee() {
        listOf(
            ScamSignal.APP_INSTALL,
            ScamSignal.LIVE_CALL_CONTROL
        ).forEach { signal ->
            val plan = RecoveryPlan24hBuilder.build(analysis = analysisWith(signal), readiness = readiness())

            assertEquals(
                "$signal recommended windows",
                listOf(
                    RecoveryWindowId.IMMEDIATE_15MIN,
                    RecoveryWindowId.WITHIN_1_HOUR,
                    RecoveryWindowId.WITHIN_TODAY,
                    RecoveryWindowId.WITHIN_WEEK
                ),
                plan.recommendedWindowIds
            )
            listOf(
                RecoveryActionId.ACCOUNT_LOCKDOWN,
                RecoveryActionId.EVIDENCE_KIT,
                RecoveryActionId.OFFICIAL_HELP_DIRECTORY,
                RecoveryActionId.RESPONSE_CENTER
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in plan.recommendedActionIds)
            }
            val copy = allCopy(plan)
            assertFalse(copy.contains("악성 앱을 자동으로 제거합니다"))
            assertFalse(copy.contains("악성코드 제거를 보장합니다"))
        }
    }

    @Test
    fun linkAndSmishingSignalsRecommendDoNotReopenEvidenceFamilyAlertAndHelpWithoutAutoScanClaim() {
        listOf(
            ScamSignal.LINK,
            ScamSignal.PARCEL_INVITATION_SMISHING
        ).forEach { signal ->
            val plan = RecoveryPlan24hBuilder.build(analysis = analysisWith(signal), readiness = readiness())

            assertEquals(
                "$signal recommended windows",
                listOf(
                    RecoveryWindowId.IMMEDIATE_15MIN,
                    RecoveryWindowId.WITHIN_TODAY,
                    RecoveryWindowId.WITHIN_WEEK
                ),
                plan.recommendedWindowIds
            )
            listOf(
                RecoveryActionId.EVIDENCE_KIT,
                RecoveryActionId.FAMILY_ALERT_MESSAGES,
                RecoveryActionId.OFFICIAL_HELP_DIRECTORY,
                RecoveryActionId.ANALYSIS_RESULT
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in plan.recommendedActionIds)
            }
            assertFalse(RecoveryActionId.RESPONSE_CENTER in plan.recommendedActionIds)

            val todayWindow = plan.windows.single { window -> window.id == RecoveryWindowId.WITHIN_TODAY }
            val todayCopy = (todayWindow.checklistSteps + todayWindow.doNotDoNotes).joinToString(" ")
            assertTrue(todayCopy.contains("다시 열지 마세요"))

            val copy = allCopy(plan)
            assertFalse(copy.contains("자동으로 검사합니다"))
            assertFalse(copy.contains("자동 스캔합니다"))
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeSignalsRecommendFamilyCallbackAndFamilyAlertEarly() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val plan = RecoveryPlan24hBuilder.build(analysis = analysisWith(signal), readiness = readiness())

            assertEquals(
                "$signal recommended windows",
                listOf(
                    RecoveryWindowId.IMMEDIATE_15MIN,
                    RecoveryWindowId.WITHIN_1_HOUR,
                    RecoveryWindowId.WITHIN_WEEK
                ),
                plan.recommendedWindowIds
            )
            listOf(
                RecoveryActionId.FAMILY_CALLBACK_CARD,
                RecoveryActionId.FAMILY_ALERT_MESSAGES
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in plan.recommendedActionIds)
            }
        }

        val familyAndUrgent = RecoveryPlan24hBuilder.build(
            analysis = analysisWith(ScamSignal.FAMILY_EMERGENCY, ScamSignal.URGENT_TRANSFER),
            readiness = readiness()
        )
        assertEquals(
            listOf(
                RecoveryWindowId.IMMEDIATE_15MIN,
                RecoveryWindowId.WITHIN_1_HOUR,
                RecoveryWindowId.WITHIN_TODAY,
                RecoveryWindowId.WITHIN_WEEK
            ),
            familyAndUrgent.recommendedWindowIds
        )
    }

    @Test
    fun everyWindowHasCompleteFieldsAndStableIdOrder() {
        listOf(
            RecoveryPlan24hBuilder.build(analysis = null, readiness = readiness()),
            RecoveryPlan24hBuilder.build(
                analysis = analysisWith(
                    ScamSignal.APP_INSTALL,
                    ScamSignal.CREDENTIALS,
                    ScamSignal.LINK,
                    ScamSignal.PHONE_NUMBER_CHANGE
                ),
                readiness = readiness(ready = true)
            )
        ).forEach { plan ->
            assertTrue(plan.title.isNotBlank())
            assertTrue(plan.intro.isNotBlank())
            assertTrue(plan.recommendationTitle.isNotBlank())
            assertTrue(plan.recommendationExplanation.isNotBlank())
            assertTrue(plan.privacySafetyNotes.isNotEmpty())
            assertTrue(plan.privacySafetyNotes.all { note -> note.isNotBlank() })
            assertTrue(
                plan.recommendedWindowIds.all { windowId ->
                    plan.windows.any { window -> window.id == windowId }
                }
            )

            plan.windows.forEach { window ->
                assertTrue(window.displayOrder > 0)
                assertTrue(window.urgency.rank >= 0)
                assertTrue(window.urgency.label.isNotBlank())
                assertTrue(window.label.isNotBlank())
                assertTrue(window.purpose.isNotBlank())
                assertTrue(window.checklistSteps.isNotEmpty())
                assertTrue(window.checklistSteps.all { step -> step.isNotBlank() })
                assertTrue(window.doNotDoNotes.isNotEmpty())
                assertTrue(window.doNotDoNotes.all { note -> note.isNotBlank() })
                assertTrue(window.primaryActionLabel.isNotBlank())
                assertTrue(window.secondaryActionLabels.all { label -> label.isNotBlank() })
                assertEquals(
                    window.secondaryActions.map { action -> action.label },
                    window.secondaryActionLabels
                )
            }
        }
    }

    @Test
    fun shareContainsWindowAndActionLabelsButExcludesSensitiveValues() {
        val safeWord = "해질녘 파란 우산은 어디에?"
        val callbackScript = "푸른 메모장을 열고 첫 번째 저장 번호로 전화한다."
        val contactLabel = "보호자-은하수"
        val contactNumber = "010-2468-1357"
        val raw =
            "원문-파란별 전화 010-9876-5432 계좌 123456789012 비밀번호 BluePassword77 " +
                "OTP A1B2C3 PIN 7788 https://evil.example/path 주민번호 900101-1234567 " +
                "안전 질문 $safeWord 콜백 내용 $callbackScript 연락처 $contactLabel $contactNumber"
        val analysis = ScamAnalysis(
            input = raw,
            score = 100,
            riskLevel = RiskLevel.HIGH,
            matchedSignals = listOf(
                ScamSignal.APP_INSTALL,
                ScamSignal.CREDENTIALS,
                ScamSignal.LINK,
                ScamSignal.PHONE_NUMBER_CHANGE
            )
        )
        val plan = RecoveryPlan24hBuilder.build(analysis = analysis, readiness = readiness(ready = true))
        val summary = plan.familyShareSummary
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)

        plan.windows.forEach { window ->
            assertTrue("share omitted window: ${window.label}", summary.contains(window.label))
            (listOf(window.primaryAction) + window.secondaryActions).forEach { action ->
                assertTrue("share omitted action: ${action.label}", summary.contains(action.label))
            }
        }
        assertTrue(summary.contains("수습 계획"))
        assertTrue(summary.contains("원문") && summary.contains("포함하지 않았습니다"))
        assertFalse(summary.contains(raw))
        assertFalse(summary.contains(redactedClue))
        listOf(
            "원문-파란별",
            safeWord,
            callbackScript,
            contactLabel,
            contactNumber,
            "010-9876-5432",
            "123456789012",
            "BluePassword77",
            "A1B2C3",
            "7788",
            "evil.example",
            "900101-1234567"
        ).forEach { secret ->
            assertFalse("recovery plan share leaked secret: $secret", summary.contains(secret))
        }
        assertFalse(Regex("0\\d{1,2}[- .]?\\d{3,4}[- .]?\\d{4}").containsMatchIn(summary))
        assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(summary))
        assertFalse(
            Regex("(?i)(?:[a-z][a-z0-9+.-]*://|www\\.|[a-z0-9-]+\\.(?:com|net|org|kr|한국))")
                .containsMatchIn(summary)
        )
        assertFalse(
            Regex("(?<!\\d)\\d{6}\\s*-?\\s*[1-8]\\d{6}(?!\\d)")
                .containsMatchIn(summary)
        )

        val defaultPlan = RecoveryPlan24hBuilder.build(analysis = null, readiness = readiness())
        val defaultSummary = defaultPlan.familyShareSummary
        defaultPlan.windows.forEach { window ->
            assertTrue(defaultSummary.contains(window.label))
            (listOf(window.primaryAction) + window.secondaryActions).forEach { action ->
                assertTrue(defaultSummary.contains(action.label))
            }
        }
    }

    @Test
    fun outputIsDeterministicForTheSameInputs() {
        val analysis = analysisWith(
            ScamSignal.APP_INSTALL,
            ScamSignal.CREDENTIALS,
            ScamSignal.LINK,
            ScamSignal.PHONE_NUMBER_CHANGE
        )
        val readinessValue = readiness(ready = true)

        val first = RecoveryPlan24hBuilder.build(analysis, readinessValue)
        val second = RecoveryPlan24hBuilder.build(analysis, readinessValue)

        assertEquals(first, second)
        assertEquals(first.windows, second.windows)
        assertEquals(first.recommendedWindowIds, second.recommendedWindowIds)
        assertEquals(first.recommendedActionIds, second.recommendedActionIds)
        assertEquals(first.familyShareSummary, second.familyShareSummary)
    }

    @Test
    fun copyStatesManualNoStorageNoContactReadingNoAutoSendNoMonitoringNoNetwork() {
        val plans = listOf(
            RecoveryPlan24hBuilder.build(analysis = null, readiness = readiness()),
            RecoveryPlan24hBuilder.build(analysis = analysisWith(ScamSignal.APP_INSTALL), readiness = readiness()),
            RecoveryPlan24hBuilder.build(
                analysis = analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER),
                readiness = readiness(ready = true)
            ),
            RecoveryPlan24hBuilder.build(analysis = analysisWith(ScamSignal.LINK), readiness = readiness())
        )

        plans.forEach { plan ->
            val privacyCopy = plan.privacySafetyNotes.joinToString(" ")
            assertTrue(privacyCopy.contains("수동 안내"))
            assertTrue(privacyCopy.contains("기기 안"))
            assertTrue(privacyCopy.contains("사용자가 직접"))
            assertTrue(privacyCopy.contains("저장하지 않"))
            assertTrue(privacyCopy.contains("연락처를 읽거나 받는 사람을 고르지 않"))
            assertTrue(privacyCopy.contains("자동으로 보내지 않"))
            assertTrue(privacyCopy.contains("자동으로 읽거나 감시하지 않"))
            assertTrue(privacyCopy.contains("네트워크"))
            assertTrue(
                privacyCopy.contains("사건 기록") &&
                    privacyCopy.contains("추가·수정·삭제하지 않습니다")
            )
            assertTrue(privacyCopy.contains("새 Android 권한"))
        }
    }

    @Test
    fun copyDoesNotImplyAutomaticMonitoringBlockingRemovalFreezeRecoveryLegalGuaranteeOrAutoDeletion() {
        val plans = listOf(
            RecoveryPlan24hBuilder.build(analysis = null, readiness = readiness()),
            RecoveryPlan24hBuilder.build(analysis = analysisWith(ScamSignal.APP_INSTALL), readiness = readiness()),
            RecoveryPlan24hBuilder.build(
                analysis = analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER),
                readiness = readiness(ready = true)
            ),
            RecoveryPlan24hBuilder.build(analysis = analysisWith(ScamSignal.LINK), readiness = readiness())
        )
        val forbiddenAffirmativeClaims = listOf(
            "자동으로 감시합니다",
            "자동 모니터링합니다",
            "몰래 확인합니다",
            "자동으로 검사합니다",
            "자동 스캔합니다",
            "자동으로 읽습니다",
            "연락처를 자동으로 읽습니다",
            "연락처 앱에서 자동으로 가져옵니다",
            "서버로 업로드합니다",
            "네트워크로 업로드합니다",
            "악성코드를 자동으로 제거합니다",
            "악성코드 제거를 보장합니다",
            "계좌를 자동으로 지급정지합니다",
            "계좌 지급정지를 보장합니다",
            "카드를 자동으로 정지합니다",
            "계정을 자동으로 잠급니다",
            "법적 효력을 보장합니다",
            "법적 결과를 보장합니다",
            "피해 회복을 보장합니다",
            "피해금 회수를 보장합니다",
            "반드시 환급됩니다",
            "반드시 회수됩니다",
            "환불 보장",
            "자동으로 삭제합니다",
            "자동 삭제합니다",
            "100%",
            "automatic monitoring",
            "reads contacts automatically",
            "server upload",
            "malware removal guarantee",
            "account freeze guarantee",
            "legal guarantee",
            "guaranteed recovery"
        )

        plans.forEach { plan ->
            val copy = allCopy(plan)
            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "recovery plan copy contained unsafe claim: $claim",
                    copy.lowercase().contains(claim.lowercase())
                )
            }
        }
    }

    private fun analysisWith(vararg signals: ScamSignal): ScamAnalysis = ScamAnalysis(
        input = "테스트 입력 ${signals.joinToString { signal -> signal.name }}",
        score = 50,
        riskLevel = RiskLevel.MEDIUM,
        matchedSignals = signals.toList()
    )

    private fun readiness(ready: Boolean = false): FamilyReadiness = FamilyReadinessBuilder.build(
        if (ready) {
            SafetyPlan(
                safeWord = "가족 질문",
                callbackScript = "기존 번호로 다시 전화한다.",
                contacts = listOf(TrustedContact("가족", "저장된 번호"))
            )
        } else {
            SafetyPlan(safeWord = "", callbackScript = "", contacts = emptyList())
        }
    )

    private fun allCopy(plan: RecoveryPlan24h): String = buildString {
        append(plan.title)
        append(plan.intro)
        append(plan.recommendationTitle)
        append(plan.recommendationExplanation)
        plan.windows.forEach { window ->
            append(window.urgency.label)
            append(window.label)
            append(window.purpose)
            window.checklistSteps.forEach(::append)
            window.doNotDoNotes.forEach(::append)
            append(window.primaryActionLabel)
            window.secondaryActionLabels.forEach(::append)
        }
        plan.privacySafetyNotes.forEach(::append)
        append(plan.familyShareSummary)
    }
}
