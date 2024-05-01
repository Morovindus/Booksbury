package com.example.booksbury.book_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.booksbury.databinding.BookInfoAuthorBinding

class BookInfoAuthor(private val profileAuthor: ImageView, private val nameAuthor: String, private val textAuthor: String) : Fragment() {

    private var _binding: BookInfoAuthorBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = BookInfoAuthorBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profileAuthor.setImageDrawable(profileAuthor.drawable)
        binding.nameAuthor.text = nameAuthor
        binding.textAuthor.text = textAuthor
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}