package de.ithandwerkstuttgart.suitekern

import android.content.Context
import java.io.File
import java.io.FileOutputStream

/**
 * Lokaler Textspeicher im privaten App-Verzeichnis. Eine Datei je Name,
 * Inhalt ist eine Zeichenkette (ueblich: JSON). Schreiben ist atomar:
 * erst in eine Nebendatei, dann fsync, dann umbenennen. Ein Absturz
 * mitten im Schreiben hinterlaesst nie eine halbe Datei.
 *
 * Kein Netz, kein Konto, kein Android-Backup: die Suite setzt
 * allowBackup="false" und bietet stattdessen [Export] an.
 */
class Tresor(context: Context, name: String) {
    private val datei = File(context.filesDir, "$name.json")

    /** Gespeicherter Inhalt oder null, wenn noch nie geschrieben wurde. */
    fun lesen(): String? = if (datei.isFile) datei.readText(Charsets.UTF_8) else null

    fun schreiben(inhalt: String) {
        val neben = File(datei.parentFile, datei.name + ".tmp")
        FileOutputStream(neben).use { aus ->
            aus.write(inhalt.toByteArray(Charsets.UTF_8))
            aus.fd.sync()
        }
        if (!neben.renameTo(datei)) {
            datei.delete()
            check(neben.renameTo(datei)) { "Tresor: Umbenennen nach ${datei.name} gescheitert" }
        }
    }

    fun loeschen(): Boolean = datei.delete()
}
