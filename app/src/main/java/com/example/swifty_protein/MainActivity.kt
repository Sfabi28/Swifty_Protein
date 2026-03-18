package com.example.swifty_protein

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.swifty_protein.data.AppLifecycleObserver
import com.example.swifty_protein.data.SessionManager
import com.example.swifty_protein.navigation.AppNavHost
import com.example.swifty_protein.ui.theme.Swifty_ProteinTheme

class MainActivity : ComponentActivity() {
    private val logoutTrigger = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            AppLifecycleObserver(
                onAppBackground = {
                    sessionManager.setLogin(false)
                },
                onAppForeground = {
                    if (!sessionManager.isLoggedIn()) {
                        logoutTrigger.value = true
                    }
                }
            )
        )

        setContent {
            Swifty_ProteinTheme {
                AppNavHost(logoutTrigger)
            }
        }
    }
}
