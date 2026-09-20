# Room und Hilt — umgesetzt

Dieses Dokument war der Bauplan für Room und Hilt. Beides ist seit dem
20.09.2026 eingebaut; hier steht jetzt, wo was liegt und warum.

## Fassungen

Die Nummern wurden am 20.09.2026 an der Quelle geprüft (`repo1.maven.org`,
`dl.google.com/dl/android/maven2`) und sind nicht geraten:

| Baustein | Fassung | Ort |
|---|---|---|
| `com.google.devtools.ksp` | 2.0.21-1.0.28 | Wurzel-`build.gradle.kts` |
| `com.google.dagger.hilt.android` | 2.57.2 | Wurzel-`build.gradle.kts` |
| `androidx.room:room-runtime` / `-ktx` / `-compiler` | 2.7.2 | `app/build.gradle.kts` |
| `com.google.dagger:hilt-android` / `hilt-compiler` | 2.57.2 | `app/build.gradle.kts` |
| `androidx.hilt:hilt-navigation-compose` | 1.3.0 | `app/build.gradle.kts` |

`2.0.21-1.0.28` ist die KSP-Fassung zur Kotlin-Fassung 2.0.21 — KSP trägt die
Kotlin-Fassung im Namen. Die Abhängigkeitssperre (`app/gradle.lockfile`) wird
bei einem Lauf mit Änderungsauftrag vor dem Bau neu geschrieben
(`./gradlew :app:dependencies --write-locks`); dafür braucht es Netzzugriff auf
`google()` und `mavenCentral()`.

Der Paketname bleibt `de.rechenwerk.mathe`. Der Gradle-Wrapper ist unberührt.

## Die Datenbank — `daten/raum/`

| Datei | Inhalt |
|---|---|
| `Zeilen.kt` | Die fünf `@Entity`-Klassen und die Umrechnung von und nach `daten/Ablage.kt` |
| `Tafeln.kt` | Vier `@Dao`-Schnittstellen; lesende Abfragen geben `Flow` zurück |
| `RechenwerkRaum.kt` | Die `@Database`-Klasse, Fassung 1, `exportSchema = true` |
| `Lernablage.kt` | Das Repository: kapselt die Tafeln, liefert `Flow<Lernstand>` |
| `AltdatenUebernahme.kt` | Der einmalige Schritt vom JSON-Stand in die Tabellen |

| Entität | entspricht | Schlüssel |
|---|---|---|
| `KompetenzStandZeile` | `KompetenzStand` | `kennung` |
| `FehlerartZeile` | `KompetenzStand.fehlerarten` | `kennung` + `art` |
| `VerlaufZeile` | `Verlaufseintrag` | fortlaufende Nummer |
| `LaufendZeile` | `Laufend` | feste Zeile 0 |
| `WerteZeile` | `Werte` | feste Zeile 0 |

Die Wiederholungstermine stecken als `naechsteWdh` und `stufe` im
Kompetenzstand und brauchen keine eigene Tabelle. Das Schema wird über
`room.schemaLocation` nach `app/src/main/schemas/` ausgegeben, damit spätere Fassungen
dagegen wandern können. `fallbackToDestructiveMigration` kommt nicht vor.

Eine beantwortete Aufgabe schreibt vier Dinge auf einmal: Kompetenzstand,
Gesamtzahlen, Verlaufseintrag und das Ende der offenen Aufgabe. Das steht in
`Lernablage.schreibeAntwort` in einer Transaktion — einzeln geschrieben meldete
die Datenbank Zwischenstände, und die Serien-Strecke zuckte.

## Der Tresor bleibt

Der Suite-Kern führt weiterhin Profil, Erscheinungsbild und das Merkzeichen der
Übernahme (`daten/Einstellungsspeicher.kt`). Jede Änderung einer Einstellung
schreibt ihn sofort, so dass nach Tod und Neustart eine gültige JSON-Datei unter
`files/` liegt. Die Room-Datei liegt daneben unter `databases/`.

`Ablage.nurEinstellungen()` schneidet den Tresor-Teil heraus: im Tresor stehen
keine fachlichen Daten mehr, damit nicht zwei Speicher dieselbe Wahrheit
behaupten. Export und Import gehen dagegen weiter über die vollständige
`Ablage` — eine exportierte Datei enthält den ganzen Lernstand.

## Die Übernahme

Beim ersten Start mit Room liest `Werk.init` den Tresor, in dem noch der
vollständige alte Stand steht, und schreibt ihn über
`AltdatenUebernahme.fuehreAus` in die Tabellen. Danach setzt es
`nachRaumUebernommen` im Tresor; eine zweite Übernahme würde frischen
Fortschritt mit altem Stand überschreiben und läuft deshalb nicht.

Die Umrechnung `AltdatenUebernahme.zeilenAus` ist eine reine Funktion und wird
ohne Gerät und ohne Datenbank geprüft: `app/src/test/.../RaumTest.kt`.

## Hilt

- `RechenwerkAnwendung` trägt `@HiltAndroidApp` und steht in
  `AndroidManifest.xml` als `android:name`. Der Paketname im Manifest bleibt
  unverändert.
- `MainActivity` trägt `@AndroidEntryPoint`.
- `Werk` trägt `@HiltViewModel` mit `@Inject constructor` und wird über
  `hiltViewModel()` geholt.
- `daten/DatenModul.kt` ist das `@Module @InstallIn(SingletonComponent::class)`
  und stellt Datenbank, die vier Tafeln und den Tresor bereit; `Lernablage` ist
  `@Singleton` mit eingespritztem Konstruktor.
