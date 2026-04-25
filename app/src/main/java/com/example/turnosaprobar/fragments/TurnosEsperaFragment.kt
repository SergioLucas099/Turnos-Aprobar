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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TurnosEsperaFragment : Fragment() {
    private lateinit var BuscadorTurno: SearchView
    private lateinit var RevListaTurnos: RecyclerView
    private lateinit var RevTurnosActuales: RecyclerView
    private lateinit var RevListaAtracciones: RecyclerView
    private lateinit var adapterAtraccion: VerAtraccionAdapter
    private val listaTurnos = mutableListOf<Turnos>()
    private val listaAtracciones = mutableListOf<Atraccion>()
    private lateinit var AvisoSinTurnos: LinearLayout
    private lateinit var AvisoSinTurnosActuales: LinearLayout
    private lateinit var adapterTurnosEspera: TurnosAdapter
    private lateinit var adapterTurnosAprobados: TurnosAdapter
    private val listaTurnosOriginal = mutableListOf<Turnos>()
    var nombreAtraccion = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_turnos_espera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        BuscadorTurno = view.findViewById(R.id.BuscadorTurno)
        AvisoSinTurnos = view.findViewById(R.id.AvisoSinTurnos)
        AvisoSinTurnosActuales = view.findViewById(R.id.AvisoSinTurnosActuales)

        RevListaAtracciones = view.findViewById(R.id.RevListaAtracciones)
        RevListaTurnos = view.findViewById(R.id.RevListaTurnos)
        RevTurnosActuales = view.findViewById(R.id.RevTurnosActuales)

        adapterAtraccion = VerAtraccionAdapter(
            listaAtracciones,
            { actualizarAtracciones(it) },
            { atraccionSeleccionada ->
                nombreAtraccion = atraccionSeleccionada.nombre
                cargarTurnos()
            }
        )

        RevListaAtracciones.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        RevListaAtracciones.adapter = adapterAtraccion

        adapterTurnosEspera = TurnosAdapter(
            mutableListOf(),
            { turno -> actualizarTurnos(turno) },
            { turno, estado -> llamarTurno(turno, estado) }
        )

        adapterTurnosAprobados = TurnosAdapter(
            mutableListOf(),
            { turno -> actualizarTurnos(turno) },
            { turno, estado -> llamarTurno(turno, estado) }
        )

        RevListaTurnos.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        RevListaTurnos.adapter = adapterTurnosEspera

        RevTurnosActuales.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        RevTurnosActuales.adapter = adapterTurnosAprobados

        BuscadorTurno.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarTurnos(newText ?: "")
                return true
            }
        })

        cargarAtracciones()
        cargarTurnos()

        conectarWebSocket()

        conectarWebSocketTurnos()
    }

    private fun cargarAtracciones() {
        lifecycleScope.launch {
            try {
                val baseUrl = ApiClient.getBaseUrl(requireContext())
                val lista: List<Atraccion> =
                    ApiClient.client.get("${baseUrl}/atracciones")
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
                val baseUrl = ApiClient.getBaseUrl(requireContext())
                val lista: List<Atraccion> =
                    ApiClient.client.get("${baseUrl}/atracciones")
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
                val baseUrl = ApiClient.getBaseUrl(requireContext())
                val lista: List<Turnos> =
                    ApiClient.client
                        .get("${baseUrl}/turnos")
                        .body()

                val listaFiltrada = if (nombreAtraccion.isEmpty()) {
                    lista
                } else {
                    lista.filter { it.nombreAtraccion == nombreAtraccion }
                }

                // 🔥 GUARDAR LISTA ORIGINAL
                listaTurnosOriginal.clear()
                listaTurnosOriginal.addAll(listaFiltrada)

                val listaEspera = listaFiltrada.filter { it.estado == "ESPERA" }
                val listaAprobados = listaFiltrada.filter { it.estado == "APROBADO" }

                adapterTurnosEspera.actualizarLista(listaEspera)
                adapterTurnosAprobados.actualizarLista(listaAprobados)

                AvisoSinTurnos.visibility =
                    if (listaEspera.isEmpty()) View.VISIBLE else View.GONE

                AvisoSinTurnosActuales.visibility =
                    if (listaAprobados.isEmpty()) View.VISIBLE else View.GONE

            } catch (e: Exception) {
                Log.e("ERROR_TURNOS", e.message ?: "Error desconocido")
            }
        }
    }

    private fun actualizarTurnos(turnos: Turnos){
        lifecycleScope.launch {
            try {
                val baseUrl = ApiClient.getBaseUrl(requireContext())
                val response = ApiClient.client.put("${baseUrl}/turnos/${turnos._id}") {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    setBody(mapOf("estado" to turnos.estado))
                }

                Log.d("PUT_TURNO", "Respuesta: $response")

                cargarTurnos()

            } catch (e: Exception) {
                Log.e("ERROR_PUT", e.message ?: "Error desconocido")
            }
        }
    }

    private fun llamarTurno(turno: Turnos, estado: Boolean){
        lifecycleScope.launch {
            try {
                val baseUrl = ApiClient.getBaseUrl(requireContext())
                val url = "${baseUrl}/turnos/${turno._id}/llamar"

                Log.d("LLAMAR_TURNO", "URL: $url")
                Log.d("LLAMAR_TURNO", "ID: ${turno._id}")
                Log.d("LLAMAR_TURNO", "Estado enviado: $estado")

                val response = ApiClient.client.put(url) {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    setBody(mapOf("llamandoTurno" to estado))
                }

                Log.d("LLAMAR_TURNO", "REQUEST ENVIADO")
                Log.d("LLAMAR_TURNO", "Respuesta RAW: $response")

            } catch (e: Exception) {
                Log.e("ERROR_LLAMAR", "ERROR COMPLETO", e)
            }
        }
    }

    private fun filtrarTurnos(texto: String) {

        val textoLower = texto.lowercase()

        // 🔥 Si no hay texto → restaurar todo
        if (textoLower.isEmpty()) {

            val listaEspera = listaTurnosOriginal.filter { it.estado == "ESPERA" }
            val listaAprobados = listaTurnosOriginal.filter { it.estado == "APROBADO" }

            adapterTurnosEspera.actualizarLista(listaEspera)
            adapterTurnosAprobados.actualizarLista(listaAprobados)

            return
        }

        // 🔍 Filtrar sobre la lista original
        val filtrados = listaTurnosOriginal.filter {

            (it.numeroTurno.contains(textoLower, true) ||
                    it.telefono.replace(" ", "").contains(textoLower)) &&

                    (nombreAtraccion.isEmpty() || it.nombreAtraccion == nombreAtraccion)
        }

        val listaEspera = filtrados.filter { it.estado == "ESPERA" }
        val listaAprobados = filtrados.filter { it.estado == "APROBADO" }

        adapterTurnosEspera.actualizarLista(listaEspera)
        adapterTurnosAprobados.actualizarLista(listaAprobados)
    }

    private fun conectarWebSocket() {

        lifecycleScope.launch {

            try {
                val baseUrl = ApiClient.getBaseUrl(requireContext())

                val host = baseUrl
                    .replace("http://", "")
                    .replace(":8080", "")

                ApiClient.client.webSocket(
                    method = io.ktor.http.HttpMethod.Get,
                    host = host,
                    port = 8080,
                    path = "/ws/atracciones"
                ) {

                    for (frame in incoming) {
                        if (frame is Frame.Text) {

                            val mensaje = frame.readText()

                            if (mensaje == "ATRACCIONES_UPDATED") {
                                withContext(Dispatchers.Main) {
                                    cargarAtracciones()
                                }
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                println("Error WebSocket: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun conectarWebSocketTurnos() {
        lifecycleScope.launch {

            try {
                val baseUrl = ApiClient.getBaseUrl(requireContext())

                val host = baseUrl
                    .replace("http://", "")
                    .replace(":8080", "")

                ApiClient.client.webSocket(
                    method = io.ktor.http.HttpMethod.Get,
                    host = host,
                    port = 8080,
                    path = "/ws/turnos"
                ) {

                    for (frame in incoming) {
                        if (frame is Frame.Text) {

                            val mensaje = frame.readText()

                            if (mensaje == "TURNOS_UPDATED") {
                                withContext(Dispatchers.Main) {
                                    cargarTurnos()
                                }
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                println("❌ Error WebSocket TURNOS: ${e.message}")
            }
        }
    }
}