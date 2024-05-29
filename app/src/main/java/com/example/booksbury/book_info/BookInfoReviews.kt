package com.example.booksbury.book_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterReview
import com.example.booksbury.databinding.BookInfoReviewsBinding
import com.example.booksbury.entity.Reviews
import com.example.booksbury.model.BookViewModel
import com.example.booksbury.model.UserViewModel
import kotlinx.coroutines.launch

// Класс фрагмента отображения отзывов о книге
class BookInfoReviews : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: BookInfoReviewsBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // Приватное свойство для хранения объекта LayoutInflater
    private lateinit var inflater: LayoutInflater

    // ViewModel для хранения состояния
    private lateinit var viewModelBook: BookViewModel

    // ViewModel для хранения состояния
    private lateinit var viewModelUser: UserViewModel

    // Блок companion object для хранения констант
    companion object {
        // Ключ для сохранения и восстановления идентификатора книги и идентификатора пользователя
        const val ENTERED_ID_BOOK_KEY = "savedIdBook"
        const val ENTERED_ID_USER_KEY = "savedIdUser"
    }

    // Метод, вызываемый при создании фрагмента
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Инициализация ViewModel
        viewModelBook = ViewModelProvider(this)[BookViewModel::class.java]
        viewModelUser = ViewModelProvider(this)[UserViewModel::class.java]

        // Восстанавливаем сохраненное значение, если оно есть
        savedInstanceState?.let {
            viewModelBook.idUser = it.getInt(BookInfoFragment.ENTERED_ID_USER_KEY, viewModelBook.idUser)
            viewModelBook.idBook = it.getInt(BookInfoFragment.ENTERED_ID_BOOK_KEY, viewModelBook.idBook)
        }
    }

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

        // Получаем значение id книги из предыдущего фрагмента, если оно еще не было установлено
        if (viewModelBook.idBook == 0) {
            viewModelBook.idBook = (activity as MainActivity).getIdBook()
        }

        // Получаем значение id пользователя из предыдущего фрагмента, если оно еще не было установлено
        if (viewModelBook.idUser == 0) {
            viewModelBook.idUser = (activity as MainActivity).getIdUser()
        }

        // Выполнение запроса на получение всех отзывов и обновление пользовательского интерфейса
        fetchDataFromServer()
    }

    // Метод, выводящий все отзывы на экран
    private fun fetchDataFromServer() {
        // Получение отзывов с сервера
        viewModelBook.getBookReviews(viewModelBook.idBook) { reviews, error ->
            if (reviews != null) {
                // Разделение отзывов на пользовательские и остальные
                val (otherReviews, userReviews) = reviews.partition { it._id != viewModelBook.idUser }

                // Обновление списка отзывов на экране
                updateRecyclerView(otherReviews)

                // Проверка приобретения книги пользователем и добавление возможности написания отзыва
                val userReview = userReviews.firstOrNull() // Извлекаем отзыв пользователя или null, если его нет
                viewModelUser.isBookPurchased(viewModelBook.idUser, viewModelBook.idBook) { isBookPurchased, error ->
                    if (isBookPurchased != null) {
                        if (userReview == null && isBookPurchased) {
                            addUserReviewButtonToLayout()
                        } else {
                            userReview?.let { addUserReviewToLayout(it) }
                        }
                    } else {
                        println("Error: $error")
                    }
                }
            } else {
                println("Error: $error")
            }
        }
    }

    // Добавление кнопки "Добавить отзыв" в макет
    private fun addUserReviewButtonToLayout() {
        binding.container.addView(
            inflater.inflate(
                R.layout.button_write_review,
                binding.container,
                false
            )
        )

        val buttonReview = binding.container.findViewById<AppCompatImageButton>(R.id.button_write_review)
        // Слушатель нажатия на кнопку "Добавить отзыв"
        buttonReview.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val navController = findNavController()
                navController.navigate(R.id.action_BookInfoFragment_to_ReviewFragment)
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
    private fun updateRecyclerView(items: List<Reviews>) {
        val adapter = CustomAdapterReview(items)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
        binding.recyclerView.adapter = adapter
    }

    // Метод, для сохранения введенного текста
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ENTERED_ID_BOOK_KEY, viewModelBook.idBook)
        outState.putInt(ENTERED_ID_USER_KEY, viewModelBook.idUser)
    }

    // Метод, вызываемый перед уничтожением представления фрагмента
    override fun onDestroyView() {
        super.onDestroyView()
        binding.ConstraintLayout.removeAllViews()
        _binding = null
    }
}
