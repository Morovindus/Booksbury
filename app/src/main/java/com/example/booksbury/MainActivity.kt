package com.example.booksbury


import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.booksbury.databinding.ActivityMainBinding
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var language: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentLocale: Locale = resources.configuration.locale
        language = currentLocale.language

        Log.d("myLogs", language)


    }

    private val ipAddress: String = "192.168.1.42"
    private var idUser: Int = 592708
    fun getIpAddress(): String {
        return ipAddress
    }

    fun setIdUser(id: Int) {
        idUser = id
    }
    fun getIdUser(): Int {
        return idUser
    }

    fun getLanguage(): String {
        return language
    }
}
