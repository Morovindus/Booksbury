package com.example.booksbury.book_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.booksbury.MainActivity
import com.example.booksbury.databinding.BookInfoAuthorBinding
import com.example.booksbury.items.Author
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Класс фрагмента отображения информации об авторе книги
class BookInfoAuthor(private val idBook: Int) : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: BookInfoAuthorBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BookInfoAuthorBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Выполнение запроса на получение данных об авторе и обновление пользовательского интерфейса
        fetchDataFromServer()
    }

    // Метод, выводящий на экран информацию об авторе
    private fun fetchDataFromServer() {
        lifecycleScope.launch {
            val author = fetchAuthorDataFromServer(idBook)

            // Установка имени автора книги, изображения автора книги и описание автора
            Picasso.get().load(author.authorImage).into(binding.profileAuthor)
            binding.nameAuthor.text = author.authorName
            binding.textAuthor.text = author.authorAbout
        }
    }

    // Запрос, возвращающий информацию об авторе
    private suspend fun fetchAuthorDataFromServer(id: Int): Author {
        return withContext(Dispatchers.IO) {
            val ipAddress = (activity as MainActivity).getIpAddress()
            val language = (activity as MainActivity).getLanguage()

            val url = URL("http://$ipAddress:3000/api/books/$id/author/$language")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            val jsonResponse = JSONObject(response)

            val authorName = jsonResponse.getString("authorName")
            val authorAbout = jsonResponse.getString("authorAbout")
            val authorImage = jsonResponse.getString("authorImage")

            Author(authorName, authorAbout, authorImage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}