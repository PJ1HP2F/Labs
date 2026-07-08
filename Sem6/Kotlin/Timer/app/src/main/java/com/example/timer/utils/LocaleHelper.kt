package com.example.timerapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import java.util.Locale

object LocaleHelper {

    fun applyLocale(context: Context): Context {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val language = prefs.getString("locale_preference", "en") ?: "en"
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun setLocale(context: Context, language: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString("locale_preference", language)
            .apply()
    }
}
