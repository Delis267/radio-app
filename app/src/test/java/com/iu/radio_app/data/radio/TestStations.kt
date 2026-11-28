package com.iu.radio_app.data.radio

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

/** Uhr, die beim Programmstart beginnt und mit der virtuellen Zeit von [scheduler] läuft. */
fun testClock(scheduler: TestCoroutineScheduler): Clock =
    object : Clock {
        override fun now(): Instant = SimulatedStation.PROGRAM_START + scheduler.currentTime.milliseconds
    }

/** Sender auf der Testuhr: Beim Start läuft der erste Titel des Katalogs, advanceTimeBy schaltet weiter. */
fun TestScope.runningStation() = SimulatedStation(backgroundScope, testClock(testScheduler), SimulatedStation.INTERVAL)

/**
 * Sender, der nie einen Titel spielt: eigene Testuhr, die niemand weiterstellt.
 * Für Tests, die das Abspielen nicht brauchen; sonst liefe advanceUntilIdle endlos.
 */
fun stoppedStation(): SimulatedStation {
    val scheduler = TestCoroutineScheduler()
    return SimulatedStation(TestScope(StandardTestDispatcher(scheduler)), testClock(scheduler), SimulatedStation.INTERVAL)
}
