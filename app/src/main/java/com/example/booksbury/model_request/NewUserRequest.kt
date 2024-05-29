package com.example.booksbury.model_request

// Data class NewUserRequest представляет запрос для добавления нового пользователя
data class NewUserRequest(
    val _id: Int,
    val username: String,
    val email: String,
    val password: String,
    val notifications: List<String> = emptyList(),
    val purchasedBooks: List<String> = emptyList(),
    val favoriteBooks: List<String> = emptyList(),
    val cart: List<String> = emptyList()
)