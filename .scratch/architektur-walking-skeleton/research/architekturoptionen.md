# Architekturoptionen für eine Compose-App mit späterem Firebase-Backend

Recherche zu Ticket 01. Stand der Quellen: 2026-11-04. Die Wahl trifft Ticket 03, dieses Dokument trifft sie nicht.

Aussagen mit Link stammen aus der verlinkten Quelle. Abschnitte und Tabellenzeilen, die mit **Einschätzung** markiert sind, sind eigene Ableitungen und nicht von einer Quelle belegt.

## Das Wichtigste vorab

- **Die drei Muster verfolgen dasselbe Ziel.** Fachlogik und UI sollen ohne die äußere Technik testbar sein. In jedem Muster liegt Firebase in einer äußeren, austauschbaren Klasse.
- **Sie unterscheiden sich vor allem in zwei Punkten:** wie viele Schichten es gibt und wem die Schnittstelle zur Datenquelle gehört, also in welche Richtung die Abhängigkeiten zeigen.
- **Bei Firebase unterscheiden sie sich kaum.** `Flow` als Rückgabetyp der Datenschnittstelle funktioniert in allen drei Mustern gleich. Auch die teuren Tests gegen den Firebase-Emulator fallen überall an.
- **Der größte Unterschied ist der Aufwand.** Clean Architecture bringt die meisten zusätzlichen Typen mit. Google rät ausdrücklich davon ab, Use Cases zu erzwingen, die nur weiterreichen.

## Die Optionen

### A: Googles empfohlene App-Architektur

**Aufbau**

- Mindestens zwei Schichten: [UI-Schicht und Datenschicht][g-arch]. Eine Domain-Schicht ist [optional und nur bei Bedarf gedacht][g-domain], zum Beispiel bei komplexer oder wiederverwendeter Logik. Für sie gilt die Stufe [„Recommended in big apps“][g-rec].
- Die [Datenschicht enthält die Geschäftslogik][g-arch]. Sie besteht aus Repositories, die null bis viele Datenquellen nutzen.
- [Pfeile bedeuten Abhängigkeiten zwischen Klassen, und die Domain-Schicht hängt von Klassen der Datenschicht ab][g-arch]. Die UI hängt also von der Datenschicht ab, nicht umgekehrt.
- Als stark empfohlen gelten [UDF, ViewModels auf Bildschirmebene, Coroutines und Flows sowie Repositories, auch bei nur einer Datenquelle][g-rec]. Firebase-APIs stehen dort ausdrücklich als Beispiel für Datenquellen, auf die UI und ViewModel nicht direkt zugreifen sollen.
- Google bezeichnet all das als [Empfehlungen und nicht als harte Vorgaben][g-rec]. Sie sollen an die eigene App angepasst werden.

**Spielraum bei der Schnittstelle**

- Der Leitfaden schreibt nicht vor, wo das Interface liegt. Sein Beispiel nutzt [ein konkretes `NewsRepository` und ein Interface `NewsApi`][g-data], mit dem Hinweis: „Relying on interfaces makes API implementations swappable“.
- Das Beispielprojekt Now in Android definiert [Repositories als Interfaces im Datenmodul][nia-repo], und [die Domain hängt vom Datenmodul ab][nia-domain]. [Test-Implementierungen derselben Interfaces ersetzen Mocking-Bibliotheken][nia-readme].
- Bei nur einer Datenquelle [verschmelzen Teams Repository und Data Source oft][g-data]. Google nennt das üblich, solange man bei einer zweiten Quelle wieder trennt.

### B: Ports & Adapters (Hexagonal Architecture)

**Aufbau**

- Das Ziel: Die Anwendung soll [„equally be driven by users, programs, automated test or batch scripts“ und isoliert von Laufzeitgeräten und Datenbanken entwickelt und getestet werden können][cockburn].
- Ein [Port ist eine zweckgebundene Schnittstelle, ein Adapter übersetzt zwischen ihr und einer Technik][cockburn]. An einem Port können mehrere Adapter hängen.
- Cockburn unterscheidet [primäre Akteure, die die Anwendung antreiben, und sekundäre, die sie selbst antreibt][cockburn]. Für einen sekundären Akteur wie eine Datenbank nennt er ein Mock als natürlichen Testadapter.
- Zur Größe schreibt er: Die Zahl der Ports ist [weitgehend Geschmackssache, und er bevorzugt „two, three or four ports“][cockburn].
- Der Artikel legt weder Pakete noch Klassen fest und erwähnt Android nicht.

**Einschätzung zur Abbildung auf Android**

- Die Datenquellen-Schnittstelle wäre ein sekundärer Port, Firestore und Stub wären seine Adapter.
- Compose mit ViewModel wäre die treibende Seite. Weil `ViewModel` eine androidx-Klasse ist, läge es bei streng technikfreiem Kern außerhalb. Google weist dem ViewModel dagegen [Geschäftslogik][g-rec] zu. Dafür gibt es keine Primärquelle, die Gruppe müsste es selbst festlegen.
- Cockburns „mock“ antwortet auf Anfragen und zeichnet Ereignisse auf. Das liegt näher an Googles [„Fake“][g-doubles] als an Googles „Mock“. Google warnt selbst vor [widersprüchlichen Definitionen][g-doubles].

### C: Clean Architecture

**Aufbau**

- Vier schematische Ringe: [Entities, Use Cases, Interface Adapters, Frameworks & Drivers][clean].
- Die Dependency Rule: [„source code dependencies can only point inwards“][clean]. Sie gilt auch für Datenformate äußerer Ringe.
- Die Ringzahl ist nicht fest: [„the circles are schematic“][clean], die Regel gilt aber immer.
- Grenzen werden [per Dependency Inversion überquert][clean]. Der Use Case ruft ein Interface im inneren Ring auf, ein äußerer Ring implementiert es.
- Der Artikel nennt [Hexagonal als eine der Architekturen, die er zu einer Idee zusammenführt][clean]. Richtung und Ziel decken sich daher mit Option B. C gibt zusätzlich die inneren Ringe vor.

**Abgrenzung zu Googles Domain-Schicht**

- Der Name täuscht: Google warnt ausdrücklich, dass [„domain layer“ in Clean Architecture etwas anderes bedeutet][g-domain].
- Bei Google hängt die Domain [von der Datenschicht ab][g-arch]. Bei Clean zeigt die Datenanbindung nach innen auf den Kern.
- Eine von Google gepflegte „Clean Architecture für Android“ wurde nicht gefunden. Der Artikel selbst ist technologieneutral. Android-Varianten stammen aus Sekundärquellen und wurden hier nicht als Beleg genutzt.

### Weitere Kandidaten

| Kandidat | Einordnung |
|---|---|
| ViewModel greift direkt auf Firebase zu | Google [rät stark davon ab][g-rec]. Eine austauschbare Stub-Datenquelle wäre so nicht möglich, das Leitkriterium wäre verletzt. |
| Firebase-Codelab: MVVM mit Service-Interfaces | Das [Firebase-Codelab][fb-codelab] nennt MVVM die empfohlene Architektur und legt den Zugriff in `model/service/impl`. Der [Firebase-Blog][fb-blog] zeigt dazu `interface StorageService` mit `Flow` und `suspend`-Funktionen. Der Aufbau ähnelt Option A mit Interface an der Datengrenze. Die 2025er Version `v2` im [Repo][fb-repo] wurde im Code nicht geprüft. |
| MVVM oder MVI | **Einschätzung:** Das sind Muster für die UI-Schicht und keine Alternative zu A, B oder C. Googles [UDF-Vorgaben][g-compose] beschreiben die Compose-Seite in allen drei Optionen. MVI wurde nicht separat recherchiert. |

## Vergleich

| Kriterium | A: Google | B: Ports & Adapters | C: Clean Architecture |
|---|---|---|---|
| Schichten | UI, Daten, optional Domain | Kern, Ports, Adapter | Vier Ringe, Anzahl variabel |
| Richtung der Abhängigkeit | UI → Domain → Daten | Adapter → Kern | Immer nach innen |
| Wem gehört die Datenschnittstelle? | Datenschicht (in Now in Android) | Dem Kern, der sie braucht | Innerem Ring |
| Sitz der Fachlogik | Datenschicht, UI-State-Logik im ViewModel, Domain optional | Kern | Entities und Use Cases |
| Herkunft | Gepflegte Android-Doku mit Beispielprojekten | Originalartikel ohne Android-Bezug | Originalartikel ohne Android-Bezug |
| Einarbeitung (**Einschätzung**) | Niedrig, Doku passt direkt auf Compose | Mittel, die Abbildung auf Android ist selbst zu klären | Hoch, Begriffe und Grenzen sind selbst zu klären |

### Testbarkeit

Für alle drei Optionen gilt:

- [Ein ViewModel-Test braucht keinen Emulator][g-doubles], weil das ViewModel nicht vom Android-Framework abhängen sollte.
- `viewModelScope` nutzt fest den Main-Dispatcher. Lokale Tests müssen ihn [per `Dispatchers.setMain` ersetzen][g-coroutines-test].
- Google bevorzugt [Fakes gegenüber Mocks][g-rec] und [empfiehlt, externe Quellen im Unit-Test zu faken][g-data].
- Compose-UI-Tests [liegen typischerweise in `androidTest`][g-what-to-test], also auf Gerät oder Emulator. Robolectric ist [eine Option für lokale Tests][g-doubles]. Die Details gehören zu Ticket 06.
- [UDF erleichtert das getrennte Testen von Zustand und UI][g-compose].

Unterschiede zwischen den Optionen:

| | A | B | C |
|---|---|---|---|
| Naht für den Test-Double | Repository-Interface und/oder Data-Source-Interface | Port | Interface im inneren Ring |
| Logik ohne Android testbar | Ja, sofern [Android-Klassen außerhalb der Komponenten bleiben][g-arch]. Der Compiler erzwingt das nicht. | Ja, durch den Kern | Ja, [ausdrückliches Merkmal][clean] |
| Zusatznutzen weiterer Tests | Domain-Tests nur, wo die Domain existiert | Kern-Tests, sobald der Kern Logik enthält | Use-Case-Tests, auch wenn der Use Case nur weiterreicht |

**Einschätzung:** Das Skeleton soll laut Karte mindestens einen echten Test je Schicht haben und transportiert nur einen technischen Wert. Ein Use-Case-Test in Option C prüfte dann fast nur eine Weiterleitung. Jede zusätzliche Schicht bringt also einen Pflichttest mit wenig Aussagekraft, solange es keine Fachlogik gibt.

### Austauschbarkeit der Datenquelle

- Alle drei tauschen über ein Interface und Dependency Injection. Google schreibt, [DI und Service Locator erlauben schnelles Umschalten zwischen Test- und Produktionsimplementierung][g-arch].
- Der wesentliche Unterschied ist die Richtung:
  - In A kennt die UI die Datenschicht und deren Modelle. Ausgetauscht wird die Implementierung hinter dem Interface.
  - In B und C kennt der Kern keinen Adapter. Das Interface liegt beim Nutzer.
- **Modelltrennung:**
  - C verlangt sie durch die [Regel zu Datenformaten][clean] immer.
  - Google verlangt sie [mindestens dann, wenn die Daten einer Quelle nicht zu dem passen, was die App erwartet][g-data]. In komplexen Apps [empfiehlt es ein Modell pro Schicht][g-rec].
  - Für alle gilt: Sobald Firestore-Typen über die Schnittstelle nach außen gelangen, ist die Austauschbarkeit dahin.

**Einschätzung:** Die Schnittstelle kapselt die Signatur, aber nicht das gesamte Verhalten. Firestore bringt [Offline-Cache][fb-offline] und [sofortige lokale Listener-Ereignisse][fb-listen] mit, das bleibt beim Austausch eine Änderung im Verhalten. Das betrifft alle drei Optionen gleich.

### Aufwand für eine kleine Gruppe

**Einschätzung:** Die Zählung unten ist eine Schätzung für das Skeleton, kein gemessener Wert. Sie geht von einem Bildschirm, einem technischen Wert und einem einzigen Gradle-Modul aus.

| | A | B | C (streng) |
|---|---|---|---|
| Produktive Typen | Composable, ViewModel, UiState, Repository-Interface, Stub | Wie A, Interface heißt Port, dazu optional ein Kern-Service | Wie B, dazu Entity, Use Case und bei Bedarf Mapper und Datenmodell |
| Grobe Anzahl | 5 | 5 bis 6 | 7 bis 10 |
| Wächst bei neuem Feature um | Repository und ViewModel | Port oder Methode am Port, Adapter und ViewModel | Zusätzlich je Aktion ein Use Case und oft ein Mapper |
| Regeln, die die Gruppe selbst festlegen muss | Wenige, die Doku gibt vor | Was ist Kern, wo liegt das ViewModel, wie viele Ports | Dasselbe, dazu Schnitt der Ringe und Modellgrenzen |

Belege dazu:

- Der Zwang, jeden Zugriff über die Domain zu leiten, hat laut Google einen [„potentially significant disadvantage“][g-domain]: Use Cases für einfache Aufrufe bringen Komplexität bei wenig Nutzen. Google rät, sie nur bei Bedarf hinzuzufügen.
- Modularisierung ist unabhängig von der Wahl. [Sie lohnt sich nicht immer, die Größe der Codebasis ist der dominierende Faktor][g-modules], und zu feine Module erzeugen Overhead. Auch [Now in Android betont, dass es keine allgemeingültige Antwort gibt][nia-modules].
- Bei DI nennt Google [Hilt oder manuelle DI für einfache Apps, Hilt ab mehreren Bildschirmen mit ViewModels][g-rec]. Das gilt für alle Optionen. Die Wahl gehört zu den Tickets 02 und 05.

**Einschätzung:** Regeln zur Abhängigkeitsrichtung sind bei einem einzigen Modul nicht compilerseitig erzwungen. [Strikte Sichtbarkeit ist ein Vorteil von Modulen][g-modules]. B und C hängen stärker an dieser Richtung als A, also trifft die offene Frage der Karte („per Konvention oder per Test?“) sie stärker.

### Eignung für Firebase

Die SDK-Fakten gelten für alle Optionen.

**Live-Updates**

- Firestore bietet [`snapshots()` und `dataObjects()` als `Flow`][fb-query]. Der Listener wird beim Sammeln angehängt und am Ende entfernt.
- Google empfiehlt [Flows für Datenänderungen über die Zeit][g-data] und zeigt [Firestore direkt als Flow-Datenquelle][g-flow]. `callbackFlow` bleibt der Standard für Listener ohne fertige Flow-Erweiterung.
- **Einschätzung:** Zusammen mit [`stateIn` und `WhileSubscribed(5000)`][g-rec] endet der Listener kurz nachdem die UI nicht mehr sammelt.
- Lokale Schreibvorgänge [lösen Listener sofort aus, erkennbar an `hasPendingWrites`][fb-listen]. Metadaten-Änderungen [kommen nur mit `MetadataChanges.INCLUDE`][fb-listen].
- Nach einem Fehler [erhält der Listener keine weiteren Ereignisse][fb-listen]. Die Referenz der Flow-Erweiterung [beschreibt keine Fehlerbehandlung][fb-query]. Das sollte beim Firestore-Adapter geprüft werden.
- Die Offline-Persistenz ist [unter Android standardmäßig aktiv][fb-offline]. Google empfiehlt für Offline-First [eine lokale Datenquelle als Source of Truth][g-data], und die Flow-Seite nennt [Room plus Firebase als mögliche Kombination][g-flow]. Ob die App Room überhaupt braucht, ist in dieser Recherche nicht geklärt.

**Anmeldung**

- `getCurrentUser` kann [auch `null` sein, wenn Auth noch nicht fertig initialisiert ist][fb-users]. Die Doku empfiehlt deshalb einen `AuthStateListener`. Ein einfaches Ja/Nein bildet den Zustand also nicht vollständig ab.
- Der Listener [läuft direkt nach der Registrierung und bei An- und Abmeldung][fb-auth-ref].
- In der [Kotlin-Referenz von `FirebaseAuth`][fb-auth-kt] steht keine Flow-Erweiterung. Vermutlich ist also ein eigener `callbackFlow` nötig. Geprüft wurde nur diese Referenzseite, nicht der SDK-Quellcode.
- Das [Firebase-Codelab][fb-codelab] stellt den Nutzerzustand als `Flow` bereit und baut abhängige Daten per `flatMapLatest` neu auf. Das passt in jede Option, sofern es eine Nutzer-Schnittstelle gibt.
- Zugriffsregeln gehören zum Teil nicht in die App: [Security Rules prüfen `request.auth`][fb-rules].

**Tests gegen Firebase**

- Die [Emulator Suite deckt Firestore und Auth ab und ist auch für CI gedacht][fb-emulator].
- Eine Android-App im Android-Emulator erreicht sie [über `10.0.2.2`, die Persistenz sollte dabei aus sein][fb-connect]. Es gibt Abweichungen zur Produktion, zum Beispiel [werden Indizes nicht geprüft und Transaktionen verhalten sich anders][fb-connect].
- **Einschätzung:** Solche Adapter-Tests dürften sowohl Android-Emulator als auch Firebase-Emulator brauchen, in allen drei Optionen gleich. Sie betreffen nur die äußerste Klasse. Je weniger Logik dort liegt, desto weniger hängt davon ab.

**Unterschiede zwischen den Optionen**

| | A | B | C |
|---|---|---|---|
| Wo liegt der Firebase-Code? | Data Source | Sekundärer Adapter | Äußerster Ring, Gateway im Adapter-Ring |
| Passung zur Doku | Direkt, Firebase ist dort als Data Source genannt | Keine Anleitung, aber keine Reibung (**Einschätzung**) | Keine Anleitung, zusätzliche Mapper bei jedem Modell (**Einschätzung**) |
| Risiko, dass Firestore-Typen durchsickern (**Einschätzung**) | Mittel, keine harte Regel | Mittel, Regel vorhanden, aber nicht erzwungen | Niedriger, wenn die Datenformat-Regel eingehalten wird |
| `kotlinx.coroutines.Flow` in der Schnittstelle | Vorgesehen | Muss der Kern zulassen | Muss der innere Ring zulassen, was eng ausgelegt gegen die Regel gehen könnte (**Einschätzung**) |

## Abwägung am Leitkriterium

Die Karte verlangt so viel Struktur, wie für Testbarkeit und Austauschbarkeit nötig ist, und nicht mehr. Das Folgende ist **Einschätzung** und keine Entscheidung.

- **Was alle liefern:** ViewModel-Tests ohne Emulator und eine austauschbare Datenquelle. Dafür reicht bereits ein Interface an der Datengrenze plus Konstruktor-Injektion.
- **Was B über A hinaus bringt:** eine klare Regel, wem die Schnittstelle gehört, und einen Ort für Fachlogik, der die Datenquelle nicht kennt. Der Mehraufwand an Typen ist gering, der an Absprachen etwas größer.
- **Was C über B hinaus bringt:** feste innere Ringe und strikte Modellgrenzen. Das zahlt sich erst aus, wenn es nennenswerte Fachlogik gibt. Bis dahin entstehen vor allem Weiterleitungen.
- **Was gegen B und C spricht:** Doku, Beispielprojekte und Firebase-Material sprechen die Sprache von A. Eigene Abbildungen muss die Gruppe selbst erklären und im PR prüfen.
- **Was gegen A spricht:** Fachlogik landet standardmäßig in der Datenschicht, also nahe an Firebase. Wer sie von der Datenquelle trennen will, muss A ergänzen.
- **A und B liegen nah beieinander:** A mit Interface an der Datengrenze und B mit einem Port unterscheiden sich im Skeleton vor allem darin, wo das Interface liegt und wie es heißt.

## Fragen für das Grilling (Ticket 03)

1. **Fachlogik:** Wie viel Logik bleibt in der App, und wie viel erledigen später Firebase und Security Rules? Davon hängt ab, ob ein eigener Kern oder Use Cases etwas tragen.
2. **Eigentum:** Soll die Datenschnittstelle der Datenschicht oder dem nutzenden Kern gehören?
3. **Ziel der Austauschbarkeit:** Geht es um Stub und Tests oder um einen realistischen Wechsel weg von Firebase?
4. **Modellgrenze:** Gilt schon im Skeleton, dass keine Firebase-Typen die Schnittstelle überqueren?
5. **Durchsetzung:** Reichen Pakete und Konvention, oder soll ein Test oder Modulschnitt die Richtung erzwingen? Die Karte führt das noch als offen.
6. **Stub:** Soll die Stub-Schnittstelle von Anfang an `Flow` liefern? **Einschätzung:** Ein einmaliger Rückgabewert würde die spätere Live-Fähigkeit im Skeleton nicht abbilden.
7. **Repository und Data Source:** Getrennt oder [zunächst zusammengelegt][g-data]? Das beeinflusst die Zahl der „Schichten“, für die je ein Test gefordert ist.

Falls die Wahl nicht auf Ports & Adapters fällt, muss laut Karte der Text des Meilensteins „1 · Setup & Walking Skeleton“ angepasst werden.

## Projektstand und Grenzen

**Projektstand (Branch `2-projekt-setup`)**

- Es gibt ein einziges Modul `app` mit Kotlin 2.2.10, AGP 9.5.0 und minSdk 25.
- `app/build.gradle.kts` enthält bisher nur `lifecycle-runtime-ktx`. DI, Coroutines-Test und Firebase fehlen noch. Die Auswahl gehört zu den Tickets 02 und 05.
- ADR 002 legt Kotlin, Compose und Material 3 fest. Keine der drei Optionen widerspricht dem.

**Grenzen dieser Recherche**

- Clean Architecture stützt sich auf Martins Artikel von 2012, nicht auf sein Buch von 2017.
- Es wurden keine Erfahrungswerte gemessen, die Aufwandszahlen sind Schätzungen. Es wurde auch kein Code gebaut.
- Der Quellcode von `make-it-so-android` `v2` und der des Firebase-SDK wurden nicht gelesen.
- Firebase-Anmeldeabläufe, die eine `Activity` oder einen `Context` brauchen, wurden nicht untersucht. Das sollte bei Meilenstein 3 geprüft werden, weil [ViewModels keinen `Context` halten sollen][g-rec].
- Die Wireframes wurden nicht ausgewertet, daher bleibt der Umfang der Fachlogik offen (siehe Frage 1).

## Quellen

Alle abgerufen am 2026-11-04.

**Android (Google)**

- [Guide to app architecture][g-arch]
- [Recommendations for Android architecture][g-rec]
- [Data layer][g-data]
- [Domain layer][g-domain]
- [Compose UI architecture][g-compose]
- [Kotlin flows on Android][g-flow]
- [Use test doubles in Android][g-doubles]
- [What to test in Android][g-what-to-test]
- [Testing Kotlin coroutines on Android][g-coroutines-test]
- [Guide to Android app modularization][g-modules]
- Now in Android: [README, Testing][nia-readme], [`TopicsRepository.kt`][nia-repo], [`core/domain/build.gradle.kts`][nia-domain], [Modularization Learning Journey][nia-modules]

**Originalartikel**

- Alistair Cockburn: [Hexagonal Architecture][cockburn]
- Robert C. Martin: [The Clean Architecture][clean] (2012)

**Firebase (Google)**

- [Get realtime updates with Cloud Firestore][fb-listen]
- [Kotlin-Referenz `Query`][fb-query]
- [Access data offline][fb-offline]
- [Manage users][fb-users]
- [`FirebaseAuth`-Referenz (Android)][fb-auth-ref] und [`FirebaseAuth`-Referenz (Kotlin)][fb-auth-kt]
- [Security Rules: Writing conditions][fb-rules]
- [Emulator Suite][fb-emulator] und [Firestore-Emulator verbinden][fb-connect]
- [Codelab: Build an Android app with Firebase and Compose][fb-codelab]
- [Blog: Using Coroutines and Flows with Firebase on Android][fb-blog]
- [make-it-so-android][fb-repo]

[g-arch]: https://developer.android.com/topic/architecture
[g-rec]: https://developer.android.com/topic/architecture/recommendations
[g-data]: https://developer.android.com/topic/architecture/data-layer
[g-domain]: https://developer.android.com/topic/architecture/domain-layer
[g-compose]: https://developer.android.com/develop/ui/compose/architecture
[g-flow]: https://developer.android.com/kotlin/flow
[g-doubles]: https://developer.android.com/training/testing/fundamentals/test-doubles
[g-what-to-test]: https://developer.android.com/training/testing/fundamentals/what-to-test
[g-coroutines-test]: https://developer.android.com/kotlin/coroutines/test
[g-modules]: https://developer.android.com/topic/modularization
[nia-readme]: https://github.com/android/nowinandroid/blob/main/README.md
[nia-repo]: https://github.com/android/nowinandroid/blob/main/core/data/src/main/kotlin/com/google/samples/apps/nowinandroid/core/data/repository/TopicsRepository.kt
[nia-domain]: https://github.com/android/nowinandroid/blob/main/core/domain/build.gradle.kts
[nia-modules]: https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md
[cockburn]: https://alistair.cockburn.us/hexagonal-architecture
[clean]: https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html
[fb-listen]: https://firebase.google.com/docs/firestore/query-data/listen
[fb-query]: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/firestore/Query
[fb-offline]: https://firebase.google.com/docs/firestore/manage-data/enable-offline
[fb-users]: https://firebase.google.com/docs/auth/android/manage-users
[fb-auth-ref]: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth
[fb-auth-kt]: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/auth/FirebaseAuth
[fb-rules]: https://firebase.google.com/docs/firestore/security/rules-conditions
[fb-emulator]: https://firebase.google.com/docs/emulator-suite
[fb-connect]: https://firebase.google.com/docs/emulator-suite/connect_firestore
[fb-codelab]: https://firebase.google.com/codelabs/build-android-app-with-firebase-compose
[fb-blog]: https://firebase.blog/posts/2022/10/using-coroutines-flows-with-firebase-on-android/
[fb-repo]: https://github.com/FirebaseExtended/make-it-so-android
