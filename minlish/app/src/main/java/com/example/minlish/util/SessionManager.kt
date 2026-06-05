package com.example.minlish.util

import android.content.Context

object SessionManager {
    private const val PREF_NAME = "minlish_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_REMINDER_ENABLED = "reminder_enabled"
    private const val KEY_REMINDER_HOUR = "reminder_hour"
    private const val KEY_REMINDER_MINUTE = "reminder_minute"
    
    private const val KEY_EMAIL_REMINDER_ENABLED = "email_reminder_enabled"
    private const val KEY_EMAIL_REMINDER_HOUR = "email_reminder_hour"
    private const val KEY_EMAIL_REMINDER_MINUTE = "email_reminder_minute"

    private const val KEY_LANGUAGE = "language"

    fun saveLanguage(context: Context, lang: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANGUAGE, lang).apply()
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_LANGUAGE)) {
            // Return system language as default if not explicitly set
            val systemLang = java.util.Locale.getDefault().language
            return if (systemLang == "vi") "vi" else "en"
        }
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun saveUserId(context: Context, userId: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USER_ID, null)
    }

    fun saveReminderSettings(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_REMINDER_ENABLED, enabled)
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
    }

    fun saveEmailReminderSettings(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_EMAIL_REMINDER_ENABLED, enabled)
            .putInt(KEY_EMAIL_REMINDER_HOUR, hour)
            .putInt(KEY_EMAIL_REMINDER_MINUTE, minute)
            .apply()
    }

    fun isReminderEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_REMINDER_ENABLED, true)
    }

    fun getReminderHour(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_REMINDER_HOUR, 20)
    }

    fun getReminderMinute(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_REMINDER_MINUTE, 0)
    }

    fun isEmailReminderEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_EMAIL_REMINDER_ENABLED, true)
    }

    fun getEmailReminderHour(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_EMAIL_REMINDER_HOUR, 9) // Default 9 AM for email
    }

    fun getEmailReminderMinute(context: Context): Int {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_EMAIL_REMINDER_MINUTE, 0)
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}
