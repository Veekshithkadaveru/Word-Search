package app.krafted.wordsearch.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.krafted.wordsearch.game.GridCell

private const val GRID_SIZE = 12
private val DefaultCellColor = Color(0xFF1A1A1A)
private val CellBorderColor = Color(0xFF333333)
private val WrongFlashColor = Color.Red.copy(alpha = 0.6f)

@Composable
fun WordGrid(
    grid: Array<CharArray>,
    selectedCells: List<GridCell>,
    foundCells: List<GridCell>,
    accentColor: Color,
    isWrongFlash: Boolean,
    onDragStart: (row: Int, col: Int) -> Unit,
    onDragMove: (row: Int, col: Int) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedSet = remember(selectedCells) { selectedCells.toSet() }
    val foundSet = remember(foundCells) { foundCells.toSet() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val density = LocalDensity.current
        val cellSizeDp = maxWidth / GRID_SIZE
        val cellSizePx = with(density) { cellSizeDp.toPx() }
        val fontSize = with(density) { (cellSizePx * 0.5f).toSp() }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val row = (offset.y / cellSizePx).toInt().coerceIn(0, GRID_SIZE - 1)
                            val col = (offset.x / cellSizePx).toInt().coerceIn(0, GRID_SIZE - 1)
                            onDragStart(row, col)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val row =
                                (change.position.y / cellSizePx).toInt().coerceIn(0, GRID_SIZE - 1)
                            val col =
                                (change.position.x / cellSizePx).toInt().coerceIn(0, GRID_SIZE - 1)
                            onDragMove(row, col)
                        },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() }
                    )
                }
        ) {
            for (row in 0 until GRID_SIZE) {
                Row {
                    for (col in 0 until GRID_SIZE) {
                        val cell = GridCell(row, col)
                        val isFound = cell in foundSet
                        val isSelected = cell in selectedSet
                        val targetColor = when {
                            isWrongFlash && isSelected -> WrongFlashColor
                            isFound -> accentColor
                            isSelected -> accentColor.copy(alpha = 0.5f)
                            else -> DefaultCellColor
                        }
                        val animatedColor by animateColorAsState(
                            targetValue = targetColor,
                            animationSpec = tween(durationMillis = if (isFound) 150 else 100),
                            label = "cellColor"
                        )
                        val pulseScale = remember { Animatable(1f) }
                        LaunchedEffect(isFound) {
                            if (isFound) {
                                pulseScale.animateTo(
                                    1.15f,
                                    animationSpec = tween(durationMillis = 80)
                                )
                                pulseScale.animateTo(
                                    1f,
                                    animationSpec = spring(
                                        dampingRatio = 0.5f,
                                        stiffness = 500f
                                    )
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(cellSizeDp)
                                .graphicsLayer {
                                    scaleX = pulseScale.value
                                    scaleY = pulseScale.value
                                }
                                .background(animatedColor)
                                .border(0.5.dp, CellBorderColor),
                            contentAlignment = Alignment.Center
                        ) {
                            val letter = grid.getOrNull(row)?.getOrNull(col) ?: ' '
                            Text(
                                text = letter.toString(),
                                color = Color.White,
                                fontSize = fontSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
