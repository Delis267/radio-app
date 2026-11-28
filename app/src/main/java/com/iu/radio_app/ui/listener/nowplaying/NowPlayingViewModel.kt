package com.iu.radio_app.ui.listener.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iu.radio_app.R
import com.iu.radio_app.data.radio.RadioRepository
import com.iu.radio_app.data.rating.RatingRepository
import com.iu.radio_app.model.RatingTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel
    @Inject
    constructor(
        radioRepository: RadioRepository,
        private val ratingRepository: RatingRepository,
    ) : ViewModel() {
        private val state = MutableStateFlow(NowPlayingUiState())
        val uiState: StateFlow<NowPlayingUiState> = state.asStateFlow()

        init {
            viewModelScope.launch {
                radioRepository.nowPlaying.collectLatest { played ->
                    val track = played?.track
                    if (track == null) {
                        state.update { it.copy(playedTrack = null, rating = null) }
                    } else {
                        ratingRepository.myRating(RatingTarget.Song(track.id)).collect { myRating ->
                            state.update { it.copy(playedTrack = played, rating = myRating?.stars) }
                        }
                    }
                }
            }
        }

        /** Sternauswahl anpassen (wird erst beim Klick auf „Song bewerten“ gespeichert). */
        fun onRate(stars: Int) {
            state.update { it.copy(rating = stars) }
        }

        /** Speichert die Bewertung des aktuellen Titels. */
        fun submitRating() {
            val currentTrack = state.value.playedTrack?.track ?: return
            val stars = state.value.rating ?: return
            viewModelScope.launch {
                ratingRepository
                    .rate(RatingTarget.Song(currentTrack.id), stars)
                    .onSuccess { state.update { it.copy(message = R.string.now_playing_rating_saved) } }
                    .onFailure { state.update { it.copy(message = R.string.now_playing_rating_failed) } }
            }
        }

        fun onMessageShown() {
            state.update { it.copy(message = null) }
        }
    }
