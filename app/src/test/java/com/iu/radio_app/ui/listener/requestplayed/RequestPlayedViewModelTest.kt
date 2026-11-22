package com.iu.radio_app.ui.listener.requestplayed

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.iu.radio_app.MainDispatcherRule
import com.iu.radio_app.data.radio.SimulatedStation
import com.iu.radio_app.data.radio.runningStation
import com.iu.radio_app.data.songrequest.StubSongRequestRepository
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RequestPlayedViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val wished = SimulatedStation.CATALOG[1]

    /** Erster Titel läuft, [wished] ist gewünscht und kommt nach einem Intervall. */
    private suspend fun TestScope.wishSecondTrack(): RequestPlayedViewModel {
        val songRequests = StubSongRequestRepository(runningStation())
        val viewModel = RequestPlayedViewModel(songRequests)
        runCurrent()
        songRequests.request(wished.id).getOrThrow()
        return viewModel
    }

    @Test
    fun `Banner erscheint, wenn der Wunsch laeuft, und verschwindet nach der Anzeigedauer`() =
        runTest {
            val viewModel = wishSecondTrack()
            assertThat(viewModel.uiState.value.track).isNull()

            advanceTimeBy(SimulatedStation.INTERVAL)
            runCurrent()
            assertThat(viewModel.uiState.value.track).isEqualTo(wished)

            advanceTimeBy(RequestPlayedViewModel.VISIBLE_DURATION)
            runCurrent()
            assertThat(viewModel.uiState.value.track).isNull()
        }

    @Test
    fun `Banner laesst sich wegtippen`() =
        runTest {
            val viewModel = wishSecondTrack()
            advanceTimeBy(SimulatedStation.INTERVAL)
            runCurrent()

            viewModel.onDismiss()

            assertThat(viewModel.uiState.value.track).isNull()
        }
}
