package com.mongmong.familyshield

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenshotPrivacyGuardCoachTest {
    @Test
    fun noAnalysisCoachIsConservativeAndNeverDeclaresCaptureOrDetectionStatus() {
        val coach = ScreenshotPrivacyGuardCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(ScreenshotPrivacyGuardCardId.entries, coach.recommendedCardIds)
        assertEquals(
            ScreenshotPrivacyGuardActionId.entries.filterNot { actionId ->
                actionId == ScreenshotPrivacyGuardActionId.ANALYSIS_RESULT
            },
            coach.recommendedActionIds
        )
        assertEquals("스크린샷 개인정보 가드 코치", coach.title)
        assertTrue(coach.recommendationTitle.contains("모든"))
        assertTrue(coach.recommendationExplanation.contains("안전·차단·감지·완료라고 단정하지 않고"))

        listOf(
            "screenshot detected",
            "capture blocked",
            "gallery protected",
            "cloud backup safe",
            "upload completed",
            "share submitted",
            "problem-free",
            "화면캡처가 감지됐습니다",
            "캡처가 차단됐습니다",
            "갤러리가 보호됐습니다",
            "클라우드 백업이 안전합니다",
            "정리가 완료됐습니다",
            "공유가 접수됐습니다",
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
    fun credentialOtpAndPaymentSignalsRecommendRedactAndAppShareRoutes() {
        listOf(
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val coach = ScreenshotPrivacyGuardCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input =
                        "OTP_VALUE PIN_VALUE PASSWORD_VALUE ACCOUNT_VALUE CARD_VALUE " +
                            "BANK_VALUE WALLET_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY, "PRIVATE_PLAN_VALUE")
            )

            assertRoutes(
                coach,
                ScreenshotPrivacyGuardActionId.CREDENTIAL_SECRET_FREEZE_COACH,
                ScreenshotPrivacyGuardActionId.ACCOUNT_LOCKDOWN,
                ScreenshotPrivacyGuardActionId.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH
            )
            assertTrue(coach.recommendationExplanation.contains("OTP·PIN·비밀번호·계좌·카드·상품권 값"))
            assertNoPrivateValues(
                coach,
                "OTP_VALUE",
                "PIN_VALUE",
                "PASSWORD_VALUE",
                "ACCOUNT_VALUE",
                "CARD_VALUE",
                "BANK_VALUE",
                "WALLET_VALUE",
                "PRIVATE_PLAN_VALUE"
            )
        }
    }

    @Test
    fun numberChangeFamilyEmergencyAndDeepfakeUseOnlyValueFreeFamilyChannelRoutes() {
        listOf(
            ScamSignal.PHONE_NUMBER_CHANGE,
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE
        ).forEach { signal ->
            val coach = ScreenshotPrivacyGuardCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input = "PHONE_VALUE NEW_NUMBER_VALUE CONTACT_VALUE CHANNEL_VALUE"
                ),
                readiness = readiness(
                    ReadinessLevel.READY,
                    "SAFE_WORD_VALUE CALLBACK_VALUE CONTACT_LABEL_VALUE"
                )
            )

            assertRoutes(
                coach,
                ScreenshotPrivacyGuardActionId.FAMILY_ALERT_MESSAGES,
                ScreenshotPrivacyGuardActionId.FAMILY_CALLBACK_CARD,
                ScreenshotPrivacyGuardActionId.TRUSTED_HELPER_REVIEW,
                ScreenshotPrivacyGuardActionId.SAFE_CALL_SCRIPTS
            )
            assertTrue(coach.recommendationExplanation.contains("전화번호·연락처·안전단어·콜백 값"))
            assertNoPrivateValues(
                coach,
                "PHONE_VALUE",
                "NEW_NUMBER_VALUE",
                "CONTACT_VALUE",
                "CHANNEL_VALUE",
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
            val coach = ScreenshotPrivacyGuardCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input = "STAFF_VALUE AGENCY_VALUE OFFICIAL_NUMBER_VALUE CASE_VALUE REPORT_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertRoutes(
                coach,
                ScreenshotPrivacyGuardActionId.OFFICIAL_HELP_DIRECTORY,
                ScreenshotPrivacyGuardActionId.REPORT_PREP,
                ScreenshotPrivacyGuardActionId.TRUSTED_HELPER_REVIEW,
                ScreenshotPrivacyGuardActionId.FAMILY_ALERT_MESSAGES,
                ScreenshotPrivacyGuardActionId.ACCOUNT_LOCKDOWN
            )
            assertTrue(coach.recommendationExplanation.contains("기관·직원 신원·공식 번호나 사건 접수 상태를 조회·검증하지 않습니다"))
            assertNoPrivateValues(
                coach,
                "STAFF_VALUE",
                "AGENCY_VALUE",
                "OFFICIAL_NUMBER_VALUE",
                "CASE_VALUE",
                "REPORT_VALUE"
            )
        }
    }

    @Test
    fun liveCallAppLinkAndSmishingRouteToCloudBackupAwarenessWithoutDeviceClaims() {
        listOf(
            ScamSignal.LIVE_CALL_CONTROL,
            ScamSignal.APP_INSTALL,
            ScamSignal.LINK,
            ScamSignal.PARCEL_INVITATION_SMISHING
        ).forEach { signal ->
            val coach = ScreenshotPrivacyGuardCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input = "CALL_STATE APP_VALUE PACKAGE_VALUE DEVICE_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertRoutes(
                coach,
                ScreenshotPrivacyGuardActionId.ACCOUNT_LOCKDOWN,
                ScreenshotPrivacyGuardActionId.EXPOSURE_IMPACT_TRIAGE,
                ScreenshotPrivacyGuardActionId.PRIVACY_CENTER
            )
            assertTrue(coach.recommendationExplanation.contains("앱·패키지·기기·설정·클라우드 백업 상태를 읽거나 바꾸지 않습니다"))
            assertNoPrivateValues(
                coach,
                "CALL_STATE",
                "APP_VALUE",
                "PACKAGE_VALUE",
                "DEVICE_VALUE"
            )
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
            val coach = ScreenshotPrivacyGuardCoachBuilder.build(
                analysis = analysis(
                    RiskLevel.MEDIUM,
                    signal,
                    input = "AMOUNT_VALUE ACCOUNT_VALUE BANK_VALUE REFUND_VALUE"
                ),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertRoutes(
                coach,
                ScreenshotPrivacyGuardActionId.CREDENTIAL_SECRET_FREEZE_COACH,
                ScreenshotPrivacyGuardActionId.ACCOUNT_LOCKDOWN,
                ScreenshotPrivacyGuardActionId.TRUSTED_HELPER_REVIEW,
                ScreenshotPrivacyGuardActionId.OFFICIAL_HELP_DIRECTORY,
                ScreenshotPrivacyGuardActionId.REPORT_PREP
            )
            assertTrue(coach.recommendationExplanation.contains("결제·은행·환급·차지백·회복 행동을 수행하지 않습니다"))
            assertNoPrivateValues(
                coach,
                "AMOUNT_VALUE",
                "ACCOUNT_VALUE",
                "BANK_VALUE",
                "REFUND_VALUE"
            )
        }
    }

    @Test
    fun savedIncidentUsesOnlyClampedPresenceCategory() {
        val baseAnalysis = analysis(RiskLevel.MEDIUM, ScamSignal.LIVE_CALL_CONTROL)
        val noIncident = ScreenshotPrivacyGuardCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 0
        )
        val negativeIncident = ScreenshotPrivacyGuardCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = -41
        )
        val oneIncident = ScreenshotPrivacyGuardCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = 1
        )
        val manyIncidents = ScreenshotPrivacyGuardCoachBuilder.build(
            baseAnalysis,
            readiness(ReadinessLevel.READY),
            incidentCount = Int.MAX_VALUE
        )

        assertEquals(noIncident, negativeIncident)
        assertEquals(oneIncident, manyIncidents)
        assertNotEquals(noIncident.recommendedActionIds, oneIncident.recommendedActionIds)
        assertTrue(
            ScreenshotPrivacyGuardCardId.AFTER_SCREENSHOT_CLEANUP_REMINDER in
                oneIncident.recommendedCardIds
        )
        assertRoutes(
            oneIncident,
            ScreenshotPrivacyGuardActionId.EVIDENCE_KIT,
            ScreenshotPrivacyGuardActionId.INCIDENT_TIMELINE_BRIEFING,
            ScreenshotPrivacyGuardActionId.FOLLOW_UP_CHECK_IN_PLANNER
        )
        assertTrue(oneIncident.recommendationExplanation.contains("개수 범주만"))
        assertTrue(oneIncident.recommendationExplanation.contains("사건 내용이나 미리보기를 읽지"))
    }

    @Test
    fun readinessUsesOnlyLevelAndReadyNeverMakesCaptureOrShareSafe() {
        listOf(ReadinessLevel.LOW, ReadinessLevel.IN_PROGRESS).forEach { level ->
            val coach = ScreenshotPrivacyGuardCoachBuilder.build(
                analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
                readiness(level, "READINESS_PRIVATE_LITERAL")
            )

            assertTrue(
                ScreenshotPrivacyGuardCardId.FAMILY_SHARING_CHANNEL_BOUNDARY_REMINDER in
                    coach.recommendedCardIds
            )
            assertFalse(allCopy(coach).contains("READINESS_PRIVATE_LITERAL"))
        }

        val readyCoach = ScreenshotPrivacyGuardCoachBuilder.build(
            analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
            readiness(ReadinessLevel.READY, "READY_PRIVATE_LITERAL")
        )
        listOf(
            "화면캡처",
            "갤러리",
            "클라우드 백업",
            "공유",
            "안전",
            "완료",
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
        val coach = ScreenshotPrivacyGuardCoachBuilder.build(
            null,
            readiness(ReadinessLevel.READY)
        )

        assertEquals(ScreenshotPrivacyGuardCardId.entries.size, coach.cards.size)
        assertEquals(ScreenshotPrivacyGuardCardId.entries, coach.cards.map { card -> card.id })
        assertEquals(
            (1..ScreenshotPrivacyGuardCardId.entries.size).toList(),
            coach.cards.map { card -> card.displayOrder }
        )
        coach.cards.forEach { card ->
            assertEquals(card.id.displayOrder, card.displayOrder)
            assertTrue(card.title.isNotBlank())
            assertTrue(card.screenshotPressureSituation.isNotBlank())
            assertTrue(card.valueFreePauseScript.isNotBlank())
            assertTrue(card.whyItMatters.isNotBlank())
            assertTrue(
                card.doNotCaptureDetectStoreUploadOrAutoShareBoundaries.size >= 2
            )
            assertTrue(
                card.doNotCaptureDetectStoreUploadOrAutoShareBoundaries
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
                    action.id in ScreenshotPrivacyGuardActionId.entries
                }
            )
        }
        assertEquals(
            coach.recommendedActions.size,
            coach.recommendedActions.map { action -> action.id }.distinct().size
        )
        val allActions = ScreenshotPrivacyGuardCoachBuilder.build(
            analysis(RiskLevel.HIGH, ScamSignal.SECRECY),
            readiness(ReadinessLevel.READY)
        ).recommendedActions
        assertEquals(ScreenshotPrivacyGuardActionId.entries, allActions.map { action -> action.id })
        assertEquals(allActions.size, allActions.map { action -> action.label }.distinct().size)
    }

    @Test
    fun everyActionIdMapsToAnExistingMainActivityFlow() {
        val activitySource = readProjectFile(
            "app/src/main/java/com/mongmong/familyshield/MainActivity.kt"
        )
        val routerSource = functionBlock(
            activitySource,
            "private fun openScreenshotPrivacyGuardAction"
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
            "CREDENTIAL_SECRET_FREEZE_COACH" to "show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)",
            "ACCOUNT_LOCKDOWN" to "show(Screen.ACCOUNT_LOCKDOWN)",
            "EXPOSURE_IMPACT_TRIAGE" to "show(Screen.EXPOSURE_IMPACT_TRIAGE)",
            "EVIDENCE_HANDOFF_PRIVACY_COACH" to "show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)",
            "EVIDENCE_KIT" to "openEvidenceKit(null)",
            "INCIDENT_TIMELINE_BRIEFING" to "show(Screen.TIMELINE_BRIEFING)",
            "ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH" to
                "show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)",
            "FAMILY_ALERT_MESSAGES" to "show(Screen.FAMILY_ALERT_MESSAGES)",
            "FAMILY_CALLBACK_CARD" to "openFamilyCallbackCard(currentAnalysis)",
            "TRUSTED_HELPER_REVIEW" to "show(Screen.TRUSTED_HELPER_REVIEW)",
            "SAFE_CALL_SCRIPTS" to "show(Screen.SAFE_CALL_SCRIPTS)",
            "OFFICIAL_HELP_DIRECTORY" to
                "openHelpDirectory(Screen.SCREENSHOT_PRIVACY_GUARD_COACH)",
            "REPORT_PREP" to
                "show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)",
            "FOLLOW_UP_CHECK_IN_PLANNER" to "show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)",
            "PRIVACY_CENTER" to "show(Screen.PRIVACY_CENTER)",
            "SAFETY_FLOW_ROUTER" to "show(Screen.FLOW_ROUTER)"
        )
        assertEquals(ScreenshotPrivacyGuardActionId.entries.size, expectedActionRoutes.size)
        expectedActionRoutes.forEach { (actionId, destination) ->
            assertTrue(
                "action did not map to existing flow: $actionId",
                normalizedRouter.contains(
                    "ScreenshotPrivacyGuardActionId.$actionId -> $destination"
                )
            )
        }
        assertEquals(
            ScreenshotPrivacyGuardActionId.entries.size,
            Regex("ScreenshotPrivacyGuardActionId\\.[A-Z_]+\\s*->")
                .findAll(routerSource)
                .count()
        )
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(routerSource).count())
    }

    @Test
    fun familyShareSummaryIsRebuiltOnlyFromGenericFixedCardAndRouteLabels() {
        val coach = ScreenshotPrivacyGuardCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                ScamSignal.CREDENTIALS,
                ScamSignal.PHONE_NUMBER_CHANGE,
                ScamSignal.IMPERSONATION,
                ScamSignal.APP_INSTALL,
                ScamSignal.URGENT_TRANSFER,
                input =
                    "RAW_INPUT REDACTED_CLUE PHONE_VALUE NEW_NUMBER_VALUE OTP_VALUE PIN_VALUE " +
                        "PASSWORD_VALUE ACCOUNT_NUMBER CARD_NUMBER BANK_VALUE WALLET_VALUE " +
                        "SCREENSHOT_VALUE IMAGE_VALUE GALLERY_VALUE CLOUD_BACKUP_VALUE " +
                        "OFFICIAL_NUMBER DIRECTORY_VALUE AGENCY_NAME CASE_ID REPORT_ID " +
                        "010-9876-5432 1234567890123 secret.example"
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
            appendLine("[스크린샷 개인정보 가드 코치]")
            appendLine("실제 화면캡처를 감지·차단하지 않고 캡처 전후에 직접 읽을 값 없는 문장을 고르는 로컬 수동 안내입니다.")
            appendLine()
            appendLine("추천 값 없는 화면캡처 개인정보 카드")
            coach.recommendedCardIds.forEach { cardId ->
                appendLine("- ${cardsById.getValue(cardId).title}")
            }
            appendLine()
            appendLine("직접 고를 기존 앱 경로")
            coach.recommendedActions.forEach { action -> appendLine("- ${action.label}") }
            appendLine()
            appendLine("공유 경계")
            appendLine("- 실제 캡처본·이미지·원문·가린 단서·계좌·전화번호·신분 값은 보내지 않습니다.")
            append("- 이 안내는 화면캡처를 감지·차단·삭제하거나 계정·결제·신고 행동을 수행하거나 결과를 보장하지 않습니다.")
        }
        assertEquals(expected, summary)

        listOf(
            "RAW_INPUT",
            "REDACTED_CLUE",
            "PHONE_VALUE",
            "NEW_NUMBER_VALUE",
            "OTP_VALUE",
            "PIN_VALUE",
            "PASSWORD_VALUE",
            "ACCOUNT_NUMBER",
            "CARD_NUMBER",
            "BANK_VALUE",
            "WALLET_VALUE",
            "SCREENSHOT_VALUE",
            "IMAGE_VALUE",
            "GALLERY_VALUE",
            "CLOUD_BACKUP_VALUE",
            "OFFICIAL_NUMBER",
            "DIRECTORY_VALUE",
            "AGENCY_NAME",
            "CASE_ID",
            "REPORT_ID",
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
        val first = ScreenshotPrivacyGuardCoachBuilder.build(
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
        val second = ScreenshotPrivacyGuardCoachBuilder.build(
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
            ScreenshotPrivacyGuardCoachBuilder.build(
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
    fun privacyCopyStatesManualLocalStatelessAndEveryScreenshotBoundary() {
        val copy = allCopy(
            ScreenshotPrivacyGuardCoachBuilder.build(
                null,
                readiness(ReadinessLevel.READY)
            )
        )

        listOf(
            "로컬 전용·무상태 수동 안내이며 화면캡처를 감지·차단·기록하지 않고",
            "새 저장소·자동 처리·자동 전송·스케줄러를 만들지 않습니다",
            "위험도·일치 신호, 가족 준비 수준, 저장 사건 개수 범주만 사용",
            "원문·가린 단서·점수·행동 계획·요약·사건 내용·미리보기",
            "WindowManager.LayoutParams.FLAG_SECURE를 사용하지 않으므로",
            "미디어스토어 콘텐츠 감시자·접근성 서비스·기타 방법으로 실제 화면캡처 발생을 감지하지 않고",
            "캡처된 이미지는 이 앱이 아니라 기기의 갤러리·카메라 롤에 보관되며",
            "갤러리·카메라·CameraX·Camera2·미디어스토어·콘텐츠 감시자·OCR·이미지 분석·파일 선택기·클립보드",
            "카메라·마이크·MediaRecorder·AudioRecord·QR·바코드 스캔·얼굴 인식·생체 인증을 사용하지 않습니다",
            "파일·문서·사진·영상·미디어·갤러리·저장소를 읽거나 고르지 않고",
            "화면캡처·갤러리·클라우드 백업·공유 상태를 검사·분석·일치·확인·검증·유효성 판정하지 않습니다",
            "전화·다이얼·발신·통화·종료·선별·차단·통화 상태·통화 기록·발신자 표시",
            "네트워크·웹 탐색·브라우저·웹뷰·외부 링크 열기",
            "클립보드·텍스트 감시기·자동완성·자격 증명 API",
            "결제·은행·ATM·NFC·지갑·블록체인·상품권 동작",
            "전화 통신·SIM·구독·통신사 API",
            "앱·패키지·설치 앱 목록·앱 이름·권한·기기·설정 상태",
            "기기 관리자·접근성 서비스·VPN·사용 통계·알림 수신 서비스",
            "공식·실제·안전·보안·검증·유효·접수·제출·수신·해결·완료·차단·감지·삭제됨",
            "파일·스트림·URI를 붙이거나 자동 발송하지 않습니다",
            "새 Android 권한을 요청하지 않고",
            "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커·스케줄러·브로드캐스트 수신기"
        ).forEach { boundary ->
            assertTrue("privacy copy omitted $boundary", copy.contains(boundary))
        }
        assertFalse(copy.contains("MediaStore"))
        assertFalse(copy.contains("ContentObserver"))
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndAutomaticDetectionClaims() {
        val copy = allCopy(
            ScreenshotPrivacyGuardCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.READY),
                incidentCount = 1
            )
        )

        listOf(
            "screenshot detected",
            "capture blocked",
            "gallery protected",
            "cloud backup safe",
            "upload completed",
            "share submitted",
            "problem-free",
            "화면캡처가 감지됐습니다",
            "캡처가 차단됐습니다",
            "갤러리가 보호됐습니다",
            "클라우드 백업이 안전합니다",
            "정리가 완료됐습니다",
            "공유가 접수됐습니다",
            "계정이 보호됐습니다",
            "신고가 제출됐습니다",
            "문제없습니다",
            "자동 화면캡처 감지",
            "자동 캡처 차단",
            "automatic monitoring",
            "automatic detection",
            "automatic blocking",
            "automatic deletion",
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
            "app/src/main/java/com/mongmong/familyshield/ScreenshotPrivacyGuardCoach.kt"
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
        assertFalse(source.contains("MediaStore"))
        assertFalse(source.contains("ContentObserver"))
        assertFalse(source.contains("AccessibilityService"))

        val implementationSource = stripStringLiterals(source)
        assertFalse(implementationSource.contains("FLAG_SECURE"))
        listOf(
            "Context",
            "Intent",
            "SharedPreferences",
            "SafetyPlan",
            "IncidentStore",
            "IncidentPreviewRedactor",
            "OfficialHelpDirectoryBuilder",
            "CameraManager",
            "CameraDevice",
            "androidx.camera",
            "ImageCapture",
            "ImageAnalysis",
            "MediaRecorder",
            "AudioRecord",
            "TextRecognition",
            "BarcodeScanner",
            "QRCodeReader",
            "TelephonyManager",
            "SubscriptionManager",
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
            "ContentObserver",
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
                "screenshot guard model used forbidden implementation source: $forbiddenSource",
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
            "private fun LinearLayout.screenshotPrivacyGuardCoachScreen"
        )
        val cardSource = functionBlock(
            activitySource,
            "private fun LinearLayout.screenshotPrivacyGuardCard"
        )
        val routerSource = functionBlock(
            activitySource,
            "private fun openScreenshotPrivacyGuardAction"
        )
        val shareSource = functionBlock(
            activitySource,
            "private fun shareScreenshotPrivacyGuardCoach"
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
            "card.screenshotPressureSituation",
            "card.valueFreePauseScript",
            "card.whyItMatters",
            "card.doNotCaptureDetectStoreUploadOrAutoShareBoundaries",
            "card.primaryAction",
            "card.secondaryActions"
        ).forEach { renderedField -> assertTrue(cardSource.contains(renderedField)) }

        assertTrue(screenSource.contains("shareScreenshotPrivacyGuardCoach(coach.familyShareSummary)"))
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
            "androidx.camera",
            "ImageCapture",
            "ImageAnalysis",
            "MediaRecorder",
            "AudioRecord",
            "TextRecognition",
            "BarcodeScanner",
            "QRCodeReader",
            "TelephonyManager",
            "SubscriptionManager",
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
            "ContentObserver",
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
                "bounded screenshot guard UI used forbidden implementation: $forbiddenApi",
                boundedImplementationSource.contains(forbiddenApi, ignoreCase = true)
            )
        }
        assertEquals(1, Regex("openHelpDirectory\\(").findAll(routerSource).count())
        assertFalse((screenSource + cardSource).contains("openHelpDirectory("))
    }

    @Test
    fun highRiskShowsAllCardsAndAllExistingRoutes() {
        val coach = ScreenshotPrivacyGuardCoachBuilder.build(
            analysis(RiskLevel.HIGH, ScamSignal.SECRECY),
            readiness(ReadinessLevel.READY)
        )

        assertEquals(ScreenshotPrivacyGuardCardId.entries, coach.recommendedCardIds)
        assertEquals(ScreenshotPrivacyGuardActionId.entries, coach.recommendedActionIds)
        assertEquals(
            ScreenshotPrivacyGuardActionId.entries.size,
            coach.recommendedActions.map { action -> action.label }.distinct().size
        )
    }

    @Test
    fun unrelatedSignalCategoriesProduceDistinctDeterministicRecommendations() {
        fun coachFor(signal: ScamSignal) = ScreenshotPrivacyGuardCoachBuilder.build(
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
            credential.cards.all { card -> card.screenshotPressureSituation.endsWith("때") }
        )
    }

    private fun assertRoutes(
        coach: ScreenshotPrivacyGuardCoach,
        vararg expected: ScreenshotPrivacyGuardActionId
    ) {
        assertTrue(coach.recommendedActionIds.containsAll(expected.toList()))
    }

    private fun assertNoPrivateValues(
        coach: ScreenshotPrivacyGuardCoach,
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

    private fun allCopy(coach: ScreenshotPrivacyGuardCoach): String = buildString {
        appendLine(coach.title)
        appendLine(coach.intro)
        appendLine(coach.modeLabel)
        appendLine(coach.recommendationTitle)
        appendLine(coach.recommendationExplanation)
        coach.recommendedActions.forEach { action -> appendLine(action.label) }
        coach.cards.forEach { card ->
            appendLine(card.title)
            appendLine(card.screenshotPressureSituation)
            appendLine(card.valueFreePauseScript)
            appendLine(card.whyItMatters)
            card.doNotCaptureDetectStoreUploadOrAutoShareBoundaries.forEach(::appendLine)
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
