package com.example.booksbury.items

// Класс, предоставляющий информацию о уведомлении
data class Notification(
    val bookId: Int,
    val time: String,
    val imageResource: String
)