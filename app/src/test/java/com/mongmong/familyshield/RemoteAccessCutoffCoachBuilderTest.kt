package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteAccessCutoffCoachBuilderTest {
    @Test
    fun noAnalysisCoachIsConservativeAndNeverDeclaresContactOrDeviceSafe() {
        val coach = RemoteAccessCutoffCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(RemoteAccessCutoffCardId.entries, coach.recommendedCardIds)
        assertTrue(
            coach.recommendedActionIds.containsAll(
                listOf(
                    RemoteAccessCutoffActionId.QUICK_STOP,
                    RemoteAccessCutoffActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                    RemoteAccessCutoffActionId.CHECKER,
                    RemoteAccessCutoffActionId.PRIVACY_CENTER,
                    RemoteAccessCutoffActionId.TRUSTED_HELPER_REVIEW,
                    RemoteAccessCutoffActionId.ACCOUNT_LOCKDOWN,
                    RemoteAccessCutoffActionId.MONEY_TRANSFER_PAUSE_GATE,
                    RemoteAccessCutoffActionId.EVIDENCE_KIT,
                    RemoteAccessCutoffActionId.OFFICIAL_HELP_DIRECTORY,
                    RemoteAccessCutoffActionId.SAFETY_FLOW_ROUTER
                )
            )
        )
        assertTrue(coach.recommendationExplanation.contains("현재 분석이 없거나 일치 신호가 없습니다"))
        assertTrue(coach.recommendationExplanation.contains("문제없다는 판정으로 보지 말고"))

        val copy = allCopy(coach)
        listOf(
            "안전합니다",
            "문제없습니다",
            "사기가 아닙니다",
            "기기가 안전",
            "원격 접근이 끝났습니다",
            "100%",
            " safe ",
            "blocked"
        ).forEach { unsafeClaim ->
            assertFalse(
                "default coach made unsafe claim: $unsafeClaim",
                copy.contains(unsafeClaim, ignoreCase = true)
            )
        }
    }

    @Test
    fun appInstallAndLiveCallControlRecommendManualCutoffHelperAccountAndEvidenceRoutes() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val coach = RemoteAccessCutoffCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        RemoteAccessCutoffCardId.END_CALL_AND_SCREEN_SHARE,
                        RemoteAccessCutoffCardId.STOP_APP_FILE_PROFILE_INSTALL,
                        RemoteAccessCutoffCardId.USE_SEPARATE_DEVICE_AND_HELPER,
                        RemoteAccessCutoffCardId.PRESERVE_SAFE_EVIDENCE
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RemoteAccessCutoffActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                        RemoteAccessCutoffActionId.TRUSTED_HELPER_REVIEW,
                        RemoteAccessCutoffActionId.ACCOUNT_LOCKDOWN,
                        RemoteAccessCutoffActionId.EVIDENCE_KIT,
                        RemoteAccessCutoffActionId.SAFETY_FLOW_ROUTER
                    )
                )
            )
            val endContact = coach.card(RemoteAccessCutoffCardId.END_CALL_AND_SCREEN_SHARE)
            val stopInstall = coach.card(RemoteAccessCutoffCardId.STOP_APP_FILE_PROFILE_INSTALL)
            assertTrue(endContact.cutoffSentence.contains("제가 직접 끝내고"))
            assertTrue(stopInstall.cutoffSentence.contains("링크·앱·파일·프로필"))
            assertTrue(stopInstall.cutoffSentence.contains("설치하지 않고"))
            assertTrue(coach.recommendationExplanation.contains("보호자·계정·증거 경로"))

            val copy = allCopy(coach)
            listOf(
                "원격 연결을 자동으로 해제합니다",
                "원격 연결 해제 완료",
                "악성 프로그램을 제거합니다",
                "악성 앱을 제거합니다",
                "기기를 자동으로 정리합니다"
            ).forEach { guarantee -> assertFalse(copy.contains(guarantee)) }
        }
    }

    @Test
    fun credentialAndOtpSignalsHideCodesAndScreensWithoutAccountFreezeClaim() {
        listOf(
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = RemoteAccessCutoffCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                RemoteAccessCutoffCardId.HIDE_CODES_AND_FINANCIAL_SCREENS in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RemoteAccessCutoffActionId.ACCOUNT_LOCKDOWN,
                        RemoteAccessCutoffActionId.EVIDENCE_KIT,
                        RemoteAccessCutoffActionId.OFFICIAL_HELP_DIRECTORY
                    )
                )
            )
            val card = coach.card(RemoteAccessCutoffCardId.HIDE_CODES_AND_FINANCIAL_SCREENS)
            assertTrue(card.cutoffSentence.contains("인증번호·PIN·비밀번호"))
            assertTrue(card.cutoffSentence.contains("계정·카드·송금 화면을 보여주지 않겠습니다"))
            assertEquals(RemoteAccessCutoffActionId.ACCOUNT_LOCKDOWN, card.primaryAction.id)

            val copy = allCopy(coach)
            assertFalse(copy.contains("계정을 자동으로 정지합니다"))
            assertFalse(copy.contains("카드를 자동으로 동결합니다"))
            assertFalse(copy.contains("계정 동결 완료"))
        }
    }

    @Test
    fun urgentTransferAndFinancialSignalsRecommendMoneyPauseAccountEvidenceAndOfficialRoutes() {
        listOf(
            analysis(RiskLevel.HIGH),
            analysis(RiskLevel.MEDIUM, ScamSignal.URGENT_TRANSFER),
            analysis(RiskLevel.MEDIUM, ScamSignal.BAIT),
            analysis(RiskLevel.MEDIUM, ScamSignal.SAFE_ACCOUNT_FRAUD),
            analysis(RiskLevel.MEDIUM, ScamSignal.GIFT_CRYPTO)
        ).forEach { current ->
            val coach = RemoteAccessCutoffCoachBuilder.build(
                analysis = current,
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        RemoteAccessCutoffCardId.HIDE_CODES_AND_FINANCIAL_SCREENS,
                        RemoteAccessCutoffCardId.PRESERVE_SAFE_EVIDENCE,
                        RemoteAccessCutoffCardId.CONTINUE_OFFICIAL_HELP_AND_FOLLOW_UP
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RemoteAccessCutoffActionId.MONEY_TRANSFER_PAUSE_GATE,
                        RemoteAccessCutoffActionId.ACCOUNT_LOCKDOWN,
                        RemoteAccessCutoffActionId.EVIDENCE_KIT,
                        RemoteAccessCutoffActionId.OFFICIAL_HELP_DIRECTORY,
                        RemoteAccessCutoffActionId.REPORT_PREP,
                        RemoteAccessCutoffActionId.RECOVERY_PLAN,
                        RemoteAccessCutoffActionId.FOLLOW_UP_CHECK_IN_PLANNER
                    )
                )
            )
            val copy = allCopy(coach)
            assertFalse(copy.contains("결제를 자동으로 취소합니다"))
            assertFalse(copy.contains("송금을 취소했습니다"))
            assertFalse(copy.contains("환급을 보장합니다"))
            assertFalse(copy.contains("피해금 회수를 보장합니다"))
        }
    }

    @Test
    fun linkAndSmishingRecommendNoLinkOrFileOpeningAndExistingPrivacyReviewRoutes() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val coach = RemoteAccessCutoffCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        RemoteAccessCutoffCardId.STOP_APP_FILE_PROFILE_INSTALL,
                        RemoteAccessCutoffCardId.PRESERVE_SAFE_EVIDENCE
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RemoteAccessCutoffActionId.CHECKER,
                        RemoteAccessCutoffActionId.PRIVACY_CENTER,
                        RemoteAccessCutoffActionId.EXPOSURE_IMPACT_TRIAGE,
                        RemoteAccessCutoffActionId.EVIDENCE_KIT
                    )
                )
            )
            val stop = coach.card(RemoteAccessCutoffCardId.STOP_APP_FILE_PROFILE_INSTALL)
            assertTrue(stop.cutoffSentence.contains("링크·앱·파일·프로필을 열거나 설치하지 않고"))
            assertTrue(coach.recommendationExplanation.contains("링크나 파일을 스캔하지 않습니다"))
            assertFalse(allCopy(coach).contains("링크를 자동 검사합니다"))
            assertFalse(allCopy(coach).contains("URL을 스캔합니다"))
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeEndCurrentContactAndUseCallbackScripts() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val coach = RemoteAccessCutoffCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        RemoteAccessCutoffCardId.END_CALL_AND_SCREEN_SHARE,
                        RemoteAccessCutoffCardId.USE_SEPARATE_DEVICE_AND_HELPER
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RemoteAccessCutoffActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                        RemoteAccessCutoffActionId.SAFE_CALL_SCRIPTS,
                        RemoteAccessCutoffActionId.FAMILY_ALERT_MESSAGES
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("현재 연락을 끝낸 뒤"))
            assertTrue(coach.recommendationExplanation.contains("가족 콜백·확인 전화 문장"))
        }
    }

    @Test
    fun secrecyAndImpersonationRejectSecretScreenShareAndUseOfficialRoute() {
        listOf(ScamSignal.SECRECY, ScamSignal.IMPERSONATION).forEach { signal ->
            val coach = RemoteAccessCutoffCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        RemoteAccessCutoffCardId.END_CALL_AND_SCREEN_SHARE,
                        RemoteAccessCutoffCardId.PRESERVE_SAFE_EVIDENCE,
                        RemoteAccessCutoffCardId.CONTINUE_OFFICIAL_HELP_AND_FOLLOW_UP
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RemoteAccessCutoffActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                        RemoteAccessCutoffActionId.FAMILY_ALERT_MESSAGES,
                        RemoteAccessCutoffActionId.OFFICIAL_HELP_DIRECTORY
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("화면공유나 현재 연락을 비밀로 하지 않고"))
            assertTrue(coach.recommendationExplanation.contains("직접 찾은 공식 경로"))
        }
    }

    @Test
    fun savedIncidentChangesRecommendationsWithoutReadingContentsAndClampsCounts() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.FAMILY_EMERGENCY)
        val ready = readiness(ReadinessLevel.READY)
        val zero = RemoteAccessCutoffCoachBuilder.build(current, ready, incidentCount = 0)
        val negative = RemoteAccessCutoffCoachBuilder.build(current, ready, incidentCount = -99)
        val one = RemoteAccessCutoffCoachBuilder.build(current, ready, incidentCount = 1)
        val many = RemoteAccessCutoffCoachBuilder.build(
            current,
            ready,
            incidentCount = Int.MAX_VALUE
        )

        assertEquals(zero, negative)
        assertEquals(one, many)
        assertNotEquals(zero, one)
        assertFalse(RemoteAccessCutoffActionId.EVIDENCE_KIT in zero.recommendedActionIds)
        assertTrue(
            one.recommendedActionIds.containsAll(
                listOf(
                    RemoteAccessCutoffActionId.EVIDENCE_KIT,
                    RemoteAccessCutoffActionId.FOLLOW_UP_CHECK_IN_PLANNER
                )
            )
        )
        assertTrue(RemoteAccessCutoffCardId.PRESERVE_SAFE_EVIDENCE in one.recommendedCardIds)
        assertTrue(one.recommendationExplanation.contains("개수 범주만 반영"))
        assertTrue(one.recommendationExplanation.contains("사건 내용은 읽지 않습니다"))
        assertFalse(one.familyShareSummary.contains(Int.MAX_VALUE.toString()))
    }

    @Test
    fun lowAndInProgressReadinessAddCallbackRoadmapFamilyAndHelperSupportWhileReadyIsCautious() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.LINK)
        val low = RemoteAccessCutoffCoachBuilder.build(current, readiness(ReadinessLevel.LOW))
        val inProgress = RemoteAccessCutoffCoachBuilder.build(
            current,
            readiness(ReadinessLevel.IN_PROGRESS)
        )
        val ready = RemoteAccessCutoffCoachBuilder.build(current, readiness(ReadinessLevel.READY))

        listOf(low, inProgress).forEach { coach ->
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RemoteAccessCutoffActionId.SAFE_CALL_SCRIPTS,
                        RemoteAccessCutoffActionId.FAMILY_ALERT_MESSAGES,
                        RemoteAccessCutoffActionId.TRUSTED_HELPER_REVIEW,
                        RemoteAccessCutoffActionId.FAMILY_READINESS_ROADMAP
                    )
                )
            )
        }
        assertFalse(
            RemoteAccessCutoffActionId.FAMILY_READINESS_ROADMAP in ready.recommendedActionIds
        )
        assertTrue(low.recommendationExplanation.contains("가족 준비가 부족한 상태"))
        assertTrue(inProgress.recommendationExplanation.contains("가족 준비가 진행 중"))
        assertTrue(ready.recommendationExplanation.contains("가족 준비가 되어 있어도"))
        assertTrue(ready.recommendationExplanation.contains("조치 완료가 확인된 것은 아닙니다"))
    }

    @Test
    fun everyCardHasCompleteFieldsStableOrderUniqueRoutesAndAllActionIdsMapToExistingFlows() {
        val noAnalysis = RemoteAccessCutoffCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )
        val analyzed = RemoteAccessCutoffCoachBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2
        )
        val expectedTitles = listOf(
            "화면공유·통화 지시 끊기",
            "앱 설치·파일·프로필 요구 중단",
            "인증번호·금융화면 숨기기",
            "민감 계정은 별도 기기/보호자와 점검",
            "증거는 비밀값 없이 보존",
            "공식 도움·후속 점검으로 이어가기"
        )

        listOf(noAnalysis, analyzed).forEach { coach ->
            assertEquals(RemoteAccessCutoffCardId.entries, coach.cards.map { card -> card.id })
            assertEquals((1..6).toList(), coach.cards.map { card -> card.displayOrder })
            assertEquals(expectedTitles, coach.cards.map { card -> card.title })
            assertEquals(coach.recommendedCardIds.distinct(), coach.recommendedCardIds)
            assertEquals(
                coach.recommendedCardIds.sortedBy { cardId -> cardId.displayOrder },
                coach.recommendedCardIds
            )
            assertEquals(coach.recommendedActionIds.distinct(), coach.recommendedActionIds)
            assertEquals(
                coach.recommendedActionIds.sortedBy { actionId -> actionId.ordinal },
                coach.recommendedActionIds
            )

            coach.cards.forEach { card ->
                assertEquals(card.id.displayOrder, card.displayOrder)
                assertTrue(card.title.isNotBlank())
                assertTrue(card.cutoffSentence.isNotBlank())
                assertTrue(card.whyItMatters.isNotBlank())
                assertTrue(card.doNotDoBoundaries.isNotEmpty())
                assertTrue(card.doNotDoBoundaries.all { boundary -> boundary.isNotBlank() })
                assertTrue(card.primaryAction.label.isNotBlank())
                assertTrue(card.secondaryActions.isNotEmpty())
                assertTrue(card.secondaryActions.all { action -> action.label.isNotBlank() })
                assertEquals(
                    card.routeActions.map { action -> action.id }.distinct(),
                    card.routeActions.map { action -> action.id }
                )
            }
        }

        val allUsedActions = (noAnalysis.cards + analyzed.cards)
            .flatMap { card -> card.routeActions } + noAnalysis.recommendedActions +
            analyzed.recommendedActions
        assertEquals(
            RemoteAccessCutoffActionId.entries.toSet(),
            allUsedActions.map { action -> action.id }.toSet()
        )
    }

    @Test
    fun familyShareContainsOnlyGenericCutoffCardsRoutesAndNoSensitiveOrDeviceValues() {
        val rawInput =
            "RAW_REMOTE 박민수 010-9876-5432 계좌 123456789012 카드 4111111111111111 " +
                "OTP 654321 PIN 7788 주민 900101-1234567 https://evil.example " +
                "ShadowRemote com.evil.remote DEVICE-ID-ABC987654"
        val readinessMarker =
            "PLAN_SECRET callback-secret contact-secret 070-1111-2222 " +
                "account-987654321012 https://private.example"
        val coach = RemoteAccessCutoffCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                *ScamSignal.entries.toTypedArray(),
                input = rawInput
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, readinessMarker),
            incidentCount = Int.MAX_VALUE
        )
        val summary = coach.familyShareSummary

        val allowedLines = buildSet {
            add("[가족 AI 보이스피싱 방패] 원격 접근 끊기 코치")
            add("지금 직접 읽을 추천 끊기 문장")
            coach.recommendedCards.forEach { card ->
                add("- ${card.title}: ${card.cutoffSentence}")
            }
            add("직접 고를 기존 앱 경로")
            coach.recommendedActions.forEach { action -> add("- ${action.label}") }
            add("전체 수동 끊기 카드")
            coach.cards.forEach { card -> add("- ${card.title}") }
            add(
                "원문·사람·연락처·비밀값·기기값을 넣지 않은 일반 수동 문장과 기존 경로 " +
                    "이름입니다. 가족이 직접 읽고 필요한 경로를 직접 고릅니다."
            )
        }
        summary.lineSequence().filter { line -> line.isNotBlank() }.forEach { line ->
            assertTrue("unexpected share line: $line", line in allowedLines)
        }

        listOf(
            "RAW_REMOTE",
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
            "PLAN_SECRET",
            "callback-secret",
            "contact-secret",
            "070-1111-2222",
            "987654321012",
            "private.example"
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
        assertFalse(Regex("(?i)DEVICE[-_ ]?ID\\s*[:=]?\\s*[A-Z0-9-]{6,}").containsMatchIn(summary))
    }

    @Test
    fun builderIsDeterministicAndIgnoresRawInputScoreAndReadinessLiteralFields() {
        val signals = listOf(
            ScamSignal.APP_INSTALL,
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

        val first = RemoteAccessCutoffCoachBuilder.build(firstAnalysis, firstReadiness, 1)
        val repeated = RemoteAccessCutoffCoachBuilder.build(firstAnalysis, firstReadiness, 1)
        val differentForbiddenValues = RemoteAccessCutoffCoachBuilder.build(
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
    fun privacyCopyStatesManualLocalStatelessAndAllDeviceControlBoundaries() {
        val coach = RemoteAccessCutoffCoachBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2
        )
        val copy = allCopy(coach)

        listOf(
            "로컬 전용·무상태 수동 안내",
            "새 저장소를 만들거나 기록하지 않습니다",
            "연락처",
            "공유도 자동 전송하지 않습니다",
            "네트워크를 사용하거나 새 Android 권한을 요청하지 않습니다",
            "스케줄러를 사용하지 않습니다",
            "추가·수정·삭제하지 않습니다",
            "기기 관리자",
            "접근성 서비스",
            "VPN",
            "알림 접근",
            "알림 리스너",
            "사용 기록",
            "설치 앱·패키지 목록을 검사하지 않습니다",
            "앱·파일·프로필을 설치하거나 제거하지 않고",
            "설정을 변경하지 않으며",
            "원격 연결 해제",
            "악성 앱 제거",
            "기기 정리를 수행하지 않습니다",
            "결제·은행 API",
            "NFC",
            "Android 계정 관리자",
            "자동 수행하지 않습니다",
            "증명하거나 보장하지 않습니다"
        ).forEach { expectedBoundary ->
            assertTrue("privacy copy omitted: $expectedBoundary", copy.contains(expectedBoundary))
        }
        assertTrue(copy.contains("원문·가린 단서·사건 내용·가족 안전 계획 값을 사용하지 않습니다"))
        assertTrue(copy.contains("원격 접근 상태·설치 앱 목록·패키지 이름·기기 상태"))
        assertTrue(copy.contains("전화를 걸거나 끊거나 선별·차단하지 않고"))
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndPositiveAutomaticDeviceOrPaymentClaims() {
        val coaches = listOf(
            RemoteAccessCutoffCoachBuilder.build(null, readiness(ReadinessLevel.READY)),
            RemoteAccessCutoffCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.LOW),
                incidentCount = 3
            )
        )
        val forbiddenClaims = listOf(
            "100%",
            " safe ",
            "blocked",
            "안전합니다",
            "문제없습니다",
            "사기임을 확정합니다",
            "피해를 차단했습니다",
            "원격 접근이 끊겼습니다",
            "원격 연결을 해제했습니다",
            "원격 연결 해제 완료",
            "악성 프로그램을 제거합니다",
            "악성 앱을 제거합니다",
            "기기 정리 완료",
            "계정을 자동으로 잠급니다",
            "계정이 동결됐습니다",
            "카드를 자동으로 정지합니다",
            "결제를 자동으로 취소합니다",
            "송금을 자동으로 취소합니다",
            "송금을 자동으로 실행합니다",
            "이체를 자동으로 실행합니다",
            "결제를 자동으로 실행합니다",
            "앱을 자동으로 설치합니다",
            "프로필을 자동으로 설치합니다",
            "앱을 자동으로 제거합니다",
            "기기 설정을 자동으로 바꿉니다",
            "설정을 자동으로 변경합니다",
            "기기를 자동으로 제어합니다",
            "화면공유를 자동으로 종료합니다",
            "원격 접근을 자동으로 차단합니다",
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
            "문자를 자동 전송합니다"
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
                Regex("자동(?:으로)?\\s*(?:차단|동결|정지|삭제|가림|감시|스캔|검사|전송|전화)(?:합니다|됩니다|완료)"),
                Regex("(?:연결 해제|차단|동결|정지|삭제|제거|정리|복구|검사)\\s*(?:완료|됐습니다|되었습니다)"),
                Regex("(?:계정|카드|피해금|기기).{0,12}(?:자동 조치|자동 복구|회수 보장)"),
                Regex("(?:송금|이체|결제).{0,12}자동으로\\s*(?:실행|취소|차단)합니다"),
                Regex("(?:앱|프로필).{0,12}자동으로\\s*(?:설치|제거|삭제)합니다"),
                Regex("(?:기기|설정|계정|카드).{0,12}자동으로\\s*(?:변경|제어|정지|동결|잠금)합니다")
            ).forEach { positiveAutomationPattern ->
                assertFalse(
                    "coach matched positive automation claim: ${positiveAutomationPattern.pattern}",
                    positiveAutomationPattern.containsMatchIn(copy)
                )
            }
        }
    }

    @Test
    fun unrelatedSignalCategoriesProduceDifferentFocusedRecommendationsAndCardsStayManual() {
        fun coachFor(signal: ScamSignal) = RemoteAccessCutoffCoachBuilder.build(
            analysis = analysis(RiskLevel.MEDIUM, signal),
            readiness = readiness(ReadinessLevel.READY)
        )

        val remote = coachFor(ScamSignal.APP_INSTALL)
        val link = coachFor(ScamSignal.LINK)
        val family = coachFor(ScamSignal.FAMILY_EMERGENCY)
        val financial = coachFor(ScamSignal.URGENT_TRANSFER)

        assertNotEquals(remote.recommendedCardIds, link.recommendedCardIds)
        assertNotEquals(remote.recommendedActionIds, link.recommendedActionIds)
        assertNotEquals(family.recommendedCardIds, financial.recommendedCardIds)
        assertNotEquals(family.recommendedActionIds, financial.recommendedActionIds)
        assertTrue(RemoteAccessCutoffCardId.STOP_APP_FILE_PROFILE_INSTALL in remote.recommendedCardIds)
        assertTrue(RemoteAccessCutoffActionId.TRUSTED_HELPER_REVIEW in remote.recommendedActionIds)
        assertTrue(RemoteAccessCutoffActionId.PRIVACY_CENTER in link.recommendedActionIds)
        assertFalse(RemoteAccessCutoffActionId.MONEY_TRANSFER_PAUSE_GATE in link.recommendedActionIds)
        assertTrue(RemoteAccessCutoffActionId.SAFE_CALL_SCRIPTS in family.recommendedActionIds)
        assertTrue(
            family.recommendedActions.single {
                action -> action.id == RemoteAccessCutoffActionId.SAFE_CALL_SCRIPTS
            }.label.contains("값 없는 가족 콜백")
        )
        assertTrue(RemoteAccessCutoffActionId.MONEY_TRANSFER_PAUSE_GATE in financial.recommendedActionIds)

        remote.cards.forEach { card ->
            assertTrue(
                "cutoff sentence lacks manual refusal wording: ${card.id}",
                listOf("않겠습니다", "끝내고", "확인하겠습니다")
                    .any { phrase -> card.cutoffSentence.contains(phrase) }
            )
            assertFalse(Regex("(?i)https?://|www\\.").containsMatchIn(card.cutoffSentence))
            assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(card.cutoffSentence))
        }
    }

    private fun RemoteAccessCutoffCoach.card(
        id: RemoteAccessCutoffCardId
    ): RemoteAccessCutoffCard = cards.single { card -> card.id == id }

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

    private fun allCopy(coach: RemoteAccessCutoffCoach): String = buildString {
        append(coach.title)
        append(coach.intro)
        append(coach.modeLabel)
        append(coach.recommendationTitle)
        append(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> append(action.label) }
        coach.cards.forEach { card ->
            append(card.title)
            append(card.cutoffSentence)
            append(card.whyItMatters)
            card.doNotDoBoundaries.forEach(::append)
            card.routeActions.forEach { action -> append(action.label) }
        }
        coach.privacySafetyNotes.forEach(::append)
        append(coach.familyShareSummary)
    }
}
