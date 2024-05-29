package com.example.booksbury.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.entity.Notification
import com.example.booksbury.fragments.NotificationFragment
import com.example.booksbury.model.BookViewModel
import com.squareup.picasso.Picasso

// Адаптер для списка уведомлений
class CustomAdapterNotification(private val items: List<Notification>, private val context: Context, private val notificationFragment: NotificationFragment) : RecyclerView.Adapter<CustomAdapterNotification.ViewHolder>() {

    // ViewModel для хранения состояния
    private lateinit var viewModel: BookViewModel

    // Создание нового ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    // Привязка данных к ViewHolder
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        // Инициализация ViewModel
        viewModel = ViewModelProvider(notificationFragment).get(BookViewModel::class.java)

        // Загрузка изображения книги с помощью Picasso
        Picasso.get().load(currentItem.image).into(holder.imageCoverBook)

        val language = (context as MainActivity).getLanguage()
        viewModel.getTitleBook(currentItem.bookId, language) { titleBook, error ->
            if (titleBook != null) {
                holder.titleBook.text = titleBook
            } else {
                println("Error: $error")
            }
        }

        // Установка текста уведомления и времени
        holder.titleNotification.text = notificationFragment.getString(R.string.you_just_buy)
        holder.titleTime.text = currentItem.time

        // Слушатель нажатия на изображение книги
        holder.imageCoverBook.setOnClickListener {
            notificationFragment.navigateToBookInfoFragment(currentItem.bookId)
        }
    }

    // Получение количества элементов в списке
    override fun getItemCount() = items.size

    // ViewHolder для отображения элемента списка приобретенных книг
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageCoverBook: ImageView = itemView.findViewById(R.id.imageCoverBook)
        val titleNotification: TextView = itemView.findViewById(R.id.title_notification)
        val titleBook: TextView = itemView.findViewById(R.id.title_book)
        val titleTime: TextView = itemView.findViewById(R.id.title_time)
    }
}