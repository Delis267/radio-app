package com.iu.radio_app.ui.listener.hostprofile

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iu.radio_app.R
import com.iu.radio_app.data.program.ProgramRepository
import com.iu.radio_app.data.rating.RatingRepository
import com.iu.radio_app.model.Host
import com.iu.radio_app.model.RatingTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HostProfileViewModel
    @Inject
    constructor(
        programRepository: ProgramRepository,
        private val ratingRepository: RatingRepository,
    ) : ViewModel() {
        private val state = MutableStateFlow(HostProfileUiState())
        val uiState: StateFlow<HostProfileUiState> = state.asStateFlow()

        // Zuletzt abgeschickter Kommentar, damit ein späterer Sternklick ihn nicht überschreibt
        private var savedComment: String? = null

        init {
            viewModelScope.launch {
                programRepository.currentShow.collect { show -> onHostChange(show?.host) }
            }
        }

        /** Sternklick speichert sofort, damit man schnell bewerten kann. */
        fun onRate(stars: Int) {
            state.update { it.copy(rating = stars) }
            save(stars, savedComment, R.string.host_rating_saved)
        }

        fun onOpinionChange(opinion: String) = state.update { it.copy(opinion = opinion) }

        /** Aktualisiert die bestehende Bewertung um den Kommentar. Ohne Sterne passiert nichts. */
        fun submit() {
            val current = state.value
            val stars = current.rating ?: return
            savedComment = current.opinion.trim().ifEmpty { null }
            save(stars, savedComment, R.string.host_feedback_saved)
        }

        fun onMessageShown() = state.update { it.copy(message = null) }

        /**
         * Die Ansicht wechselt mit der laufenden Sendung zum nächsten Host.
         * Sterne und Meinung gehören zum vorigen Host und werden verworfen, damit sie nicht beim neuen landen.
         */
        private fun onHostChange(host: Host?) {
            if (host?.id == state.value.host?.id) {
                state.update { it.copy(host = host) }
                return
            }
            savedComment = null
            state.update { it.copy(host = host, rating = null, opinion = "") }
        }

        private fun save(
            stars: Int,
            comment: String?,
            @StringRes successMessage: Int,
        ) {
            val host = state.value.host ?: return
            viewModelScope.launch {
                ratingRepository
                    .rate(RatingTarget.Moderation(host.id), stars, comment)
                    .onSuccess { state.update { it.copy(message = successMessage) } }
                    .onFailure { state.update { it.copy(message = R.string.host_rating_failed) } }
            }
        }
    }
