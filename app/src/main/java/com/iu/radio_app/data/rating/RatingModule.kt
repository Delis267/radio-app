package com.iu.radio_app.data.rating

import com.iu.radio_app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider

/**
 * Legt fest, welche Implementierung hinter [RatingRepository] steckt (ADR 003).
 * Standard ist Firestore, mit `radio.stubs=true` der Stub. [Provider] erzeugt nur die gewählte.
 */
@Module
@InstallIn(SingletonComponent::class)
object RatingModule {
    @Provides
    fun provideRatingRepository(
        stub: Provider<StubRatingRepository>,
        firestore: Provider<FirestoreRatingRepository>,
    ): RatingRepository = if (BuildConfig.USE_STUBS) stub.get() else firestore.get()
}
