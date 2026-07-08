package com.example.seabattle.presentation.effect

sealed interface PlacementEffect {
    object NavigateToBattle : PlacementEffect
    data class ShowSnackbar(val message: String) : PlacementEffect
}