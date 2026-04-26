package com.example.preventihome.ui.patient

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
import com.example.preventihome.databinding.FragmentEvaluacionBinding
import com.example.preventihome.domain.model.Progreso
import com.example.preventihome.viewmodel.ProgresoUiState
import com.example.preventihome.viewmodel.ProgresoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EvaluacionFragment : Fragment() {

    private var _binding: FragmentEvaluacionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProgresoViewModel by viewModels()

    private var evaluacionSeleccionada: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEvaluacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recibir argumentos del detalle
        val ejercicioId     = arguments?.getString("ejercicioId") ?: ""
        val ejercicioNombre = arguments?.getString("ejercicioNombre") ?: ""
        val ejercicioZona   = arguments?.getString("ejercicioZona") ?: ""
        val duracion        = arguments?.getInt("duracion") ?: 0
        val series          = arguments?.getInt("series") ?: 0
        val repeticiones    = arguments?.getInt("repeticiones") ?: 0

        // Mostrar info del ejercicio
        binding.tvNombreEjercicio.text = ejercicioNombre
        val minutos  = duracion / 60
        val segundos = duracion % 60
        binding.tvDuracion.text = "Duración: %02d:%02d min".format(minutos, segundos)

        setupEvaluacion()
        setupObserver()

        binding.btnGuardar.setOnClickListener {
            if (evaluacionSeleccionada.isEmpty()) {
                Toast.makeText(requireContext(), "Selecciona una evaluación", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val progreso = Progreso(
                ejercicioId      = ejercicioId,
                ejercicioNombre  = ejercicioNombre,
                zona             = ejercicioZona,
                duracionSegundos = duracion,
                evaluacion       = evaluacionSeleccionada,
                series           = series,
                repeticiones     = repeticiones
            )
            viewModel.guardarProgreso(progreso)
        }
    }

    private fun setupEvaluacion() {
        binding.btnBien.setOnClickListener {
            seleccionarEvaluacion("bien", binding.btnBien)
        }
        binding.btnRegular.setOnClickListener {
            seleccionarEvaluacion("regular", binding.btnRegular)
        }
        binding.btnMal.setOnClickListener {
            seleccionarEvaluacion("mal", binding.btnMal)
        }
    }

    private fun seleccionarEvaluacion(valor: String, vistaSeleccionada: View) {
        // Deseleccionar todos
        binding.btnBien.isSelected    = false
        binding.btnRegular.isSelected = false
        binding.btnMal.isSelected     = false

        // Seleccionar el tocado
        vistaSeleccionada.isSelected = true
        evaluacionSeleccionada = valor

        val texto = when (valor) {
            "bien"    -> "Seleccionaste: Bien 👍"
            "regular" -> "Seleccionaste: Regular 😐"
            "mal"     -> "Seleccionaste: Mal 👎"
            else      -> ""
        }
        binding.tvSeleccionada.text = texto
        binding.tvSeleccionada.visibility = View.VISIBLE
        binding.btnGuardar.isEnabled = true
    }

    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ProgresoUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnGuardar.isEnabled = false
                        }
                        is ProgresoUiState.Guardado -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "¡Progreso guardado!",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Volver al home del paciente
                            findNavController().popBackStack(
                                com.example.preventihome.R.id.patientHomeFragment,
                                false
                            )
                        }
                        is ProgresoUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnGuardar.isEnabled = true
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
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