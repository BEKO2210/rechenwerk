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
import de.rechenwerk.mathe.daten.Fehlerarten
import de.rechenwerk.mathe.daten.Katalog
import de.rechenwerk.mathe.daten.Kompetenz
import de.rechenwerk.mathe.daten.Wiederholung

/**
 * Fortschritt: was sitzt, was in Arbeit ist, wie viel Zeit das gekostet hat
 * und welche Fehlerarten dabei aufgefallen sind. Keine Punkte, keine Serie,
 * die man verlieren kann -- eine Pause kostet hier nichts.
 */
@Composable
fun FortschrittBildschirm(
    ablage: Ablage,
    aufKompetenz: (String) -> Unit,
    innenrand: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val werte = ablage.werte
    // Gezaehlt wird das eigene Pensum, nicht der ganze Katalog.
    val pensum = Katalog.fuer(ablage.profil)
    val beruehrte = pensum.filter { ablage.stand(it.kennung).erfasst }
    val sicher = pensum.count { ablage.stand(it.kennung).grad >= Wiederholung.SICHER }
    val inArbeit = beruehrte.count { ablage.stand(it.kennung).grad < Wiederholung.SICHER }
    val quote = if (werte.versuche == 0) 0.0 else werte.treffer.toDouble() / werte.versuche
    val mitFehlern = beruehrte.filter { ablage.stand(it.kennung).fehlerarten.isNotEmpty() }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = Abstand.l,
            end = Abstand.l,
            top = innenrand.calculateTopPadding() + Abstand.l,
            bottom = innenrand.calculateBottomPadding() + Abstand.xxl,
        ),
        verticalArrangement = Arrangement.spacedBy(Abstand.m),
    ) {
        item {
            Text(
                text = stringResource(R.string.fortschritt_titel),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        if (werte.versuche == 0) {
            item {
                Leerzustand(
                    titel = stringResource(R.string.leer_fortschritt_titel),
                    einladung = stringResource(R.string.leer_fortschritt_text),
                )
            }
            return@LazyColumn
        }

        item {
            Kachel(hervorgehoben = true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Kennzahl(
                        symbol = Sym.Haken,
                        wert = sicher.toString(),
                        beschriftung = stringResource(R.string.fortschritt_beherrscht),
                        modifier = Modifier.weight(1f),
                    )
                    Kennzahl(
                        symbol = Sym.Ziel,
                        wert = inArbeit.toString(),
                        beschriftung = stringResource(R.string.fortschritt_in_arbeit),
                        modifier = Modifier.weight(1f),
                    )
                    Kennzahl(
                        symbol = Sym.Saeulen,
                        wert = prozent(quote),
                        beschriftung = stringResource(R.string.fortschritt_quote),
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(Abstand.l))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Kennzahl(
                        symbol = Sym.Uhr,
                        wert = dauer(werte.lernzeitMs),
                        beschriftung = stringResource(R.string.fortschritt_lernzeit),
                        modifier = Modifier.weight(1f),
                    )
                    Kennzahl(
                        symbol = Sym.Wiederkehr,
                        wert = werte.wiederholungen.toString(),
                        beschriftung = stringResource(R.string.fortschritt_wiederholungen),
                        modifier = Modifier.weight(1f),
                    )
                    Kennzahl(
                        symbol = Sym.Rechenzeichen,
                        wert = werte.versuche.toString(),
                        beschriftung = stringResource(R.string.fortschritt_aufgaben),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item { Abschnitt(titel = stringResource(R.string.fortschritt_bestwerte)) }

        item {
            Kachel {
                Wertzeile(
                    bezeichnung = stringResource(R.string.fortschritt_beste_serie),
                    wert = werte.besteSerie.toString(),
                )
                Wertzeile(
                    bezeichnung = stringResource(R.string.fortschritt_schnellste),
                    wert = if (werte.schnellsteMs > 0L) {
                        dauer(werte.schnellsteMs)
                    } else {
                        stringResource(R.string.wann_nie)
                    },
                )
                Wertzeile(
                    bezeichnung = stringResource(R.string.fortschritt_sitzungen),
                    wert = werte.sitzungen.toString(),
                )
            }
        }

        item { Abschnitt(titel = stringResource(R.string.fortschritt_kompetenzen)) }

        items(beruehrte, key = { it.kennung }) { kompetenz ->
            Auftritt(platz = beruehrte.indexOf(kompetenz)) {
                KompetenzZeile(ablage = ablage, kompetenz = kompetenz, aufDruck = { aufKompetenz(kompetenz.kennung) })
            }
        }

        item { Abschnitt(titel = stringResource(R.string.fortschritt_fehlerarten)) }

        if (mitFehlern.isEmpty()) {
            item {
                Leerzustand(
                    titel = stringResource(R.string.leer_fehler_titel),
                    einladung = stringResource(R.string.leer_fehler_text),
                )
            }
        } else {
            items(mitFehlern, key = { "fehler_" + it.kennung }) { kompetenz ->
                Kachel {
                    Text(
                        text = stringResource(kompetenz.name),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(Abstand.s))
                    for ((kennung, anzahl) in ablage.stand(kompetenz.kennung).fehlerarten
                        .entries.sortedByDescending { it.value }) {
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
    }
}

@Composable
private fun KompetenzZeile(ablage: Ablage, kompetenz: Kompetenz, aufDruck: () -> Unit) {
    val stand = ablage.stand(kompetenz.kennung)
    Kachel(aufDruck = aufDruck) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(kompetenz.name),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = kompetenz.kennung,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = prozent(stand.grad),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Icon(
                imageVector = Sym.PfeilRechts,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Abstand.s).size(Masse.symbolKlein),
            )
        }
        Box(modifier = Modifier.padding(top = Abstand.m)) {
            Gradbalken(anteil = stand.grad)
        }
    }
}
