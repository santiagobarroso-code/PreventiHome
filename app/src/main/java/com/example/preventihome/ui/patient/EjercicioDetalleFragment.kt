package com.example.preventihome.ui.patient

import android.os.Bundle
import android.os.SystemClock
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
import com.example.preventihome.R
import com.example.preventihome.databinding.FragmentEjercicioDetalleBinding
import com.example.preventihome.domain.model.Ejercicio
import com.example.preventihome.viewmodel.EjercicioUiState
import com.example.preventihome.viewmodel.EjercicioViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EjercicioDetalleFragment : Fragment() {

    private var _binding: FragmentEjercicioDetalleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EjercicioViewModel by viewModels()

    private var ejercicioActual: Ejercicio? = null
    private var timerJob: Job? = null
    private var segundosTranscurridos = 0
    private var ejercicioIniciado = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEjercicioDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ejercicioId = arguments?.getString("ejercicioId") ?: return

        setupToolbar()
        setupObserver()
        viewModel.cargarEjercicioById(ejercicioId)
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ejercicioDetalle.collect { ejercicio ->
                    ejercicio?.let { mostrarEjercicio(it) }
                }
            }
        }
    }

    private fun mostrarEjercicio(ejercicio: Ejercicio) {
        ejercicioActual = ejercicio
        binding.toolbar.title = ejercicio.nombre
        binding.tvNombre.text = ejercicio.nombre
        binding.tvDescripcion.text = ejercicio.descripcion
        binding.tvZona.text = ejercicio.zona.replaceFirstChar { it.uppercase() }
        binding.tvSeriesNum.text = ejercicio.series.toString()
        binding.tvRepsNum.text = ejercicio.repeticiones.toString()
        binding.tvDificultad.text = ejercicio.dificultad.toString()
    }

    private fun setupClickListeners() {
        binding.btnIniciar.setOnClickListener {
            iniciarEjercicio()
        }
        binding.btnCompletar.setOnClickListener {
            completarEjercicio()
        }
    }

    private fun iniciarEjercicio() {
        ejercicioIniciado = true
        segundosTranscurridos = 0
        binding.btnIniciar.visibility = View.GONE
        binding.tvTimer.visibility = View.VISIBLE
        binding.btnCompletar.visibility = View.VISIBLE

        timerJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                delay(1000)
                segundosTranscurridos++
                val minutos = segundosTranscurridos / 60
                val segundos = segundosTranscurridos % 60
                binding.tvTimer.text = String.format("%02d:%02d", minutos, segundos)
            }
        }
    }

    private fun completarEjercicio() {
        timerJob?.cancel()
        val ejercicio = ejercicioActual ?: return

        // Navegar a evaluación pasando datos del ejercicio
        findNavController().navigate(
            R.id.action_detalle_to_evaluacion,
            bundleOf(
                "ejercicioId"      to ejercicio.id,
                "ejercicioNombre"  to ejercicio.nombre,
                "ejercicioZona"    to ejercicio.zona,
                "duracion"         to segundosTranscurridos,
                "series"           to ejercicio.series,
                "repeticiones"     to ejercicio.repeticiones
            )
        )
    }

    override fun onDestroyView() {
        timerJob?.cancel()
        super.onDestroyView()
        _binding = null
    }
}