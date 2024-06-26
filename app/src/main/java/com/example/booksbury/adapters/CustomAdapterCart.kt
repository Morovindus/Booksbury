package com.example.booksbury.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.booksbury.R
import com.example.booksbury.entity.Book
import com.example.booksbury.fragments.CartFragment
import com.example.booksbury.model.BookViewModel
import com.example.booksbury.model.UserViewModel
import com.squareup.picasso.Picasso

// Адаптер для списка товаров в корзине
class CustomAdapterCart(private val items: ArrayList<Book>, private val cartFragment: CartFragment) : RecyclerView.Adapter<CustomAdapterCart.ViewHolder>() {

    // ViewModel для хранения состояния
    private lateinit var viewModelBook: BookViewModel

    // ViewModel для хранения состояния
    private lateinit var viewModelUser: UserViewModel

    // Создание нового ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    // Привязка данных к ViewHolder
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        // Инициализация ViewModel
        viewModelBook = ViewModelProvider(cartFragment)[BookViewModel::class.java]
        viewModelUser = ViewModelProvider(cartFragment)[UserViewModel::class.java]

        // Загрузка обложки книги с помощью Picasso
        Picasso.get().load(currentItem.imageResource).into(holder.imageCoverBook)

        // Установка заголовка книги, имени автора, рейтинга книги и цены книги
        holder.titleBook.text = currentItem.titleBook
        holder.nameAuthor.text = currentItem.nameAuthor
        holder.ratings.text = "${currentItem.ratings} ${cartFragment.getString(R.string.ratings)}"
        holder.price.text = "${currentItem.price}\u20BD"

        // Установка звездочек рейтинга книги
        val orangeStarDrawable = R.drawable.star_orange
        if (currentItem.stars >= 1) holder.starFirst.setImageResource(orangeStarDrawable)
        if (currentItem.stars >= 2) holder.starSecond.setImageResource(orangeStarDrawable)
        if (currentItem.stars >= 3) holder.starThird.setImageResource(orangeStarDrawable)
        if (currentItem.stars >= 4) holder.starFourth.setImageResource(orangeStarDrawable)
        if (currentItem.stars >= 5) holder.starFifth.setImageResource(orangeStarDrawable)

        // Обработка нажатия на кнопку удаления книги из корзины
        holder.buttonDelete.setOnClickListener {
            val removedItem = items.removeAt(position) // Удаление элемента из списка и получение удаленного элемента
            removeFromCart(removedItem.id) // Удаление элемента из корзины

            // Пересчет общей суммы корзины
            val newSum = cartFragment.getTotalSum() - removedItem.price
            cartFragment.recalculationSum(newSum)

            notifyItemRemoved(position) // Уведомление адаптера об удалении элемента из списка
        }

    }

    // Получение количества элементов в списке
    override fun getItemCount(): Int {
        return items.size
    }

    // ViewHolder для отображения элемента списка книг в корзине
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageCoverBook: ImageView = itemView.findViewById(R.id.imageCoverBook)
        val titleBook: TextView = itemView.findViewById(R.id.title_book)
        val nameAuthor: TextView = itemView.findViewById(R.id.name_author)
        val ratings: TextView = itemView.findViewById(R.id.textRating)
        val price: TextView = itemView.findViewById(R.id.price)

        val starFirst: ImageView = itemView.findViewById(R.id.starFirst)
        val starSecond: ImageView = itemView.findViewById(R.id.starSecond)
        val starThird: ImageView = itemView.findViewById(R.id.starThird)
        val starFourth: ImageView = itemView.findViewById(R.id.starFourth)
        val starFifth: ImageView = itemView.findViewById(R.id.starFifth)

        val buttonDelete: Button = itemView.findViewById(R.id.button_delete)
    }

    // Запрос на удаления книги из корзины
    private fun removeFromCart(bookId: Int) {

        // Посылаем запрос на удаление книги из избранного
        viewModelUser.deleteCartBook(viewModelBook.idUser, bookId) { success, error ->
            if (success) {
                println("Книга успешно удалена из корзины пользователя")
            } else {
                println("Ошибка: $error")
            }
        }
    }
}
