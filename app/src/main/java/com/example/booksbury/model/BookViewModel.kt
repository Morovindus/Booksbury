package com.example.booksbury.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.booksbury.items.Book
import com.example.booksbury.network.ApiClient
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
        ApiClient.instance.searchBooks(query, language).enqueue(object : Callback<ArrayList<Book>> {
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

    fun fetchRandomBook(lang: String, imageType: String, callback: (Book?, String?) -> Unit) {
        val call = ApiClient.instance.getRandomBook(lang, imageType)
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
}