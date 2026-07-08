package com.example.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object CalculatorLogic {
    private val mc = MathContext(16, RoundingMode.HALF_UP)

    fun evaluate(expression: String): BigDecimal {
        if (expression.isBlank()) return BigDecimal.ZERO
        
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): BigDecimal {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): BigDecimal {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x = x.add(parseTerm(), mc)
                    else if (eat('-'.code)) x = x.subtract(parseTerm(), mc)
                    else return x
                }
            }

            fun parseTerm(): BigDecimal {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x = x.multiply(parseFactor(), mc)
                    else if (eat('/'.code)) {
                        val divisor = parseFactor()
                        if (divisor.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("Division by zero")
                        x = x.divide(divisor, mc)
                    }
                    else if (eat('%'.code)) x = x.remainder(parseFactor(), mc)
                    else return x
                }
            }

            fun parseFactor(): BigDecimal {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return parseFactor().negate()
                var x: BigDecimal
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                    while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                    val str = expression.substring(startPos, pos)
                    x = try {
                        BigDecimal(str, mc)
                    } catch (e: Exception) {
                        BigDecimal.ZERO
                    }
                } else if (ch >= 'a'.code && ch <= 'z'.code || ch == '√'.code.toInt()) {
                    while (ch >= 'a'.code && ch <= 'z'.code || ch == '√'.code.toInt()) nextChar()
                    val func = expression.substring(startPos, pos)
                    if (eat('('.code)) {
                        x = parseExpression()
                        eat(')'.code)
                    } else {
                        x = parseFactor()
                    }
                    x = when (func) {
                        "√" -> customSqrt(x)
                        else -> throw RuntimeException("Unknown function")
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                
                while (true) {
                    if (eat('!'.code)) {
                        try {
                            x = factorial(x.toBigInteger().toInt())
                        } catch (e: Exception) {
                            throw RuntimeException("Factorial error")
                        }
                    }
                    else if (eat('^'.code)) {
                        val exponent = parseFactor()
                        x = customPow(x, exponent)
                    }
                    else break
                }
                
                return x
            }
        }.parse()
    }

    private fun customSqrt(n: BigDecimal): BigDecimal {
        if (n.signum() == -1) throw ArithmeticException("Root of negative")
        if (n.signum() == 0) return BigDecimal.ZERO
        
        // Начальное приближение
        var x = n.divide(BigDecimal.valueOf(2), mc)
        if (x.signum() == 0) x = BigDecimal("0.1")
        
        // Метод Ньютона (Герона)
        repeat(50) {
            val nextX = n.divide(x, mc).add(x, mc).divide(BigDecimal.valueOf(2), mc)
            if (x == nextX) return@repeat
            x = nextX
        }
        return x
    }

    private fun customPow(base: BigDecimal, exponent: BigDecimal): BigDecimal {
        val isInteger = try {
            exponent.stripTrailingZeros().scale() <= 0
        } catch (e: Exception) {
            false
        }

        return if (isInteger) {
            var e = exponent.toLong()
            var b = base
            if (e == 0L) return BigDecimal.ONE
            if (e < 0) {
                b = BigDecimal.ONE.divide(b, mc)
                e = -e
            }
            var res = BigDecimal.ONE
            while (e > 0) {
                if (e % 2 == 1L) res = res.multiply(b, mc)
                b = b.multiply(b, mc)
                e /= 2
            }
            res
        } else {
            BigDecimal(Math.pow(base.toDouble(), exponent.toDouble()).toString(), mc)
        }
    }

    private fun factorial(n: Int): BigDecimal {
        if (n < 0) throw ArithmeticException("Negative factorial")
        if (n > 1000) throw ArithmeticException("Too large factorial")
        var res = BigDecimal.ONE
        for (i in 1..n) res = res.multiply(BigDecimal(i), mc)
        return res
    }
}