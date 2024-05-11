package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.adapters.MyDialogFragment
import com.example.booksbury.databinding.SignUpFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random

class SignUpFragment : Fragment() {

    private var _binding: SignUpFragmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = SignUpFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRegistration.setOnClickListener {

            val username = binding.editName.text.toString()
            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()
            val passwordConfirm = binding.editPasswordConfirm.text.toString()

            if (!validateUserFields(username, email, password, passwordConfirm)!!) {
            } else {
                try {
                    checkUsernameAndPassword(username, email, password, passwordConfirm)
                } catch (e: UnsupportedEncodingException) {
                    throw RuntimeException(e)
                }
            }
        }
        binding.signInText.setOnClickListener {
            findNavController().navigate(R.id.action_SignUpFragment_to_SignInFragment)
        }
    }

    // Проверка, всех данных введенных пользователем
    private fun checkUsernameAndPassword(username: String, email: String, password: String, passwordConfirm: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val usernameExists = checkUsernameExists(username)
            if (!usernameExists)
            {
                if (validatePassword(password, passwordConfirm))
                {
                    val idUser = generateUniqueRandomId(fetchUserIdsFromReviews())
                    addNewUser(idUser, username, email, password)

                    val dialogFragment = MyDialogFragment(this@SignUpFragment)
                    dialogFragment.show(parentFragmentManager, "dialog")

                } else { }
            } else {
                binding.editName.error = getString(R.string.userExistsError)
            }
        }
    }

    // Добавления нового пользователя в БД
    private fun addNewUser(idUser: Number, username: String, email: String, password: String) {
        val newUserJson = """
        {
          "_id": $idUser,
          "username": "$username",
          "email": "$email",
          "password": "$password",
          "notifications": [],
          "purchasedBooks": [],
          "favoriteBooks": [],
          "cart": []
        }
    """.trimIndent()

        val ipAddress = (activity as MainActivity).getIpAddress()
        val url = URL("http:$ipAddress:3000/api/users")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        try {
            val outputStream = OutputStreamWriter(connection.outputStream)
            outputStream.write(newUserJson)
            outputStream.flush()


            if (connection.responseCode == HttpURLConnection.HTTP_CREATED) {
                println("Пользователь успешно добавлен")
            } else {
                println("Ошибка при добавлении пользователя: ${connection.responseMessage}")
            }
        } catch (e: Exception) {
            println("Ошибка при отправке запроса: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }

    // Проверка, что имя пользователя уже существует в БД
    private fun checkUsernameExists(username: String): Boolean {
        return try {
            val ipAddress = (activity as MainActivity).getIpAddress()
            val url = URL("http:$ipAddress:3000/api/check_username/$username")

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            inputStream.close()
            connection.disconnect()

            val jsonResponse = JSONObject(response)
            jsonResponse.getBoolean("exists")

        } catch (e: Exception) {
            false
        }
    }

    // Метод генерирующий случайный id пользователя
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
    private suspend fun validatePassword(password: String, confirmPassword: String): Boolean {
        return if (password == confirmPassword) {
            true
        } else {
            withContext(Dispatchers.Main) {
                binding.editPasswordConfirm.error = getString(R.string.errorConfirmPassword)
            }
            false
        }
    }

    // Запрос возвращающий id всех уже имеющихся пользователей
    private suspend fun fetchUserIdsFromReviews(): ArrayList<Int> {
        return withContext(Dispatchers.IO) {
            val userIds = ArrayList<Int>()
            val ipAddress = (activity as MainActivity).getIpAddress()

            val url = URL("http://$ipAddress:3000/api/users/ids")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val response = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }

                val jsonArray = JSONArray(response)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val userId = jsonObject.getInt("_id")
                    userIds.add(userId)
                }
            } else {
                println("HTTP Error: $responseCode")
            }

            connection.disconnect()
            userIds
        }
    }

    // Метод возвращающий пользователя на начальный экран
    fun navigateToSignInFragment() {
        findNavController().navigate(R.id.action_SignUpFragment_to_SignInFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}