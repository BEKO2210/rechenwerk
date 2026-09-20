package de.rechenwerk.mathe.daten

import androidx.annotation.StringRes
import de.rechenwerk.mathe.R

/** Kompetenzbereich -- die grobe Schublade, in der der Startbildschirm sortiert. */
enum class Bereich(@StringRes val titel: Int) {
    GRUNDRECHNEN(R.string.bereich_grundrechnen),
    BRUECHE(R.string.bereich_brueche),
    DEZIMAL(R.string.bereich_dezimal),
    NEGATIV(R.string.bereich_negativ),
    PROZENT(R.string.bereich_prozent),
    GLEICHUNGEN(R.string.bereich_gleichungen),
    POTENZEN(R.string.bereich_potenzen),
    GEOMETRIE(R.string.bereich_geometrie),
}

/**
 * Eine einzelne Kompetenz. [muehe] ordnet sie auf einer Schwierigkeitsachse ein;
 * der Einstufungstest laeuft entlang dieser Achse auf und ab. [abNiveau] sagt,
 * ab welchem Niveau die Kompetenz ueberhaupt vorkommt: G heisst in allen drei
 * Stufen, M erst ab dem mittleren, E nur im erweiterten Niveau.
 */
data class Kompetenz(
    val kennung: String,
    @StringRes val name: Int,
    val bereich: Bereich,
    val abKlasse: Int,
    val muehe: Int,
    val abNiveau: Niveau = Niveau.G,
    val voraussetzungen: List<String> = emptyList(),
) {
    fun giltFuer(niveau: Niveau): Boolean = niveau.ordinal >= abNiveau.ordinal
}

/**
 * Der Kompetenzkatalog. Grundlage ist der Bildungsplan Baden-Wuerttemberg 2016,
 * Mathematik, Sekundarstufe I; [Kompetenz.abKlasse] und [Kompetenz.abNiveau]
 * halten die Einordnung fest, die auch der Einstufungstest benutzt.
 *
 * Die Einordnung ist ein Arbeitsstand und an der Quelle zu pruefen: der
 * Bildungsplan buendelt die Sekundarstufe I teilweise ueber Klassenstufen
 * hinweg, die hier genannte Einzelklasse ist eine Einordnung des Rechenwerks.
 * Offen ist insbesondere, ob GE-KOE-1 und GL-LGS-1 je nach Schulart in Klasse 9
 * statt 10 gehoeren und ob PO-RAT-1 noch in die Sekundarstufe I faellt.
 */
object Katalog {

    val alle: List<Kompetenz> = listOf(
        Kompetenz("GR-MUL-1", R.string.k_gr_mul_1, Bereich.GRUNDRECHNEN, 5, 1),
        Kompetenz("GR-ADD-1", R.string.k_gr_add_1, Bereich.GRUNDRECHNEN, 5, 2),
        Kompetenz("GR-DIV-1", R.string.k_gr_div_1, Bereich.GRUNDRECHNEN, 5, 3, voraussetzungen = listOf("GR-MUL-1")),
        Kompetenz("GR-MUL-2", R.string.k_gr_mul_2, Bereich.GRUNDRECHNEN, 5, 5, voraussetzungen = listOf("GR-MUL-1")),

        Kompetenz("DZ-ADD-1", R.string.k_dz_add_1, Bereich.DEZIMAL, 6, 4, voraussetzungen = listOf("GR-ADD-1")),
        Kompetenz("DZ-MUL-1", R.string.k_dz_mul_1, Bereich.DEZIMAL, 6, 7, voraussetzungen = listOf("GR-MUL-2")),
        Kompetenz("DZ-BRU-1", R.string.k_dz_bru_1, Bereich.DEZIMAL, 6, 8, abNiveau = Niveau.M, voraussetzungen = listOf("BR-KUE-1")),

        Kompetenz("BR-KUE-1", R.string.k_br_kue_1, Bereich.BRUECHE, 6, 6, voraussetzungen = listOf("GR-DIV-1")),
        Kompetenz("BR-ADD-1", R.string.k_br_add_1, Bereich.BRUECHE, 6, 9, voraussetzungen = listOf("BR-KUE-1")),
        Kompetenz("BR-MUL-1", R.string.k_br_mul_1, Bereich.BRUECHE, 6, 10, abNiveau = Niveau.M, voraussetzungen = listOf("BR-KUE-1")),
        Kompetenz("BR-DIV-1", R.string.k_br_div_1, Bereich.BRUECHE, 7, 12, abNiveau = Niveau.M, voraussetzungen = listOf("BR-MUL-1")),

        Kompetenz("NZ-ADD-1", R.string.k_nz_add_1, Bereich.NEGATIV, 7, 11, voraussetzungen = listOf("GR-ADD-1")),
        Kompetenz("NZ-MUL-1", R.string.k_nz_mul_1, Bereich.NEGATIV, 7, 13, abNiveau = Niveau.M, voraussetzungen = listOf("NZ-ADD-1")),

        Kompetenz("PZ-VON-1", R.string.k_pz_von_1, Bereich.PROZENT, 7, 14, voraussetzungen = listOf("DZ-MUL-1")),
        Kompetenz("PZ-SAT-1", R.string.k_pz_sat_1, Bereich.PROZENT, 8, 16, abNiveau = Niveau.M, voraussetzungen = listOf("PZ-VON-1")),

        Kompetenz("GL-TER-1", R.string.k_gl_ter_1, Bereich.GLEICHUNGEN, 7, 15, voraussetzungen = listOf("NZ-MUL-1")),
        Kompetenz("GL-LIN-1", R.string.k_gl_lin_1, Bereich.GLEICHUNGEN, 8, 17, abNiveau = Niveau.M, voraussetzungen = listOf("GL-TER-1")),
        Kompetenz("GL-KLA-1", R.string.k_gl_kla_1, Bereich.GLEICHUNGEN, 8, 18, abNiveau = Niveau.E, voraussetzungen = listOf("GL-LIN-1")),

        // ---- Klasse 9 --------------------------------------------------------
        Kompetenz("GE-PYT-1", R.string.k_ge_pyt_1, Bereich.GEOMETRIE, 9, 19, voraussetzungen = listOf("GR-MUL-2")),
        Kompetenz("PO-WUR-1", R.string.k_po_wur_1, Bereich.POTENZEN, 9, 20, abNiveau = Niveau.M, voraussetzungen = listOf("GE-PYT-1", "BR-KUE-1")),
        Kompetenz("GE-STR-1", R.string.k_ge_str_1, Bereich.GEOMETRIE, 9, 21, abNiveau = Niveau.M, voraussetzungen = listOf("BR-MUL-1", "GL-LIN-1")),
        Kompetenz("PZ-ZIN-1", R.string.k_pz_zin_1, Bereich.PROZENT, 9, 22, abNiveau = Niveau.M, voraussetzungen = listOf("PZ-SAT-1")),
        Kompetenz("GL-QUA-1", R.string.k_gl_qua_1, Bereich.GLEICHUNGEN, 9, 23, abNiveau = Niveau.M, voraussetzungen = listOf("GL-KLA-1", "PO-WUR-1")),

        // ---- Klasse 10 -------------------------------------------------------
        Kompetenz("GE-KOE-1", R.string.k_ge_koe_1, Bereich.GEOMETRIE, 10, 24, voraussetzungen = listOf("GE-PYT-1")),
        Kompetenz("GL-LGS-1", R.string.k_gl_lgs_1, Bereich.GLEICHUNGEN, 10, 25, abNiveau = Niveau.M, voraussetzungen = listOf("GL-LIN-1")),
        Kompetenz("PO-RAT-1", R.string.k_po_rat_1, Bereich.POTENZEN, 10, 26, abNiveau = Niveau.E, voraussetzungen = listOf("PO-WUR-1")),
    )

    private val nachKennung: Map<String, Kompetenz> = alle.associateBy { it.kennung }

    fun finde(kennung: String): Kompetenz? = nachKennung[kennung]

    /**
     * Kompetenzen eines Bereichs, die zum Profil passen. Ein Fuenftklaessler
     * bekommt hier keine Kompetenz der Klasse 10 zu sehen -- und damit auch
     * keinen Beherrschungsgrad, der an Stoff haengt, den er nie zu Gesicht
     * bekommt.
     */
    fun imBereich(bereich: Bereich, profil: Profil): List<Kompetenz> =
        fuer(profil).filter { it.bereich == bereich }

    /** Kompetenzen, die zu Klasse und Niveau des Profils passen. */
    fun fuer(profil: Profil): List<Kompetenz> {
        val passend = alle.filter { it.abKlasse <= profil.klasse && it.giltFuer(profil.niveau) }
        return passend.ifEmpty { alle.filter { it.muehe <= 4 } }
    }

    /**
     * Die Voraussetzungen, die auf diesem Niveau wirklich gelten. Eine
     * Voraussetzung, die erst ab einem hoeheren Niveau vorkommt, hat auf
     * diesem Niveau niemand ueben koennen -- an ihre Stelle treten ihre
     * eigenen Voraussetzungen, damit die Kette nicht abreisst. Ohne das
     * bliebe etwa GL-TER-1 im Niveau G fuer immer gesperrt, weil seine
     * Voraussetzung NZ-MUL-1 dort gar nicht vorkommt.
     */
    fun voraussetzungenFuer(kompetenz: Kompetenz, niveau: Niveau): List<String> {
        val gefunden = LinkedHashSet<String>()
        fun sammle(kennungen: List<String>) {
            for (kennung in kennungen) {
                val vorher = nachKennung[kennung] ?: continue
                if (vorher.giltFuer(niveau)) gefunden += kennung else sammle(vorher.voraussetzungen)
            }
        }
        sammle(kompetenz.voraussetzungen)
        return gefunden.toList()
    }

    /** Bereiche, in denen das Profil ueberhaupt etwas zu ueben hat. */
    fun bereicheFuer(profil: Profil): List<Bereich> {
        val vorhanden = fuer(profil).map { it.bereich }.toSet()
        return Bereich.entries.filter { it in vorhanden }
    }
}
