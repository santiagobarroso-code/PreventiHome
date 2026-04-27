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
import com.example.preventihome.databinding.FragmentConsultasPacienteBinding
import com.example.preventihome.ui.physio.ConsultaAdapter
import com.example.preventihome.viewmodel.ConsultaUiState
import com.example.preventihome.viewmodel.ConsultaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Pantalla del paciente para ver sus consultas registradas por el fisioterapeuta.
 *
 * Muestra una lista de consultas ordenadas por fecha con:
 * - Patología diagnosticada
 * - Diagnóstico del fisio
 * - Notas de seguimiento
 * - Estado de la consulta (activa/cerrada)
 *
 * Al tocar una consulta navega al detalle completo.
 */
@AndroidEntryPoint
class ConsultasPacienteFragment : Fragment() {

    private var _binding: FragmentConsultasPacienteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ConsultaViewModel by viewModels()
    private lateinit var adapter: ConsultaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsultasPacienteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupObserver()
        viewModel.cargarMisConsultas()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Configura el RecyclerView con el adapter de consultas.
     * Al tocar una consulta navega al detalle pasando el ID como argumento.
     */
    private fun setupRecyclerView() {
        adapter = ConsultaAdapter { consulta ->
            findNavController().navigate(
                R.id.action_consultasPaciente_to_detalleConsulta,
                bundleOf("consultaId" to consulta.id)
            )
        }
        binding.rvConsultas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvConsultas.adapter = adapter
    }

    /**
     * Observa el StateFlow del ViewModel y actualiza la UI
     * según el estado actual.
     */
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