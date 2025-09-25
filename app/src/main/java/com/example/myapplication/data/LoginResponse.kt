package com.example.myapplication.data

// Classe de dados ajustada para a tabela 'Usuarios'
// Não há CPF, então foi removido. 'usuarioNome' virá da coluna 'Usuario'.
data class LoginResponse(
    val usuarioId: Int,
    val usuarioNome: String,
    val usuarioEmail: String
)