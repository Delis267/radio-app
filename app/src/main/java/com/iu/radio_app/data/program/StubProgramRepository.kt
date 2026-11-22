package com.iu.radio_app.data.program

import com.iu.radio_app.data.radio.StubRadio
import com.iu.radio_app.model.Host
import com.iu.radio_app.model.PlayedTrack
import com.iu.radio_app.model.Show
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant

/**
 * Datenquelle ohne Netzwerk für lokale Entwicklung (ADR 003). Später ersetzt durch Firestore.
 * Es läuft immer [SHOW], die Titel kommen von [StubRadio].
 */
@Singleton
class StubProgramRepository
    @Inject
    constructor(
        private val radio: StubRadio,
    ) : ProgramRepository {
        override val currentShow: Flow<Show?> = flowOf(SHOW)

        override val nowPlaying: Flow<PlayedTrack?> = radio.nowPlaying

        override fun playlist(showId: String): Flow<List<PlayedTrack>> = if (showId == SHOW.id) radio.history else flowOf(emptyList())

        override fun showsOf(hostId: String): Flow<List<Show>> = flowOf(if (hostId == HOST.id) listOf(SHOW) else emptyList())

        companion object {
            val HOST =
                Host(
                    id = "elise-mueller",
                    name = "Elise Müller",
                    description =
                        "Elise ist schon wach, wenn du noch den Snooze-Knopf suchst. Sie bringt dir Wetter, Stau und " +
                            "Ohrwürmer. Gut gelaunt wie sonst niemand vor 7 Uhr. Kaffee hilft, aber Elise hilft mehr.",
                    photoUrl = null,
                )
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
