package com.iu.radio_app.data.auth

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.iu.radio_app.model.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Vertragstest (ADR 006): Jede Implementierung von [AuthRepository],
 * auch der Fake, muss ihn bestehen.
 */
abstract class AuthRepositoryContract {
    abstract fun createRepository(): AuthRepository

    abstract val validEmail: String
    abstract val validPassword: String

    @Test
    fun `zu Beginn ist niemand angemeldet`() =
        runTest {
            val repository = createRepository()

            assertThat(repository.currentUser.first()).isNull()
        }

    @Test
    fun `gueltige Anmeldung liefert die Person und meldet sie an`() =
        runTest {
            val repository = createRepository()

            val result = repository.signIn(validEmail, validPassword)

            assertThat(result.getOrNull()).isEqualTo(User(validEmail))
            assertThat(repository.currentUser.first()).isEqualTo(User(validEmail))
        }

    @Test
    fun `falsches Passwort schlaegt fehl und meldet niemanden an`() =
        runTest {
            val repository = createRepository()

            val result = repository.signIn(validEmail, "falsch")

            assertThat(result.exceptionOrNull()).isNotNull().isInstanceOf(InvalidCredentialsException::class)
            assertThat(repository.currentUser.first()).isNull()
        }

    @Test
    fun `Abmelden setzt die Person zurueck`() =
        runTest {
            val repository = createRepository()
            repository.signIn(validEmail, validPassword)

            repository.signOut()

            assertThat(repository.currentUser.first()).isNull()
        }
}
