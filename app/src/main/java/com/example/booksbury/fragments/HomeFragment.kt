package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterBooks
import com.example.booksbury.databinding.HomeFragmentBinding
import com.example.booksbury.items.ItemCart
import com.example.booksbury.items.MainBook
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonMore.setOnClickListener{
            findNavController().navigate(R.id.action_HomeFragment_to_ExploreFragment)
        }
        binding.buttonFavourites.setOnClickListener{
            findNavController().navigate(R.id.action_HomeFragment_to_FavouritesFragment)
        }
        binding.buttonProfile.setOnClickListener{
            findNavController().navigate(R.id.action_HomeFragment_to_ProfileFragment)
        }
        binding.mainImage.setOnClickListener{
            findNavController().navigate(R.id.action_HomeFragment_to_BookInfoFragment)
        }
        binding.buttonSearch.setOnClickListener{
            findNavController().navigate(R.id.action_HomeFragment_to_SearchFragment)
        }
        binding.buttonNotification.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_NotificaionFragment)
        }



        GlobalScope.launch(Dispatchers.IO) {
            val mainBook: MainBook = fetchMainBookFromServer()

            withContext(Dispatchers.Main) {
                Picasso.get().load(mainBook.imageResource).into(binding.mainImage)
                Picasso.get().load(mainBook.authorResource).into(binding.imageAuthor)
                binding.mainTitle.text = mainBook.titleBook
                binding.textAuthor.text = mainBook.nameAuthor

                binding.mainImage.setOnClickListener {
                    val id = mainBook.id
                    navigateToBookInfoFragment(id)
                }
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            val items: ArrayList<ItemCart> = fetchItemsFromServer()

            val adapter = CustomAdapterBooks(items, this@HomeFragment)
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
            binding.recyclerView.adapter = adapter
        }
    }

    fun navigateToBookInfoFragment(id: Int) {
        val bundle = Bundle().apply {
            putInt("id", id)
        }
        findNavController().navigate(R.id.action_HomeFragment_to_BookInfoFragment, bundle)
    }

    private fun fetchMainBookFromServer(): MainBook {
        val ipAddress = (activity as MainActivity).getIpAddress()

        val url = URL("http://$ipAddress:3000/api/books/one/random")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        val jsonResponse = JSONObject(response)
        val id = jsonResponse.getString("_id").toInt()
        val title = jsonResponse.getJSONObject("en").getString("title")
        val authorName = jsonResponse.getJSONObject("en").getString("authorName")
        val authorImageUrl = jsonResponse.getJSONObject("images").getString("authorImage")
        val bigCoverUrl = jsonResponse.getJSONObject("images").getString("bigCover")

        return MainBook(id, bigCoverUrl, title, authorImageUrl, authorName)
    }

    private fun fetchItemsFromServer(): ArrayList<ItemCart> {
        val ipAddress = (activity as MainActivity).getIpAddress()

        val url = URL("http://$ipAddress:3000/api/books/special/random")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        val jsonResponse = JSONArray(response)

        val items = ArrayList<ItemCart>()
        for (i in 0 until jsonResponse.length()) {
            val bookObject = jsonResponse.getJSONObject(i)
            val id = bookObject.getInt("_id")
            val title = bookObject.getString("title")
            val authorName = bookObject.getString("authorName")
            val stars = bookObject.getInt("averageStars")
            val ratings = bookObject.getInt("ratings")
            val price = bookObject.getInt("price")
            val smallCover = bookObject.getString("smallCover")

            val item = ItemCart(id, smallCover, title, authorName, stars, ratings, price)
            items.add(item)
        }
        return items
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}