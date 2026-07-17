package com.mongmong.familyshield

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GiftCardCodePauseCoachTest {
    @Test
    fun noAnalysisCoachIsConservativeAndNeverDeclaresGiftCardOrOutcomeStatus() {
        val coach = GiftCardCodePauseCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(GiftCardCodePauseCardId.entries, coach.recommendedCardIds)
        assertEquals(
            GiftCardCodePauseActionId.entries.filterNot { actionId ->
                actionId == GiftCardCodePauseActionId.ANALYSIS_RESULT
            },
            coach.recommendedActionIds
        )
        assertTrue(coach.recommendationTitle.contains("모든 상품권·코드 멈춤 카드"))
        assertTrue(coach.recommendationExplanation.contains("상태를 단정하지 말고"))

        val copy = allCopy(coach)
        listOf(
            "valid code",
            "unused code",
            "redeemed code",
            "balance checked",
            "payment cancelled",
            "refund guaranteed",
            "chargeback accepted",
            "account frozen",
            "case accepted",
            "report submitted",
            "problem-free",
            "공식 상품권입니다",
            "안전한 코드입니다",
            "유효한 PIN입니다",
            "사용되지 않은 코드입니다",
            "잔액이 확인됐습니다",
            "상품권이 사용됐습니다",
            "결제가 취소됐습니다",
            "환급이 보장됩니다",
            "차지백이 접수됐습니다",
            "계정이 동결됐습니다",
            "사건이 접수됐습니다",
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
    fun giftCardAndMoneySignalsRecommendPauseFreezeRecoveryPressureAndHelpRoutes() {
        listOf(
            ScamSignal.GIFTICON_PIN,
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.BAIT,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = GiftCardCodePauseCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, signal),
                readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        GiftCardCodePauseCardId.TREAT_GIFT_CARDS_LIKE_MONEY,
                        GiftCardCodePauseCardId.NEVER_REVEAL_CODE_PIN_BARCODE_OR_QR,
                        GiftCardCodePauseCardId.STOP_SPLIT_PURCHASE_INSTRUCTIONS,
                        GiftCardCodePauseCardId.NO_FEE_REFUND_FINE_OR_EMERGENCY_PAYMENT,
                        GiftCardCodePauseCardId.VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP
                    )
                )
            )
            assertRoutes(
                coach,
                GiftCardCodePauseActionId.MONEY_TRANSFER_PAUSE_GATE,
                GiftCardCodePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH,
                GiftCardCodePauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH,
                GiftCardCodePauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
                GiftCardCodePauseActionId.ACCOUNT_LOCKDOWN,
                GiftCardCodePauseActionId.EXPOSURE_IMPACT_TRIAGE,
                GiftCardCodePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                GiftCardCodePauseActionId.TRUSTED_HELPER_REVIEW,
                GiftCardCodePauseActionId.OFFICIAL_HELP_DIRECTORY,
                GiftCardCodePauseActionId.REPORT_PREP
            )
            if (
                signal == ScamSignal.BAIT ||
                signal == ScamSignal.SAFE_ACCOUNT_FRAUD ||
                signal == ScamSignal.LOAN_REFINANCE_FEE
            ) {
                assertEquals(
                    "환급·회복 약속에 상품권을 쓰지 않기",
                    coach.recommendationTitle
                )
            }
            listOf(
                "상품권을 사용했습니다",
                "잔액을 확인했습니다",
                "코드를 차단했습니다",
                "결제를 취소했습니다",
                "환급됐습니다",
                "차지백이 접수됐습니다",
                "피해금을 회수했습니다",
                "회복됐습니다"
            ).forEach { unsafeOutcome -> assertFalse(allCopy(coach).contains(unsafeOutcome)) }
        }
    }

    @Test
    fun credentialSignalsRecommendSecretPrivacyAccountRemoteAndExposureWithoutValues() {
        listOf(
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = GiftCardCodePauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "GIFT_CODE_VALUE OTP_VALUE PIN_VALUE PASSWORD_VALUE " +
                            "SECURITY_ANSWER_VALUE ACCOUNT_VALUE CARD_VALUE BANK_VALUE " +
                            "WALLET_VALUE"
                ),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "SAFE_WORD_VALUE CALLBACK_VALUE CONTACT_VALUE"
                )
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        GiftCardCodePauseCardId.NEVER_REVEAL_CODE_PIN_BARCODE_OR_QR,
                        GiftCardCodePauseCardId.LEAVE_LINK_APP_CALL_AND_SCREEN_SHARE
                    )
                )
            )
            assertRoutes(
                coach,
                GiftCardCodePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH,
                GiftCardCodePauseActionId.SAFE_PASTE_PRIVACY_COACH,
                GiftCardCodePauseActionId.ACCOUNT_LOCKDOWN,
                GiftCardCodePauseActionId.REMOTE_ACCESS_CUTOFF_COACH,
                GiftCardCodePauseActionId.EXPOSURE_IMPACT_TRIAGE
            )
            assertNoPrivateValues(
                coach,
                "GIFT_CODE_VALUE",
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
            )
        }
    }

    @Test
    fun impersonationAndSecrecyRecommendPressureExitFamilyEvidenceAndHelpWithoutOfficialValues() {
        listOf(ScamSignal.IMPERSONATION, ScamSignal.SECRECY).forEach { signal ->
            val coach = GiftCardCodePauseCoachBuilder.build(
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
                GiftCardCodePauseCardId.NO_FEE_REFUND_FINE_OR_EMERGENCY_PAYMENT in
                    coach.recommendedCardIds
            )
            assertRoutes(
                coach,
                GiftCardCodePauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
                GiftCardCodePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                GiftCardCodePauseActionId.FAMILY_ALERT_MESSAGES,
                GiftCardCodePauseActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                GiftCardCodePauseActionId.TRUSTED_HELPER_REVIEW,
                GiftCardCodePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                GiftCardCodePauseActionId.OFFICIAL_HELP_DIRECTORY,
                GiftCardCodePauseActionId.REPORT_PREP
            )
            assertTrue(coach.recommendationExplanation.contains("공식 번호·디렉터리 값"))
            assertNoPrivateValues(
                coach,
                "AGENCY_VALUE",
                "DIRECTORY_VALUE",
                "OFFICIAL_NUMBER_VALUE",
                "CASE_VALUE",
                "HELPER_VALUE",
                "LAWYER_VALUE",
                "INVESTIGATOR_VALUE",
                "PLAN_PRIVATE_VALUE"
            )
        }
    }

    @Test
    fun recoveryPromiseContextsUseExistingSignalsAndNeverDeclareRefundChargebackOrRecovery() {
        listOf(
            ScamSignal.BAIT,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = GiftCardCodePauseCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, signal),
                readiness(ReadinessLevel.READY)
            )

            assertRoutes(
                coach,
                GiftCardCodePauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH,
                GiftCardCodePauseActionId.MONEY_TRANSFER_PAUSE_GATE,
                GiftCardCodePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH,
                GiftCardCodePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                GiftCardCodePauseActionId.TRUSTED_HELPER_REVIEW,
                GiftCardCodePauseActionId.OFFICIAL_HELP_DIRECTORY,
                GiftCardCodePauseActionId.REPORT_PREP
            )
            listOf(
                "회복이 확인됐습니다",
                "환급이 승인됐습니다",
                "차지백이 접수됐습니다",
                "환불이 보장됩니다",
                "피해금을 되찾았습니다"
            ).forEach { claim -> assertFalse(allCopy(coach).contains(claim)) }
        }
    }

    @Test
    fun linkSmishingAppInstallAndLiveCallLeaveSameSessionWithoutTechnicalClaims() {
        listOf(
            ScamSignal.LINK,
            ScamSignal.PARCEL_INVITATION_SMISHING,
            ScamSignal.APP_INSTALL,
            ScamSignal.LIVE_CALL_CONTROL
        ).forEach { signal ->
            val coach = GiftCardCodePauseCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, signal),
                readiness(ReadinessLevel.READY)
            )

            assertTrue(
                GiftCardCodePauseCardId.LEAVE_LINK_APP_CALL_AND_SCREEN_SHARE in
                    coach.recommendedCardIds
            )
            assertRoutes(
                coach,
                GiftCardCodePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                GiftCardCodePauseActionId.REMOTE_ACCESS_CUTOFF_COACH,
                GiftCardCodePauseActionId.EXPOSURE_IMPACT_TRIAGE,
                GiftCardCodePauseActionId.SAFE_PASTE_PRIVACY_COACH,
                GiftCardCodePauseActionId.PRIVACY_CENTER
            )
            assertTrue(coach.recommendationExplanation.contains("같은 링크·앱·통화·화면공유"))
            listOf(
                "URL 검증 완료",
                "링크 검사가 끝났습니다",
                "설치 앱 목록을 확인했습니다",
                "악성 앱을 제거했습니다",
                "원격 연결을 종료했습니다",
                "기기를 정리했습니다",
                "상품권 잔액을 확인했습니다",
                "코드를 사용했습니다"
            ).forEach { unsafeClaim -> assertFalse(allCopy(coach).contains(unsafeClaim)) }
        }
    }

    @Test
    fun familyEmergencyDeepfakeAndNumberChangeUseOnlyValueFreeFamilyRoutes() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val coach = GiftCardCodePauseCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal, input = "RAW_CONTACT_VALUE"),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "CONTACT_LABEL PHONE_VALUE SAFE_WORD_VALUE CALLBACK_VALUE"
                )
            )

            assertRoutes(
                coach,
                GiftCardCodePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                GiftCardCodePauseActionId.FAMILY_CALLBACK_CARD,
                GiftCardCodePauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH,
                GiftCardCodePauseActionId.NEW_NUMBER_QUARANTINE_COACH,
                GiftCardCodePauseActionId.FAMILY_SAFE_WORD_RESET_COACH,
                GiftCardCodePauseActionId.SAFE_CALL_SCRIPTS,
                GiftCardCodePauseActionId.FAMILY_ALERT_MESSAGES
            )
            assertTrue(coach.recommendationExplanation.contains("연락처·안전단어·콜백 값을 사용하지"))
            assertNoPrivateValues(
                coach,
                "RAW_CONTACT_VALUE",
                "CONTACT_LABEL",
                "PHONE_VALUE",
                "SAFE_WORD_VALUE",
                "CALLBACK_VALUE"
            )
        }
    }

    @Test
    fun savedIncidentUsesOnlyClampedPresenceCategory() {
        val baseAnalysis = analysis(RiskLevel.MEDIUM, ScamSignal.LINK)
        val noIncident = GiftCardCodePauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 0
        )
        val negativeIncident = GiftCardCodePauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = -91
        )
        val oneIncident = GiftCardCodePauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 1
        )
        val manyIncidents = GiftCardCodePauseCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = Int.MAX_VALUE
        )

        assertEquals(noIncident, negativeIncident)
        assertEquals(oneIncident, manyIncidents)
        assertNotEquals(noIncident.recommendedActionIds, oneIncident.recommendedActionIds)
        assertTrue(
            GiftCardCodePauseCardId.VALUE_FREE_EVIDENCE_HELP_AND_FOLLOW_UP in
                oneIncident.recommendedCardIds
        )
        assertRoutes(
            oneIncident,
            GiftCardCodePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
            GiftCardCodePauseActionId.EVIDENCE_KIT,
            GiftCardCodePauseActionId.FOLLOW_UP_CHECK_IN_PLANNER
        )
        assertTrue(oneIncident.recommendationExplanation.contains("개수 범주만"))
        assertTrue(oneIncident.recommendationExplanation.contains("사건 내용이나 미리보기는 읽지"))
    }

    @Test
    fun readinessUsesOnlyLevelAndReadyNeverMakesGiftCardCodeOrRequestSafe() {
        listOf(ReadinessLevel.LOW, ReadinessLevel.IN_PROGRESS).forEach { level ->
            val coach = GiftCardCodePauseCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
                readiness(level, "READINESS_PRIVATE_LITERAL")
            )

            assertRoutes(
                coach,
                GiftCardCodePauseActionId.SAFETY_PLAN,
                GiftCardCodePauseActionId.FAMILY_SAFE_WORD_RESET_COACH,
                GiftCardCodePauseActionId.FAMILY_READINESS_ROADMAP,
                GiftCardCodePauseActionId.FAMILY_SAFETY_DRILL_COACH,
                GiftCardCodePauseActionId.SAFE_CALL_SCRIPTS
            )
            assertFalse(allCopy(coach).contains("READINESS_PRIVATE_LITERAL"))
        }

        val readyCoach = GiftCardCodePauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
            readiness(ReadinessLevel.READY, "READY_PRIVATE_LITERAL")
        )
        assertTrue(
            readyCoach.recommendationExplanation.contains(
                "상품권·교환권·코드·PIN·바코드·QR 요청이나 사람·기관·가족 위급 " +
                    "주장이 실제이거나 안전하다는 뜻이 아닙니다"
            )
        )
        assertFalse(allCopy(readyCoach).contains("READY_PRIVATE_LITERAL"))
    }

    @Test
    fun cardsAreCompleteStableAndEveryActionMapsToAnExistingFlow() {
        val coach = GiftCardCodePauseCoachBuilder.build(
            null,
            readiness(ReadinessLevel.READY)
        )

        assertEquals(GiftCardCodePauseCardId.entries.size, coach.cards.size)
        assertEquals(GiftCardCodePauseCardId.entries, coach.cards.map { card -> card.id })
        assertEquals(
            (1..GiftCardCodePauseCardId.entries.size).toList(),
            coach.cards.map { card -> card.displayOrder }
        )
        coach.cards.forEach { card ->
            assertEquals(card.id.displayOrder, card.displayOrder)
            assertTrue(card.title.isNotBlank())
            assertTrue(card.codePressureScenario.isNotBlank())
            assertTrue(card.valueFreeActionSentence.isNotBlank())
            assertTrue(card.whyItMatters.isNotBlank())
            assertTrue(card.doNotBuyShareScanRedeemOrAttachBoundaries.size >= 2)
            assertTrue(card.doNotBuyShareScanRedeemOrAttachBoundaries.all { it.isNotBlank() })
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
            .substringAfter("private fun openGiftCardCodePauseAction")
            .substringBefore("private fun shareGiftCardCodePauseCoach")
            .replace(Regex("\\s+"), " ")
        val expectedRoutes = linkedMapOf(
            GiftCardCodePauseActionId.QUICK_STOP to "show(Screen.QUICK_STOP)",
            GiftCardCodePauseActionId.CHECKER to "show(Screen.CHECKER)",
            GiftCardCodePauseActionId.ANALYSIS_RESULT to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)",
            GiftCardCodePauseActionId.SAFETY_PLAN to "show(Screen.PLAN)",
            GiftCardCodePauseActionId.FAMILY_READINESS_ROADMAP to
                "show(Screen.FAMILY_READINESS_ROADMAP)",
            GiftCardCodePauseActionId.FAMILY_SAFETY_DRILL_COACH to "openDrill()",
            GiftCardCodePauseActionId.SAFE_PASTE_PRIVACY_COACH to
                "show(Screen.SAFE_PASTE_PRIVACY_COACH)",
            GiftCardCodePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS to
                "show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)",
            GiftCardCodePauseActionId.FAMILY_CALLBACK_CARD to
                "openFamilyCallbackCard(currentAnalysis)",
            GiftCardCodePauseActionId.FAMILY_SAFE_WORD_RESET_COACH to
                "show(Screen.FAMILY_SAFE_WORD_RESET_COACH)",
            GiftCardCodePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH to
                "show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)",
            GiftCardCodePauseActionId.SAFE_CALL_SCRIPTS to "show(Screen.SAFE_CALL_SCRIPTS)",
            GiftCardCodePauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH to
                "show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)",
            GiftCardCodePauseActionId.NEW_NUMBER_QUARANTINE_COACH to
                "show(Screen.NEW_NUMBER_QUARANTINE_COACH)",
            GiftCardCodePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH to
                "show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            GiftCardCodePauseActionId.FAMILY_ALERT_MESSAGES to
                "show(Screen.FAMILY_ALERT_MESSAGES)",
            GiftCardCodePauseActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD to
                "show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)",
            GiftCardCodePauseActionId.RECONTACT_BOUNDARY_COACH to
                "show(Screen.RECONTACT_BOUNDARY_COACH)",
            GiftCardCodePauseActionId.TRUSTED_HELPER_REVIEW to
                "show(Screen.TRUSTED_HELPER_REVIEW)",
            GiftCardCodePauseActionId.FAMILY_RESPONSE_ROLE_BOARD to
                "show(Screen.FAMILY_RESPONSE_ROLE_BOARD)",
            GiftCardCodePauseActionId.FAMILY_CALM_DEBRIEF_AGENDA to
                "show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)",
            GiftCardCodePauseActionId.MONEY_TRANSFER_PAUSE_GATE to
                "show(Screen.MONEY_TRANSFER_PAUSE_GATE)",
            GiftCardCodePauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH to
                "show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)",
            GiftCardCodePauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH to
                "show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)",
            GiftCardCodePauseActionId.ACCOUNT_LOCKDOWN to "show(Screen.ACCOUNT_LOCKDOWN)",
            GiftCardCodePauseActionId.REMOTE_ACCESS_CUTOFF_COACH to
                "show(Screen.REMOTE_ACCESS_CUTOFF_COACH)",
            GiftCardCodePauseActionId.EXPOSURE_IMPACT_TRIAGE to
                "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            GiftCardCodePauseActionId.PRIVACY_CENTER to "show(Screen.PRIVACY_CENTER)",
            GiftCardCodePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH to
                "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            GiftCardCodePauseActionId.EVIDENCE_KIT to "openEvidenceKit(null)",
            GiftCardCodePauseActionId.OFFICIAL_HELP_DIRECTORY to
                "openHelpDirectory(Screen.GIFT_CARD_CODE_PAUSE_COACH)",
            GiftCardCodePauseActionId.REPORT_PREP to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            GiftCardCodePauseActionId.FOLLOW_UP_CHECK_IN_PLANNER to
                "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            GiftCardCodePauseActionId.SAFETY_FLOW_ROUTER to "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(GiftCardCodePauseActionId.entries, expectedRoutes.keys.toList())
        expectedRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "action did not map to an existing flow: $actionId",
                routerSource.contains("GiftCardCodePauseActionId.$actionId -> $destination")
            )
        }
    }

    @Test
    fun familyShareSummaryIsRebuiltOnlyFromGenericCardsRoutesAndActions() {
        val coach = GiftCardCodePauseCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.GIFTICON_PIN,
                ScamSignal.CREDENTIALS,
                ScamSignal.IMPERSONATION,
                ScamSignal.LINK,
                input =
                    "RAW_INPUT REDACTED_CLUE GIFT_CODE BARCODE_VALUE QR_VALUE SERIAL_VALUE " +
                        "REDEEM_VALUE STORE_NAME BRAND_NAME AGENCY_NAME OFFICIAL_NUMBER " +
                        "CASE_ID HELPER_NAME LAWYER_NAME INVESTIGATOR_NAME OTP_VALUE " +
                        "PIN_VALUE PASSWORD_VALUE SECURITY_ANSWER ACCOUNT_VALUE CARD_VALUE " +
                        "BANK_VALUE WALLET_VALUE TRANSACTION_VALUE RESIDENT_VALUE PHONE_VALUE " +
                        "HTTPS_VALUE DOMAIN_VALUE PACKAGE_VALUE APP_VALUE DEVICE_VALUE " +
                        "FILE_VALUE URI_VALUE SCREENSHOT_VALUE OCR_VALUE AUDIO_VALUE " +
                        "TRANSCRIPT_VALUE CLIPBOARD_VALUE LOGIN_STATE CALL_STATE SMS_STATE " +
                        "NOTIFICATION_STATE RECIPIENT_VALUE REPORT_STATUS SUBMISSION_STATUS " +
                        "RECEIPT_ID PURCHASE_STATUS BALANCE_STATUS REDEEM_STATUS REFUND_STATUS " +
                        "CHARGEBACK_STATUS RECOVERY_STATUS FREEZE_STATUS CANCEL_STATUS " +
                        "BLOCK_STATUS TRACE_STATUS LEGAL_RESULT"
            ),
            readiness = readiness(
                ReadinessLevel.READY,
                "SAFE_WORD_VALUE CALLBACK_VALUE CONTACT_VALUE PLAN_VALUE"
            ),
            incidentCount = 7
        )
        val summary = coach.familyShareSummary

        assertTrue(summary.contains("상품권·코드 멈춤 코치"))
        assertTrue(summary.contains("추천 상품권·코드 멈춤 카드"))
        coach.recommendedCards.forEach { card -> assertTrue(summary.contains(card.title)) }
        coach.recommendedActions.forEach { action -> assertTrue(summary.contains(action.label)) }
        listOf(
            "RAW_INPUT",
            "REDACTED_CLUE",
            "GIFT_CODE",
            "BARCODE_VALUE",
            "QR_VALUE",
            "SERIAL_VALUE",
            "REDEEM_VALUE",
            "STORE_NAME",
            "BRAND_NAME",
            "AGENCY_NAME",
            "OFFICIAL_NUMBER",
            "경찰 긴급 신고",
            "보이스피싱 통합신고·상담",
            "금융감독원",
            "은행·카드사 공식 대표번호",
            "Google Play",
            "App Store",
            "Steam",
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
            "PURCHASE_STATUS",
            "BALANCE_STATUS",
            "REDEEM_STATUS",
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
        val first = GiftCardCodePauseCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.GIFTICON_PIN,
                ScamSignal.CREDENTIALS,
                input = "FIRST_RAW_VALUE",
                score = 1
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "FIRST_PLAN_VALUE"),
            incidentCount = 2
        )
        val second = GiftCardCodePauseCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.GIFTICON_PIN,
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
            GiftCardCodePauseCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.HIGH,
                    ScamSignal.GIFTICON_PIN,
                    ScamSignal.CREDENTIALS,
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
            GiftCardCodePauseCoachBuilder.build(null, readiness(ReadinessLevel.READY))
        )

        listOf(
            "로컬 전용·무상태 수동 안내",
            "입력하라고 요구하지 않습니다",
            "상품권·교환권·기프티콘·선불카드·코드·PIN·바코드·QR·일련번호",
            "촬영·캡처·스캔·해독·검증·사용·잔액 조회·구매",
            "환급·차지백·동결·차단·회수·추적·회복",
            "법률 서비스·자문",
            "지갑·블록체인·가상자산·거래소 추적",
            "결제·은행·계좌·카드 기능 또는 API·NFC",
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
            "파일·문서·영수증·사진·이미지·스크린샷·오디오·미디어·저장소",
            "카메라·화면 캡처·바코드·QR·OCR·이미지 분석·녹음·전사",
            "첨부·스트림·URI 공유",
            "자동 발송하지 않습니다",
            "새 Android 권한",
            "백그라운드 워커·스케줄러",
            "접근성 서비스·기기 관리자·VPN",
            "설치 앱·패키지 목록 조회",
            "새 저장소를 만들지 않고"
        ).forEach { boundary ->
            assertTrue("privacy copy omitted $boundary", copy.contains(boundary))
        }
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndAutomaticProtectionClaims() {
        val copy = allCopy(
            GiftCardCodePauseCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.READY),
                incidentCount = 1
            )
        )

        listOf(
            "valid code",
            "unused",
            "redeemed",
            "balance checked",
            "payment cancelled",
            "refund guaranteed",
            "chargeback accepted",
            "recovered",
            "problem-free",
            "유효한 코드입니다",
            "사용되지 않은 상품권입니다",
            "상품권이 사용됐습니다",
            "잔액이 확인됐습니다",
            "결제가 취소됐습니다",
            "환급이 보장됩니다",
            "차지백이 접수됐습니다",
            "회복이 확인됐습니다",
            "계정이 동결됐습니다",
            "코드가 차단됐습니다",
            "사건이 접수됐습니다",
            "신고가 제출됐습니다",
            "해결됐습니다",
            "완료됐습니다",
            "문제없습니다",
            "자동 상품권 스캔",
            "자동 코드 차단",
            "자동 결제 취소",
            "환급 결과를 보장",
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
            "app/src/main/java/com/mongmong/familyshield/GiftCardCodePauseCoach.kt"
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
            "경찰 긴급 신고",
            "보이스피싱 통합신고·상담",
            "금융감독원",
            "은행·카드사 공식 대표번호",
            "Google Play",
            "App Store",
            "Steam",
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
            "BarcodeScanner",
            "QRCodeReader",
            "TextRecognition",
            "ImageAnalysis",
            "ImageCapture",
            "RefundManager",
            "ChargebackManager",
            "GiftCardManager",
            "BalanceChecker",
            "RedemptionService",
            "WalletConnect",
            "web3",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenSource ->
            assertFalse(
                "gift-card code model used forbidden source: $forbiddenSource",
                source.contains(forbiddenSource)
            )
        }
    }

    @Test
    fun highRiskShowsAllCardsAndRoutesWhileSignalCategoriesRemainDistinct() {
        val highRisk = GiftCardCodePauseCoachBuilder.build(
            analysis(RiskLevel.HIGH, ScamSignal.SECRECY),
            readiness(ReadinessLevel.READY)
        )
        assertEquals(GiftCardCodePauseCardId.entries, highRisk.recommendedCardIds)
        assertEquals(GiftCardCodePauseActionId.entries, highRisk.recommendedActionIds)

        val financial = GiftCardCodePauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.GIFTICON_PIN),
            readiness(ReadinessLevel.READY)
        )
        val link = GiftCardCodePauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.LINK),
            readiness(ReadinessLevel.READY)
        )
        val family = GiftCardCodePauseCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.FAMILY_EMERGENCY),
            readiness(ReadinessLevel.READY)
        )
        assertNotEquals(financial.recommendedActionIds, link.recommendedActionIds)
        assertNotEquals(link.recommendedActionIds, family.recommendedActionIds)
        assertNotEquals(financial.recommendationTitle, family.recommendationTitle)
    }

    private fun assertRoutes(
        coach: GiftCardCodePauseCoach,
        vararg expected: GiftCardCodePauseActionId
    ) {
        assertTrue(coach.recommendedActionIds.containsAll(expected.toList()))
    }

    private fun assertNoPrivateValues(
        coach: GiftCardCodePauseCoach,
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

    private fun allCopy(coach: GiftCardCodePauseCoach): String = buildString {
        appendLine(coach.title)
        appendLine(coach.intro)
        appendLine(coach.modeLabel)
        appendLine(coach.recommendationTitle)
        appendLine(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> appendLine(action.label) }
        coach.cards.forEach { card ->
            appendLine(card.title)
            appendLine(card.codePressureScenario)
            appendLine(card.valueFreeActionSentence)
            appendLine(card.whyItMatters)
            card.doNotBuyShareScanRedeemOrAttachBoundaries.forEach(::appendLine)
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
