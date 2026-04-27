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
import com.example.preventihome.databinding.FragmentHistorialPacienteBinding
import com.example.preventihome.domain.model.Consulta
import com.example.preventihome.viewmodel.ConsultaUiState
import com.example.preventihome.viewmodel.ConsultaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de detalle de una consulta vista por el paciente.
 *
 * Recibe el ID de la consulta como argumento de navegación y carga
 * los datos completos desde Firestore. Muestra:
 * - Patología diagnosticada con badge de color
 * - Diagnóstico completo del fisioterapeuta
 * - Notas de seguimiento
 * - Fecha y estado de la consulta
 *
 * Argumento requerido: "consultaId" (String)
 */
@AndroidEntryPoint
class DetalleConsultaFragment : Fragment() {

    private var _binding: FragmentHistorialPacienteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ConsultaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistorialPacienteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()

        // Obtener ID de la consulta desde los argumentos de navegación
        val consultaId = arguments?.getString("consultaId") ?: return

        setupObserver(consultaId)
        // Cargar las consultas del paciente para filtrar la seleccionada
        viewModel.cargarMisConsultas()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Observa las consultas cargadas y muestra la que corresponde al ID recibido.
     * @param consultaId ID de la consulta a mostrar
     */
    private fun setupObserver(consultaId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state is ConsultaUiState.Success) {
                        // Encontrar la consulta específica por ID
                        val consulta = state.consultas.find { it.id == consultaId }
                        consulta?.let { mostrarConsulta(it) }
                    }
                }
            }
        }
    }

    /**
     * Rellena las vistas con los datos de la consulta.
     * @param consulta Objeto Consulta con todos los datos del diagnóstico
     */
    private fun mostrarConsulta(consulta: Consulta) {
        // Formatear y mostrar patología
        binding.tvPatologiaDetalle.text = consulta.patologia
            .replace("_", " ")
            .replaceFirstChar { it.uppercaseChar() }

        binding.tvDiagnosticoDetalle.text = consulta.diagnostico

        // Mostrar notas o mensaje por defecto
        binding.tvNotasDetalle.text = consulta.notas.ifEmpty {
            "El fisioterapeuta aún no ha agregado notas de seguimiento."
        }

        // Formatear fecha
        val formato = SimpleDateFormat("dd 'de' MMMM yyyy", Locale("es", "MX"))
        binding.tvFechaDetalle.text = "Fecha: ${formato.format(Date(consulta.fecha))}"

        binding.tvEstadoDetalle.text = "Estado: ${
            when (consulta.estado) {
                "activa"  -> "Consulta activa"
                "cerrada" -> "Consulta cerrada"
                else      -> consulta.estado
            }
        }"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}