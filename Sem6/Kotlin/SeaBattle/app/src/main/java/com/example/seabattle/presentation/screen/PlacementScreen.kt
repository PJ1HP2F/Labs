package com.example.seabattle.presentation.screen

import android.content.ClipData
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.seabattle.R
import com.example.seabattle.presentation.action.PlacementAction
import com.example.seabattle.presentation.effect.PlacementEffect
import com.example.seabattle.presentation.viewModel.PlacementViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacementScreen(
    viewModel: PlacementViewModel,
    gameId: String,
    isHost: Boolean,
    onNavigateToBattle: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.onAction(PlacementAction.Init(gameId, isHost))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PlacementEffect.NavigateToBattle -> onNavigateToBattle()
                is PlacementEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val density = LocalDensity.current
    val panelScrollState = rememberScrollState()

    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            val availableWidth = maxWidth
            val availableHeight = maxHeight

            val cellSize = with(density) {
                val sizeByWidth = availableWidth / 11
                val sizeByHeight = availableHeight / 11
                minOf(sizeByWidth, sizeByHeight).coerceIn(24.dp, 45.dp)
            }

            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {
                // Игровое поле
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Буквы
                    Row {
                        Spacer(modifier = Modifier.width(cellSize))
                        "АБВГДЕЁЖЗИ".forEach { char ->
                            Box(
                                modifier = Modifier.size(cellSize),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = char.toString(), fontSize = (cellSize.value * 0.3f).sp)
                            }
                        }
                    }

                    Box {
                        Column {
                            for (row in 0 until 10) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.width(cellSize),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${row + 1}",
                                            fontSize = (cellSize.value * 0.3f).sp
                                        )
                                    }
                                    for (col in 0 until 10) {
                                        Box(
                                            modifier = Modifier
                                                .size(cellSize)
                                                .border(1.dp, Color.LightGray)
                                                .background(Color.White.copy(alpha = 0.2f))
                                                .clickable {
                                                    viewModel.onAction(
                                                        PlacementAction.TryPlaceShip(
                                                            col,
                                                            row
                                                        )
                                                    )
                                                }
                                        )
                                    }
                                }
                            }
                        }

                        state.placedShips.forEach { ship ->
                            val shipPainter = when (ship.size) {
                                1 -> if (ship.isHorizontal) painterResource(R.drawable.first_ship) else painterResource(
                                    R.drawable.first_ship_vert
                                )

                                2 -> if (ship.isHorizontal) painterResource(R.drawable.second_ship) else painterResource(
                                    R.drawable.second_ship_vert
                                )

                                3 -> if (ship.isHorizontal) painterResource(R.drawable.third_ship) else painterResource(
                                    R.drawable.third_ship_vert
                                )

                                else -> if (ship.isHorizontal) painterResource(R.drawable.forth_ship) else painterResource(
                                    R.drawable.forth_ship_vert
                                )
                            }

                            val widthDp = if (ship.isHorizontal) cellSize * ship.size else cellSize
                            val heightDp = if (ship.isHorizontal) cellSize else cellSize * ship.size

                            Image(
                                painter = shipPainter,
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .offset(
                                        x = cellSize * (ship.x + 1),
                                        y = cellSize * ship.y
                                    )
                                    .size(width = widthDp, height = heightDp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .width(340.dp)
                        .verticalScroll(panelScrollState),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "id игры: ${state.gameId}",
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.clickable(
                            onClick = {
                                scope.launch {
                                    val clipEntry = ClipEntry(ClipData.newPlainText("Game ID", state.gameId))
                                    clipboard.setClipEntry(clipEntry)
                                }
                            }
                        )
                    )

                    Button(
                        onClick = { viewModel.onAction(PlacementAction.ToggleOrientation) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ориентация: ${if (state.isHorizontal) "Гориз." else "Вертик."}")
                    }

                    Button(
                        onClick = { viewModel.onAction(PlacementAction.RandomPlacement) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    ) {
                        Text("Случайная расстановка")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.onAction(PlacementAction.UndoLastShip) },
                            modifier = Modifier.weight(1f),
                            enabled = state.placedShips.isNotEmpty() && !state.isLoading
                        ) {
                            Text("Отмена")
                        }

                        Button(
                            onClick = { viewModel.onAction(PlacementAction.ClearBoard) },
                            modifier = Modifier.weight(1f),
                            enabled = state.placedShips.isNotEmpty() && !state.isLoading
                        ) {
                            Text("Очистить")
                        }
                    }

                    Text("Корабли:", fontWeight = FontWeight.Bold)
                    Text(
                        text = "3- и 4-палубные — подлодки: скрыты у соперника и требуют 2 попадания по клетке",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    listOf(1, 2, 3, 4).forEach { size ->
                        val count = state.remainingShips[size] ?: 0
                        if (count > 0) {
                            ShipItem(
                                size = size,
                                count = count,
                                isSelected = state.selectedShipSize == size,
                                onClick = { viewModel.onAction(PlacementAction.SelectShip(size)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { viewModel.onAction(PlacementAction.ConfirmPlacement) },
                        enabled = state.remainingShips.values.all { it == 0 } && !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Готово")
                    }
                }
            }
        }
    }
}

@Composable
fun ShipItem(
    size: Int,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val painter = when (size) {
        1 -> painterResource(R.drawable.first_ship)
        2 -> painterResource(R.drawable.second_ship)
        3 -> painterResource(R.drawable.third_ship)
        else -> painterResource(R.drawable.forth_ship)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            Image(painter = painter, contentDescription = null, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("${size}-палубный x$count")
        }
    }
}