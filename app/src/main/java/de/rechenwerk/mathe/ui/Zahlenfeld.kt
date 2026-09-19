package de.rechenwerk.mathe.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import de.rechenwerk.mathe.R
import de.rechenwerk.mathe.daten.Bruch
import de.rechenwerk.mathe.daten.Form

/** Das Feld, in dem die eingetippte Antwort steht, samt Hinweis auf die Form. */
@Composable
fun Eingabefeld(eingabe: String, form: Form, gesperrt: Boolean, modifier: Modifier = Modifier) {
    val rand = if (gesperrt) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.primary
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Masse.tippziel),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(Abstand.m)) {
                Text(
                    text = eingabe.ifEmpty { stringResource(R.string.training_platzhalter) },
                    style = MaterialTheme.typography.displaySmall,
                    color = if (eingabe.isEmpty()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(top = Abstand.xs)
                .width(Masse.held)
                .height(Masse.strich)
                .background(rand),
        )
        Text(
            text = stringResource(
                when (form) {
                    Form.BRUCH -> R.string.training_form_bruch
                    Form.PROZENT -> R.string.training_form_prozent
                    Form.ZAHL -> R.string.training_form_zahl
                }
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Abstand.s),
        )
    }
}

/**
 * Eigenes Zahlenfeld statt Systemtastatur: Ziffern, Komma, Bruchstrich und
 * Vorzeichen sind damit immer erreichbar, und das Raster bleibt symmetrisch.
 * Alle Tasten sind gleich breit, die Sendetaste nimmt zwei Felder ein.
 */
@Composable
fun Zahlenfeld(
    gesperrt: Boolean,
    sendbar: Boolean,
    sendenText: String,
    aufZeichen: (String) -> Unit,
    aufLoeschen: () -> Unit,
    aufSenden: () -> Unit,
    modifier: Modifier = Modifier,
    kopf: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Abstand.m, vertical = Abstand.s),
        verticalArrangement = Arrangement.spacedBy(Abstand.s),
    ) {
        kopf()
        Row(horizontalArrangement = Arrangement.spacedBy(Abstand.s)) {
            Zifferntaste("7", gesperrt, aufZeichen)
            Zifferntaste("8", gesperrt, aufZeichen)
            Zifferntaste("9", gesperrt, aufZeichen)
            Symbolfeldtaste(
                symbol = Sym.Ruecktaste,
                beschreibung = stringResource(R.string.taste_loeschen),
                gesperrt = gesperrt,
                aufDruck = aufLoeschen,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Abstand.s)) {
            Zifferntaste("4", gesperrt, aufZeichen)
            Zifferntaste("5", gesperrt, aufZeichen)
            Zifferntaste("6", gesperrt, aufZeichen)
            Zifferntaste("/", gesperrt, aufZeichen)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Abstand.s)) {
            Zifferntaste("1", gesperrt, aufZeichen)
            Zifferntaste("2", gesperrt, aufZeichen)
            Zifferntaste("3", gesperrt, aufZeichen)
            Zifferntaste(Bruch.MINUS, gesperrt, aufZeichen)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Abstand.s)) {
            Zifferntaste(",", gesperrt, aufZeichen)
            Zifferntaste("0", gesperrt, aufZeichen)
            Werktaste(
                text = sendenText,
                modifier = Modifier.weight(2f),
                aktiv = sendbar && !gesperrt,
                aufDruck = aufSenden,
            )
        }
    }
}

@Composable
private fun RowScope.Zifferntaste(zeichen: String, gesperrt: Boolean, aufDruck: (String) -> Unit) {
    val quelle = remember { MutableInteractionSource() }
    val gedrueckt by quelle.collectIsPressedAsState()
    val groesse by animateFloatAsState(
        targetValue = if (gedrueckt) 0.94f else 1f,
        animationSpec = tween(90),
        label = "zifferndruck",
    )
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(Masse.tasteHoehe)
            .graphicsLayer { scaleX = groesse; scaleY = groesse }
            .clip(MaterialTheme.shapes.small)
            .clickable(
                interactionSource = quelle,
                indication = null,
                enabled = !gesperrt,
                onClick = { aufDruck(zeichen) },
            ),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = zeichen,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun RowScope.Symbolfeldtaste(
    symbol: ImageVector,
    beschreibung: String,
    gesperrt: Boolean,
    aufDruck: () -> Unit,
) {
    val quelle = remember { MutableInteractionSource() }
    val gedrueckt by quelle.collectIsPressedAsState()
    val groesse by animateFloatAsState(
        targetValue = if (gedrueckt) 0.94f else 1f,
        animationSpec = tween(90),
        label = "symboldruck",
    )
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(Masse.tasteHoehe)
            .graphicsLayer { scaleX = groesse; scaleY = groesse }
            .clip(MaterialTheme.shapes.small)
            .clickable(
                interactionSource = quelle,
                indication = null,
                enabled = !gesperrt,
                onClick = aufDruck,
            ),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = symbol,
                contentDescription = beschreibung,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(Masse.symbol),
            )
        }
    }
}
