package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterNotification
import com.example.booksbury.databinding.NotificationFragmentBinding
import com.example.booksbury.items.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

// Класс фрагмента уведомлений, отображаемого в пользовательском интерфейсе
class NotificationFragment : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: NotificationFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

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

        // Выполнение запроса на получение уведомлений и обновление пользовательского интерфейса
        fetchBooksAndUpdateUI()
    }

    // Метод, который реализует вывод всех уведомлений на экран
    private fun fetchBooksAndUpdateUI() {
        lifecycleScope.launch {
            try {
                // Загрузка всех уведомлений из сети
                val notifications: ArrayList<Notification> = withContext(Dispatchers.IO) {
                    fetchNotificationFromServer()
                }

                updateUIWithNotifications(notifications)
            } catch (e: Exception) {
                showNoNotificationsNotification()
            }
        }
    }

    // Метод для обновления пользовательского интерфейса
    private fun updateUIWithNotifications(notifications: ArrayList<Notification>) {
        notifications.reverse()
        val adapter = CustomAdapterNotification(notifications, requireContext(),this@NotificationFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
        binding.recyclerView.adapter = adapter
    }

    // Метод, который позволяет переключить фрагмент, и передать ему значение id книги
    fun navigateToBookInfoFragment(id: Int) {
        val bundle = Bundle().apply {
            putInt("id", id)
        }
        findNavController().navigate(R.id.action_NotificationFragment_to_BookInfoFragment, bundle)
    }

    // Запрос на получение всех уведомлений
    private fun fetchNotificationFromServer(): ArrayList<Notification> {
        val ipAddress = (activity as MainActivity).getIpAddress()
        val userId = (context as MainActivity).getIdUser()

        val url = URL("http:$ipAddress:3000/api/users/$userId/notifications")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        val jsonResponse = JSONArray(response)

        val items = ArrayList<Notification>()
        for (i in 0 until jsonResponse.length()) {
            val bookObject = jsonResponse.getJSONObject(i)
            val bookId = bookObject.getInt("bookId")
            val time = bookObject.getString("time")
            val image = bookObject.getString("image")

            val item = Notification(bookId, time, image)
            items.add(item)
        }
        return items
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}