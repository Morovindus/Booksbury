package com.example.booksbury.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterBooks
import com.example.booksbury.databinding.HomeFragmentBinding
import com.example.booksbury.entity.Book
import com.example.booksbury.interfaces.OnBookClickListener
import com.example.booksbury.model.BookViewModel
import com.squareup.picasso.Picasso

// Класс фрагмента главной страницы
class HomeFragment : Fragment(), OnBookClickListener {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: HomeFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // ViewModel для хранения состояния
    private lateinit var viewModel: BookViewModel

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обработчики нажатий на все кнопки на экране
        binding.buttonMore.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_ExploreFragment) }
        binding.buttonFavourites.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_FavouritesFragment) }
        binding.buttonProfile.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_ProfileFragment) }
        binding.mainImage.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_BookInfoFragment) }
        binding.buttonSearch.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_SearchFragment) }
        binding.buttonNotification.setOnClickListener { navigateToFragment(R.id.action_HomeFragment_to_NotificaionFragment) }

        // Инициализация ViewModel
        viewModel = ViewModelProvider(this)[BookViewModel::class.java]

        // Получаем и обновляем данные о книгах на экране
        fetchBooksAndUpdateUI()
    }

    // Метод, который реализует вывод случайных книг на экран
    private fun fetchBooksAndUpdateUI() {

        val language = (activity as MainActivity).getLanguage()

        // Выполняем запрос на получение случайной книги
        viewModel.fetchRandomBook(language, "bigCover") { mainBook, error ->
            if (error != null) {
                // Обработка ошибки
                Log.e("MyLogs", "Failed to fetch random book: $error")
            } else {
                // Обработка успешного ответа
                mainBook?.let { book ->
                    // Обновляем UI с полученной книгой
                    binding.mainTitle.text = book.titleBook
                    Picasso.get().load(book.imageResource).into(binding.mainImage)

                    viewModel.getAuthorImage(mainBook.id) { authorImage, error ->
                        if (authorImage != null) {
                            Picasso.get().load(authorImage).into(binding.imageAuthor)
                        } else {
                            println("Error: $error")
                        }
                    }

                    binding.mainTitle.text = mainBook.titleBook
                    binding.textAuthor.text = mainBook.nameAuthor

                    // Установка слушателя нажатия на основное изображение
                    binding.mainImage.setOnClickListener {
                        onBookClick(mainBook.id)
                    }
                }
            }
        }

        // Получение 10 книг с сервера
        viewModel.getTenBooks(language) { topBooks, error ->
            if (topBooks != null) {
                updateUIWithTopBooks(topBooks)
            } else {
                println("Error: $error")
            }
        }
    }

    // Метод для обновления пользовательского интерфейса
    private fun updateUIWithTopBooks(topBooks: List<Book>) {
        val adapter = CustomAdapterBooks(topBooks, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
        binding.recyclerView.adapter = adapter
    }

    // Метод, который позволяет переключить фрагмент, и передать ему значение id книги
    override fun onBookClick(id: Int) {
        (activity as MainActivity).setIdBook(id)
        findNavController().navigate(R.id.action_HomeFragment_to_BookInfoFragment)
    }

    // Метод переключающий фрагменты
    private fun navigateToFragment(actionId: Int) {
        findNavController().navigate(actionId)
    }

    // Метод, вызываемый перед уничтожением представления фрагмента
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}