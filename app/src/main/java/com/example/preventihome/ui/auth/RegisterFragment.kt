package com.example.preventihome.ui.auth

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
import com.example.preventihome.databinding.FragmentRegisterBinding
import com.example.preventihome.viewmodel.AuthUiState
import com.example.preventihome.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment encargado del registro de nuevos usuarios.
 *
 * Permite:
 * - Crear cuenta con nombre, correo y contraseña
 * - Validar restricciones de dominio
 * - Navegar al home tras registro exitoso
 */
@AndroidEntryPoint
class RegisterFragment : Fragment() {

    /** Binding para acceder a las vistas del layout */
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    /** ViewModel de autenticación */
    private val viewModel: AuthViewModel by viewModels()

    /**
     * Infla el layout del fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Se ejecuta cuando la vista ha sido creada.
     * Inicializa observers y listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObserver()
        setupClickListeners()
    }

    /**
     * Observa el estado de autenticación desde el ViewModel.
     *
     * Maneja:
     * - Estados de carga
     * - Éxito en el registro
     * - Errores
     */
    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AuthUiState.Idle -> showIdle()
                        is AuthUiState.Loading -> showLoading()

                        is AuthUiState.Success -> {
                            // Registro exitoso → redirigir al home del paciente
                            findNavController().navigate(
                                R.id.action_registerFragment_to_patientHomeFragment
                            )
                            viewModel.resetState()
                        }

                        is AuthUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    /**
     * Configura los eventos de los botones.
     */
    private fun setupClickListeners() {

        /** Botón de registro */
        binding.btnRegister.setOnClickListener {

            val nombre   = binding.etNombre.text.toString().trim()
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            /**
             * Validación de dominios restringidos.
             *
             * Los correos con dominio:
             * - @fisio.preventihome.com
             * - @admin.preventihome.com
             *
             * Solo pueden ser creados por un administrador.
             */
            if (email.endsWith("@fisio.preventihome.com") ||
                email.endsWith("@admin.preventihome.com")) {

                Toast.makeText(
                    requireContext(),
                    "Ese dominio es de uso exclusivo del administrador. " +
                            "Contacta al administrador para crear tu cuenta.",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            // Llamar al ViewModel para registrar al usuario
            viewModel.register(email, password, nombre)
        }

        /** Navegar de regreso al login */
        binding.tvLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Muestra estado de carga (loading)
     */
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false
    }

    /**
     * Muestra estado normal (idle)
     */
    private fun showIdle() {
        binding.progressBar.visibility = View.GONE
        binding.btnRegister.isEnabled = true
    }

    /**
     * Muestra un error en pantalla
     */
    private fun showError(message: String) {
        showIdle()
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        viewModel.resetState()
    }

    /**
     * Limpia el binding para evitar memory leaks
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}