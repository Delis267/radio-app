package com.iu.radio_app.data.notification

import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    /** Benachrichtigen, wenn ein eigener Songwunsch gespielt wird. */
    val notifyWhenRequestPlayed: Flow<Boolean>

    suspend fun setNotifyWhenRequestPlayed(enabled: Boolean)
}
