package com.example.turnosaprobar.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.turnosaprobar.R
import com.example.turnosaprobar.adapter.TurnosAdapter
import com.example.turnosaprobar.adapter.VerAtraccionAdapter
import com.example.turnosaprobar.model.Atraccion
import com.example.turnosaprobar.model.Turnos
import com.example.turnosaprobar.network.ApiClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TurnosCanceladosFragment : Fragment() {

    private lateinit var RevListaAtraccionesTC: RecyclerView
    private lateinit var RevListaTurnosCancelados: RecyclerView
    private lateinit var AvisoSinTurnosCancelados: LinearLayout
    private lateinit var BuscadorTurnoCancelados: SearchView
    private lateinit var adapterTurnos: TurnosAdapter
    private lateinit var adapterAtraccion: VerAtraccionAdapter
    private val listaTurnos = mutableListOf<Turnos>()
    private val listaTurnosOriginal = mutableListOf<Turnos>()
    private val listaAtracciones = mutableListOf<Atraccion>()
    var nombreAtraccion = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_turnos_cancelados, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        BuscadorTurnoCancelados = view.findViewById(R.id.BuscadorTurnoCancelados)
        AvisoSinTurnosCancelados = view.findViewById(R.id.AvisoSinTurnosCancelados)

        // Lista Atracciones
        RevListaAtraccionesTC = view.findViewById(R.id.RevListaAtraccionesTC)

        adapterAtraccion = VerAtraccionAdapter(
            listaAtracciones,
            { atraccionesActualizadas -> actualizarAtracciones(atraccionesActualizadas) },
            { atraccionSeleccionada ->
                nombreAtraccion = atraccionSeleccionada.nombre
                cargarTurnos()
            }
        )

        RevListaAtraccionesTC.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        RevListaAtraccionesTC.adapter = adapterAtraccion

        // Lista Turnos Cancelados
        RevListaTurnosCancelados = view.findViewById(R.id.RevListaTurnosCancelados)

        adapterTurnos = TurnosAdapter(
            mutableListOf(),
            { turno -> actualizarTurnos(turno) }
        )

        RevListaTurnosCancelados.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        RevListaTurnosCancelados.adapter = adapterTurnos

        BuscadorTurnoCancelados.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarTurnos(newText ?: "")
                return true
            }
        })

        // Cargar datos
        cargarAtracciones()
        cargarTurnos()

        // Conectar WebSocket UNA SOLA VEZ
        conectarWebSocket()
    }

    private fun cargarAtracciones() {
        lifecycleScope.launch {
            try {
                val lista: List<Atraccion> =
                    ApiClient.client.get("${ApiClient.BASE_URL}/atracciones")
                        .body()
                adapterAtraccion.actualizarLista(lista)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun actualizarAtracciones(atraccion: Atraccion) {
        lifecycleScope.launch {
            try {
                val lista: List<Atraccion> =
                    ApiClient.client.get("${ApiClient.BASE_URL}/atracciones")
                        .body()
                adapterAtraccion.actualizarLista(lista)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun cargarTurnos() {
        lifecycleScope.launch {
            try {
                val lista: List<Turnos> =
                    ApiClient.client
                        .get("${ApiClient.BASE_URL}/turnos")
                        .body()

                val listaFiltrada = if (nombreAtraccion.isEmpty()) {
                    lista
                } else {
                    lista.filter { it.nombreAtraccion == nombreAtraccion }
                }

                val listaCancelados = listaFiltrada.filter { it.estado == "CANCELADO" }

                listaTurnosOriginal.clear()
                listaTurnosOriginal.addAll(listaCancelados)

                adapterTurnos.actualizarLista(listaCancelados)

                AvisoSinTurnosCancelados.visibility =
                    if (listaCancelados.isEmpty()) View.VISIBLE else View.GONE

            } catch (e: Exception) {
                Log.e("ERROR_TURNOS", e.message ?: "Error desconocido")
            }
        }
    }

    private fun filtrarTurnos(texto: String) {

        val textoLower = texto.lowercase()

        if (textoLower.isEmpty()) {
            adapterTurnos.actualizarLista(listaTurnosOriginal)
            return
        }

        val filtrados = listaTurnosOriginal.filter {

            it.numeroTurno.contains(textoLower, true) ||
                    it.telefono.replace(" ", "").contains(textoLower)
        }

        adapterTurnos.actualizarLista(filtrados)
    }

    private fun actualizarTurnos(turnos: Turnos){
        lifecycleScope.launch {
            try {
                ApiClient.client.put("${ApiClient.BASE_URL}/turnos/${turnos._id}") {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    setBody(turnos)
                }
                cargarTurnos()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun conectarWebSocket() {

        lifecycleScope.launch {

            try {

                ApiClient.client.webSocket(
                    method = io.ktor.http.HttpMethod.Get,
                    host = "192.168.0.182",
                    port = 8080,
                    path = "/ws/turnos"
                ) {

                    for (frame in incoming) {

                        if (frame is Frame.Text) {

                            val mensaje = frame.readText()
                            Log.d("WEBSOCKET", "Mensaje recibido: $mensaje")

                            if (mensaje == "TURNOS_UPDATED") {

                                withContext(Dispatchers.Main) {
                                    cargarTurnos()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}