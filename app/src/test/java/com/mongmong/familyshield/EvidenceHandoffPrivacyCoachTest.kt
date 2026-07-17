package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvidenceHandoffPrivacyCoachTest {
    @Test
    fun noAnalysisCoachIsConservativeWithoutDeclaringEvidenceReadyOrAnyOutcomeComplete() {
        val coach = EvidenceHandoffPrivacyCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(EvidenceHandoffPrivacyCardId.entries, coach.recommendedCardIds)
        assertTrue(
            coach.recommendedActionIds.containsAll(
                listOf(
                    EvidenceHandoffPrivacyActionId.QUICK_STOP,
                    EvidenceHandoffPrivacyActionId.CHECKER,
                    EvidenceHandoffPrivacyActionId.SAFE_PASTE_PRIVACY_COACH,
                    EvidenceHandoffPrivacyActionId.PRIVACY_CENTER,
                    EvidenceHandoffPrivacyActionId.EXPOSURE_IMPACT_TRIAGE,
                    EvidenceHandoffPrivacyActionId.FAMILY_CALLBACK_CARD,
                    EvidenceHandoffPrivacyActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                    EvidenceHandoffPrivacyActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD,
                    EvidenceHandoffPrivacyActionId.SAFE_CALL_SCRIPTS,
                    EvidenceHandoffPrivacyActionId.FAMILY_ALERT_MESSAGES,
                    EvidenceHandoffPrivacyActionId.TRUSTED_HELPER_REVIEW,
                    EvidenceHandoffPrivacyActionId.MONEY_TRANSFER_PAUSE_GATE,
                    EvidenceHandoffPrivacyActionId.ACCOUNT_LOCKDOWN,
                    EvidenceHandoffPrivacyActionId.REMOTE_ACCESS_CUTOFF_COACH,
                    EvidenceHandoffPrivacyActionId.EVIDENCE_KIT,
                    EvidenceHandoffPrivacyActionId.OFFICIAL_HELP_DIRECTORY,
                    EvidenceHandoffPrivacyActionId.RECOVERY_PLAN,
                    EvidenceHandoffPrivacyActionId.FOLLOW_UP_CHECK_IN_PLANNER,
                    EvidenceHandoffPrivacyActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                    EvidenceHandoffPrivacyActionId.SAFETY_FLOW_ROUTER
                )
            )
        )
        assertFalse(
            EvidenceHandoffPrivacyActionId.ANALYSIS_RESULT in coach.recommendedActionIds
        )
        assertFalse(EvidenceHandoffPrivacyActionId.REPORT_PREP in coach.recommendedActionIds)
        assertFalse(
            EvidenceHandoffPrivacyActionId.INCIDENT_TIMELINE_BRIEFING in
                coach.recommendedActionIds
        )
        assertTrue(coach.recommendationExplanation.contains("현재 분석이 없거나 일치 신호가 없습니다"))
        assertTrue(coach.recommendationExplanation.contains("모든 카드를 보수적으로 직접 읽으세요"))

        val copy = allCopy(coach)
        listOf(
            "증거가 안전합니다",
            "증거가 정리됐습니다",
            "증거를 가렸습니다",
            "업로드됐습니다",
            "제출됐습니다",
            "검증됐습니다",
            "공식으로 확인됐습니다",
            "해결됐습니다",
            "복구됐습니다",
            "차단됐습니다",
            "증거 준비 완료",
            "문제없습니다",
            "100%",
            " safe ",
            "sanitized",
            "redacted",
            "uploaded",
            "submitted",
            "verified",
            "officially confirmed",
            "resolved",
            "recovered",
            "blocked",
            "complete",
            "problem-free"
        ).forEach { unsafeClaim ->
            assertFalse(
                "default coach made unsafe claim: $unsafeClaim",
                copy.contains(unsafeClaim, ignoreCase = true)
            )
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendValueFreeFamilyAndSeparateChannelRoutes() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val coach = EvidenceHandoffPrivacyCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "SAFE_WORD_SECRET CALLBACK_SECRET 010-1111-2222"
                )
            )

            assertTrue(
                EvidenceHandoffPrivacyCardId.FAMILY_HELPER_ACTION_CATEGORIES in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        EvidenceHandoffPrivacyActionId.FAMILY_CALLBACK_CARD,
                        EvidenceHandoffPrivacyActionId.SAFE_CALL_SCRIPTS,
                        EvidenceHandoffPrivacyActionId.FAMILY_ALERT_MESSAGES,
                        EvidenceHandoffPrivacyActionId.TRUSTED_HELPER_REVIEW,
                        EvidenceHandoffPrivacyActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                        EvidenceHandoffPrivacyActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("가족 확인 비밀 없이 행동 종류만 공유"))
            assertTrue(
                coach.card(EvidenceHandoffPrivacyCardId.FAMILY_HELPER_ACTION_CATEGORIES)
                    .privacyActionSentence.contains("행동을 멈췄는지만 말하겠습니다")
            )
            assertFalse(allCopy(coach).contains("SAFE_WORD_SECRET"))
            assertFalse(allCopy(coach).contains("CALLBACK_SECRET"))
            assertFalse(allCopy(coach).contains("010-1111-2222"))
        }
    }

    @Test
    fun impersonationAndSecrecyRecommendNonSecretFamilyEvidenceHelpReportAndSeparateRoutes() {
        listOf(ScamSignal.IMPERSONATION, ScamSignal.SECRECY).forEach { signal ->
            val coach = EvidenceHandoffPrivacyCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        EvidenceHandoffPrivacyCardId.FAMILY_HELPER_ACTION_CATEGORIES,
                        EvidenceHandoffPrivacyCardId.OFFICIAL_CONSULTATION_PREP
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        EvidenceHandoffPrivacyActionId.FAMILY_ALERT_MESSAGES,
                        EvidenceHandoffPrivacyActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                        EvidenceHandoffPrivacyActionId.EVIDENCE_KIT,
                        EvidenceHandoffPrivacyActionId.OFFICIAL_HELP_DIRECTORY,
                        EvidenceHandoffPrivacyActionId.REPORT_PREP,
                        EvidenceHandoffPrivacyActionId.SEPARATE_CHANNEL_VERIFICATION_COACH
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("증거 전달을 혼자 숨기지 않고"))
            assertTrue(
                coach.card(EvidenceHandoffPrivacyCardId.OFFICIAL_CONSULTATION_PREP)
                    .privacyActionSentence.contains("행동 종류와 지금 필요한 도움만")
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
            val coach = EvidenceHandoffPrivacyCoachBuilder.build(
                analysis = current,
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                EvidenceHandoffPrivacyCardId.MONEY_ACCOUNT_CREDENTIALS in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        EvidenceHandoffPrivacyActionId.MONEY_TRANSFER_PAUSE_GATE,
                        EvidenceHandoffPrivacyActionId.ACCOUNT_LOCKDOWN,
                        EvidenceHandoffPrivacyActionId.EVIDENCE_KIT,
                        EvidenceHandoffPrivacyActionId.OFFICIAL_HELP_DIRECTORY,
                        EvidenceHandoffPrivacyActionId.REPORT_PREP,
                        EvidenceHandoffPrivacyActionId.RECOVERY_PLAN,
                        EvidenceHandoffPrivacyActionId.FOLLOW_UP_CHECK_IN_PLANNER
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

        val highWithoutFinancialSignal = EvidenceHandoffPrivacyCoachBuilder.build(
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
                "높은 위험 신호에서 원본 보관"
            )
        )
        assertFalse(highWithoutFinancialSignal.recommendationTitle.contains("돈 관련 값을"))
    }

    @Test
    fun credentialsAndOtpRecommendAccountAndRemoteCutoffWithoutFreezeOrSecretCleanupClaim() {
        listOf(
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = EvidenceHandoffPrivacyCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                EvidenceHandoffPrivacyCardId.MONEY_ACCOUNT_CREDENTIALS in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        EvidenceHandoffPrivacyActionId.ACCOUNT_LOCKDOWN,
                        EvidenceHandoffPrivacyActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        EvidenceHandoffPrivacyActionId.EVIDENCE_KIT
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("비밀값을 지우거나"))
            assertTrue(coach.recommendationExplanation.contains("자동 정지·동결하는 기능은 아닙니다"))
            assertFalse(allCopy(coach).contains("비밀값 삭제 완료"))
            assertFalse(allCopy(coach).contains("계정 동결 완료"))
            assertFalse(allCopy(coach).contains("카드 정지 완료"))
        }
    }

    @Test
    fun appInstallAndLiveCallControlRecommendRemoteHelperAccountAndEvidenceWithoutDeviceClaims() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val coach = EvidenceHandoffPrivacyCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                EvidenceHandoffPrivacyCardId.LINK_APP_REMOTE_DETAILS in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        EvidenceHandoffPrivacyActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        EvidenceHandoffPrivacyActionId.TRUSTED_HELPER_REVIEW,
                        EvidenceHandoffPrivacyActionId.ACCOUNT_LOCKDOWN,
                        EvidenceHandoffPrivacyActionId.EVIDENCE_KIT
                    )
                )
            )
            val copy = allCopy(coach)
            assertTrue(copy.contains("화면이나 설치 목록을 검토하지 않고"))
            assertTrue(copy.contains("원격 연결 해제·악성 프로그램 제거·기기 정리"))
            assertFalse(copy.contains("스크린샷을 검토했습니다"))
            assertFalse(copy.contains("원격 연결을 해제했습니다"))
            assertFalse(copy.contains("악성 프로그램을 제거했습니다"))
            assertFalse(copy.contains("기기 정리 완료"))
        }
    }

    @Test
    fun linkAndSmishingRecommendCheckerExposurePrivacySafePasteAndEvidenceWithoutScanClaim() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val coach = EvidenceHandoffPrivacyCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                EvidenceHandoffPrivacyCardId.LINK_APP_REMOTE_DETAILS in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        EvidenceHandoffPrivacyActionId.CHECKER,
                        EvidenceHandoffPrivacyActionId.EXPOSURE_IMPACT_TRIAGE,
                        EvidenceHandoffPrivacyActionId.PRIVACY_CENTER,
                        EvidenceHandoffPrivacyActionId.SAFE_PASTE_PRIVACY_COACH,
                        EvidenceHandoffPrivacyActionId.EVIDENCE_KIT
                    )
                )
            )
            val card = coach.card(EvidenceHandoffPrivacyCardId.LINK_APP_REMOTE_DETAILS)
            assertTrue(card.handoffScenario.contains("링크·설치 지시"))
            assertTrue(card.doNotAttachRecordOrShareBoundaries.any { it.contains("URL·도메인") })
            assertTrue(
                coach.recommendationExplanation.contains(
                    "링크를 스캔하거나 URL·도메인을 검증하지 않습니다"
                )
            )
            assertFalse(allCopy(coach).contains("링크를 자동 검사합니다"))
            assertFalse(allCopy(coach).contains("URL 검증 완료"))
            assertFalse(allCopy(coach).contains("OCR로 읽었습니다"))
            assertFalse(allCopy(coach).contains("자동으로 가렸습니다"))
        }
    }

    @Test
    fun savedIncidentChangesRecommendationsWithoutReadingContentsAndClampsCounts() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.FAMILY_EMERGENCY)
        val ready = readiness(ReadinessLevel.READY)
        val zero = EvidenceHandoffPrivacyCoachBuilder.build(current, ready, 0)
        val negative = EvidenceHandoffPrivacyCoachBuilder.build(current, ready, -99)
        val one = EvidenceHandoffPrivacyCoachBuilder.build(current, ready, 1)
        val many = EvidenceHandoffPrivacyCoachBuilder.build(current, ready, Int.MAX_VALUE)

        assertEquals(zero, negative)
        assertEquals(one, many)
        assertNotEquals(zero, one)
        assertFalse(
            EvidenceHandoffPrivacyActionId.INCIDENT_TIMELINE_BRIEFING in
                zero.recommendedActionIds
        )
        assertFalse(EvidenceHandoffPrivacyActionId.REPORT_PREP in zero.recommendedActionIds)
        assertTrue(
            EvidenceHandoffPrivacyActionId.INCIDENT_TIMELINE_BRIEFING in
                one.recommendedActionIds
        )
        assertTrue(EvidenceHandoffPrivacyActionId.REPORT_PREP in one.recommendedActionIds)
        assertTrue(one.recommendationExplanation.contains("개수 범주만 반영"))
        assertTrue(one.recommendationExplanation.contains("사건 내용은 읽지 않습니다"))
        assertFalse(one.familyShareSummary.contains(Int.MAX_VALUE.toString()))
    }

    @Test
    fun lowAndInProgressReadinessAddFamilySupportHelperAndRoadmapWhileReadyStaysCautious() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.LINK)
        val low = EvidenceHandoffPrivacyCoachBuilder.build(
            current,
            readiness(ReadinessLevel.LOW)
        )
        val inProgress = EvidenceHandoffPrivacyCoachBuilder.build(
            current,
            readiness(ReadinessLevel.IN_PROGRESS)
        )
        val ready = EvidenceHandoffPrivacyCoachBuilder.build(
            current,
            readiness(ReadinessLevel.READY)
        )

        listOf(low, inProgress).forEach { coach ->
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        EvidenceHandoffPrivacyActionId.FAMILY_CALLBACK_CARD,
                        EvidenceHandoffPrivacyActionId.SAFE_CALL_SCRIPTS,
                        EvidenceHandoffPrivacyActionId.FAMILY_ALERT_MESSAGES,
                        EvidenceHandoffPrivacyActionId.TRUSTED_HELPER_REVIEW,
                        EvidenceHandoffPrivacyActionId.FAMILY_READINESS_ROADMAP
                    )
                )
            )
        }
        assertFalse(
            EvidenceHandoffPrivacyActionId.FAMILY_READINESS_ROADMAP in
                ready.recommendedActionIds
        )
        assertTrue(low.recommendationExplanation.contains("가족 준비가 부족한 상태"))
        assertTrue(inProgress.recommendationExplanation.contains("가족 준비가 진행 중"))
        assertTrue(ready.recommendationExplanation.contains("가족 준비가 되어 있어도"))
        assertTrue(ready.recommendationExplanation.contains("전달 결과를 확인했다는 뜻은 아닙니다"))
    }

    @Test
    fun everyCardHasCompleteFieldsStableOrderUniqueRoutesAndEveryActionMapsToAFlow() {
        val noAnalysis = EvidenceHandoffPrivacyCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )
        val analyzed = EvidenceHandoffPrivacyCoachBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2
        )
        val expectedTitles = listOf(
            "원문·스크린샷 보내기 전: 민감값 먼저 빼기",
            "가족·보호자에게 줄 때: 행동 종류만 공유하기",
            "기관·상담 준비: 원본은 내가 보관하고 말할 내용만 정리하기",
            "돈·계정·OTP 노출 증거: 값은 말하지 않고 보호 경로로 이동",
            "링크·앱·원격지시 증거: URL·앱·기기값 퍼뜨리지 않기",
            "후속 점검: 전달 기록을 남기지 않고 가족과 다시 보기"
        )

        listOf(noAnalysis, analyzed).forEach { coach ->
            assertEquals(EvidenceHandoffPrivacyCardId.entries, coach.cards.map { it.id })
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
                assertTrue(card.handoffScenario.isNotBlank())
                assertTrue(card.handoffScenario.endsWith("때"))
                assertTrue(card.privacyActionSentence.isNotBlank())
                assertTrue(card.whyItMatters.isNotBlank())
                assertTrue(card.doNotAttachRecordOrShareBoundaries.isNotEmpty())
                assertTrue(card.doNotAttachRecordOrShareBoundaries.all { it.isNotBlank() })
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
            EvidenceHandoffPrivacyActionId.entries.toSet(),
            usedActions.map { action -> action.id }.toSet()
        )
    }

    @Test
    fun familyShareContainsOnlyFixedCardAndRouteLabelsWithoutSecretsFilesOrResultValues() {
        val rawInput =
            "RAW_HANDOFF 박민수 010-9876-5432 계좌 123456789012 카드 4111111111111111 " +
                "OTP 654321 PIN 7788 주민 900101-1234567 https://evil.example " +
                "ShadowRemote com.evil.remote DEVICE-ID-ABC987654 screenshot-secret.png " +
                "content://private/evidence OCR_RESULT RECIPIENT_SECRET HANDOFF_RESULT"
        val readinessMarker =
            "PLAN_SECRET callback-secret contact-secret 070-1111-2222 " +
                "account-987654321012 https://private.example SAFE_WORD_VALUE family-photo.jpg"
        val coach = EvidenceHandoffPrivacyCoachBuilder.build(
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
            add("[가족 AI 보이스피싱 방패] 증거 전달 개인정보 코치")
            add("증거를 보내기 전에 값 없는 전달 경계와 기존 앱 경로를 함께 보고 있습니다.")
            add("추천 전달 경계 카드")
            coach.recommendedCards.forEach { card -> add("- ${card.title}") }
            add("직접 고를 기존 앱 경로")
            coach.recommendedActions.forEach { action -> add("- ${action.label}") }
            add("가족에게 말할 값 없는 문장")
            add("- 원본은 내가 따로 보관하고, 필요한 행동 종류와 다음 앱 경로만 함께 고르겠습니다.")
            add(
                "원문·민감값·사람·연락처·번호·링크·계정·카드·인증·기기·가족 계획·" +
                    "파일·경로·받는 사람·전달 결과 없이 고정된 카드와 기존 앱 경로 이름만 담았습니다."
            )
        }
        assertEquals(
            expectedLines,
            summary.lineSequence().filter { it.isNotBlank() }.toList()
        )

        listOf(
            "RAW_HANDOFF",
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
            "screenshot-secret.png",
            "content://private/evidence",
            "OCR_RESULT",
            "RECIPIENT_SECRET",
            "HANDOFF_RESULT",
            "PLAN_SECRET",
            "callback-secret",
            "contact-secret",
            "070-1111-2222",
            "987654321012",
            "private.example",
            "SAFE_WORD_VALUE",
            "family-photo.jpg"
        ).forEach { secret ->
            assertFalse("family share leaked secret: $secret", summary.contains(secret))
        }
        assertFalse(Regex("01[016789][- .]?\\d{3,4}[- .]?\\d{4}").containsMatchIn(summary))
        assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(summary))
        assertFalse(
            Regex("(?i)https?://|content://|www\\.|[a-z0-9-]+\\.(?:com|net|kr)")
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
            input = "FIRST_RAW 010-1111-2222 https://first.example 12345678 file-one.png",
            score = 31,
            riskLevel = RiskLevel.HIGH,
            matchedSignals = signals
        )
        val secondAnalysis = firstAnalysis.copy(
            input = "SECOND_RAW 010-9999-8888 https://second.example 87654321 file-two.jpg",
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

        val first = EvidenceHandoffPrivacyCoachBuilder.build(firstAnalysis, firstReadiness, 1)
        val repeated = EvidenceHandoffPrivacyCoachBuilder.build(firstAnalysis, firstReadiness, 1)
        val differentForbiddenValues = EvidenceHandoffPrivacyCoachBuilder.build(
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
            "file-one.png",
            "1000"
        ).forEach { ignoredValue ->
            assertFalse(allCopy(first).contains(ignoredValue))
            assertFalse(allCopy(differentForbiddenValues).contains(ignoredValue))
        }
    }

    @Test
    fun privacyCopyStatesManualLocalNoHandoffStorageNoFileAccessNoAutomationAndNoMutation() {
        val coach = EvidenceHandoffPrivacyCoachBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2
        )
        val copy = allCopy(coach)

        listOf(
            "로컬 전용·무상태 수동 안내",
            "증거·받는 사람·전달 결과 입력을 요구하지 않습니다",
            "증거 전달 저장소를 만들거나 기록하지 않습니다",
            "파일·사진·이미지·미디어·저장소를 읽지 않고",
            "카메라를 사용하거나 화면을 캡처하지 않으며",
            "스크린샷을 가져오거나 OCR·이미지 분석을 하지 않습니다",
            "첨부하거나 업로드하지 않고 스트림·URI로 공유하지 않으며",
            "파일·문서·사진 선택기를 열지 않습니다",
            "연락처·통화 기록·문자·메시지·통화 상태·알림 내용을 읽거나 감시하지 않고",
            "연락처를 읽거나 쓰거나 고르지 않습니다",
            "전화 앱을 열거나 자동 다이얼·발신·통화·종료를 하지 않고",
            "문자·SMS를 보내거나 읽지 않습니다",
            "공유를 자동 전송하지 않고",
            "알림 리스너·알림 모니터링",
            "클립보드를 읽거나 쓰지 않고",
            "네트워크·웹 탐색·지도·위치",
            "기관 번호 조회·공식 경로 조회",
            "전화번호 검증",
            "URL·도메인 검증·링크 스캔",
            "새 Android 권한을 요청하지 않습니다",
            "스케줄러를 사용하지 않습니다",
            "결제·은행 기능이나 API",
            "기기 제어 기능을 사용하지 않고",
            "추가·수정·삭제하지 않습니다",
            "자동으로 가리거나 모니터링·스캔·OCR·삭제·차단·호출·메시지 전송·조회·확인·검증·저장·업로드·제출·첨부하지 않습니다",
            "증명하거나 보장하지 않습니다"
        ).forEach { expectedBoundary ->
            assertTrue("privacy copy omitted: $expectedBoundary", copy.contains(expectedBoundary))
        }
        assertTrue(
            copy.contains("원문·가린 단서·사건 내용·가족 안전 계획 값을 사용하지 않습니다")
        )
        assertTrue(copy.contains("증거 전달 상태"))
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndPositiveAutomaticOutcomeClaims() {
        val coaches = listOf(
            EvidenceHandoffPrivacyCoachBuilder.build(
                null,
                readiness(ReadinessLevel.READY)
            ),
            EvidenceHandoffPrivacyCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.LOW),
                3
            )
        )
        val forbiddenClaims = listOf(
            "100%",
            " safe ",
            "sanitized",
            "redacted",
            "uploaded",
            "submitted",
            "verified",
            "officially confirmed",
            "resolved",
            "recovered",
            "blocked",
            "complete",
            "안전합니다",
            "문제없습니다",
            "증거 가림 완료",
            "증거 제출 완료",
            "증거가 접수됐습니다",
            "증거가 확인됐습니다",
            "공식 증거입니다",
            "링크 검증 완료",
            "번호 검증 완료",
            "통화를 차단했습니다",
            "문자를 차단했습니다",
            "알림을 차단했습니다",
            "원격 접근이 끊겼습니다",
            "스크린샷을 검토했습니다",
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
            "자동으로 OCR합니다",
            "자동으로 삭제합니다",
            "자동으로 가립니다",
            "자동으로 차단합니다",
            "자동으로 전화합니다",
            "메시지를 자동 전송합니다",
            "문자를 자동 전송합니다",
            "기관을 자동 조회합니다",
            "번호를 자동 검증합니다",
            "링크를 자동 검증합니다",
            "증거를 자동 저장합니다",
            "증거를 자동 업로드합니다",
            "증거를 자동 제출합니다",
            "파일을 자동 첨부합니다",
            "전달 결과를 저장합니다"
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
                Regex("자동(?:으로)?\\s*(?:차단|동결|정지|삭제|가림|감시|스캔|검사|OCR|전송|전화|조회|확인|검증|저장|업로드|제출|첨부)(?:합니다|됩니다|완료)"),
                Regex("(?:확인|검증|제출|접수|연결 해제|차단|동결|정지|삭제|제거|정리|복구|회복)\\s*(?:완료|됐습니다|되었습니다)"),
                Regex("(?:번호|링크|URL|기관|본인|증거|전달).{0,12}(?:확인 완료|검증 완료|공식 확인|저장 완료|제출 완료|접수 완료)"),
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
    fun unrelatedSignalCategoriesProduceDifferentRecommendationsAndScenariosStayHypothetical() {
        fun coachFor(signal: ScamSignal) = EvidenceHandoffPrivacyCoachBuilder.build(
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
            EvidenceHandoffPrivacyCardId.FAMILY_HELPER_ACTION_CATEGORIES in
                family.recommendedCardIds
        )
        assertTrue(
            EvidenceHandoffPrivacyActionId.MONEY_TRANSFER_PAUSE_GATE in
                financial.recommendedActionIds
        )
        assertTrue(EvidenceHandoffPrivacyActionId.SAFE_PASTE_PRIVACY_COACH in link.recommendedActionIds)
        assertTrue(
            EvidenceHandoffPrivacyActionId.REMOTE_ACCESS_CUTOFF_COACH in
                remote.recommendedActionIds
        )

        remote.cards.forEach { card ->
            assertTrue(
                "scenario is not hypothetical: ${card.id}",
                card.handoffScenario.endsWith("때")
            )
            assertFalse(Regex("(?i)https?://|www\\.").containsMatchIn(card.handoffScenario))
            assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(card.handoffScenario))
        }
    }

    private fun EvidenceHandoffPrivacyCoach.card(
        id: EvidenceHandoffPrivacyCardId
    ): EvidenceHandoffPrivacyCard = cards.single { card -> card.id == id }

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

    private fun allCopy(coach: EvidenceHandoffPrivacyCoach): String = buildString {
        append(coach.title)
        append(coach.intro)
        append(coach.modeLabel)
        append(coach.recommendationTitle)
        append(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> append(action.label) }
        coach.cards.forEach { card ->
            append(card.title)
            append(card.handoffScenario)
            append(card.privacyActionSentence)
            append(card.whyItMatters)
            card.doNotAttachRecordOrShareBoundaries.forEach(::append)
            card.routeActions.forEach { action -> append(action.label) }
        }
        coach.privacySafetyNotes.forEach(::append)
        append(coach.familyShareSummary)
    }
}
