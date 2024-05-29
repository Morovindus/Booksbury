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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.entity.Book
import com.example.booksbury.fragments.ExploreFragment
import com.example.booksbury.model.BookViewModel
import com.example.booksbury.model.UserViewModel
import com.squareup.picasso.Picasso

// Адаптер для списка книг в магазине
class CustomAdapterMarket(private val items: ArrayList<Book>, private val context: Context, private val exploreFragment: ExploreFragment) : RecyclerView.Adapter<CustomAdapterMarket.ViewHolder>() {

    // ViewModel для хранения состояния
    private lateinit var viewModelBook: BookViewModel

    // ViewModel для хранения состояния
    private lateinit var viewModelUser: UserViewModel

    // Создание нового ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_explore, parent, false)
        return ViewHolder(view)
    }

    // Привязка данных к ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        // Инициализация ViewModel
        viewModelBook = ViewModelProvider(exploreFragment)[BookViewModel::class.java]
        viewModelUser = ViewModelProvider(exploreFragment)[UserViewModel::class.java]

        // Загрузка изображения книги с помощью Picasso
        Picasso.get().load(currentItem.imageResource).into(holder.imageBook)

        // Установка заголовка книги, имени автора и цены книги
        holder.titleBook.text = currentItem.titleBook
        holder.nameAuthor.text = currentItem.nameAuthor
        holder.price.text = "${currentItem.price}\u20BD"

        // Слушатель нажатия на изображение книги
        holder.imageBook.setOnClickListener {
            exploreFragment.navigateToBookInfoFragment(currentItem.id)
        }

        // Слушатель нажатия на кнопку "Купить"
        holder.buttonBuy.setOnClickListener {

            Log.d("myLogs", (context as MainActivity).getIdUser().toString())

            viewModelUser.isBookPurchased((context as MainActivity).getIdUser(), currentItem.id) { isBookPurchased, error ->
                if (isBookPurchased != null) {
                    if (isBookPurchased) {
                        Toast.makeText(context, context.getString(R.string.book_already_purchased), Toast.LENGTH_LONG).show()
                    } else {
                        viewModelUser.isBookInCart(context.getIdUser(), currentItem.id) { isBookInCart, error ->
                            if (isBookInCart != null) {
                                if (isBookInCart) {
                                    Toast.makeText(context, context.getString(R.string.book_already_in_cart), Toast.LENGTH_LONG).show()
                                } else {
                                    addBookToCart(currentItem.id)
                                }
                            } else {
                                println("Ошибка: $error")
                            }
                        }
                    }
                } else {
                    println("Ошибка: $error")
                }
            }
        }
    }

    // Получение количества элементов в списке
    override fun getItemCount() = items.size

    // ViewHolder для отображения элемента списка книг в магазине
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageBook: ImageView = itemView.findViewById(R.id.imageBook)
        val titleBook: TextView = itemView.findViewById(R.id.book_title)
        val nameAuthor: TextView = itemView.findViewById(R.id.book_author)
        val price: TextView = itemView.findViewById(R.id.price)
        val buttonBuy: Button = itemView.findViewById(R.id.button_buy)
    }

    // Запрос на добавление новой книги в корзину
    private fun addBookToCart(bookId: Int) {

        // Добавление книги в корзину
        viewModelUser.addBookToCart((context as MainActivity).getIdUser(), bookId) { success, message ->
            if (success) {
                Toast.makeText(context, context.getString(R.string.book_added_to_cart), Toast.LENGTH_LONG).show()
            } else {
                println("Ошибка при добавлении книги в корзину: $message")
            }
        }
    }
}