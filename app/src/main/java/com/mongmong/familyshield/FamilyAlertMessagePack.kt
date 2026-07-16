package com.mongmong.familyshield

enum class FamilyAlertTemplateId {
    FAMILY_WIDE_WARNING,
    NEW_NUMBER_IMPERSONATION_WARNING,
    ACCOUNT_TAKEOVER_ALERT,
    REMOTE_CONTROL_LINK_CAUTION,
    NO_TRANSFER_OR_SECRETS,
    EVIDENCE_HELP_COOPERATION,
    EVERYDAY_PREVENTION
}

enum class FamilyAlertActionId {
    FAMILY_CALLBACK_CARD,
    SAFETY_PLAN,
    SAFETY_FLOW_ROUTER,
    ACCOUNT_LOCKDOWN,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    NEW_ANALYSIS
}

enum class FamilyAlertUrgency(val rank: Int, val label: String) {
    IMMEDIATE(0, "지금 경고"),
    VERIFY(1, "직접 확인"),
    SUPPORT(2, "증거·도움"),
    PREVENTION(3, "예방 공유")
}

data class FamilyAlertAction(
    val id: FamilyAlertActionId,
    val label: String
)

data class FamilyAlertTemplate(
    val id: FamilyAlertTemplateId,
    val displayOrder: Int,
    val title: String,
    val audience: String,
    val urgency: FamilyAlertUrgency,
    val whenToUse: String,
    val messageBody: String,
    val followUpChecklist: List<String>,
    val actions: List<FamilyAlertAction>
) {
    val actionLabels: List<String>
        get() = actions.map { action -> action.label }
}

data class FamilyAlertMessagePack(
    val title: String,
    val intro: String,
    val recommendationTitle: String,
    val recommendationExplanation: String,
    val recommendedTemplateIds: List<FamilyAlertTemplateId>,
    val templates: List<FamilyAlertTemplate>,
    val privacySafetyNotes: List<String>,
    val combinedShareSummary: String
) {
    val recommendedTemplates: List<FamilyAlertTemplate>
        get() = recommendedTemplateIds.mapNotNull { templateId ->
            templates.find { template -> template.id == templateId }
        }

    val recommendedActionIds: List<FamilyAlertActionId>
        get() = recommendedTemplates
            .flatMap { template -> template.actions }
            .map { action -> action.id }
            .distinct()
}

object FamilyAlertMessagePackBuilder {
    private const val MANUAL_SHARE_FOOTER =
        "이 안내는 제가 직접 선택해 공유합니다. 앱은 연락처를 읽거나 메시지를 자동 전송하지 않습니다."

    private val familySignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.PHONE_NUMBER_CHANGE
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

    private val remoteSignals = setOf(
        ScamSignal.APP_INSTALL,
        ScamSignal.LIVE_CALL_CONTROL
    )

    private val linkSignals = setOf(
        ScamSignal.LINK,
        ScamSignal.PARCEL_INVITATION_SMISHING
    )

    fun build(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness
    ): FamilyAlertMessagePack {
        val templates = buildTemplates(hasAnalysis = analysis != null)
        val recommendedTemplateIds = recommendTemplateIds(analysis)

        return FamilyAlertMessagePack(
            title = "가족 경고 메시지 팩",
            intro =
                "상황에 맞는 개인정보 보호 경고문을 골라 Android 공유 화면에서 직접 보내세요. " +
                    "앱은 연락처를 읽거나 메시지를 자동으로 보내지 않습니다.",
            recommendationTitle = if (analysis == null) {
                "분석 전 기본 예방 안내"
            } else {
                "현재 분석 기준 추천 메시지"
            },
            recommendationExplanation = buildRecommendationExplanation(analysis, readiness),
            recommendedTemplateIds = recommendedTemplateIds,
            templates = templates,
            privacySafetyNotes = listOf(
                "현재 분석에서는 탐지 신호만, 가족 안전 계획에서는 값이 없는 준비 상태만 기기 안에서 사용합니다. " +
                    "붙여넣은 원문과 개인정보 가림 단서, 안전 질문·콜백 대사·연락처 값은 메시지에 넣지 않습니다.",
                "공유 버튼은 Android 공유 화면만 엽니다. 사용자가 받을 사람과 앱을 직접 고르고 직접 전송해야 하며, " +
                    "앱은 연락처를 읽거나 메시지를 자동 전송하지 않습니다.",
                "템플릿 선택, 공유 이력, 체크 상태를 저장하지 않고 이 화면을 열거나 공유해도 사건 기록을 추가·수정·삭제하지 않습니다.",
                "문자·통화 자동 감시나 링크·기기 자동 검사를 하지 않으며 네트워크 업로드나 새 Android 권한을 사용하지 않습니다.",
                "계정 복구, 사기 차단, 피해금 회수·환급, 법적 결과나 안전을 보장하지 않습니다. 실제 조치는 공식 기관과 서비스에 직접 확인하세요.",
                "앱은 메시지·앱·기록을 자동으로 삭제하지 않습니다. 필요한 증거 보존과 삭제는 상황을 확인한 뒤 사용자가 직접 결정하세요."
            ),
            combinedShareSummary = buildCombinedShareSummary(
                recommendedTemplateIds = recommendedTemplateIds,
                templates = templates
            )
        )
    }

    private fun buildTemplates(hasAnalysis: Boolean): List<FamilyAlertTemplate> = listOf(
        FamilyAlertTemplate(
            id = FamilyAlertTemplateId.FAMILY_WIDE_WARNING,
            displayOrder = 1,
            title = "가족 전체 경고",
            audience = "가족 단체방과 가까운 보호자",
            urgency = FamilyAlertUrgency.IMMEDIATE,
            whenToUse = "내 계정이나 전화번호를 누군가 사칭할 가능성을 가족 전체에 빠르게 알릴 때",
            messageBody = withManualShareFooter(
                "[가족 전체 경고]\n" +
                    "제 계정이나 전화번호를 사칭한 연락이 갈 수 있습니다. 제 이름으로 송금·상품권 구매·인증정보 전달·앱 설치를 요청받아도 응하지 마세요. " +
                    "새로 알려온 번호나 그 연락의 답장으로 확인하지 말고, 평소 알고 있던 기존 연락 방법으로 저에게 직접 확인해 주세요."
            ),
            followUpChecklist = listOf(
                "가족이 평소 알고 있던 기존 번호로 직접 콜백했는지 확인하세요.",
                "송금·상품권·인증정보·앱 설치 요청에 응답하지 않았는지 확인하세요.",
                "가족 안전 질문의 실제 답이나 연락처 값은 단체방에 쓰지 마세요."
            ),
            actions = listOf(
                FamilyAlertAction(
                    id = FamilyAlertActionId.FAMILY_CALLBACK_CARD,
                    label = "가족 콜백 확인 카드"
                ),
                FamilyAlertAction(
                    id = FamilyAlertActionId.SAFETY_FLOW_ROUTER,
                    label = "안전 흐름 길잡이"
                )
            )
        ),
        FamilyAlertTemplate(
            id = FamilyAlertTemplateId.NEW_NUMBER_IMPERSONATION_WARNING,
            displayOrder = 2,
            title = "새 번호 사칭 주의",
            audience = "부모님·자녀·새 번호 연락을 받기 쉬운 가족",
            urgency = FamilyAlertUrgency.IMMEDIATE,
            whenToUse = "번호 변경, 가족 긴급상황, 목소리·영상 사칭 가능성이 있을 때",
            messageBody = withManualShareFooter(
                "[새 번호 사칭 주의]\n" +
                    "‘번호가 바뀌었다’, ‘휴대전화가 고장 났다’, ‘급한 일이 있다’는 새 연락만 믿지 마세요. 목소리나 영상이 익숙해도 본인 확인이 끝난 것은 아닙니다. " +
                    "새 번호를 저장하거나 답장하지 말고, 평소 알고 있던 기존 번호와 연락 방법으로 가족에게 직접 확인해 주세요."
            ),
            followUpChecklist = listOf(
                "새로 받은 번호를 저장하거나 확인 번호로 사용하지 마세요.",
                "평소 쓰던 번호로 콜백하고 가족 안전 질문을 직접 확인하세요.",
                "확인 전에는 돈·앱·인증정보 요청을 모두 보류하세요."
            ),
            actions = listOf(
                FamilyAlertAction(
                    id = FamilyAlertActionId.FAMILY_CALLBACK_CARD,
                    label = "가족 콜백 확인 카드"
                ),
                FamilyAlertAction(
                    id = FamilyAlertActionId.SAFETY_PLAN,
                    label = "가족 안전 계획 확인"
                )
            )
        ),
        FamilyAlertTemplate(
            id = FamilyAlertTemplateId.ACCOUNT_TAKEOVER_ALERT,
            displayOrder = 3,
            title = "계정 탈취 의심 알림",
            audience = "메신저·SNS로 나와 대화하는 가족과 지인",
            urgency = FamilyAlertUrgency.IMMEDIATE,
            whenToUse = "메신저·SNS 계정 노출이나 비정상 로그인·사칭 요청이 의심될 때",
            messageBody = withManualShareFooter(
                "[계정 탈취 의심 알림]\n" +
                    "제 메신저나 SNS 계정이 노출되었을 가능성을 확인 중입니다. 제 계정에서 돈·상품권·인증정보·비밀번호·앱 설치를 요청하는 연락이 와도 답하거나 보내지 마세요. " +
                    "그 대화창이 아닌 평소의 다른 연락 방법으로 저에게 직접 확인해 주세요."
            ),
            followUpChecklist = listOf(
                "가족과 지인이 의심 요청에 답하지 않았는지 확인하세요.",
                "계정 보호는 신뢰할 수 있는 기기와 서비스 공식 화면에서 직접 진행하세요.",
                "새 비밀번호·인증정보·복구코드는 가족 채팅에도 공유하지 마세요."
            ),
            actions = listOf(
                FamilyAlertAction(
                    id = FamilyAlertActionId.ACCOUNT_LOCKDOWN,
                    label = "계정·기기 잠금 체크리스트"
                ),
                FamilyAlertAction(
                    id = FamilyAlertActionId.FAMILY_CALLBACK_CARD,
                    label = "가족 콜백 확인 카드"
                )
            )
        ),
        FamilyAlertTemplate(
            id = FamilyAlertTemplateId.REMOTE_CONTROL_LINK_CAUTION,
            displayOrder = 4,
            title = "원격제어·앱 설치·링크 주의",
            audience = "같은 연락을 받을 수 있는 가족과 지인",
            urgency = FamilyAlertUrgency.IMMEDIATE,
            whenToUse = "링크, 앱 설치, 화면공유, 원격제어 요구가 있었거나 이어질 수 있을 때",
            messageBody = withManualShareFooter(
                "[원격제어·앱 설치·링크 주의]\n" +
                    "제 이름이나 가족 이름으로 연락이 와도 링크를 열거나 앱을 설치하지 말고, 화면공유·원격제어를 허용하지 마세요. " +
                    "상대가 보낸 경로를 다시 열지 말고 대화를 멈춘 뒤, 평소 알고 있던 연락 방법과 서비스의 공식 경로로 직접 확인해 주세요."
            ),
            followUpChecklist = listOf(
                "링크·파일·앱을 다시 열지 않았는지 확인하세요.",
                "화면공유·원격제어 요청을 거절하고 진행 중인 연결이 있다면 먼저 끊으세요.",
                "이미 허용했다면 기존 계정·기기 잠금과 증거 보존 안내를 확인하세요."
            ),
            actions = listOf(
                FamilyAlertAction(
                    id = FamilyAlertActionId.ACCOUNT_LOCKDOWN,
                    label = "계정·기기 잠금 체크리스트"
                ),
                FamilyAlertAction(
                    id = FamilyAlertActionId.SAFETY_FLOW_ROUTER,
                    label = "안전 흐름 길잡이"
                ),
                FamilyAlertAction(
                    id = FamilyAlertActionId.EVIDENCE_KIT,
                    label = "증거 보존 체크리스트"
                )
            )
        ),
        FamilyAlertTemplate(
            id = FamilyAlertTemplateId.NO_TRANSFER_OR_SECRETS,
            displayOrder = 5,
            title = "송금·상품권·OTP 금지",
            audience = "송금이나 인증 요청을 받을 수 있는 모든 가족",
            urgency = FamilyAlertUrgency.IMMEDIATE,
            whenToUse = "송금·안전계좌·상품권·가상자산 또는 인증정보 요구가 의심될 때",
            messageBody = withManualShareFooter(
                "[송금·상품권·인증정보 금지]\n" +
                    "누구를 사칭하든 확인 전에는 돈을 보내거나 상품권·가상자산을 전달하지 마세요. OTP·PIN·비밀번호·복구코드·신분정보도 말하거나 보내면 안 됩니다. " +
                    "‘안전계좌’, ‘긴급 보호’, ‘대신 인증’이라는 말이 나와도 대화를 멈추고 기존 연락 방법과 공식 경로로 직접 확인해 주세요."
            ),
            followUpChecklist = listOf(
                "송금·상품권 구매·가상자산 전달을 모두 보류하세요.",
                "OTP·PIN·비밀번호·복구코드·신분정보를 보내지 마세요.",
                "이미 돈이나 정보를 보냈다면 안전 흐름 길잡이와 공식 도움을 바로 확인하세요."
            ),
            actions = listOf(
                FamilyAlertAction(
                    id = FamilyAlertActionId.SAFETY_FLOW_ROUTER,
                    label = "안전 흐름 길잡이"
                ),
                FamilyAlertAction(
                    id = FamilyAlertActionId.ACCOUNT_LOCKDOWN,
                    label = "계정·기기 잠금 체크리스트"
                ),
                FamilyAlertAction(
                    id = FamilyAlertActionId.OFFICIAL_HELP_DIRECTORY,
                    label = "신고·상담 연락처"
                )
            )
        ),
        FamilyAlertTemplate(
            id = FamilyAlertTemplateId.EVIDENCE_HELP_COOPERATION,
            displayOrder = 6,
            title = "증거·신고 협조 요청",
            audience = "의심 연락을 함께 받았거나 확인을 도울 가족",
            urgency = FamilyAlertUrgency.SUPPORT,
            whenToUse = "가족이 받은 사칭 연락의 화면과 시각을 보존하고 공식 상담을 준비할 때",
            messageBody = withManualShareFooter(
                "[증거·신고 협조 요청]\n" +
                    "비슷한 연락을 받았다면 답장하거나 다시 접속하지 말고, 메시지 화면과 받은 시각을 지우지 않은 채 보관해 주세요. " +
                    "실제 비밀번호·인증정보·계좌·신분정보는 가족 채팅에 옮기지 말고, 필요한 내용은 공식 도움 경로에서 직접 설명해 주세요."
            ),
            followUpChecklist = listOf(
                "메시지·통화·앱·거래 관련 화면과 발생 시각을 직접 보존하세요.",
                "증거를 모으려고 의심 링크나 앱을 다시 열지 마세요.",
                "민감정보를 단체방에 복사하지 말고 증거 보존·공식 도움 안내를 확인하세요."
            ),
            actions = buildList {
                add(
                    FamilyAlertAction(
                        id = FamilyAlertActionId.EVIDENCE_KIT,
                        label = "증거 보존 체크리스트"
                    )
                )
                add(
                    FamilyAlertAction(
                        id = FamilyAlertActionId.OFFICIAL_HELP_DIRECTORY,
                        label = "신고·상담 연락처"
                    )
                )
                add(
                    if (hasAnalysis) {
                        FamilyAlertAction(
                            id = FamilyAlertActionId.REPORT_PREP,
                            label = "신고용 상담 준비 카드"
                        )
                    } else {
                        FamilyAlertAction(
                            id = FamilyAlertActionId.NEW_ANALYSIS,
                            label = "새 의심 내용 분석"
                        )
                    }
                )
            }
        ),
        FamilyAlertTemplate(
            id = FamilyAlertTemplateId.EVERYDAY_PREVENTION,
            displayOrder = 7,
            title = "일상 예방 공유",
            audience = "평소 예방 루틴을 함께 정할 가족",
            urgency = FamilyAlertUrgency.PREVENTION,
            whenToUse = "현재 뚜렷한 분석 결과가 없거나 가족 예방 약속을 미리 공유할 때",
            messageBody = withManualShareFooter(
                "[가족 보이스피싱 예방 약속]\n" +
                    "급한 돈·상품권·인증정보·앱 설치 요청을 받으면 먼저 대화를 멈춰 주세요. 새 번호나 익숙한 목소리만 믿지 말고, 평소 알고 있던 가족 연락 방법으로 직접 콜백해 확인해 주세요. " +
                    "확인이 어렵다면 상대가 보낸 링크 대신 서비스의 공식 경로에서 도움을 확인해 주세요."
            ),
            followUpChecklist = listOf(
                "가족 안전 질문과 기존 콜백 방법을 미리 정해 두세요.",
                "급한 요청에도 멈춤·기존 번호 콜백·공식 경로 확인 순서를 지키세요.",
                "예방 메시지에는 실제 안전 질문의 답이나 연락처 값을 쓰지 마세요."
            ),
            actions = listOf(
                FamilyAlertAction(
                    id = FamilyAlertActionId.FAMILY_CALLBACK_CARD,
                    label = "가족 콜백 확인 카드"
                ),
                FamilyAlertAction(
                    id = FamilyAlertActionId.SAFETY_PLAN,
                    label = "가족 안전 계획 확인"
                ),
                FamilyAlertAction(
                    id = FamilyAlertActionId.NEW_ANALYSIS,
                    label = "새 의심 내용 분석"
                )
            )
        )
    )

    private fun recommendTemplateIds(analysis: ScamAnalysis?): List<FamilyAlertTemplateId> {
        if (analysis == null) {
            return defaultRecommendations()
        }

        val signals = analysis.matchedSignals.toSet()
        val recommended = linkedSetOf<FamilyAlertTemplateId>()

        if (signals.any { signal -> signal in familySignals }) {
            recommended += FamilyAlertTemplateId.NEW_NUMBER_IMPERSONATION_WARNING
            recommended += FamilyAlertTemplateId.FAMILY_WIDE_WARNING
        }
        if (signals.any { signal -> signal in financialCredentialSignals }) {
            recommended += FamilyAlertTemplateId.NO_TRANSFER_OR_SECRETS
            recommended += FamilyAlertTemplateId.ACCOUNT_TAKEOVER_ALERT
        }
        if (signals.any { signal -> signal in remoteSignals }) {
            recommended += FamilyAlertTemplateId.REMOTE_CONTROL_LINK_CAUTION
            recommended += FamilyAlertTemplateId.ACCOUNT_TAKEOVER_ALERT
        }
        if (signals.any { signal -> signal in linkSignals }) {
            recommended += FamilyAlertTemplateId.REMOTE_CONTROL_LINK_CAUTION
            recommended += FamilyAlertTemplateId.EVIDENCE_HELP_COOPERATION
        }

        return if (recommended.isEmpty()) {
            defaultRecommendations()
        } else {
            recommended.toList()
        }
    }

    private fun defaultRecommendations(): List<FamilyAlertTemplateId> = listOf(
        FamilyAlertTemplateId.EVERYDAY_PREVENTION,
        FamilyAlertTemplateId.FAMILY_WIDE_WARNING
    )

    private fun buildRecommendationExplanation(
        analysis: ScamAnalysis?,
        readiness: FamilyReadiness
    ): String {
        val signals = analysis?.matchedSignals?.toSet().orEmpty()
        val reasons = buildList {
            if (signals.any { signal -> signal in familySignals }) {
                add("가족·새 번호·목소리 사칭 가능성이 있어 새 번호 주의와 가족 전체 경고를 먼저 권합니다.")
            }
            if (signals.any { signal -> signal in financialCredentialSignals }) {
                add("돈·상품권·인증정보 관련 신호가 있어 송금·OTP 금지와 계정 탈취 의심 알림을 권합니다.")
            }
            if (signals.any { signal -> signal in remoteSignals }) {
                add("앱 설치·화면공유·원격제어 관련 신호가 있어 원격제어 주의와 계정 보호 알림을 권합니다.")
            }
            if (signals.any { signal -> signal in linkSignals }) {
                add("링크·스미싱 관련 신호가 있어 링크 주의와 증거·공식 도움 협조 메시지를 권합니다.")
            }
        }

        val base = if (analysis == null || reasons.isEmpty()) {
            "뚜렷한 유형을 단정하지 않고 일상 예방 공유와 가족 전체 경고문을 기본으로 준비하세요."
        } else {
            reasons.joinToString(" ")
        }
        val readinessGuidance = if (readiness.level == ReadinessLevel.READY) {
            "가족 콜백 확인 카드에서 준비된 확인 루틴도 함께 점검하세요."
        } else {
            "가족 안전 계획을 보완하고 가족 콜백 확인 카드에서 기존 번호 확인 순서를 함께 준비하세요."
        }

        return "$base $readinessGuidance"
    }

    private fun buildCombinedShareSummary(
        recommendedTemplateIds: List<FamilyAlertTemplateId>,
        templates: List<FamilyAlertTemplate>
    ): String {
        val templatesById = templates.associateBy { template -> template.id }

        return buildString {
            appendLine("[가족 AI 보이스피싱 방패] 가족 안전 알림")
            appendLine()
            appendLine("지금 먼저 확인할 안내")
            recommendedTemplateIds.forEachIndexed { index, templateId ->
                val template = templatesById.getValue(templateId)
                appendLine()
                appendLine("${index + 1}. ${template.title}")
                appendLine(template.messageBody.removeSuffix("\n\n$MANUAL_SHARE_FOOTER"))
            }
            appendLine()
            appendLine("붙여넣은 원문, 개인정보 가림 단서, 가족 안전 질문·콜백 대사·연락처 값, 전화·계좌·인증정보·링크·신분정보는 포함하지 않았습니다.")
            appendLine(MANUAL_SHARE_FOOTER)
            append("앱은 템플릿 선택이나 공유 이력을 저장하지 않습니다.")
        }.trim()
    }

    private fun withManualShareFooter(body: String): String =
        "$body\n\n$MANUAL_SHARE_FOOTER"
}
