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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class BookInfoReviews(private val idBook: Int) : Fragment() {

    private var _binding: BookInfoReviewsBinding? = null
    private val binding get() = _binding!!
    private lateinit var inflater: LayoutInflater
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.inflater = inflater
        _binding = BookInfoReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchDataFromServer()
    }

    private fun fetchDataFromServer() {
        lifecycleScope.launch {
            val reviews = fetchReviewsDataFromServer(idBook)

            var commented = false
            var userReview: Reviews? = null

            val idUser = (activity as? MainActivity)?.getIdUser()

            val items = ArrayList<Reviews>()

            for (review in reviews) {
                if (review.id != idUser)
                {
                    items.add(
                        Reviews(
                            review.id,
                            review.nameUser,
                            review.date,
                            review.textUser,
                            review.stars
                        )
                    )
                } else {
                    commented = true
                    userReview = Reviews(
                        review.id,
                        review.nameUser,
                        review.date,
                        review.textUser,
                        review.stars
                    )
                }
            }

            val adapter = CustomAdapterReview(items)
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
            binding.recyclerView.adapter = adapter


            if (commented){
                // Добавляем уже существующий комментарий пользователя

                val inflatedView = inflater.inflate(R.layout.user_review, binding.container,true)
                inflatedView.id = View.generateViewId()

                val dateTextView = inflatedView.findViewById<TextView>(R.id.date_reviews)
                val starFirst = inflatedView.findViewById<ImageView>(R.id.starFirst)
                val starSecond =inflatedView.findViewById<ImageView>(R.id.starSecond)
                val starThird =inflatedView.findViewById<ImageView>(R.id.starThird)
                val starFourth =inflatedView.findViewById<ImageView>(R.id.starFourth)
                val starFifth = inflatedView.findViewById<ImageView>(R.id.starFifth)

                val orangeStarDrawable = R.drawable.star_orange

                if (userReview != null) {
                    if (userReview.stars >= 1) starFirst.setImageResource(orangeStarDrawable)
                    if (userReview.stars >= 2) starSecond.setImageResource(orangeStarDrawable)
                    if (userReview.stars >= 3) starThird.setImageResource(orangeStarDrawable)
                    if (userReview.stars >= 4) starFourth.setImageResource(orangeStarDrawable)
                    if (userReview.stars >= 5) starFifth.setImageResource(orangeStarDrawable)

                    val textReviewsTextView = inflatedView.findViewById<TextView>(R.id.text_reviews)

                    dateTextView.text = userReview.date
                    textReviewsTextView.text = userReview.textUser
                }
            } else {
                // Добавляем кнопку "Добавление нового отзыва"
                inflater.inflate(R.layout.button_write_review, binding.container, true)
            }

        }
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.ConstraintLayout.removeAllViews()
        _binding = null
    }
}