package com.example.booksbury.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.databinding.ProfileFragmentBinding
import com.example.booksbury.items.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ProfileFragment : Fragment() {

    private var _binding: ProfileFragmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = ProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonHome.setOnClickListener{
            findNavController().navigate(R.id.action_ProfileFragment_to_HomeFragment)
        }
        binding.buttonExplore.setOnClickListener{
            findNavController().navigate(R.id.action_ProfileFragment_to_ExploreFragment)
        }
        binding.buttonFavourites.setOnClickListener{
            findNavController().navigate(R.id.action_ProfileFragment_to_FavouritesFragment)
        }
        binding.buttonSearch.setOnClickListener{
            findNavController().navigate(R.id.action_ProfileFragment_to_SearchFragment)
        }
        binding.buttonNotification.setOnClickListener {
            findNavController().navigate(R.id.action_ProfileFragment_to_NotificaionFragment)
        }
        binding.buttonCart.setOnClickListener {
            findNavController().navigate(R.id.action_ProfileFragment_to_CartFragment)
        }
        binding.buttonBooks.setOnClickListener {
            findNavController().navigate(R.id.action_ProfileFragment_to_BooksFragment)
        }
        binding.buttonExit.setOnClickListener {
            showExitConfirmationDialog()
        }

        GlobalScope.launch(Dispatchers.IO) {
            val user: User = fetchNameEmailDataFromServer()

            binding.nameUser.text = user.userName
            binding.emailUser.text = user.email

        }
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}