package com.iu.radio_app.data.radio

import com.iu.radio_app.model.PlayedTrack
import com.iu.radio_app.model.Show
import com.iu.radio_app.model.Track
import kotlinx.coroutines.flow.Flow

/**
 * Alles, was vom Sender kommt (ADR 011): Programm, „Jetzt läuft“ und Katalog.
 * Heute berechnet von [SimulatedStation]. Gäbe es den Sender, würde nur die Implementierung ersetzt.
 */
interface RadioRepository {
    /** Laufende Sendung, `null` außerhalb des Programms. */
    val currentShow: Flow<Show?>

    /** Gerade laufender Titel, `null` wenn nichts läuft. */
    val nowPlaying: Flow<PlayedTrack?>

    /** Alle Titel, die der Sender spielt und die man sich wünschen kann. */
    val catalog: List<Track>

    /** Suche im Katalog nach Interpret oder Songtitel. */
    suspend fun search(query: String): Result<List<Track>>

    /** Alle Titel einer Sendung in der Reihenfolge, in der sie gespielt wurden. */
    fun playlist(showId: String): Flow<List<PlayedTrack>>

    /** Alle Sendungen einer Moderatorin oder eines Moderators, neueste zuerst. */
    fun showsOf(hostId: String): Flow<List<Show>>
}
