package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterBooks
import com.example.booksbury.databinding.HomeFragmentBinding
import com.example.booksbury.items.Book
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Слушатели нажатий всех кнопок на экране
        binding.buttonMore.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_ExploreFragment) }
        binding.buttonFavourites.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_FavouritesFragment) }
        binding.buttonProfile.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_ProfileFragment) }
        binding.mainImage.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_BookInfoFragment) }
        binding.buttonSearch.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_SearchFragment) }
        binding.buttonNotification.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_NotificaionFragment) }

        fetchBooksAndUpdateUI()
    }

    // Метод, который реализует вывод всех избранных книг на экран
    private fun fetchBooksAndUpdateUI() {
        lifecycleScope.launch {
            // Загрузка основной книги из сети
            val mainBook: Book = withContext(Dispatchers.IO) {
                fetchMainBookFromServer()
            }

            // Загрузка изображения автора из сети
            val authorImage: String = withContext(Dispatchers.IO) {
                fetchAuthorImageFromServer(mainBook.id)
            }

            // Загрузка списка элементов из сети
            val topBooks: ArrayList<Book> = withContext(Dispatchers.IO) {
                fetchItemsFromServer()
            }

            // Переключение на главный поток для обновления UI
            withContext(Dispatchers.Main) {
                // Отображение основной книги
                Picasso.get().load(mainBook.imageResource).into(binding.mainImage)
                Picasso.get().load(authorImage).into(binding.imageAuthor)
                binding.mainTitle.text = mainBook.titleBook
                binding.textAuthor.text = mainBook.nameAuthor

                // Установка слушателя нажатия на основное изображение
                binding.mainImage.setOnClickListener {
                    val id = mainBook.id
                    navigateToBookInfoFragment(id)
                }
            }

            updateUIWithTopBooks(topBooks)
        }
    }

    // Метод для обновления пользовательского интерфейса
    private fun updateUIWithTopBooks(topBooks: ArrayList<Book>) {
        val adapter = CustomAdapterBooks(topBooks, this@HomeFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
        binding.recyclerView.adapter = adapter
    }

    // Метод, который позволяет переключить фрагмент, и передать ему значение id книги
    fun navigateToBookInfoFragment(id: Int) {
        val bundle = Bundle().apply {
            putInt("id", id)
        }
        findNavController().navigate(R.id.action_HomeFragment_to_BookInfoFragment, bundle)
    }

    // Запрос на получение одной случайной книги
    private fun fetchMainBookFromServer(): Book {
        val ipAddress = (activity as MainActivity).getIpAddress()
        val language = (activity as MainActivity).getLanguage()

        val url = URL("http:$ipAddress:3000/api/books/randomBook/$language/bigCover")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        val jsonObject = JSONObject(response)

        val id = jsonObject.getInt("_id")
        val title = jsonObject.getString("title")
        val authorName = jsonObject.getString("authorName")
        val stars = jsonObject.getInt("averageStars")
        val ratings = jsonObject.getInt("ratings")
        val price = jsonObject.getInt("price")
        val bigCover = jsonObject.getString("bigCover")

        return Book(id, bigCover, title, authorName, stars, ratings, price)
    }

    // Запрос на получение 10 случайных книг
    private fun fetchItemsFromServer(): ArrayList<Book> {
        val ipAddress = (activity as MainActivity).getIpAddress()
        val language = (activity as MainActivity).getLanguage()

        val url = URL("http:$ipAddress:3000/api/books/special/random/$language")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        val jsonResponse = JSONArray(response)

        val items = ArrayList<Book>()
        for (i in 0 until jsonResponse.length()) {
            val bookObject = jsonResponse.getJSONObject(i)
            val id = bookObject.getInt("_id")
            val title = bookObject.getString("title")
            val authorName = bookObject.getString("authorName")
            val stars = bookObject.getInt("averageStars")
            val ratings = bookObject.getInt("ratings")
            val price = bookObject.getInt("price")
            val smallCover = bookObject.getString("smallCover")

            val item = Book(id, smallCover, title, authorName, stars, ratings, price)
            items.add(item)
        }
        return items
    }

    // Запрос на получение изображения автора главной книги
    private fun fetchAuthorImageFromServer(id: Int): String {
        val ipAddress = (activity as MainActivity).getIpAddress()

        val url = URL("http:$ipAddress:3000/api/books/authorImage/$id")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        val jsonObject = JSONObject(response)
        val imageAuthor = jsonObject.getString("imageAuthor")

        return imageAuthor
    }

    // Метод переключающий фрагменты
    private fun navigateToFragment(actionId: Int) {
        findNavController().navigate(actionId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}