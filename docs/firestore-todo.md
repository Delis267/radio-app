# Firestore einrichten: Todos

Ziel: Alle im Team nutzen das gemeinsame Firebase-Projekt `radio-app-iu` für Songwünsche, Vorschläge und Bewertungen (ADR 003, ADR 011).
Senderdaten (Sendungen, Playlists, „Jetzt läuft“) bleiben im Code und kommen nicht nach Firestore.

Stand: Die Firestore-Datenbank ist angelegt, sonst ist noch nichts eingerichtet. Die App nutzt Firestore noch nicht.

## 1. Firebase Console (einmalig, Inhaber:in des Projekts)

- [ ] **Teammitglieder hinzufügen:** Projekteinstellungen → Nutzer und Berechtigungen → je Person ein Google-Konto mit Rolle **Bearbeiter**
- [ ] **Anonyme Anmeldung einschalten:** Authentication → Jetzt starten → Anmeldemethode → **Anonym** aktivieren
- [ ] **Tarif prüfen:** Projekt bleibt im kostenlosen **Spark**-Tarif
- [ ] **Datenbank prüfen:** Firestore Database → Regeln: Bis die Regeln aus Schritt 3 deployt sind, muss dort alles gesperrt sein (`allow read, write: if false`)

## 2. Code

- [ ] **Firebase Auth einbinden:** `firebase-auth` in `gradle/libs.versions.toml` und `app/build.gradle.kts`
- [ ] **Anonym anmelden beim App-Start** (nur Betriebsart Firestore), die `uid` ist die `userId` der eigenen Daten
- [ ] **`FirestoreSongRequestRepository`** für `songRequests` und `suggestions`
  - Wunsch anlegen mit `userId`, `trackId`, `requestedAt`
  - eigene offene Wünsche lesen, alle offenen Wünsche für die Moderation lesen
  - gespielte Wünsche erkennen (Titel lief laut `SimulatedStation` nach `requestedAt`) und eigene löschen
  - unbekannte `trackId` überspringen statt abstürzen
- [ ] **`FirestoreRatingRepository`** für `ratings`
  - feste Dokument-ID je Person und Ziel, z. B. `{userId}_{typ}_{zielId}`, damit eine neue Bewertung die alte ersetzt
  - Zusammenfassung und Kommentare je Ziel auf dem Gerät berechnen
- [ ] **Umschalten über `BuildConfig.USE_STUBS`** in `SongRequestModule` und `RatingModule` (Stub oder Firestore)
- [ ] **Benachrichtigungen** an `playedRequests` des gewählten Repositories hängen statt fest an `StubSongRequestRepository`
- [ ] **Unit-Tests** für alles, was ohne Firebase-Typen auskommt (ADR 006); Firestore-Teil von Hand prüfen

## 3. Sicherheitsregeln

- [ ] **Regeln in `firestore.rules` schreiben:**
  - `songRequests`, `suggestions`, `ratings`: lesen für alle Angemeldeten
  - anlegen, ändern, löschen nur, wenn `userId == request.auth.uid`
  - alles andere bleibt gesperrt
- [ ] **Regeln deployen** (im Repo-Ordner, `.firebaserc` zeigt auf `radio-app-iu`):

  ```
  npm install -g firebase-tools
  firebase login
  firebase deploy --only firestore:rules
  ```

  Alternativ den Inhalt von `firestore.rules` in der Console unter Firestore Database → Regeln einfügen.

## 4. Gemeinsam testen

- [ ] Alle pullen und bauen die App ohne `radio.stubs` (Standard ist Firestore)
- [ ] Gerät A wünscht sich einen Titel → Wunsch erscheint in der Console und in der Moderations-Ansicht auf Gerät B
- [ ] Titel läuft → Wunsch verschwindet auf beiden Geräten, Gerät A bekommt Banner bzw. Benachrichtigung
- [ ] Gerät A bewertet Elise → Bewertung und Kommentar erscheinen in der Moderations-Ansicht auf Gerät B
- [ ] Gerät B kann Wünsche oder Bewertungen von Gerät A nicht ändern (Regeln greifen)

## Offen

- [ ] Moderations-Ansicht: Zuordnung von angemeldeter E-Mail zu `hostId` (z. B. fest im Code bei den Senderdaten)
- [ ] Name und Beschreibung des zweiten Moderators statt Platzhalter „Tom Berger“
