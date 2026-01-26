package fr.eurecom.flowie.data.remote

import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.gotrue.providers.builtin.OTP
import kotlinx.coroutines.flow.StateFlow

object AuthManager {

    // Observe login/logout changes in UI
    val sessionStatus: StateFlow<SessionStatus>
        get() = SupabaseProvider.client.auth.sessionStatus

    fun currentUserIdOrNull(): String? {
        return SupabaseProvider.client.auth.currentUserOrNull()?.id
    }

    /**
     * Sends an email OTP (your Supabase email template decides whether it looks like a link or a code).
     */
    suspend fun sendEmailOtp(email: String, redirectUrl: String? = null) {
        val cleanEmail = email.trim()

        SupabaseProvider.client.auth.signInWith(
            provider = OTP,
            redirectUrl = redirectUrl?.takeIf { it.isNotBlank() }
        ) {
            this.email = cleanEmail
        }
    }

    /**
     * Verifies the OTP code that the user received by email ({{ .Token }} in your template).
     */
    suspend fun verifyEmailOtp(email: String, code: String) {
        SupabaseProvider.client.auth.verifyEmailOtp(
            type = OtpType.Email.EMAIL,
            email = email.trim(),
            token = code.trim()
        )
    }

    suspend fun signOut() {
        SupabaseProvider.client.auth.signOut()
    }
}