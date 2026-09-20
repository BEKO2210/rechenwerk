package de.rechenwerk.mathe.daten.raum

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rechenwerk.mathe.daten.KompetenzStand
import de.rechenwerk.mathe.daten.Laufend
import de.rechenwerk.mathe.daten.Niveau
import de.rechenwerk.mathe.daten.Verlaufseintrag
import de.rechenwerk.mathe.daten.Werte

/**
 * Die Tabellen der Datenbank. Jede Zeile entspricht eins zu eins einem der
 * Datentypen aus `daten/Ablage.kt`; die Umrechnung steht in dieser Datei und
 * nirgends sonst.
 */

/** Ein Kompetenzstand. Schluessel ist die Kennung der Kompetenz. */
@Entity(tableName = "kompetenzstand")
data class KompetenzStandZeile(
    @PrimaryKey val kennung: String,
    val grad: Double,
    val versuche: Int,
    val treffer: Int,
    val zeitSummeMs: Long,
    val letzterKontakt: Long,
    val naechsteWdh: Long,
    val stufe: Int,
    val wiederholungen: Int,
)

/** Wie oft eine benannte Fehlvorstellung bei einer Kompetenz gezaehlt wurde. */
@Entity(tableName = "fehlerart", primaryKeys = ["kennung", "art"])
data class FehlerartZeile(
    val kennung: String,
    val art: String,
    val anzahl: Int,
)

/** Eine beantwortete Aufgabe. Gespeichert wird ihr Startwert, nicht ihr Text. */
@Entity(tableName = "verlauf")
data class VerlaufZeile(
    @PrimaryKey(autoGenerate = true) val nummer: Long = 0L,
    val kompetenz: String,
    val startwert: Long,
    val niveau: String,
    val richtig: Boolean,
    val zeitpunkt: Long,
)

/** Die gerade gestellte, noch unbeantwortete Aufgabe. Immer die feste Zeile 0. */
@Entity(tableName = "laufend")
data class LaufendZeile(
    @PrimaryKey val zeile: Int = ZEILE,
    val kompetenz: String,
    val startwert: Long,
    val niveau: String,
) {
    companion object { const val ZEILE = 0 }
}

/** Die Gesamtzahlen ueber alle Kompetenzen. Ebenfalls eine feste Zeile 0. */
@Entity(tableName = "werte")
data class WerteZeile(
    @PrimaryKey val zeile: Int = ZEILE,
    val lernzeitMs: Long,
    val versuche: Int,
    val treffer: Int,
    val wiederholungen: Int,
    val serie: Int,
    val besteSerie: Int,
    val schnellsteMs: Long,
    val sitzungen: Int,
) {
    companion object { const val ZEILE = 0 }
}

// ---- Umrechnung zwischen Tabelle und Datentyp -------------------------------

fun KompetenzStand.alsZeile(): KompetenzStandZeile = KompetenzStandZeile(
    kennung = kennung,
    grad = grad,
    versuche = versuche,
    treffer = treffer,
    zeitSummeMs = zeitSummeMs,
    letzterKontakt = letzterKontakt,
    naechsteWdh = naechsteWdh,
    stufe = stufe,
    wiederholungen = wiederholungen,
)

fun KompetenzStand.fehlerartZeilen(): List<FehlerartZeile> =
    fehlerarten.map { (art, anzahl) -> FehlerartZeile(kennung, art, anzahl) }

fun KompetenzStandZeile.alsStand(fehlerarten: Map<String, Int>): KompetenzStand = KompetenzStand(
    kennung = kennung,
    grad = grad,
    versuche = versuche,
    treffer = treffer,
    zeitSummeMs = zeitSummeMs,
    letzterKontakt = letzterKontakt,
    naechsteWdh = naechsteWdh,
    stufe = stufe,
    wiederholungen = wiederholungen,
    fehlerarten = fehlerarten,
)

fun Verlaufseintrag.alsZeile(): VerlaufZeile = VerlaufZeile(
    kompetenz = kompetenz,
    startwert = startwert,
    niveau = niveau.name,
    richtig = richtig,
    zeitpunkt = zeitpunkt,
)

fun VerlaufZeile.alsEintrag(): Verlaufseintrag = Verlaufseintrag(
    kompetenz = kompetenz,
    startwert = startwert,
    niveau = niveauAus(niveau),
    richtig = richtig,
    zeitpunkt = zeitpunkt,
)

fun Laufend.alsZeile(): LaufendZeile = LaufendZeile(
    kompetenz = kompetenz,
    startwert = startwert,
    niveau = niveau.name,
)

fun LaufendZeile.alsLaufend(): Laufend = Laufend(
    kompetenz = kompetenz,
    startwert = startwert,
    niveau = niveauAus(niveau),
)

fun Werte.alsZeile(): WerteZeile = WerteZeile(
    lernzeitMs = lernzeitMs,
    versuche = versuche,
    treffer = treffer,
    wiederholungen = wiederholungen,
    serie = serie,
    besteSerie = besteSerie,
    schnellsteMs = schnellsteMs,
    sitzungen = sitzungen,
)

fun WerteZeile.alsWerte(): Werte = Werte(
    lernzeitMs = lernzeitMs,
    versuche = versuche,
    treffer = treffer,
    wiederholungen = wiederholungen,
    serie = serie,
    besteSerie = besteSerie,
    schnellsteMs = schnellsteMs,
    sitzungen = sitzungen,
)

/** Ein unbekannter Name faellt auf das mittlere Niveau zurueck, nie auf einen Absturz. */
private fun niveauAus(name: String): Niveau =
    Niveau.entries.firstOrNull { it.name == name } ?: Niveau.M
