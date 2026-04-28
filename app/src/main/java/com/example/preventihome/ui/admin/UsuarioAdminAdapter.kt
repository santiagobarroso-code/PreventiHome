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
 *
 * Muestra cada usuario con su información y un botón de eliminar.
 * Los admins no tienen botón de eliminar para evitar eliminar la cuenta propia.
 *
 * @param onEliminar Callback cuando el admin quiere eliminar un usuario
 */
class UsuarioAdminAdapter(
    private val onEliminar: (User) -> Unit
) : ListAdapter<User, UsuarioAdminAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemUsuarioAdminBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos del usuario con las vistas.
         * Configura el badge de rol con color diferenciado.
         * Solo pacientes y fisios tienen botón de eliminar.
         */
        fun bind(user: User) {
            val nombre = user.nombre.ifEmpty {
                user.email.substringBefore("@")
            }
            binding.tvNombre.text = nombre
            binding.tvEmail.text = user.email
            binding.tvAvatar.text =
                nombre.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

            // Badge de rol con color diferenciado por tipo
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

            // Solo pacientes y fisios se pueden eliminar
            // Los admins no tienen botón de eliminar
            if (user.rol == "admin") {
                binding.btnAccion.visibility = ViewGroup.GONE
            } else {
                binding.btnAccion.text = "Eliminar"
                binding.btnAccion.visibility = ViewGroup.VISIBLE
                binding.btnAccion.setOnClickListener { onEliminar(user) }
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

    class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(old: User, new: User) = old.uid == new.uid
        override fun areContentsTheSame(old: User, new: User) = old == new
    }
}