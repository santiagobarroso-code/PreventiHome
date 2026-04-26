package com.example.preventihome.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.preventihome.R
import com.example.preventihome.databinding.FragmentLoginBinding
import com.example.preventihome.domain.model.User
import com.example.preventihome.viewmodel.AuthUiState
import com.example.preventihome.viewmodel.AuthViewModel

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    // Código para Google Sign-In (Activity Result API)
    private val RC_SIGN_IN = 9001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObserver()
        setupClickListeners()
        checkBiometricAvailability()
    }

    // ── Observa el StateFlow del ViewModel ─────────────────────────────────
    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AuthUiState.Idle -> showIdle()
                        is AuthUiState.Loading -> showLoading()
                        is AuthUiState.Success -> navigateByRole(state.user)
                        is AuthUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    // ── Listeners de botones ────────────────────────────────────────────────
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.loginWithEmail(email, password)
        }

        binding.btnBiometric.setOnClickListener {
            showBiometricPrompt()
        }

        binding.btnGoogle.setOnClickListener {
            launchGoogleSignIn()
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    // ── Biometría ───────────────────────────────────────────────────────────
    private fun checkBiometricAvailability() {
        val biometricManager = BiometricManager.from(requireContext())
        val canAuthenticate = biometricManager.canAuthenticate(BIOMETRIC_STRONG)

        // Mostrar botón de huella solo si el dispositivo la soporta
        // Y si hay credenciales guardadas para usar
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS &&
            viewModel.hasSavedCredentials()) {
            binding.btnBiometric.visibility = View.VISIBLE
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(requireContext())

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                // Biometría exitosa → usar credenciales guardadas para Firebase
                viewModel.loginWithBiometrics()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Solo mostrar error si no fue cancelación del usuario
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Toast.makeText(requireContext(), "Error: $errString", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onAuthenticationFailed() {
                Toast.makeText(requireContext(), "Huella no reconocida", Toast.LENGTH_SHORT).show()
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("PreventiHome")
            .setSubtitle("Usa tu huella para entrar")
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        BiometricPrompt(this, executor, callback).authenticate(promptInfo)
    }

    // ── Google Sign-In ──────────────────────────────────────────────────────
    private fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(requireActivity(), gso)
        startActivityForResult(client.signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Usar Activity Result API en versión final")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)
                account.idToken?.let { viewModel.loginWithGoogle(it) }
            } catch (e: ApiException) {
                showError("Error con Google: ${e.message}")
            }
        }
    }

    // ── Navegación según rol ─────────────────────────────────────────────────
    private fun navigateByRole(user: User) {
        viewModel.resetState()  // ← esto evita que MainActivity reaccione también
        val destination = when (user.rol) {
            "fisio"  -> R.id.action_loginFragment_to_fisioHomeFragment
            "admin"  -> R.id.action_loginFragment_to_adminFragment
            else     -> R.id.action_loginFragment_to_patientHomeFragment
        }
        findNavController().navigate(destination)
    }

    // ── Estados de UI ────────────────────────────────────────────────────────
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
        binding.btnBiometric.isEnabled = false
        binding.btnGoogle.isEnabled = false
    }

    private fun showIdle() {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
        binding.btnBiometric.isEnabled = true
        binding.btnGoogle.isEnabled = true
    }

    private fun showError(message: String) {
        showIdle()
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        viewModel.resetState()
    }

    private fun hasSavedCredentials() = viewModel.hasSavedCredentials()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null   // evitar memory leak
    }
}