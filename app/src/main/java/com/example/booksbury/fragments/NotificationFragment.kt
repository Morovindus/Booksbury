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
import com.example.booksbury.adapters.CustomAdapterNotification
import com.example.booksbury.book_info.BookInfoReviews
import com.example.booksbury.databinding.NotificationFragmentBinding
import com.example.booksbury.entity.Notification
import com.example.booksbury.model.BookViewModel
import com.example.booksbury.model.UserViewModel

// Класс фрагмента уведомлений, отображаемого в пользовательском интерфейсе
class NotificationFragment : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: NotificationFragmentBinding? = null

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

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = NotificationFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Установка слушателя для кнопки "Назад"
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Инициализация ViewModel
        viewModelBook = ViewModelProvider(this)[BookViewModel::class.java]
        viewModelUser = ViewModelProvider(this)[UserViewModel::class.java]

        // Получаем значение id пользователя из предыдущего фрагмента, если оно еще не было установлено
        if (viewModelBook.idUser == 0) {
            viewModelBook.idUser = (activity as MainActivity).getIdUser()
        }

        // Восстанавливаем сохраненное значение, если оно есть
        savedInstanceState?.let {
            viewModelBook.idUser = it.getInt(BookInfoReviews.ENTERED_ID_USER_KEY, viewModelBook.idUser)
        }

        // Выполнение запроса на получение уведомлений и обновление пользовательского интерфейса
        fetchBooksAndUpdateUI()
    }

    // Метод, который реализует вывод всех уведомлений на экран
    private fun fetchBooksAndUpdateUI() {
        viewModelUser.getUserNotifications(viewModelBook.idUser) { notifications, error ->
            if (notifications != null) {
                updateUIWithNotifications(notifications)

            } else {
                showNoNotificationsNotification()
                println("Error: $error")
            }
        }
    }

    // Метод для обновления пользовательского интерфейса
    private fun updateUIWithNotifications(notifications: List<Notification>) {
        val adapter = CustomAdapterNotification(notifications, requireContext(),this@NotificationFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
        binding.recyclerView.adapter = adapter
    }

    // Метод, который позволяет переключить фрагмент, и передать ему значение id книги
    fun navigateToBookInfoFragment(id: Int) {
        (activity as MainActivity).setIdBook(id)
        findNavController().navigate(R.id.action_NotificationFragment_to_BookInfoFragment)
    }

    // Метод, который обрабатывает случай - пустого массива уведомлений
    private fun showNoNotificationsNotification() {

        // Получаем родительский ConstraintLayout
        val constraintLayout = binding.ConstraintLayout

        // Создаем новое представление
        val newView = LayoutInflater.from(requireContext()).inflate(R.layout.notification_no_notification, null)

        // Определяем параметры размещения для нового представления
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        // Устанавливаем отношения в параметрах размещения для нового представления
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        params.topToBottom = R.id.topPanel
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID

        // Устанавливаем новые параметры размещения для нового представления
        newView.layoutParams = params

        // Удаляем старое представление RecyclerView из родительского ConstraintLayout
        constraintLayout.removeView(binding.recyclerView)

        // Добавляем новое представление в родительский ConstraintLayout
        constraintLayout.addView(newView)
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