package com.iu.radio_app.data.songrequest

import android.util.Log
import com.iu.radio_app.data.coroutines.ApplicationScope
import com.iu.radio_app.data.radio.RadioRepository
import com.iu.radio_app.model.RequestedTrack
import com.iu.radio_app.model.SongRequest
import com.iu.radio_app.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

/**
 * Datenquelle ohne Netzwerk für die Betriebsart Stubs (ADR 003). Kennt nur die eigenen Wünsche,
 * [requestedTracks] zählt daher nur diese.
 * Spielt der Sender ([RadioRepository]) einen Titel, gelten alle offenen Wünsche dafür als gespielt (ADR 011).
 */
@Singleton
class StubSongRequestRepository
    @Inject
    constructor(
        private val radio: RadioRepository,
        @ApplicationScope scope: CoroutineScope,
    ) : SongRequestRepository {
        private val requests = MutableStateFlow<List<SongRequest>>(emptyList())
        private val suggestions = mutableListOf<String>()
        private var nextId = 1
        private val played = MutableSharedFlow<SongRequest>(extraBufferCapacity = PLAYED_BUFFER)

        override val playedRequests: SharedFlow<SongRequest> = played.asSharedFlow()

        init {
            scope.launch {
                radio.nowPlaying.filterNotNull().collect { markPlayed(it.track) }
            }
        }

        private fun markPlayed(track: Track) {
            var newlyPlayed = emptyList<SongRequest>()
            requests.update { all ->
                newlyPlayed =
                    all
                        .filter { it.track.id == track.id && it.status == SongRequest.Status.OPEN }
                        .map { it.copy(status = SongRequest.Status.PLAYED) }
                all.map { request -> newlyPlayed.find { it.id == request.id } ?: request }
            }
            newlyPlayed.forEach {
                Log.i(TAG, "Wunsch ${it.id} gespielt (${track.artist} – ${track.title}), nicht mehr offen")
                played.tryEmit(it)
            }
        }

        override suspend fun request(trackId: String): Result<SongRequest> {
            val track = radio.catalog.find { it.id == trackId }
            if (track == null) {
                Log.w(TAG, "Wunsch abgelehnt: Titel $trackId nicht im Katalog")
                return Result.failure(NoSuchElementException("Track $trackId not in catalog"))
            }
            if (requests.value.any { it.track.id == trackId && it.status == SongRequest.Status.OPEN }) {
                Log.w(TAG, "Wunsch abgelehnt: \"${track.title}\" ist bereits offen gewünscht")
                return Result.failure(AlreadyRequestedException())
            }
            val request = SongRequest("request-${nextId++}", track, Clock.System.now(), SongRequest.Status.OPEN)
            requests.update { it + request }
            Log.i(TAG, "Wunsch ${request.id} angelegt: ${track.artist} – ${track.title} (offen: ${requests.value.size})")
            return Result.success(request)
        }

        override suspend fun suggest(artistAndTitle: String): Result<Unit> {
            val text = artistAndTitle.trim()
            if (text.isEmpty()) {
                Log.w(TAG, "Vorschlag abgelehnt: leerer Text")
                return Result.failure(IllegalArgumentException("Suggestion must not be blank"))
            }
            suggestions += text
            Log.i(TAG, "Vorschlag eingereicht: \"$text\" (insgesamt ${suggestions.size})")
            return Result.success(Unit)
        }

        override val openRequests: Flow<List<SongRequest>> =
            requests.map { all ->
                all
                    .asReversed()
                    .filter { it.status == SongRequest.Status.OPEN }
                    .also { Log.d(TAG, "Offene Wünsche: ${it.size}") }
            }

        override val requestedTracks: Flow<List<RequestedTrack>> =
            requests.map { all ->
                all
                    .filter { it.status == SongRequest.Status.OPEN }
                    .groupBy { it.track.id }
                    .values
                    .map { group -> RequestedTrack(group.first().track, group.size, group.maxOf { it.requestedAt }) }
                    .sortedWith(compareByDescending<RequestedTrack> { it.requestCount }.thenByDescending { it.lastRequestedAt })
                    .also { Log.d(TAG, "Gewünschte Titel: ${it.size}") }
            }

        companion object {
            // Höchstens 23 Zeichen, sonst kürzt Android den Tag unter API 26
            private const val TAG = "StubSongRequests"
            private const val PLAYED_BUFFER = 16
        }
    }
