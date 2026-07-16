package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyCallbackCardBuilderTest {
    private val emptyPlan = SafetyPlan(safeWord = "", callbackScript = "", contacts = emptyList())

    @Test
    fun highRiskFamilyNumberChangeDeepfakeMoneyAndOtpStopsConversationBeforeSavedNumberCallback() {
        val analysis = ScamAnalyzer.analyze(
            "엄마 나 번호 바뀌었어. 휴대폰이 고장 났고 목소리 복제처럼 들려도 진짜 딸이야. " +
                "지금 바로 계좌로 송금하고 OTP 482913을 알려줘."
        )
        val plan = completePlan()

        val card = FamilyCallbackCardBuilder.build(analysis, plan)

        assertEquals(RiskLevel.HIGH, analysis.riskLevel)
        assertTrue(ScamSignal.FAMILY_EMERGENCY in analysis.matchedSignals)
        assertTrue(ScamSignal.PHONE_NUMBER_CHANGE in analysis.matchedSignals)
        assertTrue(ScamSignal.DEEPFAKE_VOICE in analysis.matchedSignals)
        assertTrue(ScamSignal.URGENT_TRANSFER in analysis.matchedSignals)
        assertTrue(ScamSignal.CREDENTIALS in analysis.matchedSignals)
        assertTrue(card.title.contains("가족") && card.title.contains("콜백"))
        assertTrue(card.riskSummary.contains(RiskLevel.HIGH.label))
        assertEquals(FamilyReadinessBuilder.build(plan), card.readiness)
        assertTrue(card.safeWordPrompt.contains(plan.safeWord))
        assertEquals(plan.contacts, card.dialableContacts)

        val stopIndex = card.callbackSteps.indexOfFirst { step ->
            (step.contains("대화") || step.contains("통화")) &&
                (step.contains("멈추") || step.contains("끊"))
        }
        val savedCallbackIndex = card.callbackSteps.indexOfFirst(::isSavedNumberCallback)

        assertEquals("high-risk routine must stop the current conversation first", 0, stopIndex)
        assertTrue("saved-number callback step was missing", savedCallbackIndex >= 0)
        assertTrue("callback must happen after stopping the current conversation", stopIndex < savedCallbackIndex)
    }

    @Test
    fun lowRiskAndNoAnalysisStillPutSavedNumberCallbackBeforeMoneyAppAndOtpGuardrails() {
        val plan = completePlan()
        val lowAnalysis = ScamAnalyzer.analyze("오늘 저녁 7시에 집에서 밥 먹자. 오는 길에 우유 하나만 사와.")
        val cards = listOf(
            FamilyCallbackCardBuilder.build(lowAnalysis, plan),
            FamilyCallbackCardBuilder.build(null, plan)
        )

        assertEquals(RiskLevel.SAFE, lowAnalysis.riskLevel)
        cards.forEach { card ->
            assertTrue("saved-number callback must be the first low/empty-state action", isSavedNumberCallback(card.callbackSteps.first()))

            val guardrailCopy = card.guardrails.joinToString(" ")
            assertTrue(guardrailCopy.contains("송금") || guardrailCopy.contains("돈"))
            assertTrue(guardrailCopy.contains("앱"))
            assertTrue(guardrailCopy.contains("OTP") || guardrailCopy.contains("인증번호"))
            assertEquals(FamilyReadinessBuilder.build(plan), card.readiness)
        }

        val emptyState = cards.last()
        assertNull(emptyState.redactedClue)
        assertTrue(emptyState.riskSummary.contains("분석"))
    }

    @Test
    fun missingOrBlankNumberSafetyPlanShowsSetupGuidanceAndNoDialableContacts() {
        val plans = listOf(
            emptyPlan,
            SafetyPlan(
                safeWord = "",
                callbackScript = "",
                contacts = listOf(TrustedContact("엄마", "   "))
            )
        )

        plans.forEach { plan ->
            val card = FamilyCallbackCardBuilder.build(null, plan)
            val setupCopy = buildString {
                append(card.riskSummary)
                append(card.safeWordPrompt)
                card.callbackSteps.forEach(::append)
                card.privacySafetyNotes.forEach(::append)
            }

            assertTrue(card.needsSafetyPlanSetup)
            assertTrue(card.dialableContacts.isEmpty())
            assertEquals(FamilyReadinessBuilder.build(plan), card.readiness)
            assertTrue(setupCopy.contains("가족 안전 계획"))
            assertTrue(setupCopy.contains("설정") || setupCopy.contains("저장"))
            assertTrue(card.safeWordPrompt.contains("안전"))
            assertTrue(card.safeWordPrompt.contains("설정") || card.safeWordPrompt.contains("저장"))
        }
    }

    @Test
    fun savedPlanExposesOrderedDialableScreenContactsButShareOmitsEveryUserEnteredPlanValue() {
        val contacts = listOf(
            TrustedContact("보호자-푸른별", "010-2468-1357"),
            TrustedContact("예비-은하수", "010-9753-8642"),
            TrustedContact("번호없음-숨김", "  ")
        )
        val plan = SafetyPlan(
            safeWord = "해질녘 파란 우산은 어디에?",
            callbackScript = "푸른 메모장을 열고 첫 번째 저장 번호로 전화한다.",
            contacts = contacts
        )

        val card = FamilyCallbackCardBuilder.build(null, plan)
        val summary = card.familyShareSummary

        assertEquals(contacts.take(2), card.dialableContacts)
        assertTrue(card.safeWordPrompt.contains(plan.safeWord))
        assertFalse(card.needsSafetyPlanSetup)
        assertTrue(summary.contains("저장된") && (summary.contains("가족") || summary.contains("연락처")))
        assertFalse(summary.contains(plan.safeWord))
        assertFalse(summary.contains(plan.callbackScript))
        contacts.forEach { contact ->
            assertFalse("share leaked contact label: ${contact.label}", summary.contains(contact.label))
            if (contact.number.isNotBlank()) {
                assertFalse("share leaked full trusted number: ${contact.number}", summary.contains(contact.number))
            }
        }
    }

    @Test
    fun privacySafeClueMasksSensitivePatternsWhileShareOmitsRawTextAndClue() {
        val raw =
            "010-1234-5678 123456789012 OTP A1B2C3 https://evil.example 주민번호 900101-1234567"
        val analysis = ScamAnalyzer.analyze(raw)

        val card = FamilyCallbackCardBuilder.build(analysis, completePlan())
        val clue = requireNotNull(card.redactedClue)

        assertEquals(IncidentPreviewRedactor.buildPrivacySafeClue(analysis), clue)
        listOf(
            "[전화번호 숨김]",
            "[긴 숫자 숨김]",
            "[인증번호 숨김]",
            "[링크 숨김]",
            "[신분정보 숨김]"
        ).forEach { mask -> assertTrue("redacted clue omitted $mask: $clue", clue.contains(mask)) }
        listOf(
            "010-1234-5678",
            "123456789012",
            "A1B2C3",
            "evil.example",
            "900101-1234567"
        ).forEach { secret ->
            assertFalse("redacted clue leaked $secret", clue.contains(secret))
            assertFalse("share leaked $secret", card.familyShareSummary.contains(secret))
        }
        assertFalse(clue.contains(raw))
        assertFalse(card.familyShareSummary.contains(raw))
        assertFalse(card.familyShareSummary.contains(clue))
    }

    @Test
    fun outputIsDeterministicForTheSameAnalysisAndPlan() {
        val analysis = ScamAnalyzer.analyze(
            "엄마 나 번호 바뀌었어. 이 번호로 저장하고 지금 바로 계좌로 송금해줘."
        )
        val plan = completePlan()

        val first = FamilyCallbackCardBuilder.build(analysis, plan)
        val second = FamilyCallbackCardBuilder.build(analysis, plan)

        assertEquals(first, second)
        assertEquals(first.callbackSteps, second.callbackSteps)
        assertEquals(first.dialableContacts, second.dialableContacts)
        assertEquals(first.familyShareSummary, second.familyShareSummary)
        assertEquals(FamilyReadinessBuilder.build(plan), first.readiness)
    }

    @Test
    fun copyStatesLocalManualPermissionAndStorageLimitsWithoutUnsafeClaims() {
        val plan = completePlan()
        val cards = listOf(
            FamilyCallbackCardBuilder.build(null, emptyPlan),
            FamilyCallbackCardBuilder.build(
                ScamAnalyzer.analyze("오늘 저녁에 통화하자."),
                plan
            ),
            FamilyCallbackCardBuilder.build(
                ScamAnalyzer.analyze("엄마 나 번호 바뀌었어. 지금 바로 계좌로 송금하고 OTP 482913을 알려줘."),
                plan
            )
        )
        val forbiddenAffirmativeClaims = listOf(
            "자동으로 감시합니다",
            "자동 모니터링합니다",
            "몰래 확인합니다",
            "자동으로 읽습니다",
            "자동으로 수집합니다",
            "연락처를 자동으로 읽습니다",
            "연락처 앱에서 자동으로 가져옵니다",
            "연락처 권한을 요청합니다",
            "연락처 권한을 사용합니다",
            "연락처 권한이 필요합니다",
            "서버로 업로드합니다",
            "네트워크로 업로드합니다",
            "네트워크로 전송합니다",
            "법적 효력을 보장합니다",
            "법적 결과를 보장합니다",
            "피해 회복을 보장합니다",
            "피해금 회수를 보장합니다",
            "반드시 환급됩니다",
            "반드시 회수됩니다",
            "100% 탐지합니다",
            "automatic monitoring",
            "reads contacts automatically",
            "server upload",
            "legal guarantee",
            "guaranteed recovery"
        )

        cards.forEach { card ->
            val allCopy = allCopy(card)
            val privacyCopy = card.privacySafetyNotes.joinToString(" ")

            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "callback-card copy contained unsafe claim: $claim",
                    allCopy.lowercase().contains(claim.lowercase())
                )
            }
            assertTrue(privacyCopy.contains("기기 안") || privacyCopy.contains("기기 내"))
            assertTrue(privacyCopy.contains("직접 입력") && privacyCopy.contains("연락처"))
            assertTrue(
                privacyCopy.contains("연락처 권한") &&
                    (privacyCopy.contains("요청하지 않") || privacyCopy.contains("사용하지 않"))
            )
            assertTrue(privacyCopy.contains("네트워크") && privacyCopy.contains("않"))
            assertTrue(
                privacyCopy.contains("따로 저장하지 않") ||
                    (privacyCopy.contains("카드") && privacyCopy.contains("저장하지 않"))
            )
            assertTrue(
                privacyCopy.contains("사건 기록") &&
                    (privacyCopy.contains("추가하거나 바꾸지 않") ||
                        privacyCopy.contains("추가·수정하지 않") ||
                        (privacyCopy.contains("자동") && privacyCopy.contains("않")))
            )
        }
    }

    private fun completePlan(): SafetyPlan = SafetyPlan(
        safeWord = "우리 가족의 파란 자전거 이름은?",
        callbackScript = "현재 통화를 끊고 저장된 가족 번호로 다시 전화한다.",
        contacts = listOf(
            TrustedContact("엄마", "010-1234-5678"),
            TrustedContact("아빠", "010-2345-6789")
        )
    )

    private fun isSavedNumberCallback(step: String): Boolean =
        step.contains("저장된") &&
            (step.contains("번호") || step.contains("연락처")) &&
            (step.contains("전화") || step.contains("콜백"))

    private fun allCopy(card: FamilyCallbackCard): String = buildString {
        append(card.title)
        append(card.riskSummary)
        append(card.redactedClue.orEmpty())
        append(card.readiness.title)
        append(card.readiness.nextAction)
        card.readiness.checklist.forEach { item ->
            append(item.label)
            append(item.action)
        }
        append(card.safeWordPrompt)
        card.callbackSteps.forEach(::append)
        card.dialableContacts.forEach { contact ->
            append(contact.label)
            append(contact.number)
        }
        card.guardrails.forEach(::append)
        card.privacySafetyNotes.forEach(::append)
        append(card.familyShareSummary)
    }
}
