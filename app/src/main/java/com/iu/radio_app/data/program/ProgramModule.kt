package com.iu.radio_app.data.program

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Das Programm kommt in beiden Betriebsarten vom simulierten Sender (ADR 003, ADR 011). */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProgramModule {
    @Binds
    abstract fun bindProgramRepository(impl: SimulatedProgramRepository): ProgramRepository
}
