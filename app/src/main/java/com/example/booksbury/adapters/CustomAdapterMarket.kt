package com.example.booksbury.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booksbury.R
import com.example.booksbury.items.ItemCart

class CustomAdapterMarket(private val items: ArrayList<ItemCart>) : RecyclerView.Adapter<CustomAdapterMarket.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_explore, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        holder.imageBook.setImageResource(currentItem.imageResource)
        holder.titleBook.text = currentItem.titleBook
        holder.nameAuthor.text = currentItem.nameAuthor
        holder.price.text = "${currentItem.price}\u20BD"
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageBook: ImageView = itemView.findViewById(R.id.imageBook)
        val titleBook: TextView = itemView.findViewById(R.id.book_title)
        val nameAuthor: TextView = itemView.findViewById(R.id.book_author)
        val price: TextView = itemView.findViewById(R.id.price)

    }
}