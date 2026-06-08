package com.example.workoutapp

import com.example.workoutapp.model.TipoExercicio
import com.example.workoutapp.model.Treino
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Testes unitários para validar as regras de negócios do TreinoViewModel.
 */
class TreinoViewModelTest {

    private lateinit var viewModel: TreinoViewModel

    @Before
    fun setUp() {
        viewModel = TreinoViewModel()
    }

    @Test
    fun testInicializacaoComDadosDeExemplo() {
        val treinos = viewModel.listaTreinos.value
        assertEquals("Devem ser inicializados 4 treinos padrão", 4, treinos.size)
        assertEquals("Supino Reto", treinos[0].nome)
        assertEquals(TipoExercicio.PEITO, treinos[0].tipo)
    }

    @Test
    fun testAdicionarTreinoEmKg() {
        viewModel.limparTreinos()
        viewModel.adicionarTreino("Supino Inclinado", 70.0, usarLbs = false, TipoExercicio.PEITO)
        
        val treinos = viewModel.listaTreinos.value
        assertEquals("Deve haver 1 treino cadastrado", 1, treinos.size)
        val treino = treinos[0]
        assertEquals("Supino Inclinado", treino.nome)
        assertEquals(70.0, treino.pesoEmKg, 0.001)
        assertEquals(TipoExercicio.PEITO, treino.tipo)
    }

    @Test
    fun testAdicionarTreinoEmLbs() {
        viewModel.limparTreinos()
        // Adiciona 110 lbs. Internamente deve armazenar convertido em KG.
        viewModel.adicionarTreino("Rosca Concentrada", 110.0, usarLbs = true, TipoExercicio.BRACO)
        
        val treinos = viewModel.listaTreinos.value
        assertEquals(1, treinos.size)
        val treino = treinos[0]
        assertEquals("Rosca Concentrada", treino.nome)
        // Peso em Kg deve ser 110 / Fator
        val pesoEsperadoKg = 110.0 / Treino.FATOR_CONVERSAO_LBS
        assertEquals(pesoEsperadoKg, treino.pesoEmKg, 0.001)
        // Peso retornado em lbs deve ser exatamente 110
        assertEquals(110.0, treino.obterPesoEmLbs(), 0.001)
    }

    @Test
    fun testExcluirTreino() {
        val treinosIniciais = viewModel.listaTreinos.value
        val idParaExcluir = treinosIniciais[0].id
        val totalInicial = treinosIniciais.size

        viewModel.excluirTreino(idParaExcluir)

        val treinosFinais = viewModel.listaTreinos.value
        assertEquals(totalInicial - 1, treinosFinais.size)
        assertFalse(treinosFinais.any { it.id == idParaExcluir })
    }

    @Test
    fun testIncrementarPesoEmKg() {
        viewModel.limparTreinos()
        viewModel.adicionarTreino("Agachamento", 100.0, usarLbs = false, TipoExercicio.PERNAS)
        val id = viewModel.listaTreinos.value[0].id

        viewModel.incrementarPeso(id, usarLbs = false, valor = 5.0)

        val treino = viewModel.listaTreinos.value[0]
        assertEquals("Peso deve aumentar de 100 para 105 Kg", 105.0, treino.pesoEmKg, 0.001)
    }

    @Test
    fun testIncrementarPesoEmLbs() {
        viewModel.limparTreinos()
        viewModel.adicionarTreino("Remada", 50.0, usarLbs = false, TipoExercicio.COSTAS) // 50kg = ~110.231 lbs
        val id = viewModel.listaTreinos.value[0].id

        val lbsOriginal = viewModel.listaTreinos.value[0].obterPesoEmLbs()
        viewModel.incrementarPeso(id, usarLbs = true, valor = 10.0) // Incrementa 10 lbs

        val treino = viewModel.listaTreinos.value[0]
        assertEquals("Peso em Lbs deve aumentar em 10.0", lbsOriginal + 10.0, treino.obterPesoEmLbs(), 0.001)
    }

    @Test
    fun testDecrementarPesoNaoFicaNegativo() {
        viewModel.limparTreinos()
        viewModel.adicionarTreino("Tríceps Testa", 5.0, usarLbs = false, TipoExercicio.BRACO)
        val id = viewModel.listaTreinos.value[0].id

        viewModel.decrementarPeso(id, usarLbs = false, valor = 10.0) // Decrementa 10 de 5.

        val treino = viewModel.listaTreinos.value[0]
        assertEquals("O peso não pode ficar menor que 0.0", 0.0, treino.pesoEmKg, 0.001)
    }
}
