# Änderungen

## Umbau vom 20.09.2026

Sieben Punkte des Änderungsauftrags. Paketname, Name, Akzentfarben
`#2F7D6E` / `#4FC3A1` und der Segmentring als Zeichen bleiben unverändert.

### 1. Room führt die fachlichen Daten

Kompetenzstände, Antwortverlauf, Fehlerarten, Wiederholungstermine und die
Gesamtzahlen liegen in einer Room-Datenbank (Fassung 1, `exportSchema = true`,
Schema nach `app/src/main/schemas/`). Fünf `@Entity`-Klassen, vier `@Dao`-Schnittstellen
mit `Flow`-Abfragen und ein Repository stehen unter
`app/src/main/java/de/rechenwerk/mathe/daten/raum/`.
`fallbackToDestructiveMigration` wird nicht benutzt. Beim ersten Start
übernimmt `AltdatenUebernahme` den vorhandenen JSON-Stand einmalig und
vollständig; ein Merkzeichen im Tresor verhindert eine zweite Übernahme.

### 2. Der Suite-Tresor bleibt daneben stehen

Profil, Erscheinungsbild und das Merkzeichen führt weiterhin der Tresor als
JSON-Datei unter `files/`, und jede Änderung einer Einstellung schreibt ihn
sofort. Export und Import gehen unverändert über die vollständige Ablage, so
dass eine ausgegebene Datei den ganzen Lernstand enthält.

### 3. Hilt spritzt ein

Neue Anwendungsklasse `RechenwerkAnwendung` mit `@HiltAndroidApp`, im Manifest
als `android:name` eingetragen; `MainActivity` mit `@AndroidEntryPoint`; `Werk`
als `@HiltViewModel` mit `@Inject constructor`, geholt über `hiltViewModel()`.
Datenbank, DAOs und Tresor kommen aus `daten/DatenModul.kt`.

### 4. Das Erscheinungsbild folgt ab Werk dem System

Der Vorgabewert wechselt von `Modus.DUNKEL` auf `Modus.SYSTEM`. Die Wahl
DUNKEL / HELL / SYSTEM bleibt; der helle Entwurf hat eigene Glasdeckkraft und
eigene Randfarbe, keine bloß invertierten Werte.

### 5. Die App belohnt sichtbar

- Nach einer richtigen Antwort wächst der Segmentring im Training um ein
  Segment, begleitet von einem Aufleuchten der Akzentfarbe (220 ms,
  `CubicBezierEasing(0.16, 1, 0.3, 1)`) und einer Tastrücksprache.
- Eine falsche Antwort lässt das Eingabefeld zweimal je 120 ms zur Seite
  wanken und nennt die Fehlvorstellung in einem ganzen Satz — kein Rot-Schock.
- Über dem Aufgabenfeld läuft eine schmale Serien-Strecke mit. Die laufende
  Serie steht als eigene Kennzahl im Datenmodell (`Werte.serie`).
- Am Ende einer Einheit steht ein eigenes Abschlussbild (`ui/Abschluss.kt`)
  mit hochzählenden Zahlen (400 ms) und einem Satz, was als Nächstes fällig ist.
- Serie und Abschlussbild tragen den zweiten Markenton Bernstein (`#F5A524`,
  hell tiefer gezogen), nie als Flächenfarbe unter Text.
- Die Listen auf Start- und Fortschrittsbildschirm laufen mit 60 ms Versatz je
  Eintrag und leichtem Überschwingen ein.

Alle Bewegungen laufen über Compose-Animations-APIs und folgen damit der
Animationsskala des Systems; die gestaffelte Wartezeit entfällt, wenn Bewegung
abgeschaltet ist.

### 6. Ausgefranste Reihen aufgeräumt

`Wahlreihe` ist einem festen `Wahlraster` gewichen: gleich breite Felder,
gleiche Abstände aus der 8-dp-Skala, mindestens 48 dp hohe Tippziele, Umbruch
innerhalb des Feldes. Die Spaltenzahl je Gruppe ist so gewählt, dass keine
Reihe mit einem einzelnen übrigen Feld endet — Schulart zwei Spalten, Klasse
und Niveau drei.

### 7. Tests

Neu: `app/src/test/java/de/rechenwerk/mathe/RaumTest.kt` mit acht Prüfungen zur
Altdaten-Übernahme und zur Serien-Zählung. Die bestehenden Prüfungen bleiben,
zwei Erwartungen folgen dem neuen Vorgabewert des Erscheinungsbilds. Das
JSON-Format steht in Fassung 3 (Serie und Merkzeichen); Fassung 1 und 2 werden
weiter gelesen. Im Wurzelverzeichnis liegt keine `.py`-Datei.

### Nachtrag: Titelschrift der Marke

Die Typografie-Rolle `Titelschrift` stand auf `Space Grotesk` und damit auf
keiner Fassung der Marke. `marke.json` nennt `Source Serif 4` als Titelschrift
und `Inter` als Textschrift; beide werden jetzt so angefragt.

### Nacharbeit nach Sichtprüfung der Prüf-Fotos

Vier Befunde, die keine Regel der Fabrik erwischt hat, weil sie alle am Bild
sichtbar sind und nicht an einer Messgröße.

**Die Markenschrift liegt jetzt im Paket.** Auch nach dem Nachtrag oben kam
sie nie auf dem Bildschirm an: `DeviceFontFamilyName` fragt eine Schrift ab,
die auf dem Gerät installiert sein muss, und weder „Space Grotesk" noch
„Source Serif 4" noch „Inter" ist das auf einem gewöhnlichen Android-Gerät.
Android setzte still Roboto ein. Jetzt liegt Manrope als variable Schriftdatei
unter `res/font/` und trägt Titel und Fließtext; `marke.json` nennt sie für
beide Rollen. Das Release wächst dadurch um 88 KB.

**Die neuen Texte sind übersetzt.** `serie_titel`, `serie_wert`,
`abschluss_serie`, `abschluss_segmente`, `abschluss_naechstes` und
`abschluss_naechstes_offen` standen in allen sechs Sprachdateien wörtlich auf
Deutsch — im türkischen Prüf-Foto las man „Serie" und „0 in Folge" mitten im
türkischen Text. Apostrophe sind maskiert (`l\'écran`, `un\'area`,
`d\'affilée`), sonst bricht aapt2 den Release-Bau ab.

**Die untere Leiste deckt jetzt.** Sie steht über scrollendem Inhalt und trug
die gewöhnliche Glasdeckkraft; die Kachel darunter las sich durch das Glas
hindurch mit. Sie bekommt die hervorgehobene Füllung (0,16 → 0,22).

**Zu lange Knopftexte gekürzt.** „Öğrenmeye devam et" brach mit einem
hängenden „et" um. Jetzt „Devam et", ebenso „Continuer", „Continua",
„Continuar" statt der langen Fassungen.

Fassung auf `versionCode = 2`, `versionName = "1.1"`.
