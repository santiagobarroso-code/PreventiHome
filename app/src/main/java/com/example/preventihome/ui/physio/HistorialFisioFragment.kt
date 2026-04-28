package com.example.preventihome.ui.physio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.preventihome.R
import com.example.preventihome.databinding.FragmentHistorialFisioBinding
import com.example.preventihome.viewmodel.ConsultaUiState
import com.example.preventihome.viewmodel.ConsultaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Pantalla del fisioterapeuta para ver el historial de consultas de un paciente.
 *
 * Recibe los datos del paciente como argumentos de navegación.
 * Muestra todas las consultas ordenadas por fecha y permite:
 * - Ver el detalle de cada consulta
 * - Crear una nueva consulta para el paciente
 *
 * Argumentos requeridos:
 * - "pacienteId": UID del paciente
 * - "pacienteNombre": Nombre del paciente
 * - "pacienteEmail": Correo del paciente
 */
@AndroidEntryPoint
class HistorialFisioFragment : Fragment() {

    private var _binding: FragmentHistorialFisioBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ConsultaViewModel by viewModels()
    private lateinit var adapter: ConsultaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistorialFisioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recibir datos del paciente
        val pacienteId     = arguments?.getString("pacienteId") ?: ""
        val pacienteNombre = arguments?.getString("pacienteNombre") ?: ""
        val pacienteEmail  = arguments?.getString("pacienteEmail") ?: ""

        setupToolbar(pacienteNombre)
        setupHeaderPaciente(pacienteNombre, pacienteEmail)
        setupRecyclerView(pacienteId, pacienteNombre, pacienteEmail)
        setupObserver()
        setupFab(pacienteId, pacienteNombre, pacienteEmail)

        // Cargar consultas del paciente
        viewModel.cargarConsultasPaciente(pacienteId)
    }

    private fun setupToolbar(nombre: String) {
        binding.toolbar.title = nombre.ifEmpty { "Paciente" }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Muestra el avatar con la inicial y los datos del paciente en el header.
     */
    private fun setupHeaderPaciente(nombre: String, email: String) {
        val nombreMostrar = nombre.ifEmpty { email.substringBefore("@") }
        binding.tvNombrePaciente.text = nombreMostrar
        binding.tvEmailPaciente.text = email
        binding.tvAvatarPaciente.text =
            nombreMostrar.firstOrNull()?.uppercaseChar()?.toString() ?: "P"
    }

    /**
     * Configura el RecyclerView con el adapter de consultas.
     * Al tocar una consulta navega al detalle con opción de editar notas.
     */
    private fun setupRecyclerView(
        pacienteId: String,
        pacienteNombre: String,
        pacienteEmail: String
    ) {
        adapter = ConsultaAdapter { consulta ->
            findNavController().navigate(
                R.id.action_historialFisio_to_detalleConsultaFisio,
                bundleOf(
                    "consultaId"     to consulta.id,
                    "pacienteId"     to pacienteId,
                    "pacienteNombre" to pacienteNombre,
                    "pacienteEmail"  to pacienteEmail
                )
            )
        }
        binding.rvConsultas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvConsultas.adapter = adapter
    }

    /**
     * Configura el FAB para crear una nueva consulta para este paciente.
     */
    private fun setupFab(
        pacienteId: String,
        pacienteNombre: String,
        pacienteEmail: String
    ) {
        binding.fabNuevaConsulta.setOnClickListener {
            findNavController().navigate(
                R.id.action_historialFisio_to_crearConsulta,
                bundleOf(
                    "pacienteId"     to pacienteId,
                    "pacienteNombre" to pacienteNombre,
                    "pacienteEmail"  to pacienteEmail
                )
            )
        }
    }

    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ConsultaUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.tvEmpty.visibility = View.GONE
                        }
                        is ConsultaUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            if (state.consultas.isEmpty()) {
                                binding.tvEmpty.visibility = View.VISIBLE
                                binding.rvConsultas.visibility = View.GONE
                            } else {
                                binding.tvEmpty.visibility = View.GONE
                                binding.rvConsultas.visibility = View.VISIBLE
                                adapter.submitList(state.consultas)
                            }
                        }
                        is ConsultaUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.tvEmpty.text = state.message
                            binding.tvEmpty.visibility = View.VISIBLE
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}