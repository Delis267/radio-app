package com.iu.radio_app.ui.login

import androidx.annotation.StringRes

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    /** Ressourcen-ID statt Text, damit das ViewModel ohne `Context` auskommt. */
    @StringRes val errorMessage: Int? = null,
    val signedInAs: String? = null,
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}
