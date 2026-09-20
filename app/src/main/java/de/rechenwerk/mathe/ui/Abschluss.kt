package de.rechenwerk.mathe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import de.rechenwerk.mathe.R
import de.rechenwerk.mathe.daten.Bilanz
import de.rechenwerk.mathe.daten.Katalog

/**
 * Das Abschlussbild am Ende einer Trainingseinheit: erreichte Segmente,
 * Serie, benoetigte Zeit und in ganzen Saetzen, was als naechstes faellig ist.
 * Die Zahlen zaehlen einmal hoch; die Seite traegt den warmen Belohnungston
 * der Marke, nie als Flaeche unter Text.
 */
@Composable
fun AbschlussBildschirm(
    bilanz: Bilanz,
    aufFertig: () -> Unit,
    innenrand: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val haptik = LocalHapticFeedback.current
    LaunchedEffect(bilanz) { haptik.performHapticFeedback(HapticFeedbackType.LongPress) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = Abstand.l,
                end = Abstand.l,
                top = innenrand.calculateTopPadding() + Abstand.xxl,
                bottom = innenrand.calculateBottomPadding() + Abstand.xl,
            ),
        verticalArrangement = Arrangement.spacedBy(Abstand.l),
    ) {
        Text(
            text = stringResource(R.string.training_bilanz_titel),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.training_bilanz_kopf),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Kachel(hervorgehoben = true) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Abschlusszahl(
                    symbol = Sym.Raster,
                    wert = zaehlend(bilanz.segmente).toString(),
                    beschriftung = stringResource(R.string.abschluss_segmente),
                    modifier = Modifier.weight(1f),
                )
                Abschlusszahl(
                    symbol = Sym.Impuls,
                    wert = zaehlend(bilanz.serie).toString(),
                    beschriftung = stringResource(R.string.abschluss_serie),
                    modifier = Modifier.weight(1f),
                )
                Abschlusszahl(
                    symbol = Sym.Uhr,
                    wert = dauer(zaehlend((bilanz.dauerMs / 1000L).toInt()) * 1000L),
                    beschriftung = stringResource(R.string.training_bilanz_dauer),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(Abstand.l))
            Wertzeile(
                bezeichnung = stringResource(R.string.training_bilanz_aufgaben),
                wert = bilanz.gestellt.toString(),
            )
            Wertzeile(
                bezeichnung = stringResource(R.string.training_bilanz_richtig),
                wert = bilanz.segmente.toString(),
            )
        }

        Kachel {
            Text(
                text = naechsterSatz(bilanz),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Werktaste(
            text = stringResource(R.string.training_bilanz_fertig),
            modifier = Modifier.fillMaxWidth(),
            aufDruck = aufFertig,
        )
    }
}

/** Ein ganzer Satz darueber, was als naechstes drankommt. */
@Composable
private fun naechsterSatz(bilanz: Bilanz): String {
    val kompetenz = bilanz.naechsteKennung?.let { Katalog.finde(it) }
        ?: return stringResource(R.string.abschluss_naechstes_offen)
    return stringResource(
        R.string.abschluss_naechstes,
        stringResource(kompetenz.name),
        kuenftig(bilanz.naechsteWdh),
    )
}

/** Eine grosse Zahl des Abschlussbilds -- Symbol und Zahl im Belohnungston. */
@Composable
private fun Abschlusszahl(
    symbol: ImageVector,
    wert: String,
    beschriftung: String,
    modifier: Modifier = Modifier,
) {
    val bernstein: Color = LocalBernstein.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(Abstand.xs),
    ) {
        Icon(
            imageVector = symbol,
            contentDescription = null,
            tint = bernstein,
            modifier = Modifier.size(Masse.symbolKlein),
        )
        Text(
            text = wert,
            style = MaterialTheme.typography.headlineSmall,
            color = bernstein,
        )
        Text(
            text = beschriftung,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
