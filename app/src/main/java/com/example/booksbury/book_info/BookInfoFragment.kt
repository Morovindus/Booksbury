package com.example.booksbury.book_info


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.databinding.BookInfoFragmentBinding
import com.example.booksbury.model.BookViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

// Класс фрагмента отображения информации о книге
class BookInfoFragment : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: BookInfoFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // ViewModel для хранения состояния
    private lateinit var viewModel: BookViewModel

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
        viewModel = ViewModelProvider(this).get(BookViewModel::class.java)

        // Восстанавливаем сохраненное значение, если оно есть
        savedInstanceState?.let {
            viewModel.idUser = it.getInt(ENTERED_ID_USER_KEY, viewModel.idUser)
            viewModel.idBook = it.getInt(ENTERED_ID_BOOK_KEY, viewModel.idBook)
        }
    }

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BookInfoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Назначение обработчика нажатия кнопки "Назад"
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Получаем значение id пользователя из предыдущего фрагмента, если оно еще не было установлено
        if (viewModel.idUser == 0) {
            viewModel.idUser = (activity as MainActivity).getIdUser()
        }

        // Получаем значение id книги из предыдущего фрагмента, если оно еще не было установлено
        if (viewModel.idBook == 0) {
            viewModel.idBook = (activity as MainActivity).getIdBook()
        }

        // Отправляем запрос, и инициализируем все поля на экране
        fetchBookData()

        // Настраиваем ViewPager и TabLayout
        setupViewPagerAndTabs()

        // Редактируем экран пользователя в зависимости от некоторых параметров
        handleBookButtons()
    }

    // Метод, для сохранения введенного текста
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ENTERED_ID_BOOK_KEY, viewModel.idBook)
        outState.putInt(ENTERED_ID_USER_KEY, viewModel.idUser)
    }

    // Обрабатываем удаление и добавление кнопок на экране
    private fun handleBookButtons() {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val container = binding.container

        lifecycleScope.launch {
            val isBookPurchased = fetchBookFromPurchasedFromServer()
            val isBookFavorite = fetchBookFromFavoriteFromServer()

            withContext(Dispatchers.Main) {
                if (isBookPurchased) {
                    // Если у пользователя уже есть эта книга
                    inflater.inflate(R.layout.button_read, container, true)
                    setupFavoriteButton(isBookFavorite)
                    setupReadButton()

                } else {
                    // Если у пользователя нет этой книги
                    inflater.inflate(R.layout.button_buy, container, true)
                    setupBuyButton(inflater, container)
                }
            }
        }
    }

    // Добавление кнопок "Читать" и "Слушать аудио"
    private fun setupReadButton(){

        val buttonRead = binding.container.findViewById<AppCompatImageButton>(R.id.button_read)
        val buttonListen = binding.container.findViewById<AppCompatImageButton>(R.id.button_listen)

        // Скачиваем и открываем pdf файл
        buttonRead.setOnClickListener {
            downloadAndOpenPdf()
        }

        // Скачиваем и открываем mp3 файл
        buttonListen.setOnClickListener {
            downloadAndPlayMp3()
        }
    }

    // Метод, загружающий и открывающий mp3 файл
    private fun downloadAndPlayMp3() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mp3Url = fetchMp3Url()
                if (mp3Url.isEmpty()) {
                    Log.d("myLogs", "Ошибка при выборе URL-адреса MP3")
                    return@launch
                }

                val fileName = mp3Url.substring(mp3Url.lastIndexOf('/') + 1)
                val downloadDir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Download_mp3")
                downloadDir.mkdirs()

                val file = File(downloadDir, fileName)
                if (file.exists()) {
                    withContext(Dispatchers.Main) {
                        playMp3(file)
                    }
                } else {
                    downloadMp3(mp3Url, file)
                    withContext(Dispatchers.Main) {
                        playMp3(file)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("myLogs", "Исключение: ${e.message}")
            }
        }
    }

    // Запрос, возвращающий ссылку на mp3 файл
    private suspend fun fetchMp3Url(): String {
        return withContext(Dispatchers.IO) {
            try {
                val ipAddress = (activity as MainActivity).getIpAddress()
                val language = (activity as MainActivity).getLanguage()

                val url = URL("http:$ipAddress:3000/api/books/${viewModel.idBook}/mp3/$language")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val result = inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(result)
                    jsonResponse.getString("audioUrl")
                } else {
                    Log.d("myLogs", "Сервер вернул код ответа $responseCode")
                    ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("myLogs", "Исключение: ${e.message}")
                ""
            }
        }
    }

    // Метод, загружающий и открывающий mp3 файл
    private suspend fun downloadMp3(mp3Url: String, file: File) {
        withContext(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), getString(R.string.download_mp3), Toast.LENGTH_LONG).show()
                }

                val url = URL(mp3Url)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.doInput = true
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream: InputStream = connection.inputStream
                    val outputStream = FileOutputStream(file)

                    val buffer = ByteArray(1024)
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        outputStream.write(buffer, 0, len)
                    }

                    outputStream.close()
                    inputStream.close()
                } else {
                    Log.d("myLogs", "Сервер вернул код ответа $responseCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("myLogs", "Исключение: ${e.message}")
            }
        }
    }

    // Метод, с помощью которого открывается mp3 файл
    private fun playMp3(file: File) {
        val context = requireContext()
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "audio/*") // Установка типа данных для аудиофайла
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            startActivity(intent)
        } else {
            Log.d("myLogs", "Не найдено подходящего приложения для воспроизведения аудио")
        }
    }

    // Метод, загружающий и открывающий pdf файл
    private fun downloadAndOpenPdf() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pdfUrl = fetchPdfUrl()
                if (pdfUrl.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Log.d("myLogs", "Ошибка при выборе URL-адреса PDF-файла")
                    }
                    return@launch
                }

                val fileName = pdfUrl.substring(pdfUrl.lastIndexOf('/') + 1)
                val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                if (file.exists()) {
                    openPdf(file)
                } else {
                    downloadPdf(pdfUrl, file)
                    openPdf(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Log.d("myLogs", "Иключение: ${e.message}")
                }
            }
        }
    }

    // Запрос, возвращающий ссылку на pdf файл
    private suspend fun fetchPdfUrl(): String {
        return withContext(Dispatchers.IO) {
            try {
                val ipAddress = (activity as MainActivity).getIpAddress()
                val language = (activity as MainActivity).getLanguage()

                val url = URL("http:$ipAddress:3000/api/books/${viewModel.idBook}/pdf/$language")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val result = inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(result)
                    jsonResponse.getString("pdfUrl")
                } else {
                    Log.d("myLogs", "Сервер вернул код ответа $responseCode")
                    ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("myLogs", "Исключение: ${e.message}")
                ""
            }
        }
    }

    // Метод, с помощью которого скачивается pdf файл
    private suspend fun downloadPdf(pdfUrl: String, file: File) {
        withContext(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), getString(R.string.download_pdf), Toast.LENGTH_LONG).show()
                }

                val url = URL(pdfUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.doInput = true
                connection.connect()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream: InputStream = connection.inputStream
                    val outputStream = FileOutputStream(file)

                    val buffer = ByteArray(1024)
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        outputStream.write(buffer, 0, len)
                    }

                    outputStream.close()
                    inputStream.close()
                } else {
                    Log.d("myLogs", "Сервер вернул код ответа $responseCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("myLogs", "Исключение: ${e.message}")
            }
        }
    }

    // Метод, с помощью которого открывается pdf файл
    private fun openPdf(file: File) {
        val context = requireContext()
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            startActivity(intent)
        } else {
            Log.d("myLogs", "Программа просмотра PDF-файлов не найдена")
        }
    }

    // Метод настраивающий отображение кнопок "Избранное" и "Удаление из избранного"
    private fun setupFavoriteButton(isBookFavorite: Boolean) {
        if (isBookFavorite) {
            // Если у пользователя книга в избранных
            setupButton(binding.buttonFavourites, false, View.INVISIBLE) {}

            setupButton(binding.buttonDeleteFavorite, true, View.VISIBLE) {
                lifecycleScope.launch {

                    // Посылаем запрос на удаление книги из избранного
                    withContext(Dispatchers.IO) {
                        removeFromFavorite()
                    }
                    Toast.makeText(activity, getString(R.string.book_removed_from_favorites), Toast.LENGTH_LONG).show()

                    setupFavoriteButton(fetchBookFromFavoriteFromServer())
                }
            }
        } else {
            // Если у пользователя данной книги в избранных нет
            setupButton(binding.buttonFavourites, true, View.VISIBLE) {

                lifecycleScope.launch {
                    // Посылаем запрос на добавление книги в избранного
                    withContext(Dispatchers.IO) {
                        addBookToFavorite()
                    }
                    Toast.makeText(activity, getString(R.string.book_added_to_favorites), Toast.LENGTH_LONG).show()

                    setupFavoriteButton(fetchBookFromFavoriteFromServer())
                }
            }

            setupButton(binding.buttonDeleteFavorite, false, View.INVISIBLE) {}
        }
    }

    // Запрос на удаления книги из избранного
    private fun removeFromFavorite() {
        val ipAddress = (context as MainActivity).getIpAddress()

        val url = URL("http:$ipAddress:3000/api/users/${viewModel.idUser}/deleteFavorite/${viewModel.idBook}")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            println("Книга успешно удалена из избранного")
        } else {
            println("Ошибка при удалении книги из избранного: $responseCode")
        }
    }

    // Метод, измененяющий отображение кнопок на экране
    private fun setupButton(button: Button, isEnabled: Boolean, isVisible: Int, clickListener: () -> Unit) {
        button.isEnabled = isEnabled
        button.visibility = isVisible

        button.setOnClickListener {
            clickListener()
        }
    }

    // Метод выводящий всю подробную информацию о книге на экран
    private fun fetchBookData() {
        GlobalScope.launch(Dispatchers.IO) {
            fetchItemsFromServer()
        }
    }

    // Добавление кнопки "Купить", удаление кнопки "Избранное"
    private fun setupBuyButton(inflater: LayoutInflater, container: ViewGroup) {
        setupButton(binding.buttonFavourites, false, View.INVISIBLE) {}

        val buttonBuy = container.findViewById<AppCompatImageButton>(R.id.button_buy_book)

        // Слушатель нажатия на кнопку "Купить"
        buttonBuy.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val isBookInCart = fetchBookFromCartFromServer()

                // Показ уведомления должен быть выполнен на главном потоке
                withContext(Dispatchers.Main) {
                    if (isBookInCart) {
                        Toast.makeText(activity, getString(R.string.book_already_in_cart), Toast.LENGTH_LONG).show()
                    } else {
                        addBookToCart()
                        Toast.makeText(activity, getString(R.string.book_added_to_cart), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Метод инициализируйющий ViewPager и TabLayout
    private fun setupViewPagerAndTabs() {
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        val adapter = ViewPagerAdapter(requireActivity())
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tabs_synopsis)
                1 -> getString(R.string.tabs_details)
                2 -> getString(R.string.tabs_author)
                3 -> getString(R.string.tabs_review)
                else -> ""
            }
        }.attach()
    }

    // Класс, отвечающий за управоление вкладок ViewPager
    private inner class ViewPagerAdapter(activity: FragmentActivity) :
        FragmentStateAdapter(activity) {

        // Возвращает общее количество вкладок
        override fun getItemCount(): Int = 4

        // Создает и возвращает фрагменты для каждой вкладки
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> BookInfoSynopsis()
            1 -> BookInfoDetails()
            2 -> BookInfoAuthor()
            3 -> BookInfoReviews()
            else -> Fragment()
        }
    }

    // Запрос, который возвращает подробную информацию о книге
    private fun fetchItemsFromServer() {
        val ipAddress = (activity as MainActivity).getIpAddress()
        val language = (activity as MainActivity).getLanguage()

        val url = URL("http:$ipAddress:3000/api/books/more/${viewModel.idBook}/$language")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        val jsonResponse = JSONObject(response)

        requireActivity().runOnUiThread {
            binding.titleBook.text = jsonResponse.getJSONObject(language).getString("title")
            binding.nameAuthor.text = jsonResponse.getJSONObject(language).getString("authorName")
            binding.price.text = "${jsonResponse.getInt("price")}\u20BD"
            binding.dateReleased.text = jsonResponse.getInt("released").toString()
            binding.part.text = jsonResponse.getInt("part").toString()
            binding.page.text = jsonResponse.getInt("page").toString()

            val middleCover = jsonResponse.getJSONObject("images").getString("middleCover")
            Picasso.get().load(middleCover).into(binding.mainImage)
        }
    }

    // Запрос, с помощью которого узнаем есть данная книга в избранных у пользователя
    private suspend fun fetchBookFromFavoriteFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val ipAddress = (context as MainActivity).getIpAddress()

            val url = URL("http:$ipAddress:3000/${viewModel.idUser}/favoriteBooks/${viewModel.idBook}")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            val jsonObject = JSONObject(response)
            jsonObject.getBoolean("isBookFavorite")
        }
    }

    // Запрос, с помощью которого узнаем есть данная книга в корзине у пользователя
    private suspend fun fetchBookFromCartFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val ipAddress = (context as MainActivity).getIpAddress()

            val url = URL("http:$ipAddress:3000/${viewModel.idUser}/cart/${viewModel.idBook}")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            val jsonObject = JSONObject(response)
            jsonObject.getBoolean("isBookInCart")
        }
    }

    // Запрос, с помощью которого узнаем есть данная книга в уже купленных книгах у пользователя
    private suspend fun fetchBookFromPurchasedFromServer(): Boolean {
        return withContext(Dispatchers.IO) {
            val ipAddress = (context as MainActivity).getIpAddress()

            val url = URL("http:$ipAddress:3000/${viewModel.idUser}/purchasedBooks/${viewModel.idBook}")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            val jsonObject = JSONObject(response)
            jsonObject.getBoolean("isBookPurchased")
        }
    }

    // Запрос на добавление новой книги в избранное
    private suspend fun addBookToFavorite() {
        withContext(Dispatchers.IO) {
            try {
                val ipAddress = (context as MainActivity).getIpAddress()

                val url = URL("http:$ipAddress:3000/add_favorite/${viewModel.idUser}/${viewModel.idBook}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val responseCode = connection.responseCode
                val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Успешное добавление книги в корзину
                } else {
                    val jsonResponse = JSONObject(responseMessage)
                    val message = jsonResponse.optString("message", "Ошибка при добавлении книги в корзину")
                    Log.d("addBookToFavorite", "Ошибка: $message")
                }
            } catch (e: Exception) {
                // Логируем исключение
                Log.d("addBookToFavorite", "Ошибка при отправке запроса: ${e.message}")
            }
        }
    }

    // Запрос на добавление новой книги в корзину
    private suspend fun addBookToCart() {
        withContext(Dispatchers.IO) {
            try {
                val ipAddress = (context as MainActivity).getIpAddress()

                val url = URL("http:$ipAddress:3000/add_cart/${viewModel.idUser}/${viewModel.idBook}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val responseCode = connection.responseCode
                val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Успешное добавление книги в корзину
                } else {
                    val jsonResponse = JSONObject(responseMessage)
                    val message = jsonResponse.optString("message", "Ошибка при добавлении книги в корзину")
                    // Логируем ошибку
                    Log.d("addBookToCart", "Ошибка: $message")
                }
            } catch (e: Exception) {
                // Логируем исключение
                Log.d("addBookToCart", "Ошибка при отправке запроса: ${e.message}")
            }
        }
    }

    // Метод, который позволяет переключить фрагмент, и передать ему значение id книги
    fun navigateToReviewFragment(id: Int) {
        val bundle = Bundle().apply {
            putInt("id", id)
        }
        findNavController().navigate(R.id.action_BookInfoFragment_to_ReviewFragment, bundle)
    }
}