# Entscheidung
Wir nutzen Firebase (Firestore, später Auth) als Backend.
Firebase bleibt austauschbar und die App ist ohne Firebase-Projekt lokal lauffähig und testbar:

- Firebase-Code liegt nur in Repository-Implementierungen unter `data/` (ADR 004).
- Zu jedem Repository gibt es eine Stub-Implementierung ohne Netzwerk.
- Welche Implementierung genutzt wird, legt ein Hilt-Modul fest (ADR 005).
- Firestore-Implementierungen werden lokal gegen die Firebase Emulator Suite getestet, nie gegen das Produktivprojekt (ADR 006).

# Begründung
Firebase erspart uns einen eigenen Server samt Betrieb und liefert Live-Updates,
die „Jetzt läuft“ und „Zuletzt gespielt“ brauchen.
Der kostenlose Tarif reicht für ein Uni-Projekt.
Mit Stub und Emulator kann jede:r ohne Zugangsdaten entwickeln und testen.

# Alternativen
- Eigenes Backend, volle Kontrolle, aber Server, Hosting und Betrieb sind zusätzlicher Aufwand.
- Nur lokale Daten (Room), keine geteilten Daten zwischen Hörer:innen und Sender möglich.
