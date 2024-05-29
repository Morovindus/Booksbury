package com.example.booksbury.model_request

// Data class AuthorInfoResponse представляет ответ с информацией об авторе
data class AuthorInfoResponse(
    val authorName: String,
    val authorAbout: String,
    val authorImage: String
)