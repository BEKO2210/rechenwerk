# Rechenwerk

Mathematik üben für die Klassen 5 bis 10. Ohne Konto, ohne Werbung, ohne
Netzzugriff. Alle Daten bleiben auf dem Gerät.

Die App richtet sich an Jugendliche, nicht an Grundschulkinder: ruhige
Typografie, dunkles Glas als gestalterischer Ausgangspunkt, keine Maskottchen,
keine Comicoptik. Belohnt wird trotzdem sichtbar — aber im Ton der Marke: ein
Segmentring, der um ein Segment wächst, eine Serien-Strecke und ein
Abschlussbild, kein Punktestand und keine Trophäen.

---

## Was die App kann

**Kompetenzgraph statt Aufgabenliste.** Jede der 26 Kompetenzen kennt ihre
Voraussetzungen. Wer die schriftliche Division üben will, bekommt sie erst
angeboten, wenn die Multiplikation begonnen wurde. Der Graph steuert die
Auswahl der nächsten Aufgabe und ist in der Detailansicht sichtbar. Er reicht
von den Grundrechenarten bis Klasse 10: quadratische Gleichungen, Satzgruppe
des Pythagoras, Strahlensätze, Wurzelterme, Potenzen mit rationalen
Exponenten, lineare Gleichungssysteme, Zinsrechnung über mehrere Schritte
sowie Flächen und Körper.

**Aufgaben werden erzeugt, nicht abgespult.** 26 Generatoren bauen jede
Aufgabe neu aus einem Startwert. Es gibt keinen festen Aufgabenpool, der
sich nach zwei Wochen wiederholt. Der Startwert wird mitgespeichert:
dieselbe Kompetenz, dasselbe Niveau und derselbe Startwert ergeben jederzeit
wieder Zeichen für Zeichen dieselbe Aufgabe, auch nach einem Neustart.

**Drei getrennte Niveaustufen.** Jede Kompetenz legt fest, ab welchem Niveau
sie überhaupt vorkommt, und jeder Generator rechnet je Stufe anders schwer:
G mit kleineren Zahlen und weniger Zwischenschritten, E mit größeren Zahlen,
Brüchen, negativen Werten und mehrschrittigen Aufgaben.

**Exakte Bruchrechnung.** Brüche rechnen durchgehend mit Ganzzahlen und
kürzen über den größten gemeinsamen Teiler. Es gibt keine Fließkommazahlen in
der Rechenlogik, also auch keine Rundungsfehler wie 0,30000000000000004.

**Fehlerarten statt richtig und falsch.** 50 benannte Fehlvorstellungen sind
hinterlegt, etwa "Übertrag vergessen", "kreuzweise multipliziert" oder
"Vorzeichenregel verwechselt". Die App leitet die typische Falschantwort
algorithmisch aus der jeweiligen Fehlstrategie ab und erkennt so, welchen
Denkfehler jemand gemacht hat. Die Häufigkeiten werden je Kompetenz
gespeichert.

**Verteiltes Wiederholen.** Die Abstände wachsen nach Erfolg über die Stufen
1, 3, 7, 16, 35 und 75 Tage. Ein Fehler wirft um zwei Stufen zurück.
Fälligkeiten werden gespeichert und steuern die Auswahl.

**Gestufte Hinweise.** Vier Stufen bis zum vollständigen Lösungsweg. Wer die
letzte Stufe aufdeckt, bekommt die Aufgabe nicht als Treffer gutgeschrieben.

**Vier Trainingsmodi.** Fünf Minuten mit Zeitlimit, Weiterlernen entlang des
Graphen, gezieltes Üben einer Kompetenz und ein Modus für die eigenen
Schwächen.

**Deutsche Schreibweise.** Komma als Dezimaltrennzeichen, eigene
Bildschirmtastatur mit Komma statt Punkt.

**Belohnung im Ton der Marke.** Nach einer richtigen Antwort wächst der
Segmentring im Training um ein Segment, begleitet von einem kurzen Aufleuchten
der Akzentfarbe (220 ms) und einer Tastrücksprache. Eine falsche Antwort gibt
keinen Rot-Schock, sondern lässt das Eingabefeld zweimal je 120 ms zur Seite
wanken und nennt die erkannte Fehlvorstellung in einem ganzen Satz. Über dem
Aufgabenfeld läuft eine schmale Serien-Strecke mit, die bei jedem Treffer
weiterwächst und bei einem Fehler sichtbar zurückfällt. Am Ende einer Einheit
steht ein eigenes Abschlussbild mit hochzählenden Zahlen und einem Satz, was
als Nächstes fällig ist. Serie und Abschlussbild tragen den zweiten,
wärmeren Markenton Bernstein (`#F5A524`, im hellen Modus tiefer gezogen).

**Glas und Tiefe.** Das Erscheinungsbild folgt ab Werk dem System; dunkel und
hell stehen gleichwertig daneben und lassen sich in den Einstellungen wählen.
Der dunkle Grund ist ein vertikaler Verlauf von `#0E1A17` nach `#09130F`,
dahinter wandern drei weich auslaufende Lichtblasen aus den beiden Akzenttönen
langsam und gegenläufig. Karten, obere Leiste und untere Navigationsleiste sind
Glasflächen mit Haarrand und Lichtverlauf an der Oberkante; ab Android 12 mit
echtem Weichzeichner, darunter mit einer zweiten halbtransparenten Lage. Diese
Fallunterscheidung steht an genau einer Stelle, in `ui/Glas.kt`.

**Die Schrift liegt im Paket.** Manrope, als variable Schriftdatei mit allen
Gewichten in einer Datei (165 KB, im Release 88 KB). Titel und Fließtext
kommen daraus. Vorher wurde die Markenschrift über `DeviceFontFamilyName`
angefragt, also als Schrift, die auf dem Gerät installiert sein muss — auf
so gut wie keinem Android-Gerät ist sie das, und Android setzte still Roboto
ein. Die Typografie der Marke stand damit nur in `marke.json`, nie auf dem
Bildschirm.

**Gleiche Felder statt ausgefranster Reihen.** Die Wahlknöpfe für Schulart,
Klasse und Niveau liegen in einem festen Raster mit gleich breiten Feldern und
gleichen Abständen. Eine zu lange Beschriftung bricht innerhalb ihres Feldes
um, statt das Feld zu verbreitern; keine Gruppe endet mit einem einzelnen
übrigen Feld am Rand.

**Eigenschaftsbasierte Tests.** Neun Prüfungen lassen jeden der 26 Generatoren
über 1000 verschiedene Startwerte und alle drei Niveaus laufen. Geprüft wird
nicht eine erwartete Aufgabe, sondern was für jede gelten muss: die
Gegenrechnung geht auf, kein Nenner ist null, jeder Bruch ist vollständig
gekürzt, keine angebotene Falschantwort ist die richtige Lösung, und derselbe
Startwert liefert zweimal exakt dasselbe.

---

## Technik

Kotlin mit Jetpack Compose und Material 3. Die Oberfläche ist vollständig
deklarativ, der Zustand liegt in einem Ansichtsmodell.

**Zwei Speicher nebeneinander.** Die fachlichen Daten — Kompetenzstände,
Antwortverlauf, Fehlerarten, Wiederholungstermine und die Gesamtzahlen — führt
eine Room-Datenbank in Fassung 1 unter `databases/`. Sie exportiert ihr Schema
nach `app/src/main/schemas/`, damit spätere Fassungen dagegen wandern können;
`fallbackToDestructiveMigration` kommt nicht vor. Daneben bleibt der Tresor der
Suite zuständig für Profil, Erscheinungsbild sowie Export und Import: eine
JSON-Datei im privaten Verzeichnis, atomar über eine temporäre Datei mit
anschließendem Umbenennen geschrieben, sodass ein Absturz mitten im Schreiben
keinen halben Datenbestand hinterlässt. Jede Änderung einer Einstellung
schreibt ihn sofort. Geschrieben wird außerhalb des Hauptstrangs.

**Einmalige Übernahme.** Beim ersten Start mit Room liest
`daten/raum/AltdatenUebernahme.kt` den vorhandenen JSON-Stand und schreibt ihn
vollständig in die Tabellen; danach ist die Datenbank die Wahrheit. Ein
Merkzeichen im Tresor (`nachRaumUebernommen`) verhindert eine zweite Übernahme.
Niemand verliert dabei Fortschritt — ein Unit-Test deckt die Umrechnung ab.

**Hilt.** Datenbank, DAOs, Ablage und Tresor kommen aus
`daten/DatenModul.kt`; das Ansichtsmodell trägt `@HiltViewModel` und wird über
`hiltViewModel()` geholt. Im Code steht keine manuelle Konstruktion mehr.

Es gibt keine einzige Berechtigung im Manifest und keinen Netzzugriff.

---

## Bauen

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Vorausgesetzt werden das Java Development Kit 21 und das Android SDK 36. Die
Signaturschlüssel liegen nicht im Repository.

---

## Herkunft

Diese App wurde von der App-Fabrik gebaut, einem Fließband aus neun
Stationen, das aus einer Beschreibung in ganzen Sätzen ein signiertes
Release erzeugt. Jede Qualitätsregel hinterlässt einen Nachweis mit Werkzeug,
Version, Befehl und Ergebnis.

Geprüft wurden unter anderem Bedienbarkeit mit Vorlesefunktion, Farbkontrast,
Symmetrie des Layouts, Verhalten unter zufälliger Bedienung, Startzeit,
Paketgröße sowie am Release sieben Sicherheitsregeln zu Datenfluss,
Abhängigkeiten, Netzwerk, Berechtigungen, Geheimnissen und Trackern.
