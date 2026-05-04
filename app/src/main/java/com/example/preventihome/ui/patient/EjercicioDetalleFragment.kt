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
import coil.load

/**
 * Fragment que muestra el detalle de un ejercicio.
 *
 * Permite:
 * - Visualizar información completa del ejercicio
 * - Iniciar un temporizador para medir duración
 * - Completar el ejercicio y pasar a evaluación
 */
@AndroidEntryPoint
class EjercicioDetalleFragment : Fragment() {

    /** Binding para acceder a las vistas */
    private var _binding: FragmentEjercicioDetalleBinding? = null
    private val binding get() = _binding!!

    /** ViewModel encargado de la lógica de ejercicios */
    private val viewModel: EjercicioViewModel by viewModels()

    /** Ejercicio actualmente cargado */
    private var ejercicioActual: Ejercicio? = null

    /** Job del temporizador (coroutine) */
    private var timerJob: Job? = null

    /** Tiempo transcurrido en segundos */
    private var segundosTranscurridos = 0

    /** Indica si el ejercicio ya fue iniciado */
    private var ejercicioIniciado = false

    /**
     * Infla el layout del fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEjercicioDetalleBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Se ejecuta cuando la vista ya fue creada.
     *
     * - Obtiene el ID del ejercicio desde argumentos
     * - Inicializa toolbar, observer y listeners
     * - Solicita el ejercicio al ViewModel
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ejercicioId = arguments?.getString("ejercicioId") ?: return

        setupToolbar()
        setupObserver()
        viewModel.cargarEjercicioById(ejercicioId)
        setupClickListeners()
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
     * Observa los cambios en el ejercicio seleccionado.
     *
     * Cuando se recibe, se muestra en pantalla.
     */
    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ejercicioDetalle.collect { ejercicio ->
                    ejercicio?.let { mostrarEjercicio(it) }
                }
            }
        }
    }

    /**
     * Muestra los datos del ejercicio en la UI.
     *
     * @param ejercicio Ejercicio a mostrar
     */
    private fun mostrarEjercicio(ejercicio: Ejercicio) {
        ejercicioActual = ejercicio

        binding.toolbar.title = ejercicio.nombre
        binding.tvNombre.text = ejercicio.nombre
        binding.tvDescripcion.text = ejercicio.descripcion
        binding.tvZona.text = ejercicio.zona.replaceFirstChar { it.uppercase() }
        binding.tvSeriesNum.text = ejercicio.series.toString()
        binding.tvRepsNum.text = ejercicio.repeticiones.toString()
        binding.tvDificultad.text = ejercicio.dificultad.toString()
        // Cargar imagen si existe URL
        if (ejercicio.imagenUrl.isNotEmpty()) {
            binding.ivEjercicio.visibility = View.VISIBLE
            binding.ivEjercicio.load(ejercicio.imagenUrl) {
                crossfade(true)
                error(android.R.drawable.ic_menu_gallery)
            }
        } else {
            binding.ivEjercicio.visibility = View.GONE
        }
    }

    /**
     * Configura los botones de la interfaz.
     */
    private fun setupClickListeners() {

        /** Iniciar ejercicio */
        binding.btnIniciar.setOnClickListener {
            iniciarEjercicio()
        }

        /** Completar ejercicio */
        binding.btnCompletar.setOnClickListener {
            completarEjercicio()
        }
    }

    /**
     * Inicia el ejercicio y el temporizador.
     *
     * - Resetea el tiempo
     * - Muestra el timer
     * - Oculta botón de iniciar
     * - Lanza una coroutine que incrementa el tiempo cada segundo
     */
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

    /**
     * Finaliza el ejercicio.
     *
     * - Detiene el temporizador
     * - Navega a la pantalla de evaluación
     * - Envía datos relevantes del ejercicio
     */
    private fun completarEjercicio() {
        timerJob?.cancel()

        val ejercicio = ejercicioActual ?: return

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

    /**
     * Limpia recursos al destruir la vista.
     *
     * - Cancela el temporizador
     * - Evita memory leaks limpiando el binding
     */
    override fun onDestroyView() {
        timerJob?.cancel()
        super.onDestroyView()
        _binding = null
    }
}