package com.example.core

import android.content.Context
import android.content.SharedPreferences

class ScreenlyPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("screenly_app_prefs", Context.MODE_PRIVATE)

    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_COMPLETED_ONBOARDING, value).apply()

    fun resetPreferences() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_HAS_COMPLETED_ONBOARDING = "key_has_completed_onboarding"
    }
}
