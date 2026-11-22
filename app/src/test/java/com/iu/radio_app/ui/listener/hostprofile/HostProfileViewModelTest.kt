package com.iu.radio_app.ui.listener.hostprofile

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.iu.radio_app.MainDispatcherRule
import com.iu.radio_app.R
import com.iu.radio_app.data.program.SimulatedProgramRepository
import com.iu.radio_app.data.radio.SimulatedStation
import com.iu.radio_app.data.radio.runningStation
import com.iu.radio_app.data.radio.stoppedStation
import com.iu.radio_app.data.rating.StubRatingRepository
import com.iu.radio_app.model.RatingTarget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HostProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val ratings = StubRatingRepository()
    private val target = RatingTarget.Moderation(SimulatedStation.ELISE.id)
    private lateinit var viewModel: HostProfileViewModel

    // Erst nach der Rule anlegen, sonst startet viewModelScope ohne Test-Dispatcher
    @Before
    fun setUp() {
        viewModel = HostProfileViewModel(SimulatedProgramRepository(stoppedStation()), ratings)
    }

    @Test
    fun `wechselt mit der Sendung zum naechsten Host und verwirft dessen Eingaben`() =
        runTest {
            val viewModel = HostProfileViewModel(SimulatedProgramRepository(runningStation()), ratings)
            runCurrent()
            viewModel.onRate(4)
            viewModel.onOpinionChange("Super Stimme")
            viewModel.submit()

            // Elises Playlist hat fünf Titel, danach läuft Toms Sendung
            advanceTimeBy(SimulatedStation.INTERVAL * 5)
            runCurrent()

            val state = viewModel.uiState.value
            assertThat(state.host).isEqualTo(SimulatedStation.TOM)
            assertThat(state.rating).isNull()
            assertThat(state.opinion).isEqualTo("")

            viewModel.onRate(2)
            val tomsRating = ratings.myRating(RatingTarget.Moderation(SimulatedStation.TOM.id)).first()
            assertThat(tomsRating?.stars).isEqualTo(2)
            assertThat(tomsRating?.comment).isNull()
            assertThat(ratings.myRating(target).first()?.stars).isEqualTo(4)
        }

    @Test
    fun `Sternklick speichert sofort und meldet es`() =
        runTest {
            viewModel.onRate(4)

            assertThat(ratings.myRating(target).first()?.stars).isEqualTo(4)
            assertThat(viewModel.uiState.value.message).isEqualTo(R.string.host_rating_saved)
        }

    @Test
    fun `Absenden ergaenzt den Kommentar, spaeterer Sternklick behaelt ihn`() =
        runTest {
            viewModel.onRate(4)
            viewModel.onOpinionChange("  Super Stimme  ")
            viewModel.submit()
            assertThat(viewModel.uiState.value.message).isEqualTo(R.string.host_feedback_saved)

            viewModel.onRate(5)

            val rating = ratings.myRating(target).first()
            assertThat(rating?.stars).isEqualTo(5)
            assertThat(rating?.comment).isEqualTo("Super Stimme")
        }

    @Test
    fun `Absenden ohne Sterne speichert nichts`() =
        runTest {
            viewModel.onOpinionChange("Super")

            viewModel.submit()

            assertThat(ratings.myRating(target).first()).isNull()
            assertThat(viewModel.uiState.value.message).isNull()
        }

    @Test
    fun `angezeigte Meldung wird geloescht`() {
        viewModel.onRate(3)

        viewModel.onMessageShown()

        assertThat(viewModel.uiState.value.message).isNull()
    }
}
