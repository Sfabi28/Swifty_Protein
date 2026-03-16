package com.example.swifty_protein

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.swifty_protein.navigation.AppNavHost
import com.example.swifty_protein.ui.theme.Swifty_ProteinTheme
import com.example.swifty_protein.ui.auth.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Swifty_ProteinTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()  ///navhost per navigare tra le schermate
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable // la preview e statica percui possiamo decidere che screen visualizzare ma per un vero test c'è da emulare
fun LoginScreenPreview() {
    Swifty_ProteinTheme {
        LoginScreen(onNavigateToHome = {})
    }
}
