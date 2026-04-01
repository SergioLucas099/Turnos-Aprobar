package com.example.turnosaprobar.model

import kotlinx.serialization.Serializable

@Serializable
data class Turnos (
    val _id: String? = null,
    val atraccionId: String,
    val nombreAtraccion: String,
    val numeroTurno: String,
    val numeroPersonas: Int,
    val telefono: String,
    val tiempoEspera: Int = 0,
    val duracion: Int = 0,
    var estado: String,
    var llamandoTurno: Boolean,
    val fecha: String
)