package com.example.booksbury.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.example.booksbury.R
import com.example.booksbury.fragments.SignUpFragment

// Диалог, уведомляющий пользователя о успешной регистрации
class MyDialogFragment(private val signUpFragment: SignUpFragment) : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_registration, container, false)

        val button = view.findViewById<ImageButton>(R.id.button_home)
        button.setOnClickListener {
            signUpFragment.navigateToSignInFragment()
            dismiss()
        }

        return view
    }
}