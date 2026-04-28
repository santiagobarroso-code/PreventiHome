package com.example.preventihome.ui.physio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.preventihome.R
import com.example.preventihome.databinding.FragmentCitasFisioBinding
import com.example.preventihome.ui.patient.CitaAdapter
import com.example.preventihome.viewmodel.CitaUiState
import com.example.preventihome.viewmodel.CitaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Pantalla del fisioterapeuta para ver y atender citas pendientes.
 *
 * Muestra todas las citas con estado "pendiente" de todos los pacientes,
 * ordenadas por fecha para atenderlas en orden cronológico.
 *
 * Al tocar una cita, navega a crear consulta para ese paciente,
 * pasando los datos de la cita para pre-llenar el formulario.
 */
@AndroidEntryPoint
class CitasFisioFragment : Fragment() {

    private var _binding: FragmentCitasFisioBinding? = null
    private val binding get() = _binding!!
    private val citaViewModel: CitaViewModel by viewModels()
    private lateinit var adapter: CitaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCitasFisioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupObserver()
        citaViewModel.cargarCitasPendientes()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * El adapter muestra el nombre del paciente porque
     * el fisio ve citas de todos sus pacientes.
     */
    private fun setupRecyclerView() {
        adapter = CitaAdapter(mostrarPaciente = true) { cita ->
            // Al atender una cita, navegar a crear consulta
            // con los datos de la cita pre-cargados
            findNavController().navigate(
                R.id.action_citasFisio_to_crearConsulta,
                bundleOf(
                    "pacienteId"     to cita.pacienteId,
                    "pacienteNombre" to cita.pacienteNombre,
                    "pacienteEmail"  to cita.pacienteEmail,
                    "citaId"         to cita.id
                )
            )
        }
        binding.rvCitas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCitas.adapter = adapter
    }

    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                citaViewModel.uiState.collect { state ->
                    when (state) {
                        is CitaUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.tvEmpty.visibility = View.GONE
                        }
                        is CitaUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            if (state.citas.isEmpty()) {
                                binding.tvEmpty.visibility = View.VISIBLE
                                binding.rvCitas.visibility = View.GONE
                            } else {
                                binding.tvEmpty.visibility = View.GONE
                                binding.rvCitas.visibility = View.VISIBLE
                                adapter.submitList(state.citas)
                            }
                        }
                        is CitaUiState.Error -> {
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