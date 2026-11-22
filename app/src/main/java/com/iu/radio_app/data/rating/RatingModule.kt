package com.iu.radio_app.data.rating

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Legt fest, welche Implementierung hinter [RatingRepository] steckt (ADR 003). */
@Module
@InstallIn(SingletonComponent::class)
abstract class RatingModule {
    @Binds
    abstract fun bindRatingRepository(impl: StubRatingRepository): RatingRepository
}
