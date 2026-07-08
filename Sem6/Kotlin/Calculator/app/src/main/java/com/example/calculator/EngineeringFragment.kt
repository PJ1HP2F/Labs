package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.calculator.R

class EngineeringFragment : Fragment() {
    private val viewModel: CalculatorViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_engineering, container, false)

        view.findViewById<Button>(R.id.btnFact)?.setOnClickListener { viewModel.applyFactorial() }
        view.findViewById<Button>(R.id.btnPow)?.setOnClickListener { viewModel.addSymbol("^") }
        view.findViewById<Button>(R.id.btnSqrt)?.setOnClickListener { viewModel.addSymbol("√") }
        view.findViewById<Button>(R.id.btnOpenBracket)?.setOnClickListener { viewModel.addSymbol("(") }
        view.findViewById<Button>(R.id.btnCloseBracket)?.setOnClickListener { viewModel.addSymbol(")") }
        view.findViewById<Button>(R.id.btnDelete)?.setOnClickListener { viewModel.deleteLast() }

        return view
    }
}