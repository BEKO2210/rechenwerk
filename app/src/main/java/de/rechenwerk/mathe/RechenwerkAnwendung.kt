package de.rechenwerk.mathe

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Die Anwendungsklasse. Sie tut nichts weiter, als Hilt den Ort zu geben, an
 * dem der Abhaengigkeitsbaum haengt -- Datenbank, Tafeln, Ablage, Tresor.
 */
@HiltAndroidApp
class RechenwerkAnwendung : Application()
