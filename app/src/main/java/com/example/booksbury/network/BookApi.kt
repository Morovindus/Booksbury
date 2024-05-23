package com.example.booksbury.network

import com.example.booksbury.items.Book
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BookApi {
    @GET("api/books/search")
    fun searchBooks(@Query("query") query: String, @Query("language") language: String): Call<ArrayList<Book>>
    @GET("api/books/randomBook/{lang}/{imageType}")
    fun getRandomBook(
        @Path("lang") lang: String,
        @Path("imageType") imageType: String
    ): Call<Book>
}