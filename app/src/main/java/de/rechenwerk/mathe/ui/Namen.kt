package de.rechenwerk.mathe.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.rechenwerk.mathe.R
import de.rechenwerk.mathe.daten.Ablage
import de.rechenwerk.mathe.daten.Bereich
import de.rechenwerk.mathe.daten.Katalog
import de.rechenwerk.mathe.daten.KompetenzStand
import de.rechenwerk.mathe.daten.Modus
import de.rechenwerk.mathe.daten.Niveau
import de.rechenwerk.mathe.daten.Schulart
import de.rechenwerk.mathe.daten.Wiederholung

@Composable
fun name(schulart: Schulart): String = stringResource(
    when (schulart) {
        Schulart.HAUPTSCHULE -> R.string.schulart_hauptschule
        Schulart.REALSCHULE -> R.string.schulart_realschule
        Schulart.GEMEINSCHAFTSSCHULE -> R.string.schulart_gemeinschaftsschule
        Schulart.GYMNASIUM -> R.string.schulart_gymnasium
    }
)

@Composable
fun name(niveau: Niveau): String = stringResource(
    when (niveau) {
        Niveau.G -> R.string.niveau_g
        Niveau.M -> R.string.niveau_m
        Niveau.E -> R.string.niveau_e
    }
)

@Composable
fun kurz(niveau: Niveau): String = niveau.name

@Composable
fun name(modus: Modus): String = stringResource(
    when (modus) {
        Modus.SYSTEM -> R.string.modus_system
        Modus.DUNKEL -> R.string.modus_dunkel
        Modus.HELL -> R.string.modus_hell
    }
)

@Composable
fun name(bereich: Bereich): String = stringResource(bereich.titel)

/** Der Auftrag deckt Baden-Wuerttemberg ab; weitere Laender kaemen hier dazu. */
@Composable
fun bundeslandName(): String = stringResource(R.string.bundesland_bw)

/** Kurze Einordnung eines Beherrschungsgrads in Worten. */
@Composable
fun einordnung(grad: Double, beruehrt: Boolean): String = when {
    !beruehrt -> stringResource(R.string.einordnung_offen)
    grad >= Wiederholung.SICHER -> stringResource(R.string.einordnung_sicher)
    grad >= 0.6 -> stringResource(R.string.einordnung_traegt)
    grad >= Wiederholung.IN_ARBEIT -> stringResource(R.string.einordnung_arbeit)
    else -> stringResource(R.string.einordnung_training)
}

/**
 * Ob zu einer Kompetenz ueberhaupt etwas bekannt ist. Der Einstufungstest setzt
 * einen Grad, ohne Versuche zu zaehlen -- beides gilt hier als erfasst.
 */
val KompetenzStand.erfasst: Boolean get() = beruehrt || grad > 0.0

/** Mittlerer Beherrschungsgrad eines Bereichs ueber alle seine Kompetenzen. */
fun bereichsGrad(ablage: Ablage, bereich: Bereich): Double {
    val kompetenzen = Katalog.imBereich(bereich)
    if (kompetenzen.isEmpty()) return 0.0
    return kompetenzen.sumOf { ablage.stand(it.kennung).grad } / kompetenzen.size
}

/** Wie viele Kompetenzen eines Bereichs noch nicht sicher sitzen. */
fun offeneImBereich(ablage: Ablage, bereich: Bereich): Int =
    Katalog.imBereich(bereich).count { ablage.stand(it.kennung).grad < Wiederholung.SICHER }

/** Beherrschungsgrad ueber die ganze App -- die Zahl in der Mitte des Werks. */
fun gesamtGrad(ablage: Ablage): Double {
    if (Katalog.alle.isEmpty()) return 0.0
    return Katalog.alle.sumOf { ablage.stand(it.kennung).grad } / Katalog.alle.size
}
