package de.rechenwerk.mathe.daten

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

enum class Schulart { HAUPTSCHULE, REALSCHULE, GEMEINSCHAFTSSCHULE, GYMNASIUM }

enum class Niveau { G, M, E }

enum class Modus { SYSTEM, DUNKEL, HELL }

data class Profil(
    val bundesland: String = "BW",
    val schulart: Schulart = Schulart.REALSCHULE,
    val klasse: Int = 5,
    val niveau: Niveau = Niveau.M,
    val eingerichtet: Boolean = false,
)

/**
 * Stand einer einzelnen Kompetenz. [grad] ist der Beherrschungsgrad von 0 bis 1,
 * [stufe] die Stufe im Wiederholungsmodell, [naechsteWdh] der Zeitpunkt der
 * naechsten faelligen Wiederholung in Millisekunden seit 1970.
 */
data class KompetenzStand(
    val kennung: String,
    val grad: Double = 0.0,
    val versuche: Int = 0,
    val treffer: Int = 0,
    val zeitSummeMs: Long = 0L,
    val letzterKontakt: Long = 0L,
    val naechsteWdh: Long = 0L,
    val stufe: Int = 0,
    val wiederholungen: Int = 0,
    val fehlerarten: Map<String, Int> = emptyMap(),
) {
    val mittlereZeitMs: Long get() = if (versuche == 0) 0L else zeitSummeMs / versuche
    val beruehrt: Boolean get() = versuche > 0
}

/** Zahlen ueber alle Kompetenzen hinweg. Eine frische Installation hat hier ueberall 0. */
data class Werte(
    val lernzeitMs: Long = 0L,
    val versuche: Int = 0,
    val treffer: Int = 0,
    val wiederholungen: Int = 0,
    val besteSerie: Int = 0,
    val schnellsteMs: Long = 0L,
    val sitzungen: Int = 0,
)

/** Alles, was einen Neustart ueberlebt. Liegt als JSON im Tresor. */
data class Ablage(
    val profil: Profil = Profil(),
    val modus: Modus = Modus.SYSTEM,
    val staende: Map<String, KompetenzStand> = emptyMap(),
    val werte: Werte = Werte(),
) {
    fun stand(kennung: String): KompetenzStand = staende[kennung] ?: KompetenzStand(kennung)
}

/**
 * JSON-Umwandlung. Liegt bewusst ohne Android-Abhaengigkeit hier, damit die
 * Unit-Tests sie ohne Geraet pruefen koennen.
 */
object Papier {

    const val FORMAT = "rechenwerk"
    const val FASSUNG = 1

    fun schreibe(ablage: Ablage): String {
        val wurzel = JSONObject()
        wurzel.put("format", FORMAT)
        wurzel.put("fassung", FASSUNG)

        val profil = JSONObject()
        profil.put("bundesland", ablage.profil.bundesland)
        profil.put("schulart", ablage.profil.schulart.name)
        profil.put("klasse", ablage.profil.klasse)
        profil.put("niveau", ablage.profil.niveau.name)
        profil.put("eingerichtet", ablage.profil.eingerichtet)
        wurzel.put("profil", profil)

        wurzel.put("modus", ablage.modus.name)

        val werte = JSONObject()
        werte.put("lernzeitMs", ablage.werte.lernzeitMs)
        werte.put("versuche", ablage.werte.versuche)
        werte.put("treffer", ablage.werte.treffer)
        werte.put("wiederholungen", ablage.werte.wiederholungen)
        werte.put("besteSerie", ablage.werte.besteSerie)
        werte.put("schnellsteMs", ablage.werte.schnellsteMs)
        werte.put("sitzungen", ablage.werte.sitzungen)
        wurzel.put("werte", werte)

        val liste = JSONArray()
        for (stand in ablage.staende.values.sortedBy { it.kennung }) {
            val eintrag = JSONObject()
            eintrag.put("kennung", stand.kennung)
            eintrag.put("grad", stand.grad)
            eintrag.put("versuche", stand.versuche)
            eintrag.put("treffer", stand.treffer)
            eintrag.put("zeitSummeMs", stand.zeitSummeMs)
            eintrag.put("letzterKontakt", stand.letzterKontakt)
            eintrag.put("naechsteWdh", stand.naechsteWdh)
            eintrag.put("stufe", stand.stufe)
            eintrag.put("wiederholungen", stand.wiederholungen)
            val fehler = JSONObject()
            for ((art, anzahl) in stand.fehlerarten) fehler.put(art, anzahl)
            eintrag.put("fehlerarten", fehler)
            liste.put(eintrag)
        }
        wurzel.put("staende", liste)

        return wurzel.toString(2)
    }

    /**
     * Liest eine Ablage. Wirft [IllegalArgumentException], wenn der Text kein
     * JSON ist, die Kennung des Formats fehlt oder die Fassung zu neu ist --
     * der Import in den Einstellungen faengt das ab und meldet es.
     */
    fun lies(text: String): Ablage {
        val wurzel = try {
            JSONObject(text)
        } catch (fehler: JSONException) {
            throw IllegalArgumentException("Kein lesbares JSON", fehler)
        }
        if (wurzel.optString("format") != FORMAT) {
            throw IllegalArgumentException("Fremdes Dateiformat")
        }
        val fassung = wurzel.optInt("fassung", 0)
        if (fassung < 1 || fassung > FASSUNG) {
            throw IllegalArgumentException("Unbekannte Fassung: $fassung")
        }

        val profilJson = wurzel.optJSONObject("profil") ?: JSONObject()
        val profil = Profil(
            bundesland = profilJson.optString("bundesland", "BW").ifEmpty { "BW" },
            schulart = wahl(profilJson.optString("schulart"), Schulart.entries, Schulart.REALSCHULE),
            klasse = profilJson.optInt("klasse", 5).coerceIn(5, 10),
            niveau = wahl(profilJson.optString("niveau"), Niveau.entries, Niveau.M),
            eingerichtet = profilJson.optBoolean("eingerichtet", false),
        )

        val werteJson = wurzel.optJSONObject("werte") ?: JSONObject()
        val werte = Werte(
            lernzeitMs = werteJson.optLong("lernzeitMs", 0L).coerceAtLeast(0L),
            versuche = werteJson.optInt("versuche", 0).coerceAtLeast(0),
            treffer = werteJson.optInt("treffer", 0).coerceAtLeast(0),
            wiederholungen = werteJson.optInt("wiederholungen", 0).coerceAtLeast(0),
            besteSerie = werteJson.optInt("besteSerie", 0).coerceAtLeast(0),
            schnellsteMs = werteJson.optLong("schnellsteMs", 0L).coerceAtLeast(0L),
            sitzungen = werteJson.optInt("sitzungen", 0).coerceAtLeast(0),
        )

        val staende = LinkedHashMap<String, KompetenzStand>()
        val liste = wurzel.optJSONArray("staende") ?: JSONArray()
        for (i in 0 until liste.length()) {
            val eintrag = liste.optJSONObject(i) ?: continue
            val kennung = eintrag.optString("kennung")
            if (kennung.isEmpty()) continue
            val fehlerarten = LinkedHashMap<String, Int>()
            val fehlerJson = eintrag.optJSONObject("fehlerarten")
            if (fehlerJson != null) {
                for (art in fehlerJson.keys()) {
                    val anzahl = fehlerJson.optInt(art, 0)
                    if (anzahl > 0) fehlerarten[art] = anzahl
                }
            }
            staende[kennung] = KompetenzStand(
                kennung = kennung,
                grad = eintrag.optDouble("grad", 0.0).coerceIn(0.0, 1.0),
                versuche = eintrag.optInt("versuche", 0).coerceAtLeast(0),
                treffer = eintrag.optInt("treffer", 0).coerceAtLeast(0),
                zeitSummeMs = eintrag.optLong("zeitSummeMs", 0L).coerceAtLeast(0L),
                letzterKontakt = eintrag.optLong("letzterKontakt", 0L).coerceAtLeast(0L),
                naechsteWdh = eintrag.optLong("naechsteWdh", 0L).coerceAtLeast(0L),
                stufe = eintrag.optInt("stufe", 0).coerceIn(0, Wiederholung.STUFEN.lastIndex),
                wiederholungen = eintrag.optInt("wiederholungen", 0).coerceAtLeast(0),
                fehlerarten = fehlerarten,
            )
        }

        return Ablage(
            profil = profil,
            modus = wahl(wurzel.optString("modus"), Modus.entries, Modus.SYSTEM),
            staende = staende,
            werte = werte,
        )
    }

    private fun <T : Enum<T>> wahl(name: String, werte: List<T>, ersatz: T): T =
        werte.firstOrNull { it.name == name } ?: ersatz
}

/**
 * Abstandsbasiertes Wiederholungsmodell. Eine richtige Antwort schiebt die
 * Kompetenz eine Stufe weiter, eine falsche zwei Stufen zurueck. Pausen
 * kosten nichts: was faellig ist, bleibt faellig, mehr passiert nicht.
 */
object Wiederholung {

    private const val TAG = 24L * 60L * 60L * 1000L

    /** Abstaende in Tagen je Stufe. */
    val STUFEN = listOf(0L, 1L, 3L, 7L, 16L, 35L, 75L)

    /** Gewicht, mit dem eine einzelne Antwort den Beherrschungsgrad zieht. */
    private const val ZUG = 0.28
    private const val RUECKZUG = 0.55

    const val SICHER = 0.85
    const val IN_ARBEIT = 0.35

    fun naechster(stand: KompetenzStand, richtig: Boolean, jetzt: Long, dauerMs: Long): KompetenzStand {
        val stufe = if (richtig) {
            (stand.stufe + 1).coerceAtMost(STUFEN.lastIndex)
        } else {
            (stand.stufe - 2).coerceAtLeast(0)
        }
        val grad = if (richtig) {
            stand.grad + (1.0 - stand.grad) * ZUG
        } else {
            stand.grad * (1.0 - RUECKZUG)
        }
        return stand.copy(
            grad = grad.coerceIn(0.0, 1.0),
            versuche = stand.versuche + 1,
            treffer = stand.treffer + if (richtig) 1 else 0,
            zeitSummeMs = stand.zeitSummeMs + dauerMs,
            letzterKontakt = jetzt,
            naechsteWdh = jetzt + STUFEN[stufe] * TAG,
            stufe = stufe,
            wiederholungen = stand.wiederholungen + if (stand.versuche > 0) 1 else 0,
        )
    }

    /** Startwert nach dem Einstufungstest -- eine Antwort, kein voller Beweis. */
    fun ausEinstufung(kennung: String, richtig: Boolean, jetzt: Long): KompetenzStand =
        KompetenzStand(
            kennung = kennung,
            grad = if (richtig) 0.7 else 0.2,
            naechsteWdh = jetzt + if (richtig) 3L * TAG else 0L,
            stufe = if (richtig) 2 else 0,
            letzterKontakt = jetzt,
        )

    fun faellig(stand: KompetenzStand, jetzt: Long): Boolean =
        !stand.beruehrt || stand.naechsteWdh <= jetzt
}
