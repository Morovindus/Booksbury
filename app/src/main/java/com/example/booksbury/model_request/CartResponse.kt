package com.example.booksbury.model_request

// Data class CartResponse представляет ответ с сообщением и списком товаров в корзине
data class CartResponse(
    val message: String,
    val cart: List<Int>
)