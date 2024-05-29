package com.example.booksbury.entity

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