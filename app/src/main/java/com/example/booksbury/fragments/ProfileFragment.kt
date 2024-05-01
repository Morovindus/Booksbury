package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.booksbury.R
import com.example.booksbury.databinding.ProfileFragmentBinding

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

        binding.nameUser.text = resources.getString(R.string.name_user)
        binding.emailUser.text = resources.getString(R.string.email_user)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}