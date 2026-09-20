package de.rechenwerk.mathe

import de.rechenwerk.mathe.daten.Ablage
import de.rechenwerk.mathe.daten.Bruch
import de.rechenwerk.mathe.daten.Katalog
import de.rechenwerk.mathe.daten.KompetenzStand
import de.rechenwerk.mathe.daten.Laufend
import de.rechenwerk.mathe.daten.Modus
import de.rechenwerk.mathe.daten.Niveau
import de.rechenwerk.mathe.daten.Papier
import de.rechenwerk.mathe.daten.Profil
import de.rechenwerk.mathe.daten.Schulart
import de.rechenwerk.mathe.daten.Verlaufseintrag
import de.rechenwerk.mathe.daten.Werkbank
import de.rechenwerk.mathe.daten.Werte
import de.rechenwerk.mathe.daten.Wiederholung
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruefungen der Datenschicht: exakte Bruchrechnung, Eingabegrenzen,
 * JSON hin und zurueck und die Abwehr kaputter Importdateien.
 */
class DatenTest {

    // ---- Bruchrechnung ------------------------------------------------------

    @Test
    fun bruchKuerztBeimAnlegen() {
        assertEquals(Bruch.von(3, 4), Bruch.von(18, 24))
        assertEquals(Bruch.von(1, 1), Bruch.von(-5, -5))
        assertEquals(Bruch.NULL, Bruch.von(0, 7))
    }

    @Test
    fun vorzeichenStehtImmerImZaehler() {
        val bruch = Bruch.von(3, -4)
        assertEquals(-3L, bruch.zaehler)
        assertEquals(4L, bruch.nenner)
    }

    @Test
    fun dezimalrechnungBleibtExakt() {
        // 1,7 * 0,4 ist exakt 0,68 -- mit Double waere es 0,6800000000000001.
        val ergebnis = Bruch.von(17, 10) * Bruch.von(4, 10)
        assertEquals(Bruch.von(17, 25), ergebnis)
        assertEquals("0,68", ergebnis.alsDezimalText())
    }

    @Test
    fun bruchteilungNutztDenKehrwert() {
        assertEquals(Bruch.von(15, 8), Bruch.von(3, 4) / Bruch.von(2, 5))
    }

    @Test
    fun nichtAbbrechendeBruecheBleibenBrueche() {
        val drittel = Bruch.von(1, 3)
        assertEquals("1/3", drittel.alsText())
        assertEquals("0,3333", drittel.alsDezimalText())
    }

    @Test
    fun negativeZahlenNutzenDasTypografischeMinus() {
        assertEquals(Bruch.MINUS + "48", Bruch.von(-48).alsBruchText())
    }

    // ---- Eingabe ------------------------------------------------------------

    @Test
    fun eingabeAkzeptiertKommaUndPunkt() {
        assertEquals(Bruch.von(17, 10), Bruch.lies("1,7"))
        assertEquals(Bruch.von(17, 10), Bruch.lies("1.7"))
        assertEquals(Bruch.von(17, 10), Bruch.lies(" 1,7 "))
    }

    @Test
    fun eingabeAkzeptiertBruecheUndVorzeichen() {
        assertEquals(Bruch.von(3, 4), Bruch.lies("3/4"))
        assertEquals(Bruch.von(-3, 4), Bruch.lies("-3/4"))
        assertEquals(Bruch.von(-8), Bruch.lies(Bruch.MINUS + "8"))
        assertEquals(Bruch.von(5), Bruch.lies("+5"))
    }

    @Test
    fun eingabeWeistUnfugAb() {
        assertNull(Bruch.lies(""))
        assertNull(Bruch.lies("   "))
        assertNull(Bruch.lies("-"))
        assertNull(Bruch.lies("1,2,3"))
        assertNull(Bruch.lies("3/0"))
        assertNull(Bruch.lies("3//4"))
        assertNull(Bruch.lies("zwölf"))
    }

    @Test
    fun eingabeBegrenztDieStellenzahl() {
        assertNotNull(Bruch.lies("123456789012345"))
        assertNull(Bruch.lies("1234567890123456"))
        assertNull(Bruch.lies("0,1234567890"))
    }

    // ---- Ablage: JSON hin und zurueck ---------------------------------------

    private fun beispiel(): Ablage = Ablage(
        profil = Profil(
            bundesland = "BW",
            schulart = Schulart.GYMNASIUM,
            klasse = 8,
            niveau = Niveau.E,
            eingerichtet = true,
        ),
        modus = Modus.HELL,
        staende = mapOf(
            "BR-ADD-1" to KompetenzStand(
                kennung = "BR-ADD-1",
                grad = 0.61,
                versuche = 9,
                treffer = 5,
                zeitSummeMs = 45_000L,
                letzterKontakt = 1_700_000_000_000L,
                naechsteWdh = 1_700_259_200_000L,
                stufe = 2,
                wiederholungen = 4,
                fehlerarten = mapOf("zaehler_und_nenner_verrechnet" to 3),
            )
        ),
        werte = Werte(
            lernzeitMs = 900_000L,
            versuche = 40,
            treffer = 31,
            wiederholungen = 12,
            serie = 3,
            besteSerie = 7,
            schnellsteMs = 2_400L,
            sitzungen = 5,
        ),
        verlauf = listOf(
            Verlaufseintrag("BR-ADD-1", -8_123_456_789L, Niveau.E, true, 1_700_000_000_000L),
            Verlaufseintrag("GL-QUA-1", 42L, Niveau.M, false, 1_700_000_060_000L),
        ),
        laufend = Laufend("GE-PYT-1", 9_876_543_210L, Niveau.E),
        nachRaumUebernommen = true,
    )

    @Test
    fun ablageUeberstehtDenWegDurchJson() {
        val vorher = beispiel()
        val nachher = Papier.lies(Papier.schreibe(vorher))
        assertEquals(vorher, nachher)
    }

    @Test
    fun leereAblageUeberstehtDenWegDurchJson() {
        val leer = Ablage()
        val nachher = Papier.lies(Papier.schreibe(leer))
        assertEquals(leer, nachher)
        assertEquals(0, nachher.werte.versuche)
        assertTrue(nachher.staende.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun importLehntKaputtesJsonAb() {
        Papier.lies("{ das ist kein json")
    }

    @Test(expected = IllegalArgumentException::class)
    fun importLehntFremdesFormatAb() {
        Papier.lies("""{"format":"etwas-anderes","fassung":1}""")
    }

    @Test(expected = IllegalArgumentException::class)
    fun importLehntZuNeueFassungAb() {
        Papier.lies("""{"format":"rechenwerk","fassung":99}""")
    }

    @Test
    fun importFaengtUnsinnigeWerteAb() {
        val roh = """
            {"format":"rechenwerk","fassung":1,
             "profil":{"schulart":"MONDSCHULE","klasse":42,"niveau":"X"},
             "modus":"BUNT",
             "werte":{"versuche":-5},
             "staende":[{"kennung":"GR-MUL-1","grad":7.5,"stufe":99}]}
        """.trimIndent()
        val ablage = Papier.lies(roh)
        assertEquals(Schulart.REALSCHULE, ablage.profil.schulart)
        assertEquals(10, ablage.profil.klasse)
        assertEquals(Niveau.M, ablage.profil.niveau)
        // Unbekanntes Erscheinungsbild faellt auf den Standard zurueck: wie das System.
        assertEquals(Modus.SYSTEM, ablage.modus)
        assertEquals(0, ablage.werte.versuche)
        assertEquals(1.0, ablage.stand("GR-MUL-1").grad, 0.0001)
        assertEquals(Wiederholung.STUFEN.lastIndex, ablage.stand("GR-MUL-1").stufe)
    }

    @Test
    fun eineFrischeInstallationFolgtDemSystemUndIstLeer() {
        val frisch = Ablage()
        assertEquals(Modus.SYSTEM, frisch.modus)
        assertTrue(frisch.staende.isEmpty())
        assertTrue(frisch.verlauf.isEmpty())
        assertNull(frisch.laufend)
        assertEquals(0, frisch.werte.versuche)
        assertEquals(0, frisch.werte.serie)
    }

    // ---- Startwerte in der Historie -----------------------------------------

    @Test
    fun startwerteUeberstehenDenWegDurchJson() {
        val nachher = Papier.lies(Papier.schreibe(beispiel()))
        assertEquals(2, nachher.verlauf.size)
        assertEquals(-8_123_456_789L, nachher.verlauf[0].startwert)
        assertEquals(Niveau.E, nachher.verlauf[0].niveau)
        assertEquals(9_876_543_210L, nachher.laufend?.startwert)
        assertEquals("GE-PYT-1", nachher.laufend?.kompetenz)
    }

    @Test
    fun derselbeStartwertBautDieselbeAufgabeWiederAuf() {
        val eintrag = beispiel().laufend!!
        val erste = Werkbank.erzeuge(eintrag.kompetenz, eintrag.niveau, eintrag.startwert)
        // Der Umweg ueber die Datei darf nichts veraendern.
        val gelesen = Papier.lies(Papier.schreibe(beispiel())).laufend!!
        val zweite = Werkbank.erzeuge(gelesen.kompetenz, gelesen.niveau, gelesen.startwert)
        assertEquals(erste.frage, zweite.frage)
        assertEquals(erste.loesung, zweite.loesung)
    }

    @Test
    fun dieHistorieBleibtBegrenzt() {
        var ablage = Ablage()
        for (i in 0 until Ablage.VERLAUF_MAX + 50) {
            ablage = ablage.copy(
                verlauf = ablage.mitVerlauf(
                    Verlaufseintrag("GR-MUL-1", i.toLong(), Niveau.M, true, i.toLong())
                )
            )
        }
        assertEquals(Ablage.VERLAUF_MAX, ablage.verlauf.size)
        // Die aeltesten Eintraege fallen hinten weg, der jüngste steht am Ende.
        assertEquals((Ablage.VERLAUF_MAX + 49).toLong(), ablage.verlauf.last().startwert)
    }

    @Test
    fun eineDateiDerFassungEinsWirdWeiterGelesen() {
        val alt = """
            {"format":"rechenwerk","fassung":1,
             "profil":{"schulart":"GYMNASIUM","klasse":7,"niveau":"E","eingerichtet":true},
             "modus":"HELL",
             "werte":{"versuche":12,"treffer":9},
             "staende":[{"kennung":"BR-KUE-1","grad":0.5,"versuche":3}]}
        """.trimIndent()
        val ablage = Papier.lies(alt)
        assertEquals(7, ablage.profil.klasse)
        assertEquals(12, ablage.werte.versuche)
        assertEquals(3, ablage.stand("BR-KUE-1").versuche)
        // Fassung 1 kannte die Historie noch nicht.
        assertTrue(ablage.verlauf.isEmpty())
        assertNull(ablage.laufend)
    }

    // ---- Wiederholungsmodell ------------------------------------------------

    @Test
    fun richtigeAntwortHebtDenGradUndSchiebtDieWiederholung() {
        val jetzt = 1_700_000_000_000L
        val vorher = KompetenzStand("GR-MUL-1")
        val nachher = Wiederholung.naechster(vorher, richtig = true, jetzt = jetzt, dauerMs = 3_000L)
        assertTrue(nachher.grad > vorher.grad)
        assertEquals(1, nachher.stufe)
        assertEquals(1, nachher.versuche)
        assertEquals(1, nachher.treffer)
        assertTrue(nachher.naechsteWdh > jetzt)
    }

    @Test
    fun falscheAntwortZiehtZurueckAberNieUnterNull() {
        val jetzt = 1_700_000_000_000L
        val hoch = KompetenzStand("GR-MUL-1", grad = 0.9, stufe = 1)
        val nachher = Wiederholung.naechster(hoch, richtig = false, jetzt = jetzt, dauerMs = 9_000L)
        assertTrue(nachher.grad < hoch.grad)
        assertTrue(nachher.grad >= 0.0)
        assertEquals(0, nachher.stufe)
        assertTrue(Wiederholung.faellig(nachher, jetzt))
    }

    @Test
    fun einePauseKostetKeinenFortschritt() {
        val jetzt = 1_700_000_000_000L
        val stand = Wiederholung.naechster(KompetenzStand("GR-MUL-1"), true, jetzt, 3_000L)
        // Ein halbes Jahr spaeter: faellig, aber der Grad steht unveraendert.
        val spaeter = jetzt + 180L * 24L * 60L * 60L * 1000L
        assertTrue(Wiederholung.faellig(stand, spaeter))
        assertEquals(0.28, stand.grad, 1e-9)
    }

    // ---- Der Kompetenzgraph --------------------------------------------------

    @Test
    fun jedeVoraussetzungStehtImKatalog() {
        for (kompetenz in Katalog.alle) {
            for (kennung in kompetenz.voraussetzungen) {
                assertNotNull(
                    "${kompetenz.kennung} verweist auf die unbekannte Voraussetzung $kennung",
                    Katalog.finde(kennung),
                )
            }
        }
    }

    @Test
    fun voraussetzungenSindLeichterUndNieSpaeter() {
        // Weniger Muehe heisst: der Graph hat keinen Kreis. Und eine
        // Voraussetzung darf nicht erst in einer spaeteren Klasse drankommen.
        for (kompetenz in Katalog.alle) {
            for (kennung in kompetenz.voraussetzungen) {
                val vorher = Katalog.finde(kennung)!!
                assertTrue(
                    "${kompetenz.kennung} setzt $kennung voraus, das nicht leichter ist",
                    vorher.muehe < kompetenz.muehe,
                )
                assertTrue(
                    "${kompetenz.kennung} setzt $kennung aus einer spaeteren Klasse voraus",
                    vorher.abKlasse <= kompetenz.abKlasse,
                )
            }
        }
    }

    @Test
    fun jedeVoraussetzungGibtEsImEigenenNiveau() {
        // Sonst haengt eine Kompetenz an Stoff, den dieses Niveau nie zu sehen
        // bekommt -- "Weiterlernen" boete sie dann nie an.
        for (niveau in Niveau.entries) {
            for (kompetenz in Katalog.alle.filter { it.giltFuer(niveau) }) {
                for (kennung in Katalog.voraussetzungenFuer(kompetenz, niveau)) {
                    assertTrue(
                        "${kompetenz.kennung} setzt im Niveau $niveau $kennung voraus," +
                            " das es dort nicht gibt",
                        Katalog.finde(kennung)!!.giltFuer(niveau),
                    )
                }
            }
        }
    }

    @Test
    fun einAusgeblendeterVorlaeuferWirdErsetztStattGestrichen() {
        // GL-TER-1 setzt NZ-MUL-1 voraus, das es erst ab M gibt. Im Niveau G
        // tritt dessen eigene Voraussetzung NZ-ADD-1 an seine Stelle.
        val term = Katalog.finde("GL-TER-1")!!
        assertEquals(listOf("NZ-MUL-1"), Katalog.voraussetzungenFuer(term, Niveau.M))
        assertEquals(listOf("NZ-ADD-1"), Katalog.voraussetzungenFuer(term, Niveau.G))

        // GL-QUA-1 haengt im Niveau M an GL-KLA-1 (nur E); dessen Vorlaeufer
        // GL-LIN-1 bleibt erhalten, PO-WUR-1 steht unveraendert daneben.
        val quadratisch = Katalog.finde("GL-QUA-1")!!
        assertEquals(
            listOf("GL-LIN-1", "PO-WUR-1"),
            Katalog.voraussetzungenFuer(quadratisch, Niveau.M),
        )
    }
}
