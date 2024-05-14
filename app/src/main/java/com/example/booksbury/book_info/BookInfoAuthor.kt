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

class BookInfoAuthor(private val idBook: Int) : Fragment() {

    private var _binding: BookInfoAuthorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BookInfoAuthorBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchDataFromServer()
    }

    // Метод, выводящий на экран информацию об авторе
    private fun fetchDataFromServer() {
        lifecycleScope.launch {
            val author = fetchAuthorDataFromServer(idBook)

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