package de.rechenwerk.mathe.daten

import de.rechenwerk.mathe.R
import de.rechenwerk.mathe.daten.Werkbank.bau
import de.rechenwerk.mathe.daten.Werkbank.bruchText
import de.rechenwerk.mathe.daten.Werkbank.je
import de.rechenwerk.mathe.daten.Werkbank.z
import kotlin.math.abs
import kotlin.random.Random

/**
 * Die Generatoren der Klassen 9 und 10. Sie folgen denselben Regeln wie die
 * der Unterstufe: alles exakt ueber [Bruch], jede Aufgabe aus einem Startwert,
 * jede Loesung mit einer eigenen Gegenrechnung.
 */
internal object Oberstufe {

    /** Ganzzahlige Potenz eines Bruchs -- ohne Fliesskomma. */
    private fun hoch(basis: Bruch, exponent: Int): Bruch {
        var ergebnis = Bruch.EINS
        repeat(exponent) { ergebnis *= basis }
        return ergebnis
    }

    private fun hoch(basis: Int, exponent: Int): Long {
        var ergebnis = 1L
        repeat(exponent) { ergebnis *= basis.toLong() }
        return ergebnis
    }

    /** Das erste Glied einer Gleichung, etwa "3x" oder "x". */
    private fun kopfglied(wert: Int, name: String): String =
        if (wert == 1) name else "$wert$name"

    /** Ein vorzeichenbehaftetes Glied einer Gleichung, etwa " − 7x". */
    private fun glied(wert: Int, name: String): String {
        if (wert == 0) return ""
        val zeichen = if (wert < 0) Bruch.MINUS else "+"
        val betrag = abs(wert)
        val zahl = if (name.isNotEmpty() && betrag == 1) "" else betrag.toString()
        return " $zeichen $zahl$name"
    }

    // ---- Satzgruppe des Pythagoras ------------------------------------------

    private val tripelG = listOf(3 to 4, 6 to 8, 5 to 12)
    private val tripelM = listOf(3 to 4, 5 to 12, 8 to 15, 7 to 24, 9 to 12, 12 to 16)
    private val tripelE = listOf(20 to 21, 12 to 35, 9 to 40, 28 to 45, 11 to 60, 16 to 63)

    fun pythagoras(zufall: Random, niveau: Niveau): Aufgabe {
        val topf = je(niveau, tripelG, tripelM, tripelE)
        val (rohA, rohB) = topf[zufall.nextInt(topf.size)]
        val streckung = when (niveau) {
            Niveau.G -> 1
            Niveau.M -> zufall.nextInt(1, 3)
            Niveau.E -> zufall.nextInt(1, 4)
        }
        val a = rohA * streckung
        val b = rohB * streckung
        // Ganzzahlige Hypotenuse: die Tripel sind so gewaehlt, dass sie aufgeht.
        val c = wurzelGanz(a.toLong() * a + b.toLong() * b)
        // Im erweiterten Niveau ist haeufiger eine Kathete gesucht.
        val sucheHypotenuse = when (niveau) {
            Niveau.G -> true
            Niveau.M -> zufall.nextBoolean()
            Niveau.E -> zufall.nextInt(3) == 0
        }
        return if (sucheHypotenuse) {
            val summe = a.toLong() * a + b.toLong() * b
            bau(
                kompetenz = "GE-PYT-1",
                frage = t(R.string.f_ge_pyt_hypotenuse, z(a), z(b)),
                loesung = Bruch.von(c),
                form = Form.ZAHL,
                weg = listOf(
                    t(R.string.w_pyt_satz),
                    t(R.string.w_pyt_quadrate, z(a), z(a.toLong() * a), z(b), z(b.toLong() * b)),
                    t(R.string.w_rechnung_plus, z(a.toLong() * a), z(b.toLong() * b), z(summe)),
                    t(R.string.w_pyt_wurzel, z(summe), z(c)),
                ),
                hilfen = listOf(
                    t(R.string.h_ge_pyt_1_a),
                    t(R.string.h_ge_pyt_1_b),
                    t(R.string.h_ge_pyt_1_c, z(summe)),
                ),
                fehlbilder = listOf(
                    Fehlbild("wurzel_vergessen", R.string.fa_wurzel_vergessen, Bruch.von(summe)),
                    Fehlbild("quadriert_statt_addiert", R.string.fa_quadriert_statt_addiert, Bruch.von(a + b)),
                ),
                probe = { x -> x * x == Bruch.von(a) * Bruch.von(a) + Bruch.von(b) * Bruch.von(b) },
            )
        } else {
            val rest = c * c - a.toLong() * a
            bau(
                kompetenz = "GE-PYT-1",
                frage = t(R.string.f_ge_pyt_kathete, z(c), z(a)),
                loesung = Bruch.von(b),
                form = Form.ZAHL,
                weg = listOf(
                    t(R.string.w_pyt_satz),
                    t(R.string.w_pyt_umstellen, z(c * c), z(a.toLong() * a), z(rest)),
                    t(R.string.w_pyt_wurzel, z(rest), z(b)),
                ),
                hilfen = listOf(
                    t(R.string.h_ge_pyt_1_a),
                    t(R.string.h_ge_pyt_1_d),
                    t(R.string.h_ge_pyt_1_e, z(rest)),
                ),
                fehlbilder = listOf(
                    Fehlbild("wurzel_vergessen", R.string.fa_wurzel_vergessen, Bruch.von(rest)),
                    Fehlbild("kathete_als_hypotenuse", R.string.fa_kathete_als_hypotenuse, Bruch.von(c - a)),
                ),
                probe = { x -> Bruch.von(a) * Bruch.von(a) + x * x == Bruch.von(c) * Bruch.von(c) },
            )
        }
    }

    /** Ganzzahlige Quadratwurzel -- nur fuer Quadratzahlen gedacht. */
    private fun wurzelGanz(wert: Long): Long {
        var vermutung = 1L
        while (vermutung * vermutung < wert) vermutung++
        return vermutung
    }

    // ---- Wurzelterme ---------------------------------------------------------

    fun wurzelterm(zufall: Random, niveau: Niveau): Aufgabe {
        // Im erweiterten Niveau steht ein Bruch unter der Wurzel.
        if (niveau == Niveau.E) {
            val nenner = zufall.nextInt(2, 13)
            var zaehler = zufall.nextInt(2, 13)
            while (Bruch.ggt(zaehler.toLong(), nenner.toLong()) != 1L) zaehler = zufall.nextInt(2, 13)
            val radikand = Bruch.von(zaehler * zaehler, nenner * nenner)
            val wert = Bruch.von(zaehler, nenner)
            return bau(
                kompetenz = "PO-WUR-1",
                frage = t(R.string.f_po_wur_bruch, bruchText(zaehler * zaehler, nenner * nenner)),
                loesung = wert,
                form = Form.BRUCH,
                weg = listOf(
                    t(R.string.w_wur_bruch, z(zaehler * zaehler), z(zaehler), z(nenner * nenner), z(nenner)),
                    t(R.string.w_kue_ergebnis, wert.alsBruchText()),
                ),
                hilfen = listOf(
                    t(R.string.h_po_wur_1_a),
                    t(R.string.h_po_wur_1_b),
                    t(R.string.h_po_wur_1_c, z(zaehler)),
                ),
                fehlbilder = listOf(
                    Fehlbild("wurzel_vergessen", R.string.fa_wurzel_vergessen, radikand),
                    Fehlbild("wurzel_halbiert", R.string.fa_wurzel_halbiert, radikand / Bruch.von(2)),
                ),
                probe = { x -> x * x == radikand },
            )
        }
        val k = zufall.nextInt(je(niveau, 2, 4, 4), je(niveau, 11, 26, 26))
        val quadrat = k.toLong() * k
        return bau(
            kompetenz = "PO-WUR-1",
            frage = t(R.string.f_po_wur_zahl, z(quadrat)),
            loesung = Bruch.von(k),
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_wur_quadratzahl, z(quadrat), z(k), z(k)),
                t(R.string.w_wur_ergebnis, z(k)),
            ),
            hilfen = listOf(
                t(R.string.h_po_wur_1_a),
                t(R.string.h_po_wur_1_b),
                t(R.string.h_po_wur_1_c, z(k)),
            ),
            fehlbilder = listOf(
                Fehlbild("wurzel_vergessen", R.string.fa_wurzel_vergessen, Bruch.von(quadrat)),
                Fehlbild("wurzel_halbiert", R.string.fa_wurzel_halbiert, Bruch.von(quadrat, 2L)),
            ),
            probe = { x -> x * x == Bruch.von(quadrat) },
        )
    }

    // ---- Strahlensaetze ------------------------------------------------------

    fun strahlensatz(zufall: Random, niveau: Niveau): Aufgabe {
        val a = zufall.nextInt(2, je(niveau, 6, 9, 13))
        val k = zufall.nextInt(2, je(niveau, 4, 6, 8))
        val b = a * k
        val c = zufall.nextInt(3, je(niveau, 9, 16, 25))
        val d = c * k
        // Im erweiterten Niveau ist die Strecke auf dem ersten Strahl gesucht.
        return if (niveau == Niveau.E) {
            bau(
                kompetenz = "GE-STR-1",
                frage = t(R.string.f_ge_str_erster, z(a), z(c), z(d)),
                loesung = Bruch.von(b),
                form = Form.ZAHL,
                weg = listOf(
                    t(R.string.w_str_verhaeltnis),
                    t(R.string.w_str_einsetzen_erster, z(a), z(c), z(d)),
                    t(R.string.w_str_aufloesen, z(a), z(d), z(c), z(b)),
                ),
                hilfen = listOf(
                    t(R.string.h_ge_str_1_a),
                    t(R.string.h_ge_str_1_b),
                    t(R.string.h_ge_str_1_c, z(k)),
                ),
                fehlbilder = listOf(
                    Fehlbild("verhaeltnis_gedreht", R.string.fa_verhaeltnis_gedreht, Bruch.von(a * c, d)),
                    Fehlbild("strecken_addiert", R.string.fa_strecken_addiert, Bruch.von(a + d - c)),
                ),
                probe = { x -> Bruch.von(a) * Bruch.von(d) == x * Bruch.von(c) },
            )
        } else {
            bau(
                kompetenz = "GE-STR-1",
                frage = t(R.string.f_ge_str_zweiter, z(a), z(b), z(c)),
                loesung = Bruch.von(d),
                form = Form.ZAHL,
                weg = listOf(
                    t(R.string.w_str_verhaeltnis),
                    t(R.string.w_str_einsetzen_zweiter, z(a), z(b), z(c)),
                    t(R.string.w_str_aufloesen, z(b), z(c), z(a), z(d)),
                ),
                hilfen = listOf(
                    t(R.string.h_ge_str_1_a),
                    t(R.string.h_ge_str_1_b),
                    t(R.string.h_ge_str_1_c, z(k)),
                ),
                fehlbilder = listOf(
                    Fehlbild("verhaeltnis_gedreht", R.string.fa_verhaeltnis_gedreht, Bruch.von(a * c, b)),
                    Fehlbild("strecken_addiert", R.string.fa_strecken_addiert, Bruch.von(c + b - a)),
                ),
                probe = { x -> Bruch.von(a) * x == Bruch.von(b) * Bruch.von(c) },
            )
        }
    }

    // ---- Zinsrechnung ueber mehrere Schritte ---------------------------------

    fun zinsen(zufall: Random, niveau: Niveau): Aufgabe {
        val kapital = 100 * zufall.nextInt(je(niveau, 1, 5, 10), je(niveau, 21, 31, 51))
        val satz = zufall.nextInt(2, je(niveau, 6, 7, 9))
        val jahre = je(niveau, 2, 2, 3)
        val start = Bruch.von(kapital)
        val zinsfuss = Bruch.von(satz.toLong(), 100L)
        return if (niveau == Niveau.G) {
            // Einfache Verzinsung: die Zinsen werden nicht mitverzinst.
            val zinsenProJahr = start * zinsfuss
            val ende = start + zinsenProJahr * Bruch.von(jahre)
            bau(
                kompetenz = "PZ-ZIN-1",
                frage = t(R.string.f_pz_zin_einfach, z(kapital), z(jahre), z(satz)),
                loesung = ende,
                form = Form.ZAHL,
                weg = listOf(
                    t(R.string.w_zin_ein_jahr, z(satz), z(kapital), zinsenProJahr.alsDezimalText()),
                    t(R.string.w_zin_mal_jahre, zinsenProJahr.alsDezimalText(), z(jahre), (zinsenProJahr * Bruch.von(jahre)).alsDezimalText()),
                    t(R.string.w_rechnung_plus, z(kapital), (zinsenProJahr * Bruch.von(jahre)).alsDezimalText(), ende.alsDezimalText()),
                ),
                hilfen = listOf(
                    t(R.string.h_pz_zin_1_a),
                    t(R.string.h_pz_zin_1_b),
                    t(R.string.h_pz_zin_1_c, zinsenProJahr.alsDezimalText()),
                ),
                fehlbilder = listOf(
                    Fehlbild("nur_einmal_verzinst", R.string.fa_nur_einmal_verzinst, start + zinsenProJahr),
                    Fehlbild("durch_hundert_vergessen", R.string.fa_durch_hundert_vergessen, start + Bruch.von(satz) * Bruch.von(jahre)),
                ),
                probe = { x -> x - start == start * zinsfuss * Bruch.von(jahre) },
            )
        } else {
            // Zinseszins: jedes Jahr derselbe Faktor auf das gewachsene Kapital.
            val faktor = Bruch.EINS + zinsfuss
            var stand = start
            val schritte = mutableListOf<Txt>()
            for (jahr in 1..jahre) {
                stand *= faktor
                schritte += t(R.string.w_zin_jahr, z(jahr), stand.alsDezimalText())
            }
            val ende = stand
            bau(
                kompetenz = "PZ-ZIN-1",
                frage = t(R.string.f_pz_zin_zinseszins, z(kapital), z(jahre), z(satz)),
                loesung = ende,
                form = Form.ZAHL,
                weg = listOf(t(R.string.w_zin_faktor, faktor.alsDezimalText())) + schritte,
                hilfen = listOf(
                    t(R.string.h_pz_zin_1_a),
                    t(R.string.h_pz_zin_1_d),
                    t(R.string.h_pz_zin_1_e, faktor.alsDezimalText()),
                ),
                fehlbilder = listOf(
                    Fehlbild("nur_einmal_verzinst", R.string.fa_nur_einmal_verzinst, start * faktor),
                    Fehlbild(
                        "zinseszins_als_summe",
                        R.string.fa_zinseszins_als_summe,
                        start + start * zinsfuss * Bruch.von(jahre),
                    ),
                ),
                // Jahr fuer Jahr zurueckrechnen muss genau das Startkapital ergeben.
                probe = { x -> (1..jahre).fold(x) { stand2, _ -> stand2 / faktor } == start },
            )
        }
    }

    // ---- Quadratische Gleichungen --------------------------------------------

    fun quadratisch(zufall: Random, niveau: Niveau): Aufgabe {
        val spanne = je(niveau, 6, 9, 12)
        var r1 = wurzelWahl(zufall, niveau, spanne)
        var r2 = wurzelWahl(zufall, niveau, spanne)
        if (r1 == r2) r2 = if (r2 < spanne) r2 + 1 else r2 - 1
        if (r1 > r2) {
            val hilf = r1; r1 = r2; r2 = hilf
        }
        val p = -(r1 + r2)
        val q = r1 * r2
        val gleichung = "x²" + glied(p, "x") + glied(q, "") + " = 0"
        return bau(
            kompetenz = "GL-QUA-1",
            frage = t(R.string.f_gl_qua_groessere, gleichung),
            loesung = Bruch.von(r2),
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_qua_vieta, z(-p), z(q)),
                t(R.string.w_qua_zerlegt, z(r1), z(r2)),
                t(R.string.w_qua_produkt, z(r1), z(r2)),
                t(R.string.w_qua_groesser, z(r2)),
            ),
            hilfen = listOf(
                t(R.string.h_gl_qua_1_a),
                t(R.string.h_gl_qua_1_b, z(-p), z(q)),
                t(R.string.h_gl_qua_1_c, z(r1)),
            ),
            fehlbilder = listOf(
                Fehlbild("zweite_loesung_genommen", R.string.fa_zweite_loesung_genommen, Bruch.von(r1)),
                Fehlbild("vorzeichen_der_wurzel", R.string.fa_vorzeichen_der_wurzel, Bruch.von(-r2)),
            ),
            // Einsetzprobe in die urspruengliche Gleichung.
            probe = { x -> x * x + Bruch.von(p) * x + Bruch.von(q) == Bruch.NULL },
        )
    }

    /** Eine Loesung der quadratischen Gleichung; erst E laesst sie negativ werden. */
    private fun wurzelWahl(zufall: Random, niveau: Niveau, spanne: Int): Int {
        val betrag = zufall.nextInt(1, spanne + 1)
        return if (niveau == Niveau.E && zufall.nextBoolean()) -betrag else betrag
    }

    // ---- Flaechen und Koerper ------------------------------------------------

    fun koerper(zufall: Random, niveau: Niveau): Aufgabe = when (niveau) {
        Niveau.G -> rechteck(zufall)
        Niveau.M -> if (zufall.nextBoolean()) dreieck(zufall) else quader(zufall)
        Niveau.E -> restkoerper(zufall)
    }

    private fun rechteck(zufall: Random): Aufgabe {
        val laenge = zufall.nextInt(4, 21)
        val breite = zufall.nextInt(2, 15)
        val flaeche = laenge * breite
        return bau(
            kompetenz = "GE-KOE-1",
            frage = t(R.string.f_ge_koe_rechteck, z(laenge), z(breite)),
            loesung = Bruch.von(flaeche),
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_koe_rechteck),
                t(R.string.w_rechnung_mal, z(laenge), z(breite), z(flaeche)),
            ),
            hilfen = listOf(
                t(R.string.h_ge_koe_1_a),
                t(R.string.h_ge_koe_1_b),
                t(R.string.h_ge_koe_1_c, z(laenge), z(breite)),
            ),
            fehlbilder = listOf(
                Fehlbild("addiert_statt_mal", R.string.fa_addiert_statt_mal, Bruch.von(laenge + breite)),
                Fehlbild("halbierung_vergessen", R.string.fa_halbierung_vergessen, Bruch.von(2 * (laenge + breite))),
            ),
            probe = { x -> x / Bruch.von(laenge) == Bruch.von(breite) },
        )
    }

    private fun dreieck(zufall: Random): Aufgabe {
        // Gerade Grundseite: die Haelfte bleibt eine ganze Zahl.
        val grund = 2 * zufall.nextInt(2, 16)
        val hoehe = zufall.nextInt(3, 19)
        val flaeche = grund * hoehe / 2
        return bau(
            kompetenz = "GE-KOE-1",
            frage = t(R.string.f_ge_koe_dreieck, z(grund), z(hoehe)),
            loesung = Bruch.von(flaeche),
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_koe_dreieck),
                t(R.string.w_rechnung_mal, z(grund), z(hoehe), z(grund * hoehe)),
                t(R.string.w_koe_halbieren, z(grund * hoehe), z(flaeche)),
            ),
            hilfen = listOf(
                t(R.string.h_ge_koe_1_a),
                t(R.string.h_ge_koe_1_d),
                t(R.string.h_ge_koe_1_e, z(grund * hoehe)),
            ),
            fehlbilder = listOf(
                Fehlbild("halbierung_vergessen", R.string.fa_halbierung_vergessen, Bruch.von(grund * hoehe)),
                Fehlbild("addiert_statt_mal", R.string.fa_addiert_statt_mal, Bruch.von(grund + hoehe)),
            ),
            probe = { x -> x * Bruch.von(2) == Bruch.von(grund) * Bruch.von(hoehe) },
        )
    }

    private fun quader(zufall: Random): Aufgabe {
        val laenge = zufall.nextInt(3, 16)
        val breite = zufall.nextInt(2, 13)
        val hoehe = zufall.nextInt(2, 13)
        val volumen = laenge * breite * hoehe
        return bau(
            kompetenz = "GE-KOE-1",
            frage = t(R.string.f_ge_koe_quader, z(laenge), z(breite), z(hoehe)),
            loesung = Bruch.von(volumen),
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_koe_quader),
                t(R.string.w_rechnung_mal, z(laenge), z(breite), z(laenge * breite)),
                t(R.string.w_rechnung_mal, z(laenge * breite), z(hoehe), z(volumen)),
            ),
            hilfen = listOf(
                t(R.string.h_ge_koe_1_a),
                t(R.string.h_ge_koe_1_f),
                t(R.string.h_ge_koe_1_g, z(laenge * breite)),
            ),
            fehlbilder = listOf(
                Fehlbild("flaeche_statt_volumen", R.string.fa_flaeche_statt_volumen, Bruch.von(laenge * breite)),
                Fehlbild("addiert_statt_mal", R.string.fa_addiert_statt_mal, Bruch.von(laenge + breite + hoehe)),
            ),
            probe = { x -> x / Bruch.von(laenge) / Bruch.von(breite) == Bruch.von(hoehe) },
        )
    }

    /** Mehrschrittig: aus einem Quader ist ein kleinerer Quader ausgespart. */
    private fun restkoerper(zufall: Random): Aufgabe {
        val laenge = zufall.nextInt(8, 21)
        val breite = zufall.nextInt(6, 16)
        val hoehe = zufall.nextInt(4, 13)
        val kl = zufall.nextInt(2, laenge / 2 + 1)
        val kb = zufall.nextInt(2, breite / 2 + 1)
        val kh = zufall.nextInt(2, hoehe / 2 + 1)
        val gross = laenge * breite * hoehe
        val klein = kl * kb * kh
        val rest = gross - klein
        return bau(
            kompetenz = "GE-KOE-1",
            frage = t(
                R.string.f_ge_koe_rest,
                z(laenge), z(breite), z(hoehe), z(kl), z(kb), z(kh),
            ),
            loesung = Bruch.von(rest),
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_koe_quader),
                t(R.string.w_koe_gross, z(laenge), z(breite), z(hoehe), z(gross)),
                t(R.string.w_koe_klein, z(kl), z(kb), z(kh), z(klein)),
                t(R.string.w_rechnung_minus, z(gross), z(klein), z(rest)),
            ),
            hilfen = listOf(
                t(R.string.h_ge_koe_1_a),
                t(R.string.h_ge_koe_1_h),
                t(R.string.h_ge_koe_1_i, z(gross)),
            ),
            fehlbilder = listOf(
                Fehlbild("flaeche_statt_volumen", R.string.fa_flaeche_statt_volumen, Bruch.von(gross)),
                Fehlbild("halbierung_vergessen", R.string.fa_halbierung_vergessen, Bruch.von(gross + klein)),
            ),
            probe = { x -> x + Bruch.von(klein) == Bruch.von(gross) },
        )
    }

    // ---- Lineare Gleichungssysteme -------------------------------------------

    fun gleichungssystem(zufall: Random, niveau: Niveau): Aufgabe {
        val spanne = je(niveau, 4, 6, 9)
        val a1 = zufall.nextInt(1, spanne)
        val b1 = zufall.nextInt(1, spanne)
        val a2 = zufall.nextInt(1, spanne)
        // Ohne eigene Steigung haette das System keine eindeutige Loesung:
        // b2 wird so lange weitergedreht, bis die Determinante nicht null ist.
        var b2 = zufall.nextInt(1, spanne)
        while (a1 * b2 == a2 * b1) b2 = if (b2 < spanne - 1) b2 + 1 else 1
        val betragX = zufall.nextInt(1, je(niveau, 7, 9, 13))
        val betragY = zufall.nextInt(1, je(niveau, 7, 9, 13))
        val x = if (niveau == Niveau.E && zufall.nextBoolean()) -betragX else betragX
        val y = if (niveau == Niveau.E && zufall.nextBoolean()) -betragY else betragY
        val c1 = a1 * x + b1 * y
        val c2 = a2 * x + b2 * y
        val nenner = a1 * b2 - a2 * b1
        val zaehler = c1 * b2 - c2 * b1
        val ersteGleichung = "${kopfglied(a1, "x")}${glied(b1, "y")} = ${z(c1)}"
        val zweiteGleichung = "${kopfglied(a2, "x")}${glied(b2, "y")} = ${z(c2)}"
        return bau(
            kompetenz = "GL-LGS-1",
            frage = t(R.string.f_gl_lgs_x, ersteGleichung, zweiteGleichung),
            loesung = Bruch.von(x),
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_lgs_gleichnamig, z(b2), z(b1)),
                t(R.string.w_lgs_y_faellt_weg, z(nenner), z(zaehler)),
                t(R.string.w_lgs_x, z(x)),
                t(R.string.w_lgs_y, z(y)),
            ),
            hilfen = listOf(
                t(R.string.h_gl_lgs_1_a),
                t(R.string.h_gl_lgs_1_b),
                t(R.string.h_gl_lgs_1_c, z(nenner), z(zaehler)),
            ),
            fehlbilder = listOf(
                Fehlbild("nur_eine_gleichung", R.string.fa_nur_eine_gleichung, Bruch.von(c1, a1)),
                Fehlbild("x_und_y_vertauscht", R.string.fa_x_und_y_vertauscht, Bruch.von(y)),
            ),
            // Erst y aus der ersten Gleichung holen, dann die zweite pruefen.
            probe = { loesung ->
                val ausErster = (Bruch.von(c1) - Bruch.von(a1) * loesung) / Bruch.von(b1)
                Bruch.von(a2) * loesung + Bruch.von(b2) * ausErster == Bruch.von(c2)
            },
        )
    }

    // ---- Potenzen mit rationalen Exponenten ----------------------------------

    fun rationalerExponent(zufall: Random, niveau: Niveau): Aufgabe {
        val basis = zufall.nextInt(2, je(niveau, 5, 6, 6))
        val nenner = zufall.nextInt(2, je(niveau, 3, 4, 4))
        val zaehler = zufall.nextInt(1, je(niveau, 2, 4, 5))
        val negativ = niveau == Niveau.E && zufall.nextBoolean()
        val grundzahl = hoch(basis, nenner)
        val potenz = hoch(basis, zaehler)
        val wert = if (negativ) Bruch.EINS / Bruch.von(potenz) else Bruch.von(potenz)
        val exponentText = (if (negativ) Bruch.MINUS else "") + bruchText(zaehler, nenner)
        val zielPotenz = hoch(Bruch.von(grundzahl), zaehler)
        return bau(
            kompetenz = "PO-RAT-1",
            frage = t(R.string.f_po_rat_hoch, z(grundzahl), exponentText),
            loesung = wert,
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_rat_wurzel, z(nenner), z(grundzahl), z(basis)),
                t(R.string.w_rat_potenz, z(basis), z(zaehler), z(potenz)),
            ) + if (negativ) listOf(t(R.string.w_rat_negativ, wert.alsBruchText())) else emptyList(),
            hilfen = listOf(
                t(R.string.h_po_rat_1_a),
                t(R.string.h_po_rat_1_b, z(nenner), z(zaehler)),
                t(R.string.h_po_rat_1_c, z(basis)),
            ),
            fehlbilder = listOf(
                Fehlbild("wurzel_vergessen", R.string.fa_wurzel_vergessen, zielPotenz),
                Fehlbild(
                    "exponent_als_faktor",
                    R.string.fa_exponent_als_faktor,
                    Bruch.von(grundzahl) * Bruch.von(zaehler, nenner),
                ),
            ),
            // Die Antwort hoch dem Nenner muss die Grundzahl hoch dem Zaehler ergeben.
            probe = { x ->
                if (negativ) hoch(x, nenner) * zielPotenz == Bruch.EINS else hoch(x, nenner) == zielPotenz
            },
        )
    }
}
