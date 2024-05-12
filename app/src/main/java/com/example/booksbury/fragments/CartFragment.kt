package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterCart
import com.example.booksbury.databinding.CartFragmentBinding
import com.example.booksbury.items.ItemCart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class CartFragment : Fragment() {

    private var _binding: CartFragmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = CartFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        recalculationSum(getString(R.string.total_price).toInt())


        GlobalScope.launch(Dispatchers.Main) {
            try {
                var totalPrice = 0
                val books = withContext(Dispatchers.IO) {
                    fetchBooksFromServer()
                }

                // Добавляем все книги в корзине в массив
                val purchasedBooks = ArrayList<ItemCart>()
                for (book in books) {
                    val purchasedBook = withContext(Dispatchers.IO) {
                        fetchCartBooksFromServer(book)
                    }
                    purchasedBooks.add(purchasedBook)
                    totalPrice += purchasedBook.price
                }

                // Обновляем пользовательский интерфейс в главном потоке
                recalculationSum(totalPrice)

                val adapter = CustomAdapterCart(purchasedBooks, requireContext(), this@CartFragment)
                binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
                binding.recyclerView.adapter = adapter


            } catch (e: Exception) {

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
        }
    }

    // Возвращаем книгу в корзине по id
    private fun fetchCartBooksFromServer(bookId: Int): ItemCart {
        val ipAddress = (activity as MainActivity).getIpAddress()
        val url = URL("http:$ipAddress:3000/book/bookById/$bookId")
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

        return ItemCart(id, smallCover, title, authorName, stars, ratings, price)
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