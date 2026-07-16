package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeparateChannelVerificationCoachBuilderTest {
    @Test
    fun noAnalysisCoachIsConservativeWithoutDeclaringAnythingSafeVerifiedOrOfficial() {
        val coach = SeparateChannelVerificationCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(SeparateChannelVerificationCardId.entries, coach.recommendedCardIds)
        assertTrue(
            coach.recommendedActionIds.containsAll(
                listOf(
                    SeparateChannelVerificationActionId.QUICK_STOP,
                    SeparateChannelVerificationActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                    SeparateChannelVerificationActionId.CHECKER,
                    SeparateChannelVerificationActionId.PRIVACY_CENTER,
                    SeparateChannelVerificationActionId.SAFE_CALL_SCRIPTS,
                    SeparateChannelVerificationActionId.TRUSTED_HELPER_REVIEW,
                    SeparateChannelVerificationActionId.OFFICIAL_HELP_DIRECTORY,
                    SeparateChannelVerificationActionId.SAFETY_FLOW_ROUTER
                )
            )
        )
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
            "100%",
            " safe ",
            "verified",
            "officially confirmed",
            "problem-free",
            "blocked"
        ).forEach { unsafeClaim ->
            assertFalse(
                "default coach made unsafe claim: $unsafeClaim",
                copy.contains(unsafeClaim, ignoreCase = true)
            )
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendKnownPathScriptsAndFamilyAlert() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val coach = SeparateChannelVerificationCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY, "PLAN_VALUE_010-1111-2222")
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        SeparateChannelVerificationCardId.ABANDON_CURRENT_CHANNEL,
                        SeparateChannelVerificationCardId.FAMILY_KNOWN_PATH_CALLBACK,
                        SeparateChannelVerificationCardId.VALUE_FREE_FAMILY_UPDATE
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        SeparateChannelVerificationActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                        SeparateChannelVerificationActionId.SAFE_CALL_SCRIPTS,
                        SeparateChannelVerificationActionId.FAMILY_ALERT_MESSAGES
                    )
                )
            )
            val familyCard = coach.card(
                SeparateChannelVerificationCardId.FAMILY_KNOWN_PATH_CALLBACK
            )
            assertTrue(familyCard.verificationSentence.contains("가족이 원래 쓰던 별도 경로"))
            assertTrue(familyCard.verificationSentence.contains("값 없는 확인 문장"))
            assertTrue(coach.recommendationExplanation.contains("평소 가족 경로의 콜백"))
            assertFalse(allCopy(coach).contains("PLAN_VALUE_010-1111-2222"))
        }
    }

    @Test
    fun impersonationAndSecrecyAbandonCurrentPathUseOfficialHelpAndRejectSecrecy() {
        listOf(ScamSignal.IMPERSONATION, ScamSignal.SECRECY).forEach { signal ->
            val coach = SeparateChannelVerificationCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        SeparateChannelVerificationCardId.ABANDON_CURRENT_CHANNEL,
                        SeparateChannelVerificationCardId.USER_FOUND_OFFICIAL_CHANNEL,
                        SeparateChannelVerificationCardId.VALUE_FREE_FAMILY_UPDATE
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        SeparateChannelVerificationActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                        SeparateChannelVerificationActionId.FAMILY_ALERT_MESSAGES,
                        SeparateChannelVerificationActionId.OFFICIAL_HELP_DIRECTORY
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("현재 연락 경로를 버리고"))
            assertTrue(coach.recommendationExplanation.contains("확인 과정을 비밀로 두지 마세요"))
        }
    }

    @Test
    fun urgentTransferFinancialAndHighRiskRecommendMoneyAccountEvidenceAndOfficialHelp() {
        listOf(
            analysis(RiskLevel.HIGH),
            analysis(RiskLevel.MEDIUM, ScamSignal.URGENT_TRANSFER),
            analysis(RiskLevel.MEDIUM, ScamSignal.BAIT),
            analysis(RiskLevel.MEDIUM, ScamSignal.GIFT_CRYPTO),
            analysis(RiskLevel.MEDIUM, ScamSignal.SAFE_ACCOUNT_FRAUD),
            analysis(RiskLevel.MEDIUM, ScamSignal.LOAN_REFINANCE_FEE),
            analysis(RiskLevel.MEDIUM, ScamSignal.GIFTICON_PIN),
            analysis(RiskLevel.MEDIUM, ScamSignal.OVERSEAS_PAYMENT_ALERT)
        ).forEach { current ->
            val coach = SeparateChannelVerificationCoachBuilder.build(
                analysis = current,
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        SeparateChannelVerificationCardId.ABANDON_CURRENT_CHANNEL,
                        SeparateChannelVerificationCardId.USER_FOUND_OFFICIAL_CHANNEL,
                        SeparateChannelVerificationCardId.SEPARATE_DEVICE_ACCOUNT_REVIEW
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        SeparateChannelVerificationActionId.MONEY_TRANSFER_PAUSE_GATE,
                        SeparateChannelVerificationActionId.ACCOUNT_LOCKDOWN,
                        SeparateChannelVerificationActionId.EVIDENCE_KIT,
                        SeparateChannelVerificationActionId.OFFICIAL_HELP_DIRECTORY,
                        SeparateChannelVerificationActionId.REPORT_PREP
                    )
                )
            )
            val copy = allCopy(coach)
            assertFalse(copy.contains("결제를 자동으로 취소합니다"))
            assertFalse(copy.contains("계정이나 카드를 정지했습니다"))
            assertFalse(copy.contains("환급을 보장합니다"))
            assertFalse(copy.contains("피해금을 회수합니다"))
        }
    }

    @Test
    fun credentialsAndOtpRecommendAccountAndRemoteCutoffWithoutFreezeClaim() {
        listOf(
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = SeparateChannelVerificationCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                SeparateChannelVerificationCardId.SEPARATE_DEVICE_ACCOUNT_REVIEW in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        SeparateChannelVerificationActionId.ACCOUNT_LOCKDOWN,
                        SeparateChannelVerificationActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        SeparateChannelVerificationActionId.EVIDENCE_KIT
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("인증 비밀 요구 가능성"))
            assertTrue(coach.recommendationExplanation.contains("자동 정지·동결하는 기능은 아닙니다"))
            assertFalse(allCopy(coach).contains("계정 동결 완료"))
            assertFalse(allCopy(coach).contains("카드 정지 완료"))
        }
    }

    @Test
    fun appInstallAndLiveCallControlRecommendRemoteHelperAccountAndEvidenceRoutes() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val coach = SeparateChannelVerificationCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        SeparateChannelVerificationCardId.ABANDON_CURRENT_CHANNEL,
                        SeparateChannelVerificationCardId.SEPARATE_DEVICE_ACCOUNT_REVIEW
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        SeparateChannelVerificationActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        SeparateChannelVerificationActionId.TRUSTED_HELPER_REVIEW,
                        SeparateChannelVerificationActionId.ACCOUNT_LOCKDOWN,
                        SeparateChannelVerificationActionId.EVIDENCE_KIT
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
    fun linkAndSmishingRejectCurrentLinkNumberAndUseCheckerExposurePrivacyRoutes() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val coach = SeparateChannelVerificationCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                SeparateChannelVerificationCardId.DIRECT_OFFICIAL_APP_OR_SITE in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        SeparateChannelVerificationActionId.CHECKER,
                        SeparateChannelVerificationActionId.EXPOSURE_IMPACT_TRIAGE,
                        SeparateChannelVerificationActionId.PRIVACY_CENTER
                    )
                )
            )
            val card = coach.card(
                SeparateChannelVerificationCardId.DIRECT_OFFICIAL_APP_OR_SITE
            )
            assertTrue(card.verificationSentence.contains("현재 메시지의 링크를 누르지 않고"))
            assertTrue(card.doNotUseOrShareBoundaries.any { it.contains("현재 링크") })
            assertTrue(coach.recommendationExplanation.contains("현재 링크와 번호를 재사용하지 않고"))
            assertTrue(coach.recommendationExplanation.contains("URL·도메인을 판정하지 않습니다"))
            assertFalse(allCopy(coach).contains("링크를 자동 검사합니다"))
            assertFalse(allCopy(coach).contains("URL 검증 완료"))
        }
    }

    @Test
    fun savedIncidentChangesRecommendationsWithoutReadingContentsAndClampsCounts() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.FAMILY_EMERGENCY)
        val ready = readiness(ReadinessLevel.READY)
        val zero = SeparateChannelVerificationCoachBuilder.build(current, ready, 0)
        val negative = SeparateChannelVerificationCoachBuilder.build(current, ready, -99)
        val one = SeparateChannelVerificationCoachBuilder.build(current, ready, 1)
        val many = SeparateChannelVerificationCoachBuilder.build(current, ready, Int.MAX_VALUE)

        assertEquals(zero, negative)
        assertEquals(one, many)
        assertNotEquals(zero, one)
        assertFalse(SeparateChannelVerificationActionId.EVIDENCE_KIT in zero.recommendedActionIds)
        assertTrue(SeparateChannelVerificationActionId.EVIDENCE_KIT in one.recommendedActionIds)
        assertTrue(SeparateChannelVerificationActionId.REPORT_PREP in one.recommendedActionIds)
        assertTrue(
            SeparateChannelVerificationCardId.USER_FOUND_OFFICIAL_CHANNEL in
                one.recommendedCardIds
        )
        assertTrue(one.recommendationExplanation.contains("개수 범주만 반영"))
        assertTrue(one.recommendationExplanation.contains("사건 내용은 읽지 않습니다"))
        assertFalse(one.familyShareSummary.contains(Int.MAX_VALUE.toString()))
    }

    @Test
    fun lowAndInProgressReadinessAddFamilyHelperAndRoadmapWhileReadyStaysCautious() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.LINK)
        val low = SeparateChannelVerificationCoachBuilder.build(
            current,
            readiness(ReadinessLevel.LOW)
        )
        val inProgress = SeparateChannelVerificationCoachBuilder.build(
            current,
            readiness(ReadinessLevel.IN_PROGRESS)
        )
        val ready = SeparateChannelVerificationCoachBuilder.build(
            current,
            readiness(ReadinessLevel.READY)
        )

        listOf(low, inProgress).forEach { coach ->
            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        SeparateChannelVerificationCardId.FAMILY_KNOWN_PATH_CALLBACK,
                        SeparateChannelVerificationCardId.SEPARATE_DEVICE_ACCOUNT_REVIEW
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        SeparateChannelVerificationActionId.SAFE_CALL_SCRIPTS,
                        SeparateChannelVerificationActionId.FAMILY_ALERT_MESSAGES,
                        SeparateChannelVerificationActionId.TRUSTED_HELPER_REVIEW,
                        SeparateChannelVerificationActionId.FAMILY_READINESS_ROADMAP
                    )
                )
            )
        }
        assertFalse(
            SeparateChannelVerificationActionId.FAMILY_READINESS_ROADMAP in
                ready.recommendedActionIds
        )
        assertTrue(low.recommendationExplanation.contains("가족 준비가 부족한 상태"))
        assertTrue(inProgress.recommendationExplanation.contains("가족 준비가 진행 중"))
        assertTrue(ready.recommendationExplanation.contains("가족 준비가 되어 있어도"))
        assertTrue(ready.recommendationExplanation.contains("점검했다는 뜻은 아닙니다"))
    }

    @Test
    fun everyCardHasCompleteFieldsStableOrderUniqueRoutesAndExistingActionIds() {
        val noAnalysis = SeparateChannelVerificationCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )
        val analyzed = SeparateChannelVerificationCoachBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2
        )
        val expectedTitles = listOf(
            "현재 연락 경로 버리기",
            "가족은 평소 경로로 콜백",
            "기관·은행·수사기관은 직접 찾은 공식 경로",
            "택배·청첩장·플랫폼 알림은 앱/공식 사이트에서 직접 확인",
            "계정·기기 노출은 별도 기기와 보호자 점검",
            "확인 결과는 값 없이 가족에게 알리기"
        )

        listOf(noAnalysis, analyzed).forEach { coach ->
            assertEquals(
                SeparateChannelVerificationCardId.entries,
                coach.cards.map { card -> card.id }
            )
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
                assertTrue(card.verificationSentence.isNotBlank())
                assertTrue(card.whenToUse.isNotBlank())
                assertTrue(card.whyItMatters.isNotBlank())
                assertTrue(card.doNotUseOrShareBoundaries.isNotEmpty())
                assertTrue(card.doNotUseOrShareBoundaries.all { boundary -> boundary.isNotBlank() })
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
            SeparateChannelVerificationActionId.entries.toSet(),
            usedActions.map { action -> action.id }.toSet()
        )
    }

    @Test
    fun familyShareContainsOnlyGenericCardRouteAndValueFreeUpdateLabels() {
        val rawInput =
            "RAW_VERIFY 박민수 010-9876-5432 계좌 123456789012 카드 4111111111111111 " +
                "OTP 654321 PIN 7788 주민 900101-1234567 https://evil.example " +
                "ShadowRemote com.evil.remote DEVICE-ID-ABC987654"
        val readinessMarker =
            "PLAN_SECRET callback-secret contact-secret 070-1111-2222 " +
                "account-987654321012 https://private.example"
        val coach = SeparateChannelVerificationCoachBuilder.build(
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
            add("[가족 AI 보이스피싱 방패] 별도 경로 확인 코치")
            add("현재 경로와 분리한 수동 확인 중입니다.")
            add("추천 확인 카드")
            coach.recommendedCards.forEach { card -> add("- ${card.title}") }
            add("직접 고를 기존 앱 경로")
            coach.recommendedActions.forEach { action -> add("- ${action.label}") }
            add("가족에게 알릴 값 없는 문장")
            add("- 현재 연락 경로를 쓰지 않고 별도 경로를 직접 확인 중입니다.")
            add(
                "원문·사람·연락처·번호·링크·인증·계정·카드·기기·가족 계획 값 없이 " +
                    "일반 카드와 기존 앱 경로 이름만 담았습니다."
            )
        }
        assertEquals(
            expectedLines,
            summary.lineSequence().filter { line -> line.isNotBlank() }.toList()
        )

        listOf(
            "RAW_VERIFY",
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

        val first = SeparateChannelVerificationCoachBuilder.build(
            firstAnalysis,
            firstReadiness,
            1
        )
        val repeated = SeparateChannelVerificationCoachBuilder.build(
            firstAnalysis,
            firstReadiness,
            1
        )
        val differentForbiddenValues = SeparateChannelVerificationCoachBuilder.build(
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
    fun privacyCopyStatesManualLocalStatelessNoLookupNoAutomationAndNoMutation() {
        val coach = SeparateChannelVerificationCoachBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2
        )
        val copy = allCopy(coach)

        listOf(
            "로컬 전용·무상태 수동 안내",
            "새 저장소를 만들거나 기록하지 않습니다",
            "연락처를 읽거나 쓰거나 고르지 않습니다",
            "공유를 자동 전송하지 않습니다",
            "네트워크·웹 탐색·지도·위치",
            "기관 번호 조회",
            "전화번호 검증",
            "URL·도메인 검증",
            "링크 스캔",
            "새 Android 권한을 요청하지 않습니다",
            "스케줄러를 사용하지 않습니다",
            "전화를 자동으로 걸거나 끊거나 선별·차단하지 않고",
            "문자·SMS를 보내거나 읽지 않으며",
            "기기 관리자·접근성 서비스·VPN",
            "설치 앱·패키지 목록을 조회·설치·제거",
            "결제·은행 기능",
            "추가·수정·삭제하지 않습니다",
            "자동으로 가리거나 모니터링·스캔·삭제·차단·호출·메시지 전송·조회·확인하지 않습니다",
            "증명하거나 보장하지 않습니다"
        ).forEach { expectedBoundary ->
            assertTrue("privacy copy omitted: $expectedBoundary", copy.contains(expectedBoundary))
        }
        assertTrue(copy.contains("원문·가린 단서·사건 내용·가족 안전 계획 값을 사용하지 않습니다"))
        assertTrue(copy.contains("기관 목록"))
        assertTrue(copy.contains("돈의 움직임"))
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndPositiveAutomaticVerificationClaims() {
        val coaches = listOf(
            SeparateChannelVerificationCoachBuilder.build(
                null,
                readiness(ReadinessLevel.READY)
            ),
            SeparateChannelVerificationCoachBuilder.build(
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
                Regex("자동(?:으로)?\\s*(?:차단|동결|정지|삭제|가림|감시|스캔|검사|전송|전화|조회|확인|검증)(?:합니다|됩니다|완료)"),
                Regex("(?:확인|검증|연결 해제|차단|동결|정지|삭제|제거|정리|복구)\\s*(?:완료|됐습니다|되었습니다)"),
                Regex("(?:번호|링크|URL|기관|본인).{0,12}(?:확인 완료|검증 완료|공식 확인)"),
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
    fun unrelatedSignalsProduceDifferentFocusedRecommendationsAndCardsStayManual() {
        fun coachFor(signal: ScamSignal) = SeparateChannelVerificationCoachBuilder.build(
            analysis = analysis(RiskLevel.MEDIUM, signal),
            readiness = readiness(ReadinessLevel.READY)
        )

        val family = coachFor(ScamSignal.FAMILY_EMERGENCY)
        val financial = coachFor(ScamSignal.URGENT_TRANSFER)
        val link = coachFor(ScamSignal.LINK)
        val remote = coachFor(ScamSignal.APP_INSTALL)

        assertNotEquals(family.recommendedCardIds, financial.recommendedCardIds)
        assertNotEquals(family.recommendedActionIds, financial.recommendedActionIds)
        assertNotEquals(link.recommendedCardIds, remote.recommendedCardIds)
        assertNotEquals(link.recommendedActionIds, remote.recommendedActionIds)
        assertTrue(
            SeparateChannelVerificationCardId.FAMILY_KNOWN_PATH_CALLBACK in
                family.recommendedCardIds
        )
        assertTrue(
            SeparateChannelVerificationActionId.MONEY_TRANSFER_PAUSE_GATE in
                financial.recommendedActionIds
        )
        assertTrue(SeparateChannelVerificationActionId.PRIVACY_CENTER in link.recommendedActionIds)
        assertTrue(
            SeparateChannelVerificationActionId.REMOTE_ACCESS_CUTOFF_COACH in
                remote.recommendedActionIds
        )

        remote.cards.forEach { card ->
            assertTrue(
                "verification sentence lacks manual wording: ${card.id}",
                listOf("않고", "직접", "고르겠습니다", "알리겠습니다")
                    .any { phrase -> card.verificationSentence.contains(phrase) }
            )
            assertFalse(Regex("(?i)https?://|www\\.").containsMatchIn(card.verificationSentence))
            assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(card.verificationSentence))
        }
    }

    private fun SeparateChannelVerificationCoach.card(
        id: SeparateChannelVerificationCardId
    ): SeparateChannelVerificationCard = cards.single { card -> card.id == id }

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

    private fun allCopy(coach: SeparateChannelVerificationCoach): String = buildString {
        append(coach.title)
        append(coach.intro)
        append(coach.modeLabel)
        append(coach.recommendationTitle)
        append(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> append(action.label) }
        coach.cards.forEach { card ->
            append(card.title)
            append(card.verificationSentence)
            append(card.whenToUse)
            append(card.whyItMatters)
            card.doNotUseOrShareBoundaries.forEach(::append)
            card.routeActions.forEach { action -> append(action.label) }
        }
        coach.privacySafetyNotes.forEach(::append)
        append(coach.familyShareSummary)
    }
}
