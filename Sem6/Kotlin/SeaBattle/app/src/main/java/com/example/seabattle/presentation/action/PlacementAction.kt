package com.example.seabattle.presentation.action

sealed interface PlacementAction {
    data class Init(val gameId: String, val isHost: Boolean) : PlacementAction
    data class SelectShip(val size: Int) : PlacementAction
    object ToggleOrientation : PlacementAction
    data class TryPlaceShip(val x: Int, val y: Int) : PlacementAction
    data class PlaceDraggedShip(val size: Int, val x: Int, val y: Int) : PlacementAction
    object RandomPlacement : PlacementAction
    object ConfirmPlacement : PlacementAction
    object ClearErrorMessage : PlacementAction
    object UndoLastShip : PlacementAction
    object ClearBoard : PlacementAction
}