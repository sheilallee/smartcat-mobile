package com.application.smartcat.ui.telas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(tarefaDAO) {
        tarefaDAO.buscar { lista ->
            tarefas = lista
        }
    }

    val searchQueryState = rememberUpdatedState(searchQuery)

    val tarefasFiltradas = tarefas.filter { tarefa ->
        searchQueryState.value.isBlank() || tarefa.titulo.contains(searchQueryState.value, ignoreCase = true) ||
                tarefa.descricao.contains(searchQueryState.value, ignoreCase = true) ||
                (tarefa.data != null && formatInstant(
                    Instant.fromEpochSeconds(tarefa.data.seconds, tarefa.data.nanoseconds)
                ).contains(searchQueryState.value, ignoreCase = true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF8E8CCC))
            .padding(16.dp)
    ) {
        Text(
            text = "Bem-vindo(a) de volta!\n\nSuas próximas tarefas:",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Barra de busca
//        OutlinedTextField(
//            value = searchQuery,
//            onValueChange = { searchQuery = it },
//            label = { Text("Buscar tarefas") },
//            modifier = Modifier.fillMaxWidth(),
//            singleLine = true
//        )
//        Spacer(modifier = Modifier.height(16.dp))

        // Barra de busca destacada e filtro aprimorado
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Buscar por título") },
            modifier = Modifier
                .fillMaxWidth()
                    .background(Color(0xFFD0CFEA), shape = RoundedCornerShape(4.dp)),
//                    .padding(4.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFD0CFEA),
                unfocusedContainerColor = Color(0xFFD0CFEA),

                focusedLabelColor = Color.White,

                focusedTextColor = Color.White,
                focusedIndicatorColor = Color.White
            ),
            singleLine = true
        )


        // Atualização do filtro para considerar apenas títulos
        val tarefasFiltradas = tarefas.filter { tarefa ->
            searchQuery.isBlank() || tarefa.titulo.contains(searchQuery, ignoreCase = true)
        }


        // Lista de tarefas com LazyColumn dinâmica
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
        Spacer(modifier = Modifier.height(16.dp))

        FloatingActionButton(
            onClick = {
                tarefaSelecionada = null
                showCadastroModal = true
            },
            containerColor = Color(0xFF8E8CCC),
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("+ Adicionar")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onLogoffClick() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sair")
        }
    }

    if (showCadastroModal) {
        AlertDialog(
            onDismissRequest = { showCadastroModal = false },
            title = {
                Text(text = if (tarefaSelecionada == null) "Adicionar Tarefa" else "Alterar Tarefa")
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