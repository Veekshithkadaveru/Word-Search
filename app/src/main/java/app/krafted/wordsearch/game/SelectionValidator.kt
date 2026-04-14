package app.krafted.wordsearch.game

import kotlin.math.abs

object SelectionValidator {
    fun isValidExtension(start: GridCell, end: GridCell): Boolean =
        getCellsBetween(start, end).isNotEmpty()

    fun getCellsBetween(start: GridCell, end: GridCell): List<GridCell> {
        val rowDelta = end.row - start.row
        val colDelta = end.col - start.col
        if (!isStraightLine(rowDelta, colDelta)) {
            return emptyList()
        }

        val steps = maxOf(abs(rowDelta), abs(colDelta))
        val rowStep = rowDelta.normalizeStep()
        val colStep = colDelta.normalizeStep()

        return List(steps + 1) { index ->
            GridCell(
                row = start.row + (rowStep * index),
                col = start.col + (colStep * index)
            )
        }
    }

    private fun isStraightLine(rowDelta: Int, colDelta: Int): Boolean =
        rowDelta == 0 || colDelta == 0 || abs(rowDelta) == abs(colDelta)

    private fun Int.normalizeStep(): Int = when {
        this > 0 -> 1
        this < 0 -> -1
        else -> 0
    }
}
