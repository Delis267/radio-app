package com.iu.radio_app.data.radio

import android.util.Log
import com.iu.radio_app.model.Host
import com.iu.radio_app.model.PlayedTrack
import com.iu.radio_app.model.Show
import com.iu.radio_app.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Simulierter Sender (ADR 011): Die Senderdaten stehen fest im Code, den Ablauf berechnet er aus der Uhrzeit.
 * Ab [PROGRAM_START] läuft [CATALOG] alle [interval] einen Titel weiter im Kreis.
 * So zeigen alle Geräte mit demselben Stand ohne Absprache denselben Titel und dieselbe Playlist.
 */
@Singleton
class SimulatedStation(
    /** Hier laufen der Sender und alle Repositories, die auf ihn reagieren. */
    val scope: CoroutineScope,
    private val clock: Clock,
    private val interval: Duration,
) {
    @Inject
    constructor() : this(CoroutineScope(SupervisorJob() + Dispatchers.Default), Clock.System, INTERVAL)

    private val current = MutableStateFlow<PlayedTrack?>(null)
    private val played = MutableStateFlow<List<PlayedTrack>>(emptyList())

    /** Gerade laufender Titel, `null` bis der Sender zum ersten Mal gerechnet hat. */
    val nowPlaying: StateFlow<PlayedTrack?> = current.asStateFlow()

    /** Die letzten gespielten Titel in der Reihenfolge, in der sie liefen, inklusive des laufenden. */
    val history: StateFlow<List<PlayedTrack>> = played.asStateFlow()

    init {
        scope.launch {
            while (true) {
                val slot = slotAt(clock.now())
                played.value = ((slot - MAX_HISTORY + 1).coerceAtLeast(0)..slot).map(::trackInSlot)
                current.value = trackInSlot(slot)
                Log.i(TAG, "Jetzt läuft: ${current.value?.track?.artist} – ${current.value?.track?.title}")
                delay(startOf(slot + 1) - clock.now())
            }
        }
    }

    /** Nummer des Sendeplatzes, der zu [instant] läuft. Vor [PROGRAM_START] läuft der erste. */
    private fun slotAt(instant: Instant): Long =
        ((instant - PROGRAM_START).inWholeMilliseconds / interval.inWholeMilliseconds).coerceAtLeast(0)

    private fun startOf(slot: Long): Instant = PROGRAM_START + (interval.inWholeMilliseconds * slot).milliseconds

    private fun trackInSlot(slot: Long) = PlayedTrack(CATALOG[(slot % CATALOG.size).toInt()], startOf(slot))

    companion object {
        // Höchstens 23 Zeichen, sonst kürzt Android den Tag unter API 26
        private const val TAG = "SimulatedStation"
        val INTERVAL = 10.seconds
        private const val MAX_HISTORY = 50

        /** Ab hier zählt der Sender. Nie ändern, sonst passt „gespielt“ nicht mehr zu bestehenden Wünschen. */
        val PROGRAM_START: Instant = Instant.parse("2026-01-01T00:00:00Z")

        // IDs in diesem Objekt nie ändern: Wünsche und Bewertungen in Firestore verweisen darauf (ADR 011)

        /** Alle Titel, die der Sender spielt und die man sich wünschen kann. */
        val CATALOG =
            listOf(
                Track("bohemian-rhapsody", "Bohemian Rhapsody", "Queen", "A Night at the Opera", 1975, null),
                Track("dont-stop-me-now", "Don't Stop Me Now", "Queen", "Jazz", 1978, null),
                Track("billie-jean", "Billie Jean", "Michael Jackson", "Thriller", 1982, null),
                Track("99-luftballons", "99 Luftballons", "Nena", "Nena", 1983, null),
                Track("major-tom", "Major Tom (Völlig losgelöst)", "Peter Schilling", "Fehler im System", 1983, null),
                Track("rolling-in-the-deep", "Rolling in the Deep", "Adele", "21", 2010, null),
                Track("atemlos", "Atemlos durch die Nacht", "Helene Fischer", "Farbenspiel", 2013, null),
                Track("auf-uns", "Auf uns", "Andreas Bourani", "Hey", 2014, null),
                Track("blinding-lights", "Blinding Lights", "The Weeknd", "After Hours", 2019, null),
                Track("bad-guy", "bad guy", "Billie Eilish", "When We All Fall Asleep, Where Do We Go?", 2019, null),
            )

        val HOST =
            Host(
                id = "elise-mueller",
                name = "Elise Müller",
                description =
                    "Elise ist schon wach, wenn du noch den Snooze-Knopf suchst. Sie bringt dir Wetter, Stau und " +
                        "Ohrwürmer. Gut gelaunt wie sonst niemand vor 7 Uhr. Kaffee hilft, aber Elise hilft mehr.",
                photoUrl = null,
            )

        /** Die einzige Sendung, sie läuft immer. */
        val SHOW =
            Show(
                id = "vormittag-mit-elise",
                title = "Vormittag mit Elise",
                host = HOST,
                startsAt = Instant.parse("2026-11-21T09:00:00Z"),
                endsAt = Instant.parse("2026-11-21T12:00:00Z"),
            )
    }
}
