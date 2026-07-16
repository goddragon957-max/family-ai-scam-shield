package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountLockdownChecklistBuilderTest {
    @Test
    fun noAnalysisHasStableOrderAndConservativeDefaultRecommendations() {
        val checklist = AccountLockdownChecklistBuilder.build(null)

        assertEquals("분석 전 기본 점검", checklist.recommendationTitle)
        assertEquals(
            listOf(
                AccountLockdownSectionId.BANK_CARD_PAYMENT,
                AccountLockdownSectionId.MESSAGING_SOCIAL_RECOVERY,
                AccountLockdownSectionId.DEVICE_REMOTE_CLEANUP,
                AccountLockdownSectionId.CREDENTIAL_OTP_RESET,
                AccountLockdownSectionId.LINK_SMISHING_CAUTION,
                AccountLockdownSectionId.SIM_PHONE_NUMBER_PROTECTION,
                AccountLockdownSectionId.EVIDENCE_OFFICIAL_HELP
            ),
            checklist.sections.map { section -> section.id }
        )
        assertEquals((1..7).toList(), checklist.sections.map { section -> section.displayOrder })
        assertEquals(
            listOf(
                AccountLockdownSectionId.CREDENTIAL_OTP_RESET,
                AccountLockdownSectionId.EVIDENCE_OFFICIAL_HELP
            ),
            checklist.recommendedSectionIds
        )
        assertEquals(
            checklist.recommendedSectionIds,
            checklist.recommendedSections.map { section -> section.id }
        )
        listOf(
            AccountLockdownActionId.SAFETY_FLOW_ROUTER,
            AccountLockdownActionId.NEW_ANALYSIS,
            AccountLockdownActionId.EVIDENCE_KIT,
            AccountLockdownActionId.OFFICIAL_HELP_DIRECTORY
        ).forEach { actionId ->
            assertTrue("default guidance omitted $actionId", actionId in checklist.recommendedActionIds)
        }
        assertFalse(AccountLockdownActionId.REPORT_PREP in checklist.recommendedActionIds)
        assertTrue(checklist.recommendationExplanation.contains("새 분석"))
        assertTrue(checklist.recommendationExplanation.contains("일반 계정 보호"))
        assertEquals(AccountLockdownSectionId.entries.size, checklist.sections.size)
        assertEquals(
            checklist.sections.size,
            checklist.sections.map { section -> section.id }.distinct().size
        )
    }

    @Test
    fun remoteControlAppInstallAndScreenSharePrioritizeDeviceThenCredentialReset() {
        listOf(
            ScamSignal.APP_INSTALL,
            ScamSignal.LIVE_CALL_CONTROL
        ).forEach { signal ->
            val checklist = AccountLockdownChecklistBuilder.build(analysisWith(signal))

            assertEquals(
                "$signal recommendation order",
                listOf(
                    AccountLockdownSectionId.DEVICE_REMOTE_CLEANUP,
                    AccountLockdownSectionId.CREDENTIAL_OTP_RESET,
                    AccountLockdownSectionId.EVIDENCE_OFFICIAL_HELP
                ),
                checklist.recommendedSectionIds
            )
            val device = checklist.sections.single {
                section -> section.id == AccountLockdownSectionId.DEVICE_REMOTE_CLEANUP
            }
            val deviceCopy = (device.manualSteps + device.doNotDoNotes).joinToString(" ")
            assertTrue(deviceCopy.contains("연결을 끊"))
            assertTrue(deviceCopy.contains("다른 안전한 기기"))
            assertTrue(deviceCopy.contains("접근성·기기 관리자 권한"))
            assertTrue(deviceCopy.contains("기기 설정"))
            assertTrue(deviceCopy.contains("제거"))
            val preserveIndex = device.manualSteps.indexOfFirst { step ->
                step.contains("설치 시각") && step.contains("먼저 남기세요")
            }
            val revokeAndRemoveIndex = device.manualSteps.indexOfFirst { step ->
                step.contains("해제한 뒤") && step.contains("제거")
            }
            assertTrue(preserveIndex >= 0)
            assertTrue(revokeAndRemoveIndex > preserveIndex)

            val bankCopy = checklist.sections
                .single { section -> section.id == AccountLockdownSectionId.BANK_CARD_PAYMENT }
                .manualSteps
                .joinToString(" ")
            assertTrue(bankCopy.contains("원격 접근이 의심되면 먼저 연결을 끊"))
            assertTrue(bankCopy.contains("다른 기기") || bankCopy.contains("안전한 전화"))
        }
    }

    @Test
    fun moneyCredentialSafeAccountGiftAndCryptoSignalsPrioritizeBankThenCredentialReset() {
        listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.CREDENTIALS,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val checklist = AccountLockdownChecklistBuilder.build(analysisWith(signal))

            assertEquals(
                "$signal recommendation order",
                listOf(
                    AccountLockdownSectionId.BANK_CARD_PAYMENT,
                    AccountLockdownSectionId.CREDENTIAL_OTP_RESET,
                    AccountLockdownSectionId.EVIDENCE_OFFICIAL_HELP
                ),
                checklist.recommendedSectionIds
            )
            listOf(
                AccountLockdownActionId.RESPONSE_CENTER,
                AccountLockdownActionId.OFFICIAL_HELP_DIRECTORY,
                AccountLockdownActionId.REPORT_PREP
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in checklist.recommendedActionIds)
            }
            assertFalse(AccountLockdownActionId.NEW_ANALYSIS in checklist.recommendedActionIds)
        }

        val bankSection = AccountLockdownChecklistBuilder.build(analysisWith(ScamSignal.CREDENTIALS))
            .sections
            .single { section -> section.id == AccountLockdownSectionId.BANK_CARD_PAYMENT }
        val bankCopy = bankSection.manualSteps.joinToString(" ")
        assertTrue(bankCopy.contains("지급정지"))
        assertTrue(bankCopy.contains("카드 이용 정지"))
        assertTrue(bankCopy.contains("결제 취소 가능 여부"))
        assertTrue(bankCopy.contains("최근 거래"))
    }

    @Test
    fun linkAndSmishingPrioritizeLinkCautionEvidenceAndOfficialHelpWithoutAutoScanClaim() {
        listOf(
            ScamSignal.LINK,
            ScamSignal.PARCEL_INVITATION_SMISHING
        ).forEach { signal ->
            val checklist = AccountLockdownChecklistBuilder.build(analysisWith(signal))

            assertEquals(
                "$signal recommendation order",
                listOf(
                    AccountLockdownSectionId.LINK_SMISHING_CAUTION,
                    AccountLockdownSectionId.EVIDENCE_OFFICIAL_HELP
                ),
                checklist.recommendedSectionIds
            )
            listOf(
                AccountLockdownActionId.EVIDENCE_KIT,
                AccountLockdownActionId.OFFICIAL_HELP_DIRECTORY,
                AccountLockdownActionId.REPORT_PREP
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in checklist.recommendedActionIds)
            }
            val linkSection = checklist.sections.single {
                section -> section.id == AccountLockdownSectionId.LINK_SMISHING_CAUTION
            }
            val linkCopy = (linkSection.manualSteps + linkSection.doNotDoNotes).joinToString(" ")
            assertTrue(linkCopy.contains("다시 열지 말"))
            assertTrue(linkCopy.contains("공식 앱"))
            assertTrue(linkCopy.contains("직접 입력"))
            assertFalse(allCopy(checklist).contains("자동으로 검사합니다"))
            assertFalse(allCopy(checklist).contains("자동 스캔합니다"))
        }
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendMessagingWarningAndCallbackRoutes() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val checklist = AccountLockdownChecklistBuilder.build(analysisWith(signal))

            assertEquals(
                "$signal should put messaging recovery first",
                AccountLockdownSectionId.MESSAGING_SOCIAL_RECOVERY,
                checklist.recommendedSectionIds.first()
            )
            listOf(
                AccountLockdownActionId.FAMILY_CALLBACK_CARD,
                AccountLockdownActionId.SAFETY_FLOW_ROUTER
            ).forEach { actionId ->
                assertTrue("$signal omitted $actionId", actionId in checklist.recommendedActionIds)
            }
            val messaging = checklist.recommendedSections.first()
            val messagingCopy = messaging.manualSteps.joinToString(" ")
            assertTrue(messagingCopy.contains("가족"))
            assertTrue(messagingCopy.contains("로그아웃"))
            assertTrue(messagingCopy.contains("2단계 인증"))

            if (signal == ScamSignal.PHONE_NUMBER_CHANGE) {
                assertTrue(
                    AccountLockdownSectionId.SIM_PHONE_NUMBER_PROTECTION in
                        checklist.recommendedSectionIds
                )
            }
        }
    }

    @Test
    fun compositeSignalsKeepUrgentPriorityWhileIncludingEveryRelevantSection() {
        val remoteAndCredential = AccountLockdownChecklistBuilder.build(
            analysisWith(ScamSignal.APP_INSTALL, ScamSignal.CREDENTIALS)
        )
        assertEquals(
            listOf(
                AccountLockdownSectionId.DEVICE_REMOTE_CLEANUP,
                AccountLockdownSectionId.CREDENTIAL_OTP_RESET,
                AccountLockdownSectionId.BANK_CARD_PAYMENT,
                AccountLockdownSectionId.EVIDENCE_OFFICIAL_HELP
            ),
            remoteAndCredential.recommendedSectionIds
        )

        val linkAndCredential = AccountLockdownChecklistBuilder.build(
            analysisWith(ScamSignal.LINK, ScamSignal.CREDENTIALS)
        )
        assertEquals(
            listOf(
                AccountLockdownSectionId.BANK_CARD_PAYMENT,
                AccountLockdownSectionId.CREDENTIAL_OTP_RESET,
                AccountLockdownSectionId.LINK_SMISHING_CAUTION,
                AccountLockdownSectionId.EVIDENCE_OFFICIAL_HELP
            ),
            linkAndCredential.recommendedSectionIds
        )

        val familyAndTransfer = AccountLockdownChecklistBuilder.build(
            analysisWith(ScamSignal.FAMILY_EMERGENCY, ScamSignal.URGENT_TRANSFER)
        )
        assertEquals(
            listOf(
                AccountLockdownSectionId.BANK_CARD_PAYMENT,
                AccountLockdownSectionId.CREDENTIAL_OTP_RESET,
                AccountLockdownSectionId.MESSAGING_SOCIAL_RECOVERY,
                AccountLockdownSectionId.EVIDENCE_OFFICIAL_HELP
            ),
            familyAndTransfer.recommendedSectionIds
        )
        assertTrue(
            AccountLockdownActionId.FAMILY_CALLBACK_CARD in
                familyAndTransfer.recommendedActionIds
        )
        assertTrue(
            AccountLockdownActionId.SAFETY_FLOW_ROUTER in
                familyAndTransfer.recommendedActionIds
        )
    }

    @Test
    fun sectionActionsOpenMatchingExistingResponseAndEvidenceContexts() {
        val checklist = AccountLockdownChecklistBuilder.build(null)

        fun action(
            sectionId: AccountLockdownSectionId,
            actionId: AccountLockdownActionId
        ): AccountLockdownAction = checklist.sections
            .single { section -> section.id == sectionId }
            .actions
            .single { candidate -> candidate.id == actionId }

        assertNull(
            action(
                AccountLockdownSectionId.BANK_CARD_PAYMENT,
                AccountLockdownActionId.RESPONSE_CENTER
            ).responseSituation
        )
        assertEquals(
            IncidentSituation.APP_INSTALLED,
            action(
                AccountLockdownSectionId.DEVICE_REMOTE_CLEANUP,
                AccountLockdownActionId.RESPONSE_CENTER
            ).responseSituation
        )
        assertEquals(
            IncidentSituation.APP_INSTALLED,
            action(
                AccountLockdownSectionId.DEVICE_REMOTE_CLEANUP,
                AccountLockdownActionId.EVIDENCE_KIT
            ).evidenceSituation
        )
        assertEquals(
            IncidentSituation.CREDENTIALS_SHARED,
            action(
                AccountLockdownSectionId.CREDENTIAL_OTP_RESET,
                AccountLockdownActionId.RESPONSE_CENTER
            ).responseSituation
        )
        assertEquals(
            IncidentSituation.LINK_OPENED,
            action(
                AccountLockdownSectionId.LINK_SMISHING_CAUTION,
                AccountLockdownActionId.RESPONSE_CENTER
            ).responseSituation
        )
        assertEquals(
            IncidentSituation.LINK_OPENED,
            action(
                AccountLockdownSectionId.LINK_SMISHING_CAUTION,
                AccountLockdownActionId.EVIDENCE_KIT
            ).evidenceSituation
        )
    }

    @Test
    fun everySectionHasCompleteManualGuidanceAndMappedActions() {
        listOf(
            AccountLockdownChecklistBuilder.build(null),
            AccountLockdownChecklistBuilder.build(
                analysisWith(
                    ScamSignal.APP_INSTALL,
                    ScamSignal.CREDENTIALS,
                    ScamSignal.LINK,
                    ScamSignal.PHONE_NUMBER_CHANGE
                )
            )
        ).forEach { checklist ->
            assertTrue(checklist.title.isNotBlank())
            assertTrue(checklist.intro.isNotBlank())
            assertTrue(checklist.recommendationTitle.isNotBlank())
            assertTrue(checklist.recommendationExplanation.isNotBlank())
            assertTrue(checklist.privacySafetyNotes.isNotEmpty())
            assertTrue(checklist.privacySafetyNotes.all { note -> note.isNotBlank() })
            assertTrue(
                checklist.recommendedSectionIds.all { sectionId ->
                    checklist.sections.any { section -> section.id == sectionId }
                }
            )

            checklist.sections.forEach { section ->
                assertTrue(section.displayOrder > 0)
                assertTrue(section.urgency.rank >= 0)
                assertTrue(section.urgency.label.isNotBlank())
                assertTrue(section.title.isNotBlank())
                assertTrue(section.whyItMatters.isNotBlank())
                assertTrue(section.manualSteps.isNotEmpty())
                assertTrue(section.manualSteps.all { step -> step.isNotBlank() })
                assertTrue(section.doNotDoNotes.isNotEmpty())
                assertTrue(section.doNotDoNotes.all { note -> note.isNotBlank() })
                assertTrue(section.actions.isNotEmpty())
                assertTrue(section.actionLabels.all { label -> label.isNotBlank() })
                assertEquals(section.actions.map { action -> action.label }, section.actionLabels)
            }
        }
    }

    @Test
    fun shareContainsSectionAndActionLabelsButNoRawClueOrSensitiveValues() {
        val safeWord = "해질녘 파란 우산은 어디에?"
        val callbackScript = "푸른 메모장을 열고 첫 번째 저장 번호로 전화한다."
        val contactLabel = "보호자-은하수"
        val contactNumber = "010-2468-1357"
        val raw =
            "원문-파란별 전화 010-9876-5432 계좌 123456789012 비밀번호 BluePassword77 " +
                "OTP A1B2C3 PIN 7788 https://evil.example/path 주민번호 900101-1234567 " +
                "안전 질문 $safeWord 콜백 내용 $callbackScript 연락처 $contactLabel $contactNumber"
        val analysis = ScamAnalysis(
            input = raw,
            score = 100,
            riskLevel = RiskLevel.HIGH,
            matchedSignals = listOf(
                ScamSignal.APP_INSTALL,
                ScamSignal.CREDENTIALS,
                ScamSignal.LINK,
                ScamSignal.PHONE_NUMBER_CHANGE
            )
        )
        val checklist = AccountLockdownChecklistBuilder.build(analysis)
        val summary = checklist.familyShareSummary
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)

        checklist.sections.forEach { section ->
            assertTrue("share omitted section: ${section.title}", summary.contains(section.title))
            section.actions.forEach { action ->
                assertTrue("share omitted action: ${action.label}", summary.contains(action.label))
            }
        }
        assertTrue(summary.contains("수동 안내"))
        assertTrue(summary.contains("원문") && summary.contains("포함하지 않았습니다"))
        assertFalse(summary.contains(raw))
        assertFalse(summary.contains(redactedClue))
        listOf(
            "원문-파란별",
            safeWord,
            callbackScript,
            contactLabel,
            contactNumber,
            "010-9876-5432",
            "123456789012",
            "BluePassword77",
            "A1B2C3",
            "7788",
            "evil.example",
            "900101-1234567"
        ).forEach { secret ->
            assertFalse("lockdown share leaked secret: $secret", summary.contains(secret))
        }
        assertFalse(Regex("0\\d{1,2}[- .]?\\d{3,4}[- .]?\\d{4}").containsMatchIn(summary))
        assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(summary))
        assertFalse(
            Regex("(?i)(?:[a-z][a-z0-9+.-]*://|www\\.|[a-z0-9-]+\\.(?:com|net|org|kr|한국))")
                .containsMatchIn(summary)
        )
        assertFalse(
            Regex("(?<!\\d)\\d{6}\\s*-?\\s*[1-8]\\d{6}(?!\\d)")
                .containsMatchIn(summary)
        )

        val defaultChecklist = AccountLockdownChecklistBuilder.build(null)
        val defaultSummary = defaultChecklist.familyShareSummary
        defaultChecklist.sections.forEach { section ->
            assertTrue(defaultSummary.contains(section.title))
            section.actions.forEach { action -> assertTrue(defaultSummary.contains(action.label)) }
        }
        assertTrue(defaultSummary.contains("새 의심 내용 분석"))
    }

    @Test
    fun outputIsDeterministicForTheSameAnalysis() {
        val analysis = analysisWith(
            ScamSignal.APP_INSTALL,
            ScamSignal.CREDENTIALS,
            ScamSignal.LINK,
            ScamSignal.PHONE_NUMBER_CHANGE
        )

        val first = AccountLockdownChecklistBuilder.build(analysis)
        val second = AccountLockdownChecklistBuilder.build(analysis)

        assertEquals(first, second)
        assertEquals(first.sections, second.sections)
        assertEquals(first.recommendedSectionIds, second.recommendedSectionIds)
        assertEquals(first.recommendedActionIds, second.recommendedActionIds)
        assertEquals(first.familyShareSummary, second.familyShareSummary)
    }

    @Test
    fun copyStatesLocalManualStatelessLimitsWithoutUnsafeClaims() {
        val checklists = listOf(
            AccountLockdownChecklistBuilder.build(null),
            AccountLockdownChecklistBuilder.build(analysisWith(ScamSignal.APP_INSTALL)),
            AccountLockdownChecklistBuilder.build(
                analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER)
            ),
            AccountLockdownChecklistBuilder.build(analysisWith(ScamSignal.LINK))
        )
        val forbiddenAffirmativeClaims = listOf(
            "자동으로 감시합니다",
            "자동 모니터링합니다",
            "몰래 확인합니다",
            "자동으로 검사합니다",
            "자동 스캔합니다",
            "자동으로 읽습니다",
            "연락처를 자동으로 읽습니다",
            "연락처 앱에서 자동으로 가져옵니다",
            "서버로 업로드합니다",
            "네트워크로 업로드합니다",
            "악성코드를 자동으로 제거합니다",
            "악성코드 제거를 보장합니다",
            "계좌를 자동으로 지급정지합니다",
            "계좌 지급정지를 보장합니다",
            "카드를 자동으로 정지합니다",
            "계정을 자동으로 잠급니다",
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
            "malware removal guarantee",
            "account freeze guarantee",
            "legal guarantee",
            "guaranteed recovery"
        )

        checklists.forEach { checklist ->
            val copy = allCopy(checklist)
            val privacyCopy = checklist.privacySafetyNotes.joinToString(" ")

            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "lockdown copy contained unsafe claim: $claim",
                    copy.lowercase().contains(claim.lowercase())
                )
            }
            assertTrue(privacyCopy.contains("기기 안"))
            assertTrue(privacyCopy.contains("사용자가 직접 붙여넣"))
            assertTrue(privacyCopy.contains("체크 완료 상태") && privacyCopy.contains("저장하지 않"))
            assertTrue(
                privacyCopy.contains("사건 기록") &&
                    privacyCopy.contains("추가·수정·삭제하지 않습니다")
            )
            assertTrue(privacyCopy.contains("네트워크 호출"))
            assertTrue(privacyCopy.contains("새 Android 권한"))
            assertTrue(privacyCopy.contains("기기 설정 변경") && privacyCopy.contains("실행하지 않습니다"))
        }
    }

    private fun analysisWith(vararg signals: ScamSignal): ScamAnalysis = ScamAnalysis(
        input = "테스트 입력 ${signals.joinToString { signal -> signal.name }}",
        score = 50,
        riskLevel = RiskLevel.MEDIUM,
        matchedSignals = signals.toList()
    )

    private fun allCopy(checklist: AccountLockdownChecklist): String = buildString {
        append(checklist.title)
        append(checklist.intro)
        append(checklist.recommendationTitle)
        append(checklist.recommendationExplanation)
        checklist.sections.forEach { section ->
            append(section.urgency.label)
            append(section.title)
            append(section.whyItMatters)
            section.manualSteps.forEach(::append)
            section.doNotDoNotes.forEach(::append)
            section.actions.forEach { action -> append(action.label) }
        }
        checklist.privacySafetyNotes.forEach(::append)
        append(checklist.familyShareSummary)
    }
}
