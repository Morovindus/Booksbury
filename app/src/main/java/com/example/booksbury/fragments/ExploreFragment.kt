package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterMarket
import com.example.booksbury.databinding.ExploreFragmentBinding
import com.example.booksbury.items.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Класс фрагмента магазина книг
class ExploreFragment : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: ExploreFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ExploreFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Слушатели нажатий всех кнопок на экране
        binding.buttonHome.setOnClickListener { navigateToFragment(R.id.action_ExploreFragment_to_HomeFragment) }
        binding.buttonFavourites.setOnClickListener { navigateToFragment(R.id.action_ExploreFragment_to_FavouritesFragment) }
        binding.buttonProfile.setOnClickListener { navigateToFragment(R.id.action_ExploreFragment_to_ProfileFragment) }
        binding.buttonSearch.setOnClickListener { navigateToFragment(R.id.action_ExploreFragment_to_SearchFragment) }
        binding.buttonNotification.setOnClickListener { navigateToFragment(R.id.action_ExploreFragment_to_NotificationFragment) }

        // Получаем и обновляем данные о книгах на экране
        fetchBooksAndUpdateUI()
    }

    // Метод, который реализует вывод всех книг на экран
    private fun fetchBooksAndUpdateUI() {
        lifecycleScope.launch {
            val books = withContext(Dispatchers.IO) {
                fetchBooksFromServer()
            }

            // Добавляем все книги в массив
            val allBooks = ArrayList<Book>()

            for (book in books) {
                val allBook = withContext(Dispatchers.IO) {
                    fetchAllBooksFromServer(book)
                }
                allBooks.add(allBook)
            }

            updateRecyclerView(allBooks)
        }
    }

    // Метод для обновления пользовательского интерфейса
    private fun updateRecyclerView(items: ArrayList<Book>) {
        val adapter = CustomAdapterMarket(items, requireContext(),this@ExploreFragment)
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 20))
        binding.recyclerView.adapter = adapter
    }

    // Метод, который позволяет переключить фрагмент, и передать ему значение id книги
    fun navigateToBookInfoFragment(id: Int) {
        (activity as MainActivity).setIdBook(id)
        findNavController().navigate(R.id.action_ExploreFragment_to_BookInfoFragment)
    }

    // Запрос на получение всех книг
    private fun fetchAllBooksFromServer(bookId: Int): Book {
        val ipAddress = (activity as MainActivity).getIpAddress()
        val language = (activity as MainActivity).getLanguage()

        val url = URL("http:$ipAddress:3000/book/bookById/$bookId/$language/averageCover")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        val jsonObject = JSONObject(response)

        val id = jsonObject.getString("_id").toInt()
        val title = jsonObject.getString("title")
        val authorName = jsonObject.getString("authorName")
        val stars = jsonObject.getInt("averageStars")
        val ratings = jsonObject.getInt("ratings")
        val price = jsonObject.getInt("price")
        val averageCover = jsonObject.getString("averageCover")

        return Book(id, averageCover, title, authorName, stars, ratings, price)
    }

    // Возвращаем все id всех книг
    private fun fetchBooksFromServer(): ArrayList<Int> {
        val ipAddress = (activity as MainActivity).getIpAddress()

        val url = URL("http:$ipAddress:3000/api/books/ids")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        val jsonArray = JSONArray(response)

        val items = ArrayList<Int>()
        for (i in 0 until jsonArray.length()) {
            val value = jsonArray.getInt(i)
            items.add(value)
        }
        return items
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