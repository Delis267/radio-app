package com.iu.radio_app.data.program

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.iu.radio_app.model.PlayedTrack
import com.iu.radio_app.model.Show
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [ProgramRepository] auf Firestore mit Live-Updates (ADR 003). Aufbau der Daten:
 *
 * ```
 * station/live                    showId, nowPlaying: {startedAt, track}   ← „Jetzt läuft“
 * shows/{showId}                  title, startsAt, endsAt, host: {id, name, description, photoUrl}
 * shows/{showId}/playlist/{id}    startedAt, track: {id, title, artist, album, releaseYear, coverUrl}
 * ```
 *
 * Host und Track sind in die Dokumente kopiert (denormalisiert), damit jede Abfrage ohne Nachladen auskommt.
 */
@Singleton
class FirestoreProgramRepository
    @Inject
    constructor(
        private val db: FirebaseFirestore,
    ) : ProgramRepository {
        private val live = db.collection(ProgramFields.STATION).document(ProgramFields.LIVE).snapshots()

        @OptIn(ExperimentalCoroutinesApi::class)
        override val currentShow: Flow<Show?> =
            live
                .map { it.getString(ProgramFields.SHOW_ID) }
                .distinctUntilChanged()
                .flatMapLatest { showId ->
                    if (showId == null) {
                        flowOf(null)
                    } else {
                        db.collection(ProgramFields.SHOWS).document(showId).snapshots().map { it.toShow() }
                    }
                }

        override val nowPlaying: Flow<PlayedTrack?> =
            live.map { (it.get(ProgramFields.NOW_PLAYING) as? Map<*, *>)?.toPlayedTrack() }

        override fun playlist(showId: String): Flow<List<PlayedTrack>> =
            db
                .collection(ProgramFields.SHOWS)
                .document(showId)
                .collection(ProgramFields.PLAYLIST)
                .orderBy(ProgramFields.STARTED_AT, Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot -> snapshot.documents.mapNotNull { it.data?.toPlayedTrack() } }

        // Sortiert auf dem Gerät statt mit orderBy: Filter plus Sortierung über ein anderes Feld
        // bräuchte einen zusammengesetzten Index im Projekt, bei wenigen Sendungen je Person lohnt das nicht.
        override fun showsOf(hostId: String): Flow<List<Show>> =
            db
                .collection(ProgramFields.SHOWS)
                .whereEqualTo("${ProgramFields.HOST}.${ProgramFields.ID}", hostId)
                .snapshots()
                .map { snapshot -> snapshot.documents.mapNotNull { it.toShow() }.sortedByDescending { it.startsAt } }
    }
