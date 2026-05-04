package com.example.preventihome.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.preventihome.R
import com.example.preventihome.databinding.FragmentPatientHomeBinding
import com.example.preventihome.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment principal del usuario tipo paciente.
 *
 * Funciona como menú de navegación hacia las diferentes
 * secciones de la aplicación:
 * - Ejercicios
 * - Historial
 * - Perfil
 * - Consultas
 * - Citas
 */
@AndroidEntryPoint
class PatientHomeFragment : Fragment() {

    /** Binding para acceder a las vistas */
    private var _binding: FragmentPatientHomeBinding? = null
    private val binding get() = _binding!!

    /** ViewModel de autenticación (útil para futuras acciones como logout) */
    private val authViewModel: AuthViewModel by viewModels()

    /**
     * Infla el layout del fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Se ejecuta cuando la vista ha sido creada.
     *
     * Inicializa:
     * - Listeners de navegación
     * - Configuración de toolbar
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupToolbar()
    }

    /**
     * Configuración de la toolbar.
     *
     * Nota:
     * - Actualmente solo muestra el título
     * - El logout se maneja desde la sección de perfil
     */
    private fun setupToolbar() {
        // Logout se maneja desde la tarjeta de perfil
        // El toolbar solo muestra el título por ahora
    }

    /**
     * Configura los eventos de navegación de las tarjetas.
     *
     * Cada tarjeta redirige a una sección distinta de la app.
     */
    private fun setupClickListeners() {

        /** Navegar a la lista de ejercicios */
        binding.cardEjercicios.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_ejercicios)
        }

        /** Navegar al historial de progreso */
        binding.cardHistorial.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_historial)
        }

        /** Navegar al perfil del usuario */
        binding.cardPerfil.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_perfil)
        }

        /** Navegar a consultas (posible chat o seguimiento) */
        binding.cardConsultas.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_consultas)
        }

        /** Navegar a citas del usuario */
        binding.cardCitas.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_misCitas)
        }
    }

    /**
     * Limpia el binding para evitar memory leaks
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}