package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveContactExitScriptsBuilderTest {
    @Test
    fun noAnalysisScriptsAreConservativeAndNeverDeclareTheContactSafe() {
        val scripts = ActiveContactExitScriptsBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(ActiveContactExitScriptCardId.entries, scripts.recommendedCardIds)
        assertTrue(
            scripts.recommendedActionIds.containsAll(
                listOf(
                    ActiveContactExitScriptActionId.QUICK_STOP,
                    ActiveContactExitScriptActionId.CHECKER,
                    ActiveContactExitScriptActionId.PRIVACY_CENTER,
                    ActiveContactExitScriptActionId.FAMILY_CALLBACK_CARD,
                    ActiveContactExitScriptActionId.SAFE_CALL_SCRIPTS,
                    ActiveContactExitScriptActionId.SAFETY_FLOW_ROUTER
                )
            )
        )
        assertTrue(scripts.recommendationExplanation.contains("현재 분석이 없거나 일치 신호가 없습니다"))
        assertTrue(scripts.recommendationExplanation.contains("문제없다는 판정으로 보지 말고"))

        val copy = allCopy(scripts)
        listOf(
            "안전합니다",
            "문제없습니다",
            "사기가 아닙니다",
            "이 연락은 안전",
            "100%",
            " safe ",
            "blocked"
        ).forEach { unsafeClaim ->
            assertFalse(
                "default scripts made unsafe claim: $unsafeClaim",
                copy.contains(unsafeClaim, ignoreCase = true)
            )
        }
    }

    @Test
    fun highRiskFinancialUrgentTransferAndCredentialsPrioritizeExitAndResponseRoutes() {
        val cases = listOf(
            analysis(RiskLevel.HIGH),
            analysis(RiskLevel.MEDIUM, ScamSignal.URGENT_TRANSFER),
            analysis(RiskLevel.MEDIUM, ScamSignal.CREDENTIALS),
            analysis(
                RiskLevel.HIGH,
                ScamSignal.URGENT_TRANSFER,
                ScamSignal.CREDENTIALS,
                ScamSignal.SAFE_ACCOUNT_FRAUD
            )
        )

        cases.forEach { current ->
            val scripts = ActiveContactExitScriptsBuilder.build(
                analysis = current,
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(ActiveContactExitScriptCardId.HANG_UP_CALL in scripts.recommendedCardIds)
            assertTrue(
                ActiveContactExitScriptCardId.REFUSE_CREDENTIALS in scripts.recommendedCardIds
            )
            assertTrue(
                scripts.recommendedActionIds.containsAll(
                    listOf(
                        ActiveContactExitScriptActionId.QUICK_STOP,
                        ActiveContactExitScriptActionId.ACCOUNT_LOCKDOWN,
                        ActiveContactExitScriptActionId.EVIDENCE_KIT,
                        ActiveContactExitScriptActionId.OFFICIAL_HELP_DIRECTORY,
                        ActiveContactExitScriptActionId.REPORT_PREP
                    )
                )
            )
            val hangUp = scripts.card(ActiveContactExitScriptCardId.HANG_UP_CALL)
            val credentialRefusal = scripts.card(
                ActiveContactExitScriptCardId.REFUSE_CREDENTIALS
            )
            assertTrue(hangUp.scriptText.contains("통화를 끊겠습니다"))
            assertTrue(credentialRefusal.scriptText.contains("말하거나 보내지 않겠습니다"))
            assertTrue(credentialRefusal.scriptText.contains("연락을 끝내겠습니다"))
        }

        val highWithoutCredentialSignal = ActiveContactExitScriptsBuilder.build(
            analysis = analysis(RiskLevel.HIGH, ScamSignal.LINK),
            readiness = readiness(ReadinessLevel.READY)
        )
        assertFalse(
            highWithoutCredentialSignal.recommendationExplanation
                .contains("인증정보 요구 신호가 있어")
        )
        assertTrue(
            highWithoutCredentialSignal.recommendationExplanation
                .contains("위험도가 높거나 금융·송금·결제 압박 신호")
        )
    }

    @Test
    fun appInstallAndLiveCallControlRecommendRefusalTrustedHelperAndAccountRoutes() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val scripts = ActiveContactExitScriptsBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                ActiveContactExitScriptCardId.REFUSE_APP_SCREEN_SHARE in
                    scripts.recommendedCardIds
            )
            assertTrue(
                scripts.recommendedActionIds.containsAll(
                    listOf(
                        ActiveContactExitScriptActionId.TRUSTED_HELPER_REVIEW,
                        ActiveContactExitScriptActionId.ACCOUNT_LOCKDOWN,
                        ActiveContactExitScriptActionId.EVIDENCE_KIT,
                        ActiveContactExitScriptActionId.SAFETY_FLOW_ROUTER
                    )
                )
            )
            val refusal = scripts.card(
                ActiveContactExitScriptCardId.REFUSE_APP_SCREEN_SHARE
            )
            assertTrue(refusal.scriptText.contains("앱 설치·화면공유·원격제어는 하지 않겠습니다"))
            assertTrue(refusal.scriptText.contains("연락을 끝내겠습니다"))
            assertTrue(scripts.recommendationExplanation.contains("곁의 보호자"))

            val copy = allCopy(scripts)
            listOf(
                "악성 프로그램을 제거합니다",
                "악성 앱을 제거합니다",
                "악성 앱 제거 완료",
                "원격 연결을 자동 차단합니다",
                "기기를 자동 복구합니다"
            ).forEach { guarantee -> assertFalse(copy.contains(guarantee)) }
        }
    }

    @Test
    fun linkAndSmishingRecommendNoClickNoReplyAndExistingReviewRoutes() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val scripts = ActiveContactExitScriptsBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                scripts.recommendedCardIds.containsAll(
                    listOf(
                        ActiveContactExitScriptCardId.STOP_MESSAGE_REPLY,
                        ActiveContactExitScriptCardId.STOP_LINK
                    )
                )
            )
            assertTrue(
                scripts.recommendedActionIds.containsAll(
                    listOf(
                        ActiveContactExitScriptActionId.CHECKER,
                        ActiveContactExitScriptActionId.EXPOSURE_IMPACT_TRIAGE,
                        ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES,
                        ActiveContactExitScriptActionId.PRIVACY_CENTER
                    )
                )
            )
            val stopLink = scripts.card(ActiveContactExitScriptCardId.STOP_LINK)
            assertTrue(stopLink.scriptText.contains("링크는 열지 않고 답장도 하지 않겠습니다"))
            assertTrue(scripts.recommendationExplanation.contains("링크를 검사하지 않습니다"))
            assertFalse(allCopy(scripts).contains("링크를 자동 검사합니다"))
            assertFalse(allCopy(scripts).contains("URL을 스캔합니다"))
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendExistingNumberRefusalAndFamilyRoutes() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val scripts = ActiveContactExitScriptsBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                ActiveContactExitScriptCardId.VERIFY_FAMILY_EXISTING_NUMBER in
                    scripts.recommendedCardIds
            )
            assertTrue(
                scripts.recommendedActionIds.containsAll(
                    listOf(
                        ActiveContactExitScriptActionId.FAMILY_CALLBACK_CARD,
                        ActiveContactExitScriptActionId.SAFE_CALL_SCRIPTS,
                        ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES
                    )
                )
            )
            val familyRefusal = scripts.card(
                ActiveContactExitScriptCardId.VERIFY_FAMILY_EXISTING_NUMBER
            )
            assertTrue(familyRefusal.scriptText.contains("새 번호나 목소리만으로 확인하지 않겠습니다"))
            assertTrue(familyRefusal.scriptText.contains("평소 알던 가족 경로"))
            assertTrue(familyRefusal.neverSayOrDo.joinToString().contains("가족 확인 답"))
        }
    }

    @Test
    fun secrecyAndImpersonationRecommendNoSecretContactExitAndOfficialRoute() {
        listOf(ScamSignal.SECRECY, ScamSignal.IMPERSONATION).forEach { signal ->
            val scripts = ActiveContactExitScriptsBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(ActiveContactExitScriptCardId.HANG_UP_CALL in scripts.recommendedCardIds)
            assertTrue(ActiveContactExitScriptCardId.END_IMPERSONATION in scripts.recommendedCardIds)
            assertTrue(
                scripts.recommendedActionIds.containsAll(
                    listOf(
                        ActiveContactExitScriptActionId.QUICK_STOP,
                        ActiveContactExitScriptActionId.OFFICIAL_HELP_DIRECTORY
                    )
                )
            )
            val exit = scripts.card(ActiveContactExitScriptCardId.END_IMPERSONATION)
            assertTrue(exit.scriptText.contains("이 연락은 여기서 끝내겠습니다"))
            assertTrue(exit.scriptText.contains("비밀로 하지 않고"))
            assertTrue(exit.scriptText.contains("직접 찾은 공식 경로"))
        }
    }

    @Test
    fun unrelatedSignalCategoriesProduceDifferentFocusedRecommendations() {
        fun scriptsFor(signal: ScamSignal) = ActiveContactExitScriptsBuilder.build(
            analysis = analysis(RiskLevel.MEDIUM, signal),
            readiness = readiness(ReadinessLevel.READY)
        )

        val remote = scriptsFor(ScamSignal.APP_INSTALL)
        val link = scriptsFor(ScamSignal.LINK)
        val family = scriptsFor(ScamSignal.FAMILY_EMERGENCY)
        val secrecy = scriptsFor(ScamSignal.SECRECY)

        assertNotEquals(remote.recommendedCardIds, link.recommendedCardIds)
        assertNotEquals(remote.recommendedActionIds, link.recommendedActionIds)
        assertNotEquals(family.recommendedCardIds, secrecy.recommendedCardIds)
        assertNotEquals(family.recommendedActionIds, secrecy.recommendedActionIds)

        assertTrue(ActiveContactExitScriptCardId.REFUSE_APP_SCREEN_SHARE in remote.recommendedCardIds)
        assertFalse(ActiveContactExitScriptCardId.STOP_LINK in remote.recommendedCardIds)
        assertTrue(ActiveContactExitScriptActionId.TRUSTED_HELPER_REVIEW in remote.recommendedActionIds)
        assertFalse(ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES in remote.recommendedActionIds)

        assertTrue(ActiveContactExitScriptCardId.STOP_LINK in link.recommendedCardIds)
        assertFalse(ActiveContactExitScriptCardId.REFUSE_APP_SCREEN_SHARE in link.recommendedCardIds)
        assertTrue(ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES in link.recommendedActionIds)
        assertFalse(ActiveContactExitScriptActionId.TRUSTED_HELPER_REVIEW in link.recommendedActionIds)

        assertTrue(
            ActiveContactExitScriptCardId.VERIFY_FAMILY_EXISTING_NUMBER in
                family.recommendedCardIds
        )
        assertFalse(
            ActiveContactExitScriptCardId.VERIFY_FAMILY_EXISTING_NUMBER in
                secrecy.recommendedCardIds
        )
        assertTrue(ActiveContactExitScriptCardId.END_IMPERSONATION in secrecy.recommendedCardIds)
    }

    @Test
    fun savedIncidentChangesRecommendationsWithoutReadingContentsAndClampsCounts() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.FAMILY_EMERGENCY)
        val ready = readiness(ReadinessLevel.READY)
        val zero = ActiveContactExitScriptsBuilder.build(current, ready, incidentCount = 0)
        val negative = ActiveContactExitScriptsBuilder.build(current, ready, incidentCount = -99)
        val one = ActiveContactExitScriptsBuilder.build(current, ready, incidentCount = 1)
        val many = ActiveContactExitScriptsBuilder.build(
            current,
            ready,
            incidentCount = Int.MAX_VALUE
        )

        assertEquals(zero, negative)
        assertEquals(one, many)
        assertNotEquals(zero, one)
        assertFalse(
            ActiveContactExitScriptActionId.EVIDENCE_KIT in zero.recommendedActionIds
        )
        assertFalse(
            ActiveContactExitScriptActionId.INCIDENT_TIMELINE_BRIEFING in
                zero.recommendedActionIds
        )
        assertTrue(
            one.recommendedActionIds.containsAll(
                listOf(
                    ActiveContactExitScriptActionId.EVIDENCE_KIT,
                    ActiveContactExitScriptActionId.INCIDENT_TIMELINE_BRIEFING
                )
            )
        )
        assertTrue(one.recommendationExplanation.contains("저장 사건이 있다는 개수 범주만 반영"))
        assertTrue(one.recommendationExplanation.contains("사건 내용은 읽지 않습니다"))
        assertFalse(one.familyShareSummary.contains(Int.MAX_VALUE.toString()))
    }

    @Test
    fun lowAndInProgressReadinessAddCallbackRoadmapAndFamilySupportWhileReadyIsCautious() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.APP_INSTALL)
        val low = ActiveContactExitScriptsBuilder.build(
            current,
            readiness(ReadinessLevel.LOW)
        )
        val inProgress = ActiveContactExitScriptsBuilder.build(
            current,
            readiness(ReadinessLevel.IN_PROGRESS)
        )
        val ready = ActiveContactExitScriptsBuilder.build(
            current,
            readiness(ReadinessLevel.READY)
        )

        listOf(low, inProgress).forEach { scripts ->
            assertTrue(
                scripts.recommendedActionIds.containsAll(
                    listOf(
                        ActiveContactExitScriptActionId.FAMILY_CALLBACK_CARD,
                        ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES,
                        ActiveContactExitScriptActionId.FAMILY_READINESS_ROADMAP,
                        ActiveContactExitScriptActionId.FAMILY_CALM_DEBRIEF_AGENDA
                    )
                )
            )
            assertTrue(
                ActiveContactExitScriptCardId.TELL_FAMILY_AFTER_EXIT in
                    scripts.recommendedCardIds
            )
        }
        assertFalse(
            ActiveContactExitScriptActionId.FAMILY_READINESS_ROADMAP in
                ready.recommendedActionIds
        )
        assertTrue(
            ActiveContactExitScriptActionId.FAMILY_CALM_DEBRIEF_AGENDA in
                ready.recommendedActionIds
        )
        assertTrue(low.recommendationExplanation.contains("가족 준비가 부족"))
        assertTrue(inProgress.recommendationExplanation.contains("가족 준비가 진행 중"))
        assertTrue(
            ready.recommendationExplanation
                .contains("이번 연락의 사실이나 조치 완료가 확인된 것은 아닙니다")
        )
    }

    @Test
    fun everyCardHasCompleteFieldsStableOrderAndUniqueExistingRoutes() {
        val states = listOf(
            ActiveContactExitScriptsBuilder.build(null, readiness(ReadinessLevel.READY)),
            ActiveContactExitScriptsBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.LOW),
                incidentCount = 2
            )
        )
        val expectedTitles = listOf(
            "통화 바로 끊기 문장",
            "문자·카톡 답장 멈춤 문장",
            "인증번호·비밀번호 거절 문장",
            "앱 설치·화면공유 거절 문장",
            "가족 새 번호·목소리 사칭 확인 문장",
            "기관·은행·수사기관 사칭 종료 문장",
            "링크·청첩장·택배 문자 멈춤 문장",
            "끊은 뒤 가족에게 말할 문장"
        )

        states.forEach { scripts ->
            assertEquals(ActiveContactExitScriptCardId.entries, scripts.cards.map { it.id })
            assertEquals((1..8).toList(), scripts.cards.map { it.displayOrder })
            assertEquals(expectedTitles, scripts.cards.map { it.title })
            assertEquals(scripts.recommendedCardIds.distinct(), scripts.recommendedCardIds)
            assertEquals(
                scripts.recommendedCardIds.sortedBy { it.displayOrder },
                scripts.recommendedCardIds
            )
            assertEquals(scripts.recommendedActionIds.distinct(), scripts.recommendedActionIds)

            scripts.cards.forEach { card ->
                assertTrue(card.title.isNotBlank())
                assertTrue(card.scriptText.isNotBlank())
                assertTrue(card.whenToUse.isNotBlank())
                assertTrue(card.neverSayOrDo.isNotEmpty())
                assertTrue(card.neverSayOrDo.all { boundary -> boundary.isNotBlank() })
                assertTrue(card.primaryAction.label.isNotBlank())
                assertTrue(card.secondaryActions.all { action -> action.label.isNotBlank() })
                assertEquals(
                    card.routeActions.map { it.id }.distinct(),
                    card.routeActions.map { it.id }
                )
            }
        }
    }

    @Test
    fun actionIdsAreCompleteStableUniqueAndAllMapToExistingFlows() {
        val scripts = ActiveContactExitScriptsBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 3
        )
        val allUsedActions = scripts.cards.flatMap { it.routeActions } + scripts.recommendedActions
        val expectedActionIds = listOf(
            ActiveContactExitScriptActionId.QUICK_STOP,
            ActiveContactExitScriptActionId.CHECKER,
            ActiveContactExitScriptActionId.ANALYSIS_RESULT,
            ActiveContactExitScriptActionId.PRIVACY_CENTER,
            ActiveContactExitScriptActionId.EXPOSURE_IMPACT_TRIAGE,
            ActiveContactExitScriptActionId.FAMILY_CALLBACK_CARD,
            ActiveContactExitScriptActionId.SAFE_CALL_SCRIPTS,
            ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES,
            ActiveContactExitScriptActionId.TRUSTED_HELPER_REVIEW,
            ActiveContactExitScriptActionId.FAMILY_READINESS_ROADMAP,
            ActiveContactExitScriptActionId.ACCOUNT_LOCKDOWN,
            ActiveContactExitScriptActionId.EVIDENCE_KIT,
            ActiveContactExitScriptActionId.OFFICIAL_HELP_DIRECTORY,
            ActiveContactExitScriptActionId.REPORT_PREP,
            ActiveContactExitScriptActionId.INCIDENT_TIMELINE_BRIEFING,
            ActiveContactExitScriptActionId.SAFETY_FLOW_ROUTER,
            ActiveContactExitScriptActionId.FAMILY_CALM_DEBRIEF_AGENDA
        )

        assertEquals(expectedActionIds, ActiveContactExitScriptActionId.entries)
        assertEquals(expectedActionIds.toSet(), allUsedActions.map { it.id }.toSet())
        assertTrue(allUsedActions.all { it.label.isNotBlank() })
        assertEquals(
            expectedActionIds.filter { it in scripts.recommendedActionIds },
            scripts.recommendedActionIds
        )
    }

    @Test
    fun shareSummaryUsesOnlyGenericScriptsAndRouteLabelsAndExcludesAdversarialSecrets() {
        val rawInput =
            "RAW_SCAM_TEXT 박민수 010-9876-5432 1234567890123456 " +
                "900101-1234567 OTP 739201 PIN 8844 https://evil.example/path"
        val marker =
            "PLAN_SECRET callback-secret contact-secret 010-1111-2222 " +
                "account-987654321012 https://private.example"
        val scripts = ActiveContactExitScriptsBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                *ScamSignal.entries.toTypedArray(),
                input = rawInput
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, marker),
            incidentCount = Int.MAX_VALUE
        )
        val summary = scripts.familyShareSummary

        listOf(
            "RAW_SCAM_TEXT",
            "박민수",
            "010-9876-5432",
            "1234567890123456",
            "900101-1234567",
            "739201",
            "8844",
            "evil.example",
            "PLAN_SECRET",
            "callback-secret",
            "contact-secret",
            "010-1111-2222",
            "987654321012",
            "private.example",
            "5432",
            "3456",
            "4567"
        ).forEach { secret ->
            assertFalse("share summary leaked: $secret", summary.contains(secret))
        }
        assertFalse(Regex("(?i)https?://|www\\.").containsMatchIn(summary))
        assertFalse(Regex("\\d{6,}").containsMatchIn(summary))
        assertFalse(
            Regex("01[016789][- .]?\\d{3,4}[- .]?\\d{4}").containsMatchIn(summary)
        )
        assertFalse(Regex("\\d{6}-?[1-4]\\d{6}").containsMatchIn(summary))
        assertFalse(Regex("(?:\\d[- .]?){6,}").containsMatchIn(summary))
        assertFalse(
            Regex("(?i)(?:OTP|PIN|인증번호)\\s*[:=]?\\s*[A-Z0-9]{4,}")
                .containsMatchIn(summary)
        )

        val allowedRecommendedLines = scripts.recommendedCards.map { card ->
            "${card.title}: ${card.scriptText}"
        }.toSet()
        val allowedRouteLabels = scripts.recommendedActions.map { action -> action.label }.toSet()
        val allowedCardTitles = scripts.cards.map { card -> card.title }.toSet()
        summary.lineSequence()
            .filter { line -> line.startsWith("- ") }
            .map { line -> line.removePrefix("- ") }
            .forEach { label ->
                assertTrue(
                    "share summary used non-generic bullet: $label",
                    label in allowedRecommendedLines ||
                        label in allowedRouteLabels ||
                        label in allowedCardTitles
                )
            }
        assertFalse(summary.contains(scripts.cards.first().whenToUse))
        assertFalse(summary.contains(scripts.cards.first().neverSayOrDo.first()))
    }

    @Test
    fun builderIsDeterministicAndIgnoresRawInputScoreAndReadinessLiteralFields() {
        val firstAnalysis = analysis(
            RiskLevel.HIGH,
            ScamSignal.CREDENTIALS,
            ScamSignal.LINK,
            input = "FIRST_RAW 010-1111-2222 https://first.example 12345678"
        )
        val secondAnalysis = firstAnalysis.copy(
            input = "SECOND_RAW 010-9999-8888 https://second.example 87654321",
            score = 1
        )
        val firstReadiness = readiness(ReadinessLevel.IN_PROGRESS, "FIRST_PLAN_SECRET")
        val secondReadiness = FamilyReadiness(
            level = ReadinessLevel.IN_PROGRESS,
            title = "SECOND_PLAN_SECRET",
            completedCount = 999,
            totalCount = 1000,
            checklist = listOf(
                ReadinessItem(
                    label = "SECOND_CONTACT_SECRET",
                    done = true,
                    action = "SECOND_CALLBACK_SECRET"
                )
            ),
            nextAction = "SECOND_SAFE_WORD_SECRET"
        )

        val first = ActiveContactExitScriptsBuilder.build(firstAnalysis, firstReadiness, 1)
        val repeated = ActiveContactExitScriptsBuilder.build(firstAnalysis, firstReadiness, 1)
        val differentForbiddenValues = ActiveContactExitScriptsBuilder.build(
            secondAnalysis,
            secondReadiness,
            1
        )

        assertEquals(first, repeated)
        assertEquals(first, differentForbiddenValues)
        listOf(
            "FIRST_RAW",
            "SECOND_RAW",
            "FIRST_PLAN_SECRET",
            "SECOND_PLAN_SECRET",
            "SECOND_CONTACT_SECRET",
            "SECOND_CALLBACK_SECRET",
            "SECOND_SAFE_WORD_SECRET"
        ).forEach { forbiddenValue ->
            assertFalse(allCopy(first).contains(forbiddenValue))
            assertFalse(allCopy(differentForbiddenValues).contains(forbiddenValue))
        }
    }

    @Test
    fun privacyCopyStatesManualLocalStatelessNoStorageNoContactsAndNoAutomation() {
        val scripts = ActiveContactExitScriptsBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 1
        )
        val notes = scripts.privacySafetyNotes.joinToString(" ")

        listOf(
            "직접 읽고 직접 말하는 로컬 전용·무상태 수동 안내",
            "묻거나 저장하지 않습니다",
            "새 저장소를 만들거나 기록하지 않습니다",
            "문자·메시지·통화·연락처·클립보드",
            "자동으로 읽거나 감시하지 않습니다",
            "네트워크를 사용하거나 새 Android 권한을 요청하지 않습니다",
            "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커",
            "전화를 걸거나 끊거나 선별·차단하지 않고",
            "문자를 보내거나 답장하거나 받는 사람을 고르지 않습니다",
            "사건 기록, 가족 안전 계획이나 기기 설정을 추가·수정·삭제하지 않습니다",
            "자동 검사·감시·가림·삭제·차단하지 않고"
        ).forEach { requiredBoundary ->
            assertTrue("privacy copy omitted: $requiredBoundary", notes.contains(requiredBoundary))
        }
        assertTrue(scripts.intro.contains("전화를 걸거나 끊거나 차단하지 않고"))
        assertTrue(scripts.intro.contains("문자를 보내지 않습니다"))
        assertTrue(scripts.modeLabel.contains("직접 말하고 직접 연락 끝내기"))
        assertTrue(notes.contains("사건 내용·가족 안전 계획 값을 사용하지 않습니다"))
        assertTrue(notes.contains("스케줄러"))
        assertTrue(notes.contains("통화 자동 발신·자동 종료"))
        assertTrue(notes.contains("SMS 자동 전송 기능이 아닙니다"))
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndPositiveAutomaticClaims() {
        val scripts = ActiveContactExitScriptsBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 1
        )
        val copy = allCopy(scripts)
        val forbiddenClaims = listOf(
            "100%",
            " safe ",
            "blocked",
            "안전합니다",
            "문제없습니다",
            "사기임을 확정합니다",
            "피해를 차단했습니다",
            "통화를 차단했습니다",
            "악성 프로그램을 제거합니다",
            "악성 앱을 제거합니다",
            "악성 앱 제거 완료",
            "계정을 자동으로 잠급니다",
            "계정이 동결됐습니다",
            "카드를 자동으로 정지합니다",
            "피해금을 회수합니다",
            "복구를 보장합니다",
            "법률 결과를 보장합니다",
            "금융 결과를 보장합니다",
            "자동으로 감시합니다",
            "자동으로 스캔합니다",
            "자동으로 검사합니다",
            "자동으로 삭제합니다",
            "자동으로 가립니다",
            "자동으로 차단합니다",
            "자동으로 전화합니다",
            "메시지를 자동 전송합니다",
            "문자를 자동 전송합니다"
        )

        forbiddenClaims.forEach { claim ->
            assertFalse(
                "scripts made unsafe guarantee: $claim",
                copy.contains(claim, ignoreCase = true)
            )
        }
        listOf(
            Regex("자동(?:으로)?\\s*(?:차단|동결|정지|삭제|가림|감시|스캔|검사|전송|전화)(?:합니다|됩니다|완료)"),
            Regex("(?:차단|동결|정지|삭제|제거|복구|검사)\\s*(?:완료|됐습니다|되었습니다)"),
            Regex("(?:계정|카드|피해금).{0,12}(?:자동 조치|자동 복구|회수 보장)")
        ).forEach { positiveAutomationPattern ->
            assertFalse(
                "scripts matched positive automation claim: ${positiveAutomationPattern.pattern}",
                positiveAutomationPattern.containsMatchIn(copy)
            )
        }
        assertTrue(copy.contains("수행하지 않습니다"))
        assertTrue(copy.contains("증명하거나 보장하지 않습니다"))
    }

    @Test
    fun fixedScriptsAreShortManualExitSentencesWithoutLiteralDestinations() {
        val scripts = ActiveContactExitScriptsBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        scripts.cards.forEach { card ->
            assertTrue("script is too long: ${card.id}", card.scriptText.length <= 90)
            assertTrue(
                "script lacks manual refusal or exit wording: ${card.id}",
                listOf(
                    "않겠습니다",
                    "멈추겠습니다",
                    "끝내겠습니다",
                    "끊겠습니다",
                    "끝냈습니다"
                )
                    .any { phrase -> card.scriptText.contains(phrase) }
            )
            assertFalse(Regex("(?i)https?://|www\\.").containsMatchIn(card.scriptText))
            assertFalse(Regex("\\d{6,}").containsMatchIn(card.scriptText))
            assertFalse(
                Regex("01[016789][- .]?\\d{3,4}[- .]?\\d{4}")
                    .containsMatchIn(card.scriptText)
            )
        }
    }

    private fun analysis(
        riskLevel: RiskLevel,
        vararg signals: ScamSignal,
        input: String = "raw input that must never be used"
    ): ScamAnalysis = ScamAnalysis(
        input = input,
        score = when (riskLevel) {
            RiskLevel.SAFE -> 0
            RiskLevel.MEDIUM -> 45
            RiskLevel.HIGH -> 90
        },
        riskLevel = riskLevel,
        matchedSignals = signals.toList()
    )

    private fun readiness(
        level: ReadinessLevel,
        marker: String = "unused readiness literal"
    ): FamilyReadiness {
        val completed = when (level) {
            ReadinessLevel.LOW -> 0
            ReadinessLevel.IN_PROGRESS -> 2
            ReadinessLevel.READY -> 4
        }
        return FamilyReadiness(
            level = level,
            title = "title $marker",
            completedCount = completed,
            totalCount = 4,
            checklist = listOf(
                ReadinessItem(
                    label = "label $marker",
                    done = completed > 0,
                    action = "action $marker"
                )
            ),
            nextAction = "next $marker"
        )
    }

    private fun ActiveContactExitScripts.card(
        id: ActiveContactExitScriptCardId
    ): ActiveContactExitScriptCard = cards.single { card -> card.id == id }

    private fun allCopy(scripts: ActiveContactExitScripts): String = buildString {
        appendLine(scripts.title)
        appendLine(scripts.intro)
        appendLine(scripts.modeLabel)
        appendLine(scripts.recommendationTitle)
        appendLine(scripts.recommendationExplanation)
        scripts.cards.forEach { card ->
            appendLine(card.title)
            appendLine(card.scriptText)
            appendLine(card.whenToUse)
            card.neverSayOrDo.forEach(::appendLine)
            card.routeActions.forEach { action -> appendLine(action.label) }
        }
        scripts.recommendedActions.forEach { action -> appendLine(action.label) }
        scripts.privacySafetyNotes.forEach(::appendLine)
        append(scripts.familyShareSummary)
    }
}
