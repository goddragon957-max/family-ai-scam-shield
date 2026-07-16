package com.mongmong.familyshield

import android.content.Context
import android.util.Base64
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Incident(
    val timestamp: Long,
    val riskLabel: String,
    val score: Int,
    val signals: String,
    val preview: String
)

class IncidentStore(context: Context) {
    private val prefs = context.getSharedPreferences("incident_log", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)

    fun add(analysis: ScamAnalysis) {
        val incident = Incident(
            timestamp = System.currentTimeMillis(),
            riskLabel = analysis.riskLevel.label,
            score = analysis.score,
            signals = analysis.matchedSignals.joinToString(", ") { it.label },
            preview = IncidentPreviewRedactor.build(analysis)
        )
        val next = (load() + incident).sortedByDescending { it.timestamp }.take(30)
        prefs.edit().putString("items", next.joinToString("\n") { encode(it) }).apply()
    }

    fun load(): List<Incident> {
        val saved = prefs.getString("items", null).orEmpty()
        val decoded = saved.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull(::decode)
            .toList()

        val sanitized = decoded.map { incident ->
            incident.copy(preview = IncidentPreviewRedactor.redact(incident.preview))
        }
        if (sanitized != decoded) {
            prefs.edit().putString("items", sanitized.joinToString("\n") { encode(it) }).apply()
        }

        return sanitized
            .sortedByDescending { it.timestamp }
    }

    fun count(): Int = prefs.getString("items", null)
        .orEmpty()
        .lineSequence()
        .filter { it.isNotBlank() }
        .mapNotNull(::decode)
        .count()

    fun clear() {
        prefs.edit().remove("items").apply()
    }

    fun displayTime(timestamp: Long): String = dateFormat.format(Date(timestamp))

    private fun encode(incident: Incident): String = listOf(
        incident.timestamp.toString(),
        incident.riskLabel,
        incident.score.toString(),
        incident.signals,
        incident.preview
    ).joinToString("|") { Base64.encodeToString(it.toByteArray(), Base64.NO_WRAP) }

    private fun decode(line: String): Incident? {
        val parts = line.split("|")
        if (parts.size != 5) return null
        return runCatching {
            val decoded = parts.map { String(Base64.decode(it, Base64.NO_WRAP)) }
            Incident(
                timestamp = decoded[0].toLong(),
                riskLabel = decoded[1],
                score = decoded[2].toInt(),
                signals = decoded[3],
                preview = decoded[4]
            )
        }.getOrNull()
    }
}
