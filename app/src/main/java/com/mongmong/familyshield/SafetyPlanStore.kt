package com.mongmong.familyshield

import android.content.Context
import android.util.Base64

data class TrustedContact(val label: String, val number: String)

data class SafetyPlan(
    val safeWord: String,
    val callbackScript: String,
    val contacts: List<TrustedContact>
) {
    val isBlank: Boolean
        get() = safeWord.isBlank() && callbackScript.isBlank() && contacts.isEmpty()
}

class SafetyPlanStore(context: Context) {
    private val prefs = context.getSharedPreferences("safety_plan", Context.MODE_PRIVATE)

    fun load(): SafetyPlan = SafetyPlan(
        safeWord = prefs.getString(KEY_SAFE_WORD, "").orEmpty(),
        callbackScript = prefs.getString(KEY_SCRIPT, "").orEmpty(),
        contacts = prefs.getString(KEY_CONTACTS, null).orEmpty()
            .lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull(::decodeContact)
            .toList()
    )

    fun save(plan: SafetyPlan) {
        prefs.edit()
            .putString(KEY_SAFE_WORD, plan.safeWord)
            .putString(KEY_SCRIPT, plan.callbackScript)
            .putString(KEY_CONTACTS, plan.contacts.joinToString("\n") { encodeContact(it) })
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun encodeContact(contact: TrustedContact): String = listOf(contact.label, contact.number)
        .joinToString("|") { Base64.encodeToString(it.toByteArray(), Base64.NO_WRAP) }

    private fun decodeContact(line: String): TrustedContact? {
        val parts = line.split("|")
        if (parts.size != 2) return null
        return runCatching {
            val decoded = parts.map { String(Base64.decode(it, Base64.NO_WRAP)) }
            TrustedContact(decoded[0], decoded[1])
        }.getOrNull()
    }

    companion object {
        private const val KEY_SAFE_WORD = "safe_word"
        private const val KEY_SCRIPT = "callback_script"
        private const val KEY_CONTACTS = "contacts"
    }
}
