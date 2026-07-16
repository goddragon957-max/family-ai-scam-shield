package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTransferPauseGateBuilderTest {
    @Test
    fun noAnalysisGateIsConservativeAndNeverDeclaresPaymentOrContactSafe() {
        val gate = MoneyTransferPauseGateBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(MoneyTransferPauseCardId.entries, gate.recommendedCardIds)
        assertTrue(
            gate.recommendedActionIds.containsAll(
                listOf(
                    MoneyTransferPauseActionId.QUICK_STOP,
                    MoneyTransferPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                    MoneyTransferPauseActionId.CHECKER,
                    MoneyTransferPauseActionId.FAMILY_CALLBACK_CARD,
                    MoneyTransferPauseActionId.ACCOUNT_LOCKDOWN,
                    MoneyTransferPauseActionId.EVIDENCE_KIT,
                    MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY,
                    MoneyTransferPauseActionId.SAFETY_FLOW_ROUTER
                )
            )
        )
        assertTrue(gate.recommendationExplanation.contains("현재 분석이 없거나 일치 신호가 없습니다"))
        assertTrue(gate.recommendationExplanation.contains("결제해도 된다는 판정으로 보지 말고"))

        val copy = allCopy(gate)
        listOf(
            "안전합니다",
            "문제없습니다",
            "사기가 아닙니다",
            "이 연락은 안전",
            "결제해도 됩니다",
            "100%",
            " safe ",
            "blocked"
        ).forEach { unsafeClaim ->
            assertFalse(
                "default gate made unsafe claim: $unsafeClaim",
                copy.contains(unsafeClaim, ignoreCase = true)
            )
        }
    }

    @Test
    fun highRiskAndUrgentTransferPrioritizePauseCredentialsAndResponseRoutes() {
        listOf(
            analysis(RiskLevel.HIGH),
            analysis(RiskLevel.MEDIUM, ScamSignal.URGENT_TRANSFER),
            analysis(
                RiskLevel.HIGH,
                ScamSignal.URGENT_TRANSFER,
                ScamSignal.CREDENTIALS,
                ScamSignal.SAFE_ACCOUNT_FRAUD
            )
        ).forEach { current ->
            val gate = MoneyTransferPauseGateBuilder.build(
                analysis = current,
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(MoneyTransferPauseCardId.PAYMENT_PAUSE in gate.recommendedCardIds)
            assertTrue(
                MoneyTransferPauseCardId.FINANCIAL_CREDENTIALS_REFUSAL in
                    gate.recommendedCardIds
            )
            assertTrue(
                MoneyTransferPauseCardId.EVIDENCE_AND_OFFICIAL_HELP in
                    gate.recommendedCardIds
            )
            assertTrue(
                gate.recommendedActionIds.containsAll(
                    listOf(
                        MoneyTransferPauseActionId.QUICK_STOP,
                        MoneyTransferPauseActionId.ACCOUNT_LOCKDOWN,
                        MoneyTransferPauseActionId.EVIDENCE_KIT,
                        MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY,
                        MoneyTransferPauseActionId.REPORT_PREP,
                        MoneyTransferPauseActionId.RECOVERY_PLAN,
                        MoneyTransferPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER
                    )
                )
            )
            assertTrue(gate.recommendationExplanation.contains("계정 보호·증거·공식 도움·후속 점검"))
            assertTrue(
                gate.card(MoneyTransferPauseCardId.PAYMENT_PAUSE).pauseSentence
                    .contains("별도 확인이 끝날 때까지")
            )
        }
    }

    @Test
    fun credentialAndOtpSignalsRefuseCodesAndUseManualAccountProtection() {
        val gate = MoneyTransferPauseGateBuilder.build(
            analysis = analysis(RiskLevel.MEDIUM, ScamSignal.CREDENTIALS),
            readiness = readiness(ReadinessLevel.READY)
        )

        assertTrue(
            MoneyTransferPauseCardId.FINANCIAL_CREDENTIALS_REFUSAL in gate.recommendedCardIds
        )
        assertTrue(
            gate.recommendedActionIds.containsAll(
                listOf(
                    MoneyTransferPauseActionId.ACCOUNT_LOCKDOWN,
                    MoneyTransferPauseActionId.EVIDENCE_KIT,
                    MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY
                )
            )
        )
        val card = gate.card(MoneyTransferPauseCardId.FINANCIAL_CREDENTIALS_REFUSAL)
        assertTrue(card.pauseSentence.contains("인증번호나 PIN·비밀번호를 말하거나 보내지 않고"))
        assertTrue(card.pauseSentence.contains("계좌·카드 정보도 입력하거나 보여주지 않겠습니다"))
        assertEquals(MoneyTransferPauseActionId.ACCOUNT_LOCKDOWN, card.primaryAction.id)

        val copy = allCopy(gate)
        assertFalse(copy.contains("계정을 자동으로 정지합니다"))
        assertFalse(copy.contains("카드를 자동으로 동결합니다"))
        assertFalse(copy.contains("계정 동결 완료"))
    }

    @Test
    fun safeAccountLoanAndRefundFeeSignalsRecommendNarrativePauseAndOfficialRoutes() {
        listOf(
            ScamSignal.SAFE_ACCOUNT_FRAUD to "안전계좌",
            ScamSignal.BAIT to "환급·대출",
            ScamSignal.LOAN_REFINANCE_FEE to "보증료"
        ).forEach { (signal, expectedCopy) ->
            val gate = MoneyTransferPauseGateBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(MoneyTransferPauseCardId.PAYMENT_PAUSE in gate.recommendedCardIds)
            assertTrue(
                MoneyTransferPauseCardId.SAFE_ACCOUNT_REFUND_LOAN_FEE in
                    gate.recommendedCardIds
            )
            assertTrue(
                gate.recommendedActionIds.containsAll(
                    listOf(
                        MoneyTransferPauseActionId.ACCOUNT_LOCKDOWN,
                        MoneyTransferPauseActionId.EVIDENCE_KIT,
                        MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY,
                        MoneyTransferPauseActionId.REPORT_PREP
                    )
                )
            )
            assertTrue(gate.recommendationExplanation.contains(expectedCopy))
            val card = gate.card(MoneyTransferPauseCardId.SAFE_ACCOUNT_REFUND_LOAN_FEE)
            assertTrue(card.pauseSentence.contains("돈을 보내지 않겠습니다"))
            assertTrue(card.pauseSentence.contains("직접 찾은 공식 경로"))
        }
    }

    @Test
    fun giftCardGifticonAndCryptoSignalsRecommendDedicatedPauseCards() {
        val giftCrypto = MoneyTransferPauseGateBuilder.build(
            analysis = analysis(RiskLevel.MEDIUM, ScamSignal.GIFT_CRYPTO),
            readiness = readiness(ReadinessLevel.READY)
        )
        val gifticon = MoneyTransferPauseGateBuilder.build(
            analysis = analysis(RiskLevel.MEDIUM, ScamSignal.GIFTICON_PIN),
            readiness = readiness(ReadinessLevel.READY)
        )

        listOf(giftCrypto, gifticon).forEach { gate ->
            assertTrue(MoneyTransferPauseCardId.PAYMENT_PAUSE in gate.recommendedCardIds)
            assertTrue(MoneyTransferPauseCardId.GIFT_CARD_CRYPTO_PAUSE in gate.recommendedCardIds)
            assertTrue(
                gate.recommendedActionIds.containsAll(
                    listOf(
                        MoneyTransferPauseActionId.ACCOUNT_LOCKDOWN,
                        MoneyTransferPauseActionId.EVIDENCE_KIT,
                        MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY
                    )
                )
            )
            val card = gate.card(MoneyTransferPauseCardId.GIFT_CARD_CRYPTO_PAUSE)
            assertTrue(card.pauseSentence.contains("상품권·기프티콘"))
            assertTrue(card.pauseSentence.contains("가상자산도 전송하지 않겠습니다"))
        }
        assertFalse(
            MoneyTransferPauseCardId.FINANCIAL_CREDENTIALS_REFUSAL in
                giftCrypto.recommendedCardIds
        )
        assertTrue(
            MoneyTransferPauseCardId.FINANCIAL_CREDENTIALS_REFUSAL in
                gifticon.recommendedCardIds
        )
    }

    @Test
    fun overseasPaymentAlertPausesPaymentCredentialsAndUsesSeparateOfficialCheck() {
        val gate = MoneyTransferPauseGateBuilder.build(
            analysis = analysis(RiskLevel.MEDIUM, ScamSignal.OVERSEAS_PAYMENT_ALERT),
            readiness = readiness(ReadinessLevel.READY)
        )

        assertTrue(
            gate.recommendedCardIds.containsAll(
                listOf(
                    MoneyTransferPauseCardId.PAYMENT_PAUSE,
                    MoneyTransferPauseCardId.FINANCIAL_CREDENTIALS_REFUSAL,
                    MoneyTransferPauseCardId.SEPARATE_FAMILY_OFFICIAL_VERIFICATION,
                    MoneyTransferPauseCardId.EVIDENCE_AND_OFFICIAL_HELP
                )
            )
        )
        assertTrue(gate.recommendationExplanation.contains("해외·소액 결제 알림"))
        assertTrue(gate.recommendationExplanation.contains("알림에 포함된 경로를 쓰지 않고"))
        assertTrue(
            gate.recommendedActionIds.containsAll(
                listOf(
                    MoneyTransferPauseActionId.ACCOUNT_LOCKDOWN,
                    MoneyTransferPauseActionId.EVIDENCE_KIT,
                    MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY
                )
            )
        )
    }

    @Test
    fun familyDeepfakeAndNumberChangeRequireCallbackBeforeMoneyMovement() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val gate = MoneyTransferPauseGateBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                MoneyTransferPauseCardId.SEPARATE_FAMILY_OFFICIAL_VERIFICATION in
                    gate.recommendedCardIds
            )
            assertTrue(
                gate.recommendedActionIds.containsAll(
                    listOf(
                        MoneyTransferPauseActionId.FAMILY_CALLBACK_CARD,
                        MoneyTransferPauseActionId.SAFE_CALL_SCRIPTS,
                        MoneyTransferPauseActionId.FAMILY_ALERT_MESSAGES
                    )
                )
            )
            assertTrue(gate.recommendationExplanation.contains("콜백하기 전에는 돈을 움직이지 않도록"))
            val card = gate.card(
                MoneyTransferPauseCardId.SEPARATE_FAMILY_OFFICIAL_VERIFICATION
            )
            assertTrue(card.pauseSentence.contains("평소 알던 가족 경로"))
            assertTrue(card.doNotDoBoundaries.joinToString().contains("새 번호·현재 목소리"))
        }
    }

    @Test
    fun secrecyAndImpersonationRejectSecretPaymentAndUseOfficialRoute() {
        listOf(ScamSignal.SECRECY, ScamSignal.IMPERSONATION).forEach { signal ->
            val gate = MoneyTransferPauseGateBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(MoneyTransferPauseCardId.PAYMENT_PAUSE in gate.recommendedCardIds)
            assertTrue(
                MoneyTransferPauseCardId.EVIDENCE_AND_OFFICIAL_HELP in
                    gate.recommendedCardIds
            )
            assertTrue(
                gate.recommendedActionIds.containsAll(
                    listOf(
                        MoneyTransferPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                        MoneyTransferPauseActionId.FAMILY_ALERT_MESSAGES,
                        MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY
                    )
                )
            )
            assertTrue(gate.recommendationExplanation.contains("결제를 비밀로 하지 않고"))
            assertTrue(gate.recommendationExplanation.contains("직접 찾은 공식 경로"))
        }
    }

    @Test
    fun savedIncidentChangesRecommendationsWithoutReadingContentsAndClampsCounts() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.FAMILY_EMERGENCY)
        val ready = readiness(ReadinessLevel.READY)
        val zero = MoneyTransferPauseGateBuilder.build(current, ready, incidentCount = 0)
        val negative = MoneyTransferPauseGateBuilder.build(current, ready, incidentCount = -99)
        val one = MoneyTransferPauseGateBuilder.build(current, ready, incidentCount = 1)
        val many = MoneyTransferPauseGateBuilder.build(
            current,
            ready,
            incidentCount = Int.MAX_VALUE
        )

        assertEquals(zero, negative)
        assertEquals(one, many)
        assertNotEquals(zero, one)
        assertFalse(MoneyTransferPauseActionId.EVIDENCE_KIT in zero.recommendedActionIds)
        assertTrue(
            one.recommendedActionIds.containsAll(
                listOf(
                    MoneyTransferPauseActionId.EVIDENCE_KIT,
                    MoneyTransferPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER
                )
            )
        )
        assertTrue(one.recommendationExplanation.contains("개수 범주만 반영"))
        assertTrue(one.recommendationExplanation.contains("사건 내용은 읽지 않습니다"))
        assertFalse(one.familyShareSummary.contains(Int.MAX_VALUE.toString()))
    }

    @Test
    fun lowAndInProgressReadinessAddCallbackRoadmapAndFamilySupportWhileReadyIsCautious() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.LINK)
        val low = MoneyTransferPauseGateBuilder.build(
            current,
            readiness(ReadinessLevel.LOW)
        )
        val inProgress = MoneyTransferPauseGateBuilder.build(
            current,
            readiness(ReadinessLevel.IN_PROGRESS)
        )
        val ready = MoneyTransferPauseGateBuilder.build(
            current,
            readiness(ReadinessLevel.READY)
        )

        listOf(low, inProgress).forEach { gate ->
            assertTrue(
                gate.recommendedActionIds.containsAll(
                    listOf(
                        MoneyTransferPauseActionId.FAMILY_CALLBACK_CARD,
                        MoneyTransferPauseActionId.FAMILY_ALERT_MESSAGES,
                        MoneyTransferPauseActionId.TRUSTED_HELPER_REVIEW,
                        MoneyTransferPauseActionId.FAMILY_READINESS_ROADMAP
                    )
                )
            )
        }
        assertFalse(
            MoneyTransferPauseActionId.FAMILY_READINESS_ROADMAP in ready.recommendedActionIds
        )
        assertTrue(low.recommendationExplanation.contains("가족 준비가 부족한 상태"))
        assertTrue(inProgress.recommendationExplanation.contains("가족 준비가 진행 중"))
        assertTrue(ready.recommendationExplanation.contains("가족 준비가 되어 있어도"))
        assertTrue(ready.recommendationExplanation.contains("돈을 움직이지 말고"))
    }

    @Test
    fun everyCardHasCompleteFieldsStableOrderUniqueRoutesAndExistingActionIds() {
        val noAnalysis = MoneyTransferPauseGateBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )
        val analyzed = MoneyTransferPauseGateBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2
        )

        assertEquals(MoneyTransferPauseCardId.entries, noAnalysis.cards.map { it.id })
        assertEquals((1..MoneyTransferPauseCardId.entries.size).toList(), noAnalysis.cards.map { it.displayOrder })
        noAnalysis.cards.forEach { card ->
            assertEquals(card.id.displayOrder, card.displayOrder)
            assertTrue(card.title.isNotBlank())
            assertTrue(card.pauseSentence.isNotBlank())
            assertTrue(card.whyItMatters.isNotBlank())
            assertTrue(card.doNotDoBoundaries.isNotEmpty())
            assertTrue(card.doNotDoBoundaries.all { boundary -> boundary.isNotBlank() })
            assertTrue(card.primaryAction.label.isNotBlank())
            assertTrue(card.secondaryActions.isNotEmpty())
            assertEquals(
                card.routeActions.size,
                card.routeActions.map { action -> action.id }.distinct().size
            )
        }

        val mappedActionIds = (noAnalysis.cards + analyzed.cards)
            .flatMap { card -> card.routeActions }
            .map { action -> action.id }
            .toSet()
        assertEquals(MoneyTransferPauseActionId.entries.toSet(), mappedActionIds)
        listOf(noAnalysis, analyzed).forEach { gate ->
            assertEquals(
                gate.recommendedCardIds.sortedBy { id -> id.ordinal },
                gate.recommendedCardIds
            )
            assertEquals(
                gate.recommendedActionIds.sortedBy { id -> id.ordinal },
                gate.recommendedActionIds
            )
            assertEquals(gate.recommendedCardIds.size, gate.recommendedCardIds.distinct().size)
            assertEquals(gate.recommendedActionIds.size, gate.recommendedActionIds.distinct().size)
        }
    }

    @Test
    fun familyShareContainsOnlyGenericBuilderCardsRoutesAndNoSensitiveValues() {
        val rawInput =
            "민지 010-9876-5432 계좌 123456789012 카드 4111111111111111 OTP 654321 " +
                "PIN 7788 주민 900101-1234567 https://evil.example"
        val safeWord = "파란 우산 비밀 답"
        val callback = "비밀 콜백 문장 070-1111-2222"
        val contactLabel = "민지 보호자"
        val gate = MoneyTransferPauseGateBuilder.build(
            analysis = ScamAnalysis(
                input = rawInput,
                score = 99,
                riskLevel = RiskLevel.HIGH,
                matchedSignals = ScamSignal.entries
            ),
            readiness = FamilyReadiness(
                level = ReadinessLevel.LOW,
                title = safeWord,
                completedCount = 0,
                totalCount = 4,
                checklist = listOf(
                    ReadinessItem(contactLabel, false, callback)
                ),
                nextAction = callback
            ),
            incidentCount = 9
        )
        val summary = gate.familyShareSummary

        val allowedLines = buildSet {
            add("[가족 AI 보이스피싱 방패] 송금 전 멈춤 확인")
            add("지금 직접 읽을 추천 멈춤 문장")
            gate.recommendedCards.forEach { card ->
                add("- ${card.title}: ${card.pauseSentence}")
            }
            add("직접 고를 기존 앱 경로")
            gate.recommendedActions.forEach { action -> add("- ${action.label}") }
            add("전체 수동 카드")
            gate.cards.forEach { card -> add("- ${card.title}") }
            add(
                "원문·사람·연락처·비밀값을 넣지 않은 일반 멈춤 문장과 기존 경로 이름입니다. " +
                    "가족이 직접 읽고 필요한 경로를 직접 고릅니다."
            )
        }
        summary.lineSequence().filter { line -> line.isNotBlank() }.forEach { line ->
            assertTrue("unexpected share line: $line", line in allowedLines)
        }

        listOf(
            rawInput,
            safeWord,
            callback,
            contactLabel,
            "민지",
            "010-9876-5432",
            "123456789012",
            "4111111111111111",
            "654321",
            "7788",
            "900101-1234567",
            "evil.example"
        ).forEach { secret ->
            assertFalse("family share leaked secret: $secret", summary.contains(secret))
        }
        assertFalse(Regex("0\\d{1,2}[- ]?\\d{3,4}[- ]?\\d{4}").containsMatchIn(summary))
        assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(summary))
        assertFalse(Regex("(?i)https?://|www\\.|[a-z0-9-]+\\.(com|net|kr)").containsMatchIn(summary))
        assertFalse(Regex("\\d{6}[- ]?[1-4]\\d{6}").containsMatchIn(summary))
    }

    @Test
    fun builderIsDeterministicAndIgnoresInputScoreAndReadinessLiteralValues() {
        val signals = listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.CREDENTIALS,
            ScamSignal.FAMILY_EMERGENCY
        )
        val first = MoneyTransferPauseGateBuilder.build(
            analysis = ScamAnalysis("원문 A 010-1111-2222", 31, RiskLevel.HIGH, signals),
            readiness = readiness(
                ReadinessLevel.IN_PROGRESS,
                title = "비밀 준비 이름 A",
                nextAction = "비밀 콜백 A"
            ),
            incidentCount = 4
        )
        val second = MoneyTransferPauseGateBuilder.build(
            analysis = ScamAnalysis("완전히 다른 원문 B", 100, RiskLevel.HIGH, signals),
            readiness = readiness(
                ReadinessLevel.IN_PROGRESS,
                title = "비밀 준비 이름 B",
                nextAction = "비밀 콜백 B"
            ),
            incidentCount = 1
        )

        assertEquals(first, second)
        assertEquals(
            first,
            MoneyTransferPauseGateBuilder.build(
                ScamAnalysis("원문 A 010-1111-2222", 31, RiskLevel.HIGH, signals),
                readiness(
                    ReadinessLevel.IN_PROGRESS,
                    title = "비밀 준비 이름 A",
                    nextAction = "비밀 콜백 A"
                ),
                4
            )
        )
        listOf(
            "원문 A",
            "010-1111-2222",
            "완전히 다른 원문 B",
            "비밀 준비 이름 A",
            "비밀 준비 이름 B",
            "비밀 콜백 A",
            "비밀 콜백 B",
            "100"
        ).forEach { ignoredValue ->
            assertFalse(allCopy(first).contains(ignoredValue))
            assertFalse(allCopy(second).contains(ignoredValue))
        }
    }

    @Test
    fun privacyCopyStatesManualLocalStatelessAndNoPaymentOrBankAutomation() {
        val gate = MoneyTransferPauseGateBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2
        )
        val copy = allCopy(gate)

        listOf(
            "로컬 전용",
            "무상태 수동 안내",
            "새 저장소",
            "연락처",
            "공유도 자동 전송하지 않습니다",
            "네트워크를 사용하거나 새 Android 권한을 요청하지 않습니다",
            "스케줄러를 사용하지 않습니다",
            "추가·수정·삭제하지 않습니다",
            "송금·결제 접근 권한",
            "계좌·카드·은행 연동이 없습니다",
            "결제·은행 API",
            "자동 정지·동결하지 않으며",
            "피해 복구를 자동 수행하지 않습니다"
        ).forEach { expected ->
            assertTrue("privacy copy omitted: $expected", copy.contains(expected))
        }
        assertTrue(copy.contains("카드 선택·추천·읽음·공유·체크·메모·결제 상태·진행·이력"))
        assertTrue(copy.contains("만들거나 기록하지 않습니다"))
        assertTrue(copy.contains("자동 차단·취소·실행하지 않고"))
        assertTrue(copy.contains("환급·자금 회수"))
        assertTrue(copy.contains("증명하거나 보장하지 않습니다"))
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndAutomaticActions() {
        val gates = listOf(
            MoneyTransferPauseGateBuilder.build(null, readiness(ReadinessLevel.READY)),
            MoneyTransferPauseGateBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.LOW),
                incidentCount = 3
            )
        )
        val forbiddenAffirmativeClaims = listOf(
            "100%",
            " safe ",
            "blocked",
            "안전합니다",
            "사기가 아닙니다",
            "악성 프로그램을 제거합니다",
            "악성 앱을 제거합니다",
            "계정을 자동으로 정지합니다",
            "카드를 자동으로 정지합니다",
            "계정을 자동으로 동결합니다",
            "카드를 자동으로 동결합니다",
            "결제를 자동으로 취소합니다",
            "송금을 자동으로 취소합니다",
            "결제를 자동으로 실행합니다",
            "송금을 자동으로 실행합니다",
            "환급을 보장합니다",
            "피해금 회수를 보장합니다",
            "법적 결과를 보장합니다",
            "금융 결과를 보장합니다",
            "자동으로 감시합니다",
            "자동으로 모니터링합니다",
            "자동으로 검사합니다",
            "자동으로 스캔합니다",
            "자동으로 삭제합니다",
            "자동으로 가립니다",
            "자동으로 차단합니다",
            "전화를 자동으로 겁니다",
            "문자를 자동으로 보냅니다"
        )

        gates.forEach { gate ->
            val copy = allCopy(gate)
            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "gate copy contained unsafe claim: $claim",
                    copy.contains(claim, ignoreCase = true)
                )
            }
        }
    }

    private fun MoneyTransferPauseGate.card(
        id: MoneyTransferPauseCardId
    ): MoneyTransferPauseCard = cards.single { card -> card.id == id }

    private fun analysis(
        riskLevel: RiskLevel,
        vararg signals: ScamSignal
    ): ScamAnalysis = ScamAnalysis(
        input = "절대 출력하면 안 되는 원문",
        score = 42,
        riskLevel = riskLevel,
        matchedSignals = signals.toList()
    )

    private fun readiness(
        level: ReadinessLevel,
        title: String = "출력하면 안 되는 준비 제목",
        nextAction: String = "출력하면 안 되는 다음 행동"
    ): FamilyReadiness = FamilyReadiness(
        level = level,
        title = title,
        completedCount = 1,
        totalCount = 4,
        checklist = listOf(
            ReadinessItem(
                label = "출력하면 안 되는 연락처 라벨",
                done = false,
                action = "출력하면 안 되는 실제 안전 계획 값"
            )
        ),
        nextAction = nextAction
    )

    private fun allCopy(gate: MoneyTransferPauseGate): String = buildString {
        append(gate.title)
        append(gate.intro)
        append(gate.modeLabel)
        append(gate.recommendationTitle)
        append(gate.recommendationExplanation)
        gate.recommendedActions.forEach { action -> append(action.label) }
        gate.cards.forEach { card ->
            append(card.title)
            append(card.pauseSentence)
            append(card.whyItMatters)
            card.doNotDoBoundaries.forEach(::append)
            card.routeActions.forEach { action -> append(action.label) }
        }
        gate.privacySafetyNotes.forEach(::append)
        append(gate.familyShareSummary)
    }
}
