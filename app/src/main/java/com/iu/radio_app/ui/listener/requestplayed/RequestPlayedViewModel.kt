package com.iu.radio_app.ui.listener.requestplayed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iu.radio_app.data.songrequest.SongRequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/** Zeigt app-weit einen Banner, sobald ein eigener Wunsch gespielt wird. Gilt unabhängig vom Benachrichtigungs-Schalter. */
@HiltViewModel
class RequestPlayedViewModel
    @Inject
    constructor(
        songRequestRepository: SongRequestRepository,
    ) : ViewModel() {
        private val state = MutableStateFlow(RequestPlayedUiState())
        val uiState: StateFlow<RequestPlayedUiState> = state.asStateFlow()

        private var hideJob: Job? = null

        init {
            viewModelScope.launch {
                // Ein neuer Wunsch ersetzt den angezeigten und startet die Anzeigedauer neu
                songRequestRepository.playedRequests.collect { request ->
                    state.value = RequestPlayedUiState(request.track)
                    hideJob?.cancel()
                    hideJob =
                        viewModelScope.launch {
                            delay(VISIBLE_DURATION)
                            state.value = RequestPlayedUiState()
                        }
                }
            }
        }

        fun onDismiss() {
            hideJob?.cancel()
            state.value = RequestPlayedUiState()
        }

        companion object {
            val VISIBLE_DURATION = 5.seconds
        }
    }
