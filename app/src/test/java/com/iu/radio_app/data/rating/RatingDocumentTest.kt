package com.iu.radio_app.data.rating

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNull
import com.iu.radio_app.model.Rating
import com.iu.radio_app.model.RatingTarget
import org.junit.Test
import kotlin.time.Instant

/** Prüft den Aufbau der Firestore-Dokumente ohne Firebase (ADR 006). Die Firestore-Aufrufe selbst werden von Hand geprüft. */
class RatingDocumentTest {
    private val createdAt = Instant.parse("2026-11-28T10:00:00Z")

    @Test
    fun `Dokument-ID ist je Person und Ziel fest, damit eine neue Bewertung die alte ersetzt`() {
        val elise = RatingTarget.Moderation("elise-mueller")

        assertThat(RatingDocument.id("uid1", elise)).isEqualTo("uid1_moderation_elise-mueller")
        assertThat(RatingDocument.id("uid1", RatingTarget.Song("billie-jean"))).isEqualTo("uid1_song_billie-jean")
        assertThat(RatingDocument.id("uid2", elise)).isNotEqualTo(RatingDocument.id("uid1", elise))
    }

    @Test
    fun `gleiche ID bei Song und Sendung fuehrt nicht zum selben Dokument`() {
        assertThat(RatingDocument.id("uid1", RatingTarget.Song("x")))
            .isNotEqualTo(RatingDocument.id("uid1", RatingTarget.Show("x")))
    }

    @Test
    fun `geschriebene Felder werden wieder zur selben Bewertung`() {
        val targets = listOf(RatingTarget.Song("billie-jean"), RatingTarget.Show("vormittag"), RatingTarget.Moderation("tom"))
        for (target in targets) {
            val fields = RatingDocument.fields("uid1", target, stars = 4, comment = "Super")

            assertThat(RatingDocument.toRating(fields, createdAt))
                .isEqualTo(Rating(target, RatingDocument.AUTHOR_NAME, 4, "Super", createdAt))
        }
    }

    @Test
    fun `Sterne kommen aus Firestore als Long und ohne Kommentar als null`() {
        val fields = RatingDocument.fields("uid1", RatingTarget.Song("a"), stars = 5, comment = null) + ("stars" to 5L)

        val rating = RatingDocument.toRating(fields, createdAt)

        assertThat(rating?.stars).isEqualTo(5)
        assertThat(rating?.comment).isNull()
    }

    @Test
    fun `unbekannter Typ, fehlende Felder und Sterne ausserhalb 1 bis 5 werden uebersprungen`() {
        val valid = RatingDocument.fields("uid1", RatingTarget.Song("a"), stars = 3, comment = null)

        assertThat(RatingDocument.toRating(valid + ("targetType" to "podcast"), createdAt)).isNull()
        assertThat(RatingDocument.toRating(valid - "targetId", createdAt)).isNull()
        assertThat(RatingDocument.toRating(valid + ("stars" to 0L), createdAt)).isNull()
        assertThat(RatingDocument.toRating(valid + ("stars" to 6L), createdAt)).isNull()
        assertThat(RatingDocument.toRating(valid, createdAt = null)).isNull()
    }

    @Test
    fun `Zusammenfassung zaehlt je Sternzahl`() {
        val ratings =
            listOf(4, 4, 2).map { Rating(RatingTarget.Song("a"), RatingDocument.AUTHOR_NAME, it, null, createdAt) }

        val summary = RatingDocument.summaryOf(ratings)

        assertThat(summary.countByStars).isEqualTo(mapOf(4 to 2, 2 to 1))
        assertThat(summary.average).isEqualTo(10.0 / 3)
    }
}
