package de.ithandwerkstuttgart.suitekern

import android.content.Context
import android.net.Uri

/**
 * Datei-Export und -Import ueber den System-Dateidialog (Storage Access
 * Framework). Die App holt sich die Uri mit
 * ActivityResultContracts.CreateDocument(Export.MIME) bzw. OpenDocument
 * und reicht sie hierher. Braucht keine Speicherberechtigung.
 */
object Export {
    const val MIME = "application/json"

    /** Schreibt [inhalt] an die vom Nutzer gewaehlte [uri]. */
    fun schreiben(context: Context, uri: Uri, inhalt: String) {
        val aus = context.contentResolver.openOutputStream(uri, "wt")
            ?: throw IllegalStateException("Export: Ziel nicht beschreibbar")
        aus.use { it.write(inhalt.toByteArray(Charsets.UTF_8)) }
    }

    /** Liest die vom Nutzer gewaehlte [uri] vollstaendig als Text. */
    fun lesen(context: Context, uri: Uri): String {
        val ein = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Export: Quelle nicht lesbar")
        return ein.use { it.readBytes().toString(Charsets.UTF_8) }
    }
}
