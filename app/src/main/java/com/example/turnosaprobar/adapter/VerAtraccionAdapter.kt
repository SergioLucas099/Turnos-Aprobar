package com.example.turnosaprobar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.turnosaprobar.R
import com.example.turnosaprobar.model.Atraccion

class VerAtraccionAdapter (
    private val lista: MutableList<Atraccion>,
    private val onEditar: (Atraccion) -> Unit,
    private val onAtraccionSeleccionada: (Atraccion) -> Unit
) : RecyclerView.Adapter<VerAtraccionAdapter.VerAtraccionAdapterViewHolder>() {

    private var posicionSeleccionada: Int = -1

    inner class VerAtraccionAdapterViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
            val nombreAtracciones: TextView = itemView.findViewById(R.id.nombreAtracciones)
            val SeleccionarAtracciones: ConstraintLayout = itemView.findViewById(R.id.SeleccionarAtracciones)
        }

    override fun onCreateViewHolder (parent: ViewGroup, viewType: Int) : VerAtraccionAdapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ver_atraccion_item, parent, false)
        return VerAtraccionAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: VerAtraccionAdapterViewHolder, position: Int) {

        val atracciones = lista[position]

        holder.nombreAtracciones.text = atracciones.nombre

        val seleccionado = position == posicionSeleccionada

        if (seleccionado) {
            holder.SeleccionarAtracciones.setBackgroundColor(
                holder.itemView.context.getColor(R.color.cafePrincipalOscuro)
            )
        } else {
            holder.SeleccionarAtracciones.setBackgroundColor(
                holder.itemView.context.getColor(R.color.cafePrincipal)
            )
        }

        holder.SeleccionarAtracciones.setOnClickListener {

            if (posicionSeleccionada == position) {
                // Deseleccionar
                posicionSeleccionada = -1
                notifyItemChanged(position)

                onAtraccionSeleccionada(
                    Atraccion(nombre = "", tiempoXpersona = 0, tiempoAcumulado = 0, turnoActual = "", activa = false)
                )

            } else {

                val posicionAnterior = posicionSeleccionada
                posicionSeleccionada = position

                if (posicionAnterior != -1) {
                    notifyItemChanged(posicionAnterior)
                }

                notifyItemChanged(position)

                onAtraccionSeleccionada(atracciones)
            }
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Atraccion>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}