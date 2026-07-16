package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecontactBoundaryCoachTest {
    @Test
    fun noAnalysisBoardIsConservativeWithoutDeclaringAnythingSafeVerifiedResolvedOrBlocked() {
        val coach = RecontactBoundaryCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(RecontactBoundaryCardId.entries, coach.recommendedCardIds)
        assertTrue(
            coach.recommendedActionIds.containsAll(
                listOf(
                    RecontactBoundaryActionId.QUICK_STOP,
                    RecontactBoundaryActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                    RecontactBoundaryActionId.CHECKER,
                    RecontactBoundaryActionId.PRIVACY_CENTER,
                    RecontactBoundaryActionId.EXPOSURE_IMPACT_TRIAGE,
                    RecontactBoundaryActionId.FAMILY_CALLBACK_CARD,
                    RecontactBoundaryActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                    RecontactBoundaryActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD,
                    RecontactBoundaryActionId.SAFE_CALL_SCRIPTS,
                    RecontactBoundaryActionId.FAMILY_ALERT_MESSAGES,
                    RecontactBoundaryActionId.TRUSTED_HELPER_REVIEW,
                    RecontactBoundaryActionId.MONEY_TRANSFER_PAUSE_GATE,
                    RecontactBoundaryActionId.ACCOUNT_LOCKDOWN,
                    RecontactBoundaryActionId.REMOTE_ACCESS_CUTOFF_COACH,
                    RecontactBoundaryActionId.EVIDENCE_KIT,
                    RecontactBoundaryActionId.OFFICIAL_HELP_DIRECTORY,
                    RecontactBoundaryActionId.RECOVERY_PLAN,
                    RecontactBoundaryActionId.FOLLOW_UP_CHECK_IN_PLANNER,
                    RecontactBoundaryActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                    RecontactBoundaryActionId.SAFETY_FLOW_ROUTER
                )
            )
        )
        assertFalse(RecontactBoundaryActionId.ANALYSIS_RESULT in coach.recommendedActionIds)
        assertFalse(RecontactBoundaryActionId.REPORT_PREP in coach.recommendedActionIds)
        assertTrue(coach.recommendationExplanation.contains("현재 분석이 없거나 일치 신호가 없습니다"))
        assertTrue(coach.recommendationExplanation.contains("문제가 없다는 판정으로 보지 말고"))

        val copy = allCopy(coach)
        listOf(
            "안전합니다",
            "검증됐습니다",
            "공식입니다",
            "공식으로 확인됐습니다",
            "문제없습니다",
            "사기가 아닙니다",
            "해결됐습니다",
            "차단됐습니다",
            "음소거됐습니다",
            "100%",
            " safe ",
            "verified",
            "officially confirmed",
            "resolved",
            "problem-free",
            "blocked",
            "silenced"
        ).forEach { unsafeClaim ->
            assertFalse(
                "default coach made unsafe claim: $unsafeClaim",
                copy.contains(unsafeClaim, ignoreCase = true)
            )
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendNonResponseKnownCallbackAlertAndVerificationRoutes() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val coach = RecontactBoundaryCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY, "PLAN_VALUE_010-1111-2222")
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        RecontactBoundaryCardId.SAME_CHANNEL_NON_RESPONSE,
                        RecontactBoundaryCardId.NEW_NUMBER_PROFILE_LINK_SEPARATE_PATH,
                        RecontactBoundaryCardId.FAMILY_FOLLOW_UP_PROMISE
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RecontactBoundaryActionId.FAMILY_CALLBACK_CARD,
                        RecontactBoundaryActionId.SAFE_CALL_SCRIPTS,
                        RecontactBoundaryActionId.FAMILY_ALERT_MESSAGES,
                        RecontactBoundaryActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                        RecontactBoundaryActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    )
                )
            )
            assertTrue(
                coach.card(RecontactBoundaryCardId.SAME_CHANNEL_NON_RESPONSE)
                    .boundaryScript.contains("이 경로에서는 더 답하지 않겠습니다")
            )
            assertTrue(
                coach.recommendationExplanation.contains(
                    "같은 경로에 답하지 않고 평소 가족 콜백"
                )
            )
            assertFalse(allCopy(coach).contains("PLAN_VALUE_010-1111-2222"))
            assertFalse(allCopy(coach).contains("안전 질문 답을 알려 주세요"))
        }
    }

    @Test
    fun impersonationAndSecrecyRecommendNonSecretFamilyExitHelpAndSeparateRoutes() {
        listOf(ScamSignal.IMPERSONATION, ScamSignal.SECRECY).forEach { signal ->
            val coach = RecontactBoundaryCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                RecontactBoundaryCardId.SECRECY_PRESSURE_FAMILY_ALERT in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RecontactBoundaryActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                        RecontactBoundaryActionId.FAMILY_ALERT_MESSAGES,
                        RecontactBoundaryActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                        RecontactBoundaryActionId.OFFICIAL_HELP_DIRECTORY,
                        RecontactBoundaryActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("재연락을 혼자 숨기지 않고"))
            assertTrue(
                coach.card(RecontactBoundaryCardId.SECRECY_PRESSURE_FAMILY_ALERT)
                    .boundaryScript.contains("이 연락은 숨기지 않겠습니다")
            )
        }
    }

    @Test
    fun highRiskAndFinancialSignalsRecommendPauseAccountEvidenceHelpReportRecoveryAndFollowUp() {
        val financialCases = listOf(
            analysis(RiskLevel.HIGH),
            analysis(RiskLevel.MEDIUM, ScamSignal.URGENT_TRANSFER),
            analysis(RiskLevel.MEDIUM, ScamSignal.BAIT),
            analysis(RiskLevel.MEDIUM, ScamSignal.GIFT_CRYPTO),
            analysis(RiskLevel.MEDIUM, ScamSignal.SAFE_ACCOUNT_FRAUD),
            analysis(RiskLevel.MEDIUM, ScamSignal.LOAN_REFINANCE_FEE),
            analysis(RiskLevel.MEDIUM, ScamSignal.GIFTICON_PIN),
            analysis(RiskLevel.MEDIUM, ScamSignal.OVERSEAS_PAYMENT_ALERT)
        )

        financialCases.forEach { current ->
            val coach = RecontactBoundaryCoachBuilder.build(
                analysis = current,
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                RecontactBoundaryCardId.MONEY_CREDENTIALS_STAY_PAUSED in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RecontactBoundaryActionId.MONEY_TRANSFER_PAUSE_GATE,
                        RecontactBoundaryActionId.ACCOUNT_LOCKDOWN,
                        RecontactBoundaryActionId.EVIDENCE_KIT,
                        RecontactBoundaryActionId.OFFICIAL_HELP_DIRECTORY,
                        RecontactBoundaryActionId.REPORT_PREP,
                        RecontactBoundaryActionId.RECOVERY_PLAN,
                        RecontactBoundaryActionId.FOLLOW_UP_CHECK_IN_PLANNER
                    )
                )
            )
            val copy = allCopy(coach)
            listOf(
                "결제를 취소했습니다",
                "송금을 취소했습니다",
                "계정을 동결했습니다",
                "카드를 정지했습니다",
                "환급을 보장합니다",
                "피해금을 회수했습니다",
                "복구를 보장합니다",
                "법률 결과를 보장합니다",
                "금융 결과를 보장합니다"
            ).forEach { guarantee -> assertFalse(copy.contains(guarantee)) }
        }

        val highWithoutFinancialSignal = RecontactBoundaryCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.FAMILY_EMERGENCY,
                ScamSignal.IMPERSONATION,
                ScamSignal.SECRECY
            ),
            readiness = readiness(ReadinessLevel.READY)
        )
        assertTrue(
            highWithoutFinancialSignal.recommendationTitle.contains(
                "높은 위험 신호에도 같은 경로를 닫고"
            )
        )
        assertFalse(highWithoutFinancialSignal.recommendationTitle.contains("돈 요구"))
    }

    @Test
    fun credentialsAndOtpRecommendAccountAndRemoteCutoffWithoutFreezeClaim() {
        listOf(
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = RecontactBoundaryCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                RecontactBoundaryCardId.MONEY_CREDENTIALS_STAY_PAUSED in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RecontactBoundaryActionId.ACCOUNT_LOCKDOWN,
                        RecontactBoundaryActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        RecontactBoundaryActionId.EVIDENCE_KIT,
                        RecontactBoundaryActionId.RECOVERY_PLAN
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("인증 비밀 재요구 가능성"))
            assertTrue(coach.recommendationExplanation.contains("자동 정지·동결하는 기능은 아닙니다"))
            assertFalse(allCopy(coach).contains("계정 동결 완료"))
            assertFalse(allCopy(coach).contains("카드 정지 완료"))
        }
    }

    @Test
    fun appInstallAndLiveCallControlRecommendRemoteHelperAccountAndEvidenceWithoutCleaningClaim() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val coach = RecontactBoundaryCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                RecontactBoundaryCardId.REMOTE_ACCESS_STAY_CLOSED in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RecontactBoundaryActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        RecontactBoundaryActionId.TRUSTED_HELPER_REVIEW,
                        RecontactBoundaryActionId.ACCOUNT_LOCKDOWN,
                        RecontactBoundaryActionId.EVIDENCE_KIT,
                        RecontactBoundaryActionId.RECOVERY_PLAN
                    )
                )
            )
            val copy = allCopy(coach)
            assertTrue(copy.contains("원격 연결 해제·악성 프로그램 제거·기기 정리"))
            assertFalse(copy.contains("원격 연결을 해제했습니다"))
            assertFalse(copy.contains("악성 프로그램을 제거했습니다"))
            assertFalse(copy.contains("기기 정리 완료"))
        }
    }

    @Test
    fun linkAndSmishingRecommendCheckerExposurePrivacyAndSeparateWithoutValidationClaim() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val coach = RecontactBoundaryCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                RecontactBoundaryCardId.NEW_NUMBER_PROFILE_LINK_SEPARATE_PATH in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RecontactBoundaryActionId.CHECKER,
                        RecontactBoundaryActionId.EXPOSURE_IMPACT_TRIAGE,
                        RecontactBoundaryActionId.PRIVACY_CENTER,
                        RecontactBoundaryActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                        RecontactBoundaryActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    )
                )
            )
            val card = coach.card(
                RecontactBoundaryCardId.NEW_NUMBER_PROFILE_LINK_SEPARATE_PATH
            )
            assertTrue(card.recontactScenario.contains("링크를 바꿔"))
            assertTrue(card.doNotRecordOrShareBoundaries.any { it.contains("URL·도메인") })
            assertTrue(
                coach.recommendationExplanation.contains(
                    "링크를 스캔하거나 URL·도메인을 검증하지 않습니다"
                )
            )
            assertFalse(allCopy(coach).contains("링크를 자동 검사합니다"))
            assertFalse(allCopy(coach).contains("URL 검증 완료"))
        }
    }

    @Test
    fun savedIncidentChangesRecommendationsWithoutReadingContentsAndClampsCounts() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.FAMILY_EMERGENCY)
        val ready = readiness(ReadinessLevel.READY)
        val zero = RecontactBoundaryCoachBuilder.build(current, ready, 0)
        val negative = RecontactBoundaryCoachBuilder.build(current, ready, -99)
        val one = RecontactBoundaryCoachBuilder.build(current, ready, 1)
        val many = RecontactBoundaryCoachBuilder.build(current, ready, Int.MAX_VALUE)

        assertEquals(zero, negative)
        assertEquals(one, many)
        assertNotEquals(zero, one)
        assertFalse(RecontactBoundaryActionId.EVIDENCE_KIT in zero.recommendedActionIds)
        assertTrue(RecontactBoundaryActionId.EVIDENCE_KIT in one.recommendedActionIds)
        assertTrue(RecontactBoundaryActionId.REPORT_PREP in one.recommendedActionIds)
        assertTrue(
            RecontactBoundaryActionId.FAMILY_CALM_DEBRIEF_AGENDA in
                one.recommendedActionIds
        )
        assertTrue(one.recommendationExplanation.contains("개수 범주만 반영"))
        assertTrue(one.recommendationExplanation.contains("사건 내용은 읽지 않습니다"))
        assertFalse(one.familyShareSummary.contains(Int.MAX_VALUE.toString()))
    }

    @Test
    fun lowAndInProgressReadinessAddSafeCallFamilySupportHelperAndRoadmapWhileReadyStaysCautious() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.LINK)
        val low = RecontactBoundaryCoachBuilder.build(
            current,
            readiness(ReadinessLevel.LOW)
        )
        val inProgress = RecontactBoundaryCoachBuilder.build(
            current,
            readiness(ReadinessLevel.IN_PROGRESS)
        )
        val ready = RecontactBoundaryCoachBuilder.build(
            current,
            readiness(ReadinessLevel.READY)
        )

        listOf(low, inProgress).forEach { coach ->
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RecontactBoundaryActionId.FAMILY_CALLBACK_CARD,
                        RecontactBoundaryActionId.SAFE_CALL_SCRIPTS,
                        RecontactBoundaryActionId.FAMILY_ALERT_MESSAGES,
                        RecontactBoundaryActionId.TRUSTED_HELPER_REVIEW,
                        RecontactBoundaryActionId.FAMILY_READINESS_ROADMAP,
                        RecontactBoundaryActionId.FAMILY_CALM_DEBRIEF_AGENDA
                    )
                )
            )
        }
        assertFalse(
            RecontactBoundaryActionId.FAMILY_READINESS_ROADMAP in
                ready.recommendedActionIds
        )
        assertTrue(low.recommendationExplanation.contains("가족 준비가 부족한 상태"))
        assertTrue(inProgress.recommendationExplanation.contains("가족 준비가 진행 중"))
        assertTrue(ready.recommendationExplanation.contains("가족 준비가 되어 있어도"))
        assertTrue(ready.recommendationExplanation.contains("재연락 결과를 확인했다는 뜻은 아닙니다"))
    }

    @Test
    fun everyCardHasCompleteFieldsStableOrderUniqueRoutesAndEveryActionMapsToAFlow() {
        val noAnalysis = RecontactBoundaryCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )
        val analyzed = RecontactBoundaryCoachBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2
        )
        val expectedTitles = listOf(
            "다시 전화·문자 오면: 같은 경로에서 응답하지 않기",
            "새 번호·새 프로필·새 링크로 오면: 별도 경로로 분리하기",
            "협박·비밀·기관 압박이 다시 오면: 가족에게 값 없이 알리기",
            "돈·OTP·계정·카드를 다시 요구하면: 돈·계정 행동 계속 멈추기",
            "앱 설치·화면공유·원격지시를 다시 요구하면: 기기 화면 닫기",
            "가족 후속 약속: 오늘·내일·이번 주 다시 확인하기"
        )

        listOf(noAnalysis, analyzed).forEach { coach ->
            assertEquals(RecontactBoundaryCardId.entries, coach.cards.map { it.id })
            assertEquals((1..6).toList(), coach.cards.map { it.displayOrder })
            assertEquals(expectedTitles, coach.cards.map { it.title })
            assertEquals(coach.recommendedCardIds.distinct(), coach.recommendedCardIds)
            assertEquals(
                coach.recommendedCardIds.sortedBy { it.displayOrder },
                coach.recommendedCardIds
            )
            assertEquals(coach.recommendedActionIds.distinct(), coach.recommendedActionIds)
            assertEquals(
                coach.recommendedActionIds.sortedBy { it.ordinal },
                coach.recommendedActionIds
            )

            coach.cards.forEach { card ->
                assertEquals(card.id.displayOrder, card.displayOrder)
                assertTrue(card.title.isNotBlank())
                assertTrue(card.recontactScenario.isNotBlank())
                assertTrue(card.recontactScenario.endsWith("면"))
                assertTrue(card.boundaryScript.isNotBlank())
                assertTrue(card.whyItMatters.isNotBlank())
                assertTrue(card.doNotRecordOrShareBoundaries.isNotEmpty())
                assertTrue(card.doNotRecordOrShareBoundaries.all { it.isNotBlank() })
                assertTrue(card.primaryAction.label.isNotBlank())
                assertTrue(card.secondaryActions.isNotEmpty())
                assertTrue(card.secondaryActions.all { it.label.isNotBlank() })
                assertEquals(
                    card.routeActions.map { it.id }.distinct(),
                    card.routeActions.map { it.id }
                )
            }
        }

        val usedActions = (noAnalysis.cards + analyzed.cards)
            .flatMap { card -> card.routeActions } +
            noAnalysis.recommendedActions + analyzed.recommendedActions
        assertEquals(
            RecontactBoundaryActionId.entries.toSet(),
            usedActions.map { action -> action.id }.toSet()
        )
    }

    @Test
    fun familyShareContainsOnlyFixedGenericCardAndRouteLabelsWithoutSecretsOrPatterns() {
        val rawInput =
            "RAW_RECONTACT 박민수 010-9876-5432 계좌 123456789012 카드 4111111111111111 " +
                "OTP 654321 PIN 7788 주민 900101-1234567 https://evil.example " +
                "ShadowRemote com.evil.remote DEVICE-ID-ABC987654 RECONTACT_RESULT"
        val readinessMarker =
            "PLAN_SECRET callback-secret contact-secret 070-1111-2222 " +
                "account-987654321012 https://private.example SAFE_WORD_VALUE"
        val coach = RecontactBoundaryCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                *ScamSignal.entries.toTypedArray(),
                input = rawInput
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, readinessMarker),
            incidentCount = Int.MAX_VALUE
        )
        val summary = coach.familyShareSummary

        val expectedLines = buildList {
            add("[가족 AI 보이스피싱 방패] 재연락 경계 코치")
            add("다시 연락해도 같은 경로에서 협상하지 않을 경계를 함께 보고 있습니다.")
            add("추천 경계 카드")
            coach.recommendedCards.forEach { card -> add("- ${card.title}") }
            add("직접 고를 기존 앱 경로")
            coach.recommendedActions.forEach { action -> add("- ${action.label}") }
            add("가족에게 알릴 값 없는 문장")
            add("- 같은 경로에는 답하지 않고 가족과 별도 앱 경로를 직접 고르고 있습니다.")
            add(
                "원문·사람·연락처·번호·링크·인증·계정·카드·기기·가족 계획·확인 답·" +
                    "재연락 결과값 없이 고정된 카드와 기존 앱 경로 이름만 담았습니다."
            )
        }
        assertEquals(
            expectedLines,
            summary.lineSequence().filter { it.isNotBlank() }.toList()
        )

        listOf(
            "RAW_RECONTACT",
            "박민수",
            "010-9876-5432",
            "123456789012",
            "4111111111111111",
            "654321",
            "7788",
            "900101-1234567",
            "evil.example",
            "ShadowRemote",
            "com.evil.remote",
            "DEVICE-ID-ABC987654",
            "RECONTACT_RESULT",
            "PLAN_SECRET",
            "callback-secret",
            "contact-secret",
            "070-1111-2222",
            "987654321012",
            "private.example",
            "SAFE_WORD_VALUE"
        ).forEach { secret ->
            assertFalse("family share leaked secret: $secret", summary.contains(secret))
        }
        assertFalse(Regex("01[016789][- .]?\\d{3,4}[- .]?\\d{4}").containsMatchIn(summary))
        assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(summary))
        assertFalse(
            Regex("(?i)https?://|www\\.|[a-z0-9-]+\\.(?:com|net|kr)")
                .containsMatchIn(summary)
        )
        assertFalse(Regex("\\d{6}[- ]?[1-4]\\d{6}").containsMatchIn(summary))
        assertFalse(
            Regex("(?i)DEVICE[-_ ]?ID\\s*[:=]?\\s*[A-Z0-9-]{6,}")
                .containsMatchIn(summary)
        )
    }

    @Test
    fun builderIsDeterministicAndIgnoresRawScoreAndReadinessLiteralFields() {
        val signals = listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.CREDENTIALS,
            ScamSignal.LINK
        )
        val firstAnalysis = ScamAnalysis(
            input = "FIRST_RAW 010-1111-2222 https://first.example 12345678",
            score = 31,
            riskLevel = RiskLevel.HIGH,
            matchedSignals = signals
        )
        val secondAnalysis = firstAnalysis.copy(
            input = "SECOND_RAW 010-9999-8888 https://second.example 87654321",
            score = 100
        )
        val firstReadiness = readiness(ReadinessLevel.IN_PROGRESS, "FIRST_PLAN_SECRET")
        val secondReadiness = FamilyReadiness(
            level = ReadinessLevel.IN_PROGRESS,
            title = "SECOND_PLAN_SECRET",
            completedCount = 999,
            totalCount = 1000,
            checklist = listOf(
                ReadinessItem(
                    label = "SECOND_CONTACT_SECRET",
                    done = true,
                    action = "SECOND_CALLBACK_SECRET"
                )
            ),
            nextAction = "SECOND_SAFE_WORD_SECRET"
        )

        val first = RecontactBoundaryCoachBuilder.build(firstAnalysis, firstReadiness, 1)
        val repeated = RecontactBoundaryCoachBuilder.build(firstAnalysis, firstReadiness, 1)
        val differentForbiddenValues = RecontactBoundaryCoachBuilder.build(
            secondAnalysis,
            secondReadiness,
            1
        )

        assertEquals(first, repeated)
        assertEquals(first, differentForbiddenValues)
        listOf(
            "FIRST_RAW",
            "SECOND_RAW",
            "FIRST_PLAN_SECRET",
            "SECOND_PLAN_SECRET",
            "SECOND_CONTACT_SECRET",
            "SECOND_CALLBACK_SECRET",
            "SECOND_SAFE_WORD_SECRET",
            "010-1111-2222",
            "first.example",
            "1000"
        ).forEach { ignoredValue ->
            assertFalse(allCopy(first).contains(ignoredValue))
            assertFalse(allCopy(differentForbiddenValues).contains(ignoredValue))
        }
    }

    @Test
    fun privacyCopyStatesManualLocalNoRecontactStorageNoAutomationAndNoMutation() {
        val coach = RecontactBoundaryCoachBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2
        )
        val copy = allCopy(coach)

        listOf(
            "로컬 전용·무상태 수동 안내",
            "재연락 결과 입력을 요구하지 않습니다",
            "새 저장소를 만들거나 기록하지 않습니다",
            "재연락 상태·경계 상태",
            "통화 기록",
            "연락처를 읽거나 쓰거나 고르지 않습니다",
            "공유를 자동 전송하지 않고",
            "네트워크·웹 탐색·지도·위치",
            "기관 번호 조회·공식 경로 조회",
            "전화번호 검증",
            "URL·도메인 검증",
            "링크 스캔",
            "새 Android 권한을 요청하지 않습니다",
            "스케줄러를 사용하지 않습니다",
            "자동 다이얼·발신·통화·종료",
            "통화를 선별·차단·음소거하지 않으며",
            "문자·SMS를 보내거나 읽지 않습니다",
            "알림을 필터링하거나 재연락을 모니터링하지 않습니다",
            "클립보드를 읽거나 쓰지 않고",
            "결제·은행 기능이나 API",
            "기기 제어 기능을 사용하지 않고",
            "추가·수정·삭제하지 않습니다",
            "자동으로 가리거나 모니터링·스캔·삭제·차단·호출·메시지 전송·조회·확인·검증·저장하지 않습니다",
            "증명하거나 보장하지 않습니다"
        ).forEach { expectedBoundary ->
            assertTrue("privacy copy omitted: $expectedBoundary", copy.contains(expectedBoundary))
        }
        assertTrue(
            copy.contains("원문·가린 단서·사건 내용·가족 안전 계획 값을 사용하지 않습니다")
        )
        assertTrue(copy.contains("재연락·경계 상태"))
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndPositiveAutomaticOutcomeClaims() {
        val coaches = listOf(
            RecontactBoundaryCoachBuilder.build(null, readiness(ReadinessLevel.READY)),
            RecontactBoundaryCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.LOW),
                3
            )
        )
        val forbiddenClaims = listOf(
            "100%",
            " safe ",
            "verified",
            "officially confirmed",
            "resolved",
            "blocked",
            "silenced",
            "안전합니다",
            "문제없습니다",
            "본인 확인 완료",
            "신원이 확인됐습니다",
            "공식 번호로 확인됐습니다",
            "공식 링크입니다",
            "링크 검증 완료",
            "번호 검증 완료",
            "통화를 차단했습니다",
            "문자를 차단했습니다",
            "알림을 음소거했습니다",
            "원격 접근이 끊겼습니다",
            "악성 프로그램을 제거했습니다",
            "기기 정리 완료",
            "계정을 자동으로 잠급니다",
            "계정이 동결됐습니다",
            "카드를 자동으로 정지합니다",
            "결제를 자동으로 취소합니다",
            "송금을 자동으로 취소합니다",
            "환급을 보장합니다",
            "피해금을 회수합니다",
            "복구를 보장합니다",
            "법률 결과를 보장합니다",
            "금융 결과를 보장합니다",
            "자동으로 모니터링합니다",
            "자동으로 감시합니다",
            "자동으로 스캔합니다",
            "자동으로 검사합니다",
            "자동으로 삭제합니다",
            "자동으로 가립니다",
            "자동으로 차단합니다",
            "자동으로 전화합니다",
            "메시지를 자동 전송합니다",
            "문자를 자동 전송합니다",
            "기관을 자동 조회합니다",
            "번호를 자동 검증합니다",
            "링크를 자동 검증합니다",
            "재연락 결과를 자동 저장합니다",
            "본인을 자동 확인합니다"
        )

        coaches.forEach { coach ->
            val copy = allCopy(coach)
            forbiddenClaims.forEach { claim ->
                assertFalse(
                    "coach made unsafe claim: $claim",
                    copy.contains(claim, ignoreCase = true)
                )
            }
            listOf(
                Regex("자동(?:으로)?\\s*(?:차단|동결|정지|삭제|가림|감시|스캔|검사|전송|전화|조회|확인|검증|저장)(?:합니다|됩니다|완료)"),
                Regex("(?:확인|검증|연결 해제|차단|동결|정지|삭제|제거|정리|복구|회복)\\s*(?:완료|됐습니다|되었습니다)"),
                Regex("(?:번호|링크|URL|기관|본인|재연락).{0,12}(?:확인 완료|검증 완료|공식 확인|저장 완료)"),
                Regex("(?:송금|이체|결제).{0,12}자동으로\\s*(?:실행|취소|차단)합니다")
            ).forEach { positiveAutomationPattern ->
                assertFalse(
                    "coach matched positive automation claim: ${positiveAutomationPattern.pattern}",
                    positiveAutomationPattern.containsMatchIn(copy)
                )
            }
        }
    }

    @Test
    fun unrelatedSignalCategoriesProduceDifferentFocusedRecommendationsAndScenariosStayHypothetical() {
        fun coachFor(signal: ScamSignal) = RecontactBoundaryCoachBuilder.build(
            analysis = analysis(RiskLevel.MEDIUM, signal),
            readiness = readiness(ReadinessLevel.READY)
        )

        val family = coachFor(ScamSignal.FAMILY_EMERGENCY)
        val financial = coachFor(ScamSignal.URGENT_TRANSFER)
        val link = coachFor(ScamSignal.LINK)
        val remote = coachFor(ScamSignal.APP_INSTALL)

        assertNotEquals(family.recommendedCardIds, financial.recommendedCardIds)
        assertNotEquals(family.recommendedActionIds, financial.recommendedActionIds)
        assertNotEquals(link.recommendedActionIds, remote.recommendedActionIds)
        assertTrue(
            RecontactBoundaryCardId.NEW_NUMBER_PROFILE_LINK_SEPARATE_PATH in
                family.recommendedCardIds
        )
        assertTrue(
            RecontactBoundaryActionId.MONEY_TRANSFER_PAUSE_GATE in
                financial.recommendedActionIds
        )
        assertTrue(RecontactBoundaryActionId.PRIVACY_CENTER in link.recommendedActionIds)
        assertTrue(
            RecontactBoundaryActionId.REMOTE_ACCESS_CUTOFF_COACH in
                remote.recommendedActionIds
        )

        remote.cards.forEach { card ->
            assertTrue(
                "scenario is not hypothetical: ${card.id}",
                card.recontactScenario.endsWith("면")
            )
            assertFalse(Regex("(?i)https?://|www\\.").containsMatchIn(card.recontactScenario))
            assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(card.recontactScenario))
        }
    }

    private fun RecontactBoundaryCoach.card(
        id: RecontactBoundaryCardId
    ): RecontactBoundaryCard = cards.single { card -> card.id == id }

    private fun analysis(
        riskLevel: RiskLevel,
        vararg signals: ScamSignal,
        input: String = "raw input that must never be used"
    ): ScamAnalysis = ScamAnalysis(
        input = input,
        score = when (riskLevel) {
            RiskLevel.SAFE -> 0
            RiskLevel.MEDIUM -> 45
            RiskLevel.HIGH -> 90
        },
        riskLevel = riskLevel,
        matchedSignals = signals.toList()
    )

    private fun readiness(
        level: ReadinessLevel,
        marker: String = "unused readiness literal"
    ): FamilyReadiness = FamilyReadiness(
        level = level,
        title = "readiness title $marker",
        completedCount = 1,
        totalCount = 4,
        checklist = listOf(
            ReadinessItem(
                label = "contact label $marker",
                done = false,
                action = "callback and safe word $marker"
            )
        ),
        nextAction = "next action $marker"
    )

    private fun allCopy(coach: RecontactBoundaryCoach): String = buildString {
        append(coach.title)
        append(coach.intro)
        append(coach.modeLabel)
        append(coach.recommendationTitle)
        append(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> append(action.label) }
        coach.cards.forEach { card ->
            append(card.title)
            append(card.recontactScenario)
            append(card.boundaryScript)
            append(card.whyItMatters)
            card.doNotRecordOrShareBoundaries.forEach(::append)
            card.routeActions.forEach { action -> append(action.label) }
        }
        coach.privacySafetyNotes.forEach(::append)
        append(coach.familyShareSummary)
    }
}
