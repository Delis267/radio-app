
package com.iu.radio_app.ui.listener.show

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iu.radio_app.R
import com.iu.radio_app.data.program.ProgramRepository
import com.iu.radio_app.data.rating.RatingRepository
import com.iu.radio_app.model.RatingTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShowViewModel @Inject constructor(
    programRepository: ProgramRepository,
    private val ratingRepository: RatingRepository,
) : ViewModel() {

    private val state = MutableStateFlow(ShowUiState())
    val uiState: StateFlow<ShowUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            programRepository.currentShow.collectLatest { show ->
                state.update {
                    it.copy(
                        show = show,
                        playlist = emptyList(),
                        rating = null,
                    )
                }

                if (show != null) {
                    coroutineScope {
                        launch {
                            programRepository.playlist(show.id).collect { playlist ->
                                state.update { it.copy(playlist = playlist) }
                            }
                        }

                        launch {
                            ratingRepository
                                .myRating(RatingTarget.Show(show.id))
                                .collect { savedRating ->
                                    state.update {
                                        it.copy(rating = savedRating?.stars)
                                    }
                                }
                        }
                    }
                }
            }
        }
    }

    fun onRate(stars: Int) {
        if (stars in 1..5 && !state.value.sending) {
            state.update { it.copy(rating = stars) }
        }
    }

    fun submitRating() {
        val current = state.value
        if (current.sending) return

        val show = current.show ?: return
        val stars = current.rating ?: return

        state.update { it.copy(sending = true) }

        viewModelScope.launch {
            ratingRepository
                .rate(RatingTarget.Show(show.id), stars)
                .onSuccess {
                    state.update {
                        it.copy(message = R.string.show_rating_saved)
                    }
                }
                .onFailure {
                    state.update {
                        it.copy(message = R.string.show_rating_failed)
                    }
                }

            state.update { it.copy(sending = false) }
        }
    }

    fun onMessageShown() {
        state.update { it.copy(message = null) }
    }
}
