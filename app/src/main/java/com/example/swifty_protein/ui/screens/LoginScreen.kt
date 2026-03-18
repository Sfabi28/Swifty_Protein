package com.example.swifty_protein.ui.screens

import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.swifty_protein.ui.auth.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel
) {
    val context = LocalContext.current
    val activity = context.findFragmentActivity()
    val biometricPossible = remember { isBiometricReady(context) }

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

        if (biometricPossible) {
            IconButton(
                onClick = {
                    activity?.let {
                        showBiometricPrompt(
                            activity = it,
                            onSuccess = {
                                viewModel.loginBiometric()
                                onNavigateToHome()
                            },
                            onError = { errorMsg ->
                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    } ?: run {
                        Toast.makeText(context, "Errore interno: Activity non trovata", Toast.LENGTH_SHORT).show()
                    }
                },
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
}

private fun hasBiometricCapability(context: Context): Int {
    return BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
}

fun isBiometricReady(context: Context) =
    hasBiometricCapability(context) == BiometricManager.BIOMETRIC_SUCCESS

fun Context.findFragmentActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

private fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Login Biometrico")
        .setSubtitle("Accedi a Swifty Protein")
        .setNegativeButtonText("Usa password")
        .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
        .build()

    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Impronta o volto non riconosciuti.")
            }
        })

    biometricPrompt.authenticate(promptInfo)
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