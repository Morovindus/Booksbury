package com.example.booksbury.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.example.booksbury.R
import com.example.booksbury.fragments.ReviewFragment

// Диалог, уведомляющий пользователя о успешной оставленном отзыве
class MyDialogFragmentReview(private val reviewFragment: ReviewFragment) : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_review, container, false)

        // Задание горизонтальных отступов
        val horizontalMargin = resources.getDimensionPixelSize(R.dimen.bigPadding)
        view.setPadding(horizontalMargin, 0, horizontalMargin, 0)

        val button = view.findViewById<ImageButton>(R.id.button_home)
        button.setOnClickListener {
            reviewFragment.navigateToSignInFragment()
            dismiss()
        }

        return view
    }
    override fun onStart() {
        super.onStart()
        // Установка размеров диалогового окна
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}