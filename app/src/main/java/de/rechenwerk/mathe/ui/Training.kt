package de.rechenwerk.mathe.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.rechenwerk.mathe.R
import de.rechenwerk.mathe.daten.Aufgabe
import de.rechenwerk.mathe.daten.Bruch
import de.rechenwerk.mathe.daten.Katalog
import de.rechenwerk.mathe.daten.Rueckmeldung
import de.rechenwerk.mathe.daten.Werk
import kotlinx.coroutines.delay

/**
 * Trainingsbildschirm. Aufgabe gross und mittig, darunter das Zahlenfeld.
 * Richtig heisst: kurz bestaetigen und weiter. Falsch heisst nie nur falsch,
 * sondern die Loesung plus den vollstaendigen Weg auf Wunsch.
 */
@Composable
fun TrainingBildschirm(
    werk: Werk,
    aufEnde: () -> Unit,
    innenrand: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val haptik = LocalHapticFeedback.current
    val aufgabe = werk.aufgabe
    val rueckmeldung = werk.rueckmeldung

    // Die Belohnung: ein kurzes Aufleuchten der Akzentfarbe nach jedem Treffer.
    val leuchten = remember { Animatable(0f) }
    // Der Fehler: kein Rot-Schock, sondern ein ruhiges seitliches Wanken.
    val wanken = remember { Animatable(0f) }

    // Nach einer richtigen Antwort geht es von selbst weiter.
    LaunchedEffect(rueckmeldung) {
        if (rueckmeldung == null) return@LaunchedEffect
        if (rueckmeldung.richtig) {
            haptik.performHapticFeedback(HapticFeedbackType.LongPress)
            leuchten.snapTo(1f)
            leuchten.animateTo(
                targetValue = 0f,
                animationSpec = tween(Bewegung.AUFLEUCHTEN_MS, easing = Bewegung.eintritt),
            )
            delay(750L)
            if (werk.zeitUm()) werk.beendeSitzung() else werk.naechsteAufgabe()
        } else {
            // Zweimal zur Seite, je 120 Millisekunden, dann zurueck in die Mitte.
            val halb = Bewegung.WANKEN_MS / 2
            repeat(2) {
                wanken.animateTo(1f, tween(halb))
                wanken.animateTo(-1f, tween(halb))
            }
            wanken.animateTo(0f, tween(halb))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = innenrand.calculateTopPadding(),
                bottom = innenrand.calculateBottomPadding(),
            ),
    ) {
        Kopfzeile(
            titel = if (aufgabe != null) {
                stringResource(Katalog.finde(aufgabe.kompetenz)?.name ?: R.string.training_titel)
            } else {
                stringResource(R.string.training_titel)
            },
            zaehler = if (aufgabe != null) {
                // Solange die Rueckmeldung steht, ist die gezaehlte Aufgabe noch
                // die auf dem Schirm -- erst die naechste hebt die Nummer.
                val nummer = if (rueckmeldung == null) {
                    werk.sitzungGestellt + 1
                } else {
                    werk.sitzungGestellt
                }
                stringResource(
                    R.string.training_zaehler,
                    nummer.toString(),
                    werk.sitzungRichtig.toString(),
                )
            } else {
                null
            },
            aufSchliessen = {
                haptik.performHapticFeedback(HapticFeedbackType.LongPress)
                werk.beendeSitzung()
            },
        )

        when {
            aufgabe == null -> Leerzustand(
                titel = stringResource(R.string.leer_training_titel),
                einladung = stringResource(R.string.leer_training_text),
                tasteText = stringResource(R.string.leer_training_taste),
                aufDruck = aufEnde,
            )

            else -> {
                // Ueber dem Aufgabenfeld: links der Segmentring, der je
                // richtiger Antwort um ein Segment waechst, rechts die
                // Serien-Strecke -- wie viele Aufgaben hintereinander sassen.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Abstand.l, vertical = Abstand.s),
                    horizontalArrangement = Arrangement.spacedBy(Abstand.l),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Werkring(segmente = werk.sitzungRichtig, leuchten = leuchten.value)
                    Serienstrecke(serie = werk.serie, modifier = Modifier.weight(1f))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Abstand.l),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = aufgabe.frage.auf(),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = Abstand.l),
                    )

                    Eingabefeld(
                        eingabe = werk.eingabe,
                        form = aufgabe.form,
                        gesperrt = rueckmeldung != null,
                        modifier = Modifier.graphicsLayer {
                            translationX = wanken.value * Masse.wanken.toPx()
                        },
                    )

                    AnimatedVisibility(
                        visible = werk.hilfestufe > 0,
                        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 4 },
                        exit = fadeOut(tween(150)),
                    ) {
                        Hilfekarte(
                            aufgabe = aufgabe,
                            stufe = werk.hilfestufe,
                            modifier = Modifier.padding(top = Abstand.l),
                        )
                    }

                    AnimatedVisibility(
                        visible = rueckmeldung != null,
                        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 4 },
                        exit = fadeOut(tween(150)),
                    ) {
                        if (rueckmeldung != null) {
                            Rueckmeldekarte(
                                aufgabe = aufgabe,
                                rueckmeldung = rueckmeldung,
                                modifier = Modifier.padding(top = Abstand.l),
                            )
                        }
                    }

                    Spacer(Modifier.height(Abstand.l))
                }

                if (rueckmeldung != null && !rueckmeldung.richtig) {
                    Werktaste(
                        text = stringResource(R.string.training_weiter),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Abstand.l),
                        aufDruck = {
                            if (werk.zeitUm()) werk.beendeSitzung() else werk.naechsteAufgabe()
                        },
                    )
                } else {
                    Zahlenfeld(
                        gesperrt = rueckmeldung != null,
                        sendbar = werk.eingabe.isNotEmpty() && Bruch.lies(werk.eingabe) != null,
                        sendenText = stringResource(R.string.training_senden),
                        aufZeichen = werk::tippe,
                        aufLoeschen = {
                            haptik.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            werk.loesche()
                        },
                        aufSenden = {
                            haptik.performHapticFeedback(HapticFeedbackType.LongPress)
                            werk.sende()
                        },
                        kopf = {
                            Nebentaste(
                                text = if (werk.hilfestufe == 0) {
                                    stringResource(R.string.training_hilfe)
                                } else {
                                    stringResource(R.string.training_hilfe_mehr)
                                },
                                symbol = Sym.Impuls,
                                modifier = Modifier.fillMaxWidth(),
                                aktiv = werk.hilfestufe < 4 && rueckmeldung == null,
                                aufDruck = werk::mehrHilfe,
                            )
                        },
                    )
                }
            }
        }
    }
}

/** Die obere Leiste ist Glas -- wie Karten und die untere Navigationsleiste. */
@Composable
private fun Kopfzeile(titel: String, zaehler: String?, aufSchliessen: () -> Unit) {
    GlasFlaeche(
        modifier = Modifier.fillMaxWidth().padding(Abstand.s),
        form = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Abstand.s, vertical = Abstand.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Symboltaste(
                symbol = Sym.Kreuz,
                beschreibung = stringResource(R.string.training_beenden),
                aufDruck = aufSchliessen,
            )
            Column(modifier = Modifier.weight(1f).padding(horizontal = Abstand.s)) {
                Text(
                    text = titel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (zaehler != null) {
                    Text(
                        text = zaehler,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Gestufte Hilfe: Impuls, Strategie, erster Rechenschritt, vollstaendiger Weg. */
@Composable
private fun Hilfekarte(aufgabe: Aufgabe, stufe: Int, modifier: Modifier = Modifier) {
    Kachel(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Sym.Impuls,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Masse.symbolKlein),
            )
            Spacer(Modifier.width(Abstand.s))
            Text(
                text = stringResource(R.string.training_hilfe_stufe, stufe.toString()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            modifier = Modifier.padding(top = Abstand.s).animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(Abstand.s),
        ) {
            for (i in 0 until minOf(stufe, aufgabe.hilfen.size)) {
                Text(
                    text = aufgabe.hilfen[i].auf(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (stufe >= 4) {
                Rechenweg(aufgabe = aufgabe)
            }
        }
    }
}

@Composable
private fun Rechenweg(aufgabe: Aufgabe, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Abstand.s)) {
        for (zeile in aufgabe.weg) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Sym.Punkt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Masse.symbolWinzig),
                )
                Spacer(Modifier.width(Abstand.s))
                Text(
                    text = zeile.auf(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun Rueckmeldekarte(
    aufgabe: Aufgabe,
    rueckmeldung: Rueckmeldung,
    modifier: Modifier = Modifier,
) {
    var offen by remember(rueckmeldung) { mutableStateOf(false) }
    val drehung by animateFloatAsState(
        targetValue = if (offen) 180f else 0f,
        animationSpec = tween(200),
        label = "warum",
    )
    Kachel(modifier = modifier, hervorgehoben = true) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (rueckmeldung.richtig) Sym.Haken else Sym.Ziel,
                contentDescription = null,
                tint = if (rueckmeldung.richtig) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.size(Masse.symbol),
            )
            Spacer(Modifier.width(Abstand.s))
            Text(
                text = if (rueckmeldung.richtig) {
                    stringResource(R.string.training_richtig)
                } else {
                    stringResource(R.string.training_loesung, aufgabe.loesungText())
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        if (!rueckmeldung.richtig) {
            if (rueckmeldung.fehlbild != null) {
                Text(
                    text = stringResource(
                        R.string.training_fehlerart,
                        stringResource(rueckmeldung.fehlbild.name),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Abstand.s),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Abstand.s)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .clickable { offen = !offen }
                    .heightIn(min = Masse.tippziel),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.training_warum),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Sym.PfeilUnten,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(Masse.symbol)
                        .graphicsLayer { rotationZ = drehung },
                )
            }
            AnimatedVisibility(visible = offen) {
                Rechenweg(aufgabe = aufgabe, modifier = Modifier.padding(top = Abstand.s))
            }
        }
    }
}
