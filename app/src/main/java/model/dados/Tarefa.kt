package com.application.smartcat.model.dados

import com.google.firebase.firestore.DocumentId
import com.google.firebase.Timestamp

data class Tarefa(
    @DocumentId val id: String = "",
    val titulo: String = "",
    val descricao: String = "",
    val data: Timestamp? = null
)



