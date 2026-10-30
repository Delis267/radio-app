# Entscheidung
Wir entwickeln eine native Android-App mit Kotlin und Jetpack Compose.
Für die Oberfläche nutzen wir Material Design 3.

# Begründung
Wir haben keine Apple-Entwicklungsumgebung ohne Macs.
Compose ist der aktuelle Standard für Android-UIs und nur mit Kotlin nutzbar.
Material 3 ist in Compose integriert, liefert fertige Komponenten, Dark Mode und
Barrierefreiheit mit, sodass wir kein eigenes Design bauen müssen.

# Alternativen
- Java mit XML-Layouts – mehr Boilerplate, UI wird imperativ aktualisiert statt aus dem Zustand beschrieben.
- Cross-Platform (Flutter, Kotlin Multiplatform), kein Mehrwert Multiplattformfähigkeit wird nicht angestrebt.