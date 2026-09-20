package de.rechenwerk.mathe.ui

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope

/** Ab dieser Fassung wirkt der Weichzeichner; darunter greift der Ersatzweg. */
private val WEICHZEICHNER_AB_API = Build.VERSION_CODES.S

/** Der Lichtverlauf liegt auf den obersten 40 Prozent der Flaeche. */
private const val LICHTKANTE = 0.4f

/**
 * Die Glasebene der App. Eine Glasflaeche ist: die Fuellfarbe mit geringer
 * Deckkraft, darueber ein zarter Lichtverlauf an der Oberkante und ein
 * haarfeiner Rand. Ab Android 12 weichzeichnet [Modifier.blur] die Fuellung;
 * darunter ist [Modifier.blur] wirkungslos, deshalb ersetzt dort eine zweite
 * halbtransparente Flaeche denselben Eindruck.
 *
 * Diese Fallunterscheidung steht genau hier und in keinem Bildschirm.
 */
@Composable
fun GlasFlaeche(
    modifier: Modifier = Modifier,
    form: Shape = MaterialTheme.shapes.large,
    hervorgehoben: Boolean = false,
    inhalt: @Composable BoxScope.() -> Unit,
) {
    val rezept = LocalGlas.current
    val deckkraft = if (hervorgehoben) rezept.deckkraftHervor else rezept.deckkraft
    val weich = Build.VERSION.SDK_INT >= WEICHZEICHNER_AB_API

    Box(modifier = modifier.clip(form)) {
        // Ebene 1: die weichgezeichnete Fuellung. Wo der Weichzeichner nichts
        // bewirkt, traegt darunter eine zweite Lage dieselbe Tiefe nach. Der
        // Weichzeichner bleibt in der Form der Flaeche -- ungebunden streute
        // er bis zu seinem Radius darueber hinaus, bei der unteren Leiste bis
        // an den Bildschirmrand.
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (weich) {
                        Modifier.blur(Masse.weichzeichnung, BlurredEdgeTreatment(form))
                    } else {
                        Modifier
                    }
                )
                .background(rezept.fuellung.copy(alpha = deckkraft))
        )
        if (!weich) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(rezept.fuellung.copy(alpha = deckkraft * 0.5f))
            )
        }
        // Ebene 2: das Licht an der Oberkante.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to rezept.licht,
                            LICHTKANTE to Color.Transparent,
                            1f to Color.Transparent,
                        )
                    )
                )
        )
        // Ebene 3: der haarfeine Rand.
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(Masse.strich, rezept.rand, form)
        )
        inhalt()
    }
}

/**
 * Der Grund der App: ein vertikaler Verlauf zweier sehr dunkler Markentoene,
 * darueber drei grosse, weich auslaufende Lichtblasen im Akzent. Sie wandern
 * langsam und gegenlaeufig, tragen keine Information und liegen hinter allem
 * anderen.
 *
 * Keine Blase reicht bis an den rechten Bildrand oder in die Ecke oben links:
 * dort bleibt der reine Verlauf stehen. Sonst faerbt die Dekoration genau die
 * Raender ein, an denen abgeschnittener Inhalt zu erkennen waere.
 */
@Composable
fun Grund(modifier: Modifier = Modifier, inhalt: @Composable BoxScope.() -> Unit) {
    val oben = MaterialTheme.colorScheme.surface
    val unten = MaterialTheme.colorScheme.background
    val blaseWarm = MaterialTheme.colorScheme.primary
    val blaseKuehl = MaterialTheme.colorScheme.tertiary

    val wanderung = rememberInfiniteTransition(label = "lichtblasen")
    val erste by wanderung.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Reverse),
        label = "blase_eins",
    )
    val zweite by wanderung.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(13000, easing = LinearEasing), RepeatMode.Reverse),
        label = "blase_zwei",
    )
    val dritte by wanderung.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(19000, easing = LinearEasing), RepeatMode.Reverse),
        label = "blase_drei",
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(brush = Brush.verticalGradient(listOf(oben, unten)))

            val weg = Masse.blasenweg.toPx()
            val radius = size.width * 0.44f
            blase(blaseWarm, 0.10f, Offset(size.width * 0.30f, size.height * 0.30f + weg * erste), radius)
            blase(blaseKuehl, 0.08f, Offset(size.width * 0.52f - weg * zweite, size.height * 0.56f), radius * 0.9f)
            blase(blaseWarm, 0.07f, Offset(size.width * 0.34f + weg * dritte, size.height * 0.88f), radius * 1.1f)
        }
        inhalt()
    }
}

/** Eine einzelne Lichtblase: radialer Verlauf aus der Akzentfarbe ins Nichts. */
private fun DrawScope.blase(
    farbe: Color,
    deckkraft: Float,
    mitte: Offset,
    radius: Float,
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(farbe.copy(alpha = deckkraft), Color.Transparent),
            center = mitte,
            radius = radius,
        ),
        radius = radius,
        center = mitte,
    )
}

