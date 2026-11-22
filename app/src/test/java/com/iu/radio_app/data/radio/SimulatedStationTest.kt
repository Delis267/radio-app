package com.iu.radio_app.data.radio

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
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

    private fun fixedClock(now: Instant): Clock =
        object : Clock {
            override fun now(): Instant = now
        }
}
