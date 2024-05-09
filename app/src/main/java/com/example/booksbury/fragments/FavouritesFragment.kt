package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.booksbury.R
import com.example.booksbury.databinding.FavouritesFragmentBinding

class FavouritesFragment : Fragment() {

    private var _binding: FavouritesFragmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FavouritesFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonHome.setOnClickListener{
            findNavController().navigate(R.id.action_FavouritesFragment_to_HomeFragment)
        }
        binding.buttonExplore.setOnClickListener{
            findNavController().navigate(R.id.action_FavouritesFragment_to_ExploreFragment)
        }
        binding.buttonProfile.setOnClickListener{
            findNavController().navigate(R.id.action_FavouritesFragment_to_ProfileFragment)
        }
        binding.buttonSearch.setOnClickListener{
            findNavController().navigate(R.id.action_FavouritesFragment_to_SearchFragment)
        }
        binding.buttonNotification.setOnClickListener {
            findNavController().navigate(R.id.action_FavouritesFragment_to_NotificaionFragment)
        }


        /*val items = ArrayList<ItemCart>()
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

        items.add(
            ItemCart(
                R.drawable.covers_book_5, resources.getString(R.string.title_book_fifth),
            resources.getString(R.string.author_fifth),
            resources.getString(R.string.stars_fifth).toInt(),
            resources.getString(R.string.ratings_fifth).toInt(),
            resources.getString(R.string.price_fifth).toInt(),)
        )

        items.add(
            ItemCart(
                R.drawable.covers_book_6, resources.getString(R.string.title_book_six),
            resources.getString(R.string.author_six),
            resources.getString(R.string.stars_six).toInt(),
            resources.getString(R.string.ratings_six).toInt(),
            resources.getString(R.string.price_six).toInt(),)
        )

        items.add(
            ItemCart(
                R.drawable.covers_book_7, resources.getString(R.string.title_book_seven),
            resources.getString(R.string.author_seven),
            resources.getString(R.string.stars_seven).toInt(),
            resources.getString(R.string.ratings_seven).toInt(),
            resources.getString(R.string.price_seven).toInt(),)
        )

        items.add(
            ItemCart(
                R.drawable.covers_book_8, resources.getString(R.string.title_book_eighth),
            resources.getString(R.string.author_eighth),
            resources.getString(R.string.stars_eighth).toInt(),
            resources.getString(R.string.ratings_eighth).toInt(),
            resources.getString(R.string.price_eighth).toInt(),)
        )


        val adapter = CustomAdapterBooks(items)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
        binding.recyclerView.adapter = adapter*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}