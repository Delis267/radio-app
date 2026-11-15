package com.iu.radio_app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iu.radio_app.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Entscheidet, ob der Login oder die App gezeigt wird. */
@HiltViewModel
class SessionViewModel
    @Inject
    constructor(
        authRepository: AuthRepository,
    ) : ViewModel() {
        /** `null`, solange noch unbekannt ist, ob jemand angemeldet ist (z. B. beim Start mit Firebase). */
        val isSignedIn: StateFlow<Boolean?> =
            authRepository.currentUser
                .map { it != null }
                .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)
    }
