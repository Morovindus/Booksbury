package com.example.booksbury.items

// Класс, предоставляющий информацию о отзывах
data class Reviews(
    val id: Int,
    val nameUser: String,
    val date: String,
    val textUser: String,
    val stars: Int
)