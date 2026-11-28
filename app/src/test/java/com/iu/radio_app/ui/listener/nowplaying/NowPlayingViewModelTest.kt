@file:OptIn(ExperimentalCoroutinesApi::class)

package com.iu.radio_app.ui.listener.nowplaying

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.iu.radio_app.MainDispatcherRule
import com.iu.radio_app.R
import com.iu.radio_app.data.program.SimulatedProgramRepository
import com.iu.radio_app.data.radio.SimulatedStation
import com.iu.radio_app.data.radio.runningStation
import com.iu.radio_app.data.rating.StubRatingRepository
import com.iu.radio_app.model.RatingTarget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class NowPlayingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val ratings = StubRatingRepository()

    private fun TestScope.createViewModel(): NowPlayingViewModel {
        // Sender auf der Testuhr, beim Start läuft der erste Titel des Katalogs
        val program = SimulatedProgramRepository(runningStation())
        val viewModel = NowPlayingViewModel(program, ratings)
        runCurrent()
        return viewModel
    }

    @Test
    fun `laufender Titel wird aus Repository geladen`() =
        runTest {
            val viewModel = createViewModel()
            assertThat(viewModel.uiState.value.playedTrack?.track).isEqualTo(SimulatedStation.CATALOG[0])
        }

    @Test
    fun `Sternauswahl passt Zustand an`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onRate(4)
            assertThat(viewModel.uiState.value.rating).isEqualTo(4)
        }

    @Test
    fun `Bewertung speichern ruft RatingRepository auf und setzt Erfolgsmeldung`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onRate(5)
            viewModel.submitRating()
            runCurrent()

            val target = RatingTarget.Song(SimulatedStation.CATALOG[0].id)
            assertThat(ratings.myRating(target).first()?.stars).isEqualTo(5)
            assertThat(viewModel.uiState.value.message).isEqualTo(R.string.now_playing_rating_saved)
        }

    @Test
    fun `angezeigte Meldung wird geloescht`() =
        runTest {
            val viewModel = createViewModel()
            viewModel.onRate(3)
            viewModel.submitRating()
            runCurrent()

            viewModel.onMessageShown()

            assertThat(viewModel.uiState.value.message).isNull()
        }
}
