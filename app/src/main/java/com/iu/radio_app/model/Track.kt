package com.iu.radio_app.model

import kotlin.time.Instant

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val releaseYear: Int?,
    val coverUrl: String?,
)

/** Ein gespielter oder gerade laufender Titel. */
data class PlayedTrack(
    val track: Track,
    val startedAt: Instant,
)
