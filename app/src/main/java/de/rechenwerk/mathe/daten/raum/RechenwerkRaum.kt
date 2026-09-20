package de.rechenwerk.mathe.daten.raum

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Die Datenbank der fachlichen Daten. Sie startet in Fassung 1 und verliert
 * keine Daten: spaetere Fassungen wandern ueber eine Migration gegen das
 * ausgegebene Schema. `fallbackToDestructiveMigration` kommt nicht vor.
 */
@Database(
    entities = [
        KompetenzStandZeile::class,
        FehlerartZeile::class,
        VerlaufZeile::class,
        LaufendZeile::class,
        WerteZeile::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class RechenwerkRaum : RoomDatabase() {

    abstract fun staende(): StandTafel

    abstract fun fehlerarten(): FehlerartTafel

    abstract fun verlauf(): VerlaufTafel

    abstract fun sitzung(): SitzungTafel

    companion object {
        /** Die SQLite-Datei liegt unter databases/, nicht im Tresor-Verzeichnis. */
        const val DATEI = "rechenwerk.db"
    }
}
