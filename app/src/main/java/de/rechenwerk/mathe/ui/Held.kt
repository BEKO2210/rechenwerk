package de.rechenwerk.mathe.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.rechenwerk.mathe.R
import de.rechenwerk.mathe.daten.Serie

/** Wie weit das laufende Band der Bahn reicht. */
private const val LAEUFER_WEITE = 140f

/**
 * Das Rechenwerk: ein Ring aus sechs Segmenten, eines je Kompetenzbereich.
 * Jedes Segment fuellt sich mit dem Beherrschungsgrad seines Bereichs, in der
 * Mitte steht die Basis insgesamt. Auf einer breiten Bahn laeuft ein Band in
 * der Akzentfarbe um, hinter dem Ring hindurch -- das einzige dauerhaft
 * bewegte Element des Startbildschirms.
 *
 * Es laeuft gleichmaessig (eine Umdrehung in sieben Sekunden, lineare Kurve):
 * in jedem beliebigen Augenblick ist Bewegung zu sehen, nicht nur zwischen
 * zwei Wendepunkten eines Hin und Her. Und es deckt, statt zu schimmern --
 * ein weicher Verlauf aendert von einem Augenblick zum naechsten zu wenig,
 * um als Bewegung zu gelten.
 *
 * Die Bahn ist bewusst breit: in eineinhalb Sekunden rueckt das Band um gut
 * ein Fuenftel ihrer Laenge vor, und die Flaeche, die dabei ihre Farbe
 * wechselt, ist als Bewegung im Raum zu erkennen -- nicht als Lichtpunkt, der
 * ueber den Ring huscht.
 */
@Composable
fun Held(gesamt: Double, segmente: List<Double>, modifier: Modifier = Modifier) {
    val akzent = MaterialTheme.colorScheme.primary
    val spur = MaterialTheme.colorScheme.surfaceContainerHigh
    val raster = MaterialTheme.colorScheme.outlineVariant

    val uebergang = rememberInfiniteTransition(label = "werk")
    val drehung by uebergang.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing)),
        label = "drehung",
    )
    val gezeigt by animateFloatAsState(
        targetValue = gesamt.toFloat().coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "basis",
    )

    Box(
        modifier = modifier.size(Masse.held),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strich = Masse.heldStrich.toPx()
            val mitte = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension - strich) / 2f - Abstand.m.toPx()
            val ecke = Offset(mitte.x - radius, mitte.y - radius)
            val kasten = Size(radius * 2f, radius * 2f)

            // Zarter Verlauf hinter dem Ring.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(akzent.copy(alpha = 0.26f), Color.Transparent),
                    center = mitte,
                    radius = radius * 1.03f,
                ),
                radius = radius * 1.14f,
                center = mitte,
            )

            // Weicher Schatten unter dem Ring.
            drawCircle(
                color = Color.Black.copy(alpha = 0.16f),
                radius = radius + strich * 0.55f,
                center = Offset(mitte.x, mitte.y + strich * 0.4f),
            )

            // Die Bahn: ein breites Band, das den Ring aussen knapp ueberholt
            // und nach innen bis kurz vor den Zahlenkern reicht. Der Ring
            // selbst deckt sie in seiner Breite ab, sichtbar bleibt das Band
            // innen und als schmaler Saum aussen. Erst die ruhige Spur,
            // darauf das laufende Band.
            val bahnAussen = radius + strich
            val bahnInnen = radius * 0.60f
            val bahnBreite = bahnAussen - bahnInnen
            val bahnRadius = (bahnAussen + bahnInnen) / 2f
            drawCircle(
                color = spur.copy(alpha = 0.5f),
                radius = bahnRadius,
                center = mitte,
                style = Stroke(width = bahnBreite),
            )
            drawArc(
                color = akzent,
                startAngle = drehung,
                sweepAngle = LAEUFER_WEITE,
                useCenter = false,
                topLeft = Offset(mitte.x - bahnRadius, mitte.y - bahnRadius),
                size = Size(bahnRadius * 2f, bahnRadius * 2f),
                style = Stroke(width = bahnBreite),
            )

            // Das Raster der Marke, ruhig im Inneren.
            val innen = radius - strich
            drawLine(
                color = raster.copy(alpha = 0.5f),
                start = Offset(mitte.x - innen * 0.62f, mitte.y),
                end = Offset(mitte.x + innen * 0.62f, mitte.y),
                strokeWidth = Masse.strich.toPx(),
            )
            drawLine(
                color = raster.copy(alpha = 0.5f),
                start = Offset(mitte.x, mitte.y - innen * 0.62f),
                end = Offset(mitte.x, mitte.y + innen * 0.62f),
                strokeWidth = Masse.strich.toPx(),
            )

            // Sechs Segmente: Spur und Fuellung, spiegelgleich um die Mitte.
            val felder = segmente.size.coerceAtLeast(1)
            val luecke = 6f
            val teil = 360f / felder
            for (i in 0 until felder) {
                val start = -90f + i * teil + luecke / 2f
                val weite = teil - luecke
                drawArc(
                    color = spur,
                    startAngle = start,
                    sweepAngle = weite,
                    useCenter = false,
                    topLeft = ecke,
                    size = kasten,
                    style = Stroke(width = strich, cap = StrokeCap.Round),
                )
                val anteil = segmente.getOrElse(i) { 0.0 }.toFloat().coerceIn(0f, 1f)
                if (anteil > 0f) {
                    drawArc(
                        color = akzent,
                        startAngle = start,
                        sweepAngle = weite * anteil,
                        useCenter = false,
                        topLeft = ecke,
                        size = kasten,
                        style = Stroke(width = strich, cap = StrokeCap.Round),
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Abstand.xs),
        ) {
            Text(
                text = stringResource(R.string.prozent_wert, Math.round(gezeigt * 100f).toString()),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.held_beschriftung),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Derselbe Segmentring, klein, im Training: nach jeder richtigen Antwort
 * waechst er um ein Segment. Eine volle Runde faengt wieder von vorn an, die
 * Zahl in der Mitte zaehlt weiter. [leuchten] ist das kurze Aufleuchten der
 * Akzentfarbe unmittelbar nach dem Treffer.
 */
@Composable
fun Werkring(segmente: Int, leuchten: Float, modifier: Modifier = Modifier) {
    val akzent = MaterialTheme.colorScheme.primary
    val spur = MaterialTheme.colorScheme.surfaceContainerHigh
    val erreicht = if (segmente > 0 && segmente % Serie.RUNDE == 0) Serie.RUNDE else segmente % Serie.RUNDE
    val gefuellt by animateFloatAsState(
        targetValue = erreicht.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "werkring",
    )

    Box(
        modifier = modifier.size(Masse.werkring),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strich = Masse.werkringStrich.toPx()
            val mitte = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension - strich) / 2f - Abstand.xs.toPx()
            val ecke = Offset(mitte.x - radius, mitte.y - radius)
            val kasten = Size(radius * 2f, radius * 2f)

            // Das Aufleuchten: ein weicher Schein aus der Akzentfarbe, der in
            // wenigen Hundertstelsekunden wieder vergeht.
            if (leuchten > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(akzent.copy(alpha = 0.45f * leuchten), Color.Transparent),
                        center = mitte,
                        radius = radius * 1.25f,
                    ),
                    radius = radius * 1.35f,
                    center = mitte,
                )
            }

            val luecke = 8f
            val teil = 360f / Serie.RUNDE
            for (i in 0 until Serie.RUNDE) {
                val start = -90f + i * teil + luecke / 2f
                val weite = teil - luecke
                drawArc(
                    color = spur,
                    startAngle = start,
                    sweepAngle = weite,
                    useCenter = false,
                    topLeft = ecke,
                    size = kasten,
                    style = Stroke(width = strich, cap = StrokeCap.Round),
                )
                val anteil = (gefuellt - i).coerceIn(0f, 1f)
                if (anteil > 0f) {
                    drawArc(
                        color = akzent,
                        startAngle = start,
                        sweepAngle = weite * anteil,
                        useCenter = false,
                        topLeft = ecke,
                        size = kasten,
                        style = Stroke(width = strich, cap = StrokeCap.Round),
                    )
                }
            }
        }
        Text(
            text = segmente.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
