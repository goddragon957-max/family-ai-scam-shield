package com.mongmong.familyshield

import android.content.Context

class SimpleModePreference(context: Context) {
    private val prefs = context.getSharedPreferences("family_shield", Context.MODE_PRIVATE)

    fun isOn(): Boolean = prefs.getBoolean(KEY_SIMPLE_MODE, false)

    fun setOn(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SIMPLE_MODE, enabled).apply()
    }

    companion object {
        const val KEY_SIMPLE_MODE = "pref_simple_mode"
    }
}
