package com.example.workoutapp.model

import java.util.UUID

/**
 * Enumeração representando os tipos de exercício solicitados.
 */
enum class TipoExercicio(val nomeExibicao: String) {
    PEITO("Peito"),
    BRACO("Braço"),
    COSTAS("Costas"),
    PERNAS("Pernas")
}

/**
 * Classe de dados representando um Treino.
 * O peso base é sempre armazenado em Quilogramas (pesoEmKg) para consistência dos dados.
 */
data class Treino(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val pesoEmKg: Double,
    val tipo: TipoExercicio
) {
    // Fator de conversão padrão: 1 kg = 2.20462 lbs
    companion object {
        const val FATOR_CONVERSAO_LBS: Double = 2.20462
    }

    /**
     * Retorna o peso convertido para Libras (lbs).
     */
    fun obterPesoEmLbs(): Double {
        return pesoEmKg * FATOR_CONVERSAO_LBS
    }
}
