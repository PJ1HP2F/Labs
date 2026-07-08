package com.example.converter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.converter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), KeyboardListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dataFragment: DataFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем ссылку на DataFragment
        dataFragment = supportFragmentManager.findFragmentById(R.id.data_fragment) as DataFragment
    }

    // Метод, вызываемый клавиатурой при нажатии на кнопки
    override fun onKeyboardButtonClicked(action: KeyboardAction) {
        dataFragment.handleKeyboardAction(action)
    }
}