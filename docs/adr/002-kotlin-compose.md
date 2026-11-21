# Entscheidung
Wir entwickeln eine native Android-App mit Kotlin und Jetpack Compose.
Für die Oberfläche nutzen wir Material Design 3.

# Begründung
Compose ist der aktuelle Standard für Android-UIs und nur mit Kotlin nutzbar.
Material 3 ist in Compose integriert, liefert fertige Komponenten und
Barrierefreiheit mit.
Dark Mode wäre möglich, vorerst gibt es nur ein helles Schema (ADR 009).

# Alternativen
- Java mit XML-Layouts – mehr Boilerplate, UI wird imperativ aktualisiert statt aus dem Zustand beschrieben.
- Cross-Platform (Flutter, Kotlin Multiplatform), kein Mehrwert Multiplattformfähigkeit wird nicht angestrebt.