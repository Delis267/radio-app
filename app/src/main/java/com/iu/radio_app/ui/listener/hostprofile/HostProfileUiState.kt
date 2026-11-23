package com.iu.radio_app.ui.listener.hostprofile

import androidx.annotation.StringRes
import com.iu.radio_app.model.Host

data class HostProfileUiState(
    /** Angezeigter Moderator, `null` solange die laufende Sendung noch nicht geladen ist. */
    val host: Host? = null,
    /** Ausgewählte Sterne, noch nicht abgeschickt. */
    val rating: Int? = null,
    val opinion: String = "",
    /** Bewertung wird gerade gespeichert, der Button ist so lange gesperrt. */
    val sending: Boolean = false,
    /** Bewertung gespeichert: Screen zeigt statt des Formulars die Dankeskarte. */
    val submitted: Boolean = false,
    /** Einmalige Rückmeldung, die Route zeigt sie als Snackbar und meldet `onMessageShown` (ADR 010). */
    @StringRes val message: Int? = null,
)
