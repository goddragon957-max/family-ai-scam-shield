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
            "안전 붙여넣기 개인정보 코치",
            "가림 이력",
            "클립보드",
            "진행 중 연락 종료 문장",
            "말하기",
            "통화 종료",
            "송금 전 멈춤 확인",
            "결제 상태",
            "화면공유·원격지시 끊기 코치",
            "원격 접근 상태",
            "앱 목록",
            "패키지 이름",
            "기기 상태",
            "별도 경로 확인 코치",
            "확인 상태",
            "채널 값",
            "공식 번호",
            "노출 영향 분류",
            "노출 선택",
            "노출 이력",
            "보호자 안전 검토",
            "보호자 질문",
            "답변",
            "메모",
            "보호자 검토 이력",
            "후속 점검 계획",
            "시점 선택",
            "알림 일정",
            "후속 점검 이력",
            "가족 대응 역할 보드",
            "담당자 배정",
            "역할 선택",
            "체크 표시",
            "역할 보드 이력",
            "가족 차분 대화 안건",
            "안건·카드 선택",
            "대화 답변",
            "대화 이력",
            "가족 콜백 카드",
            "상담 준비 카드",
            "체크리스트",
            "가족 경고 메시지",
            "공유 이력",
            "가족 안전 연습 코치",
            "드릴 선택",
            "통과 여부",
            "연습 이력",
            "가족 준비 로드맵",
            "카드 선택",
            "완료",
            "로드맵 이력",
            "샘플 선택"
        ).forEach { term ->
            assertTrue("transient copy omitted $term", transientCopy.contains(term))
        }
        assertTrue(
            transientCopy.contains("영구 저장하지 않") ||
                transientCopy.contains("SharedPreferences에 저장하지 않")
        )
        assertTrue(
            transientCopy.contains(
                "가족 준비 로드맵의 카드 선택·완료·체크·점수·읽음·진행·공유·로드맵 이력"
            )
        )
        assertTrue(
            transientCopy.contains(
                "노출 영향 분류의 노출 선택·답변·완료·체크·점수·읽음·진행·공유·노출 이력"
            )
        )
        assertTrue(
            transientCopy.contains(
                "보호자 안전 검토의 카드 선택·보호자 질문·답변·메모·완료·체크·점수·읽음·진행·" +
                    "공유·보호자 검토 이력"
            )
        )
        assertTrue(
            transientCopy.contains(
                "후속 점검 계획의 시점 선택·답변·메모·알림 일정·완료·체크·점수·읽음·진행·" +
                    "공유·후속 점검 이력"
            )
        )
        assertTrue(
            transientCopy.contains(
                "가족 대응 역할 보드의 이름·담당자 배정·역할 선택·체크 표시·메모·완료·점수·읽음·진행·" +
                    "공유·역할 보드 이력"
            )
        )
        assertTrue(
            transientCopy.contains(
                "가족 차분 대화 안건의 안건·카드 선택·대화 답변·메모·담당자·날짜·체크·완료·점수·" +
                    "읽음·진행·공유·대화 이력"
            )
        )
        assertTrue(
            transientCopy.contains(
                "가족 대응 역할 보드"
            ) && transientCopy.contains("사건 기록을 추가·수정·삭제하지 않습니다")
        )
        assertTrue(
            transientCopy.contains("가족 차분 대화 안건") &&
                transientCopy.contains("무상태 수동 안내") &&
                transientCopy.contains("새 권한·네트워크") &&
                transientCopy.contains("연락처") &&
                transientCopy.contains("읽거나 감시하지 않") &&
                transientCopy.contains("자동 전송하지 않습니다")
        )
        assertTrue(
            transientCopy.contains("가족 차분 대화 안건 화면") &&
                transientCopy.contains("안건·경로·공유 버튼") &&
                transientCopy.contains("사건 기록을 추가·수정·삭제하지 않습니다")
        )
        assertTrue(
            transientCopy.contains("안전 붙여넣기 개인정보 코치") &&
                transientCopy.contains("로컬 전용·무상태 수동 안내") &&
                transientCopy.contains("새 Android 권한·네트워크") &&
                transientCopy.contains("클립보드") &&
                transientCopy.contains("읽거나 감시하지 않") &&
                transientCopy.contains("자동 가림·교정·검사·삭제") &&
                transientCopy.contains("자동 전송하지 않")
        )
        assertTrue(
            transientCopy.contains("카드 선택·진행·읽음·공유·가림 이력·체크 상태·메모") &&
                transientCopy.contains("저장하지 않습니다")
        )
        assertTrue(
            transientCopy.contains("안전 붙여넣기 개인정보 코치 화면") &&
                transientCopy.contains("카드·경로·공유 버튼") &&
                transientCopy.contains("붙여넣은 텍스트나 사건 기록을 추가·수정·삭제하지 않습니다")
        )
        assertTrue(
            transientCopy.contains("알림·알람·캘린더 일정·리마인더·백그라운드 작업") &&
                transientCopy.contains("만들거나 예약하지 않습니다")
        )
        assertTrue(
            transientCopy.contains(
                "진행 중 연락 종료 문장의 카드 선택·추천·읽음·공유·말하기·답장·통화 종료·" +
                    "체크 상태·메모"
            ) && transientCopy.contains("저장하지 않")
        )
        assertTrue(
            transientCopy.contains(
                "송금 전 멈춤 확인의 카드 선택·추천·읽음·공유·체크·메모·결제 상태·진행·이력"
            ) && transientCopy.contains("저장하지 않")
        )
        assertTrue(
            transientCopy.contains(
                "화면공유·원격지시 끊기 코치의 카드 선택·추천·읽음·공유·체크·메모·원격 접근 상태·" +
                    "앱 목록·패키지 이름·기기 상태·진행·이력"
            ) && transientCopy.contains("저장하지 않")
        )
        assertTrue(
            transientCopy.contains("저장하지 않") || transientCopy.contains("기록하지 않")
        )
    }

    @Test
    fun activeContactExitScriptsAreInventoriedAsLocalManualAndNonAutomated() {
        val center = PrivacyCenterBuilder.build(emptyPlan(), 0, false)
        val inventory = center.localDataInventoryItems.joinToString("\n") { item ->
            "${item.title} ${item.status} ${item.detail}"
        }
        val statements = center.noCollectionStatements.joinToString("\n")
        val featureCopy = "$inventory\n$statements\n${center.familyShareSummary}"

        assertTrue(featureCopy.contains("진행 중 연락 종료 문장"))
        assertTrue(featureCopy.contains("로컬 전용") && featureCopy.contains("무상태 수동 안내"))
        assertTrue(featureCopy.contains("새 Android 권한") && featureCopy.contains("네트워크"))
        assertTrue(featureCopy.contains("문자·메시지·통화·연락처·클립보드"))
        assertTrue(featureCopy.contains("읽거나 감시하지 않"))
        assertTrue(featureCopy.contains("전화를 걸거나 끊지 않"))
        assertTrue(featureCopy.contains("문자 답장·받는 사람 선택·공유를 자동으로 하지 않습니다"))
        assertTrue(featureCopy.contains("통화 차단") && featureCopy.contains("통화 선별"))
        assertTrue(featureCopy.contains("자동 SMS 기능이 아닙니다"))
        assertTrue(featureCopy.contains("카드 선택·추천·읽음·공유·말하기·답장·통화 종료·체크 상태·메모"))
        assertTrue(featureCopy.contains("저장하지 않"))
        assertTrue(featureCopy.contains("알림·알람·일정·백그라운드 작업"))
        assertTrue(featureCopy.contains("만들거나 예약하지 않습니다"))
        assertTrue(
            featureCopy.contains("붙여넣은 텍스트·통화 상태·연락처·사건 기록·가족 안전 계획·기기 설정") &&
                featureCopy.contains("추가·수정·삭제하지 않습니다")
        )
    }

    @Test
    fun moneyTransferPauseGateIsInventoriedAsLocalManualAndHasNoFinancialAccess() {
        val center = PrivacyCenterBuilder.build(emptyPlan(), 0, false)
        val inventory = center.localDataInventoryItems.joinToString("\n") { item ->
            "${item.title} ${item.status} ${item.detail}"
        }
        val statements = center.noCollectionStatements.joinToString("\n")
        val featureCopy = "$inventory\n$statements\n${center.familyShareSummary}"

        assertTrue(featureCopy.contains("송금 전 멈춤 확인"))
        assertTrue(featureCopy.contains("로컬 전용") && featureCopy.contains("무상태 수동 안내"))
        assertTrue(featureCopy.contains("새 Android 권한") && featureCopy.contains("네트워크"))
        assertTrue(featureCopy.contains("문자·메시지·통화·연락처·클립보드·금융 앱"))
        assertTrue(featureCopy.contains("읽거나 감시하지 않"))
        assertTrue(featureCopy.contains("결제·은행 API"))
        assertTrue(featureCopy.contains("NFC") && featureCopy.contains("Android 계정 관리자"))
        assertTrue(featureCopy.contains("송금·결제 접근 권한"))
        assertTrue(featureCopy.contains("계좌·카드·은행 연동"))
        assertTrue(featureCopy.contains("받는 사람") && featureCopy.contains("자동 전송"))
        assertTrue(featureCopy.contains("알림·알람·일정·백그라운드 작업"))
        assertTrue(featureCopy.contains("만들거나 예약하지 않습니다"))
        assertTrue(
            featureCopy.contains("카드 선택·추천·읽음·공유·체크·메모·결제 상태·진행·이력") &&
                featureCopy.contains("저장하지 않")
        )
        assertTrue(featureCopy.contains("돈을 자동 차단·이체·결제·취소"))
        assertTrue(featureCopy.contains("계정·카드를 자동 정지·동결"))
        assertTrue(featureCopy.contains("환급·자금 회수·피해 복구"))
        assertTrue(
            featureCopy.contains(
                "붙여넣은 텍스트·통화 상태·연락처·사건 기록·가족 안전 계획·계정·카드·은행·" +
                    "기기 설정·돈의 움직임"
            ) && featureCopy.contains("추가·수정·삭제하지 않습니다")
        )
    }

    @Test
    fun remoteAccessCutoffCoachIsInventoriedAsLocalManualAndHasNoDeviceControl() {
        val center = PrivacyCenterBuilder.build(emptyPlan(), 0, false)
        val inventory = center.localDataInventoryItems.joinToString("\n") { item ->
            "${item.title} ${item.status} ${item.detail}"
        }
        val statements = center.noCollectionStatements.joinToString("\n")
        val featureCopy = "$inventory\n$statements\n${center.familyShareSummary}"

        assertTrue(featureCopy.contains("화면공유·원격지시 끊기 코치"))
        assertTrue(featureCopy.contains("로컬 전용") && featureCopy.contains("무상태 수동 안내"))
        assertTrue(
            featureCopy.contains("새 Android 권한·네트워크·저장소") &&
                featureCopy.contains("문자·메시지·통화·연락처·클립보드") &&
                featureCopy.contains("읽거나 감시하지 않")
        )
        listOf(
            "기기 관리자",
            "접근성 서비스",
            "VPN",
            "알림 리스너",
            "사용 통계",
            "앱 작업 권한"
        ).forEach { boundary ->
            assertTrue("remote-access copy omitted $boundary", featureCopy.contains(boundary))
        }
        assertTrue(
            featureCopy.contains("설치된 앱·패키지 목록") &&
                (featureCopy.contains("조회하거나 스캔하지 않습니다") ||
                    featureCopy.contains("조회·스캔하지 않습니다"))
        )
        assertTrue(featureCopy.contains("앱·파일·프로필") && featureCopy.contains("설치"))
        assertTrue(featureCopy.contains("앱을 제거하지 않") || featureCopy.contains("앱 제거"))
        assertTrue(
            featureCopy.contains("기기·계정·접근성·네트워크·VPN·알림 설정") &&
                (featureCopy.contains("바꾸지 않습니다") || featureCopy.contains("설정 변경"))
        )
        assertTrue(featureCopy.contains("받는 사람") && featureCopy.contains("자동 전송하지 않"))
        assertTrue(featureCopy.contains("알림·알람·일정·백그라운드 작업"))
        assertTrue(featureCopy.contains("만들거나 예약하지 않습니다"))
        assertTrue(
            featureCopy.contains(
                "카드 선택·추천·읽음·공유·체크·메모·원격 접근 상태·앱 목록·패키지 이름·" +
                    "기기 상태·진행·이력"
            ) && featureCopy.contains("저장하지 않")
        )
        assertTrue(featureCopy.contains("원격 접근·화면공유·통화·앱을 자동으로 끊거나 차단하지 않"))
        assertTrue(
            featureCopy.contains("악성코드를 검사·제거") &&
                featureCopy.contains("기기를 청소하지 않")
        )
        assertTrue(featureCopy.contains("계정·카드 동결") && featureCopy.contains("결제 취소"))
        assertTrue(featureCopy.contains("환급·자금 회수·피해 복구를 자동 수행하지 않습니다"))
        assertTrue(
            featureCopy.contains(
                "붙여넣은 텍스트·통화·화면공유·원격 접근 상태·연락처·사건 기록·가족 안전 계획·" +
                    "계정·카드·은행·기기 설정·설치 앱·패키지 상태·접근성·네트워크·VPN·알림 " +
                    "설정·돈의 움직임"
            ) && featureCopy.contains("추가·수정·삭제하지 않습니다")
        )
    }

    @Test
    fun separateChannelVerificationCoachIsInventoriedAsManualValueFreeAndNonAutomated() {
        val center = PrivacyCenterBuilder.build(emptyPlan(), 0, false)
        val transientInventory = center.localDataInventoryItems.single { item ->
            item.title == "임시 입력·카드·선택 상태"
        }
        val featureStatements = center.noCollectionStatements.filter { statement ->
            statement.contains("별도 경로 확인 코치")
        }
        assertEquals(3, featureStatements.size)
        val persistenceStatement = featureStatements.single { statement ->
            !statement.startsWith("별도 경로 확인 코치")
        }
        val boundaryStatement = featureStatements.single { statement ->
            statement.startsWith("별도 경로 확인 코치는")
        }
        val mutationStatement = featureStatements.single { statement ->
            statement.startsWith("별도 경로 확인 코치 화면")
        }

        assertTrue(transientInventory.detail.contains("별도 경로 확인 코치"))
        assertTrue(boundaryStatement.contains("로컬 전용·무상태 수동 안내"))
        assertTrue(boundaryStatement.contains("새 Android 권한·네트워크·저장소"))
        assertTrue(
            boundaryStatement.contains("연락처·문자·메시지·통화·클립보드") &&
                boundaryStatement.contains("읽거나 감시하지 않")
        )
        listOf(
            "연락처를 읽거나 쓰거나 고르지 않",
            "전화 앱 열기",
            "자동 발신",
            "SMS",
            "받는 사람 선택",
            "자동 전송",
            "웹 탐색",
            "지도·위치",
            "기관 번호 조회",
            "전화번호 검증",
            "URL·도메인 검증",
            "링크 스캔",
            "알림·알람·일정",
            "스케줄러",
            "백그라운드 작업",
            "결제·은행 기능",
            "기기 제어 기능",
            "앱 설치·제거·설정 변경"
        ).forEach { boundary ->
            assertTrue(
                "separate-channel boundary statement omitted $boundary",
                boundaryStatement.contains(boundary)
            )
        }
        assertTrue(
            transientInventory.detail.contains(
                "카드 선택·추천·읽음·공유·확인 상태·채널 값·공식 번호·앱 이름·기기 상태·" +
                    "체크·메모·진행·이력은 만들거나 기록하지 않"
            )
        )
        assertTrue(
            persistenceStatement.contains(
                "별도 경로 확인 코치의 카드 선택·추천·읽음·공유·확인 상태·채널 값·공식 " +
                    "번호·앱 이름·기기 상태·체크·메모·진행·이력은 만들거나 기록하지 않"
            )
        )
        assertTrue(
            mutationStatement.contains("별도 경로 확인 코치 화면") &&
                mutationStatement.contains("카드·경로·공유 버튼") &&
                mutationStatement.contains("기관 목록") &&
                mutationStatement.contains("돈의 움직임을 추가·수정·삭제하지 않습니다")
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
        assertTrue(
            summary.contains(
                "가족 대응 역할 보드의 이름·담당자 배정·역할 선택·체크 표시·메모·완료·점수·읽음·진행·" +
                    "공유·역할 보드 이력"
            )
        )
        assertTrue(
            summary.contains(
                "가족 차분 대화 안건의 안건·카드 선택·대화 답변·메모·담당자·날짜·체크·완료·점수·" +
                    "읽음·진행·공유·대화 이력"
            )
        )
        assertTrue(
            summary.contains("가족 대응 역할 보드") &&
                summary.contains("사건 기록을 추가·수정·삭제하지 않습니다")
        )
        assertTrue(
            summary.contains("가족 차분 대화 안건") &&
                summary.contains("새 권한·네트워크") &&
                summary.contains("무상태 수동 안내") &&
                summary.contains("연락처·메시지·통화를 읽거나 감시하지 않") &&
                summary.contains("자동 전송하지 않습니다")
        )
        assertTrue(
            summary.contains("안건·경로·공유 버튼") &&
                summary.contains("사건 기록을 추가·수정·삭제하지 않습니다")
        )
        assertTrue(
            summary.contains("안전 붙여넣기 개인정보 코치") &&
                summary.contains("로컬 전용·무상태 수동 안내") &&
                summary.contains("새 Android 권한·네트워크") &&
                summary.contains("클립보드") &&
                summary.contains("읽거나 감시·저장·변경하지 않") &&
                summary.contains("자동 가림·교정·검사·삭제") &&
                summary.contains("자동 전송을 하지 않")
        )
        assertTrue(
            summary.contains("카드 선택·진행·읽음·공유·가림 이력·체크 상태·메모") &&
                summary.contains("영구 저장하지 않습니다")
        )
        assertTrue(
            summary.contains("안전 붙여넣기 개인정보 코치 화면") &&
                summary.contains("카드·경로·공유 버튼") &&
                summary.contains("붙여넣은 텍스트나 사건 기록을 추가·수정·삭제하지 않습니다")
        )
        assertTrue(
            summary.contains("송금 전 멈춤 확인") &&
                summary.contains("로컬 전용·무상태 수동 안내") &&
                summary.contains("새 Android 권한·네트워크·저장소") &&
                summary.contains("결제·은행 API") &&
                summary.contains("송금·결제 접근 권한") &&
                summary.contains("계좌·카드·은행 연동") &&
                summary.contains("자동 전송") &&
                summary.contains("자동 정지·동결") &&
                summary.contains("환급·자금 회수·피해 복구")
        )
        assertTrue(
            summary.contains("송금 전 멈춤 확인 화면") &&
                summary.contains("카드·경로·공유 버튼") &&
                summary.contains("돈의 움직임을 추가·수정·삭제하지 않습니다")
        )
        assertTrue(
            summary.contains("화면공유·원격지시 끊기 코치") &&
                summary.contains("로컬 전용·무상태 수동 안내") &&
                summary.contains("새 Android 권한·네트워크·저장소") &&
                summary.contains("기기 관리자·접근성 서비스·VPN·알림 리스너·사용 통계·앱 작업 권한") &&
                summary.contains("설치된 앱·패키지 목록") &&
                summary.contains("조회·스캔하지 않습니다") &&
                summary.contains("앱·파일·프로필 설치") &&
                summary.contains("앱 제거") &&
                summary.contains("설정 변경") &&
                summary.contains("자동 전송") &&
                summary.contains("자동으로 끊거나 차단하지 않") &&
                summary.contains("악성코드를 검사·제거") &&
                summary.contains("기기를 청소하지 않")
        )
        assertTrue(
            summary.contains("화면공유·원격지시 끊기 코치 화면") &&
                summary.contains("카드·경로·공유 버튼") &&
                summary.contains("설치 앱·패키지 상태") &&
                summary.contains("돈의 움직임을 추가·수정·삭제하지 않습니다")
        )
        assertTrue(
            summary.contains("별도 경로 확인 코치") &&
                summary.contains("로컬 전용·무상태 수동 안내") &&
                summary.contains("연락처 읽기·쓰기·고르기") &&
                summary.contains("전화 앱 열기·자동 발신·통화·SMS") &&
                summary.contains("기관 번호 조회·전화번호 검증") &&
                summary.contains("URL·도메인 검증·링크 스캔") &&
                summary.contains("결제·은행·기기 제어")
        )
        assertTrue(
            summary.contains("별도 경로 확인 코치 화면") &&
                summary.contains("기관 목록") &&
                summary.contains("돈의 움직임을 추가·수정·삭제하지 않습니다")
        )

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
            "클립보드를 읽습니다",
            "붙여넣은 원문을 자동으로 가립니다",
            "자동으로 가림 처리합니다",
            "자동으로 교정합니다",
            "자동으로 검사합니다",
            "자동 스캔합니다",
            "통화를 자동으로 겁니다",
            "통화를 자동으로 끊습니다",
            "통화를 차단합니다",
            "통화를 선별합니다",
            "문자에 자동으로 답장합니다",
            "받는 사람을 자동으로 선택합니다",
            "송금을 자동으로 실행합니다",
            "결제를 자동으로 실행합니다",
            "송금을 자동으로 취소합니다",
            "결제를 자동으로 취소합니다",
            "계정을 자동으로 정지합니다",
            "카드를 자동으로 정지합니다",
            "계정을 자동으로 동결합니다",
            "카드를 자동으로 동결합니다",
            "원격 접근을 끊었습니다",
            "원격 접근이 끊겼습니다",
            "원격 접근을 자동으로 끊습니다",
            "화면공유를 자동으로 끊습니다",
            "악성코드를 제거합니다",
            "기기를 청소합니다",
            "설치 앱을 스캔합니다",
            "앱을 자동으로 제거합니다",
            "기기 설정을 자동으로 바꿉니다",
            "VPN으로 차단합니다",
            "자동 보호합니다",
            "본인 확인 완료",
            "번호 검증 완료",
            "링크 검증 완료",
            "공식 번호로 확인됐습니다",
            "공식 링크입니다",
            "기관을 자동 조회합니다",
            "번호를 자동 검증합니다",
            "링크를 자동 검증합니다",
            "환급을 보장합니다",
            "금융 결과를 보장합니다",
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
            assertTrue(
                center.noCollectionStatements.any {
                    it.contains("안전 붙여넣기 개인정보 코치") &&
                        it.contains("로컬 전용·무상태 수동") &&
                        it.contains("클립보드") &&
                        it.contains("자동 가림·교정·검사·삭제를 하지 않습니다")
                }
            )
            assertTrue(
                center.noCollectionStatements.any {
                    it.contains("진행 중 연락 종료 문장") &&
                        it.contains("로컬 전용") &&
                        it.contains("무상태 수동") &&
                        it.contains("통화 차단") &&
                        it.contains("자동 SMS 기능이 아닙니다")
                }
            )
            assertTrue(
                center.noCollectionStatements.any {
                    it.contains("송금 전 멈춤 확인") &&
                        it.contains("로컬 전용") &&
                        it.contains("무상태 수동") &&
                        it.contains("결제·은행 API") &&
                        it.contains("송금·결제 접근 권한") &&
                        it.contains("계좌·카드·은행 연동") &&
                        it.contains("자동 정지·동결하지 않") &&
                        it.contains("피해 복구를 자동 수행하지 않습니다")
                }
            )
            assertTrue(
                center.noCollectionStatements.any {
                    it.contains("별도 경로 확인 코치") &&
                        it.contains("로컬 전용") &&
                        it.contains("무상태 수동") &&
                        it.contains("연락처를 읽거나 쓰거나 고르지 않") &&
                        it.contains("기관 번호 조회") &&
                        it.contains("전화번호 검증") &&
                        it.contains("URL·도메인 검증") &&
                        it.contains("자동 전송") &&
                        it.contains("만들거나 기록하지 않습니다")
                }
            )
            assertTrue(
                center.noCollectionStatements.any {
                    it.contains("화면공유·원격지시 끊기 코치") &&
                        it.contains("로컬 전용") &&
                        it.contains("무상태 수동") &&
                        it.contains("기기 관리자") &&
                        it.contains("접근성 서비스") &&
                        it.contains("VPN") &&
                        it.contains("알림 리스너") &&
                        it.contains("설치된 앱·패키지 목록") &&
                        it.contains("앱을 제거하지 않") &&
                        it.contains("설정을 바꾸지 않습니다") &&
                        it.contains("자동으로 끊거나 차단하지 않") &&
                        it.contains("기기를 청소하지 않") &&
                        it.contains("피해 복구를 자동 수행하지 않습니다")
                }
            )
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
