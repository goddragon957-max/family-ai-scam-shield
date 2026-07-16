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
        ONBOARDING, HOME, CHECKER, RESULT, LOG, PLAN, RESPONSE_CENTER, EVIDENCE, DRILL, SAMPLES
    }

    private val prefs by lazy { getSharedPreferences("family_shield", MODE_PRIVATE) }
    private val incidents by lazy { IncidentStore(this) }
    private val safetyPlan by lazy { SafetyPlanStore(this) }
    private var currentAnalysis: ScamAnalysis? = null
    private var pendingSharedText: String? = null
    private var selectedIncidentSituation: IncidentSituation? = null
    private var selectedEvidenceSituation: IncidentSituation? = null
    private var selectedDrillScenario: DrillScenario? = null
    private var selectedSampleCategory: ScamSampleCategory? = null

    private val defaultCallbackScript =
        "1. 전화를 끊고 저장된 가족 번호로 다시 겁니다.\n" +
            "2. 안전 확인 질문을 물어보고 답을 확인합니다.\n" +
            "3. 답이 어색하면 송금, 앱 설치, 인증번호 제공을 거부합니다.\n" +
            "4. 필요하면 112 또는 1394에 신고합니다."

    companion object {
        private const val KEY_PENDING_SHARED_TEXT = "pending_shared_text"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingSharedText = savedInstanceState?.getString(KEY_PENDING_SHARED_TEXT) ?: extractSharedText(intent)
        if (savedInstanceState == null && !pendingSharedText.isNullOrBlank()) {
            clearSharedIntent()
        }
        val consented = prefs.getBoolean("consented", false)
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
            show(if (prefs.getBoolean("consented", false)) Screen.HOME else Screen.ONBOARDING)
            return
        }
        pendingSharedText = sharedText
        clearSharedIntent()
        show(if (prefs.getBoolean("consented", false)) Screen.CHECKER else Screen.ONBOARDING)
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
                prefs.edit().putBoolean("consented", true).apply()
                show(if (pendingSharedText.isNullOrBlank()) Screen.HOME else Screen.CHECKER)
            } else {
                toast("두 항목에 동의해야 시작할 수 있습니다.")
            }
        }
    }

    private fun LinearLayout.home() {
        title("가족 AI 보호자 대기 중")
        subtitle("돈, 앱 설치, 인증번호 요청은 먼저 멈추고 AI 보호자 분석 → 가족 콜백 → 신고/차단 순서로 처리하세요.")
        section("개인정보 보호", "분석은 로컬 규칙 기반 AI 보호자 엔진으로만 진행됩니다. 네트워크 권한, SMS 권한, 통화 권한을 요청하지 않습니다.")
        readinessSection()
        primaryButton("의심 메시지/통화 검사") { show(Screen.CHECKER) }
        secondaryButton("샘플로 AI 분석 연습") { openSampleLibrary() }
        secondaryButton("이미 피해가 의심돼요 / 긴급 대응 센터") { openResponseCenter() }
        secondaryButton("증거 보존 체크리스트") { openEvidenceKit(null) }
        secondaryButton("사건 기록 보기") { show(Screen.LOG) }
        section("AI 방지 루프", "1. 수상한 요청을 붙여넣기\n2. 위험 신호와 즉시 중단 행동 확인\n3. 기존 저장 번호로 가족 콜백\n4. 필요하면 112/1394 또는 은행 지급정지")
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
        val analysis = currentAnalysis ?: ScamAnalyzer.analyze("")
        title("AI 분석 결과")
        riskCard(analysis)
        actionPlanSection(analysis)
        aiGuardianSection(analysis)
        section("권장 행동", analysis.summary)
        familyPlanSection()
        section(
            "긴급 대응",
            "이미 돈을 보냈다면 즉시 은행 지급정지와 112 또는 1394 신고를 진행하세요. 원격제어를 허용했다면 " +
                "먼저 연결을 끊고 앱 정보를 안전하게 남긴 뒤, 다른 안전한 기기에서 계정을 보호하세요."
        )
        secondaryButton("이미 피해가 의심돼요 / 긴급 대응 센터") { openResponseCenter() }
        primaryButton("가족에게 안전 리포트 공유") { shareSafetyReport(analysis) }
        primaryButton("사건 기록에 저장") {
            incidents.add(analysis)
            toast("사건 기록에 저장했습니다.")
            show(Screen.LOG)
        }
        secondaryButton("다른 내용 검사") { show(Screen.CHECKER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun LinearLayout.incidentLog() {
        title("사건 기록")
        subtitle("기록은 이 기기의 SharedPreferences에만 저장됩니다. 최근 30개만 보관합니다.")
        val items = incidents.load()
        if (items.isEmpty()) {
            section("기록 없음", "저장된 분석 결과가 없습니다.")
        } else {
            items.forEach { incident ->
                section(
                    "${incidents.displayTime(incident.timestamp)} · ${incident.riskLabel} (${incident.score}점)",
                    "신호: ${incident.signals.ifBlank { "강한 신호 없음" }}\n내용: ${incident.preview}"
                )
            }
            secondaryButton("기록 모두 삭제") {
                incidents.clear()
                toast("사건 기록을 삭제했습니다.")
                show(Screen.LOG)
            }
        }
        primaryButton("새 검사") { show(Screen.CHECKER) }
        secondaryButton("홈으로") { show(Screen.HOME) }
    }

    private fun openResponseCenter() {
        selectedIncidentSituation = null
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
                secondaryButton("${contact.label} 전화 연결 (${contact.number})") { dial(contact.number) }
            }
            primaryButton("가족에게 대응 요약 공유") { shareIncidentResponse(plan) }
        }

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
        addView(button(label, 0xFF0F766E.toInt(), Color.WHITE, onClick), LinearLayout.LayoutParams(match, dp(52)).withMargins(bottom = dp(10)))
    }

    private fun LinearLayout.secondaryButton(label: String, onClick: () -> Unit) {
        addView(button(label, Color.WHITE, 0xFF0F766E.toInt(), onClick), LinearLayout.LayoutParams(match, dp(48)).withMargins(bottom = dp(10)))
    }

    private fun button(label: String, bg: Int, fg: Int, onClick: () -> Unit): Button =
        Button(this).apply {
            text = label
            textSize = 16f
            setTextColor(fg)
            setBackgroundColor(bg)
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
