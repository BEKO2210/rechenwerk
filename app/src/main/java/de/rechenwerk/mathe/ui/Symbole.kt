package de.rechenwerk.mathe.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.PathData

/**
 * Alle Symbole der App als Vektor. Kein Zeichensatz, kein Emoji, kein Bild aus
 * dem Netz. Jede Figur sitzt im Raster 24 x 24 und ist auf ihren Mittelpunkt
 * (12 | 12) zentriert, Anfang und Ende spiegelgleich.
 */
object Sym {

    private const val KANTE = 24f
    private const val MITTE = 12f

    private fun strich(name: String, breite: Float = 2f, bau: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = Masse.symbol,
            defaultHeight = Masse.symbol,
            viewportWidth = KANTE,
            viewportHeight = KANTE,
        ).apply {
            addPath(
                pathData = PathData(bau),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = breite,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()

    private fun flaeche(name: String, bau: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = Masse.symbol,
            defaultHeight = Masse.symbol,
            viewportWidth = KANTE,
            viewportHeight = KANTE,
        ).apply {
            addPath(pathData = PathData(bau), fill = SolidColor(Color.Black))
        }.build()

    private fun PathBuilder.kreis(mx: Float, my: Float, r: Float) {
        moveTo(mx - r, my)
        arcTo(r, r, 0f, true, true, mx + r, my)
        arcTo(r, r, 0f, true, true, mx - r, my)
        close()
    }

    private fun PathBuilder.kasten(links: Float, oben: Float, breite: Float, hoehe: Float) {
        moveTo(links, oben)
        lineTo(links + breite, oben)
        lineTo(links + breite, oben + hoehe)
        lineTo(links, oben + hoehe)
        close()
    }

    /** Neun Quadrate -- das Raster der Werkstatt. Steht fuer den Startbildschirm. */
    val Raster: ImageVector by lazy {
        flaeche("raster") {
            val kante = 4.4f
            val luecke = 1.4f
            val gesamt = 3 * kante + 2 * luecke
            val start = MITTE - gesamt / 2f
            for (zeile in 0..2) {
                for (spalte in 0..2) {
                    kasten(
                        start + spalte * (kante + luecke),
                        start + zeile * (kante + luecke),
                        kante,
                        kante,
                    )
                }
            }
        }
    }

    /** Drei Saeulen auf gemeinsamer Grundlinie -- der Fortschritt. */
    val Saeulen: ImageVector by lazy {
        flaeche("saeulen") {
            val breite = 4f
            val luecke = 2f
            val gesamt = 3 * breite + 2 * luecke
            val start = MITTE - gesamt / 2f
            val fuss = 19f
            val hoehen = floatArrayOf(6f, 11f, 8.5f)
            for (i in 0..2) {
                kasten(start + i * (breite + luecke), fuss - hoehen[i], breite, hoehen[i])
            }
        }
    }

    /** Zwei Schieber -- die Einstellungen. */
    val Schieber: ImageVector by lazy {
        ImageVector.Builder(
            name = "schieber",
            defaultWidth = Masse.symbol,
            defaultHeight = Masse.symbol,
            viewportWidth = KANTE,
            viewportHeight = KANTE,
        ).apply {
            addPath(
                pathData = PathData {
                    moveTo(4f, 9f); lineTo(20f, 9f)
                    moveTo(4f, 15f); lineTo(20f, 15f)
                },
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
            )
            addPath(
                pathData = PathData {
                    kreis(9f, 9f, 2.6f)
                    kreis(15f, 15f, 2.6f)
                },
                fill = SolidColor(Color.Black),
            )
        }.build()
    }

    /** Das Rechenzeichen der Marke: Plus und Minus uebereinander, achsensymmetrisch. */
    val Rechenzeichen: ImageVector by lazy {
        strich("rechenzeichen", 2f) {
            moveTo(5f, 8f); lineTo(19f, 8f)
            moveTo(MITTE, 3f); lineTo(MITTE, 13f)
            moveTo(5f, 18f); lineTo(19f, 18f)
        }
    }

    val Haken: ImageVector by lazy {
        strich("haken", 2.4f) {
            moveTo(5f, 12.5f); lineTo(10f, 17.5f); lineTo(19f, 6.5f)
        }
    }

    val Kreuz: ImageVector by lazy {
        strich("kreuz", 2.2f) {
            moveTo(6f, 6f); lineTo(18f, 18f)
            moveTo(18f, 6f); lineTo(6f, 18f)
        }
    }

    val PfeilLinks: ImageVector by lazy {
        strich("pfeil_links", 2.2f) {
            moveTo(15f, 5f); lineTo(8f, MITTE); lineTo(15f, 19f)
        }
    }

    val PfeilRechts: ImageVector by lazy {
        strich("pfeil_rechts", 2.2f) {
            moveTo(9f, 5f); lineTo(16f, MITTE); lineTo(9f, 19f)
        }
    }

    val PfeilUnten: ImageVector by lazy {
        strich("pfeil_unten", 2.2f) {
            moveTo(5f, 9f); lineTo(MITTE, 16f); lineTo(19f, 9f)
        }
    }

    val PfeilOben: ImageVector by lazy {
        strich("pfeil_oben", 2.2f) {
            moveTo(5f, 15f); lineTo(MITTE, 8f); lineTo(19f, 15f)
        }
    }

    /** Ruecktaste: Kasten mit abgeschraegter Spitze und einem Kreuz darin. */
    val Ruecktaste: ImageVector by lazy {
        ImageVector.Builder(
            name = "ruecktaste",
            defaultWidth = Masse.symbol,
            defaultHeight = Masse.symbol,
            viewportWidth = KANTE,
            viewportHeight = KANTE,
        ).apply {
            addPath(
                pathData = PathData {
                    moveTo(9f, 5f); lineTo(21f, 5f); lineTo(21f, 19f); lineTo(9f, 19f)
                    lineTo(3f, MITTE); close()
                    moveTo(12.5f, 9.5f); lineTo(17.5f, 14.5f)
                    moveTo(17.5f, 9.5f); lineTo(12.5f, 14.5f)
                },
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()
    }

    /** Ausgeben: Pfeil aus der Schale heraus. */
    val Ausgeben: ImageVector by lazy {
        strich("ausgeben", 2f) {
            moveTo(MITTE, 4f); lineTo(MITTE, 14f)
            moveTo(7.5f, 8.5f); lineTo(MITTE, 4f); lineTo(16.5f, 8.5f)
            moveTo(5f, 14f); lineTo(5f, 19f); lineTo(19f, 19f); lineTo(19f, 14f)
        }
    }

    /** Einlesen: Pfeil in die Schale hinein. */
    val Einlesen: ImageVector by lazy {
        strich("einlesen", 2f) {
            moveTo(MITTE, 4f); lineTo(MITTE, 14f)
            moveTo(7.5f, 9.5f); lineTo(MITTE, 14f); lineTo(16.5f, 9.5f)
            moveTo(5f, 14f); lineTo(5f, 19f); lineTo(19f, 19f); lineTo(19f, 14f)
        }
    }

    /** Zuruecksetzen: offener Ring mit Spitze, auf den Mittelpunkt zentriert. */
    val Zuruecksetzen: ImageVector by lazy {
        ImageVector.Builder(
            name = "zuruecksetzen",
            defaultWidth = Masse.symbol,
            defaultHeight = Masse.symbol,
            viewportWidth = KANTE,
            viewportHeight = KANTE,
        ).apply {
            addPath(
                pathData = PathData {
                    moveTo(MITTE, 5f)
                    arcTo(7f, 7f, 0f, true, true, 5.8f, 8.6f)
                    moveTo(5.8f, 3.6f); lineTo(5.8f, 8.6f); lineTo(10.8f, 8.6f)
                },
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()
    }

    /** Impuls: Ring mit Kern und vier Strahlen -- die gestufte Hilfe. */
    val Impuls: ImageVector by lazy {
        ImageVector.Builder(
            name = "impuls",
            defaultWidth = Masse.symbol,
            defaultHeight = Masse.symbol,
            viewportWidth = KANTE,
            viewportHeight = KANTE,
        ).apply {
            addPath(
                pathData = PathData {
                    kreis(MITTE, MITTE, 5f)
                    moveTo(MITTE, 1.5f); lineTo(MITTE, 4f)
                    moveTo(MITTE, 20f); lineTo(MITTE, 22.5f)
                    moveTo(1.5f, MITTE); lineTo(4f, MITTE)
                    moveTo(20f, MITTE); lineTo(22.5f, MITTE)
                },
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
            )
            addPath(pathData = PathData { kreis(MITTE, MITTE, 1.8f) }, fill = SolidColor(Color.Black))
        }.build()
    }

    /** Uhr: Ring mit zwei Zeigern -- die Lernzeit. */
    val Uhr: ImageVector by lazy {
        strich("uhr", 1.8f) {
            kreis(MITTE, MITTE, 8f)
            moveTo(MITTE, 7f); lineTo(MITTE, MITTE); lineTo(16f, 14f)
        }
    }

    /** Ziel: zwei Ringe und ein Kern -- der Beherrschungsgrad. */
    val Ziel: ImageVector by lazy {
        ImageVector.Builder(
            name = "ziel",
            defaultWidth = Masse.symbol,
            defaultHeight = Masse.symbol,
            viewportWidth = KANTE,
            viewportHeight = KANTE,
        ).apply {
            addPath(
                pathData = PathData {
                    kreis(MITTE, MITTE, 8.5f)
                    kreis(MITTE, MITTE, 4.8f)
                },
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
            )
            addPath(pathData = PathData { kreis(MITTE, MITTE, 1.8f) }, fill = SolidColor(Color.Black))
        }.build()
    }

    /** Wiederholung: zwei Pfeile im Kreis, punktsymmetrisch. */
    val Wiederkehr: ImageVector by lazy {
        strich("wiederkehr", 1.9f) {
            moveTo(4.5f, 9.5f)
            arcTo(7.5f, 7.5f, 0f, false, true, 19.5f, 9.5f)
            moveTo(19.5f, 14.5f)
            arcTo(7.5f, 7.5f, 0f, false, true, 4.5f, 14.5f)
            moveTo(1.8f, 6.8f); lineTo(4.5f, 9.5f); lineTo(7.2f, 6.8f)
            moveTo(22.2f, 17.2f); lineTo(19.5f, 14.5f); lineTo(16.8f, 17.2f)
        }
    }

    /** Leerzustand: Raster mit fehlendem Kern -- hier ist noch nichts. */
    val Leer: ImageVector by lazy {
        ImageVector.Builder(
            name = "leer",
            defaultWidth = Masse.symbolGross,
            defaultHeight = Masse.symbolGross,
            viewportWidth = KANTE,
            viewportHeight = KANTE,
        ).apply {
            addPath(
                pathData = PathData {
                    kasten(3.5f, 3.5f, 17f, 17f)
                    moveTo(MITTE, 3.5f); lineTo(MITTE, 20.5f)
                    moveTo(3.5f, MITTE); lineTo(20.5f, MITTE)
                },
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.4f,
                strokeLineJoin = StrokeJoin.Round,
            )
            addPath(pathData = PathData { kreis(MITTE, MITTE, 2.6f) }, fill = SolidColor(Color.Black))
        }.build()
    }

    /** Ein kleiner Punkt fuer Aufzaehlungen -- ersetzt den Aufzaehlungsstrich. */
    val Punkt: ImageVector by lazy {
        ImageVector.Builder(
            name = "punkt",
            defaultWidth = Masse.symbolWinzig,
            defaultHeight = Masse.symbolWinzig,
            viewportWidth = KANTE,
            viewportHeight = KANTE,
        ).apply {
            addPath(pathData = PathData { kreis(MITTE, MITTE, 5f) }, fill = SolidColor(Color.Black))
        }.build()
    }
}
