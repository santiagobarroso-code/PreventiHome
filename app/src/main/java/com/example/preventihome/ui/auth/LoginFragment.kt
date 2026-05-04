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

/**
 * Fragment encargado de la autenticación del usuario.
 *
 * Permite:
 * - Login con correo y contraseña
 * - Login con biometría (huella)
 * - Login con Google
 *
 * Utiliza:
 * - ViewModel (AuthViewModel) para la lógica
 * - StateFlow para observar estados de UI
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {

    /** Binding para acceder a las vistas del layout */
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    /** ViewModel de autenticación */
    private val viewModel: AuthViewModel by viewModels()

    /** Código de solicitud para Google Sign-In */
    private val RC_SIGN_IN = 9001

    /**
     * Infla el layout del fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Se ejecuta cuando la vista ya fue creada.
     * Inicializa observers, listeners y biometría.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObserver()
        setupClickListeners()
        checkBiometricAvailability()
    }


    /**
     * Observa los cambios en el estado de autenticación (StateFlow).
     *
     * Estados posibles:
     * - Idle
     * - Loading
     * - Success
     * - Error
     */
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


    /**
     * Configura los eventos de los botones del login.
     */
    private fun setupClickListeners() {

        /** Login con email y contraseña */
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.loginWithEmail(email, password)
        }

        /** Login con biometría */
        binding.btnBiometric.setOnClickListener {
            showBiometricPrompt()
        }

        /** Login con Google */
        binding.btnGoogle.setOnClickListener {
            launchGoogleSignIn()
        }

        /** Navegación a pantalla de registro */
        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }


    /**
     * Verifica si el dispositivo soporta biometría
     * y si hay credenciales guardadas.
     *
     * Solo muestra el botón si ambas condiciones se cumplen.
     */
    private fun checkBiometricAvailability() {
        val biometricManager = BiometricManager.from(requireContext())
        val canAuthenticate = biometricManager.canAuthenticate(BIOMETRIC_STRONG)

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS &&
            viewModel.hasSavedCredentials()) {
            binding.btnBiometric.visibility = View.VISIBLE
        }
    }

    /**
     * Muestra el prompt de autenticación biométrica.
     */
    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(requireContext())

        val callback = object : BiometricPrompt.AuthenticationCallback() {

            /** Autenticación exitosa */
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                // Usa credenciales guardadas para iniciar sesión
                viewModel.loginWithBiometrics()
            }

            /** Error en autenticación */
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Toast.makeText(requireContext(), "Error: $errString", Toast.LENGTH_SHORT).show()
                }
            }

            /** Huella no reconocida */
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


    /**
     * Inicia el flujo de autenticación con Google.
     */
    private fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(requireActivity(), gso)
        startActivityForResult(client.signInIntent, RC_SIGN_IN)
    }

    /**
     * Maneja el resultado del login con Google.
     *
     * Nota: Este método está deprecado, se recomienda usar Activity Result API.
     */
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


    /**
     * Redirige al usuario según su rol.
     */
    private fun navigateByRole(user: User) {

        // Evita que otros observers reaccionen nuevamente
        viewModel.resetState()

        val destination = when (user.rol) {
            "fisio"  -> R.id.action_loginFragment_to_fisioHomeFragment
            "admin"  -> R.id.action_loginFragment_to_adminFragment
            else     -> R.id.action_loginFragment_to_patientHomeFragment
        }

        findNavController().navigate(destination)
    }

    /**
     * Muestra estado de carga (loading)
     */
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
        binding.btnBiometric.isEnabled = false
        binding.btnGoogle.isEnabled = false
    }

    /**
     * Muestra estado normal (idle)
     */
    private fun showIdle() {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
        binding.btnBiometric.isEnabled = true
        binding.btnGoogle.isEnabled = true
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
     * Verifica si existen credenciales guardadas (para biometría)
     */
    private fun hasSavedCredentials() = viewModel.hasSavedCredentials()

    /**
     * Limpia el binding para evitar memory leaks
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}