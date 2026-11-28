package com.iu.radio_app.data.rating

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.iu.radio_app.data.firebase.AnonymousUser
import com.iu.radio_app.model.Rating
import com.iu.radio_app.model.RatingSummary
import com.iu.radio_app.model.RatingTarget
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant

/**
 * [RatingRepository] auf Firestore mit Live-Updates (ADR 003, ADR 011). Aufbau der Dokumente in [RatingDocument].
 *
 * Lesefehler (z. B. Anmeldung offline gescheitert, Regel verweigert) werden geloggt und als „keine Bewertung“
 * geliefert, damit die App nicht abstürzt. Schreibfehler kommen als [Result.failure] zurück.
 */
@Singleton
class FirestoreRatingRepository
    @Inject
    constructor(
        private val db: FirebaseFirestore,
        private val user: AnonymousUser,
    ) : RatingRepository {
        private val ratings get() = db.collection(RatingDocument.COLLECTION)

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun myRating(target: RatingTarget): Flow<Rating?> =
            flow { emit(user.id()) }
                .flatMapLatest { userId -> ratings.document(RatingDocument.id(userId, target)).snapshots() }
                .map { it.toRating() }
                .orOnError("eigene Bewertung $target", null)

        override suspend fun rate(
            target: RatingTarget,
            stars: Int,
            comment: String?,
        ): Result<Unit> =
            runCatching {
                val userId = user.id()
                val fields = RatingDocument.fields(userId, target, stars, comment) +
                    (RatingDocument.CREATED_AT to FieldValue.serverTimestamp())
                ratings.document(RatingDocument.id(userId, target)).set(fields).await()
                Log.i(TAG, "Bewertung gespeichert: $target, $stars Sterne")
            }.onFailure { Log.w(TAG, "Bewertung $target nicht gespeichert", it) }
                .map { }

        override fun summary(target: RatingTarget): Flow<RatingSummary> =
            ratingsOf(target)
                .map(RatingDocument::summaryOf)
                .orOnError("Zusammenfassung $target", RatingDocument.summaryOf(emptyList()))

        override fun summaries(targets: Set<RatingTarget>): Flow<Map<RatingTarget, RatingSummary>> =
            if (targets.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(targets.map { target -> summary(target).map { target to it } }) { it.toMap() }
            }

        // Sortiert auf dem Gerät statt mit orderBy, das bräuchte einen zusammengesetzten Index im Projekt
        override fun comments(hostId: String): Flow<List<Rating>> =
            ratingsOf(RatingTarget.Moderation(hostId))
                .map { all -> all.filter { it.comment != null }.sortedByDescending { it.createdAt } }
                .orOnError("Kommentare $hostId", emptyList())

        /** Alle Bewertungen für [target], erst nach der Anmeldung, weil die Regeln nur Angemeldete lesen lassen. */
        @OptIn(ExperimentalCoroutinesApi::class)
        private fun ratingsOf(target: RatingTarget): Flow<List<Rating>> =
            flow { emit(user.id()) }
                .flatMapLatest {
                    ratings
                        .whereEqualTo(RatingDocument.TARGET_TYPE, RatingDocument.typeOf(target))
                        .whereEqualTo(RatingDocument.TARGET_ID, RatingDocument.idOf(target))
                        .snapshots()
                }.map { snapshot -> snapshot.documents.mapNotNull { it.toRating() } }

        private fun DocumentSnapshot.toRating(): Rating? {
            val fields = data ?: return null
            // Eigene, noch nicht bestätigte Schreibvorgänge haben noch keinen Zeitstempel des Servers: geschätzten nehmen
            val createdAt =
                getTimestamp(RatingDocument.CREATED_AT, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds) }
            return RatingDocument.toRating(fields, createdAt).also {
                if (it == null) Log.w(TAG, "Bewertung ${reference.path} unvollständig oder unbekannt, übersprungen")
            }
        }

        private fun <T> Flow<T>.orOnError(
            what: String,
            fallback: T,
        ): Flow<T> =
            catch {
                Log.w(TAG, "$what nicht lesbar, zeige Ersatzwert", it)
                emit(fallback)
            }

        private companion object {
            // Höchstens 23 Zeichen, sonst kürzt Android den Tag unter API 26
            const val TAG = "FirestoreRatings"
        }
    }
