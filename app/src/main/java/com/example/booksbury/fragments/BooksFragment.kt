package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterBooks
import com.example.booksbury.databinding.BooksFragmentBinding
import com.example.booksbury.entity.Book
import com.example.booksbury.interfaces.OnBookClickListener
import com.example.booksbury.model.BookViewModel
import com.example.booksbury.model.UserViewModel

// Класс фрагмента отображения списка купленных книг
class BooksFragment : Fragment(), OnBookClickListener {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: BooksFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // ViewModel для хранения состояния
    private lateinit var viewModelBook: BookViewModel

    // ViewModel для хранения состояния
    private lateinit var viewModelUser: UserViewModel

    // Блок companion object для хранения констант
    companion object {
        // Ключ для сохранения и восстановления идентификатора пользователя
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
            viewModelBook.idUser = it.getInt(ENTERED_ID_USER_KEY, viewModelBook.idUser)
        }
    }

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BooksFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обработчик нажатия кнопки "Назад"
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Получаем значение id пользователя из предыдущего фрагмента, если оно еще не было установлено
        if (viewModelBook.idUser == 0) {
            viewModelBook.idUser = (activity as MainActivity).getIdUser()
        }

        // Получение списка книг с сервера и обновление пользовательского интерфейса
        fetchBooksAndUpdateUI()
    }

    // Метод, который реализует вывод всех купленных книг на экран
    private fun fetchBooksAndUpdateUI() {

        viewModelUser.getBookId(viewModelBook.idUser) { books, error ->
            if (books != null) {
                // Создаем массив для хранения всех купленных книг пользователем
                val purchasedBooks = ArrayList<Book>()
                val totalBooks = books.size
                var completedBooks = 0

                for (bookId in books) {
                    val language = (activity as MainActivity).getLanguage()
                    viewModelBook.getPurchasedBook(bookId, language) { purchasedBook, error ->
                        if (purchasedBook != null) {
                            purchasedBooks.add(purchasedBook)
                        } else {
                            println("Ошибка: $error")
                        }

                        // Увеличиваем счетчик завершенных вызовов
                        completedBooks++

                        // Проверяем, завершены ли все вызовы
                        if (completedBooks == totalBooks) {
                            // Обновляем пользовательский интерфейс
                            updateUIWithPurchasedBooks(purchasedBooks)
                        }
                    }
                }

                // Если books пуст, вызовы getPurchasedBook не произойдут, нужно обновить UI здесь
                if (totalBooks == 0) {
                    updateUIWithPurchasedBooks(purchasedBooks)
                }
            } else {
                showNoPurchasedBooksNotification()
                println("Error: $error")
            }
        }
    }

    // Метод для обновления пользовательского интерфейса
    private fun updateUIWithPurchasedBooks(purchasedBooks: ArrayList<Book>) {
        val adapter = CustomAdapterBooks(purchasedBooks, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
        binding.recyclerView.adapter = adapter
    }

    // Метод, который обрабатывает случай - пустого массива купленных книг
    private fun showNoPurchasedBooksNotification() {
        // Создаем новое представление
        val newView = LayoutInflater.from(requireContext()).inflate(R.layout.notification_no_purchased_books, null)

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

        // Удаляем старое представление RecyclerView из родительского ConstraintLayout
        val recyclerView = binding.recyclerView
        binding.ConstraintLayout.removeView(recyclerView)

        // Добавляем новое представление в родительский ConstraintLayout
        binding.ConstraintLayout.addView(newView)
    }

    // Метод, который позволяет переключить фрагмент, и передать ему значение id книги
    override fun onBookClick(id: Int) {
        (activity as MainActivity).setIdBook(id)
        findNavController().navigate(R.id.action_BooksFragment_to_BookInfoFragment)
    }

    // Метод, для сохранения введенного текста
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ENTERED_ID_USER_KEY, viewModelBook.idUser)
    }

    // Метод, вызываемый перед уничтожением представления фрагмента
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}