package de.rechenwerk.mathe.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import kotlin.math.cos
import kotlin.math.sin

/**
 * Das Rechenwerk: ein Ring aus sechs Segmenten, eines je Kompetenzbereich.
 * Jedes Segment fuellt sich mit dem Beherrschungsgrad seines Bereichs, in der
 * Mitte steht die Basis insgesamt. Ein Lichtpunkt wandert langsam ueber den
 * Ring, das Licht dahinter atmet -- das einzige dauerhaft bewegte Element
 * des Startbildschirms.
 */
@Composable
fun Held(gesamt: Double, segmente: List<Double>, modifier: Modifier = Modifier) {
    val akzent = MaterialTheme.colorScheme.primary
    val spur = MaterialTheme.colorScheme.surfaceContainerHigh
    val raster = MaterialTheme.colorScheme.outlineVariant

    val uebergang = rememberInfiniteTransition(label = "werk")
    val licht by uebergang.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing)),
        label = "licht",
    )
    val atem by uebergang.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(5200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "atem",
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

            // Zarter Verlauf hinter dem Ring, langsam atmend.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(akzent.copy(alpha = 0.26f), Color.Transparent),
                    center = mitte,
                    radius = radius * (0.92f + 0.22f * atem),
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

            // Der wandernde Lichtpunkt auf der Ringbahn.
            val bogen = Math.toRadians((licht - 90f).toDouble())
            val punkt = Offset(
                mitte.x + radius * cos(bogen).toFloat(),
                mitte.y + radius * sin(bogen).toFloat(),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.85f), akzent.copy(alpha = 0f)),
                    center = punkt,
                    radius = strich * 1.7f,
                ),
                radius = strich * 1.7f,
                center = punkt,
            )
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
