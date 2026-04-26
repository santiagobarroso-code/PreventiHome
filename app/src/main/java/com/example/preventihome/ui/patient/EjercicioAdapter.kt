package com.example.preventihome.ui.patient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.preventihome.databinding.ItemEjercicioBinding
import com.example.preventihome.domain.model.Ejercicio

class EjercicioAdapter(
    private val onItemClick: (Ejercicio) -> Unit
) : ListAdapter<Ejercicio, EjercicioAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemEjercicioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ejercicio: Ejercicio) {
            binding.tvNombre.text = ejercicio.nombre
            binding.tvDescripcion.text = ejercicio.descripcion
            binding.tvZona.text = ejercicio.zona.replaceFirstChar { it.uppercase() }
            binding.tvSeriesReps.text = "${ejercicio.series} series × ${ejercicio.repeticiones} reps"

            // Mostrar badge recomendado si aplica
            binding.tvRecomendado.visibility =
                if (ejercicio.chefRecomendado) View.VISIBLE else View.GONE

            binding.btnVerDetalle.setOnClickListener { onItemClick(ejercicio) }
            binding.root.setOnClickListener { onItemClick(ejercicio) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEjercicioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Ejercicio>() {
        override fun areItemsTheSame(old: Ejercicio, new: Ejercicio) = old.id == new.id
        override fun areContentsTheSame(old: Ejercicio, new: Ejercicio) = old == new
    }
}