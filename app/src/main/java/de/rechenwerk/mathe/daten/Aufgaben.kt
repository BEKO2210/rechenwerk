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

private fun t(@StringRes id: Int, vararg teile: String) = Txt(id, teile.toList())

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
    )

    fun name(kennung: String): Int? = namen[kennung]
}

/**
 * Erzeugt Aufgaben. Jede Kompetenz hat einen eigenen Generator: es gibt keine
 * feste Aufgabenliste, nur Bauplaene mit Zufallszahlen in sinnvollen Grenzen.
 */
object Werkbank {

    fun erzeuge(kennung: String, zufall: Random = Random.Default): Aufgabe = when (kennung) {
        "GR-MUL-1" -> einmaleins(zufall)
        "GR-ADD-1" -> addierenBisTausend(zufall)
        "GR-DIV-1" -> teilen(zufall)
        "GR-MUL-2" -> mehrstelligMal(zufall)
        "DZ-ADD-1" -> dezimalAddieren(zufall)
        "DZ-MUL-1" -> dezimalMal(zufall)
        "DZ-BRU-1" -> bruchAlsDezimal(zufall)
        "BR-KUE-1" -> kuerzen(zufall)
        "BR-ADD-1" -> bruecheAddieren(zufall)
        "BR-MUL-1" -> bruecheMal(zufall)
        "BR-DIV-1" -> bruecheGeteilt(zufall)
        "NZ-ADD-1" -> negativAddieren(zufall)
        "NZ-MUL-1" -> negativMal(zufall)
        "PZ-VON-1" -> prozentwert(zufall)
        "PZ-SAT-1" -> prozentsatz(zufall)
        "GL-TER-1" -> termAuswerten(zufall)
        "GL-LIN-1" -> gleichungLinear(zufall)
        "GL-KLA-1" -> gleichungKlammer(zufall)
        else -> einmaleins(zufall)
    }

    // ---- Bauhilfen ----------------------------------------------------------

    private fun bau(
        kompetenz: String,
        frage: Txt,
        loesung: Bruch,
        form: Form,
        weg: List<Txt>,
        hilfen: List<Txt>,
        fehlbilder: List<Fehlbild>,
    ): Aufgabe {
        val sauber = LinkedHashMap<Bruch, Fehlbild>()
        for (bild in fehlbilder) {
            if (bild.antwort == loesung) continue
            if (!sauber.containsKey(bild.antwort)) sauber[bild.antwort] = bild
        }
        return Aufgabe(kompetenz, frage, loesung, form, weg, hilfen, sauber.values.toList())
    }

    private fun z(wert: Long): String = Bruch.ganzText(wert)

    private fun z(wert: Int): String = Bruch.ganzText(wert.toLong())

    private fun bruchText(zaehler: Int, nenner: Int): String = "$zaehler/$nenner"

    private fun kgv(a: Int, b: Int): Int = (a / Bruch.ggt(a.toLong(), b.toLong()).toInt()) * b

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

    private fun geklammert(wert: Int): String =
        if (wert < 0) "(" + Bruch.ganzText(wert.toLong()) + ")" else wert.toString()

    // ---- Grundrechnen -------------------------------------------------------

    private fun einmaleins(zufall: Random): Aufgabe {
        val a = zufall.nextInt(3, 11)
        val b = zufall.nextInt(4, 11)
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
        )
    }

    private fun addierenBisTausend(zufall: Random): Aufgabe {
        val plus = zufall.nextBoolean()
        return if (plus) {
            // Einerziffern beider Zahlen zusammen ueber zehn: der Uebertrag ist Pflicht.
            val a = zufall.nextInt(12, 88) * 10 + zufall.nextInt(5, 10)
            val b = zufall.nextInt(3, 29) * 10 + zufall.nextInt(5, 10)
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
            )
        } else {
            // Obere Einerziffer kleiner als die untere: es muss entbuendelt werden.
            val a = zufall.nextInt(32, 95) * 10 + zufall.nextInt(0, 5)
            val b = zufall.nextInt(4, 29) * 10 + zufall.nextInt(5, 10)
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
            )
        }
    }

    private fun teilen(zufall: Random): Aufgabe {
        val teiler = zufall.nextInt(3, 13)
        val ergebnis = zufall.nextInt(3, 13)
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
        )
    }

    private fun mehrstelligMal(zufall: Random): Aufgabe {
        val a = zufall.nextInt(13, 30)
        val b = zufall.nextInt(3, 10)
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
        )
    }

    // ---- Dezimalzahlen ------------------------------------------------------

    private fun dezimalAddieren(zufall: Random): Aufgabe {
        val plus = zufall.nextBoolean()
        val k1 = zufall.nextInt(12, 99)
        val k2 = zufall.nextInt(105, 989)
        val a = Bruch.von(k1.toLong(), 10L)
        val b = Bruch.von(k2.toLong(), 100L)
        val aufgefuellt = dezimalMitStellen(k1 * 10, 2)
        val bText = dezimalMitStellen(k2, 2)
        return if (plus || a.alsDouble() < b.alsDouble()) {
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
                    Fehlbild("stellen_nicht_ausgerichtet", R.string.fa_stellen_nicht_ausgerichtet, Bruch.von((k1 + k2).toLong(), 100L)),
                    Fehlbild("komma_verrutscht", R.string.fa_komma_verrutscht, ergebnis * Bruch.von(10)),
                ),
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
                    Fehlbild("stellen_nicht_ausgerichtet", R.string.fa_stellen_nicht_ausgerichtet, Bruch.von((k1 - k2).toLong(), 100L)),
                    Fehlbild("komma_verrutscht", R.string.fa_komma_verrutscht, ergebnis * Bruch.von(10)),
                ),
            )
        }
    }

    /** Ganzzahl [wert] als Dezimalzahl mit genau [stellen] Nachkommastellen. */
    private fun dezimalMitStellen(wert: Int, stellen: Int): String {
        var faktor = 1
        repeat(stellen) { faktor *= 10 }
        val vor = wert / faktor
        val nach = (wert % faktor).toString().padStart(stellen, '0')
        return "$vor,$nach"
    }

    private fun dezimalMal(zufall: Random): Aufgabe {
        val k1 = zufall.nextInt(11, 99)
        val k2 = zufall.nextInt(2, 10)
        val a = Bruch.von(k1.toLong(), 10L)
        val b = Bruch.von(k2.toLong(), 10L)
        val ergebnis = a * b
        val ohneKomma = k1 * k2
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
        )
    }

    private val dezimalNenner = listOf(2, 4, 5, 8, 10, 20, 25)

    private fun bruchAlsDezimal(zufall: Random): Aufgabe {
        val nenner = dezimalNenner[zufall.nextInt(dezimalNenner.size)]
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
        )
    }

    // ---- Brueche ------------------------------------------------------------

    private fun kuerzen(zufall: Random): Aufgabe {
        val nenner = zufall.nextInt(3, 13)
        var zaehler = zufall.nextInt(1, nenner)
        while (Bruch.ggt(zaehler.toLong(), nenner.toLong()) != 1L) zaehler = zufall.nextInt(1, nenner)
        val faktor = zufall.nextInt(2, 7)
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

    private fun bruecheAddieren(zufall: Random): Aufgabe {
        var n1 = zufall.nextInt(2, 10)
        var n2 = zufall.nextInt(2, 10)
        if (n1 == n2) n2 = if (n2 < 9) n2 + 1 else n2 - 1
        var z1 = zufall.nextInt(1, n1)
        var z2 = zufall.nextInt(1, n2)
        val plus = zufall.nextBoolean()
        if (!plus && Bruch.von(z1, n1).alsDouble() < Bruch.von(z2, n2).alsDouble()) {
            val hz = z1; val hn = n1
            z1 = z2; n1 = n2
            z2 = hz; n2 = hn
        }
        val haupt = kgv(n1, n2)
        val e1 = z1 * (haupt / n1)
        val e2 = z2 * (haupt / n2)
        val roh = if (plus) e1 + e2 else e1 - e2
        val ergebnis = Bruch.von(roh, haupt)
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
        )
    }

    private fun bruecheMal(zufall: Random): Aufgabe {
        val n1 = zufall.nextInt(2, 10)
        val n2 = zufall.nextInt(2, 10)
        val z1 = zufall.nextInt(1, n1)
        val z2 = zufall.nextInt(1, n2)
        val ergebnis = Bruch.von(z1 * z2, n1 * n2)
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
        )
    }

    private fun bruecheGeteilt(zufall: Random): Aufgabe {
        val n1 = zufall.nextInt(2, 10)
        val n2 = zufall.nextInt(2, 10)
        val z1 = zufall.nextInt(1, n1)
        val z2 = zufall.nextInt(1, n2)
        val ergebnis = Bruch.von(z1 * n2, n1 * z2)
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
        )
    }

    // ---- Negative Zahlen ----------------------------------------------------

    private fun negativAddieren(zufall: Random): Aufgabe {
        val a = zufall.nextInt(-15, 16).let { if (it == 0) -7 else it }
        val b = zufall.nextInt(2, 16)
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
        )
    }

    private fun negativMal(zufall: Random): Aufgabe {
        val mal = zufall.nextBoolean()
        val betragA = zufall.nextInt(3, 13)
        val betragB = zufall.nextInt(2, 10)
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
            )
        }
    }

    // ---- Prozent ------------------------------------------------------------

    private val prozentsaetze = listOf(5, 10, 12, 15, 18, 20, 25, 30, 40, 60, 75)

    private fun prozentwert(zufall: Random): Aufgabe {
        val satz = prozentsaetze[zufall.nextInt(prozentsaetze.size)]
        val grund = zufall.nextInt(3, 31) * 50
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
        )
    }

    private val grundwerte = listOf(60, 80, 120, 150, 180, 200, 240, 250, 300, 400)
    private val glatteSaetze = listOf(5, 10, 15, 20, 25, 40, 60, 75)

    private fun prozentsatz(zufall: Random): Aufgabe {
        val grund = grundwerte[zufall.nextInt(grundwerte.size)]
        val satz = glatteSaetze[zufall.nextInt(glatteSaetze.size)]
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
        )
    }

    // ---- Terme und Gleichungen ----------------------------------------------

    private fun termAuswerten(zufall: Random): Aufgabe {
        val faktor = zufall.nextInt(2, 10)
        val x = zufall.nextInt(2, 13)
        val summand = zufall.nextInt(2, 16)
        val plus = zufall.nextBoolean()
        val produkt = faktor * x
        val ergebnis = if (plus) produkt + summand else produkt - summand
        val term = if (plus) {
            "${faktor}x + $summand"
        } else {
            "${faktor}x ${Bruch.MINUS} $summand"
        }
        val eingesetzt = if (plus) {
            "$faktor · $x + $summand"
        } else {
            "$faktor · $x ${Bruch.MINUS} $summand"
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
        )
    }

    private fun gleichungLinear(zufall: Random): Aufgabe {
        val faktor = zufall.nextInt(2, 10)
        val x = zufall.nextInt(2, 13)
        val summand = zufall.nextInt(2, 16) * (if (zufall.nextBoolean()) 1 else -1)
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
        )
    }

    private fun gleichungKlammer(zufall: Random): Aufgabe {
        val faktor = zufall.nextInt(2, 7)
        val x = zufall.nextInt(2, 10)
        val summand = zufall.nextInt(2, 10)
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
        )
    }
}
