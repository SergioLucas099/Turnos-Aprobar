package com.example.turnosaprobar.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json

object ApiClient {

    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json() }
        install(WebSockets)
    }

    const val BASE_URL = "http://192.168.0.182:8080"

    //⚠️ IMPORTANTE
    //Reemplaza IP por la IP de tu PC en red local.

//    Adaptador de Ethernet Ethernet 4:
//
//    Sufijo DNS específico para la conexión. . : www.tendawifi.com
//    Vínculo: dirección IPv6 local. . . : fe80::a301:b3fd:9469:8ce2%22
//    Dirección IPv4. . . . . . . . . . . . . . : 192.168.0.200
//    Máscara de subred . . . . . . . . . . . . : 255.255.255.0
//    Puerta de enlace predeterminada . . . . . : 192.168.0.1
}