package com.mongmong.familyshield

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceImpersonationRealityCheckCoachTest {
    @Test
    fun noAnalysisCoachIsConservativeAndNeverDeclaresVoiceOrCallerStatus() {
        val coach = VoiceImpersonationRealityCheckCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(VoiceImpersonationRealityCheckCardId.entries, coach.recommendedCardIds)
        assertTrue(
            coach.recommendedActionIds.containsAll(
                listOf(
                    VoiceImpersonationRealityCheckActionId.CHECKER,
                    VoiceImpersonationRealityCheckActionId.QUICK_STOP,
                    VoiceImpersonationRealityCheckActionId.FAMILY_CALLBACK_CARD,
                    VoiceImpersonationRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH,
                    VoiceImpersonationRealityCheckActionId
                        .SEPARATE_CHANNEL_VERIFICATION_COACH,
                    VoiceImpersonationRealityCheckActionId.SAFE_CALL_SCRIPTS,
                    VoiceImpersonationRealityCheckActionId.FAMILY_ALERT_MESSAGES,
                    VoiceImpersonationRealityCheckActionId
                        .VERIFICATION_OUTCOME_NEXT_STEPS_BOARD,
                    VoiceImpersonationRealityCheckActionId.PRIVACY_CENTER,
                    VoiceImpersonationRealityCheckActionId.SAFETY_FLOW_ROUTER
                )
            )
        )
        assertFalse(
            VoiceImpersonationRealityCheckActionId.ANALYSIS_RESULT in
                coach.recommendedActionIds
        )
        assertTrue(coach.recommendationExplanation.contains("모든 카드를 보수적으로 직접 읽으세요"))
        assertTrue(coach.recommendationExplanation.contains("사람 여부나 합성 여부를 단정하지 말고"))

        val copy = allCopy(coach)
        listOf(
            "real voice",
            "fake voice",
            "voice cloned",
            "voice verified",
            "speaker matched",
            "voice recognized",
            "voice identified",
            "voice analyzed",
            "voice classified",
            "safe voice",
            "secure voice",
            "official voice",
            "voice resolved",
            "voice blocked",
            "voice recorded",
            "voice check complete",
            "problem-free",
            "진짜 목소리입니다",
            "가짜 목소리입니다",
            "복제된 목소리입니다",
            "목소리가 확인됐습니다",
            "화자가 일치했습니다",
            "목소리를 인식했습니다",
            "사람을 식별했습니다",
            "음성을 분석했습니다",
            "음성을 분류했습니다",
            "안전한 통화입니다",
            "공식 통화입니다",
            "문제가 해결됐습니다",
            "통화를 차단했습니다",
            "녹음했습니다",
            "점검 완료",
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
    fun familyDeepfakeAndNumberChangeRecommendVoiceNotProofAndFamilyVerificationRoutes() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val coach = VoiceImpersonationRealityCheckCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "SAFE_WORD_SECRET CALLBACK_SECRET CONTACT_SECRET 010-1111-2222"
                )
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        VoiceImpersonationRealityCheckCardId.VOICE_IS_NOT_PROOF,
                        VoiceImpersonationRealityCheckCardId.STOP_SAME_CALL_EXCHANGE,
                        VoiceImpersonationRealityCheckCardId.VERIFY_THROUGH_FAMILY_ROUTE,
                        VoiceImpersonationRealityCheckCardId.SHARE_ACTION_CATEGORIES_ONLY
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        VoiceImpersonationRealityCheckActionId.FAMILY_CALLBACK_CARD,
                        VoiceImpersonationRealityCheckActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH,
                        VoiceImpersonationRealityCheckActionId.SAFE_CALL_SCRIPTS,
                        VoiceImpersonationRealityCheckActionId.FAMILY_ALERT_MESSAGES,
                        VoiceImpersonationRealityCheckActionId
                            .FAMILY_SAFE_WORD_RESET_COACH,
                        VoiceImpersonationRealityCheckActionId
                            .VERIFICATION_OUTCOME_NEXT_STEPS_BOARD
                    )
                )
            )
            assertTrue(
                coach.card(VoiceImpersonationRealityCheckCardId.VOICE_IS_NOT_PROOF)
                    .valueFreeActionSentence.contains("목소리만으로 상대를 정하지 않고")
            )
            assertTrue(coach.recommendationExplanation.contains("목소리를 증거로 삼지 않고"))
            val copy = allCopy(coach)
            listOf(
                "SAFE_WORD_SECRET",
                "CALLBACK_SECRET",
                "CONTACT_SECRET",
                "010-1111-2222"
            ).forEach { forbiddenValue -> assertFalse(copy.contains(forbiddenValue)) }
        }
    }

    @Test
    fun impersonationAndSecrecyRecommendFamilyHelperEvidencePrivacyAndManualHelpRoutes() {
        listOf(ScamSignal.IMPERSONATION, ScamSignal.SECRECY).forEach { signal ->
            val coach = VoiceImpersonationRealityCheckCoachBuilder.build(
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
                        VoiceImpersonationRealityCheckCardId.STOP_SAME_CALL_EXCHANGE,
                        VoiceImpersonationRealityCheckCardId.SHARE_ACTION_CATEGORIES_ONLY
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        VoiceImpersonationRealityCheckActionId.FAMILY_ALERT_MESSAGES,
                        VoiceImpersonationRealityCheckActionId
                            .FAMILY_CALM_DEBRIEF_AGENDA,
                        VoiceImpersonationRealityCheckActionId.TRUSTED_HELPER_REVIEW,
                        VoiceImpersonationRealityCheckActionId
                            .EVIDENCE_HANDOFF_PRIVACY_COACH,
                        VoiceImpersonationRealityCheckActionId.PRIVACY_CENTER,
                        VoiceImpersonationRealityCheckActionId.OFFICIAL_HELP_DIRECTORY,
                        VoiceImpersonationRealityCheckActionId.REPORT_PREP
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("같은 통화 안에서 숨기지 말고"))

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
    fun financialPaymentCredentialAndOtpSignalsRecommendPauseAccountAndRemoteOnlyAsRoutes() {
        val cases = listOf(
            analysis(RiskLevel.MEDIUM, ScamSignal.URGENT_TRANSFER),
            analysis(RiskLevel.MEDIUM, ScamSignal.BAIT),
            analysis(RiskLevel.MEDIUM, ScamSignal.GIFT_CRYPTO),
            analysis(RiskLevel.MEDIUM, ScamSignal.SAFE_ACCOUNT_FRAUD),
            analysis(RiskLevel.MEDIUM, ScamSignal.LOAN_REFINANCE_FEE),
            analysis(RiskLevel.MEDIUM, ScamSignal.GIFTICON_PIN),
            analysis(RiskLevel.MEDIUM, ScamSignal.OVERSEAS_PAYMENT_ALERT),
            analysis(RiskLevel.MEDIUM, ScamSignal.CREDENTIALS)
        )

        cases.forEach { current ->
            val coach = VoiceImpersonationRealityCheckCoachBuilder.build(
                current,
                readiness(ReadinessLevel.READY)
            )

            assertTrue(
                VoiceImpersonationRealityCheckCardId.PAUSE_MONEY_CREDENTIAL_OR_APP in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        VoiceImpersonationRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE,
                        VoiceImpersonationRealityCheckActionId.ACCOUNT_LOCKDOWN,
                        VoiceImpersonationRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        VoiceImpersonationRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE
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
            val coach = VoiceImpersonationRealityCheckCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, signal),
                readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        VoiceImpersonationRealityCheckCardId.STOP_SAME_CALL_EXCHANGE,
                        VoiceImpersonationRealityCheckCardId.KEEP_VOICE_ARTIFACTS_OUT,
                        VoiceImpersonationRealityCheckCardId.PAUSE_MONEY_CREDENTIAL_OR_APP
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        VoiceImpersonationRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        VoiceImpersonationRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE,
                        VoiceImpersonationRealityCheckActionId.PRIVACY_CENTER
                    )
                )
            )
            assertTrue(
                coach.recommendationExplanation.contains("같은") &&
                    coach.recommendationExplanation.contains("통화")
            )
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
        val zero = VoiceImpersonationRealityCheckCoachBuilder.build(current, ready, 0)
        val negative = VoiceImpersonationRealityCheckCoachBuilder.build(current, ready, -99)
        val one = VoiceImpersonationRealityCheckCoachBuilder.build(current, ready, 1)
        val many = VoiceImpersonationRealityCheckCoachBuilder.build(
            current,
            ready,
            Int.MAX_VALUE
        )

        assertEquals(zero, negative)
        assertEquals(one, many)
        assertNotEquals(zero, one)
        assertFalse(
            VoiceImpersonationRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH in
                zero.recommendedActionIds
        )
        assertFalse(
            VoiceImpersonationRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER in
                zero.recommendedActionIds
        )
        assertTrue(
            VoiceImpersonationRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH in
                one.recommendedActionIds
        )
        assertTrue(
            VoiceImpersonationRealityCheckActionId.EVIDENCE_KIT in
                one.recommendedActionIds
        )
        assertTrue(
            VoiceImpersonationRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER in
                one.recommendedActionIds
        )
        assertTrue(one.recommendationExplanation.contains("개수 범주만 반영"))
        assertTrue(one.recommendationExplanation.contains("사건 내용이나 미리보기는 읽지 않습니다"))
        assertFalse(one.familyShareSummary.contains(Int.MAX_VALUE.toString()))
    }

    @Test
    fun readinessLevelsRecommendPlanResetRoadmapDrillAndScriptsWhileReadyProvesNothing() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.LINK)
        val low = VoiceImpersonationRealityCheckCoachBuilder.build(
            current,
            readiness(ReadinessLevel.LOW)
        )
        val inProgress = VoiceImpersonationRealityCheckCoachBuilder.build(
            current,
            readiness(ReadinessLevel.IN_PROGRESS)
        )
        val ready = VoiceImpersonationRealityCheckCoachBuilder.build(
            current,
            readiness(ReadinessLevel.READY)
        )

        listOf(low, inProgress).forEach { coach ->
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        VoiceImpersonationRealityCheckActionId.SAFETY_PLAN,
                        VoiceImpersonationRealityCheckActionId
                            .FAMILY_SAFE_WORD_RESET_COACH,
                        VoiceImpersonationRealityCheckActionId.FAMILY_READINESS_ROADMAP,
                        VoiceImpersonationRealityCheckActionId.FAMILY_SAFETY_DRILL_COACH,
                        VoiceImpersonationRealityCheckActionId.SAFE_CALL_SCRIPTS
                    )
                )
            )
            assertTrue(
                VoiceImpersonationRealityCheckCardId.VERIFY_THROUGH_FAMILY_ROUTE in
                    coach.recommendedCardIds
            )
        }
        assertTrue(low.recommendationExplanation.contains("가족 준비가 부족한 범주"))
        assertTrue(inProgress.recommendationExplanation.contains("가족 준비가 진행 중인 범주"))
        assertTrue(ready.recommendationExplanation.contains("기존 준비 항목이 채워졌다는 뜻일 뿐"))
        assertTrue(ready.recommendationExplanation.contains("통화 상대의 신원을 증명하지 않습니다"))
        assertTrue(ready.recommendationExplanation.contains("익숙한 목소리도 단독 근거로 삼지 말고"))
    }

    @Test
    fun everyCardHasCompleteFieldsStableOrderUniqueRoutesAndEveryActionMapsToAFlow() {
        val noAnalysis = VoiceImpersonationRealityCheckCoachBuilder.build(
            null,
            readiness(ReadinessLevel.READY)
        )
        val analyzed = VoiceImpersonationRealityCheckCoachBuilder.build(
            analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness(ReadinessLevel.LOW),
            2
        )
        val expectedTitles = listOf(
            "익숙한 목소리도 증거로 단정하지 않기",
            "통화 안에서 새 질문·답·돈 이야기를 이어가지 않기",
            "목소리 대신 평소 가족 경로로 다시 확인하기",
            "목소리 샘플·녹음·스크린샷을 앱에 넣지 않기",
            "돈·인증·앱 설치 요구가 붙으면 먼저 멈추기",
            "가족에게는 목소리값 대신 행동 종류만 알리기"
        )

        listOf(noAnalysis, analyzed).forEach { coach ->
            assertEquals(VoiceImpersonationRealityCheckCardId.entries, coach.cards.map { it.id })
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
                assertTrue(card.realityCheckScenario.isNotBlank())
                assertTrue(card.realityCheckScenario.endsWith("때"))
                assertTrue(card.valueFreeActionSentence.isNotBlank())
                assertTrue(card.whyItMatters.isNotBlank())
                assertTrue(card.doNotRecordShareOrAskBoundaries.isNotEmpty())
                assertTrue(card.doNotRecordShareOrAskBoundaries.all { it.isNotBlank() })
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
            VoiceImpersonationRealityCheckActionId.entries.toSet(),
            usedActions.map { action -> action.id }.toSet()
        )
    }

    @Test
    fun familyShareContainsOnlyFixedCardsRoutesAndActionLabelsWithoutPrivateValues() {
        val rawInput =
            "RAW_VOICE 박민수 010-9876-5432 123456789012 4111111111111111 " +
                "OTP 654321 PIN 7788 900101-1234567 https://evil.example " +
                "ShadowRemote com.evil.remote DEVICE-ID-ABC987654 caller.wav transcript.txt " +
                "content://private/voice screenshot.png OCR_RESULT RECIPIENT RESULT_STATUS"
        val readinessMarker =
            "SAFE_WORD CALLBACK CONTACT 070-1111-2222 account-987654321012 " +
                "https://private.example family-audio.m4a"
        val coach = VoiceImpersonationRealityCheckCoachBuilder.build(
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
            add("[가족 AI 보이스피싱 방패] 목소리 사칭 현실 점검 코치")
            add("목소리를 증거로 단정하지 않고 값 없는 수동 점검 순서를 함께 보고 있습니다.")
            add("추천 현실 점검 카드")
            coach.recommendedCards.forEach { card -> add("- ${card.title}") }
            add("직접 고를 기존 앱 경로")
            coach.recommendedActions.forEach { action -> add("- ${action.label}") }
            add("가족에게 말할 값 없는 문장")
            add("- 목소리 대신 멈춤·별도 확인·가족 공유 같은 행동 종류만 함께 살펴보세요.")
            add(
                "사람·연락처·번호·안전단어·콜백 문장·원문·사건 내용·링크·계정·인증·" +
                    "기기·앱·파일·오디오·받는 사람·결과값 없이 고정된 카드와 기존 앱 경로 " +
                    "이름만 담았습니다."
            )
        }
        assertEquals(
            expectedLines,
            summary.lineSequence().filter { line -> line.isNotBlank() }.toList()
        )

        listOf(
            "RAW_VOICE",
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
            "content://private/voice",
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
            "family-audio.m4a",
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
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.CREDENTIALS,
            ScamSignal.LINK
        )
        val firstAnalysis = ScamAnalysis(
            input = "FIRST_RAW 010-1111-2222 https://first.example audio-one.wav",
            score = 31,
            riskLevel = RiskLevel.HIGH,
            matchedSignals = signals
        )
        val secondAnalysis = firstAnalysis.copy(
            input = "SECOND_RAW 010-9999-8888 https://second.example audio-two.m4a",
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

        val first = VoiceImpersonationRealityCheckCoachBuilder.build(
            firstAnalysis,
            firstReadiness,
            1
        )
        val repeated = VoiceImpersonationRealityCheckCoachBuilder.build(
            firstAnalysis,
            firstReadiness,
            1
        )
        val differentForbiddenValues = VoiceImpersonationRealityCheckCoachBuilder.build(
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
            "audio-one.wav",
            "1000"
        ).forEach { ignoredValue ->
            assertFalse(allCopy(first).contains(ignoredValue))
            assertFalse(allCopy(differentForbiddenValues).contains(ignoredValue))
        }
    }

    @Test
    fun privacyCopyStatesManualLocalStatelessAndNoAudioCallMediaDataOrAutomation() {
        val coach = VoiceImpersonationRealityCheckCoachBuilder.build(
            analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness(ReadinessLevel.LOW),
            2
        )
        val copy = allCopy(coach)

        listOf(
            "로컬 전용·무상태 수동 안내",
            "이름·목소리·통화 내용·결과값 입력을 요구하지 않습니다",
            "위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용",
            "가족 안전 계획의 실제 값을 사용하지 않습니다",
            "마이크·음성 녹음·통화 녹음·소리 재생",
            "통화 청취·통화 상태 감시·음성 인식·전사·자막 생성",
            "화자 검증·목소리 대조·딥페이크 탐지·분류·사람 식별",
            "음성 샘플·녹음 파일·전사문·소리 특징을 묻거나 저장·공유하지 않습니다",
            "새 저장소를 만들거나 기록하지 않습니다",
            "연락처·통화 기록·문자·메시지·통화 상태·알림·클립보드",
            "전화·다이얼·통화 종료·통화 차단·SMS·받는 사람 선택·자동 전송",
            "파일·사진·이미지·오디오·미디어·저장소를 읽지 않고",
            "카메라·화면 캡처·스크린샷 가져오기·OCR·이미지 분석",
            "파일·문서·사진·오디오 선택기·첨부·스트림·URI 공유",
            "네트워크·웹 탐색·지도·위치·기관 정보 조회",
            "전화번호 판정·링크·URL·도메인 판정·링크 스캔",
            "업로드·클라우드 백업·증거 동기화",
            "새 Android 권한을 요청하지 않고",
            "백그라운드 워커·스케줄러를 만들거나 예약하지 않습니다",
            "결제·은행 기능이나 API, NFC, Android 계정 관리자",
            "기기 관리자·접근성 서비스·VPN",
            "설치 앱·패키지 목록 조회",
            "추가·수정·삭제하지 않습니다",
            "자동 모니터링·녹음·청취·인식·전사·대조·탐지·분류·스캔·OCR",
            "저장·업로드·첨부·제출을 수행하지 않습니다"
        ).forEach { expectedBoundary ->
            assertTrue("privacy copy omitted: $expectedBoundary", copy.contains(expectedBoundary))
        }
    }

    @Test
    fun copyAvoidsUnsafeVoiceMoneyAccountLegalAndAutomaticOutcomeClaims() {
        val coaches = listOf(
            VoiceImpersonationRealityCheckCoachBuilder.build(
                null,
                readiness(ReadinessLevel.READY)
            ),
            VoiceImpersonationRealityCheckCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.LOW),
                3
            )
        )
        val forbiddenClaims = listOf(
            "100%",
            "real voice",
            "fake voice",
            "deepfake detected",
            "speaker matched",
            "voice verified",
            "voice recognized",
            "voice classified",
            "voice analyzed",
            "safe and secure",
            "complete",
            "진짜 목소리입니다",
            "가짜 목소리입니다",
            "딥페이크를 탐지했습니다",
            "화자가 일치했습니다",
            "목소리가 확인됐습니다",
            "목소리를 인식했습니다",
            "음성을 분류했습니다",
            "음성을 분석했습니다",
            "안전합니다",
            "보안이 확인됐습니다",
            "공식 확인됐습니다",
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
            "자동으로 녹음합니다",
            "자동으로 분석합니다",
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
        }
    }

    @Test
    fun modelSourceIsPureAndUsesOnlyAllowedAnalysisReadinessAndIncidentCategoryInputs() {
        val modelSource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/" +
                "VoiceImpersonationRealityCheckCoach.kt"
        )
        val normalized = modelSource.replace(Regex("\\s+"), " ")

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(modelSource))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(modelSource))
        assertTrue(
            normalized.contains(
                "fun build( analysis: ScamAnalysis?, readiness: FamilyReadiness, " +
                    "incidentCount: Int = 0 ): VoiceImpersonationRealityCheckCoach"
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
            "dialNumber",
            "ScamAnalyzer",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "RecognizerIntent",
            "TextToSpeech",
            "AudioManager",
            "MediaPlayer",
            "SoundPool",
            "android.media",
            "android.speech",
            "RECORD_AUDIO",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "ContactsContract",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "ClipboardManager",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "FileProvider",
            "MediaStore",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_VIEW",
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
                "voice reality-check model used forbidden source: $forbiddenSource",
                modelSource.contains(forbiddenSource)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(
                "voice reality-check model embedded official number: $exactOfficialNumber",
                Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(modelSource)
            )
        }
    }

    @Test
    fun highRiskRecommendsAllCardsAndBroadExistingRoutesWithoutStatusClaims() {
        val coach = VoiceImpersonationRealityCheckCoachBuilder.build(
            analysis(RiskLevel.HIGH),
            readiness(ReadinessLevel.READY)
        )

        assertEquals(VoiceImpersonationRealityCheckCardId.entries, coach.recommendedCardIds)
        assertEquals(
            VoiceImpersonationRealityCheckActionId.entries,
            coach.recommendedActionIds
        )
        assertTrue(coach.recommendationExplanation.contains("통화 상대나 소리의 상태"))
        assertTrue(coach.recommendationExplanation.contains("판정하는 뜻은 아닙니다"))
        assertFalse(allCopy(coach).contains("고위험 목소리를 탐지했습니다"))
        assertFalse(allCopy(coach).contains("통화를 차단했습니다"))
    }

    @Test
    fun unrelatedCategoriesProduceDifferentRecommendationsAndScenariosRemainHypothetical() {
        fun coachFor(signal: ScamSignal) = VoiceImpersonationRealityCheckCoachBuilder.build(
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
            VoiceImpersonationRealityCheckActionId.FAMILY_CALLBACK_CARD in
                family.recommendedActionIds
        )
        assertTrue(
            VoiceImpersonationRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE in
                financial.recommendedActionIds
        )
        assertTrue(
            VoiceImpersonationRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA in
                secrecy.recommendedActionIds
        )
        assertTrue(
            VoiceImpersonationRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH in
                link.recommendedActionIds
        )
        assertTrue(
            VoiceImpersonationRealityCheckActionId.TRUSTED_HELPER_REVIEW in
                remote.recommendedActionIds
        )

        remote.cards.forEach { card ->
            assertTrue(
                "scenario is not hypothetical: ${card.id}",
                card.realityCheckScenario.endsWith("때")
            )
            assertFalse(Regex("(?i)https?://|www\\.").containsMatchIn(card.realityCheckScenario))
            assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(card.realityCheckScenario))
        }
    }

    private fun VoiceImpersonationRealityCheckCoach.card(
        id: VoiceImpersonationRealityCheckCardId
    ): VoiceImpersonationRealityCheckCard = cards.single { card -> card.id == id }

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

    private fun allCopy(coach: VoiceImpersonationRealityCheckCoach): String = buildString {
        append(coach.title)
        append(coach.intro)
        append(coach.modeLabel)
        append(coach.recommendationTitle)
        append(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> append(action.label) }
        coach.cards.forEach { card ->
            append(card.title)
            append(card.realityCheckScenario)
            append(card.valueFreeActionSentence)
            append(card.whyItMatters)
            card.doNotRecordShareOrAskBoundaries.forEach(::append)
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
