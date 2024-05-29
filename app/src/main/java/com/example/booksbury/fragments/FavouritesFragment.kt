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
import com.example.booksbury.databinding.FavouritesFragmentBinding
import com.example.booksbury.entity.Book
import com.example.booksbury.interfaces.OnBookClickListener
import com.example.booksbury.model.BookViewModel
import com.example.booksbury.model.UserViewModel

// Класс фрагмента избранных книг
class FavouritesFragment : Fragment(), OnBookClickListener {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: FavouritesFragmentBinding? = null

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
            viewModelBook.idUser = it.getInt(BooksFragment.ENTERED_ID_USER_KEY, viewModelBook.idUser)
        }
    }

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FavouritesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем значение id пользователя из предыдущего фрагмента, если оно еще не было установлено
        if (viewModelBook.idUser == 0) {
            viewModelBook.idUser = (activity as MainActivity).getIdUser()
        }

        // Слушатели нажатий всех кнопок на экране
        binding.buttonHome.setOnClickListener { navigateToFragment(R.id.action_FavouritesFragment_to_HomeFragment) }
        binding.buttonExplore.setOnClickListener { navigateToFragment(R.id.action_FavouritesFragment_to_ExploreFragment) }
        binding.buttonProfile.setOnClickListener { navigateToFragment(R.id.action_FavouritesFragment_to_ProfileFragment) }
        binding.buttonSearch.setOnClickListener { navigateToFragment(R.id.action_FavouritesFragment_to_SearchFragment) }
        binding.buttonNotification.setOnClickListener { navigateToFragment(R.id.action_FavouritesFragment_to_NotificaionFragment) }

        // Получаем и обновляем данные о книгах на экране
        fetchBooksAndUpdateUI()
    }

    // Метод, который реализует вывод всех избранных книг на экран
    private fun fetchBooksAndUpdateUI() {

        viewModelUser.getFavoriteBookId(viewModelBook.idUser) { favoriteBooks, error ->
            if (favoriteBooks != null) {
                // Создаем массив для хранения всех купленных книг пользователем
                val purchasedBooks = ArrayList<Book>()
                val totalBooks = favoriteBooks.size
                var completedBooks = 0

                for (bookId in favoriteBooks) {
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
                            updateUIWithFavoriteBooks(purchasedBooks)
                        }
                    }
                }

                // Если books пуст, вызовы getPurchasedBook не произойдут, нужно обновить UI здесь
                if (totalBooks == 0) {
                    updateUIWithFavoriteBooks(purchasedBooks)
                }
            } else {
                showNoFavoriteBooksNotification()
                println("Error: $error")
            }
        }
    }

    // Метод для обновления пользовательского интерфейса
    private fun updateUIWithFavoriteBooks(favoriteBooks: ArrayList<Book>) {
        val adapter = CustomAdapterBooks(favoriteBooks, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
        binding.recyclerView.adapter = adapter
    }

    // Метод, который обрабатывает случай - пустого массива купленных книг
    private fun showNoFavoriteBooksNotification() {

        // Получаем родительский ConstraintLayout
        val constraintLayout = binding.ConstraintLayout

        // Создаем новое представление
        val newView = LayoutInflater.from(requireContext()).inflate(R.layout.notification_no_favorite, null)

        // Определяем параметры размещения для нового представления
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        // Устанавливаем отношения в параметрах размещения для нового представления
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        params.topToBottom = R.id.blackRectangle
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        params.bottomToTop = R.id.cardView

        // Устанавливаем новые параметры размещения для нового представления
        newView.layoutParams = params

        // Удаляем старое представление RecyclerView из родительского ConstraintLayout
        constraintLayout.removeView(binding.recyclerView)

        // Добавляем новое представление в родительский ConstraintLayout
        constraintLayout.addView(newView)
    }

    // Метод переключающий фрагменты
    private fun navigateToFragment(actionId: Int) {
        findNavController().navigate(actionId)
    }

    // Метод, который позволяет переключить фрагмент, и передать ему значение id книги
    override fun onBookClick(id: Int) {
        (activity as MainActivity).setIdBook(id)
        findNavController().navigate(R.id.action_FavouritesFragment_to_BookInfoFragment)
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