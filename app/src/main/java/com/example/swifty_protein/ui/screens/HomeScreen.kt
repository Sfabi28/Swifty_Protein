package com.example.swifty_protein.ui.screens

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swifty_protein.R
import com.example.swifty_protein.ui.viewmodel.LigandViewModel
import com.example.swifty_protein.data.FilamentHelper
import com.example.swifty_protein.data.Resource
import com.example.swifty_protein.model.Ligand
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState


@Composable
fun SearchBarLigand(onValueChange: (String) -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val notFoundText = stringResource(R.string.notFound)
    val listOfLigands = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        try {
            context.assets.open("ligands.txt").bufferedReader().useLines { lines ->
                lines.forEach { listOfLigands.add(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var isFocused by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }

    val listToShow = remember(text, listOfLigands.size) {
        if (text.isBlank()) listOfLigands.toList()
        else {
            val filtered = listOfLigands.filter { it.contains(text.trim(), ignoreCase = true) }
            filtered.ifEmpty {
                listOf(notFoundText)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.Top
    ) {
        TextField(
            value = text,
            onValueChange = {
                text = it
                onValueChange("")
            },
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
        if (isFocused) {
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
                            .clickable(enabled = !isNotFound) {
                                text = ligand
                                onValueChange(ligand)
                                focusManager.clearFocus()
                            }
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isNotFound) Color.Gray else Color.Unspecified,
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

@Composable
fun FilamentViewer(modifier: Modifier = Modifier, ligand : Ligand) {
    val context = LocalContext.current
    val filamentHelper = remember { mutableStateOf<FilamentHelper?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            SurfaceView(ctx).apply {
                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        // Creiamo e avviamo l'helper solo quando la superficie è pronta
                        val helper = FilamentHelper(ctx, this@apply, ligand)
                        filamentHelper.value = helper
                        helper.startRendering()
                    }

                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                        // Gestione eventuale del resize se necessario
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        filamentHelper.value?.destroy()
                        filamentHelper.value = null
                    }
                })
                setOnTouchListener { _, event ->
                    filamentHelper.value?.onTouchEvent(event)
                    true
                }
            }
        },
        onRelease = {
            filamentHelper.value?.destroy()
            filamentHelper.value = null
        }
    )
}

@Composable
fun HomeScreen(username: String, onBack: () -> Unit) {
    val viewModel: LigandViewModel = viewModel()
    var searchId by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val state = viewModel.ligandData



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome $username", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

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

        Text(text = "Ligande 3D (ball.glb):", style = MaterialTheme.typography.titleMedium)

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
                        val ligand = remember(state.data) {
                            state.data.also { it.parseCifData() }
                        }

                        FilamentViewer(modifier = Modifier.fillMaxSize(), ligand)


                    }

                    is Resource.Error -> {
                        Text(text = "ERRORE: ${state.message}", color = Color.Red)
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = { viewModel.updateCount() }) {
                Text("Ligandi nel DB: ${viewModel.ligandCount}")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { onBack() }) {
            Text(text = "Torna al Login")
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    com.example.swifty_protein.ui.theme.Swifty_ProteinTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                username = "User_Demo",
                onBack = { /* No-op in preview */ }
            )
        }
    }
}
