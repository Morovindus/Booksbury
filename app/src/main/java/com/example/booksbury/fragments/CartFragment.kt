package com.example.booksbury.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterCart
import com.example.booksbury.databinding.CartFragmentBinding
import com.example.booksbury.entity.Book
import com.example.booksbury.entity.Notification
import com.example.booksbury.model.BookViewModel
import com.example.booksbury.model.UserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Класс фрагмента корзины
class CartFragment : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: CartFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // Список книг в корзине
    private var cartBooksNew = ArrayList<Book>()

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
        _binding = CartFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем значение id пользователя из предыдущего фрагмента, если оно еще не было установлено
        if (viewModelBook.idUser == 0) {
            viewModelBook.idUser = (activity as MainActivity).getIdUser()
        }

        // Выводим список вех книг в корзине наэкран
        fetchBooksAndUpdateUI()

        // Слушатель кнпоки "Назад"
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Слушатель кнпоки "Оформить заказ"
        binding.buttonOrder.setOnClickListener {
            showOrderConfirmationDialog(cartBooksNew)
        }
    }

    // Диалог, всплывающий, когда пользователь хочет оформить заказ
    private fun showOrderConfirmationDialog(cartBook: ArrayList<Book>) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.order_confirmation_title))
            .setMessage(getString(R.string.order_confirmation_message))
            .setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
                handleOrderConfirmation(cartBook)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    // Метод для обработки подтверждения заказа
    private fun handleOrderConfirmation(cartBooks: ArrayList<Book>) {

        for (book in cartBooks) {

            // Добавление нового уведомления
            viewModelUser.addNotification(viewModelBook.idUser, Notification(book.id, getCurrentDateTimeFormatted(), book.imageResource) ) { success, error ->
                // Если добавление успешно
                if (success) {
                    println("Уведомление успешно добавлено")
                    // Посылаем запрос на добавление книги в избранного
                    viewModelUser.addBookToPurchased(viewModelBook.idUser, book.id) { success, message ->
                        if (success) {
                            println("Книга добавлена в купленное")
                            // Посылаем запрос на удаление книги из избранного
                            viewModelUser.deleteCartBook(viewModelBook.idUser, book.id) { success, error ->
                                if (success) {
                                    println("Книга успешно удалена из корзины пользователя")
                                } else {
                                    println("Ошибка: $error")
                                }
                            }
                        } else {
                            println("Ошибка при добавлении книги в купленное: $message")
                        }
                    }
                } else {
                    // Если произошла ошибка при добавлении
                    println("Ошибка при добавлении уведомления: $error")
                }
            }
        }

        // Выводим увеломление о успешной покупке
        Toast.makeText(activity, getString(R.string.order_confirmation_success), Toast.LENGTH_LONG).show()

        // Переключаем экран пользователя на главную страницу
        findNavController().navigate(R.id.action_CartFragment_to_HomeFragment)
    }

    // Метод, для получения текущей даты и времени
    private fun getCurrentDateTimeFormatted(): String {
        val dateFormat = SimpleDateFormat("HH:mm dd MMM yyyy", Locale.ENGLISH)
        val date = Date()
        return dateFormat.format(date)
    }

    // Метод, который реализует вывод всех книг в корзине на экран
    private fun fetchBooksAndUpdateUI() {

        viewModelUser.getBookInCart(viewModelBook.idUser) { cartBookId, error ->
            if (cartBookId != null) {
                // Создаем массив для хранения всех купленных книг пользователем
                val purchasedBooks = ArrayList<Book>()
                var totalPrice = 0
                val totalBooks = cartBookId.size
                var completedBooks = 0

                for (bookId in cartBookId) {
                    val language = (activity as MainActivity).getLanguage()
                    viewModelBook.getPurchasedBook(bookId, language) { purchasedBook, error ->
                        if (purchasedBook != null) {
                            purchasedBooks.add(purchasedBook)
                            totalPrice += purchasedBook.price
                        } else {
                            println("Ошибка: $error")
                        }

                        // Увеличиваем счетчик завершенных вызовов
                        completedBooks++

                        // Проверяем, завершены ли все вызовы
                        if (completedBooks == totalBooks) {
                            // Обновляем пользовательский интерфейс
                            updateUIWithCartBooks(purchasedBooks, totalPrice)
                            cartBooksNew = purchasedBooks
                        }
                    }
                }

                // Если books пуст, вызовы getPurchasedBook не произойдут, нужно обновить UI здесь
                if (totalBooks == 0) {
                    updateUIWithCartBooks(purchasedBooks, totalPrice)
                    cartBooksNew = purchasedBooks
                }
            } else {
                showNoCartBooksNotification()
                println("Error: $error")
            }
        }
    }

    // Метод для обновления пользовательского интерфейса
    private fun updateUIWithCartBooks(cartBook: ArrayList<Book>, totalPrice: Int) {
        recalculationSum(totalPrice)
        val adapter = CustomAdapterCart(cartBook, this@CartFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
        binding.recyclerView.adapter = adapter
    }

    // Метод, который обрабатывает случай - пустого массива купленных книг
    private fun showNoCartBooksNotification() {
        // Получаем родительский ConstraintLayout
        val constraintLayout = binding.ConstraintLayout

        // Создаем новое представление
        val newView = LayoutInflater.from(requireContext()).inflate(R.layout.notification_no_cart_book, null)

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

        // Удаляем старые представления из родительского ConstraintLayout
        constraintLayout.removeView(binding.recyclerView)
        constraintLayout.removeView(binding.cardView)

        // Добавляем новое представление в родительский ConstraintLayout
        constraintLayout.addView(newView)
    }

    // Пересчет общей суммы всех книг в корзине
    fun recalculationSum(totalPrice: Int){
        binding.totalPrice.text = "$totalPrice\u20BD"
    }

    // Возврат значения общей суммы
    fun getTotalSum(): Int {
        val totalPriceRubles = binding.totalPrice.text.toString()
        return totalPriceRubles.substringBefore('\u20BD').toIntOrNull() ?: 0
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