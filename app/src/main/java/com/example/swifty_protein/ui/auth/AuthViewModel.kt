package com.example.swifty_protein.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swifty_protein.data.DbHelper
import com.example.swifty_protein.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(
    private val db: DbHelper,
    private val sessionManager: SessionManager
) : ViewModel() {

    fun loginBiometric() {
        sessionManager.setLogin(true) // TODO fare accesso con utente specifico invece che accesso random
    }
    fun loginUser(username: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = db.getUser(username, pass)

            withContext(Dispatchers.Main) {
                if (userId != -1L) {
                    sessionManager.setLogin(true)
                    onResult(true, "Login successful")
                } else {
                    onResult(false, "Invalid username or password")
                }
            }
        }
    }

    fun registerUser(username: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = db.addUser(username, pass)

            withContext(Dispatchers.Main) {
                if (result.success) {
                    sessionManager.setLogin(true)
                }
                onResult(result.success, result.message)
            }
        }
    }

    fun logout() {
        sessionManager.setLogin(false)
    }

    fun checkLogin() = sessionManager.isLoggedIn()
}
