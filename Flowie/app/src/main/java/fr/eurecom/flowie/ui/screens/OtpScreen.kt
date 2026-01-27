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
fun OtpScreen(
    email: String,
    onBack: () -> Unit,
    onVerified: () -> Unit
) {
    val backgroundColor = Color(0xFF0B0B0F)
    val surfaceColor = Color(0xFF12121A)
    val borderColor = Color(0xFF2A2A35)
    val accentBlue = Color(0xFF3B82F6)
    val textPrimary = Color.White
    val textSecondary = Color(0xFFB3B3C2)

    var code by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
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
                text = "Enter code",
                color = textPrimary,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "We sent a one-time code to:",
                color = textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = email,
                color = textPrimary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = code,
                onValueChange = {
                    // keep it simple: digits only, max 8
                    val filtered = it.filter { ch -> ch.isDigit() }.take(8)
                    code = filtered
                    error = null
                },
                label = { Text("8-digit code", color = textSecondary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
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
                    if (email.isBlank()) {
                        error = "Missing email. Go back and try again."
                        return@Button
                    }
                    if (code.length !in setOf(6, 8)) {
                        error = "Please enter the code from the email."
                        return@Button
                    }

                    scope.launch {
                        isVerifying = true
                        error = null
                        try {
                            AuthManager.verifyEmailOtp(email = email, code = code)
                            onVerified()
                        } catch (e: Exception) {
                            error = e.message ?: "Invalid code."
                        } finally {
                            isVerifying = false
                        }
                    }
                },
                enabled = !isVerifying,
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
                Text(if (isVerifying) "Verifying…" else "Verify code")
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    if (email.isBlank()) {
                        error = "Missing email. Go back and try again."
                        return@OutlinedButton
                    }
                    scope.launch {
                        isResending = true
                        error = null
                        try {
                            AuthManager.sendEmailOtp(email)
                        } catch (e: Exception) {
                            error = e.message ?: "Failed to resend code."
                        } finally {
                            isResending = false
                        }
                    }
                },
                enabled = !isResending && !isVerifying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
            ) {
                Text(if (isResending) "Resending…" else "Resend code")
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onBack) {
                Text("Back", color = textSecondary)
            }
        }
    }
}