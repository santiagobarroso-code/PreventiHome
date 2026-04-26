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
import com.example.preventihome.R
import com.example.preventihome.databinding.FragmentPerfilBinding
import com.example.preventihome.viewmodel.AuthUiState
import com.example.preventihome.viewmodel.AuthViewModel
import com.example.preventihome.viewmodel.PerfilUiState
import com.example.preventihome.viewmodel.PerfilViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        setupObservers()
        setupClickListeners()
        perfilViewModel.cargarPerfil()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                perfilViewModel.uiState.collect { state ->
                    when (state) {
                        is PerfilUiState.Success -> {
                            val user = state.user
                            val inicial = user.nombre.firstOrNull()?.uppercaseChar()
                                ?: user.email.firstOrNull()?.uppercaseChar()
                                ?: "U"
                            binding.tvAvatar.text = inicial.toString()
                            binding.tvNombrePerfil.text = when {
                                user.nombre.isNotEmpty() -> user.nombre
                                else -> user.email.substringBefore("@")
                            }
                            binding.tvRolBadge.text = when (user.rol) {
                                "fisio"  -> "Fisioterapeuta"
                                "admin"  -> "Administrador"
                                else     -> "Paciente"
                            }
                            binding.tvNombreData.text = when {
                                user.nombre.isNotEmpty() -> user.nombre
                                else -> user.email.substringBefore("@")
                            }
                            binding.tvEmailData.text = user.email
                            binding.tvRolData.text = user.rol.replaceFirstChar { it.uppercaseChar() }
                        }
                        is PerfilUiState.Error -> {
                            binding.tvNombrePerfil.text = "Error al cargar"
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
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