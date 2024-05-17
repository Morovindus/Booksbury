package com.example.booksbury


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.booksbury.databinding.ActivityMainBinding
import java.util.Locale


// Класс главной активности
class MainActivity : AppCompatActivity() {

    // Приватное свойство для хранения привязки к макету активности
    private lateinit var binding: ActivityMainBinding

    // Публичные свойства для IP-адреса, ID книги, ID пользователя и языка
    private val ipAddress: String = "192.168.1.42"
    private var idUser: Int = 0
    private var idBook: Int = 0
    private var language: String = ""

    // Метод, вызываемый при создании активности
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация привязки к макету и установка макета для активности
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получение текущей локализации и установка языка
        val currentLocale: Locale = resources.configuration.locale
        language = currentLocale.language
    }

    // Метод для получения IP-адреса
    fun getIpAddress(): String {
        return ipAddress
    }

    // Методы для установки и получения ID пользователя
    fun setIdUser(id: Int) {
        idUser = id
    }
    fun getIdUser(): Int {
        return idUser
    }

    // Метод для получения языка
    fun getLanguage(): String {
        return language
    }

    // Методы для установки и получения ID книги
    fun setIdBook(id: Int) {
        idBook = id
    }
    fun getIdBook(): Int {
        return idBook
    }

    // Метод, для сохранения данных при повороте экрана
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("idUser", idUser)
        outState.putString("language", language)
        outState.putInt("idBook", idBook)
    }

    // Метод, для восстановления данных при повороте экрана
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        idUser = savedInstanceState.getInt("idUser", 0)
        language = savedInstanceState.getString("language", "")
        idBook = savedInstanceState.getInt("idBook", 0)
    }
}