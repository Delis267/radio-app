# Technische Bausteine: Optionen für DI, Nebenläufigkeit und Tests

Ticket: `issues/02-technische-bausteine-recherchieren.md`. Stand: 2026-11-04. Geprüft am Commit `a71eef0` (`2-projekt-setup`).

Dieses Dokument liefert Fakten und Abwägungen für die Tickets 05 und 06. Es trifft keine Wahl.

## Kernaussagen

1. **Alle Optionen laufen auf unserem Stack.** Manuelle DI, Hilt 2.60.1 mit KSP und Koin 4.2.2 bauten in Wegwerf-Kopien des Projekts. Ihre JVM-Tests liefen ohne Emulator durch. Am Plugin-Block war nur bei Hilt und KSP etwas zu ergänzen.
2. **Das fehlende Kotlin-Android-Plugin ist kein Hindernis.** Hilt und KSP funktionieren mit Built-in Kotlin, solange die Versionen stimmen. Hilt braucht mindestens 2.59, KSP sollte mindestens 2.3.10 sein. kapt ist dagegen nicht direkt nutzbar.
3. **Der eigentliche Stolperstein ist Espresso, nicht lifecycle oder activity.** Compose-Tests unter Robolectric mit SDK 37 scheitern zunächst an zwei Stellen: einem fehlenden JVM-Flag und dem alten Espresso. Beides ist mit je einer Zeile Build-Konfiguration behoben.
4. **lifecycle 2.6.1 und activity-compose 1.8.0 behindern keine Option.** Die Einträge im Versionskatalog gelten faktisch gar nicht: Gradle löst sie ohnehin höher auf, etwa lifecycle auf 2.9.4.
5. **Nur echte Geräte- und Emulatortests brauchen einen Emulator.** Logik-, ViewModel-, Compose-Screen- und Verdrahtungstests liefen alle auf der JVM.

## Methode und Belegstufen

Jede Aussage trägt eine von drei Kennzeichnungen:

- **[Quelle]**: steht in einer Primärquelle, siehe Liste am Ende.
- **[Experiment]**: selbst ausgeführt, siehe unten.
- **[Einschätzung]**: eigene Schlussfolgerung, nicht direkt belegt.

### Experimente

Das Projekt wurde in ein temporäres Verzeichnis exportiert, das Repository blieb unverändert. Jede Kopie bekam einen Mini-Durchstich aus `ValueSource` (Interface mit Stub und Fake), `ValueViewModel` (`StateFlow` über `stateIn`) und `ValueScreen` (Composable).

Umgebung: Gradle 9.6.0, AGP 9.5.0, JBR 25.0.4 als `JAVA_HOME`, Windows 11. Es lief kein Emulator.

| Kopie | Inhalt | `testDebugUnitTest` | `assembleDebug` und `assembleDebugAndroidTest` |
|---|---|---|---|
| E1 | manuelle DI, Katalogversionen unverändert | 4 von 4 bestanden | gebaut |
| E1b | manuelle DI, lifecycle 2.11.0, activity 1.13.0, Coroutines 1.11.0 | 4 von 4 | gebaut |
| E2 | Hilt 2.60.1, KSP 2.3.13, `hilt-lifecycle-viewmodel-compose` 1.4.0 | 4 von 4 | gebaut |
| E3 | Koin 4.2.2 (klassische DSL) | 6 von 6, siehe Koin-Befund | gebaut |
| E4a | JUnit 5.14.1 ohne Zusatz-Plugin, Vintage-Engine | 4 von 4 | nicht geprüft |
| E4b | JUnit 6.1.3 ohne Zusatz-Plugin, Vintage-Engine | 4 von 4 | nicht geprüft |
| E4c | JUnit 5.14.1 mit `de.mannodermaus.android-junit` 2.0.2 | 4 von 4 | nicht geprüft |

Die Testzahlen gelten für den Endstand mit den beiden Korrekturen aus dem Abschnitt „Robolectric auf SDK 37“.

**Grenzen der Experimente:**

- Instrumentierte Tests wurden nur kompiliert, nie ausgeführt.
- Das Ersetzen der Datenquelle durch einen Fake im Verdrahtungstest lief nur bei Hilt. E1 und E3 prüften die Verdrahtung mit dem Stub.
- `verify()` von Koin lief nur gegen einen korrekten Graphen. Ob es Fehler findet, ist nicht getestet.
- Zeiten sind Einzelmessungen, kein Benchmark.
- Turbine, MockK, kapt und das Compose-Modul von `android-junit5` wurden nicht ausprobiert.
- Das `enableEdgeToEdge()` der echten `MainActivity` war im Durchstich nicht enthalten.

## Ausgangslage des Stacks

| Punkt | Befund | Beleg |
|---|---|---|
| AGP 9.5 | maximal API 37, verlangt Gradle ab 9.6.0 und JDK ab 17 | [Quelle: AGP 9.5] |
| Kotlin | AGP 9 hängt zur Laufzeit an KGP 2.2.10, im Projekt effektiv 2.2.10 | [Quelle: AGP 9.0], [Experiment] |
| Compose BOM 2026.02.01 | enthält `ui` und `ui-test-junit4` 1.10.4, `material3` 1.4.0. Aktuell ist 2026.09.00 | [Quelle: Maven] |
| Coroutines | ohne eigene Deklaration transitiv vorhanden: 1.9.0, auch für den Hauptcode kompilierbar | [Experiment E1] |

## Auswirkungen von AGP 9 ohne Kotlin-Android-Plugin

- Built-in Kotlin ist ab AGP 9 Standard und ersetzt `org.jetbrains.kotlin.android`. Das Plugin zusätzlich anzuwenden führt zu einem Fehler. [Quelle: Built-in Kotlin]
- Das Abschalten per `android.builtInKotlin=false` geht ab AGP 10 nicht mehr und erfordert außerdem `android.newDsl=false`. [Quelle: Built-in Kotlin]
- **KSP:** funktioniert ohne Sonderkonfiguration. E2 lief mit KSP 2.3.13, Kotlin 2.2.10 und ohne geänderte `gradle.properties`. Der Abhängigkeitsbericht zeigt `2.2.10-2.0.2 -> 2.3.13`. [Experiment]
- **KSP-Versionen:** Seit 2.3.0 ist die KSP-Version nicht mehr an Kotlin gebunden. 2.3.10 behebt die Auflösung der R-Klasse bei Built-in Kotlin. 2.3.12 verlangt AGP ab 8.12.0. [Quelle: KSP-Releases]
- **Hilt:** Das Gradle-Plugin unterstützt AGP 9 seit 2.59 und setzt ab da AGP 9 voraus. 2.59.2 behebt langsame inkrementelle Builds durch `HiltSyncTask`. [Quelle: Dagger-Releases]
- **Dagger mit KSP:** gilt als stabil ab Dagger 2.60 und KSP 2.3.9. [Quelle: Dagger KSP]
- **Veraltetes Beispiel:** Die Android-Doku zu Hilt-Tests nennt noch Hilt 2.57.1. Diese Version liegt vor der AGP-9-Unterstützung des Plugins und passt nicht zu unserem Stack. [Quelle: Hilt Testing], [Einschätzung]
- **kapt:** `org.jetbrains.kotlin.kapt` ist mit Built-in Kotlin inkompatibel. Der Ausweg heißt `com.android.legacy-kapt`, empfohlen wird KSP. Die Hilt-Setup-Seite nennt noch das alte kapt-Plugin, das gilt hier also nicht. [Quelle: Built-in Kotlin], [Quelle: Hilt Setup]
- **Configuration Cache:** im Projekt aktiv. E2, E3 und E4c liefen damit ohne gemeldete Probleme. [Experiment]

**Nebenwirkung auf die Standardbibliothek:** Hilt zieht `kotlin-stdlib` auf 2.3.21, Koin auf 2.3.20, während der Compiler bei 2.2.10 bleibt. Das baute und lief. [Experiment] Kotlin sagt dazu, das Binärformat sei für das nächste Sprach-Release „mostly forwards compatible“, garantiert das aber nicht, und spätere Releases sind ausgeschlossen. [Quelle: Kotlin Evolution] Ein künftiges Bibliotheks-Update auf eine stdlib 2.4 könnte daher eine Anhebung des Compilers erzwingen. [Einschätzung] Wie das geht, beschreibt die AGP-Doku. [Quelle: AGP 9.0]

## Alte Abhängigkeiten

| Katalogeintrag | Aufgelöst in E1 | Behindert eine Option? |
|---|---|---|
| `lifecycle-runtime-ktx` 2.6.1 | 2.9.4 (mit Hilt 2.11.0, mit Koin 2.10.0) | Nein |
| `activity-compose` 1.8.0 | 1.8.2 (mit Koin 1.12.4) | Nein |
| `core-ktx` 1.10.1 | 1.16.0 | Nein |
| `espresso-core` 3.5.1 | gilt nur für `androidTest`, in `test` kommt ein älteres Espresso transitiv über `ui-test-junit4` | **Ja, für Robolectric auf SDK 37, siehe Abschnitt „Robolectric auf SDK 37“** |

- Selbst mit neu ergänzten Artefakten in Version 2.6.1 hebt Gradle auf 2.9.4 an, und der Durchstich läuft. [Experiment E1]
- `collectAsStateWithLifecycle` gibt es seit lifecycle 2.6.0. [Quelle: lifecycle] Das Artefakt `lifecycle-runtime-compose` fehlt aber im Katalog, ebenso `lifecycle-viewmodel-compose`. Beide müssen unabhängig von der DI-Wahl ergänzt werden, sofern sie gebraucht werden. Koin bringt das zweite selbst mit. [Experiment]
- Ein Update auf die aktuellen stabilen Versionen (lifecycle 2.11.0, activity 1.13.0) läuft ebenfalls. [Experiment E1b] Die minSdk dieser Bibliotheken liegt bei 23, also unter unserer. [Quelle: lifecycle], [Quelle: activity]
- **Eine künftige Grenze:** activity `1.14.0-alpha02` hebt das `compileSdk` der Compose-Bibliotheken auf 37.1, AGP 9.5 unterstützt laut Doku maximal API 37. Das betrifft heute keine stabile Version, kann aber ein AGP-Update erzwingen. [Quelle: activity], [Einschätzung]
- **Offene Frage der Karte:** Für die Kompatibilität ist kein Update nötig. Dafür spricht trotzdem, dass der Katalog heute Versionen nennt, die gar nicht zum Einsatz kommen. [Einschätzung]

## Dependency Injection

| | Manuell | Hilt 2.60.1 | Koin 4.2.2 |
|---|---|---|---|
| Kompatibel mit dem Stack | Ja | Ja | Ja |
| Zusätzliche Gradle-Plugins | keine | 2 (KSP, Hilt) | keine |
| Zusätzliche Abhängigkeiten im Hauptcode | keine für die DI selbst | `hilt-android`, `hilt-compiler` (ksp), `hilt-lifecycle-viewmodel-compose` | `koin-bom`, `koin-androidx-compose` |
| Zusätzliche Abhängigkeiten für Tests | keine | `hilt-android-testing`, `hilt-compiler` (kspTest) | `koin-test` |
| Eigener Code | `AppContainer`, `Application`, ViewModel-Factory | `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, `@Module` | `Application` mit `startKoin`, Modul-DSL |
| Fehler im Graphen fallen auf | beim Kompilieren, weil gewöhnliche Konstruktoraufrufe | beim Kompilieren | zur Laufzeit oder im `verify()`-Test |
| Erzeugter Code | keiner | 22 Dateien aus KSP im Durchstich, plus Task `hiltJavaCompileDebug` | keiner |
| Debug-APK (Einzelmessung, ohne R8) | 11,7 MB | 11,6 MB | 13,1 MB |

Die Einstufung „beim Kompilieren“ für manuelle DI ist eine [Einschätzung], die übrigen Zeilen stammen aus den Experimenten und den Quellen unten.

### Manuell

- Die Android-Doku beschreibt das Muster mit `AppContainer` in der `Application` und einer Factory für das ViewModel, auch für Compose. [Quelle: Manuelle DI], [Quelle: ViewModel-Factories]
- Sie empfiehlt: „When possible, it's recommended to use Hilt rather than manual dependency injection.“ [Quelle: Manuelle DI]
- Die genannten Nachteile betreffen vor allem wachsende Apps: Boilerplate und selbst verwaltete Container für Scopes einzelner Abläufe. Falsch gemacht, drohen „subtle bugs and memory leaks“. [Quelle: Manuelle DI]
- Für Unit-Tests ist kein Framework nötig. Das gilt für alle drei Optionen. [Quelle: Hilt Testing]
- **Noch zu klären, falls gewählt:** Wie kommt ein Fake in einen Verdrahtungstest über die ganze Activity? Das lief in E1 nicht und bräuchte eine eigene Lösung, etwa einen austauschbaren Container oder einen Test mit `setContent` ohne Activity. [Einschätzung]

### Hilt

- Laut Android-Doku die für Jetpack empfohlene DI-Bibliothek, mit Prüfung zur Kompilierzeit. [Quelle: DI-Überblick]
- Der Austausch der Datenquelle per `@TestInstallIn` ist der dokumentierte Weg. Er lief unter Robolectric bis in die UI: `FakeValueSource` kam als „Wert: fake“ an. [Quelle: Hilt Testing], [Experiment E2]
- **Mehrkosten bei Tests:** Für Robolectric braucht es `@HiltAndroidTest`, `HiltAndroidRule` und `@Config(application = HiltTestApplication::class)`. Instrumentierte Tests brauchen zusätzlich einen eigenen `AndroidJUnitRunner` und eine `HiltTestActivity`. [Quelle: Hilt Testing]
- `hiltViewModel()` liegt seit androidx.hilt 1.3.0 im Artefakt `hilt-lifecycle-viewmodel-compose` und zieht damit keine Navigation-Abhängigkeit nach. [Quelle: androidx.hilt]
- Die Regel `HiltAndroidRule` ist eine JUnit-4-Regel, das bindet diese Tests an JUnit 4 oder die Vintage-Engine. [Quelle: Hilt Testing], [Einschätzung]

### Koin

- Aktuell stabil ist 4.2.2. Der Maven-Eintrag „latest“ zeigt 4.3.0-Beta1, daher sollte die Version fest angegeben werden. [Quelle: Koin Setup], [Quelle: Maven]
- **Das Compiler-Plugin scheidet aktuell aus.** Es verlangt Kotlin ab 2.3.20, wir haben 2.2.10. Ohne Anhebung des Compilers bleibt also die klassische DSL ohne Prüfung beim Build. [Quelle: Koin Compiler-Plugin]
- Ersatz ist `Module.verify()` als JVM-Test. Es prüft Konstruktorparameter, aber keine Qualifier, Properties und `get()`-Aufrufe in Definitionen. `checkModules()` ist veraltet. [Quelle: Koin Verify] Weil `viewModel { ValueViewModel(get()) }` genau so einen `get()`-Aufruf enthält, bleibt offen, wie viel der Test bei dieser Schreibweise abdeckt. Das müsste ein Fehlertest klären. [Einschätzung]
- **Befund bei Robolectric:** Jeder Test, der die `Application` startet, ruft `startKoin` auf. Ohne Aufräumen scheitert schon der zweite Test mit `KoinApplicationAlreadyStartedException`, auch ein reiner Screen-Test ohne Koin-Bezug. Erst `stopKoin()` im `@After` jedes betroffenen Tests löst das. [Experiment E3]
- Die Android-Doku warnt allgemein bei Service Locators vor schwerer testbarem Code, weil alle Tests denselben globalen Zustand teilen. [Quelle: DI-Überblick] Der Koin-Befund passt zu dieser Warnung. Koin selbst als Service Locator einzuordnen, ist aber eine [Einschätzung].

### Abwägung nach dem Leitkriterium

| Kriterium | Manuell | Hilt | Koin |
|---|---|---|---|
| Einrichtung | am geringsten | am höchsten | mittel |
| Austauschbarkeit der Datenquelle | eine Zeile im Container | Modul austauschen, im Test belegt | Modul austauschen |
| Fake im App-weiten Test | selbst zu bauen | eingebaut | möglich, aber globaler Zustand |
| Fehler im Graphen | beim Kompilieren | beim Kompilieren | erst im Test oder zur Laufzeit |
| Verhalten beim Wachsen | Boilerplate steigt | stabil | stabil |
| Konzepte, die die Gruppe lernen muss | wenige | viele (Komponenten, Scopes, Module) | mittel |

Diese Tabelle ist eine [Einschätzung] auf Basis der Befunde oben.

Der Umstieg von manuell auf ein Framework dürfte günstig bleiben, wenn Klassen ihre Abhängigkeiten über den Konstruktor bekommen. Die Android-Doku zeigt, dass mit Hilt annotierte Klassen weiterhin direkt konstruierbar sind. [Quelle: Hilt Testing], [Einschätzung]

## Nebenläufigkeit

Coroutines und Flow sind hier keine echten Alternativen, sondern zwei Teile desselben Pakets.

- Coroutines sind die empfohlene Lösung für asynchrone Programmierung auf Android. [Quelle: Coroutines]
- Flow baut auf Coroutines auf und liefert mehrere Werte nacheinander, etwa Live-Updates. Eine `suspend`-Funktion liefert nur einen Wert. [Quelle: Flow]
- **Eignung für Firebase:** Die Doku nennt Flow „a great fit for live data updates“. Firestore bringt laut Doku eigene Flow-Erweiterungen mit, für andere Listener gibt es `callbackFlow`. [Quelle: Flow]
- **UI-Zustand:** Die Doku zeigt `StateFlow` im ViewModel mit `collectAsStateWithLifecycle()` im Composable. [Quelle: State Holder] Das lief in allen Varianten. [Experiment]
- **Kosten:** kein Plugin. Für Hauptcode ist nichts zwingend zu ergänzen, weil Coroutines transitiv vorhanden sind. [Experiment E1] Sich darauf zu verlassen ist aber fragil, die Doku nennt `kotlinx-coroutines-android` als Abhängigkeit. [Quelle: Coroutines], [Einschätzung] Für Tests kommt `kotlinx-coroutines-test` hinzu. [Quelle: Coroutines testen]

**Was beim Testen zu beachten ist** [Quelle: Coroutines testen], [Quelle: Flow testen]:

- In lokalen Tests fehlt `Dispatchers.Main`. `viewModelScope` nutzt ihn fest verdrahtet, daher braucht es `Dispatchers.setMain`, üblicherweise als JUnit-4-Regel.
- Dispatcher in Klassen zu injizieren macht Tests deterministisch.
- Ein mit `stateIn` und `WhileSubscribed` erzeugter `StateFlow` braucht im Test einen Sammler, sonst bleibt der Wert stehen. Das war auch im Durchstich nötig. [Experiment]
- Die Doku empfiehlt, bei `StateFlow` auf `value` zu prüfen. Turbine ist eine optionale Fremdbibliothek.
- `UnconfinedTestDispatcher` und `setMain` erzeugen Opt-in-Warnungen, keine Fehler. [Experiment]

## Tests

### Welche Tests laufen ohne Emulator?

| Testart | Ort | Emulator? | Laufzeit im Experiment | Beleg |
|---|---|---|---|---|
| Logik, ViewModel, Flow mit Fakes | `src/test` | nein | ca. 0,2 s | [Experiment] |
| `verify()` des Koin-Graphen | `src/test` | nein | ca. 1 s | [Experiment E3] |
| Compose-Screen unter Robolectric | `src/test` | nein | ca. 5–20 s je Klasse | [Experiment] |
| Ganze Activity samt DI unter Robolectric | `src/test` | nein | ca. 16–20 s | [Experiment] |
| Compose-UI-Test auf Gerät | `src/androidTest` | **ja** | nicht ausgeführt | [Quelle: Testen in Studio] |
| Kompilieren der `androidTest`-Quellen | Gradle | nein | im Build enthalten | [Experiment] |

Die Laufzeiten unter Robolectric enthalten den Start der Sandbox.

**Grenzen von Robolectric:** Die Doku nennt es geeignet für die meisten UI-Verhaltenstests. Gerätetests bleiben nötig für System-UI wie Edge-to-Edge und Bild-in-Bild sowie für nicht unterstützte Funktionen wie `WebView`. Für reine Logik gilt Robolectric als letzter Ausweg. [Quelle: Robolectric bei Android] Die App ruft heute `enableEdgeToEdge()` auf, das fällt also in diese Kategorie.

**Instrumentierte Tests in CI:**

- Build-verwaltete Geräte gibt es erst ab API 27, also oberhalb unserer minSdk 25. [Quelle: Managed Devices]
- API 25 ließe sich so nicht testen, dafür bräuchte es einen anderen Emulator-Start. [Einschätzung]
- Auf GitHub Actions ist `-Pandroid.testoptions.manageddevices.emulator.gpu=swiftshader_indirect` nötig. [Quelle: Managed Devices]
- Der CI-Aufbau selbst ist nach der Karte außerhalb dieses Rahmens.

### Robolectric auf SDK 37

Robolectric 4.17 unterstützt SDK 37 und ist die erste stabile Version dafür. [Quelle: Robolectric-Releases] Standardmäßig nutzte der Testlauf die `targetSdk` 37, per `@Config(sdk = [25])` auch unsere minSdk. Beide Fälle liefen im Endstand. [Experiment]

**Einrichtung im Build:**

```kotlin
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED") }
        }
    }
}
dependencies {
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation("org.robolectric:robolectric:4.17")
    testImplementation("androidx.test.espresso:espresso-core:3.7.0")
}
```

Dieser Block stammt aus den Experimenten und ist kein fertiger Vorschlag für den Katalog.

**Die beiden Fehler, die ohne diese Zeilen auftreten** (nur auf SDK 37, der Test mit SDK 25 lief von Anfang an) [Experiment]:

1. Ohne das Flag: `IllegalAccessException` auf `jdk.internal.access.SharedSecrets`. Die Robolectric-Doku listet neun `--add-opens`-Flags für Java ab 17. Im Experiment reichte dieses eine. [Quelle: Robolectric Setup]
2. Mit dem transitiven Espresso aus `ui-test-junit4`: `NoSuchMethodException: InputManager.getInstance()`, ausgelöst über `runEspressoOnIdle` aus dem Compose-Test. Espresso 3.7.0 ersetzt genau diesen Reflection-Aufruf. [Quelle: androidx.test]

**Offen:** Dasselbe Espresso 3.5.1 steht im Katalog für `androidTest`. Auf aktuellen Geräten könnte derselbe Fehler auftreten, geprüft ist das nicht. [Einschätzung]

**Weitere Kosten:**

- Beim ersten Lauf lädt Robolectric die Android-Laufzeit: 228 MB für SDK 37, 85 MB für SDK 25. Das ist in CI ein Thema für den Cache. [Experiment]
- Der Lauf meldet unter JDK 25: „Restricted methods will be blocked in a future release“, weil Robolectric `System.load` nutzt. Abhilfe laut Meldung: `--enable-native-access=ALL-UNNAMED`. [Experiment]
- Für SDK 36 verlangt Robolectric JDK 21. Eine Angabe für SDK 37 fehlt. Getestet wurde nur mit JDK 25, was auch dem `toolchainVersion=25` des Projekts entspricht. CI sollte daher dieselbe Version nutzen. [Quelle: Robolectric-Releases], [Einschätzung]

### JUnit 4, 5 oder 6

| | JUnit 4 | JUnit 5 ohne Plugin | JUnit 6 ohne Plugin | JUnit 5 mit `android-junit5` |
|---|---|---|---|---|
| Im Projekt | vorhanden | neu | neu | neu |
| Lokale Tests | ja | ja | ja | ja |
| Robolectric | direkt | nur über Vintage-Engine | nur über Vintage-Engine | nur über Vintage-Engine |
| Einrichtung | keine | BOM, Jupiter, Launcher, Vintage, `useJUnitPlatform()` | wie JUnit 5 | zusätzlich ein Gradle-Plugin |
| Instrumentierte Tests | ja | nicht ohne Plugin | nicht ohne Plugin | Gerät ab API 26 |

- Die Android-Doku für lokale Tests und Android Studio gehen von JUnit 4 aus. [Quelle: Lokale Tests], [Quelle: Testen in Studio]
- `createComposeRule` kommt aus `ui-test-junit4`. [Quelle: Compose testen]
- Die Beispiele von Robolectric nutzen JUnit 4. Echte JUnit-5-Unterstützung ist als Issue #3477 offen, die Vintage-Engine ist der gängige Weg. Dass sie im Stack funktioniert, ist in E4a bis E4c belegt. [Quelle: Robolectric Setup], [Quelle: Robolectric #3477], [Experiment]
- **Konflikt mit minSdk 25:** `android-junit5` verlangt für JUnit 5 Geräte ab API 26, für JUnit 6 ab API 35. Auf älteren Geräten gibt es eine Exception, außer man setzt `behaviorForUnsupportedDevices = Skip`. Die README erwähnt AGP 9 nicht, im Experiment lief nur die lokale Seite. [Quelle: android-junit5], [Experiment E4c]
- Kosten eines Mischbetriebs: Jupiter-Tests und JUnit-4-Tests mit Robolectric liefen nebeneinander, aber die Gruppe hätte dann zwei Test-APIs im selben Ordner. [Experiment], [Einschätzung]

### Fakes statt Mocks

- Die Android-Doku bevorzugt Fakes: „Fakes don't require a mocking framework and are lightweight. They are **preferred**.“ Komplexe Mocks sollen vermieden werden. [Quelle: Test Doubles], [Quelle: Lokale Tests]
- **Kosten:** Fakes brauchen keine Abhängigkeit, aber ein Interface als Naht. Das Skeleton braucht dieses ohnehin für die austauschbare Datenquelle. Im Durchstich war der Fake wenige Zeilen lang. [Experiment]
- Ein Mock-Framework bleibt nur für Android-Klassen oder Drittanbieter ohne Naht interessant. Die Doku nennt Mockito und MockK als optional. MockK ist aktuell 1.14.11, wurde aber nicht getestet. [Quelle: Lokale Tests], [Quelle: Maven]

## Fragen für die Grilling-Tickets

**Ticket 05 (Bausteine):**

1. Wiegt die Prüfung beim Build oder die geringere Einrichtung schwerer? Manuelle DI bietet beides für kleine Graphen, skaliert aber schlechter.
2. Wie groß wird der Graph bis Meilenstein 3 mit Firebase und Rollen voraussichtlich?
3. Ist Koin ohne Compiler-Plugin attraktiv, wenn der Compiler vorerst bei 2.2.10 bleibt? Oder soll Kotlin angehoben werden?
4. Soll `kotlinx-coroutines-android` ausdrücklich deklariert werden?
5. Werden die alten Katalogversionen im selben Zug aktualisiert?

**Ticket 06 (Testschichten):**

1. Reicht Robolectric für die Compose-Schicht, oder gibt es zusätzlich einen Gerätetest, etwa für Edge-to-Edge?
2. Wird `espresso-core` auch für `androidTest` auf 3.7.0 angehoben?
3. Bleibt es bei JUnit 4, oder lohnt sich der Mischbetrieb?
4. Wie wird API 25 in CI abgedeckt, wenn überhaupt? Build-verwaltete Geräte beginnen erst bei API 27.
5. Wird der Download der Android-Laufzeit in CI gecacht?

## Quellen

**Build und Kotlin**

- [AGP 9.5](https://developer.android.com/build/releases/agp-9-5-0-release-notes)
- [AGP 9.0](https://developer.android.com/build/releases/agp-9-0-0-release-notes)
- [Built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin)
- [KSP-Releases](https://github.com/google/ksp/releases), [KSP 2.3.0](https://github.com/google/ksp/releases/tag/2.3.0)
- [Kotlin Evolution](https://kotlinlang.org/docs/kotlin-evolution-principles.html)
- Maven: [Google Maven](https://dl.google.com/dl/android/maven2/), [Maven Central](https://repo1.maven.org/maven2/), [Gradle Plugin Portal](https://plugins.gradle.org/m2/), Metadaten abgerufen am 2026-11-04

**Dependency Injection**

- [DI-Überblick](https://developer.android.com/training/dependency-injection)
- [Manuelle DI](https://developer.android.com/training/dependency-injection/manual)
- [ViewModel-Factories](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories)
- [Hilt Setup](https://dagger.dev/hilt/gradle-setup), [Dagger KSP](https://dagger.dev/dev-guide/ksp.html), [Dagger-Releases](https://github.com/google/dagger/releases)
- [Hilt Testing](https://developer.android.com/training/dependency-injection/hilt-testing), [androidx.hilt](https://developer.android.com/jetpack/androidx/releases/hilt)
- [Koin Setup](https://insert-koin.io/docs/setup/gradle/), [Koin Compiler-Plugin](https://insert-koin.io/docs/setup/compiler-plugin/), [Koin Verify](https://insert-koin.io/docs/reference/koin-test/verify/)

**Nebenläufigkeit und Zustand**

- [Coroutines](https://developer.android.com/kotlin/coroutines), [Coroutines testen](https://developer.android.com/kotlin/coroutines/test)
- [Flow](https://developer.android.com/kotlin/flow), [Flow testen](https://developer.android.com/kotlin/flow/test)
- [State Holder](https://developer.android.com/topic/architecture/ui-layer/stateholders)
- [lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle), [activity](https://developer.android.com/jetpack/androidx/releases/activity)

**Tests**

- [Lokale Tests](https://developer.android.com/training/testing/local-tests), [Testen in Studio](https://developer.android.com/studio/test/test-in-android-studio)
- [Test Doubles](https://developer.android.com/training/testing/fundamentals/test-doubles)
- [Compose testen](https://developer.android.com/develop/ui/compose/testing)
- [Robolectric bei Android](https://developer.android.com/training/testing/local-tests/robolectric)
- [Robolectric Setup](https://robolectric.org/getting-started/), [Robolectric-Releases](https://github.com/robolectric/robolectric/releases), [Robolectric #3477](https://github.com/robolectric/robolectric/issues/3477)
- [androidx.test](https://developer.android.com/jetpack/androidx/releases/test)
- [Managed Devices](https://developer.android.com/studio/test/gradle-managed-devices)
- [android-junit5](https://github.com/mannodermaus/android-junit5)
