package com.application.smartcat.ui.telas

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.application.smartcat.model.dados.Usuario
import com.application.smartcat.model.dados.UsuarioDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TelaCadastro(modifier: Modifier = Modifier, onCadastroSucesso: () -> Unit, onCancelar: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nome by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mensagemErro by remember { mutableStateOf<String?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text(text = "Nome") })
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text(text = "Senha") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    if (nome.isNotEmpty() && senha.isNotEmpty()) {
                        scope.launch(Dispatchers.IO) {
                            val usuario = Usuario(nome = nome, senha = senha)
                            UsuarioDAO().adicionar(usuario) { usuarioAdicionado ->
                                if (usuarioAdicionado != null) {
                                    onCadastroSucesso()
                                } else {
                                    mensagemErro = "Falha ao criar sua conta. Tente novamente!"
                                }
                            }
                        }
                    } else {
                        mensagemErro = "Nome e senha são obrigatórios para continuar!"
                    }
                }
            ) {
                Text(text = "Cadastrar")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                modifier = Modifier.weight(1f),
                onClick = { onCancelar() }
            ) {
                Text(text = "Cancelar")
            }
        }

        mensagemErro?.let { erro ->
            LaunchedEffect(erro) {
                Toast.makeText(context, erro, Toast.LENGTH_SHORT).show()
                mensagemErro = null
            }
        }
    }
}




