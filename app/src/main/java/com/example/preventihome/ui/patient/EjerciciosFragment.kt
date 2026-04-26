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

@AndroidEntryPoint
class EjerciciosFragment : Fragment() {

    private var _binding: FragmentEjerciciosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EjercicioViewModel by viewModels()
    private lateinit var adapter: EjercicioAdapter

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
        setupObserver()
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = EjercicioAdapter { ejercicio ->
            // Navegar al detalle pasando el ID del ejercicio
            findNavController().navigate(
                R.id.action_ejercicios_to_detalle,
                bundleOf("ejercicioId" to ejercicio.id)
            )
        }
        binding.rvEjercicios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEjercicios.adapter = adapter
    }

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

    private fun deselectAllChips() {
        binding.chipTodos.isChecked = false
        binding.chipRecomendados.isChecked = false
        binding.chipRodilla.isChecked = false
        binding.chipLumbar.isChecked = false
        binding.chipCervical.isChecked = false
        binding.chipHombro.isChecked = false
        binding.chipTobillo.isChecked = false
    }

    private fun setupObserver() {
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}