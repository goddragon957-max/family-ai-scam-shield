package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrustedHelperHandoffCardBuilderTest {
    @Test
    fun noAnalysisHasStableCardOrderAndConservativeDefaultRecommendations() {
        val card = TrustedHelperHandoffBuilder.build(analysis = null, readiness = readiness())

        assertEquals("분석 전 기본 추천", card.recommendationTitle)
        assertEquals(
            listOf(
                TrustedHelperCardId.CALM_COMPANION,
                TrustedHelperCardId.FAMILY_CONFIRMATION_HELPER,
                TrustedHelperCardId.FINANCE_ACCOUNT_HELPER,
                TrustedHelperCardId.DEVICE_APP_HELPER,
                TrustedHelperCardId.EVIDENCE_REPORT_HELPER,
                TrustedHelperCardId.FAMILY_WARNING_HELPER,
                TrustedHelperCardId.WEEKLY_REVIEW_HELPER
            ),
            card.cards.map { helper -> helper.id }
        )
        assertEquals((1..7).toList(), card.cards.map { helper -> helper.displayOrder })
        assertEquals(
            listOf(
                TrustedHelperCardId.CALM_COMPANION,
                TrustedHelperCardId.FAMILY_CONFIRMATION_HELPER,
                TrustedHelperCardId.WEEKLY_REVIEW_HELPER
            ),
            card.recommendedCardIds
        )
        assertEquals(card.recommendedCardIds, card.recommendedCards.map { helper -> helper.id })
        listOf(
            TrustedHelperActionId.QUICK_STOP,
            TrustedHelperActionId.CHECKER,
            TrustedHelperActionId.FAMILY_CALLBACK_CARD,
            TrustedHelperActionId.SAFETY_PLAN,
            TrustedHelperActionId.RECOVERY_PLAN
        ).forEach { actionId ->
            assertTrue("default guidance omitted $actionId", actionId in card.recommendedActionIds)
        }
        listOf(
            TrustedHelperActionId.RESPONSE_CENTER,
            TrustedHelperActionId.ACCOUNT_LOCKDOWN,
            TrustedHelperActionId.EVIDENCE_KIT,
            TrustedHelperActionId.ANALYSIS_RESULT
        ).forEach { actionId ->
            assertFalse(
                "default guidance should not include not-yet-relevant $actionId",
                actionId in card.recommendedActionIds
            )
        }
        assertEquals(TrustedHelperCardId.entries.size, card.cards.size)
        assertEquals(card.cards.size, card.cards.map { helper -> helper.id }.distinct().size)
    }

    @Test
    fun familyDeepfakeAndNumberChangeSignalsRecommendFamilyConfirmationAndFamilyWarningEarly() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val card = TrustedHelperHandoffBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                "$signal recommended order",
                listOf(
                    TrustedHelperCardId.FAMILY_CONFIRMATION_HELPER,
                    TrustedHelperCardId.FAMILY_WARNING_HELPER
                ),
                card.recommendedCardIds
            )
            listOf(
                TrustedHelperActionId.FAMILY_CALLBACK_CARD,
                TrustedHelperActionId.SAFETY_PLAN,
                TrustedHelperActionId.FAMILY_ALERT_MESSAGES
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in card.recommendedActionIds)
            }
        }
    }

    @Test
    fun moneyCredentialSafeAccountGiftAndCryptoSignalsRecommendFinanceAccountAndEvidenceReportHelp() {
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
            val card = TrustedHelperHandoffBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                "$signal recommended cards",
                listOf(TrustedHelperCardId.FINANCE_ACCOUNT_HELPER, TrustedHelperCardId.EVIDENCE_REPORT_HELPER),
                card.recommendedCardIds
            )
            listOf(
                TrustedHelperActionId.RESPONSE_CENTER,
                TrustedHelperActionId.ACCOUNT_LOCKDOWN,
                TrustedHelperActionId.OFFICIAL_HELP_DIRECTORY,
                TrustedHelperActionId.EVIDENCE_KIT
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in card.recommendedActionIds)
            }
        }
    }

    @Test
    fun remoteControlAppInstallAndScreenShareRecommendDeviceAppEvidenceAndAccountWithoutMalwareRemovalGuarantee() {
        listOf(
            ScamSignal.APP_INSTALL,
            ScamSignal.LIVE_CALL_CONTROL
        ).forEach { signal ->
            val card = TrustedHelperHandoffBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                "$signal recommended cards",
                listOf(
                    TrustedHelperCardId.DEVICE_APP_HELPER,
                    TrustedHelperCardId.EVIDENCE_REPORT_HELPER,
                    TrustedHelperCardId.FINANCE_ACCOUNT_HELPER
                ),
                card.recommendedCardIds
            )
            listOf(
                TrustedHelperActionId.ACCOUNT_LOCKDOWN,
                TrustedHelperActionId.EVIDENCE_KIT,
                TrustedHelperActionId.RESPONSE_CENTER
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in card.recommendedActionIds)
            }
            val copy = allCopy(card)
            assertFalse(copy.contains("악성 앱을 자동으로 제거합니다"))
            assertFalse(copy.contains("악성코드 제거를 보장합니다"))
        }
    }

    @Test
    fun linkAndSmishingSignalsRecommendEvidenceReportFamilyWarningAndCalmCompanionWithoutAutoScanClaim() {
        listOf(
            ScamSignal.LINK,
            ScamSignal.PARCEL_INVITATION_SMISHING
        ).forEach { signal ->
            val card = TrustedHelperHandoffBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                "$signal recommended cards",
                listOf(
                    TrustedHelperCardId.EVIDENCE_REPORT_HELPER,
                    TrustedHelperCardId.FAMILY_WARNING_HELPER,
                    TrustedHelperCardId.CALM_COMPANION
                ),
                card.recommendedCardIds
            )
            listOf(
                TrustedHelperActionId.EVIDENCE_KIT,
                TrustedHelperActionId.FAMILY_ALERT_MESSAGES,
                TrustedHelperActionId.ANALYSIS_RESULT
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in card.recommendedActionIds)
            }
            assertFalse(TrustedHelperActionId.RESPONSE_CENTER in card.recommendedActionIds)

            val copy = allCopy(card)
            assertFalse(copy.contains("자동으로 검사합니다"))
            assertFalse(copy.contains("자동 스캔합니다"))
        }
    }

    @Test
    fun lowReadinessRecommendsFamilyConfirmationHelperAndReadyReadinessCopyDiffersCalmly() {
        val lowReadinessCard = TrustedHelperHandoffBuilder.build(
            analysis = analysisWith(ScamSignal.APP_INSTALL),
            readiness = readiness(ready = false)
        )
        assertTrue(
            TrustedHelperCardId.FAMILY_CONFIRMATION_HELPER in lowReadinessCard.recommendedCardIds
        )
        assertTrue(lowReadinessCard.recommendationExplanation.contains("준비가 부족"))

        val readyReadinessCard = TrustedHelperHandoffBuilder.build(
            analysis = analysisWith(ScamSignal.APP_INSTALL),
            readiness = readiness(ready = true)
        )
        assertFalse(
            TrustedHelperCardId.FAMILY_CONFIRMATION_HELPER in readyReadinessCard.recommendedCardIds
        )
        assertTrue(readyReadinessCard.recommendationExplanation.contains("준비되어 있으니"))
        assertTrue(lowReadinessCard.recommendationExplanation != readyReadinessCard.recommendationExplanation)
    }

    @Test
    fun everyHelperCardHasCompleteFieldsAndStableIdOrder() {
        listOf(
            TrustedHelperHandoffBuilder.build(analysis = null, readiness = readiness()),
            TrustedHelperHandoffBuilder.build(
                analysis = analysisWith(
                    ScamSignal.APP_INSTALL,
                    ScamSignal.CREDENTIALS,
                    ScamSignal.LINK,
                    ScamSignal.PHONE_NUMBER_CHANGE
                ),
                readiness = readiness(ready = true)
            )
        ).forEach { card ->
            assertTrue(card.title.isNotBlank())
            assertTrue(card.intro.isNotBlank())
            assertTrue(card.recommendationTitle.isNotBlank())
            assertTrue(card.recommendationExplanation.isNotBlank())
            assertTrue(card.privacySafetyNotes.isNotEmpty())
            assertTrue(card.privacySafetyNotes.all { note -> note.isNotBlank() })
            assertTrue(
                card.recommendedCardIds.all { cardId ->
                    card.cards.any { helper -> helper.id == cardId }
                }
            )

            card.cards.forEach { helper ->
                assertTrue(helper.displayOrder > 0)
                assertTrue(helper.urgency.rank >= 0)
                assertTrue(helper.urgency.label.isNotBlank())
                assertTrue(helper.label.isNotBlank())
                assertTrue(helper.audience.isNotBlank())
                assertTrue(helper.whyItMatters.isNotBlank())
                assertTrue(helper.askSteps.isNotEmpty())
                assertTrue(helper.askSteps.all { step -> step.isNotBlank() })
                assertTrue(helper.doNotAskNotes.isNotEmpty())
                assertTrue(helper.doNotAskNotes.all { note -> note.isNotBlank() })
                assertTrue(helper.primaryActionLabel.isNotBlank())
                assertTrue(helper.secondaryActionLabels.all { label -> label.isNotBlank() })
                assertEquals(
                    helper.secondaryActions.map { action -> action.label },
                    helper.secondaryActionLabels
                )
            }
        }
    }

    @Test
    fun shareContainsHelperAndActionLabelsButExcludesSensitiveValues() {
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
        val card = TrustedHelperHandoffBuilder.build(analysis = analysis, readiness = readiness(ready = true))
        val message = card.handoffShareMessage
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)

        card.cards.forEach { helper ->
            assertTrue("share omitted card: ${helper.label}", message.contains(helper.label))
            (listOf(helper.primaryAction) + helper.secondaryActions).forEach { action ->
                assertTrue("share omitted action: ${action.label}", message.contains(action.label))
            }
        }
        assertTrue(message.contains("도움 요청"))
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
            assertFalse("handoff share leaked secret: $secret", message.contains(secret))
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

        val defaultCard = TrustedHelperHandoffBuilder.build(analysis = null, readiness = readiness())
        val defaultMessage = defaultCard.handoffShareMessage
        defaultCard.cards.forEach { helper ->
            assertTrue(defaultMessage.contains(helper.label))
            (listOf(helper.primaryAction) + helper.secondaryActions).forEach { action ->
                assertTrue(defaultMessage.contains(action.label))
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

        val first = TrustedHelperHandoffBuilder.build(analysis, readinessValue)
        val second = TrustedHelperHandoffBuilder.build(analysis, readinessValue)

        assertEquals(first, second)
        assertEquals(first.cards, second.cards)
        assertEquals(first.recommendedCardIds, second.recommendedCardIds)
        assertEquals(first.recommendedActionIds, second.recommendedActionIds)
        assertEquals(first.handoffShareMessage, second.handoffShareMessage)
    }

    @Test
    fun copyStatesManualNoStorageNoContactReadingNoAutoSendNoMonitoringNoNetworkNoHelperSecretAccess() {
        val cards = listOf(
            TrustedHelperHandoffBuilder.build(analysis = null, readiness = readiness()),
            TrustedHelperHandoffBuilder.build(analysis = analysisWith(ScamSignal.APP_INSTALL), readiness = readiness()),
            TrustedHelperHandoffBuilder.build(
                analysis = analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER),
                readiness = readiness(ready = true)
            ),
            TrustedHelperHandoffBuilder.build(analysis = analysisWith(ScamSignal.LINK), readiness = readiness())
        )

        cards.forEach { card ->
            val privacyCopy = card.privacySafetyNotes.joinToString(" ")
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
            assertTrue(privacyCopy.contains("도우미가 이런 비밀 정보에 접근하게 하지 않습니다"))
        }
    }

    @Test
    fun copyDoesNotImplyAutomaticMonitoringBlockingRemovalFreezeRecoveryLegalGuaranteeOrAutoDeletion() {
        val cards = listOf(
            TrustedHelperHandoffBuilder.build(analysis = null, readiness = readiness()),
            TrustedHelperHandoffBuilder.build(analysis = analysisWith(ScamSignal.APP_INSTALL), readiness = readiness()),
            TrustedHelperHandoffBuilder.build(
                analysis = analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER),
                readiness = readiness(ready = true)
            ),
            TrustedHelperHandoffBuilder.build(analysis = analysisWith(ScamSignal.LINK), readiness = readiness())
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

        cards.forEach { card ->
            val copy = allCopy(card)
            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "trusted helper handoff copy contained unsafe claim: $claim",
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

    private fun allCopy(card: TrustedHelperHandoffCard): String = buildString {
        append(card.title)
        append(card.intro)
        append(card.recommendationTitle)
        append(card.recommendationExplanation)
        card.cards.forEach { helper ->
            append(helper.urgency.label)
            append(helper.label)
            append(helper.audience)
            append(helper.whyItMatters)
            helper.askSteps.forEach(::append)
            helper.doNotAskNotes.forEach(::append)
            append(helper.primaryActionLabel)
            helper.secondaryActionLabels.forEach(::append)
        }
        card.privacySafetyNotes.forEach(::append)
        append(card.handoffShareMessage)
    }
}
