package com.iu.radio_app.data.radio

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope

/**
 * Sender, der nie einen Titel spielt: eigene Testuhr, die niemand weiterstellt.
 * Für Tests, die das Abspielen nicht brauchen; sonst liefe advanceUntilIdle endlos.
 */
fun stoppedRadio() = StubRadio(TestScope(StandardTestDispatcher(TestCoroutineScheduler())), StubRadio.INTERVAL)
