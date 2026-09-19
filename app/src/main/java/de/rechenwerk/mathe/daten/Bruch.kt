package de.rechenwerk.mathe.daten

import kotlin.math.abs

/**
 * Exakte rationale Zahl als Zaehler/Nenner. Immer gekuerzt, Vorzeichen immer
 * im Zaehler, Nenner immer positiv. Es wird nirgends mit Double gerechnet --
 * 1,7 * 0,4 ergibt hier 17/10 * 2/5 = 17/25 und damit exakt 0,68.
 */
class Bruch private constructor(val zaehler: Long, val nenner: Long) {

    val istGanz: Boolean get() = nenner == 1L

    val istNegativ: Boolean get() = zaehler < 0L

    operator fun plus(anderer: Bruch): Bruch =
        von(zaehler * anderer.nenner + anderer.zaehler * nenner, nenner * anderer.nenner)

    operator fun minus(anderer: Bruch): Bruch =
        von(zaehler * anderer.nenner - anderer.zaehler * nenner, nenner * anderer.nenner)

    operator fun times(anderer: Bruch): Bruch =
        von(zaehler * anderer.zaehler, nenner * anderer.nenner)

    operator fun div(anderer: Bruch): Bruch {
        require(anderer.zaehler != 0L) { "Division durch null" }
        return von(zaehler * anderer.nenner, nenner * anderer.zaehler)
    }

    operator fun unaryMinus(): Bruch = von(-zaehler, nenner)

    fun kehrwert(): Bruch {
        require(zaehler != 0L) { "Kehrwert von null" }
        return von(nenner, zaehler)
    }

    fun alsDouble(): Double = zaehler.toDouble() / nenner.toDouble()

    /**
     * Bruchdarstellung: "3/4", bei ganzen Zahlen nur "3", bei negativen
     * Zahlen mit typografischem Minus.
     */
    fun alsBruchText(): String =
        if (istGanz) ganzText(zaehler) else "${ganzText(zaehler)}/$nenner"

    /**
     * Deutsche Dezimaldarstellung mit Komma. Bricht der Bruch nicht ab
     * (z. B. 1/3), wird auf [stellen] Nachkommastellen gerundet.
     */
    fun alsDezimalText(stellen: Int = 4): String {
        if (istGanz) return ganzText(zaehler)
        val abbrechend = nachkommastellen()
        val genutzt = abbrechend ?: stellen
        if (genutzt == 0) return ganzText(zaehler / nenner)
        var faktor = 1L
        repeat(genutzt) { faktor *= 10L }
        val roh = abs(zaehler) * faktor
        val gerundet = (roh + nenner / 2) / nenner
        val vor = gerundet / faktor
        val nach = (gerundet % faktor).toString().padStart(genutzt, '0').trimEnd('0')
        val zeichen = if (istNegativ) MINUS else ""
        return if (nach.isEmpty()) "$zeichen$vor" else "$zeichen$vor,$nach"
    }

    /** Text fuer die Antwortkontrolle: Bruch wenn nicht abbrechend, sonst Dezimal. */
    fun alsText(): String =
        if (nachkommastellen() == null) alsBruchText() else alsDezimalText()

    /** Anzahl der Nachkommastellen, oder null wenn die Darstellung nicht abbricht. */
    private fun nachkommastellen(): Int? {
        var rest = nenner
        var stellen = 0
        while (rest % 2L == 0L) {
            rest /= 2L
            stellen++
        }
        var fuenfer = 0
        while (rest % 5L == 0L) {
            rest /= 5L
            fuenfer++
        }
        if (rest != 1L) return null
        return maxOf(stellen, fuenfer)
    }

    override fun equals(other: Any?): Boolean =
        other is Bruch && zaehler == other.zaehler && nenner == other.nenner

    override fun hashCode(): Int = 31 * zaehler.hashCode() + nenner.hashCode()

    override fun toString(): String = alsBruchText()

    companion object {
        /** Typografisches Minus -- breiter als der Bindestrich, passt zur Notation. */
        const val MINUS = "−"

        val NULL: Bruch = Bruch(0L, 1L)
        val EINS: Bruch = Bruch(1L, 1L)
        val HUNDERT: Bruch = Bruch(100L, 1L)

        fun von(zaehler: Long, nenner: Long = 1L): Bruch {
            require(nenner != 0L) { "Nenner null" }
            if (zaehler == 0L) return NULL
            val vorzeichen = if ((zaehler < 0L) != (nenner < 0L)) -1L else 1L
            val z = abs(zaehler)
            val n = abs(nenner)
            val teiler = ggt(z, n)
            return Bruch(vorzeichen * (z / teiler), n / teiler)
        }

        fun von(zaehler: Int, nenner: Int = 1): Bruch = von(zaehler.toLong(), nenner.toLong())

        fun ggt(a: Long, b: Long): Long {
            var x = abs(a)
            var y = abs(b)
            while (y != 0L) {
                val h = x % y
                x = y
                y = h
            }
            return if (x == 0L) 1L else x
        }

        /**
         * Liest eine Nutzereingabe. Erlaubt sind Komma und Punkt als
         * Dezimaltrennzeichen, fuehrendes Plus oder Minus (auch das
         * typografische Minus) und die Bruchschreibweise "3/4".
         * Gibt null zurueck, wenn die Eingabe keine Zahl ergibt.
         */
        fun lies(eingabe: String): Bruch? {
            val roh = eingabe.trim().replace(MINUS, "-").replace(" ", "")
            if (roh.isEmpty()) return null
            val schraegstrich = roh.indexOf('/')
            if (schraegstrich >= 0) {
                val oben = zahl(roh.substring(0, schraegstrich)) ?: return null
                val unten = zahl(roh.substring(schraegstrich + 1)) ?: return null
                if (unten == NULL) return null
                return oben / unten
            }
            return zahl(roh)
        }

        /** Eine einzelne Dezimal- oder Ganzzahl ohne Bruchstrich. */
        private fun zahl(text: String): Bruch? {
            if (text.isEmpty()) return null
            val negativ = text.startsWith("-")
            val ohneZeichen = text.removePrefix("-").removePrefix("+")
            if (ohneZeichen.isEmpty()) return null
            val teile = ohneZeichen.replace('.', ',').split(',')
            if (teile.size > 2) return null
            val vor = teile[0]
            val nach = if (teile.size == 2) teile[1] else ""
            if (vor.isEmpty() && nach.isEmpty()) return null
            if (!vor.all { it.isDigit() } || !nach.all { it.isDigit() }) return null
            if (vor.length > 15 || nach.length > 9) return null
            val ziffern = (vor + nach).ifEmpty { "0" }
            val wert = ziffern.toLongOrNull() ?: return null
            var faktor = 1L
            repeat(nach.length) { faktor *= 10L }
            val betrag = von(wert, faktor)
            return if (negativ) -betrag else betrag
        }

        /** Ganze Zahl mit typografischem Minus. */
        fun ganzText(wert: Long): String =
            if (wert < 0L) MINUS + abs(wert).toString() else wert.toString()
    }
}
