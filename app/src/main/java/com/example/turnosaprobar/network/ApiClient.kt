package com.example.turnosaprobar.network

import android.content.Context
import android.net.wifi.WifiManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ApiClient {

    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json() }
        install(WebSockets)
    }

    private var cachedBaseUrl: String? = null

    suspend fun getBaseUrl(context: Context): String {

        val prefs = context.getSharedPreferences("api_prefs", Context.MODE_PRIVATE)

        // 1. Revisar cache en memoria
        cachedBaseUrl?.let { return it }

        // 2. Revisar cache persistente (SharedPreferences)
        val savedUrl = prefs.getString("base_url", null)

        if (savedUrl != null) {
            try {
                // Validar que aún funciona
                client.get("$savedUrl/turnos/ping")

                cachedBaseUrl = savedUrl
                return savedUrl

            } catch (_: Exception) {
                // Si falla, seguimos al escaneo
            }
        }

        // 3. Obtener base IP
        val baseIp = getBaseIp(context)

        // 4. Escanear red
        val serverIp = scanNetworkForServer(baseIp)

        val url = "http://$serverIp:8080"

        // 5. Guardar en memoria
        cachedBaseUrl = url

        // 6. Guardar en SharedPreferences 🔥
        prefs.edit().putString("base_url", url).apply()

        return url
    }

    private fun getBaseIp(context: Context): String {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ip = wm.connectionInfo.ipAddress

        val ipString = String.format(
            "%d.%d.%d.%d",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff,
            ip shr 24 and 0xff
        )

        return ipString.substringBeforeLast(".")
    }

    private suspend fun scanNetworkForServer(baseIp: String): String {
        return withContext(Dispatchers.IO) {

            coroutineScope {

                val result = CompletableDeferred<String>()

                for (i in 1..254) {
                    val ip = "$baseIp.$i"

                    launch {
                        try {
                            val response = client.get("http://$ip:8080/turnos/ping")

                            if (response.status.value == 200) {
                                if (!result.isCompleted) {
                                    result.complete(ip)
                                }
                            }

                        } catch (_: Exception) {}
                    }
                }

                return@coroutineScope result.await()
            }
        }
    }

    fun extractHost(baseUrl: String): String {
        return baseUrl
            .replace("http://", "")
            .replace(":8080", "")
    }
}