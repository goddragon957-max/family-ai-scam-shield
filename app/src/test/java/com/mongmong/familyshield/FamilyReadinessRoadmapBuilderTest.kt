package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyReadinessRoadmapBuilderTest {
    @Test
    fun lowReadinessRecommendsSafetyPlanCallbackAndQuickStopInOrder() {
        val roadmap = FamilyReadinessRoadmapBuilder.build(
            analysis = null,
            readiness = lowReadiness()
        )

        assertEquals(
            listOf(
                FamilyReadinessRoadmapCardId.SAFETY_PLAN_FOUNDATION,
                FamilyReadinessRoadmapCardId.CALLBACK_ROUTINE_PRACTICE,
                FamilyReadinessRoadmapCardId.QUICK_STOP_PREPARATION
            ),
            roadmap.recommendedCardIds.take(3)
        )
        assertEquals(
            listOf(
                FamilyReadinessRoadmapActionId.SAFETY_PLAN,
                FamilyReadinessRoadmapActionId.FAMILY_CALLBACK_CARD,
                FamilyReadinessRoadmapActionId.FAMILY_SAFETY_DRILL_COACH,
                FamilyReadinessRoadmapActionId.QUICK_STOP
            ),
            roadmap.recommendedActionIds.take(4)
        )
        assertEquals(ReadinessLevel.LOW, roadmap.readinessSummary.level)
        assertEquals(0, roadmap.readinessSummary.completedCount)
        assertEquals(4, roadmap.readinessSummary.totalCount)
        assertEquals(4, roadmap.readinessSummary.remainingCount)
        assertTrue(roadmap.recommendationExplanation.contains("차분히 시작"))
    }

    @Test
    fun inProgressReadinessRecommendsFollowUpDrillSafeCallsAndPrivacyInOrder() {
        val roadmap = FamilyReadinessRoadmapBuilder.build(
            analysis = analysisWith(),
            readiness = inProgressReadiness()
        )

        assertEquals(
            listOf(
                FamilyReadinessRoadmapCardId.SAFETY_PLAN_FOUNDATION,
                FamilyReadinessRoadmapCardId.CALLBACK_ROUTINE_PRACTICE,
                FamilyReadinessRoadmapCardId.QUICK_STOP_PREPARATION,
                FamilyReadinessRoadmapCardId.PRIVACY_LOCAL_DATA_REVIEW
            ),
            roadmap.recommendedCardIds.take(4)
        )
        assertEquals(
            listOf(
                FamilyReadinessRoadmapActionId.SAFETY_PLAN,
                FamilyReadinessRoadmapActionId.FAMILY_SAFETY_DRILL_COACH,
                FamilyReadinessRoadmapActionId.SAFE_CALL_SCRIPTS,
                FamilyReadinessRoadmapActionId.PRIVACY_CENTER
            ),
            roadmap.recommendedActionIds.take(4)
        )
        val planCard = roadmap.cards.single {
            card -> card.id == FamilyReadinessRoadmapCardId.SAFETY_PLAN_FOUNDATION
        }
        assertEquals(FamilyReadinessRoadmapStatus.FOLLOW_UP, planCard.readinessStatus)
        assertTrue(planCard.primaryAction.label.contains("보완"))
        assertEquals(ReadinessLevel.IN_PROGRESS, roadmap.readinessSummary.level)
        assertTrue(roadmap.recommendationExplanation.contains("진행 중"))
    }

    @Test
    fun readyReadinessRecommendsPracticePrivacyMaintenanceAndRiskLearningInOrder() {
        val low = FamilyReadinessRoadmapBuilder.build(null, lowReadiness())
        val ready = FamilyReadinessRoadmapBuilder.build(
            analysis = analysisWith(),
            readiness = readyReadiness()
        )

        assertEquals(
            listOf(
                FamilyReadinessRoadmapCardId.CALLBACK_ROUTINE_PRACTICE,
                FamilyReadinessRoadmapCardId.PRIVACY_LOCAL_DATA_REVIEW,
                FamilyReadinessRoadmapCardId.RISK_SIGNAL_LEARNING
            ),
            ready.recommendedCardIds.take(3)
        )
        assertEquals(
            listOf(
                FamilyReadinessRoadmapActionId.FAMILY_SAFETY_DRILL_COACH,
                FamilyReadinessRoadmapActionId.PRIVACY_CENTER,
                FamilyReadinessRoadmapActionId.RISK_SIGNAL_EXPLAINERS
            ),
            ready.recommendedActionIds.take(3)
        )
        assertEquals(ReadinessLevel.READY, ready.readinessSummary.level)
        assertEquals(4, ready.readinessSummary.completedCount)
        assertEquals(0, ready.readinessSummary.remainingCount)
        assertTrue(ready.recommendationExplanation.contains("가상 연습"))
        assertTrue(ready.recommendationExplanation.contains("주간 개인정보 점검"))
        assertTrue(ready.recommendationExplanation.contains("위험 신호 학습"))
        assertNotEquals(low.recommendationExplanation, ready.recommendationExplanation)
    }

    @Test
    fun noAnalysisRecommendsCheckerSampleAndRiskEducationWithoutSafeVerdict() {
        val roadmap = FamilyReadinessRoadmapBuilder.build(
            analysis = null,
            readiness = readyReadiness()
        )

        listOf(
            FamilyReadinessRoadmapActionId.CHECKER,
            FamilyReadinessRoadmapActionId.SAMPLE_LIBRARY,
            FamilyReadinessRoadmapActionId.RISK_SIGNAL_EXPLAINERS
        ).forEach { actionId ->
            assertTrue("no-analysis roadmap omitted $actionId", actionId in roadmap.recommendedActionIds)
        }
        assertTrue(FamilyReadinessRoadmapCardId.RISK_SIGNAL_LEARNING in roadmap.recommendedCardIds)
        assertEquals("분석 전 가족 준비 순서", roadmap.recommendationTitle)
        assertTrue(roadmap.recommendationExplanation.contains("현재 분석 결과가 없어"))
        assertTrue(roadmap.recommendationExplanation.contains("괜찮다고 단정하지 않습니다"))
        listOf("안전합니다", "위험 없음", "사기 아님", "문제없습니다").forEach { verdict ->
            assertFalse(roadmap.recommendationExplanation.contains(verdict))
        }

        val riskCard = roadmap.cards.single {
            card -> card.id == FamilyReadinessRoadmapCardId.RISK_SIGNAL_LEARNING
        }
        assertEquals(FamilyReadinessRoadmapActionId.CHECKER, riskCard.primaryAction.id)
        assertTrue(
            riskCard.secondaryActions.any {
                action -> action.id == FamilyReadinessRoadmapActionId.SAMPLE_LIBRARY
            }
        )
        assertTrue(
            riskCard.secondaryActions.any {
                action -> action.id == FamilyReadinessRoadmapActionId.RISK_SIGNAL_EXPLAINERS
            }
        )
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendCallbackAlertAndHelperActions() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val roadmap = FamilyReadinessRoadmapBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertEquals(
                listOf(
                    FamilyReadinessRoadmapCardId.CALLBACK_ROUTINE_PRACTICE,
                    FamilyReadinessRoadmapCardId.FAMILY_HELPER_NETWORK
                ),
                roadmap.recommendedCardIds.take(2)
            )
            assertEquals(
                listOf(
                    FamilyReadinessRoadmapActionId.FAMILY_CALLBACK_CARD,
                    FamilyReadinessRoadmapActionId.FAMILY_ALERT_MESSAGES,
                    FamilyReadinessRoadmapActionId.TRUSTED_HELPER_HANDOFF
                ),
                roadmap.recommendedActionIds.take(3)
            )
        }
    }

    @Test
    fun financialCredentialSafeAccountGiftCryptoAndPaymentRecommendProtectionHelpAndRecovery() {
        listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.IMPERSONATION,
            ScamSignal.BAIT,
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val roadmap = FamilyReadinessRoadmapBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            listOf(
                FamilyReadinessRoadmapActionId.SAFE_CALL_SCRIPTS,
                FamilyReadinessRoadmapActionId.ACCOUNT_LOCKDOWN,
                FamilyReadinessRoadmapActionId.OFFICIAL_HELP_DIRECTORY,
                FamilyReadinessRoadmapActionId.RECOVERY_PLAN
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in roadmap.recommendedActionIds)
            }
            assertTrue(
                FamilyReadinessRoadmapCardId.INCIDENT_RESPONSE_PREPARATION in
                    roadmap.recommendedCardIds
            )
            assertTrue(
                FamilyReadinessRoadmapCardId.EVIDENCE_ACCOUNT_PROTECTION in
                    roadmap.recommendedCardIds
            )
        }
    }

    @Test
    fun remoteControlAppAndScreenShareRecommendAccountEvidenceAndHelperWithoutRemovalGuarantee() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val roadmap = FamilyReadinessRoadmapBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            listOf(
                FamilyReadinessRoadmapActionId.ACCOUNT_LOCKDOWN,
                FamilyReadinessRoadmapActionId.EVIDENCE_KIT,
                FamilyReadinessRoadmapActionId.TRUSTED_HELPER_HANDOFF
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in roadmap.recommendedActionIds)
            }
            assertTrue(
                FamilyReadinessRoadmapCardId.EVIDENCE_ACCOUNT_PROTECTION in
                    roadmap.recommendedCardIds
            )
            assertTrue(
                FamilyReadinessRoadmapCardId.FAMILY_HELPER_NETWORK in
                    roadmap.recommendedCardIds
            )
            assertFalse(allCopy(roadmap).contains("악성 앱을 자동으로 제거합니다"))
            assertFalse(allCopy(roadmap).contains("악성코드 제거를 보장합니다"))
        }
    }

    @Test
    fun linkAndSmishingRecommendRiskEvidenceAndFamilyAlertWithoutAutoScanClaim() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val roadmap = FamilyReadinessRoadmapBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertEquals(
                listOf(
                    FamilyReadinessRoadmapCardId.RISK_SIGNAL_LEARNING,
                    FamilyReadinessRoadmapCardId.EVIDENCE_ACCOUNT_PROTECTION,
                    FamilyReadinessRoadmapCardId.FAMILY_HELPER_NETWORK
                ),
                roadmap.recommendedCardIds.take(3)
            )
            assertEquals(
                listOf(
                    FamilyReadinessRoadmapActionId.RISK_SIGNAL_EXPLAINERS,
                    FamilyReadinessRoadmapActionId.EVIDENCE_KIT,
                    FamilyReadinessRoadmapActionId.FAMILY_ALERT_MESSAGES
                ),
                roadmap.recommendedActionIds.take(3)
            )
            assertFalse(allCopy(roadmap).contains("자동으로 검사합니다"))
            assertFalse(allCopy(roadmap).contains("자동 스캔합니다"))
        }
    }

    @Test
    fun highRiskAndDangerousSignalsPutQuickStopAndRecoveryBeforePreventionCards() {
        val highWithSignals = FamilyReadinessRoadmapBuilder.build(
            analysis = analysisWith(
                ScamSignal.DEEPFAKE_VOICE,
                ScamSignal.CREDENTIALS,
                riskLevel = RiskLevel.HIGH
            ),
            readiness = lowReadiness()
        )
        val highWithoutSignals = FamilyReadinessRoadmapBuilder.build(
            analysis = ScamAnalysis("테스트", 80, RiskLevel.HIGH, emptyList()),
            readiness = readyReadiness()
        )
        val dangerousSignals = listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.CREDENTIALS,
            ScamSignal.APP_INSTALL,
            ScamSignal.LIVE_CALL_CONTROL,
            ScamSignal.SECRECY
        ).map { signal ->
            FamilyReadinessRoadmapBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )
        }

        (listOf(highWithSignals, highWithoutSignals) + dangerousSignals).forEach { roadmap ->
            assertEquals(
                listOf(
                    FamilyReadinessRoadmapCardId.QUICK_STOP_PREPARATION,
                    FamilyReadinessRoadmapCardId.INCIDENT_RESPONSE_PREPARATION
                ),
                roadmap.recommendedCardIds.take(2)
            )
            assertEquals(
                listOf(
                    FamilyReadinessRoadmapActionId.QUICK_STOP,
                    FamilyReadinessRoadmapActionId.RECOVERY_PLAN
                ),
                roadmap.recommendedActionIds.take(2)
            )
        }
    }

    @Test
    fun everyCardHasCompleteFieldsActionsPriorityAndStableOrder() {
        val noAnalysis = FamilyReadinessRoadmapBuilder.build(null, lowReadiness())
        val withAnalysis = FamilyReadinessRoadmapBuilder.build(
            analysis = analysisWith(
                ScamSignal.APP_INSTALL,
                ScamSignal.CREDENTIALS,
                ScamSignal.LINK,
                ScamSignal.PHONE_NUMBER_CHANGE,
                riskLevel = RiskLevel.HIGH
            ),
            readiness = readyReadiness()
        )

        listOf(noAnalysis, withAnalysis).forEach { roadmap ->
            assertTrue(roadmap.title.isNotBlank())
            assertTrue(roadmap.intro.isNotBlank())
            assertTrue(roadmap.recommendationTitle.isNotBlank())
            assertTrue(roadmap.recommendationExplanation.isNotBlank())
            assertTrue(roadmap.privacySafetyNotes.isNotEmpty())
            assertEquals(
                FamilyReadinessRoadmapCardId.entries,
                roadmap.cards.map { card -> card.id }
            )
            assertEquals(
                (1..FamilyReadinessRoadmapCardId.entries.size).toList(),
                roadmap.cards.map { card -> card.displayOrder }
            )
            assertEquals(roadmap.cards.size, roadmap.cards.map { card -> card.id }.distinct().size)
            assertEquals(
                roadmap.recommendedCardIds,
                roadmap.recommendedCardIds.distinct()
            )
            assertEquals(
                roadmap.recommendedCardIds,
                roadmap.recommendedCards.map { card -> card.id }
            )
            assertEquals(
                roadmap.recommendedActionIds,
                roadmap.recommendedActionIds.distinct()
            )

            roadmap.cards.forEach { card ->
                assertEquals(card.id.displayOrder, card.displayOrder)
                assertTrue(card.stage.label.isNotBlank())
                assertTrue(card.title.isNotBlank())
                assertTrue(card.readinessStatus.label.isNotBlank())
                assertTrue(card.whyItMatters.isNotBlank())
                assertTrue(card.nextSmallSteps.size >= 2)
                assertTrue(card.nextSmallSteps.all { step -> step.isNotBlank() })
                assertTrue(card.avoidNotes.size >= 2)
                assertTrue(card.avoidNotes.all { note -> note.isNotBlank() })
                assertTrue(card.priority.rank >= 0)
                assertTrue(card.priority.label.isNotBlank())
                assertTrue(card.primaryActionLabel.isNotBlank())
                assertTrue(card.secondaryActions.isNotEmpty())
                assertTrue(card.secondaryActionLabels.all { label -> label.isNotBlank() })
                assertEquals(
                    card.secondaryActions.map { action -> action.label },
                    card.secondaryActionLabels
                )
            }
        }

        val routedActionIds = (noAnalysis.cards + withAnalysis.cards)
            .flatMap { card -> listOf(card.primaryAction) + card.secondaryActions }
            .map { action -> action.id }
            .toSet()
        assertEquals(FamilyReadinessRoadmapActionId.entries.toSet(), routedActionIds)

        val expectedContracts = mapOf(
            FamilyReadinessRoadmapCardId.SAFETY_PLAN_FOUNDATION to ExpectedCardContract(
                stage = FamilyReadinessRoadmapStage.FOUNDATION,
                title = "가족 안전 계획 시작",
                requiredActionIds = setOf(FamilyReadinessRoadmapActionId.SAFETY_PLAN)
            ),
            FamilyReadinessRoadmapCardId.CALLBACK_ROUTINE_PRACTICE to ExpectedCardContract(
                stage = FamilyReadinessRoadmapStage.VERIFY,
                title = "콜백 루틴 연습",
                requiredActionIds = setOf(
                    FamilyReadinessRoadmapActionId.FAMILY_CALLBACK_CARD,
                    FamilyReadinessRoadmapActionId.FAMILY_SAFETY_DRILL_COACH
                )
            ),
            FamilyReadinessRoadmapCardId.RISK_SIGNAL_LEARNING to ExpectedCardContract(
                stage = FamilyReadinessRoadmapStage.LEARN,
                title = "위험 신호 이해",
                requiredActionIds = setOf(
                    FamilyReadinessRoadmapActionId.RISK_SIGNAL_EXPLAINERS,
                    FamilyReadinessRoadmapActionId.CHECKER,
                    FamilyReadinessRoadmapActionId.SAMPLE_LIBRARY
                )
            ),
            FamilyReadinessRoadmapCardId.QUICK_STOP_PREPARATION to ExpectedCardContract(
                stage = FamilyReadinessRoadmapStage.STOP,
                title = "긴급 멈춤 준비",
                requiredActionIds = setOf(
                    FamilyReadinessRoadmapActionId.QUICK_STOP,
                    FamilyReadinessRoadmapActionId.SAFE_CALL_SCRIPTS
                )
            ),
            FamilyReadinessRoadmapCardId.INCIDENT_RESPONSE_PREPARATION to ExpectedCardContract(
                stage = FamilyReadinessRoadmapStage.RESPOND,
                title = "피해 의심 시 대응 준비",
                requiredActionIds = setOf(
                    FamilyReadinessRoadmapActionId.RECOVERY_PLAN,
                    FamilyReadinessRoadmapActionId.OFFICIAL_HELP_DIRECTORY,
                    FamilyReadinessRoadmapActionId.RESPONSE_CENTER
                )
            ),
            FamilyReadinessRoadmapCardId.EVIDENCE_ACCOUNT_PROTECTION to ExpectedCardContract(
                stage = FamilyReadinessRoadmapStage.PROTECT,
                title = "증거와 계정 보호 준비",
                requiredActionIds = setOf(
                    FamilyReadinessRoadmapActionId.EVIDENCE_KIT,
                    FamilyReadinessRoadmapActionId.ACCOUNT_LOCKDOWN
                )
            ),
            FamilyReadinessRoadmapCardId.FAMILY_HELPER_NETWORK to ExpectedCardContract(
                stage = FamilyReadinessRoadmapStage.CONNECT,
                title = "가족 경고/도우미 네트워크",
                requiredActionIds = setOf(
                    FamilyReadinessRoadmapActionId.FAMILY_ALERT_MESSAGES,
                    FamilyReadinessRoadmapActionId.TRUSTED_HELPER_HANDOFF
                )
            ),
            FamilyReadinessRoadmapCardId.PRIVACY_LOCAL_DATA_REVIEW to ExpectedCardContract(
                stage = FamilyReadinessRoadmapStage.REVIEW,
                title = "개인정보와 로컬 데이터 점검",
                requiredActionIds = setOf(FamilyReadinessRoadmapActionId.PRIVACY_CENTER)
            )
        )
        assertEquals(FamilyReadinessRoadmapCardId.entries.toSet(), expectedContracts.keys)
        expectedContracts.forEach { (cardId, contract) ->
            val cards = listOf(
                noAnalysis.cards.single { card -> card.id == cardId },
                withAnalysis.cards.single { card -> card.id == cardId }
            )
            cards.forEach { card ->
                assertEquals(contract.stage, card.stage)
                assertEquals(contract.title, card.title)
            }
            val actionIds = cards
                .flatMap { card -> listOf(card.primaryAction) + card.secondaryActions }
                .map { action -> action.id }
                .toSet()
            assertTrue(
                "$cardId omitted required actions ${contract.requiredActionIds}",
                actionIds.containsAll(contract.requiredActionIds)
            )
        }
    }

    @Test
    fun shareUsesOnlyStagesTitlesActionLabelsAndGenericStepsWithoutSecrets() {
        val safeWord = "해질녘 파란 우산은 어디에?"
        val callbackScript = "푸른 메모장을 열고 첫 번째 저장 번호로 전화한다."
        val contactLabel = "보호자-은하수"
        val contactNumber = "010-2468-1357"
        val raw =
            "RAW-ROADMAP-COMET 전화 010-9876-5432 계좌 123456789012 비밀번호 BluePassword77 " +
                "OTP A1B2C3 PIN 7788 https://evil.example/path 주민번호 900101-1234567 " +
                "안전 질문 $safeWord 콜백 $callbackScript 연락처 $contactLabel $contactNumber"
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
        val readiness = FamilyReadinessBuilder.build(
            SafetyPlan(
                safeWord = safeWord,
                callbackScript = callbackScript,
                contacts = listOf(TrustedContact(contactLabel, contactNumber))
            )
        )
        val roadmap = FamilyReadinessRoadmapBuilder.build(analysis, readiness)
        val message = roadmap.familyShareSummary
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)

        assertEquals(expectedFamilyShareSummary(roadmap), message)
        roadmap.cards.forEach { card ->
            assertTrue(message.contains(card.stage.label))
            assertTrue(message.contains(card.title))
            assertTrue(message.contains(card.nextSmallSteps.first()))
            (listOf(card.primaryAction) + card.secondaryActions).forEach { action ->
                assertTrue("share omitted action: ${action.label}", message.contains(action.label))
            }
            assertFalse(message.contains(card.whyItMatters))
            card.avoidNotes.forEach { note -> assertFalse(message.contains(note)) }
            card.nextSmallSteps.drop(1).forEach { step -> assertFalse(message.contains(step)) }
        }
        assertFalse(message.contains(raw))
        assertFalse(message.contains(redactedClue))
        val secrets = listOf(
            "RAW-ROADMAP-COMET",
            safeWord,
            callbackScript,
            contactLabel,
            contactNumber,
            "010-9876-5432",
            "123456789012",
            "BluePassword77",
            "A1B2C3",
            "654321",
            "7788",
            "evil.example",
            "900101-1234567"
        )
        secrets.forEach { secret ->
            assertFalse("roadmap share leaked secret: $secret", message.contains(secret))
        }
        val completeCopy = allCopy(roadmap)
        assertFalse(completeCopy.contains(raw))
        assertFalse(completeCopy.contains(redactedClue))
        secrets.forEach { secret ->
            assertFalse("roadmap output leaked secret: $secret", completeCopy.contains(secret))
        }
        assertFalse(phonePattern.containsMatchIn(message))
        assertFalse(longNumberPattern.containsMatchIn(message))
        assertFalse(groupedNumberPattern.containsMatchIn(message))
        assertFalse(urlDomainPattern.containsMatchIn(message))
        assertFalse(residentPattern.containsMatchIn(message))

        val adversarialReadiness = FamilyReadiness(
            level = ReadinessLevel.READY,
            title = raw,
            completedCount = 99,
            totalCount = -4,
            checklist = listOf(
                ReadinessItem(
                    label = "$safeWord $contactNumber",
                    done = false,
                    action = "$callbackScript https://evil.example"
                )
            ),
            nextAction = "계좌 123456789012 OTP 654321"
        )
        val adversarialRoadmap = FamilyReadinessRoadmapBuilder.build(
            analysis = analysis,
            readiness = adversarialReadiness
        )
        assertEquals(0, adversarialRoadmap.readinessSummary.completedCount)
        assertEquals(0, adversarialRoadmap.readinessSummary.totalCount)
        assertEquals(0, adversarialRoadmap.readinessSummary.remainingCount)
        assertFalse(allCopy(adversarialRoadmap).contains(raw))
        secrets.forEach { secret ->
            assertFalse(
                "roadmap used caller-supplied readiness secret: $secret",
                allCopy(adversarialRoadmap).contains(secret)
            )
        }
        assertFalse(phonePattern.containsMatchIn(adversarialRoadmap.familyShareSummary))
        assertFalse(longNumberPattern.containsMatchIn(adversarialRoadmap.familyShareSummary))
        assertFalse(urlDomainPattern.containsMatchIn(adversarialRoadmap.familyShareSummary))
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
        val readiness = readyReadiness()

        val first = FamilyReadinessRoadmapBuilder.build(analysis, readiness)
        val second = FamilyReadinessRoadmapBuilder.build(analysis, readiness)

        assertEquals(first, second)
        assertEquals(first.cards, second.cards)
        assertEquals(first.recommendedCardIds, second.recommendedCardIds)
        assertEquals(first.recommendedActions, second.recommendedActions)
        assertEquals(first.recommendedActionIds, second.recommendedActionIds)
        assertEquals(first.familyShareSummary, second.familyShareSummary)
    }

    @Test
    fun copyStatesManualLocalValueLessNoStorageNoContactReadingNoAutoSendNoMonitoringAndNoMutation() {
        representativeRoadmaps().forEach { roadmap ->
            val privacyCopy = roadmap.privacySafetyNotes.joinToString(" ")
            assertTrue(privacyCopy.contains("수동 안내"))
            assertTrue(privacyCopy.contains("기기 안"))
            assertTrue(privacyCopy.contains("로컬 규칙"))
            assertTrue(privacyCopy.contains("값 없는 가족 준비"))
            assertTrue(privacyCopy.contains("실제 값을 전달하지 않"))
            assertTrue(privacyCopy.contains("로드맵 선택"))
            assertTrue(privacyCopy.contains("완료 상태"))
            assertTrue(privacyCopy.contains("체크 상태"))
            assertTrue(privacyCopy.contains("점수"))
            assertTrue(privacyCopy.contains("읽음·진행 상태"))
            assertTrue(privacyCopy.contains("공유 이력"))
            assertTrue(privacyCopy.contains("로드맵 이력"))
            assertTrue(privacyCopy.contains("저장하지 않습니다"))
            assertTrue(privacyCopy.contains("연락처를 읽거나 받는 사람을 고르지 않습니다"))
            assertTrue(privacyCopy.contains("자동으로 보내지 않습니다"))
            assertTrue(privacyCopy.contains("자동으로 읽거나 감시하지 않"))
            assertTrue(privacyCopy.contains("자동 모니터링하지 않습니다"))
            assertTrue(privacyCopy.contains("네트워크를 사용하지 않"))
            assertTrue(privacyCopy.contains("사건 기록을 추가·수정·삭제하지 않습니다"))
            assertTrue(privacyCopy.contains("새 Android 권한을 요청하지 않"))
            assertTrue(privacyCopy.contains("전화나 메시지를 자동 실행하지 않"))
        }
    }

    @Test
    fun copyDoesNotImplyGuaranteedSafetyMonitoringBlockingRemovalFreezeRecoveryLegalAdviceOrSecretAccess() {
        val forbiddenAffirmativeClaims = listOf(
            "모든 사기를 막습니다",
            "모든 사기를 예방합니다",
            "사기를 확정합니다",
            "사기를 예방합니다",
            "안전합니다",
            "안전을 보장합니다",
            "자동으로 감시합니다",
            "자동 모니터링합니다",
            "몰래 확인합니다",
            "자동으로 검사합니다",
            "자동 스캔합니다",
            "자동으로 읽습니다",
            "연락처를 자동으로 읽습니다",
            "서버로 업로드합니다",
            "네트워크로 업로드합니다",
            "자동으로 전송합니다",
            "전화를 자동으로 겁니다",
            "문자를 자동으로 차단합니다",
            "전화를 자동으로 차단합니다",
            "통화·문자를 차단합니다",
            "악성 앱을 자동으로 제거합니다",
            "악성 앱을 제거합니다",
            "악성코드를 제거합니다",
            "악성코드 제거를 보장합니다",
            "계좌를 자동으로 지급정지합니다",
            "카드를 자동으로 정지합니다",
            "계좌를 정지합니다",
            "카드를 정지합니다",
            "계좌·카드를 정지합니다",
            "계정을 자동으로 잠급니다",
            "피해금 회수를 보장합니다",
            "피해금을 회수합니다",
            "피해 복구를 보장합니다",
            "환급을 보장합니다",
            "법적 효력을 보장합니다",
            "법적 결과를 보장합니다",
            "법률 자문을 제공합니다",
            "법률 조언을 제공합니다",
            "자동으로 삭제합니다",
            "자동 삭제합니다",
            "비밀값에 접근합니다",
            "비밀정보에 접근합니다",
            "100%",
            "automatic monitoring",
            "conclusive prevention",
            "blocks calls automatically",
            "blocks messages automatically",
            "malware removal guarantee",
            "account freeze guarantee",
            "guaranteed recovery",
            "legal guarantee"
        )

        representativeRoadmaps().forEach { roadmap ->
            val copy = allCopy(roadmap)
            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "family readiness roadmap copy contained unsafe claim: $claim",
                    copy.lowercase().contains(claim.lowercase())
                )
            }
        }
    }

    private fun representativeRoadmaps(): List<FamilyReadinessRoadmap> = listOf(
        FamilyReadinessRoadmapBuilder.build(null, lowReadiness()),
        FamilyReadinessRoadmapBuilder.build(analysisWith(ScamSignal.APP_INSTALL), inProgressReadiness()),
        FamilyReadinessRoadmapBuilder.build(
            analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER),
            readyReadiness()
        ),
        FamilyReadinessRoadmapBuilder.build(analysisWith(ScamSignal.LINK), readyReadiness()),
        FamilyReadinessRoadmapBuilder.build(analysisWith(ScamSignal.DEEPFAKE_VOICE), readyReadiness())
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

    private fun lowReadiness(): FamilyReadiness = FamilyReadinessBuilder.build(
        SafetyPlan(safeWord = "", callbackScript = "", contacts = emptyList())
    )

    private fun inProgressReadiness(): FamilyReadiness = FamilyReadinessBuilder.build(
        SafetyPlan(
            safeWord = "우리 가족만 아는 질문",
            callbackScript = "",
            contacts = emptyList()
        )
    )

    private fun readyReadiness(): FamilyReadiness = FamilyReadinessBuilder.build(
        SafetyPlan(
            safeWord = "우리 가족만 아는 질문",
            callbackScript = "통화를 끝내고 저장된 번호로 다시 전화한다.",
            contacts = listOf(
                TrustedContact("가족", "저장된 번호"),
                TrustedContact("예비 가족", "예비 저장 번호")
            )
        )
    )

    private fun cardCopy(card: FamilyReadinessRoadmapCard): String = buildString {
        append(card.stage.label)
        append(card.title)
        append(card.readinessStatus.label)
        append(card.whyItMatters)
        card.nextSmallSteps.forEach(::append)
        card.avoidNotes.forEach(::append)
        append(card.priority.label)
        append(card.primaryAction.label)
        card.secondaryActions.forEach { action -> append(action.label) }
    }

    private fun allCopy(roadmap: FamilyReadinessRoadmap): String = buildString {
        append(roadmap.title)
        append(roadmap.intro)
        append(roadmap.readinessSummary.levelLabel)
        append(roadmap.recommendationTitle)
        append(roadmap.recommendationExplanation)
        roadmap.recommendedActions.forEach { action -> append(action.label) }
        roadmap.cards.forEach { card -> append(cardCopy(card)) }
        roadmap.privacySafetyNotes.forEach(::append)
        append(roadmap.familyShareSummary)
    }

    private fun expectedFamilyShareSummary(roadmap: FamilyReadinessRoadmap): String {
        val cardsById = roadmap.cards.associateBy { card -> card.id }
        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 가족 준비 로드맵")
            appendLine()
            appendLine("먼저 준비할 단계")
            roadmap.recommendedCardIds.forEachIndexed { index, cardId ->
                val card = cardsById.getValue(cardId)
                appendLine("${index + 1}. [${card.stage.label}] ${card.title}")
                appendLine("   다음 작은 단계: ${card.nextSmallSteps.first()}")
                appendLine("   앱 행동: ${actionLabels(card)}")
            }
            appendLine()
            appendLine("전체 가족 준비 지도")
            roadmap.cards.forEach { card ->
                appendLine("- [${card.stage.label}] ${card.title}")
                appendLine("  다음 작은 단계: ${card.nextSmallSteps.first()}")
                appendLine("  앱 행동: ${actionLabels(card)}")
            }
            appendLine()
            appendLine("각 단계는 가족이 직접 선택하는 일반 준비 안내이며 선택·완료·공유 이력을 저장하지 않습니다.")
            append(
                "붙여넣은 원문·개인정보 가림 단서·안전 질문·콜백 내용·연락처 값과 전화·계좌·비밀번호·" +
                    "인증번호·PIN·링크·도메인·신분정보 값은 이 공유문에 포함하지 않았습니다."
            )
        }.trim()
    }

    private fun actionLabels(card: FamilyReadinessRoadmapCard): String =
        (listOf(card.primaryAction) + card.secondaryActions)
            .joinToString(" / ") { action -> action.label }

    private data class ExpectedCardContract(
        val stage: FamilyReadinessRoadmapStage,
        val title: String,
        val requiredActionIds: Set<FamilyReadinessRoadmapActionId>
    )

    private companion object {
        val phonePattern = Regex("0\\d{1,2}[- .]?\\d{3,4}[- .]?\\d{4}")
        val longNumberPattern = Regex("(?<!\\d)\\d{6,}(?!\\d)")
        val groupedNumberPattern = Regex("(?<!\\d)(?:\\d[- .]?){5,}\\d(?!\\d)")
        val urlDomainPattern = Regex(
            "(?i)(?:[a-z][a-z0-9+.-]*://|www\\.|(?:[a-z0-9-]+\\.)+[a-z]{2,63}\\b|" +
                "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b)"
        )
        val residentPattern = Regex("(?<!\\d)\\d{6}\\s*-?\\s*[1-8]\\d{6}(?!\\d)")
    }
}
