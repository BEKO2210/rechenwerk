package de.rechenwerk.mathe

import de.rechenwerk.mathe.daten.Ablage
import de.rechenwerk.mathe.daten.KompetenzStand
import de.rechenwerk.mathe.daten.Laufend
import de.rechenwerk.mathe.daten.Niveau
import de.rechenwerk.mathe.daten.Papier
import de.rechenwerk.mathe.daten.Serie
import de.rechenwerk.mathe.daten.Verlaufseintrag
import de.rechenwerk.mathe.daten.Werte
import de.rechenwerk.mathe.daten.raum.AltdatenUebernahme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruefungen der Room-Schicht, soweit sie ohne Geraet moeglich sind: die
 * Umrechnung des alten JSON-Stands in Tabellenzeilen und die Serien-Zaehlung.
 */
class RaumTest {

    // ---- Altdaten-Uebernahme ------------------------------------------------

    private fun altStand(): Ablage = Ablage(
        staende = mapOf(
            "BR-KUE-1" to KompetenzStand(
                kennung = "BR-KUE-1",
                grad = 0.42,
                versuche = 6,
                treffer = 4,
                zeitSummeMs = 30_000L,
                letzterKontakt = 1_700_000_000_000L,
                naechsteWdh = 1_700_259_200_000L,
                stufe = 2,
                wiederholungen = 3,
                fehlerarten = mapOf("nur_zaehler_gekuerzt" to 2, "nenner_addiert" to 1),
            ),
            "GR-MUL-1" to KompetenzStand(
                kennung = "GR-MUL-1",
                grad = 0.9,
                versuche = 12,
                treffer = 11,
                fehlerarten = mapOf("reihe_verzaehlt" to 1),
            ),
        ),
        werte = Werte(lernzeitMs = 60_000L, versuche = 18, treffer = 15, serie = 4, besteSerie = 9),
        verlauf = listOf(
            Verlaufseintrag("GR-MUL-1", 17L, Niveau.M, true, 1_700_000_000_000L),
            Verlaufseintrag("BR-KUE-1", -4L, Niveau.E, false, 1_700_000_060_000L),
        ),
        laufend = Laufend("BR-KUE-1", 99L, Niveau.G),
    )

    @Test
    fun dieUebernahmeStehtNurVorDemMerkzeichenAn() {
        assertTrue(AltdatenUebernahme.noetig(Ablage()))
        assertFalse(AltdatenUebernahme.noetig(Ablage(nachRaumUebernommen = true)))
    }

    @Test
    fun dieUebernahmeVerliertKeinenFortschritt() {
        val satz = AltdatenUebernahme.zeilenAus(altStand())

        assertEquals(2, satz.staende.size)
        val brueche = satz.staende.first { it.kennung == "BR-KUE-1" }
        assertEquals(0.42, brueche.grad, 1e-9)
        assertEquals(6, brueche.versuche)
        assertEquals(4, brueche.treffer)
        assertEquals(30_000L, brueche.zeitSummeMs)
        assertEquals(1_700_259_200_000L, brueche.naechsteWdh)
        assertEquals(2, brueche.stufe)
        assertEquals(3, brueche.wiederholungen)

        // Fehlerarten stehen in einer eigenen Tabelle, eine Zeile je Muster.
        assertEquals(3, satz.fehlerarten.size)
        assertEquals(
            2,
            satz.fehlerarten.first { it.kennung == "BR-KUE-1" && it.art == "nur_zaehler_gekuerzt" }.anzahl,
        )

        assertEquals(2, satz.verlauf.size)
        assertEquals(17L, satz.verlauf[0].startwert)
        assertEquals("M", satz.verlauf[0].niveau)
        assertTrue(satz.verlauf[0].richtig)
        assertFalse(satz.verlauf[1].richtig)

        assertNotNull(satz.laufend)
        assertEquals("BR-KUE-1", satz.laufend?.kompetenz)
        assertEquals(99L, satz.laufend?.startwert)
        assertEquals("G", satz.laufend?.niveau)

        assertEquals(18, satz.werte.versuche)
        assertEquals(4, satz.werte.serie)
        assertEquals(9, satz.werte.besteSerie)
    }

    @Test
    fun einLeererStandErgibtLeereTabellen() {
        val satz = AltdatenUebernahme.zeilenAus(Ablage())
        assertTrue(satz.staende.isEmpty())
        assertTrue(satz.fehlerarten.isEmpty())
        assertTrue(satz.verlauf.isEmpty())
        assertNull(satz.laufend)
        assertEquals(0, satz.werte.versuche)
    }

    @Test
    fun dieUebernahmeKuerztDieHistorieAufIhrLimit() {
        val lang = (0 until Ablage.VERLAUF_MAX + 40).map {
            Verlaufseintrag("GR-MUL-1", it.toLong(), Niveau.M, true, it.toLong())
        }
        val satz = AltdatenUebernahme.zeilenAus(Ablage(verlauf = lang))
        assertEquals(Ablage.VERLAUF_MAX, satz.verlauf.size)
        assertEquals((Ablage.VERLAUF_MAX + 39).toLong(), satz.verlauf.last().startwert)
    }

    @Test
    fun einStandAusDerDateiUeberstehtDieUebernahme() {
        val gelesen = Papier.lies(Papier.schreibe(altStand()))
        val satz = AltdatenUebernahme.zeilenAus(gelesen)
        assertEquals(2, satz.staende.size)
        assertEquals(3, satz.fehlerarten.size)
        assertEquals(2, satz.verlauf.size)
        assertEquals(4, satz.werte.serie)
    }

    // ---- Serien-Zaehlung ----------------------------------------------------

    @Test
    fun jederTrefferVerlaengertDieSerie() {
        var werte = Werte()
        repeat(5) { werte = Serie.nachAntwort(werte, richtig = true) }
        assertEquals(5, werte.serie)
        assertEquals(5, werte.besteSerie)
    }

    @Test
    fun einFehlerWirftDieSerieAufNullUndLaesstDenBestwertStehen() {
        var werte = Werte()
        repeat(6) { werte = Serie.nachAntwort(werte, richtig = true) }
        werte = Serie.nachAntwort(werte, richtig = false)
        assertEquals(0, werte.serie)
        assertEquals(6, werte.besteSerie)

        // Danach faengt sie wieder bei eins an -- der Bestwert bleibt.
        werte = Serie.nachAntwort(werte, richtig = true)
        assertEquals(1, werte.serie)
        assertEquals(6, werte.besteSerie)
    }

    @Test
    fun derBestwertWaechstErstWennDieSerieIhnUeberholt() {
        var werte = Werte(besteSerie = 4)
        repeat(4) { werte = Serie.nachAntwort(werte, richtig = true) }
        assertEquals(4, werte.besteSerie)
        werte = Serie.nachAntwort(werte, richtig = true)
        assertEquals(5, werte.serie)
        assertEquals(5, werte.besteSerie)
    }

    @Test
    fun einFehlerGanzZuBeginnAendertNichts() {
        val werte = Serie.nachAntwort(Werte(), richtig = false)
        assertEquals(0, werte.serie)
        assertEquals(0, werte.besteSerie)
    }
}
