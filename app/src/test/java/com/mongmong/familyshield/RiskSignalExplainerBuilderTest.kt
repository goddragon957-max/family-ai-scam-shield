package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskSignalExplainerBuilderTest {
    @Test
    fun noAnalysisAndNoSignalsHaveStableOrderAndConservativeRecommendations() {
        val noAnalysis = RiskSignalExplainerBuilder.build(
            analysis = null,
            readiness = readiness()
        )
        val noSignals = RiskSignalExplainerBuilder.build(
            analysis = ScamAnalysis(
                input = "일반 대화",
                score = 0,
                riskLevel = RiskLevel.SAFE,
                matchedSignals = emptyList()
            ),
            readiness = readiness()
        )
        val expectedDefaults = listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.CREDENTIALS,
            ScamSignal.LINK,
            ScamSignal.FAMILY_EMERGENCY
        )

        listOf(noAnalysis, noSignals).forEach { explainer ->
            assertEquals(ScamSignal.entries, explainer.cards.map { card -> card.id })
            assertEquals((1..ScamSignal.entries.size).toList(), explainer.cards.map { card -> card.displayOrder })
            assertEquals(expectedDefaults, explainer.recommendedSignalIds)
            assertEquals(expectedDefaults, explainer.recommendedCards.map { card -> card.id })
            assertEquals(
                listOf(
                    RiskSignalExplainerActionId.CHECKER,
                    RiskSignalExplainerActionId.QUICK_STOP,
                    RiskSignalExplainerActionId.FAMILY_CALLBACK_CARD,
                    RiskSignalExplainerActionId.SAFETY_PLAN
                ),
                explainer.recommendedActionIds
            )
            assertTrue(explainer.recommendationExplanation.contains("직접 분석"))
        }
        assertEquals("분석 전에도 알아둘 기본 신호", noAnalysis.recommendationTitle)
        assertEquals("뚜렷한 일치 신호가 없을 때 기본 확인", noSignals.recommendationTitle)
        assertTrue(noAnalysis.recommendationExplanation.contains("현재 분석이 없어"))
        assertTrue(noSignals.recommendationExplanation.contains("다른 의심 내용"))
        assertEquals(ScamSignal.entries.size, noAnalysis.cards.map { card -> card.id }.distinct().size)
    }

    @Test
    fun everyScamSignalHasOneCardWithStableLabelAndCategory() {
        val explainer = RiskSignalExplainerBuilder.build(null, readiness())
        val expectedCategories = mapOf(
            ScamSignal.URGENT_TRANSFER to RiskSignalCategory.URGENCY_MONEY_PRESSURE,
            ScamSignal.IMPERSONATION to RiskSignalCategory.URGENCY_MONEY_PRESSURE,
            ScamSignal.APP_INSTALL to RiskSignalCategory.APP_AND_REMOTE_CONTROL,
            ScamSignal.LINK to RiskSignalCategory.LINKS_AND_SMISHING,
            ScamSignal.BAIT to RiskSignalCategory.FINANCIAL_BAIT,
            ScamSignal.CREDENTIALS to RiskSignalCategory.CREDENTIALS_AND_SECRETS,
            ScamSignal.FAMILY_EMERGENCY to RiskSignalCategory.FAMILY_IMPERSONATION,
            ScamSignal.GIFT_CRYPTO to RiskSignalCategory.GIFT_AND_CRYPTO,
            ScamSignal.SECRECY to RiskSignalCategory.URGENCY_MONEY_PRESSURE,
            ScamSignal.PHONE_NUMBER_CHANGE to RiskSignalCategory.FAMILY_IMPERSONATION,
            ScamSignal.LOAN_REFINANCE_FEE to RiskSignalCategory.FINANCIAL_BAIT,
            ScamSignal.GIFTICON_PIN to RiskSignalCategory.GIFT_AND_CRYPTO,
            ScamSignal.LIVE_CALL_CONTROL to RiskSignalCategory.APP_AND_REMOTE_CONTROL,
            ScamSignal.DEEPFAKE_VOICE to RiskSignalCategory.FAMILY_IMPERSONATION,
            ScamSignal.SAFE_ACCOUNT_FRAUD to RiskSignalCategory.FINANCIAL_BAIT,
            ScamSignal.PARCEL_INVITATION_SMISHING to RiskSignalCategory.LINKS_AND_SMISHING,
            ScamSignal.OVERSEAS_PAYMENT_ALERT to RiskSignalCategory.FINANCIAL_BAIT
        )

        assertEquals(ScamSignal.entries.toSet(), expectedCategories.keys)
        ScamSignal.entries.forEach { signal ->
            val card = explainer.cards.single { candidate -> candidate.id == signal }
            assertEquals(signal.label, card.label)
            assertEquals(signal.name, card.signalKey)
            assertEquals(expectedCategories.getValue(signal), card.category)
            assertTrue(card.category.label.isNotBlank())
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendFamilyCardsAndActionsFirst() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val explainer = RiskSignalExplainerBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                listOf(
                    ScamSignal.FAMILY_EMERGENCY,
                    ScamSignal.PHONE_NUMBER_CHANGE,
                    ScamSignal.DEEPFAKE_VOICE
                ),
                explainer.recommendedSignalIds.take(3)
            )
            assertEquals(
                listOf(
                    RiskSignalExplainerActionId.FAMILY_CALLBACK_CARD,
                    RiskSignalExplainerActionId.FAMILY_ALERT_MESSAGES
                ),
                explainer.recommendedActionIds.take(2)
            )
        }
    }

    @Test
    fun moneyCredentialSafeAccountGiftAndCryptoRecommendMoneyCredentialAndOfficialActions() {
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
            val explainer = RiskSignalExplainerBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                listOf(ScamSignal.URGENT_TRANSFER, ScamSignal.CREDENTIALS),
                explainer.recommendedSignalIds.take(2)
            )
            listOf(
                RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS,
                RiskSignalExplainerActionId.ACCOUNT_LOCKDOWN,
                RiskSignalExplainerActionId.OFFICIAL_HELP_DIRECTORY
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in explainer.recommendedActionIds)
            }
        }
    }

    @Test
    fun remoteControlAppAndScreenShareRecommendProtectionWithoutRemovalGuarantee() {
        listOf(
            ScamSignal.APP_INSTALL,
            ScamSignal.LIVE_CALL_CONTROL
        ).forEach { signal ->
            val explainer = RiskSignalExplainerBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL),
                explainer.recommendedSignalIds.take(2)
            )
            assertEquals(
                listOf(
                    RiskSignalExplainerActionId.ACCOUNT_LOCKDOWN,
                    RiskSignalExplainerActionId.EVIDENCE_KIT,
                    RiskSignalExplainerActionId.TRUSTED_HELPER_HANDOFF
                ),
                explainer.recommendedActionIds.take(3)
            )
            assertFalse(allCopy(explainer).contains("악성 앱을 자동으로 제거합니다"))
            assertFalse(allCopy(explainer).contains("악성코드 제거를 보장합니다"))
        }
    }

    @Test
    fun linkAndSmishingRecommendEvidenceFamilyAlertAndCurrentResultWithoutAutoScanClaim() {
        listOf(
            ScamSignal.LINK,
            ScamSignal.PARCEL_INVITATION_SMISHING
        ).forEach { signal ->
            val explainer = RiskSignalExplainerBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING),
                explainer.recommendedSignalIds.take(2)
            )
            assertEquals(
                listOf(
                    RiskSignalExplainerActionId.EVIDENCE_KIT,
                    RiskSignalExplainerActionId.FAMILY_ALERT_MESSAGES,
                    RiskSignalExplainerActionId.ANALYSIS_RESULT
                ),
                explainer.recommendedActionIds.take(3)
            )
            assertFalse(allCopy(explainer).contains("자동으로 검사합니다"))
            assertFalse(allCopy(explainer).contains("자동 스캔합니다"))
        }
    }

    @Test
    fun highRiskAlwaysIncludesQuickStopAndRecoveryPlanActions() {
        val highRiskSignals = RiskSignalExplainerBuilder.build(
            analysis = analysisWith(
                ScamSignal.DEEPFAKE_VOICE,
                ScamSignal.CREDENTIALS,
                riskLevel = RiskLevel.HIGH
            ),
            readiness = readiness(ready = true)
        )
        val highRiskWithoutSignals = RiskSignalExplainerBuilder.build(
            analysis = ScamAnalysis("테스트", 80, RiskLevel.HIGH, emptyList()),
            readiness = readiness()
        )

        listOf(highRiskSignals, highRiskWithoutSignals).forEach { explainer ->
            assertEquals(
                listOf(
                    RiskSignalExplainerActionId.QUICK_STOP,
                    RiskSignalExplainerActionId.RECOVERY_PLAN
                ),
                explainer.recommendedActionIds.take(2)
            )
            assertTrue(RiskSignalExplainerActionId.QUICK_STOP in explainer.recommendedActionIds)
            assertTrue(RiskSignalExplainerActionId.RECOVERY_PLAN in explainer.recommendedActionIds)
        }
    }

    @Test
    fun everyCardHasCompleteEducationalFieldsActionsAndStableOrder() {
        listOf(
            RiskSignalExplainerBuilder.build(null, readiness()),
            RiskSignalExplainerBuilder.build(
                analysisWith(
                    ScamSignal.APP_INSTALL,
                    ScamSignal.CREDENTIALS,
                    ScamSignal.LINK,
                    ScamSignal.PHONE_NUMBER_CHANGE
                ),
                readiness(ready = true)
            )
        ).forEach { explainer ->
            assertTrue(explainer.title.isNotBlank())
            assertTrue(explainer.intro.isNotBlank())
            assertTrue(explainer.recommendationTitle.isNotBlank())
            assertTrue(explainer.recommendationExplanation.isNotBlank())
            assertTrue(explainer.privacySafetyNotes.isNotEmpty())
            assertTrue(explainer.privacySafetyNotes.all { note -> note.isNotBlank() })
            assertEquals(ScamSignal.entries, explainer.cards.map { card -> card.id })
            assertEquals((1..17).toList(), explainer.cards.map { card -> card.displayOrder })

            explainer.cards.forEach { card ->
                assertTrue(card.category.label.isNotBlank())
                assertTrue(card.urgency.rank >= 0)
                assertTrue(card.urgency.label.isNotBlank())
                assertTrue(card.label.isNotBlank())
                assertTrue(card.whySuspicious.isNotBlank())
                assertTrue(card.scamPressureGoal.isNotBlank())
                assertTrue(card.immediateSafeMove.isNotBlank())
                assertTrue(card.familyDiscussionPrompt.isNotBlank())
                assertTrue(card.primaryActionLabel.isNotBlank())
                assertTrue(card.secondaryActions.isNotEmpty())
                assertTrue(card.secondaryActionLabels.all { label -> label.isNotBlank() })
                assertEquals(card.secondaryActions.map { action -> action.label }, card.secondaryActionLabels)
            }
        }
    }

    @Test
    fun shareContainsOnlySignalCategoryActionLabelsAndGenericMovesWithoutSecrets() {
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
        val explainer = RiskSignalExplainerBuilder.build(
            analysis = analysis,
            readiness = FamilyReadinessBuilder.build(
                SafetyPlan(
                    safeWord = safeWord,
                    callbackScript = callbackScript,
                    contacts = listOf(TrustedContact(contactLabel, contactNumber))
                )
            )
        )
        val message = explainer.familyShareSummary
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)

        explainer.cards.forEach { card ->
            assertTrue("share omitted signal: ${card.label}", message.contains(card.label))
            assertTrue("share omitted category: ${card.category.label}", message.contains(card.category.label))
            (listOf(card.primaryAction) + card.secondaryActions).forEach { action ->
                assertTrue("share omitted action: ${action.label}", message.contains(action.label))
            }
        }
        explainer.recommendedActions.forEach { action ->
            assertTrue(message.contains(action.label))
        }
        assertTrue(message.contains("일반 안전 행동"))
        assertTrue(message.contains("원문") && message.contains("포함하지 않았습니다"))
        assertFalse(message.contains(raw))
        assertFalse(message.contains(redactedClue))
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
            assertFalse("risk signal share leaked secret: $secret", message.contains(secret))
        }
        assertFalse(Regex("0\\d{1,2}[- .]?\\d{3,4}[- .]?\\d{4}").containsMatchIn(message))
        assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(message))
        assertFalse(
            Regex("(?i)(?:[a-z][a-z0-9+.-]*://|www\\.|[a-z0-9-]+\\.(?:com|net|org|kr|한국))")
                .containsMatchIn(message)
        )
        assertFalse(
            Regex("(?<!\\d)\\d{6}\\s*-?\\s*[1-8]\\d{6}(?!\\d)")
                .containsMatchIn(message)
        )
    }

    @Test
    fun outputIsDeterministicForTheSameInputs() {
        val analysis = analysisWith(
            ScamSignal.APP_INSTALL,
            ScamSignal.CREDENTIALS,
            ScamSignal.LINK,
            ScamSignal.PHONE_NUMBER_CHANGE,
            riskLevel = RiskLevel.HIGH
        )
        val readinessValue = readiness(ready = true)

        val first = RiskSignalExplainerBuilder.build(analysis, readinessValue)
        val second = RiskSignalExplainerBuilder.build(analysis, readinessValue)

        assertEquals(first, second)
        assertEquals(first.cards, second.cards)
        assertEquals(first.recommendedSignalIds, second.recommendedSignalIds)
        assertEquals(first.recommendedActionIds, second.recommendedActionIds)
        assertEquals(first.familyShareSummary, second.familyShareSummary)
    }

    @Test
    fun copyStatesManualLocalNoStorageNoContactReadingNoAutoSendNoMonitoringAndNoNetwork() {
        val explainers = representativeExplainers()

        explainers.forEach { explainer ->
            val privacyCopy = explainer.privacySafetyNotes.joinToString(" ")
            assertTrue(privacyCopy.contains("수동 교육 안내"))
            assertTrue(privacyCopy.contains("기기 안"))
            assertTrue(privacyCopy.contains("사용자가 직접"))
            assertTrue(privacyCopy.contains("저장하지 않"))
            assertTrue(privacyCopy.contains("연락처를 읽거나 받는 사람을 고르지 않"))
            assertTrue(privacyCopy.contains("자동으로 보내지 않"))
            assertTrue(privacyCopy.contains("전화를 자동으로 걸지 않"))
            assertTrue(privacyCopy.contains("자동으로 읽거나 감시하지 않"))
            assertTrue(privacyCopy.contains("네트워크"))
            assertTrue(
                privacyCopy.contains("사건 기록") &&
                    privacyCopy.contains("추가·수정·삭제하지 않습니다")
            )
            assertTrue(privacyCopy.contains("새 Android 권한"))
            assertTrue(explainer.intro.contains("원격 AI"))
            assertTrue(explainer.intro.contains("공식 기관의 확정 판정이 아닌"))
        }
    }

    @Test
    fun copyDoesNotImplyConclusiveDetectionMonitoringBlockingRemovalFreezeRecoveryOrGuarantees() {
        val forbiddenAffirmativeClaims = listOf(
            "모든 사기를 찾아냅니다",
            "사기를 확정합니다",
            "공식 판정입니다",
            "자동으로 감시합니다",
            "자동 모니터링합니다",
            "몰래 확인합니다",
            "자동으로 검사합니다",
            "자동 스캔합니다",
            "자동으로 읽습니다",
            "연락처를 자동으로 읽습니다",
            "서버로 업로드합니다",
            "네트워크로 업로드합니다",
            "문자를 자동으로 차단합니다",
            "전화를 자동으로 차단합니다",
            "악성 앱을 자동으로 제거합니다",
            "악성코드 제거를 보장합니다",
            "계좌를 자동으로 지급정지합니다",
            "카드를 자동으로 정지합니다",
            "계정을 자동으로 잠급니다",
            "피해금 회수를 보장합니다",
            "법적 결과를 보장합니다",
            "안전을 보장합니다",
            "자동으로 삭제합니다",
            "비밀값에 접근합니다",
            "100%",
            "automatic monitoring",
            "conclusive detection",
            "server upload",
            "malware removal guarantee",
            "account freeze guarantee",
            "guaranteed recovery",
            "legal guarantee"
        )

        representativeExplainers().forEach { explainer ->
            val copy = allCopy(explainer)
            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "risk signal explainer copy contained unsafe claim: $claim",
                    copy.lowercase().contains(claim.lowercase())
                )
            }
        }
    }

    private fun representativeExplainers(): List<RiskSignalExplainer> = listOf(
        RiskSignalExplainerBuilder.build(null, readiness()),
        RiskSignalExplainerBuilder.build(analysisWith(ScamSignal.APP_INSTALL), readiness()),
        RiskSignalExplainerBuilder.build(
            analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER),
            readiness(ready = true)
        ),
        RiskSignalExplainerBuilder.build(analysisWith(ScamSignal.LINK), readiness()),
        RiskSignalExplainerBuilder.build(analysisWith(ScamSignal.DEEPFAKE_VOICE), readiness())
    )

    private fun analysisWith(
        vararg signals: ScamSignal,
        riskLevel: RiskLevel = RiskLevel.MEDIUM
    ): ScamAnalysis = ScamAnalysis(
        input = "테스트 입력 ${signals.joinToString { signal -> signal.name }}",
        score = if (riskLevel == RiskLevel.HIGH) 80 else 50,
        riskLevel = riskLevel,
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

    private fun allCopy(explainer: RiskSignalExplainer): String = buildString {
        append(explainer.title)
        append(explainer.intro)
        append(explainer.recommendationTitle)
        append(explainer.recommendationExplanation)
        explainer.recommendedActions.forEach { action -> append(action.label) }
        explainer.cards.forEach { card ->
            append(card.signalKey)
            append(card.category.label)
            append(card.urgency.label)
            append(card.label)
            append(card.whySuspicious)
            append(card.scamPressureGoal)
            append(card.immediateSafeMove)
            append(card.familyDiscussionPrompt)
            append(card.primaryActionLabel)
            card.secondaryActionLabels.forEach(::append)
        }
        explainer.privacySafetyNotes.forEach(::append)
        append(explainer.familyShareSummary)
    }
}
