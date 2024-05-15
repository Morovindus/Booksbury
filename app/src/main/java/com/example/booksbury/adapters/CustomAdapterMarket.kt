package com.example.booksbury.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.fragments.ExploreFragment
import com.example.booksbury.items.Book
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Адаптер для списка книг в магазине
class CustomAdapterMarket(private val items: ArrayList<Book>, private val context: Context, private val exploreFragment: ExploreFragment) : RecyclerView.Adapter<CustomAdapterMarket.ViewHolder>() {

    // Создание нового ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_explore, parent, false)
        return ViewHolder(view)
    }

    // Привязка данных к ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        // Загрузка изображения книги с помощью Picasso
        Picasso.get().load(currentItem.imageResource).into(holder.imageBook)

        // Установка заголовка книги, имени автора и цены книги
        holder.titleBook.text = currentItem.titleBook
        holder.nameAuthor.text = currentItem.nameAuthor
        holder.price.text = "${currentItem.price}\u20BD"

        // Слушатель нажатия на изображение книги
        holder.imageBook.setOnClickListener {
            val id = currentItem.id
            exploreFragment.navigateToBookInfoFragment(id)
        }

        // Слушатель нажатия на кнопку "Купить"
        holder.buttonBuy.setOnClickListener {

            // Запуск корутины
            (context as MainActivity).lifecycleScope.launch {
                val isBookInCart = fetchBookFromCartFromServer(currentItem.id)
                val isBookPurchased = fetchBookFromPurchasedFromServer(currentItem.id)

                // Показ уведомления должен быть выполнен на главном потоке
                withContext(Dispatchers.Main) {
                    if (isBookPurchased) {
                        Toast.makeText(context, context.getString(R.string.book_already_purchased), Toast.LENGTH_LONG).show()
                    } else if (isBookInCart) {
                        Toast.makeText(context, context.getString(R.string.book_already_in_cart), Toast.LENGTH_LONG).show()
                    } else {
                        addBookToCart(currentItem.id)
                        Toast.makeText(context, context.getString(R.string.book_added_to_cart), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Получение количества элементов в списке
    override fun getItemCount(): Int {
        return items.size
    }

    // ViewHolder для отображения элемента списка книг в магазине
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageBook: ImageView = itemView.findViewById(R.id.imageBook)
        val titleBook: TextView = itemView.findViewById(R.id.book_title)
        val nameAuthor: TextView = itemView.findViewById(R.id.book_author)
        val price: TextView = itemView.findViewById(R.id.price)
        val buttonBuy: Button = itemView.findViewById(R.id.button_buy)
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

    // Запрос на добавление новой книги в корзину
    suspend fun addBookToCart(bookId: Int) {
        withContext(Dispatchers.IO) {
            try {
                val ipAddress = (context as MainActivity).getIpAddress()
                val userId = (context as MainActivity).getIdUser()

                val url = URL("http://$ipAddress:3000/add_cart/$userId/$bookId")
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
}