# Entscheidung
Das Figma-Design „Radio-App“ ist die Vorlage für die Oberfläche, die Wireframes dienen nur zur Orientierung.
Design-Werte stehen zentral im Theme (`ui/theme/`), Screens und Komponenten lesen sie nur über `MaterialTheme`.

- Farben werden auf Material-3-Farbrollen abgebildet (`Theme.kt`), die Rohwerte stehen einmal in `Color.kt`.
  Screens nutzen nur Rollen (`MaterialTheme.colorScheme.primary`), nie Hex-Werte.
- Rollen werden nach Bedeutung gewählt, nicht nach Farbton. Derselbe Wert darf in mehreren Rollen stehen (z. B. `surface` und `background`).
- Hat eine Figma-Farbe keine passende Rolle, klären wir das zuerst im Design (Farbe angleichen oder einer Rolle zuordnen).
  Eine eigene Farbe außerhalb des Schemas ist die Ausnahme und wird hier ergänzt.
- Nur ein helles Farbschema, Dynamic Color ist aus. Dark Mode kommt erst, wenn es dafür ein Design in Figma gibt.
- Überschriften (Figma „FUNK/…“) in Space Grotesk Bold über `headlineLarge`, `titleLarge` und `titleMedium`, alles andere in Roboto.
  Beide Schriften liegen als TTF in `res/font/`, die Lizenzen in `docs/licenses/`.

# Begründung
Werte, die auf mehreren Screens vorkommen, stehen an einer Stelle und lassen sich zentral ändern.
Material-Komponenten wie Button, TextField und NavigationBar lesen ihre Farben selbst aus den Rollen,
eigene Farben müssten wir jeder Komponente einzeln übergeben.
Dynamic Color würde ab Android 12 die Markenfarben durch Farben aus dem Hintergrundbild ersetzen.
Ein Dark Mode ohne Design hätte ungeprüfte Kontraste und müsste manuell getestet werden.
Die Schriften liegen in der App, damit sie offline und schon beim ersten Start richtig angezeigt werden.

# Alternativen
- Eigene Farbklasse über `CompositionLocal`, freie Namen, aber Material-Komponenten lesen sie nicht automatisch.
- Hex-Werte direkt im Screen, schnell, aber jede Farbänderung betrifft viele Stellen.
- Dark Mode aus dem M3-Standard ableiten, ohne Design und ohne geprüfte Kontraste.
- Downloadable Fonts über Google Play Services, kleineres APK, aber braucht Netzwerk und Play Services.
