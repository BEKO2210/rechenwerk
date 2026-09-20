package de.rechenwerk.mathe.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import de.ithandwerkstuttgart.suitekern.Export
import de.rechenwerk.mathe.R
import de.rechenwerk.mathe.daten.Modus
import de.rechenwerk.mathe.daten.Niveau
import de.rechenwerk.mathe.daten.Profil
import de.rechenwerk.mathe.daten.Schulart
import de.rechenwerk.mathe.daten.Werk

/**
 * Einstellungen: Profil aendern, Erscheinungsbild, Daten aus- und einlesen,
 * Lehrplanquelle und der Weg, alles lokal zu loeschen.
 */
@Composable
fun EinstellungenBildschirm(
    werk: Werk,
    innenrand: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val zusammenhang = LocalContext.current
    val haptik = LocalHapticFeedback.current
    val profil = werk.ablage.profil
    var meldung by remember { mutableStateOf<Int?>(null) }
    var loeschfrage by remember { mutableStateOf(false) }

    val ausgeben = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(Export.MIME)
    ) { ziel ->
        if (ziel == null) return@rememberLauncherForActivityResult
        meldung = try {
            Export.schreiben(zusammenhang, ziel, werk.alsText())
            R.string.einst_export_fertig
        } catch (fehler: Exception) {
            R.string.einst_export_fehler
        }
    }

    val einlesen = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { quelle ->
        if (quelle == null) return@rememberLauncherForActivityResult
        meldung = try {
            if (werk.uebernimm(Export.lesen(zusammenhang, quelle))) {
                R.string.einst_import_fertig
            } else {
                R.string.einst_import_fehler
            }
        } catch (fehler: Exception) {
            R.string.einst_import_fehler
        }
    }

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
                text = stringResource(R.string.einst_titel),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        item { Abschnitt(titel = stringResource(R.string.einst_profil)) }

        item {
            Kachel {
                ProfilFelder(profil = profil, aufProfil = { werk.setzeProfil(it) })
            }
        }

        item { Abschnitt(titel = stringResource(R.string.einst_erscheinung)) }

        item {
            Kachel {
                Wahlraster(
                    werte = Modus.entries.toList(),
                    gewaehlt = werk.ablage.modus,
                    spalten = 3,
                    beschriftung = { name(it) },
                    aufWahl = { werk.setzeModus(it) },
                )
            }
        }

        item { Abschnitt(titel = stringResource(R.string.einst_daten)) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(Abstand.m)) {
                Nebentaste(
                    text = stringResource(R.string.einst_export),
                    symbol = Sym.Ausgeben,
                    modifier = Modifier.weight(1f),
                    aufDruck = {
                        haptik.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        meldung = null
                        ausgeben.launch(DATEINAME)
                    },
                )
                Nebentaste(
                    text = stringResource(R.string.einst_import),
                    symbol = Sym.Einlesen,
                    modifier = Modifier.weight(1f),
                    aufDruck = {
                        haptik.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        meldung = null
                        einlesen.launch(arrayOf(Export.MIME))
                    },
                )
            }
        }

        val hinweis = meldung
        if (hinweis != null) {
            item {
                Kachel(hervorgehoben = true) {
                    Text(
                        text = stringResource(hinweis),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        item { Abschnitt(titel = stringResource(R.string.einst_quelle)) }

        item {
            Kachel {
                Wertzeile(
                    bezeichnung = stringResource(R.string.einst_quelle_name),
                    wert = stringResource(R.string.einst_quelle_wert),
                )
                Wertzeile(
                    bezeichnung = stringResource(R.string.einst_quelle_fassung),
                    wert = stringResource(R.string.einst_quelle_fassung_wert),
                )
                Spacer(Modifier.height(Abstand.s))
                Text(
                    text = stringResource(R.string.einst_lokal),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item { Abschnitt(titel = stringResource(R.string.einst_zuruecksetzen)) }

        item {
            Nebentaste(
                text = stringResource(R.string.einst_zuruecksetzen_taste),
                symbol = Sym.Zuruecksetzen,
                modifier = Modifier.fillMaxWidth(),
                aufDruck = { loeschfrage = true },
            )
        }
    }

    if (loeschfrage) {
        AlertDialog(
            onDismissRequest = { loeschfrage = false },
            title = { Text(stringResource(R.string.einst_loeschen_titel)) },
            text = { Text(stringResource(R.string.einst_loeschen_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptik.performHapticFeedback(HapticFeedbackType.LongPress)
                        werk.setzeZurueck()
                        loeschfrage = false
                        meldung = R.string.einst_loeschen_fertig
                    },
                    modifier = Modifier.defaultMinSize(minHeight = Masse.tippziel),
                ) {
                    Text(stringResource(R.string.einst_loeschen_ja))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { loeschfrage = false },
                    modifier = Modifier.defaultMinSize(minHeight = Masse.tippziel),
                ) {
                    Text(stringResource(R.string.abbrechen))
                }
            },
        )
    }
}

private const val DATEINAME = "rechenwerk.json"

/**
 * Der Spaltenaufbau wird auch vom Onboarding genutzt. Jede Gruppe liegt in
 * einem eigenen Raster mit gleich breiten Feldern; die Spaltenzahl ist so
 * gewaehlt, dass in keiner Gruppe ein einzelnes Feld allein in der letzten
 * Reihe steht: eine Schulart-Reihe traegt zwei, Klassen und Niveaus drei.
 */
@Composable
fun ProfilFelder(
    profil: Profil,
    aufProfil: (Profil) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Abstand.l)) {
        Wahlgruppe(titel = stringResource(R.string.onboarding_bundesland)) {
            Wahlraster(
                werte = listOf("BW"),
                gewaehlt = profil.bundesland,
                spalten = 1,
                beschriftung = { bundeslandName() },
                aufWahl = { aufProfil(profil.copy(bundesland = it)) },
            )
        }
        Wahlgruppe(titel = stringResource(R.string.onboarding_schulart)) {
            Wahlraster(
                werte = Schulart.entries.toList(),
                gewaehlt = profil.schulart,
                spalten = 2,
                beschriftung = { name(it) },
                aufWahl = { aufProfil(profil.copy(schulart = it)) },
            )
        }
        Wahlgruppe(titel = stringResource(R.string.onboarding_klasse)) {
            Wahlraster(
                werte = (5..10).toList(),
                gewaehlt = profil.klasse,
                spalten = 3,
                beschriftung = { it.toString() },
                aufWahl = { aufProfil(profil.copy(klasse = it)) },
            )
        }
        Wahlgruppe(titel = stringResource(R.string.onboarding_niveau)) {
            Wahlraster(
                werte = Niveau.entries.toList(),
                gewaehlt = profil.niveau,
                spalten = 3,
                beschriftung = { name(it) },
                aufWahl = { aufProfil(profil.copy(niveau = it)) },
            )
        }
    }
}

/** Ueberschrift und Raster einer Wahlgruppe -- immer im selben Abstand. */
@Composable
private fun Wahlgruppe(titel: String, inhalt: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Abstand.s)) {
        Text(
            text = titel,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        inhalt()
    }
}
