package com.example.preventihome.ui.patient

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
import com.example.preventihome.databinding.FragmentEjerciciosBinding
import com.example.preventihome.viewmodel.EjercicioUiState
import com.example.preventihome.viewmodel.EjercicioViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Pantalla del paciente para explorar el catálogo de ejercicios.
 *
 * Funcionalidades:
 * - Ver todos los ejercicios activos desde Firestore
 * - Filtrar por zona corporal mediante chips
 * - Filtrar por patología activa si el paciente tiene una consulta activa
 * - Filtrar por ejercicios recomendados
 * - Navegar al detalle de un ejercicio para ejecutarlo
 *
 * Si el paciente tiene una consulta activa con patología asignada,
 * el chip "Mi patología" aparece resaltado y pre-seleccionado
 * para mostrar los ejercicios más relevantes para su condición.
 */
@AndroidEntryPoint
class EjerciciosFragment : Fragment() {

    private var _binding: FragmentEjerciciosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EjercicioViewModel by viewModels()
    private lateinit var adapter: EjercicioAdapter

    /** Patología activa del paciente (null si no tiene consulta activa) */
    private var patologiaActiva: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEjerciciosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFiltros()
        setupObservers()
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Configura el RecyclerView con el adapter de ejercicios.
     * Al tocar un ejercicio navega al detalle pasando su ID.
     */
    private fun setupRecyclerView() {
        adapter = EjercicioAdapter { ejercicio ->
            findNavController().navigate(
                R.id.action_ejercicios_to_detalle,
                bundleOf("ejercicioId" to ejercicio.id)
            )
        }
        binding.rvEjercicios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEjercicios.adapter = adapter
    }

    /**
     * Configura los chips de filtro.
     * El chip de patología solo aparece si el paciente tiene consulta activa.
     */
    private fun setupFiltros() {
        binding.chipTodos.setOnClickListener {
            deselectAllChips()
            binding.chipTodos.isChecked = true
            viewModel.filtrarPorZona(null)
        }
        binding.chipRecomendados.setOnClickListener {
            deselectAllChips()
            binding.chipRecomendados.isChecked = true
            viewModel.filtrarRecomendados()
        }
        binding.chipPatologia.setOnClickListener {
            deselectAllChips()
            binding.chipPatologia.isChecked = true
            patologiaActiva?.let { viewModel.filtrarPorPatologia(it) }
        }
        binding.chipRodilla.setOnClickListener {
            deselectAllChips()
            binding.chipRodilla.isChecked = true
            viewModel.filtrarPorZona("rodilla")
        }
        binding.chipLumbar.setOnClickListener {
            deselectAllChips()
            binding.chipLumbar.isChecked = true
            viewModel.filtrarPorZona("lumbar")
        }
        binding.chipCervical.setOnClickListener {
            deselectAllChips()
            binding.chipCervical.isChecked = true
            viewModel.filtrarPorZona("cervical")
        }
        binding.chipHombro.setOnClickListener {
            deselectAllChips()
            binding.chipHombro.isChecked = true
            viewModel.filtrarPorZona("hombro")
        }
        binding.chipTobillo.setOnClickListener {
            deselectAllChips()
            binding.chipTobillo.isChecked = true
            viewModel.filtrarPorZona("tobillo")
        }
    }

    /**
     * Observa tanto el estado de ejercicios como la patología activa.
     * Si detecta una patología activa muestra y configura el chip correspondiente.
     */
    private fun setupObservers() {
        // Observar lista de ejercicios
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is EjercicioUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.rvEjercicios.visibility = View.GONE
                            binding.tvEmpty.visibility = View.GONE
                        }
                        is EjercicioUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            if (state.ejercicios.isEmpty()) {
                                binding.rvEjercicios.visibility = View.GONE
                                binding.tvEmpty.visibility = View.VISIBLE
                            } else {
                                binding.rvEjercicios.visibility = View.VISIBLE
                                binding.tvEmpty.visibility = View.GONE
                                adapter.submitList(state.ejercicios)
                            }
                        }
                        is EjercicioUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.tvEmpty.text = state.message
                            binding.tvEmpty.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

        // Observar patología activa del paciente
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.patologiaActiva.collect { patologia ->
                    patologiaActiva = patologia
                    if (patologia != null) {
                        // Mostrar chip con el nombre legible de la patología
                        val nombreLegible = patologia
                            .replace("_", " ")
                            .replaceFirstChar { it.uppercaseChar() }
                        binding.chipPatologia.text = nombreLegible
                        binding.chipPatologia.visibility = View.VISIBLE

                        // Pre-seleccionar automáticamente el chip de patología
                        deselectAllChips()
                        binding.chipPatologia.isChecked = true
                        viewModel.filtrarPorPatologia(patologia)
                    } else {
                        binding.chipPatologia.visibility = View.GONE
                    }
                }
            }
        }
    }

    /** Desmarca todos los chips de filtro */
    private fun deselectAllChips() {
        binding.chipTodos.isChecked       = false
        binding.chipRecomendados.isChecked = false
        binding.chipPatologia.isChecked   = false
        binding.chipRodilla.isChecked     = false
        binding.chipLumbar.isChecked      = false
        binding.chipCervical.isChecked    = false
        binding.chipHombro.isChecked      = false
        binding.chipTobillo.isChecked     = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}