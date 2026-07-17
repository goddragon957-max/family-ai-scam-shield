package com.mongmong.familyshield

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryPromiseRealityCheckCoachTest {
    @Test
    fun noAnalysisCoachIsConservativeAndNeverDeclaresRecoveryOrOutcomeStatus() {
        val coach = RecoveryPromiseRealityCheckCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(RecoveryPromiseRealityCheckCardId.entries, coach.recommendedCardIds)
        assertEquals(
            RecoveryPromiseRealityCheckActionId.entries.filterNot { actionId ->
                actionId == RecoveryPromiseRealityCheckActionId.ANALYSIS_RESULT
            },
            coach.recommendedActionIds
        )
        assertTrue(coach.recommendationExplanation.contains("모든 회복 약속 카드를 보수적으로"))
        assertTrue(coach.recommendationExplanation.contains("상태를 단정하지 말고"))

        val copy = allCopy(coach)
        listOf(
            "verified recovery",
            "refund guaranteed",
            "chargeback accepted",
            "case accepted",
            "wallet traced",
            "payment cancelled",
            "account frozen",
            "account protected",
            "problem-free",
            "공식 기관입니다",
            "회복이 확인됐습니다",
            "환급이 보장됩니다",
            "차지백이 접수됐습니다",
            "사건이 접수됐습니다",
            "지갑 추적이 완료됐습니다",
            "결제가 취소됐습니다",
            "계정이 동결됐습니다",
            "카드가 보호됐습니다",
            "신고가 제출됐습니다",
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
    fun financialRecoveryBaitSignalsRecommendPauseFreezePressureAndHelpRoutes() {
        listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.BAIT,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = RecoveryPromiseRealityCheckCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, signal),
                readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        RecoveryPromiseRealityCheckCardId.PROMISE_IS_NOT_A_RESULT,
                        RecoveryPromiseRealityCheckCardId.NO_UPFRONT_FEE_OR_PAYMENT,
                        RecoveryPromiseRealityCheckCardId.NO_SECRET_OR_FINANCIAL_VALUES,
                        RecoveryPromiseRealityCheckCardId
                            .SEPARATE_TITLE_AND_OFFICIAL_PRESSURE,
                        RecoveryPromiseRealityCheckCardId
                            .VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RecoveryPromiseRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE,
                        RecoveryPromiseRealityCheckActionId
                            .CREDENTIAL_SECRET_FREEZE_COACH,
                        RecoveryPromiseRealityCheckActionId
                            .OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
                        RecoveryPromiseRealityCheckActionId.ACCOUNT_LOCKDOWN,
                        RecoveryPromiseRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE,
                        RecoveryPromiseRealityCheckActionId
                            .EVIDENCE_HANDOFF_PRIVACY_COACH,
                        RecoveryPromiseRealityCheckActionId.TRUSTED_HELPER_REVIEW,
                        RecoveryPromiseRealityCheckActionId.OFFICIAL_HELP_DIRECTORY,
                        RecoveryPromiseRealityCheckActionId.REPORT_PREP
                    )
                )
            )
            val copy = allCopy(coach)
            listOf(
                "송금을 취소했습니다",
                "결제를 취소했습니다",
                "계정을 동결했습니다",
                "카드를 정지했습니다",
                "환급됐습니다",
                "차지백이 접수됐습니다",
                "지갑을 추적했습니다",
                "피해금을 회수했습니다",
                "회복됐습니다"
            ).forEach { unsafeOutcome -> assertFalse(copy.contains(unsafeOutcome)) }
        }
    }

    @Test
    fun credentialSignalsRecommendSecretPrivacyAccountRemoteAndExposureWithoutValues() {
        listOf(
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = RecoveryPromiseRealityCheckCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "OTP_VALUE PIN_VALUE PASSWORD_VALUE SECURITY_ANSWER_VALUE " +
                            "ACCOUNT_VALUE CARD_VALUE BANK_VALUE WALLET_VALUE"
                ),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "SAFE_WORD_VALUE CALLBACK_VALUE CONTACT_VALUE"
                )
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        RecoveryPromiseRealityCheckCardId.NO_SECRET_OR_FINANCIAL_VALUES,
                        RecoveryPromiseRealityCheckCardId.NO_APP_REMOTE_OR_SCREEN_SHARE
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RecoveryPromiseRealityCheckActionId
                            .CREDENTIAL_SECRET_FREEZE_COACH,
                        RecoveryPromiseRealityCheckActionId.SAFE_PASTE_PRIVACY_COACH,
                        RecoveryPromiseRealityCheckActionId.ACCOUNT_LOCKDOWN,
                        RecoveryPromiseRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        RecoveryPromiseRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE
                    )
                )
            )
            listOf(
                "OTP_VALUE",
                "PIN_VALUE",
                "PASSWORD_VALUE",
                "SECURITY_ANSWER_VALUE",
                "ACCOUNT_VALUE",
                "CARD_VALUE",
                "BANK_VALUE",
                "WALLET_VALUE",
                "SAFE_WORD_VALUE",
                "CALLBACK_VALUE",
                "CONTACT_VALUE"
            ).forEach { privateValue -> assertFalse(allCopy(coach).contains(privateValue)) }
        }
    }

    @Test
    fun impersonationAndSecrecyRecommendPressureExitFamilyEvidenceAndHelpRoutes() {
        listOf(ScamSignal.IMPERSONATION, ScamSignal.SECRECY).forEach { signal ->
            val coach = RecoveryPromiseRealityCheckCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "AGENCY_VALUE DIRECTORY_VALUE OFFICIAL_NUMBER_VALUE CASE_VALUE " +
                            "HELPER_VALUE LAWYER_VALUE INVESTIGATOR_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY, "PLAN_PRIVATE_VALUE")
            )

            assertTrue(
                RecoveryPromiseRealityCheckCardId.SEPARATE_TITLE_AND_OFFICIAL_PRESSURE in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RecoveryPromiseRealityCheckActionId
                            .OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
                        RecoveryPromiseRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                        RecoveryPromiseRealityCheckActionId.FAMILY_ALERT_MESSAGES,
                        RecoveryPromiseRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                        RecoveryPromiseRealityCheckActionId.TRUSTED_HELPER_REVIEW,
                        RecoveryPromiseRealityCheckActionId
                            .EVIDENCE_HANDOFF_PRIVACY_COACH,
                        RecoveryPromiseRealityCheckActionId.OFFICIAL_HELP_DIRECTORY,
                        RecoveryPromiseRealityCheckActionId.REPORT_PREP
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("공식 번호·디렉터리 값"))
            listOf(
                "AGENCY_VALUE",
                "DIRECTORY_VALUE",
                "OFFICIAL_NUMBER_VALUE",
                "CASE_VALUE",
                "HELPER_VALUE",
                "LAWYER_VALUE",
                "INVESTIGATOR_VALUE",
                "PLAN_PRIVATE_VALUE"
            ).forEach { privateValue -> assertFalse(allCopy(coach).contains(privateValue)) }
        }
    }

    @Test
    fun linkSmishingAppInstallAndLiveCallLeaveSameSessionWithoutTechClaims() {
        listOf(
            ScamSignal.LINK,
            ScamSignal.PARCEL_INVITATION_SMISHING,
            ScamSignal.APP_INSTALL,
            ScamSignal.LIVE_CALL_CONTROL
        ).forEach { signal ->
            val coach = RecoveryPromiseRealityCheckCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, signal),
                readiness(ReadinessLevel.READY)
            )

            assertTrue(
                RecoveryPromiseRealityCheckCardId.NO_APP_REMOTE_OR_SCREEN_SHARE in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RecoveryPromiseRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                        RecoveryPromiseRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        RecoveryPromiseRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE,
                        RecoveryPromiseRealityCheckActionId.SAFE_PASTE_PRIVACY_COACH,
                        RecoveryPromiseRealityCheckActionId.PRIVACY_CENTER
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("같은 링크·앱·통화·화면공유"))
            val copy = allCopy(coach)
            listOf(
                "URL 검증 완료",
                "링크 검사 완료",
                "설치 앱 목록을 확인했습니다",
                "악성 앱을 제거했습니다",
                "원격 연결을 종료했습니다",
                "기기를 정리했습니다",
                "지갑을 추적했습니다"
            ).forEach { unsafeClaim -> assertFalse(copy.contains(unsafeClaim)) }
        }
    }

    @Test
    fun familyEmergencyDeepfakeAndNumberChangeUseOnlyValueFreeFamilyRoutes() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val coach = RecoveryPromiseRealityCheckCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal, input = "RAW_CONTACT_VALUE"),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "CONTACT_LABEL PHONE_VALUE SAFE_WORD_VALUE CALLBACK_VALUE"
                )
            )

            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RecoveryPromiseRealityCheckActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH,
                        RecoveryPromiseRealityCheckActionId.FAMILY_CALLBACK_CARD,
                        RecoveryPromiseRealityCheckActionId
                            .VOICE_IMPERSONATION_REALITY_CHECK_COACH,
                        RecoveryPromiseRealityCheckActionId.NEW_NUMBER_QUARANTINE_COACH,
                        RecoveryPromiseRealityCheckActionId
                            .FAMILY_SAFE_WORD_RESET_COACH,
                        RecoveryPromiseRealityCheckActionId.SAFE_CALL_SCRIPTS,
                        RecoveryPromiseRealityCheckActionId.FAMILY_ALERT_MESSAGES
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("연락처·안전단어·콜백 값을 사용하지"))
            listOf(
                "RAW_CONTACT_VALUE",
                "CONTACT_LABEL",
                "PHONE_VALUE",
                "SAFE_WORD_VALUE",
                "CALLBACK_VALUE"
            ).forEach { privateValue -> assertFalse(allCopy(coach).contains(privateValue)) }
        }
    }

    @Test
    fun savedIncidentUsesOnlyClampedPresenceCategory() {
        val baseAnalysis = analysis(RiskLevel.MEDIUM, ScamSignal.LINK)
        val noIncident = RecoveryPromiseRealityCheckCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 0
        )
        val negativeIncident = RecoveryPromiseRealityCheckCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = -91
        )
        val oneIncident = RecoveryPromiseRealityCheckCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 1
        )
        val manyIncidents = RecoveryPromiseRealityCheckCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = Int.MAX_VALUE
        )

        assertEquals(noIncident, negativeIncident)
        assertEquals(oneIncident, manyIncidents)
        assertNotEquals(noIncident.recommendedActionIds, oneIncident.recommendedActionIds)
        assertTrue(
            RecoveryPromiseRealityCheckCardId.VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP in
                oneIncident.recommendedCardIds
        )
        assertTrue(
            oneIncident.recommendedActionIds.containsAll(
                listOf(
                    RecoveryPromiseRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                    RecoveryPromiseRealityCheckActionId.EVIDENCE_KIT,
                    RecoveryPromiseRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER
                )
            )
        )
        assertTrue(oneIncident.recommendationExplanation.contains("개수 범주만"))
        assertTrue(oneIncident.recommendationExplanation.contains("사건 내용이나 미리보기는 읽지"))
    }

    @Test
    fun readinessUsesOnlyLevelAndReadyNeverMakesRecoveryHelperInstitutionOrRequestSafe() {
        listOf(ReadinessLevel.LOW, ReadinessLevel.IN_PROGRESS).forEach { level ->
            val coach = RecoveryPromiseRealityCheckCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
                readiness(level, "READINESS_PRIVATE_LITERAL")
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        RecoveryPromiseRealityCheckActionId.SAFETY_PLAN,
                        RecoveryPromiseRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH,
                        RecoveryPromiseRealityCheckActionId.FAMILY_READINESS_ROADMAP,
                        RecoveryPromiseRealityCheckActionId.FAMILY_SAFETY_DRILL_COACH,
                        RecoveryPromiseRealityCheckActionId.SAFE_CALL_SCRIPTS
                    )
                )
            )
            assertFalse(allCopy(coach).contains("READINESS_PRIVATE_LITERAL"))
        }

        val readyCoach = RecoveryPromiseRealityCheckCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
            readiness(ReadinessLevel.READY, "READY_PRIVATE_LITERAL")
        )
        assertTrue(
            readyCoach.recommendationExplanation.contains(
                "회복 제안·도우미·변호사·수사관·기관·요청이 실제이거나 안전하다는 뜻이 아닙니다"
            )
        )
        assertFalse(allCopy(readyCoach).contains("READY_PRIVATE_LITERAL"))
    }

    @Test
    fun cardsAreCompleteStableAndEveryActionMapsToAnExistingFlow() {
        val coach = RecoveryPromiseRealityCheckCoachBuilder.build(
            null,
            readiness(ReadinessLevel.READY)
        )

        assertEquals(RecoveryPromiseRealityCheckCardId.entries.size, coach.cards.size)
        assertEquals(
            RecoveryPromiseRealityCheckCardId.entries,
            coach.cards.map { card -> card.id }
        )
        assertEquals(
            (1..RecoveryPromiseRealityCheckCardId.entries.size).toList(),
            coach.cards.map { card -> card.displayOrder }
        )
        coach.cards.forEach { card ->
            assertEquals(card.id.displayOrder, card.displayOrder)
            assertTrue(card.title.isNotBlank())
            assertTrue(card.recoveryPromiseScenario.isNotBlank())
            assertTrue(card.valueFreeActionSentence.isNotBlank())
            assertTrue(card.whyItMatters.isNotBlank())
            assertTrue(card.doNotPayShareSubmitInstallOrCallBoundaries.size >= 2)
            assertTrue(card.doNotPayShareSubmitInstallOrCallBoundaries.all { it.isNotBlank() })
            assertTrue(card.primaryAction.label.isNotBlank())
            assertTrue(card.secondaryActions.isNotEmpty())
            assertEquals(
                card.routeActions.size,
                card.routeActions.map { action -> action.id }.distinct().size
            )
        }
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
            .substringAfter("private fun openRecoveryPromiseRealityCheckAction")
            .substringBefore("private fun shareRecoveryPromiseRealityCheckCoach")
            .replace(Regex("\\s+"), " ")
        val expectedRoutes = linkedMapOf(
            RecoveryPromiseRealityCheckActionId.QUICK_STOP to "show(Screen.QUICK_STOP)",
            RecoveryPromiseRealityCheckActionId.CHECKER to "show(Screen.CHECKER)",
            RecoveryPromiseRealityCheckActionId.ANALYSIS_RESULT to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            RecoveryPromiseRealityCheckActionId.SAFETY_PLAN to "show(Screen.PLAN)",
            RecoveryPromiseRealityCheckActionId.FAMILY_READINESS_ROADMAP to
                "show(Screen.FAMILY_READINESS_ROADMAP)",
            RecoveryPromiseRealityCheckActionId.FAMILY_SAFETY_DRILL_COACH to "openDrill()",
            RecoveryPromiseRealityCheckActionId.SAFE_PASTE_PRIVACY_COACH to
                "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            RecoveryPromiseRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS to
                "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            RecoveryPromiseRealityCheckActionId.FAMILY_CALLBACK_CARD to
                "openFamilyCallbackCard(currentAnalysis)",
            RecoveryPromiseRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH to
                "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            RecoveryPromiseRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            RecoveryPromiseRealityCheckActionId.SAFE_CALL_SCRIPTS to
                "show(Screen.SAFE_CALL_SCRIPTS)",
            RecoveryPromiseRealityCheckActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            RecoveryPromiseRealityCheckActionId.NEW_NUMBER_QUARANTINE_COACH to
                "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            RecoveryPromiseRealityCheckActionId.CREDENTIAL_SECRET_FREEZE_COACH to
                "show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            RecoveryPromiseRealityCheckActionId.FAMILY_ALERT_MESSAGES to
                "show(Screen.FAMILY_ALERT_MESSAGES)",
            RecoveryPromiseRealityCheckActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            RecoveryPromiseRealityCheckActionId.RECONTACT_BOUNDARY_COACH to
                "show(Screen.RECONTACT_BOUNDARY_COACH)",
            RecoveryPromiseRealityCheckActionId.TRUSTED_HELPER_REVIEW to
                "show(Screen.TRUSTED_HELPER_REVIEW)",
            RecoveryPromiseRealityCheckActionId.FAMILY_RESPONSE_ROLE_BOARD to
                "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            RecoveryPromiseRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            RecoveryPromiseRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE to
                "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            RecoveryPromiseRealityCheckActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH to
                "show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)",
            RecoveryPromiseRealityCheckActionId.ACCOUNT_LOCKDOWN to
                "show(Screen.ACCOUNT_LOCKDOWN)",
            RecoveryPromiseRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH to
                "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            RecoveryPromiseRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE to
                "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            RecoveryPromiseRealityCheckActionId.PRIVACY_CENTER to
                "show(Screen.PRIVACY_CENTER)",
            RecoveryPromiseRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            RecoveryPromiseRealityCheckActionId.EVIDENCE_KIT to "openEvidenceKit(null)",
            RecoveryPromiseRealityCheckActionId.OFFICIAL_HELP_DIRECTORY to
                "openHelpDirectory(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)",
            RecoveryPromiseRealityCheckActionId.REPORT_PREP to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            RecoveryPromiseRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER to
                "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            RecoveryPromiseRealityCheckActionId.SAFETY_FLOW_ROUTER to
                "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(RecoveryPromiseRealityCheckActionId.entries, expectedRoutes.keys.toList())
        expectedRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "action did not map to an existing flow: $actionId",
                routerSource.contains(
                    "RecoveryPromiseRealityCheckActionId.$actionId -> $destination"
                )
            )
        }
    }

    @Test
    fun familyShareSummaryIsRebuiltOnlyFromGenericCardsRoutesAndActions() {
        val coach = RecoveryPromiseRealityCheckCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.BAIT,
                ScamSignal.CREDENTIALS,
                ScamSignal.IMPERSONATION,
                ScamSignal.LINK,
                input =
                    "RAW_INPUT REDACTED_CLUE AGENCY_NAME OFFICIAL_NUMBER CASE_ID " +
                        "HELPER_NAME LAWYER_NAME INVESTIGATOR_NAME OTP_VALUE PIN_VALUE " +
                        "PASSWORD_VALUE SECURITY_ANSWER ACCOUNT_VALUE CARD_VALUE BANK_VALUE " +
                        "WALLET_VALUE TRANSACTION_VALUE RESIDENT_VALUE PHONE_VALUE " +
                        "HTTPS_VALUE DOMAIN_VALUE PACKAGE_VALUE APP_VALUE DEVICE_VALUE " +
                        "FILE_VALUE URI_VALUE SCREENSHOT_VALUE OCR_VALUE AUDIO_VALUE " +
                        "TRANSCRIPT_VALUE CLIPBOARD_VALUE LOGIN_STATE CALL_STATE SMS_STATE " +
                        "NOTIFICATION_STATE RECIPIENT_VALUE REPORT_STATUS SUBMISSION_STATUS " +
                        "RECEIPT_ID REFUND_STATUS CHARGEBACK_STATUS RECOVERY_STATUS " +
                        "FREEZE_STATUS CANCEL_STATUS BLOCK_STATUS TRACE_STATUS LEGAL_RESULT"
            ),
            readiness = readiness(
                ReadinessLevel.READY,
                "SAFE_WORD_VALUE CALLBACK_VALUE CONTACT_VALUE PLAN_VALUE"
            ),
            incidentCount = 7
        )
        val summary = coach.familyShareSummary

        assertTrue(summary.contains("회복 약속 현실 점검 코치"))
        assertTrue(summary.contains("추천 회복 약속 점검 카드"))
        coach.recommendedCards.forEach { card -> assertTrue(summary.contains(card.title)) }
        coach.recommendedActions.forEach { action -> assertTrue(summary.contains(action.label)) }
        listOf(
            "RAW_INPUT",
            "REDACTED_CLUE",
            "AGENCY_NAME",
            "OFFICIAL_NUMBER",
            "CASE_ID",
            "HELPER_NAME",
            "LAWYER_NAME",
            "INVESTIGATOR_NAME",
            "OTP_VALUE",
            "PIN_VALUE",
            "PASSWORD_VALUE",
            "SECURITY_ANSWER",
            "ACCOUNT_VALUE",
            "CARD_VALUE",
            "BANK_VALUE",
            "WALLET_VALUE",
            "TRANSACTION_VALUE",
            "RESIDENT_VALUE",
            "PHONE_VALUE",
            "HTTPS_VALUE",
            "DOMAIN_VALUE",
            "PACKAGE_VALUE",
            "APP_VALUE",
            "DEVICE_VALUE",
            "FILE_VALUE",
            "URI_VALUE",
            "SCREENSHOT_VALUE",
            "OCR_VALUE",
            "AUDIO_VALUE",
            "TRANSCRIPT_VALUE",
            "CLIPBOARD_VALUE",
            "LOGIN_STATE",
            "CALL_STATE",
            "SMS_STATE",
            "NOTIFICATION_STATE",
            "RECIPIENT_VALUE",
            "REPORT_STATUS",
            "SUBMISSION_STATUS",
            "RECEIPT_ID",
            "REFUND_STATUS",
            "CHARGEBACK_STATUS",
            "RECOVERY_STATUS",
            "FREEZE_STATUS",
            "CANCEL_STATUS",
            "BLOCK_STATUS",
            "TRACE_STATUS",
            "LEGAL_RESULT",
            "SAFE_WORD_VALUE",
            "CALLBACK_VALUE",
            "CONTACT_VALUE",
            "PLAN_VALUE"
        ).forEach { forbiddenValue ->
            assertFalse("share leaked $forbiddenValue", summary.contains(forbiddenValue))
        }
        assertFalse(
            Regex("https?://|www\\.|content://", RegexOption.IGNORE_CASE)
                .containsMatchIn(summary)
        )
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
        val first = RecoveryPromiseRealityCheckCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.BAIT,
                ScamSignal.CREDENTIALS,
                input = "FIRST_RAW_VALUE",
                score = 1
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "FIRST_PLAN_VALUE"),
            incidentCount = 2
        )
        val second = RecoveryPromiseRealityCheckCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.BAIT,
                ScamSignal.CREDENTIALS,
                input = "SECOND_RAW_VALUE",
                score = 999
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "SECOND_PLAN_VALUE"),
            incidentCount = 93
        )

        assertEquals(first, second)
        assertEquals(
            first,
            RecoveryPromiseRealityCheckCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.HIGH,
                    ScamSignal.BAIT,
                    ScamSignal.CREDENTIALS,
                    input = "FIRST_RAW_VALUE",
                    score = 1
                ),
                readiness = readiness(ReadinessLevel.IN_PROGRESS, "FIRST_PLAN_VALUE"),
                incidentCount = 2
            )
        )
        listOf(
            "FIRST_RAW_VALUE",
            "SECOND_RAW_VALUE",
            "FIRST_PLAN_VALUE",
            "SECOND_PLAN_VALUE"
        ).forEach { privateValue ->
            assertFalse(allCopy(first).contains(privateValue))
            assertFalse(allCopy(second).contains(privateValue))
        }
    }

    @Test
    fun privacyCopyStatesManualLocalStatelessAndAllNoActionBoundaries() {
        val copy = allCopy(
            RecoveryPromiseRealityCheckCoachBuilder.build(
                null,
                readiness(ReadinessLevel.READY)
            )
        )

        listOf(
            "로컬 전용·무상태 수동 안내",
            "입력을 요구하지 않습니다",
            "회복·환급·차지백·취소·동결·차단·회수·추적",
            "법률 서비스·자문",
            "지갑·블록체인·가상자산·거래소 추적",
            "결제·은행·카드 기능 또는 API·NFC",
            "공식 번호 조회",
            "전화번호 검증",
            "URL 검증",
            "발신자 표시 확인",
            "문서 검증",
            "신고 제출",
            "네트워크·웹 탐색·브라우저·지도·위치·지오코딩",
            "전화·다이얼·발신·통화",
            "문자·SMS",
            "연락처·주소록·통화 기록·통화 상태·문자·알림",
            "클립보드·텍스트 감시기·자동완성",
            "자격 증명·계정·인증",
            "파일·문서·사진·이미지·스크린샷·오디오·미디어·저장소",
            "카메라·화면 캡처·OCR·이미지 분석·녹음·전사",
            "첨부·스트림·URI 공유",
            "자동 발송하지 않습니다",
            "새 Android 권한",
            "백그라운드 워커·스케줄러",
            "기기 관리자·접근성 서비스·VPN",
            "설치 앱·패키지 목록 조회",
            "새 저장소를 만들지 않고"
        ).forEach { boundary ->
            assertTrue("privacy copy omitted $boundary", copy.contains(boundary))
        }
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndAutomaticProtectionClaims() {
        val copy = allCopy(
            RecoveryPromiseRealityCheckCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.READY),
                incidentCount = 1
            )
        )

        listOf(
            "verified recovery",
            "refund guaranteed",
            "chargeback accepted",
            "case accepted",
            "wallet traced",
            "payment cancelled",
            "account frozen",
            "account protected",
            "problem-free",
            "회복이 확인됐습니다",
            "환급이 보장됩니다",
            "차지백이 접수됐습니다",
            "사건이 접수됐습니다",
            "지갑 추적이 완료됐습니다",
            "결제가 취소됐습니다",
            "계정이 동결됐습니다",
            "계정이 보호됐습니다",
            "회수됐습니다",
            "환급됐습니다",
            "취소됐습니다",
            "차단됐습니다",
            "해결됐습니다",
            "완료됐습니다",
            "문제없습니다",
            "자동 계정 동결",
            "자동 결제 취소",
            "피해 회복을 보장",
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
            "app/src/main/java/com/mongmong/familyshield/RecoveryPromiseRealityCheckCoach.kt"
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
            "dialNumber",
            "ClipboardManager",
            "getPrimaryClip",
            "setPrimaryClip",
            "TextWatcher",
            "addTextChangedListener",
            "AutofillManager",
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
            "FileProvider",
            "MediaStore",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Intent.ACTION_VIEW",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "ACTION_SENDTO",
            "Geocoder",
            "Patterns.WEB_URL",
            "URLUtil",
            "InetAddress",
            "Patterns.PHONE",
            "BillingClient",
            "NfcAdapter",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "PackageInstaller",
            "Settings.",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenSource ->
            assertFalse(
                "recovery promise model used forbidden source: $forbiddenSource",
                source.contains(forbiddenSource)
            )
        }
    }

    @Test
    fun highRiskShowsAllCardsAndRoutesWhileSignalCategoriesRemainDistinct() {
        val highRisk = RecoveryPromiseRealityCheckCoachBuilder.build(
            analysis(RiskLevel.HIGH, ScamSignal.SECRECY),
            readiness(ReadinessLevel.READY)
        )
        assertEquals(RecoveryPromiseRealityCheckCardId.entries, highRisk.recommendedCardIds)
        assertEquals(RecoveryPromiseRealityCheckActionId.entries, highRisk.recommendedActionIds)

        val financial = RecoveryPromiseRealityCheckCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.SAFE_ACCOUNT_FRAUD),
            readiness(ReadinessLevel.READY)
        )
        val link = RecoveryPromiseRealityCheckCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.LINK),
            readiness(ReadinessLevel.READY)
        )
        val family = RecoveryPromiseRealityCheckCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.FAMILY_EMERGENCY),
            readiness(ReadinessLevel.READY)
        )
        assertNotEquals(financial.recommendedActionIds, link.recommendedActionIds)
        assertNotEquals(link.recommendedActionIds, family.recommendedActionIds)
        assertNotEquals(financial.recommendationTitle, family.recommendationTitle)
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

    private fun allCopy(coach: RecoveryPromiseRealityCheckCoach): String = buildString {
        appendLine(coach.title)
        appendLine(coach.intro)
        appendLine(coach.modeLabel)
        appendLine(coach.recommendationTitle)
        appendLine(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> appendLine(action.label) }
        coach.cards.forEach { card ->
            appendLine(card.title)
            appendLine(card.recoveryPromiseScenario)
            appendLine(card.valueFreeActionSentence)
            appendLine(card.whyItMatters)
            card.doNotPayShareSubmitInstallOrCallBoundaries.forEach(::appendLine)
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
