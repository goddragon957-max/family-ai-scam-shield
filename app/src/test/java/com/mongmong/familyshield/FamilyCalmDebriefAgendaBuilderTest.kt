package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyCalmDebriefAgendaBuilderTest {
    @Test
    fun noAnalysisAndNoSignalsUseConservativeAgendaWithoutSafeVerdict() {
        val noAnalysis = FamilyCalmDebriefAgendaBuilder.build(
            analysis = null,
            readiness = readyReadiness()
        )
        val noSignals = FamilyCalmDebriefAgendaBuilder.build(
            analysis = ScamAnalysis("일반 대화", 0, RiskLevel.SAFE, emptyList()),
            readiness = readyReadiness()
        )
        val expectedCards = listOf(
            FamilyCalmDebriefCardId.CALM_WITHOUT_BLAME,
            FamilyCalmDebriefCardId.CONFIRM_ACTION_PAUSE,
            FamilyCalmDebriefCardId.DESCRIBE_ACTION_CATEGORIES,
            FamilyCalmDebriefCardId.CHOOSE_SEPARATE_CALLBACK,
            FamilyCalmDebriefCardId.SET_MANUAL_FOLLOW_UP
        )

        listOf(noAnalysis, noSignals).forEach { agenda ->
            assertEquals(expectedCards, agenda.recommendedCardIds)
            assertEquals(expectedCards, agenda.recommendedCards.map { card -> card.id })
            assertTrue(agenda.recommendationExplanation.contains("괜찮다는 판정으로 보지 않"))
            listOf(
                FamilyCalmDebriefActionId.QUICK_STOP,
                FamilyCalmDebriefActionId.EXPOSURE_IMPACT_TRIAGE,
                FamilyCalmDebriefActionId.FAMILY_CALLBACK_CARD,
                FamilyCalmDebriefActionId.FAMILY_RESPONSE_ROLE_BOARD,
                FamilyCalmDebriefActionId.FOLLOW_UP_CHECK_IN_PLANNER
            ).forEach { actionId ->
                assertTrue("default agenda omitted $actionId", actionId in agenda.recommendedActionIds)
            }
            listOf(
                "안전합니다",
                "위험 없음",
                "사기 아님",
                "문제없습니다",
                "검증 완료"
            ).forEach { verdict ->
                assertFalse("default agenda asserted $verdict", allCopy(agenda).contains(verdict))
            }
        }
        assertTrue(FamilyCalmDebriefActionId.CHECKER in noAnalysis.recommendedActionIds)
        assertFalse(FamilyCalmDebriefActionId.ANALYSIS_RESULT in noAnalysis.recommendedActionIds)
        assertTrue(FamilyCalmDebriefActionId.ANALYSIS_RESULT in noSignals.recommendedActionIds)
        assertFalse(FamilyCalmDebriefActionId.CHECKER in noSignals.recommendedActionIds)
    }

    @Test
    fun highRiskFinancialCredentialsAndUrgentTransferRecommendProtectionEvidenceOfficialAndFollowUp() {
        val highRiskWithoutSignals = FamilyCalmDebriefAgendaBuilder.build(
            ScamAnalysis("테스트", 90, RiskLevel.HIGH, emptyList()),
            readyReadiness()
        )
        val signalAgendas = listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.BAIT,
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).map { signal ->
            FamilyCalmDebriefAgendaBuilder.build(analysisWith(signal), readyReadiness())
        }

        (listOf(highRiskWithoutSignals) + signalAgendas).forEach { agenda ->
            assertTrue(FamilyCalmDebriefCardId.CHOOSE_RESPONSE_ROUTES in agenda.recommendedCardIds)
            assertTrue(FamilyCalmDebriefCardId.SET_MANUAL_FOLLOW_UP in agenda.recommendedCardIds)
            listOf(
                FamilyCalmDebriefActionId.ACCOUNT_LOCKDOWN,
                FamilyCalmDebriefActionId.EVIDENCE_KIT,
                FamilyCalmDebriefActionId.OFFICIAL_HELP_DIRECTORY,
                FamilyCalmDebriefActionId.FOLLOW_UP_CHECK_IN_PLANNER,
                FamilyCalmDebriefActionId.RECOVERY_PLAN,
                FamilyCalmDebriefActionId.REPORT_PREP
            ).forEach { actionId ->
                assertTrue("financial agenda omitted $actionId", actionId in agenda.recommendedActionIds)
            }
        }
        assertTrue(
            highRiskWithoutSignals.recommendationExplanation.contains(
                "위험도가 높거나 금융·결제·계정 관련 신호"
            )
        )
    }

    @Test
    fun appInstallAndLiveCallControlRecommendDeviceEvidenceAndTrustedHelperWithoutRemovalGuarantee() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val agenda = FamilyCalmDebriefAgendaBuilder.build(
                analysisWith(signal),
                readyReadiness()
            )

            assertTrue(FamilyCalmDebriefCardId.CHOOSE_RESPONSE_ROUTES in agenda.recommendedCardIds)
            listOf(
                FamilyCalmDebriefActionId.ACCOUNT_LOCKDOWN,
                FamilyCalmDebriefActionId.EVIDENCE_KIT,
                FamilyCalmDebriefActionId.TRUSTED_HELPER_REVIEW,
                FamilyCalmDebriefActionId.RECOVERY_PLAN
            ).forEach { actionId ->
                assertTrue("$signal agenda omitted $actionId", actionId in agenda.recommendedActionIds)
            }
            val copy = allCopy(agenda)
            assertFalse(copy.contains("악성 앱을 제거했습니다"))
            assertFalse(copy.contains("악성코드 제거를 보장합니다"))
            assertFalse(copy.contains("기기를 안전하게 만들었습니다"))
        }
    }

    @Test
    fun linkAndSmishingRecommendExposureEvidenceFamilyWarningPrivacyAndFollowUpWithoutScanClaim() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val agenda = FamilyCalmDebriefAgendaBuilder.build(
                analysisWith(signal),
                readyReadiness()
            )

            assertTrue(FamilyCalmDebriefCardId.DESCRIBE_ACTION_CATEGORIES in agenda.recommendedCardIds)
            assertTrue(FamilyCalmDebriefCardId.CHOOSE_RESPONSE_ROUTES in agenda.recommendedCardIds)
            listOf(
                FamilyCalmDebriefActionId.EXPOSURE_IMPACT_TRIAGE,
                FamilyCalmDebriefActionId.EVIDENCE_KIT,
                FamilyCalmDebriefActionId.FAMILY_ALERT_MESSAGES,
                FamilyCalmDebriefActionId.PRIVACY_CENTER,
                FamilyCalmDebriefActionId.FOLLOW_UP_CHECK_IN_PLANNER
            ).forEach { actionId ->
                assertTrue("$signal agenda omitted $actionId", actionId in agenda.recommendedActionIds)
            }
            assertFalse(allCopy(agenda).contains("링크를 자동으로 검사합니다"))
            assertFalse(allCopy(agenda).contains("자동 스캔합니다"))
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendSeparateCallbackAndFamilyWarning() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val agenda = FamilyCalmDebriefAgendaBuilder.build(
                analysisWith(signal),
                readyReadiness()
            )

            assertTrue(FamilyCalmDebriefCardId.CHOOSE_SEPARATE_CALLBACK in agenda.recommendedCardIds)
            listOf(
                FamilyCalmDebriefActionId.FAMILY_CALLBACK_CARD,
                FamilyCalmDebriefActionId.SAFE_CALL_SCRIPTS,
                FamilyCalmDebriefActionId.FAMILY_ALERT_MESSAGES,
                FamilyCalmDebriefActionId.FAMILY_RESPONSE_ROLE_BOARD
            ).forEach { actionId ->
                assertTrue("$signal agenda omitted $actionId", actionId in agenda.recommendedActionIds)
            }
        }
    }

    @Test
    fun secrecyAndImpersonationRecommendStopExposureAndOfficialRoute() {
        listOf(ScamSignal.SECRECY, ScamSignal.IMPERSONATION).forEach { signal ->
            val agenda = FamilyCalmDebriefAgendaBuilder.build(
                analysisWith(signal),
                readyReadiness()
            )

            assertTrue(
                agenda.recommendedCardIds.containsAll(
                    listOf(
                        FamilyCalmDebriefCardId.CALM_WITHOUT_BLAME,
                        FamilyCalmDebriefCardId.CONFIRM_ACTION_PAUSE,
                        FamilyCalmDebriefCardId.DESCRIBE_ACTION_CATEGORIES,
                        FamilyCalmDebriefCardId.CHOOSE_RESPONSE_ROUTES
                    )
                )
            )
            listOf(
                FamilyCalmDebriefActionId.QUICK_STOP,
                FamilyCalmDebriefActionId.EXPOSURE_IMPACT_TRIAGE,
                FamilyCalmDebriefActionId.OFFICIAL_HELP_DIRECTORY
            ).forEach { actionId ->
                assertTrue("$signal agenda omitted $actionId", actionId in agenda.recommendedActionIds)
            }
        }
    }

    @Test
    fun savedIncidentPresenceChangesRoutesWithoutReadingIncidentContentsOrCountValue() {
        val raw = "INCIDENT-DEBRIEF-COMET 010-7654-3210 https://incident-debrief.example"
        val analysis = ScamAnalysis(raw, 0, RiskLevel.SAFE, emptyList())
        val withIncident = FamilyCalmDebriefAgendaBuilder.build(
            analysis,
            readyReadiness(),
            incidentCount = 987654321
        )
        val oneIncident = FamilyCalmDebriefAgendaBuilder.build(
            analysis,
            readyReadiness(),
            incidentCount = 1
        )
        val withoutIncident = FamilyCalmDebriefAgendaBuilder.build(
            analysis,
            readyReadiness(),
            incidentCount = 0
        )

        assertEquals(oneIncident, withIncident)
        assertTrue(FamilyCalmDebriefCardId.CHOOSE_RESPONSE_ROUTES in withIncident.recommendedCardIds)
        listOf(
            FamilyCalmDebriefActionId.EVIDENCE_KIT,
            FamilyCalmDebriefActionId.INCIDENT_TIMELINE_BRIEFING,
            FamilyCalmDebriefActionId.FOLLOW_UP_CHECK_IN_PLANNER
        ).forEach { actionId ->
            assertTrue("saved-incident agenda omitted $actionId", actionId in withIncident.recommendedActionIds)
        }
        assertFalse(
            FamilyCalmDebriefActionId.INCIDENT_TIMELINE_BRIEFING in
                withoutIncident.recommendedActionIds
        )
        listOf(raw, "INCIDENT-DEBRIEF-COMET", "010-7654-3210", "incident-debrief.example", "987654321")
            .forEach { secret ->
                assertFalse("incident value leaked: $secret", allCopy(withIncident).contains(secret))
            }
    }

    @Test
    fun lowAndInProgressReadinessAddCallbackRoadmapDrillAndFollowUpWhileReadyStaysCautious() {
        val analysis = analysisWith(ScamSignal.LINK)
        val low = FamilyCalmDebriefAgendaBuilder.build(analysis, lowReadiness())
        val inProgress = FamilyCalmDebriefAgendaBuilder.build(analysis, inProgressReadiness())
        val ready = FamilyCalmDebriefAgendaBuilder.build(analysis, readyReadiness())

        assertTrue(FamilyCalmDebriefCardId.CHOOSE_SEPARATE_CALLBACK in low.recommendedCardIds)
        listOf(
            FamilyCalmDebriefActionId.FAMILY_CALLBACK_CARD,
            FamilyCalmDebriefActionId.FAMILY_READINESS_ROADMAP,
            FamilyCalmDebriefActionId.FAMILY_SAFETY_DRILL_COACH,
            FamilyCalmDebriefActionId.FOLLOW_UP_CHECK_IN_PLANNER
        ).forEach { actionId ->
            assertTrue("low readiness omitted $actionId", actionId in low.recommendedActionIds)
            assertTrue("in-progress readiness omitted $actionId", actionId in inProgress.recommendedActionIds)
        }
        assertFalse(FamilyCalmDebriefActionId.FAMILY_READINESS_ROADMAP in ready.recommendedActionIds)
        assertTrue(low.recommendationExplanation.contains("부족해도 괜찮"))
        assertTrue(inProgress.recommendationExplanation.contains("진행 중"))
        assertTrue(ready.recommendationExplanation.contains("되어 있어도"))
        assertTrue(ready.recommendationExplanation.contains("확인된 것은 아닙니다"))
        assertNotEquals(low.recommendationExplanation, ready.recommendationExplanation)
    }

    @Test
    fun everyCardHasCompleteFieldsStableOrderUniqueRoutesAndAllActionsMapToCards() {
        val withoutAnalysis = FamilyCalmDebriefAgendaBuilder.build(null, lowReadiness())
        val withAnalysis = FamilyCalmDebriefAgendaBuilder.build(
            analysisWith(ScamSignal.LINK),
            readyReadiness()
        )
        val expectedOrder = listOf(
            FamilyCalmDebriefCardId.CALM_WITHOUT_BLAME,
            FamilyCalmDebriefCardId.CONFIRM_ACTION_PAUSE,
            FamilyCalmDebriefCardId.DESCRIBE_ACTION_CATEGORIES,
            FamilyCalmDebriefCardId.CHOOSE_SEPARATE_CALLBACK,
            FamilyCalmDebriefCardId.CHOOSE_RESPONSE_ROUTES,
            FamilyCalmDebriefCardId.SET_MANUAL_FOLLOW_UP
        )

        assertEquals(expectedOrder, FamilyCalmDebriefCardId.entries)
        assertEquals(expectedOrder, withoutAnalysis.agendaCards.map { card -> card.id })
        assertEquals((1..6).toList(), withoutAnalysis.agendaCards.map { card -> card.displayOrder })
        assertEquals(withoutAnalysis.agendaCards, withoutAnalysis.cards)
        withoutAnalysis.agendaCards.forEach { card ->
            assertEquals(card.id.displayOrder, card.displayOrder)
            assertTrue(card.title.isNotBlank())
            assertTrue(card.category.label.isNotBlank())
            assertTrue(card.whatToSayOrDo.isNotBlank())
            assertTrue(card.whatNotToAskStoreOrShare.isNotEmpty())
            assertTrue(card.whatNotToAskStoreOrShare.all { boundary -> boundary.isNotBlank() })
            assertTrue(card.primaryAction.label.isNotBlank())
            assertTrue(card.secondaryActions.isNotEmpty())
            assertEquals(card.primaryAction, card.routeActions.first())
            assertEquals(
                card.routeActions.size,
                card.routeActions.map { action -> action.id }.distinct().size
            )
        }

        val coveredActionIds = (withoutAnalysis.agendaCards + withAnalysis.agendaCards)
            .flatMap { card -> card.routeActions }
            .map { action -> action.id }
            .toSet()
        assertEquals(FamilyCalmDebriefActionId.entries.toSet(), coveredActionIds)
        assertEquals(
            withoutAnalysis.recommendedActionIds.size,
            withoutAnalysis.recommendedActionIds.distinct().size
        )
    }

    @Test
    fun copyFramesBlameFreeManualAgendaWithoutAskingForNamesContactsSecretsOrRawText() {
        val agenda = FamilyCalmDebriefAgendaBuilder.build(null, lowReadiness())
        val copy = allCopy(agenda)

        assertTrue(agenda.intro.contains("누구를 탓하지 않고"))
        assertTrue(agenda.intro.contains("로컬 전용·무상태"))
        assertTrue(agenda.intro.contains("채팅 감시·업무 배정·일기 기능이 아니"))
        listOf(
            "이름을 입력하세요",
            "전화번호를 입력하세요",
            "비밀번호를 입력하세요",
            "OTP를 입력하세요",
            "PIN을 입력하세요",
            "안전단어 답을 입력하세요",
            "계좌번호를 입력하세요",
            "URL을 입력하세요",
            "원문을 붙여넣으세요",
            "가족 이름을 적어 주세요",
            "대화 내용을 저장하세요"
        ).forEach { request ->
            assertFalse("copy requested a forbidden value: $request", copy.contains(request))
        }
    }

    @Test
    fun shareUsesOnlyGenericAgendaCategoryAndRecommendedRouteLabelsWithoutTaintedInputs() {
        val safeWord = "해질녘 파란 우산은 어디에?"
        val callbackScript = "푸른 메모장을 열고 첫 저장 번호로 전화한다."
        val contactLabel = "보호자-은하수"
        val contactNumber = "010-2468-1357"
        val raw =
            "RAW-DEBRIEF-COMET 전화 010-9876-5432 계좌 123456789012 비밀번호 BluePassword77 " +
                "OTP A1B2C3 인증번호 654321 PIN 7788 https://evil.example/path " +
                "주민번호 900101-1234567 안전 질문 $safeWord 콜백 $callbackScript $contactLabel $contactNumber"
        val analysis = ScamAnalysis(
            input = raw,
            score = 100,
            riskLevel = RiskLevel.HIGH,
            matchedSignals = listOf(
                ScamSignal.URGENT_TRANSFER,
                ScamSignal.APP_INSTALL,
                ScamSignal.LINK,
                ScamSignal.PHONE_NUMBER_CHANGE,
                ScamSignal.SECRECY
            )
        )
        val readiness = FamilyReadiness(
            level = ReadinessLevel.READY,
            title = raw,
            completedCount = 654321,
            totalCount = 987654,
            checklist = listOf(
                ReadinessItem("$safeWord $contactNumber", false, "$callbackScript https://evil.example")
            ),
            nextAction = "계좌 123456789012 OTP 654321 PIN 7788"
        )
        val agenda = FamilyCalmDebriefAgendaBuilder.build(
            analysis,
            readiness,
            incidentCount = 654321
        )
        val summary = agenda.familyShareSummary
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)

        agenda.agendaCards.forEach { card ->
            assertTrue("share omitted card: ${card.id}", summary.contains(card.title))
            assertTrue("share omitted category: ${card.id}", summary.contains(card.category.label))
            assertFalse("share included full saying: ${card.id}", summary.contains(card.whatToSayOrDo))
            card.whatNotToAskStoreOrShare.forEach { boundary ->
                assertFalse("share included detailed boundary: $boundary", summary.contains(boundary))
            }
        }
        agenda.recommendedActions.forEach { action ->
            assertTrue("share omitted recommended route: ${action.id}", summary.contains(action.label))
        }
        assertFalse(summary.contains(agenda.recommendationExplanation))
        assertFalse(summary.contains(raw))
        assertFalse(summary.contains(redactedClue))

        listOf(
            "RAW-DEBRIEF-COMET",
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
        ).forEach { secret ->
            assertFalse("debrief share leaked: $secret", summary.contains(secret))
            assertFalse("debrief output leaked: $secret", allCopy(agenda).contains(secret))
        }
        assertFalse(phonePattern.containsMatchIn(summary))
        assertFalse(longNumberPattern.containsMatchIn(summary))
        assertFalse(groupedNumberPattern.containsMatchIn(summary))
        assertFalse(urlDomainPattern.containsMatchIn(summary))
        assertFalse(residentPattern.containsMatchIn(summary))
    }

    @Test
    fun builderIsDeterministicIgnoresValueFieldsTreatsPositiveCountAsPresenceAndClampsNegative() {
        val firstAnalysis = ScamAnalysis(
            input = "FIRST-SECRET 010-1111-2222",
            score = 99,
            riskLevel = RiskLevel.HIGH,
            matchedSignals = listOf(
                ScamSignal.LINK,
                ScamSignal.APP_INSTALL,
                ScamSignal.URGENT_TRANSFER,
                ScamSignal.SECRECY
            )
        )
        val secondAnalysis = ScamAnalysis(
            input = "SECOND-SECRET https://second.example",
            score = 1,
            riskLevel = RiskLevel.HIGH,
            matchedSignals = listOf(
                ScamSignal.SECRECY,
                ScamSignal.URGENT_TRANSFER,
                ScamSignal.APP_INSTALL,
                ScamSignal.LINK
            )
        )
        val firstReadiness = taintedReadiness(ReadinessLevel.IN_PROGRESS, "FIRST-PLAN-SECRET")
        val secondReadiness = taintedReadiness(ReadinessLevel.IN_PROGRESS, "SECOND-PLAN-SECRET")

        val first = FamilyCalmDebriefAgendaBuilder.build(firstAnalysis, firstReadiness, 1)
        val second = FamilyCalmDebriefAgendaBuilder.build(secondAnalysis, secondReadiness, Int.MAX_VALUE)
        assertEquals(first, second)
        assertEquals(first.recommendedCards, second.recommendedCards)
        assertEquals(first.recommendedActionIds, second.recommendedActionIds)
        assertEquals(first.familyShareSummary, second.familyShareSummary)

        val negative = FamilyCalmDebriefAgendaBuilder.build(firstAnalysis, firstReadiness, -5)
        val zero = FamilyCalmDebriefAgendaBuilder.build(firstAnalysis, firstReadiness, 0)
        assertEquals(zero, negative)
    }

    @Test
    fun privacyCopyStatesManualLocalStatelessNoStorageContactsAutoSendNetworkPermissionsSchedulerOrMutation() {
        representativeAgendas().forEach { agenda ->
            val copy = agenda.privacySafetyNotes.joinToString(" ")

            assertTrue(copy.contains("로컬 전용·무상태 수동"))
            assertTrue(copy.contains("가족 이름·연락처"))
            assertTrue(copy.contains("안건·카드 선택"))
            assertTrue(copy.contains("대화 답변·메모"))
            assertTrue(copy.contains("체크·완료·점수·읽음·진행 상태"))
            assertTrue(copy.contains("공유 이력·대화 이력"))
            assertTrue(copy.contains("저장하지 않습니다"))
            assertTrue(copy.contains("문자·메시지·통화·연락처"))
            assertTrue(copy.contains("읽거나 감시하지 않"))
            assertTrue(copy.contains("받는 사람을 고르거나 메시지를 자동으로 보내지 않"))
            assertTrue(copy.contains("네트워크"))
            assertTrue(copy.contains("새 Android 권한"))
            assertTrue(copy.contains("알림·알람·캘린더 일정·리마인더"))
            assertTrue(copy.contains("백그라운드 워커"))
            assertTrue(copy.contains("만들거나 예약하지 않습니다"))
            assertTrue(copy.contains("사건 기록을 자동으로 추가·수정·삭제하지 않습니다"))
        }
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesBlockingRemovalFreezeRecoveryAndAutomaticMonitoringScanningDeletion() {
        val forbiddenClaims = listOf(
            "100%",
            "safe",
            "blocked",
            "모든 사기를 찾아냅니다",
            "사기를 확정합니다",
            "안전합니다",
            "위험 없음",
            "사기 아님",
            "검증을 완료했습니다",
            "자동으로 감시합니다",
            "자동 모니터링합니다",
            "자동으로 검사합니다",
            "자동 스캔합니다",
            "연락처를 자동으로 읽습니다",
            "자동으로 전송합니다",
            "문자를 자동으로 차단합니다",
            "전화를 자동으로 차단합니다",
            "악성 앱을 제거했습니다",
            "악성코드를 제거합니다",
            "악성코드 제거를 보장합니다",
            "계좌를 자동으로 지급정지합니다",
            "카드를 자동으로 정지합니다",
            "계정을 자동으로 잠급니다",
            "피해금 회수를 보장합니다",
            "법적 결과를 보장합니다",
            "금융 결과를 보장합니다",
            "자동으로 삭제합니다"
        )

        representativeAgendas().forEach { agenda ->
            val copy = allCopy(agenda).lowercase()
            forbiddenClaims.forEach { claim ->
                assertFalse(
                    "debrief copy contained unsafe claim: $claim",
                    copy.contains(claim.lowercase())
                )
            }
        }
    }

    private fun representativeAgendas(): List<FamilyCalmDebriefAgenda> = listOf(
        FamilyCalmDebriefAgendaBuilder.build(null, lowReadiness()),
        FamilyCalmDebriefAgendaBuilder.build(
            analysisWith(ScamSignal.APP_INSTALL),
            inProgressReadiness()
        ),
        FamilyCalmDebriefAgendaBuilder.build(
            analysisWith(
                ScamSignal.CREDENTIALS,
                ScamSignal.URGENT_TRANSFER,
                riskLevel = RiskLevel.HIGH
            ),
            readyReadiness(),
            incidentCount = 2
        ),
        FamilyCalmDebriefAgendaBuilder.build(
            analysisWith(ScamSignal.LINK, ScamSignal.DEEPFAKE_VOICE, ScamSignal.SECRECY),
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

    private fun lowReadiness(): FamilyReadiness = FamilyReadiness(
        level = ReadinessLevel.LOW,
        title = "준비 부족",
        completedCount = 0,
        totalCount = 4,
        checklist = emptyList(),
        nextAction = "직접 준비"
    )

    private fun inProgressReadiness(): FamilyReadiness = FamilyReadiness(
        level = ReadinessLevel.IN_PROGRESS,
        title = "준비 중",
        completedCount = 1,
        totalCount = 4,
        checklist = emptyList(),
        nextAction = "직접 보완"
    )

    private fun readyReadiness(): FamilyReadiness = FamilyReadiness(
        level = ReadinessLevel.READY,
        title = "준비 좋음",
        completedCount = 3,
        totalCount = 4,
        checklist = emptyList(),
        nextAction = "직접 다시 확인"
    )

    private fun taintedReadiness(level: ReadinessLevel, marker: String): FamilyReadiness =
        FamilyReadiness(
            level = level,
            title = "$marker 010-7777-8888",
            completedCount = 654321,
            totalCount = 987654,
            checklist = listOf(
                ReadinessItem(marker, false, "https://$marker.example 123456789012")
            ),
            nextAction = "$marker OTP 654321 PIN 7788"
        )

    private fun allCopy(agenda: FamilyCalmDebriefAgenda): String = buildString {
        appendLine(agenda.title)
        appendLine(agenda.intro)
        appendLine(agenda.durationLabel)
        appendLine(agenda.recommendationTitle)
        appendLine(agenda.recommendationExplanation)
        agenda.recommendedActions.forEach { action -> appendLine(action.label) }
        agenda.agendaCards.forEach { card ->
            appendLine(card.title)
            appendLine(card.category.label)
            appendLine(card.whatToSayOrDo)
            card.whatNotToAskStoreOrShare.forEach(::appendLine)
            card.routeActions.forEach { action -> appendLine(action.label) }
        }
        agenda.privacySafetyNotes.forEach(::appendLine)
        append(agenda.familyShareSummary)
    }

    private val phonePattern = Regex("0\\d{1,2}[- ]?\\d{3,4}[- ]?\\d{4}")
    private val longNumberPattern = Regex("(?<!\\d)\\d{6,}(?!\\d)")
    private val groupedNumberPattern = Regex("(?<!\\d)\\d{2,4}(?:[- ]\\d{2,4}){2,}(?!\\d)")
    private val urlDomainPattern = Regex(
        "(?i)(?:https?://|www\\.|[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.(?:com|net|org|kr|io|co))"
    )
    private val residentPattern = Regex("(?<!\\d)\\d{6}[- ]?[1-4]\\d{6}(?!\\d)")
}
