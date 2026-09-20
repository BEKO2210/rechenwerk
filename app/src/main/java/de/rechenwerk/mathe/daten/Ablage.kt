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

/**
 * Zahlen ueber alle Kompetenzen hinweg. Eine frische Installation hat hier
 * ueberall 0. [serie] ist die laufende Strecke richtiger Antworten,
 * [besteSerie] ihr Hoechststand.
 */
data class Werte(
    val lernzeitMs: Long = 0L,
    val versuche: Int = 0,
    val treffer: Int = 0,
    val wiederholungen: Int = 0,
    val serie: Int = 0,
    val besteSerie: Int = 0,
    val schnellsteMs: Long = 0L,
    val sitzungen: Int = 0,
)

/**
 * Die Serie: wie viele Aufgaben hintereinander richtig waren. Jeder Treffer
 * verlaengert sie um eins, ein Fehler wirft sie auf null zurueck. Der
 * Hoechststand bleibt stehen -- eine verlorene Serie loescht keinen Bestwert.
 */
object Serie {

    fun nachAntwort(werte: Werte, richtig: Boolean): Werte {
        val laufend = if (richtig) werte.serie + 1 else 0
        return werte.copy(serie = laufend, besteSerie = maxOf(werte.besteSerie, laufend))
    }

    /** So viele Segmente hat eine Runde auf der Serien-Strecke. */
    const val RUNDE = 8
}

/**
 * Eine beantwortete Aufgabe in der Historie. Gespeichert wird nicht die Aufgabe
 * selbst, sondern ihr [startwert]: aus Kompetenz, Niveau und Startwert entsteht
 * sie jederzeit wieder Zeichen fuer Zeichen gleich.
 */
data class Verlaufseintrag(
    val kompetenz: String,
    val startwert: Long,
    val niveau: Niveau,
    val richtig: Boolean,
    val zeitpunkt: Long,
)

/**
 * Die gerade gestellte, noch unbeantwortete Aufgabe. Sie steht mit im Tresor,
 * damit ein Neustart mitten in der Sitzung genau dieselbe Aufgabe wieder zeigt.
 */
data class Laufend(
    val kompetenz: String,
    val startwert: Long,
    val niveau: Niveau,
)

/**
 * Alles, was einen Neustart ueberlebt. Zwei Speicher tragen sie gemeinsam:
 * [profil], [modus] und [nachRaumUebernommen] liegen als JSON im Suite-Tresor,
 * die fachlichen Felder in der Room-Datenbank. Beim Export und beim Import
 * steht sie vollstaendig in einer Datei.
 */
data class Ablage(
    val profil: Profil = Profil(),
    // Eine frische Installation folgt dem Anzeigemodus des Systems. Der dunkle
    // Entwurf bleibt der gestalterische Ausgangspunkt, aber wer sein Telefon
    // hell gestellt hat, bekommt eine helle Oberflaeche.
    val modus: Modus = Modus.SYSTEM,
    val staende: Map<String, KompetenzStand> = emptyMap(),
    val werte: Werte = Werte(),
    val verlauf: List<Verlaufseintrag> = emptyList(),
    val laufend: Laufend? = null,
    /** Merkzeichen: der alte JSON-Stand ist einmalig nach Room gewandert. */
    val nachRaumUebernommen: Boolean = false,
) {
    fun stand(kennung: String): KompetenzStand = staende[kennung] ?: KompetenzStand(kennung)

    /**
     * Der Teil, der im Suite-Tresor steht: Profil, Erscheinungsbild und das
     * Merkzeichen der Uebernahme. Die fachlichen Felder fuehrt Room; im Tresor
     * bleiben sie leer, damit nicht zwei Speicher dieselbe Wahrheit behaupten.
     */
    fun nurEinstellungen(): Ablage =
        Ablage(profil = profil, modus = modus, nachRaumUebernommen = nachRaumUebernommen)

    /** Haengt einen Eintrag an die Historie und haelt sie auf [VERLAUF_MAX] kurz. */
    fun mitVerlauf(eintrag: Verlaufseintrag): List<Verlaufseintrag> =
        (verlauf + eintrag).takeLast(VERLAUF_MAX)

    companion object {
        /** So viele Aufgaben bleiben in der Historie stehen. */
        const val VERLAUF_MAX = 200
    }
}

/**
 * JSON-Umwandlung. Liegt bewusst ohne Android-Abhaengigkeit hier, damit die
 * Unit-Tests sie ohne Geraet pruefen koennen.
 */
object Papier {

    const val FORMAT = "rechenwerk"

    /**
     * Fassung 2 fuehrt die Aufgabenhistorie mit Startwerten, Fassung 3 die
     * laufende Serie und das Merkzeichen der Room-Uebernahme. Aeltere
     * Fassungen werden weiter gelesen; ihre neuen Felder stehen auf null.
     */
    const val FASSUNG = 3

    fun schreibe(ablage: Ablage): String {
        val wurzel = JSONObject()
        wurzel.put("format", FORMAT)
        wurzel.put("fassung", FASSUNG)
        wurzel.put("nachRaumUebernommen", ablage.nachRaumUebernommen)

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
        werte.put("serie", ablage.werte.serie)
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

        val historie = JSONArray()
        for (eintrag in ablage.verlauf) {
            val satz = JSONObject()
            satz.put("kompetenz", eintrag.kompetenz)
            satz.put("startwert", eintrag.startwert)
            satz.put("niveau", eintrag.niveau.name)
            satz.put("richtig", eintrag.richtig)
            satz.put("zeitpunkt", eintrag.zeitpunkt)
            historie.put(satz)
        }
        wurzel.put("verlauf", historie)

        val offen = ablage.laufend
        if (offen != null) {
            val satz = JSONObject()
            satz.put("kompetenz", offen.kompetenz)
            satz.put("startwert", offen.startwert)
            satz.put("niveau", offen.niveau.name)
            wurzel.put("laufend", satz)
        }

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
            serie = werteJson.optInt("serie", 0).coerceAtLeast(0),
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

        val verlauf = mutableListOf<Verlaufseintrag>()
        val historie = wurzel.optJSONArray("verlauf") ?: JSONArray()
        for (i in 0 until historie.length()) {
            val satz = historie.optJSONObject(i) ?: continue
            val kompetenz = satz.optString("kompetenz")
            if (kompetenz.isEmpty()) continue
            verlauf += Verlaufseintrag(
                kompetenz = kompetenz,
                startwert = satz.optLong("startwert", 0L),
                niveau = wahl(satz.optString("niveau"), Niveau.entries, Niveau.M),
                richtig = satz.optBoolean("richtig", false),
                zeitpunkt = satz.optLong("zeitpunkt", 0L).coerceAtLeast(0L),
            )
        }

        val offenJson = wurzel.optJSONObject("laufend")
        val laufend = if (offenJson != null && offenJson.optString("kompetenz").isNotEmpty()) {
            Laufend(
                kompetenz = offenJson.optString("kompetenz"),
                startwert = offenJson.optLong("startwert", 0L),
                niveau = wahl(offenJson.optString("niveau"), Niveau.entries, Niveau.M),
            )
        } else {
            null
        }

        return Ablage(
            profil = profil,
            modus = wahl(wurzel.optString("modus"), Modus.entries, Modus.SYSTEM),
            staende = staende,
            werte = werte,
            verlauf = verlauf.takeLast(Ablage.VERLAUF_MAX),
            laufend = laufend,
            nachRaumUebernommen = wurzel.optBoolean("nachRaumUebernommen", false),
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
