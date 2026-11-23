package com.iu.radio_app.data.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.iu.radio_app.R
import com.iu.radio_app.model.SongRequest

/**
 * Zeigt die System-Benachrichtigung „Dein Songwunsch läuft gerade“ als Banner von oben (hohe Wichtigkeit).
 * Später kommt sie per Push (FCM).
 */
class PlayedRequestNotification(
    private val context: Context,
) {
    fun show(request: SongRequest) {
        val manager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Keine Berechtigung für Benachrichtigungen, Wunsch ${request.id} wird nicht gemeldet")
            return
        }
        if (!manager.areNotificationsEnabled()) {
            Log.w(TAG, "Benachrichtigungen in den Systemeinstellungen aus, Wunsch ${request.id} wird nicht gemeldet")
            return
        }
        manager.createNotificationChannel(
            NotificationChannelCompat
                .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName(context.getString(R.string.notification_channel_song_requests))
                .build(),
        )
        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_heart)
                .setContentTitle(context.getString(R.string.notification_request_played_title))
                .setContentText(context.getString(R.string.notification_request_played_text, request.track.artist, request.track.title))
                .setContentIntent(openApp())
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Banner von oben vor Android 8
                .setAutoCancel(true)
                .build()
        manager.notify(request.id.hashCode(), notification)
        Log.i(TAG, "Benachrichtigung gezeigt: Wunsch ${request.id} (${request.track.title}) läuft")
    }

    /** Tippen auf die Benachrichtigung öffnet die App. */
    private fun openApp(): PendingIntent? =
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

    private companion object {
        const val TAG = "StubNotifications"
        // Wichtigkeit eines Kanals lässt sich nach dem Anlegen nicht mehr ändern, daher neue ID statt "song_requests"
        const val CHANNEL_ID = "song_requests_played"
    }
}
