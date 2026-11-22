package com.iu.radio_app.data.songrequest

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Legt fest, welche Implementierung hinter [SongRequestRepository] steckt (ADR 003). */
@Module
@InstallIn(SingletonComponent::class)
abstract class SongRequestModule {
    @Binds
    abstract fun bindSongRequestRepository(impl: StubSongRequestRepository): SongRequestRepository
}
