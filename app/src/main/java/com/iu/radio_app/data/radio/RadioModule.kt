package com.iu.radio_app.data.radio

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Der Sender ist in beiden Betriebsarten der simulierte (ADR 003, ADR 011). */
@Module
@InstallIn(SingletonComponent::class)
abstract class RadioModule {
    @Binds
    abstract fun bindRadioRepository(impl: SimulatedStation): RadioRepository
}
