# Rechenwerk

Mathematik üben für die Klassen 5 bis 10. Ohne Konto, ohne Werbung, ohne
Netzzugriff. Alle Daten bleiben auf dem Gerät.

Die App richtet sich an Jugendliche, nicht an Grundschulkinder: ruhige
Typografie, dunkle Oberfläche als Standard, keine Maskottchen, keine Punkte-
und Belohnungsmechanik.

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

**Glas und Tiefe.** Dunkel ist der Standard einer neuen Installation, nicht
"wie das System". Der Grund ist ein vertikaler Verlauf von `#0E1A17` nach
`#060B0A`, dahinter wandern drei weich auslaufende Lichtblasen aus den beiden
Akzenttönen langsam und gegenläufig. Karten, obere Leiste und untere
Navigationsleiste sind Glasflächen mit Haarrand und Lichtverlauf an der
Oberkante; ab Android 12 mit echtem Weichzeichner, darunter mit einer zweiten
halbtransparenten Lage. Diese Fallunterscheidung steht an genau einer Stelle,
in `ui/Glas.kt`. Der helle Modus ist gleichwertig gepflegt.

**Eigenschaftsbasierte Tests.** Neun Prüfungen lassen jeden der 26 Generatoren
über 1000 verschiedene Startwerte und alle drei Niveaus laufen. Geprüft wird
nicht eine erwartete Aufgabe, sondern was für jede gelten muss: die
Gegenrechnung geht auf, kein Nenner ist null, jeder Bruch ist vollständig
gekürzt, keine angebotene Falschantwort ist die richtige Lösung, und derselbe
Startwert liefert zweimal exakt dasselbe.

---

## Was noch fehlt

Dieser Punkt des Auftrags ist nicht umgesetzt. Er steht hier, damit niemand
ihn beim Lesen des Codes sucht.

- **Room und Hilt werden nicht verwendet.** Statt einer SQLite-Datenbank
  speichert die App eine JSON-Datei, statt eines Einspritzrahmens genügt der
  Standardkonstruktor des Ansichtsmodells. Beides braucht Einträge in
  `app/build.gradle.kts` (KSP-Plugin, `androidx.room:*`,
  `com.google.dagger:hilt-android`) und eine neu erzeugte
  `app/gradle.lockfile`. Beide Dateien liegen außerhalb dessen, was dieser
  Arbeitsschritt schreiben darf. Was genau einzutragen ist, steht in
  [docs/ROOM_HILT_NACHTRAG.md](docs/ROOM_HILT_NACHTRAG.md).

---

## Technik

Kotlin mit Jetpack Compose und Material 3. Die Oberfläche ist vollständig
deklarativ, der Zustand liegt in einem Ansichtsmodell.

Gespeichert wird über eine kleine gemeinsame Bibliothek der Suite. Sie legt
eine JSON-Datei im privaten Verzeichnis der App ab und schreibt atomar über
eine temporäre Datei mit anschließendem Umbenennen, sodass ein Absturz
mitten im Schreiben keinen halben Datenbestand hinterlässt. Geschrieben wird
außerhalb des Hauptstrangs.

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
