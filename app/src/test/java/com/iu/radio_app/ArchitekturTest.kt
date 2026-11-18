package com.iu.radio_app

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withNameStartingWith
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Prüft die Regeln aus ADR 003 und ADR 004, inklusive der Trennung nach Rolle in `ui/`.
 * Ein fehlschlagender Test nennt die betroffene Datei.
 */
class ArchitekturTest {
    private val produktion = Konsist.scopeFromProduction()

    @Test
    fun `Datenschicht haengt nicht von der UI ab`() {
        produktion.files
            .filter { it.packagee?.name?.startsWith("com.iu.radio_app.data") == true }
            .flatMap { it.imports }
            .assertFalse(strict = false) { it.name.startsWith("com.iu.radio_app.ui") }
    }

    @Test
    fun `Domaenenmodell ist reines Kotlin ohne Abhaengigkeiten`() {
        val verboten =
            listOf("com.iu.radio_app.data", "com.iu.radio_app.ui", "android", "androidx", "com.google")
        produktion.files
            .filter { it.packagee?.name?.startsWith("com.iu.radio_app.model") == true }
            .flatMap { it.imports }
            .assertFalse(strict = false) { import -> verboten.any { import.name.startsWith("$it.") } }
    }

    @Test
    fun `Firebase wird nur in der Datenschicht importiert`() {
        produktion.files
            .filterNot { it.packagee?.name?.startsWith("com.iu.radio_app.data") == true }
            .flatMap { it.imports }
            .assertFalse(strict = false) { it.name.startsWith("com.google.firebase") }
    }

    @Test
    fun `ViewModels liegen in der UI-Schicht`() {
        produktion
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue(strict = false) { it.resideInPackage("com.iu.radio_app.ui..") }
    }

    @Test
    fun `Repository-Interfaces liegen in der Datenschicht`() {
        produktion
            .interfaces()
            .withNameEndingWith("Repository")
            .assertTrue(strict = false) { it.resideInPackage("com.iu.radio_app.data..") }
    }

    @Test
    fun `Fakes gibt es nur im Testcode`() {
        produktion
            .classes()
            .withNameStartingWith("Fake")
            .assertEmpty()
    }

    @Test
    fun `Hoerer-UI haengt nicht von der Moderations-UI ab`() {
        importsAus("$UI.listener").assertFalse(strict = false) { it.name.startsWith("$UI.moderator") }
    }

    @Test
    fun `Moderations-UI haengt nicht von der Hoerer-UI ab`() {
        importsAus("$UI.moderator").assertFalse(strict = false) { it.name.startsWith("$UI.listener") }
    }

    @Test
    fun `Komponenten kennen weder Features noch Daten noch ViewModels`() {
        val verboten =
            listOf(
                "$UI.listener",
                "$UI.moderator",
                "$UI.login",
                "$UI.navigation",
                "com.iu.radio_app.data",
                "androidx.lifecycle.ViewModel",
                "androidx.hilt.lifecycle.viewmodel",
            )
        importsAus("$UI.components").assertFalse(strict = false) { import ->
            verboten.any { import.name.startsWith(it) }
        }
    }

    @Test
    fun `UI-Pakete liegen in einem Bereich aus ADR 004`() {
        val bereiche = setOf("theme", "components", "navigation", "login", "listener", "moderator")
        produktion.files
            .filter { it.packagee?.name?.startsWith("$UI.") == true }
            .assertTrue(strict = false) { file ->
                file.packagee?.name?.removePrefix("$UI.")?.substringBefore('.') in bereiche
            }
    }

    private fun importsAus(paket: String) =
        produktion.files
            .filter { it.packagee?.name?.startsWith(paket) == true }
            .flatMap { it.imports }

    private companion object {
        const val UI = "com.iu.radio_app.ui"
    }
}
