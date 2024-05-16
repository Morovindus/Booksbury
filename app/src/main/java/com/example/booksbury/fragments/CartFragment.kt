package com.example.booksbury.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterCart
import com.example.booksbury.databinding.CartFragmentBinding
import com.example.booksbury.items.Book
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Класс фрагмента корзины
class CartFragment : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: CartFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // Список книг в корзине
    private var cartBooksNew = ArrayList<Book>()

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CartFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Выводим список вех книг в корзине наэкран
        fetchBooksAndUpdateUI()

        // Слушатель кнпоки "Назад"
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Слушатель кнпоки "Оформить заказ"
        binding.buttonOrder.setOnClickListener {
            showOrderConfirmationDialog(cartBooksNew)
        }
    }

    // Диалог, всплывающий, когда пользователь хочет оформить заказ
    private fun showOrderConfirmationDialog(cartBook: ArrayList<Book>) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.order_confirmation_title))
            .setMessage(getString(R.string.order_confirmation_message))
            .setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
                handleOrderConfirmation(cartBook)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    // Метод для обработки подтверждения заказа
    private fun handleOrderConfirmation(cartBooks: ArrayList<Book>) {
        val ipAddress = (activity as MainActivity).getIpAddress()
        val userId = (activity as MainActivity).getIdUser()

        for (book in cartBooks) {
            // Запуск корутины для каждого запроса
            CoroutineScope(Dispatchers.IO).launch {

                // Отправляем уведомление о покупке книги
                sendNotification(book, ipAddress, userId)

                // Добавляем книгу в массив купленных книг
                addBookToPurchased(book.id)

                // Удаляем книгу из корзины
                removeFromCart(book.id)
            }
        }

        // Выводим увеломление о успешной покупке
        Toast.makeText(activity, getString(R.string.order_confirmation_success), Toast.LENGTH_LONG).show()

        // Переключаем экран пользователя на главную страницу
        findNavController().navigate(R.id.action_CartFragment_to_HomeFragment)
    }

    // Запрос на добавление новой книги в купленное
    private suspend fun addBookToPurchased(bookId: Int) {
        withContext(Dispatchers.IO) {
            try {
                val ipAddress = (context as MainActivity).getIpAddress()
                val userId = (context as MainActivity).getIdUser()

                val url = URL("http:$ipAddress:3000/api/users/add_purchased/$userId/$bookId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val responseCode = connection.responseCode
                val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    println("Книга успешно добавлена в купленное")
                } else {
                    val jsonResponse = JSONObject(responseMessage)
                    val message = jsonResponse.optString("message", "Ошибка при добавлении книги в купленное")
                    Log.d("addBookToFavorite", "Ошибка: $message")
                }
            } catch (e: Exception) {
                // Логируем исключение
                Log.d("addBookToFavorite", "Ошибка при отправке запроса: ${e.message}")
            }
        }
    }

    // Запрос на удаления книги из корзины
    private fun removeFromCart(bookId: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val ipAddress = (context as MainActivity).getIpAddress()
                val userId = (context as MainActivity).getIdUser()

                val url = URL("http:$ipAddress:3000/api/users/$userId/cart/$bookId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "DELETE"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    println("Книга успешно удалена из корзины пользователя")
                } else {
                    println("Ошибка при удалении книги из корзины пользователя: $responseCode")
                }
            } catch (e: Exception) {
                println("Ошибка при удалении книги из корзины пользователя: ${e.message}")
            }
        }
    }

    // Метод для отправки уведомления
    private suspend fun sendNotification(book: Book, ipAddress: String, userId: Int) {
        val newNotificationJson = """
        {
          "bookId": "${book.id}",
          "time": "${getCurrentDateTimeFormatted()}",
          "image": "${book.imageResource}"
        }
    """.trimIndent()

        val url = URL("http:$ipAddress:3000/api/users/$userId/notifications")
        val connection = withContext(Dispatchers.IO) {
            url.openConnection() as HttpURLConnection
        }
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        try {
            withContext(Dispatchers.IO) {
                val outputStream = OutputStreamWriter(connection.outputStream)
                outputStream.write(newNotificationJson)
                outputStream.flush()
            }

            val responseMessage = withContext(Dispatchers.IO) {
                if (connection.responseCode == HttpURLConnection.HTTP_CREATED) {
                    "Уведомление успешно добавлено"
                } else {
                    "Ошибка при добавлении уведомления: ${connection.responseMessage}"
                }
            }
            println(responseMessage)
        } catch (e: Exception) {
            println("Ошибка при отправке запроса: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }

    // Метод, для получения текущей даты и времени
    private fun getCurrentDateTimeFormatted(): String {
        val dateFormat = SimpleDateFormat("HH:mm dd MMM yyyy", Locale.ENGLISH)
        val date = Date()
        return dateFormat.format(date)
    }

    // Метод, который реализует вывод всех книг в корзине на экран
    private fun fetchBooksAndUpdateUI() {
        lifecycleScope.launch {
            try {
                var totalPrice = 0
                val books = withContext(Dispatchers.IO) {
                    fetchBooksFromServer()
                }

                // Добавляем все книги в корзине в массив
                val cartBooks = ArrayList<Book>()
                for (book in books) {
                    val purchasedBook = withContext(Dispatchers.IO) {
                        fetchCartBooksFromServer(book)
                    }
                    cartBooks.add(purchasedBook)
                    totalPrice += purchasedBook.price
                }

                cartBooksNew = cartBooks

                // Обновляем пользовательский интерфейс
                updateUIWithCartBooks(cartBooks, totalPrice)
            } catch (e: Exception) {
                showNoCartBooksNotification()
            }
        }
    }

    // Метод для обновления пользовательского интерфейса
    private fun updateUIWithCartBooks(cartBook: ArrayList<Book>, totalPrice: Int) {
        recalculationSum(totalPrice)
        val adapter = CustomAdapterCart(cartBook, requireContext(), this@CartFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
        binding.recyclerView.adapter = adapter
    }

    // Метод, который обрабатывает случай - пустого массива купленных книг
    private fun showNoCartBooksNotification() {
        // Получаем родительский ConstraintLayout
        val constraintLayout = binding.ConstraintLayout

        // Создаем новое представление
        val newView = LayoutInflater.from(requireContext()).inflate(R.layout.notification_no_cart_book, null)

        // Определяем параметры размещения для нового представления
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        // Устанавливаем отношения в параметрах размещения для нового представления
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        params.topToBottom = R.id.blackRectangle
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID

        // Устанавливаем новые параметры размещения для нового представления
        newView.layoutParams = params

        // Удаляем старые представления из родительского ConstraintLayout
        constraintLayout.removeView(binding.recyclerView)
        constraintLayout.removeView(binding.cardView)

        // Добавляем новое представление в родительский ConstraintLayout
        constraintLayout.addView(newView)
    }

    // Возвращаем книгу в корзине по id
    private fun fetchCartBooksFromServer(bookId: Int): Book {
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

    // Возвращаем все id книг находящихся в корзине
    private fun fetchBooksFromServer(): ArrayList<Int> {
        val ipAddress = (activity as MainActivity).getIpAddress()
        val userId = (activity as MainActivity).getIdUser()

        val url = URL("http:$ipAddress:3000/api/users/$userId/cart")
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

    // Пересчет общей суммы всех книг в корзине
    fun recalculationSum(totalPrice: Int){
        binding.totalPrice.text = "$totalPrice\u20BD"
    }

    // Возврат значения общей суммы
    fun getTotalSum(): Int {
        val totalPriceRubles = binding.totalPrice.text.toString()
        return totalPriceRubles.substringBefore('\u20BD').toIntOrNull() ?: 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}