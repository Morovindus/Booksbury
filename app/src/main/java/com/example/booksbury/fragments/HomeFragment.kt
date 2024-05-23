package com.example.booksbury.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterBooks
import com.example.booksbury.databinding.HomeFragmentBinding
import com.example.booksbury.items.Book
import com.example.booksbury.model.BookViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Класс фрагмента главной страницы
class HomeFragment : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: HomeFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // ViewModel для хранения состояния
    private lateinit var viewModel: BookViewModel

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обработчики нажатий на все кнопки на экране
        binding.buttonMore.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_ExploreFragment) }
        binding.buttonFavourites.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_FavouritesFragment) }
        binding.buttonProfile.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_ProfileFragment) }
        binding.mainImage.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_BookInfoFragment) }
        binding.buttonSearch.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_SearchFragment) }
        binding.buttonNotification.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_NotificaionFragment) }

        // Инициализация ViewModel
        viewModel = ViewModelProvider(this)[BookViewModel::class.java]


        // Получаем и обновляем данные о книгах на экране
        fetchBooksAndUpdateUI()
    }

    // Метод, который реализует вывод случайных книг на экран
    private fun fetchBooksAndUpdateUI() {

        val language = (activity as MainActivity).getLanguage()

        // Выполняем запрос на получение случайной книги
        viewModel.fetchRandomBook(language, "bigCover") { mainBook, error ->
            if (error != null) {
                // Обработка ошибки
                Log.e("MyLogs", "Failed to fetch random book: $error")
            } else {
                // Обработка успешного ответа
                mainBook?.let { book ->
                    // Обновляем UI с полученной книгой
                    binding.mainTitle.text = book.titleBook
                    Picasso.get().load(book.imageResource).into(binding.mainImage)

                    // Используем корутину для получения изображения автора
                    lifecycleScope.launch {
                        try {
                            val authorImageUrl = fetchAuthorImageFromServer(mainBook.id)
                            Picasso.get().load(authorImageUrl).into(binding.imageAuthor)
                        } catch (e: Exception) {
                            Log.e("MyLogs", "Failed to fetch author image: $e")
                        }
                    }

                    binding.mainTitle.text = mainBook.titleBook
                    binding.textAuthor.text = mainBook.nameAuthor

                    // Установка слушателя нажатия на основное изображение
                    binding.mainImage.setOnClickListener {
                        navigateToBookInfoFragment(mainBook.id)
                    }
                }
            }
        }


        lifecycleScope.launch {

            // Загрузка списка элементов из сети
            val topBooks: ArrayList<Book> = withContext(Dispatchers.IO) {
                fetchItemsFromServer()
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
        (activity as MainActivity).setIdBook(id)
        findNavController().navigate(R.id.action_HomeFragment_to_BookInfoFragment)
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
    private suspend fun fetchAuthorImageFromServer(id: Int): String {
        val ipAddress = (activity as MainActivity).getIpAddress()
        val url = URL("http:$ipAddress:3000/api/books/authorImage/$id")
        val connection = withContext(Dispatchers.IO) {
            url.openConnection() as HttpURLConnection
        }
        connection.requestMethod = "GET"

        val response = withContext(Dispatchers.IO) {
            connection.inputStream.bufferedReader().use { it.readText() }
        }

        val jsonObject = JSONObject(response)
        return jsonObject.getString("imageAuthor")
    }

    // Метод переключающий фрагменты
    private fun navigateToFragment(actionId: Int) {
        findNavController().navigate(actionId)
    }

    // Метод, вызываемый перед уничтожением представления фрагмента
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}