# Nachtrag: Room und Hilt (Auftragspunkte 6 und 7)

Die Punkte 6 (Room für die Lerndaten) und 7 (Hilt für die Abhängigkeiten) des
Änderungsauftrags sind **nicht umgesetzt**. Dieses Dokument sagt, warum, und
was genau einzutragen ist, damit der nächste Arbeitsschritt sie ohne erneute
Untersuchung erledigen kann.

## Warum blockiert

Zwei Sperren, beide unabhängig voneinander ausreichend:

1. **Schreibgrenze.** Der Arbeitsschritt, der die Compose-Bildschirme schreibt,
   darf ausschließlich unterhalb von `app/src/main/java` und
   `app/src/main/res` schreiben; `build.gradle.kts` ist ausdrücklich
   ausgenommen. Room und Hilt brauchen dort das KSP-Plugin und vier bis sechs
   neue Abhängigkeiten. Ohne sie übersetzt eine einzige Datei mit `@Entity`
   oder `@HiltAndroidApp` nicht — der Bau schlüge fehl, nicht nur die Prüfung.

2. **Abhängigkeitssperre.** `app/build.gradle.kts` schaltet
   `dependencyLocking { lockAllConfigurations() }` ein, `app/gradle.lockfile`
   hält die vier App-Klassenpfade fest. Jede neue Abhängigkeit lässt die
   Auflösung scheitern, solange die Lockdatei nicht neu erzeugt ist. Das geht
   nur mit einem Gradle-Lauf.

Solange beides gilt, wäre jedes Eintragen von Room- oder Hilt-Quellcode ein
sicher kaputter Bau. Deshalb steht hier ein Plan statt halber Dateien.

## Schritt 1 — Wurzel-`build.gradle.kts`

```kotlin
plugins {
    id("com.android.application") version "8.13.2" apply false
    id("com.android.library") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "<KSP>" apply false
    id("com.google.dagger.hilt.android") version "<HILT>" apply false
}
```

`<KSP>` muss zur Kotlin-Fassung 2.0.21 passen; die KSP-Versionen tragen die
Kotlin-Fassung im Namen (`2.0.21-…`). `<HILT>` muss eine Fassung sein, die
Kotlin 2.0 und KSP unterstützt. **Beide Nummern sind an der Quelle zu prüfen**
(`https://dl.google.com/dl/android/maven2/`, `https://repo1.maven.org/maven2/`);
aus dieser Sitzung heraus war kein Netzzugriff möglich, deshalb stehen hier
Platzhalter statt geratener Zahlen.

## Schritt 2 — `app/build.gradle.kts`

Im `plugins`-Block:

```kotlin
id("com.google.devtools.ksp")
id("com.google.dagger.hilt.android")
```

Im `dependencies`-Block:

```kotlin
implementation("androidx.room:room-runtime:<ROOM>")
implementation("androidx.room:room-ktx:<ROOM>")
ksp("androidx.room:room-compiler:<ROOM>")

implementation("com.google.dagger:hilt-android:<HILT>")
ksp("com.google.dagger:hilt-compiler:<HILT>")
implementation("androidx.hilt:hilt-navigation-compose:<HILT_NAV>")

testImplementation("androidx.room:room-testing:<ROOM>")
```

Das Schema der Datenbank gehört ins Repository, damit spätere Fassungen
gegen es wandern können:

```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

## Schritt 3 — Lockdatei neu erzeugen

```bash
./gradlew :app:dependencies --write-locks
```

Ohne diesen Lauf scheitert jede Auflösung. Er braucht Netzzugriff auf
`google()` und `mavenCentral()`.

## Schritt 4 — Quellcode

Erst nach Schritt 1 bis 3 übersetzt das Folgende. Paketname bleibt
`de.rechenwerk.mathe`.

**`daten/raum/` — die Datenbank.** Vier Entitäten, die den heutigen
`Ablage`-Datentypen eins zu eins entsprechen (`daten/Ablage.kt`):

| Entität | entspricht heute | Schlüssel |
|---|---|---|
| `KompetenzStandZeile` | `KompetenzStand` | `kennung` |
| `FehlerartZeile` | `KompetenzStand.fehlerarten` | `kennung` + `art` |
| `VerlaufZeile` | `Verlaufseintrag` | fortlaufende Nummer |
| `LaufendZeile` | `Laufend` | feste Zeile 0 |

Wiederholungstermine stecken bereits als `naechsteWdh` und `stufe` im
`KompetenzStand` und brauchen keine eigene Tabelle. Dazu ein DAO je Bereich
und eine `@Database(version = 1)`-Klasse mit `exportSchema = true`.

**Der Tresor bleibt.** Punkt 6 verlangt beides nebeneinander: Room führt die
Lerndaten, der Tresor des Suite-Kerns führt weiterhin Profil und
Erscheinungsbild und schreibt den Export als JSON-Datei. `daten/Ablage.kt`
(`Papier.schreibe` / `Papier.lies`) bleibt dafür unverändert das Format —
der Export muss den vollständigen Lernstand enthalten, also die Room-Daten
mit ausgeben und beim Import zurückschreiben.

**Einmalige Übernahme.** Beim ersten Start mit Room wird eine vorhandene
Tresor-Datei gelesen und ihr Inhalt nach Room geschrieben, danach ein
Merkzeichen im Tresor gesetzt (etwa `"nachRaumUebernommen": true`), damit die
Übernahme nicht zweimal läuft. Niemand darf dabei Fortschritt verlieren.

**Hilt.** Anwendungsklasse mit `@HiltAndroidApp` (neu, muss zusätzlich ins
Manifest als `android:name`), `MainActivity` mit `@AndroidEntryPoint`, ein
`@Module @InstallIn(SingletonComponent::class)`, das Datenbank, DAOs und
Ablagen bereitstellt, und `Werk` als `@HiltViewModel` mit eingespritztem
Konstruktor statt `viewModel()` mit Standardkonstruktor.

> Achtung: `android:name` im `<application>`-Element zu setzen ist erlaubt —
> gesperrt ist nur der **Paketname** im Manifest. Er bleibt
> `de.rechenwerk.mathe`.

**Tests.** `app/src/test` deckt heute die JSON-Schicht ab. Dazu kommen Tests
für die Übernahme Tresor → Room und für Export und Import über beide
Speicher hinweg.
