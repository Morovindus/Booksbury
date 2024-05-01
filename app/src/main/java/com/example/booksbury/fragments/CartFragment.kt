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
import com.example.booksbury.adapters.CustomAdapterCart
import com.example.booksbury.databinding.CartFragmentBinding
import com.example.booksbury.items.ItemCart

class CartFragment : Fragment() {

    private var _binding: CartFragmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = CartFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val totalPriceString = getString(R.string.total_price)
        binding.totalPrice.text = "$totalPriceString\u20BD"

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


        val adapter = CustomAdapterCart(items)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(SpacesItemDecoration(80, 0))
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}