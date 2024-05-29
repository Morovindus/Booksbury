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
import com.example.booksbury.R
import com.example.booksbury.databinding.SignUpFragmentBinding
import com.example.booksbury.dialog.MyDialogFragmentRegistration
import com.example.booksbury.model.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.UnsupportedEncodingException
import java.util.Random

// Класс фрагмента регистрации нового пользователя
class SignUpFragment : Fragment() {

    // Приватное свойство для хранения привязки к макету фрагмента
    private var _binding: SignUpFragmentBinding? = null

    // Приватное свойство, предоставляющее доступ к привязке к макету фрагмента
    private val binding get() = _binding!!

    // ViewModel для хранения состояния
    private lateinit var viewModel: UserViewModel

    // Метод, вызываемый при создании макета фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SignUpFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Метод, вызываемый после создания макета фрагмента
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация ViewModel
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // Обработчики нажатий на кнопку "Регистрация"
        binding.buttonRegistration.setOnClickListener {
            val username = binding.editName.text.toString()
            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()
            val passwordConfirm = binding.editPasswordConfirm.text.toString()

            // Проверка полей ввода
            if (validateUserFields(username, email, password, passwordConfirm)) {
                // Проверка уникальности имени пользователя и соответствия паролей
                try {
                    checkUsernameAndPassword(username, email, password, passwordConfirm)
                } catch (e: UnsupportedEncodingException) {
                    throw RuntimeException(e)
                }
            }
        }

        // Обработчик нажатия на текст "Войти"
        binding.signInText.setOnClickListener {
            findNavController().navigate(R.id.action_SignUpFragment_to_SignInFragment)
        }
    }

    // Проверка всех данных, введенных пользователем
    private fun checkUsernameAndPassword(username: String, email: String, password: String, passwordConfirm: String) {
        // Запускаем корутину на диспетчере ввода-вывода для выполнения сетевых операций
        lifecycleScope.launch(Dispatchers.IO) {

            // Проверка, существует ли пользователь с таким именем
            viewModel.checkUsernameExists(username) { usernameExists ->

                // Если имя пользователя не существует, продолжаем
                if (!usernameExists) {
                    // Проверка, совпадают ли пароли и удовлетворяют ли они требованиям
                    if (validatePassword(password, passwordConfirm)) {

                        // Получение всех ID пользователей для генерации уникального ID
                        viewModel.getAllUserId { allIdUser ->
                            // Генерация уникального случайного ID для нового пользователя
                            val idUser = generateUniqueRandomId(allIdUser)

                            // Добавление нового пользователя в базу данных
                            viewModel.addNewUser(idUser, username, email, EncryptionUtil().encrypt(password)) { success, error ->
                                // Если добавление успешно
                                if (success) {
                                    println("Пользователь успешно добавлен")
                                } else {
                                    // Если произошла ошибка при добавлении
                                    println("Ошибка при добавлении пользователя: $error")
                                }
                            }
                        }

                        // Показ диалогового окна после успешной регистрации
                        val dialogFragment = MyDialogFragmentRegistration(this@SignUpFragment)
                        dialogFragment.show(parentFragmentManager, "dialog")
                    }
                } else {
                    // Если имя пользователя уже существует, показываем ошибку
                    binding.editName.error = getString(R.string.userExistsError)
                }
            }
        }
    }

    // Генерация уникального случайного id пользователя
    private fun generateUniqueRandomId(existingIds: List<Int>): Int {
        val random = Random()
        var generatedId: Int
        do {
            generatedId = 100000 + random.nextInt(900000)
        } while (generatedId in existingIds)
        return generatedId
    }

    // Проверка введенных полей пользователем
    private fun validateUserFields(username: String, email: String, password: String, passwordConfirm: String): Boolean {
        var isValid = true

        // Проверка имени пользователя
        isValid = isValid and validateField(username, binding.editName, R.string.errorName)

        // Проверка адреса электронной почты
        isValid = isValid and validateField(email, binding.editEmail, R.string.errorEmail)

        // Проверка пароля
        isValid = isValid and validateField(password, binding.editPassword, R.string.errorPassword)

        // Проверка подтверждения пароля
        isValid = isValid and validateField(passwordConfirm, binding.editPasswordConfirm, R.string.errorPasswordConfirm)

        return isValid
    }

    // Проверка, если значение пустое, устанавливается сообщение об ошибке в EditText
    private fun validateField(value: String, editText: EditText, errorMessageResId: Int): Boolean {
        return if (value.isEmpty()) {
            editText.error = getString(errorMessageResId)
            false
        } else {
            editText.error = null
            true
        }
    }

    // Проверка, что поля пароля и подтверждения пароля совпадают
    private fun validatePassword(password: String, confirmPassword: String): Boolean {
        return if (password == confirmPassword) {
            true
        } else {
            binding.editPasswordConfirm.error = getString(R.string.errorConfirmPassword)
            false
        }
    }

    // Метод, возвращающий пользователя на начальный экран
    fun navigateToSignInFragment() {
        findNavController().navigate(R.id.action_SignUpFragment_to_SignInFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}