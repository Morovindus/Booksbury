package com.example.booksbury.interfaces


import com.example.booksbury.entity.Notification
import com.example.booksbury.model_request.CartResponse
import com.example.booksbury.model_request.FavoriteResponse
import com.example.booksbury.model_request.NewUserRequest
import com.example.booksbury.model_request.UserDetailsResponse
import com.example.booksbury.model_request.UserIdResponse
import com.example.booksbury.model_request.UserResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Интерфейс UserApi содержит определения запросов, связанных с пользователями
interface UserApi {
    @GET("api/check_password/{username}/{password}")
    fun checkPassword(
        @Path("username") username: String,
        @Path("password") password: String
    ): Call<Boolean>

    @GET("api/check_username/{username}")
    fun checkUsernameExists(
        @Path("username") username: String,
    ): Call<Boolean>

    @GET("api/get_user_id/{username}")
    fun getId(
        @Path("username") username: String,
    ): Call<String>

    @GET("api/users/ids")
    fun getAllUserIds(): Call<List<UserIdResponse>>

    @POST("api/new_user")
    fun addNewUser(@Body newUser: NewUserRequest): Call<UserResponse>

    @GET("api/users/{userId}/purchasedBooks/{bookId}")
    fun isBookPurchased(
        @Path("userId") userId: Int,
        @Path("bookId") bookId: Int
    ): Call<Boolean>

    @GET("api/users/{userId}/favoriteBooks/{bookId}")
    fun isBookFavorite(
        @Path("userId") userId: Int,
        @Path("bookId") bookId: Int
    ): Call<Boolean>

    @POST("api/users/add_cart/{userId}/{bookId}")
    fun addBookToCart(
        @Path("userId") userId: Int,
        @Path("bookId") bookId: Int
    ): Call<CartResponse>

    @POST("api/users/add_favorite/{userId}/{bookId}")
    fun addBookToFavorite(
        @Path("userId") userId: Int,
        @Path("bookId") bookId: Int
    ): Call<FavoriteResponse>

    @GET("api/users/{userId}/cart/{bookId}")
    fun isBookInCart(
        @Path("userId") userId: Int,
        @Path("bookId") bookId: Int
    ): Call<Boolean>

    @DELETE("api/users/{userId}/deleteFavorite/{bookId}")
    fun deleteFavoriteBook(
        @Path("userId") userId: Int,
        @Path("bookId") bookId: Int
    ): Call<ResponseBody>

    @GET("api/user/{userId}")
    fun getUserDetails(@Path("userId") userId: Int): Call<UserDetailsResponse>

    @DELETE("api/users/{userId}/cart/{bookId}")
    fun deleteCartBook(
        @Path("userId") userId: Int,
        @Path("bookId") bookId: Int
    ): Call<ResponseBody>

    @GET("api/users/{userId}/purchasedBooks")
    fun getBookId(@Path("userId") userId: Int): Call<List<Int>>

    @GET("api/users/{userId}/notifications")
    fun getUserNotifications(@Path("userId") userId: Int): Call<List<Notification>>

    @GET("api/users/{userId}/favoriteBooks")
    fun getFavoriteBookId(@Path("userId") userId: Int): Call<List<Int>>

    @POST("api/users/{userId}/new_notifications")
    fun addNotification(@Path("userId") userId: Int, @Body notification: Notification): Call<ResponseBody>

    @GET("api/users/{userId}/cart")
    fun getBookInCart(@Path("userId") userId: Int): Call<List<Int>>

    @POST("api/users/add_purchased/{userId}/{bookId}")
    fun addBookToPurchased(
        @Path("userId") userId: Int,
        @Path("bookId") bookId: Int
    ): Call<ResponseBody>
}