package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.databinding.SignInFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL

class SignInFragment : Fragment() {

    private var _binding: SignInFragmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = SignInFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        binding.signUpText.setOnClickListener {
            //findNavController().navigate(R.id.action_SignInFragment_to_SignUpFragment)
            findNavController().navigate(R.id.action_SignInFragment_to_HomeFragment)
        }
    }

    private fun checkUsernameAndPassword(username: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val usernameExists = checkUsernameExists(username)
            if (usernameExists) {
                val passwordValid = checkPasswordValid(username, password)
                if (passwordValid) {
                    val idUser = getIdFromServer(username)
                    (activity as MainActivity).setIdUser(idUser)
                    withContext(Dispatchers.Main) {
                        findNavController().navigate(R.id.action_SignInFragment_to_HomeFragment)
                    }

                } else {
                    withContext(Dispatchers.Main) {
                        binding.editPassword.error = getString(R.string.incorrectPassword)
                        binding.editPassword.setText("")
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    binding.editUsername.error = getString(R.string.incorrectUsername)
                    binding.editUsername.setText("")
                    binding.editPassword.setText("")
                }
            }
        }
    }

    private fun getIdFromServer(username: String): Int {
        val ipAddress = (activity as MainActivity).getIpAddress()
        val url = URL("http:$ipAddress:3000/api/get_user_id/$username")

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        inputStream.close() // Закрываем inputStream

        connection.disconnect() // Закрываем соединение

        val jsonResponse = JSONObject(response)

        return jsonResponse.getString("id").toInt()
    }

    private fun checkUsernameExists(username: String): Boolean {
        return try {
            val ipAddress = (activity as MainActivity).getIpAddress()
            val url = URL("http:$ipAddress:3000/api/check_username/$username")

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            inputStream.close() // Закрываем inputStream
            connection.disconnect() // Закрываем соединение

            val jsonResponse = JSONObject(response)
            jsonResponse.getBoolean("exists")

        } catch (e: Exception) {
            false
        }
    }

    private fun checkPasswordValid(username: String, password: String): Boolean {
        return try {
            val ipAddress = (activity as MainActivity).getIpAddress()
            val url = URL("http:$ipAddress:3000/api/check_password/$username/$password")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream = connection.inputStream
            val response = inputStream.bufferedReader().use { it.readText() }

            inputStream.close() // Закрываем inputStream
            connection.disconnect() // Закрываем соединение

            val jsonResponse = JSONObject(response)
            jsonResponse.getBoolean("match")
        } catch (e: Exception) {
            false
        }
    }



    private fun validateUsernameAndPassword(username: String, password: String): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            binding.editUsername.error = getString(R.string.errorName)
            isValid = false
        } else {
            binding.editUsername.error = null
        }

        if (password.isEmpty()) {
            binding.editPassword.error = getString(R.string.errorPassword)
            isValid = false
        } else {
            binding.editPassword.error = null
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}