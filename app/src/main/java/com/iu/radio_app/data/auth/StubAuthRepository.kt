package com.iu.radio_app.data.auth

import com.iu.radio_app.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

/** Datenquelle ohne Netzwerk für lokale Entwicklung (ADR 003). Später ersetzt durch Firebase Auth. */
@Singleton
class StubAuthRepository
    @Inject
    constructor() : AuthRepository {
        private val user = MutableStateFlow<User?>(null)

        override val currentUser: StateFlow<User?> = user

        override suspend fun signIn(
            email: String,
            password: String,
        ): Result<User> {
            delay(SIMULATED_LATENCY_MS.milliseconds)
            if (email != DEMO_EMAIL || password != DEMO_PASSWORD) {
                return Result.failure(InvalidCredentialsException())
            }
            return Result.success(User(email).also { user.value = it })
        }

        override suspend fun signOut() {
            user.value = null
        }

        companion object {
            const val DEMO_EMAIL = "demo@radio.app"
            const val DEMO_PASSWORD = "demo123"
            private const val SIMULATED_LATENCY_MS = 500L
        }
    }
