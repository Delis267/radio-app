package com.iu.radio_app.model

import kotlin.time.Instant

/** Was bewertet wird. */
sealed interface RatingTarget {
    data class Song(
        val trackId: String,
    ) : RatingTarget

    data class Show(
        val showId: String,
    ) : RatingTarget

    data class Moderation(
        val hostId: String,
    ) : RatingTarget
}

data class Rating(
    val target: RatingTarget,
    val authorName: String,
    val stars: Int,
    val comment: String?,
    val createdAt: Instant,
)

data class RatingSummary(
    /** Anzahl Bewertungen je Sternzahl (1 bis 5). */
    val countByStars: Map<Int, Int>,
) {
    val count: Int get() = countByStars.values.sum()

    /** `null`, solange es keine Bewertung gibt. */
    val average: Double?
        get() = if (count == 0) null else countByStars.entries.sumOf { (stars, n) -> stars * n }.toDouble() / count
}
