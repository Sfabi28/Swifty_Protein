package com.example.swifty_protein.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.swifty_protein.ui.auth.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel
) {
    val context = LocalContext.current
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val isInputValid = username.isNotEmpty() && password.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Swifty Protein",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            enabled = !isLoading
        )

        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.padding(bottom = 24.dp),
            enabled = !isLoading
        )

        SubmitButton(
            textId = "Login",
            loading = isLoading,
            validInputs = isInputValid,
            onClick = {
                isLoading = true
                viewModel.loginUser(username, password) { success, message ->
                    isLoading = false
                    if (success) {
                        Toast.makeText(context, message ?: "Login effettuato", Toast.LENGTH_SHORT).show()
                        onNavigateToHome()
                    } else {
                        Toast.makeText(context, message ?: "Credenziali errate", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SubmitButton(
            textId = "Register",
            loading = isLoading,
            validInputs = isInputValid,
            onClick = {
                isLoading = true
                viewModel.registerUser(username, password) { success, message ->
                    isLoading = false
                    Toast.makeText(context, message ?: "Registrazione completata", Toast.LENGTH_SHORT).show()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        IconButton(
            onClick = { /* Biometria */ },
            modifier = Modifier.size(64.dp),
            enabled = !isLoading
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Biometric Login",
                modifier = Modifier.size(48.dp),
                tint = if (isLoading) Color.Gray else Color(0xFFF47B20)
            )
        }
    }
}

@Composable
fun SubmitButton(
    textId: String,
    loading: Boolean,
    validInputs: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(3.dp)
            .fillMaxWidth(),
        enabled = !loading && validInputs,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF47B20))
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(25.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = textId, modifier = Modifier.padding(5.dp))
        }
    }
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var passwordHidden by rememberSaveable { mutableStateOf(true) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text("Password") },
        visualTransformation = if (passwordHidden) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = {
            IconButton(onClick = { passwordHidden = !passwordHidden }) {
                val visibilityIcon = if (passwordHidden) {
                    Icons.Filled.Visibility
                } else {
                    Icons.Filled.VisibilityOff
                }
                Icon(imageVector = visibilityIcon, contentDescription = null)
            }
        },
        singleLine = true
    )
}
