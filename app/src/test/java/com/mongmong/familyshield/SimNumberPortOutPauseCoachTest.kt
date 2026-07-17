package com.mongmong.familyshield

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimNumberPortOutPauseCoachTest {
    @Test
    fun noAnalysisCoachIsConservativeAndNeverDeclaresTelecomOrPortStatus() {
        val coach = SimNumberPortOutPauseCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(SimNumberPortOutPauseCardId.entries, coach.recommendedCardIds)
        assertEquals(
            SimNumberPortOutPauseActionId.entries.filterNot { actionId ->
                actionId == SimNumberPortOutPauseActionId.ANALYSIS_RESULT
            },
            coach.recommendedActionIds
        )
        assertEquals("SIM·번호이동 멈춤 코치", coach.title)
        assertTrue(coach.recommendationTitle.contains("모든"))
        assertTrue(coach.recommendationExplanation.contains("상태를 공식"))
        assertTrue(coach.recommendationExplanation.contains("단정하지 않고"))

        listOf(
            "official carrier",
            "verified number",
            "safe SIM",
            "trusted store",
            "port completed",
            "SIM reissued",
            "number activated",
            "account protected",
            "report submitted",
            "refund guaranteed",
            "problem-free",
            "공식 번호입니다",
            "검증된 통신사입니다",
            "안전한 SIM입니다",
            "번호이동이 완료됐습니다",
            "재발급됐습니다",
            "개통됐습니다",
            "해지됐습니다",
            "차단됐습니다",
            "복구됐습니다",
            "계정이 보호됐습니다",
            "신고가 제출됐습니다",
            "문제없습니다",
            "100%"
        ).forEach { unsafeClaim ->
            assertFalse(
                "default coach made unsafe claim: $unsafeClaim",
                allCopy(coach).contains(unsafeClaim, ignoreCase = true)
            )
        }
    }

    @Test
    fun credentialOtpAndPaymentSignalsRecommendValueFreeAccountAndExposureRoutes() {
        listOf(
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = SimNumberPortOutPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "OTP_VALUE PIN_VALUE PASSWORD_VALUE PORTING_PIN_VALUE ACCOUNT_VALUE " +
                            "CARD_VALUE BANK_VALUE WALLET_VALUE GIFT_CARD_VALUE CASH_VALUE " +
                            "SIM_VALUE CARRIER_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY, "PRIVATE_PLAN_VALUE")
            )

            assertRoutes(
                coach,
                SimNumberPortOutPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH,
                SimNumberPortOutPauseActionId.ACCOUNT_LOCKDOWN,
                SimNumberPortOutPauseActionId.SAFE_PASTE_PRIVACY_COACH,
                SimNumberPortOutPauseActionId.REMOTE_ACCESS_CUTOFF_COACH,
                SimNumberPortOutPauseActionId.EXPOSURE_IMPACT_TRIAGE
            )
            assertTrue(coach.recommendationExplanation.contains("OTP·PIN·비밀번호"))
            assertNoPrivateValues(
                coach,
                "OTP_VALUE",
                "PIN_VALUE",
                "PASSWORD_VALUE",
                "PORTING_PIN_VALUE",
                "ACCOUNT_VALUE",
                "CARD_VALUE",
                "BANK_VALUE",
                "WALLET_VALUE",
                "GIFT_CARD_VALUE",
                "CASH_VALUE",
                "SIM_VALUE",
                "CARRIER_VALUE",
                "PRIVATE_PLAN_VALUE"
            )
        }
    }

    @Test
    fun numberChangeFamilyEmergencyAndDeepfakeUseOnlyValueFreeFamilyRoutes() {
        listOf(
            ScamSignal.PHONE_NUMBER_CHANGE,
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE
        ).forEach { signal ->
            val coach = SimNumberPortOutPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "PHONE_VALUE NEW_NUMBER_VALUE OLD_NUMBER_VALUE CONTACT_VALUE " +
                            "CHANNEL_VALUE PROFILE_VALUE CALL_STATE SMS_STATE"
                ),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "SAFE_WORD_VALUE CALLBACK_VALUE CONTACT_LABEL_VALUE"
                )
            )

            assertRoutes(
                coach,
                SimNumberPortOutPauseActionId.NEW_NUMBER_QUARANTINE_COACH,
                SimNumberPortOutPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                SimNumberPortOutPauseActionId.FAMILY_CALLBACK_CARD,
                SimNumberPortOutPauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH,
                SimNumberPortOutPauseActionId.FAMILY_SAFE_WORD_RESET_COACH,
                SimNumberPortOutPauseActionId.SAFE_CALL_SCRIPTS,
                SimNumberPortOutPauseActionId.FAMILY_ALERT_MESSAGES
            )
            assertTrue(coach.recommendationExplanation.contains("전화번호·연락처·안전단어·콜백 값"))
            assertTrue(coach.recommendationExplanation.contains("통화·문자를 사용하지 않습니다"))
            assertNoPrivateValues(
                coach,
                "PHONE_VALUE",
                "NEW_NUMBER_VALUE",
                "OLD_NUMBER_VALUE",
                "CONTACT_VALUE",
                "CHANNEL_VALUE",
                "PROFILE_VALUE",
                "CALL_STATE",
                "SMS_STATE",
                "SAFE_WORD_VALUE",
                "CALLBACK_VALUE",
                "CONTACT_LABEL_VALUE"
            )
        }
    }

    @Test
    fun officialPressureAndSecrecyRouteToManualHelpWithoutLookupOrSubmission() {
        listOf(
            ScamSignal.IMPERSONATION,
            ScamSignal.SECRECY,
            ScamSignal.SAFE_ACCOUNT_FRAUD
        ).forEach { signal ->
            val coach = SimNumberPortOutPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "CARRIER_VALUE STORE_VALUE STAFF_VALUE AGENCY_VALUE OFFICIAL_NUMBER_VALUE " +
                            "DOCUMENT_VALUE CASE_VALUE REPORT_VALUE RECEIPT_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertRoutes(
                coach,
                SimNumberPortOutPauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
                SimNumberPortOutPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                SimNumberPortOutPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                SimNumberPortOutPauseActionId.FAMILY_ALERT_MESSAGES,
                SimNumberPortOutPauseActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                SimNumberPortOutPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                SimNumberPortOutPauseActionId.TRUSTED_HELPER_REVIEW,
                SimNumberPortOutPauseActionId.OFFICIAL_HELP_DIRECTORY,
                SimNumberPortOutPauseActionId.REPORT_PREP
            )
            assertTrue(coach.recommendationExplanation.contains("통신사·매장·직원·기관 신원"))
            assertTrue(
                coach.recommendationExplanation.contains(
                    "전화·브라우저·문자·신고 제출을 하지 않습니다"
                )
            )
            assertNoPrivateValues(
                coach,
                "CARRIER_VALUE",
                "STORE_VALUE",
                "STAFF_VALUE",
                "AGENCY_VALUE",
                "OFFICIAL_NUMBER_VALUE",
                "DOCUMENT_VALUE",
                "CASE_VALUE",
                "REPORT_VALUE",
                "RECEIPT_VALUE"
            )
            listOf(
                "통신사를 확인했습니다",
                "직원 신원을 확인했습니다",
                "공식 번호를 찾았습니다",
                "문서를 검증했습니다",
                "사건을 조회했습니다",
                "전화를 걸었습니다",
                "신고를 제출했습니다"
            ).forEach { claim -> assertFalse(allCopy(coach).contains(claim)) }
        }
    }

    @Test
    fun liveCallAppLinkAndSmishingLeaveSameFlowWithoutTechOrTelephonyClaims() {
        listOf(
            ScamSignal.LIVE_CALL_CONTROL,
            ScamSignal.APP_INSTALL,
            ScamSignal.LINK,
            ScamSignal.PARCEL_INVITATION_SMISHING
        ).forEach { signal ->
            val coach = SimNumberPortOutPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "CALL_STATE APP_VALUE PACKAGE_VALUE DEVICE_VALUE SETTINGS_VALUE " +
                            "URL_VALUE DOMAIN_VALUE TELEPHONY_STATE SIM_STATE SUBSCRIPTION_STATE"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertRoutes(
                coach,
                SimNumberPortOutPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                SimNumberPortOutPauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH,
                SimNumberPortOutPauseActionId.REMOTE_ACCESS_CUTOFF_COACH,
                SimNumberPortOutPauseActionId.ACCOUNT_LOCKDOWN,
                SimNumberPortOutPauseActionId.EXPOSURE_IMPACT_TRIAGE,
                SimNumberPortOutPauseActionId.PRIVACY_CENTER
            )
            assertTrue(coach.recommendationExplanation.contains("통화 상태·앱·패키지·기기"))
            assertTrue(coach.recommendationExplanation.contains("통신·SIM 상태를 읽거나"))
            assertTrue(coach.recommendationExplanation.contains("원격 종료·기기 정리를 하지 않습니다"))
            assertNoPrivateValues(
                coach,
                "CALL_STATE",
                "APP_VALUE",
                "PACKAGE_VALUE",
                "DEVICE_VALUE",
                "SETTINGS_VALUE",
                "URL_VALUE",
                "DOMAIN_VALUE",
                "TELEPHONY_STATE",
                "SIM_STATE",
                "SUBSCRIPTION_STATE"
            )
            listOf(
                "통화를 종료했습니다",
                "앱을 제거했습니다",
                "설정을 변경했습니다",
                "원격 연결을 종료했습니다",
                "기기를 정리했습니다",
                "SIM 상태를 확인했습니다",
                "전화번호를 검증했습니다"
            ).forEach { claim -> assertFalse(allCopy(coach).contains(claim)) }
        }
    }

    @Test
    fun financialAndRecoverySignalsRouteToPauseRealityCheckAndManualHelpOnly() {
        listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.BAIT,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.OVERSEAS_PAYMENT_ALERT,
            ScamSignal.GIFTICON_PIN
        ).forEach { signal ->
            val coach = SimNumberPortOutPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "AMOUNT_VALUE ACCOUNT_VALUE BANK_VALUE CARD_VALUE WALLET_VALUE " +
                            "PAYMENT_VALUE REFUND_VALUE CHARGEBACK_VALUE RECOVERY_VALUE " +
                            "FREEZE_VALUE CANCEL_VALUE BLOCK_VALUE TRACE_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertRoutes(
                coach,
                SimNumberPortOutPauseActionId.MONEY_TRANSFER_PAUSE_GATE,
                SimNumberPortOutPauseActionId.GIFT_CARD_CODE_PAUSE_COACH,
                SimNumberPortOutPauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH,
                SimNumberPortOutPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH,
                SimNumberPortOutPauseActionId.ACCOUNT_LOCKDOWN,
                SimNumberPortOutPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                SimNumberPortOutPauseActionId.TRUSTED_HELPER_REVIEW,
                SimNumberPortOutPauseActionId.OFFICIAL_HELP_DIRECTORY,
                SimNumberPortOutPauseActionId.REPORT_PREP
            )
            assertTrue(coach.recommendationExplanation.contains("결제·은행"))
            assertTrue(coach.recommendationExplanation.contains("번호이동·재발급 행동을 수행하지"))
            assertNoPrivateValues(
                coach,
                "AMOUNT_VALUE",
                "ACCOUNT_VALUE",
                "BANK_VALUE",
                "CARD_VALUE",
                "WALLET_VALUE",
                "PAYMENT_VALUE",
                "REFUND_VALUE",
                "CHARGEBACK_VALUE",
                "RECOVERY_VALUE",
                "FREEZE_VALUE",
                "CANCEL_VALUE",
                "BLOCK_VALUE",
                "TRACE_VALUE"
            )
            listOf(
                "결제를 취소했습니다",
                "계정을 동결했습니다",
                "환급했습니다",
                "차지백했습니다",
                "자금을 회복했습니다",
                "번호이동을 취소했습니다"
            ).forEach { claim -> assertFalse(allCopy(coach).contains(claim)) }
        }
    }

    @Test
    fun savedIncidentUsesOnlyClampedPresenceCategory() {
        val baseAnalysis = analysis(RiskLevel.MEDIUM, ScamSignal.LIVE_CALL_CONTROL)
        val noIncident = SimNumberPortOutPauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 0
        )
        val negativeIncident = SimNumberPortOutPauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = -41
        )
        val oneIncident = SimNumberPortOutPauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 1
        )
        val manyIncidents = SimNumberPortOutPauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = Int.MAX_VALUE
        )

        assertEquals(noIncident, negativeIncident)
        assertEquals(oneIncident, manyIncidents)
        assertNotEquals(noIncident.recommendedActionIds, oneIncident.recommendedActionIds)
        assertTrue(
            SimNumberPortOutPauseCardId.CHOOSE_HELP_REPORT_AND_FOLLOW_UP_ROUTES in
                oneIncident.recommendedCardIds
        )
        assertRoutes(
            oneIncident,
            SimNumberPortOutPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
            SimNumberPortOutPauseActionId.EVIDENCE_KIT,
            SimNumberPortOutPauseActionId.INCIDENT_TIMELINE_BRIEFING,
            SimNumberPortOutPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER
        )
        assertTrue(oneIncident.recommendationExplanation.contains("개수 범주만"))
        assertTrue(oneIncident.recommendationExplanation.contains("사건 내용이나 미리보기를 읽지"))
    }

    @Test
    fun readinessUsesOnlyLevelAndReadyNeverMakesNumberSimCarrierAccountOrCodeSafe() {
        listOf(ReadinessLevel.LOW, ReadinessLevel.IN_PROGRESS).forEach { level ->
            val coach = SimNumberPortOutPauseCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
                readiness(level, "READINESS_PRIVATE_LITERAL")
            )

            assertRoutes(
                coach,
                SimNumberPortOutPauseActionId.SAFETY_PLAN,
                SimNumberPortOutPauseActionId.FAMILY_SAFE_WORD_RESET_COACH,
                SimNumberPortOutPauseActionId.FAMILY_READINESS_ROADMAP,
                SimNumberPortOutPauseActionId.FAMILY_SAFETY_DRILL_COACH,
                SimNumberPortOutPauseActionId.SAFE_CALL_SCRIPTS
            )
            assertFalse(allCopy(coach).contains("READINESS_PRIVATE_LITERAL"))
        }

        val readyCoach = SimNumberPortOutPauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
            readiness(ReadinessLevel.READY, "READY_PRIVATE_LITERAL")
        )
        listOf(
            "전화번호",
            "SIM/eSIM/USIM",
            "통신사",
            "매장",
            "계정",
            "요청",
            "코드",
            "안전",
            "신뢰",
            "공식",
            "아닙니다"
        ).forEach { token ->
            assertTrue(
                "ready boundary omitted $token",
                readyCoach.recommendationExplanation.contains(token)
            )
        }
        assertFalse(allCopy(readyCoach).contains("READY_PRIVATE_LITERAL"))
    }

    @Test
    fun cardsAreCompleteStableAndUseUniqueDeclaredRouteActions() {
        val coach = SimNumberPortOutPauseCoachBuilder.build(
            null,
            readiness(ReadinessLevel.READY)
        )

        assertEquals(SimNumberPortOutPauseCardId.entries.size, coach.cards.size)
        assertEquals(SimNumberPortOutPauseCardId.entries, coach.cards.map { card -> card.id })
        assertEquals(
            (1..SimNumberPortOutPauseCardId.entries.size).toList(),
            coach.cards.map { card -> card.displayOrder }
        )
        coach.cards.forEach { card ->
            assertEquals(card.id.displayOrder, card.displayOrder)
            assertTrue(card.title.isNotBlank())
            assertTrue(card.simNumberPortPressureSituation.isNotBlank())
            assertTrue(card.valueFreePauseScript.isNotBlank())
            assertTrue(card.whyItMatters.isNotBlank())
            assertTrue(
                card
                    .doNotShareCodePortReissueCallSmsContactAccountDeviceSayShareAttachOrRecordBoundaries
                    .size >= 2
            )
            assertTrue(
                card
                    .doNotShareCodePortReissueCallSmsContactAccountDeviceSayShareAttachOrRecordBoundaries
                    .all { boundary -> boundary.isNotBlank() }
            )
            assertTrue(card.primaryAction.label.isNotBlank())
            assertTrue(card.secondaryActions.isNotEmpty())
            assertEquals(
                card.routeActions.size,
                card.routeActions.map { action -> action.id }.distinct().size
            )
            assertTrue(
                card.routeActions.all { action ->
                    action.id in SimNumberPortOutPauseActionId.entries
                }
            )
        }
        assertEquals(
            coach.recommendedActions.size,
            coach.recommendedActions.map { action -> action.id }.distinct().size
        )
        val allActions = SimNumberPortOutPauseCoachBuilder.build(
            analysis(RiskLevel.HIGH, ScamSignal.SECRECY),
            readiness(ReadinessLevel.READY)
        ).recommendedActions
        assertEquals(SimNumberPortOutPauseActionId.entries, allActions.map { action -> action.id })
        assertEquals(allActions.size, allActions.map { action -> action.label }.distinct().size)
    }

    @Test
    fun everyActionIdMapsToAnExistingMainActivityFlow() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val routerSource = functionBlock(
            activitySource,
            "private fun openSimNumberPortOutPauseAction"
        )
        val normalizedRouter = routerSource.replace(Regex("\\s+"), " ")
        val expectedActionRoutes = linkedMapOf(
            "QUICK_STOP" to "show(Screen.QUICK_STOP)",
            "CHECKER" to "show(Screen.CHECKER)",
            "ANALYSIS_RESULT" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            "SAFETY_PLAN" to "show(Screen.PLAN)",
            "FAMILY_READINESS_ROADMAP" to "show(Screen.FAMILY_READINESS_ROADMAP)",
            "FAMILY_SAFETY_DRILL_COACH" to "openDrill()",
            "SAFE_PASTE_PRIVACY_COACH" to "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            "ACTIVE_CONTACT_EXIT_SCRIPTS" to "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "FAMILY_SAFE_WORD_RESET_COACH" to "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            "SEPARATE_CHANNEL_VERIFICATION_COACH" to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "VOICE_IMPERSONATION_REALITY_CHECK_COACH" to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            "NEW_NUMBER_QUARANTINE_COACH" to "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            "CREDENTIAL_SECRET_FREEZE_COACH" to
                "show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "VERIFICATION_OUTCOME_NEXT_STEPS_BOARD" to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            "RECONTACT_BOUNDARY_COACH" to "show(Screen.RECONTACT_BOUNDARY_COACH)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "FAMILY_RESPONSE_ROLE_BOARD" to "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            "FAMILY_CALM_DEBRIEF_AGENDA" to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            "TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH" to
                "show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)",
            "MONEY_TRANSFER_PAUSE_GATE" to "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            "GIFT_CARD_CODE_PAUSE_COACH" to "show(Screen.GIFT_CARD_CODE_PAUSE_COACH)",
            "LINK_APP_INSTALL_QUARANTINE_COACH" to
                "show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)",
            "OFFICIAL_PRESSURE_REALITY_CHECK_COACH" to
                "show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)",
            "RECOVERY_PROMISE_REALITY_CHECK_COACH" to
                "show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "REMOTE_ACCESS_CUTOFF_COACH" to "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "INCIDENT_TIMELINE_BRIEFING" to "show(Screen.TIMELINE_BRIEFING)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(SimNumberPortOutPauseActionId.entries.size, expectedActionRoutes.size)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "action did not map to existing flow: $actionId",
                normalizedRouter.contains(
                    "SimNumberPortOutPauseActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            SimNumberPortOutPauseActionId.entries.size,
            Regex("SimNumberPortOutPauseActionId\\.[A-Z_]+\\s*->")
                .findAll(routerSource)
                .count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(routerSource).count())
    }

    @Test
    fun familyShareSummaryIsRebuiltOnlyFromGenericFixedCardAndRouteLabels() {
        val coach = SimNumberPortOutPauseCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.CREDENTIALS,
                ScamSignal.PHONE_NUMBER_CHANGE,
                ScamSignal.IMPERSONATION,
                ScamSignal.APP_INSTALL,
                ScamSignal.URGENT_TRANSFER,
                input =
                    "RAW_INPUT REDACTED_CLUE PHONE_VALUE NEW_NUMBER_VALUE OLD_NUMBER_VALUE " +
                        "CHANNEL_VALUE PROFILE_VALUE CARRIER_VALUE STORE_VALUE SIM_VALUE ESIM_VALUE " +
                        "USIM_VALUE IMSI_VALUE ICCID_VALUE IMEI_VALUE SERIAL_VALUE DEVICE_ID_VALUE " +
                        "ACCOUNT_ID CUSTOMER_NUMBER PORTING_PIN OTP_VALUE PIN_VALUE PASSWORD_VALUE " +
                        "SECURITY_ANSWER URL_VALUE DOMAIN_VALUE APP_VALUE PACKAGE_VALUE FILE_VALUE " +
                        "MEDIA_VALUE URI_VALUE PATH_VALUE QR_VALUE BARCODE_VALUE CODE_VALUE " +
                        "ACCOUNT_NUMBER CARD_NUMBER BANK_VALUE WALLET_VALUE TRANSACTION_VALUE " +
                        "PAYMENT_VALUE AMOUNT_VALUE CASH_SERIAL OFFICIAL_NUMBER DIRECTORY_VALUE " +
                        "AGENCY_NAME HELPER_NAME LAWYER_NAME INVESTIGATOR_NAME STAFF_NAME CASE_ID " +
                        "REPORT_ID SUBMISSION_STATUS RECEIPT_ID SCREENSHOT_VALUE IMAGE_VALUE " +
                        "OCR_VALUE AUDIO_VALUE TRANSCRIPT_VALUE CLIPBOARD_VALUE AUTOFILL_VALUE " +
                        "ACCOUNT_STATE LOGIN_STATE CALL_STATE SMS_STATE NOTIFICATION_STATE " +
                        "RECIPIENT_NAME VERIFICATION_RESULT RESULT_STATUS PORTED_STATUS " +
                        "REISSUED_STATUS ACTIVATED_STATUS DEACTIVATED_STATUS BLOCKED_STATUS " +
                        "UNBLOCKED_STATUS CANCELLED_STATUS RECOVERED_STATUS FROZEN_STATUS " +
                        "CHANGED_STATUS UPDATED_STATUS SAVED_STATUS REPLACED_STATUS " +
                        "TRANSFERRED_STATUS PAYMENT_STATUS REFUND_STATUS CHARGEBACK_STATUS " +
                        "LEGAL_OUTCOME FINANCIAL_OUTCOME 010-9876-5432 1234567890123 secret.example"
            ),
            readiness = readiness(
                ReadinessLevel.IN_PROGRESS,
                "PLAN_LITERAL SAFE_WORD_LITERAL CALLBACK_LITERAL CONTACT_LITERAL"
            ),
            incidentCount = 7
        )
        val summary = coach.familyShareSummary
        val cardsById = coach.cards.associateBy { card -> card.id }
        val expected = buildString {
            appendLine("[SIM·번호이동 멈춤 코치]")
            appendLine("민감한 값을 말하지 않고 통신·번호이동 압박을 멈춘 채 직접 읽을 문장을 고르는 로컬 수동 안내입니다.")
            appendLine()
            appendLine("추천 값 없는 통신·번호이동 멈춤 카드")
            coach.recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            coach.recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 실제 전화번호·SIM/eSIM/USIM·통신사·계정·인증코드·기기·연락처·링크·결제 값은 보내지 않습니다.")
            append("- 이 안내는 번호이동·재발급·보호·복구·차단·확인·신고·계정·기기 행동을 수행하거나 결과를 보장하지 않습니다.")
        }
        assertEquals(expected, summary)

        listOf(
            "RAW_INPUT",
            "REDACTED_CLUE",
            "PHONE_VALUE",
            "NEW_NUMBER_VALUE",
            "OLD_NUMBER_VALUE",
            "CHANNEL_VALUE",
            "PROFILE_VALUE",
            "CARRIER_VALUE",
            "STORE_VALUE",
            "SIM_VALUE",
            "ESIM_VALUE",
            "USIM_VALUE",
            "IMSI_VALUE",
            "ICCID_VALUE",
            "IMEI_VALUE",
            "SERIAL_VALUE",
            "DEVICE_ID_VALUE",
            "ACCOUNT_ID",
            "CUSTOMER_NUMBER",
            "PORTING_PIN",
            "OTP_VALUE",
            "PIN_VALUE",
            "PASSWORD_VALUE",
            "SECURITY_ANSWER",
            "URL_VALUE",
            "DOMAIN_VALUE",
            "APP_VALUE",
            "PACKAGE_VALUE",
            "FILE_VALUE",
            "MEDIA_VALUE",
            "URI_VALUE",
            "PATH_VALUE",
            "QR_VALUE",
            "BARCODE_VALUE",
            "CODE_VALUE",
            "ACCOUNT_NUMBER",
            "CARD_NUMBER",
            "BANK_VALUE",
            "WALLET_VALUE",
            "TRANSACTION_VALUE",
            "PAYMENT_VALUE",
            "AMOUNT_VALUE",
            "CASH_SERIAL",
            "OFFICIAL_NUMBER",
            "DIRECTORY_VALUE",
            "AGENCY_NAME",
            "HELPER_NAME",
            "LAWYER_NAME",
            "INVESTIGATOR_NAME",
            "STAFF_NAME",
            "CASE_ID",
            "REPORT_ID",
            "SUBMISSION_STATUS",
            "RECEIPT_ID",
            "SCREENSHOT_VALUE",
            "IMAGE_VALUE",
            "OCR_VALUE",
            "AUDIO_VALUE",
            "TRANSCRIPT_VALUE",
            "CLIPBOARD_VALUE",
            "AUTOFILL_VALUE",
            "ACCOUNT_STATE",
            "LOGIN_STATE",
            "CALL_STATE",
            "SMS_STATE",
            "NOTIFICATION_STATE",
            "RECIPIENT_NAME",
            "VERIFICATION_RESULT",
            "RESULT_STATUS",
            "PORTED_STATUS",
            "REISSUED_STATUS",
            "ACTIVATED_STATUS",
            "DEACTIVATED_STATUS",
            "BLOCKED_STATUS",
            "UNBLOCKED_STATUS",
            "CANCELLED_STATUS",
            "RECOVERED_STATUS",
            "FROZEN_STATUS",
            "CHANGED_STATUS",
            "UPDATED_STATUS",
            "SAVED_STATUS",
            "REPLACED_STATUS",
            "TRANSFERRED_STATUS",
            "PAYMENT_STATUS",
            "REFUND_STATUS",
            "CHARGEBACK_STATUS",
            "LEGAL_OUTCOME",
            "FINANCIAL_OUTCOME",
            "PLAN_LITERAL",
            "SAFE_WORD_LITERAL",
            "CALLBACK_LITERAL",
            "CONTACT_LITERAL"
        ).forEach { privateValue ->
            assertFalse("share leaked $privateValue", summary.contains(privateValue))
        }
        assertFalse(Regex("(?<!\\d)\\d{7,}(?!\\d)").containsMatchIn(summary))
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
    fun builderIsDeterministicAndIgnoresRawAnalysisScoreAndReadinessFields() {
        val first = SimNumberPortOutPauseCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.CREDENTIALS,
                ScamSignal.APP_INSTALL,
                input = "FIRST_RAW_VALUE",
                score = 1
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "FIRST_PLAN_VALUE"),
            incidentCount = 2
        )
        val second = SimNumberPortOutPauseCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.APP_INSTALL,
                ScamSignal.CREDENTIALS,
                input = "SECOND_RAW_VALUE",
                score = 999
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "SECOND_PLAN_VALUE"),
            incidentCount = 82
        )

        assertEquals(first, second)
        assertEquals(
            first,
            SimNumberPortOutPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.HIGH,
                    ScamSignal.CREDENTIALS,
                    ScamSignal.APP_INSTALL,
                    input = "FIRST_RAW_VALUE",
                    score = 1
                ),
                readiness = readiness(ReadinessLevel.IN_PROGRESS, "FIRST_PLAN_VALUE"),
                incidentCount = 2
            )
        )
        assertNoPrivateValues(
            first,
            "FIRST_RAW_VALUE",
            "SECOND_RAW_VALUE",
            "FIRST_PLAN_VALUE",
            "SECOND_PLAN_VALUE"
        )
    }

    @Test
    fun privacyCopyStatesManualLocalStatelessAndEveryNoTelecomBoundary() {
        val copy = allCopy(
            SimNumberPortOutPauseCoachBuilder.build(
                null,
                readiness(ReadinessLevel.READY)
            )
        )

        listOf(
            "로컬 전용·무상태 수동 안내",
            "민감한 값을 입력하라고 요구하지 않고",
            "새 저장소·자동 처리·자동 전송·스케줄러를 만들지 않습니다",
            "위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용",
            "원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기",
            "전화번호·새 번호·이전 번호·채널·프로필·통신사·매장·SIM/eSIM/USIM",
            "IMSI·ICCID·IMEI·기기 일련값·기기 ID",
            "번호이동용 PIN·OTP·PIN·비밀번호·보안 답",
            "전화 통신·SIM·구독·통신사 API",
            "전화번호를 검증",
            "번호이동·개통·해지·재발급·명의변경·교체·활성화·비활성화·차단·해제 상태",
            "전화·다이얼·발신·통화·종료·선별·차단·통화 상태·통화 기록·발신자 표시",
            "문자·SMS·받는 사람 선택",
            "연락처·주소록·알림을 읽기·쓰기·편집·선택·감시·필터링하지 않습니다",
            "네트워크·웹 탐색·브라우저·웹뷰",
            "공식/통신사 정보·번호·디렉터리·매장·직원·신원·문서·사건",
            "전화·문자·신고·출동을 요청·제출하지 않습니다",
            "QR·바코드·OCR·카메라·이미지 분석·화면 캡처",
            "파일·문서·사진·스크린샷·오디오·미디어·저장소",
            "첨부·스트림·URI 공유·업로드·동기화하지 않습니다",
            "클립보드·텍스트 감시기·자동완성·자격 증명 API",
            "계정·로그인·인증·생체 인증·키가드·키 저장소·암호화 API",
            "결제·은행·ATM·NFC·지갑·블록체인·상품권 동작",
            "환급·차지백·취소·동결·차단·추적·회복을 수행하지 않습니다",
            "앱·패키지·설치 앱 목록·앱 이름·권한·기기·설정 상태",
            "설치·제거·비활성화·강제 종료·권한 허용·철회·설정 변경",
            "IMEI·ICCID·IMSI·기기 ID·일련값을 읽지 않고",
            "기기 관리자·접근성 서비스·VPN·사용 통계·알림 수신 서비스",
            "파일·스트림·URI를 붙이거나 자동 발송하지 않습니다",
            "새 Android 권한",
            "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러·브로드캐스트 수신기",
            "가족 계획·사건·공식 디렉터리·연락처·알림·통화·문자·통신·SIM·구독·통신사·번호·계정·결제·앱·패키지·권한·기기·설정·자격·신고·확인·결과 상태를 수정하지 않습니다"
        ).forEach { boundary ->
            assertTrue("privacy copy omitted $boundary", copy.contains(boundary))
        }
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndAutomaticProtectionClaims() {
        val copy = allCopy(
            SimNumberPortOutPauseCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.READY),
                incidentCount = 1
            )
        )

        listOf(
            "official number",
            "verified carrier",
            "safe SIM",
            "trusted store",
            "port completed",
            "SIM reissued",
            "account protected",
            "refund guaranteed",
            "report submitted",
            "problem-free",
            "공식 번호입니다",
            "검증된 통신사입니다",
            "안전한 SIM입니다",
            "신뢰할 수 있는 매장입니다",
            "번호이동이 완료됐습니다",
            "재발급됐습니다",
            "개통됐습니다",
            "해지됐습니다",
            "차단됐습니다",
            "해제됐습니다",
            "취소됐습니다",
            "교체됐습니다",
            "변경됐습니다",
            "업데이트됐습니다",
            "저장됐습니다",
            "복구됐습니다",
            "환급됐습니다",
            "계정이 보호됐습니다",
            "신고가 제출됐습니다",
            "문제없습니다",
            "자동 번호 확인",
            "자동 SIM 확인",
            "자동 통신사 조회",
            "자동 번호이동 차단",
            "automatic monitoring",
            "automatic lookup",
            "automatic verification",
            "automatic blocking",
            "automatic recovery",
            "automatic storage",
            "automatic upload",
            "automatic submission",
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
            "app/src/main/java/com/mongmong/familyshield/SimNumberPortOutPauseCoach.kt"
        )

        assertFalse(Regex("(?m)^\\s*import\\s+android(?:x)?\\.").containsMatchIn(source))
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

        val implementationSource = stripStringLiterals(source)
        listOf(
            "Context",
            "Intent",
            "SharedPreferences",
            "SafetyPlan",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "OfficialHelpDirectoryBuilder",
            "TelephonyManager",
            "SubscriptionManager",
            "CarrierConfigManager",
            "TelecomManager",
            "SmsManager",
            "CallLog",
            "ContactsContract",
            "PhoneNumberUtils",
            "NotificationListenerService",
            "HttpURLConnection",
            "java.net.URL",
            "WebView",
            "Uri.parse",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "KeyStore",
            "javax.crypto",
            "FileProvider",
            "MediaStore",
            "PackageManager",
            "PackageInstaller",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "BillingClient",
            "NfcAdapter",
            "PaymentClient",
            "BankingClient",
            "RefundClient",
            "ChargebackClient",
            "AlarmManager",
            "WorkManager",
            "JobScheduler",
            "CalendarContract",
            "BroadcastReceiver",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            ".save("
        ).forEach { forbiddenSource ->
            assertFalse(
                "SIM/port model used forbidden implementation source: $forbiddenSource",
                implementationSource.contains(forbiddenSource, ignoreCase = true)
            )
        }
    }

    @Test
    fun boundedUiSliceUsesOnlyCountReadinessRoutesAndManualTextShare() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val screenSource = functionBlock(
            activitySource,
            "private fun LinearLayout.simNumberPortOutPauseCoachScreen"
        )
        val cardSource = functionBlock(
            activitySource,
            "private fun LinearLayout.simNumberPortOutPauseCard"
        )
        val routerSource = functionBlock(
            activitySource,
            "private fun openSimNumberPortOutPauseAction"
        )
        val shareSource = functionBlock(
            activitySource,
            "private fun shareSimNumberPortOutPauseCoach"
        )

        assertTrue(screenSource.contains("incidents.count()"))
        assertTrue(screenSource.contains("FamilyReadinessBuilder.build(safetyPlan.load())"))
        assertFalse(screenSource.contains("incidents.load"))
        assertFalse(screenSource.contains("currentAnalysis?.input"))
        assertFalse(screenSource.contains("currentAnalysis?.score"))
        assertFalse(screenSource.contains("currentAnalysis?.actionPlan"))
        assertFalse(screenSource.contains("currentAnalysis?.summary"))
        assertFalse(screenSource.contains("IncidentPreviewRedactor"))
        listOf(
            "card.displayOrder",
            "card.title",
            "card.simNumberPortPressureSituation",
            "card.valueFreePauseScript",
            "card.whyItMatters",
            "card.doNotShareCodePortReissueCallSmsContactAccountDeviceSayShareAttachOrRecordBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedField -> assertTrue(cardSource.contains(renderedField)) }

        assertTrue(screenSource.contains("shareSimNumberPortOutPauseCoach(coach.familyShareSummary)"))
        assertTrue(shareSource.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareSource.contains("type = \"text/plain\""))
        assertTrue(shareSource.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertFalse(shareSource.contains("Intent.EXTRA_STREAM"))
        assertFalse(shareSource.contains("Intent.ACTION_VIEW"))
        assertFalse(shareSource.contains("Uri.parse"))

        val boundedImplementationSource = stripStringLiterals(
            screenSource + cardSource + routerSource + shareSource
        )
        listOf(
            "TelephonyManager",
            "SubscriptionManager",
            "CarrierConfigManager",
            "TelecomManager",
            "SmsManager",
            "CallLog",
            "ContactsContract",
            "PhoneNumberUtils",
            "PackageManager",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
            "WebView",
            "Intent.ACTION_VIEW",
            "Intent.ACTION_DIAL",
            "Intent.ACTION_CALL",
            "ACTION_SENDTO",
            "Intent.EXTRA_STREAM",
            "ClipboardManager",
            "CredentialManager",
            "AccountManager",
            "FileProvider",
            "MediaStore",
            "prefs.edit(",
            "safetyPlan.save(",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear("
        ).forEach { forbiddenApi ->
            assertFalse(
                "bounded SIM/port UI used forbidden implementation: $forbiddenApi",
                boundedImplementationSource.contains(forbiddenApi, ignoreCase = true)
            )
        }
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(routerSource).count())
        assertFalse((screenSource + cardSource).contains("openHelpDirectory("))
    }

    @Test
    fun highRiskShowsAllCardsAndAllExistingRoutes() {
        val coach = SimNumberPortOutPauseCoachBuilder.build(
            analysis(RiskLevel.HIGH, ScamSignal.SECRECY),
            readiness(ReadinessLevel.READY)
        )

        assertEquals(SimNumberPortOutPauseCardId.entries, coach.recommendedCardIds)
        assertEquals(SimNumberPortOutPauseActionId.entries, coach.recommendedActionIds)
        assertEquals(
            SimNumberPortOutPauseActionId.entries.size,
            coach.recommendedActions.map { action -> action.label }.distinct().size
        )
    }

    @Test
    fun unrelatedSignalCategoriesProduceDistinctDeterministicRecommendations() {
        fun coachFor(signal: ScamSignal) = SimNumberPortOutPauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, signal),
            readiness(ReadinessLevel.READY)
        )

        val credential = coachFor(ScamSignal.CREDENTIALS)
        val family = coachFor(ScamSignal.PHONE_NUMBER_CHANGE)
        val official = coachFor(ScamSignal.IMPERSONATION)
        val liveApp = coachFor(ScamSignal.APP_INSTALL)
        val financial = coachFor(ScamSignal.URGENT_TRANSFER)

        assertNotEquals(credential.recommendedActionIds, family.recommendedActionIds)
        assertNotEquals(family.recommendedActionIds, official.recommendedActionIds)
        assertNotEquals(official.recommendedActionIds, liveApp.recommendedActionIds)
        assertNotEquals(liveApp.recommendedActionIds, financial.recommendedActionIds)
        assertNotEquals(credential.recommendationTitle, family.recommendationTitle)
        assertNotEquals(family.recommendationTitle, official.recommendationTitle)
        assertTrue(
            credential.cards.all { card ->
                card.simNumberPortPressureSituation.endsWith("때")
            }
        )
    }

    private fun assertRoutes(
        coach: SimNumberPortOutPauseCoach,
        vararg expected: SimNumberPortOutPauseActionId
    ) {
        assertTrue(coach.recommendedActionIds.containsAll(expected.toList()))
    }

    private fun assertNoPrivateValues(
        coach: SimNumberPortOutPauseCoach,
        vararg privateValues: String
    ) {
        val copy = allCopy(coach)
        privateValues.forEach { privateValue ->
            assertFalse("coach leaked $privateValue", copy.contains(privateValue))
        }
    }

    private fun analysis(
        riskLevel: RiskLevel,
        vararg signals: ScamSignal,
        input: String = "PRIVATE_ANALYSIS_INPUT",
        score: Int = 73
    ): ScamAnalysis = ScamAnalysis(
        input = input,
        score = score,
        riskLevel = riskLevel,
        matchedSignals = signals.toList()
    )

    private fun readiness(
        level: ReadinessLevel,
        privateLiteral: String = "PRIVATE_READINESS_LITERAL"
    ): FamilyReadiness = FamilyReadiness(
        level = level,
        title = "TITLE_$privateLiteral",
        completedCount = 919,
        totalCount = 937,
        checklist = listOf(
            ReadinessItem(
                label = "LABEL_$privateLiteral",
                done = true,
                action = "ACTION_$privateLiteral"
            )
        ),
        nextAction = "NEXT_$privateLiteral"
    )

    private fun allCopy(coach: SimNumberPortOutPauseCoach): String = buildString {
        appendLine(coach.title)
        appendLine(coach.intro)
        appendLine(coach.modeLabel)
        appendLine(coach.recommendationTitle)
        appendLine(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> appendLine(action.label) }
        coach.cards.forEach { card ->
            appendLine(card.title)
            appendLine(card.simNumberPortPressureSituation)
            appendLine(card.valueFreePauseScript)
            appendLine(card.whyItMatters)
            card
                .doNotShareCodePortReissueCallSmsContactAccountDeviceSayShareAttachOrRecordBoundaries
                .forEach(::appendLine)
            card.routeActions.forEach { action -> appendLine(action.label) }
        }
        coach.privacySafetyNotes.forEach(::appendLine)
        append(coach.familyShareSummary)
    }

    private fun stripStringLiterals(source: String): String = source.replace(
        Regex("\"(?:\\\\.|[^\"\\\\])*\""),
        "\"\""
    )

    private fun functionBlock(source: String, marker: String): String {
        val start = source.indexOf(marker)
        require(start >= 0) { "Source marker missing: $marker" }
        val openingBrace = source.indexOf('{', start)
        require(openingBrace >= 0) { "Opening brace missing after: $marker" }
        var depth = 0
        for (index in openingBrace until source.length) {
            when (source[index]) {
                '{' -> depth += 1
                '}' -> {
                    depth -= 1
                    if (depth == 0) return source.substring(start, index + 1)
                }
            }
        }
        error("Closing brace missing after: $marker")
    }

    private fun readProjectFile(relativePath: String): String {
        var directory = File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
        repeat(8) {
            val candidate = File(directory, relativePath)
            if (candidate.isFile) return candidate.readText()
            val parent = directory.parentFile
            if (parent != null) directory = parent
        }
        error("Could not locate project file: $relativePath")
    }
}
