package com.iu.radio_app.data.program

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Legt fest, welche Implementierung hinter [ProgramRepository] steckt (ADR 003). */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProgramModule {
    @Binds
    abstract fun bindProgramRepository(impl: StubProgramRepository): ProgramRepository
}
