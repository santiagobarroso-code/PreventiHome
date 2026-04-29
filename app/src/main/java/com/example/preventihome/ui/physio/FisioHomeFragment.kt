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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.preventihome.R
import com.example.preventihome.databinding.FragmentFisioHomeBinding
import com.example.preventihome.viewmodel.AuthViewModel
import com.example.preventihome.viewmodel.FisioUiState
import com.example.preventihome.viewmodel.FisioViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.os.bundleOf

@AndroidEntryPoint
class FisioHomeFragment : Fragment() {

    private var _binding: FragmentFisioHomeBinding? = null
    private val binding get() = _binding!!
    private val fisioViewModel: FisioViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var adapter: PacienteAdapter

    @Inject lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFisioHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupObserver()
        setupClickListeners()
        mostrarNombreFisio()
    }

    private fun setupToolbar() {
        binding.toolbar.subtitle = auth.currentUser?.email ?: ""
    }

    /**
     * Muestra el nombre del fisio en el header.
     * Prioriza el nombre del perfil de Firestore sobre el email.
     */
    private fun mostrarNombreFisio() {
        viewLifecycleOwner.lifecycleScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            try {
                val user = com.example.preventihome.data.remote.FirestoreSource(
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                ).getUser(uid)
                val nombreMostrar = when {
                    user.nombre.isNotEmpty() -> user.nombre
                    else -> auth.currentUser?.email?.substringBefore("@") ?: "Fisioterapeuta"
                }
                binding.tvBienvenidaFisio.text = "Hola, $nombreMostrar"
            } catch (e: Exception) {
                val email = auth.currentUser?.email ?: ""
                binding.tvBienvenidaFisio.text = "Hola, ${email.substringBefore("@")}"
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = PacienteAdapter { paciente ->
            val nombre = paciente.nombre.ifEmpty {
                paciente.email.substringBefore("@")
            }
            findNavController().navigate(
                R.id.action_fisioHome_to_historialPaciente,
                bundleOf(
                    "pacienteId"     to paciente.uid,
                    "pacienteNombre" to nombre,
                    "pacienteEmail"  to paciente.email
                )
            )
        }
        binding.rvPacientes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPacientes.adapter = adapter
    }

    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                fisioViewModel.uiState.collect { state ->
                    when (state) {
                        is FisioUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.tvEmpty.visibility = View.GONE
                        }
                        is FisioUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            val pacientes = state.pacientes
                            binding.tvContadorPacientes.text =
                                "${pacientes.size} paciente(s) registrado(s)"
                            if (pacientes.isEmpty()) {
                                binding.tvEmpty.visibility = View.VISIBLE
                                binding.rvPacientes.visibility = View.GONE
                            } else {
                                binding.tvEmpty.visibility = View.GONE
                                binding.rvPacientes.visibility = View.VISIBLE
                                adapter.submitList(pacientes)
                            }
                        }
                        is FisioUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.tvEmpty.text = state.message
                            binding.tvEmpty.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_fisioHome_to_login)
        }
        binding.btnVerCitas.setOnClickListener {
            findNavController().navigate(R.id.action_fisioHome_to_citasPendientes)
        }
        binding.btnPerfil.setOnClickListener {
            findNavController().navigate(R.id.action_fisioHome_to_perfilFisio)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}