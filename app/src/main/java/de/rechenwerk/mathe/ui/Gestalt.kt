package de.rechenwerk.mathe.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.rechenwerk.mathe.daten.Modus

/**
 * Die einzige Abstandsskala der App. Jeder Rand und jeder Zwischenraum kommt
 * aus dieser Liste -- es gibt keine dp-Werte in den Bildschirmen.
 */
object Abstand {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}

/** Feste Groessen von Bauteilen. Getrennt von der Abstandsskala, aber ebenso zentral. */
object Masse {
    /** Kleinstes antippbares Ziel -- gilt fuer jede Taste und jede Kachel. */
    val tippziel = 48.dp
    val symbolWinzig = 8.dp
    val symbolKlein = 18.dp
    val symbol = 24.dp
    val symbolGross = 40.dp
    val held = 264.dp
    val heldStrich = 14.dp
    val balken = 10.dp
    val strich = 1.dp
    val tasteHoehe = 56.dp
}

/** Die einzige Formskala. Kacheln bekommen [Formen.large], Tasten [Formen.small]. */
val Formen = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// Marke: ein einziger kuehl-gruener Akzent auf Graphit.
private val AkzentHell = Color(0xFF2F7D6E)
private val AkzentDunkel = Color(0xFF4FC3A1)

private val DunkleFarben = darkColorScheme(
    primary = AkzentDunkel,
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF1F5A4E),
    onPrimaryContainer = Color(0xFFB8EEDD),
    secondary = Color(0xFF9CCCBF),
    onSecondary = Color(0xFF07352C),
    secondaryContainer = Color(0xFF1E4A41),
    onSecondaryContainer = Color(0xFFCDEDE3),
    tertiary = Color(0xFF7FB3A6),
    onTertiary = Color(0xFF00201A),
    tertiaryContainer = Color(0xFF17403A),
    onTertiaryContainer = Color(0xFFBEE5DA),
    background = Color(0xFF0D1110),
    onBackground = Color(0xFFE3E6E4),
    surface = Color(0xFF0D1110),
    onSurface = Color(0xFFE3E6E4),
    surfaceVariant = Color(0xFF232A28),
    onSurfaceVariant = Color(0xFFAFBAB6),
    surfaceContainerLowest = Color(0xFF080B0A),
    surfaceContainerLow = Color(0xFF141918),
    surfaceContainer = Color(0xFF181E1D),
    surfaceContainerHigh = Color(0xFF222927),
    surfaceContainerHighest = Color(0xFF2C3432),
    outline = Color(0xFF6A7570),
    outlineVariant = Color(0xFF39413F),
    error = Color(0xFFE79188),
    onError = Color(0xFF3C0906),
    errorContainer = Color(0xFF5A211B),
    onErrorContainer = Color(0xFFFFDAD5),
)

private val HelleFarben = lightColorScheme(
    primary = AkzentHell,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB9E9DA),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF3F685E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC6E8DE),
    onSecondaryContainer = Color(0xFF12291F),
    tertiary = Color(0xFF3C6A5E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD5EFE7),
    onTertiaryContainer = Color(0xFF0C241E),
    background = Color(0xFFF6F8F7),
    onBackground = Color(0xFF181C1B),
    surface = Color(0xFFF6F8F7),
    onSurface = Color(0xFF181C1B),
    surfaceVariant = Color(0xFFDCE5E1),
    // Dunkel genug, damit Beschriftungen auch vor der abgedunkelten Ringmitte
    // des Startbildschirms noch 4,5:1 erreichen.
    onSurfaceVariant = Color(0xFF262C2A),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F3F1),
    surfaceContainer = Color(0xFFEAEEEC),
    surfaceContainerHigh = Color(0xFFE2E8E5),
    surfaceContainerHighest = Color(0xFFDCE3E0),
    outline = Color(0xFF717A76),
    outlineVariant = Color(0xFFC0C9C5),
    error = Color(0xFF9C3F35),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD5),
    onErrorContainer = Color(0xFF40110C),
)

/** Titel- und Textschrift der Marke, mit dem Systemgrotesk als Rueckfall. */
private val Marke = FontFamily(
    Font(DeviceFontFamilyName("IBM Plex Sans"), FontWeight.Normal),
    Font(DeviceFontFamilyName("IBM Plex Sans"), FontWeight.Medium),
    Font(DeviceFontFamilyName("IBM Plex Sans"), FontWeight.SemiBold),
)

/** Ziffern in gleicher Breite -- Zahlen springen beim Zaehlen nicht. */
private const val TABELLARISCH = "tnum"

private fun rolle(
    groesse: Int,
    zeile: Int,
    gewicht: FontWeight,
    abstand: Double = 0.0,
    zahlen: Boolean = false,
) = TextStyle(
    fontFamily = Marke,
    fontSize = groesse.sp,
    lineHeight = zeile.sp,
    fontWeight = gewicht,
    letterSpacing = abstand.sp,
    fontFeatureSettings = if (zahlen) TABELLARISCH else null,
)

/**
 * Die benannten Typografie-Rollen. display traegt Zahlen und Aufgaben,
 * title Ueberschriften, body Fliesstext, label Beschriftungen.
 */
val Typografie = Typography(
    displayLarge = rolle(56, 60, FontWeight.SemiBold, -1.0, zahlen = true),
    displayMedium = rolle(44, 50, FontWeight.SemiBold, -0.5, zahlen = true),
    displaySmall = rolle(34, 42, FontWeight.SemiBold, -0.25, zahlen = true),
    headlineLarge = rolle(30, 38, FontWeight.SemiBold),
    headlineMedium = rolle(25, 32, FontWeight.SemiBold),
    headlineSmall = rolle(22, 28, FontWeight.Medium),
    titleLarge = rolle(20, 26, FontWeight.SemiBold),
    titleMedium = rolle(17, 23, FontWeight.Medium, 0.1),
    titleSmall = rolle(15, 20, FontWeight.Medium, 0.1),
    bodyLarge = rolle(17, 25, FontWeight.Normal, 0.15),
    bodyMedium = rolle(15, 22, FontWeight.Normal, 0.2),
    bodySmall = rolle(13, 19, FontWeight.Normal, 0.2),
    labelLarge = rolle(15, 20, FontWeight.Medium, 0.1),
    labelMedium = rolle(13, 17, FontWeight.Medium, 0.4),
    labelSmall = rolle(11, 15, FontWeight.Medium, 0.5),
)

@Composable
fun RechenwerkTheme(modus: Modus, inhalt: @Composable () -> Unit) {
    val dunkel = when (modus) {
        Modus.DUNKEL -> true
        Modus.HELL -> false
        Modus.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dunkel) DunkleFarben else HelleFarben,
        typography = Typografie,
        shapes = Formen,
        content = inhalt,
    )
}
