package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.calculator.R

class BasicFragment : Fragment() {
    private val viewModel: CalculatorViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_basic, container, false)

        val buttons = mapOf<Int, String>(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2",
            R.id.btn3 to "3", R.id.btn4 to "4", R.id.btn5 to "5",
            R.id.btn6 to "6", R.id.btn7 to "7", R.id.btn8 to "8",
            R.id.btn9 to "9", R.id.btnAdd to "+", R.id.btnSub to "-",
            R.id.btnMul to "*", R.id.btnDiv to "/", R.id.btnDot to ".",
            R.id.btnPercent to "%"
        )

        buttons.forEach { (id, value) ->
            view.findViewById<Button>(id)?.setOnClickListener { viewModel.addSymbol(value) }
        }

        view.findViewById<Button>(R.id.btnClear)?.setOnClickListener { viewModel.clear() }
        view.findViewById<Button>(R.id.btnEqual)?.setOnClickListener { viewModel.calculate() }

        return view
    }
}