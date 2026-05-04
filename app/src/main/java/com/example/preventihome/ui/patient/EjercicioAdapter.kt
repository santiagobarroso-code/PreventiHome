package com.example.preventihome.ui.patient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.preventihome.databinding.ItemEjercicioBinding
import com.example.preventihome.domain.model.Ejercicio

/**
 * Adapter para mostrar una lista de ejercicios en un RecyclerView.
 *
 * Usa ListAdapter + DiffUtil para manejar cambios de forma eficiente.
 *
 * @param onItemClick Callback que se ejecuta al hacer click en un ejercicio
 */
class EjercicioAdapter(
    private val onItemClick: (Ejercicio) -> Unit
) : ListAdapter<Ejercicio, EjercicioAdapter.ViewHolder>(DiffCallback()) {

    /**
     * ViewHolder que representa cada item (ejercicio) en la lista.
     */
    inner class ViewHolder(private val binding: ItemEjercicioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de un ejercicio con las vistas del layout.
         *
         * @param ejercicio Objeto Ejercicio a mostrar
         */
        fun bind(ejercicio: Ejercicio) {

            // Asignación de datos básicos
            binding.tvNombre.text = ejercicio.nombre
            binding.tvDescripcion.text = ejercicio.descripcion

            // Capitaliza la primera letra de la zona
            binding.tvZona.text = ejercicio.zona.replaceFirstChar { it.uppercase() }

            // Muestra formato: "X series × Y reps"
            binding.tvSeriesReps.text = "${ejercicio.series} series × ${ejercicio.repeticiones} reps"

            /**
             * Muestra u oculta el badge de "recomendado"
             * según la propiedad chefRecomendado
             */
            binding.tvRecomendado.visibility =
                if (ejercicio.chefRecomendado) View.VISIBLE else View.GONE

            /**
             * Click en botón de detalle
             */
            binding.btnVerDetalle.setOnClickListener { onItemClick(ejercicio) }

            /**
             * Click en todo el item
             */
            binding.root.setOnClickListener { onItemClick(ejercicio) }
        }
    }

    /**
     * Crea un nuevo ViewHolder inflando el layout del item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEjercicioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    /**
     * Vincula un ViewHolder con los datos en la posición dada.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffUtil para optimizar cambios en la lista.
     *
     * Permite actualizar solo los elementos que cambian,
     * evitando recargar toda la lista.
     */
    class DiffCallback : DiffUtil.ItemCallback<Ejercicio>() {

        /**
         * Verifica si dos elementos representan el mismo item
         * comparando sus IDs.
         */
        override fun areItemsTheSame(old: Ejercicio, new: Ejercicio) =
            old.id == new.id

        /**
         * Verifica si el contenido de los elementos es igual.
         */
        override fun areContentsTheSame(old: Ejercicio, new: Ejercicio) =
            old == new
    }
}