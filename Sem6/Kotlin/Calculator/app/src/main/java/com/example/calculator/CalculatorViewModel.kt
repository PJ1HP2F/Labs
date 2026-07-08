package com.example.calculator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.math.BigDecimal

class CalculatorViewModel : ViewModel() {
    // Текущая строка ввода
    val expression = MutableLiveData<String>("")
    // Результат вычислений
    val result = MutableLiveData<String>("")

    private var isCalculated = false

    fun addSymbol(symbol: String) {
        if (isCalculated) {
            expression.value = ""
            isCalculated = false
        }
        val current = expression.value ?: ""
        
        // Базовая проверка ввода
        if (symbol == "." && (current.isEmpty() || current.last() == '.' || isLastPartContainsDot(current))) return
        if (isOperator(symbol) && (current.isEmpty() || isOperator(current.last().toString()))) return

        expression.value = current + symbol
    }

    private fun isLastPartContainsDot(expr: String): Boolean {
        val lastPart = expr.split(Regex("[+\\-*/%^!]")).lastOrNull() ?: ""
        return lastPart.contains(".")
    }

    private fun isOperator(s: String): Boolean = s in listOf("+", "-", "*", "/", "%", "^")

    fun clear() {
        expression.value = ""
        result.value = ""
        isCalculated = false
    }

    fun deleteLast() {
        val current = expression.value ?: ""
        if (current.isNotEmpty()) {
            expression.value = current.dropLast(1)
        }
        isCalculated = false
    }

    fun calculate() {
        val currentExpr = expression.value ?: ""
        if (currentExpr.isBlank()) return
        
        try {
            val res = CalculatorLogic.evaluate(currentExpr)
            // Вывод без экспоненциальной записи и лишних нулей
            result.value = res.stripTrailingZeros().toPlainString()
            isCalculated = true
        } catch (e: Exception) {
            result.value = "Ошибка"
            isCalculated = true
        }
    }

    fun applyFactorial() {
        if (isCalculated) {
            expression.value = ""
            isCalculated = false
        }
        val current = expression.value ?: ""
        if (current.isNotEmpty() && (current.last().isDigit() || current.last() == ')')) {
            addSymbol("!")
        }
    }
}