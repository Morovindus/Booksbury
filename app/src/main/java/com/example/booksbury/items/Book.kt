package com.example.booksbury.items

// Класс, предоставляющий информацию о книге
data class Book(
    val id: Int,
    val imageResource: String,
    val titleBook: String,
    val nameAuthor: String,
    val stars: Int,
    val ratings: Int,
    val price: Int
)