package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VerificationOutcomeNextStepsBoardTest {
    @Test
    fun noAnalysisBoardIsConservativeWithoutDeclaringAnythingSafeVerifiedOrResolved() {
        val board = VerificationOutcomeNextStepsBoardBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(VerificationOutcomeNextStepsCardId.entries, board.recommendedCardIds)
        assertTrue(
            board.recommendedActionIds.containsAll(
                listOf(
                    VerificationOutcomeNextStepsActionId.QUICK_STOP,
                    VerificationOutcomeNextStepsActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                    VerificationOutcomeNextStepsActionId.CHECKER,
                    VerificationOutcomeNextStepsActionId.PRIVACY_CENTER,
                    VerificationOutcomeNextStepsActionId.EXPOSURE_IMPACT_TRIAGE,
                    VerificationOutcomeNextStepsActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                    VerificationOutcomeNextStepsActionId.SAFE_CALL_SCRIPTS,
                    VerificationOutcomeNextStepsActionId.FAMILY_ALERT_MESSAGES,
                    VerificationOutcomeNextStepsActionId.TRUSTED_HELPER_REVIEW,
                    VerificationOutcomeNextStepsActionId.OFFICIAL_HELP_DIRECTORY,
                    VerificationOutcomeNextStepsActionId.FOLLOW_UP_CHECK_IN_PLANNER,
                    VerificationOutcomeNextStepsActionId.SAFETY_FLOW_ROUTER
                )
            )
        )
        assertTrue(board.recommendationExplanation.contains("현재 분석이 없거나 일치 신호가 없습니다"))
        assertTrue(board.recommendationExplanation.contains("문제가 없다는 판정으로 보지 말고"))

        val copy = allCopy(board)
        listOf(
            "안전합니다",
            "검증됐습니다",
            "공식입니다",
            "공식으로 확인됐습니다",
            "문제없습니다",
            "사기가 아닙니다",
            "해결됐습니다",
            "복구됐습니다",
            "100%",
            " safe ",
            "verified",
            "officially confirmed",
            "resolved",
            "recovered",
            "problem-free",
            "blocked"
        ).forEach { unsafeClaim ->
            assertFalse(
                "default board made unsafe claim: $unsafeClaim",
                copy.contains(unsafeClaim, ignoreCase = true)
            )
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendUnreachedNotFromThemAndFamilyRoutes() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val board = VerificationOutcomeNextStepsBoardBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY, "PLAN_VALUE_010-1111-2222")
            )

            assertTrue(
                board.recommendedCardIds.containsAll(
                    listOf(
                        VerificationOutcomeNextStepsCardId.UNREACHED_KEEP_STOPPED,
                        VerificationOutcomeNextStepsCardId.NOT_FROM_THEM_FAMILY_WARNING,
                        VerificationOutcomeNextStepsCardId.VALUE_FREE_FAMILY_UPDATE
                    )
                )
            )
            assertTrue(
                board.recommendedActionIds.containsAll(
                    listOf(
                        VerificationOutcomeNextStepsActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                        VerificationOutcomeNextStepsActionId.SAFE_CALL_SCRIPTS,
                        VerificationOutcomeNextStepsActionId.FAMILY_ALERT_MESSAGES
                    )
                )
            )
            assertTrue(board.card(VerificationOutcomeNextStepsCardId.UNREACHED_KEEP_STOPPED)
                .outcomeCondition.contains("아직 연락이 닿지 않으면"))
            assertTrue(board.card(VerificationOutcomeNextStepsCardId.NOT_FROM_THEM_FAMILY_WARNING)
                .outcomeCondition.contains("그 가족에게서 온 것이 아니라고 들으면"))
            assertTrue(board.recommendationExplanation.contains("값 없는 가족 경고·확인 전화·별도 경로"))
            assertFalse(allCopy(board).contains("PLAN_VALUE_010-1111-2222"))
        }
    }

    @Test
    fun impersonationAndSecrecyRecommendExitOfficialHelpAndNonSecretFamilyUpdate() {
        listOf(ScamSignal.IMPERSONATION, ScamSignal.SECRECY).forEach { signal ->
            val board = VerificationOutcomeNextStepsBoardBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                board.recommendedCardIds.containsAll(
                    listOf(
                        VerificationOutcomeNextStepsCardId.NOT_FROM_THEM_FAMILY_WARNING,
                        VerificationOutcomeNextStepsCardId.USER_FOUND_INSTITUTION_PATH,
                        VerificationOutcomeNextStepsCardId.VALUE_FREE_FAMILY_UPDATE
                    )
                )
            )
            assertTrue(
                board.recommendedActionIds.containsAll(
                    listOf(
                        VerificationOutcomeNextStepsActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                        VerificationOutcomeNextStepsActionId.FAMILY_ALERT_MESSAGES,
                        VerificationOutcomeNextStepsActionId.OFFICIAL_HELP_DIRECTORY,
                        VerificationOutcomeNextStepsActionId.FAMILY_CALM_DEBRIEF_AGENDA
                    )
                )
            )
            assertTrue(board.recommendationExplanation.contains("결과를 혼자 감추지 않고"))
            assertTrue(board.card(VerificationOutcomeNextStepsCardId.NOT_FROM_THEM_FAMILY_WARNING)
                .nextStepSentence.contains("결과를 비밀로 두지 말고"))
        }
    }

    @Test
    fun highRiskAndFinancialSignalsRecommendMoneyAccountEvidenceHelpReportRecoveryAndFollowUp() {
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
            val board = VerificationOutcomeNextStepsBoardBuilder.build(
                analysis = current,
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                VerificationOutcomeNextStepsCardId.MONEY_CREDENTIAL_ACCOUNT_EXPOSURE in
                    board.recommendedCardIds
            )
            assertTrue(
                board.recommendedActionIds.containsAll(
                    listOf(
                        VerificationOutcomeNextStepsActionId.MONEY_TRANSFER_PAUSE_GATE,
                        VerificationOutcomeNextStepsActionId.ACCOUNT_LOCKDOWN,
                        VerificationOutcomeNextStepsActionId.EVIDENCE_KIT,
                        VerificationOutcomeNextStepsActionId.OFFICIAL_HELP_DIRECTORY,
                        VerificationOutcomeNextStepsActionId.REPORT_PREP,
                        VerificationOutcomeNextStepsActionId.RECOVERY_PLAN,
                        VerificationOutcomeNextStepsActionId.FOLLOW_UP_CHECK_IN_PLANNER
                    )
                )
            )
            val copy = allCopy(board)
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
    }

    @Test
    fun credentialsAndOtpRecommendAccountAndRemoteCutoffWithoutFreezeClaim() {
        listOf(
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val board = VerificationOutcomeNextStepsBoardBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                VerificationOutcomeNextStepsCardId.MONEY_CREDENTIAL_ACCOUNT_EXPOSURE in
                    board.recommendedCardIds
            )
            assertTrue(
                board.recommendedActionIds.containsAll(
                    listOf(
                        VerificationOutcomeNextStepsActionId.ACCOUNT_LOCKDOWN,
                        VerificationOutcomeNextStepsActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        VerificationOutcomeNextStepsActionId.EVIDENCE_KIT,
                        VerificationOutcomeNextStepsActionId.RECOVERY_PLAN
                    )
                )
            )
            assertTrue(board.recommendationExplanation.contains("인증 비밀 요구 가능성"))
            assertTrue(board.recommendationExplanation.contains("자동 정지·동결하는 기능은 아닙니다"))
            assertFalse(allCopy(board).contains("계정 동결 완료"))
            assertFalse(allCopy(board).contains("카드 정지 완료"))
        }
    }

    @Test
    fun appInstallAndLiveCallControlRecommendRemoteHelperAccountAndEvidenceWithoutCleaningClaim() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val board = VerificationOutcomeNextStepsBoardBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                VerificationOutcomeNextStepsCardId.LINK_APP_SCREEN_SHARE_EXPOSURE in
                    board.recommendedCardIds
            )
            assertTrue(
                board.recommendedActionIds.containsAll(
                    listOf(
                        VerificationOutcomeNextStepsActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        VerificationOutcomeNextStepsActionId.TRUSTED_HELPER_REVIEW,
                        VerificationOutcomeNextStepsActionId.ACCOUNT_LOCKDOWN,
                        VerificationOutcomeNextStepsActionId.EVIDENCE_KIT,
                        VerificationOutcomeNextStepsActionId.RECOVERY_PLAN
                    )
                )
            )
            val copy = allCopy(board)
            assertTrue(copy.contains("원격 연결 해제·악성 프로그램 제거·기기 정리"))
            assertFalse(copy.contains("원격 연결을 해제했습니다"))
            assertFalse(copy.contains("악성 프로그램을 제거했습니다"))
            assertFalse(copy.contains("기기 정리 완료"))
        }
    }

    @Test
    fun linkAndSmishingRecommendCheckerExposurePrivacyWithoutScanOrValidationClaim() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val board = VerificationOutcomeNextStepsBoardBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                VerificationOutcomeNextStepsCardId.LINK_APP_SCREEN_SHARE_EXPOSURE in
                    board.recommendedCardIds
            )
            assertTrue(
                board.recommendedActionIds.containsAll(
                    listOf(
                        VerificationOutcomeNextStepsActionId.CHECKER,
                        VerificationOutcomeNextStepsActionId.EXPOSURE_IMPACT_TRIAGE,
                        VerificationOutcomeNextStepsActionId.PRIVACY_CENTER
                    )
                )
            )
            val card = board.card(
                VerificationOutcomeNextStepsCardId.LINK_APP_SCREEN_SHARE_EXPOSURE
            )
            assertTrue(card.outcomeCondition.contains("링크 열기"))
            assertTrue(card.doNotRecordOrShareBoundaries.any { it.contains("정확한 URL·도메인") })
            assertTrue(board.recommendationExplanation.contains("링크를 스캔하거나 URL·도메인을 검증하지 않습니다"))
            assertFalse(allCopy(board).contains("링크를 자동 검사합니다"))
            assertFalse(allCopy(board).contains("URL 검증 완료"))
        }
    }

    @Test
    fun savedIncidentChangesRecommendationsWithoutReadingContentsAndClampsCounts() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.FAMILY_EMERGENCY)
        val ready = readiness(ReadinessLevel.READY)
        val zero = VerificationOutcomeNextStepsBoardBuilder.build(current, ready, 0)
        val negative = VerificationOutcomeNextStepsBoardBuilder.build(current, ready, -99)
        val one = VerificationOutcomeNextStepsBoardBuilder.build(current, ready, 1)
        val many = VerificationOutcomeNextStepsBoardBuilder.build(current, ready, Int.MAX_VALUE)

        assertEquals(zero, negative)
        assertEquals(one, many)
        assertNotEquals(zero, one)
        assertFalse(VerificationOutcomeNextStepsActionId.EVIDENCE_KIT in zero.recommendedActionIds)
        assertTrue(VerificationOutcomeNextStepsActionId.EVIDENCE_KIT in one.recommendedActionIds)
        assertTrue(VerificationOutcomeNextStepsActionId.REPORT_PREP in one.recommendedActionIds)
        assertTrue(
            VerificationOutcomeNextStepsCardId.USER_FOUND_INSTITUTION_PATH in
                one.recommendedCardIds
        )
        assertTrue(one.recommendationExplanation.contains("개수 범주만 반영"))
        assertTrue(one.recommendationExplanation.contains("사건 내용은 읽지 않습니다"))
        assertFalse(one.familyShareSummary.contains(Int.MAX_VALUE.toString()))
    }

    @Test
    fun lowAndInProgressReadinessAddHelperFamilyAndRoadmapWhileReadyStaysCautious() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.LINK)
        val low = VerificationOutcomeNextStepsBoardBuilder.build(
            current,
            readiness(ReadinessLevel.LOW)
        )
        val inProgress = VerificationOutcomeNextStepsBoardBuilder.build(
            current,
            readiness(ReadinessLevel.IN_PROGRESS)
        )
        val ready = VerificationOutcomeNextStepsBoardBuilder.build(
            current,
            readiness(ReadinessLevel.READY)
        )

        listOf(low, inProgress).forEach { board ->
            assertTrue(
                board.recommendedActionIds.containsAll(
                    listOf(
                        VerificationOutcomeNextStepsActionId.SAFE_CALL_SCRIPTS,
                        VerificationOutcomeNextStepsActionId.FAMILY_ALERT_MESSAGES,
                        VerificationOutcomeNextStepsActionId.TRUSTED_HELPER_REVIEW,
                        VerificationOutcomeNextStepsActionId.FAMILY_READINESS_ROADMAP,
                        VerificationOutcomeNextStepsActionId.FAMILY_CALM_DEBRIEF_AGENDA
                    )
                )
            )
        }
        assertFalse(
            VerificationOutcomeNextStepsActionId.FAMILY_READINESS_ROADMAP in
                ready.recommendedActionIds
        )
        assertTrue(low.recommendationExplanation.contains("가족 준비가 부족한 상태"))
        assertTrue(inProgress.recommendationExplanation.contains("가족 준비가 진행 중"))
        assertTrue(ready.recommendationExplanation.contains("가족 준비가 되어 있어도"))
        assertTrue(ready.recommendationExplanation.contains("결과를 확인했다는 뜻은 아닙니다"))
    }

    @Test
    fun everyCardHasCompleteFieldsStableOrderUniqueRoutesAndEveryActionMapsToAFlow() {
        val noAnalysis = VerificationOutcomeNextStepsBoardBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )
        val analyzed = VerificationOutcomeNextStepsBoardBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2
        )
        val expectedTitles = listOf(
            "아직 확인 안 됨: 계속 멈춤 유지",
            "가족 경로가 아니라고 확인되면: 가족 경고와 증거",
            "기관·은행 경로 확인 뒤: 공식 도움·신고 준비",
            "돈·인증·계정 노출 가능성: 계정·송금 수습",
            "링크·앱·화면공유 노출 가능성: 원격·노출 점검",
            "가족에게 값 없는 결과 공유와 후속 점검"
        )

        listOf(noAnalysis, analyzed).forEach { board ->
            assertEquals(
                VerificationOutcomeNextStepsCardId.entries,
                board.cards.map { card -> card.id }
            )
            assertEquals((1..6).toList(), board.cards.map { card -> card.displayOrder })
            assertEquals(expectedTitles, board.cards.map { card -> card.title })
            assertEquals(board.recommendedCardIds.distinct(), board.recommendedCardIds)
            assertEquals(
                board.recommendedCardIds.sortedBy { cardId -> cardId.displayOrder },
                board.recommendedCardIds
            )
            assertEquals(board.recommendedActionIds.distinct(), board.recommendedActionIds)
            assertEquals(
                board.recommendedActionIds.sortedBy { actionId -> actionId.ordinal },
                board.recommendedActionIds
            )

            board.cards.forEach { card ->
                assertEquals(card.id.displayOrder, card.displayOrder)
                assertTrue(card.title.isNotBlank())
                assertTrue(card.outcomeCondition.isNotBlank())
                assertTrue(card.outcomeCondition.endsWith("면"))
                assertTrue(card.nextStepSentence.isNotBlank())
                assertTrue(card.whyItMatters.isNotBlank())
                assertTrue(card.doNotRecordOrShareBoundaries.isNotEmpty())
                assertTrue(card.doNotRecordOrShareBoundaries.all { boundary -> boundary.isNotBlank() })
                assertTrue(card.primaryAction.label.isNotBlank())
                assertTrue(card.secondaryActions.isNotEmpty())
                assertTrue(card.secondaryActions.all { action -> action.label.isNotBlank() })
                assertEquals(
                    card.routeActions.map { action -> action.id }.distinct(),
                    card.routeActions.map { action -> action.id }
                )
            }
        }

        val usedActions = (noAnalysis.cards + analyzed.cards)
            .flatMap { card -> card.routeActions } +
            noAnalysis.recommendedActions + analyzed.recommendedActions
        assertEquals(
            VerificationOutcomeNextStepsActionId.entries.toSet(),
            usedActions.map { action -> action.id }.toSet()
        )
    }

    @Test
    fun familyShareContainsOnlyFixedGenericCardAndRouteLabelsWithoutSecretsOrPatterns() {
        val rawInput =
            "RAW_OUTCOME 박민수 010-9876-5432 계좌 123456789012 카드 4111111111111111 " +
                "OTP 654321 PIN 7788 주민 900101-1234567 https://evil.example " +
                "ShadowRemote com.evil.remote DEVICE-ID-ABC987654 VERIFIED_RESULT"
        val readinessMarker =
            "PLAN_SECRET callback-secret contact-secret 070-1111-2222 " +
                "account-987654321012 https://private.example SAFE_WORD_VALUE"
        val board = VerificationOutcomeNextStepsBoardBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                *ScamSignal.entries.toTypedArray(),
                input = rawInput
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, readinessMarker),
            incidentCount = Int.MAX_VALUE
        )
        val summary = board.familyShareSummary

        val expectedLines = buildList {
            add("[가족 AI 보이스피싱 방패] 확인 결과별 다음 단계 보드")
            add("가능한 결과별 다음 수동 경로를 함께 보고 있습니다.")
            add("추천 결과 카드")
            board.recommendedCards.forEach { card -> add("- ${card.title}") }
            add("직접 고를 기존 앱 경로")
            board.recommendedActions.forEach { action -> add("- ${action.label}") }
            add("가족에게 알릴 값 없는 문장")
            add("- 현재 연락에서 더 행동하지 않고 가능한 결과별 다음 수동 경로를 직접 고르고 있습니다.")
            add(
                "원문·사람·연락처·번호·링크·인증·계정·카드·기기·가족 계획·확인 답·결과값 없이 " +
                    "고정된 카드와 기존 앱 경로 이름만 담았습니다."
            )
        }
        assertEquals(
            expectedLines,
            summary.lineSequence().filter { line -> line.isNotBlank() }.toList()
        )

        listOf(
            "RAW_OUTCOME",
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
            "VERIFIED_RESULT",
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

        val first = VerificationOutcomeNextStepsBoardBuilder.build(
            firstAnalysis,
            firstReadiness,
            1
        )
        val repeated = VerificationOutcomeNextStepsBoardBuilder.build(
            firstAnalysis,
            firstReadiness,
            1
        )
        val differentForbiddenValues = VerificationOutcomeNextStepsBoardBuilder.build(
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
    fun privacyCopyStatesManualLocalNoOutcomeStorageNoAutomationAndNoMutation() {
        val board = VerificationOutcomeNextStepsBoardBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2
        )
        val copy = allCopy(board)

        listOf(
            "로컬 전용·무상태 수동 안내",
            "결과 입력을 요구하지 않습니다",
            "새 저장소를 만들거나 기록하지 않습니다",
            "확인 결과·결과 상태",
            "연락처를 읽거나 쓰거나 고르지 않습니다",
            "공유를 자동 전송하지 않습니다",
            "네트워크·웹 탐색·지도·위치",
            "기관 번호 조회·공식 경로 조회",
            "전화번호 검증",
            "URL·도메인 검증",
            "링크 스캔",
            "새 Android 권한을 요청하지 않습니다",
            "스케줄러를 사용하지 않습니다",
            "전화 앱을 열거나 전화를 자동으로 걸거나 끊거나 선별·차단하지 않고",
            "문자·SMS를 보내거나 읽지 않으며",
            "클립보드를 읽거나 쓰지 않고",
            "기기 관리자·접근성 서비스·VPN",
            "설치 앱·패키지 목록을 조회·설치·제거",
            "결제·은행 기능이나 API",
            "추가·수정·삭제하지 않습니다",
            "자동으로 가리거나 모니터링·스캔·삭제·차단·호출·메시지 전송·조회·확인·검증·저장하지 않습니다",
            "증명하거나 보장하지 않습니다"
        ).forEach { expectedBoundary ->
            assertTrue("privacy copy omitted: $expectedBoundary", copy.contains(expectedBoundary))
        }
        assertTrue(copy.contains("원문·가린 단서·사건 내용·가족 안전 계획 값을 사용하지 않습니다"))
        assertTrue(copy.contains("돈의 움직임이나 확인 결과"))
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndPositiveAutomaticOutcomeClaims() {
        val boards = listOf(
            VerificationOutcomeNextStepsBoardBuilder.build(
                null,
                readiness(ReadinessLevel.READY)
            ),
            VerificationOutcomeNextStepsBoardBuilder.build(
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
            "recovered",
            "blocked",
            "안전합니다",
            "문제없습니다",
            "본인 확인 완료",
            "신원이 확인됐습니다",
            "공식 번호로 확인됐습니다",
            "공식 링크입니다",
            "링크 검증 완료",
            "번호 검증 완료",
            "피해를 차단했습니다",
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
            "결과를 자동 저장합니다",
            "본인을 자동 확인합니다"
        )

        boards.forEach { board ->
            val copy = allCopy(board)
            forbiddenClaims.forEach { claim ->
                assertFalse(
                    "board made unsafe claim: $claim",
                    copy.contains(claim, ignoreCase = true)
                )
            }
            listOf(
                Regex("자동(?:으로)?\\s*(?:차단|동결|정지|삭제|가림|감시|스캔|검사|전송|전화|조회|확인|검증|저장)(?:합니다|됩니다|완료)"),
                Regex("(?:확인|검증|연결 해제|차단|동결|정지|삭제|제거|정리|복구|회복)\\s*(?:완료|됐습니다|되었습니다)"),
                Regex("(?:번호|링크|URL|기관|본인|결과).{0,12}(?:확인 완료|검증 완료|공식 확인|저장 완료)"),
                Regex("(?:송금|이체|결제).{0,12}자동으로\\s*(?:실행|취소|차단)합니다")
            ).forEach { positiveAutomationPattern ->
                assertFalse(
                    "board matched positive automation claim: ${positiveAutomationPattern.pattern}",
                    positiveAutomationPattern.containsMatchIn(copy)
                )
            }
        }
    }

    @Test
    fun unrelatedSignalCategoriesProduceDifferentFocusedRecommendationsAndCardsStayHypothetical() {
        fun boardFor(signal: ScamSignal) = VerificationOutcomeNextStepsBoardBuilder.build(
            analysis = analysis(RiskLevel.MEDIUM, signal),
            readiness = readiness(ReadinessLevel.READY)
        )

        val family = boardFor(ScamSignal.FAMILY_EMERGENCY)
        val financial = boardFor(ScamSignal.URGENT_TRANSFER)
        val link = boardFor(ScamSignal.LINK)
        val remote = boardFor(ScamSignal.APP_INSTALL)

        assertNotEquals(family.recommendedCardIds, financial.recommendedCardIds)
        assertNotEquals(family.recommendedActionIds, financial.recommendedActionIds)
        assertNotEquals(link.recommendedActionIds, remote.recommendedActionIds)
        assertTrue(
            VerificationOutcomeNextStepsCardId.NOT_FROM_THEM_FAMILY_WARNING in
                family.recommendedCardIds
        )
        assertTrue(
            VerificationOutcomeNextStepsActionId.MONEY_TRANSFER_PAUSE_GATE in
                financial.recommendedActionIds
        )
        assertTrue(VerificationOutcomeNextStepsActionId.PRIVACY_CENTER in link.recommendedActionIds)
        assertTrue(
            VerificationOutcomeNextStepsActionId.REMOTE_ACCESS_CUTOFF_COACH in
                remote.recommendedActionIds
        )

        remote.cards.forEach { card ->
            assertTrue(
                "outcome condition is not hypothetical: ${card.id}",
                card.outcomeCondition.endsWith("면")
            )
            assertFalse(Regex("(?i)https?://|www\\.").containsMatchIn(card.outcomeCondition))
            assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(card.outcomeCondition))
        }
    }

    private fun VerificationOutcomeNextStepsBoard.card(
        id: VerificationOutcomeNextStepsCardId
    ): VerificationOutcomeNextStepsCard = cards.single { card -> card.id == id }

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

    private fun allCopy(board: VerificationOutcomeNextStepsBoard): String = buildString {
        append(board.title)
        append(board.intro)
        append(board.modeLabel)
        append(board.recommendationTitle)
        append(board.recommendationExplanation)
        board.recommendedActions.forEach { action -> append(action.label) }
        board.cards.forEach { card ->
            append(card.title)
            append(card.outcomeCondition)
            append(card.nextStepSentence)
            append(card.whyItMatters)
            card.doNotRecordOrShareBoundaries.forEach(::append)
            card.routeActions.forEach { action -> append(action.label) }
        }
        board.privacySafetyNotes.forEach(::append)
        append(board.familyShareSummary)
    }
}
