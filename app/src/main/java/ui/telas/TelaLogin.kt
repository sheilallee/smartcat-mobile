package com.application.smartcat.ui.telas

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.application.smartcat.model.dados.UsuarioDAO
import com.application.smartcat.util.Sessao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TelaLogin(modifier: Modifier = Modifier, onSigninClick: () -> Unit, onCadastroClick: () -> Unit) {
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
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = { senha = it },
            label = { Text(text = "Senha") }
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(modifier = Modifier.weight(1f), onClick = {
                scope.launch(Dispatchers.IO) {
                    // Busca o usuário pelo nome. O objeto retornado conterá o ID do documento graças ao @DocumentId.
                    UsuarioDAO().buscarPorNome(nome) { usuario ->
                        if (usuario != null && usuario.senha == senha) {
                            // Armazena o usuário na sessão para uso interno (não visível ao usuário)
                            Sessao.usuarioAtual = usuario
                            onSigninClick()
                        } else {
                            mensagemErro = "Nome ou senha inválidos!"
                        }
                    }
                }
            }) {
                Text("Entrar")
            }

            Button(modifier = Modifier.weight(1f), onClick = {
                onCadastroClick()
            }) {
                Text("Cadastrar")
            }
        }

        mensagemErro?.let {
            LaunchedEffect(it) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                mensagemErro = null
            }
        }
    }
}



