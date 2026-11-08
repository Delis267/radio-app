package com.iu.radio_app.data.auth

import com.iu.radio_app.model.User
import kotlinx.coroutines.flow.Flow

/** Naht zwischen UI und Datenquelle (ADR 004, ADR 006). */
interface AuthRepository {
    /** Aktuell angemeldete Person, `null` wenn niemand angemeldet ist. */
    val currentUser: Flow<User?>

    suspend fun signIn(
        email: String,
        password: String,
    ): Result<User>

    suspend fun signOut()
}

class InvalidCredentialsException : Exception("E-Mail oder Passwort ist falsch")
