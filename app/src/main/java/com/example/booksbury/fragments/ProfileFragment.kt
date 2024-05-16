package com.example.booksbury.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.databinding.ProfileFragmentBinding
import com.example.booksbury.items.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Класс фрагмента профиля пользователя
class ProfileFragment : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: ProfileFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обработчики нажатий на все кнопки на экране
        binding.buttonHome.setOnClickListener { navigateToFragment(R.id.action_ProfileFragment_to_HomeFragment) }
        binding.buttonExplore.setOnClickListener { navigateToFragment(R.id.action_ProfileFragment_to_ExploreFragment) }
        binding.buttonFavourites.setOnClickListener { navigateToFragment(R.id.action_ProfileFragment_to_FavouritesFragment) }
        binding.buttonSearch.setOnClickListener { navigateToFragment(R.id.action_ProfileFragment_to_SearchFragment) }
        binding.buttonNotification.setOnClickListener { navigateToFragment(R.id.action_ProfileFragment_to_NotificaionFragment) }
        binding.buttonCart.setOnClickListener { navigateToFragment(R.id.action_ProfileFragment_to_CartFragment) }
        binding.buttonBooks.setOnClickListener { navigateToFragment(R.id.action_ProfileFragment_to_BooksFragment) }
        binding.buttonExit.setOnClickListener { showExitConfirmationDialog() }

        // Получаем и обновляем данные о имени и электронной почте на экране
        fetchNameAndEmailUI()
    }

    // Метод, который реализует вывод на экран имя и почту пользователя
    private fun fetchNameAndEmailUI() {
        lifecycleScope.launch {
            val user: User = withContext(Dispatchers.IO) {
                fetchNameEmailDataFromServer()
            }

            // Обновляем пользовательский интерфейс в главном потоке
            binding.nameUser.text = user.userName
            binding.emailUser.text = user.email
        }
    }

    // Диалог, всплывающий, когда пользователь хочет выйти из приложения
    private fun showExitConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.exit_title))
            .setMessage(getString(R.string.exit_message))
            .setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
                activity?.finish()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    // Запрос возвращающий имя и почту пользователя
    private suspend fun fetchNameEmailDataFromServer(): User {
        return withContext(Dispatchers.IO) {
            val ipAddress = (activity as MainActivity).getIpAddress()
            val idUser = (activity as MainActivity).getIdUser()

            val url = URL("http://$ipAddress:3000/api/user/$idUser")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            val jsonResponse = JSONObject(response)

            val userName = jsonResponse.getString("username")
            val email = jsonResponse.getString("email")

            User(userName, email)
        }
    }

    // Метод переключающий фрагменты
    private fun navigateToFragment(actionId: Int) {
        findNavController().navigate(actionId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}