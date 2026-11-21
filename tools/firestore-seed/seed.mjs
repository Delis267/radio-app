// Lädt Testdaten in den Firestore-Emulator, nie in das echte Projekt (ADR 003).
// Daten wie in StubProgramRepository und StubRadio, damit Stub- und Firestore-Modus gleich aussehen.
// Aufbau siehe KDoc von FirestoreProgramRepository.
//
//   firebase emulators:start --only firestore     (Terminal 1, Repo-Root)
//   npm install && npm run seed                    (Terminal 2, tools/firestore-seed)
//
// Mehrfach ausführbar: überschreibt die eigenen Dokumente, Playlist wird vorher geleert.

import { initializeApp } from "firebase-admin/app";
import { getFirestore, Timestamp } from "firebase-admin/firestore";

// Ohne diese Variable würde das Admin SDK das echte Projekt ansprechen
process.env.FIRESTORE_EMULATOR_HOST ??= "127.0.0.1:8080";
// Dieselbe Projekt-ID wie die App (.firebaserc), damit sie im Emulator dieselben Daten sieht
const PROJECT_ID = process.env.FIREBASE_PROJECT_ID ?? "radio-app-iu";

const db = getFirestore(initializeApp({ projectId: PROJECT_ID }));

const CATALOG = [
  ["bohemian-rhapsody", "Bohemian Rhapsody", "Queen", "A Night at the Opera", 1975],
  ["dont-stop-me-now", "Don't Stop Me Now", "Queen", "Jazz", 1978],
  ["billie-jean", "Billie Jean", "Michael Jackson", "Thriller", 1982],
  ["99-luftballons", "99 Luftballons", "Nena", "Nena", 1983],
  ["major-tom", "Major Tom (Völlig losgelöst)", "Peter Schilling", "Fehler im System", 1983],
  ["rolling-in-the-deep", "Rolling in the Deep", "Adele", "21", 2010],
  ["atemlos", "Atemlos durch die Nacht", "Helene Fischer", "Farbenspiel", 2013],
  ["auf-uns", "Auf uns", "Andreas Bourani", "Hey", 2014],
  ["blinding-lights", "Blinding Lights", "The Weeknd", "After Hours", 2019],
  ["bad-guy", "bad guy", "Billie Eilish", "When We All Fall Asleep, Where Do We Go?", 2019],
].map(([id, title, artist, album, releaseYear]) => ({ id, title, artist, album, releaseYear, coverUrl: null }));

const HOST = {
  id: "elise-mueller",
  name: "Elise Müller",
  description:
    "Elise ist schon wach, wenn du noch den Snooze-Knopf suchst. Sie bringt dir Wetter, Stau und " +
    "Ohrwürmer. Gut gelaunt wie sonst niemand vor 7 Uhr. Kaffee hilft, aber Elise hilft mehr.",
  photoUrl: null,
};

// Sendung läuft gerade: begann vor einer Stunde, endet in zwei
const now = Date.now();
const HOUR = 60 * 60 * 1000;
const show = {
  id: "vormittag-mit-elise",
  title: "Vormittag mit Elise",
  host: HOST,
  startsAt: Timestamp.fromMillis(now - HOUR),
  endsAt: Timestamp.fromMillis(now + 2 * HOUR),
};

// Die ersten fünf Titel liefen im Abstand von vier Minuten, der letzte läuft noch
const played = CATALOG.slice(0, 5).map((track, i) => ({
  startedAt: Timestamp.fromMillis(now - (5 - i) * 4 * 60 * 1000),
  track,
}));

const { id: showId, ...showData } = show;
const showRef = db.collection("shows").doc(showId);
const playlist = showRef.collection("playlist");

const old = await playlist.get();
const batch = db.batch();
old.docs.forEach((doc) => batch.delete(doc.ref));
batch.set(showRef, showData);
played.forEach((entry, i) => batch.set(playlist.doc(String(i + 1).padStart(3, "0")), entry));
batch.set(db.collection("station").doc("live"), { showId, nowPlaying: played.at(-1) });
await batch.commit();

console.log(`Emulator ${process.env.FIRESTORE_EMULATOR_HOST}, Projekt ${PROJECT_ID}:`);
console.log(`  shows/${showId} mit ${played.length} Titeln in der Playlist`);
console.log(`  station/live → läuft: ${played.at(-1).track.artist} – ${played.at(-1).track.title}`);
