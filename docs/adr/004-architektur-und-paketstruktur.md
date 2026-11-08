# Entscheidung
Wir folgen Googles empfohlener App-Architektur mit UI- und Datenschicht.
Die UI hängt von der Datenschicht ab, nie umgekehrt. Ein Gradle-Modul `app`.

```
app/src/main/java/com/iu/radio_app/
├── MainActivity.kt
├── model/                Domänenmodell: Track, Show, SongRequest, Rating, User
├── ui/
│   ├── theme/
│   └── <feature>/        Screen, ViewModel, UiState
└── data/
    └── <topic>/          <Topic>Repository (Interface), Stub-/Firestore-Implementierung, ggf. DTOs
```

- Code ist englisch, die Fachsprache deutsch. `GLOSSARY.md` ordnet die Begriffe einander zu.
- `model/` enthält reine Kotlin-Klassen ohne Android, Firebase und Abhängigkeiten auf `data/` oder `ui/`.
  Kleine Fachlogik direkt am Modell ist erlaubt.
- Repository-Interfaces liegen in `data/<topic>/`, neben ihren Implementierungen.
- Repositories liefern `Flow` und Modelle aus `model/`, Firebase-Typen und DTOs verlassen `data/` nicht.
- Ein eigener UiState-Typ für Daten entsteht nur, wenn der Bildschirm sie aufbereitet anzeigt.
- Repository und Datenquelle bleiben zusammengelegt, bis es eine zweite Quelle gibt.
- Ein Paket `domain/` mit Use Cases entsteht erst, wenn Logik von mehreren ViewModels geteilt wird.
- Die Regeln prüft ein Konsist-Test (`ArchitekturTest`), nicht nur die Konvention.

# Begründung
Liefert mit einem Interface an der Datengrenze schon Testbarkeit (ViewModel-Tests mit Fake, ohne Emulator)
und eine austauschbare Datenquelle, mit den wenigsten Typen.
Android- und Firebase-Doku sowie Now in Android nutzen genau diesen Aufbau.

# Alternativen
- Ports & Adapters, Interface gehört dem Kern. Im Skeleton fast gleich, aber die Abbildung auf Android müssten wir selbst festlegen.
- Clean Architecture, viele Weiterleitungs-Use-Cases und Mapper ohne Fachlogik, die sie tragen.
