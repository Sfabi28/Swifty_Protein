package com.example.swifty_protein.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.swifty_protein.data.DbHelper
import com.example.swifty_protein.data.SessionManager

class AuthViewModelFactory(
    private val dbHelper: DbHelper,
    private val sessionManager: SessionManager // Aggiunto
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(dbHelper, sessionManager) as T
    }
}
