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
import com.example.preventihome.databinding.FragmentHistorialBinding
import com.example.preventihome.viewmodel.ProgresoUiState
import com.example.preventihome.viewmodel.ProgresoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment encargado de mostrar el historial de progreso del usuario.
 *
 * Permite:
 * - Visualizar la lista de ejercicios realizados
 * - Mostrar estado vacío si no hay datos
 * - Manejar estados de carga y error
 */
@AndroidEntryPoint
class HistorialFragment : Fragment() {

    /** Binding para acceder a las vistas */
    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    /** ViewModel encargado de obtener el historial */
    private val viewModel: ProgresoViewModel by viewModels()

    /** Adapter del RecyclerView */
    private lateinit var adapter: ProgresoAdapter

    /**
     * Infla el layout del fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Se ejecuta cuando la vista ha sido creada.
     *
     * - Configura toolbar
     * - Configura RecyclerView
     * - Observa cambios del ViewModel
     * - Solicita cargar el historial
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupObserver()

        // Solicitar datos al ViewModel
        viewModel.cargarHistorial()
    }

    /**
     * Configura la toolbar (botón de regreso)
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Configura el RecyclerView:
     * - LayoutManager
     * - Adapter
     */
    private fun setupRecyclerView() {
        adapter = ProgresoAdapter()
        binding.rvHistorial.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistorial.adapter = adapter
    }

    /**
     * Observa el estado del historial desde el ViewModel.
     *
     * Maneja:
     * - Loading
     * - Lista de datos
     * - Estado vacío
     * - Error
     */
    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {

                        /** Estado de carga */
                        is ProgresoUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.tvEmpty.visibility = View.GONE
                        }

                        /** Historial cargado */
                        is ProgresoUiState.Historial -> {
                            binding.progressBar.visibility = View.GONE

                            if (state.lista.isEmpty()) {
                                // Mostrar mensaje si no hay datos
                                binding.tvEmpty.visibility = View.VISIBLE
                                binding.rvHistorial.visibility = View.GONE
                            } else {
                                // Mostrar lista
                                binding.tvEmpty.visibility = View.GONE
                                binding.rvHistorial.visibility = View.VISIBLE
                                adapter.submitList(state.lista)
                            }
                        }

                        /** Error al cargar */
                        is ProgresoUiState.Error -> {
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

    /**
     * Limpia el binding para evitar memory leaks
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}