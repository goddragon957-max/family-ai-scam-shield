package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafePastePrivacyCoachBuilderTest {
    @Test
    fun noAnalysisCoachIsConservativeAndNeverDeclaresPastedTextSafe() {
        val coach = SafePastePrivacyCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )

        assertEquals(SafePastePrivacyCoachCardId.entries, coach.recommendedCardIds)
        assertTrue(
            coach.recommendedActionIds.containsAll(
                listOf(
                    SafePastePrivacyCoachActionId.CHECKER,
                    SafePastePrivacyCoachActionId.SAMPLE_LIBRARY,
                    SafePastePrivacyCoachActionId.PRIVACY_CENTER,
                    SafePastePrivacyCoachActionId.FAMILY_CALLBACK_CARD
                )
            )
        )
        assertTrue(coach.recommendationExplanation.contains("현재 분석이 없거나 일치 신호가 없습니다"))
        assertTrue(coach.recommendationExplanation.contains("판정으로 보지 말고"))

        val copy = allCopy(coach)
        listOf(
            "안전합니다",
            "문제없습니다",
            "사기가 아닙니다",
            "붙여넣은 내용은 안전",
            "100%",
            " safe ",
            "blocked"
        ).forEach { unsafeClaim ->
            assertFalse("default coach made unsafe claim: $unsafeClaim", copy.contains(unsafeClaim, true))
        }
    }

    @Test
    fun highRiskFinancialCredentialsAndUrgentTransferPrioritizeMaskingAndResponseRoutes() {
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
            val coach = SafePastePrivacyCoachBuilder.build(
                analysis = current,
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        SafePastePrivacyCoachCardId.PHONE_ACCOUNT_IDENTITY,
                        SafePastePrivacyCoachCardId.CREDENTIALS_AND_SAFETY_ANSWERS,
                        SafePastePrivacyCoachCardId.ACTION_CATEGORY_CONTEXT,
                        SafePastePrivacyCoachCardId.OPEN_EXISTING_CHECKER
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        SafePastePrivacyCoachActionId.CHECKER,
                        SafePastePrivacyCoachActionId.ACCOUNT_LOCKDOWN,
                        SafePastePrivacyCoachActionId.EVIDENCE_KIT,
                        SafePastePrivacyCoachActionId.OFFICIAL_HELP_DIRECTORY
                    )
                )
            )
            assertTrue(
                coach.recommendationExplanation.contains("금융") ||
                    coach.recommendationExplanation.contains("인증정보") ||
                    current.matchedSignals.isEmpty()
            )
            assertTrue(allCopy(coach).contains("OTP/PIN/비밀번호"))
        }

        val accountCard = cases.last().let { current ->
            SafePastePrivacyCoachBuilder.build(
                analysis = current,
                readiness = readiness(ReadinessLevel.READY)
            ).cards.single { it.id == SafePastePrivacyCoachCardId.PHONE_ACCOUNT_IDENTITY }
        }
        val credentialCard = SafePastePrivacyCoachBuilder.build(
            analysis = cases.last(),
            readiness = readiness(ReadinessLevel.READY)
        ).cards.single { it.id == SafePastePrivacyCoachCardId.CREDENTIALS_AND_SAFETY_ANSWERS }
        assertTrue(accountCard.howToMaskManually.contains("계좌번호 요구"))
        assertTrue(credentialCard.howToMaskManually.contains("인증번호(OTP) 요구"))

        val highWithoutCredentialSignal = SafePastePrivacyCoachBuilder.build(
            analysis = analysis(RiskLevel.HIGH, ScamSignal.LINK),
            readiness = readiness(ReadinessLevel.READY)
        ).recommendationExplanation
        assertFalse(highWithoutCredentialSignal.contains("인증정보 관련 신호가 있어"))
        assertTrue(highWithoutCredentialSignal.contains("인증정보 신호가 따로 없어도"))
    }

    @Test
    fun appInstallAndLiveCallControlRecommendRemoteMaskingAndExistingProtectionRoutes() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val coach = SafePastePrivacyCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                SafePastePrivacyCoachCardId.APP_AND_REMOTE_CONTROL in coach.recommendedCardIds
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        SafePastePrivacyCoachActionId.TRUSTED_HELPER_REVIEW,
                        SafePastePrivacyCoachActionId.ACCOUNT_LOCKDOWN,
                        SafePastePrivacyCoachActionId.EVIDENCE_KIT,
                        SafePastePrivacyCoachActionId.SAFETY_FLOW_ROUTER
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("앱 설치·원격제어·통화 유지"))

            val copy = allCopy(coach)
            listOf(
                "악성 프로그램을 제거합니다",
                "악성 앱 제거 완료",
                "기기를 자동 복구합니다",
                "원격 연결을 자동 차단합니다"
            ).forEach { guarantee -> assertFalse(copy.contains(guarantee)) }
        }
    }

    @Test
    fun linkAndSmishingRecommendManualLinkMaskingWithoutLinkScanClaims() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val coach = SafePastePrivacyCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(SafePastePrivacyCoachCardId.LINK_AND_DOMAIN in coach.recommendedCardIds)
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        SafePastePrivacyCoachActionId.CHECKER,
                        SafePastePrivacyCoachActionId.EXPOSURE_IMPACT_TRIAGE,
                        SafePastePrivacyCoachActionId.FAMILY_ALERT_MESSAGES,
                        SafePastePrivacyCoachActionId.PRIVACY_CENTER
                    )
                )
            )
            assertTrue(allCopy(coach).contains("[링크]"))
            assertTrue(coach.recommendationExplanation.contains("링크를 열거나 검사하지 않습니다"))
            assertFalse(allCopy(coach).contains("링크를 자동 검사합니다"))
            assertFalse(allCopy(coach).contains("URL을 스캔합니다"))
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeProtectCallbackAnswersAndRecommendFamilyRoutes() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val coach = SafePastePrivacyCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        SafePastePrivacyCoachCardId.FAMILY_CALLBACK_DETAILS,
                        SafePastePrivacyCoachCardId.CREDENTIALS_AND_SAFETY_ANSWERS
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        SafePastePrivacyCoachActionId.FAMILY_CALLBACK_CARD,
                        SafePastePrivacyCoachActionId.SAFE_CALL_SCRIPTS,
                        SafePastePrivacyCoachActionId.FAMILY_ALERT_MESSAGES
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("안전질문 답은 빼고"))
        }

        val familyCard = SafePastePrivacyCoachBuilder.build(
            analysis = analysis(RiskLevel.MEDIUM, ScamSignal.PHONE_NUMBER_CHANGE),
            readiness = readiness(ReadinessLevel.READY)
        ).cards.single { it.id == SafePastePrivacyCoachCardId.FAMILY_CALLBACK_DETAILS }
        assertTrue(familyCard.howToMaskManually.contains("가족 휴대폰 고장 사칭"))
        assertTrue(familyCard.howToMaskManually.contains("새 번호로 연락 요구"))
        assertTrue(familyCard.whatNotToPasteOrShare.joinToString().contains("안전질문 답"))
    }

    @Test
    fun secrecyAndImpersonationHideRawPressureAndRecommendStopAndOfficialRoutes() {
        listOf(ScamSignal.SECRECY, ScamSignal.IMPERSONATION).forEach { signal ->
            val coach = SafePastePrivacyCoachBuilder.build(
                analysis = analysis(RiskLevel.MEDIUM, signal),
                readiness = readiness(ReadinessLevel.READY)
            )

            assertTrue(
                coach.recommendedCardIds.containsAll(
                    listOf(
                        SafePastePrivacyCoachCardId.MINIMUM_NEEDED_CONTEXT,
                        SafePastePrivacyCoachCardId.ACTION_CATEGORY_CONTEXT
                    )
                )
            )
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        SafePastePrivacyCoachActionId.CHECKER,
                        SafePastePrivacyCoachActionId.QUICK_STOP,
                        SafePastePrivacyCoachActionId.OFFICIAL_HELP_DIRECTORY
                    )
                )
            )
            assertTrue(coach.recommendationExplanation.contains("원문 전체를 숨기고"))
            assertTrue(coach.recommendationExplanation.contains("추가 행동을 멈춘 뒤"))
        }

        val actionCard = SafePastePrivacyCoachBuilder.build(
            analysis = analysis(RiskLevel.MEDIUM, ScamSignal.IMPERSONATION),
            readiness = readiness(ReadinessLevel.READY)
        ).cards.single { it.id == SafePastePrivacyCoachCardId.ACTION_CATEGORY_CONTEXT }
        assertTrue(actionCard.howToMaskManually.contains("경찰·은행 사칭"))
    }

    @Test
    fun unrelatedSignalCategoriesProduceDifferentFocusedRecommendations() {
        fun coachFor(signal: ScamSignal) = SafePastePrivacyCoachBuilder.build(
            analysis = analysis(RiskLevel.MEDIUM, signal),
            readiness = readiness(ReadinessLevel.READY)
        )

        val remote = coachFor(ScamSignal.APP_INSTALL)
        val link = coachFor(ScamSignal.LINK)
        val family = coachFor(ScamSignal.FAMILY_EMERGENCY)
        val secrecy = coachFor(ScamSignal.SECRECY)

        assertNotEquals(remote.recommendedCardIds, link.recommendedCardIds)
        assertNotEquals(remote.recommendedActionIds, link.recommendedActionIds)
        assertNotEquals(family.recommendedCardIds, secrecy.recommendedCardIds)
        assertNotEquals(family.recommendedActionIds, secrecy.recommendedActionIds)

        assertTrue(SafePastePrivacyCoachCardId.APP_AND_REMOTE_CONTROL in remote.recommendedCardIds)
        assertFalse(SafePastePrivacyCoachCardId.LINK_AND_DOMAIN in remote.recommendedCardIds)
        assertTrue(SafePastePrivacyCoachActionId.TRUSTED_HELPER_REVIEW in remote.recommendedActionIds)
        assertFalse(SafePastePrivacyCoachActionId.FAMILY_ALERT_MESSAGES in remote.recommendedActionIds)

        assertTrue(SafePastePrivacyCoachCardId.LINK_AND_DOMAIN in link.recommendedCardIds)
        assertFalse(SafePastePrivacyCoachCardId.APP_AND_REMOTE_CONTROL in link.recommendedCardIds)
        assertTrue(SafePastePrivacyCoachActionId.FAMILY_ALERT_MESSAGES in link.recommendedActionIds)
        assertFalse(SafePastePrivacyCoachActionId.TRUSTED_HELPER_REVIEW in link.recommendedActionIds)

        assertTrue(SafePastePrivacyCoachCardId.FAMILY_CALLBACK_DETAILS in family.recommendedCardIds)
        assertTrue(SafePastePrivacyCoachActionId.FAMILY_CALLBACK_CARD in family.recommendedActionIds)
        assertFalse(SafePastePrivacyCoachActionId.QUICK_STOP in family.recommendedActionIds)

        assertFalse(SafePastePrivacyCoachCardId.FAMILY_CALLBACK_DETAILS in secrecy.recommendedCardIds)
        assertTrue(SafePastePrivacyCoachActionId.QUICK_STOP in secrecy.recommendedActionIds)
        assertFalse(SafePastePrivacyCoachActionId.FAMILY_CALLBACK_CARD in secrecy.recommendedActionIds)
    }

    @Test
    fun savedIncidentChangesCategoryRecommendationsWithoutReadingContentsAndClampsCounts() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.LINK)
        val ready = readiness(ReadinessLevel.READY)
        val zero = SafePastePrivacyCoachBuilder.build(current, ready, incidentCount = 0)
        val negative = SafePastePrivacyCoachBuilder.build(current, ready, incidentCount = -99)
        val one = SafePastePrivacyCoachBuilder.build(current, ready, incidentCount = 1)
        val many = SafePastePrivacyCoachBuilder.build(current, ready, incidentCount = Int.MAX_VALUE)

        assertEquals(zero, negative)
        assertEquals(one, many)
        assertNotEquals(zero, one)
        assertFalse(SafePastePrivacyCoachActionId.EVIDENCE_KIT in zero.recommendedActionIds)
        assertFalse(
            SafePastePrivacyCoachActionId.INCIDENT_TIMELINE_BRIEFING in
                zero.recommendedActionIds
        )
        assertTrue(
            one.recommendedActionIds.containsAll(
                listOf(
                    SafePastePrivacyCoachActionId.EVIDENCE_KIT,
                    SafePastePrivacyCoachActionId.INCIDENT_TIMELINE_BRIEFING
                )
            )
        )
        assertTrue(one.recommendationExplanation.contains("저장 사건이 있다는 범주만 반영"))
        assertTrue(one.recommendationExplanation.contains("사건 내용은 읽지 않습니다"))
        assertFalse(one.familyShareSummary.contains(Int.MAX_VALUE.toString()))
    }

    @Test
    fun lowAndInProgressReadinessAddCallbackRoadmapAndPrivacyWhileReadyStaysCautious() {
        val current = analysis(RiskLevel.MEDIUM, ScamSignal.LINK)
        val low = SafePastePrivacyCoachBuilder.build(current, readiness(ReadinessLevel.LOW))
        val inProgress = SafePastePrivacyCoachBuilder.build(
            current,
            readiness(ReadinessLevel.IN_PROGRESS)
        )
        val ready = SafePastePrivacyCoachBuilder.build(current, readiness(ReadinessLevel.READY))

        listOf(low, inProgress).forEach { coach ->
            assertTrue(
                coach.recommendedActionIds.containsAll(
                    listOf(
                        SafePastePrivacyCoachActionId.FAMILY_CALLBACK_CARD,
                        SafePastePrivacyCoachActionId.FAMILY_READINESS_ROADMAP,
                        SafePastePrivacyCoachActionId.PRIVACY_CENTER
                    )
                )
            )
            assertTrue(
                SafePastePrivacyCoachCardId.FAMILY_CALLBACK_DETAILS in
                    coach.recommendedCardIds
            )
        }
        assertFalse(
            SafePastePrivacyCoachActionId.FAMILY_READINESS_ROADMAP in
                ready.recommendedActionIds
        )
        assertTrue(SafePastePrivacyCoachActionId.PRIVACY_CENTER in ready.recommendedActionIds)
        assertTrue(low.recommendationExplanation.contains("가족 준비가 부족"))
        assertTrue(inProgress.recommendationExplanation.contains("가족 준비가 진행 중"))
        assertTrue(ready.recommendationExplanation.contains("문제없거나 본인확인이 끝났다는 뜻은 아닙니다"))
    }

    @Test
    fun everyCardHasCompleteFieldsStableOrderAndUniqueRoutes() {
        val states = listOf(
            SafePastePrivacyCoachBuilder.build(null, readiness(ReadinessLevel.READY)),
            SafePastePrivacyCoachBuilder.build(
                analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
                readiness(ReadinessLevel.LOW),
                incidentCount = 2
            )
        )
        val expectedTitles = listOf(
            "원문 전체를 그대로 붙이지 않기",
            "전화·계좌·신분정보 가리기",
            "OTP/PIN/비밀번호/안전질문 답 가리기",
            "링크·도메인은 [링크]로 바꾸기",
            "앱·원격제어 세부정보 가리기",
            "가족 정보·콜백 답 가리기",
            "행동 종류는 남기기",
            "붙여넣은 뒤 기존 검사로 이동하기"
        )

        states.forEach { coach ->
            assertEquals(SafePastePrivacyCoachCardId.entries, coach.cards.map { it.id })
            assertEquals((1..8).toList(), coach.cards.map { it.displayOrder })
            assertEquals(expectedTitles, coach.cards.map { it.title })
            assertEquals(coach.recommendedCardIds.distinct(), coach.recommendedCardIds)
            assertEquals(
                coach.recommendedCardIds.sortedBy { it.displayOrder },
                coach.recommendedCardIds
            )

            coach.cards.forEach { card ->
                assertTrue(card.title.isNotBlank())
                assertTrue(card.whyItMatters.isNotBlank())
                assertTrue(card.howToMaskManually.isNotBlank())
                assertTrue(card.whatNotToPasteOrShare.isNotEmpty())
                assertTrue(card.whatNotToPasteOrShare.all { it.isNotBlank() })
                assertTrue(card.primaryAction.label.isNotBlank())
                assertTrue(card.secondaryActions.all { it.label.isNotBlank() })
                assertEquals(
                    card.routeActions.map { it.id }.distinct(),
                    card.routeActions.map { it.id }
                )
            }
        }
    }

    @Test
    fun actionIdsAreCompleteUniqueAndAlwaysMapToKnownExistingFlows() {
        val coach = SafePastePrivacyCoachBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 3
        )
        val allUsedActions = coach.cards.flatMap { it.routeActions } + coach.recommendedActions
        val expectedActionIds = listOf(
            SafePastePrivacyCoachActionId.CHECKER,
            SafePastePrivacyCoachActionId.ANALYSIS_RESULT,
            SafePastePrivacyCoachActionId.SAMPLE_LIBRARY,
            SafePastePrivacyCoachActionId.PRIVACY_CENTER,
            SafePastePrivacyCoachActionId.EXPOSURE_IMPACT_TRIAGE,
            SafePastePrivacyCoachActionId.QUICK_STOP,
            SafePastePrivacyCoachActionId.FAMILY_CALLBACK_CARD,
            SafePastePrivacyCoachActionId.SAFE_CALL_SCRIPTS,
            SafePastePrivacyCoachActionId.FAMILY_ALERT_MESSAGES,
            SafePastePrivacyCoachActionId.TRUSTED_HELPER_REVIEW,
            SafePastePrivacyCoachActionId.FAMILY_READINESS_ROADMAP,
            SafePastePrivacyCoachActionId.ACCOUNT_LOCKDOWN,
            SafePastePrivacyCoachActionId.EVIDENCE_KIT,
            SafePastePrivacyCoachActionId.OFFICIAL_HELP_DIRECTORY,
            SafePastePrivacyCoachActionId.INCIDENT_TIMELINE_BRIEFING,
            SafePastePrivacyCoachActionId.SAFETY_FLOW_ROUTER
        )

        assertEquals(expectedActionIds, SafePastePrivacyCoachActionId.entries)
        assertEquals(expectedActionIds.toSet(), allUsedActions.map { it.id }.toSet())
        assertTrue(allUsedActions.all { it.label.isNotBlank() })
        assertEquals(coach.recommendedActionIds.distinct(), coach.recommendedActionIds)
        assertEquals(
            expectedActionIds.filter { it in coach.recommendedActionIds },
            coach.recommendedActionIds
        )
    }

    @Test
    fun shareSummaryUsesOnlyGenericCardAndRouteLabelsAndExcludesAdversarialSecrets() {
        val rawInput =
            "RAW_SCAM_TEXT 박민수 010-9876-5432 1234567890123456 " +
                "900101-1234567 OTP 739201 PIN 8844 https://evil.example/path"
        val marker =
            "PLAN_SECRET callback-secret contact-secret 010-1111-2222 " +
                "account-987654321012 https://private.example"
        val coach = SafePastePrivacyCoachBuilder.build(
            analysis = analysis(
                RiskLevel.HIGH,
                *ScamSignal.entries.toTypedArray(),
                input = rawInput
            ),
            readiness = readiness(ReadinessLevel.IN_PROGRESS, marker),
            incidentCount = Int.MAX_VALUE
        )
        val summary = coach.familyShareSummary

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
            "4567",
            "[전화번호 숨김]",
            "[긴 숫자 숨김]",
            "[신분정보 숨김]",
            "[인증번호 숨김]"
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

        val allowedBulletLabels = (
            coach.cards.map { it.title } + coach.recommendedActions.map { it.label }
            ).toSet()
        summary.lineSequence()
            .filter { it.startsWith("- ") }
            .map { it.removePrefix("- ") }
            .forEach { label ->
                assertTrue("share summary used non-generic bullet: $label", label in allowedBulletLabels)
            }
        assertFalse(summary.contains(coach.cards.first().whyItMatters))
        assertFalse(summary.contains(coach.cards.first().howToMaskManually))
        assertFalse(summary.contains(coach.cards.first().whatNotToPasteOrShare.first()))
    }

    @Test
    fun builderIsDeterministicAndIgnoresRawInputAndReadinessLiteralFields() {
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

        val first = SafePastePrivacyCoachBuilder.build(firstAnalysis, firstReadiness, 1)
        val repeated = SafePastePrivacyCoachBuilder.build(firstAnalysis, firstReadiness, 1)
        val differentForbiddenValues = SafePastePrivacyCoachBuilder.build(
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
    fun privacyCopyExplicitlyStatesManualLocalStatelessAndNoAutomationOrPersistence() {
        val coach = SafePastePrivacyCoachBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 1
        )
        val notes = coach.privacySafetyNotes.joinToString(" ")

        listOf(
            "로컬 전용·무상태 수동 안내",
            "직접 읽고 직접 가리는",
            "파싱·스캔하지 않",
            "저장하지 않습니다",
            "문자·메시지·통화·연락처·클립보드",
            "자동으로 읽거나 감시하지 않습니다",
            "네트워크를 사용하거나 새 Android 권한을 요청하지 않습니다",
            "알림·알람·캘린더 일정·리마인더·작업·서비스·백그라운드 워커",
            "자동 가림·교정·검사·수정·삭제하지 않고",
            "자동 전송하거나 받는 사람을 고르지 않습니다",
            "사건 기록을 추가·수정·삭제하지 않습니다"
        ).forEach { requiredBoundary ->
            assertTrue("privacy copy omitted: $requiredBoundary", notes.contains(requiredBoundary))
        }
        assertTrue(coach.intro.contains("원문을 받거나 자동으로 고치지 않"))
        assertTrue(coach.modeLabel.contains("직접 읽고 직접 가리기"))
    }

    @Test
    fun copyAvoidsUnsafeGuaranteesAndPositiveAutomaticClaims() {
        val coach = SafePastePrivacyCoachBuilder.build(
            analysis = analysis(RiskLevel.HIGH, *ScamSignal.entries.toTypedArray()),
            readiness = readiness(ReadinessLevel.LOW),
            incidentCount = 1
        )
        val copy = allCopy(coach)
        val forbiddenClaims = listOf(
            "100%",
            " safe ",
            "blocked",
            "안전합니다",
            "문제없습니다",
            "사기임을 확정합니다",
            "피해를 차단했습니다",
            "악성 프로그램을 제거합니다",
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
            "자동으로 삭제합니다",
            "자동으로 가립니다",
            "자동으로 교정합니다",
            "메시지를 자동 전송합니다"
        )

        forbiddenClaims.forEach { claim ->
            assertFalse("coach made unsafe guarantee: $claim", copy.contains(claim, true))
        }
        listOf(
            Regex("자동(?:으로)?\\s*(?:차단|동결|정지|삭제|가림|교정|감시|스캔|검사|전송)(?:합니다|됩니다|완료)"),
            Regex("(?:차단|동결|정지|삭제|제거|복구|검사)\\s*(?:완료|됐습니다|되었습니다)"),
            Regex("(?:계정|카드|피해금).{0,12}(?:자동 조치|자동 복구|회수 보장)")
        ).forEach { positiveAutomationPattern ->
            assertFalse(
                "coach matched positive automation claim: ${positiveAutomationPattern.pattern}",
                positiveAutomationPattern.containsMatchIn(copy)
            )
        }
        assertTrue(copy.contains("증명하지 않고"))
        assertTrue(copy.contains("수행하거나 보장하지 않습니다"))
    }

    @Test
    fun genericManualCategoriesPreserveUsefulLocalRuleSignalsWithoutLiteralValues() {
        val coach = SafePastePrivacyCoachBuilder.build(
            analysis = null,
            readiness = readiness(ReadinessLevel.READY)
        )
        fun card(id: SafePastePrivacyCoachCardId) = coach.cards.single { it.id == id }

        val accountSignals = ScamAnalyzer.analyze(
            card(SafePastePrivacyCoachCardId.PHONE_ACCOUNT_IDENTITY).howToMaskManually
        ).matchedSignals
        val credentialSignals = ScamAnalyzer.analyze(
            card(SafePastePrivacyCoachCardId.CREDENTIALS_AND_SAFETY_ANSWERS)
                .howToMaskManually
        ).matchedSignals
        val linkSignals = ScamAnalyzer.analyze(
            card(SafePastePrivacyCoachCardId.LINK_AND_DOMAIN).howToMaskManually
        ).matchedSignals
        val remoteSignals = ScamAnalyzer.analyze(
            card(SafePastePrivacyCoachCardId.APP_AND_REMOTE_CONTROL).howToMaskManually
        ).matchedSignals
        val familySignals = ScamAnalyzer.analyze(
            card(SafePastePrivacyCoachCardId.FAMILY_CALLBACK_DETAILS).howToMaskManually
        ).matchedSignals
        val actionSignals = ScamAnalyzer.analyze(
            card(SafePastePrivacyCoachCardId.ACTION_CATEGORY_CONTEXT).howToMaskManually
        ).matchedSignals

        assertTrue(ScamSignal.CREDENTIALS in accountSignals)
        assertTrue(ScamSignal.CREDENTIALS in credentialSignals)
        assertTrue(ScamSignal.LINK in linkSignals)
        assertTrue(ScamSignal.PARCEL_INVITATION_SMISHING in linkSignals)
        assertTrue(ScamSignal.APP_INSTALL in remoteSignals)
        assertTrue(ScamSignal.LIVE_CALL_CONTROL in remoteSignals)
        assertTrue(ScamSignal.FAMILY_EMERGENCY in familySignals)
        assertTrue(ScamSignal.PHONE_NUMBER_CHANGE in familySignals)
        assertTrue(ScamSignal.DEEPFAKE_VOICE in familySignals)
        assertTrue(ScamSignal.URGENT_TRANSFER in actionSignals)
        assertTrue(ScamSignal.IMPERSONATION in actionSignals)
        assertTrue(ScamSignal.SECRECY in actionSignals)

        listOf(
            card(SafePastePrivacyCoachCardId.PHONE_ACCOUNT_IDENTITY).howToMaskManually,
            card(SafePastePrivacyCoachCardId.CREDENTIALS_AND_SAFETY_ANSWERS).howToMaskManually,
            card(SafePastePrivacyCoachCardId.LINK_AND_DOMAIN).howToMaskManually,
            card(SafePastePrivacyCoachCardId.APP_AND_REMOTE_CONTROL).howToMaskManually,
            card(SafePastePrivacyCoachCardId.FAMILY_CALLBACK_DETAILS).howToMaskManually,
            card(SafePastePrivacyCoachCardId.ACTION_CATEGORY_CONTEXT).howToMaskManually
        ).forEach { genericInstruction ->
            assertFalse(Regex("\\d{6,}").containsMatchIn(genericInstruction))
            assertFalse(Regex("(?i)https?://|www\\.").containsMatchIn(genericInstruction))
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

    private fun allCopy(coach: SafePastePrivacyCoach): String = buildString {
        appendLine(coach.title)
        appendLine(coach.intro)
        appendLine(coach.modeLabel)
        appendLine(coach.recommendationTitle)
        appendLine(coach.recommendationExplanation)
        coach.cards.forEach { card ->
            appendLine(card.title)
            appendLine(card.whyItMatters)
            appendLine(card.howToMaskManually)
            card.whatNotToPasteOrShare.forEach(::appendLine)
            card.routeActions.forEach { action -> appendLine(action.label) }
        }
        coach.recommendedActions.forEach { action -> appendLine(action.label) }
        coach.privacySafetyNotes.forEach(::appendLine)
        append(coach.familyShareSummary)
    }
}
