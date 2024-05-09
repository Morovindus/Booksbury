package com.example.booksbury


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.booksbury.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
}
