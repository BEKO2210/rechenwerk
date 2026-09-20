package de.rechenwerk.mathe.daten.raum

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Die Zugaenge zu den Tabellen, einer je Bereich. Lesende Abfragen geben
 * [Flow] zurueck: die Oberflaeche bekommt jede Aenderung von selbst, ohne
 * irgendwo nachzufragen.
 */

@Dao
interface StandTafel {

    @Query("SELECT * FROM kompetenzstand")
    fun alle(): Flow<List<KompetenzStandZeile>>

    @Upsert
    suspend fun schreibe(zeile: KompetenzStandZeile)

    @Insert
    suspend fun legeAlle(zeilen: List<KompetenzStandZeile>)

    @Query("DELETE FROM kompetenzstand")
    suspend fun leere()
}

@Dao
interface FehlerartTafel {

    @Query("SELECT * FROM fehlerart")
    fun alle(): Flow<List<FehlerartZeile>>

    @Upsert
    suspend fun schreibeAlle(zeilen: List<FehlerartZeile>)

    @Query("DELETE FROM fehlerart WHERE kennung = :kennung")
    suspend fun leereZu(kennung: String)

    @Query("DELETE FROM fehlerart")
    suspend fun leere()
}

@Dao
interface VerlaufTafel {

    @Query("SELECT * FROM verlauf ORDER BY nummer ASC")
    fun alle(): Flow<List<VerlaufZeile>>

    @Insert
    suspend fun lege(zeile: VerlaufZeile)

    @Insert
    suspend fun legeAlle(zeilen: List<VerlaufZeile>)

    /** Haelt die Historie kurz: alles ausser den juengsten [behalten] faellt weg. */
    @Query(
        "DELETE FROM verlauf WHERE nummer NOT IN " +
            "(SELECT nummer FROM verlauf ORDER BY nummer DESC LIMIT :behalten)"
    )
    suspend fun kuerze(behalten: Int)

    @Query("DELETE FROM verlauf")
    suspend fun leere()
}

/** Laufende Aufgabe und Gesamtzahlen -- beides je eine feste Zeile 0. */
@Dao
interface SitzungTafel {

    // Beide Abfragen geben eine Liste mit hoechstens einer Zeile zurueck: das
    // ist eindeutiger als ein Fluss ueber einen Wert, den es noch nicht gibt.
    @Query("SELECT * FROM laufend WHERE zeile = 0")
    fun laufend(): Flow<List<LaufendZeile>>

    @Upsert
    suspend fun schreibeLaufend(zeile: LaufendZeile)

    @Query("DELETE FROM laufend")
    suspend fun leereLaufend()

    @Query("SELECT * FROM werte WHERE zeile = 0")
    fun werte(): Flow<List<WerteZeile>>

    @Upsert
    suspend fun schreibeWerte(zeile: WerteZeile)

    @Query("DELETE FROM werte")
    suspend fun leereWerte()
}
