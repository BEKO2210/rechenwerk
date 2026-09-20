package de.rechenwerk.mathe.daten

import de.ithandwerkstuttgart.suitekern.Tresor

/**
 * Der Suite-Tresor fuehrt weiterhin die Einstellungen: Profil,
 * Erscheinungsbild und das Merkzeichen der Room-Uebernahme. Er wird nicht nur
 * beim Start gelesen -- jede Aenderung schreibt ihn sofort, so dass nach einem
 * Absturz und Neustart eine gueltige JSON-Datei unter files/ liegt.
 *
 * Die fachlichen Daten stehen daneben in der Room-Datenbank unter databases/.
 */
class Einstellungsspeicher(private val tresor: Tresor) {

    /**
     * Liest den Tresor. Vor der Uebernahme steht dort noch der vollstaendige
     * alte Stand -- deshalb gibt diese Stelle die ganze [Ablage] zurueck und
     * nicht nur Profil und Modus. Eine unlesbare Datei ergibt einen frischen
     * Stand, keinen Absturz.
     */
    fun lies(): Ablage {
        val roh = tresor.lesen() ?: return Ablage()
        return try {
            Papier.lies(roh)
        } catch (fehler: IllegalArgumentException) {
            Ablage()
        }
    }

    /** Schreibt Profil, Erscheinungsbild und Merkzeichen. Blockiert -- gehoert auf den IO-Faden. */
    fun schreibe(ablage: Ablage) {
        tresor.schreiben(Papier.schreibe(ablage.nurEinstellungen()))
    }
}
