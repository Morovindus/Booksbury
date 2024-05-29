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
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.databinding.BookInfoFragmentBinding
import com.example.booksbury.model.BookViewModel
import com.example.booksbury.model.UserViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
            viewModelBook.idUser = it.getInt(ENTERED_ID_USER_KEY, viewModelBook.idUser)
            viewModelBook.idBook = it.getInt(ENTERED_ID_BOOK_KEY, viewModelBook.idBook)
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
        if (viewModelBook.idUser == 0) {
            viewModelBook.idUser = (activity as MainActivity).getIdUser()
        }

        // Получаем значение id книги из предыдущего фрагмента, если оно еще не было установлено
        if (viewModelBook.idBook == 0) {
            viewModelBook.idBook = (activity as MainActivity).getIdBook()
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
        outState.putInt(ENTERED_ID_BOOK_KEY, viewModelBook.idBook)
        outState.putInt(ENTERED_ID_USER_KEY, viewModelBook.idUser)
    }

    // Обрабатываем удаление и добавление кнопок на экране
    private fun handleBookButtons() {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val container = binding.container

        viewModelUser.isBookPurchased(viewModelBook.idUser, viewModelBook.idBook) { isBookPurchased, error ->
            if (isBookPurchased != null) {
                if (isBookPurchased) {
                    // Если у пользователя уже есть эта книга

                    viewModelUser.isBookFavorite(viewModelBook.idUser, viewModelBook.idBook) { isBookFavorite, error ->
                        if (isBookFavorite != null) {
                            setupFavoriteButton(isBookFavorite)
                        } else {
                            println("Error: $error")
                        }
                    }

                    inflater.inflate(R.layout.button_read, container, true)
                    setupReadButton()

                } else {
                    // Если у пользователя нет этой книги
                    inflater.inflate(R.layout.button_buy, container, true)
                    setupBuyButton(inflater, container)
                }
            } else {
                println("Error: $error")
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

                val url = URL("http:$ipAddress:3000/api/books/${viewModelBook.idBook}/mp3/$language")
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

                val url = URL("http:$ipAddress:3000/api/books/${viewModelBook.idBook}/pdf/$language")
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

                // Посылаем запрос на удаление книги из избранного
                viewModelUser.deleteFavoriteBook(viewModelBook.idUser, viewModelBook.idBook) { success, error ->
                    if (success) {
                        Toast.makeText(activity, getString(R.string.book_removed_from_favorites), Toast.LENGTH_LONG).show()

                        viewModelUser.isBookFavorite(viewModelBook.idUser, viewModelBook.idBook) { isBookFavorite, error ->
                            if (isBookFavorite != null) {
                                setupFavoriteButton(isBookFavorite)
                            } else {
                                ("Error: $error")
                            }
                        }

                    } else {
                        println("Ошибка: $error")
                    }
                }
            }
        } else {
            // Если у пользователя данной книги в избранных нет
            setupButton(binding.buttonFavourites, true, View.VISIBLE) {

                // Посылаем запрос на добавление книги в избранного
                viewModelUser.addBookToFavorite(viewModelBook.idUser, viewModelBook.idBook) { success, message ->
                    if (success) {
                        Toast.makeText(activity, getString(R.string.book_added_to_favorites), Toast.LENGTH_LONG).show()

                        viewModelUser.isBookFavorite(viewModelBook.idUser, viewModelBook.idBook) { isBookFavorite, error ->
                            if (isBookFavorite != null) {
                                setupFavoriteButton(isBookFavorite)
                            } else {
                                println("Error: $error")
                            }
                        }
                    } else {
                        println("Ошибка при добавлении книги в избранное: $message")
                    }
                }
            }

            setupButton(binding.buttonDeleteFavorite, false, View.INVISIBLE) {}
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
        val language = (activity as MainActivity).getLanguage()

        viewModelBook.getBookMoreDetails(viewModelBook.idBook, language) { bookDetails, error ->
            if (bookDetails != null) {
                binding.titleBook.text = bookDetails.title
                binding.nameAuthor.text = bookDetails.authorName
                binding.price.text = "${bookDetails.price.toInt()}\u20BD"
                binding.dateReleased.text = bookDetails.released
                binding.part.text = bookDetails.part
                binding.page.text = bookDetails.page

                Picasso.get().load(bookDetails.images).into(binding.mainImage)
            } else {
                println("Ошибка: $error")
            }
        }
    }

    // Добавление кнопки "Купить", удаление кнопки "Избранное"
    private fun setupBuyButton(inflater: LayoutInflater, container: ViewGroup) {
        setupButton(binding.buttonFavourites, false, View.INVISIBLE) {}

        val buttonBuy = container.findViewById<AppCompatImageButton>(R.id.button_buy_book)

        // Слушатель нажатия на кнопку "Купить"
        buttonBuy.setOnClickListener {

            viewModelUser.isBookInCart(viewModelBook.idUser, viewModelBook.idBook) { isBookInCart, error ->
                if (isBookInCart != null) {
                    if (isBookInCart) {
                        Toast.makeText(activity, getString(R.string.book_already_in_cart), Toast.LENGTH_LONG).show()
                    } else {
                        // Добавление книги в корзину
                        viewModelUser.addBookToCart(viewModelBook.idUser, viewModelBook.idBook) { success, message ->
                            if (success) {
                                Toast.makeText(activity, getString(R.string.book_added_to_cart), Toast.LENGTH_LONG).show()
                            } else {
                                println("Ошибка при добавлении книги в корзину: $message")
                            }
                        }
                    }
                } else {
                    println("Ошибка: $error")
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
}