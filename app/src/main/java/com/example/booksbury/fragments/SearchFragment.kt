package com.example.booksbury.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.booksbury.databinding.SearchFragmentBinding

class SearchFragment : Fragment() {

    private var _binding: SearchFragmentBinding? = null
    private val binding get() = _binding!!

    private var enteredText: String = ""

    private lateinit var editText: EditText

    public companion object {
        const val ENTERED_TEXT_KEY = "entered_text"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = SearchFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        editText = binding.searchBar
        val buttonCancel = binding.buttonCancel

        // Восстанавливаем сохраненное значение, если оно есть
        savedInstanceState?.let {
            enteredText = it.getString(ENTERED_TEXT_KEY, "")
            editText.setText(enteredText)
        }

        // Скрываем кнопку при запуске активности
        buttonCancel.visibility = View.INVISIBLE
        buttonCancel.isEnabled = false

        // Устанавливаем слушатель для изменений текста в текстовом поле
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Если текстовое поле не пустое, показываем кнопку, иначе скрываем
                if (s.isNullOrEmpty()) {
                    buttonCancel.visibility = Button.INVISIBLE
                    buttonCancel.isEnabled = false
                } else {
                    buttonCancel.visibility = Button.VISIBLE
                    buttonCancel.isEnabled = true
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Сброс текста и скрытие клавиатуры при нажатии на кнопку
        buttonCancel.setOnClickListener {
            editText.text.clear()
            hideKeyboard(editText)
        }
    }

    // Метод для скрытия клавиатуры
    fun hideKeyboard(editText: EditText) {
        val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем введенный текст
        outState.putString(ENTERED_TEXT_KEY, editText.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}