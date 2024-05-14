package com.example.booksbury.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booksbury.R
import com.example.booksbury.fragments.BooksFragment
import com.example.booksbury.items.Book
import com.squareup.picasso.Picasso

class CustomAdapterPurchasedBooks(private val items: ArrayList<Book>, private val booksFragment: BooksFragment) : RecyclerView.Adapter<CustomAdapterPurchasedBooks.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_books, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]


        Picasso.get().load(currentItem.imageResource).into(holder.imageCoverBook)

        holder.titleBook.text = currentItem.titleBook
        holder.nameAuthor.text = currentItem.nameAuthor
        holder.ratings.text = "${currentItem.ratings} ${booksFragment.getString(R.string.ratings)}"


        val orangeStarDrawable = R.drawable.star_orange

        if (currentItem.stars >= 1) holder.starFirst.setImageResource(orangeStarDrawable)
        if (currentItem.stars >= 2) holder.starSecond.setImageResource(orangeStarDrawable)
        if (currentItem.stars >= 3) holder.starThird.setImageResource(orangeStarDrawable)
        if (currentItem.stars >= 4) holder.starFourth.setImageResource(orangeStarDrawable)
        if (currentItem.stars >= 5) holder.starFifth.setImageResource(orangeStarDrawable)

        holder.imageCoverBook.setOnClickListener {
            val id = currentItem.id
            booksFragment.navigateToBookInfoFragment(id)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageCoverBook: ImageView = itemView.findViewById(R.id.imageCoverBook)
        val titleBook: TextView = itemView.findViewById(R.id.title_book)
        val nameAuthor: TextView = itemView.findViewById(R.id.name_author)
        val ratings: TextView = itemView.findViewById(R.id.textRating)

        val starFirst: ImageView = itemView.findViewById(R.id.starFirst)
        val starSecond: ImageView = itemView.findViewById(R.id.starSecond)
        val starThird: ImageView = itemView.findViewById(R.id.starThird)
        val starFourth: ImageView = itemView.findViewById(R.id.starFourth)
        val starFifth: ImageView = itemView.findViewById(R.id.starFifth)
    }
}