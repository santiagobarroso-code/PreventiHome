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

/**
 * Adapter para mostrar el historial de progreso del usuario en un RecyclerView.
 *
 * Utiliza ListAdapter junto con DiffUtil para optimizar la actualización
 * de los elementos en la lista.
 */
class ProgresoAdapter : ListAdapter<Progreso, ProgresoAdapter.ViewHolder>(DiffCallback()) {

    /**
     * ViewHolder que representa cada elemento del historial.
     */
    inner class ViewHolder(private val binding: ItemProgresoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de un progreso con las vistas del layout.
         *
         * @param progreso Objeto Progreso a mostrar
         */
        fun bind(progreso: Progreso) {

            // Nombre del ejercicio realizado
            binding.tvNombre.text = progreso.ejercicioNombre

            /**
             * Muestra un emoji dependiendo de la evaluación:
             * - 👍 bien
             * - 😐 regular
             * - 👎 mal
             */
            binding.tvEmoji.text = when (progreso.evaluacion) {
                "bien"    -> "👍"
                "regular" -> "😐"
                "mal"     -> "👎"
                else      -> "✓"
            }

            /**
             * Formatea la fecha a un formato legible en español (México).
             * Ejemplo: 29 abr 2026, 14:30
             */
            val formato = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "MX"))
            binding.tvFecha.text = formato.format(Date(progreso.fecha))

            /**
             * Muestra detalle del progreso:
             * - Zona del cuerpo (capitalizada)
             * - Duración en formato mm:ss
             */
            val min = progreso.duracionSegundos / 60
            val seg = progreso.duracionSegundos % 60

            binding.tvDetalle.text =
                "${progreso.zona.replaceFirstChar { it.uppercase() }} · %02d:%02d min".format(min, seg)
        }
    }

    /**
     * Crea un nuevo ViewHolder inflando el layout del item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProgresoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * Vincula el ViewHolder con el elemento en la posición actual.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffUtil para optimizar cambios en la lista.
     */
    class DiffCallback : DiffUtil.ItemCallback<Progreso>() {

        /**
         * Verifica si dos elementos representan el mismo progreso
         * comparando su ID.
         */
        override fun areItemsTheSame(old: Progreso, new: Progreso) =
            old.id == new.id

        /**
         * Verifica si el contenido de los elementos es igual.
         */
        override fun areContentsTheSame(old: Progreso, new: Progreso) =
            old == new
    }
}