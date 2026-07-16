package com.mongmong.familyshield

enum class IncidentBriefingActionId {
    SAFETY_FLOW_ROUTER,
    RESPONSE_CENTER,
    EVIDENCE_KIT,
    OFFICIAL_HELP_DIRECTORY,
    REPORT_PREP,
    NEW_ANALYSIS
}

data class IncidentBriefingRiskCounts(
    val high: Int,
    val medium: Int,
    val low: Int,
    val needsReview: Int
)

data class IncidentBriefingSignal(
    val label: String,
    val incidentCount: Int
)

data class IncidentBriefingTimelineItem(
    val timestamp: Long,
    val displayTime: String,
    val riskLabel: String,
    val score: Int,
    val signalLabels: List<String>,
    val signalSummary: String,
    val redactedPreview: String,
    val recommendedNextActionLabel: String
)

data class IncidentBriefingNextStep(
    val id: IncidentBriefingActionId,
    val label: String,
    val explanation: String,
    val recommended: Boolean
)

data class IncidentTimelineBriefing(
    val title: String,
    val intro: String,
    val emptyStateTitle: String?,
    val emptyStateMessage: String?,
    val totalCount: Int,
    val riskCounts: IncidentBriefingRiskCounts,
    val highestRiskLabel: String,
    val latestIncidentDisplayTime: String?,
    val latestIncidentRiskLabel: String?,
    val topSignals: List<IncidentBriefingSignal>,
    val timelineItems: List<IncidentBriefingTimelineItem>,
    val nextSteps: List<IncidentBriefingNextStep>,
    val privacySafetyNotes: List<String>,
    val familyShareSummary: String
)

object IncidentTimelineBriefingBuilder {
    private const val UNKNOWN_RISK_LABEL = "확인 필요"
    private const val NO_SIGNAL_LABEL = "강한 위험 신호 없음"
    private const val OTHER_SIGNAL_LABEL = "기타 확인 신호"
    private const val UNKNOWN_TIME_LABEL = "시각 정보 없음"
    private const val MAX_TOP_SIGNALS = 5

    private enum class StoredRisk(val label: String, val rank: Int) {
        SAFE(RiskLevel.SAFE.label, 0),
        UNKNOWN(UNKNOWN_RISK_LABEL, 1),
        MEDIUM(RiskLevel.MEDIUM.label, 2),
        HIGH(RiskLevel.HIGH.label, 3)
    }

    private data class ParsedSignals(
        val labels: List<String>,
        val knownSignals: Set<ScamSignal>
    )

    private data class NormalizedIncident(
        val timestamp: Long,
        val risk: StoredRisk,
        val score: Int,
        val signals: ParsedSignals,
        val redactedPreview: String
    )

    private val familySignals = setOf(
        ScamSignal.FAMILY_EMERGENCY,
        ScamSignal.DEEPFAKE_VOICE,
        ScamSignal.PHONE_NUMBER_CHANGE
    )

    private val urgentSignals = setOf(
        ScamSignal.URGENT_TRANSFER,
        ScamSignal.GIFT_CRYPTO,
        ScamSignal.GIFTICON_PIN,
        ScamSignal.CREDENTIALS,
        ScamSignal.APP_INSTALL,
        ScamSignal.LIVE_CALL_CONTROL,
        ScamSignal.SAFE_ACCOUNT_FRAUD,
        ScamSignal.LOAN_REFINANCE_FEE
    )

    private val linkSignals = setOf(
        ScamSignal.LINK,
        ScamSignal.PARCEL_INVITATION_SMISHING
    )

    fun build(
        incidents: List<Incident>,
        hasCurrentAnalysis: Boolean,
        displayTime: (Long) -> String
    ): IncidentTimelineBriefing {
        val normalized = incidents
            .map(::normalizeIncident)
            .sortedWith(
                compareByDescending<NormalizedIncident> { it.timestamp }
                    .thenByDescending { it.risk.rank }
                    .thenByDescending { it.score }
                    .thenBy { it.signals.labels.joinToString("\u0000") }
                    .thenBy { it.redactedPreview }
            )
        val riskCounts = IncidentBriefingRiskCounts(
            high = normalized.count { it.risk == StoredRisk.HIGH },
            medium = normalized.count { it.risk == StoredRisk.MEDIUM },
            low = normalized.count { it.risk == StoredRisk.SAFE },
            needsReview = normalized.count { it.risk == StoredRisk.UNKNOWN }
        )
        val highestRiskLabel = normalized
            .maxByOrNull { it.risk.rank }
            ?.risk
            ?.label
            ?: "기록 없음"
        val topSignals = aggregateTopSignals(normalized)
        val allKnownSignals = normalized
            .flatMap { it.signals.knownSignals }
            .toSet()
        val highestStoredRisk = normalized.maxByOrNull { it.risk.rank }?.risk
        val nextSteps = buildNextSteps(
            hasIncidents = normalized.isNotEmpty(),
            hasCurrentAnalysis = hasCurrentAnalysis,
            knownSignals = allKnownSignals,
            highestRisk = highestStoredRisk
        )
        val timelineItems = normalized.map { incident ->
            IncidentBriefingTimelineItem(
                timestamp = incident.timestamp,
                displayTime = safeDisplayTime(incident.timestamp, displayTime),
                riskLabel = incident.risk.label,
                score = incident.score,
                signalLabels = incident.signals.labels,
                signalSummary = incident.signals.labels.ifEmpty { listOf(NO_SIGNAL_LABEL) }
                    .joinToString(", "),
                redactedPreview = incident.redactedPreview,
                recommendedNextActionLabel = recommendTimelineAction(incident)
            )
        }
        val privacySafetyNotes = listOf(
            "사용자가 직접 저장한 개인정보 가림 사건 기록만 기기 안에서 읽어 집계합니다.",
            "문자·통화·연락처를 자동으로 읽거나 감시하지 않으며, 요약이나 경로 선택을 따로 저장하지 않습니다.",
            "사건별 개인정보 가림 단서는 사건 화면에서 확인할 수 있지만 가족 공유 요약에는 넣지 않습니다.",
            "이 화면을 여는 것만으로 새 사건을 추가하지 않고 기존 사건을 삭제하지 않습니다.",
            "사건 기록과 요약을 앱 자체 서버나 네트워크로 자동으로 보내지 않으며, 가족 공유는 사용자가 직접 공유 앱을 고를 때만 진행됩니다. 새 Android 권한을 사용하지 않습니다.",
            "공식 도움 경로는 상담 준비를 위한 안내입니다. 실제 조치와 결과는 공식 기관과 금융회사에 확인하세요."
        )
        val familyShareSummary = buildFamilyShareSummary(
            totalCount = normalized.size,
            riskCounts = riskCounts,
            highestRiskLabel = highestRiskLabel,
            topSignals = topSignals,
            nextSteps = nextSteps
        )

        return IncidentTimelineBriefing(
            title = "사건 타임라인 요약",
            intro = "이 기기에 저장된 개인정보 가림 사건 기록을 시간 순서와 집계로 정리합니다. 사건별 단서는 공유문에서 제외합니다.",
            emptyStateTitle = if (normalized.isEmpty()) "저장된 사건 없음" else null,
            emptyStateMessage = if (normalized.isEmpty()) {
                "저장된 사건 기록이 아직 없습니다. 새 검사를 직접 시작하거나 안전 흐름 길잡이에서 지금 필요한 행동을 고르세요. 이 화면을 열어도 새 기록은 생기지 않습니다."
            } else {
                null
            },
            totalCount = normalized.size,
            riskCounts = riskCounts,
            highestRiskLabel = highestRiskLabel,
            latestIncidentDisplayTime = timelineItems.firstOrNull()?.displayTime,
            latestIncidentRiskLabel = timelineItems.firstOrNull()?.riskLabel,
            topSignals = topSignals,
            timelineItems = timelineItems,
            nextSteps = nextSteps,
            privacySafetyNotes = privacySafetyNotes,
            familyShareSummary = familyShareSummary
        )
    }

    private fun normalizeIncident(incident: Incident): NormalizedIncident = NormalizedIncident(
        timestamp = incident.timestamp,
        risk = normalizeRisk(incident.riskLabel),
        score = incident.score.coerceAtLeast(0),
        signals = parseSignals(incident.signals),
        redactedPreview = IncidentPreviewRedactor.redact(incident.preview)
    )

    private fun normalizeRisk(rawLabel: String): StoredRisk {
        val normalized = rawLabel.trim().lowercase()
        return when (normalized) {
            RiskLevel.HIGH.label, "고위험", "high" -> StoredRisk.HIGH
            RiskLevel.MEDIUM.label, "중간", "medium" -> StoredRisk.MEDIUM
            RiskLevel.SAFE.label, "안전", "low", "safe" -> StoredRisk.SAFE
            else -> StoredRisk.UNKNOWN
        }
    }

    private fun parseSignals(rawSignals: String): ParsedSignals {
        val knownSignals = linkedSetOf<ScamSignal>()
        var hasUnknownSignal = false

        rawSignals.split(',')
            .map(::collapseWhitespace)
            .filter { it.isNotBlank() }
            .forEach { rawLabel ->
                val signal = canonicalSignal(rawLabel)
                if (signal == null) {
                    hasUnknownSignal = true
                } else {
                    knownSignals += signal
                }
            }

        val labels = buildList {
            ScamSignal.entries.forEach { signal ->
                if (signal in knownSignals) add(signal.label)
            }
            if (hasUnknownSignal) add(OTHER_SIGNAL_LABEL)
        }
        return ParsedSignals(labels = labels, knownSignals = knownSignals)
    }

    private fun canonicalSignal(rawLabel: String): ScamSignal? {
        ScamSignal.entries.firstOrNull { it.label.equals(rawLabel, ignoreCase = true) }
            ?.let { return it }

        val label = rawLabel.lowercase()
        return when {
            label.contains("번호") && listOf("변경", "바뀌", "바꿨").any(label::contains) ->
                ScamSignal.PHONE_NUMBER_CHANGE
            listOf("딥페이크", "음성 복제", "목소리 복제", "음성 합성", "목소리 합성").any(label::contains) ->
                ScamSignal.DEEPFAKE_VOICE
            label.contains("가족") && listOf("긴급", "목소리", "사칭", "사고").any(label::contains) ->
                ScamSignal.FAMILY_EMERGENCY
            listOf("화면공유", "화면 공유", "통화 유지", "통화유지").any(label::contains) ->
                ScamSignal.LIVE_CALL_CONTROL
            listOf("원격", "앱 설치", "어플 설치", "보안앱").any(label::contains) ->
                ScamSignal.APP_INSTALL
            listOf("기프티콘", "상품권 pin", "상품권 핀").any(label::contains) ->
                ScamSignal.GIFTICON_PIN
            listOf("상품권", "가상자산", "가상 자산", "코인").any(label::contains) ->
                ScamSignal.GIFT_CRYPTO
            listOf("비밀번호", "otp", "인증번호", "인증 정보", "계정 정보", "신분증").any(label::contains) ->
                ScamSignal.CREDENTIALS
            listOf("안전계좌", "안전 계좌", "명의도용", "명의 도용", "대포통장").any(label::contains) ->
                ScamSignal.SAFE_ACCOUNT_FRAUD
            listOf("긴급 송금", "송금 압박", "이체 압박", "입금 압박").any(label::contains) ->
                ScamSignal.URGENT_TRANSFER
            listOf("대환대출", "대환 대출", "보증료", "선입금").any(label::contains) ->
                ScamSignal.LOAN_REFINANCE_FEE
            listOf("택배", "청첩장", "부고").any(label::contains) ->
                ScamSignal.PARCEL_INVITATION_SMISHING
            listOf("링크", "스미싱", "url", "도메인").any(label::contains) ->
                ScamSignal.LINK
            listOf("해외 결제", "소액 결제", "결제 승인").any(label::contains) ->
                ScamSignal.OVERSEAS_PAYMENT_ALERT
            listOf("지원금", "환급", "대출 미끼").any(label::contains) ->
                ScamSignal.BAIT
            listOf("기관 사칭", "검찰 사칭", "경찰 사칭", "은행 사칭").any(label::contains) ->
                ScamSignal.IMPERSONATION
            listOf("비밀 유지", "비밀 압박").any(label::contains) ->
                ScamSignal.SECRECY
            else -> null
        }
    }

    private fun aggregateTopSignals(
        incidents: List<NormalizedIncident>
    ): List<IncidentBriefingSignal> {
        val counts = linkedMapOf<String, Int>()
        incidents.forEach { incident ->
            incident.signals.labels.distinct().forEach { label ->
                counts[label] = (counts[label] ?: 0) + 1
            }
        }
        return counts.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { it.value }
                    .thenBy { signalDisplayOrder(it.key) }
                    .thenBy { it.key }
            )
            .take(MAX_TOP_SIGNALS)
            .map { (label, count) -> IncidentBriefingSignal(label, count) }
    }

    private fun signalDisplayOrder(label: String): Int =
        ScamSignal.entries.indexOfFirst { it.label == label }.let { index ->
            if (index >= 0) index else Int.MAX_VALUE
        }

    private fun buildNextSteps(
        hasIncidents: Boolean,
        hasCurrentAnalysis: Boolean,
        knownSignals: Set<ScamSignal>,
        highestRisk: StoredRisk?
    ): List<IncidentBriefingNextStep> {
        val analysisAction = if (hasCurrentAnalysis) {
            IncidentBriefingNextStep(
                id = IncidentBriefingActionId.REPORT_PREP,
                label = "신고용 상담 준비 카드",
                explanation = "현재 기기 내 분석으로 개인정보를 가린 상담 준비 내용을 확인하세요.",
                recommended = false
            )
        } else {
            IncidentBriefingNextStep(
                id = IncidentBriefingActionId.NEW_ANALYSIS,
                label = "새 검사",
                explanation = "저장 기록만으로 원문 분석을 다시 만들지 않습니다. 새 의심 내용을 직접 붙여넣어 확인하세요.",
                recommended = false
            )
        }
        val baseActions = listOf(
            IncidentBriefingNextStep(
                id = IncidentBriefingActionId.SAFETY_FLOW_ROUTER,
                label = "안전 흐름 길잡이",
                explanation = "현재 상황과 가까운 가족 콜백·긴급 대응 경로를 직접 고르세요.",
                recommended = false
            ),
            IncidentBriefingNextStep(
                id = IncidentBriefingActionId.RESPONSE_CENTER,
                label = "긴급 대응 센터",
                explanation = "이미 돈·정보를 보냈거나 앱·원격 접근을 허용했다면 실제로 한 행동에 맞는 순서를 확인하세요.",
                recommended = false
            ),
            IncidentBriefingNextStep(
                id = IncidentBriefingActionId.EVIDENCE_KIT,
                label = "증거 보존 체크리스트",
                explanation = "메시지·통화 시각·거래·앱 정보를 지우기 전에 수동 보존 순서를 확인하세요.",
                recommended = false
            ),
            IncidentBriefingNextStep(
                id = IncidentBriefingActionId.OFFICIAL_HELP_DIRECTORY,
                label = "신고·상담 연락처",
                explanation = "상황에 맞는 공식 창구와 전화 전에 말할 내용을 확인하세요.",
                recommended = false
            ),
            analysisAction
        )
        val hasFamilySignal = knownSignals.any { it in familySignals }
        val hasUrgentSignal = knownSignals.any { it in urgentSignals } || highestRisk == StoredRisk.HIGH
        val hasLinkSignal = knownSignals.any { it in linkSignals }
        val analysisActionId = analysisAction.id
        val recommendedOrder = buildList {
            when {
                !hasIncidents -> {
                    add(analysisActionId)
                    add(IncidentBriefingActionId.SAFETY_FLOW_ROUTER)
                }
                hasFamilySignal -> {
                    add(IncidentBriefingActionId.SAFETY_FLOW_ROUTER)
                    if (hasUrgentSignal) {
                        add(IncidentBriefingActionId.RESPONSE_CENTER)
                        add(IncidentBriefingActionId.EVIDENCE_KIT)
                        add(IncidentBriefingActionId.OFFICIAL_HELP_DIRECTORY)
                    }
                    add(analysisActionId)
                }
                hasUrgentSignal -> {
                    add(IncidentBriefingActionId.RESPONSE_CENTER)
                    add(IncidentBriefingActionId.EVIDENCE_KIT)
                    add(IncidentBriefingActionId.OFFICIAL_HELP_DIRECTORY)
                    add(analysisActionId)
                }
                hasLinkSignal -> {
                    add(analysisActionId)
                    add(IncidentBriefingActionId.EVIDENCE_KIT)
                    add(IncidentBriefingActionId.OFFICIAL_HELP_DIRECTORY)
                    add(IncidentBriefingActionId.SAFETY_FLOW_ROUTER)
                }
                else -> {
                    add(analysisActionId)
                    add(IncidentBriefingActionId.SAFETY_FLOW_ROUTER)
                }
            }
        }.distinct()
        val actionById = baseActions.associateBy { it.id }

        return (recommendedOrder + baseActions.map { it.id })
            .distinct()
            .map { actionId ->
                val action = actionById.getValue(actionId)
                action.copy(recommended = actionId in recommendedOrder)
            }
    }

    private fun recommendTimelineAction(incident: NormalizedIncident): String = when {
        incident.signals.knownSignals.any { it in familySignals } ->
            "안전 흐름에서 가족 콜백 확인"
        incident.signals.knownSignals.any { it in urgentSignals } || incident.risk == StoredRisk.HIGH ->
            "긴급 대응 센터 확인"
        incident.signals.knownSignals.any { it in linkSignals } ->
            "증거 보존·공식 도움 확인"
        else -> "안전 흐름 길잡이 확인"
    }

    private fun safeDisplayTime(timestamp: Long, displayTime: (Long) -> String): String =
        runCatching { collapseWhitespace(displayTime(timestamp)) }
            .getOrDefault(UNKNOWN_TIME_LABEL)
            .ifBlank { UNKNOWN_TIME_LABEL }

    private fun buildFamilyShareSummary(
        totalCount: Int,
        riskCounts: IncidentBriefingRiskCounts,
        highestRiskLabel: String,
        topSignals: List<IncidentBriefingSignal>,
        nextSteps: List<IncidentBriefingNextStep>
    ): String = buildString {
        appendLine("[가족 AI 보이스피싱 방패] 사건 타임라인 요약")
        appendLine()
        appendLine("저장 사건: ${totalCount}건")
        appendLine(
            "위험도 집계: 높음 ${riskCounts.high}건 · 주의 ${riskCounts.medium}건 · " +
                "낮음 ${riskCounts.low}건 · 확인 필요 ${riskCounts.needsReview}건"
        )
        appendLine("가장 높은 위험도: $highestRiskLabel")
        appendLine()
        appendLine("주요 탐지 신호")
        if (topSignals.isEmpty()) {
            appendLine("- 저장된 탐지 신호 없음")
        } else {
            topSignals.forEach { signal ->
                appendLine("- ${signal.label} · ${signal.incidentCount}건")
            }
        }
        appendLine()
        appendLine("다음 확인 경로")
        nextSteps.forEach { action -> appendLine("- ${action.label}") }
        appendLine()
        appendLine("이 요약은 저장 기록의 집계와 앱 안의 확인 경로만 안내합니다. 공식 도움은 상담 경로 안내이며 실제 조치와 결과는 공식 기관과 금융회사에 확인해 주세요.")
        appendLine(
            "사건별 개인정보 가림 단서와 붙여넣은 원문, 전화번호·계좌번호·비밀번호·인증번호(OTP)·PIN·" +
                "링크·도메인·신분정보, 가족 안전단어·콜백 질문·연락처 이름과 번호는 포함하지 않았습니다."
        )
        append("앱이 문자·통화·연락처를 자동으로 읽거나 사건을 새로 저장한 결과가 아닙니다.")
    }.trim()

    private fun collapseWhitespace(value: String): String = value
        .replace(Regex("[\\s\\p{Z}]+"), " ")
        .trim()
}
