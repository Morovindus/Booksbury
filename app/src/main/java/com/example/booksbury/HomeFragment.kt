package com.example.booksbury

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.booksbury.databinding.HomeFragmentBinding

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

    companion object{
        @JvmStatic
        fun newInstance() = HomeFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonMore: Button = binding.buttonMore;
        val buttonImage: ImageButton = binding.mainImage;
        val buttonSearch: Button = binding.buttonSearch;

        buttonMore.setOnClickListener{
            findNavController().navigate(R.id.action_HomeFragment_to_ExploreFragment)
        }
        buttonImage.setOnClickListener{
            findNavController().navigate(R.id.action_HomeFragment_to_BookInfoFragment)
        }
        buttonSearch.setOnClickListener{
            findNavController().navigate(R.id.action_HomeFragment_to_SearchFragment)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}