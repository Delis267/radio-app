package com.iu.radio_app.ui.listener.hostprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iu.radio_app.R
import com.iu.radio_app.data.program.ProgramRepository
import com.iu.radio_app.data.rating.RatingRepository
import com.iu.radio_app.model.Host
import com.iu.radio_app.model.RatingTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class HostProfileViewModel
    @Inject
    constructor(
        programRepository: ProgramRepository,
        private val ratingRepository: RatingRepository,
    ) : ViewModel() {
        private val state = MutableStateFlow(HostProfileUiState())
        val uiState: StateFlow<HostProfileUiState> = state.asStateFlow()

        // Wer gerade moderiert. Angezeigt wird er erst, wenn der Nutzer nicht mehr bewertet (showLatestHost).
        private var latestHost: Host? = null

        // Nur für die Sperre nötig, der Screen braucht den Wert nicht zurück
        private var opinionFocused = false

        // Dankeskarte bleibt nach dem Absenden mindestens CARD_HOLD stehen
        private var holdingCard = false

        init {
            viewModelScope.launch {
                programRepository.currentShow.collect { show ->
                    latestHost = show?.host
                    showLatestHost()
                }
            }
        }

        /** Wählt nur aus, abgeschickt wird mit [submit]. */
        fun onRate(stars: Int) = state.update { it.copy(rating = stars) }

        /** Übernimmt höchstens [MAX_OPINION_LENGTH] Zeichen, das Feld lässt deshalb keine weitere Eingabe zu. */
        fun onOpinionChange(opinion: String) {
            state.update { it.copy(opinion = opinion.take(MAX_OPINION_LENGTH)) }
            showLatestHost()
        }

        fun onOpinionFocusChange(focused: Boolean) {
            opinionFocused = focused
            showLatestHost()
        }

        /** Schickt Sterne und Kommentar einmalig ab. Ohne Sterne oder während des Sendens passiert nichts. */
        fun submit() {
            // TODO: keine Prüfung auf frühere Bewertung (myRating), kommt mit echtem Datenspeicher
            val current = state.value
            if (current.sending) return
            val host = current.host ?: return
            val stars = current.rating ?: return
            val comment = current.opinion.trim().ifEmpty { null }
            state.update { it.copy(sending = true) }
            viewModelScope.launch {
                ratingRepository
                    .rate(RatingTarget.Moderation(host.id), stars, comment)
                    .onSuccess {
                        // Das Textfeld verschwindet mit der Karte und meldet seinen Fokusverlust nicht mehr zuverlässig
                        opinionFocused = false
                        holdingCard = true
                        state.update { it.copy(sending = false, submitted = true) }
                        delay(CARD_HOLD)
                        holdingCard = false
                    }.onFailure { state.update { it.copy(sending = false, message = R.string.host_rating_failed) } }
                showLatestHost()
            }
        }

        fun onMessageShown() = state.update { it.copy(message = null) }

        /**
         * Zeigt [latestHost], solange der Nutzer nicht gerade den angezeigten bewertet.
         * Ein anderer Moderator (andere ID) bekommt ein leeres Formular, damit Eingaben nie beim falschen landen.
         */
        private fun showLatestHost() =
            state.update {
                val busy = if (it.submitted) holdingCard else it.opinion.isNotEmpty() || opinionFocused || it.sending
                when {
                    it.host?.id == latestHost?.id -> it.copy(host = latestHost)
                    busy -> it
                    else -> HostProfileUiState(host = latestHost)
                }
            }

        companion object {
            // TODO: Grenze auch in den Firestore-Regeln prüfen (comment.size() <= 800), die App kann umgangen werden.
            const val MAX_OPINION_LENGTH = 800

            val CARD_HOLD = 30.seconds
        }
    }
