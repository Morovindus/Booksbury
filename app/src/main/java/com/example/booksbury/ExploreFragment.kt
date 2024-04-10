package com.example.booksbury

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.booksbury.databinding.ExploreFragmentBinding

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

    companion object{
        @JvmStatic
        fun newInstance() = ExploreFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //val button3: Button = requireActivity().findViewById(R.id.personal_button)
        val buttonMore: Button = binding.buttonHome;
        buttonMore.setOnClickListener{
            findNavController().navigate(R.id.action_ExploreFragment_to_HomeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}