# Entscheidung
Das Repository-Interface ist die einzige Naht für Fakes. Getestet wird von beiden Seiten bis zu ihr.

| Testart | Umfang | Fake | Ort |
|---|---|---|---|
| UI-Seite | Composable und ViewModel bis zum Interface | Fake-Repository | `src/test` (Robolectric für Composables) |
| Datenseite | Interface bis zur Stub-Implementierung | keiner | `src/test` |
| Durchstich | wenige Tests durch alle Schichten mit echtem Hilt-Graphen | keiner | `src/test` (Robolectric) |

- Fakes liegen in `src/test/java/com/iu/radio_app/data/<thema>/Fake<Thema>Repository.kt`.
- Unterhalb der UI-Seite und innerhalb der Datenseite wird nichts gefaked.
- Fake und Stub bestehen denselben Vertragstest, damit beide dasselbe Verhalten abbilden.
- Firestore-Implementierungen werden nicht automatisch getestet, sondern von Hand im gemeinsamen Firebase-Projekt geprüft (ADR 003).
  Logik ohne Firebase-Typen, z. B. die Berechnung des Senderablaufs (ADR 011), liegt außerhalb davon und wird auf der JVM getestet.
- Alle automatischen Tests laufen auf der JVM und damit in `./gradlew check`.
- `src/androidTest` enthält nur Tests, die ein Gerät brauchen, und läuft nur lokal.

# Begründung
Die meisten Tests laufen schnell auf der JVM. Der Vertragstest stellt sicher, dass beide Seiten
an der Naht zusammenpassen, die Durchstiche prüfen die Verdrahtung.
Zusammen decken die drei Testarten alle Schichten ab, ohne Emulator in CI.
Robolectric deckt alles ab, was die App braucht, nur System-UI wie Edge-to-Edge nicht.
Automatische Firestore-Tests bräuchten den Firebase-Emulator, den wir nicht nutzen (ADR 003).
Gegen das gemeinsame Projekt würden sie die Daten des Teams verändern.
Weil Firestore-Implementierungen nur Daten übersetzen, reicht für ein Uni-Projekt die Prüfung von Hand.

# Alternativen
- Fakes oder Mocks auf jeder Schicht, mehr Doubles, die vom echten Verhalten abweichen können.
- Überwiegend Tests durch alle Schichten, langsam, brauchen Emulator und zeigen schlecht, wo ein Fehler liegt.
- Firestore-Tests gegen den Firebase-Emulator, sicherer, aber Firebase CLI und Node für alle, und in CI hoher Einrichtungsaufwand.
- Gerätetests auf Emulator in CI, langsam und wackelig, bringen gegenüber Robolectric kaum mehr.
