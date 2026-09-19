package de.rechenwerk.mathe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.rechenwerk.mathe.R
import de.rechenwerk.mathe.daten.Ablage
import de.rechenwerk.mathe.daten.Bereich
import de.rechenwerk.mathe.daten.Fehlerarten
import de.rechenwerk.mathe.daten.Katalog
import de.rechenwerk.mathe.daten.Kompetenz

/** Kopfzeile mit Rueckweg -- gleiche Hoehe und gleicher Rand auf allen Unterseiten. */
@Composable
private fun Unterkopf(titel: String, aufZurueck: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Abstand.s, vertical = Abstand.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Symboltaste(
            symbol = Sym.PfeilLinks,
            beschreibung = stringResource(R.string.zurueck),
            aufDruck = aufZurueck,
        )
        Text(
            text = titel,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Abstand.s),
        )
    }
}

/** Uebersicht eines Kompetenzbereichs: alle Kompetenzen mit ihrem Stand. */
@Composable
fun BereichBildschirm(
    ablage: Ablage,
    bereich: Bereich,
    aufKompetenz: (String) -> Unit,
    aufZurueck: () -> Unit,
    innenrand: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val kompetenzen = Katalog.imBereich(bereich)
    Column(
        modifier = modifier.padding(
            top = innenrand.calculateTopPadding(),
            bottom = innenrand.calculateBottomPadding(),
        ),
    ) {
        Unterkopf(titel = name(bereich), aufZurueck = aufZurueck)
        LazyColumn(
            contentPadding = PaddingValues(
                start = Abstand.l,
                end = Abstand.l,
                top = Abstand.s,
                bottom = Abstand.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(Abstand.m),
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Gradbalken(anteil = bereichsGrad(ablage, bereich))
                }
            }
            items(kompetenzen, key = { it.kennung }) { kompetenz ->
                Auftritt(platz = kompetenzen.indexOf(kompetenz)) {
                    Kachel(aufDruck = { aufKompetenz(kompetenz.kennung) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(kompetenz.name),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = prozent(ablage.stand(kompetenz.kennung).grad),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Box(modifier = Modifier.padding(top = Abstand.m)) {
                            Gradbalken(anteil = ablage.stand(kompetenz.kennung).grad)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Kompetenzdetail: alle Zahlen zu einer einzelnen Kompetenz und der direkte
 * Einstieg in ein gezieltes Training.
 */
@Composable
fun KompetenzBildschirm(
    ablage: Ablage,
    kompetenz: Kompetenz,
    aufTraining: (String) -> Unit,
    aufKompetenz: (String) -> Unit,
    aufZurueck: () -> Unit,
    innenrand: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val stand = ablage.stand(kompetenz.kennung)
    val fehler = stand.fehlerarten.entries.sortedByDescending { it.value }

    Column(
        modifier = modifier.padding(
            top = innenrand.calculateTopPadding(),
            bottom = innenrand.calculateBottomPadding(),
        ),
    ) {
        Unterkopf(titel = name(kompetenz.bereich), aufZurueck = aufZurueck)
        LazyColumn(
            contentPadding = PaddingValues(
                start = Abstand.l,
                end = Abstand.l,
                top = Abstand.s,
                bottom = Abstand.xxl,
            ),
            verticalArrangement = Arrangement.spacedBy(Abstand.m),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Abstand.xs)) {
                    Text(
                        text = stringResource(kompetenz.name),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = kompetenz.kennung,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                Kachel(hervorgehoben = true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.detail_grad),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = prozent(stand.grad),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(Modifier.height(Abstand.m))
                    Gradbalken(anteil = stand.grad)
                    Spacer(Modifier.height(Abstand.s))
                    Text(
                        text = einordnung(stand.grad, stand.erfasst),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                Werktaste(
                    text = stringResource(R.string.detail_training),
                    symbol = Sym.Rechenzeichen,
                    modifier = Modifier.fillMaxWidth(),
                    aufDruck = { aufTraining(kompetenz.kennung) },
                )
            }

            item { Abschnitt(titel = stringResource(R.string.detail_zahlen)) }

            item {
                Kachel {
                    Wertzeile(
                        bezeichnung = stringResource(R.string.detail_versuche),
                        wert = stand.versuche.toString(),
                    )
                    Wertzeile(
                        bezeichnung = stringResource(R.string.detail_treffer),
                        wert = stand.treffer.toString(),
                    )
                    Wertzeile(
                        bezeichnung = stringResource(R.string.detail_zeit),
                        wert = if (stand.versuche > 0) {
                            dauer(stand.mittlereZeitMs)
                        } else {
                            stringResource(R.string.wann_nie)
                        },
                    )
                    Wertzeile(
                        bezeichnung = stringResource(R.string.detail_kontakt),
                        wert = vergangen(stand.letzterKontakt),
                    )
                    Wertzeile(
                        bezeichnung = stringResource(R.string.detail_wiederholung),
                        wert = if (stand.erfasst) {
                            kuenftig(stand.naechsteWdh)
                        } else {
                            stringResource(R.string.wann_faellig)
                        },
                    )
                    Wertzeile(
                        bezeichnung = stringResource(R.string.detail_wiederholungen),
                        wert = stand.wiederholungen.toString(),
                    )
                }
            }

            item { Abschnitt(titel = stringResource(R.string.detail_fehlerarten)) }

            if (fehler.isEmpty()) {
                item {
                    Leerzustand(
                        titel = stringResource(R.string.leer_detail_fehler_titel),
                        einladung = stringResource(R.string.leer_detail_fehler_text),
                    )
                }
            } else {
                item {
                    Kachel {
                        for ((kennung, anzahl) in fehler) {
                            val name = Fehlerarten.name(kennung)
                            if (name != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = Abstand.xs),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Sym.Punkt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(Masse.symbolWinzig),
                                    )
                                    Spacer(Modifier.width(Abstand.s))
                                    Text(
                                        text = stringResource(name),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        text = anzahl.toString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Abschnitt(titel = stringResource(R.string.detail_voraussetzungen)) }

            if (kompetenz.voraussetzungen.isEmpty()) {
                item {
                    Leerzustand(
                        titel = stringResource(R.string.leer_voraussetzung_titel),
                        einladung = stringResource(R.string.leer_voraussetzung_text),
                    )
                }
            } else {
                items(kompetenz.voraussetzungen, key = { "vor_$it" }) { kennung ->
                    val vorher = Katalog.finde(kennung)
                    if (vorher != null) {
                        Kachel(aufDruck = { aufKompetenz(kennung) }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(vorher.name),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = prozent(ablage.stand(kennung).grad),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
