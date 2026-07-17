package com.mongmong.familyshield

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CashWithdrawalCourierHandoffPauseCoachTest {
    @Test
    fun noAnalysisCoachIsConservativeAndNeverDeclaresCashCourierOrOutcomeStatus() {
        val coach = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(CashWithdrawalCourierHandoffPauseCardId.entries, coach.recommendedCardIds)
        assertEquals(
            CashWithdrawalCourierHandoffPauseActionId.entries.filterNot { actionId ->
                actionId == CashWithdrawalCourierHandoffPauseActionId.ANALYSIS_RESULT
            },
            coach.recommendedActionIds
        )
        assertTrue(coach.recommendationTitle.contains("모든 현금·대면 전달 멈춤 카드"))
        assertTrue(coach.recommendationExplanation.contains("상태를 단정하지 말고"))

        val copy = allCopy(coach)
        listOf(
            "official courier",
            "verified investigator",
            "safe handoff",
            "bank verified",
            "cash protected",
            "case accepted",
            "report submitted",
            "report accepted",
            "report received",
            "delivered successfully",
            "collected successfully",
            "deposited successfully",
            "meeting scheduled",
            "payment cancelled",
            "refund guaranteed",
            "chargeback accepted",
            "funds recovered",
            "account frozen",
            "route traced",
            "legal outcome guaranteed",
            "problem-free",
            "공식 기사입니다",
            "검증된 수사관입니다",
            "안전한 전달입니다",
            "은행 확인 완료",
            "현금이 보호됐습니다",
            "사건이 접수됐습니다",
            "신고가 제출됐습니다",
            "신고가 접수됐습니다",
            "신고가 수신됐습니다",
            "배달됐습니다",
            "수거됐습니다",
            "입금됐습니다",
            "만남이 예약됐습니다",
            "결제가 취소됐습니다",
            "환급이 보장됩니다",
            "차지백이 접수됐습니다",
            "자금이 회복됐습니다",
            "계정이 동결됐습니다",
            "경로가 추적됐습니다",
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
    fun financialSignalsRecommendMoneyPressureExitVerificationAccountEvidenceAndHelpRoutes() {
        listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.BAIT,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "CASH_AMOUNT ATM_VALUE BANK_VALUE CARD_VALUE PAYMENT_VALUE " +
                            "LOCATION_VALUE ROUTE_VALUE COURIER_VALUE OFFICIAL_NUMBER_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        CashWithdrawalCourierHandoffPauseCardId
                            .STOP_CASH_WITHDRAWAL_AND_ATM_BANK_VISIT,
                        CashWithdrawalCourierHandoffPauseCardId
                            .DO_NOT_HAND_OVER_CASH_CARD_BANKBOOK_DEVICE_OR_DOCUMENTS,
                        CashWithdrawalCourierHandoffPauseCardId
                            .DO_NOT_USE_LOCKER_PARCEL_MAIL_QUICK_OR_ENVELOPE_HANDOFF,
                        CashWithdrawalCourierHandoffPauseCardId
                            .SEPARATE_OFFICIAL_TITLE_CASE_AND_DOCUMENT_PRESSURE,
                        CashWithdrawalCourierHandoffPauseCardId
                            .VALUE_FREE_EXPOSURE_EVIDENCE_FAMILY_HELP_AND_FOLLOW_UP
                    )
                )
            )
            assertRoutes(
                coach,
                CashWithdrawalCourierHandoffPauseActionId.MONEY_TRANSFER_PAUSE_GATE,
                CashWithdrawalCourierHandoffPauseActionId
                    .OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
                CashWithdrawalCourierHandoffPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                CashWithdrawalCourierHandoffPauseActionId
                    .SEPARATE_CHANNEL_VERIFICATION_COACH,
                CashWithdrawalCourierHandoffPauseActionId.ACCOUNT_LOCKDOWN,
                CashWithdrawalCourierHandoffPauseActionId.EXPOSURE_IMPACT_TRIAGE,
                CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                CashWithdrawalCourierHandoffPauseActionId.TRUSTED_HELPER_REVIEW,
                CashWithdrawalCourierHandoffPauseActionId.OFFICIAL_HELP_DIRECTORY,
                CashWithdrawalCourierHandoffPauseActionId.REPORT_PREP
            )
            assertTrue(coach.recommendationExplanation.contains("현금 인출"))
            assertTrue(coach.recommendationExplanation.contains("공식 번호 조회를 수행하지 않습니다"))
            assertNoPrivateValues(
                coach,
                "CASH_AMOUNT",
                "ATM_VALUE",
                "BANK_VALUE",
                "CARD_VALUE",
                "PAYMENT_VALUE",
                "LOCATION_VALUE",
                "ROUTE_VALUE",
                "COURIER_VALUE",
                "OFFICIAL_NUMBER_VALUE"
            )
            listOf(
                "현금을 인출했습니다",
                "ATM을 찾았습니다",
                "은행 지점을 안내합니다",
                "카드를 정지했습니다",
                "결제를 취소했습니다",
                "환급을 신청했습니다",
                "차지백을 접수했습니다",
                "택배를 추적했습니다",
                "공식 번호를 조회했습니다"
            ).forEach { claim -> assertFalse(allCopy(coach).contains(claim)) }
        }
    }

    @Test
    fun officialPressureSignalsRecommendRealityCheckExitFamilyEvidenceAndHelpWithoutLookup() {
        listOf(
            ScamSignal.IMPERSONATION,
            ScamSignal.SECRECY,
            ScamSignal.SAFE_ACCOUNT_FRAUD
        ).forEach { signal ->
            val coach = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "AGENCY_VALUE INVESTIGATOR_VALUE CASE_VALUE DOCUMENT_VALUE " +
                            "DIRECTORY_VALUE OFFICIAL_NUMBER_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY, "PRIVATE_PLAN_VALUE")
            )

            assertRoutes(
                coach,
                CashWithdrawalCourierHandoffPauseActionId
                    .OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
                CashWithdrawalCourierHandoffPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                CashWithdrawalCourierHandoffPauseActionId
                    .SEPARATE_CHANNEL_VERIFICATION_COACH,
                CashWithdrawalCourierHandoffPauseActionId.FAMILY_ALERT_MESSAGES,
                CashWithdrawalCourierHandoffPauseActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                CashWithdrawalCourierHandoffPauseActionId.TRUSTED_HELPER_REVIEW,
                CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                CashWithdrawalCourierHandoffPauseActionId.OFFICIAL_HELP_DIRECTORY,
                CashWithdrawalCourierHandoffPauseActionId.REPORT_PREP
            )
            assertTrue(coach.recommendationExplanation.contains("공식 번호·디렉터리 값"))
            assertTrue(coach.recommendationExplanation.contains("신고 제출을 하지 않습니다"))
            assertNoPrivateValues(
                coach,
                "AGENCY_VALUE",
                "INVESTIGATOR_VALUE",
                "CASE_VALUE",
                "DOCUMENT_VALUE",
                "DIRECTORY_VALUE",
                "OFFICIAL_NUMBER_VALUE",
                "PRIVATE_PLAN_VALUE"
            )
            listOf(
                "공식 번호를 찾았습니다",
                "수사관을 확인했습니다",
                "문서를 검증했습니다",
                "사건을 조회했습니다",
                "전화를 걸었습니다",
                "문자를 보냈습니다",
                "신고를 제출했습니다"
            ).forEach { claim -> assertFalse(allCopy(coach).contains(claim)) }
        }
    }

    @Test
    fun liveCallAppLinkAndParcelSignalsRecommendExitQuarantineRemoteExposureAndPrivacy() {
        listOf(
            ScamSignal.LIVE_CALL_CONTROL,
            ScamSignal.APP_INSTALL,
            ScamSignal.LINK,
            ScamSignal.PARCEL_INVITATION_SMISHING
        ).forEach { signal ->
            val coach = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "CALL_STATE APP_VALUE PACKAGE_VALUE PERMISSION_VALUE DEVICE_VALUE " +
                            "URL_VALUE LOCATION_VALUE TRACKING_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertRoutes(
                coach,
                CashWithdrawalCourierHandoffPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                CashWithdrawalCourierHandoffPauseActionId
                    .LINK_APP_INSTALL_QUARANTINE_COACH,
                CashWithdrawalCourierHandoffPauseActionId.REMOTE_ACCESS_CUTOFF_COACH,
                CashWithdrawalCourierHandoffPauseActionId.ACCOUNT_LOCKDOWN,
                CashWithdrawalCourierHandoffPauseActionId.EXPOSURE_IMPACT_TRIAGE,
                CashWithdrawalCourierHandoffPauseActionId.PRIVACY_CENTER
            )
            assertTrue(coach.recommendationExplanation.contains("같은 연락을 수동으로 벗어나고"))
            assertTrue(coach.recommendationExplanation.contains("택배 추적"))
            assertNoPrivateValues(
                coach,
                "CALL_STATE",
                "APP_VALUE",
                "PACKAGE_VALUE",
                "PERMISSION_VALUE",
                "DEVICE_VALUE",
                "URL_VALUE",
                "LOCATION_VALUE",
                "TRACKING_VALUE"
            )
            listOf(
                "통화를 종료했습니다",
                "앱을 제거했습니다",
                "권한을 철회했습니다",
                "설정을 변경했습니다",
                "원격 연결을 종료했습니다",
                "기기를 정리했습니다",
                "위치를 확인했습니다",
                "택배를 추적했습니다"
            ).forEach { claim -> assertFalse(allCopy(coach).contains(claim)) }
        }
    }

    @Test
    fun credentialOtpAndGiftCardSignalsRecommendSecretAccountPrivacyRemoteAndExposureWithoutValues() {
        listOf(
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "OTP_VALUE PIN_VALUE PASSWORD_VALUE SECURITY_ANSWER ACCOUNT_VALUE " +
                            "CARD_VALUE BANK_VALUE WALLET_VALUE GIFT_CODE_VALUE CASH_VALUE"
                ),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "SAFE_WORD_VALUE CALLBACK_VALUE CONTACT_VALUE"
                )
            )

            assertRoutes(
                coach,
                CashWithdrawalCourierHandoffPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH,
                CashWithdrawalCourierHandoffPauseActionId.GIFT_CARD_CODE_PAUSE_COACH,
                CashWithdrawalCourierHandoffPauseActionId.ACCOUNT_LOCKDOWN,
                CashWithdrawalCourierHandoffPauseActionId.SAFE_PASTE_PRIVACY_COACH,
                CashWithdrawalCourierHandoffPauseActionId.REMOTE_ACCESS_CUTOFF_COACH,
                CashWithdrawalCourierHandoffPauseActionId.EXPOSURE_IMPACT_TRIAGE
            )
            assertTrue(coach.recommendationExplanation.contains("OTP·PIN·비밀번호"))
            assertNoPrivateValues(
                coach,
                "OTP_VALUE",
                "PIN_VALUE",
                "PASSWORD_VALUE",
                "SECURITY_ANSWER",
                "ACCOUNT_VALUE",
                "CARD_VALUE",
                "BANK_VALUE",
                "WALLET_VALUE",
                "GIFT_CODE_VALUE",
                "CASH_VALUE",
                "SAFE_WORD_VALUE",
                "CALLBACK_VALUE",
                "CONTACT_VALUE"
            )
        }
    }

    @Test
    fun recoveryPromiseSignalsUseExistingRealityCheckWithoutRefundChargebackOrRecoveryActions() {
        listOf(
            ScamSignal.BAIT,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "REFUND_VALUE CHARGEBACK_VALUE RECOVERY_VALUE FREEZE_VALUE " +
                            "TRACE_VALUE HANDOFF_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertRoutes(
                coach,
                CashWithdrawalCourierHandoffPauseActionId
                    .RECOVERY_PROMISE_REALITY_CHECK_COACH,
                CashWithdrawalCourierHandoffPauseActionId.MONEY_TRANSFER_PAUSE_GATE,
                CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                CashWithdrawalCourierHandoffPauseActionId.TRUSTED_HELPER_REVIEW,
                CashWithdrawalCourierHandoffPauseActionId.OFFICIAL_HELP_DIRECTORY,
                CashWithdrawalCourierHandoffPauseActionId.REPORT_PREP
            )
            assertTrue(coach.recommendationExplanation.contains("현금·물품 회수를 수행하거나 보장하지 않습니다"))
            assertNoPrivateValues(
                coach,
                "REFUND_VALUE",
                "CHARGEBACK_VALUE",
                "RECOVERY_VALUE",
                "FREEZE_VALUE",
                "TRACE_VALUE",
                "HANDOFF_VALUE"
            )
            listOf(
                "환급이 완료됐습니다",
                "차지백을 접수했습니다",
                "결제를 취소했습니다",
                "계정을 동결했습니다",
                "자금을 추적했습니다",
                "현금을 회수했습니다"
            ).forEach { claim -> assertFalse(allCopy(coach).contains(claim)) }
        }
    }

    @Test
    fun familyEmergencyDeepfakeAndNumberChangeUseOnlyValueFreeFamilyRoutes() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val coach = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input = "FAMILY_VALUE PHONE_VALUE CHANNEL_VALUE PROFILE_VALUE"
                ),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "CONTACT_VALUE SAFE_WORD_VALUE CALLBACK_VALUE"
                )
            )

            assertRoutes(
                coach,
                CashWithdrawalCourierHandoffPauseActionId
                    .SEPARATE_CHANNEL_VERIFICATION_COACH,
                CashWithdrawalCourierHandoffPauseActionId.FAMILY_CALLBACK_CARD,
                CashWithdrawalCourierHandoffPauseActionId
                    .VOICE_IMPERSONATION_REALITY_CHECK_COACH,
                CashWithdrawalCourierHandoffPauseActionId.NEW_NUMBER_QUARANTINE_COACH,
                CashWithdrawalCourierHandoffPauseActionId.FAMILY_SAFE_WORD_RESET_COACH,
                CashWithdrawalCourierHandoffPauseActionId.SAFE_CALL_SCRIPTS,
                CashWithdrawalCourierHandoffPauseActionId.FAMILY_ALERT_MESSAGES
            )
            assertTrue(coach.recommendationExplanation.contains("연락처·안전단어·콜백 값을 사용"))
            assertTrue(coach.recommendationExplanation.contains("통화 기록·문자를 읽지 않습니다"))
            assertNoPrivateValues(
                coach,
                "FAMILY_VALUE",
                "PHONE_VALUE",
                "CHANNEL_VALUE",
                "PROFILE_VALUE",
                "CONTACT_VALUE",
                "SAFE_WORD_VALUE",
                "CALLBACK_VALUE"
            )
        }
    }

    @Test
    fun savedIncidentUsesOnlyClampedPresenceCategory() {
        val baseAnalysis = analysis(RiskLevel.MEDIUM, ScamSignal.LIVE_CALL_CONTROL)
        val noIncident = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 0
        )
        val negativeIncident = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = -271
        )
        val oneIncident = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 1
        )
        val manyIncidents = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = Int.MAX_VALUE
        )

        assertEquals(noIncident, negativeIncident)
        assertEquals(oneIncident, manyIncidents)
        assertNotEquals(noIncident.recommendedActionIds, oneIncident.recommendedActionIds)
        assertTrue(
            CashWithdrawalCourierHandoffPauseCardId
                .VALUE_FREE_EXPOSURE_EVIDENCE_FAMILY_HELP_AND_FOLLOW_UP in
                oneIncident.recommendedCardIds
        )
        assertRoutes(
            oneIncident,
            CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
            CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_KIT,
            CashWithdrawalCourierHandoffPauseActionId.INCIDENT_TIMELINE_BRIEFING,
            CashWithdrawalCourierHandoffPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER
        )
        assertTrue(oneIncident.recommendationExplanation.contains("개수 범주만"))
        assertTrue(oneIncident.recommendationExplanation.contains("사건 내용이나 미리보기는 읽지"))
    }

    @Test
    fun readinessUsesOnlyLevelAndReadyNeverMakesCashHandoffOrRequestSafeOrOfficial() {
        listOf(ReadinessLevel.LOW, ReadinessLevel.IN_PROGRESS).forEach { level ->
            val coach = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
                readiness(level, "READINESS_PRIVATE_LITERAL")
            )

            assertRoutes(
                coach,
                CashWithdrawalCourierHandoffPauseActionId.SAFETY_PLAN,
                CashWithdrawalCourierHandoffPauseActionId.FAMILY_SAFE_WORD_RESET_COACH,
                CashWithdrawalCourierHandoffPauseActionId.FAMILY_READINESS_ROADMAP,
                CashWithdrawalCourierHandoffPauseActionId.FAMILY_SAFETY_DRILL_COACH,
                CashWithdrawalCourierHandoffPauseActionId.SAFE_CALL_SCRIPTS
            )
            assertFalse(allCopy(coach).contains("READINESS_PRIVATE_LITERAL"))
        }

        val readyCoach = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
            readiness(ReadinessLevel.READY, "READY_PRIVATE_LITERAL")
        )
        assertTrue(
            readyCoach.recommendationExplanation.contains(
                "현금 인출·ATM·은행 방문·이동·만남·택배/퀵·보관함·현금·카드·통장·기기·" +
                    "문서 전달 요청이 실제이거나 안전하거나 신뢰할 수 있거나 공식이라는 뜻이 아닙니다"
            )
        )
        assertFalse(allCopy(readyCoach).contains("READY_PRIVATE_LITERAL"))
    }

    @Test
    fun cardsAreCompleteStableAndUseUniqueDeclaredRouteActions() {
        val coach = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            null,
            readiness(ReadinessLevel.READY)
        )

        assertEquals(CashWithdrawalCourierHandoffPauseCardId.entries.size, coach.cards.size)
        assertEquals(
            CashWithdrawalCourierHandoffPauseCardId.entries,
            coach.cards.map { card -> card.id }
        )
        assertEquals(
            (1..CashWithdrawalCourierHandoffPauseCardId.entries.size).toList(),
            coach.cards.map { card -> card.displayOrder }
        )
        coach.cards.forEach { card ->
            assertEquals(card.id.displayOrder, card.displayOrder)
            assertTrue(card.title.isNotBlank())
            assertTrue(card.cashCourierPressureScenario.isNotBlank())
            assertTrue(card.valueFreeActionSentence.isNotBlank())
            assertTrue(card.whyItMatters.isNotBlank())
            assertTrue(card.doNotWithdrawTravelMeetHandoffShareOrAttachBoundaries.size >= 2)
            assertTrue(
                card.doNotWithdrawTravelMeetHandoffShareOrAttachBoundaries.all { boundary ->
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
                    action.id in CashWithdrawalCourierHandoffPauseActionId.entries
                }
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

        val source = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/" +
                "CashWithdrawalCourierHandoffPauseCoach.kt"
        )
        CashWithdrawalCourierHandoffPauseActionId.entries.forEach { actionId ->
            assertTrue(
                "action lacks a stable label: $actionId",
                source.replace(Regex("\\s+"), " ").contains(
                    "CashWithdrawalCourierHandoffPauseActionId.$actionId to"
                )
            )
        }
    }

    @Test
    fun everyActionIdMapsToAnExistingMainActivityFlow() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val routerSource = activitySource
            .substringAfter("private fun openCashWithdrawalCourierHandoffPauseAction")
            .substringBefore("private fun shareCashWithdrawalCourierHandoffPauseCoach")
        val normalizedRouter = routerSource.replace(Regex("\\s+"), " ")

        assertFalse(
            "cash/courier action router is missing",
            routerSource == activitySource || routerSource.isBlank()
        )
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
                "openHelpDirectory(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(CashWithdrawalCourierHandoffPauseActionId.entries.size, expectedActionRoutes.size)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "action did not map to existing flow: $actionId",
                normalizedRouter.contains(
                    "CashWithdrawalCourierHandoffPauseActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            CashWithdrawalCourierHandoffPauseActionId.entries.size,
            Regex("CashWithdrawalCourierHandoffPauseActionId\\.[A-Z_]+\\s*->")
                .findAll(routerSource)
                .count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(routerSource).count())
    }

    @Test
    fun familyShareSummaryIsRebuiltOnlyFromGenericCardAndRouteLabels() {
        val coach = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.URGENT_TRANSFER,
                ScamSignal.IMPERSONATION,
                ScamSignal.LIVE_CALL_CONTROL,
                ScamSignal.CREDENTIALS,
                input =
                    "RAW_INPUT REDACTED_CLUE PERSON_NAME COURIER_NAME INVESTIGATOR_NAME " +
                        "HELPER_NAME LAWYER_NAME AGENCY_NAME BANK_BRANCH ATM_ADDRESS " +
                        "MEETING_POINT LOCKER_ID ROUTE_VALUE LOCATION_VALUE CASH_AMOUNT " +
                        "BANKNOTE_SERIAL ACCOUNT_NUMBER CARD_NUMBER WALLET_VALUE " +
                        "TRANSACTION_VALUE PAYMENT_VALUE REFUND_VALUE CHARGEBACK_VALUE " +
                        "OTP_VALUE PIN_VALUE PASSWORD_VALUE SECURITY_ANSWER RESIDENT_ID " +
                        "DEVICE_ID PHONE_VALUE CHANNEL_VALUE PROFILE_VALUE URL_VALUE " +
                        "DOMAIN_VALUE PACKAGE_VALUE APP_VALUE FILE_VALUE MEDIA_VALUE " +
                        "URI_VALUE PATH_VALUE CASE_ID REPORT_ID RECEIPT_ID SCREENSHOT_VALUE " +
                        "OCR_VALUE AUDIO_VALUE TRANSCRIPT_VALUE CLIPBOARD_VALUE LOGIN_STATE " +
                        "NOTIFICATION_VALUE RECIPIENT_NAME HANDOFF_STATUS DELIVERY_STATUS"
            ),
            readiness = readiness(
                ReadinessLevel.IN_PROGRESS,
                "PLAN_LITERAL SAFE_WORD_LITERAL CALLBACK_LITERAL CONTACT_LITERAL"
            ),
            incidentCount = 4
        )
        val summary = coach.familyShareSummary
        val cardsById = coach.cards.associateBy { card -> card.id }
        val expected = buildString {
            appendLine("[현금 인출·택배/퀵 대면 전달 멈춤 코치]")
            appendLine("현금 인출·이동·만남·물품 전달을 멈추는 로컬 수동 점검입니다.")
            appendLine()
            appendLine("추천 현금·대면 전달 멈춤 카드")
            coach.recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            coach.recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine(
                "- 실제 장소·주소·경로·사람·기사·연락처·금액·현금·계정·카드·통장·기기·문서·사건 값은 보내지 않습니다."
            )
            append(
                "- 이 안내는 인출·이동·만남·전달·추적·예약·검증·신고·동결·취소·환급·회복을 수행하거나 보장하지 않습니다."
            )
        }

        assertEquals(expected, summary)
        listOf(
            "RAW_INPUT",
            "REDACTED_CLUE",
            "PERSON_NAME",
            "COURIER_NAME",
            "INVESTIGATOR_NAME",
            "HELPER_NAME",
            "LAWYER_NAME",
            "AGENCY_NAME",
            "BANK_BRANCH",
            "ATM_ADDRESS",
            "MEETING_POINT",
            "LOCKER_ID",
            "ROUTE_VALUE",
            "LOCATION_VALUE",
            "CASH_AMOUNT",
            "BANKNOTE_SERIAL",
            "ACCOUNT_NUMBER",
            "CARD_NUMBER",
            "WALLET_VALUE",
            "TRANSACTION_VALUE",
            "PAYMENT_VALUE",
            "REFUND_VALUE",
            "CHARGEBACK_VALUE",
            "OTP_VALUE",
            "PIN_VALUE",
            "PASSWORD_VALUE",
            "SECURITY_ANSWER",
            "RESIDENT_ID",
            "DEVICE_ID",
            "PHONE_VALUE",
            "CHANNEL_VALUE",
            "PROFILE_VALUE",
            "URL_VALUE",
            "DOMAIN_VALUE",
            "PACKAGE_VALUE",
            "APP_VALUE",
            "FILE_VALUE",
            "MEDIA_VALUE",
            "URI_VALUE",
            "PATH_VALUE",
            "CASE_ID",
            "REPORT_ID",
            "RECEIPT_ID",
            "SCREENSHOT_VALUE",
            "OCR_VALUE",
            "AUDIO_VALUE",
            "TRANSCRIPT_VALUE",
            "CLIPBOARD_VALUE",
            "LOGIN_STATE",
            "NOTIFICATION_VALUE",
            "RECIPIENT_NAME",
            "HANDOFF_STATUS",
            "DELIVERY_STATUS",
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
    fun builderIsDeterministicAndIgnoresRawAnalysisAndReadinessFields() {
        val first = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.URGENT_TRANSFER,
                ScamSignal.LIVE_CALL_CONTROL,
                input = "FIRST_RAW_VALUE",
                score = 1
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "FIRST_PLAN_VALUE"),
            incidentCount = 2
        )
        val second = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.URGENT_TRANSFER,
                ScamSignal.LIVE_CALL_CONTROL,
                input = "SECOND_RAW_VALUE",
                score = 999
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "SECOND_PLAN_VALUE"),
            incidentCount = 93
        )

        assertEquals(first, second)
        assertEquals(
            first,
            CashWithdrawalCourierHandoffPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.HIGH,
                    ScamSignal.URGENT_TRANSFER,
                    ScamSignal.LIVE_CALL_CONTROL,
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
        assertNoPrivateValues(
            second,
            "FIRST_RAW_VALUE",
            "SECOND_RAW_VALUE",
            "FIRST_PLAN_VALUE",
            "SECOND_PLAN_VALUE"
        )
    }

    @Test
    fun privacyCopyStatesManualLocalStatelessAndAllNoActionBoundaries() {
        val copy = allCopy(
            CashWithdrawalCourierHandoffPauseCoachBuilder.build(
                null,
                readiness(ReadinessLevel.READY)
            )
        )

        listOf(
            "로컬 전용·무상태 수동 안내",
            "입력하라고 요구하지 않습니다",
            "현금 인출·ATM·은행 방문·송금·결제",
            "카드·통장·은행·NFC·지갑·블록체인·상품권 기능 또는 API",
            "동결·차단·취소·환급·차지백·추적·회복·역전",
            "지도·위치·경로·길 안내·내비게이션·지오코딩·주소 검증",
            "은행·ATM·지점·만남 장소·목적지·보관함을 찾거나 기록·검증·추천하지 않습니다",
            "택배·퀵·우편·배송·보관함·봉투",
            "조회·예약·접수·추적·발송·배차·수거·보관·인계하지 않고",
            "만남·방문·이동·경로·일정을 만들거나 예약하지 않습니다",
            "네트워크·웹 탐색·브라우저·웹뷰",
            "공식 번호·디렉터리·발신자 표시·전화번호",
            "사람·신원·문서·사건·법률·금융·전달 상태",
            "전화·다이얼·발신·통화·종료·선별·차단",
            "문자·SMS·받는 사람 선택·자동 전송",
            "연락처·주소록·알림을 읽기·쓰기·편집·선택·감시·필터링하지 않습니다",
            "QR·바코드·OCR·카메라·이미지 분석·화면 캡처",
            "파일·문서·사진·스크린샷·오디오·미디어·저장소",
            "첨부·스트림·URI 공유·업로드·동기화하지 않습니다",
            "클립보드·텍스트 감시기·자동완성·자격 증명·계정·인증",
            "생체 인증·키가드·키 저장소·암호화 API",
            "로그인·계정·인증 상태",
            "OTP·PIN·비밀번호·보안 답·주민 식별값",
            "안전단어·콜백 문장·연락처 이름·번호",
            "금액·현금 일련값·상품권·코드 값",
            "앱·패키지·설치 앱 목록·앱 이름·권한·기기 상태",
            "설치·제거·비활성화·강제 종료·권한 허용·철회·설정 변경",
            "기기 정리·원격 연결 종료",
            "접근성 서비스·기기 관리자·VPN·사용 통계·알림 수신 서비스",
            "새 Android 권한",
            "백그라운드 워커·스케줄러·브로드캐스트 수신기",
            "새 저장소를 만들지 않고",
            "가족 계획·사건·디렉터리·연락처·알림·통화·문자·계정·결제·위치·기기 상태를 수정하지 않습니다"
        ).forEach { boundary ->
            assertTrue("privacy copy omitted $boundary", copy.contains(boundary))
        }
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndAutomaticProtectionClaims() {
        val copy = allCopy(
            CashWithdrawalCourierHandoffPauseCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.READY),
                incidentCount = 1
            )
        )

        listOf(
            "official courier",
            "verified investigator",
            "safe handoff",
            "bank verified",
            "cash protected",
            "case accepted",
            "report submitted",
            "report accepted",
            "report received",
            "delivered successfully",
            "collected successfully",
            "deposited successfully",
            "meeting scheduled",
            "payment cancelled",
            "refund guaranteed",
            "chargeback accepted",
            "funds recovered",
            "account frozen",
            "route traced",
            "legal result guaranteed",
            "financial result guaranteed",
            "guaranteed result",
            "problem-free",
            "공식 택배 기사입니다",
            "검증된 수사관입니다",
            "안전한 전달입니다",
            "은행 확인 완료",
            "현금이 보호됐습니다",
            "사건이 접수됐습니다",
            "신고가 제출됐습니다",
            "신고가 수신됐습니다",
            "배달이 완료됐습니다",
            "수거가 완료됐습니다",
            "입금이 완료됐습니다",
            "만남이 예약됐습니다",
            "결제가 취소됐습니다",
            "환급이 보장됩니다",
            "차지백이 접수됐습니다",
            "자금이 회복됐습니다",
            "계정이 동결됐습니다",
            "경로가 추적됐습니다",
            "법률 결과를 보장합니다",
            "금융 결과를 보장합니다",
            "문제없습니다",
            "자동 현금 감시",
            "자동 위치 추적",
            "자동 택배 조회",
            "자동 만남 예약",
            "자동 증거 촬영",
            "자동 파일 첨부",
            "자동 신고 제출",
            "automatic monitoring",
            "automatic scanning",
            "automatic deletion",
            "automatic redaction",
            "automatic blocking",
            "automatic calling",
            "automatic messaging",
            "automatic lookup",
            "automatic verification",
            "automatic storage",
            "automatic upload",
            "automatic attachment",
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
            "app/src/main/java/com/mongmong/familyshield/" +
                "CashWithdrawalCourierHandoffPauseCoach.kt"
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
            "HttpURLConnection",
            "HttpsURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "ktor.client",
            "WebView",
            "Intent.ACTION_VIEW",
            "Geocoder",
            "LocationManager",
            "FusedLocationProvider",
            "LocationServices",
            "GoogleMap",
            "MapView",
            "NavController",
            "Patterns.WEB_URL",
            "URLUtil",
            "InetAddress",
            "PhoneNumberUtils",
            "Patterns.PHONE",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "ACTION_SENDTO",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "PhoneStateListener",
            "CallLog",
            "SmsManager",
            "ContactsContract",
            "NotificationManager",
            "NotificationCompat",
            "NotificationListenerService",
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
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "FileProvider",
            "MediaStore",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "BarcodeScanner",
            "BarcodeScanning",
            "QRCodeReader",
            "TextRecognition",
            "ImageAnalysis",
            "ImageCapture",
            "CameraManager",
            "BitmapFactory",
            "ExifInterface",
            "PackageManager",
            "getInstalledPackages",
            "getInstalledApplications",
            "PackageInstaller",
            "deletePackage",
            "ACTION_MANAGE_UNKNOWN_APP_SOURCES",
            "ACTION_APPLICATION_DETAILS_SETTINGS",
            "ACTION_REQUEST_INSTALL_PACKAGE",
            "ACTION_UNINSTALL_PACKAGE",
            "DevicePolicyManager",
            "AccessibilityService",
            "AccessibilityManager",
            "VpnService",
            "UsageStatsManager",
            "AppOpsManager",
            "BillingClient",
            "NfcAdapter",
            "WalletConnect",
            "BlockchainClient",
            "PaymentClient",
            "BankingClient",
            "CardClient",
            "RefundClient",
            "ChargebackClient",
            "GiftCardClient",
            "BalanceClient",
            "RedeemClient",
            "CashManager",
            "AtmClient",
            "BankBranchClient",
            "CourierClient",
            "DeliveryTracking",
            "TrackingClient",
            "MeetingScheduler",
            "CalendarContract",
            "AlarmManager",
            "WorkManager",
            "JobScheduler",
            "ForegroundService",
            "BroadcastReceiver",
            "urlValue",
            "domainValue",
            "locationValue",
            "routeValue",
            "addressValue",
            "lockerId",
            "courierId",
            "cashAmount",
            "cashSerial",
            "accountNumber",
            "cardNumber",
            "walletValue",
            "transactionValue",
            "otpValue",
            "pinValue",
            "passwordValue",
            "securityAnswer",
            "residentId",
            "deviceId",
            "caseId",
            "reportStatus",
            "submissionStatus",
            "receiptId",
            "verificationResult",
            "handoffStatus",
            "recoveryResult",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenSource ->
            assertFalse(
                "cash/courier model used forbidden source: $forbiddenSource",
                source.contains(forbiddenSource, ignoreCase = true)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(source))
        }
    }

    @Test
    fun highRiskShowsAllCardsAndRoutesWhileSignalCategoriesRemainDistinct() {
        val highRisk = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            analysis(RiskLevel.HIGH, ScamSignal.SECRECY),
            readiness(ReadinessLevel.READY)
        )
        assertEquals(CashWithdrawalCourierHandoffPauseCardId.entries, highRisk.recommendedCardIds)
        assertEquals(CashWithdrawalCourierHandoffPauseActionId.entries, highRisk.recommendedActionIds)

        val financial = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.URGENT_TRANSFER),
            readiness(ReadinessLevel.READY)
        )
        val official = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
            readiness(ReadinessLevel.READY)
        )
        val liveApp = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.APP_INSTALL),
            readiness(ReadinessLevel.READY)
        )
        val credential = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.CREDENTIALS),
            readiness(ReadinessLevel.READY)
        )
        val family = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.FAMILY_EMERGENCY),
            readiness(ReadinessLevel.READY)
        )
        assertNotEquals(financial.recommendedActionIds, official.recommendedActionIds)
        assertNotEquals(official.recommendedActionIds, liveApp.recommendedActionIds)
        assertNotEquals(liveApp.recommendedActionIds, credential.recommendedActionIds)
        assertNotEquals(credential.recommendedActionIds, family.recommendedActionIds)
        assertNotEquals(financial.recommendationTitle, family.recommendationTitle)
    }

    private fun assertRoutes(
        coach: CashWithdrawalCourierHandoffPauseCoach,
        vararg expected: CashWithdrawalCourierHandoffPauseActionId
    ) {
        assertTrue(coach.recommendedActionIds.containsAll(expected.toList()))
    }

    private fun assertNoPrivateValues(
        coach: CashWithdrawalCourierHandoffPauseCoach,
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

    private fun allCopy(coach: CashWithdrawalCourierHandoffPauseCoach): String = buildString {
        appendLine(coach.title)
        appendLine(coach.intro)
        appendLine(coach.modeLabel)
        appendLine(coach.recommendationTitle)
        appendLine(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> appendLine(action.label) }
        coach.cards.forEach { card ->
            appendLine(card.title)
            appendLine(card.cashCourierPressureScenario)
            appendLine(card.valueFreeActionSentence)
            appendLine(card.whyItMatters)
            card.doNotWithdrawTravelMeetHandoffShareOrAttachBoundaries.forEach(::appendLine)
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
