package com.mongmong.familyshield

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private enum class Screen {
        ONBOARDING, HOME, CHECKER, RESULT, LOG, PLAN, RESPONSE_CENTER, EVIDENCE, DRILL, SAMPLES,
        HELP_DIRECTORY, REPORT_PREP, CALLBACK_CARD, PRIVACY_CENTER, FLOW_ROUTER, TIMELINE_BRIEFING,
        ACCOUNT_LOCKDOWN, FAMILY_ALERT_MESSAGES, QUICK_STOP, RECOVERY_PLAN, TRUSTED_HELPER_HANDOFF,
        TRUSTED_HELPER_REVIEW, SAFE_CALL_SCRIPTS, RISK_SIGNAL_EXPLAINERS, FAMILY_READINESS_ROADMAP,
        EXPOSURE_IMPACT_TRIAGE, FOLLOW_UP_CHECK_IN_PLANNER, FAMILY_RESPONSE_ROLE_BOARD,
        FAMILY_CALM_DEBRIEF_AGENDA, SAFE_PASTE_PRIVACY_COACH, ACTIVE_CONTACT_EXIT_SCRIPTS,
        MONEY_TRANSFER_PAUSE_GATE, REMOTE_ACCESS_CUTOFF_COACH,
        SEPARATE_CHANNEL_VERIFICATION_COACH, VERIFICATION_OUTCOME_NEXT_STEPS_BOARD,
        RECONTACT_BOUNDARY_COACH, EVIDENCE_HANDOFF_PRIVACY_COACH,
        FAMILY_SAFE_WORD_RESET_COACH, VOICE_IMPERSONATION_REALITY_CHECK,
        NEW_NUMBER_QUARANTINE_COACH, CREDENTIAL_SECRET_FREEZE_COACH,
        OFFICIAL_PRESSURE_REALITY_CHECK_COACH, RECOVERY_PROMISE_REALITY_CHECK_COACH,
        GIFT_CARD_CODE_PAUSE_COACH, LINK_APP_INSTALL_QUARANTINE_COACH,
        CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH, BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH,
        DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH, TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH,
        SIM_NUMBER_PORT_OUT_PAUSE_COACH, ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH,
        SCREENSHOT_PRIVACY_GUARD_COACH, ACCESSIBILITY_COACH,
        POST_INCIDENT_REVIEW_CALENDAR
    }

    private val prefs by lazy { getSharedPreferences("family_shield", MODE_PRIVATE) }
    private val incidents by lazy { IncidentStore(this) }
    private val safetyPlan by lazy { SafetyPlanStore(this) }
    private val simpleMode by lazy { SimpleModePreference(this) }
    private var currentAnalysis: ScamAnalysis? = null
    private var callbackCardAnalysis: ScamAnalysis? = null
    private var pendingSharedText: String? = null
    private var selectedIncidentSituation: IncidentSituation? = null
    private var selectedEvidenceSituation: IncidentSituation? = null
    private var selectedSampleCategory: ScamSampleCategory? = null
    private var helpDirectoryReturnScreen = Screen.HOME

    private val defaultCallbackScript =
        "1. 전화를 끊고 저장된 가족 번호로 다시 겁니다.\n" +
            "2. 안전 확인 질문을 물어보고 답을 확인합니다.\n" +
            "3. 답이 어색하면 송금, 앱 설치, 인증번호 제공을 거부합니다.\n" +
            "4. 필요하면 112 또는 1394에 신고합니다."

    companion object {
        private const val KEY_PENDING_SHARED_TEXT = "pending_shared_text"
        private const val KEY_CONSENTED = "consented"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingSharedText = savedInstanceState?.getString(KEY_PENDING_SHARED_TEXT) ?: extractSharedText(intent)
        if (savedInstanceState == null && !pendingSharedText.isNullOrBlank()) {
            clearSharedIntent()
        }
        val consented = prefs.getBoolean(KEY_CONSENTED, false)
        val startScreen = when {
            !consented -> Screen.ONBOARDING
            pendingSharedText.isNullOrBlank() -> Screen.HOME
            else -> Screen.CHECKER
        }
        show(startScreen)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (!pendingSharedText.isNullOrBlank()) {
            outState.putString(KEY_PENDING_SHARED_TEXT, pendingSharedText)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val sharedText = extractSharedText(intent)
        if (sharedText.isNullOrBlank()) {
            pendingSharedText = null
            clearSharedIntent()
            show(if (prefs.getBoolean(KEY_CONSENTED, false)) Screen.HOME else Screen.ONBOARDING)
            return
        }
        pendingSharedText = sharedText
        clearSharedIntent()
        show(if (prefs.getBoolean(KEY_CONSENTED, false)) Screen.CHECKER else Screen.ONBOARDING)
    }

    private fun extractSharedText(intent: Intent?): String? {
        if (intent == null || intent.action != Intent.ACTION_SEND) return null
        val type = intent.type.orEmpty()
        if (!type.startsWith("text/")) return null
        return SharedTextParser.composeSharedText(
            text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT),
            subject = intent.getCharSequenceExtra(Intent.EXTRA_SUBJECT)
        )
    }

    private fun clearSharedIntent() {
        setIntent(Intent(this, MainActivity::class.java).apply { action = Intent.ACTION_MAIN })
    }

    private fun show(screen: Screen) {
        val content = vertical {
            setPadding(dp(20), dp(20), dp(20), dp(28))
            when (screen) {
                Screen.ONBOARDING -> onboarding()
                Screen.HOME -> home()
                Screen.CHECKER -> checker()
                Screen.RESULT -> result()
                Screen.LOG -> incidentLog()
                Screen.PLAN -> safetyPlanScreen()
                Screen.RESPONSE_CENTER -> responseCenter()
                Screen.EVIDENCE -> evidencePreservationScreen()
                Screen.DRILL -> drillScreen()
                Screen.SAMPLES -> sampleLibraryScreen()
                Screen.HELP_DIRECTORY -> helpDirectoryScreen()
                Screen.REPORT_PREP -> officialReportPrepScreen()
                Screen.CALLBACK_CARD -> familyCallbackCardScreen()
                Screen.PRIVACY_CENTER -> privacyCenterScreen()
                Screen.FLOW_ROUTER -> safetyFlowRouterScreen()
                Screen.TIMELINE_BRIEFING -> incidentTimelineBriefingScreen()
                Screen.ACCOUNT_LOCKDOWN -> accountLockdownChecklistScreen()
                Screen.FAMILY_ALERT_MESSAGES -> familyAlertMessagePackScreen()
                Screen.QUICK_STOP -> quickStopModeScreen()
                Screen.RECOVERY_PLAN -> recoveryPlan24hScreen()
                Screen.TRUSTED_HELPER_HANDOFF -> trustedHelperHandoffScreen()
                Screen.TRUSTED_HELPER_REVIEW -> trustedHelperReviewModeScreen()
                Screen.SAFE_CALL_SCRIPTS -> safeVerificationCallScriptsScreen()
                Screen.RISK_SIGNAL_EXPLAINERS -> riskSignalExplainerScreen()
                Screen.FAMILY_READINESS_ROADMAP -> familyReadinessRoadmapScreen()
                Screen.EXPOSURE_IMPACT_TRIAGE -> exposureImpactTriageScreen()
                Screen.FOLLOW_UP_CHECK_IN_PLANNER -> followUpCheckInPlannerScreen()
                Screen.FAMILY_RESPONSE_ROLE_BOARD -> familyResponseRoleBoardScreen()
                Screen.FAMILY_CALM_DEBRIEF_AGENDA -> familyCalmDebriefAgendaScreen()
                Screen.SAFE_PASTE_PRIVACY_COACH -> safePastePrivacyCoachScreen()
                Screen.ACTIVE_CONTACT_EXIT_SCRIPTS -> activeContactExitScriptsScreen()
                Screen.MONEY_TRANSFER_PAUSE_GATE -> moneyTransferPauseGateScreen()
                Screen.REMOTE_ACCESS_CUTOFF_COACH -> remoteAccessCutoffCoachScreen()
                Screen.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                    separateChannelVerificationCoachScreen()
                Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                    verificationOutcomeNextStepsBoardScreen()
                Screen.RECONTACT_BOUNDARY_COACH -> recontactBoundaryCoachScreen()
                Screen.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                    evidenceHandoffPrivacyCoachScreen()
                Screen.FAMILY_SAFE_WORD_RESET_COACH -> familySafeWordResetCoachScreen()
                Screen.VOICE_IMPERSONATION_REALITY_CHECK ->
                    voiceImpersonationRealityCheckCoachScreen()
                Screen.NEW_NUMBER_QUARANTINE_COACH -> newNumberQuarantineCoachScreen()
                Screen.CREDENTIAL_SECRET_FREEZE_COACH ->
                    credentialSecretFreezeCoachScreen()
                Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                    officialPressureRealityCheckCoachScreen()
                Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH ->
                    recoveryPromiseRealityCheckCoachScreen()
                Screen.GIFT_CARD_CODE_PAUSE_COACH -> giftCardCodePauseCoachScreen()
                Screen.LINK_APP_INSTALL_QUARANTINE_COACH ->
                    linkAppInstallQuarantineCoachScreen()
                Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH ->
                    cashWithdrawalCourierHandoffPauseCoachScreen()
                Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH ->
                    bankAtmStaffDisclosureScriptCoachScreen()
                Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH ->
                    doorstepVisitorHandoffRefusalCoachScreen()
                Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH ->
                    travelRouteMeetingPointPauseCoachScreen()
                Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH ->
                    simNumberPortOutPauseCoachScreen()
                Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH ->
                    idDocumentSelfieCapturePauseCoachScreen()
                Screen.SCREENSHOT_PRIVACY_GUARD_COACH -> screenshotPrivacyGuardCoachScreen()
                Screen.ACCESSIBILITY_COACH -> accessibilityCoachScreen()
                Screen.POST_INCIDENT_REVIEW_CALENDAR -> postIncidentReviewCalendarScreen()
            }
        }
        setContentView(ScrollView(this).apply {
            setBackgroundColor(0xFFF7F8FA.toInt())
            addView(content)
        })
    }

    private fun LinearLayout.postIncidentReviewCalendarScreen() {
        val calendar = PostIncidentReviewCalendarBuilder.build(
            consent = prefs.getBoolean(KEY_CONSENTED, false),
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count(),
            lastIncidentDaysAgo = lastIncidentDaysAgo()
        )
        val recommendedIds = calendar.recommendedAnchorIds.toSet()

        title(calendar.title)
        subtitle(calendar.intro)
        section("자동 예약 없음", calendar.manualCalendarNote)
        section(
            "${calendar.modeLabel} · ${calendar.recommendationTitle}",
            buildString {
                append(calendar.recommendationExplanation)
                if (calendar.calendarSkipped) {
                    append("\n\n먼저 볼 안내\n")
                    append("• ${calendar.firstTimeMessage.orEmpty()}")
                } else {
                    append("\n\n먼저 직접 표시할 점검 시점\n")
                    append(
                        calendar.recommendedAnchors.joinToString("\n") { anchor ->
                            "• ${anchor.dayLabel} · ${anchor.title} · ${anchor.emphasis.label}"
                        }
                    )
                }
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    calendar.recommendedActions.joinToString("\n") { action -> "• ${action.label}" }
                )
            }
        )
        calendar.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openPostIncidentReviewCalendarAction(action) }
            } else {
                secondaryButton(action.label) { openPostIncidentReviewCalendarAction(action) }
            }
        }

        section(
            "전체 점검 시점",
            "여덟 시점은 항상 같은 순서로 한 번씩 표시됩니다. 이 앱은 날짜를 저장하거나 달력 앱에 " +
                "일정을 쓰지 않고 알림·알람을 만들지 않으므로, 각 시점은 가족이 직접 자기 달력에 " +
                "옮겨 적고 그날 직접 열어 보세요."
        )
        calendar.anchors.forEach { anchor ->
            postIncidentReviewAnchorCard(anchor, recommended = anchor.id in recommendedIds)
        }

        section(
            "개인정보·안전 안내",
            calendar.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 점검 시점 공유") {
            sharePostIncidentReviewCalendar(calendar.familyShareSummary)
        }
        secondaryButton("며칠 뒤 다시 확인할 것") { show(Screen.FOLLOW_UP_CHECK_IN_PLANNER) }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.postIncidentReviewAnchorCard(
        anchor: PostIncidentReviewAnchor,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(anchor.displayOrder)
            append(". ")
            append(anchor.dayLabel)
            append(" · ")
            append(anchor.title)
            append(" · ")
            append(anchor.emphasis.label)
        }
        section(
            heading,
            buildString {
                append("왜 이 시점인가요?\n")
                append(anchor.whyItMatters)
                append("\n\n직접 다시 살펴볼 것\n")
                anchor.manualRecheckItems.forEach { item ->
                    append("• ").append(item).append('\n')
                }
                append("\n가족과 상의할 질문 3개\n")
                anchor.familyDiscussionQuestions.forEachIndexed { index, question ->
                    append(index + 1).append(". ").append(question).append('\n')
                }
                append("\n이 일정 직접 표시\n")
                append(anchor.markInOwnCalendarInstruction)
            }.trim()
        )
        if (recommended) {
            primaryButton(anchor.inAppViewLabel) {
                openPostIncidentReviewCalendarAction(anchor.primaryAction)
            }
        } else {
            secondaryButton(anchor.inAppViewLabel) {
                openPostIncidentReviewCalendarAction(anchor.primaryAction)
            }
        }
        anchor.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openPostIncidentReviewCalendarAction(action) }
        }
    }

    private fun openPostIncidentReviewCalendarAction(action: PostIncidentReviewAction) {
        when (action.id) {
            PostIncidentReviewActionId.ONBOARDING -> show(Screen.ONBOARDING)
            PostIncidentReviewActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            PostIncidentReviewActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            PostIncidentReviewActionId.SAFETY_PLAN -> show(Screen.PLAN)
            PostIncidentReviewActionId.RECONTACT_BOUNDARY_COACH ->
                show(Screen.RECONTACT_BOUNDARY_COACH)
            PostIncidentReviewActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            PostIncidentReviewActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            PostIncidentReviewActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            PostIncidentReviewActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            PostIncidentReviewActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            PostIncidentReviewActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            PostIncidentReviewActionId.INCIDENT_TIMELINE_BRIEFING ->
                show(Screen.TIMELINE_BRIEFING)
            PostIncidentReviewActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            PostIncidentReviewActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.POST_INCIDENT_REVIEW_CALENDAR)
            PostIncidentReviewActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            PostIncidentReviewActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            PostIncidentReviewActionId.HOME -> show(Screen.HOME)
        }
    }

    private fun sharePostIncidentReviewCalendar(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(
            Intent.createChooser(shareIntent, "가족에게 점검 시점 공유")
        )
    }

    private fun lastIncidentDaysAgo(): Int? {
        val latestTimestamp = incidents.load()
            .maxOfOrNull { incident -> incident.timestamp }
            ?: return null
        val elapsedMillis = System.currentTimeMillis() - latestTimestamp
        if (elapsedMillis <= 0L) return 0
        return (elapsedMillis / 86_400_000L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    private fun LinearLayout.idDocumentSelfieCapturePauseCoachScreen() {
        val coach = IdDocumentSelfieCapturePauseCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 신분증·셀피 촬영 압박 멈춤 문구\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.idDocumentSelfiePressureSituation}\n" +
                            card.valueFreePauseScript
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) {
                    openIdDocumentSelfieCapturePauseAction(action)
                }
            } else {
                secondaryButton(action.label) {
                    openIdDocumentSelfieCapturePauseAction(action)
                }
            }
        }

        section(
            "전체 수동 신분증·셀피 촬영 압박 멈춤 문구 카드",
            "카드는 항상 같은 순서입니다. 신분증·문서를 촬영·스캔·전송하거나 " +
                "셀피·영상을 찍고 얼굴·계정 정보를 말하기 전에 값 없는 멈춤 문장을 " +
                "직접 고르세요. 앱은 신분증·얼굴·사진·영상·파일·계정 값을 받지 않으며 " +
                "선택·읽음·확인 상태도 남기지 않습니다."
        )
        coach.cards.forEach { card ->
            idDocumentSelfieCapturePauseCard(
                card,
                recommended = card.id in recommendedIds
            )
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 신분증·셀피 촬영 멈춤 안내 공유") {
            shareIdDocumentSelfieCapturePauseCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("비밀값 전달 전 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("링크·앱 설치 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("원격 접근 중단 코치") {
            show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.idDocumentSelfieCapturePauseCard(
        card: IdDocumentSelfieCapturePauseCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("신분증·셀피 촬영 압박 상황\n")
                append(card.idDocumentSelfiePressureSituation)
                append("\n\n값 없이 직접 읽을 멈춤·거절 문장\n")
                append(card.valueFreePauseScript)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n촬영·스캔·업로드·읽기·얼굴·계정·말하기·공유·첨부·기록 경계\n")
                card.doNotPhotographScanUploadReadIdFaceAccountSayShareAttachOrRecordBoundaries
                    .forEach { boundary ->
                        append("• ").append(boundary).append('\n')
                    }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openIdDocumentSelfieCapturePauseAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openIdDocumentSelfieCapturePauseAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) {
                openIdDocumentSelfieCapturePauseAction(action)
            }
        }
    }

    private fun openIdDocumentSelfieCapturePauseAction(
        action: IdDocumentSelfieCapturePauseAction
    ) {
        when (action.id) {
            IdDocumentSelfieCapturePauseActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            IdDocumentSelfieCapturePauseActionId.CHECKER -> show(Screen.CHECKER)
            IdDocumentSelfieCapturePauseActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            IdDocumentSelfieCapturePauseActionId.SAFETY_PLAN -> show(Screen.PLAN)
            IdDocumentSelfieCapturePauseActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            IdDocumentSelfieCapturePauseActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            IdDocumentSelfieCapturePauseActionId.SAFE_PASTE_PRIVACY_COACH ->
                show(Screen.SAFE_PASTE_PRIVACY_COACH)
            IdDocumentSelfieCapturePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            IdDocumentSelfieCapturePauseActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            IdDocumentSelfieCapturePauseActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
            IdDocumentSelfieCapturePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            IdDocumentSelfieCapturePauseActionId.SAFE_CALL_SCRIPTS ->
                show(Screen.SAFE_CALL_SCRIPTS)
            IdDocumentSelfieCapturePauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
            IdDocumentSelfieCapturePauseActionId.NEW_NUMBER_QUARANTINE_COACH ->
                show(Screen.NEW_NUMBER_QUARANTINE_COACH)
            IdDocumentSelfieCapturePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
            IdDocumentSelfieCapturePauseActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            IdDocumentSelfieCapturePauseActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            IdDocumentSelfieCapturePauseActionId.RECONTACT_BOUNDARY_COACH ->
                show(Screen.RECONTACT_BOUNDARY_COACH)
            IdDocumentSelfieCapturePauseActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            IdDocumentSelfieCapturePauseActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            IdDocumentSelfieCapturePauseActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            IdDocumentSelfieCapturePauseActionId.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH ->
                show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
            IdDocumentSelfieCapturePauseActionId.SIM_NUMBER_PORT_OUT_PAUSE_COACH ->
                show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
            IdDocumentSelfieCapturePauseActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            IdDocumentSelfieCapturePauseActionId.GIFT_CARD_CODE_PAUSE_COACH ->
                show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
            IdDocumentSelfieCapturePauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH ->
                show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
            IdDocumentSelfieCapturePauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
            IdDocumentSelfieCapturePauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH ->
                show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
            IdDocumentSelfieCapturePauseActionId.ACCOUNT_LOCKDOWN ->
                show(Screen.ACCOUNT_LOCKDOWN)
            IdDocumentSelfieCapturePauseActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            IdDocumentSelfieCapturePauseActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            IdDocumentSelfieCapturePauseActionId.PRIVACY_CENTER ->
                show(Screen.PRIVACY_CENTER)
            IdDocumentSelfieCapturePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            IdDocumentSelfieCapturePauseActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            IdDocumentSelfieCapturePauseActionId.INCIDENT_TIMELINE_BRIEFING ->
                show(Screen.TIMELINE_BRIEFING)
            IdDocumentSelfieCapturePauseActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
            IdDocumentSelfieCapturePauseActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            IdDocumentSelfieCapturePauseActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            IdDocumentSelfieCapturePauseActionId.SAFETY_FLOW_ROUTER ->
                show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareIdDocumentSelfieCapturePauseCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(
            Intent.createChooser(shareIntent, "가족에게 신분증·셀피 촬영 멈춤 안내 공유")
        )
    }

    private fun LinearLayout.screenshotPrivacyGuardCoachScreen() {
        val coach = ScreenshotPrivacyGuardCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 화면캡처 개인정보 문구\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.screenshotPressureSituation}\n" +
                            card.valueFreePauseScript
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) {
                    openScreenshotPrivacyGuardAction(action)
                }
            } else {
                secondaryButton(action.label) {
                    openScreenshotPrivacyGuardAction(action)
                }
            }
        }

        section(
            "전체 수동 화면캡처 개인정보 카드",
            "카드는 항상 같은 순서입니다. 이 앱은 화면캡처를 감지하거나 막지 않으므로, " +
                "캡처하기 전이나 후에 값 없는 안내 문장을 직접 고르세요. 앱은 캡처본·이미지·" +
                "갤러리·클라우드 백업 상태를 읽지 않으며 선택·읽음·확인 상태도 남기지 않습니다."
        )
        coach.cards.forEach { card ->
            screenshotPrivacyGuardCard(
                card,
                recommended = card.id in recommendedIds
            )
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 화면캡처 개인정보 안내 공유") {
            shareScreenshotPrivacyGuardCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        secondaryButton("붙여넣기 전 개인정보 가리기") {
            show(Screen.SAFE_PASTE_PRIVACY_COACH)
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.screenshotPrivacyGuardCard(
        card: ScreenshotPrivacyGuardCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("화면캡처 압박 상황\n")
                append(card.screenshotPressureSituation)
                append("\n\n값 없이 직접 읽을 안내 문장\n")
                append(card.valueFreePauseScript)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n감지·차단·저장·업로드·자동 공유 경계\n")
                card.doNotCaptureDetectStoreUploadOrAutoShareBoundaries
                    .forEach { boundary ->
                        append("• ").append(boundary).append('\n')
                    }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openScreenshotPrivacyGuardAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openScreenshotPrivacyGuardAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) {
                openScreenshotPrivacyGuardAction(action)
            }
        }
    }

    private fun openScreenshotPrivacyGuardAction(action: ScreenshotPrivacyGuardAction) {
        when (action.id) {
            ScreenshotPrivacyGuardActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            ScreenshotPrivacyGuardActionId.CHECKER -> show(Screen.CHECKER)
            ScreenshotPrivacyGuardActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            ScreenshotPrivacyGuardActionId.SAFETY_PLAN -> show(Screen.PLAN)
            ScreenshotPrivacyGuardActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            ScreenshotPrivacyGuardActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            ScreenshotPrivacyGuardActionId.SAFE_PASTE_PRIVACY_COACH ->
                show(Screen.SAFE_PASTE_PRIVACY_COACH)
            ScreenshotPrivacyGuardActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
            ScreenshotPrivacyGuardActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            ScreenshotPrivacyGuardActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            ScreenshotPrivacyGuardActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            ScreenshotPrivacyGuardActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            ScreenshotPrivacyGuardActionId.INCIDENT_TIMELINE_BRIEFING ->
                show(Screen.TIMELINE_BRIEFING)
            ScreenshotPrivacyGuardActionId.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH ->
                show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
            ScreenshotPrivacyGuardActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            ScreenshotPrivacyGuardActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            ScreenshotPrivacyGuardActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            ScreenshotPrivacyGuardActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            ScreenshotPrivacyGuardActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.SCREENSHOT_PRIVACY_GUARD_COACH)
            ScreenshotPrivacyGuardActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            ScreenshotPrivacyGuardActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            ScreenshotPrivacyGuardActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            ScreenshotPrivacyGuardActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareScreenshotPrivacyGuardCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(
            Intent.createChooser(shareIntent, "가족에게 화면캡처 개인정보 안내 공유")
        )
    }

    private fun LinearLayout.accessibilityCoachScreen() {
        val coach = AccessibilityCoachBuilder.build(
            consent = prefs.getBoolean(KEY_CONSENTED, false),
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        section(
            "지금 글자 크게 보기",
            "이 화면은 글자 크기를 직접 바꾸지 않습니다. 안드로이드 설정 → 디스플레이 → 글자 " +
                "크기에서 슬라이더를 밀면 이 화면을 포함한 모든 앱 글자가 함께 커집니다. 설정을 " +
                "마친 뒤 이 화면으로 돌아와 커졌는지 직접 확인하세요."
        )
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 볼 추천 접근성 카드\n")
                append(
                    coach.recommendedCards.joinToString("\n") { card -> "• ${card.title}" }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action -> "• ${action.label}" }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openAccessibilityCoachAction(action) }
            } else {
                secondaryButton(action.label) { openAccessibilityCoachAction(action) }
            }
        }

        section(
            "전체 수동 접근성 카드",
            "카드는 항상 같은 순서입니다. 이 앱은 시스템 설정을 대신 켜지 않으므로, 이 앱에서 " +
                "직접 되는 일과 안드로이드 시스템 설정에서 해야 하는 일을 나눠 확인한 뒤 필요한 " +
                "기존 앱 경로만 직접 고르세요."
        )
        coach.cards.forEach { card ->
            accessibilityCoachCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 접근성 안내 공유") {
            shareAccessibilityCoach(coach.familyShareSummary)
        }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.accessibilityCoachCard(
        card: AccessibilityCoachCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n이 앱에서 직접 해주는 것\n")
                if (card.inAppSteps.isEmpty()) {
                    append("• 없음 (이 카드는 안드로이드 시스템 설정으로만 안내합니다)")
                } else {
                    card.inAppSteps.forEach { step -> append("• ").append(step).append('\n') }
                }
                append("\n안드로이드 시스템 설정에서 해야 하는 것\n")
                if (card.systemSettingSteps.isEmpty()) {
                    append("• 없음 (이 카드는 이 앱 안 화면으로만 안내합니다)")
                } else {
                    card.systemSettingSteps.forEachIndexed { index, step ->
                        append(index + 1).append(". ").append(step).append('\n')
                    }
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openAccessibilityCoachAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openAccessibilityCoachAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openAccessibilityCoachAction(action) }
        }
    }

    private fun openAccessibilityCoachAction(action: AccessibilityCoachAction) {
        when (action.id) {
            AccessibilityCoachActionId.ONBOARDING -> show(Screen.ONBOARDING)
            AccessibilityCoachActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            AccessibilityCoachActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            AccessibilityCoachActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            AccessibilityCoachActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            AccessibilityCoachActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            AccessibilityCoachActionId.SAFETY_PLAN -> show(Screen.PLAN)
            AccessibilityCoachActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            AccessibilityCoachActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.ACCESSIBILITY_COACH)
            AccessibilityCoachActionId.CHECKER -> show(Screen.CHECKER)
            AccessibilityCoachActionId.HOME -> show(Screen.HOME)
        }
    }

    private fun shareAccessibilityCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(
            Intent.createChooser(shareIntent, "가족에게 접근성 안내 공유")
        )
    }

    private fun LinearLayout.simNumberPortOutPauseCoachScreen() {
        val coach = SimNumberPortOutPauseCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("SIM·번호 보호 명목의 신분증·셀피 요구 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 SIM·번호이동 압박 멈춤 문구\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.simNumberPortPressureSituation}\n" +
                            card.valueFreePauseScript
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) {
                    openSimNumberPortOutPauseAction(action)
                }
            } else {
                secondaryButton(action.label) {
                    openSimNumberPortOutPauseAction(action)
                }
            }
        }

        section(
            "전체 수동 SIM·번호이동 압박 멈춤 문구 카드",
            "카드는 항상 같은 순서입니다. 통신사·유심·번호이동을 내세운 요청에 " +
                "코드나 계정·기기 정보를 말하거나 입력하기 전에 값 없는 멈춤 문장을 " +
                "직접 고르세요. 앱은 번호·SIM·계정·코드·사건 값을 받지 않으며 " +
                "선택·읽음·확인 상태도 남기지 않습니다."
        )
        coach.cards.forEach { card ->
            simNumberPortOutPauseCard(
                card,
                recommended = card.id in recommendedIds
            )
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 SIM·번호이동 멈춤 안내 공유") {
            shareSimNumberPortOutPauseCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("비밀값 전달 전 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("새 번호·새 프로필 격리") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("별도 경로 확인") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        secondaryButton("계정·기기 잠금 체크리스트") {
            show(Screen.ACCOUNT_LOCKDOWN)
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.simNumberPortOutPauseCard(
        card: SimNumberPortOutPauseCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("SIM·번호이동 압박 상황\n")
                append(card.simNumberPortPressureSituation)
                append("\n\n값 없이 직접 읽을 멈춤·거절 문장\n")
                append(card.valueFreePauseScript)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n코드·번호이동·재발급·통화·문자·연락처·계정·기기·말하기·공유·첨부·기록 경계\n")
                card.doNotShareCodePortReissueCallSmsContactAccountDeviceSayShareAttachOrRecordBoundaries
                    .forEach { boundary ->
                        append("• ").append(boundary).append('\n')
                    }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openSimNumberPortOutPauseAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openSimNumberPortOutPauseAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) {
                openSimNumberPortOutPauseAction(action)
            }
        }
    }

    private fun openSimNumberPortOutPauseAction(
        action: SimNumberPortOutPauseAction
    ) {
        when (action.id) {
            SimNumberPortOutPauseActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            SimNumberPortOutPauseActionId.CHECKER -> show(Screen.CHECKER)
            SimNumberPortOutPauseActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            SimNumberPortOutPauseActionId.SAFETY_PLAN -> show(Screen.PLAN)
            SimNumberPortOutPauseActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            SimNumberPortOutPauseActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            SimNumberPortOutPauseActionId.SAFE_PASTE_PRIVACY_COACH ->
                show(Screen.SAFE_PASTE_PRIVACY_COACH)
            SimNumberPortOutPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            SimNumberPortOutPauseActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            SimNumberPortOutPauseActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
            SimNumberPortOutPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            SimNumberPortOutPauseActionId.SAFE_CALL_SCRIPTS ->
                show(Screen.SAFE_CALL_SCRIPTS)
            SimNumberPortOutPauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
            SimNumberPortOutPauseActionId.NEW_NUMBER_QUARANTINE_COACH ->
                show(Screen.NEW_NUMBER_QUARANTINE_COACH)
            SimNumberPortOutPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
            SimNumberPortOutPauseActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            SimNumberPortOutPauseActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            SimNumberPortOutPauseActionId.RECONTACT_BOUNDARY_COACH ->
                show(Screen.RECONTACT_BOUNDARY_COACH)
            SimNumberPortOutPauseActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            SimNumberPortOutPauseActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            SimNumberPortOutPauseActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            SimNumberPortOutPauseActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            SimNumberPortOutPauseActionId.GIFT_CARD_CODE_PAUSE_COACH ->
                show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
            SimNumberPortOutPauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH ->
                show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
            SimNumberPortOutPauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
            SimNumberPortOutPauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH ->
                show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
            SimNumberPortOutPauseActionId.ACCOUNT_LOCKDOWN ->
                show(Screen.ACCOUNT_LOCKDOWN)
            SimNumberPortOutPauseActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            SimNumberPortOutPauseActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            SimNumberPortOutPauseActionId.PRIVACY_CENTER ->
                show(Screen.PRIVACY_CENTER)
            SimNumberPortOutPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            SimNumberPortOutPauseActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            SimNumberPortOutPauseActionId.INCIDENT_TIMELINE_BRIEFING ->
                show(Screen.TIMELINE_BRIEFING)
            SimNumberPortOutPauseActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
            SimNumberPortOutPauseActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            SimNumberPortOutPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            SimNumberPortOutPauseActionId.SAFETY_FLOW_ROUTER ->
                show(Screen.FLOW_ROUTER)
            SimNumberPortOutPauseActionId.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH ->
                show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
    }

    private fun shareSimNumberPortOutPauseCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 SIM·번호이동 멈춤 안내 공유"))
    }

    private fun LinearLayout.travelRouteMeetingPointPauseCoachScreen() {
        val coach = TravelRouteMeetingPointPauseCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("이동·만남 중 신분증·셀피 촬영·전송 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 이동·만남 지시 멈춤 문구\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.movementMeetingPressureSituation}\n" +
                            card.valueFreePauseScript
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) {
                    openTravelRouteMeetingPointPauseAction(action)
                }
            } else {
                secondaryButton(action.label) {
                    openTravelRouteMeetingPointPauseAction(action)
                }
            }
        }

        section(
            "전체 수동 이동·만남 지시 멈춤 문구 카드",
            "카드는 항상 같은 순서입니다. 출발하거나 누군가를 만나거나 현재 위치·" +
                "목적지·경로·교통수단·도착 시간을 말하기 전에 값 없는 멈춤 문장을 " +
                "직접 고르세요. 앱은 위치·주소·이동·만남·사건 값을 받지 않으며 " +
                "선택·읽음·확인 상태도 남기지 않습니다."
        )
        coach.cards.forEach { card ->
            travelRouteMeetingPointPauseCard(
                card,
                recommended = card.id in recommendedIds
            )
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 이동·만남 지시 멈춤 안내 공유") {
            shareTravelRouteMeetingPointPauseCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("현금 인출·직접 전달 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("현재 연락을 끝내는 문장") {
            show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
        }
        secondaryButton("별도 경로 확인") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.travelRouteMeetingPointPauseCard(
        card: TravelRouteMeetingPointPauseCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("이동·만남 압박 상황\n")
                append(card.movementMeetingPressureSituation)
                append("\n\n값 없이 직접 읽을 멈춤·거절 문장\n")
                append(card.valueFreePauseScript)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n이동·길 안내·위치 공유·만남·전달·말하기·공유·첨부·기록 경계\n")
                card.doNotTravelNavigateShareLocationMeetHandoffSayShareAttachOrRecordBoundaries
                    .forEach { boundary ->
                        append("• ").append(boundary).append('\n')
                    }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openTravelRouteMeetingPointPauseAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openTravelRouteMeetingPointPauseAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) {
                openTravelRouteMeetingPointPauseAction(action)
            }
        }
    }

    private fun openTravelRouteMeetingPointPauseAction(
        action: TravelRouteMeetingPointPauseAction
    ) {
        when (action.id) {
            TravelRouteMeetingPointPauseActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            TravelRouteMeetingPointPauseActionId.CHECKER -> show(Screen.CHECKER)
            TravelRouteMeetingPointPauseActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            TravelRouteMeetingPointPauseActionId.SAFETY_PLAN -> show(Screen.PLAN)
            TravelRouteMeetingPointPauseActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            TravelRouteMeetingPointPauseActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            TravelRouteMeetingPointPauseActionId.SAFE_PASTE_PRIVACY_COACH ->
                show(Screen.SAFE_PASTE_PRIVACY_COACH)
            TravelRouteMeetingPointPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            TravelRouteMeetingPointPauseActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            TravelRouteMeetingPointPauseActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
            TravelRouteMeetingPointPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            TravelRouteMeetingPointPauseActionId.SAFE_CALL_SCRIPTS ->
                show(Screen.SAFE_CALL_SCRIPTS)
            TravelRouteMeetingPointPauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
            TravelRouteMeetingPointPauseActionId.NEW_NUMBER_QUARANTINE_COACH ->
                show(Screen.NEW_NUMBER_QUARANTINE_COACH)
            TravelRouteMeetingPointPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
            TravelRouteMeetingPointPauseActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            TravelRouteMeetingPointPauseActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            TravelRouteMeetingPointPauseActionId.RECONTACT_BOUNDARY_COACH ->
                show(Screen.RECONTACT_BOUNDARY_COACH)
            TravelRouteMeetingPointPauseActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            TravelRouteMeetingPointPauseActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            TravelRouteMeetingPointPauseActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            TravelRouteMeetingPointPauseActionId.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH ->
                show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
            TravelRouteMeetingPointPauseActionId.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH ->
                show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
            TravelRouteMeetingPointPauseActionId.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH ->
                show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
            TravelRouteMeetingPointPauseActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            TravelRouteMeetingPointPauseActionId.GIFT_CARD_CODE_PAUSE_COACH ->
                show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
            TravelRouteMeetingPointPauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH ->
                show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
            TravelRouteMeetingPointPauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
            TravelRouteMeetingPointPauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH ->
                show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
            TravelRouteMeetingPointPauseActionId.ACCOUNT_LOCKDOWN ->
                show(Screen.ACCOUNT_LOCKDOWN)
            TravelRouteMeetingPointPauseActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            TravelRouteMeetingPointPauseActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            TravelRouteMeetingPointPauseActionId.PRIVACY_CENTER ->
                show(Screen.PRIVACY_CENTER)
            TravelRouteMeetingPointPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            TravelRouteMeetingPointPauseActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            TravelRouteMeetingPointPauseActionId.INCIDENT_TIMELINE_BRIEFING ->
                show(Screen.TIMELINE_BRIEFING)
            TravelRouteMeetingPointPauseActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
            TravelRouteMeetingPointPauseActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            TravelRouteMeetingPointPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            TravelRouteMeetingPointPauseActionId.SAFETY_FLOW_ROUTER ->
                show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareTravelRouteMeetingPointPauseCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 이동·만남 지시 멈춤 안내 공유"))
    }

    private fun LinearLayout.doorstepVisitorHandoffRefusalCoachScreen() {
        val coach = DoorstepVisitorHandoffRefusalCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("문 앞의 신분증·서류 촬영·전달 요구 거절") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 문 앞 방문·직접 전달 거절 문구\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.doorstepHandoffPressureSituation}\n" +
                            card.valueFreeRefusalScript
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) {
                    openDoorstepVisitorHandoffRefusalAction(action)
                }
            } else {
                secondaryButton(action.label) {
                    openDoorstepVisitorHandoffRefusalAction(action)
                }
            }
        }

        section(
            "전체 수동 문 앞 방문·직접 전달 거절 문구 카드",
            "카드는 항상 같은 순서입니다. 문을 열거나 사람을 들이거나 현금·카드·통장·" +
                "휴대폰·서류·봉투를 건네기 전에 값 없는 거절 문장을 직접 고르세요. " +
                "앱은 주소·방문자·금액·사건·물품 값을 받지 않으며 선택·읽음·확인 " +
                "상태도 남기지 않습니다."
        )
        coach.cards.forEach { card ->
            doorstepVisitorHandoffRefusalCard(
                card,
                recommended = card.id in recommendedIds
            )
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 문 앞 방문·직접 전달 거절 안내 공유") {
            shareDoorstepVisitorHandoffRefusalCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("현금 인출·직접 전달 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("현재 연락을 끝내는 문장") {
            show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
        }
        secondaryButton("별도 경로 확인") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.doorstepVisitorHandoffRefusalCard(
        card: DoorstepVisitorHandoffRefusalCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("문 앞 방문·직접 전달 압박 상황\n")
                append(card.doorstepHandoffPressureSituation)
                append("\n\n값 없이 직접 읽을 거절 문장\n")
                append(card.valueFreeRefusalScript)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n열기·들임·전달·말하기·공유·첨부·기록 경계\n")
                card.doNotOpenLetInHandoffSayShareAttachOrRecordBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openDoorstepVisitorHandoffRefusalAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openDoorstepVisitorHandoffRefusalAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) {
                openDoorstepVisitorHandoffRefusalAction(action)
            }
        }
    }

    private fun openDoorstepVisitorHandoffRefusalAction(
        action: DoorstepVisitorHandoffRefusalAction
    ) {
        when (action.id) {
            DoorstepVisitorHandoffRefusalActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            DoorstepVisitorHandoffRefusalActionId.CHECKER -> show(Screen.CHECKER)
            DoorstepVisitorHandoffRefusalActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            DoorstepVisitorHandoffRefusalActionId.SAFETY_PLAN -> show(Screen.PLAN)
            DoorstepVisitorHandoffRefusalActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            DoorstepVisitorHandoffRefusalActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            DoorstepVisitorHandoffRefusalActionId.SAFE_PASTE_PRIVACY_COACH ->
                show(Screen.SAFE_PASTE_PRIVACY_COACH)
            DoorstepVisitorHandoffRefusalActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            DoorstepVisitorHandoffRefusalActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            DoorstepVisitorHandoffRefusalActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
            DoorstepVisitorHandoffRefusalActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            DoorstepVisitorHandoffRefusalActionId.SAFE_CALL_SCRIPTS ->
                show(Screen.SAFE_CALL_SCRIPTS)
            DoorstepVisitorHandoffRefusalActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
            DoorstepVisitorHandoffRefusalActionId.NEW_NUMBER_QUARANTINE_COACH ->
                show(Screen.NEW_NUMBER_QUARANTINE_COACH)
            DoorstepVisitorHandoffRefusalActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
            DoorstepVisitorHandoffRefusalActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            DoorstepVisitorHandoffRefusalActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            DoorstepVisitorHandoffRefusalActionId.RECONTACT_BOUNDARY_COACH ->
                show(Screen.RECONTACT_BOUNDARY_COACH)
            DoorstepVisitorHandoffRefusalActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            DoorstepVisitorHandoffRefusalActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            DoorstepVisitorHandoffRefusalActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            DoorstepVisitorHandoffRefusalActionId.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH ->
                show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
            DoorstepVisitorHandoffRefusalActionId.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH ->
                show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
            DoorstepVisitorHandoffRefusalActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            DoorstepVisitorHandoffRefusalActionId.GIFT_CARD_CODE_PAUSE_COACH ->
                show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
            DoorstepVisitorHandoffRefusalActionId.LINK_APP_INSTALL_QUARANTINE_COACH ->
                show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
            DoorstepVisitorHandoffRefusalActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
            DoorstepVisitorHandoffRefusalActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH ->
                show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
            DoorstepVisitorHandoffRefusalActionId.ACCOUNT_LOCKDOWN ->
                show(Screen.ACCOUNT_LOCKDOWN)
            DoorstepVisitorHandoffRefusalActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            DoorstepVisitorHandoffRefusalActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            DoorstepVisitorHandoffRefusalActionId.PRIVACY_CENTER ->
                show(Screen.PRIVACY_CENTER)
            DoorstepVisitorHandoffRefusalActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            DoorstepVisitorHandoffRefusalActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            DoorstepVisitorHandoffRefusalActionId.INCIDENT_TIMELINE_BRIEFING ->
                show(Screen.TIMELINE_BRIEFING)
            DoorstepVisitorHandoffRefusalActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
            DoorstepVisitorHandoffRefusalActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            DoorstepVisitorHandoffRefusalActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            DoorstepVisitorHandoffRefusalActionId.SAFETY_FLOW_ROUTER ->
                show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareDoorstepVisitorHandoffRefusalCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 문 앞 방문·직접 전달 거절 안내 공유"))
    }

    private fun LinearLayout.bankAtmStaffDisclosureScriptCoachScreen() {
        val coach = BankAtmStaffDisclosureScriptCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 지시와 신분증·셀피 제출 압박 분리") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 은행·ATM 직원 공개 문구\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.staffDisclosureSituation}\n" +
                            card.valueFreeScript
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) {
                    openBankAtmStaffDisclosureScriptAction(action)
                }
            } else {
                secondaryButton(action.label) {
                    openBankAtmStaffDisclosureScriptAction(action)
                }
            }
        }

        section(
            "전체 수동 은행·ATM 직원 공개 문구 카드",
            "카드는 항상 같은 순서입니다. 가까운 직원이나 도우미에게 직접 읽을 짧은 문장을 " +
                "고르되 금액·계좌·카드·사건·전화·위치·경로·인물 값은 이 화면에 " +
                "넣지 마세요. 앱은 값 없는 문장과 기존 앱 경로만 보여 주며 선택·읽음·확인 " +
                "상태도 남기지 않습니다."
        )
        coach.cards.forEach { card ->
            bankAtmStaffDisclosureScriptCard(
                card,
                recommended = card.id in recommendedIds
            )
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 은행·ATM 직원 공개 문구 안내 공유") {
            shareBankAtmStaffDisclosureScriptCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("현금 인출·직접 전달 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("송금 전 멈춤 확인") { show(Screen.MONEY_TRANSFER_PAUSE_GATE) }
        secondaryButton("기관 사칭·압박 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("현재 연락을 끝내는 문장") {
            show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
        }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.bankAtmStaffDisclosureScriptCard(
        card: BankAtmStaffDisclosureScriptCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("직접 읽을 직원 도움 요청 상황\n")
                append(card.staffDisclosureSituation)
                append("\n\n값 없이 직접 읽을 문장\n")
                append(card.valueFreeScript)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n말하기·공유·첨부·기록 경계\n")
                card.doNotSayShareAttachOrRecordBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openBankAtmStaffDisclosureScriptAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openBankAtmStaffDisclosureScriptAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) {
                openBankAtmStaffDisclosureScriptAction(action)
            }
        }
    }

    private fun openBankAtmStaffDisclosureScriptAction(
        action: BankAtmStaffDisclosureScriptAction
    ) {
        when (action.id) {
            BankAtmStaffDisclosureScriptActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            BankAtmStaffDisclosureScriptActionId.CHECKER -> show(Screen.CHECKER)
            BankAtmStaffDisclosureScriptActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            BankAtmStaffDisclosureScriptActionId.SAFETY_PLAN -> show(Screen.PLAN)
            BankAtmStaffDisclosureScriptActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            BankAtmStaffDisclosureScriptActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            BankAtmStaffDisclosureScriptActionId.SAFE_PASTE_PRIVACY_COACH ->
                show(Screen.SAFE_PASTE_PRIVACY_COACH)
            BankAtmStaffDisclosureScriptActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            BankAtmStaffDisclosureScriptActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            BankAtmStaffDisclosureScriptActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
            BankAtmStaffDisclosureScriptActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            BankAtmStaffDisclosureScriptActionId.SAFE_CALL_SCRIPTS ->
                show(Screen.SAFE_CALL_SCRIPTS)
            BankAtmStaffDisclosureScriptActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
            BankAtmStaffDisclosureScriptActionId.NEW_NUMBER_QUARANTINE_COACH ->
                show(Screen.NEW_NUMBER_QUARANTINE_COACH)
            BankAtmStaffDisclosureScriptActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
            BankAtmStaffDisclosureScriptActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            BankAtmStaffDisclosureScriptActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            BankAtmStaffDisclosureScriptActionId.RECONTACT_BOUNDARY_COACH ->
                show(Screen.RECONTACT_BOUNDARY_COACH)
            BankAtmStaffDisclosureScriptActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            BankAtmStaffDisclosureScriptActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            BankAtmStaffDisclosureScriptActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            BankAtmStaffDisclosureScriptActionId.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH ->
                show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
            BankAtmStaffDisclosureScriptActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            BankAtmStaffDisclosureScriptActionId.GIFT_CARD_CODE_PAUSE_COACH ->
                show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
            BankAtmStaffDisclosureScriptActionId.LINK_APP_INSTALL_QUARANTINE_COACH ->
                show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
            BankAtmStaffDisclosureScriptActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
            BankAtmStaffDisclosureScriptActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH ->
                show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
            BankAtmStaffDisclosureScriptActionId.ACCOUNT_LOCKDOWN ->
                show(Screen.ACCOUNT_LOCKDOWN)
            BankAtmStaffDisclosureScriptActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            BankAtmStaffDisclosureScriptActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            BankAtmStaffDisclosureScriptActionId.PRIVACY_CENTER ->
                show(Screen.PRIVACY_CENTER)
            BankAtmStaffDisclosureScriptActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            BankAtmStaffDisclosureScriptActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            BankAtmStaffDisclosureScriptActionId.INCIDENT_TIMELINE_BRIEFING ->
                show(Screen.TIMELINE_BRIEFING)
            BankAtmStaffDisclosureScriptActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
            BankAtmStaffDisclosureScriptActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            BankAtmStaffDisclosureScriptActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            BankAtmStaffDisclosureScriptActionId.SAFETY_FLOW_ROUTER ->
                show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareBankAtmStaffDisclosureScriptCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 은행·ATM 직원 공개 문구 안내 공유"))
    }

    private fun LinearLayout.cashWithdrawalCourierHandoffPauseCoachScreen() {
        val coach = CashWithdrawalCourierHandoffPauseCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("현금·서류 전달과 신분증·셀피 요구 함께 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 현금 인출·전달 멈춤 카드\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.cashCourierPressureScenario}\n" +
                            card.valueFreeActionSentence
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) {
                    openCashWithdrawalCourierHandoffPauseAction(action)
                }
            } else {
                secondaryButton(action.label) {
                    openCashWithdrawalCourierHandoffPauseAction(action)
                }
            }
        }

        section(
            "전체 수동 현금 인출·전달 멈춤 카드",
            "카드는 항상 같은 순서입니다. 현금을 인출하거나 이동·만남·전달·보관 지시를 " +
                "따르지 말고, 현금·카드·통장·기기·서류 또는 장소·금액·경로·인물 값을 " +
                "이 화면에 넣지 마세요. 앱은 값 없는 행동 문장과 기존 앱 경로만 보여 주며 " +
                "선택·읽음·확인 상태도 남기지 않습니다."
        )
        coach.cards.forEach { card ->
            cashWithdrawalCourierHandoffPauseCard(
                card,
                recommended = card.id in recommendedIds
            )
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 현금 인출·전달 멈춤 안내 공유") {
            shareCashWithdrawalCourierHandoffPauseCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("송금 전 멈춤 확인") { show(Screen.MONEY_TRANSFER_PAUSE_GATE) }
        secondaryButton("기관 사칭·압박 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("현재 연락을 끝내는 문장") {
            show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
        }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.cashWithdrawalCourierHandoffPauseCard(
        card: CashWithdrawalCourierHandoffPauseCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("직접 읽을 현금 인출·전달 압박 상황\n")
                append(card.cashCourierPressureScenario)
                append("\n\n값 없는 다음 행동\n")
                append(card.valueFreeActionSentence)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n인출·이동·만남·전달·공유·첨부 경계\n")
                card.doNotWithdrawTravelMeetHandoffShareOrAttachBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openCashWithdrawalCourierHandoffPauseAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openCashWithdrawalCourierHandoffPauseAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) {
                openCashWithdrawalCourierHandoffPauseAction(action)
            }
        }
    }

    private fun openCashWithdrawalCourierHandoffPauseAction(
        action: CashWithdrawalCourierHandoffPauseAction
    ) {
        when (action.id) {
            CashWithdrawalCourierHandoffPauseActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            CashWithdrawalCourierHandoffPauseActionId.CHECKER -> show(Screen.CHECKER)
            CashWithdrawalCourierHandoffPauseActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            CashWithdrawalCourierHandoffPauseActionId.SAFETY_PLAN -> show(Screen.PLAN)
            CashWithdrawalCourierHandoffPauseActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            CashWithdrawalCourierHandoffPauseActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            CashWithdrawalCourierHandoffPauseActionId.SAFE_PASTE_PRIVACY_COACH ->
                show(Screen.SAFE_PASTE_PRIVACY_COACH)
            CashWithdrawalCourierHandoffPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            CashWithdrawalCourierHandoffPauseActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            CashWithdrawalCourierHandoffPauseActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
            CashWithdrawalCourierHandoffPauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            CashWithdrawalCourierHandoffPauseActionId.SAFE_CALL_SCRIPTS ->
                show(Screen.SAFE_CALL_SCRIPTS)
            CashWithdrawalCourierHandoffPauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
            CashWithdrawalCourierHandoffPauseActionId.NEW_NUMBER_QUARANTINE_COACH ->
                show(Screen.NEW_NUMBER_QUARANTINE_COACH)
            CashWithdrawalCourierHandoffPauseActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
            CashWithdrawalCourierHandoffPauseActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            CashWithdrawalCourierHandoffPauseActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            CashWithdrawalCourierHandoffPauseActionId.RECONTACT_BOUNDARY_COACH ->
                show(Screen.RECONTACT_BOUNDARY_COACH)
            CashWithdrawalCourierHandoffPauseActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            CashWithdrawalCourierHandoffPauseActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            CashWithdrawalCourierHandoffPauseActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            CashWithdrawalCourierHandoffPauseActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            CashWithdrawalCourierHandoffPauseActionId.GIFT_CARD_CODE_PAUSE_COACH ->
                show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
            CashWithdrawalCourierHandoffPauseActionId.LINK_APP_INSTALL_QUARANTINE_COACH ->
                show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
            CashWithdrawalCourierHandoffPauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
            CashWithdrawalCourierHandoffPauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH ->
                show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
            CashWithdrawalCourierHandoffPauseActionId.ACCOUNT_LOCKDOWN ->
                show(Screen.ACCOUNT_LOCKDOWN)
            CashWithdrawalCourierHandoffPauseActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            CashWithdrawalCourierHandoffPauseActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            CashWithdrawalCourierHandoffPauseActionId.PRIVACY_CENTER ->
                show(Screen.PRIVACY_CENTER)
            CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            CashWithdrawalCourierHandoffPauseActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            CashWithdrawalCourierHandoffPauseActionId.INCIDENT_TIMELINE_BRIEFING ->
                show(Screen.TIMELINE_BRIEFING)
            CashWithdrawalCourierHandoffPauseActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
            CashWithdrawalCourierHandoffPauseActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            CashWithdrawalCourierHandoffPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            CashWithdrawalCourierHandoffPauseActionId.SAFETY_FLOW_ROUTER ->
                show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareCashWithdrawalCourierHandoffPauseCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 현금 인출·전달 멈춤 안내 공유"))
    }

    private fun LinearLayout.linkAppInstallQuarantineCoachScreen() {
        val coach = LinkAppInstallQuarantineCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("링크·앱 지시가 현금 인출·전달로 이어지면 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("링크·앱 안의 신분증·셀피 촬영 요구 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 링크·앱 설치 격리 카드\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.linkAppPressureScenario}\n" +
                            card.valueFreeActionSentence
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openLinkAppInstallQuarantineAction(action) }
            } else {
                secondaryButton(action.label) { openLinkAppInstallQuarantineAction(action) }
            }
        }

        section(
            "전체 수동 링크·앱 설치 격리 카드",
            "카드는 항상 같은 순서입니다. 상대가 건넨 링크를 열거나 미리 보거나 " +
                "복사·공유·스캔하지 말고, APK·앱을 설치하거나 권한을 허용하지 마세요. " +
                "같은 통화·채팅·화면공유 세션에서 로그인 정보나 결제·상품권 값을 " +
                "입력하지 않습니다. 앱은 링크나 앱을 검사하지 않고 값 없는 행동 문장과 " +
                "기존 앱 경로만 보여 주며 선택·읽음·확인 상태도 남기지 않습니다."
        )
        coach.cards.forEach { card ->
            linkAppInstallQuarantineCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 링크·앱 설치 격리 안내 공유") {
            shareLinkAppInstallQuarantineCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("안전 붙여넣기 개인정보 코치") {
            show(Screen.SAFE_PASTE_PRIVACY_COACH)
        }
        secondaryButton("원격 접근 중단 코치") { show(Screen.REMOTE_ACCESS_CUTOFF_COACH) }
        secondaryButton("인증정보 노출 멈춤 코치") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.linkAppInstallQuarantineCard(
        card: LinkAppInstallQuarantineCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("직접 읽을 링크·앱 설치 압박 상황\n")
                append(card.linkAppPressureScenario)
                append("\n\n값 없는 다음 행동\n")
                append(card.valueFreeActionSentence)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n열기·설치·입력·스캔·권한·공유 경계\n")
                card.doNotOpenInstallEnterScanGrantOrShareBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openLinkAppInstallQuarantineAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openLinkAppInstallQuarantineAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openLinkAppInstallQuarantineAction(action) }
        }
    }

    private fun openLinkAppInstallQuarantineAction(action: LinkAppInstallQuarantineAction) {
        when (action.id) {
            LinkAppInstallQuarantineActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            LinkAppInstallQuarantineActionId.CHECKER -> show(Screen.CHECKER)
            LinkAppInstallQuarantineActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            LinkAppInstallQuarantineActionId.SAFETY_PLAN -> show(Screen.PLAN)
            LinkAppInstallQuarantineActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            LinkAppInstallQuarantineActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            LinkAppInstallQuarantineActionId.SAFE_PASTE_PRIVACY_COACH ->
                show(Screen.SAFE_PASTE_PRIVACY_COACH)
            LinkAppInstallQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            LinkAppInstallQuarantineActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            LinkAppInstallQuarantineActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
            LinkAppInstallQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            LinkAppInstallQuarantineActionId.SAFE_CALL_SCRIPTS ->
                show(Screen.SAFE_CALL_SCRIPTS)
            LinkAppInstallQuarantineActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
            LinkAppInstallQuarantineActionId.NEW_NUMBER_QUARANTINE_COACH ->
                show(Screen.NEW_NUMBER_QUARANTINE_COACH)
            LinkAppInstallQuarantineActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
            LinkAppInstallQuarantineActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            LinkAppInstallQuarantineActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            LinkAppInstallQuarantineActionId.RECONTACT_BOUNDARY_COACH ->
                show(Screen.RECONTACT_BOUNDARY_COACH)
            LinkAppInstallQuarantineActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            LinkAppInstallQuarantineActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            LinkAppInstallQuarantineActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            LinkAppInstallQuarantineActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            LinkAppInstallQuarantineActionId.GIFT_CARD_CODE_PAUSE_COACH ->
                show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
            LinkAppInstallQuarantineActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
            LinkAppInstallQuarantineActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH ->
                show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
            LinkAppInstallQuarantineActionId.ACCOUNT_LOCKDOWN ->
                show(Screen.ACCOUNT_LOCKDOWN)
            LinkAppInstallQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            LinkAppInstallQuarantineActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            LinkAppInstallQuarantineActionId.PRIVACY_CENTER ->
                show(Screen.PRIVACY_CENTER)
            LinkAppInstallQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            LinkAppInstallQuarantineActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            LinkAppInstallQuarantineActionId.INCIDENT_TIMELINE_BRIEFING ->
                show(Screen.TIMELINE_BRIEFING)
            LinkAppInstallQuarantineActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
            LinkAppInstallQuarantineActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            LinkAppInstallQuarantineActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            LinkAppInstallQuarantineActionId.SAFETY_FLOW_ROUTER ->
                show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareLinkAppInstallQuarantineCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 링크·앱 설치 격리 안내 공유"))
    }

    private fun LinearLayout.giftCardCodePauseCoachScreen() {
        val coach = GiftCardCodePauseCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("상품권 대신 현금·카드·통장 전달을 요구해도 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("상품권·코드와 함께 요구한 신분증·셀피도 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("상품권·코드 링크나 설치 지시도 격리 점검") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 상품권·코드 멈춤 카드\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.codePressureScenario}\n" +
                            card.valueFreeActionSentence
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openGiftCardCodePauseAction(action) }
            } else {
                secondaryButton(action.label) { openGiftCardCodePauseAction(action) }
            }
        }

        section(
            "전체 수동 상품권·코드 멈춤 카드",
            "카드는 항상 같은 순서입니다. 상품권·기프티콘·선불카드의 코드·PIN·" +
                "바코드·QR을 입력하거나 보여 주지 마세요. 앱은 구매·촬영·스캔·전달·" +
                "잔액 확인·사용·환급·취소를 하지 않고, 값 없는 행동 문장과 기존 앱 " +
                "경로만 보여 줍니다. 선택·읽음·확인 상태도 남기지 않습니다."
        )
        coach.cards.forEach { card ->
            giftCardCodePauseCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 상품권·코드 멈춤 안내 공유") {
            shareGiftCardCodePauseCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("송금 전 멈춤 확인") { show(Screen.MONEY_TRANSFER_PAUSE_GATE) }
        secondaryButton("인증정보 노출 멈춤 코치") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.giftCardCodePauseCard(
        card: GiftCardCodePauseCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("직접 읽을 상품권·코드 압박 상황\n")
                append(card.codePressureScenario)
                append("\n\n값 없는 다음 행동\n")
                append(card.valueFreeActionSentence)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n구매·공유·스캔·사용·첨부 경계\n")
                card.doNotBuyShareScanRedeemOrAttachBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openGiftCardCodePauseAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openGiftCardCodePauseAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openGiftCardCodePauseAction(action) }
        }
    }

    private fun openGiftCardCodePauseAction(action: GiftCardCodePauseAction) {
        when (action.id) {
            GiftCardCodePauseActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            GiftCardCodePauseActionId.CHECKER -> show(Screen.CHECKER)
            GiftCardCodePauseActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            GiftCardCodePauseActionId.SAFETY_PLAN -> show(Screen.PLAN)
            GiftCardCodePauseActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            GiftCardCodePauseActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            GiftCardCodePauseActionId.SAFE_PASTE_PRIVACY_COACH ->
                show(Screen.SAFE_PASTE_PRIVACY_COACH)
            GiftCardCodePauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            GiftCardCodePauseActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            GiftCardCodePauseActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
            GiftCardCodePauseActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            GiftCardCodePauseActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            GiftCardCodePauseActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
            GiftCardCodePauseActionId.NEW_NUMBER_QUARANTINE_COACH ->
                show(Screen.NEW_NUMBER_QUARANTINE_COACH)
            GiftCardCodePauseActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
            GiftCardCodePauseActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            GiftCardCodePauseActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            GiftCardCodePauseActionId.RECONTACT_BOUNDARY_COACH ->
                show(Screen.RECONTACT_BOUNDARY_COACH)
            GiftCardCodePauseActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            GiftCardCodePauseActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            GiftCardCodePauseActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            GiftCardCodePauseActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            GiftCardCodePauseActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
            GiftCardCodePauseActionId.RECOVERY_PROMISE_REALITY_CHECK_COACH ->
                show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
            GiftCardCodePauseActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            GiftCardCodePauseActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            GiftCardCodePauseActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            GiftCardCodePauseActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            GiftCardCodePauseActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            GiftCardCodePauseActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            GiftCardCodePauseActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.GIFT_CARD_CODE_PAUSE_COACH)
            GiftCardCodePauseActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            GiftCardCodePauseActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            GiftCardCodePauseActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareGiftCardCodePauseCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 상품권·코드 멈춤 안내 공유"))
    }

    private fun LinearLayout.recoveryPromiseRealityCheckCoachScreen() {
        val coach = RecoveryPromiseRealityCheckCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("회복 조건의 현금 인출·전달 지시도 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("회복 조건의 신분증·셀피 제출 요구 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("회복 링크·앱 설치 지시를 열기 전 격리 점검") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("회복 비용을 상품권·코드로 요구하면 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 회복 약속 현실 점검 카드\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.recoveryPromiseScenario}\n" +
                            card.valueFreeActionSentence
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openRecoveryPromiseRealityCheckAction(action) }
            } else {
                secondaryButton(action.label) { openRecoveryPromiseRealityCheckAction(action) }
            }
        }

        section(
            "전체 수동 회복 약속 현실 점검 카드",
            "카드는 항상 같은 순서입니다. 회복·환급·취소·동결·추적 약속을 결과로 " +
                "단정하지 말고, 실제 이름·연락처·사건·계정·카드·결제·지갑·인증 값을 " +
                "이 화면에 넣지 마세요. 앱은 값 없는 행동 문장과 기존 앱 경로만 보여 " +
                "주며 선택·읽음·확인 상태를 남기지 않습니다."
        )
        coach.cards.forEach { card ->
            recoveryPromiseRealityCheckCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 회복 약속 점검 안내 공유") {
            shareRecoveryPromiseRealityCheckCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("신고 준비 화면으로 이동") {
            show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
        }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.recoveryPromiseRealityCheckCard(
        card: RecoveryPromiseRealityCheckCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("직접 읽을 회복 약속 상황\n")
                append(card.recoveryPromiseScenario)
                append("\n\n값 없는 다음 행동\n")
                append(card.valueFreeActionSentence)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n지불·공유·제출·설치·통화 경계\n")
                card.doNotPayShareSubmitInstallOrCallBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openRecoveryPromiseRealityCheckAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openRecoveryPromiseRealityCheckAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openRecoveryPromiseRealityCheckAction(action) }
        }
    }

    private fun openRecoveryPromiseRealityCheckAction(
        action: RecoveryPromiseRealityCheckAction
    ) {
        when (action.id) {
            RecoveryPromiseRealityCheckActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            RecoveryPromiseRealityCheckActionId.CHECKER -> show(Screen.CHECKER)
            RecoveryPromiseRealityCheckActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            RecoveryPromiseRealityCheckActionId.SAFETY_PLAN -> show(Screen.PLAN)
            RecoveryPromiseRealityCheckActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            RecoveryPromiseRealityCheckActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            RecoveryPromiseRealityCheckActionId.SAFE_PASTE_PRIVACY_COACH ->
                show(Screen.SAFE_PASTE_PRIVACY_COACH)
            RecoveryPromiseRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            RecoveryPromiseRealityCheckActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            RecoveryPromiseRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
            RecoveryPromiseRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            RecoveryPromiseRealityCheckActionId.SAFE_CALL_SCRIPTS ->
                show(Screen.SAFE_CALL_SCRIPTS)
            RecoveryPromiseRealityCheckActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
            RecoveryPromiseRealityCheckActionId.NEW_NUMBER_QUARANTINE_COACH ->
                show(Screen.NEW_NUMBER_QUARANTINE_COACH)
            RecoveryPromiseRealityCheckActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
            RecoveryPromiseRealityCheckActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            RecoveryPromiseRealityCheckActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            RecoveryPromiseRealityCheckActionId.RECONTACT_BOUNDARY_COACH ->
                show(Screen.RECONTACT_BOUNDARY_COACH)
            RecoveryPromiseRealityCheckActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            RecoveryPromiseRealityCheckActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            RecoveryPromiseRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            RecoveryPromiseRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            RecoveryPromiseRealityCheckActionId.OFFICIAL_PRESSURE_REALITY_CHECK_COACH ->
                show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
            RecoveryPromiseRealityCheckActionId.ACCOUNT_LOCKDOWN ->
                show(Screen.ACCOUNT_LOCKDOWN)
            RecoveryPromiseRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            RecoveryPromiseRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            RecoveryPromiseRealityCheckActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            RecoveryPromiseRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            RecoveryPromiseRealityCheckActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            RecoveryPromiseRealityCheckActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
            RecoveryPromiseRealityCheckActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            RecoveryPromiseRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            RecoveryPromiseRealityCheckActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareRecoveryPromiseRealityCheckCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 회복 약속 점검 안내 공유"))
    }

    private fun LinearLayout.officialPressureRealityCheckCoachScreen() {
        val coach = OfficialPressureRealityCheckCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("기관 직함으로 현금 인출·전달을 지시하면 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("기관 사칭의 신분증·셀피 제출 압박 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("기관 사칭 링크·앱 설치 압박 격리 점검") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("상품권·코드 요구를 따르기 전 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("회복·환급·취소 약속을 믿기 전 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 기관 사칭·압박 현실 점검 카드\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.pressureScenario}\n" +
                            card.valueFreeActionSentence
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openOfficialPressureRealityCheckAction(action) }
            } else {
                secondaryButton(action.label) { openOfficialPressureRealityCheckAction(action) }
            }
        }

        section(
            "전체 수동 기관 사칭·압박 현실 점검 카드",
            "카드는 항상 같은 순서입니다. 직함·기관명·발신번호·링크·문서 화면을 증거로 " +
                "단정하지 말고, 실제 이름·번호·주소·사건·계정·결제 값을 이 화면에 넣지 " +
                "마세요. 앱은 값 없는 행동 문장과 기존 앱 경로만 보여 주며 선택·읽음·확인 " +
                "상태를 남기지 않습니다."
        )
        coach.cards.forEach { card ->
            officialPressureRealityCheckCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 기관 사칭·압박 점검 안내 공유") {
            shareOfficialPressureRealityCheckCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("신고 준비 화면으로 이동") {
            show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
        }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.officialPressureRealityCheckCard(
        card: OfficialPressureRealityCheckCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("직접 읽을 압박 상황\n")
                append(card.pressureScenario)
                append("\n\n값 없는 다음 행동\n")
                append(card.valueFreeActionSentence)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n전화·조회·공유·제출 경계\n")
                card.doNotCallLookupShareOrSubmitBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openOfficialPressureRealityCheckAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openOfficialPressureRealityCheckAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openOfficialPressureRealityCheckAction(action) }
        }
    }

    private fun openOfficialPressureRealityCheckAction(
        action: OfficialPressureRealityCheckAction
    ) {
        when (action.id) {
            OfficialPressureRealityCheckActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            OfficialPressureRealityCheckActionId.CHECKER -> show(Screen.CHECKER)
            OfficialPressureRealityCheckActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            OfficialPressureRealityCheckActionId.SAFETY_PLAN -> show(Screen.PLAN)
            OfficialPressureRealityCheckActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            OfficialPressureRealityCheckActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            OfficialPressureRealityCheckActionId.SAFE_PASTE_PRIVACY_COACH ->
                show(Screen.SAFE_PASTE_PRIVACY_COACH)
            OfficialPressureRealityCheckActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            OfficialPressureRealityCheckActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            OfficialPressureRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
            OfficialPressureRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            OfficialPressureRealityCheckActionId.SAFE_CALL_SCRIPTS ->
                show(Screen.SAFE_CALL_SCRIPTS)
            OfficialPressureRealityCheckActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
            OfficialPressureRealityCheckActionId.NEW_NUMBER_QUARANTINE_COACH ->
                show(Screen.NEW_NUMBER_QUARANTINE_COACH)
            OfficialPressureRealityCheckActionId.CREDENTIAL_SECRET_FREEZE_COACH ->
                show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
            OfficialPressureRealityCheckActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            OfficialPressureRealityCheckActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            OfficialPressureRealityCheckActionId.RECONTACT_BOUNDARY_COACH ->
                show(Screen.RECONTACT_BOUNDARY_COACH)
            OfficialPressureRealityCheckActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            OfficialPressureRealityCheckActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            OfficialPressureRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            OfficialPressureRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            OfficialPressureRealityCheckActionId.ACCOUNT_LOCKDOWN ->
                show(Screen.ACCOUNT_LOCKDOWN)
            OfficialPressureRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            OfficialPressureRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            OfficialPressureRealityCheckActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            OfficialPressureRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            OfficialPressureRealityCheckActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            OfficialPressureRealityCheckActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
            OfficialPressureRealityCheckActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            OfficialPressureRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            OfficialPressureRealityCheckActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareOfficialPressureRealityCheckCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 기관 사칭·압박 점검 안내 공유"))
    }

    private fun LinearLayout.credentialSecretFreezeCoachScreen() {
        val coach = CredentialSecretFreezeCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("비밀값과 함께 현금·카드·기기 전달도 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("신분증·셀피 요구도 비밀값처럼 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("로그인·인증값 입력 링크와 설치 지시 격리 점검") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("상품권·코드 요구를 따르기 전 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("회복 조건으로 비밀값을 요구하면 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("직함·기관명·문서 압박을 믿기 전 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 비밀값 멈춤 카드\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.freezeScenario}\n" +
                            card.valueFreeActionSentence
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openCredentialSecretFreezeAction(action) }
            } else {
                secondaryButton(action.label) { openCredentialSecretFreezeAction(action) }
            }
        }

        section(
            "전체 수동 비밀값 멈춤 카드",
            "카드는 항상 같은 순서입니다. 실제 인증번호·PIN·비밀번호·보안 답·계정·카드 값을 " +
                "이 앱에 넣지 말고, 값 없는 행동 문장과 기존 앱 경로만 직접 고르세요. " +
                "선택·읽음·노출·계정·결제·복구 상태는 남지 않습니다."
        )
        coach.cards.forEach { card ->
            credentialSecretFreezeCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 비밀값 멈춤 안내 공유") {
            shareCredentialSecretFreezeCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("돈 행동부터 멈추기") {
            show(Screen.MONEY_TRANSFER_PAUSE_GATE)
        }
        secondaryButton("계정·기기 대응 순서 보기") {
            show(Screen.ACCOUNT_LOCKDOWN)
        }
        secondaryButton("붙여넣기 전 개인정보 경계 보기") {
            show(Screen.SAFE_PASTE_PRIVACY_COACH)
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.credentialSecretFreezeCard(
        card: CredentialSecretFreezeCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("직접 읽을 멈춤 상황\n")
                append(card.freezeScenario)
                append("\n\n값 없는 다음 행동\n")
                append(card.valueFreeActionSentence)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n입력·공유·클립보드·자동완성 경계\n")
                card.doNotEnterShareClipboardOrAutofillBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openCredentialSecretFreezeAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openCredentialSecretFreezeAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openCredentialSecretFreezeAction(action) }
        }
    }

    private fun openCredentialSecretFreezeAction(action: CredentialSecretFreezeAction) {
        when (action.id) {
            CredentialSecretFreezeActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            CredentialSecretFreezeActionId.CHECKER -> show(Screen.CHECKER)
            CredentialSecretFreezeActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            CredentialSecretFreezeActionId.SAFETY_PLAN -> show(Screen.PLAN)
            CredentialSecretFreezeActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            CredentialSecretFreezeActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            CredentialSecretFreezeActionId.SAFE_PASTE_PRIVACY_COACH ->
                show(Screen.SAFE_PASTE_PRIVACY_COACH)
            CredentialSecretFreezeActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            CredentialSecretFreezeActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            CredentialSecretFreezeActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
            CredentialSecretFreezeActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            CredentialSecretFreezeActionId.SAFE_CALL_SCRIPTS ->
                show(Screen.SAFE_CALL_SCRIPTS)
            CredentialSecretFreezeActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
            CredentialSecretFreezeActionId.NEW_NUMBER_QUARANTINE_COACH ->
                show(Screen.NEW_NUMBER_QUARANTINE_COACH)
            CredentialSecretFreezeActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            CredentialSecretFreezeActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            CredentialSecretFreezeActionId.RECONTACT_BOUNDARY_COACH ->
                show(Screen.RECONTACT_BOUNDARY_COACH)
            CredentialSecretFreezeActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            CredentialSecretFreezeActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            CredentialSecretFreezeActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            CredentialSecretFreezeActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            CredentialSecretFreezeActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            CredentialSecretFreezeActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            CredentialSecretFreezeActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            CredentialSecretFreezeActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            CredentialSecretFreezeActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            CredentialSecretFreezeActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            CredentialSecretFreezeActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
            CredentialSecretFreezeActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            CredentialSecretFreezeActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            CredentialSecretFreezeActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareCredentialSecretFreezeCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 비밀값 멈춤 안내 공유"))
    }

    private fun LinearLayout.newNumberQuarantineCoachScreen() {
        val coach = NewNumberQuarantineCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("새 연락수단의 현금 인출·전달 지시 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("새 연락수단의 신분증·셀피 요구 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("새 연락수단이 건넨 링크·앱 설치 지시 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("새 연락수단의 상품권·코드 요구 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("새 연락수단의 회복 약속을 믿기 전 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 사칭·압박이 붙은 새 연락수단 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("비밀값을 말하거나 붙여넣기 전 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 새 연락수단 격리 카드\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.quarantineScenario}\n" +
                            card.valueFreeActionSentence
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openNewNumberQuarantineAction(action) }
            } else {
                secondaryButton(action.label) { openNewNumberQuarantineAction(action) }
            }
        }

        section(
            "전체 수동 새 연락수단 격리 카드",
            "카드는 항상 같은 순서입니다. 새 번호·프로필·채널의 실제 값을 이 앱에 넣지 말고, " +
                "바깥의 기존 경로에서 확인하기 전까지 값 없는 행동 문장과 기존 앱 경로만 직접 " +
                "고르세요. 선택·읽음·확인·연락처 상태는 남지 않습니다."
        )
        coach.cards.forEach { card ->
            newNumberQuarantineCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 새 연락수단 격리 안내 공유") {
            shareNewNumberQuarantineCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("현재 연락 밖의 기존 경로에서 확인") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        secondaryButton("가족 콜백 확인 카드") {
            openFamilyCallbackCard(currentAnalysis)
        }
        secondaryButton("목소리를 증거로 믿기 전 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        secondaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.newNumberQuarantineCard(
        card: NewNumberQuarantineCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("직접 읽을 격리 상황\n")
                append(card.quarantineScenario)
                append("\n\n값 없는 다음 행동\n")
                append(card.valueFreeActionSentence)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n저장·연락처·실제 값 경계\n")
                card.doNotSaveContactOrValueBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openNewNumberQuarantineAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openNewNumberQuarantineAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openNewNumberQuarantineAction(action) }
        }
    }

    private fun openNewNumberQuarantineAction(action: NewNumberQuarantineAction) {
        when (action.id) {
            NewNumberQuarantineActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            NewNumberQuarantineActionId.CHECKER -> show(Screen.CHECKER)
            NewNumberQuarantineActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            NewNumberQuarantineActionId.SAFETY_PLAN -> show(Screen.PLAN)
            NewNumberQuarantineActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            NewNumberQuarantineActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            NewNumberQuarantineActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            NewNumberQuarantineActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            NewNumberQuarantineActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
            NewNumberQuarantineActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            NewNumberQuarantineActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            NewNumberQuarantineActionId.VOICE_IMPERSONATION_REALITY_CHECK_COACH ->
                show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
            NewNumberQuarantineActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            NewNumberQuarantineActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            NewNumberQuarantineActionId.RECONTACT_BOUNDARY_COACH ->
                show(Screen.RECONTACT_BOUNDARY_COACH)
            NewNumberQuarantineActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            NewNumberQuarantineActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            NewNumberQuarantineActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            NewNumberQuarantineActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            NewNumberQuarantineActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            NewNumberQuarantineActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            NewNumberQuarantineActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            NewNumberQuarantineActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            NewNumberQuarantineActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            NewNumberQuarantineActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            NewNumberQuarantineActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.NEW_NUMBER_QUARANTINE_COACH)
            NewNumberQuarantineActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            NewNumberQuarantineActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            NewNumberQuarantineActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareNewNumberQuarantineCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 새 연락수단 격리 안내 공유"))
    }

    private fun LinearLayout.voiceImpersonationRealityCheckCoachScreen() {
        val coach = VoiceImpersonationRealityCheckCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("목소리로 받은 현금 인출·만남·전달 지시 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("목소리 요청의 신분증·셀피 촬영·전송 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("목소리로 받은 링크·앱 설치 지시 격리 점검") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("목소리로 받은 상품권·코드 요구 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("목소리로 건넨 회복 약속을 믿기 전 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 직함·위협과 목소리를 증거로 믿기 전 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("목소리 요청에 비밀값을 주기 전 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("새 번호·새 프로필을 믿기 전 격리") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 목소리 현실 확인 카드\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.realityCheckScenario}\n" +
                            card.valueFreeActionSentence
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openVoiceImpersonationRealityCheckAction(action) }
            } else {
                secondaryButton(action.label) { openVoiceImpersonationRealityCheckAction(action) }
            }
        }

        section(
            "전체 수동 목소리 현실 확인 카드",
            "카드는 항상 같은 순서입니다. 목소리나 통화 내용을 이 앱에 넣지 말고, " +
                "값 없는 문장과 필요한 기존 앱 경로만 가족이 직접 고르세요. " +
                "선택·읽음·체크·통화 상태는 남지 않습니다."
        )
        coach.cards.forEach { card ->
            voiceImpersonationRealityCheckCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 목소리 현실 확인 안내 공유") {
            shareVoiceImpersonationRealityCheckCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("현재 연락과 분리해 가족 확인") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        secondaryButton("가족 콜백 확인 카드") {
            openFamilyCallbackCard(currentAnalysis)
        }
        secondaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.voiceImpersonationRealityCheckCard(
        card: VoiceImpersonationRealityCheckCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("직접 읽을 현실 확인 상황\n")
                append(card.realityCheckScenario)
                append("\n\n값 없는 다음 행동\n")
                append(card.valueFreeActionSentence)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n녹음·공유·질문하지 않을 것\n")
                card.doNotRecordShareOrAskBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openVoiceImpersonationRealityCheckAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openVoiceImpersonationRealityCheckAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openVoiceImpersonationRealityCheckAction(action) }
        }
    }

    private fun openVoiceImpersonationRealityCheckAction(
        action: VoiceImpersonationRealityCheckAction
    ) {
        when (action.id) {
            VoiceImpersonationRealityCheckActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            VoiceImpersonationRealityCheckActionId.CHECKER -> show(Screen.CHECKER)
            VoiceImpersonationRealityCheckActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            VoiceImpersonationRealityCheckActionId.SAFETY_PLAN -> show(Screen.PLAN)
            VoiceImpersonationRealityCheckActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            VoiceImpersonationRealityCheckActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            VoiceImpersonationRealityCheckActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            VoiceImpersonationRealityCheckActionId.FAMILY_SAFE_WORD_RESET_COACH ->
                show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
            VoiceImpersonationRealityCheckActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            VoiceImpersonationRealityCheckActionId.SAFE_CALL_SCRIPTS ->
                show(Screen.SAFE_CALL_SCRIPTS)
            VoiceImpersonationRealityCheckActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            VoiceImpersonationRealityCheckActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            VoiceImpersonationRealityCheckActionId.RECONTACT_BOUNDARY_COACH ->
                show(Screen.RECONTACT_BOUNDARY_COACH)
            VoiceImpersonationRealityCheckActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            VoiceImpersonationRealityCheckActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            VoiceImpersonationRealityCheckActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            VoiceImpersonationRealityCheckActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            VoiceImpersonationRealityCheckActionId.ACCOUNT_LOCKDOWN ->
                show(Screen.ACCOUNT_LOCKDOWN)
            VoiceImpersonationRealityCheckActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            VoiceImpersonationRealityCheckActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            VoiceImpersonationRealityCheckActionId.PRIVACY_CENTER ->
                show(Screen.PRIVACY_CENTER)
            VoiceImpersonationRealityCheckActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            VoiceImpersonationRealityCheckActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            VoiceImpersonationRealityCheckActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
            VoiceImpersonationRealityCheckActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            VoiceImpersonationRealityCheckActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            VoiceImpersonationRealityCheckActionId.SAFETY_FLOW_ROUTER ->
                show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareVoiceImpersonationRealityCheckCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 목소리 현실 확인 안내 공유"))
    }

    private fun LinearLayout.familySafeWordResetCoachScreen() {
        val coach = FamilySafeWordResetCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("새 번호·새 프로필을 믿기 전 격리") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("익숙한 목소리를 믿기 전 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 재정비 카드\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.resetScenario}\n${card.valueFreeFamilySentence}"
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openFamilySafeWordResetAction(action) }
            } else {
                secondaryButton(action.label) { openFamilySafeWordResetAction(action) }
            }
        }

        section(
            "전체 수동 가족 확인 재정비 카드",
            "카드는 항상 같은 순서입니다. 실제 안전단어·확인 답·콜백 문장·연락처를 이 " +
                "코치에 입력하지 말고 값 없는 문장과 필요한 기존 앱 경로만 가족이 직접 " +
                "고르세요. 선택·읽음·체크·재정비 상태는 남지 않습니다."
        )
        coach.cards.forEach { card ->
            familySafeWordResetCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 재정비 안내 공유") {
            shareFamilySafeWordResetCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("가족 안전 계획 직접 편집") { show(Screen.PLAN) }
        secondaryButton("가상 문장으로 가족 안전 연습") { openDrill() }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.familySafeWordResetCard(
        card: FamilySafeWordResetCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("직접 읽을 재정비 상황\n")
                append(card.resetScenario)
                append("\n\n값 없는 가족 문장\n")
                append(card.valueFreeFamilySentence)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n말하거나 기록·공유하지 않을 것\n")
                card.doNotSayRecordOrShareBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openFamilySafeWordResetAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openFamilySafeWordResetAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openFamilySafeWordResetAction(action) }
        }
    }

    private fun openFamilySafeWordResetAction(action: FamilySafeWordResetAction) {
        when (action.id) {
            FamilySafeWordResetActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            FamilySafeWordResetActionId.CHECKER -> show(Screen.CHECKER)
            FamilySafeWordResetActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            FamilySafeWordResetActionId.SAFETY_PLAN -> show(Screen.PLAN)
            FamilySafeWordResetActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            FamilySafeWordResetActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            FamilySafeWordResetActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            FamilySafeWordResetActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            FamilySafeWordResetActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            FamilySafeWordResetActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            FamilySafeWordResetActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            FamilySafeWordResetActionId.RECONTACT_BOUNDARY_COACH ->
                show(Screen.RECONTACT_BOUNDARY_COACH)
            FamilySafeWordResetActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            FamilySafeWordResetActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            FamilySafeWordResetActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            FamilySafeWordResetActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            FamilySafeWordResetActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            FamilySafeWordResetActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            FamilySafeWordResetActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            FamilySafeWordResetActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            FamilySafeWordResetActionId.EVIDENCE_HANDOFF_PRIVACY_COACH ->
                show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            FamilySafeWordResetActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            FamilySafeWordResetActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.FAMILY_SAFE_WORD_RESET_COACH)
            FamilySafeWordResetActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            FamilySafeWordResetActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            FamilySafeWordResetActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareFamilySafeWordResetCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 값 없는 재정비 안내 공유"))
    }

    private fun LinearLayout.evidenceHandoffPrivacyCoachScreen() {
        val coach = EvidenceHandoffPrivacyCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("증거 전달과 현금·카드·기기 전달을 분리해 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("증거 전달과 신분증·셀피 제출 요구 분리") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("링크·앱 값을 넘기지 않고 격리 경계 점검") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("상품권·코드 값 없이 증거 전달 멈춤 점검") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("회복 약속에 증거를 넘기기 전 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 사칭·압박 자료를 넘기기 전 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("증거에 비밀값을 넣기 전 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("새 연락수단을 격리한 채 증거 경계 확인") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("목소리 자료 없이 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 증거 전달 경계\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.handoffScenario}\n${card.privacyActionSentence}"
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openEvidenceHandoffPrivacyAction(action) }
            } else {
                secondaryButton(action.label) { openEvidenceHandoffPrivacyAction(action) }
            }
        }

        section(
            "전체 수동 증거 전달 개인정보 카드",
            "카드는 항상 같은 순서입니다. 증거 원본을 이 앱에 넣지 말고, 보내기 전에 값 없는 " +
                "문장과 필요한 기존 앱 경로만 가족이 직접 고르세요. 선택·읽음·체크·첨부·전달 " +
                "상태는 남지 않습니다."
        )
        coach.cards.forEach { card ->
            evidenceHandoffPrivacyCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 증거 전달 경계 공유") {
            shareEvidenceHandoffPrivacyCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("증거 보존 체크리스트") { openEvidenceKit(null) }
        secondaryButton("신고용 상담 준비 카드") {
            show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
        }
        secondaryButton("붙여넣기 전 개인정보 가리기") {
            show(Screen.SAFE_PASTE_PRIVACY_COACH)
        }
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.evidenceHandoffPrivacyCard(
        card: EvidenceHandoffPrivacyCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("직접 읽을 전달 상황\n")
                append(card.handoffScenario)
                append("\n\n값 없는 개인정보 행동 문장\n")
                append(card.privacyActionSentence)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n첨부·기록·공유하지 않을 것\n")
                card.doNotAttachRecordOrShareBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openEvidenceHandoffPrivacyAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openEvidenceHandoffPrivacyAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openEvidenceHandoffPrivacyAction(action) }
        }
    }

    private fun openEvidenceHandoffPrivacyAction(action: EvidenceHandoffPrivacyAction) {
        when (action.id) {
            EvidenceHandoffPrivacyActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            EvidenceHandoffPrivacyActionId.CHECKER -> show(Screen.CHECKER)
            EvidenceHandoffPrivacyActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            EvidenceHandoffPrivacyActionId.SAFE_PASTE_PRIVACY_COACH ->
                show(Screen.SAFE_PASTE_PRIVACY_COACH)
            EvidenceHandoffPrivacyActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            EvidenceHandoffPrivacyActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            EvidenceHandoffPrivacyActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            EvidenceHandoffPrivacyActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            EvidenceHandoffPrivacyActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            EvidenceHandoffPrivacyActionId.SAFE_CALL_SCRIPTS ->
                show(Screen.SAFE_CALL_SCRIPTS)
            EvidenceHandoffPrivacyActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            EvidenceHandoffPrivacyActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            EvidenceHandoffPrivacyActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            EvidenceHandoffPrivacyActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            EvidenceHandoffPrivacyActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            EvidenceHandoffPrivacyActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            EvidenceHandoffPrivacyActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            EvidenceHandoffPrivacyActionId.INCIDENT_TIMELINE_BRIEFING ->
                show(Screen.TIMELINE_BRIEFING)
            EvidenceHandoffPrivacyActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
            EvidenceHandoffPrivacyActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            EvidenceHandoffPrivacyActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)
            EvidenceHandoffPrivacyActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            EvidenceHandoffPrivacyActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            EvidenceHandoffPrivacyActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareEvidenceHandoffPrivacyCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 증거 전달 경계 공유"))
    }

    private fun LinearLayout.recontactBoundaryCoachScreen() {
        val coach = RecontactBoundaryCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("재연락의 현금 인출·전달 지시 다시 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("재연락의 신분증·셀피 요구 다시 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("재연락의 링크·앱 설치 압박 다시 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("재연락의 상품권·코드 요구 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("재연락의 회복 약속을 믿기 전 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 사칭·압박 재연락을 믿기 전 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("재연락에서 비밀값을 요구하면 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("새 연락수단을 믿지 않고 격리") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("다시 들린 목소리를 믿기 전 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 읽을 추천 재연락 경계\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.recontactScenario}\n${card.boundaryScript}"
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openRecontactBoundaryAction(action) }
            } else {
                secondaryButton(action.label) { openRecontactBoundaryAction(action) }
            }
        }

        section(
            "전체 수동 재연락 경계 카드",
            "카드는 항상 같은 순서입니다. 다시 연락한 상대와 같은 경로에서는 협상하지 않고, " +
                "상황에 맞는 짧은 경계 문장과 기존 앱 경로만 가족이 직접 고르세요. " +
                "연락 결과·읽음·선택·체크 상태는 남지 않습니다."
        )
        coach.cards.forEach { card ->
            recontactBoundaryCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 재연락 경계 안내 공유") {
            shareRecontactBoundaryCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("현재 연락과 분리해 확인 경로 고르기") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        secondaryButton("별도 확인 뒤 결과별 다음 단계") {
            show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
        }
        secondaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        secondaryButton("진행 중 연락 종료 문장") {
            show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.recontactBoundaryCard(
        card: RecontactBoundaryCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("다시 연락한 상황\n")
                append(card.recontactScenario)
                append("\n\n직접 읽을 경계 문장\n")
                append(card.boundaryScript)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n기록하거나 공유하지 않을 것\n")
                card.doNotRecordOrShareBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openRecontactBoundaryAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openRecontactBoundaryAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openRecontactBoundaryAction(action) }
        }
    }

    private fun openRecontactBoundaryAction(action: RecontactBoundaryAction) {
        when (action.id) {
            RecontactBoundaryActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            RecontactBoundaryActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            RecontactBoundaryActionId.CHECKER -> show(Screen.CHECKER)
            RecontactBoundaryActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            RecontactBoundaryActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            RecontactBoundaryActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            RecontactBoundaryActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            RecontactBoundaryActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            RecontactBoundaryActionId.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD ->
                show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            RecontactBoundaryActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            RecontactBoundaryActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            RecontactBoundaryActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            RecontactBoundaryActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            RecontactBoundaryActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            RecontactBoundaryActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            RecontactBoundaryActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            RecontactBoundaryActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            RecontactBoundaryActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.RECONTACT_BOUNDARY_COACH)
            RecontactBoundaryActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            RecontactBoundaryActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)
            RecontactBoundaryActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            RecontactBoundaryActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            RecontactBoundaryActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareRecontactBoundaryCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 재연락 경계 안내 공유"))
    }

    private fun LinearLayout.verificationOutcomeNextStepsBoardScreen() {
        val board = VerificationOutcomeNextStepsBoardBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = board.recommendedCardIds.toSet()

        title(board.title)
        subtitle(board.intro)
        secondaryButton("확인 결과와 별개로 현금 인출·전달 지시 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("확인 결과와 별개로 신분증·셀피 요구 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("확인 결과와 별개로 링크·앱 설치 지시 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("확인 결과와 별개로 상품권·코드 요구 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("회복 약속과 실제 결과를 분리해 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 사칭·압박의 말과 결과를 분리해 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("확인 결과와 관계없이 비밀값 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("새 연락수단 격리 원칙 먼저 보기") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("목소리를 증거로 삼지 않는 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        section(
            "${board.modeLabel} · ${board.recommendationTitle}",
            buildString {
                append(board.recommendationExplanation)
                append("\n\n먼저 읽을 추천 결과별 다음 단계\n")
                append(
                    board.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.outcomeCondition}\n${card.nextStepSentence}"
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    board.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        board.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openVerificationOutcomeNextStepsAction(action) }
            } else {
                secondaryButton(action.label) { openVerificationOutcomeNextStepsAction(action) }
            }
        }

        section(
            "전체 수동 결과별 다음 단계 카드",
            "카드는 항상 같은 순서이며 결과를 입력하거나 선택하는 화면이 아닙니다. " +
                "해당할 수 있는 조건을 가족이 직접 읽고 필요한 기존 앱 경로만 고르세요. " +
                "결과·선택·읽음·체크 상태는 남지 않습니다."
        )
        board.cards.forEach { card ->
            verificationOutcomeNextStepsCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            board.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 값 없는 결과별 다음 단계 공유") {
            shareVerificationOutcomeNextStepsBoard(board.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("현재 연락과 분리해 확인 경로 고르기") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        secondaryButton("다시 연락할 때 경계 문장 보기") {
            show(Screen.RECONTACT_BOUNDARY_COACH)
        }
        secondaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.verificationOutcomeNextStepsCard(
        card: VerificationOutcomeNextStepsCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("값 없이 읽을 조건\n")
                append(card.outcomeCondition)
                append("\n\n다음 단계\n")
                append(card.nextStepSentence)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n기록하거나 공유하지 않을 것\n")
                card.doNotRecordOrShareBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openVerificationOutcomeNextStepsAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openVerificationOutcomeNextStepsAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openVerificationOutcomeNextStepsAction(action) }
        }
    }

    private fun openVerificationOutcomeNextStepsAction(
        action: VerificationOutcomeNextStepsAction
    ) {
        when (action.id) {
            VerificationOutcomeNextStepsActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            VerificationOutcomeNextStepsActionId.SEPARATE_CHANNEL_VERIFICATION_COACH ->
                show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            VerificationOutcomeNextStepsActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            VerificationOutcomeNextStepsActionId.CHECKER -> show(Screen.CHECKER)
            VerificationOutcomeNextStepsActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            VerificationOutcomeNextStepsActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            VerificationOutcomeNextStepsActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            VerificationOutcomeNextStepsActionId.SAFE_CALL_SCRIPTS ->
                show(Screen.SAFE_CALL_SCRIPTS)
            VerificationOutcomeNextStepsActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            VerificationOutcomeNextStepsActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            VerificationOutcomeNextStepsActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            VerificationOutcomeNextStepsActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            VerificationOutcomeNextStepsActionId.ACCOUNT_LOCKDOWN ->
                show(Screen.ACCOUNT_LOCKDOWN)
            VerificationOutcomeNextStepsActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            VerificationOutcomeNextStepsActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            VerificationOutcomeNextStepsActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
            VerificationOutcomeNextStepsActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            VerificationOutcomeNextStepsActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            VerificationOutcomeNextStepsActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)
            VerificationOutcomeNextStepsActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            VerificationOutcomeNextStepsActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
        }
    }

    private fun shareVerificationOutcomeNextStepsBoard(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 결과별 다음 단계 공유"))
    }

    private fun LinearLayout.separateChannelVerificationCoachScreen() {
        val coach = SeparateChannelVerificationCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("별도 확인 전 현금 인출·이동·전달 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("별도 확인 전 신분증·셀피 촬영·전송 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("별도 확인 전 링크·앱 설치 지시 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("별도 확인 전 상품권·코드 요구 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("회복 약속도 별도 확인 전에 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 사칭·압박을 별도 확인 전에 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("별도 확인 전 비밀값 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("새 번호·새 프로필 격리 원칙") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("익숙한 목소리를 믿기 전 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n지금 직접 할 추천 확인 행동\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.verificationSentence}"
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openSeparateChannelVerificationAction(action) }
            } else {
                secondaryButton(action.label) { openSeparateChannelVerificationAction(action) }
            }
        }

        section(
            "전체 수동 별도 경로 카드",
            "카드는 항상 같은 순서입니다. 현재 상대가 준 연락 수단은 멈추고 상황에 맞는 별도 " +
                "경로 종류와 기존 앱 화면만 직접 고르세요. 선택·읽음·확인 상태는 남지 않습니다."
        )
        coach.cards.forEach { card ->
            separateChannelVerificationCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 별도 경로 확인 안내 공유") {
            shareSeparateChannelVerificationCoach(coach.familyShareSummary)
        }
        secondaryButton("별도 확인 뒤 결과별 다음 단계") {
            show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
        }
        secondaryButton("다시 연락할 때 경계 문장 보기") {
            show(Screen.RECONTACT_BOUNDARY_COACH)
        }
        secondaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("진행 중 연락 종료 문장") {
            show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.separateChannelVerificationCard(
        card: SeparateChannelVerificationCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("직접 할 확인 행동\n")
                append(card.verificationSentence)
                append("\n\n언제 쓰나요?\n")
                append(card.whenToUse)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n사용하거나 공유하지 않을 것\n")
                card.doNotUseOrShareBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openSeparateChannelVerificationAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openSeparateChannelVerificationAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openSeparateChannelVerificationAction(action) }
        }
    }

    private fun openSeparateChannelVerificationAction(
        action: SeparateChannelVerificationAction
    ) {
        when (action.id) {
            SeparateChannelVerificationActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            SeparateChannelVerificationActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            SeparateChannelVerificationActionId.CHECKER -> show(Screen.CHECKER)
            SeparateChannelVerificationActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            SeparateChannelVerificationActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            SeparateChannelVerificationActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            SeparateChannelVerificationActionId.SAFE_CALL_SCRIPTS ->
                show(Screen.SAFE_CALL_SCRIPTS)
            SeparateChannelVerificationActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            SeparateChannelVerificationActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            SeparateChannelVerificationActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            SeparateChannelVerificationActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            SeparateChannelVerificationActionId.ACCOUNT_LOCKDOWN ->
                show(Screen.ACCOUNT_LOCKDOWN)
            SeparateChannelVerificationActionId.REMOTE_ACCESS_CUTOFF_COACH ->
                show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            SeparateChannelVerificationActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            SeparateChannelVerificationActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
            SeparateChannelVerificationActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            SeparateChannelVerificationActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareSeparateChannelVerificationCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 별도 경로 확인 안내 공유"))
    }

    private fun LinearLayout.remoteAccessCutoffCoachScreen() {
        val coach = RemoteAccessCutoffCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("원격 지시의 현금 인출·이동·전달도 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("원격 지시의 신분증·셀피 촬영·전송 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("원격 지시의 링크·앱 설치 단계 격리 점검") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("앱·화면 공유 중 상품권·코드 요구 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("회복을 돕는다는 앱 설치 약속 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 사칭·앱 설치 압박을 믿기 전 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("원격지시 중 비밀값 입력도 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("새 연락수단에서 원격지시를 이어가기 전 격리") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("통화 목소리를 믿기 전 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        section(
            "${coach.modeLabel} · ${coach.recommendationTitle}",
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n지금 직접 할 추천 중단 행동\n")
                append(
                    coach.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.cutoffSentence}"
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openRemoteAccessCutoffAction(action) }
            } else {
                secondaryButton(action.label) { openRemoteAccessCutoffAction(action) }
            }
        }

        section(
            "전체 수동 중단 카드",
            "카드는 항상 같은 순서입니다. 현재 통화·채팅·화면공유와 설치 지시를 사용자가 직접 멈춘 뒤, " +
                "민감한 계정 작업은 가능하면 별도 기기와 신뢰하는 보호자와 함께 진행하세요. " +
                "선택·읽음·진행 상태는 남지 않습니다."
        )
        coach.cards.forEach { card ->
            remoteAccessCutoffCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 원격지시 중단 안내 공유") {
            shareRemoteAccessCutoffCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("현재 연락과 분리해 확인 경로 고르기") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        secondaryButton("확인 뒤 결과별 다음 단계") {
            show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
        }
        secondaryButton("다시 연락할 때 경계 문장 보기") {
            show(Screen.RECONTACT_BOUNDARY_COACH)
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        secondaryButton("진행 중 연락 종료 문장") {
            show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.remoteAccessCutoffCard(
        card: RemoteAccessCutoffCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("직접 할 중단 행동\n")
                append(card.cutoffSentence)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n하지 않을 것\n")
                card.doNotDoBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openRemoteAccessCutoffAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openRemoteAccessCutoffAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openRemoteAccessCutoffAction(action) }
        }
    }

    private fun openRemoteAccessCutoffAction(action: RemoteAccessCutoffAction) {
        when (action.id) {
            RemoteAccessCutoffActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            RemoteAccessCutoffActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            RemoteAccessCutoffActionId.CHECKER -> show(Screen.CHECKER)
            RemoteAccessCutoffActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            RemoteAccessCutoffActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            RemoteAccessCutoffActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            RemoteAccessCutoffActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            RemoteAccessCutoffActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            RemoteAccessCutoffActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            RemoteAccessCutoffActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            RemoteAccessCutoffActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            RemoteAccessCutoffActionId.MONEY_TRANSFER_PAUSE_GATE ->
                show(Screen.MONEY_TRANSFER_PAUSE_GATE)
            RemoteAccessCutoffActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            RemoteAccessCutoffActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.REMOTE_ACCESS_CUTOFF_COACH)
            RemoteAccessCutoffActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            RemoteAccessCutoffActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            RemoteAccessCutoffActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)
            RemoteAccessCutoffActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
        }
    }

    private fun shareRemoteAccessCutoffCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 원격지시 중단 안내 공유"))
    }

    private fun LinearLayout.moneyTransferPauseGateScreen() {
        val gate = MoneyTransferPauseGateBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = gate.recommendedCardIds.toSet()

        title(gate.title)
        subtitle(gate.intro)
        primaryButton("현금 인출·직접 전달도 돈 행동처럼 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        primaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        primaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        primaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        primaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("대출·환급 명목의 신분증·셀피 요구 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        primaryButton("송금 링크·앱 설치 지시를 따르기 전 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        primaryButton("상품권·기프티콘·코드 요구도 송금처럼 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("환급·복구를 조건으로 돈을 요구하면 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 사칭·안전계좌·환급 압박 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        primaryButton("송금 전에 비밀값 전달도 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("새 연락수단에서 돈 이야기를 이어가기 전 격리") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("목소리 요청을 믿기 전 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        section(
            "${gate.modeLabel} · ${gate.recommendationTitle}",
            buildString {
                append(gate.recommendationExplanation)
                append("\n\n지금 직접 읽을 추천 멈춤 문장\n")
                append(
                    gate.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.pauseSentence}"
                    }
                )
                append("\n\n직접 고를 기존 앱 경로\n")
                append(
                    gate.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        gate.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openMoneyTransferPauseAction(action) }
            } else {
                secondaryButton(action.label) { openMoneyTransferPauseAction(action) }
            }
        }

        section(
            "전체 수동 멈춤 카드",
            "카드는 항상 같은 순서입니다. 결제나 금융정보 제공 전에 상황에 맞는 문장을 직접 읽고, 필요한 기존 앱 경로만 직접 고르세요. 선택·읽음·진행 상태는 남지 않습니다."
        )
        gate.cards.forEach { card ->
            moneyTransferPauseCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            gate.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 송금 멈춤 안내 공유") {
            shareMoneyTransferPauseGate(gate.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("현재 연락과 분리해 확인 경로 고르기") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        secondaryButton("확인 뒤 결과별 다음 단계") {
            show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
        }
        secondaryButton("다시 연락할 때 경계 문장 보기") {
            show(Screen.RECONTACT_BOUNDARY_COACH)
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        secondaryButton("화면공유·원격지시도 끊기") {
            show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
        }
        secondaryButton("진행 중 연락 종료 문장") {
            show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.moneyTransferPauseCard(
        card: MoneyTransferPauseCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("직접 읽을 멈춤 문장\n")
                append(card.pauseSentence)
                append("\n\n왜 중요한가요?\n")
                append(card.whyItMatters)
                append("\n\n하지 않을 것\n")
                card.doNotDoBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openMoneyTransferPauseAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openMoneyTransferPauseAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openMoneyTransferPauseAction(action) }
        }
    }

    private fun openMoneyTransferPauseAction(action: MoneyTransferPauseAction) {
        when (action.id) {
            MoneyTransferPauseActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            MoneyTransferPauseActionId.ACTIVE_CONTACT_EXIT_SCRIPTS ->
                show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            MoneyTransferPauseActionId.CHECKER -> show(Screen.CHECKER)
            MoneyTransferPauseActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            MoneyTransferPauseActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            MoneyTransferPauseActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            MoneyTransferPauseActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            MoneyTransferPauseActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            MoneyTransferPauseActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            MoneyTransferPauseActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            MoneyTransferPauseActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            MoneyTransferPauseActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.MONEY_TRANSFER_PAUSE_GATE)
            MoneyTransferPauseActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            MoneyTransferPauseActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            MoneyTransferPauseActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)
            MoneyTransferPauseActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
        }
    }

    private fun shareMoneyTransferPauseGate(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 송금 멈춤 안내 공유"))
    }

    private fun LinearLayout.activeContactExitScriptsScreen() {
        val scripts = ActiveContactExitScriptsBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = scripts.recommendedCardIds.toSet()

        title(scripts.title)
        subtitle(scripts.intro)
        primaryButton("현금 인출·만남·전달 지시 연락을 끝내고 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        primaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        primaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        primaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        primaryButton("링크·앱 설치 압박 연락을 끝내고 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        primaryButton("상품권·코드 요구 연락을 끝내고 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        primaryButton("회복 약속 연락을 이어가지 않고 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        primaryButton("기관 사칭·위협 압박을 믿지 않고 멈춤") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        primaryButton("연락을 끝내고 비밀값 전달 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        primaryButton("새 번호·새 프로필을 믿기 전 격리") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        primaryButton("화면공유·원격지시 중단 코치") {
            show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
        }
        primaryButton("송금 전 멈춤 확인") { show(Screen.MONEY_TRANSFER_PAUSE_GATE) }
        primaryButton("현재 연락과 분리해 확인 경로 고르기") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        primaryButton("연락을 멈춘 뒤 결과별 다음 단계") {
            show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
        }
        primaryButton("연락을 멈춘 뒤 재연락 경계 정하기") {
            show(Screen.RECONTACT_BOUNDARY_COACH)
        }
        section(
            "${scripts.modeLabel} · ${scripts.recommendationTitle}",
            buildString {
                append(scripts.recommendationExplanation)
                append("\n\n지금 직접 읽을 추천 문장\n")
                append(
                    scripts.recommendedCards.joinToString("\n\n") { card ->
                        "• ${card.title}\n${card.scriptText}"
                    }
                )
                append("\n\n연락을 끝낸 뒤 이어갈 기존 앱 경로\n")
                append(
                    scripts.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        scripts.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openActiveContactExitScriptAction(action) }
            } else {
                secondaryButton(action.label) { openActiveContactExitScriptAction(action) }
            }
        }

        section(
            "전체 수동 종료 문장",
            "카드는 항상 같은 순서입니다. 지금 상황에 맞는 짧은 문장을 사용자가 직접 읽고 연락을 끝낸 뒤, 필요한 기존 앱 경로만 직접 고르세요. 읽음이나 선택 상태는 남지 않습니다."
        )
        scripts.cards.forEach { card ->
            activeContactExitScriptCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·사용 안내",
            scripts.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 종료 문장 안내 공유") {
            shareActiveContactExitScripts(scripts.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.activeContactExitScriptCard(
        card: ActiveContactExitScriptCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("직접 읽을 문장\n")
                append(card.scriptText)
                append("\n\n언제 쓰나요?\n")
                append(card.whenToUse)
                append("\n\n말하지 않거나 하지 않을 것\n")
                card.neverSayOrDo.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openActiveContactExitScriptAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openActiveContactExitScriptAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openActiveContactExitScriptAction(action) }
        }
    }

    private fun openActiveContactExitScriptAction(action: ActiveContactExitScriptAction) {
        when (action.id) {
            ActiveContactExitScriptActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            ActiveContactExitScriptActionId.CHECKER -> show(Screen.CHECKER)
            ActiveContactExitScriptActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            ActiveContactExitScriptActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            ActiveContactExitScriptActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            ActiveContactExitScriptActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            ActiveContactExitScriptActionId.SAFE_CALL_SCRIPTS ->
                show(Screen.SAFE_CALL_SCRIPTS)
            ActiveContactExitScriptActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            ActiveContactExitScriptActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            ActiveContactExitScriptActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            ActiveContactExitScriptActionId.ACCOUNT_LOCKDOWN ->
                show(Screen.ACCOUNT_LOCKDOWN)
            ActiveContactExitScriptActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            ActiveContactExitScriptActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
            ActiveContactExitScriptActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            ActiveContactExitScriptActionId.INCIDENT_TIMELINE_BRIEFING ->
                show(Screen.TIMELINE_BRIEFING)
            ActiveContactExitScriptActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            ActiveContactExitScriptActionId.FAMILY_CALM_DEBRIEF_AGENDA ->
                show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
        }
    }

    private fun shareActiveContactExitScripts(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 종료 문장 안내 공유"))
    }

    private fun LinearLayout.safePastePrivacyCoachScreen() {
        val coach = SafePastePrivacyCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = if (incidents.hasSavedIncidents()) 1 else 0
        )
        val recommendedIds = coach.recommendedCardIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        secondaryButton("신분증·셀피 촬영·전송 요구도 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("링크를 붙여넣거나 열지 않고 설치 지시 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("상품권·코드 값을 붙여넣지 않고 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("회복 약속에 값을 붙여넣기 전 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 사칭·링크·문서 압박 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("비밀값은 붙여넣지 않고 먼저 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        section(
            coach.modeLabel,
            "이 코치 화면은 의심 내용을 받거나 읽지 않고 자동으로 바꾸지 않습니다. " +
                "아래 순서를 먼저 읽고, 붙여넣기 전 사용자가 직접 필요한 부분만 가리세요."
        )
        secondaryButton("연락 중 압박을 끝내는 문장") {
            show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
        }
        section(
            coach.recommendationTitle,
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 확인할 가리기 카드\n")
                append(
                    coach.recommendedCards.joinToString("\n") { card ->
                        "• ${card.displayOrder}. ${card.title}"
                    }
                )
                append("\n\n먼저 이어갈 기존 앱 경로\n")
                append(
                    coach.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openSafePastePrivacyCoachAction(action) }
            } else {
                secondaryButton(action.label) { openSafePastePrivacyCoachAction(action) }
            }
        }

        section(
            "전체 수동 가리기 체크리스트",
            "카드는 항상 같은 순서로 표시됩니다. 추천 표시는 현재 로컬 위험 신호, 값 없는 가족 준비 수준, 저장 사건 유무만 반영하며 읽음이나 진행 상태를 남기지 않습니다."
        )
        coach.cards.forEach { card ->
            safePastePrivacyCoachCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·안전 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 개인정보 가리기 안내 공유") {
            shareSafePastePrivacyCoach(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        secondaryButton("의심 내용 검사로") { show(Screen.CHECKER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.safePastePrivacyCoachCard(
        card: SafePastePrivacyCoachCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("왜 필요한가요?\n")
                append(card.whyItMatters)
                append("\n\n직접 가리는 방법\n")
                append(card.howToMaskManually)
                append("\n\n붙여넣거나 공유하지 마세요\n")
                card.whatNotToPasteOrShare.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openSafePastePrivacyCoachAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openSafePastePrivacyCoachAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openSafePastePrivacyCoachAction(action) }
        }
    }

    private fun openSafePastePrivacyCoachAction(action: SafePastePrivacyCoachAction) {
        when (action.id) {
            SafePastePrivacyCoachActionId.CHECKER -> show(Screen.CHECKER)
            SafePastePrivacyCoachActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            SafePastePrivacyCoachActionId.SAMPLE_LIBRARY -> openSampleLibrary()
            SafePastePrivacyCoachActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            SafePastePrivacyCoachActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            SafePastePrivacyCoachActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            SafePastePrivacyCoachActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            SafePastePrivacyCoachActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            SafePastePrivacyCoachActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            SafePastePrivacyCoachActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            SafePastePrivacyCoachActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            SafePastePrivacyCoachActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            SafePastePrivacyCoachActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            SafePastePrivacyCoachActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.SAFE_PASTE_PRIVACY_COACH)
            SafePastePrivacyCoachActionId.INCIDENT_TIMELINE_BRIEFING ->
                show(Screen.TIMELINE_BRIEFING)
            SafePastePrivacyCoachActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
        }
    }

    private fun shareSafePastePrivacyCoach(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 개인정보 가리기 안내 공유"))
    }

    private fun LinearLayout.familyCalmDebriefAgendaScreen() {
        val agenda = FamilyCalmDebriefAgendaBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = agenda.recommendedCardIds.toSet()

        title(agenda.title)
        subtitle(agenda.intro)
        secondaryButton("현금 인출·이동·전달 지시를 숨기지 않고 가족과 점검") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("신분증·셀피 요구를 숨기지 않고 가족과 점검") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("링크·앱 설치 압박을 가족과 값 없이 점검") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("상품권·코드 요구를 가족과 값 없이 점검") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("회복 약속을 가족과 함께 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 사칭·압박을 가족과 숨기지 않고 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("가족 대화에도 비밀값 없이 멈춤 순서 공유") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("새 연락수단 격리 약속 함께 보기") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("가족 대화 전 목소리 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        section(
            agenda.durationLabel,
            "타이머나 기록 없이 아래 순서를 가족이 직접 읽고, 필요한 기존 앱 경로만 골라 이어가세요."
        )
        secondaryButton("다시 연락 중이면 종료 문장 보기") {
            show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
        }
        secondaryButton("별도 확인 뒤 결과별 다음 단계") {
            show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
        }
        secondaryButton("다시 연락할 때 경계 문장 정하기") {
            show(Screen.RECONTACT_BOUNDARY_COACH)
        }
        section(
            agenda.recommendationTitle,
            buildString {
                append(agenda.recommendationExplanation)
                append("\n\n먼저 이야기할 안건\n")
                append(
                    agenda.recommendedCards.joinToString("\n") { card ->
                        "• ${card.title} · ${card.category.label}"
                    }
                )
                append("\n\n먼저 이어갈 앱 경로\n")
                append(
                    agenda.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        agenda.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openFamilyCalmDebriefAction(action) }
            } else {
                secondaryButton(action.label) { openFamilyCalmDebriefAction(action) }
            }
        }

        section(
            "전체 5분 대화 순서",
            "여섯 안건은 항상 같은 순서로 한 번씩 표시됩니다. 현재 로컬 분석 신호, 값 없는 가족 준비 수준, 저장 사건 유무에 맞는 항목만 추천으로 표시하며 대화 답변이나 진행 상태는 남지 않습니다."
        )
        agenda.agendaCards.forEach { card ->
            familyCalmDebriefCard(card, recommended = card.id in recommendedIds)
        }

        section(
            "개인정보·안전 안내",
            agenda.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 차분 대화 순서 공유") {
            shareFamilyCalmDebriefAgenda(agenda.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        secondaryButton("큰글씨·읽어주기 접근성 코치") { show(Screen.ACCESSIBILITY_COACH) }
        secondaryButton("사건 뒤 가족 점검 달력") {
            show(Screen.POST_INCIDENT_REVIEW_CALENDAR)
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.familyCalmDebriefCard(
        card: FamilyCalmDebriefCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.displayOrder)
            append(". ")
            append(card.title)
            append(" · ")
            append(card.category.label)
        }
        section(
            heading,
            buildString {
                append("함께 말하거나 할 일\n")
                append(card.whatToSayOrDo)
                append("\n\n묻거나 저장·공유하지 않을 것\n")
                card.whatNotToAskStoreOrShare.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openFamilyCalmDebriefAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openFamilyCalmDebriefAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openFamilyCalmDebriefAction(action) }
        }
    }

    private fun openFamilyCalmDebriefAction(action: FamilyCalmDebriefAction) {
        when (action.id) {
            FamilyCalmDebriefActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            FamilyCalmDebriefActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            FamilyCalmDebriefActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            FamilyCalmDebriefActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            FamilyCalmDebriefActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            FamilyCalmDebriefActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
            FamilyCalmDebriefActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            FamilyCalmDebriefActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            FamilyCalmDebriefActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            FamilyCalmDebriefActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            FamilyCalmDebriefActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            FamilyCalmDebriefActionId.FAMILY_RESPONSE_ROLE_BOARD ->
                show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            FamilyCalmDebriefActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            FamilyCalmDebriefActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)
            FamilyCalmDebriefActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            FamilyCalmDebriefActionId.INCIDENT_TIMELINE_BRIEFING ->
                show(Screen.TIMELINE_BRIEFING)
            FamilyCalmDebriefActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            FamilyCalmDebriefActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
            FamilyCalmDebriefActionId.CHECKER -> show(Screen.CHECKER)
            FamilyCalmDebriefActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
        }
    }

    private fun shareFamilyCalmDebriefAgenda(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 차분 대화 순서 공유"))
    }

    private fun LinearLayout.familyResponseRoleBoardScreen() {
        val board = FamilyResponseRoleBoardBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = board.recommendedRoleIds.toSet()

        title(board.title)
        subtitle(board.intro)
        secondaryButton("현금 인출·전달을 멈추는 가족 역할 함께 보기") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("신분증·셀피 요구 대응 역할 함께 보기") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("링크·앱 설치 압박 대응 역할 전에 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("상품권·코드 요구 대응 역할 전에 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("회복 약속에 대응하기 전 역할별 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 사칭·압박 대응 역할 전 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("비밀값 없이 대응 역할 고르기") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("새 연락수단 격리 역할 함께 보기") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("목소리를 증거로 믿지 않는 역할 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        section(
            board.recommendationTitle,
            buildString {
                append(board.recommendationExplanation)
                append("\n\n먼저 나눌 수동 역할\n")
                append(
                    board.recommendedRoles.joinToString("\n") { card ->
                        "• ${card.roleLabel} · ${card.category.label} · ${card.priority.label}"
                    }
                )
                append("\n\n먼저 이어갈 앱 행동\n")
                append(
                    board.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        board.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openFamilyResponseRoleAction(action) }
            } else {
                secondaryButton(action.label) { openFamilyResponseRoleAction(action) }
            }
        }

        section(
            "추천 가족 역할",
            "이름을 배정하거나 저장하지 않고, 지금 필요한 일반 역할을 가족이 말로 나눠 맡으세요. 역할 선택과 진행 상태는 남지 않습니다."
        )
        board.recommendedRoles.forEach { card ->
            familyResponseRoleCard(card, recommended = true)
        }

        val remainingRoles = board.roleCards.filterNot { card -> card.id in recommendedIds }
        if (remainingRoles.isNotEmpty()) {
            section(
                "나머지 가족 역할",
                "추천과 관계없이 필요한 역할을 직접 살펴보세요. 어떤 가족이 맡았는지 앱에 입력하거나 기록하지 않습니다."
            )
            remainingRoles.forEach { card ->
                familyResponseRoleCard(card, recommended = false)
            }
        }

        section(
            "개인정보·안전 안내",
            board.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        secondaryButton("역할을 나눈 뒤 5분 가족 대화") {
            show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
        }
        secondaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        primaryButton("가족에게 역할 보드 공유") {
            shareFamilyResponseRoleBoard(board.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.familyResponseRoleCard(
        card: FamilyResponseRoleCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.roleLabel)
            append(" · ")
            append(card.category.label)
            append(" · ")
            append(card.priority.label)
        }
        section(
            heading,
            buildString {
                append("역할 임무\n")
                append(card.mission)
                append("\n\n안전한 인계 문장\n")
                append(card.safeHandoffSentence)
                append("\n\n언제 맡나요?\n")
                append(card.whenToUse)
                append("\n\n하지 마세요\n")
                card.doNotDoBoundaries.forEach { boundary ->
                    append("• ").append(boundary).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openFamilyResponseRoleAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openFamilyResponseRoleAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openFamilyResponseRoleAction(action) }
        }
    }

    private fun openFamilyResponseRoleAction(action: FamilyResponseRoleAction) {
        when (action.id) {
            FamilyResponseRoleActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            FamilyResponseRoleActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            FamilyResponseRoleActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            FamilyResponseRoleActionId.TRUSTED_HELPER_HANDOFF ->
                show(Screen.TRUSTED_HELPER_HANDOFF)
            FamilyResponseRoleActionId.CHECKER -> show(Screen.CHECKER)
            FamilyResponseRoleActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            FamilyResponseRoleActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            FamilyResponseRoleActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            FamilyResponseRoleActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            FamilyResponseRoleActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            FamilyResponseRoleActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            FamilyResponseRoleActionId.FOLLOW_UP_CHECK_IN_PLANNER ->
                show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            FamilyResponseRoleActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)
            FamilyResponseRoleActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            FamilyResponseRoleActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            FamilyResponseRoleActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.FAMILY_RESPONSE_ROLE_BOARD)
            FamilyResponseRoleActionId.REPORT_PREP -> show(Screen.REPORT_PREP)
            FamilyResponseRoleActionId.INCIDENT_TIMELINE_BRIEFING ->
                show(Screen.TIMELINE_BRIEFING)
            FamilyResponseRoleActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            FamilyResponseRoleActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
        }
    }

    private fun shareFamilyResponseRoleBoard(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 역할 보드 공유"))
    }

    private fun LinearLayout.onboarding() {
        title("가족 AI 보이스피싱 방패")
        subtitle("의심 문자, 카카오톡, 통화 녹취를 붙여넣으면 AI 보호자 모드가 기기 안에서 위험 신호와 확인 행동을 정리합니다.")
        section(
            "시작 전 동의",
            "이 앱은 가족의 동의와 대화를 돕는 도구입니다.\n" +
                "SMS, 통화, 연락처를 자동으로 읽지 않으며 서버로 전송하지 않습니다.\n" +
                "민감정보는 붙여넣기 전에 가려도 됩니다."
        )

        val consentFamily = CheckBox(this@MainActivity).apply {
            text = "가족 동의 없이 감시하거나 몰래 확인하지 않겠습니다."
            textSize = 15f
        }
        val consentLocal = CheckBox(this@MainActivity).apply {
            text = "분석은 참고용이며 송금 전 가족에게 직접 확인하겠습니다."
            textSize = 15f
        }
        addView(consentFamily)
        addView(consentLocal)
        primaryButton("동의하고 시작") {
            if (consentFamily.isChecked && consentLocal.isChecked) {
                prefs.edit().putBoolean(KEY_CONSENTED, true).apply()
                show(if (pendingSharedText.isNullOrBlank()) Screen.HOME else Screen.CHECKER)
            } else {
                toast("두 항목에 동의해야 시작할 수 있습니다.")
            }
        }
    }

    private fun LinearLayout.home() {
        if (simpleMode.isOn()) {
            simpleModeHome()
            return
        }
        secondaryButton("큰 버튼 단순 모드로 보기") {
            simpleMode.setOn(true)
            show(Screen.HOME)
        }
        title("가족 AI 보호자 대기 중")
        subtitle("돈, 앱 설치, 인증번호 요청은 먼저 멈추고 AI 보호자 분석 → 가족 콜백 → 신고/차단 순서로 처리하세요.")
        primaryButton("현금 인출·만남·직접 전달 전에 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        primaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        primaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        primaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        primaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        primaryButton("신분증·셀피 촬영·전송 전에 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        primaryButton("사건 화면을 캡처해 공유하기 전 개인정보 가드") {
            show(Screen.SCREENSHOT_PRIVACY_GUARD_COACH)
        }
        primaryButton("링크를 열거나 앱을 설치하기 전 격리 점검") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        primaryButton("상품권·기프티콘·코드 요구를 따르기 전 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        primaryButton("회복·환급·취소 약속을 믿기 전 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        primaryButton("경찰·검찰·은행·배송 사칭 압박 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        primaryButton("OTP·PIN·비밀번호를 주기 전 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        primaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        primaryButton("새 번호·새 프로필을 저장하기 전 격리") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        primaryButton("익숙한 목소리를 믿기 전 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        primaryButton("화면공유·원격지시 끊기") {
            show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
        }
        primaryButton("송금 전 멈춤 확인") { show(Screen.MONEY_TRANSFER_PAUSE_GATE) }
        primaryButton("지금 연락 끝내는 문장") { show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS) }
        primaryButton("현재 연락과 분리해 확인 경로 고르기") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        primaryButton("별도 확인 뒤 결과별 다음 단계") {
            show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
        }
        primaryButton("다시 연락할 때 경계 정하기") {
            show(Screen.RECONTACT_BOUNDARY_COACH)
        }
        primaryButton("지금 뭐부터 할지 고르기") { show(Screen.FLOW_ROUTER) }
        secondaryButton("무엇이 노출됐는지 고르기") {
            show(Screen.EXPOSURE_IMPACT_TRIAGE)
        }
        secondaryButton("24시간 수습 계획") { show(Screen.RECOVERY_PLAN) }
        secondaryButton("며칠 뒤 다시 확인할 것") {
            show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
        }
        secondaryButton("계정·기기 잠금 체크리스트") { show(Screen.ACCOUNT_LOCKDOWN) }
        secondaryButton("가족 경고 메시지 만들기") { show(Screen.FAMILY_ALERT_MESSAGES) }
        secondaryButton("보호자 도움 요청 카드") { show(Screen.TRUSTED_HELPER_HANDOFF) }
        secondaryButton("보호자와 함께 안전 검토") { show(Screen.TRUSTED_HELPER_REVIEW) }
        secondaryButton("가족 역할 나눠 대응하기") { show(Screen.FAMILY_RESPONSE_ROLE_BOARD) }
        secondaryButton("5분 가족 차분 대화하기") {
            show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
        }
        secondaryButton("안전 확인 전화 스크립트") { show(Screen.SAFE_CALL_SCRIPTS) }
        section("개인정보 보호", "분석은 로컬 규칙 기반 AI 보호자 엔진으로만 진행됩니다. 네트워크 권한, SMS 권한, 통화 권한을 요청하지 않습니다.")
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        secondaryButton("붙여넣기 전 개인정보 가리기") {
            show(Screen.SAFE_PASTE_PRIVACY_COACH)
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        secondaryButton("큰글씨·읽어주기 접근성 코치") { show(Screen.ACCESSIBILITY_COACH) }
        secondaryButton("사건 뒤 가족 점검 달력") {
            show(Screen.POST_INCIDENT_REVIEW_CALENDAR)
        }
        readinessSection()
        primaryButton("의심 메시지/통화 검사") { show(Screen.CHECKER) }
        secondaryButton("위험 신호 설명 보기") { show(Screen.RISK_SIGNAL_EXPLAINERS) }
        secondaryButton("가족 콜백 루틴 확인") { openFamilyCallbackCard(null) }
        secondaryButton("신고용 상담 준비 카드") { show(Screen.REPORT_PREP) }
        secondaryButton("샘플로 AI 분석 연습") { openSampleLibrary() }
        secondaryButton("이미 피해가 의심돼요 / 긴급 대응 센터") { openResponseCenter() }
        secondaryButton("신고·상담 연락처 보기") { openHelpDirectory(Screen.HOME) }
        secondaryButton("증거 보존 체크리스트") { openEvidenceKit(null) }
        secondaryButton("사건 타임라인 요약") { show(Screen.TIMELINE_BRIEFING) }
        secondaryButton("사건 기록 보기") { show(Screen.LOG) }
        section("AI 방지 루프", "1. 수상한 요청을 붙여넣기\n2. 위험 신호와 즉시 중단 행동 확인\n3. 기존 저장 번호로 가족 콜백\n4. 필요하면 112/1394 또는 은행 지급정지")
    }

    private fun LinearLayout.simpleModeHome() {
        title("단순 모드")
        subtitle("가장 많이 쓰는 세 가지만 큰 버튼으로 보여줍니다. 자세한 화면이 필요하면 아래에서 일반 모드로 돌아가세요.")
        giantButton("수상한 문자 / 통화 분석") { show(Screen.CHECKER) }
        giantButton("지금 가족에게 전화") { openFamilyCallbackCard(currentAnalysis) }
        giantButton("긴급 신고") { openHelpDirectory(Screen.HOME) }
        secondaryButton("큰글씨·읽어주기 접근성 코치") { show(Screen.ACCESSIBILITY_COACH) }
        secondaryButton("일반 모드로 돌아가기") {
            simpleMode.setOn(false)
            show(Screen.HOME)
        }
    }

    private fun LinearLayout.quickStopModeScreen() {
        val mode = QuickStopModeBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load())
        )
        val recommendedIds = mode.recommendedCardIds.toSet()

        title(mode.title)
        subtitle(mode.intro)
        primaryButton("현금 인출·이동·직접 전달 즉시 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        primaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        primaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        primaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        primaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        primaryButton("신분증·셀피 촬영·전송 요구 즉시 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        primaryButton("링크·앱 설치 압박 즉시 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        primaryButton("상품권·기프티콘·코드를 사거나 보내기 전 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        primaryButton("회복 약속을 믿거나 비용을 내기 전에 멈춤") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        primaryButton("기관 사칭·위협 압박을 증거로 믿지 않고 멈춤") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        primaryButton("비밀값을 말하거나 입력하기 전 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        primaryButton("새 번호·새 프로필을 믿기 전 격리") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        primaryButton("목소리를 증거로 믿기 전 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        section(
            mode.stopScriptTitle,
            mode.stopScriptSteps.joinToString("\n") { step ->
                "${step.displayOrder}. ${step.instruction}"
            }
        )
        primaryButton("화면공유·원격지시 중단 코치") {
            show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
        }
        primaryButton("송금·결제 전에 멈춤 확인") {
            show(Screen.MONEY_TRANSFER_PAUSE_GATE)
        }
        primaryButton("압박 연락을 끝내는 문장 보기") {
            show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
        }
        primaryButton("현재 연락과 분리해 확인 경로 고르기") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        primaryButton("멈춘 뒤 결과별 다음 단계") {
            show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
        }
        primaryButton("멈춘 뒤 재연락 경계 정하기") {
            show(Screen.RECONTACT_BOUNDARY_COACH)
        }
        primaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        section(
            mode.recommendationTitle,
            buildString {
                append(mode.recommendationExplanation)
                append("\n\n추천 행동 카드\n")
                append(
                    mode.recommendedCards.joinToString("\n") { card ->
                        "• ${card.label} · ${card.urgency.label}"
                    }
                )
            }
        )
        mode.recommendedCards.forEach { card ->
            primaryButton("추천 · ${card.primaryAction.label}") {
                openQuickStopAction(card.primaryAction)
            }
        }

        section(
            "모든 행동 카드",
            "추천 표시와 관계없이 본인 상황에 해당하는 버튼만 직접 고르세요. 선택은 저장되지 않습니다."
        )
        mode.cards.forEach { card ->
            val recommended = card.id in recommendedIds
            val heading = buildString {
                if (recommended) append("추천 · ")
                append(card.label)
                append(" · ")
                append(card.urgency.label)
            }
            section(
                heading,
                buildString {
                    append(card.explanation)
                    append("\n\n안전 메모\n")
                    append(card.safetyNotes.joinToString("\n") { note -> "• $note" })
                }
            )
            if (recommended) {
                primaryButton(card.primaryAction.label) {
                    openQuickStopAction(card.primaryAction)
                }
            } else {
                secondaryButton(card.primaryAction.label) {
                    openQuickStopAction(card.primaryAction)
                }
            }
            card.secondaryActions.forEach { action ->
                secondaryButton(action.label) { openQuickStopAction(action) }
            }
        }

        section(
            "개인정보·안전 안내",
            mode.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        secondaryButton("멈춘 뒤 5분 가족 대화") {
            show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
        }
        primaryButton("가족에게 멈춤 안내 공유") {
            shareQuickStopSummary(mode.familyShareSummary)
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun openQuickStopAction(action: QuickStopAction) {
        when (action.id) {
            QuickStopActionId.STOP_SCRIPT -> show(Screen.QUICK_STOP)
            QuickStopActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            QuickStopActionId.SAFETY_PLAN -> show(Screen.PLAN)
            QuickStopActionId.CHECKER -> show(Screen.CHECKER)
            QuickStopActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            QuickStopActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            QuickStopActionId.RESPONSE_CENTER -> openResponseCenter()
            QuickStopActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            QuickStopActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.QUICK_STOP)
            QuickStopActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            QuickStopActionId.FAMILY_ALERT_MESSAGES -> show(Screen.FAMILY_ALERT_MESSAGES)
        }
    }

    private fun LinearLayout.safetyFlowRouterScreen() {
        val router = SafetyFlowRouterBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load())
        )

        title(router.title)
        subtitle(router.intro)
        primaryButton("현금 인출·직접 전달 멈춤 경로") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        primaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        primaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        primaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        primaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        primaryButton("신분증·셀피 촬영·전송 요구 멈춤 경로") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        primaryButton("링크·앱 설치 격리 경로") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        primaryButton("상품권·코드 요구 멈춤 경로") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        primaryButton("회복 약속 현실 점검 경로") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        primaryButton("기관 사칭·압박 현실 점검 경로") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        primaryButton("비밀값 전달 전 멈춤 경로") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        primaryButton("새 번호·새 프로필 격리 순서") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        primaryButton("익숙한 목소리를 믿기 전 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        primaryButton("화면공유·원격지시 끊기") {
            show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
        }
        primaryButton("송금·결제 전에 멈춤 확인") {
            show(Screen.MONEY_TRANSFER_PAUSE_GATE)
        }
        primaryButton("연락 중 압박을 끝내는 문장") {
            show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
        }
        primaryButton("현재 연락과 분리해 확인 경로 고르기") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        primaryButton("확인 뒤 결과별 다음 단계") {
            show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
        }
        primaryButton("다시 연락할 때 경계 정하기") {
            show(Screen.RECONTACT_BOUNDARY_COACH)
        }
        primaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        section(
            router.recommendationTitle,
            buildString {
                append(router.recommendationExplanation)
                append("\n\n")
                append(router.recommendedRoutes.joinToString("\n") { route -> "• ${route.label}" })
            }
        )

        router.routes.forEach { route ->
            section(
                "${route.label} · ${route.urgency.label}",
                buildString {
                    append(route.explanation)
                    append("\n\n안전 메모\n")
                    append(route.safetyNotes.joinToString("\n") { note -> "• $note" })
                }
            )
            primaryButton(route.primaryAction.label) {
                openSafetyFlowAction(route.primaryAction)
            }
            route.secondaryActions.forEach { action ->
                secondaryButton(action.label) { openSafetyFlowAction(action) }
            }
        }

        section(
            "개인정보·안전 안내",
            router.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        secondaryButton("붙여넣기 전 개인정보 가리기") {
            show(Screen.SAFE_PASTE_PRIVACY_COACH)
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        secondaryButton("큰글씨·읽어주기 접근성 코치") { show(Screen.ACCESSIBILITY_COACH) }
        primaryButton("가족에게 안전 흐름 요약 공유") {
            shareSafetyFlowRouter(router.familyShareSummary)
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun openSafetyFlowAction(action: SafetyFlowAction) {
        when (action.id) {
            SafetyFlowActionId.CHECKER -> show(Screen.CHECKER)
            SafetyFlowActionId.ANALYSIS_RESULT -> {
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            }
            SafetyFlowActionId.FAMILY_CALLBACK_CARD -> openFamilyCallbackCard(currentAnalysis)
            SafetyFlowActionId.SAFETY_PLAN -> show(Screen.PLAN)
            SafetyFlowActionId.RESPONSE_CENTER -> openResponseCenter()
            SafetyFlowActionId.REPORT_PREP -> show(Screen.REPORT_PREP)
            SafetyFlowActionId.OFFICIAL_HELP_DIRECTORY -> openHelpDirectory(Screen.FLOW_ROUTER)
            SafetyFlowActionId.EVIDENCE_KIT -> openEvidenceKit(action.evidenceSituation)
            SafetyFlowActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
        }
    }

    private fun LinearLayout.familyAlertMessagePackScreen() {
        val pack = FamilyAlertMessagePackBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load())
        )
        val recommendedIds = pack.recommendedTemplateIds.toSet()

        title(pack.title)
        subtitle(pack.intro)
        secondaryButton("현금 인출·이동·전달 지시를 가족과 값 없이 점검") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("신분증·셀피 요구를 가족과 값 없이 점검") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("링크·앱 설치 압박을 가족과 값 없이 점검") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("상품권·코드 요구를 가족과 값 없이 점검") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("회복 약속을 가족과 값 없이 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 사칭·비밀 유지 압박을 가족과 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("가족에게 비밀값 대신 행동만 알리기") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("새 연락수단 값 없이 격리 안내 보기") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("가족에게 알리기 전 목소리 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        section(
            pack.recommendationTitle,
            buildString {
                append(pack.recommendationExplanation)
                append("\n\n")
                append(
                    pack.recommendedTemplates.joinToString("\n") { template ->
                        "• ${template.title} · ${template.urgency.label}"
                    }
                )
            }
        )

        pack.templates.forEach { template ->
            val recommended = template.id in recommendedIds
            val heading = buildString {
                if (recommended) append("추천 · ")
                append(template.title)
                append(" · ")
                append(template.urgency.label)
            }
            section(
                heading,
                buildString {
                    append("받는 사람\n")
                    append(template.audience)
                    append("\n\n언제 쓰나요?\n")
                    append(template.whenToUse)
                    append("\n\n공유할 메시지\n")
                    append(template.messageBody)
                    append("\n\n보낸 뒤 확인\n")
                    template.followUpChecklist.forEach { item ->
                        append("• ").append(item).append('\n')
                    }
                }.trim()
            )
            if (recommended) {
                primaryButton("${template.title} 직접 공유") {
                    shareFamilyAlertText(template.messageBody)
                }
            } else {
                secondaryButton("${template.title} 직접 공유") {
                    shareFamilyAlertText(template.messageBody)
                }
            }
            template.actions.forEach { action ->
                secondaryButton(action.label) { openFamilyAlertAction(action) }
            }
        }

        section(
            "개인정보·안전 안내",
            pack.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        secondaryButton("확인 뒤 결과별 다음 단계") {
            show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
        }
        secondaryButton("다시 연락할 때 경계 문장 정하기") {
            show(Screen.RECONTACT_BOUNDARY_COACH)
        }
        secondaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        primaryButton("추천 메시지 묶음 직접 공유") {
            shareFamilyAlertText(pack.combinedShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun openFamilyAlertAction(action: FamilyAlertAction) {
        when (action.id) {
            FamilyAlertActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            FamilyAlertActionId.SAFETY_PLAN -> show(Screen.PLAN)
            FamilyAlertActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            FamilyAlertActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            FamilyAlertActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            FamilyAlertActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.FAMILY_ALERT_MESSAGES)
            FamilyAlertActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            FamilyAlertActionId.NEW_ANALYSIS -> show(Screen.CHECKER)
        }
    }

    private fun LinearLayout.accountLockdownChecklistScreen() {
        val checklist = AccountLockdownChecklistBuilder.build(currentAnalysis)
        val recommendedIds = checklist.recommendedSectionIds.toSet()

        title(checklist.title)
        subtitle(checklist.intro)
        secondaryButton("계정 대응과 현금 인출·이동·전달 지시를 분리해 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("계정 보호 명목의 신분증·셀피 요구 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("계정·기기 조치 전에 링크·앱 설치 지시 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("계정·기기 조치 전에 상품권·코드 요구 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("회복을 조건으로 계정 조치를 요구하면 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 사칭·계정 보호 압박을 믿기 전 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("계정 조치 전에 비밀값 전달 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("새 연락수단을 믿기 전 격리") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("목소리 요청을 믿기 전 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        secondaryButton("화면공유·원격지시 먼저 끊기") {
            show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
        }
        secondaryButton("송금·결제 전에 멈춤 확인") {
            show(Screen.MONEY_TRANSFER_PAUSE_GATE)
        }
        secondaryButton("현재 연락과 분리해 확인 경로 고르기") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        section(
            checklist.recommendationTitle,
            buildString {
                append(checklist.recommendationExplanation)
                append("\n\n")
                append(
                    checklist.recommendedSections.joinToString("\n") { section ->
                        "• ${section.title} · ${section.urgency.label}"
                    }
                )
            }
        )

        checklist.sections.forEach { section ->
            val recommended = section.id in recommendedIds
            val heading = buildString {
                if (recommended) append("추천 · ")
                append(section.title)
                append(" · ")
                append(section.urgency.label)
            }
            section(
                heading,
                buildString {
                    append("왜 확인하나요?\n")
                    append(section.whyItMatters)
                    append("\n\n직접 할 일\n")
                    section.manualSteps.forEachIndexed { index, step ->
                        append(index + 1).append(". ").append(step).append('\n')
                    }
                    append("\n하지 마세요\n")
                    section.doNotDoNotes.forEach { note ->
                        append("• ").append(note).append('\n')
                    }
                }.trim()
            )
            section.actions.forEachIndexed { index, action ->
                if (recommended && index == 0) {
                    primaryButton(action.label) { openAccountLockdownAction(action) }
                } else {
                    secondaryButton(action.label) { openAccountLockdownAction(action) }
                }
            }
        }

        section(
            "개인정보·안전 안내",
            checklist.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 잠금 체크리스트 공유") {
            shareAccountLockdownChecklist(checklist.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun openAccountLockdownAction(action: AccountLockdownAction) {
        when (action.id) {
            AccountLockdownActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            AccountLockdownActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            AccountLockdownActionId.RESPONSE_CENTER ->
                openResponseCenter(action.responseSituation)
            AccountLockdownActionId.EVIDENCE_KIT -> openEvidenceKit(action.evidenceSituation)
            AccountLockdownActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.ACCOUNT_LOCKDOWN)
            AccountLockdownActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            AccountLockdownActionId.NEW_ANALYSIS -> show(Screen.CHECKER)
        }
    }

    private fun LinearLayout.recoveryPlan24hScreen() {
        val plan = RecoveryPlan24hBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load())
        )
        val recommendedIds = plan.recommendedWindowIds.toSet()

        title(plan.title)
        subtitle(plan.intro)
        secondaryButton("화면공유·원격지시 먼저 끊기") {
            show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
        }
        secondaryButton("송금 전 멈춤 확인") { show(Screen.MONEY_TRANSFER_PAUSE_GATE) }
        section(
            plan.recommendationTitle,
            buildString {
                append(plan.recommendationExplanation)
                append("\n\n")
                append(
                    plan.recommendedWindows.joinToString("\n") { window ->
                        "• ${window.label} · ${window.urgency.label}"
                    }
                )
            }
        )

        plan.windows.forEach { window ->
            val recommended = window.id in recommendedIds
            val heading = buildString {
                if (recommended) append("추천 · ")
                append(window.label)
                append(" · ")
                append(window.urgency.label)
            }
            section(
                heading,
                buildString {
                    append(window.purpose)
                    append("\n\n할 일\n")
                    window.checklistSteps.forEachIndexed { index, step ->
                        append(index + 1).append(". ").append(step).append('\n')
                    }
                    append("\n하지 마세요\n")
                    window.doNotDoNotes.forEach { note -> append("• ").append(note).append('\n') }
                }.trim()
            )
            if (recommended) {
                primaryButton(window.primaryActionLabel) { openRecoveryPlanAction(window.primaryAction) }
            } else {
                secondaryButton(window.primaryActionLabel) { openRecoveryPlanAction(window.primaryAction) }
            }
            window.secondaryActions.forEach { action ->
                secondaryButton(action.label) { openRecoveryPlanAction(action) }
            }
        }

        section(
            "개인정보·안전 안내",
            plan.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 수습 계획 공유") {
            shareRecoveryPlanSummary(plan.familyShareSummary)
        }
        secondaryButton("며칠 뒤 후속 점검 보기") {
            show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun openRecoveryPlanAction(action: RecoveryAction) {
        when (action.id) {
            RecoveryActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            RecoveryActionId.FAMILY_CALLBACK_CARD -> openFamilyCallbackCard(currentAnalysis)
            RecoveryActionId.CHECKER -> show(Screen.CHECKER)
            RecoveryActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            RecoveryActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            RecoveryActionId.RESPONSE_CENTER -> openResponseCenter()
            RecoveryActionId.FAMILY_ALERT_MESSAGES -> show(Screen.FAMILY_ALERT_MESSAGES)
            RecoveryActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            RecoveryActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            RecoveryActionId.OFFICIAL_HELP_DIRECTORY -> openHelpDirectory(Screen.RECOVERY_PLAN)
            RecoveryActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            RecoveryActionId.NEW_ANALYSIS -> show(Screen.CHECKER)
            RecoveryActionId.SAFETY_PLAN -> show(Screen.PLAN)
            RecoveryActionId.TIMELINE_BRIEFING -> show(Screen.TIMELINE_BRIEFING)
            RecoveryActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
        }
    }

    private fun LinearLayout.checker() {
        title("AI 보호자 분석")
        subtitle("문자, 카카오톡, 통화 녹취를 직접 붙여넣으세요. 민감정보는 지우고 검사해도 됩니다.")
        secondaryButton("붙여넣기 전 개인정보 가리기") {
            show(Screen.SAFE_PASTE_PRIVACY_COACH)
        }
        val sharedText = pendingSharedText.orEmpty()
        if (sharedText.isNotBlank()) {
            section("공유된 내용 불러옴", "카카오톡/SMS/메모에서 공유한 텍스트를 아래 입력칸에 넣었습니다. 민감정보는 지우고 분석해도 됩니다.")
        }
        val input = EditText(this@MainActivity).apply {
            hint = "예: 엄마 나 폰 고장났어. 통화 끊지 말고 급히 계좌로 돈 보내줘..."
            if (sharedText.isNotBlank()) {
                setText(sharedText)
                setSelection(text.length)
                pendingSharedText = null
            }
            minLines = 8
            gravity = Gravity.TOP
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setBackgroundColor(Color.WHITE)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        addView(input, LinearLayout.LayoutParams(match, dp(180)).withMargins(bottom = dp(16)))
        primaryButton("AI 보호자 분석하기") {
            val text = input.text.toString()
            if (text.isBlank()) {
                toast("검사할 내용을 붙여넣어 주세요.")
            } else {
                currentAnalysis = ScamAnalyzer.analyze(text)
                show(Screen.RESULT)
            }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.result() {
        title("AI 분석 결과")
        val analysis = currentAnalysis
        if (analysis == null) {
            section(
                "분석 결과 없음",
                "현재 분석 결과가 없습니다. 의심 내용을 먼저 직접 붙여넣어 분석한 뒤 결과와 상담 준비 카드를 확인하세요."
            )
            primaryButton("AI 보호자 분석 먼저 하기") { show(Screen.CHECKER) }
            secondaryButton("홈으로") { show(Screen.HOME) }
            return
        }
        riskCard(analysis)
        primaryButton("이 현금 인출·이동·전달 지시를 따르기 전 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        primaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        primaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        primaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        primaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        primaryButton("이 신분증·셀피 촬영·전송 요구를 따르기 전 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        primaryButton("이 결과 화면을 캡처해 공유하기 전 개인정보 가드") {
            show(Screen.SCREENSHOT_PRIVACY_GUARD_COACH)
        }
        primaryButton("이 링크를 열거나 앱을 설치하기 전 격리 점검") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        primaryButton("이 상품권·기프티콘·코드 요구를 따르기 전 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        primaryButton("이 회복·환급·취소 약속을 믿기 전 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        primaryButton("이 기관 사칭·압박 요청을 믿기 전 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        primaryButton("이 요청에 비밀값을 주기 전 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        primaryButton("이 새 번호·새 프로필을 믿기 전 격리") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        primaryButton("이 목소리를 믿기 전 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        primaryButton("이 상황에서 화면공유·원격지시 끊기") {
            show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
        }
        primaryButton("이 요청에 돈 보내기 전 멈춤 확인") {
            show(Screen.MONEY_TRANSFER_PAUSE_GATE)
        }
        primaryButton("이 상황에서 연락 끝내는 문장") {
            show(Screen.ACTIVE_CONTACT_EXIT_SCRIPTS)
        }
        primaryButton("이 상황을 별도 경로에서 확인하기") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        primaryButton("별도 확인 뒤 결과별 다음 단계") {
            show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
        }
        primaryButton("다시 연락할 때 경계 정하기") {
            show(Screen.RECONTACT_BOUNDARY_COACH)
        }
        primaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        secondaryButton("다음부터 붙여넣기 전 가리기") {
            show(Screen.SAFE_PASTE_PRIVACY_COACH)
        }
        secondaryButton("탐지 신호 설명 보기") { show(Screen.RISK_SIGNAL_EXPLAINERS) }
        secondaryButton("이 신호로 노출 가능성 확인") {
            show(Screen.EXPOSURE_IMPACT_TRIAGE)
        }
        secondaryButton("이 신호로 보호자 검토 시작") {
            show(Screen.TRUSTED_HELPER_REVIEW)
        }
        secondaryButton("이 신호로 가족 역할 나누기") {
            show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
        }
        secondaryButton("이 신호로 5분 가족 대화하기") {
            show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
        }
        secondaryButton("이 신호로 후속 점검 보기") {
            show(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
        }
        secondaryButton("이 신호로 가족 연습하기") { openDrill() }
        secondaryButton("이 상황 기준 준비 로드맵") { show(Screen.FAMILY_READINESS_ROADMAP) }
        actionPlanSection(analysis)
        aiGuardianSection(analysis)
        section("권장 행동", analysis.summary)
        familyPlanSection()
        primaryButton("가족 콜백 확인 카드") { openFamilyCallbackCard(analysis) }
        section(
            "긴급 대응",
            "이미 돈을 보냈다면 즉시 은행 지급정지와 112 또는 1394 신고를 진행하세요. 원격제어를 허용했다면 " +
                "먼저 연결을 끊고 앱 정보를 안전하게 남긴 뒤, 다른 안전한 기기에서 계정을 보호하세요."
        )
        secondaryButton("이미 피해가 의심돼요 / 긴급 대응 센터") { openResponseCenter() }
        secondaryButton("계정·기기 잠금 체크리스트") { show(Screen.ACCOUNT_LOCKDOWN) }
        secondaryButton("가족 경고 메시지 만들기") { show(Screen.FAMILY_ALERT_MESSAGES) }
        secondaryButton("신고용 상담 준비 카드") { show(Screen.REPORT_PREP) }
        secondaryButton("신고·상담 연락처 보기") { openHelpDirectory(Screen.RESULT) }
        primaryButton("가족에게 안전 리포트 공유") { shareSafetyReport(analysis) }
        primaryButton("사건 기록에 저장") {
            incidents.add(analysis)
            toast("개인정보를 가린 사건 기록을 저장했습니다.")
            show(Screen.LOG)
        }
        secondaryButton("다른 내용 검사") { show(Screen.CHECKER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.incidentLog() {
        title("사건 기록")
        subtitle(
            "기록은 이 기기에만 저장되고 최근 30개만 보관합니다. 붙여넣은 전체 원문은 사건 기록에 남기지 않으며, " +
                "개인정보를 가린 짧은 단서만 표시합니다."
        )
        val items = incidents.load()
        if (items.isEmpty()) {
            section("기록 없음", "저장된 분석 결과가 없습니다.")
        } else {
            items.forEach { incident ->
                section(
                    "${incidents.displayTime(incident.timestamp)} · ${incident.riskLabel} (${incident.score}점)",
                    "신호: ${incident.signals.ifBlank { "강한 신호 없음" }}\n가림 처리된 단서: ${incident.preview}"
                )
            }
            secondaryButton("기록 모두 삭제") {
                incidents.clear()
                toast("사건 기록을 삭제했습니다.")
                show(Screen.LOG)
            }
        }
        primaryButton("사건 타임라인 요약") { show(Screen.TIMELINE_BRIEFING) }
        primaryButton("새 검사") { show(Screen.CHECKER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.incidentTimelineBriefingScreen() {
        val briefing = IncidentTimelineBriefingBuilder.build(
            incidents = incidents.load(),
            hasCurrentAnalysis = currentAnalysis != null,
            displayTime = incidents::displayTime
        )

        title(briefing.title)
        subtitle(briefing.intro)
        secondaryButton("저장 내용 값을 읽히지 않고 현금 인출·전달 지시 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("저장 내용 값을 읽히지 않고 링크·앱 설치 지시 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("저장 내용 값을 읽히지 않고 상품권·코드 요구 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("저장 내용 값을 읽히지 않고 회복 약속 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("이 타임라인 화면을 캡처해 공유하기 전 개인정보 가드") {
            show(Screen.SCREENSHOT_PRIVACY_GUARD_COACH)
        }
        briefing.emptyStateTitle?.let { emptyTitle ->
            section(emptyTitle, briefing.emptyStateMessage.orEmpty())
        }

        val latestSummary = if (briefing.latestIncidentDisplayTime == null) {
            "최근 사건: 저장된 사건 없음"
        } else {
            "최근 사건: ${briefing.latestIncidentDisplayTime} · ${briefing.latestIncidentRiskLabel}"
        }
        section(
            "저장 기록 요약",
            buildString {
                appendLine("전체 ${briefing.totalCount}건")
                appendLine(
                    "높음 ${briefing.riskCounts.high}건 · 주의 ${briefing.riskCounts.medium}건 · " +
                        "낮음 ${briefing.riskCounts.low}건 · 확인 필요 ${briefing.riskCounts.needsReview}건"
                )
                appendLine("가장 높은 위험도: ${briefing.highestRiskLabel}")
                append(latestSummary)
            }
        )
        section(
            "주요 탐지 신호",
            if (briefing.topSignals.isEmpty()) {
                "저장된 탐지 신호가 없습니다. 새 요청이 돈·앱 설치·인증정보로 이어지면 직접 검사하세요."
            } else {
                briefing.topSignals.joinToString("\n") { signal ->
                    "• ${signal.label} · ${signal.incidentCount}건"
                }
            }
        )

        section(
            "추천 다음 단계",
            "저장 기록은 실제 피해 행동을 뜻하지 않습니다. 본인 상황에 해당하는 경로만 직접 고르세요."
        )
        briefing.nextSteps.forEach { action ->
            section(
                if (action.recommended) "추천 · ${action.label}" else action.label,
                action.explanation
            )
            if (action.recommended) {
                primaryButton(action.label) { openIncidentBriefingAction(action) }
            } else {
                secondaryButton(action.label) { openIncidentBriefingAction(action) }
            }
        }

        if (briefing.timelineItems.isNotEmpty()) {
            section(
                "최근 사건 타임라인",
                "최신 기록부터 표시합니다. 각 단서는 저장할 때 개인정보를 가린 미리보기이며 가족 공유 요약에는 넣지 않습니다."
            )
            briefing.timelineItems.forEach { item ->
                section(
                    "${item.displayTime} · ${item.riskLabel} (${item.score}점)",
                    "신호: ${item.signalSummary}\n" +
                        "개인정보 가림 단서: ${item.redactedPreview}\n" +
                        "다음 행동: ${item.recommendedNextActionLabel}"
                )
            }
        }

        section(
            "개인정보·안전 안내",
            briefing.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 사건 요약 공유") {
            shareIncidentTimelineBriefing(briefing.familyShareSummary)
        }
        secondaryButton("사건 기록으로 돌아가기") { show(Screen.LOG) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun openIncidentBriefingAction(action: IncidentBriefingNextStep) {
        when (action.id) {
            IncidentBriefingActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            IncidentBriefingActionId.RESPONSE_CENTER -> openResponseCenter()
            IncidentBriefingActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            IncidentBriefingActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.TIMELINE_BRIEFING)
            IncidentBriefingActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            IncidentBriefingActionId.NEW_ANALYSIS -> show(Screen.CHECKER)
        }
    }

    private fun LinearLayout.privacyCenterScreen() {
        val privacyCenter = PrivacyCenterBuilder.build(
            plan = safetyPlan.load(),
            incidentCount = incidents.count(),
            consentAccepted = prefs.getBoolean(KEY_CONSENTED, false)
        )

        title(privacyCenter.title)
        subtitle(privacyCenter.intro)
        secondaryButton("값·위치·경로 없이 현금 인출·전달 지시 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("카메라·파일 읽기 없이 신분증·셀피 요구 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("값·열기·조회·스캔 없이 링크·앱 설치 지시 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("값·조회·스캔 없이 상품권·코드 요구 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("값·조회 없이 회복 약속 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("값·조회 없이 기관 사칭·압박 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("비밀값을 보지 않는 멈춤 코치") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("값 없이 새 연락수단 격리 안내 보기") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("목소리 자료 없이 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        secondaryButton("큰글씨·읽어주기 접근성 코치") { show(Screen.ACCESSIBILITY_COACH) }
        secondaryButton("알림 없이 사건 뒤 가족 점검 달력 보기") {
            show(Screen.POST_INCIDENT_REVIEW_CALENDAR)
        }
        section(
            "사건 뒤 가족 점검 달력",
            "이 화면은 자동으로 알림을 보내지 않습니다. 가족과 직접 정한 일정에 가족 안전 계획 " +
                "점검을 표시해 두세요. 점검 날짜·시각·반복 주기·완료 여부는 저장하지 않고 기기 " +
                "달력 앱에 일정을 만들거나 바꾸지 않습니다."
        )
        section(
            "큰 버튼 단순 모드",
            "단순 모드를 켜면 홈 화면이 큰 버튼 세 개(문자 분석, 가족에게 전화, 긴급 신고)로 " +
                "바뀝니다. 켜짐/꺼짐 값 하나만 이 기기의 family_shield 저장소에 남기며 다른 " +
                "코치나 저장된 데이터는 바꾸지 않습니다. 시스템 글자 크기·고대비·읽어주기 설정은 " +
                "이 토글과 별개로 안드로이드 설정에서 직접 켜야 합니다."
        )
        secondaryButton(if (simpleMode.isOn()) "단순 모드 끄기" else "단순 모드 켜기") {
            simpleMode.setOn(!simpleMode.isOn())
            toast(if (simpleMode.isOn()) "단순 모드를 켰습니다." else "단순 모드를 껐습니다.")
            show(Screen.PRIVACY_CENTER)
        }
        section(
            "기기 안에 있는 데이터",
            privacyCenter.localDataInventoryItems.joinToString("\n\n") { item ->
                "• ${item.title} · ${item.status}\n${item.detail}"
            }
        )
        section(
            "권한 상태",
            privacyCenter.permissionStatusItems.joinToString("\n\n") { item ->
                "• ${item.title} · ${item.status}\n${item.detail}"
            }
        )
        section(
            "수집하지 않는 정보·하지 않는 일",
            privacyCenter.noCollectionStatements.joinToString("\n") { "• $it" }
        )
        section(
            "개인정보 안내",
            privacyCenter.privacyNotes.joinToString("\n") { "• $it" }
        )
        section(
            "항목별 데이터 정리",
            privacyCenter.cleanupActions.joinToString("\n\n") { action ->
                "• ${action.label}\n${action.description}"
            }
        )

        val incidentCleanup = privacyCenter.cleanupActions.single {
            it.id == PrivacyCleanupActionId.INCIDENT_LOGS
        }
        secondaryButton(incidentCleanup.label) {
            incidents.clear()
            toast("사건 기록 저장 영역만 비웠습니다.")
            show(Screen.PRIVACY_CENTER)
        }

        val safetyPlanCleanup = privacyCenter.cleanupActions.single {
            it.id == PrivacyCleanupActionId.SAFETY_PLAN
        }
        secondaryButton(safetyPlanCleanup.label) {
            safetyPlan.clear()
            toast("가족 안전 계획 저장 영역만 비웠습니다.")
            show(Screen.PRIVACY_CENTER)
        }

        val consentCleanup = privacyCenter.cleanupActions.single {
            it.id == PrivacyCleanupActionId.ONBOARDING_CONSENT
        }
        secondaryButton(consentCleanup.label) {
            prefs.edit().remove(KEY_CONSENTED).apply()
            toast("시작 동의 기록만 초기화했습니다. 다른 로컬 데이터는 그대로 남습니다.")
            show(Screen.ONBOARDING)
        }

        secondaryButton("값 없이 별도 확인 경로 고르기") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        secondaryButton("값 없이 결과별 다음 단계 보기") {
            show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
        }
        secondaryButton("값 없이 재연락 경계 문장 보기") {
            show(Screen.RECONTACT_BOUNDARY_COACH)
        }
        secondaryButton("값 없이 가족 확인 비밀 재정비 보기") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        secondaryButton("붙여넣기 전 개인정보 가리기") {
            show(Screen.SAFE_PASTE_PRIVACY_COACH)
        }
        primaryButton("가족에게 개인정보 요약 공유") {
            sharePrivacySummary(privacyCenter.familyShareSummary)
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun openResponseCenter(situation: IncidentSituation? = null) {
        selectedIncidentSituation = situation
        show(Screen.RESPONSE_CENTER)
    }

    private fun LinearLayout.responseCenter() {
        title("긴급 대응 센터")
        subtitle("이미 벌어진 상황과 가장 가까운 항목을 고르면 지금 해야 할 행동과 도움 연락처를 보여줍니다.")
        section("개인정보 보호", "이 화면은 원문 문자나 통화 내용을 저장하지 않습니다. 선택한 상황과 대응 단계만 사용됩니다.")

        IncidentSituation.entries.forEach { situation ->
            secondaryButton(situation.label) {
                selectedIncidentSituation = situation
                show(Screen.RESPONSE_CENTER)
            }
        }

        val situation = selectedIncidentSituation
        if (situation != null) {
            val plan = IncidentResponseBuilder.build(situation)
            val body = plan.steps.mapIndexed { index, step -> "${index + 1}. $step" }.joinToString("\n")
            section(plan.title, body)
            plan.emergencyContacts.forEach { contact ->
                secondaryButton("${contact.number} ${contact.label} 전화 앱에서 열기") { dial(contact.number) }
            }
            primaryButton("가족에게 대응 요약 공유") { shareIncidentResponse(plan) }
        }

        secondaryButton("신고·상담 연락처 보기") { openHelpDirectory(Screen.RESPONSE_CENTER) }
        secondaryButton(
            if (situation == null) "증거 보존 체크리스트 열기" else "이 상황의 증거 보존 체크리스트"
        ) { openEvidenceKit(situation) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun openEvidenceKit(situation: IncidentSituation?) {
        selectedEvidenceSituation = situation
        show(Screen.EVIDENCE)
    }

    private fun LinearLayout.evidencePreservationScreen() {
        title("증거 보존 체크리스트")
        subtitle("상황에 맞는 항목을 골라, 메시지나 앱을 지우기 전에 직접 보관할 내용을 차분히 확인하세요.")
        secondaryButton("증거 보존 전 현금 인출·전달 지시 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("링크·앱 값을 다루지 않고 설치 압박 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("상품권·코드 값 없이 멈춤 경계 점검") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("증거 값 없이 회복 약속 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("이 체크리스트를 캡처해 공유하기 전 개인정보 가드") {
            show(Screen.SCREENSHOT_PRIVACY_GUARD_COACH)
        }
        section(
            "수동·로컬 안내",
            "이 화면은 증거를 자동으로 캡처하거나 업로드하지 않습니다. 선택 내용과 완료 상태도 저장하지 않으며, " +
                "법적 효력을 보장하는 안내가 아닙니다. 계정·자금 보호가 급하면 증거 정리보다 연결 차단과 지급정지를 먼저 하세요."
        )

        secondaryButton(evidenceOptionLabel(null)) {
            selectedEvidenceSituation = null
            show(Screen.EVIDENCE)
        }
        IncidentSituation.entries.forEach { situation ->
            secondaryButton(evidenceOptionLabel(situation)) {
                selectedEvidenceSituation = situation
                show(Screen.EVIDENCE)
            }
        }

        val kit = EvidencePreservationKitBuilder.build(selectedEvidenceSituation)
        section(
            kit.title,
            kit.checklistItems.mapIndexed { index, item -> "${index + 1}. $item" }.joinToString("\n")
        )
        section("주의할 점", kit.cautions.joinToString("\n") { "• $it" })
        if (kit.officialReportPrepNotes.isNotEmpty()) {
            section("신고·상담 전에 준비", kit.officialReportPrepNotes.joinToString("\n") { "• $it" })
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        secondaryButton("신고·상담 연락처 보기") { openHelpDirectory(Screen.EVIDENCE) }
        section("가족에게 보낼 안전 요약", kit.familyShareSummary)
        primaryButton("가족에게 증거 보존 요약 공유") { shareEvidenceKit(kit) }
        secondaryButton("긴급 대응 센터로") {
            selectedIncidentSituation = selectedEvidenceSituation
            show(Screen.RESPONSE_CENTER)
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun evidenceOptionLabel(situation: IncidentSituation?): String {
        val label = situation?.label ?: EvidencePreservationKitBuilder.GENERIC_SITUATION_LABEL
        return if (selectedEvidenceSituation == situation) "✓ $label" else label
    }

    private fun openHelpDirectory(returnTo: Screen) {
        helpDirectoryReturnScreen = returnTo
        show(Screen.HELP_DIRECTORY)
    }

    private fun LinearLayout.helpDirectoryScreen() {
        val directory = OfficialHelpDirectoryBuilder.build()
        title(directory.title)
        subtitle(directory.intro)
        secondaryButton("공식 도움과 상대의 현금 인출·전달 지시를 분리") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("공식 도움과 상대의 신분증·셀피 요구 분리") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("공식 도움과 분리해 링크·앱 설치 지시 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("공식 도움과 분리해 상품권·코드 요구 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("이 안내와 회복 약속을 분리해 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("이 안내와 상대의 주장을 분리해 압박 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        section(
            "로컬·수동 안내",
            "연락처와 말할 내용은 앱에 내장되어 있으며 네트워크에서 조회하지 않습니다. " +
                "전화 버튼은 통화를 자동으로 걸지 않고 전화 앱만 엽니다."
        )
        secondaryButton("현재 상대와 분리된 확인 경로 고르기") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        secondaryButton("확인 뒤 결과별 다음 단계") {
            show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
        }

        directory.entries.forEach { entry ->
            val heading = entry.dialNumber?.let { number -> "$number · ${entry.label}" } ?: entry.label
            val body = buildString {
                append("언제 사용하나요?\n")
                append(entry.whenToUse)
                append("\n\n이렇게 말하세요\n")
                entry.whatToSay.forEachIndexed { index, script ->
                    append(index + 1).append(". ").append(script).append('\n')
                }
                append("\n안전 메모\n")
                entry.safetyNotes.forEach { note -> append("• ").append(note).append('\n') }
            }.trim()
            section(heading, body)
            entry.dialNumber?.let { number ->
                secondaryButton("$number ${entry.label} 전화 앱에서 열기") { dial(number) }
            }
        }

        section(
            "공유 전 확인",
            "가족 공유 요약에는 공식 연락처와 말할 내용만 들어갑니다. 붙여넣은 원문, 계좌 전체 번호, " +
                "비밀번호, 인증번호(OTP), 상품권 PIN은 넣지 않습니다."
        )
        secondaryButton("송금·결제 전에 멈춤 확인") {
            show(Screen.MONEY_TRANSFER_PAUSE_GATE)
        }
        primaryButton("가족에게 신고 연락처 공유") { shareHelpDirectory(directory) }
        if (helpDirectoryReturnScreen != Screen.HOME) {
            secondaryButton("이전 화면으로") { show(helpDirectoryReturnScreen) }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.officialReportPrepScreen() {
        title("신고용 상담 준비 카드")
        subtitle("현재 AI 보호자 분석을 개인정보 가림 단서, 추천 공식 연락처, 상담 대사와 전화 전 체크리스트로 정리합니다.")
        secondaryButton("신고 준비와 현금 인출·전달 지시를 분리") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("신고 준비와 신분증·셀피 제출 요구 분리") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("신고 준비와 분리해 링크·앱 설치 지시 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("신고 준비와 분리해 상품권·코드 요구 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("신고 준비와 회복 약속을 분리해 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("신고 준비 전에 기관 사칭·압박 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("현재 상대와 분리된 확인 경로 고르기") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        secondaryButton("확인 뒤 결과별 다음 단계") {
            show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }

        val analysis = currentAnalysis
        if (analysis == null) {
            section(
                "분석 결과가 필요해요",
                "이 카드는 현재 분석 결과로만 만듭니다. 의심 문자·메신저·통화 내용을 직접 붙여넣어 분석하면 개인정보를 가린 상담 준비 카드를 볼 수 있습니다."
            )
            section(
                "저장하지 않는 임시 안내",
                "카드는 따로 저장되지 않고 사건 기록도 자동으로 바꾸지 않습니다. 분석은 기기 안에서만 진행됩니다."
            )
            secondaryButton("송금 전 멈춤 확인") { show(Screen.MONEY_TRANSFER_PAUSE_GATE) }
            primaryButton("AI 보호자 분석 먼저 하기") { show(Screen.CHECKER) }
            secondaryButton("홈으로") { show(Screen.HOME) }
            return
        }

        val prep = OfficialReportPrepBuilder.build(analysis)
        section("위험 요약", prep.riskSummary)
        section("개인정보 가림 단서", prep.redactedClue)
        section("탐지 신호", prep.detectedSignalLabels.joinToString("\n") { "• $it" })
        section(
            "상담원에게 말할 내용",
            prep.scriptLines.mapIndexed { index, line -> "${index + 1}. $line" }.joinToString("\n")
        )
        section("전화 전 확인", prep.beforeCallChecklist.joinToString("\n") { "• $it" })
        section("개인정보·안전 안내", prep.privacySafetyNotes.joinToString("\n") { "• $it" })

        section("추천 공식 연락처", "위 준비 내용을 확인한 뒤 상황에 맞는 공식 창구를 선택하세요.")
        prep.recommendedEntries.forEach { entry ->
            val heading = entry.dialNumber?.let { number -> "$number · ${entry.label}" } ?: entry.label
            section(heading, entry.whenToUse)
            entry.dialNumber?.let { number ->
                secondaryButton("$number ${entry.label} 전화 앱에서 열기") { dial(number) }
            }
        }

        secondaryButton("송금 전 멈춤 확인") { show(Screen.MONEY_TRANSFER_PAUSE_GATE) }
        primaryButton("가족에게 상담 준비 카드 공유") { shareOfficialReportPrep(prep) }
        secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun openFamilyCallbackCard(analysis: ScamAnalysis?) {
        callbackCardAnalysis = analysis
        show(Screen.CALLBACK_CARD)
    }

    private fun LinearLayout.familyCallbackCardScreen() {
        val analysis = callbackCardAnalysis
        val card = FamilyCallbackCardBuilder.build(analysis, safetyPlan.load())

        title(card.title)
        subtitle("의심 대화를 멈추고, 새로 받은 번호가 아닌 가족 안전 계획에 직접 저장한 번호로 확인하세요.")
        secondaryButton("새 번호·새 프로필을 믿기 전 격리") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("익숙한 목소리를 믿기 전 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        secondaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        secondaryButton("값 없이 별도 확인 경로부터 고르기") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        section(
            if (analysis == null) "분석 전 콜백 루틴" else "위험 요약",
            card.riskSummary
        )
        if (analysis != null) {
            card.redactedClue?.let { clue ->
                section("개인정보 가림 단서", clue)
            }
        }

        val readiness = card.readiness
        val readinessBody = buildString {
            readiness.checklist.forEach { item ->
                append(if (item.done) "✔ " else "☐ ").append(item.label).append('\n')
            }
            append("\n다음 할 일: ").append(readiness.nextAction)
        }
        section(
            "${readiness.title} (${readiness.completedCount}/${readiness.totalCount})",
            readinessBody
        )
        section("가족 안전 질문", card.safeWordPrompt)
        section(
            "저장된 번호로 확인하는 순서",
            card.callbackSteps.mapIndexed { index, step -> "${index + 1}. $step" }.joinToString("\n")
        )

        if (card.dialableContacts.isEmpty()) {
            section(
                "저장된 콜백 번호 없음",
                "의심 메시지나 통화에서 새로 알려준 번호를 믿지 말고, 가족 안전 계획에 신뢰 연락처를 직접 저장한 뒤 확인하세요."
            )
        } else {
            section(
                "저장된 신뢰 연락처",
                "가족 안전 계획에 직접 저장한 번호만 표시합니다. 버튼을 눌러도 통화를 자동으로 걸지 않고 전화 앱만 엽니다."
            )
            card.dialableContacts.forEach { contact ->
                val label = contact.label.ifBlank { "저장된 가족 번호" }
                secondaryButton("$label 전화 앱에서 열기 (${contact.number})") { dial(contact.number) }
            }
        }

        section("확인 전에는 하지 마세요", card.guardrails.joinToString("\n") { "• $it" })
        section("개인정보·안전 안내", card.privacySafetyNotes.joinToString("\n") { "• $it" })

        if (card.needsSafetyPlanSetup) {
            primaryButton("가족 안전 계획 설정") { show(Screen.PLAN) }
        }
        if (analysis == null) {
            primaryButton("AI 보호자 분석하기") { show(Screen.CHECKER) }
        }
        primaryButton("가족에게 콜백 확인 요약 공유") {
            shareFamilyCallbackCard(card.familyShareSummary)
        }
        if (analysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.readinessSection() {
        val readiness = FamilyReadinessBuilder.build(safetyPlan.load())
        val body = buildString {
            readiness.checklist.forEach { item ->
                append(if (item.done) "✔ " else "☐ ").append(item.label).append('\n')
            }
            append("\n다음 할 일: ").append(readiness.nextAction)
        }
        section("${readiness.title} (${readiness.completedCount}/${readiness.totalCount})", body)
        secondaryButton("준비 상태와 별개로 현금 인출·전달 지시 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("준비 상태와 별개로 신분증·셀피 요구 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("준비 상태와 별개로 링크·앱 설치 지시 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("준비 상태와 별개로 상품권·코드 요구 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("준비 상태와 별개로 회복 약속 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 사칭·압박 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("비밀값 전달 전 멈춤 순서") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("새 번호·새 프로필 격리 순서") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("목소리를 증거로 믿기 전 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        secondaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        secondaryButton("가족 준비 로드맵") { show(Screen.FAMILY_READINESS_ROADMAP) }
        if (readiness.level == ReadinessLevel.READY) {
            secondaryButton("가족 안전 연습 코치") { openDrill() }
            secondaryButton("가족 안전 계획 설정") { show(Screen.PLAN) }
        } else {
            secondaryButton("가족 안전 계획 설정") { show(Screen.PLAN) }
            secondaryButton("가족 안전 연습 코치") { openDrill() }
        }
    }

    private fun openDrill() {
        show(Screen.DRILL)
    }

    private fun LinearLayout.drillScreen() {
        val coach = FamilySafetyDrillCoachBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load())
        )
        val recommendedIds = coach.recommendedDrillIds.toSet()

        title(coach.title)
        subtitle(coach.intro)
        section(
            "가상 연습 안내",
            coach.fictionNotice +
                "\n가족 한 명이 상대 역할의 가상 대사를 읽고, 다른 가족이 멈춤 문구와 직접 확인 순서를 소리 내어 연습하세요."
        )
        section(
            coach.recommendationTitle,
            buildString {
                append(coach.recommendationExplanation)
                append("\n\n먼저 연습할 카드\n")
                append(
                    coach.recommendedCards.joinToString("\n") { card ->
                        "• ${card.title} · ${card.difficulty.label} · ${card.urgency.label}"
                    }
                )
            }
        )
        coach.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openFamilySafetyDrillAction(action) }
            } else {
                secondaryButton(action.label) { openFamilySafetyDrillAction(action) }
            }
        }

        section(
            "추천 연습 카드",
            "현재 로컬 분석 신호와 값 없는 가족 준비도에 맞춘 순서입니다. 통과 여부나 선택은 저장되지 않습니다."
        )
        coach.recommendedCards.forEach { card ->
            familySafetyDrillCard(card, recommended = true)
        }

        val remainingCards = coach.cards.filterNot { card -> card.id in recommendedIds }
        if (remainingCards.isNotEmpty()) {
            section(
                "모든 추가 연습 카드",
                "추천과 관계없이 가족에게 필요한 가상 상황을 이어서 연습할 수 있습니다."
            )
            remainingCards.forEach { card ->
                familySafetyDrillCard(card, recommended = false)
            }
        }

        section(
            "개인정보·안전 안내",
            coach.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 연습 과제 공유") {
            shareFamilySafetyDrillSummary(coach.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.familySafetyDrillCard(
        card: FamilySafetyDrillCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.title)
            append(" · ")
            append(card.difficulty.label)
            append(" · ")
            append(card.urgency.label)
        }
        section(
            heading,
            buildString {
                append("가상 상황\n")
                append(card.scenario)
                append("\n\n")
                append(card.fictionalSetup)
                append("\n\n역할 대사\n")
                card.rolePlayLines.forEach { line -> append("• ").append(line).append('\n') }
                append("\n멈춤 문구\n")
                append(card.expectedStopPhrase)
                append("\n\n대응 순서\n")
                card.responseSteps.forEachIndexed { index, step ->
                    append(index + 1).append(". ").append(step).append('\n')
                }
                append("\n통과 체크\n")
                card.passChecklist.forEach { item -> append("• ").append(item).append('\n') }
                append("\n가족과 돌아보기\n")
                append(card.familyReflectionPrompt)
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openFamilySafetyDrillAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openFamilySafetyDrillAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openFamilySafetyDrillAction(action) }
        }
    }

    private fun openFamilySafetyDrillAction(action: FamilySafetyDrillAction) {
        when (action.id) {
            FamilySafetyDrillActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            FamilySafetyDrillActionId.CHECKER -> show(Screen.CHECKER)
            FamilySafetyDrillActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            FamilySafetyDrillActionId.RISK_SIGNAL_EXPLAINERS ->
                show(Screen.RISK_SIGNAL_EXPLAINERS)
            FamilySafetyDrillActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            FamilySafetyDrillActionId.SAFETY_PLAN -> show(Screen.PLAN)
            FamilySafetyDrillActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            FamilySafetyDrillActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            FamilySafetyDrillActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            FamilySafetyDrillActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.DRILL)
            FamilySafetyDrillActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            FamilySafetyDrillActionId.TRUSTED_HELPER_HANDOFF ->
                show(Screen.TRUSTED_HELPER_HANDOFF)
            FamilySafetyDrillActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            FamilySafetyDrillActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)
        }
    }

    private fun shareFamilySafetyDrillSummary(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 연습 과제 공유"))
    }

    private fun openSampleLibrary() {
        selectedSampleCategory = null
        show(Screen.SAMPLES)
    }

    private fun LinearLayout.sampleLibraryScreen() {
        title("샘플로 AI 분석 연습")
        subtitle("실제 사기 문자가 없어도, 앱에 내장된 연습용 가상 메시지로 AI 보호자 분석을 안전하게 연습할 수 있습니다.")
        section(
            "개인정보 보호",
            "샘플은 모두 앱에 내장된 연습용 가상 메시지입니다. 어떤 샘플을 골랐는지 저장하지 않으며, " +
                "결과 화면에서 ‘사건 기록에 저장’을 직접 누르기 전에는 사건 기록에 남지 않습니다."
        )
        section("연습 방법", ScamSampleLibrary.FICTION_NOTICE + "\n샘플을 고르면 가상 메시지와 의심 포인트를 먼저 보여주고, 같은 내용을 AI 보호자 분석으로 확인할 수 있습니다.")

        ScamSampleCategory.entries.forEach { category ->
            secondaryButton(category.label) {
                selectedSampleCategory = category
                show(Screen.SAMPLES)
            }
        }

        val category = selectedSampleCategory
        if (category != null) {
            val sample = ScamSampleLibrary.build(category)
            section(sample.title, sample.sampleText)
            section("왜 의심스러운가요?", sample.learningNote)
            primaryButton("이 샘플로 AI 보호자 분석하기") {
                currentAnalysis = ScamAnalyzer.analyze(sample.sampleText)
                show(Screen.RESULT)
            }
        }

        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.familyPlanSection() {
        val plan = safetyPlan.load()
        if (plan.isBlank) {
            section(
                "가족 확인 루틴",
                "아직 저장된 안전 계획이 없습니다. 안전단어와 콜백 번호를 미리 저장해두면 위급할 때 더 빠르게 확인할 수 있습니다."
            )
            primaryButton("가족 안전 계획 만들기") { show(Screen.PLAN) }
            return
        }

        val body = buildString {
            if (plan.safeWord.isNotBlank()) {
                append("1. 안전 확인 질문을 먼저 물어보세요: “${plan.safeWord}”\n")
            } else {
                append("1. 가족만 아는 안전단어나 질문을 먼저 물어보세요.\n")
            }
            append("2. 답이 어색하면 아래 저장된 번호로 직접 전화해 확인하세요.\n")
            append("3. 확인 전에는 송금, 앱 설치, 인증번호(OTP) 제공을 거부하세요.")
            if (plan.callbackScript.isNotBlank()) {
                append("\n\n콜백 스크립트:\n${plan.callbackScript}")
            }
        }
        section("가족 안전 계획", body)
        plan.contacts.filter { it.number.isNotBlank() }.forEach { contact ->
            val label = contact.label.ifBlank { "저장된 번호" }
            secondaryButton("$label 전화 연결 (${contact.number})") { dial(contact.number) }
        }
        secondaryButton("안전 계획 수정") { show(Screen.PLAN) }
    }

    private fun LinearLayout.safetyPlanScreen() {
        val plan = safetyPlan.load()
        title("가족 안전 계획")
        subtitle("안전단어와 연락처는 직접 입력하며, 이 기기에만 저장됩니다. 서버로 전송되지 않습니다.")
        section("사용 방법", "위급한 메시지를 받으면 아래 안전단어를 묻고, 저장된 번호로 직접 전화해 확인하세요.")
        secondaryButton("값 없이 재정비 순서 먼저 보기") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }

        label("안전 확인 질문 / 단어")
        val safeWordInput = EditText(this@MainActivity).apply {
            hint = "예: 우리 가족만 아는 어릴 적 별명은?"
            setText(plan.safeWord)
            setBackgroundColor(Color.WHITE)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        addView(safeWordInput, LinearLayout.LayoutParams(match, wrap).withMargins(bottom = dp(14)))

        label("신뢰하는 연락처 (한 줄에 하나, \"이름 - 번호\")")
        val contactsInput = EditText(this@MainActivity).apply {
            hint = "예: 엄마 - 010-1234-5678\n아빠 - 010-2345-6789"
            setText(formatContacts(plan.contacts))
            minLines = 3
            gravity = Gravity.TOP
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setBackgroundColor(Color.WHITE)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        addView(contactsInput, LinearLayout.LayoutParams(match, dp(120)).withMargins(bottom = dp(14)))

        label("콜백 스크립트")
        val scriptInput = EditText(this@MainActivity).apply {
            hint = "가족에게 확인할 때 읽을 대사"
            setText(plan.callbackScript.ifBlank { defaultCallbackScript })
            minLines = 5
            gravity = Gravity.TOP
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setBackgroundColor(Color.WHITE)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        addView(scriptInput, LinearLayout.LayoutParams(match, dp(150)).withMargins(bottom = dp(16)))

        primaryButton("저장하기") {
            safetyPlan.save(
                SafetyPlan(
                    safeWord = safeWordInput.text.toString().trim(),
                    callbackScript = scriptInput.text.toString().trim(),
                    contacts = parseContacts(contactsInput.text.toString())
                )
            )
            toast("가족 안전 계획을 저장했습니다.")
            show(Screen.HOME)
        }
        secondaryButton("계획 삭제") {
            safetyPlan.clear()
            toast("저장된 안전 계획을 삭제했습니다.")
            show(Screen.PLAN)
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun formatContacts(contacts: List<TrustedContact>): String =
        contacts.joinToString("\n") { "${it.label} - ${it.number}" }

    private fun parseContacts(raw: String): List<TrustedContact> =
        raw.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { line ->
                val idx = line.indexOf(" - ")
                if (idx > 0) {
                    TrustedContact(line.substring(0, idx).trim(), line.substring(idx + 3).trim())
                } else {
                    TrustedContact(line, "")
                }
            }
            .toList()

    private fun dial(number: String) {
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(number)}")))
    }

    private fun shareQuickStopSummary(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 멈춤 안내 공유"))
    }

    private fun shareSafetyReport(analysis: ScamAnalysis) {
        val report = SafetyReportBuilder.build(analysis)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "가족 AI 보이스피싱 방패 확인 요청")
            putExtra(Intent.EXTRA_TEXT, report)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 안전 리포트 공유"))
    }

    private fun shareIncidentResponse(plan: IncidentResponsePlan) {
        val report = IncidentResponseReportBuilder.build(plan)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "가족 AI 보이스피싱 방패 긴급 대응 요약")
            putExtra(Intent.EXTRA_TEXT, report)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 대응 요약 공유"))
    }

    private fun shareEvidenceKit(kit: EvidencePreservationKit) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "가족 AI 보이스피싱 방패 증거 보존 확인")
            putExtra(Intent.EXTRA_TEXT, kit.familyShareSummary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 증거 보존 요약 공유"))
    }

    private fun shareHelpDirectory(directory: OfficialHelpDirectory) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "가족 AI 보이스피싱 방패 신고·상담 연락처")
            putExtra(Intent.EXTRA_TEXT, directory.familyShareSummary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 신고 연락처 공유"))
    }

    private fun shareOfficialReportPrep(prep: OfficialReportPrepCard) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "가족 AI 보이스피싱 방패 신고용 상담 준비 카드")
            putExtra(Intent.EXTRA_TEXT, prep.familyShareSummary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 상담 준비 카드 공유"))
    }

    private fun shareFamilyCallbackCard(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "가족 AI 보이스피싱 방패 콜백 확인 카드")
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 콜백 확인 요약 공유"))
    }

    private fun shareSafetyFlowRouter(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "가족 AI 보이스피싱 방패 안전 흐름 안내")
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 안전 흐름 요약 공유"))
    }

    private fun shareFamilyAlertText(text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(shareIntent, "가족 경고 메시지 직접 공유"))
    }

    private fun shareAccountLockdownChecklist(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "가족 AI 보이스피싱 방패 계정·기기 잠금 체크리스트")
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 잠금 체크리스트 공유"))
    }

    private fun shareIncidentTimelineBriefing(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "가족 AI 보이스피싱 방패 사건 요약")
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 사건 요약 공유"))
    }

    private fun sharePrivacySummary(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "가족 AI 보이스피싱 방패 개인정보 요약")
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 개인정보 요약 공유"))
    }

    private fun shareRecoveryPlanSummary(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "가족 AI 보이스피싱 방패 24시간 수습 계획")
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 수습 계획 공유"))
    }

    private fun LinearLayout.riskCard(analysis: ScamAnalysis) {
        val matched = if (analysis.matchedSignals.isEmpty()) {
            "강한 위험 신호 없음"
        } else {
            analysis.matchedSignals.joinToString("\n") { "• ${it.label}: ${it.description}" }
        }
        val box = vertical {
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(Color.WHITE)
            addView(TextView(this@MainActivity).apply {
                text = "위험도 ${analysis.riskLevel.label} · ${analysis.score}점"
                textSize = 22f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(analysis.riskLevel.color)
            })
            addView(TextView(this@MainActivity).apply {
                text = matched
                textSize = 15f
                setTextColor(0xFF334155.toInt())
                setPadding(0, dp(10), 0, 0)
            })
        }
        addView(box, LinearLayout.LayoutParams(match, wrap).withMargins(bottom = dp(14)))
    }

    private fun LinearLayout.actionPlanSection(analysis: ScamAnalysis) {
        val plan = analysis.actionPlan
        val body = plan.steps.mapIndexed { index, step -> "${index + 1}. $step" }.joinToString("\n")
        section(plan.title, body)
    }

    private fun LinearLayout.aiGuardianSection(analysis: ScamAnalysis) {
        val advice = analysis.guardianAdvice
        val body = buildString {
            append(advice.verdict)
            append("\n\n즉시 할 일:\n")
            advice.immediateActions.forEach { action -> append("• ").append(action).append("\n") }
            append("\n")
            append(advice.callbackPrompt)
        }.trim()
        section("AI 보호자 방지 루틴", body)
    }

    private fun LinearLayout.trustedHelperReviewModeScreen() {
        val review = TrustedHelperReviewModeBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load())
        )
        val recommendedIds = review.recommendedCardIds.toSet()

        title(review.title)
        subtitle(review.intro)
        secondaryButton("보호자와 현금 인출·전달 지시를 값 없이 점검") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("보호자와 신분증·셀피 촬영·전송 요구 점검") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("보호자와 링크·앱 설치 압박 격리 점검") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("보호자와 상품권·코드 요구 멈춤 점검") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("보호자와 회복 약속 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("보호자와 기관 사칭·압박 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("보호자와 비밀값 없는 멈춤 순서 보기") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("보호자와 새 연락수단 격리 원칙 보기") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("보호자와 목소리 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        secondaryButton("화면공유·원격지시 중단 코치") {
            show(Screen.REMOTE_ACCESS_CUTOFF_COACH)
        }
        section(
            review.recommendationTitle,
            buildString {
                append(review.recommendationExplanation)
                append("\n\n먼저 함께 볼 검토 카드\n")
                append(
                    review.recommendedCards.joinToString("\n") { card ->
                        "• ${card.title} · ${card.stage.label} · ${card.priority.label}"
                    }
                )
                append("\n\n분석 신호만으로 실제 노출이나 본인 확인이 끝났다고 단정하지 마세요.")
            }
        )
        review.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openTrustedHelperReviewAction(action) }
            } else {
                secondaryButton(action.label) { openTrustedHelperReviewAction(action) }
            }
        }

        section(
            "추천 보호자 검토 카드",
            "현재 로컬 분석 신호와 값 없는 가족 준비 수준에 맞춘 순서입니다. 질문에는 실제 값 대신 종류만 답하고, 답변이나 메모는 저장되지 않습니다."
        )
        review.recommendedCards.forEach { card ->
            trustedHelperReviewCard(card, recommended = true)
        }

        val remainingCards = review.cards.filterNot { card -> card.id in recommendedIds }
        if (remainingCards.isNotEmpty()) {
            section(
                "나머지 보호자 검토 카드",
                "추천과 관계없이 필요한 단계를 함께 살펴보세요. 카드 선택·읽음·진행 상태는 저장되지 않습니다."
            )
            remainingCards.forEach { card ->
                trustedHelperReviewCard(card, recommended = false)
            }
        }

        section(
            "개인정보·안전 안내",
            review.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        secondaryButton("가족 역할 보드로 이어가기") {
            show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
        }
        secondaryButton("함께 본 뒤 5분 가족 대화") {
            show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
        }
        secondaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        primaryButton("가족에게 보호자 검토 요약 공유") {
            shareTrustedHelperReview(review.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.trustedHelperReviewCard(
        card: TrustedHelperReviewCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.title)
            append(" · ")
            append(card.stage.label)
            append(" · ")
            append(card.category.label)
            append(" · ")
            append(card.priority.label)
        }
        section(
            heading,
            buildString {
                append("보호자 역할\n")
                append(card.helperRole)
                append("\n\n안전하게 물을 질문\n")
                append(card.safeQuestion)
                append("\n\n묻거나 적지 마세요\n")
                card.whatNotToAsk.forEach { note -> append("• ").append(note).append('\n') }
                append("\n첫 안전 행동\n")
                append(card.firstSafeMove)
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openTrustedHelperReviewAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openTrustedHelperReviewAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openTrustedHelperReviewAction(action) }
        }
    }

    private fun openTrustedHelperReviewAction(action: TrustedHelperReviewAction) {
        when (action.id) {
            TrustedHelperReviewActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            TrustedHelperReviewActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            TrustedHelperReviewActionId.CHECKER -> show(Screen.CHECKER)
            TrustedHelperReviewActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            TrustedHelperReviewActionId.RISK_SIGNAL_EXPLAINERS ->
                show(Screen.RISK_SIGNAL_EXPLAINERS)
            TrustedHelperReviewActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            TrustedHelperReviewActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            TrustedHelperReviewActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            TrustedHelperReviewActionId.TRUSTED_HELPER_HANDOFF ->
                show(Screen.TRUSTED_HELPER_HANDOFF)
            TrustedHelperReviewActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            TrustedHelperReviewActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            TrustedHelperReviewActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)
            TrustedHelperReviewActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            TrustedHelperReviewActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            TrustedHelperReviewActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.TRUSTED_HELPER_REVIEW)
            TrustedHelperReviewActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            TrustedHelperReviewActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            TrustedHelperReviewActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
        }
    }

    private fun shareTrustedHelperReview(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 보호자 검토 요약 공유"))
    }

    private fun LinearLayout.trustedHelperHandoffScreen() {
        val card = TrustedHelperHandoffBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load())
        )
        val recommendedIds = card.recommendedCardIds.toSet()

        title(card.title)
        subtitle(card.intro)
        section(
            card.recommendationTitle,
            buildString {
                append(card.recommendationExplanation)
                append("\n\n")
                append(
                    card.recommendedCards.joinToString("\n") { helper ->
                        "• ${helper.label} · ${helper.urgency.label}"
                    }
                )
            }
        )

        card.cards.forEach { helper ->
            val recommended = helper.id in recommendedIds
            val heading = buildString {
                if (recommended) append("추천 · ")
                append(helper.label)
                append(" · ")
                append(helper.urgency.label)
            }
            section(
                heading,
                buildString {
                    append("도와줄 사람\n")
                    append(helper.audience)
                    append("\n\n왜 필요한가요?\n")
                    append(helper.whyItMatters)
                    append("\n\n부탁할 일\n")
                    helper.askSteps.forEachIndexed { index, step ->
                        append(index + 1).append(". ").append(step).append('\n')
                    }
                    append("\n부탁하지 마세요\n")
                    helper.doNotAskNotes.forEach { note -> append("• ").append(note).append('\n') }
                }.trim()
            )
            if (recommended) {
                primaryButton(helper.primaryAction.label) { openTrustedHelperAction(helper.primaryAction) }
            } else {
                secondaryButton(helper.primaryAction.label) { openTrustedHelperAction(helper.primaryAction) }
            }
            helper.secondaryActions.forEach { action ->
                secondaryButton(action.label) { openTrustedHelperAction(action) }
            }
        }

        section(
            "개인정보·안전 안내",
            card.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        secondaryButton("보호자와 함께 안전 검토") {
            show(Screen.TRUSTED_HELPER_REVIEW)
        }
        primaryButton("보호자에게 도움 요청 공유") {
            shareTrustedHelperHandoff(card.handoffShareMessage)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun openTrustedHelperAction(action: TrustedHelperAction) {
        when (action.id) {
            TrustedHelperActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            TrustedHelperActionId.FAMILY_CALLBACK_CARD -> openFamilyCallbackCard(currentAnalysis)
            TrustedHelperActionId.CHECKER -> show(Screen.CHECKER)
            TrustedHelperActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            TrustedHelperActionId.SAFETY_PLAN -> show(Screen.PLAN)
            TrustedHelperActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            TrustedHelperActionId.RESPONSE_CENTER -> openResponseCenter()
            TrustedHelperActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            TrustedHelperActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            TrustedHelperActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.TRUSTED_HELPER_HANDOFF)
            TrustedHelperActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            TrustedHelperActionId.NEW_ANALYSIS -> show(Screen.CHECKER)
            TrustedHelperActionId.FAMILY_ALERT_MESSAGES -> show(Screen.FAMILY_ALERT_MESSAGES)
            TrustedHelperActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)
            TrustedHelperActionId.TIMELINE_BRIEFING -> show(Screen.TIMELINE_BRIEFING)
            TrustedHelperActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
        }
    }

    private fun shareTrustedHelperHandoff(message: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        startActivity(Intent.createChooser(shareIntent, "보호자에게 도움 요청 공유"))
    }

    private fun LinearLayout.title(textValue: String) = addView(TextView(this@MainActivity).apply {
        text = textValue
        textSize = 28f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(0xFF111827.toInt())
    }, LinearLayout.LayoutParams(match, wrap).withMargins(bottom = dp(8)))

    private fun LinearLayout.subtitle(textValue: String) = addView(TextView(this@MainActivity).apply {
        text = textValue
        textSize = 16f
        setTextColor(0xFF475569.toInt())
        setLineSpacing(4f, 1f)
    }, LinearLayout.LayoutParams(match, wrap).withMargins(bottom = dp(18)))

    private fun LinearLayout.label(textValue: String) = addView(TextView(this@MainActivity).apply {
        text = textValue
        textSize = 13f
        setTextColor(0xFF64748B.toInt())
    }, LinearLayout.LayoutParams(match, wrap).withMargins(bottom = dp(4)))

    private fun LinearLayout.section(heading: String, body: String) {
        val box = vertical {
            setPadding(dp(16), dp(14), dp(16), dp(14))
            setBackgroundColor(Color.WHITE)
            addView(TextView(this@MainActivity).apply {
                text = heading
                textSize = 17f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(0xFF0F172A.toInt())
            })
            addView(TextView(this@MainActivity).apply {
                text = body
                textSize = 15f
                setTextColor(0xFF475569.toInt())
                setPadding(0, dp(8), 0, 0)
                setLineSpacing(4f, 1f)
            })
        }
        addView(box, LinearLayout.LayoutParams(match, wrap).withMargins(bottom = dp(12)))
    }

    private fun LinearLayout.primaryButton(label: String, onClick: () -> Unit) {
        val action = button(label, 0xFF0F766E.toInt(), Color.WHITE, onClick).apply {
            minimumHeight = dp(52)
        }
        addView(action, LinearLayout.LayoutParams(match, wrap).withMargins(bottom = dp(10)))
    }

    private fun LinearLayout.secondaryButton(label: String, onClick: () -> Unit) {
        val action = button(label, Color.WHITE, 0xFF0F766E.toInt(), onClick).apply {
            minimumHeight = dp(48)
        }
        addView(action, LinearLayout.LayoutParams(match, wrap).withMargins(bottom = dp(10)))
    }

    private fun LinearLayout.giantButton(label: String, onClick: () -> Unit) {
        val action = button(label, 0xFF0F766E.toInt(), Color.WHITE, onClick).apply {
            minimumHeight = dp(84)
            textSize = 22f
            setLineSpacing(0f, 1.5f)
        }
        addView(action, LinearLayout.LayoutParams(match, wrap).withMargins(bottom = dp(16)))
    }

    private fun button(label: String, bg: Int, fg: Int, onClick: () -> Unit): Button =
        Button(this).apply {
            text = label
            textSize = 16f
            setTextColor(fg)
            setBackgroundColor(bg)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setOnClickListener { onClick() }
        }

    private fun vertical(block: LinearLayout.() -> Unit): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            block()
        }

    private fun LinearLayout.LayoutParams.withMargins(
        left: Int = 0,
        top: Int = 0,
        right: Int = 0,
        bottom: Int = 0
    ): LinearLayout.LayoutParams = apply { setMargins(left, top, right, bottom) }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun LinearLayout.familyReadinessRoadmapScreen() {
        val roadmap = FamilyReadinessRoadmapBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load())
        )
        val recommendedIds = roadmap.recommendedCardIds.toSet()

        title(roadmap.title)
        subtitle(roadmap.intro)
        section(
            "값 없는 가족 준비 요약",
            buildString {
                appendLine("준비 수준: ${roadmap.readinessSummary.levelLabel}")
                appendLine(
                    "준비 항목: ${roadmap.readinessSummary.completedCount}/" +
                        "${roadmap.readinessSummary.totalCount}"
                )
                append("아직 확인할 항목: ${roadmap.readinessSummary.remainingCount}개")
            }
        )
        section(
            roadmap.recommendationTitle,
            buildString {
                append(roadmap.recommendationExplanation)
                append("\n\n먼저 이어갈 앱 행동\n")
                append(
                    roadmap.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )
        roadmap.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openFamilyReadinessRoadmapAction(action) }
            } else {
                secondaryButton(action.label) { openFamilyReadinessRoadmapAction(action) }
            }
        }

        section(
            "추천 준비 카드",
            "현재 로컬 분석 신호와 값 없는 준비 수준에 맞춘 순서입니다. 선택이나 완료 상태는 저장되지 않습니다."
        )
        roadmap.recommendedCards.forEach { card ->
            familyReadinessRoadmapCard(card, recommended = true)
        }

        val remainingCards = roadmap.cards.filterNot { card -> card.id in recommendedIds }
        if (remainingCards.isNotEmpty()) {
            section(
                "나머지 가족 준비 지도",
                "추천과 관계없이 필요한 단계를 살펴보고 기존 안전 화면으로 직접 이동할 수 있습니다."
            )
            remainingCards.forEach { card ->
                familyReadinessRoadmapCard(card, recommended = false)
            }
        }

        section(
            "개인정보·안전 안내",
            roadmap.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 준비 로드맵 공유") {
            shareFamilyReadinessRoadmap(roadmap.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.familyReadinessRoadmapCard(
        card: FamilyReadinessRoadmapCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.stage.label)
            append(" · ")
            append(card.title)
        }
        section(
            heading,
            buildString {
                append("준비 상태 · ").append(card.readinessStatus.label)
                append("\n우선순위 · ").append(card.priority.label)
                append("\n\n왜 필요한가요?\n")
                append(card.whyItMatters)
                append("\n\n다음 작은 단계\n")
                card.nextSmallSteps.forEachIndexed { index, step ->
                    append(index + 1).append(". ").append(step).append('\n')
                }
                append("\n피해 주세요\n")
                card.avoidNotes.forEach { note -> append("• ").append(note).append('\n') }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openFamilyReadinessRoadmapAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openFamilyReadinessRoadmapAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openFamilyReadinessRoadmapAction(action) }
        }
    }

    private fun openFamilyReadinessRoadmapAction(action: FamilyReadinessRoadmapAction) {
        when (action.id) {
            FamilyReadinessRoadmapActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            FamilyReadinessRoadmapActionId.CHECKER -> show(Screen.CHECKER)
            FamilyReadinessRoadmapActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            FamilyReadinessRoadmapActionId.RISK_SIGNAL_EXPLAINERS ->
                show(Screen.RISK_SIGNAL_EXPLAINERS)
            FamilyReadinessRoadmapActionId.SAMPLE_LIBRARY -> openSampleLibrary()
            FamilyReadinessRoadmapActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            FamilyReadinessRoadmapActionId.SAFETY_PLAN -> show(Screen.PLAN)
            FamilyReadinessRoadmapActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            FamilyReadinessRoadmapActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            FamilyReadinessRoadmapActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            FamilyReadinessRoadmapActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.FAMILY_READINESS_ROADMAP)
            FamilyReadinessRoadmapActionId.RESPONSE_CENTER -> openResponseCenter()
            FamilyReadinessRoadmapActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            FamilyReadinessRoadmapActionId.TRUSTED_HELPER_HANDOFF ->
                show(Screen.TRUSTED_HELPER_HANDOFF)
            FamilyReadinessRoadmapActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            FamilyReadinessRoadmapActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)
            FamilyReadinessRoadmapActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            FamilyReadinessRoadmapActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
        }
    }

    private fun shareFamilyReadinessRoadmap(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 준비 로드맵 공유"))
    }

    private fun LinearLayout.riskSignalExplainerScreen() {
        val explainer = RiskSignalExplainerBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load())
        )
        val recommendedIds = explainer.recommendedSignalIds.toSet()

        title(explainer.title)
        subtitle(explainer.intro)
        section(
            explainer.recommendationTitle,
            buildString {
                append(explainer.recommendationExplanation)
                append("\n\n먼저 볼 설명 카드\n")
                append(
                    explainer.recommendedCards.joinToString("\n") { card ->
                        "• ${card.label} · ${card.category.label} · ${card.urgency.label}"
                    }
                )
            }
        )
        explainer.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openRiskSignalExplainerAction(action) }
            } else {
                secondaryButton(action.label) { openRiskSignalExplainerAction(action) }
            }
        }

        section(
            "추천 신호 자세히",
            "현재 상황과 가까운 카드를 먼저 보여드립니다. 추천은 로컬 규칙의 참고 안내이며 사기 여부를 확정하지 않습니다."
        )
        explainer.recommendedCards.forEach { card ->
            riskSignalExplainerCard(card, recommended = true)
        }

        val remainingCards = explainer.cards.filterNot { card -> card.id in recommendedIds }
        if (remainingCards.isNotEmpty()) {
            section(
                "그 밖의 분류별 위험 신호",
                "추천되지 않은 신호도 궁금하면 살펴보세요. 카드를 본 기록이나 선택은 저장되지 않습니다."
            )
            RiskSignalCategory.entries
                .sortedBy { category -> category.displayOrder }
                .forEach { category ->
                    val categoryCards = remainingCards.filter { card -> card.category == category }
                    if (categoryCards.isNotEmpty()) {
                        section(
                            "분류 · ${category.label}",
                            categoryCards.joinToString("\n") { card -> "• ${card.label}" }
                        )
                        categoryCards.forEach { card ->
                            riskSignalExplainerCard(card, recommended = false)
                        }
                    }
                }
        }

        section(
            "개인정보·안전 안내",
            explainer.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 위험 신호 설명 공유") {
            shareRiskSignalExplainerSummary(explainer.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.riskSignalExplainerCard(
        card: RiskSignalExplainerCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.label)
            append(" · ")
            append(card.category.label)
            append(" · ")
            append(card.urgency.label)
        }
        section(
            heading,
            buildString {
                append("왜 의심하나요?\n")
                append(card.whySuspicious)
                append("\n\n상대가 노리는 행동\n")
                append(card.scamPressureGoal)
                append("\n\n지금 안전하게 할 일\n")
                append(card.immediateSafeMove)
                append("\n\n가족과 이야기해 보기\n")
                append(card.familyDiscussionPrompt)
            }
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openRiskSignalExplainerAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openRiskSignalExplainerAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openRiskSignalExplainerAction(action) }
        }
    }

    private fun openRiskSignalExplainerAction(action: RiskSignalExplainerAction) {
        when (action.id) {
            RiskSignalExplainerActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            RiskSignalExplainerActionId.CHECKER -> show(Screen.CHECKER)
            RiskSignalExplainerActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            RiskSignalExplainerActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            RiskSignalExplainerActionId.SAFETY_PLAN -> show(Screen.PLAN)
            RiskSignalExplainerActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            RiskSignalExplainerActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            RiskSignalExplainerActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            RiskSignalExplainerActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.RISK_SIGNAL_EXPLAINERS)
            RiskSignalExplainerActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            RiskSignalExplainerActionId.TRUSTED_HELPER_HANDOFF ->
                show(Screen.TRUSTED_HELPER_HANDOFF)
            RiskSignalExplainerActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            RiskSignalExplainerActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)
        }
    }

    private fun shareRiskSignalExplainerSummary(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 위험 신호 설명 공유"))
    }

    private fun LinearLayout.safeVerificationCallScriptsScreen() {
        val scripts = SafeVerificationCallScriptsBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load())
        )
        val recommendedIds = scripts.recommendedScriptIds.toSet()

        title(scripts.title)
        subtitle(scripts.intro)
        secondaryButton("새 번호가 아닌 기존 경로로 확인하기 전 격리") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("전화 전 목소리 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        secondaryButton("현재 연락과 분리해 확인 경로 고르기") {
            show(Screen.SEPARATE_CHANNEL_VERIFICATION_COACH)
        }
        secondaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        section(
            scripts.recommendationTitle,
            buildString {
                append(scripts.recommendationExplanation)
                append("\n\n")
                append(
                    scripts.recommendedCards.joinToString("\n") { card ->
                        "• ${card.label} · ${card.urgency.label}"
                    }
                )
            }
        )

        scripts.cards.forEach { card ->
            val recommended = card.id in recommendedIds
            val heading = buildString {
                if (recommended) append("추천 · ")
                append(card.label)
                append(" · ")
                append(card.urgency.label)
            }
            section(
                heading,
                buildString {
                    append("누구에게 거나요\n")
                    append(card.whoToCall)
                    append("\n\n언제 거나요\n")
                    append(card.whenToUse)
                    append("\n\n먼저 이렇게 말하세요\n")
                    append(card.openingScript)
                    append("\n\n물어볼 내용\n")
                    card.questionsToAsk.forEachIndexed { index, question ->
                        append(index + 1).append(". ").append(question).append('\n')
                    }
                    append("\n절대 말하지 마세요\n")
                    card.neverSayNotes.forEach { note -> append("• ").append(note).append('\n') }
                }.trim()
            )
            if (recommended) {
                primaryButton(card.primaryAction.label) { openSafeCallScriptAction(card.primaryAction) }
            } else {
                secondaryButton(card.primaryAction.label) { openSafeCallScriptAction(card.primaryAction) }
            }
            card.secondaryActions.forEach { action ->
                secondaryButton(action.label) { openSafeCallScriptAction(action) }
            }
        }

        section(
            "개인정보·안전 안내",
            scripts.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 전화 스크립트 공유") {
            shareSafeCallScriptsSummary(scripts.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun openSafeCallScriptAction(action: SafeCallScriptAction) {
        when (action.id) {
            SafeCallScriptActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            SafeCallScriptActionId.CHECKER -> show(Screen.CHECKER)
            SafeCallScriptActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            SafeCallScriptActionId.FAMILY_CALLBACK_CARD -> openFamilyCallbackCard(currentAnalysis)
            SafeCallScriptActionId.SAFETY_PLAN -> show(Screen.PLAN)
            SafeCallScriptActionId.TRUSTED_HELPER_HANDOFF -> show(Screen.TRUSTED_HELPER_HANDOFF)
            SafeCallScriptActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)
            SafeCallScriptActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            SafeCallScriptActionId.RESPONSE_CENTER -> openResponseCenter()
            SafeCallScriptActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            SafeCallScriptActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            SafeCallScriptActionId.OFFICIAL_HELP_DIRECTORY -> openHelpDirectory(Screen.SAFE_CALL_SCRIPTS)
            SafeCallScriptActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            SafeCallScriptActionId.FAMILY_ALERT_MESSAGES -> show(Screen.FAMILY_ALERT_MESSAGES)
            SafeCallScriptActionId.NEW_ANALYSIS -> show(Screen.CHECKER)
        }
    }

    private fun shareSafeCallScriptsSummary(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 전화 스크립트 공유"))
    }

    private fun LinearLayout.exposureImpactTriageScreen() {
        val triage = ExposureImpactTriageBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load())
        )
        val recommendedIds = triage.recommendedExposureIds.toSet()

        title(triage.title)
        subtitle(triage.intro)
        secondaryButton("노출 대응과 현금 인출·이동·전달 지시를 분리해 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("신분증·셀피 노출 가능성을 값 없이 점검") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("링크·앱 설치 압박과 실제 행동을 분리해 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("상품권·코드 요구와 실제 행동을 분리해 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("회복 약속과 실제 노출 행동을 분리해 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 사칭·압박과 실제 행동을 분리해 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("노출됐을 수 있는 비밀값부터 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        section(
            triage.recommendationTitle,
            buildString {
                append(triage.recommendationExplanation)
                append("\n\n먼저 직접 확인할 분류\n")
                append(
                    triage.recommendedCards.joinToString("\n") { card ->
                        "• ${card.label} · ${card.category.label} · ${card.priority.label}"
                    }
                )
                append("\n\n분석 신호는 실제 노출을 뜻하지 않습니다. 본인이 한 행동만 직접 확인하세요.")
            }
        )
        triage.recommendedActions.forEachIndexed { index, action ->
            if (index == 0) {
                primaryButton(action.label) { openExposureImpactTriageAction(action) }
            } else {
                secondaryButton(action.label) { openExposureImpactTriageAction(action) }
            }
        }

        section(
            "추천 노출 분류",
            "현재 로컬 분석 신호와 값 없는 가족 준비 수준에서 확인할 가능성을 먼저 보여드립니다. 추천은 실제 행동의 증거나 확정 판정이 아닙니다."
        )
        triage.recommendedCards.forEach { card ->
            exposureImpactTriageCard(card, recommended = true)
        }

        val remainingCards = triage.cards.filterNot { card -> card.id in recommendedIds }
        if (remainingCards.isNotEmpty()) {
            section(
                "나머지 노출 분류",
                "추천과 관계없이 실제로 한 행동과 가까운 항목을 직접 확인할 수 있습니다. 선택과 확인 상태는 저장되지 않습니다."
            )
            remainingCards.forEach { card ->
                exposureImpactTriageCard(card, recommended = false)
            }
        }

        section(
            "개인정보·안전 안내",
            triage.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        primaryButton("가족에게 노출 분류 공유") {
            shareExposureImpactTriage(triage.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.exposureImpactTriageCard(
        card: ExposureImpactCard,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(card.label)
            append(" · ")
            append(card.category.label)
            append(" · ")
            append(card.priority.label)
        }
        section(
            heading,
            buildString {
                append("언제 해당될 수 있나요?\n")
                append(card.whenThisMightApply)
                append("\n\n첫 안전 행동\n")
                append(card.firstSafeMove)
                append("\n\n피해 주세요\n")
                card.whatToAvoid.forEach { note -> append("• ").append(note).append('\n') }
                append("\n공식 도움 전에 준비\n")
                card.officialHelpPrepNotes.forEach { note ->
                    append("• ").append(note).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(card.primaryAction.label) {
                openExposureImpactTriageAction(card.primaryAction)
            }
        } else {
            secondaryButton(card.primaryAction.label) {
                openExposureImpactTriageAction(card.primaryAction)
            }
        }
        card.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openExposureImpactTriageAction(action) }
        }
    }

    private fun openExposureImpactTriageAction(action: ExposureImpactAction) {
        when (action.id) {
            ExposureImpactActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            ExposureImpactActionId.CHECKER -> show(Screen.CHECKER)
            ExposureImpactActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            ExposureImpactActionId.RISK_SIGNAL_EXPLAINERS ->
                show(Screen.RISK_SIGNAL_EXPLAINERS)
            ExposureImpactActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            ExposureImpactActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            ExposureImpactActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            ExposureImpactActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            ExposureImpactActionId.EVIDENCE_KIT ->
                openEvidenceKit(action.evidenceSituation)
            ExposureImpactActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.EXPOSURE_IMPACT_TRIAGE)
            ExposureImpactActionId.REPORT_PREP ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.REPORT_PREP)
            ExposureImpactActionId.RESPONSE_CENTER ->
                openResponseCenter(action.responseSituation)
            ExposureImpactActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            ExposureImpactActionId.TRUSTED_HELPER_HANDOFF ->
                show(Screen.TRUSTED_HELPER_HANDOFF)
            ExposureImpactActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            ExposureImpactActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)
            ExposureImpactActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
        }
    }

    private fun shareExposureImpactTriage(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 노출 분류 공유"))
    }

    private fun LinearLayout.followUpCheckInPlannerScreen() {
        val planner = FollowUpCheckInPlannerBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load()),
            incidentCount = incidents.count()
        )
        val recommendedIds = planner.recommendedCheckpointIds.toSet()

        title(planner.title)
        subtitle(planner.intro)
        secondaryButton("후속 연락의 현금 인출·전달 지시 다시 멈춤") {
            show(Screen.CASH_WITHDRAWAL_COURIER_HANDOFF_PAUSE_COACH)
        }
        secondaryButton("은행·ATM 직원 공개 문구 코치") {
            show(Screen.BANK_ATM_STAFF_DISCLOSURE_SCRIPT_COACH)
        }
        secondaryButton("문 앞 방문·직접 전달 거절 문구 코치") {
            show(Screen.DOORSTEP_VISITOR_HANDOFF_REFUSAL_COACH)
        }
        secondaryButton("이동·만남 지시 멈춤 코치") {
            show(Screen.TRAVEL_ROUTE_MEETING_POINT_PAUSE_COACH)
        }
        secondaryButton("SIM·번호이동 멈춤 코치") {
            show(Screen.SIM_NUMBER_PORT_OUT_PAUSE_COACH)
        }
        secondaryButton("후속 연락의 신분증·셀피 요구 다시 멈춤") {
            show(Screen.ID_DOCUMENT_SELFIE_CAPTURE_PAUSE_COACH)
        }
        secondaryButton("후속 연락의 링크·앱 설치 지시 다시 격리") {
            show(Screen.LINK_APP_INSTALL_QUARANTINE_COACH)
        }
        secondaryButton("후속 연락의 상품권·코드 요구 다시 멈춤") {
            show(Screen.GIFT_CARD_CODE_PAUSE_COACH)
        }
        secondaryButton("후속 회복 약속을 믿기 전 다시 현실 점검") {
            show(Screen.RECOVERY_PROMISE_REALITY_CHECK_COACH)
        }
        secondaryButton("기관 사칭·압박이 이어질 때 다시 현실 점검") {
            show(Screen.OFFICIAL_PRESSURE_REALITY_CHECK_COACH)
        }
        secondaryButton("후속 점검에도 비밀값을 적지 않고 멈춤") {
            show(Screen.CREDENTIAL_SECRET_FREEZE_COACH)
        }
        secondaryButton("후속 점검 중에도 새 연락수단 격리") {
            show(Screen.NEW_NUMBER_QUARANTINE_COACH)
        }
        secondaryButton("다시 들린 목소리를 믿기 전 현실 확인") {
            show(Screen.VOICE_IMPERSONATION_REALITY_CHECK)
        }
        section(
            planner.recommendationTitle,
            buildString {
                append(planner.recommendationExplanation)
                append("\n\n먼저 직접 확인할 시점\n")
                append(
                    planner.recommendedCheckpoints.joinToString("\n") { checkpoint ->
                        "• ${checkpoint.timeLabel} · ${checkpoint.title} · ${checkpoint.priority.label}"
                    }
                )
                append("\n\n먼저 이어갈 앱 행동\n")
                append(
                    planner.recommendedActions.joinToString("\n") { action ->
                        "• ${action.label}"
                    }
                )
            }
        )

        section(
            "추천 후속 점검",
            "현재 로컬 분석 신호, 값 없는 가족 준비 수준, 저장 사건 개수만으로 먼저 볼 시점을 정했습니다. " +
                "각 항목은 가족이 해당 시점에 직접 확인하며 선택과 완료 상태는 저장되지 않습니다."
        )
        planner.recommendedCheckpoints.forEach { checkpoint ->
            followUpCheckInCard(checkpoint, recommended = true)
        }

        val remainingCheckpoints = planner.checkpoints.filterNot { checkpoint ->
            checkpoint.id in recommendedIds
        }
        if (remainingCheckpoints.isNotEmpty()) {
            section(
                "나머지 후속 점검",
                "추천과 관계없이 필요한 시점을 직접 살펴보세요. 앱이 계정·기기·메시지를 자동으로 확인한 결과가 아닙니다."
            )
            remainingCheckpoints.forEach { checkpoint ->
                followUpCheckInCard(checkpoint, recommended = false)
            }
        }

        section(
            "개인정보·안전 안내",
            planner.privacySafetyNotes.joinToString("\n") { note -> "• $note" }
        )
        secondaryButton("가족 역할 나눠 후속 대응하기") {
            show(Screen.FAMILY_RESPONSE_ROLE_BOARD)
        }
        secondaryButton("가족 차분 대화로 약속 정하기") {
            show(Screen.FAMILY_CALM_DEBRIEF_AGENDA)
        }
        secondaryButton("확인 뒤 결과별 다음 단계") {
            show(Screen.VERIFICATION_OUTCOME_NEXT_STEPS_BOARD)
        }
        secondaryButton("다시 연락할 때 경계 문장 정하기") {
            show(Screen.RECONTACT_BOUNDARY_COACH)
        }
        secondaryButton("가족 확인 비밀 재정비 순서") {
            show(Screen.FAMILY_SAFE_WORD_RESET_COACH)
        }
        secondaryButton("증거 전달 전 개인정보 확인") {
            show(Screen.EVIDENCE_HANDOFF_PRIVACY_COACH)
        }
        secondaryButton("사건 뒤 30·90일 가족 점검 달력") {
            show(Screen.POST_INCIDENT_REVIEW_CALENDAR)
        }
        primaryButton("가족에게 후속 점검 공유") {
            shareFollowUpCheckInPlanner(planner.familyShareSummary)
        }
        if (currentAnalysis != null) {
            secondaryButton("분석 결과로 돌아가기") { show(Screen.RESULT) }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.followUpCheckInCard(
        checkpoint: FollowUpCheckpoint,
        recommended: Boolean
    ) {
        val heading = buildString {
            if (recommended) append("추천 · ")
            append(checkpoint.timeLabel)
            append(" · ")
            append(checkpoint.title)
            append(" · ")
            append(checkpoint.category.label)
            append(" · ")
            append(checkpoint.priority.label)
        }
        section(
            heading,
            buildString {
                append("점검 목적\n")
                append(checkpoint.purpose)
                append("\n\n직접 확인할 일\n")
                checkpoint.manualCheckSteps.forEachIndexed { index, step ->
                    append(index + 1).append(". ").append(step).append('\n')
                }
                append("\n하지 마세요\n")
                checkpoint.whatNotToDo.forEach { note ->
                    append("• ").append(note).append('\n')
                }
            }.trim()
        )
        if (recommended) {
            primaryButton(checkpoint.primaryAction.label) {
                openFollowUpCheckInAction(checkpoint.primaryAction)
            }
        } else {
            secondaryButton(checkpoint.primaryAction.label) {
                openFollowUpCheckInAction(checkpoint.primaryAction)
            }
        }
        checkpoint.secondaryActions.forEach { action ->
            secondaryButton(action.label) { openFollowUpCheckInAction(action) }
        }
    }

    private fun openFollowUpCheckInAction(action: FollowUpCheckInAction) {
        when (action.id) {
            FollowUpCheckInActionId.QUICK_STOP -> show(Screen.QUICK_STOP)
            FollowUpCheckInActionId.EXPOSURE_IMPACT_TRIAGE ->
                show(Screen.EXPOSURE_IMPACT_TRIAGE)
            FollowUpCheckInActionId.TRUSTED_HELPER_REVIEW ->
                show(Screen.TRUSTED_HELPER_REVIEW)
            FollowUpCheckInActionId.CHECKER -> show(Screen.CHECKER)
            FollowUpCheckInActionId.ANALYSIS_RESULT ->
                show(if (currentAnalysis == null) Screen.CHECKER else Screen.RESULT)
            FollowUpCheckInActionId.FAMILY_CALLBACK_CARD ->
                openFamilyCallbackCard(currentAnalysis)
            FollowUpCheckInActionId.SAFE_CALL_SCRIPTS -> show(Screen.SAFE_CALL_SCRIPTS)
            FollowUpCheckInActionId.FAMILY_ALERT_MESSAGES ->
                show(Screen.FAMILY_ALERT_MESSAGES)
            FollowUpCheckInActionId.FAMILY_READINESS_ROADMAP ->
                show(Screen.FAMILY_READINESS_ROADMAP)
            FollowUpCheckInActionId.FAMILY_SAFETY_DRILL_COACH -> openDrill()
            FollowUpCheckInActionId.SAFETY_PLAN -> show(Screen.PLAN)
            FollowUpCheckInActionId.RECOVERY_PLAN -> show(Screen.RECOVERY_PLAN)
            FollowUpCheckInActionId.ACCOUNT_LOCKDOWN -> show(Screen.ACCOUNT_LOCKDOWN)
            FollowUpCheckInActionId.EVIDENCE_KIT -> openEvidenceKit(null)
            FollowUpCheckInActionId.OFFICIAL_HELP_DIRECTORY ->
                openHelpDirectory(Screen.FOLLOW_UP_CHECK_IN_PLANNER)
            FollowUpCheckInActionId.REPORT_PREP -> show(Screen.REPORT_PREP)
            FollowUpCheckInActionId.INCIDENT_TIMELINE_BRIEFING ->
                show(Screen.TIMELINE_BRIEFING)
            FollowUpCheckInActionId.SAFETY_FLOW_ROUTER -> show(Screen.FLOW_ROUTER)
            FollowUpCheckInActionId.PRIVACY_CENTER -> show(Screen.PRIVACY_CENTER)
        }
    }

    private fun shareFollowUpCheckInPlanner(summary: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "가족에게 후속 점검 공유"))
    }

    private val match = LinearLayout.LayoutParams.MATCH_PARENT
    private val wrap = LinearLayout.LayoutParams.WRAP_CONTENT
}
