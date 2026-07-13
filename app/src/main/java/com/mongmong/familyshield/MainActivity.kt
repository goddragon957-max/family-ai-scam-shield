package com.mongmong.familyshield

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
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
    private enum class Screen { ONBOARDING, HOME, CHECKER, RESULT, LOG }

    private val prefs by lazy { getSharedPreferences("family_shield", MODE_PRIVATE) }
    private val incidents by lazy { IncidentStore(this) }
    private var currentAnalysis: ScamAnalysis? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        show(if (prefs.getBoolean("consented", false)) Screen.HOME else Screen.ONBOARDING)
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
            }
        }
        setContentView(ScrollView(this).apply {
            setBackgroundColor(0xFFF7F8FA.toInt())
            addView(content)
        })
    }

    private fun LinearLayout.onboarding() {
        title("가족 사기 방패")
        subtitle("의심스러운 문자, 카카오톡, 통화 내용을 붙여넣어 기기 안에서만 사기 위험 신호를 확인합니다.")
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
                show(Screen.HOME)
            } else {
                toast("두 항목에 동의해야 시작할 수 있습니다.")
            }
        }
    }

    private fun LinearLayout.home() {
        title("오늘도 확인하고 멈추기")
        subtitle("돈, 앱 설치, 인증번호 요청은 먼저 멈추고 가족 확인 루틴을 진행하세요.")
        section("개인정보 보호", "분석은 로컬 규칙으로만 진행됩니다. 네트워크 권한, SMS 권한, 통화 권한을 요청하지 않습니다.")
        primaryButton("의심 메시지 검사") { show(Screen.CHECKER) }
        secondaryButton("사건 기록 보기") { show(Screen.LOG) }
        section("가족 확인 루틴", "1. 기존에 저장된 번호로 직접 전화하기\n2. 가족만 아는 안전단어 묻기\n3. 송금, 상품권, 앱 설치는 확인 전 중단하기")
    }

    private fun LinearLayout.checker() {
        title("의심 내용 붙여넣기")
        subtitle("문자, 카카오톡, 통화 녹취를 직접 붙여넣으세요. 민감정보는 지우고 검사해도 됩니다.")
        val input = EditText(this@MainActivity).apply {
            hint = "예: 엄마 나 폰 고장났어. 급히 계좌로 돈 보내줘..."
            minLines = 8
            gravity = Gravity.TOP
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setBackgroundColor(Color.WHITE)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
        addView(input, LinearLayout.LayoutParams(match, dp(180)).withMargins(bottom = dp(16)))
        primaryButton("로컬 분석하기") {
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
        title("분석 결과")
        riskCard(analysis)
        section("권장 행동", analysis.summary)
        section("가족 콜백/안전단어", "앱 안의 결과만 믿지 말고 기존 연락처로 직접 전화하세요. 가족끼리 정한 안전단어를 묻고, 답이 어색하면 송금을 멈추세요.")
        section("긴급 대응", "이미 돈을 보냈다면 즉시 은행 지급정지, 112 또는 182 신고, 계정 비밀번호 변경, 원격제어 앱 삭제를 진행하세요.")
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
