# Entscheidung
- Dependency Injection mit Hilt über KSP (kein kapt).
- Nebenläufigkeit mit Coroutines und Flow, UI-Zustand als `StateFlow`, gesammelt mit `collectAsStateWithLifecycle()`.
- Tests mit JUnit 4, Assertions mit AssertJ 3.x, Fakes statt Mock-Framework.
- Statische Analyse mit Android Lint und ktlint, Befunde kommentiert die CI am PR.
- Veraltete Versionen im Versionskatalog werden beim Einrichten mit aktualisiert.

# Begründung
Hilt ist die von Android empfohlene DI-Bibliothek, prüft den Graphen beim Kompilieren
und bleibt stabil, wenn mit Firebase, Auth und mehreren Bildschirmen der Graph wächst.
Flow passt zu den Live-Updates von Firestore.
JUnit 4 setzen Robolectric und `createComposeRule` voraus, JUnit 5 kollidiert mit minSdk 25.
AssertJ liest sich besser als JUnit-Asserts und liefert aussagekräftigere Fehlermeldungen.

# Alternativen
- Manuelle DI, weniger Einrichtung, aber Boilerplate wächst mit jedem Bildschirm.
- Koin, Graph-Fehler erst zur Laufzeit, globaler Zustand stört Robolectric-Tests.
- AssertK, Kotlin-nativ, aber seit 2024 kein Release mehr.
