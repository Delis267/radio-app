# Entscheidung
Die App ist eine Companion-App zu einem Sender, den es nicht gibt. Wir trennen die Daten des Senders von den Daten der App:

- **Senderdaten stehen fest im Code**, im simulierten Sender unter `data/radio`:
  Katalog (`Track`), Sendungen mit Moderation (`Show`, `Host`) sowie Startzeitpunkt und Takt des Programms.
  Die App liest sie nur, so wie sie es bei einem echten Sendesystem über dessen Schnittstelle täte.
- **Den Ablauf berechnet der simulierte Sender** aus Senderdaten und Uhrzeit:
  Ab dem Startzeitpunkt läuft der Katalog im Takt im Kreis. Daraus ergeben sich „Jetzt läuft“ und die Playlist.
  Beides wird nirgends gespeichert, alle Geräte mit demselben Stand berechnen ohne Absprache dasselbe.
- Die Reihenfolge legt allein der Sender fest. Wünsche ändern sie nicht.
- **Firestore enthält nur Daten der App** (ADR 003): `songRequests`, `suggestions` und `ratings`.
  Nur Hörer:innen schreiben, und zwar nur eigene Dokumente. Die Moderation liest nur.
- Beide Seiten sind nur über IDs der Senderdaten verknüpft (`trackId`, `showId`, `hostId`).
  Diese IDs ändern sich nie, neue Einträge sind jederzeit möglich. Eine wiederkehrende Sendung bekommt je Ausgabe eine eigene `showId`.
  Daten der App mit einer unbekannten ID werden übersprungen statt die App abstürzen zu lassen.
- Ob ein Wunsch gespielt wurde, wird nicht gespeichert, sondern abgeleitet:
  Er gilt als gespielt, wenn sein Titel laut berechneter Playlist nach dem Zeitpunkt des Wunschs lief.
- Gespielte Wünsche werden aus Firestore gelöscht. Das übernimmt die App der Person, die sich den Titel gewünscht hat,
  sobald sie das Spielen bemerkt. Kein Client darf fremde Wünsche löschen.
  Bis dahin filtern alle Ansichten gespielte Wünsche selbst heraus, z. B. die Ansicht der Moderation,
  falls die App der wünschenden Person gerade nicht läuft.

# Begründung
Die App wird nie veröffentlicht, alle im Team bauen sie aus Git.
Eine Änderung an den Senderdaten ist damit nur ein Commit und erreicht nach dem nächsten Pull alle.
In der App ändert niemand Senderdaten, auch die Moderation nicht. Deshalb müssen sie nicht in einer geteilten Datenbank liegen.
So nutzen wir Firestore nur für Daten, die Geräte untereinander teilen, und brauchen weder Seed-Skript noch Service-Account-Schlüssel.
Der simulierte Sender ist in beiden Betriebsarten (ADR 003) derselbe, nur die Daten der App kommen aus Stubs oder Firestore.
Gäbe es den Sender doch, würde nur der simulierte Sender durch einen Zugriff auf dessen Schnittstelle ersetzt.
Weil niemand den Ablauf schreibt, brauchen wir weder Cloud Functions, die es nur im kostenpflichtigen Blaze-Tarif gibt,
noch ein Skript, das neben der App laufen muss.
Durch das Löschen bleiben in Firestore nur offene Wünsche, und eine Meldung „Wunsch gespielt“ kommt höchstens einmal.
Der berechnete Ablauf ist in Tests mit fester Uhr vorhersagbar (ADR 006).

# Alternativen
- Senderdaten in Firestore unter `stations/{stationId}`, für Clients nur lesbar.
  Änderungen ohne neuen Build, aber dafür Seed-Skript oder Pflege über die Console, und ohne Veröffentlichung bringt das keinen Vorteil.
- Senderdaten in einer eigenen Firestore-Datenbank im selben Projekt.
  Wie oben, zusätzlich zwei Datenbanken in App, Regeln und Console, und das kostenlose Kontingent gilt nur für die Standard-Datenbank.
- Auch den Ablauf in Firestore speichern (`station/live`, Playlist), geschrieben von
  - einer Cloud Function per Zeitplan: braucht den Blaze-Tarif.
  - einem Simulator-Skript mit Admin SDK: Das Radio läuft nur, solange jemand das Skript startet.
  - der App, eine Transaktion sorgt dafür, dass nur ein Gerät weiterschaltet:
    Läuft nur, solange irgendeine App offen ist, und die Regeln müssten Clients die Senderdaten schreiben lassen.
- Simulator mit eigenem Takt ab App-Start (wie bisher `StubRadio`).
  Einfach, aber jedes Gerät zeigt einen anderen Titel, und ob ein Wunsch gespielt wurde, hinge vom Gerät ab.
