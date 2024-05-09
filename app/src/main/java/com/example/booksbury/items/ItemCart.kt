package com.example.booksbury.items
data class ItemCart(
    val id: Int,
    val imageResource: String,
    val titleBook: String,
    val nameAuthor: String,
    val stars: Int,
    val ratings: Int,
    val price: Int
)