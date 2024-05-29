package com.example.booksbury.book_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.booksbury.MainActivity
import com.example.booksbury.databinding.BookInfoSynopsisBinding
import com.example.booksbury.model.BookViewModel


// Класс фрагмента отображения описания книги
class BookInfoSynopsis : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: BookInfoSynopsisBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // ViewModel для хранения состояния
    private lateinit var viewModel: BookViewModel

    // Блок companion object для хранения констант
    companion object {
        // Ключ для сохранения и восстановления идентификатора книги
        const val ENTERED_ID_BOOK_KEY = "savedIdBook"
    }

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BookInfoSynopsisBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация ViewModel
        viewModel = ViewModelProvider(this).get(BookViewModel::class.java)

        // Получаем значение id книги из предыдущего фрагмента, если оно еще не было установлено
        if (viewModel.idBook == 0) {
            viewModel.idBook = (activity as MainActivity).getIdBook()
        }

        // Восстанавливаем сохраненное значение, если оно есть
        savedInstanceState?.let {
            viewModel.idBook = it.getInt(ENTERED_ID_BOOK_KEY, viewModel.idBook)
        }

        // Выполнение запроса на получение описания книги и обновление пользовательского интерфейса
        fetchDataFromServer()
    }

    // Метод, выводящий на экран описание книги
    private fun fetchDataFromServer() {
        val language = (activity as MainActivity).getLanguage()
        viewModel.getBookSynopsis(viewModel.idBook, language) { bookSynopsis, error ->
            if (bookSynopsis != null) {
                binding.textSynopsis.text = bookSynopsis
            } else {
                println("Error: $error")
            }
        }
    }

    // Метод, для сохранения введенного текста
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ENTERED_ID_BOOK_KEY, viewModel.idBook)
    }

    // Метод, вызываемый перед уничтожением представления фрагмента
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}