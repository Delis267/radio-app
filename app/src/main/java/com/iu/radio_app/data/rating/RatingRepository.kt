package com.iu.radio_app.data.rating

import com.iu.radio_app.model.Rating
import com.iu.radio_app.model.RatingSummary
import com.iu.radio_app.model.RatingTarget
import kotlinx.coroutines.flow.Flow

interface RatingRepository {
    fun myRating(target: RatingTarget): Flow<Rating?>

    /** Bewertet [target]. Eine erneute Bewertung ersetzt die vorige. [comment] nur bei Moderation. */
    suspend fun rate(
        target: RatingTarget,
        stars: Int,
        comment: String? = null,
    ): Result<Unit>

    fun summary(target: RatingTarget): Flow<RatingSummary>

    fun summaries(targets: Set<RatingTarget>): Flow<Map<RatingTarget, RatingSummary>>

    /** Bewertungen der Moderation mit Kommentar, neueste zuerst. */
    fun comments(hostId: String): Flow<List<Rating>>
}
