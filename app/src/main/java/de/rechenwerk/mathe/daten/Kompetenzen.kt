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
}

/**
 * Eine einzelne Kompetenz. [muehe] ordnet sie auf einer Schwierigkeitsachse ein;
 * der Einstufungstest laeuft entlang dieser Achse auf und ab.
 */
data class Kompetenz(
    val kennung: String,
    @StringRes val name: Int,
    val bereich: Bereich,
    val abKlasse: Int,
    val muehe: Int,
    val niveaus: Set<Niveau> = setOf(Niveau.G, Niveau.M, Niveau.E),
    val voraussetzungen: List<String> = emptyList(),
)

object Katalog {

    val alle: List<Kompetenz> = listOf(
        Kompetenz("GR-MUL-1", R.string.k_gr_mul_1, Bereich.GRUNDRECHNEN, 5, 1),
        Kompetenz("GR-ADD-1", R.string.k_gr_add_1, Bereich.GRUNDRECHNEN, 5, 2),
        Kompetenz("GR-DIV-1", R.string.k_gr_div_1, Bereich.GRUNDRECHNEN, 5, 3, voraussetzungen = listOf("GR-MUL-1")),
        Kompetenz("GR-MUL-2", R.string.k_gr_mul_2, Bereich.GRUNDRECHNEN, 5, 5, voraussetzungen = listOf("GR-MUL-1")),

        Kompetenz("DZ-ADD-1", R.string.k_dz_add_1, Bereich.DEZIMAL, 6, 4, voraussetzungen = listOf("GR-ADD-1")),
        Kompetenz("DZ-MUL-1", R.string.k_dz_mul_1, Bereich.DEZIMAL, 6, 7, voraussetzungen = listOf("GR-MUL-2")),
        Kompetenz("DZ-BRU-1", R.string.k_dz_bru_1, Bereich.DEZIMAL, 6, 8, voraussetzungen = listOf("BR-KUE-1")),

        Kompetenz("BR-KUE-1", R.string.k_br_kue_1, Bereich.BRUECHE, 6, 6, voraussetzungen = listOf("GR-DIV-1")),
        Kompetenz("BR-ADD-1", R.string.k_br_add_1, Bereich.BRUECHE, 6, 9, voraussetzungen = listOf("BR-KUE-1")),
        Kompetenz("BR-MUL-1", R.string.k_br_mul_1, Bereich.BRUECHE, 6, 10, voraussetzungen = listOf("BR-KUE-1")),
        Kompetenz("BR-DIV-1", R.string.k_br_div_1, Bereich.BRUECHE, 7, 12, voraussetzungen = listOf("BR-MUL-1")),

        Kompetenz("NZ-ADD-1", R.string.k_nz_add_1, Bereich.NEGATIV, 7, 11, voraussetzungen = listOf("GR-ADD-1")),
        Kompetenz("NZ-MUL-1", R.string.k_nz_mul_1, Bereich.NEGATIV, 7, 13, voraussetzungen = listOf("NZ-ADD-1")),

        Kompetenz("PZ-VON-1", R.string.k_pz_von_1, Bereich.PROZENT, 7, 14, voraussetzungen = listOf("DZ-MUL-1")),
        Kompetenz("PZ-SAT-1", R.string.k_pz_sat_1, Bereich.PROZENT, 8, 16, voraussetzungen = listOf("PZ-VON-1")),

        Kompetenz("GL-TER-1", R.string.k_gl_ter_1, Bereich.GLEICHUNGEN, 7, 15, voraussetzungen = listOf("NZ-MUL-1")),
        Kompetenz("GL-LIN-1", R.string.k_gl_lin_1, Bereich.GLEICHUNGEN, 8, 17, voraussetzungen = listOf("GL-TER-1")),
        Kompetenz("GL-KLA-1", R.string.k_gl_kla_1, Bereich.GLEICHUNGEN, 8, 18, niveaus = setOf(Niveau.M, Niveau.E), voraussetzungen = listOf("GL-LIN-1")),
    )

    private val nachKennung: Map<String, Kompetenz> = alle.associateBy { it.kennung }

    fun finde(kennung: String): Kompetenz? = nachKennung[kennung]

    fun imBereich(bereich: Bereich): List<Kompetenz> = alle.filter { it.bereich == bereich }

    /** Kompetenzen, die zu Klasse und Niveau des Profils passen. */
    fun fuer(profil: Profil): List<Kompetenz> {
        val passend = alle.filter { it.abKlasse <= profil.klasse && profil.niveau in it.niveaus }
        return passend.ifEmpty { alle.filter { it.muehe <= 4 } }
    }

    /** Reihenfolge fuer den Einstufungstest: quer durch die Bereiche, aufsteigend schwer. */
    val nachMuehe: List<Kompetenz> = alle.sortedBy { it.muehe }
}
