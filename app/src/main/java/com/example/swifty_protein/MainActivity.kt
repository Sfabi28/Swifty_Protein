package com.example.swifty_protein

import android.os.Bundle
import android.widget.Button
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swifty_protein.data.ApiClient
import com.example.swifty_protein.data.Resource
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

class LigandViewModel : ViewModel() {
    // Questo è lo stato che la UI osserverà
    var ligandData by mutableStateOf<Resource<String>>(Resource.Loading)
        private set

    fun fetchLigand(id: String) {
        viewModelScope.launch {
            ligandData = Resource.Loading
            // Chiamata sicura usando safeApiCall che abbiamo definito
            ligandData = ApiClient.safeApiCall {
                ApiClient.rcsbService.getLigandCif(id)
            }
        }
    }
}

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
@Composable
fun SearchBarLigand(onValueChange: (String) -> Unit){
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val notFoundText = stringResource(R.string.notFound)
    val listOfLigands = remember { mutableStateListOf<String>() }
    LaunchedEffect(Unit) {
        try {
            context.assets.open("ligands.txt").bufferedReader().useLines { lines ->
                lines.forEach { listOfLigands.add(it) }
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }
    var isFocused by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }

    val listToShow = remember(text, listOfLigands.size){
        if (text.isBlank()) listOfLigands.toList()
        else {
            val filtered = listOfLigands.filter { it.contains(text.trim(), ignoreCase = true) }
            filtered.ifEmpty {
                listOf(notFoundText)
            }

        }
    }
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ){
        TextField(
            value = text,
            onValueChange = {text = it},
            label = { Text("Filtra Ligandi") },
            placeholder = { Text("Es: 1EJ") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    isFocused = it.isFocused
                }

        )
        Spacer(modifier = Modifier.height(10.dp))
        if (isFocused){
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                items(listToShow) { ligand ->
                    val isNotFound = ligand == notFoundText
                    Text(
                        text = ligand,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(!isNotFound) {
                                onValueChange(ligand)
                                text = ligand
                                focusManager.clearFocus()
                            }
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isNotFound) Color.Gray else Color.Unspecified
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable //proto home screen
fun HomeScreen(onBack: () -> Unit) {
    val viewModel = remember { LigandViewModel() }
    var searchId by remember { mutableStateOf("") }
    val state = viewModel.ligandData
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .pointerInput(Unit) {
                detectTapGestures ( onTap = {
                    focusManager.clearFocus()
                })
            },

        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Test API RCSB", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        // Input per l'ID del ligando (es. 1EJ, ABC, 001)
        Spacer(modifier = Modifier.height(10.dp))


        SearchBarLigand(onValueChange = { searchId = it })
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

        // Area Risultato
        Text(text = "Risultato (Raw):", style = MaterialTheme.typography.titleMedium)

        Surface(
            modifier = Modifier
                .weight(1f) // Occupa lo spazio rimanente
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
                        // Il file CIF può essere lunghissimo, serve lo scroll
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text(
                                text = state.data,
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = MaterialTheme.typography.bodySmall.fontSize)
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



@Preview(showBackground = true)
@Composable // la preview e statica percui possiamo decidere che screen visualizzare ma per un vero test c'è da emulare
fun LoginScreenPreview() {
    Swifty_ProteinTheme {
        HomeScreen(onBack = {})
    }
}
