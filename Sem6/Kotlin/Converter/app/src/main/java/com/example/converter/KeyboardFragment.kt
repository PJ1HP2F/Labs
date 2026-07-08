package com.example.converter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import android.util.Log

class KeyboardFragment : Fragment() {

    public var listener: KeyboardListener? = null

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        listener = context as? KeyboardListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Log.d("KeyboardFragment", "onCreateView called")
        val view = inflater.inflate(R.layout.fragment_keyboard, container, false)

        // Кнопки цифр
        view.findViewById<Button>(R.id.btn_0).setOnClickListener { sendAction(KeyboardAction.DIGIT_0) }
        view.findViewById<Button>(R.id.btn_1).setOnClickListener { sendAction(KeyboardAction.DIGIT_1) }
        view.findViewById<Button>(R.id.btn_2).setOnClickListener { sendAction(KeyboardAction.DIGIT_2) }
        view.findViewById<Button>(R.id.btn_3).setOnClickListener { sendAction(KeyboardAction.DIGIT_3) }
        view.findViewById<Button>(R.id.btn_4).setOnClickListener { sendAction(KeyboardAction.DIGIT_4) }
        view.findViewById<Button>(R.id.btn_5).setOnClickListener { sendAction(KeyboardAction.DIGIT_5) }
        view.findViewById<Button>(R.id.btn_6).setOnClickListener { sendAction(KeyboardAction.DIGIT_6) }
        view.findViewById<Button>(R.id.btn_7).setOnClickListener { sendAction(KeyboardAction.DIGIT_7) }
        view.findViewById<Button>(R.id.btn_8).setOnClickListener { sendAction(KeyboardAction.DIGIT_8) }
        view.findViewById<Button>(R.id.btn_9).setOnClickListener { sendAction(KeyboardAction.DIGIT_9) }
        view.findViewById<Button>(R.id.btn_dot).setOnClickListener { sendAction(KeyboardAction.DOT) }
        view.findViewById<Button>(R.id.btn_clear).setOnClickListener { sendAction(KeyboardAction.CLEAR) }
        view.findViewById<Button>(R.id.btn_delete).setOnClickListener { sendAction(KeyboardAction.DELETE) }
        view.findViewById<Button>(R.id.btn_convert).setOnClickListener { sendAction(KeyboardAction.CONVERT) }

        return view
    }

    private fun sendAction(action: KeyboardAction) {
        listener?.onKeyboardButtonClicked(action)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}