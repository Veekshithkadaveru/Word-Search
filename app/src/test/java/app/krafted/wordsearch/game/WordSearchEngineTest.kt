package app.krafted.wordsearch.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordSearchEngineTest {
    private val engine = WordSearchEngine()

    @Test
    fun generateGridCreatesStableTwelveByTwelveBoard() {
        val first = engine.generateGrid(
            words = listOf("joker", "stack", "heart", "deal"),
            seed = 1_001L
        )
        val second = engine.generateGrid(
            words = listOf("joker", "stack", "heart", "deal"),
            seed = 1_001L
        )

        assertEquals(12, first.grid.size)
        assertTrue(first.grid.all { it.size == 12 })
        assertEquals(first.grid.serialize(), second.grid.serialize())
        assertEquals(first.placedWords, second.placedWords)
    }

    @Test
    fun canPlaceRejectsOutOfBoundsPlacements() {
        val grid = Array(12) { CharArray(12) }

        assertFalse(
            engine.canPlace(
                grid = grid,
                word = "JOKER",
                startRow = 10,
                startCol = 10,
                direction = 0 to 1
            )
        )
    }

    @Test
    fun canPlaceRejectsConflictingLettersAndAllowsMatchingOverlap() {
        val conflictingGrid = Array(12) { CharArray(12) }
        conflictingGrid[4][4] = 'X'

        assertFalse(
            engine.canPlace(
                grid = conflictingGrid,
                word = "JOKER",
                startRow = 4,
                startCol = 4,
                direction = 0 to 1
            )
        )

        val overlappingGrid = Array(12) { CharArray(12) }
        engine.placeWord(
            grid = overlappingGrid,
            word = "JOKER",
            startRow = 2,
            startCol = 2,
            direction = 0 to 1
        )

        assertTrue(
            engine.canPlace(
                grid = overlappingGrid,
                word = "OK",
                startRow = 2,
                startCol = 3,
                direction = 0 to 1
            )
        )
    }

    @Test
    fun generateGridFillsEveryCellWithUppercaseLettersAndRecordsPlacements() {
        val generated = engine.generateGrid(
            words = listOf("joker", "stack", "heart", "deal"),
            seed = 2_003L
        )

        assertEquals(listOf("JOKER", "STACK", "HEART", "DEAL"), generated.placedWords.map { it.word })
        generated.grid.forEach { row ->
            row.forEach { cell ->
                assertTrue(cell in 'A'..'Z')
            }
        }
        generated.placedWords.forEach { placedWord ->
            assertEquals(placedWord.word, generated.grid.readWord(placedWord))
        }
    }

    private fun Array<CharArray>.serialize(): List<String> =
        map { it.concatToString() }

    private fun Array<CharArray>.readWord(placedWord: PlacedWord): String {
        val (rowStep, colStep) = placedWord.direction
        return buildString {
            repeat(placedWord.word.length) { index ->
                append(this@readWord[placedWord.startRow + (rowStep * index)][placedWord.startCol + (colStep * index)])
            }
        }
    }
}
