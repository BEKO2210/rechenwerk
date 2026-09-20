package de.rechenwerk.mathe.daten

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.ithandwerkstuttgart.suitekern.Tresor
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Woher eine Trainingssitzung ihre Aufgaben nimmt. */
enum class Art { FUENF_MINUTEN, WEITERLERNEN, SCHWAECHEN, GEZIELT }

data class Rueckmeldung(val richtig: Boolean, val gegeben: Bruch?, val fehlbild: Fehlbild?)

/** Wie eine Sitzung ausgegangen ist -- fuer die Abschlusskarte im Training. */
data class Bilanz(val gestellt: Int, val anzahlRichtig: Int, val dauerMs: Long)

/**
 * Waehlt die naechste Kompetenz. Faelliges zuerst, danach das Schwaechste --
 * und nie zweimal hintereinander dieselbe, solange es Alternativen gibt.
 */
object Auswahl {

    fun naechste(
        art: Art,
        ziel: String?,
        ablage: Ablage,
        jetzt: Long,
        zufall: Random,
        zuletzt: String?,
    ): String {
        if (art == Art.GEZIELT && ziel != null) return ziel

        val kandidaten = Katalog.fuer(ablage.profil)
        val ohneLetzte = kandidaten.filter { it.kennung != zuletzt }.ifEmpty { kandidaten }

        val gewaehlt = when (art) {
            Art.SCHWAECHEN -> ohneLetzte
                .filter { ablage.stand(it.kennung).grad < Wiederholung.SICHER }
                .minByOrNull { ablage.stand(it.kennung).grad * 100 + it.muehe }

            Art.WEITERLERNEN -> ohneLetzte
                .filter { ablage.stand(it.kennung).grad < Wiederholung.SICHER }
                .filter { bereit(it, ablage) }
                .minByOrNull { it.muehe }

            else -> {
                val faellig = ohneLetzte.filter { Wiederholung.faellig(ablage.stand(it.kennung), jetzt) }
                val topf = faellig.ifEmpty { ohneLetzte }
                gewichtet(topf, ablage, zufall)
            }
        }
        return (gewaehlt ?: ohneLetzte.first()).kennung
    }

    /** Sind alle Voraussetzungen sicher genug, um hier weiterzugehen? */
    private fun bereit(kompetenz: Kompetenz, ablage: Ablage): Boolean =
        Katalog.voraussetzungenFuer(kompetenz, ablage.profil.niveau)
            .all { ablage.stand(it).grad >= Wiederholung.IN_ARBEIT }

    private fun gewichtet(topf: List<Kompetenz>, ablage: Ablage, zufall: Random): Kompetenz? {
        if (topf.isEmpty()) return null
        val gewichte = topf.map { 1.0 - ablage.stand(it.kennung).grad + 0.15 }
        val summe = gewichte.sum()
        var wurf = zufall.nextDouble() * summe
        for (i in topf.indices) {
            wurf -= gewichte[i]
            if (wurf <= 0.0) return topf[i]
        }
        return topf.last()
    }
}

/**
 * Der Zustand der App. Alles Dauerhafte liegt als JSON im Tresor; der
 * Sitzungszustand lebt hier und ueberdauert Drehungen des Geraets.
 */
class Werk(anwendung: Application) : AndroidViewModel(anwendung) {

    private val tresor = Tresor(anwendung, "rechenwerk")
    private val zufall = Random(System.nanoTime())

    /** Haelt die Schreibvorgaenge in der Reihenfolge, in der sie ausgeloest wurden. */
    private val schreibsperre = Mutex()

    var ablage by mutableStateOf(lade())
        private set

    // ---- Trainingssitzung ---------------------------------------------------

    /**
     * Eine offene Aufgabe aus dem Tresor wird aus ihrem Startwert neu gebaut.
     * Wer mitten in einer Aufgabe die App verliert, findet genau sie wieder.
     */
    var aufgabe by mutableStateOf(
        ablage.laufend?.let { Werkbank.erzeuge(it.kompetenz, it.niveau, it.startwert) }
    )
        private set
    var eingabe by mutableStateOf("")
        private set
    var hilfestufe by mutableStateOf(0)
        private set
    var rueckmeldung by mutableStateOf<Rueckmeldung?>(null)
        private set
    var bilanz by mutableStateOf<Bilanz?>(null)
        private set

    var art by mutableStateOf(Art.FUENF_MINUTEN)
        private set
    var ziel by mutableStateOf<String?>(null)
        private set

    private var gestellt = 0
    private var anzahlRichtig = 0
    private var serie = 0
    private var aufgabeBegonnen = 0L
    private var sitzungBegonnen = 0L
    private var zuletzt: String? = null

    init {
        // Eine aus dem Tresor wiederhergestellte Aufgabe faengt jetzt an zu
        // laufen, nicht 1970 -- sonst zaehlte die Pause als Bearbeitungszeit.
        val wiederaufgenommen = aufgabe
        if (wiederaufgenommen != null) {
            aufgabeBegonnen = System.currentTimeMillis()
            sitzungBegonnen = aufgabeBegonnen
            zuletzt = wiederaufgenommen.kompetenz
        }
    }

    val sitzungGestellt: Int get() = gestellt
    val sitzungRichtig: Int get() = anzahlRichtig

    // ---- Einstufung ---------------------------------------------------------

    var pruefAufgabe by mutableStateOf<Aufgabe?>(null)
        private set
    var pruefNummer by mutableStateOf(0)
        private set
    private var pruefMuehe = 6
    private val pruefBenutzt = mutableSetOf<String>()
    private val pruefErgebnis = mutableMapOf<String, Boolean>()

    private fun lade(): Ablage {
        val roh = tresor.lesen() ?: return Ablage()
        return try {
            Papier.lies(roh)
        } catch (fehler: IllegalArgumentException) {
            Ablage()
        }
    }

    /**
     * Uebernimmt den neuen Stand sofort in den Speicher und schreibt ihn
     * abseits des Hauptfadens auf die Platte. Der Tresor macht ein fsync, das
     * je nach Geraet Hunderte Millisekunden dauert -- bei jeder Antwort und
     * jeder Profilaenderung im Hauptfaden staut das die Eingaben bis zum ANR.
     * [NonCancellable] sorgt dafuer, dass ein bereits ausgeloester Schreibvorgang
     * auch dann fertig wird, wenn das Werk gerade abgeraeumt wird.
     */
    private fun sichere(neu: Ablage) {
        ablage = neu
        viewModelScope.launch(NonCancellable) {
            schreibsperre.withLock {
                withContext(Dispatchers.IO) { tresor.schreiben(Papier.schreibe(neu)) }
            }
        }
    }

    // ---- Profil und Einstellungen -------------------------------------------

    fun setzeProfil(profil: Profil) = sichere(ablage.copy(profil = profil))

    fun setzeModus(modus: Modus) = sichere(ablage.copy(modus = modus))

    fun alsText(): String = Papier.schreibe(ablage)

    /** Uebernimmt eine importierte Datei. Gibt false zurueck, wenn sie unbrauchbar ist. */
    fun uebernimm(text: String): Boolean = try {
        sichere(Papier.lies(text))
        true
    } catch (fehler: IllegalArgumentException) {
        false
    }

    fun setzeZurueck() {
        beendeSitzung()
        sichere(Ablage())
    }

    // ---- Training -----------------------------------------------------------

    fun starteSitzung(neueArt: Art, kennung: String? = null) {
        art = neueArt
        ziel = kennung
        gestellt = 0
        anzahlRichtig = 0
        serie = 0
        zuletzt = null
        bilanz = null
        sitzungBegonnen = System.currentTimeMillis()
        sichere(ablage.copy(werte = ablage.werte.copy(sitzungen = ablage.werte.sitzungen + 1)))
        naechsteAufgabe()
    }

    fun naechsteAufgabe() {
        val jetzt = System.currentTimeMillis()
        val kennung = Auswahl.naechste(art, ziel, ablage, jetzt, zufall, zuletzt)
        val niveau = ablage.profil.niveau
        // Der Startwert ist die Aufgabe: aus ihm entsteht sie jederzeit neu.
        val startwert = zufall.nextLong()
        zuletzt = kennung
        aufgabe = Werkbank.erzeuge(kennung, niveau, startwert)
        eingabe = ""
        hilfestufe = 0
        rueckmeldung = null
        aufgabeBegonnen = jetzt
        sichere(ablage.copy(laufend = Laufend(kennung, startwert, niveau)))
    }

    fun tippe(zeichen: String) {
        if (rueckmeldung != null) return
        if (eingabe.length >= 12) return
        eingabe = when {
            zeichen == Bruch.MINUS && eingabe.isEmpty() -> Bruch.MINUS
            zeichen == Bruch.MINUS -> eingabe
            zeichen == "," && (eingabe.isEmpty() || eingabe.endsWith(",") || eingabe.contains("/")) -> eingabe
            zeichen == "/" && (eingabe.isEmpty() || eingabe.contains("/") || eingabe.contains(",")) -> eingabe
            else -> eingabe + zeichen
        }
    }

    fun loesche() {
        if (rueckmeldung != null) return
        if (eingabe.isNotEmpty()) eingabe = eingabe.dropLast(1)
    }

    fun mehrHilfe() {
        if (hilfestufe < 4) hilfestufe += 1
    }

    fun sende() {
        val laufend = aufgabe ?: return
        if (rueckmeldung != null) return
        val gegeben = Bruch.lies(eingabe) ?: return
        val jetzt = System.currentTimeMillis()
        val dauer = (jetzt - aufgabeBegonnen).coerceIn(0L, 10L * 60L * 1000L)
        val richtig = gegeben == laufend.loesung
        // Wer den vollstaendigen Weg aufgeschlagen hat, bekommt die Kompetenz
        // nicht gutgeschrieben -- die Rueckmeldung bleibt trotzdem freundlich.
        val alsTreffer = richtig && hilfestufe < 4

        val alt = ablage.stand(laufend.kompetenz)
        var neu = Wiederholung.naechster(alt, alsTreffer, jetzt, dauer)
        val fehlbild = if (richtig) null else laufend.fehlbildZu(gegeben)
        if (fehlbild != null) {
            val gezaehlt = neu.fehlerarten.toMutableMap()
            gezaehlt[fehlbild.kennung] = (gezaehlt[fehlbild.kennung] ?: 0) + 1
            neu = neu.copy(fehlerarten = gezaehlt)
        }

        serie = if (richtig) serie + 1 else 0
        gestellt += 1
        if (richtig) anzahlRichtig += 1

        val werte = ablage.werte
        val eintrag = Verlaufseintrag(
            kompetenz = laufend.kompetenz,
            startwert = laufend.startwert,
            niveau = laufend.niveau,
            richtig = richtig,
            zeitpunkt = jetzt,
        )
        sichere(
            ablage.copy(
                staende = ablage.staende + (laufend.kompetenz to neu),
                verlauf = ablage.mitVerlauf(eintrag),
                laufend = null,
                werte = werte.copy(
                    lernzeitMs = werte.lernzeitMs + dauer,
                    versuche = werte.versuche + 1,
                    treffer = werte.treffer + if (richtig) 1 else 0,
                    wiederholungen = werte.wiederholungen + if (alt.beruehrt) 1 else 0,
                    besteSerie = maxOf(werte.besteSerie, serie),
                    schnellsteMs = if (richtig && dauer > 0L) {
                        if (werte.schnellsteMs == 0L) dauer else minOf(werte.schnellsteMs, dauer)
                    } else {
                        werte.schnellsteMs
                    },
                ),
            )
        )
        rueckmeldung = Rueckmeldung(richtig, gegeben, fehlbild)
    }

    /** True, wenn die Fuenf-Minuten-Sitzung ihre Zeit voll hat. */
    fun zeitUm(): Boolean =
        art == Art.FUENF_MINUTEN && System.currentTimeMillis() - sitzungBegonnen >= 5L * 60L * 1000L

    fun beendeSitzung() {
        bilanz = if (gestellt > 0) {
            Bilanz(gestellt, anzahlRichtig, System.currentTimeMillis() - sitzungBegonnen)
        } else {
            null
        }
        aufgabe = null
        rueckmeldung = null
        eingabe = ""
        hilfestufe = 0
        if (ablage.laufend != null) sichere(ablage.copy(laufend = null))
    }

    fun verwirfBilanz() {
        bilanz = null
    }

    // ---- Einstufungstest ----------------------------------------------------

    fun startePruefung() {
        pruefNummer = 0
        pruefMuehe = 6
        pruefBenutzt.clear()
        pruefErgebnis.clear()
        eingabe = ""
        rueckmeldung = null
        pruefSchritt()
    }

    private fun pruefSchritt() {
        // Der Test laeuft die Schwierigkeitsachse des eigenen Profils ab, nicht
        // die des ganzen Katalogs -- sonst pruefte er Stoff anderer Klassenstufen.
        val frei = Katalog.fuer(ablage.profil)
            .sortedBy { it.muehe }
            .filter { it.kennung !in pruefBenutzt }
        if (frei.isEmpty()) {
            pruefAufgabe = null
            return
        }
        val gewaehlt = frei.minByOrNull { kotlin.math.abs(it.muehe - pruefMuehe) } ?: frei.first()
        pruefBenutzt += gewaehlt.kennung
        pruefAufgabe = Werkbank.erzeuge(gewaehlt.kennung, ablage.profil.niveau, zufall.nextLong())
        eingabe = ""
    }

    /** Beantwortet die laufende Testaufgabe und rueckt weiter. */
    fun pruefeAntwort() {
        val laufend = pruefAufgabe ?: return
        val gegeben = Bruch.lies(eingabe)
        val richtig = gegeben != null && gegeben == laufend.loesung
        pruefErgebnis[laufend.kompetenz] = richtig
        pruefMuehe = if (richtig) pruefMuehe + 3 else pruefMuehe - 3
        pruefMuehe = pruefMuehe.coerceIn(1, Katalog.alle.size)
        pruefNummer += 1
        if (pruefNummer >= PRUEF_ANZAHL) pruefAufgabe = null else pruefSchritt()
    }

    fun ueberspringePruefaufgabe() {
        pruefNummer += 1
        if (pruefNummer >= PRUEF_ANZAHL) pruefAufgabe = null else pruefSchritt()
    }

    /** Schreibt die Startwerte aus dem Test fest und schliesst das Onboarding ab. */
    fun schliessePruefungAb(profil: Profil) {
        val jetzt = System.currentTimeMillis()
        val staende = LinkedHashMap<String, KompetenzStand>()
        for ((kennung, richtig) in pruefErgebnis) {
            staende[kennung] = Wiederholung.ausEinstufung(kennung, richtig, jetzt)
        }
        sichere(
            ablage.copy(
                profil = profil.copy(eingerichtet = true),
                staende = staende,
            )
        )
        pruefAufgabe = null
        pruefNummer = 0
        eingabe = ""
    }

    companion object {
        const val PRUEF_ANZAHL = 10
    }
}
