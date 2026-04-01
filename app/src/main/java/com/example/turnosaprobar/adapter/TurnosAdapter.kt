package com.example.turnosaprobar.adapter

import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.turnosaprobar.R
import com.example.turnosaprobar.model.Turnos
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.toString
import android.os.Handler
import android.os.Looper

class TurnosAdapter (
    private val lista: MutableList<Turnos>,
    private val onEditar: (Turnos) -> Unit,
    private val onLlamar: (Turnos, Boolean) -> Unit
) : RecyclerView.Adapter<TurnosAdapter.TurnosAdapterViewHolder>() {

    inner class TurnosAdapterViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val tiempoTurno: TextView =
            itemView.findViewById(R.id.tiempoTurno)
        val TurnoAsignado: TextView =
            itemView.findViewById(R.id.TurnoAsignado)
        val personasTurno: TextView =
            itemView.findViewById(R.id.personasTurno)
        val ContenidoAtraccion: LinearLayout =
            itemView.findViewById(R.id.ContenidoAtraccion)
        val EliminarTurno: ImageView =
            itemView.findViewById(R.id.EliminarTurno)
        val LlamarTurno: ImageView =
            itemView.findViewById(R.id.LlamarTurno)
        val CancelarTurno: ImageView =
            itemView.findViewById(R.id.CancelarTurno)
        val AceptarTurno: ImageView =
            itemView.findViewById(R.id.AceptarTurno)
        val AprobarTurno: LinearLayout =
            itemView.findViewById(R.id.AprobarTurno)
        val FinalizarTurno: ImageView =
            itemView.findViewById(R.id.FinalizarTurno)
        val DevolverTurno: ImageView =
            itemView.findViewById(R.id.DevolverTurno)
        val FuncionesTurnosEspera: LinearLayout =
            itemView.findViewById(R.id.FuncionesTurnosEspera)
        val FuncionesTurnosAprobados: LinearLayout =
            itemView.findViewById(R.id.FuncionesTurnosAprobados)
        val FuncionesTurnosCancelados: LinearLayout =
            itemView.findViewById(R.id.FuncionesTurnosCancelados)
        val nombreAtraccionSelect: TextView =
            itemView.findViewById(R.id.nombreAtraccionSelect)

        var countDownTimer: CountDownTimer? = null
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TurnosAdapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.turnos_item, parent, false)

        return TurnosAdapterViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(
        holder: TurnosAdapterViewHolder,
        position: Int) {

        val turno = lista[position]

        holder.tiempoTurno.text = formatearTiempo(turno.duracion)
        holder.TurnoAsignado.text = turno.numeroTurno
        holder.personasTurno.text = turno.numeroPersonas.toString()
        holder.nombreAtraccionSelect.text = turno.nombreAtraccion
        holder.FinalizarTurno.visibility = View.GONE

        val estadoTurno = turno.estado

        holder.ContenidoAtraccion.setOnClickListener {
            val dialogView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.dialog_validar_info, null)

            val txtDatosAtraccionTurno = dialogView.findViewById<TextView>(R.id.txtDatosAtraccionTurno)
            txtDatosAtraccionTurno.text = turno.nombreAtraccion

            val txtDatosNumeroTurno = dialogView.findViewById<TextView>(R.id.txtDatosNumeroTurno)
            txtDatosNumeroTurno.text = turno.numeroTurno

            val txtDatosPersonasTurno = dialogView.findViewById<TextView>(R.id.txtDatosPersonasTurno)
            txtDatosPersonasTurno.text = turno.numeroPersonas.toString()

            val txtDatosTelefonoTurno = dialogView.findViewById<TextView>(R.id.txtDatosTelefonoTurno)
            txtDatosTelefonoTurno.text = turno.telefono
            if (turno.telefono.isEmpty()) {
                txtDatosTelefonoTurno.text = "No registrado"
            }

            val txtDatosTiempoTurno = dialogView.findViewById<TextView>(R.id.txtDatosTiempoTurno)
            txtDatosTiempoTurno.text = formatearTiempo(turno.duracion)

            val txtDatosTiempoEsperaTurno = dialogView.findViewById<TextView>(R.id.txtDatosTiempoEsperaTurno)
            txtDatosTiempoEsperaTurno.text = formatearTiempo(turno.tiempoEspera)

            val txtDatosEstadoTurno = dialogView.findViewById<TextView>(R.id.txtDatosEstadoTurno)
            txtDatosEstadoTurno.text = turno.estado

            val txtDatosFechaTurno = dialogView.findViewById<TextView>(R.id.txtDatosFechaTurno)
            val fecha = LocalDateTime.parse(turno.fecha)

            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")

            txtDatosFechaTurno.text = fecha
                .format(formatter)
                .replace("AM","a.m")
                .replace("PM","p.m")

            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setView(dialogView)
                .setPositiveButton("Aceptar") { dialog, _ ->
                    // acción si el usuario confirma
                    dialog.dismiss()
                }

            val dialog = builder.create()
            dialog.show()
        }

        holder.EliminarTurno.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Cancelar Turno ${turno.nombreAtraccion}")
                .setMessage("¿Seguro que desea cancelar el turno ${turno.numeroTurno}?")
                .setPositiveButton("Si") {_, _ ->

                    turno.estado = "CANCELADO"
                    onEditar(turno)

                    Toast.makeText(
                        holder.itemView.context,
                        "Turno ${turno.numeroTurno} de ${turno.nombreAtraccion} eliminado",
                        Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        holder.LlamarTurno.setOnClickListener {

            Toast.makeText(
                holder.itemView.context,
                "Llamando Turno...",
                Toast.LENGTH_SHORT
            ).show()

            onLlamar(turno, true)

            Handler(Looper.getMainLooper()).postDelayed({

                onLlamar(turno, false)

            }, 6000)
        }

        holder.AceptarTurno.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Recibir Turno ${turno.nombreAtraccion}")
                .setMessage("¿Seguro que desea recibir el turno ${turno.numeroTurno}?")
                .setPositiveButton("Si") {_, _ ->

                    turno.estado = "APROBADO"
                    onEditar(turno)

                    Toast.makeText(
                        holder.itemView.context,
                        "Turno ${turno.numeroTurno} de ${turno.nombreAtraccion} recibido",
                        Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        holder.AprobarTurno.setOnClickListener {
            Toast.makeText(
                holder.itemView.context,
                "Iniciando Cronómetro",
                Toast.LENGTH_SHORT
            ).show()

            holder.AprobarTurno.visibility = View.GONE
            holder.FinalizarTurno.visibility = View.VISIBLE

            iniciarCronometro(holder, turno.duracion)
        }

        holder.FinalizarTurno.setOnClickListener {

            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Finalizar Turno ${turno.nombreAtraccion}")
                .setMessage("¿Seguro que desea Finalizar el turno ${turno.numeroTurno}?")
                .setPositiveButton("Si") {_, _ ->

                    turno.estado = "FINALIZADO"
                    onEditar(turno)

                    Toast.makeText(
                        holder.itemView.context,
                        "Turno ${turno.numeroTurno} de ${turno.nombreAtraccion} recibido",
                        Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()

            holder.AprobarTurno.visibility = View.VISIBLE
            holder.FinalizarTurno.visibility = View.GONE
        }

        holder.DevolverTurno.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Devolver Turno ${turno.nombreAtraccion}")
                .setMessage("¿Seguro que desea devolver el turno ${turno.numeroTurno}?")
                .setPositiveButton("Si") {_, _ ->

                    turno.estado = "ESPERA"
                    onEditar(turno)

                    Toast.makeText(
                        holder.itemView.context,
                        "Turno ${turno.numeroTurno} de ${turno.nombreAtraccion} restaurado",
                        Toast.LENGTH_SHORT).show()

                    Log.d("DEVOLVER_TURNO", "ID: ${turno._id} → ${turno.estado}")
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        if (estadoTurno == "ESPERA") {
            holder.FuncionesTurnosEspera.visibility = View.VISIBLE
            holder.FuncionesTurnosAprobados.visibility = View.GONE
            holder.FuncionesTurnosCancelados.visibility = View.GONE
        } else if (estadoTurno == "APROBADO") {
            holder.FuncionesTurnosEspera.visibility = View.GONE
            holder.FuncionesTurnosAprobados.visibility = View.VISIBLE
            holder.FuncionesTurnosCancelados.visibility = View.GONE
        } else if (estadoTurno == "CANCELADO") {
            holder.FuncionesTurnosEspera.visibility = View.GONE
            holder.FuncionesTurnosAprobados.visibility = View.GONE
            holder.FuncionesTurnosCancelados.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = lista.size

    override fun onViewRecycled(holder: TurnosAdapterViewHolder) {
        super.onViewRecycled(holder)
        holder.countDownTimer?.cancel()
    }

    fun actualizarLista(nuevaLista: List<Turnos>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    private fun formatearTiempo(segundos: Int): String {

        val horas = segundos / 3600
        val minutos = (segundos % 3600) / 60
        val seg = segundos % 60

        return when {
            horas > 0 -> String.format("%02d:%02d hr", horas, minutos)
            minutos > 0 -> String.format("%02d:%02d min", minutos, seg)
            else -> String.format("00:%02d seg", seg)
        }
    }

    private fun iniciarCronometro(holder: TurnosAdapterViewHolder, duracionSegundos: Int) {

        // Cancelar timer anterior si existe
        holder.countDownTimer?.cancel()

        holder.countDownTimer = object : CountDownTimer(duracionSegundos * 1000L, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val segundosRestantes = (millisUntilFinished / 1000).toInt()
                holder.tiempoTurno.text = formatearTiempo(segundosRestantes)
            }

            override fun onFinish() {
                holder.tiempoTurno.text = "00:00"

                holder.FinalizarTurno.visibility = View.VISIBLE
            }

        }.start()
    }


}