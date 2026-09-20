package de.rechenwerk.mathe.daten.raum

import androidx.room.withTransaction
import de.rechenwerk.mathe.daten.Ablage
import de.rechenwerk.mathe.daten.KompetenzStand
import de.rechenwerk.mathe.daten.Laufend
import de.rechenwerk.mathe.daten.Verlaufseintrag
import de.rechenwerk.mathe.daten.Werte
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Der fachliche Stand, wie ihn die Datenbank fuehrt. */
data class Lernstand(
    val staende: Map<String, KompetenzStand> = emptyMap(),
    val werte: Werte = Werte(),
    val verlauf: List<Verlaufseintrag> = emptyList(),
    val laufend: Laufend? = null,
)

/**
 * Die einzige Quelle der fachlichen Daten. Sie kapselt die vier Tafeln; das
 * Ansichtsmodell kennt nur diese Klasse, nie ein DAO und nie SQL.
 */
@Singleton
class Lernablage @Inject constructor(
    private val raum: RechenwerkRaum,
    private val staendeTafel: StandTafel,
    private val fehlerTafel: FehlerartTafel,
    private val verlaufTafel: VerlaufTafel,
    private val sitzungTafel: SitzungTafel,
) {

    /** Der ganze Lernstand als ein Fluss -- jede Aenderung kommt von selbst an. */
    val stand: Flow<Lernstand> = combine(
        staendeTafel.alle(),
        fehlerTafel.alle(),
        verlaufTafel.alle(),
        sitzungTafel.laufend(),
        sitzungTafel.werte(),
    ) { staende, fehlerarten, verlauf, laufend, werte ->
        val nachKennung = fehlerarten.groupBy { it.kennung }
        Lernstand(
            staende = staende.associate { zeile ->
                val eigene = nachKennung[zeile.kennung]
                    ?.sortedByDescending { it.anzahl }
                    ?.associate { it.art to it.anzahl }
                    ?: emptyMap()
                zeile.kennung to zeile.alsStand(eigene)
            },
            werte = werte.firstOrNull()?.alsWerte() ?: Werte(),
            verlauf = verlauf.map { it.alsEintrag() },
            laufend = laufend.firstOrNull()?.alsLaufend(),
        )
    }

    /** Schreibt einen Kompetenzstand samt seinen gezaehlten Fehlerarten. */
    suspend fun schreibeStand(stand: KompetenzStand) = raum.withTransaction {
        staendeTafel.schreibe(stand.alsZeile())
        fehlerTafel.leereZu(stand.kennung)
        val zeilen = stand.fehlerartZeilen()
        if (zeilen.isNotEmpty()) fehlerTafel.schreibeAlle(zeilen)
    }

    suspend fun schreibeWerte(werte: Werte) = sitzungTafel.schreibeWerte(werte.alsZeile())

    /** Haengt eine beantwortete Aufgabe an und haelt die Historie kurz. */
    suspend fun haengeVerlaufAn(eintrag: Verlaufseintrag) = raum.withTransaction {
        verlaufTafel.lege(eintrag.alsZeile())
        verlaufTafel.kuerze(Ablage.VERLAUF_MAX)
    }

    /**
     * Alles, was eine beantwortete Aufgabe hinterlaesst, in einer Transaktion:
     * der neue Kompetenzstand, die Gesamtzahlen, der Verlaufseintrag und das
     * Ende der offenen Aufgabe. Einzeln geschrieben meldete die Datenbank
     * Zwischenstaende, und die Serien-Strecke zuckte.
     */
    suspend fun schreibeAntwort(
        stand: KompetenzStand,
        werte: Werte,
        eintrag: Verlaufseintrag,
    ) = raum.withTransaction {
        schreibeStand(stand)
        schreibeWerte(werte)
        haengeVerlaufAn(eintrag)
        setzeLaufend(null)
    }

    suspend fun setzeLaufend(laufend: Laufend?) {
        if (laufend == null) sitzungTafel.leereLaufend() else sitzungTafel.schreibeLaufend(laufend.alsZeile())
    }

    /**
     * Setzt den gesamten Lernstand neu -- fuer die Altdaten-Uebernahme, den
     * Import und das Zuruecksetzen. In einer Transaktion, damit nie ein halber
     * Stand stehen bleibt.
     */
    suspend fun ersetzeAlles(satz: Uebernahmesatz) = raum.withTransaction {
        staendeTafel.leere()
        fehlerTafel.leere()
        verlaufTafel.leere()
        sitzungTafel.leereLaufend()
        if (satz.staende.isNotEmpty()) staendeTafel.legeAlle(satz.staende)
        if (satz.fehlerarten.isNotEmpty()) fehlerTafel.schreibeAlle(satz.fehlerarten)
        if (satz.verlauf.isNotEmpty()) verlaufTafel.legeAlle(satz.verlauf)
        satz.laufend?.let { sitzungTafel.schreibeLaufend(it) }
        sitzungTafel.schreibeWerte(satz.werte)
    }
}
