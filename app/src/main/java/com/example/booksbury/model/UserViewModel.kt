package com.example.booksbury.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.booksbury.entity.Notification
import com.example.booksbury.model_request.CartResponse
import com.example.booksbury.model_request.FavoriteResponse
import com.example.booksbury.model_request.NewUserRequest
import com.example.booksbury.model_request.UserDetailsResponse
import com.example.booksbury.model_request.UserIdResponse
import com.example.booksbury.model_request.UserResponse
import com.example.booksbury.network.ApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel : ViewModel() {

    // Проверка, что пользователь ввел вверный пароль
    fun checkPasswordValid(username: String, password: String, callback: (Boolean) -> Unit) {
        val call = ApiClient.instanceUser.checkPassword(username, password)

        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {
                    val isMatch = response.body() ?: false
                    callback(isMatch)
                } else {
                    callback(false)
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                callback(false)
            }
        })
    }

    // Проверка, что введенный пользователем никнейм существует в БД
    fun checkUsernameExists(username: String, callback: (Boolean) -> Unit) {
        val call = ApiClient.instanceUser.checkUsernameExists(username)

        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {
                    val isMatch = response.body() ?: false
                    callback(isMatch)
                } else {
                    callback(false)
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                callback(false)
            }
        })
    }

    // Запрос для получения id пользователя
    fun getId(username: String, callback: (String?) -> Unit) {
        val call = ApiClient.instanceUser.getId(username)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val id = response.body()
                    callback(id)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                callback(null)
            }
        })
    }

    // Запрос, возвращающий id всех уже имеющихся пользователей
    fun getAllUserId(callback: (List<Int>) -> Unit) {

        val call = ApiClient.instanceUser.getAllUserIds()

        // Здесь исправлено использование Callback на тип, соответствующий ожидаемому списку UserIdResponse
        call.enqueue(object : Callback<List<UserIdResponse>> {
            override fun onResponse(
                call: Call<List<UserIdResponse>>,
                response: Response<List<UserIdResponse>>
            ) {
                if (response.isSuccessful) {
                    val userIdsResponse = response.body()
                    // Если ответ успешен, преобразуем список UserIdResponse в список идентификаторов
                    val userIds = userIdsResponse?.map { it._id } ?: emptyList()
                    // Вызываем колбэк и передаем список идентификаторов
                    callback(userIds)
                } else {
                    Log.d("myLogs", "Response is not successful")
                }
            }

            override fun onFailure(call: Call<List<UserIdResponse>>, t: Throwable) {
                Log.d("myLogs", t.message.toString())
            }
        })
    }

    // Добавление нового пользователя в БД
    fun addNewUser(idUser: Int, username: String, email: String, password: String, callback: (Boolean, String?) -> Unit) {
        val newUser = NewUserRequest(
            _id = idUser,
            username = username,
            email = email,
            password = password
        )

        val call = ApiClient.instanceUser.addNewUser(newUser)

        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    println("Пользователь успешно добавлен: ${response.body()}")
                    callback(true, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("Ошибка при добавлении пользователя: $errorBody")
                    callback(false, errorBody)
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                println("Ошибка при отправке запроса: ${t.message}")
                callback(false, t.message)
            }
        })
    }

    //Запрос, с помощью которого узнаем есть данная книга в уже купленных книгах у пользователя
    fun isBookPurchased(userId: Int, bookId: Int, callback: (Boolean?, String?) -> Unit) {
        val call = ApiClient.instanceUser.isBookPurchased(userId, bookId)

        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {
                    val isBookPurchased = response.body()
                    callback(isBookPurchased, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                callback(null, "Failure: ${t.message}")
            }
        })
    }

    // Запрос, с помощью которого узнаем есть данная книга в избранных у пользователя
    fun isBookFavorite(userId: Int, bookId: Int, callback: (Boolean?, String?) -> Unit) {
        val call = ApiClient.instanceUser.isBookFavorite(userId, bookId)

        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {
                    val isBookFavorite = response.body()
                    callback(isBookFavorite, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                callback(null, "Failure: ${t.message}")
            }
        })
    }

    // Запрос на добавление новой книги в корзину
    fun addBookToCart(userId: Int, bookId: Int, callback: (Boolean, String?) -> Unit) {
        val call = ApiClient.instanceUser.addBookToCart(userId, bookId)

        call.enqueue(object : Callback<CartResponse> {
            override fun onResponse(call: Call<CartResponse>, response: Response<CartResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(true, it.message)
                    } ?: callback(false, "Пустой ответ от сервера")
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(false, "Ошибка: $errorBody")
                }
            }

            override fun onFailure(call: Call<CartResponse>, t: Throwable) {
                callback(false, "Ошибка: ${t.message}")
            }
        })
    }

    // Запрос на добавление новой книги в избранное
    fun addBookToFavorite(userId: Int, bookId: Int, callback: (Boolean, String?) -> Unit) {
        val call = ApiClient.instanceUser.addBookToFavorite(userId, bookId)

        call.enqueue(object : Callback<FavoriteResponse> {
            override fun onResponse(call: Call<FavoriteResponse>, response: Response<FavoriteResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callback(true, it.message)
                    } ?: callback(false, "Пустой ответ от сервера")
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(false, "Ошибка: $errorBody")
                }
            }

            override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                callback(false, "Ошибка: ${t.message}")
            }
        })
    }

    // Запрос, с помощью которого узнаем есть данная книга в корзине у пользователя
    fun isBookInCart(userId: Int, bookId: Int, callback: (Boolean?, String?) -> Unit) {
        val call = ApiClient.instanceUser.isBookInCart(userId, bookId)

        call.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {
                    val isBookInCart = response.body()
                    callback(isBookInCart, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                callback(null, "Failure: ${t.message}")
            }
        })
    }

    // Запрос на удаления книги из избранного
    fun deleteFavoriteBook(userId: Int, bookId: Int, callback: (Boolean, String?) -> Unit) {
        val call = ApiClient.instanceUser.deleteFavoriteBook(userId, bookId)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    callback(true, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(false, "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(false, "Failure: ${t.message}")
            }
        })
    }

    // Запрос возвращающий имя и почту пользователя
    fun getUserDetails(userId: Int, callback: (UserDetailsResponse?, String?) -> Unit) {
        val call = ApiClient.instanceUser.getUserDetails(userId)

        call.enqueue(object : Callback<UserDetailsResponse> {
            override fun onResponse(call: Call<UserDetailsResponse>, response: Response<UserDetailsResponse>) {
                if (response.isSuccessful) {
                    val userInfo = response.body()
                    callback(userInfo, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<UserDetailsResponse>, t: Throwable) {
                callback(null, "Failure: ${t.message}")
            }
        })
    }

    // Запрос на удаления книги из корзины
    fun deleteCartBook(userId: Int, bookId: Int, callback: (Boolean, String?) -> Unit) {
        val call = ApiClient.instanceUser.deleteCartBook(userId, bookId)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    callback(true, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(false, "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(false, "Failure: ${t.message}")
            }
        })
    }

    // Запрос, который возвращает все id купленных пользователем книг
    fun getBookId(idUser: Int, callback: (List<Int>?, String?) -> Unit) {

        val call = ApiClient.instanceUser.getBookId(idUser)

        call.enqueue(object : Callback<List<Int>> {
            override fun onResponse(call: Call<List<Int>>, response: Response<List<Int>>) {
                if (response.isSuccessful) {
                    val idBooks = response.body()
                    callback(idBooks, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, errorBody)
                }
            }

            override fun onFailure(call: Call<List<Int>>, t: Throwable) {
                callback(null, t.message)
            }
        })
    }

    // Запрос на получение всех уведомлений
    fun getUserNotifications(idUser: Int, callback: (List<Notification>?, String?) -> Unit) {

        val call = ApiClient.instanceUser.getUserNotifications(idUser)

        call.enqueue(object : Callback<List<Notification>> {
            override fun onResponse(call: Call<List<Notification>>, response: Response<List<Notification>>) {
                if (response.isSuccessful) {
                    val idBooks = response.body()
                    callback(idBooks, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, errorBody)
                }
            }

            override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                callback(null, t.message)
            }
        })
    }

    // Возвращаем все id избранных пользователем книг
    fun getFavoriteBookId(idUser: Int, callback: (List<Int>?, String?) -> Unit) {

        val call = ApiClient.instanceUser.getFavoriteBookId(idUser)

        call.enqueue(object : Callback<List<Int>> {
            override fun onResponse(call: Call<List<Int>>, response: Response<List<Int>>) {
                if (response.isSuccessful) {
                    val idBooks = response.body()
                    callback(idBooks, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, errorBody)
                }
            }

            override fun onFailure(call: Call<List<Int>>, t: Throwable) {
                callback(null, t.message)
            }
        })
    }

    // Запрос на добавление нового уведомления
    fun addNotification(userId: Int, notification: Notification, callback: (Boolean, String?) -> Unit) {

        val call = ApiClient.instanceUser.addNotification(userId, notification)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    callback(true, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(false, "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(false, "Failure: ${t.message}")
            }
        })
    }

    // Возвращаем все id всех книг в корзине
    fun getBookInCart(idUser: Int, callback: (List<Int>?, String?) -> Unit) {

        val call = ApiClient.instanceUser.getBookInCart(idUser)

        call.enqueue(object : Callback<List<Int>> {
            override fun onResponse(call: Call<List<Int>>, response: Response<List<Int>>) {
                if (response.isSuccessful) {
                    val idBooks = response.body()
                    callback(idBooks, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, errorBody)
                }
            }

            override fun onFailure(call: Call<List<Int>>, t: Throwable) {
                callback(null, t.message)
            }
        })
    }

    // Запрос на добавление новой книги в купленное
    fun addBookToPurchased(userId: Int, bookId: Int, callback: (Boolean, String?) -> Unit) {
        val call = ApiClient.instanceUser.addBookToPurchased(userId, bookId)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    callback(true, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(false, "Ошибка: $errorBody")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(false, "Ошибка: ${t.message}")
            }
        })
    }
}

