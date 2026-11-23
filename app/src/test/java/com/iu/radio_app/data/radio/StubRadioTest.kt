package com.iu.radio_app.data.radio

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class StubRadioTest {
    @Test
    fun `spielt den Katalog der Reihe nach im Abstand des Intervalls`() =
        runTest {
            val radio = StubRadio(backgroundScope, StubRadio.INTERVAL)
            runCurrent()
            assertThat(radio.nowPlaying.value?.track).isEqualTo(StubRadio.CATALOG[0])

            advanceTimeBy(StubRadio.INTERVAL)
            runCurrent()

            assertThat(radio.nowPlaying.value?.track).isEqualTo(StubRadio.CATALOG[1])
            assertThat(radio.history.value.map { it.track }).containsExactly(StubRadio.CATALOG[0], StubRadio.CATALOG[1])
        }

    @Test
    fun `beginnt nach dem letzten Titel wieder von vorn`() =
        runTest {
            val radio = StubRadio(backgroundScope, StubRadio.INTERVAL)
            runCurrent()

            advanceTimeBy(StubRadio.INTERVAL * StubRadio.CATALOG.size)
            runCurrent()

            assertThat(radio.nowPlaying.value?.track).isEqualTo(StubRadio.CATALOG[0])
        }
}
