package com.example.booksbury.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.booksbury.R
import com.example.booksbury.databinding.SignUpFragmentBinding

class SignUpFragment : Fragment() {

    private var _binding: SignUpFragmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = SignUpFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRegistration.setOnClickListener {
            findNavController().navigate(R.id.action_SignUpFragment_to_SignInFragment)
        }
        binding.signInText.setOnClickListener {
            findNavController().navigate(R.id.action_SignUpFragment_to_SignInFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}