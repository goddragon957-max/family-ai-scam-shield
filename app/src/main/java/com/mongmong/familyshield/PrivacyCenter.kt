package com.mongmong.familyshield

data class PrivacyPermissionFacts(
    val smsPermissionDeclared: Boolean = false,
    val callPhonePermissionDeclared: Boolean = false,
    val callLogPermissionDeclared: Boolean = false,
    val contactsPermissionDeclared: Boolean = false,
    val microphonePermissionDeclared: Boolean = false,
    val internetPermissionDeclared: Boolean = false,
    val usesDialerIntentOnly: Boolean = true,
    val androidBackupAllowed: Boolean = false
) {
    companion object {
        val LOCAL_ONLY_MVP = PrivacyPermissionFacts()
    }
}

data class PrivacyCenterItem(
    val title: String,
    val status: String,
    val detail: String
)

enum class PrivacyCleanupActionId {
    INCIDENT_LOGS,
    SAFETY_PLAN,
    ONBOARDING_CONSENT
}

data class PrivacyCleanupAction(
    val id: PrivacyCleanupActionId,
    val label: String,
    val description: String,
    val hasData: Boolean
)

data class PrivacyCenter(
    val title: String,
    val intro: String,
    val consentAccepted: Boolean,
    val incidentCount: Int,
    val trustedContactCount: Int,
    val hasStoredSafetyPlan: Boolean,
    val readiness: FamilyReadiness,
    val localDataInventoryItems: List<PrivacyCenterItem>,
    val permissionStatusItems: List<PrivacyCenterItem>,
    val noCollectionStatements: List<String>,
    val cleanupActions: List<PrivacyCleanupAction>,
    val privacyNotes: List<String>,
    val familyShareSummary: String
)

object PrivacyCenterBuilder {
    fun build(
        plan: SafetyPlan,
        incidentCount: Int,
        consentAccepted: Boolean,
        permissionFacts: PrivacyPermissionFacts = PrivacyPermissionFacts.LOCAL_ONLY_MVP
    ): PrivacyCenter {
        val safeIncidentCount = incidentCount.coerceAtLeast(0)
        val trustedContactCount = plan.contacts.count { it.number.isNotBlank() }
        val hasStoredSafetyPlan = !plan.isBlank
        val readiness = FamilyReadinessBuilder.build(plan)
        val localDataInventoryItems = buildInventoryItems(
            plan = plan,
            incidentCount = safeIncidentCount,
            consentAccepted = consentAccepted,
            trustedContactCount = trustedContactCount,
            readiness = readiness
        )
        val permissionStatusItems = buildPermissionItems(permissionFacts)
        val noCollectionStatements = listOf(
            "SMS·통화 기록·연락처 목록을 자동으로 읽거나 감시하지 않습니다.",
            "마이크로 통화를 녹음하거나 목소리를 수집하지 않습니다.",
            "앱 자체 서버로 분석 내용이나 저장 데이터를 전송하지 않으며 네트워크 분석을 하지 않습니다.",
            "사용자가 직접 붙여넣거나 텍스트로 공유한 내용만 현재 앱 흐름에서 로컬 분석하며, 붙여넣은 전체 원문은 사건 기록에 저장하지 않습니다.",
            "증거를 자동으로 캡처·수집하지 않으며 문자·통화·연락처를 백그라운드에서 확인하지 않습니다.",
            "긴급 멈춤 모드의 버튼 선택·공유 이력·체크 상태, 안전 흐름 선택, " +
                "노출 영향 분류의 노출 선택·답변·완료·체크·점수·읽음·진행·공유·노출 이력, " +
                "보호자 안전 검토의 카드 선택·보호자 질문·답변·메모·완료·체크·점수·읽음·진행·" +
                "공유·보호자 검토 이력, " +
                "후속 점검 계획의 시점 선택·답변·메모·알림 일정·완료·체크·점수·읽음·진행·" +
                "공유·후속 점검 이력, " +
                "가족 콜백 카드, 상담 준비 카드, " +
                "체크리스트, 가족 경고 메시지 템플릿 선택·공유 이력, " +
                "가족 안전 연습 코치의 드릴 선택·통과 여부·체크·점수·읽음·진행·공유·연습 이력은 " +
                "만들거나 기록하지 않습니다. 가족 준비 로드맵의 카드 선택·완료·체크·점수·읽음·진행·" +
                "공유·로드맵 이력과 샘플 선택도 영구 저장하지 않습니다."
        )
        val cleanupActions = buildCleanupActions(
            incidentCount = safeIncidentCount,
            hasStoredSafetyPlan = hasStoredSafetyPlan,
            consentAccepted = consentAccepted
        )
        val privacyNotes = buildPrivacyNotes(permissionFacts)
        val familyShareSummary = buildFamilyShareSummary(
            consentAccepted = consentAccepted,
            incidentCount = safeIncidentCount,
            trustedContactCount = trustedContactCount,
            hasStoredSafetyPlan = hasStoredSafetyPlan,
            readiness = readiness,
            permissionStatusItems = permissionStatusItems,
            usesDialerIntentOnly = permissionFacts.usesDialerIntentOnly
        )

        return PrivacyCenter(
            title = "개인정보 센터",
            intro = "이 기기에 저장된 정보와 사용하지 않는 권한을 한눈에 확인하고, 필요한 항목만 직접 삭제할 수 있습니다.",
            consentAccepted = consentAccepted,
            incidentCount = safeIncidentCount,
            trustedContactCount = trustedContactCount,
            hasStoredSafetyPlan = hasStoredSafetyPlan,
            readiness = readiness,
            localDataInventoryItems = localDataInventoryItems,
            permissionStatusItems = permissionStatusItems,
            noCollectionStatements = noCollectionStatements,
            cleanupActions = cleanupActions,
            privacyNotes = privacyNotes,
            familyShareSummary = familyShareSummary
        )
    }

    private fun buildInventoryItems(
        plan: SafetyPlan,
        incidentCount: Int,
        consentAccepted: Boolean,
        trustedContactCount: Int,
        readiness: FamilyReadiness
    ): List<PrivacyCenterItem> {
        val hasSafeWord = plan.safeWord.isNotBlank()
        val hasCallbackScript = plan.callbackScript.isNotBlank()
        val hasStoredSafetyPlan = !plan.isBlank

        return listOf(
            PrivacyCenterItem(
                title = "시작 동의 기록",
                status = if (consentAccepted) "동의 완료" else "동의 기록 없음",
                detail = "동의 여부 한 항목만 family_shield SharedPreferences에 이 기기 로컬 값으로 저장합니다."
            ),
            PrivacyCenterItem(
                title = "가족 안전 계획",
                status = if (hasStoredSafetyPlan) {
                    "저장됨 · ${readiness.level.label} (${readiness.completedCount}/${readiness.totalCount}) · " +
                        "번호가 있는 신뢰 연락처 ${trustedContactCount}명"
                } else {
                    "저장된 계획 없음 · ${readiness.level.label} (${readiness.completedCount}/${readiness.totalCount})"
                },
                detail = "safety_plan SharedPreferences에 안전단어 ${presence(hasSafeWord)}, " +
                    "콜백 스크립트 ${presence(hasCallbackScript)}, 사용자가 직접 입력한 신뢰 연락처를 저장합니다. " +
                    "이 센터에는 실제 입력값을 표시하지 않습니다."
            ),
            PrivacyCenterItem(
                title = "사건 기록",
                status = if (incidentCount == 0) "저장된 기록 없음 (0개)" else "${incidentCount}개 저장",
                detail = "현재 버전은 incident_log SharedPreferences에 최근 30개까지 위험도·탐지 신호·점수·저장 시각·" +
                    "개인정보를 가린 짧은 미리보기만 저장합니다. 붙여넣은 전체 원문은 사건 기록에 보관하지 않으며, " +
                    "이전 버전의 미리보기는 사건 기록 화면에서 불러올 때 다시 가립니다."
            ),
            PrivacyCenterItem(
                title = "임시 입력·카드·선택 상태",
                status = "영구 저장하지 않음",
                detail = "붙여넣은 입력의 현재 분석, 긴급 멈춤 모드의 버튼 선택·공유 이력·체크 상태, 안전 흐름 선택, " +
                    "노출 영향 분류의 노출 선택·답변·완료·체크·점수·읽음·진행·공유·노출 이력, 가족 콜백 카드, " +
                    "보호자 안전 검토의 카드 선택·보호자 질문·답변·메모·완료·체크·점수·읽음·진행·" +
                    "공유·보호자 검토 이력, " +
                    "후속 점검 계획의 시점 선택·답변·메모·알림 일정·완료·체크·점수·읽음·진행·" +
                    "공유·후속 점검 이력, " +
                    "신고용 상담 준비 카드, 대응·증거 보존 체크리스트, 가족 경고 메시지 템플릿 선택·공유 이력, " +
                    "가족 안전 연습 코치의 드릴 선택·통과 여부·체크·점수·읽음·진행·공유·연습 이력은 " +
                    "만들거나 기록하지 않습니다. 가족 준비 로드맵의 카드 선택·완료·체크·점수·읽음·진행·" +
                    "공유·로드맵 이력도 기록하지 않습니다. 샘플 선택은 현재 앱 흐름에서만 사용하며 " +
                    "앱의 SharedPreferences에 저장하지 않습니다."
            )
        )
    }

    private fun buildPermissionItems(facts: PrivacyPermissionFacts): List<PrivacyCenterItem> = listOf(
        permissionItem(
            title = "문자 읽기 (READ_SMS)",
            declared = facts.smsPermissionDeclared,
            noPermissionDetail = "SMS를 자동으로 읽는 권한을 요청하지 않습니다."
        ),
        permissionItem(
            title = "직접 통화 (CALL_PHONE)",
            declared = facts.callPhonePermissionDeclared,
            noPermissionDetail = "앱이 통화를 자동으로 시작하는 권한을 요청하지 않습니다."
        ),
        permissionItem(
            title = "통화 기록 (READ_CALL_LOG)",
            declared = facts.callLogPermissionDeclared,
            noPermissionDetail = "통화 내역을 읽는 권한을 요청하지 않습니다."
        ),
        permissionItem(
            title = "연락처 (READ_CONTACTS)",
            declared = facts.contactsPermissionDeclared,
            noPermissionDetail = "연락처 목록을 읽는 권한을 요청하지 않으며 신뢰 연락처는 사용자가 직접 입력합니다."
        ),
        permissionItem(
            title = "마이크 (RECORD_AUDIO)",
            declared = facts.microphonePermissionDeclared,
            noPermissionDetail = "통화나 주변 소리를 녹음하는 권한을 요청하지 않습니다."
        ),
        permissionItem(
            title = "인터넷 (INTERNET)",
            declared = facts.internetPermissionDeclared,
            noPermissionDetail = "서버 전송이나 네트워크 분석을 위한 인터넷 권한을 요청하지 않습니다."
        ),
        PrivacyCenterItem(
            title = "전화 버튼 동작 (ACTION_DIAL)",
            status = if (facts.usesDialerIntentOnly) "전화 앱만 열기" else "앱 설정 확인 필요",
            detail = if (facts.usesDialerIntentOnly) {
                "ACTION_DIAL로 전화 앱만 열며, 번호와 통화를 확인한 뒤 사용자가 직접 통화를 시작합니다."
            } else {
                "전화 버튼 동작이 ACTION_DIAL 전용인지 앱 구현과 매니페스트를 다시 확인해야 합니다."
            }
        ),
        PrivacyCenterItem(
            title = "Android 클라우드 백업",
            status = if (facts.androidBackupAllowed) "백업 허용" else "클라우드 백업 비허용",
            detail = if (facts.androidBackupAllowed) {
                "android:allowBackup 설정이 켜져 있습니다. 로컬 데이터의 플랫폼 백업 범위를 확인하세요."
            } else {
                "android:allowBackup=false로 클라우드 백업·복원을 허용하지 않습니다. Android 12 이상에서는 " +
                    "제조사에 따라 기기 간 데이터 전송을 끌 수 없는 경우가 있으므로 새 기기의 전송 설정도 확인하세요."
            }
        )
    )

    private fun permissionItem(
        title: String,
        declared: Boolean,
        noPermissionDetail: String
    ): PrivacyCenterItem = PrivacyCenterItem(
        title = title,
        status = if (declared) "권한 선언됨" else "권한 없음",
        detail = if (declared) {
            "현재 제공된 권한 사실에는 이 권한이 선언된 것으로 표시됩니다. 앱 매니페스트와 동작을 확인하세요."
        } else {
            noPermissionDetail
        }
    )

    private fun buildCleanupActions(
        incidentCount: Int,
        hasStoredSafetyPlan: Boolean,
        consentAccepted: Boolean
    ): List<PrivacyCleanupAction> = listOf(
        PrivacyCleanupAction(
            id = PrivacyCleanupActionId.INCIDENT_LOGS,
            label = "사건 기록만 삭제",
            description = if (incidentCount > 0) {
                "저장된 사건 기록 ${incidentCount}개만 삭제합니다. 가족 안전 계획과 시작 동의 기록은 그대로 남습니다."
            } else {
                "현재 표시할 사건 기록이 없습니다. 이 버튼은 사건 기록 저장 영역만 비우며 " +
                    "가족 안전 계획과 시작 동의 기록은 바뀌지 않습니다."
            },
            hasData = incidentCount > 0
        ),
        PrivacyCleanupAction(
            id = PrivacyCleanupActionId.SAFETY_PLAN,
            label = "가족 안전 계획만 삭제",
            description = if (hasStoredSafetyPlan) {
                "안전단어·콜백 스크립트·직접 입력한 신뢰 연락처만 삭제합니다. 사건 기록과 시작 동의 기록은 그대로 남습니다."
            } else {
                "현재 표시할 가족 안전 계획이 없습니다. 이 버튼은 안전 계획 저장 영역만 비우며 " +
                    "사건 기록과 시작 동의 기록은 바뀌지 않습니다."
            },
            hasData = hasStoredSafetyPlan
        ),
        PrivacyCleanupAction(
            id = PrivacyCleanupActionId.ONBOARDING_CONSENT,
            label = "시작 동의 기록 초기화 (온보딩 다시 보기)",
            description = if (consentAccepted) {
                "시작 동의 기록만 지우고 즉시 동의 온보딩을 다시 표시합니다. 가족 안전 계획과 사건 기록은 그대로 남습니다."
            } else {
                "저장된 시작 동의 기록이 없습니다. 다음 시작에는 동의 온보딩이 표시됩니다."
            },
            hasData = consentAccepted
        )
    )

    private fun buildPrivacyNotes(facts: PrivacyPermissionFacts): List<String> = listOf(
        "SharedPreferences는 앱 전용 로컬 저장소로 사용합니다. Base64 저장 형식은 암호화를 뜻하지 않으므로 기기 잠금도 함께 사용하세요.",
        if (facts.androidBackupAllowed) {
            "Android 플랫폼 백업이 허용된 상태로 제공되었습니다. 실제 배포 전 백업 범위를 다시 확인하세요."
        } else {
            "android:allowBackup=false로 클라우드 백업·복원을 허용하지 않습니다. Android 12 이상 일부 제조사 기기의 " +
                "기기 간 전송은 이 설정만으로 막히지 않을 수 있으므로 새 기기의 전송 설정도 확인하세요."
        },
        "이 화면에 들어오는 것만으로 데이터가 삭제되지는 않습니다. 이름이 표시된 삭제 버튼을 사용자가 직접 누른 항목만 지웁니다.",
        "가족 공유 버튼은 사용자가 누를 때 개인정보 값이 없는 텍스트 요약만 공유 앱 선택 화면에 전달합니다. 실제로 공유한 뒤에는 선택한 앱의 개인정보 처리 방침이 적용됩니다.",
        "로컬 규칙 기반 분석과 개인정보 안내는 참고용이며 법률·금융 자문, 피해금 회수·환급 또는 안전 결과를 보장하지 않습니다."
    )

    private fun buildFamilyShareSummary(
        consentAccepted: Boolean,
        incidentCount: Int,
        trustedContactCount: Int,
        hasStoredSafetyPlan: Boolean,
        readiness: FamilyReadiness,
        permissionStatusItems: List<PrivacyCenterItem>,
        usesDialerIntentOnly: Boolean
    ): String = buildString {
        appendLine("[가족 AI 보이스피싱 방패] 개인정보 요약")
        appendLine()
        appendLine("이 앱은 사용자가 직접 붙여넣거나 텍스트로 공유한 내용만 기기 안에서 로컬 분석합니다.")
        appendLine("앱 자체 서버로 보내는 네트워크 분석이나 자동 문자·통화·연락처 감시를 하지 않습니다.")
        appendLine()
        appendLine("기기 안의 저장 상태")
        appendLine("- 시작 동의 기록: ${if (consentAccepted) "동의 완료" else "기록 없음"}")
        appendLine(
            "- 가족 안전 계획: ${if (hasStoredSafetyPlan) "저장됨" else "저장 없음"}, " +
                "${readiness.level.label} (${readiness.completedCount}/${readiness.totalCount}), " +
                "번호가 있는 신뢰 연락처 ${trustedContactCount}명"
        )
        appendLine("- 사건 기록: ${incidentCount}개")
        appendLine("- 현재 버전의 사건 기록에는 위험도·탐지 신호·점수·저장 시각·개인정보를 가린 짧은 미리보기만 보관하고 붙여넣은 전체 원문은 보관하지 않습니다.")
        appendLine("- 안전 흐름 선택, 노출 영향 분류의 노출 선택·답변·완료·체크·점수·읽음·진행·공유·노출 이력, 보호자 안전 검토의 카드 선택·보호자 질문·답변·메모·완료·체크·점수·읽음·진행·공유·보호자 검토 이력, 후속 점검 계획의 시점 선택·답변·메모·알림 일정·완료·체크·점수·읽음·진행·공유·후속 점검 이력, 콜백·상담 준비 카드, 체크리스트, 가족 경고 메시지 템플릿 선택·공유 이력은 영구 저장하지 않습니다. 가족 안전 연습 코치의 드릴 선택·통과 여부·체크·점수·읽음·진행·공유·연습 이력과 가족 준비 로드맵의 카드 선택·완료·체크·점수·읽음·진행·공유·로드맵 이력은 만들거나 기록하지 않고, 샘플 선택도 영구 저장하지 않습니다.")
        appendLine()
        appendLine("권한 상태")
        permissionStatusItems.forEach { item ->
            appendLine("- ${item.title}: ${item.status}")
        }
        appendLine()
        appendLine("데이터 정리는 개인정보 센터에서 사건 기록, 가족 안전 계획, 시작 동의 기록을 각각 따로 선택해 진행합니다.")
        if (usesDialerIntentOnly) {
            appendLine("전화 버튼은 ACTION_DIAL로 전화 앱만 열고 사용자가 직접 통화를 시작합니다.")
        } else {
            appendLine("전화 버튼이 ACTION_DIAL 전용 상태가 아니므로 앱 설정과 매니페스트 확인이 필요합니다.")
        }
        append(
            "안전단어, 콜백 스크립트, 연락처 이름·전체 전화번호, 붙여넣은 원문, " +
                "계좌번호·비밀번호·인증번호(OTP)·PIN·링크·신분정보 값은 이 텍스트 요약에 포함하지 않았습니다."
        )
    }.trim()

    private fun presence(present: Boolean): String = if (present) "있음" else "없음"
}
