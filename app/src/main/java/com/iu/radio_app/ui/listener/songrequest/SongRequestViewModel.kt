package com.iu.radio_app.ui.listener.songrequest

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iu.radio_app.R
import com.iu.radio_app.data.notification.NotificationRepository
import com.iu.radio_app.data.radio.RadioRepository
import com.iu.radio_app.data.songrequest.AlreadyRequestedException
import com.iu.radio_app.data.songrequest.SongRequestRepository
import com.iu.radio_app.model.Track
import com.iu.radio_app.ui.listener.songrequest.SongRequestUiState.Companion.MIN_QUERY_LENGTH
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongRequestViewModel
    @Inject
    constructor(
        private val radioRepository: RadioRepository,
        private val songRequestRepository: SongRequestRepository,
        private val notificationRepository: NotificationRepository,
    ) : ViewModel() {
        private val state = MutableStateFlow(SongRequestUiState())
        val uiState: StateFlow<SongRequestUiState> = state.asStateFlow()

        init {
            viewModelScope.launch {
                songRequestRepository.openRequests.collect { requests -> state.update { it.copy(openRequests = requests) } }
            }
            viewModelScope.launch {
                notificationRepository.notifyWhenRequestPlayed.collect { enabled ->
                    state.update { it.copy(notifyWhenPlayed = enabled) }
                }
            }
            viewModelScope.launch { searchWhileTyping() }
        }

        /** Live-Suche: startet nach einer kurzen Tipp-Pause, eine neue Eingabe bricht die laufende Suche ab. */
        @OptIn(FlowPreview::class)
        private suspend fun searchWhileTyping() {
            state
                .map { it.query.trim() }
                .distinctUntilChanged()
                .debounce(SEARCH_DEBOUNCE_MS)
                .collectLatest { term ->
                    if (term.length < MIN_QUERY_LENGTH) return@collectLatest
                    radioRepository
                        .search(term)
                        .onSuccess { hits ->
                            state.update {
                                when {
                                    // Eingabe hat sich während der Suche geändert, Ergebnis ist veraltet
                                    it.query.trim() != term -> it
                                    // Ohne passende Treffer gleich den Vorschlag anbieten
                                    else -> it.copy(searchResults = hits, suggestionExpanded = it.suggestionExpanded || hits.isEmpty())
                                }
                            }
                        }.onFailure { state.update { it.copy(message = R.string.song_request_search_failed) } }
                }
        }

        /** Unter [MIN_QUERY_LENGTH] Zeichen zurück zur Startansicht, sonst bleiben alte Treffer bis zu den neuen stehen. */
        fun onQueryChange(query: String) =
            state.update {
                if (query.trim().length < MIN_QUERY_LENGTH) {
                    it.copy(query = query, searchResults = null, suggestionExpanded = false)
                } else {
                    it.copy(query = query)
                }
            }

        /** Zurück-Pfeil und X im Suchfeld: Suche beenden. */
        fun onClearSearch() = onQueryChange("")

        /** Tippen auf einen Treffer schickt den Wunsch sofort ab. */
        fun onRequest(track: Track) {
            viewModelScope.launch {
                songRequestRepository
                    .request(track.id)
                    .onSuccess { backToStart(R.string.song_request_sent) }
                    .onFailure { error ->
                        val message =
                            if (error is AlreadyRequestedException) {
                                R.string.song_request_already_requested
                            } else {
                                R.string.song_request_failed
                            }
                        state.update { it.copy(message = message) }
                    }
            }
        }

        fun onToggleSuggestion() = state.update { it.copy(suggestionExpanded = !it.suggestionExpanded) }

        fun onSuggestionChange(suggestion: String) = state.update { it.copy(suggestion = suggestion) }

        /** Ohne Text passiert nichts, der Button ist dann ohnehin deaktiviert. */
        fun submitSuggestion() {
            val suggestion = state.value.suggestion.trim()
            if (suggestion.isEmpty()) return
            viewModelScope.launch {
                songRequestRepository
                    .suggest(suggestion)
                    .onSuccess { backToStart(R.string.song_request_suggestion_sent) }
                    .onFailure { state.update { it.copy(message = R.string.song_request_suggestion_failed) } }
            }
        }

        fun onNotifyChange(enabled: Boolean) {
            viewModelScope.launch { notificationRepository.setNotifyWhenRequestPlayed(enabled) }
        }

        fun onMessageShown() = state.update { it.copy(message = null) }

        private fun backToStart(@StringRes message: Int) =
            state.update {
                it.copy(query = "", searchResults = null, suggestion = "", suggestionExpanded = false, message = message)
            }

        private companion object {
            const val SEARCH_DEBOUNCE_MS = 300L
        }
    }
