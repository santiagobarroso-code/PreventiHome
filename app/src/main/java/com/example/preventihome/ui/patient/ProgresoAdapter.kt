package com.example.preventihome.ui.patient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.preventihome.databinding.ItemProgresoBinding
import com.example.preventihome.domain.model.Progreso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProgresoAdapter : ListAdapter<Progreso, ProgresoAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemProgresoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(progreso: Progreso) {
            binding.tvNombre.text = progreso.ejercicioNombre

            // Emoji según evaluación
            binding.tvEmoji.text = when (progreso.evaluacion) {
                "bien"    -> "👍"
                "regular" -> "😐"
                "mal"     -> "👎"
                else      -> "✓"
            }

            // Formato de fecha
            val formato = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "MX"))
            binding.tvFecha.text = formato.format(Date(progreso.fecha))

            // Duración y zona
            val min = progreso.duracionSegundos / 60
            val seg = progreso.duracionSegundos % 60
            binding.tvDetalle.text =
                "${progreso.zona.replaceFirstChar { it.uppercase() }} · %02d:%02d min".format(min, seg)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProgresoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Progreso>() {
        override fun areItemsTheSame(old: Progreso, new: Progreso) = old.id == new.id
        override fun areContentsTheSame(old: Progreso, new: Progreso) = old == new
    }
}