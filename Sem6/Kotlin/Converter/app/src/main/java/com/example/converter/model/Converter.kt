package com.example.converter.model

object Converter {
    fun convert(value: Double, fromUnit: Unit, toUnit: Unit): Double {
        val baseValue = value * fromUnit.baseCoefficient
        return baseValue / toUnit.baseCoefficient
    }
}