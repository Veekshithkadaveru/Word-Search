package app.krafted.wordsearch.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectionValidatorTest {
    @Test
    fun sameCellReturnsSingleCellPath() {
        val cell = GridCell(row = 5, col = 5)

        assertTrue(SelectionValidator.isValidExtension(cell, cell))
        assertEquals(listOf(cell), SelectionValidator.getCellsBetween(cell, cell))
    }

    @Test
    fun getCellsBetweenSupportsHorizontalVerticalAndDiagonalDirections() {
        val cases = listOf(
            Triple(
                GridCell(2, 2),
                GridCell(2, 5),
                listOf(GridCell(2, 2), GridCell(2, 3), GridCell(2, 4), GridCell(2, 5))
            ),
            Triple(
                GridCell(2, 5),
                GridCell(2, 2),
                listOf(GridCell(2, 5), GridCell(2, 4), GridCell(2, 3), GridCell(2, 2))
            ),
            Triple(
                GridCell(1, 4),
                GridCell(4, 4),
                listOf(GridCell(1, 4), GridCell(2, 4), GridCell(3, 4), GridCell(4, 4))
            ),
            Triple(
                GridCell(4, 4),
                GridCell(1, 4),
                listOf(GridCell(4, 4), GridCell(3, 4), GridCell(2, 4), GridCell(1, 4))
            ),
            Triple(
                GridCell(1, 1),
                GridCell(4, 4),
                listOf(GridCell(1, 1), GridCell(2, 2), GridCell(3, 3), GridCell(4, 4))
            ),
            Triple(
                GridCell(4, 4),
                GridCell(1, 1),
                listOf(GridCell(4, 4), GridCell(3, 3), GridCell(2, 2), GridCell(1, 1))
            ),
            Triple(
                GridCell(1, 4),
                GridCell(4, 1),
                listOf(GridCell(1, 4), GridCell(2, 3), GridCell(3, 2), GridCell(4, 1))
            ),
            Triple(
                GridCell(4, 1),
                GridCell(1, 4),
                listOf(GridCell(4, 1), GridCell(3, 2), GridCell(2, 3), GridCell(1, 4))
            )
        )

        cases.forEach { (start, end, expected) ->
            assertTrue(SelectionValidator.isValidExtension(start, end))
            assertEquals(expected, SelectionValidator.getCellsBetween(start, end))
        }
    }

    @Test
    fun nonStraightPathsAreRejected() {
        val start = GridCell(row = 0, col = 0)
        val end = GridCell(row = 2, col = 3)

        assertFalse(SelectionValidator.isValidExtension(start, end))
        assertTrue(SelectionValidator.getCellsBetween(start, end).isEmpty())
    }
}
