package com.example.booksbury.model_request

// Data class UserResponse представляет ответ с информацией о пользователе
data class UserResponse(
    val _id: Int,
    val username: String,
    val email: String,
    val password: String,
    val notifications: List<String>,
    val purchasedBooks: List<String>,
    val favoriteBooks: List<String>,
    val cart: List<String>
)