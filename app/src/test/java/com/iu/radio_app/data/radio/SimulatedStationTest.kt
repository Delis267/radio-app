package com.iu.radio_app.data.radio

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.iu.radio_app.model.Host
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

class SimulatedStationTest {
    @Test
    fun `spielt den Katalog der Reihe nach im Abstand des Intervalls`() =
        runTest {
            val station = runningStation()
            runCurrent()
            assertThat(station.nowPlaying.value?.track).isEqualTo(SimulatedStation.CATALOG[0])

            advanceTimeBy(SimulatedStation.INTERVAL)
            runCurrent()

            assertThat(station.nowPlaying.value?.track).isEqualTo(SimulatedStation.CATALOG[1])
            assertThat(station.history.value.map { it.track })
                .containsExactly(SimulatedStation.CATALOG[0], SimulatedStation.CATALOG[1])
        }

    @Test
    fun `beginnt nach dem letzten Titel wieder von vorn`() =
        runTest {
            val station = runningStation()
            runCurrent()

            advanceTimeBy(SimulatedStation.INTERVAL * SimulatedStation.CATALOG.size)
            runCurrent()

            assertThat(station.nowPlaying.value?.track).isEqualTo(SimulatedStation.CATALOG[0])
        }

    @Test
    fun `zwei Sender mit derselben Uhrzeit spielen denselben Titel seit demselben Zeitpunkt`() =
        runTest {
            // Wie zwei Geräte, die zu unterschiedlichen Zeiten gestartet wurden
            val now = SimulatedStation.PROGRAM_START + SimulatedStation.INTERVAL * 23 + SimulatedStation.INTERVAL / 2
            val clock = fixedClock(now)
            val first = SimulatedStation(backgroundScope, clock, SimulatedStation.INTERVAL)
            val second = SimulatedStation(backgroundScope, clock, SimulatedStation.INTERVAL)
            runCurrent()

            val expected = SimulatedStation.CATALOG[23 % SimulatedStation.CATALOG.size]
            assertThat(first.nowPlaying.value?.track).isEqualTo(expected)
            assertThat(first.nowPlaying.value).isEqualTo(second.nowPlaying.value)
            assertThat(first.nowPlaying.value?.startedAt)
                .isEqualTo(SimulatedStation.PROGRAM_START + SimulatedStation.INTERVAL * 23)
        }

    @Test
    fun `Playlist enthaelt auch Titel, die vor dem Start der App liefen`() =
        runTest {
            val clock = fixedClock(SimulatedStation.PROGRAM_START + SimulatedStation.INTERVAL * 2)
            val station = SimulatedStation(backgroundScope, clock, SimulatedStation.INTERVAL)
            runCurrent()

            assertThat(station.history.value.map { it.track })
                .containsExactly(SimulatedStation.CATALOG[0], SimulatedStation.CATALOG[1], SimulatedStation.CATALOG[2])
        }

    @Test
    fun `nach der Playlist der ersten Sendung folgt ohne Luecke die zweite`() =
        runTest {
            val station = runningStation()
            runCurrent()
            assertThat(station.currentShow.value.host).isEqualTo(SimulatedStation.ELISE)

            advanceTimeBy(SimulatedStation.INTERVAL * ELISE_TRACKS)
            runCurrent()

            val show = station.currentShow.value
            assertThat(show.host).isEqualTo(SimulatedStation.TOM)
            assertThat(show.startsAt).isEqualTo(slotStart(ELISE_TRACKS))
            assertThat(show.endsAt).isEqualTo(slotStart(SimulatedStation.CATALOG.size))
            assertThat(station.nowPlaying.value?.track).isEqualTo(SimulatedStation.CATALOG[ELISE_TRACKS])
        }

    @Test
    fun `Playlist einer Sendung enthaelt nur deren Titel aus der letzten Ausgabe`() =
        runTest {
            val station = SimulatedStation(backgroundScope, fixedClock(slotStart(ELISE_TRACKS + 2)), SimulatedStation.INTERVAL)
            val elise = station.currentShowIdOf(SimulatedStation.ELISE)
            val tom = station.currentShowIdOf(SimulatedStation.TOM)

            assertThat(station.playlistOf(elise).first().map { it.track })
                .isEqualTo(SimulatedStation.CATALOG.take(ELISE_TRACKS))
            assertThat(station.playlistOf(tom).first().map { it.track })
                .isEqualTo(SimulatedStation.CATALOG.subList(ELISE_TRACKS, ELISE_TRACKS + 3))
        }

    @Test
    fun `Moderation ohne bisherige Ausgabe hat noch keine Sendungen`() =
        runTest {
            val station = SimulatedStation(backgroundScope, fixedClock(slotStart(1)), SimulatedStation.INTERVAL)

            assertThat(station.showsOf(SimulatedStation.TOM.id).first()).isEmpty()
            assertThat(station.showsOf(SimulatedStation.ELISE.id).first().map { it.startsAt })
                .containsExactly(SimulatedStation.PROGRAM_START)
        }

    /** ID der Sendung von [host], abgelesen aus den letzten Ausgaben. */
    private suspend fun SimulatedStation.currentShowIdOf(host: Host): String = showsOf(host.id).first().single().id

    private fun slotStart(slot: Int): Instant = SimulatedStation.PROGRAM_START + SimulatedStation.INTERVAL * slot

    private fun fixedClock(now: Instant): Clock =
        object : Clock {
            override fun now(): Instant = now
        }

    private companion object {
        /** Länge der Playlist von Elise, die als erste Sendung läuft. */
        const val ELISE_TRACKS = 5
    }
}
