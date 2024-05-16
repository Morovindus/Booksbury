package com.example.booksbury.book_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.booksbury.MainActivity
import com.example.booksbury.databinding.BookInfoSynopsisBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


// Класс фрагмента отображения описания книги
class BookInfoSynopsis(private val idBook: Int) : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: BookInfoSynopsisBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BookInfoSynopsisBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Выполнение запроса на получение описания книги и обновление пользовательского интерфейса
        fetchDataFromServer()
    }

    // Метод, выводящий на экран описание книги
    private fun fetchDataFromServer() {
        lifecycleScope.launch {
            val synopsis = fetchSynopsisDataFromServer(idBook)
            binding.textSynopsis.text = synopsis.substring(1, synopsis.length - 1)
        }
    }

    // Запрос, возвращающий описание книги
    private suspend fun fetchSynopsisDataFromServer(id: Int): String {
        return withContext(Dispatchers.IO) {
            val ipAddress = (activity as MainActivity).getIpAddress()
            val language = (activity as MainActivity).getLanguage()

            val url = URL("http://$ipAddress:3000/api/books/$id/synopsis/$language")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            return@withContext BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}