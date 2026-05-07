package de.clueventure.clue_venture

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Login/Register Screen for user authentication
 */
@Composable
fun AuthScreen(
    onLoginSuccess: (user: User) -> Unit,
    onNavigateToAdventureList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var showLoginScreen by remember { mutableStateOf(true) }

    if (showLoginScreen) {
        if (isLoginMode) {
            LoginScreenUI(
                onLoginSuccess = onLoginSuccess,
                onSwitchToRegister = { isLoginMode = false },
                modifier = modifier,
            )
        } else {
            RegisterScreenUI(
                onRegisterSuccess = { onLoginSuccess(it) },
                onSwitchToLogin = { isLoginMode = true },
                modifier = modifier,
            )
        }
    }
}

@Composable
fun LoginScreenUI(
    onLoginSuccess: (user: User) -> Unit,
    onSwitchToRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "🗺️ Clue Venture",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = "Anmelden",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-Mail") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                )

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                Button(
                    onClick = {
                        if (email.isEmpty() || password.isEmpty()) {
                            errorMessage = "Bitte alle Felder ausfüllen"
                        } else {
                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    val user = authenticateUser(email, password)
                                    if (user != null) {
                                        onLoginSuccess(user)
                                        errorMessage = ""
                                    } else {
                                        errorMessage = "Ungültige Anmeldedaten"
                                        isLoading = false
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Fehler: ${e.message}"
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isLoading,
                ) {
                    Text(if (isLoading) "Wird angemeldet..." else "Anmelden")
                }

                TextButton(
                    onClick = onSwitchToRegister,
                ) {
                    Text("Noch kein Konto? Registrieren")
                }
            }
        }
    }
}

@Composable
fun RegisterScreenUI(
    onRegisterSuccess: (user: User) -> Unit,
    onSwitchToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "🗺️ Clue Venture",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = "Registrieren",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Benutzername") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-Mail") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = passwordConfirm,
                    onValueChange = { passwordConfirm = it },
                    label = { Text("Passwort bestätigen") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                )

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                Button(
                    onClick = {
                        when {
                            username.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                                errorMessage = "Bitte alle Felder ausfüllen"
                            }

                            password != passwordConfirm -> {
                                errorMessage = "Passwörter stimmen nicht überein"
                            }

                            password.length < 6 -> {
                                errorMessage = "Passwort muss mindestens 6 Zeichen lang sein"
                            }

                            else -> {
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        val user = registerUser(email, password, username)
                                        if (user != null) {
                                            onRegisterSuccess(user)
                                            errorMessage = ""
                                        } else {
                                            errorMessage = "Registrierung fehlgeschlagen"
                                            isLoading = false
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Fehler: ${e.message}"
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isLoading,
                ) {
                    Text(if (isLoading) "Wird registriert..." else "Registrieren")
                }

                TextButton(
                    onClick = onSwitchToLogin,
                ) {
                    Text("Bereits registriert? Anmelden")
                }
            }
        }
    }
}
