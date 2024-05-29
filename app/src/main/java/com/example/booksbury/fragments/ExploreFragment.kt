package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterMarket
import com.example.booksbury.databinding.ExploreFragmentBinding
import com.example.booksbury.entity.Book
import com.example.booksbury.model.BookViewModel

// Класс фрагмента магазина книг
class ExploreFragment : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: ExploreFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // ViewModel для хранения состояния
    private lateinit var viewModelBook: BookViewModel

    // Метод, вызываемый при создании фрагмента
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Инициализация ViewModel
        viewModelBook = ViewModelProvider(this)[BookViewModel::class.java]

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
        _binding = ExploreFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Слушатели нажатий всех кнопок на экране
        binding.buttonHome.setOnClickListener { navigateToFragment(R.id.action_ExploreFragment_to_HomeFragment) }
        binding.buttonFavourites.setOnClickListener { navigateToFragment(R.id.action_ExploreFragment_to_FavouritesFragment) }
        binding.buttonProfile.setOnClickListener { navigateToFragment(R.id.action_ExploreFragment_to_ProfileFragment) }
        binding.buttonSearch.setOnClickListener { navigateToFragment(R.id.action_ExploreFragment_to_SearchFragment) }
        binding.buttonNotification.setOnClickListener { navigateToFragment(R.id.action_ExploreFragment_to_NotificationFragment) }

        // Получаем и обновляем данные о книгах на экране
        fetchBooksAndUpdateUI()
    }

    // Метод, который реализует вывод всех книг на экран
    private fun fetchBooksAndUpdateUI() {

        viewModelBook.getAllBookId() { allBookId, error ->
            if (allBookId != null) {
                // Создаем массив для хранения всех книг
                val purchasedBooks = ArrayList<Book>()
                val totalBooks = allBookId.size
                var completedBooks = 0

                for (bookId in allBookId) {
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
                            updateRecyclerView(purchasedBooks)
                        }
                    }
                }

                // Если books пуст, вызовы getPurchasedBook не произойдут, нужно обновить UI здесь
                if (totalBooks == 0) {
                    updateRecyclerView(purchasedBooks)
                }
            } else {
                println("Error: $error")
            }
        }
    }

    // Метод для обновления пользовательского интерфейса
    private fun updateRecyclerView(items: ArrayList<Book>) {
        val adapter = CustomAdapterMarket(items, requireContext(),this@ExploreFragment)
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 20))
        binding.recyclerView.adapter = adapter
    }

    // Метод, который позволяет переключить фрагмент, и передать ему значение id книги
    fun navigateToBookInfoFragment(id: Int) {
        (activity as MainActivity).setIdBook(id)
        findNavController().navigate(R.id.action_ExploreFragment_to_BookInfoFragment)
    }

    // Метод переключающий фрагменты
    private fun navigateToFragment(actionId: Int) {
        findNavController().navigate(actionId)
    }

    // Метод, для сохранения введенного текста
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(FavouritesFragment.ENTERED_ID_USER_KEY, viewModelBook.idUser)
    }

    // Метод, вызываемый перед уничтожением представления фрагмента
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}