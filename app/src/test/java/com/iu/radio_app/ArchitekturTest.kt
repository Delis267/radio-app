package com.iu.radio_app

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withNameStartingWith
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/** Prüft die Regeln aus ADR 003 und ADR 004. */
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
}
