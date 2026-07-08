package com.example.converter.model

data class Unit(
    val name: String,
    val category: Category,
    val baseCoefficient: Double
)

object Units {
    val all = listOf(
        Unit("Метр", Category.LENGTH, 1.0),
        Unit("Километр", Category.LENGTH, 1000.0),
        Unit("Миля", Category.LENGTH, 1609.34),
        Unit("Грамм", Category.WEIGHT, 1.0),
        Unit("Килограмм", Category.WEIGHT, 1000.0),
        Unit("Фунт", Category.WEIGHT, 453.592),
        Unit("Рубль", Category.CURRENCY, 1.0),
        Unit("Доллар", Category.CURRENCY, 2.96),
        Unit("Евро", Category.CURRENCY, 3.4)
    )
}