package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyFlowRouterBuilderTest {
    @Test
    fun noAnalysisHasStableDefaultOrderAndEveryRoute() {
        val router = SafetyFlowRouterBuilder.build(null, emptyReadiness())

        assertEquals("분석 전 기본 안내", router.recommendationTitle)
        assertEquals(
            listOf(
                SafetyFlowRouteId.SUSPICIOUS_REQUEST,
                SafetyFlowRouteId.FAMILY_OR_NUMBER_CHANGE,
                SafetyFlowRouteId.MONEY_OR_INFORMATION_SENT,
                SafetyFlowRouteId.REMOTE_ACCESS_GRANTED,
                SafetyFlowRouteId.OFFICIAL_HELP,
                SafetyFlowRouteId.EVIDENCE_PRESERVATION,
                SafetyFlowRouteId.PRIVACY
            ),
            router.routes.map { route -> route.id }
        )
        assertEquals((1..7).toList(), router.routes.map { route -> route.displayOrder })
        assertEquals(
            listOf(
                SafetyFlowRouteId.SUSPICIOUS_REQUEST,
                SafetyFlowRouteId.FAMILY_OR_NUMBER_CHANGE
            ),
            router.recommendedRouteIds
        )
        assertEquals(router.recommendedRouteIds, router.recommendedRoutes.map { route -> route.id })
        assertEquals(SafetyFlowRouteId.entries.size, router.routes.size)
        assertEquals(router.routes.size, router.routes.map { route -> route.id }.distinct().size)
    }

    @Test
    fun highRiskMoneyOtpRemoteAndSafeAccountRecommendsResponseReportEvidenceAndHelp() {
        val analysis = ScamAnalyzer.analyze(
            "은행 보안팀입니다. 안전계좌 123456789012로 지금 바로 송금하고 " +
                "원격제어 앱 설치 후 OTP 482913을 알려주세요."
        )

        val router = SafetyFlowRouterBuilder.build(analysis, emptyReadiness())

        assertEquals(RiskLevel.HIGH, analysis.riskLevel)
        listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.CREDENTIALS,
            ScamSignal.APP_INSTALL,
            ScamSignal.SAFE_ACCOUNT_FRAUD
        ).forEach { signal -> assertTrue("analysis omitted $signal", signal in analysis.matchedSignals) }
        assertEquals(
            listOf(
                SafetyFlowRouteId.MONEY_OR_INFORMATION_SENT,
                SafetyFlowRouteId.REMOTE_ACCESS_GRANTED,
                SafetyFlowRouteId.EVIDENCE_PRESERVATION,
                SafetyFlowRouteId.OFFICIAL_HELP
            ),
            router.recommendedRouteIds
        )
        assertEquals(
            listOf(
                SafetyFlowActionId.RESPONSE_CENTER,
                SafetyFlowActionId.REPORT_PREP,
                SafetyFlowActionId.EVIDENCE_KIT,
                SafetyFlowActionId.OFFICIAL_HELP_DIRECTORY
            ),
            router.recommendedActionIds
        )
    }

    @Test
    fun familyEmergencyDeepfakeAndNumberChangeRecommendsFamilyCallbackFirst() {
        val analysis = ScamAnalyzer.analyze(
            "엄마, 나 휴대폰 고장났고 번호 바뀌었어. 딥페이크 목소리 통화야. " +
                "지금 바로 계좌로 송금해줘."
        )

        val router = SafetyFlowRouterBuilder.build(analysis, readyReadiness())

        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal -> assertTrue("analysis omitted $signal", signal in analysis.matchedSignals) }
        assertEquals(SafetyFlowRouteId.FAMILY_OR_NUMBER_CHANGE, router.recommendedRouteIds.first())
        assertEquals(
            SafetyFlowActionId.FAMILY_CALLBACK_CARD,
            router.recommendedRoutes.first().primaryAction.id
        )
    }

    @Test
    fun linkSmishingRecommendsCheckerResultEvidenceAndHelpWithoutAutomaticScanning() {
        val analysis = ScamAnalyzer.analyze(
            "택배 배송 조회는 링크 클릭 https://short.example/parcel 에서 확인하세요."
        )

        val router = SafetyFlowRouterBuilder.build(analysis, emptyReadiness())
        val suspiciousRoute = router.routes.single { route -> route.id == SafetyFlowRouteId.SUSPICIOUS_REQUEST }

        assertTrue(ScamSignal.LINK in analysis.matchedSignals)
        assertTrue(ScamSignal.PARCEL_INVITATION_SMISHING in analysis.matchedSignals)
        assertEquals(
            listOf(
                SafetyFlowRouteId.SUSPICIOUS_REQUEST,
                SafetyFlowRouteId.EVIDENCE_PRESERVATION,
                SafetyFlowRouteId.OFFICIAL_HELP
            ),
            router.recommendedRouteIds
        )
        listOf(
            SafetyFlowActionId.CHECKER,
            SafetyFlowActionId.ANALYSIS_RESULT,
            SafetyFlowActionId.EVIDENCE_KIT,
            SafetyFlowActionId.OFFICIAL_HELP_DIRECTORY
        ).forEach { actionId ->
            assertTrue("link recommendation omitted $actionId", actionId in router.recommendedActionIds)
        }
        assertFalse(SafetyFlowActionId.RESPONSE_CENTER in router.recommendedActionIds)
        assertTrue(suspiciousRoute.explanation.contains("직접 붙여넣"))
        assertTrue(
            suspiciousRoute.safetyNotes.any { note ->
                note.contains("자동으로 찾는 기능이 아니라") && note.contains("직접 붙여넣")
            }
        )
        assertFalse(allCopy(router).contains("자동으로 검사합니다"))
        assertFalse(allCopy(router).contains("자동 스캔합니다"))
    }

    @Test
    fun everyRouteCardAndRouterHeadingHasCompleteCopyAndActions() {
        val router = SafetyFlowRouterBuilder.build(null, emptyReadiness())

        assertTrue(router.title.isNotBlank())
        assertTrue(router.intro.isNotBlank())
        assertTrue(router.recommendationTitle.isNotBlank())
        assertTrue(router.recommendationExplanation.isNotBlank())
        assertTrue(router.privacySafetyNotes.isNotEmpty())
        assertTrue(router.privacySafetyNotes.all { note -> note.isNotBlank() })
        router.routes.forEach { route ->
            assertTrue(route.displayOrder > 0)
            assertTrue(route.urgency.rank >= 0)
            assertTrue(route.urgency.label.isNotBlank())
            assertTrue(route.label.isNotBlank())
            assertTrue(route.explanation.isNotBlank())
            assertTrue(route.primaryAction.label.isNotBlank())
            assertTrue(route.secondaryActions.all { action -> action.label.isNotBlank() })
            assertTrue(route.safetyNotes.isNotEmpty())
            assertTrue(route.safetyNotes.all { note -> note.isNotBlank() })
        }
    }

    @Test
    fun shareContainsEveryRouteAndActionButNoRawCluePlanOrSensitiveValues() {
        val safeWord = "해질녘 파란 우산은 어디에?"
        val callbackScript = "푸른 메모장을 열고 첫 번째 저장 번호로 전화한다."
        val contacts = listOf(
            TrustedContact("보호자-푸른별", "010-2468-1357"),
            TrustedContact("예비-은하수", "010-9753-8642")
        )
        val plan = SafetyPlan(safeWord, callbackScript, contacts)
        val raw =
            "원문-비밀-문장 010-1234-5678 안전계좌 123456789012 비밀번호 BluePassword77 " +
                "OTP A1B2C3 PIN 7788 https://evil.example 주민번호 900101-1234567"
        val analysis = ScamAnalyzer.analyze(raw)
        val router = SafetyFlowRouterBuilder.build(analysis, FamilyReadinessBuilder.build(plan))
        val summary = router.familyShareSummary
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)

        router.routes.forEach { route ->
            assertTrue("share omitted route label: ${route.label}", summary.contains(route.label))
            (listOf(route.primaryAction) + route.secondaryActions).forEach { action ->
                assertTrue("share omitted action label: ${action.label}", summary.contains(action.label))
            }
        }
        assertTrue(summary.contains("원문") && summary.contains("포함하지 않았습니다"))
        assertFalse(summary.contains(raw))
        assertFalse(summary.contains(redactedClue))
        listOf(
            "원문-비밀-문장",
            safeWord,
            callbackScript,
            contacts[0].label,
            contacts[0].number,
            contacts[1].label,
            contacts[1].number,
            "010-1234-5678",
            "123456789012",
            "BluePassword77",
            "A1B2C3",
            "7788",
            "evil.example",
            "900101-1234567"
        ).forEach { secret ->
            assertFalse("router share leaked secret: $secret", summary.contains(secret))
        }
        assertFalse(Regex("0\\d{1,2}[- ]?\\d{3,4}[- ]?\\d{4}").containsMatchIn(summary))
        assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(summary))
        assertFalse(Regex("(?i)https?://|www\\.|[a-z0-9-]+\\.(com|net|kr)").containsMatchIn(summary))
    }

    @Test
    fun outputIsDeterministicForTheSameAnalysisAndReadiness() {
        val analysis = ScamAnalyzer.analyze(
            "엄마 나 번호 바뀌었어. 택배 링크 클릭 https://short.example/parcel"
        )
        val readiness = readyReadiness()

        val first = SafetyFlowRouterBuilder.build(analysis, readiness)
        val second = SafetyFlowRouterBuilder.build(analysis, readiness)

        assertEquals(first, second)
        assertEquals(first.routes, second.routes)
        assertEquals(first.recommendedRouteIds, second.recommendedRouteIds)
        assertEquals(first.recommendedActionIds, second.recommendedActionIds)
        assertEquals(first.familyShareSummary, second.familyShareSummary)
    }

    @Test
    fun copyStatesLocalManualNoStorageAndNoIncidentMutationWithoutUnsafeClaims() {
        val routers = listOf(
            SafetyFlowRouterBuilder.build(null, emptyReadiness()),
            SafetyFlowRouterBuilder.build(
                ScamAnalyzer.analyze("택배 링크 클릭 https://short.example/parcel"),
                readyReadiness()
            ),
            SafetyFlowRouterBuilder.build(
                ScamAnalyzer.analyze("안전계좌로 지금 바로 송금하고 OTP 482913을 알려줘"),
                emptyReadiness()
            )
        )
        val forbiddenAffirmativeClaims = listOf(
            "자동으로 감시합니다",
            "자동 모니터링합니다",
            "몰래 확인합니다",
            "자동으로 검사합니다",
            "자동 스캔합니다",
            "자동으로 읽습니다",
            "자동으로 수집합니다",
            "연락처를 자동으로 읽습니다",
            "연락처 앱에서 자동으로 가져옵니다",
            "연락처 권한을 요청합니다",
            "연락처 권한을 사용합니다",
            "연락처 권한이 필요합니다",
            "서버로 업로드합니다",
            "네트워크로 업로드합니다",
            "네트워크로 전송합니다",
            "법적 효력을 보장합니다",
            "법적 결과를 보장합니다",
            "피해 회복을 보장합니다",
            "피해금 회수를 보장합니다",
            "반드시 환급됩니다",
            "반드시 회수됩니다",
            "환불 보장",
            "자동으로 삭제합니다",
            "자동 삭제합니다",
            "100%",
            "automatic monitoring",
            "reads contacts automatically",
            "server upload",
            "legal guarantee",
            "guaranteed recovery"
        )

        routers.forEach { router ->
            val allCopy = allCopy(router)
            val privacyCopy = router.privacySafetyNotes.joinToString(" ")

            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "router copy contained unsafe claim: $claim",
                    allCopy.lowercase().contains(claim.lowercase())
                )
            }
            assertTrue(privacyCopy.contains("기기 안"))
            assertTrue(privacyCopy.contains("사용자가 직접 붙여넣"))
            assertTrue(privacyCopy.contains("경로 선택") && privacyCopy.contains("저장하지 않"))
            assertTrue(
                privacyCopy.contains("사건 기록") &&
                    privacyCopy.contains("자동으로") &&
                    privacyCopy.contains("추가하거나 수정하지 않")
            )
            assertTrue(
                privacyCopy.contains("문자·통화·연락처") &&
                    privacyCopy.contains("자동으로 읽거나 감시하지 않")
            )
            assertTrue(privacyCopy.contains("네트워크 분석") && privacyCopy.contains("하지 않습니다"))
        }
    }

    private fun emptyReadiness(): FamilyReadiness = FamilyReadinessBuilder.build(
        SafetyPlan(safeWord = "", callbackScript = "", contacts = emptyList())
    )

    private fun readyReadiness(): FamilyReadiness = FamilyReadinessBuilder.build(
        SafetyPlan(
            safeWord = "우리 가족만 아는 질문",
            callbackScript = "통화를 끊고 저장된 번호로 다시 전화한다.",
            contacts = listOf(
                TrustedContact("가족", "010-1111-2222"),
                TrustedContact("예비 가족", "010-3333-4444")
            )
        )
    )

    private fun allCopy(router: SafetyFlowRouter): String = buildString {
        append(router.title)
        append(router.intro)
        append(router.recommendationTitle)
        append(router.recommendationExplanation)
        router.routes.forEach { route ->
            append(route.urgency.label)
            append(route.label)
            append(route.explanation)
            append(route.primaryAction.label)
            route.secondaryActions.forEach { action -> append(action.label) }
            route.safetyNotes.forEach(::append)
        }
        router.privacySafetyNotes.forEach(::append)
        append(router.familyShareSummary)
    }
}
