package com.iu.radio_app.data.radio

import android.util.Log
import com.iu.radio_app.data.coroutines.ApplicationScope
import com.iu.radio_app.model.Host
import com.iu.radio_app.model.PlayedTrack
import com.iu.radio_app.model.Show
import com.iu.radio_app.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Simulierter Sender (ADR 011): Die Senderdaten stehen fest im Code, den Ablauf berechnet er aus der Uhrzeit.
 *
 * Ab [PROGRAM_START] spielt jede Sendung aus [SCHEDULE] ihre Playlist, alle [interval] ein Titel.
 * Ist die Playlist durch, folgt ohne Lücke die nächste Sendung, nach der letzten wieder die erste.
 * So zeigen alle Geräte mit demselben Stand ohne Absprache dieselbe Sendung, denselben Titel und dieselbe Playlist.
 *
 * Die Zeit ist in Sendeplätze von je [interval] eingeteilt, gezählt ab [PROGRAM_START].
 * Ein Durchlauf aller Sendungen dauert [CYCLE] Sendeplätze.
 *
 * Einzige Implementierung von [RadioRepository], in beiden Betriebsarten dieselbe (ADR 003).
 */
@Singleton
class SimulatedStation(
    /** Hier läuft der Sender. In Tests auch die Repositories, die auf ihn reagieren. */
    val scope: CoroutineScope,
    private val clock: Clock,
    private val interval: Duration,
) : RadioRepository {
    @Inject
    constructor(@ApplicationScope scope: CoroutineScope) : this(scope, Clock.System, INTERVAL)

    override val catalog: List<Track> get() = CATALOG

    /** Wartet kurz wie bei einer Anfrage an einen echten Sender, damit die App ihren Ladezustand zeigt. */
    override suspend fun search(query: String): Result<List<Track>> {
        delay(SEARCH_LATENCY)
        val term = query.trim()
        val hits = CATALOG.filter { it.artist.contains(term, ignoreCase = true) || it.title.contains(term, ignoreCase = true) }
        Log.d(TAG, "Suche \"$term\": ${hits.size} Treffer ${hits.map { it.id }}")
        return Result.success(hits)
    }

    private val slot = MutableStateFlow(slotAt(clock.now()))

    /** Gerade laufender Titel. */
    override val nowPlaying: StateFlow<PlayedTrack?> = MutableStateFlow<PlayedTrack?>(trackInSlot(slot.value)).also { flow ->
        scope.launch { slot.collect { flow.value = trackInSlot(it) } }
    }.asStateFlow()

    /** Die letzten gespielten Titel über alle Sendungen, in der Reihenfolge, in der sie liefen, inklusive des laufenden. */
    val history: StateFlow<List<PlayedTrack>> = MutableStateFlow(historyUntil(slot.value)).also { flow ->
        scope.launch { slot.collect { flow.value = historyUntil(it) } }
    }.asStateFlow()

    /** Gerade laufende Ausgabe einer Sendung. Es läuft immer eine. */
    override val currentShow: StateFlow<Show> = MutableStateFlow(showInSlot(slot.value)).also { flow ->
        scope.launch { slot.collect { flow.value = showInSlot(it) } }
    }.asStateFlow()

    init {
        scope.launch {
            while (true) {
                val now = slotAt(clock.now())
                slot.value = now
                Log.i(TAG, "${showInSlot(now).title}: ${trackInSlot(now).track.artist} – ${trackInSlot(now).track.title}")
                delay(startOf(now + 1) - clock.now())
            }
        }
    }

    /** Bisher gespielte Titel der letzten oder laufenden Ausgabe von [showId], inklusive des laufenden. */
    override fun playlist(showId: String): Flow<List<PlayedTrack>> =
        slot.map { now -> latestEdition(showId, now)?.let { first -> (first..minOf(now, first + lengthOf(showId) - 1)) } }
            .distinctUntilChanged()
            .map { slots -> slots?.map(::trackInSlot).orEmpty() }

    /** Letzte oder laufende Ausgabe jeder Sendung von [hostId], neueste zuerst. */
    override fun showsOf(hostId: String): Flow<List<Show>> =
        slot.map { now ->
            SCHEDULE
                .filter { it.host.id == hostId }
                .mapNotNull { plan -> latestEdition(plan.id, now)?.let(::showInSlot) }
                .sortedByDescending { it.startsAt }
        }.distinctUntilChanged()

    private fun slotAt(instant: Instant): Long =
        ((instant - PROGRAM_START).inWholeMilliseconds / interval.inWholeMilliseconds).coerceAtLeast(0)

    private fun startOf(slot: Long): Instant = PROGRAM_START + (interval.inWholeMilliseconds * slot).milliseconds

    private fun trackInSlot(slot: Long): PlayedTrack = PlayedTrack(CATALOG[(slot % CYCLE).toInt()], startOf(slot))

    private fun historyUntil(slot: Long): List<PlayedTrack> =
        ((slot - MAX_HISTORY + 1).coerceAtLeast(0)..slot).map(::trackInSlot)

    /** Die Ausgabe, die in [slot] läuft, mit Beginn und Ende dieser Ausgabe. */
    private fun showInSlot(slot: Long): Show {
        val position = (slot % CYCLE).toInt()
        val plan = SCHEDULE.last { OFFSETS.getValue(it.id) <= position }
        val first = slot - position + OFFSETS.getValue(plan.id)
        return Show(plan.id, plan.title, plan.host, startOf(first), startOf(first + plan.playlist.size))
    }

    /** Erster Sendeplatz der letzten Ausgabe von [showId], die bis [now] begonnen hat, `null` wenn es noch keine gab. */
    private fun latestEdition(
        showId: String,
        now: Long,
    ): Long? {
        val offset = OFFSETS[showId] ?: return null
        val inThisCycle = now - now % CYCLE + offset
        val first = if (inThisCycle <= now) inThisCycle else inThisCycle - CYCLE
        return first.takeIf { it >= 0 }
    }

    private fun lengthOf(showId: String): Int = SCHEDULE.first { it.id == showId }.playlist.size

    /** Eine Sendung im Sendeplan. Ihre Ausgaben unterscheiden sich nur in Beginn und Ende, die ID bleibt gleich. */
    private class Plan(
        val id: String,
        val title: String,
        val host: Host,
        val playlist: List<Track>,
    )

    companion object {
        // Höchstens 23 Zeichen, sonst kürzt Android den Tag unter API 26
        private const val TAG = "SimulatedStation"
        val INTERVAL = 1.minutes
        private const val MAX_HISTORY = 50
        private val SEARCH_LATENCY = 300.milliseconds

        /** Ab hier zählt der Sender. Nie ändern, sonst passt „gespielt“ nicht mehr zu bestehenden Wünschen. */
        val PROGRAM_START: Instant = Instant.parse("2026-01-01T00:00:00Z")

        // IDs in diesem Objekt nie ändern: Wünsche und Bewertungen in Firestore verweisen darauf (ADR 011)

        val ELISE =
            Host(
                id = "elise-mueller",
                name = "Elise Müller",
                description =
                    "Elise ist schon wach, wenn du noch den Snooze-Knopf suchst. Sie bringt dir Wetter, Stau und " +
                        "Ohrwürmer. Gut gelaunt wie sonst niemand vor 7 Uhr. Kaffee hilft, aber Elise hilft mehr.",
                photoUrl = null,
            )

        // Platzhalter, bis Name und Beschreibung feststehen
        val TOM =
            Host(
                id = "tom-berger",
                name = "Tom Berger",
                description = "Platzhalter: Beschreibung von Tom.",
                photoUrl = null,
            )

        /** Sendungen in der Reihenfolge, in der sie laufen. Jeder Titel gehört zu genau einer Sendung. */
        private val SCHEDULE =
            listOf(
                Plan(
                    id = "sendung-elise",
                    title = "Sendung mit Elise",
                    host = ELISE,
                    playlist =
                        listOf(
                            Track("bohemian-rhapsody", "Bohemian Rhapsody", "Queen", "A Night at the Opera", 1975, null),
                            Track("dont-stop-me-now", "Don't Stop Me Now", "Queen", "Jazz", 1978, null),
                            Track("billie-jean", "Billie Jean", "Michael Jackson", "Thriller", 1982, null),
                            Track("99-luftballons", "99 Luftballons", "Nena", "Nena", 1983, null),
                            Track("major-tom", "Major Tom (Völlig losgelöst)", "Peter Schilling", "Fehler im System", 1983, null),
                        ),
                ),
                Plan(
                    id = "sendung-tom",
                    title = "Sendung mit Tom",
                    host = TOM,
                    playlist =
                        listOf(
                            Track("rolling-in-the-deep", "Rolling in the Deep", "Adele", "21", 2010, null),
                            Track("atemlos", "Atemlos durch die Nacht", "Helene Fischer", "Farbenspiel", 2013, null),
                            Track("auf-uns", "Auf uns", "Andreas Bourani", "Hey", 2014, null),
                            Track("blinding-lights", "Blinding Lights", "The Weeknd", "After Hours", 2019, null),
                            Track("bad-guy", "bad guy", "Billie Eilish", "When We All Fall Asleep, Where Do We Go?", 2019, null),
                        ),
                ),
            )

        /** Alle Titel, die der Sender spielt und die man sich wünschen kann, in Sendereihenfolge. */
        val CATALOG: List<Track> = SCHEDULE.flatMap { it.playlist }

        /** Sendeplätze für einen Durchlauf aller Sendungen. */
        private val CYCLE: Long = CATALOG.size.toLong()

        /** Erster Sendeplatz jeder Sendung innerhalb eines Durchlaufs. */
        private val OFFSETS: Map<String, Int> =
            SCHEDULE
                .runningFold(0) { offset, plan -> offset + plan.playlist.size }
                .zip(SCHEDULE) { offset, plan -> plan.id to offset }
                .toMap()
    }
}
