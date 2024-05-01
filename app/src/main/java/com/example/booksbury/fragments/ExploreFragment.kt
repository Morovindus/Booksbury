package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.booksbury.R
import com.example.booksbury.SpacesItemDecoration
import com.example.booksbury.adapters.CustomAdapterMarket
import com.example.booksbury.databinding.ExploreFragmentBinding
import com.example.booksbury.items.ItemCart

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

        val items = ArrayList<ItemCart>()
        items.add(
            ItemCart(
                R.drawable.book_1, resources.getString(R.string.book_title_1),
                resources.getString(R.string.author_1),
                resources.getString(R.string.stars_first).toInt(),
                resources.getString(R.string.ratings_first).toInt(),
                resources.getString(R.string.price_1).toInt(),)
        )

        items.add(
            ItemCart(
                R.drawable.book_2, resources.getString(R.string.book_title_2),
                resources.getString(R.string.author_2),
                resources.getString(R.string.stars_second).toInt(),
                resources.getString(R.string.ratings_second).toInt(),
                resources.getString(R.string.price_2).toInt(),)
        )

        items.add(
            ItemCart(
                R.drawable.book_3, resources.getString(R.string.book_title_3),
                resources.getString(R.string.author_3),
                resources.getString(R.string.stars_third).toInt(),
                resources.getString(R.string.ratings_third).toInt(),
                resources.getString(R.string.price_3).toInt(),)
        )

        items.add(
            ItemCart(
                R.drawable.book_4, resources.getString(R.string.book_title_4),
                resources.getString(R.string.author_4),
                resources.getString(R.string.stars_fourth).toInt(),
                resources.getString(R.string.ratings_fourth).toInt(),
                resources.getString(R.string.price_4).toInt(),)
        )

        items.add(
            ItemCart(
                R.drawable.book_5, resources.getString(R.string.book_title_5),
                resources.getString(R.string.author_5),
                resources.getString(R.string.stars_fourth).toInt(),
                resources.getString(R.string.ratings_fourth).toInt(),
                resources.getString(R.string.price_5).toInt(),)
        )

        items.add(
            ItemCart(
                R.drawable.book_6, resources.getString(R.string.book_title_6),
                resources.getString(R.string.author_6),
                resources.getString(R.string.stars_fourth).toInt(),
                resources.getString(R.string.ratings_fourth).toInt(),
                resources.getString(R.string.price_6).toInt(),)
        )

        val adapter = CustomAdapterMarket(items)
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 20))
        binding.recyclerView.adapter = adapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}