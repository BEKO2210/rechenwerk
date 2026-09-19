# Rechenwerk

Mathematik üben für die Klassen 5 bis 10. Ohne Konto, ohne Werbung, ohne
Netzzugriff. Alle Daten bleiben auf dem Gerät.

Die App richtet sich an Jugendliche, nicht an Grundschulkinder: ruhige
Typografie, dunkle Oberfläche als Standard, keine Maskottchen, keine Punkte-
und Belohnungsmechanik.

---

## Was die App kann

**Kompetenzgraph statt Aufgabenliste.** Jede der 18 Kompetenzen kennt ihre
Voraussetzungen. Wer die schriftliche Division üben will, bekommt sie erst
angeboten, wenn die Multiplikation begonnen wurde. Der Graph steuert die
Auswahl der nächsten Aufgabe und ist in der Detailansicht sichtbar.

**Aufgaben werden erzeugt, nicht abgespult.** 18 Generatoren bauen jede
Aufgabe neu aus einer Zufallsquelle. Es gibt keinen festen Aufgabenpool, der
sich nach zwei Wochen wiederholt.

**Exakte Bruchrechnung.** Brüche rechnen durchgehend mit Ganzzahlen und
kürzen über den größten gemeinsamen Teiler. Es gibt keine Fließkommazahlen in
der Rechenlogik, also auch keine Rundungsfehler wie 0,30000000000000004.

**Fehlerarten statt richtig und falsch.** 35 benannte Fehlvorstellungen sind
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

---

## Was noch fehlt

Diese Punkte des Auftrags sind nicht umgesetzt. Sie stehen hier, damit
niemand sie beim Lesen des Codes suchen muss.

- **Klassen 9 und 10 sind leer.** Die Profilauswahl bietet sie an, aber keine
  Kompetenz ist für diese Stufen hinterlegt. Wer Klasse 9 wählt, bekommt den
  Stoff der Klasse 8.
- **Die Niveaustufen G, M und E sind kaum getrennt.** 17 der 18 Kompetenzen
  gelten für alle drei Stufen gleich.
- **Reproduzierbarkeit ist nicht belegt.** Die Generatoren ziehen zwar
  ausschließlich aus der übergebenen Zufallsquelle, aber der Startwert wird
  weder gespeichert noch prüft ein Test, dass derselbe Startwert dieselbe
  Aufgabe ergibt.
- **Keine eigenschaftsbasierten Tests.** Es gibt 19 Tests mit festen
  Einzelfällen. Die 18 Generatoren sind davon nicht abgedeckt.
- **Room und Hilt werden nicht verwendet.** Statt einer SQLite-Datenbank
  speichert die App eine JSON-Datei, statt eines Einspritzrahmens genügt der
  Standardkonstruktor des Ansichtsmodells.

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
