package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterFavoriteBooks
import com.example.booksbury.databinding.FavouritesFragmentBinding
import com.example.booksbury.items.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Класс фрагмента избранных книг
class FavouritesFragment : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: FavouritesFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FavouritesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Слушатели нажатий всех кнопок на экране
        binding.buttonHome.setOnClickListener { navigateToFragment(R.id.action_FavouritesFragment_to_HomeFragment) }
        binding.buttonExplore.setOnClickListener { navigateToFragment(R.id.action_FavouritesFragment_to_ExploreFragment) }
        binding.buttonProfile.setOnClickListener { navigateToFragment(R.id.action_FavouritesFragment_to_ProfileFragment) }
        binding.buttonSearch.setOnClickListener { navigateToFragment(R.id.action_FavouritesFragment_to_SearchFragment) }
        binding.buttonNotification.setOnClickListener { navigateToFragment(R.id.action_FavouritesFragment_to_NotificaionFragment) }

        // Получаем и обновляем данные о книгах на экране
        fetchBooksAndUpdateUI()
    }

    // Метод, который реализует вывод всех избранных книг на экран
    private fun fetchBooksAndUpdateUI() {
        lifecycleScope.launch {
            try {
                val books = withContext(Dispatchers.IO) {
                    fetchBooksFromServer()
                }

                // Добавляем все купленные книги пользователем в массив
                val favoriteBooks = ArrayList<Book>()
                for (book in books) {
                    val purchasedBook = withContext(Dispatchers.IO) {
                        fetchFavoriteBooksFromServer(book)
                    }
                    favoriteBooks.add(purchasedBook)
                }

                updateUIWithFavoriteBooks(favoriteBooks)
            } catch (e: Exception) {
                showNoFavoriteBooksNotification()
            }
        }
    }

    // Метод для обновления пользовательского интерфейса
    private fun updateUIWithFavoriteBooks(favoritetBooks: ArrayList<Book>) {
        val adapter = CustomAdapterFavoriteBooks(favoritetBooks, this@FavouritesFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
        binding.recyclerView.adapter = adapter
    }

    // Метод, который обрабатывает случай - пустого массива купленных книг
    private fun showNoFavoriteBooksNotification() {

        // Получаем родительский ConstraintLayout
        val constraintLayout = binding.ConstraintLayout

        // Создаем новое представление
        val newView = LayoutInflater.from(requireContext()).inflate(R.layout.notification_no_favorite, null)

        // Определяем параметры размещения для нового представления
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        // Устанавливаем отношения в параметрах размещения для нового представления
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        params.topToBottom = R.id.blackRectangle
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        params.bottomToTop = R.id.cardView

        // Устанавливаем новые параметры размещения для нового представления
        newView.layoutParams = params

        // Удаляем старое представление RecyclerView из родительского ConstraintLayout
        constraintLayout.removeView(binding.recyclerView)

        // Добавляем новое представление в родительский ConstraintLayout
        constraintLayout.addView(newView)
    }

    // Возвращаем избранную книгу по id
    private fun fetchFavoriteBooksFromServer(bookId: Int): Book {
        val ipAddress = (activity as MainActivity).getIpAddress()
        val language = (activity as MainActivity).getLanguage()

        val url = URL("http:$ipAddress:3000/book/bookById/$bookId/$language/smallCover")
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
        val smallCover = jsonObject.getString("smallCover")

        return Book(id, smallCover, title, authorName, stars, ratings, price)
    }

    // Возвращаем все id избранных пользователем книг
    private fun fetchBooksFromServer(): ArrayList<Int> {
        val ipAddress = (activity as MainActivity).getIpAddress()
        val userId = (activity as MainActivity).getIdUser()

        val url = URL("http:$ipAddress:3000/api/users/$userId/favoriteBooks")
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

    // Метод, который позволяет переключить фрагмент, и передать ему значение id книги
    fun navigateToBookInfoFragment(id: Int) {
        val bundle = Bundle().apply {
            putInt("id", id)
        }
        findNavController().navigate(R.id.action_FavouritesFragment_to_BookInfoFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}