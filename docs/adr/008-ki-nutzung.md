# Entscheidung
Wir nutzen Claude Code für Recherche, Entwürfe und Code.
Jeder Commit und jeder Push wird von einem Menschen gemacht, nie von der KI.

- Wer committet, hat die Änderung gelesen und verantwortet sie.
- `.claude/settings.json` ist eingecheckt. Ein Hook blockiert dort alle Git-Befehle, die Commits anlegen oder veröffentlichen.
- Die CI lässt keinen PR mit Commits durch, die als KI-Commit gekennzeichnet sind (`Co-Authored-By: Claude`).
- Persönliche Einstellungen (`.claude/settings.local.json`) bleiben lokal.

# Begründung
Die KI beschleunigt die Arbeit, die Verantwortung für den Code bleibt bei der Gruppe.
Ein menschlicher Commit stellt sicher, dass jede Änderung mindestens einmal bewusst geprüft wurde.
Die Sperre in den Einstellungen macht die Regel für alle verbindlich, statt sie nur zu vereinbaren.

# Alternativen
- KI committet selbst, schneller, aber Änderungen landen ungeprüft im Verlauf.
- Keine KI, keine Fragen zur Verantwortung, aber deutlich langsamer 
- Agentische KI wird in Unternehmen verwendet, es wäre unrealistisch Sie nicht zu verwenden.
