package com.example.booksbury.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.booksbury.entity.Book
import com.example.booksbury.entity.Reviews
import com.example.booksbury.model_request.AuthorInfoResponse
import com.example.booksbury.model_request.BookDetailsResponse
import com.example.booksbury.model_request.NewReviewRequest
import com.example.booksbury.network.ApiClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookViewModel : ViewModel() {
    var idBook: Int = 0
    var idUser: Int = 0
    var star: Int = 0

    private val _books = MutableLiveData<ArrayList<Book>>()
    val books: LiveData<ArrayList<Book>> get() = _books

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Метод для поиска книг по заданному запросу и языку.
    fun searchBooks(query: String, language: String) {
        ApiClient.instanceBook.searchBooks(query, language).enqueue(object : Callback<ArrayList<Book>> {
            override fun onResponse(
                call: Call<ArrayList<Book>>,
                response: Response<ArrayList<Book>>
            ) {
                if (response.isSuccessful) {
                    val bookList = response.body() ?: ArrayList()
                    _books.postValue(bookList)

                } else {
                    _error.postValue("Ошибка: ${response.message()}")
                    _books.postValue(ArrayList())
                }
            }

            override fun onFailure(call: Call<ArrayList<Book>>, t: Throwable) {
                _error.postValue(t.message)
            }
        })
    }

    // Запрос на получение случайно книги
    fun fetchRandomBook(lang: String, imageType: String, callback: (Book?, String?) -> Unit) {
        val call = ApiClient.instanceBook.getRandomBook(lang, imageType)
        call.enqueue(object : Callback<Book> {
            override fun onResponse(call: Call<Book>, response: Response<Book>) {
                if (response.isSuccessful) {
                    val book = response.body()
                    callback(book, null)
                } else {
                    callback(null, "Response is not successful")
                }
            }

            override fun onFailure(call: Call<Book>, t: Throwable) {
                callback(null, t.message)
            }
        })
    }

    // Запрос, возвращающий информацию об авторе
    fun getAuthorInfo(bookId: Int, lang: String, callback: (AuthorInfoResponse?, String?) -> Unit) {
        val call = ApiClient.instanceBook.getAuthorInfo(bookId, lang)

        call.enqueue(object : Callback<AuthorInfoResponse> {
            override fun onResponse(call: Call<AuthorInfoResponse>, response: Response<AuthorInfoResponse>) {
                if (response.isSuccessful) {
                    val authorInfo = response.body()
                    callback(authorInfo, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<AuthorInfoResponse>, t: Throwable) {
                callback(null, "Failure: ${t.message}")
            }
        })
    }

    // Запрос, возвращающий подробности книги
    fun getBookDetails(bookId: Int, lang: String, callback: (String?, String?) -> Unit) {
        val call = ApiClient.instanceBook.getBookDetails(bookId, lang)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val bookDetails = response.body()
                    callback(bookDetails, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                callback(null, "Failure: ${t.message}")
            }
        })
    }

    // Запрос, который возвращает подробную информацию о книге
    fun getBookMoreDetails(bookId: Int, lang: String, callback: (BookDetailsResponse?, String?) -> Unit) {
        val call = ApiClient.instanceBook.getBookMoreDetails(bookId, lang)

        call.enqueue(object : Callback<BookDetailsResponse> {
            override fun onResponse(call: Call<BookDetailsResponse>, response: Response<BookDetailsResponse>) {
                if (response.isSuccessful) {
                    val bookDetails = response.body()
                    callback(bookDetails, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<BookDetailsResponse>, t: Throwable) {
                callback(null, "Failure: ${t.message}")
            }
        })
    }

    // Добавление нового пользователя в БД
    fun addNewReview(idBook: Int, idUser: Int, username: String, date: String, textUser: String, stars: Int, callback: (Boolean, String?) -> Unit) {
        val newReview = NewReviewRequest(
            _id = idUser,
            nameUser = username,
            date = date,
            textUser = textUser,
            stars = stars
        )

        val call = ApiClient.instanceBook.addReview(idBook, newReview)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    println("Комментарий успешно добавлен: ${response.body()}")
                    callback(true, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("Ошибка при добавлении комментария: $errorBody")
                    callback(false, errorBody)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println("Ошибка при отправке запроса: ${t.message}")
                callback(false, t.message)
            }
        })
    }

    // Запрос, возвращающий все отзывы по id книги
    fun getBookReviews(idBook: Int, callback: (List<Reviews>?, String?) -> Unit) {

        val call = ApiClient.instanceBook.getBookReviews(idBook)

        call.enqueue(object : Callback<List<Reviews>> {
            override fun onResponse(call: Call<List<Reviews>>, response: Response<List<Reviews>>) {
                if (response.isSuccessful) {
                    val reviews = response.body()
                    callback(reviews, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, errorBody)
                }
            }

            override fun onFailure(call: Call<List<Reviews>>, t: Throwable) {
                callback(null, t.message)
            }
        })
    }

    // Запрос, возвращающий подробности книги
    fun getBookSynopsis(bookId: Int, lang: String, callback: (String?, String?) -> Unit) {
        val call = ApiClient.instanceBook.getBookSynopsis(bookId, lang)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val bookSynopsis = response.body()
                    callback(bookSynopsis, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                callback(null, "Failure: ${t.message}")
            }
        })
    }

    // Запрос, возвращающий подробности книги
    fun getTitleBook(bookId: Int, lang: String, callback: (String?, String?) -> Unit) {
        val call = ApiClient.instanceBook.getTitleBook(bookId, lang)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val bookSynopsis = response.body()
                    callback(bookSynopsis, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                callback(null, "Failure: ${t.message}")
            }
        })
    }

    // Запрос, который возвращает купленную книгу по id
    fun getPurchasedBook(bookId: Int, lang: String, callback: (Book?, String?) -> Unit) {
        val call = ApiClient.instanceBook.getPurchasedBook(bookId, lang)

        call.enqueue(object : Callback<Book> {
            override fun onResponse(call: Call<Book>, response: Response<Book>) {
                if (response.isSuccessful) {
                    val bookDetails = response.body()
                    callback(bookDetails, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<Book>, t: Throwable) {
                callback(null, "Failure: ${t.message}")
            }
        })
    }

    // Запрос на получение изображения автора главной книги
    fun getAuthorImage(bookId: Int, callback: (String?, String?) -> Unit) {
        val call = ApiClient.instanceBook.getAuthorImage(bookId)

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val bookSynopsis = response.body()
                    callback(bookSynopsis, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                callback(null, "Failure: ${t.message}")
            }
        })
    }

    // Запрос на получение 10 случайных книг
    fun getTenBooks(lang: String, callback: (List<Book>?, String?) -> Unit) {

        val call = ApiClient.instanceBook.getTenBooks(lang)

        call.enqueue(object : Callback<List<Book>> {
            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                if (response.isSuccessful) {
                    val reviews = response.body()
                    callback(reviews, null)
                } else {
                    val errorBody = response.errorBody()?.string()
                    callback(null, errorBody)
                }
            }

            override fun onFailure(call: Call<List<Book>>, t: Throwable) {
                callback(null, t.message)
            }
        })
    }

    // Возвращаем все id всех книг
    fun getAllBookId(callback: (List<Int>?, String?) -> Unit) {

        val call = ApiClient.instanceBook.getAllBookId()

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

}