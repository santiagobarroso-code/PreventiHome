package com.example.preventihome.ui.physio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.preventihome.databinding.ItemPacienteBinding
import com.example.preventihome.domain.model.User

class PacienteAdapter(
    private val onItemClick: (User) -> Unit
) : ListAdapter<User, PacienteAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemPacienteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            val nombre = user.nombre.ifEmpty {
                user.email.substringBefore("@")
            }
            binding.tvNombre.text = nombre
            binding.tvEmail.text = user.email
            binding.tvAvatar.text = nombre.firstOrNull()
                ?.uppercaseChar()?.toString() ?: "P"
            binding.root.setOnClickListener { onItemClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPacienteBinding.inflate(
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