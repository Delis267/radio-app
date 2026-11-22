package com.iu.radio_app.data.rating

import com.iu.radio_app.model.Rating
import com.iu.radio_app.model.RatingSummary
import com.iu.radio_app.model.RatingTarget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

/**
 * Datenquelle ohne Netzwerk für lokale Entwicklung (ADR 003). Später ersetzt durch Firestore.
 * Kennt nur die eigenen Bewertungen, Zusammenfassungen zählen daher nur diese.
 */
@Singleton
class StubRatingRepository
    @Inject
    constructor() : RatingRepository {
        private val ratings = MutableStateFlow<Map<RatingTarget, Rating>>(emptyMap())

        override fun myRating(target: RatingTarget): Flow<Rating?> = ratings.map { it[target] }

        override suspend fun rate(
            target: RatingTarget,
            stars: Int,
            comment: String?,
        ): Result<Unit> {
            ratings.update { it + (target to Rating(target, AUTHOR_NAME, stars, comment, Clock.System.now())) }
            return Result.success(Unit)
        }

        override fun summary(target: RatingTarget): Flow<RatingSummary> = ratings.map { summaryOf(listOfNotNull(it[target])) }

        override fun summaries(targets: Set<RatingTarget>): Flow<Map<RatingTarget, RatingSummary>> =
            ratings.map { all -> targets.associateWith { summaryOf(listOfNotNull(all[it])) } }

        override fun comments(hostId: String): Flow<List<Rating>> =
            ratings.map { all ->
                all.values
                    .filter { it.target == RatingTarget.Moderation(hostId) && it.comment != null }
                    .sortedByDescending { it.createdAt }
            }

        private fun summaryOf(ratings: List<Rating>) = RatingSummary(ratings.groupingBy { it.stars }.eachCount())

        companion object {
            private const val AUTHOR_NAME = "Demo"
        }
    }
