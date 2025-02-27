package com.application.smartcat.ui.telas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.application.smartcat.model.dados.Tarefa
import com.application.smartcat.model.dados.TarefaDAO
import com.application.smartcat.util.formatInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

@Composable
fun TelaPrincipal(
    modifier: Modifier = Modifier,
    onLogoffClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tarefaDAO = remember { TarefaDAO() }
    var tarefas by remember { mutableStateOf<List<Tarefa>>(emptyList()) }
    var showCadastroModal by remember { mutableStateOf(false) }
    var tarefaSelecionada by remember { mutableStateOf<Tarefa?>(null) }
    var barraDePesquisaConsulta by remember { mutableStateOf("") }

    LaunchedEffect(tarefaDAO) {
        tarefaDAO.buscar { lista ->
            tarefas = lista
        }
    }

    val barraDePesquisaState = rememberUpdatedState(barraDePesquisaConsulta)

    val tarefasFiltradas = tarefas.filter { tarefa ->
        barraDePesquisaState.value.isBlank() || tarefa.titulo.contains(barraDePesquisaState.value, ignoreCase = true) ||
                tarefa.descricao.contains(barraDePesquisaState.value, ignoreCase = true) ||
                (tarefa.data != null && formatInstant(
                    Instant.fromEpochSeconds(tarefa.data.seconds, tarefa.data.nanoseconds)
                ).contains(barraDePesquisaState.value, ignoreCase = true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF8E8CCC))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bem-vindo(a) de volta!\n\nSuas próximas tarefas:",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.weight(1f)
            )

            FloatingActionButton(
                onClick = { onLogoffClick() },
                containerColor = Color(0xFFD0CFEA),
                contentColor = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Sair"
                )
            }
        }

        OutlinedTextField(
            value = barraDePesquisaConsulta,
            onValueChange = { barraDePesquisaConsulta = it },
            placeholder = { Text("Filtre por título, descrição ou data", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFD0CFEA),
                unfocusedContainerColor = Color(0xFFD0CFEA),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(tarefasFiltradas) { tarefa ->
                TarefaCard(
                    tarefa = tarefa,
                    onEdit = {
                        tarefaSelecionada = tarefa
                        showCadastroModal = true
                    },
                    onDelete = {
                        scope.launch(Dispatchers.IO) {
                            tarefaDAO.remover(tarefa.id) { sucesso ->
                                scope.launch(Dispatchers.Main) {
                                    if (sucesso) {
                                        tarefas = tarefas.filter { it.id != tarefa.id }
                                    } else {
                                        Toast.makeText(context, "Erro ao remover tarefa", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }

        FloatingActionButton(
            onClick = {
                tarefaSelecionada = null
                showCadastroModal = true
            },
            containerColor = Color(0xFFD0CFEA),
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Adicionar Tarefa"
            )
        }
    }

    if (showCadastroModal) {
        AlertDialog(
            onDismissRequest = { showCadastroModal = false },
            title = {
                Text(
                    text = if (tarefaSelecionada == null) "Adicionar Tarefa" else "Editar Tarefa",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                TelaCadastroTarefa(
                    tarefaParaEditar = tarefaSelecionada,
                    onCadastroSucesso = {
                        tarefaDAO.buscar { lista ->
                            tarefas = lista
                        }
                        showCadastroModal = false
                    },
                    onCancelar = { showCadastroModal = false }
                )
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
fun TarefaCard(tarefa: Tarefa, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD0CFEA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Título: ${tarefa.titulo}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Descrição: ${tarefa.descricao}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Data: " + if (tarefa.data != null) {
                    val instant = Instant.fromEpochSeconds(tarefa.data.seconds, tarefa.data.nanoseconds)
                    formatInstant(instant)
                } else "Sem data",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar tarefa"
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remover tarefa"
                    )
                }
            }
        }
    }
}
