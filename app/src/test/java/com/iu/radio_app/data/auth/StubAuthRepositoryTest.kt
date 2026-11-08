package com.iu.radio_app.data.auth

class StubAuthRepositoryTest : AuthRepositoryContract() {
    override fun createRepository(): AuthRepository = StubAuthRepository()

    override val validEmail = StubAuthRepository.DEMO_EMAIL
    override val validPassword = StubAuthRepository.DEMO_PASSWORD
}
