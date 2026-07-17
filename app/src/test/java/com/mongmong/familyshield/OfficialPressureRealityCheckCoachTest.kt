package com.mongmong.familyshield

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfficialPressureRealityCheckCoachTest {
    @Test
    fun noAnalysisCoachIsConservativeAndNeverDeclaresOfficialOrOutcomeStatus() {
        val coach = OfficialPressureRealityCheckCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(OfficialPressureRealityCheckCardId.entries, coach.recommendedCardIds)
        assertEquals(
            OfficialPressureRealityCheckActionId.entries.filterNot { actionId ->
                actionId == OfficialPressureRealityCheckActionId.ANALYSIS_RESULT
            },
            coach.recommendedActionIds
        )
        assertTrue(coach.recommendationExplanation.contains("모든 압박 카드를 보수적으로"))
        assertTrue(coach.recommendationExplanation.contains("단정하지 말고"))

        val copy = allCopy(coach)
        listOf(
            "verified official",
            "official confirmed",
            "valid case",
            "safe caller",
            "secure account",
            "trusted number",
            "real institution",
            "report accepted",
            "report submitted",
            "report received",
            "payment blocked",
            "issue resolved",
            "recovery complete",
            "account recovered",
            "payment refunded",
            "card frozen",
            "device protected",
            "legal request",
            "problem-free",
            "공식 기관입니다",
            "공식 번호로 확인됐습니다",
            "직함이 확인됐습니다",
            "발신자가 확인됐습니다",
            "유효한 사건입니다",
            "안전한 링크입니다",
            "신뢰할 수 있는 문서입니다",
            "신고가 접수됐습니다",
            "신고가 제출됐습니다",
            "결제가 차단됐습니다",
            "문제가 해결됐습니다",
            "복구가 완료됐습니다",
            "계정이 복구됐습니다",
            "결제가 환급됐습니다",
            "카드가 동결됐습니다",
            "기기가 보호됐습니다",
            "합법적인 요청입니다",
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
    fun impersonationAndSecrecyRecommendDisclosureStopHelpAndEvidenceRoutes() {
        listOf(ScamSignal.IMPERSONATION, ScamSignal.SECRECY).forEach { signal ->
            val coach = OfficialPressureRealityCheckCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input = "AGENCY_VALUE DIRECTORY_VALUE CASE_VALUE OFFICIAL_NUMBER_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY, "PLAN_PRIVATE_VALUE")
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        OfficialPressureRealityCheckCardId
                            .TITLE_CHANNEL_AND_DOCUMENT_ARE_NOT_PROOF,
                        OfficialPressureRealityCheckCardId.BREAK_SECRECY_AND_PRESSURE,
                        OfficialPressureRealityCheckCardId.FIND_A_PUBLIC_CHANNEL_LATER,
                        OfficialPressureRealityCheckCardId
                            .PREPARE_VALUE_FREE_EVIDENCE_AND_REPORT
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        OfficialPressureRealityCheckActionId.QUICK_STOP,
                        OfficialPressureRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                        OfficialPressureRealityCheckActionId.FAMILY_ALERT_MESSAGES,
                        OfficialPressureRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                        OfficialPressureRealityCheckActionId.TRUSTED_HELPER_REVIEW,
                        OfficialPressureRealityCheckActionId
                            .EVIDENCE_HANDOFF_PRIVACY_COACH,
                        OfficialPressureRealityCheckActionId.PRIVACY_CENTER,
                        OfficialPressureRealityCheckActionId.OFFICIAL_HELP_DIRECTORY,
                        OfficialPressureRealityCheckActionId.REPORT_PREP
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("요청을 숨긴 채 이어가지 말고"))
            assertTrue(coach.recommendationExplanation.contains("디렉터리 값은 사용하지"))
            listOf(
                "AGENCY_VALUE",
                "DIRECTORY_VALUE",
                "CASE_VALUE",
                "OFFICIAL_NUMBER_VALUE",
                "PLAN_PRIVATE_VALUE"
            ).forEach { privateValue -> assertFalse(allCopy(coach).contains(privateValue)) }
        }
    }

    @Test
    fun financialSignalsRecommendPauseCredentialAccountRemoteAndExposureRoutes() {
        listOf(
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.OVERSEAS_PAYMENT_ALERT,
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.BAIT,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.GIFTICON_PIN
        ).forEach { signal ->
            val coach = OfficialPressureRealityCheckCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, signal),
                readiness(ReadinessLevel.READY)
            )

            assertTrue(
                OfficialPressureRealityCheckCardId.PAUSE_MONEY_BEFORE_ANY_CHECK in
                    coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        OfficialPressureRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE,
                        OfficialPressureRealityCheckActionId.CREDENTIAL_SECRET_FREEZE_COACH,
                        OfficialPressureRealityCheckActionId.ACCOUNT_LOCKDOWN,
                        OfficialPressureRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        OfficialPressureRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE
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
                "피해금을 회수했습니다",
                "복구됐습니다",
                "기관 확인이 완료됐습니다"
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
            val coach = OfficialPressureRealityCheckCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "OTP_VALUE PIN_VALUE PASSWORD_VALUE SECURITY_ANSWER_VALUE " +
                            "ACCOUNT_VALUE CARD_VALUE"
                ),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "SAFE_WORD_VALUE CALLBACK_VALUE CONTACT_VALUE"
                )
            )

            assertTrue(
                OfficialPressureRealityCheckCardId
                    .SEPARATE_CREDENTIAL_APP_AND_SCREEN_REQUESTS in coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        OfficialPressureRealityCheckActionId.CREDENTIAL_SECRET_FREEZE_COACH,
                        OfficialPressureRealityCheckActionId.SAFE_PASTE_PRIVACY_COACH,
                        OfficialPressureRealityCheckActionId.ACCOUNT_LOCKDOWN,
                        OfficialPressureRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        OfficialPressureRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE
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
                "SAFE_WORD_VALUE",
                "CALLBACK_VALUE",
                "CONTACT_VALUE"
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
            val coach = OfficialPressureRealityCheckCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, signal),
                readiness(ReadinessLevel.READY)
            )

            assertTrue(
                OfficialPressureRealityCheckCardId
                    .SEPARATE_CREDENTIAL_APP_AND_SCREEN_REQUESTS in coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        OfficialPressureRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                        OfficialPressureRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH,
                        OfficialPressureRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE,
                        OfficialPressureRealityCheckActionId.PRIVACY_CENTER
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
    fun familyEmergencyDeepfakeAndNumberChangeUseOnlyValueFreeFamilyRoutes() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val coach = OfficialPressureRealityCheckCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal, input = "RAW_CONTACT_VALUE"),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "CONTACT_LABEL PHONE_VALUE SAFE_WORD_VALUE CALLBACK_VALUE"
                )
            )

            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        OfficialPressureRealityCheckActionId
                            .SEPARATE_CHANNEL_VERIFICATION_COACH,
                        OfficialPressureRealityCheckActionId.FAMILY_CALLBACK_CARD,
                        OfficialPressureRealityCheckActionId
                            .VOICE_IMPERSONATION_REALITY_CHECK_COACH,
                        OfficialPressureRealityCheckActionId.NEW_NUMBER_QUARANTINE_COACH,
                        OfficialPressureRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH,
                        OfficialPressureRealityCheckActionId.SAFE_CALL_SCRIPTS,
                        OfficialPressureRealityCheckActionId.FAMILY_ALERT_MESSAGES
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
        val noIncident = OfficialPressureRealityCheckCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 0
        )
        val negativeIncident = OfficialPressureRealityCheckCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = -91
        )
        val oneIncident = OfficialPressureRealityCheckCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 1
        )
        val manyIncidents = OfficialPressureRealityCheckCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = Int.MAX_VALUE
        )

        assertEquals(noIncident, negativeIncident)
        assertEquals(oneIncident, manyIncidents)
        assertNotEquals(noIncident.recommendedActionIds, oneIncident.recommendedActionIds)
        assertTrue(
            OfficialPressureRealityCheckCardId.PREPARE_VALUE_FREE_EVIDENCE_AND_REPORT in
                oneIncident.recommendedCardIds
        )
        assertTrue(
            oneIncident.recommendedActionIds.containsAll(
                listOf(
                    OfficialPressureRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                    OfficialPressureRealityCheckActionId.EVIDENCE_KIT,
                    OfficialPressureRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER
                )
            )
        )
        assertTrue(oneIncident.recommendationExplanation.contains("개수 범주만"))
        assertTrue(oneIncident.recommendationExplanation.contains("사건 내용이나 미리보기는 읽지"))
    }

    @Test
    fun readinessUsesOnlyLevelAndReadyNeverMakesInstitutionContactOrRequestReal() {
        listOf(ReadinessLevel.LOW, ReadinessLevel.IN_PROGRESS).forEach { level ->
            val coach = OfficialPressureRealityCheckCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
                readiness(level, "READINESS_PRIVATE_LITERAL")
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        OfficialPressureRealityCheckActionId.SAFETY_PLAN,
                        OfficialPressureRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH,
                        OfficialPressureRealityCheckActionId.FAMILY_READINESS_ROADMAP,
                        OfficialPressureRealityCheckActionId.FAMILY_SAFETY_DRILL_COACH,
                        OfficialPressureRealityCheckActionId.SAFE_CALL_SCRIPTS
                    )
                )
            )
            assertFalse(allCopy(coach).contains("READINESS_PRIVATE_LITERAL"))
        }

        val readyCoach = OfficialPressureRealityCheckCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
            readiness(ReadinessLevel.READY, "READY_PRIVATE_LITERAL")
        )
        assertTrue(
            readyCoach.recommendationExplanation.contains(
                "기관·연락·요청이 실재하거나 믿을 만하다는 뜻이 아닙니다"
            )
        )
        assertFalse(allCopy(readyCoach).contains("READY_PRIVATE_LITERAL"))
    }

    @Test
    fun cardsAreCompleteStableAndEveryActionMapsToAnExistingFlow() {
        val coach = OfficialPressureRealityCheckCoachBuilder.build(
            null,
            readiness(ReadinessLevel.READY)
        )

        assertEquals(OfficialPressureRealityCheckCardId.entries.size, coach.cards.size)
        assertEquals(
            OfficialPressureRealityCheckCardId.entries,
            coach.cards.map { card -> card.id }
        )
        assertEquals(
            (1..OfficialPressureRealityCheckCardId.entries.size).toList(),
            coach.cards.map { card -> card.displayOrder }
        )
        coach.cards.forEach { card ->
            assertEquals(card.id.displayOrder, card.displayOrder)
            assertTrue(card.title.isNotBlank())
            assertTrue(card.pressureScenario.isNotBlank())
            assertTrue(card.valueFreeActionSentence.isNotBlank())
            assertTrue(card.whyItMatters.isNotBlank())
            assertTrue(card.doNotCallLookupShareOrSubmitBoundaries.size >= 2)
            assertTrue(card.doNotCallLookupShareOrSubmitBoundaries.all { it.isNotBlank() })
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
            .substringAfter("private fun openOfficialPressureRealityCheckAction")
            .substringBefore("private fun shareOfficialPressureRealityCheckCoach")
            .replace(Regex("\\s+"), " ")
        val expectedRoutes = linkedMapOf(
            OfficialPressureRealityCheckActionId.QUICK_STOP to "show(Screen.QUICK_STOP)",
            OfficialPressureRealityCheckActionId.CHECKER to "show(Screen.CHECKER)",
            OfficialPressureRealityCheckActionId.ANALYSIS_RESULT to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            OfficialPressureRealityCheckActionId.SAFETY_PLAN to "show(Screen.PLAN)",
            OfficialPressureRealityCheckActionId.FAMILY_READINESS_ROADMAP to
                "show(Screen.FAMILY_READINESS_ROADMAP)",
            OfficialPressureRealityCheckActionId.FAMILY_SAFETY_DRILL_COACH to "openDrill()",
            OfficialPressureRealityCheckActionId.SAFE_PASTE_PRIVACY_COACH to
                "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            OfficialPressureRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS to
                "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            OfficialPressureRealityCheckActionId.FAMILY_CALLBACK_CARD to
                "openFamilyCallbackCard(currentAnalysis)",
            OfficialPressureRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH to
                "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            OfficialPressureRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            OfficialPressureRealityCheckActionId.SAFE_CALL_SCRIPTS to
                "show(Screen.SAFE_CALL_SCRIPTS)",
            OfficialPressureRealityCheckActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            OfficialPressureRealityCheckActionId.NEW_NUMBER_QUARANTINE_COACH to
                "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            OfficialPressureRealityCheckActionId.CREDENTIAL_SECRET_FREEZE_COACH to
                "show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            OfficialPressureRealityCheckActionId.FAMILY_ALERT_MESSAGES to
                "show(Screen.FAMILY_ALERT_MESSAGES)",
            OfficialPressureRealityCheckActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            OfficialPressureRealityCheckActionId.RECONTACT_BOUNDARY_COACH to
                "show(Screen.RECONTACT_BOUNDARY_COACH)",
            OfficialPressureRealityCheckActionId.TRUSTED_HELPER_REVIEW to
                "show(Screen.TRUSTED_HELPER_REVIEW)",
            OfficialPressureRealityCheckActionId.FAMILY_RESPONSE_ROLE_BOARD to
                "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            OfficialPressureRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            OfficialPressureRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE to
                "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            OfficialPressureRealityCheckActionId.ACCOUNT_LOCKDOWN to
                "show(Screen.ACCOUNT_LOCKDOWN)",
            OfficialPressureRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH to
                "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            OfficialPressureRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE to
                "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            OfficialPressureRealityCheckActionId.PRIVACY_CENTER to
                "show(Screen.PRIVACY_CENTER)",
            OfficialPressureRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            OfficialPressureRealityCheckActionId.EVIDENCE_KIT to "openEvidenceKit(null)",
            OfficialPressureRealityCheckActionId.OFFICIAL_HELP_DIRECTORY to
                "openHelpDirectory(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)",
            OfficialPressureRealityCheckActionId.REPORT_PREP to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            OfficialPressureRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER to
                "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            OfficialPressureRealityCheckActionId.SAFETY_FLOW_ROUTER to
                "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(OfficialPressureRealityCheckActionId.entries, expectedRoutes.keys.toList())
        expectedRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "action did not map to an existing flow: $actionId",
                routerSource.contains(
                    "OfficialPressureRealityCheckActionId.$actionId -> $destination"
                )
            )
        }
    }

    @Test
    fun familyShareSummaryIsRebuiltOnlyFromGenericCardsRoutesAndActions() {
        val coach = OfficialPressureRealityCheckCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.IMPERSONATION,
                ScamSignal.CREDENTIALS,
                ScamSignal.LINK,
                input =
                    "RAW_INPUT REDACTED_CLUE AGENCY_NAME OFFICIAL_NUMBER CASE_ID " +
                        "OTP_VALUE PIN_VALUE PASSWORD_VALUE SECURITY_ANSWER ACCOUNT_VALUE " +
                        "CARD_VALUE RESIDENT_VALUE PHONE_VALUE HTTPS_VALUE DOMAIN_VALUE " +
                        "PACKAGE_VALUE APP_VALUE DEVICE_VALUE FILE_VALUE URI_VALUE " +
                        "SCREENSHOT_VALUE OCR_VALUE AUDIO_VALUE TRANSCRIPT_VALUE " +
                        "CLIPBOARD_VALUE LOGIN_STATE RECIPIENT_VALUE REPORT_STATUS " +
                        "SUBMISSION_STATUS RECOVERY_STATUS"
            ),
            readiness = readiness(
                ReadinessLevel.READY,
                "SAFE_WORD_VALUE CALLBACK_VALUE CONTACT_VALUE PLAN_VALUE"
            ),
            incidentCount = 7
        )
        val summary = coach.familyShareSummary

        assertTrue(summary.contains("기관 사칭 압박 현실 점검 코치"))
        assertTrue(summary.contains("추천 현실 점검 카드"))
        coach.recommendedCards.forEach { card -> assertTrue(summary.contains(card.title)) }
        coach.recommendedActions.forEach { action -> assertTrue(summary.contains(action.label)) }
        listOf(
            "RAW_INPUT",
            "REDACTED_CLUE",
            "AGENCY_NAME",
            "OFFICIAL_NUMBER",
            "CASE_ID",
            "OTP_VALUE",
            "PIN_VALUE",
            "PASSWORD_VALUE",
            "SECURITY_ANSWER",
            "ACCOUNT_VALUE",
            "CARD_VALUE",
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
            "RECIPIENT_VALUE",
            "REPORT_STATUS",
            "SUBMISSION_STATUS",
            "RECOVERY_STATUS",
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
        val first = OfficialPressureRealityCheckCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.IMPERSONATION,
                ScamSignal.CREDENTIALS,
                input = "FIRST_RAW_VALUE",
                score = 1
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "FIRST_PLAN_VALUE"),
            incidentCount = 2
        )
        val second = OfficialPressureRealityCheckCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.IMPERSONATION,
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
            OfficialPressureRealityCheckCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.HIGH,
                    ScamSignal.IMPERSONATION,
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
    fun privacyCopyStatesManualLocalStatelessAndAllNoLookupNoPermissionBoundaries() {
        val copy = allCopy(
            OfficialPressureRealityCheckCoachBuilder.build(
                null,
                readiness(ReadinessLevel.READY)
            )
        )

        listOf(
            "로컬 전용·무상태 수동 안내",
            "입력을 요구하지 않습니다",
            "공식 번호 조회",
            "전화번호 검증",
            "URL 검증",
            "도메인 스캔",
            "발신자 표시 확인",
            "문서 검증",
            "신고 제출",
            "네트워크·웹 탐색·브라우저·지도·위치·지오코딩",
            "전화·다이얼·발신·통화",
            "문자·SMS",
            "연락처·주소록·통화 기록·통화 상태·문자·알림",
            "클립보드·텍스트 감시기·자동완성",
            "Credential Manager·Account Manager",
            "파일·문서·사진·이미지·스크린샷·오디오·미디어·저장소",
            "카메라·화면 캡처·OCR·이미지 분석·녹음·전사",
            "첨부·스트림·URI 공유",
            "자동 발송하지 않습니다",
            "새 Android 권한",
            "백그라운드 워커·스케줄러",
            "결제·은행 기능이나 API·NFC",
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
            OfficialPressureRealityCheckCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.READY),
                incidentCount = 1
            )
        )

        listOf(
            "verified official",
            "official confirmed",
            "valid case",
            "safe number",
            "secure link",
            "trusted caller",
            "report accepted",
            "report submitted",
            "report received",
            "payment blocked",
            "issue resolved",
            "recovery complete",
            "payment refunded",
            "account frozen",
            "device protected",
            "problem-free",
            "공식이라고 확인했습니다",
            "기관이 확인됐습니다",
            "유효한 사건입니다",
            "안전한 번호입니다",
            "보안된 링크입니다",
            "신뢰할 수 있는 발신자입니다",
            "신고가 접수됐습니다",
            "신고를 제출했습니다",
            "신고가 수신됐습니다",
            "결제가 차단됐습니다",
            "문제가 해결됐습니다",
            "복구가 완료됐습니다",
            "결제가 환급됐습니다",
            "계정이 동결됐습니다",
            "기기가 보호됐습니다",
            "문제없습니다",
            "자동 계정 동결",
            "자동 결제 취소",
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
            "app/src/main/java/com/mongmong/familyshield/OfficialPressureRealityCheckCoach.kt"
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
                "official pressure model used forbidden source: $forbiddenSource",
                source.contains(forbiddenSource)
            )
        }
    }

    @Test
    fun highRiskShowsAllCardsAndRoutesWhileSignalCategoriesRemainDistinct() {
        val highRisk = OfficialPressureRealityCheckCoachBuilder.build(
            analysis(RiskLevel.HIGH, ScamSignal.SECRECY),
            readiness(ReadinessLevel.READY)
        )
        assertEquals(OfficialPressureRealityCheckCardId.entries, highRisk.recommendedCardIds)
        assertEquals(OfficialPressureRealityCheckActionId.entries, highRisk.recommendedActionIds)

        val financial = OfficialPressureRealityCheckCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.SAFE_ACCOUNT_FRAUD),
            readiness(ReadinessLevel.READY)
        )
        val link = OfficialPressureRealityCheckCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.LINK),
            readiness(ReadinessLevel.READY)
        )
        val family = OfficialPressureRealityCheckCoachBuilder.build(
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

    private fun allCopy(coach: OfficialPressureRealityCheckCoach): String = buildString {
        appendLine(coach.title)
        appendLine(coach.intro)
        appendLine(coach.modeLabel)
        appendLine(coach.recommendationTitle)
        appendLine(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> appendLine(action.label) }
        coach.cards.forEach { card ->
            appendLine(card.title)
            appendLine(card.pressureScenario)
            appendLine(card.valueFreeActionSentence)
            appendLine(card.whyItMatters)
            card.doNotCallLookupShareOrSubmitBoundaries.forEach(::appendLine)
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
