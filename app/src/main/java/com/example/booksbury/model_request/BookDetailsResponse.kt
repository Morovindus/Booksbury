package com.example.booksbury.model_request

// Data class BookDetailsResponse представляет ответ с подробной информацией о книге
data class BookDetailsResponse(
    val _id: String,
    val price: Double,
    val released: String,
    val part: String,
    val page: String,
    val title: String,
    val authorName: String,
    val images: String
)