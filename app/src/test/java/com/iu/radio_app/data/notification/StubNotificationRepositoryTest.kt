package com.iu.radio_app.data.notification

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import com.iu.radio_app.data.radio.StubRadio
import com.iu.radio_app.model.SongRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class StubNotificationRepositoryTest {
    private val playedRequests = MutableSharedFlow<SongRequest>()
    private val shown = mutableListOf<SongRequest>()
    private var appInForeground = false
    private val request =
        SongRequest("request-1", StubRadio.CATALOG[0], Instant.parse("2026-11-21T10:00:00Z"), SongRequest.Status.PLAYED)

    private fun TestScope.repository() = StubNotificationRepository(playedRequests, backgroundScope, { appInForeground }) { shown += it }

    @Test
    fun `meldet gespielten Wunsch nur bei eingeschalteter Benachrichtigung`() =
        runTest {
            val repository = repository()
            runCurrent()

            playedRequests.emit(request)
            runCurrent()
            assertThat(shown).isEmpty()

            repository.setNotifyWhenRequestPlayed(true)
            playedRequests.emit(request)
            runCurrent()
            assertThat(shown).containsExactly(request)
        }

    @Test
    fun `keine Push-Nachricht, solange die App offen ist`() =
        runTest {
            val repository = repository()
            repository.setNotifyWhenRequestPlayed(true)
            appInForeground = true
            runCurrent()

            playedRequests.emit(request)
            runCurrent()

            assertThat(shown).isEmpty()
        }
}
