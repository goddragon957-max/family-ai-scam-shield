package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FollowUpCheckInPlannerBuilderTest {
    @Test
    fun noAnalysisAndNoSignalsRecommendStabilizationFamilyReadinessAndUncertaintyWithoutSafeVerdict() {
        val noAnalysis = FollowUpCheckInPlannerBuilder.build(
            analysis = null,
            readiness = lowReadiness()
        )
        val noSignals = FollowUpCheckInPlannerBuilder.build(
            analysis = ScamAnalysis(
                input = "일반 대화",
                score = 0,
                riskLevel = RiskLevel.SAFE,
                matchedSignals = emptyList()
            ),
            readiness = readyReadiness()
        )
        val expectedIds = listOf(
            FollowUpCheckpointId.TONIGHT_STABILIZE,
            FollowUpCheckpointId.THIS_WEEK_FAMILY_CALLBACK,
            FollowUpCheckpointId.THIS_MONTH_FAMILY_READINESS,
            FollowUpCheckpointId.WHEN_ANXIETY_RETURNS_STOP
        )

        listOf(noAnalysis, noSignals).forEach { plan ->
            assertEquals(expectedIds, plan.recommendedCheckpointIds)
            assertEquals(expectedIds, plan.recommendedCheckpoints.map { checkpoint -> checkpoint.id })
            listOf(
                FollowUpCheckInActionId.QUICK_STOP,
                FollowUpCheckInActionId.EXPOSURE_IMPACT_TRIAGE,
                FollowUpCheckInActionId.TRUSTED_HELPER_REVIEW,
                FollowUpCheckInActionId.FAMILY_CALLBACK_CARD,
                FollowUpCheckInActionId.SAFE_CALL_SCRIPTS,
                FollowUpCheckInActionId.FAMILY_READINESS_ROADMAP
            ).forEach { actionId ->
                assertTrue("default plan omitted $actionId", actionId in plan.recommendedActionIds)
            }
            assertTrue(plan.recommendationExplanation.contains("단정하지 않"))
            listOf(
                "안전합니다",
                "위험 없음",
                "사기 아님",
                "문제없습니다",
                "검증 완료"
            ).forEach { verdict ->
                assertFalse("default plan asserted $verdict", allCopy(plan).contains(verdict))
            }
        }
        assertTrue(FollowUpCheckInActionId.CHECKER in noAnalysis.recommendedActionIds)
        assertFalse(FollowUpCheckInActionId.ANALYSIS_RESULT in noAnalysis.recommendedActionIds)
        assertTrue(FollowUpCheckInActionId.ANALYSIS_RESULT in noSignals.recommendedActionIds)
        assertFalse(FollowUpCheckInActionId.CHECKER in noSignals.recommendedActionIds)
    }

    @Test
    fun highRiskFinancialCredentialSafeAccountGiftCryptoAndPaymentRecommendAllLaterProtectionChecks() {
        val plans = listOf(
            FollowUpCheckInPlannerBuilder.build(
                analysis = ScamAnalysis("테스트", 90, RiskLevel.HIGH, emptyList()),
                readiness = readyReadiness()
            )
        ) + listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.BAIT,
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).map { signal ->
            FollowUpCheckInPlannerBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )
        }

        plans.forEach { plan ->
            assertTrue(
                plan.recommendedCheckpointIds.containsAll(
                    listOf(
                        FollowUpCheckpointId.NEXT_MORNING_FINANCE_ACCOUNT,
                        FollowUpCheckpointId.THIS_WEEK_OFFICIAL_EVIDENCE,
                        FollowUpCheckpointId.ONE_TO_TWO_WEEKS_STATEMENTS_ALERTS
                    )
                )
            )
            listOf(
                FollowUpCheckInActionId.RECOVERY_PLAN,
                FollowUpCheckInActionId.ACCOUNT_LOCKDOWN,
                FollowUpCheckInActionId.EVIDENCE_KIT,
                FollowUpCheckInActionId.OFFICIAL_HELP_DIRECTORY,
                FollowUpCheckInActionId.REPORT_PREP,
                FollowUpCheckInActionId.PRIVACY_CENTER
            ).forEach { actionId ->
                assertTrue("financial plan omitted $actionId", actionId in plan.recommendedActionIds)
            }
            assertTrue(plan.recommendationExplanation.contains("내일 오전"))
            assertTrue(plan.recommendationExplanation.contains("명세"))
            assertTrue(plan.recommendationExplanation.contains("직접 확인"))
        }
    }

    @Test
    fun appRemoteControlAndScreenShareRecommendDeviceSessionAccountEvidenceAndHelperWithoutRemovalClaim() {
        listOf(ScamSignal.APP_INSTALL, ScamSignal.LIVE_CALL_CONTROL).forEach { signal ->
            val plan = FollowUpCheckInPlannerBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertTrue(
                FollowUpCheckpointId.TWO_TO_THREE_DAYS_DEVICE_MESSENGER in
                    plan.recommendedCheckpointIds
            )
            listOf(
                FollowUpCheckInActionId.ACCOUNT_LOCKDOWN,
                FollowUpCheckInActionId.EVIDENCE_KIT,
                FollowUpCheckInActionId.TRUSTED_HELPER_REVIEW,
                FollowUpCheckInActionId.RECOVERY_PLAN
            ).forEach { actionId ->
                assertTrue("$signal plan omitted $actionId", actionId in plan.recommendedActionIds)
            }
            assertTrue(plan.recommendationExplanation.contains("기기 설정"))
            assertTrue(plan.recommendationExplanation.contains("계정 세션"))
            val copy = allCopy(plan)
            assertFalse(copy.contains("악성 앱을 자동으로 제거합니다"))
            assertFalse(copy.contains("악성코드 제거를 보장합니다"))
            assertFalse(copy.contains("기기를 안전하게 만들었습니다"))
        }
    }

    @Test
    fun linkAndSmishingRecommendDeviceAccountFamilyAlertAndPrivacyWithoutAutoScanClaim() {
        listOf(ScamSignal.LINK, ScamSignal.PARCEL_INVITATION_SMISHING).forEach { signal ->
            val plan = FollowUpCheckInPlannerBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertTrue(
                plan.recommendedCheckpointIds.containsAll(
                    listOf(
                        FollowUpCheckpointId.TWO_TO_THREE_DAYS_DEVICE_MESSENGER,
                        FollowUpCheckpointId.THIS_WEEK_FAMILY_CALLBACK,
                        FollowUpCheckpointId.ONE_TO_TWO_WEEKS_STATEMENTS_ALERTS
                    )
                )
            )
            listOf(
                FollowUpCheckInActionId.ACCOUNT_LOCKDOWN,
                FollowUpCheckInActionId.FAMILY_ALERT_MESSAGES,
                FollowUpCheckInActionId.PRIVACY_CENTER
            ).forEach { actionId ->
                assertTrue("$signal plan omitted $actionId", actionId in plan.recommendedActionIds)
            }
            assertTrue(plan.recommendationExplanation.contains("자동 검사에 기대지 말고"))
            assertFalse(allCopy(plan).contains("자동으로 검사합니다"))
            assertFalse(allCopy(plan).contains("자동 스캔합니다"))
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendFamilyCallbackWeekAlertAndSafeCalls() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val plan = FollowUpCheckInPlannerBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertTrue(
                FollowUpCheckpointId.THIS_WEEK_FAMILY_CALLBACK in
                    plan.recommendedCheckpointIds
            )
            listOf(
                FollowUpCheckInActionId.FAMILY_CALLBACK_CARD,
                FollowUpCheckInActionId.SAFE_CALL_SCRIPTS,
                FollowUpCheckInActionId.FAMILY_ALERT_MESSAGES
            ).forEach { actionId ->
                assertTrue("$signal plan omitted $actionId", actionId in plan.recommendedActionIds)
            }
            assertTrue(plan.recommendationExplanation.contains("평소 알던 별도 경로"))
        }
    }

    @Test
    fun secrecyAndImpersonationRecommendTonightStopHelperAndOfficialHelp() {
        listOf(ScamSignal.SECRECY, ScamSignal.IMPERSONATION).forEach { signal ->
            val plan = FollowUpCheckInPlannerBuilder.build(
                analysis = analysisWith(signal),
                readiness = readyReadiness()
            )

            assertTrue(FollowUpCheckpointId.TONIGHT_STABILIZE in plan.recommendedCheckpointIds)
            assertTrue(
                FollowUpCheckpointId.THIS_WEEK_OFFICIAL_EVIDENCE in
                    plan.recommendedCheckpointIds
            )
            listOf(
                FollowUpCheckInActionId.QUICK_STOP,
                FollowUpCheckInActionId.TRUSTED_HELPER_REVIEW,
                FollowUpCheckInActionId.OFFICIAL_HELP_DIRECTORY
            ).forEach { actionId ->
                assertTrue("$signal plan omitted $actionId", actionId in plan.recommendedActionIds)
            }
            assertTrue(plan.recommendationExplanation.contains("비밀 유지·기관 사칭"))
        }
    }

    @Test
    fun savedIncidentPresenceAddsTimelineEvidenceAndReportPrepWithoutIncidentDataLeak() {
        val incidentMarker = 987654321
        val raw = "INCIDENT-DETAIL-COMET 010-7654-3210 https://incident-leak.example"
        val analysis = ScamAnalysis(raw, 0, RiskLevel.SAFE, emptyList())
        val withIncident = FollowUpCheckInPlannerBuilder.build(
            analysis = analysis,
            readiness = readyReadiness(),
            incidentCount = incidentMarker
        )
        val withoutIncident = FollowUpCheckInPlannerBuilder.build(
            analysis = analysis,
            readiness = readyReadiness(),
            incidentCount = 0
        )

        assertTrue(
            FollowUpCheckpointId.THIS_WEEK_OFFICIAL_EVIDENCE in
                withIncident.recommendedCheckpointIds
        )
        listOf(
            FollowUpCheckInActionId.INCIDENT_TIMELINE_BRIEFING,
            FollowUpCheckInActionId.EVIDENCE_KIT,
            FollowUpCheckInActionId.REPORT_PREP
        ).forEach { actionId ->
            assertTrue("incident follow-up omitted $actionId", actionId in withIncident.recommendedActionIds)
            assertFalse("zero incident unexpectedly added $actionId", actionId in withoutIncident.recommendedActionIds)
        }
        val copy = allCopy(withIncident)
        assertFalse(copy.contains(raw))
        assertFalse(copy.contains("INCIDENT-DETAIL-COMET"))
        assertFalse(copy.contains("010-7654-3210"))
        assertFalse(copy.contains("incident-leak.example"))
        assertFalse(copy.contains(incidentMarker.toString()))
    }

    @Test
    fun lowReadinessAddsRoadmapDrillAndSafetyPlanWhileReadyCopyDiffersCalmly() {
        val analysis = analysisWith(ScamSignal.LINK)
        val low = FollowUpCheckInPlannerBuilder.build(analysis, lowReadiness())
        val inProgress = FollowUpCheckInPlannerBuilder.build(analysis, inProgressReadiness())
        val ready = FollowUpCheckInPlannerBuilder.build(analysis, readyReadiness())

        assertTrue(
            FollowUpCheckpointId.THIS_MONTH_FAMILY_READINESS in
                low.recommendedCheckpointIds
        )
        listOf(
            FollowUpCheckInActionId.FAMILY_READINESS_ROADMAP,
            FollowUpCheckInActionId.FAMILY_SAFETY_DRILL_COACH,
            FollowUpCheckInActionId.SAFETY_PLAN
        ).forEach { actionId ->
            assertTrue("low readiness omitted $actionId", actionId in low.recommendedActionIds)
        }
        assertTrue(low.recommendationExplanation.contains("부족해도 괜찮"))
        assertTrue(low.recommendationExplanation.contains("차분히 시작"))
        assertTrue(inProgress.recommendationExplanation.contains("진행 중"))
        assertTrue(ready.recommendationExplanation.contains("되어 있어도"))
        assertTrue(ready.recommendationExplanation.contains("차분히"))
        assertNotEquals(low.recommendationExplanation, ready.recommendationExplanation)
    }

    @Test
    fun everyCheckpointHasCompleteManualFieldsPrimaryRoutePriorityAndStableOrder() {
        val plan = FollowUpCheckInPlannerBuilder.build(null, lowReadiness())
        val expectedOrder = listOf(
            FollowUpCheckpointId.TONIGHT_STABILIZE,
            FollowUpCheckpointId.NEXT_MORNING_FINANCE_ACCOUNT,
            FollowUpCheckpointId.TWO_TO_THREE_DAYS_DEVICE_MESSENGER,
            FollowUpCheckpointId.THIS_WEEK_FAMILY_CALLBACK,
            FollowUpCheckpointId.THIS_WEEK_OFFICIAL_EVIDENCE,
            FollowUpCheckpointId.ONE_TO_TWO_WEEKS_STATEMENTS_ALERTS,
            FollowUpCheckpointId.THIS_MONTH_FAMILY_READINESS,
            FollowUpCheckpointId.WHEN_ANXIETY_RETURNS_STOP
        )

        assertEquals(expectedOrder, FollowUpCheckpointId.entries)
        assertEquals(expectedOrder, plan.checkpoints.map { checkpoint -> checkpoint.id })
        assertEquals((1..expectedOrder.size).toList(), plan.checkpoints.map { it.displayOrder })
        plan.checkpoints.forEach { checkpoint ->
            assertEquals(checkpoint.id.displayOrder, checkpoint.displayOrder)
            assertTrue(checkpoint.timeLabel.isNotBlank())
            assertTrue(checkpoint.category.label.isNotBlank())
            assertTrue(checkpoint.title.isNotBlank())
            assertTrue(checkpoint.purpose.isNotBlank())
            assertTrue(checkpoint.manualCheckSteps.isNotEmpty())
            assertTrue(checkpoint.manualCheckSteps.all { step -> step.isNotBlank() })
            assertTrue(checkpoint.whatNotToDo.isNotEmpty())
            assertTrue(checkpoint.whatNotToDo.all { note -> note.isNotBlank() })
            assertTrue(checkpoint.priority.label.isNotBlank())
            assertTrue(checkpoint.primaryAction.label.isNotBlank())
            assertTrue(checkpoint.secondaryActions.isNotEmpty())
            assertEquals(checkpoint.primaryAction, checkpoint.routeActions.first())
            assertEquals(
                checkpoint.routeActions.map { action -> action.id }.distinct().size,
                checkpoint.routeActions.size
            )
        }
        assertEquals(
            listOf("오늘 밤", "내일 오전", "2~3일 뒤", "이번 주", "이번 주", "1~2주 뒤", "이번 달", "불안이 다시 들 때"),
            plan.checkpoints.map { checkpoint -> checkpoint.timeLabel }
        )
    }

    @Test
    fun copyMakesManualOnlyNoSchedulerPromiseAndCoversEveryRequestedCheckpointTheme() {
        val plan = FollowUpCheckInPlannerBuilder.build(
            analysis = analysisWith(*ScamSignal.entries.toTypedArray(), riskLevel = RiskLevel.HIGH),
            readiness = lowReadiness(),
            incidentCount = 2
        )
        val copy = allCopy(plan)
        val privacyCopy = plan.privacySafetyNotes.joinToString(" ")

        listOf(
            "오늘 밤 다시 안정시키기",
            "내일 오전 금융·계정 확인",
            "2~3일 기기·메신저 관찰",
            "이번 주 가족 경고·콜백 점검",
            "이번 주 공식 접수·증거 정리",
            "1~2주 명세·알림 재확인",
            "이번 달 가족 준비 보강",
            "불안 재발 시 다시 멈춤"
        ).forEach { title -> assertTrue("missing checkpoint: $title", copy.contains(title)) }
        assertTrue(plan.intro.contains("직접"))
        assertTrue(plan.intro.contains("로컬·무상태"))
        assertTrue(privacyCopy.contains("수동 후속 안내"))
        assertTrue(privacyCopy.contains("알림·알람·캘린더 일정·리마인더"))
        assertTrue(privacyCopy.contains("만들거나 예약하지 않"))
        assertTrue(privacyCopy.contains("백그라운드 작업을 실행하지 않습니다"))

        listOf(
            "알림을 예약합니다",
            "알람을 설정합니다",
            "캘린더 일정을 만듭니다",
            "리마인더를 예약합니다",
            "백그라운드에서 점검합니다",
            "나중에 자동으로 알려드립니다",
            "후속 점검을 자동 실행합니다"
        ).forEach { promise ->
            assertFalse("copy promised scheduler behavior: $promise", copy.contains(promise))
        }
    }

    @Test
    fun shareUsesOnlyCheckpointActionLabelsAndGenericStepsWithoutInputOrSecretValues() {
        val safeWord = "해질녘 파란 우산은 어디에?"
        val callbackScript = "푸른 메모장을 열고 첫 번째 저장 번호로 전화한다."
        val contactLabel = "보호자-은하수"
        val contactNumber = "010-2468-1357"
        val raw =
            "RAW-FOLLOW-UP-COMET 전화 010-9876-5432 계좌 123456789012 " +
                "비밀번호 BluePassword77 OTP A1B2C3 인증번호 654321 PIN 7788 " +
                "https://evil.example/path 주민번호 900101-1234567 안전 질문 $safeWord " +
                "콜백 $callbackScript 연락처 $contactLabel $contactNumber"
        val analysis = ScamAnalysis(
            input = raw,
            score = 100,
            riskLevel = RiskLevel.HIGH,
            matchedSignals = listOf(
                ScamSignal.URGENT_TRANSFER,
                ScamSignal.APP_INSTALL,
                ScamSignal.LINK,
                ScamSignal.PHONE_NUMBER_CHANGE,
                ScamSignal.SECRECY
            )
        )
        val plan = FollowUpCheckInPlannerBuilder.build(
            analysis = analysis,
            readiness = FamilyReadinessBuilder.build(
                SafetyPlan(
                    safeWord = safeWord,
                    callbackScript = callbackScript,
                    contacts = listOf(TrustedContact(contactLabel, contactNumber))
                )
            ),
            incidentCount = 654321
        )
        val summary = plan.familyShareSummary
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)

        plan.checkpoints.forEach { checkpoint ->
            assertTrue("share omitted time: ${checkpoint.id}", summary.contains(checkpoint.timeLabel))
            assertTrue("share omitted category: ${checkpoint.id}", summary.contains(checkpoint.category.label))
            assertTrue("share omitted title: ${checkpoint.id}", summary.contains(checkpoint.title))
            checkpoint.manualCheckSteps.forEach { step ->
                assertTrue("share omitted manual step: $step", summary.contains(step))
            }
            checkpoint.routeActions.forEach { action ->
                assertTrue("share omitted route: ${action.id}", summary.contains(action.label))
            }
            assertFalse("share included purpose: ${checkpoint.id}", summary.contains(checkpoint.purpose))
            checkpoint.whatNotToDo.forEach { note ->
                assertFalse("share included detailed do-not-do note: $note", summary.contains(note))
            }
        }
        plan.recommendedActions.forEach { action ->
            assertTrue("share omitted recommendation: ${action.id}", summary.contains(action.label))
        }
        assertTrue(summary.contains("직접 확인"))
        assertTrue(summary.contains("원문") && summary.contains("포함하지 않았습니다"))
        assertFalse(summary.contains(plan.recommendationTitle))
        assertFalse(summary.contains(raw))
        assertFalse(summary.contains(redactedClue))

        val secrets = listOf(
            "RAW-FOLLOW-UP-COMET",
            safeWord,
            callbackScript,
            contactLabel,
            contactNumber,
            "010-9876-5432",
            "123456789012",
            "BluePassword77",
            "A1B2C3",
            "654321",
            "7788",
            "evil.example",
            "900101-1234567"
        )
        secrets.forEach { secret ->
            assertFalse("follow-up share leaked: $secret", summary.contains(secret))
            assertFalse("follow-up output leaked: $secret", allCopy(plan).contains(secret))
        }
        assertFalse(phonePattern.containsMatchIn(summary))
        assertFalse(longNumberPattern.containsMatchIn(summary))
        assertFalse(groupedNumberPattern.containsMatchIn(summary))
        assertFalse(urlDomainPattern.containsMatchIn(summary))
        assertFalse(residentPattern.containsMatchIn(summary))

        val adversarialReadiness = FamilyReadiness(
            level = ReadinessLevel.READY,
            title = raw,
            completedCount = 654321,
            totalCount = 987654,
            checklist = listOf(
                ReadinessItem(
                    label = "$safeWord $contactNumber",
                    done = false,
                    action = "$callbackScript https://evil.example"
                )
            ),
            nextAction = "계좌 123456789012 OTP 654321 PIN 7788"
        )
        val adversarialPlan = FollowUpCheckInPlannerBuilder.build(
            analysis,
            adversarialReadiness,
            incidentCount = 654321
        )
        secrets.forEach { secret ->
            assertFalse(
                "planner used caller-supplied readiness value: $secret",
                allCopy(adversarialPlan).contains(secret)
            )
        }
        assertFalse(phonePattern.containsMatchIn(adversarialPlan.familyShareSummary))
        assertFalse(longNumberPattern.containsMatchIn(adversarialPlan.familyShareSummary))
        assertFalse(groupedNumberPattern.containsMatchIn(adversarialPlan.familyShareSummary))
        assertFalse(urlDomainPattern.containsMatchIn(adversarialPlan.familyShareSummary))
        assertFalse(residentPattern.containsMatchIn(adversarialPlan.familyShareSummary))
    }

    @Test
    fun outputIsDeterministicForSameInputs() {
        val analysis = analysisWith(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.APP_INSTALL,
            ScamSignal.LINK,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.SECRECY,
            riskLevel = RiskLevel.HIGH
        )
        val readiness = readyReadiness()

        val first = FollowUpCheckInPlannerBuilder.build(analysis, readiness, incidentCount = 3)
        val second = FollowUpCheckInPlannerBuilder.build(analysis, readiness, incidentCount = 3)

        assertEquals(first, second)
        assertEquals(first.checkpoints, second.checkpoints)
        assertEquals(first.recommendedCheckpointIds, second.recommendedCheckpointIds)
        assertEquals(first.recommendedCheckpoints, second.recommendedCheckpoints)
        assertEquals(first.recommendedActions, second.recommendedActions)
        assertEquals(first.recommendedActionIds, second.recommendedActionIds)
        assertEquals(first.familyShareSummary, second.familyShareSummary)
    }

    @Test
    fun copyStatesManualLocalNoStorageNoContactsAutoSendMonitoringNetworkMutationOrScheduler() {
        representativePlans().forEach { plan ->
            val privacyCopy = plan.privacySafetyNotes.joinToString(" ")

            assertTrue(privacyCopy.contains("수동 후속 안내"))
            assertTrue(privacyCopy.contains("기기 안"))
            assertTrue(privacyCopy.contains("로컬 규칙"))
            assertTrue(privacyCopy.contains("선택한 점검"))
            assertTrue(privacyCopy.contains("답변·메모"))
            assertTrue(privacyCopy.contains("완료·체크 상태"))
            assertTrue(privacyCopy.contains("점수"))
            assertTrue(privacyCopy.contains("읽음·진행 상태"))
            assertTrue(privacyCopy.contains("공유 이력"))
            assertTrue(privacyCopy.contains("후속 점검 이력"))
            assertTrue(privacyCopy.contains("저장하지 않습니다"))
            assertTrue(privacyCopy.contains("연락처를 읽거나"))
            assertTrue(privacyCopy.contains("대상을 고르지 않"))
            assertTrue(privacyCopy.contains("자동 전송하지 않습니다"))
            assertTrue(privacyCopy.contains("읽거나 감시하지 않"))
            assertTrue(privacyCopy.contains("자동 모니터링하지 않습니다"))
            assertTrue(privacyCopy.contains("네트워크"))
            assertTrue(privacyCopy.contains("사건 기록을 자동으로 추가·수정·삭제하지 않습니다"))
            assertTrue(privacyCopy.contains("새 Android 권한을 요청하지 않"))
            assertTrue(privacyCopy.contains("전화·문자") && privacyCopy.contains("자동 실행"))
            assertTrue(privacyCopy.contains("알림·알람·캘린더 일정·리마인더"))
            assertTrue(privacyCopy.contains("백그라운드 작업"))
        }
    }

    @Test
    fun copyDoesNotImplyProofMonitoringBlockingRemovalFreezeRecoveryLegalGuaranteeDeletionOrSecrets() {
        val forbiddenAffirmativeClaims = listOf(
            "모든 사기를 찾아냅니다",
            "모든 사기를 막습니다",
            "사기를 확정합니다",
            "노출을 확정합니다",
            "노출 여부를 증명합니다",
            "공식 판정입니다",
            "안전합니다",
            "위험 없음",
            "사기 아님",
            "문제없습니다",
            "검증을 완료했습니다",
            "자동으로 감시합니다",
            "자동 모니터링합니다",
            "몰래 확인합니다",
            "자동으로 검사합니다",
            "자동 스캔합니다",
            "연락처를 자동으로 읽습니다",
            "서버로 업로드합니다",
            "네트워크로 업로드합니다",
            "자동으로 전송합니다",
            "전화를 자동으로 겁니다",
            "메시지를 자동으로 보냅니다",
            "문자를 자동으로 차단합니다",
            "전화를 자동으로 차단합니다",
            "악성 앱을 자동으로 제거합니다",
            "악성 앱을 제거합니다",
            "악성코드를 제거합니다",
            "악성코드 제거를 보장합니다",
            "계좌를 자동으로 지급정지합니다",
            "카드를 자동으로 정지합니다",
            "계좌·카드를 정지합니다",
            "계정을 자동으로 잠급니다",
            "피해금 회수를 보장합니다",
            "피해금을 회수합니다",
            "피해 복구를 보장합니다",
            "법률 자문을 제공합니다",
            "법률 조언을 제공합니다",
            "법적 결과를 보장합니다",
            "안전을 보장합니다",
            "자동으로 삭제합니다",
            "자동 삭제합니다",
            "비밀값에 접근합니다",
            "비밀정보에 접근합니다",
            "100%",
            "automatic monitoring",
            "conclusive detection",
            "proof of exposure",
            "blocks calls automatically",
            "blocks messages automatically",
            "malware removal guarantee",
            "account freeze guarantee",
            "guaranteed recovery",
            "legal guarantee"
        )

        representativePlans().forEach { plan ->
            val copy = allCopy(plan)
            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "follow-up planner copy contained unsafe claim: $claim",
                    copy.lowercase().contains(claim.lowercase())
                )
            }
        }
    }

    private fun representativePlans(): List<FollowUpCheckInPlan> = listOf(
        FollowUpCheckInPlannerBuilder.build(null, lowReadiness()),
        FollowUpCheckInPlannerBuilder.build(
            analysisWith(ScamSignal.APP_INSTALL),
            inProgressReadiness()
        ),
        FollowUpCheckInPlannerBuilder.build(
            analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER),
            readyReadiness(),
            incidentCount = 2
        ),
        FollowUpCheckInPlannerBuilder.build(analysisWith(ScamSignal.LINK), readyReadiness()),
        FollowUpCheckInPlannerBuilder.build(
            analysisWith(ScamSignal.DEEPFAKE_VOICE),
            readyReadiness()
        ),
        FollowUpCheckInPlannerBuilder.build(analysisWith(ScamSignal.SECRECY), readyReadiness())
    )

    private fun analysisWith(
        vararg signals: ScamSignal,
        riskLevel: RiskLevel = RiskLevel.MEDIUM
    ): ScamAnalysis = ScamAnalysis(
        input = "테스트 입력 ${signals.joinToString { signal -> signal.name }}",
        score = if (riskLevel == RiskLevel.HIGH) 80 else 50,
        riskLevel = riskLevel,
        matchedSignals = signals.toList()
    )

    private fun lowReadiness(): FamilyReadiness = FamilyReadinessBuilder.build(
        SafetyPlan(safeWord = "", callbackScript = "", contacts = emptyList())
    )

    private fun inProgressReadiness(): FamilyReadiness = FamilyReadinessBuilder.build(
        SafetyPlan(
            safeWord = "우리 가족만 아는 질문",
            callbackScript = "",
            contacts = emptyList()
        )
    )

    private fun readyReadiness(): FamilyReadiness = FamilyReadinessBuilder.build(
        SafetyPlan(
            safeWord = "우리 가족만 아는 질문",
            callbackScript = "통화를 끝내고 저장된 번호로 다시 전화한다.",
            contacts = listOf(
                TrustedContact("가족", "저장된 번호"),
                TrustedContact("예비 가족", "예비 저장 번호")
            )
        )
    )

    private fun checkpointCopy(checkpoint: FollowUpCheckpoint): String = buildString {
        append(checkpoint.timeLabel)
        append(checkpoint.category.label)
        append(checkpoint.title)
        append(checkpoint.purpose)
        checkpoint.manualCheckSteps.forEach(::append)
        checkpoint.whatNotToDo.forEach(::append)
        append(checkpoint.priority.label)
        checkpoint.routeActions.forEach { action -> append(action.label) }
    }

    private fun allCopy(plan: FollowUpCheckInPlan): String = buildString {
        append(plan.title)
        append(plan.intro)
        append(plan.recommendationTitle)
        append(plan.recommendationExplanation)
        plan.recommendedActions.forEach { action -> append(action.label) }
        plan.checkpoints.forEach { checkpoint -> append(checkpointCopy(checkpoint)) }
        plan.privacySafetyNotes.forEach(::append)
        append(plan.familyShareSummary)
    }

    private companion object {
        val phonePattern = Regex("0\\d{1,2}[- .]?\\d{3,4}[- .]?\\d{4}")
        val longNumberPattern = Regex("(?<!\\d)\\d{6,}(?!\\d)")
        val groupedNumberPattern = Regex("(?<!\\d)(?:\\d[- .]?){5,}\\d(?!\\d)")
        val urlDomainPattern = Regex(
            "(?i)(?:[a-z][a-z0-9+.-]*://|www\\.|(?:[a-z0-9-]+\\.)+[a-z]{2,63}\\b|" +
                "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b)"
        )
        val residentPattern = Regex("(?<!\\d)\\d{6}\\s*-?\\s*[1-8]\\d{6}(?!\\d)")
    }
}
