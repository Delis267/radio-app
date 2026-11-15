package com.iu.radio_app.model

import kotlin.time.Instant

data class SongRequest(
    val id: String,
    val track: Track,
    val requestedAt: Instant,
    val status: Status,
) {
    enum class Status {
        OPEN,
        PLAYED,
    }
}

/** Ein Titel mit der Anzahl offener Wünsche dafür. */
data class RequestedTrack(
    val track: Track,
    val requestCount: Int,
    val lastRequestedAt: Instant,
)
