package com.example.preventihome.ui.physio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.preventihome.databinding.FragmentCrearConsultaBinding
import com.example.preventihome.domain.model.Consulta
import com.example.preventihome.viewmodel.CitaViewModel
import com.example.preventihome.viewmodel.ConsultaUiState
import com.example.preventihome.viewmodel.ConsultaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Pantalla para que el fisioterapeuta cree una nueva consulta para un paciente.
 *
 * Recibe los datos del paciente como argumentos de navegación:
 * - "pacienteId":     UID del paciente en Firebase
 * - "pacienteNombre": Nombre del paciente
 * - "pacienteEmail":  Correo del paciente
 * - "citaId":         ID de la cita origen (opcional — vacío si no viene de una cita)
 *
 * Si viene de una cita pendiente, al guardar la consulta marca
 * la cita como atendida en Firestore.
 */
@AndroidEntryPoint
class CrearConsultaFragment : Fragment() {

    private var _binding: FragmentCrearConsultaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ConsultaViewModel by viewModels()
    private val citaViewModel: CitaViewModel by viewModels()

    /**
     * ID de la cita de origen (si aplica).
     * Declarado como propiedad de clase para que sea accesible
     * desde setupObserver() y setupClickListeners().
     */
    private var citaId: String = ""

    /** Patologías disponibles en la plataforma */
    private val patologias = listOf(
        "lumbalgia",
        "cervicalgia",
        "gonalgia",
        "tendinitis_hombro",
        "esguince_tobillo"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearConsultaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recibir datos del paciente desde la navegación
        val pacienteId     = arguments?.getString("pacienteId") ?: ""
        val pacienteNombre = arguments?.getString("pacienteNombre") ?: ""
        val pacienteEmail  = arguments?.getString("pacienteEmail") ?: ""

        // citaId es opcional — viene si el fisio está atendiendo una cita pendiente
        citaId = arguments?.getString("citaId") ?: ""

        setupToolbar()
        setupSpinner()
        mostrarDatosPaciente(pacienteNombre, pacienteEmail)
        setupObserver()
        setupClickListeners(pacienteId, pacienteNombre, pacienteEmail)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Configura el spinner con las patologías disponibles.
     * Se muestran con formato legible (guiones bajos reemplazados por espacios).
     */
    private fun setupSpinner() {
        val patologiasDisplay = patologias.map { p ->
            p.replace("_", " ").replaceFirstChar { it.uppercaseChar() }
        }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            patologiasDisplay
        )
        binding.spinnerPatologia.adapter = adapter
    }

    /**
     * Muestra el nombre y correo del paciente en el formulario.
     * Estos campos son de solo lectura — el fisio no los puede cambiar.
     */
    private fun mostrarDatosPaciente(nombre: String, email: String) {
        binding.tvPacienteNombre.text = nombre.ifEmpty {
            email.substringBefore("@")
        }
        binding.tvPacienteEmail.text = email
    }

    /**
     * Observa el estado del ViewModel.
     * Al crear exitosamente la consulta:
     * - Si viene de una cita, la marca como atendida
     * - Regresa a la pantalla anterior
     */
    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ConsultaUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnGuardarConsulta.isEnabled = false
                        }
                        is ConsultaUiState.OperacionExitosa -> {
                            binding.progressBar.visibility = View.GONE
                            // Si venimos de una cita pendiente, marcarla como atendida
                            if (citaId.isNotEmpty()) {
                                citaViewModel.marcarComoAtendida(citaId, "")
                            }
                            Toast.makeText(
                                requireContext(),
                                "Consulta creada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack()
                        }
                        is ConsultaUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnGuardarConsulta.isEnabled = true
                            Toast.makeText(
                                requireContext(),
                                state.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnGuardarConsulta.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    /**
     * Configura el botón de guardar consulta.
     * Construye el objeto Consulta con los datos del formulario
     * y lo envía al ViewModel para persistirlo en Firestore.
     *
     * @param pacienteId     UID del paciente
     * @param pacienteNombre Nombre del paciente
     * @param pacienteEmail  Correo del paciente
     */
    private fun setupClickListeners(
        pacienteId: String,
        pacienteNombre: String,
        pacienteEmail: String
    ) {
        binding.btnGuardarConsulta.setOnClickListener {
            val diagnostico    = binding.etDiagnostico.text.toString().trim()
            val notas          = binding.etNotas.text.toString().trim()
            val patologiaIndex = binding.spinnerPatologia.selectedItemPosition
            val patologia      = patologias[patologiaIndex]

            val consulta = Consulta(
                pacienteId     = pacienteId,
                pacienteNombre = pacienteNombre,
                pacienteEmail  = pacienteEmail,
                patologia      = patologia,
                diagnostico    = diagnostico,
                notas          = notas
            )
            viewModel.crearConsulta(consulta)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}