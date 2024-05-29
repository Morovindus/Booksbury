package com.example.booksbury.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterBooks
import com.example.booksbury.databinding.SearchFragmentBinding
import com.example.booksbury.interfaces.OnBookClickListener
import com.example.booksbury.model.BookViewModel

// Класс фрагмента поиска книг
class SearchFragment : Fragment(), OnBookClickListener {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: SearchFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // Адаптер для RecyclerView
    private lateinit var adapter: CustomAdapterBooks

    // ViewModel для хранения состояния
    private lateinit var viewModel: BookViewModel

    // Ключ для сохранения и восстановления идентификатора пользователя
    companion object {
        const val ENTERED_ID_USER_KEY = "savedIdUser"
        // Имя файла настроек для сохранения истории поиска
        private const val PREFS_NAME = "search_prefs"
        // Ключ для сохранения и восстановления истории поиска
        private const val KEY_HISTORY = "search_history"
        // Максимальный размер истории поиска
        private const val MAX_HISTORY_SIZE = 10
        // Задержка перед выполнением поиска (в миллисекундах)
        private const val DELAY_MILLIS = 2000L
    }

    // Адаптер для истории поиска
    private lateinit var historyAdapter: ArrayAdapter<String>

    // Список истории поиска
    private lateinit var searchHistoryList: ArrayList<String>

    // Объект для работы с SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences

    // Обработчик для выполнения отложенного поиска
    private val handler = Handler(Looper.getMainLooper())

    // Runnable для выполнения поиска с задержкой
    private val searchRunnable = Runnable { performSearch() }

    // Метод, вызываемый при создании фрагмента
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Инициализация ViewModel
        viewModel = ViewModelProvider(this)[BookViewModel::class.java]

        // Восстанавливаем сохраненное значение, если оно есть
        savedInstanceState?.let {
            viewModel.idUser = it.getInt(ENTERED_ID_USER_KEY, viewModel.idUser)
        }
    }

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SearchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Инициализация истории поиска
        searchHistoryList = ArrayList(getSearchHistory())
        historyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, searchHistoryList)
        binding.searchHistoryListView.adapter = historyAdapter

        // Установка обработчиков на элементы
        setupUI()

        // Инициализация RecyclerView
        updateUIWithFavoriteBooks()

        // Установка наблюдателей
        setupObservers()
    }

    // Метод для получения истории поиска
    private fun getSearchHistory(): Set<String> {
        return sharedPreferences.getStringSet(KEY_HISTORY, emptySet()) ?: emptySet()
    }

    // Метод для добавления поисковых запросов в историю поиска
    private fun addSearchQueryToHistory(query: String) {
        val historySet = getSearchHistory().toMutableSet()

        // Удаляем старую запись, если она есть
        if (historySet.contains(query)) {
            searchHistoryList.remove(query)
        } else {
            // Ограничиваем размер истории
            if (historySet.size >= MAX_HISTORY_SIZE) {
                val iterator = historySet.iterator()
                if (iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                }
            }
        }

        // Добавляем новую запись
        historySet.add(query)
        searchHistoryList.add(0, query)

        historyAdapter.notifyDataSetChanged()
        saveSearchHistory(historySet)
    }

    // Метод для сохранения истории поиска
    private fun saveSearchHistory(historySet: Set<String>) {
        sharedPreferences.edit().putStringSet(KEY_HISTORY, historySet).apply()
    }

    // Метод очистки истории поиска
    private fun clearSearchHistory() {
        sharedPreferences.edit().remove(KEY_HISTORY).apply()
    }

    // Метод для установки обработчиков на элементы
    private fun setupUI() {
        // Установка слушателя на поле для ввода поиска
        binding.searchBar.setOnClickListener {
            if (searchHistoryList.isNotEmpty()) {
                binding.searchHistoryListView.visibility = View.VISIBLE
                binding.buttonClearHistory.visibility = View.VISIBLE
            }
        }

        // Установка слушателя на список истории поиска
        binding.searchHistoryListView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = searchHistoryList[position]
            addSearchQueryToHistory(selectedItem)
            binding.searchBar.setText(selectedItem)
            binding.searchHistoryListView.visibility = View.GONE
            binding.buttonClearHistory.visibility = View.GONE
        }

        // Установка слушателя на кнопку "Очистить" историю поиска
        binding.buttonClearHistory.setOnClickListener {
            clearSearchHistory()
            searchHistoryList.clear()
            historyAdapter.notifyDataSetChanged()
            binding.searchHistoryListView.visibility = View.GONE
            binding.buttonClearHistory.visibility = View.GONE
            Toast.makeText(requireContext(), getString(R.string.clear_hostory_notification), Toast.LENGTH_SHORT).show()
        }

        // Установка слушателя на кнопку "Назад"
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Установка слушателя на кнопку очистить поисковый запрос
        binding.buttonCancel.setOnClickListener {
            binding.searchBar.text.clear()
            hideKeyboard(binding.searchBar)
        }

        // Настраиваем поля для ввода поискового запроса
        setupSearchBar()
    }

    // Метод, для настройки поля для ввода поискового запроса
    private fun setupSearchBar() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    binding.buttonCancel.visibility = Button.INVISIBLE
                    binding.buttonCancel.isEnabled = false
                } else {
                    binding.buttonCancel.visibility = Button.VISIBLE
                    binding.buttonCancel.isEnabled = true

                    handler.postDelayed(searchRunnable, DELAY_MILLIS)
                }

                // Удаляем все отложенные вызовы searchRunnable
                handler.removeCallbacks(searchRunnable)
                binding.progressBar.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {
                // Ставим новый отложенный вызов searchRunnable с задержкой в 2 секунды
                if (!TextUtils.isEmpty(s)) {
                    handler.postDelayed(searchRunnable, DELAY_MILLIS)
                }
                binding.progressBar.visibility = View.VISIBLE
            }
        })


        // Установка слушателя на кнопку отправить поисковый запрос
        binding.buttonSearch.setOnClickListener {
            performSearch()
        }
    }

    // Метод для установки наблюдателей
    private fun setupObservers() {
        viewModel.books.observe(viewLifecycleOwner, Observer { books ->
            if (books.isEmpty()) {
                setVisibility(true)
                binding.searchHistoryListView.visibility = View.GONE
                binding.buttonClearHistory.visibility = View.GONE
            } else {
                setVisibility(false)
                adapter.updateBooks(books)
            }
            binding.progressBar.visibility = View.GONE
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Log.e("myLogs", "Error: $it")
            }
            binding.progressBar.visibility = View.GONE
        })
    }

    // Метод для отправки поискового запроса
    private fun performSearch() {
        val query = binding.searchBar.text.toString()
        val language = (activity as MainActivity).getLanguage()

        if (!TextUtils.isEmpty(query)) {
            binding.progressBar.visibility = View.VISIBLE
            viewModel.searchBooks(query, language)
            addSearchQueryToHistory(query)
            binding.searchBar.setText("")
            binding.searchHistoryListView.visibility = View.GONE
            binding.buttonClearHistory.visibility = View.GONE
        } else {
            binding.searchBar.error = getString(R.string.QueryError)
        }
    }

    // Метод, который показывает уведомление
    private fun setVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.INVISIBLE

        // Находим ImageView и устанавливаем видимость
        binding.emptySearch.visibility = visibility

        // Находим TextView для "Пусто" и устанавливаем видимость
        binding.textEmpty.visibility = visibility

        // Находим TextView для "Нет результатов" и устанавливаем видимость
        binding.textNoResults.visibility = visibility

        // Находим RecyclerView и устанавливаем видимость
        binding.recyclerView.visibility = if (isVisible) View.INVISIBLE else View.VISIBLE
    }

    // Метод для обновления пользовательского интерфейса
    private fun updateUIWithFavoriteBooks() {
        adapter = CustomAdapterBooks(ArrayList(), this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
        binding.recyclerView.adapter = adapter
    }

    // Метод для скрытия клавиатуры
    private fun hideKeyboard(editText: EditText) {
        val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    // Метод, для сохранения введенного текста
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ENTERED_ID_USER_KEY, viewModel.idUser)
    }

    // Метод, который позволяет переключить фрагмент, и передать ему значение id книги
    override fun onBookClick(id: Int) {
        (activity as MainActivity).setIdBook(id)
        findNavController().navigate(R.id.action_SearchFragment_to_BookInfoFragment)
    }

    // Метод, вызываемый перед уничтожением представления фрагмента
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}