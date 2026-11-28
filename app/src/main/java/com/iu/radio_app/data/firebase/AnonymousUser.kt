package com.iu.radio_app.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Anonyme Anmeldung bei Firebase (ADR 003). Jedes Gerät bekommt eine feste [id], die Sicherheitsregeln
 * erlauben damit nur das Ändern eigener Daten. Unabhängig vom Login der App, Nutzer:innen merken davon nichts.
 *
 * Angemeldet wird erst beim ersten Zugriff. Firebase speichert die Anmeldung auf dem Gerät,
 * nach einem Neustart der App ist die ID dieselbe.
 */
@Singleton
class AnonymousUser
    @Inject
    constructor(
        private val auth: FirebaseAuth,
    ) {
        // Mehrere Repositories fragen beim Start gleichzeitig, angemeldet werden soll nur einmal
        private val signIn = Mutex()

        /** ID des Geräts, meldet bei Bedarf an. Wirft, wenn die Anmeldung scheitert (z. B. offline beim ersten Start). */
        suspend fun id(): String =
            auth.currentUser?.uid ?: signIn.withLock {
                auth.currentUser?.uid ?: auth.signInAnonymously().await().user!!.uid.also {
                    Log.i(TAG, "Anonym angemeldet")
                }
            }

        private companion object {
            // Höchstens 23 Zeichen, sonst kürzt Android den Tag unter API 26
            const val TAG = "AnonymousUser"
        }
    }
