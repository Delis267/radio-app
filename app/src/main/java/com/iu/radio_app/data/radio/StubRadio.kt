package com.iu.radio_app.data.radio

import android.util.Log
import com.iu.radio_app.model.PlayedTrack
import com.iu.radio_app.model.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Simulierter Sender für die Stubs (ADR 003): spielt [CATALOG] der Reihe nach im Kreis, alle [interval] ein Titel.
 * Ersetzt das, was später Firestore und eine Cloud Function liefern („Jetzt läuft“, Wünsche als gespielt markieren).
 * Läuft nur, solange der App-Prozess läuft.
 */
@Singleton
class StubRadio(
    /** Hier laufen der Sender und alle Stubs, die auf ihn reagieren. */
    val scope: CoroutineScope,
    private val interval: Duration,
) {
    @Inject
    constructor() : this(CoroutineScope(SupervisorJob() + Dispatchers.Default), INTERVAL)

    private val current = MutableStateFlow<PlayedTrack?>(null)
    private val played = MutableStateFlow<List<PlayedTrack>>(emptyList())

    /** Gerade laufender Titel, `null` bis der erste Titel startet. */
    val nowPlaying: StateFlow<PlayedTrack?> = current.asStateFlow()

    /** Gespielte Titel in der Reihenfolge, in der sie liefen, inklusive des laufenden. */
    val history: StateFlow<List<PlayedTrack>> = played.asStateFlow()

    init {
        scope.launch {
            var index = 0
            while (true) {
                val track = PlayedTrack(CATALOG[index], Clock.System.now())
                current.value = track
                played.update { (it + track).takeLast(MAX_HISTORY) }
                Log.i(TAG, "Jetzt läuft: ${track.track.artist} – ${track.track.title}")
                index = (index + 1) % CATALOG.size
                delay(interval)
            }
        }
    }

    companion object {
        // Höchstens 23 Zeichen, sonst kürzt Android den Tag unter API 26
        private const val TAG = "StubRadio"
        val INTERVAL = 10.seconds
        private const val MAX_HISTORY = 50

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
    }
}
