# CI und Release

Warum die CI so aufgebaut ist, steht in ADR 007. Hier steht, wie man damit arbeitet.

## Was läuft wann

| Workflow | Auslöser | Was passiert |
|---|---|---|
| `pr.yml` | jeder Pull Request | Build, Tests, Lint, ktlint. Befunde erscheinen am PR |
| `main.yml` | Push auf `main` | dieselben Prüfungen, Debug-APK als Artefakt |
| `release.yml` | Tag `v*` | dieselben Prüfungen, signiertes APK als GitHub-Release |

Die Prüfungen selbst stehen nur einmal in `checks.yml`, aufgeteilt in zwei parallele Jobs:

| Job | Prüft |
|---|---|
| **Lint** | Android Lint (Android-Regeln: Manifest, Ressourcen, API-Level, Compose) und ktlint (Kotlin-Stil) |
| **Build & Test** | Build, Unit-, Architektur- und Robolectric-Tests, Testabdeckung |

Zusammen entsprechen sie lokal `./gradlew check`: was lokal grün ist, ist auch in der CI grün.

## Mein PR ist rot

1. Im PR unter **Checks** den roten Job öffnen, im ersten Schritt nach dem Setup steht der Fehler.
2. Lint- und ktlint-Befunde stehen zusätzlich als Annotation an der Zeile unter **Files changed**.
3. Fehlgeschlagene Tests und die Testabdeckung stehen in der **Zusammenfassung** des Laufs.
4. Die vollständigen Berichte liegen als Artefakt `lint-berichte` bzw. `test-berichte` unten in der Zusammenfassung.

Häufige Fälle:

- **ktlint:** `./gradlew ktlintFormat` ausführen, das behebt fast alles automatisch.
- **`ArchitekturTest`:** Eine Schichtregel aus ADR 004 ist verletzt, die Meldung nennt die Datei.
- **Gradle-Version aktualisieren:** nie nur die URL ändern, sondern `./gradlew wrapper --gradle-version X` ausführen.
  Sonst passt die Prüfsumme nicht und jeder Build bricht ab.

## Release erstellen

```bash
git tag v0.1.0
git push origin v0.1.0
```

Die Version im APK kommt aus dem Tag. Das Release mit APK und automatischen Release Notes erscheint unter **Releases**.

## Einmalige Einrichtung auf GitHub

### Ruleset für `main`

Ohne Ruleset meldet die CI Fehler nur, sie verhindert keinen Merge.

**Settings → Rules → Rulesets → New branch ruleset**

| Einstellung | Wert | Wieso |
|---|---|---|
| Enforcement status | Active | sonst greift die Regel nicht |
| Target branches | Default branch (`main`) | schützt den Hauptstand |
| Restrict deletions | an | `main` kann nicht gelöscht werden |
| Block force pushes | an | die Historie kann nicht überschrieben werden |
| Require a pull request before merging | an, 1 Approval | jede Änderung wird von einem Menschen geprüft (ADR 008) |
| Require status checks to pass | **checks / Lint** und **checks / Build & Test** | ein roter Job blockiert den Merge |
| Require branches to be up to date | an | der getestete Stand ist genau der, der auf `main` landet |

Ein Check lässt sich erst auswählen, nachdem er einmal gelaufen ist.

### Secrets für das Release

Android installiert Updates nur, wenn sie mit demselben Schlüssel signiert sind.
Der Schlüssel liegt deshalb nur in den Secrets, nicht im Repo und nicht auf einem einzelnen Laptop.

Schlüssel einmalig erzeugen (Git Bash, Java aus Android Studio reicht):

```bash
keytool -genkeypair -keystore release.jks -alias radio-app -keyalg RSA -keysize 2048 -validity 10000
base64 -w0 release.jks > release.b64
```

**Settings → Secrets and variables → Actions → New repository secret**

| Secret | Inhalt |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | Inhalt von `release.b64` |
| `RELEASE_KEYSTORE_PASSWORD` | Passwort des Keystores |
| `RELEASE_KEY_ALIAS` | `radio-app` |
| `RELEASE_KEY_PASSWORD` | Passwort des Schlüssels |

`release.jks` und die Passwörter sicher aufbewahren, z. B. im Passwortmanager der Gruppe.
Geht der Schlüssel verloren, lassen sich installierte Apps nicht mehr aktualisieren.
