package com.mongmong.familyshield

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CredentialSecretFreezeCoachTest {
    @Test
    fun noAnalysisCoachIsConservativeAndNeverDeclaresSecretOrRecoveryStatus() {
        val coach = CredentialSecretFreezeCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(CredentialSecretFreezeCardId.entries, coach.recommendedCardIds)
        assertEquals(
            CredentialSecretFreezeActionId.entries.filterNot { actionId ->
                actionId == CredentialSecretFreezeActionId.ANALYSIS_RESULT
            },
            coach.recommendedActionIds
        )
        assertTrue(coach.recommendationExplanation.contains("모든 멈춤 카드를 보수적으로"))
        assertTrue(coach.recommendationExplanation.contains("단정하지 말고"))

        val copy = allCopy(coach)
        listOf(
            "credential is safe",
            "secure credential",
            "verified code",
            "valid OTP",
            "invalid OTP",
            "correct PIN",
            "accepted password",
            "account is safe",
            "login is secure",
            "device is safe",
            "payment is safe",
            "recovery complete",
            "credential is blocked",
            "credential is revoked",
            "password reset complete",
            "secret rotated",
            "password changed",
            "device cleaned",
            "account recovered",
            "payment refunded",
            "card frozen",
            "account protected",
            "비밀값이 안전합니다",
            "보안이 확인된 비밀값입니다",
            "확인된 코드입니다",
            "유효한 인증번호입니다",
            "잘못된 인증번호입니다",
            "올바른 PIN입니다",
            "비밀번호가 승인됐습니다",
            "계정이 안전합니다",
            "로그인이 보호됐습니다",
            "기기가 안전합니다",
            "결제가 완료됐습니다",
            "복구가 완료됐습니다",
            "자격 증명이 차단됐습니다",
            "자격 증명이 취소됐습니다",
            "비밀번호가 재설정됐습니다",
            "비밀값이 교체됐습니다",
            "비밀번호가 변경됐습니다",
            "기기가 정리됐습니다",
            "계정이 복구됐습니다",
            "결제가 환급됐습니다",
            "카드가 동결됐습니다",
            "계정이 보호됐습니다",
            "문제가 해결됐습니다",
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
    fun credentialGifticonAndPaymentAlertSignalsRecommendBroadSecretFreeRoutes() {
        listOf(
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = CredentialSecretFreezeCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "OTP_VALUE_482913 PIN_VALUE_7788 PASSWORD_VALUE " +
                            "ACCOUNT_VALUE CARD_VALUE SECURITY_ANSWER_VALUE"
                ),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "PLAN_SECRET SAFE_WORD_SECRET CALLBACK_SECRET CONTACT_SECRET"
                )
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        CredentialSecretFreezeCardId.DO_NOT_SAY_TYPE_PASTE_OR_FORWARD,
                        CredentialSecretFreezeCardId.LEAVE_REQUESTING_SESSION,
                        CredentialSecretFreezeCardId.PAUSE_MONEY_ACCOUNT_AND_DEVICE_ACTIONS,
                        CredentialSecretFreezeCardId.SHARE_ACTION_CATEGORIES_ONLY,
                        CredentialSecretFreezeCardId.PREPARE_MANUAL_OFFICIAL_ACTION
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        CredentialSecretFreezeActionId.ACCOUNT_LOCKDOWN,
                        CredentialSecretFreezeActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        CredentialSecretFreezeActionId.EXPOSURE_IMPACT_TRIAGE,
                        CredentialSecretFreezeActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                        CredentialSecretFreezeActionId.TRUSTED_HELPER_REVIEW,
                        CredentialSecretFreezeActionId.FAMILY_ALERT_MESSAGES,
                        CredentialSecretFreezeActionId.SAFE_PASTE_PRIVACY_COACH
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("말하기·입력·붙여넣기"))
            assertTrue(coach.recommendationExplanation.contains("계정 잠금"))

            val copy = allCopy(coach)
            listOf(
                "OTP_VALUE_482913",
                "PIN_VALUE_7788",
                "PASSWORD_VALUE",
                "ACCOUNT_VALUE",
                "CARD_VALUE",
                "SECURITY_ANSWER_VALUE",
                "PLAN_SECRET",
                "SAFE_WORD_SECRET",
                "CALLBACK_SECRET",
                "CONTACT_SECRET"
            ).forEach { forbiddenValue -> assertFalse(copy.contains(forbiddenValue)) }
        }
    }

    @Test
    fun financialAndSafeAccountBaitRecommendPauseRoutesWithoutOutcomeClaims() {
        listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.BAIT,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE
        ).forEach { signal ->
            val coach = CredentialSecretFreezeCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, signal),
                readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        CredentialSecretFreezeActionId.MONEY_TRANSFER_PAUSE_GATE,
                        CredentialSecretFreezeActionId.ACCOUNT_LOCKDOWN,
                        CredentialSecretFreezeActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        CredentialSecretFreezeActionId.EXPOSURE_IMPACT_TRIAGE
                    )
                )
            )
            assertTrue(
                CredentialSecretFreezeCardId.PAUSE_MONEY_ACCOUNT_AND_DEVICE_ACTIONS in
                    coach.recommendedCardIds
            )
            val copy = allCopy(coach)
            listOf(
                "송금을 취소했습니다",
                "결제를 취소했습니다",
                "계정을 동결했습니다",
                "카드를 정지했습니다",
                "환급됐습니다",
                "피해금을 회수했습니다",
                "복구됐습니다",
                "자격 증명을 취소했습니다",
                "비밀번호를 변경했습니다",
                "비밀값 정리가 완료됐습니다"
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
            val coach = CredentialSecretFreezeCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, signal),
                readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        CredentialSecretFreezeCardId.DO_NOT_SAY_TYPE_PASTE_OR_FORWARD,
                        CredentialSecretFreezeCardId.LEAVE_REQUESTING_SESSION,
                        CredentialSecretFreezeCardId.SHARE_ACTION_CATEGORIES_ONLY
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        CredentialSecretFreezeActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                        CredentialSecretFreezeActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        CredentialSecretFreezeActionId.EXPOSURE_IMPACT_TRIAGE,
                        CredentialSecretFreezeActionId.PRIVACY_CENTER
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("같은 링크·앱·통화"))
            val copy = allCopy(coach)
            listOf(
                "URL 검증 완료",
                "링크 검사 완료",
                "설치 앱 목록을 확인했습니다",
                "악성 앱을 제거했습니다",
                "원격 연결을 종료했습니다",
                "기기를 정리했습니다"
            ).forEach { unsafeClaim -> assertFalse(copy.contains(unsafeClaim)) }
        }
    }

    @Test
    fun familyEmergencyDeepfakeAndNumberChangeUseValueFreeFamilyRoutes() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val coach = CredentialSecretFreezeCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, signal),
                readiness(
                    ReadinessLevel.READY,
                    "CONTACT_NAME 010-1111-2222 SAFE_WORD_VALUE CALLBACK_VALUE"
                )
            )

            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        CredentialSecretFreezeActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                        CredentialSecretFreezeActionId.FAMILY_CALLBACK_CARD,
                        CredentialSecretFreezeActionId
                            .VOICE_IMPERSONATION_REALITY_CHECK_COACH,
                        CredentialSecretFreezeActionId.NEW_NUMBER_QUARANTINE_COACH,
                        CredentialSecretFreezeActionId.FAMILY_SAFE_WORD_RESET_COACH,
                        CredentialSecretFreezeActionId.SAFE_CALL_SCRIPTS,
                        CredentialSecretFreezeActionId.FAMILY_ALERT_MESSAGES
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("비밀값을 묻거나 공유하지 않고"))
            val copy = allCopy(coach)
            listOf(
                "CONTACT_NAME",
                "010-1111-2222",
                "SAFE_WORD_VALUE",
                "CALLBACK_VALUE"
            ).forEach { forbiddenValue -> assertFalse(copy.contains(forbiddenValue)) }
        }
    }

    @Test
    fun secrecyAndImpersonationRecommendDisclosureAndManualHelpWithoutDirectoryValues() {
        listOf(ScamSignal.SECRECY, ScamSignal.IMPERSONATION).forEach { signal ->
            val coach = CredentialSecretFreezeCoachBuilder.build(
                analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input = "OFFICIAL_ENTRY_SECRET DIRECTORY_VALUE 112 1394 1332"
                ),
                readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        CredentialSecretFreezeActionId.FAMILY_ALERT_MESSAGES,
                        CredentialSecretFreezeActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                        CredentialSecretFreezeActionId.TRUSTED_HELPER_REVIEW,
                        CredentialSecretFreezeActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                        CredentialSecretFreezeActionId.PRIVACY_CENTER,
                        CredentialSecretFreezeActionId.OFFICIAL_HELP_DIRECTORY,
                        CredentialSecretFreezeActionId.REPORT_PREP
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("요청을 숨긴 채 이어가지 말고"))
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
    fun savedIncidentUsesOnlyClampedPresenceCategory() {
        val baseAnalysis = analysis(RiskLevel.MEDIUM, ScamSignal.CREDENTIALS)
        val noIncident = CredentialSecretFreezeCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 0
        )
        val negativeIncident = CredentialSecretFreezeCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = -99
        )
        val oneIncident = CredentialSecretFreezeCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 1
        )
        val manyIncidents = CredentialSecretFreezeCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = Int.MAX_VALUE
        )

        assertEquals(noIncident, negativeIncident)
        assertEquals(oneIncident, manyIncidents)
        assertNotEquals(noIncident.recommendedActionIds, oneIncident.recommendedActionIds)
        assertTrue(
            CredentialSecretFreezeCardId.REVIEW_WITHOUT_SECRET_INPUT in
                oneIncident.recommendedCardIds
        )
        assertTrue(
            oneIncident.recommendedActionIds.containsAll(
                listOf(
                    CredentialSecretFreezeActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                    CredentialSecretFreezeActionId.EVIDENCE_KIT,
                    CredentialSecretFreezeActionId.FOLLOW_UP_CHECK_IN_PLANNER
                )
            )
        )
        assertTrue(oneIncident.recommendationExplanation.contains("개수 범주만"))
        assertTrue(oneIncident.recommendationExplanation.contains("사건 내용이나 미리보기는 읽지"))
    }

    @Test
    fun readinessUsesOnlyLevelAndReadyNeverMakesSecretRequestSafe() {
        listOf(ReadinessLevel.LOW, ReadinessLevel.IN_PROGRESS).forEach { level ->
            val coach = CredentialSecretFreezeCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, ScamSignal.SECRECY),
                readiness(level, "READINESS_PRIVATE_LITERAL")
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        CredentialSecretFreezeActionId.SAFETY_PLAN,
                        CredentialSecretFreezeActionId.FAMILY_SAFE_WORD_RESET_COACH,
                        CredentialSecretFreezeActionId.FAMILY_READINESS_ROADMAP,
                        CredentialSecretFreezeActionId.FAMILY_SAFETY_DRILL_COACH,
                        CredentialSecretFreezeActionId.SAFE_CALL_SCRIPTS
                    )
                )
            )
            assertFalse(allCopy(coach).contains("READINESS_PRIVATE_LITERAL"))
        }

        val readyCoach = CredentialSecretFreezeCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.SECRECY),
            readiness(ReadinessLevel.READY, "READY_PRIVATE_LITERAL")
        )
        assertTrue(readyCoach.recommendationExplanation.contains("비밀값 요청도 안전하다는 뜻이 아닙니다"))
        assertFalse(allCopy(readyCoach).contains("READY_PRIVATE_LITERAL"))
    }

    @Test
    fun cardsAreCompleteStableAndRouteOnlyToDeclaredExistingActions() {
        val coach = CredentialSecretFreezeCoachBuilder.build(
            null,
            readiness(ReadinessLevel.READY)
        )

        assertEquals(CredentialSecretFreezeCardId.entries.size, coach.cards.size)
        assertEquals(
            CredentialSecretFreezeCardId.entries,
            coach.cards.map { card -> card.id }
        )
        assertEquals(
            (1..CredentialSecretFreezeCardId.entries.size).toList(),
            coach.cards.map { card -> card.displayOrder }
        )
        coach.cards.forEach { card ->
            assertEquals(card.id.displayOrder, card.displayOrder)
            assertTrue(card.title.isNotBlank())
            assertTrue(card.freezeScenario.isNotBlank())
            assertTrue(card.valueFreeActionSentence.isNotBlank())
            assertTrue(card.whyItMatters.isNotBlank())
            assertTrue(card.doNotEnterShareClipboardOrAutofillBoundaries.size >= 2)
            assertTrue(
                card.doNotEnterShareClipboardOrAutofillBoundaries.all { boundary ->
                    boundary.isNotBlank()
                }
            )
            assertTrue(card.primaryAction.label.isNotBlank())
            assertTrue(card.secondaryActions.isNotEmpty())
            assertEquals(
                card.routeActions.size,
                card.routeActions.map { action -> action.id }.distinct().size
            )
            assertTrue(
                card.routeActions.all { action ->
                    action.id in CredentialSecretFreezeActionId.entries &&
                        action.label.isNotBlank()
                }
            )
        }
        assertEquals(
            CredentialSecretFreezeActionId.entries.filterNot { actionId ->
                actionId == CredentialSecretFreezeActionId.ANALYSIS_RESULT
            },
            coach.recommendedActionIds
        )
        assertEquals(
            coach.recommendedActions.size,
            coach.recommendedActions.map { action -> action.id }.distinct().size
        )
        assertEquals(
            coach.recommendedActions.size,
            coach.recommendedActions.map { action -> action.label }.distinct().size
        )

        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val routerSource = activitySource
            .substringAfter("private fun openCredentialSecretFreezeAction")
            .substringBefore("private fun shareCredentialSecretFreezeCoach")
            .replace(Regex("\\s+"), " ")
        val expectedRoutes = linkedMapOf(
            CredentialSecretFreezeActionId.QUICK_STOP to "show(Screen.QUICK_STOP)",
            CredentialSecretFreezeActionId.CHECKER to "show(Screen.CHECKER)",
            CredentialSecretFreezeActionId.ANALYSIS_RESULT to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            CredentialSecretFreezeActionId.SAFETY_PLAN to "show(Screen.PLAN)",
            CredentialSecretFreezeActionId.FAMILY_READINESS_ROADMAP to
                "show(Screen.FAMILY_READINESS_ROADMAP)",
            CredentialSecretFreezeActionId.FAMILY_SAFETY_DRILL_COACH to "openDrill()",
            CredentialSecretFreezeActionId.SAFE_PASTE_PRIVACY_COACH to
                "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            CredentialSecretFreezeActionId.ACTIVE_CONTACT_EXIT_SCRIPTS to
                "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            CredentialSecretFreezeActionId.FAMILY_CALLBACK_CARD to
                "openFamilyCallbackCard(currentAnalysis)",
            CredentialSecretFreezeActionId.FAMILY_SAFE_WORD_RESET_COACH to
                "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            CredentialSecretFreezeActionId.SEPARATE_CHANNEL_VERIFICATION_COACH to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            CredentialSecretFreezeActionId.SAFE_CALL_SCRIPTS to
                "show(Screen.SAFE_CALL_SCRIPTS)",
            CredentialSecretFreezeActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            CredentialSecretFreezeActionId.NEW_NUMBER_QUARANTINE_COACH to
                "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            CredentialSecretFreezeActionId.FAMILY_ALERT_MESSAGES to
                "show(Screen.FAMILY_ALERT_MESSAGES)",
            CredentialSecretFreezeActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            CredentialSecretFreezeActionId.RECONTACT_BOUNDARY_COACH to
                "show(Screen.RECONTACT_BOUNDARY_COACH)",
            CredentialSecretFreezeActionId.TRUSTED_HELPER_REVIEW to
                "show(Screen.TRUSTED_HELPER_REVIEW)",
            CredentialSecretFreezeActionId.FAMILY_RESPONSE_ROLE_BOARD to
                "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            CredentialSecretFreezeActionId.FAMILY_CALM_DEBRIEF_AGENDA to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            CredentialSecretFreezeActionId.MONEY_TRANSFER_PAUSE_GATE to
                "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            CredentialSecretFreezeActionId.ACCOUNT_LOCKDOWN to
                "show(Screen.ACCOUNT_LOCKDOWN)",
            CredentialSecretFreezeActionId.REMOTE_ACCESS_CUTOFF_COACH to
                "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            CredentialSecretFreezeActionId.EXPOSURE_IMPACT_TRIAGE to
                "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            CredentialSecretFreezeActionId.PRIVACY_CENTER to
                "show(Screen.PRIVACY_CENTER)",
            CredentialSecretFreezeActionId.EVIDENCE_HANDOFF_PRIVACY_COACH to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            CredentialSecretFreezeActionId.EVIDENCE_KIT to "openEvidenceKit(null)",
            CredentialSecretFreezeActionId.OFFICIAL_HELP_DIRECTORY to
                "openHelpDirectory(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            CredentialSecretFreezeActionId.REPORT_PREP to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            CredentialSecretFreezeActionId.FOLLOW_UP_CHECK_IN_PLANNER to
                "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            CredentialSecretFreezeActionId.SAFETY_FLOW_ROUTER to
                "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(CredentialSecretFreezeActionId.entries, expectedRoutes.keys.toList())
        expectedRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "action did not map to an existing flow: $actionId",
                routerSource.contains(
                    "CredentialSecretFreezeActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            CredentialSecretFreezeActionId.entries.size,
            Regex("CredentialSecretFreezeActionId\\.[A-Z_]+\\s*->")
                .findAll(routerSource)
                .count()
        )
    }

    @Test
    fun familyShareSummaryIsRebuiltOnlyFromGenericCardsRoutesAndActions() {
        val coach = CredentialSecretFreezeCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.CREDENTIALS,
                ScamSignal.LINK,
                input =
                    "RAW_SECRET REDACTED_CLUE OTP_482913 PIN_7788 PASSWORD_Secret! " +
                        "ACCOUNT_123456789012 CARD_4111111111111111 SECURITY_ANSWER " +
                        "RESIDENT_900101-1234567 CONTACT_NAME 010-9876-5432 " +
                        "https://secret.example/private PACKAGE_NAME com.secret.bank " +
                        "DEVICE_ID FILE_NAME.png /secret/path content://secret AUDIO_NAME " +
                        "TRANSCRIPT_VALUE RECIPIENT_NAME VERIFIED_STATUS RECOVERY_STATUS"
            ),
            readiness = readiness(
                ReadinessLevel.READY,
                "SAFE_WORD_VALUE CALLBACK_VALUE CONTACT_VALUE PLAN_VALUE"
            ),
            incidentCount = 8
        )
        val summary = coach.familyShareSummary

        assertTrue(summary.contains("비밀값 멈춤 코치"))
        assertTrue(summary.contains("추천 멈춤 카드"))
        coach.recommendedCards.forEach { card -> assertTrue(summary.contains(card.title)) }
        coach.recommendedActions.forEach { action -> assertTrue(summary.contains(action.label)) }
        val expectedSummary = buildString {
            appendLine("[가족 AI 보이스피싱 방패] 비밀값 멈춤 코치")
            appendLine()
            appendLine("비밀값을 보거나 옮기지 않는 수동 멈춤 순서를 함께 보고 있습니다.")
            appendLine()
            appendLine("추천 멈춤 카드")
            coach.recommendedCards.forEach { card -> appendLine("- ${card.title}") }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            coach.recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("가족에게 말할 값 없는 문장")
            appendLine("- 멈춤·별도 확인·돈 멈춤·계정 점검·도움 요청 같은 행동 종류만 함께 살펴보세요.")
            append(
                "사람·연락처·채널·비밀값·계정값·원문·사건 내용·링크·기기·앱·파일·" +
                    "받는 사람·확인·결제·복구 결과 없이 고정 카드와 기존 앱 경로 이름만 담았습니다."
            )
        }.trim()
        assertEquals(expectedSummary, summary)

        listOf(
            "RAW_SECRET",
            "REDACTED_CLUE",
            "OTP_482913",
            "PIN_7788",
            "PASSWORD_Secret!",
            "ACCOUNT_123456789012",
            "CARD_4111111111111111",
            "SECURITY_ANSWER",
            "RESIDENT_900101-1234567",
            "CONTACT_NAME",
            "010-9876-5432",
            "secret.example",
            "PACKAGE_NAME",
            "com.secret.bank",
            "DEVICE_ID",
            "FILE_NAME.png",
            "/secret/path",
            "content://secret",
            "AUDIO_NAME",
            "TRANSCRIPT_VALUE",
            "RECIPIENT_NAME",
            "VERIFIED_STATUS",
            "RECOVERY_STATUS",
            "SAFE_WORD_VALUE",
            "CALLBACK_VALUE",
            "CONTACT_VALUE",
            "PLAN_VALUE"
        ).forEach { forbiddenValue ->
            assertFalse("share leaked $forbiddenValue", summary.contains(forbiddenValue))
        }
        assertFalse(Regex("https?://|www\\.|content://", RegexOption.IGNORE_CASE).containsMatchIn(summary))
        assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(summary))
        assertFalse(
            Regex("(?<!\\d)(?:\\+?82[- .]?)?0\\d{1,2}[- .]?\\d{3,4}[- .]?\\d{4}(?!\\d)")
                .containsMatchIn(summary)
        )
        assertFalse(
            Regex("(?i)\\b[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?(?:\\.[a-z0-9-]{2,})+\\b")
                .containsMatchIn(summary)
        )
        assertFalse(Regex("(?<!\\d)\\d{6}[- ]?[1-4]\\d{6}(?!\\d)").containsMatchIn(summary))
    }

    @Test
    fun builderIsDeterministicAndIgnoresRawAnalysisAndReadinessFields() {
        val first = CredentialSecretFreezeCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.CREDENTIALS,
                ScamSignal.SECRECY,
                input = "FIRST_RAW_SECRET",
                score = 1
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "FIRST_PLAN_LITERAL"),
            incidentCount = 3
        )
        val second = CredentialSecretFreezeCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.CREDENTIALS,
                ScamSignal.SECRECY,
                input = "SECOND_RAW_SECRET",
                score = 999
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "SECOND_PLAN_LITERAL"),
            incidentCount = 47
        )

        assertEquals(first, second)
        assertEquals(
            first,
            CredentialSecretFreezeCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.HIGH,
                    ScamSignal.CREDENTIALS,
                    ScamSignal.SECRECY,
                    input = "FIRST_RAW_SECRET",
                    score = 1
                ),
                readiness = readiness(ReadinessLevel.IN_PROGRESS, "FIRST_PLAN_LITERAL"),
                incidentCount = 3
            )
        )
        listOf(
            "FIRST_RAW_SECRET",
            "SECOND_RAW_SECRET",
            "FIRST_PLAN_LITERAL",
            "SECOND_PLAN_LITERAL"
        ).forEach { forbiddenValue ->
            assertFalse(allCopy(first).contains(forbiddenValue))
            assertFalse(allCopy(second).contains(forbiddenValue))
        }
    }

    @Test
    fun privacyCopyStatesManualLocalStatelessAndEveryNoSecretBoundary() {
        val coach = CredentialSecretFreezeCoachBuilder.build(
            null,
            readiness(ReadinessLevel.READY)
        )
        val copy = allCopy(coach)

        listOf(
            "로컬 전용",
            "무상태 수동 안내",
            "입력을 요구하지 않습니다",
            "읽기·표시·저장·복사·붙여넣기·자동 입력·제출·생성",
            "클립보드",
            "텍스트 감시기",
            "자동완성",
            "Credential Manager",
            "Account Manager",
            "Biometric Prompt",
            "Keyguard Manager",
            "Key Store",
            "비밀번호 관리자",
            "인증 앱",
            "계정 로그인·계정 복구·비밀번호 변경·코드 검증",
            "비밀번호 강도 검사",
            "비밀값 생성",
            "연락처·주소록·통화 기록·문자·SMS·통화 상태·알림",
            "파일·문서·사진·이미지·오디오·미디어·저장소",
            "카메라",
            "OCR",
            "첨부·스트림·URI 공유",
            "네트워크·웹 탐색·브라우저",
            "공식 번호 조회",
            "새 Android 권한",
            "백그라운드 워커·스케줄러",
            "결제·은행 기능이나 API·NFC",
            "기기 관리자·접근성 서비스·VPN",
            "설치 앱·패키지 목록 조회",
            "자동 발송하지 않습니다"
        ).forEach { boundary ->
            assertTrue("privacy copy omitted $boundary", copy.contains(boundary))
        }
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndAutomaticProtectionClaims() {
        val copy = allCopy(
            CredentialSecretFreezeCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.READY),
                incidentCount = 1
            )
        )

        listOf(
            "verified code",
            "valid OTP",
            "safe credential",
            "secure account",
            "accepted PIN",
            "blocked credential",
            "revoked credential",
            "reset password complete",
            "rotated secret",
            "changed password",
            "cleaned device",
            "recovered account",
            "refunded payment",
            "frozen card",
            "protected account",
            "resolved incident",
            "problem-free",
            "코드를 확인했습니다",
            "유효한 인증번호입니다",
            "비밀값이 안전합니다",
            "계정이 보호됐습니다",
            "PIN이 승인됐습니다",
            "자격 증명을 차단했습니다",
            "자격 증명을 취소했습니다",
            "비밀번호 재설정이 완료됐습니다",
            "비밀값을 교체했습니다",
            "비밀번호를 변경했습니다",
            "기기를 정리했습니다",
            "계정을 복구했습니다",
            "결제를 환급했습니다",
            "카드를 동결했습니다",
            "사건이 해결됐습니다",
            "문제없습니다",
            "자동 계정 동결",
            "자동 카드 정지",
            "피해 복구를 보장",
            "법률 결과를 보장",
            "금융 결과를 보장",
            "100%"
        ).forEach { unsafeClaim ->
            assertFalse(
                "coach made unsafe claim: $unsafeClaim",
                copy.contains(unsafeClaim, ignoreCase = true)
            )
        }
    }

    @Test
    fun modelSourceIsPureAndReadsOnlyApprovedCategoryInputs() {
        val source = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/CredentialSecretFreezeCoach.kt"
        )

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(source))
        assertFalse(Regex("\\b(?:Context|Intent|SharedPreferences)\\b").containsMatchIn(source))
        val analysisProperties = Regex(
            "analysis\\s*\\?\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(source).map { match -> match.groupValues[1] }.toSet()
        val readinessProperties = Regex(
            "readiness\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(source).map { match -> match.groupValues[1] }.toSet()
        val incidentCountOperations = Regex(
            "incidentCount\\s*\\.\\s*([A-Za-z][A-Za-z0-9_]*)"
        ).findAll(source).map { match -> match.groupValues[1] }.toSet()
        assertEquals(setOf("riskLevel", "matchedSignals"), analysisProperties)
        assertEquals(setOf("level"), readinessProperties)
        assertEquals(setOf("coerceAtLeast"), incidentCountOperations)
        assertEquals(2, Regex("\\bincidentCount\\b").findAll(source).count())
        assertFalse(Regex("analysis\\s*\\.\\s*[A-Za-z]").containsMatchIn(source))
        assertFalse(
            Regex("analysis\\s*\\?\\.\\s*(?:let|run|also|apply|takeIf)\\b")
                .containsMatchIn(source)
        )
        assertFalse(
            Regex("readiness\\s*\\.\\s*(?:let|run|also|apply|takeIf)\\b")
                .containsMatchIn(source)
        )
        assertFalse(Regex("\\([^)]*\\)\\s*=\\s*analysis\\b").containsMatchIn(source))
        assertFalse(Regex("\\([^)]*\\)\\s*=\\s*readiness\\b").containsMatchIn(source))

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
            "contact.label",
            "contact.number",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "OfficialHelpEntry",
            "directory.entries",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "AutofillManager",
            "AutofillValue",
            "android.view.autofill",
            "CredentialManager",
            "android.credentials",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "MessageDigest",
            "ContactsContract",
            "PhoneNumberUtils",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "CallLog",
            "SmsManager",
            "NotificationManager",
            "NotificationListenerService",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "MediaStore",
            "FileProvider",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "HttpURLConnection",
            "OkHttp",
            "WebView",
            "Geocoder",
            "BillingClient",
            "NfcAdapter",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenSource ->
            assertFalse(
                "credential freeze model used forbidden source: $forbiddenSource",
                source.contains(forbiddenSource)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(
                Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(source)
            )
        }
    }

    @Test
    fun highRiskShowsAllCardsAndRoutesWhileCategoriesRemainDistinct() {
        val highRisk = CredentialSecretFreezeCoachBuilder.build(
            analysis(RiskLevel.HIGH, ScamSignal.SECRECY),
            readiness(ReadinessLevel.READY)
        )
        assertEquals(CredentialSecretFreezeCardId.entries, highRisk.recommendedCardIds)
        assertEquals(CredentialSecretFreezeActionId.entries, highRisk.recommendedActionIds)

        val credential = CredentialSecretFreezeCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.CREDENTIALS),
            readiness(ReadinessLevel.READY)
        )
        val link = CredentialSecretFreezeCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.LINK),
            readiness(ReadinessLevel.READY)
        )
        val family = CredentialSecretFreezeCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.FAMILY_EMERGENCY),
            readiness(ReadinessLevel.READY)
        )
        assertNotEquals(credential.recommendedActionIds, link.recommendedActionIds)
        assertNotEquals(link.recommendedActionIds, family.recommendedActionIds)
        assertNotEquals(credential.recommendationTitle, family.recommendationTitle)
    }

    private fun analysis(
        riskLevel: RiskLevel,
        vararg signals: ScamSignal,
        input: String = "PRIVATE_RAW_INPUT",
        score: Int = 73
    ): ScamAnalysis = ScamAnalysis(
        input = input,
        score = score,
        riskLevel = riskLevel,
        matchedSignals = signals.toList()
    )

    private fun readiness(
        level: ReadinessLevel,
        privateLiteral: String = "PRIVATE_PLAN_LITERAL"
    ): FamilyReadiness = FamilyReadiness(
        level = level,
        title = "TITLE_$privateLiteral",
        completedCount = 77,
        totalCount = 99,
        checklist = listOf(
            ReadinessItem(
                label = "LABEL_$privateLiteral",
                done = true,
                action = "ACTION_$privateLiteral"
            )
        ),
        nextAction = "NEXT_$privateLiteral"
    )

    private fun CredentialSecretFreezeCoach.card(
        id: CredentialSecretFreezeCardId
    ): CredentialSecretFreezeCard = cards.single { card -> card.id == id }

    private fun allCopy(coach: CredentialSecretFreezeCoach): String = buildString {
        appendLine(coach.title)
        appendLine(coach.intro)
        appendLine(coach.modeLabel)
        appendLine(coach.recommendationTitle)
        appendLine(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> appendLine(action.label) }
        coach.cards.forEach { card ->
            appendLine(card.title)
            appendLine(card.freezeScenario)
            appendLine(card.valueFreeActionSentence)
            appendLine(card.whyItMatters)
            card.doNotEnterShareClipboardOrAutofillBoundaries.forEach(::appendLine)
            card.routeActions.forEach { action -> appendLine(action.label) }
        }
        coach.privacySafetyNotes.forEach(::appendLine)
        append(coach.familyShareSummary)
    }

    private fun readProjectFile(relativePath: String): String {
        val userDirectory = requireNotNull(System.getProperty("user.dir"))
        var directory = File(userDirectory).absoluteFile
        while (true) {
            val candidate = File(directory, relativePath)
            if (candidate.exists()) return candidate.readText()
            directory = directory.parentFile
                ?: error("Could not find project file from $userDirectory: $relativePath")
        }
    }
}
