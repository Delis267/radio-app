package com.iu.radio_app.ui.listener.songrequest

import androidx.annotation.StringRes
import com.iu.radio_app.model.SongRequest
import com.iu.radio_app.model.Track

data class SongRequestUiState(
    val query: String = "",
    /** Treffer zur aktuellen Suche, `null` ohne Suche oder solange die Suche läuft. */
    val searchResults: List<Track>? = null,
    val openRequests: List<SongRequest> = emptyList(),
    val suggestion: String = "",
    val suggestionExpanded: Boolean = false,
    val notifyWhenPlayed: Boolean = false,
    /** Einmalige Rückmeldung, der Screen zeigt sie an und meldet `onMessageShown`. */
    @StringRes val message: Int? = null,
) {
    /** Suchansicht statt Startansicht, sobald genug Zeichen eingegeben sind. */
    val isSearching: Boolean get() = query.trim().length >= MIN_QUERY_LENGTH

    companion object {
        const val MIN_QUERY_LENGTH = 2
    }
}
