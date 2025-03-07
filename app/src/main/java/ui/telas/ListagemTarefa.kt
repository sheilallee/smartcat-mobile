package com.application.smartcat.ui.telas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.application.smartcat.model.dados.Tarefa
import com.application.smartcat.model.dados.TarefaDAO
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@Composable
fun ListagemTarefa(navController: NavController) {
    val scope = rememberCoroutineScope()
    val tarefaDAO = remember { TarefaDAO() }
    var tarefas by remember { mutableStateOf<List<Tarefa>>(emptyList()) }
    val pagerState = rememberPagerState()

    LaunchedEffect(Unit) { tarefaDAO.buscar { tarefas = it } }

    val categorias = listOf("A Fazer", "Concluído")
    val statusMap = mapOf("A Fazer" to 1, "Concluído" to 2)

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF8E8CCC))) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Categoria: ${categorias[pagerState.currentPage]}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .height(50.dp)
                    .background(
                        color = Color(0xFFB39DDB),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            TabRow(selectedTabIndex = pagerState.currentPage) {
                categorias.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) }
                    )
                }
            }

            HorizontalPager(count = categorias.size, state = pagerState) { page ->
                val status = statusMap[categorias[page]] ?: 1
                val tarefasFiltradas = tarefas.filter { it.status == status }

                Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(tarefasFiltradas, key = { it.id }) { tarefa ->
                            TarefaCard(
                                tarefa = tarefa,
                                onEdit = { navController.navigate("editarTarefa/${tarefa.id}") },
                                onDelete = { navController.navigate("removerTarefa/${tarefa.id}") },
                                onChangeStatus = { novoStatus ->
                                    tarefaDAO.moverTarefa(tarefa.id, novoStatus) { sucesso ->
                                        if (sucesso) {
                                            tarefaDAO.buscar { tarefas = it }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFFD0CFEA)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
        }
    }
}


/*
package com.application.smartcat.ui.telas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.application.smartcat.model.dados.Tarefa
import com.application.smartcat.model.dados.TarefaDAO
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ListagemTarefa(navController: NavController) {
    val scope = rememberCoroutineScope()
    val tarefaDAO = remember { TarefaDAO() }
    var tarefas by remember { mutableStateOf<List<Tarefa>>(emptyList()) }
    val pagerState = rememberPagerState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { tarefaDAO.buscar { tarefas = it } }

    val categorias = listOf("A Fazer", "Em Andamento", "Concluído")
    val statusMap = mapOf("A Fazer" to 1, "Em Andamento" to 2, "Concluído" to 3)

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF8E8CCC))) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            //
            Text(
                text = "Categoria: ${categorias[pagerState.currentPage]}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .height(50.dp)
                    .background(
                        color = Color(0xFFB39DDB),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            //
            TabRow(selectedTabIndex = pagerState.currentPage) {
                categorias.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) }
                    )
                }
            }

            //
            HorizontalPager(count = categorias.size, state = pagerState) { page ->
                val status = statusMap[categorias[page]] ?: 1
                val tarefasFiltradas = tarefas.filter { it.status == status }

                // **Adiciona Scroll Vertical para Listas Longas**
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(tarefasFiltradas, key = { it.id }) { tarefa ->
                            TarefaCard(
                                tarefa = tarefa,
                                onEdit = { navController.navigate("editarTarefa/${tarefa.id}") },
                                onDelete = { navController.navigate("removerTarefa/${tarefa.id}") },
                                onChangeStatus = { novoStatus ->
                                    if (tarefa.status == 3) {
                                        Toast.makeText(
                                            context,
                                            "Tarefa já concluída, não pode ser movida!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        tarefaDAO.moverTarefa(tarefa.id, novoStatus) { sucesso ->
                                            if (sucesso) {
                                                tarefaDAO.buscar { tarefas = it }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        //
        FloatingActionButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFFD0CFEA)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
        }
    }
}

*/


/*
package com.application.smartcat.ui.telas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.application.smartcat.model.dados.Tarefa
import com.application.smartcat.model.dados.TarefaDAO
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ListagemTarefa(navController: NavController) {
    val scope = rememberCoroutineScope()
    val tarefaDAO = remember { TarefaDAO() }
    var tarefas by remember { mutableStateOf<List<Tarefa>>(emptyList()) }
    val pagerState = rememberPagerState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { tarefaDAO.buscar { tarefas = it } }

    val categorias = listOf("A Fazer", "Em Andamento", "Concluído")
    val statusMap = mapOf("A Fazer" to 1, "Em Andamento" to 2, "Concluído" to 3)

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF8E8CCC))) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            //
            Text(
                text = "Categoria: ${categorias[pagerState.currentPage]}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .height(50.dp)
                    .background(
                        color = Color(0xFFB39DDB),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            //
            TabRow(selectedTabIndex = pagerState.currentPage) {
                categorias.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) }
                    )
                }
            }

            //
            HorizontalPager(count = categorias.size, state = pagerState) { page ->
                val status = statusMap[categorias[page]] ?: 1
                val tarefasFiltradas = tarefas.filter { it.status == status }

                // **Adiciona Scroll Vertical para Listas Longas**
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(tarefasFiltradas, key = { it.id }) { tarefa ->
                            TarefaCard(
                                tarefa = tarefa,
                                onEdit = { navController.navigate("editarTarefa/${tarefa.id}") },
                                onDelete = { navController.navigate("removerTarefa/${tarefa.id}") },
                                onChangeStatus = { novoStatus ->
                                    if (tarefa.status == 3) {
                                        Toast.makeText(
                                            context,
                                            "Tarefa já concluída, não pode ser movida!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        tarefaDAO.moverTarefa(tarefa.id, novoStatus) { sucesso ->
                                            if (sucesso) {
                                                tarefaDAO.buscar { tarefas = it }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        //
        FloatingActionButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFFD0CFEA)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
        }
    }
}
*/


/*
package com.application.smartcat.ui.telas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.application.smartcat.model.dados.Tarefa
import com.application.smartcat.model.dados.TarefaDAO
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ListagemTarefa(navController: NavController) {
    val scope = rememberCoroutineScope()
    val tarefaDAO = remember { TarefaDAO() }
    var tarefas by remember { mutableStateOf<List<Tarefa>>(emptyList()) }
    val pagerState = rememberPagerState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { tarefaDAO.buscar { tarefas = it } }

    val categorias = listOf("A Fazer", "Em Andamento", "Concluído")
    val statusMap = mapOf("A Fazer" to 1, "Em Andamento" to 2, "Concluído" to 3)

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF8E8CCC))) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Categoria: ${categorias[pagerState.currentPage]}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp).height(50.dp)
                .background(
                        color = Color(0xFFB39DDB),
                shape = RoundedCornerShape(16.dp)
            )
                .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            TabRow(selectedTabIndex = pagerState.currentPage) {
                categorias.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) }
                    )
                }
            }

            HorizontalPager(count = categorias.size, state = pagerState) { page ->
                val status = statusMap[categorias[page]] ?: 1
                val tarefasFiltradas = tarefas.filter { it.status == status }
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    items(tarefasFiltradas, key = { it.id }) { tarefa ->
                        TarefaCard(
                            tarefa = tarefa,
                            onEdit = { navController.navigate("editarTarefa/${tarefa.id}") },
                            onDelete = { navController.navigate("removerTarefa/${tarefa.id}") },
                            onDrag = if (tarefa.status == 3) {
                                {
                                    Toast.makeText(
                                        context,
                                        "Tarefa já concluída, não pode ser movida!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else { direcao ->
                                val novoStatus = when {
                                    // Se está na aba "A Fazer" (1), só pode ir para "Em Andamento" (2)
                                    status == 1 && direcao == "direita" -> 2
                                    // Se está na aba "Em Andamento" (2), pode ir para qualquer lado
                                    status == 2 && direcao == "direita" -> 3
                                    status == 2 && direcao == "esquerda" -> 1
                                    // Se está na aba "Concluído" (3), não pode mover
                                    else -> status
                                }

                                if (novoStatus != status) {
                                    tarefaDAO.moverTarefa(tarefa.id, novoStatus) { sucesso ->
                                        if (sucesso) {
                                            tarefaDAO.buscar { tarefas = it }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFFD0CFEA)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
        }
    }
}


*/
