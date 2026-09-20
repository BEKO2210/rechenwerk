package de.rechenwerk.mathe.daten

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rechenwerk.mathe.daten.raum.AltdatenUebernahme
import de.rechenwerk.mathe.daten.raum.Lernablage
import de.rechenwerk.mathe.daten.raum.Lernstand
import javax.inject.Inject
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

/**
 * Wie eine Sitzung ausgegangen ist -- die Zahlen des Abschlussbilds.
 * [segmente] sind die erreichten Segmente, also die richtigen Antworten dieser
 * Einheit; [serie] ist die laengste Strecke darin. [naechsteKennung] und
 * [naechsteWdh] sagen, was als naechstes faellig ist.
 */
data class Bilanz(
    val gestellt: Int,
    val segmente: Int,
    val serie: Int,
    val dauerMs: Long,
    val naechsteKennung: String?,
    val naechsteWdh: Long,
)

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
 * Der Zustand der App. Die fachlichen Daten fuehrt die Room-Datenbank ueber
 * [Lernablage], die Einstellungen der Suite-Tresor ueber
 * [Einstellungsspeicher]. Beide werden eingespritzt; der Sitzungszustand lebt
 * hier und ueberdauert Drehungen des Geraets.
 */
@HiltViewModel
class Werk @Inject constructor(
    private val einstellungen: Einstellungsspeicher,
    private val lernablage: Lernablage,
) : ViewModel() {

    private val zufall = Random(System.nanoTime())

    /** Haelt die Schreibvorgaenge in der Reihenfolge, in der sie ausgeloest wurden. */
    private val schreibsperre = Mutex()

    /**
     * Der Start liest den Tresor: vor der Uebernahme steht dort noch der
     * vollstaendige alte Stand, danach nur noch Profil und Erscheinungsbild.
     * So steht das Profil sofort -- die Einrichtung blitzt nicht kurz auf,
     * waehrend die Datenbank oeffnet.
     */
    var ablage by mutableStateOf(einstellungen.lies())
        private set

    // ---- Trainingssitzung ---------------------------------------------------

    var aufgabe by mutableStateOf<Aufgabe?>(null)
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
    private var sitzungSerie = 0
    private var aufgabeBegonnen = 0L
    private var sitzungBegonnen = 0L
    private var zuletzt: String? = null

    /** Die offene Aufgabe wird genau einmal wiederhergestellt, nicht bei jedem Fluss. */
    private var wiederaufgenommen = false

    init {
        viewModelScope.launch {
            // Erst die einmalige Uebernahme, dann der Fluss -- und unter
            // derselben Sperre wie jeder andere Schreibvorgang, damit keine
            // fruehe Eingabe an ihr vorbeizieht.
            schreibsperre.withLock {
                if (AltdatenUebernahme.fuehreAus(lernablage, ablage)) {
                    val gemerkt = ablage.copy(nachRaumUebernommen = true)
                    ablage = gemerkt
                    withContext(Dispatchers.IO) { einstellungen.schreibe(gemerkt) }
                }
            }
            lernablage.stand.collect { stand -> uebernimmStand(stand) }
        }
    }

    /**
     * Uebernimmt einen Stand aus der Datenbank in den Speicher. Beim ersten Mal
     * wird eine offene Aufgabe aus ihrem Startwert neu gebaut: wer mitten in
     * einer Aufgabe die App verliert, findet genau sie wieder -- und sie faengt
     * jetzt an zu laufen, nicht 1970, sonst zaehlte die Pause als Bearbeitungszeit.
     */
    private fun uebernimmStand(stand: Lernstand) {
        ablage = ablage.copy(
            staende = stand.staende,
            werte = stand.werte,
            verlauf = stand.verlauf,
            laufend = stand.laufend,
        )
        if (wiederaufgenommen) return
        wiederaufgenommen = true
        val offen = stand.laufend ?: return
        aufgabe = Werkbank.erzeuge(offen.kompetenz, offen.niveau, offen.startwert)
        aufgabeBegonnen = System.currentTimeMillis()
        sitzungBegonnen = aufgabeBegonnen
        zuletzt = offen.kompetenz
    }

    val sitzungGestellt: Int get() = gestellt
    val sitzungRichtig: Int get() = anzahlRichtig

    /** Die laufende Serie -- die Strecke ueber dem Aufgabenfeld liest sie. */
    val serie: Int get() = ablage.werte.serie

    // ---- Einstufung ---------------------------------------------------------

    var pruefAufgabe by mutableStateOf<Aufgabe?>(null)
        private set
    var pruefNummer by mutableStateOf(0)
        private set
    private var pruefMuehe = 6
    private val pruefBenutzt = mutableSetOf<String>()
    private val pruefErgebnis = mutableMapOf<String, Boolean>()

    /**
     * Uebernimmt den neuen Stand sofort in den Speicher und schreibt ihn
     * abseits des Hauptfadens weg. Der Tresor macht ein fsync, das je nach
     * Geraet Hunderte Millisekunden dauert -- im Hauptfaden staut das die
     * Eingaben bis zum ANR. [NonCancellable] sorgt dafuer, dass ein bereits
     * ausgeloester Schreibvorgang auch dann fertig wird, wenn das Werk gerade
     * abgeraeumt wird.
     */
    private fun schreibe(arbeit: suspend () -> Unit) {
        viewModelScope.launch(NonCancellable) {
            schreibsperre.withLock { arbeit() }
        }
    }

    /** Eine Aenderung an den Einstellungen: Speicher und Tresor, sofort. */
    private fun sichereEinstellungen(neu: Ablage) {
        ablage = neu
        schreibe { withContext(Dispatchers.IO) { einstellungen.schreibe(neu) } }
    }

    // ---- Profil und Einstellungen -------------------------------------------

    fun setzeProfil(profil: Profil) = sichereEinstellungen(ablage.copy(profil = profil))

    fun setzeModus(modus: Modus) = sichereEinstellungen(ablage.copy(modus = modus))

    fun alsText(): String = Papier.schreibe(ablage)

    /** Uebernimmt eine importierte Datei. Gibt false zurueck, wenn sie unbrauchbar ist. */
    fun uebernimm(text: String): Boolean {
        val gelesen = try {
            Papier.lies(text)
        } catch (fehler: IllegalArgumentException) {
            return false
        }
        // Die Datei ist ab jetzt der Stand -- sie noch einmal uebernehmen zu
        // wollen, waere ein zweiter Durchlauf mit demselben Inhalt.
        val neu = gelesen.copy(nachRaumUebernommen = true)
        beendeSitzungStill()
        ablage = neu
        schreibe {
            lernablage.ersetzeAlles(AltdatenUebernahme.zeilenAus(neu))
            withContext(Dispatchers.IO) { einstellungen.schreibe(neu) }
        }
        return true
    }

    fun setzeZurueck() {
        beendeSitzungStill()
        bilanz = null
        // Das Merkzeichen bleibt: die Datenbank steht, sie ist nur leer. Ohne
        // es liefe beim naechsten Start die Uebernahme noch einmal an.
        val leer = Ablage(nachRaumUebernommen = true)
        ablage = leer
        schreibe {
            lernablage.ersetzeAlles(AltdatenUebernahme.zeilenAus(leer))
            withContext(Dispatchers.IO) { einstellungen.schreibe(leer) }
        }
    }

    // ---- Training -----------------------------------------------------------

    fun starteSitzung(neueArt: Art, kennung: String? = null) {
        art = neueArt
        ziel = kennung
        gestellt = 0
        anzahlRichtig = 0
        sitzungSerie = 0
        zuletzt = null
        bilanz = null
        sitzungBegonnen = System.currentTimeMillis()
        val werte = ablage.werte.copy(sitzungen = ablage.werte.sitzungen + 1)
        ablage = ablage.copy(werte = werte)
        schreibe { lernablage.schreibeWerte(werte) }
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
        val laufend = Laufend(kennung, startwert, niveau)
        ablage = ablage.copy(laufend = laufend)
        schreibe { lernablage.setzeLaufend(laufend) }
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

        gestellt += 1
        if (richtig) anzahlRichtig += 1

        val alteWerte = ablage.werte
        val werte = Serie.nachAntwort(alteWerte, richtig).copy(
            lernzeitMs = alteWerte.lernzeitMs + dauer,
            versuche = alteWerte.versuche + 1,
            treffer = alteWerte.treffer + if (richtig) 1 else 0,
            wiederholungen = alteWerte.wiederholungen + if (alt.beruehrt) 1 else 0,
            schnellsteMs = if (richtig && dauer > 0L) {
                if (alteWerte.schnellsteMs == 0L) dauer else minOf(alteWerte.schnellsteMs, dauer)
            } else {
                alteWerte.schnellsteMs
            },
        )
        sitzungSerie = maxOf(sitzungSerie, werte.serie)

        val eintrag = Verlaufseintrag(
            kompetenz = laufend.kompetenz,
            startwert = laufend.startwert,
            niveau = laufend.niveau,
            richtig = richtig,
            zeitpunkt = jetzt,
        )
        ablage = ablage.copy(
            staende = ablage.staende + (laufend.kompetenz to neu),
            verlauf = ablage.mitVerlauf(eintrag),
            laufend = null,
            werte = werte,
        )
        schreibe { lernablage.schreibeAntwort(neu, werte, eintrag) }
        rueckmeldung = Rueckmeldung(richtig, gegeben, fehlbild)
    }

    /** True, wenn die Fuenf-Minuten-Sitzung ihre Zeit voll hat. */
    fun zeitUm(): Boolean =
        art == Art.FUENF_MINUTEN && System.currentTimeMillis() - sitzungBegonnen >= 5L * 60L * 1000L

    fun beendeSitzung() {
        val faellig = naechsteFaellige()
        bilanz = if (gestellt > 0) {
            Bilanz(
                gestellt = gestellt,
                segmente = anzahlRichtig,
                serie = sitzungSerie,
                dauerMs = System.currentTimeMillis() - sitzungBegonnen,
                naechsteKennung = faellig?.first,
                naechsteWdh = faellig?.second ?: 0L,
            )
        } else {
            null
        }
        beendeSitzungStill()
    }

    /** Raeumt die Sitzung ab, ohne ein Abschlussbild zu stellen. */
    private fun beendeSitzungStill() {
        aufgabe = null
        rueckmeldung = null
        eingabe = ""
        hilfestufe = 0
        if (ablage.laufend != null) {
            ablage = ablage.copy(laufend = null)
            schreibe { lernablage.setzeLaufend(null) }
        }
    }

    fun verwirfBilanz() {
        bilanz = null
    }

    /** Was als naechstes drankommt: das am laengsten Faellige, sonst das Schwaechste. */
    private fun naechsteFaellige(): Pair<String, Long>? {
        val pensum = Katalog.fuer(ablage.profil)
        val gewaehlt = pensum
            .filter { ablage.stand(it.kennung).beruehrt }
            .minByOrNull { ablage.stand(it.kennung).naechsteWdh }
            ?: pensum.minByOrNull { ablage.stand(it.kennung).grad }
            ?: return null
        return gewaehlt.kennung to ablage.stand(gewaehlt.kennung).naechsteWdh
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
        val neu = ablage.copy(profil = profil.copy(eingerichtet = true), staende = staende)
        ablage = neu
        schreibe {
            lernablage.ersetzeAlles(AltdatenUebernahme.zeilenAus(neu))
            withContext(Dispatchers.IO) { einstellungen.schreibe(neu) }
        }
        pruefAufgabe = null
        pruefNummer = 0
        eingabe = ""
    }

    companion object {
        const val PRUEF_ANZAHL = 10
    }
}
