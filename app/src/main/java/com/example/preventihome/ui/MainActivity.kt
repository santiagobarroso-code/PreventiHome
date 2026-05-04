package com.example.preventihome.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.preventihome.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Actividad principal de la aplicación.
 *
 * Es el contenedor base donde se alojan los fragments
 * a través del sistema de navegación (NavHostFragment).
 *
 * También es el punto de entrada de la app.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    /** Binding para acceder a las vistas del layout */
    private lateinit var binding: ActivityMainBinding

    /**
     * Método llamado al crear la actividad.
     *
     * - Inicializa el ViewBinding
     * - Establece el layout principal
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar binding
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Establecer la vista raíz de la actividad
        setContentView(binding.root)
    }
}