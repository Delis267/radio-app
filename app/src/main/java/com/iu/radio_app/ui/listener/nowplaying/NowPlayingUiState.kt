package com.iu.radio_app.ui.listener.nowplaying

import androidx.annotation.StringRes
import com.iu.radio_app.model.PlayedTrack

data class NowPlayingUiState(
    /** Der gerade laufende Titel, `null` solange noch kein Titel geladen ist. */
    val playedTrack: PlayedTrack? = null,
    /** Vom Nutzer vergebene Sterne für den aktuellen Titel (1..5), `null` wenn noch nicht bewertet. */
    val rating: Int? = null,
    /** Einmalige Rückmeldung, der Screen zeigt sie an und meldet `onMessageShown`. */
    @StringRes val message: Int? = null,
)
