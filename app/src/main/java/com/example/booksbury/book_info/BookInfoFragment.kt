package com.example.booksbury.book_info

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.booksbury.MainActivity
import com.example.booksbury.R
import com.example.booksbury.databinding.BookInfoFragmentBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class BookInfoFragment : Fragment() {

    private var _binding: BookInfoFragmentBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = BookInfoFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val idBook = arguments?.getInt("id", 0) ?: 0

        GlobalScope.launch(Dispatchers.IO) {
            fetchItemsFromServer(idBook)
        }

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        val adapter = ViewPagerAdapter(requireActivity(), idBook)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tabs_synopsis)
                1 -> getString(R.string.tabs_details)
                2 -> getString(R.string.tabs_author)
                3 -> getString(R.string.tabs_review)
                else -> ""
            }
        }.attach()

        val k = 1

        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val container = binding.container

        if (k == 0) {
            inflater.inflate(R.layout.button_read, container, true)
        } else if (k == 1) {
            binding.buttonFavourites.visibility = View.INVISIBLE
            binding.buttonFavourites.isEnabled = false
            inflater.inflate(R.layout.button_buy, container, true)

            val buttonBuy = container.findViewById<AppCompatImageButton>(R.id.button_buy_book)
            buttonBuy.setOnClickListener {
                Log.d("MyLogs", "Button Buy clicked")
            }
        }
    }

    private inner class ViewPagerAdapter(activity: FragmentActivity, private val idBook: Int) :
        FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> BookInfoSynopsis(idBook)
            1 -> BookInfoDetails(idBook)
            2 -> BookInfoAuthor(idBook)
            3 -> BookInfoReviews(idBook)
            else -> Fragment()
        }
    }


    private fun fetchItemsFromServer(id: Int) {
        val ipAddress = (activity as MainActivity).getIpAddress()

        val url = URL("http://$ipAddress:3000/api/books/more/$id")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val inputStream = connection.inputStream
        val response = inputStream.bufferedReader().use { it.readText() }

        val jsonResponse = JSONObject(response)


        requireActivity().runOnUiThread {
            binding.titleBook.text = jsonResponse.getJSONObject("en").getString("title")
            binding.nameAuthor.text = "By " + jsonResponse.getJSONObject("en").getString("authorName")
            binding.price.text = "${jsonResponse.getInt("price")}\u20BD"
            binding.dateReleased.text = jsonResponse.getInt("released").toString()
            binding.part.text = jsonResponse.getInt("part").toString()
            binding.page.text = jsonResponse.getInt("page").toString()

            val middleCover = jsonResponse.getJSONObject("images").getString("middleCover")
            Picasso.get().load(middleCover).into(binding.mainImage)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}