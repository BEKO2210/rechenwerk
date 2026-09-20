package de.rechenwerk.mathe.daten

import androidx.annotation.StringRes
import de.rechenwerk.mathe.R
import kotlin.math.abs
import kotlin.random.Random

/**
 * Ein Text, der erst im Bildschirm aufgeloest wird: Kennung aus strings.xml
 * plus die eingesetzten Zahlen. So bleibt jede sichtbare Zeichenkette in den
 * Ressourcen, obwohl die Aufgaben prozedural entstehen.
 */
data class Txt(@StringRes val id: Int, val teile: List<String> = emptyList())

internal fun t(@StringRes id: Int, vararg teile: String) = Txt(id, teile.toList())

/** Erwartete Antwortform -- steuert nur den Hinweis unter dem Eingabefeld. */
enum class Form { ZAHL, BRUCH, PROZENT }

/** Eine benannte typische Fehlvorstellung samt der Antwort, die sie erzeugt. */
data class Fehlbild(val kennung: String, @StringRes val name: Int, val antwort: Bruch)

data class Aufgabe(
    val kompetenz: String,
    val frage: Txt,
    val loesung: Bruch,
    val form: Form,
    val weg: List<Txt>,
    val hilfen: List<Txt>,
    val fehlbilder: List<Fehlbild>,
    /** Der Startwert, aus dem diese Aufgabe entstanden ist. */
    val startwert: Long = 0L,
    /** Das Niveau, mit dem sie erzeugt wurde. */
    val niveau: Niveau = Niveau.M,
    /**
     * Die Gegenrechnung: setzt eine Antwort in die urspruenglichen Zahlen ein
     * und sagt, ob die Aufgabe damit aufgeht. Sie laeuft ueber einen anderen
     * Rechenweg als der Generator -- die Eigenschaftstests pruefen damit, dass
     * jede erzeugte Loesung wirklich stimmt.
     */
    val probe: (Bruch) -> Boolean = { true },
) {
    /** Die Loesung so geschrieben, wie sie erwartet wird. */
    fun loesungText(): String = when (form) {
        Form.BRUCH -> loesung.alsBruchText()
        else -> loesung.alsText()
    }

    fun fehlbildZu(antwort: Bruch): Fehlbild? = fehlbilder.firstOrNull { it.antwort == antwort }
}

/**
 * Namen aller Fehlerarten. Gespeichert wird nur die Kennung; die Bildschirme
 * schlagen hier den lesbaren Namen nach.
 */
object Fehlerarten {

    val namen: Map<String, Int> = mapOf(
        "reihe_verzaehlt" to R.string.fa_reihe_verzaehlt,
        "addiert_statt_mal" to R.string.fa_addiert_statt_mal,
        "uebertrag_vergessen" to R.string.fa_uebertrag_vergessen,
        "um_zehn_daneben" to R.string.fa_um_zehn_daneben,
        "entbuendeln_vergessen" to R.string.fa_entbuendeln_vergessen,
        "divisor_als_ergebnis" to R.string.fa_divisor_als_ergebnis,
        "subtrahiert_statt_geteilt" to R.string.fa_subtrahiert_statt_geteilt,
        "einer_vergessen" to R.string.fa_einer_vergessen,
        "stellenwert_verloren" to R.string.fa_stellenwert_verloren,
        "stellen_nicht_ausgerichtet" to R.string.fa_stellen_nicht_ausgerichtet,
        "komma_verrutscht" to R.string.fa_komma_verrutscht,
        "komma_zu_weit_rechts" to R.string.fa_komma_zu_weit_rechts,
        "komma_zu_weit_links" to R.string.fa_komma_zu_weit_links,
        "bruchstrich_verwechselt" to R.string.fa_bruchstrich_verwechselt,
        "nur_zaehler_gekuerzt" to R.string.fa_nur_zaehler_gekuerzt,
        "nicht_fertig_gekuerzt" to R.string.fa_nicht_fertig_gekuerzt,
        "zaehler_und_nenner_verrechnet" to R.string.fa_zaehler_und_nenner_verrechnet,
        "nenner_erweitert_zaehler_nicht" to R.string.fa_nenner_erweitert_zaehler_nicht,
        "kreuzweise_multipliziert" to R.string.fa_kreuzweise_multipliziert,
        "nur_zaehler_multipliziert" to R.string.fa_nur_zaehler_multipliziert,
        "kehrwert_vergessen" to R.string.fa_kehrwert_vergessen,
        "falschen_bruch_gedreht" to R.string.fa_falschen_bruch_gedreht,
        "vorzeichen_vertauscht" to R.string.fa_vorzeichen_vertauscht,
        "minus_uebersehen" to R.string.fa_minus_uebersehen,
        "vorzeichenregel_verwechselt" to R.string.fa_vorzeichenregel_verwechselt,
        "durch_hundert_vergessen" to R.string.fa_durch_hundert_vergessen,
        "geteilt_statt_mal" to R.string.fa_geteilt_statt_mal,
        "bezug_vertauscht" to R.string.fa_bezug_vertauscht,
        "mal_hundert_vergessen" to R.string.fa_mal_hundert_vergessen,
        "punkt_vor_strich_missachtet" to R.string.fa_punkt_vor_strich_missachtet,
        "mal_als_plus" to R.string.fa_mal_als_plus,
        "summand_uebersehen" to R.string.fa_summand_uebersehen,
        "falsche_gegenrechnung" to R.string.fa_falsche_gegenrechnung,
        "klammer_nicht_beachtet" to R.string.fa_klammer_nicht_beachtet,
        "falsches_vorzeichen_umgestellt" to R.string.fa_falsches_vorzeichen_umgestellt,
        "quadriert_statt_addiert" to R.string.fa_quadriert_statt_addiert,
        "wurzel_vergessen" to R.string.fa_wurzel_vergessen,
        "kathete_als_hypotenuse" to R.string.fa_kathete_als_hypotenuse,
        "wurzel_halbiert" to R.string.fa_wurzel_halbiert,
        "verhaeltnis_gedreht" to R.string.fa_verhaeltnis_gedreht,
        "strecken_addiert" to R.string.fa_strecken_addiert,
        "nur_einmal_verzinst" to R.string.fa_nur_einmal_verzinst,
        "zinseszins_als_summe" to R.string.fa_zinseszins_als_summe,
        "zweite_loesung_genommen" to R.string.fa_zweite_loesung_genommen,
        "vorzeichen_der_wurzel" to R.string.fa_vorzeichen_der_wurzel,
        "nur_eine_gleichung" to R.string.fa_nur_eine_gleichung,
        "x_und_y_vertauscht" to R.string.fa_x_und_y_vertauscht,
        "exponent_als_faktor" to R.string.fa_exponent_als_faktor,
        "halbierung_vergessen" to R.string.fa_halbierung_vergessen,
        "flaeche_statt_volumen" to R.string.fa_flaeche_statt_volumen,
    )

    fun name(kennung: String): Int? = namen[kennung]
}

/**
 * Erzeugt Aufgaben. Jede Kompetenz hat einen eigenen Generator: es gibt keine
 * feste Aufgabenliste, nur Bauplaene mit Zufallszahlen in sinnvollen Grenzen.
 *
 * Jede Aufgabe entsteht aus genau einem Startwert. Derselbe Startwert und
 * dieselbe Kompetenz ergeben bei gleichem Niveau immer exakt dieselbe Aufgabe.
 */
object Werkbank {

    fun erzeuge(kennung: String, niveau: Niveau, startwert: Long): Aufgabe {
        val zufall = Random(startwert)
        val roh = when (kennung) {
            "GR-MUL-1" -> einmaleins(zufall, niveau)
            "GR-ADD-1" -> addierenBisTausend(zufall, niveau)
            "GR-DIV-1" -> teilen(zufall, niveau)
            "GR-MUL-2" -> mehrstelligMal(zufall, niveau)
            "DZ-ADD-1" -> dezimalAddieren(zufall, niveau)
            "DZ-MUL-1" -> dezimalMal(zufall, niveau)
            "DZ-BRU-1" -> bruchAlsDezimal(zufall, niveau)
            "BR-KUE-1" -> kuerzen(zufall, niveau)
            "BR-ADD-1" -> bruecheAddieren(zufall, niveau)
            "BR-MUL-1" -> bruecheMal(zufall, niveau)
            "BR-DIV-1" -> bruecheGeteilt(zufall, niveau)
            "NZ-ADD-1" -> negativAddieren(zufall, niveau)
            "NZ-MUL-1" -> negativMal(zufall, niveau)
            "PZ-VON-1" -> prozentwert(zufall, niveau)
            "PZ-SAT-1" -> prozentsatz(zufall, niveau)
            "GL-TER-1" -> termAuswerten(zufall, niveau)
            "GL-LIN-1" -> gleichungLinear(zufall, niveau)
            "GL-KLA-1" -> gleichungKlammer(zufall, niveau)
            "GE-PYT-1" -> Oberstufe.pythagoras(zufall, niveau)
            "PO-WUR-1" -> Oberstufe.wurzelterm(zufall, niveau)
            "GE-STR-1" -> Oberstufe.strahlensatz(zufall, niveau)
            "PZ-ZIN-1" -> Oberstufe.zinsen(zufall, niveau)
            "GL-QUA-1" -> Oberstufe.quadratisch(zufall, niveau)
            "GE-KOE-1" -> Oberstufe.koerper(zufall, niveau)
            "GL-LGS-1" -> Oberstufe.gleichungssystem(zufall, niveau)
            "PO-RAT-1" -> Oberstufe.rationalerExponent(zufall, niveau)
            else -> einmaleins(zufall, niveau)
        }
        return roh.copy(startwert = startwert, niveau = niveau)
    }

    // ---- Bauhilfen ----------------------------------------------------------

    /** Wert je Niveau -- die eine Stelle, an der sich G, M und E unterscheiden. */
    internal fun <T> je(niveau: Niveau, g: T, m: T, e: T): T = when (niveau) {
        Niveau.G -> g
        Niveau.M -> m
        Niveau.E -> e
    }

    internal fun bau(
        kompetenz: String,
        frage: Txt,
        loesung: Bruch,
        form: Form,
        weg: List<Txt>,
        hilfen: List<Txt>,
        fehlbilder: List<Fehlbild>,
        probe: (Bruch) -> Boolean,
    ): Aufgabe {
        val sauber = LinkedHashMap<Bruch, Fehlbild>()
        for (bild in fehlbilder) {
            if (bild.antwort == loesung) continue
            if (!sauber.containsKey(bild.antwort)) sauber[bild.antwort] = bild
        }
        return Aufgabe(
            kompetenz = kompetenz,
            frage = frage,
            loesung = loesung,
            form = form,
            weg = weg,
            hilfen = hilfen,
            fehlbilder = sauber.values.toList(),
            probe = probe,
        )
    }

    internal fun z(wert: Long): String = Bruch.ganzText(wert)

    internal fun z(wert: Int): String = Bruch.ganzText(wert.toLong())

    /** Bruch in der Schreibweise der Aufgabentexte, mit typografischem Minus. */
    internal fun bruchText(zaehler: Int, nenner: Int): String = "${z(zaehler)}/$nenner"

    private fun kgv(a: Int, b: Int): Int = (a / Bruch.ggt(a.toLong(), b.toLong()).toInt()) * b

    /** Zehnerpotenz als Long -- fuer die Dezimalstellen. */
    private fun zehn(stellen: Int): Long {
        var wert = 1L
        repeat(stellen) { wert *= 10L }
        return wert
    }

    /** Ziffernweise addieren ohne Uebertrag -- so rechnet, wer den Uebertrag vergisst. */
    private fun ohneUebertrag(a: Int, b: Int): Int {
        var stelle = 1
        var ergebnis = 0
        var x = a
        var y = b
        while (x > 0 || y > 0) {
            ergebnis += ((x % 10 + y % 10) % 10) * stelle
            x /= 10
            y /= 10
            stelle *= 10
        }
        return ergebnis
    }

    /** Ziffernweise die kleinere von der groesseren Ziffer -- der klassische Entbuendelungsfehler. */
    private fun ziffernBetrag(a: Int, b: Int): Int {
        var stelle = 1
        var ergebnis = 0
        var x = a
        var y = b
        while (x > 0 || y > 0) {
            ergebnis += abs(x % 10 - y % 10) * stelle
            x /= 10
            y /= 10
            stelle *= 10
        }
        return ergebnis
    }

    internal fun geklammert(wert: Int): String =
        if (wert < 0) "(" + Bruch.ganzText(wert.toLong()) + ")" else wert.toString()

    // ---- Grundrechnen -------------------------------------------------------

    private fun einmaleins(zufall: Random, niveau: Niveau): Aufgabe {
        val a = zufall.nextInt(je(niveau, 2, 3, 4), je(niveau, 7, 11, 14))
        val b = zufall.nextInt(je(niveau, 2, 4, 6), je(niveau, 7, 11, 16))
        val ergebnis = a * b
        val vorstufe = a * (b - 1)
        return bau(
            kompetenz = "GR-MUL-1",
            frage = t(R.string.f_mal, z(a), z(b)),
            loesung = Bruch.von(ergebnis),
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_mal_zerlegen, z(a), z(b), z(b - 1)),
                t(R.string.w_rechnung_mal, z(a), z(b - 1), z(vorstufe)),
                t(R.string.w_rechnung_plus, z(vorstufe), z(a), z(ergebnis)),
            ),
            hilfen = listOf(
                t(R.string.h_gr_mul_1_a),
                t(R.string.h_gr_mul_1_b, z(a), z(b - 1)),
                t(R.string.h_gr_mul_1_c, z(a), z(b - 1), z(vorstufe)),
            ),
            fehlbilder = listOf(
                Fehlbild("reihe_verzaehlt", R.string.fa_reihe_verzaehlt, Bruch.von(vorstufe)),
                Fehlbild("addiert_statt_mal", R.string.fa_addiert_statt_mal, Bruch.von(a + b)),
            ),
            probe = { x -> x / Bruch.von(a) == Bruch.von(b) },
        )
    }

    private fun addierenBisTausend(zufall: Random, niveau: Niveau): Aufgabe {
        val plus = zufall.nextBoolean()
        return if (plus) {
            // Einerziffern beider Zahlen zusammen ueber zehn: der Uebertrag ist Pflicht.
            val a = zufall.nextInt(je(niveau, 2, 12, 120), je(niveau, 9, 88, 880)) * 10 + zufall.nextInt(5, 10)
            val b = zufall.nextInt(je(niveau, 1, 3, 30), je(niveau, 8, 29, 290)) * 10 + zufall.nextInt(5, 10)
            val zehner = b - b % 10
            val einer = b % 10
            val mitte = a + zehner
            val ergebnis = a + b
            bau(
                kompetenz = "GR-ADD-1",
                frage = t(R.string.f_plus, z(a), z(b)),
                loesung = Bruch.von(ergebnis),
                form = Form.ZAHL,
                weg = listOf(
                    t(R.string.w_add_zerlegen, z(b), z(zehner), z(einer)),
                    t(R.string.w_rechnung_plus, z(a), z(zehner), z(mitte)),
                    t(R.string.w_rechnung_plus, z(mitte), z(einer), z(ergebnis)),
                ),
                hilfen = listOf(
                    t(R.string.h_gr_add_1_a),
                    t(R.string.h_gr_add_1_b, z(zehner), z(einer)),
                    t(R.string.h_rechnung_plus, z(a), z(zehner), z(mitte)),
                ),
                fehlbilder = listOf(
                    Fehlbild("uebertrag_vergessen", R.string.fa_uebertrag_vergessen, Bruch.von(ohneUebertrag(a, b))),
                    Fehlbild("um_zehn_daneben", R.string.fa_um_zehn_daneben, Bruch.von(ergebnis - 10)),
                ),
                probe = { x -> x - Bruch.von(b) == Bruch.von(a) },
            )
        } else {
            // Obere Einerziffer kleiner als die untere: es muss entbuendelt werden.
            val a = zufall.nextInt(je(niveau, 4, 32, 320), je(niveau, 9, 95, 950)) * 10 + zufall.nextInt(0, 5)
            val b = zufall.nextInt(je(niveau, 1, 4, 40), je(niveau, 3, 29, 290)) * 10 + zufall.nextInt(5, 10)
            val zehner = b - b % 10
            val einer = b % 10
            val mitte = a - zehner
            val ergebnis = a - b
            bau(
                kompetenz = "GR-ADD-1",
                frage = t(R.string.f_minus, z(a), z(b)),
                loesung = Bruch.von(ergebnis),
                form = Form.ZAHL,
                weg = listOf(
                    t(R.string.w_add_zerlegen, z(b), z(zehner), z(einer)),
                    t(R.string.w_rechnung_minus, z(a), z(zehner), z(mitte)),
                    t(R.string.w_rechnung_minus, z(mitte), z(einer), z(ergebnis)),
                ),
                hilfen = listOf(
                    t(R.string.h_gr_sub_1_a),
                    t(R.string.h_gr_sub_1_b, z(zehner), z(einer)),
                    t(R.string.h_rechnung_minus, z(a), z(zehner), z(mitte)),
                ),
                fehlbilder = listOf(
                    Fehlbild("entbuendeln_vergessen", R.string.fa_entbuendeln_vergessen, Bruch.von(ziffernBetrag(a, b))),
                    Fehlbild("um_zehn_daneben", R.string.fa_um_zehn_daneben, Bruch.von(ergebnis + 10)),
                ),
                probe = { x -> x + Bruch.von(b) == Bruch.von(a) },
            )
        }
    }

    private fun teilen(zufall: Random, niveau: Niveau): Aufgabe {
        val teiler = zufall.nextInt(je(niveau, 2, 3, 4), je(niveau, 7, 13, 17))
        // Ab drei: sonst faellt bei 4 : 2 der Fehlbildwert mit der Loesung zusammen.
        val ergebnis = zufall.nextInt(je(niveau, 3, 3, 4), je(niveau, 8, 13, 17))
        val ganzes = teiler * ergebnis
        return bau(
            kompetenz = "GR-DIV-1",
            frage = t(R.string.f_geteilt, z(ganzes), z(teiler)),
            loesung = Bruch.von(ergebnis),
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_div_umkehr, z(teiler), z(ganzes)),
                t(R.string.w_rechnung_mal, z(teiler), z(ergebnis), z(ganzes)),
                t(R.string.w_div_ergebnis, z(ganzes), z(teiler), z(ergebnis)),
            ),
            hilfen = listOf(
                t(R.string.h_gr_div_1_a),
                t(R.string.h_gr_div_1_b, z(teiler), z(ganzes)),
                t(R.string.h_gr_div_1_c, z(teiler), z(ergebnis - 1), z(teiler * (ergebnis - 1))),
            ),
            fehlbilder = listOf(
                Fehlbild("divisor_als_ergebnis", R.string.fa_divisor_als_ergebnis, Bruch.von(teiler)),
                Fehlbild("subtrahiert_statt_geteilt", R.string.fa_subtrahiert_statt_geteilt, Bruch.von(ganzes - teiler)),
            ),
            probe = { x -> x * Bruch.von(teiler) == Bruch.von(ganzes) },
        )
    }

    private fun mehrstelligMal(zufall: Random, niveau: Niveau): Aufgabe {
        val a = zufall.nextInt(je(niveau, 11, 13, 21), je(niveau, 21, 30, 61))
        val b = zufall.nextInt(je(niveau, 2, 3, 4), je(niveau, 7, 10, 14))
        val zehner = a - a % 10
        val einer = a % 10
        val teilZehner = zehner * b
        val teilEiner = einer * b
        val ergebnis = a * b
        return bau(
            kompetenz = "GR-MUL-2",
            frage = t(R.string.f_mal, z(a), z(b)),
            loesung = Bruch.von(ergebnis),
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_add_zerlegen, z(a), z(zehner), z(einer)),
                t(R.string.w_rechnung_mal, z(zehner), z(b), z(teilZehner)),
                t(R.string.w_rechnung_mal, z(einer), z(b), z(teilEiner)),
                t(R.string.w_rechnung_plus, z(teilZehner), z(teilEiner), z(ergebnis)),
            ),
            hilfen = listOf(
                t(R.string.h_gr_mul_2_a),
                t(R.string.h_gr_mul_2_b, z(zehner), z(b), z(einer)),
                t(R.string.h_rechnung_mal, z(zehner), z(b), z(teilZehner)),
            ),
            fehlbilder = listOf(
                Fehlbild("einer_vergessen", R.string.fa_einer_vergessen, Bruch.von(teilZehner)),
                Fehlbild("stellenwert_verloren", R.string.fa_stellenwert_verloren, Bruch.von(a / 10 * b + teilEiner)),
            ),
            probe = { x -> x / Bruch.von(b) == Bruch.von(a) },
        )
    }

    // ---- Dezimalzahlen ------------------------------------------------------

    /** Ganzzahl [wert] als Dezimalzahl mit genau [stellen] Nachkommastellen. */
    private fun dezimalMitStellen(wert: Long, stellen: Int): String {
        val faktor = zehn(stellen)
        val vor = wert / faktor
        val nach = (wert % faktor).toString().padStart(stellen, '0')
        return "$vor,$nach"
    }

    private fun dezimalAddieren(zufall: Random, niveau: Niveau): Aufgabe {
        // Unterschiedlich viele Nachkommastellen -- genau daran scheitert das
        // Untereinanderschreiben. E rechnet mit Tausendsteln, G mit Zehnteln.
        val stellenA = je(niveau, 1, 1, 2)
        val stellenB = je(niveau, 2, 2, 3)
        val gemeinsam = maxOf(stellenA, stellenB)
        val k1 = zufall.nextInt(je(niveau, 12, 12, 105), je(niveau, 60, 99, 989)).toLong()
        val k2 = zufall.nextInt(je(niveau, 105, 105, 1005), je(niveau, 600, 989, 8989)).toLong()
        val a = Bruch.von(k1, zehn(stellenA))
        val b = Bruch.von(k2, zehn(stellenB))
        val aufgefuellt = dezimalMitStellen(k1 * zehn(gemeinsam - stellenA), gemeinsam)
        val bText = dezimalMitStellen(k2 * zehn(gemeinsam - stellenB), gemeinsam)
        // Wer die Kommas nicht ausrichtet, addiert die reinen Ziffernfolgen.
        val schief = Bruch.von(k1 + k2, zehn(gemeinsam))
        return if (zufall.nextBoolean() || a.alsDouble() < b.alsDouble()) {
            val ergebnis = a + b
            bau(
                kompetenz = "DZ-ADD-1",
                frage = t(R.string.f_plus, a.alsDezimalText(), b.alsDezimalText()),
                loesung = ergebnis,
                form = Form.ZAHL,
                weg = listOf(
                    t(R.string.w_dz_komma_ausrichten),
                    t(R.string.w_dz_aufgefuellt, aufgefuellt, bText),
                    t(R.string.w_rechnung_plus, aufgefuellt, bText, ergebnis.alsDezimalText()),
                ),
                hilfen = listOf(
                    t(R.string.h_dz_add_1_a),
                    t(R.string.h_dz_add_1_b),
                    t(R.string.h_dz_add_1_c, aufgefuellt, bText),
                ),
                fehlbilder = listOf(
                    Fehlbild("stellen_nicht_ausgerichtet", R.string.fa_stellen_nicht_ausgerichtet, schief),
                    Fehlbild("komma_verrutscht", R.string.fa_komma_verrutscht, ergebnis * Bruch.von(10)),
                ),
                probe = { x -> x - b == a },
            )
        } else {
            val ergebnis = a - b
            bau(
                kompetenz = "DZ-ADD-1",
                frage = t(R.string.f_minus, a.alsDezimalText(), b.alsDezimalText()),
                loesung = ergebnis,
                form = Form.ZAHL,
                weg = listOf(
                    t(R.string.w_dz_komma_ausrichten),
                    t(R.string.w_dz_aufgefuellt, aufgefuellt, bText),
                    t(R.string.w_rechnung_minus, aufgefuellt, bText, ergebnis.alsDezimalText()),
                ),
                hilfen = listOf(
                    t(R.string.h_dz_add_1_a),
                    t(R.string.h_dz_add_1_b),
                    t(R.string.h_dz_add_1_c, aufgefuellt, bText),
                ),
                fehlbilder = listOf(
                    Fehlbild("stellen_nicht_ausgerichtet", R.string.fa_stellen_nicht_ausgerichtet, Bruch.von(k1 - k2, zehn(gemeinsam))),
                    Fehlbild("komma_verrutscht", R.string.fa_komma_verrutscht, ergebnis * Bruch.von(10)),
                ),
                probe = { x -> x + b == a },
            )
        }
    }

    private fun dezimalMal(zufall: Random, niveau: Niveau): Aufgabe {
        // Beide Faktoren behalten genau eine Nachkommastelle -- der Rechenweg
        // nennt das ausdruecklich. Die Niveaus trennt der Zahlenraum.
        val k1 = zufall.nextInt(je(niveau, 11, 11, 101), je(niveau, 41, 99, 999))
        val k2 = zufall.nextInt(je(niveau, 2, 2, 3), je(niveau, 7, 10, 10))
        val a = Bruch.von(k1.toLong(), 10L)
        val b = Bruch.von(k2.toLong(), 10L)
        val ergebnis = a * b
        val ohneKomma = k1.toLong() * k2.toLong()
        return bau(
            kompetenz = "DZ-MUL-1",
            frage = t(R.string.f_mal, a.alsDezimalText(), b.alsDezimalText()),
            loesung = ergebnis,
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_dz_ohne_komma, z(k1), z(k2), z(ohneKomma)),
                t(R.string.w_dz_stellen_zaehlen, a.alsDezimalText(), b.alsDezimalText()),
                t(R.string.w_dz_komma_setzen, dezimalMitStellen(ohneKomma, 2)),
            ),
            hilfen = listOf(
                t(R.string.h_dz_mul_1_a),
                t(R.string.h_dz_mul_1_b),
                t(R.string.h_rechnung_mal, z(k1), z(k2), z(ohneKomma)),
            ),
            fehlbilder = listOf(
                Fehlbild("komma_zu_weit_rechts", R.string.fa_komma_zu_weit_rechts, ergebnis * Bruch.von(10)),
                Fehlbild("komma_zu_weit_links", R.string.fa_komma_zu_weit_links, ergebnis / Bruch.von(10)),
            ),
            probe = { x -> x / b == a },
        )
    }

    private val dezimalNennerG = listOf(2, 4, 5, 10)
    private val dezimalNennerM = listOf(2, 4, 5, 8, 10, 20, 25)
    private val dezimalNennerE = listOf(8, 16, 20, 25, 40, 50, 80, 125)

    private fun bruchAlsDezimal(zufall: Random, niveau: Niveau): Aufgabe {
        val topf = je(niveau, dezimalNennerG, dezimalNennerM, dezimalNennerE)
        val nenner = topf[zufall.nextInt(topf.size)]
        var zaehler = zufall.nextInt(1, nenner)
        while (Bruch.ggt(zaehler.toLong(), nenner.toLong()) != 1L) zaehler = zufall.nextInt(1, nenner)
        // Kleinste Zehnerpotenz, die der Nenner teilt -- 4 braucht 100, 8 braucht 1000.
        var ziel = 10
        while (ziel % nenner != 0) ziel *= 10
        val faktor = ziel / nenner
        val wert = Bruch.von(zaehler, nenner)
        return bau(
            kompetenz = "DZ-BRU-1",
            frage = t(R.string.f_als_dezimalzahl, bruchText(zaehler, nenner)),
            loesung = wert,
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_dz_bru_erweitern, bruchText(zaehler, nenner), z(faktor), bruchText(zaehler * faktor, ziel)),
                t(R.string.w_dz_bru_ablesen, bruchText(zaehler * faktor, ziel), wert.alsDezimalText()),
            ),
            hilfen = listOf(
                t(R.string.h_dz_bru_1_a),
                t(R.string.h_dz_bru_1_b),
                t(R.string.h_dz_bru_1_c, z(faktor), z(ziel)),
            ),
            fehlbilder = listOf(
                Fehlbild("bruchstrich_verwechselt", R.string.fa_bruchstrich_verwechselt, Bruch.von(nenner, zaehler)),
                Fehlbild("komma_verrutscht", R.string.fa_komma_verrutscht, wert * Bruch.von(10)),
            ),
            probe = { x -> x * Bruch.von(nenner) == Bruch.von(zaehler) },
        )
    }

    // ---- Brueche ------------------------------------------------------------

    private fun kuerzen(zufall: Random, niveau: Niveau): Aufgabe {
        val nenner = zufall.nextInt(je(niveau, 2, 3, 3), je(niveau, 8, 13, 21))
        var zaehler = zufall.nextInt(1, nenner)
        while (Bruch.ggt(zaehler.toLong(), nenner.toLong()) != 1L) zaehler = zufall.nextInt(1, nenner)
        val faktor = zufall.nextInt(je(niveau, 2, 2, 3), je(niveau, 6, 7, 14))
        val obenRoh = zaehler * faktor
        val untenRoh = nenner * faktor
        val gekuerzt = Bruch.von(zaehler, nenner)
        val halb = kleinsterTeiler(faktor)
        val fehlbilder = mutableListOf(
            Fehlbild("nur_zaehler_gekuerzt", R.string.fa_nur_zaehler_gekuerzt, Bruch.von(zaehler, untenRoh)),
        )
        if (halb in 2 until faktor) {
            fehlbilder += Fehlbild(
                "nicht_fertig_gekuerzt",
                R.string.fa_nicht_fertig_gekuerzt,
                Bruch.von(obenRoh / halb, untenRoh / halb),
            )
        }
        return bau(
            kompetenz = "BR-KUE-1",
            frage = t(R.string.f_kuerzen, bruchText(obenRoh, untenRoh)),
            loesung = gekuerzt,
            form = Form.BRUCH,
            weg = listOf(
                t(R.string.w_kue_ggt, z(obenRoh), z(untenRoh), z(faktor)),
                t(R.string.w_kue_teilen, z(obenRoh), z(faktor), z(zaehler), z(untenRoh), z(nenner)),
                t(R.string.w_kue_ergebnis, bruchText(zaehler, nenner)),
            ),
            hilfen = listOf(
                t(R.string.h_br_kue_1_a),
                t(R.string.h_br_kue_1_b),
                t(R.string.h_br_kue_1_c, z(faktor)),
            ),
            fehlbilder = fehlbilder,
            // Zurueckerweitern muss genau die Zahlen der Aufgabe ergeben.
            probe = { x -> x.zaehler * faktor == obenRoh.toLong() && x.nenner * faktor == untenRoh.toLong() },
        )
    }

    private fun kleinsterTeiler(wert: Int): Int {
        var pruef = 2
        while (pruef * pruef <= wert) {
            if (wert % pruef == 0) return pruef
            pruef++
        }
        return wert
    }

    private fun bruecheAddieren(zufall: Random, niveau: Niveau): Aufgabe {
        val obergrenze = je(niveau, 7, 10, 14)
        var n1 = zufall.nextInt(2, obergrenze)
        var n2 = zufall.nextInt(2, obergrenze)
        if (n1 == n2) n2 = if (n2 < obergrenze - 1) n2 + 1 else n2 - 1
        var z1 = zufall.nextInt(1, n1)
        var z2 = zufall.nextInt(1, n2)
        val plus = zufall.nextBoolean()
        // G und M bleiben im Positiven; E darf unter null rutschen.
        val negativErlaubt = niveau == Niveau.E
        if (!plus && !negativErlaubt && Bruch.von(z1, n1).alsDouble() < Bruch.von(z2, n2).alsDouble()) {
            val hz = z1; val hn = n1
            z1 = z2; n1 = n2
            z2 = hz; n2 = hn
        }
        val haupt = kgv(n1, n2)
        val e1 = z1 * (haupt / n1)
        val e2 = z2 * (haupt / n2)
        val roh = if (plus) e1 + e2 else e1 - e2
        val ergebnis = Bruch.von(roh, haupt)
        val ersterBruch = Bruch.von(z1, n1)
        val zweiterBruch = Bruch.von(z2, n2)
        val fehlbilder = mutableListOf<Fehlbild>()
        val nennerRoh = if (plus) n1 + n2 else n1 - n2
        if (nennerRoh != 0) {
            fehlbilder += Fehlbild(
                "zaehler_und_nenner_verrechnet",
                R.string.fa_zaehler_und_nenner_verrechnet,
                Bruch.von(if (plus) z1 + z2 else z1 - z2, nennerRoh),
            )
        }
        fehlbilder += Fehlbild(
            "nenner_erweitert_zaehler_nicht",
            R.string.fa_nenner_erweitert_zaehler_nicht,
            Bruch.von(if (plus) z1 + z2 else z1 - z2, haupt),
        )
        return bau(
            kompetenz = "BR-ADD-1",
            frage = t(
                if (plus) R.string.f_plus else R.string.f_minus,
                bruchText(z1, n1),
                bruchText(z2, n2),
            ),
            loesung = ergebnis,
            form = Form.BRUCH,
            weg = listOf(
                t(R.string.w_br_hauptnenner, z(n1), z(n2), z(haupt)),
                t(R.string.w_br_erweitern, bruchText(z1, n1), bruchText(e1, haupt), bruchText(z2, n2), bruchText(e2, haupt)),
                t(
                    if (plus) R.string.w_br_zaehler_plus else R.string.w_br_zaehler_minus,
                    z(e1), z(e2), bruchText(roh, haupt),
                ),
                t(R.string.w_kue_ergebnis, ergebnis.alsBruchText()),
            ),
            hilfen = listOf(
                t(R.string.h_br_add_1_a),
                t(R.string.h_br_add_1_b),
                t(R.string.h_br_add_1_c, z(haupt)),
            ),
            fehlbilder = fehlbilder,
            probe = { x -> if (plus) x - zweiterBruch == ersterBruch else x + zweiterBruch == ersterBruch },
        )
    }

    private fun bruecheMal(zufall: Random, niveau: Niveau): Aufgabe {
        val obergrenze = je(niveau, 7, 10, 14)
        val n1 = zufall.nextInt(2, obergrenze)
        val n2 = zufall.nextInt(2, obergrenze)
        // E rechnet auch mit einem negativen Bruch.
        val vorzeichen = if (niveau == Niveau.E && zufall.nextBoolean()) -1 else 1
        val z1 = zufall.nextInt(1, n1) * vorzeichen
        val z2 = zufall.nextInt(1, n2)
        val ersterBruch = Bruch.von(z1, n1)
        val zweiterBruch = Bruch.von(z2, n2)
        val ergebnis = ersterBruch * zweiterBruch
        return bau(
            kompetenz = "BR-MUL-1",
            frage = t(R.string.f_mal, bruchText(z1, n1), bruchText(z2, n2)),
            loesung = ergebnis,
            form = Form.BRUCH,
            weg = listOf(
                t(R.string.w_br_mul_regel),
                t(R.string.w_br_mul_zaehler, z(z1), z(z2), z(z1 * z2)),
                t(R.string.w_br_mul_nenner, z(n1), z(n2), z(n1 * n2)),
                t(R.string.w_kue_ergebnis, ergebnis.alsBruchText()),
            ),
            hilfen = listOf(
                t(R.string.h_br_mul_1_a),
                t(R.string.h_br_mul_1_b),
                t(R.string.h_br_mul_1_c, z(z1), z(z2), z(z1 * z2)),
            ),
            fehlbilder = listOf(
                Fehlbild("kreuzweise_multipliziert", R.string.fa_kreuzweise_multipliziert, Bruch.von(z1 * n2, n1 * z2)),
                Fehlbild("nur_zaehler_multipliziert", R.string.fa_nur_zaehler_multipliziert, Bruch.von(z1 * z2, n1)),
            ),
            probe = { x -> x / zweiterBruch == ersterBruch },
        )
    }

    private fun bruecheGeteilt(zufall: Random, niveau: Niveau): Aufgabe {
        val obergrenze = je(niveau, 7, 10, 14)
        val n1 = zufall.nextInt(2, obergrenze)
        val n2 = zufall.nextInt(2, obergrenze)
        val vorzeichen = if (niveau == Niveau.E && zufall.nextBoolean()) -1 else 1
        val z1 = zufall.nextInt(1, n1) * vorzeichen
        val z2 = zufall.nextInt(1, n2)
        val ersterBruch = Bruch.von(z1, n1)
        val zweiterBruch = Bruch.von(z2, n2)
        val ergebnis = ersterBruch / zweiterBruch
        return bau(
            kompetenz = "BR-DIV-1",
            frage = t(R.string.f_geteilt, bruchText(z1, n1), bruchText(z2, n2)),
            loesung = ergebnis,
            form = Form.BRUCH,
            weg = listOf(
                t(R.string.w_br_div_regel),
                t(R.string.w_br_div_umformen, bruchText(z1, n1), bruchText(z2, n2), bruchText(n2, z2)),
                t(R.string.w_br_mul_zaehler, z(z1), z(n2), z(z1 * n2)),
                t(R.string.w_br_mul_nenner, z(n1), z(z2), z(n1 * z2)),
                t(R.string.w_kue_ergebnis, ergebnis.alsBruchText()),
            ),
            hilfen = listOf(
                t(R.string.h_br_div_1_a),
                t(R.string.h_br_div_1_b),
                t(R.string.h_br_div_1_c, bruchText(z2, n2), bruchText(n2, z2)),
            ),
            fehlbilder = listOf(
                Fehlbild("kehrwert_vergessen", R.string.fa_kehrwert_vergessen, Bruch.von(z1 * z2, n1 * n2)),
                Fehlbild("falschen_bruch_gedreht", R.string.fa_falschen_bruch_gedreht, Bruch.von(n1 * z2, z1 * n2)),
            ),
            probe = { x -> x * zweiterBruch == ersterBruch },
        )
    }

    // ---- Negative Zahlen ----------------------------------------------------

    private fun negativAddieren(zufall: Random, niveau: Niveau): Aufgabe {
        val spanne = je(niveau, 10, 15, 45)
        val a = zufall.nextInt(-spanne, spanne + 1).let { if (it == 0) -spanne / 2 else it }
        val b = zufall.nextInt(2, je(niveau, 11, 16, 41))
        val plus = zufall.nextBoolean()
        val ergebnis = if (plus) a + b else a - b
        val schritte = b
        return bau(
            kompetenz = "NZ-ADD-1",
            frage = t(if (plus) R.string.f_plus else R.string.f_minus, z(a), z(b)),
            loesung = Bruch.von(ergebnis),
            form = Form.ZAHL,
            weg = listOf(
                t(
                    if (plus) R.string.w_nz_nach_rechts else R.string.w_nz_nach_links,
                    z(a), z(schritte),
                ),
                t(R.string.w_nz_landen, z(ergebnis)),
            ),
            hilfen = listOf(
                t(R.string.h_nz_add_1_a),
                t(R.string.h_nz_add_1_b),
                t(R.string.h_nz_add_1_c, z(a), z(schritte)),
            ),
            fehlbilder = listOf(
                Fehlbild("vorzeichen_vertauscht", R.string.fa_vorzeichen_vertauscht, Bruch.von(-ergebnis)),
                Fehlbild("minus_uebersehen", R.string.fa_minus_uebersehen, Bruch.von(if (plus) abs(a) + b else abs(a) - b)),
            ),
            probe = { x -> if (plus) x - Bruch.von(b) == Bruch.von(a) else x + Bruch.von(b) == Bruch.von(a) },
        )
    }

    private fun negativMal(zufall: Random, niveau: Niveau): Aufgabe {
        val mal = zufall.nextBoolean()
        val betragA = zufall.nextInt(je(niveau, 2, 3, 3), je(niveau, 8, 13, 17))
        val betragB = zufall.nextInt(2, je(niveau, 7, 10, 14))
        // Mindestens ein Minuszeichen, sonst waere es keine Vorzeichenaufgabe.
        val negB = zufall.nextBoolean()
        val negA = if (negB) zufall.nextBoolean() else true
        return if (mal) {
            val a = if (negA) -betragA else betragA
            val b = if (negB) -betragB else betragB
            val ergebnis = a * b
            val gleich = (a < 0) == (b < 0)
            bau(
                kompetenz = "NZ-MUL-1",
                frage = t(R.string.f_mal, z(a), geklammert(b)),
                loesung = Bruch.von(ergebnis),
                form = Form.ZAHL,
                weg = listOf(
                    t(R.string.w_nz_betraege_mal, z(betragA), z(betragB), z(betragA * betragB)),
                    t(if (gleich) R.string.w_nz_regel_gleich else R.string.w_nz_regel_ungleich),
                    t(R.string.w_nz_also, z(ergebnis)),
                ),
                hilfen = listOf(
                    t(R.string.h_nz_mul_1_a),
                    t(R.string.h_nz_mul_1_b),
                    t(R.string.h_nz_mul_1_c, z(betragA * betragB)),
                ),
                fehlbilder = listOf(
                    Fehlbild("vorzeichenregel_verwechselt", R.string.fa_vorzeichenregel_verwechselt, Bruch.von(-ergebnis)),
                    Fehlbild("addiert_statt_mal", R.string.fa_addiert_statt_mal, Bruch.von(a + b)),
                ),
                probe = { x -> x / Bruch.von(b) == Bruch.von(a) },
            )
        } else {
            val ergebnisBetrag = betragB
            val ganzesBetrag = betragA * betragB
            val a = if (negA) -ganzesBetrag else ganzesBetrag
            val b = if (negB) -betragA else betragA
            val ergebnis = a / b
            val gleich = (a < 0) == (b < 0)
            bau(
                kompetenz = "NZ-MUL-1",
                frage = t(R.string.f_geteilt, z(a), geklammert(b)),
                loesung = Bruch.von(ergebnis),
                form = Form.ZAHL,
                weg = listOf(
                    t(R.string.w_nz_betraege_geteilt, z(ganzesBetrag), z(betragA), z(ergebnisBetrag)),
                    t(if (gleich) R.string.w_nz_regel_gleich else R.string.w_nz_regel_ungleich),
                    t(R.string.w_nz_also, z(ergebnis)),
                ),
                hilfen = listOf(
                    t(R.string.h_nz_mul_1_a),
                    t(R.string.h_nz_mul_1_b),
                    t(R.string.h_nz_mul_1_c, z(ergebnisBetrag)),
                ),
                fehlbilder = listOf(
                    Fehlbild("vorzeichenregel_verwechselt", R.string.fa_vorzeichenregel_verwechselt, Bruch.von(-ergebnis)),
                    Fehlbild("subtrahiert_statt_geteilt", R.string.fa_subtrahiert_statt_geteilt, Bruch.von(a - b)),
                ),
                probe = { x -> x * Bruch.von(b) == Bruch.von(a) },
            )
        }
    }

    // ---- Prozent ------------------------------------------------------------

    private val saetzeG = listOf(10, 20, 25, 50)
    private val saetzeM = listOf(5, 10, 12, 15, 18, 20, 25, 30, 40, 60, 75)
    private val saetzeE = listOf(3, 7, 12, 18, 23, 37, 45, 65, 85, 110, 125)

    private fun prozentwert(zufall: Random, niveau: Niveau): Aufgabe {
        val topf = je(niveau, saetzeG, saetzeM, saetzeE)
        val satz = topf[zufall.nextInt(topf.size)]
        val grund = je(niveau, 100, 50, 25) * zufall.nextInt(je(niveau, 1, 3, 5), je(niveau, 10, 31, 81))
        val einProzent = Bruch.von(grund.toLong(), 100L)
        val ergebnis = einProzent * Bruch.von(satz)
        return bau(
            kompetenz = "PZ-VON-1",
            frage = t(R.string.f_prozent_von, z(satz), z(grund)),
            loesung = ergebnis,
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_pz_ein_prozent, z(grund), einProzent.alsDezimalText()),
                t(R.string.w_rechnung_mal, einProzent.alsDezimalText(), z(satz), ergebnis.alsDezimalText()),
            ),
            hilfen = listOf(
                t(R.string.h_pz_von_1_a),
                t(R.string.h_pz_von_1_b),
                t(R.string.h_pz_von_1_c, einProzent.alsDezimalText()),
            ),
            fehlbilder = listOf(
                Fehlbild("durch_hundert_vergessen", R.string.fa_durch_hundert_vergessen, Bruch.von(satz.toLong() * grund.toLong())),
                Fehlbild("geteilt_statt_mal", R.string.fa_geteilt_statt_mal, Bruch.von(grund, satz)),
            ),
            probe = { x -> x * Bruch.HUNDERT == Bruch.von(satz) * Bruch.von(grund) },
        )
    }

    private val grundwerteG = listOf(100, 200, 400)
    private val grundwerteM = listOf(60, 80, 120, 150, 180, 200, 240, 250, 300, 400)
    private val grundwerteE = listOf(64, 96, 128, 175, 224, 320, 360, 480, 625, 800)
    private val glatteG = listOf(10, 20, 25, 50)
    private val glatteM = listOf(5, 10, 15, 20, 25, 40, 60, 75)
    private val glatteE = listOf(4, 12, 24, 35, 45, 55, 65, 80, 96)

    private fun prozentsatz(zufall: Random, niveau: Niveau): Aufgabe {
        val grundTopf = je(niveau, grundwerteG, grundwerteM, grundwerteE)
        val satzTopf = je(niveau, glatteG, glatteM, glatteE)
        val grund = grundTopf[zufall.nextInt(grundTopf.size)]
        val satz = satzTopf[zufall.nextInt(satzTopf.size)]
        val wert = Bruch.von(grund.toLong() * satz.toLong(), 100L)
        val anteil = Bruch.von(satz.toLong(), 100L)
        return bau(
            kompetenz = "PZ-SAT-1",
            frage = t(R.string.f_prozentsatz, wert.alsDezimalText(), z(grund)),
            loesung = Bruch.von(satz),
            form = Form.PROZENT,
            weg = listOf(
                t(R.string.w_pz_anteil, wert.alsDezimalText(), z(grund), anteil.alsDezimalText()),
                t(R.string.w_pz_mal_hundert, anteil.alsDezimalText(), z(satz)),
            ),
            hilfen = listOf(
                t(R.string.h_pz_sat_1_a),
                t(R.string.h_pz_sat_1_b),
                t(R.string.h_pz_sat_1_c, wert.alsDezimalText(), z(grund), anteil.alsDezimalText()),
            ),
            fehlbilder = listOf(
                Fehlbild("bezug_vertauscht", R.string.fa_bezug_vertauscht, Bruch.von(grund.toLong(), 1L) / wert * Bruch.HUNDERT),
                Fehlbild("mal_hundert_vergessen", R.string.fa_mal_hundert_vergessen, anteil),
            ),
            probe = { x -> Bruch.von(grund) * x == wert * Bruch.HUNDERT },
        )
    }

    // ---- Terme und Gleichungen ----------------------------------------------

    private fun termAuswerten(zufall: Random, niveau: Niveau): Aufgabe {
        val faktor = zufall.nextInt(2, je(niveau, 7, 10, 14))
        // Erst im erweiterten Niveau wird auch ein negatives x eingesetzt.
        val betragX = zufall.nextInt(2, je(niveau, 8, 13, 17))
        val x = if (niveau == Niveau.E && zufall.nextBoolean()) -betragX else betragX
        val summand = zufall.nextInt(2, je(niveau, 11, 16, 41))
        val plus = zufall.nextBoolean()
        val produkt = faktor * x
        val ergebnis = if (plus) produkt + summand else produkt - summand
        val term = if (plus) {
            "${faktor}x + $summand"
        } else {
            "${faktor}x ${Bruch.MINUS} $summand"
        }
        val eingesetzt = if (plus) {
            "$faktor · ${geklammert(x)} + $summand"
        } else {
            "$faktor · ${geklammert(x)} ${Bruch.MINUS} $summand"
        }
        return bau(
            kompetenz = "GL-TER-1",
            frage = t(R.string.f_term_fuer_x, term, z(x)),
            loesung = Bruch.von(ergebnis),
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_gl_einsetzen, eingesetzt),
                t(R.string.w_gl_punkt_vor_strich, z(faktor), z(x), z(produkt)),
                t(
                    if (plus) R.string.w_rechnung_plus else R.string.w_rechnung_minus,
                    z(produkt), z(summand), z(ergebnis),
                ),
            ),
            hilfen = listOf(
                t(R.string.h_gl_ter_1_a),
                t(R.string.h_gl_ter_1_b),
                t(R.string.h_rechnung_mal, z(faktor), z(x), z(produkt)),
            ),
            fehlbilder = listOf(
                Fehlbild(
                    "punkt_vor_strich_missachtet",
                    R.string.fa_punkt_vor_strich_missachtet,
                    Bruch.von(faktor * (if (plus) x + summand else x - summand)),
                ),
                Fehlbild("mal_als_plus", R.string.fa_mal_als_plus, Bruch.von(if (plus) faktor + x + summand else faktor + x - summand)),
            ),
            // Denselben Term noch einmal, aber exakt ueber Brueche gerechnet.
            probe = { y ->
                val summe = Bruch.von(faktor) * Bruch.von(x)
                y == if (plus) summe + Bruch.von(summand) else summe - Bruch.von(summand)
            },
        )
    }

    private fun gleichungLinear(zufall: Random, niveau: Niveau): Aufgabe {
        val faktor = zufall.nextInt(2, je(niveau, 7, 10, 14))
        val betragX = zufall.nextInt(2, je(niveau, 9, 13, 21))
        val x = if (niveau == Niveau.E && zufall.nextBoolean()) -betragX else betragX
        val betrag = zufall.nextInt(2, je(niveau, 11, 16, 41))
        val summand = if (niveau == Niveau.G) betrag else betrag * (if (zufall.nextBoolean()) 1 else -1)
        val rechts = faktor * x + summand
        val zeichen = if (summand >= 0) "+" else Bruch.MINUS
        val gleichung = "${faktor}x $zeichen ${abs(summand)} = ${Bruch.ganzText(rechts.toLong())}"
        val gegenzeichen = if (summand >= 0) Bruch.MINUS else "+"
        val zwischen = "${faktor}x = ${Bruch.ganzText((rechts - summand).toLong())}"
        return bau(
            kompetenz = "GL-LIN-1",
            frage = t(R.string.f_ausdruck, gleichung),
            loesung = Bruch.von(x),
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_gl_beide_seiten, "$gegenzeichen ${abs(summand)}", zwischen),
                t(R.string.w_gl_durch_teilen, z(faktor), z(x)),
            ),
            hilfen = listOf(
                t(R.string.h_gl_lin_1_a),
                t(R.string.h_gl_lin_1_b),
                t(R.string.h_gl_zwischenschritt, zwischen),
            ),
            fehlbilder = listOf(
                Fehlbild("summand_uebersehen", R.string.fa_summand_uebersehen, Bruch.von(rechts, faktor)),
                Fehlbild("falsche_gegenrechnung", R.string.fa_falsche_gegenrechnung, Bruch.von(rechts + summand, faktor)),
            ),
            // Einsetzprobe: die Gleichung muss mit der Loesung aufgehen.
            probe = { loesung -> Bruch.von(faktor) * loesung + Bruch.von(summand) == Bruch.von(rechts) },
        )
    }

    private fun gleichungKlammer(zufall: Random, niveau: Niveau): Aufgabe {
        val faktor = zufall.nextInt(2, je(niveau, 5, 7, 11))
        val x = zufall.nextInt(2, je(niveau, 7, 10, 17))
        val summand = zufall.nextInt(2, je(niveau, 7, 10, 21))
        val rechts = faktor * (x + summand)
        val gleichung = "$faktor · (x + $summand) = $rechts"
        val zwischen = "x + $summand = ${rechts / faktor}"
        return bau(
            kompetenz = "GL-KLA-1",
            frage = t(R.string.f_ausdruck, gleichung),
            loesung = Bruch.von(x),
            form = Form.ZAHL,
            weg = listOf(
                t(R.string.w_gl_klammer_teilen, z(faktor), z(summand), z(rechts / faktor)),
                t(R.string.w_gl_klammer_ausgleichen, z(summand), z(x)),
            ),
            hilfen = listOf(
                t(R.string.h_gl_kla_1_a, z(faktor)),
                t(R.string.h_gl_kla_1_b),
                t(R.string.h_gl_zwischenschritt, zwischen),
            ),
            fehlbilder = listOf(
                Fehlbild("klammer_nicht_beachtet", R.string.fa_klammer_nicht_beachtet, Bruch.von(rechts - summand, faktor)),
                Fehlbild("falsches_vorzeichen_umgestellt", R.string.fa_falsches_vorzeichen_umgestellt, Bruch.von(rechts / faktor + summand)),
            ),
            probe = { loesung -> Bruch.von(faktor) * (loesung + Bruch.von(summand)) == Bruch.von(rechts) },
        )
    }
}
