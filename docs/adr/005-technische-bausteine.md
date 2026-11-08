# Entscheidung
- Dependency Injection mit Hilt über KSP (kein kapt).
- Nebenläufigkeit mit Coroutines und Flow, UI-Zustand als `StateFlow`, gesammelt mit `collectAsStateWithLifecycle()`.
- Tests mit JUnit 4, Assertions mit AssertK, Fakes statt Mock-Framework.
- Statische Analyse nur mit Android Lint, die CI schlägt nur bei Befunden der Schwere Error fehl. Kein ktlint.
- Veraltete Versionen im Versionskatalog werden beim Einrichten mit aktualisiert.

# Begründung
Hilt ist die von Android empfohlene DI-Bibliothek.
Flow passt zu den Live-Updates von Firestore.
JUnit 4 setzen Robolectric und `createComposeRule` voraus, JUnit 5 kollidiert mit minSdk 25.
AssertK liest sich besser als JUnit-Asserts, liefert aussagekräftigere Fehlermeldungen und ist für Kotlin gebaut.

# Alternativen
- Manuelle DI, weniger Einrichtung, aber Boilerplate wächst mit jedem Bildschirm.
- Koin, Graph-Fehler erst zur Laufzeit, globaler Zustand stört Robolectric-Tests.
- AssertJ, aktiver gepflegt, aber für Java gebaut und ohne Kotlin-Nullability.
