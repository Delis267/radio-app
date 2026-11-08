package com.iu.radio_app.ui.login

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.iu.radio_app.MainDispatcherRule
import com.iu.radio_app.R
import com.iu.radio_app.data.auth.FakeAuthRepository
import com.iu.radio_app.model.User
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository(validEmail = "test@radio.app", validPassword = "passwort")
    private lateinit var viewModel: LoginViewModel

    // Erst nach der Rule anlegen, sonst startet viewModelScope ohne Test-Dispatcher
    @Before
    fun setUp() {
        viewModel = LoginViewModel(repository)
    }

    private fun fillIn(
        email: String,
        password: String,
    ) {
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
    }

    @Test
    fun `Absenden ist erst mit E-Mail und Passwort moeglich`() {
        assertThat(viewModel.uiState.value.canSubmit).isFalse()

        fillIn("test@radio.app", "passwort")

        assertThat(viewModel.uiState.value.canSubmit).isTrue()
    }

    @Test
    fun `erfolgreiche Anmeldung meldet die Person an und leert das Passwort`() =
        runTest {
            fillIn("test@radio.app", "passwort")

            viewModel.signIn()

            val state = viewModel.uiState.value
            assertThat(repository.currentUser.value).isEqualTo(User("test@radio.app"))
            assertThat(state.password).isEmpty()
            assertThat(state.isLoading).isFalse()
        }

    @Test
    fun `falsches Passwort zeigt eine Fehlermeldung`() =
        runTest {
            fillIn("test@radio.app", "falsch")

            viewModel.signIn()

            val state = viewModel.uiState.value
            assertThat(state.errorMessage).isEqualTo(R.string.login_error_invalid_credentials)
            assertThat(repository.currentUser.value).isNull()
        }

    @Test
    fun `unbekannter Fehler zeigt eine allgemeine Fehlermeldung`() =
        runTest {
            repository.failNextSignInWith = IllegalStateException("Netzwerk weg")
            fillIn("test@radio.app", "passwort")

            viewModel.signIn()

            assertThat(viewModel.uiState.value.errorMessage).isEqualTo(R.string.login_error_unknown)
        }

    @Test
    fun `waehrend der Anmeldung wird geladen`() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            repository.pendingSignIn = gate
            fillIn("test@radio.app", "passwort")

            viewModel.signIn()
            assertThat(viewModel.uiState.value.isLoading).isTrue()

            gate.complete(Unit)
            assertThat(viewModel.uiState.value.isLoading).isFalse()
        }

    @Test
    fun `Eingabe nach einem Fehler blendet die Meldung aus`() =
        runTest {
            fillIn("test@radio.app", "falsch")
            viewModel.signIn()

            viewModel.onPasswordChange("passwort")

            assertThat(viewModel.uiState.value.errorMessage).isNull()
        }
}
