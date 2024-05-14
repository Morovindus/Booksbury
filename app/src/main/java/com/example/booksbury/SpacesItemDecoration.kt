package com.example.booksbury

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


// Декоратор, определяющий отступы между элементами списка в RecyclerView
class SpacesItemDecoration(private val verticalSpace: Int, private val horizontalSpace: Int) : ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.bottom = verticalSpace
        outRect.left = horizontalSpace
        outRect.right = horizontalSpace
    }
}