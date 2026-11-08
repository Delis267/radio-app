## Build and test

Requires JDK 25 (`JAVA_HOME`, Android Studio's bundled JBR works).

- `./gradlew check`: unit tests (including `ArchitekturTest`, which enforces the layer rules from the ADRs) and Android Lint. Run it before calling work done.

## Agent skills

### Domain docs

Single-context: one `docs/adr/` at the repo root.
