package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilySafetyDrillCoachBuilderTest {
    @Test
    fun noAnalysisAndNoSignalsHaveStableOrderAndConservativeRecommendations() {
        val noAnalysis = FamilySafetyDrillCoachBuilder.build(
            analysis = null,
            readiness = readiness()
        )
        val noSignals = FamilySafetyDrillCoachBuilder.build(
            analysis = ScamAnalysis(
                input = "일반 대화",
                score = 0,
                riskLevel = RiskLevel.SAFE,
                matchedSignals = emptyList()
            ),
            readiness = readiness()
        )
        val expectedDefaults = listOf(
            FamilySafetyDrillId.FAMILY_NEW_NUMBER_MONEY,
            FamilySafetyDrillId.OTP_PASSWORD_VERIFICATION,
            FamilySafetyDrillId.WEEKLY_PREVENTION_CHECKIN
        )

        listOf(noAnalysis, noSignals).forEach { coach ->
            assertEquals(FamilySafetyDrillId.entries, coach.cards.map { card -> card.id })
            assertEquals(
                (1..FamilySafetyDrillId.entries.size).toList(),
                coach.cards.map { card -> card.displayOrder }
            )
            assertEquals(expectedDefaults, coach.recommendedDrillIds)
            assertEquals(expectedDefaults, coach.recommendedCards.map { card -> card.id })
            assertEquals(
                listOf(
                    FamilySafetyDrillActionId.CHECKER,
                    FamilySafetyDrillActionId.SAFETY_PLAN
                ),
                coach.recommendedActionIds
            )
        }
        assertEquals("분석 전 가족 기본 연습", noAnalysis.recommendationTitle)
        assertEquals("뚜렷한 신호가 없어도 해둘 기본 연습", noSignals.recommendationTitle)
        assertTrue(noAnalysis.recommendationExplanation.contains("현재 분석이 없어"))
        assertTrue(noSignals.recommendationExplanation.contains("안전을 단정하지 않"))
    }

    @Test
    fun everyDrillCardHasCompleteFictionalFieldsActionsAndStableOrder() {
        val coach = FamilySafetyDrillCoachBuilder.build(null, readiness())

        assertTrue(coach.title.isNotBlank())
        assertTrue(coach.intro.isNotBlank())
        assertTrue(coach.fictionNotice.contains("실제 사건이 아닌"))
        assertTrue(coach.recommendationTitle.isNotBlank())
        assertTrue(coach.recommendationExplanation.isNotBlank())
        assertTrue(coach.privacySafetyNotes.isNotEmpty())
        assertEquals(FamilySafetyDrillId.entries, coach.cards.map { card -> card.id })
        assertEquals(
            (1..FamilySafetyDrillId.entries.size).toList(),
            coach.cards.map { card -> card.displayOrder }
        )
        assertEquals(coach.cards.size, coach.cards.map { card -> card.id }.distinct().size)

        coach.cards.forEach { card ->
            assertEquals(card.id.displayOrder, card.displayOrder)
            assertTrue(card.scenario.isNotBlank())
            assertTrue(card.title.contains("가상 연습"))
            assertTrue(card.fictionalSetup.contains("연습용 가상"))
            assertTrue(card.rolePlayLines.size >= 2)
            assertTrue(card.rolePlayLines.all { line -> line.isNotBlank() })
            assertTrue(card.rolePlayLines.any { line -> line.contains("(가상)") })
            assertTrue(card.expectedStopPhrase.isNotBlank())
            assertTrue(card.responseSteps.size >= 3)
            assertTrue(card.responseSteps.all { step -> step.isNotBlank() })
            assertTrue(card.passChecklist.size >= 3)
            assertTrue(card.passChecklist.all { item -> item.isNotBlank() })
            assertTrue(card.familyReflectionPrompt.isNotBlank())
            assertTrue(card.difficulty.rank >= 0)
            assertTrue(card.difficulty.label.isNotBlank())
            assertTrue(card.urgency.rank >= 0)
            assertTrue(card.urgency.label.isNotBlank())
            assertTrue(card.primaryActionLabel.isNotBlank())
            assertTrue(card.secondaryActions.isNotEmpty())
            assertTrue(card.secondaryActionLabels.all { label -> label.isNotBlank() })
            assertEquals(
                card.secondaryActions.map { action -> action.label },
                card.secondaryActionLabels
            )
        }

        val withAnalysis = FamilySafetyDrillCoachBuilder.build(
            analysis = analysisWith(ScamSignal.LINK),
            readiness = readiness(ready = true)
        )
        val routedActionIds = (coach.cards + withAnalysis.cards)
            .flatMap { card -> listOf(card.primaryAction) + card.secondaryActions }
            .map { action -> action.id }
            .toSet()
        assertEquals(FamilySafetyDrillActionId.entries.toSet(), routedActionIds)
    }

    @Test
    fun everyDrillContainsNoRealPhoneAccountOtpPinLinkDomainOrResidentValues() {
        val coach = FamilySafetyDrillCoachBuilder.build(null, readiness())

        coach.cards.forEach { card ->
            val cardCopy = cardCopy(card)
            assertFalse("${card.id} contained a full phone", phonePattern.containsMatchIn(cardCopy))
            assertFalse("${card.id} contained a long secret number", longNumberPattern.containsMatchIn(cardCopy))
            assertFalse("${card.id} contained a grouped account-like number", groupedNumberPattern.containsMatchIn(cardCopy))
            assertFalse("${card.id} contained a URL or domain", urlDomainPattern.containsMatchIn(cardCopy))
            assertFalse("${card.id} contained a resident number", residentPattern.containsMatchIn(cardCopy))
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendFamilyDrillAndRoutesFirst() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val coach = FamilySafetyDrillCoachBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                FamilySafetyDrillId.FAMILY_NEW_NUMBER_MONEY,
                coach.recommendedDrillIds.first()
            )
            assertEquals(
                listOf(
                    FamilySafetyDrillActionId.FAMILY_CALLBACK_CARD,
                    FamilySafetyDrillActionId.FAMILY_ALERT_MESSAGES,
                    FamilySafetyDrillActionId.SAFE_CALL_SCRIPTS
                ),
                coach.recommendedActionIds.take(3)
            )
        }
    }

    @Test
    fun moneySafeAccountLoanGiftCryptoPaymentAndCredentialsRecommendFinancialDrillsAndRoutes() {
        val expectedDrillBySignal = mapOf(
            ScamSignal.URGENT_TRANSFER to FamilySafetyDrillId.PROSECUTOR_SAFE_ACCOUNT,
            ScamSignal.IMPERSONATION to FamilySafetyDrillId.PROSECUTOR_SAFE_ACCOUNT,
            ScamSignal.BAIT to FamilySafetyDrillId.PROSECUTOR_SAFE_ACCOUNT,
            ScamSignal.LOAN_REFINANCE_FEE to FamilySafetyDrillId.PROSECUTOR_SAFE_ACCOUNT,
            ScamSignal.SAFE_ACCOUNT_FRAUD to FamilySafetyDrillId.PROSECUTOR_SAFE_ACCOUNT,
            ScamSignal.GIFT_CRYPTO to FamilySafetyDrillId.GIFT_CARD_CRYPTO_TRANSFER,
            ScamSignal.GIFTICON_PIN to FamilySafetyDrillId.GIFT_CARD_CRYPTO_TRANSFER,
            ScamSignal.OVERSEAS_PAYMENT_ALERT to FamilySafetyDrillId.OVERSEAS_PAYMENT_CANCEL,
            ScamSignal.CREDENTIALS to FamilySafetyDrillId.OTP_PASSWORD_VERIFICATION
        )

        expectedDrillBySignal.forEach { (signal, expectedDrill) ->
            val coach = FamilySafetyDrillCoachBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals("unexpected first financial drill for $signal", expectedDrill, coach.recommendedDrillIds.first())
            listOf(
                FamilySafetyDrillActionId.QUICK_STOP,
                FamilySafetyDrillActionId.SAFE_CALL_SCRIPTS,
                FamilySafetyDrillActionId.ACCOUNT_LOCKDOWN,
                FamilySafetyDrillActionId.OFFICIAL_HELP_DIRECTORY
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in coach.recommendedActionIds)
            }
        }
    }

    @Test
    fun secrecyPressureUsesConservativeBasicsWithoutClaimingSafeAccountEvidence() {
        val coach = FamilySafetyDrillCoachBuilder.build(
            analysis = analysisWith(ScamSignal.SECRECY),
            readiness = readiness(ready = true)
        )

        assertEquals(
            listOf(
                FamilySafetyDrillId.FAMILY_NEW_NUMBER_MONEY,
                FamilySafetyDrillId.OTP_PASSWORD_VERIFICATION,
                FamilySafetyDrillId.WEEKLY_PREVENTION_CHECKIN
            ),
            coach.recommendedDrillIds
        )
        listOf(
            FamilySafetyDrillActionId.QUICK_STOP,
            FamilySafetyDrillActionId.SAFE_CALL_SCRIPTS,
            FamilySafetyDrillActionId.FAMILY_ALERT_MESSAGES
        ).forEach { actionId -> assertTrue(actionId in coach.recommendedActionIds) }
        assertTrue(coach.recommendationExplanation.contains("비밀 유지 압박"))
        assertFalse(coach.recommendationExplanation.contains("송금·안전계좌·인증"))
    }

    @Test
    fun remoteControlAppAndScreenShareRecommendProtectionWithoutRemovalGuarantee() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val coach = FamilySafetyDrillCoachBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                FamilySafetyDrillId.REMOTE_CONTROL_SCREEN_SHARE,
                coach.recommendedDrillIds.first()
            )
            assertEquals(
                listOf(
                    FamilySafetyDrillActionId.ACCOUNT_LOCKDOWN,
                    FamilySafetyDrillActionId.EVIDENCE_KIT,
                    FamilySafetyDrillActionId.TRUSTED_HELPER_HANDOFF
                ),
                coach.recommendedActionIds.take(3)
            )
            assertFalse(allCopy(coach).contains("악성 앱을 자동으로 제거합니다"))
            assertFalse(allCopy(coach).contains("악성코드 제거를 보장합니다"))
        }
    }

    @Test
    fun linkAndSmishingRecommendSmishingDrillAndRoutesWithoutAutoScanClaim() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val coach = FamilySafetyDrillCoachBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                FamilySafetyDrillId.PARCEL_INVITATION_SMISHING,
                coach.recommendedDrillIds.first()
            )
            assertEquals(
                listOf(
                    FamilySafetyDrillActionId.EVIDENCE_KIT,
                    FamilySafetyDrillActionId.FAMILY_ALERT_MESSAGES,
                    FamilySafetyDrillActionId.RISK_SIGNAL_EXPLAINERS
                ),
                coach.recommendedActionIds.take(3)
            )
            assertFalse(allCopy(coach).contains("자동으로 검사합니다"))
            assertFalse(allCopy(coach).contains("자동 스캔합니다"))
        }
    }

    @Test
    fun lowReadinessAddsWeeklyCheckinAndSafetyPlanWhileReadyCopyDiffersCalmly() {
        val analysis = analysisWith(ScamSignal.APP_INSTALL)
        val low = FamilySafetyDrillCoachBuilder.build(analysis, readiness())
        val inProgress = FamilySafetyDrillCoachBuilder.build(
            analysis,
            FamilyReadinessBuilder.build(
                SafetyPlan(
                    safeWord = "우리 가족만 아는 질문",
                    callbackScript = "",
                    contacts = emptyList()
                )
            )
        )
        val ready = FamilySafetyDrillCoachBuilder.build(analysis, readiness(ready = true))

        assertTrue(FamilySafetyDrillId.WEEKLY_PREVENTION_CHECKIN in low.recommendedDrillIds)
        assertTrue(FamilySafetyDrillActionId.SAFETY_PLAN in low.recommendedActionIds)
        assertTrue(FamilySafetyDrillId.WEEKLY_PREVENTION_CHECKIN in inProgress.recommendedDrillIds)
        assertTrue(FamilySafetyDrillActionId.SAFETY_PLAN in inProgress.recommendedActionIds)
        assertTrue(low.recommendationExplanation.contains("부족해도 괜찮습니다"))
        assertTrue(low.recommendationExplanation.contains("차분히"))
        assertTrue(inProgress.recommendationExplanation.contains("진행 중"))
        assertTrue(inProgress.recommendationExplanation.contains("차분히"))
        assertTrue(ready.recommendationExplanation.contains("준비되어 있습니다"))
        assertTrue(ready.recommendationExplanation.contains("비밀값을 드러내지 않고"))
        assertNotEquals(low.recommendationExplanation, ready.recommendationExplanation)
    }

    @Test
    fun highRiskAlwaysIncludesQuickStopAndRecoveryPlanActions() {
        val withSignals = FamilySafetyDrillCoachBuilder.build(
            analysis = analysisWith(
                ScamSignal.DEEPFAKE_VOICE,
                ScamSignal.CREDENTIALS,
                riskLevel = RiskLevel.HIGH
            ),
            readiness = readiness(ready = true)
        )
        val withoutSignals = FamilySafetyDrillCoachBuilder.build(
            analysis = ScamAnalysis("테스트", 80, RiskLevel.HIGH, emptyList()),
            readiness = readiness()
        )

        listOf(withSignals, withoutSignals).forEach { coach ->
            assertEquals(
                listOf(
                    FamilySafetyDrillActionId.QUICK_STOP,
                    FamilySafetyDrillActionId.RECOVERY_PLAN
                ),
                coach.recommendedActionIds.take(2)
            )
            assertTrue(FamilySafetyDrillActionId.QUICK_STOP in coach.recommendedActionIds)
            assertTrue(FamilySafetyDrillActionId.RECOVERY_PLAN in coach.recommendedActionIds)
        }
    }

    @Test
    fun shareContainsOnlyScenarioTitlesStopChecklistAndActionLabelsWithoutSecrets() {
        val safeWord = "해질녘 파란 우산은 어디에?"
        val callbackScript = "푸른 메모장을 열고 첫 번째 저장 번호로 전화한다."
        val contactLabel = "보호자-은하수"
        val contactNumber = "010-2468-1357"
        val raw =
            "RAW-DRILL-COMET 전화 010-9876-5432 계좌 123456789012 비밀번호 BluePassword77 " +
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
        val coach = FamilySafetyDrillCoachBuilder.build(
            analysis = analysis,
            readiness = FamilyReadinessBuilder.build(
                SafetyPlan(
                    safeWord = safeWord,
                    callbackScript = callbackScript,
                    contacts = listOf(TrustedContact(contactLabel, contactNumber))
                )
            )
        )
        val message = coach.familyShareSummary
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)

        assertEquals(expectedFamilyShareSummary(coach), message)

        coach.cards.forEach { card ->
            assertTrue("share omitted title: ${card.title}", message.contains(card.title))
            assertTrue("share omitted stop phrase: ${card.id}", message.contains(card.expectedStopPhrase))
            card.passChecklist.forEach { label ->
                assertTrue("share omitted pass label: $label", message.contains(label))
            }
            (listOf(card.primaryAction) + card.secondaryActions).forEach { action ->
                assertTrue("share omitted action: ${action.label}", message.contains(action.label))
            }
            assertFalse("share included setup: ${card.id}", message.contains(card.fictionalSetup))
            assertFalse("share included scenario detail: ${card.id}", message.contains(card.scenario))
            card.rolePlayLines.forEach { line ->
                assertFalse("share included role line: $line", message.contains(line))
            }
            card.responseSteps.forEach { step ->
                assertFalse("share included response step: $step", message.contains(step))
            }
            assertFalse(message.contains(card.familyReflectionPrompt))
        }
        coach.recommendedActions.forEach { action -> assertTrue(message.contains(action.label)) }
        assertFalse(message.contains(raw))
        assertFalse(message.contains(redactedClue))
        val secrets = listOf(
            "RAW-DRILL-COMET",
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
        )
        secrets.forEach { secret ->
            assertFalse("drill share leaked secret: $secret", message.contains(secret))
        }
        val completeCopy = allCopy(coach)
        assertFalse(completeCopy.contains(raw))
        assertFalse(completeCopy.contains(redactedClue))
        secrets.forEach { secret ->
            assertFalse("drill coach output leaked secret: $secret", completeCopy.contains(secret))
        }
        assertFalse(phonePattern.containsMatchIn(message))
        assertFalse(longNumberPattern.containsMatchIn(message))
        assertFalse(groupedNumberPattern.containsMatchIn(message))
        assertFalse(urlDomainPattern.containsMatchIn(message))
        assertFalse(residentPattern.containsMatchIn(message))
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

        val first = FamilySafetyDrillCoachBuilder.build(analysis, readinessValue)
        val second = FamilySafetyDrillCoachBuilder.build(analysis, readinessValue)

        assertEquals(first, second)
        assertEquals(first.cards, second.cards)
        assertEquals(first.recommendedDrillIds, second.recommendedDrillIds)
        assertEquals(first.recommendedActionIds, second.recommendedActionIds)
        assertEquals(first.familyShareSummary, second.familyShareSummary)
    }

    @Test
    fun copyStatesFictionalManualLocalNoStorageNoContactReadingNoAutoSendNoMonitoringAndNoNetwork() {
        representativeCoaches().forEach { coach ->
            val privacyCopy = coach.privacySafetyNotes.joinToString(" ")
            assertTrue(coach.fictionNotice.contains("가상 시나리오"))
            assertTrue(privacyCopy.contains("수동 연습 안내"))
            assertTrue(privacyCopy.contains("실제 사건이 아닌"))
            assertTrue(privacyCopy.contains("기기 안"))
            assertTrue(privacyCopy.contains("로컬 규칙"))
            assertTrue(privacyCopy.contains("사용자가 직접"))
            assertTrue(privacyCopy.contains("선택한 드릴"))
            assertTrue(privacyCopy.contains("통과 여부"))
            assertTrue(privacyCopy.contains("체크 상태"))
            assertTrue(privacyCopy.contains("읽음·진행 상태"))
            assertTrue(privacyCopy.contains("공유 이력"))
            assertTrue(privacyCopy.contains("연습 기록"))
            assertTrue(privacyCopy.contains("저장하지 않습니다"))
            assertTrue(privacyCopy.contains("연락처를 읽거나 받는 사람을 고르지 않습니다"))
            assertTrue(privacyCopy.contains("자동으로 보내지 않습니다"))
            assertTrue(privacyCopy.contains("자동으로 읽거나 감시하지 않"))
            assertTrue(privacyCopy.contains("자동 모니터링하지 않습니다"))
            assertTrue(privacyCopy.contains("네트워크를 사용하지 않습니다"))
            assertTrue(privacyCopy.contains("사건 기록을 추가·수정·삭제하지 않습니다"))
            assertTrue(privacyCopy.contains("새 Android 권한을 요청하지 않"))
            assertTrue(privacyCopy.contains("AI 모델을 학습시키지 않"))
        }
    }

    @Test
    fun copyDoesNotImplyTrainingMonitoringPreventionBlockingRemovalFreezeRecoveryOrGuarantees() {
        val forbiddenAffirmativeClaims = listOf(
            "모든 사기를 막습니다",
            "모든 사기를 예방합니다",
            "사기를 확정합니다",
            "사기를 예방합니다",
            "AI 모델을 학습합니다",
            "AI 모델을 훈련합니다",
            "훈련 데이터를 저장합니다",
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
            "악성코드 제거를 보장합니다",
            "계좌를 자동으로 지급정지합니다",
            "카드를 자동으로 정지합니다",
            "계좌를 정지합니다",
            "카드를 정지합니다",
            "계좌·카드를 정지합니다",
            "계정을 자동으로 잠급니다",
            "피해금 회수를 보장합니다",
            "피해금을 회수합니다",
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
            "trains an ai model",
            "conclusive prevention",
            "blocks calls automatically",
            "blocks messages automatically",
            "malware removal guarantee",
            "account freeze guarantee",
            "guaranteed recovery",
            "legal guarantee"
        )

        representativeCoaches().forEach { coach ->
            val copy = allCopy(coach)
            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "family drill coach copy contained unsafe claim: $claim",
                    copy.lowercase().contains(claim.lowercase())
                )
            }
        }
    }

    private fun representativeCoaches(): List<FamilySafetyDrillCoach> = listOf(
        FamilySafetyDrillCoachBuilder.build(null, readiness()),
        FamilySafetyDrillCoachBuilder.build(analysisWith(ScamSignal.APP_INSTALL), readiness()),
        FamilySafetyDrillCoachBuilder.build(
            analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER),
            readiness(ready = true)
        ),
        FamilySafetyDrillCoachBuilder.build(analysisWith(ScamSignal.LINK), readiness()),
        FamilySafetyDrillCoachBuilder.build(analysisWith(ScamSignal.DEEPFAKE_VOICE), readiness())
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

    private fun readiness(ready: Boolean = false): FamilyReadiness =
        FamilyReadinessBuilder.build(
            if (ready) {
                SafetyPlan(
                    safeWord = "우리 가족만 아는 질문",
                    callbackScript = "통화를 끝내고 저장된 번호로 다시 전화한다.",
                    contacts = listOf(
                        TrustedContact("가족", "저장된 번호"),
                        TrustedContact("예비 가족", "예비 저장 번호")
                    )
                )
            } else {
                SafetyPlan(safeWord = "", callbackScript = "", contacts = emptyList())
            }
        )

    private fun cardCopy(card: FamilySafetyDrillCard): String = buildString {
        append(card.scenario)
        append(card.title)
        append(card.fictionalSetup)
        card.rolePlayLines.forEach(::append)
        append(card.expectedStopPhrase)
        card.responseSteps.forEach(::append)
        card.passChecklist.forEach(::append)
        append(card.familyReflectionPrompt)
        append(card.difficulty.label)
        append(card.urgency.label)
        append(card.primaryAction.label)
        card.secondaryActions.forEach { action -> append(action.label) }
    }

    private fun allCopy(coach: FamilySafetyDrillCoach): String = buildString {
        append(coach.title)
        append(coach.intro)
        append(coach.fictionNotice)
        append(coach.recommendationTitle)
        append(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> append(action.label) }
        coach.cards.forEach { card -> append(cardCopy(card)) }
        coach.privacySafetyNotes.forEach(::append)
        append(coach.familyShareSummary)
    }

    private fun expectedFamilyShareSummary(coach: FamilySafetyDrillCoach): String {
        val cardsById = coach.cards.associateBy { card -> card.id }
        val allActions = coach.cards
            .flatMap { card -> listOf(card.primaryAction) + card.secondaryActions }
            .plus(coach.recommendedActions)
            .distinctBy { action -> action.label }

        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 가상 연습 과제")
            appendLine()
            appendLine("추천 가상 시나리오 제목")
            coach.recommendedDrillIds.forEach { drillId ->
                appendLine("- ${cardsById.getValue(drillId).title}")
            }
            appendLine()
            appendLine("가상 시나리오별 멈춤 문구와 통과 체크")
            coach.cards.forEach { card ->
                appendLine("- ${card.title}")
                appendLine("  멈춤 문구: ${card.expectedStopPhrase}")
                card.passChecklist.forEach { checklistLabel ->
                    appendLine("  통과 체크: $checklistLabel")
                }
            }
            appendLine()
            appendLine("기존 앱 행동")
            allActions.forEach { action -> appendLine("- ${action.label}") }
        }.trim()
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
