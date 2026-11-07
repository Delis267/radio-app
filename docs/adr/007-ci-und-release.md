# Entscheidung
GitHub Actions baut und prüft jeden PR. Ein PR darf erst gemergt werden, wenn alle Prüfungen grün sind.

| Workflow | Auslöser | Ergebnis |
|---|---|---|
| PR | jeder Pull Request | Build, Tests, Lint, ktlint. Befunde als Annotationen, Testergebnisse und Testabdeckung in der Zusammenfassung |
| Main | Push auf `main` | dieselben Prüfungen, Debug-APK als Artefakt |
| Release | Tag `vX.Y.Z` | dieselben Prüfungen, signiertes APK als GitHub-Release |

- Die Prüfungen liegen einmal in `checks.yml`, die drei Workflows rufen sie auf.
- `versionName` kommt aus dem Tag, `versionCode` aus der Laufnummer.
- Der Release-Keystore liegt nur in den Repository-Secrets.

# Begründung
Fehler sollen vor dem Merge auffallen, nicht danach auf `main`.
Eine gute CI und Testinfra ermöglicht uns eine effiziente asynchrone Teamarbeit.
Annotationen und Zusammenfassung zeigen Befunde am Check, ohne Logs zu lesen und ohne den PR mit Kommentaren zu füllen.
Ein Release per Tag liefert der Gruppe und den Prüfenden ein installierbares APK ohne Handarbeit.

# Alternativen
- -