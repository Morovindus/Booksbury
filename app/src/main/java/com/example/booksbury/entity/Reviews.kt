package com.example.booksbury.entity

// Класс, предоставляющий информацию о отзывах
data class Reviews(
    val _id: Int,
    val nameUser: String,
    val date: String,
    val textUser: String,
    val stars: Int
)