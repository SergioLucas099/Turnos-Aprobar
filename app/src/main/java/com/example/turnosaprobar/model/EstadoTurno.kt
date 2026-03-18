package com.example.turnosaprobar.model

import kotlinx.serialization.Serializable

@Serializable
enum class EstadoTurno {
    ESPERA,
    APROBADO,
    LLAMADO,
    FINALIZADO,
    CANCELADO,
}