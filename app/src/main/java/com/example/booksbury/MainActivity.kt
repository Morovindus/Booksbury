package com.example.booksbury

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.booksbury.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*binding.basketButton.setOnClickListener {
            val fragmentManager = supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment1, SecondFragment.newInstance())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.shopButton.setOnClickListener {
            val fragmentManager = supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment1, FirstFragment.newInstance())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.personalButton.setOnClickListener {
            val fragmentManager = supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment1, ThirdFragment.newInstance())
            transaction.addToBackStack(null)
            transaction.commit()
        }*/
    }
}