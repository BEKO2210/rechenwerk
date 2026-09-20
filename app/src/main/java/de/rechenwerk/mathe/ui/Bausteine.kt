package de.rechenwerk.mathe.ui

import android.animation.ValueAnimator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import de.rechenwerk.mathe.daten.Serie
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
 * Eine Flaeche der zweiten Ebene. Alle Kacheln der App sind Glas und teilen
 * sich diese Form, diesen Innenrand und dieselbe Rezeptur aus [GlasFlaeche].
 */
@Composable
fun Kachel(
    modifier: Modifier = Modifier,
    hervorgehoben: Boolean = false,
    aufDruck: (() -> Unit)? = null,
    inhalt: @Composable ColumnScope.() -> Unit,
) {
    val form = MaterialTheme.shapes.large
    val gemeinsam = modifier
        .fillMaxWidth()
        .defaultMinSize(minHeight = Masse.tippziel)
    GlasFlaeche(modifier = gemeinsam, form = form, hervorgehoben = hervorgehoben) {
        val innen = if (aufDruck == null) {
            Modifier
        } else {
            Modifier.clickable(onClick = aufDruck)
        }
        Column(
            modifier = innen
                .fillMaxWidth()
                .defaultMinSize(minHeight = Masse.tippziel)
                .padding(Abstand.l),
            content = inhalt,
        )
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
 * Eine Gruppe gleichwertiger Auswahlfelder in einem festen Raster. Alle Felder
 * einer Gruppe sind gleich breit und gleich hoch, die Abstaende kommen aus der
 * einen Abstandsskala. Eine zu lange Beschriftung bricht innerhalb ihres
 * Feldes um, statt das Feld zu verbreitern -- so bleibt keine Reihe
 * ausgefranst.
 *
 * [spalten] wird so gewaehlt, dass in der letzten Reihe nie ein einzelnes
 * uebriges Feld am Rand steht: vier Schularten in zwei Spalten, sechs Klassen
 * in drei, drei Niveaus in drei.
 */
@Composable
fun <T> Wahlraster(
    werte: List<T>,
    gewaehlt: T,
    spalten: Int,
    beschriftung: @Composable (T) -> String,
    aufWahl: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Abstand.s),
    ) {
        for (reihe in werte.chunked(spalten)) {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(Abstand.s),
            ) {
                for (wert in reihe) {
                    Wahlfeld(
                        text = beschriftung(wert),
                        aktiv = wert == gewaehlt,
                        aufWahl = { aufWahl(wert) },
                    )
                }
                // Die letzte Reihe wird mit leeren Plaetzen aufgefuellt, damit
                // ihre Felder genauso breit sind wie die daruber.
                repeat(spalten - reihe.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun RowScope.Wahlfeld(text: String, aktiv: Boolean, aufWahl: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (aktiv) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        modifier = Modifier
            .weight(1f)
            .defaultMinSize(minHeight = Masse.tippziel)
            .fillMaxHeight()
            .clip(MaterialTheme.shapes.small)
            .selectable(
                selected = aktiv,
                role = Role.RadioButton,
                onClick = aufWahl,
            ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = Abstand.s, vertical = Abstand.m),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = if (aktiv) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

/**
 * Die Serien-Strecke: wie viele Aufgaben hintereinander richtig waren, als
 * schmaler Balken. Jeder Treffer laesst sie weiterwachsen, ein Fehler laesst
 * sie sichtbar zurueckfallen. Sie traegt den warmen Belohnungston der Marke.
 */
@Composable
fun Serienstrecke(serie: Int, modifier: Modifier = Modifier) {
    val bernstein = LocalBernstein.current
    // Die Strecke laeuft bis zur vollen Runde und bleibt dann voll. Sie faengt
    // nie von vorn an -- ein Neubeginn waere von einem Fehler nicht zu
    // unterscheiden, und genau der soll hier sichtbar sein.
    val erreicht = serie.coerceAtMost(Serie.RUNDE)
    val anteil by animateFloatAsState(
        targetValue = erreicht.toFloat() / Serie.RUNDE,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "serienstrecke",
    )
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Abstand.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.serie_titel),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.serie_wert, serie.toString()),
                style = MaterialTheme.typography.labelLarge,
                color = bernstein,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Masse.strecke)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            if (anteil > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(anteil)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(bernstein),
                )
            }
        }
    }
}

/**
 * Eine Zahl, die einmal von null auf ihren Wert hochlaeuft. Ist die Bewegung
 * im System abgeschaltet, steht sie sofort da -- [animateIntAsState] folgt der
 * Animationsskala von selbst.
 */
@Composable
fun zaehlend(ziel: Int): Int {
    var gesetzt by remember { mutableStateOf(0) }
    val wert by animateIntAsState(
        targetValue = gesetzt,
        animationSpec = tween(Bewegung.ZAEHLEN_MS, easing = Bewegung.eintritt),
        label = "zaehlwerk",
    )
    LaunchedEffect(ziel) { gesetzt = ziel }
    return wert
}

/**
 * Ob das System Bewegung zulaesst. Die Compose-Animationen kuerzen sich bei
 * abgeschalteter Animationsskala von selbst; nur eine gestaffelte Wartezeit
 * muesste sonst trotzdem ablaufen.
 */
@Composable
fun bewegungAn(): Boolean = remember { ValueAnimator.areAnimatorsEnabled() }

/**
 * Listeneintraege treten gestaffelt ein: 60 Millisekunden Versatz je Platz,
 * rund 380 Millisekunden Dauer, mit leichtem Ueberschwingen.
 */
@Composable
fun Auftritt(platz: Int, inhalt: @Composable () -> Unit) {
    val bewegt = bewegungAn()
    val zustand = remember { MutableTransitionState(false) }
    LaunchedEffect(platz, bewegt) {
        if (bewegt) delay(platz * Bewegung.VERSATZ_MS)
        zustand.targetState = true
    }
    AnimatedVisibility(
        visibleState = zustand,
        enter = fadeIn(animationSpec = tween(Bewegung.AUFTRITT_MS, easing = Bewegung.eintritt)) +
            slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
            ) { hoehe -> hoehe / 4 },
    ) {
        inhalt()
    }
}
