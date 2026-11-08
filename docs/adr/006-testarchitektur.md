# Entscheidung
Das Repository-Interface ist die einzige Naht für Fakes. Getestet wird von beiden Seiten bis zu ihr.

| Testart | Umfang | Fake | Ort |
|---|---|---|---|
| UI-Seite | Composable und ViewModel bis zum Interface | Fake-Repository | `src/test` (Robolectric für Composables) |
| Datenseite | Interface bis zur echten Implementierung | keiner (Firestore gegen Firebase-Emulator) | `src/test`, Firestore in `src/androidTest` |
| Durchstich | wenige Tests durch alle Schichten mit echtem Hilt-Graphen | keiner | `src/test` (Robolectric) |

- Fakes liegen in `src/test/java/com/iu/radio_app/data/<thema>/Fake<Thema>Repository.kt`.
- Unterhalb der UI-Seite und innerhalb der Datenseite wird nichts gefaked.
- Fake und echte Implementierung bestehen denselben Vertragstest, damit der Fake das echte Verhalten abbildet.
- Alles außer Firestore läuft auf der JVM und damit in `./gradlew check`.
- `src/androidTest` enthält nur Tests, die ein Gerät brauchen, und läuft nur lokal.

# Begründung
Die meisten Tests laufen schnell auf der JVM. Der Vertragstest stellt sicher, dass beide Seiten
an der Naht zusammenpassen, die Durchstiche prüfen die Verdrahtung.
Zusammen decken die drei Testarten alle Schichten ab, ohne Emulator in CI.
Robolectric deckt alles ab, was die App braucht, nur System-UI wie Edge-to-Edge nicht.
Firestore-Tests in CI bräuchten Android- und Firebase-Emulator zugleich, der Aufwand lohnt sich für ein Uni-Projekt nicht.

# Alternativen
- Fakes oder Mocks auf jeder Schicht, mehr Doubles, die vom echten Verhalten abweichen können.
- Überwiegend Tests durch alle Schichten, langsam, brauchen Emulator und zeigen schlecht, wo ein Fehler liegt.
- Firestore-Tests in CI, sicherer, aber hoher Einrichtungsaufwand.
- Gerätetests auf Emulator in CI, langsam und wackelig, bringen gegenüber Robolectric kaum mehr.
