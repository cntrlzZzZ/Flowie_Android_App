package fr.eurecom.flowie.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.eurecom.flowie.data.remote.AuthManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onContinueAsGuest: () -> Unit,
    onLoginStarted: () -> Unit
) {
    val backgroundColor = Color(0xFF0B0B0F)
    val surfaceColor = Color(0xFF12121A)
    val borderColor = Color(0xFF2A2A35)
    val accentBlue = Color(0xFF3B82F6)
    val textPrimary = Color.White
    val textSecondary = Color(0xFFB3B3C2)

    var email by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login",
                color = textPrimary,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "We’ll send a login link to your email.",
                color = textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; error = null },
                label = { Text("Email", color = textSecondary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = surfaceColor,
                    focusedContainerColor = surfaceColor,
                    unfocusedTextColor = textPrimary,
                    focusedTextColor = textPrimary,
                    unfocusedIndicatorColor = borderColor,
                    focusedIndicatorColor = accentBlue,
                    cursorColor = accentBlue
                )
            )

            if (error != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = error!!,
                    color = Color(0xFFFF6B6B),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val trimmed = email.trim()
                    if (!trimmed.contains("@") || !trimmed.contains(".")) {
                        error = "Please enter a valid email."
                        return@Button
                    }

                    scope.launch {
                        isSending = true
                        error = null
                        try {
                            AuthManager.sendMagicLink(
                                email = trimmed,
                                redirectUrl = "fr.eurecom.flowie://login"
                            )
                            onLoginStarted()
                        } catch (e: Exception) {
                            error = e.message ?: "Failed to send link."
                        } finally {
                            isSending = false
                        }
                    }
                },
                enabled = !isSending,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentBlue,
                    contentColor = textPrimary,
                    disabledContainerColor = Color(0xFF2A2A35),
                    disabledContentColor = textSecondary
                )
            ) {
                Text(if (isSending) "Sending…" else "Send login link")
            }

            Spacer(Modifier.height(14.dp))

            OutlinedButton(
                onClick = onContinueAsGuest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
            ) {
                Text("Continue without login")
            }
        }
    }
}
