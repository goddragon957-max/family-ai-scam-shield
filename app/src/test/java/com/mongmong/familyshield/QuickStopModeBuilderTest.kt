package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickStopModeBuilderTest {
    @Test
    fun noAnalysisHasExactStopScriptStableCardOrderAndConservativeRecommendations() {
        val mode = QuickStopModeBuilder.build(
            analysis = null,
            readiness = lowReadiness()
        )

        assertEquals("긴급 멈춤 모드", mode.title)
        assertEquals("분석 전, 이 순서부터", mode.recommendationTitle)
        assertEquals("그대로 읽는 3단계 멈춤 대사", mode.stopScriptTitle)
        assertEquals(
            listOf(
                QuickStopStep(1, "지금 대화를 멈추고 전화를 끊거나 답장을 보류하세요."),
                QuickStopStep(2, "확인 전에는 돈을 보내거나 개인정보·인증번호를 공유하거나 앱을 설치하지 마세요."),
                QuickStopStep(3, "상대가 알려준 새 번호·링크가 아닌, 평소 저장한 가족 번호나 공식 경로로 직접 확인하세요.")
            ),
            mode.stopScriptSteps
        )
        assertEquals(
            listOf(
                QuickStopCardId.STOP_CONVERSATION,
                QuickStopCardId.FAMILY_CALLBACK,
                QuickStopCardId.RISK_ANALYSIS,
                QuickStopCardId.EXPOSURE_RESPONSE,
                QuickStopCardId.OFFICIAL_HELP,
                QuickStopCardId.EVIDENCE_PRESERVATION,
                QuickStopCardId.FAMILY_ALERT
            ),
            mode.cards.map { card -> card.id }
        )
        assertEquals((1..7).toList(), mode.cards.map { card -> card.displayOrder })
        assertEquals(
            listOf(
                QuickStopCardId.STOP_CONVERSATION,
                QuickStopCardId.RISK_ANALYSIS,
                QuickStopCardId.FAMILY_CALLBACK
            ),
            mode.recommendedCardIds
        )
        assertEquals(
            mode.recommendedCardIds,
            mode.recommendedCards.map { card -> card.id }
        )

        val stopCard = mode.cards.single { card -> card.id == QuickStopCardId.STOP_CONVERSATION }
        val analysisCard = mode.cards.single { card -> card.id == QuickStopCardId.RISK_ANALYSIS }
        assertEquals(QuickStopActionId.STOP_SCRIPT, stopCard.primaryAction.id)
        assertTrue(stopCard.safetyNotes.any { note -> note.contains("이 화면에 그대로 머뭅니다") })
        assertEquals(QuickStopActionId.CHECKER, analysisCard.primaryAction.id)
        assertFalse(QuickStopActionId.ANALYSIS_RESULT in analysisCard.secondaryActions.map { it.id })
    }

    @Test
    fun highRiskAndUrgentExposureSignalsRecommendStopResponseAccountEvidenceAndHelp() {
        val expectedCards = listOf(
            QuickStopCardId.STOP_CONVERSATION,
            QuickStopCardId.EXPOSURE_RESPONSE,
            QuickStopCardId.EVIDENCE_PRESERVATION,
            QuickStopCardId.OFFICIAL_HELP
        )
        val requiredActions = listOf(
            QuickStopActionId.STOP_SCRIPT,
            QuickStopActionId.RESPONSE_CENTER,
            QuickStopActionId.ACCOUNT_LOCKDOWN,
            QuickStopActionId.EVIDENCE_KIT,
            QuickStopActionId.OFFICIAL_HELP_DIRECTORY
        )

        val highRiskMode = QuickStopModeBuilder.build(
            analysis = analysisWithRisk(RiskLevel.HIGH),
            readiness = readyReadiness()
        )
        assertEquals(expectedCards, highRiskMode.recommendedCardIds)
        requiredActions.forEach { actionId ->
            assertTrue("high-risk recommendation omitted $actionId", actionId in highRiskMode.recommendedActionIds)
        }

        listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.CREDENTIALS,
            ScamSignal.APP_INSTALL,
            ScamSignal.LIVE_CALL_CONTROL,
            ScamSignal.SAFE_ACCOUNT_FRAUD
        ).forEach { signal ->
            val mode = QuickStopModeBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertEquals(signal.name, expectedCards, mode.recommendedCardIds)
            requiredActions.forEach { actionId ->
                assertTrue("$signal recommendation omitted $actionId", actionId in mode.recommendedActionIds)
            }
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangePutFamilyCallbackImmediatelyAfterStop() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val mode = QuickStopModeBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertEquals(
                signal.name,
                listOf(
                    QuickStopCardId.STOP_CONVERSATION,
                    QuickStopCardId.FAMILY_CALLBACK,
                    QuickStopCardId.RISK_ANALYSIS
                ),
                mode.recommendedCardIds
            )
            assertEquals(
                QuickStopActionId.FAMILY_CALLBACK_CARD,
                mode.recommendedCards[1].primaryAction.id
            )
        }

        val familyAndMoney = QuickStopModeBuilder.build(
            analysis = analysisWith(
                ScamSignal.FAMILY_EMERGENCY,
                ScamSignal.DEEPFAKE_VOICE,
                ScamSignal.PHONE_NUMBER_CHANGE,
                ScamSignal.URGENT_TRANSFER
            ),
            readiness = readyReadiness()
        )
        assertEquals(QuickStopCardId.FAMILY_CALLBACK, familyAndMoney.recommendedCardIds[1])
        assertTrue(
            familyAndMoney.recommendedCardIds.indexOf(QuickStopCardId.FAMILY_CALLBACK) <
                familyAndMoney.recommendedCardIds.indexOf(QuickStopCardId.EXPOSURE_RESPONSE)
        )
    }

    @Test
    fun linkAndSmishingRecommendCurrentAnalysisEvidenceFamilyAlertAndHelpWithoutAutoScan() {
        listOf(
            ScamSignal.LINK,
            ScamSignal.PARCEL_INVITATION_SMISHING
        ).forEach { signal ->
            val mode = QuickStopModeBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertEquals(
                signal.name,
                listOf(
                    QuickStopCardId.STOP_CONVERSATION,
                    QuickStopCardId.RISK_ANALYSIS,
                    QuickStopCardId.EVIDENCE_PRESERVATION,
                    QuickStopCardId.FAMILY_ALERT,
                    QuickStopCardId.OFFICIAL_HELP
                ),
                mode.recommendedCardIds
            )
            listOf(
                QuickStopActionId.ANALYSIS_RESULT,
                QuickStopActionId.CHECKER,
                QuickStopActionId.EVIDENCE_KIT,
                QuickStopActionId.FAMILY_ALERT_MESSAGES,
                QuickStopActionId.OFFICIAL_HELP_DIRECTORY
            ).forEach { actionId ->
                assertTrue("$signal recommendation omitted $actionId", actionId in mode.recommendedActionIds)
            }
            assertFalse(QuickStopActionId.RESPONSE_CENTER in mode.recommendedActionIds)

            val analysisCard = mode.cards.single { card -> card.id == QuickStopCardId.RISK_ANALYSIS }
            assertEquals(QuickStopActionId.ANALYSIS_RESULT, analysisCard.primaryAction.id)
            assertTrue(analysisCard.explanation.contains("직접 붙여넣"))
            assertTrue(
                analysisCard.safetyNotes.any { note ->
                    note.contains("자동으로") && note.contains("기능이 아니라") && note.contains("텍스트만")
                }
            )
            assertFalse(allCopy(mode).contains("자동으로 검사합니다"))
            assertFalse(allCopy(mode).contains("자동 스캔합니다"))
        }
    }

    @Test
    fun lowReadinessAddsCallbackAndPlanSetupWhileReadyCopyStaysCalmAndDifferent() {
        val analysis = analysisWith(ScamSignal.URGENT_TRANSFER, ScamSignal.CREDENTIALS)
        val lowMode = QuickStopModeBuilder.build(analysis, lowReadiness())
        val readyMode = QuickStopModeBuilder.build(analysis, readyReadiness())

        assertEquals(QuickStopCardId.FAMILY_CALLBACK, lowMode.recommendedCardIds[1])
        assertTrue(QuickStopCardId.FAMILY_CALLBACK in lowMode.recommendedCardIds)
        assertFalse(QuickStopCardId.FAMILY_CALLBACK in readyMode.recommendedCardIds)
        assertTrue(lowMode.readinessMessage.contains("부족해도 괜찮습니다"))
        assertTrue(lowMode.readinessMessage.contains("차분히"))
        assertTrue(readyMode.readinessMessage.contains("준비되어 있습니다"))
        assertTrue(readyMode.readinessMessage.contains("차분히"))
        assertFalse(lowMode.readinessMessage == readyMode.readinessMessage)

        val lowCallback = lowMode.cards.single { card -> card.id == QuickStopCardId.FAMILY_CALLBACK }
        val readyCallback = readyMode.cards.single { card -> card.id == QuickStopCardId.FAMILY_CALLBACK }
        assertEquals(
            "가족 안전 계획 만들기",
            lowCallback.secondaryActions.single { action -> action.id == QuickStopActionId.SAFETY_PLAN }.label
        )
        assertEquals(
            "가족 안전 계획 확인하기",
            readyCallback.secondaryActions.single { action -> action.id == QuickStopActionId.SAFETY_PLAN }.label
        )
    }

    @Test
    fun everyCardHasCompleteCopyMappedActionsAndStableUniqueIds() {
        val modes = listOf(
            QuickStopModeBuilder.build(null, lowReadiness()),
            QuickStopModeBuilder.build(
                analysisWith(
                    ScamSignal.PHONE_NUMBER_CHANGE,
                    ScamSignal.CREDENTIALS,
                    ScamSignal.APP_INSTALL,
                    ScamSignal.LINK
                ),
                readyReadiness()
            )
        )

        modes.forEach { mode ->
            assertTrue(mode.title.isNotBlank())
            assertTrue(mode.intro.isNotBlank())
            assertTrue(mode.stopScriptTitle.isNotBlank())
            assertEquals(listOf(1, 2, 3), mode.stopScriptSteps.map { step -> step.displayOrder })
            assertTrue(mode.stopScriptSteps.all { step -> step.instruction.isNotBlank() })
            assertTrue(mode.recommendationTitle.isNotBlank())
            assertTrue(mode.recommendationExplanation.isNotBlank())
            assertTrue(mode.readinessMessage.isNotBlank())
            assertTrue(mode.privacySafetyNotes.isNotEmpty())
            assertTrue(mode.privacySafetyNotes.all { note -> note.isNotBlank() })
            assertTrue(mode.familyShareSummary.isNotBlank())
            assertEquals(QuickStopCardId.entries.size, mode.cards.size)
            assertEquals(mode.cards.size, mode.cards.map { card -> card.id }.distinct().size)
            assertEquals((1..mode.cards.size).toList(), mode.cards.map { card -> card.displayOrder })
            assertTrue(mode.recommendedCardIds.all { cardId -> mode.cards.any { card -> card.id == cardId } })

            mode.cards.forEach { card ->
                assertTrue(card.displayOrder > 0)
                assertTrue(card.urgency.rank >= 0)
                assertTrue(card.urgency.label.isNotBlank())
                assertTrue(card.label.isNotBlank())
                assertTrue(card.explanation.isNotBlank())
                assertTrue(card.primaryAction.label.isNotBlank())
                assertEquals(card.primaryAction.label, card.primaryActionLabel)
                assertTrue(card.secondaryActions.all { action -> action.label.isNotBlank() })
                assertEquals(card.secondaryActions.map { action -> action.label }, card.secondaryActionLabels)
                assertTrue(card.safetyNotes.isNotEmpty())
                assertTrue(card.safetyNotes.all { note -> note.isNotBlank() })
            }
        }

        assertEquals(
            QuickStopActionId.entries.toSet(),
            modes.flatMap { mode -> mode.cards }
                .flatMap { card -> listOf(card.primaryAction) + card.secondaryActions }
                .map { action -> action.id }
                .toSet()
        )
    }

    @Test
    fun familyShareContainsOnlyGenericStopGuidanceAndRouteLabelsWithoutSecretsOrPatterns() {
        val safeWord = "해질녘 파란 우산은 어디에?"
        val callbackScript = "PRIVATE-CALLBACK-푸른메모장"
        val contactLabel = "보호자-은하수"
        val contactNumber = "010-2468-1357"
        val raw =
            "RAW-QUICK-COMET 전화 010-9876-5432 국제전화 +82 10 9876 5432 " +
                "계좌 123456789012 묶음계좌 1234-5678-9012 묶음OTP 482 913 " +
                "비밀번호 BluePassword77 OTP A1B2C3 PIN 7788 " +
                "https://evil.example/path http://192.0.2.1/path malicious.app 주민번호 900101-1234567 " +
                "안전 질문 $safeWord 콜백 $callbackScript 연락처 $contactLabel $contactNumber"
        val analysis = ScamAnalysis(
            input = raw,
            score = 100,
            riskLevel = RiskLevel.HIGH,
            matchedSignals = listOf(
                ScamSignal.PHONE_NUMBER_CHANGE,
                ScamSignal.CREDENTIALS,
                ScamSignal.APP_INSTALL,
                ScamSignal.LINK
            )
        )
        val plan = SafetyPlan(
            safeWord = safeWord,
            callbackScript = callbackScript,
            contacts = listOf(TrustedContact(contactLabel, contactNumber))
        )
        val mode = QuickStopModeBuilder.build(analysis, FamilyReadinessBuilder.build(plan))
        val summary = mode.familyShareSummary
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)

        mode.stopScriptSteps.forEach { step ->
            assertTrue("share omitted stop step ${step.displayOrder}", summary.contains(step.instruction))
        }
        mode.cards.forEach { card ->
            assertTrue("share omitted card label: ${card.label}", summary.contains(card.label))
            (listOf(card.primaryAction) + card.secondaryActions).forEach { action ->
                assertTrue("share omitted action label: ${action.label}", summary.contains(action.label))
            }
        }
        assertTrue(summary.contains("수동 확인"))
        assertTrue(summary.contains("포함하지 않았습니다"))
        assertFalse(summary.contains(raw))
        assertFalse(summary.contains(redactedClue))
        listOf(
            "RAW-QUICK-COMET",
            safeWord,
            callbackScript,
            contactLabel,
            contactNumber,
            "010-9876-5432",
            "+82 10 9876 5432",
            "123456789012",
            "1234-5678-9012",
            "482 913",
            "BluePassword77",
            "A1B2C3",
            "7788",
            "evil.example",
            "192.0.2.1",
            "malicious.app",
            "900101-1234567"
        ).forEach { secret ->
            assertFalse("quick-stop share leaked secret: $secret", summary.contains(secret))
        }
        assertFalse(Regex("0\\d{1,2}[- .]?\\d{3,4}[- .]?\\d{4}").containsMatchIn(summary))
        assertFalse(
            Regex("(?<!\\d)(?:\\+82[- .]?)?0?1\\d[- .]?\\d{3,4}[- .]?\\d{4}(?!\\d)")
                .containsMatchIn(summary)
        )
        assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(summary))
        assertFalse(Regex("(?<!\\d)(?:\\d[- .]?){5,}\\d(?!\\d)").containsMatchIn(summary))
        assertFalse(
            Regex(
                "(?i)(?:[a-z][a-z0-9+.-]*://|www\\.|" +
                    "(?:[a-z0-9-]+\\.)+[a-z]{2,63}\\b|\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b)"
            ).containsMatchIn(summary)
        )
        assertFalse(
            Regex("(?<!\\d)\\d{6}\\s*-?\\s*[1-8]\\d{6}(?!\\d)")
                .containsMatchIn(summary)
        )
    }

    @Test
    fun outputIsDeterministicForTheSameInputs() {
        val analysis = analysisWith(
            ScamSignal.PHONE_NUMBER_CHANGE,
            ScamSignal.CREDENTIALS,
            ScamSignal.APP_INSTALL,
            ScamSignal.LINK
        )
        val readiness = readyReadiness()

        val first = QuickStopModeBuilder.build(analysis, readiness)
        val second = QuickStopModeBuilder.build(analysis, readiness)

        assertEquals(first, second)
        assertEquals(first.stopScriptSteps, second.stopScriptSteps)
        assertEquals(first.cards, second.cards)
        assertEquals(first.recommendedCardIds, second.recommendedCardIds)
        assertEquals(first.recommendedActionIds, second.recommendedActionIds)
        assertEquals(first.familyShareSummary, second.familyShareSummary)
    }

    @Test
    fun copyStatesManualLocalStatelessNoContactReadingOrAutoSendWithoutUnsafeClaims() {
        val modes = listOf(
            QuickStopModeBuilder.build(null, lowReadiness()),
            QuickStopModeBuilder.build(
                analysisWith(ScamSignal.PHONE_NUMBER_CHANGE),
                readyReadiness()
            ),
            QuickStopModeBuilder.build(
                analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER),
                lowReadiness()
            ),
            QuickStopModeBuilder.build(
                analysisWith(ScamSignal.APP_INSTALL, ScamSignal.LINK),
                readyReadiness()
            )
        )
        val forbiddenAffirmativeClaims = listOf(
            "자동으로 감시합니다",
            "자동 모니터링합니다",
            "몰래 확인합니다",
            "자동으로 검사합니다",
            "자동 스캔합니다",
            "통화를 차단합니다",
            "문자를 차단합니다",
            "SMS를 차단합니다",
            "연락처를 읽습니다",
            "연락처를 자동으로 읽습니다",
            "연락처에서 가져옵니다",
            "메시지를 자동으로 보냅니다",
            "문자를 자동으로 보냅니다",
            "자동 전송합니다",
            "서버로 업로드합니다",
            "네트워크로 업로드합니다",
            "악성 앱을 제거합니다",
            "악성코드를 제거합니다",
            "계좌를 정지합니다",
            "계좌를 동결합니다",
            "카드를 정지합니다",
            "피해 회복을 보장합니다",
            "피해금 회수를 보장합니다",
            "반드시 환급됩니다",
            "반드시 회수됩니다",
            "법적 효력을 보장합니다",
            "법적 결과를 보장합니다",
            "안전을 보장합니다",
            "자동으로 삭제합니다",
            "자동 삭제합니다",
            "100%",
            "automatic monitoring",
            "blocks calls automatically",
            "blocks messages automatically",
            "reads contacts automatically",
            "server upload",
            "removes malware",
            "freezes accounts",
            "guaranteed recovery",
            "legal guarantee"
        )

        modes.forEach { mode ->
            val copy = allCopy(mode)
            val privacyCopy = mode.privacySafetyNotes.joinToString(" ")

            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "quick-stop copy contained unsafe claim: $claim",
                    copy.lowercase().contains(claim.lowercase())
                )
            }
            assertTrue(privacyCopy.contains("사용자가 직접 여는 수동 안내"))
            assertTrue(privacyCopy.contains("기기 안"))
            assertTrue(
                privacyCopy.contains("문자·통화·연락처") &&
                    privacyCopy.contains("자동으로 읽거나 감시하지 않습니다")
            )
            assertTrue(privacyCopy.contains("네트워크로 업로드하지 않습니다"))
            assertTrue(privacyCopy.contains("버튼 선택") && privacyCopy.contains("저장하지 않"))
            assertTrue(privacyCopy.contains("공유 이력") && privacyCopy.contains("저장하지 않"))
            assertTrue(privacyCopy.contains("체크 상태") && privacyCopy.contains("저장하지 않"))
            assertTrue(
                privacyCopy.contains("사건 기록") &&
                    privacyCopy.contains("추가·수정·삭제하지 않습니다")
            )
            assertTrue(privacyCopy.contains("연락처를 읽거나") && privacyCopy.contains("자동으로 보내지 않습니다"))
            assertTrue(privacyCopy.contains("새 Android 권한") && privacyCopy.contains("요청하지 않습니다"))
            assertTrue(privacyCopy.contains("통화·문자를 차단") && privacyCopy.contains("보장하지 않습니다"))
            assertTrue(privacyCopy.contains("악성 앱을 제거"))
            assertTrue(privacyCopy.contains("계좌·카드를 정지"))
            assertTrue(privacyCopy.contains("피해금 회수"))
            assertTrue(privacyCopy.contains("법적 결과") && privacyCopy.contains("보장하는 기능이 아니"))
        }
    }

    private fun analysisWith(vararg signals: ScamSignal): ScamAnalysis =
        analysisWithRisk(RiskLevel.MEDIUM, *signals)

    private fun analysisWithRisk(
        riskLevel: RiskLevel,
        vararg signals: ScamSignal
    ): ScamAnalysis = ScamAnalysis(
        input = "테스트 입력 ${signals.joinToString { signal -> signal.name }}",
        score = if (riskLevel == RiskLevel.HIGH) 100 else 50,
        riskLevel = riskLevel,
        matchedSignals = signals.toList()
    )

    private fun lowReadiness(): FamilyReadiness = FamilyReadinessBuilder.build(
        SafetyPlan(safeWord = "", callbackScript = "", contacts = emptyList())
    )

    private fun readyReadiness(): FamilyReadiness = FamilyReadinessBuilder.build(
        SafetyPlan(
            safeWord = "우리 가족만 아는 질문",
            callbackScript = "통화를 끊고 저장된 번호로 다시 전화한다.",
            contacts = listOf(
                TrustedContact("가족", "010-1111-2222"),
                TrustedContact("예비 가족", "010-3333-4444")
            )
        )
    )

    private fun allCopy(mode: QuickStopMode): String = buildString {
        append(mode.title)
        append(mode.intro)
        append(mode.stopScriptTitle)
        mode.stopScriptSteps.forEach { step -> append(step.instruction) }
        append(mode.recommendationTitle)
        append(mode.recommendationExplanation)
        append(mode.readinessMessage)
        mode.cards.forEach { card ->
            append(card.urgency.label)
            append(card.label)
            append(card.explanation)
            append(card.primaryAction.label)
            card.secondaryActions.forEach { action -> append(action.label) }
            card.safetyNotes.forEach(::append)
        }
        mode.privacySafetyNotes.forEach(::append)
        append(mode.familyShareSummary)
    }
}
