package com.example.preventihome.ui.patient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.preventihome.R
import com.example.preventihome.databinding.ItemCitaBinding
import com.example.preventihome.domain.model.Cita
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter para mostrar listas de citas.
 * Reutilizado tanto en el paciente como en el fisioterapeuta.
 *
 * @param mostrarPaciente Si es true muestra el nombre del paciente
 *                        (útil para la vista del fisioterapeuta)
 * @param onItemClick     Callback al tocar una cita
 */
class CitaAdapter(
    private val mostrarPaciente: Boolean = false,
    private val onItemClick: (Cita) -> Unit
) : ListAdapter<Cita, CitaAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemCitaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de la cita con las vistas.
         * El badge de estado cambia de color según el estado actual.
         */
        fun bind(cita: Cita) {
            // Formato de fecha legible en español
            val formato = SimpleDateFormat(
                "EEEE dd 'de' MMMM, HH:mm 'hrs'",
                Locale("es", "MX")
            )
            binding.tvFechaCita.text = formato.format(Date(cita.fechaCita))
            binding.tvMotivoCita.text = cita.motivo

            // Mostrar nombre del paciente solo en vista del fisio
            if (mostrarPaciente && cita.pacienteNombre.isNotEmpty()) {
                binding.tvPacienteCita.text = cita.pacienteNombre
                binding.tvPacienteCita.visibility = ViewGroup.VISIBLE
            }

            // Badge de estado con color diferenciado
            binding.tvEstadoCita.text = when (cita.estado) {
                "pendiente" -> "Pendiente"
                "atendida"  -> "Atendida"
                "cancelada" -> "Cancelada"
                else        -> cita.estado
            }
            binding.tvEstadoCita.backgroundTintList =
                binding.root.context.getColorStateList(
                    when (cita.estado) {
                        "pendiente" -> R.color.secondary
                        "atendida"  -> R.color.primary
                        else        -> R.color.text_secondary
                    }
                )

            binding.root.setOnClickListener { onItemClick(cita) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCitaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Cita>() {
        override fun areItemsTheSame(old: Cita, new: Cita) = old.id == new.id
        override fun areContentsTheSame(old: Cita, new: Cita) = old == new
    }
}