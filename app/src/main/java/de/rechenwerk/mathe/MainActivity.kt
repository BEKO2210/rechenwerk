package de.rechenwerk.mathe

import android.graphics.Color as SystemFarbe
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import de.rechenwerk.mathe.daten.Art
import de.rechenwerk.mathe.daten.Bereich
import de.rechenwerk.mathe.daten.Katalog
import de.rechenwerk.mathe.daten.Werk
import de.rechenwerk.mathe.ui.AbschlussBildschirm
import de.rechenwerk.mathe.ui.Abstand
import de.rechenwerk.mathe.ui.BereichBildschirm
import de.rechenwerk.mathe.ui.EinstellungenBildschirm
import de.rechenwerk.mathe.ui.FortschrittBildschirm
import de.rechenwerk.mathe.ui.GlasFlaeche
import de.rechenwerk.mathe.ui.Grund
import de.rechenwerk.mathe.ui.KompetenzBildschirm
import de.rechenwerk.mathe.ui.OnboardingProfil
import de.rechenwerk.mathe.ui.OnboardingTest
import de.rechenwerk.mathe.ui.RechenwerkTheme
import de.rechenwerk.mathe.ui.StartBildschirm
import de.rechenwerk.mathe.ui.Sym
import de.rechenwerk.mathe.ui.TrainingBildschirm
import de.rechenwerk.mathe.ui.istDunkel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(zustand: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(zustand)
        setContent {
            // Das Ansichtsmodell kommt von Hilt samt Datenbank, Tafeln und
            // Tresor -- kein Standardkonstruktor, keine Handarbeit.
            val werk: Werk = hiltViewModel()
            // Die Systemleisten folgen dem gewaehlten Erscheinungsbild, nicht
            // dem des Systems -- sonst staenden dunkle Symbole auf dunklem Glas.
            val dunkel = istDunkel(werk.ablage.modus)
            SideEffect {
                val leiste = SystemBarStyle.auto(
                    SystemFarbe.TRANSPARENT,
                    SystemFarbe.TRANSPARENT,
                ) { dunkel }
                enableEdgeToEdge(statusBarStyle = leiste, navigationBarStyle = leiste)
            }
            RechenwerkTheme(modus = werk.ablage.modus) {
                // Der Grund mit seinen wandernden Lichtblasen liegt hinter
                // allem -- einmal fuer die ganze App, nicht je Bildschirm.
                Grund(modifier = Modifier.fillMaxSize()) {
                    Rahmen(werk = werk)
                }
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
        when {
            // Aus dem Abschlussbild geht es auf die Reiterflaeche zurueck.
            ort == Ort.Training && werk.bilanz != null -> {
                werk.verwirfBilanz()
                ort = Ort.Reiterflaeche
            }
            // Aus dem Training zuerst auf das Abschlussbild -- es sei denn, es
            // wurde noch nichts gerechnet, dann gibt es nichts zu zeigen.
            ort == Ort.Training -> {
                werk.beendeSitzung()
                if (werk.bilanz == null) ort = Ort.Reiterflaeche
            }
            else -> ort = Ort.Reiterflaeche
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        bottomBar = {
            if (ort == Ort.Reiterflaeche) {
                // Die untere Leiste ist Glas: alle Reiter gleich breit, die
                // Flaeche traegt Fuellung, Lichtkante und Haarrand. Sie steht
                // mit demselben Rand links wie rechts frei -- ihr Haarrand lag
                // sonst auf dem Bildschirmrand und sah nach abgeschnittenem
                // Inhalt aus.
                GlasFlaeche(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Abstand.l),
                    form = MaterialTheme.shapes.extraLarge,
                    // Die Leiste steht ueber scrollendem Inhalt: mit der
                    // gewoehnlichen Deckkraft las man die Kachel darunter
                    // durch das Glas hindurch mit. Sie bekommt deshalb die
                    // hervorgehobene Fuellung der Glasebene.
                    hervorgehoben = true,
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
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
            }
        },
    ) { innenrand ->
        when (val hier = ort) {
            Ort.Training -> {
                // Am Ende einer Einheit tritt das Abschlussbild an die Stelle
                // des Trainings -- ein eigener Bildschirm, kein Einschub.
                val bilanz = werk.bilanz
                if (bilanz != null) {
                    AbschlussBildschirm(
                        bilanz = bilanz,
                        aufFertig = {
                            werk.verwirfBilanz()
                            ort = Ort.Reiterflaeche
                        },
                        innenrand = innenrand,
                    )
                } else {
                    TrainingBildschirm(
                        werk = werk,
                        aufEnde = { ort = Ort.Reiterflaeche },
                        innenrand = innenrand,
                    )
                }
            }

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
        containerColor = Color.Transparent,
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
