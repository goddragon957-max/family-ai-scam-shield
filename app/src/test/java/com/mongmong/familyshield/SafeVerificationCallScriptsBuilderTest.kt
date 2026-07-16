package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeVerificationCallScriptsBuilderTest {
    @Test
    fun noAnalysisHasStableCardOrderAndConservativeDefaultRecommendations() {
        val scripts = SafeVerificationCallScriptsBuilder.build(analysis = null, readiness = readiness())

        assertEquals("분석 전 기본 추천", scripts.recommendationTitle)
        assertEquals(
            listOf(
                SafeCallScriptId.FAMILY_EXISTING_NUMBER,
                SafeCallScriptId.BANK_CARD_VERIFICATION,
                SafeCallScriptId.OFFICIAL_HOTLINE,
                SafeCallScriptId.CARRIER_ACCOUNT_SUPPORT,
                SafeCallScriptId.DEVICE_REMOTE_CONTROL_HELP,
                SafeCallScriptId.EVIDENCE_REPORT_PREP,
                SafeCallScriptId.PREVENTION_CHECKIN
            ),
            scripts.cards.map { card -> card.id }
        )
        assertEquals((1..7).toList(), scripts.cards.map { card -> card.displayOrder })
        assertEquals(
            listOf(SafeCallScriptId.FAMILY_EXISTING_NUMBER, SafeCallScriptId.PREVENTION_CHECKIN),
            scripts.recommendedScriptIds
        )
        assertEquals(scripts.recommendedScriptIds, scripts.recommendedCards.map { card -> card.id })
        listOf(
            SafeCallScriptActionId.FAMILY_CALLBACK_CARD,
            SafeCallScriptActionId.CHECKER,
            SafeCallScriptActionId.SAFETY_PLAN,
            SafeCallScriptActionId.RECOVERY_PLAN,
            SafeCallScriptActionId.SAFETY_FLOW_ROUTER,
            SafeCallScriptActionId.FAMILY_ALERT_MESSAGES
        ).forEach { actionId ->
            assertTrue("default guidance omitted $actionId", actionId in scripts.recommendedActionIds)
        }
        listOf(
            SafeCallScriptActionId.RESPONSE_CENTER,
            SafeCallScriptActionId.ACCOUNT_LOCKDOWN,
            SafeCallScriptActionId.EVIDENCE_KIT,
            SafeCallScriptActionId.ANALYSIS_RESULT,
            SafeCallScriptActionId.OFFICIAL_HELP_DIRECTORY,
            SafeCallScriptActionId.REPORT_PREP,
            SafeCallScriptActionId.TRUSTED_HELPER_HANDOFF
        ).forEach { actionId ->
            assertFalse(
                "default guidance should not include not-yet-relevant $actionId",
                actionId in scripts.recommendedActionIds
            )
        }
        assertEquals(SafeCallScriptId.entries.size, scripts.cards.size)
        assertEquals(scripts.cards.size, scripts.cards.map { card -> card.id }.distinct().size)
    }

    @Test
    fun familyDeepfakeAndNumberChangeSignalsRecommendFamilyExistingNumberCallEarly() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val scripts = SafeVerificationCallScriptsBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                "$signal recommended order",
                listOf(SafeCallScriptId.FAMILY_EXISTING_NUMBER),
                scripts.recommendedScriptIds
            )
            listOf(
                SafeCallScriptActionId.FAMILY_CALLBACK_CARD,
                SafeCallScriptActionId.SAFETY_PLAN,
                SafeCallScriptActionId.FAMILY_ALERT_MESSAGES
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in scripts.recommendedActionIds)
            }
        }
    }

    @Test
    fun moneyCredentialSafeAccountGiftAndCryptoSignalsRecommendBankCardAndOfficialHotline() {
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
            val scripts = SafeVerificationCallScriptsBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                "$signal recommended cards",
                listOf(SafeCallScriptId.BANK_CARD_VERIFICATION, SafeCallScriptId.OFFICIAL_HOTLINE),
                scripts.recommendedScriptIds
            )
            listOf(
                SafeCallScriptActionId.OFFICIAL_HELP_DIRECTORY,
                SafeCallScriptActionId.ACCOUNT_LOCKDOWN,
                SafeCallScriptActionId.REPORT_PREP,
                SafeCallScriptActionId.RESPONSE_CENTER
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in scripts.recommendedActionIds)
            }
        }
    }

    @Test
    fun remoteControlAppInstallAndScreenShareRecommendDeviceHelpAndEvidenceWithoutMalwareRemovalGuarantee() {
        listOf(
            ScamSignal.APP_INSTALL,
            ScamSignal.LIVE_CALL_CONTROL
        ).forEach { signal ->
            val scripts = SafeVerificationCallScriptsBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                "$signal recommended cards",
                listOf(SafeCallScriptId.DEVICE_REMOTE_CONTROL_HELP, SafeCallScriptId.EVIDENCE_REPORT_PREP),
                scripts.recommendedScriptIds
            )
            listOf(
                SafeCallScriptActionId.TRUSTED_HELPER_HANDOFF,
                SafeCallScriptActionId.ACCOUNT_LOCKDOWN,
                SafeCallScriptActionId.EVIDENCE_KIT
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in scripts.recommendedActionIds)
            }
            val copy = allCopy(scripts)
            assertFalse(copy.contains("악성 앱을 자동으로 제거합니다"))
            assertFalse(copy.contains("악성코드 제거를 보장합니다"))
        }
    }

    @Test
    fun linkAndSmishingSignalsRecommendEvidenceReportAndOfficialHotlineWithoutAutoScanClaim() {
        listOf(
            ScamSignal.LINK,
            ScamSignal.PARCEL_INVITATION_SMISHING
        ).forEach { signal ->
            val scripts = SafeVerificationCallScriptsBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                "$signal recommended cards",
                listOf(SafeCallScriptId.EVIDENCE_REPORT_PREP, SafeCallScriptId.OFFICIAL_HOTLINE),
                scripts.recommendedScriptIds
            )
            listOf(
                SafeCallScriptActionId.EVIDENCE_KIT,
                SafeCallScriptActionId.OFFICIAL_HELP_DIRECTORY,
                SafeCallScriptActionId.FAMILY_ALERT_MESSAGES,
                SafeCallScriptActionId.ANALYSIS_RESULT
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in scripts.recommendedActionIds)
            }

            val copy = allCopy(scripts)
            assertFalse(copy.contains("자동으로 검사합니다"))
            assertFalse(copy.contains("자동 스캔합니다"))
        }
    }

    @Test
    fun lowReadinessRecommendsFamilyExistingNumberCallAndReadyReadinessCopyDiffersCalmly() {
        val lowReadinessScripts = SafeVerificationCallScriptsBuilder.build(
            analysis = analysisWith(ScamSignal.APP_INSTALL),
            readiness = readiness(ready = false)
        )
        assertTrue(SafeCallScriptId.FAMILY_EXISTING_NUMBER in lowReadinessScripts.recommendedScriptIds)
        assertTrue(lowReadinessScripts.recommendationExplanation.contains("준비가 부족"))

        val readyReadinessScripts = SafeVerificationCallScriptsBuilder.build(
            analysis = analysisWith(ScamSignal.APP_INSTALL),
            readiness = readiness(ready = true)
        )
        assertFalse(SafeCallScriptId.FAMILY_EXISTING_NUMBER in readyReadinessScripts.recommendedScriptIds)
        assertTrue(readyReadinessScripts.recommendationExplanation.contains("준비되어 있으니"))
        assertTrue(
            lowReadinessScripts.recommendationExplanation != readyReadinessScripts.recommendationExplanation
        )
    }

    @Test
    fun everyScriptCardHasCompleteFieldsAndStableIdOrder() {
        listOf(
            SafeVerificationCallScriptsBuilder.build(analysis = null, readiness = readiness()),
            SafeVerificationCallScriptsBuilder.build(
                analysis = analysisWith(
                    ScamSignal.APP_INSTALL,
                    ScamSignal.CREDENTIALS,
                    ScamSignal.LINK,
                    ScamSignal.PHONE_NUMBER_CHANGE
                ),
                readiness = readiness(ready = true)
            )
        ).forEach { scripts ->
            assertTrue(scripts.title.isNotBlank())
            assertTrue(scripts.intro.isNotBlank())
            assertTrue(scripts.recommendationTitle.isNotBlank())
            assertTrue(scripts.recommendationExplanation.isNotBlank())
            assertTrue(scripts.privacySafetyNotes.isNotEmpty())
            assertTrue(scripts.privacySafetyNotes.all { note -> note.isNotBlank() })
            assertTrue(
                scripts.recommendedScriptIds.all { scriptId ->
                    scripts.cards.any { card -> card.id == scriptId }
                }
            )

            scripts.cards.forEach { card ->
                assertTrue(card.displayOrder > 0)
                assertTrue(card.urgency.rank >= 0)
                assertTrue(card.urgency.label.isNotBlank())
                assertTrue(card.label.isNotBlank())
                assertTrue(card.whoToCall.isNotBlank())
                assertTrue(card.whenToUse.isNotBlank())
                assertTrue(card.openingScript.isNotBlank())
                assertTrue(card.questionsToAsk.isNotEmpty())
                assertTrue(card.questionsToAsk.all { question -> question.isNotBlank() })
                assertTrue(card.neverSayNotes.isNotEmpty())
                assertTrue(card.neverSayNotes.all { note -> note.isNotBlank() })
                assertTrue(card.primaryActionLabel.isNotBlank())
                assertTrue(card.secondaryActionLabels.all { label -> label.isNotBlank() })
                assertEquals(
                    card.secondaryActions.map { action -> action.label },
                    card.secondaryActionLabels
                )
            }
        }
    }

    @Test
    fun shareContainsScriptAndActionLabelsButExcludesSensitiveValues() {
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
        val scripts = SafeVerificationCallScriptsBuilder.build(analysis = analysis, readiness = readiness(ready = true))
        val message = scripts.familyShareSummary
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)

        scripts.cards.forEach { card ->
            assertTrue("share omitted card: ${card.label}", message.contains(card.label))
            (listOf(card.primaryAction) + card.secondaryActions).forEach { action ->
                assertTrue("share omitted action: ${action.label}", message.contains(action.label))
            }
        }
        assertTrue(message.contains("전화 스크립트"))
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
            assertFalse("call script share leaked secret: $secret", message.contains(secret))
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

        val defaultScripts = SafeVerificationCallScriptsBuilder.build(analysis = null, readiness = readiness())
        val defaultMessage = defaultScripts.familyShareSummary
        defaultScripts.cards.forEach { card ->
            assertTrue(defaultMessage.contains(card.label))
            (listOf(card.primaryAction) + card.secondaryActions).forEach { action ->
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

        val first = SafeVerificationCallScriptsBuilder.build(analysis, readinessValue)
        val second = SafeVerificationCallScriptsBuilder.build(analysis, readinessValue)

        assertEquals(first, second)
        assertEquals(first.cards, second.cards)
        assertEquals(first.recommendedScriptIds, second.recommendedScriptIds)
        assertEquals(first.recommendedActionIds, second.recommendedActionIds)
        assertEquals(first.familyShareSummary, second.familyShareSummary)
    }

    @Test
    fun copyStatesManualNoStorageNoContactReadingNoAutoSendNoAutoDialNoMonitoringNoNetwork() {
        val scriptsList = listOf(
            SafeVerificationCallScriptsBuilder.build(analysis = null, readiness = readiness()),
            SafeVerificationCallScriptsBuilder.build(analysis = analysisWith(ScamSignal.APP_INSTALL), readiness = readiness()),
            SafeVerificationCallScriptsBuilder.build(
                analysis = analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER),
                readiness = readiness(ready = true)
            ),
            SafeVerificationCallScriptsBuilder.build(analysis = analysisWith(ScamSignal.LINK), readiness = readiness())
        )

        scriptsList.forEach { scripts ->
            val privacyCopy = scripts.privacySafetyNotes.joinToString(" ")
            assertTrue(privacyCopy.contains("수동 안내"))
            assertTrue(privacyCopy.contains("기기 안"))
            assertTrue(privacyCopy.contains("사용자가 직접"))
            assertTrue(privacyCopy.contains("저장하지 않"))
            assertTrue(privacyCopy.contains("연락처를 읽거나 받는 사람을 고르지 않"))
            assertTrue(privacyCopy.contains("자동으로 보내지 않"))
            assertTrue(privacyCopy.contains("자동으로 읽거나 감시하지 않"))
            assertTrue(privacyCopy.contains("자동으로 걸지 않"))
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
        val scriptsList = listOf(
            SafeVerificationCallScriptsBuilder.build(analysis = null, readiness = readiness()),
            SafeVerificationCallScriptsBuilder.build(analysis = analysisWith(ScamSignal.APP_INSTALL), readiness = readiness()),
            SafeVerificationCallScriptsBuilder.build(
                analysis = analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER),
                readiness = readiness(ready = true)
            ),
            SafeVerificationCallScriptsBuilder.build(analysis = analysisWith(ScamSignal.LINK), readiness = readiness())
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
            "전화를 자동으로 겁니다",
            "통화를 자동으로 시작합니다",
            "문자를 자동으로 차단합니다",
            "전화를 자동으로 차단합니다",
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

        scriptsList.forEach { scripts ->
            val copy = allCopy(scripts)
            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "safe verification call script copy contained unsafe claim: $claim",
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

    private fun allCopy(scripts: SafeVerificationCallScripts): String = buildString {
        append(scripts.title)
        append(scripts.intro)
        append(scripts.recommendationTitle)
        append(scripts.recommendationExplanation)
        scripts.cards.forEach { card ->
            append(card.urgency.label)
            append(card.label)
            append(card.whoToCall)
            append(card.whenToUse)
            append(card.openingScript)
            card.questionsToAsk.forEach(::append)
            card.neverSayNotes.forEach(::append)
            append(card.primaryActionLabel)
            card.secondaryActionLabels.forEach(::append)
        }
        scripts.privacySafetyNotes.forEach(::append)
        append(scripts.familyShareSummary)
    }
}
