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
import com.example.preventihome.R
import com.example.preventihome.databinding.FragmentPerfilFisioBinding
import com.example.preventihome.viewmodel.AuthViewModel
import com.example.preventihome.viewmodel.PerfilUiState
import com.example.preventihome.viewmodel.PerfilViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Pantalla de perfil del fisioterapeuta.
 *
 * Permite ver sus datos y actualizar su nombre.
 * No incluye cambio de contraseña — las cuentas de fisioterapeutas
 * son gestionadas por el administrador.
 *
 * Arquitectura: Fragment → PerfilViewModel → FirestoreSource
 */
@AndroidEntryPoint
class PerfilFisioFragment : Fragment() {

    private var _binding: FragmentPerfilFisioBinding? = null
    private val binding get() = _binding!!
    private val perfilViewModel: PerfilViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilFisioBinding.inflate(inflater, container, false)
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
     * Observa el estado del PerfilViewModel.
     * Muestra los datos del fisio y maneja actualizaciones.
     */
    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                perfilViewModel.uiState.collect { state ->
                    when (state) {
                        is PerfilUiState.Success -> {
                            val user = state.user
                            val nombre = user.nombre.ifEmpty {
                                user.email.substringBefore("@")
                            }
                            binding.tvAvatar.text =
                                nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "F"
                            binding.tvNombrePerfil.text = nombre
                            binding.tvNombreData.text = nombre
                            binding.tvEmailData.text = user.email
                            binding.etNuevoNombre.setText(user.nombre)
                        }
                        is PerfilUiState.ActualizacionExitosa -> {
                            Toast.makeText(
                                requireContext(),
                                "Nombre actualizado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is PerfilUiState.Error -> {
                            Toast.makeText(
                                requireContext(),
                                state.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    /**
     * Configura el botón de actualizar nombre y el de cerrar sesión.
     */
    private fun setupClickListeners() {
        binding.btnActualizarNombre.setOnClickListener {
            val nuevoNombre = binding.etNuevoNombre.text.toString().trim()
            perfilViewModel.actualizarNombre(nuevoNombre)
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_perfilFisio_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}