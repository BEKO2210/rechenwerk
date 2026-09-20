package de.rechenwerk.mathe

import de.rechenwerk.mathe.daten.Aufgabe
import de.rechenwerk.mathe.daten.Bruch
import de.rechenwerk.mathe.daten.Fehlerarten
import de.rechenwerk.mathe.daten.Katalog
import de.rechenwerk.mathe.daten.Niveau
import de.rechenwerk.mathe.daten.Werkbank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Eigenschaftsbasierte Pruefung aller Aufgabengeneratoren. Jeder Generator
 * laeuft ueber [STARTWERTE] verschiedene Startwerte und durch alle drei
 * Niveaus. Geprueft wird nicht eine einzelne erwartete Aufgabe, sondern das,
 * was fuer jede erzeugte Aufgabe gelten muss.
 */
class GeneratorTest {

    private companion object {
        /** So viele verschiedene Startwerte je Kompetenz und Niveau. */
        const val STARTWERTE = 1000
    }

    /**
     * Die Startwerte: weit gestreut, mit negativen Werten und den Raendern des
     * Zahlenbereichs. Alle verschieden, damit die Pruefung wirklich tausend
     * Faelle je Generator abdeckt.
     */
    private val startwerte: List<Long> = buildList {
        add(Long.MIN_VALUE)
        add(Long.MAX_VALUE)
        add(0L)
        add(-1L)
        var wert = 1L
        while (size < STARTWERTE) {
            // Ein grosser Schritt streut die Werte ueber den ganzen Bereich.
            wert += 7_919_311L
            add(if (size % 2 == 0) wert else -wert)
        }
    }

    private val niveaus = Niveau.entries.toList()

    @Test
    fun startwerteSindVerschiedenUndZahlreich() {
        assertTrue(startwerte.size >= STARTWERTE)
        assertEquals(startwerte.size, startwerte.toSet().size)
    }

    @Test
    fun jedeKompetenzHatEinenEigenenGenerator() {
        // Ohne eigenen Generator faellt erzeuge() auf das Einmaleins zurueck --
        // genau der Fehler, den diese Pruefung abfangen soll.
        for (kompetenz in Katalog.alle) {
            val aufgabe = Werkbank.erzeuge(kompetenz.kennung, Niveau.M, 42L)
            assertEquals(
                "Kompetenz ${kompetenz.kennung} hat keinen eigenen Generator",
                kompetenz.kennung,
                aufgabe.kompetenz,
            )
        }
    }

    /**
     * Der Hauptdurchlauf: jeder Generator, jedes Niveau, tausend Startwerte.
     * Laeuft eine Erzeugung in eine Division durch null, wirft [Bruch] von
     * sich aus -- schon das Durchlaufen ist damit ein Teil der Pruefung.
     */
    @Test
    fun jedeErzeugteAufgabeHaeltDieZusagenEin() {
        var geprueft = 0
        for (kompetenz in Katalog.alle) {
            for (niveau in niveaus) {
                for (startwert in startwerte) {
                    val aufgabe = Werkbank.erzeuge(kompetenz.kennung, niveau, startwert)
                    val hinweis = "${kompetenz.kennung}/$niveau/$startwert"

                    // Mathematisch korrekt: die Gegenrechnung muss aufgehen.
                    assertTrue(
                        "$hinweis: Gegenrechnung geht mit ${aufgabe.loesung} nicht auf",
                        aufgabe.probe(aufgabe.loesung),
                    )

                    for (bruch in alleBrueche(aufgabe)) {
                        // Nirgends durch null geteilt.
                        assertTrue(
                            "$hinweis: Nenner ${bruch.nenner} ist nicht positiv",
                            bruch.nenner > 0L,
                        )
                        // Vollstaendig gekuerzt.
                        assertEquals(
                            "$hinweis: $bruch ist nicht vollstaendig gekuerzt",
                            1L,
                            Bruch.ggt(bruch.zaehler, bruch.nenner),
                        )
                    }

                    for (bild in aufgabe.fehlbilder) {
                        // Keine angebotene Falschantwort ist die richtige Loesung.
                        assertTrue(
                            "$hinweis: Fehlbild ${bild.kennung} traegt die richtige Loesung",
                            bild.antwort != aufgabe.loesung,
                        )
                        assertNotNull(
                            "$hinweis: Fehlerart ${bild.kennung} fehlt im Namensverzeichnis",
                            Fehlerarten.name(bild.kennung),
                        )
                    }
                    // Keine zwei Fehlbilder auf derselben Antwort -- sonst zaehlte
                    // ein einziger Fehler unter zwei Namen.
                    val antworten = aufgabe.fehlbilder.map { it.antwort }
                    assertEquals(
                        "$hinweis: zwei Fehlbilder mit derselben Antwort",
                        antworten.size,
                        antworten.toSet().size,
                    )

                    // Die Aufgabe kennt ihre Herkunft.
                    assertEquals("$hinweis: Startwert fehlt", startwert, aufgabe.startwert)
                    assertEquals("$hinweis: Niveau fehlt", niveau, aufgabe.niveau)
                    assertTrue("$hinweis: kein Rechenweg", aufgabe.weg.isNotEmpty())
                    assertTrue("$hinweis: keine Hilfen", aufgabe.hilfen.isNotEmpty())

                    // Die angezeigte Loesung laesst sich genau so eintippen.
                    val gezeigt = aufgabe.loesungText()
                    assertEquals(
                        "$hinweis: angezeigte Loesung $gezeigt liest sich nicht zurueck",
                        aufgabe.loesung,
                        Bruch.lies(gezeigt),
                    )
                    geprueft++
                }
            }
        }
        assertEquals(Katalog.alle.size * niveaus.size * STARTWERTE, geprueft)
    }

    @Test
    fun derselbeStartwertLiefertZweimalDasselbe() {
        for (kompetenz in Katalog.alle) {
            for (niveau in niveaus) {
                for (startwert in startwerte) {
                    val erste = Werkbank.erzeuge(kompetenz.kennung, niveau, startwert)
                    val zweite = Werkbank.erzeuge(kompetenz.kennung, niveau, startwert)
                    val hinweis = "${kompetenz.kennung}/$niveau/$startwert"
                    assertEquals("$hinweis: Frage weicht ab", erste.frage, zweite.frage)
                    assertEquals("$hinweis: Loesung weicht ab", erste.loesung, zweite.loesung)
                    assertEquals("$hinweis: Antwortform weicht ab", erste.form, zweite.form)
                    assertEquals("$hinweis: Rechenweg weicht ab", erste.weg, zweite.weg)
                    assertEquals("$hinweis: Hilfen weichen ab", erste.hilfen, zweite.hilfen)
                    assertEquals("$hinweis: Fehlbilder weichen ab", erste.fehlbilder, zweite.fehlbilder)
                }
            }
        }
    }

    @Test
    fun jedeKompetenzBenenntFehlerarten() {
        // Ueber tausend Aufgaben hinweg muss jeder Generator zu jedem Niveau
        // typische Fehlvorstellungen anbieten koennen.
        for (kompetenz in Katalog.alle) {
            for (niveau in niveaus) {
                val benannt = startwerte.flatMap {
                    Werkbank.erzeuge(kompetenz.kennung, niveau, it).fehlbilder.map { bild -> bild.kennung }
                }.toSet()
                assertTrue(
                    "${kompetenz.kennung}/$niveau benennt keine einzige Fehlerart",
                    benannt.isNotEmpty(),
                )
            }
        }
    }

    @Test
    fun verschiedeneStartwerteErgebenVerschiedeneAufgaben() {
        // Ein Generator, der immer dasselbe liefert, waere kein Generator.
        for (kompetenz in Katalog.alle) {
            val fragen = startwerte.take(200).map {
                Werkbank.erzeuge(kompetenz.kennung, Niveau.M, it).frage
            }
            assertTrue(
                "${kompetenz.kennung} erzeugt kaum verschiedene Aufgaben",
                fragen.toSet().size > 10,
            )
        }
    }

    @Test
    fun jedesNiveauRechnetAndersSchwer() {
        // G darf nicht dieselbe Aufgabenmenge sein wie E -- sonst waeren die
        // Niveaustufen nur Etiketten.
        for (kompetenz in Katalog.alle) {
            val grundlegend = startwerte.take(200).map {
                Werkbank.erzeuge(kompetenz.kennung, Niveau.G, it).frage
            }.toSet()
            val erweitert = startwerte.take(200).map {
                Werkbank.erzeuge(kompetenz.kennung, Niveau.E, it).frage
            }.toSet()
            assertTrue(
                "${kompetenz.kennung} erzeugt in G und E dieselben Aufgaben",
                grundlegend != erweitert,
            )
        }
    }

    // ---- Deutsche Kommaschreibweise hin und zurueck --------------------------

    @Test
    fun kommaschreibweiseUeberstehtDenWegHinUndZurueck() {
        val proben = listOf(
            Bruch.von(17, 10),
            Bruch.von(-17, 10),
            Bruch.von(1, 8),
            Bruch.von(68, 100),
            Bruch.von(3, 2),
            Bruch.von(12345, 1000),
            Bruch.von(-1, 4),
            Bruch.von(0, 5),
            Bruch.von(7),
            Bruch.von(-7),
        )
        for (wert in proben) {
            val geschrieben = wert.alsDezimalText()
            assertEquals("$geschrieben laesst sich nicht zurueckrechnen", wert, Bruch.lies(geschrieben))
        }
    }

    @Test
    fun kommaschreibweiseNutztKommaUndTypografischesMinus() {
        assertEquals("0,68", (Bruch.von(17, 10) * Bruch.von(4, 10)).alsDezimalText())
        assertEquals("1,25", Bruch.von(5, 4).alsDezimalText())
        assertEquals(Bruch.MINUS + "0,75", Bruch.von(-3, 4).alsDezimalText())
        assertTrue(
            "Der Punkt gehoert nicht in die deutsche Schreibweise",
            !Bruch.von(5, 4).alsDezimalText().contains('.'),
        )
    }

    // ---- Hilfsmittel ---------------------------------------------------------

    /** Alle Brueche einer Aufgabe: die Loesung und jede angebotene Falschantwort. */
    private fun alleBrueche(aufgabe: Aufgabe): List<Bruch> =
        listOf(aufgabe.loesung) + aufgabe.fehlbilder.map { it.antwort }
}
