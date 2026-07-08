package com.example.converter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast

class DataFragment : BaseDataFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_data, container, false)
        initCommonViews(view)

        // Кнопка обмена единицами
        val swapButton = view.findViewById<Button>(R.id.btn_swap)
        swapButton.setOnClickListener { swapUnitsAndValues() }

        // Кнопки копирования
        view.findViewById<Button>(R.id.btn_copy_input).setOnClickListener {
            copyToClipboard(inputTextView.text.toString())
        }
        view.findViewById<Button>(R.id.btn_copy_result).setOnClickListener {
            copyToClipboard(resultTextView.text.toString())
        }

        // Кнопка вставки (если есть в разметке)
        val pasteButton = view.findViewById<Button>(R.id.btn_paste)
        pasteButton?.setOnClickListener { pasteFromClipboard() }

        return view
    }

    private fun swapUnitsAndValues() {
        val resultText = resultTextView.text.toString()
        val inputText = inputTextView.text.toString()

        // Проверяем, что поля содержат числа или пусты
        val isInputValid = inputText.isEmpty() || try { inputText.toDouble(); true } catch (e: NumberFormatException) { false }
        val isResultValid = resultText.isEmpty() || try { resultText.toDouble(); true } catch (e: NumberFormatException) { false }

        if (isInputValid && isResultValid) {
            val fromPos = fromUnitSpinner.selectedItemPosition
            val toPos = toUnitSpinner.selectedItemPosition

            // Меняем тексты
            inputTextView.text = resultText
            resultTextView.text = inputText
            currentInput = resultText  // теперь входным становится бывший результат

            // Меняем единицы
            fromUnitSpinner.setSelection(toPos)
            toUnitSpinner.setSelection(fromPos)

            // Пересчитываем результат
            updateResult()
        } else {
            Toast.makeText(requireContext(), "Невозможно обменять: поля содержат некорректные значения", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Converted value", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Copied", Toast.LENGTH_SHORT).show()
    }

    private fun pasteFromClipboard() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val pastedText = clip.getItemAt(0).text.toString()
            // Проверяем, что это число (целое или дробное)
            if (pastedText.matches(Regex("-?\\d+(\\.\\d+)?"))) {
                currentInput = pastedText
                inputTextView.text = pastedText
                updateResult()
            } else {
                Toast.makeText(requireContext(), "Clipboard does not contain a number", Toast.LENGTH_SHORT).show()
            }
        }
    }
}