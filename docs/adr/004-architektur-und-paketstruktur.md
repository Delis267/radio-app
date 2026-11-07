# Entscheidung
Wir folgen Googles empfohlener App-Architektur mit UI- und Datenschicht.
Die UI hängt von der Datenschicht ab, nie umgekehrt. Ein Gradle-Modul `app`.

```
app/src/main/java/com/iu/radio_app/
├── MainActivity.kt
├── ui/
│   ├── theme/
│   └── <feature>/        Screen, ViewModel, UiState
└── data/
    └── <thema>/          <Thema>Repository (Interface), Stub-/Firestore-Implementierung
```

- Repository-Interfaces liegen in `data/<thema>/`, neben ihren Implementierungen.
- Repositories liefern `Flow` und eigene Modelle, Firebase-Typen verlassen `data/` nicht.
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
