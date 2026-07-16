package com.mongmong.familyshield

enum class AccountLockdownSectionId {
    BANK_CARD_PAYMENT,
    MESSAGING_SOCIAL_RECOVERY,
    DEVICE_REMOTE_CLEANUP,
    CREDENTIAL_OTP_RESET,
    LINK_SMISHING_CAUTION,
    SIM_PHONE_NUMBER_PROTECTION,
    EVIDENCE_OFFICIAL_HELP
}

enum class AccountLockdownActionId {
    SAFETY_FLOW_ROUTER,
    FAMILY_CALLBACK_CARD,
    RESPONSE_CENTER,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    NEW_ANALYSIS
}

enum class AccountLockdownUrgency(val rank: Int, val label: String) {
    IMMEDIATE(0, "지금 먼저"),
    PROTECT(1, "계정 보호"),
    VERIFY(2, "직접 확인"),
    PRESERVE(3, "증거·상담")
}

data class AccountLockdownAction(
    val id: AccountLockdownActionId,
    val label: String,
    val responseSituation: IncidentSituation? = null,
    val evidenceSituation: IncidentSituation? = null
)

data class AccountLockdownSection(
    val id: AccountLockdownSectionId,
    val displayOrder: Int,
    val urgency: AccountLockdownUrgency,
    val title: String,
    val whyItMatters: String,
    val manualSteps: List<String>,
    val doNotDoNotes: List<String>,
    val actions: List<AccountLockdownAction>
) {
    val actionLabels: List<String>
        get() = actions.map { action -> action.label }
}

data class AccountLockdownChecklist(
    val title: String,
    val intro: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedSectionIds: List<AccountLockdownSectionId>,
    val sections: List<AccountLockdownSection>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
) {
    val recommendedSections: List<AccountLockdownSection>
        get() = recommendedSectionIds.mapNotNull { sectionId ->
            sections.find { section -> section.id == sectionId }
        }

    val recommendedActionIds: List<AccountLockdownActionId>
        get() = recommendedSections
            .flatMap { section -> section.actions }
            .map { action -> action.id }
            .distinct()
}

object AccountLockdownChecklistBuilder {
    private val remoteSignals = setOf(
        ScamSignal.APP_INSTALL,
        ScamSignal.LIVE_CALL_CONTROL
    )

    private val financialCredentialSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.BAIT,
        ScamSignal.CREDENTIALS,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.OVERSEAS_PAYMENT_ALERT
    )

    private val linkSignals = setOf(
        ScamSignal.LINK,
        ScamSignal.PARCEL_INVITATION_SMISHING
    )

    private val familySignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.PHONE_NUMBER_CHANGE
    )

    fun build(analysis: ScamAnalysis?): AccountLockdownChecklist {
        val sections = buildSections(hasAnalysis = analysis != null)
        val recommendedSectionIds = recommendSectionIds(analysis)
        val recommendationTitle = if (analysis == null) {
            "분석 전 기본 점검"
        } else {
            "현재 분석 기준 우선 점검"
        }
        val privacySafetyNotes = listOf(
            "현재 분석이 있으면 탐지 신호만 기기 안에서 추천 순서에 사용하며 붙여넣은 원문이나 개인정보 가림 단서를 체크리스트에 표시하지 않습니다.",
            "문자·통화·연락처를 자동으로 읽거나 감시하지 않고 사용자가 직접 붙여넣어 분석한 내용만 참고합니다.",
            "체크 완료 상태와 선택은 저장하지 않으며 이 화면을 열어도 사건 기록을 추가·수정·삭제하지 않습니다.",
            "이 앱은 기기 설정 변경, 앱 삭제, 악성코드 판정, 계정 잠금, 카드 정지나 지급정지를 실행하지 않습니다. 모든 단계는 사용자가 공식 경로에서 직접 진행합니다.",
            "체크리스트를 위해 네트워크 호출이나 새 Android 권한을 사용하지 않습니다.",
            "복구·지급정지·신고 결과는 보장할 수 없습니다. 실제 처리 가능 여부와 다음 단계는 통신사·금융회사·공식 기관에 확인하세요."
        )

        return AccountLockdownChecklist(
            title = "계정·기기 잠금 체크리스트",
            intro =
                "비밀번호·인증번호를 알려줬거나 앱 설치·화면공유·링크 클릭이 의심될 때 직접 확인할 순서입니다. " +
                    "이 앱이 설정이나 계정을 자동으로 바꾸지는 않습니다.",
            recommendationTitle = recommendationTitle,
            recommendationExplanation = buildRecommendationExplanation(analysis),
            recommendedSectionIds = recommendedSectionIds,
            sections = sections,
            privacySafetyNotes = privacySafetyNotes,
            familyShareSummary = buildFamilyShareSummary(
                recommendationTitle = recommendationTitle,
                recommendedSectionIds = recommendedSectionIds,
                sections = sections
            )
        )
    }

    private fun buildSections(hasAnalysis: Boolean): List<AccountLockdownSection> = listOf(
        AccountLockdownSection(
            id = AccountLockdownSectionId.BANK_CARD_PAYMENT,
            displayOrder = 1,
            urgency = AccountLockdownUrgency.IMMEDIATE,
            title = "은행·카드·결제 보호",
            whyItMatters =
                "계좌·카드·간편결제 정보가 노출됐거나 돈을 보냈다면 추가 거래를 막기 위한 금융회사 확인이 먼저 필요합니다.",
            manualSteps = listOf(
                "원격 접근이 의심되면 먼저 연결을 끊고 신뢰할 수 있는 다른 기기나 안전한 전화에서 은행·카드사의 공식 앱, 카드 뒷면처럼 평소 확인하던 공식 경로로 고객센터를 직접 찾으세요.",
                "사기 노출 가능성을 알리고 계좌 지급정지, 카드 이용 정지, 결제 취소 가능 여부를 요청한 뒤 처리 결과와 접수번호를 확인하세요.",
                "최근 거래와 자동이체·간편결제 연결 내역을 직접 검토하고 모르는 거래를 금융회사에 알리세요."
            ),
            doNotDoNotes = listOf(
                "문자·메신저·검색 광고에 있는 링크나 번호로 금융회사에 연락하지 마세요.",
                "피해금 회수나 환급을 약속하며 수수료·선입금을 요구하는 상대에게 추가로 돈을 보내지 마세요."
            ),
            actions = listOf(
                AccountLockdownAction(
                    id = AccountLockdownActionId.RESPONSE_CENTER,
                    label = "긴급 대응 센터"
                ),
                AccountLockdownAction(
                    id = AccountLockdownActionId.OFFICIAL_HELP_DIRECTORY,
                    label = "신고·상담 연락처"
                )
            )
        ),
        AccountLockdownSection(
            id = AccountLockdownSectionId.MESSAGING_SOCIAL_RECOVERY,
            displayOrder = 2,
            urgency = AccountLockdownUrgency.PROTECT,
            title = "메신저·SNS 계정 복구",
            whyItMatters =
                "카카오톡·텔레그램·SNS 계정이 탈취되면 가족과 지인에게 본인을 사칭한 송금·인증 요청이 이어질 수 있습니다.",
            manualSteps = listOf(
                "원격 접근이 의심되면 신뢰할 수 있는 다른 기기에서 로그인된 기기·세션을 확인하고 모르는 세션을 직접 로그아웃하세요.",
                "각 서비스의 공식 앱이나 직접 연 공식 화면에서 비밀번호를 바꾸고 2단계 인증과 복구 정보를 확인하세요.",
                "평소 쓰던 가족 연락 경로로 계정 노출 가능성을 알리고 새 송금·인증 요청에 응하지 말라고 안내하세요."
            ),
            doNotDoNotes = listOf(
                "상대가 보내온 계정 복구 링크나 새 연락처를 사용하지 마세요.",
                "인증번호·복구코드·새 비밀번호를 가족 채팅이나 상대에게 보내지 마세요."
            ),
            actions = listOf(
                AccountLockdownAction(
                    id = AccountLockdownActionId.FAMILY_CALLBACK_CARD,
                    label = "가족 콜백 확인 카드"
                ),
                AccountLockdownAction(
                    id = AccountLockdownActionId.SAFETY_FLOW_ROUTER,
                    label = "안전 흐름 길잡이"
                )
            )
        ),
        AccountLockdownSection(
            id = AccountLockdownSectionId.DEVICE_REMOTE_CLEANUP,
            displayOrder = 3,
            urgency = AccountLockdownUrgency.IMMEDIATE,
            title = "기기·원격제어 정리",
            whyItMatters =
                "원격제어·화면공유·출처 불명 앱이 남아 있으면 비밀번호를 바꾸는 화면과 새 인증정보도 상대에게 보일 수 있습니다.",
            manualSteps = listOf(
                "원격 조작이나 화면공유가 진행 중이면 먼저 비행기 모드로 전환하거나 와이파이·모바일 데이터를 꺼 연결을 끊으세요.",
                "연결이 끊긴 상태에서 안전하게 가능할 때 의심 앱의 이름·설치 시각·허용 권한과 접근성·기기 관리자 설정 화면을 먼저 남기세요.",
                "기기 설정에서 접근성·기기 관리자 권한을 사용자가 직접 확인해 해제한 뒤 의심스러운 원격제어·제어 앱을 제거하세요.",
                "비밀번호 변경과 세션 로그아웃은 의심 휴대전화가 아닌 신뢰할 수 있는 가족 휴대전화나 다른 안전한 기기에서 진행하세요.",
                "앱을 제거할 수 없거나 이상 동작이 계속되면 제조사 공식 서비스센터나 통신사 공식 지원 경로에 점검을 요청하세요."
            ),
            doNotDoNotes = listOf(
                "증거를 더 모으려고 연결을 다시 켜거나 의심 앱을 다시 실행하지 마세요.",
                "원격 연결이 이어지는 의심 기기에서 은행·이메일·메신저 비밀번호를 바꾸지 마세요.",
                "이 체크리스트만 보고 기기가 안전해졌다고 단정하지 마세요."
            ),
            actions = listOf(
                AccountLockdownAction(
                    id = AccountLockdownActionId.RESPONSE_CENTER,
                    label = "원격 접근 긴급 대응",
                    responseSituation = IncidentSituation.APP_INSTALLED
                ),
                AccountLockdownAction(
                    id = AccountLockdownActionId.EVIDENCE_KIT,
                    label = "원격제어 증거 보존",
                    evidenceSituation = IncidentSituation.APP_INSTALLED
                )
            )
        ),
        AccountLockdownSection(
            id = AccountLockdownSectionId.CREDENTIAL_OTP_RESET,
            displayOrder = 4,
            urgency = AccountLockdownUrgency.PROTECT,
            title = "비밀번호·OTP·보안수단 재설정",
            whyItMatters =
                "비밀번호·인증번호·보안카드 정보가 노출되면 같은 비밀번호를 쓰는 다른 계정과 로그인 세션도 함께 위험할 수 있습니다.",
            manualSteps = listOf(
                "원격 접근이 의심되면 다른 안전한 기기에서 이메일·금융·메신저 계정마다 서로 다른 새 비밀번호로 변경하세요.",
                "각 계정의 로그인 기기와 세션을 확인해 전체 로그아웃 또는 모르는 세션 해제를 직접 진행하세요.",
                "2단계 인증, 복구 이메일·전화번호가 바뀌지 않았는지 확인하고 본인이 관리하는 값으로 정리하세요.",
                "OTP·보안카드 정보를 알려줬다면 발급 기관의 공식 경로에서 폐기·재발급 필요 여부를 확인하세요."
            ),
            doNotDoNotes = listOf(
                "노출된 비밀번호를 조금만 바꿔 재사용하거나 여러 계정에 같은 비밀번호를 쓰지 마세요.",
                "실제 비밀번호·OTP·보안카드 번호·복구코드를 메모 공유나 가족 채팅에 복사하지 마세요."
            ),
            actions = listOf(
                AccountLockdownAction(
                    id = AccountLockdownActionId.RESPONSE_CENTER,
                    label = "인증정보 긴급 대응",
                    responseSituation = IncidentSituation.CREDENTIALS_SHARED
                ),
                AccountLockdownAction(
                    id = AccountLockdownActionId.OFFICIAL_HELP_DIRECTORY,
                    label = "공식 도움 확인"
                )
            )
        ),
        AccountLockdownSection(
            id = AccountLockdownSectionId.LINK_SMISHING_CAUTION,
            displayOrder = 5,
            urgency = AccountLockdownUrgency.VERIFY,
            title = "링크·스미싱 후속 확인",
            whyItMatters =
                "링크를 연 뒤 입력·다운로드·앱 설치가 있었는지에 따라 계정과 기기 보호 순서가 달라집니다.",
            manualSteps = listOf(
                "의심 링크를 다시 열지 말고 발신 메시지와 수신·클릭 시각을 지우지 않은 채 보관하세요.",
                "링크 뒤에 정보 입력, 파일 다운로드, 앱 설치, 화면공유가 있었는지 시간 순서대로 직접 적으세요.",
                "서비스 상태는 문자 속 링크가 아니라 공식 앱이나 주소를 직접 입력해 연 공식 사이트에서 수동으로 확인하세요.",
                "앱 설치나 원격 접근까지 허용했다면 링크 확인을 멈추고 기기·원격제어 정리 순서를 먼저 따르세요."
            ),
            doNotDoNotes = listOf(
                "확인을 위해 링크나 다운로드 파일을 다시 열거나 의심 앱을 실행하지 마세요.",
                "이 앱이 링크나 기기를 자동 검사했다고 생각하지 마세요."
            ),
            actions = listOf(
                AccountLockdownAction(
                    id = AccountLockdownActionId.EVIDENCE_KIT,
                    label = "링크 증거 보존",
                    evidenceSituation = IncidentSituation.LINK_OPENED
                ),
                AccountLockdownAction(
                    id = AccountLockdownActionId.RESPONSE_CENTER,
                    label = "링크 클릭 대응",
                    responseSituation = IncidentSituation.LINK_OPENED
                )
            )
        ),
        AccountLockdownSection(
            id = AccountLockdownSectionId.SIM_PHONE_NUMBER_PROTECTION,
            displayOrder = 6,
            urgency = AccountLockdownUrgency.VERIFY,
            title = "유심·전화번호 보호",
            whyItMatters =
                "갑작스러운 통신 끊김이나 본인이 신청하지 않은 유심·이심·번호 변경 알림은 전화번호 탈취 가능성을 통신사에 확인할 신호입니다.",
            manualSteps = listOf(
                "갑자기 통신이 끊기거나 모르는 유심·이심 변경, 번호 이동 알림이 왔는지 확인하세요.",
                "이런 징후가 있으면 다른 안전한 전화에서 통신사 공식 고객센터를 직접 찾아 본인 모르게 처리된 변경이 있는지 확인하세요.",
                "통신사 계정 비밀번호와 본인확인 수단을 공식 경로에서 점검하고 가능한 추가 보호 조치를 문의하세요."
            ),
            doNotDoNotes = listOf(
                "번호 변경을 알리는 메시지만 보고 전화번호 탈취가 확정됐다고 단정하지 마세요.",
                "문자·메신저 링크에서 통신사에 로그인하거나 인증번호를 상대에게 알려주지 마세요."
            ),
            actions = listOf(
                AccountLockdownAction(
                    id = AccountLockdownActionId.SAFETY_FLOW_ROUTER,
                    label = "안전 흐름 길잡이"
                ),
                AccountLockdownAction(
                    id = AccountLockdownActionId.FAMILY_CALLBACK_CARD,
                    label = "가족 콜백 확인 카드"
                ),
                AccountLockdownAction(
                    id = AccountLockdownActionId.OFFICIAL_HELP_DIRECTORY,
                    label = "공식 도움 확인"
                )
            )
        ),
        AccountLockdownSection(
            id = AccountLockdownSectionId.EVIDENCE_OFFICIAL_HELP,
            displayOrder = 7,
            urgency = AccountLockdownUrgency.PRESERVE,
            title = "증거 보존·공식 도움",
            whyItMatters =
                "메시지·거래·앱 정보를 안전하게 남기고 공식 기관에 실제 상황을 설명하면 다음 보호 조치를 확인하기 쉽습니다.",
            manualSteps = listOf(
                "원격 연결 차단이나 금융회사 보호 요청이 급하면 증거 정리보다 그 조치를 먼저 진행하세요.",
                "메시지·통화·거래·앱 관련 화면과 발생 시각을 보존하되 실제 비밀번호·OTP·신분증 값을 가족 채팅에 복사하지 마세요.",
                "증거 보존 체크리스트와 신고·상담 연락처를 열어 상황에 맞는 항목만 직접 확인하세요.",
                if (hasAnalysis) {
                    "현재 분석의 신고용 상담 준비 카드에서 개인정보를 가린 말할 내용을 확인하세요."
                } else {
                    "새 의심 내용을 직접 붙여넣어 분석한 뒤 필요한 상담 준비 경로를 확인하세요."
                }
            ),
            doNotDoNotes = listOf(
                "긴급 연결 차단·지급정지를 늦추면서 모든 자료를 완벽하게 모으려 하지 마세요.",
                "필요한 화면을 남기기 전에 메시지·앱·기기를 성급히 삭제하거나 초기화하지 마세요.",
                "피해 회복이나 법적 결과를 확정해 준다는 비공식 연락을 믿지 마세요."
            ),
            actions = buildList {
                add(
                    AccountLockdownAction(
                        id = AccountLockdownActionId.SAFETY_FLOW_ROUTER,
                        label = "안전 흐름 길잡이"
                    )
                )
                add(
                    AccountLockdownAction(
                        id = AccountLockdownActionId.EVIDENCE_KIT,
                        label = "증거 보존 체크리스트"
                    )
                )
                add(
                    AccountLockdownAction(
                        id = AccountLockdownActionId.OFFICIAL_HELP_DIRECTORY,
                        label = "신고·상담 연락처"
                    )
                )
                add(
                    if (hasAnalysis) {
                        AccountLockdownAction(
                            id = AccountLockdownActionId.REPORT_PREP,
                            label = "신고용 상담 준비 카드"
                        )
                    } else {
                        AccountLockdownAction(
                            id = AccountLockdownActionId.NEW_ANALYSIS,
                            label = "새 의심 내용 분석"
                        )
                    }
                )
            }
        )
    )

    private fun recommendSectionIds(analysis: ScamAnalysis?): List<AccountLockdownSectionId> {
        if (analysis == null) {
            return listOf(
                AccountLockdownSectionId.CREDENTIAL_OTP_RESET,
                AccountLockdownSectionId.EVIDENCE_OFFICIAL_HELP
            )
        }

        val signals = analysis.matchedSignals.toSet()
        val recommended = linkedSetOf<AccountLockdownSectionId>()

        if (signals.any { signal -> signal in remoteSignals }) {
            recommended += AccountLockdownSectionId.DEVICE_REMOTE_CLEANUP
            recommended += AccountLockdownSectionId.CREDENTIAL_OTP_RESET
        }
        if (signals.any { signal -> signal in financialCredentialSignals }) {
            recommended += AccountLockdownSectionId.BANK_CARD_PAYMENT
            recommended += AccountLockdownSectionId.CREDENTIAL_OTP_RESET
        }
        if (signals.any { signal -> signal in linkSignals }) {
            recommended += AccountLockdownSectionId.LINK_SMISHING_CAUTION
        }
        if (signals.any { signal -> signal in familySignals }) {
            recommended += AccountLockdownSectionId.MESSAGING_SOCIAL_RECOVERY
        }
        if (ScamSignal.PHONE_NUMBER_CHANGE in signals) {
            recommended += AccountLockdownSectionId.SIM_PHONE_NUMBER_PROTECTION
        }
        if (recommended.isEmpty()) {
            recommended += AccountLockdownSectionId.CREDENTIAL_OTP_RESET
        }
        recommended += AccountLockdownSectionId.EVIDENCE_OFFICIAL_HELP

        return recommended.toList()
    }

    private fun buildRecommendationExplanation(analysis: ScamAnalysis?): String {
        if (analysis == null) {
            return "안전 흐름 길잡이에서 현재 상황을 고르고 새 분석을 시작한 뒤, 일반 계정 보호와 증거·공식 도움 항목을 차례로 확인하세요."
        }

        val signals = analysis.matchedSignals.toSet()
        val reasons = buildList {
            if (signals.any { signal -> signal in remoteSignals }) {
                add("원격제어·앱·화면공유 신호가 있어 연결 차단과 안전한 다른 기기에서의 계정 보호를 먼저 권합니다.")
            }
            if (signals.any { signal -> signal in financialCredentialSignals }) {
                add("금융·인증정보 관련 신호가 있어 은행·카드 확인과 비밀번호·보안수단 재설정을 우선 안내합니다.")
            }
            if (signals.any { signal -> signal in linkSignals }) {
                add("링크·스미싱 신호가 있어 링크를 다시 열지 않는 확인과 증거 보존을 안내합니다.")
            }
            if (signals.any { signal -> signal in familySignals }) {
                add("가족 사칭·번호 변경 가능성이 있어 평소 연락 경로로 가족에게 알리고 콜백 확인을 권합니다.")
            }
        }
        return if (reasons.isEmpty()) {
            "강한 노출 유형을 단정할 신호가 적어 일반 계정 보호와 증거·공식 도움 항목부터 확인하세요."
        } else {
            reasons.joinToString(" ") + " 분석 신호만으로 실제 피해를 단정하지 말고 본인에게 해당하는 단계만 직접 진행하세요."
        }
    }

    private fun buildFamilyShareSummary(
        recommendationTitle: String,
        recommendedSectionIds: List<AccountLockdownSectionId>,
        sections: List<AccountLockdownSection>
    ): String {
        val sectionsById = sections.associateBy { section -> section.id }

        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 계정·기기 잠금 체크리스트")
            appendLine()
            appendLine(recommendationTitle)
            recommendedSectionIds.forEachIndexed { index, sectionId ->
                val section = sectionsById.getValue(sectionId)
                appendLine("${index + 1}. ${section.title}: ${actionLabels(section)}")
            }
            appendLine()
            appendLine("전체 수동 점검")
            sections.forEach { section ->
                appendLine("- ${section.title}: ${actionLabels(section)}")
            }
            appendLine()
            appendLine("이 체크리스트는 설정·계정·거래를 자동으로 바꾸지 않는 수동 안내입니다.")
            append(
                "붙여넣은 원문과 개인정보 가림 단서, 가족 안전 질문·콜백 내용·연락처 값, " +
                    "전화번호·계좌번호·비밀번호·인증번호·PIN·링크·도메인·신분정보는 포함하지 않았습니다."
            )
        }.trim()
    }

    private fun actionLabels(section: AccountLockdownSection): String =
        section.actions.joinToString(" / ") { action -> action.label }
}
