package com.example.booksbury.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.booksbury.model.BookViewModel
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.databinding.ReviewFragmentBinding
import com.example.booksbury.dialog.MyDialogFragmentReview
import com.example.booksbury.items.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Класс фрагмента добавления отзыва
class ReviewFragment : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: ReviewFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // ViewModel для хранения состояния
    private lateinit var viewModel: BookViewModel

    // Блок companion object для хранения констант
    companion object {
        // Ключ для сохранения и восстановления идентификатора книги, идентификатора пользователя и количество звезд
        const val ENTERED_ID_BOOK_KEY = "savedIdBook"
        const val ENTERED_ID_USER_KEY = "savedIdUser"
        const val ENTERED_STAR_KEY = "savedStar"
    }

    // Метод, вызываемый при создании фрагмента
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Инициализация ViewModel
        viewModel = ViewModelProvider(this).get(BookViewModel::class.java)

        // Восстанавливаем сохраненное значение, если оно есть
        savedInstanceState?.let {
            viewModel.idBook = it.getInt(ENTERED_ID_BOOK_KEY, viewModel.idBook)
            viewModel.idUser = it.getInt(ENTERED_ID_USER_KEY, viewModel.idUser)
            viewModel.star = it.getInt(ENTERED_STAR_KEY, viewModel.star)
        }
    }

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ReviewFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем значение id звезды из предыдущего фрагмента, если оно еще не было установлено
        if (viewModel.star == 0) {
            viewModel.star = 0
        }

        // Получаем значение id книги из предыдущего фрагмента, если оно еще не было установлено
        if (viewModel.idBook == 0) {
            viewModel.idBook = (activity as MainActivity).getIdBook()
        }

        // Получаем значение id пользователя из предыдущего фрагмента, если оно еще не было установлено
        if (viewModel.idUser == 0) {
            viewModel.idUser = (activity as MainActivity).getIdUser()
        }

        // Инициализация звезд
        val starViews = listOf(
            binding.starFirst,
            binding.starSecond,
            binding.starThird,
            binding.starFourth,
            binding.starFifth
        )

        // Восстанавливаем количество звезд
        setRating(viewModel.star, starViews)

        // Установка слушателей клика для звезд
        starViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                setRating(index + 1, starViews)
            }
        }

        // Сброс текста и скрытие клавиатуры при нажатии на кнопку
        binding.buttonCancel.setOnClickListener {
            binding.reviewBar.text.clear()
            hideKeyboard(binding.reviewBar)
        }

        // Установка слушателя для кнопки "Назад"
        binding.buttonBack.setOnClickListener {
            (activity as MainActivity).setIdBook(viewModel.idBook)

            findNavController().popBackStack()
        }

        // Установка слушателя для кнопки "Добавить"
        binding.buttonConfirm.setOnClickListener {
            val reviewText = binding.reviewBar.text.toString().trim()

            // Проверка на наличие оценки
            if (viewModel.star == 0) {
                Toast.makeText(context, getString(R.string.pls_select_rating), Toast.LENGTH_SHORT).show()

                // Проверка на заполненность поля отзыва
            } else if (reviewText.isEmpty()) {
                binding.reviewBar.error = getString(R.string.pls_write_review)
            } else {
                lifecycleScope.launch {
                    // Получение имени пользователя и почты
                    val user: User = withContext(Dispatchers.IO) {
                        fetchNameEmailDataFromServer()
                    }

                    // Отправка запроса на добавление нового отзыва
                    addReview(user.userName, reviewText)

                    // Показ диалогового окна после успешного добавления отзыва
                    val dialogFragment = MyDialogFragmentReview(this@ReviewFragment)
                    dialogFragment.show(parentFragmentManager, "dialog")
                }
            }
        }

        // Скрываем кнопку при запуске активности
        binding.buttonCancel.visibility = View.INVISIBLE
        binding.buttonCancel.isEnabled = false

        // Устанавливаем слушатель для изменений текста в текстовом поле
        binding.reviewBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Если текстовое поле не пустое, показываем кнопку, иначе скрываем
                if (s.isNullOrEmpty()) {
                    binding.buttonCancel.visibility = Button.INVISIBLE
                    binding.buttonCancel.isEnabled = false
                } else {
                    binding.buttonCancel.visibility = Button.VISIBLE
                    binding.buttonCancel.isEnabled = true
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Метод для установки визуальной оценки на экран
    private fun setRating(rating: Int, starViews: List<ImageView>) {
        viewModel.star = rating
        // Изменение изображения звезд в зависимости от выбранного рейтинга
        starViews.forEachIndexed { index, imageView ->
            if (index < rating) {
                imageView.setImageResource(R.drawable.star_orange)
            } else {
                imageView.setImageResource(R.drawable.star_white)
            }
        }
    }

    // Запрос для добавления нового отзыва
    private suspend fun addReview(nameUser: String, textUser: String) {
        withContext(Dispatchers.IO) {
            val ipAddress = (activity as MainActivity).getIpAddress()
            val url = URL("http:$ipAddress:3000/api/books/${viewModel.idBook}/reviews")
            val date = getCurrentDateFormatted()

            val reviewJson = JSONObject().apply {
                put("userId", viewModel.idUser)
                put("nameUser", nameUser)
                put("date", date)
                put("textUser", textUser)
                put("stars", viewModel.star)
            }

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            try {
                val outputStream = OutputStreamWriter(connection.outputStream)
                outputStream.write(reviewJson.toString())
                outputStream.flush()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    println("Отзыв успешно добавлен")
                } else {
                    println("Ошибка при добавлении отзыва: ${connection.responseMessage}")
                }
            } catch (e: Exception) {
                println("Ошибка при отправке запроса: ${e.message}")
            } finally {
                connection.disconnect()
            }
        }
    }

    // Запрос возвращающий имя и почту пользователя
    private suspend fun fetchNameEmailDataFromServer(): User {
        return withContext(Dispatchers.IO) {
            val ipAddress = (activity as MainActivity).getIpAddress()
            val url = URL("http:$ipAddress:3000/api/user/${viewModel.idUser}")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            val jsonResponse = JSONObject(response)
            val userName = jsonResponse.getString("username")
            val email = jsonResponse.getString("email")

            User(userName, email)
        }
    }

    // Метод, для получения текущей даты
    private fun getCurrentDateFormatted(): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val date = Date()
        return dateFormat.format(date)
    }

    // Метод возвращающий пользователя на начальный экран
    fun navigateToSignInFragment() {
        findNavController().navigate(R.id.action_ReviewFragment_to_HomeFragment)
    }

    // Метод для скрытия клавиатуры
    fun hideKeyboard(editText: EditText) {
        val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    // Метод, для сохранения введенного текста
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ENTERED_ID_BOOK_KEY, viewModel.idBook)
        outState.putInt(ENTERED_ID_USER_KEY, viewModel.idUser)
        outState.putInt(ENTERED_STAR_KEY, viewModel.star)
    }

    // Метод, вызываемый перед уничтожением представления фрагмента
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}