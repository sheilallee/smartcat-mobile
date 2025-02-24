package com.application.smartcat.model.dados

import com.google.firebase.firestore.DocumentId

data class Usuario(
    @DocumentId
    val id: String = "",
    val nome: String = "",
    val senha: String = ""
)