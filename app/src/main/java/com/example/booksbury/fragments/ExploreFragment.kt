package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterMarket
import com.example.booksbury.databinding.ExploreFragmentBinding
import com.example.booksbury.items.ItemExplore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class ExploreFragment : Fragment() {

    private var _binding: ExploreFragmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = ExploreFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonHome.setOnClickListener{
            findNavController().navigate(R.id.action_ExploreFragment_to_HomeFragment)
        }
        binding.buttonFavourites.setOnClickListener{
            findNavController().navigate(R.id.action_ExploreFragment_to_FavouritesFragment)
        }
        binding.buttonProfile.setOnClickListener{
            findNavController().navigate(R.id.action_ExploreFragment_to_ProfileFragment)
        }
        binding.buttonSearch.setOnClickListener{
            findNavController().navigate(R.id.action_ExploreFragment_to_SearchFragment)
        }
        binding.buttonNotification.setOnClickListener {
            findNavController().navigate(R.id.action_ExploreFragment_to_NotificationFragment)
        }

        GlobalScope.launch(Dispatchers.IO) {
            val items: ArrayList<ItemExplore> = fetchItemsFromServer()

            val adapter = CustomAdapterMarket(items, this@ExploreFragment)
            val layoutManager = GridLayoutManager(requireContext(), 2)
            binding.recyclerView.layoutManager = layoutManager
            binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 20))
            binding.recyclerView.adapter = adapter
        }

    }

    fun navigateToBookInfoFragment(id: Int) {
        val bundle = Bundle().apply {
            putInt("id", id)
        }
        findNavController().navigate(R.id.action_ExploreFragment_to_BookInfoFragment, bundle)
    }

    private fun fetchItemsFromServer(): ArrayList<ItemExplore> {
        val ipAddress = (activity as MainActivity).getIpAddress()

        val url = URL("http://$ipAddress:3000/api/all_books/all")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        val jsonResponse = JSONArray(response)

        val items = ArrayList<ItemExplore>()
        for (i in 0 until jsonResponse.length()) {
            val bookObject = jsonResponse.getJSONObject(i)
            val id = bookObject.getInt("_id")
            val title = bookObject.getJSONObject("en").getString("title")
            val authorName = bookObject.getJSONObject("en").getString("authorName")
            val price = bookObject.getInt("price")
            val averageCover = bookObject.getJSONObject("images").getString("averageCover")

            val item = ItemExplore(id, averageCover, title, authorName, price)
            items.add(item)
        }
        return items
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}