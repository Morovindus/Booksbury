package com.example.booksbury.book_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.booksbury.databinding.BookInfoSynopsisBinding


class BookInfoSynopsis(private val synopsisText: String) : Fragment() {

    private var _binding:BookInfoSynopsisBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = BookInfoSynopsisBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textSynopsis.text = synopsisText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}