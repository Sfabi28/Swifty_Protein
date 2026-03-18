package com.example.swifty_protein

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.swifty_protein.data.AppLifecycleObserver
import com.example.swifty_protein.navigation.AppNavHost
import com.example.swifty_protein.ui.theme.Swifty_ProteinTheme

class MainActivity : FragmentActivity() {
    
    private val logoutTrigger = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            AppLifecycleObserver(
                onAppBackground = {
                    logoutTrigger.value = true
                },
                onAppForeground = {
                }
            )
        )

        enableEdgeToEdge()
        setContent {
            Swifty_ProteinTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost(logoutTrigger = logoutTrigger)
                }
            }
        }
    }
}
