package de.rechenwerk.mathe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import de.rechenwerk.mathe.daten.Art
import de.rechenwerk.mathe.daten.Bereich
import de.rechenwerk.mathe.daten.Katalog
import de.rechenwerk.mathe.daten.Werk
import de.rechenwerk.mathe.ui.BereichBildschirm
import de.rechenwerk.mathe.ui.EinstellungenBildschirm
import de.rechenwerk.mathe.ui.FortschrittBildschirm
import de.rechenwerk.mathe.ui.KompetenzBildschirm
import de.rechenwerk.mathe.ui.OnboardingProfil
import de.rechenwerk.mathe.ui.OnboardingTest
import de.rechenwerk.mathe.ui.RechenwerkTheme
import de.rechenwerk.mathe.ui.StartBildschirm
import de.rechenwerk.mathe.ui.Sym
import de.rechenwerk.mathe.ui.TrainingBildschirm

class MainActivity : ComponentActivity() {
    override fun onCreate(zustand: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(zustand)
        setContent {
            val werk: Werk = viewModel()
            RechenwerkTheme(modus = werk.ablage.modus) {
                Rahmen(werk = werk)
            }
        }
    }
}

private enum class Reiter { START, FORTSCHRITT, EINSTELLUNGEN }

/** Wo die App gerade steht. Ein Zustand, kein Navigationsgraph. */
private sealed interface Ort {
    data object Reiterflaeche : Ort
    data object Training : Ort
    data class Bereichsliste(val bereich: Bereich) : Ort
    data class Kompetenzblatt(val kennung: String) : Ort
}

/** Damit eine Drehung des Geraets nicht aus dem Training wirft. */
private val ortRetter = Saver<Ort, String>(
    save = { ort ->
        when (ort) {
            Ort.Reiterflaeche -> "R"
            Ort.Training -> "T"
            is Ort.Bereichsliste -> "B:" + ort.bereich.name
            is Ort.Kompetenzblatt -> "K:" + ort.kennung
        }
    },
    restore = { text ->
        when {
            text == "T" -> Ort.Training
            text.startsWith("B:") -> Bereich.entries
                .firstOrNull { it.name == text.substring(2) }
                ?.let { Ort.Bereichsliste(it) } ?: Ort.Reiterflaeche
            text.startsWith("K:") -> Ort.Kompetenzblatt(text.substring(2))
            else -> Ort.Reiterflaeche
        }
    },
)

@Composable
private fun Rahmen(werk: Werk) {
    if (!werk.ablage.profil.eingerichtet) {
        Onboarding(werk = werk)
        return
    }

    var reiter by rememberSaveable { mutableStateOf(Reiter.START) }
    var ort by rememberSaveable(stateSaver = ortRetter) {
        mutableStateOf<Ort>(Ort.Reiterflaeche)
    }

    BackHandler(enabled = ort != Ort.Reiterflaeche) {
        if (ort == Ort.Training) werk.beendeSitzung()
        ort = Ort.Reiterflaeche
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (ort == Ort.Reiterflaeche) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    for (eintrag in Reiter.entries) {
                        NavigationBarItem(
                            selected = reiter == eintrag,
                            onClick = { reiter = eintrag },
                            icon = {
                                Icon(
                                    imageVector = when (eintrag) {
                                        Reiter.START -> Sym.Raster
                                        Reiter.FORTSCHRITT -> Sym.Saeulen
                                        Reiter.EINSTELLUNGEN -> Sym.Schieber
                                    },
                                    contentDescription = null,
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(
                                        when (eintrag) {
                                            Reiter.START -> R.string.reiter_start
                                            Reiter.FORTSCHRITT -> R.string.reiter_fortschritt
                                            Reiter.EINSTELLUNGEN -> R.string.reiter_einstellungen
                                        }
                                    )
                                )
                            },
                        )
                    }
                }
            }
        },
    ) { innenrand ->
        when (val hier = ort) {
            Ort.Training -> TrainingBildschirm(
                werk = werk,
                aufEnde = { ort = Ort.Reiterflaeche },
                innenrand = innenrand,
            )

            is Ort.Bereichsliste -> BereichBildschirm(
                ablage = werk.ablage,
                bereich = hier.bereich,
                aufKompetenz = { ort = Ort.Kompetenzblatt(it) },
                aufZurueck = { ort = Ort.Reiterflaeche },
                innenrand = innenrand,
            )

            is Ort.Kompetenzblatt -> {
                val kompetenz = Katalog.finde(hier.kennung)
                if (kompetenz == null) {
                    LaunchedEffect(hier) { ort = Ort.Reiterflaeche }
                } else {
                    KompetenzBildschirm(
                        ablage = werk.ablage,
                        kompetenz = kompetenz,
                        aufTraining = { kennung ->
                            werk.starteSitzung(Art.GEZIELT, kennung)
                            ort = Ort.Training
                        },
                        aufKompetenz = { ort = Ort.Kompetenzblatt(it) },
                        aufZurueck = { ort = Ort.Reiterflaeche },
                        innenrand = innenrand,
                    )
                }
            }

            Ort.Reiterflaeche -> when (reiter) {
                Reiter.START -> StartBildschirm(
                    ablage = werk.ablage,
                    aufTraining = { art ->
                        werk.starteSitzung(art)
                        ort = Ort.Training
                    },
                    aufBereich = { ort = Ort.Bereichsliste(it) },
                    innenrand = innenrand,
                )

                Reiter.FORTSCHRITT -> FortschrittBildschirm(
                    ablage = werk.ablage,
                    aufKompetenz = { ort = Ort.Kompetenzblatt(it) },
                    innenrand = innenrand,
                )

                Reiter.EINSTELLUNGEN -> EinstellungenBildschirm(
                    werk = werk,
                    innenrand = innenrand,
                )
            }
        }
    }
}

@Composable
private fun Onboarding(werk: Werk) {
    var schritt by rememberSaveable { mutableStateOf(0) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innenrand ->
        if (schritt == 0) {
            OnboardingProfil(
                profil = werk.ablage.profil,
                aufProfil = werk::setzeProfil,
                aufWeiter = {
                    werk.startePruefung()
                    schritt = 1
                },
                innenrand = innenrand,
            )
        } else {
            OnboardingTest(
                werk = werk,
                aufFertig = { werk.schliessePruefungAb(werk.ablage.profil) },
                innenrand = innenrand,
            )
        }
    }
}
