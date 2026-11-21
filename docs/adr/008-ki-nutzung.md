# Entscheidung
Wir nutzen Claude Code für Recherche, Entwürfe und Code.
Jeder Commit und jeder Push wird von einem Menschen gemacht, nie von der KI.

- Wer committet, hat die Änderung gelesen und verantwortet sie.
- `.claude/settings.json` ist eingecheckt und für alle gleich. Ein Hook blockiert dort alle Git-Befehle, die Commits anlegen oder veröffentlichen.
- Die KI legt kein Verhalten der Oberfläche selbst fest. Ist offen, wie die App auf eine Aktion reagiert (z. B. erneute Bewertung, Rückmeldung nach einem Klick, Leer- oder Fehlerzustand), fragt sie nach, bevor sie Code schreibt.

# Begründung
Die KI beschleunigt die Arbeit, die Verantwortung für den Code bleibt bei der Gruppe.
Ein menschlicher Commit stellt sicher, dass jede Änderung mindestens einmal bewusst geprüft wurde.
Die Sperre in den Einstellungen macht die Regel für alle verbindlich, statt sie nur zu vereinbaren.
Wie sich die App verhält, ist eine Designentscheidung der Gruppe. Figma zeigt meist nur das Aussehen, nicht das Verhalten. Füllt die KI solche Lücken selbst, fallen sie im Review kaum auf.

# Alternativen
- KI committet selbst, schneller, aber Änderungen landen ungeprüft im Verlauf.
- Keine KI, keine Fragen zur Verantwortung, aber deutlich langsamer 
- Agentische KI wird in Unternehmen verwendet, es wäre unrealistisch Sie nicht zu verwenden.
- KI wählt übliches Verhalten selbst, schneller, aber Entscheidungen fallen unbemerkt und unterscheiden sich je nach Sitzung.
