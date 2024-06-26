package com.example.booksbury.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.databinding.ProfileFragmentBinding
import com.example.booksbury.model.BookViewModel
import com.example.booksbury.model.UserViewModel

// Класс фрагмента профиля пользователя
class ProfileFragment : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: ProfileFragmentBinding? = null

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

    // Метод, вызываемый при создании фрагмента
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Инициализация ViewModel
        viewModelBook = ViewModelProvider(this)[BookViewModel::class.java]
        viewModelUser = ViewModelProvider(this)[UserViewModel::class.java]

        // Восстанавливаем сохраненное значение, если оно есть
        savedInstanceState?.let {
            viewModelBook.idUser = it.getInt(ENTERED_ID_USER_KEY, viewModelBook.idUser)
        }
    }

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

        // Получаем значение id пользователя из предыдущего фрагмента, если оно еще не было установлено
        if (viewModelBook.idUser == 0) {
            viewModelBook.idUser = (activity as MainActivity).getIdUser()
        }

        // Обработчики нажатий на все кнопки на экране
        binding.buttonHome.setOnClickListener { navigateToFragment(R.id.action_ProfileFragment_to_HomeFragment) }
        binding.buttonExplore.setOnClickListener { navigateToFragment(R.id.action_ProfileFragment_to_ExploreFragment) }
        binding.buttonFavourites.setOnClickListener { navigateToFragment(R.id.action_ProfileFragment_to_FavouritesFragment) }
        binding.buttonSearch.setOnClickListener { navigateToFragment(R.id.action_ProfileFragment_to_SearchFragment) }
        binding.buttonNotification.setOnClickListener { navigateToFragment(R.id.action_ProfileFragment_to_NotificaionFragment) }
        binding.buttonCart.setOnClickListener { navigateToFragment(R.id.action_ProfileFragment_to_CartFragment) }
        binding.buttonBooks.setOnClickListener { navigateToFragment(R.id.action_ProfileFragment_to_BooksFragment) }
        binding.buttonExit.setOnClickListener { showExitConfirmationDialog() }
        binding.buttonAboutApp.setOnClickListener { navigateToFragment(R.id.action_ProfileFragment_to_AboutAppFragment) }

        binding.buttonDesign.setOnClickListener {
            showThemeDialog()
        }

        // Получаем и обновляем данные о имени и электронной почте на экране
        fetchNameAndEmailUI()
    }

    // Диалог для смены темы
    private fun showThemeDialog() {
        val themes = arrayOf(getString(R.string.light_theme), getString(R.string.dark_theme), getString(R.string.system_theme))
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.chose_theme))
        builder.setItems(themes) { _, which ->
            when (which) {
                0 -> setAppTheme(AppCompatDelegate.MODE_NIGHT_NO)
                1 -> setAppTheme(AppCompatDelegate.MODE_NIGHT_YES)
                2 -> setAppTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
        builder.create().show()
    }

    // Метод, который меняет тему приложения
    private fun setAppTheme(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
        // Перезагрузить активность, чтобы применить новую тему
        activity?.recreate()
    }

    // Метод, который реализует вывод на экран имя и почту пользователя
    private fun fetchNameAndEmailUI() {
            // Получение имени пользователя и почты
        viewModelUser.getUserDetails(viewModelBook.idUser) { userInfo, error ->
            if (userInfo != null) {
                // Обновляем пользовательский интерфейс в главном потоке
                binding.nameUser.text = userInfo.username
                binding.emailUser.text = userInfo.email
            }else {
                println("Error: $error")
            }
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

    // Метод переключающий фрагменты
    private fun navigateToFragment(actionId: Int) {
        findNavController().navigate(actionId)
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