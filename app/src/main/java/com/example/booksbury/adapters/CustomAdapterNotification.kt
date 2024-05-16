package com.example.booksbury.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.fragments.NotificationFragment
import com.example.booksbury.items.Notification
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Адаптер для списка уведомлений
class CustomAdapterNotification(private val items: ArrayList<Notification>, private val context: Context, private val notificationFragment: NotificationFragment) : RecyclerView.Adapter<CustomAdapterNotification.ViewHolder>() {

    // Создание нового ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    // Привязка данных к ViewHolder
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        // Загрузка изображения книги с помощью Picasso
        Picasso.get().load(currentItem.imageResource).into(holder.imageCoverBook)

        (context as MainActivity).lifecycleScope.launch {
            // Установка заголовка книги
            val titleBook = getTitleBookByIdAndLang(currentItem.bookId)

            withContext(Dispatchers.Main) {
                holder.titleBook.text = titleBook
            }
        }

        // Установка текста уведомления и времени
        holder.titleNotification.text = notificationFragment.getString(R.string.you_just_buy)
        holder.titleTime.text = currentItem.time

        // Слушатель нажатия на изображение книги
        holder.imageCoverBook.setOnClickListener {
            val id = currentItem.bookId
            notificationFragment.navigateToBookInfoFragment(id)
        }
    }

    // Получение количества элементов в списке
    override fun getItemCount(): Int {
        return items.size
    }

    // ViewHolder для отображения элемента списка приобретенных книг
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageCoverBook: ImageView = itemView.findViewById(R.id.imageCoverBook)
        val titleNotification: TextView = itemView.findViewById(R.id.title_notification)
        val titleBook: TextView = itemView.findViewById(R.id.title_book)
        val titleTime: TextView = itemView.findViewById(R.id.title_time)
    }

    // Запрос на получение названия книги по языку и id книги
    private suspend fun getTitleBookByIdAndLang(bookId: Int): String {
        return withContext(Dispatchers.IO) {
            val ipAddress = (context as MainActivity).getIpAddress()
            val language = (context as MainActivity).getLanguage()

            val url = URL("http:$ipAddress:3000/api/users/titleBook/$bookId/$language")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            val jsonObject = JSONObject(response)
            jsonObject.getString("title")
        }
    }
}