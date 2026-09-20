package de.rechenwerk.mathe.daten

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.ithandwerkstuttgart.suitekern.Tresor
import de.rechenwerk.mathe.daten.raum.FehlerartTafel
import de.rechenwerk.mathe.daten.raum.RechenwerkRaum
import de.rechenwerk.mathe.daten.raum.SitzungTafel
import de.rechenwerk.mathe.daten.raum.StandTafel
import de.rechenwerk.mathe.daten.raum.VerlaufTafel
import javax.inject.Singleton

/**
 * Woher die Datenschicht kommt. Datenbank, Tafeln und der Tresor werden hier
 * einmal gebaut und von Hilt weitergereicht -- im Ansichtsmodell und in den
 * Bildschirmen steht kein `new` mehr.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatenModul {

    @Provides
    @Singleton
    fun raum(@ApplicationContext zusammenhang: Context): RechenwerkRaum =
        Room.databaseBuilder(
            zusammenhang,
            RechenwerkRaum::class.java,
            RechenwerkRaum.DATEI,
        ).build()

    @Provides
    fun staendeTafel(raum: RechenwerkRaum): StandTafel = raum.staende()

    @Provides
    fun fehlerartTafel(raum: RechenwerkRaum): FehlerartTafel = raum.fehlerarten()

    @Provides
    fun verlaufTafel(raum: RechenwerkRaum): VerlaufTafel = raum.verlauf()

    @Provides
    fun sitzungTafel(raum: RechenwerkRaum): SitzungTafel = raum.sitzung()

    @Provides
    @Singleton
    fun einstellungen(@ApplicationContext zusammenhang: Context): Einstellungsspeicher =
        Einstellungsspeicher(Tresor(zusammenhang, TRESOR_NAME))

    /** Der Tresor heisst wie die App: files/rechenwerk.json. */
    private const val TRESOR_NAME = "rechenwerk"
}
