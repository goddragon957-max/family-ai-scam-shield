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
        ACCOUNT_LOCKDOWN, FAMILY_ALERT_MESSAGES, QUICK_STOP, RECOVERY_PLAN
    }

    private val prefs by lazy { getSharedPreferences("family_shield", MODE_PRIVATE) }
    private val incidents by lazy { IncidentStore(this) }
    private val safetyPlan by lazy { SafetyPlanStore(this) }
    private var currentAnalysis: ScamAnalysis? = null
    private var callbackCardAnalysis: ScamAnalysis? = null
    private var pendingSharedText: String? = null
    private var selectedIncidentSituation: IncidentSituation? = null
    private var selectedEvidenceSituation: IncidentSituation? = null
    private var selectedDrillScenario: DrillScenario? = null
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
            }
        }
        setContentView(ScrollView(this).apply {
            setBackgroundColor(0xFFF7F8FA.toInt())
            addView(content)
        })
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
        title("가족 AI 보호자 대기 중")
        subtitle("돈, 앱 설치, 인증번호 요청은 먼저 멈추고 AI 보호자 분석 → 가족 콜백 → 신고/차단 순서로 처리하세요.")
        primaryButton("긴급 멈춤 모드") { show(Screen.QUICK_STOP) }
        primaryButton("지금 뭐부터 할지 고르기") { show(Screen.FLOW_ROUTER) }
        secondaryButton("24시간 수습 계획") { show(Screen.RECOVERY_PLAN) }
        secondaryButton("계정·기기 잠금 체크리스트") { show(Screen.ACCOUNT_LOCKDOWN) }
        secondaryButton("가족 경고 메시지 만들기") { show(Screen.FAMILY_ALERT_MESSAGES) }
        section("개인정보 보호", "분석은 로컬 규칙 기반 AI 보호자 엔진으로만 진행됩니다. 네트워크 권한, SMS 권한, 통화 권한을 요청하지 않습니다.")
        secondaryButton("개인정보 센터") { show(Screen.PRIVACY_CENTER) }
        readinessSection()
        primaryButton("의심 메시지/통화 검사") { show(Screen.CHECKER) }
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

    private fun LinearLayout.quickStopModeScreen() {
        val mode = QuickStopModeBuilder.build(
            analysis = currentAnalysis,
            readiness = FamilyReadinessBuilder.build(safetyPlan.load())
        )
        val recommendedIds = mode.recommendedCardIds.toSet()

        title(mode.title)
        subtitle(mode.intro)
        section(
            mode.stopScriptTitle,
            mode.stopScriptSteps.joinToString("\n") { step ->
                "${step.displayOrder}. ${step.instruction}"
            }
        )
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
        section(
            "로컬·수동 안내",
            "연락처와 말할 내용은 앱에 내장되어 있으며 네트워크에서 조회하지 않습니다. " +
                "전화 버튼은 통화를 자동으로 걸지 않고 전화 앱만 엽니다."
        )

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
        primaryButton("가족에게 신고 연락처 공유") { shareHelpDirectory(directory) }
        if (helpDirectoryReturnScreen != Screen.HOME) {
            secondaryButton("이전 화면으로") { show(helpDirectoryReturnScreen) }
        }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.officialReportPrepScreen() {
        title("신고용 상담 준비 카드")
        subtitle("현재 AI 보호자 분석을 개인정보 가림 단서, 추천 공식 연락처, 상담 대사와 전화 전 체크리스트로 정리합니다.")

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
        if (readiness.level == ReadinessLevel.READY) {
            secondaryButton("가족 안전 연습 / 3분 훈련") { openDrill() }
            secondaryButton("가족 안전 계획 설정") { show(Screen.PLAN) }
        } else {
            secondaryButton("가족 안전 계획 설정") { show(Screen.PLAN) }
            secondaryButton("가족 안전 연습 / 3분 훈련") { openDrill() }
        }
    }

    private fun openDrill() {
        selectedDrillScenario = null
        show(Screen.DRILL)
    }

    private fun LinearLayout.drillScreen() {
        title("가족 안전 연습 · 3분 훈련")
        subtitle("실제 사건이 아닌 앱에 내장된 가상 시나리오로, 위급한 순간에 쓸 안전단어 질문과 콜백 루틴을 미리 연습합니다.")
        section("개인정보 보호", "훈련은 내장된 가상 시나리오만 사용합니다. 연습 내용이나 결과를 저장하거나 전송하지 않습니다.")

        val plan = safetyPlan.load()
        val readiness = FamilyReadinessBuilder.build(plan)
        if (plan.safeWord.isNotBlank()) {
            section("우리 가족 연습 질문", "훈련 중 상대 역할에게 이렇게 물어보세요: “${plan.safeWord}”\n답이 어색하면 요구를 거절하고 저장된 번호로 콜백하는 것까지 연습하세요.")
        }
        if (readiness.level == ReadinessLevel.READY) {
            section("연습 준비 상태 · ${readiness.level.label}", "안전단어, 콜백 연락처, 콜백 스크립트가 모두 저장되어 있어요. 저장된 루틴 그대로 연습하면 됩니다.")
        } else {
            section("연습 전 점검 · ${readiness.level.label}", "저장된 안전 계획이 아직 부족해요. 계획을 먼저 채우면 우리 가족만의 질문과 번호로 연습할 수 있습니다.\n다음 할 일: ${readiness.nextAction}")
            primaryButton("가족 안전 계획 설정") { show(Screen.PLAN) }
        }

        section("연습 방법", SafetyDrillBuilder.FICTION_NOTICE + "\n가족 한 명이 아래 시나리오의 가상 대사를 읽어주고, 다른 가족이 대응을 연습하세요.")

        DrillScenario.entries.forEach { scenario ->
            secondaryButton(scenario.label) {
                selectedDrillScenario = scenario
                show(Screen.DRILL)
            }
        }

        val scenario = selectedDrillScenario
        if (scenario != null) {
            val drill = SafetyDrillBuilder.build(scenario)
            section(drill.title, drill.fictionalScript)
            section(
                "이렇게 말하고 행동하세요",
                drill.responseSteps.mapIndexed { index, step -> "${index + 1}. $step" }.joinToString("\n")
            )
            section(
                "통과 체크리스트",
                drill.passChecklist.joinToString("\n") { "• $it" }
            )
        }

        secondaryButton("홈으로") { show(Screen.HOME) }
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

    private val match = LinearLayout.LayoutParams.MATCH_PARENT
    private val wrap = LinearLayout.LayoutParams.WRAP_CONTENT
}
