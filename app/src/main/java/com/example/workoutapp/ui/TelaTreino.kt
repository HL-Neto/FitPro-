package com.example.workoutapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workoutapp.TreinoViewModel
import com.example.workoutapp.model.TipoExercicio
import com.example.workoutapp.model.Treino
import com.example.workoutapp.ui.theme.*
import java.util.Locale
import kotlin.OptIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaTreino(viewModel: TreinoViewModel) {
    val listaTreinos by viewModel.listaTreinos.collectAsState()
    var tipoFiltroSelecionado by remember { mutableStateOf<TipoExercicio?>(null) }
    var mostrarDialogoAdicionar by remember { mutableStateOf(false) }

    // Filtragem da lista
    val listaFiltrada = remember(listaTreinos, tipoFiltroSelecionado) {
        if (tipoFiltroSelecionado == null) {
            listaTreinos
        } else {
            listaTreinos.filter { it.tipo == tipoFiltroSelecionado }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MEU TREINO",
                        style = Typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                },
                actions = {
                    // Botão para resetar exemplos caso a lista fique vazia
                    IconButton(
                        onClick = { viewModel.limparTreinos() },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = CoralRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Limpar tudo"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBg
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoAdicionar = true },
                containerColor = CyberTeal,
                contentColor = ObsidianBg,
                shape = CircleShape,
                modifier = Modifier
                    .padding(16.dp)
                    .border(2.dp, ElectricBlue, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar Treino",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        containerColor = ObsidianBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Dashboard de Resumo do Treino
            CardResumo(listaTreinos)

            Spacer(modifier = Modifier.height(16.dp))

            // Seletor de Categoria (Filtro)
            Text(
                text = "Categorias",
                style = Typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SeletorFiltroCategorias(
                tipoSelecionado = tipoFiltroSelecionado,
                aoSelecionar = { tipoFiltroSelecionado = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cabeçalho da Lista
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Exercícios (${listaFiltrada.size})",
                    style = Typography.titleLarge,
                    color = TextPrimary
                )
                if (listaTreinos.isEmpty()) {
                    TextButton(
                        onClick = { 
                            viewModel.adicionarTreino("Supino Reto", 60.0, false, TipoExercicio.PEITO)
                            viewModel.adicionarTreino("Rosca Direta", 15.0, false, TipoExercicio.BRACO)
                            viewModel.adicionarTreino("Puxada Pulley", 50.0, false, TipoExercicio.COSTAS)
                            viewModel.adicionarTreino("Agachamento Livre", 80.0, false, TipoExercicio.PERNAS)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = CyberTeal)
                    ) {
                        Text("Carregar Exemplos")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de Treinos
            if (listaFiltrada.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (listaTreinos.isEmpty()) 
                            "Nenhum exercício cadastrado.\nClique no botão + abaixo para começar!" 
                            else "Nenhum exercício encontrado nesta categoria.",
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        style = Typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(
                        items = listaFiltrada,
                        key = { it.id }
                    ) { treino ->
                        CardTreino(
                            treino = treino,
                            aoIncrementar = { usarLbs, valor -> 
                                viewModel.incrementarPeso(treino.id, usarLbs, valor) 
                            },
                            aoDecrementar = { usarLbs, valor -> 
                                viewModel.decrementarPeso(treino.id, usarLbs, valor) 
                            },
                            aoExcluir = { viewModel.excluirTreino(treino.id) }
                        )
                    }
                }
            }
        }
    }

    // Modal de Cadastro de Treino
    if (mostrarDialogoAdicionar) {
        DialogoAdicionarTreino(
            aoConfirmar = { nome, peso, usarLbs, tipo ->
                viewModel.adicionarTreino(nome, peso, usarLbs, tipo)
                mostrarDialogoAdicionar = false
            },
            aoCancelar = { mostrarDialogoAdicionar = false }
        )
    }
}

/**
 * Card superior com o resumo do progresso/estatísticas.
 */
@Composable
fun CardResumo(listaTreinos: List<Treino>) {
    val totalExercicios = listaTreinos.size
    val pesoMedioKg = if (listaTreinos.isEmpty()) 0.0 else listaTreinos.map { it.pesoEmKg }.average()
    val pesoMedioLbs = pesoMedioKg * Treino.FATOR_CONVERSAO_LBS

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(CyberTeal, ElectricBlue)),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = CardSlate)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CardSlate,
                            ObsidianBg
                        )
                    )
                )
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RESUMO GERAL",
                    style = Typography.labelMedium,
                    color = CyberTeal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Progresso Ativo",
                    style = Typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = totalExercicios.toString(),
                        style = Typography.headlineMedium,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Treinos",
                        style = Typography.labelMedium,
                        color = TextSecondary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%.1f", pesoMedioKg),
                        style = Typography.headlineMedium,
                        color = CyberTeal,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Méd. Kg",
                        style = Typography.labelMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Filtro horizontal de categorias.
 */
@Composable
fun SeletorFiltroCategorias(
    tipoSelecionado: TipoExercicio?,
    aoSelecionar: (TipoExercicio?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = tipoSelecionado == null,
                onClick = { aoSelecionar(null) },
                label = { Text("Todos") },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CardSlate,
                    labelColor = TextSecondary,
                    selectedContainerColor = CyberTeal,
                    selectedLabelColor = ObsidianBg
                )
            )
        }
        items(TipoExercicio.values()) { tipo ->
            FilterChip(
                selected = tipoSelecionado == tipo,
                onClick = { aoSelecionar(tipo) },
                label = { Text(tipo.nomeExibicao) },
                leadingIcon = {
                    Icon(
                        imageVector = obterIconeCategoria(tipo),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CardSlate,
                    labelColor = TextSecondary,
                    selectedContainerColor = obterCorCategoria(tipo),
                    selectedLabelColor = ObsidianBg
                )
            )
        }
    }
}

/**
 * Card individual de exercício (Treino).
 */
@Composable
fun CardTreino(
    treino: Treino,
    aoIncrementar: (usarLbs: Boolean, valor: Double) -> Unit,
    aoDecrementar: (usarLbs: Boolean, valor: Double) -> Unit,
    aoExcluir: () -> Unit
) {
    var usarLbsParaAjuste by remember { mutableStateOf(false) }
    val corCategoria = obterCorCategoria(treino.tipo)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, TextDisabled.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSlate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabeçalho: Nome do exercício, Categoria e Excluir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = treino.nome,
                        style = Typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Badge da categoria
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(corCategoria.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = obterIconeCategoria(treino.tipo),
                            contentDescription = null,
                            tint = corCategoria,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = treino.tipo.nomeExibicao.uppercase(),
                            color = corCategoria,
                            style = Typography.labelMedium.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Botão Excluir
                IconButton(
                    onClick = aoExcluir,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = CoralRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir Treino",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seção de Exibição dos Pesos (KG e LBS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Caixa KG
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!usarLbsParaAjuste) corCategoria.copy(alpha = 0.1f) else SurfaceSlate)
                        .border(
                            1.dp,
                            if (!usarLbsParaAjuste) corCategoria else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { usarLbsParaAjuste = false }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.1f", treino.pesoEmKg),
                            style = Typography.headlineMedium,
                            color = if (!usarLbsParaAjuste) TextPrimary else TextSecondary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "KILOGRAMAS",
                            style = Typography.labelMedium,
                            color = if (!usarLbsParaAjuste) corCategoria else TextDisabled,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Caixa LBS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (usarLbsParaAjuste) corCategoria.copy(alpha = 0.1f) else SurfaceSlate)
                        .border(
                            1.dp,
                            if (usarLbsParaAjuste) corCategoria else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { usarLbsParaAjuste = true }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.1f", treino.obterPesoEmLbs()),
                            style = Typography.headlineMedium,
                            color = if (usarLbsParaAjuste) TextPrimary else TextSecondary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "LIBRAS (LBS)",
                            style = Typography.labelMedium,
                            color = if (usarLbsParaAjuste) corCategoria else TextDisabled,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seção de Controles de Ajuste de Peso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ajustar peso em: ${if (usarLbsParaAjuste) "Lbs" else "Kg"}",
                    style = Typography.bodyMedium,
                    color = TextSecondary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botões de Decremento
                    Button(
                        onClick = { aoDecrementar(usarLbsParaAjuste, 1.0) },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceSlate),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("-1", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { aoDecrementar(usarLbsParaAjuste, 5.0) },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceSlate),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("-5", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }

                    // Botões de Incremento
                    Button(
                        onClick = { aoIncrementar(usarLbsParaAjuste, 1.0) },
                        colors = ButtonDefaults.buttonColors(containerColor = corCategoria),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+1", color = ObsidianBg, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { aoIncrementar(usarLbsParaAjuste, 5.0) },
                        colors = ButtonDefaults.buttonColors(containerColor = corCategoria),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+5", color = ObsidianBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Diálogo flutuante para criação de um novo treino.
 */
@Composable
fun DialogoAdicionarTreino(
    aoConfirmar: (nome: String, peso: Double, usarLbs: Boolean, tipo: TipoExercicio) -> Unit,
    aoCancelar: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var pesoStr by remember { mutableStateOf("") }
    var usarLbs by remember { mutableStateOf(false) }
    var tipoSelecionado by remember { mutableStateOf(TipoExercicio.PEITO) }
    var erroNome by remember { mutableStateOf(false) }
    var erroPeso by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = aoCancelar,
        containerColor = SurfaceSlate,
        title = {
            Text(
                "NOVO EXERCÍCIO",
                style = Typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nome do exercício
                OutlinedTextField(
                    value = nome,
                    onValueChange = {
                        nome = it
                        erroNome = false
                    },
                    label = { Text("Nome do Exercício") },
                    isError = erroNome,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberTeal,
                        focusedLabelColor = CyberTeal,
                        unfocusedTextColor = TextPrimary,
                        focusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (erroNome) {
                    Text("O nome é obrigatório", color = CoralRed, style = Typography.labelMedium)
                }

                // Linha de Peso e Unidade
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = pesoStr,
                        onValueChange = {
                            pesoStr = it
                            erroPeso = false
                        },
                        label = { Text("Peso") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = erroPeso,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberTeal,
                            focusedLabelColor = CyberTeal,
                            unfocusedTextColor = TextPrimary,
                            focusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Seletor de unidade KG / LBS
                    Row(
                        modifier = Modifier
                            .height(56.dp)
                            .border(1.dp, TextSecondary, RoundedCornerShape(4.dp))
                            .clip(RoundedCornerShape(4.dp))
                            .background(CardSlate)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(if (!usarLbs) CyberTeal else Color.Transparent)
                                .clickable { usarLbs = false }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "KG",
                                color = if (!usarLbs) ObsidianBg else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(if (usarLbs) CyberTeal else Color.Transparent)
                                .clickable { usarLbs = true }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "LBS",
                                color = if (usarLbs) ObsidianBg else TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (erroPeso) {
                    Text("Informe um peso numérico válido", color = CoralRed, style = Typography.labelMedium)
                }

                // Categoria (Tipo)
                Text("Tipo de Exercício", style = Typography.bodyLarge, color = TextPrimary)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TipoExercicio.values().forEach { tipo ->
                        val selecionado = tipoSelecionado == tipo
                        val cor = obterCorCategoria(tipo)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = if (selecionado) cor else TextDisabled,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    if (selecionado) cor.copy(alpha = 0.15f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { tipoSelecionado = tipo }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = obterIconeCategoria(tipo),
                                    contentDescription = null,
                                    tint = if (selecionado) cor else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tipo.nomeExibicao,
                                    style = Typography.labelMedium,
                                    color = if (selecionado) TextPrimary else TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nomeValido = nome.isNotBlank()
                    val pesoDouble = pesoStr.toDoubleOrNull()
                    val pesoValido = pesoDouble != null && pesoDouble >= 0.0

                    erroNome = !nomeValido
                    erroPeso = !pesoValido

                    if (nomeValido && pesoValido) {
                        aoConfirmar(nome, pesoDouble!!, usarLbs, tipoSelecionado)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberTeal)
            ) {
                Text("Adicionar", color = ObsidianBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = aoCancelar) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

/**
 * Retorna uma cor específica para identificar cada tipo de exercício.
 */
fun obterCorCategoria(tipo: TipoExercicio): Color {
    return when (tipo) {
        TipoExercicio.PEITO -> Color(0xFFFF4D4D)   // Vermelho energético
        TipoExercicio.BRACO -> CyberTeal           // Ciano cibernético
        TipoExercicio.COSTAS -> Color(0xFFFFB300)  // Laranja/Ouro
        TipoExercicio.PERNAS -> Color(0xFF4CAF50)  // Verde atlético
    }
}

/**
 * Retorna um ícone específico para cada tipo de exercício.
 */
fun obterIconeCategoria(tipo: TipoExercicio): ImageVector {
    return when (tipo) {
        TipoExercicio.PEITO -> Icons.Default.FavoriteBorder // Próximo ao peito/coração
        TipoExercicio.BRACO -> Icons.Default.FitnessCenter   // Halteres
        TipoExercicio.COSTAS -> Icons.Default.AccessibilityNew // Costas/Postura corporal
        TipoExercicio.PERNAS -> Icons.Default.DirectionsRun // Movimento de pernas
    }
}
