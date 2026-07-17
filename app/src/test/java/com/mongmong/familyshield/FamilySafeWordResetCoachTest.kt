package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilySafeWordResetCoachTest {
    @Test
    fun noAnalysisCoachIsConservativeWithoutDeclaringOldOrNewSecretStatus() {
        val coach = FamilySafeWordResetCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(FamilySafeWordResetCardId.entries, coach.recommendedCardIds)
        assertTrue(
            coach.recommendedActionIds.containsAll(
                listOf(
                    FamilySafeWordResetActionId.CHECKER,
                    FamilySafeWordResetActionId.SAFETY_PLAN,
                    FamilySafeWordResetActionId.FAMILY_READINESS_ROADMAP,
                    FamilySafeWordResetActionId.FAMILY_SAFETY_DRILL_COACH,
                    FamilySafeWordResetActionId.FAMILY_CALLBACK_CARD,
                    FamilySafeWordResetActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                    FamilySafeWordResetActionId.SAFE_CALL_SCRIPTS,
                    FamilySafeWordResetActionId.FAMILY_ALERT_MESSAGES,
                    FamilySafeWordResetActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD,
                    FamilySafeWordResetActionId.RECONTACT_BOUNDARY_COACH,
                    FamilySafeWordResetActionId.TRUSTED_HELPER_REVIEW,
                    FamilySafeWordResetActionId.FAMILY_RESPONSE_ROLE_BOARD,
                    FamilySafeWordResetActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                    FamilySafeWordResetActionId.PRIVACY_CENTER,
                    FamilySafeWordResetActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                    FamilySafeWordResetActionId.SAFETY_FLOW_ROUTER
                )
            )
        )
        assertFalse(
            FamilySafeWordResetActionId.ANALYSIS_RESULT in coach.recommendedActionIds
        )
        assertTrue(coach.recommendationExplanation.contains("어떤 비밀 상태도 단정하지 말고"))
        assertTrue(coach.recommendationExplanation.contains("모든 카드를 보수적으로 직접 읽으세요"))

        val copy = allCopy(coach)
        listOf(
            "이전 안전단어가 무효화됐습니다",
            "이전 문구를 무효화했습니다",
            "새 안전단어를 만들었습니다",
            "새 안전단어가 생성됐습니다",
            "새 값으로 교체했습니다",
            "자동 교체 완료",
            "재설정 완료",
            "새 값이 활성화됐습니다",
            "안전합니다",
            "보안이 확인됐습니다",
            "검증됐습니다",
            "공식 확인됐습니다",
            "업데이트됐습니다",
            "동기화됐습니다",
            "전달됐습니다",
            "완료됐습니다",
            "문제없습니다",
            "100%"
        ).forEach { unsafeClaim ->
            assertFalse(
                "default coach made unsafe claim: $unsafeClaim",
                copy.contains(unsafeClaim, ignoreCase = true)
            )
        }
        assertFalse(
            Regex(
                "(?i)\\b(?:safe|secure|generated|rotated|updated|synced|delivered|" +
                    "verified|complete)\\b"
            ).containsMatchIn(copy)
        )
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendOldPhraseNotEnoughAndSeparateFamilyRoutes() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val coach = FamilySafeWordResetCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "SAFE_WORD_SECRET CALLBACK_SECRET CONTACT_SECRET 010-1111-2222"
                )
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        FamilySafeWordResetCardId.PAUSE_OLD_SECRET,
                        FamilySafeWordResetCardId.CHOOSE_SEPARATE_OFFLINE_CHECK,
                        FamilySafeWordResetCardId.MANUAL_SAFETY_PLAN_EDIT,
                        FamilySafeWordResetCardId.VERIFY_AFTER_FAMILY_PRESSURE,
                        FamilySafeWordResetCardId.FICTIONAL_REHEARSAL_ONLY
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        FamilySafeWordResetActionId.SAFETY_PLAN,
                        FamilySafeWordResetActionId.FAMILY_READINESS_ROADMAP,
                        FamilySafeWordResetActionId.FAMILY_SAFETY_DRILL_COACH,
                        FamilySafeWordResetActionId.FAMILY_CALLBACK_CARD,
                        FamilySafeWordResetActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                        FamilySafeWordResetActionId.SAFE_CALL_SCRIPTS,
                        FamilySafeWordResetActionId.FAMILY_ALERT_MESSAGES,
                        FamilySafeWordResetActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    )
                )
            )
            assertTrue(
                coach.card(FamilySafeWordResetCardId.PAUSE_OLD_SECRET)
                    .valueFreeFamilySentence.contains("이전 문구 하나만으로 상대를 믿지 않고")
            )
            assertTrue(coach.recommendationExplanation.contains("이전 문구만 믿지 않고"))
            val copy = allCopy(coach)
            assertFalse(copy.contains("SAFE_WORD_SECRET"))
            assertFalse(copy.contains("CALLBACK_SECRET"))
            assertFalse(copy.contains("CONTACT_SECRET"))
            assertFalse(copy.contains("010-1111-2222"))
        }
    }

    @Test
    fun impersonationAndSecrecyRecommendNonSecretFamilyHelperEvidenceAndOfficialPrep() {
        listOf(ScamSignal.IMPERSONATION, ScamSignal.SECRECY).forEach { signal ->
            val coach = FamilySafeWordResetCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input = "OFFICIAL_ENTRY_SECRET 112 1394 1332 DIRECTORY_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        FamilySafeWordResetCardId.SHARE_RULE_NOT_VALUE,
                        FamilySafeWordResetCardId.VERIFY_AFTER_FAMILY_PRESSURE
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        FamilySafeWordResetActionId.FAMILY_ALERT_MESSAGES,
                        FamilySafeWordResetActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                        FamilySafeWordResetActionId.TRUSTED_HELPER_REVIEW,
                        FamilySafeWordResetActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                        FamilySafeWordResetActionId.OFFICIAL_HELP_DIRECTORY,
                        FamilySafeWordResetActionId.REPORT_PREP
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("재정비 사실을 혼자 숨기지 말고"))
            assertTrue(coach.recommendationExplanation.contains("실제 값 대신"))

            val copy = allCopy(coach)
            listOf("OFFICIAL_ENTRY_SECRET", "DIRECTORY_VALUE", "112", "1394", "1332")
                .forEach { forbiddenValue ->
                    assertFalse(
                        "official value leaked: $forbiddenValue",
                        Regex("(?<!\\d)${Regex.escape(forbiddenValue)}(?!\\d)")
                            .containsMatchIn(copy)
                    )
                }
        }
    }

    @Test
    fun highRiskAndFinancialSignalsRecommendPauseAccountRemoteAndExposureWithoutOutcomeClaims() {
        val cases = listOf(
            analysis(RiskLevel.HIGH),
            analysis(RiskLevel.MEDIUM, ScamSignal.URGENT_TRANSFER),
            analysis(RiskLevel.MEDIUM, ScamSignal.BAIT),
            analysis(RiskLevel.MEDIUM, ScamSignal.GIFT_CRYPTO),
            analysis(RiskLevel.MEDIUM, ScamSignal.SAFE_ACCOUNT_FRAUD),
            analysis(RiskLevel.MEDIUM, ScamSignal.LOAN_REFINANCE_FEE),
            analysis(RiskLevel.MEDIUM, ScamSignal.GIFTICON_PIN),
            analysis(RiskLevel.MEDIUM, ScamSignal.OVERSEAS_PAYMENT_ALERT)
        )

        cases.forEach { current ->
            val coach = FamilySafeWordResetCoachBuilder.build(
                analysis = current,
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(FamilySafeWordResetCardId.PAUSE_OLD_SECRET in coach.recommendedCardIds)
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        FamilySafeWordResetActionId.MONEY_TRANSFER_PAUSE_GATE,
                        FamilySafeWordResetActionId.ACCOUNT_LOCKDOWN,
                        FamilySafeWordResetActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        FamilySafeWordResetActionId.EXPOSURE_IMPACT_TRIAGE,
                        FamilySafeWordResetActionId.EVIDENCE_KIT
                    )
                )
            )
            val copy = allCopy(coach)
            listOf(
                "송금을 취소했습니다",
                "결제를 취소했습니다",
                "계정을 동결했습니다",
                "카드를 정지했습니다",
                "환급을 보장합니다",
                "피해금을 회수했습니다",
                "복구를 보장합니다",
                "법률 결과를 보장합니다",
                "금융 결과를 보장합니다"
            ).forEach { unsafeOutcome ->
                assertFalse(copy.contains(unsafeOutcome))
            }
        }
    }

    @Test
    fun credentialsAndOtpRecommendPauseAccountAndRemoteWithoutSecretCleanupClaims() {
        listOf(
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = FamilySafeWordResetCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(FamilySafeWordResetCardId.PAUSE_OLD_SECRET in coach.recommendedCardIds)
            assertTrue(
                "credential route omitted money pause for $signal",
                FamilySafeWordResetActionId.MONEY_TRANSFER_PAUSE_GATE in
                    coach.recommendedActionIds
            )
            assertTrue(FamilySafeWordResetActionId.ACCOUNT_LOCKDOWN in coach.recommendedActionIds)
            assertTrue(
                FamilySafeWordResetActionId.REMOTE_ACCESS_CUTOFF_COACH in
                    coach.recommendedActionIds
            )
            assertTrue(
                FamilySafeWordResetActionId.EXPOSURE_IMPACT_TRIAGE in
                    coach.recommendedActionIds
            )
            assertTrue(coach.recommendationExplanation.contains("비밀 정리를 대신하지 않습니다"))
            assertFalse(allCopy(coach).contains("비밀 정리 완료"))
            assertFalse(allCopy(coach).contains("인증값 삭제 완료"))
        }
    }

    @Test
    fun linkAndSmishingKeepNewSecretOffSamePathWithoutUrlOrScanClaims() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val coach = FamilySafeWordResetCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        FamilySafeWordResetCardId.CHOOSE_SEPARATE_OFFLINE_CHECK,
                        FamilySafeWordResetCardId.VERIFY_AFTER_FAMILY_PRESSURE
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        FamilySafeWordResetActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                        FamilySafeWordResetActionId.EXPOSURE_IMPACT_TRIAGE,
                        FamilySafeWordResetActionId.PRIVACY_CENTER
                    )
                )
            )
            val card = coach.card(FamilySafeWordResetCardId.CHOOSE_SEPARATE_OFFLINE_CHECK)
            assertTrue(
                card.doNotSayRecordOrShareBoundaries.any { boundary ->
                    boundary.contains("같은 채팅·통화·링크·앱·화면공유 세션")
                }
            )
            assertTrue(coach.recommendationExplanation.contains("같은 링크·메시지 경로로 새 비밀을 보내지 말고"))
            assertTrue(coach.recommendationExplanation.contains("URL·도메인을 검증하거나"))
            assertTrue(coach.recommendationExplanation.contains("링크를 스캔하지 않습니다"))
            assertFalse(allCopy(coach).contains("URL 검증 완료"))
            assertFalse(allCopy(coach).contains("링크 검사 완료"))
            assertFalse(allCopy(coach).contains("OCR로 확인했습니다"))
        }
    }

    @Test
    fun appInstallAndLiveCallControlLeaveSameSessionWithoutInventoryOrCleaningClaims() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val coach = FamilySafeWordResetCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        FamilySafeWordResetCardId.CHOOSE_SEPARATE_OFFLINE_CHECK,
                        FamilySafeWordResetCardId.VERIFY_AFTER_FAMILY_PRESSURE
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        FamilySafeWordResetActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        FamilySafeWordResetActionId.EXPOSURE_IMPACT_TRIAGE,
                        FamilySafeWordResetActionId.PRIVACY_CENTER,
                        FamilySafeWordResetActionId.TRUSTED_HELPER_REVIEW
                    )
                )
            )
            assertTrue(
                coach.recommendationExplanation.contains("같은 앱·통화·화면 세션을 벗어나는")
            )
            assertTrue(
                coach.recommendationExplanation.contains("설치 앱 목록이나 화면을 읽지 않고")
            )
            assertTrue(coach.recommendationExplanation.contains("기기 검사·정리를 하지 않습니다"))
            val copy = allCopy(coach)
            assertFalse(copy.contains("설치 앱 목록을 확인했습니다"))
            assertFalse(copy.contains("원격 연결을 종료했습니다"))
            assertFalse(copy.contains("악성 프로그램을 제거했습니다"))
            assertFalse(copy.contains("기기 정리 완료"))
        }
    }

    @Test
    fun savedIncidentChangesOnlyCountCategoryWithoutReadingContentsAndClampsCounts() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.FAMILY_EMERGENCY)
        val ready = readiness(ReadinessLevel.READY)
        val zero = FamilySafeWordResetCoachBuilder.build(current, ready, 0)
        val negative = FamilySafeWordResetCoachBuilder.build(current, ready, -99)
        val one = FamilySafeWordResetCoachBuilder.build(current, ready, 1)
        val many = FamilySafeWordResetCoachBuilder.build(current, ready, Int.MAX_VALUE)

        assertEquals(zero, negative)
        assertEquals(one, many)
        assertNotEquals(zero, one)
        assertFalse(
            FamilySafeWordResetActionId.EVIDENCE_HANDOFF_PRIVACY_COACH in
                zero.recommendedActionIds
        )
        assertFalse(FamilySafeWordResetActionId.EVIDENCE_KIT in zero.recommendedActionIds)
        assertTrue(
            FamilySafeWordResetActionId.EVIDENCE_HANDOFF_PRIVACY_COACH in
                one.recommendedActionIds
        )
        assertTrue(FamilySafeWordResetActionId.EVIDENCE_KIT in one.recommendedActionIds)
        assertTrue(
            FamilySafeWordResetActionId.FOLLOW_UP_CHECK_IN_PLANNER in
                one.recommendedActionIds
        )
        assertTrue(one.recommendationExplanation.contains("개수 범주만 반영"))
        assertTrue(one.recommendationExplanation.contains("사건 내용이나 미리보기는 읽지 않습니다"))
        assertFalse(one.familyShareSummary.contains(Int.MAX_VALUE.toString()))
    }

    @Test
    fun readinessLevelsRecommendManualPlanRoadmapDrillAndCallsWhileReadyStaysCautious() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.LINK)
        val low = FamilySafeWordResetCoachBuilder.build(
            current,
            readiness(ReadinessLevel.LOW)
        )
        val inProgress = FamilySafeWordResetCoachBuilder.build(
            current,
            readiness(ReadinessLevel.IN_PROGRESS)
        )
        val ready = FamilySafeWordResetCoachBuilder.build(
            current,
            readiness(ReadinessLevel.READY)
        )

        listOf(low, inProgress).forEach { coach ->
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        FamilySafeWordResetActionId.SAFETY_PLAN,
                        FamilySafeWordResetActionId.FAMILY_READINESS_ROADMAP,
                        FamilySafeWordResetActionId.FAMILY_SAFETY_DRILL_COACH,
                        FamilySafeWordResetActionId.SAFE_CALL_SCRIPTS
                    )
                )
            )
            assertTrue(
                FamilySafeWordResetCardId.MANUAL_SAFETY_PLAN_EDIT in
                    coach.recommendedCardIds
            )
        }
        assertTrue(low.recommendationExplanation.contains("가족 준비가 부족한 범주"))
        assertTrue(inProgress.recommendationExplanation.contains("가족 준비가 진행 중인 범주"))
        assertTrue(ready.recommendationExplanation.contains("기존 항목이 채워졌다는 뜻일 뿐"))
        assertTrue(ready.recommendationExplanation.contains("확인 비밀이 노출되지 않았다는 증거가 아닙니다"))
        assertTrue(ready.recommendationExplanation.contains("실제 답 없이 재정비 상황"))
        assertTrue(ready.recommendationExplanation.contains("가상으로 연습하세요"))
        assertTrue(
            FamilySafeWordResetCardId.FICTIONAL_REHEARSAL_ONLY in
                ready.recommendedCardIds
        )
    }

    @Test
    fun everyCardHasCompleteFieldsStableOrderUniqueRoutesAndEveryActionMapsToAFlow() {
        val noAnalysis = FamilySafeWordResetCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )
        val analyzed = FamilySafeWordResetCoachBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2
        )
        val expectedTitles = listOf(
            "이전 안전단어는 잠시 단독 근거로 믿지 않기",
            "새 확인 질문은 같은 채팅·통화 안에서 정하지 않기",
            "가족 안전 계획은 직접 열어 새 값으로 바꾸기",
            "가족에게는 새 값 대신 바뀔 규칙만 말하기",
            "새 번호·딥페이크·비밀 압박 뒤에는 별도 경로로 다시 확인하기",
            "연습은 가상 문장으로만 하고 실제 답은 말하지 않기"
        )

        listOf(noAnalysis, analyzed).forEach { coach ->
            assertEquals(FamilySafeWordResetCardId.entries, coach.cards.map { it.id })
            assertEquals((1..6).toList(), coach.cards.map { it.displayOrder })
            assertEquals(expectedTitles, coach.cards.map { it.title })
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
                assertTrue(card.resetScenario.isNotBlank())
                assertTrue(card.resetScenario.endsWith("때"))
                assertTrue(card.valueFreeFamilySentence.isNotBlank())
                assertTrue(card.whyItMatters.isNotBlank())
                assertTrue(card.doNotSayRecordOrShareBoundaries.isNotEmpty())
                assertTrue(card.doNotSayRecordOrShareBoundaries.all { it.isNotBlank() })
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
            FamilySafeWordResetActionId.entries.toSet(),
            usedActions.map { action -> action.id }.toSet()
        )
    }

    @Test
    fun familyShareContainsOnlyFixedCardsAndRoutesWithoutSecretsFilesStatusesOrGeneratedOutput() {
        val rawInput =
            "RAW_RESET 박민수 010-9876-5432 계좌 123456789012 카드 4111111111111111 " +
                "OTP 654321 PIN 7788 주민 900101-1234567 https://evil.example " +
                "ShadowRemote com.evil.remote DEVICE-ID-ABC987654 reset-proof.png " +
                "content://private/reset OCR_RESULT RECIPIENT_SECRET RESET_RESULT VERIFIED_RESULT"
        val readinessMarker =
            "OLD_SAFE_WORD NEW_SAFE_WORD callback-secret contact-secret 070-1111-2222 " +
                "account-987654321012 https://private.example family-photo.jpg"
        val coach = FamilySafeWordResetCoachBuilder.build(
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
            add("[가족 AI 보이스피싱 방패] 가족 안전단어 재설정 코치")
            add("실제 질문과 답 없이 가족 확인 루틴의 수동 재정비 순서를 함께 보고 있습니다.")
            add("추천 재정비 카드")
            coach.recommendedCards.forEach { card -> add("- ${card.title}") }
            add("직접 고를 기존 앱 경로")
            coach.recommendedActions.forEach { action -> add("- ${action.label}") }
            add("가족에게 말할 값 없는 문장")
            add("- 실제 답 대신 이전 문구 하나에 기대지 않는 규칙과 별도 확인 순서만 함께 살펴보세요.")
            add(
                "사람·연락처·번호·질문·답·힌트·콜백 문장·원문·사건 내용·링크·계정·" +
                    "인증·기기·파일·받는 사람·결과값 없이 고정된 카드와 기존 앱 경로 이름만 담았습니다."
            )
        }
        assertEquals(
            expectedLines,
            summary.lineSequence().filter { line -> line.isNotBlank() }.toList()
        )

        listOf(
            "RAW_RESET",
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
            "reset-proof.png",
            "content://private/reset",
            "OCR_RESULT",
            "RECIPIENT_SECRET",
            "RESET_RESULT",
            "VERIFIED_RESULT",
            "OLD_SAFE_WORD",
            "NEW_SAFE_WORD",
            "callback-secret",
            "contact-secret",
            "070-1111-2222",
            "987654321012",
            "private.example",
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
        assertFalse(
            Regex("(?:새|실제)\\s*(?:안전단어|질문|답)\\s*[:：=]")
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

        val first = FamilySafeWordResetCoachBuilder.build(firstAnalysis, firstReadiness, 1)
        val repeated = FamilySafeWordResetCoachBuilder.build(firstAnalysis, firstReadiness, 1)
        val differentForbiddenValues = FamilySafeWordResetCoachBuilder.build(
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
    fun privacyCopyStatesManualLocalStatelessNoSecretHandlingNoPlanMutationOrAutomation() {
        val coach = FamilySafeWordResetCoachBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 2
        )
        val copy = allCopy(coach)

        listOf(
            "로컬 전용·무상태 수동 안내",
            "실제 답이나 재정비 결과 입력을 요구하지 않습니다",
            "가족 안전 계획의 실제 값을 사용하지 않습니다",
            "가족 안전 계획으로 이동하는 버튼은 사용자가 원할 때 기존 편집 화면을 열 뿐입니다",
            "표시·공유·수정·저장·삭제하지 않습니다",
            "생성·추천·추론·변환·검사·검증·복사·동기화하거나 자동 교체하지 않고",
            "새 저장소를 만들거나 기록하지 않습니다",
            "카드 선택·추천·읽음·공유·체크·메모",
            "연락처·통화 기록·문자·메시지·통화 상태·알림·클립보드",
            "연락처를 읽거나 쓰거나 고르지 않으며",
            "전화·다이얼·통화 종료·SMS·받는 사람 선택·자동 전송을 하지 않습니다",
            "파일·사진·이미지·미디어·저장소를 읽지 않고",
            "카메라·화면 캡처·스크린샷 가져오기·OCR·이미지 분석",
            "파일·문서·사진 선택기·첨부·스트림·URI 공유",
            "네트워크·웹 탐색·지도·위치",
            "기관 번호 조회·공식 경로 조회·전화번호 검증",
            "링크·URL·도메인 검증·링크 스캔",
            "새 Android 권한을 요청하지 않고",
            "백그라운드 워커·스케줄러를 만들거나 예약하지 않습니다",
            "결제·은행 기능이나 API, NFC, Android 계정 관리자",
            "기기 관리자·접근성 서비스·VPN",
            "설치 앱·패키지 목록 조회",
            "추가·수정·삭제하지 않습니다",
            "자동 모니터링·스캔·OCR",
            "저장·업로드·첨부",
            "수행하지 않습니다"
        ).forEach { expectedBoundary ->
            assertTrue("privacy copy omitted: $expectedBoundary", copy.contains(expectedBoundary))
        }
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndPositiveAutomaticOutcomeClaims() {
        val coaches = listOf(
            FamilySafeWordResetCoachBuilder.build(
                null,
                readiness(ReadinessLevel.READY)
            ),
            FamilySafeWordResetCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.LOW),
                3
            )
        )
        val forbiddenClaims = listOf(
            "100%",
            "old secret invalidated",
            "new secret active",
            "safe and secure",
            "generated",
            "rotated",
            "updated",
            "synced",
            "delivered",
            "verified",
            "officially confirmed",
            "complete",
            "이전 안전단어가 무효화됐습니다",
            "이전 문구를 무효화했습니다",
            "새 안전단어를 생성했습니다",
            "새 안전단어를 만들었습니다",
            "새 값으로 교체했습니다",
            "자동 교체 완료",
            "재설정 완료",
            "새 값이 활성화됐습니다",
            "안전합니다",
            "보안이 확인됐습니다",
            "검증됐습니다",
            "공식 확인됐습니다",
            "업데이트됐습니다",
            "동기화됐습니다",
            "전달됐습니다",
            "완료됐습니다",
            "문제없습니다",
            "계정이 동결됐습니다",
            "카드가 정지됐습니다",
            "결제가 취소됐습니다",
            "송금이 취소됐습니다",
            "환급을 보장합니다",
            "피해금을 회수합니다",
            "복구를 보장합니다",
            "법률 결과를 보장합니다",
            "금융 결과를 보장합니다",
            "자동으로 모니터링합니다",
            "자동으로 스캔합니다",
            "자동으로 OCR합니다",
            "자동으로 삭제합니다",
            "자동으로 가립니다",
            "자동으로 차단합니다",
            "자동으로 전화합니다",
            "메시지를 자동 전송합니다",
            "기관을 자동 조회합니다",
            "번호를 자동 검증합니다",
            "링크를 자동 검증합니다",
            "자동으로 저장합니다",
            "자동으로 업로드합니다",
            "자동으로 첨부합니다"
        )

        coaches.forEach { coach ->
            val copy = allCopy(coach)
            forbiddenClaims.forEach { claim ->
                assertFalse(
                    "coach made unsafe claim: $claim",
                    copy.contains(claim, ignoreCase = true)
                )
            }
            assertFalse(
                Regex(
                    "(?:무효화|생성|교체|재설정|검증|동기화|전달|활성화).{0,8}" +
                        "(?:완료|됐습니다|되었습니다)"
                ).containsMatchIn(copy)
            )
        }
    }

    @Test
    fun unrelatedSignalCategoriesProduceDifferentFocusedRecommendationsAndScenariosStayHypothetical() {
        fun coachFor(signal: ScamSignal) = FamilySafeWordResetCoachBuilder.build(
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
            FamilySafeWordResetCardId.MANUAL_SAFETY_PLAN_EDIT in
                family.recommendedCardIds
        )
        assertTrue(
            FamilySafeWordResetActionId.MONEY_TRANSFER_PAUSE_GATE in
                financial.recommendedActionIds
        )
        assertTrue(
            FamilySafeWordResetActionId.SEPARATE_CHANNEL_VERIFICATION_COACH in
                link.recommendedActionIds
        )
        assertTrue(
            FamilySafeWordResetActionId.REMOTE_ACCESS_CUTOFF_COACH in
                remote.recommendedActionIds
        )

        remote.cards.forEach { card ->
            assertTrue(
                "scenario is not hypothetical: ${card.id}",
                card.resetScenario.endsWith("때")
            )
            assertFalse(Regex("(?i)https?://|www\\.").containsMatchIn(card.resetScenario))
            assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(card.resetScenario))
        }
    }

    private fun FamilySafeWordResetCoach.card(
        id: FamilySafeWordResetCardId
    ): FamilySafeWordResetCard = cards.single { card -> card.id == id }

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

    private fun allCopy(coach: FamilySafeWordResetCoach): String = buildString {
        append(coach.title)
        append(coach.intro)
        append(coach.modeLabel)
        append(coach.recommendationTitle)
        append(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> append(action.label) }
        coach.cards.forEach { card ->
            append(card.title)
            append(card.resetScenario)
            append(card.valueFreeFamilySentence)
            append(card.whyItMatters)
            card.doNotSayRecordOrShareBoundaries.forEach(::append)
            card.routeActions.forEach { action -> append(action.label) }
        }
        coach.privacySafetyNotes.forEach(::append)
        append(coach.familyShareSummary)
    }
}
