package com.example.booksbury.model_request

// Data class UserDetailsResponse представляет ответ с информацией о пользователе (без пароля)
data class UserDetailsResponse(
    val username: String,
    val email: String
)