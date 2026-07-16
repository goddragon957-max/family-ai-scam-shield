package com.mongmong.familyshield

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyCenterBuilderTest {
    @Test
    fun emptyStateShowsNoConsentSafetyPlanOrIncidents() {
        val center = PrivacyCenterBuilder.build(
            plan = emptyPlan(),
            incidentCount = 0,
            consentAccepted = false
        )

        assertFalse(center.consentAccepted)
        assertFalse(center.hasStoredSafetyPlan)
        assertEquals(0, center.trustedContactCount)
        assertEquals(0, center.incidentCount)
        assertEquals(ReadinessLevel.LOW, center.readiness.level)

        val consent = center.localDataInventoryItems.single { it.title == "시작 동의 기록" }
        val plan = center.localDataInventoryItems.single { it.title == "가족 안전 계획" }
        val incidents = center.localDataInventoryItems.single { it.title == "사건 기록" }
        assertTrue(consent.status.contains("기록 없음"))
        assertTrue(plan.status.contains("저장된 계획 없음"))
        assertTrue(plan.status.contains("준비 부족"))
        assertTrue(incidents.status.contains("0개"))
        assertTrue(center.cleanupActions.none { it.hasData })
    }

    @Test
    fun populatedStateShowsOnlyReadinessAndCountsWithoutPlanValues() {
        val safeWord = "파란 달 OTP 482913 https://family-secret.example"
        val callbackScript = "비밀 대사 뒤 010-9876-5432와 계좌 123456789012 PIN 7788"
        val contactLabel = "가족대표-비밀이름"
        val contactNumber = "010-2468-1357"
        val plan = SafetyPlan(
            safeWord = safeWord,
            callbackScript = callbackScript,
            contacts = listOf(
                TrustedContact(contactLabel, contactNumber),
                TrustedContact("번호없는-비밀라벨", "")
            )
        )

        val center = PrivacyCenterBuilder.build(
            plan = plan,
            incidentCount = 7,
            consentAccepted = true
        )
        val allCopy = allCopy(center)
        val planInventory = center.localDataInventoryItems.single { it.title == "가족 안전 계획" }
        val incidentInventory = center.localDataInventoryItems.single { it.title == "사건 기록" }

        assertTrue(center.consentAccepted)
        assertTrue(center.hasStoredSafetyPlan)
        assertEquals(ReadinessLevel.READY, center.readiness.level)
        assertEquals(1, center.trustedContactCount)
        assertEquals(7, center.incidentCount)
        assertTrue(planInventory.status.contains("준비 좋음"))
        assertTrue(planInventory.status.contains("신뢰 연락처 1명"))
        assertTrue(incidentInventory.status.contains("7개"))
        assertTrue(incidentInventory.detail.contains("현재 버전"))
        assertTrue(incidentInventory.detail.contains("이전 버전") && incidentInventory.detail.contains("불러올 때 다시 가립니다"))
        listOf(safeWord, callbackScript, contactLabel, contactNumber, "번호없는-비밀라벨").forEach { secret ->
            assertFalse("privacy-center output leaked plan value: $secret", allCopy.contains(secret))
        }
    }

    @Test
    fun permissionCopyNamesEveryAbsentPermissionAndDialerOnlyBehavior() {
        val center = PrivacyCenterBuilder.build(emptyPlan(), 0, false)
        val permissionCopy = center.permissionStatusItems.joinToString("\n") { item ->
            "${item.title} ${item.status} ${item.detail}"
        }

        listOf(
            "READ_SMS",
            "CALL_PHONE",
            "READ_CALL_LOG",
            "READ_CONTACTS",
            "RECORD_AUDIO",
            "INTERNET"
        ).forEach { permission ->
            val item = center.permissionStatusItems.single { it.title.contains(permission) }
            assertEquals("권한 없음", item.status)
            assertTrue(permissionCopy.contains(permission))
        }
        assertTrue(permissionCopy.contains("ACTION_DIAL"))
        assertTrue(permissionCopy.contains("전화 앱만"))
        assertTrue(permissionCopy.contains("사용자가 직접 통화를 시작"))
        assertTrue(permissionCopy.contains("android:allowBackup=false"))
        assertTrue(permissionCopy.contains("클라우드 백업·복원"))
        assertTrue(permissionCopy.contains("제조사") && permissionCopy.contains("기기 간 데이터 전송"))

        val facts = PrivacyPermissionFacts.LOCAL_ONLY_MVP
        assertFalse(facts.smsPermissionDeclared)
        assertFalse(facts.callPhonePermissionDeclared)
        assertFalse(facts.callLogPermissionDeclared)
        assertFalse(facts.contactsPermissionDeclared)
        assertFalse(facts.microphonePermissionDeclared)
        assertFalse(facts.internetPermissionDeclared)
        assertTrue(facts.usesDialerIntentOnly)
        assertFalse(facts.androidBackupAllowed)
    }

    @Test
    fun nonDefaultPermissionFactsDoNotProduceContradictoryDialerOrBackupClaims() {
        val center = PrivacyCenterBuilder.build(
            plan = emptyPlan(),
            incidentCount = 0,
            consentAccepted = false,
            permissionFacts = PrivacyPermissionFacts(
                smsPermissionDeclared = true,
                callPhonePermissionDeclared = true,
                usesDialerIntentOnly = false,
                androidBackupAllowed = true
            )
        )
        val summary = center.familyShareSummary

        assertEquals(
            "권한 선언됨",
            center.permissionStatusItems.single { it.title.contains("READ_SMS") }.status
        )
        assertEquals(
            "권한 선언됨",
            center.permissionStatusItems.single { it.title.contains("CALL_PHONE") }.status
        )
        assertEquals(
            "앱 설정 확인 필요",
            center.permissionStatusItems.single { it.title.contains("ACTION_DIAL") }.status
        )
        assertEquals(
            "백업 허용",
            center.permissionStatusItems.single { it.title == "Android 클라우드 백업" }.status
        )
        assertTrue(summary.contains("전화 버튼이 ACTION_DIAL 전용 상태가 아니"))
        assertFalse(summary.contains("전화 버튼은 ACTION_DIAL로 전화 앱만 열고"))
        assertTrue(summary.contains("Android 클라우드 백업: 백업 허용"))
    }

    @Test
    fun transientCardsChecklistsDrillsAndSampleSelectionsAreNotStored() {
        val center = PrivacyCenterBuilder.build(emptyPlan(), 0, false)
        val transientCopy = buildString {
            center.localDataInventoryItems.forEach { item ->
                append(item.title).append(item.status).append(item.detail)
            }
            center.noCollectionStatements.forEach(::append)
            append(center.familyShareSummary)
        }

        listOf(
            "긴급 멈춤 모드",
            "버튼 선택",
            "안전 흐름 선택",
            "가족 콜백 카드",
            "상담 준비 카드",
            "체크리스트",
            "가족 경고 메시지",
            "공유 이력",
            "가족 안전 연습 코치",
            "드릴 선택",
            "통과 여부",
            "연습 이력",
            "샘플 선택"
        ).forEach { term ->
            assertTrue("transient copy omitted $term", transientCopy.contains(term))
        }
        assertTrue(
            transientCopy.contains("영구 저장하지 않") ||
                transientCopy.contains("SharedPreferences에 저장하지 않")
        )
    }

    @Test
    fun familyShareExplainsPrivacyPostureWithoutUserSecretsOrSensitivePatterns() {
        val rawPastedText =
            "엄마 지금 300만원을 계좌 123456789012로 보내고 OTP 654321 PIN 7788 입력 후 https://evil.example 열어"
        val safeWord = "우리 가족 비밀은 초록 우산"
        val contactNumber = "010-9876-5432"
        val plan = SafetyPlan(
            safeWord = safeWord,
            callbackScript = rawPastedText,
            contacts = listOf(TrustedContact("비밀 가족 라벨", contactNumber))
        )

        val summary = PrivacyCenterBuilder.build(plan, 2, true).familyShareSummary

        assertTrue(summary.contains("기기 안"))
        assertTrue(summary.contains("앱 자체 서버") && summary.contains("하지 않습니다"))
        assertTrue(summary.contains("READ_SMS"))
        assertTrue(summary.contains("READ_CALL_LOG"))
        assertTrue(summary.contains("READ_CONTACTS"))
        assertTrue(summary.contains("RECORD_AUDIO"))
        assertTrue(summary.contains("INTERNET"))
        assertTrue(summary.contains("ACTION_DIAL"))
        assertTrue(summary.contains("붙여넣은 전체 원문") && summary.contains("보관하지 않습니다"))
        assertTrue(summary.contains("텍스트 요약"))

        listOf(
            rawPastedText,
            safeWord,
            "비밀 가족 라벨",
            contactNumber,
            "123456789012",
            "654321",
            "7788",
            "evil.example"
        ).forEach { secret ->
            assertFalse("family share leaked secret: $secret", summary.contains(secret))
        }
        assertFalse(Regex("0\\d{1,2}[- ]?\\d{3,4}[- ]?\\d{4}").containsMatchIn(summary))
        assertFalse(Regex("(?<!\\d)\\d{6,}(?!\\d)").containsMatchIn(summary))
        assertFalse(Regex("(?i)https?://|www\\.|[a-z0-9-]+\\.(com|net|kr)").containsMatchIn(summary))
    }

    @Test
    fun outputIsDeterministicAndNegativeIncidentCountsAreNormalized() {
        val plan = SafetyPlan(
            safeWord = "가족 질문",
            callbackScript = "통화를 끊고 다시 확인",
            contacts = listOf(TrustedContact("가족", "010-1111-2222"))
        )

        val first = PrivacyCenterBuilder.build(plan, -4, true)
        val second = PrivacyCenterBuilder.build(plan, -4, true)

        assertEquals(first, second)
        assertEquals(first.familyShareSummary, second.familyShareSummary)
        assertEquals(0, first.incidentCount)
        assertTrue(first.localDataInventoryItems.single { it.title == "사건 기록" }.status.contains("0개"))
    }

    @Test
    fun cleanupActionsAreExplicitlyScopedAndConsentResetRestartsOnboarding() {
        val center = PrivacyCenterBuilder.build(
            plan = SafetyPlan(
                safeWord = "질문",
                callbackScript = "대사",
                contacts = listOf(TrustedContact("가족", "010-1111-2222"))
            ),
            incidentCount = 3,
            consentAccepted = true
        )
        val incidents = center.cleanupActions.single { it.id == PrivacyCleanupActionId.INCIDENT_LOGS }
        val plan = center.cleanupActions.single { it.id == PrivacyCleanupActionId.SAFETY_PLAN }
        val consent = center.cleanupActions.single { it.id == PrivacyCleanupActionId.ONBOARDING_CONSENT }

        assertEquals(3, center.cleanupActions.size)
        assertTrue(center.cleanupActions.all { it.hasData })
        assertTrue(incidents.label.contains("사건 기록만"))
        assertTrue(incidents.description.contains("안전 계획") && incidents.description.contains("동의 기록"))
        assertTrue(incidents.description.contains("그대로 남"))
        assertTrue(plan.label.contains("안전 계획만"))
        assertTrue(plan.description.contains("사건 기록") && plan.description.contains("동의 기록"))
        assertTrue(plan.description.contains("그대로 남"))
        assertTrue(consent.label.contains("온보딩 다시 보기"))
        assertTrue(consent.description.contains("즉시") && consent.description.contains("온보딩"))
        assertTrue(consent.description.contains("안전 계획") && consent.description.contains("사건 기록"))
        assertTrue(consent.description.contains("그대로 남"))
    }

    @Test
    fun copyDoesNotPromiseMonitoringUploadDeletionLegalOutcomeOrRecovery() {
        val centers = listOf(
            PrivacyCenterBuilder.build(emptyPlan(), 0, false),
            PrivacyCenterBuilder.build(
                SafetyPlan(
                    safeWord = "가족 질문",
                    callbackScript = "통화를 끊고 저장된 번호로 확인",
                    contacts = listOf(TrustedContact("가족", "010-1111-2222"))
                ),
                4,
                true
            )
        )
        val forbiddenAffirmativeClaims = listOf(
            "자동으로 감시합니다",
            "자동 모니터링합니다",
            "몰래 확인합니다",
            "연락처를 읽습니다",
            "연락처 권한을 사용합니다",
            "서버로 업로드합니다",
            "네트워크로 전송합니다",
            "법적 효력을 보장합니다",
            "법적 결과를 보장합니다",
            "피해 회복을 보장합니다",
            "피해금 회수를 보장합니다",
            "반드시 환급됩니다",
            "반드시 회수됩니다",
            "자동으로 삭제합니다",
            "자동 삭제합니다",
            "100%"
        )

        centers.forEach { center ->
            val allCopy = allCopy(center)
            forbiddenAffirmativeClaims.forEach { claim ->
                assertFalse(
                    "privacy-center copy contained unsafe claim: $claim",
                    allCopy.contains(claim)
                )
            }
            assertTrue(center.noCollectionStatements.any { it.contains("자동으로 읽거나 감시하지 않습니다") })
            assertTrue(center.noCollectionStatements.any { it.contains("앱 자체 서버") && it.contains("전송하지 않") })
            assertTrue(center.privacyNotes.any { it.contains("들어오는 것만으로") && it.contains("삭제되지는 않습니다") })
            assertTrue(center.privacyNotes.any { it.contains("보장하지 않습니다") })
        }
    }

    private fun emptyPlan(): SafetyPlan = SafetyPlan("", "", emptyList())

    private fun allCopy(center: PrivacyCenter): String = buildString {
        append(center.title)
        append(center.intro)
        append(center.readiness.title)
        append(center.readiness.nextAction)
        center.readiness.checklist.forEach { item ->
            append(item.label)
            append(item.action)
        }
        center.localDataInventoryItems.forEach { item ->
            append(item.title)
            append(item.status)
            append(item.detail)
        }
        center.permissionStatusItems.forEach { item ->
            append(item.title)
            append(item.status)
            append(item.detail)
        }
        center.noCollectionStatements.forEach(::append)
        center.cleanupActions.forEach { action ->
            append(action.label)
            append(action.description)
        }
        center.privacyNotes.forEach(::append)
        append(center.familyShareSummary)
    }
}
