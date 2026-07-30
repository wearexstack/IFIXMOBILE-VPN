package com.example.data

import android.content.Context

/** Persists login when user checks «مرا به خاطر بسپار». */
class SessionStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun saveSession(username: String, password: String) {
        prefs.edit()
            .putString(KEY_USER, username)
            .putString(KEY_PASS, password)
            .putBoolean(KEY_REMEMBER, true)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun isRemembered(): Boolean = prefs.getBoolean(KEY_REMEMBER, false)

    fun username(): String? = prefs.getString(KEY_USER, null)

    fun password(): String? = prefs.getString(KEY_PASS, null)

    companion object {
        private const val PREFS = "ifixmobile_session"
        private const val KEY_USER = "username"
        private const val KEY_PASS = "password"
        private const val KEY_REMEMBER = "remember"
    }
}
