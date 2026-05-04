package com.example.preventihome.ui.physio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.preventihome.R
import com.example.preventihome.databinding.FragmentFisioHomeBinding
import com.example.preventihome.viewmodel.AuthViewModel
import com.example.preventihome.viewmodel.FisioUiState
import com.example.preventihome.viewmodel.FisioViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.os.bundleOf

/**
 * Fragment principal para el rol de fisioterapeuta.
 *
 * Permite:
 * - Ver lista de pacientes registrados
 * - Navegar al historial de cada paciente
 * - Acceder a citas pendientes
 * - Acceder al perfil
 * - Cerrar sesión
 */
@AndroidEntryPoint
class FisioHomeFragment : Fragment() {

    /** Binding para acceder a las vistas */
    private var _binding: FragmentFisioHomeBinding? = null
    private val binding get() = _binding!!

    /** ViewModel encargado de la lógica de pacientes */
    private val fisioViewModel: FisioViewModel by viewModels()

    /** ViewModel de autenticación */
    private val authViewModel: AuthViewModel by viewModels()

    /** Adapter para mostrar pacientes en RecyclerView */
    private lateinit var adapter: PacienteAdapter

    /** Instancia de FirebaseAuth inyectada con Hilt */
    @Inject lateinit var auth: FirebaseAuth

    /**
     * Infla el layout del fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFisioHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Se ejecuta cuando la vista ha sido creada.
     *
     * Inicializa:
     * - Toolbar
     * - RecyclerView
     * - Observers
     * - Listeners
     * - Nombre del fisioterapeuta
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupObserver()
        setupClickListeners()
        mostrarNombreFisio()
    }

    /**
     * Configura la toolbar mostrando el correo del usuario como subtítulo.
     */
    private fun setupToolbar() {
        binding.toolbar.subtitle = auth.currentUser?.email ?: ""
    }

    /**
     * Muestra el nombre del fisioterapeuta en el header.
     *
     * Prioridad:
     * 1. Nombre guardado en Firestore
     * 2. Parte del email antes del "@"
     * 3. Texto por defecto
     */
    private fun mostrarNombreFisio() {
        viewLifecycleOwner.lifecycleScope.launch {

            val uid = auth.currentUser?.uid ?: return@launch

            try {
                val user = com.example.preventihome.data.remote.FirestoreSource(
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                ).getUser(uid)

                val nombreMostrar = when {
                    user.nombre.isNotEmpty() -> user.nombre
                    else -> auth.currentUser?.email?.substringBefore("@") ?: "Fisioterapeuta"
                }

                binding.tvBienvenidaFisio.text = "Hola, $nombreMostrar"

            } catch (e: Exception) {

                // Fallback en caso de error
                val email = auth.currentUser?.email ?: ""
                binding.tvBienvenidaFisio.text = "Hola, ${email.substringBefore("@")}"
            }
        }
    }

    /**
     * Configura el RecyclerView de pacientes.
     *
     * Al hacer click en un paciente:
     * - Navega a su historial
     * - Envía datos necesarios por bundle
     */
    private fun setupRecyclerView() {
        adapter = PacienteAdapter { paciente ->

            val nombre = paciente.nombre.ifEmpty {
                paciente.email.substringBefore("@")
            }

            findNavController().navigate(
                R.id.action_fisioHome_to_historialPaciente,
                bundleOf(
                    "pacienteId"     to paciente.uid,
                    "pacienteNombre" to nombre,
                    "pacienteEmail"  to paciente.email
                )
            )
        }

        binding.rvPacientes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPacientes.adapter = adapter
    }

    /**
     * Observa el estado del ViewModel.
     *
     * Maneja:
     * - Loading
     * - Lista de pacientes
     * - Estado vacío
     * - Error
     */
    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fisioViewModel.uiState.collect { state ->
                    when (state) {

                        /** Estado de carga */
                        is FisioUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.tvEmpty.visibility = View.GONE
                        }

                        /** Datos cargados correctamente */
                        is FisioUiState.Success -> {
                            binding.progressBar.visibility = View.GONE

                            val pacientes = state.pacientes

                            binding.tvContadorPacientes.text =
                                "${pacientes.size} paciente(s) registrado(s)"

                            if (pacientes.isEmpty()) {
                                binding.tvEmpty.visibility = View.VISIBLE
                                binding.rvPacientes.visibility = View.GONE
                            } else {
                                binding.tvEmpty.visibility = View.GONE
                                binding.rvPacientes.visibility = View.VISIBLE
                                adapter.submitList(pacientes)
                            }
                        }

                        /** Error al cargar datos */
                        is FisioUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.tvEmpty.text = state.message
                            binding.tvEmpty.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    /**
     * Configura los botones principales del fragment.
     */
    private fun setupClickListeners() {

        /** Cerrar sesión */
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_fisioHome_to_login)
        }

        /** Navegar a citas pendientes */
        binding.btnVerCitas.setOnClickListener {
            findNavController().navigate(R.id.action_fisioHome_to_citasPendientes)
        }

        /** Navegar al perfil del fisioterapeuta */
        binding.btnPerfil.setOnClickListener {
            findNavController().navigate(R.id.action_fisioHome_to_perfilFisio)
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