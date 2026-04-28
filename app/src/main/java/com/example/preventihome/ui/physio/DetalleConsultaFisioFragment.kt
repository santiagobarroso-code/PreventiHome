package com.example.preventihome.ui.physio

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
import com.example.preventihome.databinding.FragmentDetalleConsultaFisioBinding
import com.example.preventihome.domain.model.Consulta
import com.example.preventihome.viewmodel.ConsultaUiState
import com.example.preventihome.viewmodel.ConsultaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Detalle de una consulta visto por el fisioterapeuta.
 *
 * Permite ver todos los datos de la consulta y realizar dos acciones:
 * - Actualizar las notas de seguimiento
 * - Cerrar la consulta
 *
 * Argumentos requeridos:
 * - "consultaId": ID del documento en Firestore
 * - "pacienteId": UID del paciente (para recargar la lista)
 */
@AndroidEntryPoint
class DetalleConsultaFisioFragment : Fragment() {

    private var _binding: FragmentDetalleConsultaFisioBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ConsultaViewModel by viewModels()

    /** Consulta actualmente mostrada — necesaria para las acciones de edición */
    private var consultaActual: Consulta? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleConsultaFisioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val consultaId = arguments?.getString("consultaId") ?: ""
        val pacienteId = arguments?.getString("pacienteId") ?: ""

        setupToolbar()
        setupObserver(consultaId, pacienteId)
        setupClickListeners(pacienteId)

        // Cargar consultas del paciente para encontrar la seleccionada
        viewModel.cargarConsultasPaciente(pacienteId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Observa el estado y muestra la consulta correspondiente al ID recibido.
     * Después de una operación exitosa (actualizar/cerrar) regresa al historial.
     */
    private fun setupObserver(consultaId: String, pacienteId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ConsultaUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is ConsultaUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            // Buscar la consulta por ID en la lista cargada
                            val consulta = state.consultas.find { it.id == consultaId }
                            consulta?.let {
                                consultaActual = it
                                mostrarConsulta(it)
                            }
                        }
                        is ConsultaUiState.OperacionExitosa -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Cambios guardados correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Regresar al historial del paciente
                            findNavController().popBackStack()
                        }
                        is ConsultaUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(), state.message, Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    /**
     * Rellena las vistas con los datos de la consulta.
     * Las notas se cargan en el campo editable para que el fisio las modifique.
     * Si la consulta está cerrada, desactiva los campos de edición.
     */
    private fun mostrarConsulta(consulta: Consulta) {
        binding.tvPatologia.text = consulta.patologia
            .replace("_", " ")
            .replaceFirstChar { it.uppercaseChar() }

        binding.tvDiagnostico.text = consulta.diagnostico
        binding.etNotas.setText(consulta.notas)

        val formato = SimpleDateFormat("dd 'de' MMMM yyyy", Locale("es", "MX"))
        binding.tvFecha.text = "Consulta del ${formato.format(Date(consulta.fecha))}"

        // Si la consulta está cerrada, desactivar edición
        if (consulta.estado == "cerrada") {
            binding.etNotas.isEnabled = false
            binding.btnActualizarNotas.isEnabled = false
            binding.btnCerrarConsulta.isEnabled = false
            binding.btnCerrarConsulta.text = "Consulta cerrada"
        }
    }

    /**
     * Configura los botones de actualizar notas y cerrar consulta.
     */
    private fun setupClickListeners(pacienteId: String) {
        binding.btnActualizarNotas.setOnClickListener {
            val consulta = consultaActual ?: return@setOnClickListener
            val nuevasNotas = binding.etNotas.text.toString().trim()
            viewModel.actualizarNotas(consulta.id, nuevasNotas)
        }

        binding.btnCerrarConsulta.setOnClickListener {
            val consulta = consultaActual ?: return@setOnClickListener
            viewModel.cerrarConsulta(consulta.id)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}