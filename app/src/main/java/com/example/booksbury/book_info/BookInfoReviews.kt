package com.example.booksbury.book_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterReview
import com.example.booksbury.databinding.BookInfoReviewsBinding
import com.example.booksbury.items.Reviews

class BookInfoReviews() : Fragment() {

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

    fun setAuthorAbout(reviews: ArrayList<Reviews>) {
        var commented = false
        var userReview: Reviews? = null

        val idUser = (activity as MainActivity).getIdUser()

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


        // Получаем ссылку на ConstraintLayout
        val constraintLayout = binding.ConstraintLayout

        // Создаем LayoutParams для нового представления
        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        // Устанавливаем ограничения для нового представления
        layoutParams.bottomToTop = binding.recyclerView.id
        layoutParams.startToStart = constraintLayout.id
        layoutParams.endToEnd = constraintLayout.id
        layoutParams.topToTop = constraintLayout.id
        layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.onlyPadding)
        layoutParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.basePadding)


        if (commented){
            // Добавляем уже существующий комментарий пользователя
            val inflatedView = inflater.inflate(R.layout.user_review, null)
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

            constraintLayout.addView(inflatedView, layoutParams)
        } else {
            // Добавляем кнопку "Добавление нового отзыва"
            inflater.inflate(R.layout.button_write_review, binding.container, true)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.ConstraintLayout.removeAllViews()
        _binding = null
    }
}