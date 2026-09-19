package de.rechenwerk.mathe.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import de.rechenwerk.mathe.R
import de.rechenwerk.mathe.daten.Txt
import kotlinx.coroutines.delay

/** Loest einen prozedural gebauten Text gegen strings.xml auf. */
@Composable
fun Txt.auf(): String = stringResource(id, *teile.toTypedArray())

/** Prozentangabe in deutscher Schreibweise. */
@Composable
fun prozent(anteil: Double): String =
    stringResource(R.string.prozent_wert, Math.round(anteil * 100.0).toString())

/** Lernzeit in Stunden, Minuten oder Sekunden -- was gerade passt. */
@Composable
fun dauer(millisekunden: Long): String {
    val sekunden = millisekunden / 1000L
    return when {
        sekunden >= 3600L -> stringResource(
            R.string.zeit_stunden,
            (sekunden / 3600L).toString(),
            ((sekunden % 3600L) / 60L).toString(),
        )
        sekunden >= 60L -> stringResource(R.string.zeit_minuten, (sekunden / 60L).toString())
        else -> stringResource(R.string.zeit_sekunden, sekunden.toString())
    }
}

private const val TAG_MS = 24L * 60L * 60L * 1000L

/** Wie lange ein Zeitpunkt her ist -- in Worten, nicht als Datum. */
@Composable
fun vergangen(zeitpunkt: Long): String {
    if (zeitpunkt <= 0L) return stringResource(R.string.wann_nie)
    val tage = ((System.currentTimeMillis() - zeitpunkt) / TAG_MS).toInt().coerceAtLeast(0)
    return when (tage) {
        0 -> stringResource(R.string.wann_heute)
        1 -> stringResource(R.string.wann_gestern)
        else -> pluralStringResource(R.plurals.wann_vor_tagen, tage, tage)
    }
}

/** Wann etwas wieder drankommt -- ebenfalls in Worten. */
@Composable
fun kuenftig(zeitpunkt: Long): String {
    val rest = zeitpunkt - System.currentTimeMillis()
    if (zeitpunkt <= 0L || rest <= 0L) return stringResource(R.string.wann_faellig)
    val tage = (rest / TAG_MS).toInt()
    return if (tage <= 0) {
        stringResource(R.string.wann_heute)
    } else {
        pluralStringResource(R.plurals.wann_in_tagen, tage, tage)
    }
}

/**
 * Eine Flaeche der zweiten Ebene. Alle Kacheln der App teilen sich diese
 * Form, diesen Innenrand und diese Farbe.
 */
@Composable
fun Kachel(
    modifier: Modifier = Modifier,
    hervorgehoben: Boolean = false,
    aufDruck: (() -> Unit)? = null,
    inhalt: @Composable ColumnScope.() -> Unit,
) {
    val farbe = if (hervorgehoben) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val gemeinsam = modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = Masse.tippziel)
    if (aufDruck == null) {
        Surface(modifier = gemeinsam, shape = MaterialTheme.shapes.large, color = farbe) {
            Column(modifier = Modifier.padding(Abstand.l), content = inhalt)
        }
    } else {
        Surface(
            onClick = aufDruck,
            modifier = gemeinsam,
            shape = MaterialTheme.shapes.large,
            color = farbe,
        ) {
            Column(modifier = Modifier.padding(Abstand.l), content = inhalt)
        }
    }
}

/** Haupttaste: drueckt sich beim Antippen auf 94 Prozent zusammen. */
@Composable
fun Werktaste(
    text: String,
    modifier: Modifier = Modifier,
    symbol: ImageVector? = null,
    aktiv: Boolean = true,
    aufDruck: () -> Unit,
) {
    val quelle = remember { MutableInteractionSource() }
    val gedrueckt by quelle.collectIsPressedAsState()
    val groesse by animateFloatAsState(
        targetValue = if (gedrueckt) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tastendruck",
    )
    Button(
        onClick = aufDruck,
        enabled = aktiv,
        interactionSource = quelle,
        shape = MaterialTheme.shapes.small,
        contentPadding = ButtonDefaults.ContentPadding,
        modifier = modifier
            .defaultMinSize(minHeight = Masse.tasteHoehe)
            .graphicsLayer { scaleX = groesse; scaleY = groesse },
    ) {
        if (symbol != null) {
            Icon(imageVector = symbol, contentDescription = null, modifier = Modifier.size(Masse.symbolKlein))
            Spacer(Modifier.width(Abstand.s))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Nebentaste: gleiche Physik, ruhigere Flaeche. */
@Composable
fun Nebentaste(
    text: String,
    modifier: Modifier = Modifier,
    symbol: ImageVector? = null,
    aktiv: Boolean = true,
    aufDruck: () -> Unit,
) {
    val quelle = remember { MutableInteractionSource() }
    val gedrueckt by quelle.collectIsPressedAsState()
    val groesse by animateFloatAsState(
        targetValue = if (gedrueckt) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "nebentastendruck",
    )
    OutlinedButton(
        onClick = aufDruck,
        enabled = aktiv,
        interactionSource = quelle,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .defaultMinSize(minHeight = Masse.tasteHoehe)
            .graphicsLayer { scaleX = groesse; scaleY = groesse },
    ) {
        if (symbol != null) {
            Icon(imageVector = symbol, contentDescription = null, modifier = Modifier.size(Masse.symbolKlein))
            Spacer(Modifier.width(Abstand.s))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Reine Symboltaste -- traegt ihren Namen in der Beschreibung. */
@Composable
fun Symboltaste(
    symbol: ImageVector,
    beschreibung: String,
    modifier: Modifier = Modifier,
    aufDruck: () -> Unit,
) {
    IconButton(
        onClick = aufDruck,
        modifier = modifier.size(Masse.tippziel),
    ) {
        Icon(
            imageVector = symbol,
            contentDescription = beschreibung,
            modifier = Modifier.size(Masse.symbol),
        )
    }
}

/**
 * Leerzustand: Symbol, kurzer Titel, eine Einladung. Steht an jeder Stelle,
 * an der sonst eine leere Liste oder ein eingefrorenes Diagramm waere.
 */
@Composable
fun Leerzustand(
    titel: String,
    einladung: String,
    modifier: Modifier = Modifier,
    tasteText: String? = null,
    aufDruck: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Abstand.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Abstand.m),
    ) {
        Icon(
            imageVector = Sym.Leer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(Masse.symbolGross),
        )
        Text(
            text = titel,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = einladung,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (tasteText != null && aufDruck != null) {
            Nebentaste(text = tasteText, aufDruck = aufDruck)
        }
    }
}

/** Ein Balken fuer den Beherrschungsgrad. Bewegt sich mit Federphysik. */
@Composable
fun Gradbalken(anteil: Double, modifier: Modifier = Modifier, farbe: Color? = null) {
    val ziel = anteil.toFloat().coerceIn(0f, 1f)
    val gezeigt by animateFloatAsState(
        targetValue = ziel,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "grad",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Masse.balken)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        if (gezeigt > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(gezeigt)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(farbe ?: MaterialTheme.colorScheme.primary),
            )
        }
    }
}

/** Eine Kennzahl mit Symbol, grosser Zahl und Beschriftung. */
@Composable
fun Kennzahl(
    symbol: ImageVector,
    wert: String,
    beschriftung: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Abstand.xs),
    ) {
        Icon(
            imageVector = symbol,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Masse.symbolKlein),
        )
        Text(
            text = wert,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = beschriftung,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Ueberschrift eines Abschnitts -- immer gleich gesetzt. */
@Composable
fun Abschnitt(titel: String, modifier: Modifier = Modifier) {
    Text(
        text = titel,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = Abstand.s),
    )
}

/** Eine Zeile aus Bezeichnung und Wert -- fuer Aufschluesselungen. */
@Composable
fun Wertzeile(bezeichnung: String, wert: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = Abstand.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = bezeichnung,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = wert,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Eine Reihe gleichwertiger Auswahlfelder. Das gewaehlte Feld traegt den
 * Akzent als Flaeche, alle Felder sind mindestens ein Tippziel hoch.
 */
@Composable
fun <T> Wahlreihe(
    werte: List<T>,
    gewaehlt: T,
    beschriftung: @Composable (T) -> String,
    aufWahl: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Abstand.s),
        verticalArrangement = Arrangement.spacedBy(Abstand.s),
    ) {
        for (wert in werte) {
            val aktiv = wert == gewaehlt
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (aktiv) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .selectable(
                        selected = aktiv,
                        role = Role.RadioButton,
                        onClick = { aufWahl(wert) },
                    )
                    .defaultMinSize(minWidth = Masse.tippziel, minHeight = Masse.tippziel),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = Abstand.l)) {
                    Text(
                        text = beschriftung(wert),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (aktiv) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

/** Listeneintraege treten gestaffelt ein: 40 Millisekunden Versatz je Platz. */
@Composable
fun Auftritt(platz: Int, inhalt: @Composable () -> Unit) {
    val zustand = remember { MutableTransitionState(false) }
    LaunchedEffect(platz) {
        delay(platz * 40L)
        zustand.targetState = true
    }
    AnimatedVisibility(
        visibleState = zustand,
        enter = fadeIn(animationSpec = tween(220)) +
            slideInVertically(animationSpec = tween(220)) { hoehe -> hoehe / 4 },
    ) {
        inhalt()
    }
}
