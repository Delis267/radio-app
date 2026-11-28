# Entscheidung
Wir nutzen Firebase (Firestore und Anonymous Auth) als Backend. Die App läuft in einer von zwei Betriebsarten:

- **Firestore** (Standard): Alle im Team nutzen dasselbe Firebase-Projekt und teilen dessen Daten.
  Die App wird nie veröffentlicht, das Projekt ist unser gemeinsames Testsystem.
- **Stubs** (`-Pradio.stubs=true`): Stub-Implementierungen ohne Netzwerk, Daten nur im Speicher.

Dabei gilt:
- Firebase-Code liegt nur in Repository-Implementierungen unter `data/` (ADR 004).
- Zu jedem Repository gibt es eine Stub-Implementierung ohne Netzwerk.
- Welche Implementierung genutzt wird, legt ein Hilt-Modul fest (ADR 005).
- Jedes Gerät meldet sich anonym an. Die Sicherheitsregeln erlauben nur, eigene Daten zu ändern.
- Wir nutzen keinen Firebase-Emulator.
- Service-Account-Schlüssel kommen nie ins Repository.

# Begründung
Ein Backend ist nicht gefordert, wir bauen es als Zusatz.
Firebase erspart uns einen eigenen Server samt Betrieb und liefert Live-Updates,
die z. B. offene Songwünsche in der Ansicht der Moderation brauchen.
Senderdaten wie Katalog, Sendungen, „Jetzt läuft“ und Playlist liegen nicht in Firebase, sie kommen vom simulierten Sender in der App (ADR 011).
Mit einem gemeinsamen Projekt können wir auf verschiedenen Geräten testen,
z. B. eine Moderation auf einem Gerät bewerten und die Bewertung auf einem anderen sehen.
Außer der `google-services.json` im Repository braucht dafür niemand eine Einrichtung.
Weil Zugangsdaten zur App im Repository liegen, schützen Anonymous Auth und Sicherheitsregeln die Daten der anderen.
Der kostenlose Tarif reicht für ein Uni-Projekt.
Mit den Stubs kann jede:r auch ohne Netzwerk an der UI arbeiten, und Unit-Tests laufen ohne Firebase.

# Alternativen
- Firebase-Emulator statt eines gemeinsamen Projekts. Keine Gefahr für gemeinsame Daten,
  aber jede:r hat eigene Daten auf dem eigenen Rechner, und es braucht Firebase CLI und Node.
- Nur Stubs, keine Einrichtung, aber keine geteilten Daten und kein dauerhafter Speicher.
- Eigenes Backend, volle Kontrolle, aber Server, Hosting und Betrieb sind zusätzlicher Aufwand.
- Nur lokale Daten (Room), keine geteilten Daten zwischen Hörer:innen und Moderation möglich.
