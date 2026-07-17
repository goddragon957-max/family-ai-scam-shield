package com.mongmong.familyshield

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TravelRouteMeetingPointPauseCoachTest {
    @Test
    fun noAnalysisCoachIsConservativeAndNeverDeclaresTravelMeetingOrOutcomeStatus() {
        val coach = TravelRouteMeetingPointPauseCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(TravelRouteMeetingPointPauseCardId.entries, coach.recommendedCardIds)
        assertEquals(
            TravelRouteMeetingPointPauseActionId.entries.filterNot { actionId ->
                actionId == TravelRouteMeetingPointPauseActionId.ANALYSIS_RESULT
            },
            coach.recommendedActionIds
        )
        assertEquals("이동 경로·만남 장소 멈춤 코치", coach.title)
        assertTrue(coach.recommendationTitle.contains("모든"))
        assertTrue(coach.recommendationExplanation.contains("상태를 공식"))
        assertTrue(coach.recommendationExplanation.contains("단정하지 않고"))

        val copy = allCopy(coach)
        listOf(
            "official route",
            "verified destination",
            "safe meeting",
            "trusted driver",
            "official courier",
            "verified staff",
            "travel scheduled",
            "meeting scheduled",
            "arrived successfully",
            "departed successfully",
            "boarded successfully",
            "route traced",
            "handoff completed",
            "payment cancelled",
            "refund guaranteed",
            "funds recovered",
            "account frozen",
            "report submitted",
            "legal outcome guaranteed",
            "problem-free",
            "공식 경로입니다",
            "검증된 목적지입니다",
            "안전한 만남입니다",
            "신뢰할 수 있는 기사입니다",
            "이동이 예약됐습니다",
            "만남이 예약됐습니다",
            "도착했습니다",
            "출발했습니다",
            "탑승했습니다",
            "경로가 추적됐습니다",
            "인계가 완료됐습니다",
            "결제가 취소됐습니다",
            "환급이 보장됩니다",
            "자금이 회복됐습니다",
            "계정이 동결됐습니다",
            "신고가 제출됐습니다",
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
    fun financialSignalsRecommendPauseExitVerificationAccountEvidenceAndHelpRoutes() {
        listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.BAIT,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = TravelRouteMeetingPointPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "CASH_VALUE BANK_VALUE CARD_VALUE PAYMENT_VALUE ACCOUNT_VALUE " +
                            "LOCATION_VALUE ROUTE_VALUE DESTINATION_VALUE VEHICLE_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertRoutes(
                coach,
                TravelRouteMeetingPointPauseActionId
                    .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH,
                TravelRouteMeetingPointPauseActionId
                    .BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH,
                TravelRouteMeetingPointPauseActionId
                    .DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH,
                TravelRouteMeetingPointPauseActionId.MONEY_TRANSFER_PAUSE_GATE,
                TravelRouteMeetingPointPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                TravelRouteMeetingPointPauseActionId
                    .OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
                TravelRouteMeetingPointPauseActionId
                    .SEPARATE_CHANNEL_VERIFICATION_COACH,
                TravelRouteMeetingPointPauseActionId.ACCOUNT_LOCKDOWN,
                TravelRouteMeetingPointPauseActionId.EXPOSURE_IMPACT_TRIAGE,
                TravelRouteMeetingPointPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                TravelRouteMeetingPointPauseActionId.TRUSTED_HELPER_REVIEW,
                TravelRouteMeetingPointPauseActionId.OFFICIAL_HELP_DIRECTORY,
                TravelRouteMeetingPointPauseActionId.REPORT_PREP
            )
            assertTrue(coach.recommendationExplanation.contains("돈 관련 범주"))
            assertTrue(
                coach.recommendationExplanation.contains(
                    "지도·위치·주소·경로·목적지·차량 행동을 하지 않습니다"
                )
            )
            assertNoPrivateValues(
                coach,
                "CASH_VALUE",
                "BANK_VALUE",
                "CARD_VALUE",
                "PAYMENT_VALUE",
                "ACCOUNT_VALUE",
                "LOCATION_VALUE",
                "ROUTE_VALUE",
                "DESTINATION_VALUE",
                "VEHICLE_VALUE"
            )
            listOf(
                "현금을 인출했습니다",
                "카드를 정지했습니다",
                "결제를 취소했습니다",
                "계정을 동결했습니다",
                "목적지를 안내합니다",
                "경로를 계산했습니다",
                "차량을 호출했습니다"
            ).forEach { claim -> assertFalse(allCopy(coach).contains(claim)) }
        }
    }

    @Test
    fun officialPressureSignalsRecommendRealityCheckFamilyEvidenceAndHelpWithoutLookup() {
        listOf(
            ScamSignal.IMPERSONATION,
            ScamSignal.SECRECY,
            ScamSignal.SAFE_ACCOUNT_FRAUD
        ).forEach { signal ->
            val coach = TravelRouteMeetingPointPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "AGENCY_VALUE STAFF_VALUE VISITOR_VALUE DRIVER_VALUE CASE_VALUE " +
                            "DOCUMENT_VALUE DESTINATION_VALUE OFFICIAL_NUMBER_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY, "PRIVATE_PLAN_VALUE")
            )

            assertRoutes(
                coach,
                TravelRouteMeetingPointPauseActionId
                    .OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
                TravelRouteMeetingPointPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                TravelRouteMeetingPointPauseActionId
                    .SEPARATE_CHANNEL_VERIFICATION_COACH,
                TravelRouteMeetingPointPauseActionId.FAMILY_ALERT_MESSAGES,
                TravelRouteMeetingPointPauseActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                TravelRouteMeetingPointPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                TravelRouteMeetingPointPauseActionId.TRUSTED_HELPER_REVIEW,
                TravelRouteMeetingPointPauseActionId.OFFICIAL_HELP_DIRECTORY,
                TravelRouteMeetingPointPauseActionId.REPORT_PREP
            )
            assertTrue(
                coach.recommendationExplanation.contains(
                    "공식 번호·목적지·직원·방문자·기사 신원·문서·사건을 검증"
                )
            )
            assertTrue(
                coach.recommendationExplanation.contains(
                    "전화·브라우저·문자·신고 제출을 하지 않습니다"
                )
            )
            assertNoPrivateValues(
                coach,
                "AGENCY_VALUE",
                "STAFF_VALUE",
                "VISITOR_VALUE",
                "DRIVER_VALUE",
                "CASE_VALUE",
                "DOCUMENT_VALUE",
                "DESTINATION_VALUE",
                "OFFICIAL_NUMBER_VALUE",
                "PRIVATE_PLAN_VALUE"
            )
            listOf(
                "공식 번호를 찾았습니다",
                "목적지를 검증했습니다",
                "직원 신원을 확인했습니다",
                "기사를 확인했습니다",
                "문서를 검증했습니다",
                "사건을 조회했습니다",
                "전화를 걸었습니다",
                "신고를 제출했습니다"
            ).forEach { claim -> assertFalse(allCopy(coach).contains(claim)) }
        }
    }

    @Test
    fun liveCallAppLinkAndCourierSignalsRecommendExitQuarantinePauseAndExposureRoutes() {
        listOf(
            ScamSignal.LIVE_CALL_CONTROL,
            ScamSignal.APP_INSTALL,
            ScamSignal.LINK,
            ScamSignal.PARCEL_INVITATION_SMISHING
        ).forEach { signal ->
            val coach = TravelRouteMeetingPointPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "CALL_STATE APP_VALUE PACKAGE_VALUE DEVICE_VALUE URL_VALUE " +
                            "LOCATION_VALUE ROUTE_VALUE TRACKING_VALUE SCHEDULE_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertRoutes(
                coach,
                TravelRouteMeetingPointPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                TravelRouteMeetingPointPauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH,
                TravelRouteMeetingPointPauseActionId.REMOTE_ACCESS_CUTOFF_COACH,
                TravelRouteMeetingPointPauseActionId
                    .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH,
                TravelRouteMeetingPointPauseActionId
                    .DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH,
                TravelRouteMeetingPointPauseActionId.ACCOUNT_LOCKDOWN,
                TravelRouteMeetingPointPauseActionId.EXPOSURE_IMPACT_TRIAGE,
                TravelRouteMeetingPointPauseActionId.PRIVACY_CENTER
            )
            assertTrue(coach.recommendationExplanation.contains("지도·위치·경로·내비게이션·택배 추적"))
            assertTrue(coach.recommendationExplanation.contains("여행 예약"))
            assertTrue(coach.recommendationExplanation.contains("원격 연결 종료·기기 정리를 하지 않습니다"))
            assertNoPrivateValues(
                coach,
                "CALL_STATE",
                "APP_VALUE",
                "PACKAGE_VALUE",
                "DEVICE_VALUE",
                "URL_VALUE",
                "LOCATION_VALUE",
                "ROUTE_VALUE",
                "TRACKING_VALUE",
                "SCHEDULE_VALUE"
            )
            listOf(
                "통화를 종료했습니다",
                "앱을 제거했습니다",
                "설정을 변경했습니다",
                "원격 연결을 종료했습니다",
                "기기를 정리했습니다",
                "위치를 확인했습니다",
                "경로를 안내했습니다",
                "택배를 추적했습니다",
                "여행을 예약했습니다"
            ).forEach { claim -> assertFalse(allCopy(coach).contains(claim)) }
        }
    }

    @Test
    fun credentialOtpAndGiftCardSignalsUseOnlyExistingValueFreeRoutes() {
        listOf(
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = TravelRouteMeetingPointPauseCoachBuilder.build(
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
                TravelRouteMeetingPointPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH,
                TravelRouteMeetingPointPauseActionId.GIFT_CARD_CODE_PAUSE_COACH,
                TravelRouteMeetingPointPauseActionId.ACCOUNT_LOCKDOWN,
                TravelRouteMeetingPointPauseActionId.SAFE_PASTE_PRIVACY_COACH,
                TravelRouteMeetingPointPauseActionId.REMOTE_ACCESS_CUTOFF_COACH,
                TravelRouteMeetingPointPauseActionId.EXPOSURE_IMPACT_TRIAGE
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
    fun recoveryPromiseSignalsRouteToRealityCheckWithoutOutcomeActions() {
        listOf(
            ScamSignal.BAIT,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = TravelRouteMeetingPointPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "REFUND_VALUE CHARGEBACK_VALUE RECOVERY_VALUE FREEZE_VALUE " +
                            "TRACE_VALUE CANCEL_VALUE ROUTE_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertRoutes(
                coach,
                TravelRouteMeetingPointPauseActionId
                    .RECOVERY_PROMISE_REALITY_CHECK_COACH,
                TravelRouteMeetingPointPauseActionId.MONEY_TRANSFER_PAUSE_GATE,
                TravelRouteMeetingPointPauseActionId
                    .CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH,
                TravelRouteMeetingPointPauseActionId
                    .DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH,
                TravelRouteMeetingPointPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                TravelRouteMeetingPointPauseActionId.TRUSTED_HELPER_REVIEW,
                TravelRouteMeetingPointPauseActionId.OFFICIAL_HELP_DIRECTORY,
                TravelRouteMeetingPointPauseActionId.REPORT_PREP
            )
            assertTrue(coach.recommendationTitle.contains("회복 약속"))
            assertTrue(coach.recommendationExplanation.contains("수행하거나 보장하지 않습니다"))
            assertNoPrivateValues(
                coach,
                "REFUND_VALUE",
                "CHARGEBACK_VALUE",
                "RECOVERY_VALUE",
                "FREEZE_VALUE",
                "TRACE_VALUE",
                "CANCEL_VALUE",
                "ROUTE_VALUE"
            )
        }
    }

    @Test
    fun familyEmergencyDeepfakeAndNumberChangeUseOnlyValueFreeFamilyRoutes() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val coach = TravelRouteMeetingPointPauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "FAMILY_VALUE PHONE_VALUE CHANNEL_VALUE PROFILE_VALUE " +
                            "CONTACT_STATE CALL_STATE SMS_STATE"
                ),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "CONTACT_VALUE SAFE_WORD_VALUE CALLBACK_VALUE"
                )
            )

            assertRoutes(
                coach,
                TravelRouteMeetingPointPauseActionId
                    .SEPARATE_CHANNEL_VERIFICATION_COACH,
                TravelRouteMeetingPointPauseActionId.FAMILY_CALLBACK_CARD,
                TravelRouteMeetingPointPauseActionId
                    .VOICE_IMPERSONATION_REALITY_CHECK_COACH,
                TravelRouteMeetingPointPauseActionId.NEW_NUMBER_QUARANTINE_COACH,
                TravelRouteMeetingPointPauseActionId.FAMILY_SAFE_WORD_RESET_COACH,
                TravelRouteMeetingPointPauseActionId.SAFE_CALL_SCRIPTS,
                TravelRouteMeetingPointPauseActionId.FAMILY_ALERT_MESSAGES
            )
            assertTrue(coach.recommendationExplanation.contains("연락처·안전단어·콜백 값을 사용"))
            assertTrue(coach.recommendationExplanation.contains("통화·문자를 읽지 않습니다"))
            assertNoPrivateValues(
                coach,
                "FAMILY_VALUE",
                "PHONE_VALUE",
                "CHANNEL_VALUE",
                "PROFILE_VALUE",
                "CONTACT_STATE",
                "CALL_STATE",
                "SMS_STATE",
                "CONTACT_VALUE",
                "SAFE_WORD_VALUE",
                "CALLBACK_VALUE"
            )
        }
    }

    @Test
    fun savedIncidentUsesOnlyClampedPresenceCategory() {
        val baseAnalysis = analysis(RiskLevel.MEDIUM, ScamSignal.LIVE_CALL_CONTROL)
        val noIncident = TravelRouteMeetingPointPauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 0
        )
        val negativeIncident = TravelRouteMeetingPointPauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = -83
        )
        val oneIncident = TravelRouteMeetingPointPauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 1
        )
        val manyIncidents = TravelRouteMeetingPointPauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = Int.MAX_VALUE
        )

        assertEquals(noIncident, negativeIncident)
        assertEquals(oneIncident, manyIncidents)
        assertNotEquals(noIncident.recommendedActionIds, oneIncident.recommendedActionIds)
        assertTrue(
            TravelRouteMeetingPointPauseCardId.CHOOSE_FAMILY_HELP_REPORT_AND_FOLLOW_UP_ROUTE in
                oneIncident.recommendedCardIds
        )
        assertRoutes(
            oneIncident,
            TravelRouteMeetingPointPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
            TravelRouteMeetingPointPauseActionId.EVIDENCE_KIT,
            TravelRouteMeetingPointPauseActionId.INCIDENT_TIMELINE_BRIEFING,
            TravelRouteMeetingPointPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER
        )
        assertTrue(oneIncident.recommendationExplanation.contains("개수 범주만"))
        assertTrue(oneIncident.recommendationExplanation.contains("사건 내용이나 미리보기를 읽지"))
    }

    @Test
    fun readinessUsesOnlyLevelAndReadyNeverMakesTravelRouteDestinationOrMeetingSafe() {
        listOf(ReadinessLevel.LOW, ReadinessLevel.IN_PROGRESS).forEach { level ->
            val coach = TravelRouteMeetingPointPauseCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
                readiness(level, "READINESS_PRIVATE_LITERAL")
            )

            assertRoutes(
                coach,
                TravelRouteMeetingPointPauseActionId.SAFETY_PLAN,
                TravelRouteMeetingPointPauseActionId.FAMILY_SAFE_WORD_RESET_COACH,
                TravelRouteMeetingPointPauseActionId.FAMILY_READINESS_ROADMAP,
                TravelRouteMeetingPointPauseActionId.FAMILY_SAFETY_DRILL_COACH,
                TravelRouteMeetingPointPauseActionId.SAFE_CALL_SCRIPTS
            )
            assertFalse(allCopy(coach).contains("READINESS_PRIVATE_LITERAL"))
        }

        val readyCoach = TravelRouteMeetingPointPauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
            readiness(ReadinessLevel.READY, "READY_PRIVATE_LITERAL")
        )
        val explanation = readyCoach.recommendationExplanation
        listOf(
            "준비",
            "경로",
            "목적지",
            "만남",
            "요청",
            "여행",
            "이동",
            "인계",
            "안전",
            "신뢰",
            "공식",
            "아닙니다"
        ).forEach { token ->
            assertTrue("ready boundary omitted $token", explanation.contains(token))
        }
        assertFalse(allCopy(readyCoach).contains("READY_PRIVATE_LITERAL"))
    }

    @Test
    fun cardsAreCompleteStableAndUseUniqueDeclaredRouteActions() {
        val coach = TravelRouteMeetingPointPauseCoachBuilder.build(
            null,
            readiness(ReadinessLevel.READY)
        )

        assertEquals(TravelRouteMeetingPointPauseCardId.entries.size, coach.cards.size)
        assertEquals(
            TravelRouteMeetingPointPauseCardId.entries,
            coach.cards.map { card -> card.id }
        )
        assertEquals(
            (1..TravelRouteMeetingPointPauseCardId.entries.size).toList(),
            coach.cards.map { card -> card.displayOrder }
        )
        coach.cards.forEach { card ->
            assertEquals(card.id.displayOrder, card.displayOrder)
            assertTrue(card.title.isNotBlank())
            assertTrue(card.movementMeetingPressureSituation.isNotBlank())
            assertTrue(card.valueFreePauseScript.isNotBlank())
            assertTrue(card.whyItMatters.isNotBlank())
            assertTrue(
                card.doNotTravelNavigateShareLocationMeetHandoffSayShareAttachOrRecordBoundaries
                    .size >= 2
            )
            assertTrue(
                card.doNotTravelNavigateShareLocationMeetHandoffSayShareAttachOrRecordBoundaries
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
                    action.id in TravelRouteMeetingPointPauseActionId.entries
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

        val allActions = TravelRouteMeetingPointPauseCoachBuilder.build(
            analysis(RiskLevel.HIGH, ScamSignal.SECRECY),
            readiness(ReadinessLevel.READY)
        ).recommendedActions
        assertEquals(TravelRouteMeetingPointPauseActionId.entries, allActions.map { it.id })
        assertTrue(allActions.all { action -> action.label.isNotBlank() })
        assertEquals(allActions.size, allActions.map { action -> action.label }.distinct().size)
    }

    @Test
    fun everyActionIdMapsToAnExistingMainActivityFlow() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val routerSource = activitySource
            .substringAfter("private fun openTravelRouteMeetingPointPauseAction")
            .substringBefore("private fun shareTravelRouteMeetingPointPauseCoach")
        val normalizedRouter = routerSource.replace(Regex("\\s+"), " ")

        assertFalse(
            "travel route meeting point action router is missing",
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
            "CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH" to
                "show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)",
            "BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH" to
                "show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)",
            "DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH" to
                "show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)",
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
                "openHelpDirectory(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(TravelRouteMeetingPointPauseActionId.entries.size, expectedActionRoutes.size)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "action did not map to existing flow: $actionId",
                normalizedRouter.contains(
                    "TravelRouteMeetingPointPauseActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            TravelRouteMeetingPointPauseActionId.entries.size,
            Regex("TravelRouteMeetingPointPauseActionId\\.[A-Z_]+\\s*->")
                .findAll(routerSource)
                .count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(routerSource).count())
    }

    @Test
    fun familyShareSummaryIsRebuiltOnlyFromGenericCardAndRouteLabels() {
        val coach = TravelRouteMeetingPointPauseCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.URGENT_TRANSFER,
                ScamSignal.IMPERSONATION,
                ScamSignal.LIVE_CALL_CONTROL,
                ScamSignal.CREDENTIALS,
                input =
                    "RAW_INPUT REDACTED_CLUE HOME_VALUE WORK_VALUE UNIT_VALUE LOBBY_VALUE " +
                        "PARKING_VALUE INTERCOM_VALUE DOOR_VALUE LOCKER_VALUE STORE_VALUE " +
                        "STATION_VALUE LOCATION_VALUE ROUTE_VALUE DESTINATION_VALUE VEHICLE_VALUE " +
                        "TAXI_VALUE BUS_VALUE SUBWAY_VALUE RIDE_VALUE FARE_VALUE ETA_VALUE " +
                        "ARRIVAL_VALUE DEPARTURE_VALUE PERSON_NAME STAFF_NAME VISITOR_NAME " +
                        "COURIER_NAME DRIVER_NAME HELPER_NAME LAWYER_NAME INVESTIGATOR_NAME " +
                        "AGENCY_NAME OFFICIAL_NUMBER DIRECTORY_VALUE CASH_AMOUNT CASH_SERIAL " +
                        "ACCOUNT_NUMBER CARD_NUMBER BANK_VALUE WALLET_VALUE TRANSACTION_VALUE " +
                        "PAYMENT_VALUE OTP_VALUE PIN_VALUE PASSWORD_VALUE SECURITY_ANSWER " +
                        "RESIDENT_ID DEVICE_ID PHONE_VALUE CHANNEL_VALUE PROFILE_VALUE URL_VALUE " +
                        "DOMAIN_VALUE PACKAGE_VALUE APP_VALUE FILE_VALUE MEDIA_VALUE URI_VALUE " +
                        "PATH_VALUE QR_VALUE BARCODE_VALUE CODE_VALUE CASE_ID REPORT_ID " +
                        "SUBMISSION_STATUS RECEIPT_ID SCREENSHOT_VALUE IMAGE_VALUE OCR_VALUE " +
                        "AUDIO_VALUE TRANSCRIPT_VALUE CLIPBOARD_VALUE AUTOFILL_VALUE " +
                        "ACCOUNT_STATE LOGIN_STATE CALL_STATE SMS_STATE NOTIFICATION_VALUE " +
                        "RECIPIENT_NAME VERIFICATION_RESULT RESULT_STATUS ROUTED_STATUS " +
                        "SCHEDULED_STATUS MET_STATUS DELIVERED_STATUS COLLECTED_STATUS " +
                        "DEPOSITED_STATUS HANDOFF_STATUS PAYMENT_STATUS REFUND_STATUS " +
                        "CHARGEBACK_STATUS RECOVERY_STATUS FREEZE_STATUS CANCEL_STATUS " +
                        "BLOCK_STATUS TRACE_STATUS LEGAL_OUTCOME FINANCIAL_OUTCOME " +
                        "010-9876-5432 1234567890123 secret.example"
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
            appendLine("[이동 경로·만남 장소 멈춤 코치]")
            appendLine("민감한 값을 말하지 않고 이동과 만남을 멈춘 채 직접 읽을 문장을 고르는 로컬 수동 안내입니다.")
            appendLine()
            appendLine("추천 값 없는 이동·만남 멈춤 카드")
            coach.recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            coach.recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 실제 현재 위치·주소·경로·목적지·교통수단·도착 시간·사람·연락처·금액·계정·기기 값은 보내지 않습니다.")
            append("- 이 안내는 이동·길 안내·위치 조회·만남·인계·추적·예약·검증·신고·금융·기기 행동을 수행하거나 보장하지 않습니다.")
        }
        assertEquals(expected, summary)

        listOf(
            "RAW_INPUT",
            "REDACTED_CLUE",
            "HOME_VALUE",
            "WORK_VALUE",
            "UNIT_VALUE",
            "LOBBY_VALUE",
            "PARKING_VALUE",
            "INTERCOM_VALUE",
            "DOOR_VALUE",
            "LOCKER_VALUE",
            "STORE_VALUE",
            "STATION_VALUE",
            "LOCATION_VALUE",
            "ROUTE_VALUE",
            "DESTINATION_VALUE",
            "VEHICLE_VALUE",
            "TAXI_VALUE",
            "BUS_VALUE",
            "SUBWAY_VALUE",
            "RIDE_VALUE",
            "FARE_VALUE",
            "ETA_VALUE",
            "ARRIVAL_VALUE",
            "DEPARTURE_VALUE",
            "PERSON_NAME",
            "STAFF_NAME",
            "VISITOR_NAME",
            "COURIER_NAME",
            "DRIVER_NAME",
            "HELPER_NAME",
            "LAWYER_NAME",
            "INVESTIGATOR_NAME",
            "AGENCY_NAME",
            "OFFICIAL_NUMBER",
            "DIRECTORY_VALUE",
            "CASH_AMOUNT",
            "CASH_SERIAL",
            "ACCOUNT_NUMBER",
            "CARD_NUMBER",
            "BANK_VALUE",
            "WALLET_VALUE",
            "TRANSACTION_VALUE",
            "PAYMENT_VALUE",
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
            "QR_VALUE",
            "BARCODE_VALUE",
            "CODE_VALUE",
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
            "NOTIFICATION_VALUE",
            "RECIPIENT_NAME",
            "VERIFICATION_RESULT",
            "RESULT_STATUS",
            "ROUTED_STATUS",
            "SCHEDULED_STATUS",
            "MET_STATUS",
            "DELIVERED_STATUS",
            "COLLECTED_STATUS",
            "DEPOSITED_STATUS",
            "HANDOFF_STATUS",
            "PAYMENT_STATUS",
            "REFUND_STATUS",
            "CHARGEBACK_STATUS",
            "RECOVERY_STATUS",
            "FREEZE_STATUS",
            "CANCEL_STATUS",
            "BLOCK_STATUS",
            "TRACE_STATUS",
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
    fun builderIsDeterministicAndIgnoresRawAnalysisAndReadinessFields() {
        val first = TravelRouteMeetingPointPauseCoachBuilder.build(
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
        val second = TravelRouteMeetingPointPauseCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.LIVE_CALL_CONTROL,
                ScamSignal.URGENT_TRANSFER,
                input = "SECOND_RAW_VALUE",
                score = 999
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "SECOND_PLAN_VALUE"),
            incidentCount = 93
        )

        assertEquals(first, second)
        assertEquals(
            first,
            TravelRouteMeetingPointPauseCoachBuilder.build(
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
            TravelRouteMeetingPointPauseCoachBuilder.build(
                null,
                readiness(ReadinessLevel.READY)
            )
        )

        listOf(
            "로컬 전용·무상태 수동 안내",
            "민감한 값을 입력하라고 요구하지 않고",
            "새 저장소·자동 처리·자동 전송·스케줄러를 만들지 않습니다",
            "현재 위치·집·직장·건물·로비·호수·엘리베이터·주차장·인터폰·문·보관함·상점·역·주소·경로·목적지",
            "지도·위치·경로·길 안내·내비게이션·지오코딩·주소 검증",
            "현재 위치를 감지",
            "경로 계획·교통·대중교통·택시·배차·승차 호출 기능이나 API",
            "걷기·운전·차량·택시·버스·지하철·승차·운임·출발·도착·예상 시간·이동 상태",
            "택배·퀵·우편·배송·보관함·기사·물품",
            "만남·방문·여행·이동·경로·캘린더 일정·약속을 만들거나 예약하지 않습니다",
            "네트워크·웹 탐색·브라우저·웹뷰",
            "기관 정보·공식 번호·디렉터리·발신자 표시·전화번호",
            "신고·출동을 요청·제출하지 않습니다",
            "전화·다이얼·발신·통화·종료·선별·차단·통화 상태·통화 기록",
            "문자·SMS·받는 사람 선택·자동 전송",
            "연락처·주소록·알림을 읽기·쓰기·편집·선택·감시·필터링하지 않습니다",
            "QR·바코드·OCR·카메라·이미지 분석·화면 캡처",
            "파일·문서·사진·스크린샷·오디오·미디어·저장소",
            "첨부·스트림·URI 공유·업로드·동기화하지 않습니다",
            "클립보드·텍스트 감시기·자동완성·자격 증명·계정·로그인·인증",
            "계정·인증 상태를 읽거나 바꾸지 않습니다",
            "OTP·PIN·비밀번호·보안 답·주민 식별값",
            "현금 인출·입금·송금·결제",
            "은행·ATM·NFC·지갑·블록체인·가상자산·상품권 기능 또는 API",
            "구매·잔액·교환·취소·동결·차단·환급·차지백·추적·회복",
            "앱·패키지·설치 앱 목록·앱 이름·권한·기기 상태",
            "설치·제거·비활성화·강제 종료·권한 허용·철회·설정 변경",
            "접근성 서비스·기기 관리자·VPN·사용 통계·알림 수신 서비스",
            "고정 카드 제목과 기존 앱 경로·행동 종류만 담고",
            "파일·스트림·URI를 붙이거나 자동 발송하지 않습니다",
            "새 Android 권한",
            "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러·브로드캐스트 수신기",
            "가족 계획·사건·공식 디렉터리·연락처·알림·통화·문자·위치·지도·경로·교통·택시·배차·택배·보관함·만남·방문·문·계정·결제·앱·권한·기기·자격·신고·확인·결과 상태를 수정하지 않습니다"
        ).forEach { boundary ->
            assertTrue("privacy copy omitted $boundary", copy.contains(boundary))
        }
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndAutomaticProtectionClaims() {
        val copy = allCopy(
            TravelRouteMeetingPointPauseCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.READY),
                incidentCount = 1
            )
        )

        listOf(
            "official route",
            "verified destination",
            "safe meeting",
            "trusted driver",
            "travel scheduled",
            "meeting scheduled",
            "arrived successfully",
            "departed successfully",
            "boarded successfully",
            "route traced",
            "handoff completed",
            "payment cancelled",
            "refund guaranteed",
            "funds recovered",
            "account frozen",
            "report submitted",
            "guaranteed result",
            "problem-free",
            "공식 경로입니다",
            "검증된 목적지입니다",
            "안전한 만남입니다",
            "신뢰할 수 있는 기사입니다",
            "이동이 예약됐습니다",
            "만남이 예약됐습니다",
            "도착했습니다",
            "출발했습니다",
            "탑승했습니다",
            "경로가 추적됐습니다",
            "인계가 완료됐습니다",
            "결제가 취소됐습니다",
            "환급이 보장됩니다",
            "자금이 회복됐습니다",
            "계정이 동결됐습니다",
            "신고가 제출됐습니다",
            "문제없습니다",
            "자동 위치 추적",
            "자동 경로 안내",
            "자동 차량 호출",
            "자동 만남 예약",
            "자동 택배 조회",
            "자동 신고 제출",
            "automatic monitoring",
            "automatic navigation",
            "automatic tracking",
            "automatic scheduling",
            "automatic calling",
            "automatic messaging",
            "automatic lookup",
            "automatic verification",
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
            "app/src/main/java/com/mongmong/familyshield/" +
                "TravelRouteMeetingPointPauseCoach.kt"
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
            ".safeWord",
            ".callbackScript",
            ".contacts",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "incident.preview",
            "incident.body",
            "OfficialHelpDirectoryBuilder",
            "directory.entries",
            "dialNumber",
            "HttpURLConnection",
            "java.net.URL",
            "OkHttp",
            "Retrofit",
            "WebView",
            "Intent.ACTION_VIEW",
            "Geocoder",
            "LocationManager",
            "FusedLocationProvider",
            "LocationServices",
            "GoogleMap",
            "MapView",
            "NavController",
            "DirectionsApi",
            "RoutePlanner",
            "NavigationClient",
            "TrafficClient",
            "TransitClient",
            "TaxiClient",
            "RideHailingClient",
            "AddressValidator",
            "CourierTrackingClient",
            "MeetingScheduler",
            "EmergencyDispatcher",
            "DocumentValidator",
            "CaseLookupClient",
            "PhoneNumberUtils",
            "Intent.ACTION_CALL",
            "Intent.ACTION_DIAL",
            "ACTION_SENDTO",
            "TelecomManager",
            "TelephonyManager",
            "CallScreeningService",
            "CallLog",
            "SmsManager",
            "ContactsContract",
            "NotificationManager",
            "NotificationListenerService",
            "ClipboardManager",
            "TextWatcher",
            "AutofillManager",
            "CredentialManager",
            "AccountManager",
            "BiometricPrompt",
            "KeyguardManager",
            "KeyStore",
            "javax.crypto",
            "MediaRecorder",
            "AudioRecord",
            "SpeechRecognizer",
            "CameraX",
            "FileProvider",
            "MediaStore",
            "Intent.EXTRA_STREAM",
            "ClipData",
            "Uri.parse",
            "BarcodeScanner",
            "TextRecognition",
            "ImageAnalysis",
            "CameraManager",
            "PackageManager",
            "getInstalledPackages",
            "PackageInstaller",
            "DevicePolicyManager",
            "AccessibilityService",
            "VpnService",
            "UsageStatsManager",
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
            "CashManager",
            "AtmClient",
            "CalendarContract",
            "AlarmManager",
            "WorkManager",
            "JobScheduler",
            "ForegroundService",
            "BroadcastReceiver",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenSource ->
            assertFalse(
                "travel route meeting point model used forbidden source: $forbiddenSource",
                source.contains(forbiddenSource, ignoreCase = true)
            )
        }
        listOf("112", "1394", "1332").forEach { exactOfficialNumber ->
            assertFalse(Regex("(?<!\\d)$exactOfficialNumber(?!\\d)").containsMatchIn(source))
        }
    }

    @Test
    fun boundedUiSliceUsesOnlyCountReadinessAndManualTextShare() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val screenSource = activitySource
            .substringAfter("private fun LinearLayout.travelRouteMeetingPointPauseCoachScreen")
            .substringBefore("private fun LinearLayout.travelRouteMeetingPointPauseCard")
        val shareSource = activitySource
            .substringAfter("private fun shareTravelRouteMeetingPointPauseCoach")
            .substringBefore("private fun ")

        assertFalse(screenSource == activitySource || screenSource.isBlank())
        assertTrue(screenSource.contains("incidents.count()"))
        assertTrue(screenSource.contains("FamilyReadinessBuilder.build(safetyPlan.load())"))
        assertFalse(screenSource.contains("incidents.load"))
        assertFalse(screenSource.contains("currentAnalysis?.input"))
        assertFalse(screenSource.contains("currentAnalysis?.score"))
        assertFalse(screenSource.contains("currentAnalysis?.actionPlan"))
        assertFalse(screenSource.contains("currentAnalysis?.summary"))
        assertFalse(screenSource.contains("IncidentPreviewRedactor"))

        assertFalse(shareSource == activitySource || shareSource.isBlank())
        assertTrue(shareSource.contains("Intent(Intent.ACTION_SEND)"))
        assertTrue(shareSource.contains("type = \"text/plain\""))
        assertTrue(shareSource.contains("putExtra(Intent.EXTRA_TEXT, summary)"))
        assertFalse(shareSource.contains("Intent.EXTRA_STREAM"))
        assertFalse(shareSource.contains("Intent.ACTION_VIEW"))
        assertFalse(shareSource.contains("Uri.parse"))
    }

    @Test
    fun highRiskShowsAllCardsAndRoutesWhileSignalCategoriesRemainDistinct() {
        val highRisk = TravelRouteMeetingPointPauseCoachBuilder.build(
            analysis(RiskLevel.HIGH, ScamSignal.SECRECY),
            readiness(ReadinessLevel.READY)
        )
        assertEquals(TravelRouteMeetingPointPauseCardId.entries, highRisk.recommendedCardIds)
        assertEquals(TravelRouteMeetingPointPauseActionId.entries, highRisk.recommendedActionIds)

        val financial = TravelRouteMeetingPointPauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.URGENT_TRANSFER),
            readiness(ReadinessLevel.READY)
        )
        val official = TravelRouteMeetingPointPauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
            readiness(ReadinessLevel.READY)
        )
        val liveApp = TravelRouteMeetingPointPauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.APP_INSTALL),
            readiness(ReadinessLevel.READY)
        )
        val credential = TravelRouteMeetingPointPauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.CREDENTIALS),
            readiness(ReadinessLevel.READY)
        )
        val family = TravelRouteMeetingPointPauseCoachBuilder.build(
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
        coach: TravelRouteMeetingPointPauseCoach,
        vararg expected: TravelRouteMeetingPointPauseActionId
    ) {
        assertTrue(coach.recommendedActionIds.containsAll(expected.toList()))
    }

    private fun assertNoPrivateValues(
        coach: TravelRouteMeetingPointPauseCoach,
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

    private fun allCopy(coach: TravelRouteMeetingPointPauseCoach): String = buildString {
        appendLine(coach.title)
        appendLine(coach.intro)
        appendLine(coach.modeLabel)
        appendLine(coach.recommendationTitle)
        appendLine(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> appendLine(action.label) }
        coach.cards.forEach { card ->
            appendLine(card.title)
            appendLine(card.movementMeetingPressureSituation)
            appendLine(card.valueFreePauseScript)
            appendLine(card.whyItMatters)
            card.doNotTravelNavigateShareLocationMeetHandoffSayShareAttachOrRecordBoundaries
                .forEach(::appendLine)
            card.routeActions.forEach { action -> appendLine(action.label) }
        }
        coach.privacySafetyNotes.forEach(::appendLine)
        append(coach.familyShareSummary)
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
