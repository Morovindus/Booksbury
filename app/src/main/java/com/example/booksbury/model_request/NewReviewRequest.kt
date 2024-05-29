package com.example.booksbury.model_request

// Data class NewReviewRequest представляет запрос для добавления нового отзыва
data class NewReviewRequest(
    val _id: Int,
    val nameUser: String,
    val date: String,
    val textUser: String,
    val stars: Int
)