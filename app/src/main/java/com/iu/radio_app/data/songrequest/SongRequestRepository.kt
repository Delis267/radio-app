package com.iu.radio_app.data.songrequest

import com.iu.radio_app.model.RequestedTrack
import com.iu.radio_app.model.SongRequest
import com.iu.radio_app.model.Track
import kotlinx.coroutines.flow.Flow

interface SongRequestRepository {
    /** Suche im Katalog nach Interpret oder Songtitel. */
    suspend fun search(query: String): Result<List<Track>>

    suspend fun request(trackId: String): Result<SongRequest>

    /** Vorschlag für einen Titel, der nicht im Katalog ist, z. B. „Interpret, Songtitel“. */
    suspend fun suggest(artistAndTitle: String): Result<Unit>

    /** Offene Wünsche der angemeldeten Person, neueste zuerst. */
    val openRequests: Flow<List<SongRequest>>

    /** Alle Titel mit offenen Wünschen, meistgewünschte zuerst. */
    val requestedTracks: Flow<List<RequestedTrack>>
}
