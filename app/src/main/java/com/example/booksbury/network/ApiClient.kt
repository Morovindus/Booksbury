package com.example.booksbury.network

import com.example.booksbury.interfaces.BookApi
import com.example.booksbury.interfaces.UserApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

// Класс ApiClient содержит реализации Retrofit клиентов для взаимодействия с UserApi и BookApi.
class ApiClient {
    companion object {
        val instanceBook: BookApi by lazy {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.1.42:3000/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            retrofit.create(BookApi::class.java)
        }

        val instanceUser: UserApi by lazy {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.1.42:3000/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            retrofit.create(UserApi::class.java)
        }
    }
}