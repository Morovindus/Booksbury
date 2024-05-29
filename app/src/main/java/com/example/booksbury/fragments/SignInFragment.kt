package com.example.booksbury.fragments

import EncryptionUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.databinding.SignInFragmentBinding
import com.example.booksbury.model.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.UnsupportedEncodingException

// Класс фрагмента входа в систему
class SignInFragment : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: SignInFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // ViewModel для хранения состояния
    private lateinit var viewModel: UserViewModel

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SignInFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация ViewModel
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // Обработчики нажатий на кнопки "Войти"
        binding.buttonLogin.setOnClickListener {
            val username = binding.editUsername.text.toString()
            val password = binding.editPassword.text.toString()

            if (!validateUsernameAndPassword(username, password)!!) {
            } else {
                try {
                    checkUsernameAndPassword(username, password)
                } catch (e: UnsupportedEncodingException) {
                    throw RuntimeException(e)
                }
            }
        }

        // Обработчик нажатия на текст "Зарегистрироваться"
        binding.signUpText.setOnClickListener {
            findNavController().navigate(R.id.action_SignInFragment_to_SignUpFragment)
        }
    }

    // Проверка, всех введенных полей пользователем
    private fun checkUsernameAndPassword(username: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Проверяем существование имени пользователя
            viewModel.checkUsernameExists(username) { usernameExists ->
                if (usernameExists) {
                    // Если имя пользователя существует, проверяем введенный пароль
                    viewModel.checkPasswordValid(username, EncryptionUtil().encrypt(password)) { passwordValid ->
                        if (passwordValid) {
                            // Если пароль подтвержден, получаем идентификатор пользователя
                            viewModel.getId(username) { idUser ->
                                idUser?.let { // Проверяем, что idUser не равен null
                                    // Устанавливаем id пользователя и переходим на главный экран
                                    (activity as MainActivity).setIdUser(idUser.toInt())
                                    findNavController().navigate(R.id.action_SignInFragment_to_HomeFragment)
                                }
                            }
                        } else {
                            // Выводим сообщение об ошибке для поля ввода пароля
                            binding.editPassword.error = getString(R.string.incorrectPassword)
                            binding.editPassword.setText("")
                        }
                    }
                } else {
                    // Выводим сообщение об ошибке для поля ввода имени пользователя и пароля
                    binding.editUsername.error = getString(R.string.incorrectUsername)
                    binding.editUsername.setText("")
                    binding.editPassword.setText("")
                }
            }
        }
    }

    // Проверка введенных полей пользователем
    private fun validateUsernameAndPassword(username: String, password: String): Boolean {

        // Проверка имени пользователя
        val isValidUsername = validateField(username, binding.editUsername, R.string.errorName)

        // Проверка пароля
        val isValidPassword = validateField(password, binding.editPassword, R.string.errorPassword)

        return isValidUsername && isValidPassword
    }

    // Проверка, если значение пустое, устанавливается сообщение об ошибке в EditText
    private fun validateField(value: String, editText: EditText, errorMessageResId: Int): Boolean {
        val isValid = value.isNotEmpty()
        editText.error = if (!isValid) getString(errorMessageResId) else null
        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}