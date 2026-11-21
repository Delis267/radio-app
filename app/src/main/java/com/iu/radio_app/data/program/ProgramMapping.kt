package com.iu.radio_app.data.program

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.iu.radio_app.model.Host
import com.iu.radio_app.model.PlayedTrack
import com.iu.radio_app.model.Show
import com.iu.radio_app.model.Track
import kotlin.time.Instant

/*
 * Übersetzt Firestore-Dokumente in Modelle aus model/ (ADR 004: Firebase-Typen verlassen data/ nicht).
 * Bewusst von Hand statt mit toObject(): ohne Reflection, daher auch mit R8 im Release-Build ohne Keep-Regeln.
 * Unvollständige Dokumente werden übersprungen und geloggt statt die App abstürzen zu lassen.
 */

/** Feldnamen in Firestore, an einer Stelle, damit Repository, Mapping und Tests nicht auseinanderlaufen. */
internal object ProgramFields {
    const val STATION = "station"
    const val LIVE = "live"
    const val SHOWS = "shows"
    const val PLAYLIST = "playlist"

    const val SHOW_ID = "showId"
    const val NOW_PLAYING = "nowPlaying"
    const val TITLE = "title"
    const val STARTS_AT = "startsAt"
    const val ENDS_AT = "endsAt"
    const val HOST = "host"
    const val ID = "id"
    const val NAME = "name"
    const val DESCRIPTION = "description"
    const val PHOTO_URL = "photoUrl"
    const val STARTED_AT = "startedAt"
    const val TRACK = "track"
    const val ARTIST = "artist"
    const val ALBUM = "album"
    const val RELEASE_YEAR = "releaseYear"
    const val COVER_URL = "coverUrl"
}

// Höchstens 23 Zeichen, sonst kürzt Android den Tag unter API 26
private const val TAG = "FirestoreProgram"

internal fun DocumentSnapshot.toShow(): Show? {
    if (!exists()) return null
    val show =
        runCatching {
            Show(
                id = id,
                title = getString(ProgramFields.TITLE)!!,
                host = (get(ProgramFields.HOST) as Map<*, *>).toHost()!!,
                startsAt = getTimestamp(ProgramFields.STARTS_AT)!!.toKotlinInstant(),
                endsAt = getTimestamp(ProgramFields.ENDS_AT)!!.toKotlinInstant(),
            )
        }.getOrNull()
    if (show == null) Log.w(TAG, "Sendung ${reference.path} unvollständig, übersprungen")
    return show
}

internal fun Map<*, *>.toPlayedTrack(): PlayedTrack? {
    val startedAt = (this[ProgramFields.STARTED_AT] as? Timestamp)?.toKotlinInstant()
    val track = (this[ProgramFields.TRACK] as? Map<*, *>)?.toTrack()
    if (startedAt == null || track == null) {
        Log.w(TAG, "Gespielter Titel unvollständig, übersprungen: $this")
        return null
    }
    return PlayedTrack(track, startedAt)
}

private fun Map<*, *>.toHost(): Host? {
    val id = this[ProgramFields.ID] as? String ?: return null
    val name = this[ProgramFields.NAME] as? String ?: return null
    return Host(
        id = id,
        name = name,
        description = this[ProgramFields.DESCRIPTION] as? String ?: "",
        photoUrl = this[ProgramFields.PHOTO_URL] as? String,
    )
}

private fun Map<*, *>.toTrack(): Track? {
    val id = this[ProgramFields.ID] as? String ?: return null
    val title = this[ProgramFields.TITLE] as? String ?: return null
    val artist = this[ProgramFields.ARTIST] as? String ?: return null
    return Track(
        id = id,
        title = title,
        artist = artist,
        album = this[ProgramFields.ALBUM] as? String,
        // Firestore speichert ganze Zahlen als Long
        releaseYear = (this[ProgramFields.RELEASE_YEAR] as? Number)?.toInt(),
        coverUrl = this[ProgramFields.COVER_URL] as? String,
    )
}

// Timestamp.toInstant() liefert java.time.Instant und braucht API 26, minSdk ist 25
private fun Timestamp.toKotlinInstant(): Instant = Instant.fromEpochSeconds(seconds, nanoseconds)
