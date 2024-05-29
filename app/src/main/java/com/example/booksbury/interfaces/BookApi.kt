package com.example.booksbury.interfaces

import com.example.booksbury.entity.Book
import com.example.booksbury.entity.Reviews
import com.example.booksbury.model_request.AuthorInfoResponse
import com.example.booksbury.model_request.BookDetailsResponse
import com.example.booksbury.model_request.NewReviewRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Интерфейс BookApi содержит определения запросов, связанных с книгами
interface BookApi {
    @GET("api/books/search")
    fun searchBooks(@Query("query") query: String, @Query("language") language: String): Call<ArrayList<Book>>
    @GET("api/books/randomBook/{lang}/{imageType}")
    fun getRandomBook(
        @Path("lang") lang: String,
        @Path("imageType") imageType: String
    ): Call<Book>

    @GET("api/books/{id}/author/{lang}")
    fun getAuthorInfo(
        @Path("id") bookId: Int,
        @Path("lang") lang: String
    ): Call<AuthorInfoResponse>

    @GET("api/books/{id}/details/{lang}")
    fun getBookDetails(
        @Path("id") bookId: Int,
        @Path("lang") lang: String
    ): Call<String>

    @GET("api/books/{id}/synopsis/{lang}")
    fun getBookSynopsis(
        @Path("id") bookId: Int,
        @Path("lang") lang: String
    ): Call<String>

    @GET("api/books/more/{id}/{lang}")
    fun getBookMoreDetails(
        @Path("id") bookId: Int,
        @Path("lang") lang: String
    ): Call<BookDetailsResponse>

    @POST("api/books/{bookId}/reviews")
    fun addReview(
        @Path("bookId") bookId: Int,
        @Body newReview: NewReviewRequest
    ): Call<ResponseBody>

    @GET("api/books/{id}/reviews")
    fun getBookReviews(@Path("id") bookId: Int): Call<List<Reviews>>

    @GET("api/users/titleBook/{id}/{lang}")
    fun getTitleBook(
        @Path("id") bookId: Int,
        @Path("lang") lang: String
    ): Call<String>
    @GET("book/bookById/{bookId}/{lang}/smallCover")
    fun getPurchasedBook(
        @Path("bookId") bookId: Int,
        @Path("lang") lang: String
    ): Call<Book>

    @GET("api/books/authorImage/{id}")
    fun getAuthorImage(
        @Path("id") bookId: Int,
    ): Call<String>

    @GET("api/books/special/random/{lang}")
    fun getTenBooks(@Path("lang") lang: String): Call<List<Book>>

    @GET("/api/books/ids")
    fun getAllBookId(): Call<List<Int>>
}