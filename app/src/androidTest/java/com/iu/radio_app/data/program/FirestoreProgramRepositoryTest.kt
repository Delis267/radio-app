package com.iu.radio_app.data.program

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.iu.radio_app.model.Host
import com.iu.radio_app.model.PlayedTrack
import com.iu.radio_app.model.Show
import com.iu.radio_app.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Prüft [FirestoreProgramRepository] gegen den Firestore-Emulator, nie gegen das echte Projekt (ADR 003, 006).
 *
 * Vorher im Repo-Root `firebase emulators:start --only firestore` starten.
 * Auf einem echten Gerät zusätzlich `adb reverse tcp:8080 tcp:8080` und den Test mit
 * `-Pandroid.testInstrumentationRunnerArguments.firestoreEmulatorHost=127.0.0.1` starten.
 *
 * Jeder Test nutzt eigene IDs, daher muss der Emulator zwischen den Läufen nicht geleert werden.
 */
@RunWith(AndroidJUnit4::class)
class FirestoreProgramRepositoryTest {
    // Testnamen mit Unterstrich statt Leerzeichen: Leerzeichen in Methodennamen erlaubt Android erst ab API 30
    private val repository = FirestoreProgramRepository(db)

    @Test
    fun laufende_Sendung_folgt_station_live() =
        runTest {
            val show = show(hostId = unique("host"))
            writeShow(show)

            live().set(mapOf(ProgramFields.SHOW_ID to show.id)).await()
            assertThat(repository.currentShow.firstMatching { it?.id == show.id }).isEqualTo(show)

            live().set(mapOf(ProgramFields.SHOW_ID to null)).await()
            assertThat(repository.currentShow.firstMatching { it == null }).isNull()
        }

    @Test
    fun jetzt_laeuft_liefert_den_Titel_auch_ohne_optionale_Felder() =
        runTest {
            val played = PlayedTrack(track(unique("track"), album = null, releaseYear = null), at("2026-11-21T09:05:00Z"))

            live().set(mapOf(ProgramFields.NOW_PLAYING to played.toFirestore())).await()

            assertThat(repository.nowPlaying.firstMatching { it?.track?.id == played.track.id }).isEqualTo(played)
        }

    @Test
    fun Playlist_in_der_Reihenfolge_des_Abspielens() =
        runTest {
            val show = show(hostId = unique("host"))
            writeShow(show)
            val first = PlayedTrack(track(unique("track")), at("2026-11-21T09:00:00Z"))
            val second = PlayedTrack(track(unique("track")), at("2026-11-21T09:04:00Z"))
            // Absichtlich in umgekehrter Reihenfolge geschrieben
            playlist(show.id).add(second.toFirestore()).await()
            playlist(show.id).add(first.toFirestore()).await()

            assertThat(repository.playlist(show.id).firstMatching { it.size == 2 }).containsExactly(first, second)
        }

    @Test
    fun Sendungen_einer_Person_neueste_zuerst_ohne_fremde() =
        runTest {
            val hostId = unique("host")
            val older = show(hostId, startsAt = at("2026-11-20T09:00:00Z"))
            val newer = show(hostId, startsAt = at("2026-11-21T09:00:00Z"))
            writeShow(older)
            writeShow(newer)
            writeShow(show(hostId = unique("andere")))

            assertThat(repository.showsOf(hostId).firstMatching { it.size == 2 }).containsExactly(newer, older)
        }

    private suspend fun <T> Flow<T>.firstMatching(predicate: (T) -> Boolean): T = withTimeout(10.seconds) { first(predicate) }

    private fun live() = db.collection(ProgramFields.STATION).document(ProgramFields.LIVE)

    private fun playlist(showId: String) = db.collection(ProgramFields.SHOWS).document(showId).collection(ProgramFields.PLAYLIST)

    private suspend fun writeShow(show: Show) {
        db
            .collection(ProgramFields.SHOWS)
            .document(show.id)
            .set(
                mapOf(
                    ProgramFields.TITLE to show.title,
                    ProgramFields.STARTS_AT to show.startsAt.toTimestamp(),
                    ProgramFields.ENDS_AT to show.endsAt.toTimestamp(),
                    ProgramFields.HOST to
                        mapOf(
                            ProgramFields.ID to show.host.id,
                            ProgramFields.NAME to show.host.name,
                            ProgramFields.DESCRIPTION to show.host.description,
                            ProgramFields.PHOTO_URL to show.host.photoUrl,
                        ),
                ),
            ).await()
    }

    private fun PlayedTrack.toFirestore() =
        mapOf(
            ProgramFields.STARTED_AT to startedAt.toTimestamp(),
            ProgramFields.TRACK to
                mapOf(
                    ProgramFields.ID to track.id,
                    ProgramFields.TITLE to track.title,
                    ProgramFields.ARTIST to track.artist,
                    ProgramFields.ALBUM to track.album,
                    ProgramFields.RELEASE_YEAR to track.releaseYear,
                    ProgramFields.COVER_URL to track.coverUrl,
                ),
        )

    private fun show(
        hostId: String,
        startsAt: Instant = at("2026-11-21T09:00:00Z"),
    ) = Show(
        id = unique("show"),
        title = "Vormittag",
        host = Host(hostId, "Elise Müller", "Moderiert den Vormittag", photoUrl = null),
        startsAt = startsAt,
        endsAt = Instant.fromEpochSeconds(startsAt.epochSeconds + 3 * 3600),
    )

    private fun track(
        id: String,
        album: String? = "Thriller",
        releaseYear: Int? = 1982,
    ) = Track(id, "Billie Jean", "Michael Jackson", album, releaseYear, coverUrl = null)

    private fun at(iso: String) = Instant.parse(iso)

    private fun Instant.toTimestamp() = Timestamp(epochSeconds, nanosecondsOfSecond)

    private fun unique(prefix: String) = "$prefix-${UUID.randomUUID()}"

    private companion object {
        // demo-*: Projekt-IDs mit diesem Präfix nimmt nur der Emulator an, ein Versehen trifft nie das echte Projekt
        const val PROJECT_ID = "demo-radio-app"
        const val EMULATOR_PORT = 8080

        /** Eigene FirebaseApp, unabhängig von google-services.json und dem Hilt-Graphen der App. */
        val db: FirebaseFirestore by lazy {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val options =
                FirebaseOptions
                    .Builder()
                    .setProjectId(PROJECT_ID)
                    .setApplicationId("1:000000000000:android:0000000000000000")
                    .setApiKey("emulator")
                    .build()
            val app = FirebaseApp.initializeApp(context, options, "emulator-test")
            val host = InstrumentationRegistry.getArguments().getString("firestoreEmulatorHost", "10.0.2.2")
            FirebaseFirestore.getInstance(app).apply { useEmulator(host, EMULATOR_PORT) }
        }
    }
}
