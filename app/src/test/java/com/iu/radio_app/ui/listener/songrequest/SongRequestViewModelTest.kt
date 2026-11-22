package com.iu.radio_app.ui.listener.songrequest

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.iu.radio_app.MainDispatcherRule
import com.iu.radio_app.R
import com.iu.radio_app.data.notification.StubNotificationRepository
import com.iu.radio_app.data.radio.stoppedRadio
import com.iu.radio_app.data.songrequest.StubSongRequestRepository
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SongRequestViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val songRequests = StubSongRequestRepository(stoppedRadio())
    private val notifications = StubNotificationRepository(emptyFlow(), stoppedRadio().scope, isAppInForeground = { false }) {}
    private lateinit var viewModel: SongRequestViewModel

    // Erst nach der Rule anlegen, sonst startet viewModelScope ohne Test-Dispatcher
    @Before
    fun setUp() {
        viewModel = SongRequestViewModel(songRequests, notifications)
    }

    private val state get() = viewModel.uiState.value

    @Test
    fun `Suche startet erst ab zwei Zeichen`() =
        runTest {
            viewModel.onQueryChange("z")
            advanceUntilIdle()
            assertThat(state.isSearching).isFalse()
            assertThat(state.searchResults).isNull()

            viewModel.onQueryChange("zz")
            advanceUntilIdle()
            assertThat(state.isSearching).isTrue()
            assertThat(state.searchResults).isNotNull().isEmpty()
        }

    @Test
    fun `Treffer erscheinen nach der Tipp-Pause`() =
        runTest {
            viewModel.onQueryChange("Queen")
            advanceUntilIdle()

            assertThat(state.searchResults?.map { it.id }).isNotNull().containsExactly("bohemian-rhapsody", "dont-stop-me-now")
            assertThat(state.suggestionExpanded).isFalse()
        }

    @Test
    fun `ohne Treffer wird der Vorschlag aufgeklappt`() =
        runTest {
            viewModel.onQueryChange("gibt es nicht")
            advanceUntilIdle()

            assertThat(state.searchResults).isNotNull().isEmpty()
            assertThat(state.suggestionExpanded).isTrue()
        }

    @Test
    fun `Suche beenden fuehrt zur Startansicht`() =
        runTest {
            viewModel.onQueryChange("gibt es nicht")
            advanceUntilIdle()

            viewModel.onClearSearch()

            assertThat(state.query).isEqualTo("")
            assertThat(state.searchResults).isNull()
            assertThat(state.suggestionExpanded).isFalse()
        }

    @Test
    fun `Wunsch wird gesendet, Startansicht zeigt ihn als offen`() =
        runTest {
            viewModel.onQueryChange("Nena")
            advanceUntilIdle()
            val track = state.searchResults!!.single()

            viewModel.onRequest(track)
            advanceUntilIdle()

            assertThat(state.message).isEqualTo(R.string.song_request_sent)
            assertThat(state.isSearching).isFalse()
            assertThat(state.openRequests.map { it.track }).containsExactly(track)
        }

    @Test
    fun `doppelter Wunsch meldet Fehler und bleibt in der Suche`() =
        runTest {
            viewModel.onQueryChange("Nena")
            advanceUntilIdle()
            val track = state.searchResults!!.single()
            viewModel.onRequest(track)
            advanceUntilIdle()
            viewModel.onMessageShown()
            viewModel.onQueryChange("Nena")
            advanceUntilIdle()

            viewModel.onRequest(track)
            advanceUntilIdle()

            assertThat(state.message).isEqualTo(R.string.song_request_already_requested)
            assertThat(state.isSearching).isTrue()
            assertThat(songRequests.openRequests.first().size).isEqualTo(1)
        }

    @Test
    fun `Vorschlag wird eingereicht und fuehrt zur Startansicht`() =
        runTest {
            viewModel.onQueryChange("Leuchtturm")
            advanceUntilIdle()
            viewModel.onSuggestionChange("  Nena, Leuchtturm  ")

            viewModel.submitSuggestion()
            advanceUntilIdle()

            assertThat(state).prop(SongRequestUiState::message).isEqualTo(R.string.song_request_suggestion_sent)
            assertThat(state.suggestion).isEqualTo("")
            assertThat(state.isSearching).isFalse()
        }

    @Test
    fun `leerer Vorschlag wird nicht gesendet`() =
        runTest {
            viewModel.onSuggestionChange("   ")

            viewModel.submitSuggestion()
            advanceUntilIdle()

            assertThat(state.message).isNull()
        }

    @Test
    fun `Schalter speichert die Benachrichtigung`() =
        runTest {
            viewModel.onNotifyChange(true)
            advanceUntilIdle()

            assertThat(notifications.notifyWhenRequestPlayed.first()).isTrue()
            assertThat(state.notifyWhenPlayed).isTrue()
        }
}
