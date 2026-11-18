package com.iu.radio_app.data.program

import com.iu.radio_app.model.PlayedTrack
import com.iu.radio_app.model.Show
import kotlinx.coroutines.flow.Flow

interface ProgramRepository {
    /** Laufende Sendung, `null` außerhalb des Programms. */
    val currentShow: Flow<Show?>

    /** Gerade laufender Titel, `null` wenn nichts läuft. */
    val nowPlaying: Flow<PlayedTrack?>

    /** Alle Titel einer Sendung in der Reihenfolge, in der sie gespielt wurden. */
    fun playlist(showId: String): Flow<List<PlayedTrack>>

    /** Alle Sendungen einer Moderatorin oder eines Moderators, neueste zuerst. */
    fun showsOf(hostId: String): Flow<List<Show>>
}
