package com.iu.radio_app.data.songrequest

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isSameInstanceAs
import assertk.assertions.isTrue
import com.iu.radio_app.data.radio.StubRadio
import com.iu.radio_app.data.radio.stoppedRadio
import com.iu.radio_app.model.SongRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StubSongRequestRepositoryTest {
    // Sender steht still, nur die Tests unten spielen Titel ab
    private val repository = StubSongRequestRepository(stoppedRadio())

    @Test
    fun `Suche findet Interpret und Titel ohne Gross- und Kleinschreibung`() =
        runTest {
            assertThat(repository.search(" queen ").getOrThrow().map { it.id })
                .containsExactly("bohemian-rhapsody", "dont-stop-me-now")
            assertThat(repository.search("LUFTBALL").getOrThrow().map { it.id }).containsExactly("99-luftballons")
            assertThat(repository.search("gibt es nicht").getOrThrow()).isEmpty()
        }

    @Test
    fun `Wunsch erscheint in offenen Wuenschen, neueste zuerst`() =
        runTest {
            repository.request("billie-jean").getOrThrow()
            repository.request("bad-guy").getOrThrow()

            val open = repository.openRequests.first()
            assertThat(open.map { it.track.id }).containsExactly("bad-guy", "billie-jean")
            assertThat(repository.requestedTracks.first().map { it.requestCount }).containsExactly(1, 1)
        }

    @Test
    fun `erneuter Wunsch fuer offenen Titel wird abgelehnt`() =
        runTest {
            repository.request("billie-jean").getOrThrow()

            val result = repository.request("billie-jean")

            assertThat(result.exceptionOrNull()).isNotNull().isInstanceOf<AlreadyRequestedException>()
            assertThat(repository.openRequests.first().size).isEqualTo(1)
        }

    @Test
    fun `unbekannter Titel und leerer Vorschlag schlagen fehl`() =
        runTest {
            assertThat(repository.request("unbekannt").isFailure).isTrue()
            assertThat(repository.suggest("   ").isFailure).isTrue()
            assertThat(repository.suggest("Nena, Leuchtturm").isSuccess).isTrue()
        }

    @Test
    fun `gespielter Wunsch ist nicht mehr offen und wird gemeldet`() =
        runTest {
            val radio = StubRadio(backgroundScope, StubRadio.INTERVAL)
            val repository = StubSongRequestRepository(radio)
            val played = mutableListOf<SongRequest>()
            backgroundScope.launch { repository.playedRequests.collect { played += it } }
            runCurrent() // Erster Titel im Karussell läuft
            repository.request("dont-stop-me-now").getOrThrow()
            repository.request("bad-guy").getOrThrow()

            advanceTimeBy(StubRadio.INTERVAL)
            runCurrent() // Zweiter Titel läuft: Don't Stop Me Now

            assertThat(repository.openRequests.first().map { it.track.id }).containsExactly("bad-guy")
            assertThat(repository.requestedTracks.first().map { it.track.id }).containsExactly("bad-guy")
            assertThat(played.map { it.track.id to it.status }).containsExactly("dont-stop-me-now" to SongRequest.Status.PLAYED)
        }

    @Test
    fun `nach dem Spielen kann man sich den Titel erneut wuenschen`() =
        runTest {
            val radio = StubRadio(backgroundScope, StubRadio.INTERVAL)
            val repository = StubSongRequestRepository(radio)
            runCurrent()
            repository.request("dont-stop-me-now").getOrThrow()
            advanceTimeBy(StubRadio.INTERVAL)
            runCurrent()

            assertThat(repository.request("dont-stop-me-now").isSuccess).isTrue()
        }
}
