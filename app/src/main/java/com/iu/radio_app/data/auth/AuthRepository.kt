package com.iu.radio_app.data.auth

import com.iu.radio_app.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>

    suspend fun signIn(
        email: String,
        password: String,
    ): Result<User>

    suspend fun signOut()
}

class InvalidCredentialsException : Exception("Invalid credentials")
