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

class BookInfoDetails(private val idBook: Int) : Fragment() {

    private var _binding: BookInfoSynopsisBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BookInfoSynopsisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchDataFromServer()
    }

    private fun fetchDataFromServer() {
        lifecycleScope.launch {
            val details = fetchDetailsDataFromServer(idBook)
            binding.textSynopsis.text = details
        }
    }

    private suspend fun fetchDetailsDataFromServer(id: Int): String {
        return withContext(Dispatchers.IO) {
            val ipAddress = (activity as MainActivity).getIpAddress()
            val url = URL("http://$ipAddress:3000/api/books/$id/details")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}