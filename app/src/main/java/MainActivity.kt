package com.application.smartcat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.application.smartcat.ui.telas.TelaCadastro
import com.application.smartcat.ui.telas.TelaLogin
import com.application.smartcat.ui.telas.TelaPrincipal
import com.application.smartcat.ui.theme.SmartCatTheme
import com.google.firebase.FirebaseApp
import com.application.smartcat.util.Sessao

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            SmartCatTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("App SmartCat") }
                        )
                    },
                    modifier = androidx.compose.ui.Modifier.fillMaxSize()
                ) { innerPadding ->
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            TelaLogin(
                                modifier = androidx.compose.ui.Modifier.padding(innerPadding),
                                onLogarClick = { navController.navigate("principal") },
                                onCadastroClick = { navController.navigate("cadastro") }
                            )
                        }
                        composable("cadastro") {
                            TelaCadastro(
                                modifier = androidx.compose.ui.Modifier.padding(innerPadding),
                                onCadastroSucesso = { navController.navigate("login") },
                                onCancelar = { navController.navigate("login") }
                            )
                        }
                        composable("principal") {
                            TelaPrincipal(
                                modifier = androidx.compose.ui.Modifier.padding(innerPadding),
                                onLogoffClick = {
                                    // Limpa a sessão no logoff
                                    Sessao.usuarioAtual = null
                                    navController.navigate("login")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}