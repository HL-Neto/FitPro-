package com.example.workoutapp

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.example.workoutapp.model.RegistroPeso
import com.example.workoutapp.model.RegistroTempo
import com.example.workoutapp.model.TipoExercicio
import com.example.workoutapp.model.TipoTemporizador
import com.example.workoutapp.model.Treino
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class TreinoViewModel : ViewModel() {

    private var prefs: SharedPreferences? = null
    private var sincronizadoComStorage = false

    private val _listaTreinos = MutableStateFlow<List<Treino>>(emptyList())
    val listaTreinos: StateFlow<List<Treino>> = _listaTreinos.asStateFlow()

    private val _historicoTempos = MutableStateFlow<List<RegistroTempo>>(emptyList())
    val historicoTempos: StateFlow<List<RegistroTempo>> = _historicoTempos.asStateFlow()

    private val _diaSelecionado = MutableStateFlow(obterDiaAtual())
    val diaSelecionado: StateFlow<Int> = _diaSelecionado.asStateFlow()

    init {
        inicializarDadosExemplo()
    }

    fun inicializar(context: Context) {
        if (sincronizadoComStorage) return

        prefs = context.getSharedPreferences("fitpro_storage", Context.MODE_PRIVATE)
        val treinosSalvos = carregarTreinos()
        val temposSalvos = carregarTempos()

        if (treinosSalvos.isNotEmpty()) {
            _listaTreinos.value = treinosSalvos
        } else {
            persistirTreinos()
        }

        if (temposSalvos.isNotEmpty()) {
            _historicoTempos.value = temposSalvos
        }

        sincronizadoComStorage = true
    }

    private fun inicializarDadosExemplo() {
        val hoje = obterDiaAtual()
        _listaTreinos.value = listOf(
            Treino(nome = "Supino Reto", pesoEmKg = 60.0, tipo = TipoExercicio.PEITO, diaSemana = hoje),
            Treino(nome = "Rosca Direta", pesoEmKg = 15.0, tipo = TipoExercicio.BICEPS, diaSemana = hoje),
            Treino(nome = "Puxada Pulley", pesoEmKg = 50.0, tipo = TipoExercicio.COSTAS, diaSemana = hoje),
            Treino(nome = "Agachamento Livre", pesoEmKg = 80.0, tipo = TipoExercicio.PERNAS, diaSemana = hoje)
        )
    }

    fun selecionarDiaSemana(diaSemana: Int) {
        _diaSelecionado.value = diaSemana
    }

    fun adicionarTreino(
        nome: String,
        peso: Double,
        usarLbs: Boolean,
        tipo: TipoExercicio,
        diaSemana: Int = _diaSelecionado.value
    ) {
        if (nome.isBlank()) return

        val pesoEmKg = if (usarLbs) peso / Treino.FATOR_CONVERSAO_LBS else peso
        val novoPeso = maxOf(0.0, pesoEmKg)

        val novoTreino = Treino(
            nome = nome.trim(),
            pesoEmKg = novoPeso,
            tipo = tipo,
            diaSemana = diaSemana,
            historicoPesos = listOf(RegistroPeso(timestamp = System.currentTimeMillis(), pesoEmKg = novoPeso))
        )

        _listaTreinos.update { it + novoTreino }
        persistirTreinos()
    }

    fun atualizarTreino(id: String, nome: String, tipo: TipoExercicio, diaSemana: Int) {
        if (nome.isBlank()) return

        _listaTreinos.update { lista ->
            lista.map { treino ->
                if (treino.id == id) {
                    treino.copy(nome = nome.trim(), tipo = tipo, diaSemana = diaSemana)
                } else {
                    treino
                }
            }
        }
        persistirTreinos()
    }

    fun excluirTreino(id: String) {
        _listaTreinos.update { lista -> lista.filter { treino -> treino.id != id } }
        persistirTreinos()
    }

    fun incrementarPeso(id: String, usarLbs: Boolean, valor: Double = 1.0) {
        _listaTreinos.update { lista ->
            lista.map { treino ->
                if (treino.id == id) {
                    val novoPesoEmKg = if (usarLbs) {
                        (treino.obterPesoEmLbs() + valor) / Treino.FATOR_CONVERSAO_LBS
                    } else {
                        treino.pesoEmKg + valor
                    }
                    val pesoSeguro = maxOf(0.0, novoPesoEmKg)
                    treino.copy(
                        pesoEmKg = pesoSeguro,
                        historicoPesos = treino.historicoPesos + RegistroPeso(
                            timestamp = System.currentTimeMillis(),
                            pesoEmKg = pesoSeguro
                        )
                    )
                } else treino
            }
        }
        persistirTreinos()
    }

    fun decrementarPeso(id: String, usarLbs: Boolean, valor: Double = 1.0) {
        _listaTreinos.update { lista ->
            lista.map { treino ->
                if (treino.id == id) {
                    val novoPesoEmKg = if (usarLbs) {
                        (treino.obterPesoEmLbs() - valor) / Treino.FATOR_CONVERSAO_LBS
                    } else {
                        treino.pesoEmKg - valor
                    }
                    val pesoSeguro = maxOf(0.0, novoPesoEmKg)
                    treino.copy(
                        pesoEmKg = pesoSeguro,
                        historicoPesos = treino.historicoPesos + RegistroPeso(
                            timestamp = System.currentTimeMillis(),
                            pesoEmKg = pesoSeguro
                        )
                    )
                } else treino
            }
        }
        persistirTreinos()
    }

    fun registrarTempoTreino(tipoTemporizador: TipoTemporizador, duracaoSegundos: Int) {
        if (duracaoSegundos <= 0) return

        _historicoTempos.update {
            it + RegistroTempo(
                timestamp = System.currentTimeMillis(),
                tipo = tipoTemporizador,
                duracaoSegundos = duracaoSegundos
            )
        }
        persistirTempos()
    }

    fun limparTreinos() {
        _listaTreinos.value = emptyList()
        persistirTreinos()
    }

    private fun persistirTreinos() {
        val array = JSONArray()
        _listaTreinos.value.forEach { treino ->
            val treinoJson = JSONObject()
                .put("id", treino.id)
                .put("nome", treino.nome)
                .put("pesoEmKg", treino.pesoEmKg)
                .put("tipo", treino.tipo.name)
                .put("diaSemana", treino.diaSemana)

            val historicoArray = JSONArray()
            treino.historicoPesos.forEach { registro ->
                historicoArray.put(
                    JSONObject()
                        .put("timestamp", registro.timestamp)
                        .put("pesoEmKg", registro.pesoEmKg)
                )
            }
            treinoJson.put("historicoPesos", historicoArray)
            array.put(treinoJson)
        }

        prefs?.edit()?.putString("treinos", array.toString())?.apply()
    }

    private fun persistirTempos() {
        val array = JSONArray()
        _historicoTempos.value.forEach { registro ->
            array.put(
                JSONObject()
                    .put("timestamp", registro.timestamp)
                    .put("tipo", registro.tipo.name)
                    .put("duracaoSegundos", registro.duracaoSegundos)
            )
        }
        prefs?.edit()?.putString("tempos", array.toString())?.apply()
    }

    private fun carregarTreinos(): List<Treino> {
        val json = prefs?.getString("treinos", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val historicoArray = item.optJSONArray("historicoPesos") ?: JSONArray()
                    val historico = buildList {
                        for (j in 0 until historicoArray.length()) {
                            val registro = historicoArray.getJSONObject(j)
                            add(
                                RegistroPeso(
                                    timestamp = registro.getLong("timestamp"),
                                    pesoEmKg = registro.getDouble("pesoEmKg")
                                )
                            )
                        }
                    }

                    add(
                        Treino(
                            id = item.getString("id"),
                            nome = item.getString("nome"),
                            pesoEmKg = item.getDouble("pesoEmKg"),
                            tipo = TipoExercicio.valueOf(item.getString("tipo")),
                            diaSemana = item.optInt("diaSemana", Calendar.MONDAY),
                            historicoPesos = historico.ifEmpty {
                                listOf(
                                    RegistroPeso(
                                        timestamp = System.currentTimeMillis(),
                                        pesoEmKg = item.getDouble("pesoEmKg")
                                    )
                                )
                            }
                        )
                    )
                }
            }
        }.getOrElse { emptyList() }
    }

    private fun carregarTempos(): List<RegistroTempo> {
        val json = prefs?.getString("tempos", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(
                        RegistroTempo(
                            timestamp = item.getLong("timestamp"),
                            tipo = TipoTemporizador.valueOf(item.getString("tipo")),
                            duracaoSegundos = item.getInt("duracaoSegundos")
                        )
                    )
                }
            }
        }.getOrElse { emptyList() }
    }

    private fun obterDiaAtual(): Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
}
