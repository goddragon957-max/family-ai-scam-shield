package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrustedHelperReviewModeBuilderTest {
    @Test
    fun noAnalysisAndNoSignalsRecommendPauseSecretClassificationAndCallbackWithoutSafeVerdict() {
        val noAnalysis = TrustedHelperReviewModeBuilder.build(
            analysis = null,
            readiness = readyReadiness()
        )
        val noSignals = TrustedHelperReviewModeBuilder.build(
            analysis = ScamAnalysis(
                input = "일반 대화",
                score = 0,
                riskLevel = RiskLevel.SAFE,
                matchedSignals = emptyList()
            ),
            readiness = readyReadiness()
        )
        val expectedCardIds = listOf(
            TrustedHelperReviewCardId.PAUSE_AND_REASSURE,
            TrustedHelperReviewCardId.SECRET_VALUE_BOUNDARY,
            TrustedHelperReviewCardId.CLASSIFY_ACTIONS_ONLY,
            TrustedHelperReviewCardId.FAMILY_IDENTITY_VERIFICATION
        )

        listOf(noAnalysis, noSignals).forEach { review ->
            assertEquals(expectedCardIds, review.recommendedCardIds)
            assertEquals(expectedCardIds, review.recommendedCards.map { card -> card.id })
            listOf(
                TrustedHelperReviewActionId.QUICK_STOP,
                TrustedHelperReviewActionId.EXPOSURE_IMPACT_TRIAGE,
                TrustedHelperReviewActionId.CHECKER,
                TrustedHelperReviewActionId.FAMILY_CALLBACK_CARD
            ).forEach { actionId ->
                assertTrue("default review omitted $actionId", actionId in review.recommendedActionIds)
            }
            assertTrue(review.recommendationExplanation.contains("단정하지 않") ||
                review.recommendationExplanation.contains("증명하지 않"))
            listOf(
                "안전합니다",
                "위험 없음",
                "사기 아님",
                "문제없습니다",
                "검증 완료"
            ).forEach { verdict ->
                assertFalse("default review asserted $verdict", allCopy(review).contains(verdict))
            }
        }
        assertFalse(TrustedHelperReviewActionId.ANALYSIS_RESULT in noAnalysis.recommendedActionIds)
        assertTrue(TrustedHelperReviewActionId.ANALYSIS_RESULT in noSignals.recommendedActionIds)
        assertTrue(noAnalysis.recommendationExplanation.contains("현재 분석 결과"))
    }

    @Test
    fun highRiskFinancialCredentialSafeAccountGiftCryptoAndPaymentRecommendProtectionHelpAndEvidence() {
        val reviews = listOf(
            TrustedHelperReviewModeBuilder.build(
                analysis = ScamAnalysis("테스트", 80, RiskLevel.HIGH, emptyList()),
                readiness = readyReadiness()
            )
        ) + listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.BAIT,
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).map { signal ->
            TrustedHelperReviewModeBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )
        }

        reviews.forEach { review ->
            assertTrue(
                review.recommendedCardIds.containsAll(
                    listOf(
                        TrustedHelperReviewCardId.PROTECT_ACTIONS_ALREADY_TAKEN,
                        TrustedHelperReviewCardId.PREPARE_OFFICIAL_HELP
                    )
                )
            )
            listOf(
                TrustedHelperReviewActionId.RECOVERY_PLAN,
                TrustedHelperReviewActionId.ACCOUNT_LOCKDOWN,
                TrustedHelperReviewActionId.EVIDENCE_KIT,
                TrustedHelperReviewActionId.OFFICIAL_HELP_DIRECTORY,
                TrustedHelperReviewActionId.REPORT_PREP
            ).forEach { actionId ->
                assertTrue("financial review omitted $actionId", actionId in review.recommendedActionIds)
            }
            assertTrue(review.recommendationExplanation.contains("실제 행동"))
            assertTrue(review.recommendationExplanation.contains("종류"))
        }
    }

    @Test
    fun remoteControlAppAndScreenShareRecommendProtectionEvidenceAndHelperWithoutRemovalGuarantee() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val review = TrustedHelperReviewModeBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertTrue(
                TrustedHelperReviewCardId.PROTECT_ACTIONS_ALREADY_TAKEN in
                    review.recommendedCardIds
            )
            listOf(
                TrustedHelperReviewActionId.RECOVERY_PLAN,
                TrustedHelperReviewActionId.ACCOUNT_LOCKDOWN,
                TrustedHelperReviewActionId.EVIDENCE_KIT,
                TrustedHelperReviewActionId.TRUSTED_HELPER_HANDOFF
            ).forEach { actionId ->
                assertTrue("$signal review omitted $actionId", actionId in review.recommendedActionIds)
            }
            val copy = allCopy(review)
            assertFalse(copy.contains("악성 앱을 자동으로 제거합니다"))
            assertFalse(copy.contains("악성코드 제거를 보장합니다"))
            assertFalse(copy.contains("기기를 안전하게 만들었습니다"))
        }
    }

    @Test
    fun linkAndSmishingRecommendClassificationEvidenceAndFamilyAlertWithoutAutoScanClaim() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val review = TrustedHelperReviewModeBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertEquals(
                listOf(
                    TrustedHelperReviewCardId.CLASSIFY_ACTIONS_ONLY,
                    TrustedHelperReviewCardId.SHARE_GENERAL_FAMILY_ALERT
                ),
                review.recommendedCardIds
            )
            listOf(
                TrustedHelperReviewActionId.EXPOSURE_IMPACT_TRIAGE,
                TrustedHelperReviewActionId.EVIDENCE_KIT,
                TrustedHelperReviewActionId.FAMILY_ALERT_MESSAGES
            ).forEach { actionId ->
                assertTrue("$signal review omitted $actionId", actionId in review.recommendedActionIds)
            }
            assertFalse(allCopy(review).contains("자동으로 검사합니다"))
            assertFalse(allCopy(review).contains("자동 스캔합니다"))
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendVerificationAlertSafeCallsAndHelper() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val review = TrustedHelperReviewModeBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertEquals(
                listOf(
                    TrustedHelperReviewCardId.FAMILY_IDENTITY_VERIFICATION,
                    TrustedHelperReviewCardId.SHARE_GENERAL_FAMILY_ALERT
                ),
                review.recommendedCardIds
            )
            listOf(
                TrustedHelperReviewActionId.FAMILY_CALLBACK_CARD,
                TrustedHelperReviewActionId.SAFE_CALL_SCRIPTS,
                TrustedHelperReviewActionId.FAMILY_ALERT_MESSAGES,
                TrustedHelperReviewActionId.TRUSTED_HELPER_HANDOFF
            ).forEach { actionId ->
                assertTrue("$signal review omitted $actionId", actionId in review.recommendedActionIds)
            }
            assertTrue(review.recommendationExplanation.contains("평소 별도 경로"))
        }
    }

    @Test
    fun secrecyAndImpersonationRecommendPauseSecretClassificationAndOfficialHelp() {
        listOf(ScamSignal.SECRECY, ScamSignal.IMPERSONATION).forEach { signal ->
            val review = TrustedHelperReviewModeBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertEquals(
                listOf(
                    TrustedHelperReviewCardId.PAUSE_AND_REASSURE,
                    TrustedHelperReviewCardId.SECRET_VALUE_BOUNDARY,
                    TrustedHelperReviewCardId.CLASSIFY_ACTIONS_ONLY,
                    TrustedHelperReviewCardId.PREPARE_OFFICIAL_HELP
                ),
                review.recommendedCardIds
            )
            listOf(
                TrustedHelperReviewActionId.QUICK_STOP,
                TrustedHelperReviewActionId.EXPOSURE_IMPACT_TRIAGE,
                TrustedHelperReviewActionId.OFFICIAL_HELP_DIRECTORY
            ).forEach { actionId ->
                assertTrue("$signal review omitted $actionId", actionId in review.recommendedActionIds)
            }
            assertTrue(review.recommendationExplanation.contains("비밀값"))
        }
    }

    @Test
    fun lowReadinessAddsRoadmapAndDrillWhileReadyCopyDiffersCalmly() {
        val analysis = analysisWith(ScamSignal.LINK)
        val low = TrustedHelperReviewModeBuilder.build(analysis, lowReadiness())
        val inProgress = TrustedHelperReviewModeBuilder.build(analysis, inProgressReadiness())
        val ready = TrustedHelperReviewModeBuilder.build(analysis, readyReadiness())

        assertTrue(
            TrustedHelperReviewCardId.CONNECT_NEXT_FAMILY_PREPARATION in
                low.recommendedCardIds
        )
        assertTrue(
            TrustedHelperReviewActionId.FAMILY_READINESS_ROADMAP in low.recommendedActionIds
        )
        assertTrue(
            TrustedHelperReviewActionId.FAMILY_SAFETY_DRILL_COACH in low.recommendedActionIds
        )
        assertTrue(low.recommendationExplanation.contains("부족해도 괜찮"))
        assertTrue(low.recommendationExplanation.contains("차분히 시작"))
        assertTrue(inProgress.recommendationExplanation.contains("진행 중"))
        assertTrue(ready.recommendationExplanation.contains("되어 있어도"))
        assertTrue(ready.recommendationExplanation.contains("차분히"))
        assertNotEquals(low.recommendationExplanation, ready.recommendationExplanation)
    }

    @Test
    fun everyHelperReviewCardHasCompleteFieldsRoutesPriorityAndStableContract() {
        val defaultReview = TrustedHelperReviewModeBuilder.build(null, lowReadiness())
        val signaledReview = TrustedHelperReviewModeBuilder.build(
            analysis = analysisWith(
                *ScamSignal.entries.toTypedArray(),
                riskLevel = RiskLevel.HIGH
            ),
            readiness = readyReadiness()
        )
        val expectedStableCardOrder = listOf(
            TrustedHelperReviewCardId.PAUSE_AND_REASSURE,
            TrustedHelperReviewCardId.SECRET_VALUE_BOUNDARY,
            TrustedHelperReviewCardId.CLASSIFY_ACTIONS_ONLY,
            TrustedHelperReviewCardId.FAMILY_IDENTITY_VERIFICATION,
            TrustedHelperReviewCardId.PROTECT_ACTIONS_ALREADY_TAKEN,
            TrustedHelperReviewCardId.PREPARE_OFFICIAL_HELP,
            TrustedHelperReviewCardId.SHARE_GENERAL_FAMILY_ALERT,
            TrustedHelperReviewCardId.CONNECT_NEXT_FAMILY_PREPARATION
        )
        assertEquals(expectedStableCardOrder, TrustedHelperReviewCardId.entries)

        listOf(defaultReview, signaledReview).forEach { review ->
            assertTrue(review.title.isNotBlank())
            assertTrue(review.intro.isNotBlank())
            assertTrue(review.recommendationTitle.isNotBlank())
            assertTrue(review.recommendationExplanation.isNotBlank())
            assertTrue(review.privacySafetyNotes.isNotEmpty())
            assertEquals(expectedStableCardOrder, review.cards.map { card -> card.id })
            assertEquals(
                (1..TrustedHelperReviewCardId.entries.size).toList(),
                review.cards.map { card -> card.displayOrder }
            )
            assertEquals(review.cards.size, review.cards.map { card -> card.id }.distinct().size)
            assertEquals(review.recommendedCardIds, review.recommendedCardIds.distinct())
            assertEquals(review.recommendedCardIds, review.recommendedCards.map { card -> card.id })
            assertEquals(review.recommendedActionIds, review.recommendedActionIds.distinct())
            assertEquals(
                review.recommendedActionIds,
                review.recommendedActions.map { action -> action.id }
            )

            review.cards.forEach { card ->
                assertEquals(card.id.displayOrder, card.displayOrder)
                assertTrue(card.stage.label.isNotBlank())
                assertTrue(card.category.label.isNotBlank())
                assertTrue(card.title.isNotBlank())
                assertTrue(card.helperRole.isNotBlank())
                assertTrue(card.safeQuestion.isNotBlank())
                assertTrue(card.safeQuestion.endsWith("?"))
                assertTrue(card.whatNotToAsk.isNotEmpty())
                assertTrue(card.whatNotToAsk.all { note -> note.isNotBlank() })
                assertTrue(card.firstSafeMove.isNotBlank())
                assertTrue(card.priority.rank >= 0)
                assertTrue(card.priority.label.isNotBlank())
                assertTrue(card.primaryAction.label.isNotBlank())
                assertTrue(card.secondaryActions.isNotEmpty())
                assertTrue(card.secondaryActions.all { action -> action.label.isNotBlank() })
                assertEquals(
                    listOf(card.primaryAction) + card.secondaryActions,
                    card.routeActions
                )
            }
        }

        val routedActionIds = (defaultReview.cards + signaledReview.cards)
            .flatMap { card -> card.routeActions }
            .map { action -> action.id }
            .toSet()
        assertEquals(TrustedHelperReviewActionId.entries.toSet(), routedActionIds)

        val expectedContracts = mapOf(
            TrustedHelperReviewCardId.PAUSE_AND_REASSURE to setOf(
                TrustedHelperReviewActionId.QUICK_STOP,
                TrustedHelperReviewActionId.CHECKER,
                TrustedHelperReviewActionId.RISK_SIGNAL_EXPLAINERS
            ),
            TrustedHelperReviewCardId.SECRET_VALUE_BOUNDARY to setOf(
                TrustedHelperReviewActionId.PRIVACY_CENTER,
                TrustedHelperReviewActionId.EXPOSURE_IMPACT_TRIAGE,
                TrustedHelperReviewActionId.SAFETY_FLOW_ROUTER
            ),
            TrustedHelperReviewCardId.CLASSIFY_ACTIONS_ONLY to setOf(
                TrustedHelperReviewActionId.EXPOSURE_IMPACT_TRIAGE,
                TrustedHelperReviewActionId.CHECKER,
                TrustedHelperReviewActionId.RISK_SIGNAL_EXPLAINERS
            ),
            TrustedHelperReviewCardId.FAMILY_IDENTITY_VERIFICATION to setOf(
                TrustedHelperReviewActionId.FAMILY_CALLBACK_CARD,
                TrustedHelperReviewActionId.SAFE_CALL_SCRIPTS,
                TrustedHelperReviewActionId.TRUSTED_HELPER_HANDOFF
            ),
            TrustedHelperReviewCardId.PROTECT_ACTIONS_ALREADY_TAKEN to setOf(
                TrustedHelperReviewActionId.RECOVERY_PLAN,
                TrustedHelperReviewActionId.ACCOUNT_LOCKDOWN,
                TrustedHelperReviewActionId.EVIDENCE_KIT
            ),
            TrustedHelperReviewCardId.PREPARE_OFFICIAL_HELP to setOf(
                TrustedHelperReviewActionId.OFFICIAL_HELP_DIRECTORY,
                TrustedHelperReviewActionId.CHECKER,
                TrustedHelperReviewActionId.EVIDENCE_KIT
            ),
            TrustedHelperReviewCardId.SHARE_GENERAL_FAMILY_ALERT to setOf(
                TrustedHelperReviewActionId.FAMILY_ALERT_MESSAGES,
                TrustedHelperReviewActionId.TRUSTED_HELPER_HANDOFF,
                TrustedHelperReviewActionId.SAFE_CALL_SCRIPTS
            ),
            TrustedHelperReviewCardId.CONNECT_NEXT_FAMILY_PREPARATION to setOf(
                TrustedHelperReviewActionId.FAMILY_READINESS_ROADMAP,
                TrustedHelperReviewActionId.FAMILY_SAFETY_DRILL_COACH,
                TrustedHelperReviewActionId.PRIVACY_CENTER
            )
        )
        assertEquals(TrustedHelperReviewCardId.entries.toSet(), expectedContracts.keys)
        expectedContracts.forEach { (cardId, requiredActionIds) ->
            val card = defaultReview.cards.single { candidate -> candidate.id == cardId }
            assertTrue(
                "$cardId omitted $requiredActionIds",
                card.routeActions.map { action -> action.id }.toSet().containsAll(requiredActionIds)
            )
        }
        val analyzedOfficialHelpCard = signaledReview.cards.single { card ->
            card.id == TrustedHelperReviewCardId.PREPARE_OFFICIAL_HELP
        }
        assertTrue(
            analyzedOfficialHelpCard.routeActions.any { action ->
                action.id == TrustedHelperReviewActionId.REPORT_PREP
            }
        )
    }

    @Test
    fun safeQuestionsAskOnlyCategoriesAndNeverRequestSecretValues() {
        val review = TrustedHelperReviewModeBuilder.build(null, lowReadiness())
        val requestPatterns = listOf(
            Regex("값(?:이|은|을)?\\s*(?:뭐|무엇|몇|알려|말해|적어|써|보여|보내)"),
            Regex(
                "(?:비밀번호|OTP|인증번호|PIN|보안카드|카드 비밀값|안전(?:단어|질문) 답|" +
                    "주민(?:등록)?번호|연락처|전화번호|계좌번호).{0,16}" +
                    "(?:알려줘|말해줘|적어줘|써줘|보여줘|보내줘|공유해|입력해|뭐야|무엇인가요)"
            ),
            Regex("(?:정확한|전체)\\s*(?:연락처|전화|계좌|주민등록)?번호.{0,16}(?:알려|말해|적어|보여|보내|뭐|무엇)"),
            Regex("(?:사기 )?원문|링크 원문").let { rawTerm ->
                Regex("(?:${rawTerm.pattern}).{0,16}(?:보여줘|보내줘|공유해|말해줘|적어줘)")
            }
        )

        review.cards.forEach { card ->
            requestPatterns.forEach { pattern ->
                assertFalse(
                    "${card.id} safe question requested a secret: ${pattern.pattern}",
                    pattern.containsMatchIn(card.safeQuestion)
                )
            }
            assertFalse(card.safeQuestion.contains("값이 뭐"))
            assertFalse(card.safeQuestion.contains("원문을 보여"))
            assertFalse(card.safeQuestion.contains("정확한 번호"))
        }
        val helperFacingCopy = buildString {
            append(review.title)
            append(review.intro)
            append(review.recommendationTitle)
            append(review.recommendationExplanation)
            review.recommendedActions.forEach { action -> append(action.label) }
            review.cards.forEach { card ->
                append(card.stage.label)
                append(card.category.label)
                append(card.title)
                append(card.helperRole)
                append(card.safeQuestion)
                append(card.firstSafeMove)
                append(card.priority.label)
                card.routeActions.forEach { action -> append(action.label) }
            }
        }
        requestPatterns.forEach { pattern ->
            assertFalse(
                "positive helper-facing copy requested a secret: ${pattern.pattern}",
                pattern.containsMatchIn(helperFacingCopy)
            )
        }

        val boundaryCopy = review.cards
            .single { card -> card.id == TrustedHelperReviewCardId.SECRET_VALUE_BOUNDARY }
            .whatNotToAsk
            .joinToString(" ")
        listOf(
            "비밀번호",
            "인증번호",
            "OTP",
            "PIN",
            "카드 비밀값",
            "안전 질문의 답",
            "연락처",
            "주민등록번호",
            "링크 원문"
        ).forEach { term ->
            assertTrue("secret boundary omitted $term", boundaryCopy.contains(term))
        }
        assertTrue(boundaryCopy.contains("묻거나 적지 마세요"))
    }

    @Test
    fun shareUsesOnlyStageCategoryActionLabelsAndGenericMovesWithoutSecrets() {
        val safeWord = "해질녘 파란 우산은 어디에?"
        val callbackScript = "푸른 메모장을 열고 첫 번째 저장 번호로 전화한다."
        val contactLabel = "보호자-은하수"
        val contactNumber = "010-2468-1357"
        val raw =
            "RAW-HELPER-REVIEW-COMET 전화 010-9876-5432 계좌 123456789012 " +
                "비밀번호 BluePassword77 OTP A1B2C3 인증번호 654321 PIN 7788 " +
                "https://evil.example/path 주민번호 900101-1234567 안전 질문 $safeWord " +
                "콜백 $callbackScript 연락처 $contactLabel $contactNumber"
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
        val review = TrustedHelperReviewModeBuilder.build(
            analysis = analysis,
            readiness = FamilyReadinessBuilder.build(
                SafetyPlan(
                    safeWord = safeWord,
                    callbackScript = callbackScript,
                    contacts = listOf(TrustedContact(contactLabel, contactNumber))
                )
            )
        )
        val message = review.familyShareSummary
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)

        review.cards.forEach { card ->
            assertTrue("share omitted stage: ${card.stage}", message.contains(card.stage.label))
            assertTrue("share omitted category: ${card.category}", message.contains(card.category.label))
            assertTrue("share omitted card: ${card.title}", message.contains(card.title))
            assertTrue("share omitted generic move: ${card.id}", message.contains(card.firstSafeMove))
            card.routeActions.forEach { action ->
                assertTrue("share omitted action: ${action.label}", message.contains(action.label))
            }
            assertFalse("share included helper role: ${card.id}", message.contains(card.helperRole))
            assertFalse("share included safe question: ${card.id}", message.contains(card.safeQuestion))
            card.whatNotToAsk.forEach { note ->
                assertFalse("share included do-not-ask detail: $note", message.contains(note))
            }
        }
        review.recommendedActions.forEach { action ->
            assertTrue("share omitted recommended action: ${action.label}", message.contains(action.label))
        }
        assertTrue(message.contains("일반 안전 행동"))
        assertTrue(message.contains("원문") && message.contains("포함하지 않았습니다"))
        assertFalse(message.contains(raw))
        assertFalse(message.contains(redactedClue))

        val secrets = listOf(
            "RAW-HELPER-REVIEW-COMET",
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
            assertFalse("helper review share leaked secret: $secret", message.contains(secret))
        }
        assertFalse(phonePattern.containsMatchIn(message))
        assertFalse(longNumberPattern.containsMatchIn(message))
        assertFalse(groupedNumberPattern.containsMatchIn(message))
        assertFalse(urlDomainPattern.containsMatchIn(message))
        assertFalse(residentPattern.containsMatchIn(message))

        val completeCopy = allCopy(review)
        assertFalse(completeCopy.contains(raw))
        assertFalse(completeCopy.contains(redactedClue))
        secrets.forEach { secret ->
            assertFalse("helper review output leaked secret: $secret", completeCopy.contains(secret))
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
        val adversarialReview = TrustedHelperReviewModeBuilder.build(analysis, adversarialReadiness)
        val adversarialCopy = allCopy(adversarialReview)
        secrets.forEach { secret ->
            assertFalse(
                "helper review used caller-supplied readiness secret: $secret",
                adversarialCopy.contains(secret)
            )
        }
        assertFalse(phonePattern.containsMatchIn(adversarialReview.familyShareSummary))
        assertFalse(longNumberPattern.containsMatchIn(adversarialReview.familyShareSummary))
        assertFalse(groupedNumberPattern.containsMatchIn(adversarialReview.familyShareSummary))
        assertFalse(urlDomainPattern.containsMatchIn(adversarialReview.familyShareSummary))
        assertFalse(residentPattern.containsMatchIn(adversarialReview.familyShareSummary))
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

        val first = TrustedHelperReviewModeBuilder.build(analysis, readiness)
        val second = TrustedHelperReviewModeBuilder.build(analysis, readiness)

        assertEquals(first, second)
        assertEquals(first.cards, second.cards)
        assertEquals(first.recommendedCardIds, second.recommendedCardIds)
        assertEquals(first.recommendedCards, second.recommendedCards)
        assertEquals(first.recommendedActions, second.recommendedActions)
        assertEquals(first.recommendedActionIds, second.recommendedActionIds)
        assertEquals(first.familyShareSummary, second.familyShareSummary)
    }

    @Test
    fun copyStatesManualLocalNoStorageNoContactReadingNoAutoSendNoMonitoringNoNetworkAndNoMutation() {
        representativeReviews().forEach { review ->
            val privacyCopy = review.privacySafetyNotes.joinToString(" ")

            assertTrue(privacyCopy.contains("수동 안내"))
            assertTrue(privacyCopy.contains("기기 안"))
            assertTrue(privacyCopy.contains("로컬 규칙"))
            assertTrue(privacyCopy.contains("선택한 카드"))
            assertTrue(privacyCopy.contains("보호자 질문·답변·메모"))
            assertTrue(privacyCopy.contains("완료·체크 상태"))
            assertTrue(privacyCopy.contains("점수"))
            assertTrue(privacyCopy.contains("읽음·진행 상태"))
            assertTrue(privacyCopy.contains("공유 이력"))
            assertTrue(privacyCopy.contains("보호자 검토 이력"))
            assertTrue(privacyCopy.contains("저장하지 않습니다"))
            assertTrue(privacyCopy.contains("연락처를 읽거나"))
            assertTrue(privacyCopy.contains("받는 사람을 고르지 않"))
            assertTrue(privacyCopy.contains("자동으로 보내지 않습니다"))
            assertTrue(privacyCopy.contains("읽거나 감시하지 않"))
            assertTrue(privacyCopy.contains("자동 모니터링하지 않습니다"))
            assertTrue(privacyCopy.contains("네트워크"))
            assertTrue(privacyCopy.contains("사건 기록을 추가·수정·삭제하지 않습니다"))
            assertTrue(privacyCopy.contains("새 Android 권한을 요청하지 않"))
            assertTrue(privacyCopy.contains("전화·문자") && privacyCopy.contains("자동 실행"))
            assertTrue(privacyCopy.contains("비밀값에 접근하지 않"))
        }
    }

    @Test
    fun copyDoesNotImplyProofMonitoringBlockingRemovalFreezeRecoveryLegalGuaranteeDeletionOrSecretAccess() {
        val forbiddenAffirmativeClaims = listOf(
            "모든 사기를 찾아냅니다",
            "모든 사기를 막습니다",
            "사기를 확정합니다",
            "노출을 확정합니다",
            "노출 여부를 증명합니다",
            "공식 판정입니다",
            "안전합니다",
            "위험 없음",
            "사기 아님",
            "문제없습니다",
            "검증을 완료했습니다",
            "자동으로 감시합니다",
            "자동 모니터링합니다",
            "몰래 확인합니다",
            "자동으로 검사합니다",
            "자동 스캔합니다",
            "연락처를 자동으로 읽습니다",
            "서버로 업로드합니다",
            "네트워크로 업로드합니다",
            "자동으로 전송합니다",
            "전화를 자동으로 겁니다",
            "메시지를 자동으로 보냅니다",
            "문자를 자동으로 차단합니다",
            "전화를 자동으로 차단합니다",
            "악성 앱을 자동으로 제거합니다",
            "악성 앱을 제거합니다",
            "악성코드를 제거합니다",
            "악성코드 제거를 보장합니다",
            "계좌를 자동으로 지급정지합니다",
            "카드를 자동으로 정지합니다",
            "계좌·카드를 정지합니다",
            "계정을 자동으로 잠급니다",
            "피해금 회수를 보장합니다",
            "피해금을 회수합니다",
            "피해 복구를 보장합니다",
            "법률 자문을 제공합니다",
            "법률 조언을 제공합니다",
            "법적 결과를 보장합니다",
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

        representativeReviews().forEach { review ->
            val copy = allCopy(review)
            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "trusted helper review copy contained unsafe claim: $claim",
                    copy.lowercase().contains(claim.lowercase())
                )
            }
        }
    }

    private fun representativeReviews(): List<TrustedHelperReviewMode> = listOf(
        TrustedHelperReviewModeBuilder.build(null, lowReadiness()),
        TrustedHelperReviewModeBuilder.build(
            analysisWith(ScamSignal.APP_INSTALL),
            inProgressReadiness()
        ),
        TrustedHelperReviewModeBuilder.build(
            analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER),
            readyReadiness()
        ),
        TrustedHelperReviewModeBuilder.build(analysisWith(ScamSignal.LINK), readyReadiness()),
        TrustedHelperReviewModeBuilder.build(
            analysisWith(ScamSignal.DEEPFAKE_VOICE),
            readyReadiness()
        ),
        TrustedHelperReviewModeBuilder.build(
            analysisWith(ScamSignal.SECRECY),
            readyReadiness()
        )
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

    private fun cardCopy(card: TrustedHelperReviewCard): String = buildString {
        append(card.stage.label)
        append(card.category.label)
        append(card.title)
        append(card.helperRole)
        append(card.safeQuestion)
        card.whatNotToAsk.forEach(::append)
        append(card.firstSafeMove)
        append(card.priority.label)
        card.routeActions.forEach { action -> append(action.label) }
    }

    private fun allCopy(review: TrustedHelperReviewMode): String = buildString {
        append(review.title)
        append(review.intro)
        append(review.recommendationTitle)
        append(review.recommendationExplanation)
        review.recommendedActions.forEach { action -> append(action.label) }
        review.cards.forEach { card -> append(cardCopy(card)) }
        review.privacySafetyNotes.forEach(::append)
        append(review.familyShareSummary)
    }

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
