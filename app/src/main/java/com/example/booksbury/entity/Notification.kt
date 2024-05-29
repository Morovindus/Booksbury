package com.example.booksbury.entity

// Класс, предоставляющий информацию о уведомлении
data class Notification(
    val bookId: Int,
    val time: String,
    val image: String
)