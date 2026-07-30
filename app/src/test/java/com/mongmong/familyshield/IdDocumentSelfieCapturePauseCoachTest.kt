package com.mongmong.familyshield

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IdDocumentSelfieCapturePauseCoachTest {
    @Test
    fun noAnalysisCoachIsConservativeAndNeverDeclaresIdentityCaptureStatus() {
        val coach = IdDocumentSelfieCapturePauseCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(IdDocumentSelfieCapturePauseCardId.entries, coach.recommendedCardIds)
        assertEquals(
            IdDocumentSelfieCapturePauseActionId.entries.filterNot { actionId ->
                actionId == IdDocumentSelfieCapturePauseActionId.ANALYSIS_RESULT
            },
            coach.recommendedActionIds
        )
        assertEquals("신분증·셀피 촬영 멈춤 코치", coach.title)
        assertTrue(coach.recommendationTitle.contains("모든"))
        assertTrue(coach.recommendationExplanation.contains("상태를 공식"))
        assertTrue(coach.recommendationExplanation.contains("단정하지 않고"))

        listOf(
            "official ID",
            "verified document",
            "safe selfie",
            "trusted face",
            "liveness passed",
            "document accepted",
            "upload completed",
            "account protected",
            "report submitted",
            "refund guaranteed",
            "problem-free",
            "공식 신분증입니다",
            "검증된 문서입니다",
            "안전한 셀피입니다",
            "얼굴 확인이 완료됐습니다",
            "라이브니스를 통과했습니다",
            "업로드됐습니다",
            "제출됐습니다",
            "촬영됐습니다",
            "스캔됐습니다",
            "분석됐습니다",
            "일치했습니다",
            "가림 처리됐습니다",
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
            val coach = IdDocumentSelfieCapturePauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "OTP_VALUE PIN_VALUE PASSWORD_VALUE RESIDENT_ID_VALUE ID_CARD_VALUE " +
                            "PASSPORT_VALUE LICENSE_VALUE DOCUMENT_VALUE ACCOUNT_VALUE " +
                            "CARD_VALUE BANK_VALUE WALLET_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY, "PRIVATE_PLAN_VALUE")
            )

            assertRoutes(
                coach,
                IdDocumentSelfieCapturePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH,
                IdDocumentSelfieCapturePauseActionId.ACCOUNT_LOCKDOWN,
                IdDocumentSelfieCapturePauseActionId.SAFE_PASTE_PRIVACY_COACH,
                IdDocumentSelfieCapturePauseActionId.EXPOSURE_IMPACT_TRIAGE,
                IdDocumentSelfieCapturePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            )
            assertTrue(coach.recommendationExplanation.contains("OTP·PIN·비밀번호"))
            assertNoPrivateValues(
                coach,
                "OTP_VALUE",
                "PIN_VALUE",
                "PASSWORD_VALUE",
                "RESIDENT_ID_VALUE",
                "ID_CARD_VALUE",
                "PASSPORT_VALUE",
                "LICENSE_VALUE",
                "DOCUMENT_VALUE",
                "ACCOUNT_VALUE",
                "CARD_VALUE",
                "BANK_VALUE",
                "WALLET_VALUE",
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
            val coach = IdDocumentSelfieCapturePauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "PHONE_VALUE NEW_NUMBER_VALUE OLD_NUMBER_VALUE CONTACT_VALUE " +
                            "CHANNEL_VALUE PROFILE_VALUE CALL_STATE SMS_STATE ID_VALUE SELFIE_VALUE"
                ),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "SAFE_WORD_VALUE CALLBACK_VALUE CONTACT_LABEL_VALUE"
                )
            )

            assertRoutes(
                coach,
                IdDocumentSelfieCapturePauseActionId.NEW_NUMBER_QUARANTINE_COACH,
                IdDocumentSelfieCapturePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                IdDocumentSelfieCapturePauseActionId.FAMILY_CALLBACK_CARD,
                IdDocumentSelfieCapturePauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH,
                IdDocumentSelfieCapturePauseActionId.FAMILY_SAFE_WORD_RESET_COACH,
                IdDocumentSelfieCapturePauseActionId.SAFE_CALL_SCRIPTS,
                IdDocumentSelfieCapturePauseActionId.FAMILY_ALERT_MESSAGES
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
                "ID_VALUE",
                "SELFIE_VALUE",
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
            val coach = IdDocumentSelfieCapturePauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "STAFF_VALUE AGENCY_VALUE OFFICIAL_NUMBER_VALUE ID_VALUE RESIDENT_VALUE " +
                            "DOCUMENT_VALUE PASSPORT_VALUE LICENSE_VALUE CASE_VALUE REPORT_VALUE RECEIPT_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertRoutes(
                coach,
                IdDocumentSelfieCapturePauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
                IdDocumentSelfieCapturePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                IdDocumentSelfieCapturePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                IdDocumentSelfieCapturePauseActionId.FAMILY_ALERT_MESSAGES,
                IdDocumentSelfieCapturePauseActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                IdDocumentSelfieCapturePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                IdDocumentSelfieCapturePauseActionId.TRUSTED_HELPER_REVIEW,
                IdDocumentSelfieCapturePauseActionId.OFFICIAL_HELP_DIRECTORY,
                IdDocumentSelfieCapturePauseActionId.REPORT_PREP
            )
            assertTrue(coach.recommendationExplanation.contains("기관·직원 신원·공식 번호"))
            assertTrue(
                coach.recommendationExplanation.contains(
                    "전화·브라우저·문자·신고 제출을 하지 않습니다"
                )
            )
            assertNoPrivateValues(
                coach,
                "STAFF_VALUE",
                "AGENCY_VALUE",
                "OFFICIAL_NUMBER_VALUE",
                "ID_VALUE",
                "RESIDENT_VALUE",
                "DOCUMENT_VALUE",
                "PASSPORT_VALUE",
                "LICENSE_VALUE",
                "CASE_VALUE",
                "REPORT_VALUE",
                "RECEIPT_VALUE"
            )
            listOf(
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
    fun liveCallAppLinkAndSmishingLeaveSameFlowWithoutCameraFileOrDeviceClaims() {
        listOf(
            ScamSignal.LIVE_CALL_CONTROL,
            ScamSignal.APP_INSTALL,
            ScamSignal.LINK,
            ScamSignal.PARCEL_INVITATION_SMISHING
        ).forEach { signal ->
            val coach = IdDocumentSelfieCapturePauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "CALL_STATE APP_VALUE PACKAGE_VALUE DEVICE_VALUE SETTINGS_VALUE " +
                            "URL_VALUE DOMAIN_VALUE CAMERA_STATE FILE_VALUE GALLERY_VALUE " +
                            "DOCUMENT_VALUE SELFIE_VALUE FACE_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertRoutes(
                coach,
                IdDocumentSelfieCapturePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                IdDocumentSelfieCapturePauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH,
                IdDocumentSelfieCapturePauseActionId.REMOTE_ACCESS_CUTOFF_COACH,
                IdDocumentSelfieCapturePauseActionId.ACCOUNT_LOCKDOWN,
                IdDocumentSelfieCapturePauseActionId.EXPOSURE_IMPACT_TRIAGE,
                IdDocumentSelfieCapturePauseActionId.PRIVACY_CENTER
            )
            assertTrue(coach.recommendationExplanation.contains("통화 상태·앱·패키지·기기"))
            assertTrue(coach.recommendationExplanation.contains("카메라·파일 상태를 읽거나"))
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
                "CAMERA_STATE",
                "FILE_VALUE",
                "GALLERY_VALUE",
                "DOCUMENT_VALUE",
                "SELFIE_VALUE",
                "FACE_VALUE"
            )
            listOf(
                "통화를 종료했습니다",
                "앱을 제거했습니다",
                "설정을 변경했습니다",
                "원격 연결을 종료했습니다",
                "기기를 정리했습니다",
                "카메라를 열었습니다",
                "문서를 스캔했습니다",
                "얼굴을 분석했습니다"
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
            val coach = IdDocumentSelfieCapturePauseCoachBuilder.build(
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
                IdDocumentSelfieCapturePauseActionId.MONEY_TRANSFER_PAUSE_GATE,
                IdDocumentSelfieCapturePauseActionId.GIFT_CARD_CODE_PAUSE_COACH,
                IdDocumentSelfieCapturePauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH,
                IdDocumentSelfieCapturePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH,
                IdDocumentSelfieCapturePauseActionId.ACCOUNT_LOCKDOWN,
                IdDocumentSelfieCapturePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                IdDocumentSelfieCapturePauseActionId.TRUSTED_HELPER_REVIEW,
                IdDocumentSelfieCapturePauseActionId.OFFICIAL_HELP_DIRECTORY,
                IdDocumentSelfieCapturePauseActionId.REPORT_PREP
            )
            assertTrue(coach.recommendationExplanation.contains("결제·은행"))
            assertTrue(coach.recommendationExplanation.contains("신분·얼굴 검증 행동을 수행하지"))
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
                "문서를 검증했습니다"
            ).forEach { claim -> assertFalse(allCopy(coach).contains(claim)) }
        }
    }

    @Test
    fun savedIncidentUsesOnlyClampedPresenceCategory() {
        val baseAnalysis = analysis(RiskLevel.MEDIUM, ScamSignal.LIVE_CALL_CONTROL)
        val noIncident = IdDocumentSelfieCapturePauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 0
        )
        val negativeIncident = IdDocumentSelfieCapturePauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = -41
        )
        val oneIncident = IdDocumentSelfieCapturePauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 1
        )
        val manyIncidents = IdDocumentSelfieCapturePauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = Int.MAX_VALUE
        )

        assertEquals(noIncident, negativeIncident)
        assertEquals(oneIncident, manyIncidents)
        assertNotEquals(noIncident.recommendedActionIds, oneIncident.recommendedActionIds)
        assertTrue(
            IdDocumentSelfieCapturePauseCardId.CHOOSE_OFFICIAL_HELP_REPORT_AND_FOLLOW_UP_ROUTES in
                oneIncident.recommendedCardIds
        )
        assertRoutes(
            oneIncident,
            IdDocumentSelfieCapturePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
            IdDocumentSelfieCapturePauseActionId.EVIDENCE_KIT,
            IdDocumentSelfieCapturePauseActionId.INCIDENT_TIMELINE_BRIEFING,
            IdDocumentSelfieCapturePauseActionId.FOLLOW_UP_CHECK_IN_PLANNER
        )
        assertTrue(oneIncident.recommendationExplanation.contains("개수 범주만"))
        assertTrue(oneIncident.recommendationExplanation.contains("사건 내용이나 미리보기를 읽지"))
    }

    @Test
    fun readinessUsesOnlyLevelAndReadyNeverMakesIdSelfieDocumentOrAccountSafe() {
        listOf(ReadinessLevel.LOW, ReadinessLevel.IN_PROGRESS).forEach { level ->
            val coach = IdDocumentSelfieCapturePauseCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
                readiness(level, "READINESS_PRIVATE_LITERAL")
            )

            assertRoutes(
                coach,
                IdDocumentSelfieCapturePauseActionId.SAFETY_PLAN,
                IdDocumentSelfieCapturePauseActionId.FAMILY_SAFE_WORD_RESET_COACH,
                IdDocumentSelfieCapturePauseActionId.FAMILY_READINESS_ROADMAP,
                IdDocumentSelfieCapturePauseActionId.FAMILY_SAFETY_DRILL_COACH,
                IdDocumentSelfieCapturePauseActionId.SAFE_CALL_SCRIPTS
            )
            assertFalse(allCopy(coach).contains("READINESS_PRIVATE_LITERAL"))
        }

        val readyCoach = IdDocumentSelfieCapturePauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
            readiness(ReadinessLevel.READY, "READY_PRIVATE_LITERAL")
        )
        listOf(
            "신분증",
            "문서",
            "셀피",
            "얼굴",
            "라이브니스",
            "계정",
            "요청",
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
        val coach = IdDocumentSelfieCapturePauseCoachBuilder.build(
            null,
            readiness(ReadinessLevel.READY)
        )

        assertEquals(IdDocumentSelfieCapturePauseCardId.entries.size, coach.cards.size)
        assertEquals(IdDocumentSelfieCapturePauseCardId.entries, coach.cards.map { card -> card.id })
        assertEquals(
            (1..IdDocumentSelfieCapturePauseCardId.entries.size).toList(),
            coach.cards.map { card -> card.displayOrder }
        )
        coach.cards.forEach { card ->
            assertEquals(card.id.displayOrder, card.displayOrder)
            assertTrue(card.title.isNotBlank())
            assertTrue(card.idDocumentSelfiePressureSituation.isNotBlank())
            assertTrue(card.valueFreePauseScript.isNotBlank())
            assertTrue(card.whyItMatters.isNotBlank())
            assertTrue(
                card
                    .doNotPhotographScanUploadReadIdFaceAccountSayShareAttachOrRecordBoundaries
                    .size >= 2
            )
            assertTrue(
                card
                    .doNotPhotographScanUploadReadIdFaceAccountSayShareAttachOrRecordBoundaries
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
                    action.id in IdDocumentSelfieCapturePauseActionId.entries
                }
            )
        }
        assertEquals(
            coach.recommendedActions.size,
            coach.recommendedActions.map { action -> action.id }.distinct().size
        )
        val allActions = IdDocumentSelfieCapturePauseCoachBuilder.build(
            analysis(RiskLevel.HIGH, ScamSignal.SECRECY),
            readiness(ReadinessLevel.READY)
        ).recommendedActions
        assertEquals(IdDocumentSelfieCapturePauseActionId.entries, allActions.map { action -> action.id })
        assertEquals(allActions.size, allActions.map { action -> action.label }.distinct().size)
    }

    @Test
    fun everyActionIdMapsToAnExistingMainActivityFlow() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val routerSource = functionBlock(
            activitySource,
            "private fun openIdDocumentSelfieCapturePauseAction"
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
            "SIM_NUMBER_PORT_OUT_PAUSE_COACH" to
                "show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)",
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
                "openHelpDirectory(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(IdDocumentSelfieCapturePauseActionId.entries.size, expectedActionRoutes.size)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "action did not map to existing flow: $actionId",
                normalizedRouter.contains(
                    "IdDocumentSelfieCapturePauseActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            IdDocumentSelfieCapturePauseActionId.entries.size,
            Regex("IdDocumentSelfieCapturePauseActionId\\.[A-Z_]+\\s*->")
                .findAll(routerSource)
                .count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(routerSource).count())
    }

    @Test
    fun familyShareSummaryIsRebuiltOnlyFromGenericFixedCardAndRouteLabels() {
        val coach = IdDocumentSelfieCapturePauseCoachBuilder.build(
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
                        "SECURITY_ANSWER ID_CARD_VALUE RESIDENT_ID_VALUE PASSPORT_VALUE " +
                        "LICENSE_VALUE HEALTH_CARD_VALUE BANKBOOK_VALUE DOCUMENT_VALUE " +
                        "PHOTO_VALUE VIDEO_VALUE SELFIE_VALUE FACE_VALUE LIVENESS_VALUE " +
                        "BIOMETRIC_VALUE GALLERY_VALUE CAMERA_VALUE URL_VALUE DOMAIN_VALUE " +
                        "APP_VALUE PACKAGE_VALUE FILE_VALUE MEDIA_VALUE URI_VALUE PATH_VALUE " +
                        "QR_VALUE BARCODE_VALUE CODE_VALUE " +
                        "ACCOUNT_NUMBER CARD_NUMBER BANK_VALUE WALLET_VALUE TRANSACTION_VALUE " +
                        "PAYMENT_VALUE AMOUNT_VALUE CASH_SERIAL OFFICIAL_NUMBER DIRECTORY_VALUE " +
                        "AGENCY_NAME HELPER_NAME LAWYER_NAME INVESTIGATOR_NAME STAFF_NAME CASE_ID " +
                        "REPORT_ID SUBMISSION_STATUS RECEIPT_ID SCREENSHOT_VALUE IMAGE_VALUE " +
                        "OCR_VALUE AUDIO_VALUE TRANSCRIPT_VALUE CLIPBOARD_VALUE AUTOFILL_VALUE " +
                        "ACCOUNT_STATE LOGIN_STATE CALL_STATE SMS_STATE NOTIFICATION_STATE " +
                        "RECIPIENT_NAME VERIFICATION_RESULT RESULT_STATUS UPLOADED_STATUS " +
                        "SUBMITTED_STATUS ACCEPTED_STATUS VALIDATED_STATUS CAPTURED_STATUS " +
                        "SCANNED_STATUS ANALYZED_STATUS MATCHED_STATUS LIVENESS_PASSED_STATUS " +
                        "SANITIZED_STATUS REDACTED_STATUS BLOCKED_STATUS " +
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
            appendLine("[신분증·셀피 촬영 멈춤 코치]")
            appendLine("민감한 신분·얼굴 값을 받지 않고 신원 확인 압박을 멈춘 채 직접 읽을 문장을 고르는 로컬 수동 안내입니다.")
            appendLine()
            appendLine("추천 값 없는 신분증·셀피 촬영 멈춤 카드")
            coach.recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            coach.recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 실제 신분증·주민 식별값·여권·면허증·문서·사진·영상·셀피·얼굴·계정 값은 보내지 않습니다.")
            append("- 이 안내는 촬영·스캔·업로드·확인·검증·제출·계정·결제·신고 행동을 수행하거나 결과를 보장하지 않습니다.")
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
            "ID_CARD_VALUE",
            "RESIDENT_ID_VALUE",
            "PASSPORT_VALUE",
            "LICENSE_VALUE",
            "HEALTH_CARD_VALUE",
            "BANKBOOK_VALUE",
            "DOCUMENT_VALUE",
            "PHOTO_VALUE",
            "VIDEO_VALUE",
            "SELFIE_VALUE",
            "FACE_VALUE",
            "LIVENESS_VALUE",
            "BIOMETRIC_VALUE",
            "GALLERY_VALUE",
            "CAMERA_VALUE",
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
            "UPLOADED_STATUS",
            "SUBMITTED_STATUS",
            "ACCEPTED_STATUS",
            "VALIDATED_STATUS",
            "CAPTURED_STATUS",
            "SCANNED_STATUS",
            "ANALYZED_STATUS",
            "MATCHED_STATUS",
            "LIVENESS_PASSED_STATUS",
            "SANITIZED_STATUS",
            "REDACTED_STATUS",
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
        val first = IdDocumentSelfieCapturePauseCoachBuilder.build(
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
        val second = IdDocumentSelfieCapturePauseCoachBuilder.build(
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
            IdDocumentSelfieCapturePauseCoachBuilder.build(
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
    fun privacyCopyStatesManualLocalStatelessAndEveryIdentityCaptureBoundary() {
        val copy = allCopy(
            IdDocumentSelfieCapturePauseCoachBuilder.build(
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
            "신분증·주민등록증·주민 식별값·여권·면허증·건강보험증·통장·문서",
            "사진·영상·셀피·얼굴·라이브니스·생존 확인·생체·갤러리·카메라·스크린샷",
            "OCR·QR·바코드·미디어·URI·경로 값",
            "카메라·CameraX·Camera2·마이크·MediaRecorder·AudioRecord",
            "OCR·텍스트 인식·QR·바코드 스캔·이미지 분석",
            "얼굴 인식·얼굴 감지·라이브니스·생존 확인·생체 인증",
            "셀피/영상 촬영·문서 스캔을 사용하지 않습니다",
            "파일·문서·사진·영상·미디어·갤러리·스크린샷·저장소",
            "파일 선택기·촬영·녹음·전사·첨부·업로드·동기화·스트림·URI 공유",
            "신분증·주민등록증·여권·면허증·건강보험증·통장·문서·얼굴·라이브니스",
            "검사·분석·일치·확인·검증·유효성 판정·가림·정제하지 않습니다",
            "전화·다이얼·발신·통화·종료·선별·차단·통화 상태·통화 기록·발신자 표시",
            "문자·SMS·받는 사람 선택",
            "연락처·주소록·알림을 읽기·쓰기·편집·선택·감시·필터링하지 않습니다",
            "네트워크·웹 탐색·브라우저·웹뷰·외부 링크 열기",
            "공식 정보·번호·디렉터리·기관·직원·신원·문서·사건",
            "전화·문자·신고·출동을 요청·제출하지 않습니다",
            "클립보드·텍스트 감시기·자동완성·자격 증명 API",
            "계정·로그인·인증·생체 인증·키가드·키 저장소·암호화 API",
            "결제·은행·ATM·NFC·지갑·블록체인·상품권 동작",
            "환급·차지백·취소·동결·차단·추적·회복을 수행하지 않습니다",
            "전화 통신·SIM·구독·통신사 API",
            "전화번호·SIM/eSIM/USIM·IMEI·ICCID·IMSI·기기 ID·일련값",
            "앱·패키지·설치 앱 목록·앱 이름·권한·기기·설정 상태",
            "설치·제거·비활성화·강제 종료·권한 허용·철회·설정 변경",
            "IMEI·ICCID·IMSI·기기 ID·일련값을 읽지 않고",
            "기기 관리자·접근성 서비스·VPN·사용 통계·알림 수신 서비스",
            "공식·실제·안전·보안·검증·유효·접수·제출·수신·해결·완료",
            "파일·스트림·URI를 붙이거나 자동 발송하지 않습니다",
            "새 Android 권한",
            "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러·브로드캐스트 수신기",
            "카메라·파일·미디어·문서·신분증·얼굴·생체·OCR·QR·바코드",
            "결과 상태를 수정하지 않습니다"
        ).forEach { boundary ->
            assertTrue("privacy copy omitted $boundary", copy.contains(boundary))
        }
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndAutomaticProtectionClaims() {
        val copy = allCopy(
            IdDocumentSelfieCapturePauseCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.READY),
                incidentCount = 1
            )
        )

        listOf(
            "official ID",
            "verified document",
            "safe selfie",
            "trusted face",
            "liveness passed",
            "document accepted",
            "account protected",
            "refund guaranteed",
            "report submitted",
            "problem-free",
            "공식 신분증입니다",
            "검증된 문서입니다",
            "안전한 셀피입니다",
            "신뢰할 수 있는 얼굴입니다",
            "라이브니스를 통과했습니다",
            "업로드됐습니다",
            "제출됐습니다",
            "접수됐습니다",
            "촬영됐습니다",
            "스캔됐습니다",
            "분석됐습니다",
            "일치했습니다",
            "가림 처리됐습니다",
            "정제됐습니다",
            "취소됐습니다",
            "복구됐습니다",
            "환급됐습니다",
            "계정이 보호됐습니다",
            "신고가 제출됐습니다",
            "문제없습니다",
            "자동 신분 확인",
            "자동 문서 검증",
            "자동 얼굴 분석",
            "자동 라이브니스 확인",
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
            "app/src/main/java/com/mongmong/familyshield/IdDocumentSelfieCapturePauseCoach.kt"
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
            "IdentityVerifier",
            "DocumentValidator",
            "DocumentScanner",
            "LivenessDetector",
            "FaceDetector",
            "FaceRecognition",
            "BiometricPrompt",
            "CameraManager",
            "CameraDevice",
            "CameraX",
            "ImageCapture",
            "ImageAnalysis",
            "MediaRecorder",
            "AudioRecord",
            "TextRecognition",
            "BarcodeScanner",
            "QRCodeReader",
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
            "ACTION_IMAGE_CAPTURE",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "Intent.EXTRA_STREAM",
            "ClipData",
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
                "ID/selfie model used forbidden implementation source: $forbiddenSource",
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
            "private fun LinearLayout.idDocumentSelfieCapturePauseCoachScreen"
        )
        val cardSource = functionBlock(
            activitySource,
            "private fun LinearLayout.idDocumentSelfieCapturePauseCard"
        )
        val routerSource = functionBlock(
            activitySource,
            "private fun openIdDocumentSelfieCapturePauseAction"
        )
        val shareSource = functionBlock(
            activitySource,
            "private fun shareIdDocumentSelfieCapturePauseCoach"
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
            "card.idDocumentSelfiePressureSituation",
            "card.valueFreePauseScript",
            "card.whyItMatters",
            "card.doNotPhotographScanUploadReadIdFaceAccountSayShareAttachOrRecordBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedField -> assertTrue(cardSource.contains(renderedField)) }

        assertTrue(screenSource.contains("shareIdDocumentSelfieCapturePauseCoach(coach.familyShareSummary)"))
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
            "CameraManager",
            "CameraX",
            "ImageCapture",
            "ImageAnalysis",
            "MediaRecorder",
            "AudioRecord",
            "TextRecognition",
            "BarcodeScanner",
            "QRCodeReader",
            "FaceDetector",
            "FaceRecognition",
            "LivenessDetector",
            "BiometricPrompt",
            "IdentityVerifier",
            "DocumentValidator",
            "DocumentScanner",
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
            "ACTION_IMAGE_CAPTURE",
            "ACTION_PICK",
            "ACTION_GET_CONTENT",
            "ACTION_OPEN_DOCUMENT",
            "ClipData",
            "prefs.edit(",
            "safetyPlan.save(",
            "incidents.load(",
            "incidents.add(",
            "incidents.clear("
        ).forEach { forbiddenApi ->
            assertFalse(
                "bounded ID/selfie UI used forbidden implementation: $forbiddenApi",
                boundedImplementationSource.contains(forbiddenApi, ignoreCase = true)
            )
        }
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(routerSource).count())
        assertFalse((screenSource + cardSource).contains("openHelpDirectory("))
    }

    @Test
    fun highRiskShowsAllCardsAndAllExistingRoutes() {
        val coach = IdDocumentSelfieCapturePauseCoachBuilder.build(
            analysis(RiskLevel.HIGH, ScamSignal.SECRECY),
            readiness(ReadinessLevel.READY)
        )

        assertEquals(IdDocumentSelfieCapturePauseCardId.entries, coach.recommendedCardIds)
        assertEquals(IdDocumentSelfieCapturePauseActionId.entries, coach.recommendedActionIds)
        assertEquals(
            IdDocumentSelfieCapturePauseActionId.entries.size,
            coach.recommendedActions.map { action -> action.label }.distinct().size
        )
    }

    @Test
    fun unrelatedSignalCategoriesProduceDistinctDeterministicRecommendations() {
        fun coachFor(signal: ScamSignal) = IdDocumentSelfieCapturePauseCoachBuilder.build(
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
                card.idDocumentSelfiePressureSituation.endsWith("때")
            }
        )
    }

    private fun assertRoutes(
        coach: IdDocumentSelfieCapturePauseCoach,
        vararg expected: IdDocumentSelfieCapturePauseActionId
    ) {
        assertTrue(coach.recommendedActionIds.containsAll(expected.toList()))
    }

    private fun assertNoPrivateValues(
        coach: IdDocumentSelfieCapturePauseCoach,
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

    private fun allCopy(coach: IdDocumentSelfieCapturePauseCoach): String = buildString {
        appendLine(coach.title)
        appendLine(coach.intro)
        appendLine(coach.modeLabel)
        appendLine(coach.recommendationTitle)
        appendLine(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> appendLine(action.label) }
        coach.cards.forEach { card ->
            appendLine(card.title)
            appendLine(card.idDocumentSelfiePressureSituation)
            appendLine(card.valueFreePauseScript)
            appendLine(card.whyItMatters)
            card
                .doNotPhotographScanUploadReadIdFaceAccountSayShareAttachOrRecordBoundaries
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
