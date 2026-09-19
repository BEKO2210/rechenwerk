package de.rechenwerk.mathe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.rechenwerk.mathe.R
import de.rechenwerk.mathe.daten.Bruch
import de.rechenwerk.mathe.daten.Profil
import de.rechenwerk.mathe.daten.Werk

/** Erster Schritt: Bundesland, Schulart, Klasse und Niveau festlegen. */
@Composable
fun OnboardingProfil(
    profil: Profil,
    aufProfil: (Profil) -> Unit,
    aufWeiter: () -> Unit,
    innenrand: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = Abstand.l,
                end = Abstand.l,
                top = innenrand.calculateTopPadding() + Abstand.xxl,
                bottom = innenrand.calculateBottomPadding() + Abstand.xl,
            ),
        verticalArrangement = Arrangement.spacedBy(Abstand.l),
    ) {
        Text(
            text = stringResource(R.string.onboarding_titel),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.onboarding_text),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Kachel {
            ProfilFelder(profil = profil, aufProfil = aufProfil)
        }
        Werktaste(
            text = stringResource(R.string.onboarding_weiter),
            modifier = Modifier.fillMaxWidth(),
            aufDruck = aufWeiter,
        )
    }
}

/**
 * Zweiter Schritt: ein kurzer Einstufungstest quer durch die Kompetenzbereiche.
 * Zehn Aufgaben, jederzeit ueberspringbar, ohne Rueckmeldung zwischendurch --
 * er setzt nur die Startwerte der Beherrschungsgrade.
 */
@Composable
fun OnboardingTest(
    werk: Werk,
    aufFertig: () -> Unit,
    innenrand: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val aufgabe = werk.pruefAufgabe

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = innenrand.calculateTopPadding(),
                bottom = innenrand.calculateBottomPadding(),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Abstand.l, vertical = Abstand.m),
            verticalArrangement = Arrangement.spacedBy(Abstand.xs),
        ) {
            Text(
                text = stringResource(R.string.pruefung_titel),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = if (aufgabe == null) {
                    stringResource(R.string.pruefung_fertig_text)
                } else {
                    stringResource(
                        R.string.pruefung_zaehler,
                        (werk.pruefNummer + 1).toString(),
                        Werk.PRUEF_ANZAHL.toString(),
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (aufgabe == null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(Abstand.l),
                verticalArrangement = Arrangement.spacedBy(Abstand.l),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(Abstand.xxl))
                Text(
                    text = stringResource(R.string.pruefung_fertig_kopf),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.pruefung_fertig_hinweis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Werktaste(
                text = stringResource(R.string.pruefung_loslegen),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Abstand.l),
                aufDruck = aufFertig,
            )
            return@Column
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
                modifier = Modifier.padding(vertical = Abstand.xl),
            )
            Eingabefeld(eingabe = werk.eingabe, form = aufgabe.form, gesperrt = false)
            Spacer(Modifier.height(Abstand.l))
        }

        Zahlenfeld(
            gesperrt = false,
            sendbar = werk.eingabe.isNotEmpty() && Bruch.lies(werk.eingabe) != null,
            sendenText = stringResource(R.string.pruefung_antworten),
            aufZeichen = werk::tippe,
            aufLoeschen = werk::loesche,
            aufSenden = werk::pruefeAntwort,
            kopf = {
                Column(verticalArrangement = Arrangement.spacedBy(Abstand.s)) {
                    Nebentaste(
                        text = stringResource(R.string.pruefung_aufgabe_ueberspringen),
                        modifier = Modifier.fillMaxWidth(),
                        aufDruck = werk::ueberspringePruefaufgabe,
                    )
                    Nebentaste(
                        text = stringResource(R.string.pruefung_ueberspringen),
                        modifier = Modifier.fillMaxWidth(),
                        aufDruck = aufFertig,
                    )
                }
            },
        )
    }
}
