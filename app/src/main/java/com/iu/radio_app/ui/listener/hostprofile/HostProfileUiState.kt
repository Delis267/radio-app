package com.iu.radio_app.ui.listener.hostprofile

import androidx.annotation.StringRes
import com.iu.radio_app.model.Host

data class HostProfileUiState(
    /** `null`, solange die laufende Sendung noch nicht geladen ist. */
    val host: Host? = null,
    val rating: Int? = null,
    val opinion: String = "",
    /** Einmalige Rückmeldung, der Screen zeigt sie an und meldet `onMessageShown`. */
    @StringRes val message: Int? = null,
)
