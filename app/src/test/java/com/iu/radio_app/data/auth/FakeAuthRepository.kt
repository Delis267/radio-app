package com.iu.radio_app.data.auth

import com.iu.radio_app.model.User
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Test-Double für die UI-Seite (ADR 006). Verhält sich wie der Vertrag verlangt
 * und lässt sich zusätzlich steuern, z. B. um den Ladezustand zu prüfen.
 */
class FakeAuthRepository(
    private val validEmail: String = "test@radio.app",
    private val validPassword: String = "passwort",
) : AuthRepository {
    private val user = MutableStateFlow<User?>(null)

    /** Wenn gesetzt, wartet [signIn], bis der Test es freigibt. */
    var pendingSignIn: CompletableDeferred<Unit>? = null

    /** Wenn gesetzt, scheitert `signIn` mit genau diesem Fehler. */
    var failNextSignInWith: Throwable? = null

    override val currentUser: StateFlow<User?> = user

    override suspend fun signIn(
        email: String,
        password: String,
    ): Result<User> {
        pendingSignIn?.await()
        failNextSignInWith?.let { return Result.failure(it) }
        if (email != validEmail || password != validPassword) {
            return Result.failure(InvalidCredentialsException())
        }
        return Result.success(User(email).also { user.value = it })
    }

    override suspend fun signOut() {
        user.value = null
    }
}
