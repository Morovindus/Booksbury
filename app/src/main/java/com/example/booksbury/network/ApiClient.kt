package com.example.booksbury.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {
    companion object {
        val instance: BookApi by lazy {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.1.42:3000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            retrofit.create(BookApi::class.java)
        }
    }
}