package com.example.booksbury.book_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterReview
import com.example.booksbury.databinding.BookInfoReviewsBinding
import com.example.booksbury.items.Reviews
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

// Класс фрагмента отображения отзывов о книге
class BookInfoReviews(private val idBook: Int) : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: BookInfoReviewsBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // Приватное свойство для хранения объекта LayoutInflater
    private lateinit var inflater: LayoutInflater

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.inflater = inflater
        _binding = BookInfoReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Выполнение запроса на получение всех отзывов и обновление пользовательского интерфейса
        fetchDataFromServer()
    }

    // Метод, выводящий все отзывы на экран
    private fun fetchDataFromServer() {
        lifecycleScope.launch {
            val reviews = fetchReviewsDataFromServer(idBook)
            val idUser = (activity as? MainActivity)?.getIdUser()

            val items = ArrayList<Reviews>()
            var userReview: Reviews? = null

            // Разделение отзывов на пользовательские и остальные
            reviews.forEach { review ->
                if (review.id != idUser) {
                    items.add(review)
                } else {
                    userReview = review
                }
            }

            // Обновляем пользовательский интерфейс
            updateRecyclerView(items)

            // Получение информации о том, приобрел ли пользователь данную книгу
            val isBookPurchased = fetchBookFromPurchasedFromServer(idBook)

            userReview?.let { review ->
                // Добавляем уже существующий комментарий пользователя
                addUserReviewToLayout(review)
            }
            if (userReview == null && isBookPurchased) {
                // Добавляем кнопку "Добавление нового отзыва"
                binding.container.addView(inflater.inflate(R.layout.button_write_review, binding.container, false))
            }
        }
    }

    // Метод для добавления, комментария пользователя
    private fun addUserReviewToLayout(review: Reviews) {
        binding.container.addView(
            inflater.inflate(R.layout.user_review, binding.container, false).apply {
                val dateTextView = findViewById<TextView>(R.id.date_reviews)
                val starFirst = findViewById<ImageView>(R.id.starFirst)
                val starSecond = findViewById<ImageView>(R.id.starSecond)
                val starThird = findViewById<ImageView>(R.id.starThird)
                val starFourth = findViewById<ImageView>(R.id.starFourth)
                val starFifth = findViewById<ImageView>(R.id.starFifth)

                val orangeStarDrawable = R.drawable.star_orange

                if (review.stars >= 1) starFirst.setImageResource(orangeStarDrawable)
                if (review.stars >= 2) starSecond.setImageResource(orangeStarDrawable)
                if (review.stars >= 3) starThird.setImageResource(orangeStarDrawable)
                if (review.stars >= 4) starFourth.setImageResource(orangeStarDrawable)
                if (review.stars >= 5) starFifth.setImageResource(orangeStarDrawable)

                val textReviewsTextView = findViewById<TextView>(R.id.text_reviews)

                dateTextView.text = review.date
                textReviewsTextView.text = review.textUser
            }
        )
    }

    // Метод для обновления пользовательского интерфейса
    private fun updateRecyclerView(items: ArrayList<Reviews>) {
        val adapter = CustomAdapterReview(items)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
        binding.recyclerView.adapter = adapter
    }

    // Запрос, возвращающий все отзывы по id книги
    suspend fun fetchReviewsDataFromServer(bookId: Int): ArrayList<Reviews> {
        return withContext(Dispatchers.IO) {
            val reviews = ArrayList<Reviews>()
            val ipAddress = (activity as MainActivity).getIpAddress()

            val url = URL("http://$ipAddress:3000/api/books/$bookId/reviews")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val response = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }

                val jsonArray = JSONArray(response)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val review = Reviews(
                        jsonObject.getInt("_id"),
                        jsonObject.getString("nameUser"),
                        jsonObject.getString("date"),
                        jsonObject.getString("textUser"),
                        jsonObject.getInt("stars")
                    )
                    reviews.add(review)
                }
            } else {
                println("HTTP Error: $responseCode")
            }

            connection.disconnect()
            reviews
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.ConstraintLayout.removeAllViews()
        _binding = null
    }
}