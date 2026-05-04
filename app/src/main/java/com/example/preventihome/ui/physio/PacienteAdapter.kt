package com.example.preventihome.ui.physio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.preventihome.databinding.ItemPacienteBinding
import com.example.preventihome.domain.model.User

/**
 * Adapter para mostrar la lista de pacientes en el módulo de fisioterapeuta.
 *
 * Utiliza ListAdapter con DiffUtil para optimizar actualizaciones
 * y evitar recargar toda la lista innecesariamente.
 *
 * @param onItemClick Callback que se ejecuta al seleccionar un paciente
 */
class PacienteAdapter(
    private val onItemClick: (User) -> Unit
) : ListAdapter<User, PacienteAdapter.ViewHolder>(DiffCallback()) {

    /**
     * ViewHolder que representa cada paciente en la lista.
     */
    inner class ViewHolder(private val binding: ItemPacienteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos del usuario (paciente) con la UI.
         *
         * @param user Objeto User que representa al paciente
         */
        fun bind(user: User) {

            /**
             * Determina el nombre a mostrar:
             * - Usa el nombre si existe
             * - Si no, usa la parte del email antes del "@"
             */
            val nombre = user.nombre.ifEmpty {
                user.email.substringBefore("@")
            }

            // Nombre del paciente
            binding.tvNombre.text = nombre

            // Email del paciente
            binding.tvEmail.text = user.email

            /**
             * Avatar simple:
             * - Primera letra del nombre en mayúscula
             * - Si no existe, usa "P" como fallback
             */
            binding.tvAvatar.text = nombre.firstOrNull()
                ?.uppercaseChar()?.toString() ?: "P"

            // Click en el item
            binding.root.setOnClickListener { onItemClick(user) }
        }
    }

    /**
     * Crea un nuevo ViewHolder inflando el layout correspondiente.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPacienteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * Vincula cada elemento de la lista con su ViewHolder.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffUtil para optimizar cambios en la lista.
     */
    class DiffCallback : DiffUtil.ItemCallback<User>() {

        /**
         * Verifica si dos elementos representan el mismo usuario
         * comparando su UID.
         */
        override fun areItemsTheSame(old: User, new: User) =
            old.uid == new.uid

        /**
         * Verifica si el contenido de los elementos es igual.
         */
        override fun areContentsTheSame(old: User, new: User) =
            old == new
    }
}