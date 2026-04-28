package com.example.preventihome.ui.patient

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.example.preventihome.databinding.FragmentAgendarCitaBinding
import com.example.preventihome.domain.model.Cita
import com.example.preventihome.viewmodel.CitaUiState
import com.example.preventihome.viewmodel.CitaViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * Formulario para que el paciente agende una nueva cita.
 *
 * El paciente selecciona:
 * - Fecha y hora preferida mediante DatePickerDialog y TimePickerDialog
 * - Motivo de la consulta en campo de texto libre
 *
 * La cita se guarda en Firestore con estado "pendiente"
 * para que el fisioterapeuta la vea y la atienda.
 */
@AndroidEntryPoint
class AgendarCitaFragment : Fragment() {

    private var _binding: FragmentAgendarCitaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CitaViewModel by viewModels()

    @Inject lateinit var auth: FirebaseAuth

    /** Calendario con la fecha/hora seleccionada por el usuario */
    private val calendarioSeleccionado = Calendar.getInstance()
    private var fechaSeleccionada = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgendarCitaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupObserver()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Muestra DatePickerDialog seguido de TimePickerDialog.
     * Una vez seleccionados ambos, actualiza el TextView con la fecha formateada.
     */
    private fun mostrarSelectorFecha() {
        val hoy = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendarioSeleccionado.set(year, month, day)
                // Después de elegir la fecha, elegir la hora
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        calendarioSeleccionado.set(Calendar.HOUR_OF_DAY, hour)
                        calendarioSeleccionado.set(Calendar.MINUTE, minute)
                        fechaSeleccionada = true

                        // Mostrar la fecha seleccionada en formato legible
                        val formato = SimpleDateFormat(
                            "EEEE dd 'de' MMMM yyyy 'a las' HH:mm 'hrs'",
                            Locale("es", "MX")
                        )
                        binding.tvFechaSeleccionada.text =
                            formato.format(calendarioSeleccionado.time)
                    },
                    hoy.get(Calendar.HOUR_OF_DAY),
                    0,
                    true
                ).show()
            },
            hoy.get(Calendar.YEAR),
            hoy.get(Calendar.MONTH),
            hoy.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // No permitir fechas pasadas
            datePicker.minDate = hoy.timeInMillis
        }.show()
    }

    /**
     * Observa el estado del ViewModel.
     * Al agendar exitosamente regresa a la lista de citas.
     */
    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is CitaUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnConfirmarCita.isEnabled = false
                        }
                        is CitaUiState.OperacionExitosa -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                requireContext(),
                                "Cita agendada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack()
                        }
                        is CitaUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnConfirmarCita.isEnabled = true
                            Toast.makeText(
                                requireContext(),
                                state.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnConfirmarCita.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSeleccionarFecha.setOnClickListener {
            mostrarSelectorFecha()
        }

        binding.btnConfirmarCita.setOnClickListener {
            if (!fechaSeleccionada) {
                Toast.makeText(
                    requireContext(),
                    "Selecciona una fecha para la cita",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val motivo = binding.etMotivo.text.toString().trim()
            val usuario = auth.currentUser

            val cita = Cita(
                pacienteId     = usuario?.uid ?: "",
                pacienteNombre = usuario?.displayName ?: "",
                pacienteEmail  = usuario?.email ?: "",
                fechaCita      = calendarioSeleccionado.timeInMillis,
                motivo         = motivo
            )
            viewModel.agendarCita(cita)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}