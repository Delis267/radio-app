
package com.iu.radio_app.ui.listener.show

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.iu.radio_app.MainDispatcherRule
import com.iu.radio_app.R
import com.iu.radio_app.data.program.SimulatedProgramRepository
import com.iu.radio_app.data.radio.stoppedStation
import com.iu.radio_app.data.rating.StubRatingRepository
import com.iu.radio_app.model.RatingTarget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ShowViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Gestoppter Sender: Beim Programmstart läuft Elises Sendung, und dabei bleibt es
    private val station = stoppedStation()
    private val program = SimulatedProgramRepository(station)
    private val ratings = StubRatingRepository()
    private val target = RatingTarget.Show(station.currentShow.value.id)

    private lateinit var viewModel: ShowViewModel

    @Before
    fun setUp() {
        viewModel = ShowViewModel(program, ratings)
    }

    @Test
    fun `aktuelle Sendung wird geladen`() {
        assertThat(viewModel.uiState.value.show)
            .isEqualTo(station.currentShow.value)
    }

    @Test
    fun `Sternauswahl speichert noch keine Bewertung`() = runTest {
        viewModel.onRate(4)

        assertThat(viewModel.uiState.value.rating).isEqualTo(4)
        assertThat(ratings.myRating(target).first()).isNull()
    }

    @Test
    fun `Bewertung wird beim Absenden gespeichert`() = runTest {
        viewModel.onRate(5)
        viewModel.submitRating()

        assertThat(ratings.myRating(target).first()?.stars).isEqualTo(5)
        assertThat(viewModel.uiState.value.message)
            .isEqualTo(R.string.show_rating_saved)
    }

    @Test
    fun `erneute Bewertung ersetzt die vorherige`() = runTest {
        viewModel.onRate(3)
        viewModel.submitRating()

        viewModel.onRate(5)
        viewModel.submitRating()

        assertThat(ratings.myRating(target).first()?.stars).isEqualTo(5)
        assertThat(ratings.summary(target).first().count).isEqualTo(1)
    }

    @Test
    fun `Erfolgsmeldung kann zurueckgesetzt werden`() {
        viewModel.onRate(4)
        viewModel.submitRating()
        viewModel.onMessageShown()

        assertThat(viewModel.uiState.value.message).isNull()
    }
}
