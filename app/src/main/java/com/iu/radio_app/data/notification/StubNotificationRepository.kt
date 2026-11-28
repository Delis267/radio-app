package com.iu.radio_app.data.notification

import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.iu.radio_app.data.coroutines.ApplicationScope
import com.iu.radio_app.data.songrequest.SongRequestRepository
import com.iu.radio_app.model.SongRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Datenquelle ohne Netzwerk für lokale Entwicklung (ADR 003). Später ersetzt durch Firebase Cloud Messaging.
 * Meldet eigene Wünsche, die der Sender gespielt hat, als System-Benachrichtigung, solange die Einstellung an ist.
 * Ist die App offen, entfällt sie, dann zeigt die App selbst einen Banner.
 */
@Singleton
class StubNotificationRepository(
    playedRequests: Flow<SongRequest>,
    scope: CoroutineScope,
    private val isAppInForeground: () -> Boolean,
    private val showNotification: (SongRequest) -> Unit,
) : NotificationRepository {
    @Inject
    constructor(
        @ApplicationContext context: Context,
        songRequests: SongRequestRepository,
        @ApplicationScope scope: CoroutineScope,
    ) : this(
        songRequests.playedRequests,
        scope,
        { ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) },
        PlayedRequestNotification(context)::show,
    )

    private val notify = MutableStateFlow(false)

    override val notifyWhenRequestPlayed: Flow<Boolean> = notify

    init {
        scope.launch {
            playedRequests.collect { request ->
                when {
                    !notify.value -> Log.d(TAG, "Wunsch ${request.id} gespielt, Benachrichtigung ist aus")
                    isAppInForeground() -> Log.d(TAG, "Wunsch ${request.id} gespielt, App ist offen, keine Push-Nachricht")
                    else -> showNotification(request)
                }
            }
        }
    }

    override suspend fun setNotifyWhenRequestPlayed(enabled: Boolean) {
        Log.i(TAG, "Benachrichtigung bei gespieltem Songwunsch: ${if (enabled) "an" else "aus"}")
        notify.value = enabled
    }

    private companion object {
        // Höchstens 23 Zeichen, sonst kürzt Android den Tag unter API 26
        const val TAG = "StubNotifications"
    }
}
