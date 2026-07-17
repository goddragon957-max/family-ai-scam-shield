package com.mongmong.familyshield

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewNumberQuarantineCoachTest {
    @Test
    fun noAnalysisCoachIsConservativeAndNeverDeclaresNewContactStatus() {
        val coach = NewNumberQuarantineCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(NewNumberQuarantineCardId.entries, coach.recommendedCardIds)
        assertEquals(
            NewNumberQuarantineActionId.entries.filterNot { actionId ->
                actionId == NewNumberQuarantineActionId.ANALYSIS_RESULT
            },
            coach.recommendedActionIds
        )
        assertTrue(coach.recommendationExplanation.contains("모든 격리 카드를 보수적으로"))
        assertTrue(coach.recommendationExplanation.contains("단정하지 말고"))

        val copy = allCopy(coach)
        listOf(
            "trusted number",
            "verified new number",
            "official number",
            "safe number",
            "secure channel",
            "blocked contact",
            "saved contact",
            "updated contact",
            "replaced contact",
            "confirmed profile",
            "resolved channel",
            "clean number",
            "spam-free",
            "problem-free",
            "신뢰하는 번호입니다",
            "확인된 새 번호입니다",
            "공식 번호입니다",
            "안전한 번호입니다",
            "보안이 확인된 채널입니다",
            "연락처를 차단했습니다",
            "연락처를 저장했습니다",
            "연락처를 갱신했습니다",
            "연락처를 교체했습니다",
            "프로필을 확인했습니다",
            "문제가 해결됐습니다",
            "스팸이 아닙니다",
            "문제없습니다",
            "100%"
        ).forEach { unsafeClaim ->
            assertFalse(
                "default coach made unsafe claim: $unsafeClaim",
                copy.contains(unsafeClaim, ignoreCase = true)
            )
        }
    }

    @Test
    fun numberChangeFamilyEmergencyAndDeepfakeRecommendFamilyVerificationRoutes() {
        listOf(
            ScamSignal.PHONE_NUMBER_CHANGE,
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE
        ).forEach { signal ->
            val coach = NewNumberQuarantineCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "SAFE_WORD_SECRET CALLBACK_SECRET CONTACT_SECRET 010-1111-2222"
                )
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        NewNumberQuarantineCardId.DO_NOT_SAVE_OR_REPLACE_NEW_CHANNEL,
                        NewNumberQuarantineCardId.STOP_SAME_CHANNEL_EXCHANGE,
                        NewNumberQuarantineCardId
                            .VERIFY_THROUGH_ESTABLISHED_FAMILY_ROUTE,
                        NewNumberQuarantineCardId.SHARE_ACTION_CATEGORIES_ONLY,
                        NewNumberQuarantineCardId.KEEP_PLAN_AND_SAFE_WORD_UNCHANGED
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        NewNumberQuarantineActionId.FAMILY_CALLBACK_CARD,
                        NewNumberQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                        NewNumberQuarantineActionId.SAFE_CALL_SCRIPTS,
                        NewNumberQuarantineActionId
                            .VOICE_IMPERSONATION_REALITY_CHECK_COACH,
                        NewNumberQuarantineActionId.FAMILY_ALERT_MESSAGES,
                        NewNumberQuarantineActionId.FAMILY_SAFE_WORD_RESET_COACH,
                        NewNumberQuarantineActionId
                            .VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    )
                )
            )
            assertTrue(
                coach.card(NewNumberQuarantineCardId.DO_NOT_SAVE_OR_REPLACE_NEW_CHANNEL)
                    .valueFreeActionSentence.contains("주소록에 넣거나")
            )
            assertTrue(coach.recommendationExplanation.contains("기존 연락과 바꾸지 않고"))
            listOf(
                "SAFE_WORD_SECRET",
                "CALLBACK_SECRET",
                "CONTACT_SECRET",
                "010-1111-2222"
            ).forEach { forbiddenValue ->
                assertFalse(allCopy(coach).contains(forbiddenValue))
            }
        }
    }

    @Test
    fun impersonationAndSecrecyRejectSecretChannelAndRecommendManualHelpRoutes() {
        listOf(ScamSignal.IMPERSONATION, ScamSignal.SECRECY).forEach { signal ->
            val coach = NewNumberQuarantineCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input = "OFFICIAL_ENTRY_SECRET DIRECTORY_VALUE 112 1394 1332"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        NewNumberQuarantineCardId.STOP_SAME_CHANNEL_EXCHANGE,
                        NewNumberQuarantineCardId.SHARE_ACTION_CATEGORIES_ONLY
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        NewNumberQuarantineActionId.FAMILY_ALERT_MESSAGES,
                        NewNumberQuarantineActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                        NewNumberQuarantineActionId.TRUSTED_HELPER_REVIEW,
                        NewNumberQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                        NewNumberQuarantineActionId.PRIVACY_CENTER,
                        NewNumberQuarantineActionId.OFFICIAL_HELP_DIRECTORY,
                        NewNumberQuarantineActionId.REPORT_PREP
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("새 연락을 숨긴 채 이어가지 말고"))
            assertTrue(coach.recommendationExplanation.contains("기관 번호나 도움 목록 값은 사용하지"))

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
    fun financialPaymentCredentialAndOtpSignalsRecommendOnlySupportingRoutes() {
        val cases = listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.BAIT,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT,
            ScamSignal.CREDENTIALS
        )

        cases.forEach { signal ->
            val coach = NewNumberQuarantineCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, signal),
                readiness(ReadinessLevel.READY)
            )

            assertTrue(
                NewNumberQuarantineCardId.STOP_SAME_CHANNEL_EXCHANGE in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        NewNumberQuarantineActionId.MONEY_TRANSFER_PAUSE_GATE,
                        NewNumberQuarantineActionId.ACCOUNT_LOCKDOWN,
                        NewNumberQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        NewNumberQuarantineActionId.EXPOSURE_IMPACT_TRIAGE
                    )
                )
            )
            val copy = allCopy(coach)
            listOf(
                "송금을 취소했습니다",
                "결제를 취소했습니다",
                "연락처를 차단했습니다",
                "계정을 동결했습니다",
                "카드를 정지했습니다",
                "환급을 보장합니다",
                "피해금을 회수했습니다",
                "복구를 보장합니다",
                "비밀 정리 완료",
                "인증값 삭제 완료"
            ).forEach { unsafeOutcome -> assertFalse(copy.contains(unsafeOutcome)) }
        }
    }

    @Test
    fun linkSmishingAppInstallAndLiveCallControlLeaveSameSessionWithoutTechClaims() {
        listOf(
            ScamSignal.LINK,
            ScamSignal.PARCEL_INVITATION_SMISHING,
            ScamSignal.APP_INSTALL,
            ScamSignal.LIVE_CALL_CONTROL
        ).forEach { signal ->
            val coach = NewNumberQuarantineCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, signal),
                readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        NewNumberQuarantineCardId.DO_NOT_SAVE_OR_REPLACE_NEW_CHANNEL,
                        NewNumberQuarantineCardId.STOP_SAME_CHANNEL_EXCHANGE,
                        NewNumberQuarantineCardId.SHARE_ACTION_CATEGORIES_ONLY
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        NewNumberQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                        NewNumberQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        NewNumberQuarantineActionId.EXPOSURE_IMPACT_TRIAGE,
                        NewNumberQuarantineActionId.PRIVACY_CENTER
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("같은 새 연락"))
            val copy = allCopy(coach)
            listOf(
                "URL 검증 완료",
                "링크 검사 완료",
                "설치 앱 목록을 확인했습니다",
                "악성 앱을 제거했습니다",
                "원격 연결을 종료했습니다",
                "기기 정리 완료"
            ).forEach { unsafeClaim -> assertFalse(copy.contains(unsafeClaim)) }
        }
    }

    @Test
    fun savedIncidentChangesOnlyCountCategoryAndClampsNegativeCounts() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.FAMILY_EMERGENCY)
        val ready = readiness(ReadinessLevel.READY)
        val zero = NewNumberQuarantineCoachBuilder.build(current, ready, 0)
        val negative = NewNumberQuarantineCoachBuilder.build(current, ready, -99)
        val one = NewNumberQuarantineCoachBuilder.build(current, ready, 1)
        val many = NewNumberQuarantineCoachBuilder.build(current, ready, Int.MAX_VALUE)

        assertEquals(zero, negative)
        assertEquals(one, many)
        assertNotEquals(zero, one)
        assertFalse(
            NewNumberQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH in
                zero.recommendedActionIds
        )
        assertFalse(
            NewNumberQuarantineActionId.FOLLOW_UP_CHECK_IN_PLANNER in
                zero.recommendedActionIds
        )
        assertTrue(
            NewNumberQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH in
                one.recommendedActionIds
        )
        assertTrue(NewNumberQuarantineActionId.EVIDENCE_KIT in one.recommendedActionIds)
        assertTrue(
            NewNumberQuarantineActionId.FOLLOW_UP_CHECK_IN_PLANNER in
                one.recommendedActionIds
        )
        assertTrue(one.recommendationExplanation.contains("개수 범주만 반영"))
        assertTrue(one.recommendationExplanation.contains("사건 내용이나 미리보기는 읽지"))
        assertFalse(one.familyShareSummary.contains(Int.MAX_VALUE.toString()))
    }

    @Test
    fun readinessLevelsRecommendPlanResetRoadmapDrillAndScriptsWhileReadyProvesNothing() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.LINK)
        val low = NewNumberQuarantineCoachBuilder.build(
            current,
            readiness(ReadinessLevel.LOW)
        )
        val inProgress = NewNumberQuarantineCoachBuilder.build(
            current,
            readiness(ReadinessLevel.IN_PROGRESS)
        )
        val ready = NewNumberQuarantineCoachBuilder.build(
            current,
            readiness(ReadinessLevel.READY)
        )

        listOf(low, inProgress).forEach { coach ->
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        NewNumberQuarantineActionId.SAFETY_PLAN,
                        NewNumberQuarantineActionId.FAMILY_SAFE_WORD_RESET_COACH,
                        NewNumberQuarantineActionId.FAMILY_READINESS_ROADMAP,
                        NewNumberQuarantineActionId.FAMILY_SAFETY_DRILL_COACH,
                        NewNumberQuarantineActionId.SAFE_CALL_SCRIPTS
                    )
                )
            )
            assertTrue(
                NewNumberQuarantineCardId.VERIFY_THROUGH_ESTABLISHED_FAMILY_ROUTE in
                    coach.recommendedCardIds
            )
        }
        assertTrue(low.recommendationExplanation.contains("가족 준비가 부족한 범주"))
        assertTrue(inProgress.recommendationExplanation.contains("가족 준비가 진행 중인 범주"))
        assertTrue(ready.recommendationExplanation.contains("기존 준비 항목이 채워졌다는 뜻일 뿐"))
        assertTrue(ready.recommendationExplanation.contains("새 연락이 가족이라는 점을 증명하지"))
    }

    @Test
    fun everyCardHasCompleteFieldsStableOrderUniqueRoutesAndEveryActionMapsToAFlow() {
        val noAnalysis = NewNumberQuarantineCoachBuilder.build(
            null,
            readiness(ReadinessLevel.READY)
        )
        val analyzed = NewNumberQuarantineCoachBuilder.build(
            analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness(ReadinessLevel.LOW),
            2
        )
        val expectedTitles = listOf(
            "새 번호·새 프로필을 바로 저장하거나 바꾸지 않기",
            "새 채팅·통화 안에서 돈·인증·앱 이야기를 이어가지 않기",
            "예전부터 알던 가족 경로로 직접 확인하기",
            "가족에게는 새 번호값 대신 행동 종류만 알리기",
            "확인 전 가족 계획·안전단어를 바꾸지 않기",
            "확인 뒤에도 기존 계획 화면에서만 직접 정리하기"
        )

        listOf(noAnalysis, analyzed).forEach { coach ->
            assertEquals(NewNumberQuarantineCardId.entries, coach.cards.map { it.id })
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
                assertTrue(card.quarantineScenario.isNotBlank())
                assertTrue(card.quarantineScenario.endsWith("때"))
                assertTrue(card.valueFreeActionSentence.isNotBlank())
                assertTrue(card.whyItMatters.isNotBlank())
                assertTrue(card.doNotSaveContactOrValueBoundaries.isNotEmpty())
                assertTrue(card.doNotSaveContactOrValueBoundaries.all { it.isNotBlank() })
                assertTrue(card.primaryAction.label.isNotBlank())
                assertTrue(card.secondaryActions.isNotEmpty())
                assertTrue(card.secondaryActions.all { it.label.isNotBlank() })
                assertEquals(
                    card.routeActions.map { it.id }.distinct(),
                    card.routeActions.map { it.id }
                )
            }
        }

        val usedActions = noAnalysis.cards.flatMap { card -> card.routeActions } +
            noAnalysis.recommendedActions + analyzed.recommendedActions
        assertEquals(
            NewNumberQuarantineActionId.entries.toSet(),
            usedActions.map { action -> action.id }.toSet()
        )
    }

    @Test
    fun familyShareContainsOnlyFixedCardsRoutesAndActionLabelsWithoutPrivateValues() {
        val rawInput =
            "RAW_NUMBER 박민수 010-9876-5432 123456789012 4111111111111111 " +
                "OTP 654321 PIN 7788 900101-1234567 https://evil.example " +
                "ShadowRemote com.evil.remote DEVICE-ID-ABC987654 caller.wav transcript.txt " +
                "content://private/new-profile screenshot.png OCR_RESULT RECIPIENT RESULT_STATUS"
        val readinessMarker =
            "SAFE_WORD CALLBACK CONTACT 070-1111-2222 account-987654321012 " +
                "https://private.example family-photo.png"
        val coach = NewNumberQuarantineCoachBuilder.build(
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
            add("[가족 AI 보이스피싱 방패] 새 번호 격리 코치")
            add("새 연락을 바로 저장하거나 바꾸지 않고 값 없는 수동 격리 순서를 함께 보고 있습니다.")
            add("추천 격리 카드")
            coach.recommendedCards.forEach { card -> add("- ${card.title}") }
            add("직접 고를 기존 앱 경로")
            coach.recommendedActions.forEach { action -> add("- ${action.label}") }
            add("가족에게 말할 값 없는 문장")
            add("- 격리·별도 확인·돈 멈춤·도움 요청 같은 행동 종류만 함께 살펴보세요.")
            add(
                "사람·연락처·번호·프로필·채널·안전단어·콜백 문장·원문·사건 내용·링크·" +
                    "계정·인증·기기·앱·파일·받는 사람·결과값 없이 고정된 카드와 기존 앱 " +
                    "경로 이름만 담았습니다."
            )
        }
        assertEquals(
            expectedLines,
            summary.lineSequence().filter { line -> line.isNotBlank() }.toList()
        )

        listOf(
            "RAW_NUMBER",
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
            "caller.wav",
            "transcript.txt",
            "content://private/new-profile",
            "screenshot.png",
            "OCR_RESULT",
            "RECIPIENT",
            "RESULT_STATUS",
            "SAFE_WORD",
            "CALLBACK",
            "CONTACT",
            "070-1111-2222",
            "987654321012",
            "private.example",
            "family-photo.png",
            Int.MAX_VALUE.toString()
        ).forEach { secret ->
            assertFalse("family share leaked value: $secret", summary.contains(secret))
        }
        assertFalse(Regex("01[016789][- .]?\\d{3,4}[- .]?\\d{4}").containsMatchIn(summary))
        assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(summary))
        assertFalse(
            Regex("(?i)https?://|content://|www\\.|[a-z0-9-]+\\.(?:com|net|kr)")
                .containsMatchIn(summary)
        )
        assertFalse(Regex("\\d{6}[- ]?[1-4]\\d{6}").containsMatchIn(summary))
    }

    @Test
    fun builderIsDeterministicAndIgnoresRawScoreAndReadinessLiteralFields() {
        val signals = listOf(
            ScamSignal.PHONE_NUMBER_CHANGE,
            ScamSignal.CREDENTIALS,
            ScamSignal.LINK
        )
        val firstAnalysis = ScamAnalysis(
            input = "FIRST_RAW 010-1111-2222 https://first.example package.one",
            score = 31,
            riskLevel = RiskLevel.HIGH,
            matchedSignals = signals
        )
        val secondAnalysis = firstAnalysis.copy(
            input = "SECOND_RAW 010-9999-8888 https://second.example package.two",
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

        val first = NewNumberQuarantineCoachBuilder.build(
            firstAnalysis,
            firstReadiness,
            1
        )
        val repeated = NewNumberQuarantineCoachBuilder.build(
            firstAnalysis,
            firstReadiness,
            1
        )
        val differentForbiddenValues = NewNumberQuarantineCoachBuilder.build(
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
            "package.one",
            "1000"
        ).forEach { ignoredValue ->
            assertFalse(allCopy(first).contains(ignoredValue))
            assertFalse(allCopy(differentForbiddenValues).contains(ignoredValue))
        }
    }

    @Test
    fun privacyCopyStatesManualLocalStatelessAndNoContactPhoneMediaOrAutomation() {
        val coach = NewNumberQuarantineCoachBuilder.build(
            analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness(ReadinessLevel.LOW),
            2
        )
        val copy = allCopy(coach)

        listOf(
            "로컬 전용·무상태 수동 안내",
            "이름·연락처·번호·프로필·채널·확인 결과값 입력을 요구하지 않습니다",
            "위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용",
            "가족 안전 계획의 실제 값을 사용하지 않습니다",
            "주소록·연락처를 읽기·쓰기·편집·삽입·삭제·선택",
            "병합·교체·저장·차단·음소거·동기화·갱신하지 않으며",
            "전화번호를 정규화·검사·판정하지 않고",
            "프로필·채널·공식 계정·신원·스팸·통신사·기관을 조회·검증·증명하지 않으며",
            "전화·다이얼·통화·통화 종료·통화 선별·차단·상태·기록·SMS",
            "통화 기록·문자·메시지·통화 상태·알림을 읽거나 감시·필터링하지 않고",
            "클립보드를 읽거나 쓰지 않습니다",
            "파일·문서·사진·이미지·오디오·미디어·저장소를 읽지 않고",
            "카메라·화면 캡처·스크린샷 가져오기·OCR·이미지 분석·음성 녹음·전사",
            "파일 선택기·첨부·스트림·URI 공유를 사용하지 않습니다",
            "네트워크·웹 탐색·브라우저·지도·위치·지오코딩",
            "공식 번호 조회·보고 제출·업로드·클라우드 백업·증거 동기화",
            "새 Android 권한을 요청하지 않고",
            "백그라운드 워커·스케줄러를 만들거나 예약하지 않습니다",
            "새 저장소를 만들지 않고",
            "결제·은행 기능이나 API, NFC, Android 계정 관리자",
            "기기 관리자·접근성 서비스·VPN",
            "설치 앱·패키지 목록 조회",
            "상태나 확인 상태를 추가·수정·삭제하지 않습니다",
            "파일·첨부·스트림·URI를 담거나 자동 발송하지 않습니다",
            "자동 모니터링·검사·스캔·OCR·삭제·가림·차단·통화·메시지 전송",
            "저장·업로드·첨부·제출을 수행하지 않습니다"
        ).forEach { expectedBoundary ->
            assertTrue("privacy copy omitted: $expectedBoundary", copy.contains(expectedBoundary))
        }
    }

    @Test
    fun copyAvoidsUnsafeContactIdentityMoneyLegalAndAutomaticOutcomeClaims() {
        val coaches = listOf(
            NewNumberQuarantineCoachBuilder.build(
                null,
                readiness(ReadinessLevel.READY)
            ),
            NewNumberQuarantineCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.LOW),
                3
            )
        )
        val forbiddenClaims = listOf(
            "100%",
            "verified new number",
            "safe and secure",
            "trusted contact",
            "official profile",
            "spam-free",
            "problem-free",
            "complete",
            "새 번호가 확인됐습니다",
            "가족 번호입니다",
            "공식 번호입니다",
            "신뢰할 수 있습니다",
            "안전합니다",
            "보안이 확인됐습니다",
            "스팸이 아닙니다",
            "연락처를 차단했습니다",
            "연락처를 저장했습니다",
            "연락처를 갱신했습니다",
            "연락처를 교체했습니다",
            "프로필을 확인했습니다",
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
            "자동으로 검사합니다",
            "자동으로 스캔합니다",
            "자동으로 OCR합니다",
            "자동으로 삭제합니다",
            "자동으로 가립니다",
            "자동으로 차단합니다",
            "자동으로 전화합니다",
            "메시지를 자동 전송합니다",
            "기관을 자동 조회합니다",
            "번호를 자동 검증합니다",
            "프로필을 자동 검증합니다",
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
        }
    }

    @Test
    fun modelSourceIsPureAndUsesOnlyAllowedAnalysisReadinessAndIncidentInputs() {
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/NewNumberQuarantineCoach.kt"
        )
        val normalized = modelSource.replace(Regex("\\s+"), " ")

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))
        assertTrue(
            normalized.contains(
                "fun build( analysis: ScamAnalysis?, readiness: FamilyReadiness, " +
                    "incidentCount: Int = 0 ): NewNumberQuarantineCoach"
            )
        )
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        val incidentOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(modelSource).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentOperations)
        assertEquals(2, Regex("\\bincidentCount\\b").findAll(modelSource).count())
        assertFalse(Regex("analysis\\s*\\.\\s*[A-Za-z]").containsMatchIn(modelSource))

        listOf(
            "analysis.input",
            "analysis.score",
            "analysis.actionPlan",
            "analysis.summary",
            "analysis.redactedClue",
            "readiness.title",
            "readiness.completedCount",
            "readiness.totalCount",
            "readiness.checklist",
            "readiness.nextAction",
            "SafetyPlanStore",
            "SafetyPlan(",
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "directory.entries",
            "ScamAnalyzer",
            "ContactsContract",
            "Intent.ACTION_INSERT",
            "Intent.ACTION_EDIT",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ACTION_SENDTO",
            "smsto:",
            "tel:",
            "PhoneNumberUtils",
            "Patterns.PHONE",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationListenerService",
            "ClipboardManager",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "RecognizerIntent",
            "TextToSpeech",
            "android.media",
            "android.speech",
            "ACTION_IMAGE_CAPTURE",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "AccountManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "AlarmManager",
            "WorkManager",
            "JobScheduler",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            ".save("
        ).forEach { forbiddenSource ->
            assertFalse(
                "new-number model used forbidden source: $forbiddenSource",
                modelSource.contains(forbiddenSource)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(
                "new-number model embedded official number: $exactOfficialNumber",
                Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource)
            )
        }
    }

    @Test
    fun highRiskRecommendsAllCardsAndBroadExistingRoutesWithoutStatusClaims() {
        val coach = NewNumberQuarantineCoachBuilder.build(
            analysis(RiskLevel.HIGH),
            readiness(ReadinessLevel.READY)
        )

        assertEquals(NewNumberQuarantineCardId.entries, coach.recommendedCardIds)
        assertEquals(NewNumberQuarantineActionId.entries, coach.recommendedActionIds)
        assertTrue(coach.recommendationExplanation.contains("번호·프로필·사람·기관·연락 상태"))
        assertTrue(coach.recommendationExplanation.contains("판정하는 뜻은 아닙니다"))
        assertFalse(allCopy(coach).contains("새 번호를 확인했습니다"))
        assertFalse(allCopy(coach).contains("연락처를 차단했습니다"))
    }

    @Test
    fun unrelatedCategoriesProduceDifferentRecommendationsAndScenariosRemainHypothetical() {
        fun coachFor(signal: ScamSignal) = NewNumberQuarantineCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, signal),
            readiness(ReadinessLevel.READY)
        )

        val family = coachFor(ScamSignal.FAMILY_EMERGENCY)
        val financial = coachFor(ScamSignal.URGENT_TRANSFER)
        val secrecy = coachFor(ScamSignal.SECRECY)
        val link = coachFor(ScamSignal.LINK)
        val remote = coachFor(ScamSignal.APP_INSTALL)

        assertNotEquals(family.recommendedCardIds, financial.recommendedCardIds)
        assertNotEquals(family.recommendedActionIds, financial.recommendedActionIds)
        assertNotEquals(secrecy.recommendedActionIds, financial.recommendedActionIds)
        assertNotEquals(link.recommendedActionIds, remote.recommendedActionIds)
        assertTrue(
            NewNumberQuarantineActionId.FAMILY_CALLBACK_CARD in
                family.recommendedActionIds
        )
        assertTrue(
            NewNumberQuarantineActionId.MONEY_TRANSFER_PAUSE_GATE in
                financial.recommendedActionIds
        )
        assertTrue(
            NewNumberQuarantineActionId.FAMILY_CALM_DEBRIEF_AGENDA in
                secrecy.recommendedActionIds
        )
        assertTrue(
            NewNumberQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH in
                link.recommendedActionIds
        )
        assertTrue(
            NewNumberQuarantineActionId.TRUSTED_HELPER_REVIEW in
                remote.recommendedActionIds
        )

        remote.cards.forEach { card ->
            assertTrue(
                "scenario is not hypothetical: ${card.id}",
                card.quarantineScenario.endsWith("때")
            )
            assertFalse(Regex("(?i)https?://|www\\.").containsMatchIn(card.quarantineScenario))
            assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(card.quarantineScenario))
        }
    }

    private fun NewNumberQuarantineCoach.card(
        id: NewNumberQuarantineCardId
    ): NewNumberQuarantineCard = cards.single { card -> card.id == id }

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

    private fun allCopy(coach: NewNumberQuarantineCoach): String = buildString {
        append(coach.title)
        append(coach.intro)
        append(coach.modeLabel)
        append(coach.recommendationTitle)
        append(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> append(action.label) }
        coach.cards.forEach { card ->
            append(card.title)
            append(card.quarantineScenario)
            append(card.valueFreeActionSentence)
            append(card.whyItMatters)
            card.doNotSaveContactOrValueBoundaries.forEach(::append)
            card.routeActions.forEach { action -> append(action.label) }
        }
        coach.privacySafetyNotes.forEach(::append)
        append(coach.familyShareSummary)
    }

    private fun readProjectFile(relativePath: String): String {
        val userDirectory = requireNotNull(System.getProperty("user.dir"))
        var directory = File(userDirectory).absoluteFile
        while (true) {
            val candidate = File(directory, relativePath)
            if (candidate.isFile) return candidate.readText()
            directory = directory.parentFile
                ?: error("Could not find project file from $userDirectory: $relativePath")
        }
    }
}
