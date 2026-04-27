package com.example.preventihome.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.preventihome.R
import com.example.preventihome.databinding.FragmentAdminBinding
import com.example.preventihome.domain.model.User
import com.example.preventihome.viewmodel.AdminUiState
import com.example.preventihome.viewmodel.AdminViewModel
import com.example.preventihome.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Panel de administración de la plataforma PreventiHome Physio.
 *
 * Funcionalidades:
 * - Ver todos los usuarios registrados con sus roles
 * - Promover pacientes a fisioterapeutas
 * - Revocar el rol de fisioterapeuta
 * - Crear nuevos usuarios (paciente o fisioterapeuta)
 * - Estadísticas rápidas del sistema
 *
 * Arquitectura: Fragment → AdminViewModel → UserRepository → FirestoreSource
 */
@AndroidEntryPoint
class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    /** ViewModel principal del panel admin */
    private val adminViewModel: AdminViewModel by viewModels()

    /** ViewModel de auth para manejar el logout */
    private val authViewModel: AuthViewModel by viewModels()

    /** Adapter del RecyclerView de usuarios */
    private lateinit var adapter: UsuarioAdminAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupObserver()
        setupClickListeners()
    }

    /**
     * Configura el toolbar con el menú de logout.
     */
    /**
     * Configura el toolbar — solo muestra el título.
     * El logout se maneja desde el botón dedicado.
     */
    private fun setupToolbar() {
        // Toolbar solo decorativo en el panel admin
    }

    /**
     * Inicializa el RecyclerView con el adapter y sus callbacks.
     * - onPromover: muestra confirmación antes de cambiar rol
     * - onRevocar: muestra confirmación antes de revocar
     */
    private fun setupRecyclerView() {
        adapter = UsuarioAdminAdapter(
            onPromover = { user -> confirmarPromover(user) },
            onRevocar  = { user -> confirmarRevocar(user) }
        )
        binding.rvUsuarios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsuarios.adapter = adapter
    }

    /**
     * Observa el StateFlow del AdminViewModel y actualiza la UI
     * según cada estado posible.
     */
    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adminViewModel.uiState.collect { state ->
                    when (state) {
                        is AdminUiState.Loading -> mostrarCargando()
                        is AdminUiState.Success -> mostrarUsuarios(state.usuarios)
                        is AdminUiState.OperacionExitosa -> {
                            Toast.makeText(
                                requireContext(),
                                "Operación realizada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is AdminUiState.Error -> {
                            ocultarCargando()
                            Toast.makeText(
                                requireContext(),
                                state.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> ocultarCargando()
                    }
                }
            }
        }
    }

    /**
     * Configura los listeners de botones principales.
     * El FAB abre el diálogo para crear un nuevo usuario.
     */
    /**
     * Configura los listeners de botones principales.
     * - FAB: abre diálogo para crear nuevo usuario
     * - btnLogout: cierra sesión y regresa al login
     */
    private fun setupClickListeners() {
        binding.fabNuevoUsuario.setOnClickListener {
            mostrarDialogoNuevoUsuario()
        }
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_admin_to_login)
        }
    }

    /**
     * Actualiza el RecyclerView y los contadores de estadísticas.
     * @param usuarios Lista completa de usuarios de la plataforma
     */
    private fun mostrarUsuarios(usuarios: List<User>) {
        ocultarCargando()

        // Actualizar contadores del header
        binding.tvCountFisios.text =
            usuarios.count { it.rol == "fisio" }.toString()
        binding.tvCountPacientes.text =
            usuarios.count { it.rol == "paciente" }.toString()
        binding.tvCountTotal.text = usuarios.size.toString()

        if (usuarios.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvUsuarios.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvUsuarios.visibility = View.VISIBLE
            adapter.submitList(usuarios)
        }
    }

    private fun mostrarCargando() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
    }

    private fun ocultarCargando() {
        binding.progressBar.visibility = View.GONE
    }

    /**
     * Muestra un diálogo de confirmación antes de promover a un usuario.
     * @param user Usuario a promover a fisioterapeuta
     */
    private fun confirmarPromover(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hacer fisioterapeuta")
            .setMessage(
                "¿Deseas promover a ${user.email} al rol de fisioterapeuta? " +
                        "Tendrá acceso al panel de fisio."
            )
            .setPositiveButton("Confirmar") { _, _ ->
                adminViewModel.promoverAFisio(user.uid)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Muestra un diálogo de confirmación antes de revocar el rol de fisio.
     * @param user Fisioterapeuta a degradar a paciente
     */
    private fun confirmarRevocar(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Revocar rol de fisioterapeuta")
            .setMessage(
                "¿Deseas revocar el acceso de fisioterapeuta a ${user.email}? " +
                        "Pasará a ser paciente."
            )
            .setPositiveButton("Confirmar") { _, _ ->
                adminViewModel.revocarFisio(user.uid)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Muestra un diálogo para crear un nuevo usuario desde el panel admin.
     * El admin especifica nombre, correo, contraseña y rol.
     * La contraseña es temporal — el usuario debería cambiarla después.
     */
    private fun mostrarDialogoNuevoUsuario() {
        // Inflar layout personalizado del diálogo
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_nuevo_usuario, null)

        val etNombre   = dialogView.findViewById<EditText>(R.id.etNombreDialog)
        val etEmail    = dialogView.findViewById<EditText>(R.id.etEmailDialog)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPasswordDialog)
        val spinnerRol = dialogView.findViewById<Spinner>(R.id.spinnerRol)

        // Configurar opciones del spinner de rol
        val roles = listOf("paciente", "fisio")
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            roles
        )
        spinnerRol.adapter = spinnerAdapter

        AlertDialog.Builder(requireContext())
            .setTitle("Crear nuevo usuario")
            .setView(dialogView)
            .setPositiveButton("Crear") { _, _ ->
                val nombre   = etNombre.text.toString().trim()
                val email    = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val rol      = spinnerRol.selectedItem.toString()

                // Validar campos antes de crear
                if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Todos los campos son obligatorios",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                crearNuevoUsuario(nombre, email, password, rol)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Llama al ViewModel para crear el usuario en Firebase Auth y Firestore.
     * Muestra feedback al admin sobre el resultado de la operación.
     */
    private fun crearNuevoUsuario(
        nombre: String,
        email: String,
        password: String,
        rol: String
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            adminViewModel.crearUsuario(nombre, email, password, rol)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}