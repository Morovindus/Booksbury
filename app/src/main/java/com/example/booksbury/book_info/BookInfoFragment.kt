package com.example.booksbury.book_info

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.booksbury.R
import com.example.booksbury.databinding.BookInfoFragmentBinding
import com.example.booksbury.items.Reviews
import com.google.android.material.tabs.TabLayoutMediator

class BookInfoFragment : Fragment() {

    private var _binding: BookInfoFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var profileAuthor: ImageView
    private lateinit var nameAuthorInfo: String
    private lateinit var textAuthor: String
    private lateinit var synopsis: String
    private lateinit var details: String

    private lateinit var reviews: ArrayList<Reviews>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = BookInfoFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }


        profileAuthor = ImageView(requireContext())
        profileAuthor.setImageResource(R.drawable.profile_author_4)

        synopsis = getString(R.string.synopsis_4_book)
        details = getString(R.string.details_4_book)

        nameAuthorInfo = getString(R.string.name_author_4)
        textAuthor = getString(R.string.about_author_4)


        binding.mainImage.setImageResource(R.drawable.book_more)

        binding.titleBook.text = resources.getString(R.string.book_title_4)
        binding.nameAuthor.text = resources.getString(R.string.author_4)
        binding.price.text = resources.getString(R.string.price_4)
        binding.dateReleased.text = resources.getString(R.string.date_4_book)
        binding.part.text = resources.getString(R.string.part_4_book)
        binding.page.text = resources.getString(R.string.page_4_book)


        reviews = arrayListOf(
            Reviews(resources.getString(R.string.author_reviews_1), resources.getString(R.string.date_reviews_1),
                resources.getString(R.string.reviews_1), resources.getString(R.string.stars_reviews_1).toInt()),
            Reviews(resources.getString(R.string.author_reviews_2), resources.getString(R.string.date_reviews_2),
                resources.getString(R.string.reviews_2), resources.getString(R.string.stars_reviews_2).toInt()),
            Reviews(resources.getString(R.string.author_reviews_3), resources.getString(R.string.date_reviews_3),
                resources.getString(R.string.reviews_3), resources.getString(R.string.stars_reviews_3).toInt()),
        )

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        val adapter = ViewPagerAdapter(requireActivity())
        viewPager.adapter = adapter

        val tabTitles = arrayOf(
            getString(R.string.tabs_synopsis),
            getString(R.string.tabs_details),
            getString(R.string.tabs_author),
            getString(R.string.tabs_review)
        )

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()



        val k = 1

        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val container = binding.container

        if (k == 0) {
            inflater.inflate(R.layout.button_read, container, true)
        } else if (k == 1) {
            binding.buttonFavourites.visibility = View.INVISIBLE
            binding.buttonFavourites.isEnabled = false
            inflater.inflate(R.layout.button_buy, container, true)

            val buttonBuy = container.findViewById<AppCompatImageButton>(R.id.button_buy_book)
            buttonBuy.setOnClickListener {
                Log.d("MyLogs", "Button Buy clicked")
            }

        }

    }



    private inner class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
            val fragment = when (position) {
                0 -> BookInfoSynopsis(synopsis)
                1 -> BookInfoSynopsis(details)
                2 -> BookInfoAuthor(profileAuthor, nameAuthorInfo, textAuthor)
                3 -> BookInfoReviews(reviews)
                else -> Fragment()
            }
            return fragment
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}