package com.example.swifty_protein.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("SwiftyProteinPrefs", Context.MODE_PRIVATE)

    fun setLogin(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean("KEY_IS_LOGGED_IN", isLoggedIn).commit()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("KEY_IS_LOGGED_IN", false)
    }
    fun saveLastUser(username: String) {
        sharedPreferences.edit().putString("last_logged_user", username).apply()
    }

    fun getLastUser(): String? {
        return sharedPreferences.getString("last_logged_user", null)
    }
}