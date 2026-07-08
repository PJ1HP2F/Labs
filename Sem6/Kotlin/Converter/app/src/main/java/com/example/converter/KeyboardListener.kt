package com.example.converter

interface KeyboardListener {
    fun onKeyboardButtonClicked(action: KeyboardAction)
}

enum class KeyboardAction {
    DIGIT_0, DIGIT_1, DIGIT_2, DIGIT_3, DIGIT_4,
    DIGIT_5, DIGIT_6, DIGIT_7, DIGIT_8, DIGIT_9,
    DOT, CLEAR, DELETE, CONVERT
}