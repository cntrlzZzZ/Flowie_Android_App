package fr.eurecom.flowie.data.remote

import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.auth
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
     * Sends an email OTP / magic link depending on your Supabase Auth email template.
     *
     * IMPORTANT:
     * - Supabase Kotlin uses redirectUrl,
     * - If redirectUrl is null, Supabase will use your project SITE_URL / platform defaults.
     */
    suspend fun sendMagicLink(
        email: String,
        redirectUrl: String? = null
    ) {
        val cleanEmail = email.trim()

        SupabaseProvider.client.auth.signInWith(
            provider = OTP,
            redirectUrl = redirectUrl?.takeIf { it.isNotBlank() }
        ) {
            this.email = cleanEmail
            // Optional: if you want to prevent auto-signup:
            // createUser = false
        }
    }

    suspend fun signOut() {
        SupabaseProvider.client.auth.signOut()
    }
}
