package app.krafted.wordsearch.game

import java.util.Locale
import kotlin.random.Random

class WordSearchEngine(
    private val gridSize: Int = GRID_SIZE
) {
    fun generateGrid(words: List<String>, seed: Long): GeneratedGrid {
        val normalizedWords = words.mapIndexed { index, word ->
            IndexedWord(index = index, word = word.trim().uppercase(Locale.ROOT))
        }
        require(normalizedWords.all { it.word.isNotEmpty() }) {
            "Words must not be blank"
        }
        require(normalizedWords.all { it.word.length <= gridSize }) {
            "Words must fit within a ${gridSize}x$gridSize grid"
        }

        val placementOrder = normalizedWords.sortedByDescending { it.word.length }
        val random = Random(seed)

        repeat(MAX_BOARD_ATTEMPTS) {
            val grid = Array(gridSize) { CharArray(gridSize) { EMPTY_CELL } }
            val placements = mutableListOf<IndexedPlacement>()
            var boardSucceeded = true

            for (indexedWord in placementOrder) {
                var wordPlaced = false

                for (attempt in 0 until PLACEMENT_ATTEMPTS_PER_WORD) {
                    val direction = DIRECTIONS[random.nextInt(DIRECTIONS.size)]
                    val startRow = random.nextInt(gridSize)
                    val startCol = random.nextInt(gridSize)

                    if (!canPlace(
                            grid = grid,
                            word = indexedWord.word,
                            startRow = startRow,
                            startCol = startCol,
                            direction = direction
                        )
                    ) {
                        continue
                    }

                    placeWord(
                        grid = grid,
                        word = indexedWord.word,
                        startRow = startRow,
                        startCol = startCol,
                        direction = direction
                    )
                    placements += IndexedPlacement(
                        index = indexedWord.index,
                        placedWord = PlacedWord(
                            word = indexedWord.word,
                            startRow = startRow,
                            startCol = startCol,
                            direction = direction
                        )
                    )
                    wordPlaced = true
                    break
                }

                if (!wordPlaced) {
                    boardSucceeded = false
                    break
                }
            }

            if (boardSucceeded) {
                fillEmptyCells(grid, random)
                return GeneratedGrid(
                    grid = grid,
                    placedWords = placements
                        .sortedBy { it.index }
                        .map { it.placedWord }
                )
            }
        }

        throw IllegalStateException(
            "Failed to generate grid after $MAX_BOARD_ATTEMPTS attempts for seed=$seed words=${
                normalizedWords.map { it.word }
            }"
        )
    }

    internal fun canPlace(
        grid: Array<CharArray>,
        word: String,
        startRow: Int,
        startCol: Int,
        direction: Pair<Int, Int>
    ): Boolean {
        val (rowStep, colStep) = direction

        word.forEachIndexed { index, letter ->
            val row = startRow + (rowStep * index)
            val col = startCol + (colStep * index)
            if (row !in 0 until gridSize || col !in 0 until gridSize) {
                return false
            }

            val cellValue = grid[row][col]
            if (cellValue != EMPTY_CELL && cellValue != letter) {
                return false
            }
        }

        return true
    }

    internal fun placeWord(
        grid: Array<CharArray>,
        word: String,
        startRow: Int,
        startCol: Int,
        direction: Pair<Int, Int>
    ) {
        val (rowStep, colStep) = direction
        word.forEachIndexed { index, letter ->
            val row = startRow + (rowStep * index)
            val col = startCol + (colStep * index)
            grid[row][col] = letter
        }
    }

    private fun fillEmptyCells(grid: Array<CharArray>, random: Random) {
        grid.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, value ->
                if (value == EMPTY_CELL) {
                    grid[rowIndex][colIndex] = ('A'.code + random.nextInt(ALPHABET_SIZE)).toChar()
                }
            }
        }
    }

    private data class IndexedWord(
        val index: Int,
        val word: String
    )

    private data class IndexedPlacement(
        val index: Int,
        val placedWord: PlacedWord
    )

    private companion object {
        const val GRID_SIZE = 12
        const val PLACEMENT_ATTEMPTS_PER_WORD = 100
        const val MAX_BOARD_ATTEMPTS = 50
        const val ALPHABET_SIZE = 26
        const val EMPTY_CELL = '\u0000'

        val DIRECTIONS = listOf(
            -1 to -1,
            -1 to 0,
            -1 to 1,
            0 to -1,
            0 to 1,
            1 to -1,
            1 to 0,
            1 to 1
        )
    }
}
