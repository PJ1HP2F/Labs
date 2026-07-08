package com.example.seabattle.presentation.screen

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.seabattle.R
import com.example.seabattle.data.entity.game.CellStatus
import com.example.seabattle.data.entity.game.GameStatus
import com.example.seabattle.presentation.action.GameAction
import com.example.seabattle.presentation.viewModel.BattleViewModel
import kotlinx.coroutines.delay

@Composable
fun BattleScreen(
    battleViewModel: BattleViewModel,
    gameId: String,
    onNavigateToProfile: () -> Unit
) {
    val gameState by battleViewModel.gameState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        battleViewModel.error.collect { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    LaunchedEffect(gameId) {
        battleViewModel.onAction(GameAction.Init(gameId))
    }

    val isHost = gameState.hostId == gameState.myId

    val opponentCells = if (isHost) gameState.guestCells else gameState.hostCells
    val myCells = if (isHost) gameState.hostCells else gameState.guestCells

    var aimTarget by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    LaunchedEffect(aimTarget) {
        aimTarget?.let { (x, y) ->
            delay(500)
            battleViewModel.onAction(GameAction.MakeMove(x, y))
            aimTarget = null
        }
    }

    if (gameState.status == GameStatus.FINISHED) {
        val isWinner = gameState.winnerId == gameState.myId
        AlertDialog(
            onDismissRequest = { },
            title = { Text(text = if (isWinner) "Победа!" else "Поражение!") },
            text = { Text(text = if (isWinner) "Поздравляем, вы уничтожили все корабли противника!" else "Все ваши корабли были уничтожены.") },
            confirmButton = {
                TextButton(onClick = onNavigateToProfile) {
                    Text("В главное меню")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            val availableWidth = maxWidth
            val availableHeight = maxHeight
            val centerIndicatorSize = 64.dp
            val horizontalGap = 8.dp

            val boardAreaWidth = (availableWidth - centerIndicatorSize - horizontalGap * 2).coerceAtLeast(0.dp)
            val sizeByWidth = boardAreaWidth / 22
            val sizeByHeight = availableHeight / 11
            val cellSize = minOf(sizeByWidth, sizeByHeight)

            val handTransition = rememberInfiniteTransition(label = "hand_transition")
            val handOffset by handTransition.animateFloat(
                initialValue = -2f,
                targetValue = 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 900, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "hand_offset"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BattleBoard(
                        cells = myCells,
                        cellSize = cellSize,
                        showUndamagedShips = true,
                        isOpponent = false,
                        aimTarget = null,
                        onCellClick = null
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = horizontalGap)
                            .size(centerIndicatorSize),
                        contentAlignment = Alignment.Center
                    ) {
                        if (gameState.status == GameStatus.ACTIVE) {
                            Image(
                                painter = painterResource(if (gameState.myTurn) R.drawable.right_hand else R.drawable.left_hand),
                                contentDescription = if (gameState.myTurn) "Ваш ход" else "Ход противника",
                                modifier = Modifier
                                    .size(centerIndicatorSize)
                                    .graphicsLayer { translationY = handOffset }
                            )
                        }
                    }

                    BattleBoard(
                        cells = opponentCells,
                        cellSize = cellSize,
                        showUndamagedShips = gameState.status == GameStatus.FINISHED,
                        isOpponent = true,
                        aimTarget = aimTarget,
                        onCellClick = { x, y ->
                            if (gameState.myTurn && gameState.status == GameStatus.ACTIVE && aimTarget == null) {
                                aimTarget = Pair(x, y)
                            }
                        }
                    )
                }
            }
        }
    }
}

data class ReconstructedShip(val x: Int, val y: Int, val size: Int, val isHorizontal: Boolean)

fun reconstructShips(cells: List<CellStatus>, showUndamaged: Boolean): List<ReconstructedShip> {
    val ships = mutableListOf<ReconstructedShip>()
    val visited = BooleanArray(100)
    for (y in 0 until 10) {
        for (x in 0 until 10) {
            val idx = y * 10 + x
            val hasShip = cells[idx] == CellStatus.SHIP ||
                cells[idx] == CellStatus.SUBMARINE ||
                cells[idx] == CellStatus.SUBMARINE_REVEALED ||
                cells[idx] == CellStatus.HURT ||
                cells[idx] == CellStatus.DESTROYED
            if (hasShip && !visited[idx]) {
                var size = 1
                var isHorizontal = true
                var cx = x + 1
                while (cx < 10 && (
                        cells[y * 10 + cx] == CellStatus.SHIP ||
                            cells[y * 10 + cx] == CellStatus.SUBMARINE ||
                            cells[y * 10 + cx] == CellStatus.SUBMARINE_REVEALED ||
                            cells[y * 10 + cx] == CellStatus.HURT ||
                            cells[y * 10 + cx] == CellStatus.DESTROYED
                        )) {
                    visited[y * 10 + cx] = true
                    size++
                    cx++
                }
                if (size == 1) {
                    var cy = y + 1
                    while (cy < 10 && (
                            cells[cy * 10 + x] == CellStatus.SHIP ||
                                cells[cy * 10 + x] == CellStatus.SUBMARINE ||
                                cells[cy * 10 + x] == CellStatus.SUBMARINE_REVEALED ||
                                cells[cy * 10 + x] == CellStatus.HURT ||
                                cells[cy * 10 + x] == CellStatus.DESTROYED
                            )) {
                        visited[cy * 10 + x] = true
                        size++
                        cy++
                        isHorizontal = false
                    }
                }
                visited[idx] = true

                val isFullyDestroyed = (0 until size).all { i ->
                    val cIdx = if (isHorizontal) y * 10 + (x + i) else (y + i) * 10 + x
                    cells.getOrNull(cIdx) == CellStatus.DESTROYED
                }

                if (showUndamaged || isFullyDestroyed) {
                    ships.add(ReconstructedShip(x, y, size, isHorizontal))
                }
            }
        }
    }
    return ships
}

@Composable
fun BattleBoard(
    cells: List<CellStatus>,
    cellSize: Dp,
    showUndamagedShips: Boolean,
    isOpponent: Boolean,
    aimTarget: Pair<Int, Int>?,
    onCellClick: ((Int, Int) -> Unit)?
) {
    val aimTransition = rememberInfiniteTransition(label = "aim_transition")
    val aimPulse by aimTransition.animateFloat(
        initialValue = 0.78f,
        targetValue = 0.94f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aim_pulse"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            Spacer(modifier = Modifier.width(cellSize))
            "АБВГДЕЁЖЗИ".forEach { char ->
                Box(modifier = Modifier.size(cellSize), contentAlignment = Alignment.Center) {
                    Text(text = char.toString(), fontSize = (cellSize.value * 0.3f).sp)
                }
            }
        }
        Box {
            Column {
                for (row in 0 until 10) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.width(cellSize), contentAlignment = Alignment.Center) {
                            Text(text = "${row + 1}", fontSize = (cellSize.value * 0.3f).sp)
                        }
                        for (col in 0 until 10) {
                            val cellStatus = cells.getOrNull(row * 10 + col) ?: CellStatus.EMPTY
                            val canClick = onCellClick != null && (
                                cellStatus == CellStatus.EMPTY ||
                                    cellStatus == CellStatus.SHIP ||
                                    cellStatus == CellStatus.SUBMARINE ||
                                    cellStatus == CellStatus.SUBMARINE_REVEALED
                                )
                            Box(
                                modifier = Modifier
                                    .size(cellSize)
                                    .border(1.dp, Color.LightGray)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .then(
                                        if (canClick) Modifier.clickable { onCellClick(col, row) } else Modifier
                                    )
                            )
                        }
                    }
                }
            }

            val ships = androidx.compose.runtime.remember(cells, showUndamagedShips) {
                reconstructShips(cells, showUndamagedShips)
            }
            ships.forEach { ship ->
                val shipPainter = when (ship.size) {
                    1 -> if (ship.isHorizontal) painterResource(R.drawable.first_ship) else painterResource(R.drawable.first_ship_vert)
                    2 -> if (ship.isHorizontal) painterResource(R.drawable.second_ship) else painterResource(R.drawable.second_ship_vert)
                    3 -> if (ship.isHorizontal) painterResource(R.drawable.third_ship) else painterResource(R.drawable.third_ship_vert)
                    else -> if (ship.isHorizontal) painterResource(R.drawable.forth_ship) else painterResource(R.drawable.forth_ship_vert)
                }

                val widthDp = if (ship.isHorizontal) cellSize * ship.size else cellSize
                val heightDp = if (ship.isHorizontal) cellSize else cellSize * ship.size

                Image(
                    painter = shipPainter,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .offset(x = cellSize * (ship.x + 1), y = cellSize * ship.y)
                        .size(width = widthDp, height = heightDp)
                )
            }

            // Маркеры
            Column {
                for (row in 0 until 10) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(cellSize))
                        for (col in 0 until 10) {
                            val cellStatus = cells.getOrNull(row * 10 + col) ?: CellStatus.EMPTY
                            val isAiming = isOpponent && aimTarget == Pair(col, row)
                            Box(modifier = Modifier.size(cellSize), contentAlignment = Alignment.Center) {
                                if (isAiming) {
                                    Image(
                                        painter = painterResource(id = R.drawable.aim),
                                        contentDescription = "Aim",
                                        modifier = Modifier.size(cellSize * aimPulse)
                                    )
                                } else {
                                    when (cellStatus) {
                                        CellStatus.HURT -> Image(painter = painterResource(id = R.drawable.hit), contentDescription = "Hit", modifier = Modifier.size(cellSize * 0.8f))
                                        CellStatus.MISS -> Image(painter = painterResource(id = R.drawable.miss), contentDescription = "Miss", modifier = Modifier.size(cellSize * 0.8f))
                                        CellStatus.DESTROYED -> Image(painter = painterResource(id = R.drawable.kill), contentDescription = "Kill", modifier = Modifier.size(cellSize * 0.8f))
                                        CellStatus.SUBMARINE_REVEALED -> Image(painter = painterResource(id = R.drawable.submarine_ping), contentDescription = "Submarine spotted", modifier = Modifier.size(cellSize * 0.72f))
                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
