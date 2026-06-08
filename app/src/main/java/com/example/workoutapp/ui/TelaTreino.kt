package com.example.workoutapp.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.CountDownTimer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.workoutapp.TreinoViewModel
import com.example.workoutapp.model.RegistroTempo
import com.example.workoutapp.model.TipoExercicio
import com.example.workoutapp.model.TipoTemporizador
import com.example.workoutapp.model.Treino
import com.example.workoutapp.ui.theme.CardSlate
import com.example.workoutapp.ui.theme.CoralRed
import com.example.workoutapp.ui.theme.CyberTeal
import com.example.workoutapp.ui.theme.ObsidianBg
import com.example.workoutapp.ui.theme.SurfaceSlate
import com.example.workoutapp.ui.theme.TextPrimary
import com.example.workoutapp.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val TIMER_CHANNEL_ID = "fitpro_timer"
private const val TIMER_NOTIFICATION_ID = 5001

@Composable
fun TelaTreino(viewModel: TreinoViewModel) {
    val listaTreinos by viewModel.listaTreinos.collectAsState()
    val historicoTempos by viewModel.historicoTempos.collectAsState()
    val diaSelecionado by viewModel.diaSelecionado.collectAsState()

    var abaSelecionada by remember { mutableIntStateOf(0) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var treinoEditando by remember { mutableStateOf<Treino?>(null) }

    val tabs = listOf("Treinos", "Temporizador", "Progresso")

    Scaffold(
        topBar = {
            Column {
                Text(
                    text = "FITPRO",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
                TabRow(selectedTabIndex = abaSelecionada, containerColor = ObsidianBg) {
                    tabs.forEachIndexed { index, titulo ->
                        Tab(
                            selected = abaSelecionada == index,
                            onClick = { abaSelecionada = index },
                            text = { Text(titulo) }
                        )
                    }
                }
            }
        },
        containerColor = ObsidianBg,
        floatingActionButton = {
            if (abaSelecionada == 0) {
                FloatingActionButton(
                    onClick = {
                        treinoEditando = null
                        mostrarDialogo = true
                    },
                    containerColor = CyberTeal
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar")
                }
            }
        }
    ) { padding ->
        when (abaSelecionada) {
            0 -> TreinosTab(
                modifier = Modifier.padding(padding),
                listaTreinos = listaTreinos,
                diaSelecionado = diaSelecionado,
                onSelecionarDia = viewModel::selecionarDiaSemana,
                onExcluir = viewModel::excluirTreino,
                onIncrementar = viewModel::incrementarPeso,
                onDecrementar = viewModel::decrementarPeso,
                onEditar = {
                    treinoEditando = it
                    mostrarDialogo = true
                }
            )

            1 -> TemporizadorTab(
                modifier = Modifier.padding(padding),
                onFinalizar = viewModel::registrarTempoTreino
            )

            else -> ProgressoTab(
                modifier = Modifier.padding(padding),
                listaTreinos = listaTreinos,
                historicoTempos = historicoTempos
            )
        }
    }

    if (mostrarDialogo) {
        DialogoTreino(
            treino = treinoEditando,
            diaPadrao = diaSelecionado,
            aoConfirmar = { nome, peso, usarLbs, tipo, diaSemana ->
                if (treinoEditando == null) {
                    viewModel.adicionarTreino(nome, peso, usarLbs, tipo, diaSemana)
                } else {
                    viewModel.atualizarTreino(treinoEditando!!.id, nome, tipo, diaSemana)
                }
                mostrarDialogo = false
            },
            aoCancelar = { mostrarDialogo = false }
        )
    }
}

@Composable
private fun TreinosTab(
    modifier: Modifier,
    listaTreinos: List<Treino>,
    diaSelecionado: Int,
    onSelecionarDia: (Int) -> Unit,
    onExcluir: (String) -> Unit,
    onIncrementar: (String, Boolean, Double) -> Unit,
    onDecrementar: (String, Boolean, Double) -> Unit,
    onEditar: (Treino) -> Unit
) {
    var filtroTipo by remember { mutableStateOf<TipoExercicio?>(null) }

    val treinosDia = listaTreinos.filter { it.diaSemana == diaSelecionado }
    val filtrados = if (filtroTipo == null) treinosDia else treinosDia.filter { it.tipo == filtroTipo }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Treino salvo por dia da semana",
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(diasSemana()) { dia ->
                FilterChip(
                    selected = diaSelecionado == dia.first,
                    onClick = { onSelecionarDia(dia.first) },
                    label = { Text(dia.second) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberTeal,
                        selectedLabelColor = ObsidianBg
                    )
                )
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = filtroTipo == null,
                    onClick = { filtroTipo = null },
                    label = { Text("Todos") }
                )
            }
            items(TipoExercicio.values()) { tipo ->
                FilterChip(
                    selected = filtroTipo == tipo,
                    onClick = { filtroTipo = tipo },
                    label = { Text(tipo.nomeExibicao) }
                )
            }
        }

        if (filtrados.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Nenhum exercício para ${nomeDiaSemana(diaSelecionado)}.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtrados, key = { it.id }) { treino ->
                    CardTreino(
                        treino = treino,
                        onExcluir = { onExcluir(treino.id) },
                        onIncrementar = { usarLbs, valor -> onIncrementar(treino.id, usarLbs, valor) },
                        onDecrementar = { usarLbs, valor -> onDecrementar(treino.id, usarLbs, valor) },
                        onEditar = { onEditar(treino) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CardTreino(
    treino: Treino,
    onExcluir: () -> Unit,
    onIncrementar: (Boolean, Double) -> Unit,
    onDecrementar: (Boolean, Double) -> Unit,
    onEditar: () -> Unit
) {
    var usarLbs by remember { mutableStateOf(false) }

    Card(colors = CardDefaults.cardColors(containerColor = CardSlate)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(treino.nome, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text(
                        "${treino.tipo.nomeExibicao} • ${nomeDiaSemana(treino.diaSemana)}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                IconButton(onClick = onExcluir) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = CoralRed)
                }
            }

            Text(
                text = if (usarLbs) String.format(Locale.US, "%.1f lbs", treino.obterPesoEmLbs())
                else String.format(Locale.US, "%.1f kg", treino.pesoEmKg),
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(selected = !usarLbs, onClick = { usarLbs = false }, label = { Text("KG") })
                FilterChip(selected = usarLbs, onClick = { usarLbs = true }, label = { Text("LBS") })
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onDecrementar(usarLbs, 1.0) }, colors = ButtonDefaults.buttonColors(containerColor = SurfaceSlate)) {
                    Text("-1")
                }
                Button(onClick = { onIncrementar(usarLbs, 1.0) }) { Text("+1") }
                Button(onClick = { onIncrementar(usarLbs, 5.0) }) { Text("+5") }
            }
        }
    }
}

@Composable
private fun DialogoTreino(
    treino: Treino?,
    diaPadrao: Int,
    aoConfirmar: (String, Double, Boolean, TipoExercicio, Int) -> Unit,
    aoCancelar: () -> Unit
) {
    var nome by remember { mutableStateOf(treino?.nome ?: "") }
    var peso by remember { mutableStateOf(if (treino == null) "" else treino.pesoEmKg.toString()) }
    var usarLbs by remember { mutableStateOf(false) }
    var tipo by remember { mutableStateOf(treino?.tipo ?: TipoExercicio.PEITO) }
    var diaSemana by remember { mutableIntStateOf(treino?.diaSemana ?: diaPadrao) }

    AlertDialog(
        onDismissRequest = aoCancelar,
        title = { Text(if (treino == null) "Adicionar exercício" else "Editar exercício") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") })
                OutlinedTextField(
                    value = peso,
                    onValueChange = { peso = it },
                    label = { Text("Peso") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = treino == null
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !usarLbs, onClick = { usarLbs = false }, label = { Text("KG") })
                    FilterChip(selected = usarLbs, onClick = { usarLbs = true }, label = { Text("LBS") })
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TipoExercicio.values()) { item ->
                        FilterChip(
                            selected = tipo == item,
                            onClick = { tipo = item },
                            label = { Text(item.nomeExibicao) }
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(diasSemana()) { dia ->
                        FilterChip(
                            selected = diaSemana == dia.first,
                            onClick = { diaSemana = dia.first },
                            label = { Text(dia.second) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val valor = peso.toDoubleOrNull() ?: treino?.pesoEmKg ?: -1.0
                if (nome.isNotBlank() && valor >= 0.0) {
                    aoConfirmar(nome, valor, usarLbs, tipo, diaSemana)
                }
            }) {
                Text(if (treino == null) "Adicionar" else "Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = aoCancelar) { Text("Cancelar") }
        }
    )
}

@Composable
private fun TemporizadorTab(
    modifier: Modifier,
    onFinalizar: (TipoTemporizador, Int) -> Unit
) {
    val context = LocalContext.current
    val manager = NotificationManagerCompat.from(context)
    val scope = rememberCoroutineScope()

    var tipo by remember { mutableStateOf(TipoTemporizador.TREINO) }
    var minutosTexto by remember { mutableStateOf("10") }
    var restanteSegundos by remember { mutableIntStateOf(600) }
    var duracaoConfigurada by remember { mutableIntStateOf(600) }
    var rodando by remember { mutableStateOf(false) }
    var timer by remember { mutableStateOf<CountDownTimer?>(null) }

    DisposableEffect(Unit) {
        criarCanalNotificacao(context)
        onDispose {
            timer?.cancel()
            manager.cancel(TIMER_NOTIFICATION_ID)
        }
    }

    fun atualizarNotificacao(segundos: Int) {
        val conteudo = "${tipo.nomeExibicao}: ${formatarDuracao(segundos)}"
        val notification = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setContentTitle("FitPro Temporizador")
            .setContentText(conteudo)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(rodando)
            .setOnlyAlertOnce(true)
            .build()

        manager.notify(TIMER_NOTIFICATION_ID, notification)
    }

    fun iniciar() {
        timer?.cancel()
        timer = object : CountDownTimer((restanteSegundos * 1000L), 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                restanteSegundos = (millisUntilFinished / 1000L).toInt()
                atualizarNotificacao(restanteSegundos)
            }

            override fun onFinish() {
                rodando = false
                restanteSegundos = 0
                atualizarNotificacao(0)
                onFinalizar(tipo, duracaoConfigurada)
            }
        }.start()
        rodando = true
        atualizarNotificacao(restanteSegundos)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Temporizador com notificação na tela de bloqueio",
            color = TextPrimary,
            style = MaterialTheme.typography.titleMedium
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TipoTemporizador.values().forEach { item ->
                FilterChip(
                    selected = tipo == item,
                    onClick = {
                        tipo = item
                        if (!rodando) atualizarNotificacao(restanteSegundos)
                    },
                    label = { Text(item.nomeExibicao) }
                )
            }
        }

        OutlinedTextField(
            value = minutosTexto,
            onValueChange = { minutosTexto = it.filter(Char::isDigit) },
            label = { Text("Minutos") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text(
            text = formatarDuracao(restanteSegundos),
            color = TextPrimary,
            style = MaterialTheme.typography.headlineLarge
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (!rodando) {
                    val minutos = minutosTexto.toIntOrNull()?.coerceIn(1, 180) ?: 10
                    if (restanteSegundos == 0 || restanteSegundos == duracaoConfigurada) {
                        duracaoConfigurada = minutos * 60
                        restanteSegundos = duracaoConfigurada
                    }
                    iniciar()
                }
            }) {
                Text("Iniciar")
            }

            Button(
                onClick = {
                    timer?.cancel()
                    rodando = false
                    atualizarNotificacao(restanteSegundos)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceSlate)
            ) {
                Text("Pausar")
            }

            Button(
                onClick = {
                    timer?.cancel()
                    rodando = false
                    val minutos = minutosTexto.toIntOrNull()?.coerceIn(1, 180) ?: 10
                    duracaoConfigurada = minutos * 60
                    restanteSegundos = duracaoConfigurada
                    manager.cancel(TIMER_NOTIFICATION_ID)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceSlate)
            ) {
                Text("Reset")
            }
        }

        Text(
            text = "Ao finalizar, o tempo é salvo na aba de progresso para comparação.",
            color = TextSecondary
        )

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = {
            scope.launch { manager.cancel(TIMER_NOTIFICATION_ID) }
        }) {
            Text("Ocultar notificação")
        }
    }
}

@Composable
private fun ProgressoTab(
    modifier: Modifier,
    listaTreinos: List<Treino>,
    historicoTempos: List<RegistroTempo>
) {
    val formatador = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Comparação de peso e tempo", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        }

        items(listaTreinos, key = { it.id }) { treino ->
            val historicoOrdenado = treino.historicoPesos.sortedByDescending { it.timestamp }.take(5)
            Card(colors = CardDefaults.cardColors(containerColor = CardSlate)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(treino.nome, color = TextPrimary, fontWeight = FontWeight.Bold)
                    if (historicoOrdenado.size >= 2) {
                        val atual = historicoOrdenado[0].pesoEmKg
                        val anterior = historicoOrdenado[1].pesoEmKg
                        val delta = atual - anterior
                        Text(
                            text = "Variação: ${String.format(Locale.US, "%.1f", delta)} kg",
                            color = if (delta >= 0) CyberTeal else CoralRed
                        )
                    }
                    historicoOrdenado.forEach { registro ->
                        Text(
                            text = "${formatador.format(Date(registro.timestamp))} • ${String.format(Locale.US, "%.1f kg", registro.pesoEmKg)}",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Histórico de tempos", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        }

        if (historicoTempos.isEmpty()) {
            item {
                Text("Ainda não há tempos registrados.", color = TextSecondary)
            }
        } else {
            items(historicoTempos.sortedByDescending { it.timestamp }) { registro ->
                Card(colors = CardDefaults.cardColors(containerColor = CardSlate)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(registro.tipo.nomeExibicao, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text(formatador.format(Date(registro.timestamp)), color = TextSecondary)
                        }
                        Text(formatarDuracao(registro.duracaoSegundos), color = CyberTeal, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun criarCanalNotificacao(context: android.content.Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            TIMER_CHANNEL_ID,
            "Temporizador FitPro",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            description = "Notificações do temporizador para tela de bloqueio"
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}

private fun diasSemana(): List<Pair<Int, String>> = listOf(
    Calendar.SUNDAY to "Dom",
    Calendar.MONDAY to "Seg",
    Calendar.TUESDAY to "Ter",
    Calendar.WEDNESDAY to "Qua",
    Calendar.THURSDAY to "Qui",
    Calendar.FRIDAY to "Sex",
    Calendar.SATURDAY to "Sáb"
)

private fun nomeDiaSemana(diaSemana: Int): String = diasSemana().firstOrNull { it.first == diaSemana }?.second ?: "Dia"

private fun formatarDuracao(totalSegundos: Int): String {
    val minutos = totalSegundos / 60
    val segundos = totalSegundos % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos)
}
