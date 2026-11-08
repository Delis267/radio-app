package com.iu.radio_app.ui.navigation

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.iu.radio_app.MainDispatcherRule
import com.iu.radio_app.data.auth.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SessionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAuthRepository(validEmail = "test@radio.app", validPassword = "passwort")
    private lateinit var viewModel: SessionViewModel

    @Before
    fun setUp() {
        viewModel = SessionViewModel(repository)
    }

    @Test
    fun `ohne Anmeldung wird der Login gezeigt`() {
        assertThat(viewModel.isSignedIn.value).isEqualTo(false)
    }

    @Test
    fun `nach der Anmeldung wird die App gezeigt`() =
        runTest {
            repository.signIn("test@radio.app", "passwort")

            assertThat(viewModel.isSignedIn.value).isEqualTo(true)
        }

    @Test
    fun `nach dem Abmelden wird wieder der Login gezeigt`() =
        runTest {
            repository.signIn("test@radio.app", "passwort")

            repository.signOut()

            assertThat(viewModel.isSignedIn.value).isEqualTo(false)
        }
}
