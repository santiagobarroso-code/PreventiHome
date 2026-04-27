package com.example.preventihome.ui.physio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.preventihome.R
import com.example.preventihome.databinding.ItemConsultaBinding
import com.example.preventihome.domain.model.Consulta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter para mostrar listas de consultas.
 * Reutilizado tanto en el panel del fisioterapeuta
 * como en la pantalla de consultas del paciente.
 *
 * @param onItemClick Callback al tocar una consulta para ver el detalle
 */
class ConsultaAdapter(
    private val onItemClick: (Consulta) -> Unit
) : ListAdapter<Consulta, ConsultaAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemConsultaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de la consulta con las vistas.
         * El badge de estado cambia de color según si está activa o cerrada.
         * Las notas solo se muestran si existen.
         */
        fun bind(consulta: Consulta) {
            // Capitalizar y formatear la patología
            binding.tvPatologia.text = consulta.patologia
                .replace("_", " ")
                .replaceFirstChar { it.uppercaseChar() }

            binding.tvDiagnostico.text = consulta.diagnostico

            // Mostrar notas solo si existen
            if (consulta.notas.isNotEmpty()) {
                binding.tvNotas.text = "Notas: ${consulta.notas}"
                binding.tvNotas.visibility = ViewGroup.VISIBLE
            } else {
                binding.tvNotas.visibility = ViewGroup.GONE
            }

            // Formatear fecha en español
            val formato = SimpleDateFormat("dd MMM yyyy", Locale("es", "MX"))
            binding.tvFecha.text = formato.format(Date(consulta.fecha))

            // Badge de estado con color diferenciado
            binding.tvEstado.text = when (consulta.estado) {
                "activa"  -> "Activa"
                "cerrada" -> "Cerrada"
                else      -> consulta.estado
            }
            binding.tvEstado.backgroundTintList =
                binding.root.context.getColorStateList(
                    when (consulta.estado) {
                        "activa"  -> R.color.primary
                        else      -> R.color.text_secondary
                    }
                )

            binding.root.setOnClickListener { onItemClick(consulta) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConsultaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffCallback para actualizaciones eficientes.
     * Compara por ID y por contenido completo para detectar cambios de notas.
     */
    class DiffCallback : DiffUtil.ItemCallback<Consulta>() {
        override fun areItemsTheSame(old: Consulta, new: Consulta) = old.id == new.id
        override fun areContentsTheSame(old: Consulta, new: Consulta) = old == new
    }
}