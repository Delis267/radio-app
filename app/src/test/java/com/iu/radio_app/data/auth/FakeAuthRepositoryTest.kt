package com.iu.radio_app.data.auth

/** Stellt sicher, dass sich der Fake wie die echte Implementierung verhält. */
class FakeAuthRepositoryTest : AuthRepositoryContract() {
    override val validEmail = "test@radio.app"
    override val validPassword = "passwort"

    override fun createRepository(): AuthRepository = FakeAuthRepository(validEmail, validPassword)
}
