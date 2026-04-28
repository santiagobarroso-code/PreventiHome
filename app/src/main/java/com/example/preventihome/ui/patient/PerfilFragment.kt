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
import com.example.preventihome.R
import com.example.preventihome.databinding.FragmentPerfilBinding
import com.example.preventihome.viewmodel.AuthViewModel
import com.example.preventihome.viewmodel.PerfilUiState
import com.example.preventihome.viewmodel.PerfilViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Pantalla de perfil del paciente.
 *
 * Funcionalidades:
 * - Ver datos del perfil (nombre, correo, rol)
 * - Actualizar nombre (se guarda en Firestore)
 * - Cambiar contraseña (se actualiza en Firebase Auth)
 * - Cerrar sesión
 *
 * Solo disponible para usuarios con rol "paciente".
 * Los fisioterapeutas tienen su propio flujo de perfil.
 */
@AndroidEntryPoint
class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private val perfilViewModel: PerfilViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupObserver()
        setupClickListeners()
        perfilViewModel.cargarPerfil()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Observa el StateFlow del PerfilViewModel y actualiza la UI.
     * Maneja carga, éxito, actualización exitosa y errores.
     */
    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                perfilViewModel.uiState.collect { state ->
                    when (state) {
                        is PerfilUiState.Loading -> {
                            // Mostrar indicador de carga si es necesario
                        }
                        is PerfilUiState.Success -> {
                            val user = state.user
                            val nombre = user.nombre.ifEmpty {
                                user.email.substringBefore("@")
                            }
                            // Avatar con inicial
                            binding.tvAvatar.text =
                                nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
                            binding.tvNombrePerfil.text = nombre
                            binding.tvRolBadge.text = when (user.rol) {
                                "fisio"  -> "Fisioterapeuta"
                                "admin"  -> "Administrador"
                                else     -> "Paciente"
                            }
                            binding.tvNombreData.text = nombre
                            binding.tvEmailData.text = user.email
                            binding.tvRolData.text =
                                user.rol.replaceFirstChar { it.uppercaseChar() }

                            // Pre-llenar el campo de nombre con el actual
                            binding.etNuevoNombre.setText(user.nombre)
                        }
                        is PerfilUiState.ActualizacionExitosa -> {
                            Toast.makeText(
                                requireContext(),
                                "Datos actualizados correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.etPasswordActual.setText("")
                            binding.etNuevaPassword.setText("")
                            binding.etConfirmarPassword.setText("")
                        }
                        is PerfilUiState.Error -> {
                            Toast.makeText(
                                requireContext(),
                                state.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Configura todos los botones de acción del perfil.
     */
    private fun setupClickListeners() {
        // Actualizar nombre en Firestore
        binding.btnActualizarNombre.setOnClickListener {
            val nuevoNombre = binding.etNuevoNombre.text.toString().trim()
            perfilViewModel.actualizarNombre(nuevoNombre)
        }

        // Cambiar contraseña en Firebase Auth
        binding.btnCambiarPassword.setOnClickListener {
            val passwordActual    = binding.etPasswordActual.text.toString()
            val nuevaPassword     = binding.etNuevaPassword.text.toString()
            val confirmarPassword = binding.etConfirmarPassword.text.toString()
            perfilViewModel.cambiarPassword(passwordActual, nuevaPassword, confirmarPassword)
        }

        // Cerrar sesión y regresar al login
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_perfil_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}