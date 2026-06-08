package com.example.workoutapp.model

import java.util.UUID

enum class TipoExercicio(val nomeExibicao: String) {
    PEITO("Peito"),
    BICEPS("Bíceps"),
    COSTAS("Costas"),
    PERNAS("Pernas")
}

enum class TipoTemporizador(val nomeExibicao: String) {
    TREINO("Treino"),
    CARDIO("Cardio"),
    EXERCICIO("Exercício")
}

data class RegistroPeso(
    val timestamp: Long,
    val pesoEmKg: Double
)

data class RegistroTempo(
    val timestamp: Long,
    val tipo: TipoTemporizador,
    val duracaoSegundos: Int
)

data class Treino(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val pesoEmKg: Double,
    val tipo: TipoExercicio,
    val diaSemana: Int,
    val historicoPesos: List<RegistroPeso> = listOf(RegistroPeso(System.currentTimeMillis(), pesoEmKg))
) {
    companion object {
        const val FATOR_CONVERSAO_LBS: Double = 2.20462
    }

    fun obterPesoEmLbs(): Double = pesoEmKg * FATOR_CONVERSAO_LBS
}
