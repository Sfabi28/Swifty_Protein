package com.example.swifty_protein.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swifty_protein.data.ApiClient
import com.example.swifty_protein.data.Resource
import kotlinx.coroutines.launch

private class LigandViewModel : ViewModel() {
    var ligandData by mutableStateOf<Resource<String>>(Resource.Loading)
        private set

    fun fetchLigand(id: String) {
        viewModelScope.launch {
            ligandData = Resource.Loading
            ligandData = ApiClient.safeApiCall {
                ApiClient.rcsbService.getLigandCif(id)
            }
        }
    }
}

@Composable
fun HomeScreen(onBack: () -> Unit) {
    val viewModel = remember { LigandViewModel() }
    var searchId by remember { mutableStateOf("") }
    val state = viewModel.ligandData

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Test API RCSB", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = searchId,
            onValueChange = { searchId = it },
            label = { Text("ID Ligando") },
            placeholder = { Text("Es: 1EJ") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (searchId.isNotBlank()) {
                    viewModel.fetchLigand(searchId.trim().uppercase())
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Scarica Dati .CIF")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Risultato (Raw):", style = MaterialTheme.typography.titleMedium)

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Box(modifier = Modifier.padding(10.dp)) {
                when (state) {
                    is Resource.Loading -> {
                        Text("Pronto per la ricerca...")
                    }
                    is Resource.Success -> {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text(
                                text = state.data,
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                                )
                            )
                        }
                    }
                    is Resource.Error -> {
                        Text(text = "ERRORE: ${state.message}", color = Color.Red)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { onBack() }) {
            Text(text = "Torna al Login")
        }
    }
}