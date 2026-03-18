package com.example.swifty_protein.data

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("swifty_prefs", Context.MODE_PRIVATE)

    fun setLogin(isLoggedIn: Boolean) {
        prefs.edit().putBoolean("is_logged_in", isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }
}