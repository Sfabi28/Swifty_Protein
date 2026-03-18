package com.example.swifty_protein.data

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("swifty_prefs", Context.MODE_PRIVATE)

    fun setLogin(isLoggedIn: Boolean) {
        prefs.edit().putBoolean("is_logged_in", isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun saveLastUser(username: String) {
        prefs.edit().putString("last_logged_user", username).apply()
    }

    fun getLastUser(): String? {
        return prefs.getString("last_logged_user", null)
    }
}