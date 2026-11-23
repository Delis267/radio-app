package com.iu.radio_app.ui.listener.hostprofile

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.iu.radio_app.MainDispatcherRule
import com.iu.radio_app.R
import com.iu.radio_app.data.program.ProgramRepository
import com.iu.radio_app.data.program.StubProgramRepository
import com.iu.radio_app.data.program.StubProgramRepository.Companion.HOST
import com.iu.radio_app.data.program.StubProgramRepository.Companion.SHOW
import com.iu.radio_app.data.rating.RatingRepository
import com.iu.radio_app.data.rating.StubRatingRepository
import com.iu.radio_app.model.RatingTarget
import com.iu.radio_app.model.Show
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class HostProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val program = FakeProgramRepository()
    private val ratings = FakeRatingRepository()
    private val target = RatingTarget.Moderation(HOST.id)
    private lateinit var viewModel: HostProfileViewModel

    // Erst nach der Rule anlegen, sonst startet viewModelScope ohne Test-Dispatcher
    @Before
    fun setUp() {
        viewModel = HostProfileViewModel(program, ratings)
    }

    private val state get() = viewModel.uiState.value

    @Test
    fun `Sternklick waehlt nur aus und speichert nichts`() =
        runTest {
            viewModel.onRate(4)

            assertThat(state.rating).isEqualTo(4)
            assertThat(ratings.myRating(target).first()).isNull()
            assertThat(state.submitted).isFalse()
        }

    @Test
    fun `Absenden speichert Sterne und Kommentar und zeigt die Dankeskarte`() =
        runTest {
            viewModel.onRate(4)
            viewModel.onOpinionChange("  Super Stimme  ")

            viewModel.submit()

            val rating = ratings.myRating(target).first()
            assertThat(rating?.stars).isEqualTo(4)
            assertThat(rating?.comment).isEqualTo("Super Stimme")
            assertThat(state.submitted).isTrue()
        }

    @Test
    fun `Kommentar wird auf die Hoechstlaenge gekuerzt`() {
        viewModel.onOpinionChange("a".repeat(HostProfileViewModel.MAX_OPINION_LENGTH + 50))

        assertThat(state.opinion.length).isEqualTo(HostProfileViewModel.MAX_OPINION_LENGTH)
    }

    @Test
    fun `Absenden ohne Sterne speichert nichts`() =
        runTest {
            viewModel.onOpinionChange("Super")

            viewModel.submit()

            assertThat(ratings.myRating(target).first()).isNull()
            assertThat(state.submitted).isFalse()
        }

    @Test
    fun `waehrend des Sendens ist der Button gesperrt und zweites Absenden speichert nicht erneut`() {
        ratings.pending = CompletableDeferred()
        viewModel.onRate(4)

        viewModel.submit()
        viewModel.submit()

        assertThat(state.sending).isTrue()
        ratings.pending?.complete(Unit)
        assertThat(ratings.calls).isEqualTo(1)
    }

    @Test
    fun `Fehler beim Speichern meldet ihn und gibt den Button wieder frei`() {
        ratings.fail = true
        viewModel.onRate(4)

        viewModel.submit()

        assertThat(state.message).isEqualTo(R.string.host_rating_failed)
        assertThat(state.sending).isFalse()
        assertThat(state.submitted).isFalse()
        viewModel.onMessageShown()
        assertThat(state.message).isNull()
    }

    @Test
    fun `neuer Moderator ersetzt das Formular und setzt Sterne zurueck`() {
        viewModel.onRate(4)

        program.currentShow.value = OTHER_SHOW

        assertThat(state.host?.id).isEqualTo(OTHER_SHOW.host.id)
        assertThat(state.rating).isNull()
    }

    @Test
    fun `gleicher Moderator behaelt die Eingaben`() {
        viewModel.onRate(4)

        program.currentShow.value = SHOW.copy(title = "Neuer Titel")

        assertThat(state.rating).isEqualTo(4)
    }

    @Test
    fun `Text im Feld sperrt den Wechsel, bis es leer ist`() {
        viewModel.onOpinionChange("Super")

        program.currentShow.value = OTHER_SHOW
        assertThat(state.host?.id).isEqualTo(HOST.id)

        viewModel.onOpinionChange("")
        assertThat(state.host?.id).isEqualTo(OTHER_SHOW.host.id)
    }

    @Test
    fun `Fokus im Feld sperrt den Wechsel, bis er weg ist`() {
        viewModel.onOpinionFocusChange(true)

        program.currentShow.value = OTHER_SHOW
        assertThat(state.host?.id).isEqualTo(HOST.id)

        viewModel.onOpinionFocusChange(false)
        assertThat(state.host?.id).isEqualTo(OTHER_SHOW.host.id)
    }

    // advanceTimeBy gilt in kotlinx-coroutines-test noch als experimentell
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Dankeskarte bleibt mindestens 30 Sekunden, danach wechselt der Moderator`() =
        runTest {
            viewModel.onRate(4)
            viewModel.onOpinionFocusChange(true)
            viewModel.submit()
            program.currentShow.value = OTHER_SHOW

            advanceTimeBy(HostProfileViewModel.CARD_HOLD - 1.seconds)
            assertThat(state.host?.id).isEqualTo(HOST.id)
            assertThat(state.submitted).isTrue()

            advanceTimeBy(2.seconds)
            assertThat(state.host?.id).isEqualTo(OTHER_SHOW.host.id)
            assertThat(state.submitted).isFalse()
        }

    private class FakeProgramRepository : ProgramRepository by StubProgramRepository() {
        override val currentShow = MutableStateFlow<Show?>(SHOW)
    }

    /** Speichert wie der Stub, lässt sich aber anhalten ([pending]) oder scheitern ([fail]). */
    private class FakeRatingRepository(
        private val stub: StubRatingRepository = StubRatingRepository(),
    ) : RatingRepository by stub {
        var pending: CompletableDeferred<Unit>? = null
        var fail = false
        var calls = 0

        override suspend fun rate(
            target: RatingTarget,
            stars: Int,
            comment: String?,
        ): Result<Unit> {
            calls++
            pending?.await()
            return if (fail) Result.failure(IllegalStateException("offline")) else stub.rate(target, stars, comment)
        }
    }

    private companion object {
        val OTHER_SHOW = SHOW.copy(id = "abend", host = HOST.copy(id = "max", name = "Max Weber"))
    }
}
