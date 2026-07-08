package com.example.converter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.converter.model.Unit
import com.example.converter.model.Units
import com.example.converter.model.Category
import com.example.converter.model.Converter
import java.util.Locale

const val MAX_LENGTH = 12

open class BaseDataFragment : Fragment() {

    protected lateinit var inputTextView: TextView
    protected lateinit var resultTextView: TextView
    protected lateinit var categorySpinner: Spinner
    protected lateinit var fromUnitSpinner: Spinner
    protected lateinit var toUnitSpinner: Spinner

    protected var currentInput = ""
    protected var categories = Category.values()
    protected var units = Units.all
    protected var isRestoring = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = null

    protected fun initCommonViews(view: View) {
        inputTextView = view.findViewById(R.id.input_value)
        resultTextView = view.findViewById(R.id.result_value)
        categorySpinner = view.findViewById(R.id.category_spinner)
        fromUnitSpinner = view.findViewById(R.id.from_unit_spinner)
        toUnitSpinner = view.findViewById(R.id.to_unit_spinner)

        setupCategorySpinner()
        setupUnitSpinners()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            isRestoring = true
            currentInput = it.getString("current_input", "")
            inputTextView.text = currentInput

            val categoryPos = it.getInt("category_pos", 0)
            val fromUnitPos = it.getInt("from_unit_pos", 0)
            val toUnitPos = it.getInt("to_unit_pos", 0)

            categorySpinner.setSelection(categoryPos)
            categorySpinner.post {
                fromUnitSpinner.setSelection(fromUnitPos)
                toUnitSpinner.setSelection(toUnitPos)
                updateResult()
                isRestoring = false
            }
        } ?: run {
            updateResult()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("current_input", currentInput)
        outState.putInt("category_pos", categorySpinner.selectedItemPosition)
        outState.putInt("from_unit_pos", fromUnitSpinner.selectedItemPosition)
        outState.putInt("to_unit_pos", toUnitSpinner.selectedItemPosition)
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories.map { it.displayName })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (!isRestoring) {
                    currentInput = ""
                    inputTextView.text = ""
                    resultTextView.text = ""
                }
                updateUnitsForCategory(categories[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateUnitsForCategory(category: Category) {
        val filtered = units.filter { it.category == category }
        val unitNames = filtered.map { it.name }

        val fromAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, unitNames)
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fromUnitSpinner.adapter = fromAdapter

        val toAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, unitNames)
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        toUnitSpinner.adapter = toAdapter

        fromUnitSpinner.onItemSelectedListener = unitSelectionListener
        toUnitSpinner.onItemSelectedListener = unitSelectionListener
    }

    private val unitSelectionListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            updateResult()
        }
        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    private fun setupUnitSpinners() {
        updateUnitsForCategory(categories[0])
    }

    fun handleKeyboardAction(action: KeyboardAction) {
        when (action) {
            KeyboardAction.CLEAR -> {
                currentInput = ""
                inputTextView.text = ""
                resultTextView.text = ""
            }
            KeyboardAction.DELETE -> {
                if (currentInput.isNotEmpty()) {
                    currentInput = currentInput.dropLast(1)
                    inputTextView.text = currentInput
                    updateResult()
                }
            }
            KeyboardAction.CONVERT -> updateResult()
            KeyboardAction.DOT -> {
                if (!currentInput.contains('.')) {
                    when {
                        currentInput.isEmpty() -> currentInput = "0."
                        else -> currentInput += "."
                    }
                    inputTextView.text = currentInput
                }
            }
            else -> { // DIGIT_0 .. DIGIT_9
                val digit = action.name.last().digitToInt()

                if (currentInput.length >= MAX_LENGTH) {
                    Toast.makeText(requireContext(), "Максимум $MAX_LENGTH цифр", Toast.LENGTH_SHORT).show()
                    return
                }

                when {
                    currentInput == "0" -> {
                        currentInput = digit.toString()
                    }
                    currentInput.isEmpty() -> {
                        currentInput = digit.toString()
                    }
                    else -> {
                        currentInput += digit.toString()
                    }
                }

                inputTextView.text = currentInput
                updateResult()
            }
        }
    }

    protected fun updateResult() {
        if (currentInput.isEmpty()) {
            resultTextView.text = ""
            return
        }
        try {
            val value = currentInput.toDouble()
            val fromUnit = getSelectedUnit(fromUnitSpinner)
            val toUnit = getSelectedUnit(toUnitSpinner)
            val result = Converter.convert(value, fromUnit, toUnit)
            resultTextView.text = formatResult(result)
        } catch (e: NumberFormatException) {
            resultTextView.text = "Ошибка"
        }
    }

    private fun getSelectedUnit(spinner: Spinner): Unit {
        val category = categories[categorySpinner.selectedItemPosition]
        val unitName = spinner.selectedItem as String
        return units.first { it.category == category && it.name == unitName }
    }

    private fun formatResult(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.4f", value).trimEnd('0').trimEnd('.')
        }
    }
}