package com.example.preventihome.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.preventihome.R
import com.example.preventihome.databinding.FragmentMisCitasBinding
import com.example.preventihome.viewmodel.CitaUiState
import com.example.preventihome.viewmodel.CitaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Pantalla del paciente para ver y gestionar sus citas.
 *
 * Muestra todas las citas del paciente ordenadas por fecha.
 * Permite agendar nuevas citas mediante el FAB.
 */
@AndroidEntryPoint
class MisCitasFragment : Fragment() {

    private var _binding: FragmentMisCitasBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CitaViewModel by viewModels()
    private lateinit var adapter: CitaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMisCitasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupObserver()
        setupFab()
        viewModel.cargarMisCitas()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * RecyclerView sin nombre de paciente (es la vista del propio paciente).
     */
    private fun setupRecyclerView() {
        adapter = CitaAdapter(mostrarPaciente = false) { _ ->
            // Por ahora solo informativo — en versión futura permitir cancelar
        }
        binding.rvCitas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCitas.adapter = adapter
    }

    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
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

    private fun setupFab() {
        binding.fabNuevaCita.setOnClickListener {
            findNavController().navigate(
                R.id.action_misCitas_to_agendarCita
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}