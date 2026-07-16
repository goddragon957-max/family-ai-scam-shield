package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyAlertMessagePackBuilderTest {
    @Test
    fun noAnalysisHasStableTemplateOrderAndConservativeRecommendations() {
        val pack = FamilyAlertMessagePackBuilder.build(
            analysis = null,
            readiness = readiness()
        )

        assertEquals("분석 전 기본 예방 안내", pack.recommendationTitle)
        assertEquals(
            listOf(
                FamilyAlertTemplateId.FAMILY_WIDE_WARNING,
                FamilyAlertTemplateId.NEW_NUMBER_IMPERSONATION_WARNING,
                FamilyAlertTemplateId.ACCOUNT_TAKEOVER_ALERT,
                FamilyAlertTemplateId.REMOTE_CONTROL_LINK_CAUTION,
                FamilyAlertTemplateId.NO_TRANSFER_OR_SECRETS,
                FamilyAlertTemplateId.EVIDENCE_HELP_COOPERATION,
                FamilyAlertTemplateId.EVERYDAY_PREVENTION
            ),
            pack.templates.map { template -> template.id }
        )
        assertEquals((1..7).toList(), pack.templates.map { template -> template.displayOrder })
        assertEquals(
            listOf(
                FamilyAlertTemplateId.EVERYDAY_PREVENTION,
                FamilyAlertTemplateId.FAMILY_WIDE_WARNING
            ),
            pack.recommendedTemplateIds
        )
        assertEquals(
            pack.recommendedTemplateIds,
            pack.recommendedTemplates.map { template -> template.id }
        )
        assertTrue(FamilyAlertActionId.SAFETY_PLAN in pack.recommendedActionIds)
        assertTrue(FamilyAlertActionId.FAMILY_CALLBACK_CARD in pack.recommendedActionIds)
        assertTrue(pack.recommendationExplanation.contains("가족 안전 계획"))
        assertTrue(pack.recommendationExplanation.contains("가족 콜백 확인 카드"))
        assertTrue(FamilyAlertTemplateId.EVIDENCE_HELP_COOPERATION in pack.templates.map { it.id })
        assertTrue(FamilyAlertTemplateId.EVERYDAY_PREVENTION in pack.templates.map { it.id })
        assertEquals(FamilyAlertTemplateId.entries.size, pack.templates.size)
        assertEquals(pack.templates.size, pack.templates.map { it.id }.distinct().size)
        pack.recommendedTemplates.forEach { template ->
            assertTrue(pack.combinedShareSummary.contains(template.title))
        }

        val lowAnalysisPack = FamilyAlertMessagePackBuilder.build(
            analysis = ScamAnalysis(
                input = "가족 저녁 식사 안내",
                score = 0,
                riskLevel = RiskLevel.SAFE,
                matchedSignals = emptyList()
            ),
            readiness = readiness()
        )
        assertEquals(pack.recommendedTemplateIds, lowAnalysisPack.recommendedTemplateIds)
    }

    @Test
    fun familyDeepfakeAndNumberChangeRecommendNewNumberThenFamilyWideWarning() {
        listOf(
            ScamSignal.FAMILY_EMERGENCY,
            ScamSignal.DEEPFAKE_VOICE,
            ScamSignal.PHONE_NUMBER_CHANGE
        ).forEach { signal ->
            val pack = FamilyAlertMessagePackBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness(ready = true)
            )

            assertEquals(
                signal.name,
                listOf(
                    FamilyAlertTemplateId.NEW_NUMBER_IMPERSONATION_WARNING,
                    FamilyAlertTemplateId.FAMILY_WIDE_WARNING
                ),
                pack.recommendedTemplateIds
            )
            assertTrue(pack.recommendationExplanation.contains("가족·새 번호·목소리"))
            assertTrue(FamilyAlertActionId.FAMILY_CALLBACK_CARD in pack.recommendedActionIds)
        }
    }

    @Test
    fun moneyCredentialGiftCryptoAndSafeAccountRecommendNoTransferThenAccountAlert() {
        listOf(
            ScamSignal.URGENT_TRANSFER,
            ScamSignal.BAIT,
            ScamSignal.CREDENTIALS,
            ScamSignal.GIFT_CRYPTO,
            ScamSignal.GIFTICON_PIN,
            ScamSignal.SAFE_ACCOUNT_FRAUD,
            ScamSignal.LOAN_REFINANCE_FEE,
            ScamSignal.OVERSEAS_PAYMENT_ALERT
        ).forEach { signal ->
            val pack = FamilyAlertMessagePackBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness()
            )

            assertEquals(
                signal.name,
                listOf(
                    FamilyAlertTemplateId.NO_TRANSFER_OR_SECRETS,
                    FamilyAlertTemplateId.ACCOUNT_TAKEOVER_ALERT
                ),
                pack.recommendedTemplateIds
            )
            assertTrue(FamilyAlertActionId.ACCOUNT_LOCKDOWN in pack.recommendedActionIds)
            assertTrue(FamilyAlertActionId.OFFICIAL_HELP_DIRECTORY in pack.recommendedActionIds)
        }
    }

    @Test
    fun appInstallAndScreenShareRecommendRemoteCautionThenAccountAlert() {
        listOf(
            ScamSignal.APP_INSTALL,
            ScamSignal.LIVE_CALL_CONTROL
        ).forEach { signal ->
            val pack = FamilyAlertMessagePackBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness()
            )

            assertEquals(
                signal.name,
                listOf(
                    FamilyAlertTemplateId.REMOTE_CONTROL_LINK_CAUTION,
                    FamilyAlertTemplateId.ACCOUNT_TAKEOVER_ALERT
                ),
                pack.recommendedTemplateIds
            )
            assertTrue(FamilyAlertActionId.ACCOUNT_LOCKDOWN in pack.recommendedActionIds)
            assertTrue(FamilyAlertActionId.EVIDENCE_KIT in pack.recommendedActionIds)
        }
    }

    @Test
    fun linkAndSmishingRecommendRemoteLinkCautionThenEvidenceHelpWithoutAutoScanClaim() {
        listOf(
            ScamSignal.LINK,
            ScamSignal.PARCEL_INVITATION_SMISHING
        ).forEach { signal ->
            val pack = FamilyAlertMessagePackBuilder.build(
                analysis = analysisWith(signal),
                readiness = readiness()
            )

            assertEquals(
                signal.name,
                listOf(
                    FamilyAlertTemplateId.REMOTE_CONTROL_LINK_CAUTION,
                    FamilyAlertTemplateId.EVIDENCE_HELP_COOPERATION
                ),
                pack.recommendedTemplateIds
            )
            assertTrue(FamilyAlertActionId.EVIDENCE_KIT in pack.recommendedActionIds)
            assertTrue(FamilyAlertActionId.OFFICIAL_HELP_DIRECTORY in pack.recommendedActionIds)
            assertFalse(allCopy(pack).contains("자동으로 검사합니다"))
            assertFalse(allCopy(pack).contains("자동 스캔합니다"))
            assertTrue(allCopy(pack).contains("직접"))
        }
    }

    @Test
    fun everyTemplateHasCompleteMessageGuidanceAndMappedActions() {
        val packs = listOf(
            FamilyAlertMessagePackBuilder.build(null, readiness()),
            FamilyAlertMessagePackBuilder.build(
                analysisWith(
                    ScamSignal.PHONE_NUMBER_CHANGE,
                    ScamSignal.CREDENTIALS,
                    ScamSignal.APP_INSTALL,
                    ScamSignal.LINK
                ),
                readiness(ready = true)
            )
        )

        packs.forEach { pack ->
            assertTrue(pack.title.isNotBlank())
            assertTrue(pack.intro.isNotBlank())
            assertTrue(pack.recommendationTitle.isNotBlank())
            assertTrue(pack.recommendationExplanation.isNotBlank())
            assertTrue(pack.privacySafetyNotes.isNotEmpty())
            assertTrue(pack.privacySafetyNotes.all { note -> note.isNotBlank() })
            assertTrue(pack.combinedShareSummary.isNotBlank())
            assertTrue(
                pack.recommendedTemplateIds.all { templateId ->
                    pack.templates.any { template -> template.id == templateId }
                }
            )

            pack.templates.forEach { template ->
                assertTrue(template.displayOrder > 0)
                assertTrue(template.title.isNotBlank())
                assertTrue(template.audience.isNotBlank())
                assertTrue(template.urgency.rank >= 0)
                assertTrue(template.urgency.label.isNotBlank())
                assertTrue(template.whenToUse.isNotBlank())
                assertTrue(template.messageBody.isNotBlank())
                assertTrue(template.followUpChecklist.isNotEmpty())
                assertTrue(template.followUpChecklist.all { item -> item.isNotBlank() })
                assertTrue(template.actions.isNotEmpty())
                assertTrue(template.actionLabels.all { label -> label.isNotBlank() })
                assertEquals(template.actions.map { action -> action.label }, template.actionLabels)
            }
            listOf(
                "가족 전체 경고",
                "새 번호 사칭 주의",
                "계정 탈취 의심 알림",
                "원격제어·앱 설치·링크 주의",
                "송금·상품권·OTP 금지",
                "증거·신고 협조 요청",
                "일상 예방 공유"
            ).forEach { title ->
                assertTrue("missing template: $title", pack.templates.any { it.title == title })
            }
        }

        assertEquals(
            FamilyAlertActionId.entries.toSet(),
            packs.flatMap { pack -> pack.templates }
                .flatMap { template -> template.actions }
                .map { action -> action.id }
                .toSet()
        )
    }

    @Test
    fun evidenceTemplateUsesReportPrepWithAnalysisAndNewAnalysisWithoutIt() {
        fun evidenceTemplate(pack: FamilyAlertMessagePack): FamilyAlertTemplate =
            pack.templates.single { template ->
                template.id == FamilyAlertTemplateId.EVIDENCE_HELP_COOPERATION
            }

        val defaultActions = evidenceTemplate(
            FamilyAlertMessagePackBuilder.build(null, readiness())
        ).actions.map { action -> action.id }
        val analyzedActions = evidenceTemplate(
            FamilyAlertMessagePackBuilder.build(
                analysisWith(ScamSignal.LINK),
                readiness()
            )
        ).actions.map { action -> action.id }

        assertTrue(FamilyAlertActionId.NEW_ANALYSIS in defaultActions)
        assertFalse(FamilyAlertActionId.REPORT_PREP in defaultActions)
        assertTrue(FamilyAlertActionId.REPORT_PREP in analyzedActions)
        assertFalse(FamilyAlertActionId.NEW_ANALYSIS in analyzedActions)
    }

    @Test
    fun eachMessageBodyAndCombinedSummaryExcludeRawCluesPlanValuesAndSensitivePatterns() {
        val safeWord = "해질녘 파란 우산은 어디에?"
        val callbackScript = "PRIVATE-CALLBACK-푸른메모장"
        val contactLabel = "보호자-은하수"
        val contactNumber = "010-2468-1357"
        val raw =
            "RAW-BLUE-COMET 전화 010-9876-5432 계좌 123456789012 비밀번호 BluePassword77 " +
                "국제전화 +82 10 9876 5432 묶음계좌 1234-5678-9012 묶음OTP 482 913 " +
                "OTP A1B2C3 PIN 7788 https://evil.example/path http://192.0.2.1/path malicious.app 주민번호 900101-1234567 " +
                "안전 질문 $safeWord 콜백 $callbackScript 연락처 $contactLabel $contactNumber"
        val analysis = ScamAnalysis(
            input = raw,
            score = 100,
            riskLevel = RiskLevel.HIGH,
            matchedSignals = listOf(
                ScamSignal.PHONE_NUMBER_CHANGE,
                ScamSignal.CREDENTIALS,
                ScamSignal.APP_INSTALL,
                ScamSignal.LINK
            )
        )
        val plan = SafetyPlan(
            safeWord = safeWord,
            callbackScript = callbackScript,
            contacts = listOf(TrustedContact(contactLabel, contactNumber))
        )
        val pack = FamilyAlertMessagePackBuilder.build(
            analysis = analysis,
            readiness = FamilyReadinessBuilder.build(plan)
        )
        val redactedClue = IncidentPreviewRedactor.buildPrivacySafeClue(analysis)
        val shareTexts = pack.templates.map { template -> template.messageBody } +
            pack.combinedShareSummary

        shareTexts.forEach { text ->
            assertFalse(text.contains(raw))
            assertFalse(text.contains(redactedClue))
            listOf(
                "RAW-BLUE-COMET",
                safeWord,
                callbackScript,
                contactLabel,
                contactNumber,
                "010-9876-5432",
                "+82 10 9876 5432",
                "123456789012",
                "1234-5678-9012",
                "482 913",
                "BluePassword77",
                "A1B2C3",
                "7788",
                "evil.example",
                "192.0.2.1",
                "malicious.app",
                "900101-1234567"
            ).forEach { secret ->
                assertFalse("family alert share leaked secret: $secret", text.contains(secret))
            }
            assertFalse(Regex("0\\d{1,2}[- .]?\\d{3,4}[- .]?\\d{4}").containsMatchIn(text))
            assertFalse(
                Regex("(?<!\\d)(?:\\+82[- .]?)?0?1\\d[- .]?\\d{3,4}[- .]?\\d{4}(?!\\d)")
                    .containsMatchIn(text)
            )
            assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(text))
            assertFalse(
                Regex("(?<!\\d)(?:\\d[- .]?){5,}\\d(?!\\d)")
                    .containsMatchIn(text)
            )
            assertFalse(
                Regex(
                    "(?i)(?:[a-z][a-z0-9+.-]*://|www\\.|" +
                        "(?:[a-z0-9-]+\\.)+[a-z]{2,63}\\b|\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b)"
                )
                    .containsMatchIn(text)
            )
            assertFalse(
                Regex("(?<!\\d)\\d{6}\\s*-?\\s*[1-8]\\d{6}(?!\\d)")
                    .containsMatchIn(text)
            )
        }
    }

    @Test
    fun outputIsDeterministicForTheSameInputs() {
        val analysis = analysisWith(
            ScamSignal.PHONE_NUMBER_CHANGE,
            ScamSignal.CREDENTIALS,
            ScamSignal.APP_INSTALL,
            ScamSignal.LINK
        )
        val readiness = readiness(ready = true)

        val first = FamilyAlertMessagePackBuilder.build(analysis, readiness)
        val second = FamilyAlertMessagePackBuilder.build(analysis, readiness)

        assertEquals(first, second)
        assertEquals(first.templates, second.templates)
        assertEquals(first.recommendedTemplateIds, second.recommendedTemplateIds)
        assertEquals(first.recommendedActionIds, second.recommendedActionIds)
        assertEquals(first.combinedShareSummary, second.combinedShareSummary)
    }

    @Test
    fun copyStatesManualLocalStatelessSharingWithoutUnsafeClaims() {
        val packs = listOf(
            FamilyAlertMessagePackBuilder.build(null, readiness()),
            FamilyAlertMessagePackBuilder.build(
                analysisWith(ScamSignal.PHONE_NUMBER_CHANGE),
                readiness(ready = true)
            ),
            FamilyAlertMessagePackBuilder.build(
                analysisWith(ScamSignal.CREDENTIALS, ScamSignal.URGENT_TRANSFER),
                readiness()
            ),
            FamilyAlertMessagePackBuilder.build(
                analysisWith(ScamSignal.APP_INSTALL, ScamSignal.LINK),
                readiness()
            )
        )
        val forbiddenAffirmativeClaims = listOf(
            "자동으로 감시합니다",
            "자동 모니터링합니다",
            "몰래 확인합니다",
            "자동으로 검사합니다",
            "자동 스캔합니다",
            "자동으로 읽습니다",
            "연락처를 읽습니다",
            "연락처에서 가져옵니다",
            "연락처를 자동으로 읽습니다",
            "서버로 업로드합니다",
            "네트워크로 업로드합니다",
            "메시지를 자동으로 전송합니다",
            "메시지를 자동으로 보냅니다",
            "문자를 자동으로 보냅니다",
            "자동 발송합니다",
            "템플릿 선택을 저장합니다",
            "공유 이력을 저장합니다",
            "계정 복구를 보장합니다",
            "사기 차단을 보장합니다",
            "피해 회복을 보장합니다",
            "피해금 회수를 보장합니다",
            "반드시 환급됩니다",
            "반드시 회수됩니다",
            "환불을 보장합니다",
            "법적 효력을 보장합니다",
            "법적 결과를 보장합니다",
            "안전을 보장합니다",
            "자동으로 삭제합니다",
            "자동 삭제합니다",
            "자동 전송합니다",
            "100%",
            "automatic monitoring",
            "reads contacts automatically",
            "server upload",
            "sends automatically",
            "account recovery guarantee",
            "financial recovery guarantee",
            "legal guarantee"
        )

        packs.forEach { pack ->
            val copy = allCopy(pack)
            val privacyCopy = pack.privacySafetyNotes.joinToString(" ")

            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "family alert copy contained unsafe claim: $claim",
                    copy.lowercase().contains(claim.lowercase())
                )
            }
            assertTrue(copy.contains("직접 선택해 공유"))
            assertTrue(copy.contains("연락처를 읽거나") && copy.contains("않습니다"))
            assertTrue(copy.contains("자동 전송하지 않습니다"))
            assertTrue(privacyCopy.contains("템플릿 선택") && privacyCopy.contains("저장하지 않"))
            assertTrue(privacyCopy.contains("공유 이력") && privacyCopy.contains("저장하지 않"))
            assertTrue(privacyCopy.contains("체크 상태") && privacyCopy.contains("저장하지 않"))
            assertTrue(
                privacyCopy.contains("사건 기록") &&
                    privacyCopy.contains("추가·수정·삭제하지 않습니다")
            )
            assertTrue(privacyCopy.contains("기기 안"))
            assertTrue(privacyCopy.contains("네트워크 업로드"))
            assertTrue(privacyCopy.contains("새 Android 권한"))
            assertTrue(privacyCopy.contains("자동으로 삭제하지 않습니다"))
            assertTrue(privacyCopy.contains("계정 복구") && privacyCopy.contains("보장하지 않습니다"))
            assertTrue(privacyCopy.contains("피해금 회수·환급"))
            assertTrue(privacyCopy.contains("법적 결과"))
        }
    }

    private fun analysisWith(vararg signals: ScamSignal): ScamAnalysis = ScamAnalysis(
        input = "테스트 입력 ${signals.joinToString { signal -> signal.name }}",
        score = 50,
        riskLevel = RiskLevel.MEDIUM,
        matchedSignals = signals.toList()
    )

    private fun readiness(ready: Boolean = false): FamilyReadiness =
        FamilyReadinessBuilder.build(
            if (ready) {
                SafetyPlan(
                    safeWord = "가족 질문",
                    callbackScript = "기존 번호로 다시 전화한다.",
                    contacts = listOf(TrustedContact("가족", "저장된 번호"))
                )
            } else {
                SafetyPlan(safeWord = "", callbackScript = "", contacts = emptyList())
            }
        )

    private fun allCopy(pack: FamilyAlertMessagePack): String = buildString {
        append(pack.title)
        append(pack.intro)
        append(pack.recommendationTitle)
        append(pack.recommendationExplanation)
        pack.templates.forEach { template ->
            append(template.title)
            append(template.audience)
            append(template.urgency.label)
            append(template.whenToUse)
            append(template.messageBody)
            template.followUpChecklist.forEach(::append)
            template.actions.forEach { action -> append(action.label) }
        }
        pack.privacySafetyNotes.forEach(::append)
        append(pack.combinedShareSummary)
    }
}
