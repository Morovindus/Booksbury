package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterBooks
import com.example.booksbury.databinding.HomeFragmentBinding
import com.example.booksbury.items.ItemCart

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

        binding.mainImage.setImageResource(R.drawable.book_main)
        binding.imageAuthor.setImageResource(R.drawable.ellipse_4)

        binding.mainTitle.text = resources.getString(R.string.Main_description)
        binding.textListen.text = resources.getString(R.string.Listen)
        binding.textAuthor.text = resources.getString(R.string.name_author)

        val items = ArrayList<ItemCart>()
        items.add(
            ItemCart(
                R.drawable.covers_book_1, resources.getString(R.string.title_book_first),
            resources.getString(R.string.author_first),
            resources.getString(R.string.stars_first).toInt(),
            resources.getString(R.string.ratings_first).toInt(),
            resources.getString(R.string.price_first).toInt(),)
        )

        items.add(
            ItemCart(
                R.drawable.covers_book_2, resources.getString(R.string.title_book_second),
            resources.getString(R.string.author_second),
            resources.getString(R.string.stars_second).toInt(),
            resources.getString(R.string.ratings_second).toInt(),
            resources.getString(R.string.price_second).toInt(),)
        )

        items.add(
            ItemCart(
                R.drawable.covers_book_3, resources.getString(R.string.title_book_third),
            resources.getString(R.string.author_third),
            resources.getString(R.string.stars_third).toInt(),
            resources.getString(R.string.ratings_third).toInt(),
            resources.getString(R.string.price_third).toInt(),)
        )

        items.add(
            ItemCart(
                R.drawable.covers_book_4, resources.getString(R.string.title_book_fourth),
            resources.getString(R.string.author_fourth),
            resources.getString(R.string.stars_fourth).toInt(),
            resources.getString(R.string.ratings_fourth).toInt(),
            resources.getString(R.string.price_fourth).toInt(),)
        )

        val adapter = CustomAdapterBooks(items)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}