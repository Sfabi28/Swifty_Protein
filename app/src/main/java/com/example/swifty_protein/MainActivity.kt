package com.example.swifty_protein

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.swifty_protein.navigation.AppNavHost
import com.example.swifty_protein.ui.theme.Swifty_ProteinTheme
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Swifty_ProteinTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                }
            }
        }
    }
}


@Composable // proto login screen
fun LoginScreen(onNavigateToHome: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Login Screen");
        Button(onClick = {
            onNavigateToHome()
        }) {
            Text(text = "Go to Home")
        }
    }

}

@Preview(showBackground = true)
@Composable // la preview e statica percui possiamo decidere che screen visualizzare ma per un vero test c'è da emulare
fun LoginScreenPreview() {
    Swifty_ProteinTheme {

    }
}
