package com.example.preventihome.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.preventihome.R
import com.example.preventihome.databinding.ItemUsuarioAdminBinding
import com.example.preventihome.domain.model.User

/**
 * Adapter para la lista de usuarios en el panel de administrador.
 * Muestra cada usuario con su rol y un botón de acción contextual:
 * - Paciente → botón "Hacer fisio"
 * - Fisio    → botón "Revocar"
 * - Admin    → sin botón de acción
 *
 * @param onPromover Callback cuando el admin promueve a un paciente a fisio
 * @param onRevocar  Callback cuando el admin revoca el rol de fisio
 */
class UsuarioAdminAdapter(
    private val onPromover: (User) -> Unit,
    private val onRevocar: (User) -> Unit
) : ListAdapter<User, UsuarioAdminAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemUsuarioAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos del usuario con las vistas del item.
         * Configura el badge de rol con color diferenciado y
         * el botón de acción según el rol actual del usuario.
         */
        fun bind(user: User) {
            // Mostrar nombre o parte del correo si no hay nombre
            val nombre = user.nombre.ifEmpty {
                user.email.substringBefore("@")
            }
            binding.tvNombre.text = nombre
            binding.tvEmail.text = user.email

            // Inicial para el avatar circular
            binding.tvAvatar.text = nombre
                .firstOrNull()?.uppercaseChar()?.toString() ?: "U"

            // Configurar badge de rol con color diferenciado
            binding.tvRol.text = when (user.rol) {
                "fisio"  -> "Fisioterapeuta"
                "admin"  -> "Administrador"
                else     -> "Paciente"
            }
            binding.tvRol.backgroundTintList =
                binding.root.context.getColorStateList(
                    when (user.rol) {
                        "fisio"  -> R.color.primary
                        "admin"  -> R.color.secondary
                        else     -> R.color.text_secondary
                    }
                )

            // Configurar botón de acción según el rol
            when (user.rol) {
                "paciente" -> {
                    binding.btnAccion.text = "Hacer fisio"
                    binding.btnAccion.visibility = ViewGroup.VISIBLE
                    binding.btnAccion.setOnClickListener { onPromover(user) }
                }
                "fisio" -> {
                    binding.btnAccion.text = "Revocar"
                    binding.btnAccion.visibility = ViewGroup.VISIBLE
                    binding.btnAccion.setOnClickListener { onRevocar(user) }
                }
                else -> {
                    // Admin no tiene botón de acción
                    binding.btnAccion.visibility = ViewGroup.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUsuarioAdminBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffCallback para actualizaciones eficientes del RecyclerView.
     * Compara por UID para identificar items y por contenido completo
     * para detectar cambios de rol.
     */
    class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(old: User, new: User) = old.uid == new.uid
        override fun areContentsTheSame(old: User, new: User) = old == new
    }
}