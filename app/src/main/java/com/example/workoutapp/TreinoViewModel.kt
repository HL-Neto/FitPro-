package com.example.workoutapp

import androidx.lifecycle.ViewModel
import com.example.workoutapp.model.Treino
import com.example.workoutapp.model.TipoExercicio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel que gerencia a lista de treinos e regras de negócios.
 */
class TreinoViewModel : ViewModel() {

    private val _listaTreinos = MutableStateFlow<List<Treino>>(emptyList())
    val listaTreinos: StateFlow<List<Treino>> = _listaTreinos.asStateFlow()

    init {
        // Inicializa com alguns treinos de exemplo para demonstração imediata
        inicializarDadosExemplo()
    }

    /**
     * Adiciona treinos iniciais padrão.
     */
    private fun inicializarDadosExemplo() {
        adicionarTreino("Supino Reto", 60.0, usarLbs = false, TipoExercicio.PEITO)
        adicionarTreino("Rosca Direta", 15.0, usarLbs = false, TipoExercicio.BRACO)
        adicionarTreino("Puxada Pulley", 50.0, usarLbs = false, TipoExercicio.COSTAS)
        adicionarTreino("Agachamento Livre", 80.0, usarLbs = false, TipoExercicio.PERNAS)
    }

    /**
     * Adiciona um novo treino à lista.
     * @param nome Nome do exercício.
     * @param peso Peso inicial inserido pelo usuário.
     * @param usarLbs Se true, o peso informado está em Libras e será convertido para kg internamente.
     * @param tipo Categoria do exercício.
     */
    fun adicionarTreino(nome: String, peso: Double, usarLbs: Boolean, tipo: TipoExercicio) {
        if (nome.isBlank()) return
        
        // Converte para KG caso o peso inicial tenha sido inserido em LBS
        val pesoEmKg = if (usarLbs) {
            peso / Treino.FATOR_CONVERSAO_LBS
        } else {
            peso
        }

        val novoTreino = Treino(
            nome = nome.trim(),
            pesoEmKg = maxOf(0.0, pesoEmKg),
            tipo = tipo
        )
        
        _listaTreinos.update { lista ->
            lista + novoTreino
        }
    }

    /**
     * Exclui um treino com base em seu ID.
     */
    fun excluirTreino(id: String) {
        _listaTreinos.update { lista ->
            lista.filter { treino -> treino.id != id }
        }
    }

    /**
     * Incrementa o peso de um treino.
     * @param id ID do treino.
     * @param usarLbs Se true, incrementa na escala de Libras, caso contrário na de Quilogramas.
     * @param valor Quantidade a ser incrementada.
     */
    fun incrementarPeso(id: String, usarLbs: Boolean, valor: Double = 1.0) {
        _listaTreinos.update { lista ->
            lista.map { treino ->
                if (treino.id == id) {
                    val novoPesoEmKg = if (usarLbs) {
                        // Incrementa em LBS e converte de volta para KG
                        val pesoLbsAtual = treino.obterPesoEmLbs()
                        (pesoLbsAtual + valor) / Treino.FATOR_CONVERSAO_LBS
                    } else {
                        // Incrementa diretamente em KG
                        treino.pesoEmKg + valor
                    }
                    treino.copy(pesoEmKg = maxOf(0.0, novoPesoEmKg))
                } else {
                    treino
                }
            }
        }
    }

    /**
     * Decrementa o peso de um treino.
     * Garantido que o peso não ficará menor que zero.
     * @param id ID do treino.
     * @param usarLbs Se true, decrementa na escala de Libras, caso contrário na de Quilogramas.
     * @param valor Quantidade a ser decrementada.
     */
    fun decrementarPeso(id: String, usarLbs: Boolean, valor: Double = 1.0) {
        _listaTreinos.update { lista ->
            lista.map { treino ->
                if (treino.id == id) {
                    val novoPesoEmKg = if (usarLbs) {
                        // Decrementa em LBS e converte de volta para KG
                        val pesoLbsAtual = treino.obterPesoEmLbs()
                        (pesoLbsAtual - valor) / Treino.FATOR_CONVERSAO_LBS
                    } else {
                        // Decrementa diretamente em KG
                        treino.pesoEmKg - valor
                    }
                    treino.copy(pesoEmKg = maxOf(0.0, novoPesoEmKg))
                } else {
                    treino
                }
            }
        }
    }

    /**
     * Limpa a lista de treinos (útil para testes).
     */
    fun limparTreinos() {
        _listaTreinos.value = emptyList()
    }
}
