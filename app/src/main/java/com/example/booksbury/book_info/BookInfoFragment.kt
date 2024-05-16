package com.example.booksbury.book_info

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.databinding.BookInfoFragmentBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Класс фрагмента отображения информации о книге
class BookInfoFragment : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: BookInfoFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BookInfoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Назначение обработчика нажатия кнопки "Назад"
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Получаем значение id книги из предыдущего фрагмента
        val idBook = arguments?.getInt("id", 0) ?: 0

        // Отправляем запрос, и инициализируем все поля на экране
        fetchBookData(idBook)

        // Настраиваем ViewPager и TabLayout
        setupViewPagerAndTabs(idBook)

        // Редактируем экран пользователя в зависимости от некоторых параметров
        handleBookButtons(idBook)
    }

    // Обрабатываем удаление и добавление кнопок на экране
    private fun handleBookButtons(idBook: Int) {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val container = binding.container

        lifecycleScope.launch {
            val isBookPurchased = fetchBookFromPurchasedFromServer(idBook)
            val isBookFavorite = fetchBookFromFavoriteFromServer(idBook)

            withContext(Dispatchers.Main) {
                if (isBookPurchased) {
                    // Если у пользователя уже есть эта книга
                    inflater.inflate(R.layout.button_read, container, true)
                    setupFavoriteButton(isBookFavorite, idBook)

                } else {
                    // Если у пользователя нет этой книги
                    inflater.inflate(R.layout.button_buy, container, true)
                    setupBuyButton(inflater, container, idBook)
                }
            }
        }
    }

    // Метод настраивающий отображение кнопок "Избранное" и "Удаление из избранного"
    private fun setupFavoriteButton(isBookFavorite: Boolean, idBook: Int) {
        if (isBookFavorite) {
            // Если у пользователя книга в избранных
            setupButton(binding.buttonFavourites, false, View.INVISIBLE) {}

            setupButton(binding.buttonDeleteFavorite, true, View.VISIBLE) {
                lifecycleScope.launch {

                    // Посылаем запрос на удаление книги из избранного
                    withContext(Dispatchers.IO) {
                        removeFromFavorite(idBook)
                    }
                    Toast.makeText(activity, getString(R.string.book_removed_from_favorites), Toast.LENGTH_LONG).show()

                    setupFavoriteButton(fetchBookFromFavoriteFromServer(idBook),idBook)
                }
            }
        } else {
            // Если у пользователя данной книги в избранных нет
            setupButton(binding.buttonFavourites, true, View.VISIBLE) {

                lifecycleScope.launch {
                    // Посылаем запрос на добавление книги в избранного
                    withContext(Dispatchers.IO) {
                        addBookToFavorite(idBook)
                    }
                    Toast.makeText(activity, getString(R.string.book_added_to_favorites), Toast.LENGTH_LONG).show()

                    setupFavoriteButton(fetchBookFromFavoriteFromServer(idBook),idBook)
                }
            }

            setupButton(binding.buttonDeleteFavorite, false, View.INVISIBLE) {}
        }
    }

    // Запрос на удаления книги из избранного
    private fun removeFromFavorite(bookId: Int) {
        val ipAddress = (context as MainActivity).getIpAddress()
        val userId = (context as MainActivity).getIdUser()

        val url = URL("http:$ipAddress:3000/api/users/$userId/deleteFavorite/$bookId")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            println("Книга успешно удалена из избранного")
        } else {
            println("Ошибка при удалении книги из избранного: $responseCode")
        }
    }

    // Метод, измененяющий отображение кнопок на экране
    private fun setupButton(button: Button, isEnabled: Boolean, isVisible: Int, clickListener: () -> Unit) {
        button.isEnabled = isEnabled
        button.visibility = isVisible

        button.setOnClickListener {
            clickListener()
        }
    }

    // Метод выводящий всю подробную информацию о книге на экран
    private fun fetchBookData(idBook: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            fetchItemsFromServer(idBook)
        }
    }

    // Добавление кнопки "Купить", удаление кнопки "Избранное"
    private fun setupBuyButton(inflater: LayoutInflater, container: ViewGroup, idBook: Int) {
        setupButton(binding.buttonFavourites, false, View.INVISIBLE) {}

        val buttonBuy = container.findViewById<AppCompatImageButton>(R.id.button_buy_book)

        // Слушатель нажатия на кнопку "Купить"
        buttonBuy.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val isBookInCart = fetchBookFromCartFromServer(idBook)

                // Показ уведомления должен быть выполнен на главном потоке
                withContext(Dispatchers.Main) {
                    if (isBookInCart) {
                        Toast.makeText(activity, getString(R.string.book_already_in_cart), Toast.LENGTH_LONG).show()
                    } else {
                        addBookToCart(idBook)
                        Toast.makeText(activity, getString(R.string.book_added_to_cart), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Метод инициализируйющий ViewPager и TabLayout
    private fun setupViewPagerAndTabs(idBook: Int) {
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        val adapter = ViewPagerAdapter(requireActivity(), idBook)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tabs_synopsis)
                1 -> getString(R.string.tabs_details)
                2 -> getString(R.string.tabs_author)
                3 -> getString(R.string.tabs_review)
                else -> ""
            }
        }.attach()
    }

    // Класс, отвечающий за управоление вкладок ViewPager
    private inner class ViewPagerAdapter(activity: FragmentActivity, private val idBook: Int) :
        FragmentStateAdapter(activity) {

        // Возвращает общее количество вкладок
        override fun getItemCount(): Int = 4

        // Создает и возвращает фрагменты для каждой вкладки
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> BookInfoSynopsis(idBook)
            1 -> BookInfoDetails(idBook)
            2 -> BookInfoAuthor(idBook)
            3 -> BookInfoReviews(idBook)
            else -> Fragment()
        }
    }

    // Запрос, который возвращает подробную информацию о книге
    private fun fetchItemsFromServer(id: Int) {
        val ipAddress = (activity as MainActivity).getIpAddress()
        val language = (activity as MainActivity).getLanguage()

        val url = URL("http://$ipAddress:3000/api/books/more/$id/$language")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        val jsonResponse = JSONObject(response)

        requireActivity().runOnUiThread {
            binding.titleBook.text = jsonResponse.getJSONObject(language).getString("title")
            binding.nameAuthor.text = jsonResponse.getJSONObject(language).getString("authorName")
            binding.price.text = "${jsonResponse.getInt("price")}\u20BD"
            binding.dateReleased.text = jsonResponse.getInt("released").toString()
            binding.part.text = jsonResponse.getInt("part").toString()
            binding.page.text = jsonResponse.getInt("page").toString()

            val middleCover = jsonResponse.getJSONObject("images").getString("middleCover")
            Picasso.get().load(middleCover).into(binding.mainImage)
        }
    }

    // Запрос, с помощью которого узнаем есть данная книга в избранных у пользователя
    private suspend fun fetchBookFromFavoriteFromServer(id: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val ipAddress = (context as MainActivity).getIpAddress()
            val userId = (context as MainActivity).getIdUser()

            val url = URL("http:$ipAddress:3000/$userId/favoriteBooks/$id")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            val jsonObject = JSONObject(response)
            jsonObject.getBoolean("isBookFavorite")
        }
    }

    // Запрос, с помощью которого узнаем есть данная книга в корзине у пользователя
    private suspend fun fetchBookFromCartFromServer(id: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val ipAddress = (context as MainActivity).getIpAddress()
            val userId = (context as MainActivity).getIdUser()

            val url = URL("http:$ipAddress:3000/$userId/cart/$id")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            val jsonObject = JSONObject(response)
            jsonObject.getBoolean("isBookInCart")
        }
    }

    // Запрос, с помощью которого узнаем есть данная книга в уже купленных книгах у пользователя
    private suspend fun fetchBookFromPurchasedFromServer(id: Int): Boolean {
        return withContext(Dispatchers.IO) {
            val ipAddress = (context as MainActivity).getIpAddress()
            val userId = (context as MainActivity).getIdUser()

            val url = URL("http:$ipAddress:3000/$userId/purchasedBooks/$id")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            val jsonObject = JSONObject(response)
            jsonObject.getBoolean("isBookPurchased")
        }
    }

    // Запрос на добавление новой книги в избранное
    private suspend fun addBookToFavorite(bookId: Int) {
        withContext(Dispatchers.IO) {
            try {
                val ipAddress = (context as MainActivity).getIpAddress()
                val userId = (context as MainActivity).getIdUser()

                val url = URL("http:$ipAddress:3000/add_favorite/$userId/$bookId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val responseCode = connection.responseCode
                val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Успешное добавление книги в корзину
                } else {
                    val jsonResponse = JSONObject(responseMessage)
                    val message = jsonResponse.optString("message", "Ошибка при добавлении книги в корзину")
                    Log.d("addBookToFavorite", "Ошибка: $message")
                }
            } catch (e: Exception) {
                // Логируем исключение
                Log.d("addBookToFavorite", "Ошибка при отправке запроса: ${e.message}")
            }
        }
    }

    // Запрос на добавление новой книги в корзину
    private suspend fun addBookToCart(bookId: Int) {
        withContext(Dispatchers.IO) {
            try {
                val ipAddress = (context as MainActivity).getIpAddress()
                val userId = (context as MainActivity).getIdUser()

                val url = URL("http:$ipAddress:3000/add_cart/$userId/$bookId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val responseCode = connection.responseCode
                val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Успешное добавление книги в корзину
                } else {
                    val jsonResponse = JSONObject(responseMessage)
                    val message = jsonResponse.optString("message", "Ошибка при добавлении книги в корзину")
                    // Логируем ошибку
                    Log.d("addBookToCart", "Ошибка: $message")
                }
            } catch (e: Exception) {
                // Логируем исключение
                Log.d("addBookToCart", "Ошибка при отправке запроса: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}