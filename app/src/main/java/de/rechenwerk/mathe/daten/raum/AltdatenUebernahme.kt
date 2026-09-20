package de.rechenwerk.mathe.daten.raum

import de.rechenwerk.mathe.daten.Ablage

/**
 * Ein vollstaendiger Lernstand in Tabellenform -- das, was in die Datenbank
 * geschrieben wird, wenn sie vollstaendig neu gesetzt wird.
 */
data class Uebernahmesatz(
    val staende: List<KompetenzStandZeile>,
    val fehlerarten: List<FehlerartZeile>,
    val verlauf: List<VerlaufZeile>,
    val laufend: LaufendZeile?,
    val werte: WerteZeile,
)

/**
 * Der einmalige Schritt vom alten JSON-Stand in die Datenbank. Beim ersten
 * Start mit Room wird die vorhandene Tresor-Datei gelesen und vollstaendig in
 * die Tabellen uebernommen; danach ist die Datenbank die Wahrheit. Ein
 * Merkzeichen im Tresor (`nachRaumUebernommen`) verhindert eine zweite
 * Uebernahme, die frischen Fortschritt mit altem Stand ueberschreiben wuerde.
 *
 * Die Umrechnung steht bewusst als reine Funktion da: sie laesst sich ohne
 * Geraet und ohne Datenbank pruefen.
 */
object AltdatenUebernahme {

    /** Steht die Uebernahme noch aus? */
    fun noetig(alt: Ablage): Boolean = !alt.nachRaumUebernommen

    /** Rechnet einen Lernstand in die Zeilen der Tabellen um. */
    fun zeilenAus(alt: Ablage): Uebernahmesatz {
        val staende = alt.staende.values.sortedBy { it.kennung }
        return Uebernahmesatz(
            staende = staende.map { it.alsZeile() },
            fehlerarten = staende.flatMap { it.fehlerartZeilen() },
            verlauf = alt.verlauf.takeLast(Ablage.VERLAUF_MAX).map { it.alsZeile() },
            laufend = alt.laufend?.alsZeile(),
            werte = alt.werte.alsZeile(),
        )
    }

    /**
     * Fuehrt die Uebernahme aus, falls sie noch aussteht. Gibt zurueck, ob sie
     * tatsaechlich gelaufen ist -- nur dann wird das Merkzeichen gesetzt.
     */
    suspend fun fuehreAus(ablage: Lernablage, alt: Ablage): Boolean {
        if (!noetig(alt)) return false
        ablage.ersetzeAlles(zeilenAus(alt))
        return true
    }
}
