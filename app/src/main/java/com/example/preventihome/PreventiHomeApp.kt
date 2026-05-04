package com.example.preventihome

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase Application de la app PreventiHome.
 *
 * Es el punto de inicialización global de la aplicación.
 *
 * La anotación @HiltAndroidApp:
 * - Activa la inyección de dependencias con Hilt
 * - Genera automáticamente los componentes necesarios
 * - Permite usar @AndroidEntryPoint en Activities, Fragments, etc.
 */
@HiltAndroidApp
class PreventiHomeApp : Application()