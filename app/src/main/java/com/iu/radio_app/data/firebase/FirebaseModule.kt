package com.iu.radio_app.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.iu.radio_app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Stellt Firestore für alle Firestore-Repositories bereit (ADR 003).
 * Ist `radio.firestoreEmulator` gesetzt (z. B. `10.0.2.2:8080`), spricht die App mit dem lokalen Emulator statt mit dem Projekt.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance().apply {
            val emulator = BuildConfig.FIRESTORE_EMULATOR
            if (emulator.isNotBlank()) {
                val (host, port) = emulator.split(':')
                useEmulator(host, port.toInt())
            }
        }
}
