package com.iu.radio_app.data.program

import com.iu.radio_app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider

/**
 * Legt fest, welche Implementierung hinter [ProgramRepository] steckt (ADR 003).
 * Standard ist Firestore, mit `-Pradio.stubs=true` läuft die App ohne Firebase.
 * [Provider] sorgt dafür, dass nur die gewählte Implementierung entsteht.
 */
@Module
@InstallIn(SingletonComponent::class)
object ProgramModule {
    @Provides
    fun provideProgramRepository(
        stub: Provider<StubProgramRepository>,
        firestore: Provider<FirestoreProgramRepository>,
    ): ProgramRepository = if (BuildConfig.USE_STUBS) stub.get() else firestore.get()
}
