package com.iu.radio_app.data.program

import com.iu.radio_app.data.radio.SimulatedStation
import com.iu.radio_app.model.PlayedTrack
import com.iu.radio_app.model.Show
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Programm aus dem simulierten Sender (ADR 011), in beiden Betriebsarten dasselbe. */
@Singleton
class SimulatedProgramRepository
    @Inject
    constructor(
        private val station: SimulatedStation,
    ) : ProgramRepository {
        override val currentShow: Flow<Show?> = station.currentShow

        override val nowPlaying: Flow<PlayedTrack?> = station.nowPlaying

        override fun playlist(showId: String): Flow<List<PlayedTrack>> = station.playlistOf(showId)

        override fun showsOf(hostId: String): Flow<List<Show>> = station.showsOf(hostId)
    }
