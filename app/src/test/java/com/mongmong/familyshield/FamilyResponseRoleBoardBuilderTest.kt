package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyResponseRoleBoardBuilderTest {
    @Test
    fun noAnalysisAndNoSignalsRecommendFourConservativeRolesWithoutSafeVerdict() {
        val noAnalysis = FamilyResponseRoleBoardBuilder.build(
            analysis = null,
            readiness = readyReadiness()
        )
        val noSignals = FamilyResponseRoleBoardBuilder.build(
            analysis = ScamAnalysis("일반 대화", 0, RiskLevel.SAFE, emptyList()),
            readiness = readyReadiness()
        )
        val expected = listOf(
            FamilyResponseRoleId.PAUSE_LEAD,
            FamilyResponseRoleId.CALLBACK_VERIFIER,
            FamilyResponseRoleId.EXPOSURE_SORTER,
            FamilyResponseRoleId.FOLLOW_UP_COORDINATOR
        )

        listOf(noAnalysis, noSignals).forEach { board ->
            assertEquals(expected, board.recommendedRoleIds)
            assertEquals(expected, board.recommendedRoles.map { card -> card.id })
            assertTrue(board.recommendationExplanation.contains("안전 판정으로 보지 않"))
            listOf(
                FamilyResponseRoleActionId.QUICK_STOP,
                FamilyResponseRoleActionId.EXPOSURE_IMPACT_TRIAGE,
                FamilyResponseRoleActionId.FAMILY_CALLBACK_CARD,
                FamilyResponseRoleActionId.FOLLOW_UP_CHECK_IN_PLANNER
            ).forEach { actionId ->
                assertTrue("default board omitted $actionId", actionId in board.recommendedActionIds)
            }
            listOf("안전합니다", "위험 없음", "사기 아님", "문제없습니다", "검증 완료").forEach { verdict ->
                assertFalse("default board asserted $verdict", allCopy(board).contains(verdict))
            }
        }
        assertTrue(FamilyResponseRoleActionId.CHECKER in noAnalysis.recommendedActionIds)
        assertFalse(FamilyResponseRoleActionId.ANALYSIS_RESULT in noAnalysis.recommendedActionIds)
        assertTrue(FamilyResponseRoleActionId.ANALYSIS_RESULT in noSignals.recommendedActionIds)
        assertFalse(FamilyResponseRoleActionId.CHECKER in noSignals.recommendedActionIds)
    }

    @Test
    fun highRiskAndFinancialCredentialPaymentSignalsRecommendProtectionOfficialEvidenceAndFollowUp() {
        val highRiskWithoutFinancialSignal = FamilyResponseRoleBoardBuilder.build(
            ScamAnalysis("테스트", 90, RiskLevel.HIGH, emptyList()),
            readyReadiness()
        )
        val signalBoards = listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.BAIT,
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).map { signal ->
            FamilyResponseRoleBoardBuilder.build(analysisWith(signal), readyReadiness())
        }
        val boards = listOf(highRiskWithoutFinancialSignal) + signalBoards

        boards.forEach { board ->
            assertTrue(
                board.recommendedRoleIds.containsAll(
                    listOf(
                        FamilyResponseRoleId.ACCOUNT_DEVICE_PROTECTOR,
                        FamilyResponseRoleId.EVIDENCE_KEEPER,
                        FamilyResponseRoleId.OFFICIAL_HELP_CONNECTOR,
                        FamilyResponseRoleId.FOLLOW_UP_COORDINATOR
                    )
                )
            )
            listOf(
                FamilyResponseRoleActionId.RECOVERY_PLAN,
                FamilyResponseRoleActionId.ACCOUNT_LOCKDOWN,
                FamilyResponseRoleActionId.EVIDENCE_KIT,
                FamilyResponseRoleActionId.OFFICIAL_HELP_DIRECTORY,
                FamilyResponseRoleActionId.FOLLOW_UP_CHECK_IN_PLANNER
            ).forEach { actionId ->
                assertTrue("financial board omitted $actionId", actionId in board.recommendedActionIds)
            }
        }
        assertTrue(
            highRiskWithoutFinancialSignal.recommendationExplanation.contains(
                "위험도가 높거나 금융·결제·계정 관련 신호가 있어"
            )
        )
        signalBoards.forEach { board ->
            assertEquals(
                FamilyResponseRoleActionId.RECOVERY_PLAN,
                board.recommendedActionIds.first()
            )
        }
    }

    @Test
    fun remoteAppAndScreenShareRecommendProtectorEvidenceAndTrustedReviewWithoutRemovalGuarantee() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val board = FamilyResponseRoleBoardBuilder.build(
                analysisWith(signal),
                readyReadiness()
            )

            assertTrue(FamilyResponseRoleId.ACCOUNT_DEVICE_PROTECTOR in board.recommendedRoleIds)
            assertTrue(FamilyResponseRoleId.EVIDENCE_KEEPER in board.recommendedRoleIds)
            listOf(
                FamilyResponseRoleActionId.ACCOUNT_LOCKDOWN,
                FamilyResponseRoleActionId.EVIDENCE_KIT,
                FamilyResponseRoleActionId.TRUSTED_HELPER_REVIEW
            ).forEach { actionId ->
                assertTrue("$signal board omitted $actionId", actionId in board.recommendedActionIds)
            }
            assertEquals(
                FamilyResponseRoleActionId.ACCOUNT_LOCKDOWN,
                board.recommendedActionIds.first()
            )
            val copy = allCopy(board)
            assertFalse(copy.contains("악성 앱을 자동으로 제거합니다"))
            assertFalse(copy.contains("악성코드 제거를 보장합니다"))
            assertFalse(copy.contains("기기를 안전하게 만들었습니다"))
        }
    }

    @Test
    fun linkAndSmishingRecommendExposureEvidenceFamilyAlertPrivacyAndFollowUpWithoutScanClaim() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val board = FamilyResponseRoleBoardBuilder.build(
                analysisWith(signal),
                readyReadiness()
            )

            assertTrue(
                board.recommendedRoleIds.containsAll(
                    listOf(
                        FamilyResponseRoleId.EXPOSURE_SORTER,
                        FamilyResponseRoleId.EVIDENCE_KEEPER,
                        FamilyResponseRoleId.FAMILY_ALERT_RELAY,
                        FamilyResponseRoleId.FOLLOW_UP_COORDINATOR
                    )
                )
            )
            listOf(
                FamilyResponseRoleActionId.EXPOSURE_IMPACT_TRIAGE,
                FamilyResponseRoleActionId.EVIDENCE_KIT,
                FamilyResponseRoleActionId.FAMILY_ALERT_MESSAGES,
                FamilyResponseRoleActionId.FOLLOW_UP_CHECK_IN_PLANNER,
                FamilyResponseRoleActionId.PRIVACY_CENTER
            ).forEach { actionId ->
                assertTrue("$signal board omitted $actionId", actionId in board.recommendedActionIds)
            }
            assertFalse(allCopy(board).contains("자동으로 검사합니다"))
            assertFalse(allCopy(board).contains("자동 스캔합니다"))
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendCallbackVerifierAndAlertRelay() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val board = FamilyResponseRoleBoardBuilder.build(
                analysisWith(signal),
                readyReadiness()
            )

            assertTrue(FamilyResponseRoleId.CALLBACK_VERIFIER in board.recommendedRoleIds)
            assertTrue(FamilyResponseRoleId.FAMILY_ALERT_RELAY in board.recommendedRoleIds)
            listOf(
                FamilyResponseRoleActionId.FAMILY_CALLBACK_CARD,
                FamilyResponseRoleActionId.SAFE_CALL_SCRIPTS,
                FamilyResponseRoleActionId.FAMILY_ALERT_MESSAGES
            ).forEach { actionId ->
                assertTrue("$signal board omitted $actionId", actionId in board.recommendedActionIds)
            }
        }
    }

    @Test
    fun secrecyAndImpersonationRecommendPauseExposureAndOfficialConnector() {
        listOf(ScamSignal.SECRECY, ScamSignal.IMPERSONATION).forEach { signal ->
            val board = FamilyResponseRoleBoardBuilder.build(
                analysisWith(signal),
                readyReadiness()
            )

            assertTrue(
                board.recommendedRoleIds.containsAll(
                    listOf(
                        FamilyResponseRoleId.PAUSE_LEAD,
                        FamilyResponseRoleId.EXPOSURE_SORTER,
                        FamilyResponseRoleId.OFFICIAL_HELP_CONNECTOR
                    )
                )
            )
            listOf(
                FamilyResponseRoleActionId.QUICK_STOP,
                FamilyResponseRoleActionId.EXPOSURE_IMPACT_TRIAGE,
                FamilyResponseRoleActionId.OFFICIAL_HELP_DIRECTORY
            ).forEach { actionId ->
                assertTrue("$signal board omitted $actionId", actionId in board.recommendedActionIds)
            }
        }
    }

    @Test
    fun savedIncidentPresenceAddsEvidenceTimelineAndFollowUpWithoutIncidentDataLeak() {
        val marker = 987654321
        val raw = "INCIDENT-ROLE-COMET 010-7654-3210 https://incident-role.example"
        val analysis = ScamAnalysis(raw, 0, RiskLevel.SAFE, emptyList())
        val withIncident = FamilyResponseRoleBoardBuilder.build(
            analysis,
            readyReadiness(),
            incidentCount = marker
        )
        val withoutIncident = FamilyResponseRoleBoardBuilder.build(
            analysis,
            readyReadiness(),
            incidentCount = 0
        )

        assertTrue(FamilyResponseRoleId.EVIDENCE_KEEPER in withIncident.recommendedRoleIds)
        assertTrue(FamilyResponseRoleId.FOLLOW_UP_COORDINATOR in withIncident.recommendedRoleIds)
        assertTrue(
            FamilyResponseRoleActionId.INCIDENT_TIMELINE_BRIEFING in
                withIncident.recommendedActionIds
        )
        assertTrue(FamilyResponseRoleActionId.EVIDENCE_KIT in withIncident.recommendedActionIds)
        assertTrue(
            FamilyResponseRoleActionId.FOLLOW_UP_CHECK_IN_PLANNER in
                withIncident.recommendedActionIds
        )
        assertFalse(
            FamilyResponseRoleActionId.INCIDENT_TIMELINE_BRIEFING in
                withoutIncident.recommendedActionIds
        )
        val copy = allCopy(withIncident)
        listOf(raw, "INCIDENT-ROLE-COMET", "010-7654-3210", "incident-role.example", marker.toString())
            .forEach { secret -> assertFalse("incident data leaked: $secret", copy.contains(secret)) }
    }

    @Test
    fun lowReadinessAddsCallbackRoadmapAndDrillWhileReadyCopyDiffersCalmly() {
        val analysis = analysisWith(ScamSignal.LINK)
        val low = FamilyResponseRoleBoardBuilder.build(analysis, lowReadiness())
        val inProgress = FamilyResponseRoleBoardBuilder.build(analysis, inProgressReadiness())
        val ready = FamilyResponseRoleBoardBuilder.build(analysis, readyReadiness())

        assertTrue(FamilyResponseRoleId.CALLBACK_VERIFIER in low.recommendedRoleIds)
        listOf(
            FamilyResponseRoleActionId.FAMILY_CALLBACK_CARD,
            FamilyResponseRoleActionId.FAMILY_READINESS_ROADMAP,
            FamilyResponseRoleActionId.FAMILY_SAFETY_DRILL_COACH
        ).forEach { actionId ->
            assertTrue("low readiness omitted $actionId", actionId in low.recommendedActionIds)
        }
        assertTrue(low.recommendationExplanation.contains("부족해도 괜찮"))
        assertTrue(inProgress.recommendationExplanation.contains("진행 중"))
        assertTrue(ready.recommendationExplanation.contains("되어 있어도"))
        assertNotEquals(low.recommendationExplanation, ready.recommendationExplanation)
    }

    @Test
    fun everyRoleHasCompleteFieldsStableOrderUniqueRoutesAndAllExistingFlows() {
        val withoutAnalysis = FamilyResponseRoleBoardBuilder.build(null, lowReadiness())
        val withAnalysis = FamilyResponseRoleBoardBuilder.build(
            analysisWith(ScamSignal.LINK),
            readyReadiness()
        )
        val expectedOrder = listOf(
            FamilyResponseRoleId.PAUSE_LEAD,
            FamilyResponseRoleId.CALLBACK_VERIFIER,
            FamilyResponseRoleId.EXPOSURE_SORTER,
            FamilyResponseRoleId.ACCOUNT_DEVICE_PROTECTOR,
            FamilyResponseRoleId.EVIDENCE_KEEPER,
            FamilyResponseRoleId.OFFICIAL_HELP_CONNECTOR,
            FamilyResponseRoleId.FAMILY_ALERT_RELAY,
            FamilyResponseRoleId.FOLLOW_UP_COORDINATOR
        )

        assertEquals(expectedOrder, FamilyResponseRoleId.entries)
        assertEquals(expectedOrder, withoutAnalysis.roleCards.map { card -> card.id })
        assertEquals((1..8).toList(), withoutAnalysis.roleCards.map { card -> card.displayOrder })
        assertEquals(withoutAnalysis.roleCards, withoutAnalysis.cards)
        assertEquals(withoutAnalysis.roleCards, withoutAnalysis.roles)
        withoutAnalysis.roleCards.forEach { card ->
            assertEquals(card.id.displayOrder, card.displayOrder)
            assertTrue(card.roleLabel.isNotBlank())
            assertTrue(card.mission.isNotBlank())
            assertTrue(card.safeHandoffSentence.isNotBlank())
            assertTrue(card.category.label.isNotBlank())
            assertTrue(card.whenToUse.isNotBlank())
            assertTrue(card.doNotDoBoundaries.isNotEmpty())
            assertTrue(card.doNotDoBoundaries.all { boundary -> boundary.isNotBlank() })
            assertTrue(card.priority.label.isNotBlank())
            assertTrue(card.primaryAction.label.isNotBlank())
            assertTrue(card.secondaryActions.isNotEmpty())
            assertEquals(card.primaryAction, card.routeActions.first())
            assertEquals(
                card.routeActions.size,
                card.routeActions.map { action -> action.id }.distinct().size
            )
        }
        assertEquals(
            FamilyResponseRolePriority.SHARE_WARNING,
            withoutAnalysis.roleCards.single { card ->
                card.id == FamilyResponseRoleId.FAMILY_ALERT_RELAY
            }.priority
        )

        val coveredActionIds = (withoutAnalysis.roleCards + withAnalysis.roleCards)
            .flatMap { card -> card.routeActions }
            .map { action -> action.id }
            .toSet()
        assertEquals(FamilyResponseRoleActionId.entries.toSet(), coveredActionIds)
    }

    @Test
    fun copyStatesGenericManualRolesWithoutAskingForNamesContactsSecretsAccountsOrRawLinks() {
        val board = FamilyResponseRoleBoardBuilder.build(null, lowReadiness())
        val copy = allCopy(board)

        assertTrue(board.intro.contains("일반 역할"))
        assertTrue(board.intro.contains("로컬 전용·무상태 수동 안내"))
        assertTrue(board.intro.contains("담당자를 배정") && board.intro.contains("추적하지 않"))
        listOf(
            "이름을 입력하세요",
            "전화번호를 입력하세요",
            "비밀번호를 입력하세요",
            "OTP를 입력하세요",
            "PIN을 입력하세요",
            "안전단어 답을 입력하세요",
            "계좌번호를 입력하세요",
            "원문 링크를 입력하세요",
            "가족 이름을 적어 주세요",
            "담당자 이름을 저장하세요"
        ).forEach { request ->
            assertFalse("copy requested a forbidden value: $request", copy.contains(request))
        }
    }

    @Test
    fun shareUsesOnlyGenericRoleCategoryActionAndHandoffCopyWithoutInputOrReadinessValues() {
        val safeWord = "해질녘 파란 우산은 어디에?"
        val callbackScript = "푸른 메모장을 열고 첫 저장 번호로 전화한다."
        val contactLabel = "보호자-은하수"
        val contactNumber = "010-2468-1357"
        val raw =
            "RAW-ROLE-COMET 전화 010-9876-5432 계좌 123456789012 비밀번호 BluePassword77 " +
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
        val board = FamilyResponseRoleBoardBuilder.build(analysis, readiness, incidentCount = 654321)
        val summary = board.familyShareSummary
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)

        board.roleCards.forEach { card ->
            assertTrue("share omitted role: ${card.id}", summary.contains(card.roleLabel))
            assertTrue("share omitted category: ${card.id}", summary.contains(card.category.label))
            assertTrue("share omitted handoff: ${card.id}", summary.contains(card.safeHandoffSentence))
            card.routeActions.forEach { action ->
                assertTrue("share omitted action: ${action.id}", summary.contains(action.label))
            }
            assertFalse("share included mission: ${card.id}", summary.contains(card.mission))
            assertFalse("share included when-to-use: ${card.id}", summary.contains(card.whenToUse))
            card.doNotDoBoundaries.forEach { boundary ->
                assertFalse("share included detailed boundary: $boundary", summary.contains(boundary))
            }
        }
        assertFalse(summary.contains(board.recommendationExplanation))
        assertFalse(summary.contains(raw))
        assertFalse(summary.contains(redactedClue))

        listOf(
            "RAW-ROLE-COMET",
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
            assertFalse("role-board share leaked: $secret", summary.contains(secret))
            assertFalse("role-board output leaked: $secret", allCopy(board).contains(secret))
        }
        assertFalse(phonePattern.containsMatchIn(summary))
        assertFalse(longNumberPattern.containsMatchIn(summary))
        assertFalse(groupedNumberPattern.containsMatchIn(summary))
        assertFalse(urlDomainPattern.containsMatchIn(summary))
        assertFalse(residentPattern.containsMatchIn(summary))
    }

    @Test
    fun outputIsDeterministicForSameInputsAndNegativeIncidentCountMatchesZero() {
        val analysis = analysisWith(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.APP_INSTALL,
            ScamSignal.LINK,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.SECRECY,
            riskLevel = RiskLevel.HIGH
        )
        val readiness = readyReadiness()

        val first = FamilyResponseRoleBoardBuilder.build(analysis, readiness, incidentCount = 3)
        val second = FamilyResponseRoleBoardBuilder.build(analysis, readiness, incidentCount = 3)
        assertEquals(first, second)
        assertEquals(first.recommendedRoles, second.recommendedRoles)
        assertEquals(first.recommendedActionIds, second.recommendedActionIds)
        assertEquals(first.familyShareSummary, second.familyShareSummary)

        val negative = FamilyResponseRoleBoardBuilder.build(analysis, readiness, incidentCount = -5)
        val zero = FamilyResponseRoleBoardBuilder.build(analysis, readiness, incidentCount = 0)
        assertEquals(zero, negative)
    }

    @Test
    fun privacyCopyStatesManualLocalNoStorageContactReadingAutoSendMonitoringNetworkMutationOrScheduler() {
        representativeBoards().forEach { board ->
            val copy = board.privacySafetyNotes.joinToString(" ")

            assertTrue(copy.contains("일반 역할"))
            assertTrue(copy.contains("로컬 전용·무상태 수동 안내"))
            assertTrue(copy.contains("가족 이름·연락처"))
            assertTrue(copy.contains("역할 담당자·선택"))
            assertTrue(copy.contains("체크 표시·완료 상태"))
            assertTrue(copy.contains("읽음·진행 상태"))
            assertTrue(copy.contains("공유 이력·역할 보드 이력"))
            assertTrue(copy.contains("저장") && copy.contains("않습니다"))
            assertTrue(copy.contains("문자·메시지·통화·연락처"))
            assertTrue(copy.contains("읽거나 감시하지 않"))
            assertTrue(copy.contains("자동으로 보내지 않"))
            assertTrue(copy.contains("네트워크"))
            assertTrue(copy.contains("새 Android 권한"))
            assertTrue(copy.contains("사건 기록을 자동으로 추가·수정·삭제하지 않습니다"))
            assertTrue(copy.contains("알림·알람·캘린더 일정·리마인더"))
            assertTrue(copy.contains("백그라운드 워커"))
            assertTrue(copy.contains("만들거나 예약하지 않습니다"))
        }
    }

    @Test
    fun copyDoesNotImplyProofMonitoringBlockingRemovalFreezeRecoveryLegalGuaranteeDeletionAssignmentOrSecrets() {
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
            "검증을 완료했습니다",
            "자동으로 감시합니다",
            "자동 모니터링합니다",
            "자동으로 검사합니다",
            "자동 스캔합니다",
            "연락처를 자동으로 읽습니다",
            "서버로 업로드합니다",
            "자동으로 전송합니다",
            "전화를 자동으로 겁니다",
            "문자를 자동으로 차단합니다",
            "전화를 자동으로 차단합니다",
            "악성 앱을 자동으로 제거합니다",
            "악성코드를 제거합니다",
            "악성코드 제거를 보장합니다",
            "계좌를 자동으로 지급정지합니다",
            "카드를 자동으로 정지합니다",
            "계정을 자동으로 잠급니다",
            "피해금 회수를 보장합니다",
            "피해금을 회수합니다",
            "법률 자문을 제공합니다",
            "법적 결과를 보장합니다",
            "안전을 보장합니다",
            "자동으로 삭제합니다",
            "담당자 이름을 저장합니다",
            "역할 배정을 저장합니다",
            "비밀값에 접근합니다",
            "100%"
        )

        representativeBoards().forEach { board ->
            val copy = allCopy(board)
            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "role-board copy contained unsafe claim: $claim",
                    copy.lowercase().contains(claim.lowercase())
                )
            }
        }
    }

    private fun representativeBoards(): List<FamilyResponseRoleBoard> = listOf(
        FamilyResponseRoleBoardBuilder.build(null, lowReadiness()),
        FamilyResponseRoleBoardBuilder.build(
            analysisWith(ScamSignal.APP_INSTALL),
            inProgressReadiness()
        ),
        FamilyResponseRoleBoardBuilder.build(
            analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER),
            readyReadiness(),
            incidentCount = 2
        ),
        FamilyResponseRoleBoardBuilder.build(
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

    private fun cardCopy(card: FamilyResponseRoleCard): String = buildString {
        append(card.roleLabel)
        append(card.mission)
        append(card.safeHandoffSentence)
        append(card.category.label)
        append(card.whenToUse)
        card.doNotDoBoundaries.forEach(::append)
        append(card.priority.label)
        card.routeActions.forEach { action -> append(action.label) }
    }

    private fun allCopy(board: FamilyResponseRoleBoard): String = buildString {
        append(board.title)
        append(board.intro)
        append(board.recommendationTitle)
        append(board.recommendationExplanation)
        board.recommendedActions.forEach { action -> append(action.label) }
        board.roleCards.forEach { card -> append(cardCopy(card)) }
        board.privacySafetyNotes.forEach(::append)
        append(board.familyShareSummary)
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
