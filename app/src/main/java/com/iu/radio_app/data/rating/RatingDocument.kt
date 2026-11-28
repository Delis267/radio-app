package com.iu.radio_app.data.rating

import com.iu.radio_app.model.Rating
import com.iu.radio_app.model.RatingSummary
import com.iu.radio_app.model.RatingTarget
import kotlin.time.Instant

/**
 * Aufbau einer Bewertung in Firestore, ohne Firebase-Typen und damit auf der JVM testbar (ADR 006).
 *
 * ```
 * ratings/{userId}_{targetType}_{targetId}
 *   userId, targetType ("song" | "show" | "moderation"), targetId, stars (1–5), comment (oder null), createdAt
 * ```
 *
 * Die feste Dokument-ID je Person und Ziel sorgt dafür, dass eine neue Bewertung die vorige ersetzt.
 * Dieselben Feldnamen und Werte prüft `firestore.rules`.
 */
internal object RatingDocument {
    const val COLLECTION = "ratings"

    const val USER_ID = "userId"
    const val TARGET_TYPE = "targetType"
    const val TARGET_ID = "targetId"
    const val STARS = "stars"
    const val COMMENT = "comment"
    const val CREATED_AT = "createdAt"

    /** Angezeigt statt eines Namens, Bewertungen sind anonym. */
    const val AUTHOR_NAME = "Hörer:in"

    private const val SONG = "song"
    private const val SHOW = "show"
    private const val MODERATION = "moderation"

    fun id(
        userId: String,
        target: RatingTarget,
    ): String = "${userId}_${typeOf(target)}_${idOf(target)}"

    fun typeOf(target: RatingTarget): String =
        when (target) {
            is RatingTarget.Song -> SONG
            is RatingTarget.Show -> SHOW
            is RatingTarget.Moderation -> MODERATION
        }

    fun idOf(target: RatingTarget): String =
        when (target) {
            is RatingTarget.Song -> target.trackId
            is RatingTarget.Show -> target.showId
            is RatingTarget.Moderation -> target.hostId
        }

    /** Felder zum Schreiben, ohne [CREATED_AT]: den setzt das Repository als Zeitstempel des Servers. */
    fun fields(
        userId: String,
        target: RatingTarget,
        stars: Int,
        comment: String?,
    ): Map<String, Any?> =
        mapOf(
            USER_ID to userId,
            TARGET_TYPE to typeOf(target),
            TARGET_ID to idOf(target),
            STARS to stars,
            COMMENT to comment,
        )

    /** Liest eine Bewertung, `null` bei unbekanntem Typ oder fehlenden Feldern (ADR 011: überspringen statt abstürzen). */
    fun toRating(
        fields: Map<String, Any?>,
        createdAt: Instant?,
    ): Rating? {
        val targetId = fields[TARGET_ID] as? String ?: return null
        val target =
            when (fields[TARGET_TYPE]) {
                SONG -> RatingTarget.Song(targetId)
                SHOW -> RatingTarget.Show(targetId)
                MODERATION -> RatingTarget.Moderation(targetId)
                else -> return null
            }
        // Firestore liefert ganze Zahlen als Long
        val stars = (fields[STARS] as? Number)?.toInt()?.takeIf { it in 1..5 } ?: return null
        return Rating(
            target = target,
            authorName = AUTHOR_NAME,
            stars = stars,
            comment = fields[COMMENT] as? String,
            createdAt = createdAt ?: return null,
        )
    }

    fun summaryOf(ratings: List<Rating>) = RatingSummary(ratings.groupingBy { it.stars }.eachCount())
}
