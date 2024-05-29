package com.example.booksbury.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booksbury.R
import com.example.booksbury.entity.Reviews

// Адаптер для списка отзывов
class CustomAdapterReview(private val items: List<Reviews>) : RecyclerView.Adapter<CustomAdapterReview.ViewHolder>() {

    // Создание нового ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ViewHolder(view)
    }

    // Привязка данных к ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        // Установка имени пользователя, даты отзыва и текста отзыва
        holder.nameUser.text = currentItem.nameUser
        holder.dateReviews.text = currentItem.date
        holder.textReviews.text = currentItem.textUser

        // Установка звездочек для рейтинга отзыва
        val orangeStarDrawable = R.drawable.star_orange
        if (currentItem.stars >= 1) holder.starFirst.setImageResource(orangeStarDrawable)
        if (currentItem.stars >= 2) holder.starSecond.setImageResource(orangeStarDrawable)
        if (currentItem.stars >= 3) holder.starThird.setImageResource(orangeStarDrawable)
        if (currentItem.stars >= 4) holder.starFourth.setImageResource(orangeStarDrawable)
        if (currentItem.stars >= 5) holder.starFifth.setImageResource(orangeStarDrawable)
    }

    // Получение количества элементов в списке
    override fun getItemCount() = items.size

    // ViewHolder для отображения элемента списка отзывов
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameUser: TextView= itemView.findViewById(R.id.name_user)
        val dateReviews: TextView = itemView.findViewById(R.id.date_reviews)
        val textReviews: TextView = itemView.findViewById(R.id.text_reviews)

        val starFirst: ImageView = itemView.findViewById(R.id.starFirst)
        val starSecond: ImageView = itemView.findViewById(R.id.starSecond)
        val starThird: ImageView = itemView.findViewById(R.id.starThird)
        val starFourth: ImageView = itemView.findViewById(R.id.starFourth)
        val starFifth: ImageView = itemView.findViewById(R.id.starFifth)
    }
}