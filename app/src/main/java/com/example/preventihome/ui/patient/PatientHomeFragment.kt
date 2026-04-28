package com.example.preventihome.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.preventihome.R
import com.example.preventihome.databinding.FragmentPatientHomeBinding
import com.example.preventihome.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PatientHomeFragment : Fragment() {

    private var _binding: FragmentPatientHomeBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupToolbar()
    }

    private fun setupToolbar() {
        // Logout se maneja desde la tarjeta de perfil
        // El toolbar solo muestra el título por ahora
    }



    private fun setupClickListeners() {
        binding.cardEjercicios.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_ejercicios)
        }
        binding.cardHistorial.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_historial)
        }
        binding.cardPerfil.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_perfil)
        }
        binding.cardConsultas.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_consultas)
        }
        binding.cardCitas.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_misCitas)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}