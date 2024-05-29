package com.example.booksbury.model_request

// Data class FavoriteResponse представляет ответ с сообщением и списком избранных книг
data class FavoriteResponse(
    val message: String,
    val favoriteBooks: List<Int>
)