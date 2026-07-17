package com.mongmong.familyshield

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkAppInstallQuarantineCoachTest {
    @Test
    fun noAnalysisCoachIsConservativeAndNeverDeclaresLinkAppOrOutcomeStatus() {
        val coach = LinkAppInstallQuarantineCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(LinkAppInstallQuarantineCardId.entries, coach.recommendedCardIds)
        assertEquals(
            LinkAppInstallQuarantineActionId.entries.filterNot { actionId ->
                actionId == LinkAppInstallQuarantineActionId.ANALYSIS_RESULT
            },
            coach.recommendedActionIds
        )
        assertTrue(coach.recommendationTitle.contains("모든 링크·앱 설치 격리 카드"))
        assertTrue(coach.recommendationExplanation.contains("상태를 단정하지 말고"))

        val copy = allCopy(coach)
        listOf(
            "official link",
            "verified URL",
            "safe domain",
            "secure link",
            "valid URL",
            "trusted app",
            "malware-free",
            "app installed",
            "app uninstalled",
            "app disabled",
            "device cleaned",
            "link blocked",
            "permission granted",
            "permission revoked",
            "QR scanned",
            "APK inspected",
            "request checked",
            "case resolved",
            "verification complete",
            "device protected",
            "report submitted",
            "report accepted",
            "report received",
            "legal outcome guaranteed",
            "problem-free",
            "공식 링크입니다",
            "검증된 URL입니다",
            "안전한 도메인입니다",
            "신뢰할 수 있는 앱입니다",
            "QR이 확인됐습니다",
            "바코드가 확인됐습니다",
            "APK가 검사됐습니다",
            "앱이 설치됐습니다",
            "앱이 제거됐습니다",
            "앱이 비활성화됐습니다",
            "기기가 정리됐습니다",
            "링크가 차단됐습니다",
            "권한이 철회됐습니다",
            "권한이 허용됐습니다",
            "악성 프로그램이 없습니다",
            "보안이 확인됐습니다",
            "유효합니다",
            "신고가 제출됐습니다",
            "신고가 수신됐습니다",
            "사건이 접수됐습니다",
            "검사가 완료됐습니다",
            "적법합니다",
            "보장됩니다",
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
    fun linkAndSmishingSignalsRecommendQuarantinePrivacyVerificationEvidenceAndHelpRoutes() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val coach = LinkAppInstallQuarantineCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, signal),
                readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        LinkAppInstallQuarantineCardId
                            .DO_NOT_OPEN_PREVIEW_COPY_SCAN_OR_SHARE_LINK,
                        LinkAppInstallQuarantineCardId
                            .LEAVE_CALL_CHAT_OR_SCREEN_SHARE_SESSION,
                        LinkAppInstallQuarantineCardId
                            .SEPARATE_DELIVERY_INVITATION_FINE_AND_REFUND,
                        LinkAppInstallQuarantineCardId
                            .VALUE_FREE_EXPOSURE_EVIDENCE_HELP_AND_FOLLOW_UP
                    )
                )
            )
            assertRoutes(
                coach,
                LinkAppInstallQuarantineActionId.SAFE_PASTE_PRIVACY_COACH,
                LinkAppInstallQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                LinkAppInstallQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                LinkAppInstallQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH,
                LinkAppInstallQuarantineActionId.EXPOSURE_IMPACT_TRIAGE,
                LinkAppInstallQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                LinkAppInstallQuarantineActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
                LinkAppInstallQuarantineActionId.OFFICIAL_HELP_DIRECTORY,
                LinkAppInstallQuarantineActionId.REPORT_PREP
            )
            val explanation = coach.recommendationExplanation
            assertTrue(explanation.contains("URL·도메인 검증"))
            assertTrue(explanation.contains("브라우저·네트워크 동작"))
            assertTrue(explanation.contains("QR·바코드 스캔"))
            assertTrue(explanation.contains("공식 번호 조회나 신고 제출을 하지 않습니다"))
            listOf(
                "URL 검증 완료",
                "도메인을 확인했습니다",
                "링크를 열었습니다",
                "QR을 스캔했습니다",
                "공식 번호를 찾았습니다",
                "신고를 제출했습니다"
            ).forEach { claim -> assertFalse(allCopy(coach).contains(claim)) }
        }
    }

    @Test
    fun appInstallAndLiveCallSignalsRecommendExitRemoteAccountExposureAndHelperWithoutDeviceActions() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val coach = LinkAppInstallQuarantineCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input = "APK_VALUE PACKAGE_VALUE APP_VALUE PERMISSION_VALUE DEVICE_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY, "PLAN_PRIVATE_VALUE")
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        LinkAppInstallQuarantineCardId.STOP_APP_INSTALL_AND_PERMISSION_PRESSURE,
                        LinkAppInstallQuarantineCardId.LEAVE_CALL_CHAT_OR_SCREEN_SHARE_SESSION,
                        LinkAppInstallQuarantineCardId.KEEP_CREDENTIAL_PAYMENT_AND_CODE_VALUES_OUT,
                        LinkAppInstallQuarantineCardId
                            .VALUE_FREE_EXPOSURE_EVIDENCE_HELP_AND_FOLLOW_UP
                    )
                )
            )
            assertRoutes(
                coach,
                LinkAppInstallQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                LinkAppInstallQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH,
                LinkAppInstallQuarantineActionId.ACCOUNT_LOCKDOWN,
                LinkAppInstallQuarantineActionId.EXPOSURE_IMPACT_TRIAGE,
                LinkAppInstallQuarantineActionId.CREDENTIAL_SECRET_FREEZE_COACH,
                LinkAppInstallQuarantineActionId.SAFE_PASTE_PRIVACY_COACH,
                LinkAppInstallQuarantineActionId.TRUSTED_HELPER_REVIEW,
                LinkAppInstallQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH
            )
            assertTrue(coach.recommendationExplanation.contains("설치 앱·패키지·권한 목록 조회"))
            assertTrue(coach.recommendationExplanation.contains("기기 제어를 수행하지 않습니다"))
            assertNoPrivateValues(
                coach,
                "APK_VALUE",
                "PACKAGE_VALUE",
                "APP_VALUE",
                "PERMISSION_VALUE",
                "DEVICE_VALUE",
                "PLAN_PRIVATE_VALUE"
            )
            listOf(
                "앱 목록을 확인했습니다",
                "앱을 설치했습니다",
                "앱을 제거했습니다",
                "권한을 철회했습니다",
                "설정을 변경했습니다",
                "악성 앱을 제거했습니다",
                "원격 연결을 종료했습니다",
                "기기를 정리했습니다"
            ).forEach { claim -> assertFalse(allCopy(coach).contains(claim)) }
        }
    }

    @Test
    fun credentialOtpAndGiftCardSignalsRecommendSecretGiftAccountRemoteAndExposureWithoutValues() {
        listOf(
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = LinkAppInstallQuarantineCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "LINK_VALUE APP_VALUE OTP_VALUE PIN_VALUE PASSWORD_VALUE " +
                            "SECURITY_ANSWER ACCOUNT_VALUE CARD_VALUE BANK_VALUE " +
                            "WALLET_VALUE GIFT_CODE_VALUE"
                ),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "SAFE_WORD_VALUE CALLBACK_VALUE CONTACT_VALUE"
                )
            )

            assertTrue(
                LinkAppInstallQuarantineCardId.KEEP_CREDENTIAL_PAYMENT_AND_CODE_VALUES_OUT in
                    coach.recommendedCardIds
            )
            assertRoutes(
                coach,
                LinkAppInstallQuarantineActionId.CREDENTIAL_SECRET_FREEZE_COACH,
                LinkAppInstallQuarantineActionId.GIFT_CARD_CODE_PAUSE_COACH,
                LinkAppInstallQuarantineActionId.SAFE_PASTE_PRIVACY_COACH,
                LinkAppInstallQuarantineActionId.ACCOUNT_LOCKDOWN,
                LinkAppInstallQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH,
                LinkAppInstallQuarantineActionId.EXPOSURE_IMPACT_TRIAGE
            )
            assertNoPrivateValues(
                coach,
                "LINK_VALUE",
                "APP_VALUE",
                "OTP_VALUE",
                "PIN_VALUE",
                "PASSWORD_VALUE",
                "SECURITY_ANSWER",
                "ACCOUNT_VALUE",
                "CARD_VALUE",
                "BANK_VALUE",
                "WALLET_VALUE",
                "GIFT_CODE_VALUE",
                "SAFE_WORD_VALUE",
                "CALLBACK_VALUE",
                "CONTACT_VALUE"
            )
        }
    }

    @Test
    fun moneyRecoveryAndPaymentSignalsRecommendPauseRealityChecksEvidenceAndHelpWithoutAutomation() {
        listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.BAIT,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = LinkAppInstallQuarantineCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, signal),
                readiness(ReadinessLevel.READY)
            )

            assertRoutes(
                coach,
                LinkAppInstallQuarantineActionId.MONEY_TRANSFER_PAUSE_GATE,
                LinkAppInstallQuarantineActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH,
                LinkAppInstallQuarantineActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
                LinkAppInstallQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                LinkAppInstallQuarantineActionId.TRUSTED_HELPER_REVIEW,
                LinkAppInstallQuarantineActionId.OFFICIAL_HELP_DIRECTORY,
                LinkAppInstallQuarantineActionId.REPORT_PREP
            )
            val copy = allCopy(coach)
            listOf(
                "결제를 취소했습니다",
                "환급됐습니다",
                "차지백이 접수됐습니다",
                "지갑을 추적했습니다",
                "피해금을 회수했습니다",
                "회복됐습니다",
                "은행 거래를 차단했습니다"
            ).forEach { claim -> assertFalse(copy.contains(claim)) }
        }
    }

    @Test
    fun impersonationAndSecrecyRecommendPressureVerificationExitFamilyEvidenceAndHelp() {
        listOf(ScamSignal.IMPERSONATION, ScamSignal.SECRECY).forEach { signal ->
            val coach = LinkAppInstallQuarantineCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "AGENCY_VALUE DIRECTORY_VALUE OFFICIAL_NUMBER CASE_VALUE " +
                            "HELPER_VALUE LAWYER_VALUE INVESTIGATOR_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY, "PRIVATE_PLAN_VALUE")
            )

            assertRoutes(
                coach,
                LinkAppInstallQuarantineActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH,
                LinkAppInstallQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                LinkAppInstallQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS,
                LinkAppInstallQuarantineActionId.FAMILY_ALERT_MESSAGES,
                LinkAppInstallQuarantineActionId.FAMILY_CALM_DEBRIEF_AGENDA,
                LinkAppInstallQuarantineActionId.TRUSTED_HELPER_REVIEW,
                LinkAppInstallQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
                LinkAppInstallQuarantineActionId.OFFICIAL_HELP_DIRECTORY,
                LinkAppInstallQuarantineActionId.REPORT_PREP
            )
            assertTrue(coach.recommendationExplanation.contains("공식 번호·디렉터리 값"))
            assertNoPrivateValues(
                coach,
                "AGENCY_VALUE",
                "DIRECTORY_VALUE",
                "OFFICIAL_NUMBER",
                "CASE_VALUE",
                "HELPER_VALUE",
                "LAWYER_VALUE",
                "INVESTIGATOR_VALUE",
                "PRIVATE_PLAN_VALUE"
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
            val coach = LinkAppInstallQuarantineCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal, input = "RAW_CONTACT_VALUE"),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "CONTACT_LABEL PHONE_VALUE SAFE_WORD_VALUE CALLBACK_VALUE"
                )
            )

            assertRoutes(
                coach,
                LinkAppInstallQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH,
                LinkAppInstallQuarantineActionId.FAMILY_CALLBACK_CARD,
                LinkAppInstallQuarantineActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH,
                LinkAppInstallQuarantineActionId.NEW_NUMBER_QUARANTINE_COACH,
                LinkAppInstallQuarantineActionId.FAMILY_SAFE_WORD_RESET_COACH,
                LinkAppInstallQuarantineActionId.SAFE_CALL_SCRIPTS,
                LinkAppInstallQuarantineActionId.FAMILY_ALERT_MESSAGES
            )
            assertTrue(coach.recommendationExplanation.contains("연락처·안전단어·콜백 값"))
            assertTrue(coach.recommendationExplanation.contains("통화 기록·문자를 읽지"))
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
        val noIncident = LinkAppInstallQuarantineCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 0
        )
        val negativeIncident = LinkAppInstallQuarantineCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = -91
        )
        val oneIncident = LinkAppInstallQuarantineCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 1
        )
        val manyIncidents = LinkAppInstallQuarantineCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = Int.MAX_VALUE
        )

        assertEquals(noIncident, negativeIncident)
        assertEquals(oneIncident, manyIncidents)
        assertNotEquals(noIncident.recommendedActionIds, oneIncident.recommendedActionIds)
        assertTrue(
            LinkAppInstallQuarantineCardId.VALUE_FREE_EXPOSURE_EVIDENCE_HELP_AND_FOLLOW_UP in
                oneIncident.recommendedCardIds
        )
        assertRoutes(
            oneIncident,
            LinkAppInstallQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH,
            LinkAppInstallQuarantineActionId.EVIDENCE_KIT,
            LinkAppInstallQuarantineActionId.INCIDENT_TIMELINE_BRIEFING,
            LinkAppInstallQuarantineActionId.FOLLOW_UP_CHECK_IN_PLANNER
        )
        assertTrue(oneIncident.recommendationExplanation.contains("개수 범주만"))
        assertTrue(oneIncident.recommendationExplanation.contains("사건 내용이나 미리보기는 읽지"))
    }

    @Test
    fun readinessUsesOnlyLevelAndReadyNeverMakesLinkAppOrRequestSafeOrTrusted() {
        listOf(ReadinessLevel.LOW, ReadinessLevel.IN_PROGRESS).forEach { level ->
            val coach = LinkAppInstallQuarantineCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
                readiness(level, "READINESS_PRIVATE_LITERAL")
            )

            assertRoutes(
                coach,
                LinkAppInstallQuarantineActionId.SAFETY_PLAN,
                LinkAppInstallQuarantineActionId.FAMILY_SAFE_WORD_RESET_COACH,
                LinkAppInstallQuarantineActionId.FAMILY_READINESS_ROADMAP,
                LinkAppInstallQuarantineActionId.FAMILY_SAFETY_DRILL_COACH,
                LinkAppInstallQuarantineActionId.SAFE_CALL_SCRIPTS
            )
            assertFalse(allCopy(coach).contains("READINESS_PRIVATE_LITERAL"))
        }

        val readyCoach = LinkAppInstallQuarantineCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
            readiness(ReadinessLevel.READY, "READY_PRIVATE_LITERAL")
        )
        assertTrue(
            readyCoach.recommendationExplanation.contains(
                "링크·URL·도메인·QR·바코드·APK·앱·패키지·권한·기기·로그인·" +
                    "계정·결제·상품권 요청이 실제이거나 안전하거나 신뢰할 수 있다는 " +
                    "뜻이 아닙니다"
            )
        )
        assertFalse(allCopy(readyCoach).contains("READY_PRIVATE_LITERAL"))
    }

    @Test
    fun cardsAreCompleteStableAndUseUniqueDeclaredRouteActions() {
        val coach = LinkAppInstallQuarantineCoachBuilder.build(
            null,
            readiness(ReadinessLevel.READY)
        )

        assertEquals(LinkAppInstallQuarantineCardId.entries.size, coach.cards.size)
        assertEquals(LinkAppInstallQuarantineCardId.entries, coach.cards.map { card -> card.id })
        assertEquals(
            (1..LinkAppInstallQuarantineCardId.entries.size).toList(),
            coach.cards.map { card -> card.displayOrder }
        )
        coach.cards.forEach { card ->
            assertEquals(card.id.displayOrder, card.displayOrder)
            assertTrue(card.title.isNotBlank())
            assertTrue(card.linkAppPressureScenario.isNotBlank())
            assertTrue(card.valueFreeActionSentence.isNotBlank())
            assertTrue(card.whyItMatters.isNotBlank())
            assertTrue(card.doNotOpenInstallEnterScanGrantOrShareBoundaries.size >= 2)
            assertTrue(
                card.doNotOpenInstallEnterScanGrantOrShareBoundaries.all { boundary ->
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
                    action.id in LinkAppInstallQuarantineActionId.entries
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
            "app/src/main/java/com/mongmong/familyshield/LinkAppInstallQuarantineCoach.kt"
        )
        LinkAppInstallQuarantineActionId.entries.forEach { actionId ->
            assertTrue(
                "action lacks a stable label: $actionId",
                source.contains("LinkAppInstallQuarantineActionId.$actionId to")
            )
        }
    }

    @Test
    fun everyActionIdMapsToAnExistingMainActivityFlow() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val routerSource = activitySource
            .substringAfter("private fun openLinkAppInstallQuarantineAction")
            .substringBefore("private fun shareLinkAppInstallQuarantineCoach")

        assertFalse(
            "link/app action router is missing",
            routerSource == activitySource || routerSource.isBlank()
        )
        LinkAppInstallQuarantineActionId.entries.forEach { actionId ->
            assertTrue(
                "action did not map to an existing flow: $actionId",
                routerSource.contains("LinkAppInstallQuarantineActionId.$actionId")
            )
        }
        assertTrue(
            routerSource.contains(
                "openHelpDirectory(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)"
            )
        )
    }

    @Test
    fun familyShareSummaryIsRebuiltOnlyFromGenericCardsRoutesAndActions() {
        val coach = LinkAppInstallQuarantineCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.LINK,
                ScamSignal.APP_INSTALL,
                ScamSignal.CREDENTIALS,
                ScamSignal.IMPERSONATION,
                input =
                    "RAW_INPUT REDACTED_CLUE HTTPS_VALUE DOMAIN_VALUE SHORT_LINK QR_VALUE " +
                        "BARCODE_VALUE APK_VALUE PACKAGE_VALUE APP_VALUE PERMISSION_VALUE " +
                        "DEVICE_VALUE FILE_VALUE MEDIA_VALUE URI_VALUE PATH_VALUE OTP_VALUE " +
                        "PIN_VALUE PASSWORD_VALUE SECURITY_ANSWER RESIDENT_VALUE ACCOUNT_VALUE " +
                        "CARD_VALUE BANK_VALUE WALLET_VALUE TRANSACTION_VALUE PAYMENT_VALUE " +
                        "GIFT_CODE SERIAL_VALUE REDEEM_VALUE CONTACT_VALUE CALLER_VALUE " +
                        "CHANNEL_VALUE PROFILE_VALUE AGENCY_NAME STORE_NAME BRAND_NAME " +
                        "HELPER_NAME LAWYER_NAME INVESTIGATOR_NAME CASE_ID REPORT_ID " +
                        "SUBMISSION_STATUS RECEIPT_ID SCREENSHOT_VALUE OCR_VALUE AUDIO_VALUE " +
                        "TRANSCRIPT_VALUE CLIPBOARD_VALUE AUTOFILL_VALUE LOGIN_STATE " +
                        "CALL_STATE SMS_STATE NOTIFICATION_STATE RECIPIENT_VALUE " +
                        "VERIFICATION_RESULT INSTALL_STATUS REMOVE_STATUS PERMISSION_STATUS " +
                        "MALWARE_STATUS DEVICE_STATUS REFUND_STATUS CHARGEBACK_STATUS " +
                        "RECOVERY_STATUS FREEZE_STATUS CANCEL_STATUS BLOCK_STATUS TRACE_STATUS " +
                        "LEGAL_RESULT FINANCIAL_RESULT"
            ),
            readiness = readiness(
                ReadinessLevel.READY,
                "SAFE_WORD_VALUE CALLBACK_VALUE CONTACT_PLAN_VALUE PLAN_VALUE"
            ),
            incidentCount = 7
        )
        val summary = coach.familyShareSummary

        assertTrue(summary.contains("링크·앱 설치 격리 코치"))
        assertTrue(summary.contains("추천 링크·앱 격리 카드"))
        coach.recommendedCards.forEach { card -> assertTrue(summary.contains(card.title)) }
        coach.recommendedActions.forEach { action -> assertTrue(summary.contains(action.label)) }
        listOf(
            "RAW_INPUT",
            "REDACTED_CLUE",
            "HTTPS_VALUE",
            "DOMAIN_VALUE",
            "SHORT_LINK",
            "QR_VALUE",
            "BARCODE_VALUE",
            "APK_VALUE",
            "PACKAGE_VALUE",
            "APP_VALUE",
            "PERMISSION_VALUE",
            "DEVICE_VALUE",
            "FILE_VALUE",
            "MEDIA_VALUE",
            "URI_VALUE",
            "PATH_VALUE",
            "OTP_VALUE",
            "PIN_VALUE",
            "PASSWORD_VALUE",
            "SECURITY_ANSWER",
            "RESIDENT_VALUE",
            "ACCOUNT_VALUE",
            "CARD_VALUE",
            "BANK_VALUE",
            "WALLET_VALUE",
            "TRANSACTION_VALUE",
            "PAYMENT_VALUE",
            "GIFT_CODE",
            "SERIAL_VALUE",
            "REDEEM_VALUE",
            "CONTACT_VALUE",
            "CALLER_VALUE",
            "CHANNEL_VALUE",
            "PROFILE_VALUE",
            "AGENCY_NAME",
            "STORE_NAME",
            "BRAND_NAME",
            "HELPER_NAME",
            "LAWYER_NAME",
            "INVESTIGATOR_NAME",
            "CASE_ID",
            "REPORT_ID",
            "SUBMISSION_STATUS",
            "RECEIPT_ID",
            "SCREENSHOT_VALUE",
            "OCR_VALUE",
            "AUDIO_VALUE",
            "TRANSCRIPT_VALUE",
            "CLIPBOARD_VALUE",
            "AUTOFILL_VALUE",
            "LOGIN_STATE",
            "CALL_STATE",
            "SMS_STATE",
            "NOTIFICATION_STATE",
            "RECIPIENT_VALUE",
            "VERIFICATION_RESULT",
            "INSTALL_STATUS",
            "REMOVE_STATUS",
            "PERMISSION_STATUS",
            "MALWARE_STATUS",
            "DEVICE_STATUS",
            "REFUND_STATUS",
            "CHARGEBACK_STATUS",
            "RECOVERY_STATUS",
            "FREEZE_STATUS",
            "CANCEL_STATUS",
            "BLOCK_STATUS",
            "TRACE_STATUS",
            "LEGAL_RESULT",
            "FINANCIAL_RESULT",
            "SAFE_WORD_VALUE",
            "CALLBACK_VALUE",
            "CONTACT_PLAN_VALUE",
            "PLAN_VALUE",
            "경찰 긴급 신고",
            "보이스피싱 통합신고·상담",
            "은행·카드사 공식 대표번호"
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
        val first = LinkAppInstallQuarantineCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.LINK,
                ScamSignal.APP_INSTALL,
                input = "FIRST_RAW_VALUE",
                score = 1
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "FIRST_PLAN_VALUE"),
            incidentCount = 2
        )
        val second = LinkAppInstallQuarantineCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.LINK,
                ScamSignal.APP_INSTALL,
                input = "SECOND_RAW_VALUE",
                score = 999
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, "SECOND_PLAN_VALUE"),
            incidentCount = 93
        )

        assertEquals(first, second)
        assertEquals(
            first,
            LinkAppInstallQuarantineCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.HIGH,
                    ScamSignal.LINK,
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
            LinkAppInstallQuarantineCoachBuilder.build(
                null,
                readiness(ReadinessLevel.READY)
            )
        )

        listOf(
            "로컬 전용·무상태 수동 안내",
            "입력하라고 요구하지 않습니다",
            "링크·URL·도메인·단축 주소",
            "열기·미리 보기·가져오기·따라가기·해결·정규화·단축·확장",
            "평판 조회·검증·분류·스캔·샌드박스 실행하지 않습니다",
            "네트워크·웹 탐색·브라우저·웹뷰·지도·위치·지오코딩",
            "앱스토어·기관 정보·공식 번호·디렉터리",
            "발신자 표시·전화번호·문서·사건·법률 상태",
            "QR·바코드·OCR·카메라·이미지 분석·화면 캡처",
            "파일·문서·사진·스크린샷·오디오·미디어·저장소",
            "첨부·스트림·URI 공유·업로드·동기화",
            "APK·앱·패키지·설치 앱 목록·앱 이름·권한·기기 상태",
            "설치·제거·비활성화·강제 종료",
            "권한 허용·철회·설정 변경·앱 작업",
            "악성 프로그램 검사·제거·기기 정리·원격 연결 종료",
            "접근성 서비스·기기 관리자·VPN·사용 통계·알림 수신 서비스",
            "권한 관리자·패키지 설치 도구",
            "전화·다이얼·발신·통화·종료·선별·차단",
            "문자·SMS·받는 사람 선택·자동 전송",
            "연락처·주소록·통화 기록·문자·알림",
            "클립보드·텍스트 감시기·자동완성",
            "자격 증명·계정·인증·생체 인증·키가드·키 저장소·암호화 API",
            "로그인·계정·인증 상태",
            "결제·은행·카드·NFC·지갑·블록체인·가상자산·상품권 기능 또는 API",
            "환급·차지백·추적·회복",
            "신고 제출·영수증 확인·법률 자문",
            "자동 발송하지 않습니다",
            "새 Android 권한",
            "백그라운드 워커·스케줄러·브로드캐스트 수신기",
            "새 저장소를 만들지 않고",
            "가족 계획·사건·디렉터리·연락처·알림·통화·문자·계정·결제·기기 상태를 수정하지 않습니다"
        ).forEach { boundary ->
            assertTrue("privacy copy omitted $boundary", copy.contains(boundary))
        }
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndAutomaticProtectionClaims() {
        val copy = allCopy(
            LinkAppInstallQuarantineCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.READY),
                incidentCount = 1
            )
        )

        listOf(
            "safe link",
            "secure link",
            "valid URL",
            "trusted app",
            "URL verified",
            "domain checked",
            "malware-free",
            "app installed",
            "app uninstalled",
            "app removed",
            "app disabled",
            "device cleaned",
            "link blocked",
            "permission revoked",
            "permission granted",
            "QR scanned",
            "APK inspected",
            "request checked",
            "case resolved",
            "verification complete",
            "device protected",
            "report submitted",
            "report accepted",
            "report received",
            "legal outcome guaranteed",
            "guaranteed result",
            "problem-free",
            "안전한 링크입니다",
            "신뢰할 수 있는 앱입니다",
            "URL 검증 완료",
            "도메인 확인 완료",
            "악성 프로그램이 없습니다",
            "앱이 설치됐습니다",
            "앱이 제거됐습니다",
            "기기가 정리됐습니다",
            "링크가 차단됐습니다",
            "권한이 철회됐습니다",
            "권한이 허용됐습니다",
            "기기가 보호됐습니다",
            "앱이 비활성화됐습니다",
            "신고가 제출됐습니다",
            "신고가 접수됐습니다",
            "환급이 보장됩니다",
            "금융 결과를 보장합니다",
            "법률 결과를 보장합니다",
            "자동 링크 감시",
            "자동 앱 검사",
            "자동 앱 삭제",
            "자동 정보 가림",
            "자동 통화 차단",
            "자동 문자 발송",
            "자동 공식 조회",
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
            "app/src/main/java/com/mongmong/familyshield/LinkAppInstallQuarantineCoach.kt"
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
            "PackageInstaller",
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
            "AccessibilityManager",
            "AppOpsManager",
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
            "Malware",
            "Antivirus",
            "VirusTotal",
            "prefs.edit(",
            "putString(",
            "putBoolean(",
            "putInt(",
            ".save("
        ).forEach { forbiddenSource ->
            assertFalse(
                "link/app quarantine model used forbidden source: $forbiddenSource",
                source.contains(forbiddenSource)
            )
        }
    }

    @Test
    fun highRiskShowsAllCardsAndRoutesWhileSignalCategoriesRemainDistinct() {
        val highRisk = LinkAppInstallQuarantineCoachBuilder.build(
            analysis(RiskLevel.HIGH, ScamSignal.SECRECY),
            readiness(ReadinessLevel.READY)
        )
        assertEquals(LinkAppInstallQuarantineCardId.entries, highRisk.recommendedCardIds)
        assertEquals(LinkAppInstallQuarantineActionId.entries, highRisk.recommendedActionIds)

        val link = LinkAppInstallQuarantineCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.LINK),
            readiness(ReadinessLevel.READY)
        )
        val appInstall = LinkAppInstallQuarantineCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.APP_INSTALL),
            readiness(ReadinessLevel.READY)
        )
        val financial = LinkAppInstallQuarantineCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.URGENT_TRANSFER),
            readiness(ReadinessLevel.READY)
        )
        val family = LinkAppInstallQuarantineCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.FAMILY_EMERGENCY),
            readiness(ReadinessLevel.READY)
        )
        assertNotEquals(link.recommendedActionIds, appInstall.recommendedActionIds)
        assertNotEquals(appInstall.recommendedActionIds, financial.recommendedActionIds)
        assertNotEquals(financial.recommendedActionIds, family.recommendedActionIds)
        assertNotEquals(link.recommendationTitle, family.recommendationTitle)
    }

    private fun assertRoutes(
        coach: LinkAppInstallQuarantineCoach,
        vararg expected: LinkAppInstallQuarantineActionId
    ) {
        assertTrue(coach.recommendedActionIds.containsAll(expected.toList()))
    }

    private fun assertNoPrivateValues(
        coach: LinkAppInstallQuarantineCoach,
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

    private fun allCopy(coach: LinkAppInstallQuarantineCoach): String = buildString {
        appendLine(coach.title)
        appendLine(coach.intro)
        appendLine(coach.modeLabel)
        appendLine(coach.recommendationTitle)
        appendLine(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> appendLine(action.label) }
        coach.cards.forEach { card ->
            appendLine(card.title)
            appendLine(card.linkAppPressureScenario)
            appendLine(card.valueFreeActionSentence)
            appendLine(card.whyItMatters)
            card.doNotOpenInstallEnterScanGrantOrShareBoundaries.forEach(::appendLine)
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
