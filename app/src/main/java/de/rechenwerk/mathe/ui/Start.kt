package de.rechenwerk.mathe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import de.rechenwerk.mathe.R
import de.rechenwerk.mathe.daten.Ablage
import de.rechenwerk.mathe.daten.Art
import de.rechenwerk.mathe.daten.Bereich
import de.rechenwerk.mathe.daten.Katalog

/**
 * Startbildschirm: Begruessung, das Rechenwerk als Heldenobjekt, der Weg ins
 * Fuenf-Minuten-Training und darunter die Basis je Kompetenzbereich.
 */
@Composable
fun StartBildschirm(
    ablage: Ablage,
    aufTraining: (Art) -> Unit,
    aufBereich: (Bereich) -> Unit,
    innenrand: PaddingValues,
    modifier: Modifier = Modifier,
) {
    // Nur die Bereiche, in denen das Profil etwas zu ueben hat -- der Ring
    // bekommt genau so viele Segmente, wie es Kacheln darunter gibt.
    val bereiche = Katalog.bereicheFuer(ablage.profil)
    val segmente = bereiche.map { bereichsGrad(ablage, it) }
    // Der Einstufungstest legt Staende an, ohne Versuche zu zaehlen -- sonst
    // stuende unter dem gefuellten Ring ein Leerzustand.
    val nochNichts = ablage.werte.versuche == 0 && ablage.staende.isEmpty()

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
            Column(verticalArrangement = Arrangement.spacedBy(Abstand.xs)) {
                Text(
                    text = stringResource(R.string.start_gruss),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(
                        R.string.start_profil,
                        ablage.profil.klasse.toString(),
                        name(ablage.profil.schulart),
                        kurz(ablage.profil.niveau),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Held(gesamt = gesamtGrad(ablage), segmente = segmente)
            }
        }

        item {
            Werktaste(
                text = stringResource(R.string.start_fuenf_minuten),
                symbol = Sym.Rechenzeichen,
                modifier = Modifier.fillMaxWidth(),
                aufDruck = { aufTraining(Art.FUENF_MINUTEN) },
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Abstand.m)) {
                Nebentaste(
                    text = stringResource(R.string.start_weiterlernen),
                    modifier = Modifier.weight(1f),
                    aufDruck = { aufTraining(Art.WEITERLERNEN) },
                )
                Nebentaste(
                    text = stringResource(R.string.start_schwaechen),
                    modifier = Modifier.weight(1f),
                    aufDruck = { aufTraining(Art.SCHWAECHEN) },
                )
            }
        }

        item { Abschnitt(titel = stringResource(R.string.start_basis)) }

        if (nochNichts) {
            item {
                Leerzustand(
                    titel = stringResource(R.string.leer_basis_titel),
                    einladung = stringResource(R.string.leer_basis_text),
                    tasteText = stringResource(R.string.leer_basis_taste),
                    aufDruck = { aufTraining(Art.FUENF_MINUTEN) },
                )
            }
        } else {
            itemsIndexed(bereiche, key = { _, bereich -> bereich.name }) { platz, bereich ->
                Auftritt(platz = platz) {
                    BereichsKachel(
                        ablage = ablage,
                        bereich = bereich,
                        aufDruck = { aufBereich(bereich) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BereichsKachel(ablage: Ablage, bereich: Bereich, aufDruck: () -> Unit) {
    val grad = bereichsGrad(ablage, bereich)
    val offen = offeneImBereich(ablage, bereich)
    val beruehrt = grad > 0.0
    Kachel(aufDruck = aufDruck) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name(bereich),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = prozent(grad),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    imageVector = Sym.PfeilRechts,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = Abstand.s)
                        .size(Masse.symbolKlein),
                )
            }
        }
        Box(modifier = Modifier.padding(top = Abstand.m, bottom = Abstand.s)) {
            Gradbalken(anteil = grad)
        }
        Text(
            text = if (offen > 0 && beruehrt) {
                pluralStringResource(R.plurals.einordnung_offene, offen, offen)
            } else {
                einordnung(grad, beruehrt)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
