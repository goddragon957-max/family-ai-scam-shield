package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExposureImpactTriageBuilderTest {
    @Test
    fun noAnalysisAndNoSignalsRecommendUncertainDirectChecksWithoutSafeVerdict() {
        val noAnalysis = ExposureImpactTriageBuilder.build(
            analysis = null,
            readiness = lowReadiness()
        )
        val noSignals = ExposureImpactTriageBuilder.build(
            analysis = ScamAnalysis(
                input = "일반 대화",
                score = 0,
                riskLevel = RiskLevel.SAFE,
                matchedSignals = emptyList()
            ),
            readiness = lowReadiness()
        )
        val expectedExposureIds = listOf(
            ExposureImpactId.NO_ACTION_OR_UNCERTAIN,
            ExposureImpactId.UNCLEAR_OR_MULTIPLE
        )

        listOf(noAnalysis, noSignals).forEach { triage ->
            assertEquals(expectedExposureIds, triage.recommendedExposureIds)
            assertEquals(expectedExposureIds, triage.recommendedCards.map { card -> card.id })
            listOf(
                ExposureImpactActionId.QUICK_STOP,
                ExposureImpactActionId.CHECKER,
                ExposureImpactActionId.FAMILY_CALLBACK_CARD,
                ExposureImpactActionId.FAMILY_READINESS_ROADMAP,
                ExposureImpactActionId.SAFETY_FLOW_ROUTER
            ).forEach { actionId ->
                assertTrue("default triage omitted $actionId", actionId in triage.recommendedActionIds)
            }
            assertTrue(triage.recommendationExplanation.contains("확인"))
            listOf("안전합니다", "위험 없음", "사기 아님", "문제없습니다").forEach { verdict ->
                assertFalse("default triage asserted $verdict", allCopy(triage).contains(verdict))
            }
        }
        assertTrue(noAnalysis.recommendationExplanation.contains("현재 분석"))
        assertTrue(noAnalysis.recommendationExplanation.contains("단정하지 않"))
        assertTrue(
            (noAnalysis.intro + noAnalysis.recommendationExplanation).contains("직접 확인")
        )
        assertEquals(ExposureImpactId.entries, noAnalysis.cards.map { card -> card.id })
        assertEquals(
            (1..ExposureImpactId.entries.size).toList(),
            noAnalysis.cards.map { card -> card.displayOrder }
        )
    }

    @Test
    fun highRiskFinancialCredentialSafeAccountGiftCryptoAndPaymentRecommendResponseActions() {
        val triages = listOf(
            ExposureImpactTriageBuilder.build(
                analysis = ScamAnalysis("테스트", 80, RiskLevel.HIGH, emptyList()),
                readiness = readyReadiness()
            )
        ) + listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.CREDENTIALS,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.OVERSEAS_PAYMENT_ALERT,
            ScamSignal.BAIT
        ).map { signal ->
            ExposureImpactTriageBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )
        }

        triages.forEach { triage ->
            assertTrue(
                triage.recommendedExposureIds.containsAll(
                    listOf(
                        ExposureImpactId.MONEY_OR_VALUE_SENT,
                        ExposureImpactId.CREDENTIAL_OR_PAYMENT_INFO_SHARED
                    )
                )
            )
            listOf(
                ExposureImpactActionId.QUICK_STOP,
                ExposureImpactActionId.RECOVERY_PLAN,
                ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY,
                ExposureImpactActionId.ACCOUNT_LOCKDOWN,
                ExposureImpactActionId.EVIDENCE_KIT
            ).forEach { actionId ->
                assertTrue("financial triage omitted $actionId", actionId in triage.recommendedActionIds)
            }
            assertTrue(
                triage.recommendationExplanation.contains("가능성") ||
                    triage.recommendedCards.any { card -> card.whenThisMightApply.contains("해당될 수") }
            )
            assertTrue(triage.recommendationExplanation.contains("직접 확인"))
        }
    }

    @Test
    fun appRemoteControlAndScreenShareRecommendProtectionWithoutRemovalGuarantee() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val triage = ExposureImpactTriageBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertEquals(
                ExposureImpactId.APP_OR_REMOTE_ACCESS_GRANTED,
                triage.recommendedExposureIds.first()
            )
            listOf(
                ExposureImpactActionId.ACCOUNT_LOCKDOWN,
                ExposureImpactActionId.EVIDENCE_KIT,
                ExposureImpactActionId.TRUSTED_HELPER_HANDOFF,
                ExposureImpactActionId.RESPONSE_CENTER
            ).forEach { actionId ->
                assertTrue("$signal triage omitted $actionId", actionId in triage.recommendedActionIds)
            }
            assertFalse(allCopy(triage).contains("악성 앱을 자동으로 제거합니다"))
            assertFalse(allCopy(triage).contains("악성코드 제거를 보장합니다"))
        }
    }

    @Test
    fun linkAndSmishingRecommendBothLinkPossibilitiesWithoutAutoScanClaim() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val triage = ExposureImpactTriageBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertEquals(
                listOf(
                    ExposureImpactId.LINK_CLICK_ONLY,
                    ExposureImpactId.LINK_INFO_OR_PAYMENT_ENTERED
                ),
                triage.recommendedExposureIds.take(2)
            )
            listOf(
                ExposureImpactActionId.EVIDENCE_KIT,
                ExposureImpactActionId.RISK_SIGNAL_EXPLAINERS,
                ExposureImpactActionId.FAMILY_ALERT_MESSAGES
            ).forEach { actionId ->
                assertTrue("$signal triage omitted $actionId", actionId in triage.recommendedActionIds)
            }
            assertFalse(allCopy(triage).contains("자동으로 검사합니다"))
            assertFalse(allCopy(triage).contains("자동 스캔합니다"))
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendManualFamilyVerificationActions() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val triage = ExposureImpactTriageBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertEquals(
                ExposureImpactId.FAMILY_IMPERSONATION_ANSWERED,
                triage.recommendedExposureIds.first()
            )
            listOf(
                ExposureImpactActionId.FAMILY_CALLBACK_CARD,
                ExposureImpactActionId.FAMILY_ALERT_MESSAGES,
                ExposureImpactActionId.SAFE_CALL_SCRIPTS,
                ExposureImpactActionId.TRUSTED_HELPER_HANDOFF
            ).forEach { actionId ->
                assertTrue("$signal triage omitted $actionId", actionId in triage.recommendedActionIds)
            }
        }
    }

    @Test
    fun secrecyAndImpersonationPressureIncludeUnclearComplexAndOfficialHelpRoutes() {
        listOf(ScamSignal.SECRECY, ScamSignal.IMPERSONATION).forEach { signal ->
            val triage = ExposureImpactTriageBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertTrue(
                ExposureImpactId.UNCLEAR_OR_MULTIPLE in triage.recommendedExposureIds
            )
            assertTrue(
                ExposureImpactActionId.SAFETY_FLOW_ROUTER in triage.recommendedActionIds
            )
            assertTrue(
                ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY in triage.recommendedActionIds
            )
            assertTrue(triage.recommendationExplanation.contains("직접 확인"))
        }
    }

    @Test
    fun lowReadinessAddsCallbackAndRoadmapWhileReadyCopyDiffersCalmly() {
        val analysis = analysisWith(ScamSignal.LINK)
        val low = ExposureImpactTriageBuilder.build(analysis, lowReadiness())
        val inProgress = ExposureImpactTriageBuilder.build(analysis, inProgressReadiness())
        val ready = ExposureImpactTriageBuilder.build(analysis, readyReadiness())

        assertTrue(ExposureImpactActionId.FAMILY_CALLBACK_CARD in low.recommendedActionIds)
        assertTrue(ExposureImpactActionId.FAMILY_READINESS_ROADMAP in low.recommendedActionIds)
        assertTrue(low.recommendationExplanation.contains("차분히"))
        assertTrue(low.recommendationExplanation.contains("부족해도 괜찮"))
        assertTrue(inProgress.recommendationExplanation.contains("진행 중"))
        assertTrue(ready.recommendationExplanation.contains("준비되어"))
        assertNotEquals(low.recommendationExplanation, ready.recommendationExplanation)
    }

    @Test
    fun everyExposureCardHasCompleteFieldsActionsPriorityAndStableContract() {
        val defaultTriage = ExposureImpactTriageBuilder.build(null, lowReadiness())
        val signaledTriage = ExposureImpactTriageBuilder.build(
            analysis = analysisWith(
                *ScamSignal.entries.toTypedArray(),
                riskLevel = RiskLevel.HIGH
            ),
            readiness = readyReadiness()
        )

        listOf(defaultTriage, signaledTriage).forEach { triage ->
            assertTrue(triage.title.isNotBlank())
            assertTrue(triage.intro.isNotBlank())
            assertTrue(triage.recommendationTitle.isNotBlank())
            assertTrue(triage.recommendationExplanation.isNotBlank())
            assertTrue(triage.privacySafetyNotes.isNotEmpty())
            assertTrue(triage.privacySafetyNotes.all { note -> note.isNotBlank() })
            assertEquals(ExposureImpactId.entries, triage.cards.map { card -> card.id })
            assertEquals(
                (1..ExposureImpactId.entries.size).toList(),
                triage.cards.map { card -> card.displayOrder }
            )
            assertEquals(triage.cards.size, triage.cards.map { card -> card.id }.distinct().size)
            assertEquals(
                triage.recommendedExposureIds,
                triage.recommendedExposureIds.distinct()
            )
            assertEquals(
                triage.recommendedExposureIds,
                triage.recommendedCards.map { card -> card.id }
            )
            assertEquals(
                triage.recommendedActionIds,
                triage.recommendedActionIds.distinct()
            )
            assertEquals(
                triage.recommendedActionIds,
                triage.recommendedActions.map { action -> action.id }
            )

            triage.cards.forEach { card ->
                assertEquals(card.id.displayOrder, card.displayOrder)
                assertTrue(card.label.isNotBlank())
                assertTrue(card.category.label.isNotBlank())
                assertTrue(card.whenThisMightApply.isNotBlank())
                assertTrue(card.firstSafeMove.isNotBlank())
                assertTrue(card.whatToAvoid.isNotEmpty())
                assertTrue(card.whatToAvoid.all { note -> note.isNotBlank() })
                assertTrue(card.officialHelpPrepNotes.isNotEmpty())
                assertTrue(card.officialHelpPrepNotes.all { note -> note.isNotBlank() })
                assertTrue(card.priority.rank >= 0)
                assertTrue(card.priority.label.isNotBlank())
                assertTrue(card.primaryAction.label.isNotBlank())
                assertTrue(card.secondaryActions.isNotEmpty())
                assertTrue(card.secondaryActions.all { action -> action.label.isNotBlank() })
            }
        }

        val routedActionIds = (defaultTriage.cards + signaledTriage.cards)
            .flatMap { card -> listOf(card.primaryAction) + card.secondaryActions }
            .map { action -> action.id }
            .toSet()
        assertEquals(ExposureImpactActionId.entries.toSet(), routedActionIds)

        val expectedContracts = mapOf(
            ExposureImpactId.NO_ACTION_OR_UNCERTAIN to ExpectedCardContract(
                category = ExposureImpactCategory.BEFORE_ACTION,
                labelTerms = setOf("아직", "행동", "불안"),
                requiredActionIds = setOf(
                    ExposureImpactActionId.QUICK_STOP,
                    ExposureImpactActionId.CHECKER,
                    ExposureImpactActionId.FAMILY_CALLBACK_CARD,
                    ExposureImpactActionId.FAMILY_READINESS_ROADMAP
                )
            ),
            ExposureImpactId.MONEY_OR_VALUE_SENT to ExpectedCardContract(
                category = ExposureImpactCategory.MONEY_VALUE,
                labelTerms = setOf("돈", "상품권", "가상자산"),
                requiredActionIds = setOf(
                    ExposureImpactActionId.RECOVERY_PLAN,
                    ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY,
                    ExposureImpactActionId.RESPONSE_CENTER,
                    ExposureImpactActionId.EVIDENCE_KIT
                )
            ),
            ExposureImpactId.CREDENTIAL_OR_PAYMENT_INFO_SHARED to ExpectedCardContract(
                category = ExposureImpactCategory.ACCOUNT_IDENTITY,
                labelTerms = setOf("계좌", "비밀번호", "OTP", "신분정보"),
                requiredActionIds = setOf(
                    ExposureImpactActionId.ACCOUNT_LOCKDOWN,
                    ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY,
                    ExposureImpactActionId.RECOVERY_PLAN
                )
            ),
            ExposureImpactId.APP_OR_REMOTE_ACCESS_GRANTED to ExpectedCardContract(
                category = ExposureImpactCategory.DEVICE_ACCESS,
                labelTerms = setOf("앱", "화면공유", "원격제어"),
                requiredActionIds = setOf(
                    ExposureImpactActionId.ACCOUNT_LOCKDOWN,
                    ExposureImpactActionId.EVIDENCE_KIT,
                    ExposureImpactActionId.TRUSTED_HELPER_HANDOFF,
                    ExposureImpactActionId.RESPONSE_CENTER
                )
            ),
            ExposureImpactId.LINK_CLICK_ONLY to ExpectedCardContract(
                category = ExposureImpactCategory.LINK_ACTIVITY,
                labelTerms = setOf("링크", "클릭"),
                requiredActionIds = setOf(
                    ExposureImpactActionId.EVIDENCE_KIT,
                    ExposureImpactActionId.RISK_SIGNAL_EXPLAINERS,
                    ExposureImpactActionId.ACCOUNT_LOCKDOWN,
                    ExposureImpactActionId.FAMILY_ALERT_MESSAGES
                )
            ),
            ExposureImpactId.LINK_INFO_OR_PAYMENT_ENTERED to ExpectedCardContract(
                category = ExposureImpactCategory.LINK_ACTIVITY,
                labelTerms = setOf("링크", "개인정보", "결제정보"),
                requiredActionIds = setOf(
                    ExposureImpactActionId.ACCOUNT_LOCKDOWN,
                    ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY,
                    ExposureImpactActionId.RECOVERY_PLAN,
                    ExposureImpactActionId.EVIDENCE_KIT
                )
            ),
            ExposureImpactId.FAMILY_IMPERSONATION_ANSWERED to ExpectedCardContract(
                category = ExposureImpactCategory.FAMILY_IMPERSONATION,
                labelTerms = setOf("가족", "딥페이크", "사칭"),
                requiredActionIds = setOf(
                    ExposureImpactActionId.FAMILY_CALLBACK_CARD,
                    ExposureImpactActionId.FAMILY_ALERT_MESSAGES,
                    ExposureImpactActionId.SAFE_CALL_SCRIPTS,
                    ExposureImpactActionId.TRUSTED_HELPER_HANDOFF
                )
            ),
            ExposureImpactId.UNCLEAR_OR_MULTIPLE to ExpectedCardContract(
                category = ExposureImpactCategory.UNCLEAR_COMPLEX,
                labelTerms = setOf("확인", "복합"),
                requiredActionIds = setOf(
                    ExposureImpactActionId.SAFETY_FLOW_ROUTER,
                    ExposureImpactActionId.RECOVERY_PLAN,
                    ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY,
                    ExposureImpactActionId.TRUSTED_HELPER_HANDOFF
                )
            )
        )
        assertEquals(ExposureImpactId.entries.toSet(), expectedContracts.keys)
        expectedContracts.forEach { (cardId, contract) ->
            val card = defaultTriage.cards.single { candidate -> candidate.id == cardId }
            assertEquals(contract.category, card.category)
            contract.labelTerms.forEach { term ->
                assertTrue("$cardId label omitted $term", card.label.contains(term))
            }
            val actionIds = (listOf(card.primaryAction) + card.secondaryActions)
                .map { action -> action.id }
                .toSet()
            assertTrue(
                "$cardId omitted required actions ${contract.requiredActionIds}",
                actionIds.containsAll(contract.requiredActionIds)
            )
        }
    }

    @Test
    fun matchedSignalsOnlySuggestPossibleExposureAndRequireDirectConfirmation() {
        ScamSignal.entries.forEach { signal ->
            val triage = ExposureImpactTriageBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )
            val recommendationCopy =
                "${triage.recommendationTitle} ${triage.recommendationExplanation}"

            assertTrue(
                "$signal recommendation did not describe a possibility",
                recommendationCopy.contains("가능성") ||
                    triage.recommendedCards.any { card -> card.whenThisMightApply.contains("해당될 수") }
            )
            assertTrue("$signal recommendation omitted direct check", recommendationCopy.contains("직접 확인"))
            listOf(
                "노출됐습니다",
                "노출되었습니다",
                "돈을 보냈습니다",
                "인증번호를 공유했습니다",
                "앱을 설치했습니다",
                "개인정보를 입력했습니다"
            ).forEach { claim ->
                assertFalse("$signal recommendation asserted $claim", recommendationCopy.contains(claim))
            }
        }
    }

    @Test
    fun contextualResponseAndEvidenceRoutesMatchTheExposureWithoutContradictingIt() {
        val triage = ExposureImpactTriageBuilder.build(
            analysis = analysisWith(
                ScamSignal.URGENT_TRANSFER,
                ScamSignal.CREDENTIALS,
                ScamSignal.APP_INSTALL,
                ScamSignal.LINK,
                riskLevel = RiskLevel.HIGH
            ),
            readiness = readyReadiness()
        )

        fun action(
            cardId: ExposureImpactId,
            actionId: ExposureImpactActionId
        ): ExposureImpactAction {
            val card = triage.cards.single { candidate -> candidate.id == cardId }
            return (listOf(card.primaryAction) + card.secondaryActions)
                .single { candidate -> candidate.id == actionId }
        }

        assertEquals(
            IncidentSituation.MONEY_SENT,
            action(
                ExposureImpactId.MONEY_OR_VALUE_SENT,
                ExposureImpactActionId.RESPONSE_CENTER
            ).responseSituation
        )
        assertEquals(
            IncidentSituation.MONEY_SENT,
            action(
                ExposureImpactId.MONEY_OR_VALUE_SENT,
                ExposureImpactActionId.EVIDENCE_KIT
            ).evidenceSituation
        )
        assertEquals(
            IncidentSituation.CREDENTIALS_SHARED,
            action(
                ExposureImpactId.CREDENTIAL_OR_PAYMENT_INFO_SHARED,
                ExposureImpactActionId.RESPONSE_CENTER
            ).responseSituation
        )
        assertEquals(
            IncidentSituation.CREDENTIALS_SHARED,
            action(
                ExposureImpactId.CREDENTIAL_OR_PAYMENT_INFO_SHARED,
                ExposureImpactActionId.EVIDENCE_KIT
            ).evidenceSituation
        )
        assertEquals(
            IncidentSituation.APP_INSTALLED,
            action(
                ExposureImpactId.APP_OR_REMOTE_ACCESS_GRANTED,
                ExposureImpactActionId.RESPONSE_CENTER
            ).responseSituation
        )
        assertEquals(
            IncidentSituation.APP_INSTALLED,
            action(
                ExposureImpactId.APP_OR_REMOTE_ACCESS_GRANTED,
                ExposureImpactActionId.EVIDENCE_KIT
            ).evidenceSituation
        )
        assertEquals(
            IncidentSituation.LINK_OPENED,
            action(
                ExposureImpactId.LINK_CLICK_ONLY,
                ExposureImpactActionId.EVIDENCE_KIT
            ).evidenceSituation
        )
        assertEquals(
            IncidentSituation.CREDENTIALS_SHARED,
            action(
                ExposureImpactId.LINK_INFO_OR_PAYMENT_ENTERED,
                ExposureImpactActionId.EVIDENCE_KIT
            ).evidenceSituation
        )
        assertFalse(
            action(
                ExposureImpactId.LINK_INFO_OR_PAYMENT_ENTERED,
                ExposureImpactActionId.EVIDENCE_KIT
            ).evidenceSituation == IncidentSituation.LINK_OPENED
        )
    }

    @Test
    fun shareUsesOnlyExposureCategoryActionLabelsAndGenericMovesWithoutSecrets() {
        val safeWord = "해질녘 파란 우산은 어디에?"
        val callbackScript = "푸른 메모장을 열고 첫 번째 저장 번호로 전화한다."
        val contactLabel = "보호자-은하수"
        val contactNumber = "010-2468-1357"
        val raw =
            "RAW-EXPOSURE-COMET 전화 010-9876-5432 계좌 123456789012 비밀번호 BluePassword77 " +
                "OTP A1B2C3 인증번호 654321 PIN 7788 https://evil.example/path 주민번호 900101-1234567 " +
                "안전 질문 $safeWord 콜백 $callbackScript 연락처 $contactLabel $contactNumber"
        val analysis = ScamAnalysis(
            input = raw,
            score = 100,
            riskLevel = RiskLevel.HIGH,
            matchedSignals = listOf(
                ScamSignal.URGENT_TRANSFER,
                ScamSignal.APP_INSTALL,
                ScamSignal.CREDENTIALS,
                ScamSignal.LINK,
                ScamSignal.PHONE_NUMBER_CHANGE,
                ScamSignal.SECRECY
            )
        )
        val triage = ExposureImpactTriageBuilder.build(
            analysis = analysis,
            readiness = FamilyReadinessBuilder.build(
                SafetyPlan(
                    safeWord = safeWord,
                    callbackScript = callbackScript,
                    contacts = listOf(TrustedContact(contactLabel, contactNumber))
                )
            )
        )
        val message = triage.familyShareSummary
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)

        triage.cards.forEach { card ->
            assertTrue("share omitted exposure: ${card.label}", message.contains(card.label))
            assertTrue("share omitted category: ${card.category.label}", message.contains(card.category.label))
            assertTrue("share omitted generic safe move: ${card.id}", message.contains(card.firstSafeMove))
            (listOf(card.primaryAction) + card.secondaryActions).forEach { action ->
                assertTrue("share omitted action: ${action.label}", message.contains(action.label))
            }
            assertFalse("share included applicability detail: ${card.id}", message.contains(card.whenThisMightApply))
            card.whatToAvoid.forEach { note ->
                assertFalse("share included avoid detail: $note", message.contains(note))
            }
            card.officialHelpPrepNotes.forEach { note ->
                assertFalse("share included official-help prep detail: $note", message.contains(note))
            }
        }
        triage.recommendedActions.forEach { action ->
            assertTrue("share omitted recommended action: ${action.label}", message.contains(action.label))
        }
        assertTrue(message.contains("일반 안전 행동"))
        assertTrue(message.contains("원문") && message.contains("포함하지 않았습니다"))
        assertFalse(message.contains(raw))
        assertFalse(message.contains(redactedClue))

        val secrets = listOf(
            "RAW-EXPOSURE-COMET",
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
            assertFalse("exposure share leaked secret: $secret", message.contains(secret))
        }
        assertFalse(phonePattern.containsMatchIn(message))
        assertFalse(longNumberPattern.containsMatchIn(message))
        assertFalse(groupedNumberPattern.containsMatchIn(message))
        assertFalse(urlDomainPattern.containsMatchIn(message))
        assertFalse(residentPattern.containsMatchIn(message))

        val completeCopy = allCopy(triage)
        assertFalse(completeCopy.contains(raw))
        assertFalse(completeCopy.contains(redactedClue))
        secrets.forEach { secret ->
            assertFalse("exposure output leaked secret: $secret", completeCopy.contains(secret))
        }

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
            nextAction = "계좌 123456789012 OTP 654321 PIN 7788"
        )
        val adversarialTriage = ExposureImpactTriageBuilder.build(analysis, adversarialReadiness)
        val adversarialCopy = allCopy(adversarialTriage)
        secrets.forEach { secret ->
            assertFalse(
                "triage used caller-supplied readiness secret: $secret",
                adversarialCopy.contains(secret)
            )
        }
        assertFalse(phonePattern.containsMatchIn(adversarialTriage.familyShareSummary))
        assertFalse(longNumberPattern.containsMatchIn(adversarialTriage.familyShareSummary))
        assertFalse(groupedNumberPattern.containsMatchIn(adversarialTriage.familyShareSummary))
        assertFalse(urlDomainPattern.containsMatchIn(adversarialTriage.familyShareSummary))
        assertFalse(residentPattern.containsMatchIn(adversarialTriage.familyShareSummary))
    }

    @Test
    fun outputIsDeterministicForTheSameInputs() {
        val analysis = analysisWith(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.APP_INSTALL,
            ScamSignal.CREDENTIALS,
            ScamSignal.LINK,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.SECRECY,
            riskLevel = RiskLevel.HIGH
        )
        val readiness = readyReadiness()

        val first = ExposureImpactTriageBuilder.build(analysis, readiness)
        val second = ExposureImpactTriageBuilder.build(analysis, readiness)

        assertEquals(first, second)
        assertEquals(first.cards, second.cards)
        assertEquals(first.recommendedExposureIds, second.recommendedExposureIds)
        assertEquals(first.recommendedCards, second.recommendedCards)
        assertEquals(first.recommendedActions, second.recommendedActions)
        assertEquals(first.recommendedActionIds, second.recommendedActionIds)
        assertEquals(first.familyShareSummary, second.familyShareSummary)
    }

    @Test
    fun copyStatesManualLocalNoStorageNoContactReadingNoAutoSendNoMonitoringNoNetworkAndNoMutation() {
        representativeTriages().forEach { triage ->
            val privacyCopy = triage.privacySafetyNotes.joinToString(" ")

            assertTrue(privacyCopy.contains("수동 안내"))
            assertTrue(privacyCopy.contains("기기 안"))
            assertTrue(privacyCopy.contains("로컬 규칙"))
            assertTrue(privacyCopy.contains("노출 분류 선택"))
            assertTrue(privacyCopy.contains("노출 이력"))
            assertTrue(privacyCopy.contains("공유 이력"))
            assertTrue(privacyCopy.contains("저장하지 않습니다"))
            assertTrue(privacyCopy.contains("연락처를 읽거나"))
            assertTrue(privacyCopy.contains("받는 사람을 고르지 않"))
            assertTrue(privacyCopy.contains("자동으로 보내지 않습니다"))
            assertTrue(privacyCopy.contains("읽거나 감시하지 않"))
            assertTrue(privacyCopy.contains("자동 모니터링하지 않습니다"))
            assertTrue(privacyCopy.contains("네트워크"))
            assertTrue(privacyCopy.contains("사건 기록을 추가·수정·삭제하지 않습니다"))
            assertTrue(privacyCopy.contains("새 Android 권한을 요청하지 않"))
            assertTrue(privacyCopy.contains("전화") && privacyCopy.contains("자동 실행"))
            assertTrue(triage.intro.contains("단정") || triage.intro.contains("확정"))
            assertTrue(triage.intro.contains("직접 확인"))
        }
    }

    @Test
    fun copyDoesNotImplyProofMonitoringBlockingRemovalFreezeRecoveryLegalGuaranteeDeletionOrSecretAccess() {
        val forbiddenAffirmativeClaims = listOf(
            "모든 사기를 찾아냅니다",
            "모든 사기를 막습니다",
            "모든 사기를 예방합니다",
            "사기를 확정합니다",
            "노출을 확정합니다",
            "노출 여부를 증명합니다",
            "공식 판정입니다",
            "안전합니다",
            "위험 없음",
            "사기 아님",
            "문제없습니다",
            "아무것도 노출되지 않았습니다",
            "노출되지 않았습니다",
            "노출됐습니다",
            "노출되었습니다",
            "돈을 보냈습니다",
            "인증번호를 공유했습니다",
            "앱을 설치했습니다",
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
            "메시지를 자동으로 보냅니다",
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
            "안전을 보장합니다",
            "자동으로 삭제합니다",
            "자동 삭제합니다",
            "비밀값에 접근합니다",
            "비밀정보에 접근합니다",
            "100%",
            "automatic monitoring",
            "conclusive detection",
            "proof of exposure",
            "blocks calls automatically",
            "blocks messages automatically",
            "malware removal guarantee",
            "account freeze guarantee",
            "guaranteed recovery",
            "legal guarantee"
        )

        representativeTriages().forEach { triage ->
            val copy = allCopy(triage)
            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "exposure impact triage copy contained unsafe claim: $claim",
                    copy.lowercase().contains(claim.lowercase())
                )
            }
        }
    }

    private fun representativeTriages(): List<ExposureImpactTriage> = listOf(
        ExposureImpactTriageBuilder.build(null, lowReadiness()),
        ExposureImpactTriageBuilder.build(analysisWith(ScamSignal.APP_INSTALL), inProgressReadiness()),
        ExposureImpactTriageBuilder.build(
            analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER),
            readyReadiness()
        ),
        ExposureImpactTriageBuilder.build(analysisWith(ScamSignal.LINK), readyReadiness()),
        ExposureImpactTriageBuilder.build(analysisWith(ScamSignal.DEEPFAKE_VOICE), readyReadiness()),
        ExposureImpactTriageBuilder.build(analysisWith(ScamSignal.SECRECY), readyReadiness())
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

    private fun cardCopy(card: ExposureImpactCard): String = buildString {
        append(card.category.label)
        append(card.label)
        append(card.whenThisMightApply)
        append(card.firstSafeMove)
        card.whatToAvoid.forEach(::append)
        card.officialHelpPrepNotes.forEach(::append)
        append(card.priority.label)
        append(card.primaryAction.label)
        card.secondaryActions.forEach { action -> append(action.label) }
    }

    private fun allCopy(triage: ExposureImpactTriage): String = buildString {
        append(triage.title)
        append(triage.intro)
        append(triage.recommendationTitle)
        append(triage.recommendationExplanation)
        triage.recommendedActions.forEach { action -> append(action.label) }
        triage.cards.forEach { card -> append(cardCopy(card)) }
        triage.privacySafetyNotes.forEach(::append)
        append(triage.familyShareSummary)
    }

    private data class ExpectedCardContract(
        val category: ExposureImpactCategory,
        val labelTerms: Set<String>,
        val requiredActionIds: Set<ExposureImpactActionId>
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
