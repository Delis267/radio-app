package com.iu.radio_app.data.program

import com.iu.radio_app.data.radio.SimulatedStation
import com.iu.radio_app.data.radio.SimulatedStation.Companion.HOST
import com.iu.radio_app.data.radio.SimulatedStation.Companion.SHOW
import com.iu.radio_app.model.PlayedTrack
import com.iu.radio_app.model.Show
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Programm aus dem simulierten Sender (ADR 011), in beiden Betriebsarten dasselbe.
 * Es läuft immer [SHOW], die Titel berechnet [SimulatedStation].
 */
@Singleton
class SimulatedProgramRepository
    @Inject
    constructor(
        private val station: SimulatedStation,
    ) : ProgramRepository {
        override val currentShow: Flow<Show?> = flowOf(SHOW)

        override val nowPlaying: Flow<PlayedTrack?> = station.nowPlaying

        override fun playlist(showId: String): Flow<List<PlayedTrack>> =
            if (showId == SHOW.id) station.history else flowOf(emptyList())

        override fun showsOf(hostId: String): Flow<List<Show>> = flowOf(if (hostId == HOST.id) listOf(SHOW) else emptyList())
    }
