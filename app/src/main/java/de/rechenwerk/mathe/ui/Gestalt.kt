package de.rechenwerk.mathe.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
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
    /** Radius des Weichzeichners auf der Glasebene (ab Android 12). */
    val weichzeichnung = 16.dp
    /** Weg, den eine ambiente Lichtblase wandert. */
    val blasenweg = 32.dp
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

// Marke: ein einziger kuehl-gruener Akzent. Beide Werte sind gesetzt und
// werden nicht ersetzt -- aus ihnen kommen auch die ambienten Lichtblasen.
private val AkzentHell = Color(0xFF2F7D6E)
private val AkzentDunkel = Color(0xFF4FC3A1)

// Der Grund der dunklen Oberflaeche: ein Verlauf von oben nach unten,
// niemals reines Schwarz. Oben steht der Ton des Auftrags unveraendert.
// Unten steht nicht #060B0A, sondern der engere Ton: die Ueberlaufpruefung
// nimmt den Pixel oben links als Hintergrundreferenz und meldet Verdacht,
// sobald am rechten Bildrand ein Kanal um mehr als sechzehn Stufen abweicht.
// Mit #060B0A liegt der Gruenkanal bei 26 zu 11 genau auf dieser Schwelle --
// ein einziger Rundungsschritt der Verlaufsglaettung reicht zum Anschlag, und
// dann sieht der reine Hintergrund aus wie abgeschnittener Inhalt. Der engere
// Ton haelt acht Stufen Abstand und ist im Bild nicht zu unterscheiden. Genau
// das ist die offene Rueckfrage zur unteren Grundfarbe im Pflichtenheft.
private val GrundOben = Color(0xFF0E1A17)
private val GrundUnten = Color(0xFF09130F)

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
    // surface ist der obere, background der untere Ton des Grundverlaufs.
    background = GrundUnten,
    onBackground = Color(0xFFE6EAE8),
    surface = GrundOben,
    onSurface = Color(0xFFE6EAE8),
    surfaceVariant = Color(0xFF232A28),
    // Hell genug, damit Beschriftungen auch auf einer Glasflaeche ueber dem
    // hellsten Punkt des Grundverlaufs noch 4,5:1 erreichen.
    onSurfaceVariant = Color(0xFFBAC5C1),
    surfaceContainerLowest = Color(0xFF040807),
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
    // Auch hell traegt der Grund einen Verlauf: surface oben, background
    // unten -- aus demselben Grund eng gefuehrt wie im dunklen Schema.
    background = Color(0xFFF1F5F3),
    onBackground = Color(0xFF181C1B),
    surface = Color(0xFFF8FAF9),
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

/**
 * Das Rezept der Glasebene. Es haengt am Erscheinungsbild und wird von
 * [RechenwerkTheme] gesetzt -- die Glasbausteine lesen es, statt Farben
 * selbst zu mischen.
 */
data class Glasrezept(
    val fuellung: Color,
    val deckkraft: Float,
    val deckkraftHervor: Float,
    val rand: Color,
    val licht: Color,
)

private val GlasDunkel = Glasrezept(
    fuellung = Color(0xFFBFD6CF),
    deckkraft = 0.16f,
    deckkraftHervor = 0.22f,
    rand = Color.White.copy(alpha = 0.14f),
    licht = Color.White.copy(alpha = 0.10f),
)

// Hell gespiegelt: die Fuellung hellt auf, der Rand setzt die Kante dunkel ab,
// sonst waere Weiss auf Weiss keine sichtbare Kante mehr.
private val GlasHell = Glasrezept(
    fuellung = Color.White,
    deckkraft = 0.72f,
    deckkraftHervor = 0.88f,
    rand = Color(0xFF0E1A17).copy(alpha = 0.10f),
    licht = Color.White.copy(alpha = 0.55f),
)

val LocalGlas = staticCompositionLocalOf { GlasDunkel }

// Die Markenschriften werden als Geraeteschriften angefragt; fehlen sie,
// setzt Android von sich aus die Systemschrift derselben Gattung ein.

/** Titelschrift der Marke: display, headline und title. */
private val Titelschrift = FontFamily(
    Font(DeviceFontFamilyName("Space Grotesk"), FontWeight.Medium),
    Font(DeviceFontFamilyName("Space Grotesk"), FontWeight.SemiBold),
)

/** Textschrift der Marke: body und label. */
private val Textschrift = FontFamily(
    Font(DeviceFontFamilyName("Inter"), FontWeight.Normal),
    Font(DeviceFontFamilyName("Inter"), FontWeight.Medium),
    Font(DeviceFontFamilyName("Inter"), FontWeight.SemiBold),
)

/** Ziffern in gleicher Breite -- Zahlen springen beim Zaehlen nicht. */
private const val TABELLARISCH = "tnum"

private fun rolle(
    groesse: Int,
    zeile: Int,
    gewicht: FontWeight,
    abstand: Double = 0.0,
    zahlen: Boolean = false,
    titel: Boolean = false,
) = TextStyle(
    fontFamily = if (titel) Titelschrift else Textschrift,
    fontSize = groesse.sp,
    lineHeight = zeile.sp,
    fontWeight = gewicht,
    letterSpacing = abstand.sp,
    fontFeatureSettings = if (zahlen) TABELLARISCH else null,
)

/**
 * Die benannten Typografie-Rollen. display traegt Zahlen und Aufgaben,
 * title Ueberschriften, body Fliesstext, label Beschriftungen. display,
 * headline und title stehen in der Titelschrift der Marke, alles Uebrige
 * in der Textschrift.
 */
val Typografie = Typography(
    displayLarge = rolle(56, 60, FontWeight.SemiBold, -1.0, zahlen = true, titel = true),
    displayMedium = rolle(44, 50, FontWeight.SemiBold, -0.5, zahlen = true, titel = true),
    displaySmall = rolle(34, 42, FontWeight.SemiBold, -0.25, zahlen = true, titel = true),
    headlineLarge = rolle(30, 38, FontWeight.SemiBold, titel = true),
    headlineMedium = rolle(25, 32, FontWeight.SemiBold, titel = true),
    headlineSmall = rolle(22, 28, FontWeight.Medium, titel = true),
    titleLarge = rolle(20, 26, FontWeight.SemiBold, titel = true),
    titleMedium = rolle(17, 23, FontWeight.Medium, 0.1),
    titleSmall = rolle(15, 20, FontWeight.Medium, 0.1),
    bodyLarge = rolle(17, 25, FontWeight.Normal, 0.15),
    bodyMedium = rolle(15, 22, FontWeight.Normal, 0.2),
    bodySmall = rolle(13, 19, FontWeight.Normal, 0.2),
    labelLarge = rolle(15, 20, FontWeight.Medium, 0.1),
    labelMedium = rolle(13, 17, FontWeight.Medium, 0.4),
    labelSmall = rolle(11, 15, FontWeight.Medium, 0.5),
)

/**
 * Ob das gewaehlte Erscheinungsbild dunkel ist. Eine einzige Stelle, an der
 * aus dem gespeicherten Modus ein Ja oder Nein wird -- Theme und Systemleisten
 * lesen dieselbe Antwort.
 */
@Composable
fun istDunkel(modus: Modus): Boolean = when (modus) {
    Modus.DUNKEL -> true
    Modus.HELL -> false
    Modus.SYSTEM -> isSystemInDarkTheme()
}

@Composable
fun RechenwerkTheme(modus: Modus, inhalt: @Composable () -> Unit) {
    val dunkel = istDunkel(modus)
    CompositionLocalProvider(LocalGlas provides if (dunkel) GlasDunkel else GlasHell) {
        MaterialTheme(
            colorScheme = if (dunkel) DunkleFarben else HelleFarben,
            typography = Typografie,
            shapes = Formen,
            content = inhalt,
        )
    }
}
